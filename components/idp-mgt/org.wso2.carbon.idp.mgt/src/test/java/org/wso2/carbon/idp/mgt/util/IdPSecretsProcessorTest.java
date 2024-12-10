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
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;


public class IdPSecretsProcessorTest {

    private SecretManagerImpl secretManager;
    private IdPSecretsProcessor idpSecretsProcessor;
    private CryptoUtil mockCryptoUtil;
    private MockedStatic<CryptoUtil> cryptoUtil;

    private static final String SAMPLE_SECRET_NAME1 = "sample-secret1";
    private static final String SAMPLE_SECRET_VALUE1 = "dummy_value1";
    private static final String SAMPLE_SECRET_VALUE2 = "dummy_value2";
    private static final String ENCRYPTED_VALUE1 = "dummy_encrypted1";
    private static final String ENCRYPTED_VALUE2 = "dummy_encrypted2";

    private static final String fedId = "5";
    private static final String fedName = "SampleOIDCAuthenticator";


    @BeforeClass
    public void setUp() throws Exception {

        secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        IdpMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn("secretId");
        doReturn(secretType).when(secretManager).getSecretType(any());
        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(false);

        cryptoUtil = mockStatic(CryptoUtil.class);
        mockCryptoUtil = mock(CryptoUtil.class);
        cryptoUtil.when(CryptoUtil::getDefaultCryptoUtil).thenReturn(mockCryptoUtil);

        idpSecretsProcessor = new IdPSecretsProcessor();
    }

    @AfterClass
    public void tearDown() throws Exception {
        cryptoUtil.close();
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
}
