/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.entitlement.persistence;

/**
 * DB related constant values.
 */
public class PersistenceManagerConstants {

    private PersistenceManagerConstants() {

    }

    public static final String LIMIT = "LIMIT";
    public static final String KEY = "KEY";
    public static final String STATUS_COUNT = "COUNT";

    public static class EntitlementTableColumns {

        private EntitlementTableColumns() {

        }

        // IDN_XACML_POLICY table
        public static final String POLICY_ID = "POLICY_ID";
        public static final String VERSION = "VERSION";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String LAST_MODIFIED_TIME = "LAST_MODIFIED_TIME";
        public static final String LAST_MODIFIED_USER = "LAST_MODIFIED_USER";
        public static final String IS_ACTIVE = "IS_ACTIVE";
        public static final String POLICY_ORDER = "POLICY_ORDER";
        public static final String POLICY_TYPE = "POLICY_TYPE";
        public static final String POLICY_EDITOR = "POLICY_EDITOR";
        public static final String POLICY = "POLICY";
        public static final String IS_IN_PAP = "IS_IN_PAP";
        public static final String IS_IN_PDP = "IS_IN_PDP";

        // IDN_XACML_POLICY_EDITOR_DATA table
        public static final String EDITOR_DATA_ORDER = "DATA_ORDER";
        public static final String EDITOR_DATA = "DATA";

        // IDN_XACML_POLICY_ATTRIBUTE table
        public static final String ATTRIBUTE_ID = "ATTRIBUTE_ID";
        public static final String ATTRIBUTE_VALUE = "ATTRIBUTE_VALUE";
        public static final String DATA_TYPE = "DATA_TYPE";
        public static final String CATEGORY = "CATEGORY";

        // IDN_XACML_POLICY_REFERENCE table
        public static final String REFERENCE = "REFERENCE";

        // IDN_XACML_POLICY_SET_REFERENCE table
        public static final String SET_REFERENCE = "SET_REFERENCE";

        // IDN_XACML_SUBSCRIBER table
        public static final String SUBSCRIBER_ID = "SUBSCRIBER_ID";
        public static final String ENTITLEMENT_MODULE_NAME = "ENTITLEMENT_MODULE_NAME";

        // IDN_XACML_SUBSCRIBER_PROPERTY table
        public static final String PROPERTY_ID = "PROPERTY_ID";
        public static final String DISPLAY_NAME = "DISPLAY_NAME";
        public static final String IS_REQUIRED = "IS_REQUIRED";
        public static final String DISPLAY_ORDER = "DISPLAY_ORDER";
        public static final String IS_SECRET = "IS_SECRET";
        public static final String MODULE = "PROPERTY_MODULE";
        public static final String PROPERTY_VALUE = "PROPERTY_VALUE";

        // IDN_XACML_POLICY_STATUS and IDN_XACML_SUBSCRIBER_STATUS tables
        public static final String STATUS_TYPE = "TYPE";
        public static final String IS_SUCCESS = "IS_SUCCESS";
        public static final String USER = "USERNAME";
        public static final String TARGET = "TARGET";
        public static final String TARGET_ACTION = "TARGET_ACTION";
        public static final String LOGGED_AT = "LOGGED_AT";
        public static final String MESSAGE = "MESSAGE";
        public static final String POLICY_VERSION = "POLICY_VERSION";

        // IDN_XACML_CONFIG table
        public static final String CONFIG_KEY = "CONFIG_KEY";
        public static final String CONFIG_VALUE = "CONFIG_VALUE";
    }

    public static class DatabaseTypes {

        private DatabaseTypes() {

        }

        public static final String MYSQL = "MySQL";
        public static final String MSSQL = "Microsoft SQL Server";
        public static final String ORACLE = "ORACLE";
        public static final String MARIADB = "MariaDB";
        public static final String DB2 = "DB2";
        public static final String H2 = "H2";
        public static final String POSTGRES = "PostgreSQL";
    }

    /**
     * SQL queries for XACML policy storage and management.
     */
    public static class SQLQueries {

        private SQLQueries() {

        }

