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

package org.wso2.carbon.identity.application.role.mgt.constants;

/**
 * Database queries related to application role management CRUD operations.
 */
public class SQLConstants {

    public static final String ADD_APPLICATION_ROLE =  "INSERT INTO APP_ROLE (ROLE_ID, ROLE_NAME, TENANT_ID, APP_ID) " +
            "VALUES (:" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + ";, :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_NAME + ";, :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";, :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_APP_ID + ";);";

    public static final String GET_APPLICATION_ROLE_BY_ID = "SELECT ROLE_ID, ROLE_NAME, TENANT_ID, APP_ID " +
            "FROM APP_ROLE WHERE ROLE_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + ";";

    public static final String GET_APPLICATION_ROLES_OF_APPLICATION = "SELECT ROLE_ID, ROLE_NAME, TENANT_ID, APP_ID " +
            "FROM APP_ROLE WHERE APP_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_APP_ID + ";";

    public static final String IS_APPLICATION_ROLE_EXISTS = "SELECT COUNT(1) FROM APP_ROLE WHERE ROLE_NAME = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_NAME + "; AND APP_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_APP_ID + "; AND TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + "; ";

    public static final String UPDATE_APPLICATION_ROLE_BY_ID = "UPDATE APP_ROLE SET ROLE_NAME = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_NAME + "; WHERE ROLE_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + ";";
    public static final String DELETE_APPLICATION_ROLE_BY_ID = "DELETE FROM APP_ROLE WHERE ROLE_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + ";";

    /**
     * SQL Placeholders.
     */
    public static final class SQLPlaceholders {

        public static final String DB_SCHEMA_COLUMN_NAME_ROLE_ID = "ROLE_ID";
        public static final String DB_SCHEMA_COLUMN_NAME_ROLE_NAME = "ROLE_NAME";
        public static final String DB_SCHEMA_COLUMN_NAME_TENANT_ID = "TENANT_ID";
        public static final String DB_SCHEMA_COLUMN_NAME_APP_ID = "APP_ID";
    }
}
