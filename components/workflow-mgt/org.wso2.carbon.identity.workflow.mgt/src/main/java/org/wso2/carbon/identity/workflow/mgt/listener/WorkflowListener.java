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

package org.wso2.carbon.identity.workflow.mgt.listener;

import org.wso2.carbon.identity.workflow.mgt.bean.Entity;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequestAssociation;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequestFilterResponse;
import org.wso2.carbon.identity.workflow.mgt.dto.Association;
import org.wso2.carbon.identity.workflow.mgt.dto.Template;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowEvent;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowImpl;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

import java.util.List;

/**
 * Listener for Workflow Request Delete Process.
 */
public interface WorkflowListener {

    /**
     * Trigger Before Listing Workflow Events.
     *
     * @throws WorkflowException
     */
    void doPreListWorkflowEvents();

    /**
     * Trigger After Listing Workflow Events.
     *
     * @param result Result of the original operation.
     */
    void doPostListWorkflowEvents(List<WorkflowEvent> result);

    /**
     * Trigger before delete the request.
     *
     * @param workflowRequest Request to delete.
     * @throws WorkflowException
     */
    void doPreDeleteWorkflowRequest(WorkflowRequest workflowRequest) throws WorkflowException;

    /**
     * Trigger after deleting the request.
     *
     * @param workflowRequest Request to delete
     * @throws WorkflowException
     */
    void doPostDeleteWorkflowRequest(WorkflowRequest workflowRequest) throws WorkflowException;

    /**
     * Trigger before delete the workflow.
     *
     * @param workflow Workflow to delete.
     * @throws WorkflowException
     */
    void doPreDeleteWorkflow(Workflow workflow) throws WorkflowException;

    /**
     * Trigger before deleting workflows by tenant id.
     *
     * @param tenantId Id of the tenant.
     * @throws WorkflowException
     */
    default void doPreDeleteWorkflows(int tenantId) throws WorkflowException {

    }

    /**
     * Trigger after delete the workflow.
     *
     * @param workflow Workflow to delete.
     * @throws WorkflowException
     */
    void doPostDeleteWorkflow(Workflow workflow) throws WorkflowException;

    /**
     * Trigger after deleting workflows by tenant id.
     *
     * @param tenantId The id of the tenant.
     * @throws WorkflowException
     */
    default void doPostDeleteWorkflows(int tenantId) throws WorkflowException {

    }

    /**
     * Trigger before listing workflow Impls.
     *
     * @param templateId Template ID to trigger workflow Impls.
     * @throws WorkflowException
     */
    void doPreListWorkflowImpls(String templateId) throws WorkflowException;

    /**
     * Trigger after listing workflow Impls.
     *
     * @param templateId Template ID to trigger workflow Impls.
     * @param result     Result of the original operation.
     * @throws WorkflowException
     */
    void doPostListWorkflowImpls(String templateId, List<WorkflowImpl> result) throws WorkflowException;

    /**
     * Trigger before retrieving event.
     *
     * @param id Event ID.
     * @throws WorkflowException
     */
    void doPreGetEvent(String id);

    /**
     * Trigger after retrieving event.
     *
     * @param id     Event ID.
     * @param result Event returned by original method.
     */
    void doPostGetEvent(String id, WorkflowEvent result);

    /**
     * Trigger before retrieving list of workflow templates.
     *
     * @throws WorkflowException
     */
    void doPreListTemplates() throws WorkflowException;

    /**
     * Trigger after retrieving list of workflow templates.
     *
     * @param result Result returned by original operation.
     * @throws WorkflowException
     */
    void doPostListTemplates(List<Template> result) throws WorkflowException;

    /**
     * Trigger before retrieving workflow template.
     *
     * @param templateId Template ID of template to retrieve.
     * @throws WorkflowException
     */
    void doPreGetTemplate(String templateId) throws WorkflowException;

    /**
     * Trigger after retrieving workflow template.
     *
     * @param templateId Template ID of template to retrieve.
     * @param result     Template object retrieved by original method.
     * @throws WorkflowException
     */
    void doPostGetTemplate(String templateId, Template result) throws WorkflowException;

