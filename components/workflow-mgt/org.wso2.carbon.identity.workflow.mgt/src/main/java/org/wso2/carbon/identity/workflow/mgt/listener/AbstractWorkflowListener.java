/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.workflow.mgt.bean.Entity;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequestAssociation;
import org.wso2.carbon.identity.workflow.mgt.dto.Association;
import org.wso2.carbon.identity.workflow.mgt.dto.Template;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowEvent;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowImpl;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

import java.util.List;

public abstract class AbstractWorkflowListener implements WorkflowListener {

    /**
     * Trigger Before Listing Workflow Events
     *
     * @throws WorkflowException
     */
    @Override
    public void doPreListWorkflowEvents() {

    }

    /**
     * Trigger After Listing Workflow Events
     *
     * @param result Result of the original operation
     */
    @Override
    public void doPostListWorkflowEvents(List<WorkflowEvent> result) {

    }

    /**
     * Trigger before delete the request
     *
     * @param workflowRequest Request to delete
     * @throws WorkflowException
     */
    @Override
    public void doPreDeleteWorkflowRequest(WorkflowRequest workflowRequest) throws WorkflowException {

    }

    /**
     * Trigger after deleting the request
     *
     * @param workflowRequest Request to delete
     * @throws WorkflowException
     */
    @Override
    public void doPostDeleteWorkflowRequest(WorkflowRequest workflowRequest) throws WorkflowException {

    }

    /**
     * Trigger before delete the workflow
     *
     * @param workflow Workflow to delete
     * @throws WorkflowException
     */
    @Override
    public void doPreDeleteWorkflow(Workflow workflow) throws WorkflowException {

    }

    /**
     * Trigger after delete the workflow
     *
     * @param workflow Workflow to delete
     * @throws WorkflowException
     */
    @Override
    public void doPostDeleteWorkflow(Workflow workflow) throws WorkflowException {

    }

    /**
     * Trigger after deleting workflows by tenant id.
     *
     * @param tenantId The id of the tenant.
     * @throws WorkflowException
     */
    @Override
    public void doPostDeleteWorkflows(int tenantId) throws WorkflowException {

    }

    /**
     * Trigger before listing workflow Impls
     *
     * @param templateId Template ID to trigger workflow Impls
     * @throws WorkflowException
     */
    @Override
    public void doPreListWorkflowImpls(String templateId) throws WorkflowException {

    }

    /**
     * Trigger after listing workflow Impls
     *
     * @param templateId Template ID to trigger workflow Impls
     * @param result     Result of the original operation
     * @throws WorkflowException
     */
    @Override
    public void doPostListWorkflowImpls(String templateId, List<WorkflowImpl> result) throws WorkflowException {

    }

    /**
     * Trigger before retrieving event
     *
     * @param id Event ID
     * @throws WorkflowException
     */
    @Override
    public void doPreGetEvent(String id) {

    }

    /**
     * Trigger after retrieving event
     *
     * @param id     Event ID
     * @param result Event returned by original method
     */
    @Override
    public void doPostGetEvent(String id, WorkflowEvent result) {

    }

    /**
     * Trigger before retrieving list of workflow templates
     *
     * @throws WorkflowException
     */
    @Override
    public void doPreListTemplates() throws WorkflowException {

    }

    /**
     * Trigger after retrieving list of workflow templates
     *
     * @param result Result returned by original operation
     * @throws WorkflowException
     */
    @Override
    public void doPostListTemplates(List<Template> result) throws WorkflowException {

    }

    /**
     * Trigger before retrieving workflow template
     *
     * @param templateId Template ID of template to retrieve
     * @throws WorkflowException
     */
    @Override
    public void doPreGetTemplate(String templateId) throws WorkflowException {

    }

    /**
     * Trigger after retrieving workflow template
     *
     * @param templateId Template ID of template to retrieve
     * @param result     Template object retrieved by original method
     * @throws WorkflowException
     */
    @Override
    public void doPostGetTemplate(String templateId, Template result) throws WorkflowException {

    }

    /**
     * Trigger before retrieving workflow impl
     *
     * @param templateId     Template id
     * @param workflowImplId Workflow impl id
     * @throws WorkflowException
     */
    @Override
    public void doPreGetWorkflowImpl(String templateId, String workflowImplId) throws WorkflowException {

    }


    /**
     * Trigger after retrieving workflow impl
     *
     * @param templateId     Template id
     * @param workflowImplId Workflow impl id
     * @param result         Result returned by original operation
     * @throws WorkflowException
     */
    @Override
    public void doPostGetWorkflowImpl(String templateId, String workflowImplId, WorkflowImpl result) throws
            WorkflowException {

    }

