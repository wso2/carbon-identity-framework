/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.constant.ActionExecutionLogConstants;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus;
import org.wso2.carbon.utils.DiagnosticLog;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.config.FlowContextHandoverConfig;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.config.FlowContextHandoverPolicy;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.config.FlowExecutionContextFilter;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class is responsible for executing In-Flow Extension actions during flow execution.
 * It integrates with the {@link ActionExecutorService} to call external services and process
 * their responses.
 *
 * <p>Execution lifecycle:</p>
 * <ol>
 *   <li>Extract executor metadata: {@code actionId}.</li>
 *   <li>Build a minimal {@link FlowContext} containing only the {@link FlowExecutionContext}.
 *       The request builder resolves access config / encryption from the action and populates
 *       additional FlowContext keys for the response processor.</li>
 *   <li>Invoke the external service via {@link ActionExecutorService}.</li>
 *   <li>Map the {@link ActionExecutionStatus} to an {@link ExecutorResponse}.
 *       On {@code SUCCESS}, pending context updates collected by the response processor
 *       are extracted from the {@link FlowContext} and forwarded to
 *       {@code TaskExecutionNode} via {@link ExecutorResponse} fields
 *       ({@code updatedUserClaims}, {@code userCredentials}, {@code contextProperties}).</li>
 * </ol>
 */
