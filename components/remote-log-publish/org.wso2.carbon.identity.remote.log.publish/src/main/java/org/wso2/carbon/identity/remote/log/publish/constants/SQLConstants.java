/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.remote.log.publish.constants;

/**
 * This class contains the SQL constants used in the remote log publish feature.
 */
public class SQLConstants {

    public static class Query {

        public static final String INSERT_REMOTE_LOG_PUBLISH_CONFIG_SQL = "INSERT INTO IDN_REMOTE_LOG_PUBLISH_CONFIG " +
                "(UUID, TENANT_DOMAIN, URL, CONNECTION_TIMEOUT, VERIFY_HOSTNAME, LOG_TYPE, USERNAME, PASSWORD, " +
                "KEYSTORE_LOCATION, KEYSTORE_PASSWORD, TRUSTSTORE_LOCATION, TRUSTSTORE_PASSWORD) VALUES " +
                ":UUID;, :TENANT_DOMAIN;, :URL;, :CONNECTION_TIMEOUT;, :VERIFY_HOSTNAME;, :LOG_TYPE;, :USERNAME;, " +
                ":PASSWORD;, :KEYSTORE_LOCATION;, :KEYSTORE_PASSWORD;, :TRUSTSTORE_LOCATION;, :TRUSTSTORE_PASSWORD;";
        public static final String GET_REMOTE_LOG_PUBLISH_CONFIG_SQL = "SELECT UUID, TENANT_DOMAIN, URL, " +
                "CONNECTION_TIMEOUT, VERIFY_HOSTNAME, LOG_TYPE, USERNAME, PASSWORD, KEYSTORE_LOCATION, " +
                "KEYSTORE_PASSWORD, TRUSTSTORE_LOCATION, TRUSTSTORE_PASSWORD FROM IDN_REMOTE_LOG_PUBLISH_CONFIG " +
                "WHERE TENANT_DOMAIN = :TENANT_DOMAIN; AND LOG_TYPE = :LOG_TYPE;";
        public static final String LIST_REMOTE_LOG_PUBLISH_CONFIGS_SQL = "SELECT UUID, TENANT_DOMAIN, URL, " +
                "CONNECTION_TIMEOUT, VERIFY_HOSTNAME, LOG_TYPE, USERNAME, PASSWORD, " +
                "KEYSTORE_LOCATION, KEYSTORE_PASSWORD, TRUSTSTORE_LOCATION, TRUSTSTORE_PASSWORD " +
                "FROM IDN_REMOTE_LOG_PUBLISH_CONFIG " +
                "WHERE TENANT_DOMAIN = :TENANT_DOMAIN;";
        public static final String UPDATE_REMOTE_LOG_PUBLISH_CONFIG_SQL =
                "UPDATE IDN_REMOTE_LOG_PUBLISH_CONFIG " +
                        "SET URL = :URL, CONNECTION_TIMEOUT = :CONNECTION_TIMEOUT, VERIFY_HOSTNAME = :VERIFY_HOSTNAME, " +
                        "USERNAME = :USERNAME, PASSWORD = :PASSWORD, KEYSTORE_LOCATION = :KEYSTORE_LOCATION, " +
                        "KEYSTORE_PASSWORD = :KEYSTORE_PASSWORD, TRUSTSTORE_LOCATION = :TRUSTSTORE_LOCATION, " +
                        "TRUSTSTORE_PASSWORD = :TRUSTSTORE_PASSWORD " +
                        "WHERE UUID = :UUID AND TENANT_DOMAIN = :TENANT_DOMAIN AND LOG_TYPE = :LOG_TYPE;";
        public static final String DELETE_REMOTE_LOG_PUBLISH_CONFIGS_SQL =
                "DELETE FROM IDN_REMOTE_LOG_PUBLISH_CONFIG " +
                        "WHERE TENANT_DOMAIN = :TENANT_DOMAIN AND LOG_TYPE = :LOG_TYPE;";
        public static final String DELETE_ALL_REMOTE_LOG_PUBLISH_CONFIGS_SQL =
                "DELETE FROM IDN_REMOTE_LOG_PUBLISH_CONFIG WHERE TENANT_DOMAIN = :TENANT_DOMAIN;";

    }

    public static class Column {

        public static final String UUID = "UUID";
        public static final String URL = "URL";
        public static final String TENANT_DOMAIN = "TENANT_DOMAIN";
        public static final String CONNECTION_TIMEOUT = "CONNECTION_TIMEOUT";
        public static final String VERIFY_HOSTNAME = "VERIFY_HOSTNAME";
        public static final String LOG_TYPE = "LOG_TYPE";
        public static final String USERNAME = "USERNAME";
        public static final String PASSWORD = "PASSWORD";
        public static final String KEYSTORE_LOCATION = "KEYSTORE_LOCATION";
        public static final String KEYSTORE_PASSWORD = "KEYSTORE_PASSWORD";
        public static final String TRUSTSTORE_LOCATION = "TRUSTSTORE_LOCATION";
        public static final String TRUSTSTORE_PASSWORD = "TRUSTSTORE_PASSWORD";
    }
}
