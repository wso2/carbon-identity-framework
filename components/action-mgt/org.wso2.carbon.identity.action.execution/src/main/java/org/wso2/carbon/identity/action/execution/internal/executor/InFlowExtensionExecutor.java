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

package org.wso2.carbon.identity.action.execution.internal.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.action.execution.internal.component.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for executing In-Flow Extension actions during flow execution.
 * It integrates with the ActionExecutorService to call external services and process their responses.
 */
public class InFlowExtensionExecutor implements Executor {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionExecutor.class);
    private static final String EXECUTOR_NAME = "ExtensionExecutor";
//    private static final String FLOW_EXECUTION_CONTEXT_KEY = "flowExecutionContext";
    protected static final String CORRELATION_ID_KEY = "correlationId";
    protected static final String FLOW_USER_INPUT_DATA_KEY = "userInputData";
    protected static final String FLOW_USER_KEY = "flowUser";
    protected static final String FLOW_PROPERTIES_KEY = "flowProperties";
    protected static final String FLOW_CONTEXT_UPDATES_KEY = "contextUpdates";
    protected static final String ACTION_ID_METADATA_KEY = "actionId";
    protected static final String ALLOWED_OPERATIONS_METADATA_KEY = "allowedOperations";
    protected static final String ALLOWED_OPERATIONS_KEY = "allowedOperations";
    protected static final String TENET_DOMAIN_KEY = "tenetDomain";
    protected static final String APPLICATION_ID_KEY = "applicationId";
    protected static final String FLOW_TYPE_KEY = "flowType";
    protected static final String CURRENT_NODE_KEY = "currentNode";

    @Override
    public String getName() {

        return EXECUTOR_NAME;
    }

    @Override
    public ExecutorResponse execute(FlowExecutionContext context) throws FlowEngineException {

        ExecutorResponse response = new ExecutorResponse();

        try {
            // Get the action ID from the executor metadata configuration
            String actionId = getActionIdFromMetadata(context);
            if (actionId == null || actionId.isEmpty()) {
                LOG.warn("No action ID configured for In-Flow Extension executor. Skipping execution.");
                response.setResult(ExecutorResult.COMPLETE.name());
                return response;
            }

            LOG.debug("Executing In-Flow Extension action with ID: " + actionId);

            // Build the flow context for ActionExecutorService
            FlowContext flowContext = buildFlowContext(context);  // passing the whole flow execution context as an object for now

            // Get the ActionExecutorService
            ActionExecutorService actionExecutorService = getActionExecutorService();
            if (actionExecutorService == null) {
                throw new FlowEngineException("ActionExecutorService is not available.");
            }

            // Check if execution is enabled for this action type
            if (!actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)) {
                LOG.debug("In-Flow Extension action execution is disabled. Skipping.");
                response.setResult(ExecutorResult.COMPLETE.name());
                return response;
            }

            // Execute the action
            ActionExecutionStatus<?> executionStatus = actionExecutorService.execute(
                    ActionType.IN_FLOW_EXTENSION,
                    actionId,
                    flowContext,
                    context.getTenantDomain());

            // Process the result
            return processExecutionStatus(executionStatus, context);

        } catch (ActionExecutionException e) {
            LOG.error("Error executing In-Flow Extension action.", e);
            response.setResult(ExecutorResult.ERROR.name());
            response.setErrorMessage("Action execution failed");
            response.setErrorDescription(e.getMessage());
            response.setThrowable(e);
            return response;
        }
    }

    @Override
    public List<String> getInitiationData() {

        return Collections.emptyList();
    }

    @Override
    public ExecutorResponse rollback(FlowExecutionContext context) throws FlowEngineException {

        ExecutorResponse response = new ExecutorResponse();
        response.setResult(ExecutorResult.COMPLETE.name());
        return response;
    }

    /**
     * Build the FlowContext for ActionExecutorService from FlowExecutionContext.
     *
     * @param context The FlowExecutionContext.
     * @return The FlowContext for action execution.
     */
    private FlowContext buildFlowContext(FlowExecutionContext context) {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(CORRELATION_ID_KEY, context.getCorrelationId());
        flowContext.add(FLOW_USER_KEY, context.getFlowUser());
        flowContext.add(FLOW_USER_INPUT_DATA_KEY, context.getUserInputData());
        flowContext.add(FLOW_PROPERTIES_KEY, context.getProperties());
        flowContext.add(TENET_DOMAIN_KEY, context.getTenantDomain());
        flowContext.add(APPLICATION_ID_KEY, context.getApplicationId());
        flowContext.add(FLOW_TYPE_KEY, context.getFlowType());
        flowContext.add(CURRENT_NODE_KEY, context.getCurrentNode());

        // Add allowed operations from executor metadata
        String allowedOperationsJson = getAllowedOperationsFromMetadata(context);
        if (allowedOperationsJson != null) {
            flowContext.add(ALLOWED_OPERATIONS_KEY, allowedOperationsJson);
        }

        return flowContext;
    }

    /**
     * Process the ActionExecutionStatus and map it to ExecutorResponse.
     *
     * @param executionStatus The status returned by ActionExecutorService.
     * @param context The FlowExecutionContext to update with response data.
     * @return The ExecutorResponse.
     */
    @SuppressWarnings("unchecked")
    private ExecutorResponse processExecutionStatus(ActionExecutionStatus<?> executionStatus,
                                                    FlowExecutionContext context) {

        ExecutorResponse response = new ExecutorResponse();

        if (executionStatus == null) {
            response.setResult(ExecutorResult.USER_INPUT_REQUIRED.name());
            return response;
        }

        switch (executionStatus.getStatus()) {
            case SUCCESS:
                response.setResult(ExecutorResult.COMPLETE.name());
                // Extract context updates from response and apply them
                applyContextUpdates(executionStatus.getResponseContext(), context, response);
                break;

            case FAILED:
                response.setResult(ExecutorResult.USER_ERROR.name());
                Failure failure = (Failure) executionStatus.getResponse();
                if (failure != null) {
                    response.setErrorMessage(failure.getFailureReason());
                    response.setErrorDescription(failure.getFailureDescription());
                }
                break;

            case ERROR:
                response.setResult(ExecutorResult.ERROR.name());
                Error error = (Error) executionStatus.getResponse();
                if (error != null) {
                    response.setErrorMessage(error.getErrorMessage());
                    response.setErrorDescription(error.getErrorDescription());
                }
                break;

            case INCOMPLETE:
                // INCOMPLETE status indicates the flow should wait for external input
                response.setResult(ExecutorResult.USER_INPUT_REQUIRED.name());
                break;

            default:
                LOG.warn("Unknown execution status: " + executionStatus.getStatus());
                response.setResult(ExecutorResult.COMPLETE.name());
        }

        return response;
    }

    /**
     * Apply context updates from the action response to the FlowExecutionContext.
     *
     * @param responseContext The response context from action execution.
     * @param flowContext The FlowExecutionContext to update.
     * @param response The ExecutorResponse to populate with context properties.
     */
    @SuppressWarnings("unchecked")
    private void applyContextUpdates(Map<String, Object> responseContext,
                                     FlowExecutionContext flowContext,
                                     ExecutorResponse response) {

        if (responseContext == null || responseContext.isEmpty()) {
            return;
        }

        // Extract context updates from the response
        Object contextUpdatesObj = responseContext.get(FLOW_CONTEXT_UPDATES_KEY);
        if (contextUpdatesObj instanceof Map) {
            Map<String, Object> contextUpdates = (Map<String, Object>) contextUpdatesObj;

            // Apply updates to FlowExecutionContext properties
            for (Map.Entry<String, Object> entry : contextUpdates.entrySet()) {
                flowContext.setProperty(entry.getKey(), entry.getValue());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Applied context update: " + entry.getKey());
                }
            }

            // Also set the context properties in the executor response
            response.setContextProperty(contextUpdates);
        }
    }

    /**
     * Get the ActionExecutorService instance.
     *
     * @return The ActionExecutorService.
     */
    private ActionExecutorService getActionExecutorService() {

        return ActionExecutionServiceComponentHolder.getInstance().getActionExecutorService();
    }

    /**
     * Extract the action ID from the executor metadata configuration.
     * The action ID should be configured in the flow JSON as part of the executor's meta object.
     *
     * @param context The FlowExecutionContext containing the current node configuration.
     * @return The action ID if configured, null otherwise.
     */
    private String getActionIdFromMetadata(FlowExecutionContext context) {

        NodeConfig currentNode = context.getCurrentNode();
        if (currentNode == null) {
            LOG.debug("Current node is null, cannot extract action ID from metadata.");
            return null;
        }

        ExecutorDTO executorConfig = currentNode.getExecutorConfig();
        if (executorConfig == null) {
            LOG.debug("Executor config is null, cannot extract action ID from metadata.");
            return null;
        }

        Map<String, String> metadata = executorConfig.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            LOG.debug("Executor metadata is null or empty, cannot extract action ID.");
            return null;
        }

        return metadata.get(ACTION_ID_METADATA_KEY);
    }

    /**
     * Extract the allowed operations configuration from the executor metadata.
     * The allowed operations should be configured in the flow JSON as part of the executor's meta object.
     *
     * @param context The FlowExecutionContext containing the current node configuration.
     * @return The allowed operations JSON string if configured, null otherwise.
     */
    private String getAllowedOperationsFromMetadata(FlowExecutionContext context) {

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

        return metadata.get(ALLOWED_OPERATIONS_METADATA_KEY);
    }

    /**
     * Enum representing the possible results of executor execution.
     * These values must match the expected status values in the flow execution engine.
     * @see org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus
     */
    public enum ExecutorResult {
        COMPLETE,           // Executor completed successfully
        ERROR,              // Server-side error occurred
        USER_ERROR,         // User-related error (e.g., validation failure)
        USER_INPUT_REQUIRED,// Additional user input needed
        EXTERNAL_REDIRECTION,// Redirect to external service
        RETRY               // Retry the current step
    }
}
