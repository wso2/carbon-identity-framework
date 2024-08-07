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
import org.apache.axiom.om.OMElement;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.core.model.IdentityKeyStoreMapping;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants.*;

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

    @Mock
    private IdentityConfigParser mockIdentityConfigParser;

    @Mock
    private OMElement mockConfig;

    private IdentityKeyStoreResolver identityKeyStoreResolver;

    // Test key store mappings
    Map<InboundProtocol, IdentityKeyStoreMapping> keyStoreMappings = new ConcurrentHashMap<>();

    private MockedStatic<IdentityConfigParser> identityConfigParser;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        // Mock key store mapping configurations
        identityConfigParser = mockStatic(IdentityConfigParser.class);
        mockIdentityConfigParser = mock(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockIdentityConfigParser);

        mockConfig = mock(OMElement.class);
        when(mockIdentityConfigParser.getConfigElement(anyString())).thenReturn(mockConfig);
        when(mockConfig.getFirstChildWithName(any())).thenReturn(null);

        IdentityKeyStoreMapping oauthKeyStoreMapping = new IdentityKeyStoreMapping(CUSTOM_KEY_STORE, InboundProtocol.OAUTH, true);
        IdentityKeyStoreMapping wsTrustKeyStoreMapping = new IdentityKeyStoreMapping(CUSTOM_KEY_STORE, InboundProtocol.WS_TRUST, false);

        keyStoreMappings.put(InboundProtocol.OAUTH, oauthKeyStoreMapping);
        keyStoreMappings.put(InboundProtocol.WS_TRUST, wsTrustKeyStoreMapping);

        setFinalStatic(IdentityKeyStoreResolver.class.getDeclaredField("keyStoreMappings"), keyStoreMappings);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(SUPER_TENANT_DOMAIN)).thenReturn(Integer.valueOf(SUPER_TENANT_ID));
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(Integer.valueOf(TENANT_ID));

        identityKeyStoreResolver = IdentityKeyStoreResolver.getInstance();
    }

    @AfterMethod
    public void close() {

        identityConfigParser.close();
        identityTenantUtil.close();
    }

    @Test
    public void testGetInstance() {

        // Test for singleton instance
        IdentityKeyStoreResolver identityKeyStoreResolver1 = IdentityKeyStoreResolver.getInstance();
        IdentityKeyStoreResolver identityKeyStoreResolver2 = IdentityKeyStoreResolver.getInstance();
        assertEquals(identityKeyStoreResolver1, identityKeyStoreResolver2);
    }

    @Test
    public void testGetKeyStore() throws Exception {

        System.setProperty(CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString());

        KeyStore primaryKeyStore = getKeyStoreFromFile(PRIMARY_KEY_STORE, PRIMARY_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME));
        KeyStore tenantKeyStore = getKeyStoreFromFile(TENANT_KEY_STORE, TENANT_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME));
        KeyStore customKeyStore = getKeyStoreFromFile(CUSTOM_KEY_STORE, CUSTOM_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME));

        KeyStoreManager keyStoreManager = mock(KeyStoreManager.class);
        ConcurrentHashMap<String, KeyStoreManager> mtKeyStoreManagers = new ConcurrentHashMap();

        mtKeyStoreManagers.put(SUPER_TENANT_ID, keyStoreManager);
        mtKeyStoreManagers.put(TENANT_ID, keyStoreManager);
        setPrivateStaticField(KeyStoreManager.class, "mtKeyStoreManagers", mtKeyStoreManagers);

        when(keyStoreManager.getPrimaryKeyStore()).thenReturn(primaryKeyStore);
        when(keyStoreManager.getKeyStore(TENANT_KEY_STORE)).thenReturn(tenantKeyStore);
        when(keyStoreManager.getKeyStore("CUSTOM/" + CUSTOM_KEY_STORE)).thenReturn(customKeyStore);

        assertEquals(primaryKeyStore, identityKeyStoreResolver.getKeyStore(SUPER_TENANT_DOMAIN, InboundProtocol.WS_FEDERATION));
        assertEquals(tenantKeyStore, identityKeyStoreResolver.getKeyStore(TENANT_DOMAIN, InboundProtocol.WS_FEDERATION));
        assertEquals(customKeyStore, identityKeyStoreResolver.getKeyStore(SUPER_TENANT_DOMAIN, InboundProtocol.OAUTH));
        assertEquals(customKeyStore, identityKeyStoreResolver.getKeyStore(TENANT_DOMAIN, InboundProtocol.OAUTH));
        assertEquals(customKeyStore, identityKeyStoreResolver.getKeyStore(SUPER_TENANT_DOMAIN, InboundProtocol.WS_TRUST));
        assertEquals(tenantKeyStore, identityKeyStoreResolver.getKeyStore(TENANT_DOMAIN, InboundProtocol.WS_TRUST));
    }

    @Test
    public void testGetPrivateKey() throws Exception {

        System.setProperty(CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString());

        Key primaryKey = getKeyStoreFromFile(PRIMARY_KEY_STORE, PRIMARY_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getKey(PRIMARY_KEY_STORE_ALIAS, PRIMARY_KEY_STORE_PASSWORD.toCharArray());
        Key tenantKey = getKeyStoreFromFile(TENANT_KEY_STORE, TENANT_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getKey(TENANT_KEY_STORE_ALIAS, TENANT_KEY_STORE_PASSWORD.toCharArray());
        Key customKey = getKeyStoreFromFile(CUSTOM_KEY_STORE, CUSTOM_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getKey(CUSTOM_KEY_STORE_ALIAS, CUSTOM_KEY_STORE_PASSWORD.toCharArray());

        Map<String, Key> publicCerts = new ConcurrentHashMap<>();
        publicCerts.put(SUPER_TENANT_ID, primaryKey);
        publicCerts.put(TENANT_ID, tenantKey);
        publicCerts.put(InboundProtocol.OAUTH.toString(), customKey);
        publicCerts.put(InboundProtocol.WS_TRUST.toString(), customKey);

        setFinalStatic(IdentityKeyStoreResolver.class.getDeclaredField("privateKeys"), publicCerts);

        assertEquals(primaryKey, identityKeyStoreResolver.getPrivateKey(SUPER_TENANT_DOMAIN, InboundProtocol.WS_FEDERATION));
        assertEquals(tenantKey, identityKeyStoreResolver.getPrivateKey(TENANT_DOMAIN, InboundProtocol.WS_FEDERATION));
        assertEquals(customKey, identityKeyStoreResolver.getPrivateKey(SUPER_TENANT_DOMAIN, InboundProtocol.OAUTH));
        assertEquals(customKey, identityKeyStoreResolver.getPrivateKey(TENANT_DOMAIN, InboundProtocol.OAUTH));
        assertEquals(customKey, identityKeyStoreResolver.getPrivateKey(SUPER_TENANT_DOMAIN, InboundProtocol.WS_TRUST));
        assertEquals(tenantKey, identityKeyStoreResolver.getPrivateKey(TENANT_DOMAIN, InboundProtocol.WS_TRUST));
    }

    @Test
    public void testGetCertificate() throws Exception {

        System.setProperty(CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString());

        Certificate primaryCertificate = getKeyStoreFromFile(PRIMARY_KEY_STORE, PRIMARY_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getCertificate(PRIMARY_KEY_STORE_ALIAS);
        Certificate tenantCertificate = getKeyStoreFromFile(TENANT_KEY_STORE, TENANT_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getCertificate(TENANT_KEY_STORE_ALIAS);
        Certificate customCertificate = getKeyStoreFromFile(CUSTOM_KEY_STORE, CUSTOM_KEY_STORE_PASSWORD,
                System.getProperty(CarbonBaseConstants.CARBON_HOME)).getCertificate(CUSTOM_KEY_STORE_ALIAS);

        Map<String, Certificate> publicCerts = new ConcurrentHashMap<>();
        publicCerts.put(SUPER_TENANT_ID, primaryCertificate);
        publicCerts.put(TENANT_ID, tenantCertificate);
        publicCerts.put(InboundProtocol.OAUTH.toString(), customCertificate);
        publicCerts.put(InboundProtocol.WS_TRUST.toString(), customCertificate);

        setFinalStatic(IdentityKeyStoreResolver.class.getDeclaredField("publicCerts"), publicCerts);

        assertEquals(primaryCertificate, identityKeyStoreResolver.getCertificate(SUPER_TENANT_DOMAIN, InboundProtocol.WS_FEDERATION));
        assertEquals(tenantCertificate, identityKeyStoreResolver.getCertificate(TENANT_DOMAIN, InboundProtocol.WS_FEDERATION));
        assertEquals(customCertificate, identityKeyStoreResolver.getCertificate(SUPER_TENANT_DOMAIN, InboundProtocol.OAUTH));
        assertEquals(customCertificate, identityKeyStoreResolver.getCertificate(TENANT_DOMAIN, InboundProtocol.OAUTH));
        assertEquals(customCertificate, identityKeyStoreResolver.getCertificate(SUPER_TENANT_DOMAIN, InboundProtocol.WS_TRUST));
        assertEquals(tenantCertificate, identityKeyStoreResolver.getCertificate(TENANT_DOMAIN, InboundProtocol.WS_TRUST));
    }

    private void setFinalStatic(Field field, Object newValue) throws Exception {

        Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        getDeclaredFields0.setAccessible(true);
        Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
        Field modifiers = null;
        for (Field each : fields) {
            if ("modifiers".equals(each.getName())) {
                modifiers = each;
                break;
            }
        }
        field.setAccessible(true);
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
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