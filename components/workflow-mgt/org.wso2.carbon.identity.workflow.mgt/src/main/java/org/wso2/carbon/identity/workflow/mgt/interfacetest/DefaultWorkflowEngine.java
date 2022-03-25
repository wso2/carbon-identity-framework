package org.wso2.carbon.identity.workflow.mgt.interfacetest;

import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequestAssociation;
import org.wso2.carbon.identity.workflow.mgt.callback.WSWorkflowCallBackService;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowEvent;

import java.util.List;

public class DefaultWorkflowEngine implements WorkflowEngine{

    @Override
    public void addWorkflow(Workflow workflow, int tenantId) {

    }

    @Override
    public List<Workflow> getDefinitions(int limit, int offSet, String searchQuery, int tenantId) {

        return null;
    }

    @Override
    public void deleteDefinition(String wfId, int tenantId) {

    }

    @Override
    public Workflow getWorkflow(String wfId, int tenantId) {

        return null;
    }

    @Override
    public void updateDefinition(Workflow newWorkflowDefinition, Workflow wfId, int tenantId) {

    }

    @Override
    public WorkflowRequest[] listActiveRequests(int limit, int offSet, String searchQuery, int tenantId) {

        return null;
    }

    @Override
    public WorkflowRequestAssociation[] retrieveActiveRequest(String requestid, int tenantId) {

        return null;
    }

    @Override
    public void deleteActiveRequest(String requestId, int tenantId) {

    }

    @Override
    public WorkflowEvent engageWorkflow(WorkflowEvent workflowEvent, String wfId) {

        return null;
    }

    @Override
    public void registerCallbackHandler(WSWorkflowCallBackService callbackHandler, int tenantId) {

    }

    @Override
    public String handleApprovalByUser(UserApproval approval, int tenantId) {

        return null;
    }

    @Override
    public boolean getPendingApprovalRequests(String userId, int tenantId) {

        return false;
    }

}
