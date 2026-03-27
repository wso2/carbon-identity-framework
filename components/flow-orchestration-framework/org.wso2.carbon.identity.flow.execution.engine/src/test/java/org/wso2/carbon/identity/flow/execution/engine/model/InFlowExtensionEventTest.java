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

package org.wso2.carbon.identity.flow.execution.engine.model;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.InFlowExtensionEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link InFlowExtensionEvent}.
 */
public class InFlowExtensionEventTest {

    @Test
    public void testBuilderWithAllFields() {

        Map<String, String> userInputs = new HashMap<>();
        userInputs.put("email", "test@example.com");

        Map<String, Object> flowProperties = new HashMap<>();
        flowProperties.put("riskScore", 85);

        InFlowExtensionEvent event = new InFlowExtensionEvent.Builder()
                .flowType("REGISTRATION")
                .currentNodeId("node-1")
                .userInputs(userInputs)
                .flowProperties(flowProperties)
                .build();

        assertEquals(event.getFlowType(), "REGISTRATION");
        assertEquals(event.getCurrentNodeId(), "node-1");
        assertEquals(event.getUserInputs().get("email"), "test@example.com");
        assertEquals(event.getFlowProperties().get("riskScore"), 85);
    }

    @Test
    public void testBuilderWithNullMaps() {

        InFlowExtensionEvent event = new InFlowExtensionEvent.Builder()
                .flowType("LOGIN")
                .currentNodeId("node-2")
                .userInputs(null)
                .flowProperties(null)
                .build();

        assertEquals(event.getFlowType(), "LOGIN");
        assertNotNull(event.getUserInputs());
        assertTrue(event.getUserInputs().isEmpty());
        assertNotNull(event.getFlowProperties());
        assertTrue(event.getFlowProperties().isEmpty());
    }

    @Test
    public void testUserInputsAreUnmodifiable() {

        Map<String, String> userInputs = new HashMap<>();
        userInputs.put("field", "value");

        InFlowExtensionEvent event = new InFlowExtensionEvent.Builder()
                .userInputs(userInputs)
                .build();

        try {
            event.getUserInputs().put("hack", "value");
            assertTrue(false, "Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected — map is unmodifiable
        }
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

        Map<String, String> userInputs = new HashMap<>();
        userInputs.put("email", "original@test.com");

        InFlowExtensionEvent event = new InFlowExtensionEvent.Builder()
                .userInputs(userInputs)
                .build();

        // Mutating the original map should not affect the event
        userInputs.put("email", "modified@test.com");
        assertEquals(event.getUserInputs().get("email"), "original@test.com");
    }
}
