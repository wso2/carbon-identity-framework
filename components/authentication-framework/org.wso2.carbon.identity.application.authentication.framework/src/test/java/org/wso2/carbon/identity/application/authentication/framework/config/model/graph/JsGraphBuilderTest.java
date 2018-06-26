/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for graph builder with Javascript.
 */
@Test
public class JsGraphBuilderTest extends AbstractFrameworkTest {

    protected static final String APPLICATION_AUTHENTICATION_FILE_NAME = "application-authentication-GraphStepHandlerTest.xml";
    private JsGraphBuilderFactory jsGraphBuilderFactory;

    @BeforeTest
    public void setUp() {
        jsGraphBuilderFactory = new JsGraphBuilderFactory();
        jsGraphBuilderFactory.init();
    }

    @Test
    public void testCreateDirectJavaInvalidStepId() throws Exception {

        ServiceProvider sp1 = getTestServiceProvider("js-sp-1.xml");
        AuthenticationContext context = getAuthenticationContext(sp1);
        Map<Integer, StepConfig> stepConfigMap = new HashMap<>();
        stepConfigMap.put(1, new StepConfig());
        JsGraphBuilder jsGraphBuilder = jsGraphBuilderFactory.createBuilder(context, stepConfigMap);
        jsGraphBuilder.executeStep(2);

        AuthenticationGraph graph = jsGraphBuilder.build();
        assertNull(graph.getStartNode());
    }

    @Test
    public void testCreateDirectJava() throws Exception {

        ServiceProvider sp1 = getTestServiceProvider("js-sp-1.xml");
        AuthenticationContext context = getAuthenticationContext(sp1);
        Map<Integer, StepConfig> stepConfigMap = new HashMap<>();
        stepConfigMap.put(1, new StepConfig());
        stepConfigMap.put(2, new StepConfig());
        JsGraphBuilder jsGraphBuilder = jsGraphBuilderFactory.createBuilder(context, stepConfigMap);
        jsGraphBuilder.executeStep(1);
        jsGraphBuilder.executeStep(2);

        AuthenticationGraph graph = jsGraphBuilder.build();
        assertNotNull(graph.getStartNode());
        assertTrue(graph.getStartNode() instanceof StepConfigGraphNode);

        StepConfigGraphNode firstStep = (StepConfigGraphNode) graph.getStartNode();
        assertNotNull(firstStep.getNext());
        assertTrue(firstStep.getNext() instanceof StepConfigGraphNode);
    }

    @Test
    public void testCreateJavascript() throws Exception {

        String script = "function onInitialRequest(context) { executeStep(1, { onSuccess : function(context) {"
            + "executeStep(2);}})};";

        ServiceProvider sp1 = getTestServiceProvider("js-sp-1.xml");
        AuthenticationContext context = getAuthenticationContext(sp1);

        Map<Integer, StepConfig> stepConfigMap = new HashMap<>();
        stepConfigMap.put(1, new StepConfig());
        stepConfigMap.put(2, new StepConfig());

        JsGraphBuilder jsGraphBuilder = jsGraphBuilderFactory.createBuilder(context, stepConfigMap);
        jsGraphBuilder.createWith(script);

        AuthenticationGraph graph = jsGraphBuilder.build();
        assertNotNull(graph.getStartNode());
        assertTrue(graph.getStartNode() instanceof StepConfigGraphNode);

        StepConfigGraphNode firstStep = (StepConfigGraphNode) graph.getStartNode();
        assertNotNull(firstStep.getNext());
        assertTrue(firstStep.getNext() instanceof DynamicDecisionNode);
    }

    @Test(dataProvider = "filterOptionsDataProvider")
    public void testFilterOptions(Map<String, Map<String, String>> options, StepConfig stepConfig, int
        expectedStepsAfterFilter)
        throws Exception {

        ServiceProvider sp1 = getTestServiceProvider("js-sp-1.xml");
        AuthenticationContext context = getAuthenticationContext(sp1);

        Map<Integer, StepConfig> stepConfigMap = new HashMap<>();
        stepConfigMap.put(1, stepConfig);

        JsGraphBuilder jsGraphBuilder = jsGraphBuilderFactory.createBuilder(context, stepConfigMap);
        jsGraphBuilder.filterOptions(options, stepConfig);
        assertEquals(stepConfig.getAuthenticatorList().size(), expectedStepsAfterFilter, "Authentication options after " +
            "filtering mismatches expected. " + options.toString());
    }

    @DataProvider
    public Object[][] filterOptionsDataProvider() {

        ApplicationAuthenticatorService.getInstance().getLocalAuthenticators().clear();
        LocalAuthenticatorConfig basic = new LocalAuthenticatorConfig();
        basic.setName("BasicAuthenticator");
        basic.setDisplayName("basic");
        LocalAuthenticatorConfig totp = new LocalAuthenticatorConfig();
        totp.setName("TOTPAuthenticator");
        totp.setDisplayName("totp");
        ApplicationAuthenticatorService.getInstance().getLocalAuthenticators().add(basic);
        ApplicationAuthenticatorService.getInstance().getLocalAuthenticators().add(totp);

        IdentityProvider localIdp = new IdentityProvider();
        localIdp.setId("LOCAL");
        localIdp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[0]);

        FederatedAuthenticatorConfig samlFederated = new FederatedAuthenticatorConfig();
        samlFederated.setDisplayName("samlsso");
        samlFederated.setName("SAMLAuthenticator");

