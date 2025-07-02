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

package org.wso2.carbon.identity.flow.execution.engine.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.core.util.LambdaExceptionUtils;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.DB2_DATABASE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.DELETE_CONTEXT_SQL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.DELETE_EXPIRED_SQL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.FLOW_STATE_JSON;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.H2_DATABASE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.INSERT_CONTEXT_SQL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.MARIA_DATABASE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.MICROSOFT_DATABASE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.MS_SQL_DATABASE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.MYSQL_DATABASE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.POSTGRESQL_DATABASE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.SELECT_CONTEXT_SQL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.SQLConstants.UPDATE_CONTEXT_SQL;

/**
 * DAO implementation for managing FlowExecutionContext store.
 */
public class FlowContextStoreDAOImpl implements FlowContextStoreDAO {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void storeContext(FlowExecutionContext context, long ttlSeconds) throws FlowEngineException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            String serializedContext = OBJECT_MAPPER.writeValueAsString(context);
            Timestamp now = Timestamp.from(Instant.now());
            Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(ttlSeconds));

            int affectedRows = jdbcTemplate.executeUpdateWithAffectedRows(
                    UPDATE_CONTEXT_SQL,
                    preparedStatement -> {
                        preparedStatement.setString(1, serializedContext);
                        preparedStatement.setString(2, context.getContextIdentifier());
                    });

            if (affectedRows == 0) {
                jdbcTemplate.executeUpdate(
                        INSERT_CONTEXT_SQL,
                        preparedStatement -> {
                            preparedStatement.setString(1, context.getContextIdentifier());
                            preparedStatement.setString(2, context.getTenantDomain());
                            preparedStatement.setString(3, context.getFlowType());
                            preparedStatement.setTimestamp(4, now);
                            preparedStatement.setTimestamp(5, expiresAt);
                            preparedStatement.setString(6, serializedContext);
                        });
            }
        } catch (IOException | DataAccessException e) {
            throw FlowExecutionEngineUtils.handleServerException(
                    Constants.ErrorMessages.ERROR_CODE_FLOW_CONTEXT_STORE_FAILURE,
                    e,
                    context.getContextIdentifier());
        }
    }

    @Override
    public FlowExecutionContext getContext(String contextId) throws FlowEngineException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            return jdbcTemplate.fetchSingleRecord(SELECT_CONTEXT_SQL, (LambdaExceptionUtils.rethrowRowMapper(
                            (resultSet, rowNumber) -> {
                                String json = resultSet.getString(FLOW_STATE_JSON);
                                return OBJECT_MAPPER.readValue(json, FlowExecutionContext.class);
                            })),
                    preparedStatement -> preparedStatement.setString(1, contextId)
            );
        } catch (DataAccessException e) {
            throw FlowExecutionEngineUtils.handleServerException(
                    Constants.ErrorMessages.ERROR_CODE_FLOW_CONTEXT_RETRIEVAL_FAILURE, e, contextId);
        }
    }

    @Override
    public void deleteContext(String contextId) throws FlowEngineException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(DELETE_CONTEXT_SQL,
                    preparedStatement -> preparedStatement.setString(1, contextId));
        } catch (DataAccessException e) {
            throw FlowExecutionEngineUtils.handleServerException(
                    Constants.ErrorMessages.ERROR_CODE_FLOW_CONTEXT_DELETION_FAILURE, e, contextId);
        }
    }

    @SuppressFBWarnings(value = "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING",
            justification = "Limit value is controlled and not user-supplied. Safe to include directly.")
    @Override
    public void cleanupExpiredContexts(int cleanupLimit) throws FlowEngineException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        String deleteExpiredSql = getDeleteExpiredSql(jdbcTemplate, cleanupLimit);
        try {
            jdbcTemplate.executeUpdate(deleteExpiredSql, preparedStatement -> {
                preparedStatement.setTimestamp(1, Timestamp.from(Instant.now()));
            });
        } catch (DataAccessException e) {
            throw FlowExecutionEngineUtils.handleServerException(
                    Constants.ErrorMessages.ERROR_CODE_FLOW_CONTEXT_CLEANUP_FAILURE, e);
        }
    }

    private String getDeleteExpiredSql(JdbcTemplate jdbcTemplate, int cleanupLimit) throws FlowEngineException {

        String sqlQuery = DELETE_EXPIRED_SQL;
        try {
            String driverName = jdbcTemplate.getDriverName();
            if (driverName == null) {
                // If driver name is not available, return default query.
                return String.format(sqlQuery, cleanupLimit);
            }
            if (driverName.contains(MYSQL_DATABASE) || driverName.contains(MARIA_DATABASE)
                    || driverName.contains(H2_DATABASE)) {
                sqlQuery = Constants.SQLConstants.DELETE_EXPIRED_SQL;
            } else if (jdbcTemplate.getDatabaseProductName() != null &&
                    jdbcTemplate.getDatabaseProductName().contains(DB2_DATABASE)) {
                sqlQuery = Constants.SQLConstants.DELETE_EXPIRED_SQL_DB2;
            } else if (driverName.contains(MS_SQL_DATABASE)
                    || driverName.contains(MICROSOFT_DATABASE)) {
                sqlQuery = Constants.SQLConstants.DELETE_EXPIRED_SQL_MSSQL;
            } else if (driverName.contains(POSTGRESQL_DATABASE)) {
                sqlQuery = Constants.SQLConstants.DELETE_EXPIRED_SQL_POSTGRESQL;
            } else {
                sqlQuery = Constants.SQLConstants.DELETE_EXPIRED_SQL_ORACLE;
            }
            return String.format(sqlQuery, cleanupLimit);
        } catch (DataAccessException e) {
            throw FlowExecutionEngineUtils.handleServerException(
                    Constants.ErrorMessages.ERROR_CODE_FLOW_CONTEXT_CLEANUP_FAILURE, e);
        }
    }
}

