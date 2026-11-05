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

package org.wso2.carbon.identity.flow.extension.executor;

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
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowVariableResolver;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants;
import org.wso2.carbon.identity.flow.extension.internal.FlowExtensionDataHolder;
import org.wso2.carbon.identity.flow.extension.model.ContextPath;
import org.wso2.carbon.identity.flow.extension.model.OperationExecutionResult;
import org.wso2.carbon.identity.flow.mgt.model.FlowRuntimeValueDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link FlowExtensionResponseProcessor}: REPLACE operation handling for user
 * claims and credentials, per-operation validation, INCOMPLETE/ERROR/FAILURE handling, and the
 * inbound JWE decryption contract gates.
 */
public class FlowExtensionResponseProcessorTest {

    private static final String TENANT = "carbon.super";
    private static final String GIVEN_NAME_CLAIM = "http://wso2.org/claims/givenname";

    private FlowExtensionResponseProcessor processor;
    private ClaimMetadataManagementService claimService;
    private MockedStatic<LoggerUtils> loggerUtils;

    @BeforeMethod
    public void setUp() {

        // LoggerUtils.isDiagnosticLogsEnabled() touches CarbonContext, which is unavailable in a
        // plain unit test; stub it off so the production diagnostic branches are skipped.
        loggerUtils = mockStatic(LoggerUtils.class);
        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);

