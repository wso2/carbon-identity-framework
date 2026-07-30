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

package org.wso2.carbon.identity.device.policy.internal.cleanup;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.internal.service.impl.DeviceTokenReplayProtectionService;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class DeviceTokenJtiCleanupServiceTest {

    private DeviceTokenJtiCleanupService cleanupService;
    private MockedStatic<DeviceTokenReplayProtectionService> mockedReplayServiceStatic;
    private DeviceTokenReplayProtectionService replayServiceMock;

    @BeforeMethod
    public void setUp() {

        replayServiceMock = mock(DeviceTokenReplayProtectionService.class);
        mockedReplayServiceStatic = mockStatic(DeviceTokenReplayProtectionService.class);
        mockedReplayServiceStatic.when(DeviceTokenReplayProtectionService::getInstance).thenReturn(replayServiceMock);

        cleanupService = new DeviceTokenJtiCleanupService(0, 1);
    }

    @AfterMethod
    public void tearDown() {

        mockedReplayServiceStatic.close();
        cleanupService.shutdown();
    }

    @Test
    public void testActivateCleanUp() throws DevicePolicyServerException {

        doNothing().when(replayServiceMock).removeExpiredTokens();

        cleanupService.activateCleanUp();

        verify(replayServiceMock, timeout(1000)).removeExpiredTokens();
    }

    @Test
    public void testShutdown() throws NoSuchFieldException, IllegalAccessException {

        cleanupService.shutdown();
        
        Field schedulerField = DeviceTokenJtiCleanupService.class.getDeclaredField("scheduler");
        schedulerField.setAccessible(true);
        ScheduledExecutorService scheduler = (ScheduledExecutorService) schedulerField.get(cleanupService);
        
        assertNotNull(scheduler);
        assertTrue(scheduler.isShutdown());
    }
}
