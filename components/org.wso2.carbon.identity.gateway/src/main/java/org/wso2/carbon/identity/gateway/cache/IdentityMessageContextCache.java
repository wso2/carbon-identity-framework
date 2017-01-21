/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.gateway.cache;

import org.wso2.carbon.identity.common.base.cache.BaseCache;
import org.wso2.carbon.identity.gateway.api.IdentityMessageContext;

public class IdentityMessageContextCache extends BaseCache<String, IdentityMessageContext> {

    private static final String IDENTITY_MESSAGE_CONTEXT_CACHE = "IdentityMessageContextCache";
    private static volatile IdentityMessageContextCache instance;
    private boolean enableRequestScopeCache = false;

    private IdentityMessageContextCache(String cacheName) {
        super(cacheName);
        /*if (IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary") != null) {
            enableRequestScopeCache = Boolean.parseBoolean(IdentityUtil.getProperty(
                    "JDBCPersistenceManager.SessionDataPersist.Temporary"));
        }*/
    }

    public static IdentityMessageContextCache getInstance() {
        if (instance == null) {
            synchronized (IdentityMessageContextCache.class) {
                if (instance == null) {
                    instance = new IdentityMessageContextCache(IDENTITY_MESSAGE_CONTEXT_CACHE);
                }
            }
        }
        return instance;
    }

    public void addToCache(String key, IdentityMessageContext context) {
        super.put(key, context);
        /*if (enableRequestScopeCache) {
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            String tenantDomain = context.getIdentityRequest().getTenantDomain();
            if (tenantDomain != null) {
                tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            }
            SessionDataStore.getInstance().storeSessionData(key, IDENTITY_MESSAGE_CONTEXT_CACHE, context, tenantId);
        }*/
    }

    public IdentityMessageContext getValueFromCache(String key) {
        IdentityMessageContext context = super.get(key);
        /*if (context == null && enableRequestScopeCache) {
            context = (IdentityMessageContext) SessionDataStore.getInstance().getSessionData(key,
                                                                                             IDENTITY_MESSAGE_CONTEXT_CACHE);
        }*/
        return context;
    }

    public void clearCacheEntry(String key) {
        super.clear(key);
        /*if (enableRequestScopeCache) {
            SessionDataStore.getInstance().clearSessionData(key, IDENTITY_MESSAGE_CONTEXT_CACHE);
        }*/
    }
}
