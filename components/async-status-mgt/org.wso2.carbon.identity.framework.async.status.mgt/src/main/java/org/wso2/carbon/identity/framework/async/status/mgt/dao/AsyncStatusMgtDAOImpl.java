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

package org.wso2.carbon.identity.framework.async.status.mgt.dao;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.framework.async.status.mgt.constant.OperationStatus;
import org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants;
import org.wso2.carbon.identity.framework.async.status.mgt.exception.AsyncStatusMgtServerException;
import org.wso2.carbon.identity.framework.async.status.mgt.models.FilterQueryBuilder;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseUnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.ATTRIBURE_COLUMN_MAP;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.CO;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.EQ;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.EW;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.GE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.GT;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.LE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.LT;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.AsyncStatusMgtConstants.SW;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.CREATE_ASYNC_OPERATION_IDN;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.CREATE_ASYNC_OPERATION_UNIT_IDN;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.OperationStatusTableColumns.IDN_OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationStatusTableColumns.IDN_CREATED_AT;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationStatusTableColumns.IDN_OPERATION_STATUS_MESSAGE;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationStatusTableColumns.IDN_RESIDENT_RESOURCE_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationStatusTableColumns.IDN_TARGET_ORG_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_ID;
import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_STATUS;
import static org.wso2.carbon.identity.framework.async.status.mgt.util.Utils.isMSSqlDB;

/**
 * DAO implementation for Asynchronous Operation Status Management.
 */
public class AsyncStatusMgtDAOImpl implements AsyncStatusMgtDAO {

    private static final Logger LOGGER =
            Logger.getLogger(AsyncStatusMgtDAOImpl.class.getName());

