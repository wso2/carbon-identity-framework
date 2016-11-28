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

import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.utils.CarbonUtils;

/**
 *
 * Cache implementation for LocalClaim
 *
 */
public class LocalClaimCache extends BaseCache<String, LocalClaimCacheEntry> {

    private static final String CACHE_NAME = "LocalClaimCache";

    private static final LocalClaimCache instance = new LocalClaimCache();

    private LocalClaimCache() {
        super(CACHE_NAME);
    }

    public static LocalClaimCache getInstance() {
        CarbonUtils.checkSecurity();
        return instance;
    }

    public LocalClaimCacheEntry getValueFromCache() {
        return super.getValueFromCache(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
    }

    public void addToCache(LocalClaimCacheEntry localClaimCacheEntry) {
        super.addToCache(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, localClaimCacheEntry);
    }

    public void clearCacheEntry() {
        super.clearCacheEntry(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
    }
}
