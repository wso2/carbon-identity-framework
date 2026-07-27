/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.idp.mgt.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.Charsets;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;


public class IdPSecretsProcessorTest {

    private SecretManagerImpl secretManager;
    private SecretResolveManager secretResolveManager;
    private IdPSecretsProcessor idpSecretsProcessor;
    private CryptoUtil mockCryptoUtil;
    private MockedStatic<CryptoUtil> cryptoUtil;
    private MockedStatic<IdentityUtil> identityUtil;

    private static final String SAMPLE_SECRET_NAME1 = "sample-secret1";
    private static final String SAMPLE_SECRET_VALUE1 = "dummy_value1";
    private static final String SAMPLE_SECRET_VALUE2 = "dummy_value2";
    private static final String ENCRYPTED_VALUE1 = "dummy_encrypted1";
    private static final String ENCRYPTED_VALUE2 = "dummy_encrypted2";

    private static final String fedId = "5";
    private static final String fedName = "SampleOIDCAuthenticator";
    private static final String provConnectorName = "scim2";
    private static final String PROV_SECRET_NAME = "clientSecret";


    @BeforeClass
    public void setUp() throws Exception {

        secretManager = mock(SecretManagerImpl.class);
        secretResolveManager = mock(SecretResolveManager.class);
        SecretType secretType = mock(SecretType.class);
        IdpMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        IdpMgtServiceComponentHolder.getInstance().setSecretResolveManager(secretResolveManager);
        when(secretType.getId()).thenReturn("secretId");
        doReturn(secretType).when(secretManager).getSecretType(any());
        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(false);

        cryptoUtil = mockStatic(CryptoUtil.class);
        mockCryptoUtil = mock(CryptoUtil.class);
        cryptoUtil.when(CryptoUtil::getDefaultCryptoUtil).thenReturn(mockCryptoUtil);

        identityUtil = mockStatic(IdentityUtil.class);
        identityUtil.when(() -> IdentityUtil.getProperty(
                IdPManagementConstants.OUTBOUND_PROVISIONING_CONFIDENTIAL_DATA_PROTECTION_ENABLED))
                .thenReturn("true");

        idpSecretsProcessor = new IdPSecretsProcessor();
    }

    @AfterClass
    public void tearDown() throws Exception {
        cryptoUtil.close();
        identityUtil.close();
    }

    @Test(priority = 1)
    public void testAddIdpSecrets() throws Exception {

        encryptSecret(SAMPLE_SECRET_VALUE1);
        IdentityProvider createdIdp = idpSecretsProcessor.encryptAssociatedSecrets(buildIDPObject());

        for (Property property : createdIdp.getFederatedAuthenticatorConfigs()[0].getProperties()) {
            if (property.isConfidential()) {
                Assert.assertEquals(buildSecretNameWithIdpObj(SAMPLE_SECRET_NAME1), property.getValue());
            }
        }
    }

    @Test(priority = 2)
    public void testGetIdpSecrets() throws Exception {

        IdentityProvider identityProvider = buildIDPObject();
        encryptSecret(SAMPLE_SECRET_VALUE1);
        IdentityProvider addedIdp = idpSecretsProcessor.encryptAssociatedSecrets(identityProvider);

        decryptSecret(ENCRYPTED_VALUE1);
        IdentityProvider updatedIdp = idpSecretsProcessor.decryptAssociatedSecrets(addedIdp);

        for (Property property : updatedIdp.getFederatedAuthenticatorConfigs()[0].getProperties()) {
            if (property.isConfidential()) {
                Assert.assertEquals(buildSecretNameWithIdpObj(SAMPLE_SECRET_NAME1), property.getValue());
            }
        }
    }

    @Test(priority = 3)
    public void testDeleteIdpSecrets() throws Exception {

        IdentityProvider identityProvider = buildIDPObject();
        encryptSecret(SAMPLE_SECRET_VALUE1);
        IdentityProvider addedIdp = idpSecretsProcessor.encryptAssociatedSecrets(identityProvider);

        idpSecretsProcessor.deleteAssociatedSecrets(addedIdp);
        for (String secretName : buildSecretNamesList(identityProvider)) {
            assertFalse(secretManager.isSecretExist(SecretConstants.IDN_SECRET_TYPE_IDP_SECRETS, secretName), "Expected secret is still available after delete idp secret functionality is executed.");
        }
    }

