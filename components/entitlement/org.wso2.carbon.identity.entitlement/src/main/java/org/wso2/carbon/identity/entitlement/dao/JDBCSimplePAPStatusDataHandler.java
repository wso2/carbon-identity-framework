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
package org.wso2.carbon.identity.entitlement.dao;

import org.apache.commons.logging.Log;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.IS_SUCCESS;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.MESSAGE;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.POLICY_ID;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.POLICY_VERSION;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.STATUS_COUNT;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.STATUS_TYPE;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.SUBSCRIBER_ID;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.TARGET;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.TARGET_ACTION;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.TENANT_ID;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.TIME_INSTANCE;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.USER;
import static org.wso2.carbon.identity.entitlement.PDPConstants.EntitlementTableColumns.VERSION;
import static org.wso2.carbon.identity.entitlement.PDPConstants.KEY;
import static org.wso2.carbon.identity.entitlement.PDPConstants.LIMIT;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.CREATE_POLICY_STATUS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.CREATE_SUBSCRIBER_STATUS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.DELETE_OLD_POLICY_STATUSES_MSSQL;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.DELETE_OLD_POLICY_STATUSES_MYSQL;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.DELETE_OLD_POLICY_STATUSES_ORACLE;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.DELETE_OLD_SUBSCRIBER_STATUSES_MSSQL;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.DELETE_OLD_SUBSCRIBER_STATUSES_MYSQL;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.DELETE_OLD_SUBSCRIBER_STATUSES_ORACLE;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.DELETE_POLICY_STATUS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.DELETE_SUBSCRIBER_STATUS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.GET_POLICY_STATUS_COUNT_SQL;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.GET_POLICY_STATUS_SQL;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.GET_SUBSCRIBER_STATUS_COUNT_SQL;
import static org.wso2.carbon.identity.entitlement.dao.SQLQueries.GET_SUBSCRIBER_STATUS_SQL;

public class JDBCSimplePAPStatusDataHandler implements PAPStatusDataHandler {

    private static final Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;
    private static final String AUDIT_MESSAGE
            = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";
    private int maxRecords;

    /**
     * init entitlement status data handler module
     *
     * @param properties properties
     */
    @Override
    public void init(Properties properties) {

        maxRecords = EntitlementUtil.getMaxNoOfStatusRecords();
    }

    /**
     * Handles
     *
     * @param about        indicates what is related with this admin status action
     * @param key          key value of the status
     * @param statusHolder <code>StatusHolder</code>
     * @throws EntitlementException throws, if fails to handle
     */
    @Override
    public void handle(String about, String key, List<StatusHolder> statusHolder) throws EntitlementException {

        // If the action is DELETE_POLICY, delete the policy or the subscriber status
        for (StatusHolder holder : statusHolder) {
            if (EntitlementConstants.StatusTypes.DELETE_POLICY.equals(holder.getType())) {
                deletePersistedData(about, key);
                return;
            }
        }
        persistStatus(about, key, statusHolder);
    }

    /**
     * Handles
     *
     * @param about        indicates what is related with this admin status action
     * @param statusHolder <code>StatusHolder</code>
     * @throws EntitlementException if fails to handle
     */
    @Override
    public void handle(String about, StatusHolder statusHolder) throws EntitlementException {

        List<StatusHolder> list = new ArrayList<>();
        list.add(statusHolder);
        handle(about, statusHolder.getKey(), list);
    }

    /**
     * Returns status data
     *
     * @param about        indicates what is related with this admin status action
     * @param key          key value of the status
     * @param type         admin action type
     * @param searchString search string for <code>StatusHolder</code>
     * @return An array of <code>StatusHolder</code>
     * @throws EntitlementException if fails
     */
    @Override
    public StatusHolder[] getStatusData(String about, String key, String type, String searchString)
            throws EntitlementException {

        String statusAboutType = EntitlementConstants.Status.ABOUT_POLICY.equals(about)
                ? EntitlementConstants.Status.ABOUT_POLICY
                : EntitlementConstants.Status.ABOUT_SUBSCRIBER;

        List<StatusHolder> holders = readStatus(key, statusAboutType);
        List<StatusHolder> filteredHolders = new ArrayList<>();
        if (!holders.isEmpty()) {
            searchString = searchString.replace("*", ".*");
            Pattern pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
            for (StatusHolder holder : holders) {
                String id = EntitlementConstants.Status.ABOUT_POLICY.equals(about)
                        ? holder.getUser()
                        : holder.getTarget();
                Matcher matcher = pattern.matcher(id);
                if (!matcher.matches()) {
                    continue;
                }
                if (!EntitlementConstants.Status.ABOUT_POLICY.equals(about) || type == null ||
                        type.equals(holder.getType())) {
                    filteredHolders.add(holder);
                }
            }
        }
        return filteredHolders.toArray(new StatusHolder[0]);
    }

