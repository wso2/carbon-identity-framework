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

package org.wso2.carbon.identity.application.mgt.ai.constant;

/**
 * Constants for the LoginFlowAI module.
 */
public class LoginFlowAIConstants {

    private LoginFlowAIConstants() {

    }

    public static final String OPERATION_ID_PROPERTY = "operation_id";
    public static final String USER_CLAIM_PROPERTY = "user_claims";
    public static final String USER_QUERY_PROPERTY = "user_query";
    public static final String AUTHENTICATORS_PROPERTY = "available_authenticators";

    /**
     * Enums for error messages.
     */
    public enum ErrorMessages {

        CLIENT_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE("AILF-10008", "Client error occurred " +
                "for %s tenant while generating authentication sequence."),
        SERVER_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE("AILF-10009", "Server error occurred " +
                "for %s tenant while generating authentication sequence.");

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
