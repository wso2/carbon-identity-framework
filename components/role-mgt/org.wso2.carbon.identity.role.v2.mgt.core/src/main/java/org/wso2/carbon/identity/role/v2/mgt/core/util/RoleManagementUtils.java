/*
 * Copyright (c) 2025-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.role.v2.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleBasicInfoCache;
import org.wso2.carbon.identity.role.v2.mgt.core.dao.RoleDAO;
import org.wso2.carbon.identity.role.v2.mgt.core.dao.RoleMgtDAOFactory;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.v2.mgt.core.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ALLOW_SYSTEM_PREFIX_FOR_ROLES;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_AUDIENCE;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_PERMISSION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ORGANIZATION;

/**
 * Util class for role management functionality.
 */
public class RoleManagementUtils {

    private static final Log log = LogFactory.getLog(RoleManagementUtils.class);

    private static final RoleDAO roleDAO = RoleMgtDAOFactory.getInstance().getCacheBackedRoleDAO();

    /**
     * Checks whether the given role is an internal or application role.
     *
     * @param roleName Role name.
     * @return Whether the passed role is "internal" or "application".
     */
    public static boolean isHybridRole(String roleName) {

        return roleName.toLowerCase(Locale.ENGLISH).startsWith((RoleConstants.INTERNAL_DOMAIN +
                CarbonConstants.DOMAIN_SEPARATOR).toLowerCase(Locale.ENGLISH)) ||
                roleName.toLowerCase(Locale.ENGLISH).startsWith((RoleConstants.APPLICATION_DOMAIN +
                        CarbonConstants.DOMAIN_SEPARATOR).toLowerCase(Locale.ENGLISH));
    }

