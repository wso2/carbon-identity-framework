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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.internal.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.execution.util.APIClient;
import org.wso2.carbon.identity.action.management.ActionManagementService;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.AuthType;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        ActionExecutionServiceComponentHolder actionExecutionServiceComponentHolder =
                ActionExecutionServiceComponentHolder.getInstance();
        actionExecutionServiceComponentHolder.setActionManagementService(actionManagementService);
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
            expectedExceptionsMessageRegExp = "No request builder found for action type: PRE_ISSUE_ACCESS_TOKEN")
    public void testActionExecuteFailureWhenNoRegisteredRequestBuilderForActionType() throws Exception {

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(mock(Action.class)));

        actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, any(), any());
    }

    @Test
    public void testActionExecuteFailureAtExceptionFromRequestBuilderForActionType() throws Exception {

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(mock(Action.class)));
        when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        when(actionExecutionRequestBuilder.buildActionExecutionRequest(any())).thenThrow(
                new ActionExecutionRequestBuilderException("Error while executing request builder."));
        try (MockedStatic<ActionExecutionRequestBuilderFactory> mockedFactory = mockStatic(
                ActionExecutionRequestBuilderFactory.class)) {
            mockedFactory.when(() -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                    .thenReturn(actionExecutionRequestBuilder);

            ActionExecutionStatus actionExecutionStatus =
                    actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, any(), any());
            assertEquals(actionExecutionStatus.getStatus(), ActionExecutionStatus.Status.FAILED);
        }
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "No response processor found for action type: PRE_ISSUE_ACCESS_TOKEN")
    public void testActionExecuteFailureWhenNoRegisteredResponseProcessorForActionType() throws Exception {

        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(mock(Action.class)));
        try (MockedStatic<ActionExecutionRequestBuilderFactory> mockedFactory = mockStatic(
                ActionExecutionRequestBuilderFactory.class)) {
            mockedFactory.when(() -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                    .thenReturn(actionExecutionRequestBuilder);

            actionExecutorService.execute(ActionType.PRE_ISSUE_ACCESS_TOKEN, any(), any());
        }
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        // Setup
        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Map<String, Object> eventContext = Collections.emptyMap();

        // Mock Action and its dependencies
        Action action = mock(Action.class);
        when(action.getStatus()).thenReturn(Action.Status.ACTIVE);
        when(action.getId()).thenReturn("actionId");
        when(action.getType()).thenReturn(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN);

        EndpointConfig endpointConfig = mock(EndpointConfig.class);
        when(action.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getUri()).thenReturn("http://example.com");

        // Mock AuthType and its properties
        List<AuthProperty> authPropertyList = createAuthProperties();
        AuthType authType = mock(AuthType.class);
        when(authType.getPropertiesWithDecryptedValues(any())).thenReturn(authPropertyList);
        when(authType.getType()).thenReturn(AuthType.AuthenticationType.BASIC);
        when(endpointConfig.getAuthentication()).thenReturn(authType);

        // Mock ActionManagementService
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        // Mock static methods
        try (MockedStatic<ActionExecutionRequestBuilderFactory> mockedFactory = mockStatic(
                ActionExecutionRequestBuilderFactory.class)) {
            mockedFactory.when(() -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                    .thenReturn(actionExecutionRequestBuilder);

            try (MockedStatic<ActionExecutionResponseProcessorFactory> reposeFactory = mockStatic(
                    ActionExecutionResponseProcessorFactory.class)) {
                reposeFactory.when(() -> ActionExecutionResponseProcessorFactory
                                .getActionExecutionResponseProcessor(any()))
                        .thenReturn(actionExecutionResponseProcessor);

                // Configure request builder
                when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
                when(actionExecutionRequestBuilder.buildActionExecutionRequest(eventContext)).thenReturn(
                        mock(ActionExecutionRequest.class));

                // Mock APIClient response
                ActionInvocationSuccessResponse successResponse = mock(ActionInvocationSuccessResponse.class);
                when(successResponse.getActionStatus()).thenReturn(ActionInvocationResponse.Status.SUCCESS);
                when(successResponse.getOperations()).thenReturn(Collections.emptyList());

                ActionInvocationResponse actionInvocationResponse = mock(ActionInvocationResponse.class);
                setField(actionInvocationResponse, "actionStatus", ActionInvocationResponse.Status.SUCCESS);
                when(actionInvocationResponse.isSuccess()).thenReturn(true);
                when(actionInvocationResponse.getResponse()).thenReturn(successResponse);

                when(apiClient.callAPI(any(), any(), any())).thenReturn(actionInvocationResponse);

                // Configure response processor
                ActionExecutionStatus expectedStatus =
                        new ActionExecutionStatus(ActionExecutionStatus.Status.SUCCESS, eventContext);
                when(actionExecutionResponseProcessor.getSupportedActionType()).thenReturn(actionType);
                when(actionExecutionResponseProcessor.processSuccessResponse(any(), any(), any())).thenReturn(
                        expectedStatus);

                // Set apiClient field using reflection
                setField(actionExecutorService, "apiClient", apiClient);

                // Execute and assert
                ActionExecutionStatus actualStatus =
                        actionExecutorService.execute(actionType, eventContext, "tenantDomain");
                assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());
            }
        }
    }

    @Test
    public void testExecuteErrorWithErrorResponse() throws Exception {
        // Setup
        ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
        Map<String, Object> eventContext = Collections.emptyMap();

        // Mock Action and its dependencies
        Action action = mock(Action.class);
        when(action.getStatus()).thenReturn(Action.Status.ACTIVE);
        when(action.getId()).thenReturn("actionId");
        when(action.getType()).thenReturn(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN);

        EndpointConfig endpointConfig = mock(EndpointConfig.class);
        when(action.getEndpoint()).thenReturn(endpointConfig);
        when(endpointConfig.getUri()).thenReturn("http://example.com");

        // Mock AuthType and its properties
        List<AuthProperty> authPropertyList = createAuthProperties();
        AuthType authType = mock(AuthType.class);
        when(authType.getPropertiesWithDecryptedValues(any())).thenReturn(authPropertyList);
        when(authType.getType()).thenReturn(AuthType.AuthenticationType.BASIC);
        when(endpointConfig.getAuthentication()).thenReturn(authType);

        // Mock ActionManagementService
        when(actionManagementService.getActionsByActionType(any(), any())).thenReturn(
                Collections.singletonList(action));

        // Mock static methods
        try (MockedStatic<ActionExecutionRequestBuilderFactory> mockedFactory = mockStatic(
                ActionExecutionRequestBuilderFactory.class)) {
            mockedFactory.when(() -> ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(any()))
                    .thenReturn(actionExecutionRequestBuilder);

            try (MockedStatic<ActionExecutionResponseProcessorFactory> reposeFactory = mockStatic(
                    ActionExecutionResponseProcessorFactory.class)) {
                reposeFactory.when(() -> ActionExecutionResponseProcessorFactory
                                .getActionExecutionResponseProcessor(any()))
                        .thenReturn(actionExecutionResponseProcessor);

                // Configure request builder
                when(actionExecutionRequestBuilder.getSupportedActionType()).thenReturn(actionType);
                when(actionExecutionRequestBuilder.buildActionExecutionRequest(eventContext)).thenReturn(
                        mock(ActionExecutionRequest.class));

                // Mock APIClient response
                ActionInvocationErrorResponse errorResponse = mock(ActionInvocationErrorResponse.class);
                when(errorResponse.getActionStatus()).thenReturn(ActionInvocationResponse.Status.ERROR);
                when(errorResponse.getError()).thenReturn("Unauthorized");
                when(errorResponse.getErrorDescription()).thenReturn("Request validation failed.");

                ActionInvocationResponse actionInvocationResponse = mock(ActionInvocationResponse.class);
                when(actionInvocationResponse.isError()).thenReturn(true);
                when(actionInvocationResponse.getResponse()).thenReturn(errorResponse);

                when(apiClient.callAPI(any(), any(), any())).thenReturn(actionInvocationResponse);

                // Configure response processor
                ActionExecutionStatus expectedStatus =
                        new ActionExecutionStatus(ActionExecutionStatus.Status.FAILED, eventContext);
                when(actionExecutionResponseProcessor.getSupportedActionType()).thenReturn(actionType);
                when(actionExecutionResponseProcessor.processErrorResponse(any(), any(), any())).thenReturn(
                        expectedStatus);

                // Set apiClient field using reflection
                setField(actionExecutorService, "apiClient", apiClient);

                // Execute and assert
                ActionExecutionStatus actualStatus =
                        actionExecutorService.execute(actionType, eventContext, "tenantDomain");
                assertEquals(actualStatus.getStatus(), expectedStatus.getStatus());
            }
        }
    }

    private List<AuthProperty> createAuthProperties() {

        List<AuthProperty> authPropertyList = new ArrayList<>();
        for (AuthType.AuthenticationType.AuthenticationProperty property :
                AuthType.AuthenticationType.BASIC.getProperties()) {
            AuthProperty authProperty;
            if (property.getName().equals("username")) {
                authProperty = new AuthProperty.AuthPropertyBuilder().name(property.getName()).value("testuser")
                        .isConfidential(true).build();
            } else if (property.getName().equals("password")) {
                authProperty = new AuthProperty.AuthPropertyBuilder().name(property.getName()).value("testpassword")
                        .isConfidential(true).build();
            } else {
                authProperty = new AuthProperty.AuthPropertyBuilder().name(property.getName()).value("unknown").build();
            }
            authPropertyList.add(authProperty);
        }
        return authPropertyList;
    }

