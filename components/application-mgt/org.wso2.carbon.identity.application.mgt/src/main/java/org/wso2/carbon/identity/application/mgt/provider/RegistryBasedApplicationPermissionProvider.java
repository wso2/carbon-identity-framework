/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.provider;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.PATH_CONSTANT;

/**
 * Registry based application permission provider class.
 */
public class RegistryBasedApplicationPermissionProvider implements ApplicationPermissionProvider {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final String APPLICATION_ROOT_PERMISSION = "applications";

    private static Log log = LogFactory.getLog(RegistryBasedApplicationPermissionProvider.class);

    @Override
    public void renameAppPermissionName(String oldName, String newName)
            throws IdentityApplicationManagementException {

        List<ApplicationPermission> loadPermissions = loadPermissions(oldName);
        String newApplicationNode = ApplicationMgtUtil.getApplicationPermissionPath() + PATH_CONSTANT + oldName;
        Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);
        // Creating new application node.
        try {
            for (ApplicationPermission applicationPermission : loadPermissions) {
                tenantGovReg.delete(newApplicationNode + PATH_CONSTANT + applicationPermission.getValue());
            }
            tenantGovReg.delete(newApplicationNode);
            Collection permissionNode = tenantGovReg.newCollection();
            permissionNode.setProperty("name", newName);
            newApplicationNode = ApplicationMgtUtil.getApplicationPermissionPath() + PATH_CONSTANT + newName;
            String applicationNode = newApplicationNode;
            tenantGovReg.put(newApplicationNode, permissionNode);
            addPermission(applicationNode, loadPermissions.toArray(new ApplicationPermission[loadPermissions.size()]),
                    tenantGovReg);
        } catch (RegistryException e) {
            throw new IdentityApplicationManagementException(
                    "Error while renaming permission node " + oldName + "to " + newName, e);
        }
    }

    @Override
    public void storePermissions(String applicationName, PermissionsAndRoleConfig permissionsConfig)
            throws IdentityApplicationManagementException {

        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        try {
            tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            IdentityTenantUtil.initializeRegistry(tenantId);
        } catch (IdentityException e) {
            throw new IdentityApplicationManagementException(
                    "Error loading tenant registry for tenant domain: " + IdentityTenantUtil.getTenantDomain(tenantId),
                    e);
        }
        Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);

        String permissionResourcePath = getApplicationPermissionPath();
        boolean loggedInUserChanged = false;
        try {
            if (!tenantGovReg.resourceExists(permissionResourcePath)) {
                UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
                if (!realm.getAuthorizationManager()
                        .isUserAuthorized(username, permissionResourcePath, UserMgtConstants.EXECUTE_ACTION)) {
                    // Logged-in user is not authorized to create the permission.
                    // Temporarily change the user to the admin for creating the permission.
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setUsername(realm.getRealmConfiguration().getAdminUserName());
                    tenantGovReg =
                            CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);
                    loggedInUserChanged = true;
                }
                Collection appRootNode = tenantGovReg.newCollection();
                appRootNode.setProperty("name", "Applications");
                tenantGovReg.put(permissionResourcePath, appRootNode);
            }

            if (permissionsConfig != null) {
                ApplicationPermission[] permissions = permissionsConfig.getPermissions();
                if (permissions == null || permissions.length < 1) {
                    return;
                }

                // Creating the application node in the tree.
                String appNode = permissionResourcePath + PATH_CONSTANT + applicationName;
                Collection appNodeColl = tenantGovReg.newCollection();
                tenantGovReg.put(appNode, appNodeColl);

                // Now start storing the permissions.
                for (ApplicationPermission permission : permissions) {
                    String permissionPath = appNode + PATH_CONSTANT + permission;
                    Resource permissionNode = tenantGovReg.newResource();
                    permissionNode.setProperty("name", permission.getValue());
                    tenantGovReg.put(permissionPath, permissionNode);
                }
            }
        } catch (Exception e) {
            throw new IdentityApplicationManagementException(
                    "Error while storing permissions for application " + applicationName, e);
        } finally {
            if (loggedInUserChanged) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
            }
        }
    }

    @Override
    public void updatePermissions(String applicationName, ApplicationPermission[] permissions)
            throws IdentityApplicationManagementException {

        String applicationNode = getApplicationPermissionPath() + PATH_CONSTANT + applicationName;

        Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);

        try {

            boolean appNodeExists = tenantGovReg.resourceExists(applicationNode);

            if (ArrayUtils.isEmpty(permissions)) { // No new permissions.
                if (appNodeExists) {
                    tenantGovReg.delete(applicationNode);
                }
                return;
            }

            if (!appNodeExists) {
                Collection appRootNode = tenantGovReg.newCollection();
                appRootNode.setProperty("name", applicationName);
                tenantGovReg.put(applicationNode, appRootNode);
            }

            Collection appNodeCollec = (Collection) tenantGovReg.get(applicationNode);
            String[] childern = appNodeCollec.getChildren();

            if (childern == null || appNodeCollec.getChildCount() < 1) { // No permissions exist for the application.
                addPermission(applicationNode, permissions, tenantGovReg);
            } else { // There are existing permissions for the application.
                List<ApplicationPermission> loadPermissions = loadPermissions(applicationName);
                for (ApplicationPermission applicationPermission : loadPermissions) {
                    tenantGovReg.delete(applicationNode + PATH_CONSTANT + applicationPermission.getValue());
                }
                addPermission(applicationNode, permissions, tenantGovReg);
            }
        } catch (RegistryException e) {
            throw new IdentityApplicationManagementException("Error while storing permissions", e);
        }
    }

    @Override
    public List<ApplicationPermission> loadPermissions(String applicationName)
            throws IdentityApplicationManagementException {

        String applicationNode = getApplicationPermissionPath() + PATH_CONSTANT + applicationName;
        Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);
        List<String> paths = new ArrayList<>();

        try {
            boolean exist = tenantGovReg.resourceExists(applicationNode);

            if (!exist) {
                return Collections.emptyList();
            }

            boolean loggedInUserChanged = false;
            String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();

            UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            if (loggedInUser == null || !realm.getAuthorizationManager()
                    .isUserAuthorized(loggedInUser, applicationNode, UserMgtConstants.EXECUTE_ACTION)) {
                // Logged-in user is not authorized to read the permission.
                // Temporarily change the user to the admin for reading the permission.
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .setUsername(realm.getRealmConfiguration().getAdminUserName());
                tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);
                loggedInUserChanged = true;
            }

            paths.clear();             // Clear current paths.
            List<ApplicationPermission> permissions = new ArrayList<>();

            permissionPath(tenantGovReg, applicationNode, paths, applicationNode);      // Get permission paths.

            // Recursively.
            for (String permissionPath : paths) {
                ApplicationPermission permission;
                permission = new ApplicationPermission();
                permission.setValue(permissionPath);
                permissions.add(permission);
            }

            if (loggedInUserChanged) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(loggedInUser);
            }

            return permissions;

        } catch (RegistryException | org.wso2.carbon.user.core.UserStoreException e) {
            throw new IdentityApplicationManagementException("Error while reading permissions", e);
        }
    }

    @Override
    public void deletePermissions(String applicationName) throws IdentityApplicationManagementException {

        String applicationNode = getApplicationPermissionPath() + PATH_CONSTANT + applicationName;
        Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);
        try {
            boolean exist = tenantGovReg.resourceExists(applicationNode);
            if (!exist) {
                return;
            }
            tenantGovReg.delete(applicationNode);
        } catch (Exception e) {
            /*
             * For more information read https://github.com/wso2/product-is/issues/12579. This is to overcome the
             * above issue.
             */
            log.error(String.format(
                    "Error occurred while trying to delete permissions for application: %s. Retrying " + "again",
                    applicationName), e);
            boolean isOperationFailed = true;
            for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
                try {
                    Thread.sleep(1000);
                    boolean exist = tenantGovReg.resourceExists(applicationNode);
                    if (!exist) {
                        return;
                    }
                    tenantGovReg.delete(applicationNode);
                    isOperationFailed = false;
                    log.info(String.format("Permissions deleted application: %s in the retry attempt: %s",
                            applicationName, attempt));
                    break;
                } catch (Exception exception) {
                    log.error(
                            String.format("Retry attempt: %s failed to delete permission for application: %s", attempt,
                                    applicationName), exception);
                }
            }
            if (isOperationFailed) {
                throw new IdentityApplicationManagementException(
                        "Error while deleting permissions for application: " + applicationName, e);
            }
        }
    }

    private void addPermission(String applicationNode, ApplicationPermission[] permissions, Registry tenantGovReg)
            throws RegistryException {

        for (ApplicationPermission permission : permissions) {
            String permissionValue = permission.getValue();

            // If permissions are starts with slash remove that.
            if ("/".equals(permissionValue.substring(0, 1))) {
                permissionValue = permissionValue.substring(1);
            }
            String[] splitedPermission = permissionValue.split("/");
            String permissinPath = applicationNode + PATH_CONSTANT;

            for (int i = 0; i < splitedPermission.length; i++) {
                permissinPath = permissinPath + splitedPermission[i] + PATH_CONSTANT;
                Collection permissionNode = tenantGovReg.newCollection();
                permissionNode.setProperty("name", splitedPermission[i]);
                tenantGovReg.put(permissinPath, permissionNode);
            }
        }
    }

    private void permissionPath(Registry tenantGovReg, String permissionPath, List<String> paths,
                                String applicationNode) throws RegistryException {

        Collection appCollection = (Collection) tenantGovReg.get(permissionPath);
        String[] children = appCollection.getChildren();

        if ((children == null || children.length == 0) && !Objects.equals(permissionPath, applicationNode)) {
            paths.add(permissionPath.replace(applicationNode, "").substring(2));
        }

        if (children != null && children.length != 0) {
            for (String child : children) {
                permissionPath(tenantGovReg, child, paths, applicationNode);
            }
        }
    }

    private String getApplicationPermissionPath() {

        return CarbonConstants.UI_PERMISSION_NAME + RegistryConstants.PATH_SEPARATOR + APPLICATION_ROOT_PERMISSION;
    }
}
