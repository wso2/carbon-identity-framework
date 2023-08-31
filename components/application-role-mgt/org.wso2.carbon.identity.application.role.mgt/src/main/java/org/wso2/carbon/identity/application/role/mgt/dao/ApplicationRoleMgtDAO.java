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

package org.wso2.carbon.identity.application.role.mgt.dao;

import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementServerException;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;

import java.util.List;

/**
 * Application role DAO interface.
 */
public interface ApplicationRoleMgtDAO {

    /**
     * Add application role.
     *
     * @param applicationRole Application role.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementServerException Error occurred while adding application role.
     */
    ApplicationRole addApplicationRole(ApplicationRole applicationRole, String tenantDomain)
            throws ApplicationRoleManagementServerException;

    /**
     * Get application role by id.
     *
     * @param roleId Application roleId.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementServerException Error occurred while retrieving application role.
     */
    ApplicationRole getApplicationRoleById(String roleId, String tenantDomain)
            throws ApplicationRoleManagementServerException;

    /**
     * Get all application role by application id.
     *
     * @param applicationId Application roleId.
     * @throws ApplicationRoleManagementServerException Error occurred while retrieving all application role by app id.
     */
    List<ApplicationRole> getApplicationRoles(String applicationId) throws ApplicationRoleManagementServerException;

    /**
     * Update application role.
     *
     * @param roleId Application roleId.
     * @param newName New Application role  name.
     * @param addedScopes Scope to be added.
     * @param removedScopes Scope to be removed.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementServerException Error occurred while updating application role.
     */
    ApplicationRole updateApplicationRole(String roleId, String newName, List<String> addedScopes,
                                          List<String> removedScopes, String tenantDomain)
            throws ApplicationRoleManagementServerException;

    /**
     * Delete application role.
     *
     * @param roleId Application roleId.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementServerException Error occurred while deleting application role.
     */
    void deleteApplicationRole(String roleId, String tenantDomain) throws ApplicationRoleManagementServerException;

    /**
     * Check application role exists by name.
     *
     * @param applicationId Application id.
     * @param roleName Role name.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementServerException Error occurred while checking application role exists by name.
     */
    boolean isExistingRole(String applicationId, String roleName, String tenantDomain)
            throws ApplicationRoleManagementServerException;

    /**
     * Check application role exists by id.
     *
     * @param roleId Application role id.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementServerException Error occurred while checking application role exists by id.
     */
    boolean checkRoleExists(String roleId, String tenantDomain) throws ApplicationRoleManagementServerException;

    /**
     * Update application role assigned users.
     *
     * @param roleId Application roleId.
     * @param addedUsers Assigned users to be added.
     * @param removedUsers Assigned users to be removed.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementException Error occurred while updating application role assigned users.
     */
    ApplicationRole updateApplicationRoleAssignedUsers(String roleId, List<String> addedUsers,
                                                       List<String> removedUsers, String tenantDomain) throws
            ApplicationRoleManagementException;

    /**
     * Get application role assigned users.
     *
     * @param roleId Application roleId.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementException Error occurred while getting application role assigned users.
     */
    ApplicationRole getApplicationRoleAssignedUsers(String roleId, String tenantDomain)
            throws ApplicationRoleManagementException;

    /**
     * Update application role assigned groups.
     *
     * @param roleId Application roleId.
     * @param identityProvider Identity provider.
     * @param addedGroups Assigned groups to be added.
     * @param removedGroups Assigned groups to be removed.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementException Error occurred while updating application role assigned groups.
     */
    ApplicationRole updateApplicationRoleAssignedGroups(String roleId, IdentityProvider identityProvider,
                                                        List<String> addedGroups, List<String> removedGroups,
                                                        String tenantDomain)
            throws ApplicationRoleManagementException;

    /**
     * Get application role assigned groups.
     *
     * @param roleId Application roleId.
     * @param identityProvider Identity provider.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementException Error occurred while getting application role assigned groups.
     */
    ApplicationRole getApplicationRoleAssignedGroups(String roleId, IdentityProvider identityProvider,
                                                     String tenantDomain) throws ApplicationRoleManagementException;

    /**
     * Get application roles by userId.
     *
     * @param userId User Id.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementException Error occurred while getting application roles by userId.
     */
    List<ApplicationRole> getApplicationRolesByUserId(String userId, String tenantDomain)
            throws ApplicationRoleManagementException;

    /**
     * Get application roles by groupId.
     *
     * @param groupId Group Id.
     * @param tenantDomain Tenant domain.
     * @throws ApplicationRoleManagementException Error occurred while getting application roles by groupId.
     */
    List<ApplicationRole> getApplicationRolesByGroupId(String groupId, String tenantDomain)
            throws ApplicationRoleManagementException;
}
