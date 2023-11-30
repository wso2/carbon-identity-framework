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
import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceComponent;
import org.wso2.carbon.identity.core.model.IdentityCacheConfig;
import org.wso2.carbon.identity.core.model.IdentityCacheConfigKey;
import org.wso2.carbon.identity.core.model.IdentityCookieConfig;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfigKey;
import org.wso2.carbon.identity.core.model.ReverseProxyConfig;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.nio.file.Paths;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;


@PrepareForTest({IdentityConfigParser.class, ServerConfiguration.class, CarbonUtils.class,
        IdentityCoreServiceComponent.class, NetworkUtils.class, IdentityTenantUtil.class})
@PowerMockIgnore({"javax.net.*", "javax.security.*", "javax.crypto.*", "javax.xml.*", "org.xml.sax.*", "org.w3c.dom" +
        ".*", "org.apache.xerces.*","org.mockito.*"})
public class IdentityUtilTest {

    @Mock
    private IdentityConfigParser mockConfigParser;
    @Mock
    private ServerConfiguration mockServerConfiguration;
    @Mock
    private ConfigurationContextService mockConfigurationContextService;
    @Mock
    private ConfigurationContext mockConfigurationContext;
    @Mock
    private AxisConfiguration mockAxisConfiguration;
    @Mock
    private RealmService mockRealmService;
    @Mock
    private UserRealm mockUserRealm;
    @Mock
    private TenantManager mockTenantManager;
    @Mock
    private UserStoreManager mockUserStoreManager;
    @Mock
    private RealmConfiguration mockRealmConfiguration;
    @Mock
    private HttpServletRequest mockRequest;

    @BeforeMethod
    public void setUp() throws Exception {
        mockStatic(CarbonUtils.class);
        mockStatic(ServerConfiguration.class);
        mockStatic(NetworkUtils.class);
        mockStatic(IdentityCoreServiceComponent.class);
        mockStatic(IdentityConfigParser.class);
        mockStatic(CarbonUtils.class);
        mockStatic(IdentityTenantUtil.class);

        when(ServerConfiguration.getInstance()).thenReturn(mockServerConfiguration);
        when(IdentityCoreServiceComponent.getConfigurationContextService()).thenReturn(mockConfigurationContextService);
        when(mockConfigurationContextService.getServerConfigContext()).thenReturn(mockConfigurationContext);
        when(mockConfigurationContext.getAxisConfiguration()).thenReturn(mockAxisConfiguration);
        when(IdentityTenantUtil.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getTenantManager()).thenReturn(mockTenantManager);
        when(CarbonUtils.getCarbonHome()).thenReturn("carbon.home");
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockUserStoreManager.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
        when(mockRealmService.getBootstrapRealmConfiguration()).thenReturn(mockRealmConfiguration);
        when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);
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

