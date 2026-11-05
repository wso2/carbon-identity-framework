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

package org.wso2.carbon.identity.flow.execution.engine;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.flow.execution.engine.core.FlowExecutionEngine;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineClientException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.listener.FlowExecutionListener;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.execution.engine.model.OTFIRetention;
import org.wso2.carbon.identity.flow.execution.engine.model.PollingState;
import org.wso2.carbon.identity.flow.execution.engine.poll.FlowPollingService;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.identity.flow.execution.engine.validation.InputProcessingListener;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.RETAIN_OTFI;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.TASK_EXECUTION;

/**
 * Unit tests for the polling state transitions FlowExecutionService applies after a node runs.
 */
public class FlowPollingTransitionTest {

    private static final String TENANT_DOMAIN = "test.com";
    private static final String FLOW_TYPE = "REGISTRATION";
    private static final String EXECUTOR_NAME = "DeviceRegistrationExecutor";
    private static final String POLL_TOKEN = "poll-token";
    private static final String NEW_POLL_TOKEN = "new-poll-token";

    private final FlowExecutionEngine engineMock = mock(FlowExecutionEngine.class);
    private final FlowPollingService pollingService = mock(FlowPollingService.class);
    private final List<String> events = new ArrayList<>();
    private MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolderMockedStatic;
    private MockedStatic<FlowExecutionEngineUtils> utils;
    private MockedStatic<FlowExecutionEngine> engine;
    private MockedStatic<FlowPollingService> polling;

