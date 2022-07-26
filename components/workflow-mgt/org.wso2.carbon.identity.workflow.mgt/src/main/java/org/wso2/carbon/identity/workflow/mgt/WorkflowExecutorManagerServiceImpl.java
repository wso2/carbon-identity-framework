/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowAssociation;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowRequestAssociationDAO;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowRequestDAO;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;

import java.util.List;

/**
 * Workflow Executor Manager service implementation.
 */
public class WorkflowExecutorManagerServiceImpl implements WorkflowExecutorManagerService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WorkflowAssociation> getWorkflowAssociationsForRequest(String eventId, int tenantId)
            throws InternalWorkflowException {

        WorkflowRequestAssociationDAO workflowRequestAssociationDAO = new WorkflowRequestAssociationDAO();
        return workflowRequestAssociationDAO.getWorkflowAssociationsForRequest(eventId, tenantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getWorkflowStatesOfRequest(String requestId) throws InternalWorkflowException {

        WorkflowRequestAssociationDAO workflowRequestAssociationDAO = new WorkflowRequestAssociationDAO();
        return workflowRequestAssociationDAO.getWorkflowStatesOfRequest(requestId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkflowRequest retrieveWorkflow(String uuid) throws InternalWorkflowException {

        WorkflowRequestDAO requestDAO = new WorkflowRequestDAO();
        return requestDAO.retrieveWorkflow(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestIdOfRelationship(String relationshipId) throws InternalWorkflowException {

        WorkflowRequestAssociationDAO workflowRequestAssociationDAO = new WorkflowRequestAssociationDAO();
        return workflowRequestAssociationDAO.getRequestIdOfRelationship(relationshipId);
    }
}
