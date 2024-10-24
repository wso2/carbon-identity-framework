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

package org.wso2.carbon.identity.user.store.configuration.deployer.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.user.store.configuration.deployer.internal.UserStoreConfigComponent;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import javax.crypto.Cipher;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@Listeners(MockitoTestNGListener.class)
public class UserStoreUtilTest {

    private static final String KEYSTORE_NAME = "wso2carbon.jks";
    private static final String KEYSTORE_TYPE = "JKS";
    private static final String KEYSTORE_PASSWORD = "wso2carbon";

    @Mock
    private ServerConfiguration serverConfiguration;
    @Mock
    private Cipher cipher;
    @Mock
    private KeyStoreManager keyStoreManager;

    @BeforeClass
    public void setup() {

        System.setProperty(
                CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString()
                          );
    }

    @Test(description = "Test case for successful Cipher initialization.")
    public void testGetCipherOfSuperTenantSuccess() throws Exception {

        System.setProperty("org.wso2.CipherTransformation", "RSA/ECB/PKCS1Padding");

        try (MockedStatic<ServerConfiguration> serverConfiguration = mockStatic(ServerConfiguration.class);
             MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<UserStoreConfigComponent> userStoreConfigComponent = mockStatic(UserStoreConfigComponent.class)) {

            serverConfiguration.when(ServerConfiguration::getInstance).thenReturn(this.serverConfiguration);
            when(this.serverConfiguration.getFirstProperty("JCEProvider")).thenReturn("SunJCE");
            when(this.serverConfiguration.getFirstProperty("Security.KeyStore.KeyAlias")).thenReturn("wso2carbon");

            userStoreConfigComponent.when(UserStoreConfigComponent::getServerConfigurationService)
                    .thenReturn(this.serverConfiguration);

            keyStoreManager.when(() -> KeyStoreManager.getInstance(
                    MultitenantConstants.SUPER_TENANT_ID)).thenReturn(this.keyStoreManager);
            when(this.keyStoreManager.getPrimaryKeyStore())
                    .thenReturn(getKeyStoreFromFile(KEYSTORE_NAME, KEYSTORE_PASSWORD));

            UserStoreUtil.getCipherOfSuperTenant();
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

        Path keystorePath = Paths.get(System.getProperty(CarbonBaseConstants.CARBON_HOME), "security", keystoreName);
        return keystorePath;
    }

}
