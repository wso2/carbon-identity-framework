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

package org.wso2.carbon.identity.application.role.mgt.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.role.mgt.cache.ApplicationRoleCache;
import org.wso2.carbon.identity.application.role.mgt.cache.ApplicationRoleCacheEntry;
import org.wso2.carbon.identity.application.role.mgt.cache.ApplicationRoleCacheKey;
import org.wso2.carbon.identity.application.role.mgt.dao.ApplicationRoleMgtDAO;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementServerException;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;

import java.util.List;

/**
 * Cache backed application role management DAO implementation.
 */
public class CacheBackedApplicationRoleMgtDAOImpl implements ApplicationRoleMgtDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedApplicationRoleMgtDAOImpl.class);

    private static ApplicationRoleCache applicationRoleCache;
    private final ApplicationRoleMgtDAO applicationRoleMgtDAO;

    public CacheBackedApplicationRoleMgtDAOImpl(ApplicationRoleMgtDAOImpl applicationRoleMgtDAO) {

        this.applicationRoleMgtDAO = applicationRoleMgtDAO;
        applicationRoleCache = ApplicationRoleCache.getInstance();
    }

    @Override
    public ApplicationRole addApplicationRole(ApplicationRole applicationRole, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        return applicationRoleMgtDAO.addApplicationRole(applicationRole, tenantDomain);
    }

    @Override
    public ApplicationRole getApplicationRoleById(String roleId, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        ApplicationRole applicationRole = getApplicationRoleFromCache(roleId, tenantDomain);
        if (applicationRole == null) {
            applicationRole = applicationRoleMgtDAO.getApplicationRoleById(roleId, tenantDomain);
            if (applicationRole != null) {
                addToCache(applicationRole, tenantDomain);
            }
        }
        return applicationRole;
    }

    @Override
    public List<ApplicationRole> getApplicationRoles(String applicationId)
            throws ApplicationRoleManagementServerException {

        return applicationRoleMgtDAO.getApplicationRoles(applicationId);
    }

    @Override
    public void updateApplicationRole(String applicationId, String roleId, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        clearFromCache(roleId, tenantDomain);
        applicationRoleMgtDAO.updateApplicationRole(applicationId, roleId, tenantDomain);
    }

    @Override
    public void deleteApplicationRole(String roleId, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        clearFromCache(roleId, tenantDomain);
        applicationRoleMgtDAO.deleteApplicationRole(roleId, tenantDomain);
    }

    @Override
    public boolean isExistingRole(String applicationId, String roleName, String tenantDomain)
            throws ApplicationRoleManagementServerException {

        // TODO: introduce a cache key with app id and role name.
        return applicationRoleMgtDAO.isExistingRole(applicationId, roleName, tenantDomain);
    }

    @Override
    public void updateApplicationRoleAssignedUsers(String roleId, List<String> addedUsers, List<String> removedUsers,
                                                   String tenantDomain)
            throws ApplicationRoleManagementException {

        applicationRoleMgtDAO.updateApplicationRoleAssignedUsers(roleId, addedUsers, removedUsers, tenantDomain);
    }

    @Override
    public ApplicationRole getApplicationRoleAssignedUsers(String roleId, String tenantDomain)
            throws ApplicationRoleManagementException {

        ApplicationRole applicationRole = getApplicationRoleFromCache(roleId, tenantDomain);
        if (applicationRole == null) {
            applicationRole = applicationRoleMgtDAO.getApplicationRoleById(roleId, tenantDomain);
            if (applicationRole != null) {
                addToCache(applicationRole, tenantDomain);
            }
        }
        return applicationRoleMgtDAO.getApplicationRoleAssignedUsers(roleId, tenantDomain);
    }

    @Override
    public void updateApplicationRoleAssignedGroups(String roleId, IdentityProvider identityProvider,
                                                    List<String> addedGroups, List<String> removedGroups,
                                                    String tenantDomain)
            throws ApplicationRoleManagementException {

        applicationRoleMgtDAO.updateApplicationRoleAssignedGroups(roleId, identityProvider, addedGroups, removedGroups,
                tenantDomain);
    }

    @Override
    public ApplicationRole getApplicationRoleAssignedGroups(String roleId, IdentityProvider identityProvider,
                                                            String tenantDomain)
            throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRoleAssignedGroups(roleId, identityProvider, tenantDomain);
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByUserId(String userId)
            throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRolesByUserId(userId);
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByGroupId(String groupId)
            throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRolesByGroupId(groupId);
    }

    private ApplicationRole getApplicationRoleFromCache(String applicationRoleId, String tenantDomain) {

        ApplicationRole applicationRole = null;
        if (StringUtils.isNotBlank(applicationRoleId)) {
            ApplicationRoleCacheKey cacheKey = new ApplicationRoleCacheKey(applicationRoleId);
            ApplicationRoleCacheEntry entry = applicationRoleCache.getValueFromCache(cacheKey, tenantDomain);
            if (entry != null) {
                applicationRole = entry.getApplicationRole();
            }
        }
        return applicationRole;
    }

    private void addToCache(ApplicationRole applicationRole, String tenantDomain) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    String.format("Add application role: %s in application: %s to cache", applicationRole.getRoleName(),
                            applicationRole.getApplicationId()));
        }
        ApplicationRoleCacheKey cacheKey = new ApplicationRoleCacheKey(applicationRole.getApplicationId());
        ApplicationRoleCacheEntry cacheEntry = new ApplicationRoleCacheEntry(applicationRole);
        applicationRoleCache.addToCache(cacheKey, cacheEntry, tenantDomain);
    }

    private void clearFromCache(String applicationRoleId, String tenantDomain) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Delete application role: %s from cache", applicationRoleId));
        }
        ApplicationRoleCacheKey cacheKey = new ApplicationRoleCacheKey(applicationRoleId);
        applicationRoleCache.clearCacheEntry(cacheKey, tenantDomain);
    }
}
