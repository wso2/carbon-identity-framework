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

package org.wso2.carbon.identity.framework.cache;

import org.wso2.carbon.identity.common.base.cache.AbstractCacheEntryListener;
import org.wso2.carbon.identity.common.base.cache.BaseCache;
import org.wso2.carbon.identity.framework.context.IdentityMessageContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache to hold {@link IdentityMessageContext} instances for correlation purposes.
 */
public class IdentityMessageContextCache extends BaseCache<String, IdentityMessageContext> {

    private Map<String, IdentityMessageContext> cache = new HashMap<>();
    private static final String IDENTITY_MESSAGE_CONTEXT_CACHE = "IdentityMessageContextCache";
    private static IdentityMessageContextCache instance = new IdentityMessageContextCache
            (IDENTITY_MESSAGE_CONTEXT_CACHE);

    private IdentityMessageContextCache(String cacheName) {
        super(cacheName);
    }

    public static IdentityMessageContextCache getInstance() {
        return instance;
    }

    @Override
    public void put(String key, IdentityMessageContext entry) {
        cache.put(key, entry);
    }

    @Override
    public IdentityMessageContext get(String key) {
        return cache.get(key);
    }

    @Override
    public void clear(String key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void addListener(AbstractCacheEntryListener listener) {
        super.addListener(listener);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int getCacheTimeout() {
        return 100000000;
    }

    @Override
    public int getCapacity() {
        return cache.size();
    }
}
