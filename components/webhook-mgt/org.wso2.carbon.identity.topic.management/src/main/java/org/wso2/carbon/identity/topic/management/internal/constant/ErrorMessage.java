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

package org.wso2.carbon.identity.topic.management.internal.constant;

/**
 * Error messages for topic management.
 */
public enum ErrorMessage {

    // Client errors (6xxxx range)
    ERROR_CODE_TOPIC_NOT_FOUND("TOPICMGT-60001", "Topic not found",
            "The requested topic could not be found in the system."),
    ERROR_CODE_TOPIC_ALREADY_EXISTS("TOPICMGT-60002", "Topic already exists",
            "A topic with the given name already exists in the system."),
    ERROR_CODE_INVALID_TOPIC("TOPICMGT-60004", "Invalid topic",
            "The topic is invalid or not accessible."),
    ERROR_CODE_INVALID_CHANNEL_URI("TOPICMGT-60005", "Invalid channel URI",
            "The provided channel URI is invalid or not supported."),

    // Server errors (65xxx range)
    ERROR_CODE_TOPIC_ADD_ERROR("TOPICMGT-65001", "Error occurred while adding topic",
            "An internal server error occurred while adding the topic: %s."),
    ERROR_CODE_TOPIC_UPDATE_ERROR("TOPICMGT-65002", "Error occurred while updating topic",
            "An internal server error occurred while updating the topic: %s."),
    ERROR_CODE_TOPIC_DELETE_ERROR("TOPICMGT-65003", "Error occurred while deleting topic",
            "An internal server error occurred while deleting the topic: %s."),
    ERROR_CODE_TOPIC_GET_ERROR("TOPICMGT-65004", "Error occurred while retrieving topic",
            "An internal server error occurred while retrieving the topic: %s."),
    ERROR_CODE_TOPIC_LIST_ERROR("TOPICMGT-65005", "Error occurred while listing topics",
            "An internal server error occurred while listing topics for tenant: %s."),
    ERROR_CODE_TOPIC_CONSTRUCT_ERROR("TOPICMGT-65006", "Error occurred while constructing topic",
            "An internal server error occurred while constructing the topic from channel URI: %s."),
    ERROR_CODE_TOPIC_REGISTRATION_ERROR("TOPICMGT-65007", "Error occurred while registering topic",
            "An internal server error occurred while registering the topic: %s."),
    ERROR_CODE_TOPIC_PERSISTENCE_ERROR("TOPICMGT-65008", "Error occurred while persisting topic",
            "An internal server error occurred while persisting the topic: %s in the database."),
    ERROR_CODE_TOPIC_DEREGISTRATION_ERROR("TOPICMGT-65009", "Error occurred while deregistering topic",
            "An internal server error occurred while deregistering the topic: %s."),
    ERROR_CODE_TOPIC_DELETION_ERROR("TOPICMGT-65010", "Error occurred while deleting topic",
            "An internal server error occurred while deleting the topic: %s from database."),
    ERROR_CODE_TOPIC_EXISTS_CHECK_ERROR("TOPICMGT-65011", "Error occurred while checking if topic exists",
            "An internal server error occurred while checking if the topic exists: %s."),
    ERROR_CODE_UNEXPECTED_ERROR("TOPICMGT-65012", "Unexpected error occurred",
            "An unexpected error occurred while processing the request."),
    ERROR_CODE_TOPIC_MANAGER_NOT_FOUND("TOPICMGT-65013", "Topic manager not found",
            "No suitable topic manager found in the system for tenant: %s.");

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
