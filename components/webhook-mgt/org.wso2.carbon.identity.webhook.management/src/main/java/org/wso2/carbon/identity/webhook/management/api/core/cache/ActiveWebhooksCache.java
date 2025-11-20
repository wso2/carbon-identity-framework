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
 * Cache for active webhooks by profile, version, channel, and tenant.
 */
public class ActiveWebhooksCache extends BaseCache<ActiveWebhooksCacheKey, ActiveWebhooksCacheEntry> {

    private static final String CACHE_NAME = "ActiveWebhooksCache";
    private static final ActiveWebhooksCache INSTANCE = new ActiveWebhooksCache();

    /**
     * Private constructor to enforce Singleton pattern.
     */
    private ActiveWebhooksCache() {

        super(CACHE_NAME);
    }

    /**
     * Get active webhooks cache instance.
     *
     * @return ActiveWebhooksCache instance.
     */
    public static ActiveWebhooksCache getInstance() {

        CarbonUtils.checkSecurity();
        return INSTANCE;
    }
}
