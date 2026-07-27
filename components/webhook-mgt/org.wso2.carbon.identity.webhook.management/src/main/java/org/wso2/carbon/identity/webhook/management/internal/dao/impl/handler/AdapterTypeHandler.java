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

package org.wso2.carbon.identity.webhook.management.internal.dao.impl.handler;

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookRunnable;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookSecretProcessor;

import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_ENCRYPTION_ERROR;

/**
 * Abstract class for handling webhook management operations with secret processing.
 * This class provides methods to encrypt, decrypt, and delete webhook secrets.
 */
public abstract class AdapterTypeHandler implements WebhookManagementDAO {

    private final WebhookSecretProcessor webhookSecretProcessor = new WebhookSecretProcessor();

    /**
     * Encrypts the webhook secret and adds it to the webhook object.
     *
     * @param webhook Webhook object.
     * @return Webhook with encrypted secrets.
     * @throws WebhookMgtException If an error occurs while encrypting the webhook secrets.
     */
    protected Webhook encryptAddingWebhookSecrets(Webhook webhook) throws WebhookMgtException {

        try {
            String encryptedSecretAlias =
                    webhookSecretProcessor.encryptAssociatedSecrets(webhook.getId(), webhook.getSecret());

            return addSecretOrAliasToBuilder(webhook, encryptedSecretAlias);
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_ENCRYPTION_ERROR, e, webhook.getId());
        }
    }

    /**
     * Deletes the webhook secrets associated with the given webhook.
     *
     * @param webhook Webhook object.
     * @throws WebhookMgtException If an error occurs while deleting the webhook secrets.
     */
    protected void deleteWebhookSecrets(Webhook webhook) throws WebhookMgtException {

        try {
            webhookSecretProcessor.deleteAssociatedSecrets(webhook.getId());
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DELETE_ERROR, e, webhook.getId());
        }
    }

    // --- Utility methods ---

    protected void runTransaction(NamedJdbcTemplate jdbcTemplate, WebhookRunnable action, ErrorMessage errorMessage)
            throws WebhookMgtException {

        try {
            jdbcTemplate.withTransaction(template -> {
                action.run();
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(errorMessage, e);
        }
    }

    protected String getWebhookDecryptedSecretValue(String webhookId) throws WebhookMgtException {

        return webhookSecretProcessor.decryptAssociatedSecrets(webhookId);
    }

    private Webhook addSecretOrAliasToBuilder(Webhook webhook, String secretAlias) throws WebhookMgtException {

        return new Webhook.Builder().uuid(webhook.getId()).endpoint(webhook.getEndpoint()).name(webhook.getName())
                .secret(secretAlias).eventProfileName(webhook.getEventProfileName())
                .eventProfileUri(webhook.getEventProfileUri()).eventProfileVersion(webhook.getEventProfileVersion())
                .status(webhook.getStatus())
                .createdAt(webhook.getCreatedAt()).updatedAt(webhook.getUpdatedAt())
                .eventsSubscribed(webhook.getEventsSubscribed()).build();
    }
}
