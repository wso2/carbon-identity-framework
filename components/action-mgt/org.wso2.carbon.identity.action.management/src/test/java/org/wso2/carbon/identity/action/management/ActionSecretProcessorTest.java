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

package org.wso2.carbon.identity.action.management;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.internal.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.AuthType;
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
public class ActionSecretProcessorTest {

    private AuthType authType;
    private List<AuthProperty> encryptedProperties;
    private static final int TENANT_ID = 2;
    private static final String SECRET_ID = "secretId";
    private final String actionId = "738699f2-737f-4c76-b517-b614f013f705";
    private final String accessToken = "c7fce95f-3f5b-4cda-8bb1-4cb7b3990f83";
    private final ActionSecretProcessor secretProcessor = new ActionSecretProcessor();
    private final SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
    private final SecretType secretType = mock(SecretType.class);
    private final SecretResolveManager secretResolveManager = mock(SecretResolveManager.class);
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeClass
    public void setUp() {

        authType = buildMockAuthType();
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        ActionMgtServiceComponentHolder.getInstance().setSecretResolveManager(secretResolveManager);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(()-> IdentityTenantUtil.getTenantId(anyString())).thenReturn(TENANT_ID);
    }

    @AfterClass
    public void tearDown() {

        identityTenantUtil.close();
    }

    @Test(priority = 1)
    public void testEncryptAssociatedSecrets() throws SecretManagementException {

        String encryptedSecretReference = SECRET_ID + ":" + actionId + ":" + authType.getType() + ":"
                + authType.getProperties().get(0).getName();
        when(secretType.getId()).thenReturn(SECRET_ID);
        when(secretManager.getSecretType(any())).thenReturn(secretType);
        encryptedProperties = secretProcessor.encryptAssociatedSecrets(authType, actionId);
        Assert.assertEquals(authType.getProperties().size(), encryptedProperties.size());
        Assert.assertEquals(authType.getProperties().get(0).getName(), encryptedProperties.get(0).getName());
        Assert.assertEquals(authType.getProperties().get(0).getIsConfidential(),
                encryptedProperties.get(0).getIsConfidential());
        Assert.assertEquals(encryptedSecretReference, encryptedProperties.get(0).getValue());
    }

    @Test(priority = 2)
    public void testDecryptAssociatedSecrets() throws SecretManagementException {

        ResolvedSecret resolvedSecret = new ResolvedSecret();
        resolvedSecret.setResolvedSecretValue(accessToken);
        when(secretResolveManager.getResolvedSecret(anyString(), anyString())).thenReturn(resolvedSecret);
        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(true);
        List<AuthProperty> decryptedProperties = secretProcessor.decryptAssociatedSecrets(encryptedProperties,
                authType.getType().getType(), actionId);
        Assert.assertEquals(encryptedProperties.size(), decryptedProperties.size());
        Assert.assertEquals(encryptedProperties.get(0).getName(), decryptedProperties.get(0).getName());
        Assert.assertEquals(accessToken, decryptedProperties.get(0).getValue());
        Assert.assertEquals(encryptedProperties.get(0).getIsConfidential(),
                decryptedProperties.get(0).getIsConfidential());
    }

    @Test(priority = 3)
    public void testDeleteAssociatedSecrets() throws SecretManagementException {

        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(true);
        secretProcessor.deleteAssociatedSecrets(authType, actionId);
    }

    @Test(priority = 4)
    public void testGetPropertiesWithSecretReferences() throws SecretManagementException {

        String propertiesSecretReference = SECRET_ID + ":" + actionId + ":" + authType.getType() + ":"
                + authType.getProperties().get(0).getName();
        when(secretType.getId()).thenReturn(SECRET_ID);
        when(secretManager.getSecretType(any())).thenReturn(secretType);
        List<AuthProperty> authProperties = secretProcessor.getPropertiesWithSecretReferences(authType.getProperties(),
                actionId, authType.getType().name());
        Assert.assertEquals(authType.getProperties().size(), authProperties.size());
        Assert.assertEquals(authType.getProperties().get(0).getName(), authProperties.get(0).getName());
        Assert.assertEquals(propertiesSecretReference, authProperties.get(0).getValue());
        Assert.assertEquals(authType.getProperties().get(0).getIsConfidential(),
                authProperties.get(0).getIsConfidential());
    }

    private AuthType buildMockAuthType() {

        return new AuthType.AuthTypeBuilder()
                .type(AuthType.AuthenticationType.BEARER)
                .properties(Arrays.asList(
                        new AuthProperty.AuthPropertyBuilder()
                                .name(AuthType.AuthenticationType.AuthenticationProperty.ACCESS_TOKEN.getName())
                                .value(accessToken)
                                .isConfidential(AuthType.AuthenticationType.AuthenticationProperty
                                        .ACCESS_TOKEN.getIsConfidential())
                                .build()))
                .build();
    }
}
