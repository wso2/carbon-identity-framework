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

package org.wso2.carbon.identity.common.base.cache;

import java.io.Serializable;

/**
 * A base class for all cache implementations in Identity Application Management modules.
 * @param <K>
 * @param <V>
 */
public class BaseCache<K extends Serializable, V extends Serializable> {

    // TODO: Implement this class using Carbon Cache.

//    private static final String CACHE_MANAGER_NAME = "IdentityApplicationManagementCacheManager";
//    private CacheBuilder<K, V> cacheBuilder;
//    private String cacheName;
//    private List<AbstractCacheEntryListener> cacheListeners = new ArrayList<AbstractCacheEntryListener>();

    public BaseCache(String cacheName) {

//        this.cacheName = cacheName;
//        CacheConfig cacheConfig = IdentityUtils.getInstance().getCacheConfig()
//                .get(new CacheConfigKey(CACHE_MANAGER_NAME, cacheName));
//        if (cacheConfig != null && !cacheConfig.isDistributed()) {
//            this.cacheName = CachingConstants.LOCAL_CACHE_PREFIX + cacheName;
//        }
    }

//    private Cache<K, V> getBaseCache() {
//
//        Cache<K, V> cache = null;
//
//        CacheManager cacheManager = Caching.getCacheManagerFactory()
//                .getCacheManager(CACHE_MANAGER_NAME);
//
//        if (getCacheTimeout() > 0 && cacheBuilder == null) {
//            synchronized (cacheName.intern()) {
//                if (cacheBuilder == null) {
//                    cacheManager.removeCache(cacheName);
//                    cacheBuilder = cacheManager.<K, V>createCacheBuilder(cacheName).
//                            setExpiry(CacheConfiguration.ExpiryType.ACCESSED,
//                                    new CacheConfiguration
//                                            .Duration(TimeUnit.SECONDS, getCacheTimeout())).
//                            setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
//                                    new CacheConfiguration
//                                            .Duration(TimeUnit.SECONDS, getCacheTimeout())).
//                            setStoreByValue(false);
//                    cache = cacheBuilder.build();
//
//                    for (AbstractCacheEntryListener cacheListener : cacheListeners) {
//                        if (cacheListener.isEnable()) {
//                            this.cacheBuilder.registerCacheEntryListener(cacheListener);
//                        }
//                    }
//
//                    setCapacity((CacheImpl) cache);
//                } else {
//                    cache = cacheManager.getCache(cacheName);
//                    setCapacity((CacheImpl) cache);
//                }
//            }
//
//        } else {
//            cache = cacheManager.getCache(cacheName);
//            setCapacity((CacheImpl) cache);
//
//        }
//
//        return null;
//    }

    /**
     * Add a cache entry.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     */
    public void put(K key, V entry) {
//        if (!isEnabled()) {
//            return;
//        }
//
//        Cache<K, V> cache = getBaseCache();
//        if (cache != null) {
//            cache.put(key, entry);
//        }
    }

    /**
     * Retrieves a cache entry.
     *
     * @param key CacheKey
     * @return Cached entry.
     */
    public V get(K key) {
//        if (!isEnabled()) {
//            return null;
//        }
//
//        if (key == null) {
//            return null;
//        }
//
//        Cache<K, V> cache = getBaseCache();
//        if (cache != null && cache.get(key) != null) {
//            return (V) cache.get(key);
//        }
        return null;
    }

    /**
     * Clears a cache entry.
     *
     * @param key Key to clear cache.
     */
    public void clear(K key) {
//        if (!isEnabled()) {
//            return;
//        }
//
//        Cache<K, V> cache = getBaseCache();
//        if (cache != null) {
//            cache.remove(key);
//        }
    }

    /**
     * Remove everything in the cache.
     */
    public void clear() {
//        if (!isEnabled()) {
//            return;
//        }
//
//        Cache<K, V> cache = getBaseCache();
//        if (cache != null) {
//            cache.removeAll();
//        }
    }

    public void addListener(AbstractCacheEntryListener listener) {
//        cacheListeners.add(listener);
    }

    public boolean isEnabled() {
//        CacheConfig cacheConfig = IdentityUtils.getInstance().getCacheConfig().get(new CacheConfigKey
//                (CACHE_MANAGER_NAME, cacheName));
//        if (cacheConfig != null) {
//            return cacheConfig.isEnabled();
//        }
        return true;
    }

    public int getCacheTimeout() {
//        CacheConfig cacheConfig = IdentityUtils.getInstance().getCacheConfig().get(new CacheConfigKey
//                (CACHE_MANAGER_NAME, cacheName));
//        if (cacheConfig != null && cacheConfig.getTimeout() > 0) {
//            return cacheConfig.getTimeout();
//        }
        return -1;
    }

    public int getCapacity() {
//        CacheConfig cacheConfig = IdentityUtils.getInstance().getCacheConfig().get(new CacheConfigKey
//                (CACHE_MANAGER_NAME, cacheName));
//        if (cacheConfig != null && cacheConfig.getCapacity() > 0) {
//            return cacheConfig.getCapacity();
//        }
        return -1;
    }

//    public void setCapacity(CacheImpl cache) {
//        if (getCapacity() > 0) {
//            cache.setCapacity(getCapacity());
//        }
//    }
}
