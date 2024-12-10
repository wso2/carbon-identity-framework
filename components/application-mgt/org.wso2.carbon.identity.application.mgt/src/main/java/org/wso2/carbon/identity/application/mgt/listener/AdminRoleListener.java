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
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.AbstractRoleManagementListener;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.CONSOLE_ORG_SCOPE_PREFIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.INTERNAL_ORG_SCOPE_PREFIX;

/**
 * Admin role listener to populate organization admin role and console application Administrator role permissions.
 */
public class AdminRoleListener extends AbstractRoleManagementListener {

    @Override
    public int getExecutionOrderId() {
        return 2;
    }

    @Override
    public int getDefaultOrderId() {
        return 2;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void postGetRole(Role role, String roleId, String tenantDomain) throws IdentityRoleManagementException {

        if (isAdminRole(roleId, tenantDomain)) {
            try {
                List<Scope> systemScopes = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getSystemAPIScopes(tenantDomain);
                List<Permission> systemPermissions = systemScopes.stream().map(scope -> new Permission(scope.getName(),
                        scope.getDisplayName(), scope.getApiID())).collect(Collectors.toList());
                if (OrganizationManagementUtil.isOrganization(tenantDomain)) {
                    // Removing the scopes which are not valid for sub organization context.
                    systemPermissions =
                            systemPermissions.stream().filter(permission ->
                                    isValidSubOrgPermission(permission.getName())).collect(Collectors.toList());
                }
                role.setPermissions(systemPermissions);
            } catch (APIResourceMgtException e) {
                throw new IdentityRoleManagementException("Error while retrieving internal scopes for tenant " +
                        "domain : " + tenantDomain, e);
            } catch (OrganizationManagementException e) {
                throw new IdentityRoleManagementException("Error while retrieving context for the sub org: " +
                        tenantDomain, e);
            }
        }
    }

    private boolean isValidSubOrgPermission(String permission) {

        return (permission.startsWith(INTERNAL_ORG_SCOPE_PREFIX) || permission.startsWith(CONSOLE_ORG_SCOPE_PREFIX));
    }

    @Override
    public void postGetPermissionListOfRole(List<Permission> permissionListOfRole, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (isAdminRole(roleId, tenantDomain)) {
            try {
                List<Scope> systemScopes = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getSystemAPIScopes(tenantDomain);
                List<Permission> systemPermissions = systemScopes.stream().map(scope -> new Permission(scope.getName(),
                        scope.getDisplayName())).collect(Collectors.toList());
                permissionListOfRole.addAll(systemPermissions);
            } catch (APIResourceMgtException e) {
                throw new IdentityRoleManagementException("Error while retrieving internal scopes for tenant " +
                        "domain : " + tenantDomain, e);
            }
        }
    }

    public void postGetPermissionListOfRoles(List<String> permissions, List<String> roleIds, String tenantDomain)
            throws IdentityRoleManagementException {

        boolean isAdminRole = false;
        for (String roleId : roleIds) {
            if (isAdminRole(roleId, tenantDomain)) {
                isAdminRole = true;
                break;
            }
        }
        if (isAdminRole) {
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

    private static String getOrgAdminRoleName() throws IdentityRoleManagementException {

        org.wso2.carbon.user.api.UserRealm realm = CarbonContext.getThreadLocalCarbonContext()
                .getUserRealm();
        if (realm == null) {
            throw new IdentityRoleManagementException("Error while retrieving user realm");
        }
        try {
            String adminRoleName = realm.getRealmConfiguration().getAdminRoleName();
            if (StringUtils.isBlank(adminRoleName)) {
                throw new IdentityRoleManagementException("Admin role name not found");
            }
            if (adminRoleName.contains("/")) {
                adminRoleName = adminRoleName.split("/")[1];
            }
            return adminRoleName;
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementException("Error while retrieving admin role name");
        }

    }

    private boolean isAdminRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementService roleManagementService = ApplicationManagementServiceComponentHolder.getInstance()
                .getRoleManagementServiceV2();
        RoleBasicInfo role = roleManagementService.getRoleBasicInfoById(roleId, tenantDomain);
        if (StringUtils.equals(getOrgAdminRoleName(), (role.getName())) &&
                role.getAudience().equals(RoleConstants.ORGANIZATION)) {
            return true;
        }
        if (RoleConstants.ADMINISTRATOR.equals(role.getName()) &&
                role.getAudienceName().equals(ApplicationConstants.CONSOLE_APPLICATION_NAME)) {
            return true;
        }
        return false;
    }
}
