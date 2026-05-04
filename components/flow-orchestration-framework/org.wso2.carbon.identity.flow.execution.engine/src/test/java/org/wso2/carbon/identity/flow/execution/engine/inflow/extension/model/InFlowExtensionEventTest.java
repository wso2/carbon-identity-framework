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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link InFlowExtensionEvent}.
 */
public class InFlowExtensionEventTest {

    @Test
    public void testBuilderWithAllFields() {

        Map<String, Object> flowProperties = new HashMap<>();
        flowProperties.put("riskScore", 85);

        InFlowExtensionEvent event = new InFlowExtensionEvent.Builder()
                .flowType("REGISTRATION")
                .flowId("flow-id-123")
                .callbackUrl("https://example.com/callback")
                .portalUrl("https://example.com/portal")
                .flowProperties(flowProperties)
                .build();

        assertEquals(event.getFlowType(), "REGISTRATION");
        assertEquals(event.getFlowId(), "flow-id-123");
        assertEquals(event.getCallbackUrl(), "https://example.com/callback");
        assertEquals(event.getPortalUrl(), "https://example.com/portal");
        assertEquals(event.getFlowProperties().get("riskScore"), 85);
    }

    @Test
    public void testOptionalFieldsDefaultToNull() {

        InFlowExtensionEvent event = new InFlowExtensionEvent.Builder()
                .flowType("LOGIN")
                .flowId("flow-id-456")
                .flowProperties(null)
                .build();

        assertEquals(event.getFlowType(), "LOGIN");
        assertEquals(event.getFlowId(), "flow-id-456");
        assertNull(event.getCallbackUrl());
        assertNull(event.getPortalUrl());
        assertNotNull(event.getFlowProperties());
        assertTrue(event.getFlowProperties().isEmpty());
    }

    @Test
    public void testFlowPropertiesAreUnmodifiable() {

        Map<String, Object> flowProperties = new HashMap<>();
        flowProperties.put("key", "value");

        InFlowExtensionEvent event = new InFlowExtensionEvent.Builder()
                .flowProperties(flowProperties)
                .build();

        try {
            event.getFlowProperties().put("hack", "value");
            assertTrue(false, "Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected — map is unmodifiable
        }
    }

    @Test
    public void testBuilderDoesNotShareMapReferences() {

        Map<String, Object> flowProperties = new HashMap<>();
        flowProperties.put("score", "original");

        InFlowExtensionEvent event = new InFlowExtensionEvent.Builder()
                .flowProperties(flowProperties)
                .build();

        // Mutating the original map should not affect the event
        flowProperties.put("score", "modified");
        assertEquals(event.getFlowProperties().get("score"), "original");
    }
}