        IdentityLogTokenParser.getInstance().getLogTokenMap().remove("authz");
        IdentityLogTokenParser.getInstance().getLogTokenMap().remove("access");
    }

    @DataProvider
    public Object[][] getIdentityErrorMsgTestData() {
        return new Object[][]{
                {"", 0, 0},
                {"\t", 1, 2},
                {null, 0, 0},
                {"TestMsg", 2, 2},
                {"Exceeding", 5, 2},
        };
    }

    @Test(dataProvider = "getIdentityErrorMsgTestData")
    public void testIdentityErrorMsg(String code, int failedAttempts, int maxAttempts) throws Exception {

        IdentityErrorMsgContext msgContext = new IdentityErrorMsgContext(code, failedAttempts, maxAttempts);
        assertNull(IdentityUtil.getIdentityErrorMsg(), "Error msg should be null initially");
        IdentityUtil.setIdentityErrorMsg(msgContext);
        assertEquals(IdentityUtil.getIdentityErrorMsg().getErrorCode(), code, String.format("Error code mismatch for " +
                "input: code = %s, failedAttempts = %d, maxAttempts = %d.", code, failedAttempts, maxAttempts));
        assertEquals(IdentityUtil.getIdentityErrorMsg().getFailedLoginAttempts(), failedAttempts, String.format
                ("Login attempt mismatch for input: code = %s, failedAttempts = %d, maxAttempts = %d.", code,
                        failedAttempts, maxAttempts));
        assertEquals(IdentityUtil.getIdentityErrorMsg().getMaximumLoginAttempts(), maxAttempts, String.format
                ("Max attempt mismatch for input: code = %s, failedAttempts = %d, maxAttempts = %d.", code,
                        failedAttempts, maxAttempts));
        IdentityUtil.clearIdentityErrorMsg();
        assertNull(IdentityUtil.getIdentityErrorMsg(), "Error msg should be null after being cleared");
    }

    @DataProvider
    public Object[][] getPropertyTestData() {
        return new Object[][]{
                {"testInt", 5, "5"},
                {"testString", "dummyValue", "dummyValue"},
                {"testEmpty", "", ""},
                {"testNull", null, null},
                {"testList", Arrays.asList("one", "two"), "one"},
        };
    }

    @Test(dataProvider = "getPropertyTestData")
    public void testGetProperty(String key, Object value, String expected) throws Exception {
        Map<String, Object> mockConfig = new HashMap<>();
        mockConfig.put(key, value);

        Whitebox.setInternalState(IdentityUtil.class, "configuration", mockConfig);
        assertEquals(IdentityUtil.getProperty(key), expected, String.format("Property value mismatch for input: key " +
                "= %s, value = %s", key, String.valueOf(value)));
    }


    @Test
    public void testReadEventListenerProperty() throws Exception {

        IdentityEventListenerConfigKey key = new IdentityEventListenerConfigKey("ListenerType", "ListenerName");
        IdentityEventListenerConfig config = new IdentityEventListenerConfig("true", 0, key, new Properties());
        Map<IdentityEventListenerConfigKey, IdentityEventListenerConfig> mockedMap = new HashMap<>();
        mockedMap.put(key, config);
        Whitebox.setInternalState(IdentityUtil.class, "eventListenerConfiguration", mockedMap);

        IdentityEventListenerConfig configResponse = IdentityUtil.readEventListenerProperty("ListenerType", "ListenerName");
        assertEquals(configResponse.getEnable(), "true", "Listener should be enabled");
        assertEquals(configResponse.getOrder(), 0, "Listener should have order : 0");

        IdentityEventListenerConfig nullResponse = IdentityUtil.readEventListenerProperty("NonExistingType",
                "NonExistingListenerName");
        assertNull(nullResponse, "Response should be null for invalid listener.");
    }

    @DataProvider
    public Object[][] getIdentityCacheConfigTestData() {
        Map<IdentityCacheConfigKey, IdentityCacheConfig> mockedCacheConfig = new HashMap<>();

        IdentityCacheConfigKey cacheConfigKey1 = new IdentityCacheConfigKey("manager1", "key1");
        IdentityCacheConfig cacheConfig1 = new IdentityCacheConfig(cacheConfigKey1);
        mockedCacheConfig.put(cacheConfigKey1, cacheConfig1);
        IdentityCacheConfigKey cacheConfigKey2 = new IdentityCacheConfigKey("manager2", "");
        IdentityCacheConfig cacheConfig2 = new IdentityCacheConfig(cacheConfigKey1);
        mockedCacheConfig.put(cacheConfigKey2, cacheConfig2);
        Whitebox.setInternalState(IdentityUtil.class, "identityCacheConfigurationHolder", mockedCacheConfig);
        return new Object[][]{
                {"manager1", "key1", cacheConfig1},
                {"manager1", "", null},
                {"manager2", "", cacheConfig2},
                {"manager1", "$__local__$.key1", cacheConfig1},
        };
    }

    @DataProvider
    public Object[][] getReverseProxyConfigData() {

        return new Object[][]{
                {"/oauth2/authorize", "/authorize", "/oidc/logout", "/logout", "/oauth2/authorize", "/authorize"},
                {"/oauth2/authorize", null, "/oidc/logout", "/logout", "/oauth2/authorize", "/oauth2/authorize"},
                {null, "/authorize", "/oidc/logout", "/logout", "/oauth2/authorize", "/oauth2/authorize"},
                {null, null, "/oidc/logout", "/logout", "/oauth2/authorize", "/oauth2/authorize"},
                {null, null, null, null, "/oauth2/authorize", "/oauth2/authorize"},
                {"", "", "/oidc/logout", "/logout", "/oauth2/authorize", "/oauth2/authorize"},
                {"/oauth2/authorize", "", "/oidc/logout", "/logout", "/oauth2/authorize", "/oauth2/authorize"},
                {"", "/authorize", "/oidc/logout", "/logout", "/oauth2/authorize", "/oauth2/authorize"},
                {"/oauth2/authorize", "/authorize", "/oidc/logout", "/logout", "", ""},
                {"/oauth2/authorize", "/authorize", "/oidc/logout", "/logout", null, null},
                {"/oauth2/authorize", "/authorize", "/oidc/logout", "/logout", "/oidc/logout", "/logout"},
                {null, null, "/oidc/logout", "/logout", "/oidc/logout", "/logout"}
        };
    }

    @Test(dataProvider = "getIdentityCacheConfigTestData")
    public void testGetIdentityCacheConfig(String cacheManagerName, String cacheName, Object expected)
            throws Exception {
        assertEquals(IdentityUtil.getIdentityCacheConfig(cacheManagerName, cacheName), expected, String.format(
                "Invalid cache config value for: cacheManagerName = %s, cacheName = %s", cacheManagerName, cacheName));
    }

    @Test
    public void testGetIdentityCookiesConfigurationHolder() throws Exception {

        Map<String, IdentityCookieConfig> mockIdentityCookiesConfigurationHolder = new HashMap<>();
        mockIdentityCookiesConfigurationHolder.put("cookie", new IdentityCookieConfig("cookieName"));
        Whitebox.setInternalState(IdentityUtil.class, "identityCookiesConfigurationHolder",
                mockIdentityCookiesConfigurationHolder);
        assertEquals(IdentityUtil.getIdentityCookiesConfigurationHolder(), mockIdentityCookiesConfigurationHolder,
                "Returned cookie holder doesn't match the given.");
    }

    @Test(dataProvider = "getReverseProxyConfigData")
    public void testGetProxyContext(String defaultContext1, String proxyContext1,
                                    String defaultContext2, String proxyContext2,
                                    String expectedDefaultContext, String expectedProxyContext) {

        Map<String, ReverseProxyConfig> mockReverseProxyConfigurationHolder = new HashMap<>();
        mockReverseProxyConfigurationHolder.put(defaultContext1,
                new ReverseProxyConfig(defaultContext1, proxyContext1));
        mockReverseProxyConfigurationHolder.put(defaultContext2,
                new ReverseProxyConfig(defaultContext2, proxyContext2));
        Whitebox.setInternalState(IdentityUtil.class, "reverseProxyConfigurationHolder",
                mockReverseProxyConfigurationHolder);
        assertEquals(IdentityUtil.getProxyContext(expectedDefaultContext), expectedProxyContext,
                "Returned proxy context is incorrect.");
    }

    @Test
    public void testGetIdentityCookieConfig() throws Exception {
        Map<String, IdentityCookieConfig> mockIdentityCookiesConfigurationHolder = new HashMap<>();
        IdentityCookieConfig cookieConfig = new IdentityCookieConfig("cookieName");
        mockIdentityCookiesConfigurationHolder.put("cookie", cookieConfig);
        Whitebox.setInternalState(IdentityUtil.class, "identityCookiesConfigurationHolder",
                mockIdentityCookiesConfigurationHolder);
        assertEquals(IdentityUtil.getIdentityCookieConfig("cookie"), cookieConfig, "Invalid cookie config value " +
                "for: cookie");
        assertNull(IdentityUtil.getIdentityCookieConfig("nonExisting"), "Non existing cookie key should be returned " +
                "with null");

    }

    @Test
    public void testPopulateProperties() throws Exception {

        Map<String, Object> mockConfig = new HashMap<>();
        mockConfig.put("dummy", new Object());
        Map<IdentityEventListenerConfigKey, IdentityEventListenerConfig> mockedEventListenerConfig = new HashMap<>();
        IdentityEventListenerConfigKey configKey = new IdentityEventListenerConfigKey("type", "name");
        mockedEventListenerConfig.put(configKey, new
                IdentityEventListenerConfig("false", 0, configKey, null));
        Map<IdentityCacheConfigKey, IdentityCacheConfig> mockedCacheConfig = new HashMap<>();
        IdentityCacheConfigKey cacheConfigKey = new IdentityCacheConfigKey("manager", "key");
        mockedCacheConfig.put(cacheConfigKey, new IdentityCacheConfig(cacheConfigKey));
        Map<String, IdentityCookieConfig> mockedCookieConfig = new HashMap<>();
        mockedCookieConfig.put("cookie", new IdentityCookieConfig("cookieName"));

        when(mockConfigParser.getConfiguration()).thenReturn(mockConfig);
        when(IdentityConfigParser.getEventListenerConfiguration()).thenReturn(mockedEventListenerConfig);
        when(IdentityConfigParser.getIdentityCacheConfigurationHolder()).thenReturn(mockedCacheConfig);
        when(IdentityConfigParser.getIdentityCookieConfigurationHolder()).thenReturn(mockedCookieConfig);
        when(IdentityConfigParser.getInstance()).thenReturn(mockConfigParser);
        IdentityUtil.populateProperties();
        assertEquals(Whitebox.getField(IdentityUtil.class, "configuration").get(IdentityUtil.class), mockConfig,
                "Configuration is not set properly during config population");
        assertEquals(Whitebox.getField(IdentityUtil.class, "eventListenerConfiguration").get(IdentityUtil.class),
                mockedEventListenerConfig, "eventListenerConfiguration is not set properly during config population");
        assertEquals(IdentityUtil.getIdentityCookiesConfigurationHolder(), mockedCookieConfig,
                "cookieConfiguration is not set properly during config population");
        assertEquals(Whitebox.getField(IdentityUtil.class, "identityCacheConfigurationHolder").get(IdentityUtil
                .class), mockedCacheConfig, "identityCacheConfigurationHolder is not set properly during config population");
    }

    @DataProvider
    public Object[][] getPPIDDisplayValueTestData() {
        return new Object[][]{
                {"aabfcdfe", "26G-NDT6-656"},
                {"", "UT3-EYBB-BDJ"},
        };
    }

    @Test(dataProvider = "getPPIDDisplayValueTestData")
    public void testGetPPIDDisplayValue(String provided, String expected) throws Exception {
        assertEquals(IdentityUtil.getPPIDDisplayValue(provided), expected, String.format("Invalid PPID value for %s",
                provided));
    }

    @Test
    public void testNodeToString() throws Exception {
        String xmlText = "<TestNode attr=\"attrValue\">Test value</TestNode>";

        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = fac.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element testNode = document.createElement("TestNode");
        testNode.setTextContent("Test value");
        testNode.setAttribute("attr", "attrValue");
        assertEquals(IdentityUtil.nodeToString(testNode), xmlText, "XML text doesn't match the expected");
    }

    @DataProvider
    public Object[][] getHmacTestData() {
        return new Object[][]{
                {"Secret", "text2hmac", "YXtiz29YSC7+tSC/MoSLUp/Bpaw="},
                {"Secret", "", "C+IW8zY183KCv2ykZKQV1rLVuAY="},
                {" ", "", "SRJSdgtKDFBrWRewM1+u6JJU3PI="},
        };
    }

    @Test(dataProvider = "getHmacTestData")
    public void testGetHMAC(String secretKey, String baseString, String expected) throws Exception {
        assertEquals(IdentityUtil.getHMAC(secretKey, baseString), expected, String.format("Invalid HMAC for " +
                "secretKey: %s, baseString: %s.", secretKey, baseString));
    }

    @DataProvider
    public Object[][] getHmacNegativeTestData() {
        return new Object[][]{
                {"", "text2hmac"},
                {"Secret", null},
                {null, null},
        };
    }

    @Test(dataProvider = "getHmacNegativeTestData", expectedExceptions = SignatureException.class)
    public void testNegativeGetHMAC(String secretKey, String baseString) throws Exception {
        IdentityUtil.getHMAC(secretKey, baseString);
    }

    @Test
    public void testGenerateUUID() throws Exception {
        String uuid = IdentityUtil.generateUUID();
        assertNotNull(uuid, "Generated UUID should be not null");
        assertEquals(uuid.length(), 64, "Generated UUID should have length 64");
    }

    @Test
    public void testGetRandomNumber() throws Exception {
        String s = IdentityUtil.getRandomNumber();
        assertNotNull(s, "Random string should not be null");
        assertFalse(s.contains("/"), "Random string should contain '/'");
        assertFalse(s.contains("="), "Random string should contain '='");
        assertFalse(s.contains("+"), "Random string should contain '+'");
    }

    @Test
    public void testGetRandomInteger() throws Exception {
        int randomInteger = IdentityUtil.getRandomInteger();
        assertTrue(randomInteger > 0, "Generated random string should be positive");
    }

    @Test
    public void testGetIdentityConfigDirPath() throws Exception {

        String mockedCarbonConfigDirPath = Paths.get("home", "mockedPath").toString();
        when(CarbonUtils.getCarbonConfigDirPath()).thenReturn(mockedCarbonConfigDirPath);
        String mockedIdentityConfigDirPath = Paths.get(mockedCarbonConfigDirPath, "identity").toString();
        assertEquals(IdentityUtil.getIdentityConfigDirPath(), mockedIdentityConfigDirPath, "Config dir path doesn't " +
                                                                                         "match the expected.");
    }

    @DataProvider
    public Object[][] getServerURLData() {
        return new Object[][]{
                {"wso2.org", 9443, "", "", "", true, true, "https://wso2.org:9443"},
                {"wso2.org", 443, "", "", "", true, true, "https://wso2.org"},
                {"wso2.org", 0, "", "", "", true, true, "https://wso2.org:9443"},
                {"wso2.org", 0, "", "", "/", true, true, "https://wso2.org:9443"},
                {"wso2.org", 0, "", "", "/", true, true, "https://wso2.org:9443"},
                {"wso2.org/", 443, "", "", "/", true, true, "https://wso2.org"},
                {"wso2.org/", 9443, "", "", "/", true, true, "https://wso2.org:9443"},
                {"wso2.org", 9443, "/proxy", "/ctxroot", "/", true, true, "https://wso2.org:9443/proxy/ctxroot"},
                {"wso2.org", 443, "proxy", "ctxroot", "/", true, true, "https://wso2.org/proxy/ctxroot"},
                {"wso2.org", 443, "proxy", "ctxroot", "carbon", true, true, "https://wso2.org/proxy/ctxroot/carbon"},
                {"wso2.org", 443, "proxy", "ctxroot/", "carbon", true, true, "https://wso2.org/proxy/ctxroot/carbon"},
                {"wso2.org", 443, "proxy", "ctxroot/", "/carbon", true, true, "https://wso2.org/proxy/ctxroot/carbon"},
                {"wso2.org", 443, "proxy", "ctxroot/", "carbon/", true, true, "https://wso2.org/proxy/ctxroot/carbon"},
                {"wso2.org", 443, "proxy", "ctxroot", "carbon", false, false, "https://wso2.org/carbon"},
                {null, 443, "proxy", "ctxroot", "carbon", true, true, "https://localhost/proxy/ctxroot/carbon"},
        };
    }

    @Test(dataProvider = "getServerURLData")
    public void testGetServerURL(String host, int port, String proxyCtx, String ctxRoot, String endpoint, boolean
            addProxyContextPath, boolean addWebContextRoot, String expected) throws Exception {

        when(CarbonUtils.getTransportPort(any(AxisConfiguration.class), anyString())).thenReturn(9443);
        when(CarbonUtils.getTransportProxyPort(any(AxisConfiguration.class), anyString())).thenReturn(port);
        when(CarbonUtils.getManagementTransport()).thenReturn("https");
        when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.HOST_NAME)).thenReturn(host);
        when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.WEB_CONTEXT_ROOT)).thenReturn(ctxRoot);
        when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.PROXY_CONTEXT_PATH)).thenReturn(proxyCtx);

        assertEquals(IdentityUtil.getServerURL(endpoint, addProxyContextPath, addWebContextRoot), expected, String
                .format("Generated server url doesn't match the expected for input: host = %s, " +
                                "port = %d, proxyCtx = %s, ctxRoot = %s, endpoint = %s, addProxyContextPath = %b, " +
                                "addWebContextRoot = %b", host, port, proxyCtx, ctxRoot, endpoint, addProxyContextPath,
                        addWebContextRoot));
    }

    @Test
    public void testGetServicePath() throws Exception {
        when(mockConfigurationContext.getServicePath()).thenReturn("servicePath");
        assertEquals(IdentityUtil.getServicePath(), "servicePath", "Returned service patch doesn't match the " +
                "expected");
    }

    @DataProvider
    public Object[][] getUserstoreUsernameCaseSensitiveData() {
        try {
            when(mockTenantManager.getTenantId(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)).thenReturn
                    (MultitenantConstants.SUPER_TENANT_ID);
            when(mockTenantManager.getTenantId("wso2.com")).thenReturn(1);
            when(mockTenantManager.getTenantId("none.com")).thenReturn(MultitenantConstants.INVALID_TENANT_ID);
            when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealm);
            when(mockUserStoreManager.getSecondaryUserStoreManager(anyString())).thenReturn(mockUserStoreManager);
        } catch (UserStoreException e) {
            // Ignored, since this is a mock behaviour
        }
        return new Object[][]{
                {"admin", "false", true},
                {"admin@carbon.super", "false", true},
                {"admin@wso2.com", "false", true},
                {"foo/admin@wso2.com", null, true},
                {"admin@none.com", "true", true},
                {"sec/admin@wso2.com", "true", false},
        };
    }

    @Test(dataProvider = "getUserstoreUsernameCaseSensitiveData")
    public void testIsUserStoreInUsernameCaseSensitive(String username, String caseInsensitivePropertyValue, boolean
            expected) throws Exception {
        when(mockRealmConfiguration.getUserStoreProperty(IdentityCoreConstants.CASE_INSENSITIVE_USERNAME)).thenReturn
                (caseInsensitivePropertyValue);
        assertEquals(IdentityUtil.isUserStoreInUsernameCaseSensitive(username), expected, String.format("Expected " +
                        "value mismatch for input: username = %s, caseInsensitivePropertyValue = %s.", username,
                caseInsensitivePropertyValue));
    }

    @Test(dependsOnMethods = "testIsUserStoreInUsernameCaseSensitive")
    public void testIsUserStoreInUsernameCaseSensitiveNegative() throws Exception {
        when(mockTenantManager.getTenantId(anyString())).thenThrow(UserStoreException.class);
        assertTrue(IdentityUtil.isUserStoreInUsernameCaseSensitive("foo"), "Non existing userstore should return case" +
                " sensitive userstore true");
    }

    @Test(dependsOnMethods = "testIsUserStoreInUsernameCaseSensitive")
    public void testIsUserStoreCaseSensitive() throws Exception {
        when(mockRealmService.getTenantUserRealm(anyInt())).thenThrow(UserStoreException.class);
        assertTrue(IdentityUtil.isUserStoreCaseSensitive("sec", 1), "Non existing tenant should return case " +
                "sensitive userstore as true");

    }

    @Test(dependsOnMethods = "testIsUserStoreInUsernameCaseSensitive")
    public void testIsUserStoreCaseSensitiveWhenNull() throws Exception {
        assertTrue(IdentityUtil.isUserStoreCaseSensitive(null), "Null userstore should return true for case " +
                "sensitivity");
    }

    @DataProvider
    public Object[][] getIsNotBlankData() {
        return new Object[][]{
                {"", false},
                {"null", false},
                {" null ", false},
                {" \n", false},
                {"nonBlank", true},
        };
    }

    @Test(dataProvider = "getIsNotBlankData")
    public void testIsNotBlank(String input, boolean expected) throws Exception {
        assertEquals(IdentityUtil.isNotBlank(input), expected, "Expected value mismatches returned for input: " + input);
    }

    @Test(dataProvider = "getIsNotBlankData")
    public void testIsBlank(String input, boolean expectedInverse) throws Exception {
        assertNotEquals(IdentityUtil.isBlank(input), expectedInverse, "Expected value mismatches returned for input:" +
                " " + input);
    }

    @DataProvider
    public Object[][] getCleanUpTimeoutData() {
        long defaultVal = Long.parseLong(IdentityConstants.ServerConfig.CLEAN_UP_TIMEOUT_DEFAULT);
        return new Object[][]{
                {"1000", 1000L},
                {" ", defaultVal},
                {"NotANumber", defaultVal},
        };
    }

    @Test(dataProvider = "getCleanUpTimeoutData")
    public void testGetCleanUpTimeout(String value, long expected) throws Exception {
        Map<String, Object> mockConfiguration = new HashMap<>();
        mockConfiguration.put(IdentityConstants.ServerConfig.CLEAN_UP_TIMEOUT, value);
        Whitebox.setInternalState(IdentityUtil.class, "configuration", mockConfiguration);
        assertEquals(IdentityUtil.getCleanUpTimeout(), expected, "Expected value mismatches returned for input: " +
                value);
    }

    @DataProvider
    public Object[][] getCleanUpPeriodData() {
        long defaultVal = Long.parseLong(IdentityConstants.ServerConfig.CLEAN_UP_PERIOD_DEFAULT);
        return new Object[][]{
                {"1000", 1000L},
                {" ", defaultVal},
                {"NotANumber", defaultVal},
        };
    }

    @Test(dataProvider = "getCleanUpPeriodData")
    public void testGetCleanUpPeriod(String value, long expected) throws Exception {
        Map<String, Object> mockConfiguration = new HashMap<>();
        mockConfiguration.put(IdentityConstants.ServerConfig.CLEAN_UP_PERIOD, value);
        Whitebox.setInternalState(IdentityUtil.class, "configuration", mockConfiguration);
        assertEquals(IdentityUtil.getCleanUpPeriod("ignoredParam"), expected, "Expected value mismatches returned " +
                "for input: " + value);
    }

    @DataProvider
    public Object[][] getOperationCleanUpTimeoutData() {
        long defaultVal = Long.parseLong(IdentityConstants.ServerConfig.OPERATION_CLEAN_UP_TIMEOUT_DEFAULT);
        return new Object[][]{
                {"1000", 1000L},
                {" ", defaultVal},
                {"NotANumber", defaultVal},
        };
    }

    @Test(dataProvider = "getOperationCleanUpTimeoutData")
    public void testGetOperationCleanUpTimeout(String value, long expected) throws Exception {
        Map<String, Object> mockConfiguration = new HashMap<>();
        mockConfiguration.put(IdentityConstants.ServerConfig.OPERATION_CLEAN_UP_TIMEOUT, value);
        Whitebox.setInternalState(IdentityUtil.class, "configuration", mockConfiguration);
        assertEquals(IdentityUtil.getOperationCleanUpTimeout(), expected, "Expected value mismatches returned for " +
                "input: " + value);
    }

    @DataProvider
    public Object[][] getOperationCleanUpPeriodData() {
        long defaultVal = Long.parseLong(IdentityConstants.ServerConfig.OPERATION_CLEAN_UP_PERIOD_DEFAULT);
        return new Object[][]{
                {"1000", 1000L},
                {" ", defaultVal},
                {"NotANumber", defaultVal},
        };
    }

    @Test(dataProvider = "getOperationCleanUpPeriodData")
    public void testGetOperationCleanUpPeriod(String value, long expected) throws Exception {
        Map<String, Object> mockConfiguration = new HashMap<>();
        mockConfiguration.put(IdentityConstants.ServerConfig.OPERATION_CLEAN_UP_PERIOD, value);
        Whitebox.setInternalState(IdentityUtil.class, "configuration", mockConfiguration);
        assertEquals(IdentityUtil.getOperationCleanUpPeriod("IgnoredParam"), expected, "Expected value mismatches " +
                "returned for input: " + value);
    }

    @DataProvider
    public Object[][] getAddDomainToNameData() {
        return new Object[][]{
                {null, null, null},
                {"foo", null, "foo"},
                {null, "dom", null},
                {"dom2/foo", "dom", "dom2/foo"},
                {"foo", "Workflow", "Workflow/foo"},
                {"foo", "Application", "Application/foo"},
                {"foo", "Internal", "Internal/foo"},
                {"foo", "dom", "DOM/foo"},
                {"foo", "DOM", "DOM/foo"},
                {"foo", "PRIMARY", "foo"},
        };
    }

    @Test(dataProvider = "getAddDomainToNameData")
    public void testAddDomainToName(String name, String domainName, String expected) throws Exception {
        assertEquals(IdentityUtil.addDomainToName(name, domainName), expected, String.format("Expected value " +
                "mismatches returned for input: name = %s, domainName = %s.", name, domainName));
    }

    @Test(dependsOnMethods = "testIsUserStoreInUsernameCaseSensitive")
    public void testGetPrimaryDomainName() throws Exception {
        when(mockRealmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))
                .thenReturn("fooDomain");
        assertEquals(IdentityUtil.getPrimaryDomainName(), "FOODOMAIN", "Expected value for domain name mismatches " +
                "returned.");
    }

    @DataProvider
    public Object[][] getIsValidFileNameData() {
        return new Object[][]{
                {"", false},
                {"<file", false},
                {"\\file", false},
                {"|file", false},
                {"/home/file", false},
                {"file", true},
                {"957", true},
                {"@foo", true},
                {"?foo", false},
                {"*file", false},
        };
    }

    @Test(dataProvider = "getIsValidFileNameData")
    public void testIsValidFileName(String fileName, boolean expected) throws Exception {
        assertEquals(IdentityUtil.isValidFileName(fileName), expected, "Expected value mismatches returned for " +
                "input: " + fileName);
    }

    @DataProvider
    public Object[][] getFillURLPlaceholdersData() {
        when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.WEB_CONTEXT_ROOT)).thenReturn("/");
        when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.PROXY_CONTEXT_PATH)).thenReturn("proxyCtx");
        return new Object[][]{
                {"", "https", ""},
                {"${carbon.host}:${carbon.management.port}", "http", "localhost:9763"},
                {"${carbon.management.port},${mgt.transport.http.port},${mgt.transport.https.port}", "https", "9443," +
                        "9763,9443"},
                {"CtxProxy:${carbon.proxycontextpath}, CtxRoot:${carbon.webcontextroot}", "https", "CtxProxy:proxyCtx, " +
                        "CtxRoot:/"},
                {"${carbon.protocol}|${carbon.context}", "https", "https|"},
                {"${carbon.home}", "https", "carbon.home"},
                {"${invalid}", "https", "${invalid}"},
        };
    }

    @Test(dataProvider = "getFillURLPlaceholdersData")
    public void testFillURLPlaceholders(String stringWithPlaceholders, String mgtTransport, String expected) throws Exception {
        when(CarbonUtils.getManagementTransport()).thenReturn(mgtTransport);
        assertEquals(IdentityUtil.fillURLPlaceholders(stringWithPlaceholders), expected, String.format("Returned " +
                "value doesn't match the expected value for input: stringWithPlaceholders = %s, mgtTransport " +
                "= %s.", stringWithPlaceholders, mgtTransport));
    }

    @Test
    public void testIsTokenLoggable() throws Exception {
        IdentityLogTokenParser identityLogTokenParser = IdentityLogTokenParser.getInstance();
        identityLogTokenParser.getLogTokenMap().put("authz", "true");
        identityLogTokenParser.getLogTokenMap().put("access", "false");
        assertTrue(IdentityUtil.isTokenLoggable("authz"), "Expected true for 'authz'");
        assertFalse(IdentityUtil.isTokenLoggable("access"), "Expected false for 'access'");
        assertFalse(IdentityUtil.isTokenLoggable("refresh"), "Expected false for 'refresh'");
    }

    @Test
    public void testGetHostName() throws Exception {
        when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.HOST_NAME)).thenReturn("wso2.com");
        assertEquals(IdentityUtil.getHostName(), "wso2.com", "Host name should be the value provided at config.");
        when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.HOST_NAME)).thenReturn(null);
        assertEquals(IdentityUtil.getHostName(), "localhost", "If not configured, host name should be taken from " +
                "network utils");
    }

    @DataProvider
    public Object[][] getQueryBuilderData() {
        Map<String, String[]> singleValuedMap = new LinkedHashMap<>();
        singleValuedMap.put("param1", new String[]{"value1"});
        singleValuedMap.put("param2", new String[]{"value 2"});

        Map<String, String[]> multiValuedMap = new LinkedHashMap<>();
        multiValuedMap.put("paramX", new String[]{"valueA", "valueB"});
        multiValuedMap.put("paramY", new String[]{"valueC"});
        multiValuedMap.put("paramZ", new String[]{"valueD", "valueE"});

        Map<String, String[]> nullValuedMap = new LinkedHashMap<>();
        nullValuedMap.put(null, new String[]{"valueA", "valueB"});
        nullValuedMap.put("paramI", null);
        nullValuedMap.put("paramJ", new String[]{"valueC", null});
        nullValuedMap.put("paramK", new String[]{""});
        nullValuedMap.put("", new String[]{"valueD"});

        return new Object[][]{
                {"http://example.com", null, StringUtils.EMPTY},
                {"http://example.com", Collections.EMPTY_MAP, StringUtils.EMPTY},
                {"http://example.com", singleValuedMap, "param1=value1&param2=value+2"},
                {"http://example.com", multiValuedMap,
                        "paramX=valueA&paramX=valueB&paramY=valueC&paramZ=valueD&paramZ=valueE"},
                {"http://example.com", nullValuedMap, "paramJ=valueC&paramK="},
        };
    }

    @Test(dataProvider = "getQueryBuilderData")
    public void testBuildQueryString(String base, Map<String, String[]> params, String expectedFragment) throws
            Exception {
        assertEquals(IdentityUtil.buildQueryString(params), "?" + expectedFragment, String.format("Invalid response " +
                "for input: base = %s, params = %s.", base, params));
    }

    @Test(dataProvider = "getQueryBuilderData")
    public void testBuildFragmentString(String base, Map<String, String[]> params, String expectedFragment) throws
            Exception {
        assertEquals(IdentityUtil.buildFragmentString(params), "#" + expectedFragment, String.format("Invalid response " +
                "for input: base = %s, params = %s.", base, params));
    }

    @Test(dataProvider = "getQueryBuilderData")
    public void testBuildQueryUrl(String base, Map<String, String[]> params, String expectedFragment) throws Exception {

        if (params != null && params.size() > 0) {
            assertEquals(IdentityUtil.buildQueryUrl(base, params), base + "?" + expectedFragment,
                    String.format("Invalid response for input: base = %s, params = %s.", base, params));
        } else {
            assertEquals(IdentityUtil.buildQueryUrl(base, params), base + expectedFragment,
                    String.format("Invalid response for input: base = %s, params = %s.", base, params));
        }
    }

    @Test(dataProvider = "getQueryBuilderData")
    public void testBuildFragmentUrl(String base, Map<String, String[]> params, String expectedFragment) throws
            Exception {
        assertEquals(IdentityUtil.buildFragmentUrl(base, params), base + "#" + expectedFragment, String.format("Invalid response " +
                "for input: base = %s, params = %s.", base, params));
    }

    @DataProvider
    public Object[][] getClientIpAddressData() {
        Map<String, String> xFwdForMap = new HashMap<>();
        xFwdForMap.put("X-Forwarded-For", "10.100.5.101,10.100.8.2");
        xFwdForMap.put("Proxy-Client-IP", "shouldBeIgnored");

        Map<String, String> unknownEntryMap = new HashMap<>();
        unknownEntryMap.put("X-Forwarded-For", "UNKNOWN");
        xFwdForMap.put("Proxy-Client-IP", "");
        xFwdForMap.put("HTTP_X_FORWARDED_FOR", "192.168.1.1");

        return new Object[][]{
                {Collections.EMPTY_MAP, "127.0.0.1"},
                {xFwdForMap, "10.100.5.101"},
                {unknownEntryMap, "192.168.1.1"},
        };
    }

    @Test(dataProvider = "getClientIpAddressData")
    public void testGetClientIpAddress(Map<String, String> headers, String expected) throws Exception {

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            when(mockRequest.getHeader(entry.getKey())).thenReturn(entry.getValue());
        }
        assertEquals(IdentityUtil.getClientIpAddress(mockRequest), expected, String.format("Invalid response " +
                "for input: headers = %s.", headers));
    }

    @DataProvider
    public Object[][] getClockSkewData() {
        int defaultVal = Integer.parseInt(IdentityConstants.ServerConfig.CLOCK_SKEW_DEFAULT);
        return new Object[][]{
                {"1000", 1000},
                {" ", defaultVal},
                {"NotANumber", defaultVal},
        };
    }

    @Test(dataProvider = "getClockSkewData")
    public void testGetClockSkewInSeconds(String value, int expected) throws Exception {
        Map<String, Object> mockConfiguration = new HashMap<>();
        mockConfiguration.put(IdentityConstants.ServerConfig.CLOCK_SKEW, value);
        Whitebox.setInternalState(IdentityUtil.class, "configuration", mockConfiguration);
        assertEquals(IdentityUtil.getClockSkewInSeconds(), expected, String.format("Invalid response " +
                "for input: value = %s.", value));
    }

    @Test
    public void testIsSupportedByUserStore() throws Exception {
        when(mockRealmConfiguration.getUserStoreProperty("op1")).thenReturn("true");
        when(mockRealmConfiguration.getUserStoreProperty("op2")).thenReturn("false");
        assertTrue(IdentityUtil.isSupportedByUserStore(mockUserStoreManager, "op1"), "Expected true for op1");
        assertFalse(IdentityUtil.isSupportedByUserStore(mockUserStoreManager, "op2"), "Expected false for op2");
        assertTrue(IdentityUtil.isSupportedByUserStore(mockUserStoreManager, "op3"), "Expected true for op3");
        assertTrue(IdentityUtil.isSupportedByUserStore(null, "op4"), "Expected true for op4 in null userstore");
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

}
