/*
* Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.device.mgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtClientException;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.model.DeviceRegistrationInitiation;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.mgt.internal.cache.DeviceRegistrationCache;
import org.wso2.carbon.identity.device.mgt.internal.cache.DeviceRegistrationCacheEntry;
import org.wso2.carbon.identity.device.mgt.internal.cache.DeviceRegistrationCacheKey;
import org.wso2.carbon.identity.device.mgt.internal.cache.DeviceRegistrationContext;
import org.wso2.carbon.identity.device.mgt.internal.dao.DeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.dao.impl.DeviceManagementDAOImpl;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceManagementAuditLogger;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceManagementExceptionHandler;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link DeviceManagementService}.
 */
public class DeviceManagementServiceImpl implements DeviceManagementService {

    private static final Log LOG = LogFactory.getLog(DeviceManagementServiceImpl.class);
    private static final DeviceManagementServiceImpl INSTANCE = new DeviceManagementServiceImpl();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final DeviceManagementAuditLogger AUDIT_LOGGER = new DeviceManagementAuditLogger();
    private final DeviceManagementDAO deviceManagementDAO;

    private DeviceManagementServiceImpl() {
        deviceManagementDAO = new DeviceManagementDAOImpl();
    }

    /**
     * Returns the service singleton instance.
     *
     * @return Service singleton.
     */
    public static DeviceManagementServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public DeviceRegistrationInitiation initiateDeviceRegistration(String username, String tenantDomain)
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
        return new DeviceRegistrationInitiation(registrationId, challenge);
    }

    @Override
    public Device verifyDeviceRegistration(
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
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_REGISTRATION_CONTEXT_NOT_FOUND, registrationId);
        }

        DeviceRegistrationContext context = cacheEntry.getContext();
        verifySignature(registrationId, context.getChallenge(), publicKey, signature);

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
                .status("ACTIVE")
                .registeredAt(Timestamp.from(Instant.now()))
                .metadata(metadata)
                .build();
    }

    @Override
    public void persistDevice(Device device, String tenantDomain) throws DeviceMgtException {

        if (device.getUserId() == null || device.getUserId().trim().isEmpty()) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_USER_ID_REQUIRED);
        }
        deviceManagementDAO.registerDevice(device, IdentityTenantUtil.getTenantId(tenantDomain));

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.REGISTER, device);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device persisted for user: " + device.getUserId() +
                    " in tenant: " + tenantDomain + " with device ID: " + device.getId());
        }
    }

    @Override
    public Device getDeviceById(String deviceId, String tenantDomain)
            throws DeviceMgtException {

        validateRequiredField(deviceId, "deviceId");
        return deviceManagementDAO.getDeviceById(
                deviceId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Device> getDevicesByUserId(String userId, String tenantDomain)
            throws DeviceMgtException {

        validateRequiredField(userId, "userId");
        return deviceManagementDAO.getDevicesByUserId(
                userId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Device> getAllDevices(String tenantDomain) throws DeviceMgtException {

        return deviceManagementDAO.getAllDevices(IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Device> getDevices(String tenantDomain, int offset, int limit) throws DeviceMgtException {

        return deviceManagementDAO.getDevices(IdentityTenantUtil.getTenantId(tenantDomain), offset, limit);
    }

    @Override
    public int getDeviceCount(String tenantDomain) throws DeviceMgtException {

        return deviceManagementDAO.getDeviceCount(IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Device updateDeviceName(String deviceId, String deviceName, String tenantDomain)
            throws DeviceMgtException {

        validateRequiredField(deviceId, "deviceId");
        validateRequiredField(deviceName, "deviceName");
        validateDeviceExists(deviceId, tenantDomain);

        Device updated = deviceManagementDAO.updateDeviceName(
                deviceId, deviceName, IdentityTenantUtil.getTenantId(tenantDomain));

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.UPDATE, updated);
        return updated;
    }

    @Override
    public void deleteDevice(String deviceId, String tenantDomain)
            throws DeviceMgtException {

        validateRequiredField(deviceId, "deviceId");
        validateDeviceExists(deviceId, tenantDomain);

        deviceManagementDAO.deleteDevice(
                deviceId, IdentityTenantUtil.getTenantId(tenantDomain));

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.DELETE, deviceId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device deleted with ID: " + deviceId + " in tenant: " + tenantDomain);
        }
    }

    private void verifySignature(String registrationId, String challenge,
                                 String publicKeyBase64, String signatureBase64) throws DeviceMgtException {

        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

            byte[] challengeBytes = Base64.getUrlDecoder().decode(challenge);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(publicKey);
            sig.update(challengeBytes);
            boolean valid = sig.verify(signatureBytes);

            if (!valid) {
                throw DeviceManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE, registrationId);
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException |
                 InvalidKeyException | SignatureException e) {
            if (e instanceof SignatureException && e.getMessage() != null
                    && e.getMessage().contains("verification failed")) {
                throw DeviceManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_INVALID_DEVICE_SIGNATURE, e, registrationId);
            }
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_VERIFYING_SIGNATURE, e, registrationId);
        }
    }

    private void validateDeviceExists(String deviceId, String tenantDomain)
            throws DeviceMgtException {

        Device existing = deviceManagementDAO.getDeviceById(
                deviceId, IdentityTenantUtil.getTenantId(tenantDomain));
        if (existing == null) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_DEVICE_NOT_FOUND, deviceId);
        }
    }

    private void validateRequiredField(String value, String fieldName)
            throws DeviceMgtClientException {

        if (value == null || value.trim().isEmpty()) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_DEVICE_FIELD, fieldName);
        }
    }
}

