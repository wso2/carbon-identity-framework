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
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
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
import java.util.Locale;

import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_DELETE_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_DELETE_EXPIRED_DEBUG_SESSIONS;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_GET_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_GET_DEBUG_SESSION_LEGACY;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_INSERT_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_INSERT_DEBUG_SESSION_LEGACY;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_UPSERT_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_UPSERT_DEBUG_SESSION_LEGACY;

/**
 * Implementation of the DebugSessionDAO using JdbcTemplate.
 */
public class DebugSessionDAOImpl implements DebugSessionDAO {

    private static final Log LOG = LogFactory.getLog(DebugSessionDAOImpl.class);
    
    @Override
    public void createDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(sessionData.getDebugId());
        String storageDebugId = toStorageDebugId(normalizedDebugId);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        try {
            jdbcTemplate.executeUpdate(SQL_INSERT_DEBUG_SESSION, preparedStatement -> {
                preparedStatement.setString(1, storageDebugId);
                preparedStatement.setString(2, sessionData.getStatus());
                setSessionData(preparedStatement, 3, sessionData);
                preparedStatement.setString(4, sessionData.getResultJson());
                preparedStatement.setTimestamp(5, new Timestamp(sessionData.getCreatedTime()));
                preparedStatement.setTimestamp(6, new Timestamp(sessionData.getExpiryTime()));
                preparedStatement.setString(7, sessionData.getResourceType());
                preparedStatement.setString(8, sessionData.getConnectionId());
            });
        } catch (DataAccessException e) {
            // Check if error is due to missing columns and fallback
            if (isMissingColumnError(e, DebugFrameworkConstants.DB_COLUMN_RESOURCE_TYPE,
                    DebugFrameworkConstants.DB_COLUMN_CONNECTION_ID)) {
                LOG.warn("Column RESOURCE_TYPE not found in IDN_DEBUG_SESSION. " +
                        "Falling back to legacy insert without resource info.");
                try {
                    jdbcTemplate.executeUpdate(SQL_INSERT_DEBUG_SESSION_LEGACY, preparedStatement -> {
                        preparedStatement.setString(1, storageDebugId);
                        preparedStatement.setString(2, sessionData.getStatus());
                        setSessionData(preparedStatement, 3, sessionData);
                        preparedStatement.setString(4, sessionData.getResultJson());
                        preparedStatement.setTimestamp(5, new Timestamp(sessionData.getCreatedTime()));
                        preparedStatement.setTimestamp(6, new Timestamp(sessionData.getExpiryTime()));
                    });
                    return;
                } catch (DataAccessException ex) {
                    // Log the fallback error
                    String errorMsg = "Error while creating debug session (fallback): " + sessionData.getDebugId();
                    LOG.error(errorMsg, ex);
                    throw new DebugFrameworkServerException(errorMsg, ex);
                }
            }
            String errorMsg = "Error while creating debug session: " + sessionData.getDebugId();
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public DebugSessionData getDebugSession(String debugId) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(debugId);
        String storageDebugId = toStorageDebugId(normalizedDebugId);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        try {
            DebugSessionData data = getDebugSessionFromDatabase(jdbcTemplate, SQL_GET_DEBUG_SESSION,
                    debugId, storageDebugId, true);
            if (data != null) {
                return data;
            }
            // Backward compatibility for rows persisted without tenant scope.
            if (!storageDebugId.equals(normalizedDebugId)) {
                return getDebugSessionFromDatabase(jdbcTemplate, SQL_GET_DEBUG_SESSION,
                        debugId, normalizedDebugId, true);
            }
            return null;
        } catch (DataAccessException e) {
            // Check if error is due to missing columns and fallback
            if (isMissingColumnError(e, DebugFrameworkConstants.DB_COLUMN_RESOURCE_TYPE,
                    DebugFrameworkConstants.DB_COLUMN_CONNECTION_ID)) {
                LOG.warn("Column RESOURCE_TYPE not found in IDN_DEBUG_SESSION. " +
                        "Falling back to legacy retrieval without resource info.");
                return getDebugSessionLegacyFallback(jdbcTemplate, debugId, storageDebugId, normalizedDebugId);
            }
            String errorMsg = "Error while retrieving debug session: " + debugId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public void deleteDebugSession(String debugId) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(debugId);
        String storageDebugId = toStorageDebugId(normalizedDebugId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQL_DELETE_DEBUG_SESSION)) {
                prepStmt.setString(1, storageDebugId);
                int deletedCount = prepStmt.executeUpdate();
                if (deletedCount == 0 && !storageDebugId.equals(normalizedDebugId)) {
                    prepStmt.setString(1, normalizedDebugId);
                    prepStmt.executeUpdate();
                }

                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Unexpected error while deleting debug session: " + debugId;
            LOG.error(errorMsg, e);
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
                try (PreparedStatement prepStmt = connection.prepareStatement(SQL_UPSERT_DEBUG_SESSION)) {
                    prepStmt.setString(1, storageDebugId);
                    prepStmt.setString(2, sessionData.getStatus());
                    setSessionData(prepStmt, 3, sessionData);
                    prepStmt.setString(4, sessionData.getResultJson());
                    prepStmt.setTimestamp(5, new Timestamp(sessionData.getCreatedTime()));
                    prepStmt.setTimestamp(6, new Timestamp(sessionData.getExpiryTime()));
                    prepStmt.setString(7, sessionData.getResourceType());
                    prepStmt.setString(8, sessionData.getConnectionId());

                    prepStmt.executeUpdate();
                    success = true;
                }
            } catch (SQLException e) {
                if (isMissingColumnError(e, DebugFrameworkConstants.DB_COLUMN_RESOURCE_TYPE,
                        DebugFrameworkConstants.DB_COLUMN_CONNECTION_ID)) {
                    LOG.warn("Column RESOURCE_TYPE not found in IDN_DEBUG_SESSION. " +
                            "Falling back to legacy upsert without resource info.");
                    try (PreparedStatement prepStmt = connection.prepareStatement(SQL_UPSERT_DEBUG_SESSION_LEGACY)) {
                        prepStmt.setString(1, storageDebugId);
                        prepStmt.setString(2, sessionData.getStatus());
                        setSessionData(prepStmt, 3, sessionData);
                        prepStmt.setString(4, sessionData.getResultJson());
                        prepStmt.setTimestamp(5, new Timestamp(sessionData.getCreatedTime()));
                        prepStmt.setTimestamp(6, new Timestamp(sessionData.getExpiryTime()));

                        prepStmt.executeUpdate();
                        success = true;
                    }
                } else {
                    throw e;
                }
            }

            if (success) {
                // Ensure commit happens
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug session upserted successfully: " + normalizedDebugId);
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error while upserting debug session: " + sessionData.getDebugId();
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
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
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    /**
     * Retrieves a debug session from the database using the specified SQL query and identifier.
     *
     * @param jdbcTemplate The JDBC template.
     * @param sqlQuery The SQL query to execute.
     * @param debugId The debug ID.
     * @param identifier The identifier (storage debug ID or normalized debug ID).
     * @param includeResourceColumns Whether to include resource type and connection ID columns.
     * @return The debug session data or null if not found.
     * @throws DataAccessException If database access fails.
     */
    private DebugSessionData getDebugSessionFromDatabase(JdbcTemplate jdbcTemplate, String sqlQuery,
                                                         String debugId, String identifier,
                                                         boolean includeResourceColumns)
            throws DataAccessException {

        return jdbcTemplate.fetchSingleRecord(sqlQuery, (resultSet, rowNumber) ->
                mapResultSetToDebugSessionData(resultSet, debugId, includeResourceColumns),
                preparedStatement -> preparedStatement.setString(1, identifier));
    }

    /**
     * Attempts to retrieve a debug session from the legacy database schema as a fallback.
     *
     * @param jdbcTemplate The JDBC template.
     * @param debugId The debug ID.
     * @param storageDebugId The storage debug ID.
     * @param normalizedDebugId The normalized debug ID.
     * @return The debug session data or null if not found.
     * @throws DebugFrameworkServerException If database access fails.
     */
    private DebugSessionData getDebugSessionLegacyFallback(JdbcTemplate jdbcTemplate, String debugId,
                            String storageDebugId, String normalizedDebugId) throws DebugFrameworkServerException {

        try {
            DebugSessionData data = getDebugSessionFromDatabase(jdbcTemplate, SQL_GET_DEBUG_SESSION_LEGACY,
                    debugId, storageDebugId, false);
            if (data != null) {
                return data;
            }
            if (!storageDebugId.equals(normalizedDebugId)) {
                return getDebugSessionFromDatabase(jdbcTemplate, SQL_GET_DEBUG_SESSION_LEGACY,
                        debugId, normalizedDebugId, false);
            }
            return null;
        } catch (DataAccessException ex) {
            String errorMsg = "Error while retrieving debug session (fallback): " + debugId;
            LOG.error(errorMsg, ex);
            throw new DebugFrameworkServerException(errorMsg, ex);
        }
    }

    /**
     * Maps a ResultSet row to a DebugSessionData object.
     *
     * @param resultSet The result set.
     * @param debugId The debug ID.
     * @param includeResourceColumns Whether to include resource type and connection ID columns.
     * @return The debug session data.
     * @throws SQLException If a SQL error occurs.
     */
    private DebugSessionData mapResultSetToDebugSessionData(ResultSet resultSet, String debugId,
                                                           boolean includeResourceColumns) throws SQLException {

        DebugSessionData sessionData = new DebugSessionData();
        sessionData.setDebugId(debugId);
        sessionData.setStatus(resultSet.getString(DebugFrameworkConstants.DB_COLUMN_STATUS));
        try {
            sessionData.setSessionData(resultSet.getBinaryStream(DebugFrameworkConstants.DB_COLUMN_SESSION_DATA));
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error reading session data stream for debug ID: " + debugId, e);
            }
        }
        sessionData.setResultJson(resultSet.getString(DebugFrameworkConstants.DB_COLUMN_RESULT_JSON));
        sessionData.setCreatedTime(getTimeInMillis(resultSet, DebugFrameworkConstants.DB_COLUMN_CREATED_TIME));
        sessionData.setExpiryTime(getTimeInMillis(resultSet, DebugFrameworkConstants.DB_COLUMN_EXPIRY_TIME));

        if (includeResourceColumns) {
            sessionData.setResourceType(resultSet.getString(DebugFrameworkConstants.DB_COLUMN_RESOURCE_TYPE));
            sessionData.setConnectionId(resultSet.getString(DebugFrameworkConstants.DB_COLUMN_CONNECTION_ID));
        }
        return sessionData;
    }

    /**
     * Normalizes the debug ID by removing hyphens from the UUID part.
     * This ensures consistent storage and retrieval regardless of format.
     *
     * @param debugId The debug ID to normalize.
     * @return Normalized debug ID (e.g., debug-32charuuid).
     */
    private String normalizeDebugId(String debugId) {

        if (debugId == null || !debugId.startsWith(DebugFrameworkConstants.DEBUG_PREFIX)) {
            return debugId;
        }

        String uuidPart = debugId.substring(DebugFrameworkConstants.DEBUG_PREFIX.length());
        String normalizedUuid = uuidPart.replace("-", "");
        return DebugFrameworkConstants.DEBUG_PREFIX + normalizedUuid;
    }

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
        preparedStatement.setBinaryStream(parameterIndex, sessionData.getSessionData());
    }

