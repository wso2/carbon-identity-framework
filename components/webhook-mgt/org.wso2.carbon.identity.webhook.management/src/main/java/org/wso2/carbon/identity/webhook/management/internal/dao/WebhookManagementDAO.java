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

package org.wso2.carbon.identity.webhook.management.internal.dao;

import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;

import java.util.List;

/**
 * Data Access Object interface for webhook management.
 */
public interface WebhookManagementDAO {

    /**
     * Create a new webhook subscription in the database.
     *
     * @param webhook  Webhook subscription to be created.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while creating the webhook subscription.
     */
    void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException;

    /**
     * Get a webhook subscription by ID from the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @return Webhook subscription.
     * @throws WebhookMgtException If an error occurs while retrieving the webhook subscription.
     */
    Webhook getWebhook(String webhookId, int tenantId) throws WebhookMgtException;

    /**
     * Update a webhook subscription in the database.
     *
     * @param webhook  Updated webhook subscription.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while updating the webhook subscription.
     */
    void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException;

    /**
     * Delete a webhook subscription from the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @throws WebhookMgtException If an error occurs while deleting the webhook subscription.
     */
    void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException;

    /**
     * Get all webhooks for a tenant from the database.
     *
     * @param tenantId Tenant ID.
     * @return List of webhook subscriptions.
     * @throws WebhookMgtException If an error occurs while retrieving webhook subscriptions.
     */
    List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException;

    /**
     * Get webhook events for a specific webhook subscription from the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @return List of events subscribed to the webhook.
     * @throws WebhookMgtException If an error occurs while retrieving the events.
     */
    List<Subscription> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException;

    /**
     * Check if a webhook endpoint exists in the database.
     *
     * @param endpoint Webhook endpoint URL.
     * @param tenantId Tenant ID.
     * @return True if the endpoint exists, false otherwise.
     * @throws WebhookMgtException If an error occurs while checking the endpoint.
     */
    boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException;

    /**
     * Enable a webhook subscription in the database.
     *
     * @param webhook  Webhook subscription to be activated.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while retrying the webhook.
     */
    void activateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException;

    /**
     * Disable a webhook subscription in the database.
     *
     * @param webhook  Webhook subscription to be deactivated.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while retrying the webhook.
     */
    void deactivateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException;

    /**
     * Retry a webhook subscription or unsubscription that has failed.
     *
     * @param webhook  Webhook subscription to be retried.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while retrying the webhook.
     */
    void retryWebhook(Webhook webhook, int tenantId) throws WebhookMgtException;

    /**
     * Get the count of webhooks for a tenant.
     *
     * @param tenantId Tenant ID.
     * @return Count of webhooks.
     * @throws WebhookMgtException If an error occurs while retrieving the webhook count.
     */
    int getWebhooksCount(int tenantId) throws WebhookMgtException;

    /**
     * Get active webhooks for a specific channel URI and tenant ID.
     *
     * @param eventProfileName    Event profile name to filter webhooks.
     * @param eventProfileVersion Event profile version to filter webhooks.
     * @param channelUri          Channel URI to filter webhooks.
     * @param tenantId            Tenant ID.
     * @return List of active webhooks for the specified channel URI and tenant ID.
     * @throws WebhookMgtException If an error occurs while retrieving the active webhooks.
     */
    List<Webhook> getActiveWebhooks(String eventProfileName, String eventProfileVersion, String channelUri,
                                    int tenantId) throws WebhookMgtException;

    /**
     * Retrieve the active webhooks an event raised in a tenant should reach, including webhooks
     * owned by an ancestor organization that has opted this tenant in for the channel.
     * <p>
     * Only meaningful for the Publisher adapter, where each webhook is posted to individually. The
     * PublisherSubscriber adapter reaches descendant organizations through topics, not through this
     * list.
     *
     * @param eventProfileName    Event profile name.
     * @param eventProfileVersion Event profile version.
     * @param channelUri          Channel URI of the event.
     * @param eventOrgTenantId    Tenant the event was raised in.
     * @return Active webhooks, own and inherited, empty when there are none.
     * @throws WebhookMgtException If the webhooks cannot be retrieved.
     */
    List<Webhook> getActiveWebhooksForOrganization(String eventProfileName, String eventProfileVersion,
                                                   String channelUri, int eventOrgTenantId) throws WebhookMgtException;

