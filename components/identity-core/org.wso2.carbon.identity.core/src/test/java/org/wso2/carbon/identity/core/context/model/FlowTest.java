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

package org.wso2.carbon.identity.core.context.model;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link Flow}.
 */
public class FlowTest {

    @Test
    public void testBuildDeviceRegisterFlowInitiatedByUser() {

        Flow flow = new Flow.Builder()
                .name(Flow.Name.DEVICE_REGISTER)
                .initiatingPersona(Flow.InitiatingPersona.USER)
                .build();

        assertEquals(flow.getName(), Flow.Name.DEVICE_REGISTER);
        assertEquals(flow.getInitiatingPersona(), Flow.InitiatingPersona.USER);
    }

    @DataProvider(name = "unsupportedDeviceRegisterPersonas")
    public Object[][] unsupportedDeviceRegisterPersonas() {

        return new Object[][]{
                {Flow.InitiatingPersona.ADMIN},
                {Flow.InitiatingPersona.APPLICATION},
                {Flow.InitiatingPersona.SYSTEM}
        };
    }

    @Test(dataProvider = "unsupportedDeviceRegisterPersonas", expectedExceptions = IllegalArgumentException.class)
    public void testBuildDeviceRegisterFlowRejectsNonUserPersona(Flow.InitiatingPersona persona) {

        new Flow.Builder()
                .name(Flow.Name.DEVICE_REGISTER)
                .initiatingPersona(persona)
                .build();
    }
}
