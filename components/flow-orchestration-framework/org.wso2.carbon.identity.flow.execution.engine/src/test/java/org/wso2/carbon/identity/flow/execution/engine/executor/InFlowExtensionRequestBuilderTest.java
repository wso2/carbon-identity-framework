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
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.HierarchicalPrefixMatcher;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.InFlowExtensionEvent;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.InFlowExtensionExecutor;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.InFlowExtensionRequestBuilder;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor.JWEEncryptionUtil;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.ContextPath;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.Encryption;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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

    @BeforeMethod
    public void setUp() {

        requestBuilder = new InFlowExtensionRequestBuilder();
        identityTenantUtilMock = mockStatic(IdentityTenantUtil.class);
        identityTenantUtilMock.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
    }

    @AfterMethod
    public void tearDown() {

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
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        assertNotNull(request);
        assertEquals(request.getActionType(), ActionType.IN_FLOW_EXTENSION);
        assertNotNull(request.getEvent());
    }

    @Test
    public void testBuildRequestUsesDefaultExposeWhenExposeIsNull()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createFullFlowExecutionContext();
        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        // With DEFAULT_EXPOSE, all areas are exposed — event should have user, properties, inputs, etc.
        InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
        assertNotNull(event);
        assertNotNull(event.getFlowType());
        assertNotNull(event.getUserInputs());
    }

    // ========================= buildAllowedOperations (from modify) =========================

    @Test
    public void testBuildRequestWithValidModifyPaths()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        AccessConfig accessConfig = new AccessConfig(null,
                Arrays.asList(new ContextPath("/properties/riskScore", false)));

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                .add(InFlowExtensionExecutor.ACCESS_CONFIG_KEY, accessConfig)
                .add(InFlowExtensionExecutor.EXPOSE_KEY,
                        Arrays.asList("/user/", "/properties/", "/input/", "/flow/"));

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        List<AllowedOperation> ops = request.getAllowedOperations();
        assertNotNull(ops);
        assertEquals(ops.size(), 1);
        assertEquals(ops.get(0).getOp(), Operation.REPLACE);
        assertTrue(ops.get(0).getPaths().contains("/properties/riskScore"));
    }

    @Test
    public void testBuildRequestWithNoAccessConfig()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                .add(InFlowExtensionExecutor.EXPOSE_KEY, HierarchicalPrefixMatcher.DEFAULT_EXPOSE);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        List<AllowedOperation> ops = request.getAllowedOperations();
        assertNotNull(ops);
        assertTrue(ops.isEmpty());
    }

    @Test
    public void testBuildRequestWithEmptyModifyPaths()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createMinimalFlowExecutionContext();
        AccessConfig accessConfig = new AccessConfig(null, Arrays.asList());

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                .add(InFlowExtensionExecutor.ACCESS_CONFIG_KEY, accessConfig)
                .add(InFlowExtensionExecutor.EXPOSE_KEY, HierarchicalPrefixMatcher.DEFAULT_EXPOSE);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        List<AllowedOperation> ops = request.getAllowedOperations();
        assertNotNull(ops);
        assertTrue(ops.isEmpty());
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
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                .add(InFlowExtensionExecutor.ACCESS_CONFIG_KEY, accessConfig)
                .add(InFlowExtensionExecutor.EXPOSE_KEY, HierarchicalPrefixMatcher.DEFAULT_EXPOSE);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        // AllowedOperation should have clean path (without {[String]}).
        AllowedOperation op = request.getAllowedOperations().get(0);
        assertTrue(op.getPaths().contains("/properties/riskFactors"));
        assertFalse(op.getPaths().contains("/properties/riskFactors{[String]}"));

        // Annotations should be stored in FlowContext.
        Map<String, String> annotations = flowContext.getValue(
                InFlowExtensionExecutor.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
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
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                .add(InFlowExtensionExecutor.ACCESS_CONFIG_KEY, accessConfig)
                .add(InFlowExtensionExecutor.EXPOSE_KEY, HierarchicalPrefixMatcher.DEFAULT_EXPOSE);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        AllowedOperation op = request.getAllowedOperations().get(0);
        assertTrue(op.getPaths().contains("/properties/items"));
        assertFalse(op.getPaths().contains("/properties/items{name: String, count: Integer}"));

        Map<String, String> annotations = flowContext.getValue(
                InFlowExtensionExecutor.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
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
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                .add(InFlowExtensionExecutor.ACCESS_CONFIG_KEY, accessConfig)
                .add(InFlowExtensionExecutor.EXPOSE_KEY, HierarchicalPrefixMatcher.DEFAULT_EXPOSE);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        // No annotations should be stored when paths have no annotations.
        Map<String, String> annotations = flowContext.getValue(
                InFlowExtensionExecutor.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
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
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                .add(InFlowExtensionExecutor.ACCESS_CONFIG_KEY, accessConfig)
                .add(InFlowExtensionExecutor.EXPOSE_KEY, HierarchicalPrefixMatcher.DEFAULT_EXPOSE);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        AllowedOperation op = request.getAllowedOperations().get(0);
        assertEquals(op.getPaths().size(), 3);
        assertTrue(op.getPaths().contains("/properties/riskScore"));
        assertTrue(op.getPaths().contains("/properties/riskFactors"));
        assertTrue(op.getPaths().contains("/properties/items"));

        Map<String, String> annotations = flowContext.getValue(
                InFlowExtensionExecutor.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
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
        List<String> expose = Arrays.asList("/input/", "/flow/");
        // Modify path targets /properties/riskScore — but this should NOT auto-expose it.
        AccessConfig accessConfig = new AccessConfig(null,
                Arrays.asList(new ContextPath("/properties/riskScore", false)));

        execCtx.setProperty("riskScore", "50");

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                .add(InFlowExtensionExecutor.ACCESS_CONFIG_KEY, accessConfig)
                .add(InFlowExtensionExecutor.EXPOSE_KEY, expose);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        // Modify paths should produce REPLACE allowed operation.
        List<AllowedOperation> ops = request.getAllowedOperations();
        assertEquals(ops.size(), 1);
        assertEquals(ops.get(0).getOp(), Operation.REPLACE);

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
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                .add(InFlowExtensionExecutor.ACCESS_CONFIG_KEY, accessConfig)
                .add(InFlowExtensionExecutor.EXPOSE_KEY, HierarchicalPrefixMatcher.DEFAULT_EXPOSE);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        // All modify paths should be grouped into a single REPLACE operation.
        List<AllowedOperation> ops = request.getAllowedOperations();
        assertEquals(ops.size(), 1);
        assertEquals(ops.get(0).getOp(), Operation.REPLACE);
        assertEquals(ops.get(0).getPaths().size(), 3);
    }

    // ========================= Expose filtering =========================

    @Test
    public void testExposeFilteringOnlyExposedAreaIncluded()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createFullFlowExecutionContext();
        // Only expose /flow/ — no user, no properties, no input.
        List<String> expose = Arrays.asList("/flow/tenantDomain", "/flow/flowType");

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                .add(InFlowExtensionExecutor.EXPOSE_KEY, expose);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

        InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
        // User should NOT be in the event since /user/ is not exposed.
        assertNull(event.getUser());
        // Flow type should be present.
        assertNotNull(event.getFlowType());
        // Tenant should be present.
        assertNotNull(event.getTenant());
    }

    @Test
    public void testExposeFilteringSpecificClaim()
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = createFullFlowExecutionContext();
        // Only expose a specific claim.
        List<String> expose = Arrays.asList(
                "/user/claims/http://wso2.org/claims/email",
                "/user/userId");

        FlowContext flowContext = FlowContext.create()
                .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                .add(InFlowExtensionExecutor.EXPOSE_KEY, expose);

        ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
        ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

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

            // Mark /properties/riskScore as expose-encrypted.
            AccessConfig accessConfig = new AccessConfig(
                    Arrays.asList(new ContextPath("/properties/riskScore", true)),
                    null);

            Encryption encryption = new Encryption(
                    new Certificate.Builder().id("cert-1").name("test")
                            .certificateContent("test-cert-pem").build());

            FlowContext flowContext = FlowContext.create()
                    .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                    .add(InFlowExtensionExecutor.EXPOSE_KEY, HierarchicalPrefixMatcher.DEFAULT_EXPOSE)
                    .add(InFlowExtensionExecutor.ACCESS_CONFIG_KEY, accessConfig)
                    .add(InFlowExtensionExecutor.ENCRYPTION_KEY, encryption);

            ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
            ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

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
    public void testUserInputsEncryptedWhenExposePathMarkedEncrypted()
            throws ActionExecutionRequestBuilderException {

        try (MockedStatic<JWEEncryptionUtil> jweUtilMock = mockStatic(JWEEncryptionUtil.class)) {
            jweUtilMock.when(() -> JWEEncryptionUtil.encrypt(anyString(), anyString()))
                    .thenAnswer(inv -> "encrypted." + inv.getArgument(0) + ".jwe.part.four");

            FlowExecutionContext execCtx = createFullFlowExecutionContext();

            // Mark /input/consent as expose-encrypted.
            AccessConfig accessConfig = new AccessConfig(
                    Arrays.asList(new ContextPath("/input/consent", true)),
                    null);

            Encryption encryption = new Encryption(
                    new Certificate.Builder().id("cert-1").name("test")
                            .certificateContent("test-cert-pem").build());

            FlowContext flowContext = FlowContext.create()
                    .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                    .add(InFlowExtensionExecutor.EXPOSE_KEY, HierarchicalPrefixMatcher.DEFAULT_EXPOSE)
                    .add(InFlowExtensionExecutor.ACCESS_CONFIG_KEY, accessConfig)
                    .add(InFlowExtensionExecutor.ENCRYPTION_KEY, encryption);

            ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
            ActionExecutionRequest request = requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);

            InFlowExtensionEvent event = (InFlowExtensionEvent) request.getEvent();
            assertNotNull(event.getUserInputs());
            // consent should be encrypted.
            String consentValue = event.getUserInputs().get("consent");
            assertNotNull(consentValue);
            assertTrue(consentValue.startsWith("encrypted."),
                    "Input value should be encrypted when expose path is marked encrypted");
            // username input is NOT marked encrypted — should remain plaintext.
            assertEquals(event.getUserInputs().get("username"), "testuser");
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
                    .add(InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, execCtx)
                    .add(InFlowExtensionExecutor.EXPOSE_KEY, HierarchicalPrefixMatcher.DEFAULT_EXPOSE)
                    .add(InFlowExtensionExecutor.ACCESS_CONFIG_KEY, accessConfig)
                    .add(InFlowExtensionExecutor.ENCRYPTION_KEY, encryption);

            ActionExecutionRequestContext reqCtx = mock(ActionExecutionRequestContext.class);
            requestBuilder.buildActionExecutionRequest(flowContext, reqCtx);
        }
    }

    // ========================= Helper methods =========================

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
