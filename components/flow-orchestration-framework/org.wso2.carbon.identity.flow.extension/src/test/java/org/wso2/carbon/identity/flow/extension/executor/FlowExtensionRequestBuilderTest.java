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

import org.wso2.carbon.identity.flow.extension.model.FlowExtensionHeader;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.api.model.Header;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowOrganization;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.FlowContextPaths;
import org.wso2.carbon.identity.flow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.extension.model.ContextPath;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionAction;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionEvent;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionRuntimeValue;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionSubjectOrganization;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionUser;
import org.wso2.carbon.identity.flow.mgt.model.FlowRuntimeValueDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDTO;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariableDataType;
import org.wso2.carbon.identity.flow.mgt.model.FlowVariablesDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.ActionManagement.NON_MODIFIABLE_PATHS;

/**
 * Unit tests for {@link FlowExtensionRequestBuilder}: expose-based filtering into the event model,
 * modify-path to REPLACE/REDIRECT allowed-operation construction, path-annotation extraction and
 * the input-validation error branches.
 */
public class FlowExtensionRequestBuilderTest {

    private static final String TENANT = "carbon.super";
    private static final String CLAIM_PATH = "/user/claims[uri=http://wso2.org/claims/givenname]";
    private static final String CREDENTIAL_PATH = "/user/credentials/password";
    private static final String USERNAME_PATH = "/user/username";
    private static final String USER_ID_CLAIM_PATH = "/user/claims[uri=http://wso2.org/claims/userid]";
    private static final String PLAN_PATH = "/runtimeValues[name=plan]";
    private static final String ATTEMPTS_PATH = "/runtimeValues[name=attempts]";
    private static final String UNDEFINED_RUNTIME_VALUE_PATH = "/runtimeValues[name=undefined]";
    private static final String DEVICE_HEADER_PATH = "/request/additionalHeaders[name=x-device]";
    private static final String UNDEFINED_HEADER_PATH = "/request/additionalHeaders[name=x-undefined]";
    private static final String PASSWORD_RECOVERY = "PASSWORD_RECOVERY";

    // A self-signed RSA X.509 certificate (base64-encoded DER, CN=flow-ext-test), valid until 2300.
    // Used only to exercise the outbound credential encryption path.
    private static final String TEST_CERTIFICATE =
            "MIIC1jCCAb6gAwIBAgIJAJW5CSgQetRIMA0GCSqGSIb3DQEBDAUAMBgxFjAUBgNVBAMTDWZsb3ctZXh0LXRlc3Qw"
            + "IBcNMjYwNzA2MDMyMTI0WhgPMjMwMDA0MjEwMzIxMjRaMBgxFjAUBgNVBAMTDWZsb3ctZXh0LXRlc3QwggEiMA0G"
            + "CSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDfg5WbkayJOskKoWZD/dLMkKNisi+3GaKqLgH9RAIMuy3NKNSQCA9p"
            + "YV4n8EUZnP0ZyM1Xv1MkB2mQLknpA6+n6fvyBuY6y2V9slIJlvrJvAUKpxhSWeyIyg3qgs2Kb+cX8NTC5vmrfN5"
            + "pw1QmjYYMAueGEz+z85Tay+C/y+bUns1LrIxTxKzheS9jIkfmAr2LE0DNoCLt7H6GdfLfT+INxfSPHTc1aa1pN4"
            + "QrQGzw91wOvkv4qIiBStJDB8BGIqsSgRMWAe5YKn0AUzYSGqUfhXJ32AnGZKBc7eFytKlpSUwpOeuwlK3ypApcn"
            + "VvNN3oAnN04jvZgdDSwGDOpbsNBAgMBAAGjITAfMB0GA1UdDgQWBBRYki8DbI6YT5peKE0DKEA9jUnPmjANBgkq"
            + "hkiG9w0BAQwFAAOCAQEAWkUK1YgFC+JTVjkhuSEiQIMUiuIprE1yENNaqaYV34LW263aMqc+wor3CDQwWN+8i/w"
            + "9UUmCqfpf06l7sNitT5XkAZSVXkry3TBqKjHKtG0PeHVbkVI4Pms+ZtLfGZut8N4GN6FJMGlYN8A9iRk5eVy+2r"
            + "HqbhC8BRVZ04FT2+IQM4F9Psa/hEvIMSF4Srfsn4tYZsiua7LJrNojX2Ai7OaFqg/+Imf/g5edV6UZaL+heERV"
            + "bRUPynphhy9HQSyR6zcT0UlzfG3G/neiNmbi17Si6LITb4EhfCo/Minv5zsDhWxppHx1YLZ46RvDZAMKKT6lY1E"
            + "rI+m/yoOhf5cj9g==";

    private final FlowExtensionRequestBuilder builder = new FlowExtensionRequestBuilder();

    private MockedStatic<LoggerUtils> loggerUtils;

    @BeforeMethod
    public void setUp() {

        // LoggerUtils.isDiagnosticLogsEnabled() touches CarbonContext, which is unavailable in a
        // plain unit test; stub it off so the diagnostic branches are skipped.
        loggerUtils = mockStatic(LoggerUtils.class);
        loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
    }

