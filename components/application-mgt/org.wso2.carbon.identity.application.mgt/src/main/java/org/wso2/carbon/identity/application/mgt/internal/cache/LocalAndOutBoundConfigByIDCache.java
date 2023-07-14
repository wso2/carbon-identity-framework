/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache for LocalAndOutBoundConfigByIDCache.
 */
public class LocalAndOutBoundConfigByIDCache extends
        BaseCache<LocalAndOutBoundConfigByIDCacheKey, LocalAndOutBoundConfigByIDCacheEntry> {

    public static final String SP_CACHE_NAME = "ServiceProviderCache.ID";

    private static volatile LocalAndOutBoundConfigByIDCache instance;

    private LocalAndOutBoundConfigByIDCache() {

        super(SP_CACHE_NAME);
    }

    public static LocalAndOutBoundConfigByIDCache getInstance() {

        if (instance == null) {
            synchronized (LocalAndOutBoundConfigByIDCache.class) {
                if (instance == null) {
                    instance = new LocalAndOutBoundConfigByIDCache();
                }
            }
        }
        return instance;
    }
}
