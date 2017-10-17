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
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.user.mgt.common.UserAdminException;

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
        try {
            String[] optimizedList = UserCoreUtil.optimizePermissions(rawPermissions);
            UserRealm realm = AdminServicesUtil.getUserRealm();
            AuthorizationManager authMan = realm.getAuthorizationManager();
            authMan.clearRoleActionOnAllResources(roleName, UserMgtConstants.EXECUTE_ACTION);
            for (String path : optimizedList) {
                authMan.authorizeRole(roleName, path, UserMgtConstants.EXECUTE_ACTION);
            }
        } catch (UserStoreException e) {
            // not logging already logged
            throw new UserAdminException(e.getMessage(), e);
        } catch (CarbonException e) {
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

}
