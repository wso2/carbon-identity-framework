/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.security.keystore;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.security.keystore.service.CertData;
import org.wso2.carbon.security.keystore.service.PaginatedKeyStoreData;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Listeners(MockitoTestNGListener.class)
public class KeyStoreAdminTest extends IdentityBaseTest {

    public static final String SERVER_TRUSTSTORE_FILE = "Security.TrustStore.Location";
    public static final String SERVER_TRUSTSTORE_PASSWORD = "Security.TrustStore.Password";
    @Mock
    ServerConfiguration serverConfiguration;
    @Mock
    KeyStoreManager keyStoreManager;
    @Mock
    Registry registry;
    private KeyStoreAdmin keyStoreAdmin;
    private int tenantID = -1234;

    @BeforeClass
    public void setup() {

        System.setProperty(
                CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString()
        );
    }

    @Test
    public void testGetPaginatedKeystoreInfo() throws Exception {

        try (MockedStatic<ServerConfiguration> serverConfiguration = mockStatic(ServerConfiguration.class);
             MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {
            serverConfiguration.when(ServerConfiguration::getInstance).thenReturn(this.serverConfiguration);

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);
            when(this.serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_FILE)).thenReturn(createPath("wso2carbon.jks").toString());
            when(this.serverConfiguration.getFirstProperty(SERVER_TRUSTSTORE_PASSWORD)).thenReturn("wso2carbon");

            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(any())).thenReturn(true);

            keyStoreManager.when(() -> KeyStoreManager.getInstance(tenantID)).thenReturn(this.keyStoreManager);
            when(this.keyStoreManager.getPrimaryKeyStore()).thenReturn(getKeyStoreFromFile("wso2carbon.jks", "wso2carbon"));

            keyStoreAdmin = new KeyStoreAdmin(tenantID, registry);
            PaginatedKeyStoreData result = keyStoreAdmin.getPaginatedKeystoreInfo("wso2carbon.jks", 10);
            int actualKeysNo = findCertDataSetSize(result.getPaginatedKeyData().getCertDataSet());
            assertEquals(actualKeysNo, 3, "Incorrect key numbers");
        }


    }

    private KeyStore getKeyStoreFromFile(String keystoreName, String password) throws Exception {

        Path tenantKeystorePath = createPath(keystoreName);
        FileInputStream file = new FileInputStream(tenantKeystorePath.toString());
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(file, password.toCharArray());
        return keystore;
    }

    private Path createPath(String keystoreName) {

        Path keystorePath = Paths.get(System.getProperty(CarbonBaseConstants.CARBON_HOME), "repository",
                "resources", "security", keystoreName);
        return keystorePath;
    }

    private int findCertDataSetSize(CertData[] certDataSet) {

        int ans = 0;
        for (CertData cert : certDataSet) {
            if (cert != null) {
                ans += 1;
            }
        }
        return ans;
    }

}
