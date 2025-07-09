/*
Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).

WSO2 LLC. licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file except
in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */

package org.wso2.carbon.identity.webhook.management.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookAdaptorType;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookRunnable;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.adaptor.handler.PublisherAdaptorHandler;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.adaptor.handler.PublisherSubscriberAdaptorHandler;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookSecretProcessor;

import java.util.List;

import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_ENCRYPTION_ERROR;

/**
 * Facade for WebhookManagementDAO to handle webhook management operations.
 */
public class WebhookManagementDAOFacade implements WebhookManagementDAO {

    private static final Log LOG = LogFactory.getLog(WebhookManagementDAOFacade.class);
    private final WebhookSecretProcessor webhookSecretProcessor;
    private final WebhookManagementDAO publisherSubscriberHandler;
    private final WebhookManagementDAO publisherHandler;

    public WebhookManagementDAOFacade(WebhookManagementDAO webhookManagementDAO) {

        this.webhookSecretProcessor = new WebhookSecretProcessor();
        this.publisherSubscriberHandler = new PublisherSubscriberAdaptorHandler(webhookManagementDAO, this);
        this.publisherHandler = new PublisherAdaptorHandler(webhookManagementDAO, this);
    }

    private WebhookManagementDAO getHandler() throws WebhookMgtException {

        WebhookAdaptorType type = WebhookManagementComponentServiceHolder.getInstance().getWebhookAdaptorType();
        if (WebhookAdaptorType.PublisherSubscriber.equals(type)) {
            return publisherSubscriberHandler;
        } else if (WebhookAdaptorType.Publisher.equals(type)) {
            return publisherHandler;
        } else {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_OPERATION_NOT_SUPPORTED, String.valueOf(type));
        }
    }

    @Override
    public void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().createWebhook(webhook, tenantId);
    }

    @Override
    public List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException {

        return getHandler().getWebhooks(tenantId);
    }

    @Override
    public Webhook getWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        return getHandler().getWebhook(webhookId, tenantId);
    }

    @Override
    public List<Subscription> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException {

        return getHandler().getWebhookEvents(webhookId, tenantId);
    }

    @Override
    public void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        getHandler().deleteWebhook(webhookId, tenantId);
    }

    @Override
    public boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException {

        return getHandler().isWebhookEndpointExists(endpoint, tenantId);
    }

    @Override
    public void activateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().activateWebhook(webhook, tenantId);
    }

    @Override
    public void activateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        getHandler().activateWebhook(webhookId, tenantId);
    }

    @Override
    public void deactivateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().deactivateWebhook(webhook, tenantId);
    }

    @Override
    public void deactivateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        getHandler().deactivateWebhook(webhookId, tenantId);
    }

    @Override
    public void retryWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().retryWebhook(webhook, tenantId);
    }

    @Override
    public int getWebhooksCount(int tenantId) throws WebhookMgtException {

        return getHandler().getWebhooksCount(tenantId);
    }

    @Override
    public void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        getHandler().updateWebhook(webhook, tenantId);
    }

    // --- Helper methods below ---

    /**
     * Encrypts the webhook secret and adds it to the webhook object.
     *
     * @param webhook Webhook object.
     * @return Webhook with encrypted secrets.
     * @throws WebhookMgtException If an error occurs while encrypting the webhook secrets.
     */
    public Webhook encryptAddingWebhookSecrets(Webhook webhook) throws WebhookMgtException {

        try {
            String encryptedSecretAlias =
                    webhookSecretProcessor.encryptAssociatedSecrets(webhook.getId(), webhook.getSecret());

            return addSecretOrAliasToBuilder(webhook, encryptedSecretAlias);
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_ENCRYPTION_ERROR, e, webhook.getId());
        }
    }

    public void deleteWebhookSecrets(Webhook webhook) throws WebhookMgtException {

        try {
            webhookSecretProcessor.deleteAssociatedSecrets(webhook.getId());
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DELETE_ERROR, e, webhook.getId());
        }
    }

    // --- Utility methods ---

    public void runTransaction(NamedJdbcTemplate jdbcTemplate, WebhookRunnable action, ErrorMessage errorMessage,
                               String debugMsg) throws WebhookMgtException {

        try {
            jdbcTemplate.withTransaction(template -> {
                action.run();
                return null;
            });
        } catch (TransactionException e) {
            if (debugMsg != null) {
                LOG.debug("Error " + debugMsg, e);
            }
            throw WebhookManagementExceptionHandler.handleServerException(errorMessage, e);
        }
    }

    public String getWebhookDecryptedSecretValue(String webhookId) throws WebhookMgtException {

        try {
            return webhookSecretProcessor.decryptAssociatedSecrets(webhookId);
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DECRYPTION_ERROR, e, webhookId);
        }
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
