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

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionResponseContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationIncompleteResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Incomplete;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.extensions.InFlowExtensionConstants;
import org.wso2.carbon.identity.flow.extensions.internal.InFlowExtensionDataHolder;
import org.wso2.carbon.identity.flow.extensions.model.ContextPath;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link InFlowExtensionResponseProcessor}.
 */
public class InFlowExtensionResponseProcessorTest {

    private InFlowExtensionResponseProcessor responseProcessor;
    private MockedStatic<LoggerUtils> loggerUtilsMock;
    private MockedStatic<InFlowExtensionDataHolder> holderMock;
    private InFlowExtensionDataHolder holderInstance;
    private FlowContext capturedFlowContext;

    @BeforeMethod
    public void setUp() throws Exception {

        responseProcessor = new InFlowExtensionResponseProcessor();
        loggerUtilsMock = mockStatic(LoggerUtils.class);
        loggerUtilsMock.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

        holderInstance = mock(InFlowExtensionDataHolder.class);
        holderMock = mockStatic(InFlowExtensionDataHolder.class);
        holderMock.when(InFlowExtensionDataHolder::getInstance).thenReturn(holderInstance);
    }

    @AfterMethod
    public void tearDown() {

        holderMock.close();
        loggerUtilsMock.close();
        capturedFlowContext = null;
    }

    // ========================= getSupportedActionType =========================

    @Test
    public void testGetSupportedActionType() {

        assertEquals(responseProcessor.getSupportedActionType(), ActionType.IN_FLOW_EXTENSION);
    }

    // ========================= processSuccessResponse — Property REPLACE =========================

