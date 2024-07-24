/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionRuntimeException;
import org.wso2.carbon.identity.action.execution.internal.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.execution.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.util.APIClient;
import org.wso2.carbon.identity.action.execution.util.AuthMethods;
import org.wso2.carbon.identity.action.execution.util.OperationComparator;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.AuthType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * This class is responsible for executing the action based on the action type and the event context.
 * It is responsible for building the request payload, calling the API, processing the response and
 * returning the status of the action execution.
 */
public class ActionExecutorServiceImpl implements ActionExecutorService {

    private static final Log log = LogFactory.getLog(ActionExecutorServiceImpl.class);

    private static final ActionExecutorServiceImpl instance = new ActionExecutorServiceImpl();
    private final APIClient apiClient;

    private ActionExecutorServiceImpl() {

        apiClient = new APIClient();
    }

    public static ActionExecutorServiceImpl getInstance() {

        return instance;
    }

    public ActionExecutionStatus execute(ActionType actionType, Map<String, Object> eventContext, String tenantDomain)
            throws ActionExecutionException {

        try {
            List<Action> actions = getActionsByActionType(actionType, tenantDomain);
            validateActions(actions, actionType);
            ActionExecutionRequest actionRequest = buildActionExecutionRequest(actionType, eventContext);
            ActionExecutionResponseProcessor actionExecutionResponseProcessor = getResponseProcessor(actionType);
            return executeAction(actions.get(0), actionRequest, actionType, eventContext,
                    actionExecutionResponseProcessor);
        } catch (ActionExecutionRuntimeException e) {
            // todo: add to diagnostics
            log.error("Skip executing actions for action type: " + actionType.name() + ". Error: " + e.getMessage(), e);
            return new ActionExecutionStatus(ActionExecutionStatus.Status.FAILURE, eventContext);

        }
    }

    private List<Action> getActionsByActionType(ActionType actionType, String tenantDomain) throws
            ActionExecutionRuntimeException {

        try {
            return ActionExecutionServiceComponentHolder.getInstance().getActionManagementService()
                    .getActionsByActionType(Action.ActionTypes.valueOf(actionType.name()).getPathParam(), tenantDomain);
        } catch (ActionMgtException e) {
            throw new ActionExecutionRuntimeException("Error occurred while retrieving actions.", e);
        }
    }

    private void validateActions(List<Action> actions, ActionType actionType) throws ActionExecutionException {

        if (actions.isEmpty()) {
            throw new ActionExecutionRuntimeException("No actions found for action type: " + actionType);
        }
        if (actions.size() > 1) {
            // when multiple actions are supported for an action type the logic below needs to be improved such that,
            // a successful processing from one action becomes the input to the successor.
            throw new ActionExecutionException("Multiple actions found for action type: " + actionType.name() +
                    ". Current implementation doesn't support multiple actions for a single action type.");
        }
    }

    private ActionExecutionRequest buildActionExecutionRequest(ActionType actionType, Map<String, Object> eventContext)
            throws ActionExecutionException {

        ActionExecutionRequestBuilder requestBuilder =
                ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(actionType);
        if (requestBuilder == null) {
            throw new ActionExecutionException("No request builder found for action type: " + actionType);
        }
        try {
            return requestBuilder.buildActionExecutionRequest(actionType, eventContext);
        } catch (ActionExecutionRequestBuilderException e) {
            throw new ActionExecutionRuntimeException("Error occurred while building the request payload.", e);
        }
    }

    private ActionExecutionResponseProcessor getResponseProcessor(ActionType actionType)
            throws ActionExecutionException {

        ActionExecutionResponseProcessor responseProcessor =
                ActionExecutionResponseProcessorFactory.getActionExecutionResponseProcessor(actionType);
        if (responseProcessor == null) {
            throw new ActionExecutionException("No response processor found for action type: " + actionType);
        }
        return responseProcessor;
    }

    private ActionExecutionStatus executeAction(Action action, ActionExecutionRequest actionRequest,
                                                ActionType actionType,
                                                Map<String, Object> eventContext,
                                                ActionExecutionResponseProcessor actionExecutionResponseProcessor)
            throws ActionExecutionRuntimeException {

        String apiEndpoint = action.getEndpoint().getUri();
        AuthType endpointAuthentication = action.getEndpoint().getAuthentication();
        AuthMethods.AuthMethod authenticationMethod;

        try {
            authenticationMethod = getAuthenticationMethod(action.getId(), endpointAuthentication);
            String payload = serializeRequest(actionRequest);

            logActionRequest(apiEndpoint, actionType, action.getId(), authenticationMethod, payload);

            ActionInvocationResponse actionInvocationResponse =
                    executeActionAsynchronously(action, authenticationMethod, payload);
            return processActionResponse(actionInvocationResponse, actionType, eventContext, actionRequest,
                    action.getId(), apiEndpoint, authenticationMethod, actionExecutionResponseProcessor);
        } catch (ActionMgtException | JsonProcessingException | ActionExecutionResponseProcessorException e) {
            throw new ActionExecutionRuntimeException("Error occurred while executing action: " + action.getId(), e);
        }
    }

