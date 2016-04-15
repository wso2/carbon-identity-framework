/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
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

package org.wso2.carbon.identity.claim.mgt.cache;

import org.wso2.carbon.identity.application.common.cache.BaseCache;
import org.wso2.carbon.identity.application.common.cache.CacheEntry;
import org.wso2.carbon.utils.CarbonUtils;


public class MappedAttributeCache extends BaseCache<MappedAttributeCacheKey, MappedAttributeCacheEntry> {

    private static final String Claim_CACHE_NAME = "MappedAttributeCacheCache";

    private static final MappedAttributeCache instance = new MappedAttributeCache(Claim_CACHE_NAME);

    private MappedAttributeCache(String cacheName) {
        super(cacheName);
    }

    public static MappedAttributeCache getInstance() {
        CarbonUtils.checkSecurity();
        return instance;
    }

    @Override
    public void addToCache(MappedAttributeCacheKey key, MappedAttributeCacheEntry entry) {
        super.addToCache(key, entry);
    }

    @Override
    public MappedAttributeCacheEntry getValueFromCache(MappedAttributeCacheKey key) {
        return super.getValueFromCache(key);
    }

    @Override
    public void clearCacheEntry(MappedAttributeCacheKey key) {
        super.clearCacheEntry(key);
    }
}
