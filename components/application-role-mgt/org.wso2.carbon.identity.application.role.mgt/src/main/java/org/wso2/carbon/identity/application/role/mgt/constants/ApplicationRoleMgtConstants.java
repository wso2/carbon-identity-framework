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

package org.wso2.carbon.identity.application.role.mgt.constants;

/**
 * Application role management constants.
 */
public class ApplicationRoleMgtConstants {

    private static final String APP_ROLE_MGT_ERROR_CODE_PREFIX = "APM-";

    /**
     * Application role management error message constants.
     */
    public enum ErrorMessages {

        // Server Errors.
        ERROR_CODE_INSERT_ROLE("65001", "Error occurred while adding the role.",
                "Error occurred while adding the role: %s to application: %s."),
        ERROR_CODE_GET_ROLE_BY_ID("65002", "Error occurred while retrieving the role.",
                "Error occurred while retrieving the role: %s."),
        ERROR_CODE_CHECKING_ROLE_EXISTENCE("65003", "Error occurred while checking the role existence.",
                "Error occurred while checking whether the role: %s exists in application: %s."),
        ERROR_CODE_GET_ROLES_BY_APPLICATION("65004", "Error occurred while retrieving the roles of the application",
                "Error occurred while retrieving the roles of application: %s."),
        ERROR_CODE_UPDATE_ROLE("65005", "Error occurred while updating the role.",
                "Error occurred while updating the role: %s of application: %s."),
        ERROR_CODE_DELETE_ROLE("65006", "Error occurred while deleting the role.",
                "Error occurred while deleting the role: %s."),
        ERROR_CODE_UPDATE_ROLE_ASSIGNED_USERS("65007", "Error occurred while assigning users to the role.",
                "Error occurred while assigning users to the role: %s."),
        ERROR_CODE_GET_ROLE_ASSIGNED_USERS("65008", "Error occurred while retrieving users of the role.",
                "Error occurred while retrieving users of the role: %s."),
        ERROR_CODE_UPDATE_ROLE_ASSIGNED_GROUPS("65007", "Error occurred while assigning groups to the role.",
                "Error occurred while assigning groups to the role: %s."),
        ERROR_CODE_GET_ROLE_ASSIGNED_GROUPS("65008", "Error occurred while retrieving groups of the role.",
                "Error occurred while retrieving groups of the role: %s."),

        // Client Errors.
        ERROR_CODE_DUPLICATE_ROLE("60001", "Role already exists.",
                "Role with name: %s already exists in application: %s.");

        private final String code;
        private final String message;
        private final String description;

        ErrorMessages(String code, String message, String description) {

            this.code = code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {

            return APP_ROLE_MGT_ERROR_CODE_PREFIX + code;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }
    }
}
