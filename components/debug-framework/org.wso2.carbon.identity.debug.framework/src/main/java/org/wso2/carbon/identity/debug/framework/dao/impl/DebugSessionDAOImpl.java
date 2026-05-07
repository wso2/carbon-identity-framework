/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.debug.framework.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_DELETE_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_DELETE_EXPIRED_DEBUG_SESSIONS;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_GET_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_INSERT_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_UPSERT_DEBUG_SESSION_H2;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_UPSERT_DEBUG_SESSION_MSSQL_OR_DB2;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_UPSERT_DEBUG_SESSION_MYSQL;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_UPSERT_DEBUG_SESSION_ORACLE;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_UPSERT_DEBUG_SESSION_POSTGRESQL;

/**
 * Implementation of the DebugSessionDAO using raw JDBC connections.
 * All database operations use IdentityDatabaseUtil.getDBConnection() for consistent connection management.
 */
public class DebugSessionDAOImpl implements DebugSessionDAO {

    private static final Log LOG = LogFactory.getLog(DebugSessionDAOImpl.class);

    private enum UpsertDbType {

        H2,
        MYSQL,
        POSTGRESQL,
        MSSQL_OR_DB2,
        ORACLE
    }

    @Override
    public void createDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(sessionData.getDebugId());
        String storageDebugId = toStorageDebugId(normalizedDebugId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQL_INSERT_DEBUG_SESSION)) {
                prepStmt.setString(1, storageDebugId);
                prepStmt.setString(2, sessionData.getStatus());
                setSessionData(prepStmt, 3, sessionData);
                prepStmt.setString(4, sessionData.getResultJson());
                prepStmt.setTimestamp(5, new Timestamp(sessionData.getCreatedTime()));
                prepStmt.setTimestamp(6, new Timestamp(sessionData.getExpiryTime()));
                prepStmt.executeUpdate();

                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error while creating debug session: " + sessionData.getDebugId();
            LOG.error(errorMsg + ". Cause: " + e.getMessage());
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public DebugSessionData getDebugSession(String debugId) throws DebugFrameworkServerException {

        String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
        if (tenantDomain == null || tenantDomain.trim().isEmpty()) {
            throw new DebugFrameworkServerException(
                    "Tenant domain could not be resolved for debug session retrieval. Debug ID: " + debugId);
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Retrieving debug session. Debug ID: " + debugId + ", Tenant: " + tenantDomain);
        }

        String normalizedDebugId = normalizeDebugId(debugId);
        String storageDebugId = toStorageDebugId(normalizedDebugId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQL_GET_DEBUG_SESSION)) {
                prepStmt.setString(1, storageDebugId);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    if (resultSet.next()) {
                        return mapResultSetToDebugSessionData(resultSet, debugId);
                    }
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error while retrieving debug session: " + debugId + " for tenant: " + tenantDomain;
            LOG.error(errorMsg + ". Cause: " + e.getMessage());
            throw new DebugFrameworkServerException(errorMsg, e);
        }
        return null;
    }

    @Override
    public void deleteDebugSession(String debugId) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(debugId);
        String storageDebugId = toStorageDebugId(normalizedDebugId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQL_DELETE_DEBUG_SESSION)) {
                prepStmt.setString(1, storageDebugId);
                prepStmt.executeUpdate();

                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Unexpected error while deleting debug session: " + debugId;
            LOG.error(errorMsg + ". Cause: " + e.getMessage());
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public void upsertDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(sessionData.getDebugId());
        String storageDebugId = toStorageDebugId(normalizedDebugId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            boolean success = false;
            try {
                try (PreparedStatement prepStmt = prepareUpsertStatement(connection)) {
                    setUpsertParameters(prepStmt, storageDebugId, sessionData);
                    prepStmt.executeUpdate();
                    success = true;
                }
            } catch (SQLException e) {
                throw e;
            }

            if (success) {
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug session upserted successfully: " + normalizedDebugId);
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error while upserting debug session: " + sessionData.getDebugId();
            LOG.error(errorMsg + ". Cause: " + e.getMessage());
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    private PreparedStatement prepareUpsertStatement(Connection connection)
            throws SQLException, DebugFrameworkServerException {

        UpsertDbType dbType = resolveUpsertDbType(connection);
        switch (dbType) {
            case MYSQL:
                return connection.prepareStatement(SQL_UPSERT_DEBUG_SESSION_MYSQL);
            case POSTGRESQL:
                return connection.prepareStatement(SQL_UPSERT_DEBUG_SESSION_POSTGRESQL);
            case ORACLE:
                return connection.prepareStatement(SQL_UPSERT_DEBUG_SESSION_ORACLE);
            case MSSQL_OR_DB2:
                return connection.prepareStatement(SQL_UPSERT_DEBUG_SESSION_MSSQL_OR_DB2);
            case H2:
            default:
                return connection.prepareStatement(SQL_UPSERT_DEBUG_SESSION_H2);
        }
    }

    private UpsertDbType resolveUpsertDbType(Connection connection) throws DebugFrameworkServerException {

        try {
            if (JdbcUtils.isMySQLDB(JdbcUtils.Database.IDENTITY) || JdbcUtils.isMariaDB(JdbcUtils.Database.IDENTITY)) {
                return UpsertDbType.MYSQL;
            }
            if (JdbcUtils.isPostgreSQLDB(JdbcUtils.Database.IDENTITY)) {
                return UpsertDbType.POSTGRESQL;
            }
            if (JdbcUtils.isOracleDB(JdbcUtils.Database.IDENTITY)) {
                return UpsertDbType.ORACLE;
            }
            if (JdbcUtils.isMSSqlDB(JdbcUtils.Database.IDENTITY) || JdbcUtils.isDB2DB(JdbcUtils.Database.IDENTITY)) {
                return UpsertDbType.MSSQL_OR_DB2;
            }
            if (JdbcUtils.isH2DB(JdbcUtils.Database.IDENTITY)) {
                return UpsertDbType.H2;
            }
        } catch (DataAccessException e) {
            throw new DebugFrameworkServerException("Error while resolving database type for debug session upsert.", e);
        }

        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            throw new DebugFrameworkServerException(
                    "Unsupported database type for debug session upsert: " + databaseProductName);
        } catch (SQLException e) {
            throw new DebugFrameworkServerException("Unsupported database type for debug session upsert.", e);
        }
    }

    private void setUpsertParameters(PreparedStatement prepStmt, String storageDebugId, DebugSessionData sessionData)
            throws SQLException {

        prepStmt.setString(1, storageDebugId);
        prepStmt.setString(2, sessionData.getStatus());
        setSessionData(prepStmt, 3, sessionData);
        prepStmt.setString(4, sessionData.getResultJson());
        prepStmt.setTimestamp(5, new Timestamp(sessionData.getCreatedTime()));
        prepStmt.setTimestamp(6, new Timestamp(sessionData.getExpiryTime()));
    }

    @Override
    public void deleteExpiredDebugSessions() throws DebugFrameworkServerException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQL_DELETE_EXPIRED_DEBUG_SESSIONS)) {
                prepStmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                int deletedCount = prepStmt.executeUpdate();

                if (!connection.getAutoCommit()) {
                    connection.commit();
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Deleted " + deletedCount + " expired debug sessions from database.");
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Unexpected error while deleting expired debug sessions";
            LOG.error(errorMsg + ". Cause: " + e.getMessage());
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }
    /**
     * Maps a ResultSet row to a DebugSessionData object.
     *
     * @param resultSet The result set.
     * @param debugId The debug ID (original value, without tenant prefix).
     * @return The debug session data.
     * @throws SQLException If a SQL error occurs.
     */
    private DebugSessionData mapResultSetToDebugSessionData(ResultSet resultSet, String debugId)
            throws SQLException {

        DebugSessionData sessionData = new DebugSessionData();
        sessionData.setDebugId(debugId);
        sessionData.setStatus(resultSet.getString(DebugFrameworkConstants.DB_COLUMN_STATUS));
        try {
            sessionData.setSessionData(resultSet.getBytes(DebugFrameworkConstants.DB_COLUMN_SESSION_DATA));
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error reading session data stream for debug ID: " + debugId, e);
            }
        }
        sessionData.setResultJson(resultSet.getString(DebugFrameworkConstants.DB_COLUMN_RESULT_JSON));
        sessionData.setCreatedTime(getTimeInMillis(resultSet, DebugFrameworkConstants.DB_COLUMN_CREATED_TIME));
        sessionData.setExpiryTime(getTimeInMillis(resultSet, DebugFrameworkConstants.DB_COLUMN_EXPIRY_TIME));
        return sessionData;
    }

    /**
     * Normalizes the debug ID by removing hyphens from the UUID part.
     * This ensures consistent storage format regardless of input format.
     *
     * @param debugId The debug ID to normalize.
     * @return Normalized debug ID (or original if not in expected format).
     */
    private String normalizeDebugId(String debugId) {

        if (debugId == null || !debugId.startsWith(DebugFrameworkConstants.DEBUG_PREFIX)) {
            return debugId;
        }

        String uuidPart = debugId.substring(DebugFrameworkConstants.DEBUG_PREFIX.length());
        String normalizedUuid = uuidPart.replace("-", "");
        return DebugFrameworkConstants.DEBUG_PREFIX + normalizedUuid;
    }

    /**
     * Converts a normalized debug ID to the storage key by adding tenant prefix.
     *
     * @param normalizedDebugId The normalized debug ID (without tenant prefix).
     * @return The storage key with tenant prefix, or the original if no tenant context.
     */
    private String toStorageDebugId(String normalizedDebugId) {

        if (normalizedDebugId == null) {
            return null;
        }
        String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
        if (tenantDomain != null && !tenantDomain.trim().isEmpty()) {
            return tenantDomain + ":" + normalizedDebugId;
        }
        return normalizedDebugId;
    }

    private void setSessionData(PreparedStatement preparedStatement, int parameterIndex,
                                DebugSessionData sessionData) throws SQLException {

        if (sessionData.getSessionData() == null) {
            preparedStatement.setNull(parameterIndex, Types.BLOB);
            return;
        }
        preparedStatement.setBytes(parameterIndex, sessionData.getSessionData());
    }

    private long getTimeInMillis(ResultSet resultSet, String columnName) throws SQLException {

        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp == null ? 0L : timestamp.getTime();
    }

}
