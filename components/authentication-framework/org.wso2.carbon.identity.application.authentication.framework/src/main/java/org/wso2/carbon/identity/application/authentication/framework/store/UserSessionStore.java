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
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.exception.DuplicatedAuthUserException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.JdbcUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

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
import java.util.concurrent.TimeUnit;

import static org.wso2.carbon.identity.core.util.JdbcUtils.isH2DB;

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
        if (idpName.equals("LOCAL")) {
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

        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(true)) {
            try (PreparedStatement preparedStatement = connection
                     .prepareStatement(SQLQueries.SQL_INSERT_USER_SESSION_STORE_OPERATION)) {
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, sessionId);
                preparedStatement.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
                if (log.isDebugEnabled()) {
                    log.debug("Stored user session data for user " + userId + " with session id: " + sessionId);
                }
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
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
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
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
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

        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(true)) {
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

        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(true)) {
            try {
                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_USER_SESSION_MAPPING_TABLE,
                        SQLQueries.SQL_DELETE_TERMINATED_SESSION_DATA);
                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_SESSION_APP_INFO_TABLE,
                        SQLQueries.SQL_DELETE_IDN_AUTH_SESSION_APP_INFO);
                deleteSessionDataFromTable(sessionsToRemove, connection, IDN_AUTH_SESSION_META_DATA_TABLE,
                        SQLQueries.SQL_DELETE_IDN_AUTH_SESSION_META_DATA);
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                log.error("Error while removing the terminated session information from the database.", e1);
            }
        } catch (SQLException e) {
            log.error("Error while obtaining the db connection to remove terminated session information", e);
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

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            jdbcTemplate.withTransaction(template -> {
                String query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_H2;
                if (JdbcUtils.isOracleDB()) {
                    query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_ORACLE;
                    template.executeUpdate(query, preparedStatement -> {
                        preparedStatement.setString(1, sessionId);
                        preparedStatement.setString(2, subject);
                        preparedStatement.setInt(3, appID);
                        preparedStatement.setString(4, inboundAuth);
                        preparedStatement.setString(5, sessionId);
                        preparedStatement.setString(6, subject);
                        preparedStatement.setInt(7, appID);
                        preparedStatement.setString(8, inboundAuth);
                    });
                } else {
                    if (JdbcUtils.isMSSqlDB() || JdbcUtils.isDB2DB()) {
                        query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MSSQL_OR_DB2;
                    } else if (JdbcUtils.isMySQLDB() ||  JdbcUtils.isMariaDB()) {
                        query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MYSQL_OR_MARIADB;
                    } else if (JdbcUtils.isPostgreSQLDB()) {
                        query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_POSTGRES;
                    } else if (JdbcUtils.isOracleDB()) {
                        query = SQLQueries.SQL_STORE_IDN_AUTH_SESSION_APP_INFO_ORACLE;
                    }
                    template.executeUpdate(query, preparedStatement -> {
                        preparedStatement.setString(1, sessionId);
                        preparedStatement.setString(2, subject);
                        preparedStatement.setInt(3, appID);
                        preparedStatement.setString(4, inboundAuth);
                    });
                }
                return null;
            });
        } catch (TransactionException e) {
            throw new DataAccessException("Error while storing application data of session id: " +
                    sessionId + ", subject: " + subject + ", app Id: " + appID + ", protocol: " + inboundAuth + ".", e);
        }
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

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            jdbcTemplate.withTransaction(template -> {
                Integer recordCount = template.fetchSingleRecord(SQLQueries.SQL_CHECK_IDN_AUTH_SESSION_APP_INFO,
                        (resultSet, rowNumber) -> resultSet.getInt(1),
                        preparedStatement -> {
                            preparedStatement.setString(1, sessionId);
                            preparedStatement.setString(2, subject);
                            preparedStatement.setInt(3, appID);
                            preparedStatement.setString(4, inboundAuth);
                        });
                if (recordCount == null) {
                    storeAppSessionData(sessionId, subject, appID, inboundAuth);
                }
                return null;
            });
        } catch (TransactionException e) {
            throw new DataAccessException("Error while storing application data of session id: " +
                    sessionId + ", subject: " + subject + ", app Id: " + appID + ", protocol: " + inboundAuth + ".", e);
        }
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

        Integer recordCount;

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
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

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            String sqlStmt = isH2DB() ? SQLQueries.SQL_INSERT_SESSION_META_DATA_H2 :
                    SQLQueries.SQL_INSERT_SESSION_META_DATA;
            jdbcTemplate.executeBatchInsert(sqlStmt, (preparedStatement -> {
                for (Map.Entry<String, String> entry : metaData.entrySet()) {
                    preparedStatement.setString(1, sessionId);
                    preparedStatement.setString(2, entry.getKey());
                    preparedStatement.setString(3, entry.getValue());
                    preparedStatement.addBatch();
                }
            }), sessionId);
            if (log.isDebugEnabled()) {
                log.debug("Inserted metadata for session id: " + sessionId);
            }
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

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.SESSION);
        try {
            String sqlStmt = isH2DB() ? SQLQueries.SQL_UPDATE_SESSION_META_DATA_H2 :
                    SQLQueries.SQL_UPDATE_SESSION_META_DATA;
            jdbcTemplate.executeUpdate(sqlStmt, preparedStatement -> {
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
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
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
                        user.getLoggableUserId() + ".", ex);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving session IDs of user: " +
                    user.getLoggableUserId() + ".", e);
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

        boolean isExisting = false;

        int tenantId = IdentityTenantUtil.getTenantId(user.getTenantDomain());
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
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
                        .getLoggableUserId() + " and session Id: " + sessionId + ".", ex);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while retrieving existing mapping between user : " + user
                    .getLoggableUserId() + " and session Id: " + sessionId + ".", e);
        }
        return isExisting;
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

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
             try (PreparedStatement prepStmt
                     = connection.prepareStatement(SQLQueries.SQL_STORE_FEDERATED_AUTH_SESSION_INFO)) {
                prepStmt.setString(1, authHistory.getIdpSessionIndex());
                prepStmt.setString(2, sessionContextKey);
                prepStmt.setString(3, authHistory.getIdpName());
                prepStmt.setString(4, authHistory.getAuthenticatorName());
                prepStmt.setString(5, authHistory.getRequestType());
                prepStmt.execute();
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new UserSessionException("Error while adding session details of the session index:"
                        + sessionContextKey + ", IdP:" + authHistory.getIdpName(), e1);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while adding session details of the session index:"
                    + sessionContextKey + ", IdP:" + authHistory.getIdpName(), e);
        }
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

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection
                     .prepareStatement(SQLQueries.SQL_STORE_FEDERATED_AUTH_SESSION_INFO_WITH_TENANT)) {
            prepStmt.setString(1, authHistory.getIdpSessionIndex());
            prepStmt.setString(2, sessionContextKey);
            prepStmt.setString(3, authHistory.getIdpName());
            prepStmt.setString(4, authHistory.getAuthenticatorName());
            prepStmt.setString(5, authHistory.getRequestType());
            prepStmt.setInt(6, tenantId);
            prepStmt.execute();
        } catch (SQLException e) {
            String msg = String.format("Error while adding session details of the session index: %s, IdP: %s " +
                    "and tenant id: %s.", sessionContextKey, authHistory.getIdpName(), tenantId);
            throw new UserSessionException(msg, e);
        }
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

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(SQLQueries.SQL_UPDATE_FEDERATED_AUTH_SESSION_INFO, preparedStatement -> {
                preparedStatement.setString(1, sessionContextKey);
                preparedStatement.setString(2, authHistory.getIdpSessionIndex());
            });
        } catch (DataAccessException e) {
            throw new UserSessionException("Error while updating " + sessionContextKey + " of session:" +
                    authHistory.getIdpSessionIndex() + " in table " + IDN_AUTH_SESSION_META_DATA_TABLE + ".", e);
        }
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

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(
                    SQLQueries.SQL_UPDATE_FEDERATED_AUTH_SESSION_INFO_WITH_TENANT, preparedStatement -> {
                        preparedStatement.setString(1, sessionContextKey);
                        preparedStatement.setString(2, authHistory.getIdpSessionIndex());
                        preparedStatement.setInt(3, tenantId);
                    });
        } catch (DataAccessException e) {
            String msg = String.format("Error while updating %s of session: %s in table " +
                            "IDN_FED_AUTH_SESSION_MAPPING for tenant id %s.", sessionContextKey,
                    authHistory.getIdpSessionIndex(), tenantId);
            throw new UserSessionException(msg, e);
        }
    }

    /**
     * Check whether there is already existing federated auth session with the given session index.
     *
     * @param idpSessionIndex IDP session index.
     * @return True if a federated auth session found with the given session index.
     * @throws UserSessionException If an error occurred while checking for an federated auth session.
     */
    public boolean hasExistingFederatedAuthSession(String idpSessionIndex) throws UserSessionException {

        boolean isExisting = false;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt
                     = connection.prepareStatement(SQLQueries.SQL_GET_FEDERATED_AUTH_SESSION_ID_BY_SESSION_ID)) {
            prepStmt.setString(1, idpSessionIndex);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    isExisting = true;
                }
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error occurred while checking for an federated auth session " +
                    "with session index: " + idpSessionIndex, e);
        }
        return isExisting;
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

        boolean isExisting = false;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = connection.prepareStatement(
                     SQLQueries.SQL_GET_FEDERATED_AUTH_SESSION_ID_BY_SESSION_ID_WITH_TENANT)) {
            prepStmt.setString(1, idpSessionIndex);
            prepStmt.setInt(2, tenantId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                if (resultSet.next()) {
                    isExisting = true;
                }
            }
        } catch (SQLException e) {
            String msg = String.format("Error occurred while checking for a federated auth session with " +
                    "session index: %s and tenant id: %s", idpSessionIndex, tenantId);
            throw new UserSessionException(msg, e);
        }
        return isExisting;
    }

    /**
     * Remove federated authentication session details of a given session context key.
     *
     * @param sessionContextKey Session Context Key.
     * @throws UserSessionException Error while deleting session details of a given session id.
     */
    public void removeFederatedAuthSessionInfo(String sessionContextKey) throws UserSessionException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
             try (PreparedStatement prepStmt
                     = connection.prepareStatement(SQLQueries.SQL_DELETE_FEDERATED_AUTH_SESSION_INFO)) {
                prepStmt.setString(1, sessionContextKey);
                prepStmt.execute();
            } catch (SQLException e1) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new UserSessionException("Error while removing federated authentication session details of " +
                        "the session index:" + sessionContextKey, e1);
            }
        } catch (SQLException e) {
            throw new UserSessionException("Error while removing federated authentication session details of " +
                    "the session index:" + sessionContextKey, e);
        }
    }

    /**
     * Method to check whether the user id is available in the IDN_AUTH_USER table.
     *
     * @param userId    Id of the user
     * @return the boolean decision
     * @throws UserSessionException if an error occurs when retrieving the mapping from the database
     */
    public boolean isExistingUser(String userId) throws UserSessionException {

        Boolean isExisting = false;
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

        int activeSessionCount = 0;
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        long idleSessionTimeOut = TimeUnit.SECONDS.toMillis(IdPManagementUtil.getIdleSessionTimeOut(tenantDomain));
        long currentTime = System.currentTimeMillis();
        long minTimestamp = currentTime - idleSessionTimeOut;

        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(false)) {
            String sqlStmt = isH2DB() ? SQLQueries.SQL_GET_ACTIVE_SESSION_COUNT_BY_TENANT_H2 :
                        SQLQueries.SQL_GET_ACTIVE_SESSION_COUNT_BY_TENANT;
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStmt)) {
                preparedStatement.setString(1, SessionMgtConstants.LAST_ACCESS_TIME);
                preparedStatement.setString(2, String.valueOf(minTimestamp));
                preparedStatement.setString(3, String.valueOf(currentTime));
                preparedStatement.setInt(4, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        activeSessionCount = resultSet.getInt(1);
                    }
                }
                IdentityDatabaseUtil.commitTransaction(connection);
            }
        } catch (DataAccessException | SQLException e) {
            throw new UserSessionException("Error while retrieving active session count of the tenant domain, " +
                    tenantDomain, e);
        }
        return activeSessionCount;
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
}
