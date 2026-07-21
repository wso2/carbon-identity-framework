/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.mgt.internal.constant;

/**
 * SQL constants used by the device management DAO layer.
 */
public final class DeviceMgtSQLConstants {

    private DeviceMgtSQLConstants() {
    }

    /**
     * Column and named-parameter names.
     */
    public static final class Column {

        public static final String ID = "ID";
        public static final String DEVICE_ID = "DEVICE_ID";
        public static final String USER_ID = "USER_ID";
        public static final String DEVICE_NAME = "DEVICE_NAME";
        public static final String DEVICE_MODEL = "DEVICE_MODEL";
        public static final String PUBLIC_KEY = "PUBLIC_KEY";
        public static final String STATUS = "STATUS";
        public static final String REGISTERED_AT = "REGISTERED_AT";
        public static final String METADATA = "METADATA";
        public static final String TENANT_ID = "TENANT_ID";

        public static final String LIMIT = "LIMIT";
        public static final String OFFSET = "OFFSET";
        public static final String LOWER_BOUND = "LOWER_BOUND";
        public static final String UPPER_BOUND = "UPPER_BOUND";

        private Column() {
        }
    }

    /**
     * SQL query definitions.
     */
    public static final class Query {

        public static final String REGISTER_DEVICE =
                "INSERT INTO IDN_DEVICE " +
                        "(ID, DEVICE_NAME, DEVICE_MODEL, PUBLIC_KEY, STATUS, REGISTERED_AT, " +
                        "TENANT_ID, METADATA) " +
                        "VALUES (:ID;, :DEVICE_NAME;, :DEVICE_MODEL;, :PUBLIC_KEY;, :STATUS;, " +
                        ":REGISTERED_AT;, :TENANT_ID;, :METADATA;)";

        public static final String ADD_USER_DEVICE =
                "INSERT INTO IDN_USER_DEVICE " +
                        "(DEVICE_ID, USER_ID, TENANT_ID) " +
                        "VALUES (:DEVICE_ID;, :USER_ID;, :TENANT_ID;)";

        public static final String GET_DEVICE_BY_ID =
                "SELECT D.ID, UD.USER_ID, D.DEVICE_NAME, D.DEVICE_MODEL, D.PUBLIC_KEY, D.STATUS, " +
                        "D.REGISTERED_AT, D.METADATA, D.TENANT_ID " +
                        "FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE D.ID = :ID; AND D.TENANT_ID = :TENANT_ID;";

        public static final String GET_DEVICES_BY_USER_ID =
                "SELECT D.ID, UD.USER_ID, D.DEVICE_NAME, D.DEVICE_MODEL, D.PUBLIC_KEY, D.STATUS, " +
                        "D.REGISTERED_AT, D.METADATA, D.TENANT_ID " +
                        "FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE UD.USER_ID = :USER_ID; AND D.STATUS = 'ACTIVE' AND D.TENANT_ID = :TENANT_ID; " +
                        "ORDER BY D.REGISTERED_AT DESC";

        public static final String UPDATE_DEVICE_NAME =
                "UPDATE IDN_DEVICE SET DEVICE_NAME = :DEVICE_NAME; " +
                        "WHERE ID = :ID; AND TENANT_ID = :TENANT_ID;";

        public static final String CHANGE_DEVICE_STATUS =
                "UPDATE IDN_DEVICE SET STATUS = :STATUS; " +
                        "WHERE ID = :ID; AND TENANT_ID = :TENANT_ID;";

        // Paginated tenant-wide device listing. Pagination syntax differs per database, so a variant
        // is selected at runtime based on the detected database type.

        // Default: H2, MySQL, MariaDB, PostgreSQL.
        public static final String GET_ALL_DEVICES_PAGINATED =
                "SELECT D.ID, UD.USER_ID, D.DEVICE_NAME, D.DEVICE_MODEL, D.PUBLIC_KEY, D.STATUS, " +
                        "D.REGISTERED_AT, D.METADATA, D.TENANT_ID " +
                        "FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE D.TENANT_ID = :TENANT_ID; ORDER BY D.REGISTERED_AT DESC, D.ID DESC " +
                        "LIMIT :LIMIT; OFFSET :OFFSET;";

        // MS SQL Server.
        public static final String GET_ALL_DEVICES_PAGINATED_MSSQL =
                "SELECT D.ID, UD.USER_ID, D.DEVICE_NAME, D.DEVICE_MODEL, D.PUBLIC_KEY, D.STATUS, " +
                        "D.REGISTERED_AT, D.METADATA, D.TENANT_ID " +
                        "FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE D.TENANT_ID = :TENANT_ID; ORDER BY D.REGISTERED_AT DESC, D.ID DESC " +
                        "OFFSET :OFFSET; ROWS FETCH NEXT :LIMIT; ROWS ONLY";

        // Oracle.
        public static final String GET_ALL_DEVICES_PAGINATED_ORACLE =
                "SELECT ID, USER_ID, DEVICE_NAME, DEVICE_MODEL, PUBLIC_KEY, STATUS, REGISTERED_AT, " +
                        "METADATA, TENANT_ID FROM (SELECT ID, USER_ID, DEVICE_NAME, DEVICE_MODEL, PUBLIC_KEY, " +
                        "STATUS, REGISTERED_AT, METADATA, TENANT_ID, rownum AS rnum FROM " +
                        "(SELECT D.ID, UD.USER_ID, D.DEVICE_NAME, D.DEVICE_MODEL, D.PUBLIC_KEY, D.STATUS, " +
                        "D.REGISTERED_AT, D.METADATA, D.TENANT_ID FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE D.TENANT_ID = :TENANT_ID; ORDER BY D.REGISTERED_AT DESC, D.ID DESC) " +
                        "WHERE rownum <= :UPPER_BOUND;) WHERE rnum > :OFFSET; ORDER BY rnum";

