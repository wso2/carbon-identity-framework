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

package org.wso2.carbon.identity.api.resource.collection.mgt.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class APIResourceCollectionManagementConstants {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DISPLAY_NAME = "displayName";
    public static final String TYPE = "type";
    public static final String SELF = "self";
    public static final String EQ = "eq";
    public static final String CO = "co";
    public static final String SW = "sw";
    public static final String EW = "ew";
    public static final String GE = "ge";
    public static final String LE = "le";
    public static final String GT = "gt";
    public static final String LT = "lt";

    public static final String ID_FILED_NAME = "id";
    public static final String NAME_FIELD_NAME = "name";
    public static final String DISPLAY_NAME_FIELD_NAME = "displayName";
    public static final String TYPE_FIELD_NAME = "type";

    private static final Map<String, String> attributeColumnMap = new HashMap<>();
    private static final Map<String, String> scopeAttributeColumnMap = new HashMap<>();
    public static final Map<String, String> ATTRIBUTE_COLUMN_MAP = Collections.unmodifiableMap(attributeColumnMap);

    static {
        attributeColumnMap.put(ID, ID_FILED_NAME);
        attributeColumnMap.put(NAME, NAME_FIELD_NAME);
        attributeColumnMap.put(TYPE, TYPE_FIELD_NAME);
        attributeColumnMap.put(DISPLAY_NAME, DISPLAY_NAME_FIELD_NAME);
    }

    /**
     * Error messages.
     */
    public enum ErrorMessages {

        // Client errors.
        ERROR_CODE_INVALID_FILTER_FORMAT("60001", "Unable to retrieve API resources.",
                "Invalid format used for filtering."),
        ERROR_CODE_INVALID_FILTER_VALUE("60005", "Unable to retrieve API resources.",
                "Invalid filter value used for filtering."),

        // Server errors.
        ERROR_CODE_ERROR_WHILE_RETRIEVING_API_RESOURCES("65001", "Error while retrieving API resources.",
                "Error while retrieving API resources from the database."),
        ERROR_CODE_ERROR_WHILE_RETRIEVING_SCOPES("65002", "Error while retrieving scopes.",
                "Error while retrieving scopes from the database.");

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
