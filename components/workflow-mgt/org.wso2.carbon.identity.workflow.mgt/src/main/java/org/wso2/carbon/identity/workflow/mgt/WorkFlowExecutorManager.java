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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jcajce.provider.drbg.DRBG;
import org.jaxen.JaxenException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowAssociation;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.MetaData;
import org.wso2.carbon.identity.workflow.mgt.dao.RequestEntityRelationshipDAO;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowDAO;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowRequestAssociationDAO;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowRequestDAO;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.extension.WorkflowRequestHandler;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowExecutorManagerListener;
import org.wso2.carbon.identity.workflow.mgt.util.ExecutorResultState;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowRequestBuilder;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowRequestStatus;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorkFlowExecutorManager {

    private static WorkFlowExecutorManager instance = new WorkFlowExecutorManager();

    private static final Log log = LogFactory.getLog(WorkFlowExecutorManager.class);

    private WorkFlowExecutorManager() {

    }

    public static WorkFlowExecutorManager getInstance() {

        return instance;
    }

    /**
     * Called when initiate a request that can be engaged with a workflow. Here it determine if operation has engaged
     * with a workflow or not. If workflows engaged this will deploy communicate with relevant workflow engine and
     * return false which will stop continuation of operation. Otherwise this will return true.
     *
     * @param workFlowRequest Workflow request object with request attributes.
     * @return
     * @throws WorkflowException
     */
    public WorkflowExecutorResult executeWorkflow(WorkflowRequest workFlowRequest) throws WorkflowException {

        WorkflowRequestAssociationDAO workflowRequestAssociationDAO = new WorkflowRequestAssociationDAO();
        List<WorkflowExecutorManagerListener> workflowListenerList =
                WorkflowServiceDataHolder.getInstance().getExecutorListenerList();
        for (WorkflowExecutorManagerListener workflowListener : workflowListenerList) {
            if (workflowListener.isEnable()) {
                workflowListener.doPreExecuteWorkflow(workFlowRequest);
            }
        }

        if (StringUtils.isBlank(workFlowRequest.getUuid())) {
            workFlowRequest.setUuid(UUID.randomUUID().toString());
        }
        OMElement xmlRequest = WorkflowRequestBuilder.buildXMLRequest(workFlowRequest);
        WorkflowRequestAssociationDAO requestAssociationDAO = new WorkflowRequestAssociationDAO();
        WorkflowDAO workflowDAO = new WorkflowDAO();
        List<WorkflowAssociation> associations =
                requestAssociationDAO.getWorkflowAssociationsForRequest(workFlowRequest.getEventType(), workFlowRequest
                        .getTenantId());
        if (CollectionUtils.isEmpty(associations)) {
            return new WorkflowExecutorResult(ExecutorResultState.NO_ASSOCIATION);
        }
        boolean workflowEngaged = false;
        boolean requestSaved = false;
        for (WorkflowAssociation association : associations) {
            try {
                AXIOMXPath axiomxPath = new AXIOMXPath(association.getAssociationCondition());
                if (axiomxPath.booleanValueOf(xmlRequest)) {
                    workflowEngaged = true;
                    if (!requestSaved) {
                        WorkflowRequestDAO requestDAO = new WorkflowRequestDAO();
                        int tenant = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                        String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
                        requestDAO.addWorkflowEntry(workFlowRequest, currentUser, tenant);
                        requestSaved = true;
                    }
                    String relationshipId = UUID.randomUUID().toString();
                    WorkflowRequest requestToSend = workFlowRequest.clone();
                    requestToSend.setUuid(relationshipId);
                    Workflow workflow = workflowDAO.getWorkflow(association.getWorkflowId());
                    AbstractWorkflow templateImplementation = WorkflowServiceDataHolder.getInstance()
                            .getWorkflowImpls().get(workflow.getTemplateId()).get(workflow.getWorkflowImplId());
                    List<Parameter> parameterList = workflowDAO.getWorkflowParams(association.getWorkflowId());
                    templateImplementation.execute(requestToSend, parameterList);
                    workflowRequestAssociationDAO.addNewRelationship(relationshipId, association.getWorkflowId(),
                            workFlowRequest
                                    .getUuid(), WorkflowRequestStatus.PENDING
                                    .toString(), workFlowRequest.getTenantId());
                }
            } catch (JaxenException e) {
                String errorMsg = "Error when executing the xpath expression:" + association.getAssociationCondition()
                        + " , on " + xmlRequest;
                log.error(errorMsg, e);
                return new WorkflowExecutorResult(ExecutorResultState.FAILED, errorMsg);
            } catch (CloneNotSupportedException e) {
                String errorMsg = "Error while cloning workflowRequest object at executor manager.";
                log.error(errorMsg, e);
                return new WorkflowExecutorResult(ExecutorResultState.FAILED, errorMsg);
            }
        }

        if (!workflowEngaged) {
            //handleCallback(workFlowRequest, WorkflowRequestStatus.SKIPPED.toString(), null, "");
            return new WorkflowExecutorResult(ExecutorResultState.CONDITION_FAILED);
        }
        WorkflowExecutorResult finalResult = new WorkflowExecutorResult(ExecutorResultState.STARTED_ASSOCIATION);
        for (WorkflowExecutorManagerListener workflowListener : workflowListenerList) {
            if (workflowListener.isEnable()) {
                workflowListener.doPostExecuteWorkflow(workFlowRequest, finalResult);
            }
        }
        return finalResult;
    }

    private void handleCallback(WorkflowRequest request, String status, Map<String, Object> additionalParams, String
            requestWorkflowId) throws WorkflowException {

        WorkflowRequestAssociationDAO workflowRequestAssociationDAO = new WorkflowRequestAssociationDAO();
        if (request != null) {
            WorkflowRequestDAO workflowRequestDAO = new WorkflowRequestDAO();
            String requestId = request.getUuid();
            workflowRequestAssociationDAO.updateStatusOfRelationship(requestWorkflowId, status);
            workflowRequestDAO.updateLastUpdatedTimeOfRequest(requestId);
            if (StringUtils.isNotBlank(requestWorkflowId) && WorkflowRequestStatus.DELETED.toString().equals
                    (workflowRequestDAO.retrieveStatusOfWorkflow(request.getUuid()))) {
                log.info("Callback received for request " + requestId + " which is already deleted by user. ");
                return;
            }
            if (status.equals(WorkflowRequestStatus.APPROVED.toString()) && !isAllWorkflowsCompleted
                    (workflowRequestAssociationDAO, requestId)) {
                return;
            }
            String eventId = request.getEventType();
            WorkflowRequestHandler requestHandler = WorkflowServiceDataHolder.getInstance().getRequestHandler(eventId);
            if (requestHandler == null) {
                throw new InternalWorkflowException("No request handlers registered for the id: " + eventId);
            }
            if (request.getTenantId() == MultitenantConstants.INVALID_TENANT_ID) {
                throw new InternalWorkflowException(
                        "Invalid tenant id for request " + eventId + " with id" + requestId);
            }
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                    .getThreadLocalCarbonContext();
            try {
                String tenantDomain = WorkflowServiceDataHolder.getInstance().getRealmService().getTenantManager()
                        .getDomain(request.getTenantId());
                carbonContext.setTenantId(request.getTenantId());
                carbonContext.setTenantDomain(tenantDomain);
                requestHandler.onWorkflowCompletion(status, request, additionalParams);
                updateDBAtWorkflowCompletion(requestId, status);
            } catch (WorkflowException e) {
                updateDBAtWorkflowCompletion(requestId, WorkflowRequestStatus.FAILED.toString());
                throw e;
            } catch (UserStoreException e) {
                updateDBAtWorkflowCompletion(requestId, WorkflowRequestStatus.FAILED.toString());
                throw new InternalWorkflowException("Error when getting tenant domain for tenant id " + request
                        .getTenantId());
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private boolean isAllWorkflowsCompleted(WorkflowRequestAssociationDAO workflowRequestAssociationDAO, String
            requestId) throws InternalWorkflowException {
        List<String> statesOfRequest = workflowRequestAssociationDAO.getWorkflowStatesOfRequest(requestId);
        for (int i = 0; i < statesOfRequest.size(); i++) {
            if (!statesOfRequest.get(i).equals(WorkflowRequestStatus.APPROVED.toString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Called when callback received for a pending operation.
     *
     * @param uuid             Unique ID of request
     * @param status           Status of approval/disapproval
     * @param additionalParams Additional parameters required to execute operation.
     * @throws WorkflowException
     */
    public void handleCallback(String uuid, String status, Map<String, Object> additionalParams)
            throws WorkflowException {

        List<WorkflowExecutorManagerListener> workflowListenerList =
                WorkflowServiceDataHolder.getInstance().getExecutorListenerList();
        for (WorkflowExecutorManagerListener workflowListener : workflowListenerList) {
            if (workflowListener.isEnable()) {
                workflowListener.doPreHandleCallback(uuid, status, additionalParams);
            }
        }

        WorkflowRequestAssociationDAO workflowRequestAssociationDAO = new WorkflowRequestAssociationDAO();
        String requestId = workflowRequestAssociationDAO.getRequestIdOfRelationship(uuid);
        WorkflowRequestDAO requestDAO = new WorkflowRequestDAO();
        WorkflowRequest request = requestDAO.retrieveWorkflow(requestId);
        handleCallback(request, status, additionalParams, uuid);

        for (WorkflowExecutorManagerListener workflowListener : workflowListenerList) {
            if (workflowListener.isEnable()) {
                workflowListener.doPostHandleCallback(uuid, status, additionalParams);
            }
        }


    }

    /**
     * Update the state and delete relationships of request at workflow completion.
     *
     * @param requestId
     * @param status
     * @throws InternalWorkflowException
     */
    private void updateDBAtWorkflowCompletion(String requestId, String status) throws InternalWorkflowException {


        RequestEntityRelationshipDAO requestEntityRelationshipDAO = new RequestEntityRelationshipDAO();
        WorkflowRequestDAO workflowRequestDAO = new WorkflowRequestDAO();
        requestEntityRelationshipDAO.deleteRelationshipsOfRequest(requestId);
        workflowRequestDAO.updateStatusOfRequest(requestId, status);
    }
}
