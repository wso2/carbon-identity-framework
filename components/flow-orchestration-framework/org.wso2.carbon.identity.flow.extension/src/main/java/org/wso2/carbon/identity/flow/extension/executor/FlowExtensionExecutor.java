/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants;
import org.wso2.carbon.utils.DiagnosticLog;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.identity.flow.extension.internal.FlowExtensionDataHolder;
import org.wso2.carbon.identity.flow.extension.util.FlowExtensionUtil;
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
 * Executes In-Flow Extension actions during flow execution by delegating to
 * {@link ActionExecutorService} and mapping the result to an {@link ExecutorResponse}.
 * On success, pending context updates (claims, credentials, properties) are forwarded
 * to the flow engine through the response object.
 */
public class FlowExtensionExecutor implements Executor {

    private static final Log LOG = LogFactory.getLog(FlowExtensionExecutor.class);
    private static final String EXECUTOR_NAME = "FlowExtensionExecutor";
    private static final String CONFIG_PARAM_ACTION_TYPE = "actionType";
    private static final String CONFIG_PARAM_ACTION_ID = "actionId";



    @Override
    public String getName() {

        return EXECUTOR_NAME;
    }

    @Override
    public ExecutorResponse execute(FlowExecutionContext context) throws FlowEngineException {

        String actionId = getMetadataValue(context, FlowExtensionConstants.ACTION_ID_METADATA_KEY);
        if (actionId == null || actionId.isEmpty()) {
            triggerDiagnosticFailure(null,
                "Flow Extension action execution failed: action ID is not configured.");
            return buildErrorResponse(FlowExtensionConstants.ErrorMessages.NOT_CONFIGURED_MESSAGE,
                FlowExtensionConstants.ErrorMessages.NOT_CONFIGURED_DESCRIPTION);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing Flow Extension action. actionId: " + actionId
                    + ", flowType: " + context.getFlowType()
                    + ", tenant: " + context.getTenantDomain());
        }

        ActionExecutorService actionExecutorService = getActionExecutorService(actionId);

