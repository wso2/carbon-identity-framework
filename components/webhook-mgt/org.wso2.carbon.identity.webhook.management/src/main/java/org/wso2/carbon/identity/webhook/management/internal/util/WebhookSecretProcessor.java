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

package org.wso2.carbon.identity.webhook.management.internal.util;

import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtServerException;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;

/**
 * Webhook secrets processor service implementation.
 */
public class WebhookSecretProcessor {

    private static final String IDN_SECRET_TYPE_WEBHOOK_SECRETS = "WEBHOOK_SECRETS";
    private static final String ENDPOINT_PREFIX = "ENDPOINT";
    private static final String SECRET_SUFFIX = "SECRET";

    public String encryptAssociatedSecrets(String webhookId, String secret)
            throws SecretManagementException {

        return encryptProperty(webhookId, secret);
    }

    public String decryptAssociatedSecrets(String webhookId) throws WebhookMgtServerException {

        try {
            return decryptProperty(webhookId);
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DECRYPTION_ERROR, e, webhookId);
        }
    }

    public void deleteAssociatedSecrets(String webhookId)
            throws SecretManagementException {

        String secretName = buildSecretName(webhookId);
        if (isSecretPropertyExists(secretName)) {
            WebhookManagementComponentServiceHolder.getInstance().getSecretManager()
                    .deleteSecret(IDN_SECRET_TYPE_WEBHOOK_SECRETS, secretName);
        }
    }

    public String getSecretWithSecretReferences(String webhookId) throws SecretManagementException {

        return buildSecretReference(buildSecretName(webhookId));
    }

    /**
     * Encrypt secret property.
     *
     * @param webhookId Webhook ID.
     * @param secret    Secret to be encrypted.
     * @return Encrypted secret name.
     * @throws SecretManagementException If an error occurs while encrypting the secret.
     */
    private String encryptProperty(String webhookId, String secret)
            throws SecretManagementException {

        String secretName = buildSecretName(webhookId);
        if (isSecretPropertyExists(secretName)) {
            updateExistingSecretProperty(secretName, secret);
        } else {
            addNewWebhookSecretProperty(secretName, secret);
        }

        return buildSecretReference(secretName);
    }

    /**
     * Decrypt secret property.
     *
     * @param webhookId Webhook ID.
     * @return Decrypted secret value.
     * @throws SecretManagementException If an error occurs while decrypting the secret.
     */
    private String decryptProperty(String webhookId)
            throws SecretManagementException {

        String secretName = buildSecretName(webhookId);
        if (!isSecretPropertyExists(secretName)) {
            throw new SecretManagementException(String.format("Unable to find the Secret of " +
                    "Webhook ID: %s from the system.", webhookId));
        }
        ResolvedSecret resolvedSecret = WebhookManagementComponentServiceHolder.getInstance().getSecretResolveManager()
                .getResolvedSecret(IDN_SECRET_TYPE_WEBHOOK_SECRETS, secretName);

        return resolvedSecret.getResolvedSecretValue();
    }

    /**
     * Check if the secret exists.
     *
     * @param secretName Name of the secret.
     * @return True if the secret exists, false otherwise.
     * @throws SecretManagementException If an error occurs while checking the existence of the secret.
     */
    private boolean isSecretPropertyExists(String secretName) throws SecretManagementException {

        return WebhookManagementComponentServiceHolder.getInstance().getSecretManager()
                .isSecretExist(IDN_SECRET_TYPE_WEBHOOK_SECRETS, secretName);
    }

    /**
     * Build secret name for the webhook.
     *
     * @param webhookId Webhook ID.
     * @return Secret name.
     */
    private String buildSecretName(String webhookId) {

        return webhookId + ":" + ENDPOINT_PREFIX + ":" + SECRET_SUFFIX;
    }

    /**
     * Build secret reference for the webhook.
     *
     * @param secretName Name of the secret.
     * @return Secret reference.
     * @throws SecretManagementException If an error occurs while building the secret reference.
     */
    private String buildSecretReference(String secretName) throws SecretManagementException {

        SecretType secretType = WebhookManagementComponentServiceHolder.getInstance().getSecretManager()
                .getSecretType(IDN_SECRET_TYPE_WEBHOOK_SECRETS);
        return secretType.getId() + ":" + secretName;
    }

    /**
     * Add a new secret of Webhook secret type.
     *
     * @param secretName  Name of the secret.
     * @param secretValue Secret value.
     * @throws SecretManagementException If an error occurs while adding the secret.
     */
    private void addNewWebhookSecretProperty(String secretName, String secretValue) throws SecretManagementException {

        Secret secret = new Secret();
        secret.setSecretName(secretName);
        secret.setSecretValue(secretValue);
        WebhookManagementComponentServiceHolder.getInstance().getSecretManager()
                .addSecret(IDN_SECRET_TYPE_WEBHOOK_SECRETS,
                        secret);
    }

    /**
     * Update an existing secret property.
     *
     * @param secretName  Name of the secret.
     * @param secretValue Secret value.
     * @throws SecretManagementException If an error occurs while updating the secret.
     */
    private void updateExistingSecretProperty(String secretName, String secretValue)
            throws SecretManagementException {

        ResolvedSecret resolvedSecret = WebhookManagementComponentServiceHolder.getInstance().getSecretResolveManager()
                .getResolvedSecret(IDN_SECRET_TYPE_WEBHOOK_SECRETS, secretName);
        if (!resolvedSecret.getResolvedSecretValue().equals(secretValue)) {
            WebhookManagementComponentServiceHolder.getInstance().getSecretManager()
                    .updateSecretValue(IDN_SECRET_TYPE_WEBHOOK_SECRETS, secretName, secretValue);
        }
    }
}
