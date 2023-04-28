/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.core.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.CacheImpl;
import org.wso2.carbon.caching.impl.CachingConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.model.IdentityCacheConfig;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * A base class for all cache implementations in Identity modules. This maintains  caches in the tenanted space.
 * A copy of this class is maintained at org.wso2.carbon.identity.organization.management.service.cache component.
 *
 * @param <K> cache key type.
 * @param <V> cache value type.
 */
public abstract class BaseCache<K extends Serializable, V extends Serializable> {

    private static final Log log = LogFactory.getLog(BaseCache.class);
    private static final String CACHE_MANAGER_NAME = "IdentityApplicationManagementCacheManager";
    private CacheBuilder<K, V> cacheBuilder;
    private final List<AbstractCacheListener<K, V>> cacheListeners;
    private String cacheName;
    private final IdentityCacheConfig identityCacheConfig;

    public BaseCache(String cacheName) {

        this(cacheName, false, null);
    }

    public BaseCache(String cacheName, List<AbstractCacheListener<K, V>> cacheListeners) {

        this(cacheName, false, cacheListeners);
    }

    public BaseCache(String cacheName, boolean isTemp) {

        this(cacheName, isTemp, null);
    }

    public BaseCache(String cacheName, boolean isTemp,
                     List<AbstractCacheListener<K, V>> cacheListeners) {

        this.cacheName = CachingConstants.LOCAL_CACHE_PREFIX + cacheName;
        identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, cacheName);
        if (identityCacheConfig != null) {
            if (identityCacheConfig.isDistributed()) {
                this.cacheName = cacheName;
            }
            identityCacheConfig.setTemporary(isTemp);
        }
        if (cacheListeners != null) {
            this.cacheListeners = cacheListeners;
        } else {
            this.cacheListeners = Collections.emptyList();
        }
        if (log.isDebugEnabled()) {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            log.debug("Cache : " + cacheName + "  is initialized for tenant domain : " + tenantDomain);
        }
    }

    private Cache<K, V> getBaseCache() {

        Cache<K, V> cache;
        CacheManager cacheManager = Caching.getCacheManagerFactory()
                .getCacheManager(CACHE_MANAGER_NAME);

        if (getCacheTimeout() > 0 && cacheBuilder == null) {
            synchronized (cacheName.intern()) {
                if (cacheBuilder == null) {
                    cacheManager.removeCache(cacheName);
                    cacheBuilder = cacheManager.<K, V>createCacheBuilder(cacheName).
                            setExpiry(CacheConfiguration.ExpiryType.ACCESSED,
                                    new CacheConfiguration.Duration(TimeUnit.SECONDS, getCacheTimeout())).
                            setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
                                    new CacheConfiguration.Duration(TimeUnit.SECONDS, getCacheTimeout())).
                            setStoreByValue(false);
                    cache = cacheBuilder.build();

                    for (AbstractCacheListener<K, V> cacheListener : cacheListeners) {
                        if (cacheListener.isEnable()) {
                            this.cacheBuilder.registerCacheEntryListener(cacheListener);
                        }
                    }

                    setCapacity((CacheImpl<K, V>) cache);
                    if (log.isDebugEnabled()) {
                        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                        log.debug("Cache : " + cacheName + "  is built with timeout value : " +
                                getCacheTimeout() + " and capacity : " + getCapacity() +
                                " for tenant domain : " + tenantDomain);
                    }
                } else {
                    cache = cacheManager.getCache(cacheName);
                    setCapacity((CacheImpl<K, V>) cache);
                }
            }
        } else {
            cache = cacheManager.getCache(cacheName);
            setCapacity((CacheImpl<K, V>) cache);
        }
        return cache;
    }

    /**
     * Add a cache entry.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     * @param tenantDomain The tenant domain where the cache is maintained.
     */
    public void addToCache(K key, V entry, String tenantDomain) {

        if (!isEnabled()) {
            return;
        }

        try {
            startTenantFlow(tenantDomain);
            Cache<K, V> cache = getBaseCache();
            if (cache != null) {
                cache.put(key, entry);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Add a cache entry.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     * @param tenantId The tenant Id where the cache is maintained.
     */
    public void addToCache(K key, V entry, int tenantId) {

        if (!isEnabled()) {
            return;
        }

        try {
            startTenantFlow(tenantId);
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
     * @param tenantDomain The tenant domain where the cache is maintained.
     */
    public V getValueFromCache(K key, String tenantDomain) {

        if (!isEnabled()) {
            return null;
        }

        if (key == null) {
            return null;
        }

        try {
            startTenantFlow(tenantDomain);
            Cache<K, V> cache = getBaseCache();
            if (cache != null && cache.get(key) != null) {
                return cache.get(key);
            }
            return null;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Retrieves a cache entry.
     *
     * @param key CacheKey
     * @return Cached entry.
     * @param tenantId The tenant Id where the cache is maintained.
     */
    public V getValueFromCache(K key, int tenantId) {

        if (!isEnabled()) {
            return null;
        }

        if (key == null) {
            return null;
        }

        try {
            startTenantFlow(tenantId);
            Cache<K, V> cache = getBaseCache();
            if (cache != null && cache.get(key) != null) {
                return cache.get(key);
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
     * @param tenantDomain The tenant domain where the cache is maintained.
     */
    public void clearCacheEntry(K key, String tenantDomain) {

        if (!isEnabled()) {
            return;
        }

        try {
            startTenantFlow(tenantDomain);
            Cache<K, V> cache = getBaseCache();
            if (cache != null) {
                cache.remove(key);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Clears a cache entry.
     *
     * @param key Key to clear cache.
     * @param tenantId The tenant Id where the cache is maintained.
     */
    public void clearCacheEntry(K key, int tenantId) {

        if (!isEnabled()) {
            return;
        }

        try {
            startTenantFlow(tenantId);
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
     *
     * @param tenantDomain The tenant domain where the cache is maintained.
     */
    public void clear(String tenantDomain) {

        if (!isEnabled()) {
            return;
        }

        try {
            startTenantFlow(tenantDomain);
            Cache<K, V> cache = getBaseCache();
            if (cache != null) {
                cache.removeAll();
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Remove everything in the cache.
     *
     * @param tenantId The tenant Id where the cache is maintained.
     */
    public void clear(int tenantId) {

        if (!isEnabled()) {
            return;
        }

        try {
            startTenantFlow(tenantId);
            Cache<K, V> cache = getBaseCache();
            if (cache != null) {
                cache.removeAll();
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public boolean isEnabled() {

        if (identityCacheConfig != null) {
            return identityCacheConfig.isEnabled();
        }
        return true;
    }

    public int getCacheTimeout() {

        if (identityCacheConfig != null && identityCacheConfig.getTimeout() > 0) {
            return identityCacheConfig.getTimeout();
        }
        return -1;
    }

    public int getCapacity() {

        if (identityCacheConfig != null && identityCacheConfig.getCapacity() > 0) {
            return identityCacheConfig.getCapacity();
        }
        return -1;
    }

    public void setCapacity(CacheImpl<K, V> cache) {

        if (getCapacity() > 0) {
            cache.setCapacity(getCapacity());
        }
    }

    private void startTenantFlow(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantId(IdentityTenantUtil.getTenantId(tenantDomain));
    }

    private void startTenantFlow(int tenantId) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(IdentityTenantUtil.getTenantDomain(tenantId));
    }
}
