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

package org.wso2.carbon.identity.core.util;

import org.wso2.carbon.identity.core.cache.CacheEntry;

/**
 * Utility class for cache operations.
 */
public class IdentityCacheUtil {

    private IdentityCacheUtil() {}

    /**
     * Check if a cache entry has expired.
     *
     * @param cacheEntry    Cache entry to check for expiration.
     * @return              true if the cache entry has expired or is null, false otherwise.
     */
    public static boolean isCacheEntryExpired(CacheEntry cacheEntry) {

        if (cacheEntry == null) {
            return true;
        }
        long validityPeriod = cacheEntry.getValidityPeriod();

        // If validityPeriod is 0, it means the entry doesn't expire.
        if (validityPeriod == 0L) {
            return false;
        }
        return System.nanoTime() > validityPeriod;
    }
}
