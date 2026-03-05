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
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.debug.framework.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Locale;

/**
 * Implementation of the DebugSessionDAO using JdbcTemplate.
 */
public class DebugSessionDAOImpl implements DebugSessionDAO {

    private static final Log LOG = LogFactory.getLog(DebugSessionDAOImpl.class);
    private static final String DEBUG_SESSION_PREFIX = "debug-";

    private static final String SQL_INSERT_DEBUG_SESSION = "INSERT INTO IDN_DEBUG_SESSION " +
            "(DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME, RESOURCE_TYPE, RESOURCE_ID) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_GET_DEBUG_SESSION = "SELECT DEBUG_ID, STATUS, SESSION_DATA, " +
            "RESULT_JSON, CREATED_TIME, EXPIRY_TIME, RESOURCE_TYPE, " +
            "RESOURCE_ID FROM IDN_DEBUG_SESSION WHERE DEBUG_ID = ?";

    private static final String SQL_DELETE_DEBUG_SESSION = "DELETE FROM IDN_DEBUG_SESSION WHERE DEBUG_ID = ?";

    private static final String SQL_DELETE_EXPIRED_DEBUG_SESSIONS 
        = "DELETE FROM IDN_DEBUG_SESSION WHERE EXPIRY_TIME < ?";

    // MERGE statement for atomic upsert (H2 and most databases support this)
    private static final String SQL_UPSERT_DEBUG_SESSION 
        = "MERGE INTO IDN_DEBUG_SESSION (DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, "
            +
            "CREATED_TIME, EXPIRY_TIME, RESOURCE_TYPE, RESOURCE_ID) KEY (DEBUG_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    // Legacy SQLs for backward compatibility (if DB schema is not updated)
    private static final String SQL_INSERT_DEBUG_SESSION_LEGACY = "INSERT INTO IDN_DEBUG_SESSION " +
            "(DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_GET_DEBUG_SESSION_LEGACY = "SELECT DEBUG_ID, STATUS, SESSION_DATA, " +
            "RESULT_JSON, CREATED_TIME, EXPIRY_TIME FROM IDN_DEBUG_SESSION WHERE DEBUG_ID = ?";

    private static final String SQL_UPSERT_DEBUG_SESSION_LEGACY = "MERGE INTO IDN_DEBUG_SESSION " +
            "(DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME) " +
            "KEY (DEBUG_ID) VALUES (?, ?, ?, ?, ?, ?)";

