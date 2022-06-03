package org.wso2.carbon.identity.workflow.mgt;

import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowAssociation;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

import java.util.List;

public interface WorkflowExecutorManagerService {

    /**
     * Get association details related to eventID
     *
     * @param eventId  Event to associate.
     * @param tenantId TenantID.
     * @return
     * @throws InternalWorkflowException
     */
    List<WorkflowAssociation> getWorkflowAssociationsForRequest(String eventId, int tenantId)
            throws InternalWorkflowException;

    List<String> getWorkflowStatesOfRequest(String requestId) throws InternalWorkflowException;

    WorkflowRequest retrieveWorkflow(String uuid) throws InternalWorkflowException;
   String getRequestIdOfRelationship(String relationshipId) throws InternalWorkflowException;
}
