package org.wso2.carbon.identity.workflow.mgt;

import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowAssociation;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowRequestAssociationDAO;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowRequestDAO;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

import java.util.List;

public class WorkflowExecutorManagerServiceImpl implements WorkflowExecutorManagerService {

    public List<WorkflowAssociation> getWorkflowAssociationsForRequest(String eventId, int tenantId)
            throws InternalWorkflowException {

        WorkflowRequestAssociationDAO workflowRequestAssociationDAO = new WorkflowRequestAssociationDAO();
        return workflowRequestAssociationDAO.getWorkflowAssociationsForRequest(eventId, tenantId);
    }

    @Override
    public List<String> getWorkflowStatesOfRequest(String requestId) throws InternalWorkflowException {

        WorkflowRequestAssociationDAO workflowRequestAssociationDAO = new WorkflowRequestAssociationDAO();
        return workflowRequestAssociationDAO.getWorkflowStatesOfRequest(requestId);
    }

    public WorkflowRequest retrieveWorkflow(String uuid) throws InternalWorkflowException {

        WorkflowRequestDAO requestDAO = new WorkflowRequestDAO();
        return requestDAO.retrieveWorkflow(uuid);
    }

    public String getRequestIdOfRelationship(String relationshipId) throws InternalWorkflowException{

        WorkflowRequestAssociationDAO workflowRequestAssociationDAO = new WorkflowRequestAssociationDAO();
        return workflowRequestAssociationDAO.getRequestIdOfRelationship(relationshipId);
    }

}
