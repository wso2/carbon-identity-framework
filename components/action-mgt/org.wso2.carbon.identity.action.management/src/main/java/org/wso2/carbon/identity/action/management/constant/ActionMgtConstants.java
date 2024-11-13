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

package org.wso2.carbon.identity.action.management.constant;

/**
 * Constants for Action management service.
 */
public class ActionMgtConstants {

    public static final String URI_ATTRIBUTE = "uri";
    public static final String AUTHN_TYPE_ATTRIBUTE = "authnType";
    public static final String IDN_SECRET_TYPE_ACTION_SECRETS = "ACTION_API_ENDPOINT_AUTH_SECRETS";

    public static final String ACTION_NAME_FIELD = "Action name";
    public static final String ENDPOINT_URI_FIELD = "Endpoint URI";
    public static final String ENDPOINT_AUTHENTICATION_TYPE_FIELD = "Endpoint authentication type";
    public static final String USERNAME_FIELD = "Username";
    public static final String PASSWORD_FIELD = "Password";
    public static final String ACCESS_TOKEN_FIELD = "Access token";
    public static final String API_KEY_HEADER_FIELD = "API key header name";
    public static final String API_KEY_VALUE_FIELD = "API key value";

    /**
     * Error messages.
     */
    public enum ErrorMessages {

        // Client errors.
        ERROR_INVALID_ACTION_TYPE("60001", "Invalid action type.",
                "Invalid action type used for path parameter."),
        ERROR_MAXIMUM_ACTIONS_PER_ACTION_TYPE_REACHED("60002", "Unable to create an Action.",
                "Maximum number of actions per action type is reached."),
        ERROR_NO_ACTION_CONFIGURED_ON_GIVEN_ACTION_TYPE_AND_ID("60003",
                "Unable to perform the operation.",
                "No Action is configured on the given Action Type and Id."),
        ERROR_EMPTY_ACTION_REQUEST_FIELD("60004", "Invalid request.",
                "%s is empty."),
        ERROR_INVALID_ACTION_REQUEST_FIELD("60005", "Invalid request.",
                "%s is invalid."),

        // Server errors.
        ERROR_WHILE_ADDING_ACTION("65001", "Error while adding Action.",
                "Error while persisting Action in the system."),
        ERROR_WHILE_ADDING_ENDPOINT_PROPERTIES("65002", "Error while adding Endpoint properties",
                "Error while persisting Action Endpoint properties in the system."),
        ERROR_WHILE_RETRIEVING_ACTION_ENDPOINT_PROPERTIES("65003", 
                "Error while retrieving Action Endpoint properties", 
                "Error while retrieving Action Endpoint properties from the system."),
        ERROR_WHILE_RETRIEVING_ACTIONS_BY_ACTION_TYPE("65004", 
                "Error while retrieving Actions by Action Type",
                "Error while retrieving Actions by Action Type from the system."),
        ERROR_WHILE_UPDATING_ENDPOINT_PROPERTIES("65005", 
                "Error while updating Action Endpoint properties",
                "Error while updating Action Endpoint properties in the system."),
        ERROR_WHILE_UPDATING_ACTION("65006", "Error while updating Action.",
                "Error while updating Action in the system."),
        ERROR_WHILE_DELETING_ACTION("65007", "Error while deleting Action.",
                "Error while deleting Action from the system."),
        ERROR_WHILE_UPDATING_ACTION_STATUS("65008", "Error while updating Action status.",
                "Error while updating Action status in the system."),
        ERROR_WHILE_RETRIEVING_ACTION_BY_ID("65009", "Error while retrieving Action by ID.",
                "Error while retrieving Action from the system."),
        ERROR_WHILE_RETRIEVING_ACTIONS_COUNT_PER_TYPE("65010",
                "Error while retrieving count of Actions per Action Type.",
                "Error while retrieving count of Actions per Action Type from the system."),
        ERROR_WHILE_RETRIEVING_ACTION_BASIC_INFO("65011", "Error while retrieving Action basic info.",
                "Error while retrieving Action basic info from the system."),
        ERROR_WHILE_DECRYPTING_ACTION_ENDPOINT_AUTH_PROPERTIES("65012",
                "Error while decrypting Action Endpoint Authentication properties",
                "Error while decrypting Action Endpoint Authentication properties in the system."),
        ERROR_NO_AUTHENTICATION_TYPE("65013",
                "Error while retrieving Action Endpoint Authentication configurations",
                "Authentication type is not defined for the Action Endpoint."),
        ERROR_WHILE_UPDATING_ACTION_BASIC_INFO("65014", "Error while updating basic Action information",
                "Error while updating basic Action information in the system.");

        private final String code;
        private final String message;
        private final String description;

        ErrorMessages(String code, String message, String description) {

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
}
