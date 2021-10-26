package org.wso2.carbon.idp.mgt.secretprocessor;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.internal.IdpMgtServiceComponentHolder;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test class for the SecretManagerPersistenceProcessor.
 */
@PrepareForTest({SecretManager.class, IdpMgtServiceComponentHolder.class, CryptoUtil.class})
public class SecretManagerPersistenceProcessorTest extends PowerMockTestCase {

    private static final String SECRET_ID = "secretId";
    private static final String SECRET_VALUE = "secretValue";
    private static final String NEW_SECRET_VALUE = "newSecretValue";
    private static final String ENCRYPTED_SECRET_VALUE = "encryptedSecretValue";
    private static final String AUTHENTICATOR_NAME = "authenticatorName";
    private static final String PROPERTY_NAME = "propertyName";
    private static final String SECRET_TYPE = "FEDERATED_AUTHENTICATOR";

    private SecretManagerPersistenceProcessor secretManagerPersistenceProcessor = new SecretManagerPersistenceProcessor();

    @Mock
    private IdpMgtServiceComponentHolder mockedIdpMgtServiceComponentHolder;

    @Mock
    private SecretManager mockedSecretManager;

    @Mock
    private Secret mockedSecret;

    @Mock
    private CryptoUtil mockedCryptoUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        mockStatic(IdpMgtServiceComponentHolder.class);
        Mockito.when(IdpMgtServiceComponentHolder.getInstance()).thenReturn(mockedIdpMgtServiceComponentHolder);
        mockStatic(SecretManager.class);
        Mockito.when(mockedIdpMgtServiceComponentHolder.getSecretManager()).thenReturn(mockedSecretManager);
    }

    @Test
    public void testAddSecret() throws Exception {

        Mockito.when(mockedSecretManager.addSecret(anyString(), anyObject())).thenReturn(mockedSecret);
        Mockito.when(mockedSecret.getSecretId()).thenReturn(SECRET_ID);

        assertEquals(secretManagerPersistenceProcessor.addSecret(1, AUTHENTICATOR_NAME, PROPERTY_NAME, SECRET_VALUE,
                SECRET_TYPE), SECRET_ID);
    }

    @Test(expectedExceptions = IdentityProviderManagementServerException.class)
    public void testAddSecretWithInvalidSecret() throws Exception {

        Mockito.when(mockedSecretManager.addSecret(anyString(), anyObject()))
                .thenThrow(new SecretManagementException());

        secretManagerPersistenceProcessor.addSecret(1, AUTHENTICATOR_NAME, PROPERTY_NAME, SECRET_VALUE, SECRET_TYPE);
    }

    @Test
    public void testGetPreprocessedSecret() throws Exception {

        Mockito.when(mockedSecretManager.getSecretById(anyString())).thenReturn(mockedSecret);
        Mockito.when(mockedSecret.getSecretValue()).thenReturn(ENCRYPTED_SECRET_VALUE);

        mockStatic(CryptoUtil.class);
        mockedCryptoUtil = mock(CryptoUtil.class);
        when(CryptoUtil.getDefaultCryptoUtil()).thenReturn(mockedCryptoUtil);
        when(mockedCryptoUtil.base64DecodeAndDecrypt(ENCRYPTED_SECRET_VALUE)).thenReturn(SECRET_VALUE.getBytes());

        assertNotNull(secretManagerPersistenceProcessor.getPreprocessedSecret(SECRET_ID));
    }

    @Test(expectedExceptions = IdentityProviderManagementServerException.class)
    public void testGetPreprocessedSecretWithDecryptionError() throws Exception {

        Mockito.when(mockedSecretManager.getSecretById(anyString())).thenReturn(mockedSecret);
        Mockito.when(mockedSecret.getSecretValue()).thenReturn(ENCRYPTED_SECRET_VALUE);

        mockStatic(CryptoUtil.class);
        mockedCryptoUtil = mock(CryptoUtil.class);
        when(CryptoUtil.getDefaultCryptoUtil()).thenReturn(mockedCryptoUtil);

        when(mockedCryptoUtil.base64DecodeAndDecrypt(anyString())).thenThrow(new CryptoException());
        secretManagerPersistenceProcessor.getPreprocessedSecret(SECRET_ID);
    }

    @Test(expectedExceptions = IdentityProviderManagementServerException.class)
    public void testGetPreprocessedSecretWithInvalidSecretId() throws Exception {

        Mockito.when(mockedSecretManager.getSecretById(anyString())).thenThrow(new SecretManagementException());

        secretManagerPersistenceProcessor.getPreprocessedSecret(SECRET_ID);
    }

    @Test
    public void testDeleteSecret() throws Exception {

        secretManagerPersistenceProcessor.deleteSecret(SECRET_ID);
    }

    @Test(expectedExceptions = IdentityProviderManagementServerException.class)
    public void testDeleteSecretWithInvalidSecretId() throws Exception {

        Mockito.doThrow(new SecretManagementException()).when(mockedSecretManager).deleteSecretById(SECRET_ID);

        secretManagerPersistenceProcessor.deleteSecret(SECRET_ID);
    }

    @Test
    public void testUpdateSecret() throws Exception {

        Mockito.when(mockedSecretManager.updateSecretValueById(anyString(), anyString())).thenReturn(mockedSecret);
        Mockito.when(mockedSecret.getSecretId()).thenReturn(SECRET_ID);

        assertEquals(secretManagerPersistenceProcessor.updateSecret(SECRET_ID, NEW_SECRET_VALUE), SECRET_ID);
    }

    @Test(expectedExceptions = IdentityProviderManagementServerException.class)
    public void testUpdateSecretWithInvalidSecretId() throws Exception {

        Mockito.when(mockedSecretManager.updateSecretValueById(anyString(), anyString()))
                .thenThrow(new SecretManagementException());

        assertEquals(secretManagerPersistenceProcessor.updateSecret(SECRET_ID, NEW_SECRET_VALUE), SECRET_ID);
    }

}
