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
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionExecutionRequestBuilderFactory;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class ActionExecutionRequestBuilderFactoryTest {

    @Mock
    private ActionExecutionRequestBuilder mockRequestBuilder;
    private final ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        when(mockRequestBuilder.getSupportedActionType()).thenReturn(actionType);
    }

    @Test
    public void testRegisterActionExecutionRequestBuilder() {

        ActionExecutionRequestBuilderFactory.registerActionExecutionRequestBuilder(mockRequestBuilder);
        ActionExecutionRequestBuilder registeredResult =
                ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(actionType);
        assertEquals(registeredResult, mockRequestBuilder);
    }

    @Test(dependsOnMethods = {"testRegisterActionExecutionRequestBuilder"})
    public void testUnregisterActionExecutionRequestBuilder() {

        ActionExecutionRequestBuilderFactory.unregisterActionExecutionRequestBuilder(mockRequestBuilder);
        ActionExecutionRequestBuilder unregisteredResult =
                ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(actionType);
        assertNull(unregisteredResult);
    }

    @Test(dependsOnMethods = {"testUnregisterActionExecutionRequestBuilder"})
    public void testGetActionExecutionRequestBuilderNotFound() {

        ActionExecutionRequestBuilder result =
                ActionExecutionRequestBuilderFactory.getActionExecutionRequestBuilder(actionType);
        assertNull(result);
    }
}