    @Override
    public void createDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(sessionData.getDebugId());
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        try {
            jdbcTemplate.executeUpdate(SQL_INSERT_DEBUG_SESSION, preparedStatement -> {
                preparedStatement.setString(1, normalizedDebugId);
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
            if (isMissingResourceTypeColumnError(e.getCause())) {
                LOG.warn("Column RESOURCE_TYPE not found in IDN_DEBUG_SESSION. " +
                        "Falling back to legacy insert without resource info.");
                try {
                    jdbcTemplate.executeUpdate(SQL_INSERT_DEBUG_SESSION_LEGACY, preparedStatement -> {
                        preparedStatement.setString(1, normalizedDebugId);
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
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        try {
            return jdbcTemplate.fetchSingleRecord(SQL_GET_DEBUG_SESSION, (resultSet, rowNumber) -> {
                DebugSessionData data = new DebugSessionData();
                data.setDebugId(resultSet.getString("DEBUG_ID"));
                data.setStatus(resultSet.getString("STATUS"));
                try {
                    data.setSessionData(resultSet.getBinaryStream("SESSION_DATA"));
                } catch (Exception e) {
                    // Ignore stream errors
                }
                data.setResultJson(resultSet.getString("RESULT_JSON"));
                data.setCreatedTime(resultSet.getTimestamp("CREATED_TIME").getTime());
                data.setExpiryTime(resultSet.getTimestamp("EXPIRY_TIME").getTime());
                data.setResourceType(resultSet.getString("RESOURCE_TYPE"));
                data.setConnectionId(resultSet.getString("RESOURCE_ID"));
                return data;
            }, preparedStatement -> preparedStatement.setString(1, normalizedDebugId));
        } catch (DataAccessException e) {
            // Check if error is due to missing columns and fallback
            if (isMissingResourceTypeColumnError(e.getCause())) {
                LOG.warn("Column RESOURCE_TYPE not found in IDN_DEBUG_SESSION. " +
                        "Falling back to legacy retrieval without resource info.");
                try {
                    return jdbcTemplate.fetchSingleRecord(SQL_GET_DEBUG_SESSION_LEGACY, (resultSet, rowNumber) -> {
                        DebugSessionData data = new DebugSessionData();
                        data.setDebugId(resultSet.getString("DEBUG_ID"));
                        data.setStatus(resultSet.getString("STATUS"));
                        try {
                            data.setSessionData(resultSet.getBinaryStream("SESSION_DATA"));
                        } catch (Exception ex) {
                            // Ignore stream errors
                        }
                        data.setResultJson(resultSet.getString("RESULT_JSON"));
                        data.setCreatedTime(resultSet.getTimestamp("CREATED_TIME").getTime());
                        data.setExpiryTime(resultSet.getTimestamp("EXPIRY_TIME").getTime());
                        return data;
                    }, preparedStatement -> preparedStatement.setString(1, normalizedDebugId));
                } catch (DataAccessException ex) {
                    String errorMsg = "Error while retrieving debug session (fallback): " + debugId;
                    LOG.error(errorMsg, ex);
                    throw new DebugFrameworkServerException(errorMsg, ex);
                }
            }
            String errorMsg = "Error while retrieving debug session: " + debugId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public void deleteDebugSession(String debugId) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(debugId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQL_DELETE_DEBUG_SESSION)) {
                prepStmt.setString(1, normalizedDebugId);
                prepStmt.executeUpdate();

                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error while deleting debug session: " + debugId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Unexpected error while deleting debug session: " + debugId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public void upsertDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(sessionData.getDebugId());

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            boolean success = false;
            try {
                try (PreparedStatement prepStmt = connection.prepareStatement(SQL_UPSERT_DEBUG_SESSION)) {
                    prepStmt.setString(1, normalizedDebugId);
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
                if (isMissingResourceTypeColumnError(e)) {
                    LOG.warn("Column RESOURCE_TYPE not found in IDN_DEBUG_SESSION. " +
                            "Falling back to legacy upsert without resource info.");
                    try (PreparedStatement prepStmt = connection.prepareStatement(SQL_UPSERT_DEBUG_SESSION_LEGACY)) {
                        prepStmt.setString(1, normalizedDebugId);
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
        } catch (Exception e) {
            String errorMsg = "Unexpected error while upserting debug session: " + sessionData.getDebugId();
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
            String errorMsg = "Error while deleting expired debug sessions";
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Unexpected error while deleting expired debug sessions";
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    /**
     * Normalizes the debug ID by removing hyphens from the UUID part.
     * This ensures consistent storage and retrieval regardless of format.
     *
     * @param debugId The debug ID to normalize.
     * @return Normalized debug ID (e.g., debug-32charuuid).
     */
    private String normalizeDebugId(String debugId) {

        if (debugId == null || !debugId.startsWith(DEBUG_SESSION_PREFIX)) {
            return debugId;
        }

        String uuidPart = debugId.substring(DEBUG_SESSION_PREFIX.length());
        String normalizedUuid = uuidPart.replace("-", "");
        return DEBUG_SESSION_PREFIX + normalizedUuid;
    }

    private void setSessionData(PreparedStatement preparedStatement, int parameterIndex,
                                DebugSessionData sessionData) throws SQLException {

        if (sessionData.getSessionData() == null) {
            preparedStatement.setNull(parameterIndex, Types.BLOB);
            return;
        }
        preparedStatement.setBinaryStream(parameterIndex, sessionData.getSessionData());
    }

    private boolean isMissingResourceTypeColumnError(Throwable throwable) {

        if (!(throwable instanceof SQLException)) {
            return false;
        }
        String message = throwable.getMessage();
        if (message == null) {
            return false;
        }
        String normalizedMessage = message.toLowerCase(Locale.ENGLISH);
        return normalizedMessage.contains("resource_type") && normalizedMessage.contains("not found");
    }
}
