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

package org.wso2.carbon.identity.application.authentication.framework.store;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.SessionSerializerException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.cache.CacheEntry;
import org.wso2.carbon.identity.core.model.IdentityCacheConfig;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Data will be persisted or stored date will be removed from the store. These two events are considered as STORE
 * operation and DELETE operations.
 * And these events are stored with unique sessionId, operation type and operation initiated timestamp.
 * Expired DELETE operations and related STORE operations will be deleted by a OperationCleanUpService task.
 * All expired operations will be deleted by SessionCleanUpService task.
 */
public class SessionDataStore {
    private static final Log log = LogFactory.getLog(SessionDataStore.class);

    private static final String OPERATION_DELETE = "DELETE";
    private static final String OPERATION_STORE = "STORE";
    private static final String SQL_INSERT_STORE_OPERATION =
            "INSERT INTO IDN_AUTH_SESSION_STORE(SESSION_ID, SESSION_TYPE, OPERATION, SESSION_OBJECT, TIME_CREATED, " +
                    "EXPIRY_TIME, TENANT_ID) VALUES (?,?,?,?,?,?,?)";
    private static final String SQL_INSERT_DELETE_OPERATION =
            "INSERT INTO IDN_AUTH_SESSION_STORE(SESSION_ID, SESSION_TYPE,OPERATION, TIME_CREATED, EXPIRY_TIME) " +
                    "VALUES (?,?,?,?,?)";
    private static final String SQL_DELETE_STORE_OPERATIONS_TASK =
            "DELETE FROM IDN_AUTH_SESSION_STORE WHERE OPERATION = '" + OPERATION_STORE + "' AND SESSION_ID in (" +
                    "SELECT SESSION_ID  FROM IDN_AUTH_SESSION_STORE WHERE OPERATION = '" + OPERATION_DELETE + "')";

    private static final String SQL_DELETE_TEMP_STORE_OPERATIONS_TASK =
            "DELETE FROM IDN_AUTH_TEMP_SESSION_STORE WHERE EXPIRY_TIME < ?";
    private static final String SQL_DELETE_STORE_OPERATIONS_TASK_MYSQL =
            "DELETE IDN_AUTH_SESSION_STORE_DELETE FROM IDN_AUTH_SESSION_STORE IDN_AUTH_SESSION_STORE_DELETE WHERE " +
                    "OPERATION = '" + OPERATION_STORE + "' AND SESSION_ID IN (SELECT SESSION_ID FROM " +
                    "(SELECT SESSION_ID FROM IDN_AUTH_SESSION_STORE WHERE OPERATION = '" + OPERATION_DELETE + "') " +
                    "IDN_AUTH_SESSION_STORE_SELECT)";
    private static final String SQL_DELETE_DELETE_OPERATIONS_TASK =
            "DELETE FROM IDN_AUTH_SESSION_STORE WHERE OPERATION = '" + OPERATION_DELETE + "' AND  EXPIRY_TIME < ?";
    private static final String SQL_DELETE_TEMP_RECORDS =
            "DELETE FROM IDN_AUTH_TEMP_SESSION_STORE WHERE SESSION_ID = ? AND  SESSION_TYPE = ?";

    private static final String SQL_DESERIALIZE_OBJECT_MYSQL =
            "SELECT OPERATION, SESSION_OBJECT, TIME_CREATED FROM IDN_AUTH_SESSION_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ORDER BY TIME_CREATED DESC LIMIT 1";
    private static final String SQL_DESERIALIZE_OBJECT_DB2SQL =
            "SELECT OPERATION, SESSION_OBJECT, TIME_CREATED FROM IDN_AUTH_SESSION_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ORDER BY TIME_CREATED DESC FETCH FIRST 1 ROWS ONLY";
    private static final String SQL_DESERIALIZE_OBJECT_MSSQL =
            "SELECT TOP 1 OPERATION, SESSION_OBJECT, TIME_CREATED FROM IDN_AUTH_SESSION_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ORDER BY TIME_CREATED DESC";
    private static final String SQL_DESERIALIZE_OBJECT_POSTGRESQL =
            "SELECT OPERATION, SESSION_OBJECT, TIME_CREATED FROM IDN_AUTH_SESSION_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ORDER BY TIME_CREATED DESC LIMIT 1";
    private static final String SQL_DESERIALIZE_OBJECT_INFORMIX =
            "SELECT FIRST 1 OPERATION, SESSION_OBJECT, TIME_CREATED FROM IDN_AUTH_SESSION_STORE " +
                    "WHERE SESSION_ID =? AND " +
                    "SESSION_TYPE=? ORDER BY TIME_CREATED DESC LIMIT 1";
    private static final String SQL_DESERIALIZE_OBJECT_ORACLE =
            "SELECT * FROM (SELECT OPERATION, SESSION_OBJECT, TIME_CREATED " +
                    "FROM IDN_AUTH_SESSION_STORE WHERE SESSION_ID =? AND " +
                    "SESSION_TYPE=? ORDER BY TIME_CREATED DESC) WHERE ROWNUM < 2";

