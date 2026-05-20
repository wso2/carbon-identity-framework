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

package org.wso2.carbon.identity.flow.extensions.executor;

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
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.extensions.InFlowExtensionConstants;
import org.wso2.carbon.identity.flow.execution.engine.Constants.ExecutorStatus;
import org.wso2.carbon.identity.flow.extensions.internal.InFlowExtensionDataHolder;
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
import static org.testng.Assert.assertFalse;
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
    private MockedStatic<InFlowExtensionDataHolder> holderMock;
    private MockedStatic<LoggerUtils> loggerUtilsMock;

    @BeforeMethod
    public void setUp() {

        mocks = MockitoAnnotations.openMocks(this);
        executor = new InFlowExtensionExecutor();

        // Stub InFlowExtensionDataHolder for action executor service.
        InFlowExtensionDataHolder holderInstance = mock(InFlowExtensionDataHolder.class);
        when(holderInstance.getActionExecutorService()).thenReturn(actionExecutorService);
        holderMock = mockStatic(InFlowExtensionDataHolder.class);
        holderMock.when(InFlowExtensionDataHolder::getInstance).thenReturn(holderInstance);

        loggerUtilsMock = mockStatic(LoggerUtils.class);
        loggerUtilsMock.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        loggerUtilsMock.close();
        holderMock.close();
        mocks.close();
    }

    // ========================= getName =========================

    @Test
    public void testGetName() {

        assertEquals(executor.getName(), "InFlowExtensionExecutor");
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

        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
        verify(actionExecutorService, never())
                .execute(any(ActionType.class), anyString(), any(FlowContext.class), anyString());
    }

    @Test
    public void testExecuteEmptyActionId() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
    }

    // ========================= execute — execution disabled =========================

    @Test
    public void testExecuteDisabledExecution() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(false);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
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

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);

        ActionExecutionStatus<Success> successStatus = mock(ActionExecutionStatus.class);
        when(successStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.SUCCESS);
        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(successStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_COMPLETE);
    }

    // ========================= execute — FAILED =========================

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteFailed() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);

        Failure failure = new Failure("risk_detected", "Risk score exceeds threshold");
        ActionExecutionStatus<Failure> failedStatus = mock(ActionExecutionStatus.class);
        when(failedStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.FAILED);
        when(failedStatus.getResponse()).thenReturn(failure);
        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(failedStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_RETRY);
        assertEquals(response.getErrorMessage(), "Risk score exceeds threshold");
        // Verify failureType metadata is set for RETRY.
        assertNotNull(response.getAdditionalInfo());
        assertEquals(response.getAdditionalInfo().get("failureType"), "IN_FLOW_EXTENSION_FAILURE");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteFailedNoDescription() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);

        Failure failure = new Failure("risk_detected", null);
        ActionExecutionStatus<Failure> failedStatus = mock(ActionExecutionStatus.class);
        when(failedStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.FAILED);
        when(failedStatus.getResponse()).thenReturn(failure);
        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(failedStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_RETRY);
        // Falls back to reason when description is null.
        assertEquals(response.getErrorMessage(), "risk_detected");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteFailedBothNull() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);

        Failure failure = new Failure(null, null);
        ActionExecutionStatus<Failure> failedStatus = mock(ActionExecutionStatus.class);
        when(failedStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.FAILED);
        when(failedStatus.getResponse()).thenReturn(failure);
        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(failedStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_RETRY);
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

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);

        Error error = new Error("internal_error", "DB connection failed");
        ActionExecutionStatus<Error> errorStatus = mock(ActionExecutionStatus.class);
        when(errorStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.ERROR);
        when(errorStatus.getResponse()).thenReturn(error);
        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(errorStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
        assertEquals(response.getErrorCode(),
                Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getCode());
        // errorMessage carries the Error reason/code field; errorDescription carries the human-readable text.
        assertEquals(response.getErrorMessage(), "internal_error");
        assertEquals(response.getErrorDescription(), "DB connection failed");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteErrorNoDescription() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);

        Error error = new Error("internal_error", null);
        ActionExecutionStatus<Error> errorStatus = mock(ActionExecutionStatus.class);
        when(errorStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.ERROR);
        when(errorStatus.getResponse()).thenReturn(error);
        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(errorStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
        assertEquals(response.getErrorCode(),
                Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getCode());
        assertEquals(response.getErrorMessage(), "internal_error");
        assertNull(response.getErrorDescription());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteErrorBothNull() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);

        Error error = new Error(null, null);
        ActionExecutionStatus<Error> errorStatus = mock(ActionExecutionStatus.class);
        when(errorStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.ERROR);
        when(errorStatus.getResponse()).thenReturn(error);
        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(errorStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
        assertEquals(response.getErrorCode(),
                Constants.ErrorMessages.ERROR_CODE_INFLOW_EXTENSION_ERROR.getCode());
        // Both fields null → errorMessage and errorDescription remain null; errorCode alone triggers FE-65033 routing.
        assertNull(response.getErrorMessage());
        assertNull(response.getErrorDescription());
    }

    // ========================= execute — INCOMPLETE =========================

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteIncompleteWithoutRedirectUrlReturnsError() throws Exception {

        // INCOMPLETE without a stashed redirect URL is a contract violation —
        // the response processor should normally have thrown, but the executor
        // defends against it as well by returning STATUS_ERROR with a clear message.
        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);

        ActionExecutionStatus<?> incompleteStatus = mock(ActionExecutionStatus.class);
        when(incompleteStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.INCOMPLETE);
        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(incompleteStatus);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
        assertEquals(response.getErrorMessage(),
                "Extension returned INCOMPLETE without a redirect URL.");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteIncompleteWithRedirectUrlReturnsExternalRedirection() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);
        // Set a context identifier so the OTFI collision-guard has something to compare against.
        context.setContextIdentifier("original-flow-id");

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);

        ActionExecutionStatus<?> incompleteStatus = mock(ActionExecutionStatus.class);
        when(incompleteStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.INCOMPLETE);

        // Simulate the response processor stashing the redirect URL into the FlowContext
        // during the actionExecutorService.execute call.
        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenAnswer(invocation -> {
                    FlowContext fc = invocation.getArgument(2);
                    fc.add(InFlowExtensionConstants.PENDING_REDIRECT_URL_KEY,
                            "https://example.com/step-up");
                    return incompleteStatus;
                });

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_EXTERNAL_REDIRECTION);

        // OTFI must be set on contextProperties so FlowExecutionService can swap caches on resume.
        Map<String, Object> ctxProps = response.getContextProperties();
        assertNotNull(ctxProps);
        Object otfi = ctxProps.get(org.wso2.carbon.identity.flow.execution.engine.Constants.OTFI);
        assertNotNull(otfi);
        assertTrue(otfi instanceof String);
        assertFalse(((String) otfi).isEmpty());
        // OTFI must not collide with the original context identifier.
        assertFalse("original-flow-id".equals(otfi));

        // Redirect URL must carry the OTFI as a flowId query parameter.
        Map<String, String> additionalInfo = response.getAdditionalInfo();
        assertNotNull(additionalInfo);
        String redirectUrl = additionalInfo.get(
                org.wso2.carbon.identity.flow.execution.engine.Constants.REDIRECT_URL);
        assertNotNull(redirectUrl);
        assertEquals(redirectUrl, "https://example.com/step-up?flowId=" + otfi);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteIncompleteRedirectAppendsFlowIdWithAmpersandWhenUrlHasQuery()
            throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);
        context.setContextIdentifier("original-flow-id");

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);

        ActionExecutionStatus<?> incompleteStatus = mock(ActionExecutionStatus.class);
        when(incompleteStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.INCOMPLETE);

        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenAnswer(invocation -> {
                    FlowContext fc = invocation.getArgument(2);
                    // URL already has a query string — the executor must use & not ?.
                    fc.add(InFlowExtensionConstants.PENDING_REDIRECT_URL_KEY,
                            "https://example.com/step-up?ref=abc");
                    return incompleteStatus;
                });

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_EXTERNAL_REDIRECTION);
        String otfi = (String) response.getContextProperties()
                .get(org.wso2.carbon.identity.flow.execution.engine.Constants.OTFI);
        String redirectUrl = response.getAdditionalInfo()
                .get(org.wso2.carbon.identity.flow.execution.engine.Constants.REDIRECT_URL);
        assertEquals(redirectUrl, "https://example.com/step-up?ref=abc&flowId=" + otfi);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteIncompleteRedirectEmptyUrlReturnsError() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);

        ActionExecutionStatus<?> incompleteStatus = mock(ActionExecutionStatus.class);
        when(incompleteStatus.getStatus()).thenReturn(ActionExecutionStatus.Status.INCOMPLETE);

        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenAnswer(invocation -> {
                    FlowContext fc = invocation.getArgument(2);
                    fc.add(InFlowExtensionConstants.PENDING_REDIRECT_URL_KEY, "");
                    return incompleteStatus;
                });

        ExecutorResponse response = executor.execute(context);

        // Empty URL is treated the same as missing — defensive STATUS_ERROR.
        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
        assertEquals(response.getErrorMessage(),
                "Extension returned INCOMPLETE without a redirect URL.");
    }

    // ========================= execute — null status =========================

    @Test
    public void testExecuteNullStatus() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);
        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenReturn(null);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
    }

    // ========================= execute — exception =========================

    @Test
    public void testExecuteActionException() throws Exception {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        when(actionExecutorService.isExecutionEnabled(ActionType.FLOW_EXTENSIONS)).thenReturn(true);
        when(actionExecutorService.execute(
                eq(ActionType.FLOW_EXTENSIONS), eq("test-action-001"),
                any(FlowContext.class), eq("carbon.super")))
                .thenThrow(new ActionExecutionException("Connection timeout"));

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
        assertEquals(response.getErrorMessage(),
                "An error occurred while processing the extension. Please try again.");
    }

    // ========================= execute — service unavailable =========================

    @Test(expectedExceptions = org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException.class)
    public void testExecuteServiceUnavailable() throws Exception {

        // Override holder mock to return null service.
        holderMock.close();

        InFlowExtensionDataHolder holderInstance = mock(InFlowExtensionDataHolder.class);
        when(holderInstance.getActionExecutorService()).thenReturn(null);

        holderMock = mockStatic(InFlowExtensionDataHolder.class);
        holderMock.when(InFlowExtensionDataHolder::getInstance).thenReturn(holderInstance);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("actionId", "test-action-001");
        FlowExecutionContext context = createContextWithMetadata(metadata);

        // Should throw FlowEngineException since service is unavailable.
        executor.execute(context);
    }

    // ========================= execute — no node config =========================

    @Test
    public void testExecuteNoNodeConfig() throws Exception {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("carbon.super");
        // No current node set → getMetadataValue returns null.

        ExecutorResponse response = executor.execute(context);

        // actionId is null → missing configuration → ERROR.
        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
    }

    @Test
    public void testExecuteNoExecutorDTO() throws Exception {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("carbon.super");

        NodeConfig nodeConfig = new NodeConfig.Builder().build();
        context.setCurrentNode(nodeConfig);

        ExecutorResponse response = executor.execute(context);

        assertEquals(response.getResult(), ExecutorStatus.STATUS_ERROR);
    }

    // ========================= Helper methods =========================

    private FlowExecutionContext createContextWithMetadata(Map<String, String> metadata) {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("carbon.super");

        ExecutorDTO executorDTO = new ExecutorDTO("InFlowExtensionExecutor", metadata);
        NodeConfig nodeConfig = new NodeConfig.Builder()
                .executorConfig(executorDTO)
                .build();
        context.setCurrentNode(nodeConfig);

        return context;
    }
}
