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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtRuntimeException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtServerException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationResponseDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationStatusCount;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.dao.AsyncOperationStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.models.dos.UnitOperationDO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util.AsyncOperationStatusMgtDbUtil;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.models.FilterQueryBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_WHILE_PERSISTING_ASYNC_OPERATION_STATUS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_WHILE_PERSISTING_ASYNC_OPERATION_STATUS_UNIT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS_UNIT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS_UNIT_COUNT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.ErrorMessage.ERROR_WHILE_UPDATING_ASYNC_OPERATION_STATUS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.CREATE_ASYNC_OPERATION;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.CREATE_ASYNC_OPERATION_UNIT_BATCH;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.DELETE_RECENT_OPERATION_RECORD;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.GET_OPERATION;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.GET_OPERATIONS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.GET_OPERATIONS_TAIL;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.GET_OPERATIONS_TAIL_MSSQL;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.GET_OPERATIONS_TAIL_ORACLE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.GET_UNIT_OPERATION;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.GET_UNIT_OPERATIONS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.GET_UNIT_OPERATIONS_TAIL;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.GET_UNIT_OPERATIONS_TAIL_MSSQL;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.GET_UNIT_OPERATIONS_TAIL_ORACLE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.GET_UNIT_OPERATION_STATUS_COUNT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.LIMIT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.CORRELATION_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.COUNT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.CREATED_AT;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.INITIATED_ORG_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.INITIATED_USER_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.LAST_MODIFIED;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.OPERATION_TYPE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.POLICY;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.RESIDENT_RESOURCE_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.STATUS;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.STATUS_MESSAGE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.SUBJECT_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.SUBJECT_TYPE;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.TARGET_ORG_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.UNIT_OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant.SQLConstants.UPDATE_ASYNC_OPERATION;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util.AsyncOperationStatusMgtDbUtil.isMSSqlDB;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util.AsyncOperationStatusMgtDbUtil.isOracleDB;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util.AsyncOperationStatusMgtExceptionHandler.handleServerException;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util.AsyncOperationStatusMgtExceptionHandler.throwRuntimeException;
import static org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.util.FilterQueryBuilderUtil.buildFilterQuery;

/**
 * DAO implementation for Asynchronous Operation Status Management.
 */
public class AsyncOperationOperationStatusMgtDAOImpl implements AsyncOperationStatusMgtDAO {

    private static final Log LOG = LogFactory.getLog(AsyncOperationOperationStatusMgtDAOImpl.class);

    @Override
    public String registerAsyncStatusWithoutUpdate(OperationInitDTO record) throws AsyncOperationStatusMgtException {
        
        try {
            return addAsyncOperationStatus(record);
        } catch (TransactionException e) {
            throw handleServerException(ERROR_WHILE_PERSISTING_ASYNC_OPERATION_STATUS, e);
        }
    }

    @Override
    public String registerAsyncStatusWithUpdate(OperationInitDTO record) throws AsyncOperationStatusMgtException {
        
        try {
            deleteOldOperationalData(record.getCorrelationId(), record.getOperationType(),
                    record.getOperationSubjectId());
            return addAsyncOperationStatus(record);
        } catch (TransactionException e) {
            throw handleServerException(ERROR_WHILE_PERSISTING_ASYNC_OPERATION_STATUS, e);
        }
    }

