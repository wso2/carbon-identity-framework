/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.provisioning.listener;

import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.provisioning.internal.ProvisioningServiceDataHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.AbstractRoleManagementListener;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.RoleManagementListener;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.List;

public class ProvisioningRoleMgtListener extends AbstractRoleManagementListener {

    @Override
    public boolean isEnable() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (RoleManagementListener.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return true;
        }
        return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
    }

    @Override
    public int getDefaultOrderId() {

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (RoleManagementListener.class.getName(), this.getClass().getName());
        if (identityEventListenerConfig == null) {
            return IdentityCoreConstants.EVENT_LISTENER_ORDER_ID;
        }
        return identityEventListenerConfig.getOrder();
    }

    @Override
    public void postUpdateUserListOfRole(String roleId, List<String> newUserIDList, List<String> deletedUserIDList,
                                         String tenantDomain) throws IdentityRoleManagementException {

        try {
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) ProvisioningServiceDataHolder
                    .getInstance().getRealmService().getTenantUserRealm(IdentityTenantUtil.getTenantId(tenantDomain))
                    .getUserStoreManager();

            String[] deletedUserNames = deletedUserIDList.stream().map(userId -> {
                try {
                    return userStoreManager.getUserNameFromUserID(userId);
                } catch (org.wso2.carbon.user.core.UserStoreException e) {
                    return userId;
                }
            }).toArray(String[]::new);

            String[] newUserNames = newUserIDList.stream().map(userId -> {
                try {
                    return userStoreManager.getUserNameFromUserID(userId);
                } catch (org.wso2.carbon.user.core.UserStoreException e) {
                    return userId;
                }
            }).toArray(String[]::new);

            if (deletedUserNames.length > 0 && newUserNames.length > 0) {
                return;
            }
            ProvisioningServiceDataHolder.getInstance().getDefaultInboundUserProvisioningListener()
                    .doPostUpdateUserListOfRole(roleId, deletedUserNames, newUserNames, userStoreManager);
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementException(e.getMessage(), e);
        }
    }
}
