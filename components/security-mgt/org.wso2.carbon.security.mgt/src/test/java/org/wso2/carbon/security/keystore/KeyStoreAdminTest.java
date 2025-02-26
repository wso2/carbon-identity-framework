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

import org.apache.geronimo.mail.util.Base64;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.security.KeyStoreMetadata;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.keystore.service.CertData;
import org.wso2.carbon.security.keystore.service.KeyStoreData;
import org.wso2.carbon.security.keystore.service.PaginatedKeyStoreData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

@Listeners(MockitoTestNGListener.class)
public class KeyStoreAdminTest extends IdentityBaseTest {

    private static final String KEYSTORE_NAME = "wso2carbon.jks";
    private static final String KEYSTORE_TYPE = "JKS";
    private static final String KEYSTORE_PASSWORD = "wso2carbon";
    private static final String KEYSTORE_ALIAS = "wso2carbon";
    @Mock
    private ServerConfiguration serverConfiguration;
    @Mock
    private KeyStoreManager keyStoreManager;
    @Mock
    private CryptoUtil cryptoUtil;
    @Mock
    private Resource resource;
    private KeyStoreAdmin keyStoreAdmin;
    private final int tenantID = -1234;

    @BeforeClass
    public void setup() throws Exception {

        System.setProperty(
                CarbonBaseConstants.CARBON_HOME,
                Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString()
                          );
    }

