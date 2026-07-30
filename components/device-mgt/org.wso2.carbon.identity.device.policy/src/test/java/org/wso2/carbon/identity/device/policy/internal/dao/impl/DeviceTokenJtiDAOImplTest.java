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

package org.wso2.carbon.identity.device.policy.internal.dao.impl;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class DeviceTokenJtiDAOImplTest {

    private DeviceTokenJtiDAOImpl deviceTokenJtiDAO;

    @BeforeMethod
    public void setUp() {
        deviceTokenJtiDAO = new DeviceTokenJtiDAOImpl();
    }

    @Test
    public void testStoreTokenAndIsTokenReplayed() throws DevicePolicyServerException {

        String jti = "test-jti-1";
        int tenantId = 1;
        Timestamp iat = new Timestamp(System.currentTimeMillis());
        Timestamp exp = new Timestamp(System.currentTimeMillis() + 100000);

        assertFalse(deviceTokenJtiDAO.isTokenReplayed(jti, tenantId));

        deviceTokenJtiDAO.storeToken(jti, tenantId, iat, exp);

        assertTrue(deviceTokenJtiDAO.isTokenReplayed(jti, tenantId));
    }

    @Test
    public void testRemoveExpiredTokens() throws DevicePolicyServerException {

        String jti = "test-jti-2";
        int tenantId = 1;
        Timestamp iat = new Timestamp(System.currentTimeMillis() - 100000);
        Timestamp exp = new Timestamp(System.currentTimeMillis() - 50000);

        deviceTokenJtiDAO.storeToken(jti, tenantId, iat, exp);
        assertTrue(deviceTokenJtiDAO.isTokenReplayed(jti, tenantId));

        Timestamp cutoff = new Timestamp(System.currentTimeMillis());
        deviceTokenJtiDAO.removeExpiredTokens(cutoff);

        assertFalse(deviceTokenJtiDAO.isTokenReplayed(jti, tenantId));
    }

    /**
     * Documents the DAO layer's own behavior on a duplicate (TENANT_ID, JTI) insert: it stays a
     * server exception whose cause chain bottoms out in a SQLIntegrityConstraintViolationException.
     * The DAO does not — and should not — reinterpret this as a client replay error itself;
     * {@link org.wso2.carbon.identity.device.policy.internal.service.impl.DeviceTokenReplayProtectionService}
     * is the layer that translates a duplicate-key violation here into ERROR_DEVICE_TOKEN_REPLAYED.
     */
    @Test
    public void testStoreTokenDuplicateInsertThrowsServerExceptionWithConstraintViolationCause()
            throws DevicePolicyServerException {

        String jti = "test-jti-duplicate";
        int tenantId = 1;
        Timestamp iat = new Timestamp(System.currentTimeMillis());
        Timestamp exp = new Timestamp(System.currentTimeMillis() + 100000);

        deviceTokenJtiDAO.storeToken(jti, tenantId, iat, exp);

        try {
            deviceTokenJtiDAO.storeToken(jti, tenantId, iat, exp);
            fail("Expected DevicePolicyServerException on duplicate (TENANT_ID, JTI) insert.");
        } catch (DevicePolicyServerException e) {
            assertEquals(e.getErrorCode(), DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_REPLAY_STORE_FAILED.getCode());

            Throwable cause = e;
            boolean foundConstraintViolation = false;
            while (cause != null) {
                if (cause instanceof SQLIntegrityConstraintViolationException) {
                    foundConstraintViolation = true;
                    break;
                }
                cause = cause.getCause();
            }
            assertTrue(foundConstraintViolation,
                    "Expected a SQLIntegrityConstraintViolationException somewhere in the cause chain.");
        }
    }
}
