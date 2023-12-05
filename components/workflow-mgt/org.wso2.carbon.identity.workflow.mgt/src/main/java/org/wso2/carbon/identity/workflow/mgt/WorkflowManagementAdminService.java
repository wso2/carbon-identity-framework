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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequestAssociation;
import org.wso2.carbon.identity.workflow.mgt.dto.Association;
import org.wso2.carbon.identity.workflow.mgt.dto.Template;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowEvent;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowImpl;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowWizard;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.identity.workflow.mgt.template.AbstractTemplate;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class WorkflowManagementAdminService {

    private static final Log log = LogFactory.getLog(WorkflowManagementAdminService.class);


    private WorkflowWizard getWorkflow(org.wso2.carbon.identity.workflow.mgt.bean.Workflow workflowBean)
            throws WorkflowException {
        try {

            WorkflowWizard workflow = null;

            if (workflowBean != null) {

                workflow = new WorkflowWizard();

                workflow.setWorkflowId(workflowBean.getWorkflowId());
                workflow.setWorkflowName(workflowBean.getWorkflowName());
                workflow.setWorkflowDescription(workflowBean.getWorkflowDescription());
                //workflow.setTemplateId(workflowBean.getTemplateId());
                //workflow.setWorkflowImplId(workflowBean.getWorkflowImplId());

                AbstractTemplate abstractTemplate =
                        WorkflowServiceDataHolder.getInstance().getTemplates().get(workflowBean.getTemplateId());

                Template template = new Template();
                template.setTemplateId(abstractTemplate.getTemplateId());
                template.setName(abstractTemplate.getName());
                template.setDescription(abstractTemplate.getDescription());

                template.setParametersMetaData(abstractTemplate.getParametersMetaData());

                workflow.setTemplate(template);


                AbstractWorkflow abstractWorkflow =
                        WorkflowServiceDataHolder.getInstance().getWorkflowImpls()
                                .get(workflowBean.getTemplateId()).get(workflowBean.getWorkflowImplId());

                WorkflowImpl workflowimpl = new WorkflowImpl();
                workflowimpl.setWorkflowImplId(abstractWorkflow.getWorkflowImplId());
                workflowimpl.setWorkflowImplName(abstractWorkflow.getWorkflowImplName());
                workflowimpl.setTemplateId(abstractWorkflow.getTemplateId());
                workflowimpl.setParametersMetaData(abstractWorkflow.getParametersMetaData());

                workflow.setWorkflowImpl(workflowimpl);

                List<Parameter> workflowParams = WorkflowServiceDataHolder.getInstance().getWorkflowService()
                        .getWorkflowParameters(workflowBean.getWorkflowId());
                List<Parameter> templateParams = new ArrayList<>();
                List<Parameter> workflowImplParams = new ArrayList<>();
                for (Parameter parameter : workflowParams) {
                    if (parameter.getHolder().equals(WFConstant.ParameterHolder.TEMPLATE)) {
                        templateParams.add(parameter);
                    } else if (parameter.getHolder().equals(WFConstant.ParameterHolder.WORKFLOW_IMPL)) {
                        workflowImplParams.add(parameter);
                    }
                }
                workflow.setTemplateParameters(templateParams.toArray(new Parameter[templateParams.size()]));
                workflow.setWorkflowImplParameters(workflowImplParams
                                                           .toArray(new Parameter[workflowImplParams.size()]));

            }
            return workflow;
        } catch (InternalWorkflowException e) {
            String errorMsg =
                    "Error occurred while reading workflow object details for given workflow id, " + e.getMessage();
            log.error(errorMsg, e);
            throw new WorkflowException(errorMsg, e);
        }

    }

    /**
     * Retrieve workflow from workflow ID
     *
     * @param workflowId  workflow id
     * @return
     * @throws WorkflowException
     */
    public WorkflowWizard getWorkflow(String workflowId) throws WorkflowException {
        org.wso2.carbon.identity.workflow.mgt.bean.Workflow workflowBean =
                WorkflowServiceDataHolder.getInstance().getWorkflowService().getWorkflow(workflowId);
        return getWorkflow(workflowBean);
    }

    /**
     * List events that can be associated to workflows, eg:- Add user, Addrole, etc
     *
     * @return
     */
    public WorkflowEvent[] listWorkflowEvents() {

        List<WorkflowEvent> events = WorkflowServiceDataHolder.getInstance().getWorkflowService().listWorkflowEvents();
        return events.toArray(new WorkflowEvent[events.size()]);
    }

    /**
     * List existing workflow templates
     *
     * @return
     * @throws WorkflowException
     */
    public Template[] listTemplates() throws WorkflowException {
        List<Template> templates = WorkflowServiceDataHolder.getInstance().getWorkflowService().listTemplates();
        return templates.toArray(new Template[templates.size()]);
    }

    /**
     * Retrieve workflow template using workflow ID
     *
     * @param templateId  template id
     * @return
     * @throws WorkflowException
     */
    public Template getTemplate(String templateId) throws WorkflowException {
        return WorkflowServiceDataHolder.getInstance().getWorkflowService().getTemplate(templateId);
    }

    /**
     * Retrieve worklflow impl object
     *
     * @param templateId     template id
     * @param implementationId workflow impl id
     * @return
     * @throws WorkflowException
     */
    public WorkflowImpl getWorkflowImpl(String templateId, String implementationId) throws WorkflowException {
        return WorkflowServiceDataHolder.getInstance().getWorkflowService().getWorkflowImpl(templateId,
                                                                                            implementationId);
    }

    /**
     * List implementations of a workflow template
     *
     * @param templateId  ID of template
     * @return
     * @throws WorkflowException
     */
    public WorkflowImpl[] listWorkflowImpls(String templateId) throws WorkflowException {
        List<WorkflowImpl> workflowList =
                WorkflowServiceDataHolder.getInstance().getWorkflowService().listWorkflowImpls(templateId);
        return workflowList.toArray(new WorkflowImpl[workflowList.size()]);
    }

    /**
     * Add new workflow
     *
     * @param workflow  Workflow details
     * @throws WorkflowException
     */
    public void addWorkflow(WorkflowWizard workflow) throws WorkflowException {

        String id = workflow.getWorkflowId();
        if (StringUtils.isBlank(id)) {
            id = UUID.randomUUID().toString();
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            org.wso2.carbon.identity.workflow.mgt.bean.Workflow workflowBean = new org.wso2.carbon.identity.workflow
                    .mgt.bean.Workflow();
            workflowBean.setWorkflowId(id);
            workflowBean.setWorkflowName(workflow.getWorkflowName());
            workflowBean.setWorkflowDescription(workflow.getWorkflowDescription());
            String templateId = workflow.getTemplateId() == null ? workflow.getTemplate().getTemplateId() :
                                workflow.getTemplateId();
            if (templateId == null) {
                throw new WorkflowException("template id can't be empty");
            }
            workflowBean.setTemplateId(templateId);
            String workflowImplId =
                    workflow.getWorkflowImplId() == null ? workflow.getWorkflowImpl().getWorkflowImplId() :
                    workflow.getWorkflowImplId();
            if (workflowImplId == null) {
                throw new WorkflowException("workflowimpl id can't be empty");
            }
            workflowBean.setWorkflowImplId(workflowImplId);

            List<Parameter> parameterList = new ArrayList<>();
            if (workflow.getTemplateParameters() != null) {
                parameterList.addAll(Arrays.asList(workflow.getTemplateParameters()));
            }
            if (workflow.getWorkflowImplParameters() != null) {
                parameterList.addAll(Arrays.asList(workflow.getWorkflowImplParameters()));
            }

            WorkflowServiceDataHolder.getInstance().getWorkflowService()
                    .addWorkflow(workflowBean, parameterList, tenantId);

        } catch (WorkflowRuntimeException e) {
            log.error("Error when adding workflow " + workflow.getWorkflowName(), e);
            throw new WorkflowException(e.getMessage());
        } catch (WorkflowException e) {
            log.error("Server error when adding workflow " + workflow.getWorkflowName(), e);
            throw new WorkflowException("Server error occurred when adding the workflow");
        }
    }

    /**
     * Add new workflow association
     *
     * @param associationName  Name for the association
     * @param workflowId  Workflow to associate
     * @param eventId  Event to associate
     * @param condition  Condition to check the event for associating
     * @throws WorkflowException
     */
    public void addAssociation(String associationName, String workflowId, String eventId, String condition)
            throws WorkflowException {

        try {
            WorkflowServiceDataHolder.getInstance().getWorkflowService()
                    .addAssociation(associationName, workflowId, eventId, condition);
        } catch (WorkflowRuntimeException e) {
            log.error("Error when adding association " + associationName, e);
            throw new WorkflowException(e.getMessage());
        } catch (WorkflowException e) {
            log.error("Server error when adding association of workflow " + workflowId + " with " + eventId, e);
            throw new WorkflowException("Server error occurred when associating the workflow with the event");
        }
    }

    /**
     * Enable or disable association
     *
     * @param associationId  Association ID
     * @param isEnable  New state
     * @throws WorkflowException
     */
    public void changeAssociationState(String associationId, boolean isEnable) throws WorkflowException {
        try {
            WorkflowServiceDataHolder.getInstance().getWorkflowService()
                    .changeAssociationState(associationId, isEnable);
        } catch (WorkflowRuntimeException e) {
            log.error("Error when changing an association ", e);
            throw new WorkflowException(e.getMessage());
        } catch (WorkflowException e) {
            log.error("Server error when changing state of association ", e);
            throw new WorkflowException("Server error occurred when changing the state of association");
        }

    }

    /**
     * List paginated workflows of a tenant.
     *
     * @param limit  Limit
     * @param offset Offset
     * @param filter filter
     * @return WorkflowWizard[]
     * @throws WorkflowException
     */
    public WorkflowWizard[] listPaginatedWorkflows(int limit, int offset, String filter) throws WorkflowException{

        List<WorkflowWizard> workflowWizards = new ArrayList<>();
        List<Workflow> workflowBeans = null;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            workflowBeans = WorkflowServiceDataHolder.getInstance().getWorkflowService().listPaginatedWorkflows(tenantId, limit, offset, filter);
            for (Workflow workflow : workflowBeans) {
                WorkflowWizard workflowTmp = getWorkflow(workflow);
                workflowWizards.add(workflowTmp);
            }
        } catch (InternalWorkflowException e) {
            throw new WorkflowException(WFConstant.Exceptions.ERROR_LISTING_WORKFLOWS, e);
        }
        return workflowWizards.toArray(new WorkflowWizard[workflowWizards.size()]);
    }

    /**
     * List workflows
     *
     * @return
     * @throws WorkflowException
     */
    public WorkflowWizard[] listWorkflows() throws WorkflowException {

        List<WorkflowWizard> workflowWizards = new ArrayList<>();
        List<Workflow> workflowBeans = null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            workflowBeans = WorkflowServiceDataHolder.getInstance().getWorkflowService().listWorkflows(tenantId);
            for (Workflow workflow : workflowBeans) {
                WorkflowWizard workflowTmp = getWorkflow(workflow);
                workflowWizards.add(workflowTmp);
            }
        } catch (InternalWorkflowException e) {
            throw new WorkflowException(WFConstant.Exceptions.ERROR_LISTING_WORKFLOWS, e);
        }
        return workflowWizards.toArray(new WorkflowWizard[workflowWizards.size()]);
    }

    /**
     * Get workflows count.
     *
     * @param filter  filter
     * @return Return count of workflows
     * @throws WorkflowException
     */
    public int getWorkflowsCount(String filter) throws WorkflowException{

        int count;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            count = WorkflowServiceDataHolder.getInstance().getWorkflowService().getWorkflowsCount(tenantId, filter);
        } catch (InternalWorkflowException e) {
            throw new WorkflowException(WFConstant.Exceptions.ERROR_GETTING_WORKFLOW_COUNT, e);
        }
        return count;
    }

    /**
     * Remove a workflow
     *
     * @param id  ID of workflow to remove
     * @throws WorkflowException
     */
    public void removeWorkflow(String id) throws WorkflowException {

        try {
            WorkflowServiceDataHolder.getInstance().getWorkflowService().removeWorkflow(id);
        } catch (InternalWorkflowException e) {
            log.error("Server error when removing workflow " + id, e);
            throw new WorkflowException("Server error occurred when removing workflow");
        }
    }

    /**
     * Remove workflows by a given tenant id.
     *
     * @param tenantId The id of the tenant to remove the workflows.
     * @throws WorkflowException
     */
    public void removeWorkflows(int tenantId) throws WorkflowException {

        try {
            WorkflowServiceDataHolder.getInstance().getWorkflowService().removeWorkflows(tenantId);
        } catch (InternalWorkflowException e) {
            log.error("Server error when removing workflows of the tenant " + tenantId, e);
            throw new WorkflowException("Server error occurred when removing workflows");
        }
    }

    /**
     * Remove association
     *
     * @param associationId  ID of association to remove
     * @throws WorkflowException
     */
    public void removeAssociation(String associationId) throws WorkflowException {

        try {
            WorkflowServiceDataHolder.getInstance().getWorkflowService()
                    .removeAssociation(Integer.parseInt(associationId));
        } catch (InternalWorkflowException e) {
            log.error("Server error when removing association " + associationId, e);
            throw new WorkflowException("Server error occurred when removing association");
        }
    }

    /**
     * List paginated associations of a tenant.
     *
     * @param limit  Limit
     * @param offset Offset
     * @param filter Filter
     * @return Association[]
     * @throws WorkflowException
     */
    public Association[] listPaginatedAssociations(int limit, int offset, String filter) throws WorkflowException {

        List<Association> associations;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            associations =
                    WorkflowServiceDataHolder.getInstance().getWorkflowService().listPaginatedAssociations(tenantId, limit, offset, filter);
        } catch (InternalWorkflowException e) {
            throw new WorkflowException(WFConstant.Exceptions.ERROR_LISTING_ASSOCIATIONS, e);
        }
        if (CollectionUtils.isEmpty(associations)) {
            return new Association[0];
        }
        return associations.toArray(new Association[associations.size()]);
    }

    /**
     * List associations of a specific workflow
     *
     * @param workflowId  Workflow ID
     * @return Association[]
     * @throws WorkflowException
     */
    public Association[] listAssociations(String workflowId) throws WorkflowException {

        List<Association> associations;
        try {
            associations =
                    WorkflowServiceDataHolder.getInstance().getWorkflowService().getAssociationsForWorkflow(workflowId);
        } catch (InternalWorkflowException e) {
            throw new WorkflowException("Server error when listing associations for workflow id:" + workflowId, e);
        }
        if (CollectionUtils.isEmpty(associations)) {
            return new Association[0];
        }
        return associations.toArray(new Association[associations.size()]);
    }

    /**
     * List all associations
     *
     * @return
     * @throws WorkflowException
     */
    public Association[] listAllAssociations() throws WorkflowException {

        List<Association> associations;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            associations = WorkflowServiceDataHolder.getInstance().getWorkflowService().listAllAssociations(tenantId);
        } catch (InternalWorkflowException e) {
            throw new WorkflowException(WFConstant.Exceptions.ERROR_LISTING_ASSOCIATIONS, e);
        }
        if (CollectionUtils.isEmpty(associations)) {
            return new Association[0];
        }
        return associations.toArray(new Association[associations.size()]);
    }

    /**
     * Get associations count.
     *
     * @param filter  filter
     * @return Return count of associations
     * @throws WorkflowException
     */
    public int getAssociationsCount(String filter) throws WorkflowException{

        int count;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            count = WorkflowServiceDataHolder.getInstance().getWorkflowService().getAssociationsCount(tenantId, filter);
        } catch (InternalWorkflowException e) {
            throw new WorkflowException(WFConstant.Exceptions.ERROR_GETTING_ASSOC_COUNT, e);
        }
        return count;
    }

    /**
     * Retrieve workflow event object from ID
     *
     * @param eventId  Workflow event ID
     * @return
     */
    public WorkflowEvent getEvent(String eventId) {

        return WorkflowServiceDataHolder.getInstance().getWorkflowService().getEvent(eventId);
    }

    /**
     * Returns array of requests initiated by a user.
     *
     * @param user  User to retrieve requests of
     * @param beginDate  Lower limit of date range
     * @param endDate  Upper limit of date range
     * @param dateCategory  Filter by created date or last updated date
     * @param status  Status of requests to filter
     * @return
     * @throws WorkflowException
     */
    public WorkflowRequest[] getRequestsCreatedByUser(String user, String beginDate, String endDate, String
            dateCategory, String status) throws WorkflowException {


        int tenant = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return WorkflowServiceDataHolder.getInstance().getWorkflowService()
                .getRequestsFromFilter(user, beginDate, endDate, dateCategory, tenant, status);
    }

    /**
     * Return array of requests according to createdAt and updatedAt filter
     *
     * @param beginDate  Lower limit of date range
     * @param endDate  Upper limit of date range
     * @param dateCategory  Filter by created date or last updated date
     * @param status  Status of requests to filter
     * @return
     * @throws WorkflowException
     */
    public WorkflowRequest[] getRequestsInFilter(String beginDate, String endDate, String
            dateCategory, String status) throws WorkflowException {

        int tenant = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return WorkflowServiceDataHolder.getInstance().getWorkflowService()
                .getRequestsFromFilter("", beginDate, endDate, dateCategory, tenant, status);
    }

    /**
     * Move workflow requests created by the logged in user to DELETED state.
     *
     * @param requestId Request ID to delete requests of.
     * @throws WorkflowException
     */
    public void deleteWorkflowRequest(String requestId) throws WorkflowException {

        WorkflowServiceDataHolder.getInstance().getWorkflowService()
                .deleteWorkflowRequest(requestId);
    }

    /**
     * Move workflow requests created by any user to DELETED state.
     *
     * @param requestId Request ID to delete requests of.
     * @throws WorkflowException
     */
    public void deleteWorkflowRequestCreatedByAnyUser(String requestId) throws WorkflowException {

        WorkflowServiceDataHolder.getInstance().getWorkflowService()
                .deleteWorkflowRequestCreatedByAnyUser(requestId);
    }

    /**
     * Get workflows of a request.
     *
     * @param requestId Request ID to get workflows of.
     * @return
     * @throws WorkflowException
     */
    public WorkflowRequestAssociation[] getWorkflowsOfRequest(String requestId) throws WorkflowException {

        return WorkflowServiceDataHolder.getInstance().getWorkflowService().getWorkflowsOfRequest(requestId);
    }


}
