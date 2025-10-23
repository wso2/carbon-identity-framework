package org.wso2.carbon.identity.debug.framework;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A simple, in-memory, server-side cache to hold debug results.
 * Results are stored with a short expiry to prevent memory leaks.
 */
public class DebugResultCache {

    private static final Log LOG = LogFactory.getLog(DebugResultCache.class);
    private static final int CACHE_EXPIRY_MINUTES = 15;

    // Use ConcurrentHashMap for thread-safe access.
    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    // Cache entry class to hold the result and its expiry time.
    private static class CacheEntry {
        final String jsonResult;
        final long expiryTime;

        CacheEntry(String jsonResult) {
            this.jsonResult = jsonResult;
            this.expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(CACHE_EXPIRY_MINUTES);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    /**
     * Adds a debug result to the cache with a 5-minute expiry.
     * @param key The session ID (state).
     * @param jsonResult The JSON result string.
     */
    public static void add(String key, String jsonResult) {
        if (key == null || jsonResult == null) {
            return;
        }
        cache.put(key, new CacheEntry(jsonResult));
        LOG.info("Debug result cached");
    }

    /**
     * Retrieves a debug result from the cache and removes it.
     * Returns null if the key is not found or the entry is expired.
     * @param key The session ID (state).
     * @return The JSON result string or null.
     */
    public static String get(String key) {
        if (key == null) {
            return null;
        }
        CacheEntry entry = cache.remove(key);
        if (entry == null) {
            LOG.warn("No debug result found in cache for session: " + key);
            return null;
        }
        if (entry.isExpired()) {
            LOG.warn("Debug result for session " + key + " has expired and was removed.");
            return null;
        }
        return entry.jsonResult;
    }

    static {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("DebugResultCache-Cleaner");
            t.setDaemon(true);
            return t;
        }).scheduleWithFixedDelay(() -> {
            try {
                int expiredCount = 0;
                for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
                    if (entry.getValue().isExpired()) {
                        cache.remove(entry.getKey());
                        expiredCount++;
                    }
                }
                if (expiredCount > 0) {
                    LOG.debug("Cleared " + expiredCount + " expired debug cache entries.");
                }
            } catch (Exception e) {
                LOG.error("Error in debug cache cleaner task", e);
            }
        }, CACHE_EXPIRY_MINUTES, CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES);
    }
}
