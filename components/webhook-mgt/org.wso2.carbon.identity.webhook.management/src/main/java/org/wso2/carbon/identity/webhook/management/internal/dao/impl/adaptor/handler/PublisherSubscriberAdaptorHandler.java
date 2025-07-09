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

package org.wso2.carbon.identity.webhook.management.internal.dao.impl.adaptor.handler;

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.subscription.management.api.exception.SubscriptionManagementException;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.subscription.management.api.model.WebhookSubscriptionRequest;
import org.wso2.carbon.identity.subscription.management.api.model.WebhookUnsubscriptionRequest;
import org.wso2.carbon.identity.subscription.management.api.service.SubscriptionManagementService;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;
import org.wso2.carbon.identity.topic.management.api.service.TopicManagementService;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOFacade;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

public class PublisherSubscriberAdaptorHandler implements WebhookManagementDAO {

    private final WebhookManagementDAO dao;
    private final WebhookManagementDAOFacade facade;

    public PublisherSubscriberAdaptorHandler(WebhookManagementDAO dao, WebhookManagementDAOFacade facade) {

        this.dao = dao;
        this.facade = facade;
    }

    @Override
    public void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        Webhook webhookToPersist;
        try {
            ensureTopicsExistOrRegister(webhook.getEventsSubscribed(), webhook.getEventProfileName(),
                    webhook.getEventProfileVersion(), tenantDomain);
        } catch (TopicManagementException e) {
            throw WebhookManagementExceptionHandler.handleServerException(ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR, e);
        }
        SubscriptionManagementService subscriptionManagementService =
                WebhookManagementComponentServiceHolder.getInstance().getSubscriptionManagementService();

