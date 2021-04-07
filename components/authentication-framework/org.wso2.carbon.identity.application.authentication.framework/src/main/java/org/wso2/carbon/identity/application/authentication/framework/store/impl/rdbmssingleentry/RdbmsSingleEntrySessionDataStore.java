/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.store.impl.rdbmssingleentry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionCleanUpService;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionContextDO;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataPersistTask;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.authentication.framework.store.TempAuthContextDataDeleteTask;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.cache.CacheEntry;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/*
 * Session data entries are persist in persistSessionData method. If there is no entry with a unique session id and
 * type combination, a new entry will be added, If there is an entry with the id and type, then the existing
 * entry will be updated. Session data entry removal as single is handled by removeSessionData method
 * and as batch is handled by removeExpiredSessionData method.
 */
public class RdbmsSingleEntrySessionDataStore extends SessionDataStore {

    public static final String DEFAULT_SESSION_STORE_TABLE_NAME = "IDN_AUTH_SESSION_RECORDS_STORE";
    public static final String DEFAULT_TEMP_SESSION_STORE_TABLE_NAME = "IDN_AUTH_TEMP_SESSION_RECORDS_STORE";
    public static final String RDBMSSINGLE_ENTRY_SESSION_DATA_STORE = "RDBMSSingleEntrySessionDataStore";
    private static final Log log = LogFactory.getLog(SessionDataStore.class);
    private static final String SQL_INSERT =
            "INSERT INTO IDN_AUTH_SESSION_RECORDS_STORE(SESSION_ID, SESSION_TYPE, SESSION_OBJECT,TIME_CREATED, EXPIRY_TIME, TENANT_ID) " +
                    "VALUES " + "(?,?,?,?,?,?)";
    private static final String SQL_DELETE =
            "DELETE FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE SESSION_ID = ? AND  SESSION_TYPE = ?";
    private static final String SQL_DELETE_TEMP_RECORDS =
            "DELETE FROM IDN_AUTH_TEMP_SESSION_RECORDS_STORE WHERE SESSION_ID = ? AND  SESSION_TYPE = ?";
    private static final String SQL_DESERIALIZE_OBJECT_MYSQL =
            "SELECT SESSION_OBJECT,TIME_CREATED FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ";
    private static final String SQL_DESERIALIZE_OBJECT_DB2SQL =
            "SELECT SESSION_OBJECT,TIME_CREATED FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ";
    private static final String SQL_DESERIALIZE_OBJECT_MSSQL =
            "SELECT TOP 1 SESSION_OBJECT,TIME_CREATED FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ";
    private static final String SQL_DESERIALIZE_OBJECT_POSTGRESQL =
            "SELECT SESSION_OBJECT,TIME_CREATED FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ";
    private static final String SQL_DESERIALIZE_OBJECT_INFORMIX =
            "SELECT FIRST 1 SESSION_OBJECT, TIME_CREATED FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ";
    private static final String SQL_DESERIALIZE_OBJECT_ORACLE =
            "SELECT * FROM (SELECT SESSION_OBJECT, TIME_CREATED FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ORDER BY TIME_CREATED DESC) WHERE ROWNUM < 2";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_MYSQL =
            "DELETE FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE EXPIRY_TIME < ? LIMIT %d";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_MSSQL =
            "DELETE TOP (%d) FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE EXPIRY_TIME < ?";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_POSTGRESQL = "DELETE FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE " +
            "CTID IN (SELECT CTID FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE EXPIRY_TIME < ? LIMIT %d)";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_ORACLE = "DELETE FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE ROWID" +
            " IN (SELECT ROWID FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE EXPIRY_TIME < ? AND ROWNUM <= %d)";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_INFOMIXSQL = "DELETE FROM (SELECT SESSION_ID, " +
            "SESSION_TYPE,TIME_CREATED FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE EXPIRY_TIME < ? LIMIT %d) ";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_DB2SQL =
            "DELETE FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE (SESSION_ID, SESSION_TYPE, TIME_CREATED) IN " +
                    "(SELECT SESSION_ID, SESSION_TYPE, TIME_CREATED FROM IDN_AUTH_SESSION_RECORDS_STORE WHERE " +
                    "EXPIRY_TIME < ? FETCH FIRST %d ROWS ONLY)";
    private static final String SQL_UPDATE = "UPDATE IDN_AUTH_SESSION_RECORDS_STORE SET SESSION_OBJECT = ?,TIME_CREATED = ?, " +
            "EXPIRY_TIME = ?, TENANT_ID = ? WHERE SESSION_ID = ? AND  SESSION_TYPE = ?";
    private static final String MYSQL_DATABASE = "MySQL";
    private static final String MARIA_DATABASE = "MariaDB";
    private static final String H2_DATABASE = "H2";
    private static final String DB2_DATABASE = "DB2";
    private static final String MS_SQL_DATABASE = "MS SQL";
    private static final String MICROSOFT_DATABASE = "Microsoft";
    private static final String POSTGRESQL_DATABASE = "PostgreSQL";
    private static final String INFORMIX_DATABASE = "Informix";
    private static final BlockingDeque<SessionContextDO> tempAuthnContextDataDeleteQueue = new LinkedBlockingDeque();
    private static int maxSessionDataPoolSize;
    private static int maxTempDataPoolSize;
    private static boolean tempDataCleanupEnabled;
    private boolean enablePersist;
    private String sqlSelect;
    private String sqlDeleteExpiredDataTask;
    private int deleteChunkSize;
    private boolean sessionDataCleanupEnabled;

