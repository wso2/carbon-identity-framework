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

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;

public class ServiceProviderTest extends TestCase {

    public void testBuild() throws Exception {
        ServiceProvider sp = getTestServiceProvider();
        assertNotNull(sp);
    }

    public void testGetLocalAndOutBoundAuthenticationConfig_AuthenticationChainConfigs() throws Exception {
        ServiceProvider sp = getTestServiceProvider();
        assertNotNull(sp.getLocalAndOutBoundAuthenticationConfig());
        assertNotNull(sp.getLocalAndOutBoundAuthenticationConfig().getAuthenticationChainConfigs());
        AuthenticationChainConfig[] chainConfigs = sp.getLocalAndOutBoundAuthenticationConfig()
                .getAuthenticationChainConfigs();
        assertEquals(3, chainConfigs.length);
        assertEquals("chain1", chainConfigs[0].getName());
        assertEquals("chain2", chainConfigs[1].getName());
        assertEquals("chain3", chainConfigs[2].getName());

        assertNotNull(chainConfigs[0].getStepConfigs());
        assertEquals(2, chainConfigs[0].getStepConfigs().length);
        assertEquals(1, chainConfigs[1].getStepConfigs().length);

        assertNotNull(chainConfigs[0].getAcr());
        assertEquals(1, chainConfigs[0].getAcr().length);
        assertEquals(3, chainConfigs[1].getAcr().length);
        assertEquals(0, chainConfigs[2].getAcr().length);

        assertEquals("acr2", chainConfigs[1].getAcr()[0]);
        assertEquals("none", chainConfigs[1].getAcr()[1]);
        assertEquals("foobar", chainConfigs[1].getAcr()[2]);
    }

    private ServiceProvider getTestServiceProvider() throws XMLStreamException {
        InputStream inputStream = ServiceProviderTest.class.getResourceAsStream("test-sp-1.xml");
        OMElement documentElement = new StAXOMBuilder(inputStream).getDocumentElement();
        return ServiceProvider.build(documentElement);
    }
}