/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.api.constant;

/**
 * Error messages.
 */
public enum ErrorMessage {

    // Client errors.
    ERROR_INVALID_ACTION_TYPE("60001", "Invalid action type.",
            "Invalid action type used for path parameter."),
    ERROR_MAXIMUM_ACTIONS_PER_ACTION_TYPE_REACHED("60002", "Unable to create an Action.",
            "Maximum number of actions per action type is reached."),
    ERROR_NO_ACTION_CONFIGURED_ON_GIVEN_ACTION_TYPE_AND_ID("60003", "Unable to perform the operation.",
            "No Action is configured on the given Action Type and Id."),
    ERROR_EMPTY_ACTION_REQUEST_FIELD("60004", "Invalid request.", "%s is empty."),
    ERROR_INVALID_ACTION_REQUEST_FIELD("60005", "Invalid request.", "%s is invalid."),
    ERROR_INVALID_ACTION_PROPERTIES("60006", "Provided Action Properties are invalid.", "%s"),
    ERROR_NOT_ALLOWED_HEADER("60007", "Provided Headers are not allowed.",
            "One or more provided headers are not allowed by the server."),
    ERROR_NOT_ALLOWED_PARAMETER("60008", "Provided Parameters are not allowed.",
            "One or more provided parameters are not allowed by the server."),
    ERROR_INVALID_ACTION_VERSION_UPDATE("60009", "Invalid action version update request.",
            "Only allowed to update to the latest Action version: %s. Ensure that only the major version is provided."),
    ERROR_INVALID_RULE_FOR_ACTION_VERSION("600010", "Invalid action rule for the action version.",
            "An unsupported rule has been provided for action version %s."),

    // Server errors.
    ERROR_WHILE_ADDING_ACTION("65001", "Error while adding Action.",
            "Error while persisting Action in the system."),
    ERROR_WHILE_RETRIEVING_ACTIONS_BY_ACTION_TYPE("65002",
            "Error while retrieving Actions by Action Type",
            "Error while retrieving Actions by Action Type from the system."),
    ERROR_WHILE_RETRIEVING_ACTION_BY_ID("65003", "Error while retrieving Action by ID.",
            "Error while retrieving Action from the system."),
    ERROR_WHILE_UPDATING_ACTION("65004", "Error while updating Action.",
            "Error while updating Action in the system."),
    ERROR_WHILE_DELETING_ACTION("65005", "Error while deleting Action.",
            "Error while deleting Action from the system."),
    ERROR_WHILE_ACTIVATING_ACTION("65006", "Error while activating Action.",
            "Error while updating Action status to ACTIVE."),
    ERROR_WHILE_DEACTIVATING_ACTION("65007", "Error while deactivating Action.",
            "Error while updating Action status to INACTIVE."),
    ERROR_WHILE_RETRIEVING_ACTIONS_COUNT_PER_TYPE("65008",
            "Error while retrieving count of Actions per Action Type.",
            "Error while retrieving count of Actions per Action Type from the system."),
    ERROR_WHILE_DECRYPTING_ACTION_ENDPOINT_AUTH_PROPERTIES("65009",
            "Error while decrypting Action Endpoint Authentication properties",
            "Error while decrypting Action Endpoint Authentication properties in the system.");

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
}
