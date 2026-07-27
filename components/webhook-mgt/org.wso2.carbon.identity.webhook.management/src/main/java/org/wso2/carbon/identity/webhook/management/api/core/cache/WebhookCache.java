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

package org.wso2.carbon.identity.webhook.management.api.core.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Cache for Webhook Management.
 * This class is used to store and retrieve Webhook objects using WebhookCacheKey.
 */
public class WebhookCache extends BaseCache<WebhookCacheKey, WebhookCacheEntry> {

    private static final String CACHE_NAME = "WebhookCache";
    private static final WebhookCache INSTANCE = new WebhookCache();

    /**
     * Private constructor to enforce Singleton pattern.
     */
    private WebhookCache() {

        super(CACHE_NAME);
    }

    /**
     * Get webhook cache instance.
     *
     * @return WebhookCache instance
     */
    public static WebhookCache getInstance() {

        CarbonUtils.checkSecurity();
        return INSTANCE;
    }
}
