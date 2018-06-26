/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import java.util.HashMap;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test
public class JsAuthenticationContextTest {

    public static final String TEST_IDP = "testIdP";
    private ScriptEngine scriptEngine;

    @BeforeClass
    public void setUp() {

        scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
    }

    @Test
    public void testClaimAssignment() throws ScriptException {

        ClaimMapping claimMapping1 = ClaimMapping.build("", "", "", false);

        ClaimMapping claimMapping2 = ClaimMapping.build("Test.Remote.Claim.Url.2", "Test.Remote.Claim.Url.2", "",
            false);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.getUserAttributes().put(claimMapping1, "TestClaimVal1");
        authenticatedUser.getUserAttributes().put(claimMapping2, "TestClaimVal2");
        AuthenticationContext authenticationContext = new AuthenticationContext();
        setupAuthContextWithStepData(authenticationContext, authenticatedUser);

        JsAuthenticationContext jsAuthenticationContext = new JsAuthenticationContext(authenticationContext);
        Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("context", jsAuthenticationContext);

        Object result = scriptEngine.eval("context.steps[1].subject.remoteClaims['Test.Remote.Claim.Url.1']");
        assertNull(result);
        result = scriptEngine.eval("context.steps[1].subject.remoteClaims['Test.Remote.Claim.Url.2']");
        assertEquals(result, "TestClaimVal2");

        scriptEngine.eval("context.steps[1].subject.remoteClaims['Test.Remote.Claim.Url.2'] = 'Modified2'");
        result = scriptEngine.eval("context.steps[1].subject.remoteClaims['Test.Remote.Claim.Url.2']");
        assertEquals(result, "Modified2");

    }

    private void setupAuthContextWithStepData(AuthenticationContext context, AuthenticatedUser authenticatedUser) {

        SequenceConfig sequenceConfig = new SequenceConfig();
        Map<Integer, StepConfig> stepConfigMap = new HashMap<>();
        StepConfig stepConfig = new StepConfig();
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
    public void testRemoteAddition() throws ScriptException {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        AuthenticationContext authenticationContext = new AuthenticationContext();
        setupAuthContextWithStepData(authenticationContext, authenticatedUser);

        JsAuthenticationContext jsAuthenticationContext = new JsAuthenticationContext(authenticationContext);
        Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("context", jsAuthenticationContext);

        scriptEngine.eval("context.steps[1].subject.remoteClaims['testClaim']='testValue'");

        ClaimMapping claimMapping = ClaimMapping.build("testClaim", "testClaim", "", false);
        String claimCreatedByJs = authenticatedUser.getUserAttributes().get(claimMapping);
        assertEquals(claimCreatedByJs, "testValue");
    }

    @Test
    public void testGetServiceProviderFromWrappedContext() throws Exception {

        final String SERVICE_PROVIDER_NAME = "service_provider_js_test";

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setServiceProviderName(SERVICE_PROVIDER_NAME);

        JsAuthenticationContext jsAuthenticationContext = new JsAuthenticationContext(authenticationContext);
        Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("context", jsAuthenticationContext);

        Object result = scriptEngine.eval("context.serviceProviderName");
        assertNotNull(result);
        assertEquals(result, SERVICE_PROVIDER_NAME, "Service Provider name set in AuthenticationContext is not " +
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

        JsAuthenticationContext jsAuthenticationContext = new JsAuthenticationContext(authenticationContext);
        Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("context", jsAuthenticationContext);

        Object result = scriptEngine.eval("context.lastLoginFailedUser");
        assertNotNull(result);
        assertTrue(result instanceof JsAuthenticatedUser);

        String username = (String) scriptEngine.eval("context.lastLoginFailedUser.username");
        assertEquals(username, LAST_ATTEMPTED_USER_USERNAME);

        String tenantDomain = (String) scriptEngine.eval("context.lastLoginFailedUser.tenantDomain");
        assertEquals(tenantDomain, LAST_ATTEMPTED_USER_TENANT_DOMAIN);

        String userStoreDomain = (String) scriptEngine.eval("context.lastLoginFailedUser.userStoreDomain");
        assertEquals(userStoreDomain, LAST_ATTEMPTED_USER_USERSTORE_DOMAIN.toUpperCase());
    }

    @Test
    public void testGetLastLoginFailedUserNullFromWrappedContext() throws Exception {

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setProperty(FrameworkConstants.JSAttributes.JS_LAST_LOGIN_FAILED_USER, null);

        JsAuthenticationContext jsAuthenticationContext = new JsAuthenticationContext(authenticationContext);
        Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("context", jsAuthenticationContext);

        Object result = scriptEngine.eval("context.lastLoginFailedUser");
        assertNull(result);
    }
}
