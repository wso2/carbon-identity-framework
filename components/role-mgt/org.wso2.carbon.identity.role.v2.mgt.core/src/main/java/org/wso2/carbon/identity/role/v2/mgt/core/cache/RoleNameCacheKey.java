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

package org.wso2.carbon.identity.role.v2.mgt.core.cache;

import org.wso2.carbon.identity.core.cache.CacheKey;

import java.util.Objects;

/**
 * Cache Key by role name for RoleIdCacheByName cache.
 */
public class RoleNameCacheKey extends CacheKey {

    private static final long serialVersionUID = 9172836450192837465L;

    private final String roleName;
    private final String audience;
    private final String audienceId;

    /**
     * Constructor to create RoleNameCacheKey.
     *
     * @param roleName   Name of the role.
     * @param audience   Audience of the role.
     * @param audienceId Audience ID of the role.
     */
    public RoleNameCacheKey(String roleName, String audience, String audienceId) {

        this.roleName = roleName;
        this.audience = audience;
        this.audienceId = audienceId;
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

        RoleNameCacheKey that = (RoleNameCacheKey) o;
        return Objects.equals(roleName, that.roleName) && Objects.equals(audience, that.audience) &&
                Objects.equals(audienceId, that.audienceId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(roleName, audience, audienceId);
    }
}
