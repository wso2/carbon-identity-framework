/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.secret.mgt.core.cache;

import org.wso2.carbon.identity.application.common.cache.BaseCache;

/**
 * Represents the cache that holds the secret by id
 * {@link org.wso2.carbon.identity.secret.mgt.core.model.Secret}.
 */
public class SecretByIdCache extends BaseCache<SecretByIdCacheKey, SecretCacheEntry> {

    private static final String SECRET_CACHE_NAME = "SecretByIdCache";
    private static volatile SecretByIdCache instance;

    private SecretByIdCache() {

        super(SECRET_CACHE_NAME);
    }

    public SecretByIdCache(String cacheName) {

        super(cacheName);
    }

    public SecretByIdCache(String cacheName, boolean isTemp) {

        super(cacheName, isTemp);
    }

    public static SecretByIdCache getInstance() {

        if (instance == null) {
            synchronized (SecretByIdCache.class) {
                if (instance == null) {
                    instance = new SecretByIdCache();
                }
            }
        }
        return instance;
    }
}
