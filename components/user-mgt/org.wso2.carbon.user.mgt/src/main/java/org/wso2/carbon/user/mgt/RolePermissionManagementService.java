/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.mgt;

import org.json.JSONArray;
import org.wso2.carbon.user.mgt.common.UserAdminException;

/**
 * OSGi service interface which use to manage role permissions.
 */
public interface RolePermissionManagementService {

    /**
     * Get permissions of a role.
     *
     * @param roleName role name.
     * @param tenantId tenant id.
     * @return JSONArray of permissions.
     * @throws UserAdminException
     */
    JSONArray getRolePermissions(String roleName, int tenantId) throws UserAdminException;

    /**
     * Replace permissions of a role.
     *
     * @param roleName role name.
     * @param permissions JSONArray of permissions.
     * @throws UserAdminException
     */
    void updateRolePermissions(String roleName, String[] permissions) throws UserAdminException;

    /**
     * Get available UI permissions.
     *
     * @param tenantId tenant Id.
     * @return JSONArray of permissions.
     * @throws UserAdminException
     */
    JSONArray getAllPermissions(int tenantId) throws UserAdminException;
}
