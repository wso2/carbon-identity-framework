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
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.core.cache.ActiveWebhooksCache;
import org.wso2.carbon.identity.webhook.management.api.core.cache.ActiveWebhooksCacheEntry;
import org.wso2.carbon.identity.webhook.management.api.core.cache.ActiveWebhooksCacheKey;
import org.wso2.carbon.identity.webhook.management.api.core.cache.WebhookCache;
import org.wso2.carbon.identity.webhook.management.api.core.cache.WebhookCacheEntry;
import org.wso2.carbon.identity.webhook.management.api.core.cache.WebhookCacheKey;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;

import java.util.Collections;
import java.util.List;

/**
 * Cache backed implementation of WebhookManagementDAO.
 * This class adds caching layer to webhook management operations.
 */
public class CacheBackedWebhookManagementDAO implements WebhookManagementDAO {

    private static final String ACTIVE_WITH_CHILD_ORGS_SCOPE = "ACTIVE_WITH_CHILD_ORGS";
    private static final String SUBSCRIBED_SCOPE = "SUBSCRIBED";
    private static final String SUBSCRIBED_WITH_CHILD_ORGS_SCOPE = "SUBSCRIBED_WITH_CHILD_ORGS";

    private static final Log LOG = LogFactory.getLog(CacheBackedWebhookManagementDAO.class);
    private final WebhookManagementDAO webhookManagementDAO;
    private final WebhookCache webhookCache;
    private final ActiveWebhooksCache activeWebhooksCache;

    /**
     * Constructor.
     *
     * @param webhookManagementDAO WebhookManagementDAO implementation to be wrapped.
     */
    public CacheBackedWebhookManagementDAO(WebhookManagementDAO webhookManagementDAO) {

        this.webhookManagementDAO = webhookManagementDAO;
        this.webhookCache = WebhookCache.getInstance();
        this.activeWebhooksCache = ActiveWebhooksCache.getInstance();
    }

