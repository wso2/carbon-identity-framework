/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.RoleManagementListener;
import org.wso2.carbon.identity.role.v2.mgt.core.model.GroupBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.IdpGroup;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.UserBasicInfo;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ORGANIZATION;

/**
 * Admin role listener to populate admin role permissions.
 */
public class AdminRoleListener implements RoleManagementListener {

    @Override
    public int getExecutionOrderId() {
        return 1;
    }

    @Override
    public int getDefaultOrderId() {
        return 1;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

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
    public void postGetRoles(List<Role> roleInfoList, Integer limit, Integer offset, String sortBy, String sortOrder,
                             String tenantDomain, List<String> requiredAttributes)
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
    public void postGetRoles(List<Role> roleInfoList, String filter, Integer limit, Integer offset, String sortBy,
                             String sortOrder, String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {
    }

    @Override
    public void preGetRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRole(Role role, String roleId, String tenantDomain) throws IdentityRoleManagementException {

        String consoleAdminRoleId = getAdministratorRoleId(tenantDomain);
        String adminRoleId = getAdminRoleId(tenantDomain);
        if (roleId.equals(consoleAdminRoleId) || roleId.equals(adminRoleId)) {
            try {
                List<Scope> systemScopes = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getSystemAPIScopes(tenantDomain);
                List<Permission> systemPermissions = systemScopes.stream().map(scope -> new Permission(scope.getName(),
                        scope.getDisplayName(), scope.getApiID())).collect(Collectors.toList());
                role.setPermissions(systemPermissions);
            } catch (APIResourceMgtException e) {
                throw new IdentityRoleManagementException("Error while retrieving internal scopes for tenant " +
                        "domain : " + tenantDomain, e);
            }
        }

        IdentityUtil.getProperty("EnableResidentIdpBanner");
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

        RoleManagementService roleManagementService = ApplicationManagementServiceComponentHolder.getInstance()
                .getRoleManagementServiceV2();
        Role role = roleManagementService.getRole(roleId, tenantDomain);
        String consoleAdminRoleId = getAdministratorRoleId(roleManagementService, tenantDomain);
        String adminRoleId = getAdminRoleId(tenantDomain);
        if (roleId.equals(consoleAdminRoleId) || roleId.equals(adminRoleId)) {
            try {
                List<Scope> systemScopes = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getSystemAPIScopes(tenantDomain);
                List<Permission> systemPermissions = systemScopes.stream().map(scope -> new Permission(scope.getName(),
                        scope.getDisplayName())).collect(Collectors.toList());
                role.setPermissions(systemPermissions);
            } catch (APIResourceMgtException e) {
                throw new IdentityRoleManagementException("Error while retrieving internal scopes for tenant " +
                        "domain : " + tenantDomain, e);
            }
        }
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

    public void postGetPermissionListOfRoles(List<String> permissions, List<String> roleIds, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleManagementService roleManagementService = ApplicationManagementServiceComponentHolder.getInstance()
                .getRoleManagementServiceV2();
        String consoleAdminRoleId = getAdministratorRoleId(roleManagementService, tenantDomain);
        String adminRoleId = getAdminRoleId(tenantDomain);
        if (roleIds.contains(consoleAdminRoleId) || roleIds.contains(adminRoleId)) {
            try {
                List<Scope> systemScopes = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getSystemAPIScopes(tenantDomain);
                permissions.addAll(systemScopes.stream().map(Scope::getName).collect(Collectors.toList()));
            } catch (APIResourceMgtException e) {
                throw new IdentityRoleManagementException("Error while retrieving internal scopes for tenant " +
                        "domain : " + tenantDomain, e);
            }
        }
    }

    private String getAdministratorRoleId(String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementService roleManagementService = ApplicationManagementServiceComponentHolder.getInstance()
                .getRoleManagementServiceV2();
        return getAdministratorRoleId(roleManagementService, tenantDomain);
    }

    private String getAdministratorRoleId(RoleManagementService roleManagementService, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
            String consoleAppId = applicationManagementService.getApplicationResourceIDByInboundKey("CONSOLE",
                    "oauth2", tenantDomain);
            return roleManagementService.getRoleIdByName(RoleConstants.ADMINISTRATOR,
                    RoleConstants.APPLICATION, consoleAppId, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new IdentityRoleManagementException("Error while retrieving Console application for tenant " +
                    "domain : " + tenantDomain, e);
        } catch (IdentityRoleManagementException e) {
            if (e.getMessage().contains(RoleConstants.ADMINISTRATOR) &&
                    e.getMessage().contains("A role doesn't exist")) {
                return null;
            }
            throw new IdentityRoleManagementException("Error while retrieving role id for admin role in " +
                    "tenant domain : " + tenantDomain, e);
        }
    }

    private String getAdminRoleId(String tenantDomain) throws IdentityRoleManagementException {

        try {
            String orgId = ApplicationManagementServiceComponentHolder.getInstance().getOrganizationManager()
                    .resolveOrganizationId(tenantDomain);
            if (StringUtils.isBlank(orgId)) {
                throw new IdentityRoleManagementException("Error while retrieving organization id from tenant " +
                        "domain : " + tenantDomain);
            }
            org.wso2.carbon.user.api.UserRealm realm = CarbonContext.getThreadLocalCarbonContext()
                    .getUserRealm();
            if (realm == null) {
                throw new IdentityRoleManagementException("Error while retrieving user realm");
            }
            String adminRoleName = realm.getRealmConfiguration().getAdminRoleName();
            if (StringUtils.isBlank(adminRoleName)) {
                throw new IdentityRoleManagementException("Admin role name not found");
            }
            return ApplicationManagementServiceComponentHolder.getInstance()
                    .getRoleManagementServiceV2().getRoleIdByName(UserCoreUtil.removeDomainFromName(adminRoleName),
                            ORGANIZATION, orgId, tenantDomain);
        } catch (IdentityRoleManagementException e) {
            throw new IdentityRoleManagementException("Error while retrieving role id for admin role in " +
                    "tenant domain : " + tenantDomain, e);
        } catch (OrganizationManagementException e) {
            throw new IdentityRoleManagementException("Error while retrieving organization id from tenant " +
                    "domain : " + tenantDomain, e);
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementException("Error while retrieving admin role name");
        }
    }
}
