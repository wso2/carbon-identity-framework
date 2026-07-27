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

package org.wso2.carbon.identity.policy.management.internal.constant;

/**
 * SQL constants for Policy Management DAO.
 * Two tables: IDN_POLICY (main) and IDN_POLICY_RESOURCE (polymorphic attachment of rules/actions to a policy).
 */
public final class PolicyMgtSQLConstants {

    private PolicyMgtSQLConstants() {

    }

    /** Column name constants. */
    public static final class Column {

        public static final String ID = "ID";
        public static final String POLICY_ID = "POLICY_ID";
        public static final String POLICY_NAME = "POLICY_NAME";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String TARGET = "TARGET";
        public static final String RESOURCE_TYPE = "RESOURCE_TYPE";
        public static final String RESOURCE_ID = "RESOURCE_ID";
        public static final String CREATED_AT = "CREATED_AT";
        public static final String UPDATED_AT = "UPDATED_AT";

        public static final String FILTER = "FILTER";
        public static final String LIMIT = "LIMIT";
        public static final String OFFSET = "OFFSET";
        public static final String LOWER_BOUND = "LOWER_BOUND";
        public static final String UPPER_BOUND = "UPPER_BOUND";

        private Column() {

        }
    }

    /** SQL query constants. */
    public static final class Query {

        // IDN_POLICY table.
        public static final String ADD_POLICY =
                "INSERT INTO IDN_POLICY (ID, POLICY_NAME, TENANT_ID, CREATED_AT, UPDATED_AT) " +
                        "VALUES (:ID;, :POLICY_NAME;, :TENANT_ID;, :CREATED_AT;, :UPDATED_AT;)";

        public static final String UPDATE_POLICY =
                "UPDATE IDN_POLICY SET POLICY_NAME = :POLICY_NAME;, UPDATED_AT = :UPDATED_AT; " +
                        "WHERE ID = :POLICY_ID; AND TENANT_ID = :TENANT_ID;";

        public static final String DELETE_POLICY =
                "DELETE FROM IDN_POLICY WHERE ID = :POLICY_ID; AND TENANT_ID = :TENANT_ID;";

        public static final String GET_POLICY_BY_ID =
                "SELECT ID, POLICY_NAME FROM IDN_POLICY " +
                        "WHERE ID = :POLICY_ID; AND TENANT_ID = :TENANT_ID;";

        public static final String GET_POLICY_BY_NAME =
                "SELECT ID, POLICY_NAME FROM IDN_POLICY " +
                        "WHERE POLICY_NAME = :POLICY_NAME; AND TENANT_ID = :TENANT_ID;";

        // Paginated policy listing. A name filter fragment (LOWER(POLICY_NAME) LIKE LOWER(:FILTER;))
        // is appended via the *_FILTER variants. Pagination syntax differs per database, so a variant
        // is selected at runtime based on the detected database type.

        // Default: H2, MySQL, MariaDB, PostgreSQL.
        public static final String GET_POLICIES_PAGINATED =
                "SELECT ID, POLICY_NAME FROM IDN_POLICY WHERE TENANT_ID = :TENANT_ID; " +
                        "ORDER BY CREATED_AT DESC, ID ASC LIMIT :LIMIT; OFFSET :OFFSET;";

        public static final String GET_POLICIES_PAGINATED_FILTER =
                "SELECT ID, POLICY_NAME FROM IDN_POLICY WHERE TENANT_ID = :TENANT_ID; " +
                        "AND LOWER(POLICY_NAME) LIKE LOWER(:FILTER;) " +
                        "ORDER BY CREATED_AT DESC, ID ASC LIMIT :LIMIT; OFFSET :OFFSET;";

        // MS SQL Server.
        public static final String GET_POLICIES_PAGINATED_MSSQL =
                "SELECT ID, POLICY_NAME FROM IDN_POLICY WHERE TENANT_ID = :TENANT_ID; " +
                        "ORDER BY CREATED_AT DESC, ID ASC OFFSET :OFFSET; ROWS FETCH NEXT :LIMIT; ROWS ONLY";

