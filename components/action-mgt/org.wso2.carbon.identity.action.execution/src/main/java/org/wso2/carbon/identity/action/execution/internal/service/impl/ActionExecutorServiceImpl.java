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

package org.wso2.carbon.identity.action.execution.internal.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRuntimeException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionResponseContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationIncompleteResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Incomplete;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.api.model.Request;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.action.execution.api.model.SuccessStatus;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.action.execution.api.service.ActionVersioningHandler;
import org.wso2.carbon.identity.action.execution.internal.component.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.action.execution.internal.util.APIClient;
import org.wso2.carbon.identity.action.execution.internal.util.ActionExecutionDiagnosticLogger;
import org.wso2.carbon.identity.action.execution.internal.util.ActionExecutorConfig;
import org.wso2.carbon.identity.action.execution.internal.util.AuthMethods;
import org.wso2.carbon.identity.action.execution.internal.util.OperationComparator;
import org.wso2.carbon.identity.action.execution.internal.util.RequestFilter;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.ThreadLocalAwareExecutors;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.FlowType;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;

import java.util.ArrayList;
import java.util.HashMap;
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
    private static final String API_VERSION_HEADER = "x-wso2-api-version";
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

    @Override
    public ActionExecutionStatus execute(ActionType actionType,
                                         FlowContext flowContext,
                                         String tenantDomain) throws ActionExecutionException {

        try {
            List<Action> actions = getActionsByActionType(actionType, tenantDomain);
            validateActions(actions, actionType);
            // As of now only one action is allowed.
            Action action = actions.get(0);

            return execute(action, flowContext, tenantDomain);
        } catch (ActionExecutionRuntimeException e) {
            LOG.debug("Skip executing actions for action type: " + actionType.name(), e);
            // Skip executing actions when no action available is considered as action execution being successful.
            return new SuccessStatus.Builder().setResponseContext(flowContext.getContextData()).build();
        }
    }

    @Override
    public ActionExecutionStatus execute(ActionType actionType, String actionId,
                                         FlowContext flowContext,
                                         String tenantDomain) throws ActionExecutionException {

        if (StringUtils.isBlank(actionId)) {
            throw new ActionExecutionException("Action Id cannot be blank.");
        }

        try {
            Action action = getActionByActionId(actionType, actionId, tenantDomain);
            return execute(action, flowContext, tenantDomain);
        } catch (ActionExecutionRuntimeException e) {
            LOG.debug("Skip executing action for action type: " + actionType.name(), e);
            // Skip executing actions when no action available is considered as action execution being successful.
            return new SuccessStatus.Builder().setResponseContext(flowContext.getContextData()).build();
        }
    }

    private ActionExecutionStatus<?> execute(Action action, FlowContext flowContext, String tenantDomain)
            throws ActionExecutionException {

        if (action.getStatus() != Action.Status.ACTIVE) {
            // If no active actions are detected, it is regarded as the action execution being successful.
            return new SuccessStatus.Builder().setResponseContext(flowContext.getContextData()).build();
        }

        ActionExecutionRequestContext actionExecutionRequestContext = ActionExecutionRequestContext.create(action);
        ActionType actionType = ActionType.valueOf(action.getType().getActionType());
        if (!isEligibleActionVersionToTrigger(actionExecutionRequestContext, flowContext)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("This action version is not eligible to be triggered for the action: %s. " +
                        "Skipping action execution.", action.getId()));
            }
            // If the action version is not satisfied, it is regarded as the action execution being successful.
            return new SuccessStatus.Builder().setResponseContext(flowContext.getContextData()).build();
        }

        DIAGNOSTIC_LOGGER.logActionInitiation(action);

        if (!evaluateActionRule(action, flowContext, tenantDomain)) {
            // If the action rule is not satisfied, it is regarded as the action execution being successful.
            return new SuccessStatus.Builder().setResponseContext(flowContext.getContextData()).build();
        }

        DIAGNOSTIC_LOGGER.logActionExecution(action);

        ActionExecutionRequest actionRequest = buildActionExecutionRequest(
                actionType, action, flowContext, actionExecutionRequestContext);
        ActionExecutionResponseProcessor actionExecutionResponseProcessor = getResponseProcessor(actionType);

        return executeAction(action, actionRequest, flowContext, actionExecutionResponseProcessor);
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

    private ActionExecutionRequest buildActionExecutionRequest(ActionType actionType, Action action,
            FlowContext flowContext, ActionExecutionRequestContext actionExecutionRequestContext)
            throws ActionExecutionException {

        ActionExecutionRequestBuilder requestBuilder =
                ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(actionType);
        if (requestBuilder == null) {
            throw new ActionExecutionException("No request builder found for action type: " + actionType);
        }
        try {
            ActionExecutionRequest actionExecutionRequest =
                    requestBuilder.buildActionExecutionRequest(flowContext, actionExecutionRequestContext);
            if (actionExecutionRequest.getEvent() == null || actionExecutionRequest.getEvent().getRequest() == null) {
                return actionExecutionRequest;
            }

            Request request = actionExecutionRequest.getEvent().getRequest();
            request.setAdditionalHeaders(RequestFilter.getFilteredHeaders(
                    request.getAdditionalHeaders(),
                    action.getEndpoint().getAllowedHeaders(),
                    actionType));
            request.setAdditionalParams(RequestFilter.getFilteredParams(
                    request.getAdditionalParams(),
                    action.getEndpoint().getAllowedParameters(),
                    actionType));
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
                                                   FlowContext flowContext,
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
            return processActionResponse(action, actionInvocationResponse, flowContext, actionRequest,
                    actionExecutionResponseProcessor);
        } catch (ActionMgtException | JsonProcessingException | ActionExecutionResponseProcessorException e) {
            throw new ActionExecutionException("Error occurred while executing action: " + action.getId(), e);
        }
    }

    private boolean evaluateActionRule(Action action, FlowContext flowContext, String tenantDomain)
            throws ActionExecutionException {

        if (action.getActionRule() == null || action.getActionRule().getId() == null) {
            logNoRuleConfiguredForAction(action);
            return true; // If no action rule available, consider the rule as satisfied and execute action.
        }

        try {
            RuleEvaluationResult ruleEvaluationResult =
                    ActionExecutionServiceComponentHolder.getInstance().getRuleEvaluationService()
                            .evaluate(action.getActionRule().getId(),
                                    new org.wso2.carbon.identity.rule.evaluation.api.model.FlowContext(
                                            FlowType.valueOf(action.getType().getActionType()),
                                            flowContext.getContextData()), tenantDomain);

            logActionRuleEvaluation(action, ruleEvaluationResult);

            return ruleEvaluationResult.isRuleSatisfied();
        } catch (RuleEvaluationException e) {
            throw new ActionExecutionException(
                    "Error occurred while evaluating the rule for action: " + action.getId(), e);
        }
    }

    private boolean isEligibleActionVersionToTrigger(ActionExecutionRequestContext actionExecutionRequestContext,
                                                     FlowContext flowContext)
            throws ActionExecutionException {

        ActionType actionType = actionExecutionRequestContext.getActionType();
        Action action = actionExecutionRequestContext.getAction();
        ActionVersioningHandler versioningHandler =
                ActionVersioningHandlerFactory.getActionVersioningHandler(actionType);
        if (versioningHandler == null) {
            throw new ActionExecutionException(
                    String.format("No action version handler found for action type: %s", actionType));
        }

        // If the action version is retired, throw an expection.
        if (versioningHandler.isRetiredActionVersion(actionType, action)) {
            throw new ActionExecutionException(
                    String.format("Action version is retired for action: %s", action.getId()));
        }

        try {
            return versioningHandler.canExecute(actionExecutionRequestContext, flowContext);
        } catch (ActionExecutionException e) {
            throw new ActionExecutionException("Error occurred when trying to validate whether action version is " +
                    "eligible to be triggered for action type: " + actionType, e);
        }
    }

    private ActionInvocationResponse executeActionAsynchronously(Action action,
                                                                 AuthMethods.AuthMethod authenticationMethod,
                                                                 String payload) throws ActionExecutionException {

        String apiEndpoint = action.getEndpoint().getUri();
        Map<String, String> headers = new HashMap<>();
        headers.put(API_VERSION_HEADER, action.getActionVersion());
        CompletableFuture<ActionInvocationResponse> actionExecutor = CompletableFuture.supplyAsync(
                () -> apiClient.callAPI(ActionType.valueOf(action.getType().getActionType()),
                        apiEndpoint, authenticationMethod, headers, payload), executorService);
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
                                                           FlowContext flowContext,
                                                           ActionExecutionRequest actionRequest,
                                                           ActionExecutionResponseProcessor
                                                                   actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException, ActionExecutionException {

        if (actionInvocationResponse.isSuccess()) {
            return processSuccessResponse(action,
                    (ActionInvocationSuccessResponse) actionInvocationResponse.getResponse(),
                    flowContext, actionRequest, actionExecutionResponseProcessor);
        } else if (actionInvocationResponse.isIncomplete()) {
            return processIncompleteResponse(action,
                    (ActionInvocationIncompleteResponse) actionInvocationResponse.getResponse(),
                    flowContext, actionRequest, actionExecutionResponseProcessor);
        } else if (actionInvocationResponse.isFailure() && actionInvocationResponse.getResponse() != null) {
            return processFailureResponse(action, (ActionInvocationFailureResponse) actionInvocationResponse
                    .getResponse(), flowContext, actionRequest, actionExecutionResponseProcessor);
        } else if (actionInvocationResponse.isError() && actionInvocationResponse.getResponse() != null) {
            return processErrorResponse(action, (ActionInvocationErrorResponse) actionInvocationResponse.getResponse(),
                    flowContext, actionRequest, actionExecutionResponseProcessor);
        }
        logErrorResponse(action, actionInvocationResponse);
        throw new ActionExecutionException("Received an invalid or unexpected response for action type: "
                + action.getType() + " action ID: " + action.getId());
    }

    private ActionExecutionStatus<Success> processSuccessResponse(Action action,
                                                                  ActionInvocationSuccessResponse successResponse,
                                                                  FlowContext flowContext,
                                                                  ActionExecutionRequest actionRequest,
                                                                  ActionExecutionResponseProcessor
                                                                          actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        logSuccessResponse(action, successResponse);

        List<PerformableOperation> allowedPerformableOperations =
                validatePerformableOperations(actionRequest, successResponse.getOperations(), action);
        ActionInvocationSuccessResponse.Builder successResponseBuilder =
                new ActionInvocationSuccessResponse.Builder().actionStatus(ActionInvocationResponse.Status.SUCCESS)
                        .operations(allowedPerformableOperations)
                        .responseData(successResponse.getData());
        return actionExecutionResponseProcessor.processSuccessResponse(flowContext,
                ActionExecutionResponseContext.create(actionRequest.getEvent(), successResponseBuilder.build()));
    }

    private ActionExecutionStatus<Incomplete> processIncompleteResponse(
            Action action,
            ActionInvocationIncompleteResponse incompleteResponse,
            FlowContext flowContext,
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
        return actionExecutionResponseProcessor.processIncompleteResponse(flowContext,
                ActionExecutionResponseContext.create(actionRequest.getEvent(), incompleteResponseBuilder.build()));
    }

    private ActionExecutionStatus<Error> processErrorResponse(Action action,
                                                              ActionInvocationErrorResponse errorResponse,
                                                              FlowContext flowContext,
                                                              ActionExecutionRequest actionRequest,
                                                              ActionExecutionResponseProcessor
                                                                      actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        logErrorResponse(action, errorResponse);
        return actionExecutionResponseProcessor.processErrorResponse(flowContext,
                ActionExecutionResponseContext.create(actionRequest.getEvent(), errorResponse));
    }

    private ActionExecutionStatus<Failure> processFailureResponse(Action action,
                                                                  ActionInvocationFailureResponse failureResponse,
                                                                  FlowContext flowContext,
                                                                  ActionExecutionRequest actionRequest,
                                                                  ActionExecutionResponseProcessor
                                                                          actionExecutionResponseProcessor)
            throws ActionExecutionResponseProcessorException {

        logFailureResponse(action, failureResponse);
        return actionExecutionResponseProcessor.processFailureResponse(flowContext,
                ActionExecutionResponseContext.create(actionRequest.getEvent(), failureResponse));
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

        DIAGNOSTIC_LOGGER.logErrorResponse(action, errorResponse);
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

        DIAGNOSTIC_LOGGER.logFailureResponse(action, failureResponse);
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
