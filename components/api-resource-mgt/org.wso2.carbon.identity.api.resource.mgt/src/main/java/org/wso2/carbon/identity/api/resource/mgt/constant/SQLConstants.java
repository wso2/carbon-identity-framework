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

package org.wso2.carbon.identity.api.resource.mgt.constant;

/**
 * SQL constants for API resource management service.
 */
public class SQLConstants {

    // DB types.
    public static final String MICROSOFT = "Microsoft";
    public static final String DB2 = "DB2";

    // Column names.
    public static final String ID_COLUMN_NAME = "ID";
    public static final String CURSOR_KEY_COLUMN_NAME = "CURSOR_KEY";
    public static final String NAME_COLUMN_NAME = "NAME";
    public static final String IDENTIFIER_COLUMN_NAME = "IDENTIFIER";
    public static final String DESCRIPTION_COLUMN_NAME = "DESCRIPTION";
    public static final String TENANT_ID_COLUMN_NAME = "TENANT_ID";
    public static final String TYPE_COLUMN_NAME = "TYPE";
    public static final String REQUIRES_AUTHORIZATION_COLUMN_NAME = "REQUIRES_AUTHORIZATION";
    public static final String DISPLAY_NAME_COLUMN_NAME = "DISPLAY_NAME";
    public static final String API_RESOURCE_ID_COLUMN_NAME = "API_RESOURCE_ID";
    public static final String API_RESOURCE_NAME_COLUMN_NAME = "API_RESOURCE_NAME";
    public static final String API_RESOURCE_IDENTIFIER_COLUMN_NAME = "API_RESOURCE_IDENTIFIER";
    public static final String API_RESOURCE_DESCRIPTION_COLUMN_NAME = "API_RESOURCE_DESCRIPTION";
    public static final String API_RESOURCE_TENANT_ID_COLUMN_NAME = "API_RESOURCE_TENANT_ID";
    public static final String API_RESOURCE_TYPE_COLUMN_NAME = "API_RESOURCE_TYPE";
    public static final String SCOPE_ID_COLUMN_NAME = "SCOPE_ID";
    public static final String SCOPE_QUALIFIED_NAME_COLUMN_NAME = "SCOPE_QUALIFIED_NAME";
    public static final String SCOPE_DISPLAY_NAME_COLUMN_NAME = "SCOPE_DISPLAY_NAME";
    public static final String SCOPE_DESCRIPTION_COLUMN_NAME = "SCOPE_DESCRIPTION";

    // Database constraint names.
    public static final String API_RESOURCE_UNIQUE_CONSTRAINT = "identifier_unique";
    public static final String SCOPE_UNIQUE_CONSTRAINT = "scope_unique";
    public static final String DB2_SQL_ERROR_CODE_UNIQUE_CONSTRAINT = "-803";

    // Placeholders.
    public static final String SCOPE_LIST_PLACEHOLDER = "_SCOPE_LIST_";

