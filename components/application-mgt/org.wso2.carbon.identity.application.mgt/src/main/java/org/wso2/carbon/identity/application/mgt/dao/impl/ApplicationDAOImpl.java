/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.InboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.AbstractInboundAuthenticatorConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponent;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.DBUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class access the IDN_APPMGT database to store/update and delete application configurations.
 * The IDN_APPMGT database contains few tables
 * <ul>
 * <li>IDN_APPMGT_APP</li>
 * <li>IDN_APPMGT_CLIENT</li>
 * <li>IDN_APPMGT_STEP</li>
 * <li>IDN_APPMGT_STEP_IDP</li>
 * <li>IDN_APPMGT_CLAIM_MAPPING</li>
 * <li>IDN_APPMGT_ROLE_MAPPING</li>
 * </ul>
 */
public class ApplicationDAOImpl implements ApplicationDAO {

    private Log log = LogFactory.getLog(ApplicationDAOImpl.class);

    private List<String> standardInboundAuthTypes;

    public ApplicationDAOImpl() {
        standardInboundAuthTypes = new ArrayList<String>();
        standardInboundAuthTypes.add("oauth2");
        standardInboundAuthTypes.add("wstrust");
        standardInboundAuthTypes.add("samlsso");
        standardInboundAuthTypes.add("openid");
        standardInboundAuthTypes.add("passivests");
    }

    private boolean isCustomInboundAuthType(String authType) {
        return !standardInboundAuthTypes.contains(authType);
    }

