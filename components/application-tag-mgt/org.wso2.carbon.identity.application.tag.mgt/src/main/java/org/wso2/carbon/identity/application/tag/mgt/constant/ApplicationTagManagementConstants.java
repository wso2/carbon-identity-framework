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

    public static final String APPLICATION_TAG_PROPERTY_NAME = "name";

    /**
     * Error messages.
     */
    public enum ErrorMessages {

        // Client errors.
        ERROR_CODE_INVALID_OFFSET_FOR_PAGINATION("60001", "Unable to retrieve Application Tags.",
                "Invalid offset requested. Offset value should be zero or greater than zero."),
        ERROR_CODE_INVALID_LIMIT_FOR_PAGINATION("60002", "Unable to retrieve Application Tags.",
                "Invalid limit requested. Limit value should be greater than zero."),
        ERROR_CODE_APP_TAG_ALREADY_EXISTS("60003", "Unable to add Application Tag.",
                "Application Tag already exists for the tenant: %s."),
        ERROR_CODE_INVALID_FILTER("60004", "Unable to retrieve filtered Application Tags.",
                "Filter attribute or filter condition is empty or invalid."),
        ERROR_CODE_UNSUPPORTED_FILTER_ATTRIBUTE("60005", "Filtering using the attempted attribute is " +
                "not supported.", "Filtering cannot be done with the '%s' attribute. " +
                "Only supported with 'name' attribute."),
        ERROR_CODE_INVALID_FILTER_OPERATION("60006", "Filtering using the attempted attribute is invalid",
                "Filtering cannot be done with the '%s' operation. " +
                "Only supported with 'eq', 'co', 'sw', 'ew' operations."),
        ERROR_CODE_INVALID_FILTER_FORMAT("60007", "Invalid filter query format.",
                "Filter needs to be in the format <attribute>+<operation>+<value>. Eg: name+eq+hr"),

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
        ERROR_CODE_ERROR_WHILE_RETRIEVING_APP_TAGS_COUNT("65006", "Error while retrieving Application " +
                "Tags count.", "Error while retrieving Application Tags count from the database."),
        ERROR_CODE_ERROR_WHILE_CHECKING_APP_TAG_EXISTENCE("65007", "Error while checking existence " +
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
