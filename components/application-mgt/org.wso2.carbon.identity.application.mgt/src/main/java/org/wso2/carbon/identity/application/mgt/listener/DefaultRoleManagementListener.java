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

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.collections.CollectionUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementServiceImpl;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCache;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCacheKey;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderByIDCache;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderByResourceIdCache;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderIDCacheKey;
import org.wso2.carbon.identity.application.mgt.internal.cache.ServiceProviderResourceIdCacheKey;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.RoleManagementListener;
import org.wso2.carbon.identity.role.v2.mgt.core.model.AssociatedApplication;
import org.wso2.carbon.identity.role.v2.mgt.core.model.GroupBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.IdpGroup;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.UserBasicInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.APPLICATION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_AUDIENCE;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_PERMISSION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;

/**
 * Default Role Management Listener implementation of Role Management V2 Listener,
 * and application based role management.
 */
public class DefaultRoleManagementListener extends AbstractApplicationMgtListener implements RoleManagementListener {

    private static final AuthorizedAPIManagementService authorizedAPIManagementService =
            new AuthorizedAPIManagementServiceImpl();

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
    public void preAddRole(String roleName, List<String> userList, List<String> groupList,
                           List<Permission> permissions, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (APPLICATION.equalsIgnoreCase(audience)) {
            validateApplicationRoleAudience(audienceId, tenantDomain);
            validatePermissionsForApplication(permissions, audienceId, tenantDomain);
        }
    }

    @Override
    public void postAddRole(RoleBasicInfo roleBasicInfo, String roleName, List<String> userList,
                            List<String> groupList, List<Permission> permissions, String audience,
                            String audienceId, String tenantDomain) throws IdentityRoleManagementException {

        if (APPLICATION.equalsIgnoreCase(audience)) {
            // Set audience name by application name.
            roleBasicInfo.setAudienceName(getApplicationName(audienceId, tenantDomain));
            clearApplicationCaches(audienceId, tenantDomain);
        }
    }

