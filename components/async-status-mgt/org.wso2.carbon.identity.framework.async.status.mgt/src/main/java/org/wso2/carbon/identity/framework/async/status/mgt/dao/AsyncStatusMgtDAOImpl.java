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
import org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants;

import java.sql.*;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationDBContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseOperationContext;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationContext;

import static org.wso2.carbon.identity.framework.async.status.mgt.constant.SQLConstants.*;

/**
 * DAO implementation for Asynchronous Operation Status Management.
 */
public class AsyncStatusMgtDAOImpl implements AsyncStatusMgtDAO {
    private static final Logger LOGGER =
            Logger.getLogger(AsyncStatusMgtDAOImpl.class.getName());

    @Override
    public String registerAsyncOperation(OperationDBContext operationDBContext) {
        LOGGER.info("Async Operation Registering Started.");

        String generatedOperationId = null;

        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                     CREATE_ASYNC_OPERATION_IDN, OperationStatusTableColumns.IDN_OPERATION_ID)) {

            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_TYPE, operationDBContext.getOperationContext().getOperationType());
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID, operationDBContext.getOperationContext().getOperationSubjectId());
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_RESOURCE_TYPE, operationDBContext.getOperationContext().getOperationType());
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_RESIDENT_ORG_ID, operationDBContext.getOperationContext().getResidentOrgId());
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID, operationDBContext.getOperationContext().getInitiatorId());
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_STATUS, OperationStatus.ONGOING.toString());
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_CREATED_TIME, currentTimestamp);
            statement.setString(SQLConstants.OperationStatusTableColumns.IDN_LAST_MODIFIED, currentTimestamp);

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
    public void registerUnitAsyncOperation(String operationId, String residentResourceId, String targetOrgId, String unitOperationStatus, String statusMessage) {
        LOGGER.info("Unit Operation Registration Started.");
        Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
        try{
            try{
                String currentTimestamp = new Timestamp(new Date().getTime()).toString();

                NamedPreparedStatement statement = new NamedPreparedStatement(connection,CREATE_ASYNC_OPERATION_UNIT, SQLConstants.UnitOperationStatusTableColumns.UM_UNIT_OPERATION_ID);
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_OPERATION_ID, operationId);
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_RESIDENT_RESOURCE_ID, residentResourceId);
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_TARGET_ORG_ID, targetOrgId);
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_UNIT_OPERATION_STATUS, unitOperationStatus);
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_OPERATION_STATUS_MESSAGE, statusMessage);
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_CREATED_AT, currentTimestamp);
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
    public void registerBulkUnitAsyncOperation(String operationId, String operationType, ConcurrentLinkedQueue<UnitOperationContext> queue) {
        LOGGER.info("Batch Unit Operation Registration Started.");

        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, CREATE_ASYNC_OPERATION_UNIT_IDN, UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_ID)) {

            for (UnitOperationContext context : queue) {
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
    public void saveOperationsBatch(ConcurrentLinkedQueue<UnitOperationContext> queue) {
        LOGGER.info("Batch Unit Operation Registration Started.");

        String currentTimestamp = new Timestamp(new Date().getTime()).toString();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(connection, CREATE_ASYNC_OPERATION_UNIT_IDN, UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_ID)) {

            for (UnitOperationContext context : queue) {
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
    public ResponseOperationContext getLatestAsyncOperationStatus(String operationSubjectId, String residentOrgId, String resourceType, String initiatorId) {
        LOGGER.info("Fetching latest async operation status for subject: " + operationSubjectId);

        String sql = "SELECT IDN_OPERATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_ID, IDN_RESOURCE_TYPE, " +
                "IDN_RESIDENT_ORG_ID, IDN_OPERATION_INITIATOR_ID, IDN_OPERATION_STATUS " +
                "FROM IDN_ASYNC_OPERATION_STATUS " +
                "WHERE IDN_OPERATION_SUBJECT_ID = ? " +
                "AND IDN_RESIDENT_ORG_ID = ? " +
                "AND IDN_RESOURCE_TYPE = ? " +
                "AND IDN_OPERATION_INITIATOR_ID = ? " +
                "ORDER BY IDN_CREATED_TIME DESC " +
                "LIMIT 1;";
        ResponseOperationContext responseContext = new ResponseOperationContext();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            LOGGER.info(":"+operationSubjectId);
            LOGGER.info(":"+residentOrgId);
            LOGGER.info(":"+resourceType);
            LOGGER.info(":"+initiatorId);

            statement.setString(1, operationSubjectId);
            statement.setString(2, residentOrgId);
            statement.setString(3, resourceType);
            statement.setString(4, initiatorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    responseContext.setOperationId(resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_ID));
                    responseContext.setOperationType(resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_TYPE));
                    responseContext.setOperationSubjectId(resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID));
                    responseContext.setResourceType(resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_RESOURCE_TYPE));
                    responseContext.setResidentOrgId(resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_RESIDENT_ORG_ID));
                    responseContext.setInitiatorId(resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID));
                    responseContext.setOperationStatus(resultSet.getString(SQLConstants.OperationStatusTableColumns.IDN_OPERATION_STATUS));
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
