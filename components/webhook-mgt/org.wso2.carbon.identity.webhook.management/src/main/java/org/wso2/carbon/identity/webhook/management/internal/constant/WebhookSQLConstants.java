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

package org.wso2.carbon.identity.webhook.management.internal.constant;

import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;

/**
 * SQL Constants for Webhook Management.
 * This class is used to store SQL queries and column names.
 */
public class WebhookSQLConstants {

    /**
     * Table names.
     */
    public static final String IDN_WEBHOOKS_TABLE = "IDN_WEBHOOKS";
    public static final String IDN_WEBHOOK_EVENTS_TABLE = "IDN_WEBHOOK_EVENTS";

    // Use WebhookStatus enum values instead of hardcoded strings
    public static final String STATUS_ACTIVE = WebhookStatus.ACTIVE.name();
    public static final String STATUS_INACTIVE = WebhookStatus.INACTIVE.name();

    private WebhookSQLConstants() {

    }

    /**
     * This class is used to store column names.
     */
    public static class Column {

        private Column() {

        }

        // Column names for IDN_WEBHOOKS table
        public static final String ID = "ID";
        public static final String UUID = "UUID";
        public static final String ENDPOINT = "ENDPOINT";
        public static final String DESCRIPTION = "DESCRIPTION";
        public static final String SECRET = "SECRET";
        public static final String VERSION = "VERSION";
        public static final String EVENT_SCHEMA = "EVENT_SCHEMA";
        public static final String EVENT_SCHEMA_NAME = "EVENT_SCHEMA_NAME";
        public static final String EVENT_SCHEMA_URI = "EVENT_SCHEMA_URI";
        public static final String EVENT_SCHEMA_VERSION = "EVENT_SCHEMA_VERSION";
        public static final String STATUS = "STATUS";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String CREATED_AT = "CREATED_AT";
        public static final String UPDATED_AT = "UPDATED_AT";

        // Column names for IDN_WEBHOOK_EVENTS table
        public static final String WEBHOOK_ID = "WEBHOOK_ID";
        public static final String EVENT_NAME = "EVENT_NAME";

        // SQL query parameters
        public static final String LIMIT = "LIMIT";
        public static final String OFFSET = "OFFSET";
    }

    /**
     * This class is used to store SQL queries.
     */
    public static class Query {

        private Query() {

        }

        // Webhook CRUD operations
        public static final String CREATE_WEBHOOK = "INSERT INTO " + IDN_WEBHOOKS_TABLE +
                " (" + Column.UUID + ", " + Column.ENDPOINT + ", " + Column.DESCRIPTION + ", " +
                Column.SECRET + ", " + Column.VERSION + ", " + Column.EVENT_SCHEMA + ", " +
                Column.EVENT_SCHEMA_NAME + ", " + Column.EVENT_SCHEMA_URI + ", " +
                Column.EVENT_SCHEMA_VERSION + ", " + Column.STATUS + ", " + Column.TENANT_ID + ") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        public static final String GET_WEBHOOK_BY_ID = "SELECT * FROM " + IDN_WEBHOOKS_TABLE +
                " WHERE " + Column.UUID + " = ? AND " + Column.TENANT_ID + " = ?";

        public static final String UPDATE_WEBHOOK = "UPDATE " + IDN_WEBHOOKS_TABLE +
                " SET " + Column.ENDPOINT + " = ?, " + Column.DESCRIPTION + " = ?, " +
                Column.SECRET + " = ?, " + Column.VERSION + " = ?, " +
                Column.EVENT_SCHEMA + " = ?, " + Column.EVENT_SCHEMA_NAME + " = ?, " +
                Column.EVENT_SCHEMA_URI + " = ?, " + Column.EVENT_SCHEMA_VERSION + " = ?, " +
                Column.STATUS + " = ?, " + Column.UPDATED_AT + " = CURRENT_TIMESTAMP " +
                "WHERE " + Column.UUID + " = ? AND " + Column.TENANT_ID + " = ?";

