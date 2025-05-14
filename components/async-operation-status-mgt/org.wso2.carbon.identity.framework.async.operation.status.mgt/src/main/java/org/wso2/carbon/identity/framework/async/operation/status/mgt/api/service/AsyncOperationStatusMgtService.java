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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.api.service;

import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.constants.OperationStatus;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.exception.AsyncOperationStatusMgtException;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.OperationResponseDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationInitDTO;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.models.UnitOperationResponseDTO;

import java.util.List;

/**
 * Service that processes the status of asynchronous operations.
 */
public interface AsyncOperationStatusMgtService {

    /**
     * Registers a new asynchronous operation status or updates an existing one based on the provided
     * {@link OperationInitDTO}.
     * This method is used to initiate the tracking of an asynchronous operation,
     * capturing its initial state and metadata.
     * If an operation with the same identifier already exists, the method can either create a new record or
     * update the existing one, depending on the 'updateIfExists' flag.
     *
     * @param record         The {@link OperationInitDTO} containing details of the asynchronous operation,
     *                       including its type, subject, resource type, resident organization, initiator,
     *                       and initial status.
     * @param updateIfExists A boolean flag that determines whether to update an existing operation record
     *                       if it already exists. If set to true, the existing record will be updated with
     *                       the new information; if false, a new record will be created.
     * @return The unique identifier (operation ID) of the registered or updated asynchronous operation.
     * @throws AsyncOperationStatusMgtException If an error occurs while registering or updating the operation status.
     */
    String registerOperationStatus(OperationInitDTO record, boolean updateIfExists) throws
            AsyncOperationStatusMgtException;

    /**
     * Updates the status of an existing asynchronous operation identified by its operation ID.
     * This method is used to reflect changes in the operation's state, such as transitioning from
     * 'IN_PROGRESS' to 'COMPLETED' or 'FAILED'.
     *
     * @param operationId The unique identifier of the asynchronous operation to be updated.
     * @param status      The new status of the operation, represented as an Enum (e.g., "COMPLETED", "FAILED",
     *                    "IN_PROGRESS").
     * @throws AsyncOperationStatusMgtException If an error occurs while updating the operation status.
     */
    void updateOperationStatus(String operationId, OperationStatus status) throws AsyncOperationStatusMgtException;

    /**
     * Registers the status of a unit operation, which is a sub-task within a larger asynchronous operation.
     * This method is used to track the progress and status of individual units of work that make up a complex
     * asynchronous task.
     *
     * @param operation The {@link UnitOperationInitDTO} containing details of the unit operation, including its
     *                  parent operation ID, resident resource ID, target organization, unit operation status,
     *                  status message, and creation timestamp.
     * @throws AsyncOperationStatusMgtException If an error occurs while registering the unit operation.
     */
    void registerUnitOperationStatus(UnitOperationInitDTO operation) throws AsyncOperationStatusMgtException;

    /**
     * Retrieves a list of asynchronous operations based on the provided query parameters.
     * This method supports filtering by creation time range and other criteria.
     *
     * @param tenantDomain The tenant domain for which the operations should be retrieved.
     * @param after        The start timestamp for filtering operations. Can be null or empty if not needed.
     * @param before       The end timestamp for filtering operations. Can be null or empty if not needed.
     * @param limit        The maximum number of operations to return.
     * @param filter       A filter expression (e.g., by status, type, or createdTime).
     * @return A list of {@link OperationResponseDTO} objects matching the criteria.
     * @throws AsyncOperationStatusMgtException If an error occurs while retrieving operations.
     */
    List<OperationResponseDTO> getOperations(String tenantDomain, String after, String before, Integer limit,
                                             String filter) throws AsyncOperationStatusMgtException;

    /**
     * Retrieves the status and metadata of a specific asynchronous operation.
     *
     * @param operationId  The unique identifier of the operation to retrieve.
     * @param tenantDomain The tenant domain associated with the operation.
     * @return The {@link OperationResponseDTO} representing the operation's details.
     * @throws AsyncOperationStatusMgtException If the operation is not found or an error occurs during retrieval.
     */
    OperationResponseDTO getOperation(String operationId, String tenantDomain) throws AsyncOperationStatusMgtException;

    /**
     * Retrieves the status and details of a specific unit operation by its ID.
     *
     * @param unitOperationId The unique identifier of the unit operation.
     * @param tenantDomain    The tenant domain associated with the unit operation.
     * @return A {@link UnitOperationResponseDTO} representing the unit operation's status and metadata.
     * @throws AsyncOperationStatusMgtException If the unit operation is not found or an error occurs during retrieval.
     */
    UnitOperationResponseDTO getUnitOperation(String unitOperationId, String tenantDomain)
            throws AsyncOperationStatusMgtException;

    /**
     * Retrieves the status of unit operations associated with a specific operation ID.
     * This method is used to track the status of sub-tasks within a larger asynchronous operation.
     *
     * @param operationId The unique identifier of the asynchronous operation.
     * @param tenantDomain The tenant domain associated with the unit operations.
     * @param after       The start timestamp for querying operations. Null or empty if no lower bound is required.
     * @param before      The end timestamp for querying operations. Null or empty if no upper bound is required.
     * @param limit       The maximum number of unit operation records to retrieve.
     * @param filter      A filter expression to further refine the query (e.g., by status or type).
     * @return A list of {@link UnitOperationResponseDTO} objects representing the unit operation status records.
     * @throws AsyncOperationStatusMgtException         If an error occurs while retrieving the unit operation status
     * records.
     */
    List<UnitOperationResponseDTO> getUnitOperationStatusRecords(String operationId, String tenantDomain, String after,
                                                                 String before, Integer limit, String filter)
            throws AsyncOperationStatusMgtException;
}
