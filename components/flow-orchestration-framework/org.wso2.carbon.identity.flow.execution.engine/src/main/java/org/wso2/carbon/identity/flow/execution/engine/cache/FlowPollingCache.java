/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;

/**
 * Per-tenant cache of polling entries. The flow context store is the source of truth and every change clears the
 * cached copy.
 */
public class FlowPollingCache extends BaseCache<FlowPollingCacheKey, FlowPollingCacheEntry> {

    private static final String FLOW_POLLING_CACHE_NAME = "FlowPollingCache";
    private static final FlowPollingCache INSTANCE = new FlowPollingCache(FLOW_POLLING_CACHE_NAME);

    private FlowPollingCache(String cacheName) {

        super(cacheName);
    }

    public static FlowPollingCache getInstance() {

        return INSTANCE;
    }

    /**
     * Add an entry if the key has none, without invalidating other nodes.
     *
     * @param key   Cache key.
     * @param entry Cache entry.
     * @throws FlowEngineException If the tenant cannot be resolved.
     */
    public void addToCacheOnRead(FlowPollingCacheKey key, FlowPollingCacheEntry entry) throws FlowEngineException {

        super.addToCacheOnRead(key, entry, FlowExecutionEngineUtils.resolveTenantDomain());
    }

    /**
     * Get the entry for the given key.
     *
     * @param key Cache key.
     * @return Cache entry, or null.
     * @throws FlowEngineException If the tenant cannot be resolved.
     */
    public FlowPollingCacheEntry getValueFromCache(FlowPollingCacheKey key) throws FlowEngineException {

        return super.getValueFromCache(key, FlowExecutionEngineUtils.resolveTenantDomain());
    }

    /**
     * Clear the entry for the given key, on all nodes when cache invalidation is enabled.
     *
     * @param key Cache key.
     * @throws FlowEngineException If the tenant cannot be resolved.
     */
    public void clearCacheEntry(FlowPollingCacheKey key) throws FlowEngineException {

        super.clearCacheEntry(key, FlowExecutionEngineUtils.resolveTenantDomain());
    }
}
