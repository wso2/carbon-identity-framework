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
package org.wso2.carbon.identity.workflow.mgt;

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

import java.util.Collections;
import java.util.List;


public interface WorkflowManagementService {

    /**
     * List events that can be associated to workflows, eg:- Add user, Addrole, etc
     *
     * @return
     */
    List<WorkflowEvent> listWorkflowEvents();

    /**
     * List implementations of a workflow template
     *
     * @param templateId  ID of template
     * @return
     * @throws WorkflowException
     */
    List<WorkflowImpl> listWorkflowImpls(String templateId) throws WorkflowException;

    /**
     * Retrieve workflow event object from ID
     *
     * @param id  Workflow event ID
     * @return
     */
    WorkflowEvent getEvent(String id);

    /**
     * List existing workflow templates
     *
     * @return
     * @throws WorkflowException
     */
    List<Template> listTemplates() throws WorkflowException;

    /**
     * Retrieve workflow template using workflow ID
     *
     * @param templateId  template id
     * @return
     * @throws WorkflowException
     */
    Template getTemplate(String templateId) throws WorkflowException;

    /**
     * Retrieve worklflow impl object
     *
     * @param templateId     template id
     * @param workflowImplId workflow impl id
     * @return
     * @throws WorkflowException
     */
    WorkflowImpl getWorkflowImpl(String templateId, String workflowImplId) throws WorkflowException;

    /**
     * Add new workflow
     *
     * @param workflowDTO  Workflow details
     * @param parameterList  List of parameters
     * @param tenantId  tenant id
     * @throws WorkflowException
     */
    void addWorkflow(Workflow workflowDTO,
                     List<Parameter> parameterList, int tenantId) throws WorkflowException;

    /**
     * Retrieve workflow from workflow ID
     *
     * @param workflowId  workflow id
     * @return
     * @throws WorkflowException
     */
    Workflow getWorkflow(String workflowId) throws WorkflowException;

    /**
     * List parameters of a workflow
     *
     * @param workflowId  workflow id
     * @return
     * @throws WorkflowException
     */
    List<Parameter> getWorkflowParameters(String workflowId) throws WorkflowException;

    /**
     * Add new workflow association
     *
     * @param associationName  Name for the association
     * @param workflowId  Workflow to associate
     * @param eventId  Event to associate
     * @param condition  Condition to check the event for associating
     * @throws WorkflowException
     */
    void addAssociation(String associationName, String workflowId, String eventId, String condition) throws
                                                                                                     WorkflowException;

    /**
     * List paginated Workflows of a tenant.
     *
     * @param tenantId Tenant Id
     * @param limit    Limit
     * @param offset   Offset
     * @param filter   Filter
     * @return  List<Workflow>
     * @throws WorkflowException
     */
    default List<Workflow> listPaginatedWorkflows(int tenantId, int limit, int offset, String filter) throws WorkflowException {

        return Collections.emptyList();
    }

    /**
     * List workflows
     *
     * @deprecated Use {@link #listPaginatedWorkflows(int, int, int, String)} instead.
     * @param tenantId  Tenant ID
     * @return
     * @throws WorkflowException
     */
    @Deprecated
    List<Workflow> listWorkflows(int tenantId) throws WorkflowException;

    /**
     * Get Workflows count.
     *
     * @param tenantId  Tenant ID
     * @param filter  filter
     * @return Return workflows count
     * @throws WorkflowException
     */
    default int getWorkflowsCount(int tenantId, String filter) throws WorkflowException {

        return 0;
    }

    /**
     * Remove a workflow
     *
     * @param id  ID of workflow to remove
     * @throws WorkflowException
     */
    void removeWorkflow(String id) throws WorkflowException;

    /**
     * Remove all workflows by tenant id.
     *
     * @param tenantId The id of the tenant.
     * @throws WorkflowException throws when an error occurs in removing workflows.
     */
    default void removeWorkflows(int tenantId) throws WorkflowException {

    }

