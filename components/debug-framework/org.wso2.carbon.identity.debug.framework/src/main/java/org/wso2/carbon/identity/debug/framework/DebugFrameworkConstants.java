/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework;

/**
 * Constants for the Debug Framework.
 */
public final class DebugFrameworkConstants {

    // Debug Flow Identification Constants.
    public static final String DEBUG_IDENTIFIER_PARAM = "isDebugFlow";
    public static final String SESSION_DATA_KEY_PARAM = "sessionDataKey";

    // Default Constants.
    public static final int CACHE_EXPIRY_MINUTES = 5;

    // Debug Result Context Properties.
    public static final String DEBUG_DIAGNOSTICS = "diagnostics";

    // Diagnostic event property keys.
    public static final String DIAGNOSTIC_STAGE = "stage";
    public static final String DIAGNOSTIC_STATUS = "status";
    public static final String DIAGNOSTIC_MESSAGE = "message";
    public static final String DIAGNOSTIC_DETAILS = "details";
    public static final String DIAGNOSTIC_TIMESTAMP = "timestamp";

    // Common Parameter Values.
    public static final String DEBUG_PREFIX = "debug-";
    public static final String TRUE_VALUE = Boolean.TRUE.toString();
    public static final String FALSE_VALUE = Boolean.FALSE.toString();

    // Database Column Names.
    public static final String DB_COLUMN_STATUS = "STATUS";
    public static final String DB_COLUMN_SESSION_DATA = "SESSION_DATA";
    public static final String DB_COLUMN_RESULT_JSON = "RESULT_JSON";
    public static final String DB_COLUMN_CREATED_TIME = "CREATED_TIME";
    public static final String DB_COLUMN_EXPIRY_TIME = "EXPIRY_TIME";

    // Debug Status Constants.
    public static final String DEBUG_STATUS_FAILURE = "FAILURE";
    public static final String DEBUG_STATUS_SUCCESS = "SUCCESS";
    public static final String DEBUG_STATUS_SUCCESS_INCOMPLETE = "SUCCESS_INCOMPLETE";
    public static final String DEBUG_STATUS_SUCCESS_COMPLETE = "SUCCESS_COMPLETE";

    // Callback parameter names.
    public static final String CALLBACK_STATE_PARAM = "state";
    public static final String CALLBACK_CODE_PARAM = "code";
    public static final String CALLBACK_ERROR_PARAM = "error";
    public static final String UNKNOWN_DEBUG_STATE = "unknown";

    // Debug Result Context Properties.
    public static final String DEBUG_AUTH_ERROR = "DEBUG_AUTH_ERROR";
    public static final String DEBUG_AUTH_SUCCESS = "DEBUG_AUTH_SUCCESS";
    public static final String DEBUG_PROTOCOL_CODE = "DEBUG_PROTOCOL_CODE";
    public static final String DEBUG_PROTOCOL_STATE = "DEBUG_PROTOCOL_STATE";
    public static final String DEBUG_SESSION_DATA_KEY = "DEBUG_SESSION_DATA_KEY";
    public static final String DEBUG_CALLBACK_TIMESTAMP = "DEBUG_CALLBACK_TIMESTAMP";
    public static final String DEBUG_CALLBACK_PROCESSED = "DEBUG_CALLBACK_PROCESSED";

    // Debug Flow Type Constants.
    public static final String DEBUG_FLOW_TYPE = "DEBUG_FLOW_TYPE";
    public static final String DEBUG_CONTEXT_CREATED = "DEBUG_CONTEXT_CREATED";
    public static final String DEBUG_CREATION_TIMESTAMP = "DEBUG_CREATION_TIMESTAMP";
    public static final String FLOW_TYPE_CALLBACK = "CALLBACK";
    public static final String DEBUG_RESULT_RETRIEVAL = "DEBUG_RESULT_RETRIEVAL";

    private DebugFrameworkConstants() {
        // Private constructor to prevent instantiation.
    }

    /**
     * Enum for error messages in the Debug Framework.
     */
    public enum ErrorMessages {

        // Client errors (60xxx).
        ERROR_CODE_INVALID_REQUEST("60001",
                "Invalid request.",
                "The debug request is null or invalid."),
        ERROR_CODE_MISSING_RESOURCE_TYPE("60002",
                "Resource type is required.",
                "The resource type must be provided in the debug request."),
        ERROR_CODE_MISSING_RESOURCE_ID("60003",
                "Resource ID is required.",
                "The resource ID must be provided in the debug request."),
        ERROR_CODE_HANDLER_NOT_FOUND("60004",
                "Handler not found.",
                "No handler available for resource type: %s"),
        ERROR_CODE_EXECUTOR_NOT_FOUND("60005",
                "Executor not found.",
                "No debug executor found for the given resource: %s"),
        ERROR_CODE_PROCESSOR_NOT_FOUND("60010",
                "Processor not found.",
                "No debug processor found for the given resource: %s"),
        ERROR_CODE_CONTEXT_PROVIDER_NOT_FOUND("60006",
                "Context provider not found.",
                "Context provider not available for resource: %s"),
        ERROR_CODE_CONTEXT_RESOLUTION_FAILED("60007",
                "Context resolution failed.",
                "Unable to resolve debug context for resource: %s"),
        ERROR_CODE_LISTENER_ABORTED("60008",
                "Request aborted by listener.",
                "Debug request aborted by %s listener."),
        ERROR_CODE_RESULT_NOT_FOUND("60009",
                "Result not found.",
                "Result not found for session: %s"),

        // Server errors (65xxx).
        ERROR_CODE_SERVER_ERROR("65001",
                "Server error.",
                "Server error processing debug request."),
        ERROR_CODE_EXECUTION_FAILED("65002",
                "Execution failed.",
                "Debug execution failed: %s");

        private static final String ERROR_PREFIX = "DSM";
        private final String code;
        private final String message;
        private final String description;

        ErrorMessages(String code, String message, String description) {

            this.code = ERROR_PREFIX + "-" + code;
            this.message = message;
            this.description = description;
        }

        /**
         * Gets the error code.
         *
         * @return Error code string with prefix.
         */
        public String getCode() {

            return code;
        }

        /**
         * Gets the error message.
         *
         * @return Error message string.
         */
        public String getMessage() {

            return message;
        }

        /**
         * Gets the error description.
         *
         * @return Error description string.
         */
        public String getDescription() {

            return description;
        }

        @Override
        public String toString() {

            return code + ":" + message;
        }
    }
}