    {
        try {
            maxSessionDataPoolSize = getIntegerPropertyFromIdentityUtil(RdbmsSingleEntryConstants.GET_POOL_SIZE,
                    RdbmsSingleEntryConstants.DEFAULT_MAX_SESSION_DATA_POOLSIZE);
            getInternalProperty("Session data pool size config value: ", maxSessionDataPoolSize);
            tempDataCleanupEnabled = getBooleanPropertyFromIdentityUtil(RdbmsSingleEntryConstants.GET_TEMP_CLEANUP_ENABLE,
                    RdbmsSingleEntryConstants.DEFAULT_TEMP_DATA_CLEANUP_ENABLED);
            maxTempDataPoolSize = getIntegerPropertyFromIdentityUtil(RdbmsSingleEntryConstants.GET_TEMP_POOL_SIZE,
                    RdbmsSingleEntryConstants.DEFAULT_MAX_TEMP_DATA_POOLSIZE);
            getInternalProperty("Temporary data pool size config value: ", maxTempDataPoolSize);

        } catch (NumberFormatException e) {
            getInternalProperty("Exception ignored : ", e);
            log.warn("One or more pool size configurations cause NumberFormatException. Default values would be used.");
        }
        if (maxSessionDataPoolSize > 0) {
            log.info("Thread pool size for session persistent consumer : " + maxSessionDataPoolSize);
            ExecutorService threadPool = Executors.newFixedThreadPool(maxSessionDataPoolSize);
            for (int i = 0; i < maxSessionDataPoolSize; i++) {
                threadPool.execute(new SessionDataPersistTask(super.getSessionContextQueue()));
            }
        }
        if (tempDataCleanupEnabled && maxTempDataPoolSize > 0) {
            log.info("Thread pool size for temporary authentication context data delete task: " + maxTempDataPoolSize);
            ExecutorService threadPool = Executors.newFixedThreadPool(maxTempDataPoolSize);
            for (int i = 0; i < maxTempDataPoolSize; i++) {
                threadPool.execute(new TempAuthContextDataDeleteTask(tempAuthnContextDataDeleteQueue));
            }
        }
    }

    public RdbmsSingleEntrySessionDataStore() {

        enablePersist = getBooleanPropertyFromIdentityUtil(RdbmsSingleEntryConstants.PERSIST_ENABLE,
                RdbmsSingleEntryConstants.DEFAULT_ENABLE_PERSIST);
        deleteChunkSize = getIntegerPropertyFromIdentityUtil(RdbmsSingleEntryConstants.GET_DELETE_CHUNK_SIZE,
                RdbmsSingleEntryConstants.DEFAULT_DETELE_CHUNK_SIZE);
        if (!enablePersist) {
            log.info("Session Data Persistence of Authentication framework is not enabled.");
        }
        sessionDataCleanupEnabled = getBooleanPropertyFromIdentityUtil(RdbmsSingleEntryConstants.SESSION_DATA_CLEANUP_ENABLE,
                RdbmsSingleEntryConstants.DEFAULT_SESSION_DATA_CLEANUP_ENABLED);
        if (sessionDataCleanupEnabled || tempDataCleanupEnabled) {
            long sessionCleanupPeriod = IdentityUtil.getCleanUpPeriod(CarbonContext.getThreadLocalCarbonContext().
                    getTenantDomain());
            getInternalProperty(String.format("Session clean up task enabled to run in %d minutes intervals.",
                    sessionCleanupPeriod), "");
            SessionCleanUpService sessionCleanUpService = new SessionCleanUpService(sessionCleanupPeriod / 4,
                    sessionCleanupPeriod);
            sessionCleanUpService.activateCleanUp();
        }
    }

    private void setBlobObject(PreparedStatement prepStmt, Object value, int index)
            throws SQLException, IOException {

        if (value != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.flush();
            oos.close();
            InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
            prepStmt.setBinaryStream(index, inputStream, inputStream.available());
        } else {
            prepStmt.setBinaryStream(index, null, 0);
        }
    }