    private ActionInvocationResponse executeActionAsynchronously(Action action,
                                                                 AuthMethods.AuthMethod authenticationMethod,
                                                                 String payload) {

        String apiEndpoint = action.getEndpoint().getUri();
        CompletableFuture<ActionInvocationResponse> actionExecutor = CompletableFuture.supplyAsync(
                () -> apiClient.callAPI(apiEndpoint, authenticationMethod, payload));
        try {
            return actionExecutor.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ActionExecutionRuntimeException("Error occurred while executing action: " + action.getId(),
                    e);
        }
    }

    private void logActionRequest(String apiEndpoint, ActionType actionType, String actionId,
                                  AuthMethods.AuthMethod authenticationMethod, String payload) {

        //todo: Add to diagnostics
        if (log.isDebugEnabled()) {
            log.debug(String.format(
                    "Calling API: %s for action type: %s action id: %s with authentication: %s payload: %s",
                    apiEndpoint,
                    actionType,
                    actionId,
                    Optional.ofNullable(authenticationMethod).map(AuthMethods.AuthMethod::getAuthType)
                            .orElse("NONE"),
                    payload));
        }
    }

    private ActionExecutionStatus processActionResponse(ActionInvocationResponse actionInvocationResponse,
                                                        ActionType actionType,
                                                        Map<String, Object> eventContext,
                                                        ActionExecutionRequest actionRequest,
                                                        String actionId, String apiEndpoint,
                                                        AuthMethods.AuthMethod authenticationMethod,
                                                        ActionExecutionResponseProcessor
                                                                actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        if (actionInvocationResponse.isSuccess()) {
            return processSuccessResponse((ActionInvocationSuccessResponse) actionInvocationResponse.getResponse(),
                    actionType,
                    eventContext, actionRequest, actionId, apiEndpoint, authenticationMethod,
                    actionExecutionResponseProcessor);
        } else if (actionInvocationResponse.isError() && actionInvocationResponse.getResponse() != null) {
            return processErrorResponse((ActionInvocationErrorResponse) actionInvocationResponse.getResponse(),
                    actionType,
                    eventContext, actionRequest, actionId, apiEndpoint, authenticationMethod,
                    actionExecutionResponseProcessor);
        } else {
            logErrorResponse(actionInvocationResponse, actionType, actionId, apiEndpoint, authenticationMethod);
        }

        return new ActionExecutionStatus(ActionExecutionStatus.Status.FAILURE, eventContext);
    }

    private ActionExecutionStatus processSuccessResponse(ActionInvocationSuccessResponse successResponse,
                                                         ActionType actionType,
                                                         Map<String, Object> eventContext,
                                                         ActionExecutionRequest actionRequest,
                                                         String actionId, String apiEndpoint,
                                                         AuthMethods.AuthMethod authenticationMethod,
                                                         ActionExecutionResponseProcessor
                                                                 actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        if (log.isDebugEnabled()) {
            // todo: add to diagnostic logs
            logSuccessResponse(successResponse, actionType, actionId, apiEndpoint, authenticationMethod);
        }

        List<PerformableOperation> allowedPerformableOperations =
                validatePerformableOperations(actionRequest, successResponse);
        ActionInvocationSuccessResponse.Builder successResponseBuilder =
                new ActionInvocationSuccessResponse.Builder().operations(allowedPerformableOperations);
        return actionExecutionResponseProcessor.processSuccessResponse(actionType, eventContext,
                actionRequest.getEvent(),
                successResponseBuilder.build());
    }

    private ActionExecutionStatus processErrorResponse(ActionInvocationErrorResponse errorResponse,
                                                       ActionType actionType,
                                                       Map<String, Object> eventContext,
                                                       ActionExecutionRequest actionRequest,
                                                       String actionId, String apiEndpoint,
                                                       AuthMethods.AuthMethod authenticationMethod,
                                                       ActionExecutionResponseProcessor
                                                               actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        if (log.isDebugEnabled()) {
            // todo: add to diagnostic logs
            logErrorResponse(errorResponse, actionType, actionId, apiEndpoint, authenticationMethod);
        }

        return actionExecutionResponseProcessor.processErrorResponse(actionType, eventContext, actionRequest.getEvent(),
                errorResponse);
    }

