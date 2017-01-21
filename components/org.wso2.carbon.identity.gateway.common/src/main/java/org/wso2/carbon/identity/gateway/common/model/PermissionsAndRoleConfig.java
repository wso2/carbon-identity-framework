/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.common.model;


import java.io.Serializable;

public class PermissionsAndRoleConfig implements Serializable {

    private static final long serialVersionUID = 784509684062361809L;

    private ApplicationPermission[] permissions = new ApplicationPermission[0];
    private RoleMapping[] roleMappings = new RoleMapping[0];
    private String[] idpRoles = new String[0];

        /**
     * @return
     */
    public ApplicationPermission[] getPermissions() {
        return permissions;
    }

    /**
     * @param permissions
     */
    public void setPermissions(ApplicationPermission[] permissions) {
        this.permissions = permissions;
    }

    /**
     * @return
     */
    public RoleMapping[] getRoleMappings() {
        return roleMappings;
    }

    /**
     * @param roleMappings
     */
    public void setRoleMappings(RoleMapping[] roleMappings) {
        this.roleMappings = roleMappings;
    }

    public String[] getIdpRoles() {
        return idpRoles;
    }

    public void setIdpRoles(String[] idpRoles) {
        this.idpRoles = idpRoles;
    }
}
