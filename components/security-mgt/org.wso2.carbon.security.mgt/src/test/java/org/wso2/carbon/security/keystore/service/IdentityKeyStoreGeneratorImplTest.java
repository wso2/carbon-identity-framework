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

package org.wso2.carbon.security.keystore.service;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.*;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;
import org.wso2.carbon.security.keystore.KeyStoreManagementException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

@Listeners(MockitoTestNGListener.class)
public class IdentityKeyStoreGeneratorImplTest extends IdentityBaseTest {

    private static final String KEYSTORE_PASSWORD = "wso2carbon";
    private static final String TENANT_DOMAIN = "wso2.com";
    private static final String CONTEXT = "cookie";
    private static final String CONTEXT_KEY_ALIAS = "wso2.com--cookie";
    private static final String CONTEXT_KEYSTORE_NAME = "wso2-com--cookie.jks";
    private static final String KEY_ALGORITHM_CONFIG = "Security.TenantKeyStore.KeyAlgorithm";
    private static final String KEY_SIZE_CONFIG = "Security.TenantKeyStore.KeySize";
    private static final String SIGNING_ALG_CONFIG = "Tenant.SigningAlgorithm";
    private static final long CERT_NOT_BEFORE_OFFSET = TimeUnit.DAYS.toMillis(30);
    private static final long CERT_NOT_AFTER_OFFSET = TimeUnit.DAYS.toMillis(365 * 10);
    private static final long TIME_TOLERANCE = TimeUnit.MINUTES.toMillis(5);

    private IdentityKeyStoreGeneratorImpl identityKeyStoreGenerator;

    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    @Mock
    private KeyStoreManager keyStoreManager;

    @Mock
    private KeyStore mockKeyStore;

    private PersistedKeyStore persistedKeyStore;

    @BeforeMethod
    public void setUp() throws Exception {

        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }

