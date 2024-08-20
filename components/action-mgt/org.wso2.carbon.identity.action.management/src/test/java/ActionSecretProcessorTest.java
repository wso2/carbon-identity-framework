import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.ActionSecretProcessor;
import org.wso2.carbon.identity.action.management.internal.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.AuthType;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * This class is a test suite for the ActionSecretProcessor class.
 * It contains unit tests to verify the functionality of the methods
 * in the ActionSecretProcessor class.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
public class ActionSecretProcessorTest {

    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private final String actionId = "738699f2-737f-4c76-b517-b614f013f705";
    private final String accessToken = "c7fce95f-3f5b-4cda-8bb1-4cb7b3990f83";
    private static final int TENANT_ID = 2;
    private static final String SECRET_ID = "secretId";
    private final ActionSecretProcessor secretProcessor = new ActionSecretProcessor();
    private final SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
    private List<AuthProperty> encryptedProperties;
    private final AuthType authType = new AuthType.AuthTypeBuilder()
            .type(AuthType.AuthenticationType.BEARER)
            .properties(Arrays.asList(
                    new AuthProperty.AuthPropertyBuilder()
                            .name(AuthType.AuthenticationType.AuthenticationProperty.ACCESS_TOKEN.getName())
                            .value(accessToken)
                            .isConfidential(AuthType.AuthenticationType.AuthenticationProperty
                                    .ACCESS_TOKEN.getIsConfidential())
                            .build()))
            .build();

    @BeforeMethod
    public void setUp() {

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(()-> IdentityTenantUtil.getTenantId(anyString())).thenReturn(TENANT_ID);
    }

    @AfterMethod
    public void tearDown() {

        identityTenantUtil.close();
    }

    @Test(priority = 1)
    public void testEncryptAssociatedSecrets() throws SecretManagementException {

        SecretType secretType = mock(SecretType.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn(SECRET_ID);
        when(secretManager.getSecretType(any())).thenReturn(secretType);
        String encryptedSecretReference = SECRET_ID + ":" + actionId + ":" + authType.getType() + ":"
                + authType.getProperties().get(0).getName();
        encryptedProperties = secretProcessor.encryptAssociatedSecrets(authType, actionId);
        Assert.assertEquals(authType.getProperties().size(), encryptedProperties.size());
        Assert.assertEquals(authType.getProperties().get(0).getName(), encryptedProperties.get(0).getName());
        Assert.assertEquals(authType.getProperties().get(0).getIsConfidential(),
                encryptedProperties.get(0).getIsConfidential());
        Assert.assertEquals(encryptedSecretReference, encryptedProperties.get(0).getValue());
    }

    @Test(priority = 2)
    public void testDecryptAssociatedSecrets() throws SecretManagementException {

        SecretResolveManager secretResolveManager = mock(SecretResolveManager.class);
        ResolvedSecret resolvedSecret = new ResolvedSecret();
        resolvedSecret.setResolvedSecretValue(accessToken);
        ActionMgtServiceComponentHolder.getInstance().setSecretResolveManager(secretResolveManager);
        when(secretResolveManager.getResolvedSecret(anyString(), anyString())).thenReturn(resolvedSecret);
        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(true);
        List<AuthProperty> decryptedProperties = secretProcessor.decryptAssociatedSecrets(encryptedProperties,
                AuthType.AuthenticationType.BEARER.getType(), actionId);
        Assert.assertEquals(encryptedProperties.size(), decryptedProperties.size());
        Assert.assertEquals(encryptedProperties.get(0).getName(), decryptedProperties.get(0).getName());
        Assert.assertEquals(encryptedProperties.get(0).getIsConfidential(),
                decryptedProperties.get(0).getIsConfidential());
        Assert.assertEquals(accessToken, decryptedProperties.get(0).getValue());
    }

    @Test(priority = 3)
    public void testDeleteAssociatedSecrets() {

        try {
            ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
            when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(true);
            secretProcessor.deleteAssociatedSecrets(authType, actionId);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test(priority = 4)
    public void testGetPropertiesWithSecretReferences() throws SecretManagementException {

        SecretType secretType = mock(SecretType.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn(SECRET_ID);
        when(secretManager.getSecretType(any())).thenReturn(secretType);
        String propertiesSecretReference = SECRET_ID + ":" + actionId + ":" + authType.getType() + ":"
                + authType.getProperties().get(0).getName();
        List<AuthProperty> authProperties = secretProcessor.getPropertiesWithSecretReferences(authType.getProperties(),
                actionId, authType.getType().name());
        Assert.assertEquals(authType.getProperties().size(), authProperties.size());
        Assert.assertEquals(authType.getProperties().get(0).getName(), authProperties.get(0).getName());
        Assert.assertEquals(propertiesSecretReference, authProperties.get(0).getValue());
        Assert.assertEquals(authType.getProperties().get(0).getIsConfidential(),
                authProperties.get(0).getIsConfidential());
    }
}
