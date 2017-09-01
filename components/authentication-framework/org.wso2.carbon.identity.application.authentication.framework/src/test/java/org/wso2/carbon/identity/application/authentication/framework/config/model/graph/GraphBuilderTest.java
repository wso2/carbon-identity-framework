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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test
public class GraphBuilderTest extends AbstractFrameworkTest {

    public void testCreateWith() throws Exception {

        ServiceProvider sp1 = getTestServiceProvider("graph-sp-1.xml");
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.createWith(sp1.getLocalAndOutBoundAuthenticationConfig().getAuthenticationGraphConfig());
        AuthenticationGraph g1 = graphBuilder.getGraph();
        assertNotNull(g1);
        assertNotNull(g1.getStartNode());
        assertTrue(g1.getStartNode() instanceof StepConfigGraphNode);
        assertNotNull(((StepConfigGraphNode) g1.getStartNode()).getNext());
        assertTrue(((StepConfigGraphNode) g1.getStartNode()).getNext() instanceof AuthDecisionPointNode);
    }

    public void testDefaultLink() throws Exception {

        ServiceProvider sp1 = getTestServiceProvider("graph-sp-1.xml");
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.createWith(sp1.getLocalAndOutBoundAuthenticationConfig().getAuthenticationGraphConfig());
        AuthenticationGraph g1 = graphBuilder.getGraph();
        AuthDecisionPointNode decisionPointNode1 = (AuthDecisionPointNode) ((StepConfigGraphNode) g1.getStartNode())
                .getNext();
        assertNotNull(decisionPointNode1.getDefaultEdge());
        assertEquals("sample_auth", decisionPointNode1.getDefaultEdge().getName());
        assertEquals("sample_auth", decisionPointNode1.getOutcome("hwk").getDestination().getName());
        assertEquals(decisionPointNode1.getDefaultEdge(), decisionPointNode1.getOutcome("hwk").getDestination(),
                "hwk outcome and default outcome should be the same java objects");
    }

    /**
     * Tests whether a circular graph is building properly without stack overflow.
     *
     * @throws Exception
     */
    public void testCreateWithCircle() throws Exception {

        ServiceProvider sp1 = getTestServiceProvider("graph-sp-2-with-circle.xml");
        GraphBuilder graphBuilder = new GraphBuilder();
        graphBuilder.createWith(sp1.getLocalAndOutBoundAuthenticationConfig().getAuthenticationGraphConfig());
        AuthenticationGraph g1 = graphBuilder.getGraph();
        assertNotNull(g1);
        assertNotNull(g1.getStartNode());
        assertTrue(g1.getStartNode() instanceof StepConfigGraphNode);
        assertNotNull(((StepConfigGraphNode) g1.getStartNode()).getNext());
        assertTrue(((StepConfigGraphNode) g1.getStartNode()).getNext() instanceof AuthDecisionPointNode);
    }

    public void testCreateStepConfigurationObject() throws Exception {

    }
}