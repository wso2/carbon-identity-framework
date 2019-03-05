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

    public static final String MAX_QUERY_LENGTH_SQL = IdentityUtil.getProperty("ConfigurationStore.MaximumQueryLength");
    public static final String INSERT_RESOURCE_TYPE_SQL = "INSERT INTO IDN_CONFIG_TYPE (ID, NAME, DESCRIPTION) " +
            "VALUES (?, ?, ?)";
    public static final String INSERT_OR_UPDATE_RESOURCE_TYPE_MYSQL = "INSERT INTO IDN_CONFIG_TYPE (ID, NAME, " +
            "DESCRIPTION) VALUES (?, ?, ?)  ON DUPLICATE KEY UPDATE NAME = VALUES(NAME), " +
            "DESCRIPTION = VALUES(DESCRIPTION)";
    public static final String INSERT_OR_UPDATE_RESOURCE_TYPE_H2 = "MERGE INTO IDN_CONFIG_TYPE KEY (ID) " +
            "VALUES (?, ?, ?)";
    public static final String GET_CREATED_TIME_COLUMN_MYSQL =
            "SELECT CREATED_TIME FROM IDN_CONFIG_RESOURCE LIMIT 1";
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
            "    HAS_FILE,\n" +
            "    HAS_ATTRIBUTE,\n" +
            "    TYPE_ID\n" +
            "  )\n" +
            "VALUES(?, ?, ?, ?, ?, ?, ?)";
    public static final String INSERT_OR_UPDATE_RESOURCE_MYSQL = "INSERT INTO\n" +
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
    public static final String UPDATE_RESOURCE_H2 =
            "UPDATE IDN_CONFIG_RESOURCE SET ID = ?, TENANT_ID = ?, NAME = ?, LAST_MODIFIED = ?, HAS_FILE = ?, " +
                    "HAS_ATTRIBUTE = ?, TYPE_ID = ?";
    public static final String INSERT_ATTRIBUTES_SQL = "INSERT INTO\n" +
            "  IDN_CONFIG_ATTRIBUTE(\n" +
            "    ID,\n" +
            "    RESOURCE_ID,\n" +
            "    ATTR_KEY,\n" +
            "    ATTR_VALUE\n" +
            "  )\n" +
            "VALUES(?, ?, ?, ?)";
    public static final String UPDATE_ATTRIBUTES_H2 = "MERGE INTO\n" +
            "  IDN_CONFIG_ATTRIBUTE KEY(ID) VALUES(?, ?, ?, ?)";
    public static final String INSERT_OR_UPDATE_ATTRIBUTES_MYSQL = "ON DUPLICATE KEY UPDATE " +
            "RESOURCE_ID = VALUES(RESOURCE_ID), ATTR_KEY = VALUES(ATTR_KEY), ATTR_VALUE = VALUES(ATTR_VALUE)";
    public static final String INSERT_ATTRIBUTE_KEY_VALUE_SQL = ", (?, ?, ?, ?)";
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
    public static final String GET_RESOURCE_TYPE_BY_ID_SQL = "SELECT ID, NAME, DESCRIPTION FROM IDN_CONFIG_TYPE WHERE" +
            " ID = ? ";
    public static final String DELETE_RESOURCE_TYPE_BY_NAME_SQL = "DELETE FROM IDN_CONFIG_TYPE WHERE NAME = ?";
    public static final String DELETE_RESOURCE_TYPE_BY_ID_SQL = "DELETE FROM IDN_CONFIG_TYPE WHERE ID = ?";
    public static final String GET_RESOURCE_ID_BY_NAME_SQL = "SELECT ID FROM IDN_CONFIG_RESOURCE WHERE NAME = ? AND " +
            "TENANT_ID = ? AND TYPE_ID = ?";
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
    public static final String UPDATE_LAST_MODIFIED_SQL = "UPDATE IDN_CONFIG_RESOURCE SET LAST_MODIFIED = ? " +
            "WHERE ID = ?";
}
