/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the polling state of {@link FlowExecutionContext}.
 */
public class FlowExecutionContextTest {

    private static final String POLL_TOKEN = "poll-token";

    @Test
    public void testStartPollingReplacesTheCurrentPolling() {

        FlowExecutionContext context = inProgressContext();
        context.endPolling(true);

        context.startPolling();

        assertEquals(context.getPollingState(), PollingState.REQUESTED);
        // The flow execution service deletes the old entry and creates a new one.
        assertEquals(context.getPollToken(), POLL_TOKEN);
        assertFalse(context.isPolling());
        assertFalse(context.isPollingCompleted());
    }

    @Test
    public void testEndPollingBeforeItStarts() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.startPolling();

        context.endPolling(true);

        assertNull(context.getPollingState());
    }

    @Test
    public void testEndPollingSuccessfully() {

        FlowExecutionContext context = inProgressContext();
        assertTrue(context.isPolling());
        assertFalse(context.isPollingCompleted());

        context.endPolling(true);

        assertEquals(context.getPollingState(), PollingState.COMPLETED);
        assertEquals(context.getPollToken(), POLL_TOKEN);
        assertFalse(context.isPolling());
        assertTrue(context.isPollingCompleted());
        // Ended polling does not change again.
        context.endPolling(false);
        assertEquals(context.getPollingState(), PollingState.COMPLETED);
    }

    @Test
    public void testEndPollingWithoutSuccess() {

        FlowExecutionContext context = inProgressContext();

        context.endPolling(false);

        assertEquals(context.getPollingState(), PollingState.FAILED);
        // The flow execution service deletes the entry and clears the token.
        assertEquals(context.getPollToken(), POLL_TOKEN);
        assertFalse(context.isPolling());
        assertFalse(context.isPollingCompleted());
    }

    @Test
    public void testPollingStateSurvivesJsonRoundTrip() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        FlowExecutionContext context = inProgressContext();

        String json = objectMapper.writeValueAsString(context);
        FlowExecutionContext restored = objectMapper.readValue(json, FlowExecutionContext.class);

        assertEquals(restored.getPollToken(), POLL_TOKEN);
        assertEquals(restored.getPollingState(), PollingState.IN_PROGRESS);
        assertTrue(restored.isPolling());
        assertNull(restored.getProperty("pollToken"));
        // The derived value is not stored.
        assertFalse(objectMapper.readTree(json).has("polling"));
        assertNull(objectMapper.readValue(objectMapper.writeValueAsString(new FlowExecutionContext()),
                FlowExecutionContext.class).getPollingState());
    }

    /**
     * A context whose polling entry the flow execution service has created.
     */
    private FlowExecutionContext inProgressContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.startPolling();
        context.setPollToken(POLL_TOKEN);
        context.setPollingState(PollingState.IN_PROGRESS);
        return context;
    }
}
