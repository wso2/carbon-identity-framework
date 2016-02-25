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
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
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
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

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

    private static final String SQL_DELETE_EXPIRED_DATA_TASK =
            "DELETE FROM IDN_AUTH_SESSION_STORE WHERE TIME_CREATED<?";
    private static final String MYSQL_DATABASE = "MySQL";
    private static final String H2_DATABASE = "H2";
    private static final String DB2_DATABASE = "DB2";
    private static final String MS_SQL_DATABASE = "MS SQL";
    private static final String MICROSOFT_DATABASE = "Microsoft";
    private static final String POSTGRESQL_DATABASE = "PostgreSQL";
    private static final String INFORMIX_DATABASE = "Informix";

    private static int maxPoolSize = 100;
    private long operationCleanUpPeriod = 720;
    private String defaultCleanUpEnabled ="true";
    private String defaultOperationCleanUpEnabled ="false";
    private static BlockingDeque<SessionContextDO> sessionContextQueue = new LinkedBlockingDeque();
    private static volatile SessionDataStore instance;
    private boolean enablePersist;
    private String sqlInsertSTORE;
    private String sqlInsertDELETE;
    private String sqlDeleteSTORETask;
    private String sqlDeleteDELETETask;
    private String sqlSelect;
    private String sqlDeleteExpiredDataTask;

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

        if (!StringUtils.isBlank(deleteExpiredDataTaskSQL)) {
            sqlDeleteExpiredDataTask = deleteExpiredDataTaskSQL;
        } else {
            sqlDeleteExpiredDataTask = SQL_DELETE_EXPIRED_DATA_TASK;
        }

        if (!enablePersist) {
            log.info("Session Data Persistence of Authentication framework is not enabled.");
        }
        String isCleanUpEnabledVal = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.SessionDataCleanUp.Enable");

        String isOperationCleanUpEnabledVal = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.OperationDataCleanUp.Enable");
        String operationCleanUpPeriodVal = IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.OperationDataCleanUp.CleanUpPeriod");


        if (StringUtils.isBlank(isCleanUpEnabledVal)) {
            isCleanUpEnabledVal = defaultCleanUpEnabled;
        }
        if (StringUtils.isBlank(isOperationCleanUpEnabledVal)) {
            isOperationCleanUpEnabledVal = defaultOperationCleanUpEnabled;
        }

        if (Boolean.parseBoolean(isCleanUpEnabledVal)) {
            long sessionCleanupPeriod = IdentityUtil.getCleanUpPeriod(
                    CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            SessionCleanUpService sessionCleanUpService = new SessionCleanUpService(sessionCleanupPeriod/4,
                    sessionCleanupPeriod);
            sessionCleanUpService.activateCleanUp();
        } else {
            log.info("Session Data CleanUp Task of Authentication framework is not enabled.");
        }
        if (Boolean.parseBoolean(isOperationCleanUpEnabledVal)) {
            if (StringUtils.isNotBlank(operationCleanUpPeriodVal)) {
                operationCleanUpPeriod = Long.parseLong(operationCleanUpPeriodVal);
            }
            OperationCleanUpService operationCleanUpService = new OperationCleanUpService(operationCleanUpPeriod/4,
                    operationCleanUpPeriod);
            operationCleanUpService.activateCleanUp();
        } else {
            log.info("Session Data Operations CleanUp Task of Authentication framework is not enabled.");
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
                if ((OPERATION_STORE.equals(operation))) {
                    return new SessionContextDO(key, type, getBlobObject(resultSet.getBinaryStream(2)), new Timestamp
                            (resultSet.getLong(3)));
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
        Timestamp timestamp = new Timestamp(new Date().getTime());
        if (maxPoolSize > 0) {
            sessionContextQueue.push(new SessionContextDO(key, type, entry, timestamp));
        } else {
            persistSessionData(key, type, entry, timestamp, tenantId);
        }
    }

    public void clearSessionData(String key, String type) {
        if (!enablePersist) {
            return;
        }
        Timestamp timestamp = new Timestamp(new Date().getTime());
        if (maxPoolSize > 0) {
            sessionContextQueue.push(new SessionContextDO(key, type, null, timestamp));
        } else {
            removeSessionData(key, type, timestamp);
        }
    }

    public void removeExpiredSessionData(Timestamp timestamp) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }
        try {
            statement = connection.prepareStatement(sqlDeleteExpiredDataTask);
            statement.setLong(1, timestamp.getTime()*1000000);
            statement.execute();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException e) {
            log.error("Error while removing session data from the database for the timestamp " + timestamp.toString(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, statement);

        }
    }

    public void removeExpiredOperationData(Timestamp timestamp) {
        deleteSTOREOperationsTask(timestamp);
        deleteDELETEOperationsTask(timestamp);
    }

    public void persistSessionData(String key, String type, Object entry, Timestamp timestamp, int tenantId) {
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
        ResultSet resultSet = null;

        // create a nano time stamp relative to Unix Epoch
        long currentStandardNano = timestamp.getTime() * 1000000;
        long currentSystemNano = System.nanoTime();

        currentStandardNano = currentStandardNano + (currentSystemNano - FrameworkServiceDataHolder.getInstance()
                .getNanoTimeReference());

        try {
            preparedStatement = connection.prepareStatement(sqlInsertSTORE);
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            preparedStatement.setString(3, OPERATION_STORE);
            setBlobObject(preparedStatement, entry, 4);
            preparedStatement.setLong(5, currentStandardNano);
            preparedStatement.setInt(6, tenantId);
            preparedStatement.executeUpdate();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
        } catch (SQLException | IOException e) {
            log.error("Error while storing session data", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, resultSet, preparedStatement);
        }
    }

    public void removeSessionData(String key, String type, Timestamp timestamp) {
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
        // create a nano time stamp relative to Unix Epoch
        long currentStandardNano = timestamp.getTime() * 1000000;
        long currentSystemNano = System.nanoTime();
        currentStandardNano = currentStandardNano + (currentSystemNano - FrameworkServiceDataHolder.getInstance()
                .getNanoTimeReference());
        try {
            preparedStatement = connection.prepareStatement(sqlInsertDELETE);
            preparedStatement.setString(1, key);
            preparedStatement.setString(2, type);
            preparedStatement.setString(3, OPERATION_DELETE);
            preparedStatement.setLong(4, currentStandardNano);
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

    private void deleteSTOREOperationsTask(Timestamp timestamp) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }
        try {
            if (StringUtils.isBlank(sqlDeleteSTORETask)) {
                if (connection.getMetaData().getDriverName().contains(MYSQL_DATABASE)) {
                    sqlDeleteSTORETask = SQL_DELETE_STORE_OPERATIONS_TASK_MYSQL;
                } else {
                    sqlDeleteSTORETask = SQL_DELETE_STORE_OPERATIONS_TASK;
                }
            }
            statement = connection.prepareStatement(sqlDeleteSTORETask);
            statement.setLong(1, timestamp.getTime());
            statement.execute();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            return;
        } catch (SQLException e) {
            log.error("Error while removing STORE operation data from the database for the timestamp " + timestamp.toString(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, statement);

        }

    }

    private void deleteDELETEOperationsTask(Timestamp timestamp) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection();
        } catch (IdentityRuntimeException e) {
            log.error(e.getMessage(), e);
            return;
        }
        try {
            statement = connection.prepareStatement(sqlDeleteDELETETask);
            statement.setLong(1, timestamp.getTime());
            statement.execute();
            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            return;
        } catch (SQLException e) {
            log.error("Error while removing DELETE operation data from the database for the timestamp " + timestamp.toString(), e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, statement);

        }
    }
}
