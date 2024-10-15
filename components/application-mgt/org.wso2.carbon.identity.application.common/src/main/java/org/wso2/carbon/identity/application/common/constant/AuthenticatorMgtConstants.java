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

package org.wso2.carbon.identity.application.common.constant;

public class AuthenticatorMgtConstants {

    /**
     * Error messages.
     */
    public enum ErrorMessages {

        // Client errors.
        ERROR_NOT_FOUND_AUTHENTICATOR("60001", "No Authenticator is found.",
                "No authenticator is found by given authenticator name: %s."),
        ERROR_OP_ON_SYSTEM_AUTHENTICATOR("60002", "No operations allowed",
                "Do not allow to perform any operation on system defined authenticator: %s."),

        // Server errors.
        ERROR_WHILE_ADDING_AUTHENTICATOR("65001", "Error while adding authenticator.",
                "Error while persisting authenticator in the system."),
        ERROR_WHILE_UPDATING_AUTHENTICATOR("65002", "Error while updating authenticator.",
                "Error while updating authenticator in the system."),
        ERROR_WHILE_RETRIEVING_AUTHENTICATOR_BY_NAME("65003", "Error while retrieving authenticator.",
                "Error while retrieving authenticator in the system."),
        ERROR_WHILE_DELETING_AUTHENTICATOR("65004", "Error while deleting authenticator.",
                "Error while deleting authenticator in the system."),;

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
