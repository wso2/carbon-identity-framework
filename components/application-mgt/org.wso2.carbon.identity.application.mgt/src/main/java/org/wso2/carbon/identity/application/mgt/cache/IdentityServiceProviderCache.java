/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.identity.application.mgt.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache implementation for Application (the Service Provider) cache.
 */
public class IdentityServiceProviderCache extends
        BaseCache<IdentityServiceProviderCacheKey, IdentityServiceProviderCacheEntry> {

    public static final String SP_CACHE_NAME = "ServiceProviderCache";

    private static volatile IdentityServiceProviderCache instance;

    private IdentityServiceProviderCache() {
        super(SP_CACHE_NAME);
    }

    public static IdentityServiceProviderCache getInstance() {
        if (instance == null) {
            synchronized (IdentityServiceProviderCache.class) {
                if (instance == null) {
                    instance = new IdentityServiceProviderCache();
                }
            }
        }
        return instance;
    }
}
