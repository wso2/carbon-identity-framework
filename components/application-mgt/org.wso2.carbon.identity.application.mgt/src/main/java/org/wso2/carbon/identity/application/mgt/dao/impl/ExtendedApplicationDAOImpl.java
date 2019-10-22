/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.Application;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ExtendedApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants.ApplicationTableColumns;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationResourceDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponent;
import org.wso2.carbon.identity.core.CertificateRetrievingException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_BY_TENANT_AND_UUID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_BASIC_APP_INFO_BY_UUID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_APP_FROM_SP_APP_WITH_UUID;

public class ExtendedApplicationDAOImpl extends ApplicationDAOImpl implements ApplicationResourceDAO {

    private static final Log log = LogFactory.getLog(ExtendedApplicationDAOImpl.class);

    @Override
    public Application getApplicationResource(String resourceId,
                                              String tenantDomain) throws IdentityApplicationManagementException {

        // Get the service provider by the id...
        Application application;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            application = getApplicationResourceBasicInfo(connection, resourceId, tenantDomain);

            if (application != null) {
                // Fill in the details from other tables.
                addApplicationConfigs(connection, tenantDomain, application);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot find an application for resourceId:" + resourceId +
                            " in tenantDomain:" + tenantDomain);
                }
            }
        } catch (SQLException | CertificateRetrievingException e) {
            throw new IdentityApplicationManagementException("Failed to retrieve application with " +
                    "resourceId:" + resourceId + " in tenantDomain:" + tenantDomain, e);
        }

        return application;
    }

    /**
     * @param connection
     * @param resourceId
     * @param tenantDomain
     * @return
     * @throws SQLException
     */
    private Application getApplicationResourceBasicInfo(Connection connection,
                                                        String resourceId,
                                                        String tenantDomain) throws SQLException {

        Application application = null;
        if (log.isDebugEnabled()) {
            log.debug("Retrieving basic app details of appResourceId: " + resourceId + " in tenantDomain: " + tenantDomain);
        }

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, LOAD_BASIC_APP_INFO_BY_UUID)) {

            statement.setString(ApplicationTableColumns.UUID, resourceId);
            statement.setInt(ApplicationTableColumns.TENANT_ID,
                    IdentityTenantUtil.getTenantId(tenantDomain));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    application = new Application();
                    application.setApplicationID(resultSet.getInt(ApplicationTableColumns.ID));
                    application.setApplicationName(resultSet.getString(ApplicationTableColumns.APP_NAME));
                    application.setDescription(resultSet.getString(ApplicationTableColumns.DESCRIPTION));
                    application.setApplicationResourceId(resultSet.getString(ApplicationTableColumns.UUID));
                    application.setLoginUrl(resultSet.getString(ApplicationTableColumns.LOGIN_URL));
                    application.setImageUrl(resultSet.getString(ApplicationTableColumns.IMAGE_URL));

                    User owner = new User();
                    owner.setUserName(resultSet.getString(ApplicationTableColumns.USERNAME));
                    owner.setUserStoreDomain(resultSet.getString(ApplicationTableColumns.USER_STORE));
                    owner.setTenantDomain(tenantDomain);
                    application.setOwner(owner);

                    ClaimConfig claimConfig = new ClaimConfig();
                    claimConfig.setUserClaimURI(resultSet.getString(ApplicationTableColumns.SUBJECT_CLAIM_URI));
                    claimConfig.setRoleClaimURI(resultSet.getString(ApplicationTableColumns.ROLE_CLAIM));
                    claimConfig.setLocalClaimDialect(
                            getBooleanValue(resultSet.getString(ApplicationTableColumns.IS_LOCAL_CLAIM_DIALECT)));
                    claimConfig.setAlwaysSendMappedLocalSubjectId(
                            getBooleanValue(resultSet.getString(ApplicationTableColumns.IS_SEND_LOCAL_SUBJECT_ID)));
                    application.setClaimConfig(claimConfig);

                    LocalAndOutboundAuthenticationConfig authConfig = new LocalAndOutboundAuthenticationConfig();
                    authConfig.setAlwaysSendBackAuthenticatedListOfIdPs(
                            getBooleanValue(resultSet.getString(ApplicationTableColumns.IS_SEND_AUTH_LIST_OF_IDPS)));
                    authConfig.setEnableAuthorization(
                            getBooleanValue(resultSet.getString(ApplicationTableColumns.ENABLE_AUTHORIZATION)));
                    authConfig.setSubjectClaimUri(resultSet.getString(ApplicationTableColumns.SUBJECT_CLAIM_URI));
                    application.setLocalAndOutBoundAuthenticationConfig(authConfig);

                    application.setSaasApp(getBooleanValue(resultSet.getString(ApplicationTableColumns.IS_SAAS_APP)));

                    if (log.isDebugEnabled()) {
                        log.debug("ApplicationID: " + application.getApplicationID()
                                + " ApplicationResourceId: " + application.getApplicationResourceId()
                                + " ApplicationName: " + application.getApplicationName()
                                + " ApplicationOwner: " + application.getOwner().toFullQualifiedUsername()
                                + " TenantDomain: " + application.getOwner().getTenantDomain());
                    }
                }
            }

            return application;
        }
    }

    private boolean getBooleanValue(String value) {

        return "1".equals(value);
    }

    @Override
    public String createApplicationResource(Application application,
                                            String tenantDomain) throws IdentityApplicationManagementException {

        return null;
    }

    @Override
    public String createApplicationResource(Application application,
                                            String tenantDomain,
                                            String templateName) throws IdentityApplicationManagementException {

        return null;
    }

    @Override
    public void updateApplicationResource(String applicationResourceId,
                                          String tenantDomain,
                                          Application updatedApp) throws IdentityApplicationManagementException {

        Connection connectionToRollback = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {

            connectionToRollback = connection;

            if (ApplicationManagementServiceComponent.getFileBasedSPs().containsKey(updatedApp.getApplicationName())) {
                throw new IdentityApplicationManagementException(
                        "Application with the same name loaded from the file system.");
            }

            int tenantID = IdentityTenantUtil.getTenantId(tenantDomain);

            // Get the application internal id and uuid.
            int applicationId = updatedApp.getApplicationID();

            // update basic information of the application. You can change application name, description, isSasApp...
            updateBasicApplicationData(updatedApp, tenantDomain, connection);

            updateApplicationCertificate(updatedApp, tenantID, connection);

            updateInboundProvisioningConfiguration(applicationId, updatedApp.getInboundProvisioningConfig(),
                    connection);

            // delete all in-bound authentication requests.
            deleteInboundAuthRequestConfiguration(updatedApp.getApplicationID(), connection);
            // update all in-bound authentication requests.
            updateInboundAuthRequestConfiguration(updatedApp.getApplicationID(), updatedApp
                    .getInboundAuthenticationConfig(), connection);

            // delete local and out-bound authentication configuration.
            deleteLocalAndOutboundAuthenticationConfiguration(applicationId, connection);
            // update local and out-bound authentication configuration.
            updateLocalAndOutboundAuthenticationConfiguration(updatedApp.getApplicationID(),
                    updatedApp.getLocalAndOutBoundAuthenticationConfig(), connection);

            deleteRequestPathAuthenticators(applicationId, connection);
            updateRequestPathAuthenticators(applicationId, updatedApp.getRequestPathAuthenticatorConfigs(),
                    connection);

            deleteClaimConfiguration(applicationId, connection);
            updateClaimConfiguration(updatedApp.getApplicationID(), updatedApp.getClaimConfig(),
                    applicationId, connection);

            deleteOutboundProvisioningConfiguration(applicationId, connection);
            updateOutboundProvisioningConfiguration(applicationId,
                    updatedApp.getOutboundProvisioningConfig(), connection);

            deletePermissionAndRoleConfiguration(applicationId, connection);

            if (updatedApp.getPermissionAndRoleConfig() != null) {
                updatePermissionAndRoleConfiguration(updatedApp.getApplicationID(),
                        updatedApp.getPermissionAndRoleConfig(), connection);
                deleteAssignedPermissions(connection, updatedApp.getApplicationName(),
                        updatedApp.getPermissionAndRoleConfig().getPermissions());
            }

            if (updatedApp.getSpProperties() != null) {
                // To update 'USE_DOMAIN_IN_ROLES' property value.
                updateUseDomainNameInRolesAsSpProperty(updatedApp);
                updateServiceProviderProperties(connection, applicationId, Arrays.asList(updatedApp
                        .getSpProperties()), tenantID);
            }

            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException | UserStoreException e) {
            IdentityDatabaseUtil.rollbackTransaction(connectionToRollback);
            throw new IdentityApplicationManagementException(
                    "Failed to update application with appResourceId:" + applicationResourceId
                            + " in tenantDomain:" + tenantDomain, e);
        }
    }

    @Override
    public void deleteApplicationResource(String appResourceId,
                                          String tenantDomain) throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting Application with appResourceId:" + appResourceId + " in tenantDomain:" + tenantDomain);
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            Application application = getApplicationResource(appResourceId, tenantDomain);

            if (application != null) {
                // Delete the application certificate if there is any
                deleteCertificateByResourceId(connection, application);
                // Delete the inbound authentication configs.
                try (NamedPreparedStatement deleteAppStatement =
                             new NamedPreparedStatement(connection, REMOVE_APP_FROM_SP_APP_WITH_UUID)) {

                    deleteAppStatement.setString(ApplicationTableColumns.UUID, appResourceId);
                    deleteAppStatement.setInt(ApplicationTableColumns.TENANT_ID,
                            IdentityTenantUtil.getTenantId(tenantDomain));
                    deleteAppStatement.execute();

                    IdentityDatabaseUtil.commitTransaction(connection);
                }
            } else {
                String msg = "Trying to delete a non-existing application with appResourceId:%s in tenantDomain:%s";
                throw new IdentityApplicationManagementException(String.format(msg, appResourceId, tenantDomain));
            }

        } catch (SQLException e) {
            String msg = "Error occurred while deleting application with applicationResourceId:%s in tenantDomain:%s.";
            throw new IdentityApplicationManagementException(String.format(msg, appResourceId, tenantDomain));
        }
    }

    @Override
    public ExtendedApplicationBasicInfo getExtendedApplicationBasicInfo(String resourceId,
                                                                        String tenantDomain) throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Getting application basic information for applicationResourceId: " + resourceId
                    + " in tenantDomain: " + tenantDomain);
        }

        ExtendedApplicationBasicInfo applicationBasicInfo = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement =
                         new NamedPreparedStatement(connection, LOAD_APP_BY_TENANT_AND_UUID)) {
                statement.setInt(ApplicationTableColumns.TENANT_ID,
                        IdentityTenantUtil.getTenantId(tenantDomain));
                statement.setString(ApplicationTableColumns.UUID, resourceId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        applicationBasicInfo = buildExtendedApplicationBasicInfo(resultSet);
                    }
                }
            }
        } catch (SQLException e) {
            String message = "Error while getting application basic information for applicationResourceId:%s in " +
                    "tenantDomain:%s";
            throw new IdentityApplicationManagementException(String.format(message, resourceId, tenantDomain), e);
        }

        return applicationBasicInfo;
    }

    private void deleteCertificateByResourceId(Connection connection, Application application) throws SQLException {

        String certificateReferenceID = getCertificateReferenceID(application.getSpProperties());
        if (certificateReferenceID != null) {
            deleteCertificate(connection, Integer.parseInt(certificateReferenceID));
        }
    }

    /**
     * @param application
     * @param connection
     * @throws SQLException
     * @throws UserStoreException
     * @throws IdentityApplicationManagementException
     */

    private void updateBasicApplicationData(Application application,
                                            String tenantDomain,
                                            Connection connection) throws SQLException, UserStoreException,
            IdentityApplicationManagementException {

        String appResourceId = application.getApplicationResourceId();
        String applicationName = application.getApplicationName();

        if (applicationName == null) {
            // check for required attributes.
            throw new IdentityApplicationManagementException("Application Name is required.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Updating Application with appResourceId: " + application.getApplicationResourceId());
        }
        // reads back the Application Name. This is to check if the Application
        // has been renamed
        int applicationId = application.getApplicationID();
        String storedAppName = getApplicationName(applicationId, connection);

        if (log.isDebugEnabled()) {
            log.debug("Stored Application name for applicationResourceId:" + appResourceId + " is " + storedAppName);
        }

        // only if the application has been renamed
        if (isAppRenamed(applicationName, storedAppName)) {
            String newAppRoleName = IdentityUtil.addDomainToName(applicationName, ApplicationConstants.
                    APPLICATION_DOMAIN);
            String oldAppRoleName = IdentityUtil.addDomainToName(storedAppName, ApplicationConstants.
                    APPLICATION_DOMAIN);
            // rename the role
            ApplicationMgtUtil.renameRole(oldAppRoleName, newAppRoleName);
            if (log.isDebugEnabled()) {
                log.debug("Renaming application role from " + storedAppName + " to " + applicationName);
            }
        }

        String sql;
        boolean isValidUserForOwnerUpdate = ApplicationMgtUtil.isValidApplicationOwner(application);
        if (isValidUserForOwnerUpdate) {
            sql = ApplicationMgtDBQueries.UPDATE_EXTENDED_BASIC_APPINFO_WITH_OWNER_UPDATE;
        } else {
            sql = ApplicationMgtDBQueries.UPDATE_EXTENDED_BASIC_APPINFO;
        }
        // update the application data
        try (NamedPreparedStatement updateStatement = new NamedPreparedStatement(connection, sql)) {

            updateStatement.setString(ApplicationTableColumns.APP_NAME, applicationName);
            updateStatement.setString(ApplicationTableColumns.DESCRIPTION,
                    application.getDescription());
            updateStatement.setString(ApplicationTableColumns.IS_SAAS_APP,
                    application.isSaasApp() ? "1" : "0");
            updateStatement.setString(ApplicationTableColumns.LOGIN_URL,
                    application.getLoginUrl());
            updateStatement.setString(ApplicationTableColumns.IMAGE_URL,
                    application.getImageUrl());

            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            if (isValidUserForOwnerUpdate) {
                User appOwner = application.getOwner();
                updateStatement.setString(ApplicationTableColumns.USERNAME,
                        appOwner.getUserName());
                updateStatement.setString(ApplicationTableColumns.USER_STORE,
                        appOwner.getUserStoreDomain());
                updateStatement.setInt(ApplicationTableColumns.TENANT_ID, tenantId);
            }

            updateStatement.setInt(ApplicationTableColumns.TENANT_ID, tenantId);
            updateStatement.setInt(ApplicationTableColumns.ID, applicationId);
            updateStatement.executeUpdate();

        }
        if (log.isDebugEnabled()) {
            log.debug("Updated Application successfully");
        }

    }

    @Override
    public ExtendedApplicationBasicInfo[] getExtendedApplicationBasicInfo(int offset, int limit)
            throws IdentityApplicationManagementException {

        validateAttributesForPagination(offset, limit);

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement getAppNamesStmt = null;
        ResultSet appNameResultSet = null;
        String sqlQuery;
        ArrayList<ExtendedApplicationBasicInfo> appInfo = new ArrayList<>();

        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            if (databaseProductName.contains("MySQL") || databaseProductName.contains("H2")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_MYSQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, tenantID);
                getAppNamesStmt.setInt(2, offset);
                getAppNamesStmt.setInt(3, limit);
            } else if (databaseProductName.contains("Oracle")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_ORACLE;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, tenantID);
                getAppNamesStmt.setInt(2, offset + limit);
                getAppNamesStmt.setInt(3, offset);
            } else if (databaseProductName.contains("Microsoft")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_MSSQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, tenantID);
                getAppNamesStmt.setInt(2, offset);
                getAppNamesStmt.setInt(3, limit);
            } else if (databaseProductName.contains("PostgreSQL")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_POSTGRESQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, tenantID);
                getAppNamesStmt.setInt(2, limit);
                getAppNamesStmt.setInt(3, offset);
            } else if (databaseProductName.contains("DB2")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_DB2SQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, tenantID);
                getAppNamesStmt.setInt(2, offset + 1);
                getAppNamesStmt.setInt(3, offset + limit);
            } else if (databaseProductName.contains("INFORMIX")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_INFORMIX;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, offset);
                getAppNamesStmt.setInt(2, limit);
                getAppNamesStmt.setInt(3, tenantID);
            } else {
                log.error("Error while loading applications from DB: Database driver could not be identified or " +
                        "not supported.");
                throw new IdentityApplicationManagementException("Error while loading applications from DB: " +
                        "Database driver could not be identified or not supported.");
            }

            appNameResultSet = getAppNamesStmt.executeQuery();

            while (appNameResultSet.next()) {
                if (ApplicationConstants.LOCAL_SP.equals(appNameResultSet.getString(1))) {
                    continue;
                }
                appInfo.add(buildExtendedApplicationBasicInfo(appNameResultSet));
            }

        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while loading applications from DB: " +
                    e.getMessage(), e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getAppNamesStmt);
            IdentityApplicationManagementUtil.closeResultSet(appNameResultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return appInfo.toArray(new ExtendedApplicationBasicInfo[0]);
    }

    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(int offset,
                                                          int limit) throws IdentityApplicationManagementException {

        return getExtendedApplicationBasicInfo(offset, limit);
    }

    @Override
    public ExtendedApplicationBasicInfo[] getExtendedApplicationBasicInfo(String filter, int offset, int limit)
            throws IdentityApplicationManagementException {

        validateAttributesForPagination(offset, limit);

        if ("*".equals(filter)) {
            return getExtendedApplicationBasicInfo(offset, limit);
        }

        validateAttributesForPagination(offset, limit);

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement getAppNamesStmt = null;
        ResultSet appNameResultSet = null;
        String sqlQuery;
        ArrayList<ExtendedApplicationBasicInfo> appInfo = new ArrayList<>();
        try {

            String filterResolvedForSQL = resolveSQLFilter(filter);

            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            if (databaseProductName.contains("MySQL") || databaseProductName.contains("H2")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_MYSQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, tenantID);
                getAppNamesStmt.setString(2, filterResolvedForSQL);
                getAppNamesStmt.setInt(3, offset);
                getAppNamesStmt.setInt(4, limit);
            } else if (databaseProductName.contains("Oracle")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_ORACLE;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, tenantID);
                getAppNamesStmt.setString(2, filterResolvedForSQL);
                getAppNamesStmt.setInt(3, offset + limit);
                getAppNamesStmt.setInt(4, offset);
            } else if (databaseProductName.contains("Microsoft")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_MSSQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, tenantID);
                getAppNamesStmt.setString(2, filterResolvedForSQL);
                getAppNamesStmt.setInt(3, offset);
                getAppNamesStmt.setInt(4, limit);
            } else if (databaseProductName.contains("PostgreSQL")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_POSTGRESQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, tenantID);
                getAppNamesStmt.setString(2, filterResolvedForSQL);
                getAppNamesStmt.setInt(3, limit);
                getAppNamesStmt.setInt(4, offset);
            } else if (databaseProductName.contains("DB2")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_DB2SQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, tenantID);
                getAppNamesStmt.setString(2, filterResolvedForSQL);
                getAppNamesStmt.setInt(3, offset + 1);
                getAppNamesStmt.setInt(4, offset + limit);
            } else if (databaseProductName.contains("INFORMIX")) {
                sqlQuery = ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_INFORMIX;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, tenantID);
                getAppNamesStmt.setString(2, filterResolvedForSQL);
                getAppNamesStmt.setInt(3, offset);
                getAppNamesStmt.setInt(4, limit);
            } else {
                log.error("Error while loading applications from DB: Database driver could not be identified or " +
                        "not supported.");
                throw new IdentityApplicationManagementException("Error while loading applications from DB:" +
                        "Database driver could not be identified or not supported.");
            }

            appNameResultSet = getAppNamesStmt.executeQuery();

            while (appNameResultSet.next()) {
                if (ApplicationConstants.LOCAL_SP.equals(appNameResultSet.getString(1))) {
                    continue;
                }
                appInfo.add(buildExtendedApplicationBasicInfo(appNameResultSet));
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while loading applications from DB: " +
                    e.getMessage(), e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getAppNamesStmt);
            IdentityApplicationManagementUtil.closeResultSet(appNameResultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return appInfo.toArray(new ExtendedApplicationBasicInfo[0]);
    }

    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(String filter, int offset, int limit) throws
            IdentityApplicationManagementException {

        return getExtendedApplicationBasicInfo(filter, offset, limit);
    }

    private boolean isAppRenamed(String applicationName, String storedAppName) {

        return !StringUtils.equals(applicationName, storedAppName);
    }

    private ExtendedApplicationBasicInfo buildExtendedApplicationBasicInfo(ResultSet appNameResultSet) throws SQLException {

        ExtendedApplicationBasicInfo basicInfo = new ExtendedApplicationBasicInfo();
        basicInfo.setApplicationId(appNameResultSet.getInt("ID"));
        basicInfo.setApplicationName(appNameResultSet.getString("APP_NAME"));
        basicInfo.setDescription(appNameResultSet.getString("DESCRIPTION"));

        basicInfo.setApplicationResourceId(appNameResultSet.getString("UUID"));
        basicInfo.setImageUrl(appNameResultSet.getString("IMAGE_URL"));
        basicInfo.setLoginUrl(appNameResultSet.getString("LOGIN_URL"));

        // TODO: add owner information in the app basic URL
        return basicInfo;
    }
}
