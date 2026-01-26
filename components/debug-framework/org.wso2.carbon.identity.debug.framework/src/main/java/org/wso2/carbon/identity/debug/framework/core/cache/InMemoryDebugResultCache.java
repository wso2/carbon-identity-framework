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

package org.wso2.carbon.identity.debug.framework.core.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.core.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.core.DebugResultCacheProvider;
import org.wso2.carbon.identity.debug.framework.exception.CacheException;
import org.wso2.carbon.identity.debug.framework.model.DebugResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * In-memory implementation of DebugResultCacheProvider.
 * Uses ConcurrentHashMap for thread-safe access.
 * Results are cached with configurable TTL and automatically cleaned up.
 */
public class InMemoryDebugResultCache extends DebugResultCacheProvider {

    private static final Log LOG = LogFactory.getLog(InMemoryDebugResultCache.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, CacheEntry> cache;
    private final int expiryMinutes;

    /**
     * Internal cache entry holding result and expiry time.
     */
    private static class CacheEntry {

        final Object data;
        final long expiryTime;
        final long createdAt;

        CacheEntry(Object data, int expiryMinutes) {
            this.data = data;
            this.createdAt = System.currentTimeMillis();
            this.expiryTime = createdAt + TimeUnit.MINUTES.toMillis(expiryMinutes);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    /**
     * Constructs an InMemoryDebugResultCache with default expiry (15 minutes).
     */
    public InMemoryDebugResultCache() {

        this(DebugFrameworkConstants.CACHE_EXPIRY_MINUTES);
    }

    /**
     * Constructs an InMemoryDebugResultCache with custom expiry.
     *
     * @param expiryMinutes Cache entry TTL in minutes.
     */
    public InMemoryDebugResultCache(int expiryMinutes) {

        this.cache = new ConcurrentHashMap<>();
        this.expiryMinutes = expiryMinutes;
        startCleanupScheduler();
    }

    @Override
    public void cache(String key, DebugResult result) throws CacheException {

        if (key == null || result == null) {
            throw new CacheException("INVALID_CACHE_PARAMS", "Cache key and result cannot be null");
        }
        try {
            cache.put(key, new CacheEntry(result, expiryMinutes));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug result cached with key: " + key);
            }
        } catch (Exception e) {
            throw new CacheException("CACHE_WRITE_ERROR", "Failed to cache result: " + e.getMessage(), e);
        }
    }

    @Override
    public void cacheJson(String key, String json) throws CacheException {

        if (key == null || json == null) {
            throw new CacheException("INVALID_CACHE_PARAMS", "Cache key and JSON cannot be null");
        }
        try {
            cache.put(key, new CacheEntry(json, expiryMinutes));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug result JSON cached with key: " + key);
            }
        } catch (Exception e) {
            throw new CacheException("CACHE_WRITE_ERROR", "Failed to cache JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public DebugResult getResult(String key) throws CacheException {

        if (key == null) {
            throw new CacheException("INVALID_KEY", "Cache key cannot be null");
        }
        try {
            CacheEntry entry = cache.get(key);
            if (entry == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug result not found in cache for key: " + key);
                }
                return null;
            }

            if (entry.isExpired()) {
                cache.remove(key);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug result expired for key: " + key);
                }
                return null;
            }

            if (entry.data instanceof DebugResult) {
                return (DebugResult) entry.data;
            } else if (entry.data instanceof String) {
                // Try to deserialize JSON string to DebugResult
                return MAPPER.readValue((String) entry.data, DebugResult.class);
            }

            return null;
        } catch (Exception e) {
            throw new CacheException("CACHE_READ_ERROR", "Failed to retrieve result: " + e.getMessage(), e);
        }
    }

    @Override
    public String getResultJson(String key) throws CacheException {

        if (key == null) {
            throw new CacheException("INVALID_KEY", "Cache key cannot be null");
        }
        try {
            CacheEntry entry = cache.get(key);
            if (entry == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug result JSON not found in cache for key: " + key);
                }
                return null;
            }

            if (entry.isExpired()) {
                cache.remove(key);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug result JSON expired for key: " + key);
                }
                return null;
            }

            if (entry.data instanceof String) {
                return (String) entry.data;
            } else if (entry.data instanceof DebugResult) {
                // Serialize DebugResult to JSON
                return MAPPER.writeValueAsString(entry.data);
            }

            return null;
        } catch (Exception e) {
            throw new CacheException("CACHE_READ_ERROR", "Failed to retrieve JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public void remove(String key) throws CacheException {

        if (key == null) {
            throw new CacheException("INVALID_KEY", "Cache key cannot be null");
        }
        try {
            cache.remove(key);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug result removed from cache with key: " + key);
            }
        } catch (Exception e) {
            throw new CacheException("CACHE_DELETE_ERROR", "Failed to remove result: " + e.getMessage(), e);
        }
    }

    @Override
    public void clear() throws CacheException {

        try {
            cache.clear();
            LOG.info("Debug result cache cleared");
        } catch (Exception e) {
            throw new CacheException("CACHE_CLEAR_ERROR", "Failed to clear cache: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String key) throws CacheException {

        if (key == null) {
            throw new CacheException("INVALID_KEY", "Cache key cannot be null");
        }
        try {
            CacheEntry entry = cache.get(key);
            if (entry == null) {
                return false;
            }
            if (entry.isExpired()) {
                cache.remove(key);
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new CacheException("CACHE_CHECK_ERROR", "Failed to check existence: " + e.getMessage(), e);
        }
    }

    @Override
    public long getSize() {

        return cache.size();
    }

    @Override
    public void maintain() {

        try {
            int cleanedCount = 0;
            for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
                if (entry.getValue().isExpired()) {
                    cache.remove(entry.getKey());
                    cleanedCount++;
                }
            }
            if (cleanedCount > 0 && LOG.isDebugEnabled()) {
                LOG.debug("Cleaned " + cleanedCount + " expired cache entries");
            }
        } catch (Exception e) {
            LOG.error("Error during cache maintenance: " + e.getMessage(), e);
        }
    }

    /**
     * Starts a background scheduler for periodic cache cleanup.
     */
    private void startCleanupScheduler() {
        
        try {
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("DebugResultCache-Cleaner");
                t.setDaemon(true);
                return t;
            }).scheduleWithFixedDelay(
                    this::maintain,
                    expiryMinutes,
                    expiryMinutes,
                    TimeUnit.MINUTES
            );
            LOG.info("Debug result cache cleanup scheduler started");
        } catch (Exception e) {
            LOG.warn("Failed to start cache cleanup scheduler: " + e.getMessage(), e);
        }
    }
}
