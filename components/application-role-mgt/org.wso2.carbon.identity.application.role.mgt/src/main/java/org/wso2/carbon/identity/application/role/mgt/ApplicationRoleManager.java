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

package org.wso2.carbon.identity.application.role.mgt;

import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;

import java.util.List;

/**
 * Application Role Manager.
 */
public interface ApplicationRoleManager {

    /**
     * Add application role.
     *
     * @param applicationRole Application role.
     * @throws ApplicationRoleManagementException Error occurred while adding application role.
     */
    void addApplicationRole(ApplicationRole applicationRole) throws ApplicationRoleManagementException;

    /**
     * Update application role.
     *
     * @param roleId role Id.
     * @param newName new role name.
     * @param addedScopes List of scopes to be added.
     * @param removedScopes List of scopes to be removed.
     * @throws ApplicationRoleManagementException Error occurred while updating the application role.
     */
    ApplicationRole updateApplicationRole(String applicationId, String roleId, String newName, List<String> addedScopes,
                               List<String> removedScopes) throws ApplicationRoleManagementException;

    /**
     * Get the application role by role id.
     *
     * @param roleId Role id.
     * @return Application role.
     * @throws ApplicationRoleManagementException Error occurred while retrieving the application role.
     */
    ApplicationRole getApplicationRoleById(String roleId) throws ApplicationRoleManagementException;

    /**
     * Get all the application roles by application id.
     *
     * @param applicationId Application id.
     * @return Application roles.
     * @throws ApplicationRoleManagementException Error occurred while retrieving the application roles of a given app.
     */
    List<ApplicationRole> getApplicationRoles(String applicationId) throws ApplicationRoleManagementException;

    /**
     * Delete application role.
     *
     * @param roleId Role id.
     * @throws ApplicationRoleManagementException Error occurred while deleting the application role.
     */
    void deleteApplicationRole(String roleId) throws ApplicationRoleManagementException;


    /**
     * Update the list of assigned users for an application role.
     *
     * @param roleId Application role ID.
     * @param addedUsers List of user IDs to be assigned.
     * @param removedUsers List of user IDs to be unassigned.
     * @throws ApplicationRoleManagementException Error occurred while updating the application role.
     */
    ApplicationRole updateApplicationRoleAssignedUsers(String roleId, List<String> addedUsers,
                                                       List<String> removedUsers)
            throws ApplicationRoleManagementException;

    /**
     * Get the list of assigned users of an application role.
     *
     * @param roleId Application role ID.
     * @throws ApplicationRoleManagementException Error occurred while updating the application role.
     */
    ApplicationRole getApplicationRoleAssignedUsers(String roleId) throws ApplicationRoleManagementException;

    /**
     * Update the list of assigned groups for an application role.
     *
     * @param roleId Application role ID.
     * @param addedGroups List of group IDs to be assigned.
     * @param removedGroups List of group IDs to be unassigned.
     * @throws ApplicationRoleManagementException Error occurred while updating the application role.
     */
    ApplicationRole updateApplicationRoleAssignedGroups(String roleId, String idpId, List<String> addedGroups,
                                             List<String> removedGroups) throws ApplicationRoleManagementException;

    /**
     * Get the list of assigned groups of an application role.
     *
     * @param roleId Application role ID.
     * @throws ApplicationRoleManagementException Error occurred while updating the application role.
     */
    ApplicationRole getApplicationRoleAssignedGroups(String roleId, String idpId)
            throws ApplicationRoleManagementException;

    /**
     * Get the list of application roles of a user.
     *
     * @param userId user ID.
     * @param tenantDomain tenant domain.
     * @throws ApplicationRoleManagementException Error occurred while updating the application role.
     */
    List<ApplicationRole> getApplicationRolesByUserId(String userId, String tenantDomain)
            throws ApplicationRoleManagementException;

    /**
     * Get the list of application roles of a group.
     *
     * @param groupId group ID.
     * @param tenantDomain tenant domain.
     * @throws ApplicationRoleManagementException Error occurred while updating the application role.
     */
    List<ApplicationRole> getApplicationRolesByGroupId(String groupId, String tenantDomain)
            throws ApplicationRoleManagementException;


}
