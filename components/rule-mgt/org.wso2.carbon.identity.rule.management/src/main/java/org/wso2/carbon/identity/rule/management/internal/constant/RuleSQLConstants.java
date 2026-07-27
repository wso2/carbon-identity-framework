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

package org.wso2.carbon.identity.rule.management.internal.constant;

/**
 * SQL Constants for Rule Management.
 * This class is used to store SQL queries and column names.
 */
public class RuleSQLConstants {

    private RuleSQLConstants() {

    }

    /**
     * This class is used to store column names.
     */
    public static class Column {

        public static final String RULE_INTERNAL_ID = "ID";
        public static final String RULE_EXTERNAL_ID = "UUID";
        public static final String RULE_CONTENT = "CONTENT";
        public static final String IS_ACTIVE = "IS_ACTIVE";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String VERSION = "VERSION";
        public static final String RULE_REFERENCE_ID = "RULE_ID";
        public static final String FIELD_NAME = "FIELD_NAME";
        public static final String FIELD_REFERENCE = "FIELD_REFERENCE";

        private Column() {

        }
    }

    /**
     * This class is used to store SQL queries.
     */
    public static class Query {

        public static final String ADD_RULE = "INSERT INTO IDN_RULE (UUID, CONTENT, IS_ACTIVE, TENANT_ID, VERSION) " +
                "VALUES (:UUID;, :CONTENT;, :IS_ACTIVE;, :TENANT_ID;, :VERSION;)";
        public static final String ADD_RULE_REFERENCES = "INSERT INTO IDN_RULE_REFERENCES (RULE_ID, " +
                "FIELD_NAME, FIELD_REFERENCE, TENANT_ID) VALUES (:RULE_ID;, :FIELD_NAME;, :FIELD_REFERENCE;, " +
                ":TENANT_ID;)";
        public static final String UPDATE_RULE =
                "UPDATE IDN_RULE SET CONTENT = :CONTENT; WHERE UUID = :UUID; AND TENANT_ID = :TENANT_ID;";
        public static final String DELETE_RULE_REFERENCES =
                "DELETE FROM IDN_RULE_REFERENCES WHERE RULE_ID = :RULE_ID; AND TENANT_ID = :TENANT_ID;";
        public static final String DELETE_RULE = "DELETE FROM IDN_RULE WHERE UUID = :UUID; AND TENANT_ID = :TENANT_ID;";
        public static final String CHANGE_RULE_STATUS = "UPDATE IDN_RULE SET IS_ACTIVE = :IS_ACTIVE; WHERE UUID = " +
                ":UUID; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_RULE_BY_ID =
                "SELECT CONTENT, IS_ACTIVE FROM IDN_RULE WHERE UUID = :UUID; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_RULE_INTERNAL_ID_BY_ID =
                "SELECT ID FROM IDN_RULE WHERE UUID = :UUID; AND TENANT_ID = :TENANT_ID;";

        private Query() {

        }
    }
}
