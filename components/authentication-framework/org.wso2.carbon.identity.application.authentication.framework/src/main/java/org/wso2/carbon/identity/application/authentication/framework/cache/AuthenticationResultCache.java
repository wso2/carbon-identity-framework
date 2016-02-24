/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.common.cache.BaseCache;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 * This cache keeps the information about the authentication result from the framework.
 */
public class AuthenticationResultCache extends
        BaseCache<AuthenticationResultCacheKey, AuthenticationResultCacheEntry> {

    private static Log log = LogFactory.getLog(AuthenticationResultCache.class);

    private static final String CACHE_NAME = "AuthenticationResultCache";

    private static volatile AuthenticationResultCache instance;

    private boolean isTemporarySessionDataPersistEnabled = false;

    /**
     * Private constructor which will not allow to create objects of this class from outside
     */
    private AuthenticationResultCache() {
        super(CACHE_NAME);
        if (IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary") != null) {
            isTemporarySessionDataPersistEnabled = Boolean.parseBoolean(
                    IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary"));
        }
    }

    /**
     * Singleton method
     *
     * @return AuthenticationResultCache
     */
    public static AuthenticationResultCache getInstance() {
        if (instance == null) {
            synchronized (AuthenticationResultCache.class) {
                if (instance == null) {
                    instance = new AuthenticationResultCache();
                }
            }
        }
        return instance;
    }

    /**
     * Add a cache entry.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     */
    public void addToCache(AuthenticationResultCacheKey key, AuthenticationResultCacheEntry entry) {
        super.addToCache(key, entry);
        if (isTemporarySessionDataPersistEnabled) {
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            if (entry.getResult() != null && entry.getResult().getSubject() != null) {
                String tenantDomain = entry.getResult().getSubject().getTenantDomain();
                if (tenantDomain != null) {
                    tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                }
            }
            SessionDataStore.getInstance().storeSessionData(key.getResultId(), CACHE_NAME, entry, tenantId);
        }
    }

    /**
     * Retrieves a cache entry.
     *
     * @param key CacheKey
     * @return Cached entry.
     */
    public AuthenticationResultCacheEntry getValueFromCache(AuthenticationResultCacheKey key) {
        AuthenticationResultCacheEntry entry = super.getValueFromCache(key);
        if (entry == null && isTemporarySessionDataPersistEnabled) {
            entry = (AuthenticationResultCacheEntry) SessionDataStore.getInstance().
                    getSessionData(key.getResultId(), CACHE_NAME);
        }
        return entry;
    }

    /**
     * Clears a cache entry.
     *
     * @param key Key to clear cache.
     */
    public void clearCacheEntry(AuthenticationResultCacheKey key) {
        super.clearCacheEntry(key);
        if (isTemporarySessionDataPersistEnabled) {
            SessionDataStore.getInstance().clearSessionData(key.getResultId(), CACHE_NAME);
        }
    }
}
