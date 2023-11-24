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

package org.wso2.carbon.identity.application.mgt.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;

/**
 * Cache for authorized API.
 */
public class AuthorizedAPICache extends BaseCache<AuthorizedAPICacheKey, AuthorizedAPICacheEntry> {

    private static final String CACHE_NAME = "AuthorizedAPICache";
    private static final AuthorizedAPICache instance = new AuthorizedAPICache();

    private AuthorizedAPICache() {

        super(CACHE_NAME);
    }

    /**
     * Get instance of AuthorizedAPICache.
     *
     * @return Instance of AuthorizedAPICache.
     */
    public static AuthorizedAPICache getInstance() {

        return instance;
    }
}