    @BeforeClass
    public void setup() {

        frameworkServiceDataHolderMockedStatic = mockStatic(FrameworkServiceDataHolder.class);
        FrameworkServiceDataHolder frameworkServiceDataHolder = mock(FrameworkServiceDataHolder.class);
        frameworkServiceDataHolderMockedStatic.when(FrameworkServiceDataHolder::getInstance)
                .thenReturn(frameworkServiceDataHolder);
        when(frameworkServiceDataHolder.getIdentityEventService()).thenReturn(mock(IdentityEventService.class));
        System.setProperty(CarbonBaseConstants.CARBON_HOME, this.getClass().getResource("/").getFile());
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234);
    }

    @BeforeMethod
    public void setUpMocks() throws Exception {

        reset(engineMock, pollingService);
        events.clear();
        utils = mockStatic(FlowExecutionEngineUtils.class);
        utils.when(() -> FlowExecutionEngineUtils.handleClientException(anyString(),
                any(Constants.ErrorMessages.class), any(Object[].class))).thenCallRealMethod();
        utils.when(() -> FlowExecutionEngineUtils.addFlowContextToCache(any(FlowExecutionContext.class)))
                .thenAnswer(invocation -> events.add("cache:" + ((FlowExecutionContext) invocation.getArgument(0))
                        .getContextIdentifier()));
        utils.when(() -> FlowExecutionEngineUtils.addFlowContextToCache(anyString(), any()))
                .thenAnswer(invocation -> events.add("cache:" + invocation.getArgument(0)));
        utils.when(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(anyString()))
                .thenAnswer(invocation -> events.add("remove:" + invocation.getArgument(0)));
        engine = mockStatic(FlowExecutionEngine.class);
        engine.when(FlowExecutionEngine::getInstance).thenReturn(engineMock);
        polling = mockStatic(FlowPollingService.class);
        polling.when(FlowPollingService::getInstance).thenReturn(pollingService);
        when(pollingService.create(anyString())).thenAnswer(invocation -> {
            events.add("create:" + invocation.getArgument(0));
            return NEW_POLL_TOKEN;
        });
        when(pollingService.markSuccess(anyString())).thenAnswer(invocation -> events.add("markSuccess"));
        doAnswer(invocation -> events.add("delete:" + invocation.getArgument(0))).when(pollingService)
                .delete(anyString());
    }

    @AfterMethod
    public void closeMocks() {

        utils.close();
        engine.close();
        polling.close();
    }

    @AfterClass
    public void teardown() {

        frameworkServiceDataHolderMockedStatic.close();
        PrivilegedCarbonContext.endTenantFlow();
    }

    @Test
    public void testStartPollingCreatesTheEntryBeforeCaching() throws Exception {

        FlowExecutionContext context = newContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        when(engineMock.execute(context)).thenAnswer(invocation -> {
            context.startPolling();
            return pollStep(flowId);
        });
        // The cached context holds the new token.
        utils.when(() -> FlowExecutionEngineUtils.addFlowContextToCache(any(FlowExecutionContext.class)))
                .thenAnswer(invocation -> events.add("cache:" + context.getPollToken()));

        FlowExecutionStep step = FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null,
                FLOW_TYPE, new HashMap<>());

        assertEquals(events, Arrays.asList("create:" + flowId, "cache:" + NEW_POLL_TOKEN));
        Map<String, String> data = step.getData().getAdditionalData();
        assertEquals(data.get("pollToken"), NEW_POLL_TOKEN);
        assertEquals(data.get("pollInterval"), "3");
        assertEquals(data.get("expiresIn"), "180");
        assertEquals(data.get("qrCodeData"), "qr");
        assertEquals(context.getPollingState(), PollingState.IN_PROGRESS);
        verify(pollingService, never()).delete(anyString());
        verify(pollingService, never()).markSuccess(anyString());
    }

    @Test
    public void testPollDataIsAddedOnlyToAPollStep() throws Exception {

        FlowExecutionContext context = newContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        when(engineMock.execute(context)).thenAnswer(invocation -> {
            context.startPolling();
            return incompleteStep(flowId);
        });

        FlowExecutionStep step = FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null,
                FLOW_TYPE, new HashMap<>());

        // The entry is created, but the step does not poll, so its token is not exposed.
        assertEquals(events, Arrays.asList("create:" + flowId, "cache:" + flowId));
        assertNull(step.getData());
        assertEquals(context.getPollToken(), NEW_POLL_TOKEN);
        assertTrue(context.isPolling());
    }

    @Test
    public void testStartPollingAgainReplacesTheEntry() throws Exception {

        FlowExecutionContext context = pollingContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        when(engineMock.execute(context)).thenAnswer(invocation -> {
            context.startPolling();
            return pollStep(flowId);
        });

        FlowExecutionStep step = FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null,
                FLOW_TYPE, new HashMap<>());

        assertEquals(events, Arrays.asList("delete:" + POLL_TOKEN, "create:" + flowId, "cache:" + flowId));
        assertEquals(step.getData().getAdditionalData().get("pollToken"), NEW_POLL_TOKEN);
        assertEquals(context.getPollToken(), NEW_POLL_TOKEN);
    }

    @Test
    public void testPollingStartedAndEndedInOneRequestCreatesNoEntry() throws Exception {

        FlowExecutionContext context = pollingContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        when(engineMock.execute(context)).thenAnswer(invocation -> {
            context.startPolling();
            context.endPolling(true);
            return incompleteStep(flowId);
        });

        FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null, FLOW_TYPE,
                new HashMap<>());

        assertEquals(events, Arrays.asList("delete:" + POLL_TOKEN, "cache:" + flowId));
        assertNull(context.getPollToken());
        assertNull(context.getPollingState());
    }

    @Test
    public void testSuccessIsMarkedOnlyAfterTheFlowIdContinuesTheFlow() throws Exception {

        String otfi = UUID.randomUUID().toString();
        FlowExecutionContext context = retainedContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(otfi)).thenReturn(context);
        when(engineMock.execute(context)).thenAnswer(invocation -> {
            // The executor ends the retention and the polling.
            context.getProperties().remove(RETAIN_OTFI);
            context.endPolling(true);
            return incompleteStep(flowId);
        });

        FlowExecutionStep step = FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, otfi, null,
                FLOW_TYPE, new HashMap<>());

        assertEquals(step.getFlowId(), otfi);
        assertEquals(events, Arrays.asList("cache:" + flowId, "remove:" + otfi, "markSuccess"));
        verify(pollingService).markSuccess(POLL_TOKEN);
        assertEquals(context.getPollToken(), POLL_TOKEN);
        assertEquals(context.getPollingState(), PollingState.COMPLETED);
        utils.verify(() -> FlowExecutionEngineUtils.rollbackContext(anyString(), anyString()), never());
        verify(pollingService, never()).delete(anyString());

        // The waiting client continues the flow, so its entry is deleted and success is not marked again.
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        doReturn(incompleteStep(flowId)).when(engineMock).execute(context);
        FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null, FLOW_TYPE,
                new HashMap<>());
        verify(pollingService).markSuccess(anyString());
        verify(pollingService).delete(POLL_TOKEN);
        assertNull(context.getPollToken());
        assertNull(context.getPollingState());
    }

    @Test
    public void testSuccessNotMarkedFailsTheRequestAndEndsTheFlow() throws Exception {

        String otfi = UUID.randomUUID().toString();
        FlowExecutionContext context = retainedContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(otfi)).thenReturn(context);
        when(engineMock.execute(context)).thenAnswer(invocation -> {
            context.getProperties().remove(RETAIN_OTFI);
            context.endPolling(true);
            return incompleteStep(flowId);
        });
        // The polling entry expired or was deleted.
        doAnswer(invocation -> !events.add("markSuccess")).when(pollingService).markSuccess(anyString());
        utils.when(() -> FlowExecutionEngineUtils.rollbackContext(anyString(), anyString()))
                .thenAnswer(invocation -> events.add("rollback:" + invocation.getArgument(1)));

        try {
            FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, otfi, null, FLOW_TYPE,
                    new HashMap<>());
            fail("The device must not get the pairing code when the browser is not told of the success.");
        } catch (FlowEngineClientException e) {
            assertEquals(e.getErrorCode(), Constants.ErrorMessages.ERROR_CODE_INVALID_FLOW_ID.getCode());
        }
        // The error path ends both ids of the flow, including the flow id the context was just cached under.
        assertEquals(events, Arrays.asList("cache:" + flowId, "remove:" + otfi, "markSuccess",
                "rollback:" + otfi, "remove:" + otfi, "rollback:" + flowId, "remove:" + flowId,
                "delete:" + POLL_TOKEN));
        assertNull(context.getPollToken());
        assertNull(context.getPollingState());
    }

    @Test
    public void testSuccessMarkingErrorEndsTheFlow() throws Exception {

        String otfi = UUID.randomUUID().toString();
        FlowExecutionContext context = retainedContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(otfi)).thenReturn(context);
        when(engineMock.execute(context)).thenAnswer(invocation -> {
            context.getProperties().remove(RETAIN_OTFI);
            context.endPolling(true);
            return incompleteStep(flowId);
        });
        doThrow(new FlowEngineServerException("FE-65038", "Error while handling the polling entry.",
                "Error while handling the polling entry.")).when(pollingService).markSuccess(anyString());

        try {
            FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, otfi, null, FLOW_TYPE,
                    new HashMap<>());
            fail("A store error while marking the success must fail the request.");
        } catch (FlowEngineServerException e) {
            assertEquals(e.getErrorCode(), "FE-65038");
        }
        utils.verify(() -> FlowExecutionEngineUtils.rollbackContext(FLOW_TYPE, flowId));
        utils.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(flowId));
        verify(pollingService).delete(POLL_TOKEN);
    }

    @Test
    public void testEndPollingWithoutSuccessDeletesTheEntryBeforeCaching() throws Exception {

        FlowExecutionContext context = pollingContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        when(engineMock.execute(context)).thenAnswer(invocation -> {
            context.endPolling(false);
            return incompleteStep(flowId);
        });
        // The cached context no longer holds the token.
        utils.when(() -> FlowExecutionEngineUtils.addFlowContextToCache(any(FlowExecutionContext.class)))
                .thenAnswer(invocation -> events.add("cache:" + context.getPollToken()));

        FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null, FLOW_TYPE,
                new HashMap<>());

        assertEquals(events, Arrays.asList("delete:" + POLL_TOKEN, "cache:null"));
        assertNull(context.getPollingState());
        verify(pollingService, never()).markSuccess(anyString());
        verify(pollingService, never()).create(anyString());
    }

    @Test
    public void testNextRequestAfterSuccessDeletesTheEntry() throws Exception {

        FlowExecutionContext context = pollingContext();
        context.endPolling(true);
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        // The waiting client continues the flow and the executor sees the completed polling.
        when(engineMock.execute(context)).thenAnswer(invocation -> {
            assertTrue(context.isPollingCompleted());
            return incompleteStep(flowId);
        });

        FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null, FLOW_TYPE,
                new HashMap<>());

        assertEquals(events, Arrays.asList("delete:" + POLL_TOKEN, "cache:" + flowId));
        assertNull(context.getPollToken());
        assertNull(context.getPollingState());
        verify(pollingService, never()).markSuccess(anyString());
        verify(pollingService, never()).create(anyString());
    }

    @Test
    public void testCompletedFlowDeletesTheEntry() throws Exception {

        FlowExecutionContext context = pollingContext();
        context.endPolling(true);
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        when(engineMock.execute(context)).thenReturn(new FlowExecutionStep.Builder().flowId(flowId)
                .flowStatus(Constants.STATUS_COMPLETE).stepType("VIEW").build());

        FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null, FLOW_TYPE,
                new HashMap<>());

        assertEquals(events, Arrays.asList("delete:" + POLL_TOKEN, "remove:" + flowId));
        assertNull(context.getPollToken());
        assertNull(context.getPollingState());
        verify(pollingService, never()).markSuccess(anyString());
    }

    @Test
    public void testCleanupErrorDoesNotFailACompletedFlow() throws Exception {

        FlowExecutionContext context = pollingContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        when(engineMock.execute(context)).thenReturn(new FlowExecutionStep.Builder().flowId(flowId)
                .flowStatus(Constants.STATUS_COMPLETE).stepType("VIEW").build());
        doThrow(new FlowEngineServerException("FE-65038", "Error while handling the polling entry.",
                "Error while handling the polling entry.")).when(pollingService).delete(anyString());

        FlowExecutionStep step = FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null,
                FLOW_TYPE, new HashMap<>());

        assertEquals(step.getFlowStatus(), Constants.STATUS_COMPLETE);
        utils.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(flowId));
        utils.verify(() -> FlowExecutionEngineUtils.rollbackContext(anyString(), anyString()), never());
        assertNull(context.getPollToken());
        assertNull(context.getPollingState());
    }

    @Test
    public void testUnchangedPollingTouchesNoEntry() throws Exception {

        FlowExecutionContext context = pollingContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        when(engineMock.execute(context)).thenReturn(incompleteStep(flowId));

        FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null, FLOW_TYPE,
                new HashMap<>());

        assertEquals(events, Collections.singletonList("cache:" + flowId));
        assertEquals(context.getPollToken(), POLL_TOKEN);
        assertEquals(context.getPollingState(), PollingState.IN_PROGRESS);
    }

    @Test
    public void testExecutorErrorDeletesTheEntry() throws Exception {

        assertErrorDeletesTheEntry(new FlowEngineClientException("DR-60002", "Invalid device.", "Invalid device."));
    }

    @Test
    public void testRetainedIdGuardErrorDeletesTheEntry() throws Exception {

        assertErrorDeletesTheEntry(new FlowEngineClientException(
                Constants.ErrorMessages.ERROR_CODE_STEP_NOT_ALLOWED_FOR_RETAINED_OTFI.getCode(),
                "Not allowed to execute the current step.", "The current step cannot be executed."));
    }

    @Test
    public void testErrorAfterStartingPollingDeletesTheEntryOfTheRequest() throws Exception {

        FlowExecutionContext context = pollingContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        when(engineMock.execute(context)).thenAnswer(invocation -> {
            context.startPolling();
            return pollStep(flowId);
        });
        // Caching fails after the new entry is created.
        utils.when(() -> FlowExecutionEngineUtils.addFlowContextToCache(any(FlowExecutionContext.class)))
                .thenThrow(new FlowEngineServerException("FE-65001", "Error.", "Error."));

        try {
            FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null, FLOW_TYPE,
                    new HashMap<>());
            fail("The caching error must fail the request.");
        } catch (FlowEngineServerException e) {
            assertEquals(e.getErrorCode(), "FE-65001");
        }
        verify(pollingService).delete(NEW_POLL_TOKEN);
        assertNull(context.getPollToken());
        assertNull(context.getPollingState());
    }

    @Test
    public void testInputGuardErrorDeletesTheEntry() throws Exception {

        String otfi = UUID.randomUUID().toString();
        FlowExecutionContext context = retainedContext();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(otfi)).thenReturn(context);
        FlowExecutionListener listener = new InputProcessingListener();
        FlowExecutionEngineDataHolder.getInstance().addFlowExecutionListeners(listener);
        try {
            // The POLL step declares no inputs, so the engine rejects inputs before any executor runs.
            FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, otfi, null, FLOW_TYPE,
                    Collections.singletonMap("publicKey", "key"));
            fail("Inputs no step declared must be rejected.");
        } catch (FlowEngineClientException e) {
            assertEquals(e.getErrorCode(), Constants.ErrorMessages.ERROR_CODE_INVALID_USER_INPUT.getCode());
        } finally {
            FlowExecutionEngineDataHolder.getInstance().getFlowListeners().remove(listener);
        }
        verify(engineMock, never()).execute(any());
        verify(pollingService).delete(POLL_TOKEN);
        verify(pollingService, never()).markSuccess(anyString());
        assertNull(context.getPollToken());
    }

    @Test
    public void testUnknownFlowIdDeletesNothing() throws Exception {

        String otfi = UUID.randomUUID().toString();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(otfi)).thenThrow(
                new FlowEngineClientException("FE-60001", "Invalid or expired flow id.", "Invalid flow id."));

        try {
            FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, otfi, null, FLOW_TYPE,
                    new HashMap<>());
            fail("An unknown flow id must be rejected.");
        } catch (FlowEngineClientException e) {
            assertEquals(e.getErrorCode(), "FE-60001");
        }
        verify(pollingService, never()).delete(anyString());
    }

    @Test
    public void testFlowWithoutPollingTouchesNoEntry() throws Exception {

        FlowExecutionContext context = newContext();
        String flowId = context.getContextIdentifier();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(flowId)).thenReturn(context);
        when(engineMock.execute(context)).thenThrow(new FlowEngineClientException("FE-60006", "Error.", "Error."));

        try {
            FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, flowId, null, FLOW_TYPE,
                    new HashMap<>());
            fail("The error must fail the request.");
        } catch (FlowEngineClientException e) {
            assertEquals(e.getErrorCode(), "FE-60006");
        }
        verify(pollingService, never()).delete(anyString());
        verify(pollingService, never()).markSuccess(anyString());
    }

    private void assertErrorDeletesTheEntry(FlowEngineException error) throws Exception {

        String otfi = UUID.randomUUID().toString();
        FlowExecutionContext context = retainedContext();
        utils.when(() -> FlowExecutionEngineUtils.retrieveFlowContextFromCache(otfi)).thenReturn(context);
        when(engineMock.execute(context)).thenAnswer(invocation -> {
            context.endPolling(true);
            throw error;
        });

        try {
            FlowExecutionService.getInstance().executeFlow(TENANT_DOMAIN, null, otfi, null, FLOW_TYPE,
                    new HashMap<>());
            fail("The error must end the flow.");
        } catch (FlowEngineException e) {
            assertEquals(e.getErrorCode(), error.getErrorCode());
        }
        utils.verify(() -> FlowExecutionEngineUtils.removeFlowContextFromCache(otfi));
        verify(pollingService).delete(POLL_TOKEN);
        verify(pollingService, never()).markSuccess(anyString());
        assertNull(context.getPollToken());
        assertNull(context.getPollingState());
    }

    private static FlowExecutionContext newContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain(TENANT_DOMAIN);
        context.setGraphConfig(new GraphConfig());
        context.setContextIdentifier(UUID.randomUUID().toString());
        context.setFlowType(FLOW_TYPE);
        context.setCurrentNode(new NodeConfig.Builder().id("device-node").type(TASK_EXECUTION).build());
        return context;
    }

    private static FlowExecutionContext pollingContext() {

        FlowExecutionContext context = newContext();
        context.startPolling();
        context.setPollToken(POLL_TOKEN);
        context.setPollingState(PollingState.IN_PROGRESS);
        return context;
    }

    private static FlowExecutionContext retainedContext() {

        FlowExecutionContext context = pollingContext();
        context.setProperty(RETAIN_OTFI, new OTFIRetention.Builder().executorName(EXECUTOR_NAME).build());
        return context;
    }

    private static FlowExecutionStep incompleteStep(String flowId) {

        return new FlowExecutionStep.Builder().flowId(flowId).flowStatus("INCOMPLETE").stepType("VIEW").build();
    }

    private static FlowExecutionStep pollStep(String flowId) {

        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("qrCodeData", "qr");
        return new FlowExecutionStep.Builder().flowId(flowId).flowStatus("INCOMPLETE").stepType("POLL")
                .data(new DataDTO.Builder().additionalData(additionalData).build()).build();
    }
}
