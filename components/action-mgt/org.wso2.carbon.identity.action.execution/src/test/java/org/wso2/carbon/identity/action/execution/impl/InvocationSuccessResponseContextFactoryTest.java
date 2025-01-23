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
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.execution.model.Context;
import org.wso2.carbon.identity.action.execution.util.UserContext;

import static org.testng.Assert.assertEquals;

public class InvocationSuccessResponseContextFactoryTest {

    @Test
    public void testRegisterInvocationSuccessResponseContextClass() {

        InvocationSuccessResponseContextFactory.registerInvocationSuccessResponseContextClass(UserContext.class);
        Class<? extends Context> registeredResult = InvocationSuccessResponseContextFactory
                .getInvocationSuccessResponseContextClass(ActionType.AUTHENTICATION);
        assertEquals(registeredResult, UserContext.class);
    }

    @Test(dependsOnMethods = {"testRegisterInvocationSuccessResponseContextClass"})
    public void testGetInvocationSuccessResponseContextClass() {

        Class<? extends Context> extendedClass = InvocationSuccessResponseContextFactory
                .getInvocationSuccessResponseContextClass(ActionType.AUTHENTICATION);
        assertEquals(extendedClass, UserContext.class);
    }

    @Test(dependsOnMethods = {"testGetInvocationSuccessResponseContextClass"})
    public void testUnregisterInvocationSuccessResponseContextClass() {

        InvocationSuccessResponseContextFactory.unregisterInvocationSuccessResponse(UserContext.class);
        Class<? extends Context> unregisteredResult = InvocationSuccessResponseContextFactory
                .getInvocationSuccessResponseContextClass(ActionType.AUTHENTICATION);
        assertEquals(unregisteredResult, Context.DefaultContext.class);
    }
}
