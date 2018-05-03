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
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;

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
import java.util.concurrent.TimeUnit;

/**
 * Data will be persisted or stored date will be removed from the store. These two events are considered as STORE operation
 * and DELETE operations.
 * And these events are stored with unique sessionId, operation type and operation initiated timestamp.
 * Expired DELETE operations and related STORE operations will be deleted by a OperationCleanUpService task.
 * All expired operations will be deleted by SessionCleanUpService task.
 *
 */
public class SessionDataStore {
    private static final Log log = LogFactory.getLog(SessionDataStore.class);

    private static final String OPERATION_DELETE = "DELETE";
    private static final String OPERATION_STORE = "STORE";
    private static final String SQL_INSERT_STORE_OPERATION =
            "INSERT INTO IDN_AUTH_SESSION_STORE(SESSION_ID, SESSION_TYPE, OPERATION, SESSION_OBJECT, TIME_CREATED, TENANT_ID) VALUES (?,?,?,?,?,?)";
    private static final String SQL_INSERT_DELETE_OPERATION =
            "INSERT INTO IDN_AUTH_SESSION_STORE(SESSION_ID, SESSION_TYPE,OPERATION, TIME_CREATED) VALUES (?,?,?,?)";
    private static final String SQL_DELETE_STORE_OPERATIONS_TASK =
            "DELETE FROM IDN_AUTH_SESSION_STORE WHERE OPERATION = '"+OPERATION_STORE+"' AND SESSION_ID in (" +
            "SELECT SESSION_ID  FROM IDN_AUTH_SESSION_STORE WHERE OPERATION = '"+OPERATION_DELETE+"' AND TIME_CREATED < ?)";
    private static final String SQL_DELETE_STORE_OPERATIONS_TASK_MYSQL =
            "DELETE IDN_AUTH_SESSION_STORE_DELETE FROM IDN_AUTH_SESSION_STORE IDN_AUTH_SESSION_STORE_DELETE WHERE " +
                    "OPERATION = '"+OPERATION_STORE+"' AND SESSION_ID IN (SELECT SESSION_ID FROM (SELECT SESSION_ID " +
                    "FROM IDN_AUTH_SESSION_STORE WHERE OPERATION = '"+OPERATION_DELETE+"' AND TIME_CREATED < ?) " +
                    "IDN_AUTH_SESSION_STORE_SELECT)";
    private static final String SQL_DELETE_DELETE_OPERATIONS_TASK =
            "DELETE FROM IDN_AUTH_SESSION_STORE WHERE OPERATION = '"+OPERATION_DELETE+"' AND  TIME_CREATED < ?";

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
            "SELECT FIRST 1 OPERATION, SESSION_OBJECT, TIME_CREATED FROM IDN_AUTH_SESSION_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ORDER BY TIME_CREATED DESC LIMIT 1";
    private static final String SQL_DESERIALIZE_OBJECT_ORACLE =
            "SELECT * FROM (SELECT OPERATION, SESSION_OBJECT, TIME_CREATED FROM IDN_AUTH_SESSION_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ORDER BY TIME_CREATED DESC) WHERE ROWNUM < 2";

