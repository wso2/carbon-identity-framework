package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.graalvm.polyglot.Context;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalJsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class PolyglotJsAuthenticationContextTest {

    public static final String TEST_IDP = "testIdP";
    private  Context context;

    @BeforeClass
    public void setUp() {

        context = Context.newBuilder("js").allowAllAccess(true).build();
    }

    @Test
    public void testClaimAssignment() throws IOException {

        ClaimMapping claimMapping1 = ClaimMapping.build("", "", "", false);

        ClaimMapping claimMapping2 = ClaimMapping.build("Test.Remote.Claim.Url.2", "Test.Remote.Claim.Url.2", "",
                false);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.getUserAttributes().put(claimMapping1, "TestClaimVal1");
        authenticatedUser.getUserAttributes().put(claimMapping2, "TestClaimVal2");
        AuthenticationContext authenticationContext = new AuthenticationContext();
        setupAuthContextWithStepData(authenticationContext, authenticatedUser);

        GraalJsAuthenticationContext jsAuthenticationContext = new GraalJsAuthenticationContext(authenticationContext);
        Value bindings = context.getBindings("js");
        bindings.putMember("context", jsAuthenticationContext);

        Value result = context.eval(Source.newBuilder("js",
                "context.steps[1].subject.remoteClaims['Test.Remote.Claim.Url.1']",
                "src.js").build());
        assertTrue(result.isNull());

        result = context.eval(Source.newBuilder("js",
                "context.steps[1].subject.remoteClaims['Test.Remote.Claim.Url.2']",
                "src.js").build());
        assertEquals(result.asString(), "TestClaimVal2");

        context.eval(Source.newBuilder("js",
                "context.steps[1].subject.remoteClaims['Test.Remote.Claim.Url.2'] = 'Modified2'",
                "src.js").build());
        result = context.eval(Source.newBuilder("js",
                "context.steps[1].subject.remoteClaims['Test.Remote.Claim.Url.2']",
                "src.js").build());
        assertEquals(result.asString(), "Modified2");

    }

    private void setupAuthContextWithStepData(AuthenticationContext context, AuthenticatedUser authenticatedUser) {

        SequenceConfig sequenceConfig = new SequenceConfig();
        Map<Integer, StepConfig> stepConfigMap = new HashMap<>();
        StepConfig stepConfig = new StepConfig();
        stepConfig.setOrder(1);
        stepConfig.setAuthenticatedIdP(TEST_IDP);
        stepConfigMap.put(1, stepConfig);
        sequenceConfig.setStepMap(stepConfigMap);
        AuthenticationGraph authenticationGraph = new AuthenticationGraph();
        authenticationGraph.setStepMap(stepConfigMap);
        sequenceConfig.setAuthenticationGraph(authenticationGraph);
        context.setSequenceConfig(sequenceConfig);
        Map<String, AuthenticatedIdPData> idPDataMap = new HashMap<>();
        AuthenticatedIdPData idPData = new AuthenticatedIdPData();
        idPData.setUser(authenticatedUser);
        idPData.setIdpName(TEST_IDP);
        idPDataMap.put(TEST_IDP, idPData);
        context.setCurrentAuthenticatedIdPs(idPDataMap);
    }

    @Test
    public void testRemoteAddition() throws IOException {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        setupAuthContextWithStepData(authenticationContext, authenticatedUser);

        GraalJsAuthenticationContext jsAuthenticationContext = new GraalJsAuthenticationContext(authenticationContext);
        Value bindings = context.getBindings("js");
        bindings.putMember("context", jsAuthenticationContext);

        context.eval(Source.newBuilder("js",
                "context.steps[1].subject.remoteClaims['testClaim']='testValue'",
                "src.js").build());

        ClaimMapping claimMapping = ClaimMapping.build("testClaim", "testClaim", "", false);
        String claimCreatedByJs = authenticatedUser.getUserAttributes().get(claimMapping);
        assertEquals(claimCreatedByJs, "testValue");
    }

    @Test
    public void testGetServiceProviderFromWrappedContext() throws Exception {

        final String SERVICE_PROVIDER_NAME = "service_provider_js_test";

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setServiceProviderName(SERVICE_PROVIDER_NAME);

        GraalJsAuthenticationContext jsAuthenticationContext = new GraalJsAuthenticationContext(authenticationContext);
        Value bindings = context.getBindings("js");
        bindings.putMember("context", jsAuthenticationContext);

        Value result = context.eval(Source.newBuilder("js",
                "context.serviceProviderName",
                "src.js").build());
        assertFalse(result.isNull());
        assertEquals(result.asString(), SERVICE_PROVIDER_NAME, "Service Provider name set in AuthenticationContext is not " +
                "accessible from JSAuthenticationContext");
    }


    @Test
    public void testGetLastLoginFailedUserFromWrappedContext() throws Exception {

        final String LAST_ATTEMPTED_USER_USERNAME = "lastAttemptedUsername";
        final String LAST_ATTEMPTED_USER_TENANT_DOMAIN = "lastAttemptedTenantDomain";
        final String LAST_ATTEMPTED_USER_USERSTORE_DOMAIN = "lastAttemptedUserstoreDomain";

        AuthenticatedUser lastAttemptedUser = new AuthenticatedUser();
        lastAttemptedUser.setUserName(LAST_ATTEMPTED_USER_USERNAME);
        lastAttemptedUser.setTenantDomain(LAST_ATTEMPTED_USER_TENANT_DOMAIN);
        lastAttemptedUser.setUserStoreDomain(LAST_ATTEMPTED_USER_USERSTORE_DOMAIN);

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setProperty(FrameworkConstants.JSAttributes.JS_LAST_LOGIN_FAILED_USER, lastAttemptedUser);

        GraalJsAuthenticationContext jsAuthenticationContext = new GraalJsAuthenticationContext(authenticationContext);
        Value bindings = context.getBindings("js");
        bindings.putMember("context", jsAuthenticationContext);

        Value result = context.eval(Source.newBuilder("js",
                "context.lastLoginFailedUser",
                "src.js").build());
        assertFalse(result.isNull());
        assertTrue(result.asProxyObject() instanceof JsAuthenticatedUser);

        Value username = context.eval(Source.newBuilder("js",
                "context.lastLoginFailedUser.username",
                "src.js").build());
        assertEquals(username.asString(), LAST_ATTEMPTED_USER_USERNAME);

        Value tenantDomain = context.eval(Source.newBuilder("js",
                "context.lastLoginFailedUser.tenantDomain",
                "src.js").build());
        assertEquals(tenantDomain.asString(), LAST_ATTEMPTED_USER_TENANT_DOMAIN);

        Value userStoreDomain = context.eval(Source.newBuilder("js",
                "context.lastLoginFailedUser.userStoreDomain",
                "src.js").build());
        assertEquals(userStoreDomain.asString(), LAST_ATTEMPTED_USER_USERSTORE_DOMAIN.toUpperCase());
    }

    @Test
    public void testGetLastLoginFailedUserNullFromWrappedContext() throws Exception {

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setProperty(FrameworkConstants.JSAttributes.JS_LAST_LOGIN_FAILED_USER, null);

        GraalJsAuthenticationContext jsAuthenticationContext = new GraalJsAuthenticationContext(authenticationContext);
        Value bindings = context.getBindings("js");
        bindings.putMember("context", jsAuthenticationContext);

        Value result = context.eval(Source.newBuilder("js",
                "context.lastLoginFailedUser",
                "src.js").build());
        assertTrue(result.isNull());
    }

}
