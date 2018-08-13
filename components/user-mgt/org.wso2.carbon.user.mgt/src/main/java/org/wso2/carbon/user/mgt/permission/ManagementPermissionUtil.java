/*
 * Copyright (c) 2010 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.permission;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.util.AdminServicesUtil;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserOperationEventListener;
import org.wso2.carbon.user.core.constants.UserCoreErrorConstants;
import org.wso2.carbon.user.core.listener.UserManagementErrorEventListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.carbon.user.mgt.internal.UserMgtDSComponent;

/**
 * This is the class should be used by Admin service authors to get the Registry
 * and Realms.
 */
public class ManagementPermissionUtil {

    /**
     * Should only be called by the Admin Services.
     * @param roleName
     * @param rawPermissions
     * @throws UserAdminException
     */

    private ManagementPermissionUtil(){

    }

    public static void updateRoleUIPermission(String roleName, String[] rawPermissions)
            throws UserAdminException {

        Permission[] permissions = null;
        UserStoreManager userStoreManager = null;
        try {
            String[] optimizedList = UserCoreUtil.optimizePermissions(rawPermissions);
            UserRealm realm = AdminServicesUtil.getUserRealm();
            AuthorizationManager authMan = realm.getAuthorizationManager();
            authMan.clearRoleActionOnAllResources(roleName, UserMgtConstants.EXECUTE_ACTION);
            permissions = new Permission[optimizedList.length];
            for (int i = 0; i < optimizedList.length; i++) {
                authMan.authorizeRole(roleName, optimizedList[i], UserMgtConstants.EXECUTE_ACTION);
                permissions[i] = new Permission(optimizedList[i], UserMgtConstants.EXECUTE_ACTION);
            }
            userStoreManager = realm.getUserStoreManager();
            handlePostUpdatePermissionsOfRole(roleName, permissions, userStoreManager);
        } catch (UserStoreException e) {
            handleOnUpdatePermissionsOfRoleFailure(e.getMessage(), roleName, permissions, userStoreManager);
            // not logging already logged
            throw new UserAdminException(e.getMessage(), e);
        } catch (CarbonException e) {
            handleOnUpdatePermissionsOfRoleFailure(e.getMessage(), roleName, null, null);
            throw new UserAdminException(e.getMessage(), e);
        }
    }
    
	public static Permission[] getRoleUIPermissions(String roleName, String[] rawPermissions)
			throws UserAdminException {
		Permission[] permissions;
		if (ArrayUtils.isEmpty(rawPermissions)) {
			return new Permission[0];
		}

		String[] optimizedList = UserCoreUtil.optimizePermissions(rawPermissions);
		permissions = new Permission[optimizedList.length];
		int i = 0;
		for (String path : optimizedList) {
			permissions[i++] = new Permission(path, UserMgtConstants.EXECUTE_ACTION);
		}

		return permissions;
	}

    /**
     * To call relevant event listeners when there is a failure while updating permissions of role.
     *
     * @param errorMessage Relevant error message.
     * @param permissions  Permissions updated for the role.
     * @throws UserAdminException User Admin Exception.
     */
    public static void handleOnUpdatePermissionsOfRoleFailure(String errorMessage, String roleName, Permission[]
            permissions, UserStoreManager userStoreManager)
            throws UserAdminException {

        try {
            for (UserManagementErrorEventListener listener : UserMgtDSComponent
                    .getUserManagementErrorEventListeners()) {
                if (listener.isEnable() && !listener.onUpdatePermissionsOfRoleFailure(
                        UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_PERMISSIONS_OF_ROLE
                                .getCode(), String.format(
                                UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ERROR_WHILE_UPDATING_PERMISSIONS_OF_ROLE
                                        .getMessage(), errorMessage), roleName, permissions, userStoreManager)) {
                    return;
                }
            }
        } catch (UserStoreException ex) {
            throw new UserAdminException(
                    "Exception while executing error listeners after a failure while updating permissions of role "
                            + roleName, ex);
        }
    }

    /**
     * To call the relevant listeners after updating permissions of a role.
     *
     * @param roleName    Name of the role.
     * @param permissions Relevant permissions that are updated.
     * @throws UserStoreException User Store Exception that will be thrown by relevant listeners while handling.
     */
    public static void handlePostUpdatePermissionsOfRole(String roleName, Permission[] permissions,
            UserStoreManager userStoreManager) throws UserStoreException {

        for (UserOperationEventListener userOperationEventListener : UserMgtDSComponent
                .getUserOperationEventListeners()) {
            if (userOperationEventListener instanceof AbstractUserOperationEventListener
                    && !((AbstractUserOperationEventListener) userOperationEventListener)
                    .doPostUpdatePermissionsOfRole(roleName, permissions, userStoreManager)) {
                return;

            }
        }
    }

}