        public static final String GET_POLICIES_PAGINATED_FILTER_MSSQL =
                "SELECT ID, POLICY_NAME FROM IDN_POLICY WHERE TENANT_ID = :TENANT_ID; " +
                        "AND LOWER(POLICY_NAME) LIKE LOWER(:FILTER;) " +
                        "ORDER BY CREATED_AT DESC, ID ASC OFFSET :OFFSET; ROWS FETCH NEXT :LIMIT; ROWS ONLY";

        // Oracle.
        public static final String GET_POLICIES_PAGINATED_ORACLE =
                "SELECT ID, POLICY_NAME FROM (SELECT ID, POLICY_NAME, rownum AS rnum FROM " +
                        "(SELECT ID, POLICY_NAME FROM IDN_POLICY WHERE TENANT_ID = :TENANT_ID; " +
                        "ORDER BY CREATED_AT DESC, ID ASC) WHERE rownum <= :UPPER_BOUND;) WHERE rnum > :OFFSET;";

        public static final String GET_POLICIES_PAGINATED_FILTER_ORACLE =
                "SELECT ID, POLICY_NAME FROM (SELECT ID, POLICY_NAME, rownum AS rnum FROM " +
                        "(SELECT ID, POLICY_NAME FROM IDN_POLICY WHERE TENANT_ID = :TENANT_ID; " +
                        "AND LOWER(POLICY_NAME) LIKE LOWER(:FILTER;) ORDER BY CREATED_AT DESC, ID ASC) " +
                        "WHERE rownum <= :UPPER_BOUND;) WHERE rnum > :OFFSET;";

        // DB2.
        public static final String GET_POLICIES_PAGINATED_DB2 =
                "SELECT ID, POLICY_NAME FROM (SELECT ROW_NUMBER() OVER(ORDER BY CREATED_AT DESC, ID ASC) AS rn, " +
                        "ID, POLICY_NAME FROM IDN_POLICY WHERE TENANT_ID = :TENANT_ID;) " +
                        "WHERE rn BETWEEN :LOWER_BOUND; AND :UPPER_BOUND;";

        public static final String GET_POLICIES_PAGINATED_FILTER_DB2 =
                "SELECT ID, POLICY_NAME FROM (SELECT ROW_NUMBER() OVER(ORDER BY CREATED_AT DESC, ID ASC) AS rn, " +
                        "ID, POLICY_NAME FROM IDN_POLICY WHERE TENANT_ID = :TENANT_ID; " +
                        "AND LOWER(POLICY_NAME) LIKE LOWER(:FILTER;)) WHERE rn BETWEEN :LOWER_BOUND; AND :UPPER_BOUND;";

        public static final String GET_POLICIES_COUNT =
                "SELECT COUNT(*) FROM IDN_POLICY WHERE TENANT_ID = :TENANT_ID;";

        public static final String GET_POLICIES_COUNT_FILTER =
                "SELECT COUNT(*) FROM IDN_POLICY WHERE TENANT_ID = :TENANT_ID; " +
                        "AND LOWER(POLICY_NAME) LIKE LOWER(:FILTER;)";

        public static final String CHECK_POLICY_NAME_EXISTS =
                "SELECT ID FROM IDN_POLICY " +
                        "WHERE POLICY_NAME = :POLICY_NAME; AND TENANT_ID = :TENANT_ID;";

        // IDN_POLICY_RESOURCE attachment table.
        public static final String ADD_POLICY_RESOURCE =
                "INSERT INTO IDN_POLICY_RESOURCE (ID, POLICY_ID, TARGET, RESOURCE_TYPE, RESOURCE_ID) " +
                        "VALUES (:ID;, :POLICY_ID;, :TARGET;, :RESOURCE_TYPE;, :RESOURCE_ID;)";

        public static final String GET_POLICY_RESOURCES =
                "SELECT ID, TARGET, RESOURCE_TYPE, RESOURCE_ID FROM IDN_POLICY_RESOURCE " +
                        "WHERE POLICY_ID = :POLICY_ID;";

        public static final String DELETE_POLICY_RESOURCES =
                "DELETE FROM IDN_POLICY_RESOURCE WHERE POLICY_ID = :POLICY_ID;";

        private Query() {

        }
    }
}
