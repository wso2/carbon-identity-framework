/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.executor;

import org.apache.commons.logging.Log;
import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionEndpointException;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;

import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

/**
 * Unit tests for {@link FlowExtensionExecutor} error logging.
 */
public class FlowExtensionExecutorTest {

    private static final String ACTION_ID = "action-id";

    @Test
    public void testEndpointFailureIsLoggedAsTenantResourceFailure() throws Exception {

        ActionExecutionEndpointException e = new ActionExecutionEndpointException(
                "Unexpected response received with status code: 403.");

        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            loggerUtils.when(() -> LoggerUtils.isTenantResourceFailure(any())).thenCallRealMethod();
            invokeLogActionExecutionException(e);

            loggerUtils.verify(() -> LoggerUtils.logTenantResourceFailure(any(Log.class),
                    anyString(), eq(e)));
        }
    }

    @Test
    public void testInternalFailureIsNotLoggedAsTenantResourceFailure() throws Exception {

        ActionExecutionException e = new ActionExecutionException("Error occurred while retrieving actions.");

        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            loggerUtils.when(() -> LoggerUtils.isTenantResourceFailure(any())).thenCallRealMethod();
            invokeLogActionExecutionException(e);

            loggerUtils.verify(() -> LoggerUtils.logTenantResourceFailure(any(), any(), any()), never());
        }
    }

    private void invokeLogActionExecutionException(ActionExecutionException e) throws Exception {

        Method method = FlowExtensionExecutor.class.getDeclaredMethod("logActionExecutionException",
                ActionExecutionException.class, String.class);
        method.setAccessible(true);
        method.invoke(new FlowExtensionExecutor(), e, ACTION_ID);
    }
}
