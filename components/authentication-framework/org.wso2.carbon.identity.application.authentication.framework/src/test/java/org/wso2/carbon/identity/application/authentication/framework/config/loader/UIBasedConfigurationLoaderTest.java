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

package org.wso2.carbon.identity.application.authentication.framework.config.loader;

import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthDecisionPointNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.EndStep;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.StepConfigGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.Collections;

public class UIBasedConfigurationLoaderTest extends AbstractFrameworkTest {

    private UIBasedConfigurationLoader loader = new UIBasedConfigurationLoader();

    public void testGetSequence_Deprecated() throws Exception {
        ServiceProvider testSp1 = new ServiceProvider();
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = new LocalAndOutboundAuthenticationConfig();
        testSp1.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);

        AuthenticationStep step1 = new AuthenticationStep();
        step1.setStepOrder(1);
        AuthenticationStep step2 = new AuthenticationStep();
        step1.setStepOrder(2);

        AuthenticationStep[] authenticationSteps = new AuthenticationStep[] { step1, step2 };

        localAndOutboundAuthenticationConfig.setAuthenticationSteps(authenticationSteps);

        SequenceConfig sequenceConfig = loader.getSequence(testSp1, "test_domain");
        assertNotNull(sequenceConfig);

        assertNotNull(sequenceConfig.getStepMap());

        assertNotNull(sequenceConfig.getStepMap().get(1));
        assertNotNull(sequenceConfig.getStepMap().get(2));
    }

    public void testGetSequence_WithGraph() throws Exception {
        ServiceProvider graphSp1 = getTestServiceProvider("graph-sp-1.xml");
        AuthenticationContext authenticationContext = getAuthenticationContext("test_app",
                "application-authentication-GraphStepHandlerTest.xml", graphSp1);
        SequenceConfig sequenceConfig = loader
                .getSequenceConfig(authenticationContext, Collections.<String, String[]>emptyMap(), graphSp1);
        AuthenticationGraph graph = sequenceConfig.getAuthenticationGraph();
        assertNotNull(graph);
        assertEquals("basic_auth", graph.getStartNode().getName());
        assertTrue(graph.getStartNode() instanceof StepConfigGraphNode);
        StepConfigGraphNode step1 = (StepConfigGraphNode) graph.getStartNode();
        assertNotNull(step1.getNext());
        assertTrue(step1.getNext() instanceof AuthDecisionPointNode);
        AuthDecisionPointNode decisionPointNode1 = (AuthDecisionPointNode) step1.getNext();
        assertNotNull(decisionPointNode1.getDefaultEdge());
        assertTrue(decisionPointNode1.getDefaultEdge() instanceof StepConfigGraphNode);
        StepConfigGraphNode step2 = (StepConfigGraphNode) decisionPointNode1.getDefaultEdge();
        assertEquals("sample_auth", step2.getName());
        assertNotNull(step2.getNext());
        assertTrue(step2.getNext() instanceof EndStep);


        assertNotNull(step1.getAuthenticatorList());
        assertEquals(1, step1.getAuthenticatorList().size());
        assertEquals("BasicAuthenticator", step1.getAuthenticatorList().get(0).getName());
    }

}