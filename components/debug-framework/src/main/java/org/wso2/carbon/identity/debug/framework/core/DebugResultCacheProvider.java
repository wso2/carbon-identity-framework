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

package org.wso2.carbon.identity.debug.framework.core;

import org.wso2.carbon.identity.debug.framework.exception.CacheException;
import org.wso2.carbon.identity.debug.framework.model.DebugResult;

/**
 * Abstract base class for caching debug results.
 * Implementations can use different backends: in-memory, Redis, database, etc.
 * This allows for flexible caching strategies depending on deployment requirements.
 */
public abstract class DebugResultCacheProvider {

    /**
     * Stores a debug result in the cache.
     *
     * @param key    Cache key (typically session ID or state parameter).
     * @param result DebugResult to cache.
     * @throws CacheException If caching fails.
     */
    public abstract void cache(String key, DebugResult result) throws CacheException;

    /**
     * Stores debug result as JSON string in cache.
     * Useful for simple caching without object serialization.
     *
     * @param key  Cache key.
     * @param json JSON string representation of result.
     * @throws CacheException If caching fails.
     */
    public abstract void cacheJson(String key, String json) throws CacheException;

    /**
     * Retrieves a cached debug result.
     *
     * @param key Cache key.
     * @return DebugResult if found, null if not found or expired.
     * @throws CacheException If retrieval fails.
     */
    public abstract DebugResult getResult(String key) throws CacheException;

    /**
     * Retrieves a cached debug result as JSON string.
     *
     * @param key Cache key.
     * @return JSON string if found, null if not found or expired.
     * @throws CacheException If retrieval fails.
     */
    public abstract String getResultJson(String key) throws CacheException;

    /**
     * Removes a cached debug result.
     *
     * @param key Cache key.
     * @throws CacheException If removal fails.
     */
    public abstract void remove(String key) throws CacheException;

    /**
     * Clears all cached debug results.
     * Use with caution in production environments.
     *
     * @throws CacheException If clear operation fails.
     */
    public abstract void clear() throws CacheException;

    /**
     * Checks if a cache entry exists.
     *
     * @param key Cache key.
     * @return true if entry exists, false otherwise.
     * @throws CacheException If check fails.
     */
    public abstract boolean exists(String key) throws CacheException;

    /**
     * Gets the number of items currently cached.
     *
     * @return Cache size.
     */
    public abstract long getSize();

    /**
     * Performs cleanup or maintenance operations on the cache.
     * Called periodically to clean expired entries or perform health checks.
     */
    public abstract void maintain();
}
