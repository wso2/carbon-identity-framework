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

import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.MapUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.securevault.SecretResolver;

import javax.xml.namespace.QName;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.securevault.commons.MiscellaneousUtil.resolve;

public class IdentityConfigParserTest {

    @AfterClass
    public void tearDown() throws Exception {
        System.clearProperty(ServerConstants.CARBON_HOME);
    }

    @Test
    public void testGetInstance() throws Exception {
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

    @Test(dependsOnMethods = "testGetInstance")
    public void testSecureVaultPropertyResolution() {
        // Test that ClockSkew element can be retrieved and contains expected value
        OMElement clockSkewElement = IdentityConfigParser.getInstance().getConfigElement("ClockSkew");
        assertEquals(clockSkewElement.getText(), "300", "ClockSkew value should be 300");

        // Test secure vault resolution with null resolver returns original text
        String propertyValue = resolve(clockSkewElement.getText(), null);
        assertEquals(propertyValue, "300", "Property value should match when no secret resolver is provided");
    }

    @Test(dependsOnMethods = "testGetInstance")
    public void testSecureVaultPropertyResolutionWithResolver() {
        // Create a mock SecretResolver
        SecretResolver mockSecretResolver = mock(SecretResolver.class);

        // Configure the mock to return true for isInitialized and isTokenProtected
        when(mockSecretResolver.isInitialized()).thenReturn(true);
        when(mockSecretResolver.isTokenProtected(anyString())).thenReturn(true);

        // Configure the mock to return a resolved secret value
        String encryptedValue = "secretAlias:ClockSkew";
        String resolvedSecret = "600";
        when(mockSecretResolver.resolve(encryptedValue)).thenReturn(resolvedSecret);

        // Test secure vault resolution with provided resolver
        String propertyValue = resolve(encryptedValue, mockSecretResolver);
        assertEquals(propertyValue, resolvedSecret, "Property value should be resolved when secret resolver is provided");

        // Test with non-encrypted value - should return original text
        String plainValue = "300";
        when(mockSecretResolver.isTokenProtected(plainValue)).thenReturn(false);
        String plainPropertyValue = resolve(plainValue, mockSecretResolver);
        assertEquals(plainPropertyValue, plainValue, "Plain value should be returned as-is when not token protected");
    }
}
