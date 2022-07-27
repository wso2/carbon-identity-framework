/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.secret.mgt.core;

import org.apache.commons.codec.Charsets;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.secret.mgt.core.dao.SecretDAO;
import org.wso2.carbon.identity.secret.mgt.core.dao.impl.SecretDAOImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementClientException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementServerException;
import org.wso2.carbon.identity.secret.mgt.core.internal.SecretManagerComponentDataHolder;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.identity.secret.mgt.core.model.Secrets;
import org.wso2.carbon.identity.secret.mgt.core.util.TestUtils;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;
import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.getSampleSecretAdd;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.getSampleSecretTypeAdd;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.spyConnection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class, CryptoUtil.class})
public class SecretManagerTest extends PowerMockTestCase {

    private SecretManager secretManager;
    private Connection connection;
    private CryptoUtil cryptoUtil;
    private SecretResolveManager secretResolveManager;

    private static final String SAMPLE_SECRET_NAME1 = "sample-secret1";
    private static final String SAMPLE_SECRET_NAME2 = "sample-secret2";
    private static final String SAMPLE_SECRET_VALUE1 = "dummy_value1";
    private static final String SAMPLE_SECRET_VALUE2 = "dummy_value2";
    private static final String SAMPLE_SECRET_ID = "ab123456";
    private static final String ENCRYPTED_VALUE1 = "dummy_encrypted1";

    private static final String SAMPLE_SECRET_TYPE_NAME1 = "sample-secret-type1";
    private static final String SAMPLE_SECRET_TYPE_DESCRIPTION1 = "sample-description1";
    private static final String SAMPLE_SECRET_TYPE_DESCRIPTION2 = "sample-description2";

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        DataSource dataSource = mock(DataSource.class);
        mockStatic(IdentityDatabaseUtil.class);
        when(IdentityDatabaseUtil.getDataSource()).thenReturn(dataSource);

        connection = TestUtils.getConnection();
        Connection spyConnection = spyConnection(connection);
        when(dataSource.getConnection()).thenReturn(spyConnection);

        prepareConfigs();
        SecretManagerComponentDataHolder.getInstance().setSecretManagementEnabled(true);