    // SQL queries.
    public static final String GET_API_RESOURCES = "SELECT ID, CURSOR_KEY, NAME, IDENTIFIER, DESCRIPTION, TENANT_ID," +
            " TYPE, REQUIRES_AUTHORIZATION FROM API_RESOURCE WHERE ";
    public static final String GET_API_RESOURCES_MSSQL = "SELECT TOP(%d) ID, CURSOR_KEY, NAME, IDENTIFIER, " +
            "DESCRIPTION, TENANT_ID, TYPE, REQUIRES_AUTHORIZATION FROM API_RESOURCE WHERE ";
    public static final String GET_API_RESOURCES_TAIL =
            " TENANT_ID = %d ORDER BY CURSOR_KEY %s LIMIT %d";
    public static final String GET_API_RESOURCES_TAIL_MSSQL =
            " TENANT_ID = %d ORDER BY CURSOR_KEY %s";
    public static final String GET_API_RESOURCES_COUNT = "SELECT COUNT(DISTINCT(ID)) FROM API_RESOURCE WHERE ";
    public static final String GET_API_RESOURCES_COUNT_TAIL = " TENANT_ID = ?";
    public static final String GET_API_RESOURCE_BY_ID = "SELECT" +
            " AR.ID AS API_RESOURCE_ID," +
            " AR.NAME AS API_RESOURCE_NAME," +
            " AR.IDENTIFIER AS API_RESOURCE_IDENTIFIER," +
            " AR.DESCRIPTION AS API_RESOURCE_DESCRIPTION," +
            " AR.TENANT_ID AS API_RESOURCE_TENANT_ID," +
            " AR.TYPE AS API_RESOURCE_TYPE," +
            " AR.REQUIRES_AUTHORIZATION AS REQUIRES_AUTHORIZATION," +
            " S.ID AS SCOPE_ID," +
            " S.NAME AS SCOPE_QUALIFIED_NAME," +
            " S.DISPLAY_NAME AS SCOPE_DISPLAY_NAME," +
            " S.DESCRIPTION AS SCOPE_DESCRIPTION" +
            " FROM API_RESOURCE AR LEFT JOIN SCOPE S ON AR.ID = S.API_ID WHERE AR.ID = ? AND AR.TENANT_ID = ?";
    public static final String GET_SCOPES_BY_API_ID = "SELECT ID, NAME, DISPLAY_NAME, DESCRIPTION, API_ID, TENANT_ID "
            + "FROM SCOPE WHERE API_ID = ? AND TENANT_ID = ?";
    public static final String GET_API_RESOURCE_BY_IDENTIFIER = "SELECT" +
            " AR.ID AS API_RESOURCE_ID," +
            " AR.NAME AS API_RESOURCE_NAME," +
            " AR.IDENTIFIER AS API_RESOURCE_IDENTIFIER," +
            " AR.DESCRIPTION AS API_RESOURCE_DESCRIPTION," +
            " AR.TENANT_ID AS API_RESOURCE_TENANT_ID," +
            " AR.TYPE AS API_RESOURCE_TYPE," +
            " AR.REQUIRES_AUTHORIZATION AS REQUIRES_AUTHORIZATION," +
            " S.ID AS SCOPE_ID," +
            " S.NAME AS SCOPE_QUALIFIED_NAME," +
            " S.DISPLAY_NAME AS SCOPE_DISPLAY_NAME," +
            " S.DESCRIPTION AS SCOPE_DESCRIPTION" +
            " FROM API_RESOURCE AR LEFT JOIN SCOPE S ON AR.ID = S.API_ID WHERE AR.IDENTIFIER = ? AND AR.TENANT_ID = ?";
    public static final String IS_API_RESOURCE_EXIST_BY_IDENTIFIER = "SELECT ID FROM API_RESOURCE WHERE " +
            "IDENTIFIER = ? AND TENANT_ID = ?";
    public static final String ADD_API_RESOURCE = "INSERT INTO API_RESOURCE (ID, TYPE, "
            + "NAME, IDENTIFIER, DESCRIPTION, TENANT_ID, REQUIRES_AUTHORIZATION) VALUES (?, ?, ?, ?, ?, ?, ?)";
    public static final String ADD_SCOPE = "INSERT INTO SCOPE (ID, "
            + "NAME, DISPLAY_NAME, DESCRIPTION, API_ID, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String DELETE_API_RESOURCE = "DELETE FROM API_RESOURCE WHERE ID = ? AND TENANT_ID = ?";
    public static final String DELETE_SCOPES_BY_API = "DELETE FROM SCOPE WHERE API_ID = ? AND TENANT_ID = ?";
    public static final String UPDATE_API_RESOURCE = "UPDATE API_RESOURCE SET NAME = ?, DESCRIPTION = ?" +
            " WHERE ID = ?";
    public static final String IS_SCOPE_EXIST_BY_ID = "SELECT ID FROM SCOPE WHERE ID = ? AND TENANT_ID = ?";
    public static final String GET_SCOPE_BY_NAME = "SELECT ID, NAME, DISPLAY_NAME, DESCRIPTION, API_ID, TENANT_ID "
            + "FROM SCOPE WHERE NAME = ? AND TENANT_ID = ?";
    public static final String GET_SCOPE_BY_NAME_API_ID = "SELECT ID, NAME, DISPLAY_NAME, DESCRIPTION, API_ID, " +
            "TENANT_ID FROM SCOPE WHERE NAME = ? AND API_ID = ? AND TENANT_ID = ?";
    public static final String GET_SCOPES_BY_TENANT_ID = "SELECT ID, NAME, DISPLAY_NAME, DESCRIPTION, API_ID, " +
            "TENANT_ID FROM SCOPE WHERE ";
    public static final String GET_SCOPES_BY_TENANT_ID_TAIL = " TENANT_ID = ?";
    public static final String DELETE_SCOPE_BY_NAME = "DELETE FROM SCOPE WHERE NAME = ? AND TENANT_ID = ?";
    public static final String GET_SCOPE_METADATA = "SELECT" +
            " AR.ID AS API_RESOURCE_ID," +
            " AR.NAME AS API_RESOURCE_NAME," +
            " S.NAME AS SCOPE_QUALIFIED_NAME," +
            " S.DISPLAY_NAME AS SCOPE_DISPLAY_NAME," +
            " S.DESCRIPTION AS SCOPE_DESCRIPTION" +
            " FROM API_RESOURCE AR LEFT JOIN SCOPE S ON AR.ID = S.API_ID WHERE S.TENANT_ID = ?" +
            " AND S.NAME IN (" + SCOPE_LIST_PLACEHOLDER + ")";
}
