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
import org.wso2.carbon.identity.workflow.mgt.WorkflowExecutorResult;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.Map;

public class WorkflowExecutorAuditLogger extends AbstractWorkflowExecutorManagerListener{

    private static final Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;
    private static final String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Data : { %s } | Result " +
            ":  %s ";
    private static final String AUDIT_SUCCESS = "Success";

    /**
     * Trigger after executing a workflow request
     *
     * @param workFlowRequest
     * @throws WorkflowException
     */
    @Override
    public void doPostExecuteWorkflow(WorkflowRequest workFlowRequest, WorkflowExecutorResult result) throws
            WorkflowException {
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isBlank(loggedInUser)) {
            loggedInUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        loggedInUser = getInitiatorForLog(loggedInUser, tenantDomain);

        String auditData = "\"" + "Operation Type" + "\" : \"" + workFlowRequest.getEventType()
                + "\",\"" + "Request parameters" + "\" : \"" + workFlowRequest.getRequestParameterAsString()
                + "\"";
        AUDIT_LOG.info(String.format(AUDIT_MESSAGE, loggedInUser, "Initiate Workflow", auditData,
                AUDIT_SUCCESS));
    }

    /**
     * Trigger after handling a callback
     *
     * @param uuid
     * @param status
     * @param additionalParams
     * @throws WorkflowException
     */
    @Override
    public void doPostHandleCallback(String uuid, String status, Map<String, Object> additionalParams) throws WorkflowException {
        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isBlank(loggedInUser)) {
            loggedInUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        loggedInUser = getInitiatorForLog(loggedInUser, tenantDomain);

        String auditData = "\"" + "Request ID" + "\" : \"" + uuid
                + "\",\"" + "Callback Status" + "\" : \"" + status
                + "\"";
        AUDIT_LOG.info(String.format(AUDIT_MESSAGE, loggedInUser, "Callback for Workflow Request", auditData,
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
