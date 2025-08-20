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

/**
 * SQL Constants for Webhook Management.
 * This class is used to store SQL queries and column names.
 */
public final class WebhookSQLConstants {

    private WebhookSQLConstants() {

    }

    /**
     * This class is used to store column names.
     */
    public static final class Column {

        public static final String ID = "ID";
        public static final String UUID = "UUID";
        public static final String ENDPOINT = "ENDPOINT";
        public static final String NAME = "NAME";
        public static final String SECRET_ALIAS = "SECRET_ALIAS";
        public static final String VERSION = "VERSION";
        public static final String EVENT_PROFILE_NAME = "EVENT_PROFILE_NAME";
        public static final String EVENT_PROFILE_URI = "EVENT_PROFILE_URI";
        public static final String EVENT_PROFILE_VERSION = "EVENT_PROFILE_VERSION";
        public static final String STATUS = "STATUS";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String CREATED_AT = "CREATED_AT";
        public static final String UPDATED_AT = "UPDATED_AT";
        public static final String CHANNEL_URI = "CHANNEL_URI";
        public static final String CHANNEL_SUBSCRIPTION_STATUS = "CHANNEL_SUBSCRIPTION_STATUS";
        public static final String WEBHOOK_ID = "WEBHOOK_ID";
        public static final String WEBHOOK_COUNT = "WEBHOOK_COUNT";

        private Column() {

        }
    }

    /**
     * This class is used to store SQL queries.
     */
    public static final class Query {

        public static final String CREATE_WEBHOOK =
                "INSERT INTO IDN_WEBHOOK (UUID, ENDPOINT, NAME, SECRET_ALIAS, VERSION, EVENT_PROFILE_NAME, " +
                        "EVENT_PROFILE_URI, EVENT_PROFILE_VERSION, STATUS, TENANT_ID, CREATED_AT, UPDATED_AT) " +
                        "VALUES (:UUID;, :ENDPOINT;, :NAME;, :SECRET_ALIAS;, :VERSION;, :EVENT_PROFILE_NAME;, " +
                        ":EVENT_PROFILE_URI;, :EVENT_PROFILE_VERSION;, :STATUS;, :TENANT_ID;, CURRENT_TIMESTAMP, " +
                        "CURRENT_TIMESTAMP)";

        public static final String UPDATE_WEBHOOK_STATUS =
                "UPDATE IDN_WEBHOOK SET STATUS = :STATUS;, UPDATED_AT = CURRENT_TIMESTAMP " +
                        "WHERE UUID = :UUID; AND TENANT_ID = :TENANT_ID;";

        public static final String UPDATE_WEBHOOK =
                "UPDATE IDN_WEBHOOK SET ENDPOINT = :ENDPOINT;, NAME = :NAME;, SECRET_ALIAS = :SECRET_ALIAS;, " +
                        "VERSION = :VERSION;, EVENT_PROFILE_NAME = :EVENT_PROFILE_NAME;, " +
                        "EVENT_PROFILE_URI = :EVENT_PROFILE_URI;, " +
                        "EVENT_PROFILE_VERSION = :EVENT_PROFILE_VERSION;, STATUS = :STATUS;," +
                        " UPDATED_AT = CURRENT_TIMESTAMP " +
                        "WHERE UUID = :UUID; AND TENANT_ID = :TENANT_ID;";

        public static final String DELETE_WEBHOOK =
                "DELETE FROM IDN_WEBHOOK WHERE UUID = :UUID; AND TENANT_ID = :TENANT_ID;";

        public static final String GET_WEBHOOK_BY_ID =
                "SELECT * FROM IDN_WEBHOOK WHERE UUID = :UUID; AND TENANT_ID = :TENANT_ID;";

        public static final String GET_WEBHOOKS_BY_TENANT =
                "SELECT * FROM IDN_WEBHOOK WHERE TENANT_ID = :TENANT_ID;";

        public static final String GET_WEBHOOK_INTERNAL_ID_BY_ID =
                "SELECT ID FROM IDN_WEBHOOK WHERE UUID = :UUID; AND TENANT_ID = :TENANT_ID;";

        public static final String CHECK_WEBHOOK_ENDPOINT_EXISTS =
                "SELECT 1 FROM IDN_WEBHOOK WHERE ENDPOINT = :ENDPOINT; AND TENANT_ID = :TENANT_ID;";

        public static final String ADD_WEBHOOK_EVENT =
                "INSERT INTO IDN_WEBHOOK_CHANNELS (WEBHOOK_ID, CHANNEL_URI, CHANNEL_SUBSCRIPTION_STATUS) VALUES " +
                        "(:WEBHOOK_ID;, :CHANNEL_URI;, :CHANNEL_SUBSCRIPTION_STATUS;)";

        public static final String UPDATE_WEBHOOK_EVENT_STATUS =
                "UPDATE IDN_WEBHOOK_CHANNELS SET CHANNEL_SUBSCRIPTION_STATUS = :CHANNEL_SUBSCRIPTION_STATUS; " +
                        "WHERE WEBHOOK_ID = :WEBHOOK_ID; AND CHANNEL_URI = :CHANNEL_URI;";

        public static final String LIST_WEBHOOK_EVENTS_BY_UUID = "SELECT E.CHANNEL_URI, " +
                "E.CHANNEL_SUBSCRIPTION_STATUS FROM IDN_WEBHOOK_CHANNELS E " +
                "INNER JOIN IDN_WEBHOOK W ON E.WEBHOOK_ID = W.ID " +
                "WHERE W.UUID = :UUID; AND W.TENANT_ID = :TENANT_ID;";

        public static final String DELETE_WEBHOOK_EVENTS =
                "DELETE FROM IDN_WEBHOOK_CHANNELS WHERE WEBHOOK_ID = :WEBHOOK_ID;";

        public static final String COUNT_WEBHOOKS_BY_TENANT =
                "SELECT COUNT(*) AS WEBHOOK_COUNT FROM IDN_WEBHOOK WHERE TENANT_ID = :TENANT_ID;";

        public static final String GET_ACTIVE_WEBHOOKS_BY_PROFILE_CHANNEL =
                "SELECT WEBHOOK.* FROM IDN_WEBHOOK WEBHOOK " +
                        "INNER JOIN IDN_WEBHOOK_CHANNELS CHANNEL ON WEBHOOK.ID = CHANNEL.WEBHOOK_ID " +
                        "WHERE CHANNEL.CHANNEL_URI = :CHANNEL_URI; " +
                        "AND WEBHOOK.TENANT_ID = :TENANT_ID; " +
                        "AND WEBHOOK.STATUS = :STATUS; " +
                        "AND WEBHOOK.EVENT_PROFILE_NAME = :EVENT_PROFILE_NAME; " +
                        "AND WEBHOOK.EVENT_PROFILE_VERSION = :EVENT_PROFILE_VERSION;";

        private Query() {

        }
    }
}
