/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.status.mgt.internal.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.framework.async.status.mgt.api.constants.OperationStatus;
import org.wso2.carbon.identity.framework.async.status.mgt.api.exception.AsyncStatusMgtException;
import org.wso2.carbon.identity.framework.async.status.mgt.api.exception.AsyncStatusMgtServerException;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.ResponseOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.ResponseUnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.UnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.dao.AsyncStatusMgtDAO;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.models.FilterQueryBuilder;
import org.wso2.carbon.identity.framework.async.status.mgt.internal.util.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.ATTRIBURE_COLUMN_MAP;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.CO;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.EQ;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.EW;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.FILTER_PLACEHOLDER_PREFIX;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.GE;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.GT;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.LE;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.LT;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.AsyncStatusMgtConstants.SW;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.CREATE_ASYNC_OPERATION;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.CREATE_ASYNC_OPERATION_UNIT_BATCH;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.DELETE_RECENT_OPERATION_RECORD;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.GET_OPERATIONS;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.GET_OPERATIONS_TAIL;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.GET_UNIT_OPERATIONS;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.GET_UNIT_OPERATIONS_TAIL;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.LIMIT;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.OperationModelProperties.MODEL_CREATED_TIME;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.OperationModelProperties.MODEL_OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.OperationModelProperties.MODEL_OPERATION_TYPE;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.OperationModelProperties.MODEL_SUBJECT_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.OperationModelProperties.MODEL_SUBJECT_TYPE;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.CORRELATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.CREATED_AT;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.INITIATED_ORG_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.INITIATED_USER_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.LAST_MODIFIED;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.OPERATION_POLICY;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.OPERATION_STATUS;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.OPERATION_SUBJECT_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.OPERATION_SUBJECT_TYPE;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.OPERATION_TYPE;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.UNIT_OPERATION_CREATED_AT;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.UNIT_OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.UNIT_OPERATION_RESIDENT_RESOURCE_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.UNIT_OPERATION_STATUS;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.UNIT_OPERATION_STATUS_MESSAGE;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.SQLPlaceholders.UNIT_OPERATION_TARGET_ORG_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.UPDATE_ASYNC_OPERATION;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.constant.SQLConstants.UnitOperationModelProperties.MODEL_CREATED_AT;
import static org.wso2.carbon.identity.framework.async.status.mgt.internal.util.Utils.isMSSqlDB;

/**
 * DAO implementation for Asynchronous Operation Status Management.
 */
public class AsyncStatusMgtDAOImpl implements AsyncStatusMgtDAO {

    private static final Log LOG = LogFactory.getLog(AsyncStatusMgtDAOImpl.class);

