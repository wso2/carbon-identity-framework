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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.ResponseData;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionInvocationResponseClassFactory;
import org.wso2.carbon.identity.action.execution.internal.service.impl.DefaultResponseData;
import org.wso2.carbon.identity.action.execution.util.TestActionInvocationResponseClassProvider;
import org.wso2.carbon.identity.action.execution.util.UserData;

import static org.testng.Assert.assertEquals;

public class InvocationSuccessResponseContextFactoryTest {

    @Test
    public void testRegisterInvocationSuccessResponseContextClass() {

        ActionInvocationResponseClassFactory.registerActionInvocationResponseClassProvider(
                new TestActionInvocationResponseClassProvider());
        Class<? extends ResponseData> registeredResult = ActionInvocationResponseClassFactory
                .getInvocationSuccessResponseDataClass(ActionType.AUTHENTICATION);
        assertEquals(registeredResult, UserData.class);
    }

    @Test(dependsOnMethods = {"testRegisterInvocationSuccessResponseContextClass"})
    public void testGetInvocationSuccessResponseContextClassWithDefault() {

        Class<? extends ResponseData> extendedClass = ActionInvocationResponseClassFactory
                .getInvocationSuccessResponseDataClass(ActionType.PRE_ISSUE_ACCESS_TOKEN);
        assertEquals(extendedClass, DefaultResponseData.class);
    }

    @Test(dependsOnMethods = {"testRegisterInvocationSuccessResponseContextClass"})
    public void testGetInvocationSuccessResponseContextClass() {

        Class<? extends ResponseData> extendedClass = ActionInvocationResponseClassFactory
                .getInvocationSuccessResponseDataClass(ActionType.AUTHENTICATION);
        assertEquals(extendedClass, UserData.class);
    }

    @Test(dependsOnMethods = {"testGetInvocationSuccessResponseContextClass"})
    public void testUnregisterInvocationSuccessResponseContextClass() {

        ActionInvocationResponseClassFactory.unregisterActionInvocationResponseClassProvider(
                new TestActionInvocationResponseClassProvider());
        Class<? extends ResponseData> unregisteredResult = ActionInvocationResponseClassFactory
                .getInvocationSuccessResponseDataClass(ActionType.AUTHENTICATION);
        assertEquals(unregisteredResult, DefaultResponseData.class);
    }
}
