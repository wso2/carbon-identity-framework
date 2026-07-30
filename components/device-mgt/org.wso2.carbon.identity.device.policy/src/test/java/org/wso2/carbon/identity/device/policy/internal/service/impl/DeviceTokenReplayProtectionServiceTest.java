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
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.internal.constant.DeviceTokenConstants;
import org.wso2.carbon.identity.device.policy.internal.dao.DeviceTokenJtiDAO;
import org.wso2.carbon.identity.device.policy.internal.dao.impl.DeviceTokenJtiDAOImpl;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Date;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
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
    public void testAssertUnusedAndRecordHandlesConcurrentDuplicateInsert() throws Exception {

        String jti = "service-jti-race";
        int tenantId = 1;
        String correlationId = "corr-race";
        Date iat = new Date();

        // Simulate the losing side of a race: another request already recorded this jti between
        // this request's pre-check and its insert. Record it directly via the real DAO first, then
        // wrap the DAO so isTokenReplayed() still (falsely) reports "not seen yet" — forcing the
        // insert itself to hit the duplicate-key path that assertUnusedAndRecord must catch.
        DeviceTokenJtiDAOImpl realDao = new DeviceTokenJtiDAOImpl();
        Timestamp issuedAt = new Timestamp(iat.getTime());
        Timestamp expiryTime = new Timestamp(iat.getTime() + 60000);
        realDao.storeToken(jti, tenantId, issuedAt, expiryTime);

        DeviceTokenJtiDAO staleReadDao = new DeviceTokenJtiDAO() {

            @Override
            public boolean isTokenReplayed(String jti, int tenantId) {
                return false;
            }

            @Override
            public void storeToken(String jti, int tenantId, Timestamp issuedAt, Timestamp expiryTime)
                    throws DevicePolicyServerException {
                realDao.storeToken(jti, tenantId, issuedAt, expiryTime);
            }

            @Override
            public void removeExpiredTokens(Timestamp cutoff) throws DevicePolicyServerException {
                realDao.removeExpiredTokens(cutoff);
            }
        };

        Field daoField = DeviceTokenReplayProtectionService.class.getDeclaredField("jtiDAO");
        daoField.setAccessible(true);
        Object originalDao = daoField.get(deviceTokenReplayProtectionService);
        daoField.set(deviceTokenReplayProtectionService, staleReadDao);

        try {
            deviceTokenReplayProtectionService.assertUnusedAndRecord(jti, iat, tenantId, correlationId);
            fail("Expected DevicePolicyClientException from the duplicate-key race path");
        } catch (DevicePolicyClientException e) {
            assertEquals(e.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_REPLAYED.getCode());
        } finally {
            daoField.set(deviceTokenReplayProtectionService, originalDao);
        }
    }

    @Test
    public void testRemoveExpiredTokens() throws DevicePolicyException {

        String jti = "service-jti-expired";
        int tenantId = 1;
        String correlationId = "corr-expired";
        // iat far enough in the past that the token's freshness window has already elapsed,
        // so the record removeExpiredTokens() targets is guaranteed to be past its expiry.
        Date iat = new Date(System.currentTimeMillis() - DeviceTokenConstants.TOKEN_FRESHNESS_WINDOW_MILLIS * 2);

        deviceTokenReplayProtectionService.assertUnusedAndRecord(jti, iat, tenantId, correlationId);

        deviceTokenReplayProtectionService.removeExpiredTokens();

        // If the expired row had not actually been deleted, recording the same jti again would
        // throw ERROR_DEVICE_TOKEN_REPLAYED instead of succeeding.
        deviceTokenReplayProtectionService.assertUnusedAndRecord(jti, new Date(), tenantId, correlationId);
    }
}