//    @Test(expectedExceptions = ActionExecutionException.class)
//    public void testExecuteFailure() throws Exception {
//
//        ActionType actionType = ActionType.SOME_ACTION_TYPE;
//        Map<String, Object> eventContext = Collections.emptyMap();
//        String tenantDomain = "example.com";
//
//        when(actionExecutorService.getActionsByActionType(actionType, tenantDomain)).thenThrow(
//                new ActionExecutionException("Error"));
//
//        actionExecutorService.execute(actionType, eventContext, tenantDomain);
//    }

    private void setField(Object target, String fieldName, Object value) throws Exception {

        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /*        Event event = new ConcreteEvent();

        Request request = new ConcreteRequest();
        Map<String, String> additionalHeaders = new HashMap<String, String>() {{
            put("X-Header-1", "header-value1");
            put("X-Header-2", "header-value2");
            put("X-Header-3", "header-value3");
        }};
        Map<String, String> additionalParams = new HashMap<String, String>() {{
            put("x-param-1", "param-value1");
            put("x-param-2", "param-value2");
        }};

        setField(request, "additionalHeaders", additionalHeaders);
        setField(request, "additionalParams", additionalParams);
        Request spyRequest = Mockito.spy(request);

        setField(event, "request", spyRequest);
        setField(event, "tenant", new Tenant("1", "example.com"));
        setField(event, "organization", new Organization("1", "example.com"));
        setField(event, "user", new User("user-id"));
        setField(event, "userStore", new UserStore("PRIMARY"));

        // Spy on the object
        Event spyEvent = Mockito.spy(event);

        ActionExecutionRequest.Builder builder = new ActionExecutionRequest.Builder()
                .actionType(actionType)
                .flowId("flowId")
                .event(spyEvent)
                .allowedOperations(Collections.singletonList(mock(AllowedOperation.class)));

        ActionExecutionRequest actionExecutionRequest = spy(builder.build());
        when(actionExecutionRequest.getRequestId()).thenReturn("requestId");

        Action action = mock(Action.class);
        when(action.getStatus()).thenReturn(Action.Status.ACTIVE);
        when(action.getId()).thenReturn("actionId");
        when(action.getType()).thenReturn(Action.ActionTypes.PRE_ISSUE_ACCESS_TOKEN);

        EndpointConfig endpointConfig = mock(EndpointConfig.class);
        when(action.getEndpoint()).thenReturn(endpointConfig);

        when(endpointConfig.getUri()).thenReturn("http://example.com");

        List<AuthProperty> authPropertyList = new ArrayList<>();
        for (AuthType.AuthenticationType.AuthenticationProperty property : AuthType.AuthenticationType.BASIC
                .getProperties()) {
            if (property.getName().equals("username")) {
                AuthProperty authProperty =
                        new AuthProperty.AuthPropertyBuilder().name(property.getName()).value("testuser")
                                .isConfidential(true).build();
                authPropertyList.add(authProperty);
            } else if (property.getName().equals("password")) {
                AuthProperty authProperty =
                        new AuthProperty.AuthPropertyBuilder().name(property.getName()).value("testpassword")
                                .isConfidential(true).build();
                authPropertyList.add(authProperty);
            } else {
                AuthProperty authProperty =
                        new AuthProperty.AuthPropertyBuilder().name(property.getName()).value("unknown").build();
                authPropertyList.add(authProperty);
            }
        }

        AuthType authType = new AuthType.AuthTypeBuilder().type(AuthType.AuthenticationType.BASIC)
                .properties(authPropertyList).build();
        when(endpointConfig.getAuthentication()).thenReturn(authType);*/

}
