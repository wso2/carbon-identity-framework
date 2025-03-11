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

import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.framework.async.status.mgt.constant.OperationStatus;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseOperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationRecord;

import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.*;

/**
 * DAO implementation for Asynchronous Operation Status Management.
 */
public class AsyncStatusMgtDAOImpl implements AsyncStatusMgtDAO {
    private static final Logger LOGGER =
            Logger.getLogger(AsyncStatusMgtDAOImpl.class.getName());
    
    @Override
    public String registerAsyncOperationWithoutUpdate(OperationRecord record) {
        
        LOGGER.info("Asynchronous operation without update started.");
        String generatedOperationId;
        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                     CREATE_ASYNC_OPERATION_IDN, OperationStatusTableColumns.IDN_OPERATION_ID)) {

            statement.setString(OperationStatusTableColumns.IDN_OPERATION_TYPE, record.getOperationType());
            statement.setString(OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID, record.getOperationSubjectId());
            statement.setString(OperationStatusTableColumns.IDN_RESOURCE_TYPE, record.getOperationPolicy());
            statement.setString(OperationStatusTableColumns.IDN_RESIDENT_ORG_ID, record.getResidentOrgId());
            statement.setString(OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID, record.getInitiatorId());
            statement.setString(OperationStatusTableColumns.IDN_OPERATION_STATUS, OperationStatus.ONGOING.toString());
            statement.setString(OperationStatusTableColumns.IDN_CREATED_TIME, currentTimestamp);
            statement.setString(OperationStatusTableColumns.IDN_LAST_MODIFIED, currentTimestamp);

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
            String deleteSql = "DELETE FROM IDN_ASYNC_OPERATION_STATUS " +
                    "WHERE IDN_OPERATION_ID = ( " +
                    "    SELECT IDN_OPERATION_ID " +
                    "    FROM IDN_ASYNC_OPERATION_STATUS " +
                    "    WHERE IDN_OPERATION_TYPE = ? " +
                    "    AND IDN_OPERATION_SUBJECT_ID = ? " +
                    "    ORDER BY IDN_CREATED_TIME DESC " +
                    "    LIMIT 1 " +
                    ")";

            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                deleteStatement.setString(1, record.getOperationType());
                deleteStatement.setString(2, record.getOperationSubjectId());
                deleteStatement.executeUpdate();
            }

            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    CREATE_ASYNC_OPERATION_IDN, OperationStatusTableColumns.IDN_OPERATION_ID)) {
                statement.setString(OperationStatusTableColumns.IDN_OPERATION_TYPE, record.getOperationType());
                statement.setString(OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID, record.getOperationSubjectId());
                statement.setString(OperationStatusTableColumns.IDN_RESOURCE_TYPE, record.getOperationPolicy());
                statement.setString(OperationStatusTableColumns.IDN_RESIDENT_ORG_ID, record.getResidentOrgId());
                statement.setString(OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID, record.getInitiatorId());
                statement.setString(OperationStatusTableColumns.IDN_OPERATION_STATUS, OperationStatus.ONGOING.toString());
                statement.setString(OperationStatusTableColumns.IDN_CREATED_TIME, currentTimestamp);
                statement.setString(OperationStatusTableColumns.IDN_LAST_MODIFIED, currentTimestamp);

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
            LOGGER.info("Asynchronous operation-wu updated (delete and insert).");
        } catch (SQLException e) {
            String errorMessage = "Error during Asynchronous operation-wu update (delete and insert).";
            LOGGER.info(errorMessage + e);
            throw new RuntimeException(errorMessage, e);
        }
        return generatedOperationId;
    }

    @Override
    public void registerUnitAsyncOperation(String operationId, String residentResourceId, String targetOrgId, String unitOperationStatus, String statusMessage) {
        LOGGER.info("Unit Operation Registration Started.");
        Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
        try{
            try{
                String currentTimestamp = new Timestamp(new Date().getTime()).toString();

                NamedPreparedStatement statement = new NamedPreparedStatement(connection,CREATE_ASYNC_OPERATION_UNIT, UnitOperationStatusTableColumns.UM_UNIT_OPERATION_ID);
                statement.setString(UnitOperationStatusTableColumns.UM_OPERATION_ID, operationId);
                statement.setString(UnitOperationStatusTableColumns.UM_RESIDENT_RESOURCE_ID, residentResourceId);
                statement.setString(UnitOperationStatusTableColumns.UM_TARGET_ORG_ID, targetOrgId);
                statement.setString(UnitOperationStatusTableColumns.UM_UNIT_OPERATION_STATUS, unitOperationStatus);
                statement.setString(UnitOperationStatusTableColumns.UM_OPERATION_STATUS_MESSAGE, statusMessage);
                statement.setString(UnitOperationStatusTableColumns.UM_CREATED_AT, currentTimestamp);
                statement.executeUpdate();
                LOGGER.info("Unit Operation Registration Success.");

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerBulkUnitAsyncOperation(String operationId, String operationType, ConcurrentLinkedQueue<UnitOperationRecord> queue) {
        LOGGER.info("Batch Unit Operation Registration Started.");

        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, CREATE_ASYNC_OPERATION_UNIT_IDN, UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_ID)) {

            for (UnitOperationRecord context : queue) {
                LOGGER.info("OperationId:" + context.getOperationId());
                statement.setString(UnitOperationStatusTableColumns.IDN_OPERATION_ID, context.getOperationId());
                statement.setString(UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_TYPE, "");
                statement.setString(UnitOperationStatusTableColumns.IDN_RESIDENT_RESOURCE_ID, context.getOperationInitiatedResourceId());
                statement.setString(UnitOperationStatusTableColumns.IDN_TARGET_ORG_ID, context.getTargetOrgId());
                statement.setString(UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_STATUS, context.getUnitOperationStatus());
                statement.setString(UnitOperationStatusTableColumns.IDN_OPERATION_STATUS_MESSAGE, context.getStatusMessage());
                statement.setString(UnitOperationStatusTableColumns.IDN_CREATED_AT, currentTimestamp);
                statement.addBatch();
            }

            int[] batchResults = statement.executeBatch();
            LOGGER.info("Batch Unit Operation Registration Success. Total Records Inserted: " + batchResults.length);

        } catch (SQLException e) {
            LOGGER.info("Error during batch unit operation registration.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveOperationsBatch(ConcurrentLinkedQueue<UnitOperationRecord> queue) {
        LOGGER.info("Batch Unit Operation Registration Started.");

        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, CREATE_ASYNC_OPERATION_UNIT_IDN, UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_ID)) {

            for (UnitOperationRecord context : queue) {
                LOGGER.info("OperationId:" + context.getOperationId());
                statement.setString(UnitOperationStatusTableColumns.IDN_OPERATION_ID, context.getOperationId());
                statement.setString(UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_TYPE, "");
                statement.setString(UnitOperationStatusTableColumns.IDN_RESIDENT_RESOURCE_ID, context.getOperationInitiatedResourceId());
                statement.setString(UnitOperationStatusTableColumns.IDN_TARGET_ORG_ID, context.getTargetOrgId());
                statement.setString(UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_STATUS, context.getUnitOperationStatus());
                statement.setString(UnitOperationStatusTableColumns.IDN_OPERATION_STATUS_MESSAGE, context.getStatusMessage());
                statement.setString(UnitOperationStatusTableColumns.IDN_CREATED_AT, currentTimestamp);
                statement.addBatch();
            }

            int[] batchResults = statement.executeBatch();
            LOGGER.info("Batch Unit Operation Registration Success. Total Records Inserted: " + batchResults.length);

        } catch (SQLException e) {
            LOGGER.info("Error during batch unit operation registration.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseOperationContext getLatestAsyncOperationStatus(String resourceType, String operationSubjectId) {
        String sql = "SELECT IDN_OPERATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_ID, IDN_RESOURCE_TYPE, IDN_RESIDENT_ORG_ID, IDN_OPERATION_INITIATOR_ID, IDN_OPERATION_STATUS " +
                "FROM IDN_ASYNC_OPERATION_STATUS " +
                "WHERE IDN_RESOURCE_TYPE = ? " +
                "AND IDN_OPERATION_SUBJECT_ID = ? " +
                "ORDER BY IDN_CREATED_TIME DESC " +
                "LIMIT 1;";
        ResponseOperationContext responseContext = new ResponseOperationContext();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resourceType);
            statement.setString(2, operationSubjectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    responseContext.setOperationId(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_ID));
                    responseContext.setOperationType(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_TYPE));
                    responseContext.setOperationSubjectId(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID));
                    responseContext.setResourceType(resultSet.getString(OperationStatusTableColumns.IDN_RESOURCE_TYPE));
                    responseContext.setResidentOrgId(resultSet.getString(OperationStatusTableColumns.IDN_RESIDENT_ORG_ID));
                    responseContext.setInitiatorId(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID));
                    responseContext.setOperationStatus(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_STATUS));
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error fetching latest async operation status for subject: " + operationSubjectId;
            LOGGER.info(errorMessage+e);
            throw new RuntimeException(errorMessage, e);
        }
        LOGGER.info("Fetching latest async operation status for subject: " + operationSubjectId);
        return responseContext;
    }

    @Override
    public List<ResponseOperationContext> getAsyncOperationStatusWithinDays(String resourceType, String operationSubjectId, int days) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime cutoff = now.minusDays(days);
        Timestamp cutoffTimestamp = Timestamp.valueOf(cutoff);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Ensure UTC time zone
        String cutoffTimestampString = sdf.format(cutoffTimestamp);

        String sql = "SELECT IDN_OPERATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_ID, IDN_RESOURCE_TYPE, IDN_RESIDENT_ORG_ID, IDN_OPERATION_INITIATOR_ID, IDN_OPERATION_STATUS " +
                "FROM IDN_ASYNC_OPERATION_STATUS " +
                "WHERE IDN_RESOURCE_TYPE = ? " +
                "AND IDN_OPERATION_SUBJECT_ID = ? " +
                "AND IDN_CREATED_TIME >= ? " +
                "ORDER BY IDN_CREATED_TIME DESC;";

        List<ResponseOperationContext> responseContexts = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resourceType);
            statement.setString(2, operationSubjectId);
            statement.setString(3, cutoffTimestampString);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ResponseOperationContext responseContext = new ResponseOperationContext();
                    responseContext.setOperationId(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_ID));
                    responseContext.setOperationType(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_TYPE));
                    responseContext.setOperationSubjectId(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID));
                    responseContext.setResourceType(resultSet.getString(OperationStatusTableColumns.IDN_RESOURCE_TYPE));
                    responseContext.setResidentOrgId(resultSet.getString(OperationStatusTableColumns.IDN_RESIDENT_ORG_ID));
                    responseContext.setInitiatorId(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID));
                    responseContext.setOperationStatus(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_STATUS));
                    responseContexts.add(responseContext);
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error fetching async operation status for subject: " + operationSubjectId + " within " + days + " days.";
             LOGGER.info(errorMessage + e);
            throw new RuntimeException(errorMessage, e);
        }
        LOGGER.info("Fetching async operation status for subject: " + operationSubjectId + " within " + days + " days.");
        return responseContexts;
    }

    @Override
    public ResponseOperationContext getLatestAsyncOperationStatusByInitiatorId(String resourceType, String operationSubjectId, String initiatorId) {
        LOGGER.info("Fetching latest async operation status for subject: " + operationSubjectId);

        String sql = "SELECT IDN_OPERATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_ID, IDN_RESOURCE_TYPE, IDN_RESIDENT_ORG_ID, IDN_OPERATION_INITIATOR_ID, IDN_OPERATION_STATUS " +
                "FROM IDN_ASYNC_OPERATION_STATUS " +
                "WHERE IDN_RESOURCE_TYPE = ? " +
                "AND IDN_OPERATION_SUBJECT_ID = ? " +
                "AND IDN_OPERATION_INITIATOR_ID = ? " +
                "ORDER BY IDN_CREATED_TIME DESC " +
                "LIMIT 1;";
        ResponseOperationContext responseContext = new ResponseOperationContext();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resourceType);
            statement.setString(2, operationSubjectId);
            statement.setString(3, initiatorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    responseContext.setOperationId(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_ID));
                    responseContext.setOperationType(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_TYPE));
                    responseContext.setOperationSubjectId(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID));
                    responseContext.setResourceType(resultSet.getString(OperationStatusTableColumns.IDN_RESOURCE_TYPE));
                    responseContext.setResidentOrgId(resultSet.getString(OperationStatusTableColumns.IDN_RESIDENT_ORG_ID));
                    responseContext.setInitiatorId(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID));
                    responseContext.setOperationStatus(resultSet.getString(OperationStatusTableColumns.IDN_OPERATION_STATUS));
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Error fetching latest async operation status for subject: " + operationSubjectId;
            LOGGER.info(errorMessage+e);
            throw new RuntimeException(errorMessage, e);
        }
        return responseContext;
    }

    @Override
    public void updateAsyncOperationStatus(String operationId, String status) {

        LOGGER.info("Asynchronous operation update started.");
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
            LOGGER.info(errorMessage+e);
            throw new RuntimeException(e);
        }
    }
}
