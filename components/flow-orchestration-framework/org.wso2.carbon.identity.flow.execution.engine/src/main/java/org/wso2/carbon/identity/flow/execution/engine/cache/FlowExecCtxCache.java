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

package org.wso2.carbon.identity.flow.execution.engine.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.store.FlowContextStore;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;

import java.util.Optional;

/**
 * Cache for FlowExecutionContext.
 */
public class FlowExecCtxCache extends BaseCache<FlowExecCtxCacheKey, FlowExecCtxCacheEntry> {

    private static final String FLOW_CONTEXT_CACHE_NAME = "FlowExecCtxCache";
    private static final Log LOG = LogFactory.getLog(FlowExecCtxCache.class);
    private static final FlowExecCtxCache instance = new FlowExecCtxCache(FLOW_CONTEXT_CACHE_NAME,
            true);

    private FlowExecCtxCache(String cacheName, boolean isTemporary) {

        super(cacheName, isTemporary);
    }

    /**
     * Get instance of FlowExecCtxCache.
     */
    public static FlowExecCtxCache getInstance() {

        LOG.debug("Returning instance of FlowExecCtxCache.");
        return instance;
    }

    /**
     * Add the FlowExecCtxCacheEntry to the cache.
     *
     * @param key   Flow execution context cache key.
     * @param entry Flow execution context cache entry.
     */
    public void addToCache(FlowExecCtxCacheKey key, FlowExecCtxCacheEntry entry) throws FlowEngineException {

        String tenantName = FlowExecutionEngineUtils.resolveTenantDomain();
        if (tenantName != null) {
            super.addToCache(key, entry, tenantName);
            FlowContextStore.getInstance().storeContext(key.getContextId(), entry.getContext());
            LOG.debug("Added FlowExecutionContext to cache for key: " + key);
        }
    }

    /**
     * Get the FlowExecCtxCacheEntry from the cache for the given key.
     *
     * @param key Flow execution context cache key.
     * @return Flow execution context cache entry.
     */
    public FlowExecCtxCacheEntry getValueFromCache(FlowExecCtxCacheKey key) throws FlowEngineException {

        String tenantName = FlowExecutionEngineUtils.resolveTenantDomain();
        FlowExecCtxCacheEntry entry = super.getValueFromCache(key, tenantName);
        if (entry == null) {
            Optional<FlowExecutionContext> context = FlowContextStore.getInstance().getContext(key.getContextId());
            if (context.isPresent()) {
                entry = new FlowExecCtxCacheEntry(context.get());
                super.addToCache(key, entry, tenantName);
                LOG.debug("FlowExecutionContext found in store and added to cache for key: " + key.getContextId());
            } else {
                LOG.debug("No valid FlowExecutionContext found for the session data key: " + key.getContextId());
            }
        }
        return entry;
    }

    /**
     * Clear the cache entry for the given key.
     *
     * @param key Flow execution context cache key.
     */
    public void clearCacheEntry(FlowExecCtxCacheKey key) throws FlowEngineException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Clearing FlowExecutionContext cache entry for key: " + key.getContextId());
        }
        String tenantName = FlowExecutionEngineUtils.resolveTenantDomain();
        super.clearCacheEntry(key, tenantName);
        FlowContextStore.getInstance().deleteContext(key.getContextId());
    }
}
