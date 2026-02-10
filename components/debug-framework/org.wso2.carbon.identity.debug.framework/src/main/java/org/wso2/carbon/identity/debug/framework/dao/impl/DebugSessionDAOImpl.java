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

/**
 * Implementation of the DebugSessionDAO using JdbcTemplate.
 */
public class DebugSessionDAOImpl implements DebugSessionDAO {

    private static final Log LOG = LogFactory.getLog(DebugSessionDAOImpl.class);

    private static final String SQL_INSERT_DEBUG_SESSION = "INSERT INTO IDN_DEBUG_SESSION " +
            "(SESSION_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME, RESOURCE_TYPE, RESOURCE_ID) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_GET_DEBUG_SESSION = "SELECT SESSION_ID, STATUS, SESSION_DATA, " +
            "RESULT_JSON, CREATED_TIME, EXPIRY_TIME, RESOURCE_TYPE, " +
            "RESOURCE_ID FROM IDN_DEBUG_SESSION WHERE SESSION_ID = ?";

    private static final String SQL_UPDATE_DEBUG_SESSION = "UPDATE IDN_DEBUG_SESSION SET " +
            "STATUS = ?, RESULT_JSON = ? WHERE SESSION_ID = ?";

    private static final String SQL_DELETE_DEBUG_SESSION = "DELETE FROM IDN_DEBUG_SESSION WHERE SESSION_ID = ?";

    private static final String SQL_DELETE_EXPIRED_DEBUG_SESSIONS 
        = "DELETE FROM IDN_DEBUG_SESSION WHERE EXPIRY_TIME < ?";

    // MERGE statement for atomic upsert (H2 and most databases support this)
    private static final String SQL_UPSERT_DEBUG_SESSION 
        = "MERGE INTO IDN_DEBUG_SESSION (SESSION_ID, STATUS, SESSION_DATA, RESULT_JSON, "
            +
            "CREATED_TIME, EXPIRY_TIME, RESOURCE_TYPE, RESOURCE_ID) KEY (SESSION_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    // Legacy SQLs for backward compatibility (if DB schema is not updated)
    private static final String SQL_INSERT_DEBUG_SESSION_LEGACY = "INSERT INTO IDN_DEBUG_SESSION " +
            "(SESSION_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_GET_DEBUG_SESSION_LEGACY = "SELECT SESSION_ID, STATUS, SESSION_DATA, " +
            "RESULT_JSON, CREATED_TIME, EXPIRY_TIME FROM IDN_DEBUG_SESSION WHERE SESSION_ID = ?";

    private static final String SQL_UPSERT_DEBUG_SESSION_LEGACY = "MERGE INTO IDN_DEBUG_SESSION " +
            "(SESSION_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME) " +
            "KEY (SESSION_ID) VALUES (?, ?, ?, ?, ?, ?)";