    @Override
    public void updateAsyncStatus(String operationId, OperationStatus status) throws AsyncOperationStatusMgtException {

        NamedJdbcTemplate jdbcTemplate = AsyncOperationStatusMgtDbUtil.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(UPDATE_ASYNC_OPERATION,
                        statement -> {
                            statement.setString(STATUS, status.toString());
                            statement.setTimeStamp(LAST_MODIFIED, new Timestamp(new Date().getTime()), null);
                            statement.setString(OPERATION_ID, operationId);
                        });
                return null;
            });
        } catch (TransactionException e) {
            throw handleServerException(ERROR_WHILE_UPDATING_ASYNC_OPERATION_STATUS, e);
        }
    }

    @Override
    public void registerAsyncStatusUnit(ConcurrentLinkedQueue<UnitOperationInitDTO> queue)
            throws AsyncOperationStatusMgtException {

        Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        NamedJdbcTemplate jdbcTemplate = AsyncOperationStatusMgtDbUtil.getNewTemplate();
        try {
            jdbcTemplate.withTransaction(template ->
                template.executeBatchInsert(CREATE_ASYNC_OPERATION_UNIT_BATCH, statement -> {
                    for (UnitOperationInitDTO context : queue) {
                        statement.setString(UNIT_OPERATION_ID, UUID.randomUUID().toString());
                        statement.setString(OPERATION_ID, context.getOperationId());
                        statement.setString(RESIDENT_RESOURCE_ID, context.getOperationInitiatedResourceId());
                        statement.setString(TARGET_ORG_ID, context.getTargetOrgId());
                        statement.setString(STATUS, context.getUnitOperationStatus());
                        statement.setString(STATUS_MESSAGE, context.getStatusMessage());
                        statement.setTimeStamp(CREATED_AT, currentTimestamp, null);
                        statement.addBatch();
                    }
                }, null));
        } catch (TransactionException e) {
            throw handleServerException(ERROR_WHILE_PERSISTING_ASYNC_OPERATION_STATUS_UNIT, e);
        }
    }

    @Override
    public List<OperationResponseDTO> getOperations(String requestInitiatedOrgId, Integer limit,
                                                    List<ExpressionNode> expressionNodes)
            throws AsyncOperationStatusMgtException {

        FilterQueryBuilder filterQueryBuilder = buildFilterQuery(expressionNodes);
        String sqlStmt = getOperationsStatusSqlStmt(filterQueryBuilder);

        List<OperationResponseDTO> operationRecords;
        NamedJdbcTemplate jdbcTemplate = AsyncOperationStatusMgtDbUtil.getNewTemplate();
        try {
            operationRecords = jdbcTemplate.executeQuery(sqlStmt, (resultSet, rowNumber) -> {
                    try {
                        return createOperationResponseDTO(resultSet);
                    } catch (DataAccessException e) {
                        throwRuntimeException(ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS_UNIT_COUNT.getMessage(), e);
                    }
                    return null;
                },
                namedPreparedStatement -> {
                    setFilterAttributes(namedPreparedStatement, filterQueryBuilder.getFilterAttributeValue(),
                            filterQueryBuilder.getTimestampFilterAttributes());
                    namedPreparedStatement.setInt(LIMIT, limit);
                    namedPreparedStatement.setString(INITIATED_ORG_ID, requestInitiatedOrgId);
                });
        } catch (AsyncOperationStatusMgtRuntimeException | DataAccessException e) {
            throw handleServerException(ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS, e);
        }
        return operationRecords;
    }

    @Override
    public OperationResponseDTO getOperation(String operationId, String requestInitiatedOrgId) throws
            AsyncOperationStatusMgtException {

        OperationResponseDTO operationRecord;
        NamedJdbcTemplate jdbcTemplate = AsyncOperationStatusMgtDbUtil.getNewTemplate();
        try {
            operationRecord = jdbcTemplate.fetchSingleRecord(GET_OPERATION, (resultSet, rowNumber) -> {
                try {
                    return createOperationResponseDTO(resultSet);
                } catch (DataAccessException e) {
                    throwRuntimeException(ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS_UNIT_COUNT.getMessage(), e);
                }
                return null;
            }, namedPreparedStatement -> {
                namedPreparedStatement.setString(OPERATION_ID, operationId);
                namedPreparedStatement.setString(INITIATED_ORG_ID, requestInitiatedOrgId);
            });
            return operationRecord;
        } catch (AsyncOperationStatusMgtRuntimeException | DataAccessException e) {
            throw handleServerException(ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS, e);
        }
    }

    @Override
    public List<UnitOperationDO> getUnitOperations(String operationId, String requestInitiatedOrgId, Integer limit,
                                                   List<ExpressionNode> expressionNodes)
            throws AsyncOperationStatusMgtServerException {

        FilterQueryBuilder filterQueryBuilder = buildFilterQuery(expressionNodes);
        String sqlStmt = getUnitOperationsStatusSqlStmt(filterQueryBuilder);

        List<UnitOperationDO> unitOperationRecords;
        NamedJdbcTemplate jdbcTemplate = AsyncOperationStatusMgtDbUtil.getNewTemplate();
        try {
            unitOperationRecords = jdbcTemplate.executeQuery(sqlStmt,
                (resultSet, rowNumber) ->
                        createUnitOperationDO(resultSet),
                namedPreparedStatement -> {
                    namedPreparedStatement.setString(OPERATION_ID, operationId);
                    setFilterAttributes(namedPreparedStatement, filterQueryBuilder.getFilterAttributeValue(),
                            filterQueryBuilder.getTimestampFilterAttributes());
                    namedPreparedStatement.setInt(LIMIT, limit);
                    namedPreparedStatement.setString(INITIATED_ORG_ID, requestInitiatedOrgId);
                });

        } catch (DataAccessException e) {
            throw handleServerException(ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS_UNIT, e);
        }
        return unitOperationRecords;
    }

    @Override
    public UnitOperationDO getUnitOperation(String unitOperationId, String requestInitiatedOrgId)
            throws AsyncOperationStatusMgtException {

        UnitOperationDO unitOperationRecord;
        NamedJdbcTemplate jdbcTemplate = AsyncOperationStatusMgtDbUtil.getNewTemplate();
        try {
            unitOperationRecord = jdbcTemplate.fetchSingleRecord(GET_UNIT_OPERATION, (resultSet, rowNumber) -> {
                if (StringUtils.isBlank(resultSet.getString(OPERATION_ID))) {
                    return null;
                }
                return createUnitOperationDO(resultSet);
            }, namedPreparedStatement -> {
                namedPreparedStatement.setString(UNIT_OPERATION_ID, unitOperationId);
                namedPreparedStatement.setString(INITIATED_ORG_ID, requestInitiatedOrgId);
            });
            return unitOperationRecord;
        } catch (DataAccessException e) {
            throw handleServerException(ERROR_WHILE_RETRIEVING_ASYNC_OPERATION_STATUS_UNIT, e);
        }
    }
    
    private String addAsyncOperationStatus(OperationInitDTO record) throws TransactionException {

        Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        String operationId = UUID.randomUUID().toString();
        NamedJdbcTemplate jdbcTemplate = AsyncOperationStatusMgtDbUtil.getNewTemplate();
        
        jdbcTemplate.withTransaction(template -> template.executeInsert(CREATE_ASYNC_OPERATION,
            statement -> {
                statement.setString(OPERATION_ID, operationId);
                statement.setString(CORRELATION_ID, record.getCorrelationId());
                statement.setString(OPERATION_TYPE, record.getOperationType());
                statement.setString(SUBJECT_TYPE, record.getOperationSubjectType());
                statement.setString(SUBJECT_ID, record.getOperationSubjectId());
                statement.setString(INITIATED_ORG_ID, record.getResidentOrgId());
                statement.setString(INITIATED_USER_ID, record.getInitiatorId());
                statement.setString(STATUS, OperationStatus.IN_PROGRESS.toString());
                statement.setTimeStamp(CREATED_AT, currentTimestamp, null);
                statement.setTimeStamp(LAST_MODIFIED, currentTimestamp, null);
                statement.setString(POLICY, record.getOperationPolicy());
            }, null, false)
        );
        return operationId;
    }

    private void deleteOldOperationalData(String correlationId, String operationType,
                                          String operationSubjectId) throws TransactionException {
        
        NamedJdbcTemplate jdbcTemplate = AsyncOperationStatusMgtDbUtil.getNewTemplate();
        
        jdbcTemplate.withTransaction(template -> {
            template.executeUpdate(DELETE_RECENT_OPERATION_RECORD, statement -> {
                statement.setString(OPERATION_TYPE, operationType);
                statement.setString(SUBJECT_ID, operationSubjectId);
                statement.setString(CORRELATION_ID, correlationId);
                });
            return null;
        });
    }

    private UnitOperationStatusCount getUnitOperationStatusCount(String operationId, String requestInitiatedOrgId)
            throws  DataAccessException {

        UnitOperationStatusCount countObj = new UnitOperationStatusCount();
        NamedJdbcTemplate jdbcTemplate = AsyncOperationStatusMgtDbUtil.getNewTemplate();

        jdbcTemplate.executeQuery(GET_UNIT_OPERATION_STATUS_COUNT, (resultSet, rowNumber) -> {

            OperationStatus status = OperationStatus.valueOf(resultSet.getString(STATUS));
            int count = resultSet.getInt(COUNT);

            switch (status) {
                case SUCCESS:
                    countObj.setSuccess(count);
                    break;
                case FAILED:
                    countObj.setFailed(count);
                    break;
                case PARTIALLY_COMPLETED:
                    countObj.setPartiallyCompleted(count);
                    break;
                default:
                    break;
            }
            return null;
        },
        namedPreparedStatement -> {
            namedPreparedStatement.setString(OPERATION_ID, operationId);
            namedPreparedStatement.setString(INITIATED_ORG_ID, requestInitiatedOrgId);
        });
        return countObj;
    }

    private static String getOperationsStatusSqlStmt(FilterQueryBuilder filterQueryBuilder)
            throws AsyncOperationStatusMgtServerException {

        String sqlStmtTail;
        if (isOracleDB()) {
            sqlStmtTail = GET_OPERATIONS_TAIL_ORACLE;
        } else if (isMSSqlDB()) {
            sqlStmtTail = GET_OPERATIONS_TAIL_MSSQL;
        } else {
            sqlStmtTail = GET_OPERATIONS_TAIL;
        }

        if (StringUtils.isNotBlank(filterQueryBuilder.getFilterQuery())) {
            return GET_OPERATIONS + " AND " + filterQueryBuilder.getFilterQuery() + sqlStmtTail;
        }
        return GET_OPERATIONS + sqlStmtTail;
    }

    private static String getUnitOperationsStatusSqlStmt(FilterQueryBuilder filterQueryBuilder)
            throws AsyncOperationStatusMgtServerException {

        String sqlStmtTail;
        if (isOracleDB()) {
            sqlStmtTail = GET_UNIT_OPERATIONS_TAIL_ORACLE;
        } else if (isMSSqlDB()) {
            sqlStmtTail = GET_UNIT_OPERATIONS_TAIL_MSSQL;
        } else {
            sqlStmtTail = GET_UNIT_OPERATIONS_TAIL;
        }
        if (StringUtils.isNotBlank(filterQueryBuilder.getFilterQuery())) {
            return GET_UNIT_OPERATIONS + " AND " + filterQueryBuilder.getFilterQuery() + sqlStmtTail;
        }
        return GET_UNIT_OPERATIONS + sqlStmtTail;
    }

    private OperationResponseDTO createOperationResponseDTO(ResultSet resultSet)
            throws SQLException, DataAccessException {

        return new OperationResponseDTO.Builder()
                .operationId(resultSet.getString(OPERATION_ID))
                .correlationId(resultSet.getString(CORRELATION_ID))
                .operationType(resultSet.getString(OPERATION_TYPE))
                .operationSubjectType(resultSet.getString(SUBJECT_TYPE))
                .operationSubjectId(resultSet.getString(SUBJECT_ID))
                .residentOrgId(resultSet.getString(INITIATED_ORG_ID))
                .initiatorId(resultSet.getString(INITIATED_USER_ID))
                .operationStatus(resultSet.getString(STATUS))
                .operationPolicy(resultSet.getString(POLICY))
                .createdTime(Timestamp.valueOf(resultSet.getString(CREATED_AT)))
                .modifiedTime(Timestamp.valueOf(resultSet.getString(LAST_MODIFIED)))
                .unitStatusCount(getUnitOperationStatusCount(resultSet.getString(OPERATION_ID),
                        resultSet.getString(INITIATED_ORG_ID)))
                .build();
    }

    private UnitOperationDO createUnitOperationDO(ResultSet resultSet) throws SQLException {

        UnitOperationDO record = new UnitOperationDO();
        record.setUnitOperationId(resultSet.getString(UNIT_OPERATION_ID));
        record.setOperationId(resultSet.getString(OPERATION_ID));
        record.setOperationInitiatedResourceId(resultSet.getString(RESIDENT_RESOURCE_ID));
        record.setTargetOrgId(resultSet.getString(TARGET_ORG_ID));
        record.setUnitOperationStatus(resultSet.getString(STATUS));
        record.setStatusMessage(resultSet.getString(STATUS_MESSAGE));
        record.setCreatedTime(Timestamp.valueOf(resultSet.getString(CREATED_AT)));
        return record;
    }

    private void setFilterAttributes(NamedPreparedStatement namedPreparedStatement,
                                           Map<String, String> filterAttributeValue, List<String> timestampTypeAttributes)
            throws SQLException {

        for (Map.Entry<String, String> entry : filterAttributeValue.entrySet()) {
            if (timestampTypeAttributes.contains(entry.getKey())) {
                namedPreparedStatement.setTimeStamp(entry.getKey(), Timestamp.valueOf(entry.getValue()), null);
            } else {
                namedPreparedStatement.setString(entry.getKey(), entry.getValue());
            }
        }
    }
}
