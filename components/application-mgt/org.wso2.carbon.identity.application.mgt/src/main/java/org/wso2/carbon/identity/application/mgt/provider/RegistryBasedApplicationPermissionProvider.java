/*
 * Copyright (c) 2022-2026, WSO2 LLC. (http://www.wso2.com).
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
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
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.PATH_CONSTANT;

/**
 * Registry based application permission provider class.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.application.mgt.provider.ApplicationPermissionProvider",
                "service.scope=singleton"
        }
)
public class RegistryBasedApplicationPermissionProvider implements ApplicationPermissionProvider {

    private static final String APPLICATION_ROOT_PERMISSION = "applications";

    /*
     * Using 1024 stripes keeps the collision probability low for typical concurrent updates across
     * different applications, so false contention remains negligible. All lock instances are
     * eagerly initialized and held for the lifetime of the class, making memory cost fixed
     * and independent of the number of active operations.
     */
    private static final int PERMISSION_LOCKS_COUNT = 1024;
    private static final ReadWriteLock[] permissionLocks = new ReadWriteLock[PERMISSION_LOCKS_COUNT];

    static {
        for (int i = 0; i < PERMISSION_LOCKS_COUNT; i++) {
            permissionLocks[i] = new ReentrantReadWriteLock();
        }
    }

    private static Log log = LogFactory.getLog(RegistryBasedApplicationPermissionProvider.class);

    @Override
    public void renameAppPermissionName(String oldName, String newName)
            throws IdentityApplicationManagementException {

        if (StringUtils.equals(oldName, newName)) {
            return;
        }

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        initializeTenantRegistry(tenantId);

        Lock writeLock = getPermissionLock(tenantId, oldName).writeLock();
        writeLock.lock();

        try {
            Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(
                    RegistryType.USER_GOVERNANCE);
            String oldApplicationNode = getApplicationPermissionPath() + PATH_CONSTANT + oldName;

            if (!tenantGovReg.resourceExists(oldApplicationNode)) {
                return;
            }

            List<ApplicationPermission> loadPermissions = loadPermissionsInternal(oldName);
            String newApplicationNode = ApplicationMgtUtil.getApplicationPermissionPath() + PATH_CONSTANT + oldName;

            // Creating new application node.
            for (ApplicationPermission applicationPermission : loadPermissions) {
                safeDelete(tenantGovReg, newApplicationNode + PATH_CONSTANT + applicationPermission.getValue());
            }
            safeDelete(tenantGovReg, newApplicationNode);
            Collection permissionNode = tenantGovReg.newCollection();
            permissionNode.setProperty("name", newName);
            newApplicationNode = ApplicationMgtUtil.getApplicationPermissionPath() + PATH_CONSTANT + newName;
            String applicationNode = newApplicationNode;
            tenantGovReg.put(newApplicationNode, permissionNode);
            addPermission(applicationNode, loadPermissions.toArray(new ApplicationPermission[loadPermissions.size()]),
                    tenantGovReg);
        } catch (RegistryException e) {
            throw new IdentityApplicationManagementException(
                    "Error while renaming permission node " + oldName + " to " + newName, e);
        } finally {
            writeLock.unlock();
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

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        initializeTenantRegistry(tenantId);

        Lock writeLock = getPermissionLock(tenantId, applicationName).writeLock();
        writeLock.lock();

        try {
            String applicationNode = getApplicationPermissionPath() + PATH_CONSTANT + applicationName;
            Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(
                    RegistryType.USER_GOVERNANCE);

            boolean appNodeExists = tenantGovReg.resourceExists(applicationNode);

            if (ArrayUtils.isEmpty(permissions)) { // No new permissions.
                if (appNodeExists) {
                    safeDelete(tenantGovReg, applicationNode);
                }
                return;
            }

            if (!appNodeExists) {
                Collection appRootNode = tenantGovReg.newCollection();
                appRootNode.setProperty("name", applicationName);
                tenantGovReg.put(applicationNode, appRootNode);
            }

            Collection appNodeCollec = (Collection) tenantGovReg.get(applicationNode);
            String[] children = appNodeCollec.getChildren();

            if (children == null || appNodeCollec.getChildCount() < 1) { // No permissions exist for the application.
                addPermission(applicationNode, permissions, tenantGovReg);
            } else { // There are existing permissions for the application.
                List<ApplicationPermission> loadPermissions = loadPermissionsInternal(applicationName);
                for (ApplicationPermission applicationPermission : loadPermissions) {
                    safeDelete(tenantGovReg,
                            applicationNode + PATH_CONSTANT + applicationPermission.getValue());
                }
                addPermission(applicationNode, permissions, tenantGovReg);
            }
        } catch (RegistryException e) {
            throw new IdentityApplicationManagementException("Error while updating permissions", e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<ApplicationPermission> loadPermissions(String applicationName)
            throws IdentityApplicationManagementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        initializeTenantRegistry(tenantId);

        Lock readLock = getPermissionLock(tenantId, applicationName).readLock();
        readLock.lock();
        try {
            return loadPermissionsInternal(applicationName);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Reads the application permissions from the registry without acquiring a lock.
     * Callers must hold an appropriate lock (read or write) before invoking this method.
     * initializeTenantRegistry must also have been called by the caller prior to this.
     */
    private List<ApplicationPermission> loadPermissionsInternal(String applicationName)
            throws IdentityApplicationManagementException {

        boolean loggedInUserChanged = false;
        String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();

        try {
            String applicationNode = getApplicationPermissionPath() + PATH_CONSTANT + applicationName;
            Registry tenantGovReg =
                CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);

            boolean exist = tenantGovReg.resourceExists(applicationNode);

            if (!exist) {
                return Collections.emptyList();
            }

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

            List<String> paths = new ArrayList<>();
            List<ApplicationPermission> permissions = new ArrayList<>();

            permissionPath(tenantGovReg, applicationNode, paths, applicationNode);      // Get permission paths.

            // Recursively.
            for (String permissionPath : paths) {
                ApplicationPermission permission;
                permission = new ApplicationPermission();
                permission.setValue(permissionPath);
                permissions.add(permission);
            }
            return permissions;

        } catch (RegistryException | org.wso2.carbon.user.core.UserStoreException e) {
            throw new IdentityApplicationManagementException("Error while reading permissions", e);
        } finally {
            if (loggedInUserChanged) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(loggedInUser);
            }
        }
    }

    @Override
    public void deletePermissions(String applicationName) throws IdentityApplicationManagementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        initializeTenantRegistry(tenantId);

        Lock writeLock = getPermissionLock(tenantId, applicationName).writeLock();
        writeLock.lock();

        try {
            String applicationNode = getApplicationPermissionPath() + PATH_CONSTANT + applicationName;
            Registry tenantGovReg =
                CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);

            boolean exist = tenantGovReg.resourceExists(applicationNode);

            if (exist) {
                safeDelete(tenantGovReg, applicationNode);
            }
        } catch (RegistryException e) {
            throw new IdentityApplicationManagementException(
                    "Error while deleting permissions for application: " + applicationName, e);
        } finally {
            writeLock.unlock();
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

    /**
     * Explicitly initializes the tenant registry before any getRegistry() call.
     *
     * The getRegistry() method in CarbonContext lazily initializes the registry for tenant-specific paths
     * if it has not been initialized yet. However, under concurrent access, race conditions can cause this
     * method to return null. To prevent such scenarios, the registry is explicitly initialized here.
     * The IdentityException is caught and logged without rethrowing to preserve existing flow behavior.
     *
     * @param tenantId The tenant ID for which to initialize the registry.
     */
    private static void initializeTenantRegistry(int tenantId) {

        try {
            IdentityTenantUtil.initializeRegistry(tenantId);
        } catch (IdentityException e) {
            log.error("Error loading tenant registry for tenant ID: " + tenantId, e);
        }
    }

    /**
     * Retrieves the lock instance for the specific tenant and application.
     *
     * @param tenantId        The tenant ID.
     * @param applicationName The name of the application.
     * @return The ReadWriteLock instance for the specific tenant and application.
     */
    private static ReadWriteLock getPermissionLock(int tenantId, String applicationName) {

        String lockKey = tenantId + ":" + applicationName;
        int index = (lockKey.hashCode() & Integer.MAX_VALUE) % PERMISSION_LOCKS_COUNT;
        return permissionLocks[index];
    }

    /**
     * Deletes the registry node at the given path, safely ignoring cases where the node is
     * already absent.
     *
     * <p>
     * This is designed for clustered environments where a concurrent node might delete a
     * resource between an existence check and the deletion call. Because the Registry API
     * often re-wraps a ResourceNotFoundException inside a generic RegistryException,
     * this method inspects the cause to ensure that an "already deleted" state is treated
     * as a successful outcome.
     * </p>
     *
     * @param registry The registry instance.
     * @param path     The absolute path of the resource to delete.
     * @throws RegistryException if a registry error occurs that is not a wrapped 'Resource Not Found' error.
     */
    private static void safeDelete(Registry registry, String path) throws RegistryException {

        try {
            registry.delete(path);
        } catch (RegistryException e) {
            // Check if the RegistryException wraps a ResourceNotFoundException.
            // If so, the node is already gone, which satisfies the intent of this call.
            if (!(e.getCause() instanceof ResourceNotFoundException)) {
                throw e;
            }
            if (log.isDebugEnabled()) {
                log.debug("Registry node at " + path + " was already deleted, likely by a concurrent request.");
            }
        }
    }
}
