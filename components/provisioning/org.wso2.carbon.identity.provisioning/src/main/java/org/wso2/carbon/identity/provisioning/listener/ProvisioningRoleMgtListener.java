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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.provisioning.internal.ProvisioningServiceDataHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.AbstractRoleManagementListener;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.RoleManagementListener;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for outbound user provisioning based on role.
 */
public class ProvisioningRoleMgtListener extends AbstractRoleManagementListener {

    private static final Log LOG = LogFactory.getLog(ProvisioningRoleMgtListener.class);

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

            String[] deletedUserNames = resolveUserNameFromUserIds(deletedUserIDList, userStoreManager);

            String[] newUserNames = resolveUserNameFromUserIds(newUserIDList, userStoreManager);

            if (deletedUserNames.length == 0 && newUserNames.length == 0) {
                return;
            }

            RoleBasicInfo roleBasicInfo = ProvisioningServiceDataHolder.getInstance().getRoleManagementService()
                    .getRoleBasicInfoById(roleId,tenantDomain);
            // Only organization audience roles are supported for role based outbound provisioning
            if (!RoleConstants.ORGANIZATION.equals(roleBasicInfo.getAudience())) {
                return;
            }
            String roleName = RoleConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR +
                    roleBasicInfo.getName();
            ProvisioningServiceDataHolder.getInstance().getDefaultInboundUserProvisioningListener()
                    .doPostUpdateUserListOfRole(roleName, deletedUserNames, newUserNames, userStoreManager);
        } catch (UserStoreException e) {
            throw new IdentityRoleManagementException(e.getMessage(), e);
        }
    }

    private String[] resolveUserNameFromUserIds(List<String> userIds, AbstractUserStoreManager userStoreManager) {

        List<String> userNames = new ArrayList<>();
        for (String userID : userIds) {
            try {
                userNames.add(userStoreManager.getUserNameFromUserID(userID));
            } catch (org.wso2.carbon.user.core.UserStoreException e) {
                LOG.warn("Fail to resolve the user by user-id.");
            }
        }
        return userNames.toArray(new String[0]);
    }
}
