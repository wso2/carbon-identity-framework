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

package org.wso2.carbon.identity.external.api.client.api.constant;

/**
 * Error message constants for API Client component.
 */
public class ErrorMessageConstant {

    /**
     * Enum for error messages.
     */
    public enum ErrorMessage {

        ERROR_CODE_MISSING_AUTH_TYPE("APICLIENT-65001", "Missing authentication type.",
                "Authentication type must be provided in the API authentication configuration."),
        ERROR_CODE_BLANK_AUTH_PROPERTY("APICLIENT-65002", "Blank API authentication property value.",
                "The property %s cannot be blank or empty."),
        ERROR_CODE_MISSING_AUTH_PROPERTY("APICLIENT-65003", "Missing API authentication property.",
                "The property %s must be included as an authentication property."),
        ERROR_CODE_MISSING_REQUEST_FIELD("APICLIENT-65004", "Missing API request context field.",
                "The field %s must be included as an API request context."),
        ERROR_CODE_UNSUPPORTED_HTTP_METHOD ("APICLIENT-65005", "Unsupported HTTP method.",
                "The HTTP method %s is not supported."),
        ERROR_CODE_WHILE_INVOKING_API ("APICLIENT-65006", "Error while invoking external API.",
                "An error occurred while invoking the external API: %s"),
        ERROR_CODE_INVALID_CONFIG_VALUE ("APICLIENT-65007", "Invalid API client configuration.",
                "The API client configuration value %s must be a positive integer."),
        ERROR_CODE_INVALID_RETRY_COUNT ("APICLIENT-65008", "Invalid retry count.",
                "The allowed retry count %s must be non-negative."),
        ERROR_CODE_INVALID_AUTH_PROPERTY("APICLIENT-65009", "Invalid API authentication property key or value.",
                "The property key must be not blank and property value must be not null."),
        ERROR_CODE_NULL_API_DATA("APICLIENT-65010", "Invalid data for API invocation.",
                "The APIRequestContext and APIClientConfig cannot be null to invoke an external API endpoint."),
        ERROR_CODE_NON_REPEATABLE_ENTITY("APICLIENT-65011", "Non‑repeatable HttpEntity.",
                "Non-repeatable HttpEntity is not supported due to request retry requirements.");

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

            return code + " | " + message;
        }
    }
}
