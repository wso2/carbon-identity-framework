/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.gateway.dao.jdbc;

/**
 * SQLQueries is hold the SQL queries.
 */
public class SQLQueries {

    private static final String OPERATION_DELETE = "DELETE";
    private static final String OPERATION_STORE = "STORE";
    private static final String SQL_INSERT_STORE_OPERATION =
            "INSERT INTO IDN_AUTH_SESSION_STORE(SESSION_ID, SESSION_TYPE, OPERATION, SESSION_OBJECT, TIME_CREATED, "
                    + "TENANT_ID) VALUES (?,?,?,?,?,?)";
    private static final String SQL_INSERT_DELETE_OPERATION =
            "INSERT INTO IDN_AUTH_SESSION_STORE(SESSION_ID, SESSION_TYPE,OPERATION, TIME_CREATED) VALUES (?,?,?,?)";

    private static final String SQL_DESERIALIZE_OBJECT_MYSQL =
            "SELECT OPERATION, SESSION_OBJECT, TIME_CREATED FROM IDN_AUTH_SESSION_STORE WHERE SESSION_ID =? AND" +
                    " SESSION_TYPE=? ORDER BY TIME_CREATED DESC LIMIT 1";
}
