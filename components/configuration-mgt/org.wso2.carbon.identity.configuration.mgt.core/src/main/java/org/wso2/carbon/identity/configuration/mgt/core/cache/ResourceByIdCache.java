/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.cache;

import org.wso2.carbon.identity.application.common.cache.BaseCache;

/**
 * Represents the cache that holds the configuration resource by id
 * {@link org.wso2.carbon.identity.configuration.mgt.core.model.Resource}.
 */
public class ResourceByIdCache extends BaseCache<ResourceByIdCacheKey, ResourceCacheEntry> {

    private static final String RESOURCE_CACHE_NAME = "ConfigurationResourceByIdCache";
    private static volatile ResourceByIdCache instance;

    private ResourceByIdCache() {
        super(RESOURCE_CACHE_NAME);
    }

    public ResourceByIdCache(String cacheName) {

        super(cacheName);
    }

    public ResourceByIdCache(String cacheName, boolean isTemp) {

        super(cacheName, isTemp);
    }

    public static ResourceByIdCache getInstance() {

        if (instance == null) {
            synchronized (ResourceByIdCache.class) {
                if (instance == null) {
                    instance = new ResourceByIdCache();
                }
            }
        }
        return instance;
    }
}
