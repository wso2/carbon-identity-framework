/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.session.extender;

import java.util.regex.Pattern;

/**
 * Constants used by the Session Extender endpoint.
 */
public class SessionExtenderConstants {

    public static final Pattern SESSION_EXTENDER_ENDPOINT = Pattern.compile("(.*)/identity/extend-session/?");
    public static final String SESSION_ID_PARAM_NAME = "idpSessionKey";

    // Custom header names.
    public static final String TRACE_ID_HEADER_NAME = "Trace-ID";

    // Format of the server logs.
    public static final String ERROR_LOG_TEMPLATE = "CorrelationId : %s | INITIATOR : %s - %s";
    public static final String AUDIT_MESSAGE_TEMPLATE = "Initiator : %s | Action : %s | Data : { %s } | Result : %s ";

    // Field names of logs and responses.
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String DESCRIPTION = "description";
    public static final String TRACE_ID = "traceId";
    public static final String SUCCESS = "Success";
    public static final String TENANT_DOMAIN = "tenantDomain";
    public static final String SESSION_CONTEXT_ID = "sessionContextId";
    public static final String EXTEND_SESSION_ACTION = "ExtendSession";

    // Default content type of the responses.
    public static final String APPLICATION_JSON = "application/json";

    /**
     * Error message enums.
     * <p>
     * Error codes for server errors start with 65 and client errors start with 60.
     */
    public enum Error {

        UNEXPECTED_SERVER_ERROR("65001", "Unexpected server error."),

        INVALID_REQUEST("60001", "Invalid request."),
        INVALID_SESSION_KEY_PARAM("60002", "Invalid session key parameter."),
        INVALID_SESSION_COOKIE("60003", "Invalid session cookie."),
        SESSION_NOT_AVAILABLE("60004", "Session not available."),
        CONFLICT("60005", "Conflict between parameter and cookie.");

        private final String code;
        private final String message;
        private static final String API_ERROR_CODE_PREFIX = "ISE-";

        Error(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return API_ERROR_CODE_PREFIX + code;
        }

        public String getMessage() {

            return message;
        }
    }
}
