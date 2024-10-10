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
import org.apache.commons.collections.CollectionUtils;
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
import org.wso2.carbon.identity.action.execution.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.execution.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.model.Request;
import org.wso2.carbon.identity.action.execution.util.APIClient;
import org.wso2.carbon.identity.action.execution.util.ActionExecutionLogConstants;
import org.wso2.carbon.identity.action.execution.util.ActionExecutorConfig;
import org.wso2.carbon.identity.action.execution.util.AuthMethods;
import org.wso2.carbon.identity.action.execution.util.OperationComparator;
import org.wso2.carbon.identity.action.execution.util.RequestFilter;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.DiagnosticLog;

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

    private static final Log LOG = LogFactory.getLog(ActionExecutorServiceImpl.class);

    private static final ActionExecutorServiceImpl INSTANCE = new ActionExecutorServiceImpl();
    private final APIClient apiClient;

    private ActionExecutorServiceImpl() {

        apiClient = new APIClient();
    }

    public static ActionExecutorServiceImpl getInstance() {

        return INSTANCE;
    }

    @Override
    public boolean isExecutionEnabled(ActionType actionType) {

        return ActionExecutorConfig.getInstance().isExecutionForActionTypeEnabled(actionType);
    }

    /**
     * Resolve the actions that need to be executed for the given action types and execute them.
     *
     * @param actionType    Action Type.
     * @param eventContext  The event context of the corresponding flow.
     * @param tenantDomain  Tenant domain.
     * @return Action execution status.
     */
    public ActionExecutionStatus execute(ActionType actionType, Map<String, Object> eventContext, String tenantDomain)
            throws ActionExecutionException {

        try {
            List<Action> actions = getActionsByActionType(actionType, tenantDomain);
            validateActions(actions, actionType);
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                        ActionExecutionLogConstants.ACTION_EXECUTION,
                        ActionExecutionLogConstants.ActionIDs.EXECUTE_ACTION);
                diagLogBuilder
                        .resultMessage(actionType + " action execution is initiated.")
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                        .build();
                LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
            }
            // As of now only one action is allowed.
            Action action = actions.get(0);
            return execute(action, eventContext);
        } catch (ActionExecutionRuntimeException e) {
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                        ActionExecutionLogConstants.ACTION_EXECUTION,
                        ActionExecutionLogConstants.ActionIDs.EXECUTE_ACTION);
                diagLogBuilder
                        .resultMessage("Skip executing action for " + actionType + " type.")
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED)
                        .build();
                LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
            }
            LOG.debug("Skip executing actions for action type: " + actionType.name(), e);
            return new ActionExecutionStatus(ActionExecutionStatus.Status.FAILED, eventContext);
        }
    }

    /**
     * Resolve the actions by given the action id list and execute them.
     *
     * @param actionType    Action Type.
     * @param actionIdList     Lis of action Ids of the actions that need to be executed.
     * @param eventContext  The event context of the corresponding flow.
     * @param tenantDomain  Tenant domain.
     * @return Action execution status.
     */
    public ActionExecutionStatus execute(ActionType actionType, String[] actionIdList, Map<String, Object> eventContext,
                                         String tenantDomain) throws ActionExecutionException {

        validateActionIdList(actionType, actionIdList);
        Action action = getActionByActionId(actionType, actionIdList[0], tenantDomain);
        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                    ActionExecutionLogConstants.ACTION_EXECUTION,
                    ActionExecutionLogConstants.ActionIDs.EXECUTE_ACTION);
            diagLogBuilder
                    .resultMessage(actionType + " action execution is initiated.")
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                    .build();
            LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
        }
        try {
            return execute(action, eventContext);
        } catch (ActionExecutionRuntimeException e) {
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                        ActionExecutionLogConstants.ACTION_EXECUTION,
                        ActionExecutionLogConstants.ActionIDs.EXECUTE_ACTION);
                diagLogBuilder
                        .configParam("action id", action.getId())
                        .configParam("action name", action.getName())
                        .resultMessage("Skip executing action for " + actionType + " type.")
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED)
                        .build();
                LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
            }
            LOG.debug("Skip executing actions for action type: " + actionType.name(), e);
            return new ActionExecutionStatus(ActionExecutionStatus.Status.FAILED, eventContext);
        }
    }

    private void validateActionIdList(ActionType actionType, String[] actionIdList) throws ActionExecutionException {

        // As of now only one action is allowed.
        if (actionIdList == null || actionIdList.length == 0) {
            throw new ActionExecutionException("No action Ids found for action type: " + actionType.name());
        }
        if (actionIdList.length > 1) {
            throw new ActionExecutionException("Multiple actions found for action type: " + actionType.name() +
                    ". Current implementation doesn't support multiple actions for a single action type.");
        }
    }

    private ActionExecutionStatus execute(Action action, Map<String, Object> eventContext)
            throws ActionExecutionException {

        ActionType actionType = ActionType.valueOf(action.getType().getActionType());
        ActionExecutionRequest actionRequest = buildActionExecutionRequest(actionType, eventContext);
        ActionExecutionResponseProcessor actionExecutionResponseProcessor = getResponseProcessor(actionType);

        return Optional.ofNullable(action)
                .filter(activeAction -> activeAction.getStatus() == Action.Status.ACTIVE)
                .map(activeAction -> executeAction(activeAction, actionRequest, eventContext,
                        actionExecutionResponseProcessor))
                .orElse(new ActionExecutionStatus(ActionExecutionStatus.Status.FAILED, eventContext));
    }

    private Action getActionByActionId(ActionType actionType, String actionId, String tenantDomain)
            throws ActionExecutionException {

        try {
            return ActionExecutionServiceComponentHolder.getInstance().getActionManagementService().getActionByActionId(
                    Action.ActionTypes.valueOf(actionType.name()).getActionType(), actionId, tenantDomain);
        } catch (ActionMgtException e) {
            throw new ActionExecutionException("Error occurred while retrieving action by action Id.", e);
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

        if (CollectionUtils.isEmpty(actions)) {
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
            ActionExecutionRequest actionExecutionRequest = requestBuilder.buildActionExecutionRequest(eventContext);
            if (actionExecutionRequest.getEvent() == null || actionExecutionRequest.getEvent().getRequest() == null) {
                return actionExecutionRequest;
            }

            Request request = actionExecutionRequest.getEvent().getRequest();
            request.setAdditionalHeaders(
                    RequestFilter.getFilteredHeaders(request.getAdditionalHeaders(), actionType));
            request.setAdditionalParams(RequestFilter.getFilteredParams(request.getAdditionalParams(), actionType));
            return actionExecutionRequest;
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

    private ActionExecutionStatus executeAction(Action action,
                                                ActionExecutionRequest actionRequest,
                                                Map<String, Object> eventContext,
                                                ActionExecutionResponseProcessor actionExecutionResponseProcessor)
            throws ActionExecutionRuntimeException {

        Authentication endpointAuthentication = action.getEndpoint().getAuthentication();
        AuthMethods.AuthMethod authenticationMethod;

        try {
            authenticationMethod = getAuthenticationMethod(action.getId(), endpointAuthentication);
            String payload = serializeRequest(actionRequest);

            logActionRequest(action, payload);

            ActionInvocationResponse actionInvocationResponse =
                    executeActionAsynchronously(action, authenticationMethod, payload);
            return processActionResponse(action, actionInvocationResponse, eventContext, actionRequest,
                    actionExecutionResponseProcessor);
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

    private void logActionRequest(Action action, String payload) {

        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                    ActionExecutionLogConstants.ACTION_EXECUTION,
                    ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_REQUEST);
            diagLogBuilder
                    .configParam("action id", action.getId())
                    .configParam("action type", action.getType().getActionType())
                    .configParam("action endpoint", action.getEndpoint().getUri())
                    .configParam("action endpoint authentication type",
                            action.getEndpoint().getAuthentication().getType().getName())
                    .resultMessage("Call external service endpoint " + action.getEndpoint().getUri() + " for "
                            + action.getType().getActionType() + " action.")
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                    .build();
            LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format(
                    "Calling API: %s for action type: %s action id: %s with authentication: %s payload: %s",
                    action.getEndpoint().getUri(),
                    action.getType().getActionType(),
                    action.getId(),
                    action.getEndpoint().getAuthentication(),
                    payload));
        }
    }

    private ActionExecutionStatus processActionResponse(Action action,
                                                        ActionInvocationResponse actionInvocationResponse,
                                                        Map<String, Object> eventContext,
                                                        ActionExecutionRequest actionRequest,
                                                        ActionExecutionResponseProcessor
                                                                actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        if (actionInvocationResponse.isSuccess()) {
            return processSuccessResponse(action,
                    (ActionInvocationSuccessResponse) actionInvocationResponse.getResponse(),
                    eventContext, actionRequest, actionExecutionResponseProcessor);
        } else if (actionInvocationResponse.isFailure() && actionInvocationResponse.getResponse() != null) {
            return processFailureResponse(action, (ActionInvocationFailureResponse) actionInvocationResponse
                    .getResponse(), eventContext, actionRequest, actionExecutionResponseProcessor);
        } else if (actionInvocationResponse.isError() && actionInvocationResponse.getResponse() != null) {
            return processErrorResponse(action, (ActionInvocationErrorResponse) actionInvocationResponse.getResponse(),
                    eventContext, actionRequest, actionExecutionResponseProcessor);
        } else {
            logErrorResponse(action, actionInvocationResponse);
        }

        return new ActionExecutionStatus(ActionExecutionStatus.Status.FAILED, eventContext);
    }

    private ActionExecutionStatus processSuccessResponse(Action action,
                                                         ActionInvocationSuccessResponse successResponse,
                                                         Map<String, Object> eventContext,
                                                         ActionExecutionRequest actionRequest,
                                                         ActionExecutionResponseProcessor
                                                                 actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        if (LOG.isDebugEnabled()) {
            logSuccessResponse(action, successResponse);
        }

        List<PerformableOperation> allowedPerformableOperations =
                validatePerformableOperations(actionRequest, successResponse, action);
        ActionInvocationSuccessResponse.Builder successResponseBuilder =
                new ActionInvocationSuccessResponse.Builder().actionStatus(ActionInvocationResponse.Status.SUCCESS)
                        .operations(allowedPerformableOperations);
        return actionExecutionResponseProcessor.processSuccessResponse(eventContext,
                actionRequest.getEvent(), successResponseBuilder.build());
    }

    private ActionExecutionStatus processErrorResponse(Action action,
                                                       ActionInvocationErrorResponse errorResponse,
                                                       Map<String, Object> eventContext,
                                                       ActionExecutionRequest actionRequest,
                                                       ActionExecutionResponseProcessor
                                                               actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        logErrorResponse(action, errorResponse);
        return actionExecutionResponseProcessor.processErrorResponse(eventContext, actionRequest.getEvent(),
                errorResponse);
    }

    private ActionExecutionStatus processFailureResponse(Action action,
                                                       ActionInvocationFailureResponse failureResponse,
                                                       Map<String, Object> eventContext,
                                                       ActionExecutionRequest actionRequest,
                                                       ActionExecutionResponseProcessor
                                                               actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        logFailureResponse(action, failureResponse);
        return actionExecutionResponseProcessor.processFailureResponse(eventContext, actionRequest.getEvent(),
                failureResponse);
    }

    private void logSuccessResponse(Action action, ActionInvocationSuccessResponse successResponse) {

        try {
            String responseBody = serializeSuccessResponse(successResponse);
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                        ActionExecutionLogConstants.ACTION_EXECUTION,
                        ActionExecutionLogConstants.ActionIDs.RECEIVE_ACTION_RESPONSE);
                diagLogBuilder
                        .configParam("action id", action.getId())
                        .configParam("action type", action.getType().getActionType())
                        .configParam("action endpoint", action.getEndpoint().getUri())
                        .configParam("action endpoint authentication type",
                                action.getEndpoint().getAuthentication().getType().getName())
                        .resultMessage("Received success response from external endpoint " +
                                action.getEndpoint().getUri() + " for " + action.getType().getActionType() + " action.")
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                        .build();
                LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
            }
            LOG.debug(String.format(
                    "Received success response from API: %s for action type: %s action id: %s with authentication: %s. "
                            + "Response: %s",
                    action.getEndpoint().getUri(),
                    action.getType().getActionType(),
                    action.getId(),
                    action.getEndpoint().getAuthentication().getType(),
                    responseBody));
        } catch (JsonProcessingException e) {
            LOG.error("Error occurred while deserializing the success response for action: " +
                    action.getId() + " for action type: " + action.getType().getActionType(), e);
        }
    }

    private void logErrorResponse(Action action, ActionInvocationErrorResponse errorResponse) {

        if (LOG.isDebugEnabled()) {
            try {
                String responseBody = serializeErrorResponse(errorResponse);
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                            ActionExecutionLogConstants.ACTION_EXECUTION,
                            ActionExecutionLogConstants.ActionIDs.RECEIVE_ACTION_RESPONSE);
                    diagLogBuilder
                            .configParam("action id", action.getId())
                            .configParam("action type", action.getType().getActionType())
                            .configParam("action endpoint", action.getEndpoint().getUri())
                            .configParam("action endpoint authentication type",
                                    action.getEndpoint().getAuthentication().getType().getName())
                            .resultMessage("Received error response from external endpoint " +
                                    action.getEndpoint().getUri() + " for " + action.getType().getActionType() +
                                    " action.")
                            .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                            .resultStatus(DiagnosticLog.ResultStatus.FAILED)
                            .build();
                    LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
                }
                LOG.debug(String.format(
                        "Received error response from API: %s for action type: %s action id: %s with " +
                                "authentication: %s. Response: %s",
                        action.getEndpoint().getUri(),
                        action.getType().getActionType(),
                        action.getId(),
                        action.getEndpoint().getAuthentication().getType(),
                        responseBody));
            } catch (JsonProcessingException e) {
                LOG.debug("Error occurred while deserializing the error response for action: " +
                        action.getId() + " for action type: " + action.getType().getActionType(), e);
            }
        }
    }

    private void logFailureResponse(Action action, ActionInvocationFailureResponse failureResponse) {

        if (LOG.isDebugEnabled()) {
            try {
                String responseBody = serializeFailureResponse(failureResponse);
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                            ActionExecutionLogConstants.ACTION_EXECUTION,
                            ActionExecutionLogConstants.ActionIDs.RECEIVE_ACTION_RESPONSE);
                    diagLogBuilder
                            .configParam("action id", action.getId())
                            .configParam("action type", action.getType().getActionType())
                            .configParam("action endpoint", action.getEndpoint().getUri())
                            .configParam("action endpoint authentication type",
                                    action.getEndpoint().getAuthentication().getType().getName())
                            .resultMessage("Received failure response from external endpoint " +
                                    action.getEndpoint().getUri() + " for " + action.getType().getActionType() +
                                    " action.")
                            .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                            .resultStatus(DiagnosticLog.ResultStatus.FAILED)
                            .build();
                    LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
                }
                LOG.debug(String.format(
                        "Received failure response from API: %s for action type: %s action id: %s with " +
                                "authentication: %s. Response: %s",
                        action.getEndpoint().getUri(),
                        action.getType().getActionType(),
                        action.getId(),
                        action.getEndpoint().getAuthentication().getType(),
                        responseBody));
            } catch (JsonProcessingException e) {
                LOG.debug("Error occurred while deserializing the failure response for action: " +
                        action.getId() + " for action type: " + action.getType().getActionType(), e);
            }
        }
    }

    private void logErrorResponse(Action action, ActionInvocationResponse actionInvocationResponse) {
        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                    ActionExecutionLogConstants.ACTION_EXECUTION,
                    ActionExecutionLogConstants.ActionIDs.RECEIVE_ACTION_RESPONSE);
            diagLogBuilder
                    .configParam("action id", action.getId())
                    .configParam("action type", action.getType().getActionType())
                    .configParam("action endpoint", action.getEndpoint().getUri())
                    .configParam("action endpoint authentication type",
                            action.getEndpoint().getAuthentication().getType().getName())
                    .resultMessage("Failed to call external endpoint for " + action.getType().getActionType()
                            + " action. " +
                            (actionInvocationResponse.getErrorLog() != null ? actionInvocationResponse.getErrorLog() :
                                    "Unknown error occured."))
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.FAILED)
                    .build();
            LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format(
                    "Failed to call API: %s for action type: %s action id: %s with authentication: %s. Error: %s",
                    action.getEndpoint().getUri(),
                    action.getType().getActionType(),
                    action.getId(),
                    action.getEndpoint().getAuthentication(),
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

    private String serializeFailureResponse(ActionInvocationFailureResponse response) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(response);
    }

    private List<PerformableOperation> validatePerformableOperations(
            ActionExecutionRequest request, ActionInvocationSuccessResponse response, Action action) {

        List<AllowedOperation> allowedOperations = request.getAllowedOperations();

        List<PerformableOperation> allowedPerformableOperations = response.getOperations().stream()
                .filter(performableOperation -> allowedOperations.stream()
                        .anyMatch(allowedOperation -> OperationComparator.compare(allowedOperation,
                                performableOperation)))
                .collect(Collectors.toList());

            if (LOG.isDebugEnabled() || LoggerUtils.isDiagnosticLogsEnabled()) {
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
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                            ActionExecutionLogConstants.ACTION_EXECUTION,
                            ActionExecutionLogConstants.ActionIDs.VALIDATE_ACTION_OPERATIONS);
                    diagLogBuilder
                            .configParam("action id", action.getId())
                            .configParam("action type", action.getType().getActionType())
                            .configParam("action endpoint", action.getEndpoint().getUri())
                            .configParam("action endpoint authentication type",
                                    action.getEndpoint().getAuthentication().getType().getName())
                            .configParam("allowed operations", allowedOps.isEmpty() ? "empty" : allowedOps)
                            .configParam("not allowed operations", notAllowedOps.isEmpty() ? "empty" : notAllowedOps)
                            .resultMessage(
                                    "Validated operations to perform on " + action.getType().getActionType()
                                            + " action.")
                            .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                            .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                            .build();
                    LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
                }
                if (LOG.isDebugEnabled()) {
                    // todo: add to diagnostics
                    LOG.debug("Allowed Operations: " + String.join(", ", allowedOps) +
                            ". Not Allowed Operations: " + String.join(", ", notAllowedOps));
                }
            }

        return allowedPerformableOperations;
    }

    private AuthMethods.AuthMethod getAuthenticationMethod(String actionId, Authentication authentication)
            throws ActionMgtException {

        List<AuthProperty> authProperties = authentication.getPropertiesWithDecryptedValues(actionId);

        switch (authentication.getType()) {
            case BASIC:
                return new AuthMethods.BasicAuth(authProperties);
            case BEARER:
                return new AuthMethods.BearerAuth(authProperties);
            case API_KEY:
                return new AuthMethods.APIKeyAuth(authProperties);
            case NONE:
                return null;
            default:
                throw new ActionMgtException("Unsupported authentication type: " + authentication.getType());

        }
    }
}
