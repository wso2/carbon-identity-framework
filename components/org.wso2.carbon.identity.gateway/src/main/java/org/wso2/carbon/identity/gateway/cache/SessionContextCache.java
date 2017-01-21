package org.wso2.carbon.identity.gateway.cache;

import org.wso2.carbon.identity.common.base.cache.BaseCache;
import org.wso2.carbon.identity.gateway.context.SessionContext;


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
        super.put(key, context);
    }

    public SessionContext getValueFromCache(String key) {
        SessionContext context = super.get(key);

        return context;
    }

    public void clearCacheEntry(String key) {
        super.clear(key);
    }
}