        // TODO:  revisit all queries using constants like, IN_PAP, IN_PDP, INACTIVE and check if they can be embedded
        /**
         * DB queries related to PAP policy store.
         */
        public static final String CREATE_PAP_POLICY_SQL = "INSERT INTO IDN_XACML_POLICY (POLICY_ID, VERSION, " +
                " IS_IN_PDP, IS_IN_PAP, POLICY, IS_ACTIVE, POLICY_TYPE, POLICY_EDITOR, POLICY_ORDER, " +
                "LAST_MODIFIED_TIME, LAST_MODIFIED_USER, TENANT_ID) VALUES (:POLICY_ID;, :VERSION;, :IS_IN_PDP;, " +
                ":IS_IN_PAP;, :POLICY;, :IS_ACTIVE;, :POLICY_TYPE;, :POLICY_EDITOR;, :POLICY_ORDER;, " +
                ":LAST_MODIFIED_TIME;, :LAST_MODIFIED_USER;, :TENANT_ID;)";
        public static final String CREATE_PAP_POLICY_REFS_SQL = "INSERT INTO IDN_XACML_POLICY_REFERENCE " +
                "(REFERENCE, POLICY_ID, VERSION, TENANT_ID) VALUES (:REFERENCE;, :POLICY_ID;, :VERSION;, :TENANT_ID;)";
        public static final String CREATE_PAP_POLICY_SET_REFS_SQL = "INSERT INTO IDN_XACML_POLICY_SET_REFERENCE " +
                "(SET_REFERENCE, POLICY_ID, VERSION, TENANT_ID) VALUES (:SET_REFERENCE;, :POLICY_ID;, :VERSION;, " +
                ":TENANT_ID;)";
        public static final String CREATE_PAP_POLICY_ATTRIBUTES_SQL = "INSERT INTO IDN_XACML_POLICY_ATTRIBUTE " +
                "(ATTRIBUTE_ID, ATTRIBUTE_VALUE, DATA_TYPE, CATEGORY, POLICY_ID, VERSION, TENANT_ID) VALUES " +
                "(:ATTRIBUTE_ID;, :ATTRIBUTE_VALUE;, :DATA_TYPE;, :CATEGORY;, :POLICY_ID;, :VERSION;, :TENANT_ID;)";
        public static final String CREATE_PAP_POLICY_EDITOR_DATA_SQL = "INSERT INTO IDN_XACML_POLICY_EDITOR_DATA " +
                "(DATA_ORDER, DATA, POLICY_ID, VERSION, TENANT_ID) VALUES (:DATA_ORDER;, :DATA;, :POLICY_ID;, " +
                ":VERSION;, :TENANT_ID;)";
        public static final String GET_PAP_POLICY_IDS_SQL = "SELECT DISTINCT POLICY_ID FROM IDN_XACML_POLICY WHERE " +
                "IS_IN_PAP= :IS_IN_PAP; AND TENANT_ID= :TENANT_ID;";
        public static final String GET_PAP_POLICY_SQL =
                "SELECT POLICY_ID, VERSION, LAST_MODIFIED_TIME, LAST_MODIFIED_USER, IS_ACTIVE, POLICY_ORDER, " +
                        "POLICY_TYPE, POLICY_EDITOR, POLICY, TENANT_ID FROM IDN_XACML_POLICY WHERE " +
                        "IS_IN_PAP = :IS_IN_PAP; AND POLICY_ID = :POLICY_ID; AND VERSION = (SELECT MAX(VERSION) " +
                        "FROM IDN_XACML_POLICY WHERE POLICY_ID = :POLICY_ID; AND TENANT_ID= :TENANT_ID;) " +
                        "AND TENANT_ID = :TENANT_ID;";
        public static final String GET_PAP_POLICY_REFS_SQL = "SELECT REFERENCE FROM IDN_XACML_POLICY_REFERENCE " +
                "WHERE POLICY_ID=:POLICY_ID; AND VERSION=:VERSION; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_PAP_POLICY_SET_REFS_SQL =
                "SELECT SET_REFERENCE FROM IDN_XACML_POLICY_SET_REFERENCE WHERE " +
                        "POLICY_ID=:POLICY_ID; AND VERSION=:VERSION; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_PAP_POLICY_EDITOR_DATA_SQL =
                "SELECT DATA_ORDER, DATA FROM IDN_XACML_POLICY_EDITOR_DATA WHERE POLICY_ID=:POLICY_ID; AND " +
                        "VERSION=:VERSION; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_PAP_POLICY_META_DATA_SQL = "SELECT ATTRIBUTE_ID, ATTRIBUTE_VALUE, DATA_TYPE, " +
                "CATEGORY FROM IDN_XACML_POLICY_ATTRIBUTE WHERE POLICY_ID=:POLICY_ID; AND VERSION=:VERSION; " +
                "AND TENANT_ID=:TENANT_ID;";
        public static final String GET_PAP_POLICY_BY_VERSION_SQL = "SELECT POLICY_ID, LAST_MODIFIED_TIME, " +
                "LAST_MODIFIED_USER, IS_ACTIVE, POLICY_ORDER, POLICY_TYPE, POLICY_EDITOR, POLICY, VERSION, TENANT_ID " +
                "FROM IDN_XACML_POLICY WHERE IS_IN_PAP = :IS_IN_PAP; AND POLICY_ID = :POLICY_ID; AND " +
                "VERSION = :VERSION; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_ALL_PAP_POLICIES_SQL = "SELECT t1.POLICY_ID, t1.VERSION, t1" +
                ".LAST_MODIFIED_TIME, t1.LAST_MODIFIED_USER, t1.IS_ACTIVE, t1.POLICY_ORDER, t1.POLICY_TYPE, " +
                "t1.POLICY_EDITOR, t1.POLICY, t1.TENANT_ID FROM IDN_XACML_POLICY t1 WHERE t1.IS_IN_PAP = :IS_IN_PAP; " +
                "AND t1.VERSION =(SELECT MAX(VERSION) FROM IDN_XACML_POLICY t2 WHERE " +
                "t2.POLICY_ID = t1.POLICY_ID AND t2.TENANT_ID = :TENANT_ID;) AND t1.TENANT_ID = :TENANT_ID;";
        public static final String DELETE_PAP_POLICY_SQL = "UPDATE IDN_XACML_POLICY SET IS_IN_PAP=:IS_IN_PAP; " +
                "WHERE IS_IN_PDP=:IS_IN_PDP; AND POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String DELETE_PAP_POLICY_BY_VERSION_SQL =
                "UPDATE IDN_XACML_POLICY SET IS_IN_PAP=:IS_IN_PAP; " +
                        "WHERE POLICY_ID=:POLICY_ID; AND VERSION=:VERSION; AND TENANT_ID=:TENANT_ID;";
        public static final String DELETE_UNPUBLISHED_POLICY_VERSIONS_SQL = "DELETE FROM IDN_XACML_POLICY " +
                "WHERE IS_IN_PDP=:IS_IN_PDP; AND POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String DELETE_POLICY_SQL =
                "DELETE FROM IDN_XACML_POLICY WHERE POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String DELETE_POLICY_VERSION_SQL =
                "DELETE FROM IDN_XACML_POLICY WHERE POLICY_ID=:POLICY_ID; " +
                        "AND VERSION=:VERSION; AND TENANT_ID=:TENANT_ID;";

