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

package org.wso2.carbon.identity.role.v2.mgt.core;

import org.wso2.carbon.identity.role.mgt.core.GroupBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.UserBasicInfo;

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
    private List<Permission> permissions;
    private String audience;
    private String audienceId;
    private String audienceName;
    private List<AssociatedApplication> associatedApplications;

    public Role() {

    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDomain() {

        return domain;
    }

    public void setDomain(String domain) {

        this.domain = domain;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public List<UserBasicInfo> getUsers() {

        return users;
    }

    public void setUsers(List<UserBasicInfo> users) {

        this.users = users;
    }

    public List<GroupBasicInfo> getGroups() {

        return groups;
    }

    public void setGroups(List<GroupBasicInfo> groups) {

        this.groups = groups;
    }

    public List<Permission> getPermissions() {

        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {

        this.permissions = permissions;
    }

    public String getAudience() {

        return audience;
    }

    public void setAudience(String audience) {

        this.audience = audience;
    }

    public String getAudienceId() {

        return audienceId;
    }

    public void setAudienceId(String audienceId) {

        this.audienceId = audienceId;
    }

    public String getAudienceName() {

        return audienceName;
    }

    public void setAudienceName(String audienceName) {

        this.audienceName = audienceName;
    }

    public List<AssociatedApplication> getAssociatedApplications() {

        return associatedApplications;
    }

    public void setAssociatedApplications(List<AssociatedApplication> associatedApplications) {

        this.associatedApplications = associatedApplications;
    }
}
