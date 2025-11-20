/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Role Id cache based on the role name.
 */
public class RoleIdCacheByName extends BaseCache<RoleNameCacheKey, RoleIdCacheEntry> {

    private static final String CACHE_NAME = "RoleIdCacheByName";
    private static final Log log = LogFactory.getLog(RoleIdCacheByName.class);

    private static final RoleIdCacheByName instance = new RoleIdCacheByName();

    private RoleIdCacheByName() {

        super(CACHE_NAME);
        if (log.isDebugEnabled()) {
            log.debug("Initialized RoleIdCacheByName with cache name: " + CACHE_NAME);
        }
    }

    /**
     * Retrieve the singleton instance.
     *
     * @return Singleton instance of RoleIdCacheByName.
     */
    public static RoleIdCacheByName getInstance() {

        return instance;
    }
}
