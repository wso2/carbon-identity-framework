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

package org.wso2.carbon.identity.user.store.configuration.utils;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigComponent;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@Listeners(MockitoTestNGListener.class)
public class SecondaryUserStoreConfiguratorTest {

    private static final String SERVER_KEYSTORE_KEY_ALIAS = "Security.KeyStore.KeyAlias";
    private static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";
    private static final String ENCRYPTION_KEYSTORE = "Security.UserStorePasswordEncryption";
    private static final String CRYPTO_PROVIDER = "CryptoService.InternalCryptoProviderClassName";;
    private static final String KEYSTORE_BASED_CRYPTO_PROVIDER = "org.wso2.carbon.crypto.provider." +
            "KeyStoreBasedInternalCryptoProvider";
    private static final String KEYSTORE_TYPE = "JKS";

    @Mock
    private ServerConfigurationService serverConfigurationService;
    private CryptoUtil cryptoUtil;
    @Mock
    private KeyStoreManager keystoreManager;

    private SecondaryUserStoreConfigurator configurator;

    @BeforeMethod
    void setUp() {
        System.setProperty(CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString());
        System.setProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY, "RSA");
        configurator = new SecondaryUserStoreConfigurator();
        cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
    }

    @Test
    void testEncryptPlainTextWithKeystore() throws Exception {

        try (MockedStatic<UserStoreConfigComponent> userStoreConfigComponent =
                     mockStatic(UserStoreConfigComponent.class);
             MockedStatic<KeyStoreManager> keystoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<CryptoUtil> cryptoUtil = mockStatic(CryptoUtil.class)) {

            userStoreConfigComponent.when(UserStoreConfigComponent::getServerConfigurationService)
                    .thenReturn(this.serverConfigurationService);

            // Mocking ServerConfigurationService responses
            when(serverConfigurationService.getFirstProperty(ENCRYPTION_KEYSTORE))
                    .thenReturn(null);
            when(serverConfigurationService.getFirstProperty(CRYPTO_PROVIDER))
                    .thenReturn(KEYSTORE_BASED_CRYPTO_PROVIDER);
            when(serverConfigurationService.getFirstProperty(SERVER_KEYSTORE_KEY_ALIAS))
                    .thenReturn("wso2carbon");

            keystoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keystoreManager);
            when(this.keystoreManager.getPrimaryKeyStore())
                    .thenReturn(getKeyStoreFromFile("wso2carbon.jks", "wso2carbon"));

            // Mocking CryptoUtil behavior
            cryptoUtil.when(CryptoUtil::getJCEProvider).thenReturn("SunJCE");
            cryptoUtil.when(CryptoUtil::getDefaultCryptoUtil).thenReturn(this.cryptoUtil);

            configurator.encryptPlainText("testPlainText");
        }
    }

    private KeyStore getKeyStoreFromFile(String keystoreName, String password) throws Exception {

        Path tenantKeystorePath = createPath(keystoreName);
        FileInputStream file = new FileInputStream(tenantKeystorePath.toString());
        KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
        keystore.load(file, password.toCharArray());
        return keystore;
    }

    private Path createPath(String keystoreName) {

        Path keystorePath = Paths.get(System.getProperty(CarbonBaseConstants.CARBON_HOME), "repository",
                "resources", "security", keystoreName);
        return keystorePath;
    }

}
