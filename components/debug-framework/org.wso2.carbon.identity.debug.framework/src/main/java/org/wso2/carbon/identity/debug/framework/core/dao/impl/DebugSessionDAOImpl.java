/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.debug.framework.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.debug.framework.core.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Implementation of the DebugSessionDAO using JdbcTemplate.
 */
public class DebugSessionDAOImpl implements DebugSessionDAO {

    private static final Log LOG = LogFactory.getLog(DebugSessionDAOImpl.class);

    private static final String SQL_INSERT_DEBUG_SESSION = "INSERT INTO IDN_DEBUG_SESSION " +
            "(SESSION_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_GET_DEBUG_SESSION = "SELECT SESSION_ID, STATUS, SESSION_DATA, RESULT_JSON, " +
            "CREATED_TIME, EXPIRY_TIME FROM IDN_DEBUG_SESSION WHERE SESSION_ID = ?";

    private static final String SQL_UPDATE_DEBUG_SESSION = "UPDATE IDN_DEBUG_SESSION SET " +
            "STATUS = ?, RESULT_JSON = ? WHERE SESSION_ID = ?";

    private static final String SQL_DELETE_DEBUG_SESSION = "DELETE FROM IDN_DEBUG_SESSION WHERE SESSION_ID = ?";

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
            });
        } catch (DataAccessException e) {
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
                data.setSessionData(resultSet.getBinaryStream("SESSION_DATA"));
                data.setResultJson(resultSet.getString("RESULT_JSON"));
                data.setCreatedTime(resultSet.getTimestamp("CREATED_TIME").getTime());
                data.setExpiryTime(resultSet.getTimestamp("EXPIRY_TIME").getTime());
                return data;
            }, preparedStatement -> preparedStatement.setString(1, normalizedSessionId));
        } catch (DataAccessException e) {
            String errorMsg = "Error while retrieving debug session: " + sessionId;
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    @Override
    public void updateDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException {

        String normalizedSessionId = normalizeSessionId(sessionData.getSessionId());
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
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

        String normalizedSessionId = normalizeSessionId(sessionId);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate(JdbcUtils.Database.IDENTITY);
        try {
            jdbcTemplate.executeUpdate(SQL_DELETE_DEBUG_SESSION, preparedStatement -> {
                preparedStatement.setString(1, normalizedSessionId);
            });
        } catch (DataAccessException e) {
            String errorMsg = "Error while deleting debug session: " + sessionId;
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
