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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.dao;

import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationResponseDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.models.dos.UnitOperationDO;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * DAO interface for Asynchronous Operation Status Management.
 */
public interface AsyncOperationStatusMgtDAO {

    /**
     * Registers a new asynchronous operation without checking for existing records.
     * This method directly inserts a new operation record into the database.
     *
     * @param record The {@link OperationInitDTO} containing the details of the asynchronous operation.
     * @return The unique identifier (operation ID) of the newly registered operation.
     * @throws AsyncOperationStatusMgtException If an error occurs during the registration process.
     */
    String registerAsyncStatusWithoutUpdate(OperationInitDTO record) throws AsyncOperationStatusMgtException;

    /**
     * Registers a new asynchronous operation or updates an existing one if a record with the same operation ID exists.
     * This method allows for idempotent operation registration, ensuring that subsequent calls with the same record
     * either create a new entry or update the existing one.
     *
     * @param record The {@link OperationInitDTO} containing the details of the asynchronous operation.
     * @return The unique identifier (operation ID) of the registered or updated operation.
     * @throws AsyncOperationStatusMgtException If an error occurs during the registration or update process.
     */
    String registerAsyncStatusWithUpdate(OperationInitDTO record) throws AsyncOperationStatusMgtException;

    /**
     * Saves a batch of unit asynchronous operations to the database.
     * This method is optimized for bulk insertion of unit operation records.
     *
     * @param queue A queue containing {@link UnitOperationInitDTO} objects to be saved.
     * @throws AsyncOperationStatusMgtException If an error occurs during the batch insert operation.
     */
    void registerAsyncStatusUnit(ConcurrentLinkedQueue<UnitOperationInitDTO> queue) throws
            AsyncOperationStatusMgtException;

    /**
     * Retrieves a list of asynchronous operations for a given organization.
     * This method supports filtering based on expression nodes and pagination via the limit parameter.
     *
     * @param requestInitiatedOrgId The ID of the organization that initiated the request.
     * @param limit                 The maximum number of operation records to retrieve.
     * @param expressionNodes       A list of {@link ExpressionNode} objects used to filter the query.
     * @return A list of {@link OperationResponseDTO} objects matching the criteria.
     * @throws AsyncOperationStatusMgtException If an error occurs while retrieving operations.
     */
    List<OperationResponseDTO> getOperations(String requestInitiatedOrgId, Integer limit,
                                             List<ExpressionNode> expressionNodes)
            throws AsyncOperationStatusMgtException;

    /**
     * Retrieves a specific asynchronous operation by its ID.
     *
     * @param operationId           The unique identifier of the asynchronous operation.
     * @param requestInitiatedOrgId The ID of the organization that initiated the request.
     * @return The {@link OperationResponseDTO} representing the asynchronous operation.
     * @throws AsyncOperationStatusMgtException If the operation is not found or an error occurs during retrieval.
     */
    OperationResponseDTO getOperation(String operationId, String requestInitiatedOrgId)
            throws AsyncOperationStatusMgtException;

    /**
     * Retrieves a list of unit operation records for a specific operation ID.
     * This method supports pagination and filtering based on various criteria.
     *
     * @param operationId           The unique identifier of the parent operation.
     * @param requestInitiatedOrgId The ID of the organization that initiated the request.
     * @param limit                 The maximum number of unit operation records to retrieve.
     * @param expressionNodes       A list of {@link ExpressionNode} objects used to filter the query.
     * @return A list of {@link UnitOperationDO} objects matching the specified filters.
     * @throws AsyncOperationStatusMgtException If an error occurs while retrieving the unit operation records.
     */
    List<UnitOperationDO> getUnitOperations(String operationId, String requestInitiatedOrgId, Integer limit,
                                            List<ExpressionNode> expressionNodes) throws
            AsyncOperationStatusMgtException;

    /**
     * Retrieves a specific unit operation by its ID.
     *
     * @param unitOperationId       The unique identifier of the unit operation.
     * @param requestInitiatedOrgId The ID of the organization that initiated the request.
     * @return The {@link UnitOperationDO} representing the unit operation.
     * @throws AsyncOperationStatusMgtException If the unit operation is not found or an error occurs during retrieval.
     */
    UnitOperationDO getUnitOperation(String unitOperationId, String requestInitiatedOrgId)
            throws AsyncOperationStatusMgtException;

    /**
     * Updates the status of an existing asynchronous operation.
     *
     * @param operationID The unique identifier of the operation to be updated.
     * @param status      The new status to be assigned to the operation.
     * @throws AsyncOperationStatusMgtException If an error occurs while updating the operation status.
     */
    void updateAsyncStatus(String operationID, OperationStatus status) throws AsyncOperationStatusMgtException;
}
