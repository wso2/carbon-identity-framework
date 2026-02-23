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

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionResponseContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link InFlowExtensionResponseProcessor}.
 */
public class InFlowExtensionResponseProcessorTest {

    private InFlowExtensionResponseProcessor responseProcessor;
    private MockedStatic<LoggerUtils> loggerUtilsMock;
    private MockedStatic<FlowExecutionEngineDataHolder> holderMock;
    private ClaimMetadataManagementService claimService;

    @BeforeMethod
    public void setUp() throws Exception {

        responseProcessor = new InFlowExtensionResponseProcessor();
        loggerUtilsMock = mockStatic(LoggerUtils.class);
        loggerUtilsMock.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

        // Mock claim service for claim validation
        claimService = mock(ClaimMetadataManagementService.class);
        LocalClaim emailClaim = mock(LocalClaim.class);
        when(emailClaim.getClaimURI()).thenReturn("http://wso2.org/claims/email");
        LocalClaim countryClaim = mock(LocalClaim.class);
        when(countryClaim.getClaimURI()).thenReturn("http://wso2.org/claims/country");
        when(claimService.getLocalClaims(anyString())).thenReturn(Arrays.asList(emailClaim, countryClaim));

        FlowExecutionEngineDataHolder holderInstance = mock(FlowExecutionEngineDataHolder.class);
        when(holderInstance.getClaimMetadataManagementService()).thenReturn(claimService);
        holderMock = mockStatic(FlowExecutionEngineDataHolder.class);
        holderMock.when(FlowExecutionEngineDataHolder::getInstance).thenReturn(holderInstance);
    }

    @AfterMethod
    public void tearDown() {

        holderMock.close();
        loggerUtilsMock.close();
    }

    // ========================= getSupportedActionType =========================

    @Test
    public void testGetSupportedActionType() {

        assertEquals(responseProcessor.getSupportedActionType(), ActionType.IN_FLOW_EXTENSION);
    }

    // ========================= processSuccessResponse — Property ADD =========================

