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

import org.mockito.MockedStatic;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.provisioning.internal.ProvisioningServiceDataHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ProvisioningRoleMgtListenerTest {

    RealmService realmService = mock(RealmService.class);
    RoleManagementService roleManagementService = mock(RoleManagementService.class);
    DefaultInboundUserProvisioningListener defaultInboundUserProvisioningListener =
            mock(DefaultInboundUserProvisioningListener.class);
    UserRealm userRealm = mock(UserRealm.class);
    AbstractUserStoreManager userStoreManager = mock(AbstractUserStoreManager.class);
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;


    @BeforeTest
    public void setup() throws UserStoreException {

        ProvisioningServiceDataHolder.getInstance().setRealmService(realmService);
        ProvisioningServiceDataHolder.getInstance().setRoleManagementService(roleManagementService);
        ProvisioningServiceDataHolder.getInstance()
                .setDefaultInboundUserProvisioningListener(defaultInboundUserProvisioningListener);

        when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(()->IdentityTenantUtil.getTenantId("carbon.super")).thenReturn(-1234);
        when(userStoreManager.getUserNameFromUserID(anyString())).thenReturn("");
    }

    @Test
    public void abc() throws IdentityRoleManagementException {

        ProvisioningRoleMgtListener postUpdateUserListOfRole = new ProvisioningRoleMgtListener();
        RoleBasicInfo roleBasicInfo = new RoleBasicInfo("", "");
        roleBasicInfo.setAudienceName("organization");
        when(roleManagementService.getRoleBasicInfoById(anyString(), anyString())).thenReturn(roleBasicInfo);
        postUpdateUserListOfRole.postUpdateUserListOfRole("", new ArrayList<>(), new ArrayList<>(), "carbon.super");
    }

}
