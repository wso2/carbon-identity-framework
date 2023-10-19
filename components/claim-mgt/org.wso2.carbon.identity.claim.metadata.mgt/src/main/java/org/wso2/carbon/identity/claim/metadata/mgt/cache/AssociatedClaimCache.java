/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt.cache;

import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.core.cache.BaseCache;

import java.util.ArrayList;

/**
 * Cache implementation for AssociatedClaims for local claim.
 */
public class AssociatedClaimCache extends BaseCache<String, ArrayList<Claim>> {

    private static final AssociatedClaimCache instance = new AssociatedClaimCache();
    private static final String CACHE_NAME = "AssociatedClaimCache";

    private AssociatedClaimCache() {

        super(CACHE_NAME);
    }

    public static AssociatedClaimCache getInstance() {

        return instance;
    }
}
