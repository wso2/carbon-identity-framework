/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine;

/**
 * Constants for the graph executor.
 */
public class Constants {

    // Constants for node status.
    public static final String STATUS_COMPLETE = "COMPLETE";
    public static final String STATUS_INCOMPLETE = "INCOMPLETE";
    public static final String STATUS_PROMPT_ONLY = "PROMPT_ONLY";
    // Constants for user attributes.
    public static final String USERNAME_CLAIM_URI = "http://wso2.org/claims/username";
    public static final String PASSWORD_KEY = "password";
    // Constants for DataDTO parameters.
    public static final String REDIRECT_URL = "redirectUrl";
    public static final String VALIDATIONS = "validations";
    public static final String IDENTIFIER = "identifier";
    public static final String ERROR = "error";
    // Constants for self registration configurations.
    public static final String SELF_REGISTRATION_DEFAULT_USERSTORE_CONFIG = "SelfRegistration.DefaultUserStore";

    private Constants() {

    }

    /**
     * Constants for executor status.
     */
    public static class ExecutorStatus {

        public static final String STATUS_USER_INPUT_REQUIRED = "USER_INPUT_REQUIRED";
        public static final String STATUS_EXTERNAL_REDIRECTION = "EXTERNAL_REDIRECTION";
        public static final String STATUS_USER_CREATED = "USER_CREATED";
        public static final String STATUS_COMPLETE = "COMPLETE";
        public static final String STATUS_RETRY = "RETRY";
        public static final String STATUS_ERROR = "ERROR";
        public static final String STATUS_USER_ERROR = "USER_ERROR";
        // User store manager related statuses.
        public static final String USER_ALREADY_EXISTING_USERNAME = "UserAlreadyExistingUsername";

        private ExecutorStatus() {

        }
    }

    /**
     * Enum for error messages.
     */
    public enum ErrorMessages {

        // Server errors.
        ERROR_CODE_REG_FLOW_NOT_FOUND("65001",
                "Registration flow not defined.",
                "Registration flow is not defined for tenant: %s"),
        ERROR_CODE_FIRST_NODE_NODE_FOUND("65002",
                "First node not found.",
                "First node not found in the registration flow: %s of tenant: %s"),
        ERROR_CODE_UNSUPPORTED_NODE("65003",
                "Unsupported node type.",
                "Unsupported node type %s found in the registration flow: %s of tenant: %s"),
        ERROR_CODE_EXECUTOR_NOT_FOUND("65004",
                "Executor data not found.",
                "Executor data not found for node: %s of registration flow: %s of tenant: %s"),
        ERROR_CODE_UNSUPPORTED_EXECUTOR("65005",
                "Unsupported executor type.",
                "Unsupported executor type %s found in the registration flow: %s of tenant: " +
                        "%s"),
        ERROR_CODE_UNSUPPORTED_EXECUTOR_STATUS("65006",
                "Unsupported executor status.",
                "Unsupported executor status %s."),
        ERROR_CODE_TENANT_RESOLVE_FAILURE("65007",
                "Error while resolving tenant id.",
                "Unexpected server error occurs while resolving tenant id for the given " +
                        "tenant domain: %s"),
        ERROR_CODE_USER_ONBOARD_FAILURE("65008",
                "Error while onboarding user.",
                "Error occurred while onboarding user: %s in the registration request of flow" +
                        " id: %s"),
        ERROR_CODE_USERSTORE_MANAGER_FAILURE("65009",
                "Error while loading the userstore manager.",
                "Error occurred loading the userstore manager of tenant: %s while serving the" +
                        " registration request of flow id: %s."),
        ERROR_CODE_GET_DEFAULT_REG_FLOW_FAILURE("65010",
                "Error while loading the registration flow.",
                "Error occurred loading the default registration flow of tenant: %s."),
        ERROR_CODE_GET_IDP_CONFIG_FAILURE("65011",
                "Error while loading identity provider configurations.",
                "Error occurred loading the configurations of identity provider: %s " +
                        "of tenant: %s."),
        ERROR_CODE_GET_INPUT_VALIDATION_CONFIG_FAILURE("65012",
                "Error while loading input validation configurations.",
                "Error occurred loading the input validation configurations of tenant: %s."),
        ERROR_CODE_EXECUTOR_FAILURE("65013",
                "An error occurred during executor processing.",
                "%s"),

        // Client errors.
        ERROR_CODE_INVALID_FLOW_ID("60001",
                "Invalid flow id.",
                "The given flow id: %s is invalid."),
        ERROR_CODE_USERNAME_NOT_PROVIDED("60002",
                "Username not provided.",
                "Username is not provided in the registration request of flow id: %s"),
        ERROR_CODE_USERNAME_ALREADY_EXISTS("60003",
                "Username already exists.",
                "The provided username already exists in the tenant: %s"),
        ERROR_CODE_UNDEFINED_FLOW_ID("60004",
                "Flow id is not defined.",
                "The flow id is not defined in the registration request."),
        ERROR_CODE_INVALID_USERNAME("60005",
                "Invalid username.",
                "The given username: %s must be an email address."),
        ERROR_CODE_REGISTRATION_FAILURE("60006",
                "Registration failed.",
                "%s"),
        ERROR_CODE_REQUEST_PROCESSING_FAILURE("60007",
                "Error while processing the registration request.",
                "%s"),
        ;

        private static final String ERROR_PREFIX = "RFE";
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
