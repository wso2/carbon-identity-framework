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

package org.wso2.carbon.identity.action.execution.impl;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.service.ActionVersioningHandler;
import org.wso2.carbon.identity.action.execution.api.service.impl.DefaultActionVersioningHandler;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionVersioningHandlerFactory;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ActionVersioningHandlerFactoryTest {

    @Mock
    private ActionVersioningHandler mockActionVersioningHandler;
    private final ActionType actionType = ActionType.PRE_ISSUE_ACCESS_TOKEN;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        when(mockActionVersioningHandler.getSupportedActionType()).thenReturn(actionType);
    }

    @Test
    public void testGetActionExecutionRequestBuilderNotFound() {

        ActionVersioningHandler result = ActionVersioningHandlerFactory.getActionVersioningHandler(actionType);
        assertEquals(result, DefaultActionVersioningHandler.getInstance());
    }

    @Test
    public void testRegisterActionExecutionRequestBuilder() {

        ActionVersioningHandlerFactory.registerActionVersioningHandler(mockActionVersioningHandler);
        ActionVersioningHandler registeredResult =
                ActionVersioningHandlerFactory.getActionVersioningHandler(actionType);
        assertEquals(registeredResult, mockActionVersioningHandler);
    }

    @Test(dependsOnMethods = {"testRegisterActionExecutionRequestBuilder"})
    public void testUnregisterActionExecutionRequestBuilder() {

        ActionVersioningHandlerFactory.unregisterActionVersioningHandler(mockActionVersioningHandler);
        ActionVersioningHandler unregisteredResult =
                ActionVersioningHandlerFactory.getActionVersioningHandler(actionType);
        assertEquals(unregisteredResult, DefaultActionVersioningHandler.getInstance());
    }
}
