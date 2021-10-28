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
import org.wso2.carbon.identity.secret.mgt.core.model.Secrets;
import org.wso2.carbon.identity.secret.mgt.core.util.TestUtils;

import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;

import javax.sql.DataSource;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.closeH2Base;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.getSampleSecret;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.identity.secret.mgt.core.util.TestUtils.spyConnection;

@PrepareForTest({PrivilegedCarbonContext.class, IdentityDatabaseUtil.class, IdentityUtil.class,
        IdentityTenantUtil.class, CryptoUtil.class})
public class SecretManagerTest extends PowerMockTestCase {

    private SecretManager secretManager;
    private Connection connection;
    private CryptoUtil cryptoUtil;
    private SecretResolveManager secretResolveManager;

    private static final String SAMPLE_SECRET_NAME1 = "sample-secret1";
    private static final String SAMPLE_SECRET_NAME2 = "sample-secret2";
    private static final String SAMPLE_INVALID_SECRET_NAME1 = "sample secret";
    private static final String SAMPLE_INVALID_SECRET_NAME2 = "123sampleSecret";
    private static final String SAMPLE_SECRET_VALUE1 = "dummy_value1";
    private static final String SAMPLE_SECRET_VALUE2 = "dummy_value2";
    private static final String SAMPLE_INVALID_SECRET_VALUE = "";
    private static final String SAMPLE_SECRET_ID = "ab123456";
    private static final String ENCRYPTED_VALUE1 = "dummy_encrypted1";

    private static final String SAMPLE_SECRET_TYPE_NAME1 = "adaptive-auth";
    private static final String SAMPLE_INVALID_SECRET_TYPE = "sample-secret-type";
    private static final String SAMPLE_SECRET_DESCRIPTION1 = "sample-description1";
    private static final String SAMPLE_SECRET_DESCRIPTION2 = "sample-description2";

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

