/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.cache;

import org.wso2.carbon.identity.framework.cache.MessageContextCache;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;

public class GatewayContextCache extends MessageContextCache<GatewayContextCacheKey, GatewayMessageContext> {

    private static final String IDENTITY_MESSAGE_CONTEXT_CACHE = "GatewayContextCache";
    private static GatewayContextCache instance =
            new GatewayContextCache(IDENTITY_MESSAGE_CONTEXT_CACHE);

    private GatewayContextCache(String cacheName) {
        super(cacheName);
    }

    public static GatewayContextCache getInstance() {

        return instance;
    }
}
