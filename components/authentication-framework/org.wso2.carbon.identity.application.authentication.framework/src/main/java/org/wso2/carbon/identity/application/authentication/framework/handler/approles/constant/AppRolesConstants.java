/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.handler.approles.constant;

/**
 * Application roles constants.
 */
public class AppRolesConstants {

    private static final String APP_ROLES_RESOLVER_ERROR_PREFIX = "APP-ROLES-";

    /**
     * Error messages.
     */
    public enum ErrorMessages {
        ERROR_CODE_USER_NULL("60001", "Authenticated user cannot be null",
                "Authenticated user cannot be null"),
        ERROR_CODE_APP_ID_NULL("60002", "Application id cannot be null",
                "Application id cannot be null"),
        ERROR_CODE_UNEXPECTED("65001", "Unexpected processing error",
                "Server encountered an error while serving the request"),
        ERROR_CODE_RETRIEVING_IDENTITY_PROVIDER("65002", "Error while retrieving identity provider",
                "Error while retrieving identity provider with name: %s in the tenant: %s"),
        ERROR_CODE_RETRIEVING_ORG_ID("65003", "Error while retrieving organization id",
                "Error while retrieving organization id for the tenant domain: %s"),
        ERROR_CODE_RETRIEVING_LOCAL_USER_GROUPS("65004", "Error while retrieving local user groups",
                "Error while retrieving local user groups for the user"),
        ERROR_CODE_RETRIEVING_APP_ROLES("65005", "Error while retrieving application roles",
                "Error while retrieving application roles for organization: %s, application: %s, " +
                        "idp: %s, groups: %s"),
        ERROR_CODE_APP_ROLES_SERVICE_NO_RESPONSE("65006", "Error while retrieving application roles",
                "No response received from the application roles service"),
        ERROR_CODE_SERVICE_ENDPOINT_UNDEFINED("60007", "Service endpoint is not defined",
                "Service endpoint is not defined");

        private final String code;
        private final String message;
        private final String description;

        ErrorMessages(String code, String message, String description) {

            this.code = code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {

            return APP_ROLES_RESOLVER_ERROR_PREFIX + code;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }
    }

}
