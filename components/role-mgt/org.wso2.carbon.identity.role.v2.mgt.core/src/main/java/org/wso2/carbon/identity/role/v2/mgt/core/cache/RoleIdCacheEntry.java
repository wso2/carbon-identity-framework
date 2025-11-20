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

import org.wso2.carbon.identity.core.cache.CacheEntry;

/**
 * Role Id Cache Entry for RoleIdCacheByName cache.
 */
public class RoleIdCacheEntry extends CacheEntry {

    private static final long serialVersionUID = 6248357192038471592L;

    private String roleId;

    /**
     * Constructor to create RoleIdCacheEntry.
     *
     * @param roleId Role Id of the role to be added to the cache.
     */
    public RoleIdCacheEntry(String roleId) {
        
        this.roleId = roleId;
    }

    /**
     * Get the role Id.
     *
     * @return Role Id.
     */
    public String getRoleId() {
        
        return roleId;
    }

    /**
     * Set the role Id.
     *
     * @param roleId Role Id.
     */
    public void setRoleId(String roleId) {
        
        this.roleId = roleId;
    }
}
