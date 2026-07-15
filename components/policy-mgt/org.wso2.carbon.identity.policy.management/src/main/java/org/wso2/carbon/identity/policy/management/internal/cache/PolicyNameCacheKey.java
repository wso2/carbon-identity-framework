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

import java.util.Objects;

/**
 * Cache key by policy name for PolicyIdByNameCache.
 */
public class PolicyNameCacheKey extends CacheKey {

    private static final long serialVersionUID = -8261947350129384756L;

    private final String policyName;

    /**
     * Constructor to create PolicyNameCacheKey.
     *
     * @param policyName Name of the policy.
     */
    public PolicyNameCacheKey(String policyName) {

        this.policyName = policyName;
    }

    /**
     * Get the policy name.
     *
     * @return Policy name.
     */
    public String getPolicyName() {

        return policyName;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        PolicyNameCacheKey that = (PolicyNameCacheKey) o;
        return Objects.equals(policyName, that.policyName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(policyName);
    }
}
