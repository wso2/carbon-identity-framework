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

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory cache for debug callback results.
 * Stores JSON-serialized debug results keyed by state parameter.
 * Results are cached temporarily during the debug flow and retrieved
 * by the debug endpoint to display to users.
 */
public final class DebugResultCache {

    private static final Log LOG = LogFactory.getLog(DebugResultCache.class);
    private static final Map<String, String> CACHE = new HashMap<>();

    /**
     * Adds a debug result to the cache.
     *
     * @param state The state parameter (cache key).
     * @param result The JSON-serialized debug result.
     */
    public static synchronized void add(String state, String result) {
        if (state != null && result != null) {
            CACHE.put(state, result);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug result cached for state: " + state);
            }
        }
    }

    /**
     * Retrieves a debug result from the cache.
     *
     * @param state The state parameter (cache key).
     * @return The JSON-serialized debug result or null if not found.
     */
    public static synchronized String get(String state) {
        if (state != null) {
            String result = CACHE.get(state);
            if (result != null && LOG.isDebugEnabled()) {
                LOG.debug("Debug result retrieved from cache for state: " + state);
            }
            return result;
        }
        return null;
    }

    /**
     * Removes a debug result from the cache.
     *
     * @param state The state parameter (cache key).
     */
    public static synchronized void remove(String state) {
        if (state != null) {
            CACHE.remove(state);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug result removed from cache for state: " + state);
            }
        }
    }

    /**
     * Clears all debug results from the cache.
     */
    public static synchronized void clear() {
        CACHE.clear();
        LOG.debug("Debug result cache cleared");
    }

    /**
     * Gets the current cache size.
     *
     * @return Number of cached results.
     */
    public static synchronized int size() {
        return CACHE.size();
    }

    private DebugResultCache() {
        // Prevent instantiation.
    }
}
