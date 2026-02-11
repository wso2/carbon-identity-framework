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

package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

import java.util.Objects;

/**
 * Cache key for role basic info, indexed by role ID.
 */
public class RoleBasicInfoCacheKey extends CacheKey {

    private static final long serialVersionUID = -8407506285763321119L;
    private String roleId;

    public RoleBasicInfoCacheKey(String roleId) {

        this.roleId = roleId;
    }

    public String getRoleId() {

        return roleId;
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

        RoleBasicInfoCacheKey that = (RoleBasicInfoCacheKey) o;

        return Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();
        result = 31 * result + (roleId != null ? roleId.hashCode() : 0);
        return result;
    }
}