    private Object getBlobObject(InputStream is)
            throws IdentityApplicationManagementException, IOException, ClassNotFoundException {

        if (is != null) {
            ObjectInput ois = null;
            try {
                ois = new ObjectInputStream(is);
                return ois.readObject();
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        log.error("IOException while trying to close ObjectInputStream.", e);
                    }
                }
            }
        }
        return null;
    }

    private String getSessionStoreDBQuery(String query, String type) {

        if (tempDataCleanupEnabled && super.isTempCache(type)) {
            query = replaceTableName(query);
        }
        return query;
    }

    private String replaceTableName(String query) {

        query = query.replace(DEFAULT_SESSION_STORE_TABLE_NAME, DEFAULT_TEMP_SESSION_STORE_TABLE_NAME);
        return query;
    }

    @Override
    public String getStoreName() {

        return RDBMSSINGLE_ENTRY_SESSION_DATA_STORE;
    }

    @Override
    public void persistSessionData(String key, String type, Object entry, long nanoTime, int tenantId) {

        if (!enablePersist) {
            return;
        }

        long validityPeriodNano = 0L;
        if (entry instanceof CacheEntry) {
            validityPeriodNano = ((CacheEntry) entry).getValidityPeriod();
        }
        if (validityPeriodNano == 0L) {
            validityPeriodNano = getCleanupTimeout(type, tenantId);
        }
        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {

            if (isDBType(connection, MYSQL_DATABASE)
                    || isDBType(connection, H2_DATABASE)) {
                sqlSelect = SQL_DESERIALIZE_OBJECT_MYSQL;
            } else if (isDBType(connection, DB2_DATABASE)) {
                sqlSelect = SQL_DESERIALIZE_OBJECT_DB2SQL;
            } else if (isDBType(connection, MS_SQL_DATABASE)
                    || isDBType(connection, MICROSOFT_DATABASE)) {
                sqlSelect = SQL_DESERIALIZE_OBJECT_MSSQL;
            } else if (isDBType(connection, POSTGRESQL_DATABASE)) {
                sqlSelect = SQL_DESERIALIZE_OBJECT_POSTGRESQL;
            } else if (isDBType(connection, INFORMIX_DATABASE)) {
                sqlSelect = SQL_DESERIALIZE_OBJECT_INFORMIX;
            } else {
                sqlSelect = SQL_DESERIALIZE_OBJECT_ORACLE;
            }

            String sqlSelectQuery = getSessionStoreDBQuery(sqlSelect, type);
            preparedStatement = connection.prepareStatement(sqlSelectQuery);
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String sqlUpdateQuery = getSessionStoreDBQuery(SQL_UPDATE, type);
                preparedStatement = connection.prepareStatement(sqlUpdateQuery);
                setBlobObject(preparedStatement, entry, 1);
                preparedStatement.setLong(2, nanoTime);
                preparedStatement.setLong(3, nanoTime + validityPeriodNano);
                preparedStatement.setInt(4, tenantId);
                preparedStatement.setString(5, key);
                preparedStatement.setString(6, type);
                preparedStatement.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);

            } else {
                String sqlQuery = getSessionStoreDBQuery(SQL_INSERT, type);
                preparedStatement = connection.prepareStatement(sqlQuery);
                preparedStatement.setString(1, key);
                preparedStatement.setString(2, type);
                setBlobObject(preparedStatement, entry, 3);
                preparedStatement.setLong(4, nanoTime);
                preparedStatement.setLong(5, nanoTime + validityPeriodNano);
                preparedStatement.setInt(6, tenantId);
                preparedStatement.executeUpdate();
                IdentityDatabaseUtil.commitTransaction(connection);
            }
        } catch (SQLException | IOException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            log.error("Error while storing session data.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, preparedStatement);
        }
    }

    @Override
    public SessionContextDO getSessionContextData(String key, String type) {

        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection(false);
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return null;
        }
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {

            if (isDBType(connection, MYSQL_DATABASE)
                    || isDBType(connection, H2_DATABASE)) {
                sqlSelect = SQL_DESERIALIZE_OBJECT_MYSQL;
            } else if (isDBType(connection, DB2_DATABASE)) {
                sqlSelect = SQL_DESERIALIZE_OBJECT_DB2SQL;
            } else if (isDBType(connection, MS_SQL_DATABASE)
                    || isDBType(connection, MICROSOFT_DATABASE)) {
                sqlSelect = SQL_DESERIALIZE_OBJECT_MSSQL;
            } else if (isDBType(connection, POSTGRESQL_DATABASE)) {
                sqlSelect = SQL_DESERIALIZE_OBJECT_POSTGRESQL;
            } else if (isDBType(connection, INFORMIX_DATABASE)) {
                sqlSelect = SQL_DESERIALIZE_OBJECT_INFORMIX;
            } else {
                sqlSelect = SQL_DESERIALIZE_OBJECT_ORACLE;
            }

            preparedStatement = connection.prepareStatement(getSessionStoreDBQuery(sqlSelect, type));
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                long nanoTime = resultSet.getLong(2);
                return new SessionContextDO(key, type, getBlobObject(resultSet.getBinaryStream(1)), nanoTime);
            }
        } catch (ClassNotFoundException | IOException | SQLException |
                IdentityApplicationManagementException e) {
            log.error("Error while retrieving session data.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }
        return null;
    }

    @Override
    /**
     * Cleans the session data from the DB.
     */
    public void removeTempAuthnContextData(String key, String type) {

        if (!enablePersist) {
            return;
        }
        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(SQL_DELETE_TEMP_RECORDS);
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            preparedStatement.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (Exception e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            log.error("Error while deleting temporary authentication context data.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, preparedStatement);
        }
    }

    /**
     * Cleans the session data from the DB.
     */
    @Override
    public void removeExpiredSessionData() {

        if (StringUtils.isBlank(sqlDeleteExpiredDataTask)) {
            try {
                sqlDeleteExpiredDataTask = getDBSpecificSessionDataRemovalQuery();
            } catch (IdentityApplicationManagementException e) {
                log.error("Error when initializing the db specific cleanup query.", e);
            }
        }
        if (sessionDataCleanupEnabled) {
            removeExpiredSessionData(sqlDeleteExpiredDataTask);
        }
        if (tempDataCleanupEnabled) {
            removeExpiredSessionData(replaceTableName(sqlDeleteExpiredDataTask));
        }
    }

    /**
     * Gets the DB specific query for the session data removal, this may be overridden by the configuration.
     * "JDBCPersistenceManager.SessionDataPersist.SQL.DeleteExpiredDataTask"
     *
     * @return
     * @throws IdentityApplicationManagementException
     */
    private String getDBSpecificSessionDataRemovalQuery() throws IdentityApplicationManagementException {

        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection();
            String nonFormattedQuery;

            if (isDBType(connection, MYSQL_DATABASE) || isDBType(connection, MARIA_DATABASE)
                    || isDBType(connection, H2_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_MYSQL;
            } else if (connection.getMetaData().getDatabaseProductName().contains(DB2_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_DB2SQL;
            } else if (isDBType(connection, MS_SQL_DATABASE)
                    || isDBType(connection, MICROSOFT_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_MSSQL;
            } else if (isDBType(connection, POSTGRESQL_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_POSTGRESQL;
            } else if (isDBType(connection, INFORMIX_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_INFOMIXSQL;
            } else {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_ORACLE;
            }
            IdentityDatabaseUtil.commitTransaction(connection);
            return String.format(nonFormattedQuery, deleteChunkSize);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new IdentityApplicationManagementException("Error while retrieving DB connection meta-data.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, null);
        }
    }

    /**
     * Removes the records related to expired sessions from DB.
     */
    private void removeExpiredSessionData(String sqlQuery) {

        getInternalProperty("DB query for removing expired data: ", sqlQuery);
        long currentTime = FrameworkUtils.getCurrentStandardNano();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            boolean deleteCompleted = false;
            int totalDeletedEntries = 0;
            while (!deleteCompleted) {
                try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
                    statement.setLong(1, currentTime);
                    int noOfDeletedRecords = statement.executeUpdate();
                    deleteCompleted = noOfDeletedRecords < deleteChunkSize;
                    totalDeletedEntries += noOfDeletedRecords;
                    // Commit the chunk deletion.
                    IdentityDatabaseUtil.commitTransaction(connection);
                    getInternalProperty(String.format("Removed %d expired session records.", noOfDeletedRecords), "");
                }
            }
            getInternalProperty(String.format("Deleted total of %d entries", totalDeletedEntries), "");
        } catch (SQLException | IdentityRuntimeException e) {
            log.error("Error while removing session data from the database for nano time: " + currentTime, e);
        }
    }

    @Override
    public void removeSessionData(String key, String type, long nanoTime) {

        if (!enablePersist) {
            return;
        }
        if (tempDataCleanupEnabled && maxTempDataPoolSize > 0 && isTempCache(type)) {
            tempAuthnContextDataDeleteQueue.push(new SessionContextDO(key, type, null, nanoTime));
            return;
        }
        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(getSessionStoreDBQuery(SQL_DELETE, type));
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            preparedStatement.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (Exception e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            log.error("Error while storing DELETE session data.", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, preparedStatement);
        }
    }

    @Override
    /**
     * @deprecated This is now run as a part of the {@link #removeExpiredSessionData()} due to a possible deadlock as
     * mentioned in IDENTITY-5131.
     */
    public void removeExpiredOperationData() {
        // Empty method.

    }

    boolean isDBType(Connection connection, String dbName) throws SQLException {

        return connection.getMetaData().getDriverName().contains(dbName);
    }

}
