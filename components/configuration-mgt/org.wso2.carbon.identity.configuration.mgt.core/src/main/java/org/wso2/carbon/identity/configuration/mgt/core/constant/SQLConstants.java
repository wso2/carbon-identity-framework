/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.constant;

import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 * Constants related to SQL operations.
 */
public class SQLConstants {

    public static final String MAX_QUERY_LENGTH_IN_BYTES_SQL =
            IdentityUtil.getProperty("ConfigurationStore.MaximumQueryLengthInBytes");
    public static final String INSERT_RESOURCE_TYPE_SQL = "INSERT INTO IDN_CONFIG_TYPE (ID, NAME, DESCRIPTION) " +
            "VALUES (?, ?, ?)";
    public static final String INSERT_OR_UPDATE_RESOURCE_TYPE_MYSQL = "INSERT INTO IDN_CONFIG_TYPE (ID, NAME, " +
            "DESCRIPTION) VALUES (?, ?, ?)  ON DUPLICATE KEY UPDATE NAME = VALUES(NAME), " +
            "DESCRIPTION = VALUES(DESCRIPTION)";
    public static final String INSERT_OR_UPDATE_RESOURCE_TYPE_H2 = "MERGE INTO IDN_CONFIG_TYPE KEY (ID) " +
            "VALUES (?, ?, ?)";
    public static final String INSERT_OR_UPDATE_RESOURCE_TYPE_POSTGRESQL =
            "INSERT INTO IDN_CONFIG_TYPE (ID, NAME, DESCRIPTION) VALUES (?, ?, ?) " +
                    "ON CONFLICT (ID) DO UPDATE SET NAME = EXCLUDED.NAME, DESCRIPTION = EXCLUDED.DESCRIPTION";
    public static final String INSERT_OR_UPDATE_RESOURCE_TYPE_MSSQL_OR_DB2 = "MERGE IDN_CONFIG_TYPE T USING  (VALUES " +
            "(?, ?, ?)) S (ID,NAME,DESCRIPTION) ON T.ID = S.ID " +
            "WHEN MATCHED THEN UPDATE SET NAME = S.NAME, DESCRIPTION = S.DESCRIPTION " +
            "WHEN NOT MATCHED THEN INSERT (ID,NAME,DESCRIPTION) VALUES (S.ID, S.NAME, S.DESCRIPTION);";
    public static final String INSERT_OR_UPDATE_RESOURCE_TYPE_ORACLE = "MERGE INTO IDN_CONFIG_TYPE USING dual ON " +
            "(ID = ?) " +
            "WHEN MATCHED THEN UPDATE SET NAME = ? , DESCRIPTION = ? " +
            "WHEN NOT MATCHED THEN INSERT (ID, NAME, DESCRIPTION) " +
            "    VALUES (?, ?, ?)";
    public static final String GET_CREATED_TIME_COLUMN_MYSQL =
            "SELECT CREATED_TIME FROM IDN_CONFIG_RESOURCE LIMIT 1";
    public static final String GET_CREATED_TIME_COLUMN_MSSQL = "SELECT TOP 1 CREATED_TIME FROM IDN_CONFIG_RESOURCE";
    public static final String GET_CREATED_TIME_COLUMN_ORACLE = "SELECT CREATED_TIME FROM IDN_CONFIG_RESOURCE WHERE " +
            "ROWNUM <= 1";
    public static final String INSERT_RESOURCE_SQL = "INSERT INTO\n" +
            "  IDN_CONFIG_RESOURCE(\n" +
            "    ID,\n" +
            "    TENANT_ID,\n" +
            "    NAME,\n" +
            "    CREATED_TIME,\n" +
            "    LAST_MODIFIED,\n" +
            "    HAS_FILE,\n" +
            "    HAS_ATTRIBUTE,\n" +
            "    TYPE_ID\n" +
            "  )\n" +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String INSERT_RESOURCE_SQL_WITHOUT_CREATED_TIME = "INSERT INTO\n" +
            "  IDN_CONFIG_RESOURCE(\n" +
            "    ID,\n" +
            "    TENANT_ID,\n" +
            "    NAME,\n" +
            "    LAST_MODIFIED,\n" +
            "    HAS_FILE," +
            "    HAS_ATTRIBUTE," +
            "    TYPE_ID" +
            "  )" +
            "VALUES(?, ?, ?, ?, ?, ?, ?)";
    public static final String INSERT_OR_UPDATE_RESOURCE_MYSQL = "INSERT INTO" +
            "  IDN_CONFIG_RESOURCE(\n" +
            "    ID,\n" +
            "    TENANT_ID,\n" +
            "    NAME,\n" +
            "    CREATED_TIME,\n" +
            "    LAST_MODIFIED,\n" +
            "    HAS_FILE,\n" +
            "    HAS_ATTRIBUTE,\n" +
            "    TYPE_ID\n" +
            "  )\n" +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE TENANT_ID = VALUES(TENANT_ID), NAME = VALUES" +
            "(NAME), " +
            "LAST_MODIFIED = VALUES(LAST_MODIFIED), HAS_FILE = VALUES(HAS_FILE), HAS_ATTRIBUTE = VALUES" +
            "(HAS_ATTRIBUTE), TYPE_ID = VALUES(TYPE_ID)";
    public static final String INSERT_OR_UPDATE_RESOURCE_POSTGRESQL = "INSERT INTO IDN_CONFIG_RESOURCE" +
            "(ID, TENANT_ID, NAME, CREATED_TIME, LAST_MODIFIED, HAS_FILE, HAS_ATTRIBUTE, TYPE_ID) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (ID) DO UPDATE " +
            "SET TENANT_ID = EXCLUDED.TENANT_ID, NAME = EXCLUDED.NAME, LAST_MODIFIED = EXCLUDED.LAST_MODIFIED, " +
            "HAS_FILE = EXCLUDED.HAS_FILE, HAS_ATTRIBUTE = EXCLUDED.HAS_ATTRIBUTE, TYPE_ID = EXCLUDED.TYPE_ID";
    public static final String INSERT_OR_UPDATE_RESOURCE_MSSQL_OR_DB2 = "MERGE INTO IDN_CONFIG_RESOURCE T USING " +
            "(VALUES (?, ?, ?, ?, ?, ?, ?, ?)) S " +
            "(ID, TENANT_ID, NAME, CREATED_TIME, LAST_MODIFIED, HAS_FILE, HAS_ATTRIBUTE, TYPE_ID) ON T.ID = S.ID " +
            "WHEN MATCHED THEN UPDATE SET TENANT_ID = S.TENANT_ID, NAME = S.NAME, LAST_MODIFIED = S.LAST_MODIFIED, " +
            "HAS_FILE = S.HAS_FILE, HAS_ATTRIBUTE = S.HAS_ATTRIBUTE, TYPE_ID = S.TYPE_ID " +
            "WHEN NOT MATCHED THEN INSERT (ID, TENANT_ID, NAME, CREATED_TIME, LAST_MODIFIED, HAS_FILE, HAS_ATTRIBUTE," +
            "TYPE_ID) VALUES (S.ID, S.TENANT_ID, S.NAME, S.CREATED_TIME, S.LAST_MODIFIED, S.HAS_FILE, S" +
            ".HAS_ATTRIBUTE, S.TYPE_ID);";
    public static final String INSERT_OR_UPDATE_RESOURCE_ORACLE = "MERGE INTO IDN_CONFIG_RESOURCE USING dual ON " +
            "(ID = ?) WHEN MATCHED THEN UPDATE SET TENANT_ID = ?, NAME = ? , LAST_MODIFIED = ?, HAS_FILE = ?, " +
            "HAS_ATTRIBUTE = ?, TYPE_ID = ? WHEN NOT MATCHED THEN INSERT (ID, TENANT_ID, NAME, CREATED_TIME, " +
            "LAST_MODIFIED, HAS_FILE, HAS_ATTRIBUTE, TYPE_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String INSERT_OR_UPDATE_RESOURCE_MYSQL_WITHOUT_CREATED_TIME = "INSERT INTO\n" +
            "  IDN_CONFIG_RESOURCE(\n" +
            "    ID,\n" +
            "    TENANT_ID,\n" +
            "    NAME,\n" +
            "    LAST_MODIFIED,\n" +
            "    HAS_FILE,\n" +
            "    HAS_ATTRIBUTE,\n" +
            "    TYPE_ID\n" +
            "  )\n" +
            "VALUES(?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE TENANT_ID = VALUES(TENANT_ID), NAME = VALUES" +
            "(NAME), " +
            "LAST_MODIFIED = VALUES(LAST_MODIFIED), HAS_FILE = VALUES(HAS_FILE), HAS_ATTRIBUTE = VALUES" +
            "(HAS_ATTRIBUTE), TYPE_ID = VALUES(TYPE_ID)";
    public static final String UPDATE_RESOURCE =
            "UPDATE IDN_CONFIG_RESOURCE SET NAME = ?, LAST_MODIFIED = ?, HAS_FILE = ?, HAS_ATTRIBUTE = ? WHERE ID = ?";
    public static final String UPDATE_RESOURCE_H2 =
            "UPDATE IDN_CONFIG_RESOURCE SET TENANT_ID = ?, NAME = ?, LAST_MODIFIED = ?, HAS_FILE = ?, " +
                    "HAS_ATTRIBUTE = ?, TYPE_ID = ? WHERE ID = ?";
    public static final String INSERT_ATTRIBUTES_SQL = "INSERT INTO\n" +
            "  IDN_CONFIG_ATTRIBUTE(\n" +
            "    ID,\n" +
            "    RESOURCE_ID,\n" +
            "    ATTR_KEY,\n" +
            "    ATTR_VALUE\n" +
            "  )\n" +
            "VALUES(?, ?, ?, ?)";
    public static final String INSERT_ATTRIBUTES_MSSQL_OR_DB2 = "MERGE INTO IDN_CONFIG_ATTRIBUTE T USING " +
            "(VALUES (?, ?, ?, ?)";
    public static final String INSERT_ATTRIBUTES_ORACLE = "MERGE INTO IDN_CONFIG_ATTRIBUTE T " +
            "USING (SELECT ? ID, ? RESOURCE_ID, ? ATTR_KEY, ? ATTR_VALUE from dual ";
    public static final String UPDATE_ATTRIBUTES_H2 = "MERGE INTO\n" +
            "  IDN_CONFIG_ATTRIBUTE KEY(ID) VALUES(?, ?, ?, ?)";
    public static final String INSERT_OR_UPDATE_ATTRIBUTES_MYSQL = "ON DUPLICATE KEY UPDATE " +
            "RESOURCE_ID = VALUES(RESOURCE_ID), ATTR_KEY = VALUES(ATTR_KEY), ATTR_VALUE = VALUES(ATTR_VALUE)";
    public static final String INSERT_OR_UPDATE_ATTRIBUTES_POSTGRESQL = "ON CONFLICT(ID) DO UPDATE SET RESOURCE_ID = " +
            "EXCLUDED.RESOURCE_ID, ATTR_KEY = EXCLUDED.ATTR_KEY, ATTR_VALUE = EXCLUDED.ATTR_VALUE";
    public static final String INSERT_OR_UPDATE_ATTRIBUTES_MSSQL_OR_DB2 = ") S (ID, RESOURCE_ID, ATTR_KEY, " +
            "ATTR_VALUE) ON T.ID = S.ID WHEN MATCHED THEN UPDATE SET RESOURCE_ID = S.RESOURCE_ID, " +
            "ATTR_KEY = S.ATTR_KEY, ATTR_VALUE = S.ATTR_VALUE WHEN NOT MATCHED THEN INSERT (ID, RESOURCE_ID, " +
            "ATTR_KEY, ATTR_VALUE) VALUES (S.ID, S.RESOURCE_ID, S.ATTR_KEY, S.ATTR_VALUE);";
    public static final String INSERT_OR_UPDATE_ATTRIBUTES_ORACLE = ") S ON (T.ID = S.ID) " +
            "WHEN MATCHED THEN UPDATE SET T.RESOURCE_ID = S.RESOURCE_ID, T.ATTR_KEY = S.ATTR_KEY, " +
            "T.ATTR_VALUE = S.ATTR_VALUE WHEN NOT MATCHED THEN INSERT (ID, RESOURCE_ID, ATTR_KEY, ATTR_VALUE) " +
            "VALUES (S.ID, S.RESOURCE_ID, S.ATTR_KEY, S.ATTR_VALUE)";
    public static final String INSERT_ATTRIBUTE_KEY_VALUE_SQL = ", (?, ?, ?, ?)";
    public static final String INSERT_ATTRIBUTE_KEY_VALUE_ORACLE = "UNION ALL " +
            "SELECT ? ID, ? RESOURCE_ID, ? ATTR_KEY, ? ATTR_VALUE from dual ";
    public static final String DELETE_RESOURCE_ATTRIBUTES_SQL = "DELETE FROM IDN_CONFIG_ATTRIBUTE WHERE RESOURCE_ID =" +
            " ?";
    public static final String UPDATE_ATTRIBUTE_MYSQL = "UPDATE IDN_CONFIG_ATTRIBUTE SET ATTR_VALUE = ? WHERE ID = ?";
    public static final String INSERT_ATTRIBUTE_MYSQL = "INSERT INTO IDN_CONFIG_ATTRIBUTE(ID, RESOURCE_ID, ATTR_KEY, " +
            "ATTR_VALUE) VALUES(?, ?, ?, ?)";
    public static final String INSERT_OR_UPDATE_ATTRIBUTE_MYSQL = "INSERT IDN_CONFIG_ATTRIBUTE(ID, RESOURCE_ID, " +
            "ATTR_KEY, ATTR_VALUE) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE RESOURCE_ID = VALUES(RESOURCE_ID), " +
            "ATTR_KEY = VALUES(ATTR_KEY), ATTR_VALUE = VALUES(ATTR_VALUE)";
    public static final String INSERT_OR_UPDATE_ATTRIBUTE_H2 = "MERGE INTO IDN_CONFIG_ATTRIBUTE KEY(ID) VALUES(?, ?, "
            + "?, ?)";
    public static final String INSERT_OR_UPDATE_ATTRIBUTE_POSTGRESQL = "INSERT INTO IDN_CONFIG_ATTRIBUTE" +
            "(ID, RESOURCE_ID, ATTR_KEY, ATTR_VALUE) VALUES(?, ?, ?, ?) ON CONFLICT(ID) " +
            "DO UPDATE SET RESOURCE_ID = EXCLUDED.RESOURCE_ID, ATTR_KEY = EXCLUDED.ATTR_KEY, ATTR_VALUE = EXCLUDED" +
            ".ATTR_VALUE";
    public static final String INSERT_OR_UPDATE_ATTRIBUTE_MSSQL_OR_DB2 = "MERGE INTO IDN_CONFIG_ATTRIBUTE T USING " +
            "(VALUES  (?, ?, ?, ?)) S (ID, RESOURCE_ID, ATTR_KEY, ATTR_VALUE) ON T.ID = S.ID " +
            "WHEN MATCHED THEN UPDATE SET RESOURCE_ID = S.RESOURCE_ID, ATTR_KEY = S.ATTR_KEY," +
            "ATTR_VALUE = S.ATTR_VALUE WHEN NOT MATCHED THEN INSERT (ID, RESOURCE_ID, ATTR_KEY, ATTR_VALUE) " +
            "VALUES (S.ID, S.RESOURCE_ID, S.ATTR_KEY, S.ATTR_VALUE);";
    public static final String INSERT_OR_UPDATE_ATTRIBUTE_ORACLE = "MERGE INTO IDN_CONFIG_ATTRIBUTE USING dual " +
            "ON (ID = ?) WHEN MATCHED THEN UPDATE SET RESOURCE_ID = ?, ATTR_KEY = ? , ATTR_VALUE = ? " +
            "WHEN NOT MATCHED THEN INSERT (ID, RESOURCE_ID, ATTR_KEY, ATTR_VALUE) VALUES (?, ?, ?, ?)";
    public static final String GET_ATTRIBUTE_SQL = "SELECT\n" +
            "  ID,\n" +
            "  RESOURCE_ID,\n" +
            "  ATTR_KEY,\n" +
            "  ATTR_VALUE\n" +
            "FROM\n" +
            "  IDN_CONFIG_ATTRIBUTE\n" +
            "WHERE\n" +
            "  ATTR_KEY = ?\n" +
            "  AND RESOURCE_ID = ?";
    public static final String DELETE_ATTRIBUTE_SQL = "DELETE FROM IDN_CONFIG_ATTRIBUTE WHERE ID = ?";
    public static final String GET_RESOURCE_TYPE_BY_NAME_SQL = "SELECT ID, NAME, DESCRIPTION FROM IDN_CONFIG_TYPE " +
            "WHERE NAME = ? ";
    public static final String GET_RESOURCE_TYPE_ID_BY_NAME_SQL = "SELECT ID FROM IDN_CONFIG_TYPE " +
            "WHERE NAME = ? ";
    public static final String GET_RESOURCE_TYPE_BY_ID_SQL = "SELECT ID, NAME, DESCRIPTION FROM IDN_CONFIG_TYPE WHERE" +
            " ID = ? ";
    public static final String DELETE_RESOURCE_TYPE_BY_NAME_SQL = "DELETE FROM IDN_CONFIG_TYPE WHERE NAME = ?";
    public static final String DELETE_RESOURCE_TYPE_BY_ID_SQL = "DELETE FROM IDN_CONFIG_TYPE WHERE ID = ?";
    public static final String GET_RESOURCE_NAME_BY_ID = "SELECT NAME FROM IDN_CONFIG_RESOURCE WHERE ID = ? AND " +
            "TENANT_ID = ?";
    public static final String GET_RESOURCE_ID_BY_NAME_SQL = "SELECT ID FROM IDN_CONFIG_RESOURCE WHERE NAME = ? AND " +
            "TENANT_ID = ? AND TYPE_ID = ?";
    public static final String GET_RESOURCE_ID_TENANT_ID_BY_TYPE_ID_SQL = "SELECT ID, TENANT_ID FROM " +
            "IDN_CONFIG_RESOURCE WHERE TYPE_ID = ?";
    public static final String GET_RESOURCE_BY_NAME_MYSQL = "SELECT\n" +
            "  R.ID,\n" +
            "  R.TENANT_ID,\n" +
            "  R.NAME,\n" +
            "  R.CREATED_TIME,\n" +
            "  R.LAST_MODIFIED,\n" +
            "  R.HAS_FILE,\n" +
            "  R.HAS_ATTRIBUTE,\n" +
            "  T.NAME AS RESOURCE_TYPE,\n" +
            "  T.DESCRIPTION AS DESCRIPTION,\n" +
            "  F.ID AS FILE_ID,\n" +
            "  F.NAME AS FILE_NAME,\n" +
            "  A.ID AS ATTR_ID,\n" +
            "  A.ATTR_KEY AS ATTR_KEY,\n" +
            "  A.ATTR_VALUE AS ATTR_VALUE\n" +
            "FROM\n" +
            "  IDN_CONFIG_RESOURCE AS R\n" +
            "  INNER JOIN IDN_CONFIG_TYPE AS T ON R.TYPE_ID = T.ID\n" +
            "  LEFT JOIN IDN_CONFIG_ATTRIBUTE AS A ON (\n" +
            "    R.HAS_ATTRIBUTE = TRUE\n" +
            "    AND A.RESOURCE_ID = R.ID\n" +
            "  )\n" +
            "  LEFT JOIN IDN_CONFIG_FILE AS F ON (\n" +
            "    R.HAS_FILE = TRUE\n" +
            "    AND F.RESOURCE_ID = R.ID\n" +
            "  )\n" +
            "WHERE\n" +
            "  R.NAME = ?\n" +
            "  AND R.TENANT_ID = ?\n" +
            "  AND R.TYPE_ID = ?";
    public static final String GET_RESOURCE_BY_NAME_MSSQL_OR_ORACLE = "SELECT " +
            "  R.ID," +
            "  R.TENANT_ID," +
            "  R.NAME," +
            "  R.CREATED_TIME," +
            "  R.LAST_MODIFIED," +
            "  R.HAS_FILE," +
            "  R.HAS_ATTRIBUTE," +
            "  T.NAME RESOURCE_TYPE," +
            "  T.DESCRIPTION DESCRIPTION," +
            "  F.ID FILE_ID," +
            "  F.NAME FILE_NAME," +
            "  A.ID ATTR_ID," +
            "  A.ATTR_KEY ATTR_KEY," +
            "  A.ATTR_VALUE ATTR_VALUE " +
            "FROM" +
            "  IDN_CONFIG_RESOURCE R" +
            "  INNER JOIN IDN_CONFIG_TYPE T ON R.TYPE_ID = T.ID" +
            "  LEFT JOIN IDN_CONFIG_ATTRIBUTE A ON (" +
            "    R.HAS_ATTRIBUTE = 1" +
            "    AND A.RESOURCE_ID = R.ID" +
            "  )" +
            "  LEFT JOIN IDN_CONFIG_FILE F ON (" +
            "    R.HAS_FILE = 1" +
            "    AND F.RESOURCE_ID = R.ID" +
            "  )" +
            "WHERE" +
            "  R.NAME = ?" +
            "  AND R.TENANT_ID = ?" +
            "  AND R.TYPE_ID = ?";
    public static final String GET_RESOURCE_BY_NAME_MYSQL_WITHOUT_CREATED_TIME = "SELECT\n" +
            "  R.ID,\n" +
            "  R.TENANT_ID,\n" +
            "  R.NAME,\n" +
            "  R.LAST_MODIFIED,\n" +
            "  R.HAS_FILE,\n" +
            "  R.HAS_ATTRIBUTE,\n" +
            "  T.NAME AS RESOURCE_TYPE,\n" +
            "  T.DESCRIPTION AS DESCRIPTION,\n" +
            "  F.ID AS FILE_ID,\n" +
            "  A.ID AS ATTR_ID,\n" +
            "  A.ATTR_KEY AS ATTR_KEY,\n" +
            "  A.ATTR_VALUE AS ATTR_VALUE\n" +
            "FROM\n" +
            "  IDN_CONFIG_RESOURCE AS R\n" +
            "  INNER JOIN IDN_CONFIG_TYPE AS T ON R.TYPE_ID = T.ID\n" +
            "  LEFT JOIN IDN_CONFIG_ATTRIBUTE AS A ON (\n" +
            "    R.HAS_ATTRIBUTE = TRUE\n" +
            "    AND A.RESOURCE_ID = R.ID\n" +
            "  )\n" +
            "  LEFT JOIN IDN_CONFIG_FILE AS F ON (\n" +
            "    R.HAS_FILE = TRUE\n" +
            "    AND F.RESOURCE_ID = R.ID\n" +
            "  )\n" +
            "WHERE\n" +
            "  R.NAME = ?\n" +
            "  AND R.TENANT_ID = ?\n" +
            "  AND R.TYPE_ID = ?";
    public static final String GET_RESOURCE_BY_ID_MYSQL = "SELECT\n" +
            "  R.ID,\n" +
            "  R.TENANT_ID,\n" +
            "  R.NAME,\n" +
            "  R.CREATED_TIME,\n" +
            "  R.LAST_MODIFIED,\n" +
            "  R.HAS_FILE,\n" +
            "  R.HAS_ATTRIBUTE,\n" +
            "  T.NAME AS RESOURCE_TYPE,\n" +
            "  T.DESCRIPTION AS DESCRIPTION,\n" +
            "  F.ID AS FILE_ID,\n" +
            "  A.ID AS ATTR_ID,\n" +
            "  A.ATTR_KEY AS ATTR_KEY,\n" +
            "  A.ATTR_VALUE AS ATTR_VALUE\n" +
            "FROM\n" +
            "  IDN_CONFIG_RESOURCE AS R\n" +
            "  INNER JOIN IDN_CONFIG_TYPE AS T ON R.TYPE_ID = T.ID\n" +
            "  LEFT JOIN IDN_CONFIG_ATTRIBUTE AS A ON (\n" +
            "    R.HAS_ATTRIBUTE = TRUE\n" +
            "    AND A.RESOURCE_ID = R.ID\n" +
            "  )\n" +
            "  LEFT JOIN IDN_CONFIG_FILE AS F ON (\n" +
            "    R.HAS_FILE = TRUE\n" +
            "    AND F.RESOURCE_ID = R.ID\n" +
            "  )\n" +
            "WHERE\n" +
            "  R.ID = ?\n";
    public static final String GET_RESOURCE_BY_ID_MSSQL_OR_ORACLE = "SELECT" +
            "   R.ID," +
            "   R.TENANT_ID," +
            "   R.NAME," +
            "   R.CREATED_TIME," +
            "   R.LAST_MODIFIED," +
            "   R.HAS_FILE," +
            "   R.HAS_ATTRIBUTE," +
            "   T.NAME RESOURCE_TYPE," +
            "   T.DESCRIPTION DESCRIPTION," +
            "   F.ID FILE_ID," +
            "   A.ID ATTR_ID," +
            "   A.ATTR_KEY ATTR_KEY," +
            "   A.ATTR_VALUE ATTR_VALUE " +
            "FROM" +
            "   IDN_CONFIG_RESOURCE R" +
            "   INNER JOIN IDN_CONFIG_TYPE T ON R.TYPE_ID = T.ID" +
            "   LEFT JOIN IDN_CONFIG_ATTRIBUTE A ON (" +
            "       R.HAS_ATTRIBUTE = 1" +
            "       AND A.RESOURCE_ID = R.ID" +
            "   )" +
            "   LEFT JOIN IDN_CONFIG_FILE F ON (" +
            "        R.HAS_FILE = 1" +
            "        AND F.RESOURCE_ID = R.ID" +
            "   )" +
            "WHERE" +
            "   R.ID = ?";
    public static final String GET_RESOURCE_BY_ID_MYSQL_WITHOUT_CREATED_TIME = "SELECT\n" +
            "  R.ID,\n" +
            "  R.TENANT_ID,\n" +
            "  R.NAME,\n" +
            "  R.LAST_MODIFIED,\n" +
            "  R.HAS_FILE,\n" +
            "  R.HAS_ATTRIBUTE,\n" +
            "  T.NAME AS RESOURCE_TYPE,\n" +
            "  T.DESCRIPTION AS DESCRIPTION,\n" +
            "  F.ID AS FILE_ID,\n" +
            "  A.ID AS ATTR_ID,\n" +
            "  A.ATTR_KEY AS ATTR_KEY,\n" +
            "  A.ATTR_VALUE AS ATTR_VALUE\n" +
            "FROM\n" +
            "  IDN_CONFIG_RESOURCE AS R\n" +
            "  INNER JOIN IDN_CONFIG_TYPE AS T ON R.TYPE_ID = T.ID\n" +
            "  LEFT JOIN IDN_CONFIG_ATTRIBUTE AS A ON (\n" +
            "    R.HAS_ATTRIBUTE = TRUE\n" +
            "    AND A.RESOURCE_ID = R.ID\n" +
            "  )\n" +
            "  LEFT JOIN IDN_CONFIG_FILE AS F ON (\n" +
            "    R.HAS_FILE = TRUE\n" +
            "    AND F.RESOURCE_ID = R.ID\n" +
            "  )\n" +
            "WHERE\n" +
            "  R.ID = ?\n";
    public static final String GET_TENANT_RESOURCES_SELECT_COLUMNS_MYSQL = "SELECT\n" +
            "  R.ID,\n" +
            "  R.TENANT_ID,\n" +
            "  R.NAME,\n" +
            "  R.CREATED_TIME,\n" +
            "  R.LAST_MODIFIED,\n" +
            "  T.NAME AS RESOURCE_TYPE,\n" +
            "  T.DESCRIPTION AS DESCRIPTION,\n" +
            "  F.ID AS FILE_ID,\n" +
            "  A.ID AS ATTR_ID,\n" +
            "  A.ATTR_KEY AS ATTR_KEY,\n" +
            "  A.ATTR_VALUE AS ATTR_VALUE\n" +
            "FROM\n" +
            "  IDN_CONFIG_RESOURCE AS R\n" +
            "  INNER JOIN IDN_CONFIG_TYPE AS T ON R.TYPE_ID = T.ID\n" +
            "  LEFT JOIN IDN_CONFIG_ATTRIBUTE AS A ON (\n" +
            "    R.HAS_ATTRIBUTE = TRUE\n" +
            "    AND A.RESOURCE_ID = R.ID\n" +
            "  )\n" +
            "  LEFT JOIN IDN_CONFIG_FILE AS F ON (\n" +
            "    R.HAS_FILE = TRUE\n" +
            "    AND F.RESOURCE_ID = R.ID\n" +
            "  )\n";
    public static final String GET_TENANT_RESOURCES_SELECT_COLUMNS_MSSQL_OR_ORACLE = "SELECT" +
            "   R.ID," +
            "   R.TENANT_ID," +
            "   R.NAME," +
            "   R.CREATED_TIME," +
            "   R.LAST_MODIFIED," +
            "   T.NAME  RESOURCE_TYPE," +
            "   T.DESCRIPTION  DESCRIPTION," +
            "   F.ID  FILE_ID," +
            "   A.ID  ATTR_ID," +
            "   A.ATTR_KEY  ATTR_KEY," +
            "   A.ATTR_VALUE  ATTR_VALUE " +
            "FROM" +
            "   IDN_CONFIG_RESOURCE  R" +
            "   INNER JOIN IDN_CONFIG_TYPE  T ON R.TYPE_ID = T.ID" +
            "   LEFT JOIN IDN_CONFIG_ATTRIBUTE  A ON (" +
            "       R.HAS_ATTRIBUTE = 1" +
            "       AND A.RESOURCE_ID = R.ID" +
            "   )" +
            "   LEFT JOIN IDN_CONFIG_FILE  F ON (" +
            "       R.HAS_FILE = 1" +
            "       AND F.RESOURCE_ID = R.ID" +
            "   )";
    public static final String GET_TENANT_RESOURCES_SELECT_COLUMNS_MYSQL_WITHOUT_CREATED_TIME = "SELECT\n" +
            "  R.ID,\n" +
            "  R.TENANT_ID,\n" +
            "  R.NAME,\n" +
            "  R.LAST_MODIFIED,\n" +
            "  T.NAME AS RESOURCE_TYPE,\n" +
            "  T.DESCRIPTION AS DESCRIPTION,\n" +
            "  F.ID AS FILE_ID,\n" +
            "  A.ID AS ATTR_ID,\n" +
            "  A.ATTR_KEY AS ATTR_KEY,\n" +
            "  A.ATTR_VALUE AS ATTR_VALUE\n" +
            "FROM\n" +
            "  IDN_CONFIG_RESOURCE AS R\n" +
            "  INNER JOIN IDN_CONFIG_TYPE AS T ON R.TYPE_ID = T.ID\n" +
            "  LEFT JOIN IDN_CONFIG_ATTRIBUTE AS A ON (\n" +
            "    R.HAS_ATTRIBUTE = TRUE\n" +
            "    AND A.RESOURCE_ID = R.ID\n" +
            "  )\n" +
            "  LEFT JOIN IDN_CONFIG_FILE AS F ON (\n" +
            "    R.HAS_FILE = TRUE\n" +
            "    AND F.RESOURCE_ID = R.ID\n" +
            "  )\n";
    public static final String GET_RESOURCE_CREATED_TIME_BY_NAME_SQL = "SELECT\n" +
            "  CREATED_TIME\n" +
            "FROM\n" +
            "  IDN_CONFIG_RESOURCE\n" +
            "WHERE\n" +
            "  NAME = ?\n" +
            "  AND TENANT_ID = ?\n" +
            "  AND TYPE_ID = ?";
    public static final String DELETE_RESOURCE_SQL = "DELETE FROM\n" +
            "  IDN_CONFIG_RESOURCE\n" +
            "WHERE\n" +
            "  NAME = ?\n" +
            "  AND TENANT_ID = ?\n" +
            "  AND TYPE_ID = ?";
    public static final String DELETE_RESOURCE_BY_ID_SQL = "DELETE FROM IDN_CONFIG_RESOURCE WHERE ID = ? AND " +
            "TENANT_ID = ?";
    public static final String UPDATE_LAST_MODIFIED_SQL = "UPDATE IDN_CONFIG_RESOURCE SET LAST_MODIFIED = ? " +
            "WHERE ID = ?";