    private static final String SQL_DELETE_EXPIRED_DATA_TASK_MYSQL =
            "DELETE FROM IDN_AUTH_SESSION_STORE WHERE TIME_CREATED < ? AND TENANT_ID=? LIMIT %d";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_MSSQL =
            "DELETE TOP (%d) FROM IDN_AUTH_SESSION_STORE WHERE TIME_CREATED < ? AND TENANT_ID = ?";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_POSTGRESQL = "DELETE FROM IDN_AUTH_SESSION_STORE WHERE " +
            "CTID IN (SELECT CTID FROM IDN_AUTH_SESSION_STORE WHERE TIME_CREATED < ? AND TENANT_ID=? LIMIT %d)";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_ORACLE = "DELETE FROM IDN_AUTH_SESSION_STORE WHERE ROWID" +
            " IN (SELECT ROWID FROM IDN_AUTH_SESSION_STORE WHERE TIME_CREATED < ? AND TENANT_ID=? AND ROWNUM <= %d)";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_INFOMIXSQL = "DELETE FROM (SELECT SESSION_ID, " +
            "SESSION_TYPE, OPERATION, TIME_CREATED FROM IDN_AUTH_SESSION_STORE WHERE TIME_CREATED < ? AND TENANT_ID =" +
            " ? LIMIT %d) ";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_DB2SQL =
            "DELETE FROM IDN_AUTH_SESSION_STORE WHERE (SESSION_ID, SESSION_TYPE, OPERATION, TIME_CREATED) IN " +
                    "(SELECT SESSION_ID, SESSION_TYPE, OPERATION, TIME_CREATED FROM IDN_AUTH_SESSION_STORE WHERE " +
                    "TIME_CREATED < ? AND TENANT_ID = ? FETCH FIRST %d ROWS ONLY)";
    private static final String MYSQL_DATABASE = "MySQL";
    private static final String H2_DATABASE = "H2";
    private static final String DB2_DATABASE = "DB2";
    private static final String MS_SQL_DATABASE = "MS SQL";
    private static final String MICROSOFT_DATABASE = "Microsoft";
    private static final String POSTGRESQL_DATABASE = "PostgreSQL";
    private static final String INFORMIX_DATABASE = "Informix";

    private static final int DEFAULT_DELETE_LIMIT = 50000;
    private static int maxPoolSize = 100;
    private static BlockingDeque<SessionContextDO> sessionContextQueue = new LinkedBlockingDeque();
    private static volatile SessionDataStore instance;
    private boolean enablePersist;
    private String sqlInsertSTORE;
    private String sqlInsertDELETE;
    private String sqlDeleteSTORETask;
    private String sqlDeleteDELETETask;
    private String sqlSelect;
    private String sqlDeleteExpiredDataTask;
    private int deleteChunkSize = DEFAULT_DELETE_LIMIT;
    private boolean sessionDataCleanupEnabled = true;
    private boolean operationDataCleanupEnabled = false;