        if (webhook.getStatus() == WebhookStatus.ACTIVE) {
            List<Subscription> subscriptions;
            List<String> channelUris = webhook.getEventsSubscribed()
                    .stream()
                    .map(Subscription::getChannelUri)
                    .collect(Collectors.toList());
            WebhookSubscriptionRequest subscriptionRequest = WebhookSubscriptionRequest.builder()
                    .channelsToSubscribe(channelUris)
                    .eventProfileVersion(webhook.getEventProfileVersion())
                    .eventProfileName(webhook.getEventProfileName())
                    .endpoint(webhook.getEndpoint())
                    .secret(webhook.getSecret())
                    .build();
            try {
                subscriptions = subscriptionManagementService.subscribe(subscriptionRequest,
                        String.valueOf(WebhookManagementComponentServiceHolder.getInstance().getWebhookAdaptorType()),
                        tenantDomain);
            } catch (SubscriptionManagementException e) {
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR, e, webhook.getName());
            }
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
                        .status(webhook.getStatus())
                        .createdAt(webhook.getCreatedAt())
                        .updatedAt(webhook.getUpdatedAt())
                        .eventsSubscribed(subscriptions)
                        .build();
            }
        } else {
            webhookToPersist = webhook;
        }
        facade.runTransaction(jdbcTemplate,
                () -> dao.createWebhook(facade.encryptAddingWebhookSecrets(webhookToPersist), tenantId),
                ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR,
                "creating webhook: " + webhook.getId() + " in tenant ID: " + tenantId);
    }

    @Override
    public List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException {

        return dao.getWebhooks(tenantId);
    }

    @Override
    public Webhook getWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        return dao.getWebhook(webhookId, tenantId);
    }

    @Override
    public List<Subscription> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException {

        return dao.getWebhookEvents(webhookId, tenantId);
    }

    @Override
    public void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        Webhook existingWebhook = dao.getWebhook(webhookId, tenantId);
        if (existingWebhook.getStatus() == WebhookStatus.ACTIVE ||
                existingWebhook.getStatus() == WebhookStatus.PARTIALLY_ACTIVE) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_DELETE_NOT_ALLOWED_ERROR, webhookId);
        }
        if (existingWebhook.getStatus() == WebhookStatus.INACTIVE ||
                existingWebhook.getStatus() == WebhookStatus.PARTIALLY_INACTIVE) {
            NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
            facade.runTransaction(jdbcTemplate, () -> {
                        dao.deleteWebhook(webhookId, tenantId);
                        facade.deleteWebhookSecrets(existingWebhook);
                    }, ErrorMessage.ERROR_CODE_WEBHOOK_DELETE_ERROR,
                    "deleting webhook: " + webhookId + " in tenant ID: " + tenantId);
        }
    }

    @Override
    public boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException {

        return dao.isWebhookEndpointExists(endpoint, tenantId);
    }

    @Override
    public void activateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        if (webhook.getStatus() == WebhookStatus.PARTIALLY_ACTIVE) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ACTIVATION_NOT_ALLOWED_ERROR, webhook.getId());
        }
        if (webhook.getStatus() == WebhookStatus.INACTIVE ||
                webhook.getStatus() == WebhookStatus.PARTIALLY_INACTIVE) {
            SubscriptionManagementService subscriptionManagementService =
                    WebhookManagementComponentServiceHolder.getInstance().getSubscriptionManagementService();

            List<Subscription> toSubscribe = webhook.getEventsSubscribed().stream()
                    .filter(s -> s.getStatus() == SubscriptionStatus.SUBSCRIPTION_PENDING
                            || s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR
                            || s.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ACCEPTED)
                    .collect(Collectors.toList());

            List<Subscription> allResults;
            List<String> channelUris = toSubscribe
                    .stream()
                    .map(Subscription::getChannelUri)
                    .collect(Collectors.toList());
            WebhookSubscriptionRequest subscriptionRequest = WebhookSubscriptionRequest.builder()
                    .channelsToSubscribe(channelUris)
                    .eventProfileVersion(webhook.getEventProfileVersion())
                    .eventProfileName(webhook.getEventProfileName())
                    .endpoint(webhook.getEndpoint())
                    .secret(facade.getWebhookDecryptedSecretValue(webhook.getId()))
                    .build();
            try {
                allResults = subscriptionManagementService.subscribe(subscriptionRequest,
                        String.valueOf(WebhookManagementComponentServiceHolder.getInstance().getWebhookAdaptorType()),
                        IdentityTenantUtil.getTenantDomain(tenantId));
            } catch (SubscriptionManagementException e) {
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_ACTIVATION_ADAPTOR_ERROR, e, webhook.getId());
            }
            boolean allError = !allResults.isEmpty() && allResults.stream()
                    .allMatch(r -> r.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR);
            if (allError) {
                throw WebhookManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_ACTIVATION_ERROR);
            }
            List<Subscription> updatedSubscriptions =
                    mergeSubscriptions(webhook.getEventsSubscribed(), allResults);
            dao.activateWebhook(
                    buildWebhookWith(webhook, updatedSubscriptions, webhook.getSecret(),
                            WebhookStatus.PARTIALLY_ACTIVE), tenantId);
        }
    }

    @Override
    public void activateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        throw WebhookManagementExceptionHandler.handleClientException(
                ErrorMessage.ERROR_OPERATION_NOT_SUPPORTED,
                "activateWebhook by id not supported for PublisherSubscriber");
    }

    @Override
    public void deactivateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        if (webhook.getStatus() == WebhookStatus.PARTIALLY_INACTIVE) {
            throw WebhookManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_DEACTIVATION_NOT_ALLOWED_ERROR, webhook.getId());
        }
        if (webhook.getStatus() == WebhookStatus.ACTIVE ||
                webhook.getStatus() == WebhookStatus.PARTIALLY_ACTIVE) {
            SubscriptionManagementService subscriptionManagementService =
                    WebhookManagementComponentServiceHolder.getInstance().getSubscriptionManagementService();

            List<Subscription> toUnsubscribe = webhook.getEventsSubscribed().stream()
                    .filter(s -> s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ACCEPTED)
                    .collect(Collectors.toList());

            List<Subscription> allResults;
            List<String> channelUris = toUnsubscribe
                    .stream()
                    .map(Subscription::getChannelUri)
                    .collect(Collectors.toList());
            WebhookUnsubscriptionRequest unsubscriptionRequest = WebhookUnsubscriptionRequest.builder()
                    .channelsToUnsubscribe(channelUris)
                    .eventProfileVersion(webhook.getEventProfileVersion())
                    .eventProfileName(webhook.getEventProfileName())
                    .endpoint(webhook.getEndpoint())
                    .build();
            try {
                allResults = subscriptionManagementService.unsubscribe(unsubscriptionRequest,
                        String.valueOf(WebhookManagementComponentServiceHolder.getInstance().getWebhookAdaptorType()),
                        IdentityTenantUtil.getTenantDomain(tenantId));
            } catch (SubscriptionManagementException e) {
                throw WebhookManagementExceptionHandler.handleServerException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_DEACTIVATION_ADAPTOR_ERROR, e, webhook.getId());
            }
            boolean allError = !allResults.isEmpty() && allResults.stream()
                    .allMatch(r -> r.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ERROR);
            if (allError) {
                throw WebhookManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_DEACTIVATION_ERROR);
            }
            List<Subscription> updatedUnsubscriptions =
                    mergeSubscriptions(webhook.getEventsSubscribed(), allResults);
            dao.deactivateWebhook(
                    buildWebhookWith(webhook, updatedUnsubscriptions, webhook.getSecret(),
                            WebhookStatus.PARTIALLY_INACTIVE), tenantId);
        }
    }

    @Override
    public void deactivateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        throw WebhookManagementExceptionHandler.handleClientException(
                ErrorMessage.ERROR_OPERATION_NOT_SUPPORTED,
                "deactivateWebhook by id not supported for PublisherSubscriber");
    }

    @Override
    public void retryWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        if (webhook.getStatus() == WebhookStatus.PARTIALLY_ACTIVE ||
                webhook.getStatus() == WebhookStatus.PARTIALLY_INACTIVE) {
            SubscriptionManagementService subscriptionManagementService =
                    WebhookManagementComponentServiceHolder.getInstance().getSubscriptionManagementService();

            if (webhook.getStatus() == WebhookStatus.PARTIALLY_ACTIVE) {
                List<Subscription> toSubscribe = webhook.getEventsSubscribed().stream()
                        .filter(s -> s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR
                                || s.getStatus() == SubscriptionStatus.SUBSCRIPTION_ACCEPTED)
                        .collect(Collectors.toList());

                List<Subscription> allResults;
                List<String> channelUris = toSubscribe
                        .stream()
                        .map(Subscription::getChannelUri)
                        .collect(Collectors.toList());
                WebhookSubscriptionRequest subscriptionRequest = WebhookSubscriptionRequest.builder()
                        .channelsToSubscribe(channelUris)
                        .eventProfileVersion(webhook.getEventProfileVersion())
                        .eventProfileName(webhook.getEventProfileName())
                        .endpoint(webhook.getEndpoint())
                        .secret(facade.getWebhookDecryptedSecretValue(webhook.getId()))
                        .build();
                try {
                    allResults = subscriptionManagementService.subscribe(subscriptionRequest,
                            String.valueOf(
                                    WebhookManagementComponentServiceHolder.getInstance().getWebhookAdaptorType()),
                            IdentityTenantUtil.getTenantDomain(tenantId));
                } catch (SubscriptionManagementException e) {
                    throw WebhookManagementExceptionHandler.handleServerException(
                            ErrorMessage.ERROR_CODE_WEBHOOK_RETRY_ADAPTOR_ERROR, e, webhook.getId());
                }
                boolean allError = !allResults.isEmpty() && allResults.stream()
                        .allMatch(r -> r.getStatus() == SubscriptionStatus.SUBSCRIPTION_ERROR);
                if (allError) {
                    throw WebhookManagementExceptionHandler.handleServerException(
                            ErrorMessage.ERROR_CODE_WEBHOOK_RETRY_ERROR, webhook.getId());
                } else {
                    List<Subscription> updatedSubscriptions =
                            mergeSubscriptions(webhook.getEventsSubscribed(), allResults);
                    dao.retryWebhook(
                            buildWebhookWith(webhook, updatedSubscriptions, webhook.getSecret(),
                                    WebhookStatus.PARTIALLY_ACTIVE), tenantId);
                }
            } else if (webhook.getStatus() == WebhookStatus.PARTIALLY_INACTIVE) {
                List<Subscription> toUnsubscribe = webhook.getEventsSubscribed().stream()
                        .filter(s -> s.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ACCEPTED
                                || s.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ERROR)
                        .collect(Collectors.toList());

                List<Subscription> allResults;
                List<String> channelUris = toUnsubscribe
                        .stream()
                        .map(Subscription::getChannelUri)
                        .collect(Collectors.toList());
                WebhookUnsubscriptionRequest unsubscriptionRequest = WebhookUnsubscriptionRequest.builder()
                        .channelsToUnsubscribe(channelUris)
                        .eventProfileVersion(webhook.getEventProfileVersion())
                        .eventProfileName(webhook.getEventProfileName())
                        .endpoint(webhook.getEndpoint())
                        .build();
                try {
                    allResults = subscriptionManagementService.unsubscribe(unsubscriptionRequest,
                            String.valueOf(
                                    WebhookManagementComponentServiceHolder.getInstance().getWebhookAdaptorType()),
                            IdentityTenantUtil.getTenantDomain(tenantId));
                } catch (SubscriptionManagementException e) {
                    throw WebhookManagementExceptionHandler.handleServerException(
                            ErrorMessage.ERROR_CODE_WEBHOOK_RETRY_ADAPTOR_ERROR, e, webhook.getId());
                }
                boolean allError = !allResults.isEmpty() && allResults.stream()
                        .allMatch(r -> r.getStatus() == SubscriptionStatus.UNSUBSCRIPTION_ERROR);
                if (allError) {
                    throw WebhookManagementExceptionHandler.handleServerException(
                            ErrorMessage.ERROR_CODE_WEBHOOK_RETRY_ERROR, webhook.getId());
                } else {
                    List<Subscription> updatedUnsubscriptions =
                            mergeSubscriptions(webhook.getEventsSubscribed(), allResults);
                    dao.retryWebhook(
                            buildWebhookWith(webhook, updatedUnsubscriptions, webhook.getSecret(),
                                    WebhookStatus.PARTIALLY_INACTIVE),
                            tenantId);
                }
            }
        }
    }

    @Override
    public int getWebhooksCount(int tenantId) throws WebhookMgtException {

        return dao.getWebhooksCount(tenantId);
    }

    @Override
    public void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        throw WebhookManagementExceptionHandler.handleClientException(
                ErrorMessage.ERROR_UPDATE_OPERATION_NOT_SUPPORTED,
                "updateWebhook not supported for PublisherSubscriber");
    }

    // --- Helper methods below ---

    private void ensureTopicsExistOrRegister(List<Subscription> events, String eventProfileName,
                                             String eventProfileVersion, String tenantDomain)
            throws TopicManagementException {

        TopicManagementService topicManagementService =
                WebhookManagementComponentServiceHolder.getInstance().getTopicManagementService();
        for (Subscription event : events) {
            if (!topicManagementService.isTopicExists(event.getChannelUri(), eventProfileName, eventProfileVersion,
                    tenantDomain)) {
                topicManagementService.registerTopic(event.getChannelUri(), eventProfileName, eventProfileVersion,
                        tenantDomain);
            }
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
}
