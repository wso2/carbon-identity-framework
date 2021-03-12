/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.services;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsFunctionRegistryImpl;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.util.Arrays;

import static org.testng.Assert.assertTrue;

public class ConditionalAuthenticationMgtServiceTest {

    ConditionalAuthenticationMgtService conditionalAuthenticationMgtService;

    @BeforeMethod
    public void setUp() {
        conditionalAuthenticationMgtService = new
                ConditionalAuthenticationMgtService();
    }

    @Test(dataProvider = "jsFunctionProvider")
    public void testGetAllAvailableFunctions(JsFunctionRegistry registry, String[] expected) {

        FrameworkServiceDataHolder.getInstance().setJsFunctionRegistry(registry);
        String[] actualAvailableFunctions = conditionalAuthenticationMgtService.getAllAvailableFunctions();
        Arrays.sort(actualAvailableFunctions);
        Arrays.sort(expected);
        assertTrue(Arrays.equals(actualAvailableFunctions, expected), "Function list is different from what is " +
                "expected. Expected function list : " + Arrays.toString(expected) + " , Actual list : " + Arrays
                .toString(actualAvailableFunctions));
    }

    @DataProvider(name = "jsFunctionProvider")
    public Object[][] singlePostAuthenticatorData() {

        JsFunctionRegistry emptyRegistry = new JsFunctionRegistryImpl();
        JsFunctionRegistry registryWithCustomFunctions = new JsFunctionRegistryImpl();
        registryWithCustomFunctions.register(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, "func1", new Object());

        return new Object[][]{
                {emptyRegistry, new String[]{"executeStep", "selectAcrFrom", "sendError", "Log.info"}},
                {registryWithCustomFunctions, new String[]{"executeStep", "selectAcrFrom", "sendError", "Log.info",
                        "func1"}},
        };
    }
}
