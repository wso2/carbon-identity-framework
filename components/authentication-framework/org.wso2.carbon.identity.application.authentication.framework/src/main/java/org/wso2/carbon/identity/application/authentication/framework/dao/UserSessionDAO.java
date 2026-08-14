/*
 * Copyright (c) 2019-2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.dao;

import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt
        .SessionManagementServerException;
import org.wso2.carbon.identity.application.authentication.framework.model.FederatedUserSession;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.authentication.framework.store.SQLQueries;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.JdbcUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Perform operations for {@link UserSession}.
 */
public interface UserSessionDAO {

    /**
     * Returns the name of the store this DAO belongs to, matched case-insensitively. A DAO registered as an
     * OSGi service must override this to be selectable.
     *
     * @return the store name, or {@code null} if this DAO is not selectable by name.
     */
    default String getStoreName() {

        return null;
    }

    /**
     * Method to retrieve session information for a given session id.
     *
     * @param sessionId Id of the session.
     * @return User session.
     * @throws SessionManagementServerException
     */
    default UserSession getSession(String sessionId) throws SessionManagementServerException {

        return null;
    }

    /**
     * Method to retrieve session information for a given user and session id.
     *
     * @param userId Id of the user.
     * @param sessionId Id of the session.
     * @return User session.
     * @throws SessionManagementServerException
     */
    default Optional<UserSession> getSession(String userId, String sessionId) throws SessionManagementServerException {

        return Optional.empty();
    }

    /**
     * Method to search active sessions on the system.
     *
     * @param tenantId  Context tenant ID.
     * @param filter    Filter expression nodes.
     * @param limit     Limit.
     * @param sortOrder Order direction (ASC, DESC).
     * @return The list of sessions found.
     * @throws UserSessionException if an error occurs when retrieving the sessions from the database.
     */
    default List<UserSession> getSessions(int tenantId, List<ExpressionNode> filter, Integer limit, String sortOrder)
            throws UserSessionException {

        return Collections.emptyList();
    }

    /**
     * Get federated user session details mapped for federated IDP sessionId.
     *
     * @param fedIdpSessionId sid claim in the logout token of the federated idp.
     * @return A FederatedUserSession containing federated authentication session details.
     * @throws SessionManagementServerException
     */
    default FederatedUserSession getFederatedAuthSessionDetails(String fedIdpSessionId)
            throws SessionManagementServerException {

        FederatedUserSession federatedUserSession;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        try {
            federatedUserSession = jdbcTemplate
                    .fetchSingleRecord(SQLQueries.SQL_GET_FEDERATED_AUTH_SESSION_INFO_BY_SESSION_ID,
                            (resultSet, rowNumber) -> new FederatedUserSession(
                                    resultSet.getString(SessionMgtConstants.FEDERATED_IDP_SESSION_ID),
                                    resultSet.getString(SessionMgtConstants.FEDERATED_SESSION_ID),
                                    resultSet.getString(SessionMgtConstants.FEDERATED_IDP_NAME),
                                    resultSet.getString(SessionMgtConstants.FEDERATED_AUTHENTICATOR_ID),
                                    resultSet.getString(SessionMgtConstants.FEDERATED_PROTOCOL_TYPE)),
                            preparedStatement -> preparedStatement.setString(1, fedIdpSessionId));
            return federatedUserSession;
        } catch (DataAccessException e) {
            throw new SessionManagementServerException(
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_FED_USER_SESSION,
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_FED_USER_SESSION.getDescription(), e);
        }
    }

    /**
     * Get all the federated user session details mapped for federated IDP sessionId.
     *
     * @param fedIdpSessionId sid claim in the logout token of the federated idp.
     * @return List of FederatedUserSession containing federated authentication session details.
     * @throws SessionManagementServerException
     */
    default List<FederatedUserSession> getFederatedAuthSessionsDetails(String fedIdpSessionId)
            throws SessionManagementServerException {

        List<FederatedUserSession> federatedUserSession;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        try {
            federatedUserSession = jdbcTemplate
                    .executeQuery(SQLQueries.SQL_GET_FEDERATED_AUTH_SESSION_INFO_BY_SESSION_ID,
                    (resultSet, rowNumber) -> new FederatedUserSession(
                            resultSet.getString(SessionMgtConstants.FEDERATED_IDP_SESSION_ID),
                            resultSet.getString(SessionMgtConstants.FEDERATED_SESSION_ID),
                            resultSet.getString(SessionMgtConstants.FEDERATED_IDP_NAME),
                            resultSet.getString(SessionMgtConstants.FEDERATED_AUTHENTICATOR_ID),
                            resultSet.getString(SessionMgtConstants.FEDERATED_PROTOCOL_TYPE)),
                    preparedStatement -> preparedStatement.setString(1, fedIdpSessionId));
            return federatedUserSession;
        } catch (DataAccessException e) {
            throw new SessionManagementServerException(
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_FED_USER_SESSION,
                    SessionMgtConstants.ErrorMessages.ERROR_CODE_UNABLE_TO_GET_FED_USER_SESSION.getDescription(), e);
        }
    }

