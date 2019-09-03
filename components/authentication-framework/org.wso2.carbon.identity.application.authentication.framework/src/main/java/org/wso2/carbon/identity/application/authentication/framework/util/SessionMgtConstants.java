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

        ERROR_CODE_UNABLE_TO_GET_SESSION("USM-00001", "Unable to retrieve session information"),
        ERROR_CODE_UNABLE_TO_GET_SESSIONS("USM-00002", "Unable to retrieve sessions"),
        ERROR_CODE_UNABLE_TO_AUTHORIZE_USER("USM-00006", "Unable to validate user"),
        ERROR_CODE_FORBIDDEN_ACTION("USM-00007", "Action forbidden");

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
            return code + " - " + message;
        }

    }
}
