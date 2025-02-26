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

package org.wso2.carbon.identity.action.management.util;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.internal.component.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.internal.util.ActionSecretProcessor;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_ISSUE_ACCESS_TOKEN_ACTION_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACCESS_TOKEN;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_ACCESS_TOKEN_UPDATED;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_API_KEY_HEADER;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_API_KEY_VALUE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_PASSWORD;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_SECRET_TYPE_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TEST_USERNAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.buildMockAPIKeyAuthentication;
import static org.wso2.carbon.identity.action.management.util.TestUtil.buildMockBasicAuthentication;
import static org.wso2.carbon.identity.action.management.util.TestUtil.buildMockBearerAuthentication;
import static org.wso2.carbon.identity.action.management.util.TestUtil.buildSecretName;

/**
 * Test class for Action secrets processor.
 */
public class ActionSecretProcessorTest {

    private SecretManager secretManager;
    private SecretResolveManager secretResolveManager;
    private ActionSecretProcessor actionSecretProcessor;

    @BeforeClass
    public void setUpClass() {

        actionSecretProcessor = new ActionSecretProcessor();
    }

    @BeforeMethod
    public void setUp() throws SecretManagementException {

        secretManager = mock(SecretManagerImpl.class);
        secretResolveManager = mock(SecretResolveManager.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        ActionMgtServiceComponentHolder.getInstance().setSecretResolveManager(secretResolveManager);

        SecretType secretType = mock(SecretType.class);
        doReturn(TEST_SECRET_TYPE_ID).when(secretType).getId();
        doReturn(secretType).when(secretManager).getSecretType(any());

    }

    @DataProvider
    public Object[] provideAuthentication() {

        return new Object[]{
                buildMockBearerAuthentication(TEST_ACCESS_TOKEN),
                buildMockBasicAuthentication(TEST_USERNAME, TEST_PASSWORD),
                buildMockAPIKeyAuthentication(TEST_API_KEY_HEADER, TEST_API_KEY_VALUE)
        };
    }

    @Test(dataProvider = "provideAuthentication")
    public void testEncryptAssociatedSecrets(Authentication authentication) throws SecretManagementException {

        doReturn(false).when(secretManager).isSecretExist(any(), any());
        doReturn(null).when(secretManager).addSecret(any(), any());

        List<AuthProperty> encryptedProperties = actionSecretProcessor.encryptAssociatedSecrets(authentication,
                PRE_ISSUE_ACCESS_TOKEN_ACTION_ID);

        Assert.assertEquals(encryptedProperties.size(), authentication.getProperties().size());
        for (AuthProperty authProperty : encryptedProperties) {
            Authentication.Property property = Arrays.stream(Authentication.Property.values())
                    .filter(prop -> prop.getName().equals(authProperty.getName()))
                    .findFirst()
                    .orElse(null);
            AuthProperty inputAuthProperty = authentication.getProperty(property);

            Assert.assertNotNull(property);
            Assert.assertEquals(authProperty.getName(), authentication.getProperty(property).getName());
            Assert.assertEquals(authProperty.getIsConfidential(), inputAuthProperty.getIsConfidential());
            if (authProperty.getIsConfidential()) {
                Assert.assertEquals(authProperty.getValue(),
                        TestUtil.buildSecretName(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, authentication.getType(), property));
            } else {
                Assert.assertEquals(authProperty.getValue(), inputAuthProperty.getValue());
            }
        }
    }

    @Test
    public void testUpdateSecret() throws SecretManagementException {

        ResolvedSecret resolvedSecret = mock(ResolvedSecret.class);
        doReturn(TEST_ACCESS_TOKEN).when(resolvedSecret).getResolvedSecretValue();
        doReturn(resolvedSecret).when(secretResolveManager).getResolvedSecret(any(), any());
        doReturn(true).when(secretManager).isSecretExist(any(), any());
        doReturn(null).when(secretManager).updateSecretValue(any(), any(), any());

        Authentication authentication = buildMockBearerAuthentication(TEST_ACCESS_TOKEN_UPDATED);
        List<AuthProperty> encryptedProperties = actionSecretProcessor.encryptAssociatedSecrets(authentication,
                PRE_ISSUE_ACCESS_TOKEN_ACTION_ID);

        Assert.assertEquals(encryptedProperties.size(), authentication.getProperties().size());
        Assert.assertEquals(encryptedProperties.get(0).getName(), authentication.getProperties().get(0).getName());
        Assert.assertEquals(encryptedProperties.get(0).getName(), authentication.getProperties().get(0).getName());
        Assert.assertEquals(encryptedProperties.get(0).getValue(), buildSecretName(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
                Authentication.Type.BEARER, Authentication.Property.ACCESS_TOKEN));
    }

    @Test
    public void testDecryptAssociatedSecrets() throws SecretManagementException {

        ResolvedSecret resolvedSecret = mock(ResolvedSecret.class);
        doReturn(TEST_ACCESS_TOKEN).when(resolvedSecret).getResolvedSecretValue();
        doReturn(resolvedSecret).when(secretResolveManager).getResolvedSecret(any(), any());
        doReturn(true).when(secretManager).isSecretExist(any(), any());

        Authentication authentication = buildMockBearerAuthentication(buildSecretName(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
                        Authentication.Type.BEARER, Authentication.Property.ACCESS_TOKEN));

        List<AuthProperty> decryptedProperties = actionSecretProcessor.decryptAssociatedSecrets(authentication,
                PRE_ISSUE_ACCESS_TOKEN_ACTION_ID);

        Assert.assertEquals(decryptedProperties.size(), authentication.getProperties().size());
        Assert.assertEquals(decryptedProperties.get(0).getName(), authentication.getProperties().get(0).getName());
        Assert.assertEquals(decryptedProperties.get(0).getIsConfidential(),
                authentication.getProperties().get(0).getIsConfidential());
        Assert.assertEquals(decryptedProperties.get(0).getValue(), TEST_ACCESS_TOKEN);
    }

    @Test
    public void testDecryptAssociatedSecretsForNonSecret() throws SecretManagementException {

        ResolvedSecret resolvedSecret = mock(ResolvedSecret.class);
        doReturn(TEST_API_KEY_VALUE).when(resolvedSecret).getResolvedSecretValue();
        doReturn(resolvedSecret).when(secretResolveManager).getResolvedSecret(any(), any());
        doReturn(true).when(secretManager).isSecretExist(any(), any());

        Authentication authentication = buildMockAPIKeyAuthentication(TEST_API_KEY_HEADER,
                buildSecretName(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, Authentication.Type.API_KEY,
                        Authentication.Property.VALUE));

        List<AuthProperty> decryptedProperties = actionSecretProcessor.decryptAssociatedSecrets(authentication,
                PRE_ISSUE_ACCESS_TOKEN_ACTION_ID);

        for (AuthProperty authProperty : decryptedProperties) {
            Authentication.Property property = Arrays.stream(Authentication.Property.values())
                    .filter(prop -> prop.getName().equals(authProperty.getName()))
                    .findFirst()
                    .orElse(null);
            AuthProperty inputAuthProperty = authentication.getProperty(property);

            Assert.assertEquals(authProperty.getName(), authentication.getProperty(property).getName());
            Assert.assertEquals(authProperty.getIsConfidential(), inputAuthProperty.getIsConfidential());
            if (authProperty.getIsConfidential()) {
                Assert.assertEquals(authProperty.getValue(), TEST_API_KEY_VALUE);
            } else {
                Assert.assertEquals(authProperty.getValue(), TEST_API_KEY_HEADER);
            }
        }
    }

    @Test(expectedExceptions = SecretManagementException.class)
    public void testDecryptAssociatedSecretsForNonExistingSecret() throws SecretManagementException {

        doReturn(false).when(secretManager).isSecretExist(any(), any());
        Authentication authentication = buildMockBearerAuthentication(buildSecretName(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID,
                Authentication.Type.BEARER, Authentication.Property.ACCESS_TOKEN));

        actionSecretProcessor.decryptAssociatedSecrets(authentication, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID);
    }

    @Test
    public void testDeleteAssociatedSecrets() throws SecretManagementException {

        doReturn(true).when(secretManager).isSecretExist(any(), any());
        doNothing().when(secretManager).deleteSecret(any(), any());

        Authentication authentication = buildMockAPIKeyAuthentication(TEST_API_KEY_HEADER,
                buildSecretName(PRE_ISSUE_ACCESS_TOKEN_ACTION_ID, Authentication.Type.API_KEY,
                        Authentication.Property.VALUE));

        actionSecretProcessor.deleteAssociatedSecrets(authentication, PRE_ISSUE_ACCESS_TOKEN_ACTION_ID);
        verify(secretManager, times(1)).deleteSecret(any(), any());
    }
}
