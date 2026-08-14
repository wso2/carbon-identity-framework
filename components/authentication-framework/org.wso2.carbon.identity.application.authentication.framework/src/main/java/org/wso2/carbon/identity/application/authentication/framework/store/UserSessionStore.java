/*
 *   Copyright (c) 2018-2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.application.authentication.framework.store;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.dao.UserSessionDAO;
import org.wso2.carbon.identity.application.authentication.framework.dao.UserSessionDAOFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.DuplicatedAuthUserException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.LOCAL_IDP_NAME;

/**
 * Class to store and retrieve user related data.
 * <p>
 * The user, identity provider and application data is held in the identity store, while the session data is
 * held in the configured session store and is accessed through {@link UserSessionDAO}.
 */
public class UserSessionStore {

    private static final UserSessionStore instance = new UserSessionStore();
    private static final String FEDERATED_USER_DOMAIN = "FEDERATED";

    private UserSessionStore() {

    }

    public static UserSessionStore getInstance() {
        return instance;
    }

    /**
     * Method to store user and session mapping.
     *
     * @param userName   Name of the authenticated user
     * @param tenantId   Id of the tenant domain
     * @param userDomain Name of the user Store domain
     * @param idPId      Identity Provider id
     * @throws UserSessionException if an error occurs when storing the authenticated user details to the database
     */
    public void storeUserData(String userId, String userName, int tenantId, String userDomain, int idPId)
            throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(true)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_INSERT_USER_STORE_OPERATION)) {
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, userName);
                preparedStatement.setInt(3, tenantId);
                preparedStatement.setString(4, (userDomain == null) ? FEDERATED_USER_DOMAIN :
                        userDomain.toUpperCase());
                preparedStatement.setInt(5, idPId);
                preparedStatement.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new DuplicatedAuthUserException("Error when store user data.", e1);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // Handle the constraint violation in case concurrent authentication requests had been initiated and the
            // mapping is already stored from another node.
            throw new DuplicatedAuthUserException("Duplicated user entry found in IDN_AUTH_USER table. Username: " +
                    userName + " Tenant Id: " + tenantId + " User Store Domain: " + userDomain + " Identity Provider " +
                    "Id: " + idPId, e);
        } catch (SQLException e) {
            // Handle constrain violation issue in JDBC drivers which does not throw
            // SQLIntegrityConstraintViolationException
            if (StringUtils.containsIgnoreCase(e.getMessage(), "USER_STORE_CONSTRAINT")) {
                throw new DuplicatedAuthUserException("Duplicated user entry found in IDN_AUTH_USER table. Username: " +
                        userName + " Tenant Id: " + tenantId + " User Store Domain: " + userDomain + " Identity " +
                        "Provider Id: " + idPId, e);

            } else {
                throw new UserSessionException("Error while storing authenticated user details to the database table " +
                        "IDN_AUTH_USER_STORE of user: " + userName + ", Tenant Id: " + tenantId + ", User domain: " +
                        userDomain + ", Identity provider id: " + idPId, e);
            }
        }
    }

    /**
     * Method to store user and session mapping for federated users.
     *
     * @param userName Name of the authenticated user
     * @param tenantId Id of the tenant domain
     * @param idPId    Identity Provider id
     * @throws UserSessionException if an error occurs when storing the authenticated user details to the database
     */
    public void storeUserData(String userId, String userName, int tenantId, int idPId) throws UserSessionException {

        storeUserData(userId, userName, tenantId, FEDERATED_USER_DOMAIN, idPId);
    }

    /**
     * Method to get the unique Id of a user from the database.
     *
     * @param userName   Name of the authenticated user
     * @param tenantId   Id of the tenant domain
     * @param userDomain Name of the user Store domain
     * @param idPId      Identity Provider id
     * @return the user id of the user
     * @throws UserSessionException if an error occurs when retrieving the user id of the user from the database
     */
    public String getUserId(String userName, int tenantId, String userDomain, int idPId)
            throws UserSessionException {

        String userId = null;
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_SELECT_USER_ID)) {
                preparedStatement.setString(1, userName);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, (userDomain == null) ? FEDERATED_USER_DOMAIN : userDomain.toUpperCase());
                preparedStatement.setInt(4, idPId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        userId = resultSet.getString(1);
                    }
                }
            } catch (SQLException e1) {
                throw new UserSessionException("Error while retrieving User Id of the user: " + userName + ", "
                        + "Tenant Id: " + tenantId + ", User domain: " + userDomain + ", Identity provider id: " +
                        idPId, e1);
            }

        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving User Id of the user: " + userName + ", Tenant Id: "
                    + tenantId + ", User domain: " + userDomain + ", Identity provider id: " + idPId, e);
        }
        return userId;
    }

    /**
     * Method to return the user Id of a user from the database.
     * @deprecated use {@link #getFederatedUserId(String, int, int)} instead.
     * Initially when the user store did not support user id, it was created and stored in here. Now the user store
     * support user ids for local users, this is not required for local users anymore. However similar capability is
     * still required for federated users.
     *
     * @param userName   Name of the authenticated user
     * @param tenantId   Id of the tenant domain
     * @param userDomain Name of the user Store domain
     * @return the user id of the user
     * @throws UserSessionException if an error occurs when retrieving the user id of the user from the database
     */
    @Deprecated
    public String getUserId(String userName, int tenantId, String userDomain) throws UserSessionException {

        String userId = null;
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_SELECT_USER_IDS_OF_USER)) {
                preparedStatement.setString(1, userName);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, (userDomain == null) ? FEDERATED_USER_DOMAIN : userDomain.toUpperCase());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        userId = resultSet.getString(1);
                    }
                }
            } catch (SQLException e1) {
                throw new UserSessionException("Error while retrieving User Id of the user: " + userName +
                        ", Tenant Id: " + tenantId + ", User domain: " + userDomain, e1);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving User Id of the user: " + userName + ", Tenant Id: "
                    + tenantId + ", User domain: " + userDomain, e);
        }
        return userId;
    }

    /**
     * Method to return the user Ids of the users in a given user store from the database.
     * @deprecated
     * User ids of local users are no longer stored in IDN_AUTH_USER table and user ids of all the users in a domain
     * should not be retrieved at once.
     *
     * @param userDomain name of the user Store domain
     * @param tenantId   id of the tenant domain
     * @return the list of user Ids of users stored in the given user store
     * @throws UserSessionException if an error occurs when retrieving the user id list from the database
     */
    @Deprecated
    public List<String> getUserIdsOfUserStore(String userDomain, int tenantId) throws UserSessionException {

        List<String> userIds = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_SELECT_USER_IDS_OF_USER_STORE)) {
                preparedStatement.setString(1, userDomain.toUpperCase());
                preparedStatement.setInt(2, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        userIds.add(resultSet.getString(1));
                    }
                }
            } catch (SQLException e1) {
                throw new UserSessionException("Error while retrieving user Ids stored in the user domain: " +
                        userDomain + ", Tenant Id: " + tenantId, e1);
            }

        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving user Ids stored in the user domain: " + userDomain
                    + ", Tenant Id: " + tenantId, e);
        }
        return userIds;
    }

    /**
     * Method to identity providers id from the IDP table.
     *
     * @param idPName name of the identity provider
     * @return id of the identity provider
     * @throws UserSessionException if an error occurs when retrieving the identity provider id list from the database
     * @deprecated instead use {@link #getIdPId(String, int)}.
     */
    @Deprecated
    public int getIdPId(String idPName) throws UserSessionException {

        int idPId = -1;
        if (idPName.equals("LOCAL")) {
            return idPId;
        }
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_SELECT_IDP_ID_OF_IDP)) {
                preparedStatement.setString(1, idPName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        idPId = resultSet.getInt(1);
                    }
                }
            } catch (SQLException e1) {
                throw new UserSessionException("Error while retrieving the IdP id of: " + idPName, e1);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving the IdP id of: " + idPName, e);
        }
        return idPId;
    }

    /**
     * Retrieve IDP ID from the IDP table using IDP name and tenant ID.
     *
     * @param idpName   IDP name.
     * @param tenantId  Tenant ID.
     * @return          IDP ID.
     * @throws UserSessionException
     */
    public int getIdPId(String idpName, int tenantId) throws UserSessionException {

        int idPId = -1;
        if (StringUtils.isBlank(idpName)) {
            throw new UserSessionException("Blank IDP Name is provided to retrieve IdP id of tenant ID: " + tenantId);
        }
        if (StringUtils.equals(LOCAL_IDP_NAME, idpName)) {
            return idPId;
        }
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_SELECT_IDP_WITH_TENANT)) {
                preparedStatement.setString(1, idpName);
                preparedStatement.setInt(2, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        idPId = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving the IdP id of: " + idpName + " and tenant ID: " +
                    tenantId, e);
        }
        return idPId;
    }

    /**
     * Method to store user id and session id mapping in the database table IDN_AUTH_USER_SESSION_MAPPING.
     *
     * @param userId    Id of the user
     * @param sessionId Id of the authenticated session
     * @throws UserSessionException if an error occurs when storing the mapping in the database
     */
    public void storeUserSessionData(String userId, String sessionId) throws UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO().storeUserSessionData(userId, sessionId);
    }

    /**
     * Method to check whether the user id and session id mapping is already exists in the database.
     *
     * @param userId    Id of the user
     * @param sessionId Id of the authenticated session
     * @return the boolean decision
     * @throws UserSessionException if an error occurs when retrieving the mapping from the database
     */
    public boolean isExistingMapping(String userId, String sessionId) throws UserSessionException {

        return UserSessionDAOFactory.getUserSessionDAO().isExistingMapping(userId, sessionId);
    }

    /**
     * Method to get session Id list of a given user Id.
     *
     * @param userId id of the user
     * @return the list of session ids
     * @throws UserSessionException if an error occurs when retrieving the session id list from the database
     */
    public List<String> getSessionId(String userId) throws UserSessionException {

        return UserSessionDAOFactory.getUserSessionDAO().getSessionId(userId);
    }

    /**
     * Method to get the active session ID list of a given user ID. A session is considered active while the session
     * store holds a record for it and no DELETE marker has been written for it.
     *
     * @param userId ID of the user.
     * @return The list of active session IDs.
     * @throws UserSessionException If an error occurs when retrieving the active session ID list from the database.
     */
    public List<String> getActiveSessionIds(String userId) throws UserSessionException {

        List<String> sessionIdList = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_SELECT_ACTIVE_SESSION_IDS_OF_USER_ID)) {
                preparedStatement.setString(1, userId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        sessionIdList.add(resultSet.getString(1));
                    }
                }
            } catch (SQLException e1) {
                throw new UserSessionException("Error while retrieving active session IDs for user ID: " + userId, e1);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving active session IDs for user ID: " + userId, e);
        }
        return sessionIdList;
    }

    /**
     * Removes all the expired session records from relevant tables.
     */
    public void removeExpiredSessionRecords() {

        UserSessionDAOFactory.getUserSessionDAO().removeExpiredSessionRecords();
    }

    /**
     * Remove the session information records of a given set of session IDs from the relevant tables.
     *
     * @param sessionIdList list of terminated session IDs
     */
    public void removeTerminatedSessionRecords(List<String> sessionIdList) {

        UserSessionDAOFactory.getUserSessionDAO().removeTerminatedSessionRecords(sessionIdList);
    }



    /**
     * Method to store app session data.
     *
     * @param sessionId   id of the authenticated session
     * @param subject     username in application
     * @param appID       id of the application
     * @param inboundAuth protocol used in app
     * @throws DataAccessException if an error occurs when storing the authenticated user details to the database
     */
    public void storeAppSessionData(String sessionId, String subject, int appID, String inboundAuth) throws
            DataAccessException {

        UserSessionDAOFactory.getUserSessionDAO().storeAppSessionData(sessionId, subject, appID, inboundAuth);
    }

    /**
     * Method to store app session data if the particular app session is not already exists in the database.
     *
     * @param sessionId   Id of the authenticated session.
     * @param subject     Username in application.
     * @param appID       Id of the application.
     * @param inboundAuth Protocol used in the app.
     * @throws DataAccessException if an error occurs when storing the authenticated user details to the database.
     * @deprecated Please use storeAppSessionData method instead.
     */
    @Deprecated
    public void storeAppSessionDataIfNotExist(String sessionId, String subject, int appID, String inboundAuth) throws
            DataAccessException {

        UserSessionDAOFactory.getUserSessionDAO().storeAppSessionDataIfNotExist(sessionId, subject, appID, inboundAuth);
    }

    /**
     * Method to get app id from SP_APP table.
     *
     * @param applicationName application Name
     * @param appTenantID     app tenant id
     * @return the application id
     * @throws UserSessionException if an error occurs when retrieving app id
     *
     * @deprecated Since the UserSessionStore should not invoke the application management table.
     */
    @Deprecated
    public int getAppId(String applicationName, int appTenantID) throws UserSessionException {

        Integer appId;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            appId = jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_SELECT_APP_ID_OF_APP,
                    ((resultSet, rowNumber) -> resultSet.getInt(1)),
                    preparedStatement -> {
                        preparedStatement.setString(1, applicationName);
                        preparedStatement.setInt(2, appTenantID);
                    });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving the app id of " + applicationName + ", " +
                    "tenant id" + appTenantID + ".", e);
        }
        return appId == null ? 0 : appId;
    }

    /**
     * Method to check whether the particular app session is already exists in the database.
     *
     * @param sessionId   id of the authenticated session
     * @param subject     user name of app
     * @param appID       id of application
     * @param inboundAuth protocol used in app
     * @return whether the app session is already available or not
     * @throws UserSessionException while retrieving existing session data
     */
    public boolean isExistingAppSession(String sessionId, String subject, int appID, String inboundAuth) throws
            UserSessionException {

        return UserSessionDAOFactory.getUserSessionDAO().isExistingAppSession(sessionId, subject, appID, inboundAuth);
    }

    /**
     * Method to store session meta data as a batch.
     *
     * @param sessionId id of the authenticated session
     * @param metaData  map of metadata type and value of the session
     * @throws UserSessionException while storing session meta data
     */
    public void storeSessionMetaData(String sessionId, Map<String, String> metaData) throws UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO().storeSessionMetaData(sessionId, metaData);
    }

    /**
     * Update session meta data.
     *
     * @param sessionId    id of the authenticated session
     * @param propertyType type of the meta data
     * @param value        value of the meta data
     * @throws UserSessionException if the meta data update in the database fails.
     */
    public void updateSessionMetaData(String sessionId, String propertyType, String value) throws
            UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO().updateSessionMetaData(sessionId, propertyType, value);
    }

    /**
     * Method to get session Id list of a given user.
     *
     * @param user  user object
     * @param idpId id of the user's idp
     * @return the list of session ids
     * @throws UserSessionException if an error occurs when retrieving the session id list from the database
     */
    public List<String> getSessionId(User user, int idpId) throws UserSessionException {

        return UserSessionDAOFactory.getUserSessionDAO().getSessionId(user, idpId);
    }

    /**
     * Method to check whether a given user already has a mapping with a given session id.
     *
     * @param user      user object
     * @param sessionId id of the authenticated session
     * @return the boolean decision
     * @throws UserSessionException if an error occurs when retrieving the mapping from the database
     */
    public boolean isExistingMapping(User user, int idpId, String sessionId) throws UserSessionException {

        return UserSessionDAOFactory.getUserSessionDAO().isExistingMapping(user, idpId, sessionId);
    }

    /**
     * Store session details of a given session context key to map the session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @throws UserSessionException Error while storing session details.
     */
    public void storeFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory)
            throws UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO().storeFederatedAuthSessionInfo(sessionContextKey, authHistory);
    }

    /**
     * Store session details with the given session context key for the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param tenantId          Tenant id.
     * @throws UserSessionException Error while storing session details.
     */
    public void storeFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId)
            throws UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO()
                .storeFederatedAuthSessionInfo(sessionContextKey, authHistory, tenantId);
    }

    /**
     * Store session details with the given session context key for the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param idpId             Federated IDP ID.
     * @throws UserSessionException Error while storing session details.
     */
    public void storeFederatedAuthSessionInfoWithIdpId(String sessionContextKey, AuthHistory authHistory, int idpId)
            throws UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO()
                .storeFederatedAuthSessionInfoWithIdpId(sessionContextKey, authHistory, idpId);
    }

    /**
     * Store session details if not exist of a given session context key to map the session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param tenantId          Tenant id.
     * @param idpId             Federated IdP id.
     * @throws UserSessionException Error while storing session details.
     */
    public void storeFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId,
                                              int idpId) throws UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO()
                .storeFederatedAuthSessionInfo(sessionContextKey, authHistory, tenantId, idpId);
    }

    /**
     * Update session details of a given session context key to map the current session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @throws UserSessionException Error while storing session details.
     */
    public void updateFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory) throws
            UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO().updateFederatedAuthSessionInfo(sessionContextKey, authHistory);
    }

    /**
     * Update session details of a given session context key to map the current session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param tenantId          Tenant id.
     * @throws UserSessionException Error while storing session details.
     */
    public void updateFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId) throws
            UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO()
                .updateFederatedAuthSessionInfo(sessionContextKey, authHistory, tenantId);
    }

    /**
     * Update session details of a given session context key to map the current session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param idpId             Federated IDP id.
     * @throws UserSessionException Error while storing session details.
     */
    public void updateFederatedAuthSessionInfoWithIdpId(String sessionContextKey, AuthHistory authHistory, int idpId)
            throws UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO()
                .updateFederatedAuthSessionInfoWithIdpId(sessionContextKey, authHistory, idpId);
    }

    /**
     * Update session details of a given session context key to map the current session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param tenantId          Tenant id.
     * @param idpId             Federated IdP id.
     * @throws UserSessionException Error while storing session details.
     */
    public void updateFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId,
            int idpId) throws UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO()
                .updateFederatedAuthSessionInfo(sessionContextKey, authHistory, tenantId, idpId);
    }

    /**
     * Check whether there is already existing federated auth session with the given session index.
     *
     * @param idpSessionIndex IDP session index.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    public boolean hasExistingFederatedAuthSession(String idpSessionIndex) throws UserSessionException {

        return UserSessionDAOFactory.getUserSessionDAO().hasExistingFederatedAuthSession(idpSessionIndex);
    }

    /**
     * Check whether there is already existing federated auth session with the given session index and the tenant id.
     *
     * @param idpSessionIndex IDP session index.
     * @param tenantId        Tenant id.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    public boolean isExistingFederatedAuthSessionAvailable(String idpSessionIndex, int tenantId)
            throws UserSessionException {

        return UserSessionDAOFactory.getUserSessionDAO()
                .isExistingFederatedAuthSessionAvailable(idpSessionIndex, tenantId);
    }

    /**
     * Check whether there is already existing federated auth session with the given session index and the idp id.
     *
     * @param idpSessionIndex IDP session index.
     * @param idpId           Federated IDP ID.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    public boolean hasExistingFederatedAuthSessionWithIdpId(String idpSessionIndex, int idpId)
            throws UserSessionException {

        return UserSessionDAOFactory.getUserSessionDAO()
                .hasExistingFederatedAuthSessionWithIdpId(idpSessionIndex, idpId);
    }

    /**
     * Check whether there is already existing federated auth session with the given session index, tenant id and
     * idp id.
     *
     * @param idpSessionIndex IDP session index.
     * @param tenantId        Tenant id.
     * @param idpId           Federated IDP id.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    public boolean hasExistingFederatedAuthSession(String idpSessionIndex, int tenantId, int idpId)
            throws UserSessionException {

        return UserSessionDAOFactory.getUserSessionDAO()
                .hasExistingFederatedAuthSession(idpSessionIndex, tenantId, idpId);
    }

    /**
     * Remove federated authentication session details of a given session context key.
     *
     * @param sessionContextKey Session Context Key.
     * @throws UserSessionException Error while deleting session details of a given session id.
     */
    public void removeFederatedAuthSessionInfo(String sessionContextKey) throws UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO().removeFederatedAuthSessionInfo(sessionContextKey);
    }

    /**
     * Remove federated authentication session details of a given session context key.
     *
     * @param sessionContextKey     Session Context Key.
     * @param idpId                 ID of the federated IdP.
     * @throws UserSessionException Error while deleting session details of a given session id.
     */
    public void removeFederatedAuthSessionInfo(String sessionContextKey, int idpId) throws UserSessionException {

        UserSessionDAOFactory.getUserSessionDAO().removeFederatedAuthSessionInfo(sessionContextKey, idpId);
    }

    /**
     * Method to check whether the user id is available in the IDN_AUTH_USER table.
     *
     * @param userId    Id of the user
     * @return the boolean decision
     * @throws UserSessionException if an error occurs when retrieving the mapping from the database
     */
    public boolean isExistingUser(String userId) throws UserSessionException {

        boolean isExisting = false;
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_SELECT_INFO_OF_USER_ID)) {
                preparedStatement.setString(1, userId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        isExisting = true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving information of user id: " + userId, e);
        }
        return isExisting;
    }

    /**
     * Counts the number of active sessions of the given tenant domain. For a session to be active, the last access
     * time of the session should not be earlier than the session timeout time.
     *
     * @param tenantDomain tenant domain
     * @return number of active sessions of the given tenant domain
     * @throws UserSessionException if something goes wrong
     */
    public int getActiveSessionCount(String tenantDomain) throws UserSessionException {

        return UserSessionDAOFactory.getUserSessionDAO().getActiveSessionCount(tenantDomain);
    }

    /**
     * Returns the user id of the federated user.
     *
     * @param subjectIdentifier - Subject Identifier of the federated user.
     * @param tenantId          - Id of the service provider's tenant domain.
     * @param idPId             - Id of the identity provider.
     * @return userId - User Id of the federated user.
     * @throws UserSessionException
     */
    public String getFederatedUserId(String subjectIdentifier, int tenantId, int idPId)
            throws UserSessionException {

        // When federated user is stored, the userDomain is added as "FEDERATED" to the store.
        return getUserId(subjectIdentifier, tenantId, FEDERATED_USER_DOMAIN, idPId);
    }

    /**
     * Method to retrieve user and associated IDP available in the IDN_AUTH_USER table.
     *
     * @param userId Id of the authenticated user
     * @return the user and associated IDP
     * @throws UserSessionException if an error occurs when retrieving the mapping from the database
     */
    public AuthenticatedUser getUser(String userId) throws UserSessionException {

        if (StringUtils.isBlank(userId)) {
            throw new UserSessionException("Invalid userId: userId cannot be null or empty.");
        }

        AuthenticatedUser user = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_SELECT_USER_FROM_USER_ID)) {
                preparedStatement.setString(1, userId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        user = new AuthenticatedUser();
                        user.setUserName(resultSet.getString(1));
                        user.setTenantDomain(IdentityTenantUtil.getTenantDomain(resultSet.getInt(2)));
                        user.setUserStoreDomain(resultSet.getString(3));
                        user.setFederatedIdPName(resultSet.getString(4));
                    }
                }
            }
            return user;
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving information of user id: " + userId, e);
        }
    }
}
