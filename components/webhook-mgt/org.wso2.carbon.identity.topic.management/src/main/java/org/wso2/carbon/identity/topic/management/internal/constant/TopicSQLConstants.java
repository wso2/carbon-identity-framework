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

package org.wso2.carbon.identity.topic.management.internal.constant;

/**
 * SQL Constants for Topic Management.
 * This class is used to store SQL queries and column names.
 */
public class TopicSQLConstants {

    private TopicSQLConstants() {

    }

    /**
     * This class is used to store column names.
     */
    public static class Column {

        public static final String TOPIC = "TOPIC";
        public static final String CHANNEL_URI = "CHANNEL_URI";
        public static final String EVENT_PROFILE_VERSION = "EVENT_PROFILE_VERSION";
        public static final String TENANT_ID = "TENANT_ID";

        private Column() {

        }
    }

    /**
     * This class is used to store SQL queries.
     */
    public static class Query {

        public static final String ADD_TOPIC =
                "INSERT INTO IDN_WEBHOOK_TOPIC (TOPIC, CHANNEL_URI, EVENT_PROFILE_VERSION, TENANT_ID) " +
                        "VALUES (:TOPIC;, :CHANNEL_URI;, :EVENT_PROFILE_VERSION;, :TENANT_ID;)";
        public static final String DELETE_TOPIC = "DELETE FROM IDN_WEBHOOK_TOPIC WHERE TOPIC = :TOPIC; " +
                "AND TENANT_ID = :TENANT_ID;";
        public static final String CHECK_TOPIC_EXISTS = "SELECT 1 FROM IDN_WEBHOOK_TOPIC WHERE TOPIC = :TOPIC; " +
                "AND TENANT_ID = :TENANT_ID;";

        private Query() {

        }
    }
}
