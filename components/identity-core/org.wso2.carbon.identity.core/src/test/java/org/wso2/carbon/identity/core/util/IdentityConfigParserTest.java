/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.core.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.collections.MapUtils;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceComponent;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.ServerConstants;

import java.nio.file.Paths;

import javax.xml.namespace.QName;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@PrepareForTest(IdentityCoreServiceComponent.class)
@PowerMockIgnore({"javax.net.*", "javax.security.*", "javax.crypto.*", "javax.xml.*", "org.xml.sax.*", "org.w3c.dom" +
        ".*", "org.apache.xerces.*","org.mockito.*"})
public class IdentityConfigParserTest {

    @Mock
    private ConfigurationContextService mockConfigurationContextService;

    @Mock
    private ConfigurationContext mockConfigurationContext;

    @Mock
    private AxisConfiguration mockAxisConfiguration;

    @AfterClass
    public void tearDown() throws Exception {
        System.clearProperty(ServerConstants.CARBON_HOME);
    }

    @Test
    public void testGetInstance() throws Exception {

        mockStatic(IdentityCoreServiceComponent.class);

        when(IdentityCoreServiceComponent.getConfigurationContextService()).thenReturn(mockConfigurationContextService);
        when(mockConfigurationContextService.getServerConfigContext()).thenReturn(mockConfigurationContext);
        when(mockConfigurationContext.getAxisConfiguration()).thenReturn(mockAxisConfiguration);

        String identityXmlPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources",
                "identity.xml").toString();
        System.setProperty(ServerConstants.CARBON_HOME, ".");
        IdentityConfigParser.getInstance(identityXmlPath);
        assertTrue(MapUtils.isNotEmpty(IdentityConfigParser.getInstance().getConfiguration()), "Configuration should " +
                "not be null/empty after initialization");
    }

    @Test(dependsOnMethods = "testGetInstance")
    public void testGetConfiguration() throws Exception {
        assertTrue(MapUtils.isNotEmpty(IdentityConfigParser.getInstance().getConfiguration()), "Configuration should " +
                "not be null/empty");
    }

    @Test(dependsOnMethods = "testGetInstance")
    public void testGetEventListenerConfiguration() throws Exception {
        assertTrue(MapUtils.isNotEmpty(IdentityConfigParser.getEventListenerConfiguration()), "Event listener config " +
                "should not be null/empty");
    }

    @Test(dependsOnMethods = "testGetInstance")
    public void testGetIdentityCacheConfigurationHolder() throws Exception {
        assertTrue(MapUtils.isNotEmpty(IdentityConfigParser.getIdentityCacheConfigurationHolder()), "Cache config " +
                "holder should not be null/empty");
    }

    @Test(dependsOnMethods = "testGetInstance")
    public void testGetIdentityCookieConfigurationHolder() throws Exception {
        assertTrue(MapUtils.isNotEmpty(IdentityConfigParser.getIdentityCookieConfigurationHolder()), "Cookie config " +
                "holder should not be null/empty");
    }

    @Test(dependsOnMethods = "testGetInstance")
    public void testGetReverseProxyConfigurationHolder() throws Exception {
        assertTrue(MapUtils.isNotEmpty(IdentityConfigParser.getReverseProxyConfigurationHolder()), "Reverse " +
                "Proxy config holder should not be null/empty");
    }

    @DataProvider(name = "ConfigElementValues")
    public Object[][] getConfigElement() {
        return new Object[][]{
                {"ClockSkew", "300"},
                {"EnableAskPasswordAdminUI", "false"}
        };
    }

    @Test(dependsOnMethods = "testGetInstance", dataProvider = "ConfigElementValues")
    public void testGetConfigElement(String localPart, String expected) throws Exception {
        assertEquals(IdentityConfigParser.getInstance().getConfigElement(localPart).getText(), expected, "Config " +
                "provided doesn't match the expected.");
    }

    @Test(dependsOnMethods = "testGetInstance")
    public void testGetConfigElementNegative() throws Exception {
        assertNull(IdentityConfigParser.getInstance().getConfigElement("NoneExisting"), "Should return null for none" +
                " existing elements");
    }

    @Test(dependsOnMethods = "testGetInstance")
    public void testGetQNameWithIdentityNS() throws Exception {
        QName element = IdentityConfigParser.getInstance().getQNameWithIdentityNS("TestElement");
        assertEquals(element.getLocalPart(), "TestElement", "Local part should be 'TestElement'");
        assertEquals(element.getNamespaceURI(), IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, "Namespace part " +
                "should be " + IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE);
    }
}
