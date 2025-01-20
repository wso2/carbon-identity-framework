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

package org.wso2.carbon.identity.action.execution.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.execution.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.action.execution.ActionExecutorService;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionRuntimeException;
import org.wso2.carbon.identity.action.execution.internal.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationIncompleteResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.execution.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.model.Error;
import org.wso2.carbon.identity.action.execution.model.Failure;
import org.wso2.carbon.identity.action.execution.model.Incomplete;
import org.wso2.carbon.identity.action.execution.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.model.Request;
import org.wso2.carbon.identity.action.execution.model.Success;
import org.wso2.carbon.identity.action.execution.model.SuccessStatus;
import org.wso2.carbon.identity.action.execution.util.APIClient;
import org.wso2.carbon.identity.action.execution.util.ActionExecutionDiagnosticLogger;
import org.wso2.carbon.identity.action.execution.util.ActionExecutorConfig;
import org.wso2.carbon.identity.action.execution.util.AuthMethods;
import org.wso2.carbon.identity.action.execution.util.OperationComparator;
import org.wso2.carbon.identity.action.execution.util.RequestFilter;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.ThreadLocalAwareExecutors;
import org.wso2.carbon.identity.rule.evaluation.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.model.FlowContext;
import org.wso2.carbon.identity.rule.evaluation.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.model.RuleEvaluationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * This class is responsible for executing the action based on the action type and the event context.
 * It is responsible for building the request payload, calling the API, processing the response and
 * returning the status of the action execution.
 */
public class ActionExecutorServiceImpl implements ActionExecutorService {

    private static final Log LOG = LogFactory.getLog(ActionExecutorServiceImpl.class);

    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final ActionExecutorServiceImpl INSTANCE = new ActionExecutorServiceImpl();
    private static final ActionExecutionDiagnosticLogger DIAGNOSTIC_LOGGER = new ActionExecutionDiagnosticLogger();
    private final APIClient apiClient;
    private final ExecutorService executorService = ThreadLocalAwareExecutors.newFixedThreadPool(THREAD_POOL_SIZE);

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
    @Override
    public ActionExecutionStatus<?> execute(ActionType actionType, Map<String, Object> eventContext,
                                            String tenantDomain) throws ActionExecutionException {

        try {
            List<Action> actions = getActionsByActionType(actionType, tenantDomain);
            validateActions(actions, actionType);
            // As of now only one action is allowed.
            Action action = actions.get(0);
            return execute(action, eventContext, tenantDomain);
        } catch (ActionExecutionRuntimeException e) {
            LOG.debug("Skip executing actions for action type: " + actionType.name(), e);
            // Skip executing actions when no action available is considered as action execution being successful.
            return new SuccessStatus.Builder().setResponseContext(eventContext).build();
        }
    }

    /**
     * Resolve the action from given action id and execute it.
     *
     * @param actionType   Action Type.
     * @param actionId     The action id of the action that need to be executed.
     * @param eventContext The event context of the corresponding flow.
     * @param tenantDomain Tenant domain.
     * @return Action execution status.
     */
    @Override
    public ActionExecutionStatus<?> execute(ActionType actionType, String actionId,
                                            Map<String, Object> eventContext, String tenantDomain)
            throws ActionExecutionException {

        if (StringUtils.isBlank(actionId)) {
            throw new ActionExecutionException("Action Id cannot be blank.");
        }

        Action action = getActionByActionId(actionType, actionId, tenantDomain);
        try {
            return execute(action, eventContext, tenantDomain);
        } catch (ActionExecutionRuntimeException e) {
            LOG.debug("Skip executing action for action type: " + actionType.name(), e);
            // Skip executing actions when no action available is considered as action execution being successful.
            return new SuccessStatus.Builder().setResponseContext(eventContext).build();
        }
    }

    private ActionExecutionStatus<?> execute(Action action, Map<String, Object> eventContext, String tenantDomain)
            throws ActionExecutionException {

        if (action.getStatus() != Action.Status.ACTIVE) {
            // If no active actions are detected, it is regarded as the action execution being successful.
            return new SuccessStatus.Builder().setResponseContext(eventContext).build();
        }

        DIAGNOSTIC_LOGGER.logActionInitiation(action);

        if (!evaluateActionRule(action, eventContext, tenantDomain)) {
            // If the action rule is not satisfied, it is regarded as the action execution being successful.
            return new SuccessStatus.Builder().setResponseContext(eventContext).build();
        }

        DIAGNOSTIC_LOGGER.logActionExecution(action);

        ActionType actionType = ActionType.valueOf(action.getType().getActionType());
        ActionExecutionRequest actionRequest = buildActionExecutionRequest(actionType, eventContext);
        ActionExecutionResponseProcessor actionExecutionResponseProcessor = getResponseProcessor(actionType);

        return executeAction(action, actionRequest, eventContext, actionExecutionResponseProcessor);
    }