    @Override
    public void createDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        String normalizedSessionId = normalizeSessionId(sessionData.getSessionId());
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        try {
            jdbcTemplate.executeUpdate(SQL_INSERT_DEBUG_SESSION, preparedStatement -> {
                preparedStatement.setString(1, normalizedSessionId);
                preparedStatement.setString(2, sessionData.getStatus());
                if (sessionData.getSessionData() != null) {
                    preparedStatement.setBinaryStream(3, sessionData.getSessionData());
                } else {
                    preparedStatement.setBinaryStream(3, null);
                }
                preparedStatement.setString(4, sessionData.getResultJson());
                preparedStatement.setTimestamp(5, new Timestamp(sessionData.getCreatedTime()));
                preparedStatement.setTimestamp(6, new Timestamp(sessionData.getExpiryTime()));
                preparedStatement.setString(7, sessionData.getResourceType());
                preparedStatement.setString(8, sessionData.getResourceId());
            });
        } catch (DataAccessException e) {
            // Check if error is due to missing columns and fallback
            if (e.getCause() instanceof SQLException &&
                    e.getCause().getMessage().contains("Column \"RESOURCE_TYPE\" not found")) {
                LOG.warn("Column RESOURCE_TYPE not found in IDN_DEBUG_SESSION. " +
                        "Falling back to legacy insert without resource info.");
                try {
                    jdbcTemplate.executeUpdate(SQL_INSERT_DEBUG_SESSION_LEGACY, preparedStatement -> {
                        preparedStatement.setString(1, normalizedSessionId);
                        preparedStatement.setString(2, sessionData.getStatus());
                        if (sessionData.getSessionData() != null) {
                            preparedStatement.setBinaryStream(3, sessionData.getSessionData());
                        } else {
                            preparedStatement.setBinaryStream(3, null);
                        }
                        preparedStatement.setString(4, sessionData.getResultJson());
                        preparedStatement.setTimestamp(5, new Timestamp(sessionData.getCreatedTime()));
                        preparedStatement.setTimestamp(6, new Timestamp(sessionData.getExpiryTime()));
                    });
                    return;
                } catch (DataAccessException ex) {
                    // Log the fallback error
                    String errorMsg = "Error while creating debug session (fallback): " + sessionData.getSessionId();
                    LOG.error(errorMsg, ex);
                    throw new DebugFrameworkServerException(errorMsg, ex);
                }
            }
            String errorMsg = "Error while creating debug session: " + sessionData.getSessionId();
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public DebugSessionData getDebugSession(String sessionId) throws DebugFrameworkServerException {

        String normalizedSessionId = normalizeSessionId(sessionId);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        try {
            return jdbcTemplate.fetchSingleRecord(SQL_GET_DEBUG_SESSION, (resultSet, rowNumber) -> {
                DebugSessionData data = new DebugSessionData();
                data.setSessionId(resultSet.getString("SESSION_ID"));
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
                data.setResourceId(resultSet.getString("RESOURCE_ID"));
                return data;
            }, preparedStatement -> preparedStatement.setString(1, normalizedSessionId));
        } catch (DataAccessException e) {
            // Check if error is due to missing columns and fallback
            if (e.getCause() instanceof SQLException &&
                    e.getCause().getMessage().contains("Column \"RESOURCE_TYPE\" not found")) {
                LOG.warn("Column RESOURCE_TYPE not found in IDN_DEBUG_SESSION. " +
                        "Falling back to legacy retrieval without resource info.");
                try {
                    return jdbcTemplate.fetchSingleRecord(SQL_GET_DEBUG_SESSION_LEGACY, (resultSet, rowNumber) -> {
                        DebugSessionData data = new DebugSessionData();
                        data.setSessionId(resultSet.getString("SESSION_ID"));
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
                    }, preparedStatement -> preparedStatement.setString(1, normalizedSessionId));
                } catch (DataAccessException ex) {
                    String errorMsg = "Error while retrieving debug session (fallback): " + sessionId;
                    LOG.error(errorMsg, ex);
                    throw new DebugFrameworkServerException(errorMsg, ex);
                }
            }
            String errorMsg = "Error while retrieving debug session: " + sessionId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public void updateDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        // Keeps the existing status/result update logic
        // If resource type/id update is needed, this method should also be updated
        // For now, only upsert handles the new fields effectively for the save flow

        String normalizedSessionId = normalizeSessionId(sessionData.getSessionId());
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);

        LOG.info("Updating debug session - original: " + sessionData.getSessionId()
                + ", normalized: " + normalizedSessionId
                + ", status: " + sessionData.getStatus());

        try {
            jdbcTemplate.executeUpdate(SQL_UPDATE_DEBUG_SESSION, preparedStatement -> {
                preparedStatement.setString(1, sessionData.getStatus());
                preparedStatement.setString(2, sessionData.getResultJson());
                preparedStatement.setString(3, normalizedSessionId);
            });
        } catch (DataAccessException e) {
            String errorMsg = "Error while updating debug session: " + sessionData.getSessionId();
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public void deleteDebugSession(String sessionId) throws DebugFrameworkServerException {

        // Implementation remains same
        String normalizedSessionId = normalizeSessionId(sessionId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQL_DELETE_DEBUG_SESSION)) {
                prepStmt.setString(1, normalizedSessionId);
                prepStmt.executeUpdate();

                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error while deleting debug session: " + sessionId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Unexpected error while deleting debug session: " + sessionId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public void upsertDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        String normalizedSessionId = normalizeSessionId(sessionData.getSessionId());

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            boolean success = false;
            try {
                try (PreparedStatement prepStmt = connection.prepareStatement(SQL_UPSERT_DEBUG_SESSION)) {
                    prepStmt.setString(1, normalizedSessionId);
                    prepStmt.setString(2, sessionData.getStatus());
                    if (sessionData.getSessionData() != null) {
                        prepStmt.setBinaryStream(3, sessionData.getSessionData());
                    } else {
                        prepStmt.setBinaryStream(3, null);
                    }
                    prepStmt.setString(4, sessionData.getResultJson());
                    prepStmt.setTimestamp(5, new Timestamp(sessionData.getCreatedTime()));
                    prepStmt.setTimestamp(6, new Timestamp(sessionData.getExpiryTime()));
                    prepStmt.setString(7, sessionData.getResourceType());
                    prepStmt.setString(8, sessionData.getResourceId());

                    prepStmt.executeUpdate();
                    success = true;
                }
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("Column \"RESOURCE_TYPE\" not found")) {
                    LOG.warn("Column RESOURCE_TYPE not found in IDN_DEBUG_SESSION. " +
                            "Falling back to legacy upsert without resource info.");
                    try (PreparedStatement prepStmt = connection.prepareStatement(SQL_UPSERT_DEBUG_SESSION_LEGACY)) {
                        prepStmt.setString(1, normalizedSessionId);
                        prepStmt.setString(2, sessionData.getStatus());
                        if (sessionData.getSessionData() != null) {
                            prepStmt.setBinaryStream(3, sessionData.getSessionData());
                        } else {
                            prepStmt.setBinaryStream(3, null);
                        }
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
                LOG.info("Debug session upserted successfully: " + normalizedSessionId);
            }
        } catch (SQLException e) {
            String errorMsg = "Error while upserting debug session: " + sessionData.getSessionId();
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Unexpected error while upserting debug session: " + sessionData.getSessionId();
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
     * Normalizes the session ID by removing hyphens from the UUID part.
     * This ensures consistent storage and retrieval regardless of format.
     *
     * @param sessionId The session ID to normalize.
     * @return Normalized session ID (e.g., debug-32charuuid).
     */
    private String normalizeSessionId(String sessionId) {

        if (sessionId == null || !sessionId.startsWith("debug-")) {
            return sessionId;
        }

        String uuidPart = sessionId.substring(6);
        String normalizedUuid = uuidPart.replaceAll("-", "");
        return "debug-" + normalizedUuid;
    }
}
