/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionContextDO;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.application.common.cache.BaseCache;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.util.concurrent.TimeUnit;

public class SessionContextCache extends BaseCache<SessionContextCacheKey, SessionContextCacheEntry> {

    private static final String SESSION_CONTEXT_CACHE_NAME = "AppAuthFrameworkSessionContextCache";
    private static final Log log = LogFactory.getLog(SessionContextCache.class);

    private static volatile SessionContextCache instance;

    private SessionContextCache() {
        super(SESSION_CONTEXT_CACHE_NAME);
    }

    public static SessionContextCache getInstance() {
        if (instance == null) {
            synchronized (SessionContextCache.class) {
                if (instance == null) {
                    instance = new SessionContextCache();
                }
            }
        }
        return instance;
    }

    public void addToCache(SessionContextCacheKey key, SessionContextCacheEntry entry) {
        entry.setAccessedTime();
        super.addToCache(key, entry);
        SessionDataStore.getInstance().storeSessionData(key.getContextId(), SESSION_CONTEXT_CACHE_NAME, entry);
    }

    public SessionContextCacheEntry getValueFromCache(SessionContextCacheKey key) {
        SessionContextCacheEntry cacheEntry = super.getValueFromCache(key);

        // Retrieve session from the database if its not in cache
        if (cacheEntry == null) {
            SessionContextDO sessionContextDO = SessionDataStore.getInstance().
                    getSessionContextData(key.getContextId(), SESSION_CONTEXT_CACHE_NAME);

            if (sessionContextDO != null) {
                cacheEntry = new SessionContextCacheEntry(sessionContextDO);
            }
        }

        if (cacheEntry == null) {
            if(log.isDebugEnabled()) {
                log.debug("Session corresponding to the key : " + key.getContextId() + " cannot be found.");
            }
            return null;
        } else if (isValidIdleSession(key, cacheEntry) || isValidRememberMeSession(key, cacheEntry)) {
            if(log.isDebugEnabled()) {
                log.debug("Found a valid session corresponding to the key : " + key.getContextId());
            }
            return cacheEntry;
        } else {
            if(log.isDebugEnabled()) {
                log.debug("Found an expired session corresponding to the key : " + key.getContextId());
            }
            clearCacheEntry(key);
            return null;
        }

    }

    public void clearCacheEntry(SessionContextCacheKey key) {
        super.clearCacheEntry(key);
        SessionDataStore.getInstance().clearSessionData(key.getContextId(), SESSION_CONTEXT_CACHE_NAME);
    }

    /**
     * Check whether the given session context is valid according to idle session timeout restrictions.
     *
     * @param key        SessionContextCacheKey
     * @param cacheEntry SessionContextCacheEntry
     * @return true if the session context is valid as per idle session configs; false otherwise
     */
    private boolean isValidIdleSession(SessionContextCacheKey key, SessionContextCacheEntry cacheEntry) {
        String contextId = key.getContextId();

        if (cacheEntry == null) {
            return false;
        }

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        long idleSessionTimeOut = TimeUnit.SECONDS.toMillis(IdPManagementUtil.getIdleSessionTimeOut(tenantDomain));

        long currentTime = System.currentTimeMillis();
        long lastAccessedTime = cacheEntry.getAccessedTime();

        if (log.isDebugEnabled()) {
            log.debug("Context ID : " + contextId + " :: idleSessionTimeOut : " + idleSessionTimeOut
                    + ", currentTime : " + currentTime + ", lastAccessedTime : " + lastAccessedTime);
        }

        if (currentTime - lastAccessedTime > idleSessionTimeOut) {
            if (log.isDebugEnabled()) {
                log.debug("Context ID : " + contextId + " :: Idle session expiry");
            }
            return false;
        }

        return true;
    }

    /**
     * Check whether the given session context is valid according to remember me session timeout restrictions.
     *
     * @param key        SessionContextCacheKey
     * @param cacheEntry SessionContextCacheEntry
     * @return true if the session context is valid as per remember me session configs; false otherwise
     */
    private boolean isValidRememberMeSession(SessionContextCacheKey key, SessionContextCacheEntry cacheEntry) {
        String contextId = key.getContextId();

        if (cacheEntry == null) {
            return false;
        }

        if (!cacheEntry.getContext().isRememberMe()) {
            return false;
        }

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        long rememberMeSessionTimeOut = TimeUnit.SECONDS.toMillis(IdPManagementUtil.getRememberMeTimeout(tenantDomain));

        long currentTime = System.currentTimeMillis();
        long lastAccessedTime = cacheEntry.getAccessedTime();

        if (log.isDebugEnabled()) {
            log.debug("Context ID : " + contextId + " :: rememberMeSessionTimeOut : " + rememberMeSessionTimeOut
                    + ", currentTime : " + currentTime + ", lastAccessedTime : " + lastAccessedTime);
        }

        if (currentTime - lastAccessedTime > rememberMeSessionTimeOut) {
            if (log.isDebugEnabled()) {
                log.debug("Context ID : " + contextId + " :: Remember me session expiry");
            }
            return false;
        }

        return true;
    }
}