    private static final String SQL_DELETE_EXPIRED_DATA_TASK_MYSQL =
            "DELETE FROM IDN_AUTH_SESSION_STORE WHERE EXPIRY_TIME < ? LIMIT %d";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_MSSQL =
            "DELETE TOP (%d) FROM IDN_AUTH_SESSION_STORE WHERE EXPIRY_TIME < ?";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_POSTGRESQL = "DELETE FROM IDN_AUTH_SESSION_STORE WHERE " +
            "CTID IN (SELECT CTID FROM IDN_AUTH_SESSION_STORE WHERE EXPIRY_TIME < ? LIMIT %d)";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_ORACLE = "DELETE FROM IDN_AUTH_SESSION_STORE WHERE ROWID" +
            " IN (SELECT ROWID FROM IDN_AUTH_SESSION_STORE WHERE EXPIRY_TIME < ? AND ROWNUM <= %d)";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_INFOMIXSQL = "DELETE FROM (SELECT SESSION_ID, " +
            "SESSION_TYPE, OPERATION, TIME_CREATED FROM IDN_AUTH_SESSION_STORE WHERE EXPIRY_TIME < ? LIMIT %d) ";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_DB2SQL =
            "DELETE FROM IDN_AUTH_SESSION_STORE WHERE (SESSION_ID, SESSION_TYPE, OPERATION, TIME_CREATED) IN " +
                    "(SELECT SESSION_ID, SESSION_TYPE, OPERATION, TIME_CREATED FROM IDN_AUTH_SESSION_STORE WHERE " +
                    "EXPIRY_TIME < ? FETCH FIRST %d ROWS ONLY)";
    private static final String MYSQL_DATABASE = "MySQL";
    private static final String MARIA_DATABASE = "MariaDB";
    private static final String H2_DATABASE = "H2";
    private static final String DB2_DATABASE = "DB2";
    private static final String MS_SQL_DATABASE = "MS SQL";
    private static final String MICROSOFT_DATABASE = "Microsoft";
    private static final String POSTGRESQL_DATABASE = "PostgreSQL";
    private static final String INFORMIX_DATABASE = "Informix";

    private static final int DEFAULT_DELETE_LIMIT = 50000;
    public static final String DEFAULT_SESSION_STORE_TABLE_NAME = "IDN_AUTH_SESSION_STORE";
    private static final String CACHE_MANAGER_NAME = "IdentityApplicationManagementCacheManager";
    public static final String DEFAULT_TEMP_SESSION_STORE_TABLE_NAME = "IDN_AUTH_TEMP_SESSION_STORE";
    private static int maxSessionDataPoolSize = 100;
    private static int maxTempDataPoolSize = 50;
    private static BlockingDeque<SessionContextDO> sessionContextQueue = new LinkedBlockingDeque();
    private static BlockingDeque<SessionContextDO> tempAuthnContextDataDeleteQueue = new LinkedBlockingDeque();
    private static volatile SessionDataStore instance;
    private boolean enablePersist;
    private String sqlInsertSTORE;
    private String sqlInsertDELETE;
    private String sqlDeleteSTORETask;
    private String sqlDeleteTempDataTask;
    private String sqlDeleteDELETETask;
    private String sqlSelect;
    private String sqlDeleteExpiredDataTask;
    private int deleteChunkSize = DEFAULT_DELETE_LIMIT;
    private boolean sessionDataCleanupEnabled = true;
    private boolean operationDataCleanupEnabled = false;
    private static boolean tempDataCleanupEnabled = false;

