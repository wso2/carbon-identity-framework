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
 * This cache keeps all parameters and headers which are directed towards authentication
 * framework. Whenever a request to authentication framework comes, The relevant component which
 * sends the request saves all required information to this cache, which are retrieved later from
 * authentication framework
 */
public class AuthenticationRequestCache extends
        BaseCache<AuthenticationRequestCacheKey, AuthenticationRequestCacheEntry> {

    private static Log log = LogFactory.getLog(AuthenticationRequestCache.class);
    private static final String AUTHENTICATION_REQUEST_CACHE_NAME = "AuthenticationRequestCache";
    private static volatile AuthenticationRequestCache instance;
    private boolean isTemporarySessionDataPersistEnabled = false;

    /**
     * Private constructor which will not allow to create objects of this class from outside
     */
    private AuthenticationRequestCache() {
        super(AUTHENTICATION_REQUEST_CACHE_NAME);
        if (IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary") != null) {
            isTemporarySessionDataPersistEnabled = Boolean.parseBoolean(
                    IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary"));
        }
    }

    /**
     * Singleton method
     *
     * @return AuthenticationRequestCache
     */
    public static AuthenticationRequestCache getInstance() {
        if (instance == null) {
            synchronized (AuthenticationRequestCache.class) {
                if (instance == null) {
                    instance = new AuthenticationRequestCache();
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
    public void addToCache(AuthenticationRequestCacheKey key, AuthenticationRequestCacheEntry entry){
        super.addToCache(key,entry);
        if(isTemporarySessionDataPersistEnabled){
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            String tenantDomain = entry.getAuthenticationRequest().getTenantDomain();
            if (tenantDomain != null) {
                tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            }
            SessionDataStore.getInstance().storeSessionData(key.getResultId(), AUTHENTICATION_REQUEST_CACHE_NAME,
                    entry, tenantId);
        }
    }

    /**
     * Retrieves a cache entry.
     *
     * @param key CacheKey
     * @return Cached entry.
     */
    public AuthenticationRequestCacheEntry getValueFromCache(AuthenticationRequestCacheKey key){
        AuthenticationRequestCacheEntry entry = super.getValueFromCache(key);
        if(entry == null && isTemporarySessionDataPersistEnabled){
            entry = (AuthenticationRequestCacheEntry) SessionDataStore.getInstance().
                    getSessionData(key.getResultId(), AUTHENTICATION_REQUEST_CACHE_NAME);
        }
        return entry;
    }

    /**
     * Clears a cache entry.
     *
     * @param key Key to clear cache.
     */
    public void clearCacheEntry(AuthenticationRequestCacheKey key){
        super.clearCacheEntry(key);
        if (isTemporarySessionDataPersistEnabled) {
            SessionDataStore.getInstance().clearSessionData(key.getResultId(), AUTHENTICATION_REQUEST_CACHE_NAME);
        }
    }
}
