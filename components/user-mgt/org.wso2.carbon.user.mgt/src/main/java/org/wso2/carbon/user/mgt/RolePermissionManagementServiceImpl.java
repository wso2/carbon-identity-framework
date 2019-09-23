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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;

/**
 * OSGi service for Permission Management of a role.
 */
public class RolePermissionManagementServiceImpl implements RolePermissionManagementService {

    private static final Log log = LogFactory.getLog(RolePermissionManagementServiceImpl.class);


    @Override
    public JSONArray getRolePermissions(String roleName, int tenantId) throws UserAdminException {

        try {
            JSONArray permissionArray = new JSONArray();
            setSelectedPermissionsToArray(getUserAdminProxy().getRolePermissions(roleName, tenantId), permissionArray);
            return permissionArray;
        } catch (UserAdminException e) {
            log.error("An error occurred when retrieving permissions of role: " + roleName);
            throw new UserAdminException("An error occurred when retrieving permissions of role: " + roleName, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateRolePermissions(String roleName, String[] permissions) throws UserAdminException {

        try {
            getUserAdminProxy().setRoleUIPermission(roleName, permissions);
        } catch (UserAdminException e) {
            log.error("An error occurred when retrieving permissions of role: " + roleName);
            throw new UserAdminException("An error occurred when retrieving permissions of role: " + roleName, e);
        }
    }


    public JSONArray getAllPermissions(int tenantId) throws UserAdminException {

        try {
            JSONArray permissionArray = new JSONArray();
            setAllPermissionsToArray(getUserAdminProxy().getAllUIPermissions(tenantId), permissionArray);
            return permissionArray;
        } catch (UserAdminException e) {
            log.error("An error occurred when retrieving all permissions.");
            throw new UserAdminException("An error occurred when retrieving all permissions.", e);
        }
    }

    /**
     * Get the UserAdmin service.
     *
     * @return UserRealmProxy of UserAdmin service.
     */
    private static UserRealmProxy getUserAdminProxy() {

        UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        return new UserRealmProxy(realm);
    }

    /**
     * Recursively go through UIPermissionNode, do not go through leaves if root node selected.
     *
     * @param node  UIPermissionNode of permissions.
     * @param permissions  JSONArray of permissions.
     */
    private void setSelectedPermissionsToArray(UIPermissionNode node, JSONArray permissions) {

        if (node.isSelected()) {
            // Assuming all child nodes selected no traversing further.
            permissions.put(node.getResourcePath());
            if (log.isDebugEnabled()) {
                log.debug("Permission: " + node.getDisplayName() + " and resourcePath: " +
                        node.getResourcePath() + " added to the permission Map");
            }

        } else {
            UIPermissionNode[] nodeList = node.getNodeList();
            if (ArrayUtils.isNotEmpty(nodeList)) {
                for (UIPermissionNode nod : nodeList) {
                    setSelectedPermissionsToArray(nod, permissions);
                }
            }
        }
    }

    /**
     * Recursively go through UIPermissionNode.
     *
     * @param allUIPermissions  UI permission list
     * @param permissions JSONArray of permissions.
     */
    private void setAllPermissionsToArray(UIPermissionNode allUIPermissions, JSONArray permissions) {

        UIPermissionNode[] nodeList = allUIPermissions.getNodeList();
        permissions.put(allUIPermissions.getResourcePath());
        if (ArrayUtils.isNotEmpty(nodeList)) {
            for (UIPermissionNode nod : nodeList) {
                setAllPermissionsToArray(nod, permissions);
            }
        }
    }
}
