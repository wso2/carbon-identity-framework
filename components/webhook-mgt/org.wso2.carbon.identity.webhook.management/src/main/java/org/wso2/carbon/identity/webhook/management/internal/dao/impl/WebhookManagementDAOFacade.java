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

package org.wso2.carbon.identity.webhook.management.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.EventSubscriberService;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookSecretProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_ENCRYPTION_ERROR;

/**
 * Facade for WebhookManagementDAO to handle webhook management operations.
 */
public class WebhookManagementDAOFacade implements WebhookManagementDAO {

    private static final Log LOG = LogFactory.getLog(WebhookManagementDAOFacade.class);
    private static final String EVENT_PROFILE_VERSION = "v1";
    private static final String ADAPTOR = "webSubHubAdapter";
    private final WebhookManagementDAO webhookManagementDAO;
    private final WebhookSecretProcessor webhookSecretProcessor;

    public WebhookManagementDAOFacade(WebhookManagementDAO webhookManagementDAO) {

        this.webhookManagementDAO = webhookManagementDAO;
        this.webhookSecretProcessor = new WebhookSecretProcessor();
    }

    @Override
    public void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        EventSubscriberService subscriberService = getSubscriberService();

        withTopicRegistrationIfActive(jdbcTemplate, webhook, tenantDomain, tenantId,
                ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR);

        if (webhook.getStatus() == WebhookStatus.ACTIVE) {
            safeSubscribe(subscriberService, webhook, tenantDomain, webhook.getEventsSubscribed(),
                    ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR);
        }

