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

package org.wso2.carbon.identity.framework.async.status.mgt;

import org.wso2.carbon.identity.framework.async.status.mgt.exception.AsyncStatusMgtClientException;
import org.wso2.carbon.identity.framework.async.status.mgt.exception.AsyncStatusMgtServerException;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.OperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.ResponseUnitOperationRecord;
import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.UnitOperationRecord;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementClientException;

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

    String registerOperationStatus(OperationRecord record, boolean updateIfExists);

    /**
     * Updates the status of an existing asynchronous operation identified by its operation ID.
     * This method is used to reflect changes in the operation's state, such as transitioning from
     * 'IN_PROGRESS' to 'COMPLETED' or 'FAILED'.
     *
     * @param operationId The unique identifier of the asynchronous operation to be updated.
     * @param status      The new status of the operation, represented as a string (e.g., "COMPLETED", "FAILED",
     *                    "IN_PROGRESS").
     */
    void updateOperationStatus(String operationId, String status);

    /**
     * Registers the status of a unit operation, which is a sub-task within a larger asynchronous operation.
     * This method is used to track the progress and status of individual units of work that make up a complex
     * asynchronous task.
     *
     * @param operation The {@link UnitOperationRecord} containing details of the unit operation, including its
     *                  parent operation ID, resident resource ID, target organization, unit operation status,
     *                  status message, and creation timestamp.
     */
    void registerUnitOperationStatus(UnitOperationRecord operation);

    /**
     * Retrieves the latest asynchronous operation status for a specific resource type and operation subject.
     * This method is useful for querying the most recent status of an operation related to a particular resource
     * and subject, providing insight into the current state of asynchronous tasks.
     *
     * @param operationType      The type of the asynchronous operation.
     * @param operationSubjectId The identifier of the subject (e.g., user, application) related to the operation.
     * @return A {@link ResponseOperationRecord} object containing the details of the latest operation status,
     * or null if no matching operation is found.
     */
    List<ResponseOperationRecord> getOperationStatusRecords(String operationType, String operationSubjectId);

    /**
     * Retrieves the latest asynchronous operation status for a specific resource type and operation subject.
     * This method is useful for querying the most recent status of an operation related to a particular resource
     * and subject, providing insight into the current state of asynchronous tasks.
     *
     * @param operationSubjectType The identifier of the subject type(e.g., user, application) related to the operation.
     * @param operationSubjectId   The identifier of the subject (e.g., userId, applicationId) related to the operation.
     * @param operationType        The type of the asynchronous operation.
     * @return A {@link ResponseOperationRecord} object containing the details of the latest operation status,
     * or null if no matching operation is found.
     */
    List<ResponseOperationRecord> getOperationStatusRecords(String operationSubjectType, String operationSubjectId,
                                                            String operationType, String after, String before,
                                                            Integer limit, String filter)
            throws OrganizationManagementClientException, AsyncStatusMgtClientException, AsyncStatusMgtServerException;

    List<ResponseUnitOperationRecord> getUnitOperationStatusRecords(String operationId, String after, String before,
                                                                    Integer limit, String filter)
            throws AsyncStatusMgtClientException, AsyncStatusMgtServerException;
}