    @Test(priority = 4)
    public void testUpdateIdpSecrets() throws Exception {

        IdentityProvider identityProvider = buildIDPObject();
        encryptSecret(SAMPLE_SECRET_VALUE1);
        idpSecretsProcessor.encryptAssociatedSecrets(identityProvider);

        IdentityProvider updatedIdpObject = buildUpdatedIdpObject(identityProvider);
        decryptSecret(ENCRYPTED_VALUE1);
        encryptSecret(SAMPLE_SECRET_VALUE2);
        idpSecretsProcessor.encryptAssociatedSecrets(updatedIdpObject);

        decryptSecret(ENCRYPTED_VALUE2);
        IdentityProvider updatedIdp = idpSecretsProcessor.decryptAssociatedSecrets(updatedIdpObject);

        for (Property property : updatedIdp.getFederatedAuthenticatorConfigs()[0].getProperties()) {
            if (property.isConfidential()) {
                assertEquals(SAMPLE_SECRET_VALUE2, property.getValue());
            }
        }
    }

    private IdentityProvider buildIDPObject() {
        IdentityProvider idp = new IdentityProvider();
        idp.setId(fedId);
        idp.setIdentityProviderName("testIdP1");
        idp.setEnable(true);
        idp.setPrimary(true);
        idp.setFederationHub(true);
        idp.setCertificate("");

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("OIDCAuthenticator");
        federatedAuthenticatorConfig.setName(fedName);
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
        for (FederatedAuthenticatorConfig fedAuthConfig : identityProvider.getFederatedAuthenticatorConfigs()) {
            for (Property prop : fedAuthConfig.getProperties()) {
                if (prop.isConfidential()) {
                    String secretName = buildSecretNameWithIdpObj(prop.getName());
                    secretNames.add(secretName);
                }
            }
        }
        return secretNames;
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

    private String buildSecretNameWithIdpObj(String propName) {
        return "secretId:" + fedId + ":" + fedName + ":" + propName;
    }

    @Test(priority = 5)
    public void testEncryptProvisioningConnectorSecrets() throws Exception {

        encryptSecret(SAMPLE_SECRET_VALUE1);
        IdentityProvider createdIdp = idpSecretsProcessor.encryptProvisioningConnectorSecrets(buildIDPWithProvisioningConfig());

        for (Property property : createdIdp.getProvisioningConnectorConfigs()[0].getProvisioningProperties()) {
            if (property.isConfidential()) {
                Assert.assertEquals(buildProvisioningSecretNameWithIdpObj(PROV_SECRET_NAME), property.getValue());
            }
        }
    }

    @Test(priority = 6)
    public void testDecryptProvisioningConnectorSecrets() throws Exception {

        IdentityProvider identityProvider = buildIDPWithProvisioningConfig();
        encryptSecret(SAMPLE_SECRET_VALUE1);
        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(true);

        ResolvedSecret resolvedSecret = mock(ResolvedSecret.class);
        when(resolvedSecret.getResolvedSecretValue()).thenReturn(SAMPLE_SECRET_VALUE1);
        when(secretResolveManager.getResolvedSecret(anyString(), anyString())).thenReturn(resolvedSecret);

        IdentityProvider decryptedIdp = idpSecretsProcessor.decryptProvisioningConnectorSecrets(identityProvider);

        for (Property property : decryptedIdp.getProvisioningConnectorConfigs()[0].getProvisioningProperties()) {
            if (property.isConfidential()) {
                Assert.assertEquals(SAMPLE_SECRET_VALUE1, property.getValue());
            }
        }
    }

    @Test(priority = 7)
    public void testDeleteProvisioningConnectorSecrets() throws Exception {

        IdentityProvider identityProvider = buildIDPWithProvisioningConfig();
        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(true);

        idpSecretsProcessor.deleteAssociatedSecrets(identityProvider);

        String expectedSecretName = fedId + ":provisioning:" + provConnectorName + ":" + PROV_SECRET_NAME;
        verify(secretManager, times(1)).deleteSecret(
                eq(SecretConstants.IDN_SECRET_TYPE_IDP_SECRETS),
                eq(expectedSecretName)
        );
    }

    @Test(priority = 8)
    public void testProvisioningSecretsSkippedWhenFeatureDisabled() throws Exception {

        identityUtil.when(() -> IdentityUtil.getProperty(
                IdPManagementConstants.OUTBOUND_PROVISIONING_CONFIDENTIAL_DATA_PROTECTION_ENABLED))
                .thenReturn("false");

        IdentityProvider identityProvider = buildIDPWithProvisioningConfig();
        IdentityProvider processedIdp = idpSecretsProcessor.encryptProvisioningConnectorSecrets(identityProvider);

        for (Property property : processedIdp.getProvisioningConnectorConfigs()[0].getProvisioningProperties()) {
            if (property.isConfidential()) {
                Assert.assertEquals(SAMPLE_SECRET_VALUE1, property.getValue(),
                        "Provisioning secret should remain unchanged when feature is disabled");
            }
        }

        identityUtil.when(() -> IdentityUtil.getProperty(
                IdPManagementConstants.OUTBOUND_PROVISIONING_CONFIDENTIAL_DATA_PROTECTION_ENABLED))
                .thenReturn("true");
    }

    private IdentityProvider buildIDPWithProvisioningConfig() {

        IdentityProvider idp = new IdentityProvider();
        idp.setId(fedId);
        idp.setIdentityProviderName("testIdP1");
        idp.setEnable(true);
        idp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[0]);

        ProvisioningConnectorConfig provisioningConnectorConfig = new ProvisioningConnectorConfig();
        provisioningConnectorConfig.setName(provConnectorName);
        provisioningConnectorConfig.setEnabled(true);

        Property secretProperty = new Property();
        secretProperty.setName(PROV_SECRET_NAME);
        secretProperty.setValue(SAMPLE_SECRET_VALUE1);
        secretProperty.setConfidential(true);

        Property nonSecretProperty = new Property();
        nonSecretProperty.setName("userName");
        nonSecretProperty.setValue("admin");
        nonSecretProperty.setConfidential(false);

        provisioningConnectorConfig.setProvisioningProperties(new Property[]{secretProperty, nonSecretProperty});
        idp.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{provisioningConnectorConfig});