    private long getTimeInMillis(ResultSet resultSet, String columnName) throws SQLException {

        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.getTime() : 0L;
    }

    private boolean isMissingColumnError(Throwable throwable, String... columnHints) {

        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException) {
                SQLException sqlException = (SQLException) current;
                if (isMissingColumnSQLState(sqlException) || isMissingColumnMessage(sqlException, columnHints)) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Checks if the SQL exception represents a missing column error based on SQL state codes.
     *
     * @param sqlException The SQL exception to check.
     * @return True if the SQL state indicates a missing column error, false otherwise.
     */
    private boolean isMissingColumnSQLState(SQLException sqlException) {

        String sqlState = sqlException.getSQLState();
        return "42S22".equals(sqlState) || "42703".equals(sqlState);
    }

    /**
     * Checks if the SQL exception message indicates a missing column error.
     *
     * @param sqlException The SQL exception to check.
     * @param columnHints Optional column name hints to match in the message.
     * @return True if the message indicates a missing column error, false otherwise.
     */
    private boolean isMissingColumnMessage(SQLException sqlException, String... columnHints) {

        String message = sqlException.getMessage();
        if (message == null) {
            return false;
        }

        String normalizedMessage = message.toLowerCase(Locale.ENGLISH);
        boolean mentionsColumn = containsColumnHint(normalizedMessage, columnHints);
        if (!mentionsColumn) {
            return false;
        }

        return isMissingColumnPattern(normalizedMessage);
    }

    /**
     * Checks if the normalized message contains any of the column hints.
     *
     * @param normalizedMessage The normalized (lowercase) message.
     * @param columnHints Column name hints to check.
     * @return True if the message contains any hint, false otherwise.
     */
    private boolean containsColumnHint(String normalizedMessage, String... columnHints) {

        if (columnHints == null || columnHints.length == 0) {
            return true;
        }

        for (String columnHint : columnHints) {
            if (columnHint != null && normalizedMessage.contains(columnHint.toLowerCase(Locale.ENGLISH))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the message contains patterns that indicate a missing column error.
     *
     * @param normalizedMessage The normalized (lowercase) message.
     * @return True if the message matches missing column error patterns, false otherwise.
     */
    private boolean isMissingColumnPattern(String normalizedMessage) {

        return normalizedMessage.contains("not found")
                || normalizedMessage.contains("does not exist")
                || normalizedMessage.contains("unknown column")
                || normalizedMessage.contains("invalid column")
                || normalizedMessage.contains("invalid identifier")
                || normalizedMessage.contains("no such column");
    }
}
