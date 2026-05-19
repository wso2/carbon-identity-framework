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
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_DELETE_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_DELETE_EXPIRED_DEBUG_SESSIONS;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_GET_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_GET_DEBUG_SESSION_FOR_UPDATE;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_INSERT_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_UPDATE_DEBUG_SESSION;

/**
 * Implementation of the DebugSessionDAO using raw JDBC connections.
 * All database operations use IdentityDatabaseUtil.getDBConnection() for consistent connection management.
 */
public class DebugSessionDAOImpl implements DebugSessionDAO {

    private static final Log LOG = LogFactory.getLog(DebugSessionDAOImpl.class);


    @Override
    public void createDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(sessionData.getDebugId());
        String storageDebugId = resolveStorageDebugId(normalizedDebugId);

        if (storageDebugId == null) {
            throw new DebugFrameworkServerException(
                    "Cannot create debug session: invalid debug ID: " + sessionData.getDebugId());
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQL_INSERT_DEBUG_SESSION)) {
                prepStmt.setString(1, storageDebugId);
                prepStmt.setString(2, sessionData.getStatus());
                setSessionData(prepStmt, 3, sessionData);
                prepStmt.setString(4, sessionData.getResultJson());
                prepStmt.setLong(5, sessionData.getCreatedTime());
                prepStmt.setLong(6, sessionData.getExpiryTime());
                prepStmt.executeUpdate();

                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error while inserting debug session into DB. Debug ID: "
                    + sessionData.getDebugId() + ", storage key: " + storageDebugId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public DebugSessionData getDebugSession(String debugId) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(debugId);
        String storageDebugId = resolveStorageDebugId(normalizedDebugId);

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
            String errorMsg = "Error while retrieving debug session from DB. Debug ID: "
                    + debugId + ", storage key: " + storageDebugId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
        return null;
    }

    @Override
    public DebugSessionData deleteAndReturnDebugSession(String debugId) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(debugId);
        String storageDebugId = resolveStorageDebugId(normalizedDebugId);
        DebugSessionData data = null;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            // First fetch the data with a lock.
            try (PreparedStatement prepStmt = connection
                    .prepareStatement(SQL_GET_DEBUG_SESSION_FOR_UPDATE)) {
                prepStmt.setString(1, storageDebugId);
                try (ResultSet resultSet = prepStmt.executeQuery()) {
                    if (resultSet.next()) {
                        data = mapResultSetToDebugSessionData(resultSet, debugId);
                    }
                }
            }

            // If data exists, delete it.
            if (data != null) {
                try (PreparedStatement prepStmt = connection.prepareStatement(SQL_DELETE_DEBUG_SESSION)) {
                    prepStmt.setString(1, storageDebugId);
                    prepStmt.executeUpdate();
                }
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            return data;
        } catch (SQLException e) {
            String errorMsg = "Error while performing atomic delete-and-return for debug session from DB. Debug ID: "
                    + debugId + ", storage key: " + storageDebugId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public void deleteDebugSession(String debugId) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(debugId);
        String storageDebugId = resolveStorageDebugId(normalizedDebugId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQL_DELETE_DEBUG_SESSION)) {
                prepStmt.setString(1, storageDebugId);
                prepStmt.executeUpdate();

                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error while deleting debug session from DB. Debug ID: "
                    + debugId + ", storage key: " + storageDebugId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public void upsertDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(sessionData.getDebugId());
        String storageDebugId = resolveStorageDebugId(normalizedDebugId);

        if (storageDebugId == null) {
            throw new DebugFrameworkServerException(
                    "Cannot upsert debug session: invalid debug ID: " + sessionData.getDebugId());
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            int affectedRows;
            try (PreparedStatement updateStmt = connection.prepareStatement(SQL_UPDATE_DEBUG_SESSION)) {
                updateStmt.setString(1, sessionData.getStatus());
                setSessionData(updateStmt, 2, sessionData);
                updateStmt.setString(3, sessionData.getResultJson());
                updateStmt.setLong(4, sessionData.getCreatedTime());
                updateStmt.setLong(5, sessionData.getExpiryTime());
                updateStmt.setString(6, storageDebugId);
                affectedRows = updateStmt.executeUpdate();
            }

            if (affectedRows == 0) {
                try (PreparedStatement insertStmt = connection.prepareStatement(SQL_INSERT_DEBUG_SESSION)) {
                    insertStmt.setString(1, storageDebugId);
                    insertStmt.setString(2, sessionData.getStatus());
                    setSessionData(insertStmt, 3, sessionData);
                    insertStmt.setString(4, sessionData.getResultJson());
                    insertStmt.setLong(5, sessionData.getCreatedTime());
                    insertStmt.setLong(6, sessionData.getExpiryTime());
                    insertStmt.executeUpdate();
                }
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug session upserted successfully: " + normalizedDebugId);
            }
        } catch (SQLException e) {
            String errorMsg = "Error while upserting debug session in DB. Debug ID: "
                    + sessionData.getDebugId() + ", storage key: " + storageDebugId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public void deleteExpiredDebugSessions() throws DebugFrameworkServerException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQL_DELETE_EXPIRED_DEBUG_SESSIONS)) {
                prepStmt.setLong(1, System.currentTimeMillis());
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
        sessionData.setStatus(DebugSessionData.SessionStatus.fromString(
                resultSet.getString(DebugFrameworkConstants.DB_COLUMN_STATUS)));
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
     * Validates that the UUID portion is a well-formed UUID before normalizing
     * to reject arbitrary strings that merely start with the debug prefix.
     *
     * @param debugId The debug ID to normalize.
     * @return Normalized debug ID, or {@code null} if the UUID part is malformed.
     */
    private String normalizeDebugId(String debugId) {

        if (debugId == null || !debugId.startsWith(DebugFrameworkConstants.DEBUG_PREFIX)) {
            return debugId;
        }

        String uuidPart = debugId.substring(DebugFrameworkConstants.DEBUG_PREFIX.length());
        try {
            UUID.fromString(uuidPart);
        } catch (IllegalArgumentException e) {
            LOG.warn("Rejected malformed debug ID with invalid UUID part: " + debugId);
            return null;
        }
        return DebugFrameworkConstants.DEBUG_PREFIX + uuidPart.replace("-", "");
    }

    /**
     * Resolves the storage debug ID and validates tenant domain.
     *
     * @param normalizedDebugId The normalized debug ID.
     * @return The storage key with tenant prefix.
     * @throws DebugFrameworkServerException If tenant domain cannot be resolved.
     */
    private String resolveStorageDebugId(String normalizedDebugId) throws DebugFrameworkServerException {

        if (normalizedDebugId == null) {
            return null;
        }
        String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
        if (tenantDomain == null || tenantDomain.trim().isEmpty()) {
            throw new DebugFrameworkServerException(
                    "Tenant domain could not be resolved for debug session storage ID: " + normalizedDebugId);
        }
        return tenantDomain + ":" + normalizedDebugId;
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

        return resultSet.getLong(columnName);
    }

}
