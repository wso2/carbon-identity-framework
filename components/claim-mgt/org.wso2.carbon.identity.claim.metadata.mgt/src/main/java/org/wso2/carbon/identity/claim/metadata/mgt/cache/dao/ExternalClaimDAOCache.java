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

package org.wso2.carbon.identity.claim.metadata.mgt.cache.dao;

import org.wso2.carbon.identity.claim.metadata.mgt.cache.ExternalClaimCacheKey;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.core.cache.BaseCache;

import java.util.ArrayList;

/**
 * The DAO cache implementation for external claims.
 */
public class ExternalClaimDAOCache extends BaseCache<ExternalClaimCacheKey, ArrayList<ExternalClaim>> {

    private static final String CACHE_NAME = "ExternalClaimDAOCache";
    private static final ExternalClaimDAOCache INSTANCE = new ExternalClaimDAOCache();

    /**
     * The private constructor which ensures that only one instance of the cache is created.
     */
    private ExternalClaimDAOCache() {

        super(CACHE_NAME);
    }

    /**
     * Gets the instance of the associated claim DAO cache.
     *
     * @return the instance of the associated claim DAO cache.
     */
    public static ExternalClaimDAOCache getInstance() {

        return INSTANCE;
    }
}

