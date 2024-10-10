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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.internal.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.execution.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.model.Application;
import org.wso2.carbon.identity.action.execution.model.Event;
import org.wso2.carbon.identity.action.execution.model.Operation;
import org.wso2.carbon.identity.action.execution.model.Organization;
import org.wso2.carbon.identity.action.execution.model.Request;
import org.wso2.carbon.identity.action.execution.model.Tenant;
import org.wso2.carbon.identity.action.execution.model.User;
import org.wso2.carbon.identity.action.execution.model.UserStore;
import org.wso2.carbon.identity.action.execution.util.APIClient;
import org.wso2.carbon.identity.action.execution.util.ActionExecutorConfig;
import org.wso2.carbon.identity.action.execution.util.RequestFilter;
import org.wso2.carbon.identity.action.management.ActionManagementService;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ActionExecutionRequestBuilder actionExecutionRequestBuilder;
    @Mock
    private ActionExecutionResponseProcessor actionExecutionResponseProcessor;
    @Mock
    private APIClient apiClient;
    @InjectMocks
    private ActionExecutorServiceImpl actionExecutorService;
    private MockedStatic<ActionExecutorConfig> actionExecutorConfigStatic;

    private MockedStatic<RequestFilter> requestFilter;
    private MockedStatic<LoggerUtils> loggerUtils;
    private MockedStatic<ActionExecutionRequestBuilderFactory> actionExecutionRequestBuilderFactory;
    private MockedStatic<ActionExecutionResponseProcessorFactory> actionExecutionResponseProcessorFactory;

    @BeforeMethod
    public void setUp() throws Exception {

        actionExecutorConfigStatic = mockStatic(ActionExecutorConfig.class);
        ActionExecutorConfig actionExecutorConfig = mock(ActionExecutorConfig.class);
        actionExecutorConfigStatic.when(ActionExecutorConfig::getInstance).thenReturn(actionExecutorConfig);
        MockitoAnnotations.openMocks(this);
        ActionExecutionServiceComponentHolder actionExecutionServiceComponentHolder =
                ActionExecutionServiceComponentHolder.getInstance();
        actionExecutionServiceComponentHolder.setActionManagementService(actionManagementService);
        // Set apiClient field using reflection
        setField(actionExecutorService, "apiClient", apiClient);

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
    public void testActionExecuteFailureWhenNoActionsAvailableForActionType() throws Exception {

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(Collections.emptyList());

        ActionExecutionStatus actionExecutionStatus =
                actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, any(), any());
        assertEquals(actionExecutionStatus.getStatus(), ActionExecutionStatus.Status.FAILED);
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Multiple actions found for action type: PRE_ISSUE_ACCESS_TOKEN. " +
                    "Current implementation doesn't support multiple actions for a single action type.")
    public void testActionExecuteFailureWhenMultipleActionsAvailableForActionType() throws Exception {

        List<Action> mockActions = Arrays.asList(mock(Action.class), mock(Action.class));
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(mockActions);

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, any(), any());
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "No action Ids found for action type: PRE_ISSUE_ACCESS_TOKEN")
    public void testActionExecuteWithActionIdsFailureWhenActionIdListIsEmpty() throws Exception {

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, new String[]{}, new HashMap<>(),
                "tenantDomain");
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "No request builder found for action type: PRE_ISSUE_ACCESS_TOKEN")
    public void testActionExecuteFailureWhenNoRegisteredRequestBuilderForActionType() throws Exception {

        // Mock Action and its dependencies
        Action action = createAction();
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, any(), any());
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "No request builder found for action type: PRE_ISSUE_ACCESS_TOKEN")
    public void testActionExecuteWithActionIdFailureWhenMultipleActionsAvailableForActionType() throws Exception {

        Action action = createAction();
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, new String[]{any()}, any(), any());
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Error occurred while retrieving action by action Id.")
    public void testActionExecuteWithActionIdFailureWhenInvalidActionIdGiven() throws Exception {

        when(actionManagementService.getActionByActionId(any(), any(), any())).thenThrow(
                new ActionMgtException("Error occurred while retrieving action by action Id."));

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, new String[]{any()}, any(), any());
    }

    @Test
    public void testActionExecuteFailureAtExceptionFromRequestBuilderForActionType() throws Exception {

        // Mock Action and its dependencies
        Action action = createAction();
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));
        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any())).thenThrow(
                new ActionExecutionRequestBuilderException("Error while executing request builder."));

        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        ActionExecutionStatus actionExecutionStatus =
                actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, any(), any());
        assertEquals(actionExecutionStatus.getStatus(), ActionExecutionStatus.Status.FAILED);

        ActionExecutionStatus actionExecutionStatusWithActionIds = actionExecutorService.execute(
                ActionType.PRE_ISSUE_ACCESS_TOKEN, new String[]{any()},  any(), any());
        assertEquals(actionExecutionStatusWithActionIds.getStatus(), ActionExecutionStatus.Status.FAILED);
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "No response processor found for action type: PRE_ISSUE_ACCESS_TOKEN")
    public void testActionExecuteFailureWhenNoRegisteredResponseProcessorForActionType() throws Exception {

        // Mock Action and its dependencies
        Action action = createAction();
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any())).thenReturn(
                mock(ActionExecutionRequest.class));

        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, any(), any());
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "No response processor found for action type: PRE_ISSUE_ACCESS_TOKEN")
    public void testActionExecuteWithActionIdsFailureWhenNoRegisteredResponseProcessorForActionType() throws Exception {

        // Mock Action and its dependencies
        Action action = createAction();
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any())).thenReturn(
                mock(ActionExecutionRequest.class));

        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, new String[]{any()}, any(), any());
    }

    @Test
    public void testBuildActionExecutionRequestWithExcludedHeaders() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Map<String, Object> eventContext = Collections.emptyMap();

        // Mock Action and its dependencies
        Action action = createAction();

        // Mock ActionManagementService
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        // Mock ActionRequestBuilder and ActionResponseProcessor
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        // Mock RequestFilter used in Request class
        requestFilter.when(() -> RequestFilter.getFilteredHeaders(any(), any())).thenReturn(new HashMap<>());
        requestFilter.when(() -> RequestFilter.getFilteredParams(any(), any())).thenReturn(new HashMap<>());

        ActionExecutionRequest actionExecutionRequest = createActionExecutionRequest(actionType);

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(eventContext)).thenReturn(
                actionExecutionRequest);

        // Mock APIClient response
        ActionInvocationResponse actionInvocationResponse = createSuccessActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any())).thenReturn(actionInvocationResponse);

        // Execute
        actionExecutorService.execute(actionType, eventContext, "tenantDomain");

        String payload = getJSONRequestPayload(actionExecutionRequest);
        // Verify that the HTTP client was called with the expected request
        verify(apiClient).callAPI(any(), any(), eq(payload));
    }

    @Test
    public void testBuildActionExecutionRequest() throws Exception {

        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Map<String, Object> eventContext = Collections.emptyMap();

        // Mock Action and its dependencies
        Action action = createAction();

        // Mock ActionManagementService
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        // Mock ActionRequestBuilder and ActionResponseProcessor
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        ActionExecutionRequest actionExecutionRequest = createActionExecutionRequest(actionType);

        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(eventContext)).thenReturn(
                actionExecutionRequest);

        // Mock APIClient response
        ActionInvocationResponse actionInvocationResponse = createSuccessActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any())).thenReturn(actionInvocationResponse);

        // Execute
        actionExecutorService.execute(actionType, eventContext, "tenantDomain");

        String payload = getJSONRequestPayload(actionExecutionRequest);
        // Verify that the HTTP client was called with the expected request
        verify(apiClient).callAPI(any(), any(), eq(payload));
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        // Setup
        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Map<String, Object> eventContext = Collections.emptyMap();

        // Mock Action and its dependencies
        Action action = createAction();

        // Mock ActionManagementService
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        // Mock ActionRequestBuilder and ActionResponseProcessor
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        // Configure request builder
        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(eventContext)).thenReturn(
                mock(ActionExecutionRequest.class));

        // Mock APIClient response
        ActionInvocationResponse actionInvocationResponse = createSuccessActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any())).thenReturn(actionInvocationResponse);

        // Configure response processor
        ActionExecutionStatus expectedStatus =
                new ActionExecutionStatus(ActionExecutionStatus.Status.SUCCESS, eventContext);
        when(actionExecutionResponseProcessor.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionResponseProcessor.processSuccessResponse(any(), any(), any())).thenReturn(
                expectedStatus);
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        // Execute and assert
        ActionExecutionStatus actualStatus =
                actionExecutorService.execute(actionType, eventContext, "tenantDomain");
        assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());

        ActionExecutionStatus actionExecutionStatusWithActionIds = actionExecutorService.execute(
                actionType, new String[]{action.getId()}, eventContext, "tenantDomain");
        assertEquals(actionExecutionStatusWithActionIds.getStatus(), expectedStatus.getStatus());
    }

    @Test
    public void testExecuteFailure() throws Exception {
        // Setup
        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Map<String, Object> eventContext = Collections.emptyMap();

        // Mock Action and its dependencies
        Action action = createAction();

        // Mock ActionManagementService
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        // Mock ActionRequestBuilder and ActionResponseProcessor
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);
        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        // Configure request builder
        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(eventContext)).thenReturn(
                mock(ActionExecutionRequest.class));

        // Mock APIClient response
        ActionInvocationResponse actionInvocationResponse = createFailureActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any())).thenReturn(actionInvocationResponse);

        // Configure response processor
        ActionExecutionStatus expectedStatus =
                new ActionExecutionStatus(ActionExecutionStatus.Status.FAILED, eventContext);
        when(actionExecutionResponseProcessor.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionResponseProcessor.processFailureResponse(any(), any(), any())).thenReturn(
                expectedStatus);
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        // Execute and assert
        ActionExecutionStatus actualStatus =
                actionExecutorService.execute(actionType, eventContext, "tenantDomain");
        assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());

        ActionExecutionStatus actionExecutionStatusWithActionIds = actionExecutorService.execute(
                actionType, new String[]{action.getId()}, eventContext, "tenantDomain");
        assertEquals(actionExecutionStatusWithActionIds.getStatus(), expectedStatus.getStatus());
    }

    @Test
    public void testExecuteError() throws Exception {
        // Setup
        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Map<String, Object> eventContext = Collections.emptyMap();

        // Mock Action and its dependencies
        Action action = createAction();

        // Mock ActionManagementService
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        // Mock static methods
        actionExecutionRequestBuilderFactory.when(
                        () -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                .thenReturn(actionExecutionRequestBuilder);

        actionExecutionResponseProcessorFactory.when(() -> ActionExecutionResponseProcessorFactory
                        .getActionExecutionResponseProcessor(any()))
                .thenReturn(actionExecutionResponseProcessor);

        // Configure request builder
        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(eventContext)).thenReturn(
                mock(ActionExecutionRequest.class));

        // Mock APIClient response
        ActionInvocationResponse actionInvocationResponse = createErrorActionInvocationResponse();
        when(apiClient.callAPI(any(), any(), any())).thenReturn(actionInvocationResponse);

        // Configure response processor
        ActionExecutionStatus expectedStatus =
                new ActionExecutionStatus(ActionExecutionStatus.Status.ERROR, eventContext);
        when(actionExecutionResponseProcessor.getSupportedActionType()).thenReturn(actionType);
        when(actionExecutionResponseProcessor.processErrorResponse(any(), any(), any())).thenReturn(
                expectedStatus);
        when(actionManagementService.getActionByActionId(any(), any(), any())).thenReturn(action);

        // Execute and assert
        ActionExecutionStatus actualStatus =
                actionExecutorService.execute(actionType, eventContext, "tenantDomain");
        assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());

        ActionExecutionStatus actionExecutionStatusWithActionIds = actionExecutorService.execute(
                actionType, new String[]{action.getId()}, eventContext, "tenantDomain");
        assertEquals(actionExecutionStatusWithActionIds.getStatus(), expectedStatus.getStatus());
    }

    private String getJSONRequestPayload(ActionExecutionRequest actionExecutionRequest) throws JsonProcessingException {

        ObjectMapper requestObjectmapper = new ObjectMapper();
        requestObjectmapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        requestObjectmapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return requestObjectmapper.writeValueAsString(actionExecutionRequest);
    }

    private ActionInvocationResponse createSuccessActionInvocationResponse() throws Exception {

        ActionInvocationSuccessResponse successResponse = mock(ActionInvocationSuccessResponse.class);
        when(successResponse.getActionStatus()).thenReturn(ActionInvocationResponse.Status.SUCCESS);
        when(successResponse.getOperations()).thenReturn(Collections.emptyList());

        ActionInvocationResponse actionInvocationResponse = mock(ActionInvocationResponse.class);
        setField(actionInvocationResponse, "actionStatus", ActionInvocationResponse.Status.SUCCESS);
        when(actionInvocationResponse.isSuccess()).thenReturn(true);
        when(actionInvocationResponse.getResponse()).thenReturn(successResponse);
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
        when(errorResponse.getError()).thenReturn("Unauthorized");
        when(errorResponse.getErrorDescription()).thenReturn("Request validation failed.");

        ActionInvocationResponse actionInvocationResponse = mock(ActionInvocationResponse.class);
        when(actionInvocationResponse.isError()).thenReturn(true);
        when(actionInvocationResponse.getResponse()).thenReturn(errorResponse);
        return actionInvocationResponse;
    }

    private ActionExecutionRequest createActionExecutionRequest(ActionType actionType) throws Exception {

        Map<String, String[]> headers = new HashMap<>();
        headers.put("Content-Type", new String[]{"application/json"});
        headers.put("X-Header-1", new String[]{"X-header-1-value"});
        headers.put("X-Header-2", new String[]{"X-header-2-value"});
        headers.put("X-Header-3", new String[]{"X-header-3-value"});

        Map<String, String[]> params = new HashMap<>();
        params.put("x-param-1", new String[]{"X-param-1-value"});
        params.put("x-param-2", new String[]{"X-param-2-value"});
        params.put("x-param-3", new String[]{"X-param-3-value"});

        Request request = spy(mock(ConcreteRequest.class));
        setField(request, "additionalHeaders", headers);
        setField(request, "additionalParams", params);

        Event event = spy(ConcreteEvent.class);
        setField(event, "request", request);
        setField(event, "tenant", new Tenant("45", "tenant-45"));
        setField(event, "organization", new Organization("9600e5d0-969d-46b4-a463-9fd5de97196a", "test-org-1"));
        setField(event, "user", new User("8ebe008f-33c1-4d2d-97ee-eaacb17d8114"));
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

        EndpointConfig endpointConfig = mock(EndpointConfig.class);
        when(action.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getUri()).thenReturn("http://example.com");

        // Mock Authentication and its properties
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
