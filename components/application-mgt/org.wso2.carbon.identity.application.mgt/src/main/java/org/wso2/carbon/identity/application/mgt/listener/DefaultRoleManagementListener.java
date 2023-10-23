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

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementService;
import org.wso2.carbon.identity.application.mgt.AuthorizedAPIManagementServiceImpl;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.RoleManagementListener;
import org.wso2.carbon.identity.role.v2.mgt.core.model.GroupBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.IdpGroup;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.UserBasicInfo;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.ALLOWED_ROLE_AUDIENCE_PROPERTY_NAME;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.APPLICATION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_AUDIENCE;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_PERMISSION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;

/**
 * Default Role Management Listener implementation of Role Management V2 Listener.
 */
public class DefaultRoleManagementListener implements RoleManagementListener {

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
    public boolean preAddRole(String roleName, List<String> userList, List<String> groupList,
                              List<Permission> permissions, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (APPLICATION.equalsIgnoreCase(audience)) {
            validateApplicationRoleAudience(audienceId, tenantDomain);
            validatePermissionsForApplication(permissions, audienceId, tenantDomain);
        }
        return true;
    }

    @Override
    public boolean postAddRole(RoleBasicInfo roleBasicInfo, String roleName, List<String> userList,
                               List<String> groupList, List<Permission> permissions, String audience,
                               String audienceId, String tenantDomain) throws IdentityRoleManagementException {

        if (APPLICATION.equalsIgnoreCase(audience)) {
            roleBasicInfo.setAudienceName(getApplicationName(audienceId, tenantDomain));
        }
        return true;
    }

    @Override
    public boolean preGetRoles(Integer limit, Integer offset, String sortBy, String sortOrder,
                               String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRoles(List<RoleBasicInfo> roleBasicInfoList, Integer limit, Integer offset,
                                String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

        for (RoleBasicInfo roleBasicInfo : roleBasicInfoList) {
            if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
                roleBasicInfo.setAudienceName(getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain));
            }
        }
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

        for (RoleBasicInfo roleBasicInfo : roleBasicInfoList) {
            if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
                roleBasicInfo.setAudienceName(getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain));
            }
        }
        return true;
    }

    @Override
    public boolean preGetRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRole(Role role, String roleID, String tenantDomain) throws IdentityRoleManagementException {

        if (APPLICATION.equalsIgnoreCase(role.getAudience())) {
            role.setAudienceName(getApplicationName(role.getAudienceId(), tenantDomain));
        }
        return true;
    }

    @Override
    public boolean preGetRoleBasicInfo(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetRoleBasicInfo(RoleBasicInfo roleBasicInfo, String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
            roleBasicInfo.setAudienceName(getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain));
        }
        return true;
    }

    @Override
    public boolean preUpdateRoleName(String roleID, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postUpdateRoleName(String roleID, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preDeleteRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postDeleteRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetUserListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetUserListOfRole(List<UserBasicInfo> userBasicInfoList, String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preUpdateUserListOfRole(String roleID, List<String> newUserIDList, List<String> deletedUserIDList,
                                           String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postUpdateUserListOfRole(String roleID, List<String> newUserIDList, List<String> deletedUserIDList,
                                            String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetGroupListOfRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetGroupListOfRole(List<GroupBasicInfo> groupBasicInfoList, String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preUpdateGroupListOfRole(String roleID, List<String> newGroupIDList,
                                            List<String> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postUpdateGroupListOfRole(String roleID, List<String> newGroupIDList,
                                             List<String> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetIdpGroupListOfRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetIdpGroupListOfRole(List<IdpGroup> idpGroups, String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preUpdateIdpGroupListOfRole(String roleID, List<IdpGroup> newGroupIDList,
                                               List<IdpGroup> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postUpdateIdpGroupListOfRole(String roleID, List<IdpGroup> newGroupIDList,
                                                List<IdpGroup> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preGetPermissionListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postGetPermissionListOfRole(List<Permission> permissionListOfRole, String roleID,
                                               String tenantDomain) throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean preUpdatePermissionsForRole(String roleID, List<Permission> addedPermissions,
                                               List<Permission> deletedPermissions, String tenantDomain)
            throws IdentityRoleManagementException {

        return true;
    }

    @Override
    public boolean postUpdatePermissionsForRole(String roleID, List<Permission> addedPermissions,
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

        for (RoleBasicInfo roleBasicInfo : roleBasicInfoList) {
            if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
                roleBasicInfo.setAudienceName(getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain));
            }
        }
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

        for (RoleBasicInfo roleBasicInfo : roleBasicInfoList) {
            if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
                roleBasicInfo.setAudienceName(getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain));
            }
        }
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

        for (RoleBasicInfo roleBasicInfo : roleBasicInfoList) {
            if (APPLICATION.equalsIgnoreCase(roleBasicInfo.getAudience())) {
                roleBasicInfo.setAudienceName(getApplicationName(roleBasicInfo.getAudienceId(), tenantDomain));
            }
        }
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
    public boolean postGetRoleIdListOfIdpGroups(List<String> roleIds, List<String> groupIds,
                                                String tenantDomain) throws IdentityRoleManagementException {

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

    /**
     * Validate application role audience.
     *
     * @param applicationId Application ID.
     * @param tenantDomain Tenant domain.
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
            boolean valid = false;
            for (ServiceProviderProperty property : app.getSpProperties()) {
                // TODO :  use osgi service to get this
                if (ALLOWED_ROLE_AUDIENCE_PROPERTY_NAME.equals(property.getName()) &&
                        APPLICATION.equalsIgnoreCase(property.getValue())) {
                    valid = true;
                }
            }
            if (!valid) {
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
     * @param permissions Permissions.
     * @param applicationId Application ID.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException Error occurred while validating permissions.
     */
    private void validatePermissionsForApplication(List<Permission> permissions, String applicationId,
                                                   String tenantDomain)
            throws IdentityRoleManagementException {

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
     * @param appId App ID.
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
     * @param tenantDomain Tenant domain.
     * @return Application name.
     * @throws IdentityRoleManagementException Error occurred while retrieving application name.
     */
    private String getApplicationName(String applicationID, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            return ApplicationManagementService.getInstance()
                    .getApplicationBasicInfoByResourceId(applicationID, tenantDomain).getApplicationName();
        } catch (IdentityApplicationManagementException e) {
            String errorMessage = "Error while retrieving the application name for the given id: " + applicationID;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }
}