    /**
     * Trigger before adding a workflow
     *
     * @param workflowDTO   Workflow details
     * @param parameterList List of parameters
     * @param tenantId      tenant id
     * @throws WorkflowException
     */
    @Override
    public void doPreAddWorkflow(Workflow workflowDTO, List<Parameter> parameterList, int tenantId) throws
            WorkflowException {

    }

    /**
     * Trigger after adding a workflow
     *
     * @param workflowDTO   Workflow details
     * @param parameterList List of parameters
     * @param tenantId      tenant id
     * @throws WorkflowException
     */
    @Override
    public void doPostAddWorkflow(Workflow workflowDTO, List<Parameter> parameterList, int tenantId) throws
            WorkflowException {

    }

    /**
     * Trigger before retrieving a workflow
     *
     * @param workflowId Workflow id
     * @throws WorkflowException
     */
    @Override
    public void doPreGetWorkflow(String workflowId) throws WorkflowException {

    }

    /**
     * Trigger after retrieving a workflow
     *
     * @param workflowId Workflow id
     * @param result     Workflow returned by original operation.
     * @throws WorkflowException
     */
    @Override
    public void doPostGetWorkflow(String workflowId, Workflow result) throws WorkflowException {

    }

    /**
     * Trigger before retrieving parameters of a workflow
     *
     * @param workflowId Workflow id
     * @throws WorkflowException
     */
    @Override
    public void doPreGetWorkflowParameters(String workflowId) throws WorkflowException {

    }

    /**
     * Trigger after retrieving parameters of a workflow
     *
     * @param workflowId Workflow id
     * @param result     Workflow parameter list returned by original method
     * @throws WorkflowException
     */
    @Override
    public void doPostGetWorkflowParameters(String workflowId, List<Parameter> result) throws WorkflowException {

    }

    /**
     * Trigger before adding a association
     *
     * @param associationName Name for the association
     * @param workflowId      Workflow to associate
     * @param eventId         Event to associate
     * @param condition       Condition to check the event for associating
     * @throws WorkflowException
     */
    @Override
    public void doPreAddAssociation(String associationName, String workflowId, String eventId, String condition)
            throws WorkflowException {

    }

    /**
     * Trigger after adding a association
     *
     * @param associationName Name for the association
     * @param workflowId      Workflow to associate
     * @param eventId         Event to associate
     * @param condition       Condition to check the event for associating
     * @throws WorkflowException
     */
    @Override
    public void doPostAddAssociation(String associationName, String workflowId, String eventId, String condition)
            throws WorkflowException {

    }

    /**
     * Trigger before listing workflows of a tenant.
     *
     * @param tenantId Tenant ID
     * @param limit    Limit
     * @param offset   Offset
     * @param filter   Filter
     * @throws WorkflowException
     */
    @Override
    public void doPreListPaginatedWorkflows(int tenantId, int limit, int offset, String filter) throws WorkflowException{

    }

    /**
     * Trigger after listing workflows of a tenant.
     *
     * @param tenantId Tenant ID
     * @param limit    Limit
     * @param offset   Offset
     * @param filter   Filter
     * @param result   List of workflows returned by original method.
     * @throws WorkflowException
     */
    @Override
    public void doPostListPaginatedWorkflows(int tenantId, int limit, int offset, String filter, List<Workflow> result) throws WorkflowException{

    }

    /**
     * Trigger before listing workflows of a tenant
     *
     * @deprecated Use {@link #doPreListPaginatedWorkflows(int, int, int, String)} instead.
     * @param tenantId Tenant ID
     * @throws WorkflowException
     */
    @Deprecated
    @Override
    public void doPreListWorkflows(int tenantId) throws WorkflowException {

    }

    /**
     * Trigger after listing workflows of a tenant
     *
     * @deprecated Use {@link #doPostListPaginatedWorkflows(int, int, int, String, List)} instead.
     * @param tenantId Tenant ID
     * @param result   List of workflows returned by original method.
     * @throws WorkflowException
     */
    @Deprecated
    @Override
    public void doPostListWorkflows(int tenantId, List<Workflow> result) throws WorkflowException {

    }

    /**
     * Trigger before removing an association.
     *
     * @param associationId ID of association to remove
     * @throws WorkflowException
     */
    @Override
    public void doPreRemoveAssociation(int associationId) throws WorkflowException {

    }

