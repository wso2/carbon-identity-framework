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

package org.wso2.carbon.identity.application.mgt.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache for authorized scopes of an application.
 */
public class AuthorizedScopesCache extends BaseCache<AuthorizedScopesCacheKey, AuthorizedScopesCacheEntry> {

    private static final String CACHE_NAME = "AuthorizedScopesCache";
    private static final AuthorizedScopesCache instance = new AuthorizedScopesCache();

    private AuthorizedScopesCache() {

        super(CACHE_NAME);
    }

    /**
     * Get instance of AuthorizedScopesCache.
     *
     * @return Instance of AuthorizedScopesCache.
     */
    public static AuthorizedScopesCache getInstance() {

        return instance;
    }
}
