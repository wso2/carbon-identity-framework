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

package org.wso2.carbon.identity.device.policy.api.service;

import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;

import java.util.Map;

/**
 * Verifies a device-data JWT against a caller-supplied public key and returns
 * the verified claims. Used where trust in the key has already been
 * established out of band (e.g. the device registration executor, which
 * verifies the key via the challenge signature) and the device is not yet
 * persisted, so a registry lookup is neither possible nor required.
 */
public interface DeviceTokenVerifier {

    /**
     * Verifies a device-data JWT signed with the given public key and returns
     * its claims. Runs signature, {@code iat} freshness and {@code jti}
     * single-use checks before the claims are trusted.
     *
     * @param token           Raw JWT string carrying the device attributes.
     * @param base64PublicKey Base64-encoded X.509 EC public key to verify.
     * @param correlationId   Identifier used only for diagnostic correlation.
     * @param tenantDomain    Tenant domain used for replay-store scoping.
     * @return Map of device field names to their values from verified JWT.
     * @throws DevicePolicyException If the token is invalid or stale.
     */
    Map<String, Object> verifyWithPublicKey(String token, String base64PublicKey,
                                            String correlationId, String tenantDomain)
            throws DevicePolicyException;
}
