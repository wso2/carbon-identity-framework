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
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants;
import org.wso2.carbon.identity.flow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.extension.model.ContextPath;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionAction;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionEvent;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link FlowExtensionRequestBuilder}: expose-based filtering into the event model,
 * modify-path to REPLACE/REDIRECT allowed-operation construction, path-annotation extraction and
 * the input-validation error branches.
 */
public class FlowExtensionRequestBuilderTest {

    private static final String TENANT = "carbon.super";
    private static final String CLAIM_PATH = "/user/claims[uri=http://wso2.org/claims/givenname]";
    private static final String CREDENTIAL_PATH = "/user/credentials/password";

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

        FlowExecutionContext execCtx = new FlowExecutionContext();
        execCtx.setContextIdentifier("ctx-1");
        execCtx.setTenantDomain(TENANT);
        execCtx.setApplicationId("app-1");
        execCtx.setFlowType("REGISTRATION");
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
}