        runTransaction(jdbcTemplate,
                () -> webhookManagementDAO.createWebhook(encryptAddingWebhookSecrets(webhook), tenantId),
                ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR,
                "creating webhook: " + webhook.getUuid() + " in tenant ID: " + tenantId, null);
    }

    @Override
    public List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.getWebhooks(tenantId);
    }

    @Override
    public Webhook getWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.getWebhook(webhookId, tenantId);
    }

    @Override
    public List<String> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.getWebhookEvents(webhookId, tenantId);
    }

    @Override
    public void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        EventSubscriberService subscriberService = getSubscriberService();

        withTopicRegistrationIfActive(jdbcTemplate, webhook, tenantDomain, tenantId,
                ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR);

        Webhook existingWebhook =
                getWebhookWithDecryptedSecretValue(webhookManagementDAO.getWebhook(webhook.getUuid(), tenantId));
        handleWebhookUpdate(subscriberService, webhook, existingWebhook, tenantDomain);

        runTransaction(jdbcTemplate,
                () -> webhookManagementDAO.updateWebhook(encryptAddingWebhookSecrets(webhook), tenantId),
                ErrorMessage.ERROR_CODE_WEBHOOK_UPDATE_ERROR,
                "updating webhook: " + webhook.getUuid() + " in tenant ID: " + tenantId, () -> {
                    if (WebhookStatus.ACTIVE.equals(webhook.getStatus())) {
                        unsubscribeWebhook(subscriberService, webhook, tenantDomain, webhook.getEventsSubscribed());
                    }
                });
    }

    @Override
    public void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        EventSubscriberService subscriberService = getSubscriberService();

        runTransaction(jdbcTemplate, () -> {
                    Webhook existingWebhook = webhookManagementDAO.getWebhook(webhookId, tenantId);
                    try {
                        unsubscribeWebhook(subscriberService, existingWebhook, tenantDomain,
                                existingWebhook.getEventsSubscribed());
                    } catch (WebhookMgtException e) {
                        LOG.warn("Error unsubscribing webhook during deletion: " + existingWebhook.getUuid(), e);
                    }
                    webhookManagementDAO.deleteWebhook(webhookId, tenantId);
                    deleteAuthenticationSecrets(existingWebhook);
                }, ErrorMessage.ERROR_CODE_WEBHOOK_DELETE_ERROR,
                "deleting webhook: " + webhookId + " in tenant ID: " + tenantId, null);
    }

    @Override
    public boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.isWebhookEndpointExists(endpoint, tenantId);
    }

    @Override
    public void activateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        toggleWebhookStatus(webhookId, tenantId, true);
    }

    @Override
    public void deactivateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        toggleWebhookStatus(webhookId, tenantId, false);
    }

    /**
     * Encrypts the webhook secret and adds it to the webhook object.
     *
     * @param webhook Webhook object.
     * @return Webhook with encrypted secrets.
     * @throws WebhookMgtException If an error occurs while encrypting the webhook secrets.
     */
    private Webhook encryptAddingWebhookSecrets(Webhook webhook) throws WebhookMgtException {

        try {
            String encryptedSecretAlias = webhookSecretProcessor.encryptAssociatedSecrets(
                    webhook.getUuid(), webhook.getSecret());

            return addSecretToBuilder(webhook, encryptedSecretAlias);
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_ENCRYPTION_ERROR, e, webhook.getUuid());
        }
    }

    /**
     * Deletes the authentication secrets associated with the webhook.
     *
     * @param webhook Webhook object.
     * @throws WebhookMgtException If an error occurs while deleting the authentication secrets.
     */
    private void deleteAuthenticationSecrets(Webhook webhook) throws WebhookMgtException {

        try {
            webhookSecretProcessor.deleteAssociatedSecrets(webhook.getUuid());
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DELETE_ERROR, e, webhook.getUuid());
        }
    }

    public Webhook getWebhookWithDecryptedSecretValue(Webhook webhook) throws WebhookMgtException {

        try {
            String decryptedSecret = webhookSecretProcessor.decryptAssociatedSecrets(webhook.getUuid());
            return addSecretToBuilder(webhook, decryptedSecret);
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DECRYPTION_ERROR, e, webhook.getUuid());
        }
    }

    private void toggleWebhookStatus(String webhookId, int tenantId, boolean activate) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        EventSubscriberService subscriberService = getSubscriberService();
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        Webhook existingWebhook = webhookManagementDAO.getWebhook(webhookId, tenantId);

        if ((activate && existingWebhook.getStatus() == WebhookStatus.ACTIVE) ||
                (!activate && existingWebhook.getStatus() == WebhookStatus.INACTIVE)) {
            LOG.debug("Webhook with ID: " + webhookId + " is already " + (activate ? "active" : "inactive") +
                    " in tenant ID: " + tenantId);
            return;
        }

        try {
            if (activate) {
                subscribeWebhook(subscriberService, existingWebhook, tenantDomain,
                        existingWebhook.getEventsSubscribed());
            } else {
                unsubscribeWebhook(subscriberService, existingWebhook, tenantDomain,
                        existingWebhook.getEventsSubscribed());
            }
        } catch (WebhookMgtException e) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    activate ? ErrorMessage.ERROR_CODE_WEBHOOK_ACTIVATION_ERROR :
                            ErrorMessage.ERROR_CODE_WEBHOOK_DEACTIVATION_ERROR, webhookId);
        }

        runTransaction(jdbcTemplate, () -> {
                    if (activate) {
                        webhookManagementDAO.activateWebhook(webhookId, tenantId);
                    } else {
                        webhookManagementDAO.deactivateWebhook(webhookId, tenantId);
                    }
                }, activate ? ErrorMessage.ERROR_CODE_WEBHOOK_ACTIVATION_ERROR :
                        ErrorMessage.ERROR_CODE_WEBHOOK_DEACTIVATION_ERROR,
                (activate ? "activating" : "deactivating") + " webhook: " + webhookId + " in tenant ID: " + tenantId,
                () -> {
                    try {
                        if (activate) {
                            unsubscribeWebhook(subscriberService, existingWebhook, tenantDomain,
                                    existingWebhook.getEventsSubscribed());
                        } else {
                            subscribeWebhook(subscriberService, existingWebhook, tenantDomain,
                                    existingWebhook.getEventsSubscribed());
                        }
                    } catch (WebhookMgtException ignore) {
                        // Already logged in subscribe/unsubscribe
                    }
                });
    }

    // --- Helper methods below ---

    private EventSubscriberService getSubscriberService() {

        return WebhookManagementComponentServiceHolder.getInstance().getEventSubscriberService();
    }

    private void ensureTopicsExist(List<String> events, String tenantDomain) throws TopicManagementException {

        TopicManagementService topicManagementService =
                WebhookManagementComponentServiceHolder.getInstance().getTopicManagementService();
        for (String event : events) {
            if (!topicManagementService.isTopicExists(event, EVENT_PROFILE_VERSION, tenantDomain)) {
                topicManagementService.registerTopic(event, EVENT_PROFILE_VERSION, tenantDomain);
            }
        }
    }

    private void rollbackTopicsExist(List<String> events, String tenantDomain) throws TopicManagementException {

        TopicManagementService topicManagementService =
                WebhookManagementComponentServiceHolder.getInstance().getTopicManagementService();
        for (String event : events) {
            if (!topicManagementService.isTopicExists(event, EVENT_PROFILE_VERSION, tenantDomain)) {
                topicManagementService.deregisterTopic(event, EVENT_PROFILE_VERSION, tenantDomain);
            }
        }
    }

    private void subscribeWebhook(EventSubscriberService subscriberService, Webhook webhook, String tenantDomain,
                                  List<String> events) throws WebhookMgtException {

        subscriberService.subscribe(webhook.getUuid(), ADAPTOR, events, EVENT_PROFILE_VERSION, webhook.getEndpoint(),
                webhook.getSecret(), tenantDomain);
    }

    private void unsubscribeWebhook(EventSubscriberService subscriberService, Webhook webhook, String tenantDomain,
                                    List<String> events) throws WebhookMgtException {

        subscriberService.unsubscribe(webhook.getUuid(), ADAPTOR, events, EVENT_PROFILE_VERSION, webhook.getEndpoint(),
                tenantDomain);
    }

    private void handleWebhookUpdate(EventSubscriberService subscriberService, Webhook newWebhook, Webhook oldWebhook,
                                     String tenantDomain) throws WebhookMgtException {

        List<String> oldEvents = oldWebhook.getEventsSubscribed();
        List<String> newEvents = newWebhook.getEventsSubscribed();
        Set<String> oldSet = new HashSet<>(oldEvents);
        Set<String> newSet = new HashSet<>(newEvents);

        if (WebhookStatus.ACTIVE.equals(newWebhook.getStatus())) {
            if (!newWebhook.getStatus().equals(oldWebhook.getStatus())) {
                safeSubscribe(subscriberService, oldWebhook, tenantDomain, oldEvents,
                        ErrorMessage.ERROR_CODE_WEBHOOK_UPDATE_ERROR);
            } else if (!oldSet.equals(newSet) || !Objects.equals(newWebhook.getEndpoint(), oldWebhook.getEndpoint()) ||
                    !newWebhook.getSecret().equals(oldWebhook.getSecret())) {
                safeUnsubscribe(subscriberService, oldWebhook, tenantDomain, oldWebhook.getEventsSubscribed(),
                        ErrorMessage.ERROR_CODE_WEBHOOK_UPDATE_ERROR);
                try {
                    //TODO: Onboard polling mechanism to verify the existing subscriber status
                    Thread.sleep(2000); // Wait for 2 seconds to ensure unsubscription is processed
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.warn("Thread interrupted while waiting for unsubscription to complete.", e);
                }
                try {
                    subscribeWebhook(subscriberService, newWebhook, tenantDomain, newWebhook.getEventsSubscribed());
                } catch (WebhookMgtException e) {
                    subscribeWebhook(subscriberService, oldWebhook, tenantDomain, oldWebhook.getEventsSubscribed());
                    throw WebhookManagementExceptionHandler.handleServerException(
                            ErrorMessage.ERROR_CODE_WEBHOOK_UPDATE_ERROR, e);
                }
            }
        } else if (!newWebhook.getStatus().equals(oldWebhook.getStatus())) {
            safeUnsubscribe(subscriberService, oldWebhook, tenantDomain,
                    oldWebhook.getEventsSubscribed(), ErrorMessage.ERROR_CODE_WEBHOOK_UPDATE_ERROR);
        }
    }

    /**
     * Helper to register topics if webhook is ACTIVE, with rollback on failure.
     */
    private void withTopicRegistrationIfActive(NamedJdbcTemplate jdbcTemplate, Webhook webhook, String tenantDomain,
                                               int tenantId, ErrorMessage errorMessage) throws WebhookMgtException {

        runTransaction(jdbcTemplate, () -> {
                    if (webhook.getStatus() == WebhookStatus.ACTIVE) {
                        try {
                            ensureTopicsExist(webhook.getEventsSubscribed(), tenantDomain);
                        } catch (TopicManagementException e) {
                            throw WebhookManagementExceptionHandler.handleServerException(errorMessage, e);
                        }
                    }
                }, errorMessage,
                "registering topics for webhook: " + webhook.getUuid() + " in tenant ID: " + tenantId, () -> {
                    if (WebhookStatus.ACTIVE.equals(webhook.getStatus())) {
                        try {
                            rollbackTopicsExist(webhook.getEventsSubscribed(), tenantDomain);
                        } catch (TopicManagementException e) {
                            LOG.error("Error during rollback of topics: ", e);
                        }
                    }
                });
    }

    // --- Utility methods ---

    @FunctionalInterface
    private interface WebhookRunnable {

        void run() throws WebhookMgtException;
    }

    private void runTransaction(NamedJdbcTemplate jdbcTemplate, WebhookRunnable action, ErrorMessage errorMessage,
                                String debugMsg, WebhookRunnable onError) throws WebhookMgtException {

        try {
            jdbcTemplate.withTransaction(template -> {
                action.run();
                return null;
            });
        } catch (TransactionException e) {
            if (debugMsg != null) {
                LOG.debug("Error " + debugMsg, e);
            }
            if (onError != null) {
                try {
                    onError.run();
                } catch (Exception ex) {
                    LOG.error("Error during rollback/cleanup: ", ex);
                }
            }
            throw WebhookManagementExceptionHandler.handleServerException(errorMessage, e);
        }
    }

    private void safeSubscribe(EventSubscriberService subscriberService, Webhook webhook, String tenantDomain,
                               List<String> events, ErrorMessage errorMessage) throws WebhookMgtException {

        try {
            subscribeWebhook(subscriberService, webhook, tenantDomain, events);
        } catch (WebhookMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(errorMessage, e);
        }
    }

    private void safeUnsubscribe(EventSubscriberService subscriberService, Webhook webhook, String tenantDomain,
                                 List<String> events, ErrorMessage errorMessage) throws WebhookMgtException {

        try {
            unsubscribeWebhook(subscriberService, webhook, tenantDomain, events);
        } catch (WebhookMgtException e) {
            throw WebhookManagementExceptionHandler.handleServerException(errorMessage, e);
        }
    }

    /**
     * Adds a secret alias to the builder.
     *
     * @param webhook     Webhook object.
     * @param secretAlias Secret alias (encrypted or plain).
     * @return Webhook with the secret alias added.
     * @throws WebhookMgtException If an error occurs while adding the secret alias.
     */
    private Webhook addSecretToBuilder(Webhook webhook, String secretAlias)
            throws WebhookMgtException {

        return new Webhook.Builder()
                .uuid(webhook.getUuid())
                .endpoint(webhook.getEndpoint())
                .name(webhook.getName())
                .secret(secretAlias)
                .tenantId(webhook.getTenantId())
                .eventProfileName(webhook.getEventProfileName())
                .eventProfileUri(webhook.getEventProfileUri())
                .status(webhook.getStatus())
                .createdAt(webhook.getCreatedAt())
                .updatedAt(webhook.getUpdatedAt())
                .eventsSubscribed(webhook.getEventsSubscribed())
                .build();
    }
}