        /**
         * DB queries related to PDP policy store.
         */
        public static final String CREATE_POLICY_COMBINING_ALGORITHM_SQL = "INSERT INTO IDN_XACML_CONFIG " +
                "(CONFIG_KEY, CONFIG_VALUE, TENANT_ID) VALUES (:CONFIG_KEY;, :CONFIG_VALUE;, :TENANT_ID;)";
        public static final String GET_POLICY_PDP_PRESENCE_SQL = "SELECT POLICY_ID FROM IDN_XACML_POLICY WHERE " +
                "IS_IN_PDP=:IS_IN_PDP; AND POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_POLICY_PAP_PRESENCE_SQL = "SELECT POLICY_ID FROM IDN_XACML_POLICY WHERE " +
                "IS_IN_PAP=:IS_IN_PAP; AND POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_PDP_POLICY_SQL =
                "SELECT POLICY, POLICY_ORDER, IS_ACTIVE, VERSION FROM IDN_XACML_POLICY WHERE IS_IN_PDP=:IS_IN_PDP; " +
                        "AND POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_POLICY_PDP_PRESENCE_BY_VERSION_SQL = "SELECT POLICY_ID FROM IDN_XACML_POLICY " +
                "WHERE IS_IN_PDP=:IS_IN_PDP; AND POLICY_ID=:POLICY_ID; AND VERSION=:VERSION; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_ALL_PDP_POLICIES_SQL = "SELECT POLICY_ID, POLICY, POLICY_ORDER, IS_ACTIVE, " +
                "VERSION FROM IDN_XACML_POLICY WHERE IS_IN_PDP=:IS_IN_PDP; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_PDP_POLICY_IDS_SQL = "SELECT DISTINCT POLICY_ID FROM IDN_XACML_POLICY WHERE " +
                "IS_IN_PDP=:IS_IN_PDP; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_PUBLISHED_POLICY_VERSION_SQL = "SELECT VERSION FROM IDN_XACML_POLICY WHERE " +
                "IS_IN_PDP=:IS_IN_PDP; AND POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_ACTIVE_STATUS_AND_ORDER_SQL = "SELECT IS_ACTIVE, POLICY_ORDER FROM " +
                "IDN_XACML_POLICY WHERE IS_IN_PDP=:IS_IN_PDP; AND POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_POLICY_COMBINING_ALGORITHM_SQL =
                "SELECT CONFIG_VALUE FROM IDN_XACML_CONFIG WHERE CONFIG_KEY=:CONFIG_KEY; AND TENANT_ID=:TENANT_ID;";
        public static final String UPDATE_ACTIVE_STATUS_SQL =
                "UPDATE IDN_XACML_POLICY SET IS_ACTIVE=:IS_ACTIVE; WHERE POLICY_ID=:POLICY_ID; AND " +
                        "VERSION=:VERSION; AND TENANT_ID=:TENANT_ID;";
        public static final String UPDATE_ORDER_SQL = "UPDATE IDN_XACML_POLICY SET POLICY_ORDER=:POLICY_ORDER; WHERE " +
                "POLICY_ID=:POLICY_ID; AND VERSION=:VERSION; AND TENANT_ID=:TENANT_ID;";
        public static final String DELETE_PUBLISHED_VERSIONS_SQL =
                "UPDATE IDN_XACML_POLICY SET IS_IN_PDP=:IS_IN_PDP;, IS_ACTIVE=:IS_ACTIVE;, POLICY_ORDER=:POLICY_ORDER;" +
                        " WHERE IS_IN_PDP=:IS_IN_PDP_1; AND POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String PUBLISH_POLICY_VERSION_SQL =
                "UPDATE IDN_XACML_POLICY SET IS_IN_PDP=:IS_IN_PDP; WHERE POLICY_ID=:POLICY_ID; " +
                        "AND VERSION=:VERSION; AND TENANT_ID=:TENANT_ID;";
        public static final String RESTORE_ACTIVE_STATUS_AND_ORDER_SQL = "UPDATE IDN_XACML_POLICY SET " +
                "IS_ACTIVE=:IS_ACTIVE;, POLICY_ORDER=:POLICY_ORDER; WHERE POLICY_ID=:POLICY_ID; AND " +
                "VERSION=:VERSION; AND TENANT_ID=:TENANT_ID;";
        public static final String UPDATE_POLICY_COMBINING_ALGORITHM_SQL = "UPDATE IDN_XACML_CONFIG SET " +
                "CONFIG_VALUE=:CONFIG_VALUE; WHERE CONFIG_KEY=:CONFIG_KEY; AND TENANT_ID=:TENANT_ID;";
        public static final String DELETE_UNUSED_POLICY_SQL =
                "DELETE FROM IDN_XACML_POLICY WHERE IS_IN_PAP=:IS_IN_PAP; AND IS_IN_PDP=:IS_IN_PDP; AND " +
                        "POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID; ";

