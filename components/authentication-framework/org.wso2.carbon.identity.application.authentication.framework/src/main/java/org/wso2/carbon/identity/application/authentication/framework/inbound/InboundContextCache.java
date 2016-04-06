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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.common.cache.BaseCache;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

public class InboundContextCache extends BaseCache<String, InboundMessageContext> {

    private static final String INBOUND_CONTEXT_CACHE_NAME = "InboundContextCache";
    private static volatile InboundContextCache instance;
    private boolean enableRequestScopeCache = false;

    private InboundContextCache(String cacheName) {
        super(cacheName);
        if (IdentityUtil.getProperty("JDBCPersistenceManager.SessionDataPersist.Temporary") != null) {
            enableRequestScopeCache = Boolean.parseBoolean(IdentityUtil.getProperty(
                    "JDBCPersistenceManager.SessionDataPersist.Temporary"));
        }
    }

    public static InboundContextCache getInstance() {
        if (instance == null) {
            synchronized (InboundContextCache.class) {
                if (instance == null) {
                    instance = new InboundContextCache(INBOUND_CONTEXT_CACHE_NAME);
                }
            }
        }
        return instance;
    }

    public void addToCache(String key, InboundMessageContext context) {
        super.addToCache(key, context);
        if (enableRequestScopeCache) {
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            String tenantDomain = context.getRequest().getTenantDomain();
            if (tenantDomain != null) {
                tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            }
            SessionDataStore.getInstance().storeSessionData(key, INBOUND_CONTEXT_CACHE_NAME, context, tenantId);
        }
    }

    public InboundMessageContext getValueFromCache(String key) {
        InboundMessageContext context = super.getValueFromCache(key);
        if (context == null && enableRequestScopeCache) {
            context = (InboundMessageContext) SessionDataStore.getInstance().getSessionData(key,
                    INBOUND_CONTEXT_CACHE_NAME);
        }
        return context;
    }

    public void clearCacheEntry(String key) {
        super.clearCacheEntry(key);
        if (enableRequestScopeCache) {
            SessionDataStore.getInstance().clearSessionData(key, INBOUND_CONTEXT_CACHE_NAME);
        }
    }
}
