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

package org.wso2.carbon.identity.application.common.cache;

import org.wso2.carbon.identity.core.cache.BaseCache;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Cache for the system defined authenticator configurations.
 */
public class SystemDefinedAuthenticatorCache extends
        BaseCache<SystemDefinedAuthenticatorCacheKey, SystemDefinedAuthenticatorCacheEntry> {

    private static final String SYSTEM_DEFINED_AUTHENTICATOR_CACHE_NAME = "SystemDefinedAuthenticatorCache";
    private static final SystemDefinedAuthenticatorCache INSTANCE = new SystemDefinedAuthenticatorCache();


    private SystemDefinedAuthenticatorCache() {
        super(SYSTEM_DEFINED_AUTHENTICATOR_CACHE_NAME);
    }

    public static SystemDefinedAuthenticatorCache getInstance() {

        CarbonUtils.checkSecurity();
        return INSTANCE;
    }
}
