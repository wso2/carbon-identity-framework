/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.identity.gateway.context.cache;

import org.wso2.carbon.identity.common.base.cache.BaseCache;
import org.wso2.carbon.identity.gateway.context.SessionContext;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCSessionDAO;

/**
 * SessionContextCache caches the SessionContext and persist in database as well.
 */
public class SessionContextCache extends BaseCache<String, SessionContext> {

    private static final String SESSION_CONTEXT_CACHE = "SessionContextCache";
    private static volatile SessionContextCache instance;

    private SessionContextCache(String cacheName) {
        super(cacheName);
    }

    /**
     * @return
     */
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

    /**
     * @param key
     */
    public void clear(String key) {
        super.clear(key);
        JDBCSessionDAO.getInstance().remove(key);
    }

    /**
     * @param key
     * @return
     */
    public SessionContext get(String key) {
        SessionContext context = super.get(key);
        if (context == null) {
            context = JDBCSessionDAO.getInstance().get(key);
        }
        return context;
    }

    /**
     * @param key
     * @param context
     */
    public void put(String key, SessionContext context) {
        super.put(key, context);
        JDBCSessionDAO.getInstance().put(key, context);
    }
}