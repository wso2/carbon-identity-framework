/*
* Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.identity.compatibility.settings.core.constant;

/**
 * Constants for Identity Compatibility Settings module.
 */
public class IdentityCompatibilitySettingsConstants {

    public static final String COMPATIBILITY_SETTINGS_RESOURCE_TYPE = "compatibility-settings-v2";
    public static final String COMPATIBILITY_SETTINGS_FILE_NAME = "compatibility-settings-metadata.json";
    public static final String COMPATIBILITY_SETTINGS_FILE_PATH = "repository/conf/";
    public static final String COMPATIBILITY_SETTINGS_FILE_PATH_SEPARATOR = "/";
    public static final String COMPATIBILITY_SETTINGS_RESOURCE_PREFIX = "COMPATIBILITY_SETTINGS_RESOURCE_";


    /**
     * Private constructor to prevent instantiation.
     */
    private IdentityCompatibilitySettingsConstants() {

    }

    /**
     * Enum for error messages.
     */
    public enum ErrorMessages {

        //Server error messages.
        ERROR_CODE_FILE_NOT_FOUND("65001",
                "File not found.",
                "Compatibility settings file not found: %s"),
        ERROR_CODE_ERROR_PARSING_JSON("65002",
                "Error parsing JSON.",
                "Error occurred while parsing compatibility settings JSON from file: %s"),
        ERROR_CODE_ERROR_READING_COMPATIBILITY_SETTINGS_CONFIG("65003",
                "Error while reading compatibility setting configurations.",
                "Error occurred while reading compatibility settings configuration for tenant: %s"),
        ERROR_CODE_ERROR_UPDATING_COMPATIBILITY_SETTINGS("65004",
                "Error while updating compatibility settings.",
                "Error occurred while updating compatibility settings for tenant: %s"),
        ERROR_CODE_ERROR_CREATING_RESOURCE_TYPE("65005",
                "Error while creating resource type.",
                "Error occurred while creating resource type: %s for tenant: %s."),
        ERROR_CODE_COMPATIBILITY_SETTING_MANAGER_NOT_INITIALIZED("65006",
                "Compatibility setting manager not initialized.",
                "Compatibility setting manager is not initialized."),
        ERROR_CODE_SUPPORTED_SETTINGS_NOT_CONFIGURED("65007",
                "Supported settings not configured.",
                "Supported settings configuration is missing or empty."),
        ERROR_CODE_ERROR_RETRIEVING_ORGANIZATION_CREATION_TIME("65008",
                "Error while retrieving organization creation time.",
                "Error occurred while retrieving organization creation time for tenant: %s"),
        ERROR_CODE_ERROR_PARSING_DATE_TIME("65009",
                "Error parsing date time.",
                "Error occurred while parsing date time value from file: %s"),

        //Client error messages.
        ERROR_CODE_INVALID_SETTING_GROUP_NAME("60001",
                "Invalid setting group name.",
                "Setting group name cannot be null or empty."),
        ERROR_CODE_UNSUPPORTED_SETTING_GROUP("60002",
                "Unsupported setting group.",
                "The setting group '%s' is not supported."),
        ERROR_CODE_UNSUPPORTED_SETTING("60003",
                "Unsupported setting.",
                "The setting '%s' is not supported in setting group '%s'."),
        ERROR_CODE_INVALID_COMPATIBILITY_SETTING("60004",
                "Invalid compatibility setting.",
                "Compatibility setting cannot be null or empty."),
        ERROR_CODE_INVALID_COMPATIBILITY_SETTING_GROUP("60005",
                "Invalid compatibility setting group.",
                "Compatibility setting group cannot be null or empty.");

        private static final String ERROR_PREFIX = "ICS";
        private final String code;
        private final String message;
        private final String description;

        ErrorMessages(String code, String message, String description) {

            this.code = ERROR_PREFIX + "-" + code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {

            return code;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }

        @Override
        public String toString() {

            return code + ":" + message;
        }
    }
}

