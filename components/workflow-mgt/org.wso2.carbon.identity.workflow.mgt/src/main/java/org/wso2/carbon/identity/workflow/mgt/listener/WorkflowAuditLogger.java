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

package org.wso2.carbon.identity.workflow.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.List;

public class WorkflowAuditLogger extends AbstractWorkflowListener {

    private static final Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;
    private static final String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Data : { %s } | Result :  %s ";
    private static final String AUDIT_SUCCESS = "Success";

    /**
     * Trigger after deleting the request
     *
     * @param workflowRequest
     * @throws WorkflowException
     */
    @Override
    public void doPostDeleteWorkflowRequest(WorkflowRequest workflowRequest) throws WorkflowException {
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isBlank(loggedInUser)) {
            loggedInUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        loggedInUser = getInitiatorForLog(loggedInUser, tenantDomain);

        String auditData = "\"" + "Request ID" + "\" : \"" + workflowRequest.getRequestId() + "\"";
        AUDIT_LOG.info(String.format(AUDIT_MESSAGE, loggedInUser, "Remove workflow request", auditData,
                AUDIT_SUCCESS));
    }

    /**
     * Trigger after delete the workflow
     *
     * @param workflow
     * @throws WorkflowException
     */
    @Override
    public void doPostDeleteWorkflow(Workflow workflow) throws WorkflowException {
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isBlank(loggedInUser)) {
            loggedInUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        loggedInUser = getInitiatorForLog(loggedInUser, tenantDomain);

        String auditData = "\"" + "Workflow ID" + "\" : \"" + workflow.getWorkflowId() + "\"";
        AUDIT_LOG.info(String.format(AUDIT_MESSAGE, loggedInUser, "Remove workflow", auditData, AUDIT_SUCCESS));
    }

    /**
     * Trigger after deleting workflows by tenant id.
     *
     * @param tenantId The id of the tenant.
     * @throws WorkflowException
     */
    @Override
    public void doPostDeleteWorkflows(int tenantId) throws WorkflowException {

        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isBlank(loggedInUser)) {
            loggedInUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        loggedInUser = getInitiatorForLog(loggedInUser, tenantDomain);

        String auditData = "\"" + "Tenant ID" + "\" : \"" + tenantId + "\"";
        AUDIT_LOG.info(String.format(AUDIT_MESSAGE, loggedInUser, "Remove all workflows of a tenant", auditData,
                AUDIT_SUCCESS));
    }

    /**
     * Trigger after adding a workflow
     *
     * @param workflowDTO
     * @param parameterList
     * @param tenantId
     * @throws WorkflowException
     */
    @Override
    public void doPostAddWorkflow(Workflow workflowDTO, List<Parameter> parameterList, int tenantId) throws
            WorkflowException {
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isBlank(loggedInUser)) {
            loggedInUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        loggedInUser = getInitiatorForLog(loggedInUser, tenantDomain);

        String auditData = "\"" + "Workflow Name" + "\" : \"" + workflowDTO.getWorkflowName() + "\",\""
                + "Workflow  Impl ID" + "\" : \"" + workflowDTO.getWorkflowImplId() + "\",\""
                + "Workflow ID" + "\" : \"" + workflowDTO.getWorkflowId() + "\",\""
                + "Workflow Description" + "\" : \"" + workflowDTO.getWorkflowDescription() + "\",\""
                + "Template ID" + "\" : \"" + workflowDTO.getTemplateId() + "\"";
        AUDIT_LOG.info(String.format(AUDIT_MESSAGE, loggedInUser, "Add Workflow", auditData, AUDIT_SUCCESS));
    }

    /**
     * Trigger after adding a association
     *
     * @param associationName
     * @param workflowId
     * @param eventId
     * @param condition
     * @throws WorkflowException
     */
    @Override
    public void doPostAddAssociation(String associationName, String workflowId, String eventId, String condition)
            throws WorkflowException {
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isBlank(loggedInUser)) {
            loggedInUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        loggedInUser = getInitiatorForLog(loggedInUser, tenantDomain);

        String auditData = "\"" + "Association Name" + "\" : \"" + associationName + "\",\""
                + "Workflow ID" + "\" : \"" + workflowId + "\",\""
                + "Event ID" + "\" : \"" + eventId + "\",\""
                + "Condition" + "\" : \"" + condition + "\"";
        AUDIT_LOG.info(String.format(AUDIT_MESSAGE, loggedInUser, "Add Association", auditData, AUDIT_SUCCESS));
    }

    /**
     * Trigger after removing an association.
     *
     * @param associationId
     * @throws WorkflowException
     */
    @Override
    public void doPostRemoveAssociation(int associationId) throws WorkflowException {
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isBlank(loggedInUser)) {
            loggedInUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        loggedInUser = getInitiatorForLog(loggedInUser, tenantDomain);

        String auditData = "\"" + "Association ID" + "\" : \"" + associationId + "\"";
        AUDIT_LOG.info(String.format(AUDIT_MESSAGE, loggedInUser, "Remove Association", auditData, AUDIT_SUCCESS));
    }

    /**
     * Trigger after changing state of an association
     *
     * @param associationId
     * @param isEnable
     * @throws WorkflowException
     */
    @Override
    public void doPostChangeAssociationState(String associationId, boolean isEnable) throws WorkflowException {
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isBlank(loggedInUser)) {
            loggedInUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        loggedInUser = getInitiatorForLog(loggedInUser, tenantDomain);

        String auditData = "\"" + "Association ID" + "\" : \"" + associationId + "\",\""
                + "Resulting State" + "\" : \"" + isEnable + "\"";
        AUDIT_LOG.info(String.format(AUDIT_MESSAGE, loggedInUser, "Change Association State", auditData,
                AUDIT_SUCCESS));
    }

    /**
     * Get the initiator for audit logs.
     *
     * @param username      Username of the initiator.
     * @param tenantDomain  Tenant domain of the initiator.
     *
     * @return initiator for the log.
     */
    private String getInitiatorForLog(String username, String tenantDomain) {

        if (!LoggerUtils.isLogMaskingEnable) {
            // Append tenant domain to username.
            return UserCoreUtil.addTenantDomainToEntry(username, tenantDomain);
        }
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(tenantDomain)) {
            String initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
            if (StringUtils.isNotBlank(initiator)) {
                return initiator;
            }
        }
        return LoggerUtils.getMaskedContent(username);
    }
}
