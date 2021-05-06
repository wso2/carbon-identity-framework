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
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.cache.BaseCache;
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

        if (log.isDebugEnabled()) {
            log.debug("Adding session context corresponding to the key : " + key.getContextId() +
                " with accessed time " + entry.getAccessedTime() + " and validity time " + entry.getValidityPeriod());
        }
        entry.setAccessedTime();
        super.addToCache(key, entry);
//        Object authUser = entry.getContext().getProperty(FrameworkConstants.AUTHENTICATED_USER);
//        if (authUser != null && authUser instanceof AuthenticatedUser) {
//            String tenantDomain = ((AuthenticatedUser) authUser).getTenantDomain();
//            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
//            SessionDataStore.getInstance()
//                    .storeSessionData(key.getContextId(), SESSION_CONTEXT_CACHE_NAME, entry, tenantId);
//        } else {
//            SessionDataStore.getInstance().storeSessionData(key.getContextId(), SESSION_CONTEXT_CACHE_NAME, entry);
//        }
    }

    public SessionContextCacheEntry getValueFromCache(SessionContextCacheKey key) {
        SessionContextCacheEntry cacheEntry = super.getValueFromCache(key);

        // Retrieve session from the database if its not in cache
        if (cacheEntry == null) {
            if (log.isDebugEnabled()) {
                log.debug("Session corresponding to the key : " + key.getContextId() + " cannot be found in the" +
                        " cache. Trying to get from db.");
            }
            cacheEntry = getSessionFromDB(key);
        }

        if (cacheEntry == null) {
            if (log.isDebugEnabled()) {
                log.debug("Session corresponding to the key : " + key.getContextId() + " cannot be found.");
            }
            return null;
        } else if (isValidIdleSession(key, cacheEntry) || isValidRememberMeSession(key, cacheEntry)) {
            if (log.isDebugEnabled()) {
                log.debug("Found a valid session corresponding to the key : " + key.getContextId());
            }
            return cacheEntry;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Found an expired session corresponding to the key : " + key.getContextId());
            }
            clearCacheEntry(key);
            return null;
        }

    }

    /**
     * Retrieve session from the database.
     *
     * @param key Session context cache key.
     * @return Session context cache entry.
     */
    private SessionContextCacheEntry getSessionFromDB(SessionContextCacheKey key) {

//        SessionContextCacheEntry cacheEntry = null;
//        SessionContextDO sessionContextDO = SessionDataStore.getInstance().
//                getSessionContextData(key.getContextId(), SESSION_CONTEXT_CACHE_NAME);

//        if (sessionContextDO != null) {
//            cacheEntry = new SessionContextCacheEntry(sessionContextDO);
//        }
        return null;
    }

    /**
     * Get session context cache entry from the cache.
     *
     * @param key Session context cache key.
     * @return Session context cache entry.
     */
    public SessionContextCacheEntry getSessionContextCacheEntry(SessionContextCacheKey key) {

        SessionContextCacheEntry cacheEntry = super.getValueFromCache(key);
        // Retrieve session from the database if it's not in the cache.
        if (cacheEntry == null) {
            if (log.isDebugEnabled()) {
                log.debug("Session corresponding to the key : " + key.getContextId() + " cannot be found in the" +
                        " cache. Trying to get from db.");
            }
            cacheEntry = getSessionFromDB(key);
        }

        if (cacheEntry == null) {
            if (log.isDebugEnabled()) {
                log.debug("Session corresponding to the key : " + key.getContextId() + " cannot be found.");
            }
            return null;
        } else {
            return cacheEntry;
        }
    }

    /**
     * Check whether the session is expired.
     *
     * @param cachekey   Session context cache key.
     * @param cacheEntry Session context cache entry.
     * @return True if the session is expired else return false.
     */
    public boolean isSessionExpired(SessionContextCacheKey cachekey, SessionContextCacheEntry cacheEntry) {

        if (isValidIdleSession(cachekey, cacheEntry) || isValidRememberMeSession(cachekey, cacheEntry)) {
            if (log.isDebugEnabled()) {
                log.debug("A valid session is available corresponding to the key : " + cachekey.getContextId());
            }
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("Found an expired session corresponding to the key : " + cachekey.getContextId());
        }
        clearCacheEntry(cachekey);
        return true;
    }

    public void clearCacheEntry(SessionContextCacheKey key) {

        if (log.isDebugEnabled()) {
            log.debug("Clear session context corresponding to the key : " + key.getContextId());
        }
        super.clearCacheEntry(key);
//        SessionDataStore.getInstance().clearSessionData(key.getContextId(), SESSION_CONTEXT_CACHE_NAME);
    }

    public void clearCacheEntry(String sessionContextKey) {

        if (log.isDebugEnabled()) {
            log.debug("Clear session context corresponding to the key : " + sessionContextKey);
        }
        SessionContextCacheKey sessionContextCacheKey = new SessionContextCacheKey(sessionContextKey);
        super.clearCacheEntry(sessionContextCacheKey);
//        SessionDataStore.getInstance().clearSessionData(sessionContextCacheKey.getContextId(),
//                SESSION_CONTEXT_CACHE_NAME);

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
        Long createdTime = cacheEntry.getAccessedTime();

        if (cacheEntry.getContext() != null) {
            Object createdTimestampObj = cacheEntry.getContext().getProperty(FrameworkConstants.CREATED_TIMESTAMP);
            if (createdTimestampObj != null) {
                createdTime = (Long) createdTimestampObj;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Context ID : " + contextId + " :: rememberMeSessionTimeOut : " + rememberMeSessionTimeOut
                    + ", currentTime : " + currentTime + ", created time : " + createdTime);
        }

        if (currentTime - createdTime > rememberMeSessionTimeOut) {
            if (log.isDebugEnabled()) {
                log.debug("Context ID : " + contextId + " :: Remember me session expiry");
            }
            return false;
        }

        return true;
    }
}
