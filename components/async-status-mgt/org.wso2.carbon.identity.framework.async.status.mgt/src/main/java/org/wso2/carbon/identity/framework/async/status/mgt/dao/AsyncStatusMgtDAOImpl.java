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

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
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
    public String registerAsyncOperation(OperationDBContext context) {
        LOGGER.info("Async Operation Registering Started.");
        Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
        String generatedOperationId = null;
        try{
            String currentTimestamp = new Timestamp(new Date().getTime()).toString();

            NamedPreparedStatement statement = new NamedPreparedStatement(connection,CREATE_ASYNC_OPERATION, SQLConstants.OperationStatusTableColumns.UM_OPERATION_ID);

            statement.setString(SQLConstants.OperationStatusTableColumns.UM_OPERATION_TYPE, context.getOperationContext().getOperationType());
            statement.setString(SQLConstants.OperationStatusTableColumns.UM_OPERATION_SUBJECT_ID, context.getOperationContext().getOperationSubjectId());
            statement.setString(SQLConstants.OperationStatusTableColumns.UM_RESOURCE_TYPE, context.getOperationContext().getOperationType());
            statement.setString(SQLConstants.OperationStatusTableColumns.UM_OPERATION_POLICY, context.getOperationContext().getSharingPolicy());
            statement.setString(SQLConstants.OperationStatusTableColumns.UM_RESIDENT_ORG_ID, context.getOperationContext().getResidentOrgId());
            statement.setString(SQLConstants.OperationStatusTableColumns.UM_OPERATION_INITIATOR_ID, context.getOperationContext().getInitiatorId());
            statement.setString(SQLConstants.OperationStatusTableColumns.UM_OPERATION_STATUS, OperationStatus.ONGOING.toString());
            statement.setString(SQLConstants.OperationStatusTableColumns.UM_CREATED_TIME, currentTimestamp);
            statement.setString(SQLConstants.OperationStatusTableColumns.UM_LAST_MODIFIED, currentTimestamp);

            int rowsAffected = statement.executeUpdate();
            LOGGER.info("B2BUserSharingAsyncOperation Registering Success. Rows affected: " + rowsAffected);

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                generatedOperationId = generatedKeys.getString(1);  // Assuming UM_OPERATION_ID is the first key
                LOGGER.info("Generated UM_OPERATION_ID: " + generatedOperationId);
            } else {
                throw new SQLException("Creating operation failed, no ID obtained.");
            }
        } catch (RuntimeException | SQLException e) {
            throw new RuntimeException(e);
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
        Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);

        try {
            String currentTimestamp = new Timestamp(new Date().getTime()).toString();

            NamedPreparedStatement statement = new NamedPreparedStatement(
                    connection, CREATE_ASYNC_OPERATION_UNIT, SQLConstants.UnitOperationStatusTableColumns.UM_UNIT_OPERATION_ID
            );

            for (UnitOperationContext context : queue) {
                LOGGER.info("OperationId:"+context.getOperationId());
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_OPERATION_ID, context.getOperationId());
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_RESIDENT_RESOURCE_ID, context.getOperationInitiatedResourceId());
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_TARGET_ORG_ID, context.getTargetOrgId());
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_UNIT_OPERATION_STATUS, context.getUnitOperationStatus());
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_OPERATION_STATUS_MESSAGE, context.getStatusMessage());
                statement.setString(SQLConstants.UnitOperationStatusTableColumns.UM_CREATED_AT, currentTimestamp);

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

        ResponseOperationContext responseContext = new ResponseOperationContext();

        String sql = "SELECT UM_OPERATION_ID, UM_OPERATION_TYPE, UM_OPERATION_SUBJECT_ID, UM_RESOURCE_TYPE, " +
                "UM_OPERATION_POLICY, UM_RESIDENT_ORG_ID, UM_OPERATION_INITIATOR_ID, UM_OPERATION_STATUS " +
                "FROM UM_ASYNC_OPERATION_STATUS " +
                "WHERE UM_OPERATION_SUBJECT_ID = ? " +
                "AND UM_RESIDENT_ORG_ID = ? " +
                "AND UM_RESOURCE_TYPE = ? " +
                "AND UM_OPERATION_INITIATOR_ID = ? " +
                "ORDER BY UM_CREATED_TIME DESC " +
                "LIMIT 1;";

        try (Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, operationSubjectId);
            statement.setString(2, residentOrgId);
            statement.setString(3, resourceType);
            statement.setString(4, initiatorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    responseContext.setOperationId(resultSet.getString(SQLConstants.OperationStatusTableColumns.UM_OPERATION_ID));
                    responseContext.setOperationType(resultSet.getString(SQLConstants.OperationStatusTableColumns.UM_OPERATION_TYPE));
                    responseContext.setOperationSubjectId(resultSet.getString(SQLConstants.OperationStatusTableColumns.UM_OPERATION_SUBJECT_ID));
                    responseContext.setResourceType(resultSet.getString(SQLConstants.OperationStatusTableColumns.UM_RESOURCE_TYPE));
                    responseContext.setSharingPolicy(resultSet.getString(SQLConstants.OperationStatusTableColumns.UM_OPERATION_POLICY));
                    responseContext.setResidentOrgId(resultSet.getString(SQLConstants.OperationStatusTableColumns.UM_RESIDENT_ORG_ID));
                    responseContext.setInitiatorId(resultSet.getString(SQLConstants.OperationStatusTableColumns.UM_OPERATION_INITIATOR_ID));
                    responseContext.setOperationStatus(resultSet.getString(SQLConstants.OperationStatusTableColumns.UM_OPERATION_STATUS));
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
        Connection connection = IdentityDatabaseUtil.getUserDBConnection(false);
        try{
            try{
                String currentTimestamp = new Timestamp(new Date().getTime()).toString();

                NamedPreparedStatement statement = new NamedPreparedStatement(connection,UPDATE_ASYNC_OPERATION_STATUS, SQLConstants.OperationStatusTableColumns.UM_OPERATION_ID);
                statement.setString(OperationStatusTableColumns.UM_OPERATION_ID, operationId);
                statement.setString(OperationStatusTableColumns.UM_OPERATION_STATUS, status);
                statement.executeUpdate();
                LOGGER.info("Operation Updated Success.");

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
