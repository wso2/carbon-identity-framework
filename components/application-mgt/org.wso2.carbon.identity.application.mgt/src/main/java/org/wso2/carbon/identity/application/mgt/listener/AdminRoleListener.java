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
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.AbstractRoleManagementListener;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ORGANIZATION;

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
                role.setPermissions(systemPermissions);
            } catch (APIResourceMgtException e) {
                throw new IdentityRoleManagementException("Error while retrieving internal scopes for tenant " +
                        "domain : " + tenantDomain, e);
            }
        }
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

    private String getConsoleAdministratorRoleId(RoleManagementService roleManagementService, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            ApplicationManagementService applicationManagementService = ApplicationManagementService.getInstance();
            String consoleAppId = applicationManagementService.getApplicationResourceIDByInboundKey(
                    ApplicationConstants.CONSOLE_APPLICATION_CLIENT_ID,
                    ApplicationConstants.CONSOLE_APPLICATION_INBOUND_TYPE, tenantDomain);
            return roleManagementService.getRoleIdByName(RoleConstants.ADMINISTRATOR,
                    RoleConstants.APPLICATION, consoleAppId, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new IdentityRoleManagementException("Error while retrieving Console application for tenant " +
                    "domain : " + tenantDomain, e);
        } catch (IdentityRoleManagementException e) {
            // Handle Console application role not being available during the startup.
            if (e.getMessage().contains(RoleConstants.ADMINISTRATOR) &&
                    e.getMessage().contains("A role doesn't exist")) {
                return null;
            }
            throw new IdentityRoleManagementException("Error while retrieving role id for console Administrator role " +
                    "in tenant domain : " + tenantDomain, e);
        }
    }

    private String getOrgAdminRoleId(String tenantDomain) throws IdentityRoleManagementException {

        try {
            String orgId = ApplicationManagementServiceComponentHolder.getInstance().getOrganizationManager()
                    .resolveOrganizationId(tenantDomain);
            if (StringUtils.isBlank(orgId)) {
                throw new IdentityRoleManagementException("Error while retrieving organization id from tenant " +
                        "domain : " + tenantDomain);
            }
            String adminRoleName = getOrgAdminRoleName();
            return ApplicationManagementServiceComponentHolder.getInstance()
                    .getRoleManagementServiceV2().getRoleIdByName(UserCoreUtil.removeDomainFromName(adminRoleName),
                            ORGANIZATION, orgId, tenantDomain);
        } catch (IdentityRoleManagementException e) {
            if (e.getMessage().contains("A role doesn't exist")) {
                return null;
            }
            throw new IdentityRoleManagementException("Error while retrieving role id for admin role in " +
                    "tenant domain : " + tenantDomain, e);
        } catch (OrganizationManagementException e) {
            throw new IdentityRoleManagementException("Error while retrieving organization id from tenant " +
                    "domain : " + tenantDomain, e);
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
        String consoleAdminRoleId = getConsoleAdministratorRoleId(roleManagementService, tenantDomain);
        String adminRoleId = getOrgAdminRoleId(tenantDomain);
        if (roleId.equals(consoleAdminRoleId) || roleId.equals(adminRoleId)) {
            return true;
        }
        RoleBasicInfo role = roleManagementService.getRoleBasicInfoById(roleId, tenantDomain);
        if (StringUtils.equals(getOrgAdminRoleName(), (role.getName()))) {
            return role.getAudienceId().equals(tenantDomain);
        } else if (RoleConstants.ADMINISTRATOR.equals(role.getName())) {
            return role.getAudienceName().equals("Console");
        }
        return false;
    }
}
