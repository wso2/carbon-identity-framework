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

package org.wso2.carbon.identity.flow.execution.engine.executor;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.executor.InFlowExtensionExecutor.ExecutorResult;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link InFlowExtensionExecutor}.
 */
public class InFlowExtensionExecutorTest {

    private InFlowExtensionExecutor executor;

    @Mock
    private ActionExecutorService actionExecutorService;

    private AutoCloseable mocks;
    private MockedStatic<FlowExecutionEngineDataHolder> holderMock;

    @BeforeMethod
    public void setUp() {

        mocks = MockitoAnnotations.openMocks(this);
        executor = new InFlowExtensionExecutor();

        FlowExecutionEngineDataHolder holderInstance =
                mock(FlowExecutionEngineDataHolder.class);
        when(holderInstance.getActionExecutorService()).thenReturn(actionExecutorService);

        holderMock = mockStatic(FlowExecutionEngineDataHolder.class);
        holderMock.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(holderInstance);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        holderMock.close();
        mocks.close();
    }

    // ========================= getName =========================

    @Test
    public void testGetName() {

        assertEquals(executor.getName(), "ExtensionExecutor");
    }

    // ========================= getInitiationData =========================

    @Test
    public void testGetInitiationData() {

        assertNotNull(executor.getInitiationData());
        assertTrue(executor.getInitiationData().isEmpty());
    }

    // ========================= rollback =========================

    @Test
    public void testRollback() {

        assertNull(executor.rollback(new FlowExecutionContext()));
    }

    // ========================= execute — no actionId =========================

