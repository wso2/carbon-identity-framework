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
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
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
        IdentityTenantUtil.class, PrivilegedCarbonContext.class, IdentityUtil.class})
@PowerMockIgnore({"javax.net.*", "javax.security.*", "javax.crypto.*", "javax.xml.*", "org.xml.sax.*", "org.w3c.dom" +
        ".*", "org.apache.xerces.*", "org.mockito.*"})
public class DefaultServiceURLBuilderTest {

    @Mock
    private ServerConfiguration mockServerConfiguration;
    @Mock
    private ConfigurationContextService mockConfigurationContextService;
    @Mock
    private ConfigurationContext mockConfigurationContext;
    @Mock
    private AxisConfiguration mockAxisConfiguration;

    private final String HTTPS = "https";

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
        when(CarbonUtils.getManagementTransport()).thenReturn(HTTPS);
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
        when(CarbonUtils.getManagementTransport()).thenReturn(HTTPS);
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
        when(CarbonUtils.getManagementTransport()).thenReturn(HTTPS);
        try {
            parameterValue = ServiceURLBuilder.create().addParameter("key", "value").build().getParameter("key");
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(parameterValue, "value");
    }

    @Test
    public void testAddParameters() {

        when(CarbonUtils.getManagementTransport()).thenReturn(HTTPS);
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

        when(CarbonUtils.getManagementTransport()).thenReturn(HTTPS);
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
        when(CarbonUtils.getManagementTransport()).thenReturn(HTTPS);
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
        when(CarbonUtils.getManagementTransport()).thenReturn(HTTPS);
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
        when(CarbonUtils.getManagementTransport()).thenReturn(HTTPS);
        when(IdentityTenantUtil.isTenantQualifiedUrlsEnabled()).thenReturn(true);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()).thenReturn("carbon.super");

        try {
            absoluteUrl =
                    ServiceURLBuilder.create().addPath(testPath1, testPath2, testPath3).addParameter(keysList[0],
                            valuesList[0]).addParameter(keysList[1], valuesList[1]).addParameter(keysList[2],
                            valuesList[2]).addFragmentParameter(keysList[0], valuesList[0])
                            .addFragmentParameter(keysList[1], valuesList[1])
                            .addFragmentParameter(keysList[2], valuesList[2]).build().getAbsolutePublicURL();
        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored
        }

