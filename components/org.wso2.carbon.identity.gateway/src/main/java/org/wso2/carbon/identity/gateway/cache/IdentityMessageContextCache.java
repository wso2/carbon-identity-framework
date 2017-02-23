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
 */

package org.wso2.carbon.identity.gateway.cache;

import org.wso2.carbon.identity.common.base.cache.BaseCache;
import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.dao.jdbc.JDBCIdentityContextDAO;

import java.util.HashMap;
import java.util.Map;

public class IdentityMessageContextCache extends BaseCache<String, GatewayMessageContext> {

    private static final String IDENTITY_MESSAGE_CONTEXT_CACHE = "IdentityMessageContextCache";
    private static volatile IdentityMessageContextCache instance;
    private static Map<String,GatewayMessageContext> authenticationContextMap = new HashMap();

    private IdentityMessageContextCache(String cacheName) {
        super(cacheName);

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

    public void put(String key, GatewayMessageContext context) {
        super.put(key, context);
        JDBCIdentityContextDAO.getInstance().put(key, context);
    }

    public GatewayMessageContext get(String key) {
        GatewayMessageContext context = super.get(key);
        if(context == null) {
            context = JDBCIdentityContextDAO.getInstance().get(key);
        }
        return context;
    }

    public void clear(String key) {
        super.clear(key);
        JDBCIdentityContextDAO.getInstance().remove(key);
    }
}