        FederatedAuthenticatorConfig oidcFederated = new FederatedAuthenticatorConfig();
        oidcFederated.setDisplayName("oidc");
        oidcFederated.setName("OIDCAuthenticator");

        FederatedAuthenticatorConfig twitterFederated = new FederatedAuthenticatorConfig();
        twitterFederated.setDisplayName("twitter");
        twitterFederated.setName("TwitterAuthenticator");

        IdentityProvider customIdp1 = new IdentityProvider();
        customIdp1.setId("customIdp1");
        customIdp1.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{samlFederated, oidcFederated});
        customIdp1.setDefaultAuthenticatorConfig(samlFederated);

        IdentityProvider customIdp2 = new IdentityProvider();
        customIdp2.setId("customIdp2");
        customIdp2.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{twitterFederated});
        customIdp2.setDefaultAuthenticatorConfig(twitterFederated);

        AuthenticatorConfig basicAuthConfig = new AuthenticatorConfig();
        basicAuthConfig.setName("BasicAuthenticator");
        basicAuthConfig.setEnabled(true);
        basicAuthConfig.getIdps().put("LOCAL", localIdp);

        AuthenticatorConfig totpAuthConfig = new AuthenticatorConfig();
        totpAuthConfig.setName("TOTPAuthenticator");
        totpAuthConfig.setEnabled(true);
        totpAuthConfig.getIdps().put("LOCAL", localIdp);

        AuthenticatorConfig samlAuthConfig = new AuthenticatorConfig();
        samlAuthConfig.setName("SAMLAuthenticator");
        samlAuthConfig.setEnabled(true);
        samlAuthConfig.getIdps().put("customIdp1", customIdp1);

        AuthenticatorConfig oidcAuthConfig = new AuthenticatorConfig();
        oidcAuthConfig.setName("OIDCAuthenticator");
        oidcAuthConfig.setEnabled(true);
        oidcAuthConfig.getIdps().put("customIdp1", customIdp1);

        AuthenticatorConfig twitterAuthConfig = new AuthenticatorConfig();
        twitterAuthConfig.setName("TwitterAuthenticator");
        twitterAuthConfig.setEnabled(true);
        twitterAuthConfig.getIdps().put("customIdp2", customIdp2);

        StepConfig stepWithSingleOption = new StepConfig();
        stepWithSingleOption.setAuthenticatorList(Collections.singletonList(basicAuthConfig));
        Map<String, Map<String, String>> singleOptionConfig = new HashMap<>();
        singleOptionConfig.put("0", Collections.singletonMap("authenticator", "basic"));

        StepConfig stepWithMultipleOptions = new StepConfig();
        stepWithMultipleOptions.setAuthenticatorList(new ArrayList<>(Arrays.asList(basicAuthConfig, totpAuthConfig,
            oidcAuthConfig, twitterAuthConfig)));

        Map<String, String> oidcOption = new HashMap<>();
        oidcOption.put("idp", "customIdp1");
        oidcOption.put("authenticator", "oidc");

        Map<String, String> twitterOption = new HashMap<>();
        twitterOption.put("idp", "customIdp2");
        twitterOption.put("authenticator", "twitter");

        Map<String, String> invalidOption = new HashMap<>();
        invalidOption.put("idp", "customIdp1");
        invalidOption.put("authenticator", "twitter");

        Map<String, Map<String, String>> multipleOptionConfig = new HashMap<>();
        multipleOptionConfig.put("0", Collections.singletonMap("authenticator", "basic"));
        multipleOptionConfig.put("1", oidcOption);
        multipleOptionConfig.put("2", twitterOption);

        Map<String, Map<String, String>> multipleAndInvalidOptionConfig = new HashMap<>();
        multipleAndInvalidOptionConfig.put("0", Collections.singletonMap("authenticator", "basic"));
        multipleAndInvalidOptionConfig.put("1", oidcOption);
        multipleAndInvalidOptionConfig.put("2", invalidOption);

        Map<String, Map<String, String>> idpOnlyOptionConfig = new HashMap<>();
        idpOnlyOptionConfig.put("0", Collections.singletonMap("authenticator", "basic"));
        idpOnlyOptionConfig.put("1", Collections.singletonMap("idp", "customIdp1"));

        Map<String, Map<String, String>> singleInvalidOptionConfig = new HashMap<>();
        singleInvalidOptionConfig.put("0", invalidOption);

        return new Object[][]{
            {singleOptionConfig, duplicateStepConfig(stepWithSingleOption), 1},
            {singleOptionConfig, duplicateStepConfig(stepWithMultipleOptions), 1},
            {multipleOptionConfig, duplicateStepConfig(stepWithMultipleOptions), 3},
            {multipleAndInvalidOptionConfig, duplicateStepConfig(stepWithMultipleOptions), 2},
            {singleInvalidOptionConfig, duplicateStepConfig(stepWithMultipleOptions), 4},
            {idpOnlyOptionConfig, duplicateStepConfig(stepWithMultipleOptions), 2},
        };
    }

    private StepConfig duplicateStepConfig(StepConfig stepConfig) {

        StepConfig newStepConfig = new StepConfig();
        newStepConfig.setAuthenticatorList(new ArrayList<>(stepConfig.getAuthenticatorList()));
        return newStepConfig;
    }
}
