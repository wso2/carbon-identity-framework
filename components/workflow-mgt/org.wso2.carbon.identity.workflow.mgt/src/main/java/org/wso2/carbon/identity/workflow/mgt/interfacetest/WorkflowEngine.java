package org.wso2.carbon.identity.workflow.mgt.interfacetest;

import org.wso2.carbon.identity.workflow.mgt.bean.Entity;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.callback.WSWorkflowCallBackService;
import org.wso2.carbon.identity.workflow.mgt.dao.RequestEntityRelationshipDAO;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowDAO;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

import java.util.List;

public interface WorkflowEngine {

    String addWorkflow(Workflow workflow, int tenantId);
    List<Workflow> getDefinitions(int limit, int offSet , String searchQuery, List<Workflow> workflowList);
    void deleteDefinition(String wfId, int tenantId);
    Workflow getWorkflow(String wfId, int tenantId);
    void updateDefinition(Workflow newWorkflowDefinition,String wfId, int tenantId);

    List<WorkflowRequest> listActiveRequests(int limit, int offSet, String searchQuery,int tenantId);
    WorkflowRequest retrieveActiveRequest(String requestid, int tenantId);
    void deleteWorkflowRequest(String requestId, int tenantId);

    void engageWorkflow(WorkflowRequest workflowRequest,String wfId);
    void registerCallbackHandler(WSWorkflowCallBackService callbackHandler, int tenantId);
    String handleApprovalByUser(UserApproval approval,int tenantId);
    List<UserApproval>  getPendingApprovalRequests(String userId,int tenantId);



    WorkflowDAO getWorkflow(String workflowId) throws WorkflowException;

    void removeWorkflow(String workflowId) throws WorkflowException;

    List<Workflow> listWorkflows(int tenantId) throws WorkflowException;

    boolean entityHasPendingWorkflows(Entity entity) throws WorkflowException;

    void updateWorkflow(Workflow workflow);
}

