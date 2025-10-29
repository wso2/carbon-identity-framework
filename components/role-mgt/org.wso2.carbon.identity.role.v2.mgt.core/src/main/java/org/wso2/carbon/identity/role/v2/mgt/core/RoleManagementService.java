/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.role.v2.mgt.core;

import org.apache.commons.lang.NotImplementedException;
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
 * OSGi service interface which use to manage roles.
 */
public interface RoleManagementService {

    /**
     * Add a new role.
     *
     * @param roleName     Role name.
     * @param userList     List of users IDs.
     * @param groupList    List of groups IDs.
     * @param permissions  List of permissions.
     * @param audience     Audience.
     * @param audienceId   Audience ID.
     * @param tenantDomain Tenant domain.
     * @return Basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo addRole(String roleName, List<String> userList, List<String> groupList, List<Permission> permissions,
                          String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Retrieve available roles.
     *
     * @param limit        Limit value.
     * @param offset       Offset value.
     * @param sortBy       SortBy value.
     * @param sortOrder    Sort order value.
     * @param tenantDomain Tenant domain.
     * @return List of roles.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<RoleBasicInfo> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Retrieve available roles.
     *
     * @param limit              Limit value.
     * @param offset             Offset value.
     * @param sortBy             SortBy value.
     * @param sortOrder          Sort order value.
     * @param tenantDomain       Tenant domain.
     * @param requiredAttributes Required attributes.
     * @return List of roles.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    default List<Role> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder, String tenantDomain,
                                List<String> requiredAttributes) throws IdentityRoleManagementException {

        throw new NotImplementedException("getRoles method is not implemented");
    }

    /**
     * Retrieve available roles.
     *
     * @param filter       Filter for the Role ID.
     * @param limit        Limit value.
     * @param offset       Offset value.
     * @param sortBy       SortBy value.
     * @param sortOrder    Sort order value.
     * @param tenantDomain Tenant domain.
     * @return List of roles.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<RoleBasicInfo> getRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
                                 String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Retrieve available roles.
     *
     * @param filter             Filter for the Role ID.
     * @param limit              Limit value.
     * @param offset             Offset value.
     * @param sortBy             SortBy value.
     * @param sortOrder          Sort order value.
     * @param tenantDomain       Tenant domain.
     * @param requiredAttributes Required attributes.
     * @return List of roles.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    default List<Role> getRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
                                String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

        throw new NotImplementedException("getRoles method is not implemented");
    }

    /**
     * Retrieve the given role.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @return The role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    Role getRole(String roleId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Retrieve the given role.
     *
     * @param roleId Role ID.
     * @return The role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    Role getRole(String roleId) throws IdentityRoleManagementException;

    /**
     * Get role basic info by id.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @return RoleBasicInfo.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo getRoleBasicInfoById(String roleId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Update Role ID.
     *
     * @param roleId       Role ID.
     * @param newRoleName  New Role ID.
     * @param tenantDomain Tenant domain.
     * @return Basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo updateRoleName(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Delete the given role.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    void deleteRole(String roleId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get user list of the given role.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @return List of users.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<UserBasicInfo> getUserListOfRole(String roleId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get user list of the given role filtered by the provided expression nodes.
     *
     * @param filter          Role filter.
     * @param limit           Limit value.
     * @param offset          Offset value.
     * @param sortBy          SortBy value.
     * @param sortOrder       Sort order value.
     * @param tenantDomain    Tenant domain.
     * @param userStoreDomain User store domain.
     * @return List of users.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    default List<UserBasicInfo> getUserListOfRoles(String filter, Integer limit, Integer offset, String sortBy,
                                                   String sortOrder, String tenantDomain, String userStoreDomain)
            throws IdentityRoleManagementException {

        throw new NotImplementedException("getUserListOfRoles method is not implemented");
    }

    /**
     * Update the list of users in the given role.
     *
     * @param roleId            Role ID.
     * @param newUserIDList     The set of new users IDs.
     * @param deletedUserIDList The set of deleted users IDs.
     * @param tenantDomain      Tenant domain.
     * @return Basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo updateUserListOfRole(String roleId, List<String> newUserIDList, List<String> deletedUserIDList,
                                       String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get group list of the given role.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @return List of groups.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<GroupBasicInfo> getGroupListOfRole(String roleId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Update the list of groups in the given role.
     *
     * @param roleId             Role ID.
     * @param newGroupIDList     The set of new group IDs.
     * @param deletedGroupIDList The set of deleted group IDs.
     * @param tenantDomain       Tenant domain.
     * @return Basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo updateGroupListOfRole(String roleId, List<String> newGroupIDList, List<String> deletedGroupIDList,
                                        String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get idp group list of the given role.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @return List of idp groups.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<IdpGroup> getIdpGroupListOfRole(String roleId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Update the list of idp groups in the given role.
     *
     * @param roleId           Role ID.
     * @param newGroupList     The set of new groups.
     * @param deletedGroupList The set of deleted groups.
     * @param tenantDomain     Tenant domain.
     * @return Basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo updateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupList, List<IdpGroup> deletedGroupList,
                                           String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get permission list of the given role.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @return List of permissions.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<Permission> getPermissionListOfRole(String roleId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get permission list of the given role ids.
     *
     * @param roleIds      Role IDs.
     * @param tenantDomain Tenant domain.
     * @return List of permissions.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<String> getPermissionListOfRoles(List<String> roleIds, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Get permission list of the given role.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @return List of permissions.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo updatePermissionListOfRole(String roleId, List<Permission> addedPermissions,
                                             List<Permission> deletedPermissions, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Check whether the given role ID exist.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @return {@code true} if the the given role ID exist.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    boolean isExistingRole(String roleId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Check whether the given role name exist.
     *
     * @param roleName     Role name.
     * @param tenantDomain Tenant domain.
     * @return {@code true} if the the given role ID exist.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    boolean isExistingRoleName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Get the list of system roles.
     *
     * @return A set of system roles.
     */
    Set<String> getSystemRoles();