    @Override
    public String registerAsyncStatusWithoutUpdate(OperationRecord record) throws AsyncStatusMgtException {

        String generatedOperationId;
        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                     CREATE_ASYNC_OPERATION, OPERATION_ID)) {

            statement.setString(CORRELATION_ID, record.getCorrelationId());
            statement.setString(OPERATION_TYPE, record.getOperationType());
            statement.setString(OPERATION_SUBJECT_TYPE, record.getOperationSubjectType());
            statement.setString(OPERATION_SUBJECT_ID, record.getOperationSubjectId());
            statement.setString(INITIATED_ORG_ID, record.getResidentOrgId());
            statement.setString(INITIATED_USER_ID, record.getInitiatorId());
            statement.setString(OPERATION_STATUS, OperationStatus.ONGOING.toString());
            statement.setString(CREATED_AT, currentTimestamp);
            statement.setString(LAST_MODIFIED, currentTimestamp);
            statement.setString(OPERATION_POLICY, record.getOperationPolicy());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                generatedOperationId = generatedKeys.getString(1);
            } else {
                throw new SQLException("Creating operation failed, no ID obtained.");
            }
        } catch (SQLException e) {
            throw new AsyncStatusMgtServerException("Error while adding Async Status Initial information " +
                    "in the system.", e);
        }
        return generatedOperationId;
    }

    @Override
    public String registerAsyncStatusWithUpdate(OperationRecord record) throws AsyncStatusMgtException {

        String generatedOperationId;
        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            deleteOldOperationalData(connection, record.getCorrelationId(), record.getOperationType(),
                    record.getOperationSubjectId());

            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    CREATE_ASYNC_OPERATION, OPERATION_ID)) {
                statement.setString(CORRELATION_ID, record.getCorrelationId());
                statement.setString(OPERATION_TYPE, record.getOperationType());
                statement.setString(OPERATION_SUBJECT_TYPE, record.getOperationSubjectType());
                statement.setString(OPERATION_SUBJECT_ID, record.getOperationSubjectId());
                statement.setString(INITIATED_ORG_ID, record.getResidentOrgId());
                statement.setString(INITIATED_USER_ID, record.getInitiatorId());
                statement.setString(OPERATION_STATUS, OperationStatus.ONGOING.toString());
                statement.setString(CREATED_AT, currentTimestamp);
                statement.setString(LAST_MODIFIED, currentTimestamp);
                statement.setString(OPERATION_POLICY, record.getOperationPolicy());
                statement.executeUpdate();

                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    generatedOperationId = generatedKeys.getString(1);
                } else {
                    throw new SQLException("Creating operation failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new AsyncStatusMgtServerException("Error while adding Async Status Initial information " +
                    "in the system.", e);
        }
        return generatedOperationId;
    }

    @Override
    public void updateAsyncStatus(String operationId, String status) throws AsyncStatusMgtException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(IdentityDatabaseUtil
                .getDBConnection(false), UPDATE_ASYNC_OPERATION)) {
            statement.setString(OPERATION_STATUS, status);
            statement.setString(LAST_MODIFIED, new Timestamp(new Date().getTime()).toString());
            statement.setString(OPERATION_ID, operationId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new AsyncStatusMgtServerException("Error while updating Async Status information in the system.", e);
        }
    }

    @Override
    public void registerAsyncStatusUnit(ConcurrentLinkedQueue<UnitOperationRecord> queue)
            throws AsyncStatusMgtException {

        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                     CREATE_ASYNC_OPERATION_UNIT_BATCH, UNIT_OPERATION_ID)) {

            for (UnitOperationRecord context : queue) {
                statement.setString(OPERATION_ID, context.getOperationId());
                statement.setString(UNIT_OPERATION_RESIDENT_RESOURCE_ID, context.getOperationInitiatedResourceId());
                statement.setString(UNIT_OPERATION_TARGET_ORG_ID, context.getTargetOrgId());
                statement.setString(UNIT_OPERATION_STATUS, context.getUnitOperationStatus());
                statement.setString(UNIT_OPERATION_STATUS_MESSAGE, context.getStatusMessage());
                statement.setString(UNIT_OPERATION_CREATED_AT, currentTimestamp);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new AsyncStatusMgtServerException("Error while adding Async Status Units Initial information " +
                    "in the system.", e);
        }
    }

    @Override
    public List<ResponseOperationRecord> getOperationRecords(String operationSubjectType, String operationSubjectId,
                                                             String operationType, Integer limit,
                                                             List<ExpressionNode> expressionNodes)
            throws AsyncStatusMgtServerException {

        FilterQueryBuilder filterQueryBuilder = buildFilterQuery(expressionNodes, MODEL_CREATED_TIME);
        String sqlStmt = getOperationsStatusSqlStmt(filterQueryBuilder);

        List<ResponseOperationRecord> operationRecords;
        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            operationRecords = namedJdbcTemplate.executeQuery(sqlStmt,
                    (resultSet, rowNumber) -> {
                        ResponseOperationRecord record = new ResponseOperationRecord();
                        record.setOperationId(resultSet.getString(1));
                        record.setCorrelationId(resultSet.getString(2));
                        record.setOperationType(resultSet.getString(3));
                        record.setOperationSubjectType(resultSet.getString(4));
                        record.setOperationSubjectId(resultSet.getString(5));
                        record.setResidentOrgId(resultSet.getString(6));
                        record.setInitiatorId(resultSet.getString(7));
                        record.setOperationStatus(resultSet.getString(8));
                        record.setOperationPolicy(resultSet.getString(9));
                        record.setCreatedTime(Timestamp.valueOf(resultSet.getString(10)));
                        record.setModifiedTime(Timestamp.valueOf(resultSet.getString(11)));
                        return record;
                    },
                    namedPreparedStatement ->
                            setPreparedStatementParamsForGetOperationStatus(namedPreparedStatement,
                                    operationSubjectType, operationSubjectId, operationType, limit,
                                    filterQueryBuilder));
        } catch (DataAccessException e) {
            throw new AsyncStatusMgtServerException("Error while retrieving Async Status information " +
                    "from the system.", e);
        }
        return operationRecords;
    }

    @Override
    public List<ResponseUnitOperationRecord> getUnitOperationRecordsForOperationId(String operationId, Integer limit,
                                                                                   List<ExpressionNode> expressionNodes)
            throws AsyncStatusMgtServerException {

        FilterQueryBuilder filterQueryBuilder = buildFilterQuery(expressionNodes, MODEL_CREATED_AT);
        String sqlStmt = getUnitOperationsStatusSqlStmt(filterQueryBuilder);

        List<ResponseUnitOperationRecord> unitOperationRecords;
        NamedJdbcTemplate namedJdbcTemplate = Utils.getNewTemplate();
        try {
            unitOperationRecords = namedJdbcTemplate.executeQuery(sqlStmt,
                    (resultSet, rowNumber) -> {
                        ResponseUnitOperationRecord record = new ResponseUnitOperationRecord();
                        record.setUnitOperationId(resultSet.getString(1));
                        record.setOperationId(resultSet.getString(2));
                        record.setOperationInitiatedResourceId(resultSet.getString(3));
                        record.setTargetOrgId(resultSet.getString(4));
                        record.setUnitOperationStatus(resultSet.getString(5));
                        record.setStatusMessage(resultSet.getString(6));
                        record.setCreatedTime(Timestamp.valueOf(resultSet.getString(7)));
                        return record;
                    },
                    namedPreparedStatement -> setPreparedStatementParams(namedPreparedStatement,
                            operationId, limit, filterQueryBuilder));
        } catch (DataAccessException e) {
            throw new AsyncStatusMgtServerException("Error while retrieving Async Status Unit information " +
                    "from the system.", e);
        }
        return unitOperationRecords;
    }

    private void deleteOldOperationalData(Connection connection, String correlationId, String operationType,
                                          String operationSubjectId) throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                DELETE_RECENT_OPERATION_RECORD)) {
            statement.setString(OPERATION_TYPE, operationType);
            statement.setString(OPERATION_SUBJECT_ID, operationSubjectId);
            statement.setString(CORRELATION_ID, correlationId);
            statement.executeUpdate();
        }
    }

    private FilterQueryBuilder buildFilterQuery(List<ExpressionNode> expressionNodes, String attributeUsedForCursor)
            throws AsyncStatusMgtServerException {

        FilterQueryBuilder filterQueryBuilder = new FilterQueryBuilder();
        appendFilterQuery(expressionNodes, filterQueryBuilder, attributeUsedForCursor);
        return filterQueryBuilder;
    }

    private void appendFilterQuery(List<ExpressionNode> expressionNodes, FilterQueryBuilder filterQueryBuilder,
                                   String attributeUsedForCursor)
            throws AsyncStatusMgtServerException {

        int count = 1;
        StringBuilder filter = new StringBuilder();
        if (CollectionUtils.isEmpty(expressionNodes)) {
            filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
        } else {
            for (ExpressionNode expressionNode : expressionNodes) {
                String operation = expressionNode.getOperation();
                String value = expressionNode.getValue();
                String attributeValue = expressionNode.getAttributeValue();
                String attributeName = ATTRIBURE_COLUMN_MAP.get(attributeValue);

                if (StringUtils.isNotBlank(attributeName) && StringUtils.isNotBlank(value) && StringUtils
                        .isNotBlank(operation)) {
                    if (UNIT_OPERATION_CREATED_AT.equals(attributeName)) {
                        filterQueryBuilder.addTimestampFilterAttributes(FILTER_PLACEHOLDER_PREFIX);
                    }
                    switch (operation) {
                        case EQ: {
                            equalFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case SW: {
                            startWithFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case EW: {
                            endWithFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case CO: {
                            containsFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case GE: {
                            greaterThanOrEqualFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case LE: {
                            lessThanOrEqualFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case GT: {
                            greaterThanFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        case LT: {
                            lessThanFilterBuilder(count, value, attributeName, filter, filterQueryBuilder);
                            count++;
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            }
            if (StringUtils.isBlank(filter.toString())) {
                filterQueryBuilder.setFilterQuery(StringUtils.EMPTY);
            } else {
                if (filter.toString().endsWith("AND ")) {
                    String filterString = filter.toString();
                    filterQueryBuilder.setFilterQuery(filterString.substring(0, filterString.length() - 4));
                } else {
                    filterQueryBuilder.setFilterQuery(filter.toString());
                }
            }
        }
    }

    private void equalFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                    FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" = :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void startWithFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                        FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value + "%");
    }

    private void endWithFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                      FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, "%" + value);
    }

    private void containsFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, "%" + value + "%");
    }

    private void greaterThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                                 FilterQueryBuilder filterQueryBuilder)
            throws AsyncStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " >= CAST(:%s%s; AS DATETIME) AND "
                : " >= :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void lessThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                              FilterQueryBuilder filterQueryBuilder)
            throws AsyncStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " <= CAST(:%s%s; AS DATETIME) AND "
                : " <= :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void greaterThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                          FilterQueryBuilder filterQueryBuilder)
            throws AsyncStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " > CAST(:%s%s; AS DATETIME) AND "
                : " > :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private void lessThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder)
            throws AsyncStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " < CAST(:%s%s; AS DATETIME) AND "
                : " < :%s%s; AND ", FILTER_PLACEHOLDER_PREFIX, count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue(FILTER_PLACEHOLDER_PREFIX, value);
    }

    private boolean isDateTimeAndMSSql(String attributeName) throws AsyncStatusMgtServerException {

        return (UNIT_OPERATION_CREATED_AT.equals(attributeName)) && isMSSqlDB();
    }

    private static String getOperationsStatusSqlStmt(FilterQueryBuilder filterQueryBuilder)
            throws AsyncStatusMgtServerException {

        if (StringUtils.isNotBlank(filterQueryBuilder.getFilterQuery())) {
            return GET_OPERATIONS + " AND " + filterQueryBuilder.getFilterQuery() + GET_OPERATIONS_TAIL;
        }
        return GET_OPERATIONS + GET_OPERATIONS_TAIL;
    }

    private static String getUnitOperationsStatusSqlStmt(FilterQueryBuilder filterQueryBuilder)
            throws AsyncStatusMgtServerException {

        if (StringUtils.isNotBlank(filterQueryBuilder.getFilterQuery())) {
            return GET_UNIT_OPERATIONS + " AND " + filterQueryBuilder.getFilterQuery() + GET_UNIT_OPERATIONS_TAIL;
        }
        return GET_UNIT_OPERATIONS + GET_UNIT_OPERATIONS_TAIL;
    }

    private void setPreparedStatementParams(NamedPreparedStatement namedPreparedStatement, String operationId,
                                            Integer limit, FilterQueryBuilder filterQueryBuilder)
            throws SQLException {

        namedPreparedStatement.setString(MODEL_OPERATION_ID, operationId);
        setFilterAttributes(namedPreparedStatement, filterQueryBuilder.getFilterAttributeValue(),
                filterQueryBuilder.getTimestampFilterAttributes());
        namedPreparedStatement.setInt(LIMIT, limit);
    }

    private void setPreparedStatementParamsForGetOperationStatus(NamedPreparedStatement namedPreparedStatement,
                                                                 String operationSubjectType, String operationSubjectId,
                                                                 String operationType, Integer limit,
                                                                 FilterQueryBuilder filterQueryBuilder)
            throws SQLException {

        namedPreparedStatement.setString(MODEL_SUBJECT_TYPE, operationSubjectType);
        namedPreparedStatement.setString(MODEL_SUBJECT_ID, operationSubjectId);
        namedPreparedStatement.setString(MODEL_OPERATION_TYPE, operationType);
        setFilterAttributes(namedPreparedStatement, filterQueryBuilder.getFilterAttributeValue(),
                filterQueryBuilder.getTimestampFilterAttributes());
        namedPreparedStatement.setInt(LIMIT, limit);
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
