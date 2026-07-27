/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.common.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Cache for all user defined local authenticators.
 */
public class UserDefinedLocalAuthenticatorsCache extends BaseCache<UserDefinedLocalAuthenticatorsCacheKey,
        UserDefinedLocalAuthenticatorsCacheEntry> {

    private static final String CACHE_NAME = "UserDefinedLocalAuthenticatorsCache";
    private static final UserDefinedLocalAuthenticatorsCache INSTANCE = new UserDefinedLocalAuthenticatorsCache();

    private UserDefinedLocalAuthenticatorsCache() {

        super(CACHE_NAME);
    }

    /**
     * Get Authenticator cache by the name instance.
     *
     * @return Authenticator cache by name instance.
     */
    public static UserDefinedLocalAuthenticatorsCache getInstance() {

        CarbonUtils.checkSecurity();
        return INSTANCE;
    }
}
