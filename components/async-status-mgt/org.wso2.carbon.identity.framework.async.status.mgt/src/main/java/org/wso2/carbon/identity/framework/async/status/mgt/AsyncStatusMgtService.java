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

import org.wso2.carbon.identity.framework.async.status.mgt.models.dos.*;

/**
 * Service that processes the status of asynchronous operations.
 */
public interface AsyncStatusMgtService {

    /**
     * Registers the operation status for asynchronous tracking.
     *
     * @param operationType The type of the asynchronous operation being tracked.
     * @param operationSubjectId The unique identifier of the entity the operation is performed on.
     * @param resourceType The type of resource the operation is related to.
     * @param sharingPolicy The sharing policy associated with the resource, if applicable.
     * @param residentOrgId The identifier of the organization where the resource resides, if applicable.
     * @param initiatorId The identifier of the user or system that initiated the operation.
     */
    String registerOperationStatus(String operationType, String operationSubjectId, String resourceType, String sharingPolicy, String residentOrgId, String initiatorId);

    void updateOperationStatus(String operationId, String status);

    /**
     * Registers the unit operation status for asynchronous tracking.
     *
     * @param operationId The identifier of the asynchronous operation being tracked.
     * @param operationInitiatedResourceId The unique identifier of the resource that initiated the operation.
     * @param sharedOrgId The identifier of the organization with which the resource is shared, if applicable.
     * @param unitOperationStatus The status of the unit operation.
     * @param statusMessage A message providing additional information about the operation status.
     */
    void registerUnitOperationStatus(String operationId, String operationType, String operationInitiatedResourceId, String sharedOrgId, String unitOperationStatus, String statusMessage);

    /**
     * Registers the unit operation status for asynchronous tracking.
     *
     * @param context Aggregated unit operation context.
     */
    void registerBulkUnitOperationStatus(ResponseUnitOperationContext context);

    ResponseOperationContext getLatestAsyncOperationStatus(String orgId, String operationSubjectId, String resourceType, String userId);

    void handleOperation(UnitOperationContext unitOperationContext);

    void finalizeOperation(String operationId);

    void addOperation(UnitOperationContext operation);
    void shutdown();


}
