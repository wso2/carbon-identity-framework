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

package org.wso2.carbon.identity.application.authentication.framework.internal.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;

import java.io.InputStream;
import javax.xml.stream.XMLStreamException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests the AuthenticationMethodNameTranslatorImpl.
 *
 */
@Test
public class AuthenticationMethodNameTranslatorImplTest {

    public void testInitializeConfigsWithServerConfig_NoContextMapping() throws Exception {

        AuthenticationMethodNameTranslatorImpl translator = new AuthenticationMethodNameTranslatorImpl();
        String path = this.getClass().getResource("identity-no-context-mappings.xml").getFile();

        IdentityConfigParser.getInstance(path);
        translator.initializeConfigsWithServerConfig();
    }

    public void testTranslateToInternalAmr() throws Exception {

        AuthenticationMethodNameTranslatorImpl translator = getTranslator("identity-xml-test1.xml");
        assertNotNull(translator.translateToInternalAmr("pwd", "openid"));
        assertEquals("SampleHardwareKeyAuthenticator", translator.translateToInternalAmr("hwk", "openid"));
    }

    public void testTranslateToExternalAmr() throws Exception {

        AuthenticationMethodNameTranslatorImpl translator = getTranslator("identity-xml-test1.xml");
        assertNotNull(translator.translateToExternalAmr("BasicAuthenticator", "openid"));
        assertEquals(1, translator.translateToExternalAmr("SampleHardwareKeyAuthenticator", "openid").size());
        assertTrue(translator.translateToExternalAmr("SampleHardwareKeyAuthenticator", "openid").contains("hwk"));
        assertTrue(translator.translateToExternalAmr("SomeOtherHwkAuthenticator", "openid").contains("hwk1"));
        assertTrue(translator.translateToExternalAmr("SomeOtherHwkAuthenticator", "openid").contains("hwk2"));
    }

    private AuthenticationMethodNameTranslatorImpl getTranslator(String fileName) throws XMLStreamException {

        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        OMElement documentElement = new StAXOMBuilder(inputStream).getDocumentElement();
        AuthenticationMethodNameTranslatorImpl result = new AuthenticationMethodNameTranslatorImpl();
        result.initializeConfigs(documentElement);

        return result;
    }
}