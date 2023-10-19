/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.secret.mgt.core.constant;

/**
 * Constants related to configuration management.
 */
public class SecretConstants {

    public static final String DB_SCHEMA_COLUMN_NAME_ID = "ID";
    public static final String DB_SCHEMA_COLUMN_NAME_SECRET_VALUE = "SECRET_VALUE";
    public static final String DB_SCHEMA_COLUMN_NAME_NAME = "NAME";
    public static final String DB_SCHEMA_COLUMN_NAME_SECRET_NAME = "SECRET_NAME";
    public static final String DB_SCHEMA_COLUMN_NAME_TENANT_ID = "TENANT_ID";
    public static final String DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED = "LAST_MODIFIED";
    public static final String DB_SCHEMA_COLUMN_NAME_CREATED_TIME = "CREATED_TIME";
    public static final String DB_SCHEMA_COLUMN_NAME_TYPE = "TYPE";
    public static final String DB_SCHEMA_COLUMN_NAME_DESCRIPTION = "DESCRIPTION";
    public static final String DB_TABLE_SECRET = "IDN_SECRET";
    public static final String DB_TABLE_SECRET_TYPE = "IDN_SECRET_TYPE";
    public static final String IDN_SECRET_TYPE_IDP_SECRETS = "IDP_SECRET_PROPERTIES";

    public enum ErrorMessages {
        ERROR_CODE_UNEXPECTED("SECRETM_00001", "Unexpected Error"),
        ERROR_CODE_GET_DAO("SECRETM_00002", "No %s are registered."),
        ERROR_CODE_SECRET_ADD_REQUEST_INVALID("SECRETM_00003", "Secret add request validation failed. " +
                "Invalid secret add request."),
        ERROR_CODE_SECRET_GET_REQUEST_INVALID("SECRETM_00004", "Secret get request validation failed"),
        ERROR_CODE_GET_SECRET("SECRETM_00005", "Error while getting the secret : %s."),
        ERROR_CODE_SECRET_ALREADY_EXISTS("SECRETM_00006", "Secret with the name: %s already exists."),
        ERROR_CODE_ADD_SECRET("SECRETM_00007", "Error while adding the secret : %s."),
        ERROR_CODE_SECRET_DELETE_REQUEST_REQUIRED("SECRETM_00008", "Secret delete request validation failed. " +
                "Invalid secret delete request."),
        ERROR_CODE_SECRET_DOES_NOT_EXISTS("SECRETM_00009", "Secret with the name: %s does not exists."),
        ERROR_CODE_SECRETS_DOES_NOT_EXISTS("SECRETM_00010", "Secrets does not exists."),
        ERROR_CODE_SECRET_REPLACE_REQUEST_INVALID("SECRETM_00011", "Secret replace request validation failed."),
        ERROR_CODE_REPLACE_SECRET("SECRETM_00012", "Error while replacing the secret : %s."),
        ERROR_CODE_SECRET_ID_DOES_NOT_EXISTS("SECRETM_00013", "Secret with the id: %s does not exists."),
        ERROR_CODE_INVALID_SECRET_ID("SECRETM_00014", "Invalid secret id: %s."),
        ERROR_CODE_DELETE_SECRET("SECRETM_00015", "Error while deleting the secret: %s."),
        ERROR_CODE_SECRET_MANAGER_NOT_ENABLED("SECRETM_00016", "Secret management feature is not enabled"),

        ERROR_CODE_SECRET_TYPE_NAME_REQUIRED("SECRETM_00017", "Secret type name: %s validation failed."),
        ERROR_CODE_ADD_SECRET_TYPE("SECRETM_00018", "Error while adding the secret type: %s."),
        ERROR_CODE_SECRET_TYPE_ALREADY_EXISTS("SECRETM_00019", "Secret type with the name: %s already exists."),
        ERROR_CODE_RETRIEVE_SECRET_TYPE("SECRETM_00020", "Error while getting the secret type: %s."),
        ERROR_CODE_SECRET_TYPE_DOES_NOT_EXISTS("SECRETM_00021", "Secret type with the name: %s does not exists."),
        ERROR_CODE_DELETE_SECRET_TYPE("SECRETM_00022", "Error while deleting the secret type: %s."),
        ERROR_CODE_UPDATE_SECRET_TYPE("SECRETM_00023", "Error while updating the secret type: %s."),
        ERROR_CODE_UPDATE_SECRET("SECRETM_00024", "Error while updating the secret %s.");

        private final String code;
        private final String message;

        ErrorMessages(String code, String message) {

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
