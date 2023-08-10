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

package org.wso2.carbon.identity.application.role.mgt.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

/**
 * Cache key to lookup application role from cache.
 */
public class ApplicationRoleCacheKey extends CacheKey {

    private static final long serialVersionUID = 8263255365985309443L;
    private String applicationRoleId;

    public ApplicationRoleCacheKey(String applicationRoleId) {

        this.applicationRoleId = applicationRoleId;
    }

    public String getApplicationRoleId() {

        return applicationRoleId;
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

        ApplicationRoleCacheKey that = (ApplicationRoleCacheKey) o;

        if (!applicationRoleId.equals(that.applicationRoleId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + applicationRoleId.hashCode();
        return result;
    }
}
