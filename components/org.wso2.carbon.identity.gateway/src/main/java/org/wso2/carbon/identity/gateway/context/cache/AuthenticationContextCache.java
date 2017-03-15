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
import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCGatewayContextDAO;

/**
 * AuthenticationContextCache caches the AuthenticationContext objects for given request.
 */
public class AuthenticationContextCache extends BaseCache<String, GatewayMessageContext> {

    private static final String IDENTITY_MESSAGE_CONTEXT_CACHE = "AuthenticationContextCache";
    private static volatile AuthenticationContextCache instance;

    private AuthenticationContextCache(String cacheName) {
        super(cacheName);
    }

    /**
     * @return
     */
    public static AuthenticationContextCache getInstance() {
        if (instance == null) {
            synchronized (AuthenticationContextCache.class) {
                if (instance == null) {
                    instance = new AuthenticationContextCache(IDENTITY_MESSAGE_CONTEXT_CACHE);
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
        JDBCGatewayContextDAO.getInstance().remove(key);
    }

    /**
     * @param key
     * @return
     */
    public GatewayMessageContext get(String key) {
        GatewayMessageContext context = super.get(key);
        if (context == null) {
            context = JDBCGatewayContextDAO.getInstance().get(key);
        }
        return context;
    }


    /**
     * @param key
     * @param context
     */
    public void put(String key, GatewayMessageContext context) {
        super.put(key, context);
        JDBCGatewayContextDAO.getInstance().put(key, context);
    }
}