    private Action getActionByActionId(ActionType actionType, String actionId, String tenantDomain)
            throws ActionExecutionException {

        try {
            return ActionExecutionServiceComponentHolder.getInstance().getActionManagementService().getActionByActionId(
                    Action.ActionTypes.valueOf(actionType.name()).getPathParam(), actionId, tenantDomain);
        } catch (ActionMgtException e) {
            throw new ActionExecutionException("Error occurred while retrieving action by action Id.", e);
        }
    }

    private List<Action> getActionsByActionType(ActionType actionType, String tenantDomain) throws
            ActionExecutionException {

        try {
            return ActionExecutionServiceComponentHolder.getInstance().getActionManagementService()
                    .getActionsByActionType(Action.ActionTypes.valueOf(actionType.name()).getPathParam(), tenantDomain);
        } catch (ActionMgtException e) {
            throw new ActionExecutionException("Error occurred while retrieving actions.", e);
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
            throw new ActionExecutionException("Failed to build the request payload for action type: " + actionType, e);
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

    private ActionExecutionStatus<?> executeAction(Action action,
                                                   ActionExecutionRequest actionRequest,
                                                   Map<String, Object> eventContext,
                                                   ActionExecutionResponseProcessor actionExecutionResponseProcessor)
            throws ActionExecutionException {

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
            throw new ActionExecutionException("Error occurred while executing action: " + action.getId(), e);
        }
    }

    private boolean evaluateActionRule(Action action, Map<String, Object> eventContext, String tenantDomain)
            throws ActionExecutionException {

        if (action.getActionRule() == null || action.getActionRule().getId() == null) {
            logNoRuleConfiguredForAction(action);
            return true; // If no action rule available, consider the rule as satisfied and execute action.
        }

        try {
            RuleEvaluationResult ruleEvaluationResult =
                    ActionExecutionServiceComponentHolder.getInstance().getRuleEvaluationService()
                            .evaluate(action.getActionRule().getId(),
                                    new FlowContext(FlowType.valueOf(action.getType().getActionType()),
                                            eventContext), tenantDomain);

            logActionRuleEvaluation(action, ruleEvaluationResult);

            return ruleEvaluationResult.isRuleSatisfied();
        } catch (RuleEvaluationException e) {
            throw new ActionExecutionException(
                    "Error occurred while evaluating the rule for action: " + action.getId(), e);
        }
    }

    private ActionInvocationResponse executeActionAsynchronously(Action action,
                                                                 AuthMethods.AuthMethod authenticationMethod,
                                                                 String payload) throws ActionExecutionException {

        String apiEndpoint = action.getEndpoint().getUri();
        CompletableFuture<ActionInvocationResponse> actionExecutor = CompletableFuture.supplyAsync(
                () -> apiClient.callAPI(ActionType.valueOf(action.getType().getActionType()),
                        apiEndpoint, authenticationMethod, payload), executorService);
        try {
            return actionExecutor.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ActionExecutionException("Error occurred while executing action: " + action.getId(),
                    e);
        }
    }

    private void logActionRequest(Action action, String payload) {

        DIAGNOSTIC_LOGGER.logActionRequest(action);
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

    private ActionExecutionStatus<?> processActionResponse(Action action,
                                                           ActionInvocationResponse actionInvocationResponse,
                                                           Map<String, Object> eventContext,
                                                           ActionExecutionRequest actionRequest,
                                                           ActionExecutionResponseProcessor
                                                                   actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException, ActionExecutionException {

        if (actionInvocationResponse.isSuccess()) {
            return processSuccessResponse(action,
                    (ActionInvocationSuccessResponse) actionInvocationResponse.getResponse(),
                    eventContext, actionRequest, actionExecutionResponseProcessor);
        } else if (actionInvocationResponse.isIncomplete()) {
            return processIncompleteResponse(action,
                    (ActionInvocationIncompleteResponse) actionInvocationResponse.getResponse(),
                    eventContext, actionRequest, actionExecutionResponseProcessor);
        } else if (actionInvocationResponse.isFailure() && actionInvocationResponse.getResponse() != null) {
            return processFailureResponse(action, (ActionInvocationFailureResponse) actionInvocationResponse
                    .getResponse(), eventContext, actionRequest, actionExecutionResponseProcessor);
        } else if (actionInvocationResponse.isError() && actionInvocationResponse.getResponse() != null) {
            return processErrorResponse(action, (ActionInvocationErrorResponse) actionInvocationResponse.getResponse(),
                    eventContext, actionRequest, actionExecutionResponseProcessor);
        }
        logErrorResponse(action, actionInvocationResponse);
        throw new ActionExecutionException("Received an invalid or unexpected response for action type: "
                + action.getType() + " action ID: " + action.getId());
    }

    private ActionExecutionStatus<Success> processSuccessResponse(Action action,
                                                                  ActionInvocationSuccessResponse successResponse,
                                                                  Map<String, Object> eventContext,
                                                                  ActionExecutionRequest actionRequest,
                                                                  ActionExecutionResponseProcessor
                                                                 actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        logSuccessResponse(action, successResponse);

        List<PerformableOperation> allowedPerformableOperations =
                validatePerformableOperations(actionRequest, successResponse.getOperations(), action);
        ActionInvocationSuccessResponse.Builder successResponseBuilder =
                new ActionInvocationSuccessResponse.Builder().actionStatus(ActionInvocationResponse.Status.SUCCESS)
                        .operations(allowedPerformableOperations);
        return actionExecutionResponseProcessor.processSuccessResponse(eventContext,
                actionRequest.getEvent(), successResponseBuilder.build());
    }

    private ActionExecutionStatus<Incomplete> processIncompleteResponse(
                                                Action action,
                                                ActionInvocationIncompleteResponse incompleteResponse,
                                                Map<String, Object> eventContext,
                                                ActionExecutionRequest actionRequest,
                                                ActionExecutionResponseProcessor actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        logIncompleteResponse(action, incompleteResponse);

        List<PerformableOperation> allowedPerformableOperations =
                validatePerformableOperations(actionRequest, incompleteResponse.getOperations(), action);
        ActionInvocationIncompleteResponse.Builder incompleteResponseBuilder =
                new ActionInvocationIncompleteResponse.Builder()
                        .actionStatus(ActionInvocationResponse.Status.INCOMPLETE)
                        .operations(allowedPerformableOperations);
        return actionExecutionResponseProcessor.processIncompleteResponse(eventContext,
                actionRequest.getEvent(), incompleteResponseBuilder.build());
    }

    private ActionExecutionStatus<Error> processErrorResponse(Action action,
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

    private ActionExecutionStatus<Failure> processFailureResponse(Action action,
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

    private void logActionRuleEvaluation(Action action, RuleEvaluationResult ruleEvaluationResult) {

        DIAGNOSTIC_LOGGER.logActionRuleEvaluation(action, ruleEvaluationResult.isRuleSatisfied());
        LOG.debug("Rule of action: " + action.getId() + " evaluated to: " +
                ruleEvaluationResult.isRuleSatisfied());
    }

    private void logNoRuleConfiguredForAction(Action action) {

        DIAGNOSTIC_LOGGER.logNoRuleConfiguredForAction(action);
        LOG.debug("No rule configured for action " + action.getId() + ". Proceed executing the action.");
    }

    private void logSuccessResponse(Action action, ActionInvocationSuccessResponse successResponse) {

        DIAGNOSTIC_LOGGER.logSuccessResponse(action);
        if (LOG.isDebugEnabled()) {
            try {
                String responseBody = serializeSuccessResponse(successResponse);
                LOG.debug(String.format(
                        "Received success response from API: %s for action type: %s action id: %s with " +
                                "authentication: %s. Response: %s",
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
    }

    private void logIncompleteResponse(Action action, ActionInvocationIncompleteResponse incompleteResponse) {

        DIAGNOSTIC_LOGGER.logIncompleteResponse(action);
        if (LOG.isDebugEnabled()) {
            try {
                String responseBody = serializeIncompleteResponse(incompleteResponse);
                LOG.debug(String.format(
                        "Received incomplete response from API: %s for action type: %s action id: %s with " +
                                "authentication: %s. Response: %s",
                        action.getEndpoint().getUri(),
                        action.getType().getActionType(),
                        action.getId(),
                        action.getEndpoint().getAuthentication().getType(),
                        responseBody));
            } catch (JsonProcessingException e) {
                LOG.error("Error occurred while deserializing the incomplete response for action: " +
                        action.getId() + " for action type: " + action.getType().getActionType(), e);
            }
        }
    }

    private void logErrorResponse(Action action, ActionInvocationErrorResponse errorResponse) {

        DIAGNOSTIC_LOGGER.logErrorResponse(action);
        if (LOG.isDebugEnabled()) {
            try {
                String responseBody = serializeErrorResponse(errorResponse);
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

        DIAGNOSTIC_LOGGER.logFailureResponse(action);
        if (LOG.isDebugEnabled()) {
            try {
                String responseBody = serializeFailureResponse(failureResponse);
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

        DIAGNOSTIC_LOGGER.logErrorResponse(action, actionInvocationResponse);
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

    private String serializeIncompleteResponse(ActionInvocationIncompleteResponse response)
            throws JsonProcessingException {

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
            ActionExecutionRequest request, List<PerformableOperation> operations, Action action) {

        List<AllowedOperation> allowedOperations = request.getAllowedOperations();

        List<PerformableOperation> allowedPerformableOperations = operations.stream()
                .filter(performableOperation -> allowedOperations.stream()
                        .anyMatch(allowedOperation -> OperationComparator.compare(allowedOperation,
                                performableOperation)))
                .collect(Collectors.toList());

            if (LOG.isDebugEnabled() || LoggerUtils.isDiagnosticLogsEnabled()) {
                List<String> allowedOps = new ArrayList<>();
                List<String> notAllowedOps = new ArrayList<>();

                operations.forEach(operation -> {
                    String operationDetails = "Operation: " + operation.getOp() + " Path: " + operation.getPath();
                    if (allowedPerformableOperations.contains(operation)) {
                        allowedOps.add(operationDetails);
                    } else {
                        notAllowedOps.add(operationDetails);
                    }
                });
                DIAGNOSTIC_LOGGER.logPerformableOperations(action, allowedOps, notAllowedOps);
                if (LOG.isDebugEnabled()) {
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