        return idp;
    }

    private String buildProvisioningSecretNameWithIdpObj(String propName) {

        return "secretId:" + fedId + ":provisioning:" + provConnectorName + ":" + propName;
    }

    @Test(priority = 9)
    public void testEncryptProvisioningConnectorSecretsWithNullConfig() throws Exception {

        IdentityProvider idp = new IdentityProvider();
        idp.setId(fedId);
        idp.setIdentityProviderName("testIdP1");
        idp.setProvisioningConnectorConfigs(null);
        idp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[0]);

        IdentityProvider result = idpSecretsProcessor.encryptProvisioningConnectorSecrets(idp);

        // When feature is enabled and configs are null, should return cloned object with no changes.IdPManagementDAO.java
        Assert.assertNotNull(result, "Result should not be null");
        Assert.assertEquals(result.getIdentityProviderName(), idp.getIdentityProviderName());
    }

    @Test(priority = 10)
    public void testDecryptProvisioningConnectorSecretsWithNullConfig() throws Exception {

        IdentityProvider idp = new IdentityProvider();
        idp.setId(fedId);
        idp.setIdentityProviderName("testIdP1");
        idp.setProvisioningConnectorConfigs(null);
        idp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[0]);

        IdentityProvider result = idpSecretsProcessor.decryptProvisioningConnectorSecrets(idp);

        // When feature is enabled and configs are null, should return cloned object with no changes.
        Assert.assertNotNull(result, "Result should not be null");
        Assert.assertEquals(result.getIdentityProviderName(), idp.getIdentityProviderName());
    }

    @Test(priority = 11)
    public void testEncryptProvisioningConnectorSecretsDoesNotAffectAuthenticators() throws Exception {

        IdentityProvider idp = buildIDPWithBothConfigs();
        encryptSecret(SAMPLE_SECRET_VALUE1);

        IdentityProvider result = idpSecretsProcessor.encryptProvisioningConnectorSecrets(idp);

        // Verify provisioning secrets are encrypted.
        for (Property property : result.getProvisioningConnectorConfigs()[0].getProvisioningProperties()) {
            if (property.isConfidential()) {
                Assert.assertEquals(buildProvisioningSecretNameWithIdpObj(PROV_SECRET_NAME), property.getValue());
            }
        }

        // Verify authenticator secrets are NOT encrypted.
        for (Property property : result.getFederatedAuthenticatorConfigs()[0].getProperties()) {
            if (property.isConfidential()) {
                Assert.assertEquals(property.getValue(), SAMPLE_SECRET_VALUE1,
                        "Authenticator secret should not be encrypted by encryptProvisioningConnectorSecrets");
            }
        }
    }

    @Test(priority = 12)
    public void testDecryptProvisioningConnectorSecretsDoesNotAffectAuthenticators() throws Exception {

        IdentityProvider idp = buildIDPWithBothConfigs();
        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(true);

        ResolvedSecret resolvedSecret = mock(ResolvedSecret.class);
        when(resolvedSecret.getResolvedSecretValue()).thenReturn("decrypted_value");
        when(secretResolveManager.getResolvedSecret(anyString(), anyString())).thenReturn(resolvedSecret);

        // Set authenticator secret to a reference (simulating already encrypted state).
        idp.getFederatedAuthenticatorConfigs()[0].getProperties()[0].setValue("secretId:ref:auth");

        IdentityProvider result = idpSecretsProcessor.decryptProvisioningConnectorSecrets(idp);

        // Verify provisioning secrets are decrypted.
        for (Property property : result.getProvisioningConnectorConfigs()[0].getProvisioningProperties()) {
            if (property.isConfidential()) {
                Assert.assertEquals(property.getValue(), "decrypted_value");
            }
        }

        // Verify authenticator secrets are NOT decrypted.
        for (Property property : result.getFederatedAuthenticatorConfigs()[0].getProperties()) {
            if (property.isConfidential()) {
                Assert.assertEquals(property.getValue(), "secretId:ref:auth",
                        "Authenticator secret should not be decrypted by decryptProvisioningConnectorSecrets");
            }
        }
    }

    private IdentityProvider buildIDPWithBothConfigs() {

        IdentityProvider idp = new IdentityProvider();
        idp.setId(fedId);
        idp.setIdentityProviderName("testIdP1");
        idp.setEnable(true);

        // Add federated authenticator config.
        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("OIDCAuthenticator");
        federatedAuthenticatorConfig.setName(fedName);
        federatedAuthenticatorConfig.setEnabled(true);
        Property authProperty = new Property();
        authProperty.setName(SAMPLE_SECRET_NAME1);
        authProperty.setValue(SAMPLE_SECRET_VALUE1);
        authProperty.setConfidential(true);
        federatedAuthenticatorConfig.setProperties(new Property[]{authProperty});
        idp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{federatedAuthenticatorConfig});

        // Add provisioning connector config.
        ProvisioningConnectorConfig provisioningConnectorConfig = new ProvisioningConnectorConfig();
        provisioningConnectorConfig.setName(provConnectorName);
        provisioningConnectorConfig.setEnabled(true);
        Property provProperty = new Property();
        provProperty.setName(PROV_SECRET_NAME);
        provProperty.setValue(SAMPLE_SECRET_VALUE1);
        provProperty.setConfidential(true);
        provisioningConnectorConfig.setProvisioningProperties(new Property[]{provProperty});
        idp.setProvisioningConnectorConfigs(new ProvisioningConnectorConfig[]{provisioningConnectorConfig});

        return idp;
    }
}
