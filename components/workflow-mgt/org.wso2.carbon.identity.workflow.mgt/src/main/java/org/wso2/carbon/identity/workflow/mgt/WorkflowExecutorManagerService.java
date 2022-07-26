/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.workflow.mgt;

import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowAssociation;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

import java.util.List;

/**
 * Workflow Executor Manager service interface.
 */
public interface WorkflowExecutorManagerService {

    /**
     * Get association details related to eventID.
     *
     * @param eventId  Event to associate.
     * @param tenantId TenantID.
     * @return workflowAssociation list.
     * @throws InternalWorkflowException
     */
    List<WorkflowAssociation> getWorkflowAssociationsForRequest(String eventId, int tenantId)
            throws InternalWorkflowException;

    /**
     * Get list of states of workflows of a request.
     *
     * @param requestId request ID that need to e checked.
     * @return status list according to the request ID.
     * @throws InternalWorkflowException
     */
    List<String> getWorkflowStatesOfRequest(String requestId) throws InternalWorkflowException;

    /**
     * Retrieve workflow request specified by the given uuid.
     *
     * @param uuid The uuid of the request to be retrieved
     * @return workflow request object.
     * @throws InternalWorkflowException
     */
    WorkflowRequest retrieveWorkflow(String uuid) throws InternalWorkflowException;

    /**
     * Get requestId of a relationship.
     *
     * @param relationshipId the relationship ID that need to be checked.
     * @return request ID.
     * @throws InternalWorkflowException
     */
    String getRequestIdOfRelationship(String relationshipId) throws InternalWorkflowException;
}