    /**
     * Trigger before retrieving workflow impl.
     *
     * @param templateId     Template id.
     * @param workflowImplId Workflow impl id.
     * @throws WorkflowException
     */
    void doPreGetWorkflowImpl(String templateId, String workflowImplId) throws WorkflowException;

    /**
     * Trigger after retrieving workflow impl.
     *
     * @param templateId     Template id.
     * @param workflowImplId Workflow impl id.
     * @param result         Result returned by original operation.
     * @throws WorkflowException
     */
    void doPostGetWorkflowImpl(String templateId, String workflowImplId, WorkflowImpl result) throws WorkflowException;

    /**
     * Trigger before adding a workflow.
     *
     * @param workflowDTO   Workflow details.
     * @param parameterList List of parameters.
     * @param tenantId      tenant id.
     * @throws WorkflowException
     */
    void doPreAddWorkflow(Workflow workflowDTO,
                          List<Parameter> parameterList, int tenantId) throws WorkflowException;

    /**
     * Trigger after adding a workflow.
     *
     * @param workflowDTO   Workflow details.
     * @param parameterList List of parameters.
     * @param tenantId      tenant id.
     * @throws WorkflowException
     */
    void doPostAddWorkflow(Workflow workflowDTO,
                           List<Parameter> parameterList, int tenantId) throws WorkflowException;

    /**
     * Trigger before retrieving a workflow.
     *
     * @param workflowId Workflow id.
     * @throws WorkflowException
     */
    void doPreGetWorkflow(String workflowId) throws WorkflowException;

    /**
     * Trigger after retrieving a workflow.
     *
     * @param workflowId Workflow id.
     * @param workflow   Workflow returned by original operation.
     * @throws WorkflowException
     */
    void doPostGetWorkflow(String workflowId, Workflow workflow) throws WorkflowException;

    /**
     * Trigger before retrieving parameters of a workflow.
     *
     * @param workflowId Workflow id.
     * @throws WorkflowException
     */
    void doPreGetWorkflowParameters(String workflowId) throws WorkflowException;

    /**
     * Trigger after retrieving parameters of a workflow.
     *
     * @param workflowId Workflow id.
     * @param result     Workflow parameter list returned by original method.
     * @throws WorkflowException
     */
    void doPostGetWorkflowParameters(String workflowId, List<Parameter> result) throws WorkflowException;

    /**
     * Trigger before adding a association.
     *
     * @param associationName Name for the association.
     * @param workflowId      Workflow to associate.
     * @param eventId         Event to associate.
     * @param condition       Condition to check the event for associating.
     * @throws WorkflowException
     */
    void doPreAddAssociation(String associationName, String workflowId, String eventId, String condition) throws
            WorkflowException;

    /**
     * Trigger after adding a association.
     *
     * @param associationName Name for the association.
     * @param workflowId      Workflow to associate.
     * @param eventId         Event to associate.
     * @param condition       Condition to check the event for associating.
     * @throws WorkflowException
     */
    void doPostAddAssociation(String associationName, String workflowId, String eventId, String condition) throws
            WorkflowException;

    /**
     * Trigger before listing workflows of a tenant.
     *
     * @param tenantId Tenant ID.
     * @param limit    Limit.
     * @param offset   Offset.
     * @param filter   Filter.
     * @throws WorkflowException
     */
    default void doPreListPaginatedWorkflows(int tenantId, int limit, int offset, String filter)
            throws WorkflowException {

    }

    /**
     * Trigger after listing workflows of a tenant.
     *
     * @param tenantId Tenant ID.
     * @param limit    Limit.
     * @param offset   Offset.
     * @param filter   Filter.
     * @param result   List of workflows returned by original method.
     * @throws WorkflowException
     */
    default void doPostListPaginatedWorkflows(int tenantId, int limit, int offset, String filter, List<Workflow> result)
            throws WorkflowException {

    }