    /**
     * Remove association
     *
     * @param associationId  ID of association to remove
     * @throws WorkflowException
     */
    void removeAssociation(int associationId) throws WorkflowException;

    /**
     * List associations of a specific workflow
     *
     * @param workflowId  Workflow ID
     * @return
     * @throws WorkflowException
     */
    List<Association> getAssociationsForWorkflow(String workflowId) throws WorkflowException;

    /**
     * List paginated associations of a tenant.
     *
     * @param tenantId Tenant Id
     * @param limit    Limit
     * @param offset   Offset
     * @param filter   Filter
     * @return List<Association>
     * @throws WorkflowException
     */
    default List<Association> listPaginatedAssociations(int tenantId, int limit, int offset, String filter) throws WorkflowException {

        return Collections.emptyList();
    }

    /**
     * List all associations of a tenant
     *
     * @deprecated Use {@link #listPaginatedAssociations(int, int, int, String)} instead.
     * @param tenantId  Tenant ID
     * @return
     * @throws WorkflowException
     */
    @Deprecated
    List<Association> listAllAssociations(int tenantId) throws WorkflowException;

    /**
     * Get associations count.
     *
     * @param tenantId  Tenant ID
     * @param filter  filter
     * @return Return associations count
     * @throws WorkflowException
     */
    default int getAssociationsCount(int tenantId, String filter) throws WorkflowException {

        return 0;
    }

    /**
     * Enable or disable association
     *
     * @param associationId  Association ID
     * @param isEnable  New state
     * @throws WorkflowException
     */
    void changeAssociationState(String associationId, boolean isEnable) throws WorkflowException;

     /**
     * Add new relationships for entities
     *
     * @param requestId  Request ID
     * @param entities  Entity list
     * @throws WorkflowException
     */
    void addRequestEntityRelationships(String requestId, Entity[] entities) throws WorkflowException;

    /**
     * Check if given entity has pending workflow associated with it
     *
     * @param entity  Entity object to test
     * @return
     * @throws WorkflowException
     */
    boolean entityHasPendingWorkflows(Entity entity) throws WorkflowException;

    /**
     * Check if a given entity associated with a given object
     *
     * @param entity  Entity object to test
     * @param requestType  Type of request, eg:- Add user
     * @return
     * @throws WorkflowException
     */
    boolean entityHasPendingWorkflowsOfType(Entity entity, String requestType) throws
    WorkflowException;

    /**
     * Check if two entities are related
     *
     * @param entity1  first entity object to test
     * @param entity2  second entity object to test
     * @return
     * @throws WorkflowException
     */
    boolean areTwoEntitiesRelated(Entity entity1, Entity entity2) throws
                                                                  WorkflowException;

    /**
     * Check if a given event is associated with workflows
     *
     * @param eventType  event type to check
     * @return
     * @throws WorkflowException
     */
    boolean isEventAssociated(String eventType) throws WorkflowException;

    /**
     * Get array of request objects initiated by user
     *
     * @param user  User name
     * @param tenantId  tenant ID
     * @return
     * @throws WorkflowException
     */
    WorkflowRequest[] getRequestsCreatedByUser(String user, int tenantId) throws WorkflowException;

    WorkflowRequestAssociation[] getWorkflowsOfRequest(String requestId) throws WorkflowException;

    /**
     * Move workflow requests created by the logged in user to DELETED state.
     *
     * @param requestId Request ID
     * @throws WorkflowException
     */
    void deleteWorkflowRequest(String requestId) throws WorkflowException;

    /**
     * Move workflow requests created by any user to DELETED state.
     *
     * @param requestId Request ID
     * @throws WorkflowException
     */
    default void deleteWorkflowRequestCreatedByAnyUser(String requestId) throws WorkflowException { }

    WorkflowRequest[] getRequestsFromFilter(String user, String beginDate, String endDate, String
            dateCategory, int tenantId, String status) throws WorkflowException;

    List<String> listEntityNames(String wfOperationType, String wfStatus, String entityType, int tenantID, String
            idFilter) throws WorkflowException;
}
