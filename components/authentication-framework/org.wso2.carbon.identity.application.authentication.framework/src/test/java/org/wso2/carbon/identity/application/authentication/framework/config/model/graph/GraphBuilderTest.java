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

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;

public class GraphBuilderTest extends AbstractFrameworkTest {

    public void testCreateWith() throws Exception {
        ServiceProvider sp1 = getTestServiceProvider("graph-sp-1.xml");
        GraphBuilder graphBuilder = new GraphBuilder();
        AuthenticationGraph g1 = graphBuilder
                .createWith(sp1.getLocalAndOutBoundAuthenticationConfig().getAuthenticationGraphConfig());
        assertNotNull(g1);
    }

    public void testCreateStepConfigurationObject() throws Exception {
    }
}