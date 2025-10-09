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

package org.wso2.carbon.identity.role.v2.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleIdCacheByName;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleIdCacheEntry;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleNameCacheKey;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.util.RoleManagementUtils;

/**
 * Cache layer implementation of the {@link RoleDAO} interface.
 * Delegates calls to the underlying RoleDAO implementation.
 */
public class CacheBackedRoleDAO extends RoleDAOImpl {

    private static final Log LOG = LogFactory.getLog(CacheBackedRoleDAO.class);

    private final RoleIdCacheByName roleCacheByName;

    public CacheBackedRoleDAO() {

        roleCacheByName = RoleIdCacheByName.getInstance();
    }

    @Override
    public String getRoleIdByName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (!RoleManagementUtils.getEveryOneRoleName(tenantDomain).equals(roleName)) {
            return super.getRoleIdByName(roleName, audience, audienceId, tenantDomain);
        }

        /* The cache is added only for the Everyone role, as it is the only role name frequently used, due to its use in
         listing user roles. Since the Everyone role cannot be modified or deleted, no invalidation scenario is
         required. However, if this method is later extended to cache other role names, please ensure the corresponding
          invalidation scenarios are implemented.
         */
        RoleNameCacheKey cacheKey = new RoleNameCacheKey(roleName, audience, audienceId);
        RoleIdCacheEntry entry = roleCacheByName.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache hit for role: " + roleName + " in tenant: " + tenantDomain);
            }
            return entry.getRoleId();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting role ID for role name: " + roleName + " in tenant: " + tenantDomain);
        }
        String roleId = super.getRoleIdByName(roleName, audience, audienceId, tenantDomain);

        if (roleName != null) {
            roleCacheByName.addToCache(cacheKey, new RoleIdCacheEntry(roleId), tenantDomain);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cached role ID for role: " + roleName + " to tenant: " + tenantDomain);
            }
        }
        return roleId;
    }
}