        assertEquals(absoluteUrl,
                "https://localhost:0/proxyContextPath/testPath1/testPath2/testPath3?key1=value1&key2=value2&key3" +
                        "=value3#key1=value1&key2=value2&key3=value3");
    }

    @DataProvider
    public Object[][] getAbsolutePublicURLData() {

        Map<String, String> parameters = new HashMap<>();
        Map<String, String> unencodedParameters = new HashMap<>();
        Map<String, String> fragmentParams = new HashMap<>();
        String proxyContext = "proxyContext";
        String fragment = "fragment";
        String samlsso = "samlsso";

        ArrayList<String> keys = new ArrayList<String>(Arrays.asList("key1", "key2", "key3", "key4"));
        for (String key : keys) {
            parameters.put(key, "v");
            fragmentParams.put(key, fragment);
        }
        unencodedParameters.put("key5", " v ");

        return new Object[][]{
                {"https", "www.wso2.com", 9443, "/" + proxyContext, "abc", false, null, "", fragmentParams,
                        "https://www.wso2.com:9443/proxyContext#key1=fragment&key2=fragment&key3=fragment&key4" +
                                "=fragment", ""},
                {"https", "www.wso2.com", 9443, "/" + proxyContext + "/", "", false, null, fragment, fragmentParams,
                        "https://www.wso2.com:9443/proxyContext/samlsso#fragment", "/" + samlsso},
                {"https", "www.wso2.com", 9443, proxyContext, "", true, null, "", fragmentParams,
                        "https://www.wso2.com:9443/proxyContext/samlsso#key1=fragment&key2=fragment&key3=fragment" +
                                "&key4=fragment", "/" + samlsso + "/"},
                {"https", "www.wso2.com", 9443, "", "abc", false, null, fragment, fragmentParams,
                        "https://www.wso2.com:9443/samlsso#fragment", samlsso},
                {"https", "www.wso2.com", 9443, null, "abc", true, parameters, fragment, fragmentParams,
                        "https://www.wso2.com:9443/t/abc/samlsso?key1=v&key2=v&key3=v&key4=v#fragment",
                        "/samlsso"},
                {"https", "www.wso2.com", 9443, null, "abc", false, parameters, "", null,
                        "https://www.wso2.com:9443/samlsso?key1=v&key2=v&key3=v&key4=v", "/" + samlsso + "/"},
                {"https", "www.wso2.com", 9443, proxyContext + "/", "abc", true, null, "fragment", fragmentParams,
                        "https://www.wso2.com:9443/proxyContext/t/abc/samlsso#fragment", "/" + samlsso},
                {"https", "www.wso2.com", 9443, "/" + proxyContext, "abc", true, null, "", null,
                        "https://www.wso2.com:9443/proxyContext/t/abc/samlsso", "/" + samlsso + "/"},
                {"https", "www.wso2.com", 9443, "", "", true, null, fragment, fragmentParams,
                        "https://www.wso2.com:9443/samlsso#fragment", samlsso + "/"},
                {"https", "www.wso2.com", 9443, "", "", true, parameters, "", fragmentParams,
                        "https://www.wso2.com:9443?key1=v&key2=v&key3=v&key4=v#key1=fragment&key2=fragment&key3" +
                                "=fragment&key4=fragment", null},
                {"https", "www.wso2.com", 9443, "", "", true, unencodedParameters, "", fragmentParams,
                        "https://www.wso2.com:9443?key5=+v+#key1=fragment&key2=fragment&key3" +
                                "=fragment&key4=fragment", null},
                {"https", "www.wso2.com", 9443, "/" + proxyContext, "", false, null, "", fragmentParams,
                        "https://www.wso2.com:9443/proxyContext#key1=fragment&key2=fragment&key3=fragment&key4" +
                                "=fragment", null}
        };
    }

    @DataProvider
    public Object[][] getAbsoluteInternalURLData() {

        Map<String, String> parameters = new HashMap<>();
        Map<String, String> fragmentParams = new HashMap<>();

        ArrayList<String> keys = new ArrayList<String>(Arrays.asList("key1", "key2", "key3", "key4"));
        for (String key : keys) {
            parameters.put(key, "v");
            fragmentParams.put(key, "fragment");
        }

        return new Object[][]{
                {"https", "internal.wso2.is", 9443, "abc", false, null, "", fragmentParams,
                        "https://internal.wso2.is:9443#key1=fragment&key2=fragment&key3=fragment&key4=fragment", ""},
                {"https", "internal.wso2.is", 9443, "", false, null, "fragment", fragmentParams,
                        "https://internal.wso2.is:9443/samlsso#fragment", "/samlsso"},
                {"https", "internal.wso2.is", 9443, "", true, null, "", fragmentParams,
                        "https://internal.wso2.is:9443/samlsso#key1=fragment&key2=fragment&key3=fragment&key4" +
                                "=fragment", "/samlsso/"},
                {"https", null, 9443, "abc", false, null, "fragment", fragmentParams,
                        "https://localhost:9443/samlsso#fragment", "samlsso"},
                {"https", "internal.wso2.is", 9443, "abc", true, parameters, "fragment", fragmentParams,
                        "https://internal.wso2.is:9443/t/abc/samlsso?key1=v&key2=v&key3=v&key4=v#fragment",
                        "/samlsso"},
                {"https", "internal.wso2.is", 9443, "abc", false, parameters, "", null,
                        "https://internal.wso2.is:9443/samlsso?key1=v&key2=v&key3=v&key4=v", "/samlsso/"},
                {"https", "internal.wso2.is", 9443, "abc", true, null, "fragment", fragmentParams,
                        "https://internal.wso2.is:9443/t/abc/samlsso#fragment", "/samlsso"},
                {"https", "internal.wso2.is", 9443, "abc", true, null, "", null,
                        "https://internal.wso2.is:9443/t/abc/samlsso", "/samlsso/"},
                {"https", "internal.wso2.is", 9443, "", true, null, "fragment", fragmentParams,
                        "https://internal.wso2.is:9443/samlsso#fragment", "samlsso/"},
                {"https", "internal.wso2.is", 9443, "", true, parameters, "", fragmentParams,
                        "https://internal.wso2.is:9443?key1=v&key2=v&key3=v&key4=v#key1=fragment&key2=fragment&key3" +
                                "=fragment&key4=fragment", null},
                {"https", null, 9443, "", false, null, "", fragmentParams,
                        "https://localhost:9443#key1=fragment&key2=fragment&key3=fragment&key4=fragment", null}
        };
    }

    @Test(dataProvider = "getAbsolutePublicURLData")
    public void testGetAbsolutePublicURL(String protocol, String hostName, int port, String proxyContextPath,
                                         String tenantNameFromContext, boolean enableTenantURLSupport,
                                         Map<String, String> parameters, String fragment, Map<String, String> fragmentParams,
                                         String expected, String urlPath) {

        when(CarbonUtils.getManagementTransport()).thenReturn(protocol);
        when(ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.HOST_NAME)).thenReturn(hostName);
        when(CarbonUtils.getTransportProxyPort(mockAxisConfiguration, protocol)).thenReturn(port);
        when(ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                .PROXY_CONTEXT_PATH)).thenReturn(proxyContextPath);
        when(IdentityTenantUtil.isTenantQualifiedUrlsEnabled()).thenReturn(enableTenantURLSupport);
        when(IdentityTenantUtil.getTenantDomainFromContext()).thenReturn(tenantNameFromContext);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()).thenReturn("carbon.super");

        String absoluteUrl = null;

        try {
            if (MapUtils.isNotEmpty(parameters) && MapUtils.isNotEmpty(fragmentParams)) {
                ServiceURLBuilder serviceURLBuilder = ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment);
                for (String paramKey : parameters.keySet()) {
                    serviceURLBuilder.addParameter(paramKey, parameters.get(paramKey));
                }
                for (String fragmentKey : fragmentParams.keySet()) {
                    serviceURLBuilder.addFragmentParameter(fragmentKey, fragmentParams.get(fragmentKey));
                }
                absoluteUrl = serviceURLBuilder.build().getAbsolutePublicURL();
            } else if (MapUtils.isNotEmpty(fragmentParams)){
                absoluteUrl =
                        ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment).addFragmentParameter("key1",
                                "fragment").addFragmentParameter("key2", "fragment").addFragmentParameter("key3",
                                "fragment").addFragmentParameter("key4", "fragment").build().getAbsolutePublicURL();
            } else if (MapUtils.isNotEmpty(parameters)){
                absoluteUrl =
                        ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment).addParameter("key1", "v")
                                .addParameter("key2", "v").addParameter("key3", "v").addParameter("key4", "v").build()
                                .getAbsolutePublicURL();
            } else {
                absoluteUrl =
                        ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment).build().getAbsolutePublicURL();
            }

        } catch (URLBuilderException e) {
            // Mock behaviour, hence ignored.
        }

        assertEquals(absoluteUrl, expected);
    }

    @Test(dataProvider = "getAbsoluteInternalURLData")
    public void testGetAbsoluteInternalURL(String protocol, String serverHostName, int port,
                                         String tenantNameFromContext, boolean enableTenantURLSupport,
                                         Map<String, String> parameters, String fragment, Map<String, String> fragmentParams,
                                         String expected, String urlPath) {

        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getProperty(IdentityCoreConstants.SERVER_HOST_NAME)).thenReturn(serverHostName);
        when(CarbonUtils.getManagementTransport()).thenReturn(protocol);
        when(CarbonUtils.getTransportPort(mockAxisConfiguration, protocol)).thenReturn(port);
        when(IdentityTenantUtil.isTenantQualifiedUrlsEnabled()).thenReturn(enableTenantURLSupport);
        when(IdentityTenantUtil.getTenantDomainFromContext()).thenReturn(tenantNameFromContext);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()).thenReturn("carbon.super");

        String absoluteUrl = null;

        try {
            if (MapUtils.isNotEmpty(parameters) && MapUtils.isNotEmpty(fragmentParams)) {
                absoluteUrl =
                        ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment).addFragmentParameter("key1",
                                "fragment").addFragmentParameter("key2", "fragment").addFragmentParameter("key3",
                                "fragment").addFragmentParameter("key4", "fragment").addParameter("key1", "v")
                                .addParameter("key2", "v").addParameter("key3", "v").addParameter("key4", "v").build()
                                .getAbsoluteInternalURL();
            } else if (MapUtils.isNotEmpty(fragmentParams)){
                absoluteUrl =
                        ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment).addFragmentParameter("key1",
                                "fragment").addFragmentParameter("key2", "fragment").addFragmentParameter("key3",
                                "fragment").addFragmentParameter("key4", "fragment").build().getAbsoluteInternalURL();
            } else if (MapUtils.isNotEmpty(parameters)){
                absoluteUrl =
                        ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment).addParameter("key1", "v")
                                .addParameter("key2", "v").addParameter("key3", "v").addParameter("key4", "v").build()
                                .getAbsoluteInternalURL();
            } else {
                absoluteUrl =
                        ServiceURLBuilder.create().addPath(urlPath).setFragment(fragment).build().getAbsoluteInternalURL();
            }

        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }

        assertEquals(absoluteUrl, expected);
    }

    @DataProvider
    public Object[][] getRelativePublicURLData() {

        return new Object[][]{
                {"https", "www.wso2.com", 9443, "/proxyContext", "abc", false, "/proxyContext/samlsso", "/samlsso"},
                {"https", "www.wso2.com", 9443, "/proxyContext/", "", false, "/proxyContext/samlsso", "samlsso"},
                {"https", "www.wso2.com", 9443, "proxyContext", "", true, "/proxyContext/samlsso", "/samlsso/"},
                {"https", "www.wso2.com", 9443, "", "abc", true, "/t/abc/samlsso", "samlsso"},
                {"https", "www.wso2.com", 9443, "/proxyContext", "abc", true, "/proxyContext/t/abc/samlsso", "samlsso"},
                {"https", "www.wso2.com", 9443, null, "carbon.super", true, "/samlsso", "/samlsso"}
        };
    }

    @Test(dataProvider = "getRelativePublicURLData")
    public void testGetRelativePublicURL(String protocol, String hostName, int port, String proxyContextPath,
                                         String tenantNameFromContext, boolean enableTenantURLSupport,
                                         String expected, String urlPath) {

        when(CarbonUtils.getManagementTransport()).thenReturn(protocol);
        when(ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.HOST_NAME)).thenReturn(hostName);
        when(CarbonUtils.getTransportProxyPort(mockAxisConfiguration, protocol)).thenReturn(port);
        when(ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                .PROXY_CONTEXT_PATH)).thenReturn(proxyContextPath);
        when(IdentityTenantUtil.isTenantQualifiedUrlsEnabled()).thenReturn(enableTenantURLSupport);
        when(IdentityTenantUtil.getTenantDomainFromContext()).thenReturn(tenantNameFromContext);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()).thenReturn("carbon.super");

        String relativePublicUrl = null;
        try {
            relativePublicUrl = ServiceURLBuilder.create().addPath(urlPath).build().getRelativePublicURL();
        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }
        assertEquals(relativePublicUrl, expected);
    }

    @DataProvider
    public Object[][] getRelativeInternalURLData() {

        return new Object[][]{
                {"https", "www.wso2.com", 9443, "/proxyContext", "abc", false, "/samlsso", "/samlsso"},
                {"https", "www.wso2.com", 9443, "/proxyContext/", "", false, "/samlsso", "samlsso"},
                {"https", "www.wso2.com", 9443, "proxyContext", "", true, "/samlsso", "/samlsso/"},
                {"https", "www.wso2.com", 9443, "", "abc", true, "/t/abc/samlsso", "samlsso"},
                {"https", "www.wso2.com", 9443, null, "carbon.super", true, "/samlsso", "/samlsso"}
        };
    }

    @Test(dataProvider = "getRelativeInternalURLData")
    public void testGetRelativeInternalURL(String protocol, String hostName, int port, String proxyContextPath,
                                           String tenantNameFromContext, boolean enableTenantURLSupport,
                                           String expected, String urlPath) {

        when(CarbonUtils.getManagementTransport()).thenReturn(protocol);
        when(ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.HOST_NAME)).thenReturn(hostName);
        when(CarbonUtils.getTransportProxyPort(mockAxisConfiguration, protocol)).thenReturn(port);
        when(ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                .PROXY_CONTEXT_PATH)).thenReturn(proxyContextPath);
        when(IdentityTenantUtil.isTenantQualifiedUrlsEnabled()).thenReturn(enableTenantURLSupport);
        when(IdentityTenantUtil.getTenantDomainFromContext()).thenReturn(tenantNameFromContext);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()).thenReturn("carbon.super");

        String relativeInternalUrl = null;
        try {
            relativeInternalUrl = ServiceURLBuilder.create().addPath(urlPath).build().getRelativeInternalURL();
        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }
        assertEquals(relativeInternalUrl, expected);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

}