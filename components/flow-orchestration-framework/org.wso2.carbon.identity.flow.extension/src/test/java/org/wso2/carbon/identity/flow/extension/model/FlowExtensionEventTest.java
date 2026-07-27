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
import org.wso2.carbon.identity.action.execution.api.model.Application;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.action.execution.api.model.Tenant;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * Unit tests for {@link FlowExtensionEvent} and its builder, including the nested flow and the
 * event-scoped fields inherited from {@code Event}.
 */
public class FlowExtensionEventTest {

    @Test
    public void testBuilderCarriesFlowAndCallbackUrl() {

        FlowExtensionFlow flow = new FlowExtensionFlow.Builder().flowId("flow-1").build();
        FlowExtensionEvent event = new FlowExtensionEvent.Builder()
                .tenant(new Tenant("1", "carbon.super"))
                .organization(new Organization.Builder().id("org-1").name("Org").build())
                .application(new Application("app-1", "App"))
                .flow(flow)
                .callbackUrl("https://callback")
                .build();

        assertSame(event.getFlow(), flow);
        assertEquals(event.getCallbackUrl(), "https://callback");
    }

    @Test
    public void testUnsetFlowAndCallbackUrlAreNull() {

        FlowExtensionEvent event = new FlowExtensionEvent.Builder().build();

        assertNull(event.getFlow());
        assertNull(event.getCallbackUrl());
    }
}