    @Override
    public void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        activeWebhooksCache.clear(tenantId);
        webhookManagementDAO.createWebhook(webhook, tenantId);
    }

    @Override
    public Webhook getWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        WebhookCacheEntry webhookCacheEntry = webhookCache.getValueFromCache(new WebhookCacheKey(webhookId), tenantId);
        if (webhookCacheEntry != null && webhookCacheEntry.getWebhook() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Webhook cache hit for webhook ID: " + webhookId + ". Returning from cache.");
            }
            return webhookCacheEntry.getWebhook();
        }

        Webhook webhook = webhookManagementDAO.getWebhook(webhookId, tenantId);
        if (webhook != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Webhook cache miss for webhook ID: " + webhookId + ". Adding to cache.");
            }
            webhookCache.addToCache(new WebhookCacheKey(webhookId), new WebhookCacheEntry(webhook), tenantId);
        }
        return webhook;
    }

    @Override
    public List<Subscription> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException {

        WebhookCacheEntry webhookCacheEntry = webhookCache.getValueFromCache(new WebhookCacheKey(webhookId), tenantId);
        if (webhookCacheEntry != null && webhookCacheEntry.getWebhook() != null &&
                webhookCacheEntry.getWebhook().getEventsSubscribed() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Webhook cache hit for webhook ID: " + webhookId + ". Returning from cache.");
            }
            return webhookCacheEntry.getWebhook().getEventsSubscribed();
        }

        Webhook webhook = webhookManagementDAO.getWebhook(webhookId, tenantId);
        if (webhook != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Webhook cache miss for webhook events for webhook ID: " + webhookId + ". Adding to cache.");
            }
            webhookCache.addToCache(new WebhookCacheKey(webhookId), new WebhookCacheEntry(webhook), tenantId);

            if (webhook.getEventsSubscribed() != null) {
                return webhook.getEventsSubscribed();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        webhookCache.clearCacheEntry(new WebhookCacheKey(webhook.getId()), tenantId);
        activeWebhooksCache.clear(tenantId);
        LOG.debug("Webhook cache entry is cleared for webhook ID: " + webhook.getId() + " for webhook update.");
        LOG.debug("Active webhooks cache is cleared for tenant ID: " + tenantId + " for webhook update.");
        webhookManagementDAO.updateWebhook(webhook, tenantId);
    }

    @Override
    public void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        webhookCache.clearCacheEntry(new WebhookCacheKey(webhookId), tenantId);
        activeWebhooksCache.clear(tenantId);
        LOG.debug("Webhook cache entry is cleared for webhook ID: " + webhookId + " for webhook deletion.");
        LOG.debug("Active webhooks cache is cleared for tenant ID: " + tenantId + " for webhook deletion.");
        webhookManagementDAO.deleteWebhook(webhookId, tenantId);
    }

    @Override
    public List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException {
        // Get all operations bypass cache
        return webhookManagementDAO.getWebhooks(tenantId);
    }

    @Override
    public boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException {
        // Endpoint existence check bypasses cache
        return webhookManagementDAO.isWebhookEndpointExists(endpoint, tenantId);
    }

    @Override
    public void activateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        webhookCache.clearCacheEntry(new WebhookCacheKey(webhook.getId()), tenantId);
        activeWebhooksCache.clear(tenantId);
        LOG.debug("Webhook cache entry is cleared for webhook ID: " + webhook.getId() + " for webhook activate.");
        LOG.debug("Active webhooks cache is cleared for tenant ID: " + tenantId + " for webhook activate.");
        webhookManagementDAO.activateWebhook(webhook, tenantId);
    }

    @Override
    public void deactivateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        webhookCache.clearCacheEntry(new WebhookCacheKey(webhook.getId()), tenantId);
        activeWebhooksCache.clear(tenantId);
        LOG.debug("Webhook cache entry is cleared for webhook ID: " + webhook.getId() + " for webhook deactivate.");
        LOG.debug("Active webhooks cache is cleared for tenant ID: " + tenantId + " for webhook deactivate.");
        webhookManagementDAO.deactivateWebhook(webhook, tenantId);
    }

    @Override
    public void retryWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        webhookCache.clearCacheEntry(new WebhookCacheKey(webhook.getId()), tenantId);
        activeWebhooksCache.clear(tenantId);
        LOG.debug("Webhook cache entry is cleared for webhook ID: " + webhook.getId() + " for webhook retry.");
        LOG.debug("Active webhooks cache is cleared for tenant ID: " + tenantId + " for webhook retry.");
        webhookManagementDAO.retryWebhook(webhook, tenantId);
    }

    @Override
    public int getWebhooksCount(int tenantId) throws WebhookMgtException {
        // Count retrieval bypasses cache
        return webhookManagementDAO.getWebhooksCount(tenantId);
    }

    @Override
    public List<Webhook> getActiveWebhooks(String eventProfileName, String eventProfileVersion, String channelUri,
                                           int tenantId) throws WebhookMgtException {

        ActiveWebhooksCacheKey cacheKey =
                new ActiveWebhooksCacheKey(eventProfileName, eventProfileVersion, channelUri, tenantId);
        ActiveWebhooksCacheEntry cacheEntry = activeWebhooksCache.getValueFromCache(cacheKey, tenantId);

        if (cacheEntry != null && cacheEntry.getWebhooks() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Active webhooks cache hit for channel URI: " + channelUri + ", event profile: " +
                        eventProfileName + ", version: " + eventProfileVersion + ", tenant ID: " + tenantId +
                        ". Returning from cache.");
            }
            return cacheEntry.getWebhooks();
        }

        // Cache miss: retrieve from database and update the cache.
        List<Webhook> webhooks =
                webhookManagementDAO.getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri, tenantId);
        if (webhooks != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Active webhooks cache miss for channel URI: " + channelUri + ", event profile: " +
                        eventProfileName + ", version: " + eventProfileVersion + ", tenant ID: " + tenantId +
                        ". Adding to cache.");
            }
            activeWebhooksCache.addToCache(cacheKey, new ActiveWebhooksCacheEntry(webhooks), tenantId);
        }
        return webhooks;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Cached under a scope of its own: this answer carries the webhooks of ancestor organizations that opted
     * the tenant in, so it differs from {@link #getActiveWebhooks} over the same profile, version, channel and
     * tenant. One tenant can reach both lookups, so neither entry may be served to the other.
     * <p>
     * Entries cached here can be invalidated by a change in a <em>different</em> tenant — the one
     * that owns the webhook — which is why {@code WebhookManagementServiceImpl} clears the cache of
     * every subscribed tenant when a webhook or its subscriptions change.
     */
    @Override
    public List<Webhook> getActiveWebhooksForOrganization(String eventProfileName, String eventProfileVersion,
                                                          String channelUri, int eventOrgTenantId)
            throws WebhookMgtException {

        ActiveWebhooksCacheKey cacheKey = new ActiveWebhooksCacheKey(eventProfileName, eventProfileVersion,
                channelUri, eventOrgTenantId, ACTIVE_WITH_CHILD_ORGS_SCOPE);
        ActiveWebhooksCacheEntry cacheEntry = activeWebhooksCache.getValueFromCache(cacheKey, eventOrgTenantId);

        if (cacheEntry != null && cacheEntry.getWebhooks() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Active webhooks cache hit (including sub organizations) for channel URI: " + channelUri +
                        ", tenant ID: " + eventOrgTenantId + ". Returning from cache.");
            }
            return cacheEntry.getWebhooks();
        }

        List<Webhook> webhooks = webhookManagementDAO.getActiveWebhooksForOrganization(
                eventProfileName, eventProfileVersion, channelUri, eventOrgTenantId);
        if (webhooks != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Active webhooks cache miss (including sub organizations) for channel URI: " + channelUri +
                        ", tenant ID: " + eventOrgTenantId + ". Adding to cache.");
            }
            activeWebhooksCache.addToCache(cacheKey, new ActiveWebhooksCacheEntry(webhooks), eventOrgTenantId);
        }
        return webhooks;
    }

    /**
     * Cached under a scope of its own: the answer differs from the active-webhook lookups over the same channel
     * and tenant, and the entries of the two must not be served to each other.
     */
    @Override
    public List<Webhook> getSubscriptionAcceptedWebhooksForOrganization(String eventProfileName,
                                                                        String eventProfileVersion,
                                                                        String channelUri, int eventOrgTenantId,
                                                                        boolean includeChildOrganizationSubscriptions)
            throws WebhookMgtException {

        String scope = includeChildOrganizationSubscriptions ? SUBSCRIBED_WITH_CHILD_ORGS_SCOPE : SUBSCRIBED_SCOPE;
        ActiveWebhooksCacheKey cacheKey =
                new ActiveWebhooksCacheKey(eventProfileName, eventProfileVersion, channelUri, eventOrgTenantId, scope);
        ActiveWebhooksCacheEntry cacheEntry = activeWebhooksCache.getValueFromCache(cacheKey, eventOrgTenantId);

        if (cacheEntry != null && cacheEntry.getWebhooks() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Accepted channel subscription cache hit for channel URI: " + channelUri + ", tenant ID: " +
                        eventOrgTenantId + ", scope: " + scope + ". Returning from cache.");
            }
            return cacheEntry.getWebhooks();
        }

        List<Webhook> webhooks = webhookManagementDAO.getSubscriptionAcceptedWebhooksForOrganization(
                eventProfileName, eventProfileVersion, channelUri, eventOrgTenantId,
                includeChildOrganizationSubscriptions);
        if (webhooks != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Accepted channel subscription cache miss for channel URI: " + channelUri + ", tenant ID: " +
                        eventOrgTenantId + ", scope: " + scope + ". Adding to cache.");
            }
            activeWebhooksCache.addToCache(cacheKey, new ActiveWebhooksCacheEntry(webhooks), eventOrgTenantId);
        }
        return webhooks;
    }

    // --- Organization subscriptions ---
    // Not cached: organization subscriptions are read on the management path, not the publishing path, and a change
    // to them already clears the active-webhook cache of every tenant it affects.

    @Override
    public void addChannelOrgSubscriptions(List<ChannelOrgSubscription> subscriptions) throws WebhookMgtException {

        webhookManagementDAO.addChannelOrgSubscriptions(subscriptions);
    }

    @Override
    public List<ChannelOrgSubscription> getChannelOrgSubscriptions(String channelUuid) throws WebhookMgtException {

        return webhookManagementDAO.getChannelOrgSubscriptions(channelUuid);
    }

    @Override
    public List<Integer> getSubscribedOrgTenantIds(String channelUuid) throws WebhookMgtException {

        return webhookManagementDAO.getSubscribedOrgTenantIds(channelUuid);
    }

    @Override
    public int getChannelOrgSubscriptionCount(String channelUuid) throws WebhookMgtException {

        return webhookManagementDAO.getChannelOrgSubscriptionCount(channelUuid);
    }

    @Override
    public void deleteChannelOrgSubscriptions(String channelUuid) throws WebhookMgtException {

        webhookManagementDAO.deleteChannelOrgSubscriptions(channelUuid);
    }

    @Override
    public void deleteChannelOrgSubscriptionsByOrgId(String subscribedOrgId) throws WebhookMgtException {

        webhookManagementDAO.deleteChannelOrgSubscriptionsByOrgId(subscribedOrgId);
    }

    @Override
    public List<String> getChannelUuids(String webhookId, int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.getChannelUuids(webhookId, tenantId);
    }

    @Override
    public int getOwningTenantId(String channelUuid) throws WebhookMgtException {

        return webhookManagementDAO.getOwningTenantId(channelUuid);
    }

    @Override
    public String getChannelUuid(String webhookId, String channelUri, int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.getChannelUuid(webhookId, channelUri, tenantId);
    }

    @Override
    public String getChannelUri(String channelUuid) throws WebhookMgtException {

        return webhookManagementDAO.getChannelUri(channelUuid);
    }

}
