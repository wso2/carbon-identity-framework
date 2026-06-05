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

package org.wso2.carbon.identity.application.mgt.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Cache key for the shared-application to main-application resolution (keyed by the shared application id).
 */
public class MainApplicationCacheKey extends CacheKey {

    private static final long serialVersionUID = 1796612905023411660L;

    private final String sharedAppId;

    public MainApplicationCacheKey(String sharedAppId) {

        this.sharedAppId = sharedAppId;
    }

    public String getSharedAppId() {

        return sharedAppId;
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
        MainApplicationCacheKey that = (MainApplicationCacheKey) o;
        return sharedAppId.equals(that.sharedAppId);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + sharedAppId.hashCode();
        return result;
    }
}
