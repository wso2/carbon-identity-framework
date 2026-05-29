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
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.ErrorMessages;
import org.wso2.carbon.identity.debug.framework.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;
import org.wso2.carbon.identity.debug.framework.util.DebugFrameworkUtils;

import java.sql.Timestamp;
import java.sql.Types;

import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQLPlaceholders.DB_COLUMN_CREATED_TIME;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQLPlaceholders.DB_COLUMN_EXPIRY_TIME;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQLPlaceholders.DB_COLUMN_RESULT_JSON;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQLPlaceholders.DB_COLUMN_SESSION_DATA;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQLPlaceholders.DB_COLUMN_STATUS;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_DELETE_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_DELETE_EXPIRED_DEBUG_SESSIONS;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_GET_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_INSERT_DEBUG_SESSION;
import static org.wso2.carbon.identity.debug.framework.dao.SQLConstants.SQL_UPDATE_DEBUG_SESSION;

/**
 * Implementation of {@link DebugSessionDAO} using {@link JdbcTemplate}.
 */
public class DebugSessionDAOImpl implements DebugSessionDAO {

    private static final Log LOG = LogFactory.getLog(DebugSessionDAOImpl.class);

    @Override
    public void createDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            int tenantId = resolveTenantId();
            jdbcTemplate.executeUpdate(SQL_INSERT_DEBUG_SESSION, preparedStatement -> {
                preparedStatement.setString(1, sessionData.getDebugId());
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, sessionData.getStatus());
                setSessionDataParam(preparedStatement, 4, sessionData);
                preparedStatement.setString(5, sessionData.getResultJson());
                preparedStatement.setTimestamp(6, new Timestamp(sessionData.getCreatedTime()));
                preparedStatement.setTimestamp(7, new Timestamp(sessionData.getExpiryTime()));
            });
        } catch (DataAccessException e) {
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e,
                    "Error inserting debug session: " + sessionData.getDebugId());
        }
    }

    @Override
    public DebugSessionData getDebugSession(String debugId) throws DebugFrameworkServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            int tenantId = resolveTenantId();
            return jdbcTemplate.fetchSingleRecord(SQL_GET_DEBUG_SESSION,
                    LambdaExceptionUtils.rethrowRowMapper(
                            (resultSet, rowNumber) -> mapResultSetToDebugSessionData(resultSet, debugId)),
                    preparedStatement -> {
                        preparedStatement.setString(1, debugId);
                        preparedStatement.setInt(2, tenantId);
                    });
        } catch (DataAccessException e) {
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e,
                    "Error retrieving debug session: " + debugId);
        }
    }

    @Override
    public void updateDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            int tenantId = resolveTenantId();
            jdbcTemplate.executeUpdate(SQL_UPDATE_DEBUG_SESSION, preparedStatement -> {
                preparedStatement.setString(1, sessionData.getStatus());
                setSessionDataParam(preparedStatement, 2, sessionData);
                preparedStatement.setString(3, sessionData.getResultJson());
                preparedStatement.setTimestamp(4, new Timestamp(sessionData.getCreatedTime()));
                preparedStatement.setTimestamp(5, new Timestamp(sessionData.getExpiryTime()));
                preparedStatement.setString(6, sessionData.getDebugId());
                preparedStatement.setInt(7, tenantId);
            });
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug session updated successfully: " + sessionData.getDebugId());
            }
        } catch (DataAccessException e) {
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e,
                    "Error updating debug session: " + sessionData.getDebugId());
        }
    }

    @Override
    public void deleteDebugSession(String debugId) throws DebugFrameworkServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            int tenantId = resolveTenantId();
            jdbcTemplate.executeUpdate(SQL_DELETE_DEBUG_SESSION, preparedStatement -> {
                preparedStatement.setString(1, debugId);
                preparedStatement.setInt(2, tenantId);
            });
        } catch (DataAccessException e) {
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e,
                    "Error deleting debug session: " + debugId);
        }
    }

    @Override
    public void deleteExpiredDebugSessions() throws DebugFrameworkServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(SQL_DELETE_EXPIRED_DEBUG_SESSIONS,
                    preparedStatement -> preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis())));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Expired debug sessions deleted from database.");
            }
        } catch (DataAccessException e) {
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e,
                    "Error deleting expired debug sessions.");
        }
    }

    private DebugSessionData mapResultSetToDebugSessionData(java.sql.ResultSet resultSet, String debugId)
            throws java.sql.SQLException {

        DebugSessionData sessionData = new DebugSessionData();
        sessionData.setDebugId(debugId);
        sessionData.setStatus(DebugSessionData.SessionStatus.fromString(resultSet.getString(DB_COLUMN_STATUS)));
        sessionData.setSessionData(resultSet.getBytes(DB_COLUMN_SESSION_DATA));
        sessionData.setResultJson(resultSet.getString(DB_COLUMN_RESULT_JSON));
        sessionData.setCreatedTime(timestampToMillis(resultSet, DB_COLUMN_CREATED_TIME));
        sessionData.setExpiryTime(timestampToMillis(resultSet, DB_COLUMN_EXPIRY_TIME));
        return sessionData;
    }

    private int resolveTenantId() throws DebugFrameworkServerException {

        try {
            String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
            return IdentityTenantUtil.getTenantId(tenantDomain);
        } catch (Exception e) {
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e,
                    "Failed to resolve tenant ID for debug session operation.");
        }
    }

    private void setSessionDataParam(java.sql.PreparedStatement preparedStatement, int parameterIndex,
                                     DebugSessionData sessionData) throws java.sql.SQLException {

        if (sessionData.getSessionData() == null) {
            preparedStatement.setNull(parameterIndex, Types.BLOB);
        } else {
            preparedStatement.setBytes(parameterIndex, sessionData.getSessionData());
        }
    }

    private long timestampToMillis(java.sql.ResultSet resultSet, String columnName) throws java.sql.SQLException {

        Timestamp ts = resultSet.getTimestamp(columnName);
        return ts != null ? ts.getTime() : 0L;
    }
}
