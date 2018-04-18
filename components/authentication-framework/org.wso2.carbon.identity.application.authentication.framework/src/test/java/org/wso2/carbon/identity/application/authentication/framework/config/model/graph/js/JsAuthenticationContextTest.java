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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

@Test
public class JsAuthenticationContextTest {

    private ScriptEngine scriptEngine;

    @BeforeClass
    public void setUp() {

        scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
    }

    public void testClaimAssignment() throws ScriptException {

        ClaimMapping claimMapping1 = new ClaimMapping();
        claimMapping1.setLocalClaim(new Claim());
        claimMapping1.setRemoteClaim(new Claim());

        ClaimMapping claimMapping2 = new ClaimMapping();
        Claim localClaim1 = new Claim();
        localClaim1.setClaimUri("Test.Local.Claim.Url.2");
        Claim remoteClaim1 = new Claim();
        remoteClaim1.setClaimUri("Test.Remote.Claim.Url.2");
        claimMapping2.setLocalClaim(localClaim1);
        claimMapping2.setRemoteClaim(remoteClaim1);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.getUserAttributes().put(claimMapping1, "TestClaimVal1");
        authenticatedUser.getUserAttributes().put(claimMapping2, "TestClaimVal2");
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setSubject(authenticatedUser);

        JsAuthenticationContext jsAuthenticationContext = new JsAuthenticationContext(authenticationContext);
        Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("context", jsAuthenticationContext);

        Object result = scriptEngine.eval("context.lastAuthenticatedUser.claims.local['Test.Local.Claim.Url.1']");
        assertNull(result);
        result = scriptEngine.eval("context.lastAuthenticatedUser.claims.local['Test.Local.Claim.Url.2']");
        assertEquals(result, "TestClaimVal2");

        scriptEngine.eval("context.lastAuthenticatedUser.claims.local['Test.Local.Claim.Url.2'] = 'Modified2'");
        result = scriptEngine.eval("context.lastAuthenticatedUser.claims.local['Test.Local.Claim.Url.2']");
        assertEquals(result, "Modified2");

    }

    @Test(dataProvider = "claimCreation")
    public void testClaimAddition(String scriptPart, boolean isNull, String expectedValue) throws ScriptException {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        AuthenticationContext authenticationContext = new AuthenticationContext();

        authenticationContext.setSubject(authenticatedUser);

        JsAuthenticationContext jsAuthenticationContext = new JsAuthenticationContext(authenticationContext);
        Bindings bindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("context", jsAuthenticationContext);

        scriptEngine.eval(scriptPart);
        scriptEngine.eval("context.lastAuthenticatedUser.claims.push(claim1)");

        ClaimMapping claimMapping2 = new ClaimMapping();
        Claim localClaim1 = new Claim();
        localClaim1.setClaimUri("local.uri");
        Claim remoteClaim1 = new Claim();
        remoteClaim1.setClaimUri("remote.uri");
        claimMapping2.setLocalClaim(localClaim1);
        claimMapping2.setRemoteClaim(remoteClaim1);

        String claimCreatedByJs = authenticatedUser.getUserAttributes().get(claimMapping2);
        assertEquals(claimCreatedByJs, expectedValue);
    }

    @DataProvider(name = "claimCreation")
    public Object[][] getClaimCreationData() {

        return new Object[][] { { "claim1 =  {'local' : {'uri' : 'local.uri'}, 'remote' : {'uri' : 'remote.uri'},"
                + " 'value' : 'AssignedByJs'}", false, "AssignedByJs" },
                { "claim1 = {}; claim1.local = {}; claim1.remote= {}; "
                        + "claim1.local.uri = 'local.uri';claim1.remote.uri = 'remote.uri';"
                        + "claim1.value = 'AssignedByJs'", false, "AssignedByJs" },
                { "claim1 = {'local' : {'uri' : 'local.uri'}, 'remote' : {'uri' : 'remote.uri'}}", true, null } };
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
}
