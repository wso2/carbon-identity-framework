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

package org.wso2.carbon.identity.webhook.management.internal.service;

import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;

import java.util.List;

/**
 * Organization-level event subscription for webhook channels.
 * <p>
 * Internal collaborator of WebhookManagementService, which is the single OSGi service this
 * component publishes. Not exported.
 * <p>
 * A webhook configured in an organization can receive events raised in its descendant
 * organizations, opted in per channel. This service owns both halves of that: the <em>intent</em>,
 * held as a sharing policy in UM_RESOURCE_SHARING_POLICY, and the <em>materialised facts</em> the
 * intent resolves to, held in the Identity database so that publish-time resolution is a single
 * index lookup with no ancestor walk and no cross-database read.
 * <p>
 * Setting a policy performs both writes. They span two databases and so are not atomic; the
 * reconciliation sweep is what makes that recoverable.
 */
public interface WebhookChannelService {

    /**
     * Retrieve the organization subscription policy of a channel.
     *
     * @param channelUuid Channel UUID.
     * @return The configured policy, or {@link SubscriptionPolicy#NONE} when none is recorded.
     * @throws WebhookMgtException If the policy cannot be retrieved.
     */
    SubscriptionPolicy getSubscriptionPolicy(String channelUuid) throws WebhookMgtException;

    /**
     * Set the organization subscription policy of a channel and materialise the organizations it
     * resolves to.
     * <p>
     * {@link SubscriptionPolicy#NONE} clears both the policy and every materialised row.
     * {@link SubscriptionPolicy#ALL_EXISTING_AND_FUTURE_ORGS} resolves to every current descendant, with organizations
     * created later added by the organization creation handler.
     * {@link SubscriptionPolicy#SELECTED_ORGS_ONLY} resolves to exactly the organizations given; a
     * selected organization does not imply its children.
     *
     * @param channelUuid      Channel UUID.
     * @param owningOrgId      Organization owning the webhook.
     * @param policy           Policy to apply.
     * @param selectedOrgIds   Organizations to subscribe. Required for SELECTED_ORGS_ONLY, ignored
     *                         otherwise.
     * @throws WebhookMgtException If the policy cannot be applied.
     */
    void setSubscriptionPolicy(String channelUuid, String owningOrgId, SubscriptionPolicy policy,
                               List<String> selectedOrgIds) throws WebhookMgtException;

    /**
     * Retrieve the organizations currently receiving events for a channel.
     *
     * The returned rows carry the organization's resolved name and parent, which are not stored.
     * Resolution is applied to the requested window only, so its cost is bounded by the page size
     * rather than by how many organizations the channel fans out to.
     *
     * @param channelUuid Channel UUID.
     * @param offset      Rows to skip. Negative is treated as zero.
     * @param limit       Maximum rows to return. Negative returns everything from the offset.
     * @return Subscribed organizations, empty when the channel has no organization fanout.
     * @throws WebhookMgtException If the organizations cannot be retrieved.
     */
    List<ChannelOrgSubscription> getSubscribedOrganizations(String channelUuid, int offset, int limit)
            throws WebhookMgtException;

    /**
     * Count the organizations currently receiving events for a channel.
     *
     * @param channelUuid Channel UUID.
     * @return Number of subscribed organizations.
     * @throws WebhookMgtException If the count cannot be retrieved.
     */
    int getSubscribedOrganizationCount(String channelUuid) throws WebhookMgtException;

    /**
     * Subscribe a newly created organization to every channel whose policy is
     * {@link SubscriptionPolicy#ALL_EXISTING_AND_FUTURE_ORGS} in any of its ancestors.
     *
     * @param newOrgId Organization that was created.
     * @throws WebhookMgtException If the subscriptions cannot be materialised.
     */
    void subscribeNewOrganization(String newOrgId) throws WebhookMgtException;

    /**
     * Clear the organization subscription policy held by each of the given channels.
     * <p>
     * Used when the channels themselves are going away. The policies live in the shared database and cannot
     * cascade from the Identity database, so they are removed explicitly.
     * <p>
     * Best effort: a policy that survives points at a channel that no longer exists, so nothing reads it and
     * nothing behaves incorrectly. It is inert until reconciliation removes it, which is a better outcome than
     * failing an operation whose real work has already been committed.
     *
     * @param channelUuids Channels whose policies are to be cleared.
     */
    void clearSubscriptionPolicies(List<String> channelUuids);

    /**
     * Remove everything a deleted organization leaves behind in the fanout tables.
     * <p>
     * Two distinct roles are cleaned up. As a subscriber, the organization's rows are removed from
     * every channel it was receiving events for. As an owner, the standing instructions of the
     * channels it owned are removed from UM_RESOURCE_SHARING_POLICY — those rows live in the shared
     * database and have no foreign key to the webhook tables, so nothing else would ever collect
     * them.
     * <p>
     * Keyed by organization id, not tenant id: the deletion event fires once the organization is
     * gone, and its tenant can no longer be resolved by then.
     *
     * @param deletedOrgId Organization that was deleted.
     * @throws WebhookMgtException If the subscriptions cannot be removed.
     */
    void unsubscribeDeletedOrganization(String deletedOrgId) throws WebhookMgtException;

    /**
     * Identifiers of every channel a webhook subscribes to.
     *
     * @param webhookId Webhook UUID.
     * @param tenantId  Tenant owning the webhook.
     * @return Channel UUIDs, empty when the webhook has no channels.
     * @throws WebhookMgtException If the channels cannot be read.
     */
    List<String> getChannelUuids(String webhookId, int tenantId) throws WebhookMgtException;

    /**
     * Identifier of one channel of a webhook, addressed by its URI.
     *
     * @param webhookId  Webhook UUID.
     * @param channelUri Channel URI.
     * @param tenantId   Tenant owning the webhook.
     * @return Channel UUID, or null when the webhook does not subscribe to that channel.
     * @throws WebhookMgtException If the channel cannot be read.
     */
    String getChannelUuid(String webhookId, String channelUri, int tenantId) throws WebhookMgtException;

    /**
     * URI of a channel, addressed by its identifier.
     *
     * @param channelUuid Channel UUID.
     * @return Channel URI, or null when no such channel exists.
     * @throws WebhookMgtException If the channel cannot be read.
     */
    String getChannelUri(String channelUuid) throws WebhookMgtException;

    /**
     * Tenants of the organizations currently subscribed to a channel.
     *
     * @param channelUuid Channel UUID.
     * @return Tenant identifiers, empty when nothing is subscribed.
     * @throws WebhookMgtException If the subscriptions cannot be read.
     */
    List<Integer> getSubscribedOrgTenantIds(String channelUuid) throws WebhookMgtException;
}
