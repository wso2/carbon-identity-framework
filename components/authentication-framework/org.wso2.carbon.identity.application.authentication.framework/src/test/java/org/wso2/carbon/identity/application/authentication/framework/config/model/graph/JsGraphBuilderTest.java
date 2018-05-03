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
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.HashMap;
import java.util.Map;

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

    public void testCreate_DirectJava_InvalidStepId() throws Exception {

        ServiceProvider sp1 = getTestServiceProvider("js-sp-1.xml");
        AuthenticationContext context = getAuthenticationContext(sp1);
        Map<Integer, StepConfig> stepConfigMap = new HashMap<>();
        stepConfigMap.put(1, new StepConfig());
        JsGraphBuilder jsGraphBuilder = jsGraphBuilderFactory.createBuilder(context, stepConfigMap);
        HashMap stepsToExecute = new HashMap<String, Object>();
        stepsToExecute.put("id", "2");
        jsGraphBuilder.executeStep(stepsToExecute);

        AuthenticationGraph graph = jsGraphBuilder.build();
        assertNull(graph.getStartNode());
    }

    public void testCreate_DirectJava() throws Exception {

        ServiceProvider sp1 = getTestServiceProvider("js-sp-1.xml");
        AuthenticationContext context = getAuthenticationContext(sp1);
        Map<Integer, StepConfig> stepConfigMap = new HashMap<>();
        stepConfigMap.put(1, new StepConfig());
        stepConfigMap.put(2, new StepConfig());
        JsGraphBuilder jsGraphBuilder = jsGraphBuilderFactory.createBuilder(context, stepConfigMap);
        HashMap stepsToExecute = new HashMap<String, Object>();
        stepsToExecute.put("id", "1");
        jsGraphBuilder.executeStep(stepsToExecute);
        stepsToExecute = new HashMap<String, Object>();
        stepsToExecute.put("id", "2");
        jsGraphBuilder.executeStep(stepsToExecute);

        AuthenticationGraph graph = jsGraphBuilder.build();
        assertNotNull(graph.getStartNode());
        assertTrue(graph.getStartNode() instanceof StepConfigGraphNode);

        StepConfigGraphNode firstStep = (StepConfigGraphNode) graph.getStartNode();
        assertNotNull(firstStep.getNext());
        assertTrue(firstStep.getNext() instanceof StepConfigGraphNode);
    }

    public void testCreate_Javascript() throws Exception {
        String script = "function onInitialRequest(context) { executeStep({id :'1', on : {success : function(context) {"
                + "executeStep({id :'2'});}}})};";

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

}