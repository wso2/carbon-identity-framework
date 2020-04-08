/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.URLResolverService;
import org.wso2.carbon.identity.core.model.ServiceURL;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;

@PrepareForTest({ServerConfiguration.class, CarbonUtils.class, IdentityCoreServiceComponent.class, NetworkUtils.class,
        IdentityTenantUtil.class, URLResolverService.class, PrivilegedCarbonContext.class})
@PowerMockIgnore({"javax.net.*", "javax.security.*", "javax.crypto.*", "javax.xml.*", "org.xml.sax.*", "org.w3c.dom" +
        ".*", "org.apache.xerces.*"})
public class DefaultServiceURLBuilderTest {

    @Mock
    private ServerConfiguration mockServerConfiguration;
    @Mock
    private ConfigurationContextService mockConfigurationContextService;
    @Mock
    private ConfigurationContext mockConfigurationContext;
    @Mock
    private AxisConfiguration mockAxisConfiguration;

    @BeforeMethod
    public void setUp() throws Exception {

        mockStatic(CarbonUtils.class);
        mockStatic(ServerConfiguration.class);
        mockStatic(NetworkUtils.class);
        mockStatic(IdentityCoreServiceComponent.class);
        mockStatic(IdentityTenantUtil.class);
        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);

        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(ServerConfiguration.getInstance()).thenReturn(mockServerConfiguration);
        when(IdentityCoreServiceComponent.getConfigurationContextService()).thenReturn(mockConfigurationContextService);
        when(mockConfigurationContextService.getServerConfigContext()).thenReturn(mockConfigurationContext);
        when(mockConfigurationContext.getAxisConfiguration()).thenReturn(mockAxisConfiguration);
        try {
            when(NetworkUtils.getLocalHostname()).thenReturn("localhost");
        } catch (SocketException e) {
            // Mock behaviour, hence ignored
        }

        System.setProperty(IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTP_PROPERTY, "9763");
        System.setProperty(IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTPS_PROPERTY, "9443");
    }

    @AfterMethod
    public void tearDown() throws Exception {

        System.clearProperty(IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTP_PROPERTY);
        System.clearProperty(IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTPS_PROPERTY);
    }

    @Test
    public void testAddPath() {

        String testPath = "/testPath";
        ServiceURL serviceURL = null;
        try {
            serviceURL = ServiceURLBuilder.create().addPath(testPath).build();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(serviceURL.getUrlPath(), testPath);
    }

    @Test
    public void testAddPaths() {

        String testPath1 = "/testPath1";
        String testPath2 = "testPath2/";
        String testPath3 = "/testPath3/";
        ServiceURL serviceURL = null;
        try {
            serviceURL = ServiceURLBuilder.create().addPath(testPath1, testPath2, testPath3).build();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(serviceURL.getUrlPath(), "/testPath1/testPath2/testPath3");
    }

    @Test
    public void testAddParameter() {

        ServiceURL serviceURL = null;
        try {
            serviceURL = ServiceURLBuilder.create().addParameter("key", "value").build();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(serviceURL.getParameter("key"), "value");
    }

    @Test
    public void testAddParameters() {

        ServiceURL serviceURL = null;
        try {
            serviceURL =
                    ServiceURLBuilder.create().addParameter("key1", "value1").addParameter("key2", "value2").build();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(serviceURL.getParameter("key1"), "value1");
        assertEquals(serviceURL.getParameter("key2"), "value2");
    }

    @Test
    public void testSetFragment() {

        when(CarbonUtils.getManagementTransport()).thenReturn("https");
        ServiceURL serviceURL = null;
        try {
            serviceURL = ServiceURLBuilder.create().setFragment("fragment").build();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(serviceURL.getFragment(), "fragment");
    }

    @Test
    public void testAddFragmentParameter() {

        ServiceURL serviceURL = null;

        try {
            serviceURL =
                    ServiceURLBuilder.create().addFragmentParameter("key1", "value1").addFragmentParameter("key2",
                            "value2").build();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(serviceURL.getFragment(), "key1=value1&key2=value2");
    }

    @Test
    public void testAddFragmentParameters() {

        ServiceURL serviceURL = null;

        try {
            serviceURL =
                    ServiceURLBuilder.create().addFragmentParameter("key1", "value1").addFragmentParameter("key2",
                            "value2").build();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(serviceURL.getFragment(), "key1=value1&key2=value2");
    }

    @Test
    public void testBuild() {

        ServiceURL serviceURL = null;
        String testPath1 = "/testPath1";
        String testPath2 = "testPath2/";
        String testPath3 = "/testPath3/";
        String[] keysList = {"key1", "key2", "key3"};
        String[] valuesList = {"value1", "value2", "value3"};

        try {
            serviceURL =
                    ServiceURLBuilder.create().addPath(testPath1, testPath2, testPath3).addParameter(keysList[0],
                            valuesList[0]).addParameter(keysList[1], valuesList[1]).addParameter(keysList[2],
                            valuesList[2]).addFragmentParameter(keysList[0], valuesList[0])
                            .addFragmentParameter(keysList[1], valuesList[1])
                            .addFragmentParameter(keysList[2], valuesList[2]).build();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(serviceURL.getAbsoluteURL(),
                "null://localhost:0/testPath1/testPath2/testPath3?key1%3Dvalue1%26key2%3Dvalue2%26key3%3Dvalue3#key1%3Dvalue1%26key2%3Dvalue2%26key3%3Dvalue3");

    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

}