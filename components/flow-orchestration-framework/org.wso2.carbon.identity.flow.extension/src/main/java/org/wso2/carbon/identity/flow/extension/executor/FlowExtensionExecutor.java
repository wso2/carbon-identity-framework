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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.constant.ActionExecutionLogConstants;
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

/**
 * Executes Flow Extension actions during flow execution by delegating to
 * {@link ActionExecutorService} and mapping the result to an {@link ExecutorResponse}.
 * On success, pending context updates (claims, credentials) are forwarded
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
            triggerDiagnosticFailure(ActionExecutionLogConstants.ActionIDs.EXECUTE_ACTION, null,
                "Flow Extension action execution failed: action ID is not configured.");
            return buildErrorResponse(FlowExtensionConstants.ErrorMessages.NOT_CONFIGURED_MESSAGE,
                FlowExtensionConstants.ErrorMessages.NOT_CONFIGURED_DESCRIPTION);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing Flow Extension action. actionId: " + actionId
                    + ", flowType: " + context.getFlowType()
                    + ", tenant: " + context.getTenantDomain());
        }

        ActionExecutorService actionExecutorService = getActionExecutorService();

        if (!actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSION)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping Flow Extension action — action type is disabled. actionId: " + actionId);
            }
            ExecutorResponse skip = new ExecutorResponse();
            skip.setResult(ExecutorStatus.STATUS_COMPLETE);
            return skip;
        }

        try {
            // Pass only the filtered copy of the FlowExecutionContext to the action framework.
            FlowExecutionContext filteredContext = FlowExtensionUtil.filterContext(context);

            FlowContext actionFlowContext = FlowContext.create()
                    .add(FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, filteredContext);

            ActionExecutionStatus<?> executionStatus = actionExecutorService.execute(
                    ActionType.FLOW_EXTENSION, actionId, actionFlowContext, context.getTenantDomain());

            ExecutorResponse executionResponse = mapExecutionStatus(executionStatus, actionFlowContext, actionId);

            if (ExecutorStatus.STATUS_COMPLETE.equals(executionResponse.getResult())) {
                applyPendingContextUpdates(executionResponse, actionFlowContext, actionId);
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
     * For INCOMPLETE/redirect the extension's redirect URL is forwarded as-is; correlation on
     * resume is the extension integration layer's responsibility (typically via a mapping it
     * maintains between its own redirect-side identifier and the IS flow context).
     *
     * @param executionStatus The status returned by ActionExecutorService.
     * @param actionFlowContext     The action {@link FlowContext} where the response processor stashed the redirect URL.
     * @param actionId        The action ID for logging retry metadata.
     * @return The ExecutorResponse for the flow execution engine.
     */
    private ExecutorResponse mapExecutionStatus(ActionExecutionStatus<?> executionStatus,
                                                FlowContext actionFlowContext, String actionId) {

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
                return response;

            case ERROR:
                handleErrorStatus(response, executionStatus);
                return response;

            case INCOMPLETE:
                return handleIncompleteExecutionStatus(response, actionFlowContext, actionId);

            default:
                return handleUnknownExecutionStatus(response, executionStatus);
        }
    }

    private ExecutorResponse buildErrorResponse(String errorMessage, String errorDescription) {

        ExecutorResponse response = new ExecutorResponse();
        response.setResult(ExecutorStatus.STATUS_ERROR);
        response.setErrorCode(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getCode());
        response.setErrorMessage(errorMessage);
        response.setErrorDescription(errorDescription);
        return response;
    }

    private void handleFailedStatus(ExecutorResponse response, ActionExecutionStatus<?> executionStatus) {

        response.setResult(ExecutorStatus.STATUS_USER_ERROR);
        Failure failure = (Failure) executionStatus.getResponse();

        String failureReason = StringUtils.isNotBlank(failure.getFailureReason()) ? failure.getFailureReason() :
                Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_FAILURE.getMessage();
        String failureDescription = StringUtils.isNotBlank(failure.getFailureDescription()) ? failure.getFailureDescription() :
                Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_FAILURE.getDescription();

        response.setErrorCode(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_FAILURE.getCode());
        response.setErrorMessage(failureReason);
        response.setErrorDescription(failureDescription);
    }

    private void handleErrorStatus(ExecutorResponse response, ActionExecutionStatus<?> executionStatus) {

        response.setResult(ExecutorStatus.STATUS_ERROR);
        response.setErrorCode(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getCode());
        Error error = (Error) executionStatus.getResponse();
        if (error == null) {
            return;
        }

        String errorMessage = StringUtils.isNotBlank(error.getErrorMessage()) ? error.getErrorMessage() :
                Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getMessage();
        String errorDescription = StringUtils.isNotBlank(error.getErrorDescription()) ? error.getErrorDescription() :
                Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getDescription();

        response.setErrorMessage(errorMessage);
        response.setErrorDescription(errorDescription);
    }

    private ExecutorResponse handleIncompleteExecutionStatus(ExecutorResponse response, FlowContext actionFlowContext,
                                                             String actionId) {

        String redirectUrl = actionFlowContext.getValue(FlowExtensionConstants.PENDING_REDIRECT_URL_KEY, String.class);
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            // Defensive: response processor should have rejected this earlier.
            LOG.debug("Flow Extension returned INCOMPLETE without a redirect URL.");
            triggerDiagnosticFailure(ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_RESPONSE, actionId,
                "Flow Extension returned INCOMPLETE without a redirect URL.");
            return buildErrorResponse(FlowExtensionConstants.ErrorMessages.INCOMPLETE_NO_REDIRECT_MESSAGE,
                FlowExtensionConstants.ErrorMessages.INCOMPLETE_NO_REDIRECT_DESCRIPTION);
        }

        Map<String, String> redirectInfo = new HashMap<>();
        redirectInfo.put(Constants.REDIRECT_URL, redirectUrl);
        response.setAdditionalInfo(redirectInfo);

        response.setResult(ExecutorStatus.STATUS_EXTERNAL_REDIRECTION);
        triggerDiagnosticSuccess(ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_RESPONSE, actionId,
                "Flow Extension returned INCOMPLETE with a redirect URL.");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Flow Extension returned INCOMPLETE. Redirect initiated.");
        }

        return response;
    }

    private ExecutorResponse handleUnknownExecutionStatus(ExecutorResponse response,
                                                          ActionExecutionStatus<?> executionStatus) {

        LOG.error("Unknown execution status: " + executionStatus.getStatus());
        response.setResult(ExecutorStatus.STATUS_ERROR);
        response.setErrorCode(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getCode());
        response.setErrorMessage(Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getMessage());
        response.setErrorDescription("The Flow Extension returned an unrecognized status.");
        return response;
    }

    private void applyPendingContextUpdates(ExecutorResponse response, FlowContext actionFlowContext, String actionId) {

        Map<String, Object> pendingClaims =
                actionFlowContext.getValue(FlowExtensionConstants.PENDING_CLAIMS_KEY, Map.class);
        if (pendingClaims != null && !pendingClaims.isEmpty()) {
            response.setUpdatedUserClaims(pendingClaims);
        }

        Map<String, char[]> pendingCredentials =
                actionFlowContext.getValue(FlowExtensionConstants.PENDING_CREDENTIALS_KEY, Map.class);
        if (pendingCredentials != null && !pendingCredentials.isEmpty()) {
            response.setUserCredentials(pendingCredentials);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Flow Extension action succeeded. actionId: " + actionId
                    + ", pendingClaims: " + (pendingClaims != null ? pendingClaims.size() : 0)
                    + ", pendingCredentials: " + (pendingCredentials != null ? pendingCredentials.size() : 0));
        }
    }

    private void triggerDiagnosticFailure(String diagnosticActionId, String actionId, String resultMessage) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder builder = new DiagnosticLog.DiagnosticLogBuilder(
            ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID, diagnosticActionId)
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
            ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID, diagnosticActionId)
                .resultMessage(resultMessage)
                .configParam(CONFIG_PARAM_ACTION_TYPE, ActionType.FLOW_EXTENSION.getDisplayName())
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .resultStatus(DiagnosticLog.ResultStatus.SUCCESS);

        if (actionId != null) {
            builder.configParam(CONFIG_PARAM_ACTION_ID, actionId);
        }

        LoggerUtils.triggerDiagnosticLogEvent(builder);
    }

    private ActionExecutorService getActionExecutorService() throws FlowEngineException {

        ActionExecutorService actionExecutorService =
                FlowExtensionDataHolder.getInstance().getActionExecutorService();
        if (actionExecutorService == null) {
            throw FlowExecutionEngineUtils.handleServerException(
                    Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR,
                    "ActionExecutorService is not available.");
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
