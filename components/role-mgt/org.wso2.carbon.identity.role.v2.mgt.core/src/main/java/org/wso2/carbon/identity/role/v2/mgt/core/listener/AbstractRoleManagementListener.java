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

package org.wso2.carbon.identity.role.v2.mgt.core.listener;

import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.GroupBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.IdpGroup;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.UserBasicInfo;

import java.util.List;

/**
 * Abstract implementation of the RoleManagementListener interface.
 */
public abstract class AbstractRoleManagementListener implements RoleManagementListener {

    @Override
    public void preAddRole(String roleName, List<String> userList, List<String> groupList, List<Permission> permissions,
                           String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postAddRole(RoleBasicInfo roleBasicInfo, String roleName, List<String> userList, List<String> groupList,
                            List<Permission> permissions, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoles(Integer limit, Integer offset, String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoles(Integer limit, Integer offset, String sortBy, String sortOrder, String tenantDomain,
                            List<String> requiredAttributes) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoles(List<RoleBasicInfo> roleBasicInfoList, Integer limit, Integer offset, String sortBy,
                             String sortOrder, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoles(List<Role> roleInfoList, Integer limit, Integer offset, String sortBy,
                             String sortOrder, String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
                            String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
                            String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoles(List<RoleBasicInfo> roleBasicInfoList, String filter, Integer limit, Integer offset,
                             String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoles(List<Role> roleInfoList, String filter, Integer limit, Integer offset,
                             String sortBy, String sortOrder, String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRole(Role role, String roleId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoleBasicInfo(String roleId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoleBasicInfo(RoleBasicInfo roleBasicInfo, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preUpdateRoleName(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postUpdateRoleName(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preDeleteRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postDeleteRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preGetUserListOfRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetUserListOfRole(List<UserBasicInfo> userBasicInfoList, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preUpdateUserListOfRole(String roleId, List<String> newUserIDList, List<String> deletedUserIDList,
                                        String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postUpdateUserListOfRole(String roleId, List<String> newUserIDList, List<String> deletedUserIDList,
                                         String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preGetGroupListOfRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetGroupListOfRole(List<GroupBasicInfo> groupBasicInfoList, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preUpdateGroupListOfRole(String roleId, List<String> newGroupIDList, List<String> deletedGroupIDList,
                                         String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postUpdateGroupListOfRole(String roleId, List<String> newGroupIDList, List<String> deletedGroupIDList,
                                          String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preGetIdpGroupListOfRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetIdpGroupListOfRole(List<IdpGroup> idpGroups, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preUpdateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupIDList,
                                            List<IdpGroup> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postUpdateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupIDList,
                                             List<IdpGroup> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preGetPermissionListOfRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetPermissionListOfRole(List<Permission> permissionListOfRole, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preUpdatePermissionsForRole(String roleId, List<Permission> addedPermissions,
                                            List<Permission> deletedPermissions, String audience, String audienceId,
                                            String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postUpdatePermissionsForRole(String roleId, List<Permission> addedPermissions,
                                             List<Permission> deletedPermissions, String audience, String audienceId,
                                             String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRolesCount(String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRolesCount(int count, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoleListOfUser(String userId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoleListOfUser(List<RoleBasicInfo> roleBasicInfoList, String userId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoleListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoleListOfGroups(List<RoleBasicInfo> roleBasicInfoList, List<String> groupIds,
                                        String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoleListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoleListOfIdpGroups(List<RoleBasicInfo> roleBasicInfoList, List<String> groupIds,
                                           String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoleIdListOfUser(String userId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoleIdListOfUser(List<String> roleIds, String userId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoleIdListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoleIdListOfGroups(List<String> roleIds, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoleIdListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoleIdListOfIdpGroups(List<String> roleIds, List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preDeleteRolesByApplication(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postDeleteRolesByApplication(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

    }
}
