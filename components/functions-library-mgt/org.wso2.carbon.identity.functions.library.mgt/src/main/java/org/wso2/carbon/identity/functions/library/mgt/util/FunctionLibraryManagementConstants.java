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

package org.wso2.carbon.identity.functions.library.mgt.util;

/**
 * Function Library Management constant class.
 */
public class FunctionLibraryManagementConstants {

    /**
     * Enum for error messages.
     */
    public enum ErrorMessage {

        ERROR_CODE_UNEXPECTED("SCL-65001", "Unexpected Error"),
        ERROR_CODE_DATABASE_CONNECTION("SCL-65001", "Couldn't get a database connection."),
        ERROR_CODE_VALIDATE_SCRIPT_LIBRARY_SCRIPT("SCL-60002", "Script library script of %s contains errors."),
        ERROR_CODE_REQUIRE_SCRIPT_LIBRARY_NAME("SCL-60003", "Script library name is required"),
        ERROR_CODE_REQUIRE_SCRIPT_LIBRARY_SCRIPT("SCL-60003", "Script library script is required"),
        ERROR_CODE_ALL_READY_EXIST_SCRIPT_LIBRARY("SCL-65004", "Already a script library available with the name: %s."),
        ERROR_CODE_INVALID_SCRIPT_LIBRARY_NAME("SCL-65004", "The script library name is not valid! It is not adhering" +
                " to the regex %s."),
        ERROR_CODE_ADD_SCRIPT_LIBRARY("SCL-65002", "Error while creating the script library: %s."),
        ERROR_CODE_PROCESSING_CONTENT_STREAM_SCRIPT_LIBRARY("SCL-65003",
                "An error occurred while processing content stream of script library script: %s."),
        ERROR_CODE_INVALID_TENANT("SCL-65004", "Error while creating script library due to invalid tenant."),
        ERROR_CODE_RETRIEVE_SCRIPT_LIBRARY("SCL-65005", "Error while getting the script library: %s."),
        ERROR_CODE_RETRIEVE_SCRIPT_LIBRARIES("SCL-65006", "Error while reading Script libraries."),
        ERROR_CODE_UPDATE_SCRIPT_LIBRARY("SCL-65007", "Failed to update Script library: %s."),
        ERROR_CODE_DELETE_SCRIPT_LIBRARY("SCL-65008", "Error while deleting Script library: %s."),
        ERROR_CODE_FAILED_TO_CHECK_SCRIPT_LIBRARY("SCL-60002", "Script library with resource ID: %s does not exists.");

        private final String code;
        private final String message;

        ErrorMessage(String code, String message) {

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

            return code + ":" + message;
        }
    }
}
