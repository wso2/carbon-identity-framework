/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.metadata.internal.constant;

/**
 * SQL Constants for Webhook Metadata.
 * This class is used to store SQL queries and column names.
 */
public final class WebhookMetadataSQLConstants {

    private WebhookMetadataSQLConstants() {

    }

    /**
     * This class is used to store column names.
     */
    public static final class Column {

        public static final String ID = "ID";
        public static final String PROPERTY_NAME = "PROPERTY_NAME";
        public static final String PROPERTY_TYPE = "PROPERTY_TYPE";
        public static final String PRIMITIVE_VALUE = "PRIMITIVE_VALUE";
        public static final String OBJECT_VALUE = "OBJECT_VALUE";
        public static final String TENANT_ID = "TENANT_ID";

        private Column() {

        }
    }

    /**
     * This class is used to store SQL queries.
     */
    public static final class Query {

        public static final String GET_WEBHOOK_METADATA_PROPERTIES_INFO_BY_ID =
                "SELECT PROPERTY_NAME, PROPERTY_TYPE, " +
                        "PRIMITIVE_VALUE, OBJECT_VALUE FROM IDN_WEBHOOK_METADATA WHERE " +
                        "TENANT_ID = :TENANT_ID;";

        public static final String INSERT_WEBHOOK_METADATA_PROPERTY =
                "INSERT INTO IDN_WEBHOOK_METADATA (PROPERTY_NAME, PROPERTY_TYPE, " +
                        "PRIMITIVE_VALUE, OBJECT_VALUE, TENANT_ID) " +
                        "VALUES (:PROPERTY_NAME;, :PROPERTY_TYPE;, :PRIMITIVE_VALUE;, :OBJECT_VALUE;, :TENANT_ID;)";

        public static final String UPDATE_WEBHOOK_METADATA_PROPERTY =
                "UPDATE IDN_WEBHOOK_METADATA SET PROPERTY_TYPE = :PROPERTY_TYPE;, PRIMITIVE_VALUE = " +
                        ":PRIMITIVE_VALUE;, OBJECT_VALUE = :OBJECT_VALUE; WHERE PROPERTY_NAME = " +
                        ":PROPERTY_NAME; AND TENANT_ID = :TENANT_ID;";

        private Query() {

        }
    }
}
