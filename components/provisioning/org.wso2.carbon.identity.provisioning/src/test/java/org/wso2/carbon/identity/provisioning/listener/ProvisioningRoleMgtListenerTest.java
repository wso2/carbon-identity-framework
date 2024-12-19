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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.provisioning.internal.ProvisioningServiceDataHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Listeners(MockitoTestNGListener.class)
public class ProvisioningRoleMgtListenerTest {

    @Mock
    private RealmService realmService;

    @Mock
    UserRealm userRealm;

    @Mock
    AbstractUserStoreManager userStoreManager;

    @Mock
    RoleManagementService roleManagementService;

    @Mock
    DefaultInboundUserProvisioningListener defaultInboundUserProvisioningListener;

    @Mock
    IdentityEventListenerConfig identityEventListenerConfig;

    ProvisioningRoleMgtListener provisioningRoleMgtListener = new ProvisioningRoleMgtListener();

    @BeforeTest
    public void beforeTest() {

        // Initialize mocks
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void setup() {

        ProvisioningServiceDataHolder.getInstance()
                .setDefaultInboundUserProvisioningListener(defaultInboundUserProvisioningListener);
        ProvisioningServiceDataHolder.getInstance().setRoleManagementService(roleManagementService);
        ProvisioningServiceDataHolder.getInstance().setRealmService(realmService);
    }

    @Test(expectedExceptions = IdentityRoleManagementException.class)
    public void testUserStoreException()
            throws org.wso2.carbon.user.api.UserStoreException, IdentityRoleManagementException {

        when(realmService.getTenantUserRealm(anyInt())).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(userRealm.getUserStoreManager()).thenThrow(new UserStoreException("Error retrieving user store"));

        try (MockedStatic<IdentityTenantUtil> identityTenantUtil = Mockito.mockStatic(IdentityTenantUtil.class)) {

            identityTenantUtil.when(()-> IdentityTenantUtil.getTenantId(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))
                    .thenReturn(MultitenantConstants.SUPER_TENANT_ID);
            provisioningRoleMgtListener.postUpdateUserListOfRole("roleId", new ArrayList<>(), new ArrayList<>(),
                    "carbon.super");
        }
    }

    @Test
    public void testUpdateRoleWithoutUserAddOrDeletion()
            throws IdentityRoleManagementException, org.wso2.carbon.user.api.UserStoreException {

        when(realmService.getTenantUserRealm(anyInt())).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        try (MockedStatic<IdentityTenantUtil> identityTenantUtil = Mockito.mockStatic(IdentityTenantUtil.class)) {

            identityTenantUtil.when(()-> IdentityTenantUtil.getTenantId(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))
                    .thenReturn(MultitenantConstants.SUPER_TENANT_ID);
            provisioningRoleMgtListener.postUpdateUserListOfRole("roleId", new ArrayList<>(), new ArrayList<>(), "carbon.super");
            Mockito.verify(roleManagementService, Mockito.times(0))
                    .getRoleBasicInfoById(anyString(), anyString());
        }
    }

    @Test
    public void testUpdateApplicationAudienceRole()
            throws IdentityRoleManagementException, org.wso2.carbon.user.api.UserStoreException {

        when(realmService.getTenantUserRealm(anyInt())).thenReturn(userRealm);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        RoleBasicInfo roleBasicInfo = new RoleBasicInfo();
        roleBasicInfo.setAudience("application");
        try (MockedStatic<IdentityTenantUtil> identityTenantUtil = Mockito.mockStatic(IdentityTenantUtil.class)) {

            identityTenantUtil.when(()-> IdentityTenantUtil.getTenantId(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))
                    .thenReturn(MultitenantConstants.SUPER_TENANT_ID);
            when(roleManagementService.getRoleBasicInfoById(anyString(), anyString())).thenReturn(roleBasicInfo);
            provisioningRoleMgtListener.postUpdateUserListOfRole("roleId", Collections.singletonList("JOHN"),
                    new ArrayList<>(), "carbon.super");
            Mockito.verify(defaultInboundUserProvisioningListener, Mockito.times(0))
                    .doPostUpdateUserListOfRole(anyString(), any(), any(), any());
        }
    }

    @Test
    public void testListenerEnabled() {

        try (MockedStatic<IdentityUtil> identityUtil = Mockito.mockStatic(IdentityUtil.class)) {

            identityUtil.when(() -> IdentityUtil.readEventListenerProperty(anyString(), anyString()))
                    .thenReturn(identityEventListenerConfig);
            when(identityEventListenerConfig.getEnable()).thenReturn("true");
            assert provisioningRoleMgtListener.isEnable();
        }
    }

    @Test
    public void testListenerDefaultOrder() {

        try (MockedStatic<IdentityUtil> identityUtil = Mockito.mockStatic(IdentityUtil.class)) {

            identityUtil.when(() -> IdentityUtil.readEventListenerProperty(anyString(), anyString()))
                    .thenReturn(identityEventListenerConfig);
            when(identityEventListenerConfig.getOrder()).thenReturn(100);
            assert provisioningRoleMgtListener.getDefaultOrderId() == 100;
        }
    }
}
