/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;

import java.util.List;

/**
 * Persistence of organization-level event subscriptions.
 * <p>
 * A webhook configured in an organization can receive events raised in its descendant
 * organizations, opted in per channel. The intent is held in UM_RESOURCE_SHARING_POLICY, keyed by
 * ResourceType.WEBHOOK_CHANNEL and the channel UUID; the organizations that intent resolves to are
 * materialised here as {@link ChannelOrgSubscription} rows, so that publish-time resolution is a
 * single index lookup in the Identity database with no ancestor walk and no cross-database read.
 * <p>
 * These operations have no adapter-specific side effects — no topic is registered and no hub call
 * is made — so they do not go through the adapter type handler.
 */
public interface WebhookChannelDAO {

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
