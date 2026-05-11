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

package org.wso2.carbon.identity.flow.mgt.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Represents a cache key for resolving flows in the flow management system.
 * This class encapsulates the organization ID for which the flow resolution is being performed.
 */
public class FlowResolveCacheKey extends CacheKey {

    private static final long serialVersionUID = 1L;
    private final String tenantId;
    private final String flowType;

    public FlowResolveCacheKey(String tenantId, String flowType) {

        this.tenantId = tenantId;
        this.flowType = flowType;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowResolveCacheKey that = (FlowResolveCacheKey) o;
        return java.util.Objects.equals(tenantId, that.tenantId)
                && java.util.Objects.equals(flowType, that.flowType);
    }

    @Override
    public int hashCode() {

        return 31 * java.util.Objects.hashCode(tenantId) + java.util.Objects.hashCode(flowType);
    }
}