    /**
     * Webhooks whose subscription to the given channel is live.
     * <p>
     * Acceptance is stated per channel, not per webhook: a partially active webhook has some channels
     * subscribed and others not, and only the subscribed ones are answered for. The webhook's own state is
     * accepted as active or partially active, the latter being where a subscriber adapter leaves a webhook
     * once the hub has accepted its subscriptions.
     * <p>
     * A subscriber adapter's own webhooks are never fully active, so the active-webhook lookup would answer
     * with none of them. This lookup exists for that case.
     *
     * @param eventProfileName                     Event profile name.
     * @param eventProfileVersion                  Event profile version.
     * @param channelUri                           Channel URI of the event.
     * @param eventOrgTenantId                     Tenant the event was raised in.
     * @param includeChildOrganizationSubscriptions Whether to widen the answer to the webhooks of ancestor
     *                                             organizations that subscribed this organization to the channel.
     * @return Those webhooks, empty when there are none.
     * @throws WebhookMgtException If the webhooks cannot be retrieved.
     */
    List<Webhook> getSubscriptionAcceptedWebhooksForOrganization(String eventProfileName, String eventProfileVersion,
                                                                 String channelUri, int eventOrgTenantId,
                                                                 boolean includeChildOrganizationSubscriptions)
            throws WebhookMgtException;

    // --- Organization subscriptions ---

    /**
     * Persist organization subscriptions for a channel.
     *
     * @param subscriptions Subscriptions to store.
     * @throws WebhookMgtException If a database error occurs.
     */
    void addChannelOrgSubscriptions(List<ChannelOrgSubscription> subscriptions) throws WebhookMgtException;

    /**
     * Retrieve every organization subscribed to a channel.
     *
     * @param channelUuid Channel UUID.
     * @return Stored subscriptions, empty when nothing is subscribed.
     * @throws WebhookMgtException If a database error occurs.
     */
    List<ChannelOrgSubscription> getChannelOrgSubscriptions(String channelUuid) throws WebhookMgtException;

    /**
     * Retrieve the tenants of the organizations subscribed to a channel.
     * <p>
     * The publish path resolves webhooks by the tenant an event was raised in, so these are the
     * tenants whose cached answer a change to this channel invalidates.
     *
     * @param channelUuid Channel UUID.
     * @return Subscribed tenant ids, empty when nothing is subscribed.
     * @throws WebhookMgtException If a database error occurs.
     */
    List<Integer> getSubscribedOrgTenantIds(String channelUuid) throws WebhookMgtException;

    /**
     * Count the organizations subscribed to a channel.
     *
     * @param channelUuid Channel UUID.
     * @return Number of subscribed organizations.
     * @throws WebhookMgtException If a database error occurs.
     */
    int getChannelOrgSubscriptionCount(String channelUuid) throws WebhookMgtException;

    /**
     * Remove every organization subscription of a channel.
     *
     * @param channelUuid Channel UUID.
     * @throws WebhookMgtException If a database error occurs.
     */
    void deleteChannelOrgSubscriptions(String channelUuid) throws WebhookMgtException;

    /**
     * Remove every organization subscription held by an organization, across all channels.
     *
     * @param subscribedOrgId Organization id of the organization being removed.
     * @throws WebhookMgtException If a database error occurs.
     */
    void deleteChannelOrgSubscriptionsByOrgId(String subscribedOrgId) throws WebhookMgtException;

    /**
     * Retrieve the UUIDs of every channel of a webhook.
     * <p>
     * Used when a webhook is deleted, to clear the sharing policy each of its channels holds in
     * UM_RESOURCE_SHARING_POLICY. The link rows themselves are removed by the foreign key on
     * CHANNEL_UUID; the policies live in a separate database and cannot cascade.
     *
     * @param webhookId Webhook UUID.
     * @param tenantId  Tenant id of the webhook-owning organization.
     * @return Channel UUIDs, empty when the webhook has no channels.
     * @throws WebhookMgtException If a database error occurs.
     */
    List<String> getChannelUuids(String webhookId, int tenantId) throws WebhookMgtException;

    /**
     * Resolve the tenant of the organization owning the webhook a channel belongs to.
     *
     * @param channelUuid Channel UUID.
     * @return Owning tenant id, or -1 when the channel does not exist.
     * @throws WebhookMgtException If a database error occurs.
     */
    int getOwningTenantId(String channelUuid) throws WebhookMgtException;

    /**
     * Resolve the UUID of a webhook's channel by its URI.
     *
     * @param webhookId  Webhook UUID.
     * @param channelUri Channel URI.
     * @param tenantId   Tenant id of the webhook-owning organization.
     * @return The channel UUID, or null when the webhook has no such channel.
     * @throws WebhookMgtException If a database error occurs.
     */
    String getChannelUuid(String webhookId, String channelUri, int tenantId) throws WebhookMgtException;

    /**
     * Resolve the channel URI of a channel.
     *
     * @param channelUuid Channel UUID.
     * @return Channel URI, or null when the channel does not exist.
     * @throws WebhookMgtException If a database error occurs.
     */
    String getChannelUri(String channelUuid) throws WebhookMgtException;

}
