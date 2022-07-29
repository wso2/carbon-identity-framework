/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.role.mgt.core.dao;

import org.apache.commons.lang.NotImplementedException;
import org.wso2.carbon.identity.role.mgt.core.GroupBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.Role;
import org.wso2.carbon.identity.role.mgt.core.RoleBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.UserBasicInfo;

import java.util.List;
import java.util.Set;

/**
 * RoleDAO interface.
 */
public interface RoleDAO {

    /**
     * Add a new role.
     *
     * @param roleName     Role name.
     * @param userList     List of users.
     * @param groupList    List of groups.
     * @param permissions  List of permissions.
     * @param tenantDomain Tenant domain.
     * @return Basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo addRole(String roleName, List<String> userList, List<String> groupList, List<String> permissions,
            String tenantDomain) throws IdentityRoleManagementException;

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
     * Retrieve available roles matching the provided filter.
     *
     * @param filter       Filter for the role name.
     * @param limit        Limit value.
     * @param offset       Offset value.
     * @param sortBy       SortBy value.
     * @param sortOrder    Sort order value.
     * @param tenantDomain Tenant domain.
     * @return List of roles.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<RoleBasicInfo> getRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
            String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Retrieve the given role.
     *
     * @param roleID       Role ID.
     * @param tenantDomain Tenant domain.
     * @return The role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    Role getRole(String roleID, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Update role name.
     *
     * @param roleID       Role ID.
     * @param newRoleName  New role name.
     * @param tenantDomain Tenant domain.
     * @return Basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo updateRoleName(String roleID, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Delete the given role.
     *
     * @param roleID       Role ID.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    void deleteRole(String roleID, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Check whether the given role name exist.
     *
     * @param roleName     Role name.
     * @param tenantDomain Tenant domain.
     * @return {@code true} if the the given role exist.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    boolean isExistingRoleName(String roleName, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Check whether the given role ID exist.
     *
     * @param roleID       Role ID.
     * @param tenantDomain Tenant domain.
     * @return {@code true} if the the given role exist.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    boolean isExistingRoleID(String roleID, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get user list of the given role.
     *
     * @param roleID       Role ID.
     * @param tenantDomain Tenant domain.
     * @return List of users.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<UserBasicInfo> getUserListOfRole(String roleID, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Update the list of users in the given role.
     *
     * @param roleID            Role ID.
     * @param newUserIDList     The set of new users IDs.
     * @param deletedUserIDList The set of deleted users IDs.
     * @param tenantDomain      Tenant domain.
     * @return Basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo updateUserListOfRole(String roleID, List<String> newUserIDList, List<String> deletedUserIDList,
            String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get group list of the given role.
     *
     * @param roleID       Role ID.
     * @param tenantDomain Tenant domain.
     * @return List of groups.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<GroupBasicInfo> getGroupListOfRole(String roleID, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Update the list of groups in the given role.
     *
     * @param roleID             Role ID.
     * @param newGroupIDList     The set of new group IDs.
     * @param deletedGroupIDList The set of deleted group IDs.
     * @param tenantDomain       Tenant domain.
     * @return Basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo updateGroupListOfRole(String roleID, List<String> newGroupIDList, List<String> deletedGroupIDList,
            String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get permission list of the given role.
     *
     * @param roleID       Role ID.
     * @param tenantDomain Tenant domain.
     * @return List of permissions.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<String> getPermissionListOfRole(String roleID, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Set the list of permission to the given role.
     *
     * @param roleID      Role ID.
     * @param permissions List of permissions.
     * @return Basic role object.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo setPermissionsForRole(String roleID, List<String> permissions, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Remove the given user from all the assigned roles.
     *
     * @param userID       User ID.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    void deleteUser(String userID, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Remove the given group from all the assigned roles.
     *
     * @param groupID      Group ID.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    void deleteGroup(String groupID, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Retrieve the role id for the given name.
     *
     * @param roleName     Role name.
     * @param tenantDomain tenant domain.
     * @return role ID.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    String getRoleIDByName(String roleName, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Retrieve the role name for the given ID.
     *
     * @param roleID       Role ID.
     * @param tenantDomain tenant domain.
     * @return role ID.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    String getRoleNameByID(String roleID, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get the list of system roles.
     *
     * @return A set of system roles.
     */
    default Set<String> getSystemRoles() throws NotImplementedException {

        throw new NotImplementedException("getSystemRoles method is not implemented");
    }

    /**
     * Retrieve the count of tenant roles.
     *
     * @param tenantDomain tenant domain.
     */
    default int getRolesCount(String tenantDomain) throws IdentityRoleManagementException {

        throw new NotImplementedException("getRoleCount method is not implemented");
    }
}
