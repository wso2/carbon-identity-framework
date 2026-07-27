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
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.*;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;
import org.wso2.carbon.security.keystore.KeyStoreManagementException;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Listeners(MockitoTestNGListener.class)
public class IdentityKeyStoreGeneratorImplTest extends IdentityBaseTest {

    private static final String KEYSTORE_PASSWORD = "wso2carbon";

    private IdentityKeyStoreGeneratorImpl identityKeyStoreGenerator;

    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    @Mock
    private KeyStoreManager keyStoreManager;

    @Mock
    private KeyStore mockKeyStore;


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