        /**
         * DB queries related to subscribers.
         */
        public static final String CREATE_SUBSCRIBER_SQL =
                "INSERT INTO IDN_XACML_SUBSCRIBER (SUBSCRIBER_ID, ENTITLEMENT_MODULE_NAME, TENANT_ID) VALUES " +
                        "(:SUBSCRIBER_ID;, :ENTITLEMENT_MODULE_NAME;, :TENANT_ID;)";
        public static final String CREATE_SUBSCRIBER_PROPERTIES_SQL = "INSERT INTO IDN_XACML_SUBSCRIBER_PROPERTY " +
                "(PROPERTY_ID, DISPLAY_NAME, PROPERTY_VALUE, IS_REQUIRED, DISPLAY_ORDER, IS_SECRET, " +
                "PROPERTY_MODULE, SUBSCRIBER_ID, TENANT_ID) VALUES (:PROPERTY_ID;, :DISPLAY_NAME;, :PROPERTY_VALUE;, " +
                ":IS_REQUIRED;, :DISPLAY_ORDER;, :IS_SECRET;, :PROPERTY_MODULE;, :SUBSCRIBER_ID;, :TENANT_ID;)";
        public static final String GET_SUBSCRIBER_EXISTENCE_SQL = "SELECT SUBSCRIBER_ID FROM IDN_XACML_SUBSCRIBER " +
                "WHERE SUBSCRIBER_ID=:SUBSCRIBER_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_SUBSCRIBER_SQL = "SELECT s.SUBSCRIBER_ID, s.ENTITLEMENT_MODULE_NAME, s.TENANT_ID, " +
                "p.PROPERTY_ID, p.DISPLAY_NAME, p.PROPERTY_VALUE, p.IS_REQUIRED, p.DISPLAY_ORDER, p.IS_SECRET, " +
                "p.PROPERTY_MODULE FROM IDN_XACML_SUBSCRIBER s INNER JOIN " +
                "IDN_XACML_SUBSCRIBER_PROPERTY p ON s.SUBSCRIBER_ID = p.SUBSCRIBER_ID AND s.TENANT_ID = p.TENANT_ID " +
                "WHERE s.SUBSCRIBER_ID = :SUBSCRIBER_ID; AND s.TENANT_ID = :TENANT_ID;";
        public static final String GET_SUBSCRIBER_IDS_SQL = "SELECT SUBSCRIBER_ID FROM IDN_XACML_SUBSCRIBER " +
                "WHERE TENANT_ID=:TENANT_ID;";
        public static final String UPDATE_SUBSCRIBER_MODULE_SQL = "UPDATE IDN_XACML_SUBSCRIBER " +
                "SET ENTITLEMENT_MODULE_NAME=:ENTITLEMENT_MODULE_NAME; WHERE " +
                "SUBSCRIBER_ID=:SUBSCRIBER_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String UPDATE_SUBSCRIBER_PROPERTIES_SQL = "UPDATE IDN_XACML_SUBSCRIBER_PROPERTY SET " +
                "PROPERTY_VALUE=:PROPERTY_VALUE; WHERE PROPERTY_ID=:PROPERTY_ID; AND SUBSCRIBER_ID=:SUBSCRIBER_ID; AND " +
                "TENANT_ID=:TENANT_ID;";
        public static final String DELETE_SUBSCRIBER_SQL = "DELETE FROM IDN_XACML_SUBSCRIBER WHERE " +
                "SUBSCRIBER_ID=:SUBSCRIBER_ID; AND TENANT_ID=:TENANT_ID;";

