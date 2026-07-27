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

package org.wso2.carbon.identity.user.pre.update.profile.action.execution;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionResponseContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Event;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.ResponseData;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.execution.PreUpdateProfileResponseProcessor;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * User Pre Update Password Action Response Processor Test.
 */
public class PreUpdateProfileResponseProcessorTest {

    private PreUpdateProfileResponseProcessor preUpdateProfileResponseProcessor;

    @BeforeClass
    void setUp() {

        preUpdateProfileResponseProcessor = new PreUpdateProfileResponseProcessor();
    }

    @Test
    public void testGetSupportedActionType() {

        assertEquals(preUpdateProfileResponseProcessor.getSupportedActionType(), ActionType.PRE_UPDATE_PROFILE);
    }

    @Test
    public void testProcessSuccessResponse() throws ActionExecutionResponseProcessorException {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));
        Event mockEvent = mock(Event.class);

        ActionInvocationSuccessResponse successResponse = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .responseData(mock(ResponseData.class))
                .build();

        ActionExecutionStatus<Success> resultStatus =
                preUpdateProfileResponseProcessor.processSuccessResponse(flowContext,
                        ActionExecutionResponseContext.create(mockEvent, successResponse));

        assertNotNull(resultStatus);
        assertEquals(resultStatus.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertEquals(resultStatus.getResponseContext().get(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY),
                flowContext.getContextData().get(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY));
    }

    @Test
    public void testProcessFailureResponse() throws ActionExecutionResponseProcessorException {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));
        Event mockEvent = mock(Event.class);

        ActionInvocationFailureResponse failureResponse = new ActionInvocationFailureResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.FAILED)
                .failureReason("Invalid Request")
                .failureDescription("Unallowed attribute to modify")
                .build();

        ActionExecutionStatus<Failure> resultStatus =
                preUpdateProfileResponseProcessor.processFailureResponse(flowContext,
                        ActionExecutionResponseContext.create(mockEvent, failureResponse));

        assertNotNull(resultStatus);
        assertEquals(resultStatus.getStatus(), ActionExecutionStatus.Status.FAILED);
        assertNotNull(resultStatus.getResponse());
        assertEquals(resultStatus.getResponse().getFailureReason(), failureResponse.getFailureReason());
        assertEquals(resultStatus.getResponse().getFailureDescription(), failureResponse.getFailureDescription());
    }

    @Test
    public void testProcessErrorResponse() throws ActionExecutionResponseProcessorException {

        FlowContext flowContext = FlowContext.create();
        flowContext.add(UserActionContext.USER_ACTION_CONTEXT_REFERENCE_KEY, mock(UserActionContext.class));
        Event mockEvent = mock(Event.class);

        ActionInvocationErrorResponse errorResponse = new ActionInvocationErrorResponse.Builder()
                .errorMessage("Internal Server Error")
                .errorDescription("Error while validating attributes")
                .actionStatus(ActionInvocationResponse.Status.ERROR)
                .build();

        ActionExecutionStatus<Error> resultStatus = preUpdateProfileResponseProcessor.processErrorResponse(flowContext,
                ActionExecutionResponseContext.create(mockEvent, errorResponse));

        assertNotNull(resultStatus);
        assertEquals(resultStatus.getStatus(), ActionExecutionStatus.Status.ERROR);
        assertNotNull(resultStatus.getResponse());
        assertEquals(resultStatus.getResponse().getErrorMessage(), errorResponse.getErrorMessage());
        assertEquals(resultStatus.getResponse().getErrorDescription(), errorResponse.getErrorDescription());
    }
}
