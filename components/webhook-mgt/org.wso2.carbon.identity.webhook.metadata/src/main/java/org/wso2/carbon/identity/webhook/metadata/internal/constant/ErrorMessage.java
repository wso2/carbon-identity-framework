/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.metadata.internal.constant;

/**
 * Error messages for webhook metadata.
 */
public enum ErrorMessage {

    // Client errors (61xxx range)
    // Continuation of client error codes can be found in the Webhook Metadata API layer as well.
    ERROR_INVALID_WEBHOOK_METADATA_REQUEST_FIELD("WEBHOOKMETA-60001", "Invalid request.",
            "%s is invalid."),

    // Server errors (66xxx range)
    // Continuation of server error codes can be found in the Webhook Metadata API layer as well.
    ERROR_CODE_PROFILES_RETRIEVE_ERROR("WEBHOOKMETA-66001", "Error occurred while retrieving profiles",
            "An internal server error occurred while retrieving event profiles."),
    ERROR_CODE_PROFILE_RETRIEVE_ERROR("WEBHOOKMETA-66002", "Error occurred while retrieving profile",
            "An internal server error occurred while retrieving the event profile %s."),
    ERROR_CODE_EVENTS_RETRIEVE_ERROR("WEBHOOKMETA-66003", "Error occurred while retrieving events",
            "An internal server error occurred while retrieving events for the profile %s."),
    ERROR_CODE_PROFILE_FILES_LOAD_ERROR("WEBHOOKMETA-66004", "Error occurred while loading profile files",
            "An internal server error occurred while loading event profile files from the directory."),
    ERROR_CODE_DIRECTORY_NOT_FOUND("WEBHOOKMETA-66005", "Directory not found",
            "The specified directory %s does not exist or is not accessible."),
    ERROR_CODE_CONFIG_FILE_NOT_FOUND("WEBHOOKMETA-66006", "Configuration file not found",
            "The configuration file %s does not exist or is not accessible."),
    ERROR_CODE_CONFIG_FILE_READ_ERROR("WEBHOOKMETA-66007", "Error reading configuration file",
            "An error occurred while reading the configuration file %s. Please check the file format and permissions."),
    ERROR_CODE_NO_ENABLED_ADAPTER("WEBHOOKMETA-66008", "No enabled adapter found",
            "No enabled adapter found in the system."),
    ERROR_CODE_ADAPTERS_RETRIEVE_ERROR("WEBHOOKMETA-66009", "Error occurred while retrieving adapters",
            "An internal server error occurred while retrieving the event adapters."),
    ERROR_CODE_ENABLED_ADAPTER_RETRIEVE_ERROR("WEBHOOKMETA-66010", "Error occurred while retrieving enabled adapter",
            "An internal server error occurred while retrieving enabled event adapter."),
    ERROR_CODE_ADAPTER_NOT_FOUND("WEBHOOKMETA-66011", "Adapter not found",
            "The specified adapter %s does not exist or is not accessible."),
    ERROR_CODE_WEBHOOK_METADATA_UPDATE_ERROR("WEBHOOKMETA-66012", "Error occurred while updating webhook metadata",
            "An internal server error occurred while updating the webhook metadata properties.");

    private final String code;
    private final String message;
    private final String description;

    ErrorMessage(String code, String message, String description) {

        this.code = code;
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

        return code + " : " + message;
    }
}
