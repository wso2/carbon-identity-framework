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

    // Client errors
    PROFILE_NOT_FOUND("CLIENT-00002", "Profile not found",
            "The requested event profile could not be found."),

    // Server errors
    ERROR_RETRIEVING_PROFILE("SERVER-00002", "Error retrieving profile",
            "An error occurred while retrieving the event profile."),
    ERROR_RETRIEVING_EVENTS("SERVER-00003", "Error retrieving events",
            "An error occurred while retrieving events for the profile."),
    ERROR_LOADING_PROFILE_FILES("SERVER-00004", "Error loading profile files",
            "An error occurred while loading event profile files from the directory.");

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
