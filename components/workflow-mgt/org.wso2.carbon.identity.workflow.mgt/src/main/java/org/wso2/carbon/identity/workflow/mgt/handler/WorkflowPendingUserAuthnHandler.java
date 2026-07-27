/*
 * Copyright (c) 2021-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.workflow.mgt.handler;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.workflow.mgt.bean.Entity;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowErrorConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;

/**
 * The WorkflowPendingUserAuthnHandler is to handle authentication when the ADD USER workflow is engaged.
 */
public class WorkflowPendingUserAuthnHandler extends AbstractEventHandler {

    private static final String WORKFLOW_PENDING_USER_AUTHN_HANDLER_ENABLE = "WorkflowPendingUserAuthnHandler.enable";

    private static final Log log = LogFactory.getLog(WorkflowPendingUserAuthnHandler.class);

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        if (!isWorkflowPendingUserAuthnHandlerEnabled()) {
            return;
        }

        Map<String, Object> eventProperties = event.getEventProperties();

        if (log.isDebugEnabled()) {
            log.debug("Handling the event : " + event.getEventName());
        }

        String userName = (String) eventProperties.get(IdentityEventConstants.EventProperty.USER_NAME);
        if (StringUtils.isEmpty(userName)) {
            userName = (String) eventProperties.get(IdentityEventConstants.EventProperty.CLAIM_VALUE);
        }
        int tenantId = (Integer) eventProperties.get(IdentityEventConstants.EventProperty.TENANT_ID);
        validatePendingApproval(userName, tenantId);
    }

    /**
     * Get the name of the handler.
     *
     * @return Name of the handler.
     */
    @Override
    public String getName() {

        return "WorkflowPendingUserAuthnHandler";
    }

    public String getFriendlyName() {

        return "Workflow Pending User Authentication Handler";
    }

    /**
     * Initialize the handler.
     *
     * @param configuration Init configuration.
     * @throws IdentityRuntimeException If an error occurred while initializing the handler.
     */
    @Override
    public void init(InitConfig configuration) throws IdentityRuntimeException {

        super.init(configuration);
    }

    /**
     * Get the priority of the handler.
     *
     * @param messageContext Message context.
     * @return Priority of the handler.
     */
    @Override
    public int getPriority(MessageContext messageContext) {

        return 113;
    }

    /**
     * Validate whether the user account approval is pending.
     *
     * @param username Username.
     * @throws IdentityEventException If an error occurred while validating pending approval.
     */
    private void validatePendingApproval(String username, int tenantId) throws IdentityEventException {

        boolean isPendingApproval;
        try {
            Entity entity = new Entity(MultitenantUtils.getTenantAwareUsername(username),
                    WFConstant.WORKFLOW_ENTITY_TYPE, tenantId);
            isPendingApproval = WorkflowServiceDataHolder.getInstance().getWorkflowService()
                    .entityHasPendingWorkflowsOfType(entity, WFConstant.WORKFLOW_REQUEST_TYPE);
        } catch (WorkflowException e) {
            throw new IdentityEventException("Error occurred while checking the pending approvals for " +
                    "the account of the user: " + username, e);
        } catch (IdentityRuntimeException e) {
            throw new IdentityEventException("Can't find the tenant domain for the user: " + username, e);
        }

        if (isPendingApproval) {
            IdentityErrorMsgContext customErrorMessageContext =
                    new IdentityErrorMsgContext(IdentityCoreConstants.USER_ACCOUNT_PENDING_APPROVAL_ERROR_CODE);
            IdentityUtil.setIdentityErrorMsg(customErrorMessageContext);
            throw new IdentityEventException(
                    WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_ACCOUNT_PENDING_APPROVAL.getCode(),
                    WorkflowErrorConstants.ErrorMessages.ERROR_CODE_USER_ACCOUNT_PENDING_APPROVAL.getMessage());
        }
    }

    private boolean isWorkflowPendingUserAuthnHandlerEnabled() {

        if (this.configs.getModuleProperties() != null) {
            String handlerEnabled = this.configs.getModuleProperties()
                    .getProperty(WORKFLOW_PENDING_USER_AUTHN_HANDLER_ENABLE);
            return Boolean.parseBoolean(handlerEnabled);
        }
        return false;
    }
}
