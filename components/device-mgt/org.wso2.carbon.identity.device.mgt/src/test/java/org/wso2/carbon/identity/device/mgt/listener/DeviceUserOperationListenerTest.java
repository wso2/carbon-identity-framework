/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.mgt.listener;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.mgt.internal.listener.DeviceUserOperationListener;
import org.wso2.carbon.identity.device.mgt.internal.service.impl.DeviceManagementServiceImpl;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeviceUserOperationListenerTest {

    private DeviceUserOperationListener listener;
    private AbstractUserStoreManager userStoreManager;
    private DeviceManagementServiceImpl deviceManagementService;
    private MockedStatic<DeviceManagementServiceImpl> serviceMockedStatic;
    private MockedStatic<IdentityTenantUtil> tenantUtilMockedStatic;

    @BeforeClass
    public void setUpClass() {

        if (System.getProperty("carbon.home") == null) {
            System.setProperty("carbon.home", System.getProperty("user.dir"));
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {

        listener = new DeviceUserOperationListener();
        userStoreManager = mock(AbstractUserStoreManager.class);
        deviceManagementService = mock(DeviceManagementServiceImpl.class);

        serviceMockedStatic = mockStatic(DeviceManagementServiceImpl.class);
        tenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);

        serviceMockedStatic.when(DeviceManagementServiceImpl::getInstance).thenReturn(deviceManagementService);
        when(userStoreManager.getTenantId()).thenReturn(1);
        tenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantDomain(1)).thenReturn("carbon.super");
    }

    @AfterMethod
    public void tearDown() {

        IdentityUtil.threadLocalProperties.get().clear();
        if (serviceMockedStatic != null) {
            serviceMockedStatic.close();
        }
        if (tenantUtilMockedStatic != null) {
            tenantUtilMockedStatic.close();
        }
    }

    @Test
    public void testDoPreDeleteUserDeletesDevices() throws Exception {

        when(userStoreManager.getUserIDFromUserName("alice")).thenReturn("user-id-123");

        boolean result = listener.doPreDeleteUser("alice", userStoreManager);

        Assert.assertTrue(result);
        verify(deviceManagementService).deleteDevicesByUserId("user-id-123", "carbon.super");
    }

    @Test
    public void testDoPreDeleteUserWithBlankUserIdDoesNotDelete() throws Exception {

        when(userStoreManager.getUserIDFromUserName("unknown")).thenReturn("");

        boolean result = listener.doPreDeleteUser("unknown", userStoreManager);

        Assert.assertTrue(result);
    }
}
