/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.core.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * In-memory cache for debug callback results.
 * Stores JSON-serialized debug results keyed by state parameter.
 * Results are cached temporarily during the debug flow and retrieved
 * by the debug endpoint to display to users.
 * Uses ConcurrentHashMap for better performance and thread-safety.
 * Results are automatically cleaned up after TTL expiry (default: 15 minutes).
 */
public final class DebugResultCache {

    private static final Log LOG = LogFactory.getLog(DebugResultCache.class);
    private static final int DEFAULT_CACHE_EXPIRY_MINUTES = 15;
    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();
    private static java.util.concurrent.ScheduledExecutorService cleanupExecutor;

    /**
     * Internal cache entry holding result and expiry time.
     */
    private static class CacheEntry {

        final String result;
        final long expiryTime;

        CacheEntry(String result, int expiryMinutes) {
            this.result = result;
            this.expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expiryMinutes);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    static {
        // Start cleanup scheduler to remove expired entries.
        startCleanupScheduler();
    }

    /**
     * Adds a debug result to the cache with default TTL.
     *
     * @param state The state parameter (cache key).
     * @param result The JSON-serialized debug result.
     */
    public static void add(String state, String result) {

        if (state == null || result == null) {
            LOG.warn("Cache.add: state and result cannot be null");
            return;
        }
        CACHE.put(state, new CacheEntry(result, DEFAULT_CACHE_EXPIRY_MINUTES));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug result cached for state");
        }
    }

    /**
     * Retrieves a debug result from the cache.
     * Automatically removes expired entries.
     * Uses atomic operations for thread-safe access.
     *
     * @param state The state parameter (cache key).
     * @return The JSON-serialized debug result or null if not found or expired.
     */
    public static String get(String state) {

        if (state == null) {
            return null;
        }
        CacheEntry entry = CACHE.get(state);
        if (entry == null) {
            return null;
        }
        // Atomically check and remove expired entries to avoid TOCTOU race conditions
        if (entry.isExpired()) {
            // Use ConcurrentHashMap's atomic remove with value check
            CACHE.remove(state, entry);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug result expired in cache");
            }
            return null;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug result retrieved from cache");
        }
        return entry.result;
    }

    /**
     * Removes a debug result from the cache.
     *
     * @param state The state parameter (cache key).
     */
    public static void remove(String state) {

        if (state == null) {
            return;
        }
        CACHE.remove(state);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug result removed from cache");
        }
    }

    /**
     * Clears all debug results from the cache.
     */
    public static void clear() {

        CACHE.clear();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug result cache cleared");
        }
    }

    /**
     * Gets the current cache size.
     *
     * @return Number of cached results.
     */
    public static int size() {

        return CACHE.size();
    }

    /**
     * Performs cache maintenance by removing expired entries.
     */
    private static void maintain() {

        try {
            int cleanedCount = 0;
            Iterator<Map.Entry<String, CacheEntry>> iterator = CACHE.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CacheEntry> entry = iterator.next();
                if (entry.getValue().isExpired()) {
                    iterator.remove();
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
    private static void startCleanupScheduler() {

        try {
            cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setName("DebugResultCache-Cleaner");
                t.setDaemon(true);
                return t;
            });
            cleanupExecutor.scheduleWithFixedDelay(
                    DebugResultCache::maintain,
                    DEFAULT_CACHE_EXPIRY_MINUTES,
                    DEFAULT_CACHE_EXPIRY_MINUTES,
                    TimeUnit.MINUTES
            );
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug result cache cleanup scheduler started");
            }
            
            // Register shutdown hook to ensure executor is properly shut down on module unload.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
                        cleanupExecutor.shutdownNow();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Debug result cache cleanup scheduler shut down");
                        }
                    }
                } catch (Exception e) {
                    LOG.warn("Error during cleanup scheduler shutdown: " + e.getMessage(), e);
                }
            }));
        } catch (Exception e) {
            LOG.warn("Failed to start cache cleanup scheduler: " + e.getMessage(), e);
        }
    }

    private DebugResultCache() {
        
        // Prevent instantiation.
    }
}