    /**
     * Retrieve the count of tenant roles.
     *
     * @param tenantDomain tenant domain.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    int getRolesCount(String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Retrieve available total roles count in a tenant for a given specific search filter.
     *
     * @param tenantDomain Tenant domain.
     * @return The list count of roles.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    int getRolesCount(String searchFilter, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get role without users.
     *
     * @param roleId       Role ID.
     * @param tenantDomain Tenant domain.
     * @return The role object without users.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    Role getRoleWithoutUsers(String roleId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get Role name by role id.
     *
     * @param roleId       Role Id.
     * @param tenantDomain Tenant Domain.
     * @return Role name for the given role id.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    String getRoleNameByRoleId(String roleId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get Role id by name.
     *
     * @param roleName     Role Id.
     * @param audience     Audience.
     * @param audienceId   Audience ID.
     * @param tenantDomain Tenant Domain.
     * @return Role name for the given role id.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    String getRoleIdByName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Add the relationship between main role and the shared role.
     *
     * @param mainRoleUUID           Main role UUID.
     * @param sharedRoleUUID         Shared role UUID.
     * @param mainRoleTenantDomain   Main role tenant domain.
     * @param sharedRoleTenantDomain Shared role tenant domain.
     * @throws IdentityRoleManagementException Error occurred while adding the relationship.
     */
    void addMainRoleToSharedRoleRelationship(String mainRoleUUID, String sharedRoleUUID, String mainRoleTenantDomain,
                                             String sharedRoleTenantDomain) throws IdentityRoleManagementException;

    /**
     * Get role list of user.
     *
     * @param userId       User ID.
     * @param tenantDomain Tenant domain.
     * @return The list of basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<RoleBasicInfo> getRoleListOfUser(String userId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get role list of groups.
     *
     * @param groupIds     Group IDs.
     * @param tenantDomain Tenant domain.
     * @return The list of basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<RoleBasicInfo> getRoleListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Get role list of idp groups.
     *
     * @param groupIds     Idp Group IDs.
     * @param tenantDomain Tenant domain.
     * @return The list of basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<RoleBasicInfo> getRoleListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Get role id list of user.
     *
     * @param userId       User ID.
     * @param tenantDomain Tenant domain.
     * @return The list of role id.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<String> getRoleIdListOfUser(String userId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get role id list of groups.
     *
     * @param groupIds     Group IDs.
     * @param tenantDomain Tenant domain.
     * @return The list of role id.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<String> getRoleIdListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Get role id list of idp groups.
     *
     * @param groupIds     Idp Group IDs.
     * @param tenantDomain Tenant domain.
     * @return The list of role id.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<String> getRoleIdListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Delete all roles associated app by id.
     *
     * @param applicationId Idp Group IDs.
     * @param tenantDomain  Tenant domain.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    void deleteRolesByApplication(String applicationId, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get main role to shared role mappings by subOrg.
     *
     * @param roleIds            Main role IDs.
     * @param subOrgTenantDomain Sub Organization tenant domain.
     * @return The map of main role id to shared roles.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    Map<String, String> getMainRoleToSharedRoleMappingsBySubOrg(List<String> roleIds, String subOrgTenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Get shared role to main role mappings by subOrg.
     *
     * @param roleIds            Shared role IDs.
     * @param subOrgTenantDomain Sub Organization tenant domain.
     * @return The map of shared role id to main roles.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    default Map<String, String> getSharedRoleToMainRoleMappingsBySubOrg(List<String> roleIds, String subOrgTenantDomain)
            throws IdentityRoleManagementException {

        throw new NotImplementedException("getSharedRoleToMainRoleMappingsBySubOrg method is not implemented");
    }

    /**
     * Get associated applications by role id.
     *
     * @param roleId       Role Id.
     * @param tenantDomain Tenant Domain.
     * @return List of application ids.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<String> getAssociatedApplicationByRoleId(String roleId, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Get shared hybrid roles for the given main role ID.
     *
     * @param roleId   The main role ID.
     * @param tenantId The tenant ID.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    default List<RoleDTO> getSharedHybridRoles(String roleId, int tenantId) throws IdentityRoleManagementException {

        return null;
    }
}
