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

package org.wso2.carbon.identity.role.v2.mgt.core.constants;

/**
 * Constants for role management.
 */
public class RoleMgtConstants {

    public static final String USER_ID = "userId";
    public static final String USER_IDS = "userIds";
    public static final String USER_GROUPS = "userGroups";
    public static final String MAIN_ROLE_UUID = "MainRoleUUID";
    public static final String SHARED_ROLE_UUID = "SharedRoleUUID";

    public static final String COMMA = ",";
    public static final String QUESTION_MARK = "?";
    public static final String CLOSE_PARENTHESIS = ")";
    public static final String WHITE_SPACE = " ";

    public static final String SHARING_ERROR_PREFIX = "RMT-";

    /**
     * Error messages for organization user sharing management related errors.
     */
    public enum ErrorMessage {

        // Service layer errors
        ERROR_CODE_NO_MAIN_ROLE_FOUND_FOR_GIVEN_SHARED_ROLE("10011",
                "Could not find a main role to the given shared role.",
                "Error while retrieving main role UUIDs for shared roles.");

        private String code;
        private String message;
        private String description;

        ErrorMessage(String code, String message, String description) {
            this.code = code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {

            return SHARING_ERROR_PREFIX + code;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }
    }

}