    /**
     * Trigger before listing workflows of a tenant.
     *
     * @param tenantId Tenant ID.
     * @throws WorkflowException
     * @deprecated Use {@link #doPreListPaginatedWorkflows(int, int, int, String)} instead.
     */
    @Deprecated
    void doPreListWorkflows(int tenantId) throws WorkflowException;

    /**
     * Trigger after listing workflows of a tenant.
     *
     * @param tenantId Tenant ID.
     * @param result   List of workflows returned by original method.
     * @throws WorkflowException
     * @deprecated Use {@link #doPostListPaginatedWorkflows(int, int, int, String, List)} instead.
     */
    @Deprecated
    void doPostListWorkflows(int tenantId, List<Workflow> result) throws WorkflowException;

    /**
     * Trigger before removing an association.
     *
     * @param associationId ID of association to remove.
     * @throws WorkflowException
     */
    void doPreRemoveAssociation(int associationId) throws WorkflowException;

    /**
     * Trigger after removing an association.
     *
     * @param associationId ID of association to remove.
     * @throws WorkflowException
     */
    void doPostRemoveAssociation(int associationId) throws WorkflowException;

    /**
     * Trigger before getting associations of a workflow.
     *
     * @param workflowId Workflow ID.
     * @throws WorkflowException
     */
    void doPreGetAssociationsForWorkflow(String workflowId) throws WorkflowException;

    /**
     * Trigger before getting associations of a workflow.
     *
     * @param workflowId Workflow ID.
     * @param result     Result of the original operation.
     * @throws WorkflowException
     */
    void doPostGetAssociationsForWorkflow(String workflowId, List<Association> result) throws WorkflowException;

    /**
     * Trigger before listing all associations.
     *
     * @param tenantId Tenant ID.
     * @param limit    Limit.
     * @param offset   Offset.
     * @param filter   Filter.
     * @throws WorkflowException
     */
    default void doPreListPaginatedAssociations(int tenantId, int limit, int offset, String filter) {

    }

    /**
     * Trigger after listing all associations.
     *
     * @param tenantId Tenant ID.
     * @param limit    Limit.
     * @param offset   Offset.
     * @param filter   Filter.
     * @param result   Result of the original operation.
     * @throws WorkflowException
     */
    default void doPostListPaginatedAssociations(int tenantId, int limit, int offset, String filter,
                                                 List<Association> result) {

    }

    /**
     * Trigger before listing all associations.
     *
     * @param tenantId Tenant ID.
     * @throws WorkflowException
     * @deprecated Use {@link #doPreListPaginatedAssociations(int, int, int, String)} instead.
     */
    @Deprecated
    void doPreListAllAssociations(int tenantId) throws WorkflowException;

    /**
     * Trigger after listing all associations.
     *
     * @param tenantId Tenant ID.
     * @param result   Result of the original operation.
     * @throws WorkflowException
     * @deprecated Use {@link #doPostListPaginatedAssociations(int, int, int, String, List)} instead.
     */
    @Deprecated
    void doPostListAllAssociations(int tenantId, List<Association> result) throws WorkflowException;

    /**
     * Trigger before getting an association.
     *
     * @param associationId Association ID.
     * @throws WorkflowException
     */
    default void doPreGetAssociation(String associationId) throws WorkflowException {

    }

    /**
     * Trigger after getting an association.
     *
     * @param associationId Association ID.
     * @throws WorkflowException
     */
    default void doPostGetAssociation(String associationId) throws WorkflowException {

    }

    /**
     * Trigger before changing state of an association.
     *
     * @param associationId Association ID.
     * @param isEnable      New state.
     * @throws WorkflowException
     */
    void doPreChangeAssociationState(String associationId, boolean isEnable) throws WorkflowException;

    /**
     * Trigger after changing state of an association.
     *
     * @param associationId Association ID.
     * @param isEnable      New state.
     * @throws WorkflowException
     */
    void doPostChangeAssociationState(String associationId, boolean isEnable) throws WorkflowException;