    @Override
    public String registerAsyncOperationWithoutUpdate(OperationRecord record) {

        String generatedOperationId;
        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                     CREATE_ASYNC_OPERATION_IDN, IDN_OPERATION_ID)) {

            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_CORRELATION_ID, record.getCorrelationId());
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_TYPE, record.getOperationType());
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_TYPE,
                    record.getOperationSubjectType());
            statement.setString(
                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID, record.getOperationSubjectId());
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_ORG_ID,
                    record.getResidentOrgId());
            statement.setString(
                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_USER_ID, record.getInitiatorId());
            statement.setString(
                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_STATUS, OperationStatus.ONGOING.toString());
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_CREATED_TIME, currentTimestamp);
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_LAST_MODIFIED, currentTimestamp);
            statement.setString(
                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_POLICY, record.getOperationPolicy());

            int rowsAffected = statement.executeUpdate();
            LOGGER.info("Async Operation Registering Success. Rows affected: " + rowsAffected);

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                generatedOperationId = generatedKeys.getString(1);
                LOGGER.info("Generated IDN_OPERATION_ID: " + generatedOperationId);
            } else {
                throw new SQLException("Creating operation failed, no ID obtained.");
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error while registering the asynchronous operation";
            throw new RuntimeException(errorMessage, e);
        }
        return generatedOperationId;
    }

    @Override
    public String registerAsyncOperationWithUpdate(OperationRecord record) {

        String generatedOperationId;
        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            deleteOldOperationalData(connection, record.getCorrelationId(), record.getOperationType(),
                    record.getOperationSubjectId());

            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    CREATE_ASYNC_OPERATION_IDN, IDN_OPERATION_ID)) {
                statement.setString(
                        SQLConstants.OperationStatusTableColumns.IDN_CORRELATION_ID, record.getCorrelationId());
                statement.setString(
                        SQLConstants.OperationStatusTableColumns.IDN_OPERATION_TYPE, record.getOperationType());
                statement.setString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_TYPE,
                        record.getOperationSubjectType());
                statement.setString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID,
                        record.getOperationSubjectId());
                statement.setString(
                        SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_ORG_ID,
                        record.getResidentOrgId());
                statement.setString(
                        SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_USER_ID,
                        record.getInitiatorId());
                statement.setString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_STATUS,
                        OperationStatus.ONGOING.toString());
                statement.setString(SQLConstants.OperationStatusTableColumns.IDN_CREATED_TIME, currentTimestamp);
                statement.setString(SQLConstants.OperationStatusTableColumns.IDN_LAST_MODIFIED, currentTimestamp);
                statement.setString(
                        SQLConstants.OperationStatusTableColumns.IDN_OPERATION_POLICY, record.getOperationPolicy());

                int rowsAffected = statement.executeUpdate();
                LOGGER.info("Async Operation Registering Success. Rows affected: " + rowsAffected);

                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    generatedOperationId = generatedKeys.getString(1);
                    LOGGER.info("Generated IDN_OPERATION_ID: " + generatedOperationId);
                } else {
                    throw new SQLException("Creating operation failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error during Asynchronous operation-wu update (delete and insert).";
            LOGGER.info(errorMessage + e);
            throw new RuntimeException(errorMessage, e);
        }
        return generatedOperationId;
    }

    @Override
    public void registerBulkUnitAsyncOperation(String operationId, String operationType,
                                               ConcurrentLinkedQueue<UnitOperationRecord> queue) {

        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, CREATE_ASYNC_OPERATION_UNIT_IDN,
                     IDN_UNIT_OPERATION_ID)) {

            for (UnitOperationRecord context : queue) {
                statement.setString(
                        SQLConstants.UnitOperationStatusTableColumns.IDN_OPERATION_ID, context.getOperationId());
                statement.setString(IDN_RESIDENT_RESOURCE_ID,
                        context.getOperationInitiatedResourceId());
                statement.setString(
                        IDN_TARGET_ORG_ID, context.getTargetOrgId());
                statement.setString(IDN_UNIT_OPERATION_STATUS,
                        context.getUnitOperationStatus());
                statement.setString(IDN_OPERATION_STATUS_MESSAGE,
                        context.getStatusMessage());
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.IDN_CREATED_AT, currentTimestamp);
                statement.addBatch();
            }

            int[] batchResults = statement.executeBatch();
            LOGGER.info("Batch Unit Operation Registration Success. Total Records Inserted: "
                    + batchResults.length);

        } catch (SQLException e) {
            LOGGER.info("Error during batch unit operation registration.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveOperationsBatch(ConcurrentLinkedQueue<UnitOperationRecord> queue) {

        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, CREATE_ASYNC_OPERATION_UNIT_IDN,
                     IDN_UNIT_OPERATION_ID)) {

            for (UnitOperationRecord context : queue) {
                statement.setString(
                        SQLConstants.UnitOperationStatusTableColumns.IDN_OPERATION_ID, context.getOperationId());
                statement.setString(IDN_RESIDENT_RESOURCE_ID,
                        context.getOperationInitiatedResourceId());
                statement.setString(
                        IDN_TARGET_ORG_ID, context.getTargetOrgId());
                statement.setString(IDN_UNIT_OPERATION_STATUS,
                        context.getUnitOperationStatus());
                statement.setString(IDN_OPERATION_STATUS_MESSAGE,
                        context.getStatusMessage());
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.IDN_CREATED_AT, currentTimestamp);
                statement.addBatch();
            }

            int[] batchResults = statement.executeBatch();
            LOGGER.info("Batch Unit Operation Registration Success. Total Records Inserted: "
                    + batchResults.length);

        } catch (SQLException e) {
            LOGGER.info("Error during batch unit operation registration.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseOperationRecord getLatestAsyncOperationStatus(String operationType, String operationSubjectId) {

        String sql =
                "SELECT IDN_OPERATION_ID, IDN_CORRELATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_TYPE, IDN_OPERATION_SUBJECT_ID," +
                        " IDN_OPERATION_INITIATED_ORG_ID, IDN_OPERATION_INITIATED_USER_ID, IDN_OPERATION_STATUS, IDN_OPERATION_POLICY "
                        + "FROM IDN_ASYNC_OPERATION_STATUS " +
                        "WHERE IDN_OPERATION_TYPE = ? " +
                        "AND IDN_OPERATION_SUBJECT_ID = ? " +
                        "ORDER BY IDN_CREATED_TIME DESC " +
                        "LIMIT 1;";
        ResponseOperationRecord responseContext = new ResponseOperationRecord();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, operationType);
            statement.setString(2, operationSubjectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    responseContext.setOperationId(resultSet.getString(
                            IDN_OPERATION_ID));
                    responseContext.setCorrelationId(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_CORRELATION_ID));
                    responseContext.setOperationType(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_TYPE));
                    responseContext.setOperationSubjectType(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_TYPE));
                    responseContext.setOperationSubjectId(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID));
                    responseContext.setResidentOrgId(
                            resultSet.getString(
                                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_ORG_ID));
                    responseContext.setInitiatorId(
                            resultSet.getString(
                                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_USER_ID));
                    responseContext.setOperationStatus(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_STATUS));
                    responseContext.setOperationPolicy(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_POLICY));
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error fetching latest async operation status for subject: " + operationSubjectId;
            LOGGER.info(errorMessage + e);
            throw new RuntimeException(errorMessage, e);
        }
        LOGGER.info("Fetching latest async operation status for subject: " + operationSubjectId);
        return responseContext;
    }

    @Override
    public List<ResponseOperationRecord> getOperationStatusByOperationTypeAndOperationSubjectId(
            String operationType, String operationSubjectId) {

        String sql =
                "SELECT IDN_OPERATION_ID, IDN_CORRELATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_TYPE, IDN_OPERATION_SUBJECT_ID, " +
                        "IDN_OPERATION_INITIATED_ORG_ID, IDN_OPERATION_INITIATED_USER_ID, IDN_OPERATION_STATUS, IDN_OPERATION_POLICY " +
                        "FROM IDN_ASYNC_OPERATION_STATUS WHERE IDN_OPERATION_TYPE = ? " +
                        "AND IDN_OPERATION_SUBJECT_ID = ? ORDER BY IDN_CREATED_TIME DESC; ";

        List<ResponseOperationRecord> responseContexts = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, operationType);
            statement.setString(2, operationSubjectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ResponseOperationRecord responseContext = new ResponseOperationRecord();
                    responseContext.setOperationId(resultSet.getString(
                            IDN_OPERATION_ID));
                    responseContext.setCorrelationId(resultSet.getString(
                            SQLConstants.OperationStatusTableColumns.IDN_CORRELATION_ID));
                    responseContext.setOperationType(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_TYPE));
                    responseContext.setOperationSubjectType(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_TYPE));
                    responseContext.setOperationSubjectId(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID));
                    responseContext.setResidentOrgId(
                            resultSet.getString(
                                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_ORG_ID));
                    responseContext.setInitiatorId(
                            resultSet.getString(
                                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_USER_ID));
                    responseContext.setOperationStatus(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_STATUS));
                    responseContext.setOperationPolicy(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_POLICY));
                    responseContexts.add(responseContext);
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error fetching async operation status for subject: " + operationSubjectId;
            LOGGER.info(errorMessage + e);
            throw new RuntimeException(errorMessage, e);
        }
        LOGGER.info("Fetching async operation status for subject: " + operationSubjectId);
        return responseContexts;
    }

    @Override
    public List<ResponseOperationRecord> getOperationStatusByOperationSubjectTypeAndOperationSubjectIdAndOperationType(
            String operationSubjectType, String operationSubjectId, String operationType) {

        String sql =
                "SELECT IDN_OPERATION_ID, IDN_CORRELATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_TYPE, IDN_OPERATION_SUBJECT_ID, " +
                        "IDN_OPERATION_INITIATED_ORG_ID, IDN_OPERATION_INITIATED_USER_ID, IDN_OPERATION_STATUS, IDN_OPERATION_POLICY " +
                        "FROM IDN_ASYNC_OPERATION_STATUS WHERE IDN_OPERATION_SUBJECT_TYPE = ? " +
                        "AND IDN_OPERATION_SUBJECT_ID = ? AND IDN_OPERATION_TYPE = ? " +
                        "ORDER BY IDN_CREATED_TIME DESC;";

        List<ResponseOperationRecord> responseContexts = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, operationSubjectType);
            statement.setString(2, operationSubjectId);
            statement.setString(3, operationType);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ResponseOperationRecord responseContext = new ResponseOperationRecord();
                    responseContext.setOperationId(resultSet.getString(
                            IDN_OPERATION_ID));
                    responseContext.setCorrelationId(resultSet.getString(
                            SQLConstants.OperationStatusTableColumns.IDN_CORRELATION_ID));
                    responseContext.setOperationType(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_TYPE));
                    responseContext.setOperationSubjectType(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_TYPE));
                    responseContext.setOperationSubjectId(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID));
                    responseContext.setResidentOrgId(
                            resultSet.getString(
                                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_ORG_ID));
                    responseContext.setInitiatorId(
                            resultSet.getString(
                                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_USER_ID));
                    responseContext.setOperationStatus(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_STATUS));
                    responseContext.setOperationPolicy(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_POLICY));
                    responseContexts.add(responseContext);
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error fetching async operation status for subject: " + operationSubjectId;
            LOGGER.info(errorMessage + e);
            throw new RuntimeException(errorMessage, e);
        }
        LOGGER.info("Fetching async operation status for subject: " + operationSubjectId);
        return responseContexts;
    }

    @Override
    public List<ResponseUnitOperationRecord> getUnitOperationRecordsForOperationId(String operationId, Integer limit,
                                                                                   List<ExpressionNode> expressionNodes)
            throws AsyncStatusMgtServerException {

        FilterQueryBuilder filterQueryBuilder = buildFilterQuery(expressionNodes, "createdTime");
        String sqlStmt = getOrgMetaAttributesSqlStmt(filterQueryBuilder);

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
            throw new RuntimeException(e);
        }
        return unitOperationRecords;
    }

    @Override
    public List<ResponseOperationRecord> getAsyncOperationStatusWithinDays(String operationType,
                                                                           String operationSubjectId, int days) {

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime cutoff = now.minusDays(days);
        Timestamp cutoffTimestamp = Timestamp.valueOf(cutoff);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String cutoffTimestampString = sdf.format(cutoffTimestamp);

        String sql =
                "SELECT IDN_OPERATION_ID, IDN_CORRELATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_ID, IDN_OPERATION_SUBJECT_ID, " +
                        "IDN_OPERATION_INITIATED_ORG_ID, IDN_OPERATION_INITIATED_USER_ID, IDN_OPERATION_STATUS, IDN_OPERATION_POLICY " +
                        "FROM IDN_ASYNC_OPERATION_STATUS WHERE IDN_OPERATION_TYPE = ? " +
                        "AND IDN_OPERATION_SUBJECT_ID = ? AND IDN_CREATED_TIME >= ? " +
                        "ORDER BY IDN_CREATED_TIME DESC;";

        List<ResponseOperationRecord> responseContexts = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, operationType);
            statement.setString(2, operationSubjectId);
            statement.setString(3, cutoffTimestampString);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ResponseOperationRecord responseContext = new ResponseOperationRecord();
                    responseContext.setOperationId(resultSet.getString(
                            IDN_OPERATION_ID));
                    responseContext.setCorrelationId(resultSet.getString(
                            SQLConstants.OperationStatusTableColumns.IDN_CORRELATION_ID));
                    responseContext.setOperationType(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_TYPE));
                    responseContext.setOperationSubjectType(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_TYPE));
                    responseContext.setOperationSubjectId(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID));
                    responseContext.setResidentOrgId(
                            resultSet.getString(
                                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_ORG_ID));
                    responseContext.setInitiatorId(
                            resultSet.getString(
                                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_USER_ID));
                    responseContext.setOperationStatus(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_STATUS));
                    responseContext.setOperationPolicy(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_POLICY));
                    responseContexts.add(responseContext);
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error fetching async operation status for subject: " + operationSubjectId + " within " + days +
                            " days.";
            LOGGER.info(errorMessage + e);
            throw new RuntimeException(errorMessage, e);
        }
        LOGGER.info(
                "Fetching async operation status for subject: " + operationSubjectId
                        + " within " + days + " days.");
        return responseContexts;
    }

    @Override
    public ResponseOperationRecord getLatestAsyncOperationStatusByInitiatorId(String operationType,
                                                                              String operationSubjectId,
                                                                              String initiatorId) {

        String sql =
                "SELECT IDN_OPERATION_ID, IDN_CORRELATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_TYPE, IDN_OPERATION_SUBJECT_ID, " +
                        "IDN_OPERATION_INITIATED_ORG_ID, IDN_OPERATION_INITIATED_USER_ID, IDN_OPERATION_STATUS, IDN_OPERATION_POLICY " +
                        "FROM IDN_ASYNC_OPERATION_STATUS WHERE IDN_OPERATION_TYPE = ? " +
                        "AND IDN_OPERATION_SUBJECT_ID = ? AND IDN_OPERATION_INITIATED_USER_ID = ? " +
                        "ORDER BY IDN_CREATED_TIME DESC LIMIT 1; ";
        ResponseOperationRecord responseContext = new ResponseOperationRecord();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, operationType);
            statement.setString(2, operationSubjectId);
            statement.setString(3, initiatorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    responseContext.setOperationId(resultSet.getString(
                            IDN_OPERATION_ID));
                    responseContext.setCorrelationId(resultSet.getString(
                            SQLConstants.OperationStatusTableColumns.IDN_CORRELATION_ID));
                    responseContext.setOperationType(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_TYPE));
                    responseContext.setOperationSubjectType(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_TYPE));
                    responseContext.setOperationSubjectId(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID));
                    responseContext.setResidentOrgId(
                            resultSet.getString(
                                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_ORG_ID));
                    responseContext.setInitiatorId(
                            resultSet.getString(
                                    SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATED_USER_ID));
                    responseContext.setOperationStatus(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_STATUS));
                    responseContext.setOperationPolicy(
                            resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_POLICY));
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error fetching latest async operation status for subject: " + operationSubjectId;
            LOGGER.info(errorMessage + e);
            throw new RuntimeException(errorMessage, e);
        }
        return responseContext;
    }

    @Override
    public void updateAsyncOperationStatus(String operationId, String status) {

        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        String sql = "UPDATE IDN_ASYNC_OPERATION_STATUS " +
                "SET IDN_OPERATION_STATUS = ?, " +
                "IDN_LAST_MODIFIED = ? " +
                "WHERE IDN_OPERATION_ID = ?";

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setString(2, currentTimestamp);
            statement.setString(3, operationId);

            statement.executeUpdate();
            LOGGER.info("Asynchronous operation updated.");

        } catch (SQLException e) {
            String errorMessage = "Error during Asynchronous operation update.";
            LOGGER.info(errorMessage + e);
            throw new RuntimeException(e);
        }
    }

    private boolean hasExistingRecords(Connection connection, String operationType, String operationSubjectId)
            throws SQLException {

        String checkSql = "SELECT COUNT(*) FROM IDN_ASYNC_OPERATION_STATUS " +
                "WHERE IDN_OPERATION_TYPE = ? AND IDN_OPERATION_SUBJECT_ID = ?";

        try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
            checkStatement.setString(1, operationType);
            checkStatement.setString(2, operationSubjectId);
            try (ResultSet resultSet = checkStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void deleteOldOperationalData(Connection connection, String correlationId, String operationType,
                                          String operationSubjectId) throws SQLException {

        String deleteSql = "DELETE FROM IDN_ASYNC_OPERATION_STATUS " +
                "WHERE IDN_OPERATION_TYPE = ? AND IDN_OPERATION_SUBJECT_ID = ? AND IDN_CORRELATION_ID != ?";

        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
            deleteStatement.setString(1, operationType);
            deleteStatement.setString(2, operationSubjectId);
            deleteStatement.setString(3, correlationId);
            deleteStatement.executeUpdate();
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
                    if (IDN_CREATED_AT.equals(attributeName)) {
                        filterQueryBuilder.addTimestampFilterAttributes("FILTER_ID_");
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
                if(filter.toString().endsWith("AND ")){
                    String filterString = filter.toString();
                    filterQueryBuilder.setFilterQuery(filterString.substring(0, filterString.length() - 4));
                }else{
                    filterQueryBuilder.setFilterQuery(filter.toString());
                }
            }
        }
    }

    private void equalFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                    FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" = :%s%s; AND ", "FILTER_ID_", count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue("FILTER_ID_", value);
    }

    private void startWithFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                        FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; AND ", "FILTER_ID_", count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue("FILTER_ID_", value + "%");
    }

    private void endWithFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                      FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; AND ", "FILTER_ID_", count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue("FILTER_ID_", "%" + value);
    }

    private void containsFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder) {

        String filterString = String.format(" like :%s%s; AND ", "FILTER_ID_", count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue("FILTER_ID_", "%" + value + "%");
    }

    private void greaterThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                                 FilterQueryBuilder filterQueryBuilder)
            throws AsyncStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " >= CAST(:%s%s; AS DATETIME) AND "
                : " >= :%s%s; AND ", "FILTER_ID_", count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue("FILTER_ID_", value);
    }

    private void lessThanOrEqualFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                              FilterQueryBuilder filterQueryBuilder)
            throws AsyncStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " <= CAST(:%s%s; AS DATETIME) AND "
                : " <= :%s%s; AND ", "FILTER_ID_", count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue("FILTER_ID_", value);
    }

    private void greaterThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                          FilterQueryBuilder filterQueryBuilder)
            throws AsyncStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " > CAST(:%s%s; AS DATETIME) AND "
                : " > :%s%s; AND ", "FILTER_ID_", count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue("FILTER_ID_", value);
    }

    private void lessThanFilterBuilder(int count, String value, String attributeName, StringBuilder filter,
                                       FilterQueryBuilder filterQueryBuilder)
            throws AsyncStatusMgtServerException {

        String filterString = String.format(isDateTimeAndMSSql(attributeName) ? " < CAST(:%s%s; AS DATETIME) AND "
                : " < :%s%s; AND ", "FILTER_ID_", count);
        filter.append(attributeName).append(filterString);
        filterQueryBuilder.setFilterAttributeValue("FILTER_ID_", value);
    }

    private boolean isDateTimeAndMSSql(String attributeName) throws AsyncStatusMgtServerException {

        return (IDN_CREATED_AT.equals(attributeName)) && isMSSqlDB();
    }

    private static String getOrgMetaAttributesSqlStmt(FilterQueryBuilder filterQueryBuilder)
            throws AsyncStatusMgtServerException {

        String getUnitOperations = "SELECT IDN_UNIT_OPERATION_ID, IDN_OPERATION_ID, IDN_RESIDENT_RESOURCE_ID, " +
                "IDN_TARGET_ORG_ID, IDN_UNIT_OPERATION_STATUS, IDN_OPERATION_STATUS_MESSAGE, IDN_CREATED_AT " +
                "FROM IDN_ASYNC_OPERATION_STATUS_UNIT WHERE IDN_OPERATION_ID = :OPERATION_ID; ";
        String getUnitOperationsTail = " ORDER BY IDN_CREATED_AT DESC LIMIT :LIMIT; ;";
        if(StringUtils.isNotBlank(filterQueryBuilder.getFilterQuery())){
            return getUnitOperations + " AND " + filterQueryBuilder.getFilterQuery() + getUnitOperationsTail;
        }
        return getUnitOperations + getUnitOperationsTail;
    }

    private void setPreparedStatementParams(NamedPreparedStatement namedPreparedStatement, String operationId,
                                            Integer limit, FilterQueryBuilder filterQueryBuilder)
            throws SQLException {

        namedPreparedStatement.setString("OPERATION_ID", operationId);
        setFilterAttributes(namedPreparedStatement, filterQueryBuilder.getFilterAttributeValue(),
                filterQueryBuilder.getTimestampFilterAttributes());
        namedPreparedStatement.setInt("LIMIT", limit);
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

    private List<ResponseUnitOperationRecord> getUnitOperationRecords(String operationId) {
        String sql =
                "SELECT IDN_UNIT_OPERATION_ID, IDN_OPERATION_ID, IDN_RESIDENT_RESOURCE_ID, IDN_TARGET_ORG_ID, IDN_UNIT_OPERATION_STATUS, " +
                        "IDN_OPERATION_STATUS_MESSAGE, IDN_CREATED_AT " +
                        "FROM IDN_ASYNC_OPERATION_STATUS WHERE IDN_OPERATION_ID = ? " +
                        "ORDER BY IDN_CREATED_TIME DESC;";

        List<ResponseUnitOperationRecord> responseContexts = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, operationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ResponseUnitOperationRecord responseContext = new ResponseUnitOperationRecord();
                    responseContext.setUnitOperationId(resultSet.getString(IDN_UNIT_OPERATION_ID));
                    responseContext.setOperationId(resultSet.getString(IDN_OPERATION_ID));
                    responseContext.setOperationInitiatedResourceId(resultSet.getString(IDN_RESIDENT_RESOURCE_ID));
                    responseContext.setTargetOrgId(resultSet.getString(IDN_TARGET_ORG_ID));
                    responseContext.setUnitOperationStatus(resultSet.getString(IDN_UNIT_OPERATION_STATUS));
                    responseContext.setStatusMessage(resultSet.getString(IDN_OPERATION_STATUS_MESSAGE));
                    responseContext.setCreatedTime(Timestamp.valueOf(resultSet.getString(IDN_CREATED_AT)));
                    responseContexts.add(responseContext);
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error fetching async operation status for operationId: " + operationId;
            LOGGER.info(errorMessage + e);
            throw new RuntimeException(errorMessage, e);
        }
        LOGGER.info("Fetching async operation status for operationId: " + operationId);
        return responseContexts;
    }
}
