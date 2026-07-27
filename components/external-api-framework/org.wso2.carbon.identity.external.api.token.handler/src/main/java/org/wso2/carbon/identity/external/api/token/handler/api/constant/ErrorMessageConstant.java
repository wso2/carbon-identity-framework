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

package org.wso2.carbon.identity.external.api.token.handler.api.constant;

/**
 * Enum for error messages for token response component.
 */
public class ErrorMessageConstant {

    /**
     * Enum for error messages.
     */
    public enum ErrorMessage {

        ERROR_CODE_MISSING_GRANT_TYPE("TOKENMGT-65001", "Missing grant type.",
                "The grant type must be provided for the GrantContext builder."),
        ERROR_CODE_BLANK_AUTH_PROPERTY("TOKENMGT-65002", "Blank grant context property value.",
                "The property %s cannot be blank or empty."),
        ERROR_CODE_MISSING_AUTH_PROPERTY("TOKENMGT-65003", "Missing grant context property.",
                "The property %s must be included as an authentication property."),
        ERROR_CODE_INVALID_PAYLOAD("TOKENMGT-65004", "Invalid grant payload.",
                "The %s payload must be non-blank payload."),
        ERROR_CODE_MISSING_REQUEST_FIELD("TOKENMGT-65005", "Missing token request field.",
                "The field %s must be included in the token request context."),
        ERROR_CODE_BUILDING_API_REQUEST("TOKENMGT-65006", "Error while APIRequestContext building.",
                "Error occurred while building APIRequestContext from the TokenRequestContext."),
        ERROR_CODE_BUILDING_API_AUTH("TOKENMGT-65007", "Error while APIAuthentication building.",
                "Error occurred while building APIAuthentication from the GrantContext."),
        ERROR_CODE_UNSUPPORTED_GRANT_TYPE("TOKENMGT-65008", "Unsupported grant type.",
                "Unsupported authentication type: %s."),
        ERROR_CODE_UNINITIALIZED_TOKEN_REQUEST("TOKENMGT-65009", "Uninitialized TokenRequestContext.",
                "The TokenRequestContext is not initialized."),
        ERROR_CODE_GETTING_ACCESS_TOKEN("TOKENMGT-65010", "Error while getting access token.",
                "Error occurred while getting access token from %s grant type."),
        ERROR_CODE_NULL_REFRESH_TOKEN_PROVIDED("TOKENMGT-65011", "Refresh token is null.",
                "Error occurred while getting access token from refresh grant type. " +
                        "Refresh token cannot be null or empty."),
        ERROR_CODE_UNEXPECTED_STATUS_CODE("TOKENMGT-65012", "Unexpected response status code.",
                "Unexpected response status code: %s. Expected: 200."),
        ERROR_CODE_NULL_API_RESPONSE("TOKENMGT-65013", "Null response received.",
                "The API response from the token endpoint is null."),
        ERROR_CODE_NULL_TOKEN_RESPONSE("TOKENMGT-65014", "Null token response.",
                "The Token response body is null. The %s token will not be set."),
        ERROR_CODE_INVALID_TOKEN_RESPONSE("TOKENMGT-65015", "Invalid token response.",
                "Failed to parse response body as JSON. The %s token will not be set."),
        ERROR_CODE_INVALID_JSON_OBJECT("TOKENMGT-65016", "Invalid JSON response body.",
                "Response body contains invalid JSON object. The %s token will not be set."),
        ERROR_CODE_TOKEN_NOT_FOUND("TOKENMGT-65017", "The token not found.",
                "The %s token is not found from the token response.");

        private final String code;
        private final String message;
        private final String description;

        /**
         * Constructor for ErrorMessage enum.
         *
         * @param code        Error code.
         * @param message     Error message.
         * @param description Error description.
         */
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
