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

package org.wso2.carbon.identity.device.registration.internal.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtClientException;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.registration.internal.cache.DeviceRegistrationCache;
import org.wso2.carbon.identity.device.registration.internal.cache.DeviceRegistrationCacheEntry;
import org.wso2.carbon.identity.device.registration.internal.cache.DeviceRegistrationCacheKey;
import org.wso2.carbon.identity.device.registration.internal.cache.DeviceRegistrationContext;
import org.wso2.carbon.identity.device.registration.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.device.registration.internal.model.DeviceRegistrationChallenge;
import org.wso2.carbon.identity.device.registration.internal.util.DeviceRegistrationExceptionHandler;
import org.wso2.carbon.identity.device.registration.internal.util.DeviceSignatureVerifier;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage.ERROR_INVALID_DEVICE_FIELD;

/**
 * Implements the two-phase device registration challenge-response protocol, internal to the
 * device.registration bundle. {@link org.wso2.carbon.identity.device.registration.DeviceRegistrationExecutor}
 * is the sole consumer.
 */
public class DeviceRegistrationHandler {

    private static final Log LOG = LogFactory.getLog(DeviceRegistrationHandler.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private DeviceRegistrationHandler() {

    }

    /**
     * Phase 1 of the two-phase registration protocol.
     * Generates a cryptographically random challenge and stores it in the distributed cache
     * keyed by the returned registrationId. The client SDK must sign the challenge with its
     * private key and return the signature in Phase 2.
     *
     * @param username     Username of the registering user.
     * @param tenantDomain Tenant domain.
     * @return DeviceRegistrationChallenge containing registrationId and challenge (base64url).
     * @throws DeviceMgtException If username or tenantDomain is invalid.
     */
    public static DeviceRegistrationChallenge initiate(String username, String tenantDomain)
            throws DeviceMgtException {

        validateRequiredField(username, "username");
        validateRequiredField(tenantDomain, "tenantDomain");

        // Generate 32 random bytes as the challenge, encoded as base64url without padding.
        byte[] challengeBytes = new byte[32];
        SECURE_RANDOM.nextBytes(challengeBytes);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);

        String registrationId = UUID.randomUUID().toString();
        DeviceRegistrationContext context = new DeviceRegistrationContext(username, challenge, tenantDomain);
        DeviceRegistrationCacheKey cacheKey = new DeviceRegistrationCacheKey(registrationId);
        DeviceRegistrationCacheEntry cacheEntry = new DeviceRegistrationCacheEntry(context);

        DeviceRegistrationCache.getInstance().addToCache(cacheKey, cacheEntry, tenantDomain);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device registration initiated for user: " + username +
                    " in tenant: " + tenantDomain +
                    " with registrationId: " + registrationId);
        }
        return new DeviceRegistrationChallenge(registrationId, challenge);
    }

    /**
     * Verifies the device registration challenge-response without persisting to the database.
     * Used during registration flows where the user does not yet have a provisioned userId —
     * the caller stores the returned object in the flow context and defers the DB write to
     * {@code DeviceManagementService.persistDevice(Device, String)} once UserProvisioningExecutor
     * has run.
     *
     * @param registrationId Opaque token returned by {@link #initiate(String, String)}.
     * @param publicKey      Base64-encoded EC public key (X.509/SubjectPublicKeyInfo DER).
     * @param signature      Base64-encoded ECDSA signature over the challenge bytes.
     * @param deviceName     Human-readable name for the device.
     * @param deviceModel    Hardware model string (nullable).
     * @param metadata       Optional JSON string for extensible attributes (nullable).
     * @param tenantDomain   Tenant domain.
     * @return A Device whose userId is unset — caller must set the real userId before persisting.
     * @throws DeviceMgtException If the registration context is missing or the signature is invalid.
     */
    public static Device verify(
            String registrationId,
            String publicKey,
            String signature,
            String deviceName,
            String deviceModel,
            String metadata,
            String tenantDomain) throws DeviceMgtException {

        validateRequiredField(registrationId, "registrationId");
        validateRequiredField(publicKey, "publicKey");
        validateRequiredField(signature, "signature");
        validateRequiredField(deviceName, "deviceName");

        DeviceRegistrationCacheKey cacheKey = new DeviceRegistrationCacheKey(registrationId);
        DeviceRegistrationCacheEntry cacheEntry =
                DeviceRegistrationCache.getInstance().getValueFromCache(cacheKey, tenantDomain);

        if (cacheEntry == null) {
            throw DeviceRegistrationExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND, registrationId);
        }

        DeviceRegistrationContext context = cacheEntry.getContext();
        DeviceSignatureVerifier.verify(registrationId, context.getChallenge(), publicKey, signature);

        DeviceRegistrationCache.getInstance().clearCacheEntry(cacheKey, tenantDomain);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device registration verified (not yet persisted) for user: " + context.getUsername() +
                    " in tenant: " + tenantDomain);
        }
        return new Device.Builder()
                .id(registrationId)
                .deviceName(deviceName)
                .deviceModel(deviceModel)
                .publicKey(publicKey)
                .registeredAt(Timestamp.from(Instant.now()))
                .metadata(metadata)
                .build();
    }

    private static void validateRequiredField(String value, String fieldName)
            throws DeviceMgtClientException {

        if (value == null || value.trim().isEmpty()) {
            throw new DeviceMgtClientException(
                    ERROR_INVALID_DEVICE_FIELD.getMessage(),
                    String.format(ERROR_INVALID_DEVICE_FIELD.getDescription(), fieldName),
                    ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }
}
