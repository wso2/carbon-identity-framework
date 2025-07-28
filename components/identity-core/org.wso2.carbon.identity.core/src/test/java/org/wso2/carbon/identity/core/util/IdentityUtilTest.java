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
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.AdminServicesUtil;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.core.util.SignatureUtil;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.IdentityKeyStoreResolver;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceComponent;
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
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants.ErrorMessages.ERROR_CODE_ERROR_RETRIEVING_TENANT_PUBLIC_CERTIFICATE;
import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants.ErrorMessages.ERROR_RETRIEVING_TENANT_CONTEXT_PUBLIC_CERTIFICATE_KEYSTORE_NOT_EXIST;

@Listeners(MockitoTestNGListener.class)
public class IdentityUtilTest {

    private static final String PRIMARY_KEY_STORE = "wso2carbon.jks";
    private static final String PRIMARY_KEY_STORE_PASSWORD = "wso2carbon";
    private static final String PRIMARY_KEY_STORE_ALIAS = "wso2carbon";

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
    @Mock
    private IdentityKeyStoreResolver mockIdentityKeyStoreResolver;
    @Mock
    private PrivateKey mockPrivateKey;
    @Mock
    private PublicKey mockPublicKey;
    @Mock
    private KeyStoreManager mockKeyStoreManager;
    @Mock
    private Certificate mockCertificate;

    private KeyStore primaryKeyStore;

    MockedStatic<CarbonUtils> carbonUtils;
    MockedStatic<ServerConfiguration> serverConfiguration;
    MockedStatic<NetworkUtils> networkUtils;
    MockedStatic<IdentityCoreServiceComponent> identityCoreServiceComponent;
    MockedStatic<IdentityConfigParser> identityConfigParser;
    MockedStatic<IdentityTenantUtil> identityTenantUtil;
    MockedStatic<SignatureUtil> signatureUtil;
    MockedStatic<IdentityKeyStoreResolver> identityKeyStoreResolver;
    MockedStatic<KeyStoreManager> keyStoreManager;
    private MockedStatic<KeystoreUtils> keystoreUtils;
    private MockedStatic<AdminServicesUtil> adminServicesUtil;
    private MockedStatic<UserCoreUtil> userCoreUtil;


