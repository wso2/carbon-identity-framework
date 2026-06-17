/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.policy.management.internal.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Cache key for Policy Management.
 * Identifies a cached policy by its name; tenant scoping is handled by the cache's tenantId parameter.
 */
public class PolicyCacheKey extends CacheKey {

    private static final long serialVersionUID = 1861274580132498765L;

    private final String policyName;

    public PolicyCacheKey(String policyName) {

        this.policyName = policyName;
    }

    public String getPolicyName() {

        return policyName;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof PolicyCacheKey)) {
            return false;
        }
        return policyName.equals(((PolicyCacheKey) o).getPolicyName());
    }

    @Override
    public int hashCode() {

        return policyName.hashCode();
    }
}
