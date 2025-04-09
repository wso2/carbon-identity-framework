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

package org.wso2.carbon.identity.framework.async.status.mgt.internal.dao;

import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.framework.async.status.mgt.api.exception.AsyncStatusMgtException;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.ResponseOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.ResponseUnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.UnitOperationRecord;

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
    String registerAsyncStatusWithoutUpdate(OperationRecord record) throws AsyncStatusMgtException;

    /**
     * Registers a new asynchronous operation or updates an existing one if a record with the same operation ID exists.
     * This method allows for idempotent operation registration, ensuring that subsequent calls with the same record
     * either create a new entry or update the existing one.
     *
     * @param record The {@link OperationRecord} containing the details of the asynchronous operation.
     * @return The unique identifier (operation ID) of the registered or updated operation.
     */
    String registerAsyncStatusWithUpdate(OperationRecord record) throws AsyncStatusMgtException;

    /**
     * Saves a batch of unit asynchronous operations to the database.
     * This method is optimized for bulk insertion of unit operation records.
     *
     * @param queue A queue containing {@link UnitOperationRecord} objects to be saved.
     */
    void registerAsyncStatusUnit(ConcurrentLinkedQueue<UnitOperationRecord> queue) throws AsyncStatusMgtException;

    /**
     * Retrieves a list of operation records based on the provided filters.
     * This method supports pagination and filtering based on various criteria.
     *
     * @param operationSubjectType The type of the operation subject (e.g., user, organization).
     * @param operationSubjectId   The unique identifier of the operation subject.
     * @param operationType        The type of the operation.
     * @param limit                The maximum number of records to retrieve.
     * @param expressionNodes      A list of {@link ExpressionNode} objects used for filtering the query.
     * @return A list of {@link ResponseOperationRecord} objects matching the specified filters.
     * @throws AsyncStatusMgtException If an error occurs while retrieving the operation records.
     */
    List<ResponseOperationRecord> getOperationRecords(String operationSubjectType, String operationSubjectId,
                                                      String operationType, Integer limit,
                                                      List<ExpressionNode> expressionNodes)
            throws AsyncStatusMgtException;

    /**
     * Retrieves a list of unit operation records for a specific operation ID.
     * This method supports pagination and filtering based on various criteria.
     *
     * @param operationId          The unique identifier of the operation.
     * @param limit                The maximum number of unit operation records to retrieve.
     * @param expressionNodes      A list of {@link ExpressionNode} objects used for filtering the query.
     * @return A list of {@link ResponseUnitOperationRecord} objects matching the specified filters.
     * @throws AsyncStatusMgtException If an error occurs while retrieving the unit operation records.
     */
    List<ResponseUnitOperationRecord> getUnitOperationRecordsForOperationId(String operationId, Integer limit,
                                                                            List<ExpressionNode> expressionNodes)
            throws AsyncStatusMgtException;

    /**
     * Updates the status of an existing asynchronous operation.
     *
     * @param operationID The unique identifier of the operation to be updated.
     * @param status      The new status of the operation.
     */
    void updateAsyncStatus(String operationID, String status) throws AsyncStatusMgtException;
}
