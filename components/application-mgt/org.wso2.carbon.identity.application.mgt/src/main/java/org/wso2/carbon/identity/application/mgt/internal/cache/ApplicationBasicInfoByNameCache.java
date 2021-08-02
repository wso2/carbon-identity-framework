/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.mgt.internal.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache to maintain the application name - ApplicationBasicInfo.
 */
public class ApplicationBasicInfoByNameCache extends
        BaseCache<ApplicationBasicInfoNameCacheKey, ApplicationBasicInfoCacheEntry> {

    private static final String SP_CACHE_NAME = "ApplicationBasicInfoCache.Name";
    private static volatile ApplicationBasicInfoByNameCache instance;

    private ApplicationBasicInfoByNameCache() {

        super(SP_CACHE_NAME);
    }

    public static ApplicationBasicInfoByNameCache getInstance() {

        if (instance == null) {
            synchronized (ApplicationBasicInfoByNameCache.class) {
                if (instance == null) {
                    instance = new ApplicationBasicInfoByNameCache();
                }
            }
        }
        return instance;
    }
}
