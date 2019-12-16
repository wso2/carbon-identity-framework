/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.util;

/**
 * This class holds the constants related to session management.
 */
public class SessionMgtConstants {

    public static final String USER_AGENT = "User Agent";
    public static final String LAST_ACCESS_TIME = "Last Access Time";
    public static final String IP_ADDRESS = "IP";
    public static final String LOGIN_TIME = "Login Time";

    public enum ErrorMessages {

        ERROR_CODE_UNABLE_TO_GET_SESSION("USM-15001",
                "Unable to retrieve session information",
                "Server encountered an error while retrieving session information."),
        ERROR_CODE_UNABLE_TO_GET_SESSIONS("USM-15002",
                "Unable to retrieve sessions",
                "Server encountered an error while retrieving session list of user, %s."),
        ERROR_CODE_UNABLE_TO_AUTHORIZE_USER("USM-15006",
                "Unable to validate user",
                "Server encountered an error while authorizing user, %s."),
        ERROR_CODE_FORBIDDEN_ACTION("USM-10007",
                "Action forbidden",
                "User is not authorized to terminate this session."),
        ERROR_CODE_INVALID_USER("USM-10008",
                "Invalid user",
                "User is not provided to perform session management tasks."),
        ERROR_CODE_INVALID_SESSION("USM-10009",
                "Invalid session",
                "Session ID is not provided to perform session termination.");

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

        @Override
        public String toString() {
            return code + " - " + message;
        }

    }
}
