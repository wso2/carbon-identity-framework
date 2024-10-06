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
import org.wso2.carbon.identity.core.model.IdentityKeyStoreMapping;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants.*;

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

    private static Key primaryKey;
    private static Key tenantKey;
    private static Key customKey;

    private static Certificate primaryCertificate;
    private static Certificate tenantCertificate;
    private static Certificate customCertificate;

    @Mock
    private IdentityConfigParser mockIdentityConfigParser;

    private IdentityKeyStoreResolver identityKeyStoreResolver;

    // Test key store mappings.
    Map<InboundProtocol, IdentityKeyStoreMapping> keyStoreMappings = new ConcurrentHashMap<>();

    private MockedStatic<IdentityConfigParser> identityConfigParser;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeClass
    public void setUp() throws Exception {

        // Use identity.xml file from test resources.
        String identityXmlPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources",
                "identity.xml").toString();
        System.setProperty(ServerConstants.CARBON_HOME, ".");
        mockIdentityConfigParser = IdentityConfigParser.getInstance(identityXmlPath);

        // Mock IdentityConfigParser.
        identityConfigParser = mockStatic(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockIdentityConfigParser);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(Integer.valueOf(SUPER_TENANT_ID));
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(Integer.valueOf(TENANT_ID));

        identityKeyStoreResolver = IdentityKeyStoreResolver.getInstance();

        // Mock getKeyStore method of key store manager.
        System.setProperty(CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString());

        primaryKeyStore = getKeyStoreFromFile(PRIMARY_KEY_STORE, PRIMARY_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME));
        tenantKeyStore = getKeyStoreFromFile(TENANT_KEY_STORE, TENANT_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME));
        customKeyStore = getKeyStoreFromFile(CUSTOM_KEY_STORE, CUSTOM_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME));

        KeyStoreManager keyStoreManager = mock(KeyStoreManager.class);
        ConcurrentHashMap<String, KeyStoreManager> mtKeyStoreManagers = new ConcurrentHashMap();

        mtKeyStoreManagers.put(SUPER_TENANT_ID, keyStoreManager);
        mtKeyStoreManagers.put(TENANT_ID, keyStoreManager);
        setPrivateStaticField(KeyStoreManager.class, "mtKeyStoreManagers", mtKeyStoreManagers);

        when(keyStoreManager.getPrimaryKeyStore()).thenReturn(primaryKeyStore);
        when(keyStoreManager.getKeyStore(TENANT_KEY_STORE)).thenReturn(tenantKeyStore);
        when(keyStoreManager.getKeyStore("CUSTOM/" + CUSTOM_KEY_STORE)).thenReturn(customKeyStore);

        // Mock set private keys.
        primaryKey = getKeyStoreFromFile(PRIMARY_KEY_STORE, PRIMARY_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getKey(PRIMARY_KEY_STORE_ALIAS, PRIMARY_KEY_STORE_PASSWORD.toCharArray());
        tenantKey = getKeyStoreFromFile(TENANT_KEY_STORE, TENANT_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getKey(TENANT_KEY_STORE_ALIAS, TENANT_KEY_STORE_PASSWORD.toCharArray());
        customKey = getKeyStoreFromFile(CUSTOM_KEY_STORE, CUSTOM_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getKey(CUSTOM_KEY_STORE_ALIAS, CUSTOM_KEY_STORE_PASSWORD.toCharArray());

        Map<String, Key> privateKeys = new ConcurrentHashMap<>();
        privateKeys.put(SUPER_TENANT_ID, primaryKey);
        privateKeys.put(TENANT_ID, tenantKey);
        privateKeys.put(InboundProtocol.OAUTH.toString(), customKey);
        privateKeys.put(InboundProtocol.WS_TRUST.toString(), customKey);

        setPrivateStaticField(IdentityKeyStoreResolver.class, "privateKeys", privateKeys);

        // Mock set certificates.
        primaryCertificate = getKeyStoreFromFile(PRIMARY_KEY_STORE, PRIMARY_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getCertificate(PRIMARY_KEY_STORE_ALIAS);
        tenantCertificate = getKeyStoreFromFile(TENANT_KEY_STORE, TENANT_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getCertificate(TENANT_KEY_STORE_ALIAS);
        customCertificate = getKeyStoreFromFile(CUSTOM_KEY_STORE, CUSTOM_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getCertificate(CUSTOM_KEY_STORE_ALIAS);

        Map<String, Certificate> publicCerts = new ConcurrentHashMap<>();
        publicCerts.put(SUPER_TENANT_ID, primaryCertificate);
        publicCerts.put(TENANT_ID, tenantCertificate);
        publicCerts.put(InboundProtocol.OAUTH.toString(), customCertificate);
        publicCerts.put(InboundProtocol.WS_TRUST.toString(), customCertificate);

        setPrivateStaticField(IdentityKeyStoreResolver.class, "publicCerts", publicCerts);
    }

    @AfterClass
    public void close() {

        identityConfigParser.close();
        identityTenantUtil.close();
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
                "identity_err2.xml",
                "identity.xml" // Finally loading correct identity.xml file to be used for other tests.
        };
    }

    @Test(dataProvider = "MissingConfigDataProvider")
    public void testMissingConfigs(String fileName) {

        try {
            // Set instance to null for creating a new instance
            Field identityKeyStoreResolverInstance = IdentityKeyStoreResolver.class.getDeclaredField("instance");
            identityKeyStoreResolverInstance.setAccessible(true);
            identityKeyStoreResolverInstance.set(null, null);

            // Use custom identity.xml file from test resources.
            String identityXmlPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources",
                    fileName).toString();
            System.setProperty(ServerConstants.CARBON_HOME, ".");
            mockIdentityConfigParser = IdentityConfigParser.getInstance(identityXmlPath);

            // Mock IdentityConfigParser.
            identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockIdentityConfigParser);

            // Test for singleton instance.
            IdentityKeyStoreResolver identityKeyStoreResolver = IdentityKeyStoreResolver.getInstance();
        } catch (Exception e) {
            fail();
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
    public void testGetPrivateKey(String tenantDomain, InboundProtocol inboundProtocol, Key expectedKey)  throws Exception {

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
    public void testGetCertificate(String tenantDomain, InboundProtocol inboundProtocol, Certificate expectedCert) throws Exception {

        assertEquals(expectedCert, identityKeyStoreResolver.getCertificate(tenantDomain, inboundProtocol));
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
