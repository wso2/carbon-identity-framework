/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache for RegistrationContext.
 */
public class RegistrationContextCache extends BaseCache<RegistrationContextCacheKey, RegistrationContextCacheEntry> {

    private static final String REGISTRATION_CONTEXT_CACHE_NAME = "RegistrationContextCache";
    private static final Log LOG = LogFactory.getLog(RegistrationContextCache.class);
    private static final RegistrationContextCache instance = new RegistrationContextCache(REGISTRATION_CONTEXT_CACHE_NAME,
                                                                                          true);
    private RegistrationContextCache(String cacheName, boolean isTemporary) {

            super(cacheName, isTemporary);
    }

    /**
     * Get instance of RegistrationContextCache.
     */
    public static RegistrationContextCache getInstance() {

        LOG.debug("Returning instance of RegistrationContextCache.");
        return instance;
    }

    /**
     * Add the RegistrationContextCacheEntry to the cache.
     *
     * @param key   Registration context cache key.
     * @param entry Registration context cache entry.
     */
    public void addToCache(RegistrationContextCacheKey key, RegistrationContextCacheEntry entry) {

        String tenantName = FrameworkUtils.getLoginTenantDomainFromContext();
        super.addToCache(key, entry, tenantName);
    }

    /**
     * Get the RegistrationContextCacheEntry from the cache for the given key.
     *
     * @param key   Registration context cache key.
     * @return Registration context cache entry.
     */
    public RegistrationContextCacheEntry getValueFromCache(RegistrationContextCacheKey key) {

        String tenantName = FrameworkUtils.getLoginTenantDomainFromContext();
        return super.getValueFromCache(key, tenantName);
    }

    /**
     * Clear the cache entry for the given key.
     *
     * @param key   Registration context cache key.
     */
    public void clearCacheEntry(RegistrationContextCacheKey key) {

        String tenantName = FrameworkUtils.getLoginTenantDomainFromContext();
        super.clearCacheEntry(key, tenantName);
    }
}
