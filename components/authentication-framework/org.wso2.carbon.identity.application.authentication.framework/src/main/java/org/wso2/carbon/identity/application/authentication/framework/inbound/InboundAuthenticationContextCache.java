/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.common.cache.BaseCache;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

public class InboundAuthenticationContextCache
        extends BaseCache<InboundAuthenticationContextCacheKey, InboundAuthenticationContextCacheEntry> {

    private static final String INBOUND_AUTHENTICATION_CONTEXT_CACHE_NAME = "InboundAuthenticationContextCache";
    private static volatile InboundAuthenticationContextCache instance;
    private boolean enableRequestScopeCache = false;

    private InboundAuthenticationContextCache(String cacheName) {
        super(cacheName);
        if (IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary") != null) {
            enableRequestScopeCache = Boolean
                    .parseBoolean(IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary"));
        }
    }

    public static InboundAuthenticationContextCache getInstance() {
        if (instance == null) {
            synchronized (InboundAuthenticationContextCache.class) {
                if (instance == null) {
                    instance = new InboundAuthenticationContextCache(INBOUND_AUTHENTICATION_CONTEXT_CACHE_NAME);
                }
            }
        }
        return instance;
    }

    public void addToCache(InboundAuthenticationContextCacheKey key, InboundAuthenticationContextCacheEntry entry) {
        super.addToCache(key, entry);
        if (enableRequestScopeCache) {
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            String tenantDomain = entry.getInboundAuthenticationContext().getTenantDomain();
            if (tenantDomain != null) {
                tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            }
            SessionDataStore.getInstance().storeSessionData(key.getResultId(),
                    INBOUND_AUTHENTICATION_CONTEXT_CACHE_NAME, entry, tenantId);
        }
    }

    public InboundAuthenticationContextCacheEntry getValueFromCache(InboundAuthenticationContextCacheKey key) {
        InboundAuthenticationContextCacheEntry entry = super.getValueFromCache(key);
        if (entry == null && enableRequestScopeCache) {
            entry = (InboundAuthenticationContextCacheEntry) SessionDataStore.getInstance()
                    .getSessionData(key.getResultId(), INBOUND_AUTHENTICATION_CONTEXT_CACHE_NAME);
        }
        return entry;
    }

    public void clearCacheEntry(InboundAuthenticationContextCacheKey key) {
        super.clearCacheEntry(key);
        if (enableRequestScopeCache) {
            SessionDataStore.getInstance().clearSessionData(key.getResultId(),
                    INBOUND_AUTHENTICATION_CONTEXT_CACHE_NAME);
        }
    }
}
