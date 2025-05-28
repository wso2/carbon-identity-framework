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

package org.wso2.carbon.identity.webhook.metadata.api.constant;

/**
 * Error messages for webhook metadata.
 */
public enum ErrorMessage {

    // Client errors (61xxx range)
    ERROR_CODE_PROFILE_NOT_FOUND("WEBHOOKMETA-61001", "Profile not found",
            "The requested event profile %s could not be found."),

    // Server errors (66xxx range)
    ERROR_CODE_PROFILES_RETRIEVE_ERROR("WEBHOOKMETA-66001", "Error occurred while retrieving profiles",
            "An internal server error occurred while retrieving event profiles."),
    ERROR_CODE_PROFILE_RETRIEVE_ERROR("WEBHOOKMETA-66002", "Error occurred while retrieving profile",
            "An internal server error occurred while retrieving the event profile %s."),
    ERROR_CODE_EVENTS_RETRIEVE_ERROR("WEBHOOKMETA-66003", "Error occurred while retrieving events",
            "An internal server error occurred while retrieving events for the profile %s."),
    ERROR_CODE_PROFILE_FILES_LOAD_ERROR("WEBHOOKMETA-66004", "Error occurred while loading profile files",
            "An internal server error occurred while loading event profile files from the directory."),
    ERROR_CODE_DIRECTORY_NOT_FOUND("WEBHOOKMETA-66005", "Directory not found",
            "The specified directory %s does not exist or is not accessible.");

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
