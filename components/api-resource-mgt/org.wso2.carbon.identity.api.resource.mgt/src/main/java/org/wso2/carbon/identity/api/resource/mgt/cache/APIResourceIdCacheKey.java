/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.api.resource.mgt.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Cache key for API resource.
 */
public class APIResourceIdCacheKey extends CacheKey {

    private final String resourceId;

    public APIResourceIdCacheKey(String resourceId) {

        this.resourceId = resourceId;
    }

    public String getResourceId() {

        return resourceId;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof APIResourceIdCacheKey)) {
            return false;
        }
        return resourceId.equals(((APIResourceIdCacheKey) o).getResourceId());
    }

    @Override
    public int hashCode() {

        return resourceId.hashCode();
    }
}
