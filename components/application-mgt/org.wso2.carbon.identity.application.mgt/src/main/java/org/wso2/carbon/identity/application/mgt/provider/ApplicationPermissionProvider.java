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

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;

import java.util.List;

/**
 * Interface to provide application permissions.
 */
public interface ApplicationPermissionProvider {

    /**
     * Rename application permission name.
     *
     * @param oldName Old permission name.
     * @param newName New permission name.
     * @throws IdentityApplicationManagementException If an error occurred while renaming the permission name.
     */
    void renameAppPermissionName(String oldName, String newName) throws IdentityApplicationManagementException;

    /**
     * Store new application permissions.
     *
     * @param applicationName   Application name.
     * @param permissionsConfig Permission configurations.
     * @throws IdentityApplicationManagementException If an error occurred while storing the new permission.
     */
    void storePermissions(String applicationName, PermissionsAndRoleConfig permissionsConfig)
            throws IdentityApplicationManagementException;

    /**
     * Update application permissions.
     *
     * @param applicationName Application name.
     * @param permissions     List of permissions.
     * @throws IdentityApplicationManagementException If an error occurred while updating permissions.
     */
    void updatePermissions(String applicationName, ApplicationPermission[] permissions)
            throws IdentityApplicationManagementException;

    /**
     * Load application permissions.
     *
     * @param applicationName Application name.
     * @return List of application permissions.
     * @throws IdentityApplicationManagementException If an error occurred while loading permissions.
     */
    List<ApplicationPermission> loadPermissions(String applicationName) throws IdentityApplicationManagementException;

    /**
     * Delete application permissions.
     *
     * @param applicationName Application name
     * @throws IdentityApplicationManagementException If an error occurred while deleting permissions.
     */
    void deletePermissions(String applicationName) throws IdentityApplicationManagementException;
}
