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
    public boolean preAddRole(String roleName, List<String> userList, List<String> groupList,
                              List<Permission> permissions, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postAddRole(RoleBasicInfo roleBasicInfo, String roleName, List<String> userList,
                               List<String> groupList, List<Permission> permissions, String audience, String audienceId,
                               String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetRoles(Integer limit, Integer offset, String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRoles(List<RoleBasicInfo> roleBasicInfoList, Integer limit, Integer offset, String sortBy,
                                String sortOrder, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
                               String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRoles(List<RoleBasicInfo> roleBasicInfoList, String filter, Integer limit, Integer offset,
                                String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRole(Role role, String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetRoleBasicInfo(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRoleBasicInfo(RoleBasicInfo roleBasicInfo, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preUpdateRoleName(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postUpdateRoleName(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preDeleteRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postDeleteRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetUserListOfRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetUserListOfRole(List<UserBasicInfo> userBasicInfoList, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preUpdateUserListOfRole(String roleId, List<String> newUserIDList, List<String> deletedUserIDList,
                                           String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postUpdateUserListOfRole(String roleId, List<String> newUserIDList, List<String> deletedUserIDList,
                                            String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetGroupListOfRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetGroupListOfRole(List<GroupBasicInfo> groupBasicInfoList, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preUpdateGroupListOfRole(String roleId, List<String> newGroupIDList, List<String> deletedGroupIDList,
                                            String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postUpdateGroupListOfRole(String roleId, List<String> newGroupIDList,
                                             List<String> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetIdpGroupListOfRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetIdpGroupListOfRole(List<IdpGroup> idpGroups, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preUpdateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupIDList,
                                               List<IdpGroup> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postUpdateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupIDList,
                                                List<IdpGroup> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetPermissionListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetPermissionListOfRole(List<Permission> permissionListOfRole, String roleId,
                                               String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preUpdatePermissionsForRole(String roleId, List<Permission> addedPermissions,
                                               List<Permission> deletedPermissions, String audience, String audienceId,
                                               String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postUpdatePermissionsForRole(String roleId, List<Permission> addedPermissions,
                                                List<Permission> deletedPermissions, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetRolesCount(String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRolesCount(int count, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetRoleListOfUser(String userId, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRoleListOfUser(List<RoleBasicInfo> roleBasicInfoList, String userId, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetRoleListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRoleListOfGroups(List<RoleBasicInfo> roleBasicInfoList, List<String> groupIds,
                                           String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetRoleListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRoleListOfIdpGroups(List<RoleBasicInfo> roleBasicInfoList, List<String> groupIds,
                                              String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetRoleIdListOfUser(String userId, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRoleIdListOfUser(List<String> roleIds, String userId, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetRoleIdListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRoleIdListOfGroups(List<String> roleIds, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetRoleIdListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRoleIdListOfIdpGroups(List<String> roleIds, List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preDeleteRolesByApplication(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postDeleteRolesByApplication(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }
}
