package org.wso2.carbon.identity.workflow.mgt.interfacetest;

import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequestAssociation;
import org.wso2.carbon.identity.workflow.mgt.callback.WSWorkflowCallBackService;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowEvent;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

import java.util.List;

public interface WorkflowEngine {

    void addWorkflow(Workflow workflow, int tenantId) throws WorkflowException;

    List<Workflow> getDefinitions(int limit, int offSet , String searchQuery, int tenantId);

    void deleteDefinition(String wfId, int tenantId);

    Workflow getWorkflow(String wfId, int tenantId);

    void updateDefinition(Workflow newWorkflowDefinition, Workflow wfId, int tenantId);

    WorkflowRequest[] listActiveRequests(int limit, int offSet, String searchQuery, int tenantId);

    WorkflowRequestAssociation[] retrieveActiveRequest(String requestid, int tenantId);

    void deleteActiveRequest(String requestId, int tenantId);

    WorkflowEvent engageWorkflow(WorkflowEvent workflowEvent, String wfId);

    void registerCallbackHandler(WSWorkflowCallBackService callbackHandler, int tenantId);

    String handleApprovalByUser(UserApproval approval, int tenantId);

    boolean getPendingApprovalRequests(String userId, int tenantId);

}

