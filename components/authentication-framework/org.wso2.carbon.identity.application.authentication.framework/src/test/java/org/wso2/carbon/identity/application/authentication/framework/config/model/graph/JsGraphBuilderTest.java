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
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator2;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
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

    public void testCreate_DirectJava() throws Exception {

        ServiceProvider sp1 = getTestServiceProvider("js-sp-1.xml");
        AuthenticationContext context = getAuthenticationContext("", APPLICATION_AUTHENTICATION_FILE_NAME, sp1);
        Map<Integer, StepConfig> stepConfigMap = new HashMap<>();
        stepConfigMap.put(1, new StepConfig());
        stepConfigMap.put(2, new StepConfig());
        JsGraphBuilder jsGraphBuilder = jsGraphBuilderFactory.createBuilder(context, stepConfigMap);
        jsGraphBuilder.execute("0");
        jsGraphBuilder.makeDecisionWith(c -> "s").when("s").thenExecute("1").whenNoMatch().thenExecute("0");

        checkGraphStructure(jsGraphBuilder);
        checkDecision(jsGraphBuilder);
    }

    public void testCreate_Javascript() throws Exception {
        String script = "function(context) {" + "execute('1');" + "makeDecisionWith(function(c) {return 's';})"
                + ".when('s').thenExecute('2')" + ".whenNoMatch().thenExecute('1')" + "}";

        ServiceProvider sp1 = getTestServiceProvider("js-sp-1.xml");
        AuthenticationContext context = getAuthenticationContext("", APPLICATION_AUTHENTICATION_FILE_NAME, sp1);

        Map<Integer, StepConfig> stepConfigMap = new HashMap<>();
        stepConfigMap.put(1, new StepConfig());
        stepConfigMap.put(2, new StepConfig());

        JsGraphBuilder jsGraphBuilder = jsGraphBuilderFactory.createBuilder(context, stepConfigMap);
        jsGraphBuilder.createWith(script);

        checkGraphStructure(jsGraphBuilder);
        checkDecision(jsGraphBuilder);
    }

    private void checkGraphStructure(JsGraphBuilder jsGraphBuilder) {
        AuthenticationGraph graph = jsGraphBuilder.build();
        assertNotNull(graph.getStartNode());
        assertTrue(graph.getStartNode() instanceof StepConfigGraphNode);

        StepConfigGraphNode firstStep = (StepConfigGraphNode) graph.getStartNode();
        assertNotNull(firstStep.getNext());
        assertTrue(firstStep.getNext() instanceof AuthDecisionPointNode);
        AuthDecisionPointNode firstDecision = (AuthDecisionPointNode) firstStep.getNext();

        assertNotNull(firstDecision.getDefaultEdge());
        assertNotNull(firstDecision.getOutcome("s"));
        assertTrue(firstDecision.getOutcome("s").getDestination() instanceof StepConfigGraphNode);
    }

    private void checkDecision(JsGraphBuilder jsGraphBuilder) {
        AuthenticationGraph graph = jsGraphBuilder.build();
        StepConfigGraphNode firstStep = (StepConfigGraphNode) graph.getStartNode();
        AuthDecisionPointNode firstDecision = (AuthDecisionPointNode) firstStep.getNext();
        AuthenticationDecisionEvaluator2 authenticationDecisionEvaluator = firstDecision
                .getAuthenticationDecisionEvaluator();
        assertNotNull(authenticationDecisionEvaluator);
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setTenantDomain("test_domain");
        String result = authenticationDecisionEvaluator.evaluate(authenticationContext);
        assertNotNull(result);
        assertEquals("s", result);
    }

}