    /**
     * Get Service provider properties
     * @param dbConnection database connection
     * @param SpId SP Id
     * @return service provider properties
     */
    private List<ServiceProviderProperty> getServicePropertiesBySpId(Connection dbConnection, int SpId)
            throws SQLException {

        String sqlStmt = ApplicationMgtDBQueries.GET_SP_METADATA_BY_SP_ID;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<ServiceProviderProperty> idpProperties = new ArrayList<ServiceProviderProperty>();
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, SpId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                ServiceProviderProperty property = new ServiceProviderProperty();
                property.setName(rs.getString("NAME"));
                property.setValue(rs.getString("VALUE"));
                property.setDisplayName(rs.getString("DISPLAY_NAME"));
                idpProperties.add(property);
            }
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
            IdentityApplicationManagementUtil.closeResultSet(rs);
        }
        return idpProperties;
    }

    /**
     * Add Service provider properties
     *
     * @param dbConnection
     * @param spId
     * @param properties
     * @throws SQLException
     */
    private void addServiceProviderProperties(Connection dbConnection, int spId,
            List<ServiceProviderProperty> properties, int tenantId)
            throws SQLException {
        String sqlStmt = ApplicationMgtDBQueries.ADD_SP_METADATA;
        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            for (ServiceProviderProperty property : properties) {
                prepStmt.setInt(1, spId);
                prepStmt.setString(2, property.getName());
                prepStmt.setString(3, property.getValue());
                prepStmt.setString(4, property.getDisplayName());
                prepStmt.setInt(5, tenantId);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
    }

    /**
     * Update Service provider properties
     *
     * @param dbConnection
     * @param spId
     * @param properties
     * @throws SQLException
     */
    private void updateServiceProviderProperties(Connection dbConnection, int spId,
            List<ServiceProviderProperty> properties, int tenantId)
            throws SQLException {

        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.DELETE_SP_METADATA);
            prepStmt.setInt(1, spId);
            prepStmt.executeUpdate();

            prepStmt = dbConnection.prepareStatement(ApplicationMgtDBQueries.ADD_SP_METADATA);

            for (ServiceProviderProperty property : properties) {
                prepStmt.setInt(1, spId);
                prepStmt.setString(2, property.getName());
                prepStmt.setString(3, property.getValue());
                prepStmt.setString(4, property.getDisplayName());
                prepStmt.setInt(5, tenantId);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
    }


    /**
     * Stores basic application information and meta-data such as the application name, creator and
     * tenant.
     *
     * @param serviceProvider
     * @throws IdentityApplicationManagementException
     */
    @Override
    public int createApplication(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        // get logged-in users tenant identifier.
        int tenantID = MultitenantConstants.INVALID_TENANT_ID;

        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        }

        String qualifiedUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (ApplicationConstants.LOCAL_SP.equals(serviceProvider.getApplicationName())) {
            qualifiedUsername = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        String username = UserCoreUtil.removeDomainFromName(qualifiedUsername);
        String userStoreDomain = IdentityUtil.extractDomainFromName(qualifiedUsername);
        String applicationName = serviceProvider.getApplicationName();
        String description = serviceProvider.getDescription();

        if (log.isDebugEnabled()) {
            log.debug("Creating Application " + applicationName + " for user " + qualifiedUsername);
        }

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement storeAppPrepStmt = null;
        ResultSet results = null;

        try {
            String dbProductName = connection.getMetaData().getDatabaseProductName();
            storeAppPrepStmt = connection.prepareStatement(
                    ApplicationMgtDBQueries.STORE_BASIC_APPINFO, new String[]{
                            DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "ID")});

            // TENANT_ID, APP_NAME, USER_STORE, USERNAME, DESCRIPTION, AUTH_TYPE
            storeAppPrepStmt.setInt(1, tenantID);
            storeAppPrepStmt.setString(2, applicationName);
            storeAppPrepStmt.setString(3, userStoreDomain);
            storeAppPrepStmt.setString(4, username);
            storeAppPrepStmt.setString(5, description);
            // by default authentication type would be default.
            // default authenticator is defined system-wide - in the configuration file.
            storeAppPrepStmt.setString(6, ApplicationConstants.AUTH_TYPE_DEFAULT);
            storeAppPrepStmt.setString(7, "0");
            storeAppPrepStmt.setString(8, "0");
            storeAppPrepStmt.setString(9, "0");
            storeAppPrepStmt.execute();

            results = storeAppPrepStmt.getGeneratedKeys();

            int applicationId = 0;
            if (results.next()) {
                applicationId = results.getInt(1);
            }
            // some JDBC Drivers returns this in the result, some don't
            if (applicationId == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("JDBC Driver did not return the application id, executing Select operation");
                }
                applicationId = getApplicationIDByName(applicationName, tenantID, connection);
            }

            if (serviceProvider.getSpProperties() != null) {
                addServiceProviderProperties(connection, applicationId,
                        Arrays.asList(serviceProvider.getSpProperties()), tenantID);
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            if (log.isDebugEnabled()) {
                log.debug("Application Stored successfully with application id " + applicationId);
            }

            return applicationId;

        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException sql) {
                throw new IdentityApplicationManagementException(
                        "Error while Creating Application", sql);
            }
            throw new IdentityApplicationManagementException("Error while Creating Application", e);
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(results);
            IdentityApplicationManagementUtil.closeStatement(storeAppPrepStmt);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
    }

    @Override
    public void updateApplication(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        int applicationId = serviceProvider.getApplicationID();

        int tenantID = MultitenantConstants.INVALID_TENANT_ID;
        if (tenantDomain != null) {
            tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        }

        try {
            if (ApplicationManagementServiceComponent.getFileBasedSPs().containsKey(
                    serviceProvider.getApplicationName())) {
                throw new IdentityApplicationManagementException(
                        "Application with the same name laoded from the file system.");
            }

            // update basic information of the application.
            // you can change application name, description, isSasApp...
            updateBasicApplicationData(serviceProvider, connection);
            updateInboundProvisioningConfiguration(applicationId, serviceProvider.getInboundProvisioningConfig(),
                    connection);

            // delete all in-bound authentication requests.
            deleteInboundAuthRequestConfiguration(serviceProvider.getApplicationID(), connection);

            // update all in-bound authentication requests.
            updateInboundAuthRequestConfiguration(serviceProvider.getApplicationID(), serviceProvider
                    .getInboundAuthenticationConfig(), connection);

            // delete local and out-bound authentication configuration.
            deleteLocalAndOutboundAuthenticationConfiguration(applicationId, connection);

            // update local and out-bound authentication configuration.
            updateLocalAndOutboundAuthenticationConfiguration(serviceProvider.getApplicationID(),
                    serviceProvider.getLocalAndOutBoundAuthenticationConfig(), connection);

            deleteRequestPathAuthenticators(applicationId, connection);
            updateRequestPathAuthenticators(applicationId, serviceProvider.getRequestPathAuthenticatorConfigs(),
                    connection);

            deteClaimConfiguration(applicationId, connection);
            updateClaimConfiguration(serviceProvider.getApplicationID(), serviceProvider.getClaimConfig(),
                    applicationId, connection);

            deleteOutboundProvisioningConfiguration(applicationId, connection);
            updateOutboundProvisioningConfiguration(applicationId,
                    serviceProvider.getOutboundProvisioningConfig(), connection);

            deletePermissionAndRoleConfiguration(applicationId, connection);
            updatePermissionAndRoleConfiguration(serviceProvider.getApplicationID(),
                    serviceProvider.getPermissionAndRoleConfig(), connection);
            deleteAssignedPermissions(connection, serviceProvider.getApplicationName(),
                    serviceProvider.getPermissionAndRoleConfig().getPermissions());

            if (serviceProvider.getSpProperties() != null) {
                updateServiceProviderProperties(connection, applicationId, Arrays.asList(serviceProvider
                        .getSpProperties()), tenantID);
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException | UserStoreException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                throw new IdentityApplicationManagementException(
                        "Failed to update service provider " + applicationId, e);
            }
            throw new IdentityApplicationManagementException("Failed to update service provider "
                    + applicationId, e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
    }

    /**
     * @param serviceProvider
     * @param connection
     * @throws SQLException
     * @throws UserStoreException
     * @throws IdentityApplicationManagementException
     */

    private void updateBasicApplicationData(ServiceProvider serviceProvider, Connection connection) throws SQLException, UserStoreException,
            IdentityApplicationManagementException {
        int applicationId = serviceProvider.getApplicationID();
        String applicationName = serviceProvider.getApplicationName();
        String description = serviceProvider.getDescription();
        boolean isSaasApp = serviceProvider.isSaasApp();
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String storedAppName = null;

        if (applicationName == null) {
            // check for required attributes.
            throw new IdentityApplicationManagementException("Application Name is required.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Updating Application with ID: " + applicationId);
        }
        // reads back the Application Name. This is to check if the Application
        // has been renamed
        storedAppName = getApplicationName(applicationId, connection);

        if (log.isDebugEnabled()) {
            log.debug("Stored Application Name " + storedAppName);
        }

        // only if the application has been renamed
        if (!StringUtils.equals(applicationName, storedAppName)) {
            String applicationNameforRole = IdentityUtil.addDomainToName(applicationName, ApplicationConstants.
                    APPLICATION_DOMAIN);
            String storedAppNameforRole = IdentityUtil.addDomainToName(storedAppName, ApplicationConstants.
                    APPLICATION_DOMAIN);
            // rename the role
            ApplicationMgtUtil.renameRole(storedAppNameforRole, applicationNameforRole);
            if (log.isDebugEnabled()) {
                log.debug("Renaming application role from " + storedAppName + " to "
                        + applicationName);
            }
            Map<String, String> applicationPermissions = readApplicationPermissions(storedAppName);
            for (Map.Entry<String, String> entry : applicationPermissions.entrySet()) {
                updatePermissionPath(entry.getKey(), entry.getValue().replace(storedAppName.toLowerCase(),
                        applicationName.toLowerCase()));
            }
        }

        // update the application data
        PreparedStatement storeAppPrepStmt = null;
        try {
            storeAppPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO);
            // SET APP_NAME=?, DESCRIPTION=? IS_SAAS_APP=? WHERE TENANT_ID= ? AND ID = ?
            storeAppPrepStmt.setString(1, applicationName);
            storeAppPrepStmt.setString(2, description);
            storeAppPrepStmt.setString(3, isSaasApp ? "1" : "0");
            storeAppPrepStmt.setInt(4, tenantID);
            storeAppPrepStmt.setInt(5, applicationId);
            storeAppPrepStmt.executeUpdate();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeAppPrepStmt);
        }

        if (log.isDebugEnabled()) {
            log.debug("Updated Application successfully");
        }

    }

    private List<Property> filterEmptyProperties(Property[] propertiesArray) {
        List<Property> propertyArrayList = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(propertiesArray)) {
            for (Property property : propertiesArray) {
                if (property != null && StringUtils.isNotBlank(property.getValue())) {
                    propertyArrayList.add(property);
                }
            }
        }
        return propertyArrayList;
    }
    /**
     * @param applicationId
     * @param inBoundAuthenticationConfig
     * @param connection
     * @throws SQLException
     */
    private void updateInboundAuthRequestConfiguration(int applicationId, InboundAuthenticationConfig
            inBoundAuthenticationConfig, Connection connection) throws IdentityApplicationManagementException {
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement inboundAuthReqConfigPrepStmt = null;

        try {
            if (inBoundAuthenticationConfig == null
                    || inBoundAuthenticationConfig.getInboundAuthenticationRequestConfigs() == null
                    || inBoundAuthenticationConfig.getInboundAuthenticationRequestConfigs().length == 0) {
                // no in-bound authentication requests defined.
                return;
            }

            inboundAuthReqConfigPrepStmt = connection.prepareStatement(ApplicationMgtDBQueries.STORE_CLIENT_INFO);
            InboundAuthenticationRequestConfig[] authRequests = inBoundAuthenticationConfig
                    .getInboundAuthenticationRequestConfigs();

            for (InboundAuthenticationRequestConfig authRequest : authRequests) {
                if (authRequest == null || authRequest.getInboundAuthType() == null) {
                    log.warn("Invalid in-bound authentication request");
                    // not a valid authentication request. Must have client and a type.
                    continue;
                }

                Property[] propertiesArray = authRequest.getProperties();
                List<Property> propertyArrayList = new ArrayList<>();

                String authKey = null;
                String inboundConfigType = ApplicationConstants.STANDARD_APPLICATION;
                if (standardInboundAuthTypes.contains(authRequest.getInboundAuthType())) {
                    authKey = authRequest.getInboundAuthKey();
                    propertyArrayList = filterEmptyProperties(propertiesArray);
                } else {
                    AbstractInboundAuthenticatorConfig inboundAuthenticatorConfig =
                            ApplicationManagementServiceComponentHolder.getInboundAuthenticatorConfig(authRequest
                                    .getInboundAuthType() + ":" + authRequest.getInboundConfigType());
                    if (inboundAuthenticatorConfig != null &&
                            StringUtils.isNotBlank(inboundAuthenticatorConfig.getRelyingPartyKey())) {
                        if (propertiesArray != null && propertiesArray.length > 0) {
                            for (Property prop : propertiesArray) {
                                if (inboundAuthenticatorConfig.getRelyingPartyKey().equals(prop.getName())) {
                                    if (StringUtils.isNotBlank(prop.getValue())) {
                                        authKey = prop.getValue();
                                    }
                                } else {
                                    if (StringUtils.isNotBlank(prop.getValue())) {
                                        propertyArrayList.add(prop);
                                    }
                                }
                            }
                        }
                    } else {
                        propertyArrayList = filterEmptyProperties(propertiesArray);
                    }
                }
                if (StringUtils.isBlank(authKey)) {
                    String applicationName = getApplicationName(applicationId, connection);
                    if (StringUtils.isNotBlank(applicationName)) {
                        authKey = applicationName;
                    }
                }
                if (StringUtils.isNotBlank(authRequest.getInboundConfigType())) {
                    inboundConfigType = authRequest.getInboundConfigType();
                }
                if (!propertyArrayList.isEmpty()) {
                    for (Property prop : propertyArrayList) {
                        inboundAuthReqConfigPrepStmt.setInt(1, tenantID);
                        inboundAuthReqConfigPrepStmt.setString(2,authKey);
                        inboundAuthReqConfigPrepStmt.setString(3,authRequest.getInboundAuthType());
                        inboundAuthReqConfigPrepStmt.setString(4,prop.getName());
                        inboundAuthReqConfigPrepStmt.setString(5,prop.getValue());
                        inboundAuthReqConfigPrepStmt.setInt(6, applicationId);
                        inboundAuthReqConfigPrepStmt.setString(7, inboundConfigType);
                        inboundAuthReqConfigPrepStmt.addBatch();
                    }
                } else {
                    inboundAuthReqConfigPrepStmt.setInt(1, tenantID);
                    inboundAuthReqConfigPrepStmt.setString(2,authKey);
                    inboundAuthReqConfigPrepStmt.setString(3,authRequest.getInboundAuthType());
                    inboundAuthReqConfigPrepStmt.setString(4, null);
                    inboundAuthReqConfigPrepStmt.setString(5, null);
                    inboundAuthReqConfigPrepStmt.setInt(6, applicationId);
                    inboundAuthReqConfigPrepStmt.setString(7, inboundConfigType);
                    inboundAuthReqConfigPrepStmt.addBatch();
                }

                if (log.isDebugEnabled()) {
                    log.debug("Updating inbound authentication request configuration of the application "
                            + applicationId
                            + "inbound auth key: "
                            + authRequest.getInboundAuthKey()
                            + " inbound auth type: "
                            + authRequest.getInboundAuthType());
                }
            }

            inboundAuthReqConfigPrepStmt.executeBatch();
        } catch (SQLException e) {
            log.error("Error occurred while updating the Inbound Authentication Request Configuration.", e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(inboundAuthReqConfigPrepStmt);
        }
    }

    /**
     * @param applicationId
     * @param inBoundProvisioningConfig
     * @param connection
     * @throws SQLException
     */
    private void updateInboundProvisioningConfiguration(int applicationId,
                                                        InboundProvisioningConfig inBoundProvisioningConfig, Connection connection)
            throws SQLException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        PreparedStatement inboundProConfigPrepStmt = null;

        try {
            inboundProConfigPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_PRO_PROPERTIES);

            // PROVISIONING_USERSTORE_DOMAIN=?
            inboundProConfigPrepStmt.setString(1, inBoundProvisioningConfig.getProvisioningUserStore());
            inboundProConfigPrepStmt.setString(2, inBoundProvisioningConfig.isDumbMode() ? "1" : "0");
            inboundProConfigPrepStmt.setInt(3, tenantID);
            inboundProConfigPrepStmt.setInt(4, applicationId);
            inboundProConfigPrepStmt.execute();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(inboundProConfigPrepStmt);
        }
    }

    /**
     * @param applicationId
     * @param outBoundProvisioningConfig
     * @param connection
     * @throws SQLException
     */
    private void updateOutboundProvisioningConfiguration(int applicationId,
                                                         OutboundProvisioningConfig outBoundProvisioningConfig, Connection connection)
            throws SQLException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        PreparedStatement outboundProConfigPrepStmt = null;

        IdentityProvider[] proProviders = outBoundProvisioningConfig
                .getProvisioningIdentityProviders();

        try {
            if (outBoundProvisioningConfig == null || proProviders == null
                    || proProviders.length == 0) {
                // no in-bound authentication requests defined.
                return;
            }

            outboundProConfigPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.STORE_PRO_CONNECTORS);
            // TENANT_ID, IDP_NAME, CONNECTOR_NAME, APP_ID

            for (IdentityProvider proProvider : proProviders) {
                if (proProvider != null) {
                    ProvisioningConnectorConfig proConnector = proProvider
                            .getDefaultProvisioningConnectorConfig();
                    if (proConnector == null) {
                        continue;
                    }

                    String jitEnabled = "0";

                    if (proProvider.getJustInTimeProvisioningConfig() != null
                            && proProvider.getJustInTimeProvisioningConfig()
                            .isProvisioningEnabled()) {
                        jitEnabled = "1";
                    }

                    String blocking = "0";

                    if (proProvider.getDefaultProvisioningConnectorConfig() != null
                            && proProvider.getDefaultProvisioningConnectorConfig().isBlocking()) {
                        blocking = "1";
                    }

                    String ruleEnabled = "0";

                    if (proProvider.getDefaultProvisioningConnectorConfig() != null
                        && proProvider.getDefaultProvisioningConnectorConfig().isRulesEnabled()) {
                        ruleEnabled = "1";
                    }

                    outboundProConfigPrepStmt.setInt(1, tenantID);
                    outboundProConfigPrepStmt.setString(2, proProvider.getIdentityProviderName());
                    outboundProConfigPrepStmt.setString(3, proConnector.getName());
                    outboundProConfigPrepStmt.setInt(4, applicationId);
                    outboundProConfigPrepStmt.setString(5, jitEnabled);
                    outboundProConfigPrepStmt.setString(6, blocking);
                    outboundProConfigPrepStmt.setString(7, ruleEnabled);
                    outboundProConfigPrepStmt.addBatch();

                }
            }

            outboundProConfigPrepStmt.executeBatch();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(outboundProConfigPrepStmt);
        }
    }

    /**
     * @param applicationId
     * @param connection
     * @return
     * @throws SQLException
     */
    private InboundProvisioningConfig getInboundProvisioningConfiguration(int applicationId,
                                                                          Connection connection, int tenantID) throws SQLException {

        PreparedStatement inboundProConfigPrepStmt = null;
        InboundProvisioningConfig inBoundProvisioningConfig = new InboundProvisioningConfig();
        ResultSet resultSet = null;

        try {

            inboundProConfigPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_PRO_PROPERTIES_BY_APP_ID);
            // PROVISIONING_USERSTORE_DOMAIN
            inboundProConfigPrepStmt.setInt(1, tenantID);
            inboundProConfigPrepStmt.setInt(2, applicationId);
            resultSet = inboundProConfigPrepStmt.executeQuery();

            while (resultSet.next()) {
                inBoundProvisioningConfig.setProvisioningUserStore(resultSet.getString(1));
                inBoundProvisioningConfig.setDumbMode("1".equals(resultSet.getString(2)));
            }

        } finally {
            IdentityApplicationManagementUtil.closeStatement(inboundProConfigPrepStmt);
        }
        return inBoundProvisioningConfig;
    }

    /**
     * @param applicationId
     * @param connection
     * @return
     * @throws SQLException
     */
    private OutboundProvisioningConfig getOutboundProvisioningConfiguration(int applicationId,
                                                                            Connection connection, int tenantID) throws SQLException {

        PreparedStatement outboundProConfigPrepStmt = null;
        OutboundProvisioningConfig outBoundProvisioningConfig = new OutboundProvisioningConfig();
        ResultSet resultSet = null;
        List<IdentityProvider> idpProConnectors = new ArrayList<IdentityProvider>();

        try {

            outboundProConfigPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_PRO_CONNECTORS_BY_APP_ID);
            // IDP_NAME, CONNECTOR_NAM
            outboundProConfigPrepStmt.setInt(1, applicationId);
            outboundProConfigPrepStmt.setInt(2, tenantID);
            resultSet = outboundProConfigPrepStmt.executeQuery();

            while (resultSet.next()) {
                ProvisioningConnectorConfig proConnector = null;
                IdentityProvider fedIdp = null;

                fedIdp = new IdentityProvider();
                fedIdp.setIdentityProviderName(resultSet.getString(1));

                proConnector = new ProvisioningConnectorConfig();
                proConnector.setName(resultSet.getString(2));

                if ("1".equals(resultSet.getString(3))) {
                    JustInTimeProvisioningConfig jitConfig = new JustInTimeProvisioningConfig();
                    jitConfig.setProvisioningEnabled(true);
                    fedIdp.setJustInTimeProvisioningConfig(jitConfig);
                }

                if ("1".equals(resultSet.getString(4))) {
                    proConnector.setBlocking(true);
                } else {
                    proConnector.setBlocking(false);
                }

                if ("1".equals(resultSet.getString(5))) {
                    proConnector.setRulesEnabled(true);
                } else {
                    proConnector.setRulesEnabled(false);
                }

                fedIdp.setDefaultProvisioningConnectorConfig(proConnector);
                idpProConnectors.add(fedIdp);

            }

            outBoundProvisioningConfig.setProvisioningIdentityProviders(idpProConnectors.toArray(new
                    IdentityProvider[idpProConnectors.size()]));

        } finally {
            IdentityApplicationManagementUtil.closeStatement(outboundProConfigPrepStmt);
        }
        return outBoundProvisioningConfig;
    }

    /**
     * @param applicationId
     * @param localAndOutboundAuthConfig
     * @param connection
     * @throws SQLException
     * @throws IdentityApplicationManagementException
     */
    private void updateLocalAndOutboundAuthenticationConfiguration(int applicationId,
                                                                   LocalAndOutboundAuthenticationConfig localAndOutboundAuthConfig, Connection connection)
            throws SQLException, IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement updateAuthTypePrepStmt = null;
        if (localAndOutboundAuthConfig == null) {
            // no local or out-bound configuration for this service provider.
            return;
        }

        PreparedStatement storeSendAuthListOfIdPsPrepStmt = null;
        try {
            storeSendAuthListOfIdPsPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_SEND_AUTH_LIST_OF_IDPS);
            // IS_SEND_LOCAL_SUBJECT_ID=? WHERE TENANT_ID= ? AND ID = ?
            storeSendAuthListOfIdPsPrepStmt.setString(1, localAndOutboundAuthConfig
                    .isAlwaysSendBackAuthenticatedListOfIdPs() ? "1" : "0");
            storeSendAuthListOfIdPsPrepStmt.setInt(2, tenantID);
            storeSendAuthListOfIdPsPrepStmt.setInt(3, applicationId);
            storeSendAuthListOfIdPsPrepStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeSendAuthListOfIdPsPrepStmt);
        }

        PreparedStatement storeUseTenantDomainInLocalSubjectIdStmt = null;
        try {
            storeUseTenantDomainInLocalSubjectIdStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries
                            .UPDATE_BASIC_APPINFO_WITH_USE_TENANT_DOMAIN_LOCAL_SUBJECT_ID);
            // IS_USE_TENANT_DIMAIN_LOCAL_SUBJECT_ID=? WHERE TENANT_ID= ? AND ID = ?
            storeUseTenantDomainInLocalSubjectIdStmt.setString(1, localAndOutboundAuthConfig
                    .isUseTenantDomainInLocalSubjectIdentifier() ? "1" : "0");
            storeUseTenantDomainInLocalSubjectIdStmt.setInt(2, tenantID);
            storeUseTenantDomainInLocalSubjectIdStmt.setInt(3, applicationId);
            storeUseTenantDomainInLocalSubjectIdStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeUseTenantDomainInLocalSubjectIdStmt);
        }

        PreparedStatement storeUseUserstoreDomainInLocalSubjectIdStmt = null;
        try {
            storeUseUserstoreDomainInLocalSubjectIdStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries
                            .UPDATE_BASIC_APPINFO_WITH_USE_USERSTORE_DOMAIN_LOCAL_SUBJECT_ID);
            // IS_USE_USERSTORE_DIMAIN_LOCAL_SUBJECT_ID=? WHERE TENANT_ID= ? AND ID = ?
            storeUseUserstoreDomainInLocalSubjectIdStmt.setString(1, localAndOutboundAuthConfig
                    .isUseUserstoreDomainInLocalSubjectIdentifier() ? "1" : "0");
            storeUseUserstoreDomainInLocalSubjectIdStmt.setInt(2, tenantID);
            storeUseUserstoreDomainInLocalSubjectIdStmt.setInt(3, applicationId);
            storeUseUserstoreDomainInLocalSubjectIdStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeUseUserstoreDomainInLocalSubjectIdStmt);
        }

        PreparedStatement enableAuthzStmt = null;
        try {
            enableAuthzStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_ENABLE_AUTHORIZATION);
            enableAuthzStmt.setString(1, localAndOutboundAuthConfig.isEnableAuthorization() ? "1" : "0");
            enableAuthzStmt.setInt(2, tenantID);
            enableAuthzStmt.setInt(3, applicationId);
            enableAuthzStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(enableAuthzStmt);
        }

        PreparedStatement storeSubjectClaimUri = null;
        try {
            storeSubjectClaimUri = connection
                    .prepareStatement(ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_SUBJECT_CLAIM_URI);
            // SUBJECT_CLAIM_URI=? WHERE TENANT_ID= ? AND ID = ?
            storeSubjectClaimUri.setString(1, localAndOutboundAuthConfig.getSubjectClaimUri());
            storeSubjectClaimUri.setInt(2, tenantID);
            storeSubjectClaimUri.setInt(3, applicationId);
            storeSubjectClaimUri.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeSubjectClaimUri);
        }

        AuthenticationStep[] authSteps = localAndOutboundAuthConfig.getAuthenticationSteps();

        if (authSteps == null || authSteps.length == 0) {
            // if no authentication steps defined - it should be the default behavior.
            localAndOutboundAuthConfig
                    .setAuthenticationType(ApplicationConstants.AUTH_TYPE_DEFAULT);
        }

        try {
            if (localAndOutboundAuthConfig.getAuthenticationType() == null) {
                // no authentication type defined - set to default.
                localAndOutboundAuthConfig
                        .setAuthenticationType(ApplicationConstants.AUTH_TYPE_DEFAULT);
            }

            updateAuthTypePrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_AUTH_TYPE);
            // AUTH_TYPE=? WHERE TENANT_ID= ? AND ID = ?
            updateAuthTypePrepStmt.setString(1, localAndOutboundAuthConfig.getAuthenticationType());
            updateAuthTypePrepStmt.setInt(2, tenantID);
            updateAuthTypePrepStmt.setInt(3, applicationId);
            updateAuthTypePrepStmt.execute();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(updateAuthTypePrepStmt);
        }

        if (authSteps != null && authSteps.length > 0) {
            // we have authentications steps defined.
            PreparedStatement storeStepIDPAuthnPrepStmt = null;
            storeStepIDPAuthnPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.STORE_STEP_IDP_AUTH);
            try {

                if (ApplicationConstants.AUTH_TYPE_LOCAL
                        .equalsIgnoreCase(localAndOutboundAuthConfig.getAuthenticationType())) {
                    // for local authentication there can only be only one authentication step and
                    // only one local authenticator.
                    if (authSteps.length != 1
                            || authSteps[0] == null
                            || authSteps[0].getLocalAuthenticatorConfigs() == null
                            || authSteps[0].getLocalAuthenticatorConfigs().length != 1
                            || (authSteps[0].getFederatedIdentityProviders() != null && authSteps[0]
                            .getFederatedIdentityProviders().length >= 1)) {
                        String errorMessage = "Invalid local authentication configuration."
                                + " For local authentication there can only be only one authentication step and only one local authenticator";
                        throw new IdentityApplicationManagementException(errorMessage);
                    }
                } else if (ApplicationConstants.AUTH_TYPE_FEDERATED
                        .equalsIgnoreCase(localAndOutboundAuthConfig.getAuthenticationType())) {
                    // for federated authentication there can only be only one authentication step
                    // and only one federated authenticator - which is the default authenticator of
                    // the corresponding authenticator.
                    if (authSteps.length != 1 || authSteps[0] == null
                            || authSteps[0].getFederatedIdentityProviders() == null
                            || authSteps[0].getFederatedIdentityProviders().length != 1
                            || authSteps[0].getLocalAuthenticatorConfigs().length > 0) {
                        String errorMessage = "Invalid federated authentication configuration."
                                + " For federated authentication there can only be only one authentication step and only one federated authenticator";
                        throw new IdentityApplicationManagementException(errorMessage);
                    }

                    IdentityProvider fedIdp = authSteps[0].getFederatedIdentityProviders()[0];

                    if (fedIdp.getDefaultAuthenticatorConfig() == null || fedIdp.getFederatedAuthenticatorConfigs() == null) {
                        IdentityProviderDAO idpDAO = ApplicationMgtSystemConfig.getInstance().getIdentityProviderDAO();

                        String defualtAuthName = idpDAO.getDefaultAuthenticator(fedIdp
                                .getIdentityProviderName());

                        // set the default authenticator.
                        FederatedAuthenticatorConfig defaultAuth = new FederatedAuthenticatorConfig();
                        defaultAuth.setName(defualtAuthName);
                        fedIdp.setDefaultAuthenticatorConfig(defaultAuth);
                        fedIdp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{defaultAuth});
                    }
                }

                // iterating through each step.
                for (AuthenticationStep authStep : authSteps) {
                    int stepId = 0;

                    IdentityProvider[] federatedIdps = authStep.getFederatedIdentityProviders();

                    // an authentication step should have at least one federated identity
                    // provider or a local authenticator.
                    if ((federatedIdps == null || federatedIdps.length == 0)
                            && (authStep.getLocalAuthenticatorConfigs() == null || authStep
                            .getLocalAuthenticatorConfigs().length == 0)) {
                        String errorMesssage = "Invalid authentication configuration."
                                + "An authentication step should have at least one federated identity "
                                + "provider or a local authenticator";
                        throw new IdentityApplicationManagementException(errorMesssage);
                    }

                    // we have valid federated identity providers.
                    PreparedStatement storeStepPrepStmtz = null;
                    ResultSet result = null;

                    try {
                        String dbProductName = connection.getMetaData().getDatabaseProductName();
                        storeStepPrepStmtz = connection.prepareStatement(
                                ApplicationMgtDBQueries.STORE_STEP_INFO, new String[]{
                                        DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "ID")});
                        // TENANT_ID, STEP_ORDER, APP_ID
                        storeStepPrepStmtz.setInt(1, tenantID);
                        storeStepPrepStmtz.setInt(2, authStep.getStepOrder());
                        storeStepPrepStmtz.setInt(3, applicationId);
                        storeStepPrepStmtz.setString(4, authStep.isSubjectStep() ? "1" : "0");
                        storeStepPrepStmtz.setString(5, authStep.isAttributeStep() ? "1" : "0");
                        storeStepPrepStmtz.execute();

                        result = storeStepPrepStmtz.getGeneratedKeys();

                        if (result.next()) {
                            stepId = result.getInt(1);
                        }
                    } finally {
                        IdentityApplicationManagementUtil.closeResultSet(result);
                        IdentityApplicationManagementUtil.closeStatement(storeStepPrepStmtz);
                    }

                    if (authStep.getLocalAuthenticatorConfigs() != null
                            && authStep.getLocalAuthenticatorConfigs().length > 0) {

                        for (LocalAuthenticatorConfig lclAuthenticator : authStep
                                .getLocalAuthenticatorConfigs()) {
                            // set the identity provider name to LOCAL.
                            int authenticatorId = getAuthentictorID(connection, tenantID,
                                    ApplicationConstants.LOCAL_IDP_NAME, lclAuthenticator.getName());
                            if (authenticatorId < 0) {
                                authenticatorId = addAuthenticator(connection, tenantID,
                                        ApplicationConstants.LOCAL_IDP_NAME,
                                        lclAuthenticator.getName(),
                                        lclAuthenticator.getDisplayName());
                            }
                            if (authenticatorId > 0) {
                                // ID, TENANT_ID, AUTHENTICATOR_ID
                                storeStepIDPAuthnPrepStmt.setInt(1, stepId);
                                storeStepIDPAuthnPrepStmt.setInt(2, tenantID);
                                storeStepIDPAuthnPrepStmt.setInt(3, authenticatorId);
                                storeStepIDPAuthnPrepStmt.addBatch();
                            }

                            if (log.isDebugEnabled()) {
                                log.debug("Updating Local IdP of Application " + applicationId
                                        + " Step Order: " + authStep.getStepOrder() + " IdP: "
                                        + ApplicationConstants.LOCAL_IDP + " Authenticator: "
                                        + lclAuthenticator.getName());
                            }
                        }
                    }

                    // we have federated identity providers.
                    if (federatedIdps != null && federatedIdps.length > 0) {

                        // iterating through each IDP of the step
                        for (IdentityProvider federatedIdp : federatedIdps) {
                            String idpName = federatedIdp.getIdentityProviderName();

                            // the identity provider name wso2carbon-local-idp is reserved.
                            if (ApplicationConstants.LOCAL_IDP.equalsIgnoreCase(idpName)) {
                                throw new IdentityApplicationManagementException(
                                        "The federated IdP name cannot be equal to "
                                                + ApplicationConstants.LOCAL_IDP);
                            }

                            FederatedAuthenticatorConfig[] authenticators = federatedIdp
                                    .getFederatedAuthenticatorConfigs();

                            if (authenticators != null && authenticators.length > 0) {

                                for (FederatedAuthenticatorConfig authenticator : authenticators) {
                                    // ID, TENANT_ID, AUTHENTICATOR_ID
                                    int authenticatorId = getAuthentictorID(connection, tenantID,
                                            idpName, authenticator.getName());
                                    if (authenticatorId > 0) {
                                        if (authenticator != null) {
                                            storeStepIDPAuthnPrepStmt.setInt(1, stepId);
                                            storeStepIDPAuthnPrepStmt.setInt(2, tenantID);
                                            storeStepIDPAuthnPrepStmt.setInt(3, authenticatorId);
                                            storeStepIDPAuthnPrepStmt.addBatch();

                                            if (log.isDebugEnabled()) {
                                                log.debug("Updating Federated IdP of Application "
                                                        + applicationId + " Step Order: "
                                                        + authStep.getStepOrder() + " IdP: "
                                                        + idpName + " Authenticator: "
                                                        + authenticator);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

                storeStepIDPAuthnPrepStmt.executeBatch();
            } finally {
                IdentityApplicationManagementUtil.closeStatement(storeStepIDPAuthnPrepStmt);
            }
        }
    }

    /**
     * @param applicationId
     * @param claimConfiguration
     * @param applicationID
     * @param connection
     * @throws SQLException
     */
    private void updateClaimConfiguration(int applicationId, ClaimConfig claimConfiguration,
                                          int applicationID, Connection connection) throws SQLException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement storeRoleClaimPrepStmt = null;
        PreparedStatement storeClaimDialectPrepStmt = null;
        PreparedStatement storeSendLocalSubIdPrepStmt = null;

        if (claimConfiguration == null) {
            return;
        }

        try {
            // update the application data
            String roleClaim = claimConfiguration.getRoleClaimURI();
            if (roleClaim != null) {
                storeRoleClaimPrepStmt = connection
                        .prepareStatement(ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_ROLE_CLAIM);
                // ROLE_CLAIM=? WHERE TENANT_ID= ? AND ID =
                storeRoleClaimPrepStmt.setString(1, roleClaim);
                storeRoleClaimPrepStmt.setInt(2, tenantID);
                storeRoleClaimPrepStmt.setInt(3, applicationId);
                storeRoleClaimPrepStmt.executeUpdate();
            }

        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeRoleClaimPrepStmt);
        }

        try {
            storeClaimDialectPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_CLAIM_DIALEECT);
            // IS_LOCAL_CLAIM_DIALECT=? WHERE TENANT_ID= ? AND ID = ?
            storeClaimDialectPrepStmt.setString(1, claimConfiguration.isLocalClaimDialect() ? "1"
                    : "0");
            storeClaimDialectPrepStmt.setInt(2, tenantID);
            storeClaimDialectPrepStmt.setInt(3, applicationId);
            storeClaimDialectPrepStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeClaimDialectPrepStmt);
        }

        try {
            storeSendLocalSubIdPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_SEND_LOCAL_SUB_ID);
            // IS_SEND_LOCAL_SUBJECT_ID=? WHERE TENANT_ID= ? AND ID = ?
            storeSendLocalSubIdPrepStmt.setString(1,
                    claimConfiguration.isAlwaysSendMappedLocalSubjectId() ? "1" : "0");
            storeSendLocalSubIdPrepStmt.setInt(2, tenantID);
            storeSendLocalSubIdPrepStmt.setInt(3, applicationId);
            storeSendLocalSubIdPrepStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeSendLocalSubIdPrepStmt);
        }

        if (claimConfiguration.getClaimMappings() == null
                || claimConfiguration.getClaimMappings().length == 0) {
            return;
        }

        List<ClaimMapping> claimMappings = Arrays.asList(claimConfiguration.getClaimMappings());

        if (claimConfiguration == null || claimMappings.isEmpty()) {
            log.debug("No claim mapping found, Skipping ..");
            return;
        }

        PreparedStatement storeClaimMapPrepStmt = null;
        try {
            storeClaimMapPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.STORE_CLAIM_MAPPING);

            for (ClaimMapping mapping : claimMappings) {
                if (mapping.getLocalClaim() == null
                        || mapping.getLocalClaim().getClaimUri() == null
                        || mapping.getRemoteClaim().getClaimUri() == null
                        || mapping.getRemoteClaim() == null) {
                    continue;
                }
                // TENANT_ID, IDP_CLAIM, SP_CLAIM, APP_ID, IS_REQUESTED
                storeClaimMapPrepStmt.setInt(1, tenantID);
                storeClaimMapPrepStmt.setString(2, mapping.getLocalClaim().getClaimUri());
                storeClaimMapPrepStmt.setString(3, mapping.getRemoteClaim().getClaimUri());
                storeClaimMapPrepStmt.setInt(4, applicationID);
                if (mapping.isRequested()) {
                    storeClaimMapPrepStmt.setString(5, "1");
                } else {
                    storeClaimMapPrepStmt.setString(5, "0");
                }
                if (mapping.isMandatory()) {
                    storeClaimMapPrepStmt.setString(6, "1");
                } else {
                    storeClaimMapPrepStmt.setString(6, "0");
                }
                storeClaimMapPrepStmt.setString(7, mapping.getDefaultValue());
                storeClaimMapPrepStmt.addBatch();

                if (log.isDebugEnabled()) {
                    log.debug("Storing Claim Mapping. Local Claim: "
                            + mapping.getLocalClaim().getClaimUri() + " SPClaim: "
                            + mapping.getRemoteClaim().getClaimUri());
                }
            }

            storeClaimMapPrepStmt.executeBatch();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeClaimMapPrepStmt);
        }
    }

    /**
     * @param applicationID
     * @param permissionsAndRoleConfiguration
     * @param connection
     * @throws SQLException
     */
    private void updatePermissionAndRoleConfiguration(int applicationID,
                                                      PermissionsAndRoleConfig permissionsAndRoleConfiguration, Connection connection)
            throws SQLException {

        if (permissionsAndRoleConfiguration == null
                || permissionsAndRoleConfiguration.getRoleMappings() == null
                || permissionsAndRoleConfiguration.getRoleMappings().length == 0) {
            return;
        }

        RoleMapping[] roleMappings = permissionsAndRoleConfiguration.getRoleMappings();
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement storeRoleMapPrepStmt = null;
        try {
            storeRoleMapPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.STORE_ROLE_MAPPING);
            for (RoleMapping roleMapping : roleMappings) {
                // TENANT_ID, IDP_ROLE, SP_ROLE, APP_ID
                storeRoleMapPrepStmt.setInt(1, tenantID);
                storeRoleMapPrepStmt.setString(2, roleMapping.getLocalRole().getLocalRoleName());
                storeRoleMapPrepStmt.setString(3, roleMapping.getRemoteRole());
                storeRoleMapPrepStmt.setInt(4, applicationID);
                storeRoleMapPrepStmt.addBatch();

                if (log.isDebugEnabled()) {
                    log.debug("Storing Claim Mapping. IDPRole: " + roleMapping.getLocalRole()
                            + " SPRole: " + roleMapping.getRemoteRole());
                }
            }

            storeRoleMapPrepStmt.executeBatch();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeRoleMapPrepStmt);
        }
    }

    @Override
    public ServiceProvider getApplication(String applicationName, String tenantDomain)
            throws IdentityApplicationManagementException {

        int applicationId = 0;
        int tenantID = MultitenantConstants.SUPER_TENANT_ID;
        if (tenantDomain != null) {
            try {
                tenantID = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                        .getTenantManager().getTenantId(tenantDomain);
            } catch (UserStoreException e1) {
                log.error("Error in reading application", e1);
                throw new IdentityApplicationManagementException("Error while reading application", e1);
            }
        }

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        try {

            // Load basic application data
            ServiceProvider serviceProvider = getBasicApplicationData(applicationName, connection,
                    tenantID);

            if ((serviceProvider == null || serviceProvider.getApplicationName() == null)
                    && ApplicationConstants.LOCAL_SP.equals(applicationName)) {
                ServiceProvider localServiceProvider = new ServiceProvider();
                localServiceProvider.setApplicationName(applicationName);
                localServiceProvider.setDescription("Local Service Provider");
                createApplication(localServiceProvider, tenantDomain);
                serviceProvider = getBasicApplicationData(applicationName, connection, tenantID);
            }

            if (serviceProvider == null) {
                return null;
            }

            applicationId = serviceProvider.getApplicationID();

            serviceProvider.setInboundAuthenticationConfig(getInboundAuthenticationConfig(
                    applicationId, connection, tenantID));
            serviceProvider
                    .setLocalAndOutBoundAuthenticationConfig(getLocalAndOutboundAuthenticationConfig(
                            applicationId, connection, tenantID));

            serviceProvider.setInboundProvisioningConfig(getInboundProvisioningConfiguration(
                    applicationId, connection, tenantID));

            serviceProvider.setOutboundProvisioningConfig(getOutboundProvisioningConfiguration(
                    applicationId, connection, tenantID));

            // Load Claim Mapping
            serviceProvider.setClaimConfig(getClaimConfiguration(applicationId, connection,
                    tenantID));

            // Load Role Mappings
            List<RoleMapping> roleMappings = getRoleMappingOfApplication(applicationId, connection,
                    tenantID);
            PermissionsAndRoleConfig permissionAndRoleConfig = new PermissionsAndRoleConfig();
            permissionAndRoleConfig.setRoleMappings(roleMappings
                    .toArray(new RoleMapping[roleMappings.size()]));
            serviceProvider.setPermissionAndRoleConfig(permissionAndRoleConfig);

            RequestPathAuthenticatorConfig[] requestPathAuthenticators = getRequestPathAuthenticators(
                    applicationId, connection, tenantID);
            serviceProvider.setRequestPathAuthenticatorConfigs(requestPathAuthenticators);

            List<ServiceProviderProperty> propertyList = getServicePropertiesBySpId(connection, applicationId);
            serviceProvider.setSpProperties(propertyList.toArray(new ServiceProviderProperty[propertyList.size()]));

            return serviceProvider;

        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Failed to update service provider "
                    + applicationId, e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
    }

    /**
     * @param applicationName
     * @param connection
     * @return
     * @throws SQLException
     */
    private ServiceProvider getBasicApplicationData(String applicationName, Connection connection,
                                                    int tenantID)
            throws SQLException, IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;

        if (log.isDebugEnabled()) {
            log.debug("Loading Basic Application Data of " + applicationName);
        }

        PreparedStatement loadBasicAppInfoStmt = null;
        ResultSet basicAppDataResultSet = null;
        try {
            loadBasicAppInfoStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_BASIC_APP_INFO_BY_APP_NAME);
            // SELECT * FROM IDN_APPMGT_APP WHERE APP_NAME = ? AND TENANT_ID = ?
            loadBasicAppInfoStmt.setString(1, applicationName);
            loadBasicAppInfoStmt.setInt(2, tenantID);
            basicAppDataResultSet = loadBasicAppInfoStmt.executeQuery();
            // ID, TENANT_ID, APP_NAME, USER_STORE, USERNAME, DESCRIPTION, ROLE_CLAIM, AUTH_TYPE,
            // PROVISIONING_USERSTORE_DOMAIN, IS_LOCAL_CLAIM_DIALECT, IS_SEND_LOCAL_SUBJECT_ID,
            // IS_SEND_AUTH_LIST_OF_IDPS, SUBJECT_CLAIM_URI, IS_SAAS_APP

            if (basicAppDataResultSet.next()) {
                serviceProvider = new ServiceProvider();
                serviceProvider.setApplicationID(basicAppDataResultSet.getInt(1));
                serviceProvider.setApplicationName(basicAppDataResultSet.getString(3));
                serviceProvider.setDescription(basicAppDataResultSet.getString(6));

                String tenantDomain;
                try {
                    tenantDomain = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                            .getTenantManager()
                            .getDomain(
                                    basicAppDataResultSet.getInt(2));
                } catch (UserStoreException e) {
                    log.error("Error while reading tenantDomain", e);
                    throw new IdentityApplicationManagementException("Error while reading tenant " +
                            "domain for application " +
                            applicationName);
                }

                User owner = new User();
                owner.setUserName(basicAppDataResultSet.getString(5));
                owner.setTenantDomain(tenantDomain);
                owner.setUserStoreDomain(basicAppDataResultSet.getString(4));
                serviceProvider.setOwner(owner);

                ClaimConfig claimConfig = new ClaimConfig();
                claimConfig.setRoleClaimURI(basicAppDataResultSet.getString(7));
                claimConfig.setLocalClaimDialect("1".equals(basicAppDataResultSet.getString(10)));
                claimConfig.setAlwaysSendMappedLocalSubjectId("1".equals(basicAppDataResultSet
                        .getString(11)));
                serviceProvider.setClaimConfig(claimConfig);

                LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new LocalAndOutboundAuthenticationConfig();
                localAndOutboundAuthenticationConfig.setAlwaysSendBackAuthenticatedListOfIdPs("1"
                        .equals(basicAppDataResultSet.getString(14)));
                localAndOutboundAuthenticationConfig.setEnableAuthorization("1".equals(basicAppDataResultSet
                        .getString(15)));
                localAndOutboundAuthenticationConfig.setSubjectClaimUri(basicAppDataResultSet
                        .getString(16));
                serviceProvider
                        .setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);

                serviceProvider.setSaasApp("1".equals(basicAppDataResultSet.getString(17)));

                if (log.isDebugEnabled()) {
                    log.debug("ApplicationID: " + serviceProvider.getApplicationID()
                            + " ApplicationName: " + serviceProvider.getApplicationName()
                            + " UserName: " + serviceProvider.getOwner().getUserName()
                            + " TenantDomain: " + serviceProvider.getOwner().getTenantDomain());
                }
            }

            return serviceProvider;
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(basicAppDataResultSet);
            IdentityApplicationManagementUtil.closeStatement(loadBasicAppInfoStmt);
        }
    }

    @Override
    public ServiceProvider getApplication(int applicationId) throws IdentityApplicationManagementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        try {

            // Load basic application data
            ServiceProvider serviceProvider = getBasicApplicationData(applicationId, connection);
            int tenantID = IdentityTenantUtil.getTenantId(serviceProvider.getOwner().getTenantDomain());
            if (serviceProvider == null) {
                return null;
            }

            serviceProvider.setInboundAuthenticationConfig(getInboundAuthenticationConfig(
                    applicationId, connection, tenantID));
            serviceProvider
                    .setLocalAndOutBoundAuthenticationConfig(getLocalAndOutboundAuthenticationConfig(
                            applicationId, connection, tenantID));

            serviceProvider.setInboundProvisioningConfig(getInboundProvisioningConfiguration(
                    applicationId, connection, tenantID));

            serviceProvider.setOutboundProvisioningConfig(getOutboundProvisioningConfiguration(
                    applicationId, connection, tenantID));

            // Load Claim Mapping
            serviceProvider.setClaimConfig(getClaimConfiguration(applicationId, connection,
                                                                 tenantID));

            // Load Role Mappings
            List<RoleMapping> roleMappings = getRoleMappingOfApplication(applicationId, connection,
                                                                         tenantID);
            PermissionsAndRoleConfig permissionAndRoleConfig = new PermissionsAndRoleConfig();
            permissionAndRoleConfig.setRoleMappings(roleMappings
                                                            .toArray(new RoleMapping[roleMappings.size()]));
            serviceProvider.setPermissionAndRoleConfig(permissionAndRoleConfig);

            RequestPathAuthenticatorConfig[] requestPathAuthenticators = getRequestPathAuthenticators(
                    applicationId, connection, tenantID);
            serviceProvider.setRequestPathAuthenticatorConfigs(requestPathAuthenticators);

            List<ServiceProviderProperty> propertyList = getServicePropertiesBySpId(connection, applicationId);
            serviceProvider.setSpProperties(propertyList.toArray(new ServiceProviderProperty[propertyList.size()]));

            return serviceProvider;

        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Failed to update service provider "
                                                             + applicationId, e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
    }

    /**
     * @param appId
     * @param connection
     * @return
     * @throws SQLException
     */
    private ServiceProvider getBasicApplicationData(int appId, Connection connection)
            throws SQLException, IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;

        if (log.isDebugEnabled()) {
            log.debug("Loading Basic Application Data of application ID: " + appId);
        }

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            prepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_BASIC_APP_INFO_BY_APP_ID);
            prepStmt.setInt(1, appId);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                serviceProvider = new ServiceProvider();
                serviceProvider.setApplicationID(rs.getInt(1));
                serviceProvider.setApplicationName(rs.getString(3));
                serviceProvider.setDescription(rs.getString(6));

                String tenantDomain;
                try {
                    tenantDomain = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                            .getTenantManager()
                            .getDomain(
                                    rs.getInt(2));
                } catch (UserStoreException e) {
                    throw new IdentityApplicationManagementException("Error while reading tenant domain for " +
                                                                     "application ID: " + appId);
                }

                User owner = new User();
                owner.setUserName(rs.getString(5));
                owner.setTenantDomain(tenantDomain);
                owner.setUserStoreDomain(rs.getString(4));
                serviceProvider.setOwner(owner);

                ClaimConfig claimConfig = new ClaimConfig();
                claimConfig.setRoleClaimURI(rs.getString(7));
                claimConfig.setLocalClaimDialect("1".equals(rs.getString(10)));
                claimConfig.setAlwaysSendMappedLocalSubjectId("1".equals(rs
                                                                                 .getString(11)));
                serviceProvider.setClaimConfig(claimConfig);

                LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new LocalAndOutboundAuthenticationConfig();
                localAndOutboundAuthenticationConfig.setAlwaysSendBackAuthenticatedListOfIdPs("1"
                                                                                                      .equals(rs.getString(14)));
                localAndOutboundAuthenticationConfig.setEnableAuthorization("1".equals(rs
                                                                                               .getString(15)));
                localAndOutboundAuthenticationConfig.setSubjectClaimUri(rs
                                                                                .getString(16));
                serviceProvider
                        .setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);

                serviceProvider.setSaasApp("1".equals(rs.getString(17)));

                if (log.isDebugEnabled()) {
                    log.debug("ApplicationID: " + serviceProvider.getApplicationID()
                              + " ApplicationName: " + serviceProvider.getApplicationName()
                              + " UserName: " + serviceProvider.getOwner().getUserName()
                              + " TenantDomain: " + serviceProvider.getOwner().getTenantDomain());
                }
            }

            return serviceProvider;
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(rs);
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
    }

    /**
     * @param applicationid
     * @param connection
     * @return
     * @throws SQLException
     */
    private String getAuthenticationType(int applicationid, Connection connection)
            throws SQLException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement authTypeStmt = null;
        ResultSet authTypeResultSet = null;
        try {
            authTypeStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_AUTH_TYPE_BY_APP_ID);
            authTypeStmt.setInt(1, applicationid);
            authTypeStmt.setInt(2, tenantID);
            authTypeResultSet = authTypeStmt.executeQuery();

            if (authTypeResultSet.next()) {
                return authTypeResultSet.getString(1);
            }

            return ApplicationConstants.AUTH_TYPE_DEFAULT;

        } finally {
            IdentityApplicationManagementUtil.closeResultSet(authTypeResultSet);
            IdentityApplicationManagementUtil.closeStatement(authTypeStmt);
        }

    }

    /**
     * This method will be heavily used by the Authentication Framework. The framework would ask for
     * application data with the given client key and secrete
     *
     * @param clientId
     * @param type
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    public ServiceProvider getApplicationData(String clientId, String type, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Loading Application Data of Client " + clientId);
        }

        int tenantID = -123;

        try {
            tenantID = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                    .getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e1) {
            log.error("Error while reading application", e1);
            throw new IdentityApplicationManagementException("Error while reading application", e1);
        }

        String applicationName = null;

        // Reading application name from the database
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement storeAppPrepStmt = null;
        ResultSet appNameResult = null;
        try {
            storeAppPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_APPLICATION_NAME_BY_CLIENT_ID_AND_TYPE);
            storeAppPrepStmt.setString(1, clientId);
            storeAppPrepStmt.setString(2, type);
            storeAppPrepStmt.setInt(3, tenantID);
            appNameResult = storeAppPrepStmt.executeQuery();
            connection.commit();
            if (appNameResult.next()) {
                applicationName = appNameResult.getString(1);
            }

        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while reading application", e);
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(appNameResult);
            IdentityApplicationManagementUtil.closeStatement(storeAppPrepStmt);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return getApplication(applicationName, tenantDomain);
    }

    /**
     * @param applicationID
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public String getApplicationName(int applicationID)
            throws IdentityApplicationManagementException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        try {
            return getApplicationName(applicationID, connection);
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Failed loading the application with "
                    + applicationID, e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
    }

    /**
     * Reads back the basic application data
     *
     * @param applicationID
     * @param connection
     * @return
     * @throws IdentityApplicationManagementException
     */
    private String getApplicationName(int applicationID, Connection connection) throws SQLException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("Loading Application Name for ID: " + applicationID);
        }

        PreparedStatement loadBasicAppInfoStmt = null;
        ResultSet appNameResultSet = null;
        String applicationName = null;

        try {
            loadBasicAppInfoStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_APP_NAME_BY_APP_ID);
            loadBasicAppInfoStmt.setInt(1, applicationID);
            loadBasicAppInfoStmt.setInt(2, tenantID);
            appNameResultSet = loadBasicAppInfoStmt.executeQuery();

            if (appNameResultSet.next()) {
                applicationName = appNameResultSet.getString(1);
            }

            if (log.isDebugEnabled()) {
                log.debug("ApplicationName : " + applicationName);
            }
            return applicationName;

        } finally {
            IdentityApplicationManagementUtil.closeResultSet(appNameResultSet);
            IdentityApplicationManagementUtil.closeStatement(loadBasicAppInfoStmt);
        }
    }

    /**
     * Returns the application ID for a given application name
     *
     * @param applicationName
     * @param tenantID
     * @param connection
     * @return
     * @throws IdentityApplicationManagementException
     */
    private int getApplicationIDByName(String applicationName, int tenantID, Connection connection)
            throws IdentityApplicationManagementException {

        int applicationId = 0;
        PreparedStatement getAppIDPrepStmt = null;
        ResultSet appidResult = null;

        try {
            getAppIDPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_APP_ID_BY_APP_NAME);
            getAppIDPrepStmt.setString(1, applicationName);
            getAppIDPrepStmt.setInt(2, tenantID);
            appidResult = getAppIDPrepStmt.executeQuery();

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            if (appidResult.next()) {
                applicationId = appidResult.getInt(1);
            }

        } catch (SQLException e) {
            IdentityApplicationManagementUtil.closeConnection(connection);
            log.error("Error in storing the application", e);
            throw new IdentityApplicationManagementException("Error while storing application", e);
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(appidResult);
            IdentityApplicationManagementUtil.closeStatement(getAppIDPrepStmt);
        }

        return applicationId;
    }


    /**
     * Reading the mapping of properties.
     *
     * @param customAuthenticator
     * @param propertyName
     * @return
     */
    private Property getMappedProperty(AbstractInboundAuthenticatorConfig customAuthenticator, String propertyName) {
        Property property = null;
        if (customAuthenticator != null) {
            Property[] confProps = customAuthenticator.getConfigurationProperties();
            if (confProps != null) {
                for (Property confProp : confProps) {
                    if (propertyName != null && propertyName.equals(confProp.getName())) {
                        property = confProp;
                    }
                }
            }
        }
        return property;
    }

    /**
     * Merge properties from config to request.
     *
     * @param sources
     * @param destinations
     */
    private void mergedPropertiesMetaData(Property[] sources, Property[] destinations) {
        Map<String, Property> destinationMap = new HashMap<>();
        if (ArrayUtils.isNotEmpty(destinations)) {
            for (Property destination : destinations) {
                destinationMap.put(destination.getName(), destination);
            }
        }
        if (ArrayUtils.isNotEmpty(sources)) {
            for (Property source : sources) {
                Property property = destinationMap.get(source.getName());
                if (property == null) {
                    destinationMap.put(source.getName(), source);
                }
            }
        }
    }

    /**
     * @param applicationId
     * @param connection
     * @return
     * @throws SQLException
     */
    private InboundAuthenticationConfig getInboundAuthenticationConfig(int applicationId, Connection connection, int
            tenantID) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("Reading Clients of Application " + applicationId);
        }
        Map<String, InboundAuthenticationRequestConfig> inboundAuthenticationRequestConfigMap =
                new HashMap<String, InboundAuthenticationRequestConfig>();

        PreparedStatement getClientInfo = null;
        ResultSet resultSet = null;

        try {
            getClientInfo = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_CLIENTS_INFO_BY_APP_ID);

            getClientInfo.setInt(1, applicationId);
            getClientInfo.setInt(2, tenantID);
            resultSet = getClientInfo.executeQuery();

            while (resultSet.next()) {
                String authKey = resultSet.getString(1);
                //this is done to handle empty string added to oracle database as null.
                if (authKey == null) {
                    authKey = new String();
                }
                String authType = resultSet.getString(2);
                String propName = resultSet.getString(3);
                String propValue = resultSet.getString(4);
                String configType = resultSet.getString(5);

                String mapKey = authType + ":" + authKey;

                InboundAuthenticationRequestConfig inboundAuthRequest = null;
                if ((inboundAuthRequest = inboundAuthenticationRequestConfigMap.get(mapKey)) == null) {
                    inboundAuthRequest = new InboundAuthenticationRequestConfig();
                }
                inboundAuthRequest.setInboundAuthKey(authKey);
                inboundAuthRequest.setInboundAuthType(authType);
                inboundAuthRequest.setInboundConfigType(configType);

                boolean isCustomAuthenticator = isCustomInboundAuthType(authType);
                AbstractInboundAuthenticatorConfig customAuthenticator = ApplicationManagementServiceComponentHolder
                        .getInboundAuthenticatorConfig(authType + ":" + configType);
                if (isCustomAuthenticator && customAuthenticator != null) {
                    inboundAuthRequest.setFriendlyName(customAuthenticator.getFriendlyName());
                }
                if (propName != null) {
                    Property prop = new Property();
                    prop.setName(propName);
                    prop.setValue(propValue);
                    if (isCustomAuthenticator && customAuthenticator != null) {
                        Property mappedProperty = getMappedProperty(customAuthenticator, propName);
                        if (mappedProperty != null) {
                            prop.setDisplayName(mappedProperty.getDisplayName());
                        }
                    }
                    inboundAuthRequest.setProperties((ApplicationMgtUtil.concatArrays(new Property[]{prop},
                            inboundAuthRequest.getProperties())));
                }
                inboundAuthenticationRequestConfigMap.put(mapKey, inboundAuthRequest);
            }
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getClientInfo);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
        }
        Map<String, AbstractInboundAuthenticatorConfig> allCustomAuthenticators = new HashMap<>
                (ApplicationManagementServiceComponentHolder.getAllInboundAuthenticatorConfig());
        for (Map.Entry<String, InboundAuthenticationRequestConfig> entry : inboundAuthenticationRequestConfigMap
                .entrySet()) {
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = entry.getValue();
            AbstractInboundAuthenticatorConfig inboundAuthenticatorConfig = allCustomAuthenticators.remove
                    (inboundAuthenticationRequestConfig.getInboundAuthType() + ":" +
                            inboundAuthenticationRequestConfig.getInboundConfigType());
            if (inboundAuthenticatorConfig != null && inboundAuthenticationRequestConfig != null) {
                Property[] sources = inboundAuthenticatorConfig.getConfigurationProperties();
                Property[] destinations = inboundAuthenticationRequestConfig.getProperties();
                Map<String, Property> destinationMap = new HashMap<>();
                for (Property destination : destinations) {
                    destinationMap.put(destination.getName(), destination);
                }
                for (Property source : sources) {
                    Property property = destinationMap.get(source.getName());
                    if (property == null) {
                        if (isCustomInboundAuthType(inboundAuthenticationRequestConfig.getInboundAuthType())) {
                            if (inboundAuthenticatorConfig.isRelyingPartyKeyConfigured()) {
                                if (StringUtils.equals(inboundAuthenticatorConfig.getRelyingPartyKey(), source
                                        .getName())) {
                                    source.setValue(inboundAuthenticationRequestConfig.getInboundAuthKey());
                                }
                            }
                        }
                        destinationMap.put(source.getName(), source);
                    }
                }
                inboundAuthenticationRequestConfig
                        .setProperties(destinationMap.values().toArray(new Property[destinationMap.size()]));
            }
        }
        List<InboundAuthenticationRequestConfig> returnList = new ArrayList<>(inboundAuthenticationRequestConfigMap
                .values());

        for (Map.Entry<String, AbstractInboundAuthenticatorConfig> entry : allCustomAuthenticators.entrySet()) {
            AbstractInboundAuthenticatorConfig inboundAuthenticatorConfig = entry.getValue();
                InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig =
                        new InboundAuthenticationRequestConfig();
                inboundAuthenticationRequestConfig.setInboundAuthType(inboundAuthenticatorConfig.getName());
                inboundAuthenticationRequestConfig.setInboundConfigType(inboundAuthenticatorConfig.getConfigName());
                inboundAuthenticationRequestConfig.setFriendlyName(inboundAuthenticatorConfig.getFriendlyName());
                inboundAuthenticationRequestConfig.setProperties(inboundAuthenticatorConfig
                        .getConfigurationProperties());

                returnList.add(inboundAuthenticationRequestConfig);
        }
        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(returnList.toArray(new
                InboundAuthenticationRequestConfig[returnList.size()]));
        return inboundAuthenticationConfig;
    }

    /**
     * @param applicationId
     * @param connection
     * @return
     * @throws SQLException
     */
    private LocalAndOutboundAuthenticationConfig getLocalAndOutboundAuthenticationConfig(
            int applicationId, Connection connection, int tenantId) throws SQLException {
        PreparedStatement getStepInfoPrepStmt = null;
        ResultSet stepInfoResultSet = null;

        if (log.isDebugEnabled()) {
            log.debug("Reading Steps of Application " + applicationId);
        }

        try {
            getStepInfoPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_STEPS_INFO_BY_APP_ID);
            // STEP_ORDER, AUTHENTICATOR_ID, IS_SUBJECT_STEP, IS_ATTRIBUTE_STEP
            getStepInfoPrepStmt.setInt(1, applicationId);
            stepInfoResultSet = getStepInfoPrepStmt.executeQuery();

            Map<String, AuthenticationStep> authSteps = new HashMap<String, AuthenticationStep>();
            Map<String, Map<String, List<FederatedAuthenticatorConfig>>> stepFedIdPAuthenticators = new HashMap<String, Map<String, List<FederatedAuthenticatorConfig>>>();
            Map<String, List<LocalAuthenticatorConfig>> stepLocalAuth = new HashMap<String, List<LocalAuthenticatorConfig>>();

            while (stepInfoResultSet.next()) {

                String step = String.valueOf(stepInfoResultSet.getInt(1));
                AuthenticationStep authStep;

                if (authSteps.containsKey(step)) {
                    authStep = authSteps.get(step);
                } else {
                    authStep = new AuthenticationStep();
                    authStep.setStepOrder(stepInfoResultSet.getInt(1));
                    stepLocalAuth.put(step, new ArrayList<LocalAuthenticatorConfig>());
                    stepFedIdPAuthenticators.put(step,
                            new HashMap<String, List<FederatedAuthenticatorConfig>>());
                }

                int authenticatorId = stepInfoResultSet.getInt(2);
                Map<String, String> authenticatorInfo = getAuthenticatorInfo(connection, tenantId,
                        authenticatorId);

                if (authenticatorInfo != null
                        && authenticatorInfo.get(ApplicationConstants.IDP_NAME) != null
                        && ApplicationConstants.LOCAL_IDP_NAME.equals(authenticatorInfo
                        .get("idpName"))) {
                    LocalAuthenticatorConfig localAuthenticator = new LocalAuthenticatorConfig();
                    localAuthenticator.setName(authenticatorInfo
                            .get(ApplicationConstants.IDP_AUTHENTICATOR_NAME));
                    localAuthenticator.setDisplayName(authenticatorInfo
                            .get(ApplicationConstants.IDP_AUTHENTICATOR_DISPLAY_NAME));
                    stepLocalAuth.get(step).add(localAuthenticator);
                } else {
                    Map<String, List<FederatedAuthenticatorConfig>> stepFedIdps = stepFedIdPAuthenticators
                            .get(step);

                    if (!stepFedIdps.containsKey(authenticatorInfo
                            .get(ApplicationConstants.IDP_NAME))) {
                        stepFedIdps.put(authenticatorInfo.get(ApplicationConstants.IDP_NAME),
                                new ArrayList<FederatedAuthenticatorConfig>());
                    }

                    List<FederatedAuthenticatorConfig> idpAuths = stepFedIdps.get(authenticatorInfo
                            .get(ApplicationConstants.IDP_NAME));
                    FederatedAuthenticatorConfig fedAuthenticator = new FederatedAuthenticatorConfig();
                    fedAuthenticator.setName(authenticatorInfo
                            .get(ApplicationConstants.IDP_AUTHENTICATOR_NAME));
                    fedAuthenticator.setDisplayName(authenticatorInfo
                            .get(ApplicationConstants.IDP_AUTHENTICATOR_DISPLAY_NAME));
                    idpAuths.add(fedAuthenticator);
                }

                authStep.setSubjectStep("1".equals(stepInfoResultSet.getString(3)));
                authStep.setAttributeStep("1".equals(stepInfoResultSet.getString(4)));

                authSteps.put(step, authStep);
            }

            LocalAndOutboundAuthenticationConfig localAndOutboundConfiguration
                    = new LocalAndOutboundAuthenticationConfig();

            AuthenticationStep[] authenticationSteps = new AuthenticationStep[authSteps.size()];

            int authStepCount = 0;

            for (Entry<String, AuthenticationStep> entry : authSteps.entrySet()) {

                AuthenticationStep authStep = entry.getValue();
                String stepId = entry.getKey();

                List<LocalAuthenticatorConfig> localAuthenticatorList = stepLocalAuth.get(stepId);

                if (localAuthenticatorList != null && localAuthenticatorList.size() > 0) {
                    authStep.setLocalAuthenticatorConfigs(
                            localAuthenticatorList.toArray(new LocalAuthenticatorConfig[localAuthenticatorList.size()]));
                }

                Map<String, List<FederatedAuthenticatorConfig>> idpList = stepFedIdPAuthenticators
                        .get(stepId);

                if (idpList != null && idpList.size() > 0) {
                    IdentityProvider[] fedIdpList = new IdentityProvider[idpList.size()];
                    int idpCount = 0;

                    for (Entry<String, List<FederatedAuthenticatorConfig>> idpEntry : idpList
                            .entrySet()) {
                        String idpName = idpEntry.getKey();
                        List<FederatedAuthenticatorConfig> fedAuthenticators = idpEntry.getValue();
                        IdentityProvider idp = new IdentityProvider();
                        idp.setIdentityProviderName(idpName);
                        idp.setFederationHub(isFederationHubIdP(idpName, connection, tenantId));
                        idp.setFederatedAuthenticatorConfigs(
                                fedAuthenticators.toArray(new FederatedAuthenticatorConfig[fedAuthenticators.size()]));
                        idp.setDefaultAuthenticatorConfig(idp.getFederatedAuthenticatorConfigs()[0]);
                        fedIdpList[idpCount++] = idp;
                    }
                    authStep.setFederatedIdentityProviders(fedIdpList);
                }

                authenticationSteps[authStepCount++] = authStep;
            }

            Comparator<AuthenticationStep> comparator = new Comparator<AuthenticationStep>() {
                public int compare(AuthenticationStep step1, AuthenticationStep step2) {
                    return step1.getStepOrder() - step2.getStepOrder();
                }
            };

            Arrays.sort(authenticationSteps, comparator);

            localAndOutboundConfiguration.setAuthenticationSteps(authenticationSteps);

            String authType = getAuthenticationType(applicationId, connection);
            localAndOutboundConfiguration.setAuthenticationType(authType);

            PreparedStatement localAndOutboundConfigPrepStmt = null;
            ResultSet localAndOutboundConfigResultSet = null;

            try {
                localAndOutboundConfigPrepStmt = connection
                        .prepareStatement(ApplicationMgtDBQueries.LOAD_LOCAL_AND_OUTBOUND_CONFIG_BY_APP_ID);
                localAndOutboundConfigPrepStmt.setInt(1, tenantId);
                localAndOutboundConfigPrepStmt.setInt(2, applicationId);
                localAndOutboundConfigResultSet = localAndOutboundConfigPrepStmt.executeQuery();

                if (localAndOutboundConfigResultSet.next()) {
                    localAndOutboundConfiguration.setUseTenantDomainInLocalSubjectIdentifier("1"
                           .equals(localAndOutboundConfigResultSet.getString(1)));
                    localAndOutboundConfiguration.setUseUserstoreDomainInLocalSubjectIdentifier("1"
                           .equals(localAndOutboundConfigResultSet.getString(2)));
                    localAndOutboundConfiguration.setEnableAuthorization("1"
                            .equals(localAndOutboundConfigResultSet.getString(3)));
                    localAndOutboundConfiguration.setAlwaysSendBackAuthenticatedListOfIdPs("1"
                            .equals(localAndOutboundConfigResultSet.getString(4)));
                    localAndOutboundConfiguration.setSubjectClaimUri(localAndOutboundConfigResultSet
                            .getString(5));
                }
            } finally {
                IdentityApplicationManagementUtil.closeStatement(localAndOutboundConfigPrepStmt);
                IdentityApplicationManagementUtil.closeResultSet(localAndOutboundConfigResultSet);
            }

            return localAndOutboundConfiguration;
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getStepInfoPrepStmt);
            IdentityApplicationManagementUtil.closeResultSet(stepInfoResultSet);
        }
    }

    private boolean isFederationHubIdP(String idPName, Connection connection, int tenantId)
            throws SQLException {

        PreparedStatement get = null;
        ResultSet resultSet = null;

        try {
            get = connection.prepareStatement(ApplicationMgtDBQueries.LOAD_HUB_IDP_BY_NAME);

            get.setString(1, idPName);
            get.setInt(2, tenantId);
            resultSet = get.executeQuery();

            if (resultSet.next()) {
                return "1".equals(resultSet.getString(1));
            }

            return false;
        } finally {
            IdentityApplicationManagementUtil.closeStatement(get);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
        }

    }

    /**
     * @param applicationId
     * @param connection
     * @return
     * @throws IdentityApplicationManagementException
     */
    private ClaimConfig getClaimConfiguration(int applicationId, Connection connection, int tenantID)
            throws IdentityApplicationManagementException {

        ClaimConfig claimConfig = new ClaimConfig();
        ArrayList<ClaimMapping> claimMappingList = new ArrayList<ClaimMapping>();

        if (log.isDebugEnabled()) {
            log.debug("Reading Claim Mappings of Application " + applicationId);
        }

        PreparedStatement get = null;
        ResultSet resultSet = null;
        try {
            get = connection.prepareStatement(ApplicationMgtDBQueries.LOAD_CLAIM_MAPPING_BY_APP_ID);
            // IDP_CLAIM, SP_CLAIM, IS_REQUESTED
            get.setInt(1, applicationId);
            get.setInt(2, tenantID);
            resultSet = get.executeQuery();

            while (resultSet.next()) {
                ClaimMapping claimMapping = new ClaimMapping();
                Claim localClaim = new Claim();
                Claim remoteClaim = new Claim();

                localClaim.setClaimUri(resultSet.getString(1));
                remoteClaim.setClaimUri(resultSet.getString(2));

                String requested = resultSet.getString(3);

                if ("1".equalsIgnoreCase(requested)) {
                    claimMapping.setRequested(true);
                } else {
                    claimMapping.setRequested(false);
                }

                String mandatory = resultSet.getString(4);

                if ("1".equalsIgnoreCase(mandatory)) {
                    claimMapping.setMandatory(true);
                } else {
                    claimMapping.setMandatory(false);
                }

                if (remoteClaim.getClaimUri() == null
                        || remoteClaim.getClaimUri().trim().length() == 0) {
                    remoteClaim.setClaimUri(localClaim.getClaimUri());
                }

                if (localClaim.getClaimUri() == null
                        || localClaim.getClaimUri().trim().length() == 0) {
                    localClaim.setClaimUri(remoteClaim.getClaimUri());
                }

                claimMapping.setDefaultValue(resultSet.getString(5));

                claimMapping.setLocalClaim(localClaim);
                claimMapping.setRemoteClaim(remoteClaim);

                claimMappingList.add(claimMapping);

                if (log.isDebugEnabled()) {
                    log.debug("Local Claim: " + claimMapping.getLocalClaim().getClaimUri()
                            + " SPClaim: " + claimMapping.getRemoteClaim().getClaimUri());
                }
            }

            claimConfig.setClaimMappings(claimMappingList.toArray(new ClaimMapping[claimMappingList
                    .size()]));
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving all application");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(get);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
        }

        PreparedStatement loadClaimConfigsPrepStmt = null;
        ResultSet loadClaimConfigsResultSet = null;

        try {
            loadClaimConfigsPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_CLAIM_CONIFG_BY_APP_ID);
            loadClaimConfigsPrepStmt.setInt(1, tenantID);
            loadClaimConfigsPrepStmt.setInt(2, applicationId);
            loadClaimConfigsResultSet = loadClaimConfigsPrepStmt.executeQuery();

            while (loadClaimConfigsResultSet.next()) {
                claimConfig.setRoleClaimURI(loadClaimConfigsResultSet.getString(1));
                claimConfig.setLocalClaimDialect("1".equals(loadClaimConfigsResultSet.getString(2)));
                claimConfig.setAlwaysSendMappedLocalSubjectId("1".equals(loadClaimConfigsResultSet
                                                                                 .getString(3)));
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while retrieving all application");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(loadClaimConfigsPrepStmt);
            IdentityApplicationManagementUtil.closeResultSet(loadClaimConfigsResultSet);
        }

        return claimConfig;
    }

    /**
     * @param applicationId
     * @param connection
     * @return
     * @throws IdentityApplicationManagementException
     */
    private RequestPathAuthenticatorConfig[] getRequestPathAuthenticators(int applicationId,
                                                                          Connection connection, int tenantID) throws IdentityApplicationManagementException {

        PreparedStatement loadReqPathAuthenticators = null;
        ResultSet authResultSet = null;
        List<RequestPathAuthenticatorConfig> authenticators = new ArrayList<RequestPathAuthenticatorConfig>();

        try {
            loadReqPathAuthenticators = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_REQ_PATH_AUTHENTICATORS_BY_APP_ID);
            loadReqPathAuthenticators.setInt(1, applicationId);
            loadReqPathAuthenticators.setInt(2, tenantID);
            authResultSet = loadReqPathAuthenticators.executeQuery();

            while (authResultSet.next()) {
                RequestPathAuthenticatorConfig reqAuth = new RequestPathAuthenticatorConfig();
                reqAuth.setName(authResultSet.getString(1));
                authenticators.add(reqAuth);
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving all application");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(loadReqPathAuthenticators);
            IdentityApplicationManagementUtil.closeResultSet(authResultSet);
        }

        return authenticators.toArray(new RequestPathAuthenticatorConfig[authenticators.size()]);
    }

    /**
     * @param applicationId
     * @param authenticators
     * @param connection
     * @throws IdentityApplicationManagementException
     */
    private void updateRequestPathAuthenticators(int applicationId,
                                                 RequestPathAuthenticatorConfig[] authenticators, Connection connection)
            throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        PreparedStatement storeReqPathAuthenticators = null;

        try {
            storeReqPathAuthenticators = connection
                    .prepareStatement(ApplicationMgtDBQueries.STORE_REQ_PATH_AUTHENTICATORS);
            if (authenticators != null && authenticators.length > 0) {
                for (RequestPathAuthenticatorConfig auth : authenticators) {
                    // TENANT_ID, AUTHENTICATOR_NAME, APP_ID
                    storeReqPathAuthenticators.setInt(1, tenantID);
                    storeReqPathAuthenticators.setString(2, auth.getName());
                    storeReqPathAuthenticators.setInt(3, applicationId);
                    storeReqPathAuthenticators.addBatch();
                }
                storeReqPathAuthenticators.executeBatch();
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving all application");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeReqPathAuthenticators);
        }
    }

    /**
     * @param applicationID
     * @param connection
     * @throws SQLException
     */
    private void deleteRequestPathAuthenticators(int applicationID, Connection connection)
            throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting request path authenticators " + applicationID);
        }

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement deleteReqAuthPrepStmt = null;
        try {
            deleteReqAuthPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.REMOVE_REQ_PATH_AUTHENTICATOR);
            deleteReqAuthPrepStmt.setInt(1, applicationID);
            deleteReqAuthPrepStmt.setInt(2, tenantID);
            deleteReqAuthPrepStmt.execute();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteReqAuthPrepStmt);
        }
    }

    /**
     * Reads the claim mappings for a given appID
     *
     * @param applicationId
     * @param connection
     * @return
     * @throws IdentityApplicationManagementException
     */
    private List<RoleMapping> getRoleMappingOfApplication(int applicationId, Connection connection,
                                                          int tenantID) throws IdentityApplicationManagementException {

        ArrayList<RoleMapping> roleMappingList = new ArrayList<RoleMapping>();

        if (log.isDebugEnabled()) {
            log.debug("Reading Role Mapping of Application " + applicationId);
        }

        PreparedStatement getClientInfo = null;
        ResultSet resultSet = null;
        try {
            getClientInfo = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_ROLE_MAPPING_BY_APP_ID);
            // IDP_ROLE, SP_ROLE
            getClientInfo.setInt(1, applicationId);
            getClientInfo.setInt(2, tenantID);
            resultSet = getClientInfo.executeQuery();

            while (resultSet.next()) {
                RoleMapping roleMapping = new RoleMapping();
                LocalRole localRole = new LocalRole();
                localRole.setLocalRoleName(resultSet.getString(1));
                roleMapping.setLocalRole(localRole);
                roleMapping.setRemoteRole(resultSet.getString(2));
                roleMappingList.add(roleMapping);

                if (log.isDebugEnabled()) {
                    log.debug("Local Role: " + roleMapping.getLocalRole().getLocalRoleName()
                            + " SPRole: " + roleMapping.getRemoteRole());
                }
            }

        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving all application");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getClientInfo);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
        }
        return roleMappingList;
    }

    /**
     * Get application Names for user
     *
     * @return
     * @throws IdentityApplicationManagementException
     */
    public ApplicationBasicInfo[] getAllApplicationBasicInfo()
            throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("Reading all Applications of Tenant " + tenantID);
        }

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement getAppNamesStmt = null;
        ResultSet appNameResultSet = null;

        ArrayList<ApplicationBasicInfo> appInfo = new ArrayList<ApplicationBasicInfo>();

        try {
            getAppNamesStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT);
            getAppNamesStmt.setInt(1, tenantID);
            appNameResultSet = getAppNamesStmt.executeQuery();

            while (appNameResultSet.next()) {
                ApplicationBasicInfo basicInfo = new ApplicationBasicInfo();
                if (ApplicationConstants.LOCAL_SP.equals(appNameResultSet.getString(1))) {
                    continue;
                }
                basicInfo.setApplicationName(appNameResultSet.getString(1));
                basicInfo.setDescription(appNameResultSet.getString(2));
                appInfo.add(basicInfo);
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while Reading all Applications");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getAppNamesStmt);
            IdentityApplicationManagementUtil.closeResultSet(appNameResultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return appInfo.toArray(new ApplicationBasicInfo[appInfo.size()]);
    }

    /**
     * Deletes the application from IDN_APPMGT_APP table. Cascade deletes with foreign key
     * constraints should delete the corresponding entries from the tables
     *
     * @param appName
     * @throws IdentityApplicationManagementException
     */
    public void deleteApplication(String appName) throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Connection connection = IdentityDatabaseUtil.getDBConnection();

        if (log.isDebugEnabled()) {
            log.debug("Deleting Application " + appName);
        }

        // Now, delete the application
        PreparedStatement deleteClientPrepStmt = null;
        try {
            // First, delete all the clients of the application
            int applicationID = getApplicationIDByName(appName, tenantID, connection);
            InboundAuthenticationConfig clients = getInboundAuthenticationConfig(applicationID,
                    connection, tenantID);
            for (InboundAuthenticationRequestConfig client : clients
                    .getInboundAuthenticationRequestConfigs()) {
                deleteClient(client.getInboundAuthKey(), client.getInboundAuthType());
            }

            deleteClientPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.REMOVE_APP_FROM_APPMGT_APP);
            deleteClientPrepStmt.setString(1, appName);
            deleteClientPrepStmt.setInt(2, tenantID);
            deleteClientPrepStmt.execute();

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error deleting application", e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteClientPrepStmt);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
    }

    /**
     * Deletes the Application with application ID
     *
     * @param applicationID
     * @param connection
     * @throws IdentityApplicationManagementException
     */
    public void deleteApplication(int applicationID, Connection connection)
            throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("Deleting Application " + applicationID);
        }

        // Now, delete the application
        PreparedStatement deleteClientPrepStmt = null;
        try {

            // delete clients
            InboundAuthenticationConfig clients = getInboundAuthenticationConfig(applicationID,
                    connection, tenantID);
            for (InboundAuthenticationRequestConfig client : clients
                    .getInboundAuthenticationRequestConfigs()) {
                deleteClient(client.getInboundAuthKey(), client.getInboundAuthType());
            }

            String applicationName = getApplicationName(applicationID, connection);
            // delete roles
            ApplicationMgtUtil.deleteAppRole(applicationName);

            deleteClientPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.REMOVE_APP_FROM_APPMGT_APP_WITH_ID);
            deleteClientPrepStmt.setInt(1, applicationID);
            deleteClientPrepStmt.setInt(2, tenantID);
            deleteClientPrepStmt.execute();

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IdentityApplicationManagementException("Error deleting application");

        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteClientPrepStmt);
        }

    }

    /**
     * Deleting Clients of the Application
     *
     * @param applicationID
     * @param connection
     * @throws IdentityApplicationManagementException
     */
    private void deleteInboundAuthRequestConfiguration(int applicationID, Connection connection)
            throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting Clients of the Application " + applicationID);
        }

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        PreparedStatement deleteClientPrepStmt = null;

        try {
            deleteClientPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.REMOVE_CLIENT_FROM_APPMGT_CLIENT);
            // APP_ID = ? AND TENANT_ID = ?
            deleteClientPrepStmt.setInt(1, applicationID);
            deleteClientPrepStmt.setInt(2, tenantID);
            deleteClientPrepStmt.execute();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteClientPrepStmt);
        }
    }

    /**
     * @param applicationId
     * @param connection
     * @throws SQLException
     */
    private void deleteLocalAndOutboundAuthenticationConfiguration(int applicationId,
                                                                   Connection connection) throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting Steps of Application " + applicationId);
        }

        PreparedStatement deleteLocalAndOutboundAuthConfigPrepStmt = null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            deleteLocalAndOutboundAuthConfigPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.REMOVE_STEP_FROM_APPMGT_STEP);
            deleteLocalAndOutboundAuthConfigPrepStmt.setInt(1, applicationId);
            deleteLocalAndOutboundAuthConfigPrepStmt.setInt(2, tenantId);
            deleteLocalAndOutboundAuthConfigPrepStmt.execute();

        } finally {
            IdentityApplicationManagementUtil
                    .closeStatement(deleteLocalAndOutboundAuthConfigPrepStmt);
        }
    }

    /**
     * @param applicationId
     * @param connection
     * @throws SQLException
     */
    private void deleteOutboundProvisioningConfiguration(int applicationId, Connection connection)
            throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting Steps of Application " + applicationId);
        }

        PreparedStatement deleteOutboundProConfigPrepStmt = null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            deleteOutboundProConfigPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.REMOVE_PRO_CONNECTORS);
            deleteOutboundProConfigPrepStmt.setInt(1, applicationId);
            deleteOutboundProConfigPrepStmt.setInt(2, tenantId);
            deleteOutboundProConfigPrepStmt.execute();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteOutboundProConfigPrepStmt);
        }
    }

    /**
     * Deletes clients using the service stubs
     *
     * @param clientIdentifier
     * @param type
     * @throws IdentityApplicationManagementException
     */
    private void deleteClient(String clientIdentifier, String type)
            throws IdentityApplicationManagementException {
        if ("samlsso".equalsIgnoreCase(type)) {
            new SAMLApplicationDAOImpl().removeServiceProviderConfiguration(clientIdentifier);
        } else if ("oauth2".equalsIgnoreCase(type)) {
            new OAuthApplicationDAOImpl().removeOAuthApplication(clientIdentifier);
        }
    }

    /**
     * Delete Claim Mapping of the Application
     *
     * @param applicationID
     * @param connection
     * @throws IdentityApplicationManagementException
     */
    private void deteClaimConfiguration(int applicationID, Connection connection)
            throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting Application Claim Mapping " + applicationID);
        }

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement deleteCliamPrepStmt = null;
        try {
            deleteCliamPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.REMOVE_CLAIM_MAPPINGS_FROM_APPMGT_CLAIM_MAPPING);
            deleteCliamPrepStmt.setInt(1, applicationID);
            deleteCliamPrepStmt.setInt(2, tenantID);
            deleteCliamPrepStmt.execute();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteCliamPrepStmt);
        }
    }

    /**
     * @param applicationID
     * @param connection
     * @throws IdentityApplicationManagementException
     */
    public void deletePermissionAndRoleConfiguration(int applicationID, Connection connection)
            throws SQLException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("Deleting Role Mapping of Application " + applicationID);
        }

        PreparedStatement deleteRoleMappingPrepStmt = null;
        try {
            deleteRoleMappingPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.REMOVE_ROLE_MAPPINGS_FROM_APPMGT_ROLE_MAPPING);
            deleteRoleMappingPrepStmt.setInt(1, applicationID);
            deleteRoleMappingPrepStmt.setInt(2, tenantID);
            deleteRoleMappingPrepStmt.execute();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteRoleMappingPrepStmt);
        }
    }

    /**
     * Delete assigned role permission mappings for deleted permissions
     *
     * @param connection
     * @param applicationName
     * @param permissions
     * @throws IdentityApplicationManagementException
     * @throws SQLException
     */
    public void deleteAssignedPermissions(Connection connection, String applicationName, ApplicationPermission[] permissions)
            throws IdentityApplicationManagementException, SQLException {
        List<ApplicationPermission> loadPermissions = ApplicationMgtUtil.loadPermissions(applicationName);
        List<ApplicationPermission> removedPermissions = null;
        if (!CollectionUtils.isEmpty(loadPermissions)) {
            if (ArrayUtils.isEmpty(permissions)) {
                removedPermissions = new ArrayList<ApplicationPermission>(loadPermissions);
            } else {
                removedPermissions = new ArrayList<ApplicationPermission>();
                for (ApplicationPermission storedPermission : loadPermissions) {
                    boolean isStored = false;
                    for (ApplicationPermission applicationPermission : permissions) {
                        if (applicationPermission.getValue().equals(storedPermission.getValue())) {
                            isStored = true;
                            break;
                        }
                    }
                    if (!isStored) {
                        removedPermissions.add(storedPermission);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(removedPermissions)) {
            //delete permissions
            for (ApplicationPermission applicationPermission : removedPermissions) {
                String permissionValue = ApplicationMgtUtil.PATH_CONSTANT +
                        ApplicationMgtUtil.getApplicationPermissionPath() +
                        ApplicationMgtUtil.PATH_CONSTANT +
                        applicationName + ApplicationMgtUtil.PATH_CONSTANT +
                        applicationPermission.getValue();
                int permisionId = getPermissionId(permissionValue.toLowerCase());
                deleteRolePermissionMapping(permisionId);
                deletePermission(permisionId);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO#getServiceProviderNameByClientId
     * (java.lang.String, java.lang.String, java.lang.String)
     */
    public String getServiceProviderNameByClientId(String clientId, String clientType,
                                                   String tenantDomain) throws IdentityApplicationManagementException {
        int tenantID = -123;

        if (tenantDomain != null) {
            try {
                tenantID = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                        .getTenantManager().getTenantId(tenantDomain);
            } catch (UserStoreException e1) {
                throw new IdentityApplicationManagementException("Error while reading application");
            }
        }

        String applicationName = null;

        // Reading application name from the database
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement storeAppPrepStmt = null;
        ResultSet appNameResult = null;
        try {
            storeAppPrepStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_APPLICATION_NAME_BY_CLIENT_ID_AND_TYPE);
            storeAppPrepStmt.setString(1, clientId);
            storeAppPrepStmt.setString(2, clientType);
            storeAppPrepStmt.setInt(3, tenantID);
            storeAppPrepStmt.setInt(4, tenantID);
            appNameResult = storeAppPrepStmt.executeQuery();
            if (appNameResult.next()) {
                applicationName = appNameResult.getString(1);
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while reading application", e);
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(appNameResult);
            IdentityApplicationManagementUtil.closeStatement(storeAppPrepStmt);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return applicationName;
    }

    /**
     * @param serviceProviderName
     * @param tenantDomain
     * @param localIdpAsKey
     * @return
     * @throws SQLException
     * @throws IdentityApplicationManagementException
     */
    private Map<String, String> getClaimMapping(String serviceProviderName, String tenantDomain,
                                                boolean localIdpAsKey) throws SQLException, IdentityApplicationManagementException {

        int tenantID = -123;

        if (tenantDomain != null) {
            try {
                tenantID = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                        .getTenantManager().getTenantId(tenantDomain);
            } catch (UserStoreException e1) {
                throw new IdentityApplicationManagementException("Error while reading application");
            }
        }
        Map<String, String> claimMapping = new HashMap<String, String>();

        if (log.isDebugEnabled()) {
            log.debug("Reading Claim Mappings of Application " + serviceProviderName);
        }

        PreparedStatement getClaimPreStmt = null;
        ResultSet resultSet = null;
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        try {

            getClaimPreStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_CLAIM_MAPPING_BY_APP_NAME);
            // IDP_CLAIM, SP_CLAIM, IS_REQUESTED
            getClaimPreStmt.setString(1, serviceProviderName);
            getClaimPreStmt.setInt(2, tenantID);
            resultSet = getClaimPreStmt.executeQuery();

            while (resultSet.next()) {
                if (localIdpAsKey) {
                    claimMapping.put(resultSet.getString(1), resultSet.getString(2));
                } else {
                    claimMapping.put(resultSet.getString(2), resultSet.getString(1));
                }
            }
            connection.commit();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getClaimPreStmt);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return claimMapping;
    }

    @Override
    public Map<String, String> getServiceProviderToLocalIdPClaimMapping(String serviceProviderName,
                                                                        String tenantDomain) throws IdentityApplicationManagementException {
        try {
            return getClaimMapping(serviceProviderName, tenantDomain, false);
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving claim mapping", e);
        }
    }

    @Override
    public Map<String, String> getLocalIdPToServiceProviderClaimMapping(String serviceProviderName,
                                                                        String tenantDomain) throws IdentityApplicationManagementException {
        try {
            return getClaimMapping(serviceProviderName, tenantDomain, true);
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving claim mapping", e);
        }
    }

    @Override
    public List<String> getAllRequestedClaimsByServiceProvider(String serviceProviderName,
                                                               String tenantDomain) throws IdentityApplicationManagementException {
        int tenantID = -123;

        if (tenantDomain != null) {
            try {
                tenantID = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                        .getTenantManager().getTenantId(tenantDomain);
            } catch (UserStoreException e1) {
                throw new IdentityApplicationManagementException("Error while reading application");
            }
        }
        List<String> reqClaimUris = new ArrayList<String>();

        if (log.isDebugEnabled()) {
            log.debug("Reading Claim Mappings of Application " + serviceProviderName);
        }

        PreparedStatement getClaimPreStmt = null;
        ResultSet resultSet = null;
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        try {

            getClaimPreStmt = connection
                    .prepareStatement(ApplicationMgtDBQueries.LOAD_CLAIM_MAPPING_BY_APP_NAME);

            // IDP_CLAIM, SP_CLAIM, IS_REQUESTED
            getClaimPreStmt.setString(1, serviceProviderName);
            getClaimPreStmt.setInt(2, tenantID);
            resultSet = getClaimPreStmt.executeQuery();
            while (resultSet.next()) {
                if ("1".equalsIgnoreCase(resultSet.getString(3))) {
                    reqClaimUris.add(resultSet.getString(1));
                }
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving requested claims", e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getClaimPreStmt);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
        return reqClaimUris;

    }

    /**
     * @param conn
     * @param tenantId
     * @param idpName
     * @param authenticatorName
     * @return
     * @throws SQLException
     */
    private int getAuthentictorID(Connection conn, int tenantId, String idpName,
                                  String authenticatorName) throws SQLException {
        if (idpName == null || idpName.isEmpty()) {
            return -1;
        }
        int authId = -1;

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt = ApplicationMgtDBQueries.LOAD_IDP_AUTHENTICATOR_ID;
        try {
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setString(1,authenticatorName);
            prepStmt.setString(2, idpName);
            prepStmt.setInt(3, tenantId);
            prepStmt.setInt(4, tenantId);
            prepStmt.setInt(5, MultitenantConstants.SUPER_TENANT_ID);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                authId = rs.getInt(1);
            }
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
        return authId;
    }

    /**
     * @param conn
     * @param tenantId
     * @param authenticatorId
     * @return
     * @throws SQLException
     */
    private Map<String, String> getAuthenticatorInfo(Connection conn, int tenantId,
                                                     int authenticatorId) throws SQLException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt = ApplicationMgtDBQueries.LOAD_IDP_AND_AUTHENTICATOR_NAMES;
        Map<String, String> returnData = new HashMap<String, String>();
        try {
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, authenticatorId);
            prepStmt.setInt(2, tenantId);
            prepStmt.setInt(3, tenantId);
            prepStmt.setInt(4, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt.setInt(5, MultitenantConstants.SUPER_TENANT_ID);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                returnData.put(ApplicationConstants.IDP_NAME, rs.getString(1));
                returnData.put(ApplicationConstants.IDP_AUTHENTICATOR_NAME, rs.getString(2));
                returnData
                        .put(ApplicationConstants.IDP_AUTHENTICATOR_DISPLAY_NAME, rs.getString(3));
            }
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
        return returnData;
    }

    /**
     * @param conn
     * @param tenantId
     * @param idpName
     * @param authenticatorName
     * @param authenticatorDispalyName
     * @return
     * @throws SQLException
     */
    private int addAuthenticator(Connection conn, int tenantId, String idpName,
                                 String authenticatorName, String authenticatorDispalyName) throws SQLException {
        int authenticatorId = -1;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        // TENANT_ID, IDP_ID, NAME,IS_ENABLED, DISPLAY_NAME
        String sqlStmt = ApplicationMgtDBQueries.STORE_LOCAL_AUTHENTICATOR;
        try {
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            prepStmt = conn.prepareStatement(sqlStmt, new String[]{
                    DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "ID")});
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, idpName);
            prepStmt.setInt(3, tenantId);
            prepStmt.setString(4, authenticatorName);
            prepStmt.setString(5, "1");
            prepStmt.setString(6, authenticatorDispalyName);
            prepStmt.execute();
            rs = prepStmt.getGeneratedKeys();
            if (rs.next()) {
                authenticatorId = rs.getInt(1);
            }
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
        return authenticatorId;
    }

    /**
     * Read application role permissions for a given application name
     *
     * @param applicationName Application name
     * @return Map of key value pairs. key is UM table id and value is permission
     * @throws SQLException
     */
    private Map<String, String> readApplicationPermissions(String applicationName) throws SQLException {
        PreparedStatement readPermissionsPrepStmt = null;
        ResultSet resultSet = null;
        Connection connection = null;
        Map<String, String> permissions = new HashMap<>();
        try {

            connection = IdentityDatabaseUtil.getUserDBConnection();
            readPermissionsPrepStmt = connection.prepareStatement(ApplicationMgtDBQueries.LOAD_UM_PERMISSIONS);
            readPermissionsPrepStmt.setString(1, "%" + ApplicationMgtUtil.getApplicationPermissionPath() + "%");
            resultSet = readPermissionsPrepStmt.executeQuery();
            while (resultSet.next()) {
                String UM_ID = resultSet.getString(1);
                String permission = resultSet.getString(2);
                if (permission.contains(ApplicationMgtUtil.getApplicationPermissionPath() +
                        ApplicationMgtUtil.PATH_CONSTANT + applicationName.toLowerCase())) {
                    permissions.put(UM_ID, permission);
                }
            }
        } finally {
            IdentityDatabaseUtil.closeResultSet(resultSet);
            IdentityDatabaseUtil.closeStatement(readPermissionsPrepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return permissions;
    }

    /**
     * Update the permission path for a given id
     *
     * @param id         Id
     * @param newPermission New permission path value
     * @throws SQLException
     */
    private void updatePermissionPath(String id, String newPermission) throws SQLException {
        PreparedStatement updatePermissionPrepStmt = null;
        Connection connection = null;
        try {

            connection = IdentityDatabaseUtil.getUserDBConnection();
            updatePermissionPrepStmt = connection.prepareStatement(ApplicationMgtDBQueries.UPDATE_SP_PERMISSIONS);
            updatePermissionPrepStmt.setString(1, newPermission);
            updatePermissionPrepStmt.setString(2, id);
            updatePermissionPrepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(updatePermissionPrepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Get permission id for a given permission path
     *
     * @param permission Permission path
     * @return Permission id
     * @throws SQLException
     */
    private int getPermissionId(String permission) throws SQLException {
        PreparedStatement loadPermissionsPrepStmt = null;
        ResultSet resultSet = null;
        Connection connection = null;
        int id = -1;
        try {

            connection = IdentityDatabaseUtil.getUserDBConnection();
            loadPermissionsPrepStmt = connection.prepareStatement(ApplicationMgtDBQueries.LOAD_UM_PERMISSIONS_W);
            loadPermissionsPrepStmt.setString(1, permission.toLowerCase());
            resultSet = loadPermissionsPrepStmt.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt(1);
            }
        } finally {
            IdentityDatabaseUtil.closeResultSet(resultSet);
            IdentityDatabaseUtil.closeStatement(loadPermissionsPrepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return id;
    }

    /**
     * Delete role permission mapping for a given permission id
     *
     * @param id   Permission id
     * @throws SQLException
     */
    private void deleteRolePermissionMapping(int id) throws SQLException {
        PreparedStatement deleteRolePermissionPrepStmt = null;
        Connection connection = null;
        try {

            connection = IdentityDatabaseUtil.getUserDBConnection();
            deleteRolePermissionPrepStmt = connection.prepareStatement(ApplicationMgtDBQueries.REMOVE_UM_ROLE_PERMISSION);
            deleteRolePermissionPrepStmt.setInt(1, id);
            deleteRolePermissionPrepStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteRolePermissionPrepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Delete permission entry for a given id
     *
     * @param entry_id   Entry id
     * @throws SQLException
     */
    private void deletePermission(int entry_id) throws SQLException {
        PreparedStatement deletePermissionPrepStmt = null;
        Connection connection = null;
        try {

            connection = IdentityDatabaseUtil.getUserDBConnection();
            deletePermissionPrepStmt = connection.prepareStatement(ApplicationMgtDBQueries.REMOVE_UM_PERMISSIONS);
            deletePermissionPrepStmt.setInt(1, entry_id);
            deletePermissionPrepStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(deletePermissionPrepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

}
