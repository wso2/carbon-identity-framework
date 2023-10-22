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

package org.wso2.carbon.identity.role.v2.mgt.core.model;

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
    private List<IdpGroup> idpGroups;
    private List<Permission> permissions;
    private String audience;
    private String audienceId;
    private String audienceName;
    private List<AssociatedApplication> associatedApplications;

    public Role() {

    }

    /**
     * Get the role id.
     *
     * @return Role id.
     */
    public String getId() {

        return id;
    }

    /**
     * Get the role id.
     *
     * @param id Role id.
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
     * Set the role nam.
     *
     * @param name Role name.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Get the domain.
     *
     * @return Domain.
     */
    public String getDomain() {

        return domain;
    }

    /**
     * Set the domain.
     *
     * @param domain Domain.
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
     * Set the tenant domain.
     *
     * @param tenantDomain Tenant domain.
     */
    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    /**
     * Get the users.
     *
     * @return Users.
     */
    public List<UserBasicInfo> getUsers() {

        return users;
    }

    /**
     * Set the users.
     *
     * @param users Users.
     */
    public void setUsers(List<UserBasicInfo> users) {

        this.users = users;
    }

    /**
     * Get the groups.
     *
     * @return Groups.
     */
    public List<GroupBasicInfo> getGroups() {

        return groups;
    }

    /**
     * Set the groups.
     *
     * @param groups Groups.
     */
    public void setGroups(List<GroupBasicInfo> groups) {

        this.groups = groups;
    }

    /**
     * Get the idp groups.
     *
     * @return Idp groups.
     */
    public List<IdpGroup> getIdpGroups() {

        return idpGroups;
    }

    /**
     * Set the idp groups.
     *
     * @param idpGroups Idp groups.
     */
    public void setIdpGroups(List<IdpGroup> idpGroups) {

        this.idpGroups = idpGroups;
    }

    /**
     * Get the permissions.
     *
     * @return Permissions.
     */
    public List<Permission> getPermissions() {

        return permissions;
    }

    /**
     * Set the permissions.
     *
     * @param permissions Permissions.
     */
    public void setPermissions(List<Permission> permissions) {

        this.permissions = permissions;
    }

    /**
     * Get the audience.
     *
     * @return Audience.
     */
    public String getAudience() {

        return audience;
    }

    /**
     * Set the audience.
     *
     * @param audience Audience.
     */
    public void setAudience(String audience) {

        this.audience = audience;
    }

    /**
     * Get the audience id.
     *
     * @return Audience Id.
     */
    public String getAudienceId() {

        return audienceId;
    }

    /**
     * Set the audience id.
     *
     * @param audienceId Audience Id.
     */
    public void setAudienceId(String audienceId) {

        this.audienceId = audienceId;
    }

    /**
     * Get the audience name.
     *
     * @return Audience Name.
     */
    public String getAudienceName() {

        return audienceName;
    }

    /**
     * Set the audience name.
     *
     * @param audienceName Audience Name.
     */
    public void setAudienceName(String audienceName) {

        this.audienceName = audienceName;
    }

    /**
     * Get the associated applications.
     *
     * @return Associated Applications.
     */
    public List<AssociatedApplication> getAssociatedApplications() {

        return associatedApplications;
    }

    /**
     * Set the associated applications.
     *
     * @param associatedApplications Associated Applications.
     */
    public void setAssociatedApplications(List<AssociatedApplication> associatedApplications) {

        this.associatedApplications = associatedApplications;
    }
}
