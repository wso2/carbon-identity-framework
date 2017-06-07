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

package org.wso2.carbon.identity.application.authentication.framework.config.builder;

import edu.emory.mathcs.backport.java.util.Arrays;
import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AcrRule;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.AuthenticationChainConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import javax.xml.stream.XMLStreamException;

/**
 * Tests DefaultAdaptiveAuthEvaluator.
 *
 */
public class DefaultAdaptiveAuthEvaluatorTest extends TestCase {

    private DefaultAuthStepsSelector defaultAdaptiveAuthEvaluator = new DefaultAuthStepsSelector();

    protected void setUp() {
        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.toString());
    }

    public void testResolveSequenceChainName_SingleAcrInContext() throws XMLStreamException {
        ServiceProvider serviceProvider = getTestServiceProvider("test-sp-1.xml");

        AuthenticationChainConfig resolvedChain = defaultAdaptiveAuthEvaluator
                .resolveSequenceChain(getAuthenticationContext(new String[] { "acr1" }), serviceProvider);
        assertNotNull(resolvedChain);
        assertEquals("The ACR acr1 is only available in chain1.", "chain1", resolvedChain.getName());

        resolvedChain = defaultAdaptiveAuthEvaluator
                .resolveSequenceChain(getAuthenticationContext(new String[] { "foobar" }), serviceProvider);
        assertNotNull(resolvedChain);
        assertEquals("The ACR foobar is only available in chain2.", "chain2", resolvedChain.getName());
    }

    public void testResolveSequenceChainName_MultipleAcrInContext() throws XMLStreamException {
        ServiceProvider serviceProvider = getTestServiceProvider("test-sp-1.xml");

        AuthenticationChainConfig resolvedChain = defaultAdaptiveAuthEvaluator
                .resolveSequenceChain(getAuthenticationContext(new String[] { "acr1", "acr2" }), serviceProvider);
        assertNotNull(resolvedChain);
        assertEquals("The ACR acr1 is in priority in chain1.", "chain1", resolvedChain.getName());

        resolvedChain = defaultAdaptiveAuthEvaluator
                .resolveSequenceChain(getAuthenticationContext(new String[] { "acr2", "acr1" }), serviceProvider);
        assertNotNull(resolvedChain);
        assertEquals("The ACR acr2 is in priority in chain2.", "chain2", resolvedChain.getName());

        resolvedChain = defaultAdaptiveAuthEvaluator
                .resolveSequenceChain(getAuthenticationContext(new String[] { "foobar", "acr1" }), serviceProvider);
        assertNotNull(resolvedChain);
        assertEquals("The ACR acr1 has high priority in chain1 than foobar at chain2.", "chain1",
                resolvedChain.getName());
    }

    private AuthenticationContext getAuthenticationContext(String[] acrArray) {
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setAcrRule(AcrRule.EXACT);
        authenticationContext.setAcrRequested(Arrays.asList(acrArray));
        return authenticationContext;
    }

    private List<SequenceConfig> getSequenceConfigs()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        FileBasedConfigurationBuilder configurationBuilder = FileBasedConfigurationBuilder.getInstance();
        Method buildConfigurationMethod = FileBasedConfigurationBuilder.class
                .getDeclaredMethod("buildConfiguration", InputStream.class);
        buildConfigurationMethod.setAccessible(true);
        InputStream file = DefaultAdaptiveAuthEvaluatorTest.class
                .getResourceAsStream("application-authentication-multi-step-1.xml");
        buildConfigurationMethod.invoke(configurationBuilder, file);
        return FileBasedConfigurationBuilder.getInstance().getSequenceList();
    }

    private ServiceProvider getTestServiceProvider(String spFileName) throws XMLStreamException {
        InputStream inputStream = DefaultAdaptiveAuthEvaluatorTest.class.getResourceAsStream(spFileName);
        OMElement documentElement = new StAXOMBuilder(inputStream).getDocumentElement();
        return ServiceProvider.build(documentElement);
    }
}