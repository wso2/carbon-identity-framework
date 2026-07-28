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

package org.wso2.carbon.identity.device.policy.internal.dao;

import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementServerException;

import java.sql.Timestamp;

/**
 * DAO for the device token replay store. Tracks accepted token identifiers (jti) so that a previously
 * seen device token cannot be replayed within its freshness window.
 */
public interface DeviceTokenReplayDAO {

    /**
     * Checks whether a device token with the given jti has already been accepted for the tenant.
     *
     * @param jti      The JWT ID claim of the device token.
     * @param tenantId The tenant the device belongs to.
     * @return {@code true} if the jti is already present in the replay store, {@code false} otherwise.
     * @throws PolicyManagementServerException If the replay store cannot be queried.
     */
    boolean isTokenReplayed(String jti, int tenantId) throws PolicyManagementServerException;

    /**
     * Records an accepted device token jti so that subsequent presentations of the same token are rejected.
     *
     * @param jti        The JWT ID claim of the device token.
     * @param tenantId   The tenant the device belongs to.
     * @param issuedAt   The token issued-at (iat) time.
     * @param expiryTime The time after which the jti record may be cleaned up (iat + freshness window).
     * @throws PolicyManagementServerException If the jti cannot be persisted.
     */
    void storeToken(String jti, int tenantId, Timestamp issuedAt, Timestamp expiryTime)
            throws PolicyManagementServerException;

    /**
     * Removes device token jti records whose expiry time has passed.
     *
     * @param cutoff The cut-off time; records with an EXPIRY_TIME older than this are removed.
     * @throws PolicyManagementServerException If the expired records cannot be removed.
     */
    void removeExpiredTokens(Timestamp cutoff) throws PolicyManagementServerException;
}
