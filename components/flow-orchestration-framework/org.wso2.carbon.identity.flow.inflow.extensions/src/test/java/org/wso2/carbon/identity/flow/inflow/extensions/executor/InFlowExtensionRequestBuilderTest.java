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

package org.wso2.carbon.identity.flow.inflow.extensions.executor;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.inflow.extensions.model.*;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.inflow.extensions.InFlowExtensionConstants;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link InFlowExtensionRequestBuilder}.
 */
public class InFlowExtensionRequestBuilderTest {

    private InFlowExtensionRequestBuilder requestBuilder;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMock;
    private MockedStatic<LoggerUtils> loggerUtilsMock;

    @BeforeMethod
    public void setUp() {

        requestBuilder = new InFlowExtensionRequestBuilder();
        identityTenantUtilMock = mockStatic(IdentityTenantUtil.class);
        identityTenantUtilMock.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
        loggerUtilsMock = mockStatic(LoggerUtils.class);
        loggerUtilsMock.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
    }

    @AfterMethod
    public void tearDown() {

        loggerUtilsMock.close();
        identityTenantUtilMock.close();
    }

    // ========================= getSupportedActionType =========================

    @Test
    public void testGetSupportedActionType() {

        assertEquals(requestBuilder.getSupportedActionType(), ActionType.IN_FLOW_EXTENSION);
    }