    static {
        try {
            String maxPoolSizeConfigValue = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.PoolSize");
            if (StringUtils.isNotBlank(maxPoolSizeConfigValue)) {
                maxPoolSize = Integer.parseInt(maxPoolSizeConfigValue);
            }
        } catch (NumberFormatException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception ignored : ", e);
            }
            log.warn("Session data persistence pool size is not configured. Using default value.");
        }
        if (maxPoolSize > 0) {
            log.info("Thread pool size for session persistent consumer : " + maxPoolSize);
            ExecutorService threadPool = Executors.newFixedThreadPool(maxPoolSize);
            for (int i = 0; i < maxPoolSize; i++) {
                threadPool.execute(new SessionDataPersistTask(sessionContextQueue));
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
        String isCleanUpEnabledVal = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.SessionDataCleanUp.Enable");

        String isOperationCleanUpEnabledVal = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.OperationDataCleanUp.Enable");

        if (StringUtils.isNotBlank(isCleanUpEnabledVal)) {
            sessionDataCleanupEnabled = Boolean.parseBoolean(isCleanUpEnabledVal);
        }
        if (StringUtils.isNotBlank(isOperationCleanUpEnabledVal)) {
            operationDataCleanupEnabled = Boolean.parseBoolean(isOperationCleanUpEnabledVal);
        }

        if (sessionDataCleanupEnabled || operationDataCleanupEnabled) {
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
        if (!enablePersist) {
            return null;
        }
        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return null;
        }
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            if (StringUtils.isBlank(sqlSelect)) {
                if (connection.getMetaData().getDriverName().contains(MYSQL_DATABASE)
                        || connection.getMetaData().getDriverName().contains(H2_DATABASE)) {
                    sqlSelect = SQL_DESERIALIZE_OBJECT_MYSQL;
                } else if (connection.getMetaData().getDatabaseProductName().contains(DB2_DATABASE)) {
                    sqlSelect = SQL_DESERIALIZE_OBJECT_DB2SQL;
                } else if (connection.getMetaData().getDriverName().contains(MS_SQL_DATABASE)
                        || connection.getMetaData().getDriverName().contains(MICROSOFT_DATABASE)) {
                    sqlSelect = SQL_DESERIALIZE_OBJECT_MSSQL;
                } else if (connection.getMetaData().getDriverName().contains(POSTGRESQL_DATABASE)) {
                    sqlSelect = SQL_DESERIALIZE_OBJECT_POSTGRESQL;
                } else if (connection.getMetaData().getDriverName().contains(INFORMIX_DATABASE)) {
                    // Driver name = "IBM Informix JDBC Driver for IBM Informix Dynamic Server"
                    sqlSelect = SQL_DESERIALIZE_OBJECT_INFORMIX;
                } else {
                    sqlSelect = SQL_DESERIALIZE_OBJECT_ORACLE;
                }
            }
            preparedStatement = connection.prepareStatement(sqlSelect);
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                String operation = resultSet.getString(1);
                long nanoTime = resultSet.getLong(3);
                if ((OPERATION_STORE.equals(operation))) {
                    return new SessionContextDO(key, type, getBlobObject(resultSet.getBinaryStream(2)), nanoTime);
                }
            }
        } catch (ClassNotFoundException | IOException | SQLException |
                IdentityApplicationManagementException e) {
            log.error("Error while retrieving session data", e);
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
        if (maxPoolSize > 0) {
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
        if (maxPoolSize > 0) {
            sessionContextQueue.push(new SessionContextDO(key, type, null, nanoTime));
        } else {
            removeSessionData(key, type, nanoTime);
        }
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
            connection = IdentityDatabaseUtil.getDBConnection();
            String nonFormattedQuery;
            if (connection.getMetaData().getDriverName().contains(MYSQL_DATABASE)
                    || connection.getMetaData().getDriverName().contains(H2_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_MYSQL;
            } else if (connection.getMetaData().getDatabaseProductName().contains(DB2_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_DB2SQL;
            } else if (connection.getMetaData().getDriverName().contains(MS_SQL_DATABASE)
                    || connection.getMetaData().getDriverName().contains(MICROSOFT_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_MSSQL;
            } else if (connection.getMetaData().getDriverName().contains(POSTGRESQL_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_POSTGRESQL;
            } else if (connection.getMetaData().getDriverName().contains(INFORMIX_DATABASE)) {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_INFOMIXSQL;
            } else {
                nonFormattedQuery = SQL_DELETE_EXPIRED_DATA_TASK_ORACLE;
            }
            return String.format(nonFormattedQuery, deleteChunkSize);
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while retrieving DB connection meta-data", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, null);
        }
    }

    /**
     * Removes all records related to expired sessions from DB
     */
    private void removeExpiredRememberMeSessions() {

        if (StringUtils.isBlank(sqlDeleteExpiredDataTask)) {
            try {
                sqlDeleteExpiredDataTask = getDBSpecificSessionDataRemovalQuery();
            } catch (IdentityApplicationManagementException e) {
                log.error("Error when initializing the db specific cleanup query.", e);
            }
        }

        try {
            Tenant[] tenants =
                    FrameworkServiceDataHolder.getInstance().getRealmService().getTenantManager().getAllTenants();
            for (Tenant tenant : tenants) {
                removeExpiredRememberMeSessions(tenant.getId());
            }
        } catch (UserStoreException e) {
            log.error("Error while listing tenants for session clean up task", e);
        }

        //The above method doesn't return the super tenant, hence we have to specifically do the clean up for
        // super tenant
        removeExpiredRememberMeSessions(MultitenantConstants.SUPER_TENANT_ID);
        //remove the entries for invalid tenant.
        removeExpiredRememberMeSessions(MultitenantConstants.INVALID_TENANT_ID);
    }

    /**
     * Removes the records of a given tenant related to expired sessions from DB
     * @param tenantId The tenant Id of whose data needs to be removed
     */
    private void removeExpiredRememberMeSessions(int tenantId) {

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }

        //If the entries are tenant independent, we are using the value from the configuration, If not the tenant's
        // remember me timeout is used
        long cleanupLimitNano = FrameworkUtils.getCurrentStandardNano() -
                TimeUnit.MINUTES.toNanos(IdentityUtil.getCleanUpTimeout());

        if (tenantId != MultitenantConstants.INVALID_TENANT_ID) {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            cleanupLimitNano = FrameworkUtils.getCurrentStandardNano() -
                    TimeUnit.SECONDS.toNanos(IdPManagementUtil.getRememberMeTimeout(tenantDomain));
        }
        try {
            boolean deleteCompleted = false;
            int totalDeletedEntries = 0;
            while (!deleteCompleted) {
                statement = connection.prepareStatement(sqlDeleteExpiredDataTask);
                statement.setLong(1, cleanupLimitNano);
                statement.setInt(2, tenantId);

                int noOfDeletedRecords = statement.executeUpdate();
                deleteCompleted = noOfDeletedRecords < deleteChunkSize;
                totalDeletedEntries += noOfDeletedRecords;
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Removed %d expired session records for tenant id: %d.",
                            noOfDeletedRecords, tenantId));
                }
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleted total of %d entries for the tenant id: %d", totalDeletedEntries,
                        tenantId));
            }
        } catch (SQLException e) {
            log.error("Error while removing session data from the database for nano time " + cleanupLimitNano, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, statement);
        }
    }

    /**
     * Cleans the session data and operation data (if enabled) from the DB
     */
    public void removeExpiredSessionData() {

        if (sessionDataCleanupEnabled) {
            removeExpiredRememberMeSessions();
        }
        if (operationDataCleanupEnabled) {
            deleteSTOREOperationsTask();
            deleteDELETEOperationsTask();
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
            connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sqlInsertSTORE);
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            preparedStatement.setString(3, OPERATION_STORE);
            setBlobObject(preparedStatement, entry, 4);
            preparedStatement.setLong(5, nanoTime);
            preparedStatement.setInt(6, tenantId);
            preparedStatement.executeUpdate();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException | IOException e) {
            log.error("Error while storing session data", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, preparedStatement);
        }
    }

    public void removeSessionData(String key, String type, long nanoTime) {
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
            preparedStatement = connection.prepareStatement(sqlInsertDELETE);
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            preparedStatement.setString(3, OPERATION_DELETE);
            preparedStatement.setLong(4, nanoTime);
            preparedStatement.executeUpdate();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (Exception e) {
            log.error("Error while storing DELETE operation session data", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, preparedStatement);
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

    private void deleteSTOREOperationsTask() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }
        long cleanupLimitNano = FrameworkUtils.getCurrentStandardNano() -
                TimeUnit.MINUTES.toNanos(IdentityUtil.getOperationCleanUpTimeout());
        try {
            if (StringUtils.isBlank(sqlDeleteSTORETask)) {
                if (connection.getMetaData().getDriverName().contains(MYSQL_DATABASE)) {
                    sqlDeleteSTORETask = SQL_DELETE_STORE_OPERATIONS_TASK_MYSQL;
                } else {
                    sqlDeleteSTORETask = SQL_DELETE_STORE_OPERATIONS_TASK;
                }
            }
            statement = connection.prepareStatement(sqlDeleteSTORETask);
            statement.setLong(1, cleanupLimitNano);
            statement.execute();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            return;
        } catch (SQLException e) {
            log.error("Error while removing STORE operation data from the database for nano time " + cleanupLimitNano, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, statement);

        }

    }

    private void deleteDELETEOperationsTask() {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }
        long cleanupLimitNano = FrameworkUtils.getCurrentStandardNano() -
                TimeUnit.MINUTES.toNanos(IdentityUtil.getOperationCleanUpTimeout());
        try {
            statement = connection.prepareStatement(sqlDeleteDELETETask);
            statement.setLong(1, cleanupLimitNano);
            statement.execute();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            return;
        } catch (SQLException e) {
            log.error("Error while removing DELETE operation data from the database for nano time " + cleanupLimitNano, e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, statement);

        }
    }
}
