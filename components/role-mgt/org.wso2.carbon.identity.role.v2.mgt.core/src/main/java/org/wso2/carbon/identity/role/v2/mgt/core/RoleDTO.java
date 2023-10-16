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

/**
 * Represents the role dto.
 */
public class RoleDTO {

    private String name;
    private String id;
    private int audienceRefId;
    private int tenantId;
    private RoleAudience roleAudience;

    public RoleDTO(String name, int audienceRefId) {

        this.name = name;
        this.audienceRefId = audienceRefId;
    }

    public RoleDTO(String name, String id, int audienceRefId, int tenantId) {

        this.name = name;
        this.id = id;
        this.audienceRefId = audienceRefId;
        this.tenantId = tenantId;
    }

    public RoleDTO(String name, int audienceRefId, int tenantId) {

        this.name = name;
        this.audienceRefId = audienceRefId;
        this.tenantId = tenantId;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public int getAudienceRefId() {

        return audienceRefId;
    }

    public void setAudienceRefId(int audienceRefId) {

        this.audienceRefId = audienceRefId;
    }

    public RoleAudience getRoleAudience() {

        return roleAudience;
    }

    public void setRoleAudience(RoleAudience roleAudience) {

        this.roleAudience = roleAudience;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }
}
