/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.io.Serializable;

import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/**
 * Base cache for authentication flow related caches. The difference from the Base cache is,
 * AuthenticationBaseCache will maintain the caches in the tenant space if the Tenant Qualified Urls enabled.
 * Else the caches will be maintained in the super tenant.
 *
 * This is because when the Tenant Qualified Urls disabled, all the authentication flow requests will be received
 * in the common endpoints. So the tenant domain will not be resolved until the session data or application data
 * retrieved using the session identifier or application identifier. In this case tenanted caching will not help.
 * So the the caches will be maintained in the super tenant when the Tenant Qualified Urls.
 *
 * @param <K> cache key type.
 * @param <V> cache value type.
 */
public class AuthenticationBaseCache<K extends Serializable, V extends Serializable> extends BaseCache<K,V> {

    public AuthenticationBaseCache(String cacheName) {

        super(cacheName);
    }

    public AuthenticationBaseCache(String cacheName, boolean isTemp) {

        super(cacheName, isTemp);
    }

    /**
     * Add a cache entry. If TenantQualifiedUrls enabled, add to the cache of tenant from Context
     * else add to the super tenant.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     */
    public void addToCache(K key, V entry) {

        addToCache(key, entry, getTenantDomainFromContext());
    }

    /**
     * Retrieves a cache entry. If TenantQualifiedUrls enabled, retrieve from the cache of tenant from Context
     * else retrieve from the cache of super tenant.
     *
     * @param key CacheKey
     * @return Cached entry.
     */
    public V getValueFromCache(K key) {

        return getValueFromCache(key, getTenantDomainFromContext());
    }

    /**
     * Clears a cache entry. If TenantQualifiedUrls enabled, clears from the cache of tenant from Context
     * else clear from the cache of super tenant.
     *
     * @param key CacheKey
     */
    public void clearCacheEntry(K key) {

        clearCacheEntry(key, getTenantDomainFromContext());
    }

    private static String getTenantDomainFromContext() {

        // We use the tenant domain set in context only in tenant qualified URL mode.
        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            return IdentityTenantUtil.getTenantDomainFromContext();
        }
        return SUPER_TENANT_DOMAIN_NAME;
    }
}
