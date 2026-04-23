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
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
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
 *       Context updates are already applied directly to the {@link FlowExecutionContext}
 *       by the response processor.</li>
 * </ol>
 */
public class InFlowExtensionExecutor implements Executor {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionExecutor.class);
    private static final String EXECUTOR_NAME = "InFlowExtensionExecutor";

    public static final String FLOW_EXECUTION_CONTEXT_KEY = "flowExecutionContext";
    public static final String PATH_TYPE_ANNOTATIONS_KEY = "pathTypeAnnotations";
    private static final String ACTION_ID_METADATA_KEY = "actionId";
    private static final String ERROR_TYPE_KEY = "errorType";
    private static final String EXTENSION_ERROR_TYPE = "EXTENSION_ERROR";

    @Override
    public String getName() {

        return EXECUTOR_NAME;
    }

    @Override
    public ExecutorResponse execute(FlowExecutionContext context) throws FlowEngineException {

        ExecutorResponse response = new ExecutorResponse();

        try {

            // TODO: STATUS_ERROR should be returned, should be a diagnostic log
            String actionId = getMetadataValue(context, ACTION_ID_METADATA_KEY);
            if (actionId == null || actionId.isEmpty()) {
                LOG.warn("No action ID configured for In-Flow Extension executor. Skipping execution.");
                response.setResult(ExecutorStatus.STATUS_COMPLETE);
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
                response.setResult(ExecutorStatus.STATUS_ERROR);
                return response;
            }

            FlowContext flowContext = FlowContext.create()
                    .add(FLOW_EXECUTION_CONTEXT_KEY, context);

            // TODO: Have <T> and switch-case
            ActionExecutionStatus<?> executionStatus = actionExecutorService.execute(
                    ActionType.IN_FLOW_EXTENSION, actionId, flowContext, context.getTenantDomain());

            // TODO: Handle the redirection as well, currently we are treating it as an error since the flow execution engine doesn't support it yet

            ExecutorResponse executionResponse = mapExecutionStatus(executionStatus);

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
     * Only performs status translation — context updates are handled by the response processor.
     *
     * @param executionStatus The status returned by ActionExecutorService.
     * @return The ExecutorResponse for the flow execution engine.
     */
    private ExecutorResponse mapExecutionStatus(ActionExecutionStatus<?> executionStatus) {

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
                    response.setErrorMessage(buildUserFacingErrorMessage(failure));
                }
                break;

            case ERROR:
                response.setResult(ExecutorStatus.STATUS_ERROR);
                Error error = (Error) executionStatus.getResponse();
                if (error != null) {
                    response.setErrorMessage(buildUserFacingErrorMessage(error));
                }
                break;

            case INCOMPLETE:
                response.setResult(ExecutorStatus.STATUS_ERROR);
                break;

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

    /**
     * Build a user-facing error message from the error details returned by the external service.
     * Prefers the errorDescription (human-readable). Falls back to errorMessage if description is absent.
     *
     * @param error The error object from the external service.
     * @return A display-ready error message string.
     */
    private String buildUserFacingErrorMessage(Error error) {

        String description = error.getErrorDescription();
        String message = error.getErrorMessage();

        if (description != null && !description.isEmpty()) {
            return description;
        }
        if (message != null && !message.isEmpty()) {
            return message;
        }
        return "An unexpected error occurred in the external service.";
    }

    private ActionExecutorService getActionExecutorService() {

        return FlowExecutionEngineDataHolder.getInstance().getActionExecutorService();
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
