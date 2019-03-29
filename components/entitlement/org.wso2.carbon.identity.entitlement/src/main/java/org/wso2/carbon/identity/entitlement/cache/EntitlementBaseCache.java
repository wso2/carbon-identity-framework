/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;

import javax.cache.Cache;
import javax.cache.CacheBuilder;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import java.util.concurrent.TimeUnit;

/**
 * A base class for all cache implementations in user entitlement module.
 */
public class EntitlementBaseCache<K extends IdentityCacheKey, V extends Object> {

    private static final String ENTITLEMENT_CACHE_MANAGER = "ENTITLEMENT_CACHE_MANAGER";
    private static Log log = LogFactory.getLog(EntitlementBaseCache.class);
    private String Entitlement_CACHE_NAME;
    private int cacheTimeout;
    private CacheBuilder<K, V> cacheBuilder;
    private CacheEntryUpdatedListener<K, V> cacheEntryUpdatedListener;
    private CacheEntryCreatedListener<K, V> cacheEntryCreatedListener;

    /**
     * Create Entitlement cache object
     *
     * @param cacheName Name for the cache, entitlement caches differentiate from this name.
     * @param timeout   Cache timeout in milliseconds.
     */
    public EntitlementBaseCache(String cacheName, int timeout) {
        this.Entitlement_CACHE_NAME = cacheName;
        if (timeout > 0) {
            this.cacheTimeout = timeout;
        } else {
            this.cacheTimeout = -1;
        }
        if (log.isDebugEnabled()) {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            log.debug(
                    "Cache : " + Entitlement_CACHE_NAME + "  is initialized" + " for tenant domain : " + tenantDomain);
        }
    }

    /**
     * Create Entiltement cache object
     *
     * @param cacheName Name for the cache, entitlement caches differentiate from this name.
     */
    public EntitlementBaseCache(String cacheName) {
        this.Entitlement_CACHE_NAME = cacheName;
        this.cacheTimeout = -1;
        if (log.isDebugEnabled()) {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            log.debug(
                    "Cache : " + Entitlement_CACHE_NAME + "  is initialized" + " for tenant domain : " + tenantDomain);
        }
    }

    /**
     * Getting existing cache if the cache available, else returns a newly created cache.
     * This logic handles by javax.cache implementation
     *
     * @return
     */
    private Cache<K, V> getEntitlementCache() {

        Cache<K, V> cache = null;
        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(ENTITLEMENT_CACHE_MANAGER);
        if (this.cacheTimeout > 0) {
            if (cacheBuilder == null) {
                synchronized (Entitlement_CACHE_NAME.intern()) {
                    if (cacheBuilder == null) {
                        cacheManager.removeCache(Entitlement_CACHE_NAME);
                        this.cacheBuilder = cacheManager.<K, V>createCacheBuilder(Entitlement_CACHE_NAME).
                                setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
                                          new CacheConfiguration.Duration(TimeUnit.SECONDS, cacheTimeout)).
                                setStoreByValue(false);
                        cache = cacheBuilder.build();

                        if (cacheEntryUpdatedListener != null) {
                            this.cacheBuilder.registerCacheEntryListener(cacheEntryUpdatedListener);
                        }
                        if (cacheEntryCreatedListener != null) {
                            this.cacheBuilder.registerCacheEntryListener(cacheEntryCreatedListener);
                        }
                        if (log.isDebugEnabled()) {
                            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                            log.debug("Cache : " + Entitlement_CACHE_NAME + "  is built with time out value " + ": " +
                                      cacheTimeout + " for tenant domain : " + tenantDomain);
                        }
                    }
                }
            } else {
                cache = cacheManager.getCache(Entitlement_CACHE_NAME);
            }
        } else {
            cache = cacheManager.getCache(Entitlement_CACHE_NAME);
        }
        return cache;
    }

    public void initCacheBuilder() {
        getEntitlementCache();
    }

    /**
     * Add a cache entry.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     */
    public void addToCache(K key, V entry) {
        // Element already in the cache. Remove it first
        clearCacheEntry(key);
        updateToCache(key, entry);
    }


    /**
     * Update the cache without clearing the cache item
     *
     * @param key
     * @param entry
     */
    public void updateToCache(K key, V entry) {
        // Element already in the cache. Remove it first

        Cache<K, V> cache = getEntitlementCache();
        if (cache != null) {
            cache.put(key, entry);
        }
        if (log.isDebugEnabled()) {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            log.debug("Cache : " + Entitlement_CACHE_NAME + "  is populated with new entry " + "in tenant domain : " +
                      tenantDomain);
        }
    }

    /**
     * Retrieves a cache entry.
     *
     * @param key CacheKey
     * @return Cached entry.
     */
    public V getValueFromCache(K key) {
        Cache<K, V> cache = getEntitlementCache();
        if (cache != null) {
            if (cache.containsKey(key)) {
                if (log.isDebugEnabled()) {
                    String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                    log.debug("Cache : " + Entitlement_CACHE_NAME + "  is HIT " + "in tenant domain : " + tenantDomain);
                }
                return cache.get(key);
            }
        }
        if (log.isDebugEnabled()) {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            log.debug("Cache : " + Entitlement_CACHE_NAME + "  is MISSED " + "in tenant domain : " + tenantDomain);
        }
        return null;
    }

    /**
     * Clears a cache entry.
     *
     * @param key Key to clear cache.
     */
    public void clearCacheEntry(K key) {
        Cache<K, V> cache = getEntitlementCache();
        if (cache != null) {
            if (cache.containsKey(key)) {
                cache.remove(key);
                if (log.isDebugEnabled()) {
                    String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                    log.debug("Cache : " + Entitlement_CACHE_NAME + " entry is removed " + "in tenant domain : " +
                              tenantDomain);
                }
            }
        }
    }

    /**
     * Remove everything in the cache.
     */
    public void clear() {
        Cache<K, V> cache = getEntitlementCache();
        if (cache != null) {
            try {
                cache.removeAll();
                if (log.isDebugEnabled()) {
                    String tenantDomain = CarbonContext
                            .getThreadLocalCarbonContext().getTenantDomain();
                    log.debug("Cache : " + Entitlement_CACHE_NAME + " is cleared " + "in tenant domain : " +
                              tenantDomain);
                }
            } catch (Exception e) {
                //TODO - Handle the IdentityCacheKey exception in cluster env.
            }
        }
    }

    public void setCacheEntryUpdatedListener(CacheEntryUpdatedListener<K, V> cacheEntryUpdatedListener) {
        this.cacheEntryUpdatedListener = cacheEntryUpdatedListener;
    }

    public void setCacheEntryCreatedListener(CacheEntryCreatedListener<K, V> cacheEntryCreatedListener) {
        this.cacheEntryCreatedListener = cacheEntryCreatedListener;
    }
}
