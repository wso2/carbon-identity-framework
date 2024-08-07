/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.entitlement.dao.puredao;

import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.DatabaseTypes.DB2;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.DatabaseTypes.H2;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.DatabaseTypes.MARIADB;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.DatabaseTypes.MSSQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.DatabaseTypes.MYSQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.DatabaseTypes.ORACLE;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.DatabaseTypes.POSTGRES;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.IS_SUCCESS;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.LOGGED_AT;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.MESSAGE;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.POLICY_ID;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.POLICY_VERSION;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.STATUS_TYPE;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.SUBSCRIBER_ID;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.TARGET;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.TARGET_ACTION;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.TENANT_ID;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.USER;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.EntitlementTableColumns.VERSION;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.KEY;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.LIMIT;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.CREATE_POLICY_STATUS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.CREATE_SUBSCRIBER_STATUS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.DELETE_OLD_POLICY_STATUSES_MSSQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.DELETE_OLD_POLICY_STATUSES_MYSQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.DELETE_OLD_POLICY_STATUSES_ORACLE;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.DELETE_OLD_SUBSCRIBER_STATUSES_MSSQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.DELETE_OLD_SUBSCRIBER_STATUSES_MYSQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.DELETE_OLD_SUBSCRIBER_STATUSES_ORACLE;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.DELETE_POLICY_STATUS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.DELETE_SUBSCRIBER_STATUS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.GET_POLICY_STATUS_COUNT_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.GET_POLICY_STATUS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.GET_SUBSCRIBER_STATUS_COUNT_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.SQLQueries.GET_SUBSCRIBER_STATUS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.DAOConstants.STATUS_COUNT;

import static java.time.ZoneOffset.UTC;

/**
 * This class handles the JDBC operations related to the status data.
 */
public class StatusPureDAO {