    private synchronized void deletePersistedData(String about, String key) throws EntitlementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String query = EntitlementConstants.Status.ABOUT_POLICY.equals(about) ?
                DELETE_POLICY_STATUS_SQL : DELETE_SUBSCRIBER_STATUS_SQL;
        try (NamedPreparedStatement deleteStatusPrepStmt = new NamedPreparedStatement(connection, query)) {
            deleteStatusPrepStmt.setString(KEY, key);
            deleteStatusPrepStmt.setInt(TENANT_ID, tenantId);
            deleteStatusPrepStmt.executeUpdate();

            IdentityDatabaseUtil.commitTransaction(connection);

        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new EntitlementException("Error while deleting policy status", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    private synchronized void persistStatus(String about, String key, List<StatusHolder> statusHolders)
            throws EntitlementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);

        try {
            boolean useLastStatusOnly = Boolean.parseBoolean(
                    IdentityUtil.getProperty(EntitlementConstants.PROP_USE_LAST_STATUS_ONLY));

            if (statusHolders != null && !statusHolders.isEmpty()) {

                if (useLastStatusOnly) {

                    // Delete the previous status
                    deletePersistedData(about, key);
                    auditAction(statusHolders.toArray(new StatusHolder[0]));
                }

                // Add new status to the database
                addStatus(connection, about, key, statusHolders);

                if (!useLastStatusOnly) {
                    // Get the existing status count
                    int statusCount = getStatusCount(connection, about, key);

                    // Delete old status data if the count exceeds the maximum records
                    if (statusCount > maxRecords) {
                        deleteStatus(connection, about, key, statusCount);
                    }
                }
            }

            IdentityDatabaseUtil.commitTransaction(connection);

        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new EntitlementException("Error while persisting policy status", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }

    }

    private synchronized List<StatusHolder> readStatus(String key, String about) throws EntitlementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<StatusHolder> statusHolders = new ArrayList<>();
        String query = EntitlementConstants.Status.ABOUT_POLICY.equals(about)
                ? GET_POLICY_STATUS_SQL
                : GET_SUBSCRIBER_STATUS_SQL;

        try (NamedPreparedStatement getStatusPrepStmt = new NamedPreparedStatement(connection, query)) {
            getStatusPrepStmt.setString(KEY, key);
            getStatusPrepStmt.setInt(TENANT_ID, tenantId);
            try (ResultSet statusSet = getStatusPrepStmt.executeQuery()) {
                while (statusSet.next()) {
                    StatusHolder statusHolder = new StatusHolder(about);
                    if (EntitlementConstants.Status.ABOUT_POLICY.equals(about)) {
                        statusHolder.setKey(statusSet.getString(POLICY_ID));
                    } else {
                        statusHolder.setKey(
                                statusSet.getString(SUBSCRIBER_ID));
                    }
                    statusHolder.setType(statusSet.getString(STATUS_TYPE));
                    statusHolder.setSuccess(statusSet.getBoolean(IS_SUCCESS));
                    statusHolder.setUser(statusSet.getString(USER));
                    statusHolder.setTarget(statusSet.getString(TARGET));
                    statusHolder.setTargetAction(statusSet.getString(TARGET_ACTION));
                    statusHolder.setTimeInstance(statusSet.getString(TIME_INSTANCE));
                    statusHolder.setMessage(statusSet.getString(MESSAGE));

                    String version;
                    if (statusSet.getInt(POLICY_VERSION) == -1) {
                        version = "";
                    } else {
                        version = Integer.toString(statusSet.getInt(POLICY_VERSION));
                    }
                    statusHolder.setVersion(version);
                    statusHolders.add(statusHolder);
                }
            }

            return statusHolders;

        } catch (SQLException e) {
            throw new EntitlementException("Error while retrieving policy status", e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    private void addStatus(Connection connection, String about, String key,
                           List<StatusHolder> statusHolders) throws SQLException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String query = EntitlementConstants.Status.ABOUT_POLICY.equals(about)
                ? CREATE_POLICY_STATUS_SQL
                : CREATE_SUBSCRIBER_STATUS_SQL;

        try (NamedPreparedStatement addStatusPrepStmt = new NamedPreparedStatement(connection, query)) {
            for (StatusHolder statusHolder : statusHolders) {

                String message = "";
                if (statusHolder.getMessage() != null) {
                    message = statusHolder.getMessage();
                }
                String target = "";
                if (statusHolder.getTarget() != null) {
                    target = statusHolder.getTarget();
                }
                String targetAction = "";
                if (statusHolder.getTargetAction() != null) {
                    targetAction = statusHolder.getTargetAction();
                }
                int version = -1;
                if (statusHolder.getVersion() != null) {
                    version = Integer.parseInt(statusHolder.getVersion());
                }

                addStatusPrepStmt.setString(STATUS_TYPE, statusHolder.getType());
                addStatusPrepStmt.setBoolean(IS_SUCCESS, statusHolder.isSuccess());
                addStatusPrepStmt.setString(USER, statusHolder.getUser());
                addStatusPrepStmt.setString(TARGET, target);
                addStatusPrepStmt.setString(TARGET_ACTION, targetAction);
                addStatusPrepStmt.setString(TIME_INSTANCE, Long.toString(System.currentTimeMillis()));
                addStatusPrepStmt.setString(MESSAGE, message);
                addStatusPrepStmt.setString(KEY, key);
                addStatusPrepStmt.setInt(TENANT_ID, tenantId);

                if (EntitlementConstants.Status.ABOUT_POLICY.equals(about)) {
                    addStatusPrepStmt.setInt(VERSION, version);
                }

                addStatusPrepStmt.addBatch();
            }
            addStatusPrepStmt.executeBatch();
        }
    }

    private int getStatusCount(Connection connection, String about, String key) throws SQLException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
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
        }
        return statusCount;
    }