    /**
     * Trigger before updating an association.
     *
     * @param associationId   Association ID.
     * @param associationName Association Name.
     * @param workflowId      Workflow ID.
     * @param eventId         Event ID.
     * @param condition       Association Condition.
     * @param isEnable        New state.
     * @throws WorkflowException
     */
    default void doPreUpdateAssociation(String associationId, String associationName, String workflowId, String eventId,
                                        String condition, boolean isEnable) {

    }

    /**
     * Trigger after updating an association.
     *
     * @param associationId   Association ID.
     * @param associationName Association Name.
     * @param workflowId      Workflow ID.
     * @param eventId         Event ID.
     * @param condition       Association Condition.
     * @param isEnable        New state.
     * @throws WorkflowException
     */
    default void doPostUpdateAssociation(String associationId, String associationName, String workflowId,
                                         String eventId, String condition, boolean isEnable) {

    }

    /**
     * Trigger before addEntityRequestEntityRelationships.
     *
     * @param requestId Request ID.
     * @param entities  Entity list.
     * @throws WorkflowException
     */
    void doPreAddRequestEntityRelationships(String requestId, Entity[] entities) throws WorkflowException;

    /**
     * Trigger after addEntityRequestEntityRelationships.
     *
     * @param requestId Request ID.
     * @param entities  Entity list.
     * @throws WorkflowException
     */
    void doPostAddRequestEntityRelationships(String requestId, Entity[] entities) throws WorkflowException;

    /**
     * Trigger before checking if entity has pending workflows.
     *
     * @param entity Entity object to test.
     * @throws WorkflowException
     */
    void doPreEntityHasPendingWorkflows(Entity entity) throws WorkflowException;

    /**
     * Trigger after checking if entity has pending workflows.
     *
     * @param entity Entity object to test.
     * @throws WorkflowException
     */
    void doPostEntityHasPendingWorkflows(Entity entity) throws WorkflowException;

    /**
     * Trigger before checking if an entity has pending relationships of a given type.
     *
     * @param entity      Entity object to test.
     * @param requestType Type of request, eg:- Add user.
     * @throws WorkflowException
     */
    void doPreEntityHasPendingWorkflowsOfType(Entity entity, String requestType) throws WorkflowException;

    /**
     * Trigger after checking if an entity has pending relationships of a given type.
     *
     * @param entity      Entity object to test.
     * @param requestType Type of request, eg:- Add user.
     * @throws WorkflowException
     */
    void doPostEntityHasPendingWorkflowsOfType(Entity entity, String requestType) throws WorkflowException;

    /**
     * Trigger before checking if two entities are related.
     *
     * @param entity1 first entity object to test.
     * @param entity2 second entity object to test.
     * @throws WorkflowException
     */
    void doPreAreTwoEntitiesRelated(Entity entity1, Entity entity2) throws WorkflowException;

    /**
     * Trigger after checking if two entities are related.
     *
     * @param entity1 first entity object to test
     * @param entity2 second entity object to test
     * @throws WorkflowException
     */
    void doPostAreTwoEntitiesRelated(Entity entity1, Entity entity2) throws WorkflowException;

    /**
     * Trigger before checking if event is associated with a workflow.
     *
     * @param eventType event type to check.
     * @throws WorkflowException
     */
    void doPreIsEventAssociated(String eventType) throws WorkflowException;

    /**
     * Trigger before checking if event is associated with a workflow.
     *
     * @param eventType event type to check.
     * @throws WorkflowException
     */
    void doPostIsEventAssociated(String eventType) throws WorkflowException;

    /**
     * Trigger before retrieving requests created by user.
     *
     * @param user     User name.
     * @param tenantId tenant ID.
     * @throws WorkflowException
     */
    void doPreGetRequestsCreatedByUser(String user, int tenantId) throws WorkflowException;

    /**
     * Trigger after retrieving requests created by user.
     *
     * @param user     User name.
     * @param tenantId tenant ID.
     * @param results  Results returned by original operation.
     * @throws WorkflowException
     */
    void doPostGetRequestsCreatedByUser(String user, int tenantId, WorkflowRequest[] results) throws WorkflowException;

