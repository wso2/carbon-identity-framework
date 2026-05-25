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
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.ErrorMessages;
import org.wso2.carbon.identity.debug.framework.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.UUID;

import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_DELETE_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_DELETE_EXPIRED_DEBUG_SESSIONS;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_GET_DEBUG_SESSION;
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

        String storageDebugId = resolveStorageDebugId(sessionData.getDebugId());

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
            throw serverError("Error inserting debug session into DB. Debug ID: "
                    + sessionData.getDebugId() + ", storage key: " + storageDebugId, e);
        }
    }

    @Override
    public DebugSessionData getDebugSession(String debugId) throws DebugFrameworkServerException {

        String storageDebugId = resolveStorageDebugId(debugId);

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
            throw serverError("Error retrieving debug session from DB. Debug ID: "
                    + debugId + ", storage key: " + storageDebugId, e);
        }
        return null;
    }

    @Override
    public void deleteDebugSession(String debugId) throws DebugFrameworkServerException {

        String storageDebugId = resolveStorageDebugId(debugId);

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement prepStmt = connection.prepareStatement(SQL_DELETE_DEBUG_SESSION)) {
                prepStmt.setString(1, storageDebugId);
                prepStmt.executeUpdate();

                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            }
        } catch (SQLException e) {
            throw serverError("Error deleting debug session from DB. Debug ID: "
                    + debugId + ", storage key: " + storageDebugId, e);
        }
    }

    @Override
    public void upsertDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        String storageDebugId = resolveStorageDebugId(sessionData.getDebugId());

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            int affectedRows;
            try (PreparedStatement updateStmt = connection.prepareStatement(SQL_UPDATE_DEBUG_SESSION)) {
                updateStmt.setString(1, sessionData.getStatus());
                setSessionData(updateStmt, 2, sessionData);
                updateStmt.setString(3, sessionData.getResultJson());
                updateStmt.setTimestamp(4, new Timestamp(sessionData.getCreatedTime()));
                updateStmt.setTimestamp(5, new Timestamp(sessionData.getExpiryTime()));
                updateStmt.setString(6, storageDebugId);
                affectedRows = updateStmt.executeUpdate();
            }

            if (affectedRows == 0) {
                try (PreparedStatement insertStmt = connection.prepareStatement(SQL_INSERT_DEBUG_SESSION)) {
                    insertStmt.setString(1, storageDebugId);
                    insertStmt.setString(2, sessionData.getStatus());
                    setSessionData(insertStmt, 3, sessionData);
                    insertStmt.setString(4, sessionData.getResultJson());
                    insertStmt.setTimestamp(5, new Timestamp(sessionData.getCreatedTime()));
                    insertStmt.setTimestamp(6, new Timestamp(sessionData.getExpiryTime()));
                    insertStmt.executeUpdate();
                }
            }

            if (!connection.getAutoCommit()) {
                connection.commit();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug session upserted successfully: " + sessionData.getDebugId());
            }
        } catch (SQLException e) {
            throw serverError("Error upserting debug session in DB. Debug ID: "
                    + sessionData.getDebugId() + ", storage key: " + storageDebugId, e);
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
            throw serverError("Error deleting expired debug sessions", e);
        }
    }

    private DebugSessionData mapResultSetToDebugSessionData(ResultSet resultSet, String debugId)
            throws SQLException {

        DebugSessionData sessionData = new DebugSessionData();
        sessionData.setDebugId(debugId);
        sessionData.setStatus(DebugSessionData.SessionStatus.fromString(
                resultSet.getString(DebugFrameworkConstants.DB_COLUMN_STATUS)));
        sessionData.setSessionData(resultSet.getBytes(DebugFrameworkConstants.DB_COLUMN_SESSION_DATA));
        sessionData.setResultJson(resultSet.getString(DebugFrameworkConstants.DB_COLUMN_RESULT_JSON));
        sessionData.setCreatedTime(getTimeInMillis(resultSet, DebugFrameworkConstants.DB_COLUMN_CREATED_TIME));
        sessionData.setExpiryTime(getTimeInMillis(resultSet, DebugFrameworkConstants.DB_COLUMN_EXPIRY_TIME));
        return sessionData;
    }

    /**
     * Normalizes the debug ID by removing hyphens from the UUID part.
     * Validates that the UUID portion is a well-formed UUID before normalizing to reject arbitrary
     * strings that merely start with the debug prefix. Returns the original value when the prefix
     * is absent (e.g. tests / direct keys).
     *
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
     * Resolves the storage debug ID by prefixing the tenant domain. Rejects malformed debug IDs
     * and missing tenant domains as server errors so callers do not silently no-op on bad state.
     */
    private String resolveStorageDebugId(String debugId) throws DebugFrameworkServerException {

        String normalizedDebugId = normalizeDebugId(debugId);
        if (normalizedDebugId == null) {
            throw new DebugFrameworkServerException(
                    ErrorMessages.ERROR_CODE_SERVER_ERROR.getCode(),
                    ErrorMessages.ERROR_CODE_SERVER_ERROR.getMessage(),
                    "Invalid debug ID: " + debugId);
        }
        String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
        if (tenantDomain == null || tenantDomain.trim().isEmpty()) {
            throw new DebugFrameworkServerException(
                    ErrorMessages.ERROR_CODE_SERVER_ERROR.getCode(),
                    ErrorMessages.ERROR_CODE_SERVER_ERROR.getMessage(),
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

        Timestamp ts = resultSet.getTimestamp(columnName);
        return ts != null ? ts.getTime() : 0L;
    }

    private DebugFrameworkServerException serverError(String message, Throwable cause) {

        return new DebugFrameworkServerException(
                ErrorMessages.ERROR_CODE_SERVER_ERROR.getCode(),
                ErrorMessages.ERROR_CODE_SERVER_ERROR.getMessage(),
                message, cause);
    }
}