    public static final String INSERT_FILE_SQL = "INSERT INTO IDN_CONFIG_FILE(ID, VALUE, RESOURCE_ID, NAME) VALUES(?, "
            + "?, ?, ?)";
    public static final String INSERT_FILE_SQL_H2 = "INSERT INTO IDN_CONFIG_FILE(ID, `VALUE`, RESOURCE_ID, NAME) VALUES(?, "
            + "?, ?, ?)";
    public static final String GET_FILE_BY_ID_SQL = "SELECT IDN_CONFIG_FILE.ID, IDN_CONFIG_FILE.VALUE, "
            + "IDN_CONFIG_FILE.RESOURCE_ID, IDN_CONFIG_FILE.NAME FROM IDN_CONFIG_FILE INNER JOIN IDN_CONFIG_RESOURCE ON "
            + "IDN_CONFIG_FILE.RESOURCE_ID = IDN_CONFIG_RESOURCE.ID INNER JOIN IDN_CONFIG_TYPE ON "
            + "IDN_CONFIG_RESOURCE.TYPE_ID = IDN_CONFIG_TYPE.ID where IDN_CONFIG_FILE.ID = ? and "
            + "IDN_CONFIG_RESOURCE.NAME = ? and IDN_CONFIG_TYPE.NAME = ?";
    public static final String GET_FILE_BY_ID_SQL_H2 = "SELECT IDN_CONFIG_FILE.ID, IDN_CONFIG_FILE.`VALUE`, "
            + "IDN_CONFIG_FILE.RESOURCE_ID, IDN_CONFIG_FILE.NAME FROM IDN_CONFIG_FILE INNER JOIN IDN_CONFIG_RESOURCE ON "
            + "IDN_CONFIG_FILE.RESOURCE_ID = IDN_CONFIG_RESOURCE.ID INNER JOIN IDN_CONFIG_TYPE ON "
            + "IDN_CONFIG_RESOURCE.TYPE_ID = IDN_CONFIG_TYPE.ID where IDN_CONFIG_FILE.ID = ? and "
            + "IDN_CONFIG_RESOURCE.NAME = ? and IDN_CONFIG_TYPE.NAME = ?";
    public static final String GET_FILES_BY_RESOURCE_ID_SQL = "SELECT\n" +
            "  ID,\n" +
            "  NAME\n" +
            "FROM\n" +
            "  IDN_CONFIG_FILE\n" +
            "WHERE\n" +
            "  RESOURCE_ID = ?";
    public static final String DELETE_FILE_SQL = "DELETE FROM IDN_CONFIG_FILE WHERE ID = ?";
    public static final String DELETE_FILES_SQL = "DELETE FROM IDN_CONFIG_FILE WHERE RESOURCE_ID = ?";
    public static final String UPDATE_HAS_FILE_SQL = "UPDATE IDN_CONFIG_RESOURCE SET HAS_FILE = ? " +
            "WHERE ID = ?";
    public static final String UPDATE_HAS_ATTRIBUTE_SQL = "UPDATE IDN_CONFIG_RESOURCE SET HAS_ATTRIBUTE = ? " +
            "WHERE ID = ?";
    public static final String GET_ATTRIBUTES_BY_RESOURCE_ID_SQL = "SELECT\n" +
            "  ID,\n" +
            "  ATTR_KEY,\n" +
            "  ATTR_VALUE\n" +
            "  FROM\n" +
            "  IDN_CONFIG_ATTRIBUTE\n" +
            "  WHERE\n" +
            "  RESOURCE_ID = ?";
    public static final String GET_FILES_BY_RESOURCE_TYPE_ID_SQL= "SELECT IDN_CONFIG_FILE.ID, IDN_CONFIG_FILE.NAME "
            + "FILE_NAME, IDN_CONFIG_RESOURCE.NAME RESOURCE_NAME, IDN_CONFIG_TYPE.NAME TYPE_NAME FROM IDN_CONFIG_FILE  "
            + "INNER JOIN IDN_CONFIG_RESOURCE  ON IDN_CONFIG_FILE.RESOURCE_ID = IDN_CONFIG_RESOURCE.ID INNER JOIN "
            + "IDN_CONFIG_TYPE ON IDN_CONFIG_RESOURCE.TYPE_ID = IDN_CONFIG_TYPE.ID where IDN_CONFIG_TYPE.ID = ? AND "
            + "IDN_CONFIG_RESOURCE.TENANT_ID = ?";
    public static final String GET_RESOURCES_BY_RESOURCE_TYPE_ID_SQL =
            "SELECT IDN_CONFIG_RESOURCE.ID, IDN_CONFIG_RESOURCE.TENANT_ID, "
                    + "IDN_CONFIG_RESOURCE.NAME, IDN_CONFIG_RESOURCE.LAST_"
                    + "MODIFIED, IDN_CONFIG_RESOURCE.CREATED_TIME, IDN_CONFIG_RESOURCE.HAS_FILE, IDN_CONFIG_RESOURCE"
                    + ".HAS_ATTRIBUTE FROM IDN_CONFIG_RESOURCE WHERE IDN_CONFIG_RESOURCE."
                    + "TYPE_ID = ? and IDN_CONFIG_RESOURCE.TENANT_ID = ?";
    public static final String DELETE_RESOURCES_BY_RESOURCE_TYPE_ID_SQL =
            "DELETE FROM IDN_CONFIG_RESOURCE WHERE TYPE_ID = ? and TENANT_ID = ?";
}