    @AfterMethod
    public void tearDown() {

        loggerUtils.close();
    }

    private FlowExecutionContext execContext() {

        return execContext(FlowExtensionConstants.ContextTree.FLOW_REGISTRATION);
    }

    private FlowExecutionContext execContext(String flowType) {

        FlowExecutionContext execCtx = new FlowExecutionContext();
        execCtx.setContextIdentifier("ctx-1");
        execCtx.setTenantDomain(TENANT);
        execCtx.setApplicationId("app-1");
        execCtx.setFlowType(flowType);
        execCtx.setPortalUrl("https://portal");

        FlowUser user = new FlowUser();
        user.setUserId("uid");
        user.setUsername("alice");
        user.setUserStoreDomain("PRIMARY");
        Map<String, String> claims = new HashMap<>();
        claims.put("http://wso2.org/claims/givenname", "John");
        user.addClaims(claims);
        Map<String, char[]> credentials = new HashMap<>();
        credentials.put("password", "secret".toCharArray());
        user.setUserCredentials(credentials);
        execCtx.setFlowUser(user);

        return execCtx;
    }

    private org.wso2.carbon.identity.action.execution.api.model.FlowContext flowContextWith(
            FlowExecutionContext execCtx) {

        return org.wso2.carbon.identity.action.execution.api.model.FlowContext.create()
                .add(FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, execCtx);
    }

    private ActionExecutionRequestContext actionContext(AccessConfig accessConfig) {

        return actionContext(accessConfig, null);
    }

    private ActionExecutionRequestContext actionContext(AccessConfig accessConfig, Certificate certificate) {

        FlowExtensionAction action = new FlowExtensionAction.ResponseBuilder()
                .name("ext")
                .type(Action.ActionTypes.FLOW_EXTENSION)
                .accessConfig(accessConfig)
                .certificate(certificate)
                .build();
        return ActionExecutionRequestContext.create(action);
    }

    @Test
    public void testGetSupportedActionType() {

        assertEquals(builder.getSupportedActionType(), ActionType.FLOW_EXTENSION);
    }

