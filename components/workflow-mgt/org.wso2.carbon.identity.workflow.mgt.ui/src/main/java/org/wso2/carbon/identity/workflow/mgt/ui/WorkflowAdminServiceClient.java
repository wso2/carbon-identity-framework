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

package org.wso2.carbon.identity.workflow.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.Association;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.Template;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowWizard;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowEvent;
import org.wso2.carbon.identity.workflow.mgt.stub.metadata.WorkflowImpl;
import org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.stub.bean.WorkflowRequestAssociation;
import org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceStub;
import org.wso2.carbon.identity.workflow.mgt.stub.WorkflowAdminServiceWorkflowException;

import java.rmi.RemoteException;

public class WorkflowAdminServiceClient {

    private WorkflowAdminServiceStub stub;
    private static final Log log = LogFactory.getLog(WorkflowAdminServiceClient.class);

    /**
     * @param cookie
     * @param backendServerURL
     * @param configCtx
     * @throws AxisFault
     */
    public WorkflowAdminServiceClient(String cookie, String backendServerURL,
                                      ConfigurationContext configCtx) throws AxisFault {

        String serviceURL = backendServerURL + "WorkflowAdminService";
        stub = new WorkflowAdminServiceStub(configCtx, serviceURL);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * List events that can be associated to workflows, eg:- Add user, Addrole, etc
     *
     * @return
     * @throws RemoteException
     */
    public WorkflowEvent[] listWorkflowEvents() throws RemoteException {

        WorkflowEvent[] workflowEvents = stub.listWorkflowEvents();
        if (workflowEvents == null) {
            workflowEvents = new WorkflowEvent[0];
        }
        return workflowEvents;
    }

    /**
     * List existing workflow templates
     *
     * @return
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public Template[] listTemplates() throws RemoteException, WorkflowAdminServiceWorkflowException {

        Template[] templates = stub.listTemplates();
        if (templates == null) {
            templates = new Template[0];
        }
        return templates;
    }

    /**
     * List implementations of a workflow template
     *
     * @param templateId  ID of template
     * @return
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public WorkflowImpl[] listWorkflowImpls(String templateId)
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowImpl[] workflows = stub.listWorkflowImpls(templateId);
        if (workflows == null) {
            workflows = new WorkflowImpl[0];
        }
        return workflows;
    }

    /**
     * Retrieve workflow template using workflow ID
     *
     * @param templateName  template name
     * @return
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public Template getTemplate(String templateName) throws RemoteException, WorkflowAdminServiceWorkflowException {

        Template template = stub.getTemplate(templateName);
        return template;
    }

    /**
     * Retrieve workflow impl object
     *
     * @param template template id
     * @param implName workflow impl name
     * @return
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public WorkflowImpl getWorkflowImp(String template, String implName)
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowImpl workflowImpl = stub.getWorkflowImpl(template, implName);
        return workflowImpl;
    }

    /**
     * Add new workflow
     *
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public void addWorkflow(WorkflowWizard workflowWizard)
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        stub.addWorkflow(workflowWizard);

    }

    /**
     * Retrieve workflow from workflow ID
     *
     * @param workflowId  workflow id
     * @return
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public WorkflowWizard getWorkflow(String workflowId)
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        return stub.getWorkflow(workflowId);
    }

    /**
     * List paginated workflows of a tenant.
     *
     * @return WorkflowEvent objects array
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public WorkflowWizard[] listPaginatedWorkflows(int limit, int offset, String filter) throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowWizard[] workflows = stub.listPaginatedWorkflows(limit, offset, filter);
        if (workflows == null) {
            workflows = new WorkflowWizard[0];
        }
        return workflows;
    }

    /**
     * List workflows with a filter
     *
     * @return WorkflowEvent objects array
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public WorkflowWizard[] listWorkflows() throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowWizard[] workflows = stub.listWorkflows();
        if (workflows == null) {
            workflows = new WorkflowWizard[0];
        }
        return workflows;
    }

    /**
     * Get workflows count.
     *
     * @return Count of workflows
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public int getWorkflowsCount(String filter) throws RemoteException, WorkflowAdminServiceWorkflowException {

        return stub.getWorkflowsCount(filter);
    }

    /**
     * Remove a workflow
     *
     * @param workflowId  ID of workflow to remove
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public void deleteWorkflow(String workflowId) throws RemoteException, WorkflowAdminServiceWorkflowException {

        stub.removeWorkflow(workflowId);
    }

    /**
     * List associations of a specific workflow
     *
     * @param workflowId  Workflow ID
     * @return
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public Association[] listAssociationsForWorkflow(String workflowId)
            throws RemoteException, WorkflowAdminServiceWorkflowException {
        Association[] associationsForWorkflow = stub.listAssociations(workflowId);
        if (associationsForWorkflow == null) {
            associationsForWorkflow = new Association[0];
        }
        return associationsForWorkflow;
    }

    /**
     * List paginated associations of a tenant.
     *
     * @return Association objects array
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public Association[] listPaginatedAssociations(int limit, int offset, String filter) throws RemoteException, WorkflowAdminServiceWorkflowException {

        Association[] associations = stub.listPaginatedAssociations(limit, offset,  filter);
        if (associations == null) {
            associations = new Association[0];
        }
        return associations;
    }

    /**
     * List all associations
     *
     * @return Association objects array
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public Association[] listAllAssociations() throws RemoteException, WorkflowAdminServiceWorkflowException {

        Association[] associations = stub.listAllAssociations();
        if (associations == null) {
            associations = new Association[0];
        }
        return associations;
    }

    /**
     * Get associations count.
     *
     * @return Count of associations
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public int getAssociationsCount(String filter) throws RemoteException, WorkflowAdminServiceWorkflowException {

        return stub.getAssociationsCount(filter);
    }

    /**
     * Remove association
     *
     * @param associationId  ID of association to remove
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public void deleteAssociation(String associationId) throws RemoteException, WorkflowAdminServiceWorkflowException {

        stub.removeAssociation(associationId);
    }

    /**
     * Add new workflow association
     *
     * @param associationName  Name for the association
     * @param workflowId  Workflow to associate
     * @param eventId  Event to associate
     * @param condition  Condition to check the event for associating
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public void addAssociation(String workflowId, String associationName, String eventId, String condition)
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        stub.addAssociation(associationName, workflowId, eventId, condition);
    }

    /**
     * Enable association to allow to execute
     *
     * @param associationId
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public void enableAssociation(String associationId)
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        stub.changeAssociationState(associationId, true);
    }

    /**
     * Disable association to avoid with execution of the workflows
     *
     * @param associationId
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public void disableAssociation(String associationId)
            throws RemoteException, WorkflowAdminServiceWorkflowException {

        stub.changeAssociationState(associationId,false);
    }

    /**
     * Retrieve workflow event object from ID
     *
     * @param id  Workflow event ID
     * @return
     * @throws RemoteException
     */
    public WorkflowEvent getEvent(String id) throws RemoteException {

        return stub.getEvent(id);
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
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public WorkflowRequest[] getRequestsCreatedByUser(String user, String beginDate, String endDate, String
            dateCategory, String status) throws RemoteException, WorkflowAdminServiceWorkflowException {

        WorkflowRequest[] request = stub.getRequestsCreatedByUser(user, beginDate, endDate, dateCategory, status);
        if (request == null) {
            request = new WorkflowRequest[0];
        }
        return request;
    }

    /**
     * Return array of requests according to createdAt and updatedAt filter
     *
     * @param beginDate  Lower limit of date range
     * @param endDate  Upper limit of date range
     * @param dateCategory  Filter by created date or last updated date
     * @param status  Status of requests to filter
     * @return
     * @throws RemoteException
     * @throws WorkflowAdminServiceWorkflowException
     */
    public WorkflowRequest[] getAllRequests(String beginDate, String endDate, String dateCategory, String status) throws
            RemoteException, WorkflowAdminServiceWorkflowException {

        //TODO ADD status as param
        WorkflowRequest[] requests = stub.getRequestsInFilter(beginDate, endDate, dateCategory,status);
        if (requests == null) {
            requests = new WorkflowRequest[0];
        }
        return requests;
    }

    /**
     * Move Workflow request to DELETED state.
     *
     * @param requestId Request ID to delete requests of.
     * @throws WorkflowAdminServiceWorkflowException
     * @throws RemoteException
     */
    public void deleteRequest(String requestId) throws WorkflowAdminServiceWorkflowException, RemoteException {
        stub.deleteWorkflowRequest(requestId);
    }

    /**
     * Move workflow request created by any user to DELETED state.
     *
     * @param requestId Request ID to delete requests of.
     * @throws WorkflowAdminServiceWorkflowException
     * @throws RemoteException
     */
    public void deleteRequestCreatedByAnyUser(String requestId) throws WorkflowAdminServiceWorkflowException,
            RemoteException {

        stub.deleteWorkflowRequestCreatedByAnyUser(requestId);
    }

    /**
     * Get workflows of a request.
     *
     * @param requestId Request ID to get workflows of.
     * @return
     * @throws WorkflowAdminServiceWorkflowException
     * @throws RemoteException
     */
    public WorkflowRequestAssociation[] getWorkflowsOfRequest(String requestId) throws
            WorkflowAdminServiceWorkflowException, RemoteException {

        return stub.getWorkflowsOfRequest(requestId);
    }
}
