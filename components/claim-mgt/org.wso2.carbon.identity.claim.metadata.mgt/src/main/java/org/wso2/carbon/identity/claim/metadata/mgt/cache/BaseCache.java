/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt.cache;

import org.wso2.carbon.caching.impl.CacheImpl;
import org.wso2.carbon.caching.impl.CachingConstants;
import org.wso2.carbon.identity.core.model.IdentityCacheConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * A base class for all cache implementations in Claim metatada management modules.
 */
public class BaseCache<K extends Serializable, V extends Serializable> {

    private static final String CACHE_MANAGER_NAME = "IdentityClaimMetadataMgtCacheManager";
    private CacheBuilder<K, V> cacheBuilder;
    private String cacheName;

    public BaseCache(String cacheName) {
        this.cacheName = cacheName;
        IdentityCacheConfig identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, cacheName);
        if (identityCacheConfig != null && !identityCacheConfig.isDistributed()) {
            this.cacheName = CachingConstants.LOCAL_CACHE_PREFIX + cacheName;
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
                                    new CacheConfiguration
                                            .Duration(TimeUnit.SECONDS, getCacheTimeout())).
                            setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
                                    new CacheConfiguration
                                            .Duration(TimeUnit.SECONDS, getCacheTimeout())).
                            setStoreByValue(false);
                    cache = cacheBuilder.build();
                    setCapacity((CacheImpl) cache);
                } else {
                    cache = cacheManager.getCache(cacheName);
                    setCapacity((CacheImpl) cache);
                }
            }

        } else {
            cache = cacheManager.getCache(cacheName);
            setCapacity((CacheImpl) cache);

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

        // Element already in the cache. Remove it first
        Cache<K, V> cache = getBaseCache();
        if (cache != null) {
            cache.put(key, entry);
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

        Cache<K, V> cache = getBaseCache();
        if (cache != null && cache.get(key) != null) {
            return (V) cache.get(key);
        }
        return null;
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

        Cache<K, V> cache = getBaseCache();
        if (cache != null) {
            cache.remove(key);
        }

    }

    /**
     * Remove everything in the cache.
     */
    public void clear() {
        if (!isEnabled()) {
            return;
        }

        Cache<K, V> cache = getBaseCache();
        if (cache != null) {
            cache.removeAll();
        }

    }

    public boolean isEnabled() {
        IdentityCacheConfig identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, cacheName);
        if (identityCacheConfig != null) {
            return identityCacheConfig.isEnabled();
        }
        return true;
    }

    public int getCacheTimeout() {
        IdentityCacheConfig identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, cacheName);
        if (identityCacheConfig != null && identityCacheConfig.getTimeout() > 0) {
            return identityCacheConfig.getTimeout();
        }
        return -1;
    }

    public int getCapacity() {
        IdentityCacheConfig identityCacheConfig = IdentityUtil.getIdentityCacheConfig(CACHE_MANAGER_NAME, cacheName);
        if (identityCacheConfig != null && identityCacheConfig.getCapacity() > 0) {
            return identityCacheConfig.getCapacity();
        }
        return -1;
    }

    public void setCapacity(CacheImpl cache) {
        if (getCapacity() > 0) {
            cache.setCapacity(getCapacity());
        }
    }
}
