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

package org.wso2.carbon.identity.flow.extension.model;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.model.User;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * Unit tests for {@link FlowExtensionFlow} and its builder.
 */
public class FlowExtensionFlowTest {

    @Test
    public void testBuilderCarriesAllFields() {

        User user = new User("uid-1");
        FlowExtensionFlow flow = new FlowExtensionFlow.Builder()
                .flowType("REGISTRATION")
                .flowId("flow-123")
                .portalUrl("https://portal")
                .user(user)
                .build();

        assertEquals(flow.getFlowType(), "REGISTRATION");
        assertEquals(flow.getFlowId(), "flow-123");
        assertEquals(flow.getPortalUrl(), "https://portal");
        assertSame(flow.getUser(), user);
    }

    @Test
    public void testUnsetFieldsAreNull() {

        FlowExtensionFlow flow = new FlowExtensionFlow.Builder().build();

        assertNull(flow.getFlowType());
        assertNull(flow.getFlowId());
        assertNull(flow.getPortalUrl());
        assertNull(flow.getUser());
    }
}
