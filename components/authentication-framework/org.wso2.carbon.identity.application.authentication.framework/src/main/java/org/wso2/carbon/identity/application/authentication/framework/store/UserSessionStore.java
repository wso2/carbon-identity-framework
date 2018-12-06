/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.JdbcUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class to store and retrieve user related data.
 */

public class UserSessionStore {

    private static final Log log = LogFactory.getLog(UserSessionStore.class);
    private static final String FEDERATED_USER_DOMAIN = "FEDERATED";
    private static UserSessionStore instance = new UserSessionStore();

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
     * @throws DataAccessException if an error occurs when storing the authenticated user details to the database
     */
    public void storeUserData(String userId, String userName, int tenantId, String userDomain, int idPId)
            throws UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(SQLQueries.SQL_INSERT_USER_STORE_OPERATION, (preparedStatement -> {
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, userName);
                preparedStatement.setInt(3, tenantId);
                preparedStatement.setString(4, (userDomain == null) ? FEDERATED_USER_DOMAIN : userDomain);
                preparedStatement.setInt(5, idPId);
            }));
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while storing authenticated user details to the database table " +
                    "IDN_AUTH_USER_STORE of user: " + userName + ", Tenant Id: " + tenantId + ", User domain: " +
                    userDomain + ", Identity provider id: " + idPId, e);
        }
    }

    /**
     * Method to get the unique Id of a user from the database.
     *
     * @param userName   Name of the authenticated user
     * @param tenantId   Id of the tenant domain
     * @param userDomain Name of the user Store domain
     * @param idPId      Identity Provider id
     * @return the user id of the user
     * @throws DataAccessException if an error occurs when retrieving the user id of the user from the database
     */
    public String getUserId(String userName, int tenantId, String userDomain, int idPId)
            throws UserSessionException {

        String userId;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            userId = jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_SELECT_USER_ID, ((resultSet, rowNumber) ->
                    resultSet.getString(1)), preparedStatement -> {
                preparedStatement.setString(1, userName);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, (userDomain == null) ? FEDERATED_USER_DOMAIN : userDomain);
                preparedStatement.setInt(4, idPId);
            });

        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving User Id of the user: " + userName + ", Tenant Id: "
                    + tenantId + ", User domain: " + userDomain + ", Identity provider id: " + idPId, e);
        }
        return userId;
    }

    /**
     * Method to get the unique Id of a user from the database.
     *
     * @param userName   Name of the authenticated user
     * @param tenantId   Id of the tenant domain
     * @param userDomain Name of the user Store domain
     * @return the user id of the user
     * @throws DataAccessException if an error occurs when retrieving the user id of the user from the database
     */

    public String getUserId(String userName, int tenantId, String userDomain)
            throws UserSessionException {

        String userId;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            userId = jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_SELECT_USER_ID, ((resultSet, rowNumber) ->
                    resultSet.getString(1)), preparedStatement -> {
                preparedStatement.setString(1, userName);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, (userDomain == null) ? FEDERATED_USER_DOMAIN : userDomain);
            });

        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving User Id of the user: " + userName + ", Tenant Id: "
                    + tenantId, e);
        }
        return userId;
    }

    /**
     * Method to identity providers id from the IDP table.
     *
     * @param idPName Name of the identity provider
     * @return Id of the identity provider
     * @throws DataAccessException If an error occurs when retrieving the identity provider id list from the database
     */
    public int getIdPId(String idPName) throws UserSessionException {

        int idPId = -1;
        if (idPName.equals("LOCAL")) {
            return idPId;
        }
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            idPId = jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_SELECT_IDP_ID_OF_IDP, (resultSet, rowNumber) ->
                    resultSet.getInt(1), preparedStatement -> preparedStatement.setString(1, idPName));
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving the IdP id of: " + idPName, e);
        }
        return idPId;
    }

    /**
     * Method to store user id and session id mapping in the database table IDN_AUTH_USER_SESSION_STORE.
     *
     * @param userId    Id of the user
     * @param sessionId Id of the authenticated session
     * @throws DataAccessException if an error occurs when storing the mapping in the database
     */
    public void storeUserSessionData(String userId, String sessionId) throws UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(SQLQueries.SQL_INSERT_USER_SESSION_STORE_OPERATION, preparedStatement -> {
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, sessionId);
            });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while storing mapping between user Id: " + userId +
                    " and session Id: " + sessionId, e);
        }
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

        final boolean[] isExisting = {false};
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_SELECT_USER_SESSION_MAP, (resultSet, rowNumber) ->
                            isExisting[0] = true,
                    preparedStatement -> {
                        preparedStatement.setString(1, userId);
                        preparedStatement.setString(2, sessionId);
                    });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving existing mapping between user Id: " + userId
                    + " and session Id: " + sessionId, e);
        }
        return isExisting[0];
    }

    /**
     * Method to store app session data.
     *
     * @param sessionId   Id of the authenticated session
     * @param subject     Username in application
     * @param appID       Id of the application
     * @param appTenantID Tenant Id of application
     * @param inboundAuth Protocol used in app
     * @throws UserSessionException if an error occurs when storing the authenticated user details to the database
     */
    public void storeAppSessionData(String sessionId, String subject, int appID, int appTenantID,
                                    String inboundAuth) throws DataAccessException {

        if (appID != 0) {
            JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
            try {
                jdbcTemplate.executeUpdate(SQLQueries.SQL_INSERT_APP_SESSION_STORE_OPERATION, preparedStatement -> {
                    preparedStatement.setString(1, sessionId);
                    preparedStatement.setString(2, subject);
                    preparedStatement.setInt(3, appID);
                    preparedStatement.setInt(4, appTenantID);
                    preparedStatement.setString(5, inboundAuth);
                });
            } catch (DataAccessException e) {
                throw new DataAccessException("Error while storing application session details to the database table ", e);
            }
        }
    }

    /**
     * Method to get app id from SP_APP table.
     *
     * @param applicationName Application Name
     * @param appTenantID     App tenant id
     * @return
     * @throws UserSessionException if an error occurs when retrieving app id
     */
    public int getAppId(String applicationName, int appTenantID) throws UserSessionException {

        final int[] applicationId = {0};
        PreparedStatement getAppIDPrepStmt = null;
        ResultSet appIdResult = null;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_SELECT_APP_ID_OF_APP,
                    ((resultSet, rowNumber) -> applicationId[0] = resultSet.getInt(1)),
                    preparedStatement -> {
                        preparedStatement.setString(1, applicationName);
                        preparedStatement.setInt(2, appTenantID);
                    });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving the app id of: " + applicationName + ", " +
                    "tenant id" + appTenantID, e);
        }
        return applicationId[0];
    }

    /**
     * Method to get app id from SP_APP table.
     *
     * @param userId Id of the user
     * @return
     * @throws UserSessionException if an error occurs when retrieving app id
     */
    public List<String> getSessionId(String userId) throws UserSessionException {

        List<String> sessionIdList = new ArrayList<>();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeQuery(SQLQueries.SQL_SELECT_SESSION_ID_OF_USER_ID,
                    ((resultSet, rowNumber) -> sessionIdList.add( resultSet.getString(1))),
                    preparedStatement -> {
                        preparedStatement.setString(1, userId);
                    });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving session Id of user Id: " + userId, e);
        }
        return sessionIdList;
    }

    /**
     * Method to check whether the particular app session is already exists in the database.
     *
     * @param sessionId   Id of the authenticated session
     * @param subject     User name of app
     * @param appID       Id of application
     * @param appTenantID Tenant Id of application
     * @param inboundAuth Protocol used in app
     * @return
     * @throws UserSessionException while retrieving existing session data
     */
    public boolean isExistingAppSession(String sessionId, String subject, int appID, int appTenantID,
                                        String inboundAuth) throws UserSessionException {

        final boolean[] isExisting = {false};
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeQuery(SQLQueries.SQL_SELECT_APP_SESSION, (resultSet, rowNumber) -> isExisting[0] = true,
                    preparedStatement -> {
                        preparedStatement.setString(1, sessionId);
                        preparedStatement.setString(2, subject);
                        preparedStatement.setInt(3, appID);
                        preparedStatement.setInt(4, appTenantID);
                        preparedStatement.setString(5, inboundAuth);
                    });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving existing session data: " +
                    " of session Id: " + sessionId + ", subject" + subject + ",app Id:"
                    + appID + ",app tenant id" + appTenantID + ",protocol:" + inboundAuth, e);
        }
        return isExisting[0];
    }

    /**
     * Method to store session meta data.
     *
     * @param sessionId Id of the authenticated session
     * @throws UserSessionException while storing session meta data
     */
    public void storeSessionMetaData(String sessionId, String propertyType, String value) throws UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(SQLQueries.SQL_INSERT_SESSION_META_DATA, (preparedStatement -> {
                preparedStatement.setString(1, sessionId);
                preparedStatement.setString(2, propertyType);
                preparedStatement.setString(3, value);
            }));
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while storing session meta data to the database table " +
                    "IDN_AUTH_SESSION_META_DATA of session: " + sessionId, e);
        }
    }

    /**
     * Method to update last access time.
     *
     * @param sessionId      Id of the authenticated session
     * @param lastAccessTime Last time user accessed
     * @throws UserSessionException while updating last access time to database
     */
    public void updateLastAccessTime(String sessionId, String lastAccessTime) throws UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(SQLQueries.UPDATE_LAST_ACCESS_TIME, preparedStatement -> {
                preparedStatement.setString(1, lastAccessTime);
                preparedStatement.setString(2, sessionId);
                preparedStatement.setString(3, "Last Access Time");
            });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while updating last access time to the database table " +
                    "IDN_AUTH_SESSION_META_DATA of: " + sessionId, e);
        }
    }

    /**
     * Method to return the user Ids of the users in a given user store from the database.
     *
     * @param userDomain Name of the user Store domain
     * @param tenantId   Id of the tenant domain
     * @return the list of user Ids of users stored in the given user store
     * @throws UserSessionException if an error occurs when retrieving the user id list from the database
     */
    public List<String> getUserIdsOfUserStore(String userDomain, int tenantId) throws UserSessionException {

        List<String> userIds = new ArrayList<>();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeQuery(SQLQueries.SQL_SELECT_SESSION_ID_OF_USER_ID,
                    ((resultSet, rowNumber) -> userIds.add( resultSet.getString(1))),
                    preparedStatement -> {
                        preparedStatement.setString(1, userDomain);
                        preparedStatement.setInt(2, tenantId);

                    });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving user Ids stored in the user domain: " + userDomain
                    + ", Tenant Id: " + tenantId, e);        }
        return userIds;
    }

    /**
     * Method used to remove the expired sessions from the database table IDN_AUTH_USER_SESSION_STORE,
     * IDN_AUTH_SESSION_STORE & IDN_AUTH_SESSION_META_DATA.
     */
    public void removeExpiredSessionRecords() {

        long cleanupLimitNano = FrameworkUtils.getCurrentStandardNano() -
                TimeUnit.MINUTES.toNanos(IdentityUtil.getOperationCleanUpTimeout());
        List<String> terminatedAuthSessionIds = new ArrayList<>();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        try {
            jdbcTemplate.executeQuery(SQLQueries.SQL_SELECT_TERMINATED_SESSION_IDS, ((resultSet, rowNumber) ->
                    terminatedAuthSessionIds.add(resultSet.getString(1))), preparedStatement ->
                    preparedStatement.setLong(1, cleanupLimitNano));

            for (String terminatedSessionId : terminatedAuthSessionIds) {

                jdbcTemplate.executeBatchInsert(SQLQueries.SQL_DELETE_TERMINATED_USER_SESSION_MAPPING_DATA,
                        preparedStatement -> preparedStatement.setString(1, terminatedSessionId), null);

                jdbcTemplate.executeBatchInsert(SQLQueries.SQL_DELETE_TERMINATED_APP_SESSION_DATA, preparedStatement ->
                        preparedStatement.setString(1, terminatedSessionId), null);

                jdbcTemplate.executeBatchInsert(SQLQueries.SQL_DELETE_TERMINATED_SESSION_META_DATA, preparedStatement ->
                        preparedStatement.setString(1, terminatedSessionId), null);
            }
        } catch (DataAccessException e) {
            log.error("Error while removing the terminated sessions from the tables", e);
        }
    }

    }
