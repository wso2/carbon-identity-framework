/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.management.util;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookSecretProcessor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebhookSecretProcessorTest {

    private static final String TEST_WEBHOOK_ID = "webhook-123";
    private static final String TEST_SECRET = "super-secret";
    private static final String TEST_SECRET_TYPE_ID = "WEBHOOK_SECRETS";
    private static final String SECRET_NAME = TEST_WEBHOOK_ID + ":ENDPOINT:SECRET";
    private static final String SECRET_REFERENCE = TEST_SECRET_TYPE_ID + ":" + SECRET_NAME;

    private SecretManager secretManager;
    private SecretResolveManager secretResolveManager;
    private WebhookSecretProcessor webhookSecretProcessor;

    @BeforeClass
    public void setUpClass() {

        webhookSecretProcessor = new WebhookSecretProcessor();
    }

    @BeforeMethod
    public void setUp() throws SecretManagementException {

        secretManager = mock(SecretManager.class);
        secretResolveManager = mock(SecretResolveManager.class);

        WebhookManagementComponentServiceHolder.getInstance().setSecretManager(secretManager);
        WebhookManagementComponentServiceHolder.getInstance().setSecretResolveManager(secretResolveManager);

        SecretType secretType = mock(SecretType.class);
        when(secretType.getId()).thenReturn(TEST_SECRET_TYPE_ID);
        when(secretManager.getSecretType(any())).thenReturn(secretType);
    }

    @Test
    public void testEncryptAssociatedSecrets_AddNewSecret() throws SecretManagementException {

        when(secretManager.isSecretExist(TEST_SECRET_TYPE_ID, SECRET_NAME)).thenReturn(false);

        String secretName = webhookSecretProcessor.encryptAssociatedSecrets(TEST_WEBHOOK_ID, TEST_SECRET);

        Assert.assertEquals(secretName, SECRET_REFERENCE);
        verify(secretManager, times(1)).addSecret(eq(TEST_SECRET_TYPE_ID), any(Secret.class));
    }

    @Test
    public void testEncryptAssociatedSecrets_UpdateExistingSecret() throws SecretManagementException {

        when(secretManager.isSecretExist(TEST_SECRET_TYPE_ID, SECRET_NAME)).thenReturn(true);

        ResolvedSecret resolvedSecret = mock(ResolvedSecret.class);
        when(resolvedSecret.getResolvedSecretValue()).thenReturn("old-secret");
        when(secretResolveManager.getResolvedSecret(TEST_SECRET_TYPE_ID, SECRET_NAME)).thenReturn(resolvedSecret);

        String secretName = webhookSecretProcessor.encryptAssociatedSecrets(TEST_WEBHOOK_ID, TEST_SECRET);

        Assert.assertEquals(secretName, SECRET_REFERENCE);
        verify(secretManager, times(1)).updateSecretValue(TEST_SECRET_TYPE_ID, SECRET_NAME, TEST_SECRET);
    }

    @Test
    public void testEncryptAssociatedSecrets_NoUpdateIfSameSecret() throws SecretManagementException {

        when(secretManager.isSecretExist(TEST_SECRET_TYPE_ID, SECRET_NAME)).thenReturn(true);

        ResolvedSecret resolvedSecret = mock(ResolvedSecret.class);
        when(resolvedSecret.getResolvedSecretValue()).thenReturn(TEST_SECRET);
        when(secretResolveManager.getResolvedSecret(TEST_SECRET_TYPE_ID, SECRET_NAME)).thenReturn(resolvedSecret);

        String secretName = webhookSecretProcessor.encryptAssociatedSecrets(TEST_WEBHOOK_ID, TEST_SECRET);

        Assert.assertEquals(secretName, SECRET_REFERENCE);
        verify(secretManager, never()).updateSecretValue(any(), any(), any());
    }

    @Test
    public void testDecryptAssociatedSecrets() throws SecretManagementException, WebhookMgtException {

        when(secretManager.isSecretExist(TEST_SECRET_TYPE_ID, SECRET_NAME)).thenReturn(true);

        ResolvedSecret resolvedSecret = mock(ResolvedSecret.class);
        when(resolvedSecret.getResolvedSecretValue()).thenReturn(TEST_SECRET);
        when(secretResolveManager.getResolvedSecret(TEST_SECRET_TYPE_ID, SECRET_NAME)).thenReturn(resolvedSecret);

        String decrypted = webhookSecretProcessor.decryptAssociatedSecrets(TEST_WEBHOOK_ID);

        Assert.assertEquals(decrypted, TEST_SECRET);
    }

    @Test(expectedExceptions = WebhookMgtException.class)
    public void testDecryptAssociatedSecrets_SecretNotExist() throws SecretManagementException, WebhookMgtException {

        when(secretManager.isSecretExist(TEST_SECRET_TYPE_ID, SECRET_NAME)).thenReturn(false);

        webhookSecretProcessor.decryptAssociatedSecrets(TEST_WEBHOOK_ID);
    }

    @Test
    public void testDeleteAssociatedSecrets() throws SecretManagementException {

        when(secretManager.isSecretExist(TEST_SECRET_TYPE_ID, SECRET_NAME)).thenReturn(true);
        doNothing().when(secretManager).deleteSecret(TEST_SECRET_TYPE_ID, SECRET_NAME);

        webhookSecretProcessor.deleteAssociatedSecrets(TEST_WEBHOOK_ID);

        verify(secretManager, times(1)).deleteSecret(TEST_SECRET_TYPE_ID, SECRET_NAME);
    }

    @Test
    public void testDeleteAssociatedSecrets_SecretNotExist() throws SecretManagementException {

        when(secretManager.isSecretExist(TEST_SECRET_TYPE_ID, SECRET_NAME)).thenReturn(false);

        webhookSecretProcessor.deleteAssociatedSecrets(TEST_WEBHOOK_ID);

        verify(secretManager, never()).deleteSecret(any(), any());
    }

    @Test
    public void testGetSecretWithSecretReferences() throws SecretManagementException {

        String reference = webhookSecretProcessor.getSecretWithSecretReferences(TEST_WEBHOOK_ID);

        Assert.assertEquals(reference, SECRET_REFERENCE);
    }
}