    /**
     * Trigger after removing an association.
     *
     * @param associationId ID of association to remove
     * @throws WorkflowException
     */
    @Override
    public void doPostRemoveAssociation(int associationId) throws WorkflowException {

    }

    /**
     * Trigger before getting associations of a workflow
     *
     * @param workflowId Workflow ID
     * @throws WorkflowException
     */
    @Override
    public void doPreGetAssociationsForWorkflow(String workflowId) throws WorkflowException {

    }

    /**
     * Trigger before getting associations of a workflow
     *
     * @param workflowId Workflow ID
     * @param result     Result of the original operation
     * @throws WorkflowException
     */
    @Override
    public void doPostGetAssociationsForWorkflow(String workflowId, List<Association> result) throws WorkflowException {

    }

    /**
     * Trigger before listing associations of a tenant.
     *
     * @param tenantId Tenant ID
     * @param limit    Limit
     * @param offset   Offset
     * @param filter   Filter
     * @throws WorkflowException
     */
    @Override
    public void doPreListPaginatedAssociations(int tenantId, int limit, int offset, String filter){

    }

    /**
     * Trigger after listing associations of a tenant.
     *
     * @param tenantId Tenant ID
     * @param limit    Limit
     * @param offset   Offset
     * @param filter   Filter
     * @param result   Result of the original operation
     * @throws WorkflowException
     */
    @Override
    public void doPostListPaginatedAssociations(int tenantId, int limit, int offset, String filter, List<Association> result){

    }

    /**
     * Trigger before listing all associations
     *
     * @deprecated Use {@link #doPreListPaginatedAssociations(int, int, int, String)} instead.
     * @param tenantId Tenant ID
     * @throws WorkflowException
     */
    @Deprecated
    @Override
    public void doPreListAllAssociations(int tenantId) throws WorkflowException {

    }

    /**
     * Trigger after listing all associations
     *
     * @deprecated Use {@link #doPostListPaginatedAssociations(int, int, int, String, List)} instead.
     * @param tenantId Tenant ID
     * @param result   Result of the original operation
     * @throws WorkflowException
     */
    @Deprecated
    @Override
    public void doPostListAllAssociations(int tenantId, List<Association> result) throws WorkflowException {

    }

    /**
     * Trigger before changing state of an association
     *
     * @param associationId Association ID
     * @param isEnable      New state
     * @throws WorkflowException
     */
    @Override
    public void doPreChangeAssociationState(String associationId, boolean isEnable) throws WorkflowException {

    }

    /**
     * Trigger after changing state of an association
     *
     * @param associationId Association ID
     * @param isEnable      New state
     * @throws WorkflowException
     */
    @Override
    public void doPostChangeAssociationState(String associationId, boolean isEnable) throws WorkflowException {

    }

    /**
     * Trigger before addEntityRequestEntityRelationships
     *
     * @param requestId Request ID
     * @param entities  Entity list
     * @throws WorkflowException
     */
    @Override
    public void doPreAddRequestEntityRelationships(String requestId, Entity[] entities) throws WorkflowException {

    }

    /**
     * Trigger after addEntityRequestEntityRelationships
     *
     * @param requestId Request ID
     * @param entities  Entity list @throws WorkflowException
     */
    @Override
    public void doPostAddRequestEntityRelationships(String requestId, Entity[] entities) throws WorkflowException {

    }

    /**
     * Trigger before checking if entity has pending workflows
     *
     * @param entity Entity object to test
     * @throws WorkflowException
     */
    @Override
    public void doPreEntityHasPendingWorkflows(Entity entity) throws WorkflowException {

    }

    /**
     * Trigger after checking if entity has pending workflows
     *
     * @param entity Entity object to test
     * @throws WorkflowException
     */
    @Override
    public void doPostEntityHasPendingWorkflows(Entity entity) throws WorkflowException {

    }

    /**
     * Trigger before checking if an entity has pending relationships of a given type
     *
     * @param entity      Entity object to test
     * @param requestType Type of request, eg:- Add user
     * @throws WorkflowException
     */
    @Override
    public void doPreEntityHasPendingWorkflowsOfType(Entity entity, String requestType) throws WorkflowException {

    }

    /**
     * Trigger after checking if an entity has pending relationships of a given type
     *
     * @param entity      Entity object to test
     * @param requestType Type of request, eg:- Add user
     * @throws WorkflowException
     */
    @Override
    public void doPostEntityHasPendingWorkflowsOfType(Entity entity, String requestType) throws WorkflowException {

    }

