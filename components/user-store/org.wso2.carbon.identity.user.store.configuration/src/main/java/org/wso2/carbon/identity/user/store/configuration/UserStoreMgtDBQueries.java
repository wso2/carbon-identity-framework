/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.store.configuration;

/**
 * SQL queries for the secondary user store configurations persisted in the shared database.
 */
public class UserStoreMgtDBQueries {

    public static final String STORE_USERSTORE_PROPERTIES =
            "INSERT INTO UM_SECONDARY_USER_STORE (UM_ID, UM_TENANT_ID, UM_USER_STORE_CONFIG, UM_DOMAIN_NAME) " +
            "VALUES (?,?,?,?)";

    //To update the user store domain name.
    public static final String UPDATE_USERSTORE_PROPERTIES =
            "UPDATE UM_SECONDARY_USER_STORE SET UM_USER_STORE_CONFIG = ?, UM_DOMAIN_NAME = ? " +
            "WHERE UM_DOMAIN_NAME = ? AND UM_TENANT_ID = ?";

    public static final String GET_USERSTORE_PROPERTIES =
            "SELECT UM_USER_STORE_CONFIG FROM UM_SECONDARY_USER_STORE " +
            "WHERE UM_DOMAIN_NAME = ? AND UM_TENANT_ID = ?";

    public static final String GET_ALL_USERSTORE_PROPERTIES =
            "SELECT UM_DOMAIN_NAME, UM_USER_STORE_CONFIG FROM UM_SECONDARY_USER_STORE WHERE UM_TENANT_ID = ?";

    public static final String DELETE_USERSTORE_PROPERTIES =
            "DELETE FROM UM_SECONDARY_USER_STORE WHERE UM_DOMAIN_NAME = ? AND UM_TENANT_ID = ?";
}
