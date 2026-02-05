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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.constant.ActionExecutionLogConstants;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionResponseContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.ErrorStatus;
import org.wso2.carbon.identity.action.execution.api.model.FailedStatus;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.action.execution.api.model.SuccessStatus;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class is responsible for processing the response from In-Flow Extension actions.
 * It processes operations (ADD, REMOVE, REPLACE) on flow context properties and user claims,
 * with dynamic path validation based on the allowed operations configured in the request.
 */
public class InFlowExtensionResponseProcessor implements ActionExecutionResponseProcessor {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionResponseProcessor.class);
    private static final String CONTEXT_UPDATES_KEY = "contextUpdates";
    
    // Path prefixes for In-Flow Extension context
    private static final String PROPERTIES_PATH_PREFIX = "/properties/";
    private static final String USER_CLAIMS_PATH_PREFIX = "/user/claims/";
    private static final String USER_INPUTS_PATH_PREFIX = "/userInputs/";
    
    private static final char PATH_SEPARATOR = '/';
    private static final String LAST_ELEMENT_CHARACTER = "-";

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.IN_FLOW_EXTENSION;
    }

    @Override
    public ActionExecutionStatus<Success> processSuccessResponse(FlowContext flowContext,
            ActionExecutionResponseContext<ActionInvocationSuccessResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        Map<String, Object> responseContextMap = new HashMap<>();
        Map<String, Object> contextUpdates = new HashMap<>();
        
        // Initialize sub-maps for different context areas
        Map<String, Object> propertiesUpdates = new HashMap<>();
        Map<String, Object> userClaimsUpdates = new HashMap<>();
        Map<String, Object> userInputsUpdates = new HashMap<>();
        
        List<OperationExecutionResult> operationExecutionResultList = new ArrayList<>();
        
        // Get operations from the response (already filtered by ActionExecutorServiceImpl)
        List<PerformableOperation> operationsToPerform = 
                responseContext.getActionInvocationResponse().getOperations();
        
        if (operationsToPerform != null && !operationsToPerform.isEmpty()) {
            for (PerformableOperation operation : operationsToPerform) {
                OperationExecutionResult result = processOperation(operation, 
                        propertiesUpdates, userClaimsUpdates, userInputsUpdates);
                operationExecutionResultList.add(result);
            }
        }
        
        // Log operation execution results
        logOperationExecutionResults(operationExecutionResultList);
        
        // Build the context updates map from processed operations
        // Add properties directly to contextUpdates (not nested under "properties")
        for (Map.Entry<String, Object> entry : propertiesUpdates.entrySet()) {
            Object value = entry.getValue();
            // If value is OperationValue, extract the actual value
            if (value instanceof OperationValue) {
                OperationValue opValue = (OperationValue) value;
                if (!"REMOVE".equals(opValue.getOperation())) {
                    contextUpdates.put(entry.getKey(), opValue.getValue());
                }
                // For REMOVE, we don't add to contextUpdates (removal handled by executor)
            } else {
                contextUpdates.put(entry.getKey(), value);
            }
        }
        
        // User claims and inputs are kept as separate maps for the executor to handle
        if (!userClaimsUpdates.isEmpty()) {
            contextUpdates.put("userClaims", userClaimsUpdates);
        }
        if (!userInputsUpdates.isEmpty()) {
            contextUpdates.put("userInputs", userInputsUpdates);
        }
        
        if (!contextUpdates.isEmpty()) {
            responseContextMap.put(CONTEXT_UPDATES_KEY, contextUpdates);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processed " + operationExecutionResultList.size() + 
                        " operations from In-Flow Extension response.");
            }
        }

        return new SuccessStatus.Builder()
                .setSuccess(new InFlowExtensionSuccess())
                .setResponseContext(responseContextMap)
                .build();
    }
    
    /**
     * Process a single operation and apply it to the appropriate context map.
     *
     * @param operation The operation to process.
     * @param propertiesUpdates Map to store property updates.
     * @param userClaimsUpdates Map to store user claim updates.
     * @param userInputsUpdates Map to store user input updates.
     * @return The result of the operation execution.
     */
    private OperationExecutionResult processOperation(PerformableOperation operation,
            Map<String, Object> propertiesUpdates,
            Map<String, Object> userClaimsUpdates,
            Map<String, Object> userInputsUpdates) {
        
        String path = operation.getPath();
        
        // Route to appropriate handler based on path prefix
        if (path.startsWith(PROPERTIES_PATH_PREFIX)) {
            return handlePropertyOperation(operation, propertiesUpdates);
        } else if (path.startsWith(USER_CLAIMS_PATH_PREFIX)) {
            return handleUserClaimOperation(operation, userClaimsUpdates);
        } else if (path.startsWith(USER_INPUTS_PATH_PREFIX)) {
            return handleUserInputOperation(operation, userInputsUpdates);
        }
        
        return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                "Unknown path prefix. Supported prefixes: " + PROPERTIES_PATH_PREFIX + 
                ", " + USER_CLAIMS_PATH_PREFIX + ", " + USER_INPUTS_PATH_PREFIX);
    }
    
    /**
     * Handle operations on flow properties.
     *
     * @param operation The operation to perform.
     * @param propertiesUpdates Map to store property updates.
     * @return The result of the operation execution.
     */
    private OperationExecutionResult handlePropertyOperation(PerformableOperation operation,
            Map<String, Object> propertiesUpdates) {
        
        String propertyName = extractNameFromPath(operation.getPath(), PROPERTIES_PATH_PREFIX);
        
        if (propertyName == null || propertyName.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid property path. Property name is required.");
        }
        
        switch (operation.getOp()) {
            case ADD:
            case REPLACE:
                if (operation.getValue() == null) {
                    return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                            "Value is required for " + operation.getOp() + " operation.");
                }
                // For ADD and REPLACE, store the value with operation type for later processing
                propertiesUpdates.put(propertyName, new OperationValue(operation.getOp().name(), 
                        operation.getValue()));
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                        "Property " + operation.getOp().name().toLowerCase(Locale.ENGLISH) + " operation queued.");
                
            case REMOVE:
                // Mark property for removal
                propertiesUpdates.put(propertyName, new OperationValue("REMOVE", null));
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                        "Property remove operation queued.");
                
            default:
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                        "Unsupported operation: " + operation.getOp());
        }
    }
    
    /**
     * Handle operations on user claims.
     *
     * @param operation The operation to perform.
     * @param userClaimsUpdates Map to store user claim updates.
     * @return The result of the operation execution.
     */
    private OperationExecutionResult handleUserClaimOperation(PerformableOperation operation,
            Map<String, Object> userClaimsUpdates) {
        
        String claimUri = extractNameFromPath(operation.getPath(), USER_CLAIMS_PATH_PREFIX);
        
        if (claimUri == null || claimUri.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid claim path. Claim URI is required.");
        }
        
        switch (operation.getOp()) {
            case ADD:
            case REPLACE:
                if (operation.getValue() == null) {
                    return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                            "Value is required for " + operation.getOp() + " operation.");
                }
                userClaimsUpdates.put(claimUri, new OperationValue(operation.getOp().name(), 
                        operation.getValue()));
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                        "User claim " + operation.getOp().name().toLowerCase(Locale.ENGLISH) + " operation queued.");
                
            case REMOVE:
                userClaimsUpdates.put(claimUri, new OperationValue("REMOVE", null));
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                        "User claim remove operation queued.");
                
            default:
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                        "Unsupported operation: " + operation.getOp());
        }
    }
    
    /**
     * Handle operations on user inputs.
     *
     * @param operation The operation to perform.
     * @param userInputsUpdates Map to store user input updates.
     * @return The result of the operation execution.
     */
    private OperationExecutionResult handleUserInputOperation(PerformableOperation operation,
            Map<String, Object> userInputsUpdates) {
        
        String inputName = extractNameFromPath(operation.getPath(), USER_INPUTS_PATH_PREFIX);
        
        if (inputName == null || inputName.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid user input path. Input name is required.");
        }
        
        switch (operation.getOp()) {
            case ADD:
            case REPLACE:
                if (operation.getValue() == null) {
                    return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                            "Value is required for " + operation.getOp() + " operation.");
                }
                userInputsUpdates.put(inputName, new OperationValue(operation.getOp().name(), 
                        operation.getValue()));
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                        "User input " + operation.getOp().name().toLowerCase(Locale.ENGLISH) + " operation queued.");
                
            case REMOVE:
                userInputsUpdates.put(inputName, new OperationValue("REMOVE", null));
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                        "User input remove operation queued.");
                
            default:
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                        "Unsupported operation: " + operation.getOp());
        }
    }
    
    /**
     * Extract the name/key from the operation path after the prefix.
     *
     * @param path The full operation path.
     * @param prefix The path prefix to remove.
     * @return The extracted name, or null if invalid.
     */
    private String extractNameFromPath(String path, String prefix) {
        
        if (path == null || !path.startsWith(prefix)) {
            return null;
        }
        
        String remaining = path.substring(prefix.length());
        
        // Handle trailing slash
        if (remaining.endsWith("/")) {
            remaining = remaining.substring(0, remaining.length() - 1);
        }
        
        // Handle array index notation (e.g., /properties/items/0)
        int lastSlash = remaining.lastIndexOf(PATH_SEPARATOR);
        if (lastSlash > 0) {
            String possibleIndex = remaining.substring(lastSlash + 1);
            if (LAST_ELEMENT_CHARACTER.equals(possibleIndex) || isNumeric(possibleIndex)) {
                // Return path up to the index
                return remaining.substring(0, lastSlash);
            }
        }
        
        return remaining;
    }
    
    /**
     * Check if a string is numeric.
     */
    private boolean isNumeric(String str) {
        
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Log operation execution results for diagnostics and debugging.
     *
     * @param operationExecutionResultList List of operation execution results.
     */
    private void logOperationExecutionResults(List<OperationExecutionResult> operationExecutionResultList) {
        
        if (operationExecutionResultList.isEmpty()) {
            return;
        }
        
        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            List<Map<String, String>> operationDetailsList = new ArrayList<>();
            operationExecutionResultList.forEach(result -> {
                Map<String, String> details = new HashMap<>();
                details.put("operation", result.getOperation().getOp() + " path: " + 
                        result.getOperation().getPath());
                details.put("status", result.getStatus().toString());
                details.put("message", result.getMessage());
                operationDetailsList.add(details);
            });
            
            DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                    ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                    ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_RESPONSE);
            diagLogBuilder
                    .inputParam("executedOperations", operationDetailsList)
                    .resultMessage("Processed operations for " + getSupportedActionType().getDisplayName() + 
                            " action.")
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                    .build();
            LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
        }
        
        if (LOG.isDebugEnabled()) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            try {
                String executionSummary = objectMapper.writeValueAsString(operationExecutionResultList);
                LOG.debug(String.format("Processed response for action type: %s. Results of operations: %s",
                        getSupportedActionType(), executionSummary));
            } catch (JsonProcessingException e) {
                LOG.debug("Error occurred while logging operation execution results.", e);
            }
        }
    }

    @Override
    public ActionExecutionStatus<Error> processErrorResponse(FlowContext flowContext,
            ActionExecutionResponseContext<ActionInvocationErrorResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        String errorMessage = responseContext.getActionInvocationResponse().getErrorMessage();
        String errorDescription = responseContext.getActionInvocationResponse().getErrorDescription();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing error response from In-Flow Extension. Error: " + errorMessage +
                    ", Description: " + errorDescription);
        }

        return new ErrorStatus(new Error(errorMessage, errorDescription));
    }

    @Override
    public ActionExecutionStatus<Failure> processFailureResponse(FlowContext flowContext,
            ActionExecutionResponseContext<ActionInvocationFailureResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        String failureReason = responseContext.getActionInvocationResponse().getFailureReason();
        String failureDescription = responseContext.getActionInvocationResponse().getFailureDescription();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing failure response from In-Flow Extension. Reason: " + failureReason +
                    ", Description: " + failureDescription);
        }

        return new FailedStatus(new Failure(failureReason, failureDescription));
    }

    /**
     * Inner class representing a successful In-Flow Extension execution result.
     */
    public static class InFlowExtensionSuccess implements Success {

        // This class can be extended to include additional success metadata if needed
    }
    
    /**
     * Inner class to wrap operation type and value for context updates.
     * This allows the executor to know what operation was performed on each field.
     */
    public static class OperationValue {
        
        private final String operation;
        private final Object value;
        
        public OperationValue(String operation, Object value) {
            this.operation = operation;
            this.value = value;
        }
        
        public String getOperation() {
            return operation;
        }
        
        public Object getValue() {
            return value;
        }
    }
}
