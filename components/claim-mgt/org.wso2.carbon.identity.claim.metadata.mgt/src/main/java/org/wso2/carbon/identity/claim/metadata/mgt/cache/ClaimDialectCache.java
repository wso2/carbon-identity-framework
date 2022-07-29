/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.core.cache.BaseCache;

import java.io.Serializable;
import java.util.List;

/**
 * Caches the claim dialect.
 * Cache can be configured with "identity.xml".
 */
public class ClaimDialectCache extends BaseCache<Integer, Serializable> {

    private static final String CACHE_NAME = "ClaimDialectCache";
    private static final ClaimDialectCache instance = new ClaimDialectCache();

    /*
     * Prevents instantiation.
     */
    private ClaimDialectCache() {
        super(CACHE_NAME);
    }

    public static ClaimDialectCache getInstance() {

        return instance;
    }

    public List<ClaimDialect> getClaimDialects(int tenantId) {

        return (List<ClaimDialect>) super.getValueFromCache(tenantId, tenantId);
    }

    public void putClaimDialects(int tenantId, List<ClaimDialect> claimDialectList) {

        super.addToCache(tenantId, (Serializable) claimDialectList, tenantId);
    }

    public void clearClaimDialects(int tenantId) {

        super.clearCacheEntry(tenantId, tenantId);
    }
}