    /**
     * Method to store user id and session id mapping in the database table IDN_AUTH_USER_SESSION_MAPPING.
     *
     * @param userId    Id of the user
     * @param sessionId Id of the authenticated session
     * @throws UserSessionException if an error occurs when storing the mapping in the database
     */
    default void storeUserSessionData(String userId, String sessionId) throws UserSessionException {

        throw new UnsupportedOperationException(
                "storeUserSessionData is not supported by this user session DAO.");
    }

    /**
     * Method to check whether the user id and session id mapping is already exists in the database.
     *
     * @param userId    Id of the user
     * @param sessionId Id of the authenticated session
     * @return the boolean decision
     * @throws UserSessionException if an error occurs when retrieving the mapping from the database
     */
    default boolean isExistingMapping(String userId, String sessionId) throws UserSessionException {

        throw new UnsupportedOperationException(
                "isExistingMapping is not supported by this user session DAO.");
    }

    /**
     * Method to get session Id list of a given user Id.
     *
     * @param userId id of the user
     * @return the list of session ids
     * @throws UserSessionException if an error occurs when retrieving the session id list from the database
     */
    default List<String> getSessionId(String userId) throws UserSessionException {

        throw new UnsupportedOperationException(
                "getSessionId is not supported by this user session DAO.");
    }

    /**
     * Removes all the expired session records from relevant tables.
     */
    default void removeExpiredSessionRecords() {

        throw new UnsupportedOperationException(
                "removeExpiredSessionRecords is not supported by this user session DAO.");
    }

