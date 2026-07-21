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

package org.wso2.carbon.identity.device.mgt.api.constant;

/**
 * Device management error messages.
 */
public enum ErrorMessage {

    ERROR_DEVICE_NOT_FOUND("DM-60001", "Device not found.",
            "No registered device found for the given device id: %s."),
    ERROR_INVALID_DEVICE_FIELD("DM-60002", "Invalid request.",
            "%s is empty or invalid."),

    ERROR_WHILE_REGISTERING_DEVICE("DM-65001", "Error while registering device.",
            "Error while registering device in the system."),
    ERROR_WHILE_RETRIEVING_DEVICE("DM-65002", "Error while retrieving device.",
            "Error while retrieving device from the system."),
    ERROR_WHILE_UPDATING_DEVICE("DM-65003", "Error while updating device.",
            "Error while updating device in the system."),
    ERROR_WHILE_DELETING_DEVICE("DM-65004", "Error while deleting device.",
            "Error while deleting device from the system."),
    ERROR_USER_ID_REQUIRED("DM-65007", "User identifier required.",
            "Cannot register device: a valid user identifier (userId) was not set before registration."),
    ERROR_DEVICE_FIELD_REQUIRED("DM-65008", "Required device field missing.",
            "Cannot register device: the required field '%s' was not set before registration.");

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
