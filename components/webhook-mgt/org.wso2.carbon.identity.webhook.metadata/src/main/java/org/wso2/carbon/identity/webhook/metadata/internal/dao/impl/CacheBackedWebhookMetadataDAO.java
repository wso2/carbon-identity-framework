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

package org.wso2.carbon.identity.webhook.metadata.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.webhook.metadata.api.core.cache.WebhookMetadataCache;
import org.wso2.carbon.identity.webhook.metadata.api.core.cache.WebhookMetadataCacheEntry;
import org.wso2.carbon.identity.webhook.metadata.api.core.cache.WebhookMetadataCacheKey;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.WebhookMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.model.WebhookMetadataProperty;

import java.util.Map;

/**
 * Cache backed implementation of WebhookMetadataDAO.
 * This class uses a cache to store and retrieve webhook metadata properties.
 */
public class CacheBackedWebhookMetadataDAO implements WebhookMetadataDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedWebhookMetadataDAO.class);
    private final WebhookMetadataDAO webhookMetadataDAO;
    private final WebhookMetadataCache webhookMetadataCache;

    public CacheBackedWebhookMetadataDAO(WebhookMetadataDAO webhookMetadataDAO) {

        this.webhookMetadataDAO = webhookMetadataDAO;
        this.webhookMetadataCache = WebhookMetadataCache.getInstance();
    }

    @Override
    public Map<String, WebhookMetadataProperty> getWebhookMetadataProperties(int tenantId)
            throws WebhookMetadataException {

        WebhookMetadataCacheKey cacheKey = new WebhookMetadataCacheKey(tenantId);
        WebhookMetadataCacheEntry cacheEntry = webhookMetadataCache.getValueFromCache(cacheKey, tenantId);

        if (cacheEntry != null && cacheEntry.getWebhookMetadataProperties() != null) {
            LOG.debug("Webhook metadata cache hit for tenant ID: " + tenantId + ". Returning from cache.");
            return cacheEntry.getWebhookMetadataProperties();
        }

        Map<String, WebhookMetadataProperty> properties = webhookMetadataDAO.getWebhookMetadataProperties(tenantId);
        if (properties != null) {
            LOG.debug("Webhook metadata cache miss for tenant ID: " + tenantId + ". Adding to cache.");
            webhookMetadataCache.addToCache(cacheKey, new WebhookMetadataCacheEntry(properties), tenantId);
        }
        return properties;
    }

    @Override
    public void updateWebhookMetadataProperties(Map<String, WebhookMetadataProperty> webhookMetadataProperties,
                                                int tenantId)
            throws WebhookMetadataException {

        WebhookMetadataCacheKey cacheKey = new WebhookMetadataCacheKey(tenantId);
        webhookMetadataCache.clearCacheEntry(cacheKey, tenantId);
        LOG.debug("Webhook metadata cache entry is cleared for tenant ID: " + tenantId + " for update.");
        webhookMetadataDAO.updateWebhookMetadataProperties(webhookMetadataProperties, tenantId);
    }

    @Override
    public void addWebhookMetadataProperties(Map<String, WebhookMetadataProperty> webhookMetadataProperties,
                                             int tenantId)
            throws WebhookMetadataException {

        WebhookMetadataCacheKey cacheKey = new WebhookMetadataCacheKey(tenantId);
        webhookMetadataCache.clearCacheEntry(cacheKey, tenantId);
        LOG.debug("Webhook metadata cache entry is cleared for tenant ID: " + tenantId + " for add.");
        webhookMetadataDAO.addWebhookMetadataProperties(webhookMetadataProperties, tenantId);
    }
}
