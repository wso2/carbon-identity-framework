/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.compatibility.settings.core.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache for storing compatibility settings per tenant.
 * Uses tenant domain as the cache key.
 */
public class CompatibilitySettingCache
        extends BaseCache<CompatibilitySettingCacheKey, CompatibilitySettingCacheEntry> {

    private static final String CACHE_NAME = "CompatibilitySettingCache";

    private static volatile CompatibilitySettingCache instance;

    /**
     * Private constructor for singleton pattern.
     */
    private CompatibilitySettingCache() {

        super(CACHE_NAME);
    }

    /**
     * Get the singleton instance of CompatibilitySettingCache.
     *
     * @return CompatibilitySettingCache instance.
     */
    public static CompatibilitySettingCache getInstance() {

        if (instance == null) {
            synchronized (CompatibilitySettingCache.class) {
                if (instance == null) {
                    instance = new CompatibilitySettingCache();
                }
            }
        }
        return instance;
    }

    /**
     * Add compatibility setting to cache for a tenant.
     *
     * @param tenantDomain        Tenant domain.
     * @param compatibilitySetting Compatibility setting to cache.
     */
    public void addToCache(String tenantDomain, CompatibilitySettingCacheEntry compatibilitySetting) {

        CompatibilitySettingCacheKey cacheKey = new CompatibilitySettingCacheKey(tenantDomain);
        super.addToCache(cacheKey, compatibilitySetting, tenantDomain);
    }

    /**
     * Get compatibility setting from cache for a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @return Compatibility setting cache entry, or null if not found.
     */
    public CompatibilitySettingCacheEntry getFromCache(String tenantDomain) {

        CompatibilitySettingCacheKey cacheKey = new CompatibilitySettingCacheKey(tenantDomain);
        return super.getValueFromCache(cacheKey, tenantDomain);
    }

    /**
     * Update compatibility setting in cache for a tenant.
     * This method clears the existing entry and adds the new one.
     *
     * @param tenantDomain        Tenant domain.
     * @param compatibilitySettingCacheEntry Updated compatibility setting.
     */
    public void updateCache(String tenantDomain, CompatibilitySettingCacheEntry compatibilitySettingCacheEntry) {

        CompatibilitySettingCacheKey cacheKey = new CompatibilitySettingCacheKey(tenantDomain);
        super.clearCacheEntry(cacheKey, tenantDomain);
        super.addToCache(cacheKey, compatibilitySettingCacheEntry, tenantDomain);
    }

    /**
     * Remove compatibility setting from cache for a tenant.
     *
     * @param tenantDomain Tenant domain.
     */
    public void clearFromCache(String tenantDomain) {

        CompatibilitySettingCacheKey cacheKey = new CompatibilitySettingCacheKey(tenantDomain);
        super.clearCacheEntry(cacheKey, tenantDomain);
    }
}

