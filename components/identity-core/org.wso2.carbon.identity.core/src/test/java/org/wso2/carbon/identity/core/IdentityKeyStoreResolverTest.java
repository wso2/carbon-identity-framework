/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.core;

import junit.framework.TestCase;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants.InboundProtocol;

/**
 * Test cases for IdentityKeyStoreResolver.
 */
public class IdentityKeyStoreResolverTest extends TestCase {

    private static final String PRIMARY_KEY_STORE = "wso2carbon.jks";
    private static final String PRIMARY_KEY_STORE_PASSWORD = "wso2carbon";
    private static final String PRIMARY_KEY_STORE_ALIAS = "wso2carbon";

    private static final String TENANT_KEY_STORE = "foo-com.jks";
    private static final String TENANT_KEY_STORE_PASSWORD = "foo.com";
    private static final String TENANT_KEY_STORE_ALIAS = "foo.com";

    private static final String CUSTOM_KEY_STORE = "custom.jks";
    private static final String CUSTOM_KEY_STORE_PASSWORD = "custom";
    private static final String CUSTOM_KEY_STORE_ALIAS = "custom";

    private static final String SUPER_TENANT_DOMAIN = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    private static final String SUPER_TENANT_ID = "-1234";
    private static final String TENANT_DOMAIN = "foo.com";
    private static final String TENANT_ID = "1";

    private KeyStore primaryKeyStore;
    private KeyStore tenantKeyStore;
    private KeyStore customKeyStore;

    private static PrivateKey primaryKey;
    private static PrivateKey tenantKey;
    private static PrivateKey customKey;

    private static X509Certificate primaryCertificate;
    private static X509Certificate tenantCertificate;
    private static X509Certificate customCertificate;

    @Mock
    private IdentityConfigParser mockIdentityConfigParser;

    private MockedStatic<IdentityConfigParser> identityConfigParser;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<KeystoreUtils> keystoreUtils;

    private IdentityKeyStoreResolver identityKeyStoreResolver;

    @BeforeClass
    public void setUp() throws Exception {

        // Set test resource path.
        String identityXmlPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources",
                "identity.xml").toString();

        // Mock IdentityConfigParser.
        mockIdentityConfigParser = IdentityConfigParser.getInstance(identityXmlPath);
        identityConfigParser = mockStatic(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockIdentityConfigParser);

        // Mock IdentityTenantUtil.
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(Integer.valueOf(SUPER_TENANT_ID));
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(Integer.valueOf(TENANT_ID));

        // Mock getKeyStore method of key store manager.
        System.setProperty(CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString());

