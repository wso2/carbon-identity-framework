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

    public static final String ADD_ROLE_SCOPE =  "INSERT INTO ROLE_SCOPE (ROLE_ID, SCOPE_NAME, TENANT_ID) " +
            "VALUES (:" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + ";, :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_SCOPE_NAME + ";, :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";);";

    public static final String DELETE_ROLE_SCOPE = "DELETE FROM ROLE_SCOPE WHERE ROLE_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + "; AND SCOPE_NAME = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_SCOPE_NAME + "; AND TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";
    public static final String GET_APPLICATION_ROLE_BY_ID = "SELECT ROLE_ID, ROLE_NAME, TENANT_ID, APP_ID " +
            "FROM APP_ROLE WHERE ROLE_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + ";";

    public static final String GET_APPLICATION_ROLE_SCOPE = "SELECT SCOPE_NAME " +
            "FROM ROLE_SCOPE WHERE ROLE_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID
            + "; AND TENANT_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";

    public static final String GET_APPLICATION_ROLES_OF_APPLICATION = "SELECT ROLE_ID, ROLE_NAME, TENANT_ID, APP_ID " +
            "FROM APP_ROLE WHERE APP_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_APP_ID + ";";

    public static final String IS_APPLICATION_ROLE_EXISTS = "SELECT COUNT(1) FROM APP_ROLE WHERE ROLE_NAME = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_NAME + "; AND APP_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_APP_ID + "; AND TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + "; ";

    public static final String UPDATE_APPLICATION_ROLE_BY_ID = "UPDATE APP_ROLE SET ROLE_NAME = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_NAME + "; WHERE ROLE_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + "; AND TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";
    public static final String DELETE_APPLICATION_ROLE_BY_ID = "DELETE FROM APP_ROLE WHERE ROLE_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + "; AND TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";

    // Application role assigned users queries.
    public static final String ADD_APPLICATION_ROLE_USER =  "INSERT INTO USER_ROLE (ROLE_ID, USER_ID, TENANT_ID) " +
            "VALUES (:" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + ";, :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_USER_ID + ";, :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";);";

    public static final String DELETE_ASSIGNED_USER_APPLICATION_ROLE = "DELETE FROM USER_ROLE WHERE ROLE_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + "; AND USER_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_USER_ID + "; AND TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";

    public static final String GET_ASSIGNED_USERS_OF_APPLICATION_ROLE = "SELECT USER_ID " +
            "FROM USER_ROLE WHERE ROLE_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID +
            "; AND TENANT_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";

    // Application role assigned groups queries.
    public static final String ADD_APPLICATION_ROLE_GROUP =  "INSERT INTO GROUP_ROLE (ROLE_ID, GROUP_ID, IDP_ID," +
            " TENANT_ID) VALUES (:" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + ";, :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_GROUP_ID + ";, :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_IDP_ID + ";, :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";);";

    public static final String DELETE_ASSIGNED_GROUP_APPLICATION_ROLE = "DELETE FROM GROUP_ROLE WHERE ROLE_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + "; AND GROUP_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_GROUP_ID + "; AND TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";

    public static final String GET_ASSIGNED_GROUPS_OF_APPLICATION_ROLE = "SELECT GROUP_ID, IDP_ID " +
            "FROM GROUP_ROLE WHERE ROLE_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID +
            "; AND TENANT_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";

    public static final String GET_ASSIGNED_GROUPS_OF_APPLICATION_ROLE_IDP_FILTER = "SELECT GROUP_ID, IDP_ID " +
            "FROM GROUP_ROLE WHERE ROLE_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID +
            "; AND IDP_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_IDP_ID +
            "; AND TENANT_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";
    public static final String GET_APPLICATION_ROLES_BY_USER_ID = "SELECT ar.ROLE_ID, ar.ROLE_NAME, ar.TENANT_ID, " +
            "ar.APP_ID FROM APP_ROLE as ar INNER JOIN USER_ROLE as ur ON ar.ROLE_ID = ur.ROLE_ID WHERE ur.USER_ID = :"
            + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_USER_ID + "; AND ar.TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";
    public static final String GET_APPLICATION_ROLES_BY_USER_ID_APP_ID = "SELECT ar.ROLE_ID, ar.ROLE_NAME, " +
            "ar.TENANT_ID, ar.APP_ID FROM APP_ROLE as ar INNER JOIN USER_ROLE as ur ON ar.ROLE_ID = ur.ROLE_ID " +
            "WHERE ur.USER_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_USER_ID + "; AND ar.APP_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_APP_ID + "; AND ar.TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";
    public static final String GET_APPLICATION_ROLES_BY_GROUP_ID = "SELECT ar.ROLE_ID, ar.ROLE_NAME, ar.TENANT_ID, " +
            "ar.APP_ID FROM APP_ROLE as ar INNER JOIN GROUP_ROLE as gr ON ar.ROLE_ID = gr.ROLE_ID WHERE gr.GROUP_ID = :"
            + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_GROUP_ID + "; AND ar.TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";
    public static final String GET_APPLICATION_ROLES_BY_GROUP_ID_APP_ID = "SELECT ar.ROLE_ID, ar.ROLE_NAME, " +
            "ar.TENANT_ID, ar.APP_ID FROM APP_ROLE as ar INNER JOIN GROUP_ROLE as gr ON ar.ROLE_ID = gr.ROLE_ID " +
            "WHERE gr.GROUP_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_GROUP_ID + "; AND ar.APP_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_APP_ID + "; AND ar.TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";

    public static final String GET_APPLICATION_ROLES_BY_GROUP_IDS = "SELECT ar.ROLE_ID, ar.ROLE_NAME, ar.TENANT_ID, " +
            "ar.APP_ID FROM APP_ROLE as ar INNER JOIN GROUP_ROLE as gr ON ar.ROLE_ID = gr.ROLE_ID WHERE " +
            "ar.APP_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_APP_ID + "; AND ar.TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";

    public static final String GET_SCOPES_BY_ROLE_IDS = "SELECT SCOPE_NAME FROM ROLE_SCOPE WHERE " +
            "TENANT_ID = :" + SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + ";";
    public static final String IS_APPLICATION_ROLE_EXISTS_BY_ID = "SELECT COUNT(1) FROM APP_ROLE WHERE ROLE_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_ROLE_ID + "; AND TENANT_ID = :" +
            SQLPlaceholders.DB_SCHEMA_COLUMN_NAME_TENANT_ID + "; ";

    /**
     * SQL Placeholders.
     */
    public static final class SQLPlaceholders {

        public static final String DB_SCHEMA_COLUMN_NAME_ROLE_ID = "ROLE_ID";
        public static final String DB_SCHEMA_COLUMN_NAME_ROLE_NAME = "ROLE_NAME";
        public static final String DB_SCHEMA_COLUMN_NAME_SCOPE_NAME = "SCOPE_NAME";
        public static final String DB_SCHEMA_COLUMN_NAME_TENANT_ID = "TENANT_ID";
        public static final String DB_SCHEMA_COLUMN_NAME_APP_ID = "APP_ID";
        public static final String DB_SCHEMA_COLUMN_NAME_USER_ID = "USER_ID";
        public static final String DB_SCHEMA_COLUMN_NAME_GROUP_ID = "GROUP_ID";
        public static final String DB_SCHEMA_COLUMN_NAME_IDP_ID = "IDP_ID";
    }

    public static final String USER_ROLE_UNIQUE_CONSTRAINT = "user_role_unique";
    public static final String GROUP_ROLE_UNIQUE_CONSTRAINT = "group_role_unique";
    public static final String ROLE_SCOPE_UNIQUE_CONSTRAINT = "role_scope_unique";
}
