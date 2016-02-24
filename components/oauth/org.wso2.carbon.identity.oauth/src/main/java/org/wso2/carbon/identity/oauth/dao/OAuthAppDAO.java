/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.oauth.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthUtil;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.internal.OAuthComponentServiceHolder;
import org.wso2.carbon.identity.oauth.tokenprocessor.PlainTextPersistenceProcessor;
import org.wso2.carbon.identity.oauth.tokenprocessor.TokenPersistenceProcessor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Based data access layer for OAuth Consumer Applications.
 */
public class OAuthAppDAO {

    public static final Log log = LogFactory.getLog(OAuthAppDAO.class);
    private TokenPersistenceProcessor persistenceProcessor;

    public OAuthAppDAO() {

        try {
            persistenceProcessor = OAuthServerConfiguration.getInstance().getPersistenceProcessor();
        } catch (IdentityOAuth2Exception e) {
            log.error("Error retrieving TokenPersistenceProcessor. Defaulting to PlainTextPersistenceProcessor");
            persistenceProcessor = new PlainTextPersistenceProcessor();
        }

    }

    public void addOAuthApplication(OAuthAppDO consumerAppDO) throws IdentityOAuthAdminException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        if (!isDuplicateApplication(consumerAppDO.getUser().getUserName(), IdentityTenantUtil.getTenantId(consumerAppDO
                .getUser().getTenantDomain()), consumerAppDO.getUser().getUserStoreDomain(), consumerAppDO)) {
            try {
                prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.ADD_OAUTH_APP);
                prepStmt.setString(1, persistenceProcessor.getProcessedClientId(consumerAppDO.getOauthConsumerKey()));
                prepStmt.setString(2, persistenceProcessor.getProcessedClientSecret(consumerAppDO.getOauthConsumerSecret()));
                prepStmt.setString(3, consumerAppDO.getUser().getUserName());
                prepStmt.setInt(4, IdentityTenantUtil.getTenantId(consumerAppDO.getUser().getTenantDomain()));
                prepStmt.setString(5, consumerAppDO.getUser().getUserStoreDomain());
                prepStmt.setString(6, consumerAppDO.getApplicationName());
                prepStmt.setString(7, consumerAppDO.getOauthVersion());
                prepStmt.setString(8, consumerAppDO.getCallbackUrl());
                prepStmt.setString(9, consumerAppDO.getGrantTypes());
                prepStmt.execute();
                connection.commit();

            } catch (SQLException e) {
                throw new IdentityOAuthAdminException("Error when executing the SQL : " +
                        SQLQueries.OAuthAppDAOSQLQueries.ADD_OAUTH_APP, e);
            } catch (IdentityOAuth2Exception e) {
                throw new IdentityOAuthAdminException("Error occurred while processing the client id and client " +
                        "secret by TokenPersistenceProcessor");
            } finally {
                IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
            }
        } else {
            throw new IdentityOAuthAdminException("Error when adding the consumer application. " +
                    "An application with the same name already exists.");
        }
    }

    public String[] addOAuthConsumer(String username, int tenantId, String userDomain) throws IdentityOAuthAdminException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        String sqlStmt = null;
        String consumerKey;
        String consumerSecret = OAuthUtil.getRandomNumber();

        do {
            consumerKey = OAuthUtil.getRandomNumber();
        }
        while (isDuplicateConsumer(consumerKey));

        try {
            sqlStmt = SQLQueries.OAuthAppDAOSQLQueries.ADD_OAUTH_CONSUMER;
            prepStmt = connection.prepareStatement(sqlStmt);
            prepStmt.setString(1, consumerKey);
            prepStmt.setString(2, consumerSecret);
            prepStmt.setString(3, username);
            prepStmt.setInt(4, tenantId);
            prepStmt.setString(5, userDomain);
            // it is assumed that the OAuth version is 1.0a because this is required with OAuth 1.0a
            prepStmt.setString(6, OAuthConstants.OAuthVersions.VERSION_1A);
            prepStmt.execute();

            connection.commit();

        } catch (SQLException e) {
            throw new IdentityOAuthAdminException("Error when executing the SQL : " + sqlStmt, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return new String[]{consumerKey, consumerSecret};
    }

    public OAuthAppDO[] getOAuthConsumerAppsOfUser(String username, int tenantId) throws IdentityOAuthAdminException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;
        OAuthAppDO[] oauthAppsOfUser;

        try {
            RealmService realmService = OAuthComponentServiceHolder.getRealmService();
            String tenantDomain = realmService.getTenantManager().getDomain(tenantId);
            String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
            String tenantUnawareUserName = tenantAwareUserName + "@" + tenantDomain;
            boolean isUsernameCaseSensitive = IdentityUtil.isUserStoreInUsernameCaseSensitive(tenantUnawareUserName);

            String sql = SQLQueries.OAuthAppDAOSQLQueries.GET_APPS_OF_USER_WITH_TENANTAWARE_OR_TENANTUNAWARE_USERNAME;
            if (!isUsernameCaseSensitive) {
                sql = sql.replace("USERNAME", "LOWER(USERNAME)");
            }
            prepStmt = connection.prepareStatement(sql);
            if (isUsernameCaseSensitive) {
                prepStmt.setString(1, tenantAwareUserName);
                prepStmt.setString(2, tenantUnawareUserName);
            } else {
                prepStmt.setString(1, tenantAwareUserName.toLowerCase());
                prepStmt.setString(2, tenantUnawareUserName.toLowerCase());
            }

            prepStmt.setInt(3, tenantId);

            rSet = prepStmt.executeQuery();
            List<OAuthAppDO> oauthApps = new ArrayList<OAuthAppDO>();
            while (rSet.next()) {
                if (rSet.getString(3) != null && rSet.getString(3).length() > 0) {
                    OAuthAppDO oauthApp = new OAuthAppDO();
                    oauthApp.setOauthConsumerKey(persistenceProcessor.getPreprocessedClientId(rSet.getString(1)));
                    oauthApp.setOauthConsumerSecret(persistenceProcessor.getPreprocessedClientSecret(rSet.getString(2)));
                    oauthApp.setApplicationName(rSet.getString(3));
                    oauthApp.setOauthVersion(rSet.getString(4));
                    oauthApp.setCallbackUrl(rSet.getString(5));
                    oauthApp.setGrantTypes(rSet.getString(6));
                    oauthApp.setId(rSet.getInt(7));
                    AuthenticatedUser authenticatedUser = new AuthenticatedUser();
                    authenticatedUser.setUserName(rSet.getString(8));
                    authenticatedUser.setTenantDomain(IdentityTenantUtil.getTenantDomain(rSet.getInt(9)));
                    authenticatedUser.setUserStoreDomain(rSet.getString(10));
                    oauthApp.setUser(authenticatedUser);
                    oauthApps.add(oauthApp);
                }
            }
            oauthAppsOfUser = oauthApps.toArray(new OAuthAppDO[oauthApps.size()]);
            connection.commit();
        } catch (SQLException e) {
            throw new IdentityOAuthAdminException("Error occurred while retrieving OAuth consumer apps of user", e);
        } catch (UserStoreException e) {
            throw new IdentityOAuthAdminException("Error while retrieving Tenant Domain for tenant ID : " + tenantId, e);
        } catch (IdentityOAuth2Exception e) {
            throw new IdentityOAuthAdminException("Error occurred while processing client id and client secret by " +
                    "TokenPersistenceProcessor", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return oauthAppsOfUser;
    }

    public OAuthAppDO getAppInformation(String consumerKey) throws InvalidOAuthClientException, IdentityOAuth2Exception {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;
        OAuthAppDO oauthApp = null;

        try {
            prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.GET_APP_INFO);
            prepStmt.setString(1, persistenceProcessor.getProcessedClientId(consumerKey));

            rSet = prepStmt.executeQuery();
            List<OAuthAppDO> oauthApps = new ArrayList<>();
            /**
             * We need to determine whether the result set has more than 1 row. Meaning, we found an application for
             * the given consumer key. There can be situations where a user passed a key which doesn't yet have an
             * associated application. We need to barf with a meaningful error message for this case
             */
            boolean rSetHasRows = false;
            while (rSet.next()) {
                // There is at least one application associated with a given key
                rSetHasRows = true;
                if (rSet.getString(4) != null && rSet.getString(4).length() > 0) {
                    oauthApp = new OAuthAppDO();
                    oauthApp.setOauthConsumerKey(consumerKey);
                    oauthApp.setOauthConsumerSecret(persistenceProcessor.getPreprocessedClientSecret(rSet.getString(1)));
                    AuthenticatedUser authenticatedUser = new AuthenticatedUser();
                    authenticatedUser.setUserName(rSet.getString(2));
                    oauthApp.setApplicationName(rSet.getString(3));
                    oauthApp.setOauthVersion(rSet.getString(4));
                    oauthApp.setCallbackUrl(rSet.getString(5));
                    authenticatedUser.setTenantDomain(IdentityTenantUtil.getTenantDomain(rSet.getInt(6)));
                    authenticatedUser.setUserStoreDomain(rSet.getString(7));
                    oauthApp.setUser(authenticatedUser);
                    oauthApp.setGrantTypes(rSet.getString(8));
                    oauthApp.setId(rSet.getInt(9));
                    oauthApps.add(oauthApp);
                }
            }
            if (!rSetHasRows) {
                /**
                 * We come here because user submitted a key that doesn't have any associated application with it.
                 * We're throwing an error here because we cannot continue without this info. Otherwise it'll throw
                 * a null values not supported error when it tries to cache this info
                 */

                throw new InvalidOAuthClientException("Cannot find an application associated with the given consumer key : " + consumerKey);
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IdentityOAuth2Exception("Error while retrieving the app information", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return oauthApp;
    }

    public OAuthAppDO getAppInformationByAppName(String appName) throws InvalidOAuthClientException, IdentityOAuth2Exception {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;
        OAuthAppDO oauthApp = null;

        try {
            int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.GET_APP_INFO_BY_APP_NAME);
            prepStmt.setString(1, appName);
            prepStmt.setInt(2, tenantID);

            rSet = prepStmt.executeQuery();
            List<OAuthAppDO> oauthApps = new ArrayList<>();
            oauthApp = new OAuthAppDO();
            oauthApp.setApplicationName(appName);
            AuthenticatedUser user = new AuthenticatedUser();
            user.setTenantDomain(IdentityTenantUtil.getTenantDomain(tenantID));
            /**
             * We need to determine whether the result set has more than 1 row. Meaning, we found an application for
             * the given consumer key. There can be situations where a user passed a key which doesn't yet have an
             * associated application. We need to barf with a meaningful error message for this case
             */
            boolean rSetHasRows = false;
            while (rSet.next()) {
                // There is at least one application associated with a given key
                rSetHasRows = true;
                if (rSet.getString(4) != null && rSet.getString(4).length() > 0) {
                    oauthApp.setOauthConsumerSecret(persistenceProcessor.getPreprocessedClientSecret(rSet.getString(1)));
                    user.setUserName(rSet.getString(2));
                    user.setUserStoreDomain(rSet.getString(3));
                    oauthApp.setUser(user);
                    oauthApp.setOauthConsumerKey(persistenceProcessor.getPreprocessedClientId(rSet.getString(4)));
                    oauthApp.setOauthVersion(rSet.getString(5));
                    oauthApp.setCallbackUrl(rSet.getString(6));
                    oauthApp.setGrantTypes(rSet.getString(7));
                    oauthApp.setId(rSet.getInt(8));
                    oauthApps.add(oauthApp);
                }
            }
            if (!rSetHasRows) {
                /**
                 * We come here because user submitted a key that doesn't have any associated application with it.
                 * We're throwing an error here because we cannot continue without this info. Otherwise it'll throw
                 * a null values not supported error when it tries to cache this info
                 */
                String message = "Cannot find an application associated with the given consumer key : " + appName;
                if(log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw new InvalidOAuthClientException(message);
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IdentityOAuth2Exception("Error while retrieving the app information", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return oauthApp;
    }

    public void updateConsumerApplication(OAuthAppDO oauthAppDO) throws IdentityOAuthAdminException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        try {
            prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.UPDATE_CONSUMER_APP);
            prepStmt.setString(1, oauthAppDO.getApplicationName());
            prepStmt.setString(2, oauthAppDO.getCallbackUrl());
            prepStmt.setString(3, oauthAppDO.getGrantTypes());
            prepStmt.setString(4, persistenceProcessor.getProcessedClientId(oauthAppDO.getOauthConsumerKey()));
            prepStmt.setString(5, persistenceProcessor.getProcessedClientSecret(oauthAppDO.getOauthConsumerSecret()));

            int count = prepStmt.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug("No. of records updated for updating consumer application. : " + count);
            }
            connection.commit();

        } catch (SQLException e) {
            throw new IdentityOAuthAdminException("Error when updating OAuth application", e);
        } catch (IdentityOAuth2Exception e) {
            throw new IdentityOAuthAdminException("Error occurred while processing client id and client secret by " +
                    "TokenPersistenceProcessor", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    public void removeConsumerApplication(String consumerKey) throws IdentityOAuthAdminException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;

        try {
            prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.REMOVE_APPLICATION);
            prepStmt.setString(1, consumerKey);

            prepStmt.execute();
            connection.commit();

        } catch (SQLException e) {;
            throw new IdentityOAuthAdminException("Error when executing the SQL : " + SQLQueries.OAuthAppDAOSQLQueries.REMOVE_APPLICATION, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    /**
     * Update the OAuth service provider name.
     * @param appName Service provider name.
     * @param consumerKey Consumer key.
     * @throws IdentityApplicationManagementException
     */
    public void updateOAuthConsumerApp(String appName, String consumerKey)
            throws IdentityApplicationManagementException {

        PreparedStatement statement = null;
        Connection connection = null;

        try {
            connection = IdentityDatabaseUtil.getDBConnection();
            statement = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.UPDATE_OAUTH_INFO);
            statement.setString(1, appName);
            statement.setString(2, consumerKey);
            statement.execute();
            connection.setAutoCommit(false);
            connection.commit();
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while executing the SQL statement.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, statement);
        }
    }

    private boolean isDuplicateApplication(String username, int tenantId, String userDomain, OAuthAppDO consumerAppDTO)
            throws IdentityOAuthAdminException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;

        boolean isDuplicateApp = false;
        boolean isUsernameCaseSensitive = IdentityUtil.isUserStoreInUsernameCaseSensitive(username, tenantId);

        try {
            String sql = SQLQueries.OAuthAppDAOSQLQueries.CHECK_EXISTING_APPLICATION;
            if (!isUsernameCaseSensitive) {
                sql = sql.replace("USERNAME", "LOWER(USERNAME)");
            }
            prepStmt = connection.prepareStatement(sql);
            if (isUsernameCaseSensitive) {
                prepStmt.setString(1, username);
            } else {
                prepStmt.setString(1, username.toLowerCase());
            }
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, userDomain);
            prepStmt.setString(4, consumerAppDTO.getApplicationName());

            rSet = prepStmt.executeQuery();
            if (rSet.next()) {
                isDuplicateApp = true;
            }
            connection.commit();
        } catch (SQLException e) {
            throw new IdentityOAuthAdminException("Error when executing the SQL : " + SQLQueries.OAuthAppDAOSQLQueries.CHECK_EXISTING_APPLICATION, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return isDuplicateApp;
    }

    private boolean isDuplicateConsumer(String consumerKey) throws IdentityOAuthAdminException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;

        boolean isDuplicateConsumer = false;

        try {
            prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.CHECK_EXISTING_CONSUMER);
            prepStmt.setString(1, persistenceProcessor.getProcessedClientId(consumerKey));

            rSet = prepStmt.executeQuery();
            if (rSet.next()) {
                isDuplicateConsumer = true;
            }
            connection.commit();
        } catch (IdentityOAuth2Exception e) {
            throw new IdentityOAuthAdminException("Error occurred while processing the client id by TokenPersistenceProcessor");
        } catch (SQLException e) {
            throw new IdentityOAuthAdminException("Error when executing the SQL : " + SQLQueries
                    .OAuthAppDAOSQLQueries.CHECK_EXISTING_CONSUMER, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return isDuplicateConsumer;
    }

}