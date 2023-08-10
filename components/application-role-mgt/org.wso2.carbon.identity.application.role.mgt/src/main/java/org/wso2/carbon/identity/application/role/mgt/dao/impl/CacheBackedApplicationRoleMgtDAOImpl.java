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
import org.wso2.carbon.identity.application.role.mgt.cache.ApplicationRoleCache;
import org.wso2.carbon.identity.application.role.mgt.cache.ApplicationRoleCacheEntry;
import org.wso2.carbon.identity.application.role.mgt.cache.ApplicationRoleCacheKey;
import org.wso2.carbon.identity.application.role.mgt.dao.ApplicationRoleMgtDAO;
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
    public ApplicationRole addApplicationRole(ApplicationRole applicationRole, int TenantID)
            throws ApplicationRoleManagementServerException {

        return applicationRoleMgtDAO.addApplicationRole(applicationRole, TenantID);
    }

    @Override
    public ApplicationRole getApplicationRoleById(String roleId, String tenantDomain) {

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
    public List<ApplicationRole> getApplicationRoles(String applicationId) {

        return applicationRoleMgtDAO.getApplicationRoles(applicationId);
    }

    @Override
    public void updateApplicationRole(String applicationId, String roleId, String tenantDomain) {

        clearFromCache(roleId, tenantDomain);
        applicationRoleMgtDAO.updateApplicationRole(applicationId, roleId, tenantDomain);
    }

    @Override
    public void deleteApplicationRole(String applicationId, String roleId, String tenantDomain) {

        clearFromCache(roleId, tenantDomain);
        applicationRoleMgtDAO.deleteApplicationRole(applicationId, roleId, tenantDomain);
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
