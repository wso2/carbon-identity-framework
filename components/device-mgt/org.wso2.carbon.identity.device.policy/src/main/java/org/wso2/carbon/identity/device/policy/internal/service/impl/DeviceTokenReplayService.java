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

import org.wso2.carbon.identity.device.policy.api.constant.DevicePolicyErrorMessage;
import org.wso2.carbon.identity.device.policy.internal.constant.DeviceTokenConstants;
import org.wso2.carbon.identity.device.policy.internal.dao.DeviceTokenReplayDAO;
import org.wso2.carbon.identity.device.policy.internal.dao.impl.DeviceTokenReplayDAOImpl;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyDiagnosticLogger;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyExceptionHandler;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementServerException;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Enforces single-use of device token jti claims against the replay store, and removes expired
 * jti records. This is the only class in the bundle that constructs {@link DeviceTokenReplayDAOImpl}.
 */
public class DeviceTokenReplayService {

    private static final DeviceTokenReplayService INSTANCE = new DeviceTokenReplayService();

    private final DeviceTokenReplayDAO replayDAO;
    private final DevicePolicyDiagnosticLogger diagnosticLogger = new DevicePolicyDiagnosticLogger();

    private DeviceTokenReplayService() {

        replayDAO = new DeviceTokenReplayDAOImpl();
    }

    /**
     * Returns the singleton instance of DeviceTokenReplayService.
     *
     * @return The singleton DeviceTokenReplayService instance.
     */
    public static DeviceTokenReplayService getInstance() {

        return INSTANCE;
    }

    /**
     * Asserts that the given jti has not already been used, then records it in the replay store.
     *
     * @param jti           The JWT ID claim of the device token.
     * @param issuedAt      The token issued-at (iat) time.
     * @param tenantId      The tenant the device belongs to.
     * @param correlationId Identifier (deviceId or registrationId) used only for diagnostic correlation.
     * @throws PolicyManagementException If the jti has already been used, or the replay store cannot
     *                                    be queried or updated.
     */
    public void assertUnusedAndRecord(String jti, Date issuedAt, int tenantId, String correlationId)
            throws PolicyManagementException {

        if (replayDAO.isTokenReplayed(jti, tenantId)) {
            diagnosticLogger.logTokenValidationFailure(correlationId,
                    "Device token jti has already been used (replay detected).");
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_REPLAYED, jti);
        }

        Timestamp issuedAtTimestamp = new Timestamp(issuedAt.getTime());
        Timestamp expiryTimestamp = new Timestamp(
                issuedAt.getTime() + DeviceTokenConstants.TOKEN_FRESHNESS_WINDOW_MILLIS);
        replayDAO.storeToken(jti, tenantId, issuedAtTimestamp, expiryTimestamp);
    }

    /**
     * Removes device token jti records whose expiry time has passed.
     *
     * @throws PolicyManagementServerException If the expired records cannot be removed.
     */
    public void removeExpiredTokens() throws PolicyManagementServerException {

        replayDAO.removeExpiredTokens(new Timestamp(System.currentTimeMillis()));
    }
}
