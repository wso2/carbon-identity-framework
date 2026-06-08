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

import org.wso2.carbon.identity.core.cache.CacheEntry;
import org.wso2.carbon.identity.policy.management.api.model.Policy;

/**
 * Cache entry for Device Policy Management.
 * Stores an un-hydrated Policy (rule IDs only, no full Rule objects).
 */
public class PolicyCacheEntry extends CacheEntry {

    private static final long serialVersionUID = -4310859152763490172L;

    private final Policy policy;

    public PolicyCacheEntry(Policy policy) {

        this.policy = policy;
    }

    public Policy getPolicy() {

        return policy;
    }
}
