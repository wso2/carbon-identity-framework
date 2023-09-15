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

package org.wso2.carbon.identity.application.tag.mgt.constant;

/**
 * SQL constants for Application Tag management service.
 */
public class SQLConstants {

    // DB types.
    public static final String ORACLE = "oracle";
    public static final String MICROSOFT = "Microsoft";
    public static final String POSTGRESQL = "PostgreSQL";
    public static final String H2 = "H2";
    public static final String MYSQL = "MySQL";
    public static final String MARIADB = "MariaDB";
    public static final String DB2 = "DB2";

    // Column names.
    public static final String ID_COLUMN_NAME = "ID";
    public static final String NAME_COLUMN_NAME = "NAME";
    public static final String COLOUR_COLUMN_NAME = "COLOUR";
    public static final String TENANT_ID_COLUMN_NAME = "TENANT_ID";
    public static final String APP_TAG_ID_COLUMN_NAME = "APP_TAG_ID";
    public static final String APP_TAG_NAME_COLUMN_NAME = "APP_TAG_NAME";
    public static final String APP_TAG_COLOUR_COLUMN_NAME = "APP_TAG_COLOUR";
    public static final String APP_TAG_TENANT_ID_COLUMN_NAME = "APP_TAG_TENANT_ID";

    // Database constraint names.
    public static final String APP_TAG_UNIQUE_CONSTRAINT = "tag_unique";
    public static final String DB2_SQL_ERROR_CODE_UNIQUE_CONSTRAINT = "-803";

    // SQL queries.
    public static final String ADD_APPLICATION_TAG = "INSERT INTO TAG (UUID, NAME, COLOUR, TENANT_ID) VALUES (?, ?, " +
            "?, ?)";
    public static final String GET_ALL_APP_TAGS = "SELECT" +
            " UUID AS APP_TAG_ID," +
            " NAME AS APP_TAG_NAME," +
            " COLOUR AS APP_TAG_COLOUR" +
            " FROM TAG WHERE TENANT_ID = ? ORDER BY NAME ASC";
    public static final String GET_APP_TAG_BY_ID = "SELECT" +
            " UUID AS APP_TAG_ID," +
            " NAME AS APP_TAG_NAME," +
            " COLOUR AS APP_TAG_COLOUR," +
            " FROM TAG WHERE UUID = ? AND TENANT_ID = ?";
    public static final String DELETE_APP_TAG = "DELETE FROM TAG WHERE UUID = ? AND TENANT_ID = ?";
    public static final String UPDATE_APP_TAG = "UPDATE TAG SET NAME = ?, COLOUR = ? WHERE UUID = ? AND TENANT_ID = ?";
    public static final String LOAD_APP_TAG_ID_BY_TAG_NAME = "SELECT UUID AS APP_TAG_ID FROM TAG WHERE NAME = ? " +
            "AND TENANT_ID = ?";
}