    @BeforeMethod
    public void setUp() throws Exception {

        carbonUtils = mockStatic(CarbonUtils.class);
        carbonUtils.when(CarbonUtils::getServerConfiguration).thenReturn(this.mockServerConfiguration);
        serverConfiguration = mockStatic(ServerConfiguration.class);
        networkUtils = mockStatic(NetworkUtils.class);
        identityCoreServiceComponent = mockStatic(IdentityCoreServiceComponent.class);
        identityConfigParser = mockStatic(IdentityConfigParser.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        signatureUtil = mockStatic(SignatureUtil.class);
        identityKeyStoreResolver = mockStatic(IdentityKeyStoreResolver.class);
        keyStoreManager = mockStatic(KeyStoreManager.class);
        keystoreUtils = mockStatic(KeystoreUtils.class);
        adminServicesUtil = mockStatic(AdminServicesUtil.class);
        userCoreUtil = mockStatic(UserCoreUtil.class);

        serverConfiguration.when(ServerConfiguration::getInstance).thenReturn(mockServerConfiguration);
        identityCoreServiceComponent.when(
                IdentityCoreServiceComponent::getConfigurationContextService).thenReturn(mockConfigurationContextService);
        lenient().when(mockConfigurationContextService.getServerConfigContext()).thenReturn(mockConfigurationContext);
        lenient().when(mockConfigurationContext.getAxisConfiguration()).thenReturn(mockAxisConfiguration);
        identityTenantUtil.when(IdentityTenantUtil::getRealmService).thenReturn(mockRealmService);
        lenient().when(mockRealmService.getTenantManager()).thenReturn(mockTenantManager);
        carbonUtils.when(CarbonUtils::getCarbonHome).thenReturn("carbon.home");
        lenient().when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(mockUserStoreManager.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
        lenient().when(mockRealmService.getBootstrapRealmConfiguration()).thenReturn(mockRealmConfiguration);
        lenient().when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);

        networkUtils.when(NetworkUtils::getLocalHostname).thenReturn("localhost");

        System.setProperty(IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTP_PROPERTY, "9763");
        System.setProperty(IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTPS_PROPERTY, "9443");

        primaryKeyStore = getKeyStoreFromFile(PRIMARY_KEY_STORE, PRIMARY_KEY_STORE_PASSWORD);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        System.clearProperty(IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTP_PROPERTY);
        System.clearProperty(IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTPS_PROPERTY);

        IdentityLogTokenParser.getInstance().getLogTokenMap().remove("authz");
        IdentityLogTokenParser.getInstance().getLogTokenMap().remove("access");

        carbonUtils.close();
        serverConfiguration.close();
        networkUtils.close();
        identityCoreServiceComponent.close();
        identityConfigParser.close();
        identityTenantUtil.close();
        signatureUtil.close();
        identityKeyStoreResolver.close();
        keyStoreManager.close();
        keystoreUtils.close();
        adminServicesUtil.close();
        userCoreUtil.close();
    }

    @Test(description = "Test converting a certificate to PEM format")
    public void convertCertificateToPEM() throws CertificateException, KeyStoreException, IOException {

        Certificate validCertificate = primaryKeyStore.getCertificate(PRIMARY_KEY_STORE_ALIAS);
        Path pemPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources",
                "repository", "resources", "security", "certificate.pem");
        String expectedPEM = String.join("\n", Files.readAllLines(pemPath));

        assertEquals(IdentityUtil.convertCertificateToPEM(validCertificate), expectedPEM);
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

        setPrivateStaticField(IdentityUtil.class, "configuration", mockConfig);
        assertEquals(IdentityUtil.getProperty(key), expected, String.format("Property value mismatch for input: key " +
                "= %s, value = %s", key, String.valueOf(value)));
    }


    @Test
    public void testReadEventListenerProperty() throws Exception {

        IdentityEventListenerConfigKey key = new IdentityEventListenerConfigKey("ListenerType", "ListenerName");
        IdentityEventListenerConfig config = new IdentityEventListenerConfig("true", 0, key, new Properties());
        Map<IdentityEventListenerConfigKey, IdentityEventListenerConfig> mockedMap = new HashMap<>();
        mockedMap.put(key, config);
        setPrivateStaticField(IdentityUtil.class, "eventListenerConfiguration", mockedMap);

        IdentityEventListenerConfig configResponse = IdentityUtil.readEventListenerProperty("ListenerType", "ListenerName");
        assertEquals(configResponse.getEnable(), "true", "Listener should be enabled");
        assertEquals(configResponse.getOrder(), 0, "Listener should have order : 0");

        IdentityEventListenerConfig nullResponse = IdentityUtil.readEventListenerProperty("NonExistingType",
                "NonExistingListenerName");
        assertNull(nullResponse, "Response should be null for invalid listener.");
    }

    @DataProvider
    public Object[][] getIdentityCacheConfigTestData() throws Exception {
        Map<IdentityCacheConfigKey, IdentityCacheConfig> mockedCacheConfig = new HashMap<>();

        IdentityCacheConfigKey cacheConfigKey1 = new IdentityCacheConfigKey("manager1", "key1");
        IdentityCacheConfig cacheConfig1 = new IdentityCacheConfig(cacheConfigKey1);
        mockedCacheConfig.put(cacheConfigKey1, cacheConfig1);
        IdentityCacheConfigKey cacheConfigKey2 = new IdentityCacheConfigKey("manager2", "");
        IdentityCacheConfig cacheConfig2 = new IdentityCacheConfig(cacheConfigKey1);
        mockedCacheConfig.put(cacheConfigKey2, cacheConfig2);
        setPrivateStaticField(IdentityUtil.class, "identityCacheConfigurationHolder", mockedCacheConfig);
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
        setPrivateStaticField(IdentityUtil.class, "identityCookiesConfigurationHolder",
                mockIdentityCookiesConfigurationHolder);
        assertEquals(IdentityUtil.getIdentityCookiesConfigurationHolder(), mockIdentityCookiesConfigurationHolder,
                "Returned cookie holder doesn't match the given.");
    }

