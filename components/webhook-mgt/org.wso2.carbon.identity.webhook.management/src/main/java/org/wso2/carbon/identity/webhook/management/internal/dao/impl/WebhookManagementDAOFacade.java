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
import org.wso2.carbon.identity.webhook.management.api.model.subscription.Subscription;
import org.wso2.carbon.identity.webhook.management.api.model.subscription.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.model.webhook.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.webhook.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.EventSubscriberService;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookSecretProcessor;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        try {
            ensureTopicsExist(webhook.getEventsSubscribed(), tenantDomain);
        } catch (TopicManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR, e);
        }

        Webhook webhookToPersist;
        if (webhook.getStatus() == WebhookStatus.ACTIVE) {
            List<Subscription> subscriptions = subscriberService.subscribe(
                    ADAPTOR, webhook.getEventsSubscribed(), EVENT_PROFILE_VERSION,
                    webhook.getEndpoint(), webhook.getSecret(), tenantDomain);

            boolean allError = subscriptions.stream()
                    .allMatch(s -> s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR);

            if (allError) {
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR);
            } else {
                webhookToPersist = new Webhook.Builder()
                        .uuid(webhook.getUuid())
                        .endpoint(webhook.getEndpoint())
                        .name(webhook.getName())
                        .secret(webhook.getSecret())
                        .tenantId(webhook.getTenantId())
                        .eventProfileName(webhook.getEventProfileName())
                        .eventProfileUri(webhook.getEventProfileUri())
                        .status(WebhookStatus.PENDING_ACTIVATION)
                        .createdAt(webhook.getCreatedAt())
                        .updatedAt(webhook.getUpdatedAt())
                        .eventsSubscribed(subscriptions)
                        .build();
            }
        } else {
            webhookToPersist = webhook;
        }

        runTransaction(jdbcTemplate,
                () -> webhookManagementDAO.createWebhook(encryptAddingWebhookSecrets(webhookToPersist), tenantId),
                ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR,
                "creating webhook: " + webhook.getUuid() + " in tenant ID: " + tenantId);
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
    public List<Subscription> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.getWebhookEvents(webhookId, tenantId);
    }

    @Override
    public void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        runTransaction(jdbcTemplate, () -> {
                    Webhook existingWebhook = webhookManagementDAO.getWebhook(webhookId, tenantId);
                    webhookManagementDAO.deleteWebhook(webhookId, tenantId);
                    deleteWebhookSecrets(existingWebhook);
                }, ErrorMessage.ERROR_CODE_WEBHOOK_DELETE_ERROR,
                "deleting webhook: " + webhookId + " in tenant ID: " + tenantId);
    }

    @Override
    public boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.isWebhookEndpointExists(endpoint, tenantId);
    }

    @Override
    public void activateWebhook(String webhookId, int tenantId, List<Subscription> channels, WebhookStatus status,
                                String webhookEndpoint) throws WebhookMgtException {

        processSubscriptions(
                webhookId, tenantId, channels, webhookEndpoint,
                SubscriptionActionType.SUBSCRIBE,
                s -> s.getStatus() == SubscriptionStatus.SUBSCRIPTION_PENDING
                        || s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR
                        || s.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ACCEPTED,
                ErrorMessage.ERROR_CODE_WEBHOOK_ACTIVATION_ERROR,
                updatedSubs -> webhookManagementDAO.deactivateWebhook(
                        webhookId, tenantId, updatedSubs, WebhookStatus.PENDING_ACTIVATION, webhookEndpoint),
                true
        );
    }

    @Override
    public void deactivateWebhook(String webhookId, int tenantId, List<Subscription> channels, WebhookStatus status,
                                  String webhookEndpoint) throws WebhookMgtException {

        processSubscriptions(
                webhookId, tenantId, channels, webhookEndpoint,
                SubscriptionActionType.UNSUBSCRIBE,
                s -> s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ACCEPTED,
                ErrorMessage.ERROR_CODE_WEBHOOK_DEACTIVATION_ERROR,
                updatedSubs -> webhookManagementDAO.deactivateWebhook(
                        webhookId, tenantId, updatedSubs, WebhookStatus.PENDING_DEACTIVATION, webhookEndpoint),
                false
        );
    }

    @Override
    public void retryWebhook(String webhookId, int tenantId, List<Subscription> channels, WebhookStatus status,
                             String webhookEndpoint) throws WebhookMgtException {

        if (status == WebhookStatus.PENDING_ACTIVATION) {
            processSubscriptions(
                    webhookId, tenantId, channels, webhookEndpoint,
                    SubscriptionActionType.SUBSCRIBE,
                    s -> s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR
                            || s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ACCEPTED,
                    ErrorMessage.ERROR_CODE_WEBHOOK_RETRY_ERROR,
                    updatedSubs -> webhookManagementDAO.retryWebhook(
                            webhookId, tenantId, updatedSubs, WebhookStatus.PENDING_ACTIVATION, webhookEndpoint),
                    true
            );
        } else if (status == WebhookStatus.PENDING_DEACTIVATION) {
            processSubscriptions(
                    webhookId, tenantId, channels, webhookEndpoint,
                    SubscriptionActionType.UNSUBSCRIBE,
                    s -> s.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ACCEPTED
                            || s.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ERROR,
                    ErrorMessage.ERROR_CODE_WEBHOOK_RETRY_ERROR,
                    updatedSubs -> webhookManagementDAO.retryWebhook(
                            webhookId, tenantId, updatedSubs, WebhookStatus.PENDING_DEACTIVATION, webhookEndpoint),
                    false
            );
        }
    }

    // --- Helper for subscription/unsubscription processing ---

    private enum SubscriptionActionType { SUBSCRIBE, UNSUBSCRIBE }

    @FunctionalInterface
    private interface SubscriptionUpdater {

        void update(List<Subscription> updatedSubscriptions) throws WebhookMgtException;
    }

    private void processSubscriptions(String webhookId, int tenantId, List<Subscription> channels,
                                      String webhookEndpoint, SubscriptionActionType actionType,
                                      Predicate<Subscription> filterPredicate, ErrorMessage errorMessage,
                                      SubscriptionUpdater updater, boolean isSubscribe) throws WebhookMgtException {

        EventSubscriberService subscriberService = getSubscriberService();
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);

        List<Subscription> filtered = channels.stream()
                .filter(filterPredicate)
                .collect(Collectors.toList());

        List<Subscription> allResults;
        if (actionType == SubscriptionActionType.SUBSCRIBE) {
            allResults = subscriberService.subscribe(
                    ADAPTOR, filtered, EVENT_PROFILE_VERSION,
                    webhookEndpoint, getWebhookDecryptedSecretValue(webhookId), tenantDomain);
        } else {
            allResults = subscriberService.unsubscribe(
                    ADAPTOR, filtered, EVENT_PROFILE_VERSION,
                    webhookEndpoint, tenantDomain);
        }

        boolean allError = !allResults.isEmpty() && allResults.stream()
                .allMatch(r -> (actionType == SubscriptionActionType.SUBSCRIBE)
                        ? r.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR
                        : r.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ERROR);

        if (allError) {
            if (isSubscribe) {
                throw WebhookManagementExceptionHandler.handleClientException(errorMessage);
            } else {
                throw WebhookManagementExceptionHandler.handleServerException(errorMessage, webhookId);
            }
        } else {
            List<Subscription> updatedSubscriptions = channels.stream()
                    .map(sub -> allResults.stream()
                            .filter(r -> r.getChannelUri().equals(sub.getChannelUri()))
                            .findFirst()
                            .orElse(sub))
                    .collect(Collectors.toList());
            updater.update(updatedSubscriptions);
        }
    }

    /**
     * Not implemented yet.
     * Update a webhook subscription in the database.
     */
    @Override
    public void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        LOG.debug("Update webhook operation is not implemented yet. " +
                "Please use createWebhook method to update the webhook: " + webhook.getUuid());
    }

    private Webhook encryptAddingWebhookSecrets(Webhook webhook) throws WebhookMgtException {

        try {
            String encryptedSecretAlias = webhookSecretProcessor.encryptAssociatedSecrets(
                    webhook.getUuid(), webhook.getSecret());
            return addSecretOrAliasToBuilder(webhook, encryptedSecretAlias);
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_ENCRYPTION_ERROR, e, webhook.getUuid());
        }
    }

    private void deleteWebhookSecrets(Webhook webhook) throws WebhookMgtException {

        try {
            webhookSecretProcessor.deleteAssociatedSecrets(webhook.getUuid());
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DELETE_ERROR, e, webhook.getUuid());
        }
    }

    // --- Helper methods below ---

    private EventSubscriberService getSubscriberService() {

        return WebhookManagementComponentServiceHolder.getInstance().getEventSubscriberService();
    }

    private void ensureTopicsExist(List<Subscription> events, String tenantDomain) throws TopicManagementException {

        TopicManagementService topicManagementService =
                WebhookManagementComponentServiceHolder.getInstance().getTopicManagementService();
        for (Subscription event : events) {
            if (!topicManagementService.isTopicExists(event.getChannelUri(), EVENT_PROFILE_VERSION, tenantDomain)) {
                topicManagementService.registerTopic(event.getChannelUri(), EVENT_PROFILE_VERSION, tenantDomain);
            }
        }
    }

    // --- Utility methods ---

    @FunctionalInterface
    private interface WebhookRunnable {

        void run() throws WebhookMgtException;
    }

    private void runTransaction(NamedJdbcTemplate jdbcTemplate, WebhookRunnable action, ErrorMessage errorMessage,
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

    private Webhook addSecretOrAliasToBuilder(Webhook webhook, String secretAlias)
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

    public String getWebhookDecryptedSecretValue(String webhookId) throws WebhookMgtException {

        try {
            return webhookSecretProcessor.decryptAssociatedSecrets(webhookId);
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DECRYPTION_ERROR, e, webhookId);
        }
    }
}