    @Test(description = "Add KeyStore test")
    public void testAddKeyStore() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME).toString());

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);

            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(any())).thenReturn(false);
            keyStoreUtil.when(() -> KeyStoreUtil.isTrustStore(any())).thenReturn(false);

            keyStoreAdmin = new KeyStoreAdmin(tenantID);
            keyStoreAdmin.addKeyStore(keyStoreContent, "new_keystore.jks", KEYSTORE_PASSWORD, " ", "JKS",
                    KEYSTORE_PASSWORD);
        }
    }

    @Test(description = "Add TrustStore test")
    public void testAddTrustStore() throws Exception {

        byte[] keyStoreContent = readBytesFromFile(createPath(KEYSTORE_NAME).toString());

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);

            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(any())).thenReturn(false);
            keyStoreUtil.when(() -> KeyStoreUtil.isTrustStore(any())).thenReturn(true);

            keyStoreAdmin = new KeyStoreAdmin(tenantID);
            keyStoreAdmin.addTrustStore(Base64.encode(keyStoreContent), "new_truststore.jks", KEYSTORE_PASSWORD, " ",
                    "JKS");
        }
    }

    @Test(description = "Test case to verify successful retrieval of TrustStore")
    public void testGetTrustStoreSuccess() throws Exception {

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(tenantID)).thenReturn(this.keyStoreManager);
            when(this.keyStoreManager.getTrustStore())
                    .thenReturn(getKeyStoreFromFile(KEYSTORE_NAME, KEYSTORE_PASSWORD));

            keyStoreAdmin = new KeyStoreAdmin(tenantID);
            KeyStore result = keyStoreAdmin.getTrustStore();

            assertNotNull(result);
        }
    }

    @Test(description = "Test case to verify that an exception is thrown when getting TrustStore")
    public void testGetTrustStoreException() throws Exception {

        // Mock ServerConfiguration static calls
        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);
            when(this.keyStoreManager.getTrustStore())
                    .thenThrow(new CarbonException("Error occurred while retrieving TrustStore"));

            keyStoreAdmin = new KeyStoreAdmin(tenantID);
            // Execute method under test
            assertThrows(SecurityConfigException.class, () -> {
                keyStoreAdmin.getTrustStore();
            });
        }
    }

    @Test(description = "Delete KeyStore test", dependsOnMethods = "testAddKeyStore")
    public void testDeleteKeyStore() throws Exception {

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);

            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(anyString())).thenReturn(false);
            keyStoreUtil.when(() -> KeyStoreUtil.isTrustStore(anyString())).thenReturn(false);

            keyStoreAdmin = new KeyStoreAdmin(tenantID);
            keyStoreAdmin.deleteStore("new_keystore.jks");
            verify(this.keyStoreManager).deleteStore("new_keystore.jks");
        }
    }

    @Test(description = "Delete TrustStore test", dependsOnMethods = "testAddTrustStore")
    public void testDeleteTrustStore() throws Exception {

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);

            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(anyString())).thenReturn(false);
            keyStoreUtil.when(() -> KeyStoreUtil.isTrustStore(anyString())).thenReturn(false);

            keyStoreAdmin = new KeyStoreAdmin(tenantID);
            keyStoreAdmin.deleteStore("new_truststore.jks");
            verify(this.keyStoreManager).deleteStore("new_truststore.jks");
        }
    }

    @Test
    public void testGetPaginatedKeystoreInfo() throws Exception {

        try (MockedStatic<ServerConfiguration> serverConfiguration = mockStatic(ServerConfiguration.class);
             MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {

            serverConfiguration.when(ServerConfiguration::getInstance).thenReturn(this.serverConfiguration);

            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(any())).thenReturn(true);

            keyStoreManager.when(() -> KeyStoreManager.getInstance(tenantID)).thenReturn(this.keyStoreManager);
            when(this.keyStoreManager.getKeyStore(anyString()))
                    .thenReturn(getKeyStoreFromFile(KEYSTORE_NAME, KEYSTORE_PASSWORD));

            keyStoreAdmin = new KeyStoreAdmin(tenantID);
            PaginatedKeyStoreData result = keyStoreAdmin.getPaginatedKeystoreInfo(KEYSTORE_NAME, 10);
            int actualKeysNo = findCertDataSetSize(result.getPaginatedKeyData().getCertDataSet());
            assertEquals(actualKeysNo, 3, "Incorrect key numbers");
        }
    }

    @Test
    public void testGetFilteredPaginatedKeystoreInfo() throws Exception {

        try (MockedStatic<ServerConfiguration> serverConfigurationMockedStatic = mockStatic(ServerConfiguration.class);
             MockedStatic<KeyStoreManager> keyStoreManagerMockedStatic = mockStatic(KeyStoreManager.class);
             MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {

            serverConfigurationMockedStatic.when(ServerConfiguration::getInstance).thenReturn(serverConfiguration);
            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(any())).thenReturn(true);

            keyStoreManagerMockedStatic.when(() -> KeyStoreManager.getInstance(tenantID)).thenReturn(keyStoreManager);
            when(keyStoreManager.getKeyStore(anyString()))
                    .thenReturn(getKeyStoreFromFile(KEYSTORE_NAME, KEYSTORE_PASSWORD));

            keyStoreAdmin = new KeyStoreAdmin(tenantID);
            PaginatedKeyStoreData result =
                    keyStoreAdmin.getFilteredPaginatedKeyStoreInfo(KEYSTORE_NAME, 10, KEYSTORE_ALIAS);
            int actualKeysNo = findCertDataSetSize(result.getPaginatedKeyData().getCertDataSet());
            assertEquals(actualKeysNo, 1, "Incorrect key numbers");
        }
    }

    @Test
    public void testGetKeystoresInfo() throws Exception {

        try (MockedStatic<ServerConfiguration> serverConfiguration = mockStatic(ServerConfiguration.class);
             MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class);
             MockedStatic<KeyStoreUtil> keyStoreUtil = mockStatic(KeyStoreUtil.class)) {

            serverConfiguration.when(ServerConfiguration::getInstance).thenReturn(this.serverConfiguration);
            keyStoreUtil.when(() -> KeyStoreUtil.isPrimaryStore(any())).thenReturn(false);
            keyStoreUtil.when(() -> KeyStoreUtil.isTrustStore(any())).thenReturn(false);

            keyStoreManager.when(() -> KeyStoreManager.getInstance(tenantID)).thenReturn(this.keyStoreManager);
            when(this.keyStoreManager.getKeyStore(anyString()))
                    .thenReturn(getKeyStoreFromFile(KEYSTORE_NAME, KEYSTORE_PASSWORD));
            when(this.keyStoreManager.getPrivateKey(anyString(), anyString()))
                    .thenReturn(getPrivateKeyFromKeyStore(KEYSTORE_NAME, KEYSTORE_ALIAS, KEYSTORE_PASSWORD));

            keyStoreAdmin = new KeyStoreAdmin(tenantID);
            KeyStoreData keystoreInfo = keyStoreAdmin.getKeystoreInfo(KEYSTORE_NAME);
            assertEquals(keystoreInfo.getKeyStoreName(), KEYSTORE_NAME, "Incorrect keystore name");
            assertEquals(keystoreInfo.getKeyStoreType(), KEYSTORE_TYPE, "Incorrect keystore type");
            assertNotNull(keystoreInfo.getKeyValue());
        }
    }

    @DataProvider(name = "testGetKeyStoreMetadataDataProvider")
    public Object[][] testGetKeyStoreMetadataDataProvider() {

        return new Object[][]{
                {true, getPrimaryKeyStoreMetadata()},
                {false, getTenantKeyStoreMetadata()}
        };
    }

    @Test(dataProvider = "testGetKeyStoreMetadataDataProvider",
            description = "Test case to verify successful retrieval keystore metadata")
    public void testGetKeyStoreMetadata(boolean isSuperTenant, KeyStoreMetadata keyStoreMetadata)
            throws SecurityConfigException {

        List<KeyStoreMetadata> metadataList = new ArrayList<>();
        metadataList.add(keyStoreMetadata);
        try (MockedStatic<KeyStoreManager> keyStoreManagerMockedStatic = mockStatic(KeyStoreManager.class)) {

            keyStoreManagerMockedStatic.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(keyStoreManager);
            when(keyStoreManager.getKeyStoresMetadata(anyBoolean())).thenReturn(
                    metadataList.toArray(new KeyStoreMetadata[0]));

            keyStoreAdmin = new KeyStoreAdmin(tenantID);
            KeyStoreData[] keyStores = keyStoreAdmin.getKeyStores(isSuperTenant);

            assertEquals(keyStores.length, 1, "Incorrect number of keystores");
            assertEquals(keyStores[0].getKeyStoreName(), keyStoreMetadata.getKeyStoreName());
            assertEquals(keyStores[0].getKeyStoreType(), keyStoreMetadata.getKeyStoreType());
            assertEquals(keyStores[0].getProvider(), keyStoreMetadata.getProvider());
            assertEquals(keyStores[0].getPrivateStore(), keyStoreMetadata.isPrivateStore());
        }
    }

    @Test
    public void testGetKeystoreEntries() throws Exception {

        try (MockedStatic<KeyStoreManager> keyStoreManager = mockStatic(KeyStoreManager.class)) {

            keyStoreManager.when(() -> KeyStoreManager.getInstance(anyInt())).thenReturn(this.keyStoreManager);
            when(this.keyStoreManager.getKeyStore(KEYSTORE_NAME))
                    .thenReturn(getKeyStoreFromFile(KEYSTORE_NAME, KEYSTORE_PASSWORD));
            keyStoreAdmin = new KeyStoreAdmin(tenantID);
            String[] names = keyStoreAdmin.getStoreEntries(KEYSTORE_NAME);
            assertEquals(names.length, 38, "Incorrect key numbers");
        }
    }

    private KeyStoreMetadata getPrimaryKeyStoreMetadata() {

        String name = createPath(KEYSTORE_NAME).toString();
        KeyStoreMetadata primaryKeyStoreMetadata = new KeyStoreMetadata();
        primaryKeyStoreMetadata.setKeyStoreName(name);
        primaryKeyStoreMetadata.setKeyStoreType(KEYSTORE_TYPE);
        primaryKeyStoreMetadata.setProvider(" ");
        primaryKeyStoreMetadata.setPrivateStore(true);
        return primaryKeyStoreMetadata;
    }

    private KeyStoreMetadata getTenantKeyStoreMetadata() {

        KeyStoreMetadata TenantKeyStoreMetadata = new KeyStoreMetadata();
        TenantKeyStoreMetadata.setKeyStoreName(KEYSTORE_NAME);
        TenantKeyStoreMetadata.setKeyStoreType(KEYSTORE_TYPE);
        TenantKeyStoreMetadata.setProvider(" ");
        TenantKeyStoreMetadata.setPrivateStore(true);
        TenantKeyStoreMetadata.setPublicCertId("12345");
        TenantKeyStoreMetadata.setPublicCert("publicCert".getBytes(StandardCharsets.UTF_8));
        return TenantKeyStoreMetadata;
    }

    private KeyStore getKeyStoreFromFile(String keystoreName, String password) throws Exception {

        Path tenantKeystorePath = createPath(keystoreName);
        FileInputStream file = new FileInputStream(tenantKeystorePath.toString());
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(file, password.toCharArray());
        return keystore;
    }

    private Key getPrivateKeyFromKeyStore(String keystoreName, String alias, String password) throws Exception {

        Path tenantKeystorePath = createPath(keystoreName);
        FileInputStream file = new FileInputStream(tenantKeystorePath.toString());
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(file, password.toCharArray());
        return keystore.getKey(alias, password.toCharArray());
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

    private byte[] readBytesFromFile(String filePath) throws IOException {

        File file = new File(filePath);
        byte[] bytes = new byte[(int) file.length()];

        try (InputStream inputStream = new FileInputStream(file)) {
            int bytesRead = 0;
            while (bytesRead < bytes.length) {
                int read = inputStream.read(bytes, bytesRead, bytes.length - bytesRead);
                if (read == -1) break; // End of file reached
                bytesRead += read;
            }
        }
        return bytes;
    }
}
