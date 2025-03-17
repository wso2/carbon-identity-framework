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

package org.wso2.carbon.identity.action.management.dao;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.action.management.internal.dao.impl.ActionDTOModelResolverFactory;

import static org.mockito.Mockito.doReturn;

/**
 * ActionDTO Model Resolver Factory Test.
 */
public class ActionDTOModelResolverFactoryTest {

    private final Action.ActionTypes actionType = Action.ActionTypes.PRE_UPDATE_PASSWORD;
    @Mock
    private ActionDTOModelResolver mockActionDTOModelResolver;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        doReturn(actionType).when(mockActionDTOModelResolver).getSupportedActionType();
    }

    @Test
    public void testRegisterActionDTOModelResolver() {

        ActionDTOModelResolverFactory.registerActionDTOModelResolver(mockActionDTOModelResolver);
        ActionDTOModelResolver registeredResult = ActionDTOModelResolverFactory.getActionDTOModelResolver(actionType);
        Assert.assertEquals(registeredResult, mockActionDTOModelResolver);
    }

    @Test(dependsOnMethods = {"testRegisterActionDTOModelResolver"})
    public void testUnregisterActionDTOModelResolver() {

        ActionDTOModelResolverFactory.unregisterActionDTOModelResolver(mockActionDTOModelResolver);
        ActionDTOModelResolver unregisteredResult = ActionDTOModelResolverFactory.getActionDTOModelResolver(actionType);
        Assert.assertNull(unregisteredResult);
    }

    @Test(dependsOnMethods = {"testUnregisterActionDTOModelResolver"})
    public void testGetActionDTOModelResolverNotFound() {

        ActionDTOModelResolver result = ActionDTOModelResolverFactory.getActionDTOModelResolver(actionType);
        Assert.assertNull(result);
    }
}