        primaryKeyStore = getKeyStoreFromFile(PRIMARY_KEY_STORE, PRIMARY_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME));
        tenantKeyStore = getKeyStoreFromFile(TENANT_KEY_STORE, TENANT_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME));
        customKeyStore = getKeyStoreFromFile(CUSTOM_KEY_STORE, CUSTOM_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME));

        primaryKey = (PrivateKey) primaryKeyStore.getKey(PRIMARY_KEY_STORE_ALIAS, PRIMARY_KEY_STORE_PASSWORD.toCharArray());
        tenantKey = (PrivateKey) tenantKeyStore.getKey(TENANT_KEY_STORE_ALIAS, TENANT_KEY_STORE_PASSWORD.toCharArray());
        customKey = (PrivateKey) customKeyStore.getKey(CUSTOM_KEY_STORE_ALIAS, CUSTOM_KEY_STORE_PASSWORD.toCharArray());

        primaryCertificate = (X509Certificate) primaryKeyStore.getCertificate(PRIMARY_KEY_STORE_ALIAS);
        tenantCertificate = (X509Certificate) tenantKeyStore.getCertificate(TENANT_KEY_STORE_ALIAS);
        customCertificate = (X509Certificate) customKeyStore.getCertificate(CUSTOM_KEY_STORE_ALIAS);

        KeyStoreManager keyStoreManager = mock(KeyStoreManager.class);
        ConcurrentHashMap<String, KeyStoreManager> mtKeyStoreManagers = new ConcurrentHashMap();

        mtKeyStoreManagers.put(SUPER_TENANT_ID, keyStoreManager);
        mtKeyStoreManagers.put(TENANT_ID, keyStoreManager);
        setPrivateStaticField(KeyStoreManager.class, "mtKeyStoreManagers", mtKeyStoreManagers);

        when(keyStoreManager.getPrimaryKeyStore()).thenReturn(primaryKeyStore);
        when(keyStoreManager.getKeyStore(TENANT_KEY_STORE)).thenReturn(tenantKeyStore);
        when(keyStoreManager.getKeyStore("CUSTOM/" + CUSTOM_KEY_STORE)).thenReturn(customKeyStore);

        when(keyStoreManager.getDefaultPrivateKey()).thenReturn(primaryKey);
        when(keyStoreManager.getPrivateKey(TENANT_KEY_STORE, TENANT_KEY_STORE_ALIAS)).thenReturn(tenantKey);
        when(keyStoreManager.getPrivateKey("CUSTOM/" + CUSTOM_KEY_STORE, null)).thenReturn(customKey);

        when(keyStoreManager.getDefaultPrimaryCertificate()).thenReturn(primaryCertificate);
        when(keyStoreManager.getCertificate(TENANT_KEY_STORE, TENANT_KEY_STORE_ALIAS)).thenReturn(tenantCertificate);
        when(keyStoreManager.getCertificate("CUSTOM/" + CUSTOM_KEY_STORE, null)).thenReturn(customCertificate);

        identityKeyStoreResolver = IdentityKeyStoreResolver.getInstance();
        keystoreUtils = mockStatic(KeystoreUtils.class);
    }

    @AfterClass
    public void close() {

        identityConfigParser.close();
        identityTenantUtil.close();
        keystoreUtils.close();
    }

    @Test
    public void testGetInstance() {

        // Test for singleton instance.
        IdentityKeyStoreResolver identityKeyStoreResolver1 = IdentityKeyStoreResolver.getInstance();
        IdentityKeyStoreResolver identityKeyStoreResolver2 = IdentityKeyStoreResolver.getInstance();
        assertEquals(identityKeyStoreResolver1, identityKeyStoreResolver2);
    }

    @DataProvider(name = "MissingConfigDataProvider")
    public String[] missingConfigDataProvider() {

        return new String[] {
                "identity_err1.xml",
                "identity_err2.xml"
        };
    }

    @Test(dataProvider = "MissingConfigDataProvider")
    public void testMissingConfigs(String fileName) {

        try {
            // Set current instance to null before creating a new instance
            Field identityKeyStoreResolverInstance = IdentityKeyStoreResolver.class.getDeclaredField("instance");
            identityKeyStoreResolverInstance.setAccessible(true);
            identityKeyStoreResolverInstance.set(null, null);

            // Use custom identity.xml file from test resources.
            String identityXmlPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources",
                    fileName).toString();

            // Mock IdentityConfigParser.
            mockIdentityConfigParser = IdentityConfigParser.getInstance(identityXmlPath);
            identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockIdentityConfigParser);

            // Test instance creation --> Config read.
            IdentityKeyStoreResolver.getInstance();
        } catch (Exception e) {
            fail("Test failed due to exception: " + e);
        }
    }

    @DataProvider(name = "KeyStoreDataProvider")
    public Object[][] keyStoreDataProvider() {

        return new Object[][] {
                {SUPER_TENANT_DOMAIN, InboundProtocol.WS_FEDERATION, primaryKeyStore},
                {TENANT_DOMAIN, InboundProtocol.WS_FEDERATION, tenantKeyStore},
                {SUPER_TENANT_DOMAIN, InboundProtocol.OAUTH, customKeyStore},
                {TENANT_DOMAIN, InboundProtocol.OAUTH, customKeyStore},
                {SUPER_TENANT_DOMAIN, InboundProtocol.WS_TRUST, customKeyStore},
                {TENANT_DOMAIN, InboundProtocol.WS_TRUST, tenantKeyStore}
        };
    }

    @Test(dataProvider = "KeyStoreDataProvider")
    public void testGetKeyStore(String tenantDomain, InboundProtocol inboundProtocol, KeyStore expectedKeyStore) throws Exception {

        keystoreUtils.when(() -> KeystoreUtils.getKeyStoreFileExtension(tenantDomain.replace(".", "-"), tenantDomain)).thenReturn(".jks");
        assertEquals(expectedKeyStore, identityKeyStoreResolver.getKeyStore(tenantDomain, inboundProtocol));
    }

    @DataProvider(name = "PrivateKeyDataProvider")
    public Object[][] privateKeyDataProvider() {

        return new Object[][] {
                {SUPER_TENANT_DOMAIN, InboundProtocol.WS_FEDERATION, primaryKey},
                {TENANT_DOMAIN, InboundProtocol.WS_FEDERATION, tenantKey},
                {SUPER_TENANT_DOMAIN, InboundProtocol.OAUTH, customKey},
                {TENANT_DOMAIN, InboundProtocol.OAUTH, customKey},
                {SUPER_TENANT_DOMAIN, InboundProtocol.WS_TRUST, customKey},
                {TENANT_DOMAIN, InboundProtocol.WS_TRUST, tenantKey}
        };
    }

    @Test(dataProvider = "PrivateKeyDataProvider")
    public void testGetPrivateKey(String tenantDomain, InboundProtocol inboundProtocol, PrivateKey expectedKey)  throws Exception {

        keystoreUtils.when(() -> KeystoreUtils.getKeyStoreFileExtension(tenantDomain.replace(".", "-"), tenantDomain)).thenReturn(".jks");
        assertEquals(expectedKey, identityKeyStoreResolver.getPrivateKey(tenantDomain, inboundProtocol));
    }

    @DataProvider(name = "PublicCertificateDataProvider")
    public Object[][] publicCertificateDataProvider() {

        return new Object[][] {
                {SUPER_TENANT_DOMAIN, InboundProtocol.WS_FEDERATION, primaryCertificate},
                {TENANT_DOMAIN, InboundProtocol.WS_FEDERATION, tenantCertificate},
                {SUPER_TENANT_DOMAIN, InboundProtocol.OAUTH, customCertificate},
                {TENANT_DOMAIN, InboundProtocol.OAUTH, customCertificate},
                {SUPER_TENANT_DOMAIN, InboundProtocol.WS_TRUST, customCertificate},
                {TENANT_DOMAIN, InboundProtocol.WS_TRUST, tenantCertificate}
        };
    }

    @Test(dataProvider = "PublicCertificateDataProvider")
    public void testGetCertificate(String tenantDomain, InboundProtocol inboundProtocol, X509Certificate expectedCert) throws Exception {

        keystoreUtils.when(() -> KeystoreUtils.getKeyStoreFileExtension(tenantDomain.replace(".", "-"), tenantDomain)).thenReturn(".jks");
        assertEquals(expectedCert, identityKeyStoreResolver.getCertificate(tenantDomain, inboundProtocol));
    }

    @Test
    public void testGetCustomKeyStore_Success() throws Exception {

        keystoreUtils.when(() -> KeystoreUtils.getKeyStoreFileExtension(TENANT_DOMAIN)).thenReturn(".jks");
        KeyStore result = identityKeyStoreResolver.getCustomKeyStore(TENANT_DOMAIN, CUSTOM_KEY_STORE);
        assertEquals(customKeyStore, result);
    }

    @Test(expectedExceptions = IdentityKeyStoreResolverException.class)
    public void testGetCustomKeyStore_InvalidTenantDomain() throws Exception {

        identityKeyStoreResolver.getCustomKeyStore("", CUSTOM_KEY_STORE);
    }

    @Test(expectedExceptions = IdentityKeyStoreResolverException.class)
    public void testGetCustomKeyStore_InvalidKeyStoreName() throws Exception {

        identityKeyStoreResolver.getCustomKeyStore(TENANT_DOMAIN, "");
    }

    @Test(expectedExceptions = IdentityKeyStoreResolverException.class)
    public void testGetTrustStore_Exception() throws Exception {

        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(Integer.valueOf(TENANT_ID));
        when(keyStoreManager.getTrustStore()).thenThrow(new org.wso2.carbon.CarbonException("error"));
        identityKeyStoreResolver.getTrustStore(TENANT_DOMAIN);
    }

    private KeyStore getKeyStoreFromFile(String keystoreName, String password, String home) throws Exception {

        Path tenantKeystorePath = Paths.get(home, "repository", "resources", "security", keystoreName);
        FileInputStream file = new FileInputStream(tenantKeystorePath.toString());
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(file, password.toCharArray());
        return keystore;
    }

    private void setPrivateStaticField(Class<?> clazz, String fieldName, Object newValue)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, newValue);
    }
}
