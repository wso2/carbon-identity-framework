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
import org.apache.commons.collections.MapUtils;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.URLResolverService;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        String urlPath = null;
        try {
            urlPath = ServiceURLBuilder.create().addPath(testPath).build().getPath();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(urlPath, testPath);
    }

    @Test
    public void testAddPaths() {

        String testPath1 = "/testPath1";
        String testPath2 = "testPath2/";
        String testPath3 = "/testPath3/";
        String urlPath = null;
        try {
            urlPath = ServiceURLBuilder.create().addPath(testPath1, testPath2, testPath3).build().getPath();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(urlPath, "/testPath1/testPath2/testPath3");
    }

    @Test
    public void testAddParameter() {

        String parameterValue = null;
        try {
            parameterValue = ServiceURLBuilder.create().addParameter("key", "value").build().getParameter("key");
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(parameterValue, "value");
    }

    @Test
    public void testAddParameters() {

        Map<String, String> parameters = new HashMap<>();

        Map<String, String> expected = new HashMap<String, String>() {
            {
                put("key1", "value1");
                put("key2", "value2");
            }
        };
        try {
            parameters =
                    ServiceURLBuilder.create().addParameter("key1", "value1").addParameter("key2", "value2").build()
                            .getParameters();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(parameters, expected);
    }

    @Test
    public void testSetFragment() {

        when(CarbonUtils.getManagementTransport()).thenReturn("https");
        String fragment = null;
        try {
            fragment = ServiceURLBuilder.create().setFragment("fragment").build().getFragment();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(fragment, "fragment");
    }

    @Test
    public void testAddFragmentParameter() {

        String fragment = null;

        try {
            fragment =
                    ServiceURLBuilder.create().addFragmentParameter("key1", "value1").addFragmentParameter("key2",
                            "value2").build().getFragment();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(fragment, "key1=value1&key2=value2");
    }

    @Test
    public void testAddFragmentParameters() {

        String fragment = null;

        try {
            fragment =
                    ServiceURLBuilder.create().addFragmentParameter("key1", "value1").addFragmentParameter("key2",
                            "value2").build().getFragment();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(fragment, "key1=value1&key2=value2");
    }

    @Test
    public void testBuild() {

        String absoluteUrl = null;
        String testPath1 = "/testPath1";
        String testPath2 = "testPath2/";
        String testPath3 = "/testPath3/";
        String[] keysList = {"key1", "key2", "key3"};
        String[] valuesList = {"value1", "value2", "value3"};
        when(ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                .PROXY_CONTEXT_PATH)).thenReturn("proxyContextPath");

        try {
            absoluteUrl =
                    ServiceURLBuilder.create().addPath(testPath1, testPath2, testPath3).addParameter(keysList[0],
                            valuesList[0]).addParameter(keysList[1], valuesList[1]).addParameter(keysList[2],
                            valuesList[2]).addFragmentParameter(keysList[0], valuesList[0])
                            .addFragmentParameter(keysList[1], valuesList[1])
                            .addFragmentParameter(keysList[2], valuesList[2]).build().getAbsoluteURL();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(absoluteUrl,
                "null://localhost:0/proxyContextPath/testPath1/testPath2/testPath3?key1%3Dvalue1%26key2%3Dvalue2" +
                        "%26key3%3Dvalue3#key1%3Dvalue1%26key2%3Dvalue2%26key3%3Dvalue3");
    }

    @DataProvider
    public Object[][] getAbsoluteURLData() {

        int port = 9443;
        Map<String, String> parameters = new HashMap<>();
        String fragment = "fragment";
        Map<String, String> fragmentParams = new HashMap<>();

        ArrayList<String> keys = new ArrayList<String>(Arrays.asList("key1", "key2", "key3", "key4"));
        for (String key : keys) {
            parameters.put(key, "v");
            fragmentParams.put(key, "fragment");
        }

        return new Object[][]{
                {"https", "www.wso2.com", 9443, "/proxyContext", null, "", fragmentParams,
                        "https://www.wso2.com:9443/proxyContext#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment" +
                                "%26key4%3Dfragment", ""},
                {"https", "www.wso2.com", 9443, "/proxyContext/", null, "fragment", fragmentParams,
                        "https://www.wso2.com:9443/proxyContext/samlsso#fragment", "/samlsso"},
                {"https", "www.wso2.com", 9443, "proxyContext", null, "", fragmentParams,
                        "https://www.wso2.com:9443/proxyContext/samlsso#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment%26key4%3Dfragment",
                        "/samlsso/"},
                {"https", "www.wso2.com", 9443, "", null, "fragment", fragmentParams,
                        "https://www.wso2.com:9443/samlsso#fragment", "samlsso"},
                {"https", "www.wso2.com", 9443, null, parameters, "fragment", fragmentParams,
                        "https://www.wso2.com:9443/samlsso?key1%3Dv%26key2%3Dv%26key3%3Dv%26key4%3Dv#fragment",
                        "/samlsso"},
                {"https", "www.wso2.com", 9443, null, parameters, "", null,
                        "https://www.wso2.com:9443/samlsso?key1%3Dv%26key2%3Dv%26key3%3Dv%26key4%3Dv", "/samlsso/"},
                {"https", "www.wso2.com", 9443, "proxyContext/", null, "fragment", fragmentParams,
                        "https://www.wso2.com:9443/proxyContext/samlsso#fragment", "/samlsso"},
                {"https", "www.wso2.com", 9443, "/proxyContext", null, "", null,
                        "https://www.wso2.com:9443/proxyContext/samlsso", "/samlsso/"},
                {"https", "www.wso2.com", 9443, "", null, "fragment", fragmentParams,
                        "https://www.wso2.com:9443/samlsso#fragment", "samlsso/"},
                {"https", "www.wso2.com", 9443, "", parameters, "", fragmentParams,
                        "https://www.wso2.com:9443?key1%3Dv%26key2%3Dv%26key3%3Dv%26key4%3Dv#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment%26key4%3Dfragment",
                        null},
                {"https", "www.wso2.com", 9443, "/proxyContext", null, "", fragmentParams,
                        "https://www.wso2.com:9443/proxyContext#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment" +
                                "%26key4%3Dfragment", null}
        };
    }

    @Test(dataProvider = "getAbsoluteURLData")
    public void testGetAbsoluteURL(String protocol, String hostName, int port,
                                   String proxyContextPath, Map<String, String> parameters,
                                   String fragment, Map<String, String> fragmentParams, String expected,
                                   String urlPath) {

        when(CarbonUtils.getManagementTransport()).thenReturn(protocol);
        when(ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.HOST_NAME)).thenReturn(hostName);
        when(CarbonUtils.getTransportProxyPort(mockAxisConfiguration, protocol)).thenReturn(port);
        when(ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                .PROXY_CONTEXT_PATH)).thenReturn(proxyContextPath);

        String absoluteUrl = null;

        try {
            if (MapUtils.isNotEmpty(parameters) && MapUtils.isNotEmpty(fragmentParams)) {
                absoluteUrl =
                        ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment).addFragmentParameter("key1",
                                "fragment").addFragmentParameter("key2", "fragment").addFragmentParameter("key3",
                                "fragment").addFragmentParameter("key4", "fragment").addParameter("key1", "v")
                                .addParameter("key2", "v").addParameter("key3", "v").addParameter("key4", "v").build()
                                .getAbsoluteURL();
            } else if (MapUtils.isNotEmpty(fragmentParams)){
                absoluteUrl =
                        ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment).addFragmentParameter("key1",
                                "fragment").addFragmentParameter("key2", "fragment").addFragmentParameter("key3",
                                "fragment").addFragmentParameter("key4", "fragment").build().getAbsoluteURL();
            } else if (MapUtils.isNotEmpty(parameters)){
                absoluteUrl =
                        ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment).addParameter("key1", "v")
                                .addParameter("key2", "v").addParameter("key3", "v").addParameter("key4", "v").build()
                                .getAbsoluteURL();
            } else {
                absoluteUrl =
                        ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment).build().getAbsoluteURL();
            }

        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }

        assertEquals(absoluteUrl, expected);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

}