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

package org.wso2.carbon.identity.webhook.management.api.service;

import org.apache.commons.lang.NotImplementedException;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelSubscribedOrganizations;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookChannel;

import java.util.List;

/**
 * Service interface for managing webhook subscriptions.
 * This interface defines the operations for creating, retrieving, updating, and deleting webhook subscriptions.
 */
public interface WebhookManagementService {

    /**
     * Create a new webhook subscription.
     *
     * @param webhook      Webhook subscription to be created.
     * @param tenantDomain Tenant domain.
     * @return Created webhook subscription.
     * @throws WebhookMgtException If an error occurs while creating the webhook subscription.
     */
    Webhook createWebhook(Webhook webhook, String tenantDomain) throws WebhookMgtException;

    /**
     * Get a webhook subscription by ID.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @return Webhook subscription.
     * @throws WebhookMgtException If an error occurs while retrieving the webhook subscription.
     */
    Webhook getWebhook(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Update a webhook subscription.
     *
     * @param webhookId    Webhook subscription ID.
     * @param webhook      Updated webhook subscription.
     * @param tenantDomain Tenant domain.
     * @return Updated webhook subscription.
     * @throws WebhookMgtException If an error occurs while updating the webhook subscription.
     */
    Webhook updateWebhook(String webhookId, Webhook webhook, String tenantDomain) throws WebhookMgtException;

    /**
     * Delete a webhook subscription.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @throws WebhookMgtException If an error occurs while deleting the webhook subscription.
     */
    void deleteWebhook(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Get all webhooks for a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @return List of webhook subscriptions.
     * @throws WebhookMgtException If an error occurs while retrieving webhook subscriptions.
     */
    List<Webhook> getWebhooks(String tenantDomain) throws WebhookMgtException;

    /**
     * Get a page of webhooks for a tenant.
     * <p>
     * Ordering is stable across calls, so paging through the collection visits every webhook once as long as
     * the collection itself does not change. Pair with {@link #getWebhooksCount(String)} to report the full
     * size alongside a page; the size is not derivable from a page.
     *
     * @param offset       Number of records to skip. Must not be negative.
     * @param limit        Maximum number of records to return. Must be greater than zero.
     * @param tenantDomain Tenant domain.
     * @return Webhooks in the requested window, empty when the offset is past the end of the collection.
     * @throws WebhookMgtException If the paging arguments are invalid, or an error occurs while retrieving
     *                             webhooks.
     */
    default List<Webhook> getWebhooks(int offset, int limit, String tenantDomain)
            throws WebhookMgtException, NotImplementedException {

        throw new NotImplementedException("getWebhooks method is not implemented.");
    }

    /**
     * Get the total number of webhooks for a tenant, independent of any page window.
     *
     * @param tenantDomain Tenant domain.
     * @return Number of webhooks configured for the tenant.
     * @throws WebhookMgtException If an error occurs while counting webhooks.
     */
    default int getWebhooksCount(String tenantDomain) throws WebhookMgtException, NotImplementedException {

        throw new NotImplementedException("getWebhooksCount method is not implemented.");
    }

    /**
     * Get webhook events by webhook ID.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @return List of webhook events.
     * @throws WebhookMgtException If an error occurs while retrieving webhook events.
     */
    List<Subscription> getWebhookEvents(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Enable a webhook subscription.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @return Activated webhook subscription.
     * @throws WebhookMgtException If an error occurs while enabling the webhook.
     */
    Webhook activateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Disable a webhook subscription.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @return Deactivated webhook subscription.
     * @throws WebhookMgtException If an error occurs while disabling the webhook.
     */
    Webhook deactivateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Retry a webhook subscription or unsubscription that has failed.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @return Retried webhook subscription.
     * @throws WebhookMgtException If an error occurs while retrying the webhook.
     */
    Webhook retryWebhook(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Get active webhooks for a specific channel URI and tenant ID.
     *
     * @param eventProfileName    Name of the event profile.
     * @param eventProfileVersion Version of the event profile.
     * @param channelUri          Channel URI to filter webhooks.
     * @param tenantDomain        Tenant domain.
     * @return List of active webhooks for the specified channel URI and tenant ID.
     * @throws WebhookMgtException If an error occurs while retrieving the active webhooks.
     */
    List<Webhook> getActiveWebhooks(String eventProfileName, String eventProfileVersion, String channelUri,
                                    String tenantDomain) throws WebhookMgtException;

    /**
     * Retrieve every channel of a webhook, in one call.
     * <p>
     * Each entry carries the channel's URI, its stable id, the webhook's subscription status for it and its
     * organization subscription policy -- enough to render a webhook without a request per channel. The
     * subscribed organizations themselves are not included; they are read from the channel organizations
     * sub-resource, which resolves names and parents and is paginated.
     *
     * @param webhookId    Webhook UUID.
     * @param tenantDomain Tenant domain of the webhook-owning organization.
     * @return One entry per channel, empty when the webhook has no channels.
     * @throws WebhookMgtException If the channels cannot be retrieved.
     */
    default List<WebhookChannel> getWebhookChannels(String webhookId, String tenantDomain)
            throws WebhookMgtException, NotImplementedException {

        throw new NotImplementedException("getWebhookChannels method is not implemented.");
    }
    /**
     * Resolve the issuer identifier of the organization that transmits events for a webhook.
     * <p>
     * A Security Event Token names its transmitter in {@code iss}, and the transmitter is the
     * organization that owns the webhook -- not the organization the event was raised in. An event
     * raised in a descendant and delivered to an ancestor's webhook is transmitted by that ancestor.
     * <p>
     * Root organizations resolve to {@code /t/<tenant domain>}; a sub-organization resolves to
     * {@code /t/<root tenant domain>/o/<organization id>}.
     *
     * @param tenantId Tenant owning the webhook.
     * @return Absolute issuer URL of the transmitting organization.
     * @throws WebhookMgtException If the organization cannot be resolved.
     */
    default String resolveTransmitterIssuer(int tenantId) throws WebhookMgtException, NotImplementedException {

        throw new NotImplementedException("resolveTransmitterIssuer method is not implemented.");
    }

    /**
     * Get a page of the organizations subscribed to a channel, together with the channel context that
     * describes it.
     * <p>
     * The channel URI, the subscription policy, the total count and the page are read behind a single
     * ownership check rather than one per value, and describe one moment rather than four.
     *
     * @param webhookId    Webhook UUID.
     * @param channelId    Channel UUID.
     * @param tenantDomain Tenant domain.
     * @param offset       Number of organizations to skip.
     * @param limit        Maximum number of organizations to return.
     * @return The page and the channel context around it.
     * @throws WebhookMgtException If the channel does not belong to the webhook, or the read fails.
     */
    default ChannelSubscribedOrganizations getChannelOrganizationSubscriptions(String webhookId, String channelId,
                                                                              String tenantDomain, int offset,
                                                                              int limit)
            throws WebhookMgtException, NotImplementedException {

        throw new NotImplementedException("getChannelOrganizationSubscriptions method is not implemented.");
    }

    /**
     * Get a single channel of a webhook: its URI, the webhook's subscription status for it, and the
     * organization subscription policy in force for it.
     * <p>
     * Read behind a single ownership check rather than one per value, and describing one moment rather than
     * several.
     *
     * @param webhookId    Webhook UUID.
     * @param channelId    Channel UUID.
     * @param tenantDomain Tenant domain.
     * @return The channel and the subscription state around it.
     * @throws WebhookMgtException If the channel does not belong to the webhook, or the read fails.
     */
    default WebhookChannel getWebhookChannel(String webhookId, String channelId, String tenantDomain)
            throws WebhookMgtException, NotImplementedException {

        throw new NotImplementedException("getWebhookChannel method is not implemented.");
    }

    /**
     * Set the organization subscription of a webhook channel, replacing whatever is in force.
     * <p>
     * This is the only path that edits an organization subscription. It deliberately does not go
     * through {@code updateWebhook}, which is unsupported on the PublisherSubscriber adapter, so
     * the operation works on both adapters.
     *
     * @param webhookId       Webhook UUID.
     * @param channelId       Channel UUID.
     * @param policy          Policy to apply.
     * @param organizationIds Organizations to subscribe. Required for
     *                        {@link SubscriptionPolicy#SELECTED_ORGS_ONLY}, ignored otherwise.
     * @param tenantDomain    Tenant domain of the webhook-owning organization.
     * @throws WebhookMgtException If the channel does not belong to the webhook, or the policy
     *                             cannot be applied.
     */
    default void updateChannelSubscription(String webhookId, String channelId, SubscriptionPolicy policy,
                                           List<String> organizationIds, String tenantDomain)
            throws WebhookMgtException, NotImplementedException {

        throw new NotImplementedException("updateChannelSubscription method is not implemented.");
    }
}
