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

package org.wso2.carbon.identity.application.tag.mgt.constant;

/**
 * Application Tag management constants.
 */
public class ApplicationTagManagementConstants {

    /**
     * Error messages.
     */
    public enum ErrorMessages {

        // Client errors.
        ERROR_CODE_INVALID_FILTER_FORMAT("60001", "Unable to retrieve Application Tag.",
                "Invalid format used for filtering."),
        ERROR_CODE_INVALID_CURSOR_FOR_PAGINATION("60002", "Unable to retrieve tenant domains.",
                "Invalid cursor used for pagination."),
        ERROR_CODE_APP_TAG_ALREADY_EXISTS("60003", "Unable to add Application Tag.",
                "Application Tag already exists for the tenant: %s."),
        ERROR_CODE_INVALID_FILTER_VALUE("60004", "Unable to retrieve Application Tag.",
                "Invalid filter value used for filtering."),

        // Server errors.
        ERROR_CODE_ERROR_WHILE_RETRIEVING_APP_TAGS("65001", "Error while retrieving Application Tags.",
                "Error while retrieving Application Tags from the database."),
        ERROR_CODE_ERROR_CHECKING_DB_METADATA("60002", "Error while checking the database metadata.",
                "Error while checking the database metadata."),
        ERROR_CODE_ERROR_WHILE_ADDING_APP_TAG("65003", "Error while adding Application Tag.",
                "Error while adding Application Tag to the database."),
        ERROR_CODE_ERROR_WHILE_DELETING_APP_TAG("65004", "Error while deleting Application Tag.",
                "Error while deleting Application Tag from the database."),
        ERROR_CODE_ERROR_WHILE_UPDATING_APP_TAG("65005", "Error while updating Application Tag.",
                "Error while updating Application Tag in the database."),
        ERROR_CODE_ERROR_WHILE_CHECKING_APP_TAG_EXISTENCE("6506", "Error while checking existence " +
                "of Application Tag.", "Error while checking existence of Application Tag in the database.");

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
