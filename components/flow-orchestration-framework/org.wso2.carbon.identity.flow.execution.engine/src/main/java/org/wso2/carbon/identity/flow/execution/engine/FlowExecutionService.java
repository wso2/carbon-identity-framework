/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.flow.execution.engine.core.FlowExecutionEngine;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.listener.FlowExecutionListener;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.execution.engine.util.AuthenticationAssertionUtils;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.OTFI;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes.REGISTRATION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.REDIRECTION;

/**
 * Service class to handle the user flow.
 */
public class FlowExecutionService {

    private static final Log LOG = LogFactory.getLog(FlowExecutionService.class);
    private static final FlowExecutionService instance = new FlowExecutionService();

    private FlowExecutionService() {

    }

    public static FlowExecutionService getInstance() {

        return instance;
    }

    /**
     * Execute the flow for the given tenant domain.
     *
     * @param tenantDomain  Tenant domain.
     * @param applicationId Application ID.
     * @param flowId        Flow ID.
     * @param actionId      Action ID.
     * @param inputs        User inputs.
     * @return ExecutionState.
     * @throws FlowEngineException If something goes wrong while executing the flow.
     */
    public FlowExecutionStep executeFlow(String tenantDomain, String applicationId, String flowId, String actionId,
                                         String flowType, Map<String, String> inputs) throws FlowEngineException {

        String appResidentOrgId = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getApplicationResidentOrganizationId();
        if (StringUtils.isNotBlank(appResidentOrgId)) {
            try {
                tenantDomain = FrameworkUtils.resolveTenantDomainFromOrganizationId(appResidentOrgId);
            } catch (FrameworkException e) {
                throw FlowExecutionEngineUtils.handleServerException(Constants.ErrorMessages
                        .ERROR_CODE_TENANT_RESOLVE_FROM_ORGANIZATION_FAILURE, appResidentOrgId);
            }
        }
        FlowExecutionStep step;
        FlowExecutionContext context = null;
        boolean isFlowEnteredInIdentityContext = false;
        GraphConfig graphConfig = null;
        try {
            if (StringUtils.isBlank(flowId)) {
                // No flowId present hence initiate the flow.
                context = FlowExecutionEngineUtils.initiateContext(tenantDomain, applicationId, flowType);
            } else {
                context = FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId);
                String originalFlowId = context.getContextIdentifier();

                // If the incoming flow id is different from the original flow id, it means this is a one time flow id.
                if (!originalFlowId.equals(flowId)) {
                    FlowExecutionEngineUtils.removeFlowContextFromCache(flowId);
                    // Retrieve the original context and set it to the current context.
                    flowId = originalFlowId;
                    // GraphConfig will be removed from cache when adding to cache.
                    graphConfig = context.getGraphConfig();
                    // Cache the context against the original flowId.
                    FlowExecutionEngineUtils.addFlowContextToCache(context);
                    context.setGraphConfig(graphConfig);
                }
            }

            isFlowEnteredInIdentityContext = enterFlowInIdentityContext(context.getFlowType());

            if (inputs != null) {
                context.getUserInputData().putAll(inputs);
            }
            
            context.setCurrentActionId(actionId);
            for (FlowExecutionListener listener :
                    FlowExecutionEngineDataHolder.getInstance().getFlowListeners()) {
                if (listener.isEnabled() && !listener.doPreExecute(context)) {
                    return null;
                }
            }
            step = FlowExecutionEngine.getInstance().execute(context);
            for (FlowExecutionListener listener :
                    FlowExecutionEngineDataHolder.getInstance().getFlowListeners()) {
                if (listener.isEnabled() && !listener.doPostExecute(step, context)) {
                    return null;
                }
            }
            if (STATUS_COMPLETE.equals(step.getFlowStatus())) {
                FlowExecutionEngineUtils.removeFlowContextFromCache(flowId);
                if (step.getData() == null) {
                    step.setData(new DataDTO.Builder().additionalData(new HashMap<>()).build());
                }
                // Generate authentication assertion only if the last step is a redirection. This prevents
                // unnecessary generation of assertions when the last step is a VIEW step.
                if (context.isGenerateAuthenticationAssertion() && REDIRECTION.equals(step.getStepType())) {
                    step.getData().addAdditionalData(FrameworkConstants.USER_ASSERTION,
                            AuthenticationAssertionUtils.getSignedUserAssertion(context));
                }
            } else {
                cacheContext(context, step);
            }
            step.setFlowType(context.getFlowType());
            return step;
        } catch (FlowEngineException e) {

            if (context != null && REGISTRATION.getType().equals(context.getFlowType())) {
                Map<String, String> userClaims =
                        context.getFlowUser() != null ? context.getFlowUser().getClaims() : null;
                FrameworkUtils.publishEventOnUserRegistrationFailure(e.getErrorCode(), e.getDescription(),
                        userClaims, tenantDomain);
            }

            FlowExecutionEngineUtils.rollbackContext(flowType, flowId);
            FlowExecutionEngineUtils.removeFlowContextFromCache(flowId);
            throw e;
        } finally {
            if (isFlowEnteredInIdentityContext) {
                IdentityContext.getThreadLocalIdentityContext().exitFlow();
            }
        }
    }

    private boolean enterFlowInIdentityContext(String flowType) {

        if (!EnumUtils.isValidEnum(FlowTypes.class, flowType)) {
            LOG.warn("Invalid flow type: " + flowType + " provided. Hence not entering the flow in IdentityContext.");
            return false;
        }

        switch (FlowTypes.valueOf(flowType)) {
            case REGISTRATION:
                IdentityContext.getThreadLocalIdentityContext().enterFlow(new Flow.Builder()
                        .name(Flow.Name.REGISTER)
                        .initiatingPersona(Flow.InitiatingPersona.USER)
                        .build());
                return true;
            case PASSWORD_RECOVERY:
                IdentityContext.getThreadLocalIdentityContext().enterFlow(new Flow.CredentialFlowBuilder()
                        .name(Flow.Name.CREDENTIAL_RESET)
                        .credentialType(Flow.CredentialType.PASSWORD)
                        .initiatingPersona(Flow.InitiatingPersona.USER)
                        .build());
                return true;
            case INVITED_USER_REGISTRATION:
                IdentityContext.getThreadLocalIdentityContext().enterFlow(new Flow.Builder()
                        .name(Flow.Name.INVITE)
                        .initiatingPersona(Flow.InitiatingPersona.ADMIN)
                        .build());
                return true;
            default:
                return false;
        }
    }

    /**
     * Cache the flow execution context.
     *
     * @param context Flow execution context.
     * @param step    Current execution step.
     * @throws FlowEngineException If something goes wrong while caching the context.
     */
    private void cacheContext(FlowExecutionContext context, FlowExecutionStep step)
            throws FlowEngineException {

        if (context == null || context.getProperties() == null) {
            return;
        }
        String otfiToken = (String) context.getProperty(OTFI);
        if (StringUtils.isBlank(otfiToken)) {
            FlowExecutionEngineUtils.addFlowContextToCache(context);
        } else {
            context.getProperties().remove(OTFI);
            FlowExecutionEngineUtils.addFlowContextToCache(otfiToken, context);
        }
    }
}
