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

    // Filtering constants.
    public static final String COL_IP_ADDRESS = "IP_ADDRESS";
    public static final String FLD_IP_ADDRESS = "ipAddress";
    public static final String FLD_IP_ADDRESS_LOWERCASE = "ipaddress";
    public static final String COL_APPLICATION = "LOWER(APP_NAME)";
    public static final String FLD_APPLICATION = "appName";
    public static final String FLD_APPLICATION_LOWERCASE = "appname";
    public static final String COL_LOGIN_ID = "LOWER(SUBJECT)";
    public static final String FLD_LOGIN_ID = "loginId";
    public static final String FLD_LOGIN_ID_LOWERCASE = "loginid";
    public static final String COL_SESSION_ID = "SESSION_ID";
    public static final String FLD_SESSION_ID = "sessionId";
    public static final String FLD_SESSION_ID_LOWERCASE = "sessionid";
    public static final String COL_USER_AGENT = "LOWER(USER_AGENT)";
    public static final String FLD_USER_AGENT = "userAgent";
    public static final String FLD_USER_AGENT_LOWERCASE = "useragent";
    public static final String COL_LOGIN_TIME = "LOGIN_TIME";
    public static final String FLD_LOGIN_TIME = "loginTime";
    public static final String FLD_LOGIN_TIME_LOWERCASE = "logintime";
    public static final String COL_LAST_ACCESS_TIME = "LAST_ACCESS_TIME";
    public static final String FLD_LAST_ACCESS_TIME = "lastAccessTime";
    public static final String FLD_LAST_ACCESS_TIME_LOWERCASE = "lastaccesstime";
    public static final String COL_TIME_CREATED = "TIME_CREATED";
    public static final String FLD_TIME_CREATED_SINCE = "since";
    public static final String FLD_TIME_CREATED_UNTIL = "until";
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";
    public static final String EQ = "eq";
    public static final String SW = "sw";
    public static final String EW = "ew";
    public static final String CO = "co";
    public static final String LE = "le";
    public static final String LT = "lt";
    public static final String GE = "ge";
    public static final String GT = "gt";

    /**
     * Enum defining filter types available for sessions.
     */
    public enum FilterType {
        DEFAULT,
        SESSION,
        APPLICATION,
        USER,
        MAIN
    }

    /**
     * Enum defining SQL query operations.
     */
    public enum QueryOperations {
        AND (" AND "),
        WHERE("WHERE {0} ");

        private final String queryString;

        QueryOperations(String queryString) {

            this.queryString = queryString;
        }

        public String getQueryString() {

            return queryString;
        }
    }

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
                "Session ID is not provided to perform session tasks."),
        ERROR_CODE_UNABLE_TO_GET_APP_DATA("USM-10010",
                "SQL Error",
                "Error while retrieving application data."),
        ERROR_CODE_INVALID_DATA("USM-10011",
                "Invalid data",
                "Data validation has failed, %s."),
        ERROR_CODE_INVALID_SESSION_ID("USM-10011",
                "Invalid Session",
                "Session cannot be found for the given session ID.");

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

    /**
     * Enum defining map fields of auth session user.
     */
    public enum AuthSessionUserKeys {
        IDP_ID,
        IDP_NAME,
        USER_ID
    }
}
