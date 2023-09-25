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

package org.wso2.carbon.identity.role.mgt.core.v2.dao;

import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.v2.Permission;
import org.wso2.carbon.identity.role.mgt.core.v2.Role;
import org.wso2.carbon.identity.role.mgt.core.v2.RoleBasicInfo;

import java.util.List;

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
     * @param audience  Audience.
     * @param audienceId  Audience ID.
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
                                                                        String tenantDomain)
            throws IdentityRoleManagementException;

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
     * Get permission list of the given role.
     *
     * @param roleID       Role ID.
     * @param tenantDomain Tenant domain.
     * @return List of permissions.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    List<Permission> getPermissionListOfRole(String roleID, String tenantDomain) throws IdentityRoleManagementException;

    /**
     * Get permission list of the given role.
     *
     * @param roleID       Role ID.
     * @param addedPermissions      Added Permissions.
     * @param deletedPermissions       Deleted Permissions.
     * @param tenantDomain Tenant domain.
     * @return List of permissions.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo updatePermissionListOfRole(String roleID, List<Permission> addedPermissions,
                                             List<Permission> deletedPermissions, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Handle role deletion (delete permissions, app associations, shared roles).
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
    boolean isExistingRoleName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException;

    /**
     * Get role basic info by id.
     *
     * @param roleID       Role ID.
     * @param tenantDomain Tenant domain.
     * @return RoleBasicInfo.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    RoleBasicInfo getRoleBasicInfoById(String roleID, String tenantDomain) throws IdentityRoleManagementException;
}