    @Test
    public void testPropertyAddFlat() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation addOp = createOperation(Operation.ADD, "/properties/riskScore", "75");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, addOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertEquals(execCtx.getProperties().get("riskScore"), "75");
    }

    @Test
    public void testPropertyAddNested() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation addOp = createOperation(Operation.ADD, "/properties/risk/level", "HIGH");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, addOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        Object riskMap = execCtx.getProperties().get("risk");
        assertNotNull(riskMap);
        assertTrue(riskMap instanceof Map);
        assertEquals(((Map<?, ?>) riskMap).get("level"), "HIGH");
    }

    @Test
    public void testPropertyAddWithArrayAnnotation() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/riskFactors", "");

        List<String> factors = Arrays.asList("ip_mismatch", "new_device");
        PerformableOperation addOp = createOperation(Operation.ADD, "/properties/riskFactors", factors);
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, addOp, annotations);

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        Object stored = execCtx.getProperties().get("riskFactors");
        assertTrue(stored instanceof List);
        assertEquals(((List<?>) stored).size(), 2);
    }

    @Test
    public void testPropertyAddWithSchemaAnnotation() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/items", "name,count");

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("name", "item1");
        item.put("count", 5);
        items.add(item);

        PerformableOperation addOp = createOperation(Operation.ADD, "/properties/items", items);
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, addOp, annotations);

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        // Schema annotation → value passed through as-is.
        Object stored = execCtx.getProperties().get("items");
        assertTrue(stored instanceof List);
    }

    @Test
    public void testPropertyAddWithoutAnnotationCoercesToString()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        // Integer value with no annotation → coerced to String.
        PerformableOperation addOp = createOperation(Operation.ADD, "/properties/riskScore", 75);
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, addOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertEquals(execCtx.getProperties().get("riskScore"), "75");
    }

    @Test
    public void testPropertyAddNullValue() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation addOp = createOperation(Operation.ADD, "/properties/riskScore", null);
        // Should still succeed (status is SUCCESS overall) but the individual operation should fail.
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, addOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        // Property should NOT be set since value is null.
        assertFalse(execCtx.getProperties().containsKey("riskScore"));
    }

    @Test
    public void testPropertyAddArrayAnnotationSingleValueWrapped()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/tags", "");

        // Single value with [] annotation → wrapped in a list.
        PerformableOperation addOp = createOperation(Operation.ADD, "/properties/tags", "singleTag");
        executeSuccessResponse(execCtx, addOp, annotations);

        Object stored = execCtx.getProperties().get("tags");
        assertTrue(stored instanceof List);
        assertEquals(((List<?>) stored).size(), 1);
        assertEquals(((List<?>) stored).get(0), "singleTag");
    }

    // ========================= processSuccessResponse — Property REPLACE =========================

    @Test
    public void testPropertyReplaceFlatExists() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.setProperty("riskScore", "50");

        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/riskScore", "80");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, replaceOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertEquals(execCtx.getProperties().get("riskScore"), "80");
    }

    @Test
    public void testPropertyReplaceFlatDoesNotExist() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        // riskScore not set — REPLACE should fail.

        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/riskScore", "80");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, replaceOp, Collections.emptyMap());

        // Overall status is SUCCESS but the property should not be set.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertFalse(execCtx.getProperties().containsKey("riskScore"));
    }

    @Test
    public void testPropertyReplaceNestedExists() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, Object> riskMap = new HashMap<>();
        riskMap.put("score", "50");
        execCtx.setProperty("risk", riskMap);

        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/risk/score", "80");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, replaceOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        @SuppressWarnings("unchecked")
        Map<String, Object> updatedRisk = (Map<String, Object>) execCtx.getProperties().get("risk");
        assertEquals(updatedRisk.get("score"), "80");
    }

    @Test
    public void testPropertyReplaceNestedParentMissing() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        // No "risk" property set.

        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/risk/score", "80");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, replaceOp, Collections.emptyMap());

        // Should fail — nested path doesn't exist.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertFalse(execCtx.getProperties().containsKey("risk"));
    }

    // ========================= processSuccessResponse — Property REMOVE =========================

    @Test
    public void testPropertyRemoveFlat() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.setProperty("riskScore", "50");

        PerformableOperation removeOp = createOperation(Operation.REMOVE, "/properties/riskScore", null);
        executeSuccessResponse(execCtx, removeOp, Collections.emptyMap());

        assertFalse(execCtx.getProperties().containsKey("riskScore"));
    }

    @Test
    public void testPropertyRemoveNested() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, Object> riskMap = new HashMap<>();
        riskMap.put("score", "50");
        riskMap.put("level", "MEDIUM");
        execCtx.setProperty("risk", riskMap);

        PerformableOperation removeOp = createOperation(Operation.REMOVE, "/properties/risk/score", null);
        executeSuccessResponse(execCtx, removeOp, Collections.emptyMap());

        @SuppressWarnings("unchecked")
        Map<String, Object> updatedRisk = (Map<String, Object>) execCtx.getProperties().get("risk");
        assertFalse(updatedRisk.containsKey("score"));
        assertTrue(updatedRisk.containsKey("level"));
    }

    @Test
    public void testPropertyRemoveNestedParentMissing() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        // No "risk" property — remove on nested path should be graceful (no error).
        PerformableOperation removeOp = createOperation(Operation.REMOVE, "/properties/risk/score", null);
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, removeOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    // ========================= processSuccessResponse — User claim operations =========================

    @Test
    public void testUserClaimAdd() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation claimOp = createOperation(
                Operation.ADD, "/user/claims/http://wso2.org/claims/country", "US");
        executeSuccessResponse(execCtx, claimOp, Collections.emptyMap());

        assertEquals(execCtx.getFlowUser().getClaims().get("http://wso2.org/claims/country"), "US");
    }

    @Test
    public void testUserClaimReplace() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.getFlowUser().addClaim("http://wso2.org/claims/email", "old@example.com");

        PerformableOperation claimOp = createOperation(
                Operation.REPLACE, "/user/claims/http://wso2.org/claims/email", "new@example.com");
        executeSuccessResponse(execCtx, claimOp, Collections.emptyMap());

        assertEquals(execCtx.getFlowUser().getClaims().get("http://wso2.org/claims/email"), "new@example.com");
    }

    @Test
    public void testUserClaimRemove() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.getFlowUser().addClaim("http://wso2.org/claims/email", "test@example.com");

        PerformableOperation claimOp = createOperation(
                Operation.REMOVE, "/user/claims/http://wso2.org/claims/email", null);
        executeSuccessResponse(execCtx, claimOp, Collections.emptyMap());

        assertFalse(execCtx.getFlowUser().getClaims().containsKey("http://wso2.org/claims/email"));
    }

    @Test
    public void testUserClaimAddNullValue() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation claimOp = createOperation(
                Operation.ADD, "/user/claims/http://wso2.org/claims/email", null);
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, claimOp, Collections.emptyMap());

        // Operation should fail — null value.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(execCtx.getFlowUser().getClaims().get("http://wso2.org/claims/email"));
    }

    @Test
    public void testUserClaimAddEmptyClaimUri() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation claimOp = createOperation(Operation.ADD, "/user/claims/", "value");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, claimOp, Collections.emptyMap());

        // Should fail — empty claim URI.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    @Test
    public void testUserClaimAddNoFlowUser() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.setFlowUser(null);

        PerformableOperation claimOp = createOperation(
                Operation.ADD, "/user/claims/http://wso2.org/claims/email", "test@email.com");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, claimOp, Collections.emptyMap());

        // Should fail — no FlowUser.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    // ========================= processSuccessResponse — User input operations =========================

    @Test
    public void testUserInputAdd() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation inputOp = createOperation(Operation.ADD, "/input/consent", "true");
        executeSuccessResponse(execCtx, inputOp, Collections.emptyMap());

        assertEquals(execCtx.getUserInputData().get("consent"), "true");
    }

    @Test
    public void testUserInputReplace() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.addUserInputData("consent", "false");

        PerformableOperation inputOp = createOperation(Operation.REPLACE, "/input/consent", "true");
        executeSuccessResponse(execCtx, inputOp, Collections.emptyMap());

        assertEquals(execCtx.getUserInputData().get("consent"), "true");
    }

    @Test
    public void testUserInputRemove() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.addUserInputData("consent", "true");

        PerformableOperation inputOp = createOperation(Operation.REMOVE, "/input/consent", null);
        executeSuccessResponse(execCtx, inputOp, Collections.emptyMap());

        assertFalse(execCtx.getUserInputData().containsKey("consent"));
    }

    // ========================= processSuccessResponse — Read-only paths =========================

    @Test
    public void testReadOnlyFlowPath() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation op = createOperation(Operation.ADD, "/flow/tenantDomain", "newValue");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, Collections.emptyMap());

        // Operation should fail but overall status is SUCCESS.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    @Test
    public void testReadOnlyGraphPath() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation op = createOperation(Operation.ADD, "/graph/currentNode/id", "newId");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    // ========================= processSuccessResponse — Unknown path =========================

    @Test
    public void testUnknownPathPrefix() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation op = createOperation(Operation.ADD, "/unknown/path", "value");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, Collections.emptyMap());

        // Unknown path → operation fails, but overall status is SUCCESS.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    // ========================= processSuccessResponse — Legacy path normalization =========================

    @Test
    public void testLegacyUserInputsPathNormalized() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        // Legacy /userInputs/ path should be normalized to /input/.
        PerformableOperation op = createOperation(Operation.ADD, "/userInputs/legacyField", "value");
        executeSuccessResponse(execCtx, op, Collections.emptyMap());

        assertEquals(execCtx.getUserInputData().get("legacyField"), "value");
    }

    // ========================= processSuccessResponse — Multiple operations =========================

    @Test
    public void testMultipleOperationsMixedResults() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.setProperty("existingProp", "old");

        List<PerformableOperation> operations = new ArrayList<>();
        operations.add(createOperation(Operation.ADD, "/properties/newProp", "newValue"));
        operations.add(createOperation(Operation.REPLACE, "/properties/existingProp", "updated"));
        operations.add(createOperation(Operation.ADD, "/flow/readonly", "fail"));  // This should fail.

        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, operations, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertEquals(execCtx.getProperties().get("newProp"), "newValue");
        assertEquals(execCtx.getProperties().get("existingProp"), "updated");
    }

    @Test
    public void testEmptyOperations() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        ActionExecutionStatus<Success> status = executeSuccessResponse(
                execCtx, Collections.emptyList(), Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    // ========================= processFailureResponse =========================

    @Test
    public void testProcessFailureResponse() throws ActionExecutionResponseProcessorException {

        ActionInvocationFailureResponse failureResponse = mock(ActionInvocationFailureResponse.class);
        when(failureResponse.getFailureReason()).thenReturn("high_risk_detected");
        when(failureResponse.getFailureDescription()).thenReturn("Risk score exceeds threshold");

        @SuppressWarnings("unchecked")
        ActionExecutionResponseContext<ActionInvocationFailureResponse> responseContext =
                mock(ActionExecutionResponseContext.class);
        when(responseContext.getActionInvocationResponse()).thenReturn(failureResponse);

        FlowContext flowContext = FlowContext.create();

        ActionExecutionStatus<Failure> status = responseProcessor.processFailureResponse(
                flowContext, responseContext);

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.FAILED);
        assertNotNull(status.getResponse());
        assertEquals(status.getResponse().getFailureReason(), "high_risk_detected");
        assertEquals(status.getResponse().getFailureDescription(), "Risk score exceeds threshold");
    }

    // ========================= processErrorResponse =========================

    @Test
    public void testProcessErrorResponse() throws ActionExecutionResponseProcessorException {

        ActionInvocationErrorResponse errorResponse = mock(ActionInvocationErrorResponse.class);
        when(errorResponse.getErrorMessage()).thenReturn("internal_error");
        when(errorResponse.getErrorDescription()).thenReturn("Database connection failed");

        @SuppressWarnings("unchecked")
        ActionExecutionResponseContext<ActionInvocationErrorResponse> responseContext =
                mock(ActionExecutionResponseContext.class);
        when(responseContext.getActionInvocationResponse()).thenReturn(errorResponse);

        FlowContext flowContext = FlowContext.create();

        ActionExecutionStatus<Error> status = responseProcessor.processErrorResponse(
                flowContext, responseContext);

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.ERROR);
        assertNotNull(status.getResponse());
        assertEquals(status.getResponse().getErrorMessage(), "internal_error");
        assertEquals(status.getResponse().getErrorDescription(), "Database connection failed");
    }

    // ========================= processSuccessResponse — Invalid property path =========================

    @Test
    public void testPropertyAddEmptyPropertyName() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation op = createOperation(Operation.ADD, "/properties/", "value");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    // ========================= processSuccessResponse — Three-level nesting =========================

    @Test
    public void testPropertyAddThreeLevelNesting() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation addOp = createOperation(
                Operation.ADD, "/properties/deep/nested/value", "deepValue");
        executeSuccessResponse(execCtx, addOp, Collections.emptyMap());

        @SuppressWarnings("unchecked")
        Map<String, Object> deep = (Map<String, Object>) execCtx.getProperties().get("deep");
        assertNotNull(deep);
        @SuppressWarnings("unchecked")
        Map<String, Object> nested = (Map<String, Object>) deep.get("nested");
        assertNotNull(nested);
        assertEquals(nested.get("value"), "deepValue");
    }

    @Test
    public void testPropertyReplaceNullValue() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.setProperty("score", "50");

        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/score", null);
        ActionExecutionStatus<Success> status = executeSuccessResponse(
                execCtx, replaceOp, Collections.emptyMap());

        // Null value should fail for REPLACE.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        // Original value should remain.
        assertEquals(execCtx.getProperties().get("score"), "50");
    }

    // ========================= Helper methods =========================

    private FlowExecutionContext createFlowExecutionContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("carbon.super");
        context.setContextIdentifier("test-id");

        FlowUser flowUser = new FlowUser();
        flowUser.setUserId("user-1");
        flowUser.setUsername("testuser");
        context.setFlowUser(flowUser);

        return context;
    }

    private PerformableOperation createOperation(Operation op, String path, Object value) {

        PerformableOperation operation = new PerformableOperation();
        operation.setOp(op);
        operation.setPath(path);
        if (value != null) {
            operation.setValue(value);
        }
        return operation;
    }

    private ActionExecutionStatus<Success> executeSuccessResponse(
            FlowExecutionContext execCtx, PerformableOperation operation,
            Map<String, String> pathTypeAnnotations)
            throws ActionExecutionResponseProcessorException {

        return executeSuccessResponse(execCtx, Collections.singletonList(operation), pathTypeAnnotations);
    }

    @SuppressWarnings("unchecked")
    private ActionExecutionStatus<Success> executeSuccessResponse(
            FlowExecutionContext execCtx, List<PerformableOperation> operations,
            Map<String, String> pathTypeAnnotations)
            throws ActionExecutionResponseProcessorException {

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        if (pathTypeAnnotations != null && !pathTypeAnnotations.isEmpty()) {
            flowContext.add(InFlowExtensionExecutor.PATH_TYPE_ANNOTATIONS_KEY, pathTypeAnnotations);
        }

        ActionInvocationSuccessResponse successResponse = mock(ActionInvocationSuccessResponse.class);
        when(successResponse.getOperations()).thenReturn(operations);

        ActionExecutionResponseContext<ActionInvocationSuccessResponse> responseContext =
                mock(ActionExecutionResponseContext.class);
        when(responseContext.getActionInvocationResponse()).thenReturn(successResponse);

        return responseProcessor.processSuccessResponse(flowContext, responseContext);
    }
}
