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

package org.wso2.carbon.identity.device.registration.internal.constant;

/**
 * Device registration error messages.
 */
public enum ErrorMessage {

    ERROR_REGISTRATION_CONTEXT_NOT_FOUND("DR-60001", "Registration context not found.",
            "No pending registration found for registration ID: %s. The context may have expired."),
    ERROR_INVALID_DEVICE_SIGNATURE("DR-60002", "Invalid device signature.",
            "The device signature verification failed for registration ID: %s."),
    ERROR_USER_NOT_IDENTIFIED("DR-60003", "User not identified.",
            "Cannot initiate device registration: no authenticated user found in the flow context."),
    ERROR_DEVICE_POLICY_NOT_COMPLIANT("DR-60004", "Device not compliant.",
            "Device does not comply with policy '%s'. Failed fields: %s."),
    ERROR_DEVICE_DATA_REQUIRED("DR-60005", "Device data required.",
            "A compliance policy is configured for this executor but no device data was submitted. " +
                    "Send device attributes as a JSON object under the 'deviceData' key."),
    ERROR_USER_NOT_AUTHENTICATED("DR-60006", "User not authenticated.",
            "The user must be authenticated earlier in the flow before a device can be registered."),
    ERROR_INVALID_QR_CODE("DR-60007", "Invalid or expired QR code.",
            "The QR code to register the device is invalid, has expired or has already been used."),
    ERROR_QR_CODE_LIMIT_REACHED("DR-60008", "Too many QR codes.",
            "The maximum number of QR codes to register a device in this step has been reached."),
    ERROR_INVALID_PAIRING_CODE("DR-60009", "Invalid pairing code.",
            "The pairing code entered does not match the code shown on the device too many times."),
    ERROR_INVALID_DEVICE_MODEL("DR-60010", "Invalid device model.",
            "The device model must not be longer than %s characters."),

    ERROR_WHILE_VERIFYING_SIGNATURE("DR-65001", "Error while verifying device signature.",
            "An unexpected error occurred during signature verification for registration ID: %s."),
    ERROR_WHILE_EVALUATING_POLICY("DR-65002", "Error while evaluating device policy.",
            "An error occurred while evaluating policy '%s'."),
    ERROR_WHILE_BUILDING_QR_CODE_DATA("DR-65003", "Error while building the QR code data.",
            "An error occurred while building the QR code data to register another device."),
    ERROR_INVALID_PHASE("DR-65004", "Invalid device registration phase.",
            "The device registration step cannot run the phase: %s."),
    ERROR_INVALID_PHASE_ORDER("DR-65005", "Invalid order of the device registration phases.",
            "The %s phase ran before the preceding device registration phases completed.");

    private final String code;
    private final String message;
    private final String description;

    ErrorMessage(String code, String message, String description) {

        this.code = code;
        this.message = message;
        this.description = description;
    }

    /**
     * Returns the error code.
     *
     * @return Error code.
     */
    public String getCode() {

        return code;
    }

    /**
     * Returns the high-level error message.
     *
     * @return Error message.
     */
    public String getMessage() {

        return message;
    }

    /**
     * Returns the detailed error description.
     *
     * @return Error description.
     */
    public String getDescription() {

        return description;
    }
}
