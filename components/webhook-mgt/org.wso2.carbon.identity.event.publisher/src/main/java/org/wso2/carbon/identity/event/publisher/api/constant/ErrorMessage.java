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

package org.wso2.carbon.identity.event.publisher.api.constant;

/**
 * Error messages for event publisher.
 */
public enum ErrorMessage {

    // Client errors (6xxxx range)

    // Server errors (65xxx range)
    ERROR_CODE_EVENT_PUBLISHER_NOT_FOUND("EVENTPUBLISHER-65001", "Event publisher not found.",
            "No event publisher is found for the given event publisher name: %s."),
    ERROR_CODE_CONSTRUCTING_HUB_TOPIC("EVENTPUBLISHER-65002", "Error constructing hub topic.",
            "Error constructing hub topic for the event publisher: %s. " +
                    "Please check the event publisher configuration."),
    ERROR_CODE_TOPIC_EXISTS_CHECK("EVENTPUBLISHER-65003", "Error checking topic existence.",
            "Error checking topic existence for the event publisher: %s."),
    CONFIG_FILE_NOT_FOUND("EVENTPUBLISHER-65004", "Configuration file not found.",
            "%s configuration file doesn't exist."),
    CONFIG_FILE_RETRIEVAL_ERROR("EVENTPUBLISHER-65005", "Error while retrieving the configuration file.",
            "Error while retrieving the configuration file: %s."),
    CONFIG_FILE_PERMISSION_DENIED("EVENTPUBLISHER-65006", "Permission denied while accessing the configuration file.",
            "Permission denied while accessing the configuration file: %s.");

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
