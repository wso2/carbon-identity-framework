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
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.mgt.common.model.Permission;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * OSGi service for Permission Management of a role.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.user.mgt.RolePermissionManagementService",
                "service.scope=singleton"
        }
)
public class RolePermissionManagementServiceImpl implements RolePermissionManagementService {

    private static final Log log = LogFactory.getLog(RolePermissionManagementServiceImpl.class);

    @Override
    public String[] getRolePermissions(String roleName, int tenantId) throws RolePermissionException {

        try {

            return getSelectedPermissions(getUserAdminProxy().getRolePermissions(roleName, tenantId));

        } catch (UserAdminException e) {
            log.error("An error occurred when retrieving permissions of role: " + roleName);
            throw new RolePermissionException("An error occurred when retrieving permissions of role : " + roleName, e);
        }
    }

    @Override
    public void setRolePermissions(String roleName, String[] permissions) throws RolePermissionException {

        try {
            getUserAdminProxy().setRoleUIPermission(roleName, permissions);
        } catch (UserAdminException e) {
            log.error("An error occurred when retrieving permissions of role: " + roleName);
            throw new RolePermissionException("An error occurred when retrieving permissions of role: " + roleName, e);
        }
    }

    @Override
    public Permission[] getAllPermissions(int tenantId) throws RolePermissionException {

        try {
            return getAllPermissions(getUserAdminProxy().getAllUIPermissions(tenantId));
        } catch (UserAdminException e) {
            log.error("An error occurred when retrieving all permissions.");
            throw new RolePermissionException("An error occurred when retrieving all permissions.", e);
        }
    }

    /**
     * Get the UserAdmin service.
     *
     * @return UserRealmProxy of UserAdmin service.
     */
    private UserRealmProxy getUserAdminProxy() {

        UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        return new UserRealmProxy(realm);
    }

    /**
     * Recursively go through UIPermissionNode, do not go through leaves if root node selected.
     *
     * @param node  UIPermissionNode of permissions.
     * @return String[] of permissions.
     */
    private String[] getSelectedPermissions(UIPermissionNode node) {

        List<String> permissions = new ArrayList<>();
        if (node.isSelected()) {
            // Assuming all child nodes selected no traversing further.
            permissions.add(node.getResourcePath());
            if (log.isDebugEnabled()) {
                log.debug("Permission: " + node.getDisplayName() + " and resourcePath: " +
                        node.getResourcePath() + " added to the permission Map");
            }
        } else {
            UIPermissionNode[] childNodes = node.getNodeList();
            if (ArrayUtils.isNotEmpty(childNodes)) {
                for (UIPermissionNode childNode : childNodes) {
                    permissions.addAll(Arrays.asList(getSelectedPermissions(childNode)));
                }
            }
        }
        return permissions.toArray(new String[0]);
    }

    /**
     * Recursively go through UIPermissionNode.
     *
     * @param node UIPermissionNode of permissions.
     * @return  Permission[] of permissions.
     */
    private Permission[] getAllPermissions(UIPermissionNode node) {

        List<Permission> permissions = new ArrayList<>();
        UIPermissionNode[] childNodes = node.getNodeList();
        Permission permission = new Permission();
        permission.setDisplayName(node.getDisplayName());
        permission.setResourcePath(node.getResourcePath());
        permissions.add(permission);
        if (ArrayUtils.isNotEmpty(childNodes)) {
            for (UIPermissionNode childNode : childNodes) {
                permissions.addAll(Arrays.asList(getAllPermissions(childNode)));
            }
        }
        return permissions.toArray(new Permission[0]);
    }
}
