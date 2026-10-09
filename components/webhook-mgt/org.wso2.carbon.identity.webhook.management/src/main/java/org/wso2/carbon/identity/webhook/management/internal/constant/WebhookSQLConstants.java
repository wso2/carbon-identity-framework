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
        public static final String CHANNEL_UUID = "CHANNEL_UUID";
        public static final String CHANNEL_URI = "CHANNEL_URI";
        public static final String CHANNEL_SUBSCRIPTION_STATUS = "CHANNEL_SUBSCRIPTION_STATUS";
        public static final String WEBHOOK_ID = "WEBHOOK_ID";
        public static final String SUBSCRIBED_ORG_TENANT_ID = "SUBSCRIBED_ORG_TENANT_ID";
        public static final String SUBSCRIBED_ORG_ID = "SUBSCRIBED_ORG_ID";
        public static final String TOPIC_UUID = "TOPIC_UUID";
        public static final String TOPIC = "TOPIC";
        public static final String WEBHOOK_COUNT = "WEBHOOK_COUNT";
        public static final String ORG_SUBSCRIPTION_COUNT = "ORG_SUBSCRIPTION_COUNT";

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

        public static final String ADD_WEBHOOK_EVENT_WITH_UUID =
                "INSERT INTO IDN_WEBHOOK_CHANNELS (UUID, WEBHOOK_ID, CHANNEL_URI, " +
                        "CHANNEL_SUBSCRIPTION_STATUS) VALUES " +
                        "(:CHANNEL_UUID;, :WEBHOOK_ID;, :CHANNEL_URI;, :CHANNEL_SUBSCRIPTION_STATUS;)";

        public static final String UPDATE_WEBHOOK_EVENT_STATUS =
                "UPDATE IDN_WEBHOOK_CHANNELS SET CHANNEL_SUBSCRIPTION_STATUS = :CHANNEL_SUBSCRIPTION_STATUS; " +
                        "WHERE WEBHOOK_ID = :WEBHOOK_ID; AND CHANNEL_URI = :CHANNEL_URI;";

        public static final String LIST_WEBHOOK_EVENTS_BY_UUID = "SELECT E.CHANNEL_URI, " +
                "E.CHANNEL_SUBSCRIPTION_STATUS FROM IDN_WEBHOOK_CHANNELS E " +
                "INNER JOIN IDN_WEBHOOK W ON E.WEBHOOK_ID = W.ID " +
                "WHERE W.UUID = :UUID; AND W.TENANT_ID = :TENANT_ID;";

        public static final String DELETE_WEBHOOK_EVENTS =
                "DELETE FROM IDN_WEBHOOK_CHANNELS WHERE WEBHOOK_ID = :WEBHOOK_ID;";

        public static final String LIST_WEBHOOK_CHANNEL_URIS_BY_WEBHOOK_ID =
                "SELECT CHANNEL_URI FROM IDN_WEBHOOK_CHANNELS WHERE WEBHOOK_ID = :WEBHOOK_ID;";

        public static final String DELETE_WEBHOOK_EVENT_BY_CHANNEL_URI =
                "DELETE FROM IDN_WEBHOOK_CHANNELS WHERE WEBHOOK_ID = :WEBHOOK_ID; " +
                        "AND CHANNEL_URI = :CHANNEL_URI;";

        public static final String COUNT_WEBHOOKS_BY_TENANT =
                "SELECT COUNT(*) AS WEBHOOK_COUNT FROM IDN_WEBHOOK WHERE TENANT_ID = :TENANT_ID;";

        // ---------------------------------------------------------------------------------------
        // Organization-level subscription. IDN_WEBHOOK_CHANNEL_ORG_SUB records which descendant
        // organizations feed a webhook channel. The intent those rows were materialised from lives
        // in UM_RESOURCE_SHARING_POLICY, keyed by ResourceType.WEBHOOK_CHANNEL and the channel UUID.
        // ---------------------------------------------------------------------------------------

        public static final String ADD_CHANNEL_ORG_SUBSCRIPTION =
                "INSERT INTO IDN_WEBHOOK_CHANNEL_ORG_SUB (CHANNEL_UUID, SUBSCRIBED_ORG_TENANT_ID, " +
                        "SUBSCRIBED_ORG_ID, TOPIC_UUID, TOPIC) VALUES " +
                        "(:CHANNEL_UUID;, :SUBSCRIBED_ORG_TENANT_ID;, :SUBSCRIBED_ORG_ID;, :TOPIC_UUID;, :TOPIC;)";

        public static final String LIST_CHANNEL_ORG_SUBSCRIPTIONS =
                "SELECT CHANNEL_UUID, SUBSCRIBED_ORG_TENANT_ID, SUBSCRIBED_ORG_ID, TOPIC_UUID, TOPIC " +
                        "FROM IDN_WEBHOOK_CHANNEL_ORG_SUB WHERE CHANNEL_UUID = :CHANNEL_UUID; " +
                        // Paging over this list is applied by the caller, so the order must be
                        // stable across calls. The primary key gives that for free.
                        "ORDER BY SUBSCRIBED_ORG_TENANT_ID";

        public static final String LIST_CHANNEL_ORG_SUBSCRIPTION_TENANTS =
                "SELECT SUBSCRIBED_ORG_TENANT_ID FROM IDN_WEBHOOK_CHANNEL_ORG_SUB " +
                        "WHERE CHANNEL_UUID = :CHANNEL_UUID;";

        public static final String COUNT_CHANNEL_ORG_SUBSCRIPTIONS =
                "SELECT COUNT(*) AS ORG_SUBSCRIPTION_COUNT FROM IDN_WEBHOOK_CHANNEL_ORG_SUB " +
                        "WHERE CHANNEL_UUID = :CHANNEL_UUID;";

        public static final String DELETE_CHANNEL_ORG_SUBSCRIPTIONS_BY_ORG =
                "DELETE FROM IDN_WEBHOOK_CHANNEL_ORG_SUB WHERE SUBSCRIBED_ORG_ID = :SUBSCRIBED_ORG_ID;";

        public static final String DELETE_CHANNEL_ORG_SUBSCRIPTIONS_BY_CHANNEL =
                "DELETE FROM IDN_WEBHOOK_CHANNEL_ORG_SUB WHERE CHANNEL_UUID = :CHANNEL_UUID;";

        public static final String LIST_CHANNEL_UUIDS_BY_WEBHOOK =
                "SELECT CH.UUID FROM IDN_WEBHOOK_CHANNELS CH " +
                        "INNER JOIN IDN_WEBHOOK W ON CH.WEBHOOK_ID = W.ID " +
                        "WHERE W.UUID = :UUID; AND W.TENANT_ID = :TENANT_ID;";

        public static final String GET_OWNING_TENANT_ID_BY_CHANNEL_UUID =
                "SELECT W.TENANT_ID FROM IDN_WEBHOOK_CHANNELS CH " +
                        "INNER JOIN IDN_WEBHOOK W ON CH.WEBHOOK_ID = W.ID " +
                        "WHERE CH.UUID = :CHANNEL_UUID;";

        public static final String GET_CHANNEL_URI_BY_CHANNEL_UUID =
                "SELECT CH.CHANNEL_URI FROM IDN_WEBHOOK_CHANNELS CH WHERE CH.UUID = :CHANNEL_UUID;";

        public static final String GET_CHANNEL_UUID_BY_WEBHOOK_AND_URI =
                "SELECT CH.UUID FROM IDN_WEBHOOK_CHANNELS CH " +
                        "INNER JOIN IDN_WEBHOOK W ON CH.WEBHOOK_ID = W.ID " +
                        "WHERE W.UUID = :UUID; AND W.TENANT_ID = :TENANT_ID; " +
                        "AND CH.CHANNEL_URI = :CHANNEL_URI;";

        /**
         * Active webhooks for an event raised in a given tenant, including webhooks owned by an
         * ancestor organization that has opted this tenant in for the channel.
         * <p>
         * The first branch is the pre-existing behaviour: a webhook configured in the tenant the
         * event was raised in. The EXISTS branch is the organization fanout: a webhook owned
         * elsewhere whose channel carries a subscription row for this tenant. The subscription
         * table is the materialised answer to "which organizations does this channel reach", so no
         * hierarchy walk happens on the publishing path.
         * <p>
         * TENANT_ID and SUBSCRIBED_ORG_TENANT_ID are both bound to the tenant of the event. They
         * are separate parameter names rather than one name used twice, so that the query does not
         * depend on repeated named parameters being supported.
         */
        public static final String GET_ACTIVE_WEBHOOKS_BY_PROFILE_CHANNEL_WITH_SUBSCRIBED_CHILD_ORGS =
                "SELECT WEBHOOK.* FROM IDN_WEBHOOK WEBHOOK " +
                        "INNER JOIN IDN_WEBHOOK_CHANNELS CHANNEL ON WEBHOOK.ID = CHANNEL.WEBHOOK_ID " +
                        "WHERE CHANNEL.CHANNEL_URI = :CHANNEL_URI; " +
                        "AND WEBHOOK.STATUS = :STATUS; " +
                        "AND WEBHOOK.EVENT_PROFILE_NAME = :EVENT_PROFILE_NAME; " +
                        "AND WEBHOOK.EVENT_PROFILE_VERSION = :EVENT_PROFILE_VERSION; " +
                        "AND (WEBHOOK.TENANT_ID = :TENANT_ID; " +
                        "OR EXISTS (SELECT 1 FROM IDN_WEBHOOK_CHANNEL_ORG_SUB SUB " +
                        "WHERE SUB.CHANNEL_UUID = CHANNEL.UUID " +
                        "AND SUB.SUBSCRIBED_ORG_TENANT_ID = :SUBSCRIBED_ORG_TENANT_ID;))";

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
