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
    public static final String TAG_ID_COLUMN_NAME = "TAG_ID";
    public static final String TAG_NAME_COLUMN_NAME = "TAG_NAME";
    public static final String TAG_COLOUR_COLUMN_NAME = "TAG_COLOUR";
    public static final String TAG_APP_COUNT_COLUMN_NAME = "TAG_APP_COUNT";

    // Database constraint names.
    public static final String APP_TAG_UNIQUE_CONSTRAINT = "tag_unique";
    public static final String DB2_SQL_ERROR_CODE_UNIQUE_CONSTRAINT = "-803";

    // SQL queries.
    public static final String ADD_APPLICATION_TAG = "INSERT INTO TAG (UUID, NAME, COLOUR, TENANT_ID) VALUES (?, ?, " +
            "?, ?)";
    public static final String LOAD_APP_TAG_COUNT_BY_TENANT_AND_FILTER = "SELECT COUNT(TAG.UUID) FROM TAG WHERE " +
            "TAG.TENANT_ID = ? AND (%s)";

    public static final String GET_ALL_APP_TAGS = "SELECT TAG.UUID AS TAG_ID, TAG.NAME AS TAG_NAME, " +
            "TAG.COLOUR AS TAG_COLOUR, IFNULL(TAG_CNT.APP_COUNT,0) as TAG_APP_COUNT FROM TAG LEFT JOIN " +
            "(SELECT SP_TAG.TAG_UUID, COUNT(1) APP_COUNT FROM SP_TAG GROUP BY SP_TAG.TAG_UUID) as TAG_CNT " +
            "ON TAG_CNT.TAG_UUID = TAG.UUID WHERE TAG.TENANT_ID = ? ORDER BY TAG_NAME ASC LIMIT ?, ?";
    public static final String GET_ALL_APP_TAGS_WITH_FILTER = "SELECT TAG.UUID AS TAG_ID, TAG.NAME AS TAG_NAME, " +
            "TAG.COLOUR AS TAG_COLOUR, IFNULL(TAG_CNT.APP_COUNT,0) AS TAG_APP_COUNT FROM TAG LEFT JOIN " +
            "(SELECT SP_TAG.TAG_UUID, COUNT(1) APP_COUNT FROM SP_TAG GROUP BY SP_TAG.TAG_UUID) as TAG_CNT " +
            "ON TAG_CNT.TAG_UUID = TAG.UUID WHERE TAG.TENANT_ID = ? AND (%s) ORDER BY TAG_NAME ASC LIMIT ?, ?";
    public static final String GET_APP_TAG_BY_ID = "SELECT UUID AS TAG_ID, NAME AS TAG_NAME, COLOUR AS TAG_COLOUR, " +
            "(SELECT COUNT(APP_ID) FROM SP_TAG WHERE SP_TAG.TAG_UUID = TAG.UUID) AS TAG_APP_COUNT FROM TAG " +
            "WHERE UUID = ? AND TENANT_ID = ?";
    public static final String LOAD_APP_TAG_ID_BY_TAG_NAME = "SELECT" +
            " UUID AS TAG_ID," +
            " NAME AS TAG_NAME," +
            " COLOUR AS TAG_COLOUR" +
            " FROM TAG WHERE NAME = ? AND TENANT_ID = ?";
    public static final String DELETE_APP_TAG = "DELETE FROM TAG WHERE UUID = ? AND TENANT_ID = ?";
    public static final String UPDATE_APP_TAG = "UPDATE TAG SET NAME = ?, COLOUR = ? WHERE UUID = ? AND TENANT_ID = ?";
}
