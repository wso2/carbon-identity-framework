/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.thrift.authentication.internal.util;

/**
 * This class holds the constants and SQLQueries related to thrift based authentication
 */
public class ThriftAuthenticationConstants {
    public static final String SECURITY_KEY_STORE_LOCATION = "Security.KeyStore.Location";
    public static final String SECURITY_KEY_STORE_PASSWORD = "Security.KeyStore.Password";

    private ThriftAuthenticationConstants(){

    }

    public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";
    public static final String CLIENT_TIMEOUT = "ClientTimeout";
    public static final int DEFAULT_CLIENT_TIMEOUT = 30000;

    public static final String THRIFT_SESSION_CACHE_ID = "THRIFT_SESSION_CACHE_ID";

    public static final String CHECK_EXISTING_THRIFT_SESSION_SQL =
            "SELECT SESSION_ID FROM IDN_THRIFT_SESSION WHERE SESSION_ID=?";

    public static final String GET_ALL_THRIFT_SESSIONS_SQL =
            "SELECT SESSION_ID, USER_NAME, CREATED_TIME, LAST_MODIFIED_TIME FROM IDN_THRIFT_SESSION";

    public static final String DELETE_SESSION_SQL = "DELETE FROM IDN_THRIFT_SESSION WHERE SESSION_ID=?";

    public static final String ADD_THRIFT_SESSION_SQL =
            "INSERT INTO IDN_THRIFT_SESSION (SESSION_ID, USER_NAME, CREATED_TIME, LAST_MODIFIED_TIME) VALUES (?,?,?,?)";

    public static final String UPDATE_LAST_MODIFIED_TIME_SQL =
            "UPDATE IDN_THRIFT_SESSION SET LAST_MODIFIED_TIME=? WHERE SESSION_ID=?";

    public static final String GET_THRIFT_SESSION_SQL =
            "SELECT SESSION_ID, USER_NAME, CREATED_TIME, LAST_MODIFIED_TIME FROM IDN_THRIFT_SESSION WHERE SESSION_ID=?";
    public static final String IP_ADDRESS = "127.0.0.1";

    public static final String CONFIG_SSL_ENABLED_PROTOCOLS = "SSLEnabledProtocols";
    public static final String CONFIG_CIPHERS = "Ciphers";
}
