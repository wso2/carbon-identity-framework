/*
 * Copyright (c) 2025-2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
import org.wso2.carbon.identity.flow.execution.engine.model.OTFIRetention;
import org.wso2.carbon.identity.flow.execution.engine.model.PollingState;
import org.wso2.carbon.identity.flow.execution.engine.poll.FlowPollingService;
import org.wso2.carbon.identity.flow.execution.engine.util.AuthenticationAssertionUtils;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowVariableResolver;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.ANONYMOUS_PROFILE_TRACKER;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_INVALID_FLOW_ID;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.OTFI;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.POLL_EXPIRES_IN;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.POLL_INTERVAL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.POLL_INTERVAL_SECONDS;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.POLL_TOKEN;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.POLL_VALIDITY_SECONDS;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes.REGISTRATION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.POLL;
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
        boolean isInitiation = StringUtils.isBlank(flowId);
        boolean isOTFIRetainingFlow = false;
        PollingState previousPollingState = null;
        try {
            if (isInitiation) {
                // No flowId present hence initiate the flow.
                context = FlowExecutionEngineUtils.initiateContext(tenantDomain, applicationId, flowType);
            } else {
                context = FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId);
                String originalFlowId = context.getContextIdentifier();

                // If the incoming flow id is different from the original flow id, it means this is a one time flow id.
                if (!originalFlowId.equals(flowId)) {
                    isOTFIRetainingFlow = shouldRetainOTFI(context);
                    if (!isOTFIRetainingFlow) {
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
            }
            // Executors change the polling state, and the transitions are applied after the node runs.
            previousPollingState = context.getPollingState();
            // The graph is attached from here on, so what the flow defines is known.
            FlowVariableResolver.populateRequestHeaders(context);

            if (inputs != null) {
                context.getUserInputData().putAll(inputs);
            }

            // On the initial request of the registration flow, move the anonymous_profile_tracker value (if
            // provided) from the user inputs into the flow context properties so that it is retained throughout
            // the flow and not treated as a user input during the subsequent input validations.
            if (isInitiation && REGISTRATION.getType().equals(context.getFlowType())
                    && context.getUserInputData().containsKey(ANONYMOUS_PROFILE_TRACKER)) {
                context.setProperty(ANONYMOUS_PROFILE_TRACKER,
                        context.getUserInputData().remove(ANONYMOUS_PROFILE_TRACKER));
            }

            isFlowEnteredInIdentityContext = enterFlowInIdentityContext(context);

            context.setCurrentActionId(actionId);
            FlowVariableResolver.applyRuntimeValues(context, inputs, isInitiation);
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
            FlowExecutionEngineUtils.publishFlowExecutionSuccessEvent(context, step);
            boolean isFlowComplete = STATUS_COMPLETE.equals(step.getFlowStatus());
            if (isFlowComplete) {
                // The waiting client learns that the flow ended.
                endPolling(context);
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
                updatePolling(context, step, previousPollingState);
                resolveRetainedOTFI(context, flowId, isOTFIRetainingFlow);
                cacheContext(context, step);
            }
            if (isOTFIRetainingFlow) {
                // The other client only knows the one time flow id.
                step.setFlowId(flowId);
                if (!shouldRetainOTFI(context)) {
                    // Retention ended, so only the flow id continues the flow.
                    FlowExecutionEngineUtils.removeFlowContextFromCache(flowId);
                }
            } else if (shouldRetainOTFI(context)) {
                // Retention started, so only the one time flow id continues the flow.
                FlowExecutionEngineUtils.removeFlowContextFromCache(context.getContextIdentifier());
            }
            if (!isFlowComplete && previousPollingState == PollingState.IN_PROGRESS
                    && context.getPollingState() == PollingState.COMPLETED) {
                // Only now can the waiting client continue the flow with the id it holds.
                markPollingSuccess(context, flowId);
            }
            step.setFlowType(context.getFlowType());
            return step;
        } catch (FlowEngineException e) {

            FlowExecutionEngineUtils.publishFlowExecutionFailureEvent(context, e.getErrorCode());
            if (context != null && REGISTRATION.getType().equals(context.getFlowType())) {
                Map<String, String> userClaims =
                        context.getFlowUser() != null ? context.getFlowUser().getClaims() : null;
                FrameworkUtils.publishEventOnUserRegistrationFailure(e.getErrorCode(), e.getDescription(),
                        userClaims, tenantDomain);
            }

            FlowExecutionEngineUtils.rollbackContext(flowType, flowId);
            FlowExecutionEngineUtils.removeFlowContextFromCache(flowId);
            String originalFlowId = context != null ? context.getContextIdentifier() : null;
            if (originalFlowId != null && !originalFlowId.equals(flowId)) {
                // A request with a retained one time flow id may have moved the flow back to its flow id. Only one
                // of the two ids is active at a time, so ending both ends the flow once.
                FlowExecutionEngineUtils.rollbackContext(flowType, originalFlowId);
                FlowExecutionEngineUtils.removeFlowContextFromCache(originalFlowId);
            }
            endPolling(context);
            throw e;
        } finally {
            if (isFlowEnteredInIdentityContext) {
                IdentityContext.getThreadLocalIdentityContext().exitFlow();
            }
        }
    }

    private boolean enterFlowInIdentityContext(FlowExecutionContext context) {

        String flowType = context.getFlowType();
        if (!EnumUtils.isValidEnum(FlowTypes.class, flowType)) {
            LOG.warn("Invalid flow type: " + flowType + " provided. Hence not entering the flow in IdentityContext.");
            return false;
        }

        switch (FlowTypes.valueOf(flowType)) {
            case REGISTRATION:
                IdentityContext.getThreadLocalIdentityContext().enterFlow(new Flow.Builder()
                        .name(Flow.Name.REGISTER)
                        .initiatingPersona(Flow.InitiatingPersona.USER)
                        .anonymousProfileTracker((String) context.getProperty(ANONYMOUS_PROFILE_TRACKER))
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
            case PROFILE_UPDATE:
                IdentityContext.getThreadLocalIdentityContext().enterFlow(new Flow.Builder()
                        .name(Flow.Name.PROFILE_UPDATE)
                        .initiatingPersona(Flow.InitiatingPersona.USER)
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
        String otfiToken = getOTFIToken(context);
        if (StringUtils.isBlank(otfiToken)) {
            FlowExecutionEngineUtils.addFlowContextToCache(context);
        } else {
            context.getProperties().remove(OTFI);
            FlowExecutionEngineUtils.addFlowContextToCache(otfiToken, context);
        }
    }

    /**
     * Make sure a retained one time flow id is set before caching, so the context is cached under the id that stays
     * active. Continued retention keeps the request's one time flow id. Started retention needs the executor to set
     * one, as the flow id is removed after caching.
     *
     * @param context             Flow execution context.
     * @param flowId              Flow id of the request.
     * @param isOTFIRetainingFlow Whether the request used a retained one time flow id.
     * @throws FlowEngineException If retention starts without a one time flow id.
     */
    private void resolveRetainedOTFI(FlowExecutionContext context, String flowId, boolean isOTFIRetainingFlow)
            throws FlowEngineException {

        if (!shouldRetainOTFI(context) || StringUtils.isNotBlank(getOTFIToken(context))) {
            return;
        }
        if (isOTFIRetainingFlow) {
            context.setProperty(OTFI, flowId);
            return;
        }
        throw FlowExecutionEngineUtils.handleServerException(context.getFlowType(),
                Constants.ErrorMessages.ERROR_CODE_RETAINED_OTFI_NOT_FOUND,
                OTFIRetention.fromContext(context).getExecutorName(), context.getContextIdentifier());
    }

    /**
     * Apply the polling state an executor set, before the context is cached. Requested polling replaces any entry
     * with a new one, and failed polling deletes its entry. Completed polling keeps its entry until the waiting client
     * continues the flow. Its poll data is added only to a POLL step, as only that step polls.
     *
     * @param context              Flow execution context.
     * @param step                 Current execution step.
     * @param previousPollingState Polling state of the flow when the request started.
     * @throws FlowEngineException If the polling entry cannot be created.
     */
    private void updatePolling(FlowExecutionContext context, FlowExecutionStep step,
                               PollingState previousPollingState) throws FlowEngineException {

        PollingState pollingState = context.getPollingState();
        if (pollingState == PollingState.IN_PROGRESS) {
            return;
        }
        if (pollingState == PollingState.COMPLETED && previousPollingState != PollingState.COMPLETED) {
            // The waiting client has not continued yet, so it still needs the entry.
            return;
        }
        // Failed or replaced polling ends here, and so does completed polling once the waiting client continued.
        endPolling(context);
        if (pollingState != PollingState.REQUESTED) {
            return;
        }
        String pollToken = FlowPollingService.getInstance().create(context.getContextIdentifier());
        context.setPollToken(pollToken);
        context.setPollingState(PollingState.IN_PROGRESS);
        if (!POLL.equals(step.getStepType())) {
            // No client can poll this step, so the token stays hidden. The entry ends with the polling or expires.
            return;
        }
        if (step.getData() == null) {
            step.setData(new DataDTO.Builder().additionalData(new HashMap<>()).build());
        }
        step.getData().addAdditionalData(POLL_TOKEN, pollToken);
        step.getData().addAdditionalData(POLL_INTERVAL, String.valueOf(POLL_INTERVAL_SECONDS));
        step.getData().addAdditionalData(POLL_EXPIRES_IN, String.valueOf(POLL_VALIDITY_SECONDS));
    }

    /**
     * Mark the flow's polling entry as successful. If it is not marked, for example because it expired or was
     * deleted, the waiting client never continues, so the request fails and the error path ends the flow.
     *
     * @param context Flow execution context.
     * @param flowId  Flow id of the request.
     * @throws FlowEngineException If the entry is not marked.
     */
    private void markPollingSuccess(FlowExecutionContext context, String flowId) throws FlowEngineException {

        if (!FlowPollingService.getInstance().markSuccess(context.getPollToken())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("The polling entry of the flow: " + context.getContextIdentifier()
                        + " is not marked as successful. Hence ending the flow.");
            }
            throw FlowExecutionEngineUtils.handleClientException(context.getFlowType(), ERROR_CODE_INVALID_FLOW_ID,
                    flowId);
        }
    }

    /**
     * End the flow's polling and delete its entry, so the waiting client learns at once that its polling ended.
     *
     * @param context Flow execution context, null if the flow was not found.
     */
    private void endPolling(FlowExecutionContext context) {

        if (context == null) {
            return;
        }
        String pollToken = context.getPollToken();
        context.setPollToken(null);
        context.setPollingState(null);
        if (pollToken == null) {
            return;
        }
        try {
            FlowPollingService.getInstance().delete(pollToken);
        } catch (FlowEngineException e) {
            LOG.error("Error while deleting the polling entry of the flow: " + context.getContextIdentifier(), e);
        }
    }

    /**
     * Get the one time flow id an executor set in the context.
     *
     * @param context Flow execution context.
     * @return One time flow id, or null if none is set.
     */
    private String getOTFIToken(FlowExecutionContext context) {

        return (String) context.getProperty(OTFI);
    }

    /**
     * Check whether the executor keeps the one time flow id across requests.
     *
     * @param context Flow execution context.
     * @return True if the one time flow id is retained.
     */
    private boolean shouldRetainOTFI(FlowExecutionContext context) {

        return OTFIRetention.fromContext(context) != null;
    }
}
