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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationIncompleteResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.api.model.Application;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.ErrorStatus;
import org.wso2.carbon.identity.action.execution.api.model.Event;
import org.wso2.carbon.identity.action.execution.api.model.FailedStatus;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Header;
import org.wso2.carbon.identity.action.execution.api.model.IncompleteStatus;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.action.execution.api.model.Param;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.api.model.Request;
import org.wso2.carbon.identity.action.execution.api.model.SuccessStatus;
import org.wso2.carbon.identity.action.execution.api.model.Tenant;
import org.wso2.carbon.identity.action.execution.api.model.User;
import org.wso2.carbon.identity.action.execution.api.model.UserClaim;
import org.wso2.carbon.identity.action.execution.api.model.UserStore;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.action.execution.internal.component.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionExecutionRequestBuilderFactory;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionExecutionResponseProcessorFactory;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionExecutorServiceImpl;
import org.wso2.carbon.identity.action.execution.internal.util.APIClient;
import org.wso2.carbon.identity.action.execution.internal.util.ActionExecutionDiagnosticLogger;
import org.wso2.carbon.identity.action.execution.internal.util.ActionExecutorConfig;
import org.wso2.carbon.identity.action.execution.internal.util.RequestFilter;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionRule;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ActionExecutorServiceImplTest {

    @Mock
    private ActionManagementService actionManagementService;
    @Mock
    private RuleEvaluationService ruleEvaluationService;
    @Mock
    private ActionExecutionRequestBuilder actionExecutionRequestBuilder;
    @Mock
    private ActionExecutionResponseProcessor actionExecutionResponseProcessor;
    @Mock
    private APIClient apiClient;
    @Mock
    private ActionExecutionDiagnosticLogger actionExecutionDiagnosticLogger;
    @InjectMocks
    private ActionExecutorServiceImpl actionExecutorService;
    private ActionExecutorConfig actionExecutorConfig;
    private MockedStatic<ActionExecutorConfig> actionExecutorConfigStatic;

    private MockedStatic<RequestFilter> requestFilter;
    private MockedStatic<LoggerUtils> loggerUtils;
    private MockedStatic<ActionExecutionRequestBuilderFactory> actionExecutionRequestBuilderFactory;
    private MockedStatic<ActionExecutionResponseProcessorFactory> actionExecutionResponseProcessorFactory;

    @BeforeMethod
    public void setUp() throws Exception {

        actionExecutorConfigStatic = mockStatic(ActionExecutorConfig.class);
        actionExecutorConfig = mock(ActionExecutorConfig.class);
        actionExecutorConfigStatic.when(ActionExecutorConfig::getInstance).thenReturn(actionExecutorConfig);
        when(actionExecutorConfig.getHttpConnectionPoolSize()).thenReturn(20);
        when(actionExecutorConfig.getRetiredUpToVersion(any())).thenReturn("v0");
        MockitoAnnotations.openMocks(this);
        ActionExecutionServiceComponentHolder actionExecutionServiceComponentHolder =
                ActionExecutionServiceComponentHolder.getInstance();
        actionExecutionServiceComponentHolder.setActionManagementService(actionManagementService);
        actionExecutionServiceComponentHolder.setRuleEvaluationService(ruleEvaluationService);
        // Set apiClient field using reflection
        setField(actionExecutorService, "apiClient", apiClient);
        setFinalField(actionExecutorService, "DIAGNOSTIC_LOGGER", actionExecutionDiagnosticLogger);

        requestFilter = mockStatic(RequestFilter.class);
        loggerUtils = mockStatic(LoggerUtils.class);
        loggerUtils.when(() -> LoggerUtils.isDiagnosticLogsEnabled()).thenReturn(true);
        actionExecutionRequestBuilderFactory = mockStatic(ActionExecutionRequestBuilderFactory.class);
        actionExecutionResponseProcessorFactory = mockStatic(ActionExecutionResponseProcessorFactory.class);
    }

    @AfterMethod
    public void tearDown() {

        requestFilter.close();
        loggerUtils.close();
        actionExecutionRequestBuilderFactory.close();
        actionExecutionResponseProcessorFactory.close();
        actionExecutorConfigStatic.close();
    }

    @Test
    public void testActionExecuteSuccessWhenNoActionsAvailableForActionType() throws Exception {

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(Collections.emptyList());

        ActionExecutionStatus expectedStatus = new SuccessStatus.Builder().build();
        ActionExecutionStatus actualStatus =
                actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, FlowContext.create(), "tenantDomain");

        assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());
    }

    @Test
    public void testActionExecuteSuccessWhenNoActiveActionAvailableForActionType() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;

        Action action = mock(Action.class);
        when(action.getStatus()).thenReturn(Action.Status.INACTIVE);
        when(action.getActionVersion()).thenReturn("v1");
        when(action.getType()).thenReturn(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN);
        when(actionManagementService.getActionsByActionType(Action.ActionTypes.valueOf(actionType.name()).
                getPathParam(), "tenantDomain")).thenReturn(new LinkedList<>(Collections.singleton(action)));

        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(actionType))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(actionType))
                .thenReturn(actionExecutionResponseProcessor);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenReturn(
                mock(ActionExecutionRequest.class));

        ActionExecutionStatus expectedStatus = new SuccessStatus.Builder().build();
        ActionExecutionStatus actualStatus =
                actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");

        assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());
    }

    @Test
    public void testActionExecuteSuccessWhenRuleConfiguredAndNotSatisfied() throws Exception {

        Action action = mock(Action.class);
        when(action.getStatus()).thenReturn(Action.Status.ACTIVE);
        when(action.getActionVersion()).thenReturn("v1");
        when(action.getType()).thenReturn(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN);
        when(action.getActionRule()).thenReturn(ActionRule.create("ruleId", "tenantDomain"));
        when(ruleEvaluationService.evaluate(any(), any(), any())).thenReturn(new RuleEvaluationResult("ruleId", false));

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;

        ActionExecutionStatus<?> status =
                actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Multiple actions found for action type: PRE_ISSUE_ACCESS_TOKEN. " +
                    "Current implementation doesn't support multiple actions for a single action type.")
    public void testActionExecuteFailureWhenMultipleActionsAvailableForActionType() throws Exception {

        List<Action> mockActions = Arrays.asList(mock(Action.class), mock(Action.class));
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(mockActions);

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, FlowContext.create(), "tenantDomain");
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Action Id cannot be blank.")
    public void testActionExecuteWithActionIdsFailureWheNullActionId() throws Exception {

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, null, FlowContext.create(),
                "tenantDomain");
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "No request builder found for action type: PRE_ISSUE_ACCESS_TOKEN")
    public void testActionExecuteFailureWhenNoRegisteredRequestBuilderForActionType() throws Exception {

        Action action = createAction();
        when(actionManagementService.getActionsByActionType(any(), eq("tenantDomain"))).thenReturn(
                Collections.singletonList(action));

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, FlowContext.create(), "tenantDomain");
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Error occurred while retrieving action by action Id.")
    public void testActionExecuteWithActionIdFailureWhenInvalidActionIdGiven() throws Exception {

        when(actionManagementService.getActionByActionId(any(), any(), any())).thenThrow(
                new ActionMgtException("Error occurred while retrieving action by action Id."));

        actionExecutorService.execute(
                ActionType.PRE_ISSUE_ACCESS_TOKEN, "actionId", FlowContext.create(), "tenantDomain");
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Error occurred while retrieving actions.")
    public void testActionExecuteWithActionFailureWhenInvalidActionGiven() throws Exception {

        when(actionManagementService.getActionsByActionType(any(), any())).thenThrow(
                new ActionMgtException("Error occurred while retrieving actions."));

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, FlowContext.create(), "tenant1");
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Error occurred while evaluating the rule for action: actionId")
    public void testActionExecuteFailureWhenRuleEvaluationFails() throws Exception {

        Action action = mock(Action.class);
        when(action.getId()).thenReturn("actionId");
        when(action.getStatus()).thenReturn(Action.Status.ACTIVE);
        when(action.getActionVersion()).thenReturn("v1");
        when(action.getType()).thenReturn(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN);
        when(action.getActionRule()).thenReturn(ActionRule.create("ruleId", "tenantDomain"));
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        when(ruleEvaluationService.evaluate(any(), any(), any())).thenThrow(new RuleEvaluationException("Error"));

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;

        ActionExecutionStatus<?> status =
                actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Failed to build the request payload for action type: " +
                    "PRE_ISSUE_ACCESS_TOKEN")
    public void testActionExecuteFailureWhenBuildingActionExecutionRequestForActionId() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;

        Action action = createAction();
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(actionType))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(actionType))
                .thenReturn(actionExecutionResponseProcessor);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenThrow(
                new ActionExecutionRequestBuilderException("Error while executing request builder."));

        actionExecutorService.execute(actionType, "actionId", FlowContext.create(), "tenantDomain");
    }

    @Test(expectedExceptions = ActionExecutionException.class)
    public void testActionExecuteFailureAtExceptionFromRequestBuilderForActionType() throws Exception {

        Action action = createAction();
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenThrow(
                new ActionExecutionRequestBuilderException("Error while executing request builder."));

        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        ActionExecutionStatus actionExecutionStatus =
                actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, FlowContext.create(), "tenantDomain");
        assertEquals(actionExecutionStatus.getStatus(), ActionExecutionStatus.Status.FAILED);

        ActionExecutionStatus actionExecutionStatusWithActionIds = actionExecutorService.execute(
                ActionType.PRE_ISSUE_ACCESS_TOKEN, any(), FlowContext.create(), "tenantDomain");
        assertEquals(actionExecutionStatusWithActionIds.getStatus(), ActionExecutionStatus.Status.FAILED);
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "No response processor found for action type: PRE_ISSUE_ACCESS_TOKEN")
    public void testActionExecuteFailureWhenNoRegisteredResponseProcessorForActionType() throws Exception {

        Action action = createAction();
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenReturn(
                mock(ActionExecutionRequest.class));

        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, FlowContext.create(), "tenantDomain");
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "No response processor found for action type: PRE_ISSUE_ACCESS_TOKEN")
    public void testActionExecuteWithActionIdsFailureWhenNoRegisteredResponseProcessorForActionType() throws Exception {

        Action action = createAction();
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenReturn(
                mock(ActionExecutionRequest.class));

        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);

        actionExecutorService.execute(
                ActionType.PRE_ISSUE_ACCESS_TOKEN, action.getId(), FlowContext.create(), "tenantDomain");
    }

    @Test
    public void testBuildActionExecutionRequestWithExcludedHeaders()
            throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Action action = createAction();

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        requestFilter.when(() -> RequestFilter.getFilteredHeaders(any(), any(), any()))
                .thenReturn(new ArrayList<Header>());
        requestFilter.when(() -> RequestFilter.getFilteredParams(any(), any(), any()))
                .thenReturn(new ArrayList<Param>());

        ActionExecutionRequest actionExecutionRequest = createActionExecutionRequest(actionType);

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenReturn(
                actionExecutionRequest);

        ActionInvocationResponse actionInvocationResponse = createSuccessActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any(), any(), any())).thenReturn(actionInvocationResponse);

        actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");

        String payload = getJSONRequestPayload(actionExecutionRequest);
        // Verify that the HTTP client was called with the expected request
        verify(apiClient).callAPI(any(), any(), any(), any(), eq(payload));
    }

    @Test
    public void testBuildActionExecutionRequest() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Action action = createAction();

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        ActionExecutionRequest actionExecutionRequest = createActionExecutionRequest(actionType);

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenReturn(
                actionExecutionRequest);

        ActionInvocationResponse actionInvocationResponse = createSuccessActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any(), any(), any())).thenReturn(actionInvocationResponse);

        actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");

        String payload = getJSONRequestPayload(actionExecutionRequest);
        // Verify that the HTTP client was called with the expected request
        verify(apiClient).callAPI(any(), any(), any(), any(), eq(payload));
    }

    @Test
    public void testActionExecuteSuccessWhenNoRuleConfiguredInAction() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Action action = createAction();

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        when(action.getActionRule()).thenReturn(null);
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        requestFilter.when(() -> RequestFilter.getFilteredHeaders(any(), any(), any()))
                .thenReturn(new ArrayList<Header>());
        requestFilter.when(() -> RequestFilter.getFilteredParams(any(), any(), any()))
                .thenReturn(new ArrayList<Param>());

        ActionExecutionRequest actionExecutionRequest = createActionExecutionRequest(actionType);
        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenReturn(
                actionExecutionRequest);

        ActionInvocationResponse actionInvocationResponse =
                createSuccessActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any(), any(), any())).thenReturn(actionInvocationResponse);

        ActionExecutionStatus expectedStatus = new SuccessStatus.Builder().build();
        when(actionExecutionResponseProcessor.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionResponseProcessor.processSuccessResponse(any(), any())).thenReturn(
                expectedStatus);
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        ActionExecutionStatus actualStatus =
                actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");
        assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());

        ActionExecutionStatus actionExecutionStatusWithActionIds = actionExecutorService.execute(
                actionType, action.getId(), FlowContext.create(), "tenantDomain");
        assertEquals(actionExecutionStatusWithActionIds.getStatus(), expectedStatus.getStatus());
    }

    @Test
    public void testActionExecuteSuccessWhenRuleConfiguredInActionIsSatisfied() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Action action = createAction();

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        when(action.getActionRule()).thenReturn(ActionRule.create("ruleId", "tenantDomain"));

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        when(ruleEvaluationService.evaluate(any(), any(), any())).thenReturn(new RuleEvaluationResult("ruleId", true));

        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        requestFilter.when(() -> RequestFilter.getFilteredHeaders(any(), any(), any()))
                .thenReturn(new ArrayList<Header>());
        requestFilter.when(() -> RequestFilter.getFilteredParams(any(), any(), any()))
                .thenReturn(new ArrayList<Param>());

        ActionExecutionRequest actionExecutionRequest = createActionExecutionRequest(actionType);
        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenReturn(
                actionExecutionRequest);

        ActionInvocationResponse actionInvocationResponse = createSuccessActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any(), any(), any())).thenReturn(actionInvocationResponse);

        ActionExecutionStatus expectedStatus = new SuccessStatus.Builder().build();
        when(actionExecutionResponseProcessor.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionResponseProcessor.processSuccessResponse(any(), any())).thenReturn(
                expectedStatus);
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        ActionExecutionStatus actualStatus =
                actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");
        assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());

        ActionExecutionStatus actionExecutionStatusWithActionIds = actionExecutorService.execute(
                actionType, action.getId(), FlowContext.create(), "tenantDomain");
        assertEquals(actionExecutionStatusWithActionIds.getStatus(), expectedStatus.getStatus());
    }

    @Test(expectedExceptions = ActionExecutionException.class,
    expectedExceptionsMessageRegExp = "Action version is retired for action: actionId")
    public void testActionExecuteFailureWithRetiredActionVersion() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Action action = createAction();

        when(actionExecutorConfig.getRetiredUpToVersion(any())).thenReturn("v1");

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        when(action.getActionRule()).thenReturn(null);
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        ActionInvocationResponse actionInvocationResponse =
                createSuccessActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any(), any(), any())).thenReturn(actionInvocationResponse);

        ActionExecutionStatus expectedStatus = new SuccessStatus.Builder().build();
        when(actionExecutionResponseProcessor.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionResponseProcessor.processSuccessResponse(any(), any())).thenReturn(
                expectedStatus);
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");
    }

    @Test
    public void testActionExecuteFailure() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Action action = createAction();

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenReturn(
                mock(ActionExecutionRequest.class));

        ActionInvocationResponse actionInvocationResponse = createFailureActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any(), any(), any())).thenReturn(actionInvocationResponse);

        ActionExecutionStatus expectedStatus = new FailedStatus(new Failure("Error_reason",
                "Error_description"));
        when(actionExecutionResponseProcessor.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionResponseProcessor.processFailureResponse(any(), any())).thenReturn(expectedStatus);
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        ActionExecutionStatus actualStatus =
                actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");
        assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());
        assertEquals(((FailedStatus) actualStatus).getResponse().getFailureReason(), "Error_reason");
        assertEquals(((FailedStatus) actualStatus).getResponse().getFailureDescription(), "Error_description");

        ActionExecutionStatus actionExecutionStatusWithActionIds = actionExecutorService.execute(
                actionType, action.getId(), FlowContext.create(), "tenantDomain");
        assertEquals(actionExecutionStatusWithActionIds.getStatus(), expectedStatus.getStatus());
    }

    @Test
    public void testExecuteIncomplete() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Action action = createAction();

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenReturn(
                mock(ActionExecutionRequest.class));

        ActionInvocationResponse actionInvocationResponse = createIncompleteActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any(), any(), any())).thenReturn(actionInvocationResponse);

        ActionExecutionStatus expectedStatus = new IncompleteStatus.Builder().build();
        when(actionExecutionResponseProcessor.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionResponseProcessor.processIncompleteResponse(any(), any())).thenReturn(expectedStatus);
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        ActionExecutionStatus actualStatus =
                actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");
        assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());

        ActionExecutionStatus actionExecutionStatusWithActionIds = actionExecutorService.execute(
                actionType, action.getId(), FlowContext.create(), "tenantDomain");
        assertEquals(actionExecutionStatusWithActionIds.getStatus(), expectedStatus.getStatus());
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Received an invalid or unexpected response for action type: " +
                    "PRE_ISSUE_ACCESS_TOKEN action ID: actionId")
    public void testActionExecuteFailureForUnexpectedAPIResponse() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Action action = createAction();

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);

        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenReturn(
                mock(ActionExecutionRequest.class));

        ActionInvocationResponse actionInvocationResponse = createActionInvocationResponseWithoutAPIResponse();
        when(apiClient.callAPI(any(), any(), any(), any(), any())).thenReturn(actionInvocationResponse);

        actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");
    }

    @Test
    public void testExecuteError() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Action action = createAction();

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);

        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any(), any())).thenReturn(
                mock(ActionExecutionRequest.class));

        ActionInvocationResponse actionInvocationResponse = createErrorActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any(), any(), any())).thenReturn(actionInvocationResponse);

        ActionExecutionStatus expectedStatus = new ErrorStatus(new Error("Error_message",
                "Error_description"));
        when(actionExecutionResponseProcessor.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionResponseProcessor.processErrorResponse(any(), any())).thenReturn(expectedStatus);
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        ActionExecutionStatus actualStatus =
                actionExecutorService.execute(actionType, FlowContext.create(), "tenantDomain");
        assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());
        assertEquals(((ErrorStatus) actualStatus).getResponse().getErrorMessage(), "Error_message");
        assertEquals(((ErrorStatus) actualStatus).getResponse().getErrorDescription(), "Error_description");

        ActionExecutionStatus actionExecutionStatusWithActionIds = actionExecutorService.execute(
                actionType, action.getId(), FlowContext.create(), "tenantDomain");
        assertEquals(actionExecutionStatusWithActionIds.getStatus(), expectedStatus.getStatus());
    }

    private String getJSONRequestPayload(ActionExecutionRequest actionExecutionRequest) throws JsonProcessingException {

        ObjectMapper requestObjectmapper = new ObjectMapper();
        requestObjectmapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        requestObjectmapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return requestObjectmapper.writeValueAsString(actionExecutionRequest);
    }

    private ActionInvocationResponse createSuccessActionInvocationResponse() throws Exception {

        PerformableOperation performableOp = new PerformableOperation();
        performableOp.setOp(Operation.ADD);
        performableOp.setPath("/accessToken/claims/-");
        performableOp.setValue("testValue");

        ActionInvocationSuccessResponse successResponse = mock(ActionInvocationSuccessResponse.class);
        when(successResponse.getActionStatus()).thenReturn(ActionInvocationResponse.Status.SUCCESS);
        when(successResponse.getOperations()).thenReturn(new ArrayList<>(Collections.singletonList(performableOp)));

        ActionInvocationResponse actionInvocationResponse = mock(ActionInvocationResponse.class);
        setField(actionInvocationResponse, "actionStatus", ActionInvocationResponse.Status.SUCCESS);
        when(actionInvocationResponse.isSuccess()).thenReturn(true);
        when(actionInvocationResponse.getResponse()).thenReturn(successResponse);
        return actionInvocationResponse;
    }

    private ActionInvocationResponse createIncompleteActionInvocationResponse() {

        ActionInvocationIncompleteResponse incompleteResponse = mock(ActionInvocationIncompleteResponse.class);
        when(incompleteResponse.getActionStatus()).thenReturn(ActionInvocationResponse.Status.INCOMPLETE);
        when(incompleteResponse.getOperations()).thenReturn(Collections.emptyList());

        ActionInvocationResponse actionInvocationResponse = mock(ActionInvocationResponse.class);
        when(actionInvocationResponse.isIncomplete()).thenReturn(true);
        when(actionInvocationResponse.getResponse()).thenReturn(incompleteResponse);
        return actionInvocationResponse;
    }

    private ActionInvocationResponse createFailureActionInvocationResponse() {

        ActionInvocationFailureResponse failureResponse = mock(ActionInvocationFailureResponse.class);
        when(failureResponse.getActionStatus()).thenReturn(ActionInvocationResponse.Status.FAILED);
        when(failureResponse.getFailureReason()).thenReturn("User is not found");
        when(failureResponse.getFailureReason()).thenReturn("User is not found in the ABC system. " +
                "Hence unable to authenticate user.");

        ActionInvocationResponse actionInvocationResponse = mock(ActionInvocationResponse.class);
        when(actionInvocationResponse.isFailure()).thenReturn(true);
        when(actionInvocationResponse.getResponse()).thenReturn(failureResponse);
        return actionInvocationResponse;
    }

    private ActionInvocationResponse createErrorActionInvocationResponse() {

        ActionInvocationErrorResponse errorResponse = mock(ActionInvocationErrorResponse.class);
        when(errorResponse.getActionStatus()).thenReturn(ActionInvocationResponse.Status.ERROR);
        when(errorResponse.getErrorMessage()).thenReturn("Unauthorized");
        when(errorResponse.getErrorDescription()).thenReturn("Request validation failed.");

        ActionInvocationResponse actionInvocationResponse = mock(ActionInvocationResponse.class);
        when(actionInvocationResponse.isError()).thenReturn(true);
        when(actionInvocationResponse.getResponse()).thenReturn(errorResponse);
        return actionInvocationResponse;
    }

    private ActionInvocationResponse createActionInvocationResponseWithoutAPIResponse() {

        ActionInvocationResponse actionInvocationResponse = mock(ActionInvocationResponse.class);
        when(actionInvocationResponse.isError()).thenReturn(true);
        when(actionInvocationResponse.getResponse()).thenReturn(null);
        return actionInvocationResponse;
    }

    private ActionExecutionRequest createActionExecutionRequest(ActionType actionType) throws Exception {

        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Content-Type", new String[]{"application/json"}));
        headers.add(new Header("X-Header-1", new String[]{"X-header-1-value"}));
        headers.add(new Header("X-Header-2", new String[]{"X-header-2-value"}));
        headers.add(new Header("X-Header-3", new String[]{"X-header-3-value"}));

        List<Param> params = new ArrayList<>();
        params.add(new Param("x-param-1", new String[]{"X-param-1-value"}));
        params.add(new Param("x-param-2", new String[]{"X-param-2-value"}));
        params.add(new Param("x-param-3", new String[]{"X-param-3-value"}));

        Request request = spy(mock(ConcreteRequest.class));
        setField(request, "additionalHeaders", headers);
        setField(request, "additionalParams", params);

        Event event = spy(ConcreteEvent.class);
        setField(event, "request", request);
        setField(event, "tenant", new Tenant("45", "tenant-45"));
        setField(event, "organization", new Organization("9600e5d0-969d-46b4-a463-9fd5de97196a", "test-org-1"));
        User.Builder userBuilder = new User.Builder("8ebe008f-33c1-4d2d-97ee-eaacb17d8114").claims(
                Collections.singletonList(new UserClaim("http://wso2.org/claims/username", "testuser")));
        setField(event, "user", userBuilder.build());
        setField(event, "userStore", new UserStore("PRIMARY"));
        setField(event, "application", new Application("af82f304-ac9b-4d2b-b4da-17e01bd13d09", "test-app-1"));

        return new ActionExecutionRequest.Builder().actionType(actionType).allowedOperations(getAllowedOperations())
                .event(event).flowId("flowId").build();
    }

    private Action createAction() throws ActionMgtException {

        Action action = mock(Action.class);
        when(action.getStatus()).thenReturn(Action.Status.ACTIVE);
        when(action.getId()).thenReturn("actionId");
        when(action.getType()).thenReturn(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN);
        when(action.getActionVersion()).thenReturn("v1");

        EndpointConfig endpointConfig = mock(EndpointConfig.class);
        when(action.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getUri()).thenReturn("http://example.com");

        Authentication mockAuthenticationConfig = new Authentication.BasicAuthBuilder("testuser",
                "testpassword").build();
        Authentication authenticationConfig = mock(Authentication.class);
        when(authenticationConfig.getPropertiesWithDecryptedValues(any()))
                .thenReturn(mockAuthenticationConfig.getProperties());
        when(authenticationConfig.getType()).thenReturn(mockAuthenticationConfig.getType());
        when(endpointConfig.getAuthentication()).thenReturn(authenticationConfig);
        return action;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {

        Field field;
        try {
            field = target.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = target.getClass().getSuperclass().getDeclaredField(fieldName);
        }

        field.setAccessible(true);
        field.set(target, value);
    }

    private void setFinalField(Object target, String fieldName, Object value) throws Exception {

        Field field;
        try {
            field = target.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            field = target.getClass().getSuperclass().getDeclaredField(fieldName);
        }

        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(target, value);
    }

    private List<AllowedOperation> getAllowedOperations() {

        AllowedOperation addOperation =
                createAllowedOperation(Operation.ADD, Arrays.asList("/accessToken/claims/", "/accessToken/scopes/"));
        AllowedOperation removeOperation = createAllowedOperation(Operation.REMOVE,
                Arrays.asList("/accessToken/claims/", "/accessToken/scopes/", "/accessToken/claims/aud/"));

        return Arrays.asList(addOperation, removeOperation);
    }

    private AllowedOperation createAllowedOperation(Operation op, List<String> paths) {

        AllowedOperation operation = new AllowedOperation();
        operation.setOp(op);
        operation.setPaths(new ArrayList<>(paths));
        return operation;
    }

    private static class ConcreteEvent extends Event {

    }

    private static class ConcreteRequest extends Request {

    }
}