    @Test(dataProvider = "getReverseProxyConfigData")
    public void testGetProxyContext(String defaultContext1, String proxyContext1,
                                    String defaultContext2, String proxyContext2,
                                    String expectedDefaultContext, String expectedProxyContext) throws Exception {

        Map<String, ReverseProxyConfig> mockReverseProxyConfigurationHolder = new HashMap<>();
        mockReverseProxyConfigurationHolder.put(defaultContext1,
                new ReverseProxyConfig(defaultContext1, proxyContext1));
        mockReverseProxyConfigurationHolder.put(defaultContext2,
                new ReverseProxyConfig(defaultContext2, proxyContext2));
        setPrivateStaticField(IdentityUtil.class, "reverseProxyConfigurationHolder",
                mockReverseProxyConfigurationHolder);
        assertEquals(IdentityUtil.getProxyContext(expectedDefaultContext), expectedProxyContext,
                "Returned proxy context is incorrect.");
    }

    @Test
    public void testGetIdentityCookieConfig() throws Exception {
        Map<String, IdentityCookieConfig> mockIdentityCookiesConfigurationHolder = new HashMap<>();
        IdentityCookieConfig cookieConfig = new IdentityCookieConfig("cookieName");
        mockIdentityCookiesConfigurationHolder.put("cookie", cookieConfig);
        setPrivateStaticField(IdentityUtil.class, "identityCookiesConfigurationHolder",
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
        identityConfigParser.when(
                IdentityConfigParser::getEventListenerConfiguration).thenReturn(mockedEventListenerConfig);
        identityConfigParser.when(
                IdentityConfigParser::getIdentityCacheConfigurationHolder).thenReturn(mockedCacheConfig);
        identityConfigParser.when(
                IdentityConfigParser::getIdentityCookieConfigurationHolder).thenReturn(mockedCookieConfig);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockConfigParser);
        IdentityUtil.populateProperties();
        assertEquals(getPrivateStaticField(IdentityUtil.class, "configuration"), mockConfig,
                "Configuration is not set properly during config population");
        assertEquals(getPrivateStaticField(IdentityUtil.class, "eventListenerConfiguration"),
                mockedEventListenerConfig, "eventListenerConfiguration is not set properly during config population");
        assertEquals(IdentityUtil.getIdentityCookiesConfigurationHolder(), mockedCookieConfig,
                "cookieConfiguration is not set properly during config population");
        assertEquals(getPrivateStaticField(IdentityUtil.class, "identityCacheConfigurationHolder"), mockedCacheConfig,
                "identityCacheConfigurationHolder is not set properly during config population");
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
        carbonUtils.when(CarbonUtils::getCarbonConfigDirPath).thenReturn(mockedCarbonConfigDirPath);
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

        carbonUtils.when(() -> CarbonUtils.getTransportPort(any(AxisConfiguration.class), anyString())).thenReturn(9443);
        carbonUtils.when(() -> CarbonUtils.getTransportProxyPort(any(AxisConfiguration.class), anyString())).thenReturn(port);
        carbonUtils.when(CarbonUtils::getManagementTransport).thenReturn("https");
        when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.HOST_NAME)).thenReturn(host);
        lenient().when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.WEB_CONTEXT_ROOT)).thenReturn(ctxRoot);
        lenient().when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.PROXY_CONTEXT_PATH)).thenReturn(proxyCtx);

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

        lenient().when(mockTenantManager.getTenantId(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)).thenReturn
                (MultitenantConstants.SUPER_TENANT_ID);
        lenient().when(mockTenantManager.getTenantId("wso2.com")).thenReturn(1);
        lenient().when(mockTenantManager.getTenantId("none.com")).thenReturn(MultitenantConstants.INVALID_TENANT_ID);
        lenient().when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealm);
        lenient().when(mockUserStoreManager.getSecondaryUserStoreManager(anyString())).thenReturn(mockUserStoreManager);
        lenient().when(mockRealmConfiguration.getUserStoreProperty(IdentityCoreConstants.CASE_INSENSITIVE_USERNAME)).thenReturn
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
        setPrivateStaticField(IdentityUtil.class, "configuration", mockConfiguration);
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
        setPrivateStaticField(IdentityUtil.class, "configuration", mockConfiguration);
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
        setPrivateStaticField(IdentityUtil.class, "configuration", mockConfiguration);
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
        setPrivateStaticField(IdentityUtil.class, "configuration", mockConfiguration);
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
        lenient().when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.WEB_CONTEXT_ROOT)).thenReturn("/");
        lenient().when(mockServerConfiguration.getFirstProperty(IdentityCoreConstants.PROXY_CONTEXT_PATH)).thenReturn("proxyCtx");
        carbonUtils.when(CarbonUtils::getManagementTransport).thenReturn(mgtTransport);
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
                {unknownEntryMap, "127.0.0.1"},
        };
    }

    @Test(dataProvider = "getClientIpAddressData")
    public void testGetClientIpAddress(Map<String, String> headers, String expected) throws Exception {

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            lenient().when(mockRequest.getHeader(entry.getKey())).thenReturn(entry.getValue());
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

    @DataProvider
    public Object[][] getSubdomainData() {
        return new Object[][] {
                {"wso2.io", "dev.wso2.io", true},       // valid subdomain
                {"wso2.io", "wso2.io", true},           // exact match
                {"wso2.io", "dev.api.wso2.io", true},   // deeper subdomain
                {"wso2.io", "niyo.io", false},          // different domain
                {"wso2.io", "test.wso2.com", false},    // completely different domain
                {null, "wso2.io", false},               // domainName null
                {"wso2.io", null, false},               // hostName null
                {null, null, false}                     // both null
        };
    }

    @DataProvider(name = "rootDomainDataProvider")
    public Object[][] getRootDomainData() {
        return new Object[][] {
                {"dev.api.wso2.io", "wso2.io"},             // Deeper subdomain
                {"api.test.com", "test.com"},               // Typical subdomain
                {"abc.com", "abc.com"},                     // Root domain itself
                {"localhost", "localhost"},                 // Localhost
                {null, null},                               // Null case
                {"", ""}                                    // Empty string
        };
    }

    @Test(dataProvider = "getClockSkewData")
    public void testGetClockSkewInSeconds(String value, int expected) throws Exception {
        Map<String, Object> mockConfiguration = new HashMap<>();
        mockConfiguration.put(IdentityConstants.ServerConfig.CLOCK_SKEW, value);
        setPrivateStaticField(IdentityUtil.class, "configuration", mockConfiguration);
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

    @Test(dataProvider = "getSubdomainData")
    public void testCheckSubdomain(String domainName, String subdomainName, boolean expectedResult) throws Exception {

        boolean result = IdentityUtil.isSubdomain(domainName, subdomainName);
        assertEquals(result, expectedResult, "Subdomain check failed for: " + domainName + " and " + subdomainName);
    }

    @Test(dataProvider = "rootDomainDataProvider")
    public void testGetRootDomain(String domain, String expectedRootDomain) {

        String actualRootDomain = IdentityUtil.getRootDomain(domain);
        assertEquals(actualRootDomain, expectedRootDomain, "Root domain extraction failed for: " + domain);
    }

    private void setPrivateStaticField(Class<?> clazz, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, newValue);
    }

    private Object getPrivateStaticField(Class<?> clazz, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    @Test
    public void testIsShowLegacyRoleClaimOnGroupRoleSeparationEnabledWithNoUserRealm() {

        adminServicesUtil.when(AdminServicesUtil::getUserRealm).thenReturn(null);
        boolean result = IdentityUtil.isShowLegacyRoleClaimOnGroupRoleSeparationEnabled();
        assertFalse(result, "Expected false when user realm is null");
    }

    @Test
    public void testIsShowLegacyRoleClaimOnGroupRoleSeparationEnabledWithConfig() {

        org.wso2.carbon.user.core.UserRealm mockUserRealm = mock(org.wso2.carbon.user.core.UserRealm.class);
        adminServicesUtil.when(AdminServicesUtil::getUserRealm).thenReturn(mockUserRealm);
        userCoreUtil.when(() -> UserCoreUtil.isShowLegacyRoleClaimOnGroupRoleSeparationEnabled(any())).thenReturn(true);
        boolean result = IdentityUtil.isShowLegacyRoleClaimOnGroupRoleSeparationEnabled();
        assertTrue(result, "Expected true when the config is enabled");

    }

    @Test
    public void testIsShowLegacyRoleClaimOnGroupRoleSeparationEnabledWithException() {

        adminServicesUtil.when(AdminServicesUtil::getUserRealm).thenThrow(new CarbonException("Error occurred"));
        boolean result = IdentityUtil.isShowLegacyRoleClaimOnGroupRoleSeparationEnabled();
        assertFalse(result, "Expected false when an exception is thrown");
    }

    @Test(description = "Test getting system roles with APIResource collection config where the SystemRole config " +
            "is empty.")
    public void testSystemRolesConfigWithAPIResourcesWithEmptyConfig() throws Exception {

        IdentityConfigParser mockConfigParser = mock(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockConfigParser);
        OMElement mockOAuthConfigElement = mock(OMElement.class);
        lenient().when(mockConfigParser.getConfigElement(IdentityConstants.SystemRoles
                .SYSTEM_ROLES_CONFIG_ELEMENT)).thenReturn(null);
        // Call the method under test
        Map<String, Set<String>> result = IdentityUtil.getSystemRolesWithAPIResources();

        // Verify that the result is an empty map
        assertEquals(result.size(), 0, "Expected empty map");
    }

    @Test(description = "Test getting system roles with APIResource collection config where there is no roles " +
            "configured.")
    public void testGetSystemRolesWithAPIResourcesWithNoRoleConfigElement() {

        IdentityConfigParser mockConfigParser = mock(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockConfigParser);

        OMElement mockSystemRolesConfig = mock(OMElement.class);
        // Mock the config parser to return a valid systemRolesConfig but no roles
        when(mockConfigParser.getConfigElement(IdentityConstants.SystemRoles.SYSTEM_ROLES_CONFIG_ELEMENT)).thenReturn(mockSystemRolesConfig);
        when(mockSystemRolesConfig.getChildrenWithLocalName(IdentityConstants.SystemRoles.ROLE_CONFIG_ELEMENT)).thenReturn(null);

        // Call the method under test
        Map<String, Set<String>> result = IdentityUtil.getSystemRolesWithAPIResources();

        // Verify that the result is an empty map
        assertTrue(result.isEmpty());
    }

    @Test(description = "Test getting system roles with APIResource collection config where there is api resources " +
            "config is not added.")
    public void testGetSystemRolesWithAPIResourcesWithNoAPIResourceConfigElement() {

        IdentityConfigParser mockConfigParser = mock(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockConfigParser);

        OMElement mockSystemRolesConfig = mock(OMElement.class);
        OMElement mockRoleIdentifierConfig = mock(OMElement.class);
        OMElement mockRoleNameConfig = mock(OMElement.class);

        // Mock systemRolesConfig and role elements
        when(mockConfigParser.getConfigElement(IdentityConstants.SystemRoles.SYSTEM_ROLES_CONFIG_ELEMENT))
                .thenReturn(mockSystemRolesConfig);
        Iterator<OMElement> roleIterator = mock(Iterator.class);
        when(mockSystemRolesConfig.getChildrenWithLocalName(IdentityConstants.SystemRoles.ROLE_CONFIG_ELEMENT))
                .thenReturn(roleIterator);
        when(roleIterator.hasNext()).thenReturn(true, false);
        when(roleIterator.next()).thenReturn(mockRoleIdentifierConfig);

        // Mock the role name element
        String roleName = "admin";
        when(mockRoleIdentifierConfig.getFirstChildWithName(new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE,
                IdentityConstants.SystemRoles.ROLE_NAME_CONFIG_ELEMENT)))
                .thenReturn(mockRoleNameConfig);
        when(mockRoleNameConfig.getText()).thenReturn(roleName);

        when(mockRoleIdentifierConfig.getFirstChildWithName(new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE,
                IdentityConstants.SystemRoles.ROLE_MANDATORY_API_RESOURCES_CONFIG_ELEMENT)))
                .thenReturn(null);
        // Call the method under test
        Map<String, Set<String>> result = IdentityUtil.getSystemRolesWithAPIResources();

        // Verify that the result is an empty map
        assertTrue(result.isEmpty());
    }

    @Test(description = "Test getting system roles with APIResource collection config where proper roles and " +
            "corresponding api resources are configured.")
    public void testGetSystemRolesWithAPIResources() {

        IdentityConfigParser mockConfigParser = mock(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockConfigParser);

        OMElement mockSystemRolesConfig = mock(OMElement.class);
        OMElement mockRoleIdentifierConfig = mock(OMElement.class);
        OMElement mockRoleNameConfig = mock(OMElement.class);
        OMElement mockMandatoryAPIResources = mock(OMElement.class);
        OMElement mockAPIResourceIdentifier = mock(OMElement.class);

        // Mock systemRolesConfig and role elements
        when(mockConfigParser.getConfigElement(IdentityConstants.SystemRoles.SYSTEM_ROLES_CONFIG_ELEMENT))
                .thenReturn(mockSystemRolesConfig);
        Iterator<OMElement> roleIterator = mock(Iterator.class);
        when(mockSystemRolesConfig.getChildrenWithLocalName(IdentityConstants.SystemRoles.ROLE_CONFIG_ELEMENT))
                .thenReturn(roleIterator);
        when(roleIterator.hasNext()).thenReturn(true, false);
        when(roleIterator.next()).thenReturn(mockRoleIdentifierConfig);

        // Mock the role name element
        String roleName = "admin";
        String apiResource1 = "applications.write";
        String apiResource2 = "applications.read";
        when(mockRoleIdentifierConfig.getFirstChildWithName(new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE,
                IdentityConstants.SystemRoles.ROLE_NAME_CONFIG_ELEMENT)))
                .thenReturn(mockRoleNameConfig);
        when(mockRoleNameConfig.getText()).thenReturn(roleName);

        // Mock the scopes
        when(mockRoleIdentifierConfig.getFirstChildWithName(new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE,
                IdentityConstants.SystemRoles.ROLE_MANDATORY_API_RESOURCES_CONFIG_ELEMENT)))
                .thenReturn(mockMandatoryAPIResources);
        Iterator<OMElement> apiResourcesIterator = mock(Iterator.class);
        when(mockMandatoryAPIResources.getChildrenWithLocalName(IdentityConstants.SystemRoles.API_RESOURCE_CONFIG_ELEMENT))
                .thenReturn(apiResourcesIterator);
        when(apiResourcesIterator.hasNext()).thenReturn(true, true, false);
        when(apiResourcesIterator.next()).thenReturn(mockAPIResourceIdentifier);
        when(mockAPIResourceIdentifier.getText()).thenReturn(apiResource1, apiResource2);

        // Call the method under test
        Map<String, Set<String>> result = IdentityUtil.getSystemRolesWithAPIResources();

        // Verify the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey(roleName));
        Set<String> scopes = result.get(roleName);
        assertEquals(2, scopes.size());
        assertTrue(scopes.contains(apiResource1));
        assertTrue(scopes.contains(apiResource2));
    }

    private KeyStore getKeyStoreFromFile(String keystoreName, String password) throws Exception {

        Path tenantKeystorePath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "repository", "resources", "security", keystoreName);
        FileInputStream file = new FileInputStream(tenantKeystorePath.toString());
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(file, password.toCharArray());
        return keystore;
    }

    @Test
    public void testValidateSignatureFromTenant() throws Exception {

        String data = "testData";
        byte[] signature = new byte[]{1, 2, 3};
        String tenantDomain = "carbon.super";

        when(mockCertificate.getPublicKey()).thenReturn(mockPublicKey);
        identityKeyStoreResolver.when(IdentityKeyStoreResolver::getInstance).thenReturn(mockIdentityKeyStoreResolver);
        when(mockIdentityKeyStoreResolver.getCertificate(tenantDomain, null)).thenReturn(mockCertificate);
        signatureUtil.when(() -> SignatureUtil.validateSignature(data, signature, mockPublicKey)).thenReturn(true);

        boolean result = IdentityUtil.validateSignatureFromTenant(data, signature, tenantDomain);
        assertTrue(result);
    }

    @Test
    public void testValidateSignatureFromContextKeystore() throws Exception {

        String data = "testData";
        byte[] signature = new byte[]{1, 2, 3};
        String tenantDomain = "carbon.super";
        String context = "cookie";

        when(mockCertificate.getPublicKey()).thenReturn(mockPublicKey);
        identityKeyStoreResolver.when(IdentityKeyStoreResolver::getInstance).thenReturn(mockIdentityKeyStoreResolver);
        when(mockIdentityKeyStoreResolver.getCertificate(tenantDomain, null, context)).thenReturn(mockCertificate);
        signatureUtil.when(() -> SignatureUtil.validateSignature(data, signature, mockPublicKey)).thenReturn(true);

        boolean result = IdentityUtil.validateSignatureFromTenant(data, signature, tenantDomain, context);
        assertTrue(result);
    }

    @Test(description = "Validate signature when the context keystore does not exist. "
            + "Expect the method to return false without throwing an exception.")
    public void testValidateSignatureFromContextKeystoreIfNotExists() throws Exception {

        String data = "testData";
        byte[] signature = new byte[]{1, 2, 3};
        String tenantDomain = "carbon.super";
        String context = "cookie";

        identityKeyStoreResolver.when(IdentityKeyStoreResolver::getInstance).thenReturn(mockIdentityKeyStoreResolver);
        when(mockIdentityKeyStoreResolver.getCertificate(tenantDomain, null, context))
                .thenThrow(new IdentityKeyStoreResolverException
                        (ERROR_RETRIEVING_TENANT_CONTEXT_PUBLIC_CERTIFICATE_KEYSTORE_NOT_EXIST.getCode(),
                         ERROR_RETRIEVING_TENANT_CONTEXT_PUBLIC_CERTIFICATE_KEYSTORE_NOT_EXIST.getDescription()));
        signatureUtil.when(() -> SignatureUtil.validateSignature(data, signature, mockPublicKey)).thenReturn(true);

        boolean result = IdentityUtil.validateSignatureFromTenant(data, signature, tenantDomain, context);
        assertFalse(result);
    }

    @Test(description = "Validate signature when an unexpected exception occurs while retrieving the "
            + "tenant's public certificate. Expect a SignatureException to be thrown.",
            expectedExceptions = SignatureException.class)
    public void testValidateSignatureFromContextKeystoreNegative() throws Exception {

        String data = "testData";
        byte[] signature = new byte[]{1, 2, 3};
        String tenantDomain = "carbon.super";
        String context = "cookie";

        identityKeyStoreResolver.when(IdentityKeyStoreResolver::getInstance).thenReturn(mockIdentityKeyStoreResolver);
        when(mockIdentityKeyStoreResolver.getCertificate(tenantDomain, null, context))
                .thenThrow(new IdentityKeyStoreResolverException
                        (ERROR_CODE_ERROR_RETRIEVING_TENANT_PUBLIC_CERTIFICATE.getCode(),
                                ERROR_CODE_ERROR_RETRIEVING_TENANT_PUBLIC_CERTIFICATE.getDescription()));

        IdentityUtil.validateSignatureFromTenant(data, signature, tenantDomain, context);
    }

    @Test
    public void testSignWithTenantKey() throws Exception {

        String data = "testData";
        String superTenantDomain = "carbon.super";
        keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(mockKeyStoreManager);
        keystoreUtils.when(() -> KeystoreUtils.getKeyStoreFileExtension(superTenantDomain)).thenReturn(".jks");
        when(mockKeyStoreManager.getDefaultPrivateKey()).thenReturn(mockPrivateKey);
        when(mockKeyStoreManager.getPrivateKey(anyString(), anyString())).thenReturn(mockPrivateKey);

        byte[] expectedSignature = new byte[]{1, 2, 3};
        signatureUtil.when(() -> SignatureUtil.doSignature(data, mockPrivateKey)).thenReturn(expectedSignature);

        byte[] result = IdentityUtil.signWithTenantKey(data, "wso2.com");
        assertEquals(result, expectedSignature);

        // Test sign with super tenant key.
        result = IdentityUtil.signWithTenantKey(data, superTenantDomain);
        assertEquals(result, expectedSignature);

        // Sign with super tenant causing an exception.
        when(mockKeyStoreManager.getDefaultPrivateKey()).thenThrow(new Exception());
        try {
            IdentityUtil.signWithTenantKey(data, superTenantDomain);
        } catch (Exception e) {
            assertEquals(e.getMessage(), String.format(IdentityKeyStoreResolverConstants.ErrorMessages
                    .ERROR_CODE_ERROR_RETRIEVING_TENANT_PRIVATE_KEY.getDescription(), superTenantDomain));
        }
    }

