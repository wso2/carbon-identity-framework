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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.Charsets;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
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

public class SecretManagerTest {

    private SecretManager secretManager;
    private Connection connection;
    private CryptoUtil mockCryptoUtil;
    private SecretResolveManager secretResolveManager;
    private SecretsProcessor<IdentityProvider> identityProviderSecretsProcessor;

    private static final String SAMPLE_SECRET_NAME1 = "sample-secret1";
    private static final String SAMPLE_SECRET_NAME2 = "sample-secret2";
    private static final String SAMPLE_SECRET_VALUE1 = "dummy_value1";
    private static final String SAMPLE_SECRET_VALUE2 = "dummy_value2";
    private static final String SAMPLE_SECRET_ID = "ab123456";
    private static final String ENCRYPTED_VALUE1 = "dummy_encrypted1";
    private static final String ENCRYPTED_VALUE2 = "dummy_encrypted2";

    private static final String SAMPLE_SECRET_TYPE_NAME1 = "sample-secret-type1";
    private static final String SAMPLE_SECRET_TYPE_DESCRIPTION1 = "sample-description1";
    private static final String SAMPLE_SECRET_TYPE_DESCRIPTION2 = "sample-description2";

    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private MockedStatic<CryptoUtil> cryptoUtil;
    MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext;
    MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        DataSource dataSource = mock(DataSource.class);
        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityDatabaseUtil.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);

        connection = TestUtils.getConnection();
        Connection spyConnection = spyConnection(connection);
        when(dataSource.getConnection()).thenReturn(spyConnection);

        prepareConfigs();
        SecretManagerComponentDataHolder.getInstance().setSecretManagementEnabled(true);

        cryptoUtil = mockStatic(CryptoUtil.class);
        this.mockCryptoUtil = mock(CryptoUtil.class);
        cryptoUtil.when(CryptoUtil::getDefaultCryptoUtil).thenReturn(this.mockCryptoUtil);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        connection.close();
        closeH2Base();
        identityDatabaseUtil.close();
        cryptoUtil.close();
        privilegedCarbonContext.close();
        identityTenantUtil.close();
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
        when(mockCryptoUtil.encryptAndBase64Encode(secretAdd.getSecretValue().getBytes())).thenThrow(new CryptoException());
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
        when(mockCryptoUtil.base64DecodeAndDecrypt(ENCRYPTED_VALUE1)).thenThrow(new CryptoException());
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

    @Test(priority = 28)
    public void testAddIdpSecrets() throws Exception {

        secretManager.addSecretType(getSampleSecretTypeAdd(
                SecretConstants.IDN_SECRET_TYPE_IDP_SECRETS, "Secret type of IDP related secret properties"
        ));
        IdentityProvider identityProvider = buildIDPObject();
        encryptSecret(SAMPLE_SECRET_VALUE1);
        identityProviderSecretsProcessor.encryptAssociatedSecrets(identityProvider);
        
        for (String secretName : buildSecretNamesList(identityProvider)) {
            assertTrue(
                    secretManager.isSecretExist(SecretConstants.IDN_SECRET_TYPE_IDP_SECRETS, secretName),
                    "Expected secret is not found in the data source.");
        }
    }

    @Test(priority = 29)
    public void testGetIdpSecrets() throws Exception {

        secretManager.addSecretType(getSampleSecretTypeAdd(
                SecretConstants.IDN_SECRET_TYPE_IDP_SECRETS, "Secret type of IDP related secret properties"
        ));
        IdentityProvider identityProvider = buildIDPObject();
        encryptSecret(SAMPLE_SECRET_VALUE1);
        IdentityProvider addedIdp = identityProviderSecretsProcessor.encryptAssociatedSecrets(identityProvider);

        decryptSecret(ENCRYPTED_VALUE1);
        IdentityProvider updatedIdp = identityProviderSecretsProcessor.decryptAssociatedSecrets(addedIdp);

        for (Property property : updatedIdp.getFederatedAuthenticatorConfigs()[0].getProperties()) {
            if (property.isConfidential()) {
                assertEquals(SAMPLE_SECRET_VALUE1, property.getValue());
            }
        }
    }

    @Test(priority = 30)
    public void testDeleteIdpSecrets() throws Exception {

        secretManager.addSecretType(getSampleSecretTypeAdd(
                SecretConstants.IDN_SECRET_TYPE_IDP_SECRETS, "Secret type of IDP related secret properties"
        ));
        IdentityProvider identityProvider = buildIDPObject();
        encryptSecret(SAMPLE_SECRET_VALUE1);
        IdentityProvider addedIdp = identityProviderSecretsProcessor.encryptAssociatedSecrets(identityProvider);

        identityProviderSecretsProcessor.deleteAssociatedSecrets(addedIdp);
        for (String secretName : buildSecretNamesList(identityProvider)) {
            assertFalse(
                    secretManager.isSecretExist(SecretConstants.IDN_SECRET_TYPE_IDP_SECRETS, secretName),
                    "Expected secret is still available after delete idp secret functionality is executed.");
        }
    }

    @Test(priority = 31)
    public void testUpdateIdpSecrets() throws Exception {

        secretManager.addSecretType(getSampleSecretTypeAdd(
                SecretConstants.IDN_SECRET_TYPE_IDP_SECRETS, "Secret type of IDP related secret properties"
        ));
        IdentityProvider identityProvider = buildIDPObject();
        encryptSecret(SAMPLE_SECRET_VALUE1);
        identityProviderSecretsProcessor.encryptAssociatedSecrets(identityProvider);

        IdentityProvider updatedIdpObject = buildUpdatedIdpObject(identityProvider);
        decryptSecret(ENCRYPTED_VALUE1);
        encryptSecret(SAMPLE_SECRET_VALUE2);
        identityProviderSecretsProcessor.encryptAssociatedSecrets(updatedIdpObject);

        decryptSecret(ENCRYPTED_VALUE2);
        IdentityProvider updatedIdp = identityProviderSecretsProcessor.decryptAssociatedSecrets(updatedIdpObject);

        for (Property property : updatedIdp.getFederatedAuthenticatorConfigs()[0].getProperties()) {
            if (property.isConfidential()) {
                assertEquals(SAMPLE_SECRET_VALUE2, property.getValue());
            }
        }
    }

    private void prepareConfigs() {

        SecretDAO secretDAO = new SecretDAOImpl();
        SecretManagerComponentDataHolder.getInstance().setSecretDAOS(Collections.singletonList(secretDAO));
        mockCarbonContextForTenant(SUPER_TENANT_ID, SUPER_TENANT_DOMAIN_NAME);
        mockIdentityTenantUtility();
        secretManager = new SecretManagerImpl();
        secretResolveManager = new SecretResolveManagerImpl();
        identityProviderSecretsProcessor = new IdPSecretsProcessor();
    }

    private void mockCarbonContextForTenant(int tenantId, String tenantDomain) {

        privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext mockPrivilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContext.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                .thenReturn(mockPrivilegedCarbonContext);
        when(mockPrivilegedCarbonContext.getTenantDomain()).thenReturn(tenantDomain);
        when(mockPrivilegedCarbonContext.getTenantId()).thenReturn(tenantId);
        when(mockPrivilegedCarbonContext.getUsername()).thenReturn("admin");
    }

    private void mockIdentityTenantUtility() {

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(any(Integer.class)))
                .thenReturn(SUPER_TENANT_DOMAIN_NAME);
    }

    private void encryptSecret(String secret) throws org.wso2.carbon.core.util.CryptoException {

        if (SAMPLE_SECRET_VALUE2.equals(secret)) {
            when(mockCryptoUtil.encryptAndBase64Encode(secret.getBytes(Charsets.UTF_8))).thenReturn(ENCRYPTED_VALUE2);
        } else {
            when(mockCryptoUtil.encryptAndBase64Encode(secret.getBytes(Charsets.UTF_8))).thenReturn(ENCRYPTED_VALUE1);
        }
    }

    private void decryptSecret(String cipherText) throws org.wso2.carbon.core.util.CryptoException {

        if (ENCRYPTED_VALUE2.equals(cipherText)) {
            when(mockCryptoUtil.base64DecodeAndDecrypt(cipherText)).thenReturn(SAMPLE_SECRET_VALUE2.getBytes());
        } else {
            when(mockCryptoUtil.base64DecodeAndDecrypt(cipherText)).thenReturn(SAMPLE_SECRET_VALUE1.getBytes());
        }
    }

    private IdentityProvider buildIDPObject() {

        IdentityProvider idp = new IdentityProvider();
        idp.setId("5");
        idp.setIdentityProviderName("testIdP1");
        idp.setEnable(true);
        idp.setPrimary(true);
        idp.setFederationHub(true);
        idp.setCertificate("");

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("OIDCAuthenticator");
        federatedAuthenticatorConfig.setName("SampleOIDCAuthenticator");
        federatedAuthenticatorConfig.setEnabled(true);
        Property property1 = new Property();
        property1.setName(SAMPLE_SECRET_NAME1);
        property1.setValue(SAMPLE_SECRET_VALUE1);
        property1.setConfidential(true);
        Property property2 = new Property();
        property2.setName("NonSecretProperty");
        property2.setValue("PropertyValue");
        property2.setConfidential(false);
        federatedAuthenticatorConfig.setProperties(new Property[]{property1, property2});
        idp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});

        return idp;
    }

    private IdentityProvider buildUpdatedIdpObject(IdentityProvider identityProvider) {

        for (Property property : identityProvider.getFederatedAuthenticatorConfigs()[0].getProperties()) {
            if (property.isConfidential()) {
                property.setValue(SAMPLE_SECRET_VALUE2);
            }
        }

        return identityProvider;
    }

    private List<String> buildSecretNamesList(IdentityProvider identityProvider) {

        List<String> secretNames = new ArrayList<>();
        identityProvider.getFederatedAuthenticatorConfigs();
        for (FederatedAuthenticatorConfig fedAuthConfig : identityProvider.getFederatedAuthenticatorConfigs()) {
            for (Property prop : fedAuthConfig.getProperties()) {
                if (prop.isConfidential()) {
                    String secretName =
                            buildSecretNameWithIdpObj(identityProvider.getId(), fedAuthConfig.getName(), prop.getName());
                    secretNames.add(secretName);
                }
            }
        }

        return secretNames;
    }

    private String buildSecretNameWithIdpObj(String idpId, String fedAuthName, String propName) {

        return idpId + ":" + fedAuthName + ":" + propName;
    }
}
