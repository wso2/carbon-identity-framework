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

package org.wso2.carbon.identity.action.management.service;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.service.ActionConverter;
import org.wso2.carbon.identity.action.management.internal.service.impl.ActionConverterFactory;

import static org.mockito.Mockito.doReturn;

/**
 * Action Converter Factory Test.
 */
public class ActionConverterFactoryTest {

    private final Action.ActionTypes actionType = Action.ActionTypes.PRE_UPDATE_PASSWORD;
    @Mock
    private ActionConverter mockActionConverter;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        doReturn(actionType).when(mockActionConverter).getSupportedActionType();
    }

    @Test
    public void testRegisterActionConverter() {

        ActionConverterFactory.registerActionConverter(mockActionConverter);
        ActionConverter registeredResult = ActionConverterFactory.getActionConverter(actionType);
        Assert.assertEquals(registeredResult, mockActionConverter);
    }

    @Test(dependsOnMethods = {"testRegisterActionConverter"})
    public void testUnregisterActionConverter() {

        ActionConverterFactory.unregisterActionConverter(mockActionConverter);
        ActionConverter unregisteredResult = ActionConverterFactory.getActionConverter(actionType);
        Assert.assertNull(unregisteredResult);
    }

    @Test(dependsOnMethods = {"testUnregisterActionConverter"})
    public void testGetActionConverterNotFound() {

        ActionConverter result = ActionConverterFactory.getActionConverter(actionType);
        Assert.assertNull(result);
    }
}
