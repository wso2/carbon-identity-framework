/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
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
    public static final String H2 = "H2";
    public static final String ORACLE = "Oracle";

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
    public static final String VALUE_COLUMN_NAME = "VALUE";
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
    public static final String SCOPE_API_ID_COLUMN_NAME = "API_ID";
    public static final String API_RESOURCE_PROPERTY_ID_COLUMN_NAME = "PROPERTY_ID";
    public static final String API_RESOURCE_PROPERTY_NAME_COLUMN_NAME = "PROPERTY_NAME";
    public static final String API_RESOURCE_PROPERTY_VALUE_COLUMN_NAME = "PROPERTY_VALUE";
    public static final String AUTHORIZATION_DETAILS_TYPE_COLUMN_NAME = "TYPE";
    public static final String AUTHORIZATION_DETAILS_SCHEMA_COLUMN_NAME = "JSON_SCHEMA";

    // Placeholders.
    public static final String SCOPE_LIST_PLACEHOLDER = "_SCOPE_LIST_";

    // SQL queries.
    public static final String GET_API_RESOURCES = "SELECT ID, CURSOR_KEY, NAME, IDENTIFIER, DESCRIPTION, TENANT_ID," +
            " TYPE, REQUIRES_AUTHORIZATION FROM API_RESOURCE WHERE ";
    public static final String GET_API_RESOURCES_MSSQL = "SELECT TOP(%d) ID, CURSOR_KEY, NAME, IDENTIFIER, " +
            "DESCRIPTION, TENANT_ID, TYPE, REQUIRES_AUTHORIZATION FROM API_RESOURCE WHERE ";
    public static final String GET_API_RESOURCES_TAIL =
            " (TENANT_ID = %d OR TENANT_ID IS NULL) ORDER BY NAME %s LIMIT %d";
    public static final String GET_API_RESOURCES_TAIL_FOR_ORGANIZATIONS =
            " (TENANT_ID = %d OR TENANT_ID IS NULL) AND TYPE NOT IN ('TENANT', 'SYSTEM', 'CONSOLE_FEATURE') " +
                    "ORDER BY NAME %s LIMIT %d";
    public static final String GET_API_RESOURCES_TAIL_MSSQL =
            " (TENANT_ID = %d OR TENANT_ID IS NULL) ORDER BY NAME %s";
    public static final String GET_API_RESOURCES_TAIL_FOR_ORGANIZATIONS_MSSQL =
            " (TENANT_ID = %d OR TENANT_ID IS NULL) AND TYPE NOT IN ('TENANT', 'SYSTEM', 'CONSOLE_FEATURE') ORDER " +
                    "BY NAME %s";
    public static final String GET_API_RESOURCES_TAIL_ORACLE =
            " (TENANT_ID = %d OR TENANT_ID IS NULL) ORDER BY NAME %s FETCH FIRST %d ROWS ONLY";
    public static final String GET_API_RESOURCES_TAIL_FOR_ORGANIZATIONS_ORACLE =
            " (TENANT_ID = %d OR TENANT_ID IS NULL) AND TYPE NOT IN ('TENANT', 'SYSTEM', 'CONSOLE_FEATURE') " +
                    "ORDER BY NAME %s FETCH FIRST %d ROWS ONLY";
    public static final String GET_API_RESOURCES_WITH_PROPERTIES_SELECTION = "SELECT" +
            " AR.ID AS API_RESOURCE_ID," +
            " AR.CURSOR_KEY AS CURSOR_KEY," +
            " AR.NAME AS API_RESOURCE_NAME," +
            " AR.IDENTIFIER AS API_RESOURCE_IDENTIFIER," +
            " AR.DESCRIPTION AS API_RESOURCE_DESCRIPTION," +
            " AR.TENANT_ID AS API_RESOURCE_TENANT_ID," +
            " AR.TYPE AS API_RESOURCE_TYPE," +
            " AR.REQUIRES_AUTHORIZATION AS REQUIRES_AUTHORIZATION," +
            " ARP.ID AS PROPERTY_ID," +
            " ARP.NAME AS PROPERTY_NAME," +
            " ARP.VALUE AS PROPERTY_VALUE" +
            " FROM (";
    public static final String GET_API_RESOURCES_WITH_PROPERTIES_SELECTION_H2 = "SELECT" +
            " AR.ID AS API_RESOURCE_ID," +
            " AR.CURSOR_KEY AS CURSOR_KEY," +
            " AR.NAME AS API_RESOURCE_NAME," +
            " AR.IDENTIFIER AS API_RESOURCE_IDENTIFIER," +
            " AR.DESCRIPTION AS API_RESOURCE_DESCRIPTION," +
            " AR.TENANT_ID AS API_RESOURCE_TENANT_ID," +
            " AR.TYPE AS API_RESOURCE_TYPE," +
            " AR.REQUIRES_AUTHORIZATION AS REQUIRES_AUTHORIZATION," +
            " ARP.ID AS PROPERTY_ID," +
            " ARP.NAME AS PROPERTY_NAME," +
            " ARP.`VALUE` AS PROPERTY_VALUE" +
            " FROM (";
    public static final String GET_API_RESOURCES_WITH_PROPERTIES_JOIN = ") AR" +
            " LEFT JOIN API_RESOURCE_PROPERTY ARP ON AR.ID = ARP.API_ID ORDER BY CURSOR_KEY %s";
    public static final String GET_API_RESOURCES_COUNT = "SELECT COUNT(DISTINCT(ID)) FROM API_RESOURCE WHERE ";
    public static final String GET_API_RESOURCES_COUNT_TAIL = " (TENANT_ID = ? OR TENANT_ID IS NULL)";
    public static final String GET_API_RESOURCES_COUNT_FOR_ORGANIZATIONS_TAIL =
            " (TENANT_ID = ? OR TENANT_ID IS NULL) AND TYPE NOT IN ('TENANT', 'SYSTEM', 'CONSOLE_FEATURE')";
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
            " FROM API_RESOURCE AR LEFT JOIN SCOPE S ON AR.ID = S.API_ID WHERE AR.ID = ? AND (AR.TENANT_ID = ?" +
            " OR AR.TENANT_ID IS NULL)";

    /**
     * @deprecated Logic moved to service layer. Use {@link #GET_API_RESOURCE_BY_ID} instead.
     */
    @Deprecated
    public static final String GET_API_RESOURCE_BY_ID_FOR_ORGANIZATIONS = "SELECT" +
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
            " FROM API_RESOURCE AR LEFT JOIN SCOPE S ON AR.ID = S.API_ID WHERE AR.ID = ? AND (AR.TENANT_ID = ?" +
            " OR AR.TENANT_ID IS NULL) AND AR.TYPE NOT IN ('TENANT', 'SYSTEM', 'CONSOLE_FEATURE')";
    public static final String GET_SCOPES_BY_API_ID = "SELECT ID, NAME, DISPLAY_NAME, DESCRIPTION, API_ID, TENANT_ID "
            + "FROM SCOPE WHERE API_ID = ? AND (TENANT_ID = ? OR TENANT_ID IS NULL)";
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
            " FROM API_RESOURCE AR LEFT JOIN SCOPE S ON AR.ID = S.API_ID WHERE AR.IDENTIFIER = ? AND" +
            " (AR.TENANT_ID = ? OR AR.TENANT_ID IS NULL)";
    public static final String IS_API_RESOURCE_EXIST_BY_IDENTIFIER = "SELECT ID FROM API_RESOURCE WHERE " +
            "IDENTIFIER = ? AND (TENANT_ID = ? OR TENANT_ID IS NULL)";
    public static final String ADD_API_RESOURCE = "INSERT INTO API_RESOURCE (ID, TYPE, "
            + "NAME, IDENTIFIER, DESCRIPTION, TENANT_ID, REQUIRES_AUTHORIZATION) VALUES (?, ?, ?, ?, ?, ?, ?)";
    public static final String ADD_SCOPE = "INSERT INTO SCOPE (ID, "
            + "NAME, DISPLAY_NAME, DESCRIPTION, API_ID, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String DELETE_API_RESOURCE = "DELETE FROM API_RESOURCE WHERE ID = ? AND ( TENANT_ID = ? " +
            "OR TENANT_ID IS NULL)";
    public static final String DELETE_SCOPES_BY_API = "DELETE FROM SCOPE WHERE API_ID = ? AND ( TENANT_ID = ? " +
            "OR TENANT_ID IS NULL)";
    public static final String UPDATE_API_RESOURCE = "UPDATE API_RESOURCE SET NAME = ?, DESCRIPTION = ?, TYPE = ?" +
            " WHERE ID = ?";
    public static final String UPDATE_SCOPE_METADATA = "UPDATE SCOPE SET DISPLAY_NAME  = ?, DESCRIPTION  = ? " +
            "WHERE NAME = ? AND TENANT_ID = ?";
    public static final String IS_SCOPE_EXIST_BY_ID = "SELECT ID FROM SCOPE WHERE ID = ? AND TENANT_ID = ?";
    public static final String GET_SCOPE_BY_NAME = "SELECT ID, NAME, DISPLAY_NAME, DESCRIPTION, API_ID, TENANT_ID "
            + "FROM SCOPE WHERE NAME = ? AND (TENANT_ID = ? OR TENANT_ID IS NULL)";
    public static final String GET_SCOPE_BY_NAME_AND_API_ID = "SELECT ID, NAME, DISPLAY_NAME, DESCRIPTION, API_ID, " +
            "TENANT_ID FROM SCOPE WHERE NAME = ? AND (TENANT_ID = ? OR TENANT_ID IS NULL) AND API_ID = ?";
    public static final String GET_SCOPE_BY_NAME_API_ID = "SELECT ID, NAME, DISPLAY_NAME, DESCRIPTION, API_ID, " +
            "TENANT_ID FROM SCOPE WHERE NAME = ? AND API_ID = ? AND (TENANT_ID = ? OR TENANT_ID IS NULL)";
    public static final String GET_SCOPES_BY_TENANT_ID = "SELECT ID, NAME, DISPLAY_NAME, DESCRIPTION, API_ID, " +
            "TENANT_ID FROM SCOPE WHERE ";
    public static final String GET_SCOPES_BY_TENANT_ID_TAIL = " (TENANT_ID = ? OR TENANT_ID IS NULL)";
    public static final String GET_SCOPES_BY_TENANT_ID_FOR_ORGANIZATIONS =
            "SELECT SC.ID, SC.NAME, SC.DISPLAY_NAME, SC.DESCRIPTION, SC.API_ID, SC.TENANT_ID FROM SCOPE SC" +
                    " JOIN API_RESOURCE AR ON AR.ID = SC.API_ID" +
                    " WHERE ";
    public static final String GET_SCOPES_BY_TENANT_ID_FOR_ORGANIZATIONS_TAIL = "(AR.TENANT_ID = ? OR AR.TENANT_ID " +
            "IS NULL) AND TYPE NOT IN ('TENANT', 'SYSTEM', 'CONSOLE_FEATURE')";
    public static final String DELETE_SCOPE_BY_NAME = "DELETE FROM SCOPE WHERE NAME = ? AND TENANT_ID = ?";
    public static final String GET_API_RESOURCE_PROPERTIES_BY_API_ID = "SELECT ID, NAME, VALUE FROM " +
            "API_RESOURCE_PROPERTY WHERE API_ID = ?";
    public static final String GET_API_RESOURCE_PROPERTIES_BY_API_ID_H2 = "SELECT ID, NAME, `VALUE` FROM " +
            "API_RESOURCE_PROPERTY WHERE API_ID = ?";
    public static final String GET_API_RESOURCE_PROPERTIES_BY_API_IDENTIFIER = "SELECT ARP.ID, ARP.NAME, ARP.VALUE " +
            "FROM API_RESOURCE AR JOIN API_RESOURCE_PROPERTY ARP ON AR.ID = ARP.API_ID WHERE AR.IDENTIFIER = ? " +
            "AND (AR.TENANT_ID = ? OR AR.TENANT_ID IS NULL)";
    public static final String GET_API_RESOURCE_PROPERTIES_BY_API_IDENTIFIER_H2 = "SELECT ARP.ID, ARP.NAME, " +
            "ARP.`VALUE` FROM API_RESOURCE AR JOIN API_RESOURCE_PROPERTY ARP ON AR.ID = ARP.API_ID WHERE " +
            "AR.IDENTIFIER = ? AND (AR.TENANT_ID = ? OR AR.TENANT_ID IS NULL)";
    public static final String ADD_API_RESOURCE_PROPERTY = "INSERT INTO API_RESOURCE_PROPERTY (API_ID, NAME, VALUE) " +
            "VALUES (?, ?, ?)";
    public static final String ADD_API_RESOURCE_PROPERTY_H2 = "INSERT INTO API_RESOURCE_PROPERTY (API_ID, NAME, " +
            "`VALUE`) VALUES (?, ?, ?)";
    public static final String GET_SCOPE_METADATA = "SELECT" +
            " AR.ID AS API_RESOURCE_ID," +
            " AR.NAME AS API_RESOURCE_NAME," +
            " S.NAME AS SCOPE_QUALIFIED_NAME," +
            " S.DISPLAY_NAME AS SCOPE_DISPLAY_NAME," +
            " S.DESCRIPTION AS SCOPE_DESCRIPTION" +
            " FROM API_RESOURCE AR LEFT JOIN SCOPE S ON AR.ID = S.API_ID WHERE (S.TENANT_ID = ? " +
            "OR S.TENANT_ID IS NULL) AND S.NAME IN (" + SCOPE_LIST_PLACEHOLDER + ")";

    public static final String ADD_AUTHORIZATION_DETAILS_TYPE = "INSERT INTO AUTHORIZATION_DETAILS_TYPES" +
            " (ID, TYPE, API_ID, NAME, DESCRIPTION, JSON_SCHEMA, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
    public static final String GET_AUTHORIZATION_DETAILS_TYPE_BY_TYPE =
            "SELECT ID, TYPE, API_ID, NAME, DESCRIPTION, JSON_SCHEMA, TENANT_ID" +
            " FROM AUTHORIZATION_DETAILS_TYPES WHERE TYPE = ? AND TENANT_ID = ?";
    public static final String GET_AUTHORIZATION_DETAILS_TYPE_BY_API_ID_AND_TYPE =
            "SELECT ID, TYPE, API_ID, NAME, DESCRIPTION, JSON_SCHEMA, TENANT_ID" +
            " FROM AUTHORIZATION_DETAILS_TYPES WHERE API_ID = ? AND TYPE = ? AND TENANT_ID = ?";
    public static final String GET_AUTHORIZATION_DETAILS_TYPE_BY_API_ID_AND_TYPE_ID =
            "SELECT ID, TYPE, API_ID, NAME, DESCRIPTION, JSON_SCHEMA, TENANT_ID" +
            " FROM AUTHORIZATION_DETAILS_TYPES WHERE API_ID = ? AND ID = ? AND TENANT_ID = ?";
    public static final String GET_AUTHORIZATION_DETAILS_TYPE_BY_API_ID =
            "SELECT ID, TYPE, API_ID, NAME, DESCRIPTION, JSON_SCHEMA, TENANT_ID" +
            " FROM AUTHORIZATION_DETAILS_TYPES WHERE API_ID = ? AND TENANT_ID = ?";
    public static final String GET_AUTHORIZATION_DETAILS_TYPE_BY_TENANT_ID_FORMAT =
            "SELECT ID, TYPE, API_ID, NAME, DESCRIPTION, JSON_SCHEMA, TENANT_ID" +
            " FROM AUTHORIZATION_DETAILS_TYPES WHERE %s %s";
    public static final String DELETE_AUTHORIZATION_DETAILS_TYPE_BY_API_ID = "DELETE FROM AUTHORIZATION_DETAILS_TYPES" +
            " WHERE API_ID = ? AND ( TENANT_ID = ? OR TENANT_ID IS NULL)";
    public static final String DELETE_AUTHORIZATION_DETAILS_TYPE_BY_API_ID_AND_TYPE_ID =
            "DELETE FROM AUTHORIZATION_DETAILS_TYPES" +
            " WHERE API_ID = ? AND ID = ? AND ( TENANT_ID = ? OR TENANT_ID IS NULL)";
    public static final String DELETE_AUTHORIZATION_DETAILS_TYPE_BY_API_ID_AND_TYPE =
            "DELETE FROM AUTHORIZATION_DETAILS_TYPES" +
            " WHERE API_ID = ? AND TYPE = ? AND ( TENANT_ID = ? OR TENANT_ID IS NULL)";
    public static final String UPDATE_AUTHORIZATION_DETAILS_TYPES = "UPDATE AUTHORIZATION_DETAILS_TYPES" +
            " SET NAME = ?, TYPE = ?, DESCRIPTION = ?, JSON_SCHEMA = ? WHERE API_ID = ? AND ID = ? AND TENANT_ID = ?";
}