    private void logSuccessResponse(ActionInvocationSuccessResponse successResponse, ActionType actionType,
                                    String actionId, String apiEndpoint, AuthMethods.AuthMethod authenticationMethod) {

        try {
            String responseBody = serializeSuccessResponse(successResponse);
            log.debug(String.format(
                    "Received success response from API: %s for action type: %s action id: %s with authentication: %s. "
                            + "Response: %s",
                    apiEndpoint,
                    actionType,
                    actionId,
                    Optional.ofNullable(authenticationMethod).map(AuthMethods.AuthMethod::getAuthType)
                            .orElse("NONE"),
                    responseBody));
        } catch (JsonProcessingException e) {
            log.error("Error occurred while deserializing the success response for action: " +
                    actionId + " for action type: " + actionType, e);
        }
    }

    private void logErrorResponse(ActionInvocationErrorResponse errorResponse, ActionType actionType,
                                  String actionId, String apiEndpoint, AuthMethods.AuthMethod authenticationMethod) {

        try {
            String responseBody = serializeErrorResponse(errorResponse);
            log.debug(String.format(
                    "Received error response from API: %s for action type: %s action id: %s with authentication: %s. " +
                            "Response: %s",
                    apiEndpoint,
                    actionType,
                    actionId,
                    Optional.ofNullable(authenticationMethod).map(AuthMethods.AuthMethod::getAuthType)
                            .orElse("NONE"),
                    responseBody));
        } catch (JsonProcessingException e) {
            log.error("Error occurred while deserializing the error response for action: " +
                    actionId + " for action type: " + actionType, e);
        }
    }

    private void logErrorResponse(ActionInvocationResponse actionInvocationResponse, ActionType actionType,
                                  String actionId, String apiEndpoint, AuthMethods.AuthMethod authenticationMethod) {
        // todo: add to diagnostic logs
        if (log.isDebugEnabled()) {
            log.debug(String.format(
                    "Failed to call API: %s for action type: %s action id: %s with authentication: %s. Error: %s",
                    apiEndpoint,
                    actionType,
                    actionId,
                    Optional.ofNullable(authenticationMethod).map(AuthMethods.AuthMethod::getAuthType)
                            .orElse("NONE"),
                    actionInvocationResponse.getErrorLog() != null ? actionInvocationResponse.getErrorLog() :
                            "Unknown"));
        }
    }

    private String serializeRequest(ActionExecutionRequest request) throws JsonProcessingException {

        ObjectMapper requestObjectmapper = new ObjectMapper();
        requestObjectmapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        requestObjectmapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return requestObjectmapper.writeValueAsString(request);
    }

    private String serializeSuccessResponse(ActionInvocationSuccessResponse response) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(response);
    }

    private String serializeErrorResponse(ActionInvocationErrorResponse response) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(response);
    }

    private List<PerformableOperation> validatePerformableOperations(ActionExecutionRequest request,
                                                                     ActionInvocationSuccessResponse response) {

        List<AllowedOperation> allowedOperations = request.getAllowedOperations();

        List<PerformableOperation> allowedPerformableOperations = response.getOperations().stream()
                .filter(performableOperation -> allowedOperations.stream()
                        .anyMatch(allowedOperation -> OperationComparator.compare(allowedOperation,
                                performableOperation)))
                .collect(Collectors.toList());

        if (log.isDebugEnabled()) {
            // todo: add to diagnostics
            List<String> allowedOps = new ArrayList<>();
            List<String> notAllowedOps = new ArrayList<>();

            response.getOperations().forEach(operation -> {
                String operationDetails = "Operation: " + operation.getOp() + " with path: " + operation.getPath();
                if (allowedPerformableOperations.contains(operation)) {
                    allowedOps.add(operationDetails);
                } else {
                    notAllowedOps.add(operationDetails);
                }
            });

            String logMessage = "Allowed Operations: " + String.join(", ", allowedOps) +
                    ". Not Allowed Operations: " + String.join(", ", notAllowedOps);
            log.debug(logMessage);
        }

        return allowedPerformableOperations;
    }

    private AuthMethods.AuthMethod getAuthenticationMethod(String actionId, AuthType authType)
            throws ActionMgtException {

        List<AuthProperty> authProperties = authType.getPropertiesWithDecryptedValues(actionId);

        switch (authType.getType()) {
            case BASIC:
                return new AuthMethods.BasicAuth(authProperties);
            case BEARER:
                return new AuthMethods.BearerAuth(authProperties);
            case API_KEY:
                return new AuthMethods.APIKeyAuth(authProperties);
            case NONE:
                return null;
            default:
                throw new ActionMgtException("Unsupported authentication type: " + authType.getType());

        }
    }
}