public class InFlowExtensionExecutor implements Executor {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionExecutor.class);
    private static final String EXECUTOR_NAME = "InFlowExtensionExecutor";

    public static final String FLOW_EXECUTION_CONTEXT_KEY = "flowExecutionContext";
    public static final String HANDOVER_POLICY_KEY = "handoverPolicy";
    public static final String PATH_TYPE_ANNOTATIONS_KEY = "pathTypeAnnotations";
    public static final String PENDING_CLAIMS_KEY = "pendingClaims";
    public static final String PENDING_CREDENTIALS_KEY = "pendingCredentials";
    public static final String PENDING_PROPERTIES_KEY = "pendingProperties";
    public static final String PENDING_REDIRECT_URL_KEY = "pendingRedirectUrl";
    private static final String ACTION_ID_METADATA_KEY = "actionId";
    private static final String ERROR_TYPE_KEY = "errorType";
    private static final String EXTENSION_ERROR_TYPE = "EXTENSION_ERROR";
    public static final String ERROR_MESSAGE_KEY = "errorMessage";
    public static final String ERROR_DESCRIPTION_KEY = "errorDescription";
    public static final String EXTENSION_ERROR_CODE = "65033";

    @Override
    public String getName() {

        return EXECUTOR_NAME;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ExecutorResponse execute(FlowExecutionContext context) throws FlowEngineException {

        ExecutorResponse response = new ExecutorResponse();

        String actionId = getMetadataValue(context, ACTION_ID_METADATA_KEY);
        if (actionId == null || actionId.isEmpty()) {
            LOG.warn("No action ID configured for In-Flow Extension executor. Cannot execute.");
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                        ActionExecutionLogConstants.ActionIDs.EXECUTE_ACTION)
                        .resultMessage("In-Flow Extension action execution failed: action ID is not configured.")
                        .configParam("actionType", ActionType.IN_FLOW_EXTENSION.getDisplayName())
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED));
            }
            response.setResult(ExecutorStatus.STATUS_ERROR);
            return response;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing In-Flow Extension action with ID: " + actionId);
        }

        ActionExecutorService actionExecutorService = getActionExecutorService();
        if (actionExecutorService == null) {
            throw new FlowEngineException("ActionExecutorService is not available.");
        }

        if (!actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)) {
            LOG.debug("In-Flow Extension action execution is disabled.");
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                        ActionExecutionLogConstants.ActionIDs.EXECUTE_ACTION)
                        .resultMessage("In-Flow Extension action execution failed: action type is disabled.")
                        .configParam("actionType", ActionType.IN_FLOW_EXTENSION.getDisplayName())
                        .configParam("actionId", actionId)
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED));
            }
            response.setResult(ExecutorStatus.STATUS_ERROR);
            return response;
        }

        try {
            // Resolve the per-flow-type handover policy and hand the action framework only a
            // FILTERED copy of the FlowExecutionContext (non-whitelisted fields nulled out).
            // The original `context` is untouched and continues to drive the engine's own
            // bookkeeping (OTFI cache swap, TaskExecutionNode pending-update propagation).
            FlowContextHandoverConfig handoverConfig = FlowExecutionEngineDataHolder.getInstance()
                    .getFlowContextHandoverConfig();
            FlowContextHandoverPolicy policy = handoverConfig.resolve(context.getFlowType());
            FlowExecutionContext filteredContext = FlowExecutionContextFilter.filter(context, policy);

            FlowContext flowContext = FlowContext.create()
                    .add(FLOW_EXECUTION_CONTEXT_KEY, filteredContext)
                    .add(HANDOVER_POLICY_KEY, policy);

            // TODO: Have <T> and switch-case
            ActionExecutionStatus<?> executionStatus = actionExecutorService.execute(
                    ActionType.IN_FLOW_EXTENSION, actionId, flowContext, context.getTenantDomain());

            ExecutorResponse executionResponse = mapExecutionStatus(executionStatus, flowContext, context);

            // On success, extract pending context updates collected by the response processor
            // and forward them to TaskExecutionNode via ExecutorResponse fields.
            if (ExecutorStatus.STATUS_COMPLETE.equals(executionResponse.getResult())) {
                Map<String, Object> pendingClaims =
                        (Map<String, Object>) flowContext.getValue(PENDING_CLAIMS_KEY, Map.class);
                if (pendingClaims != null && !pendingClaims.isEmpty()) {
                    executionResponse.setUpdatedUserClaims(pendingClaims);
                }
                Map<String, char[]> pendingCredentials =
                        (Map<String, char[]>) flowContext.getValue(PENDING_CREDENTIALS_KEY, Map.class);
                if (pendingCredentials != null && !pendingCredentials.isEmpty()) {
                    executionResponse.setUserCredentials(pendingCredentials);
                }
                Map<String, Object> pendingProperties =
                        (Map<String, Object>) flowContext.getValue(PENDING_PROPERTIES_KEY, Map.class);
                if (pendingProperties != null && !pendingProperties.isEmpty()) {
                    executionResponse.setContextProperty(pendingProperties);
                }
            }

            // Tag RETRY responses with errorType so the frontend can identify extension errors.
            if (ExecutorStatus.STATUS_RETRY.equals(executionResponse.getResult())) {
                Map<String, String> additionalInfo = executionResponse.getAdditionalInfo();
                if (additionalInfo == null) {
                    additionalInfo = new HashMap<>();
                }
                additionalInfo.put(ERROR_TYPE_KEY, EXTENSION_ERROR_TYPE);
                executionResponse.setAdditionalInfo(additionalInfo);
            }

            return executionResponse;

        } catch (ActionExecutionException e) { // TODO: replace with a action execution server exception
            LOG.error("Error executing In-Flow Extension action.", e);
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                        ActionExecutionLogConstants.ActionIDs.EXECUTE_ACTION)
                        .resultMessage("In-Flow Extension action execution failed: " + e.getMessage())
                        .configParam("actionType", ActionType.IN_FLOW_EXTENSION.getDisplayName())
                        .configParam("actionId", actionId)
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED));
            }
            response.setResult(ExecutorStatus.STATUS_ERROR);
            response.setErrorMessage("An error occurred while processing the extension. Please try again.");
            return response;
        }
    }

    @Override
    public List<String> getInitiationData() {

        return Collections.emptyList();
    }

    @Override
    public ExecutorResponse rollback(FlowExecutionContext context) {

        return null;
    }

    /**
     * Map the {@link ActionExecutionStatus} to an {@link ExecutorResponse}.
     * Performs status translation and (for INCOMPLETE/redirect) generates the OTFI used by
     * {@code FlowExecutionService} to swap caches on resume — same pattern as {@code MagicLinkExecutor}.
     *
     * @param executionStatus The status returned by ActionExecutorService.
     * @param flowContext     The action {@link FlowContext} where the response processor stashed the redirect URL.
     * @param context         The engine {@link FlowExecutionContext} (used for OTFI collision-guard).
     * @return The ExecutorResponse for the flow execution engine.
     */
    private ExecutorResponse mapExecutionStatus(ActionExecutionStatus<?> executionStatus,
                                                FlowContext flowContext, FlowExecutionContext context) {

        ExecutorResponse response = new ExecutorResponse();

        if (executionStatus == null) {
            response.setResult(ExecutorStatus.STATUS_ERROR);
            return response;
        }

        switch (executionStatus.getStatus()) {
            case SUCCESS:
                response.setResult(ExecutorStatus.STATUS_COMPLETE);
                break;

            case FAILED:
                response.setResult(ExecutorStatus.STATUS_RETRY);
                Failure failure = (Failure) executionStatus.getResponse();
                if (failure != null) {
                    Map<String, String> failureInfo = new HashMap<>();
                    if (failure.getFailureReason() != null) {
                        failureInfo.put(ERROR_MESSAGE_KEY, failure.getFailureReason());
                    }
                    if (failure.getFailureDescription() != null) {
                        failureInfo.put(ERROR_DESCRIPTION_KEY, failure.getFailureDescription());
                    }
                    response.setAdditionalInfo(failureInfo);
                    response.setErrorMessage(buildUserFacingErrorMessage(failure));
                }
                break;

            case ERROR:
                response.setResult(ExecutorStatus.STATUS_ERROR);
                Error error = (Error) executionStatus.getResponse();
                response.setErrorCode(EXTENSION_ERROR_CODE);
                if (error != null) {
                    Map<String, String> errorInfo = new HashMap<>();
                    if (error.getErrorMessage() != null) {
                        errorInfo.put(ERROR_MESSAGE_KEY, error.getErrorMessage());
                    }
                    if (error.getErrorDescription() != null) {
                        errorInfo.put(ERROR_DESCRIPTION_KEY, error.getErrorDescription());
                    }
                    response.setAdditionalInfo(errorInfo);
                    response.setErrorMessage(stripI18nBraces(error.getErrorMessage()));
                    response.setErrorDescription(stripI18nBraces(error.getErrorDescription()));
                }
                break;

            case INCOMPLETE: {
                String redirectUrl = flowContext.getValue(PENDING_REDIRECT_URL_KEY, String.class);
                if (redirectUrl == null || redirectUrl.isEmpty()) {
                    // Defensive: response processor should have rejected this earlier.
                    response.setResult(ExecutorStatus.STATUS_ERROR);
                    response.setErrorMessage("Extension returned INCOMPLETE without a redirect URL.");
                    break;
                }

                // Generate OTFI exactly like MagicLinkExecutor — keeps the cache-swap mechanism
                // in FlowExecutionService unchanged across executors.
                String otfi = UUID.randomUUID().toString();
                while (otfi.equals(context.getContextIdentifier())) {
                    otfi = UUID.randomUUID().toString();
                }
                Map<String, Object> redirectProps = new HashMap<>();
                redirectProps.put(Constants.OTFI, otfi);
                response.setContextProperty(redirectProps);

                String separator = redirectUrl.contains("?") ? "&" : "?";
                String urlWithFlowId = redirectUrl + separator + "flowId=" + otfi;

                Map<String, String> redirectInfo = new HashMap<>();
                redirectInfo.put(Constants.REDIRECT_URL, urlWithFlowId);
                response.setAdditionalInfo(redirectInfo);

                response.setResult(ExecutorStatus.STATUS_EXTERNAL_REDIRECTION);
                break;
            }

            default:
                LOG.warn("Unknown execution status: " + executionStatus.getStatus());
                response.setResult(ExecutorStatus.STATUS_ERROR);
        }

        return response;
    }

    /**
     * Build a user-facing error message from the failure details returned by the external service.
     * Prefers the failureDescription (human-readable). Falls back to failureReason if description is absent.
     *
     * @param failure The failure object from the external service.
     * @return A display-ready error message string.
     */
    private String buildUserFacingErrorMessage(Failure failure) {

        String description = failure.getFailureDescription();
        String reason = failure.getFailureReason();

        if (description != null && !description.isEmpty()) {
            return description;
        }
        if (reason != null && !reason.isEmpty()) {
            return reason;
        }
        return "The operation could not be completed due to an external service failure.";
    }

    private ActionExecutorService getActionExecutorService() {

        return FlowExecutionEngineDataHolder.getInstance().getActionExecutorService();
    }

    /**
     * Strip the {@code {{...}}} wrapper from an i18n key so the JSP error page can resolve it
     * via {@code AuthenticationEndpointUtil.i18n(resourceBundle, key)}. Raw text values (without
     * the wrapper) and {@code null} are returned unchanged.
     */
    private static String stripI18nBraces(String value) {

        if (value == null) {
            return null;
        }
        if (value.startsWith("{{") && value.endsWith("}}") && value.length() > 4) {
            return value.substring(2, value.length() - 2);
        }
        return value;
    }

    /**
     * Read a single metadata value from the current node's executor configuration.
     *
     * @param context The FlowExecutionContext.
     * @param key     The metadata key.
     * @return The value, or {@code null} if not found.
     */
    private String getMetadataValue(FlowExecutionContext context, String key) {

        NodeConfig currentNode = context.getCurrentNode();
        if (currentNode == null) {
            return null;
        }
        ExecutorDTO executorConfig = currentNode.getExecutorConfig();
        if (executorConfig == null) {
            return null;
        }
        Map<String, String> metadata = executorConfig.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        return metadata.get(key);
    }
}
