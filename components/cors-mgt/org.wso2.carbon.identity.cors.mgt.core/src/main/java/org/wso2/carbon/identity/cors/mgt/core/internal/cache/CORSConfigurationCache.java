/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.cors.mgt.core.internal.cache;

import org.wso2.carbon.identity.application.common.cache.BaseCache;

/**
 * Cache for CORS configurations.
 */
public class CORSConfigurationCache extends BaseCache<CORSConfigurationCacheKey,
        CORSConfigurationCacheEntry> {

    private static final String CORS_CACHE_NAME = "CORSConfigurationCache";

    private static volatile CORSConfigurationCache instance;

    private CORSConfigurationCache() {

        super(CORS_CACHE_NAME);
    }

    public static CORSConfigurationCache getInstance() {

        if (instance == null) {
            synchronized (CORSOriginCache.class) {
                if (instance == null) {
                    instance = new CORSConfigurationCache();
                }
            }
        }
        return instance;
    }
}