        // DB2.
        public static final String GET_ALL_DEVICES_PAGINATED_DB2 =
                "SELECT ID, USER_ID, DEVICE_NAME, DEVICE_MODEL, PUBLIC_KEY, STATUS, REGISTERED_AT, " +
                        "METADATA, TENANT_ID FROM (SELECT " +
                        "ROW_NUMBER() OVER(ORDER BY D.REGISTERED_AT DESC, D.ID DESC) AS rn, " +
                        "D.ID, UD.USER_ID, D.DEVICE_NAME, D.DEVICE_MODEL, D.PUBLIC_KEY, D.STATUS, " +
                        "D.REGISTERED_AT, D.METADATA, D.TENANT_ID FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE D.TENANT_ID = :TENANT_ID;) WHERE rn BETWEEN :LOWER_BOUND; AND :UPPER_BOUND; " +
                        "ORDER BY rn";

        public static final String GET_DEVICES_COUNT =
                "SELECT COUNT(*) FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE D.TENANT_ID = :TENANT_ID;";

        // Paginated tenant-wide device listing filtered to a single user. Same DB-specific pagination
        // variants as GET_ALL_DEVICES_PAGINATED, with an added user id condition.

        // Default: H2, MySQL, MariaDB, PostgreSQL.
        public static final String GET_ALL_DEVICES_PAGINATED_BY_USER =
                "SELECT D.ID, UD.USER_ID, D.DEVICE_NAME, D.DEVICE_MODEL, D.PUBLIC_KEY, D.STATUS, " +
                        "D.REGISTERED_AT, D.METADATA, D.TENANT_ID " +
                        "FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE D.TENANT_ID = :TENANT_ID; AND UD.USER_ID = :USER_ID; " +
                        "ORDER BY D.REGISTERED_AT DESC, D.ID DESC LIMIT :LIMIT; OFFSET :OFFSET;";

        // MS SQL Server.
        public static final String GET_ALL_DEVICES_PAGINATED_BY_USER_MSSQL =
                "SELECT D.ID, UD.USER_ID, D.DEVICE_NAME, D.DEVICE_MODEL, D.PUBLIC_KEY, D.STATUS, " +
                        "D.REGISTERED_AT, D.METADATA, D.TENANT_ID " +
                        "FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE D.TENANT_ID = :TENANT_ID; AND UD.USER_ID = :USER_ID; " +
                        "ORDER BY D.REGISTERED_AT DESC, D.ID DESC OFFSET :OFFSET; ROWS FETCH NEXT :LIMIT; ROWS ONLY";

        // Oracle.
        public static final String GET_ALL_DEVICES_PAGINATED_BY_USER_ORACLE =
                "SELECT ID, USER_ID, DEVICE_NAME, DEVICE_MODEL, PUBLIC_KEY, STATUS, REGISTERED_AT, " +
                        "METADATA, TENANT_ID FROM (SELECT ID, USER_ID, DEVICE_NAME, DEVICE_MODEL, PUBLIC_KEY, " +
                        "STATUS, REGISTERED_AT, METADATA, TENANT_ID, rownum AS rnum FROM " +
                        "(SELECT D.ID, UD.USER_ID, D.DEVICE_NAME, D.DEVICE_MODEL, D.PUBLIC_KEY, D.STATUS, " +
                        "D.REGISTERED_AT, D.METADATA, D.TENANT_ID FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE D.TENANT_ID = :TENANT_ID; AND UD.USER_ID = :USER_ID; " +
                        "ORDER BY D.REGISTERED_AT DESC, D.ID DESC) " +
                        "WHERE rownum <= :UPPER_BOUND;) WHERE rnum > :OFFSET; ORDER BY rnum";

        // DB2.
        public static final String GET_ALL_DEVICES_PAGINATED_BY_USER_DB2 =
                "SELECT ID, USER_ID, DEVICE_NAME, DEVICE_MODEL, PUBLIC_KEY, STATUS, REGISTERED_AT, " +
                        "METADATA, TENANT_ID FROM (SELECT " +
                        "ROW_NUMBER() OVER(ORDER BY D.REGISTERED_AT DESC, D.ID DESC) AS rn, " +
                        "D.ID, UD.USER_ID, D.DEVICE_NAME, D.DEVICE_MODEL, D.PUBLIC_KEY, D.STATUS, " +
                        "D.REGISTERED_AT, D.METADATA, D.TENANT_ID FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE D.TENANT_ID = :TENANT_ID; AND UD.USER_ID = :USER_ID;) " +
                        "WHERE rn BETWEEN :LOWER_BOUND; AND :UPPER_BOUND; ORDER BY rn";

        public static final String GET_DEVICES_COUNT_BY_USER =
                "SELECT COUNT(*) FROM IDN_DEVICE D " +
                        "INNER JOIN IDN_USER_DEVICE UD ON D.ID = UD.DEVICE_ID AND D.TENANT_ID = UD.TENANT_ID " +
                        "WHERE D.TENANT_ID = :TENANT_ID; AND UD.USER_ID = :USER_ID;";

        public static final String DELETE_USER_DEVICE =
                "DELETE FROM IDN_USER_DEVICE " +
                        "WHERE DEVICE_ID = :DEVICE_ID; AND TENANT_ID = :TENANT_ID;";

        public static final String DELETE_DEVICE =
                "DELETE FROM IDN_DEVICE " +
                        "WHERE ID = :ID; AND TENANT_ID = :TENANT_ID;";

        private Query() {
        }
    }
}
