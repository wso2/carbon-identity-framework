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

package org.wso2.carbon.identity.user.pre.update.password.action.execution;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.execution.model.Error;
import org.wso2.carbon.identity.action.execution.model.Event;
import org.wso2.carbon.identity.action.execution.model.Failure;
import org.wso2.carbon.identity.action.execution.model.ResponseData;
import org.wso2.carbon.identity.action.execution.model.Success;
import org.wso2.carbon.identity.user.action.service.model.UserActionContext;
import org.wso2.carbon.identity.user.pre.update.password.action.core.constant.PreUpdatePasswordActionConstants;
import org.wso2.carbon.identity.user.pre.update.password.action.core.execution.PreUpdatePasswordResponseProcessor;
import org.wso2.carbon.identity.user.pre.update.password.action.service.model.PreUpdatePasswordAction;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * User Pre Update Password Action Response Processor Test.
 */
public class PreUpdatePasswordResponseProcessorTest {

    private PreUpdatePasswordResponseProcessor responseProcessor;

    @BeforeClass
    void setUp() {

        responseProcessor = new PreUpdatePasswordResponseProcessor();
    }

    @Test
    public void testGetSupportedActionType() {

        assertEquals(ActionType.PRE_UPDATE_PASSWORD, responseProcessor.getSupportedActionType());
    }

    @Test
    public void testProcessSuccessResponse() {

        Map<String, Object> eventContext = new HashMap<>();
        eventContext.put(PreUpdatePasswordActionConstants.USER_ACTION_CONTEXT, mock(UserActionContext.class));
        eventContext.put("action", mock(PreUpdatePasswordAction.class));
        Event mockEvent = mock(Event.class);
        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus = responseProcessor.processSuccessResponse(eventContext, mockEvent,
                successResponse);

        assertNotNull(resultStatus);
        assertEquals(resultStatus.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertEquals(resultStatus.getResponseContext().get(PreUpdatePasswordActionConstants.USER_ACTION_CONTEXT),
                eventContext.get(PreUpdatePasswordActionConstants.USER_ACTION_CONTEXT));
        assertEquals(resultStatus.getResponseContext().get("action"), eventContext.get("action"));
    }

    @Test
    public void testProcessFailureResponse() throws ActionExecutionResponseProcessorException {

        Map<String, Object> eventContext = new HashMap<>();
        eventContext.put(PreUpdatePasswordActionConstants.USER_ACTION_CONTEXT, mock(UserActionContext.class));
        eventContext.put("action", mock(PreUpdatePasswordAction.class));
        Event mockEvent = mock(Event.class);
        ActionInvocationFailureResponse failureResponse = new ActionInvocationFailureResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.FAILED)
                .failureReason("Invalid Request")
                .failureDescription("Compromised Password")
                .build();

        ActionExecutionStatus<Failure> resultStatus = responseProcessor.processFailureResponse(eventContext, mockEvent,
                failureResponse);

        assertNotNull(resultStatus);
        assertEquals(resultStatus.getStatus(), ActionExecutionStatus.Status.FAILED);
        assertNotNull(resultStatus.getResponse());
        assertEquals(resultStatus.getResponse().getFailureReason(), failureResponse.getFailureReason());
        assertEquals(resultStatus.getResponse().getFailureDescription(), failureResponse.getFailureDescription());
    }

    @Test
    public void testProcessErrorResponse() throws ActionExecutionResponseProcessorException {

        Map<String, Object> eventContext = new HashMap<>();
        eventContext.put(PreUpdatePasswordActionConstants.USER_ACTION_CONTEXT, mock(UserActionContext.class));
        eventContext.put("action", mock(PreUpdatePasswordAction.class));
        Event mockEvent = mock(Event.class);
        ActionInvocationErrorResponse errorResponse = new ActionInvocationErrorResponse.Builder()
                .errorMessage("Internal server error")
                .errorDescription("Error while validating password")
                .actionStatus(ActionInvocationResponse.Status.ERROR)
                .build();

        ActionExecutionStatus<Error> resultStatus = responseProcessor.processErrorResponse(eventContext, mockEvent,
                errorResponse);

        assertNotNull(resultStatus);
        assertEquals(resultStatus.getStatus(), ActionExecutionStatus.Status.ERROR);
        assertNotNull(resultStatus.getResponse());
        assertEquals(resultStatus.getResponse().getErrorMessage(), errorResponse.getErrorMessage());
        assertEquals(resultStatus.getResponse().getErrorDescription(), errorResponse.getErrorDescription());
    }
}
