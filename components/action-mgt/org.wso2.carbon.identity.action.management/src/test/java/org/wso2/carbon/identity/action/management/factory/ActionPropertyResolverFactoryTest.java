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

package org.wso2.carbon.identity.action.management.factory;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.ActionPropertyResolver;
import org.wso2.carbon.identity.action.management.model.Action;

import static org.mockito.Mockito.doReturn;

/**
 * Action Property Resolver Factory Test.
 */
public class ActionPropertyResolverFactoryTest {

    private final Action.ActionTypes actionType = Action.ActionTypes.PRE_UPDATE_PASSWORD;
    @Mock
    private ActionPropertyResolver mockActionPropertyResolver;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        doReturn(actionType).when(mockActionPropertyResolver).getSupportedActionType();
    }

    @Test
    public void testRegisterActionPropertyResolver() {

        ActionPropertyResolverFactory.registerActionPropertyResolver(mockActionPropertyResolver);
        ActionPropertyResolver registeredResult = ActionPropertyResolverFactory.getActionPropertyResolver(actionType);
        Assert.assertEquals(registeredResult, mockActionPropertyResolver);
    }

    @Test(dependsOnMethods = {"testRegisterActionPropertyResolver"})
    public void testUnregisterActionPropertyResolver() {

        ActionPropertyResolverFactory.unregisterActionPropertyResolver(mockActionPropertyResolver);
        ActionPropertyResolver unregisteredResult = ActionPropertyResolverFactory.getActionPropertyResolver(actionType);
        Assert.assertNull(unregisteredResult);
    }

    @Test(dependsOnMethods = {"testUnregisterActionPropertyResolver"})
    public void testGetActionPropertyResolverNotFound() {

        ActionPropertyResolver result = ActionPropertyResolverFactory.getActionPropertyResolver(actionType);
        Assert.assertNull(result);
    }
}
