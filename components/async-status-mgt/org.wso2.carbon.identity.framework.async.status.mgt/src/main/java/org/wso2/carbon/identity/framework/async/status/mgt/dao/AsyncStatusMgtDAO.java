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

import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.framework.async.status.mgt.exception.AsyncStatusMgtServerException;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseUnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationRecord;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * DAO interface for Asynchronous Operation
 * Status Management.
 */
public interface AsyncStatusMgtDAO {

    /**
     * Registers a new asynchronous operation without checking for existing records.
     * This method directly inserts a new operation record into the database.
     *
     * @param record The {@link OperationRecord} containing the details of the asynchronous operation.
     * @return The unique identifier (operation ID) of the newly registered operation.
     */
    String registerAsyncOperationWithoutUpdate(OperationRecord record);

    /**
     * Registers a new asynchronous operation or updates an existing one if a record with the same operation ID exists.
     * This method allows for idempotent operation registration, ensuring that subsequent calls with the same record
     * either create a new entry or update the existing one.
     *
     * @param record The {@link OperationRecord} containing the details of the asynchronous operation.
     * @return The unique identifier (operation ID) of the registered or updated operation.
     */
    String registerAsyncOperationWithUpdate(OperationRecord record);

    /**
     * Registers a batch of unit asynchronous operations.
     * This method efficiently inserts multiple unit operation statuses into the database.
     *
     * @param operationId   The unique identifier of the parent asynchronous operation.
     * @param operationType The type of the parent asynchronous operation.
     * @param queue         A queue containing {@link UnitOperationRecord} objects, each representing a unit operation.
     */
    void registerBulkUnitAsyncOperation(String operationId, String operationType,
                                        ConcurrentLinkedQueue<UnitOperationRecord> queue);

    /**
     * Saves a batch of unit asynchronous operations to the database.
     * This method is optimized for bulk insertion of unit operation records.
     *
     * @param queue A queue containing {@link UnitOperationRecord} objects to be saved.
     */
    void saveOperationsBatch(ConcurrentLinkedQueue<UnitOperationRecord> queue);

    /**
     * Retrieves the latest asynchronous operation status for a given resource type and operation subject.
     *
     * @param operationType      The type of the operation.
     * @param operationSubjectId The identifier of the subject related to the operation.
     * @return A {@link ResponseOperationRecord} object containing the latest operation status, or null if not found.
     */
    ResponseOperationRecord getLatestAsyncOperationStatus(String operationType, String operationSubjectId);

    /**
     * Retrieves the latest asynchronous operation status for a given resource type and operation subject.
     *
     * @param operationType      The type of the operation.
     * @param operationSubjectId The identifier of the subject related to the operation.
     * @return A {@link ResponseOperationRecord} object containing the latest operation status, or null if not found.
     */
    List<ResponseOperationRecord> getOperationStatusByOperationTypeAndOperationSubjectId(String operationType,
                                                                                         String operationSubjectId);

    /**
     * Retrieves the latest asynchronous operation status for a given resource type and operation subject.
     *
     * @param operationType      The type of the operation.
     * @param operationSubjectId The identifier of the subject related to the operation.
     * @return A {@link ResponseOperationRecord} object containing the latest operation status, or null if not found.
     */
    List<ResponseOperationRecord> getOperationStatusByOperationSubjectTypeAndOperationSubjectIdAndOperationType(
            String operationSubjectType, String operationSubjectId, String operationType);

    List<ResponseOperationRecord> getOperationRecords(String operationSubjectType, String operationSubjectId,
                                                      String operationType, Integer limit,
                                                      List<ExpressionNode> expressionNodes)
            throws AsyncStatusMgtServerException;

    List<ResponseUnitOperationRecord> getUnitOperationRecordsForOperationId(String operationId, Integer limit,
                                                                            List<ExpressionNode> expressionNodes)
            throws AsyncStatusMgtServerException;

    /**
     * Updates the status of an existing asynchronous operation.
     *
     * @param operationID The unique identifier of the operation to be updated.
     * @param status      The new status of the operation.
     */
    void updateAsyncOperationStatus(String operationID, String status);



}
