/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.claim.metadata.mgt.cache;

import org.wso2.carbon.utils.CarbonUtils;

/**
 *
 * Cache implementation for ExternalClaim
 *
 */
public class ExternalClaimCache extends BaseCache<String, ExternalClaimCacheEntry> {

    private static final String CACHE_NAME = "ExternalClaimCache";

    private static final ExternalClaimCache instance = new ExternalClaimCache();

    private ExternalClaimCache() {
        super(CACHE_NAME);
    }

    public static ExternalClaimCache getInstance() {
        CarbonUtils.checkSecurity();
        return instance;
    }

    @Override
    public ExternalClaimCacheEntry getValueFromCache(String claimDialectURI) {
        return super.getValueFromCache(claimDialectURI);
    }
}
