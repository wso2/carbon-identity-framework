/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.cache;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 * Cache for AuthenticationError to be display error messages provided in adaptive script in retry page.
 */
public class AuthenticationErrorCache extends
        AuthenticationBaseCache<AuthenticationErrorCacheKey, AuthenticationErrorCacheEntry> {

    private static final String CACHE_NAME = "AuthenticationErrorCache";

    private static final AuthenticationErrorCache instance = new AuthenticationErrorCache();

    private boolean isTemporarySessionDataPersistEnabled = false;

    /**
     * Private constructor which will not allow to create objects of this class from outside
     */
    private AuthenticationErrorCache() {

        super(CACHE_NAME, true);
        if (IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary") != null) {
            isTemporarySessionDataPersistEnabled = Boolean.parseBoolean(
                    IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary"));
        }
    }

    /**
     * Singleton method
     *
     * @return AuthenticationErrorCache
     */
    public static AuthenticationErrorCache getInstance() {

        return instance;
    }

    /**
     * Add a cache entry.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     */
    public void addToCache(AuthenticationErrorCacheKey key, AuthenticationErrorCacheEntry entry) {

        super.addToCache(key, entry);
        if (isTemporarySessionDataPersistEnabled) {
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            if (entry.getTenantDomain() != null) {
                String tenantDomain = entry.getTenantDomain();
                if (tenantDomain != null) {
                    tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                }
            }
            SessionDataStore.getInstance().storeSessionData(key.getErrorKey(), CACHE_NAME, entry, tenantId);
        }
    }

    /**
     * Retrieves a cache entry.
     *
     * @param key CacheKey
     * @return Cached entry.
     */
    public AuthenticationErrorCacheEntry getValueFromCache(AuthenticationErrorCacheKey key) {

        AuthenticationErrorCacheEntry entry = super.getValueFromCache(key);
        if (entry == null && isTemporarySessionDataPersistEnabled) {
            entry = (AuthenticationErrorCacheEntry) SessionDataStore.getInstance().
                    getSessionData(key.getErrorKey(), CACHE_NAME);
        }
        return entry;
    }

    /**
     * Clears a cache entry.
     *
     * @param key Key to clear cache.
     */
    public void clearCacheEntry(AuthenticationErrorCacheKey key) {

        super.clearCacheEntry(key);
        if (isTemporarySessionDataPersistEnabled) {
            SessionDataStore.getInstance().clearSessionData(key.getErrorKey(), CACHE_NAME);
        }
    }
}