    /**
     * DAO method to delete all status records.
     *
     * @param about whether the status is about a policy or publisher.
     * @param key   key value of the status.
     * @throws EntitlementException if fails to delete.
     */
    public void deleteStatusTrail(String about, String key, int tenantId) throws EntitlementException {

        String query = EntitlementConstants.Status.ABOUT_POLICY.equals(about) ?
                DELETE_POLICY_STATUS_SQL : DELETE_SUBSCRIBER_STATUS_SQL;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement deleteStatusPrepStmt = new NamedPreparedStatement(connection, query)) {
                deleteStatusPrepStmt.setString(KEY, key);
                deleteStatusPrepStmt.setInt(TENANT_ID, tenantId);
                deleteStatusPrepStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new EntitlementException("Error while deleting policy status", e);
        }
    }

    /**
     * DAO method to get the status records.
     *
     * @param key      key value of the status.
     * @param about    whether the status is about a policy or publisher.
     * @param tenantId tenant id.
     * @return list of status holders.
     * @throws EntitlementException if fails to get status.
     */
    public List<StatusHolder> getStatus(String key, String about, int tenantId) throws EntitlementException {

        List<StatusHolder> statusHolders = new ArrayList<>();
        String query = EntitlementConstants.Status.ABOUT_POLICY.equals(about)
                ? GET_POLICY_STATUS_SQL
                : GET_SUBSCRIBER_STATUS_SQL;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement getStatusPrepStmt = new NamedPreparedStatement(connection, query)) {
                getStatusPrepStmt.setString(KEY, key);
                getStatusPrepStmt.setInt(TENANT_ID, tenantId);
                try (ResultSet statusSet = getStatusPrepStmt.executeQuery()) {
                    while (statusSet.next()) {
                        StatusHolder statusHolder = new StatusHolder(about);
                        statusHolder.setType(statusSet.getString(STATUS_TYPE));
                        statusHolder.setSuccess(statusSet.getBoolean(IS_SUCCESS));
                        statusHolder.setUser(statusSet.getString(USER));
                        statusHolder.setTarget(statusSet.getString(TARGET));
                        statusHolder.setTargetAction(statusSet.getString(TARGET_ACTION));
                        statusHolder.setTimeInstance(String.valueOf(statusSet.getTimestamp(LOGGED_AT).getTime()));
                        statusHolder.setMessage(statusSet.getString(MESSAGE));

                        if (EntitlementConstants.Status.ABOUT_POLICY.equals(about)) {
                            statusHolder.setKey(statusSet.getString(POLICY_ID));
                            int version = statusSet.getInt(POLICY_VERSION);
                            if (version != -1) {
                                statusHolder.setVersion(Integer.toString(version));
                            }
                        } else {
                            statusHolder.setKey(statusSet.getString(SUBSCRIBER_ID));
                        }
                        statusHolders.add(statusHolder);
                    }
                }
                return statusHolders;
            }
        } catch (SQLException e) {
            throw new EntitlementException("Error while retrieving policy status", e);
        }
    }

    /**
     * DAO method to insert status records.
     *
     * @param about         whether the status is about a policy or publisher.
     * @param key           key value of the status.
     * @param statusHolders list of status holders.
     * @param tenantId      tenant id.
     * @throws EntitlementException if fails to insert status.
     */
    public void insertStatus(String about, String key, List<StatusHolder> statusHolders, int tenantId)
            throws EntitlementException {

        String query = EntitlementConstants.Status.ABOUT_POLICY.equals(about)
                ? CREATE_POLICY_STATUS_SQL
                : CREATE_SUBSCRIBER_STATUS_SQL;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement addStatusPrepStmt = new NamedPreparedStatement(connection, query)) {
                for (StatusHolder statusHolder : statusHolders) {

                    int version = -1;
                    if (statusHolder.getVersion() != null) {
                        version = Integer.parseInt(statusHolder.getVersion());
                    }

                    addStatusPrepStmt.setString(KEY, key);
                    addStatusPrepStmt.setString(STATUS_TYPE, statusHolder.getType());
                    addStatusPrepStmt.setBoolean(IS_SUCCESS, statusHolder.isSuccess());
                    addStatusPrepStmt.setString(USER, statusHolder.getUser());
                    addStatusPrepStmt.setString(TARGET, statusHolder.getTarget());
                    addStatusPrepStmt.setString(TARGET_ACTION, statusHolder.getTargetAction());
                    addStatusPrepStmt.setString(MESSAGE, statusHolder.getMessage());
                    addStatusPrepStmt.setTimeStamp(LOGGED_AT, new Timestamp(System.currentTimeMillis()),
                            Calendar.getInstance(TimeZone.getTimeZone(UTC)));
                    if (EntitlementConstants.Status.ABOUT_POLICY.equals(about)) {
                        addStatusPrepStmt.setInt(VERSION, version);
                    }
                    addStatusPrepStmt.setInt(TENANT_ID, tenantId);

                    addStatusPrepStmt.addBatch();
                }
                addStatusPrepStmt.executeBatch();
            }
        } catch (SQLException e) {
            throw new EntitlementException("Error while persisting policy status", e);
        }
    }

    /**
     * DAO method to delete excess status records (if surpassing maximum, excess number of old records are deleted).
     *
     * @param about    whether the status is about a policy or publisher.
     * @param key      key value of the status.
     * @param tenantId tenant id.
     * @throws EntitlementException if fails to delete.
     */
    public void deleteExcessStatusData(String about, String key, int tenantId, int maxRecords)
            throws EntitlementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        try {
            // Get the existing status count
            int statusCount = getStatusCount(connection, about, key, tenantId);

            // Delete old status data if the count exceeds the maximum records
            if (statusCount > maxRecords) {
                int statusCountToDelete = statusCount - maxRecords;
                deleteStatus(connection, about, key, statusCountToDelete, tenantId);
            }
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new EntitlementException("Error while deleting surplus policy status", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    private void deleteStatus(Connection connection, String about, String key, int statusCountToDelete, int tenantId)
            throws SQLException, EntitlementException {

        String query = resolveDeleteStatusQuery(connection, about);
        try (NamedPreparedStatement deleteOldRecordsPrepStmt = new NamedPreparedStatement(connection, query)) {
            deleteOldRecordsPrepStmt.setString(KEY, key);
            deleteOldRecordsPrepStmt.setInt(TENANT_ID, tenantId);
            deleteOldRecordsPrepStmt.setInt(LIMIT, statusCountToDelete);
            deleteOldRecordsPrepStmt.executeUpdate();
        }
    }

    private int getStatusCount(Connection connection, String about, String key, int tenantId)
            throws EntitlementException {

        int statusCount = 0;

        String query = EntitlementConstants.Status.ABOUT_POLICY.equals(about)
                ? GET_POLICY_STATUS_COUNT_SQL
                : GET_SUBSCRIBER_STATUS_COUNT_SQL;

        try (NamedPreparedStatement getStatusCountPrepStmt = new NamedPreparedStatement(connection, query)) {
            getStatusCountPrepStmt.setString(KEY, key);
            getStatusCountPrepStmt.setInt(TENANT_ID, tenantId);
            try (ResultSet count = getStatusCountPrepStmt.executeQuery()) {
                if (count.next()) {
                    statusCount = count.getInt(STATUS_COUNT);
                }
            }
        } catch (SQLException e) {
            throw new EntitlementException("Error while getting policy status count", e);
        }
        return statusCount;
    }

    private String resolveDeleteStatusQuery(Connection connection, String about)
            throws SQLException, EntitlementException {

        String databaseProductName = connection.getMetaData().getDatabaseProductName();

        Map<String, String> policyQueries = new HashMap<>();
        policyQueries.put(MYSQL, DELETE_OLD_POLICY_STATUSES_MYSQL);
        policyQueries.put(MARIADB, DELETE_OLD_POLICY_STATUSES_MYSQL);
        policyQueries.put(H2, DELETE_OLD_POLICY_STATUSES_MYSQL);
        policyQueries.put(MSSQL, DELETE_OLD_POLICY_STATUSES_MSSQL);
        policyQueries.put(ORACLE, DELETE_OLD_POLICY_STATUSES_ORACLE);
        policyQueries.put(POSTGRES, DELETE_OLD_POLICY_STATUSES_MYSQL);
        policyQueries.put(DB2, DELETE_OLD_POLICY_STATUSES_MYSQL);

        Map<String, String> subscriberQueries = new HashMap<>();
        subscriberQueries.put(MYSQL, DELETE_OLD_SUBSCRIBER_STATUSES_MYSQL);
        subscriberQueries.put(MARIADB, DELETE_OLD_SUBSCRIBER_STATUSES_MYSQL);
        subscriberQueries.put(H2, DELETE_OLD_SUBSCRIBER_STATUSES_MYSQL);
        subscriberQueries.put(MSSQL, DELETE_OLD_SUBSCRIBER_STATUSES_MSSQL);
        subscriberQueries.put(ORACLE, DELETE_OLD_SUBSCRIBER_STATUSES_ORACLE);
        subscriberQueries.put(POSTGRES, DELETE_OLD_POLICY_STATUSES_MYSQL);
        subscriberQueries.put(DB2, DELETE_OLD_POLICY_STATUSES_MYSQL);

        String query;
        if (EntitlementConstants.Status.ABOUT_POLICY.equals(about)) {
            query = policyQueries.get(databaseProductName);
        } else {
            query = subscriberQueries.get(databaseProductName);
        }

        if (query == null) {
            throw new EntitlementException("Database driver could not be identified or not supported.");
        }
        return query;
    }
}
