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

package org.wso2.carbon.identity.api.resource.mgt.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * API resource management constants.
 */
public class APIResourceManagementConstants {

    public static final String NAME = "name";
    public static final String IDENTIFIER = "identifier";
    public static final String TYPE = "type";
    public static final String RBAC_AUTHORIZATION = "RBAC";
    public static final String NO_POLICY = "NO POLICY";
    public static final String ASC = "ASC";
    public static final String NON_BUSINESS_API_FILTER = "type ne BUSINESS";
    public static final String TENANT_API_FILTER = "type eq TENANT";
    public static final String ME_API_FILTER = "name eq Me API";
    public static final String INTERNAL_SCOPE_FILTER = "name sw internal_";
    public static final String CONSOLE_SCOPE_FILTER = "name sw console:";
    public static final String BEFORE = "before";
    public static final String AFTER = "after";
    public static final String PROPERTIES = "properties";
    public static final String BUSINESS_TYPE = "BUSINESS";
    public static final String SYSTEM_TYPE = "SYSTEM";
    public static final String EQ = "eq";
    public static final String NE = "ne";
    public static final String CO = "co";
    public static final String SW = "sw";
    public static final String EW = "ew";
    public static final String GE = "ge";
    public static final String LE = "le";
    public static final String GT = "gt";
    public static final String LT = "lt";
    public static final String BEFORE_GT = "before gt ";
    public static final String AFTER_LT = "after lt ";
    public static final String ME_API = "Me API";
    private static final Map<String, String> attributeColumnMap = new HashMap<>();
    private static final Map<String, String> scopeAttributeColumnMap = new HashMap<>();
    public static final Map<String, String> ATTRIBUTE_COLUMN_MAP = Collections.unmodifiableMap(attributeColumnMap);
    public static final Map<String, String> SCOPE_ATTRIBUTE_COLUMN_MAP =
            Collections.unmodifiableMap(scopeAttributeColumnMap);

    static {
        attributeColumnMap.put(NAME, SQLConstants.NAME_COLUMN_NAME);
        attributeColumnMap.put(IDENTIFIER, SQLConstants.IDENTIFIER_COLUMN_NAME);
        attributeColumnMap.put(BEFORE, SQLConstants.CURSOR_KEY_COLUMN_NAME);
        attributeColumnMap.put(AFTER, SQLConstants.CURSOR_KEY_COLUMN_NAME);
        attributeColumnMap.put(TYPE, SQLConstants.TYPE_COLUMN_NAME);

        scopeAttributeColumnMap.put(NAME, SQLConstants.NAME_COLUMN_NAME);
    }

    /**
     * API resource configuration builder constants.
     */
    public static class APIResourceConfigBuilderConstants {

        public static final String API_RESOURCE_ELEMENT = "APIResource";
        public static final String SCOPES_ELEMENT = "Scopes";
        public static final String SCOPE_ELEMENT = "Scope";
        public static final String NAME = "name";
        public static final String IDENTIFIER = "identifier";
        public static final String DISPLAY_NAME = "displayName";
        public static final String DESCRIPTION = "description";
        public static final String REQUIRES_AUTHORIZATION = "requiresAuthorization";
        public static final String TYPE = "type";
        public static final String TENANT_ADMIN_TYPE = "TENANT_ADMIN";
    }

    /**
     * Error messages.
     */
    public enum ErrorMessages {

        // Client errors.
        ERROR_CODE_INVALID_FILTER_FORMAT("60001", "Unable to retrieve API resources.",
                "Invalid format used for filtering."),
        ERROR_CODE_INVALID_CURSOR_FOR_PAGINATION("60002", "Unable to retrieve tenant domains.",
                "Invalid cursor used for pagination."),
        ERROR_CODE_API_RESOURCE_ALREADY_EXISTS("60003", "Unable to add API resource.",
                "API resource already exists for the tenant: %s."),
        ERROR_CODE_SCOPE_ALREADY_EXISTS("60004", "Unable to add scope.",
                "Scope already exists for the tenant: %s."),
        ERROR_CODE_INVALID_FILTER_VALUE("60005", "Unable to retrieve API resources.",
                "Invalid filter value used for filtering."),
        ERROR_CODE_CREATION_RESTRICTED("60006", "API resource creation is restricted.",
                "API resource creation is restricted in organizations."),
        ERROR_CODE_INVALID_IDENTIFIER_VALUE("60007", "Unable to add API resources.",
                "Invalid identifier value provided."),

        // Server errors.
        ERROR_CODE_ERROR_WHILE_RETRIEVING_API_RESOURCES("65001", "Error while retrieving API resources.",
                "Error while retrieving API resources from the database."),
        ERROR_CODE_ERROR_WHILE_RETRIEVING_SCOPES("65002", "Error while retrieving scopes.",
                "Error while retrieving scopes from the database."),
        ERROR_CODE_ERROR_WHILE_ADDING_API_RESOURCE("65003", "Error while adding API resource.",
                "Error while adding API resource to the database."),
        ERROR_CODE_ERROR_WHILE_DELETING_API_RESOURCE("65004", "Error while deleting API resource.",
                "Error while deleting API resource from the database."),
        ERROR_CODE_ERROR_WHILE_UPDATING_API_RESOURCE("65005", "Error while updating API resource.",
                "Error while updating API resource in the database."),
        ERROR_CODE_ERROR_WHILE_ADDING_SCOPES("65006", "Error while adding scopes.",
                "Error while adding scopes to the database."),
        ERROR_CODE_ERROR_WHILE_UPDATING_SCOPES("65007", "Error while updating scopes.",
                "Error while updating scopes in the database."),
        ERROR_CODE_ERROR_WHILE_DELETING_SCOPES("65008", "Error while deleting scopes.",
                "Error while deleting scopes from the database."),
        ERROR_CODE_ERROR_WHILE_GETTING_SCOPES("65009", "Error while getting scopes.",
                "Error while getting scopes from the database."),
        ERROR_CODE_ERROR_WHILE_CHECKING_EXISTENCE_OF_SCOPE("65010", "Error while checking existence of" +
                " scope.", "Error while checking existence of scope in the database."),
        ERROR_CODE_ERROR_WHILE_CHECKING_API_RESOURCE_EXISTENCE("65011", "Error while checking existence " +
                "of API resource.", "Error while checking existence of API resource in the database."),
        ERROR_CODE_ERROR_WHILE_RETRIEVING_SCOPE_METADATA("65012", "Error while retrieving scope metadata.",
                "Error while retrieving scope metadata from the database."),
        ERROR_CODE_ERROR_WHILE_RETRIEVING_API_RESOURCE_PROPERTIES("65013", "Error while retrieving API " +
                "resource properties.", "Error while retrieving API resource properties from the database."),
        ERROR_CODE_ERROR_WHILE_ADDING_API_RESOURCE_PROPERTIES("65014", "Error while adding API resource " +
                "properties.", "Error while adding API resource properties to the database."),
        ;

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
