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

package org.wso2.carbon.identity.device.policy.internal.service.impl;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyClientException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;

import java.util.Date;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class DeviceTokenReplayProtectionServiceTest {

    private DeviceTokenReplayProtectionService deviceTokenReplayProtectionService;
    private MockedStatic<LoggerUtils> mockedLoggerUtils;

    @BeforeMethod
    public void setUp() {
        mockedLoggerUtils = mockStatic(LoggerUtils.class);
        mockedLoggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
        deviceTokenReplayProtectionService = DeviceTokenReplayProtectionService.getInstance();
    }

    @AfterMethod
    public void tearDown() {
        if (mockedLoggerUtils != null) {
            mockedLoggerUtils.close();
        }
    }

    @Test
    public void testGetInstance() {

        assertNotNull(DeviceTokenReplayProtectionService.getInstance());
    }

    @Test
    public void testAssertUnusedAndRecord() throws DevicePolicyException {

        String jti = "service-jti-1";
        int tenantId = 1;
        String correlationId = "corr-1";
        Date iat = new Date();

        deviceTokenReplayProtectionService.assertUnusedAndRecord(jti, iat, tenantId, correlationId);

        try {
            deviceTokenReplayProtectionService.assertUnusedAndRecord(jti, iat, tenantId, correlationId);
            fail("Expected DevicePolicyClientException");
        } catch (DevicePolicyClientException e) {
            assertTrue(e.getErrorCode().equals(DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_REPLAYED.getCode()));
        }
    }

    @Test
    public void testRemoveExpiredTokens() throws DevicePolicyException {
        deviceTokenReplayProtectionService.removeExpiredTokens();
        assertTrue(true);
    }
}
