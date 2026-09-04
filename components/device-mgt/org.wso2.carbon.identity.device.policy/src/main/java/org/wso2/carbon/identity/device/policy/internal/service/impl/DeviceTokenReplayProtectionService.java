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
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.internal.constant.DeviceTokenConstants;
import org.wso2.carbon.identity.device.policy.internal.dao.DeviceTokenJtiDAO;
import org.wso2.carbon.identity.device.policy.internal.dao.impl.DeviceTokenJtiDAOImpl;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyDiagnosticLogger;
import org.wso2.carbon.identity.device.policy.internal.util.DevicePolicyExceptionHandler;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Enforces single-use of device token jti claims against the replay store, and removes expired
 * jti records. This is the only class in the bundle that constructs {@link DeviceTokenJtiDAOImpl}.
 */
public class DeviceTokenReplayProtectionService {

    private static final DeviceTokenReplayProtectionService INSTANCE = new DeviceTokenReplayProtectionService();

    private final DeviceTokenJtiDAO jtiDAO;
    private final DevicePolicyDiagnosticLogger diagnosticLogger = new DevicePolicyDiagnosticLogger();

    private DeviceTokenReplayProtectionService() {

        jtiDAO = new DeviceTokenJtiDAOImpl();
    }

    /**
     * Returns the singleton instance of DeviceTokenReplayProtectionService.
     *
     * @return The singleton DeviceTokenReplayProtectionService instance.
     */
    public static DeviceTokenReplayProtectionService getInstance() {

        return INSTANCE;
    }

    /**
     * Asserts that the given jti has not already been used, then records it in the replay store.
     *
     * <p>The pre-check below is a fast-path optimization only, not the correctness guarantee —
     * two concurrent requests for the same jti can both pass it before either inserts. Correctness
     * comes from the (TENANT_ID, JTI) primary key on IDN_DEVICE_TOKEN_JTI: the insert that loses
     * the race fails with a duplicate-key violation, which is caught below and translated into the
     * same client-facing replay error the pre-check would have produced.
     *
     * @param jti           The JWT ID claim of the device token.
     * @param issuedAt      The token issued-at (iat) time.
     * @param tenantId      The tenant the device belongs to.
     * @param correlationId Identifier (deviceId or registrationId) used only for diagnostic correlation.
     * @throws DevicePolicyException If the jti has already been used, or the replay store cannot
     *                               be queried or updated.
     */
    public void assertUnusedAndRecord(String jti, Date issuedAt, int tenantId, String correlationId)
            throws DevicePolicyException {

        // Fast-path only: the PRIMARY KEY (TENANT_ID, JTI) on IDN_DEVICE_TOKEN_JTI, enforced by the
        // storeToken() insert below, is the actual single-use guard. This check just avoids the
        // insert-and-catch round trip in the common (non-racing) case.
        if (jtiDAO.isTokenReplayed(jti, tenantId)) {
            diagnosticLogger.logTokenValidationFailure(correlationId,
                    "Device token jti has already been used (replay detected).");
            throw DevicePolicyExceptionHandler.handleClientException(
                    DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_REPLAYED, jti);
        }

        Timestamp issuedAtTimestamp = new Timestamp(issuedAt.getTime());
        Timestamp expiryTimestamp = new Timestamp(
                issuedAt.getTime() + DeviceTokenConstants.TOKEN_FRESHNESS_WINDOW_MILLIS);
        try {
            jtiDAO.storeToken(jti, tenantId, issuedAtTimestamp, expiryTimestamp);
        } catch (DevicePolicyServerException e) {
            if (isDuplicateKeyViolation(e)) {
                diagnosticLogger.logTokenValidationFailure(correlationId,
                        "Device token jti has already been used (replay detected).");
                throw DevicePolicyExceptionHandler.handleClientException(
                        DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_REPLAYED, jti);
            }
            throw e;
        }
    }

    /**
     * Checks whether the given exception's cause chain contains a SQL unique/primary-key
     * constraint violation — the signal that a concurrent request already recorded this jti
     * between the {@link #assertUnusedAndRecord} pre-check and the insert.
     *
     * @param e Exception thrown while storing the token.
     * @return {@code true} if the failure was a duplicate-key violation.
     */
    private boolean isDuplicateKeyViolation(Throwable e) {

        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            if (cause instanceof SQLException) {
                SQLException sqlEx = (SQLException) cause;
                String sqlState = sqlEx.getSQLState();
                int vendorCode = sqlEx.getErrorCode();

                // Common SQLState for integrity constraint violations is class '23'.
                if (sqlState != null && sqlState.startsWith("23")) {
                    return true;
                }

                // Vendor-specific codes: MySQL duplicate key = 1062, Oracle ORA-00001 = vendorCode 1
                if (vendorCode == 1062 || vendorCode == 1) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * Removes device token jti records whose expiry time has passed.
     *
     * @throws DevicePolicyServerException If the expired records cannot be removed.
     */
    public void removeExpiredTokens() throws DevicePolicyServerException {

        jtiDAO.removeExpiredTokens(new Timestamp(System.currentTimeMillis()));
    }
}