    static {
        try {
            String maxPoolSizeValue = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.PoolSize");
            if (StringUtils.isNotBlank(maxPoolSizeValue)) {
                if (log.isDebugEnabled()) {
                    log.debug("Session data pool size config value: " + maxPoolSizeValue);
                }
                maxSessionDataPoolSize = Integer.parseInt(maxPoolSizeValue);
            }

            String isTempDataCleanupEnabledVal
                    = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.TempDataCleanup.Enable");
            if (StringUtils.isNotBlank(isTempDataCleanupEnabledVal)) {
                tempDataCleanupEnabled = Boolean.parseBoolean(isTempDataCleanupEnabledVal);
            }

            String maxTempDataPoolSizeValue
                    = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.TempDataCleanup.PoolSize");
            if (StringUtils.isNotBlank(maxTempDataPoolSizeValue)) {
                if (log.isDebugEnabled()) {
                    log.debug("Temporary data pool size config value: " + maxPoolSizeValue);
                }
                maxTempDataPoolSize = Integer.parseInt(maxTempDataPoolSizeValue);
            }

        } catch (NumberFormatException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception ignored : ", e);
            }
            log.warn("One or more pool size configurations cause NumberFormatException. Default values would be used");
        }
        if (maxSessionDataPoolSize > 0) {
            log.info("Thread pool size for session persistent consumer : " + maxSessionDataPoolSize);
            ExecutorService threadPool = Executors.newFixedThreadPool(maxSessionDataPoolSize);
            for (int i = 0; i < maxSessionDataPoolSize; i++) {
                threadPool.execute(new SessionDataPersistTask(sessionContextQueue));
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

    private SessionDataStore() {
        String enablePersistVal = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Enable");
        enablePersist = true;
        if (enablePersistVal != null) {
            enablePersist = Boolean.parseBoolean(enablePersistVal);
        }
        String insertSTORESQL = IdentityUtil
                .getProperty("JDBCPersistenceManager.SessionDataPersist.SQL.InsertSTORE");
        String insertDELETESQL = IdentityUtil
                .getProperty("JDBCPersistenceManager.SessionDataPersist.SQL.InsertDELETE");
        String deleteSTORETaskSQL = IdentityUtil
                .getProperty("JDBCPersistenceManager.SessionDataPersist.SQL.DeleteSTORETask");
        String deleteTempSTORETaskSQL = IdentityUtil
                .getProperty("JDBCPersistenceManager.SessionDataPersist.SQL.DeleteTempDataTask");
        String deleteDELETETaskSQL = IdentityUtil
                .getProperty("JDBCPersistenceManager.SessionDataPersist.SQL.DeleteDELETETask");
        String selectSQL = IdentityUtil
                .getProperty("JDBCPersistenceManager.SessionDataPersist.SQL.Select");
        String deleteExpiredDataTaskSQL = IdentityUtil
                .getProperty("JDBCPersistenceManager.SessionDataPersist.SQL.DeleteExpiredDataTask");
        if (!StringUtils.isBlank(insertSTORESQL)) {
            sqlInsertSTORE = insertSTORESQL;
        } else {
            sqlInsertSTORE = SQL_INSERT_STORE_OPERATION;
        }
        if (!StringUtils.isBlank(insertDELETESQL)) {
            sqlInsertDELETE = insertDELETESQL;
        } else {
            sqlInsertDELETE = SQL_INSERT_DELETE_OPERATION;
        }
        if (!StringUtils.isBlank(deleteSTORETaskSQL)) {
            sqlDeleteSTORETask = deleteSTORETaskSQL;
        }
        if (!StringUtils.isBlank(deleteTempSTORETaskSQL)) {
            sqlDeleteTempDataTask = deleteTempSTORETaskSQL;
        } else {
            sqlDeleteTempDataTask = SQL_DELETE_TEMP_STORE_OPERATIONS_TASK;
        }

        if (!StringUtils.isBlank(deleteDELETETaskSQL)) {
            sqlDeleteDELETETask = deleteDELETETaskSQL;
        } else {
            sqlDeleteDELETETask = SQL_DELETE_DELETE_OPERATIONS_TASK;
        }
        if (!StringUtils.isBlank(selectSQL)) {
            sqlSelect = selectSQL;
        }

        String deleteChunkSizeString = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist" +
                ".SessionDataCleanUp.DeleteChunkSize");
        if (StringUtils.isNotBlank(deleteChunkSizeString)) {
            deleteChunkSize = Integer.parseInt(deleteChunkSizeString);
        }

        if (StringUtils.isNotBlank(deleteExpiredDataTaskSQL)) {
            sqlDeleteExpiredDataTask = String.format(deleteExpiredDataTaskSQL, deleteChunkSize);
        }

        if (!enablePersist) {
            log.info("Session Data Persistence of Authentication framework is not enabled.");
        }
        String isCleanUpEnabledVal
                = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.SessionDataCleanUp.Enable");

        String isOperationCleanUpEnabledVal
                = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.OperationDataCleanUp.Enable");

        if (StringUtils.isNotBlank(isCleanUpEnabledVal)) {
            sessionDataCleanupEnabled = Boolean.parseBoolean(isCleanUpEnabledVal);
        }
        if (StringUtils.isNotBlank(isOperationCleanUpEnabledVal)) {
            operationDataCleanupEnabled = Boolean.parseBoolean(isOperationCleanUpEnabledVal);
        }

        if (sessionDataCleanupEnabled || operationDataCleanupEnabled || tempDataCleanupEnabled) {
            long sessionCleanupPeriod = IdentityUtil.getCleanUpPeriod(
                    CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            if (log.isDebugEnabled()) {
                log.debug(String.format("Session clean up task enabled to run in %d minutes intervals",
                        sessionCleanupPeriod));
            }
            SessionCleanUpService sessionCleanUpService = new SessionCleanUpService(sessionCleanupPeriod / 4,
                    sessionCleanupPeriod);
            sessionCleanUpService.activateCleanUp();
        }
    }

    public static SessionDataStore getInstance() {
        if (instance == null) {
            synchronized (SessionDataStore.class) {
                if (instance == null) {
                    instance = new SessionDataStore();
                }
            }
        }
        return instance;
    }

    public Object getSessionData(String key, String type) {
        SessionContextDO sessionContextDO = getSessionContextData(key, type);
        return sessionContextDO != null ? sessionContextDO.getEntry() : null;
    }

    public SessionContextDO getSessionContextData(String key, String type) {

        if (log.isDebugEnabled()) {
            log.debug("Getting SessionContextData from DB. key : " + key + " type : " + type);
        }
        if (!enablePersist) {
            return null;
        }
        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getSessionDBConnection(false);
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return null;
        }
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            if (StringUtils.isBlank(sqlSelect)) {
                String driverName = connection.getMetaData().getDriverName();
                if (driverName.contains(MYSQL_DATABASE) || driverName.contains(MARIA_DATABASE)
                        || driverName.contains(H2_DATABASE)) {
                    sqlSelect = SQL_DESERIALIZE_OBJECT_MYSQL;
                } else if (connection.getMetaData().getDatabaseProductName().contains(DB2_DATABASE)) {
                    sqlSelect = SQL_DESERIALIZE_OBJECT_DB2SQL;
                } else if (driverName.contains(MS_SQL_DATABASE)
                        || driverName.contains(MICROSOFT_DATABASE)) {
                    sqlSelect = SQL_DESERIALIZE_OBJECT_MSSQL;
                } else if (driverName.contains(POSTGRESQL_DATABASE)) {
                    sqlSelect = SQL_DESERIALIZE_OBJECT_POSTGRESQL;
                } else if (driverName.contains(INFORMIX_DATABASE)) {
                    // Driver name = "IBM Informix JDBC Driver for IBM Informix Dynamic Server"
                    sqlSelect = SQL_DESERIALIZE_OBJECT_INFORMIX;
                } else {
                    sqlSelect = SQL_DESERIALIZE_OBJECT_ORACLE;
                }
            }
            preparedStatement = connection.prepareStatement(getSessionStoreDBQuery(sqlSelect, type));
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String operation = resultSet.getString(1);
                long nanoTime = resultSet.getLong(3);
                if ((OPERATION_STORE.equals(operation))) {
                    return new SessionContextDO(key, type, getBlobObject(resultSet.getBinaryStream(2)), nanoTime);
                }
            }
        } catch (ClassNotFoundException | IOException | SQLException | SessionSerializerException |
                IdentityApplicationManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving session data", e);
            }
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }
        return null;
    }

    public void storeSessionData(String key, String type, Object entry) {

        storeSessionData(key, type, entry, MultitenantConstants.INVALID_TENANT_ID);
    }

    public void storeSessionData(String key, String type, Object entry, int tenantId) {
        if (!enablePersist) {
            return;
        }
        long nanoTime = FrameworkUtils.getCurrentStandardNano();
        if (maxSessionDataPoolSize > 0 && !isTempCache(type)) {
            sessionContextQueue.push(new SessionContextDO(key, type, entry, nanoTime, tenantId));
        } else {
            persistSessionData(key, type, entry, nanoTime, tenantId);
        }
    }

    public void clearSessionData(String key, String type) {
        if (!enablePersist) {
            return;
        }
        long nanoTime = FrameworkUtils.getCurrentStandardNano();
        if (maxSessionDataPoolSize > 0 && !isTempCache(type)) {
            sessionContextQueue.push(new SessionContextDO(key, type, null, nanoTime));
        } else {
            removeSessionData(key, type, nanoTime);
        }
    }

    /**
     * Method to stop running tasks, when the component is deactivated.
     */
    public void stopService() {

        TempAuthContextDataDeleteTask.shutdown();
        SessionDataPersistTask.shutdown();
    }

    /**
     * Gets the DB specific query for the session data removal, this may be overridden by the configuration
     * "JDBCPersistenceManager.SessionDataPersist.SQL.DeleteExpiredDataTask"
     *
     * @return
     * @throws IdentityApplicationManagementException
     */
    private String getDBSpecificSessionDataRemovalQuery() throws IdentityApplicationManagementException {

        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getSessionDBConnection(true);
            String nonFormattedQuery;
            String driverName = connection.getMetaData().getDriverName();
            if (driverName.contains(MYSQL_DATABASE) || driverName.contains(MARIA_DATABASE)
                    || driverName.contains(H2_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_MYSQL;
            } else if (connection.getMetaData().getDatabaseProductName().contains(DB2_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_DB2SQL;
            } else if (driverName.contains(MS_SQL_DATABASE)
                    || driverName.contains(MICROSOFT_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_MSSQL;
            } else if (driverName.contains(POSTGRESQL_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_POSTGRESQL;
            } else if (driverName.contains(INFORMIX_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_INFOMIXSQL;
            } else {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_ORACLE;
            }
            IdentityDatabaseUtil.commitTransaction(connection);
            return String.format(nonFormattedQuery, deleteChunkSize);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new IdentityApplicationManagementException("Error while retrieving DB connection meta-data", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, null);
        }
    }

    /**
     * Removes the records related to expired sessions from DB.
     */
    private void removeExpiredSessionData(String sqlQuery) {

        if (log.isDebugEnabled()) {
            log.debug("DB query for removing expired data: " + sqlQuery);
        }
        long currentTime = FrameworkUtils.getCurrentStandardNano();
        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(true)) {
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
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Removed %d expired session records.", noOfDeletedRecords));
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleted total of %d entries", totalDeletedEntries));
            }
        } catch (SQLException | IdentityRuntimeException e) {
            log.error("Error while removing session data from the database for nano time: " + currentTime, e);
        }
    }

    /**
     * Cleans the session data and operation data (if enabled) from the DB
     */
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
        if (operationDataCleanupEnabled) {
            removeInvalidatedSTOREOperations();
        }
    }

    /**
     * @deprecated This is now run as a part of the {@link #removeExpiredSessionData()} due to a possible deadlock as
     * mentioned in IDENTITY-5131
     */
    @Deprecated
    public void removeExpiredOperationData() {

    }

    public void persistSessionData(String key, String type, Object entry, long nanoTime, int tenantId) {
        if (!enablePersist) {
            return;
        }
        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getSessionDBConnection(true);
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }

        long validityPeriodNano = 0L;

        if (entry instanceof CacheEntry) {
            validityPeriodNano = ((CacheEntry) entry).getValidityPeriod();
        }

        if (validityPeriodNano == 0L) {
            validityPeriodNano = getCleanupTimeout(type, tenantId);
        }

        PreparedStatement preparedStatement = null;
        try {
            String sqlQuery = getSessionStoreDBQuery(sqlInsertSTORE, type);
            preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            preparedStatement.setString(3, OPERATION_STORE);
            setBlobObject(preparedStatement, entry, 4);
            preparedStatement.setLong(5, nanoTime);
            preparedStatement.setLong(6, nanoTime + validityPeriodNano);
            preparedStatement.setInt(7, tenantId);
            preparedStatement.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException | IOException | SessionSerializerException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            log.error("Error while storing session data", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, preparedStatement);
        }

        if (log.isDebugEnabled()) {
            log.debug("Persisted SessionContextData to DB. key : " + key + " type : " + type);
        }
    }

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
            connection = IdentityDatabaseUtil.getSessionDBConnection(true);
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }
        PreparedStatement preparedStatement = null;

        long timeoutNano = nanoTime + getCleanupTimeout(type, MultitenantConstants.INVALID_TENANT_ID);
        try {
            preparedStatement = connection.prepareStatement(getSessionStoreDBQuery(sqlInsertDELETE, type));
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            preparedStatement.setString(3, OPERATION_DELETE);
            preparedStatement.setLong(4, nanoTime);
            preparedStatement.setLong(5, timeoutNano);
            preparedStatement.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (Exception e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            log.error("Error while storing DELETE operation session data", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, preparedStatement);
        }

        if (log.isDebugEnabled()) {
            log.debug("Removed SessionContextData from DB. key : " + key + " type : " + type);
        }
    }

    /**
     * Removes temporary authn context data from the table if temporary data cleanup is enabled.
     *
     * @param key
     * @param type
     */
    public void removeTempAuthnContextData(String key, String type) {

        if (!enablePersist) {
            return;
        }
        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getSessionDBConnection(true);
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
            log.error("Error while deleting temporary authentication context data", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, preparedStatement);
        }
    }

    private void setBlobObject(PreparedStatement prepStmt, Object value, int index)
            throws SQLException, IOException, SessionSerializerException {
        if (value != null) {
            InputStream inputStream = FrameworkServiceDataHolder.getInstance().
                    getSessionSerializer().serializeSessionObject(value);
            prepStmt.setBinaryStream(index, inputStream, inputStream.available());
        } else {
            prepStmt.setBinaryStream(index, null, 0);
        }
    }

    private Object getBlobObject(InputStream is)
            throws IdentityApplicationManagementException, IOException, ClassNotFoundException,
            SessionSerializerException {
        if (is != null) {
            ObjectInput ois = null;
            try {
                return FrameworkServiceDataHolder.getInstance().getSessionSerializer().deSerializeSessionObject(is);
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

    /**
     * Removes STORE records related to DELETE records in IDN_AUTH_SESSION_STORE table
     */
    private void removeInvalidatedSTOREOperations() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = IdentityDatabaseUtil.getSessionDBConnection(true);
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }

        try {
            if (StringUtils.isBlank(sqlDeleteSTORETask)) {
                String driverName = connection.getMetaData().getDriverName();
                if (driverName.contains(MYSQL_DATABASE) || driverName.contains(MARIA_DATABASE)) {
                    sqlDeleteSTORETask = SQL_DELETE_STORE_OPERATIONS_TASK_MYSQL;
                } else {
                    sqlDeleteSTORETask = SQL_DELETE_STORE_OPERATIONS_TASK;
                }
            }
            statement = connection.prepareStatement(sqlDeleteSTORETask);
            statement.execute();
            IdentityDatabaseUtil.commitTransaction(connection);
            return;
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            log.error("Error while removing STORE operation data from the database. ", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, statement);

        }

    }

    private boolean isTempCache(String type) {

        IdentityCacheConfig identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, type);

        if (identityCacheConfig != null) {
            return identityCacheConfig.isTemporary();
        }
        return false;
    }

    private String getSessionStoreDBQuery(String query, String type) {

        if (tempDataCleanupEnabled && isTempCache(type)) {
            query = replaceTableName(query);
        }
        return query;
    }

    private String replaceTableName(String query) {

        query = query.replace(DEFAULT_SESSION_STORE_TABLE_NAME, DEFAULT_TEMP_SESSION_STORE_TABLE_NAME);
        return query;
    }

    private long getCleanupTimeout(String type, int tenantId) {
        if (isTempCache(type)) {
            return TimeUnit.MINUTES.toNanos(IdentityUtil.getTempDataCleanUpTimeout());
        } else if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            return TimeUnit.SECONDS.toNanos(IdPManagementUtil.getRememberMeTimeout(tenantDomain));
        } else {
            return TimeUnit.MINUTES.toNanos(IdentityUtil.getCleanUpTimeout());
        }
    }

}
