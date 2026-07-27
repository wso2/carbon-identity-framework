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
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants;
import org.wso2.carbon.identity.flow.extension.internal.FlowExtensionDataHolder;
import org.wso2.carbon.identity.flow.extension.model.ContextPath;

import java.util.Arrays;
import java.util.Collections;
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
}
