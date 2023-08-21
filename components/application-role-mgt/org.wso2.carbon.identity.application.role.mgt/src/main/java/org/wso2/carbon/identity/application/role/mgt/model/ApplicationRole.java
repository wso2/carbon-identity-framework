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

package org.wso2.carbon.identity.application.role.mgt.model;

import java.util.List;

/**
 * Application role model.
 */
public class ApplicationRole {

    private String roleId;
    private String roleName;
    private String[] permissions;
    private String applicationId;
    private List<User> assignedUsers;
    private List<User> assignedGroups;

    public ApplicationRole(String roleId, String roleName, String[] permissions, String applicationId) {

        this.roleId = roleId;
        this.roleName = roleName;
        this.permissions = permissions;
        this.applicationId = applicationId;
    }

    public ApplicationRole(String roleId, String roleName, String applicationId) {

        this.roleId = roleId;
        this.roleName = roleName;
        this.applicationId = applicationId;
    }

    public ApplicationRole(String roleName, String[] permissions, String applicationId) {

        this.roleName = roleName;
        this.permissions = permissions;
        this.applicationId = applicationId;
    }

    public ApplicationRole(String roleName, String applicationId) {

        this.roleName = roleName;
        this.applicationId = applicationId;
    }

    public ApplicationRole(String roleId) {

        this.roleId = roleId;
    }

    public String getRoleId() {

        return roleId;
    }

    public void setRoleId(String roleId) {

        this.roleId = roleId;
    }

    public String getRoleName() {

        return roleName;
    }

    public void setRoleName(String roleName) {

        this.roleName = roleName;
    }

    public String[] getPermissions() {

        return permissions;
    }

    public void setPermissions(String[] permissions) {

        this.permissions = permissions;
    }

    public String getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(String applicationId) {

        this.applicationId = applicationId;
    }

    public List<User> getAssignedUsers() {

        return assignedUsers;
    }

    public void setAssignedUsers(List<User> assignedUsers) {

        this.assignedUsers = assignedUsers;
    }

    public List<User> getAssignedGroups() {

        return assignedGroups;
    }

    public void setAssignedGroups(List<User> assignedGroups) {

        this.assignedGroups = assignedGroups;
    }
}
