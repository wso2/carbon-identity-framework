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

/**
 * API Resource Collection Management Constants.
 */
public class APIResourceCollectionManagementConstants {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DISPLAY_NAME = "displayName";
    public static final String TYPE = "type";
    public static final String EQ = "eq";
    public static final String CO = "co";
    public static final String SW = "sw";
    public static final String EW = "ew";
    public static final String OR = "or";
    public static final String EQUAL_SIGN = "=";
    public static final String API_RESOURCE_COLLECTION_FILE_NAME = "api-resource-collection.xml";
    public static final String READ_SCOPES = "readScopes";
    public static final String WRITE_SCOPES = "writeScopes";
    public static final String READ = "read";
    public static final String WRITE = "write";
    public static final String API_RESOURCES = "apiResources";

    /**
     * API resource collection configuration builder constants.
     */
    public static class APIResourceCollectionConfigBuilderConstants {

        public static final String API_RESOURCE_COLLECTION_ELEMENT = "APIResourceCollection";
        public static final String SCOPES_ELEMENT = "Scopes";
        public static final String SCOPE_ELEMENT = "Scope";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String DISPLAY_NAME = "displayName";
        public static final String TYPE = "type";
        public static final String READ = "Read";
        public static final String FEATURE = "Feature";
    }

    /**
     * Error messages.
     */
    public enum ErrorMessages {

        ERROR_CODE_INVALID_FILTER_FORMAT("60001", "Unable to retrieve API resource collections.",
                "Invalid format used for filtering."),
        ERROR_CODE_INVALID_FILTER_ATTRIBUTE("60003", "Unable to retrieve API resource collections.",
                "Invalid filter attribute used for filtering."),
        ERROR_CODE_INVALID_FILTER_OPERATOR("60004", "Unable to retrieve API resource collections.",
                "Invalid filter operator used for filtering."),
        ERROR_CODE_INVALID_FILTER_OPERATION("60005", "Unable to retrieve API resource collections.",
                "Invalid filter operation used for filtering."),
        // Server errors.
        ERROR_CODE_ERROR_WHILE_RETRIEVING_SCOPE_METADATA("65001", "Error while retrieving scope metadata.",
                "Error while retrieving scope metadata from the database."),
        ERROR_CODE_WHILE_FILTERING_API_RESOURCE_COLLECTIONS("65002",
                "Unable to retrieve API resource collections.",
                "Error while filtering API resource collections.");

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
