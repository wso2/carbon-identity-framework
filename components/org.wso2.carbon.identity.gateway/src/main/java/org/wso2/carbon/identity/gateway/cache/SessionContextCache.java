package org.wso2.carbon.identity.gateway.cache;

import org.wso2.carbon.identity.common.base.cache.BaseCache;
import org.wso2.carbon.identity.gateway.context.SessionContext;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCSessionDAO;


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

    public void put(String key, SessionContext context) {
        super.put(key, context);
        JDBCSessionDAO.getInstance().put(key, context);
    }

    public SessionContext get(String key) {
        SessionContext context = super.get(key);
        if(context == null) {
            context = JDBCSessionDAO.getInstance().get(key);
        }
        return context;
    }

    public void clear(String key) {
        super.clear(key);
        JDBCSessionDAO.getInstance().remove(key);
    }
}