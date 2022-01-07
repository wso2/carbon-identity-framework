/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.role.mgt.core;

import java.util.List;

/**
 * Represents the role.
 */
public class Role {

    private String id;
    private String name;
    private String domain;
    private String tenantDomain;
    private List<UserBasicInfo> users;
    private List<GroupBasicInfo> groups;
    private List<String> permissions;

    /**
     * Get the role Id.
     *
     * @return role Id.
     */
    public String getId() {

        return id;
    }

    /**
     * Set the role Id.
     *
     * @param id Role Id.
     */
    public void setId(String id) {

        this.id = id;
    }

    /**
     * Get the role name.
     *
     * @return Role name.
     */
    public String getName() {

        return name;
    }

    /**
     * Set the role name.
     *
     * @param name Role name.
     */
    public void setName(String name) {

        this.name = name;

    }

    /**
     * Get the role domain.
     *
     * @return Role domain.
     */
    public String getDomain() {

        return domain;
    }

    /**
     * Set the role domain.
     *
     * @param domain Role domain.
     */
    public void setDomain(String domain) {

        this.domain = domain;
    }

    /**
     * Get the tenant domain.
     *
     * @return Tenant domain.
     */
    public String getTenantDomain() {

        return tenantDomain;
    }

    /**
     * Set the Tenant domain.
     *
     * @param tenantDomain Tenant domain.
     */
    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    /**
     * Get the list of users.
     *
     * @return The list of users.
     */
    public List<UserBasicInfo> getUsers() {

        return users;
    }

    /**
     * Set the list of users.
     *
     * @param users The list of users.
     */
    public void setUsers(List<UserBasicInfo> users) {

        this.users = users;
    }

    /**
     * Get the list of groups.
     *
     * @return The list of groups.
     */
    public List<GroupBasicInfo> getGroups() {

        return groups;
    }

    /**
     * Set the list of groups.
     *
     * @param groups The list of groups.
     */
    public void setGroups(List<GroupBasicInfo> groups) {

        this.groups = groups;
    }

    /**
     * Get the list of permissions.
     *
     * @return The list of permissions.
     */
    public List<String> getPermissions() {

        return permissions;
    }

    /**
     * Set the list of permissions.
     *
     * @param permissions The list of permissions.
     */
    public void setPermissions(List<String> permissions) {

        this.permissions = permissions;
    }
}
