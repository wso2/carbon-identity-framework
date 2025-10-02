/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.internal.constant;

/**
 * SQL constants for Action management service.
 */
public class ActionMgtSQLConstants {

    private ActionMgtSQLConstants() {

    }

    /**
     * Column Names.
     */
    public static class Column {

        public static final String SCHEMA_VERSION = "VERSION";
        public static final String ACTION_UUID = "UUID";
        public static final String ACTION_TYPE = "TYPE";
        public static final String ACTION_NAME = "NAME";
        public static final String ACTION_DESCRIPTION = "DESCRIPTION";
        public static final String ACTION_STATUS = "STATUS";
        public static final String CREATED_AT = "CREATED_AT";
        public static final String UPDATED_AT = "UPDATED_AT";
        public static final String ACTION_COUNT = "COUNT";
        public static final String ACTION_VERSION = "ACTION_VERSION";
        public static final String ACTION_PROPERTIES_UUID = "ACTION_UUID";
        public static final String ACTION_PROPERTIES_PROPERTY_NAME = "PROPERTY_NAME";
        public static final String ACTION_PROPERTIES_PROPERTY_TYPE = "PROPERTY_TYPE";
        public static final String ACTION_PROPERTIES_PROPERTY_VALUE = "PROPERTY_VALUE";
        public static final String ACTION_PROPERTIES_OBJECT_VALUE = "OBJECT_VALUE";
        public static final String TENANT_ID = "TENANT_ID";

        private Column() {

        }
    }

    /**
     * Queries.
     */
    public static class Query {

        public static final String ADD_ACTION_TO_ACTION_TYPE = "INSERT INTO IDN_ACTION (UUID, TYPE, NAME, " +
                "DESCRIPTION, STATUS, ACTION_VERSION, TENANT_ID, VERSION, CREATED_AT, UPDATED_AT) VALUES (:UUID;, " +
                ":TYPE;, :NAME;, :DESCRIPTION;, :STATUS;, :ACTION_VERSION;, :TENANT_ID;, :VERSION;, :CREATED_AT;, " +
                ":UPDATED_AT;)";
        public static final String ADD_ACTION_PROPERTIES = "INSERT INTO IDN_ACTION_PROPERTIES (ACTION_UUID, " +
                "PROPERTY_NAME, PROPERTY_TYPE, PROPERTY_VALUE, OBJECT_VALUE, TENANT_ID) VALUES (:ACTION_UUID;, " +
                ":PROPERTY_NAME;, :PROPERTY_TYPE;, :PROPERTY_VALUE;, :OBJECT_VALUE;, :TENANT_ID;)";
        public static final String GET_ACTION_BASIC_INFO_BY_ID = "SELECT TYPE, NAME, DESCRIPTION, STATUS, " +
                "ACTION_VERSION, CREATED_AT, UPDATED_AT FROM IDN_ACTION " +
                "WHERE TYPE = :TYPE; AND UUID = :UUID; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_ACTION_PROPERTIES_INFO_BY_ID = "SELECT PROPERTY_NAME, PROPERTY_TYPE, " +
                "PROPERTY_VALUE, OBJECT_VALUE FROM IDN_ACTION_PROPERTIES WHERE ACTION_UUID = :ACTION_UUID; AND " +
                "TENANT_ID = :TENANT_ID;";
        public static final String GET_ACTIONS_BASIC_INFO_BY_ACTION_TYPE = "SELECT UUID, TYPE, NAME, DESCRIPTION," +
                " STATUS, ACTION_VERSION, CREATED_AT, UPDATED_AT FROM IDN_ACTION " +
                "WHERE TYPE = :TYPE; AND TENANT_ID = :TENANT_ID;";
        public static final String UPDATE_ACTION_BASIC_INFO = "UPDATE IDN_ACTION SET NAME = :NAME;, DESCRIPTION = " +
                ":DESCRIPTION;, ACTION_VERSION = :ACTION_VERSION;, UPDATED_AT = :UPDATED_AT; " +
                "WHERE UUID = :UUID; AND TYPE = :TYPE; AND TENANT_ID = :TENANT_ID;";
        public static final String DELETE_ACTION_PROPERTY = "DELETE FROM IDN_ACTION_PROPERTIES WHERE " +
                "PROPERTY_NAME = :PROPERTY_NAME; AND ACTION_UUID = :ACTION_UUID; AND TENANT_ID = :TENANT_ID;";
        public static final String DELETE_ACTION = "DELETE FROM IDN_ACTION WHERE UUID = :UUID; AND TYPE = :TYPE;" +
                " AND TENANT_ID = :TENANT_ID;";
        public static final String CHANGE_ACTION_STATUS = "UPDATE IDN_ACTION SET STATUS = :STATUS;, " +
                "UPDATED_AT = :UPDATED_AT; WHERE UUID = :UUID; AND TYPE = :TYPE; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_ACTIONS_COUNT_PER_ACTION_TYPE = "SELECT TYPE, COUNT(UUID) AS COUNT" +
                " FROM IDN_ACTION WHERE TENANT_ID = :TENANT_ID; GROUP BY TYPE";
        public static final String UPDATE_ACTION_PROPERTY = "UPDATE IDN_ACTION_PROPERTIES SET " +
                "PROPERTY_VALUE = :PROPERTY_VALUE;, OBJECT_VALUE = :OBJECT_VALUE; WHERE ACTION_UUID = " +
                ":ACTION_UUID; AND TENANT_ID = :TENANT_ID; AND PROPERTY_NAME = :PROPERTY_NAME;";

        private Query() {

        }
    }
}