    @Test
    public void testExecuteNoActionId() throws Exception {

        FlowExecutionContext context = createContextWithMetadata(new HashMap<>());

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.COMPLETE.name());
        verify(actionExecutorService, never())
                .execute(any(ActionType.class), anyString(), any(FlowContext.class), anyString());
    }

    @Test
    public void testExecuteEmptyActionId() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.COMPLETE.name());
    }

    // ========================= execute — execution disabled =========================

    @Test
    public void testExecuteDisabledExecution() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(false);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.COMPLETE.name());
        verify(actionExecutorService, never())
                .execute(any(ActionType.class), anyString(), any(FlowContext.class), anyString());
    }

    // ========================= execute — SUCCESS =========================

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteSuccess() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);

        ActionExecutionStatus<Success> successStatus = mock(ActionExecutionStatus.class);
        when(successStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.SUCCESS);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(successStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.COMPLETE.name());
    }

    // ========================= execute — FAILED =========================

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteFailed() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);

        Failure failure = new Failure("risk_detected", "Risk score exceeds threshold");
        ActionExecutionStatus<Failure> failedStatus = mock(ActionExecutionStatus.class);
        when(failedStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.FAILED);
        when(failedStatus.getResponse()).thenReturn(failure);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(failedStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.RETRY.name());
        assertEquals(response.getErrorMessage(), "Risk score exceeds threshold");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteFailedNoDescription() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);

        Failure failure = new Failure("risk_detected", null);
        ActionExecutionStatus<Failure> failedStatus = mock(ActionExecutionStatus.class);
        when(failedStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.FAILED);
        when(failedStatus.getResponse()).thenReturn(failure);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(failedStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.RETRY.name());
        // Falls back to reason when description is null.
        assertEquals(response.getErrorMessage(), "risk_detected");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteFailedBothNull() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);

        Failure failure = new Failure(null, null);
        ActionExecutionStatus<Failure> failedStatus = mock(ActionExecutionStatus.class);
        when(failedStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.FAILED);
        when(failedStatus.getResponse()).thenReturn(failure);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(failedStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.RETRY.name());
        assertEquals(response.getErrorMessage(),
                "The operation could not be completed due to an external service failure.");
    }

    // ========================= execute — ERROR =========================

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteError() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);

        Error error = new Error("internal_error", "DB connection failed");
        ActionExecutionStatus<Error> errorStatus = mock(ActionExecutionStatus.class);
        when(errorStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.ERROR);
        when(errorStatus.getResponse()).thenReturn(error);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(errorStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.RETRY.name());
        assertEquals(response.getErrorMessage(), "DB connection failed");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteErrorNoDescription() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);

        Error error = new Error("internal_error", null);
        ActionExecutionStatus<Error> errorStatus = mock(ActionExecutionStatus.class);
        when(errorStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.ERROR);
        when(errorStatus.getResponse()).thenReturn(error);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(errorStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.RETRY.name());
        assertEquals(response.getErrorMessage(), "internal_error");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteErrorBothNull() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);

        Error error = new Error(null, null);
        ActionExecutionStatus<Error> errorStatus = mock(ActionExecutionStatus.class);
        when(errorStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.ERROR);
        when(errorStatus.getResponse()).thenReturn(error);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(errorStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.RETRY.name());
        assertEquals(response.getErrorMessage(),
                "An unexpected error occurred in the external service.");
    }

    // ========================= execute — INCOMPLETE =========================

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteIncomplete() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);

        ActionExecutionStatus<?> incompleteStatus = mock(ActionExecutionStatus.class);
        when(incompleteStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.INCOMPLETE);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(incompleteStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.USER_INPUT_REQUIRED.name());
    }

    // ========================= execute — null status =========================

    @Test
    public void testExecuteNullStatus() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(null);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.USER_INPUT_REQUIRED.name());
    }

    // ========================= execute — exception =========================

    @Test
    public void testExecuteActionException() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenThrow(new ActionExecutionException("Connection timeout"));

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.RETRY.name());
        assertEquals(response.getErrorMessage(),
                "An error occurred while processing the extension. Please try again.");
    }

    // ========================= execute — service unavailable =========================

    @Test(expectedExceptions = org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException.class)
    public void testExecuteServiceUnavailable() throws Exception {

        // Override holder mock to return null service.
        holderMock.close();

        FlowExecutionEngineDataHolder holderInstance =
                mock(FlowExecutionEngineDataHolder.class);
        when(holderInstance.getActionExecutorService()).thenReturn(null);

        holderMock = mockStatic(FlowExecutionEngineDataHolder.class);
        holderMock.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(holderInstance);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        // Should throw FlowEngineException since service is unavailable.
        executor.execute(context);
    }

    // ========================= execute — expose JSON parsing =========================

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteWithValidExposeJson() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        metadata.put("expose", "[\"/properties\",\"/user/claims\"]");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);

        ActionExecutionStatus<Success> successStatus = mock(ActionExecutionStatus.class);
        when(successStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.SUCCESS);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(successStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.COMPLETE.name());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteWithMalformedExposeJson() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        metadata.put("expose", "not_valid_json");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);

        ActionExecutionStatus<Success> successStatus = mock(ActionExecutionStatus.class);
        when(successStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.SUCCESS);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(successStatus);

        // Should fall back to default expose but not crash.
        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.COMPLETE.name());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteWithAllowedOperationsJson() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        metadata.put("allowedOperations",
                "[{\"op\":\"add\",\"paths\":[\"/properties/riskScore\"]}]");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.IN_FLOW_EXTENSION)).thenReturn(true);

        ActionExecutionStatus<Success> successStatus = mock(ActionExecutionStatus.class);
        when(successStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.SUCCESS);
        when(actionExecutorService.execute(
                eq(ActionType.IN_FLOW_EXTENSION), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(successStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.COMPLETE.name());
    }

    // ========================= execute — no node config =========================

    @Test
    public void testExecuteNoNodeConfig() throws Exception {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("carbon.super");
        // No current node set → getMetadataValue returns null.

        ExecutorResponse response = executor.execute(context);

        // actionId is null → COMPLETE.
        assertEquals(response.getResult(), ExecutorResult.COMPLETE.name());
    }

    @Test
    public void testExecuteNoExecutorDTO() throws Exception {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("carbon.super");

        NodeConfig nodeConfig = new NodeConfig.Builder().build();
        context.setCurrentNode(nodeConfig);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorResult.COMPLETE.name());
    }

    // ========================= ExecutorResult enum =========================

    @Test
    public void testExecutorResultValues() {

        assertEquals(ExecutorResult.values().length, 6);
        assertNotNull(ExecutorResult.valueOf("COMPLETE"));
        assertNotNull(ExecutorResult.valueOf("ERROR"));
        assertNotNull(ExecutorResult.valueOf("USER_ERROR"));
        assertNotNull(ExecutorResult.valueOf("USER_INPUT_REQUIRED"));
        assertNotNull(ExecutorResult.valueOf("EXTERNAL_REDIRECTION"));
        assertNotNull(ExecutorResult.valueOf("RETRY"));
    }

    // ========================= Helper methods =========================

    private FlowExecutionContext createContextWithMetadata(Map<String, String> metadata) {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("carbon.super");

        ExecutorDTO executorDTO = new ExecutorDTO("extensionExecutor", metadata);
        NodeConfig nodeConfig = new NodeConfig.Builder()
                .executorConfig(executorDTO)
                .build();
        context.setCurrentNode(nodeConfig);

        return context;
    }
}
