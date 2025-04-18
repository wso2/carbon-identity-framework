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

package org.wso2.carbon.identity.framework.async.status.mgt.api.service;

import org.wso2.carbon.identity.framework.async.status.mgt.api.exception.AsyncStatusMgtException;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.ResponseOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.ResponseUnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.api.models.UnitOperationRecord;

import java.util.List;

/**
 * Service that processes the status of asynchronous operations.
 */
//TODO: rename class and package
public interface AsyncStatusMgtService {

    /**
     * Registers a new asynchronous operation status or updates an existing one based on the provided
     * {@link OperationRecord}.
     * This method is used to initiate the tracking of an asynchronous operation,
     * capturing its initial state and metadata.
     * If an operation with the same identifier already exists, the method can either create a new record or
     * update the existing one,depending on the 'updateIfExists' flag.
     *
     * @param record         The {@link OperationRecord} containing details of the asynchronous operation,
     *                       including its type, subject, resource type, resident organization, initiator,
     *                       and initial status.
     * @param updateIfExists A boolean flag that determines whether to update an existing operation record
     *                       if it already exists. If set to true, the existing record will be updated with
     *                       the new information; if false, a new record will be created.
     * @return The unique identifier (operation ID) of the registered or updated asynchronous operation.
     */

    String registerOperationStatus(OperationRecord record, boolean updateIfExists) throws AsyncStatusMgtException;

    /**
     * Updates the status of an existing asynchronous operation identified by its operation ID.
     * This method is used to reflect changes in the operation's state, such as transitioning from
     * 'IN_PROGRESS' to 'COMPLETED' or 'FAILED'.
     *
     * @param operationId The unique identifier of the asynchronous operation to be updated.
     * @param status      The new status of the operation, represented as a string (e.g., "COMPLETED", "FAILED",
     *                    "IN_PROGRESS").
     */
    void updateOperationStatus(String operationId, String status) throws AsyncStatusMgtException;

    /**
     * Registers the status of a unit operation, which is a sub-task within a larger asynchronous operation.
     * This method is used to track the progress and status of individual units of work that make up a complex
     * asynchronous task.
     *
     * @param operation The {@link UnitOperationRecord} containing details of the unit operation, including its
     *                  parent operation ID, resident resource ID, target organization, unit operation status,
     *                  status message, and creation timestamp.
     */
    void registerUnitOperationStatus(UnitOperationRecord operation) throws AsyncStatusMgtException;

    List<ResponseOperationRecord> getOperations(String after, String before, Integer limit, String filter)
            throws AsyncStatusMgtException;

    ResponseOperationRecord getOperation(String operationId) throws AsyncStatusMgtException;

    ResponseUnitOperationRecord getUnitOperation(String unitOperationId) throws AsyncStatusMgtException;

    /**
     * Retrieves the status of unit operations associated with a specific operation ID.
     * This method is used to track the status of sub-tasks within a larger asynchronous operation.
     *
     * @param operationId The unique identifier of the asynchronous operation.
     * @param after       The start timestamp for querying operations. Null or empty if no lower bound is required.
     * @param before      The end timestamp for querying operations. Null or empty if no upper bound is required.
     * @param limit       The maximum number of unit operation records to retrieve.
     * @param filter      A filter expression to further refine the query (e.g., by status or type).
     * @return A list of {@link ResponseUnitOperationRecord} objects representing the unit operation status records.
     * @throws AsyncStatusMgtException If an error occurs while retrieving the unit operation status records.
     */
    List<ResponseUnitOperationRecord> getUnitOperationStatusRecords(String operationId, String after, String before,
                                                                    Integer limit, String filter)
            throws AsyncStatusMgtException;
}
