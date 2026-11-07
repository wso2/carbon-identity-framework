/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.poll.FlowPollingEntry;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;

import java.sql.Timestamp;
import java.time.Instant;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_FLOW_POLLING_FAILURE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.DELETE_POLL_ENTRY_SQL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.FLOW_STATE_JSON;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.INSERT_CONTEXT_SQL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.SELECT_POLL_ENTRY_SQL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.UPDATE_POLL_ENTRY_SQL;

/**
 * DAO implementation for flow polling entries.
 */
public class FlowPollingDAOImpl implements FlowPollingDAO {

    private static final String POLL_ENTRY_TYPE = "FLOW_POLL";
    private static final String FLOW_ID = "flowId";
    private static final String STATUS = "status";
    private static final String EXPIRES_AT = "expiresAt";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void addPollingEntry(String entryId, int tenantId, FlowPollingEntry entry) throws FlowEngineException {

        String json = toJson(entry);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(INSERT_CONTEXT_SQL,
                    preparedStatement -> {
                        preparedStatement.setString(1, entryId);
                        preparedStatement.setInt(2, tenantId);
                        preparedStatement.setString(3, POLL_ENTRY_TYPE);
                        preparedStatement.setTimestamp(4, Timestamp.from(Instant.now()));
                        preparedStatement.setTimestamp(5, new Timestamp(entry.getExpiresAt()));
                        preparedStatement.setString(6, json);
                    });
        } catch (DataAccessException e) {
            throw FlowExecutionEngineUtils.handleServerException(ERROR_CODE_FLOW_POLLING_FAILURE, e);
        }
    }

    @Override
    public FlowPollingEntry getPollingEntry(String entryId, int tenantId) throws FlowEngineException {

        String json;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            json = jdbcTemplate.fetchSingleRecord(SELECT_POLL_ENTRY_SQL,
                    (resultSet, rowNumber) -> resultSet.getString(FLOW_STATE_JSON),
                    preparedStatement -> {
                        preparedStatement.setString(1, entryId);
                        preparedStatement.setInt(2, tenantId);
                        preparedStatement.setString(3, POLL_ENTRY_TYPE);
                        preparedStatement.setTimestamp(4, Timestamp.from(Instant.now()));
                    });
        } catch (DataAccessException e) {
            throw FlowExecutionEngineUtils.handleServerException(ERROR_CODE_FLOW_POLLING_FAILURE, e);
        }
        return json == null ? null : fromJson(json);
    }

    @Override
    public boolean updatePollingEntry(String entryId, int tenantId, FlowPollingEntry entry)
            throws FlowEngineException {

        String json = toJson(entry);
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.executeUpdateWithAffectedRows(UPDATE_POLL_ENTRY_SQL,
                    preparedStatement -> {
                        preparedStatement.setString(1, json);
                        preparedStatement.setString(2, entryId);
                        preparedStatement.setInt(3, tenantId);
                        preparedStatement.setString(4, POLL_ENTRY_TYPE);
                    }) > 0;
        } catch (DataAccessException e) {
            throw FlowExecutionEngineUtils.handleServerException(ERROR_CODE_FLOW_POLLING_FAILURE, e);
        }
    }

    @Override
    public void deletePollingEntry(String entryId, int tenantId) throws FlowEngineException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_POLL_ENTRY_SQL,
                    preparedStatement -> {
                        preparedStatement.setString(1, entryId);
                        preparedStatement.setInt(2, tenantId);
                        preparedStatement.setString(3, POLL_ENTRY_TYPE);
                    });
        } catch (DataAccessException e) {
            throw FlowExecutionEngineUtils.handleServerException(ERROR_CODE_FLOW_POLLING_FAILURE, e);
        }
    }

    private static String toJson(FlowPollingEntry entry) throws FlowEngineException {

        ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put(FLOW_ID, entry.getFlowId());
        node.put(STATUS, entry.getStatus());
        if (entry.getExpiresAt() > 0) {
            node.put(EXPIRES_AT, entry.getExpiresAt());
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw FlowExecutionEngineUtils.handleServerException(ERROR_CODE_FLOW_POLLING_FAILURE, e);
        }
    }

    private static FlowPollingEntry fromJson(String json) throws FlowEngineException {

        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            return new FlowPollingEntry(node.path(FLOW_ID).asText(null), node.path(STATUS).asText(),
                    node.path(EXPIRES_AT).asLong(0L));
        } catch (JsonProcessingException e) {
            throw FlowExecutionEngineUtils.handleServerException(ERROR_CODE_FLOW_POLLING_FAILURE, e);
        }
    }
}