        public static final String DELETE_WEBHOOK = "DELETE FROM " + IDN_WEBHOOKS_TABLE +
                " WHERE " + Column.UUID + " = ? AND " + Column.TENANT_ID + " = ?";

        public static final String LIST_WEBHOOKS = "SELECT * FROM " + IDN_WEBHOOKS_TABLE +
                " WHERE " + Column.TENANT_ID + " = ? " +
                "ORDER BY " + Column.CREATED_AT + " DESC " +
                "LIMIT ? OFFSET ?";

        public static final String LIST_WEBHOOKS_BY_STATUS = "SELECT * FROM " + IDN_WEBHOOKS_TABLE +
                " WHERE " + Column.TENANT_ID + " = ? AND " + Column.STATUS + " = ? " +
                "ORDER BY " + Column.CREATED_AT + " DESC " +
                "LIMIT ? OFFSET ?";

        public static final String COUNT_WEBHOOKS = "SELECT COUNT(1) FROM " + IDN_WEBHOOKS_TABLE +
                " WHERE " + Column.TENANT_ID + " = ?";

        public static final String COUNT_WEBHOOKS_BY_STATUS = "SELECT COUNT(1) FROM " + IDN_WEBHOOKS_TABLE +
                " WHERE " + Column.TENANT_ID + " = ? AND " + Column.STATUS + " = ?";

        // Webhook status operations
        public static final String ACTIVATE_WEBHOOK = "UPDATE " + IDN_WEBHOOKS_TABLE +
                " SET " + Column.STATUS + " = '" + STATUS_ACTIVE + "', " +
                Column.UPDATED_AT + " = CURRENT_TIMESTAMP " +
                "WHERE " + Column.UUID + " = ? AND " + Column.TENANT_ID + " = ?";

        public static final String DEACTIVATE_WEBHOOK = "UPDATE " + IDN_WEBHOOKS_TABLE +
                " SET " + Column.STATUS + " = '" + STATUS_INACTIVE + "', " +
                Column.UPDATED_AT + " = CURRENT_TIMESTAMP " +
                "WHERE " + Column.UUID + " = ? AND " + Column.TENANT_ID + " = ?";

        // Webhook event operations
        public static final String ADD_WEBHOOK_EVENT = "INSERT INTO " + IDN_WEBHOOK_EVENTS_TABLE +
                " (" + Column.WEBHOOK_ID + ", " + Column.EVENT_NAME + ") " +
                "VALUES (?, ?)";

        public static final String REMOVE_WEBHOOK_EVENT = "DELETE FROM " + IDN_WEBHOOK_EVENTS_TABLE +
                " WHERE " + Column.WEBHOOK_ID + " = ? AND " + Column.EVENT_NAME + " = ?";

        public static final String LIST_WEBHOOK_EVENTS = "SELECT " + Column.EVENT_NAME + " FROM " +
                IDN_WEBHOOK_EVENTS_TABLE + " WHERE " + Column.WEBHOOK_ID + " = ?";

        public static final String DELETE_WEBHOOK_EVENTS = "DELETE FROM " + IDN_WEBHOOK_EVENTS_TABLE +
                " WHERE " + Column.WEBHOOK_ID + " = ?";

        public static final String LIST_WEBHOOK_ENDPOINTS =
                "SELECT " + Column.ENDPOINT + " FROM " + IDN_WEBHOOKS_TABLE +
                        " WHERE " + Column.TENANT_ID + " = ?";
        public static final String CHECK_WEBHOOK_ENDPOINT_EXISTS =
                "SELECT COUNT(1) FROM " + IDN_WEBHOOKS_TABLE +
                        " WHERE " + Column.ENDPOINT + " = ? AND " + Column.TENANT_ID + " = ?";

        public static final String LIST_ALL_WEBHOOKS = "SELECT * FROM " + IDN_WEBHOOKS_TABLE +
                " WHERE " + Column.TENANT_ID + " = ? " +
                "ORDER BY " + Column.CREATED_AT + " DESC";
    }
}