    /**
     * Trigger before checking if two entities are related
     *
     * @param entity1 first entity object to test
     * @param entity2 second entity object to test
     * @throws WorkflowException
     */
    @Override
    public void doPreAreTwoEntitiesRelated(Entity entity1, Entity entity2) throws WorkflowException {

    }

    /**
     * Trigger after checking if two entities are related
     *
     * @param entity1 first entity object to test
     * @param entity2 second entity object to test
     * @throws WorkflowException
     */
    @Override
    public void doPostAreTwoEntitiesRelated(Entity entity1, Entity entity2) throws WorkflowException {

    }

    /**
     * Trigger before checking if event is associated with a workflow
     *
     * @param eventType event type to check
     * @throws WorkflowException
     */
    @Override
    public void doPreIsEventAssociated(String eventType) throws WorkflowException {

    }

    /**
     * Trigger after checking if event is associated with a workflow
     *
     * @param eventType event type to check
     * @throws WorkflowException
     */
    @Override
    public void doPostIsEventAssociated(String eventType) throws WorkflowException {

    }

    /**
     * Trigger before retrieving requests created by user
     *
     * @param user     User name
     * @param tenantId tenant ID
     * @throws WorkflowException
     */
    @Override
    public void doPreGetRequestsCreatedByUser(String user, int tenantId) throws WorkflowException {

    }

    /**
     * Trigger after retrieving requests created by user
     *
     * @param user     User name
     * @param tenantId tenant ID
     * @param results  Results returned by original operation
     * @throws WorkflowException
     */
    @Override
    public void doPostGetRequestsCreatedByUser(String user, int tenantId, WorkflowRequest[] results) throws
            WorkflowException {

    }

    /**
     * Trigger before retrieving workflows of request
     *
     * @param requestId Request ID of request to get workflows of
     * @throws WorkflowException
     */
    @Override
    public void doPreGetWorkflowsOfRequest(String requestId) throws WorkflowException {

    }

    /**
     * Trigger after retrieving workflows of request
     *
     * @param requestId Request ID of request to get workflows of
     * @param results   Results returned by original request
     * @throws WorkflowException
     */
    @Override
    public void doPostGetWorkflowsOfRequest(String requestId, WorkflowRequestAssociation[] results) throws
            WorkflowException {

    }

    /**
     * @param user
     * @param beginDate
     * @param endDate
     * @param dateCategory
     * @param tenantId
     * @param status
     * @throws WorkflowException
     */
    @Override
    public void doPreGetRequestsFromFilter(String user, String beginDate, String endDate, String dateCategory, int
            tenantId, String status) throws WorkflowException {

    }

    /**
     * @param user
     * @param beginDate
     * @param endDate
     * @param dateCategory
     * @param tenantId
     * @param status
     * @param result
     * @throws WorkflowException
     */
    @Override
    public void doPostGetRequestsFromFilter(String user, String beginDate, String endDate, String dateCategory, int
            tenantId, String status, WorkflowRequest[] result) throws WorkflowException {

    }

    /**
     * @param wfOperationType
     * @param wfStatus
     * @param entityType
     * @param tenantID
     * @param idFilter
     * @throws WorkflowException
     */
    @Override
    public void doPreListEntityNames(String wfOperationType, String wfStatus, String entityType, int tenantID, String
            idFilter) throws WorkflowException {

    }

    /**
     * @param wfOperationType
     * @param wfStatus
     * @param entityType
     * @param tenantID
     * @param idFilter
     * @param result
     * @throws WorkflowException
     */
    @Override
    public void doPostListEntityNames(String wfOperationType, String wfStatus, String entityType, int tenantID,
                                      String idFilter, List<String> result) throws WorkflowException {

    }

    /**
     * Check if listener is enabled or not.
     *
     * @return
     */
    public boolean isEnable() {
        IdentityEventListenerConfig workflowListener = IdentityUtil.readEventListenerProperty
                (WorkflowListener.class.getName(), this.getClass().getName());

        if (workflowListener == null) {
            return true;
        }

        if (StringUtils.isNotBlank(workflowListener.getEnable())) {
            return Boolean.parseBoolean(workflowListener.getEnable());
        } else {
            return true;
        }
    }

    /**
     * get order ID (priority of current listener)
     *
     * @return
     */
    public int getOrderId() {
        IdentityEventListenerConfig workflowListener = IdentityUtil.readEventListenerProperty
                (WorkflowListener.class.getName(), this.getClass().getName());
        if (workflowListener == null) {
            return IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        }
        return workflowListener.getOrder();
    }
}
