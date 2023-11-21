package org.wso2.carbon.identity.role.v2.mgt.core.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.cache.APIResourceIdCacheKey;
import org.wso2.carbon.identity.api.resource.mgt.cache.APIResourceIdentifierCacheKey;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleCacheById;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleCacheEntry;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleIdCacheByName;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleIdCacheEntry;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleIdCacheKey;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleNameCacheKey;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.AssociatedApplication;
import org.wso2.carbon.identity.role.v2.mgt.core.model.GroupBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.IdpGroup;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.UserBasicInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CacheBackedRoleDAO implements RoleDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedRoleDAO.class);
    private final RoleCacheById roleCacheById;
    private final RoleIdCacheByName roleIdCacheByName;
    private final RoleDAO roleDAO;

    public CacheBackedRoleDAO(RoleDAO roleDAO) {

        this.roleDAO = roleDAO;
        roleCacheById = RoleCacheById.getInstance();
        roleIdCacheByName =  RoleIdCacheByName.getInstance();
    }

    @Override
    public RoleBasicInfo addRole(String roleName, List<String> userList, List<String> groupList,
                                 List<Permission> permissions, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.addRole(roleName, userList, groupList, permissions, audience, audienceId, tenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder,
                                        String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.getRoles(limit, offset, sortBy, sortOrder, tenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoles(List<ExpressionNode> expressionNodes, Integer limit, Integer offset,
                                        String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getRoles(expressionNodes, limit, offset, sortBy, sortOrder, tenantDomain);
    }

    @Override
    public Role getRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        RoleIdCacheKey cacheKey =  new RoleIdCacheKey(roleId);
        RoleCacheEntry entry = roleCacheById.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role " + roleId);
            }
            return entry.getRole();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleId + ". Fetching entry from DB");
        }

        Role role = roleDAO.getRole(roleId, tenantDomain);

        if (role != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for role " + roleId + ". Updating cache");
            }
            roleCacheById.addToCache(cacheKey, new RoleCacheEntry(role), tenantDomain);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for role " + roleId + " not found in cache or DB");
            }
        }
        return role;
    }

    @Override
    public List<Permission> getPermissionListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleIdCacheKey cacheKey =  new RoleIdCacheKey(roleId);
        RoleCacheEntry entry = roleCacheById.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role " + roleId);
            }
            return entry.getRole().getPermissions();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleId + ". Fetching entry from DB");
        }
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

        clearRoleCacheById(roleId, tenantDomain);
        roleDAO.updatePermissionListOfRole(roleId, addedPermissions, deletedPermissions, tenantDomain);
    }

    @Override
    public List<IdpGroup> getIdpGroupListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleIdCacheKey cacheKey =  new RoleIdCacheKey(roleId);
        RoleCacheEntry entry = roleCacheById.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role " + roleId);
            }
            return entry.getRole().getIdpGroups();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleId + ". Fetching entry from DB");
        }
        return roleDAO.getIdpGroupListOfRole(roleId, tenantDomain);
    }

    @Override
    public void updateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupList, List<IdpGroup> deletedGroupList,
                                         String tenantDomain) throws IdentityRoleManagementException {

        clearRoleCacheById(roleId, tenantDomain);
        roleDAO.updateIdpGroupListOfRole(roleId, newGroupList, deletedGroupList, tenantDomain);
    }

    @Override
    public void deleteRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        clearRoleCacheById(roleId, tenantDomain);
        roleDAO.deleteRole(roleId, tenantDomain);
    }

    @Override
    public void updateRoleName(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        clearRoleCacheById(roleId, tenantDomain);
        roleDAO.updateRoleName(roleId, newRoleName, tenantDomain);
    }

    @Override
    public List<UserBasicInfo> getUserListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleIdCacheKey cacheKey =  new RoleIdCacheKey(roleId);
        RoleCacheEntry entry = roleCacheById.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role " + roleId);
            }
            return entry.getRole().getUsers();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleId + ". Fetching entry from DB");
        }
        return roleDAO.getUserListOfRole(roleId, tenantDomain);
    }

    @Override
    public void updateGroupListOfRole(String roleId, List<String> newGroupIDList, List<String> deletedGroupIDList,
                                      String tenantDomain) throws IdentityRoleManagementException {

        clearRoleCacheById(roleId, tenantDomain);
        roleDAO.updateGroupListOfRole(roleId, newGroupIDList, deletedGroupIDList, tenantDomain);
    }

    @Override
    public List<GroupBasicInfo> getGroupListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleIdCacheKey cacheKey =  new RoleIdCacheKey(roleId);
        RoleCacheEntry entry = roleCacheById.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role " + roleId);
            }
            return entry.getRole().getGroups();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleId + ". Fetching entry from DB");
        }
        return roleDAO.getGroupListOfRole(roleId, tenantDomain);
    }

    @Override
    public void updateUserListOfRole(String roleId, List<String> newUserIDList, List<String> deletedUserIDList,
                                     String tenantDomain) throws IdentityRoleManagementException {

        clearRoleCacheById(roleId, tenantDomain);
        roleDAO.updateUserListOfRole(roleId, newUserIDList, deletedUserIDList, tenantDomain);
    }

    @Override
    public String getRoleNameByID(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        RoleIdCacheKey cacheKey =  new RoleIdCacheKey(roleId);
        RoleCacheEntry entry = roleCacheById.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role " + roleId);
            }
            return entry.getRole().getId();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleId + ". Fetching entry from DB");
        }
        return roleDAO.getRoleNameByID(roleId, tenantDomain);
    }

    @Override
    public String getRoleIdByName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleNameCacheKey cacheKey =  new RoleNameCacheKey(roleName, audience, audienceId, tenantDomain);
        RoleIdCacheEntry entry = roleIdCacheByName.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role : " + roleName + " audience : " + audience
                        + " audienceId : " + audienceId + "tenant domain : " + tenantDomain);
            }
            return entry.getRoleId();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleName + " audience : " + audience
                    + " audienceId : " + audienceId + "tenant domain : " + tenantDomain + ". Fetching entry from DB");
        }

        String roleId = roleDAO.getRoleIdByName(roleName, audience, audienceId, tenantDomain);

        if (StringUtils.isNotBlank(roleId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for role " + roleName + " audience : " + audience
                        + " audienceId : " + audienceId + "tenant domain : " + tenantDomain + ". Updating cache");
            }
            roleIdCacheByName.addToCache(cacheKey, new RoleIdCacheEntry(roleId), tenantDomain);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for role " + roleName + " audience : " + audience + " audienceId : "
                        + audienceId + "tenant domain : " + tenantDomain + " not found in cache or DB");
            }
        }
        return roleId;
    }

    @Override
    public boolean isExistingRoleName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleNameCacheKey cacheKey =  new RoleNameCacheKey(roleName, audience, audienceId, tenantDomain);
        RoleIdCacheEntry entry = roleIdCacheByName.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role : " + roleName + " audience : " + audience
                        + " audienceId : " + audienceId + "tenant domain : " + tenantDomain);
            }
            return true;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleName + " audience : " + audience
                    + " audienceId : " + audienceId + "tenant domain : " + tenantDomain + ". Fetching entry from DB");
        }
        return roleDAO.isExistingRoleName(roleName, audience, audienceId, tenantDomain);
    }

    @Override
    public boolean isExistingRoleID(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        RoleIdCacheKey cacheKey =  new RoleIdCacheKey(roleId);
        RoleCacheEntry entry = roleCacheById.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role " + roleId);
            }
            return true;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleId + ". Fetching entry from DB");
        }
        return roleDAO.isExistingRoleID(roleId, tenantDomain);
    }

    @Override
    public RoleBasicInfo getRoleBasicInfoById(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleIdCacheKey cacheKey =  new RoleIdCacheKey(roleId);
        RoleCacheEntry entry = roleCacheById.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role " + roleId);
            }
            Role role = entry.getRole();
            RoleBasicInfo roleBasicInfo = new RoleBasicInfo(role.getId(), role.getName());
            roleBasicInfo.setAudienceId(role.getAudienceId());
            roleBasicInfo.setAudience(role.getAudience());
            roleBasicInfo.setAudienceName(role.getAudienceName());
            return roleBasicInfo;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleId + ". Fetching entry from DB");
        }
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
    public Role getRoleWithoutUsers(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        RoleIdCacheKey cacheKey =  new RoleIdCacheKey(roleId);
        RoleCacheEntry entry = roleCacheById.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role " + roleId);
            }
            Role role = entry.getRole();
            role.setUsers(null);
            return role;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleId + ". Fetching entry from DB");
        }
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

        roleCacheById.clear(tenantDomain);
        roleDAO.deleteRolesByApplication(applicationId, tenantDomain);
    }

    @Override
    public Map<String, String> getMainRoleToSharedRoleMappingsBySubOrg(List<String> roleIds, String subOrgTenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getMainRoleToSharedRoleMappingsBySubOrg(roleIds, subOrgTenantDomain);
    }

    @Override
    public List<String> getAssociatedApplicationIdsByRoleId(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleIdCacheKey cacheKey =  new RoleIdCacheKey(roleId);
        RoleCacheEntry entry = roleCacheById.getValueFromCache(cacheKey, tenantDomain);
        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for role " + roleId);
            }
            return entry.getRole().getAssociatedApplications().stream().map(AssociatedApplication::getId).collect(
                    Collectors.toList());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for role " + roleId + ". Fetching entry from DB");
        }
        return roleDAO.getAssociatedApplicationIdsByRoleId(roleId, tenantDomain);
    }

    private void clearRoleCacheById(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        // clearing cache entries related to the role by id.
        Role role = null;
        if (StringUtils.isNotBlank(roleId)) {
            role = this.getRole(roleId, tenantDomain);
        }

        if (role != null) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Removing entry for role " + role.getName() + " of tenant domain:"
                        + tenantDomain + " from cache.");
            }
            RoleIdCacheKey roleIdCacheKey = new RoleIdCacheKey(roleId);
            roleCacheById.clearCacheEntry(roleIdCacheKey, tenantDomain);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for role " + roleId + " not found in cache or DB");
            }
        }
    }
}
