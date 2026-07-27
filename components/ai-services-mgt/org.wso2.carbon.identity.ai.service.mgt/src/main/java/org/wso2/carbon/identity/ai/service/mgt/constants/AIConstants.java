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

package org.wso2.carbon.identity.ai.service.mgt.constants;

/**
 * Constants for the LoginFlowAI module.
 */
public class AIConstants {

    private AIConstants () {

    }

    public static final String AI_SERVICE_KEY_PROPERTY_NAME = "AIServices.Key";
    public static final String AI_TOKEN_ENDPOINT_PROPERTY_NAME = "AIServices.TokenEndpoint";
    public static final String AI_TOKEN_SERVICE_MAX_RETRIES_PROPERTY_NAME = "AIServices.TokenRequestMaxRetries";
    public static final String AI_TOKEN_CONNECTION_TIMEOUT_PROPERTY_NAME = "AIServices.TokenConnectionTimeout";
    public static final String AI_TOKEN_CONNECTION_REQUEST_TIMEOUT_PROPERTY_NAME = "AIServices" +
            ".TokenConnectionRequestTimeout";
    public static final String AI_TOKEN_SOCKET_TIMEOUT_PROPERTY_NAME = "AIServices.TokenConnectionSocketTimeout";

    public static final String HTTP_CONNECTION_POOL_SIZE_PROPERTY_NAME = "AIServices.HTTPConnectionPoolSize";
    public static final String HTTP_CONNECTION_TIMEOUT_PROPERTY_NAME = "AIServices.HTTPConnectionTimeout";
    public static final String HTTP_CONNECTION_REQUEST_TIMEOUT_PROPERTY_NAME = "AIServices" +
            ".HTTPConnectionRequestTimeout";
    public static final String HTTP_SOCKET_TIMEOUT_PROPERTY_NAME = "AIServices.HTTPSocketTimeout";

    // Http constants.
    public static final String HTTP_BASIC = "Basic";
    public static final String HTTP_BEARER = "Bearer";
    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";

    // Access Token response constants.
    public static final String ACCESS_TOKEN_KEY = "access_token";

    public static final String TENANT_CONTEXT_PREFIX = "/t/";

    // Default Property values.
    public static final int DEFAULT_TOKEN_REQUEST_MAX_RETRIES = 3;
    public static final int DEFAULT_TOKEN_CONNECTION_TIMEOUT = 3000;
    public static final int DEFAULT_TOKEN_CONNECTION_REQUEST_TIMEOUT = 3000;
    public static final int DEFAULT_TOKEN_SOCKET_TIMEOUT = 3000;

    public static final int DEFAULT_HTTP_CONNECTION_POOL_SIZE = 20;
    public static final int DEFAULT_HTTP_CONNECTION_TIMEOUT = 3000;
    public static final int DEFAULT_HTTP_CONNECTION_REQUEST_TIMEOUT = 3000;
    public static final int DEFAULT_HTTP_SOCKET_TIMEOUT = 3000;

    /**
     * Enums for error messages.
     */
    public enum ErrorMessages {

        MAXIMUM_RETRIES_EXCEEDED("AI-10000", "Maximum retries exceeded to retrieve the access token."),
        UNABLE_TO_ACCESS_AI_SERVICE_WITH_RENEW_ACCESS_TOKEN("AI-10003", "Unable to access the " +
                "AI service with the renewed access token."),
        REQUEST_TIMEOUT("AI-10004", "Request to the AI service timed out."),
        ERROR_RETRIEVING_ACCESS_TOKEN("AI-10007", "Error occurred while retrieving the " +
                "access token."),
        CLIENT_ERROR_WHILE_CONNECTING_TO_AI_SERVICE("AI-10008", "Client error occurred " +
                "for %s tenant while connecting to AI service."),
        SERVER_ERROR_WHILE_CONNECTING_TO_AI_SERVICE("AI-10009", "Server error occurred " +
                "for %s tenant while connecting to AI service.");

        private final String code;
        private final String message;

        ErrorMessages(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return code;
        }

        public String getMessage() {

            return message;
        }

        @Override
        public String toString() {

            return code + ":" + message;
        }
    }
}
