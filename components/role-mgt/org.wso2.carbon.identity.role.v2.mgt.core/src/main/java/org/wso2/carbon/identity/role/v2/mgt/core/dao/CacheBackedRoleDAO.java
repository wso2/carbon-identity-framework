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
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleIdCacheByName;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleIdCacheEntry;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleNameCacheKey;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.GroupBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.IdpGroup;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleDTO;
import org.wso2.carbon.identity.role.v2.mgt.core.model.UserBasicInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cache layer implementation of the {@link RoleDAO} interface.
 * Delegates calls to the underlying RoleDAO implementation.
 */
public class CacheBackedRoleDAO implements RoleDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedRoleDAO.class);
    private final RoleDAOImpl roleDAO;
    private final RoleIdCacheByName roleCacheByName;

    public CacheBackedRoleDAO(RoleDAOImpl roleDAO) {

        this.roleDAO = roleDAO;
        roleCacheByName = RoleIdCacheByName.getInstance();
    }

    @Override
    public RoleBasicInfo addRole(String roleName, List<String> userList, List<String> groupList,
                                 List<Permission> permissions, String audience, String audienceId,
                                 String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.addRole(roleName, userList, groupList, permissions, audience, audienceId, tenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder,
                                        String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.getRoles(limit, offset, sortBy, sortOrder, tenantDomain);
    }

    @Override
    public List<Role> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder, String tenantDomain,
                               List<String> requiredAttributes) throws IdentityRoleManagementException {

        return roleDAO.getRoles(limit, offset, sortBy, sortOrder, tenantDomain, requiredAttributes);
    }

    @Override
    public List<RoleBasicInfo> getRoles(List<ExpressionNode> expressionNodes, Integer limit, Integer offset,
                                        String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getRoles(expressionNodes, limit, offset, sortBy, sortOrder, tenantDomain);
    }

    @Override
    public List<Role> getRoles(List<ExpressionNode> expressionNodes, Integer limit, Integer offset, String sortBy,
                               String sortOrder, String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

        return roleDAO.getRoles(expressionNodes, limit, offset, sortBy, sortOrder, tenantDomain, requiredAttributes);
    }

    @Override
    public Role getRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.getRole(roleId, tenantDomain);
    }

    @Override
    public Role getRole(String roleId) throws IdentityRoleManagementException {

        return roleDAO.getRole(roleId);
    }

    @Override
    public List<Permission> getPermissionListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getPermissionListOfRole(roleId, tenantDomain);
    }

    @Override
    public List<String> getPermissionListOfRoles(List<String> roleIds, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getPermissionListOfRoles(roleIds, tenantDomain);
    }

    @Override
    public void updatePermissionListOfRole(String roleId, List<Permission> addedPermissions,
                                           List<Permission> deletedPermissions, String tenantDomain)
            throws IdentityRoleManagementException {

        roleDAO.updatePermissionListOfRole(roleId, addedPermissions, deletedPermissions, tenantDomain);
    }

    @Override
    public List<IdpGroup> getIdpGroupListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getIdpGroupListOfRole(roleId, tenantDomain);
    }

    @Override
    public void updateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupList,
                                         List<IdpGroup> deletedGroupList, String tenantDomain)
            throws IdentityRoleManagementException {

        roleDAO.updateIdpGroupListOfRole(roleId, newGroupList, deletedGroupList, tenantDomain);
    }

    @Override
    public void deleteRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        Role role = roleDAO.getRole(roleId, tenantDomain);
        RoleNameCacheKey cacheKey = new RoleNameCacheKey(role.getName(), role.getAudience(), role.getAudienceId());
        roleCacheByName.clearCacheEntry(cacheKey, tenantDomain);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache cleared for role: " + role.getName());
        }
        roleDAO.deleteRole(roleId, tenantDomain);
    }

    @Override
    public void updateRoleName(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        Role role = roleDAO.getRole(roleId, tenantDomain);
        RoleNameCacheKey cacheKey = new RoleNameCacheKey(role.getName(), role.getAudience(), role.getAudienceId());
        roleCacheByName.clearCacheEntry(cacheKey, tenantDomain);
        roleDAO.updateRoleName(roleId, newRoleName, tenantDomain);
    }

    @Override
    public List<UserBasicInfo> getUserListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getUserListOfRole(roleId, tenantDomain);
    }

    @Override
    public void updateGroupListOfRole(String roleId, List<String> newGroupIDList, List<String> deletedGroupIDList,
                                      String tenantDomain) throws IdentityRoleManagementException {

        roleDAO.updateGroupListOfRole(roleId, newGroupIDList, deletedGroupIDList, tenantDomain);
    }

    @Override
    public List<GroupBasicInfo> getGroupListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getGroupListOfRole(roleId, tenantDomain);
    }

    @Override
    public void updateUserListOfRole(String roleId, List<String> newUserIDList, List<String> deletedUserIDList,
                                     String tenantDomain) throws IdentityRoleManagementException {

        roleDAO.updateUserListOfRole(roleId, newUserIDList, deletedUserIDList, tenantDomain);
    }

    @Override
    public String getRoleNameByID(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.getRoleNameByID(roleId, tenantDomain);
    }

    @Override
    public String getRoleIdByName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleNameCacheKey cacheKey = new RoleNameCacheKey(roleName, audience, audienceId);
        RoleIdCacheEntry entry = roleCacheByName.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache hit for role: " + roleName);
            }
            return entry.getRoleId();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting role ID for role name: " + roleName + " in tenant: " + tenantDomain);
        }
        String roleId = roleDAO.getRoleIdByName(roleName, audience, audienceId, tenantDomain);

        if (roleName != null) {
            roleCacheByName.addToCache(cacheKey, new RoleIdCacheEntry(roleId), tenantDomain);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cached role ID for role: " + roleName);
            }
        }
        return roleId;
    }

    @Override
    public boolean isExistingRoleName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.isExistingRoleName(roleName, audience, audienceId, tenantDomain);
    }

    @Override
    public boolean isExistingRoleID(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.isExistingRoleID(roleId, tenantDomain);
    }

    @Override
    public RoleBasicInfo getRoleBasicInfoById(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getRoleBasicInfoById(roleId, tenantDomain);
    }

    @Override
    public Set<String> getSystemRoles() {

        return roleDAO.getSystemRoles();
    }

    @Override
    public int getRolesCount(String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.getRolesCount(tenantDomain);
    }

    @Override
    public int getRolesCount(List<ExpressionNode> expressionNodes, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getRolesCount(expressionNodes, tenantDomain);
    }

    @Override
    public Role getRoleWithoutUsers(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.getRoleWithoutUsers(roleId, tenantDomain);
    }

    @Override
    public void addMainRoleToSharedRoleRelationship(String mainRoleUUID, String sharedRoleUUID,
                                                    String mainRoleTenantDomain, String sharedRoleTenantDomain)
            throws IdentityRoleManagementException {

        roleDAO.addMainRoleToSharedRoleRelationship(mainRoleUUID, sharedRoleUUID, mainRoleTenantDomain,
                sharedRoleTenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoleListOfUser(String userId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getRoleListOfUser(userId, tenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoleListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getRoleListOfGroups(groupIds, tenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoleListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getRoleListOfIdpGroups(groupIds, tenantDomain);
    }

    @Override
    public List<String> getRoleIdListOfUser(String userId, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.getRoleIdListOfUser(userId, tenantDomain);
    }

    @Override
    public List<String> getRoleIdListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getRoleIdListOfGroups(groupIds, tenantDomain);
    }

    @Override
    public List<String> getRoleIdListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getRoleIdListOfIdpGroups(groupIds, tenantDomain);
    }

    @Override
    public void deleteRolesByApplication(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

        roleDAO.deleteRolesByApplication(applicationId, tenantDomain);
    }

    @Override
    public Map<String, String> getMainRoleToSharedRoleMappingsBySubOrg(List<String> roleIds, String subOrgTenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getMainRoleToSharedRoleMappingsBySubOrg(roleIds, subOrgTenantDomain);
    }

    @Override
    public Map<String, String> getSharedRoleToMainRoleMappingsBySubOrg(List<String> roleIds, String subOrgTenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getSharedRoleToMainRoleMappingsBySubOrg(roleIds, subOrgTenantDomain);
    }

    @Override
    public List<String> getAssociatedApplicationIdsByRoleId(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getAssociatedApplicationIdsByRoleId(roleId, tenantDomain);
    }

    @Override
    public int getRoleAudienceRefId(String audience, String audienceId) throws IdentityRoleManagementException {

        return roleDAO.getRoleAudienceRefId(audience, audienceId);
    }

    @Override
    public List<RoleDTO> getSharedHybridRoles(String roleId, int mainTenantId) throws IdentityRoleManagementException {

        return roleDAO.getSharedHybridRoles(roleId, mainTenantId);
    }

    @Override
    public boolean isSharedRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.isSharedRole(roleId, tenantDomain);
    }
}