    // ========================= buildActionExecutionRequest — basics =========================

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class)
    public void testBuildRequestThrowsWhenFlowExecutionContextMissing()
            throws ActionExecutionRequestBuilderException {

        FlowContext flowContext = FlowContext.create();
        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);
    }

    @Test
    public void testBuildRequestWithMinimalContext() throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        assertNotNull(request);
        assertEquals(request.getActionType(), ActionType.IN_FLOW_EXTENSION);
        assertNotNull(request.getEvent());
    }

    @Test
    public void testBuildRequestUsesEmptyExposeWhenExposeIsNull()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createFullFlowExecutionContext();
        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        // With empty expose, no context areas should be included in the event.
        InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
        assertNotNull(event);
        assertNull(event.getFlowType());
        // flowProperties defaults to emptyMap() in the builder — verify it is empty.
        assertTrue(event.getFlowProperties().isEmpty());
    }

    // ========================= buildAllowedOperations (from modify) =========================

    @Test
    public void testBuildRequestWithValidModifyPaths()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        AccessConfig accessConfig = new AccessConfig(null,
                Arrays.asList(new ContextPath("/properties/riskScore", false)));

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        List<AllowedOperation> ops = request.getAllowedOperations();
        assertNotNull(ops);
        // REPLACE + REDIRECT (REDIRECT is always advertised).
        assertEquals(ops.size(), 2);
        AllowedOperation replaceOp = findOperation(ops, Operation.REPLACE);
        assertNotNull(replaceOp, "REPLACE should be present when modify paths are configured");
        assertTrue(replaceOp.getPaths().contains("/properties/riskScore"));
        assertNotNull(findOperation(ops, Operation.REDIRECT),
                "REDIRECT should always be present");
    }

    @Test
    public void testBuildRequestWithNoAccessConfig()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        // No action → no access config → no modify paths → only REDIRECT (always present).
        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        List<AllowedOperation> ops = request.getAllowedOperations();
        assertNotNull(ops);
        assertEquals(ops.size(), 1);
        assertEquals(ops.get(0).getOp(), Operation.REDIRECT);
    }

    @Test
    public void testBuildRequestWithEmptyModifyPaths()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        AccessConfig accessConfig = new AccessConfig(null, Arrays.asList());

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        List<AllowedOperation> ops = request.getAllowedOperations();
        assertNotNull(ops);
        // No modify paths → only REDIRECT.
        assertEquals(ops.size(), 1);
        assertEquals(ops.get(0).getOp(), Operation.REDIRECT);
    }

    // ========================= REDIRECT always advertised =========================

    @Test
    public void testRedirectIsAdvertisedAlongsideReplace()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        AccessConfig accessConfig = new AccessConfig(null,
                Arrays.asList(new ContextPath("/properties/riskScore", false)));

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        AllowedOperation redirectOp = findOperation(request.getAllowedOperations(), Operation.REDIRECT);
        assertNotNull(redirectOp);
        // REDIRECT does not target a path — paths must be null or empty.
        assertTrue(redirectOp.getPaths() == null || redirectOp.getPaths().isEmpty(),
                "REDIRECT must not carry any paths");
    }

    @Test
    public void testRedirectIsAdvertisedWhenNoAccessConfig()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        // REDIRECT must be available even without any modify config so extensions can always
        // signal mid-flow redirects.
        AllowedOperation redirectOp = findOperation(request.getAllowedOperations(), Operation.REDIRECT);
        assertNotNull(redirectOp);
    }

    // ========================= Path annotation stripping =========================

    @Test
    @SuppressWarnings("unchecked")
    public void testPathAnnotationStrippingSimpleArray()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        AccessConfig accessConfig = new AccessConfig(null,
                Arrays.asList(new ContextPath("/properties/riskFactors{[String]}", false)));

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        // AllowedOperation should have clean path (without {[String]}).
        AllowedOperation op = findOperation(request.getAllowedOperations(), Operation.REPLACE);
        assertNotNull(op);
        assertTrue(op.getPaths().contains("/properties/riskFactors"));
        assertFalse(op.getPaths().contains("/properties/riskFactors{[String]}"));

        // Annotations should be stored in FlowContext.
        Map<String, String> annotations = flowContext.getValue(
                InFlowExtensionConstants.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
        assertNotNull(annotations);
        assertEquals(annotations.get("/properties/riskFactors"), "[String]");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPathAnnotationStrippingSchemaAnnotation()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        AccessConfig accessConfig = new AccessConfig(null,
                Arrays.asList(new ContextPath("/properties/items{name: String, count: Integer}", false)));

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        AllowedOperation op = findOperation(request.getAllowedOperations(), Operation.REPLACE);
        assertNotNull(op);
        assertTrue(op.getPaths().contains("/properties/items"));
        assertFalse(op.getPaths().contains("/properties/items{name: String, count: Integer}"));

        Map<String, String> annotations = flowContext.getValue(
                InFlowExtensionConstants.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
        assertEquals(annotations.get("/properties/items"), "name: String, count: Integer");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPathWithoutAnnotationNotStoredInAnnotationsMap()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        AccessConfig accessConfig = new AccessConfig(null,
                Arrays.asList(new ContextPath("/properties/riskScore", false)));

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        requestBuilder.buildActionExecutionRequest(flowContext, mockReqCtx(accessConfig, null));

        // No annotations should be stored when paths have no annotations.
        Map<String, String> annotations = flowContext.getValue(
                InFlowExtensionConstants.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
        assertNull(annotations);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleAnnotatedPaths()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        AccessConfig accessConfig = new AccessConfig(null, Arrays.asList(
                new ContextPath("/properties/riskScore", false),
                new ContextPath("/properties/riskFactors{[String]}", false),
                new ContextPath("/properties/items{name: String, count: Integer}", false)));

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        AllowedOperation op = findOperation(request.getAllowedOperations(), Operation.REPLACE);
        assertNotNull(op);
        assertEquals(op.getPaths().size(), 3);
        assertTrue(op.getPaths().contains("/properties/riskScore"));
        assertTrue(op.getPaths().contains("/properties/riskFactors"));
        assertTrue(op.getPaths().contains("/properties/items"));

        Map<String, String> annotations = flowContext.getValue(
                InFlowExtensionConstants.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
        assertEquals(annotations.size(), 2);
        assertEquals(annotations.get("/properties/riskFactors"), "[String]");
        assertEquals(annotations.get("/properties/items"), "name: String, count: Integer");
    }

    // ========================= Expose and modify independence =========================

    @Test
    public void testModifyPathsDoNotAffectExpose()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createFullFlowExecutionContext();
        // Expose only /input/ and /flow/ — /properties/ is NOT exposed.
        // Modify path targets /properties/riskScore — but this should NOT auto-expose it.
        AccessConfig accessConfig = new AccessConfig(
                Arrays.asList(
                        new ContextPath("/input/", false),
                        new ContextPath("/flow/", false)),
                Arrays.asList(new ContextPath("/properties/riskScore", false)));

        execCtx.setProperty("riskScore", "50");

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        // Modify paths should produce a REPLACE allowed op alongside the always-present REDIRECT.
        List<AllowedOperation> ops = request.getAllowedOperations();
        assertEquals(ops.size(), 2);
        assertNotNull(findOperation(ops, Operation.REPLACE));
        assertNotNull(findOperation(ops, Operation.REDIRECT));

        // Properties should NOT be in event — expose and modify are independent.
        InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
        assertTrue(event.getFlowProperties() == null || event.getFlowProperties().isEmpty());
    }

    @Test
    public void testMultipleModifyPathsProduceSingleReplaceOperation()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createFullFlowExecutionContext();
        AccessConfig accessConfig = new AccessConfig(null, Arrays.asList(
                new ContextPath("/properties/riskScore", false),
                new ContextPath("/properties/riskLevel", false),
                new ContextPath("/user/claims/http://wso2.org/claims/email", false)));

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        // All modify paths grouped into a single REPLACE op (REDIRECT is always added too).
        List<AllowedOperation> ops = request.getAllowedOperations();
        assertEquals(ops.size(), 2);
        AllowedOperation replaceOp = findOperation(ops, Operation.REPLACE);
        assertNotNull(replaceOp);
        assertEquals(replaceOp.getPaths().size(), 3);
        assertNotNull(findOperation(ops, Operation.REDIRECT));
    }

    // ========================= Expose filtering =========================

    @Test
    public void testExposeFilteringOnlyExposedAreaIncluded()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createFullFlowExecutionContext();
        // Only expose /flow/ — no user, no properties, no input.
        AccessConfig accessConfig = new AccessConfig(Arrays.asList(
                new ContextPath("/flow/tenantDomain", false),
                new ContextPath("/flow/flowType", false)), null);

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
        // User should NOT be in the event since /user/ is not exposed.
        assertNull(event.getUser());
        // Flow type should be present.
        assertNotNull(event.getFlowType());
        // Tenant should be present.
        assertNotNull(event.getTenant());
    }

    @Test
    public void testFlowPortalUrlExposed()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createFullFlowExecutionContext();
        execCtx.setPortalUrl("https://localhost:9443/accounts/recovery");

        AccessConfig accessConfig = new AccessConfig(Arrays.asList(
                new ContextPath("/flow/portalUrl", false)), null);

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
        assertEquals(event.getPortalUrl(), "https://localhost:9443/accounts/recovery");
    }

    @Test
    public void testFlowPortalUrlNotExposedYieldsNull()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createFullFlowExecutionContext();
        execCtx.setPortalUrl("https://localhost:9443/accounts/recovery");

        // /flow/portalUrl NOT in expose list — must be omitted from the event even when
        // the context has a value, mirroring the rest of the expose-gated paths.
        AccessConfig accessConfig = new AccessConfig(Arrays.asList(
                new ContextPath("/flow/flowType", false)), null);

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
        assertNull(event.getPortalUrl());
    }

    @Test
    public void testFlowCallbackUrlExposed()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createFullFlowExecutionContext();
        execCtx.setCallbackUrl("https://example.com/callback");

        AccessConfig accessConfig = new AccessConfig(Arrays.asList(
                new ContextPath("/flow/callbackUrl", false)), null);

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
        assertEquals(event.getCallbackUrl(), "https://example.com/callback");
    }

    @Test
    public void testExposeFilteringSpecificClaim()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createFullFlowExecutionContext();
        // Only expose a specific claim.
        AccessConfig accessConfig = new AccessConfig(Arrays.asList(
                new ContextPath("/user/claims/http://wso2.org/claims/email", false),
                new ContextPath("/user/userId", false)), null);

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                flowContext, mockReqCtx(accessConfig, null));

        InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
        assertNotNull(event.getUser());
        // Only the email claim should be present, not the country claim.
        List<?> claims = event.getUser().getClaims();
        assertEquals(claims.size(), 1);
    }

    // ========================= Outbound encryption of properties and inputs =========================

    @Test
    public void testPropertiesEncryptedWhenExposePathMarkedEncrypted()
            throws ActionExecutionRequestBuilderException {

        try (MockedStatic<JWEEncryptionUtil> jweUtilMock = mockStatic(JWEEncryptionUtil.class)) {
            jweUtilMock.when(() -> JWEEncryptionUtil.encrypt(anyString(), anyString()))
                    .thenAnswer(inv -> "encrypted." + inv.getArgument(0) + ".jwe.part.four");

            FlowExecutionContext execCtx = createFullFlowExecutionContext();
            execCtx.setProperty("riskScore", "85");

            // riskScore is expose-encrypted; existingProp is exposed plaintext.
            AccessConfig accessConfig = new AccessConfig(
                    Arrays.asList(
                            new ContextPath("/properties/riskScore", true),
                            new ContextPath("/properties/existingProp", false)),
                    null);

            Encryption encryption = new Encryption(
                    new Certificate.Builder().id("cert-1").name("test")
                            .certificateContent("test-cert-pem").build());

            FlowContext flowContext = FlowContext.create()
                    .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

            ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                    flowContext, mockReqCtx(accessConfig, encryption));

            InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
            assertNotNull(event.getFlowProperties());
            // riskScore should be encrypted.
            Object riskScoreValue = event.getFlowProperties().get("riskScore");
            assertNotNull(riskScoreValue);
            assertTrue(riskScoreValue.toString().startsWith("encrypted."),
                    "Property value should be encrypted when expose path is marked encrypted");
            // existingProp is NOT marked encrypted — should remain plaintext.
            assertEquals(event.getFlowProperties().get("existingProp"), "existingValue");
        }
    }

    @Test
    public void testClaimEncryptedWhenExposePathMarkedEncrypted()
            throws ActionExecutionRequestBuilderException {

        try (MockedStatic<JWEEncryptionUtil> jweUtilMock = mockStatic(JWEEncryptionUtil.class)) {
            jweUtilMock.when(() -> JWEEncryptionUtil.encrypt(anyString(), anyString()))
                    .thenAnswer(inv -> "encrypted." + inv.getArgument(0) + ".jwe.part.four");

            FlowExecutionContext execCtx = createFullFlowExecutionContext();

            // email claim is expose-encrypted; country claim is exposed plaintext.
            AccessConfig accessConfig = new AccessConfig(
                    Arrays.asList(
                            new ContextPath("/user/claims/http://wso2.org/claims/email", true),
                            new ContextPath("/user/claims/http://wso2.org/claims/country", false)),
                    null);

            Encryption encryption = new Encryption(
                    new Certificate.Builder().id("cert-1").name("test")
                            .certificateContent("test-cert-pem").build());

            FlowContext flowContext = FlowContext.create()
                    .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

            ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                    flowContext, mockReqCtx(accessConfig, encryption));

            InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
            assertNotNull(event.getUser());
            List<?> claims = event.getUser().getClaims();
            assertEquals(claims.size(), 2);

            org.wso2.carbon.identity.action.execution.api.model.UserClaim emailClaim = null;
            org.wso2.carbon.identity.action.execution.api.model.UserClaim countryClaim = null;
            for (Object c : claims) {
                org.wso2.carbon.identity.action.execution.api.model.UserClaim uc =
                        (org.wso2.carbon.identity.action.execution.api.model.UserClaim) c;
                if ("http://wso2.org/claims/email".equals(uc.getUri())) {
                    emailClaim = uc;
                } else if ("http://wso2.org/claims/country".equals(uc.getUri())) {
                    countryClaim = uc;
                }
            }

            assertNotNull(emailClaim, "email claim should be present");
            assertTrue(emailClaim.getValue().toString().startsWith("encrypted."),
                    "email claim should be encrypted when expose path is marked encrypted");

            assertNotNull(countryClaim, "country claim should be present");
            assertEquals(countryClaim.getValue().toString(), "US",
                    "country claim should remain plaintext when expose path is not marked encrypted");
        }
    }

    @Test
    public void testCredentialEncryptedWhenExposePathMarkedEncrypted()
            throws ActionExecutionRequestBuilderException {

        try (MockedStatic<JWEEncryptionUtil> jweUtilMock = mockStatic(JWEEncryptionUtil.class)) {
            jweUtilMock.when(() -> JWEEncryptionUtil.encrypt(anyString(), anyString()))
                    .thenAnswer(inv -> "encrypted." + inv.getArgument(0) + ".jwe.part.four");

            FlowExecutionContext execCtx = createFullFlowExecutionContext();
            Map<String, char[]> creds = new java.util.HashMap<>();
            creds.put("password", "secret123".toCharArray());
            execCtx.getFlowUser().setUserCredentials(creds);

            // password credential is expose-encrypted.
            AccessConfig accessConfig = new AccessConfig(
                    Arrays.asList(new ContextPath("/user/credentials/password", true)),
                    null);

            Encryption encryption = new Encryption(
                    new Certificate.Builder().id("cert-1").name("test")
                            .certificateContent("test-cert-pem").build());

            FlowContext flowContext = FlowContext.create()
                    .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

            ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(
                    flowContext, mockReqCtx(accessConfig, encryption));

            InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
            assertNotNull(event.getUser());
            Map<String, char[]> eventCreds = event.getUser().getUserCredentials();
            assertNotNull(eventCreds);
            assertTrue(eventCreds.containsKey("password"));
            String credValue = new String(eventCreds.get("password"));
            assertTrue(credValue.startsWith("encrypted."),
                    "credential should be encrypted when expose path is marked encrypted");
        }
    }

    // ========================= Outbound encryption failure =========================

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class)
    public void testEncryptionFailureThrowsException()
            throws ActionExecutionRequestBuilderException {

        try (MockedStatic<JWEEncryptionUtil> jweUtilMock = mockStatic(JWEEncryptionUtil.class)) {
            jweUtilMock.when(() -> JWEEncryptionUtil.encrypt(anyString(), anyString()))
                    .thenThrow(new RuntimeException("Encryption failed"));

            FlowExecutionContext execCtx = createFullFlowExecutionContext();
            execCtx.setProperty("riskScore", "85");

            AccessConfig accessConfig = new AccessConfig(
                    Arrays.asList(new ContextPath("/properties/riskScore", true)),
                    null);

            Encryption encryption = new Encryption(
                    new Certificate.Builder().id("cert-1").name("test")
                            .certificateContent("test-cert-pem").build());

            FlowContext flowContext = FlowContext.create()
                    .add(InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

            requestBuilder.buildActionExecutionRequest(flowContext, mockReqCtx(accessConfig, encryption));
        }
    }

    // ========================= Helper methods =========================

    /**
     * Locate the first AllowedOperation in {@code ops} matching {@code op}, or null if absent.
     */
    private AllowedOperation findOperation(List<AllowedOperation> ops, Operation op) {

        if (ops == null) {
            return null;
        }
        for (AllowedOperation candidate : ops) {
            if (candidate.getOp() == op) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Create a mock ActionExecutionRequestContext whose getAction() returns an InFlowExtensionAction
     * configured with the given access config and encryption.
     */
    private ActionExecutionRequestContext mockReqCtx(AccessConfig accessConfig, Encryption encryption) {

        InFlowExtensionAction action = mock(InFlowExtensionAction.class);
        when(action.resolveAccessConfig(any())).thenReturn(accessConfig);
        when(action.getEncryption()).thenReturn(encryption);
        when(action.getName()).thenReturn("Test Action");
        ActionExecutionRequestContext ctx = mock(ActionExecutionRequestContext.class);
        when(ctx.getAction()).thenReturn(action);
        return ctx;
    }

    private FlowExecutionContext createMinimalFlowExecutionContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("carbon.super");
        context.setContextIdentifier("test-correlation-id");

        NodeConfig node = new NodeConfig.Builder()
                .id("node1")
                .type("EXECUTION")
                .build();
        context.setCurrentNode(node);

        return context;
    }

    private FlowExecutionContext createFullFlowExecutionContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setTenantDomain("carbon.super");
        context.setContextIdentifier("test-correlation-id");
        context.setApplicationId("app-123");
        context.setFlowType("REGISTRATION");

        NodeConfig node = new NodeConfig.Builder()
                .id("execution_node_1")
                .type("EXECUTION")
                .build();
        context.setCurrentNode(node);

        FlowUser flowUser = new FlowUser();
        flowUser.setUserId("user-456");
        flowUser.setUsername("testuser");
        flowUser.setUserStoreDomain("PRIMARY");
        flowUser.addClaim("http://wso2.org/claims/email", "test@example.com");
        flowUser.addClaim("http://wso2.org/claims/country", "US");
        context.setFlowUser(flowUser);

        context.addUserInputData("username", "testuser");
        context.addUserInputData("consent", "true");

        context.setProperty("existingProp", "existingValue");

        return context;
    }
}
