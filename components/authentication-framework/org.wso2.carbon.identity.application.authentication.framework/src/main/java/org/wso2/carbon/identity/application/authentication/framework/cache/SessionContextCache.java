package org.wso2.carbon.identity.application.authentication.framework.cache;

import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.common.cache.BaseCache;


public class SessionContextCache extends BaseCache<String, SessionContext> {

    private static final String SESSION_CONTEXT_CACHE = "SessionContextCache";
    private static volatile SessionContextCache instance;

    private SessionContextCache(String cacheName) {
        super(cacheName);
    }

    public static SessionContextCache getInstance() {
        if (instance == null) {
            synchronized (SessionContextCache.class) {
                if (instance == null) {
                    instance = new SessionContextCache(SESSION_CONTEXT_CACHE);
                }
            }
        }
        return instance;
    }

    public void addToCache(String key, SessionContext context) {
        super.addToCache(key, context);
    }

    public SessionContext getValueFromCache(String key) {
        SessionContext context = super.getValueFromCache(key);

        return context;
    }

    public void clearCacheEntry(String key) {
        super.clearCacheEntry(key);
    }
}