    @Override
    public void preGetRoles(Integer limit, Integer offset, String sortBy, String sortOrder,
                            String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preGetRoles(Integer limit, Integer offset, String sortBy, String sortOrder, String tenantDomain,
                            List<String> requiredAttributes) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoles(List<RoleBasicInfo> roleBasicInfoList, Integer limit, Integer offset,
                             String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

        Iterator<RoleBasicInfo> iterator = roleBasicInfoList.iterator();
        while (iterator.hasNext()) {
            RoleBasicInfo roleBasicInfo = iterator.next();
            if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
                String applicationName = getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain);
                if (applicationName == null) {
                    iterator.remove();
                }
                roleBasicInfo.setAudienceName(applicationName);
            }
        }
    }

    @Override
    public void postGetRoles(List<Role> roleInfoList, Integer limit, Integer offset,
                             String sortBy, String sortOrder, String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

        Iterator<Role> iterator = roleInfoList.iterator();
        while (iterator.hasNext()) {
            Role roleInfo = iterator.next();
            if (APPLICATION.equalsIgnoreCase(roleInfo.getAudience())) {
                String applicationName = getApplicationName(roleInfo.getAudienceId(), tenantDomain);
                if (applicationName == null) {
                    iterator.remove();
                }
                roleInfo.setAudienceName(applicationName);
            }
        }
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

        for (RoleBasicInfo roleBasicInfo : roleBasicInfoList) {
            if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
                roleBasicInfo.setAudienceName(getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain));
            }
        }
    }

    @Override
    public void postGetRoles(List<Role> roleInfoList, String filter, Integer limit, Integer offset,
                             String sortBy, String sortOrder, String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

        for (Role roleInfo : roleInfoList) {
            if (APPLICATION.equalsIgnoreCase(roleInfo.getAudience())) {
                roleInfo.setAudienceName(getApplicationName(roleInfo.getAudienceId(), tenantDomain));
            }
        }
    }

    @Override
    public void preGetRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRole(Role role, String roleID, String tenantDomain) throws IdentityRoleManagementException {

        if (APPLICATION.equalsIgnoreCase(role.getAudience())) {
            role.setAudienceName(getApplicationName(role.getAudienceId(), tenantDomain));
        }
    }

    @Override
    public void preGetRoleBasicInfo(String roleID, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoleBasicInfo(RoleBasicInfo roleBasicInfo, String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
            roleBasicInfo.setAudienceName(getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain));
        }
    }

    @Override
    public void preUpdateRoleName(String roleID, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            Role role = ApplicationManagementServiceComponentHolder.getInstance().getRoleManagementServiceV2()
                    .getRole(roleID, tenantDomain);
            List<AssociatedApplication> associatedApplications = role.getAssociatedApplications();
            for (AssociatedApplication application : associatedApplications) {
                clearApplicationCaches(application.getId(), tenantDomain);
            }
        } catch (IdentityRoleManagementException e) {
            throw new IdentityRoleManagementException(
                    String.format("Error occurred while updating the name of role : %s in tenant domain : %s",
                            roleID, tenantDomain), e);
        }

    }

    @Override
    public void postUpdateRoleName(String roleID, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preDeleteRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            Role role = ApplicationManagementServiceComponentHolder.getInstance().getRoleManagementServiceV2()
                    .getRole(roleID, tenantDomain);
            List<AssociatedApplication> associatedApplications = role.getAssociatedApplications();
            for (AssociatedApplication application : associatedApplications) {
                clearApplicationCaches(application.getId(), tenantDomain);
            }
        } catch (IdentityRoleManagementException e) {
            throw new IdentityRoleManagementException(
                    String.format("Error occurred while deleting role : %s and tenant domain : %s",
                            roleID, tenantDomain), e);
        }
    }

    @Override
    public void postDeleteRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preGetUserListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postGetUserListOfRole(List<UserBasicInfo> userBasicInfoList, String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preUpdateUserListOfRole(String roleID, List<String> newUserIDList, List<String> deletedUserIDList,
                                        String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postUpdateUserListOfRole(String roleID, List<String> newUserIDList, List<String> deletedUserIDList,
                                         String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preGetGroupListOfRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetGroupListOfRole(List<GroupBasicInfo> groupBasicInfoList, String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preUpdateGroupListOfRole(String roleID, List<String> newGroupIDList,
                                         List<String> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postUpdateGroupListOfRole(String roleID, List<String> newGroupIDList,
                                          List<String> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preGetIdpGroupListOfRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void postGetIdpGroupListOfRole(List<IdpGroup> idpGroups, String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preUpdateIdpGroupListOfRole(String roleID, List<IdpGroup> newGroupIDList,
                                            List<IdpGroup> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postUpdateIdpGroupListOfRole(String roleID, List<IdpGroup> newGroupIDList,
                                             List<IdpGroup> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void preGetPermissionListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postGetPermissionListOfRole(List<Permission> permissionListOfRole, String roleID,
                                            String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preUpdatePermissionsForRole(String roleID, List<Permission> addedPermissions,
                                            List<Permission> deletedPermissions, String audience, String audienceId,
                                            String tenantDomain)
            throws IdentityRoleManagementException {

        if (APPLICATION.equalsIgnoreCase(audience)) {
            validatePermissionsForApplication(addedPermissions, audienceId, tenantDomain);
        }

    }

    @Override
    public void postUpdatePermissionsForRole(String roleID, List<Permission> addedPermissions,
                                             List<Permission> deletedPermissions, String audience, String audienceId,
                                             String tenantDomain)
            throws IdentityRoleManagementException {

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

        for (RoleBasicInfo roleBasicInfo : roleBasicInfoList) {
            if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
                roleBasicInfo.setAudienceName(getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain));
            }
        }

    }

    @Override
    public void preGetRoleListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoleListOfGroups(List<RoleBasicInfo> roleBasicInfoList, List<String> groupIds,
                                        String tenantDomain) throws IdentityRoleManagementException {

        for (RoleBasicInfo roleBasicInfo : roleBasicInfoList) {
            if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
                roleBasicInfo.setAudienceName(getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain));
            }
        }

    }

    @Override
    public void preGetRoleListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postGetRoleListOfIdpGroups(List<RoleBasicInfo> roleBasicInfoList, List<String> groupIds,
                                           String tenantDomain) throws IdentityRoleManagementException {

        for (RoleBasicInfo roleBasicInfo : roleBasicInfoList) {
            if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
                roleBasicInfo.setAudienceName(getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain));
            }
        }

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
    public void postGetRoleIdListOfIdpGroups(List<String> roleIds, List<String> groupIds,
                                             String tenantDomain) throws IdentityRoleManagementException {

    }

    @Override
    public void preDeleteRolesByApplication(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    @Override
    public void postDeleteRolesByApplication(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

    }

    /**
     * Validate application role audience.
     *
     * @param applicationId Application ID.
     * @param tenantDomain  Tenant domain.
     * @throws IdentityRoleManagementException Error occurred while validating application role audience.
     */
    private void validateApplicationRoleAudience(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            ServiceProvider app = ApplicationManagementService.getInstance()
                    .getApplicationByResourceId(applicationId, tenantDomain);
            if (app == null) {
                throw new IdentityRoleManagementClientException(INVALID_AUDIENCE.getCode(),
                        "Invalid audience. No application found with application id: " + applicationId +
                                " and tenant domain : " + tenantDomain);
            }
            String allowedAudienceForRoleAssociation = ApplicationManagementService.getInstance()
                    .getAllowedAudienceForRoleAssociation(app.getApplicationResourceId(), tenantDomain);
            if (!APPLICATION.equalsIgnoreCase(allowedAudienceForRoleAssociation.toLowerCase())) {
                throw new IdentityRoleManagementClientException(INVALID_AUDIENCE.getCode(),
                        "Application: " + applicationId + " does not have Application role audience type");
            }
        } catch (IdentityApplicationManagementException e) {
            String errorMessage = "Error while retrieving the application for the given id: " + applicationId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Validate permissions for application audience.
     *
     * @param permissions   Permissions.
     * @param applicationId Application ID.
     * @param tenantDomain  Tenant domain.
     * @throws IdentityRoleManagementException Error occurred while validating permissions.
     */
    private void validatePermissionsForApplication(List<Permission> permissions, String applicationId,
                                                   String tenantDomain)
            throws IdentityRoleManagementException {

        if (CollectionUtils.isEmpty(permissions)) {
            return;
        }
        List<String> authorizedScopes = getAuthorizedScopes(applicationId, tenantDomain);
        for (Permission permission : permissions) {
            if (!authorizedScopes.contains(permission.getName())) {
                throw new IdentityRoleManagementClientException(INVALID_PERMISSION.getCode(),
                        "Permission : " + permission.getName() + " is not authorized for application " +
                                "with ID: " + applicationId);
            }
        }
    }

    /**
     * Get authorized scopes by app ID.
     *
     * @param appId        App ID.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException Error occurred while retrieving authorized scopes by app ID.
     */
    private List<String> getAuthorizedScopes(String appId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<AuthorizedScopes> authorizedScopesList;
        try {
            authorizedScopesList = authorizedAPIManagementService.getAuthorizedScopes(appId, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new IdentityRoleManagementException("Error while retrieving authorized scopes.",
                    "Error while retrieving authorized scopes for app id : " + appId, e);
        }
        if (authorizedScopesList == null) {
            return new ArrayList<>();
        }
        List<String> allScopes = new ArrayList<>();
        for (AuthorizedScopes authorizedScopes : authorizedScopesList) {
            List<String> scopes = authorizedScopes.getScopes();
            allScopes.addAll(scopes);
        }
        return allScopes;
    }

    /**
     * Get application name.
     *
     * @param applicationID Application ID.
     * @param tenantDomain  Tenant domain.
     * @return Application name.
     * @throws IdentityRoleManagementException Error occurred while retrieving application name.
     */
    private String getApplicationName(String applicationID, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            ApplicationBasicInfo appBasicInfo = ApplicationManagementService.getInstance()
                    .getApplicationBasicInfoByResourceId(applicationID, tenantDomain);
            return (appBasicInfo != null) ? appBasicInfo.getApplicationName() : null;
        } catch (IdentityApplicationManagementException e) {
            String errorMessage = "Error while retrieving the application name for the given id: " + applicationID;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Get application basic information.
     *
     * @param applicationID Application ID.
     * @param tenantDomain  Tenant domain.
     * @return Basic information of the application.
     * @throws IdentityRoleManagementServerException When an error occurred while retrieving application basic 
     *                                               information.
     */
    private ApplicationBasicInfo getApplicationBasicInfo(String applicationID, String tenantDomain) 
            throws IdentityRoleManagementServerException {

        try {
            return ApplicationManagementService.getInstance()
                    .getApplicationBasicInfoByResourceId(applicationID, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            String errorMessage = "Error while retrieving the basic information for the given app id: " + applicationID;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Clears the ServiceProviderByIDCache, ServiceProviderByResourceIdCache and IdentityServiceProviderCache.
     * 
     * @param appId Application ID.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException When an error occurred while retrieving the basic information of the 
     *                                         application, or if the basic info of the application is null.
     */
    private void clearApplicationCaches(String appId, String tenantDomain) throws IdentityRoleManagementException {

        ApplicationBasicInfo appBasicInfo = getApplicationBasicInfo(appId, tenantDomain);
        if (appBasicInfo == null) {
            throw new IdentityRoleManagementClientException(UNEXPECTED_SERVER_ERROR.getCode(), "Error while " +
                    "retrieving the application information for the given application id: " + appId);
        }
        
        ServiceProviderResourceIdCacheKey resourceIdKey = new ServiceProviderResourceIdCacheKey(appId);
        ServiceProviderByResourceIdCache.getInstance().clearCacheEntry(resourceIdKey, tenantDomain);
        
        ServiceProviderIDCacheKey appIdKey = new ServiceProviderIDCacheKey(appBasicInfo.getApplicationId());
        ServiceProviderByIDCache.getInstance().clearCacheEntry(appIdKey, tenantDomain);
        
        IdentityServiceProviderCacheKey appNameKey = new IdentityServiceProviderCacheKey(
                appBasicInfo.getApplicationName());
        IdentityServiceProviderCache.getInstance().clearCacheEntry(appNameKey, tenantDomain);
    }

    @Override
    public boolean doPostDeleteApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        try {
            ApplicationManagementServiceComponentHolder.getInstance().getRoleManagementServiceV2()
                    .deleteRolesByApplication(serviceProvider.getApplicationResourceId(), tenantDomain);
        } catch (IdentityRoleManagementException e) {
            throw new IdentityApplicationManagementException(
                    String.format("Error occurred while deleting roles created for the application: %s.",
                            serviceProvider.getApplicationName()), e);
        }
        return true;
    }
}