        System.setProperty(
                CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString()
        );
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        persistedKeyStore = null;
    }

    @AfterMethod
    public void tearDown() throws Exception {

        identityKeyStoreGenerator = null;
        identityTenantUtil.close();
    }

    @Test(description = "Test the generation of a keystore for a given tenant domain and context if exits.")
    public void testGenerateKeystoreIfExists() throws Exception {

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeystoreUtils> keyStoreUtils = mockStatic(KeystoreUtils.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);
            identityTenantUtil.when(()->IdentityTenantUtil.getTenantId("carbon.super"))
                    .thenReturn(-1234);
            identityTenantUtil.when(() -> IdentityTenantUtil.initializeRegistry(anyInt()))
                    .thenAnswer((Answer<Void>) invocation -> null);
            keyStoreUtils.when(() -> KeystoreUtils.getKeyStoreFileLocation("carbon-super--cookie", "carbon.super"))
                    .thenReturn("carbon-super--cookie.jks");
            when(this.keyStoreManager.getKeyStore("carbon-super--cookie.jks"))
                    .thenReturn(getKeyStoreFromFile("carbon-super--cookie.jks", KEYSTORE_PASSWORD));
            identityKeyStoreGenerator = new IdentityKeyStoreGeneratorImpl();
            identityKeyStoreGenerator.generateKeyStore("carbon.super", "cookie");
        }
    }

    /**
     * Sets up the mock behavior for KeyStoreManager and KeystoreUtils.
     *
     * @param exceptionToThrow the exception to throw when `getKeyStore` is called.
     * @throws Exception if any setup steps fail.
     */
    private void setupKeyStoreMocksWithException(Exception exceptionToThrow) throws Exception {
        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeystoreUtils> keyStoreUtils = mockStatic(KeystoreUtils.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);
            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId("carbon.super")).thenReturn(-1234);
            identityTenantUtil.when(() -> IdentityTenantUtil.initializeRegistry(anyInt()))
                    .thenAnswer((Answer<Void>) invocation -> null);
            keyStoreUtils.when(() -> KeystoreUtils.getKeyStoreFileLocation("carbon.super--cookie"))
                    .thenReturn("wso2carbon--cookie.jks");

            when(this.keyStoreManager.getKeyStore("wso2carbon--cookie.jks")).thenThrow(exceptionToThrow);
        }
    }

    @Test(description = "Test error creating a keystore for a given tenant domain and context with SecurityException.",
            expectedExceptions = KeyStoreManagementException.class)
    public void testGenerateKeystoreWithSecurityException() throws Exception {

        setupKeyStoreMocksWithException(new SecurityException("Error while creating keystore."));
        identityKeyStoreGenerator = new IdentityKeyStoreGeneratorImpl();
        identityKeyStoreGenerator.generateKeyStore("carbon.super", "cookie");
    }

    @Test(description = "Test error creating a keystore for a given tenant domain and context with generic Exception.",
            expectedExceptions = KeyStoreManagementException.class)
    public void testGenerateKeystoreWithGenericException() throws Exception {

        setupKeyStoreMocksWithException(new Exception("Error while creating keystore."));
        identityKeyStoreGenerator = new IdentityKeyStoreGeneratorImpl();
        identityKeyStoreGenerator.generateKeyStore("carbon.super", "cookie");
    }


    @Test(description = "Test the generation of a keystore for a given tenant domain and context if not exits.")
    public void testGenerateKeystoreIfNotExists() throws Exception {

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeystoreUtils> keyStoreUtils = mockStatic(KeystoreUtils.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);
            identityTenantUtil.when(()->IdentityTenantUtil.getTenantId("carbon.super"))
                    .thenReturn(-1234);
            identityTenantUtil.when(() -> IdentityTenantUtil.initializeRegistry(anyInt()))
                    .thenAnswer((Answer<Void>) invocation -> null);
            keyStoreUtils.when(() -> KeystoreUtils.getKeyStoreFileLocation("carbon-super--cookie", "carbon.super"))
                    .thenReturn("carbon-super--cookie.jks");
            when(this.keyStoreManager.getKeyStore("carbon-super--cookie.jks"))
                    .thenThrow(new SecurityException("Key Store with a name: carbon-super--cookie.jks" +
                            " does not exist."));
            keyStoreUtils.when(() -> KeystoreUtils.getKeyStoreFileType("carbon.super"))
                    .thenReturn("JKS");
            keyStoreUtils.when(() -> KeystoreUtils.getKeystoreInstance("JKS"))
                    .thenReturn(this.mockKeyStore);
            keyStoreUtils.when(KeystoreUtils::getTenantKeyAlgorithm).thenReturn("RSA");
            keyStoreUtils.when(KeystoreUtils::getTenantKeySize).thenReturn(2048);
            doNothing().when(this.mockKeyStore).setKeyEntry(anyString(), any(PrivateKey.class), any(), any());

            identityKeyStoreGenerator = new IdentityKeyStoreGeneratorImpl();
            identityKeyStoreGenerator.generateKeyStore("carbon.super", "cookie");
        }
    }

    @Test(description = "Test the generation of a keystore for a given tenant domain and context if not exits.")
    public void testGenerateKeystoreAlreadyExists() throws Exception {

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeystoreUtils> keyStoreUtils = mockStatic(KeystoreUtils.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);
            identityTenantUtil.when(()->IdentityTenantUtil.getTenantId("carbon.super"))
                    .thenReturn(-1234);
            identityTenantUtil.when(() -> IdentityTenantUtil.initializeRegistry(anyInt()))
                    .thenAnswer((Answer<Void>) invocation -> null);
            keyStoreUtils.when(() -> KeystoreUtils.getKeyStoreFileLocation("carbon-super--cookie", "carbon.super"))
                    .thenReturn("carbon-super--cookie.jks");
            when(this.keyStoreManager.getKeyStore("carbon-super--cookie.jks"))
                    .thenThrow(new SecurityException("Key Store with a name: carbon-super--cookie.jks" +
                            " does not exist."));
            keyStoreUtils.when(() -> KeystoreUtils.getKeyStoreFileType("carbon.super"))
                    .thenReturn("JKS");
            keyStoreUtils.when(() -> KeystoreUtils.getKeystoreInstance("JKS"))
                    .thenReturn(this.mockKeyStore);
            keyStoreUtils.when(KeystoreUtils::getTenantKeyAlgorithm).thenReturn("RSA");
            keyStoreUtils.when(KeystoreUtils::getTenantKeySize).thenReturn(2048);
            doNothing().when(this.mockKeyStore).setKeyEntry(anyString(), any(PrivateKey.class), any(), any());
            keyStoreUtils.when(() -> KeystoreUtils.getKeyStoreFileExtension("carbon.super"))
                    .thenReturn(".jks");
            doThrow(new SecurityException("Key store carbon-super--cookie.jks already available"))
                    .when(this.keyStoreManager)
                    .addKeyStore(
                            any(byte[].class), // Match any byte array
                            anyString(),       // Match any String
                            any(char[].class), // Match any char array
                            anyString(),       // Match any String
                            anyString(),       // Match any String
                            any(char[].class)  // Match any char array
                    );

            identityKeyStoreGenerator = new IdentityKeyStoreGeneratorImpl();
            identityKeyStoreGenerator.generateKeyStore("carbon.super", "cookie");
        }
    }

    @Test(description = "Test the generation of a keystore for a given tenant domain and context if not exits.",
    expectedExceptions = KeyStoreManagementException.class)
    public void testGenerateKeystoreIfNotExistsNegative() throws Exception {

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeystoreUtils> keyStoreUtils = mockStatic(KeystoreUtils.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);
            identityTenantUtil.when(()->IdentityTenantUtil.getTenantId("carbon.super"))
                    .thenReturn(-1234);
            identityTenantUtil.when(() -> IdentityTenantUtil.initializeRegistry(anyInt()))
                    .thenAnswer((Answer<Void>) invocation -> null);
            keyStoreUtils.when(() -> KeystoreUtils.getKeyStoreFileLocation("carbon-super--cookie", "carbon.super"))
                    .thenReturn("carbon-super--cookie.jks");
            when(this.keyStoreManager.getKeyStore("carbon-super--cookie.jks"))
                    .thenThrow(new SecurityException("Key Store with a name: carbon-super--cookie.jks" +
                            " does not exist."));
            keyStoreUtils.when(() -> KeystoreUtils.getKeyStoreFileType("carbon.super"))
                    .thenReturn("JKS");
            keyStoreUtils.when(() -> KeystoreUtils.getKeystoreInstance("JKS"))
                    .thenReturn(this.mockKeyStore);
            keyStoreUtils.when(KeystoreUtils::getTenantKeyAlgorithm).thenReturn("RSA");
            keyStoreUtils.when(KeystoreUtils::getTenantKeySize).thenReturn(2048);
            doNothing().when(this.mockKeyStore).setKeyEntry(anyString(), any(PrivateKey.class), any(), any());
            keyStoreUtils.when(() -> KeystoreUtils.getKeyStoreFileExtension("carbon.super"))
                    .thenReturn(".jks");
            doThrow(new SecurityException("Error while adding keystore"))
                    .when(this.keyStoreManager)
                    .addKeyStore(
                            any(byte[].class), // Match any byte array
                            anyString(),       // Match any String
                            any(char[].class), // Match any char array
                            anyString(),       // Match any String
                            anyString(),       // Match any String
                            any(char[].class)  // Match any char array
                    );

            identityKeyStoreGenerator = new IdentityKeyStoreGeneratorImpl();
            identityKeyStoreGenerator.generateKeyStore("carbon.super", "cookie");
        }
    }


    @Test(description = "Test the key and certificate of a context keystore when no key configuration is set.")
    public void testGenerateContextKeyStoreWithDefaultKeyConfiguration() throws Exception {

        long now = System.currentTimeMillis();
        generateContextKeyStore(new HashMap<>());

        X509Certificate certificate = getPersistedCertificate();
        assertEquals(certificate.getPublicKey().getAlgorithm(), "RSA");
        assertEquals(((RSAPublicKey) certificate.getPublicKey()).getModulus().bitLength(), 2048);
        assertEquals(((RSAPrivateKey) getPersistedPrivateKey()).getModulus().bitLength(), 2048);
        assertSelfSignedCertificate(certificate, now);
    }

    @DataProvider(name = "validKeySizes")
    public Object[][] validKeySizes() {

        return new Object[][]{{"2048", 2048}, {"3072", 3072}, {"4096", 4096}};
    }

    @Test(description = "Test the key size of a context keystore with a configured key size.",
            dataProvider = "validKeySizes")
    public void testGenerateContextKeyStoreWithConfiguredKeySize(String configuredKeySize, int expectedKeySize)
            throws Exception {

        Map<String, String> configuration = new HashMap<>();
        configuration.put(KEY_ALGORITHM_CONFIG, "RSA");
        configuration.put(KEY_SIZE_CONFIG, configuredKeySize);
        long now = System.currentTimeMillis();
        generateContextKeyStore(configuration);

        X509Certificate certificate = getPersistedCertificate();
        RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();
        RSAPrivateKey privateKey = (RSAPrivateKey) getPersistedPrivateKey();
        assertEquals(publicKey.getModulus().bitLength(), expectedKeySize);
        assertEquals(privateKey.getModulus(), publicKey.getModulus());
        assertSelfSignedCertificate(certificate, now);
    }

    @DataProvider(name = "invalidKeyConfigurations")
    public Object[][] invalidKeyConfigurations() {

        return new Object[][]{
                {KEY_SIZE_CONFIG, "1024", "Invalid key size '1024'"},
                {KEY_SIZE_CONFIG, "2047", "Invalid key size '2047'"},
                {KEY_SIZE_CONFIG, "3000", "Invalid key size '3000'"},
                {KEY_SIZE_CONFIG, "abc", "Invalid key size 'abc'"},
                {KEY_SIZE_CONFIG, "16384", "Invalid key size '16384'"},
                {KEY_ALGORITHM_CONFIG, "EC", "Unsupported key algorithm 'EC'"},
                {KEY_ALGORITHM_CONFIG, "DSA", "Unsupported key algorithm 'DSA'"}
        };
    }

    @Test(description = "Test that an invalid key configuration fails without persisting a context keystore.",
            dataProvider = "invalidKeyConfigurations")
    public void testGenerateContextKeyStoreWithInvalidKeyConfiguration(String configKey, String configValue,
                                                                       String expectedMessage) throws Exception {

        Map<String, String> configuration = new HashMap<>();
        configuration.put(configKey, configValue);

        KeyStoreManagementException e = expectThrows(KeyStoreManagementException.class,
                () -> generateContextKeyStore(configuration));
        assertTrue(e.getMessage().contains(expectedMessage), e.getMessage());
        assertNull(persistedKeyStore);
        verify(keyStoreManager, never()).addKeyStore(any(byte[].class), anyString(), any(char[].class),
                anyString(), anyString(), any(char[].class));
    }

    @DataProvider(name = "signatureAlgorithms")
    public Object[][] signatureAlgorithms() {

        return new Object[][]{
                {null, "MD5withRSA"},
                {"", "MD5withRSA"},
                {"unknown", "MD5withRSA"},
                {"SHA256withRSA", "SHA256withRSA"},
                {"SHA384withRSA", "SHA384withRSA"},
                {"sha512withrsa", "SHA512withRSA"},
                {"MD5withRSA", "MD5withRSA"},
                {"SHA1withRSA", "SHA1withRSA"}
        };
    }

    @Test(description = "Test the signature algorithm of the context keystore certificate.",
            dataProvider = "signatureAlgorithms")
    public void testGenerateContextKeyStoreSignatureAlgorithm(String configuredAlgorithm, String expectedAlgorithm)
            throws Exception {

        Map<String, String> configuration = new HashMap<>();
        configuration.put(SIGNING_ALG_CONFIG, configuredAlgorithm);
        generateContextKeyStore(configuration);

        X509Certificate certificate = getPersistedCertificate();
        assertTrue(expectedAlgorithm.equalsIgnoreCase(certificate.getSigAlgName()),
                "Expected " + expectedAlgorithm + " but was " + certificate.getSigAlgName());
        certificate.verify(certificate.getPublicKey());
    }

    /**
     * Generates a context keystore with the real {@link KeystoreUtils} and the given server configuration, and
     * captures the keystore passed to {@link KeyStoreManager} for persistence.
     *
     * @param configuration Server configuration properties.
     * @throws Exception If the keystore generation fails.
     */
    private void generateContextKeyStore(Map<String, String> configuration) throws Exception {

        // Lenient, since other server configuration properties are also read while generating the keystore.
        ServerConfiguration serverConfiguration =
                mock(ServerConfiguration.class, withSettings().strictness(Strictness.LENIENT));
        configuration.forEach((key, value) -> when(serverConfiguration.getFirstProperty(key)).thenReturn(value));

        try (MockedStatic<KeyStoreManager> keyStoreManagerStatic = mockStatic(KeyStoreManager.class);
             MockedStatic<CarbonUtils> carbonUtils = mockStatic(CarbonUtils.class);
             MockedStatic<ServerConfiguration> serverConfigurationStatic = mockStatic(ServerConfiguration.class);
             MockedStatic<CryptoUtil> cryptoUtil = mockStatic(CryptoUtil.class)) {

            carbonUtils.when(CarbonUtils::getServerConfiguration).thenReturn(serverConfiguration);
            serverConfigurationStatic.when(ServerConfiguration::getInstance).thenReturn(serverConfiguration);
            keyStoreManagerStatic.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(keyStoreManager);
            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(1);
            identityTenantUtil.when(() -> IdentityTenantUtil.initializeRegistry(anyInt()))
                    .thenAnswer((Answer<Void>) invocation -> null);
            when(keyStoreManager.getKeyStore(CONTEXT_KEYSTORE_NAME)).thenThrow(
                    new SecurityException("Key Store with a name: " + CONTEXT_KEYSTORE_NAME + " does not exist."));
            // Lenient, since the keystore is not persisted when the key configuration is invalid.
            lenient().doAnswer(invocation -> {
                persistedKeyStore = new PersistedKeyStore(invocation.getArgument(0), invocation.getArgument(2),
                        invocation.getArgument(4));
                return null;
            }).when(keyStoreManager).addKeyStore(any(byte[].class), eq(CONTEXT_KEYSTORE_NAME), any(char[].class),
                    anyString(), anyString(), any(char[].class));

            new IdentityKeyStoreGeneratorImpl().generateKeyStore(TENANT_DOMAIN, CONTEXT);
        }
    }

    private void assertSelfSignedCertificate(X509Certificate certificate, long generatedAt) throws Exception {

        assertEquals(certificate.getSubjectX500Principal(), certificate.getIssuerX500Principal());
        assertTrue(certificate.getSubjectX500Principal().getName().contains("CN=" + CONTEXT_KEY_ALIAS));
        certificate.verify(certificate.getPublicKey());
        assertWithinTolerance(certificate.getNotBefore().getTime(), generatedAt - CERT_NOT_BEFORE_OFFSET);
        assertWithinTolerance(certificate.getNotAfter().getTime(), generatedAt + CERT_NOT_AFTER_OFFSET);
    }

    private void assertWithinTolerance(long actual, long expected) {

        assertTrue(Math.abs(actual - expected) < TIME_TOLERANCE,
                "Expected a time close to " + expected + " but was " + actual);
    }

    private KeyStore loadPersistedKeyStore() throws Exception {

        assertNotNull(persistedKeyStore, "The context keystore was not persisted.");
        KeyStore keyStore = KeyStore.getInstance(persistedKeyStore.type);
        keyStore.load(new ByteArrayInputStream(persistedKeyStore.content), persistedKeyStore.password);
        return keyStore;
    }

    private X509Certificate getPersistedCertificate() throws Exception {

        return (X509Certificate) loadPersistedKeyStore().getCertificate(CONTEXT_KEY_ALIAS);
    }

    private java.security.Key getPersistedPrivateKey() throws Exception {

        return loadPersistedKeyStore().getKey(CONTEXT_KEY_ALIAS, persistedKeyStore.password);
    }

    /**
     * Keystore content and password passed to {@link KeyStoreManager} for persistence.
     */
    private static class PersistedKeyStore {

        private final byte[] content;
        private final char[] password;
        private final String type;

        PersistedKeyStore(byte[] content, char[] password, String type) {

            this.content = content;
            this.password = password;
            this.type = type;
        }
    }

    private Path createPath(String keystoreName) {

        return Paths.get(System.getProperty(CarbonBaseConstants.CARBON_HOME), "repository",
                "resources", "security", keystoreName);
    }

    private KeyStore getKeyStoreFromFile(String keystoreName, String password) throws Exception {

        Path tenantKeystorePath = createPath(keystoreName);
        FileInputStream file = new FileInputStream(tenantKeystorePath.toString());
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(file, password.toCharArray());
        return keystore;
    }
}
