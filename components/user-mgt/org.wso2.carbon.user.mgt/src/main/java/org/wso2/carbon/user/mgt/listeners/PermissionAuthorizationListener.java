/*
 * Copyright (c) 2008 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.mgt.listeners;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractAuthorizationManagerListener;
import org.wso2.carbon.user.core.listener.AuthorizationManagerListener;

/**
 * Class to restrict set authorization for super tenant permission nodes
 */
public class PermissionAuthorizationListener extends AbstractAuthorizationManagerListener
        implements AuthorizationManagerListener {

    @Override
    public int getExecutionOrderId() {
        return AuthorizationManagerListener.PERMISSION_AUTHORIZATION_LISTENER;
    }

    @Override
    public boolean isRoleAuthorized(String roleName, String resourceId, String action,
                                    AuthorizationManager authorizationManager) throws
            UserStoreException {
        return isAuthorized(resourceId, authorizationManager);
    }

    @Override
    public boolean isUserAuthorized(String userName, String resourceId, String action,
                                    AuthorizationManager authorizationManager) throws
            UserStoreException {
        return isAuthorized(resourceId, authorizationManager);
    }

    public boolean isAuthorized(String resourceId,
                                AuthorizationManager authorizationManager) throws
            UserStoreException {
        int tenantId = authorizationManager.getTenantId();
        if (tenantId == CarbonConstants.SUPER_TENANT_ID) {
            // no restrictions for the super tenant
            return true;
        }
        // so don't allow the rest of the tenants to set any permission to protected nodes
        String protectedPermissionPath = RegistryUtils.getAbsolutePath(null,
                RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                        CarbonConstants.UI_PROTECTED_PERMISSION_COLLECTION);

        return !(resourceId.startsWith(protectedPermissionPath));
    }

    public void resetPermissionOnUpdateRole(String roleName, String newRoleName)
            throws UserStoreException {
        return;

    }


}
