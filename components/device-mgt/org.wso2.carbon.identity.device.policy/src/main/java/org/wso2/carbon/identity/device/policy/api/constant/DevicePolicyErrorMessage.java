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

package org.wso2.carbon.identity.device.policy.api.constant;

/**
 * Error messages for the Device Policy component.
 */
public enum DevicePolicyErrorMessage {

    // Client errors.
    ERROR_DEVICE_TOKEN_PARSE_FAILED("DPM-60008", "Device token invalid.",
            "Failed to parse X-Device-Token JWT."),
    ERROR_DEVICE_TOKEN_MISSING_DEVICE_ID("DPM-60009", "Device token invalid.",
            "deviceId is missing from the JWT header."),
    ERROR_DEVICE_TOKEN_SIGNATURE_INVALID("DPM-60011",
            "Device token signature invalid.",
            "JWT signature verification failed for deviceId: %s."),
    ERROR_DEVICE_TOKEN_MISSING_JTI("DPM-60012", "Device token invalid.",
            "jti claim is missing from the device token."),
    ERROR_DEVICE_TOKEN_MISSING_IAT("DPM-60013", "Device token invalid.",
            "iat claim is missing from the device token."),
    ERROR_DEVICE_TOKEN_EXPIRED("DPM-60014", "Device token expired.",
            "Device token is outside the allowed %s-second freshness window."),
    ERROR_DEVICE_TOKEN_REPLAYED("DPM-60015", "Device token replayed.",
            "Device token with jti %s has already been used."),
    ERROR_DEVICE_NOT_ACTIVE("DPM-60016", "Device not active.",
            "The device with id: %s is not active and cannot be " +
                    "used for authentication."),

    // Server errors.
    ERROR_DEVICE_ECDSA_VERIFICATION_FAILED("DPM-65011",
            "Device token verification failed.",
            "ECDSA verification error for the device token."),
    ERROR_DEVICE_LOOKUP_FAILED("DPM-65012", "Device lookup failed.",
            "Error retrieving device from registry."),
    ERROR_DEVICE_PUBLIC_KEY_DECODE_FAILED("DPM-65013",
            "Device public key invalid.",
            "Failed to decode EC public key for the registered device."),
    ERROR_DEVICE_TOKEN_REPLAY_CHECK_FAILED("DPM-65014",
            "Device token replay check failed.",
            "Error checking the device token jti against the replay store."),
    ERROR_DEVICE_TOKEN_REPLAY_STORE_FAILED("DPM-65015",
            "Device token replay store failed.",
            "Error persisting the device token jti to the replay store."),
    ERROR_DEVICE_TOKEN_REPLAY_CLEANUP_FAILED("DPM-65016",
            "Device token replay cleanup failed.",
            "Error removing expired device token jti records from " +
                    "the replay store."),
    ERROR_DEVICE_POLICY_EVALUATION_FAILED("DPM-65020",
            "Device policy evaluation failed.",
            "Error evaluating device policy: %s.");

    private final String code;
    private final String message;
    private final String description;

    /**
     * Constructor for DevicePolicyErrorMessage.
     *
     * @param code        Error code.
     * @param message     Error message.
     * @param description Error description.
     */
    DevicePolicyErrorMessage(String code, String message, String description) {

        this.code = code;
        this.message = message;
        this.description = description;
    }

    /**
     * Get the error code.
     *
     * @return Error code.
     */
    public String getCode() {

        return code;
    }

    /**
     * Get the error message.
     *
     * @return Error message.
     */
    public String getMessage() {

        return message;
    }

    /**
     * Get the error description.
     *
     * @return Error description.
     */
    public String getDescription() {

        return description;
    }
}
