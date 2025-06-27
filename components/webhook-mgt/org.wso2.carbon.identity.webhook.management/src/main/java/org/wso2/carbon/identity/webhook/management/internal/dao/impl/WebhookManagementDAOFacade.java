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
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookRunnable;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.EventSubscriberService;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookSecretProcessor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_ENCRYPTION_ERROR;
import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_RETRY_OPERATION_NOT_SUPPORTED;
import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_UPDATE_OPERATION_NOT_SUPPORTED;

/**
 * Facade for WebhookManagementDAO to handle webhook management operations.
 */
public class WebhookManagementDAOFacade implements WebhookManagementDAO {

    private static final Log LOG = LogFactory.getLog(WebhookManagementDAOFacade.class);
    private static final Set<String> UPDATE_NOT_IMPLEMENTED_ADAPTORS = new HashSet<>();
    private static final Set<String> RETRY_NOT_IMPLEMENTED_ADAPTORS = new HashSet<>();
    private static final String WEBSUBHUB_ADAPTOR = "webSubHubAdapter";
    private static final String HTTP_ADAPTOR = "httpAdapter";
    private final WebhookManagementDAO webhookManagementDAO;
    private final WebhookSecretProcessor webhookSecretProcessor;

    public WebhookManagementDAOFacade(WebhookManagementDAO webhookManagementDAO) {

        this.webhookManagementDAO = webhookManagementDAO;
        this.webhookSecretProcessor = new WebhookSecretProcessor();
    }

    static {
        UPDATE_NOT_IMPLEMENTED_ADAPTORS.add(WEBSUBHUB_ADAPTOR);
        RETRY_NOT_IMPLEMENTED_ADAPTORS.add(HTTP_ADAPTOR);
    }

