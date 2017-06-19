/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.common.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.CacheImpl;
import org.wso2.carbon.caching.impl.CachingConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.internal.DataHolder;
import org.wso2.carbon.identity.application.common.listener.AbstractCacheListener;
import org.wso2.carbon.identity.core.model.IdentityCacheConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * A base class for all cache implementations in Identity Application Management modules.
 */
public class BaseCache<K extends Serializable, V extends Serializable> {

    private static final Log log = LogFactory.getLog(BaseCache.class);

    private static final String CACHE_MANAGER_NAME = "IdentityApplicationManagementCacheManager";
    private static final String DEFAULT_CACHE_TIMEOUT_STRING = "Cache.DefaultCacheTimeout";
    private int defaultCacheTimeout = getDefaultCacheTimeout();

    private CacheBuilder<K, V> cacheBuilder;
    private String cacheName;
    private List<AbstractCacheListener> cacheListeners = new ArrayList<AbstractCacheListener>();

    public BaseCache(String cacheName) {
        this.cacheName = cacheName;
        IdentityCacheConfig identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, cacheName);
        if (identityCacheConfig != null && !identityCacheConfig.isDistributed()) {
            this.cacheName = CachingConstants.LOCAL_CACHE_PREFIX + cacheName;
        }
    }

    private Cache<K, V> getBaseCache() {

        Cache<K, V> cache = null;
        try {

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            CacheManager cacheManager = Caching.getCacheManagerFactory()
                    .getCacheManager(CACHE_MANAGER_NAME);

            if (cacheBuilder == null) {
                addListener(new ClusterCacheInvalidationRequestSender());
                synchronized (cacheName.intern()) {
                    if (cacheBuilder == null) {
                        cacheManager.removeCache(cacheName);
                        cacheBuilder = cacheManager.<K, V>createCacheBuilder(cacheName).
                                setExpiry(CacheConfiguration.ExpiryType.ACCESSED,
                                        new CacheConfiguration
                                                .Duration(TimeUnit.SECONDS, getCacheTimeout())).
                                setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
                                        new CacheConfiguration
                                                .Duration(TimeUnit.SECONDS, getCacheTimeout())).
                                setStoreByValue(false);
                        cache = cacheBuilder.build();

                        for (AbstractCacheListener cacheListener : cacheListeners) {
                            if (cacheListener.isEnable()) {
                                this.cacheBuilder.registerCacheEntryListener(cacheListener);
                            }
                        }

                        setCapacity(cache);
                    } else {
                        cache = cacheManager.getCache(cacheName);
                        setCapacity(cache);
                    }
                }
            } else {
                cache = cacheManager.getCache(cacheName);
                setCapacity(cache);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        return cache;
    }

    /**
     * Add a cache entry.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     */
    public void addToCache(K key, V entry) {
        if (!isEnabled()) {
            return;
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                    .getThreadLocalCarbonContext();
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            // Element already in the cache. Remove it first
            Cache<K, V> cache = getBaseCache();
            if (cache != null) {
                cache.put(key, entry);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Retrieves a cache entry.
     *
     * @param key CacheKey
     * @return Cached entry.
     */
    public V getValueFromCache(K key) {
        if (!isEnabled()) {
            return null;
        }

        if(key == null) {
            return null;
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                    .getThreadLocalCarbonContext();
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            Cache<K, V> cache = getBaseCache();
            if (cache != null && cache.get(key) != null) {
                return (V) cache.get(key);
            }
            return null;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Clears a cache entry.
     *
     * @param key Key to clear cache.
     */
    public void clearCacheEntry(K key) {
        if (!isEnabled()) {
            return;
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                    .getThreadLocalCarbonContext();
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            Cache<K, V> cache = getBaseCache();
            if (cache != null) {
                cache.remove(key);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Remove everything in the cache.
     */
    public void clear() {
        if (!isEnabled()) {
            return;
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                    .getThreadLocalCarbonContext();
            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            Cache<K, V> cache = getBaseCache();
            if (cache != null) {
                cache.removeAll();
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void addListener(AbstractCacheListener listener){
        if (log.isDebugEnabled()) {
            log.debug("Adding identity cache listener " + listener.getClass().getName());
        }
        cacheListeners.add(listener);
    }

    private void setCapacity(Cache cache) {
        if (! (cache instanceof CacheImpl)) {
            return;
        }

        long capacity = 5000L;
        IdentityCacheConfig identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, cacheName);
        if (identityCacheConfig != null && identityCacheConfig.getCapacity() > 0) {
            capacity = identityCacheConfig.getCapacity();
        }

        ((CacheImpl) cache).setCapacity(capacity);
    }

    private boolean isEnabled() {
        IdentityCacheConfig identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, cacheName);
        return identityCacheConfig == null || identityCacheConfig.isEnabled();
    }

    private int getCacheTimeout() {
        IdentityCacheConfig identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, cacheName);
        if (identityCacheConfig != null && identityCacheConfig.getTimeout() > 0) {
            return identityCacheConfig.getTimeout();
        }
        return defaultCacheTimeout;
    }

    /**
     * get default cache timeout in seconds from carbon.xml
     */
    private int getDefaultCacheTimeout() {
        try {
            String timeoutStr = DataHolder.getInstance().getServerConfigurationService()
                    .getFirstProperty(DEFAULT_CACHE_TIMEOUT_STRING);
            int timeout = Integer.parseInt(timeoutStr);
            return timeout * 60;
        } catch (NumberFormatException | NullPointerException e) {
            //default
            return 15 * 60;
        }
    }

}
