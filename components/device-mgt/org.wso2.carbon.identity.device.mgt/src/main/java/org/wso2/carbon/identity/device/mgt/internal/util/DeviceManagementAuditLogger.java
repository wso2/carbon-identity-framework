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

package org.wso2.carbon.identity.device.mgt.internal.util;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.utils.AuditLog;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.jsonObjectToMap;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;

/**
 * Audit logger for device management state changes.
 * Emits an audit record whenever a device credential is registered, renamed or deleted so the
 * "who changed which device" trail is preserved. Read operations are intentionally not audited.
 */
public class DeviceManagementAuditLogger {

    private static final String DEVICE_TARGET_TYPE = "Device";
    private static final String FINGERPRINT_UNAVAILABLE = "unavailable";

    /**
     * Print an audit log for an operation that carries the full device state.
     *
     * @param operation Operation associated with the state change.
     * @param device    Device to be logged, or {@code null} if unavailable.
     */
    public void printAuditLog(Operation operation, Device device) {

        if (device == null) {
            printAuditLog(operation, (String) null);
            return;
        }
        JSONObject data = createAuditLogEntry(device);
        buildAuditLog(device.getId(), operation, data);
    }

    /**
     * Print an audit log for an operation identified only by the device ID (e.g. delete).
     *
     * @param operation Operation associated with the state change.
     * @param deviceId  ID of the device to be logged.
     */
    public void printAuditLog(Operation operation, String deviceId) {

        JSONObject data = new JSONObject();
        data.put(LogConstants.ID_FIELD, deviceId != null ? deviceId : JSONObject.NULL);
        buildAuditLog(deviceId, operation, data);
    }

    /**
     * Build and trigger the audit log event.
     *
     * @param targetId  Target device ID.
     * @param operation Operation to be logged.
     * @param data      Data to be logged.
     */
    private void buildAuditLog(String targetId, Operation operation, JSONObject data) {

        String initiatorId = getInitiatorId();
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                initiatorId,
                LoggerUtils.getInitiatorType(initiatorId),
                targetId,
                DEVICE_TARGET_TYPE,
                operation.getLogAction())
                .data(jsonObjectToMap(data));
        triggerAuditLogEvent(auditLogBuilder);
    }

    /**
     * Create audit log data for the given device.
     * The device owner is recorded as the opaque user ID (a UUID, not PII, so not masked) and the
     * public key is reduced to a fingerprint; the raw public key and free-form metadata are never logged.
     *
     * @param device Device to be logged.
     * @return Audit log data as a JSONObject.
     */
    private JSONObject createAuditLogEntry(Device device) {

        JSONObject data = new JSONObject();
        data.put(LogConstants.ID_FIELD, device.getId() != null ? device.getId() : JSONObject.NULL);
        data.put(LogConstants.DEVICE_NAME_FIELD,
                device.getDeviceName() != null ? device.getDeviceName() : JSONObject.NULL);
        data.put(LogConstants.DEVICE_MODEL_FIELD,
                device.getDeviceModel() != null ? device.getDeviceModel() : JSONObject.NULL);
        data.put(LogConstants.STATUS_FIELD,
                device.getStatus() != null ? device.getStatus().name() : JSONObject.NULL);
        data.put(LogConstants.REGISTERED_AT_FIELD,
                device.getRegisteredAt() != null ? String.valueOf(device.getRegisteredAt()) : JSONObject.NULL);
        data.put(LogConstants.USER_ID_FIELD, device.getUserId() != null
                ? device.getUserId() : JSONObject.NULL);
        data.put(LogConstants.PUBLIC_KEY_FINGERPRINT_FIELD, device.getPublicKey() != null
                ? fingerprint(device.getPublicKey()) : JSONObject.NULL);
        return data;
    }

    /**
     * Resolve the initiator of the audit event from the carbon context, falling back to System.
     *
     * @return Initiator ID.
     */
    private String getInitiatorId() {

        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        String username = carbonContext.getUsername();
        String tenantDomain = carbonContext.getTenantDomain();

        if (StringUtils.isBlank(username)) {
            return LoggerUtils.Initiator.System.name();
        }
        String initiator = null;
        if (StringUtils.isNotBlank(tenantDomain)) {
            initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
        }
        return StringUtils.isNotBlank(initiator) ? initiator : LoggerUtils.getMaskedContent(username);
    }

    /**
     * Produce a short, non-reversible fingerprint of the device public key for correlation.
     *
     * @param publicKey Base64 encoded public key.
     * @return Truncated SHA-256 fingerprint, or a placeholder if hashing is unavailable.
     */
    private String fingerprint(String publicKey) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKey.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return encoded.length() > 16 ? encoded.substring(0, 16) : encoded;
        } catch (NoSuchAlgorithmException e) {
            return FINGERPRINT_UNAVAILABLE;
        }
    }

    /**
     * Device management operations to be audited.
     */
    public enum Operation {
        REGISTER("register-device"),
        UPDATE("update-device"),
        DELETE("delete-device"),
        ACTIVATE("activate-device"),
        DEACTIVATE("deactivate-device");

        private final String logAction;

        Operation(String logAction) {

            this.logAction = logAction;
        }

        /**
         * Get the log action associated with the operation.
         *
         * @return Log action string.
         */
        public String getLogAction() {

            return this.logAction;
        }
    }

    /**
     * Device management audit log field constants.
     */
    private static class LogConstants {

        private static final String ID_FIELD = "Id";
        private static final String DEVICE_NAME_FIELD = "DeviceName";
        private static final String DEVICE_MODEL_FIELD = "DeviceModel";
        private static final String STATUS_FIELD = "Status";
        private static final String REGISTERED_AT_FIELD = "RegisteredAt";
        private static final String USER_ID_FIELD = "UserId";
        private static final String PUBLIC_KEY_FINGERPRINT_FIELD = "PublicKeyFingerprint";
    }
}
