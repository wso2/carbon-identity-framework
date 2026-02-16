/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache to maintain the role ID - role basic info mapping.
 */
public class RoleBasicInfoCache extends BaseCache<RoleBasicInfoCacheKey, RoleBasicInfoCacheEntry> {

    private static final String CACHE_NAME = "RoleBasicInfoCache";

    private static final RoleBasicInfoCache instance = new RoleBasicInfoCache();

    private RoleBasicInfoCache() {

        super(CACHE_NAME);
    }

    public static RoleBasicInfoCache getInstance() {

        return instance;
    }
}