    @Override
    public void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        EventSubscriberService subscriberService = getSubscriberService();
        try {
            ensureTopicsExistOrRegister(webhook.getEventsSubscribed(), webhook.getEventProfileVersion(), tenantDomain);
        } catch (TopicManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR, e);
        }

        Webhook webhookToPersist;
        if (webhook.getStatus() == WebhookStatus.ACTIVE) {
            List<Subscription> subscriptions = subscriberService.subscribe(webhook, WEBSUBHUB_ADAPTOR, tenantId);

            boolean allError = subscriptions.stream()
                    .allMatch(s -> s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR);

            if (allError) {
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR);
            } else {
                webhookToPersist = new Webhook.Builder()
                        .uuid(webhook.getId())
                        .endpoint(webhook.getEndpoint())
                        .name(webhook.getName())
                        .secret(webhook.getSecret())
                        .eventProfileName(webhook.getEventProfileName())
                        .eventProfileUri(webhook.getEventProfileUri())
                        .eventProfileVersion(webhook.getEventProfileVersion())
                        .status(WebhookStatus.PARTIALLY_ACTIVE)
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
                "creating webhook: " + webhook.getId() + " in tenant ID: " + tenantId);
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
    public void activateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        EventSubscriberService subscriberService = getSubscriberService();

        List<Subscription> toSubscribe = webhook.getEventsSubscribed().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.SUBSCRIPTION_PENDING
                        || s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR
                        || s.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ACCEPTED)
                .collect(Collectors.toList());

        List<Subscription> allResults = subscriberService.subscribe(
                buildWebhookWith(webhook, toSubscribe, getWebhookDecryptedSecretValue(webhook.getId()),
                        webhook.getStatus()), WEBSUBHUB_ADAPTOR, tenantId);

        boolean allError = !allResults.isEmpty() && allResults.stream()
                .allMatch(r -> r.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR);

        if (allError) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ACTIVATION_ERROR);
        }

        List<Subscription> updatedSubscriptions = mergeSubscriptions(webhook.getEventsSubscribed(), allResults);
        webhookManagementDAO.activateWebhook(
                buildWebhookWith(webhook, updatedSubscriptions, webhook.getSecret(),
                        WebhookStatus.PARTIALLY_ACTIVE), tenantId);
    }

    @Override
    public void deactivateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        EventSubscriberService subscriberService = getSubscriberService();

        List<Subscription> toUnsubscribe = webhook.getEventsSubscribed().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ACCEPTED)
                .collect(Collectors.toList());

        List<Subscription> allResults =
                subscriberService.unsubscribe(buildWebhookWith(webhook, toUnsubscribe, webhook.getSecret(),
                        webhook.getStatus()), WEBSUBHUB_ADAPTOR, tenantId);

        boolean allError = !allResults.isEmpty() && allResults.stream()
                .allMatch(r -> r.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ERROR);

        if (allError) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_DEACTIVATION_ERROR);
        }

        List<Subscription> updatedUnsubscriptions = mergeSubscriptions(webhook.getEventsSubscribed(), allResults);
        webhookManagementDAO.deactivateWebhook(
                buildWebhookWith(webhook, updatedUnsubscriptions, webhook.getSecret(),
                        WebhookStatus.PARTIALLY_INACTIVE), tenantId);
    }

    @Override
    public void retryWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        validateRetryOperationSupported(WEBSUBHUB_ADAPTOR);
        EventSubscriberService subscriberService = getSubscriberService();

        if (webhook.getStatus() == WebhookStatus.PARTIALLY_ACTIVE) {
            List<Subscription> toSubscribe = webhook.getEventsSubscribed().stream()
                    .filter(s -> s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR
                            || s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ACCEPTED)
                    .collect(Collectors.toList());

            List<Subscription> allResults = subscriberService.subscribe(
                    buildWebhookWith(webhook, toSubscribe, getWebhookDecryptedSecretValue(webhook.getId()),
                            webhook.getStatus()), WEBSUBHUB_ADAPTOR, tenantId);

            boolean allError = !allResults.isEmpty() && allResults.stream()
                    .allMatch(r -> r.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR);

            if (allError) {
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_RETRY_ERROR, webhook.getId());
            } else {
                List<Subscription> updatedSubscriptions = mergeSubscriptions(webhook.getEventsSubscribed(), allResults);
                webhookManagementDAO.retryWebhook(
                        buildWebhookWith(webhook, updatedSubscriptions, webhook.getSecret(),
                                WebhookStatus.PARTIALLY_ACTIVE), tenantId);
            }
        } else if (webhook.getStatus() == WebhookStatus.PARTIALLY_INACTIVE) {
            List<Subscription> toUnsubscribe = webhook.getEventsSubscribed().stream()
                    .filter(s -> s.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ACCEPTED
                            || s.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ERROR)
                    .collect(Collectors.toList());

            List<Subscription> allResults =
                    subscriberService.unsubscribe(buildWebhookWith(webhook, toUnsubscribe, webhook.getSecret(),
                            webhook.getStatus()), WEBSUBHUB_ADAPTOR, tenantId);

            boolean allError = !allResults.isEmpty() && allResults.stream()
                    .allMatch(r -> r.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ERROR);

            if (allError) {
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_RETRY_ERROR, webhook.getId());
            } else {
                List<Subscription> updatedUnsubscriptions =
                        mergeSubscriptions(webhook.getEventsSubscribed(), allResults);
                webhookManagementDAO.retryWebhook(
                        buildWebhookWith(webhook, updatedUnsubscriptions, webhook.getSecret(),
                                WebhookStatus.PARTIALLY_INACTIVE),
                        tenantId);
            }
        }
    }

    @Override
    public void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        validateUpdateOperationSupported(WEBSUBHUB_ADAPTOR);
        webhookManagementDAO.updateWebhook(webhook, tenantId);
    }

    // --- Helper methods below ---

    /**
     * Encrypts the webhook secret and adds it to the webhook object.
     *
     * @param webhook Webhook object.
     * @return Webhook with encrypted secrets.
     * @throws WebhookMgtException If an error occurs while encrypting the webhook secrets.
     */
    private Webhook encryptAddingWebhookSecrets(Webhook webhook) throws WebhookMgtException {

        try {
            String encryptedSecretAlias =
                    webhookSecretProcessor.encryptAssociatedSecrets(webhook.getId(), webhook.getSecret());

            return addSecretOrAliasToBuilder(webhook, encryptedSecretAlias);
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_ENCRYPTION_ERROR, e, webhook.getId());
        }
    }

    private void deleteWebhookSecrets(Webhook webhook) throws WebhookMgtException {

        try {
            webhookSecretProcessor.deleteAssociatedSecrets(webhook.getId());
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DELETE_ERROR, e, webhook.getId());
        }
    }

    private EventSubscriberService getSubscriberService() {

        return WebhookManagementComponentServiceHolder.getInstance().getEventSubscriberService();
    }

    private void ensureTopicsExistOrRegister(List<Subscription> events, String eventProfileVersion, String tenantDomain)
            throws TopicManagementException {

        TopicManagementService topicManagementService =
                WebhookManagementComponentServiceHolder.getInstance().getTopicManagementService();
        for (Subscription event : events) {
            if (!topicManagementService.isTopicExists(event.getChannelUri(), eventProfileVersion,
                    tenantDomain)) {
                topicManagementService.registerTopic(event.getChannelUri(), eventProfileVersion,
                        tenantDomain);
            }
        }
    }

    // --- Utility methods ---

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

    private Webhook buildWebhookWith(Webhook base, List<Subscription> updatedSubs, String secret,
                                     WebhookStatus status) {

        return new Webhook.Builder()
                .uuid(base.getId())
                .endpoint(base.getEndpoint())
                .name(base.getName())
                .secret(secret)
                .eventProfileName(base.getEventProfileName())
                .eventProfileUri(base.getEventProfileUri())
                .eventProfileVersion(base.getEventProfileVersion())
                .status(status)
                .createdAt(base.getCreatedAt())
                .updatedAt(base.getUpdatedAt())
                .eventsSubscribed(updatedSubs)
                .build();
    }

    private List<Subscription> mergeSubscriptions(List<Subscription> original, List<Subscription> updates) {

        return original.stream()
                .map(sub -> updates.stream()
                        .filter(r -> r.getChannelUri().equals(sub.getChannelUri()))
                        .findFirst()
                        .orElse(sub))
                .collect(Collectors.toList());
    }

    private Webhook addSecretOrAliasToBuilder(Webhook webhook, String secretAlias) throws WebhookMgtException {

        return new Webhook.Builder().uuid(webhook.getId()).endpoint(webhook.getEndpoint()).name(webhook.getName())
                .secret(secretAlias).eventProfileName(webhook.getEventProfileName())
                .eventProfileUri(webhook.getEventProfileUri()).eventProfileVersion(webhook.getEventProfileVersion())
                .status(webhook.getStatus())
                .createdAt(webhook.getCreatedAt()).updatedAt(webhook.getUpdatedAt())
                .eventsSubscribed(webhook.getEventsSubscribed()).build();
    }

    private String getWebhookDecryptedSecretValue(String webhookId) throws WebhookMgtException {

        try {
            return webhookSecretProcessor.decryptAssociatedSecrets(webhookId);
        } catch (SecretManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_SECRET_DECRYPTION_ERROR, e, webhookId);
        }
    }

    private void validateUpdateOperationSupported(String adaptor) throws WebhookMgtClientException {

        if (UPDATE_NOT_IMPLEMENTED_ADAPTORS.contains(adaptor)) {
            throw WebhookManagementExceptionHandler.handleClientException(ERROR_UPDATE_OPERATION_NOT_SUPPORTED,
                    adaptor);
        }
    }

    private void validateRetryOperationSupported(String adaptor) throws WebhookMgtClientException {

        if (RETRY_NOT_IMPLEMENTED_ADAPTORS.contains(adaptor)) {
            throw WebhookManagementExceptionHandler.handleClientException(ERROR_RETRY_OPERATION_NOT_SUPPORTED);
        }
    }
}
