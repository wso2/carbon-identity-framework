/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package org.wso2.carbon.identity.flow.engine.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

/**
 * Cache for FlowContext.
 */
public class FlowContextCache extends BaseCache<FlowContextCacheKey, FlowContextCacheEntry> {

    private static final String FLOW_CONTEXT_CACHE_NAME = "FlowContextCache";
    private static final Log LOG = LogFactory.getLog(FlowContextCache.class);
    private static final FlowContextCache instance = new FlowContextCache(FLOW_CONTEXT_CACHE_NAME,
            true);

    private FlowContextCache(String cacheName, boolean isTemporary) {

        super(cacheName, isTemporary);
    }

    /**
     * Get instance of FlowContextCache.
     */
    public static FlowContextCache getInstance() {

        LOG.debug("Returning instance of FlowContextCache.");
        return instance;
    }

    /**
     * Add the FlowContextCacheEntry to the cache.
     *
     * @param key   Flow context cache key.
     * @param entry Flow context cache entry.
     */
    public void addToCache(FlowContextCacheKey key, FlowContextCacheEntry entry) {

        String tenantName = FrameworkUtils.getLoginTenantDomainFromContext();
        if (tenantName != null) {
            int tenantId = IdentityTenantUtil.getTenantId(tenantName);
            super.addToCache(key, entry, tenantName);
            SessionDataStore.getInstance().storeSessionData(key.getContextId(), FLOW_CONTEXT_CACHE_NAME,
                    entry, tenantId);
        }
    }

    /**
     * Get the FlowContextCacheEntry from the cache for the given key.
     *
     * @param key Flow context cache key.
     * @return Flow context cache entry.
     */
    public FlowContextCacheEntry getValueFromCache(FlowContextCacheKey key) {

        String tenantName = FrameworkUtils.getLoginTenantDomainFromContext();
        FlowContextCacheEntry entry = super.getValueFromCache(key, tenantName);
        if (entry == null) {
            entry = (FlowContextCacheEntry) SessionDataStore.getInstance().
                    getSessionData(key.getContextId(), FLOW_CONTEXT_CACHE_NAME);
            if (entry != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found a valid FlowContextCacheEntry corresponding to the session data key : " +
                            key.getContextId() + " from the data store. ");
                }
                super.addToCache(key, entry, tenantName);
            }
        }
        return entry;
    }

    /**
     * Clear the cache entry for the given key.
     *
     * @param key Flow context cache key.
     */
    public void clearCacheEntry(FlowContextCacheKey key) {

        String tenantName = FrameworkUtils.getLoginTenantDomainFromContext();
        super.clearCacheEntry(key, tenantName);
        SessionDataStore.getInstance().clearSessionData(key.getContextId(), FLOW_CONTEXT_CACHE_NAME);
    }
}