    /**
     * Resolve role audience ref id.
     *
     * @param audience   Audience.
     * @param audienceId Audience ID.
     * @return audience ref id.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    public static int resolveAudienceRefId(String audience, String audienceId) throws IdentityRoleManagementException {

        return roleDAO.getRoleAudienceRefId(audience, audienceId);
    }

    /**
     * Checks whether the given role is a shared role in the given tenant domain.
     *
     * @param roleId       The role ID.
     * @param tenantDomain The tenant domain.
     * @return Whether the role is a shared role or not.
     * @throws IdentityRoleManagementException If an error occurs while checking the shared role.
     */
    public static boolean isSharedRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.isSharedRole(roleId, tenantDomain);
    }

    /**
     * Get the configuration to allow adding roles with the system_ prefix. This config should be enabled only if
     * IS is used as a KM for APIM.
     *
     * @return True, if it is allowed to add roles with system_ prefix.
     */
    public static boolean isAllowSystemPrefixForRole() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(ALLOW_SYSTEM_PREFIX_FOR_ROLES));
    }

    /**
     * Get organization ID by tenantDomain.
     *
     * @param tenantDomain tenantDomain.
     * @throws IdentityRoleManagementException Error occurred while retrieving organization id.
     */
    public static String getOrganizationIdByTenantDomain(String tenantDomain) throws IdentityRoleManagementException {

        try {
            return RoleManagementServiceComponentHolder.getInstance().
                    getOrganizationManager().resolveOrganizationId(tenantDomain);

        } catch (OrganizationManagementException e) {
            String errorMessage = "Error while retrieving the organization id for the given tenantDomain: "
                    + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Validate organization role audience.
     *
     * @param audienceId               Audience ID.
     * @param roleCreationTenantDomain Role creation tenant domain.
     * @throws IdentityRoleManagementException Error occurred while validating organization role audience.
     */
    public static void validateOrganizationRoleAudience(String audienceId, String roleCreationTenantDomain)
            throws IdentityRoleManagementException {

        try {
            OrganizationManager organizationManager = RoleManagementServiceComponentHolder.getInstance().
                    getOrganizationManager();
            String orgIdOfTenantDomain = organizationManager.resolveOrganizationId(roleCreationTenantDomain);
            if (orgIdOfTenantDomain == null || !orgIdOfTenantDomain.equalsIgnoreCase(audienceId)) {
                throw new IdentityRoleManagementClientException(INVALID_AUDIENCE.getCode(),
                        "Invalid audience. Given Organization id: " + audienceId + " is invalid");
            }
            if (!organizationManager.isOrganizationExistById(audienceId)) {
                throw new IdentityRoleManagementClientException(INVALID_AUDIENCE.getCode(),
                        "Invalid audience. No organization found with organization id: " + audienceId);
            }
        } catch (OrganizationManagementException e) {
            String errorMessage = "Error while checking the organization exist by id : " + audienceId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Validate permissions for organization audience.
     *
     * @param permissions Permissions.
     * @throws IdentityRoleManagementException Error occurred while validating permissions.
     */
    public static void validatePermissionsForOrganization(List<Permission> permissions, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            List<Scope> scopes = RoleManagementServiceComponentHolder.getInstance().getApiResourceManager().
                    getScopesByTenantDomain(tenantDomain, "");
            Set<String> scopeNameList = new HashSet<>();
            for (Scope scope : scopes) {
                scopeNameList.add(scope.getName());
            }
            for (Permission permission : permissions) {
                if (!scopeNameList.contains(permission.getName())) {
                    throw new IdentityRoleManagementClientException(INVALID_PERMISSION.getCode(),
                            "Permission: " + permission.getName() + " not found");
                }
            }
        } catch (APIResourceMgtException e) {
            throw new IdentityRoleManagementException("Error while retrieving scopes", "Error while retrieving scopes "
                    + "for tenantDomain: " + tenantDomain, e);
        }
    }

    /**
     * Validate permissions.
     *
     * @param permissions  Permissions.
     * @param audience     Audience.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException Error occurred while validating permissions.
     */
    public static void validatePermissions(List<Permission> permissions, String audience, String tenantDomain)
            throws IdentityRoleManagementException {

        if (audience.equals(ORGANIZATION)) {
            RoleManagementUtils.validatePermissionsForOrganization(permissions, tenantDomain);
        }
    }

    /**
     * Get organization id by tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @return organization id.
     * @throws IdentityRoleManagementServerException IdentityRoleManagementServerException.
     */
    public static String getOrganizationId(String tenantDomain) throws IdentityRoleManagementServerException {

        try {
            return RoleManagementServiceComponentHolder.getInstance().getOrganizationManager()
                    .resolveOrganizationId(tenantDomain);
        } catch (OrganizationManagementException e) {
            String errorMessage = "Error while retrieving the organization id for the tenant domain: " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Remove internal domain.
     *
     * @param roleName Role name.
     * @return Domain removed role name.
     */
    public static String removeInternalDomain(String roleName) {

        if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(IdentityUtil.extractDomainFromName(roleName))) {
            return UserCoreUtil.removeDomainFromName(roleName);
        }
        return roleName;
    }

    /**
     * Get everyone role name by tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @return every one role name.
     * @throws IdentityRoleManagementException if error occurred while retrieving everyone role name.
     */
    public static String getEveryOneRoleName(String tenantDomain) throws IdentityRoleManagementException {

        String everyOneRoleName;
        try {
            everyOneRoleName = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
                    .getEveryOneRoleName();
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementException("Error while retrieving everyone role name", e);
        }
        if (everyOneRoleName == null) {
            String errorMessage =
                    "Everyone role name not found for tenantDomain : " + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage);
        }
        return removeInternalDomain(everyOneRoleName);
    }

    /**
     * Clear all role basic info cache entries for a tenant.
     * This is useful when application names change, which affects the audience name in cached role info.
     *
     * @param tenantDomain Tenant domain.
     */
    public static void clearRoleBasicInfoCacheByTenant(String tenantDomain) {

        RoleBasicInfoCache.getInstance().clear(tenantDomain);
    }
}
