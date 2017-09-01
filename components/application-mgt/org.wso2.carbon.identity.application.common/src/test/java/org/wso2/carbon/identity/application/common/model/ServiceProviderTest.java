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

package org.wso2.carbon.identity.application.common.model;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.identity.application.common.model.graph.AuthenticationGraphConfig;
import org.wso2.carbon.identity.application.common.model.graph.StepNode;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;

public class ServiceProviderTest extends TestCase {

    public void testBuild() throws Exception {
        ServiceProvider sp = getTestServiceProvider("test-sp-1.xml");
        assertNotNull(sp);
    }

    public void testGetLocalAndOutBoundAuthenticationConfig_AuthenticationGraph() throws Exception {
        ServiceProvider sp = getTestServiceProvider("graph-sp-1.xml");
        assertNotNull(sp.getLocalAndOutBoundAuthenticationConfig().getAuthenticationGraphConfig());

        AuthenticationGraphConfig graph = sp.getLocalAndOutBoundAuthenticationConfig().getAuthenticationGraphConfig();
        assertNotNull(graph.getStartNode());
        assertEquals("basic_auth", graph.getStartNode().getName());

        assertTrue(graph.getStartNode() instanceof StepNode);

        StepNode stepNode = (StepNode) graph.getStartNode();
        assertNotNull(stepNode.getNext());
        assertNotNull(stepNode.getAuthenticationStep());
    }

    public void testGetLocalAndOutBoundAuthenticationConfig_AuthenticationGraph_InfiniteLoop() throws Exception {
        ServiceProvider sp2 = getTestServiceProvider("graph-sp-2-infinite-loop.xml");
        assertNotNull(sp2.getLocalAndOutBoundAuthenticationConfig().getAuthenticationGraphConfig());
    }

    private ServiceProvider getTestServiceProvider(String name) throws XMLStreamException {
        InputStream inputStream = ServiceProviderTest.class.getResourceAsStream(name);
        OMElement documentElement = new StAXOMBuilder(inputStream).getDocumentElement();
        return ServiceProvider.build(documentElement);
    }
}