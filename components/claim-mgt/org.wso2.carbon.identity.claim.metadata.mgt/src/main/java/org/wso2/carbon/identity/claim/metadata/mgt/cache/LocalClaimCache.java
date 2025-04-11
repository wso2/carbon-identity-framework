/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.claim.metadata.mgt.cache;

import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.cache.BaseCache;

import java.util.ArrayList;

/**
 * Cache implementation for LocalClaims.
 */
public class LocalClaimCache extends BaseCache<Integer, ArrayList<LocalClaim>> {

    private static final LocalClaimCache instance = new LocalClaimCache();
    private static final String CACHE_NAME = "LocalClaimCache";

    private LocalClaimCache() {
        super(CACHE_NAME);
    }

    public static LocalClaimCache getInstance() {
        return instance;
    }

    /**
     * Add a cache entry. Avoid adding entry if the current entry value is the same.
     *
     * @param key      Key which cache entry is indexed.
     * @param entry    Actual object where cache entry is placed.
     * @param tenantId The tenant Id where the cache is maintained.
     */
    @Override
    public void addToCache(Integer key, ArrayList<LocalClaim> entry, int tenantId) {

        if (key == null ) {
            return;
        }
        ArrayList<LocalClaim> currentEntry = getValueFromCache(key, tenantId);
        if (currentEntry != null && currentEntry.equals(entry)) {
            return;
        }
        super.addToCache(key, entry, tenantId);
    }

    /**
     * Add a cache entry. Avoid adding entry if the current entry value is the same.
     *
     * @param key      Key which cache entry is indexed.
     * @param entry    Actual object where cache entry is placed.
     * @param tenantDomain The tenant domain where the cache is maintained.
     */
    @Override
    public void addToCache(Integer key, ArrayList<LocalClaim> entry, String tenantDomain) {

        if (key == null ) {
            return;
        }
        ArrayList<LocalClaim> currentEntry = getValueFromCache(key, tenantDomain);
        if (currentEntry != null && currentEntry.equals(entry)) {
            return;
        }
        super.addToCache(key, entry, tenantDomain);
    }
}
