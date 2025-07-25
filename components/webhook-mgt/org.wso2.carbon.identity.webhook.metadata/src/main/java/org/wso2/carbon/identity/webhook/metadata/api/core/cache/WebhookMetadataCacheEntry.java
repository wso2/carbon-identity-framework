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

package org.wso2.carbon.identity.webhook.metadata.api.core.cache;

import org.wso2.carbon.identity.core.cache.CacheEntry;
import org.wso2.carbon.identity.webhook.metadata.internal.model.WebhookMetadataProperty;

import java.util.Map;

/**
 * Cache entry for webhook metadata cache.
 * This class holds the webhook metadata properties in a cache entry.
 */
public class WebhookMetadataCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 202406120101L;
    private Map<String, WebhookMetadataProperty> webhookMetadataProperties;

    /**
     * Constructor.
     *
     * @param webhookMetadataProperties Webhook metadata properties object.
     */
    public WebhookMetadataCacheEntry(Map<String, WebhookMetadataProperty> webhookMetadataProperties) {

        this.webhookMetadataProperties = webhookMetadataProperties;
    }

    /**
     * Get webhook.
     *
     * @return WebhookMetadataProperties object.
     */
    public Map<String, WebhookMetadataProperty> getWebhookMetadataProperties() {

        return webhookMetadataProperties;
    }
}