        /**
         * DB queries related to status.
         */
        public static final String CREATE_POLICY_STATUS_SQL = "INSERT INTO IDN_XACML_POLICY_STATUS (TYPE, IS_SUCCESS, " +
                "USERNAME, TARGET, TARGET_ACTION, LOGGED_AT, MESSAGE, POLICY_ID, POLICY_VERSION, TENANT_ID) " +
                "VALUES (:TYPE;, :IS_SUCCESS;, :USERNAME;, :TARGET;, :TARGET_ACTION;, :LOGGED_AT;, :MESSAGE;, " +
                ":KEY;, :VERSION;, :TENANT_ID;)";
        public static final String CREATE_SUBSCRIBER_STATUS_SQL = "INSERT INTO IDN_XACML_SUBSCRIBER_STATUS " +
                "(TYPE, IS_SUCCESS, USERNAME, TARGET, TARGET_ACTION, LOGGED_AT, MESSAGE, SUBSCRIBER_ID, " +
                "TENANT_ID) VALUES (:TYPE;, :IS_SUCCESS;, :USERNAME;, :TARGET;, :TARGET_ACTION;, :LOGGED_AT;, " +
                ":MESSAGE;, :KEY;, :TENANT_ID;)";
        public static final String GET_POLICY_STATUS_SQL = "SELECT POLICY_ID, TYPE, IS_SUCCESS, USERNAME, TARGET, " +
                "TARGET_ACTION, LOGGED_AT, MESSAGE, POLICY_VERSION FROM IDN_XACML_POLICY_STATUS WHERE POLICY_ID=:KEY; " +
                "AND TENANT_ID=:TENANT_ID;";
        public static final String GET_SUBSCRIBER_STATUS_SQL =
                "SELECT SUBSCRIBER_ID, TYPE, IS_SUCCESS, USERNAME, TARGET, TARGET_ACTION, LOGGED_AT, MESSAGE FROM " +
                        "IDN_XACML_SUBSCRIBER_STATUS WHERE SUBSCRIBER_ID=:KEY; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_POLICY_STATUS_COUNT_SQL =
                "SELECT COUNT(POLICY_ID) AS COUNT FROM IDN_XACML_POLICY_STATUS WHERE POLICY_ID=:KEY; AND " +
                        "TENANT_ID=:TENANT_ID;";
        public static final String GET_SUBSCRIBER_STATUS_COUNT_SQL = "SELECT COUNT(SUBSCRIBER_ID) AS COUNT FROM " +
                "IDN_XACML_SUBSCRIBER_STATUS WHERE SUBSCRIBER_ID=:KEY; AND TENANT_ID=:TENANT_ID;";
        public static final String DELETE_POLICY_STATUS_SQL = "DELETE FROM IDN_XACML_POLICY_STATUS WHERE POLICY_ID=:KEY; " +
                "AND TENANT_ID=:TENANT_ID;";
        public static final String DELETE_SUBSCRIBER_STATUS_SQL = "DELETE FROM IDN_XACML_SUBSCRIBER_STATUS WHERE " +
                "SUBSCRIBER_ID=:KEY; AND TENANT_ID=:TENANT_ID;";
        public static final String DELETE_OLD_POLICY_STATUSES_MYSQL = "DELETE FROM IDN_XACML_POLICY_STATUS WHERE " +
                "ID IN (SELECT ID FROM IDN_XACML_POLICY_STATUS WHERE POLICY_ID= :KEY; AND " +
                "TENANT_ID= :TENANT_ID; ORDER BY LOGGED_AT ASC, ID ASC LIMIT :LIMIT;)";
        public static final String DELETE_OLD_SUBSCRIBER_STATUSES_MYSQL =
                "DELETE FROM IDN_XACML_SUBSCRIBER_STATUS WHERE ID " +
                        "IN (SELECT ID FROM IDN_XACML_SUBSCRIBER_STATUS WHERE SUBSCRIBER_ID= :KEY; AND " +
                        "TENANT_ID= :TENANT_ID; ORDER BY LOGGED_AT ASC, ID ASC LIMIT :LIMIT;)";
        public static final String DELETE_OLD_POLICY_STATUSES_MSSQL =
                "DELETE FROM IDN_XACML_POLICY_STATUS WHERE ID IN (SELECT ID FROM IDN_XACML_POLICY_STATUS WHERE " +
                        "POLICY_ID = :KEY; AND TENANT_ID = :TENANT_ID; ORDER BY LOGGED_AT ASC, ID ASC OFFSET 0 ROWS " +
                        "FETCH NEXT :LIMIT; ROWS ONLY)";
        public static final String DELETE_OLD_SUBSCRIBER_STATUSES_MSSQL =
                "DELETE FROM IDN_XACML_SUBSCRIBER_STATUS WHERE ID IN (SELECT ID FROM IDN_XACML_SUBSCRIBER_STATUS WHERE " +
                        "SUBSCRIBER_ID= :KEY; AND TENANT_ID=:TENANT_ID; ORDER BY LOGGED_AT ASC, ID ASC OFFSET 0 " +
                        "ROWS FETCH NEXT :LIMIT; ROWS ONLY)";
        public static final String DELETE_OLD_POLICY_STATUSES_ORACLE =
                "DELETE FROM IDN_XACML_POLICY_STATUS WHERE ID IN" +
                        " (SELECT ID FROM (SELECT ID FROM IDN_XACML_POLICY_STATUS WHERE POLICY_ID= :KEY; AND" +
                        " TENANT_ID=:TENANT_ID; ORDER BY LOGGED_AT ASC, ID ASC) WHERE ROWNUM <= :LIMIT;)";
        public static final String DELETE_OLD_SUBSCRIBER_STATUSES_ORACLE =
                "DELETE FROM IDN_XACML_SUBSCRIBER_STATUS WHERE ID " +
                        "IN (SELECT ID FROM (SELECT ID FROM IDN_XACML_SUBSCRIBER_STATUS WHERE SUBSCRIBER_ID= :KEY; " +
                        "AND TENANT_ID=:TENANT_ID; ORDER BY LOGGED_AT ASC, ID ASC) WHERE ROWNUM <= :LIMIT;)";

        /**
         * DB queries related to policy version management.
         */
        public static final String GET_LATEST_POLICY_VERSION_SQL =
                "SELECT MAX(VERSION) AS VERSION FROM IDN_XACML_POLICY " +
                        "WHERE IS_IN_PAP=:IS_IN_PAP; AND POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID;";
        public static final String GET_POLICY_VERSIONS_SQL = "SELECT VERSION FROM IDN_XACML_POLICY WHERE " +
                "IS_IN_PAP=:IS_IN_PAP; AND POLICY_ID=:POLICY_ID; AND TENANT_ID=:TENANT_ID;";
    }
}
