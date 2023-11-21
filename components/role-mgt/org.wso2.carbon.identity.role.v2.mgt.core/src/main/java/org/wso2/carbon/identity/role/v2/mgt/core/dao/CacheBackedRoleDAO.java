package org.wso2.carbon.identity.role.v2.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleCacheById;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleCacheEntry;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleIdCacheKey;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.GroupBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.IdpGroup;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.UserBasicInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CacheBackedRoleDAO implements RoleDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedRoleDAO.class);
    private final RoleCacheById roleCacheById;
    private final RoleDAO roleDAO;

    public CacheBackedRoleDAO(RoleDAO roleDAO) {

        this.roleDAO = roleDAO;
        roleCacheById = RoleCacheById.getInstance();
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
        }
        return role;
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
    public void updateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupList, List<IdpGroup> deletedGroupList,
                                         String tenantDomain) throws IdentityRoleManagementException {

        roleDAO.updateIdpGroupListOfRole(roleId, newGroupList, deletedGroupList, tenantDomain);
    }

    @Override
    public void deleteRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        roleDAO.deleteRole(roleId, tenantDomain);
    }

    @Override
    public void updateRoleName(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

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

        return roleDAO.getRoleIdByName(roleName, audience, audienceId, tenantDomain);
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
    public List<String> getAssociatedApplicationIdsByRoleId(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getAssociatedApplicationIdsByRoleId(roleId, tenantDomain);
    }
}
