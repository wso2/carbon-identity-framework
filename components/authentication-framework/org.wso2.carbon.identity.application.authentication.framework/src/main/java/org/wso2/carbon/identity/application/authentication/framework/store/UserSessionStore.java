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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.exception.DuplicatedAuthUserException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.JdbcUtils;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to store and retrieve user related data.
 */
public class UserSessionStore {

    private static final Log log = LogFactory.getLog(UserSessionStore.class);

    private static UserSessionStore instance = new UserSessionStore();
    private static final String FEDERATED_USER_DOMAIN = "FEDERATED";
    private static final String DELETE_CHUNK_SIZE_PROPERTY = "JDBCPersistenceManager.SessionDataPersist" +
            ".UserSessionMapping.DeleteChunkSize";
    private static final String IDN_AUTH_USER_SESSION_MAPPING_TABLE = "IDN_AUTH_USER_SESSION_MAPPING";
    private static final String IDN_AUTH_SESSION_APP_INFO_TABLE = "IDN_AUTH_SESSION_APP_INFO_TABLE";
    private static final String IDN_AUTH_SESSION_META_DATA_TABLE = "IDN_AUTH_SESSION_META_DATA";

    private int deleteChunkSize = 10000;

    private UserSessionStore() {

        String deleteChunkSizeString = IdentityUtil.getProperty(DELETE_CHUNK_SIZE_PROPERTY);
        if (StringUtils.isNotBlank(deleteChunkSizeString)) {
            deleteChunkSize = Integer.parseInt(deleteChunkSizeString);
        }
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

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try ( PreparedStatement preparedStatement = connection
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
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                            .prepareStatement(SQLQueries.SQL_SELECT_USER_ID)) {
                preparedStatement.setString(1, userName);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, (userDomain == null) ? FEDERATED_USER_DOMAIN :
                        userDomain.toUpperCase());
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
     *
     * @param userName   Name of the authenticated user
     * @param tenantId   Id of the tenant domain
     * @param userDomain Name of the user Store domain
     * @return the user id of the user
     * @throws UserSessionException if an error occurs when retrieving the user id of the user from the database
     */
    public String getUserId(String userName, int tenantId, String userDomain) throws UserSessionException {

        String userId = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                            .prepareStatement(SQLQueries.SQL_SELECT_USER_IDS_OF_USER)) {
                preparedStatement.setString(1, userName);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, (userDomain == null) ? FEDERATED_USER_DOMAIN :
                        userDomain.toUpperCase());
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
     *
     * @param userDomain name of the user Store domain
     * @param tenantId   id of the tenant domain
     * @return the list of user Ids of users stored in the given user store
     * @throws UserSessionException if an error occurs when retrieving the user id list from the database
     */
    public List<String> getUserIdsOfUserStore(String userDomain, int tenantId) throws UserSessionException {

        List<String> userIds = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
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
     */
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
     * Method to store user id and session id mapping in the database table IDN_AUTH_USER_SESSION_STORE.
     *
     * @param userId    Id of the user
     * @param sessionId Id of the authenticated session
     * @throws UserSessionException if an error occurs when storing the mapping in the database
     */
    public void storeUserSessionData(String userId, String sessionId) throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            try(PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_INSERT_USER_SESSION_STORE_OPERATION)) {
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, sessionId);
                preparedStatement.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new UserSessionException("Error while storing mapping between user Id: " + userId +
                        " and session Id: " + sessionId, e1);
            }
        } catch (SQLException e) {
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

        Boolean isExisting = false;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_SELECT_USER_SESSION_MAP)) {
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, sessionId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        isExisting = true;
                    }
                }
            } catch (SQLException e1) {
                throw new UserSessionException("Error while retrieving existing mapping between user Id: " + userId
                        + " and session Id: " + sessionId, e1);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving existing mapping between user Id: " + userId
                    + " and session Id: " + sessionId, e);
        }
        return isExisting;
    }

    /**
     * Method to get session Id list of a given user Id.
     *
     * @param userId id of the user
     * @return the list of session ids
     * @throws UserSessionException if an error occurs when retrieving the session id list from the database
     */
    public List<String> getSessionId(String userId) throws UserSessionException {

        List<String> sessionIdList = new ArrayList<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
             try (PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_SELECT_SESSION_ID_OF_USER_ID)) {
                 preparedStatement.setString(1, userId);
                 try (ResultSet resultSet = preparedStatement.executeQuery()) {
                     while (resultSet.next()) {
                         sessionIdList.add(resultSet.getString(1));
                     }
                 }
             } catch (SQLException e1) {
                 throw new UserSessionException("Error while retrieving session Id of user Id: " + userId, e1);
             }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving session Id of user Id: " + userId, e);
        }
        return sessionIdList;
    }

    /**
     * Removes all the expired session records from relevant tables.
     */
    public void removeExpiredSessionRecords() {

        if (log.isDebugEnabled()) {
            log.debug("Removing information of expired and deleted sessions.");
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            Set<String> terminatedAuthSessionIds = getSessionsTerminated(connection);
            String[] sessionsToRemove = new String[terminatedAuthSessionIds.size()];
            terminatedAuthSessionIds.toArray(sessionsToRemove);

            if (!terminatedAuthSessionIds.isEmpty()) {

                if (log.isDebugEnabled()) {
                    log.debug(terminatedAuthSessionIds.size() + " number of sessions should be removed from the " +
                            "database. Removing in " + deleteChunkSize + " size batches.");
                }

                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_USER_SESSION_MAPPING_TABLE,
                        SQLQueries.SQL_DELETE_TERMINATED_SESSION_DATA);
                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_SESSION_APP_INFO_TABLE,
                        SQLQueries.SQL_DELETE_IDN_AUTH_SESSION_APP_INFO);
                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_SESSION_META_DATA_TABLE,
                        SQLQueries.SQL_DELETE_IDN_AUTH_SESSION_META_DATA);

                IdentityDatabaseUtil.commitTransaction(connection);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No expired sessions found to remove.");
                }
            }
        } catch (SQLException e) {
            log.error("Error while removing expired session information from the database.", e);
        }
    }

    /**
     * Remove the session information records of a given set of session IDs from the relevant tables.
     *
     * @param sessionIdList list of terminated session IDs
     */
    public void removeTerminatedSessionRecords(List<String> sessionIdList) {

        String[] sessionsToRemove = sessionIdList.toArray(new String[0]);

        if (log.isDebugEnabled()) {
            log.debug("Removing meta information of the deleted sessions.");
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {

            deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_USER_SESSION_MAPPING_TABLE,
                    SQLQueries.SQL_DELETE_TERMINATED_SESSION_DATA);
            deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_SESSION_APP_INFO_TABLE,
                    SQLQueries.SQL_DELETE_IDN_AUTH_SESSION_APP_INFO);
            deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_SESSION_META_DATA_TABLE,
                    SQLQueries.SQL_DELETE_IDN_AUTH_SESSION_META_DATA);
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            log.error("Error while removing the terminated session information from the database.", e);
        }
    }

    private Set<String> getSessionsTerminated(Connection connection) throws SQLException {

        Set<String> terminatedSessionIds = new HashSet<>();

        /**
         * Retrieve only sessions which have an expiry time less than the current time.
         * As the session cleanup task deletes only entries matching the same condition, in case sessions that are
         * being marked as deleted are also retrieved that might load a huge amount of entries to the memory all the
         * time. Yet those entries will be removed from the IDN_AUTH_USER_SESSION_MAPPING_TABLE table on the first
         * execution, and there after every time the loop will be executed and the table will be scanned for a non
         * existing entry.
         */
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQLQueries
                .SQL_SELECT_TERMINATED_SESSION_IDS)) {
            preparedStatement.setLong(1, FrameworkUtils.getCurrentStandardNano());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    terminatedSessionIds.add(resultSet.getString(1));
                }
            }
        }

        return terminatedSessionIds;
    }

    /**
     * This method is used to chunk-wise deletion of records of a given table.
     *
     * @param sessionsToRemove array of session ids which should be removed
     * @param connection       db connection
     * @param tableName        table name from which the records are removed
     * @param deleteQuery      delete query for the relevant table
     * @throws SQLException if the DB execution fails
     */
    private void deleteSessionDataFromTable(String[] sessionsToRemove, Connection connection, String tableName,
                                            String deleteQuery) throws SQLException {

        int totalSessionsToRemove = sessionsToRemove.length;
        int iterations = (totalSessionsToRemove / deleteChunkSize) + 1;
        int startCount = 0;
        for (int i = 0; i < iterations; i++) {

            int endCount = (i + 1) * deleteChunkSize;
            if (totalSessionsToRemove < endCount) {
                endCount = totalSessionsToRemove;
            }

            try (PreparedStatement preparedStatementForDelete = connection.prepareStatement(deleteQuery)) {

                for (int j = startCount; j < endCount; j++) {
                    preparedStatementForDelete.setString(1, sessionsToRemove[j]);
                    preparedStatementForDelete.addBatch();
                }
                preparedStatementForDelete.executeBatch();

                if (log.isDebugEnabled()) {
                    log.debug("Removed  " + (endCount - startCount) + " records from " + tableName + ".");
                }
            }
            startCount = endCount;
        }

        if (log.isDebugEnabled()) {
            log.debug("Removed total " + totalSessionsToRemove + " records from " + tableName + ".");
        }
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

            JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
            try {
                jdbcTemplate.executeUpdate(SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO, preparedStatement -> {
                    preparedStatement.setString(1, sessionId);
                    preparedStatement.setString(2, subject);
                    preparedStatement.setInt(3, appID);
                    preparedStatement.setString(4, inboundAuth);
                });
            } catch (DataAccessException e) {
                throw new DataAccessException("Error while storing application data for session in the database.", e);
            }
    }

    /**
     * Method to get app id from SP_APP table.
     *
     * @param applicationName application Name
     * @param appTenantID     app tenant id
     * @return the application id
     * @throws UserSessionException if an error occurs when retrieving app id
     */
    public int getAppId(String applicationName, int appTenantID) throws UserSessionException {

        Integer appId;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
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

        Integer recordCount;

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            recordCount = jdbcTemplate.fetchSingleRecord(SQLQueries.SQL_CHECK_IDN_AUTH_SESSION_APP_INFO,
                    (resultSet, rowNumber) -> resultSet.getInt(1),
                    preparedStatement -> {
                        preparedStatement.setString(1, sessionId);
                        preparedStatement.setString(2, subject);
                        preparedStatement.setInt(3, appID);
                        preparedStatement.setString(4, inboundAuth);
                    });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while retrieving application data of session id: " +
                    sessionId + ", subject: " + subject + ", app Id: " + appID + ", protocol: " + inboundAuth + ".", e);
        }
        return recordCount != null;
    }

    /**
     * Method to store session meta data as a batch
     *
     * @param sessionId id of the authenticated session
     * @param metaData  map of metadata type and value of the session
     * @throws UserSessionException while storing session meta data
     */
    public void storeSessionMetaData(String sessionId, Map<String, String> metaData) throws UserSessionException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeBatchInsert(SQLQueries.SQL_INSERT_SESSION_META_DATA, (preparedStatement -> {
                for (Map.Entry<String, String> entry : metaData.entrySet()) {
                    preparedStatement.setString(1, sessionId);
                    preparedStatement.setString(2, entry.getKey());
                    preparedStatement.setString(3, entry.getValue());
                    preparedStatement.addBatch();
                }
            }), sessionId);
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while storing metadata of session:" + sessionId +
                    " in table " + IDN_AUTH_SESSION_META_DATA_TABLE + ".", e);
        }
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

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(SQLQueries.SQL_UPDATE_SESSION_META_DATA, preparedStatement -> {
                preparedStatement.setString(1, value);
                preparedStatement.setString(2, sessionId);
                preparedStatement.setString(3, propertyType);
            });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while updating " + propertyType + " of session:" + sessionId +
                    " in table " + IDN_AUTH_SESSION_META_DATA_TABLE + ".", e);
        }
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

        List<String> sessionIdList = new ArrayList<>();
        int tenantId = IdentityTenantUtil.getTenantId(user.getTenantDomain());
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_GET_SESSIONS_BY_USER)) {
                preparedStatement.setString(1, user.getUserName());
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, (user.getUserStoreDomain() == null) ? FEDERATED_USER_DOMAIN :
                        user.getUserStoreDomain().toUpperCase());
                preparedStatement.setInt(4, idpId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        sessionIdList.add(resultSet.getString(1));
                    }
                }
            } catch (SQLException ex) {
                throw new UserSessionException("Error while retrieving session IDs of user: " +
                        user.getUserName() + ".", ex);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving session IDs of user: " +
                    user.getUserName() + ".", e);
        }
        return sessionIdList;
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

        Boolean isExisting = false;

        int tenantId = IdentityTenantUtil.getTenantId(user.getTenantDomain());
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement(SQLQueries.SQL_GET_SESSION_MAPPING_BY_USER)) {
                preparedStatement.setString(1, sessionId);
                preparedStatement.setString(2, user.getUserName());
                preparedStatement.setInt(3, tenantId);
                preparedStatement.setString(4, (user.getUserStoreDomain() == null) ? FEDERATED_USER_DOMAIN :
                        user.getUserStoreDomain().toUpperCase());
                preparedStatement.setInt(5, idpId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        isExisting = true;
                    }
                }
            } catch (SQLException ex) {
                throw new UserSessionException("Error while retrieving existing mapping between user : " + user
                        .getUserName() + " and session Id: " + sessionId + ".", ex);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving existing mapping between user : " + user
                    .getUserName() + " and session Id: " + sessionId + ".", e);
        }
        return isExisting;
    }
}
