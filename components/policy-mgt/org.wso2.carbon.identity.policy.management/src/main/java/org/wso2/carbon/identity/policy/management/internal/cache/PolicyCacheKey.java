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
 * Supports lookup by policy ID or by policy name using prefixed string keys,
 * so both types can share a single cache instance without collision.
 */
public class PolicyCacheKey extends CacheKey {

    private static final long serialVersionUID = 1861274580132498765L;

    private final String key;

    private PolicyCacheKey(String key) {

        this.key = key;
    }

    /**
     * Creates a cache key for a policy ID lookup.
     *
     * @param policyId Policy ID.
     * @return Cache key scoped to the ID namespace.
     */
    public static PolicyCacheKey forId(String policyId) {

        return new PolicyCacheKey("id:" + policyId);
    }

    /**
     * Creates a cache key for a policy name lookup.
     *
     * @param policyName Policy name.
     * @return Cache key scoped to the name namespace.
     */
    public static PolicyCacheKey forName(String policyName) {

        return new PolicyCacheKey("name:" + policyName);
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof PolicyCacheKey)) {
            return false;
        }
        return key.equals(((PolicyCacheKey) o).key);
    }

    @Override
    public int hashCode() {

        return key.hashCode();
    }
}