@Test
public void testGetAgentIdentityUserstoreName_Default() throws Exception {
        // Remove any cached value
        Field field = IdentityUtil.class.getDeclaredField("groupsVsRolesSeparationImprovementsEnabled");
        field.setAccessible(true);
        field.set(null, null);

        // No config set, should return default
        Map<String, Object> mockConfig = new HashMap<>();
        setPrivateStaticField(IdentityUtil.class, "configuration", mockConfig);

        String userstoreName = IdentityUtil.getAgentIdentityUserstoreName();
        assertEquals(userstoreName, "AGENT", "Should return default agent identity userstore name");
}

@Test
public void testGetAgentIdentityUserstoreName_Configured() throws Exception {
        Map<String, Object> mockConfig = new HashMap<>();
        mockConfig.put("AgentIdentity.Userstore", "MY_AGENT_STORE");
        setPrivateStaticField(IdentityUtil.class, "configuration", mockConfig);

        String userstoreName = IdentityUtil.getAgentIdentityUserstoreName();
        assertEquals(userstoreName, "MY_AGENT_STORE", "Should return configured agent identity userstore name");
}

@Test
public void testIsAgentIdentityEnabled_True() throws Exception {
        Map<String, Object> mockConfig = new HashMap<>();
        mockConfig.put("AgentIdentity.Enabled", "true");
        setPrivateStaticField(IdentityUtil.class, "configuration", mockConfig);

        boolean enabled = IdentityUtil.isAgentIdentityEnabled();
        assertTrue(enabled, "Should return true when agent identity is enabled");
}

@Test
public void testIsAgentIdentityEnabled_False() throws Exception {
        Map<String, Object> mockConfig = new HashMap<>();
        mockConfig.put("AgentIdentity.Enabled", "false");
        setPrivateStaticField(IdentityUtil.class, "configuration", mockConfig);

        boolean enabled = IdentityUtil.isAgentIdentityEnabled();
        assertFalse(enabled, "Should return false when agent identity is disabled");
}

@Test
public void testIsAgentIdentityEnabled_Default() throws Exception {
        Map<String, Object> mockConfig = new HashMap<>();
        setPrivateStaticField(IdentityUtil.class, "configuration", mockConfig);

        boolean enabled = IdentityUtil.isAgentIdentityEnabled();
        assertFalse(enabled, "Should return false when agent identity config is not set");
}
}