    @Test(priority = 1, expectedExceptions = SecretManagementClientException.class)
    public void testAddExistingSecretType() throws Exception {

        secretManager.addSecretType(SAMPLE_SECRET_TYPE_NAME1);

        org.junit.Assert.fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 2)
    public void testAddSecret() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        Secret secretResponse = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        assertNotNull(secretResponse.getSecretId(), "Created secret type id cannot be null");
        assertEquals(secret.getSecretName(), secretResponse.getSecretName());
    }

    @Test(priority = 3, expectedExceptions = SecretManagementClientException.class)
    public void testAddDuplicateSecret() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 4, expectedExceptions = SecretManagementServerException.class)
    public void testSecretWithEncryptionError() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        when(cryptoUtil.encryptAndBase64Encode(secret.getSecretValue().getBytes())).thenThrow(new CryptoException());
        secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);

        fail("Expected: " + SecretManagementServerException.class.getName());
    }

    @Test(priority = 5, expectedExceptions = SecretManagementClientException.class)
    public void testReplaceNonExistingSecret() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        secretManager.replaceSecret(SAMPLE_SECRET_TYPE_NAME1, secret);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 6, expectedExceptions = SecretManagementClientException.class)
    public void testReplaceNonExistingSecretById() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        secret.setSecretId(SAMPLE_SECRET_ID);
        encryptSecret(secret.getSecretValue());
        secretManager.replaceSecretById(SAMPLE_SECRET_TYPE_NAME1, secret);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 7)
    public void testReplaceExistingSecret() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        Secret secretCreated = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        Secret secretUpdated = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE2);
        encryptSecret(secretUpdated.getSecretValue());
        Secret secretReplaced = secretManager.replaceSecret(SAMPLE_SECRET_TYPE_NAME1, secretUpdated);

        assertNotEquals("Created time should be different from the last updated time",
                secretReplaced.getCreatedTime(), secretReplaced.getLastModified());
        assertEquals(secretCreated.getSecretId(), secretReplaced.getSecretId(),
                "Existing id should be equal to the replaced id");
    }

    @Test(priority = 8)
    public void testReplaceExistingSecretById() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        Secret secretCreated = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        Secret secretUpdated = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE2);
        secretUpdated.setSecretId(secretCreated.getSecretId());
        encryptSecret(secretUpdated.getSecretValue());
        Secret secretReplaced = secretManager.replaceSecretById(SAMPLE_SECRET_TYPE_NAME1, secretUpdated);

        assertNotEquals("Created time should be different from the last updated time",
                secretReplaced.getCreatedTime(), secretReplaced.getLastModified());
        assertEquals(secretCreated.getSecretId(), secretReplaced.getSecretId(),
                "Existing id should be equal to the replaced id");
    }

    @Test(priority = 9, expectedExceptions = SecretManagementClientException.class)
    public void testGetNonExistingSecret() throws Exception {

        secretManager.getSecret(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_NAME1);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 10)
    public void testGetExistingSecret() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        Secret secretCreated = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        Secret secretRetrieved = secretManager.getSecret(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_NAME1);

        assertEquals(secretCreated.getSecretId(), secretRetrieved.getSecretId(),
                "Existing id should be equal " + "to the retrieved id");
    }

    @Test(priority = 11)
    public void testGetExistingSecretById() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        Secret secretCreated = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        Secret secretRetrieved = secretManager.getSecretById(SAMPLE_SECRET_TYPE_NAME1, secretCreated.getSecretId());

        assertEquals(secretCreated.getSecretName(), secretRetrieved.getSecretName(),
                "Existing id should be equal to the retrieved id");
    }

    @Test(priority = 12, expectedExceptions = SecretManagementClientException.class)
    public void testGetNonExistingSecretById() throws Exception {

        secretManager.getSecretById(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_ID);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 13, expectedExceptions = SecretManagementClientException.class)
    public void testDeleteNonExistingSecret() throws Exception {

        secretManager.deleteSecret(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_NAME1);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 14, expectedExceptions = SecretManagementClientException.class)
    public void testDeleteExistingSecret() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        secretManager.deleteSecret(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_NAME1);
        secretManager.getSecret(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_NAME1);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 15, expectedExceptions = SecretManagementClientException.class)
    public void testDeleteNonExistingSecretById() throws Exception {

        secretManager.deleteSecretById(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_ID);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 16, expectedExceptions = SecretManagementClientException.class)
    public void testDeleteExistingSecretById() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        Secret secretResponse = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        secretManager.deleteSecretById(SAMPLE_SECRET_TYPE_NAME1, secretResponse.getSecretId());
        secretManager.getSecret(SAMPLE_SECRET_TYPE_NAME1, secretResponse.getSecretName());

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 17)
    public void testGetSecrets() throws Exception {

        Secret secret1 = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret1.getSecretValue());
        Secret secretResponse1 = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret1);

        Secret secret2 = getSampleSecret(SAMPLE_SECRET_NAME2, SAMPLE_SECRET_VALUE2);
        encryptSecret(secret2.getSecretValue());
        Secret secretResponse2 = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret2);

        Secrets secrets = secretManager.getSecrets(SAMPLE_SECRET_TYPE_NAME1);
        Assert.assertEquals(2, secrets.getSecrets().size(),
                "Retrieved secret count should be equal to the added value");
        Assert.assertEquals(secretResponse1.getSecretName(), secrets.getSecrets().get(0).getSecretName(),
                "Created secret name should be equal to the retrieved secret name");
        Assert.assertEquals(secretResponse2.getSecretName(), secrets.getSecrets().get(1).getSecretName(),
                "Created secret name should be equal to the retrieved secret name");
    }

    @Test(priority = 18, expectedExceptions = SecretManagementClientException.class)
    public void testGetNonExistingSecretResolved() throws Exception {

        secretResolveManager.getResolvedSecret(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_NAME1);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 19)
    public void testGetExistingSecretResolved() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        Secret secretCreated = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        decryptSecret(ENCRYPTED_VALUE1);
        ResolvedSecret secretRetrieved = secretResolveManager
                .getResolvedSecret(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_NAME1);

        assertEquals(secretCreated.getSecretId(), secretRetrieved.getSecretId(),
                "Existing id should be equal to the retrieved id");
        assertEquals(SAMPLE_SECRET_VALUE1, secretRetrieved.getResolvedSecretValue(),
                "Existing id should be equal to the retrieved id");
    }

    @Test(priority = 20, expectedExceptions = SecretManagementServerException.class)
    public void testGetResolvedSecretWithDecryptError() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        when(cryptoUtil.base64DecodeAndDecrypt(ENCRYPTED_VALUE1)).thenThrow(new CryptoException());
        secretResolveManager.getResolvedSecret(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_NAME1);

        fail("Expected: " + SecretManagementServerException.class.getName());
    }

    @Test(priority = 21)
    public void testUpdateExistingSecretValue() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        Secret secretCreated = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        encryptSecret(SAMPLE_SECRET_VALUE2);
        Secret secretUpdated = secretManager.updateSecretValue(SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_NAME1,
                SAMPLE_SECRET_VALUE2);

        assertNotEquals("Created time should be different from the last updated time",
                secretUpdated.getCreatedTime(), secretUpdated.getLastModified());
        assertEquals(secretCreated.getSecretId(), secretUpdated.getSecretId(),
                "Existing id should be equal to the replaced id");
    }

    @Test(priority = 22)
    public void testUpdateExistingSecretValueById() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        Secret secretCreated = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        encryptSecret(SAMPLE_SECRET_VALUE2);
        Secret secretUpdated = secretManager
                .updateSecretValueById(SAMPLE_SECRET_TYPE_NAME1, secretCreated.getSecretId(), SAMPLE_SECRET_VALUE2);

        assertNotEquals("Created time should be different from the last updated time",
                secretUpdated.getCreatedTime(), secretUpdated.getLastModified());
        assertEquals(secretCreated.getSecretId(), secretUpdated.getSecretId(),
                "Existing id should be equal to the replaced id");
    }

    @Test(priority = 23)
    public void testUpdateExistingSecretDescription() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1, SAMPLE_SECRET_DESCRIPTION1);
        encryptSecret(secret.getSecretValue());
        Secret secretCreated = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        Secret secretUpdated = secretManager.updateSecretDescription(SAMPLE_SECRET_TYPE_NAME1,
                SAMPLE_SECRET_NAME1, SAMPLE_SECRET_DESCRIPTION2);

        assertNotEquals("Created time should be different from the last updated time",
                secretUpdated.getCreatedTime(), secretUpdated.getLastModified());
        assertNotEquals("Secret description should be different from the previous description",
                secretCreated.getDescription(), secretUpdated.getDescription());
        assertEquals(secretCreated.getSecretId(), secretUpdated.getSecretId(),
                "Existing id should be equal to the replaced id");
    }

    @Test(priority = 24)
    public void testUpdateExistingSecretDescriptionById() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1, SAMPLE_SECRET_DESCRIPTION1);
        encryptSecret(secret.getSecretValue());
        Secret secretCreated = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        Secret secretUpdated = secretManager.updateSecretDescriptionById(SAMPLE_SECRET_TYPE_NAME1,
                secretCreated.getSecretId(), SAMPLE_SECRET_DESCRIPTION2);

        assertNotEquals("Created time should be different from the last updated time",
                secretUpdated.getCreatedTime(), secretUpdated.getLastModified());
        assertNotEquals("Secret description should be different from the previous description",
                secretCreated.getDescription(), secretUpdated.getDescription());
        assertEquals(secretCreated.getSecretId(), secretUpdated.getSecretId(),
                "Existing id should be equal to the replaced id");
    }

    @Test(priority = 25, expectedExceptions = SecretManagementClientException.class)
    public void testAddSecretWithInvalidSecretNameRegexContainsSpaces() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_INVALID_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 26, expectedExceptions = SecretManagementClientException.class)
    public void testAddSecretWithInvalidSecretNameRegexStartsWithNumeric() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_INVALID_SECRET_NAME2, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 27, expectedExceptions = SecretManagementClientException.class)
    public void testAddSecretWithInvalidSecretValueRegex() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_INVALID_SECRET_VALUE);
        encryptSecret(secret.getSecretValue());
        secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 28, expectedExceptions = SecretManagementClientException.class)
    public void testUpdateSecretWithInvalidSecretValueRegex() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        encryptSecret(SAMPLE_INVALID_SECRET_VALUE);
        secretManager.updateSecretValue(
                SAMPLE_SECRET_TYPE_NAME1, SAMPLE_SECRET_NAME1, SAMPLE_INVALID_SECRET_VALUE);

        fail("Expected: " + SecretManagementClientException.class.getName());
    }

    @Test(priority = 29, expectedExceptions = SecretManagementClientException.class)
    public void testUpdateSecretByIdWithInvalidSecretValueRegex() throws Exception {

        Secret secret = getSampleSecret(SAMPLE_SECRET_NAME1, SAMPLE_SECRET_VALUE1);
        encryptSecret(secret.getSecretValue());
        Secret secretCreated = secretManager.addSecret(SAMPLE_SECRET_TYPE_NAME1, secret);
        encryptSecret(SAMPLE_INVALID_SECRET_VALUE);
        secretManager.updateSecretValueById(
                SAMPLE_SECRET_TYPE_NAME1, secretCreated.getSecretId(), SAMPLE_INVALID_SECRET_VALUE);

        fail("Expected: " + SecretManagementClientException.class.getName());
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
