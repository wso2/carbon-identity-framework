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

package org.wso2.carbon.identity.consent.server.configs.mgt.utils;

/**
 * Class with the constants.
 */
public class Constants {

    public static final String EXTERNAL_CONSENT_PAGE_CONFIGURATIONS = "external_consent_page_configurations";
    public static final String EXTERNAL_CONSENT_PAGE = "external_consent_page";
    public static final String EXTERNAL_CONSENT_PAGE_URL = "external_consent_page_url";
    public static final String RESOURCE_TYPE_NAME = "resourceTypeName";
    public static final String RESOURCE_NAME = "resourceName";

    /**
     * Error messages.
     */
    public enum ErrorMessages {

        //Client error codes.
        ERROR_NO_EXTERNAL_CONSENT_PAGE_CONFIGURATIONS_FOUND("60001",
                "External consent page configurations are not found.",
                "External consent page configurations are not configured for the tenant: %s"),

        ERROR_NO_EXTERNAL_CONSENT_PAGE_URL_FOUND("60002",
                "External consent page url is not found.",
                "External consent page url is not configured for the tenant: %s"),

        //Server error codes.
        ERROR_GETTING_EXISTING_CONFIGURATIONS("65001",
                "Unable to get external consent page configurations.",
                "Error occurred while getting the external consent page configurations for the tenant: %s");

        private final String code;
        private final String message;
        private final String description;

        ErrorMessages(String code, String message, String description) {

            this.code = code;
            this.message = message;
            this.description = description;
        }

        /**
         * Return the error code.
         *
         * @return Code.
         */
        public String getCode() {

            return code;
        }

        /**
         * Return the error message.
         *
         * @return Message.
         */
        public String getMessage() {

            return message;
        }

        /**
         * Return the error description.
         *
         * @return Description.
         */
        public String getDescription() {

            return description;
        }

        @Override
        public String toString() {

            return code + " | " + message;
        }
    }
}