        processor = new FlowExtensionResponseProcessor();
        claimService = mock(ClaimMetadataManagementService.class);
        FlowExtensionDataHolder.getInstance().setClaimMetadataManagementService(claimService);
    }

    @AfterMethod
    public void tearDown() {

        FlowExtensionDataHolder.getInstance().setClaimMetadataManagementService(null);
        loggerUtils.close();
    }

    // ------------------------------------------------------------------ helpers

    private FlowContext actionFlowContext() {

        FlowExecutionContext execCtx = new FlowExecutionContext();
        execCtx.setTenantDomain(TENANT);
        return FlowContext.create().add(FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);
    }

    private PerformableOperation replace(String path, Object value) {

        PerformableOperation op = new PerformableOperation();
        op.setOp(Operation.REPLACE);
        op.setPath(path);
        op.setValue(value);
        return op;
    }

    private PerformableOperation redirect(String url) {

        PerformableOperation op = new PerformableOperation();
        op.setOp(Operation.REDIRECT);
        op.setUrl(url);
        return op;
    }

    private ActionExecutionResponseContext<ActionInvocationSuccessResponse> successContext(
            List<PerformableOperation> operations) {

        ActionInvocationSuccessResponse response = new ActionInvocationSuccessResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.SUCCESS)
                .operations(operations)
                .build();
        return ActionExecutionResponseContext.create(null, response);
    }

    private ActionExecutionResponseContext<ActionInvocationIncompleteResponse> incompleteContext(
            List<PerformableOperation> operations) {

        ActionInvocationIncompleteResponse response = new ActionInvocationIncompleteResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.INCOMPLETE)
                .operations(operations)
                .build();
        return ActionExecutionResponseContext.create(null, response);
    }

    // ------------------------------------------------------------------ success: claims / credentials

    @Test
    public void testGetSupportedActionType() {

        assertEquals(processor.getSupportedActionType(), ActionType.FLOW_EXTENSION);
    }

    @Test
    public void testSingleValuedClaimReplaceCollectedAsPending() throws Exception {

        when(claimService.getLocalClaim(eq(GIVEN_NAME_CLAIM), eq(TENANT)))
                .thenReturn(Optional.of(new LocalClaim(GIVEN_NAME_CLAIM)));

        FlowContext actionFlowContext = actionFlowContext();
        List<PerformableOperation> ops = Collections.singletonList(
                replace("/user/claims[uri=" + GIVEN_NAME_CLAIM + "]", "John"));

        ActionExecutionStatus<?> status = processor.processSuccessResponse(actionFlowContext, successContext(ops));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        Object pending = actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_CLAIMS_KEY);
        assertNotNull(pending);
        assertEquals(((Map<?, ?>) pending).get(GIVEN_NAME_CLAIM), "John");
    }

    @Test
    public void testMultiValuedClaimReplaceJoinsValues() throws Exception {

        LocalClaim multiValued = new LocalClaim(GIVEN_NAME_CLAIM);
        multiValued.setClaimProperty("multiValued", "true");
        when(claimService.getLocalClaim(eq(GIVEN_NAME_CLAIM), eq(TENANT)))
                .thenReturn(Optional.of(multiValued));

        FlowContext actionFlowContext = actionFlowContext();
        List<PerformableOperation> ops = Collections.singletonList(
                replace("/user/claims[uri=" + GIVEN_NAME_CLAIM + "]", Arrays.asList("a", "b")));

        processor.processSuccessResponse(actionFlowContext, successContext(ops));

        Map<?, ?> pending = (Map<?, ?>) actionFlowContext.getContextData()
                .get(FlowExtensionConstants.PENDING_CLAIMS_KEY);
        assertEquals(pending.get(GIVEN_NAME_CLAIM), "a,b");
    }

    @Test
    public void testCredentialReplaceCollectedAsPending() throws Exception {

        FlowContext actionFlowContext = actionFlowContext();
        List<PerformableOperation> ops = Collections.singletonList(
                replace("/user/credentials/password", "secret"));

        processor.processSuccessResponse(actionFlowContext, successContext(ops));

        Object pending = actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_CREDENTIALS_KEY);
        assertNotNull(pending);
        char[] stored = (char[]) ((Map<?, ?>) pending).get("password");
        assertEquals(new String(stored), "secret");
    }

    @Test
    public void testEmptyOperationsProduceNoPendingUpdates() throws Exception {

        FlowContext actionFlowContext = actionFlowContext();

        ActionExecutionStatus<?> status =
                processor.processSuccessResponse(actionFlowContext, successContext(Collections.emptyList()));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_CLAIMS_KEY));
        assertNull(actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_CREDENTIALS_KEY));
    }

    // ------------------------------------------------------------------ success: per-operation validation drops

    @Test
    public void testInvalidOperationsAreDroppedButResponseSucceeds() throws Exception {

        // Unknown claim URI -> lookup returns empty -> dropped.
        when(claimService.getLocalClaim(anyString(), eq(TENANT))).thenReturn(Optional.empty());

        FlowContext actionFlowContext = actionFlowContext();
        List<PerformableOperation> ops = Arrays.asList(
                replace("", "x"),                                             // blank path
                replace("/flow/flowType", "x"),                               // read-only
                replace("/unknown/path", "x"),                                // unknown area
                replace("/user/claims[uri=http://example.com/foo]", "x"),     // non-local dialect
                replace("/user/claims[uri=http://wso2.org/claims/identity/x]", "x"), // identity claim
                replace("/user/claims[uri=http://wso2.org/claims/unknown]", "x"),    // unknown local claim
                replace("/user/credentials/", "x"));                          // blank credential key

        ActionExecutionStatus<?> status = processor.processSuccessResponse(actionFlowContext, successContext(ops));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_CLAIMS_KEY));
        assertNull(actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_CREDENTIALS_KEY));
    }

    @Test
    public void testMissingValueOnReplaceIsDropped() throws Exception {

        when(claimService.getLocalClaim(eq(GIVEN_NAME_CLAIM), eq(TENANT)))
                .thenReturn(Optional.of(new LocalClaim(GIVEN_NAME_CLAIM)));

        FlowContext actionFlowContext = actionFlowContext();
        List<PerformableOperation> ops = Collections.singletonList(
                replace("/user/claims[uri=" + GIVEN_NAME_CLAIM + "]", null));

        processor.processSuccessResponse(actionFlowContext, successContext(ops));

        assertNull(actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_CLAIMS_KEY));
    }

    @Test
    public void testSingleValuedClaimWithNonStringValueIsDropped() throws Exception {

        // A wrong-typed value is a per-operation validation failure: the op is dropped and the
        // response still succeeds — a misbehaving extension must not abort the whole flow.
        when(claimService.getLocalClaim(eq(GIVEN_NAME_CLAIM), eq(TENANT)))
                .thenReturn(Optional.of(new LocalClaim(GIVEN_NAME_CLAIM)));

        FlowContext actionFlowContext = actionFlowContext();
        List<PerformableOperation> ops = Collections.singletonList(
                replace("/user/claims[uri=" + GIVEN_NAME_CLAIM + "]", 42));

        ActionExecutionStatus<?> status = processor.processSuccessResponse(actionFlowContext, successContext(ops));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_CLAIMS_KEY));
    }

    @Test
    public void testMultiValuedClaimWithNonListValueIsDropped() throws Exception {

        LocalClaim multiValued = new LocalClaim(GIVEN_NAME_CLAIM);
        multiValued.setClaimProperty("multiValued", "true");
        when(claimService.getLocalClaim(eq(GIVEN_NAME_CLAIM), eq(TENANT)))
                .thenReturn(Optional.of(multiValued));

        FlowContext actionFlowContext = actionFlowContext();
        List<PerformableOperation> ops = Collections.singletonList(
                replace("/user/claims[uri=" + GIVEN_NAME_CLAIM + "]", "not-a-list"));

        ActionExecutionStatus<?> status = processor.processSuccessResponse(actionFlowContext, successContext(ops));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_CLAIMS_KEY));
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testMissingClaimServiceAborts() throws Exception {

        FlowExtensionDataHolder.getInstance().setClaimMetadataManagementService(null);

        FlowContext actionFlowContext = actionFlowContext();
        List<PerformableOperation> ops = Collections.singletonList(
                replace("/user/claims[uri=" + GIVEN_NAME_CLAIM + "]", "John"));

        processor.processSuccessResponse(actionFlowContext, successContext(ops));
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testMissingFlowExecutionContextAborts() throws Exception {

        processor.processSuccessResponse(FlowContext.create(),
                successContext(Collections.emptyList()));
    }

    // ------------------------------------------------------------------ inbound JWE decryption contract

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testEncryptedModifyPathWithNonStringValueAborts() throws Exception {

        FlowContext actionFlowContext = actionFlowContext();
        actionFlowContext.add(FlowExtensionConstants.MODIFY_PATHS_KEY,
                Collections.singletonList(new ContextPath("/user/credentials/token", true)));

        List<PerformableOperation> ops = Collections.singletonList(
                replace("/user/credentials/token", 12345));

        processor.processSuccessResponse(actionFlowContext, successContext(ops));
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testEncryptedModifyPathWithPlaintextValueAborts() throws Exception {

        FlowContext actionFlowContext = actionFlowContext();
        actionFlowContext.add(FlowExtensionConstants.MODIFY_PATHS_KEY,
                Collections.singletonList(new ContextPath("/user/credentials/token", true)));

        List<PerformableOperation> ops = Collections.singletonList(
                replace("/user/credentials/token", "not-a-jwe-value"));

        processor.processSuccessResponse(actionFlowContext, successContext(ops));
    }

    @Test
    public void testEncryptedMultiValuedClaimDecryptsSingleJweAndSplitsOnComma() throws Exception {

        // A multi-valued claim on an encrypted path arrives as a single-element array holding one JWE
        // string that encrypts the comma-joined values; the plaintext is split on commas back into a
        // list and the values joined.
        LocalClaim multiValued = new LocalClaim(GIVEN_NAME_CLAIM);
        multiValued.setClaimProperty("multiValued", "true");
        when(claimService.getLocalClaim(eq(GIVEN_NAME_CLAIM), eq(TENANT)))
                .thenReturn(Optional.of(multiValued));

        String claimPath = "/user/claims[uri=" + GIVEN_NAME_CLAIM + "]";
        FlowContext actionFlowContext = actionFlowContext();
        actionFlowContext.add(FlowExtensionConstants.MODIFY_PATHS_KEY,
                Collections.singletonList(new ContextPath(claimPath, true)));

        List<PerformableOperation> ops = Collections.singletonList(
                replace(claimPath, Collections.singletonList("jwe")));

        try (MockedStatic<JWEEncryptionUtil> jwe = mockStatic(JWEEncryptionUtil.class)) {
            jwe.when(() -> JWEEncryptionUtil.isJWEEncrypted(anyString())).thenReturn(true);
            jwe.when(() -> JWEEncryptionUtil.decrypt("jwe", TENANT)).thenReturn("one,two");

            processor.processSuccessResponse(actionFlowContext, successContext(ops));
        }

        Map<?, ?> pending = (Map<?, ?>) actionFlowContext.getContextData()
                .get(FlowExtensionConstants.PENDING_CLAIMS_KEY);
        assertNotNull(pending);
        assertEquals(pending.get(GIVEN_NAME_CLAIM), "one,two");
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testEncryptedMultiValuedClaimWithNonJweElementAborts() throws Exception {

        String claimPath = "/user/claims[uri=" + GIVEN_NAME_CLAIM + "]";
        FlowContext actionFlowContext = actionFlowContext();
        actionFlowContext.add(FlowExtensionConstants.MODIFY_PATHS_KEY,
                Collections.singletonList(new ContextPath(claimPath, true)));

        List<PerformableOperation> ops = Collections.singletonList(
                replace(claimPath, Collections.singletonList("plaintext-not-jwe")));

        processor.processSuccessResponse(actionFlowContext, successContext(ops));
    }

    // ------------------------------------------------------------------ incomplete

    @Test
    public void testIncompleteResponseStoresRedirectUrl() throws Exception {

        FlowContext actionFlowContext = actionFlowContext();
        ActionExecutionStatus<?> status = processor.processIncompleteResponse(actionFlowContext,
                incompleteContext(Collections.singletonList(redirect("https://redirect"))));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.INCOMPLETE);
        assertEquals(actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_REDIRECT_URL_KEY),
                "https://redirect");
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testIncompleteResponseWithNoOperationsAborts() throws Exception {

        processor.processIncompleteResponse(actionFlowContext(), incompleteContext(Collections.emptyList()));
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testIncompleteResponseWithMultipleOperationsAborts() throws Exception {

        processor.processIncompleteResponse(actionFlowContext(),
                incompleteContext(Arrays.asList(redirect("https://a"), redirect("https://b"))));
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testIncompleteResponseWithNonRedirectOperationAborts() throws Exception {

        processor.processIncompleteResponse(actionFlowContext(),
                incompleteContext(Collections.singletonList(replace("/user/credentials/x", "y"))));
    }

    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testIncompleteResponseWithEmptyRedirectUrlAborts() throws Exception {

        processor.processIncompleteResponse(actionFlowContext(),
                incompleteContext(Collections.singletonList(redirect(""))));
    }

    // ------------------------------------------------------------------ error / failure

    @Test
    public void testProcessErrorResponse() throws Exception {

        ActionInvocationErrorResponse response = new ActionInvocationErrorResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.ERROR)
                .errorMessage("Something went wrong")
                .errorDescription("A detailed description")
                .build();

        ActionExecutionStatus<Error> status = processor.processErrorResponse(
                actionFlowContext(), ActionExecutionResponseContext.create(null, response));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.ERROR);
        assertEquals(status.getResponse().getErrorMessage(), "Something went wrong");
        assertEquals(status.getResponse().getErrorDescription(), "A detailed description");
    }

    @Test
    public void testProcessFailureResponse() throws Exception {

        ActionInvocationFailureResponse response = new ActionInvocationFailureResponse.Builder()
                .actionStatus(ActionInvocationResponse.Status.FAILED)
                .failureReason("Failed reason")
                .failureDescription("Failure description")
                .build();

        ActionExecutionStatus<Failure> status = processor.processFailureResponse(
                actionFlowContext(), ActionExecutionResponseContext.create(null, response));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.FAILED);
        assertEquals(status.getResponse().getFailureReason(), "Failed reason");
        assertEquals(status.getResponse().getFailureDescription(), "Failure description");
    }

    // ------------------------------------------------------------------ runtime values

    private FlowExecutionContext execContextWithFlowVariables() {

        FlowVariablesDTO variables = new FlowVariablesDTO();
        variables.getRuntimeValues().add(new FlowRuntimeValueDTO("plan", FlowVariableDataType.STRING));
        variables.getRuntimeValues().add(new FlowRuntimeValueDTO("attempts", FlowVariableDataType.INTEGER));
        variables.getRuntimeValues().add(new FlowRuntimeValueDTO("tags", FlowVariableDataType.STRING_ARRAY));
        variables.getRuntimeValues().add(new FlowRuntimeValueDTO("abTestingKey", FlowVariableDataType.INTEGER));
        variables.getRuntimeValues().add(new FlowRuntimeValueDTO("score", FlowVariableDataType.DECIMAL));
        variables.getHeaders().add(new FlowVariableDTO("x-device", FlowVariableDataType.STRING_ARRAY));
        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setFlowVariables(variables);

        FlowExecutionContext execCtx = new FlowExecutionContext();
        execCtx.setTenantDomain(TENANT);
        execCtx.setFlowType("REGISTRATION");
        execCtx.setGraphConfig(graphConfig);
        return execCtx;
    }

    private OperationExecutionResult processOperation(PerformableOperation operation, FlowExecutionContext execCtx)
            throws Exception {

        Method method = FlowExtensionResponseProcessor.class.getDeclaredMethod("processOperation",
                PerformableOperation.class, Map.class, Map.class, Map.class, FlowExecutionContext.class);
        method.setAccessible(true);
        try {
            return (OperationExecutionResult) method.invoke(processor, operation, new HashMap<>(), new HashMap<>(),
                    new HashMap<>(), execCtx);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    /**
     * An encrypted array runtime value arrives as one JWE string over the JSON array, so an entry keeps a comma it
     * holds.
     */
    @Test
    public void testEncryptedArrayRuntimeValueIsReadAsJson() throws Exception {

        FlowExecutionContext execCtx = execContextWithFlowVariables();
        FlowContext actionFlowContext =
                FlowContext.create().add(FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);
        actionFlowContext.add(FlowExtensionConstants.MODIFY_PATHS_KEY, Collections.singletonList(
                new ContextPath("/runtimeValues[name=tags]", true, "REGISTRATION")));
        List<PerformableOperation> ops = Collections.singletonList(replace("/runtimeValues[name=tags]", "jwe"));

        try (MockedStatic<JWEEncryptionUtil> jwe = mockStatic(JWEEncryptionUtil.class)) {
            jwe.when(() -> JWEEncryptionUtil.isJWEEncrypted(anyString())).thenReturn(true);
            jwe.when(() -> JWEEncryptionUtil.decrypt("jwe", TENANT)).thenReturn("[\"a,b\",\"c\"]");

            processor.processSuccessResponse(actionFlowContext, successContext(ops));
        }

        Map<?, ?> pending = (Map<?, ?>) actionFlowContext.getContextData()
                .get(FlowExtensionConstants.PENDING_RUNTIME_VALUES_KEY);
        assertNotNull(pending);
        assertEquals(pending.get("tags"), "[\"a,b\",\"c\"]");
        assertTrue(FlowVariableResolver.setRuntimeValue(execCtx, "tags", pending.get("tags")));
        assertEquals(execCtx.getRuntimeValues().get("tags"), Arrays.asList("a,b", "c"));
    }

    /**
     * A decimal that rounds to more digits than the type allows when read is still applied, as the extension's value
     * is staged as it was sent and read once.
     */
    @Test
    public void testRoundedDecimalRuntimeValueIsApplied() throws Exception {

        FlowExecutionContext execCtx = execContextWithFlowVariables();
        FlowContext actionFlowContext =
                FlowContext.create().add(FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);
        List<PerformableOperation> ops =
                Collections.singletonList(replace("/runtimeValues[name=score]", "999999999999999.99"));

        processor.processSuccessResponse(actionFlowContext, successContext(ops));

        Map<?, ?> pending = (Map<?, ?>) actionFlowContext.getContextData()
                .get(FlowExtensionConstants.PENDING_RUNTIME_VALUES_KEY);
        assertNotNull(pending);
        assertEquals(pending.get("score"), "999999999999999.99");
        assertTrue(FlowVariableResolver.setRuntimeValue(execCtx, "score", pending.get("score")));
        assertEquals(execCtx.getRuntimeValues().get("score"), 1.0E15);
    }

    /**
     * An encrypted runtime value is one JWE string, so an array holding one is not read.
     */
    @Test(expectedExceptions = ActionExecutionResponseProcessorException.class)
    public void testEncryptedRuntimeValueInAnArrayIsRejected() throws Exception {

        FlowExecutionContext execCtx = execContextWithFlowVariables();
        FlowContext actionFlowContext =
                FlowContext.create().add(FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);
        actionFlowContext.add(FlowExtensionConstants.MODIFY_PATHS_KEY, Collections.singletonList(
                new ContextPath("/runtimeValues[name=tags]", true, "REGISTRATION")));
        List<PerformableOperation> ops = Collections.singletonList(
                replace("/runtimeValues[name=tags]", Collections.singletonList("jwe")));

        try (MockedStatic<JWEEncryptionUtil> jwe = mockStatic(JWEEncryptionUtil.class)) {
            jwe.when(() -> JWEEncryptionUtil.isJWEEncrypted(anyString())).thenReturn(true);
            jwe.when(() -> JWEEncryptionUtil.decrypt("jwe", TENANT)).thenReturn("[\"a\",\"b\"]");

            processor.processSuccessResponse(actionFlowContext, successContext(ops));
        }
    }

    /**
     * An encrypted primitive runtime value is read for its data type once decrypted.
     */
    @Test
    public void testEncryptedIntegerRuntimeValueIsReadForItsType() throws Exception {

        FlowExecutionContext execCtx = execContextWithFlowVariables();
        FlowContext actionFlowContext =
                FlowContext.create().add(FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);
        actionFlowContext.add(FlowExtensionConstants.MODIFY_PATHS_KEY, Collections.singletonList(
                new ContextPath("/runtimeValues[name=attempts]", true, "REGISTRATION")));
        List<PerformableOperation> ops = Collections.singletonList(replace("/runtimeValues[name=attempts]", "jwe"));

        try (MockedStatic<JWEEncryptionUtil> jwe = mockStatic(JWEEncryptionUtil.class)) {
            jwe.when(() -> JWEEncryptionUtil.isJWEEncrypted(anyString())).thenReturn(true);
            jwe.when(() -> JWEEncryptionUtil.decrypt("jwe", TENANT)).thenReturn("3");

            processor.processSuccessResponse(actionFlowContext, successContext(ops));
        }

        Map<?, ?> pending = (Map<?, ?>) actionFlowContext.getContextData()
                .get(FlowExtensionConstants.PENDING_RUNTIME_VALUES_KEY);
        assertNotNull(pending);
        assertTrue(FlowVariableResolver.setRuntimeValue(execCtx, "attempts", pending.get("attempts")));
        assertEquals(execCtx.getRuntimeValues().get("attempts"), 3L);
    }

    @Test
    public void testRuntimeValueReplaceCollectedAsPending() throws Exception {

        FlowExecutionContext execCtx = execContextWithFlowVariables();
        FlowContext actionFlowContext =
                FlowContext.create().add(FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);
        List<PerformableOperation> ops = Arrays.asList(
                replace("/runtimeValues[name=plan]", "gold"),
                replace("/runtimeValues[name=attempts]", "3"));

        ActionExecutionStatus<?> status = processor.processSuccessResponse(actionFlowContext, successContext(ops));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        Map<?, ?> pending = (Map<?, ?>) actionFlowContext.getContextData()
                .get(FlowExtensionConstants.PENDING_RUNTIME_VALUES_KEY);
        assertNotNull(pending);
        assertEquals(pending.size(), 2);
        assertEquals(pending.get("plan"), "gold");
        assertEquals(pending.get("attempts"), "3");
        // The runtime values change only when the engine applies the pending values.
        assertTrue(execCtx.getRuntimeValues().isEmpty());
        assertTrue(FlowVariableResolver.setRuntimeValue(execCtx, "attempts", pending.get("attempts")));
        assertEquals(execCtx.getRuntimeValues().get("attempts"), 3L);

        assertEquals(processOperation(replace("/runtimeValues[name=plan]", "gold"), execCtx).getStatus(),
                OperationExecutionResult.Status.SUCCESS);
        assertEquals(processOperation(replace("/runtimeValues[name=abTestingKey]", 42), execCtx).getStatus(),
                OperationExecutionResult.Status.SUCCESS);
    }

    /**
     * A runtime value the flow does not define, an A/B testing key out of range, a value not valid for the type, and
     * a malformed path each fail, and the response still succeeds.
     */
    @Test
    public void testInvalidRuntimeValueReplaceFails() throws Exception {

        List<PerformableOperation> ops = Arrays.asList(
                replace("/runtimeValues[name=undefined]", "x"),
                replace("/runtimeValues[name=abTestingKey]", 100),
                replace("/runtimeValues[name=attempts]", "three"),
                replace("/runtimeValues[name=attempts]", null),
                replace("/runtimeValues[name=plan", "gold"));
        FlowExecutionContext execCtx = execContextWithFlowVariables();
        FlowContext actionFlowContext =
                FlowContext.create().add(FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionStatus<?> status = processor.processSuccessResponse(actionFlowContext, successContext(ops));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_RUNTIME_VALUES_KEY));
        for (PerformableOperation op : ops) {
            assertEquals(processOperation(op, execCtx).getStatus(), OperationExecutionResult.Status.FAILURE,
                    op.getPath() + " = " + op.getValue());
        }
    }

    @Test
    public void testRuntimeValueReplaceFailsWhenTheFlowDefinesNone() throws Exception {

        FlowContext actionFlowContext = actionFlowContext();
        PerformableOperation op = replace("/runtimeValues[name=plan]", "gold");

        processor.processSuccessResponse(actionFlowContext, successContext(Collections.singletonList(op)));

        assertNull(actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_RUNTIME_VALUES_KEY));
        assertEquals(processOperation(op, new FlowExecutionContext()).getStatus(),
                OperationExecutionResult.Status.FAILURE);
    }

    @Test
    public void testSubjectOrganizationReplaceFailsAsReadOnly() throws Exception {

        FlowExecutionContext execCtx = execContextWithFlowVariables();
        execCtx.getFlowOrganization().setOrganizationName("Acme");

        for (PerformableOperation op : Arrays.asList(replace("/subjectOrganization/name", "Other"),
                replace("/subjectOrganization/attributes[name=industry]", "Banking"))) {
            OperationExecutionResult result = processOperation(op, execCtx);
            assertEquals(result.getStatus(), OperationExecutionResult.Status.FAILURE, op.getPath());
            assertTrue(result.getMessage().contains("read-only"), result.getMessage());
        }
        assertEquals(execCtx.getFlowOrganization().getOrganizationName(), "Acme");
        assertTrue(execCtx.getFlowOrganization().getAttributes().isEmpty());
    }

    @Test
    public void testRequestHeaderReplaceFailsAsReadOnly() throws Exception {

        FlowExecutionContext execCtx = execContextWithFlowVariables();
        FlowContext actionFlowContext =
                FlowContext.create().add(FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);
        PerformableOperation op = replace("/request/additionalHeaders[name=x-device]", Collections.singletonList("a"));

        ActionExecutionStatus<?> status = processor.processSuccessResponse(actionFlowContext,
                successContext(Collections.singletonList(op)));

        assertEquals(status.getStatus(), ActionExecutionStatus.Status.SUCCESS);
        assertNull(actionFlowContext.getContextData().get(FlowExtensionConstants.PENDING_RUNTIME_VALUES_KEY));
        assertTrue(execCtx.getRequest().getHeaders().isEmpty());
        OperationExecutionResult result = processOperation(op, execCtx);
        assertEquals(result.getStatus(), OperationExecutionResult.Status.FAILURE);
        assertTrue(result.getMessage().contains("read-only"), result.getMessage());
    }
}
