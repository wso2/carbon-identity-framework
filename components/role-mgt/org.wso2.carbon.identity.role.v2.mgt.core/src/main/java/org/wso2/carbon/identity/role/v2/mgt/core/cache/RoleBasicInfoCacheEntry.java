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

import org.wso2.carbon.identity.core.cache.CacheEntry;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;

/**
 * Cache entry for role basic information.
 */
public class RoleBasicInfoCacheEntry extends CacheEntry {

    private static final long serialVersionUID = -8407506285763321120L;
    private RoleBasicInfo roleBasicInfo;

    public RoleBasicInfoCacheEntry(RoleBasicInfo roleBasicInfo) {

        this.roleBasicInfo = roleBasicInfo;
    }

    public RoleBasicInfo getRoleBasicInfo() {

        return roleBasicInfo;
    }

    public void setRoleBasicInfo(RoleBasicInfo roleBasicInfo) {

        this.roleBasicInfo = roleBasicInfo;
    }
}