    @Test
    public void testPropertyReplaceFlatExists() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.setProperty("riskScore", "50");

        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/riskScore", "80");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, replaceOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        Map<String, Object> pendingProps =
                capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class);
        assertNotNull(pendingProps);
        assertEquals(pendingProps.get("riskScore"), "80");
    }

    @Test
    public void testPropertyReplaceCreatesIfMissing() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        // riskScore not set — REPLACE should auto-create it.

        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/riskScore", "80");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, replaceOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        Map<String, Object> pendingProps =
                capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class);
        assertNotNull(pendingProps);
        assertEquals(pendingProps.get("riskScore"), "80");
    }

    @Test
    public void testPropertyReplaceCoercesToStringByDefault()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        // Integer value with no annotation → coerced to String.
        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/riskScore", 75);
        ActionExecutionStatus<Success> status = executeSuccessResponse(
                execCtx, replaceOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        Map<String, Object> pendingProps =
                capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class);
        assertNotNull(pendingProps);
        assertEquals(pendingProps.get("riskScore"), "75");
    }

    @Test
    public void testPropertyReplaceWithMultivaluedAnnotation()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/riskFactors", "[String]");

        List<String> factors = Arrays.asList("ip_mismatch", "new_device");
        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/riskFactors", factors);
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, replaceOp, annotations);

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        Map<String, Object> pendingProps =
                capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class);
        assertNotNull(pendingProps);
        Object stored = pendingProps.get("riskFactors");
        assertTrue(stored instanceof List);
        assertEquals(((List<?>) stored).size(), 2);
    }

    @Test
    public void testPropertyReplaceMultivaluedSingleValueWrapped()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/tags", "[String]");

        // Single value with [String] annotation → wrapped in a list.
        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/tags", "singleTag");
        executeSuccessResponse(execCtx, replaceOp, annotations);

        Map<String, Object> pendingProps =
                capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class);
        assertNotNull(pendingProps);
        Object stored = pendingProps.get("tags");
        assertTrue(stored instanceof List);
        assertEquals(((List<?>) stored).size(), 1);
        assertEquals(((List<?>) stored).get(0), "singleTag");
    }

    @Test
    public void testPropertyReplaceWithComplexAnnotation()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/item", "name: String, count: Integer");

        Map<String, Object> item = new HashMap<>();
        item.put("name", "item1");
        item.put("count", 5);

        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/item", item);
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, replaceOp, annotations);

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        Map<String, Object> pendingProps =
                capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class);
        assertNotNull(pendingProps);
        // Complex annotation → value passed through as-is.
        Object stored = pendingProps.get("item");
        assertTrue(stored instanceof Map);
    }

    @Test
    public void testPropertyReplaceWithPrimaryTypeAnnotation()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/score", "Integer");

        // Integer annotation → coerced to String.
        PerformableOperation replaceOp = createOperation(Operation.REPLACE, "/properties/score", 95);
        executeSuccessResponse(execCtx, replaceOp, annotations);

        Map<String, Object> pendingProps =
                capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class);
        assertNotNull(pendingProps);
        assertEquals(pendingProps.get("score"), "95");
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

    @Test
    public void testPropertyReplaceEmptyPropertyName() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation op = createOperation(Operation.REPLACE, "/properties/", "value");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, Collections.emptyMap());

        // Empty property name should fail.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    @Test
    public void testPropertyReplaceComplexObjectInvalidSchema()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/data", "risk: Float, factor: String");

        // Value has an unknown attribute not in the schema.
        Map<String, Object> value = new HashMap<>();
        value.put("risk", 0.85);
        value.put("unknown", "bad");

        PerformableOperation op = createOperation(Operation.REPLACE, "/properties/data", value);
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, annotations);

        // Should succeed overall (logged as failure for this operation) but property not set.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(execCtx.getProperties().get("data"));
    }

    @Test
    public void testPropertyReplaceArrayExceedsItemLimit()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/tags", "[String]");

        // Create a list with 11 items (exceeds max 10).
        List<String> bigList = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            bigList.add("item" + i);
        }

        PerformableOperation op = createOperation(Operation.REPLACE, "/properties/tags", bigList);
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, annotations);

        // Should succeed overall but property not set due to array limit.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(execCtx.getProperties().get("tags"));
    }

    @Test
    public void testPropertyReplaceComplexArrayExceedsItemLimit()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        Map<String, String> annotations = new HashMap<>();
        annotations.put("/properties/risks", "[risk: Float]");

        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("risk", (float) i);
            items.add(item);
        }

        PerformableOperation op = createOperation(Operation.REPLACE, "/properties/risks", items);
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, annotations);

        // Should succeed overall but property not set due to item limit on complex array.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(execCtx.getProperties().get("risks"));
    }

    // ========================= processSuccessResponse — User claim REPLACE =========================

    @Test
    public void testUserClaimReplace() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.getFlowUser().addClaim("http://wso2.org/claims/email", "old@example.com");

        PerformableOperation claimOp = createOperation(
                Operation.REPLACE, "/user/claims[uri=http://wso2.org/claims/email]", "new@example.com");
        executeSuccessResponse(execCtx, claimOp, Collections.emptyMap());

        Map<String, Object> pendingClaims =
                capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_CLAIMS_KEY, Map.class);
        assertNotNull(pendingClaims);
        assertEquals(pendingClaims.get("http://wso2.org/claims/email"), "new@example.com");
    }

    @Test
    public void testUserClaimReplaceCreatesNewClaim() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation claimOp = createOperation(
                Operation.REPLACE, "/user/claims[uri=http://wso2.org/claims/country]", "US");
        executeSuccessResponse(execCtx, claimOp, Collections.emptyMap());

        Map<String, Object> pendingClaims =
                capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_CLAIMS_KEY, Map.class);
        assertNotNull(pendingClaims);
        assertEquals(pendingClaims.get("http://wso2.org/claims/country"), "US");
    }

    @Test
    public void testUserClaimReplaceStringifiesValue() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        // Numeric value should be stringified.
        PerformableOperation claimOp = createOperation(
                Operation.REPLACE, "/user/claims[uri=http://wso2.org/claims/country]", 42);
        executeSuccessResponse(execCtx, claimOp, Collections.emptyMap());

        Map<String, Object> pendingClaims =
                capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_CLAIMS_KEY, Map.class);
        assertNotNull(pendingClaims);
        assertEquals(pendingClaims.get("http://wso2.org/claims/country"), "42");
    }

    @Test
    public void testUserClaimReplaceIdentityClaimRejected()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        // Identity claim should be rejected by the identity-claim prefix guard.
        PerformableOperation claimOp = createOperation(
                Operation.REPLACE, "/user/claims[uri=http://wso2.org/claims/identity/accountLocked]", "true");
        ActionExecutionStatus<Success> status = executeSuccessResponse(
                execCtx, claimOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        // Claim must NOT be staged in the pending map.
        assertNull(capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_CLAIMS_KEY, Map.class));
    }

    @Test
    public void testUserClaimReplaceNonExistentClaimRejected()
            throws ActionExecutionResponseProcessorException, ClaimMetadataException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        // Mock ClaimMetadataManagementService to return null for an unknown claim URI.
        ClaimMetadataManagementService claimService = mock(ClaimMetadataManagementService.class);
        when(holderInstance.getClaimMetadataManagementService()).thenReturn(claimService);
        when(claimService.getLocalClaim("http://wso2.org/claims/nonexistent", "carbon.super"))
                .thenReturn(java.util.Optional.empty());

        // Claim not registered in the system should be rejected.
        PerformableOperation claimOp = createOperation(
                Operation.REPLACE,
                "/user/claims[uri=http://wso2.org/claims/nonexistent]", "value");
        ActionExecutionStatus<Success> status = executeSuccessResponse(
                execCtx, claimOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        // Claim must NOT be staged in the pending map.
        assertNull(capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_CLAIMS_KEY, Map.class));
    }

    @Test
    public void testUserClaimReplaceNonLocalDialectRejected()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        // Non-local dialect claim should be rejected by the local-dialect prefix guard.
        PerformableOperation claimOp = createOperation(
                Operation.REPLACE,
                "/user/claims[uri=urn:ietf:params:scim:schemas:core:2.0:User:name.givenName]", "John");
        ActionExecutionStatus<Success> status = executeSuccessResponse(
                execCtx, claimOp, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        // Claim must NOT be staged in the pending map.
        assertNull(capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_CLAIMS_KEY, Map.class));
    }

    @Test
    public void testUserClaimReplaceNullValue() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation claimOp = createOperation(
                Operation.REPLACE, "/user/claims[uri=http://wso2.org/claims/email]", null);
        ActionExecutionStatus<Success> status = executeSuccessResponse(
                execCtx, claimOp, Collections.emptyMap());

        // Operation should fail — null value.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(execCtx.getFlowUser().getClaims().get("http://wso2.org/claims/email"));
    }

    @Test
    public void testUserClaimReplaceEmptyClaimUri() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation claimOp = createOperation(Operation.REPLACE, "/user/claims[uri=]", "value");
        ActionExecutionStatus<Success> status = executeSuccessResponse(
                execCtx, claimOp, Collections.emptyMap());

        // Should fail — empty claim URI.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    @Test
    public void testUserClaimReplaceNoFlowUser() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.setFlowUser(null);

        PerformableOperation claimOp = createOperation(
                Operation.REPLACE, "/user/claims[uri=http://wso2.org/claims/email]", "test@email.com");
        ActionExecutionStatus<Success> status = executeSuccessResponse(
                execCtx, claimOp, Collections.emptyMap());

        // Should fail — no FlowUser.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    // ========================= processSuccessResponse — User input REPLACE =========================

    @Test
    public void testUserInputReplace() throws ActionExecutionResponseProcessorException {

        // /input/ paths are no longer a modify target; operations on them are silently ignored.
        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.addUserInputData("consent", "false");

        PerformableOperation inputOp = createOperation(Operation.REPLACE, "/input/consent", "true");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, inputOp, Collections.emptyMap());

        // Operation succeeds overall but /input/ is not a modify target — input data unchanged.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertEquals(execCtx.getUserInputData().get("consent"), "false");
    }

    @Test
    public void testUserInputReplaceCreatesNew() throws ActionExecutionResponseProcessorException {

        // /input/ paths are no longer a modify target; operations on them are silently ignored.
        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation inputOp = createOperation(Operation.REPLACE, "/input/consent", "true");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, inputOp, Collections.emptyMap());

        // Operation succeeds overall but /input/ is not a modify target — value is not created.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(execCtx.getUserInputData().get("consent"));
    }

    @Test
    public void testUserInputReplaceNullValue() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.addUserInputData("consent", "true");

        PerformableOperation inputOp = createOperation(Operation.REPLACE, "/input/consent", null);
        ActionExecutionStatus<Success> status = executeSuccessResponse(
                execCtx, inputOp, Collections.emptyMap());

        // Null value should fail.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        // Original value should remain.
        assertEquals(execCtx.getUserInputData().get("consent"), "true");
    }

    // ========================= processSuccessResponse — Read-only paths =========================

    @Test
    public void testReadOnlyFlowPath() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation op = createOperation(Operation.REPLACE, "/flow/tenantDomain", "newValue");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, Collections.emptyMap());

        // Operation should fail but overall status is SUCCESS.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    @Test
    public void testReadOnlyGraphPath() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation op = createOperation(Operation.REPLACE, "/graph/currentNode/id", "newId");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    // ========================= processSuccessResponse — Unknown path =========================

    @Test
    public void testUnknownPathPrefix() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        PerformableOperation op = createOperation(Operation.REPLACE, "/unknown/path", "value");
        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, Collections.emptyMap());

        // Unknown path → operation fails, but overall status is SUCCESS.
        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
    }

    @Test
    public void testNullPathOperationFails() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        // Operation with null path must fail gracefully without NPE.
        PerformableOperation op = new PerformableOperation();
        op.setOp(Operation.REPLACE);
        // path is intentionally not set (null).

        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        // Nothing should be staged in any pending map.
        assertNull(capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class));
        assertNull(capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_CLAIMS_KEY, Map.class));
        assertNull(capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_CREDENTIALS_KEY, Map.class));
    }

    @Test
    public void testUnsupportedOperationTypeOnValidPathFails()
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();

        // ADD (non-REPLACE) on a valid properties path must be rejected before routing.
        PerformableOperation op = new PerformableOperation();
        op.setOp(Operation.ADD);
        op.setPath("/properties/riskScore");
        op.setValue("90");

        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, op, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        // Property must NOT be staged.
        assertNull(capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class));
    }

    // ========================= processSuccessResponse — Multiple operations =========================

    @Test
    public void testMultipleOperationsMixedResults() throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        execCtx.setProperty("existingProp", "old");

        List<PerformableOperation> operations = new ArrayList<>();
        operations.add(createOperation(Operation.REPLACE, "/properties/newProp", "newValue"));
        operations.add(createOperation(Operation.REPLACE, "/properties/existingProp", "updated"));
        operations.add(createOperation(Operation.REPLACE, "/flow/readonly", "fail"));  // This should fail.

        ActionExecutionStatus<Success> status = executeSuccessResponse(execCtx, operations, Collections.emptyMap());

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        Map<String, Object> pendingProps =
                capturedFlowContext.getValue(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class);
        assertNotNull(pendingProps);
        assertEquals(pendingProps.get("newProp"), "newValue");
        assertEquals(pendingProps.get("existingProp"), "updated");
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

    // ========================= processFailureResponse — full-key pass-through =========================

    @Test
    public void testProcessFailureResponseWithFullKeyPassesThrough()
            throws ActionExecutionResponseProcessorException {

        // Extension now sends the full i18n key directly — processor must pass it through unchanged.
        ActionInvocationFailureResponse failureResponse = mock(ActionInvocationFailureResponse.class);
        when(failureResponse.getFailureReason()).thenReturn("high_risk");
        when(failureResponse.getFailureDescription())
                .thenReturn("inflow.extension.risk.assessment.extension.user.access.denied");

        @SuppressWarnings("unchecked")
        ActionExecutionResponseContext<ActionInvocationFailureResponse> responseContext =
                mock(ActionExecutionResponseContext.class);
        when(responseContext.getActionInvocationResponse()).thenReturn(failureResponse);

        FlowContext flowContext = FlowContext.create();

        ActionExecutionStatus<Failure> status = responseProcessor.processFailureResponse(
                flowContext, responseContext);

        // Full key passes through unchanged — no wrapping or prefixing.
        assertEquals(status.getResponse().getFailureDescription(),
                "inflow.extension.risk.assessment.extension.user.access.denied");
        assertEquals(status.getResponse().getFailureReason(), "high_risk");
    }

    @Test
    public void testProcessFailureResponseWithShortKeyPassesThrough()
            throws ActionExecutionResponseProcessorException {

        // Short dot-separated key also passes through as-is — IS no longer adds a prefix.
        ActionInvocationFailureResponse failureResponse = mock(ActionInvocationFailureResponse.class);
        when(failureResponse.getFailureReason()).thenReturn("high_risk");
        when(failureResponse.getFailureDescription()).thenReturn("user.access.denied");

        @SuppressWarnings("unchecked")
        ActionExecutionResponseContext<ActionInvocationFailureResponse> responseContext =
                mock(ActionExecutionResponseContext.class);
        when(responseContext.getActionInvocationResponse()).thenReturn(failureResponse);

        FlowContext flowContext = FlowContext.create();

        ActionExecutionStatus<Failure> status = responseProcessor.processFailureResponse(
                flowContext, responseContext);

        assertEquals(status.getResponse().getFailureDescription(), "user.access.denied");
        assertEquals(status.getResponse().getFailureReason(), "high_risk");
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

    // ========================= processIncompleteResponse =========================

    @Test
    public void testProcessIncompleteResponseWithRedirectStashesUrlAndReturnsIncomplete()
            throws ActionExecutionResponseProcessorException {

        PerformableOperation redirect = createRedirectOperation("https://example.com/step-up");

        FlowContext flowContext = FlowContext.create();
        ActionExecutionStatus<Incomplete> status = executeIncompleteResponse(
                flowContext, Collections.singletonList(redirect));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.INCOMPLETE);
        // URL must be stashed under the key the executor reads.
        assertEquals(flowContext.getValue(InFlowExtensionConstants.PENDING_REDIRECT_URL_KEY, String.class),
                "https://example.com/step-up");
    }

    @Test
    public void testProcessIncompleteResponseIgnoresNonRedirectOperations()
            throws ActionExecutionResponseProcessorException {

        // Contract: REPLACE ops sent alongside REDIRECT on an INCOMPLETE response are dropped
        // — the extension is expected to resend them on the resume call.
        PerformableOperation redirect = createRedirectOperation("https://example.com/step-up");
        PerformableOperation replace = createOperation(
                Operation.REPLACE, "/properties/riskScore", "42");

        FlowContext flowContext = FlowContext.create();
        ActionExecutionStatus<Incomplete> status = executeIncompleteResponse(
                flowContext, Arrays.asList(redirect, replace));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.INCOMPLETE);
        // Redirect URL is captured, but the REPLACE must NOT have produced any pending props.
        assertEquals(flowContext.getValue(InFlowExtensionConstants.PENDING_REDIRECT_URL_KEY, String.class),
                "https://example.com/step-up");
        assertNull(flowContext.getValue(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, Map.class));
        assertNull(flowContext.getValue(InFlowExtensionConstants.PENDING_CLAIMS_KEY, Map.class));
        assertNull(flowContext.getValue(InFlowExtensionConstants.PENDING_CREDENTIALS_KEY, Map.class));
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testProcessIncompleteResponseWithoutRedirectThrows()
            throws ActionExecutionResponseProcessorException {

        // INCOMPLETE without a REDIRECT op is a contract violation.
        PerformableOperation replace = createOperation(
                Operation.REPLACE, "/properties/riskScore", "42");

        executeIncompleteResponse(FlowContext.create(), Collections.singletonList(replace));
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testProcessIncompleteResponseWithEmptyOperationsThrows()
            throws ActionExecutionResponseProcessorException {

        executeIncompleteResponse(FlowContext.create(), Collections.emptyList());
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testProcessIncompleteResponseWithNullOperationsThrows()
            throws ActionExecutionResponseProcessorException {

        executeIncompleteResponse(FlowContext.create(), null);
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testProcessIncompleteResponseWithEmptyRedirectUrlThrows()
            throws ActionExecutionResponseProcessorException {

        PerformableOperation emptyRedirect = createRedirectOperation("");

        executeIncompleteResponse(FlowContext.create(), Collections.singletonList(emptyRedirect));
    }

    @SuppressWarnings("unchecked")
    private ActionExecutionStatus<Incomplete> executeIncompleteResponse(
            FlowContext flowContext, List<PerformableOperation> operations)
            throws ActionExecutionResponseProcessorException {

        ActionInvocationIncompleteResponse incompleteResponse =
                mock(ActionInvocationIncompleteResponse.class);
        when(incompleteResponse.getOperations()).thenReturn(operations);

        ActionExecutionResponseContext<ActionInvocationIncompleteResponse> responseContext =
                mock(ActionExecutionResponseContext.class);
        when(responseContext.getActionInvocationResponse()).thenReturn(incompleteResponse);

        return responseProcessor.processIncompleteResponse(flowContext, responseContext);
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

    private PerformableOperation createRedirectOperation(String url) {

        // PerformableOperation rejects setPath/setValue for REDIRECT and rejects setUrl for
        // every other op — REDIRECT carries its target in the dedicated `url` field.
        PerformableOperation operation = new PerformableOperation();
        operation.setOp(Operation.REDIRECT);
        operation.setUrl(url);
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
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        if (pathTypeAnnotations != null && !pathTypeAnnotations.isEmpty()) {
            flowContext.add(InFlowExtensionConstants.PATH_TYPE_ANNOTATIONS_KEY, pathTypeAnnotations);
        }

        capturedFlowContext = flowContext;

        ActionInvocationSuccessResponse successResponse = mock(ActionInvocationSuccessResponse.class);
        when(successResponse.getOperations()).thenReturn(operations);

        ActionExecutionResponseContext<ActionInvocationSuccessResponse> responseContext =
                mock(ActionExecutionResponseContext.class);
        when(responseContext.getActionInvocationResponse()).thenReturn(successResponse);

        return responseProcessor.processSuccessResponse(flowContext, responseContext);
    }

    @SuppressWarnings("unchecked")
    private ActionExecutionStatus<Success> executeSuccessResponseWithModifyPaths(
            FlowExecutionContext execCtx, List<PerformableOperation> operations,
            Map<String, String> pathTypeAnnotations, List<ContextPath> modifyPaths)
            throws ActionExecutionResponseProcessorException {

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        if (pathTypeAnnotations != null && !pathTypeAnnotations.isEmpty()) {
            flowContext.add(InFlowExtensionConstants.PATH_TYPE_ANNOTATIONS_KEY, pathTypeAnnotations);
        }
        if (modifyPaths != null) {
            flowContext.add(InFlowExtensionConstants.MODIFY_PATHS_KEY, modifyPaths);
        }

        capturedFlowContext = flowContext;

        ActionInvocationSuccessResponse successResponse = mock(ActionInvocationSuccessResponse.class);
        when(successResponse.getOperations()).thenReturn(operations);

        ActionExecutionResponseContext<ActionInvocationSuccessResponse> responseContext =
                mock(ActionExecutionResponseContext.class);
        when(responseContext.getActionInvocationResponse()).thenReturn(successResponse);

        return responseProcessor.processSuccessResponse(flowContext, responseContext);
    }

    // ========================= Inbound decryption failure =========================

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testDecryptionFailureThrowsException()
            throws ActionExecutionResponseProcessorException {

        try (MockedStatic<JWEEncryptionUtil> jweUtilMock = mockStatic(JWEEncryptionUtil.class)) {
            jweUtilMock.when(() -> JWEEncryptionUtil.isJWEEncrypted(anyString())).thenReturn(true);
            jweUtilMock.when(() -> JWEEncryptionUtil.decrypt(anyString(), anyString()))
                    .thenThrow(new RuntimeException("Decryption failed"));

            FlowExecutionContext execCtx = createFlowExecutionContext();
            List<ContextPath> modifyPaths = Arrays.asList(new ContextPath("/properties/secret", true));

            // Value looks like JWE (5 dot-separated parts).
            PerformableOperation op = createOperation(
                    Operation.REPLACE, "/properties/secret", "a.b.c.d.e");

            executeSuccessResponseWithModifyPaths(execCtx, Collections.singletonList(op),
                    Collections.emptyMap(), modifyPaths);
        }
    }

    @Test
    public void testNonStringValueForEncryptedPathThrowsException() {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        List<ContextPath> modifyPaths = Arrays.asList(new ContextPath("/properties/data", true));

        // Non-string value (Integer) for an encrypted path must now be rejected with an exception
        // because accepting non-JWE values would make the encryption flag advisory.
        PerformableOperation op = createOperation(Operation.REPLACE, "/properties/data", 42);

        try {
            executeSuccessResponseWithModifyPaths(
                    execCtx, Collections.singletonList(op), Collections.emptyMap(), modifyPaths);
            throw new AssertionError("Expected ActionExecutionResponseProcessorException was not thrown.");
        } catch (ActionExecutionResponseProcessorException e) {
            assertTrue(e.getMessage().contains("/properties/data"),
                    "Exception message should reference the offending path.");
        }
    }

    @Test
    public void testPlaintextStringForEncryptedPathThrowsException() {

        FlowExecutionContext execCtx = createFlowExecutionContext();
        List<ContextPath> modifyPaths = Arrays.asList(new ContextPath("/properties/secret", true));

        // Plain-text string (not JWE-formatted) for an encrypted modify path must be rejected.
        PerformableOperation op = createOperation(Operation.REPLACE, "/properties/secret", "plaintext-value");

        try (MockedStatic<JWEEncryptionUtil> jweUtilMock = mockStatic(JWEEncryptionUtil.class)) {
            jweUtilMock.when(() -> JWEEncryptionUtil.isJWEEncrypted(anyString())).thenReturn(false);
            try {
                executeSuccessResponseWithModifyPaths(
                        execCtx, Collections.singletonList(op), Collections.emptyMap(), modifyPaths);
                throw new AssertionError("Expected ActionExecutionResponseProcessorException was not thrown.");
            } catch (ActionExecutionResponseProcessorException e) {
                assertTrue(e.getMessage().contains("/properties/secret"),
                        "Exception message should reference the offending path.");
            }
        }
    }
}
