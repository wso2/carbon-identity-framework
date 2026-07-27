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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.ErrorStatus;
import org.wso2.carbon.identity.action.execution.api.model.FailedStatus;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.action.execution.api.model.SuccessStatus;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.user.action.api.model.UserActionContext;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.component.PreUpdatePasswordActionServiceComponentHolder;
import org.wso2.carbon.identity.user.pre.update.password.action.internal.execution.PreUpdatePasswordActionExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.user.pre.update.password.action.util.TestUtil.TENANT_DOMAIN;

/**
 * User Pre Update Password Action Executor Test.
 */
public class PreUpdatePasswordActionExecutorTest {

    @Mock
    private UserActionContext mockUserActionContext;
    @Mock
    private ActionExecutorService mockActionExecutorService;
    private PreUpdatePasswordActionExecutor executor;

    @BeforeClass
    public void setUp() {

        MockitoAnnotations.initMocks(this);
        PreUpdatePasswordActionServiceComponentHolder.getInstance().setActionExecutorService(mockActionExecutorService);
        executor = new PreUpdatePasswordActionExecutor();
    }

    @Test
    public void testGetSupportedActionType() {

        assertEquals(executor.getSupportedActionType(), ActionType.PRE_UPDATE_PASSWORD);
    }

    @Test
    public void testExecuteSuccess() throws ActionExecutionException {

        ActionExecutionStatus<Success> expectedStatus = new SuccessStatus.Builder().build();
        doReturn(expectedStatus).when(mockActionExecutorService).execute(any(), any(FlowContext.class), anyString());

        ActionExecutionStatus<?> resultStatus = executor.execute(mockUserActionContext, TENANT_DOMAIN);
        assertTrue(resultStatus instanceof SuccessStatus);
        assertEquals(resultStatus.getStatus(), expectedStatus.getStatus());
    }

    @Test
    public void testExecuteFailure() throws ActionExecutionException {

        ActionExecutionStatus<Failure> expectedStatus = new FailedStatus(new Failure("Invalid Request",
                "Compromised Password"));
        doReturn(expectedStatus).when(mockActionExecutorService).execute(any(), any(FlowContext.class), anyString());

        ActionExecutionStatus<?> resultStatus = executor.execute(mockUserActionContext, TENANT_DOMAIN);
        assertTrue(resultStatus instanceof FailedStatus);
        assertEquals(resultStatus.getStatus(), expectedStatus.getStatus());
        assertEquals(((FailedStatus) resultStatus).getResponse().getFailureReason(),
                expectedStatus.getResponse().getFailureReason());
        assertEquals(((FailedStatus) resultStatus).getResponse().getFailureDescription(),
                expectedStatus.getResponse().getFailureDescription());
    }

    @Test
    public void testExecuteError() throws ActionExecutionException {

        ActionExecutionStatus<Error> expectedStatus = new ErrorStatus(new Error("Internal server error",
                "Error while validating password"));
        doReturn(expectedStatus).when(mockActionExecutorService).execute(any(), any(FlowContext.class), anyString());

        ActionExecutionStatus<?> resultStatus = executor.execute(mockUserActionContext, TENANT_DOMAIN);
        assertTrue(resultStatus instanceof ErrorStatus);
        assertEquals(resultStatus.getStatus(), expectedStatus.getStatus());
        assertEquals(((ErrorStatus) resultStatus).getResponse().getErrorMessage(),
                expectedStatus.getResponse().getErrorMessage());
        assertEquals(((ErrorStatus) resultStatus).getResponse().getErrorDescription(),
                expectedStatus.getResponse().getErrorDescription());
    }
}
