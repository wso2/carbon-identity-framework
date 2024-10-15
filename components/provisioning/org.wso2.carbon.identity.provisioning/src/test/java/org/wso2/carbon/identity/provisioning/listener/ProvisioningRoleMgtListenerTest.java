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

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.provisioning.internal.ProvisioningServiceDataHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@Listeners(MockitoTestNGListener.class)
public class ProvisioningRoleMgtListenerTest {

    @Mock
    RealmService realmService;
    @Mock
    RealmConfiguration realmConfiguration;
    @Mock
    UserRealm userRealm;
    @Mock
    AbstractUserStoreManager userStoreManager;
    @Mock
    RoleManagementService roleManagementService;
    @Mock
    ApplicationManagementService applicationManagementService;

    DefaultInboundUserProvisioningListener defaultInboundUserProvisioningListener;

    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic;

    @BeforeTest
    public void setup() throws UserStoreException, IdentityApplicationManagementException {

        // Initialize mocks
        MockitoAnnotations.initMocks(this);
        defaultInboundUserProvisioningListener = new DefaultInboundUserProvisioningListener();

        ProvisioningServiceDataHolder.getInstance().setRealmService(realmService);
        ProvisioningServiceDataHolder.getInstance()
                .setDefaultInboundUserProvisioningListener(defaultInboundUserProvisioningListener);


        when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(()->IdentityTenantUtil.getTenantId("carbon.super")).thenReturn(-1234);
        when(userStoreManager.getUserNameFromUserID(anyString())).thenReturn("");

        when(userStoreManager.getRealmConfiguration()).thenReturn(realmConfiguration);
        when(realmConfiguration.getUserStoreProperty("DomainName")).thenReturn("PRIMARY");

        applicationManagementServiceMockedStatic = mockStatic(ApplicationManagementService.class);
        applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance)
                .thenReturn(applicationManagementService);
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName("sample-app");
        when(applicationManagementService.getServiceProvider(anyString(), anyString())).thenReturn(serviceProvider);
    }

    @Test
    public void abc() throws IdentityRoleManagementException {

        ProvisioningRoleMgtListener postUpdateUserListOfRole = new ProvisioningRoleMgtListener();
        RoleBasicInfo roleBasicInfo = new RoleBasicInfo("", "developer");
        roleBasicInfo.setAudience("organization");
        ProvisioningServiceDataHolder.getInstance().setRoleManagementService(roleManagementService);
        when(roleManagementService.getRoleBasicInfoById(anyString(), eq("carbon.super"))).thenReturn(roleBasicInfo);
        List<String> newUserIdList = new ArrayList<>();
        newUserIdList.add("12345");
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
            postUpdateUserListOfRole.postUpdateUserListOfRole("", newUserIdList, new ArrayList<>(), "carbon.super");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