    /**
     * Remove the session information records of a given set of session IDs from the relevant tables.
     *
     * @param sessionIdList list of terminated session IDs
     */
    default void removeTerminatedSessionRecords(List<String> sessionIdList) {

        throw new UnsupportedOperationException(
                "removeTerminatedSessionRecords is not supported by this user session DAO.");
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
    default void storeAppSessionData(String sessionId, String subject, int appID, String inboundAuth) throws
            DataAccessException {

        throw new UnsupportedOperationException(
                "storeAppSessionData is not supported by this user session DAO.");
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
    default void storeAppSessionDataIfNotExist(String sessionId, String subject, int appID, String inboundAuth) throws
            DataAccessException {

        throw new UnsupportedOperationException(
                "storeAppSessionDataIfNotExist is not supported by this user session DAO.");
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
    default boolean isExistingAppSession(String sessionId, String subject, int appID, String inboundAuth) throws
            UserSessionException {

        throw new UnsupportedOperationException(
                "isExistingAppSession is not supported by this user session DAO.");
    }

    /**
     * Method to store session meta data as a batch.
     *
     * @param sessionId id of the authenticated session
     * @param metaData  map of metadata type and value of the session
     * @throws UserSessionException while storing session meta data
     */
    default void storeSessionMetaData(String sessionId, Map<String, String> metaData) throws UserSessionException {

        throw new UnsupportedOperationException(
                "storeSessionMetaData is not supported by this user session DAO.");
    }

    /**
     * Update session meta data.
     *
     * @param sessionId    id of the authenticated session
     * @param propertyType type of the meta data
     * @param value        value of the meta data
     * @throws UserSessionException if the meta data update in the database fails.
     */
    default void updateSessionMetaData(String sessionId, String propertyType, String value) throws
            UserSessionException {

        throw new UnsupportedOperationException(
                "updateSessionMetaData is not supported by this user session DAO.");
    }

    /**
     * Method to get session Id list of a given user.
     *
     * @param user  user object
     * @param idpId id of the user's idp
     * @return the list of session ids
     * @throws UserSessionException if an error occurs when retrieving the session id list from the database
     */
    default List<String> getSessionId(User user, int idpId) throws UserSessionException {

        throw new UnsupportedOperationException(
                "getSessionId is not supported by this user session DAO.");
    }

    /**
     * Method to check whether a given user already has a mapping with a given session id.
     *
     * @param user      user object
     * @param sessionId id of the authenticated session
     * @return the boolean decision
     * @throws UserSessionException if an error occurs when retrieving the mapping from the database
     */
    default boolean isExistingMapping(User user, int idpId, String sessionId) throws UserSessionException {

        throw new UnsupportedOperationException(
                "isExistingMapping is not supported by this user session DAO.");
    }

    /**
     * Store session details of a given session context key to map the session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @throws UserSessionException Error while storing session details.
     */
    default void storeFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory)
            throws UserSessionException {

        throw new UnsupportedOperationException(
                "storeFederatedAuthSessionInfo is not supported by this user session DAO.");
    }

    /**
     * Store session details with the given session context key for the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param tenantId          Tenant id.
     * @throws UserSessionException Error while storing session details.
     */
    default void storeFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId)
            throws UserSessionException {

        throw new UnsupportedOperationException(
                "storeFederatedAuthSessionInfo is not supported by this user session DAO.");
    }

    /**
     * Store session details with the given session context key for the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @param idpId             Federated IDP ID.
     * @throws UserSessionException Error while storing session details.
     */
    default void storeFederatedAuthSessionInfoWithIdpId(String sessionContextKey, AuthHistory authHistory, int idpId)
            throws UserSessionException {

        throw new UnsupportedOperationException(
                "storeFederatedAuthSessionInfoWithIdpId is not supported by this user session DAO.");
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
    default void storeFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId,
                                              int idpId) throws UserSessionException {

        throw new UnsupportedOperationException(
                "storeFederatedAuthSessionInfo is not supported by this user session DAO.");
    }

    /**
     * Update session details of a given session context key to map the current session context key with
     * the federated IdP's session ID.
     *
     * @param sessionContextKey Session Context Key.
     * @param authHistory       History of the authentication flow.
     * @throws UserSessionException Error while storing session details.
     */
    default void updateFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory) throws
            UserSessionException {

        throw new UnsupportedOperationException(
                "updateFederatedAuthSessionInfo is not supported by this user session DAO.");
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
    default void updateFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId) throws
            UserSessionException {

        throw new UnsupportedOperationException(
                "updateFederatedAuthSessionInfo is not supported by this user session DAO.");
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
    default void updateFederatedAuthSessionInfoWithIdpId(String sessionContextKey, AuthHistory authHistory, int idpId)
            throws UserSessionException {

        throw new UnsupportedOperationException(
                "updateFederatedAuthSessionInfoWithIdpId is not supported by this user session DAO.");
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
    default void updateFederatedAuthSessionInfo(String sessionContextKey, AuthHistory authHistory, int tenantId,
            int idpId) throws UserSessionException {

        throw new UnsupportedOperationException(
                "updateFederatedAuthSessionInfo is not supported by this user session DAO.");
    }

    /**
     * Check whether there is already existing federated auth session with the given session index.
     *
     * @param idpSessionIndex IDP session index.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    default boolean hasExistingFederatedAuthSession(String idpSessionIndex) throws UserSessionException {

        throw new UnsupportedOperationException(
                "hasExistingFederatedAuthSession is not supported by this user session DAO.");
    }

    /**
     * Check whether there is already existing federated auth session with the given session index and the tenant id.
     *
     * @param idpSessionIndex IDP session index.
     * @param tenantId        Tenant id.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    default boolean isExistingFederatedAuthSessionAvailable(String idpSessionIndex, int tenantId)
            throws UserSessionException {

        throw new UnsupportedOperationException(
                "isExistingFederatedAuthSessionAvailable is not supported by this user session DAO.");
    }

    /**
     * Check whether there is already existing federated auth session with the given session index and the idp id.
     *
     * @param idpSessionIndex IDP session index.
     * @param idpId           Federated IDP ID.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    default boolean hasExistingFederatedAuthSessionWithIdpId(String idpSessionIndex, int idpId)
            throws UserSessionException {

        throw new UnsupportedOperationException(
                "hasExistingFederatedAuthSessionWithIdpId is not supported by this user session DAO.");
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
    default boolean hasExistingFederatedAuthSession(String idpSessionIndex, int tenantId, int idpId)
            throws UserSessionException {

        throw new UnsupportedOperationException(
                "hasExistingFederatedAuthSession is not supported by this user session DAO.");
    }

    /**
     * Remove federated authentication session details of a given session context key.
     *
     * @param sessionContextKey Session Context Key.
     * @throws UserSessionException Error while deleting session details of a given session id.
     */
    default void removeFederatedAuthSessionInfo(String sessionContextKey) throws UserSessionException {

        throw new UnsupportedOperationException(
                "removeFederatedAuthSessionInfo is not supported by this user session DAO.");
    }

    /**
     * Remove federated authentication session details of a given session context key.
     *
     * @param sessionContextKey     Session Context Key.
     * @param idpId                 ID of the federated IdP.
     * @throws UserSessionException Error while deleting session details of a given session id.
     */
    default void removeFederatedAuthSessionInfo(String sessionContextKey, int idpId) throws UserSessionException {

        throw new UnsupportedOperationException(
                "removeFederatedAuthSessionInfo is not supported by this user session DAO.");
    }

    /**
     * Counts the number of active sessions of the given tenant domain.
     *
     * @param tenantDomain tenant domain
     * @return number of active sessions of the given tenant domain
     * @throws UserSessionException if something goes wrong
     */
    default int getActiveSessionCount(String tenantDomain) throws UserSessionException {

        throw new UnsupportedOperationException(
                "getActiveSessionCount is not supported by this user session DAO.");
    }
}