        mockStatic(CryptoUtil.class);
        cryptoUtil = mock(CryptoUtil.class);
        when(CryptoUtil.getDefaultCryptoUtil()).thenReturn(cryptoUtil);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        closeH2Base();
    }

    @Test(priority = 1)
    public void testAddSecretType() throws Exception {

        SecretType secretTypeAdd = getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1);

        SecretType secretType = secretManager.addSecretType(secretTypeAdd);
        assertNotNull(secretType.getId(), "Created secret type id cannot be null");
    }

    @Test(priority = 2, expectedExceptions = SecretManagementClientException.class)
    public void testAddDuplicateResourceType() throws Exception {

        SecretType secretTypeAdd = getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1);

        secretManager.addSecretType(secretTypeAdd);
        secretManager.addSecretType(secretTypeAdd);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 3, expectedExceptions = SecretManagementClientException.class)
    public void testReplaceNonExistingSecretType() throws Exception {

        SecretType secretTypeAdd = getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1);
        secretManager.replaceSecretType(secretTypeAdd);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 4)
    public void testReplaceExistingSecretType() throws Exception {

        SecretType secretTypeAdd1 = getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1);
        SecretType secretTypeCreated = secretManager.addSecretType(secretTypeAdd1);
        SecretType secretTypeAdd2 = getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION2);
        SecretType secretTypeReplaced = secretManager.replaceSecretType(secretTypeAdd2);

        assertEquals(secretTypeCreated.getId(),
                secretTypeReplaced.getId(), "Existing id should be equal to the replaced id");
    }

    @Test(priority = 5, expectedExceptions = SecretManagementClientException.class)
    public void testGetNonExistingSecretType() throws Exception {

        secretManager.getSecretType(SAMPLE_SECRET_TYPE_NAME1);

        org.junit.Assert.fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 6)
    public void testGetExistingSecretType() throws Exception {

        SecretType secretTypeCreated = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        SecretType secretTypeRetrieved = secretManager.getSecretType(SAMPLE_SECRET_TYPE_NAME1);

        assertEquals(secretTypeCreated.getId(), secretTypeRetrieved.getId(), "Existing id should be equal to the retrieved id");
    }

    @Test(priority = 7, expectedExceptions = SecretManagementClientException.class)
    public void testDeleteNonExistingSecretType() throws Exception {

        secretManager.deleteSecretType(SAMPLE_SECRET_TYPE_NAME1);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 8, expectedExceptions = SecretManagementClientException.class)
    public void testDeleteExistingSecretType() throws Exception {

        secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        secretManager.deleteSecretType(SAMPLE_SECRET_TYPE_NAME1);
        secretManager.getSecretType(SAMPLE_SECRET_TYPE_NAME1);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 9)
    public void testAddSecret() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        Secret secret = secretManager.addSecret(secretType.getName(), secretAdd);
        assertNotNull(secret.getSecretId(), "Created secret type id cannot be null");
        assertEquals(secretAdd.getSecretName(), secret.getSecretName());
    }

    @Test(priority = 10, expectedExceptions = SecretManagementClientException.class)
    public void testAddDuplicateSecret() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        secretManager.addSecret(secretType.getName(), secretAdd);
        secretManager.addSecret(secretType.getName(), secretAdd);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 11, expectedExceptions = SecretManagementServerException.class)
    public void testSecretWithEncryptionError() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        when(cryptoUtil.encryptAndBase64Encode(secretAdd.getSecretValue().getBytes())).thenThrow(new CryptoException());
        secretManager.addSecret(secretType.getName(), secretAdd);

        fail("Expected: " + SecretManagementServerException.class.getName());
    }

    @Test(priority = 12, expectedExceptions = SecretManagementClientException.class)
    public void testReplaceNonExistingSecret() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        secretManager.replaceSecret(secretType.getName(), secretAdd);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 13)
    public void testReplaceExistingSecret() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        Secret secretCreated = secretManager.addSecret(secretType.getName(), secretAdd);
        Secret secretUpdated = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE2);
        encryptSecret(secretUpdated.getSecretValue());
        Secret secretReplaced = secretManager.replaceSecret(secretType.getName(), secretUpdated);

        assertNotEquals("Created time should be different from the last updated time",
                secretReplaced.getCreatedTime(), secretReplaced.getLastModified());
        assertEquals( secretCreated.getSecretId(), secretReplaced.getSecretId(),
                "Existing id should be equal to the replaced id");
    }

    @Test(priority = 14, expectedExceptions = SecretManagementClientException.class)
    public void testGetNonExistingSecret() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        secretManager.getSecret(secretType.getName(), SAMPLE_SECRET_NAME1);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 15)
    public void testGetExistingSecret() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        Secret secretCreated = secretManager.addSecret(secretType.getName(), secretAdd);
        Secret secretRetrieved = secretManager.getSecret(secretType.getName(), SAMPLE_SECRET_NAME1);

        assertEquals(secretCreated.getSecretId(), secretRetrieved.getSecretId(), "Existing id should be equal " +
                "to the retrieved id");
    }

    @Test(priority = 16)
    public void testGetExistingSecretById() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        Secret secretCreated = secretManager.addSecret(secretType.getName(), secretAdd);
        Secret secretRetrieved = secretManager.getSecretById(secretCreated.getSecretId());

        assertEquals(secretCreated.getSecretName(), secretRetrieved.getSecretName(), "Existing id should be" +
                " equal to the retrieved id");
    }

    @Test(priority = 17, expectedExceptions = SecretManagementClientException.class)
    public void testGetNonExistingSecretById() throws Exception {

        secretManager.getSecretById( SAMPLE_SECRET_ID);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 18, expectedExceptions = SecretManagementClientException.class)
    public void testDeleteNonExistingSecret() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        secretManager.deleteSecret(secretType.getName(), SAMPLE_SECRET_NAME1);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }


    @Test(priority = 19, expectedExceptions = SecretManagementClientException.class)
    public void testDeleteExistingSecret() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        secretManager.addSecret(secretType.getName(), secretAdd);
        secretManager.deleteSecret(secretType.getName(), SAMPLE_SECRET_NAME1);
        secretManager.getSecret(secretType.getName(), SAMPLE_SECRET_NAME1);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }


    @Test(priority = 20, expectedExceptions = SecretManagementClientException.class)
    public void testDeleteNonExistingSecretById() throws Exception {

        secretManager.deleteSecretById(SAMPLE_SECRET_ID);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 21, expectedExceptions = SecretManagementClientException.class)
    public void testDeleteExistingSecretById() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        Secret secret = secretManager.addSecret(secretType.getName(), secretAdd);
        secretManager.deleteSecretById(secret.getSecretId());
        secretManager.getSecret(secretType.getName(), secret.getSecretName());

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 22)
    public void testGetSecrets() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd1 = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd1.getSecretValue());
        Secret secret1 = secretManager.addSecret(secretType.getName(), secretAdd1);

        Secret secretAdd2 = getSampleSecretAdd(SAMPLE_SECRET_NAME2, SAMPLE_SECRET_VALUE2);
        encryptSecret(secretAdd2.getSecretValue());
        Secret secret2 = secretManager.addSecret(secretType.getName(), secretAdd2);

        Secrets secrets = secretManager.getSecrets(secretType.getName());
        Assert.assertEquals(2, secrets.getSecrets().size(), "Retrieved secret count should be equal to the " +
                "added value");
        Assert.assertEquals(secret1.getSecretName(), secrets.getSecrets().get(0).getSecretName(),
                "Created secret name should be equal to the retrieved secret name");
        Assert.assertEquals(secret2.getSecretName(), secrets.getSecrets().get(1).getSecretName(),
                "Created secret name should be equal to the retrieved secret name");
    }

    @Test(priority = 23, expectedExceptions = SecretManagementClientException.class)
    public void testGetNonExistingSecretResolved() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        secretResolveManager.getResolvedSecret(secretType.getName(), SAMPLE_SECRET_NAME1);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 24)
    public void testGetExistingSecretResolved() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        Secret secretCreated = secretManager.addSecret(secretType.getName(), secretAdd);
        decryptSecret(ENCRYPTED_VALUE1);
        ResolvedSecret secretRetrieved = secretResolveManager.getResolvedSecret(secretType.getName(), SAMPLE_SECRET_NAME1);

        assertEquals(secretCreated.getSecretId(), secretRetrieved.getSecretId(), "Existing id should be equal " +
                "to the retrieved id");
        assertEquals(SAMPLE_SECRET_VALUE1, secretRetrieved.getResolvedSecretValue(), "Existing id should be equal " +
                "to the retrieved id");
    }

    @Test(priority = 25, expectedExceptions = SecretManagementServerException.class)
    public void testGetResolvedSecretWithDecryptError() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION1));

        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        secretManager.addSecret(secretType.getName(), secretAdd);
        when(cryptoUtil.base64DecodeAndDecrypt(ENCRYPTED_VALUE1)).thenThrow(new CryptoException());
        secretResolveManager.getResolvedSecret(secretType.getName(), SAMPLE_SECRET_NAME1);

        fail("Expected: " + SecretManagementServerException.class.getName());
    }

    @Test(priority = 26)
    public void testUpdateExistingSecretValue() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1,
                SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        Secret secretCreated = secretManager.addSecret(secretType.getName(), secretAdd);
        encryptSecret(SAMPLE_SECRET_VALUE2);
        Secret secretUpdated = secretManager.updateSecretValue(secretType.getName(), SAMPLE_SECRET_NAME1,
                SAMPLE_SECRET_VALUE2);

        assertNotEquals("Created time should be different from the last updated time",
                secretUpdated.getCreatedTime(), secretUpdated.getLastModified());
        assertEquals( secretCreated.getSecretId(), secretUpdated.getSecretId(),
                "Existing id should be equal to the replaced id");
    }

    @Test(priority = 27)
    public void testUpdateExistingSecretDescription() throws Exception {

        SecretType secretType = secretManager.addSecretType(getSampleSecretTypeAdd(SAMPLE_SECRET_TYPE_NAME1,
                SAMPLE_SECRET_TYPE_DESCRIPTION1));
        Secret secretAdd = getSampleSecretAdd(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secretAdd.getSecretValue());
        Secret secretCreated = secretManager.addSecret(secretType.getName(), secretAdd);
        Secret secretUpdated = secretManager.updateSecretDescription(secretType.getName(),
                SAMPLE_SECRET_NAME1, SAMPLE_SECRET_TYPE_DESCRIPTION2);

        assertNotEquals("Created time should be different from the last updated time",
                secretUpdated.getCreatedTime(), secretUpdated.getLastModified());
        assertEquals( secretCreated.getSecretId(), secretUpdated.getSecretId(),
                "Existing id should be equal to the replaced id");
    }

    private void prepareConfigs() {

        SecretDAO secretDAO = new SecretDAOImpl();
        SecretManagerComponentDataHolder.getInstance().setSecretDAOS(Collections.singletonList(secretDAO));
        mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);
        mockIdentityTenantUtility();
        secretManager = new SecretManagerImpl();
        secretResolveManager = new SecretResolveManagerImpl();
    }

    private void mockCarbonContextForTenant(int tenantId, String tenantDomain) {

        mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        when(privilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomain);
        when(privilegedCarbonContext.getTenantId()).thenReturn(tenantId);
        when(privilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockIdentityTenantUtility() {

        mockStatic(IdentityTenantUtil.class);
        IdentityTenantUtil identityTenantUtil = mock(IdentityTenantUtil.class);
        when(identityTenantUtil.getTenantDomain(any(Integer.class))).thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }

    private void encryptSecret(String secret) throws org.wso2.carbon.core.util.CryptoException {

        when(cryptoUtil.encryptAndBase64Encode(secret.getBytes(Charsets.UTF_8))).thenReturn(ENCRYPTED_VALUE1);
    }

    private void decryptSecret(String cipherText) throws org.wso2.carbon.core.util.CryptoException {

        when(cryptoUtil.base64DecodeAndDecrypt(cipherText)).thenReturn(SAMPLE_SECRET_VALUE1.getBytes());
    }
}
