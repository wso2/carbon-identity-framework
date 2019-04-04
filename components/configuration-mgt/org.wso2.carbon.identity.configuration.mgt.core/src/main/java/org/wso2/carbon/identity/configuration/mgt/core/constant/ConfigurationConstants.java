/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.constant;

/**
 * Constants related to configuration management.
 */
public class ConfigurationConstants {

    public static final String STATUS_BAD_REQUEST_MESSAGE_DEFAULT = "Bad Request";
    public static final String STATUS_NOT_FOUND_MESSAGE_DEFAULT = "Not Found";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String DEFAULT_RESPONSE_CONTENT_TYPE = APPLICATION_JSON;
    public static final String STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT = "Internal server error";
    public static final String RESOURCE_PATH = "/resource";
    public static final String RESOURCE_TYPE_PATH = "/resource-type";
    public static final String MY_SQL = "MySQL";
    public static final String POSTGRE_SQL = "PostgreSQL";
    public static final String DB2 = "DB2";
    public static final String MICROSOFT = "Microsoft";
    public static final String S_MICROSOFT = "microsoft";
    public static final String INFORMIX = "Informix";
    public static final String H2 = "H2";
    public static final String RESOURCE_SEARCH_BEAN_FIELD_TENANT_ID = "tenantId";
    public static final String RESOURCE_SEARCH_BEAN_FIELD_TENANT_DOMAIN = "tenantDomain";
    public static final String RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_TYPE_ID = "resourceTypeId";
    public static final String RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_TYPE_NAME = "resourceTypeName";
    public static final String RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_ID = "resourceId";
    public static final String RESOURCE_SEARCH_BEAN_FIELD_RESOURCE_NAME = "resourceName";
    public static final String RESOURCE_SEARCH_BEAN_FIELD_ATTRIBUTE_KEY = "attributeKey";
    public static final String RESOURCE_SEARCH_BEAN_FIELD_ATTRIBUTE_VALUE = "attributeValue";
    public static final String DB_SCHEMA_COLUMN_NAME_ID = "ID";
    public static final String DB_SCHEMA_COLUMN_NAME_NAME = "NAME";
    public static final String DB_SCHEMA_COLUMN_NAME_TENANT_ID = "TENANT_ID";
    public static final String DB_SCHEMA_COLUMN_NAME_LAST_MODIFIED = "LAST_MODIFIED";
    public static final String DB_SCHEMA_COLUMN_NAME_RESOURCE_TYPE = "RESOURCE_TYPE";
    public static final String DB_SCHEMA_COLUMN_NAME_DESCRIPTTION = "DESCRIPTION";
    public static final String DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_KEY = "ATTR_KEY";
    public static final String DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_VALUE = "ATTR_VALUE";
    public static final String DB_SCHEMA_COLUMN_NAME_ATTRIBUTE_ID = "ATTR_ID";
    public static final String DB_SCHEMA_COLUMN_NAME_HAS_FILE = "HAS_FILE";
    public static final String DB_SCHEMA_COLUMN_NAME_HAS_ATTRIBUTE = "HAS_ATTRIBUTE";
    public static final String DB_SCHEMA_COLUMN_NAME_FILE_ID = "FILE_ID";
    public static final String DB_SCHEMA_COLUMN_NAME_CREATED_TIME = "CREATED_TIME";

