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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionExecutionResponseProcessorFactory;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class ActionExecutionResponseProcessorFactoryTest {

    private final ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;
    @Mock
    private ActionExecutionResponseProcessor mockResponseProcessor;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        when(mockResponseProcessor.getSupportedActionType()).thenReturn(actionType);
    }

    @Test
    public void testRegisterActionExecutionResponseProcessor() {

        ActionExecutionResponseProcessorFactory.registerActionExecutionResponseProcessor(mockResponseProcessor);
        ActionExecutionResponseProcessor registeredResult =
                ActionExecutionResponseProcessorFactory.getActionExecutionResponseProcessor(actionType);
        assertEquals(registeredResult, mockResponseProcessor);
    }

    @Test(dependsOnMethods = {"testRegisterActionExecutionResponseProcessor"})
    public void testUnregisterActionExecutionResponseProcessor() {

        ActionExecutionResponseProcessorFactory.unregisterActionExecutionResponseProcessor(mockResponseProcessor);
        ActionExecutionResponseProcessor unregisteredResult =
                ActionExecutionResponseProcessorFactory.getActionExecutionResponseProcessor(actionType);
        assertNull(unregisteredResult);
    }

    @Test(dependsOnMethods = {"testUnregisterActionExecutionResponseProcessor"})
    public void testGetActionExecutionResponseProcessorNotFound() {

        ActionExecutionResponseProcessor result =
                ActionExecutionResponseProcessorFactory.getActionExecutionResponseProcessor(actionType);
        assertNull(result);
    }

}