        if (!actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSION)) {
            triggerDiagnosticFailure(actionId,
                "Flow Extension action execution failed: action type is disabled.");
            return buildErrorResponse(FlowExtensionConstants.ErrorMessages.EXECUTION_DISABLED_MESSAGE,
                FlowExtensionConstants.ErrorMessages.EXECUTION_DISABLED_DESCRIPTION);
        }

        try {
            // Hand the action framework only a FILTERED copy of the FlowExecutionContext
            // (non-whitelisted fields nulled out). Policy is sourced from compile-time constants.
            FlowExecutionContext filteredContext = FlowExtensionUtil.filterContext(context);

            FlowContext flowContext = FlowContext.create()
                    .add(FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, filteredContext);

            ActionExecutionStatus<?> executionStatus = actionExecutorService.execute(
                    ActionType.FLOW_EXTENSION, actionId, flowContext, context.getTenantDomain());

            ExecutorResponse executionResponse = mapExecutionStatus(executionStatus, flowContext, context, actionId);

            // On success, extract pending context updates collected by the response processor
            // and forward them to TaskExecutionNode via ExecutorResponse fields.
            if (ExecutorStatus.STATUS_COMPLETE.equals(executionResponse.getResult())) {
                applyPendingContextUpdates(executionResponse, flowContext, actionId);
            }

            return executionResponse;

        } catch (ActionExecutionException e) {
            logActionExecutionException(e, actionId);
            return buildErrorResponse(FlowExtensionConstants.ErrorMessages.EXECUTION_FAILED_MESSAGE,
                FlowExtensionConstants.ErrorMessages.EXECUTION_FAILED_DESCRIPTION);
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
     * @param actionId        The action ID for logging retry metadata.
     * @return The ExecutorResponse for the flow execution engine.
     */
    private ExecutorResponse mapExecutionStatus(ActionExecutionStatus<?> executionStatus,
                                                FlowContext flowContext, FlowExecutionContext context, String actionId) {

        ExecutorResponse response = new ExecutorResponse();

        if (executionStatus == null) {
            response.setResult(ExecutorStatus.STATUS_ERROR);
            response.setErrorCode(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getCode());
            response.setErrorMessage(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getMessage());
            response.setErrorDescription("The Flow Extension action did not return a status. Please try again.");
            return response;
        }

        switch (executionStatus.getStatus()) {
            case SUCCESS:
                response.setResult(ExecutorStatus.STATUS_COMPLETE);
                return response;

            case FAILED:
                handleFailedStatus(response, executionStatus);
                applyRetryMetadata(response, actionId);
                return response;

            case ERROR:
                handleErrorStatus(response, executionStatus);
                return response;

            case INCOMPLETE:
                return handleIncompleteExecutionStatus(response, flowContext, context);

            default:
                return handleUnknownExecutionStatus(response, executionStatus);
        }
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

    private ExecutorResponse buildErrorResponse(String errorMessage, String errorDescription) {

        ExecutorResponse response = new ExecutorResponse();
        response.setResult(ExecutorStatus.STATUS_ERROR);
        response.setErrorCode(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getCode());
        response.setErrorMessage(errorMessage);
        response.setErrorDescription(errorDescription);
        return response;
    }

    private void applyRetryMetadata(ExecutorResponse response, String actionId) {

        Map<String, String> additionalInfo = response.getAdditionalInfo();
        if (additionalInfo == null) {
            additionalInfo = new HashMap<>();
        }
        additionalInfo.put(FlowExtensionConstants.FAILURE_TYPE_KEY,
                FlowExtensionConstants.FLOW_EXTENSION_FAILURE_TYPE);
        response.setAdditionalInfo(additionalInfo);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Flow Extension action returned FAILED. actionId: " + actionId
                    + ", reason: " + additionalInfo.get(FlowExtensionConstants.FAILURE_MESSAGE_KEY));
        }
    }

    private void handleFailedStatus(ExecutorResponse response, ActionExecutionStatus<?> executionStatus) {

        response.setResult(ExecutorStatus.STATUS_RETRY);
        Failure failure = (Failure) executionStatus.getResponse();
        if (failure == null) {
            return;
        }

        Map<String, String> failureInfo = new HashMap<>();
        if (failure.getFailureReason() != null) {
            failureInfo.put(FlowExtensionConstants.FAILURE_MESSAGE_KEY, failure.getFailureReason());
        }
        if (failure.getFailureDescription() != null) {
            failureInfo.put(FlowExtensionConstants.FAILURE_DESCRIPTION_KEY, failure.getFailureDescription());
        }
        response.setAdditionalInfo(failureInfo);
        response.setErrorCode(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_FAILURE.getCode());

        String reason = failure.getFailureReason();
        String description = failure.getFailureDescription();

        if (reason != null && !reason.isEmpty()) {
            response.setErrorMessage(reason);
        } else {
            response.setErrorMessage("The operation could not be completed.");
        }

        if (description != null && !description.isEmpty()) {
            response.setErrorDescription(description);
        } else {
            response.setErrorDescription(buildUserFacingErrorMessage(failure));
        }
    }

    private void handleErrorStatus(ExecutorResponse response, ActionExecutionStatus<?> executionStatus) {

        response.setResult(ExecutorStatus.STATUS_ERROR);
        response.setErrorCode(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getCode());
        Error error = (Error) executionStatus.getResponse();
        if (error == null) {
            return;
        }

        response.setErrorMessage(error.getErrorMessage());
        response.setErrorDescription(error.getErrorDescription());
    }

    private ExecutorResponse handleIncompleteExecutionStatus(ExecutorResponse response, FlowContext flowContext,
                                                             FlowExecutionContext context) {

        String redirectUrl = flowContext.getValue(FlowExtensionConstants.PENDING_REDIRECT_URL_KEY, String.class);
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            // Defensive: response processor should have rejected this earlier.
            LOG.debug("Flow Extension returned INCOMPLETE without a redirect URL.");
            triggerDiagnosticFailure(FlowExtensionConstants.Log.ActionIDs.PROCESS_RESPONSE, null,
                "Flow Extension returned INCOMPLETE without a redirect URL.");
            return buildErrorResponse(FlowExtensionConstants.ErrorMessages.INCOMPLETE_NO_REDIRECT_MESSAGE,
                FlowExtensionConstants.ErrorMessages.INCOMPLETE_NO_REDIRECT_DESCRIPTION);
        }

        String otfi = generateUniqueOtfi(context.getContextIdentifier());
        Map<String, Object> redirectProps = new HashMap<>();
        redirectProps.put(Constants.OTFI, otfi);
        response.setContextProperty(redirectProps);

        String urlWithFlowId = appendFlowId(redirectUrl, otfi);
        Map<String, String> redirectInfo = new HashMap<>();
        redirectInfo.put(Constants.REDIRECT_URL, urlWithFlowId);
        response.setAdditionalInfo(redirectInfo);

        response.setResult(ExecutorStatus.STATUS_EXTERNAL_REDIRECTION);
        triggerDiagnosticSuccess(FlowExtensionConstants.Log.ActionIDs.PROCESS_RESPONSE, null,
                "Flow Extension returned INCOMPLETE with a redirect URL.");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Flow Extension returned INCOMPLETE. Redirect initiated and flowId (OTFI) generated.");
        }

        return response;
    }

    private ExecutorResponse handleUnknownExecutionStatus(ExecutorResponse response,
                                                          ActionExecutionStatus<?> executionStatus) {

        LOG.warn("Unknown execution status: " + executionStatus.getStatus());
        response.setResult(ExecutorStatus.STATUS_ERROR);
        response.setErrorCode(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getCode());
        response.setErrorMessage(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getMessage());
        response.setErrorDescription("The Flow Extension returned an unrecognised status. Please try again.");
        return response;
    }

    private String generateUniqueOtfi(String currentContextIdentifier) {

        // Avoid accidental collision with the current context identifier.
        String otfi = UUID.randomUUID().toString();
        while (otfi.equals(currentContextIdentifier)) {
            otfi = UUID.randomUUID().toString();
        }
        return otfi;
    }

    private String appendFlowId(String redirectUrl, String otfi) {

        String separator = redirectUrl.contains("?") ? "&" : "?";
        return redirectUrl + separator + "flowId=" + otfi;
    }

    @SuppressWarnings("unchecked")
    private void applyPendingContextUpdates(ExecutorResponse response, FlowContext flowContext, String actionId) {

        Map<String, Object> pendingClaims =
                flowContext.getValue(FlowExtensionConstants.PENDING_CLAIMS_KEY, Map.class);
        if (pendingClaims != null && !pendingClaims.isEmpty()) {
            response.setUpdatedUserClaims(pendingClaims);
        }

        Map<String, char[]> pendingCredentials =
                flowContext.getValue(FlowExtensionConstants.PENDING_CREDENTIALS_KEY, Map.class);
        if (pendingCredentials != null && !pendingCredentials.isEmpty()) {
            response.setUserCredentials(pendingCredentials);
        }

        Map<String, Object> pendingProperties =
                flowContext.getValue(FlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class);
        if (pendingProperties != null && !pendingProperties.isEmpty()) {
            response.setContextProperty(pendingProperties);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Flow Extension action succeeded. actionId: " + actionId
                    + ", pendingClaims: " + (pendingClaims != null ? pendingClaims.size() : 0)
                    + ", pendingCredentials: " + (pendingCredentials != null ? pendingCredentials.size() : 0)
                    + ", pendingProperties: " + (pendingProperties != null ? pendingProperties.size() : 0));
        }
    }

    private void triggerDiagnosticFailure(String actionId, String resultMessage) {

        triggerDiagnosticFailure(FlowExtensionConstants.Log.ActionIDs.EXECUTE, actionId, resultMessage);
    }

    private void triggerDiagnosticFailure(String diagnosticActionId, String actionId, String resultMessage) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder builder = new DiagnosticLog.DiagnosticLogBuilder(
            FlowExtensionConstants.Log.COMPONENT_ID, diagnosticActionId)
                .resultMessage(resultMessage)
                .configParam(CONFIG_PARAM_ACTION_TYPE, ActionType.FLOW_EXTENSION.getDisplayName())
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .resultStatus(DiagnosticLog.ResultStatus.FAILED);

        if (actionId != null) {
            builder.configParam(CONFIG_PARAM_ACTION_ID, actionId);
        }

        LoggerUtils.triggerDiagnosticLogEvent(builder);
    }

    private void triggerDiagnosticSuccess(String diagnosticActionId, String actionId, String resultMessage) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder builder = new DiagnosticLog.DiagnosticLogBuilder(
            FlowExtensionConstants.Log.COMPONENT_ID, diagnosticActionId)
                .resultMessage(resultMessage)
                .configParam(CONFIG_PARAM_ACTION_TYPE, ActionType.FLOW_EXTENSION.getDisplayName())
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .resultStatus(DiagnosticLog.ResultStatus.SUCCESS);

        if (actionId != null) {
            builder.configParam(CONFIG_PARAM_ACTION_ID, actionId);
        }

        LoggerUtils.triggerDiagnosticLogEvent(builder);
    }

    private ActionExecutorService getActionExecutorService(String actionId) throws FlowEngineException {

        ActionExecutorService actionExecutorService =
                FlowExtensionDataHolder.getInstance().getActionExecutorService();
        if (actionExecutorService == null) {
            throw FlowExecutionEngineUtils.handleServerException(
                    Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR,
                    "ActionExecutorService is not available. actionId: " + actionId);
        }
        return actionExecutorService;
    }

    /**
     * Log an {@link ActionExecutionException} at the appropriate level based on its root cause.
     * Config, contract violations, and request builder failures are all treated as errors.
     */
    private void logActionExecutionException(ActionExecutionException e, String actionId) {

        Throwable cause = e.getCause();
        if (cause instanceof ActionExecutionRequestBuilderException) {
            LOG.error("Flow Extension action '" + actionId
                    + "' request build failed. Check action access configuration: " + e.getMessage(), e);
        } else if (cause instanceof ActionExecutionResponseProcessorException) {
            LOG.error("Flow Extension action '" + actionId
                    + "' response processing failed (extension contract violation or internal error).", e);
        } else {
            LOG.error("Error executing Flow Extension action '" + actionId + "'.", e);
        }
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