    private void deleteStatus(Connection connection, String about, String key, int statusCount)
            throws SQLException, EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        int oldRecordsCount = statusCount - maxRecords;

        String query = resolveDeleteStatusQuery(connection, about);

        try (NamedPreparedStatement deleteOldRecordsPrepStmt = new NamedPreparedStatement(connection, query)) {
            deleteOldRecordsPrepStmt.setString(KEY, key);
            deleteOldRecordsPrepStmt.setInt(TENANT_ID, tenantId);
            deleteOldRecordsPrepStmt.setInt(LIMIT, oldRecordsCount);
            deleteOldRecordsPrepStmt.executeUpdate();
        }
    }

    private String resolveDeleteStatusQuery(Connection connection, String about)
            throws SQLException, EntitlementException {

        String databaseProductName = connection.getMetaData().getDatabaseProductName();

        Map<String, String> policyQueries = new HashMap<>();
        policyQueries.put("MySQL", DELETE_OLD_POLICY_STATUSES_MYSQL);
        policyQueries.put("MariaDB", DELETE_OLD_POLICY_STATUSES_MYSQL);
        policyQueries.put("H2", DELETE_OLD_POLICY_STATUSES_MYSQL);
        policyQueries.put("Microsoft SQL Server", DELETE_OLD_POLICY_STATUSES_MSSQL);
        policyQueries.put("Oracle", DELETE_OLD_POLICY_STATUSES_ORACLE);
        policyQueries.put("PostgreSQL", DELETE_OLD_POLICY_STATUSES_MYSQL);
        policyQueries.put("DB2", DELETE_OLD_POLICY_STATUSES_MYSQL);

        Map<String, String> subscriberQueries = new HashMap<>();
        subscriberQueries.put("MySQL", DELETE_OLD_SUBSCRIBER_STATUSES_MYSQL);
        subscriberQueries.put("MariaDB", DELETE_OLD_SUBSCRIBER_STATUSES_MYSQL);
        subscriberQueries.put("H2", DELETE_OLD_SUBSCRIBER_STATUSES_MYSQL);
        subscriberQueries.put("Microsoft SQL Server", DELETE_OLD_SUBSCRIBER_STATUSES_MSSQL);
        subscriberQueries.put("Oracle", DELETE_OLD_SUBSCRIBER_STATUSES_ORACLE);
        subscriberQueries.put("PostgreSQL", DELETE_OLD_POLICY_STATUSES_MYSQL);
        subscriberQueries.put("DB2", DELETE_OLD_POLICY_STATUSES_MYSQL);

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

    private void auditAction(StatusHolder[] statusHolders) {

        if (statusHolders != null) {
            for (StatusHolder statusHolder : statusHolders) {
                if (statusHolder != null) {
                    String initiator = statusHolder.getUser();
                    if (LoggerUtils.isLogMaskingEnable) {
                        initiator = LoggerUtils.getMaskedContent(initiator);
                    }
                    String action = statusHolder.getType();
                    String key = statusHolder.getKey();
                    String target = statusHolder.getTarget();
                    String targetAction = statusHolder.getTargetAction();
                    String result = "FAILURE";
                    if (statusHolder.isSuccess()) {
                        result = "SUCCESS";
                    }
                    String auditData = String.format("\"Key\" : \"%s\" , \"Target Action\" : \"%s\"",
                            key, targetAction);

                    AUDIT_LOG.info(String.format(AUDIT_MESSAGE, initiator, action, target, auditData, result));
                }
            }
        }
    }
}
