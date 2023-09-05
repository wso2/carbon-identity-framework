/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.application.authentication.framework.store;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Data store for Pushed Authorization Requests.
 */
public class PushedAuthDataStore {

    private static final Log log = LogFactory.getLog(PushedAuthDataStore.class);
    private static volatile PushedAuthDataStore instance;
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_MYSQL =
            "DELETE FROM IDN_OAUTH_PAR WHERE SCHEDULED_EXPIRY < ? LIMIT %d";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_MSSQL =
            "DELETE TOP (%d) FROM IDN_OAUTH_PAR WHERE SCHEDULED_EXPIRY < ?";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_POSTGRESQL = "DELETE FROM IDN_OAUTH_PAR WHERE " +
            "CTID IN (SELECT CTID FROM IDN_OAUTH_PAR WHERE SCHEDULED_EXPIRY < ? LIMIT %d)";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_ORACLE = "DELETE FROM IDN_OAUTH_PAR WHERE ROWID" +
            " IN (SELECT ROWID FROM IDN_OAUTH_PAR WHERE SCHEDULED_EXPIRY < ? AND ROWNUM <= %d)";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_INFOMIXSQL = "DELETE FROM (SELECT REQ_URI_REF FROM " +
            "IDN_OAUTH_PAR WHERE SCHEDULED_EXPIRY < ? LIMIT %d) ";
    private static final String SQL_DELETE_EXPIRED_DATA_TASK_DB2SQL =
            "DELETE FROM IDN_OAUTH_PAR WHERE REQ_URI_REF IN " +
                    "(SELECT REQ_URI_REF FROM IDN_OAUTH_PAR WHERE SCHEDULED_EXPIRY < ? FETCH FIRST %d ROWS ONLY)";
    private static final String MYSQL_DATABASE = "MySQL";
    private static final String MARIA_DATABASE = "MariaDB";
    private static final String H2_DATABASE = "H2";
    private static final String DB2_DATABASE = "DB2";
    private static final String MS_SQL_DATABASE = "MS SQL";
    private static final String MICROSOFT_DATABASE = "Microsoft";
    private static final String POSTGRESQL_DATABASE = "PostgreSQL";
    private static final String INFORMIX_DATABASE = "Informix";
    private static final int DEFAULT_DELETE_LIMIT = 50000;
    private static final int EXPIRATION_GRACE_PERIOD_IN_MINUTES = 10;
    private static final int DELETE_CHUNK_SIZE = DEFAULT_DELETE_LIMIT;
    private static final String CLEAN_UP_PERIOD_DEFAULT_IN_MINUTES = "1440";
    private String sqlDeleteExpiredDataTask;
    private boolean requestCleanupEnabled = true;

    /**
     * Gets the instance of PushedAuthDataStore.
     *
     * @return Instance of PushedAuthDataStore.
     */
    public static PushedAuthDataStore getInstance() {
        if (instance == null) {
            synchronized (PushedAuthDataStore.class) {
                if (instance == null) {
                    instance = new PushedAuthDataStore();
                }
            }
        }
        return instance;
    }

    private PushedAuthDataStore() {

        String isCleanUpEnabledVal
                = IdentityUtil.getProperty("JDBCPersistenceManager.PushedAuthReqCleanUp.Enable");
        if (StringUtils.isNotBlank(isCleanUpEnabledVal)) {
            requestCleanupEnabled = Boolean.parseBoolean(isCleanUpEnabledVal);
        }
        if (requestCleanupEnabled) {
            long requestCleanupPeriod = getCleanUpPeriod();
            if (log.isDebugEnabled()) {
                log.debug(String.format("PAR clean up task enabled to run in %d minutes intervals",
                        requestCleanupPeriod));
            }
            PushedAuthReqCleanupService pushedAuthReqCleanupService =
                    new PushedAuthReqCleanupService(requestCleanupPeriod / 4, requestCleanupPeriod);
            pushedAuthReqCleanupService.activateCleanUp();
        }
    }

    /**
     * Gets the DB specific query for the request removal task.
     *
     * @return DB specific query for the request removal task.
     * @throws IdentityApplicationManagementException Error when retrieving DB connection meta-data.
     */
    private String getDBSpecificRequestRemovalQuery() throws IdentityApplicationManagementException {

        Connection connection = null;
        try {
            connection = IdentityDatabaseUtil.getDBConnection(true);
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
            return String.format(nonFormattedQuery, DELETE_CHUNK_SIZE);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new IdentityApplicationManagementException("Error while retrieving DB connection meta-data", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, null);
        }
    }

    /**
     * Gets the cleanup period.
     *
     * @return Clean up period.
     */
    private long getCleanUpPeriod() {

        String cleanUpPeriod = IdentityUtil.getProperty("JDBCPersistenceManager.PushedAuthReqCleanUp.CleanUpPeriod");
        if (StringUtils.isBlank(cleanUpPeriod) || !StringUtils.isNumeric(cleanUpPeriod)) {
            cleanUpPeriod = CLEAN_UP_PERIOD_DEFAULT_IN_MINUTES;
        }
        return Long.parseLong(cleanUpPeriod);
    }

    /**
     * Removes the records related to expired requests from DB.
     *
     * @param sqlQuery SQL query to remove expired requests.
     */
    private void removeExpiredRequests(String sqlQuery) {

        if (log.isDebugEnabled()) {
            log.debug("DB query for removing expired data: " + sqlQuery);
        }
        long currentTime = FrameworkUtils.getCurrentStandardNano();
        long cleanUpTime = currentTime - EXPIRATION_GRACE_PERIOD_IN_MINUTES * 60 * 1000;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            boolean deleteCompleted = false;
            int totalDeletedEntries = 0;
            while (!deleteCompleted) {
                try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
                    statement.setLong(1, cleanUpTime);
                    int noOfDeletedRecords = statement.executeUpdate();
                    deleteCompleted = noOfDeletedRecords < DELETE_CHUNK_SIZE;
                    totalDeletedEntries += noOfDeletedRecords;
                    // Commit the chunk deletion.
                    IdentityDatabaseUtil.commitTransaction(connection);
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Removed %d expired requests.", noOfDeletedRecords));
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("Deleted total of %d entries", totalDeletedEntries));
            }
        } catch (SQLException | IdentityRuntimeException e) {
            log.error("Error while removing requests from the database for nano time: " + currentTime, e);
        }
    }

    /**
     * Cleans the expired pushed auth requests from the DB if enabled.
     */
    public void removeExpiredRequests() {

        if (StringUtils.isBlank(sqlDeleteExpiredDataTask)) {
            try {
                sqlDeleteExpiredDataTask = getDBSpecificRequestRemovalQuery();
            } catch (IdentityApplicationManagementException e) {
                log.error("Error when initializing the db specific cleanup query.", e);
            }
        }
        if (requestCleanupEnabled) {
            removeExpiredRequests(sqlDeleteExpiredDataTask);
        }
    }

}
