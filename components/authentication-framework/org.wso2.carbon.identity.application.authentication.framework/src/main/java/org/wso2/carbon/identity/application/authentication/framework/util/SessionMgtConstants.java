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

    // Federated authentication session details column names.
    public static final String FEDERATED_IDP_SESSION_ID = "IDP_SESSION_ID";
    public static final String FEDERATED_SESSION_ID = "SESSION_ID";
    public static final String FEDERATED_IDP_NAME = "IDP_NAME";
    public static final String FEDERATED_AUTHENTICATOR_ID = "AUTHENTICATOR_ID";
    public static final String FEDERATED_PROTOCOL_TYPE = "PROTOCOL_TYPE";
    public static final String AUDIT_MESSAGE_TEMPLATE = "Initiator : %s | Action : %s | Data : { %s } | Result : %s ";
    public static final String SESSION_CONTEXT_ID = "sessionContextId";
    public static final String REMEMBER_ME = "RememberMe";
    public static final String SUCCESS = "Success";
    public static final String AUTHENTICATED_USER = "AuthenticatedUser";
    public static final String AUTHENTICATED_USER_TENANT_DOMAIN = "AuthenticatedUserTenantDomain";
    public static final String TRACE_ID = "traceId";
    public static final String STORE_SESSION_ACTION = "StoreSession";
    public static final String UPDATE_SESSION_ACTION = "UpdateSession";
    public static final String TERMINATE_SESSION_ACTION = "TerminateSession";
    public static final String SESSION_LAST_ACCESSED_TIMESTAMP = "LastAccessedTimestamp";
    public static final String SESSION_TERMINATE_TIMESTAMP = "TerminatedTimestamp";



    /**
     * Session management error messages.
     */
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
        ERROR_CODE_UNABLE_TO_GET_FED_USER_SESSION("USM-15007",
                "Unable to retrieve federated authentication session information",
                "Server encountered an error while retrieving federated authentication session information."),
        ERROR_CODE_FORBIDDEN_ACTION("USM-10007",
                "Action forbidden",
                "User is not authorized to terminate this session."),
        ERROR_CODE_INVALID_USER("USM-10008",
                "Invalid user",
                "User is not provided to perform session management tasks."),
        ERROR_CODE_INVALID_SESSION("USM-10009",
                "Invalid session",
                "Session ID is not provided to perform session termination."),
        ERROR_CODE_UNABLE_TO_GET_APP_DATA("USM-10010",
                "SQL Error",
                "Error while retrieving application data.");

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