    @Test
    public void testBuildFullRequest() throws Exception {

        AccessConfig accessConfig = new AccessConfig(
                Arrays.asList(
                        new ContextPath("/tenant/domain", false),
                        new ContextPath("/application/id", false),
                        new ContextPath("/user/id", false),
                        new ContextPath("/user/username", false),
                        new ContextPath("/user/userStoreDomain", false),
                        new ContextPath(CLAIM_PATH, false),
                        new ContextPath("/user/credentials/password", false),
                        new ContextPath("/flow/flowType", false),
                        new ContextPath("/flow/portalUrl", false)),
                Arrays.asList(
                        new ContextPath(CLAIM_PATH + "{[string]}", false),
                        new ContextPath("/user/credentials/password", false)));

        FlowExecutionContext execCtx = execContext();
        org.wso2.carbon.identity.action.execution.api.model.FlowContext actionFlowContext =
                flowContextWith(execCtx);

        ActionExecutionRequest request;
        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class)) {

            tenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT)).thenReturn(1);

            request = builder.buildActionExecutionRequest(actionFlowContext, actionContext(accessConfig));
        }

        assertEquals(request.getActionType(), ActionType.FLOW_EXTENSION);

        // Allowed operations: a REPLACE with both clean modify paths, then a REDIRECT.
        List<AllowedOperation> allowedOps = request.getAllowedOperations();
        assertEquals(allowedOps.size(), 2);
        assertEquals(allowedOps.get(0).getOp(), Operation.REPLACE);
        assertTrue(allowedOps.get(0).getPaths().contains(CLAIM_PATH));
        assertTrue(allowedOps.get(0).getPaths().contains("/user/credentials/password"));
        assertEquals(allowedOps.get(1).getOp(), Operation.REDIRECT);

        // Event carries the exposed, expose-filtered flow/user data.
        FlowExtensionEvent event = (FlowExtensionEvent) request.getEvent();
        assertNotNull(event.getFlow());
        assertEquals(event.getFlow().getFlowType(), "REGISTRATION");
        assertEquals(event.getFlow().getFlowId(), "ctx-1");
        assertEquals(event.getFlow().getPortalUrl(), "https://portal");
        assertEquals(event.getFlow().getUser().getId(), "uid");

        // Modify paths and the stripped path-type annotation are stashed for the response processor.
        assertNotNull(actionFlowContext.getContextData().get(FlowExtensionConstants.MODIFY_PATHS_KEY));
        Object annotations = actionFlowContext.getContextData().get(FlowExtensionConstants.PATH_TYPE_ANNOTATIONS_KEY);
        assertNotNull(annotations);
        assertEquals(((Map<?, ?>) annotations).get(CLAIM_PATH), "[string]");
    }

    @Test
    public void testUnencryptedCredentialWrappedAsTypedObject() throws Exception {

        // A plaintext-exposed credential is wrapped as {"type":"PLAIN_TEXT","value":"<secret>"} and
        // carried as a nested object (not a bare string) for the mapper to serialize.
        AccessConfig accessConfig = new AccessConfig(
                Collections.singletonList(new ContextPath(CREDENTIAL_PATH, false)), null);

        ActionExecutionRequest request;
        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class)) {
            tenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT)).thenReturn(1);
            request = builder.buildActionExecutionRequest(
                    flowContextWith(execContext()), actionContext(accessConfig));
        }

        FlowExtensionUser user = (FlowExtensionUser) ((FlowExtensionEvent) request.getEvent())
                .getFlow().getUser();
        Object credential = user.getCredentials().get("password");
        assertTrue(credential instanceof Map, "Unencrypted credential must be a typed object.");
        Map<?, ?> typed = (Map<?, ?>) credential;
        assertEquals(typed.get(FlowExtensionConstants.Credentials.TYPE_KEY),
                FlowExtensionConstants.Credentials.TYPE_PLAIN_TEXT);
        assertEquals(typed.get(FlowExtensionConstants.Credentials.VALUE_KEY), "secret");
    }

    @Test
    public void testEncryptedCredentialSerializedAsJweString() throws Exception {

        // An encrypted-exposed credential is serialized to a JWE compact string and the plaintext
        // secret must not appear verbatim in it.
        AccessConfig accessConfig = new AccessConfig(
                Collections.singletonList(new ContextPath(CREDENTIAL_PATH, true)), null);
        Certificate certificate = new Certificate.Builder().certificateContent(TEST_CERTIFICATE).build();

        ActionExecutionRequest request;
        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class)) {
            tenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT)).thenReturn(1);
            request = builder.buildActionExecutionRequest(
                    flowContextWith(execContext()), actionContext(accessConfig, certificate));
        }

        FlowExtensionUser user = (FlowExtensionUser) ((FlowExtensionEvent) request.getEvent())
                .getFlow().getUser();
        Object credential = user.getCredentials().get("password");
        assertTrue(credential instanceof String, "Encrypted credential must be a JWE string.");
        String jwe = (String) credential;
        assertTrue(JWEEncryptionUtil.isJWEEncrypted(jwe),
                "Encrypted credential must be a JWE compact serialization: " + jwe);
        assertFalse(jwe.contains("secret"), "Ciphertext must not contain the plaintext secret.");
    }

    @Test
    public void testNoAccessConfigProducesRedirectOnly() throws Exception {

        ActionExecutionRequest request = builder.buildActionExecutionRequest(
                flowContextWith(execContext()), actionContext(new AccessConfig(null, null)));

        List<AllowedOperation> allowedOps = request.getAllowedOperations();
        assertEquals(allowedOps.size(), 1);
        assertEquals(allowedOps.get(0).getOp(), Operation.REDIRECT);

        // Nothing exposed -> the flow only carries the always-sent flowId.
        FlowExtensionEvent event = (FlowExtensionEvent) request.getEvent();
        assertNull(event.getFlow().getUser());
        assertEquals(event.getFlow().getFlowId(), "ctx-1");
    }

    @Test
    public void testEncryptedExposePrunedWhenCertificateAbsent() throws Exception {

        // An encrypted expose path with no outbound certificate is pruned, so the user area ends
        // up unexposed and no user object is attached.
        AccessConfig accessConfig = new AccessConfig(
                Collections.singletonList(new ContextPath("/user/id", true)), null);

        ActionExecutionRequest request = builder.buildActionExecutionRequest(
                flowContextWith(execContext()), actionContext(accessConfig));

        FlowExtensionEvent event = (FlowExtensionEvent) request.getEvent();
        assertNull(event.getFlow().getUser());
    }

    // ------------------------------------------------------------------ expose is not flow scoped

    // Exposure is opted into explicitly through the access config, so it is honoured on every flow
    // type. Only modification of the username is scoped to self registration.

    @Test
    public void testUsernameExposedForSelfRegistrationFlow() throws Exception {

        AccessConfig accessConfig = new AccessConfig(
                Arrays.asList(new ContextPath("/user/id", false), new ContextPath(USERNAME_PATH, false)), null);

        ActionExecutionRequest request = builder.buildActionExecutionRequest(
                flowContextWith(execContext(FlowExtensionConstants.ContextTree.FLOW_REGISTRATION)),
                actionContext(accessConfig));

        FlowExtensionUser user = (FlowExtensionUser) ((FlowExtensionEvent) request.getEvent()).getFlow().getUser();
        assertNotNull(user);
        assertEquals(user.getUsername(), "alice");
    }

    @Test
    public void testUsernameExposedForNonSelfRegistrationFlow() throws Exception {

        AccessConfig accessConfig = new AccessConfig(
                Arrays.asList(new ContextPath("/user/id", false), new ContextPath(USERNAME_PATH, false)), null);

        ActionExecutionRequest request = builder.buildActionExecutionRequest(
                flowContextWith(execContext("PASSWORD_RECOVERY")), actionContext(accessConfig));

        FlowExtensionUser user = (FlowExtensionUser) ((FlowExtensionEvent) request.getEvent()).getFlow().getUser();
        assertNotNull(user);
        assertEquals(user.getId(), "uid");
        assertEquals(user.getUsername(), "alice", "Expose config must be honoured on every flow type.");
    }

    // ------------------------------------------------------------------ modify path restrictions

    // Restricted paths are stripped before the allowed operations are built, so the extension is
    // never told it may modify a path whose modification would be discarded. An operation on a path
    // that was never advertised is dropped by the action execution framework.

    private AllowedOperation replaceOperation(ActionExecutionRequest request) {

        return request.getAllowedOperations().stream()
                .filter(op -> op.getOp() == Operation.REPLACE)
                .findFirst()
                .orElse(null);
    }

    private ActionExecutionRequest buildWithNonModifiablePaths(AccessConfig accessConfig, String flowType,
                                                               String... nonModifiablePaths) throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getPropertyAsList(NON_MODIFIABLE_PATHS))
                    .thenReturn(Arrays.asList(nonModifiablePaths));

            return builder.buildActionExecutionRequest(
                    flowContextWith(execContext(flowType)), actionContext(accessConfig));
        }
    }

    @Test
    public void testNonModifiablePathNotAdvertisedAsModifiable() throws Exception {

        AccessConfig accessConfig = new AccessConfig(null, Arrays.asList(
                new ContextPath(USER_ID_CLAIM_PATH, false), new ContextPath(CLAIM_PATH, false)));

        ActionExecutionRequest request = buildWithNonModifiablePaths(
                accessConfig, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION, USER_ID_CLAIM_PATH);

        AllowedOperation replaceOp = replaceOperation(request);
        assertNotNull(replaceOp);
        assertFalse(replaceOp.getPaths().contains(USER_ID_CLAIM_PATH),
                "A non-modifiable path must not be advertised as modifiable.");
        assertTrue(replaceOp.getPaths().contains(CLAIM_PATH));
    }

    @Test
    public void testNonModifiablePathRemovedFromStoredModifyPaths() throws Exception {

        // The response processor reads MODIFY_PATHS_KEY for its decryption decisions, so the
        // dropped path must not survive there either.
        AccessConfig accessConfig = new AccessConfig(null, Arrays.asList(
                new ContextPath(USER_ID_CLAIM_PATH, false), new ContextPath(CLAIM_PATH, false)));

        org.wso2.carbon.identity.action.execution.api.model.FlowContext actionFlowContext =
                flowContextWith(execContext());

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getPropertyAsList(NON_MODIFIABLE_PATHS))
                    .thenReturn(Collections.singletonList(USER_ID_CLAIM_PATH));

            builder.buildActionExecutionRequest(actionFlowContext, actionContext(accessConfig));
        }

        List<?> storedPaths = (List<?>) actionFlowContext.getContextData()
                .get(FlowExtensionConstants.MODIFY_PATHS_KEY);
        assertEquals(storedPaths.size(), 1);
        assertEquals(((ContextPath) storedPaths.get(0)).getPath(), CLAIM_PATH);
    }

    @Test
    public void testNonModifiablePathMatchedAfterStrippingTypeAnnotation() throws Exception {

        // Restrictions are configured against clean paths, so an annotated modify path must still
        // be matched once the annotation is stripped.
        AccessConfig accessConfig = new AccessConfig(null, Collections.singletonList(
                new ContextPath(USER_ID_CLAIM_PATH + "{[string]}", false)));

        ActionExecutionRequest request = buildWithNonModifiablePaths(
                accessConfig, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION, USER_ID_CLAIM_PATH);

        assertNull(replaceOperation(request), "Only the REDIRECT operation should remain.");
        assertEquals(request.getAllowedOperations().size(), 1);
    }

    @Test
    public void testAllModifyPathsRestrictedLeavesRedirectOnly() throws Exception {

        AccessConfig accessConfig = new AccessConfig(null, Collections.singletonList(
                new ContextPath(USER_ID_CLAIM_PATH, false)));

        ActionExecutionRequest request = buildWithNonModifiablePaths(
                accessConfig, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION, USER_ID_CLAIM_PATH);

        assertEquals(request.getAllowedOperations().size(), 1);
        assertEquals(request.getAllowedOperations().get(0).getOp(), Operation.REDIRECT);
    }

    @Test
    public void testUnrestrictedModifyPathStillAdvertised() throws Exception {

        AccessConfig accessConfig = new AccessConfig(null, Collections.singletonList(
                new ContextPath(CLAIM_PATH, false)));

        ActionExecutionRequest request = buildWithNonModifiablePaths(
                accessConfig, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION, USER_ID_CLAIM_PATH);

        AllowedOperation replaceOp = replaceOperation(request);
        assertNotNull(replaceOp);
        assertTrue(replaceOp.getPaths().contains(CLAIM_PATH));
    }

    @Test
    public void testNullModifyPathEntriesToleratedByRestrictionFilter() throws Exception {

        // Null entries are skipped downstream by extractAllowedModifyPaths; the restriction filter
        // runs first and must not trip over them.
        AccessConfig accessConfig = new AccessConfig(null, Arrays.asList(
                null, new ContextPath(null, false), new ContextPath(CLAIM_PATH, false)));

        ActionExecutionRequest request = buildWithNonModifiablePaths(
                accessConfig, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION, USER_ID_CLAIM_PATH);

        AllowedOperation replaceOp = replaceOperation(request);
        assertNotNull(replaceOp);
        assertEquals(replaceOp.getPaths(), Collections.singletonList(CLAIM_PATH));
    }

    @Test
    public void testUsernameModifyPathNotAdvertisedForNonSelfRegistrationFlow() throws Exception {

        AccessConfig accessConfig = new AccessConfig(null, Arrays.asList(
                new ContextPath(USERNAME_PATH, false), new ContextPath(CLAIM_PATH, false)));

        ActionExecutionRequest request = buildWithNonModifiablePaths(accessConfig, "PASSWORD_RECOVERY");

        AllowedOperation replaceOp = replaceOperation(request);
        assertNotNull(replaceOp);
        assertFalse(replaceOp.getPaths().contains(USERNAME_PATH),
                "Username must not be advertised as modifiable outside self registration.");
        assertTrue(replaceOp.getPaths().contains(CLAIM_PATH));
    }

    @Test
    public void testUsernameModifyPathAdvertisedForSelfRegistrationFlow() throws Exception {

        AccessConfig accessConfig = new AccessConfig(null, Collections.singletonList(
                new ContextPath(USERNAME_PATH, false)));

        ActionExecutionRequest request = buildWithNonModifiablePaths(
                accessConfig, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION);

        AllowedOperation replaceOp = replaceOperation(request);
        assertNotNull(replaceOp);
        assertTrue(replaceOp.getPaths().contains(USERNAME_PATH));
    }

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class)
    public void testMissingFlowExecutionContextAborts() throws Exception {

        builder.buildActionExecutionRequest(
                org.wso2.carbon.identity.action.execution.api.model.FlowContext.create(),
                actionContext(new AccessConfig(null, null)));
    }

    @Test(expectedExceptions = ActionExecutionRequestBuilderException.class)
    public void testNonFlowExtensionActionAborts() throws Exception {

        Action plainAction = new Action.ActionResponseBuilder().name("plain").build();

        builder.buildActionExecutionRequest(
                flowContextWith(execContext()), ActionExecutionRequestContext.create(plainAction));
    }

    // ------------------------------------------------------------------ runtime values and request headers

    private FlowExecutionContext execContextWithFlowVariables() {

        FlowVariablesDTO variables = new FlowVariablesDTO();
        variables.getRuntimeValues().add(new FlowRuntimeValueDTO("plan", FlowVariableDataType.STRING));
        variables.getRuntimeValues().add(new FlowRuntimeValueDTO("attempts", FlowVariableDataType.INTEGER));
        variables.getHeaders().add(new FlowVariableDTO("x-device", FlowVariableDataType.STRING_ARRAY));
        GraphConfig graphConfig = new GraphConfig();
        graphConfig.setFlowVariables(variables);

        FlowExecutionContext execCtx = execContext();
        execCtx.setGraphConfig(graphConfig);
        execCtx.getRuntimeValues().put("plan", "gold");
        execCtx.getRuntimeValues().put("attempts", 3L);
        execCtx.getRequest().getHeaders().put("x-device", Arrays.asList("mobile", "tablet"));
        return execCtx;
    }

    /**
     * Only the entries for the running flow type, and those the flow defines, reach the extension.
     */
    @Test
    public void testRuntimeValuesAndHeadersAreScopedToTheRunningFlow() throws Exception {

        AccessConfig accessConfig = new AccessConfig(
                Arrays.asList(
                        new ContextPath("/user/id", false),
                        new ContextPath(PLAN_PATH, false, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION),
                        new ContextPath(ATTEMPTS_PATH, false, PASSWORD_RECOVERY),
                        new ContextPath(UNDEFINED_RUNTIME_VALUE_PATH, false,
                                FlowExtensionConstants.ContextTree.FLOW_REGISTRATION),
                        new ContextPath(DEVICE_HEADER_PATH, false, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION),
                        new ContextPath(UNDEFINED_HEADER_PATH, false,
                                FlowExtensionConstants.ContextTree.FLOW_REGISTRATION)),
                Arrays.asList(
                        new ContextPath(PLAN_PATH, false, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION),
                        new ContextPath(ATTEMPTS_PATH, false, PASSWORD_RECOVERY),
                        new ContextPath(UNDEFINED_RUNTIME_VALUE_PATH, false,
                                FlowExtensionConstants.ContextTree.FLOW_REGISTRATION)));

        org.wso2.carbon.identity.action.execution.api.model.FlowContext actionFlowContext =
                flowContextWith(execContextWithFlowVariables());
        ActionExecutionRequest request =
                builder.buildActionExecutionRequest(actionFlowContext, actionContext(accessConfig));

        List<AllowedOperation> allowedOps = request.getAllowedOperations();
        assertEquals(allowedOps.size(), 2);
        assertEquals(allowedOps.get(0).getOp(), Operation.REPLACE);
        assertEquals(allowedOps.get(0).getPaths(), Collections.singletonList(PLAN_PATH));
        List<?> modifyPaths = (List<?>) actionFlowContext.getContextData().get(FlowExtensionConstants.MODIFY_PATHS_KEY);
        assertEquals(modifyPaths.size(), 1);
        assertEquals(((ContextPath) modifyPaths.get(0)).getPath(), PLAN_PATH);

        FlowExtensionEvent event = (FlowExtensionEvent) request.getEvent();
        assertEquals(event.getFlow().getUser().getId(), "uid");

        List<FlowExtensionRuntimeValue> runtimeValues = event.getRuntimeValues();
        assertEquals(runtimeValues.size(), 1);
        assertEquals(runtimeValues.get(0).getName(), "plan");
        assertEquals(runtimeValues.get(0).getDataType(), "STRING");
        assertEquals(runtimeValues.get(0).getValue(), "gold");

        List<Header> headers = event.getRequest().getAdditionalHeaders();
        assertEquals(headers.size(), 1);
        assertEquals(headers.get(0).getName(), "x-device");
        assertEquals(headers.get(0).getValue(), new String[]{"mobile", "tablet"});
    }

    @Test
    public void testRuntimeValuesForAnotherFlowTypeAreNotSent() throws Exception {

        AccessConfig accessConfig = new AccessConfig(
                Arrays.asList(new ContextPath(PLAN_PATH, false, PASSWORD_RECOVERY),
                        new ContextPath(DEVICE_HEADER_PATH, false, PASSWORD_RECOVERY)),
                Collections.singletonList(new ContextPath(PLAN_PATH, false, PASSWORD_RECOVERY)));

        ActionExecutionRequest request = builder.buildActionExecutionRequest(
                flowContextWith(execContextWithFlowVariables()), actionContext(accessConfig));

        List<AllowedOperation> allowedOps = request.getAllowedOperations();
        assertEquals(allowedOps.size(), 1);
        assertEquals(allowedOps.get(0).getOp(), Operation.REDIRECT);
        FlowExtensionEvent event = (FlowExtensionEvent) request.getEvent();
        assertTrue(event.getRuntimeValues().isEmpty());
        assertNull(event.getRequest());
    }

    /**
     * A number header is sent as the text of its number, without an exponent.
     */
    @Test
    public void testNumberHeaderIsSentAsItsText() throws Exception {

        FlowExecutionContext execCtx = execContextWithFlowVariables();
        execCtx.getGraphConfig().getFlowVariables().getHeaders()
                .add(new FlowVariableDTO("x-count", FlowVariableDataType.NUMBER_ARRAY));
        execCtx.getRequest().getHeaders().put("x-count", Arrays.asList(10.0, 12345678.0, 2.5, 1.0E-7));
        AccessConfig accessConfig = new AccessConfig(Collections.singletonList(
                new ContextPath("/request/additionalHeaders[name=x-count]", false,
                        FlowExtensionConstants.ContextTree.FLOW_REGISTRATION)), null);

        ActionExecutionRequest request =
                builder.buildActionExecutionRequest(flowContextWith(execCtx), actionContext(accessConfig));

        List<Header> headers = ((FlowExtensionEvent) request.getEvent()).getRequest().getAdditionalHeaders();
        assertEquals(headers.size(), 1);
        assertEquals(headers.get(0).getValue(), new String[]{"10.0", "12345678.0", "2.5", "0.0000001"});
    }

    /**
     * A runtime value without a value, or a header not received, is left out of the event.
     */
    @Test
    public void testRuntimeValueWithoutAValueIsNotSent() throws Exception {

        FlowExecutionContext execCtx = execContextWithFlowVariables();
        execCtx.getRuntimeValues().remove("plan");
        execCtx.getRequest().getHeaders().remove("x-device");
        AccessConfig accessConfig = new AccessConfig(
                Arrays.asList(
                        new ContextPath(PLAN_PATH, false, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION),
                        new ContextPath(ATTEMPTS_PATH, false, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION),
                        new ContextPath(DEVICE_HEADER_PATH, false,
                                FlowExtensionConstants.ContextTree.FLOW_REGISTRATION)), null);

        ActionExecutionRequest request =
                builder.buildActionExecutionRequest(flowContextWith(execCtx), actionContext(accessConfig));

        FlowExtensionEvent event = (FlowExtensionEvent) request.getEvent();
        assertEquals(event.getRuntimeValues().size(), 1);
        assertEquals(event.getRuntimeValues().get(0).getName(), "attempts");
        assertEquals(event.getRuntimeValues().get(0).getDataType(), "INTEGER");
        assertEquals(event.getRuntimeValues().get(0).getValue(), 3L);
        assertNull(event.getRequest());
    }

    @Test
    public void testEncryptedRuntimeValueAndHeaderAreSentAsJwe() throws Exception {

        AccessConfig accessConfig = new AccessConfig(
                Arrays.asList(
                        new ContextPath(PLAN_PATH, true, FlowExtensionConstants.ContextTree.FLOW_REGISTRATION),
                        new ContextPath(DEVICE_HEADER_PATH, true,
                                FlowExtensionConstants.ContextTree.FLOW_REGISTRATION)), null);
        Certificate certificate = new Certificate.Builder().certificateContent(TEST_CERTIFICATE).build();

        ActionExecutionRequest request;
        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class)) {
            tenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT)).thenReturn(1);
            request = builder.buildActionExecutionRequest(
                    flowContextWith(execContextWithFlowVariables()), actionContext(accessConfig, certificate));
        }

        FlowExtensionEvent event = (FlowExtensionEvent) request.getEvent();
        Object plan = event.getRuntimeValues().get(0).getValue();
        assertTrue(plan instanceof String && JWEEncryptionUtil.isJWEEncrypted((String) plan), String.valueOf(plan));
        Object device = ((FlowExtensionHeader) event.getRequest().getAdditionalHeaders().get(0)).getSentValue();
        assertTrue(device instanceof String && JWEEncryptionUtil.isJWEEncrypted((String) device),
                String.valueOf(device));
    }

    /**
     * A primitive value is encrypted as its text and an array as its whole JSON array, with numbers written as they
     * are when not encrypted. A header is encrypted as the same array of text it is sent as when not encrypted.
     */
    @Test
    public void testEncryptedValuesAreTheirTextOrJsonArray() throws Exception {

        FlowExecutionContext execCtx = execContextWithFlowVariables();
        execCtx.getGraphConfig().getFlowVariables().getHeaders()
                .add(new FlowVariableDTO("x-count", FlowVariableDataType.NUMBER_ARRAY));
        execCtx.getRequest().getHeaders().put("x-count", Arrays.asList(10.0, 2.5, 1.0E-7));
        String registration = FlowExtensionConstants.ContextTree.FLOW_REGISTRATION;
        AccessConfig accessConfig = new AccessConfig(Arrays.asList(
                new ContextPath(PLAN_PATH, true, registration),
                new ContextPath(ATTEMPTS_PATH, true, registration),
                new ContextPath(DEVICE_HEADER_PATH, true, registration),
                new ContextPath("/request/additionalHeaders[name=x-count]", true, registration)), null);
        Certificate certificate = new Certificate.Builder().certificateContent(TEST_CERTIFICATE).build();

        ActionExecutionRequest request;
        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<JWEEncryptionUtil> jwe = mockStatic(JWEEncryptionUtil.class)) {
            tenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT)).thenReturn(1);
            jwe.when(() -> JWEEncryptionUtil.encrypt(anyString(), eq(TEST_CERTIFICATE)))
                    .thenAnswer(invocation -> "jwe(" + invocation.getArgument(0) + ")");
            request = builder.buildActionExecutionRequest(flowContextWith(execCtx),
                    actionContext(accessConfig, certificate));
        }

        FlowExtensionEvent event = (FlowExtensionEvent) request.getEvent();
        Map<String, Object> runtimeValues = new HashMap<>();
        event.getRuntimeValues().forEach(value -> runtimeValues.put(value.getName(), value.getValue()));
        assertEquals(runtimeValues.get("plan"), "jwe(gold)");
        assertEquals(runtimeValues.get("attempts"), "jwe(3)");
        Map<String, Object> headers = new HashMap<>();
        event.getRequest().getAdditionalHeaders().forEach(header ->
                headers.put(header.getName(), ((FlowExtensionHeader) header).getSentValue()));
        assertEquals(headers.get("x-device"), "jwe([\"mobile\",\"tablet\"])");
        assertEquals(headers.get("x-count"), "jwe([\"10.0\",\"2.5\",\"0.0000001\"])");
    }

    /**
     * In the request JSON, a header's value is its array of values, or the encrypted string when it is encrypted.
     */
    @Test
    public void testHeaderValueInTheRequestJson() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        FlowExtensionRequest request = new FlowExtensionRequest(Arrays.asList(
                new FlowExtensionHeader("x-device", new String[]{"mobile", "tablet"}),
                new FlowExtensionHeader("x-count", "jwe")));

        assertEquals(objectMapper.writeValueAsString(request),
                "{\"additionalHeaders\":[{\"name\":\"x-device\",\"value\":[\"mobile\",\"tablet\"]},"
                        + "{\"name\":\"x-count\",\"value\":\"jwe\"}]}");
    }

    // ------------------------------------------------------------------ subject organization

    private static final String REGISTRATION_FLOW = FlowExtensionConstants.ContextTree.FLOW_REGISTRATION;

    private FlowExecutionContext execContextWithOrganization() {

        FlowExecutionContext execCtx = execContext();
        FlowOrganization organization = execCtx.getFlowOrganization();
        organization.setOrganizationName("Acme");
        organization.setOrganizationHandle("acme");
        organization.setOrganizationDescription("Acme Corp");
        organization.setAttribute("industry", "Retail");
        return execCtx;
    }

    /**
     * Only the exposed fields of the organization the flow onboards are sent. An attribute the flow has not collected
     * is left out.
     */
    @Test
    public void testSubjectOrganizationExposedFieldsAreSent() throws Exception {

        FlowExecutionContext execCtx = execContextWithOrganization();
        AccessConfig accessConfig = new AccessConfig(Arrays.asList(
                new ContextPath(FlowContextPaths.SUBJECT_ORGANIZATION_ID_PATH, false, REGISTRATION_FLOW),
                new ContextPath(FlowContextPaths.SUBJECT_ORGANIZATION_NAME_PATH, false, REGISTRATION_FLOW),
                new ContextPath(FlowContextPaths.SUBJECT_ORGANIZATION_DESCRIPTION_PATH, false, REGISTRATION_FLOW),
                new ContextPath("/subjectOrganization/attributes[name=industry]", false, REGISTRATION_FLOW),
                new ContextPath("/subjectOrganization/attributes[name=size]", false, REGISTRATION_FLOW)), null);

        ActionExecutionRequest request =
                builder.buildActionExecutionRequest(flowContextWith(execCtx), actionContext(accessConfig));

        FlowExtensionSubjectOrganization organization =
                ((FlowExtensionEvent) request.getEvent()).getSubjectOrganization();
        assertNotNull(organization);
        assertEquals(organization.getId(), execCtx.getFlowOrganization().getOrganizationId());
        assertEquals(organization.getName(), "Acme");
        assertNull(organization.getHandle());
        assertEquals(organization.getDescription(), "Acme Corp");
        assertEquals(organization.getAttributes().size(), 1);
        assertEquals(organization.getAttributes().get(0).getName(), "industry");
        assertEquals(organization.getAttributes().get(0).getValue(), "Retail");
    }

    @Test
    public void testSubjectOrganizationIsNotSentWhenNotExposed() throws Exception {

        AccessConfig accessConfig = new AccessConfig(
                Collections.singletonList(new ContextPath(FlowContextPaths.USER_ID_PATH, false)), null);

        ActionExecutionRequest request = builder.buildActionExecutionRequest(
                flowContextWith(execContextWithOrganization()), actionContext(accessConfig));

        assertNull(((FlowExtensionEvent) request.getEvent()).getSubjectOrganization());
    }

    /**
     * A flow that has collected no organization details onboards no organization, so none is sent, not even its ID.
     */
    @Test
    public void testSubjectOrganizationIsNotSentWhenTheFlowOnboardsNone() throws Exception {

        AccessConfig accessConfig = new AccessConfig(Arrays.asList(
                new ContextPath(FlowContextPaths.SUBJECT_ORGANIZATION_ID_PATH, false, REGISTRATION_FLOW),
                new ContextPath(FlowContextPaths.SUBJECT_ORGANIZATION_NAME_PATH, false, REGISTRATION_FLOW)), null);

        ActionExecutionRequest request =
                builder.buildActionExecutionRequest(flowContextWith(execContext()), actionContext(accessConfig));

        assertNull(((FlowExtensionEvent) request.getEvent()).getSubjectOrganization());
    }

    @Test
    public void testSubjectOrganizationIsNotSentInAnotherFlow() throws Exception {

        FlowExecutionContext execCtx = execContext(PASSWORD_RECOVERY);
        execCtx.getFlowOrganization().setOrganizationName("Acme");
        AccessConfig accessConfig = new AccessConfig(Collections.singletonList(
                new ContextPath(FlowContextPaths.SUBJECT_ORGANIZATION_NAME_PATH, false, REGISTRATION_FLOW)), null);

        ActionExecutionRequest request =
                builder.buildActionExecutionRequest(flowContextWith(execCtx), actionContext(accessConfig));

        assertNull(((FlowExtensionEvent) request.getEvent()).getSubjectOrganization());
    }

    @Test
    public void testEncryptedSubjectOrganizationFieldsAreSentAsJwe() throws Exception {

        AccessConfig accessConfig = new AccessConfig(Arrays.asList(
                new ContextPath(FlowContextPaths.SUBJECT_ORGANIZATION_HANDLE_PATH, true, REGISTRATION_FLOW),
                new ContextPath("/subjectOrganization/attributes[name=industry]", true, REGISTRATION_FLOW)), null);
        Certificate certificate = new Certificate.Builder().certificateContent(TEST_CERTIFICATE).build();

        ActionExecutionRequest request = builder.buildActionExecutionRequest(
                flowContextWith(execContextWithOrganization()), actionContext(accessConfig, certificate));

        FlowExtensionSubjectOrganization organization =
                ((FlowExtensionEvent) request.getEvent()).getSubjectOrganization();
        assertTrue(JWEEncryptionUtil.isJWEEncrypted(organization.getHandle()), organization.getHandle());
        String industry = (String) organization.getAttributes().get(0).getValue();
        assertTrue(JWEEncryptionUtil.isJWEEncrypted(industry), industry);
    }
}