    /**
     * Trigger before retrieving workflows of request.
     *
     * @param requestId Request ID of request to get workflows of request.
     * @throws WorkflowException
     */
    void doPreGetWorkflowsOfRequest(String requestId) throws WorkflowException;

    /**
     * Trigger after retrieving workflows of request.
     *
     * @param requestId Request ID of request to get workflows of request.
     * @param results   Results returned by original request.
     * @throws WorkflowException
     */
    void doPostGetWorkflowsOfRequest(String requestId, WorkflowRequestAssociation[] results) throws WorkflowException;

    /**
     * Trigger before retrieving requests from filter.
     * 
     * @param user
     * @param beginDate
     * @param endDate
     * @param dateCategory
     * @param tenantId
     * @param status
     * @throws WorkflowException
     * @deprecated Use {@link #doPreGetRequestsFromFilter(String, String, String, String, int, String, int, int)}
     * instead.
     */
    @Deprecated
    default void doPreGetRequestsFromFilter(String user, String beginDate, String endDate, String
            dateCategory, int tenantId, String status) throws WorkflowException{

        }

    /**
     * Trigger after retrieving requests from filter.
     * 
     * @param user
     * @param beginDate
     * @param endDate
     * @param dateCategory
     * @param tenantId
     * @param status
     * @param result
     * @throws WorkflowException
     * @deprecated Use {@link #doPostGetRequestsFromFilter(String, String, String, String, int, String, int, int,
     *  WorkflowRequest[])} instead.
     */
    @Deprecated
    default void doPostGetRequestsFromFilter(String user, String beginDate, String endDate, String
            dateCategory, int tenantId, String status, WorkflowRequest[] result) throws WorkflowException{

        }

    /**
     * Trigger before retrieving requests from filter.
     * 
     * @param user
     * @param operationType
     * @param beginDate
     * @param endDate
     * @param dateCategory
     * @param tenantId
     * @param status
     * @throws WorkflowException
     */
    default void doPreGetRequestsFromFilter(String user, String operationType, String beginDate, String endDate,
                String dateCategory, int tenantId, String status , int limit, int offset) throws WorkflowException {

        }

    /**
     * Trigger after retrieving requests from filter.
     * 
     * @param user
     * @param operationType
     * @param beginDate
     * @param endDate
     * @param dateCategory
     * @param tenantId
     * @param status
     * @param result
     * @throws WorkflowException
     */
     default void doPostGetRequestsFromFilter(String user, String operationType, String beginDate, String endDate,
            String dateCategory, int tenantId, String status, int limit, int offset, 
            WorkflowRequestFilterResponse result) throws WorkflowException {

     }

    /**
     * @param wfOperationType
     * @param wfStatus
     * @param entityType
     * @param tenantID
     * @param idFilter
     * @throws WorkflowException
     */
    void doPreListEntityNames(String wfOperationType, String wfStatus, String entityType, int tenantID, String
            idFilter) throws WorkflowException;

    /**
     * @param wfOperationType
     * @param wfStatus
     * @param entityType
     * @param tenantID
     * @param idFilter
     * @param result
     * @throws WorkflowException
     */
    void doPostListEntityNames(String wfOperationType, String wfStatus, String entityType, int tenantID, String
            idFilter, List<String> result) throws WorkflowException;

    /**
     * Check if listener is enabled or not.
     *
     * @return
     */
    boolean isEnable();

    /**
     * get order ID (priority of current listener).
     *
     * @return
     */
    int getOrderId();

    /**
    * Trigger before retrieving a workflow request.
    *
    * @param requestId ID of the workflow request to retrieve.
    * @throws WorkflowException
    */
    default void doPreGetWorkflowRequest(String requestId) throws WorkflowException {

    }
        
    /**
    * Trigger after retrieving a workflow request.
    *
    * @param requestId ID of the workflow request that was retrieved.
    * @param workflowRequest Workflow request object returned by the original operation.
    * @throws WorkflowException
    */
    default void doPostGetWorkflowRequest(String requestId, WorkflowRequest workflowRequest) throws WorkflowException {

    }
}