    public enum ErrorMessages {
        ERROR_CODE_NO_USER_FOUND("CONFIGM_00001", "No authenticated user found to perform the operation: %s."),
        ERROR_CODE_UNEXPECTED("CONFIGM_00002", "Unexpected Error"),
        ERROR_CODE_GET_DAO("CONFIGM_00003", "No %s are registered."),
        ERROR_CODE_RESOURCE_TYPE_NAME_REQUIRED("CONFIGM_00004", "Resource type name: %s validation failed."),
        ERROR_CODE_ADD_RESOURCE_TYPE("CONFIGM_00005", "Error while adding the resource type: %s."),
        ERROR_CODE_RESOURCE_TYPE_ALREADY_EXISTS("CONFIGM_00006", "Resource type with the name: %s already exists."),
        ERROR_CODE_RETRIEVE_RESOURCE_TYPE("CONFIGM_00007", "Error while getting the resource type: %s."),
        ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS("CONFIGM_00008", "Resource type with the name: %s does not exists."),
        ERROR_CODE_DELETE_RESOURCE_TYPE("CONFIGM_00009", "Error while deleting the resource type: %s."),
        ERROR_CODE_RESOURCE_ADD_REQUEST_INVALID("CONFIGM_00010", "Resource add request validation failed. " +
                "Invalid resource add request."),
        ERROR_CODE_RESOURCE_GET_REQUEST_INVALID("CONFIGM_00011", "Resource get request validation failed"),
        ERROR_CODE_GET_RESOURCE("CONFIGM_00012", "Error while getting the resource : %s."),
        ERROR_CODE_RESOURCE_ALREADY_EXISTS("CONFIGM_00013", "Resource with the name: %s already exists."),
        ERROR_CODE_ADD_RESOURCE("CONFIGM_00014", "Error while adding the resource : %s."),
        ERROR_CODE_QUERY_LENGTH_EXCEEDED("CONFIGM_00015", "Too large SQL query length."),
        ERROR_CODE_RESOURCE_DELETE_REQUEST_REQUIRED("CONFIGM_00016", "Resource delete request validation failed. " +
                "Invalid resource delete request."),
        ERROR_CODE_RESOURCE_DOES_NOT_EXISTS("CONFIGM_00017", "Resource with the name: %s does not exists."),
        ERROR_CODE_SEARCH_REQUEST_INVALID("CONFIGM_00018", "Search request validation failed. " +
                "Invalid search filter."),
        ERROR_CODE_SEARCH_TENANT_RESOURCES("CONFIGM_00019", "Error occurred while searching for resources."),
        ERROR_CODE_RESOURCES_DOES_NOT_EXISTS("CONFIGM_00020", "Resources does not exists."),
        ERROR_CODE_SEARCH_QUERY_PROPERTY_DOES_NOT_EXISTS("CONFIGM_00021", "Search query property: %s is either " +
                "invalid or not " +
                "found in the permitted properties."),
        ERROR_CODE_SEARCH_QUERY_SQL_PROPERTY_PARSE_ERROR("CONFIGM_00022", "Search query syntax error in the " +
                "condition: %s."),
        ERROR_CODE_SEARCH_QUERY_SQL_PARSE_ERROR("CONFIGM_00023", "Search query syntax error"),
        ERROR_CODE_ATTRIBUTE_IDENTIFIERS_REQUIRED("CONFIGM_00024", "One or more identifiers for the attribute: %s " +
                "validation failed."),
        ERROR_CODE_GET_ATTRIBUTE("CONFIGM_00025", "Error while replacing the attribute: %s."),
        ERROR_CODE_ATTRIBUTE_REQUIRED("CONFIGM_00026", "Attribute validation failed"),
        ERROR_CODE_ATTRIBUTE_DOES_NOT_EXISTS("CONFIGM_00027", "Attribute with the key: %s does not exists."),
        ERROR_CODE_UPDATE_ATTRIBUTE("CONFIGM_00028", "Error while updating the attribute: %s."),
        ERROR_CODE_DELETE_ATTRIBUTE("CONFIGM_00029", "Error while deleting the attribute: %s."),
        ERROR_CODE_UPDATE_RESOURCE_TYPE("CONFIGM_00030", "Error while updating the resource type: %s."),
        ERROR_CODE_ATTRIBUTE_ALREADY_EXISTS("CONFIGM_00031", "Attribute with the name: %s already exists."),
        ERROR_CODE_INSERT_ATTRIBUTE("CONFIGM_00032", "Error while adding the attribute: %s."),
        ERROR_CODE_RESOURCE_REPLACE_REQUEST_INVALID("CONFIGM_00033", "Resource replace request validation failed."),
        ERROR_CODE_REPLACE_RESOURCE("CONFIGM_00034", "Error while replacing the resource : %s."),
        ERROR_CODE_REPLACE_ATTRIBUTE("CONFIGM_00035", "Error while replacing the attribute: %s."),
        ERROR_CODE_FEATURE_NOT_ENABLED("CONFIGM_00036", "Configuration management feature is not enabled."),
        ;

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
