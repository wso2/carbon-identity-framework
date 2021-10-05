/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.user.functionality.mgt;

/**
 * This class holds the constants used in the module, user-functionality-mgt.
 */
public class UserFunctionalityMgtConstants {

    public static final String ENABLE_PER_USER_FUNCTIONALITY_LOCKING = "EnablePerUserFunctionalityLocking";

    public static final String ORACLE = "oracle";

    /**
     * SQL Query definitions.
     */
    public static class SqlQueries {

        public static final String INSERT_FUNCTIONALITY_MAPPING =
                "INSERT INTO IDN_USER_FUNCTIONALITY_MAPPING (ID, USER_ID, TENANT_ID, FUNCTIONALITY_ID, " +
                        "IS_FUNCTIONALITY_LOCKED, FUNCTIONALITY_UNLOCK_TIME, FUNCTIONALITY_LOCK_REASON, " +
                        "FUNCTIONALITY_LOCK_REASON_CODE) VALUES (?,?,?,?,?,?,?,?)";
        public static final String GET_FUNCTIONALITY_LOCK_STATUS = "SELECT IS_FUNCTIONALITY_LOCKED, " +
                "FUNCTIONALITY_UNLOCK_TIME, FUNCTIONALITY_LOCK_REASON_CODE, FUNCTIONALITY_LOCK_REASON FROM " +
                "IDN_USER_FUNCTIONALITY_MAPPING WHERE USER_ID=? AND TENANT_ID=? AND FUNCTIONALITY_ID=?";
        public static final String UPDATE_FUNCTIONALITY_MAPPING =
                "UPDATE IDN_USER_FUNCTIONALITY_MAPPING SET USER_ID=?, TENANT_ID=?, FUNCTIONALITY_ID=?, " +
                        "IS_FUNCTIONALITY_LOCKED=?, FUNCTIONALITY_UNLOCK_TIME=?, FUNCTIONALITY_LOCK_REASON=?, " +
                        "FUNCTIONALITY_LOCK_REASON_CODE=?  WHERE USER_ID=? AND TENANT_ID=? AND FUNCTIONALITY_ID=?";
        public static final String DELETE_FUNCTIONALITY_MAPPING =
                "DELETE FROM IDN_USER_FUNCTIONALITY_MAPPING WHERE USER_ID=? AND TENANT_ID=? AND FUNCTIONALITY_ID=?";
        public static final String DELETE_ALL_FUNCTIONALITY_MAPPINGS_FOR_TENANT =
                "DELETE FROM IDN_USER_FUNCTIONALITY_MAPPING WHERE TENANT_ID=?";

        public static final String INSERT_PROPERTY = "INSERT INTO IDN_USER_FUNCTIONALITY_PROPERTY (ID, USER_ID, " +
                "TENANT_ID, FUNCTIONALITY_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES (?,?,?,?,?,?)";
        public static final String GET_PROPERTY_VALUE = "SELECT PROPERTY_VALUE FROM IDN_USER_FUNCTIONALITY_PROPERTY " +
                "WHERE USER_ID=? AND TENANT_ID=? AND FUNCTIONALITY_ID=? AND PROPERTY_NAME=?";
        public static final String GET_ALL_PROPERTIES = "SELECT PROPERTY_NAME, PROPERTY_VALUE FROM " +
                "IDN_USER_FUNCTIONALITY_PROPERTY WHERE USER_ID=? AND TENANT_ID=? AND FUNCTIONALITY_ID=?";
        public static final String UPDATE_PROPERTY_VALUE =
                "UPDATE IDN_USER_FUNCTIONALITY_PROPERTY SET PROPERTY_VALUE=? WHERE USER_ID=? AND TENANT_ID=? AND " +
                        "FUNCTIONALITY_ID=? AND PROPERTY_NAME=?";
        public static final String DELETE_PROPERTY =
                "DELETE FROM IDN_USER_FUNCTIONALITY_PROPERTY WHERE USER_ID=? AND TENANT_ID=? AND FUNCTIONALITY_ID=? " +
                        "AND PROPERTY_NAME=?";
        public static final String DELETE_ALL_PROPERTIES_FOR_MAPPING =
                "DELETE FROM IDN_USER_FUNCTIONALITY_PROPERTY WHERE USER_ID=? AND TENANT_ID=? AND FUNCTIONALITY_ID=?";
        public static final String DELETE_ALL_PROPERTIES_FOR_TENANT =
                "DELETE FROM IDN_USER_FUNCTIONALITY_PROPERTY WHERE USER_ID=? AND TENANT_ID=? AND FUNCTIONALITY_ID=?";

        public static final String INSERT_PROPERTY_ORACLE = "INSERT INTO %s (ID, " +
                "USER_ID, TENANT_ID, FUNCTIONALITY_ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES (?,?,?,?,?,?)";
        public static final String GET_PROPERTY_VALUE_ORACLE = "SELECT PROPERTY_VALUE FROM %s WHERE USER_ID=? AND " +
                "TENANT_ID=? AND FUNCTIONALITY_ID=? AND PROPERTY_NAME=?";
        public static final String GET_ALL_PROPERTIES_ORACLE = "SELECT PROPERTY_NAME, PROPERTY_VALUE FROM " +
                "%s WHERE USER_ID=? AND TENANT_ID=? AND FUNCTIONALITY_ID=?";
        public static final String UPDATE_PROPERTY_VALUE_ORACLE = "UPDATE %s SET PROPERTY_VALUE=? WHERE USER_ID=? AND" +
                " TENANT_ID=? AND FUNCTIONALITY_ID=? AND PROPERTY_NAME=?";
        public static final String DELETE_PROPERTY_ORACLE = "DELETE FROM %s WHERE USER_ID=? AND TENANT_ID=? AND " +
                "FUNCTIONALITY_ID=? AND PROPERTY_NAME=?";
        public static final String DELETE_ALL_PROPERTIES_FOR_MAPPING_ORACLE =
                "DELETE FROM %s WHERE USER_ID=? AND TENANT_ID=? AND FUNCTIONALITY_ID=?";
        public static final String DELETE_ALL_PROPERTIES_FOR_TENANT_ORACLE =
                "DELETE FROM %s WHERE USER_ID=? AND TENANT_ID=? AND FUNCTIONALITY_ID=?";
        public static final String GET_ORACLE_TABLE_NAME = "SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME IN " +
                "('IDN_USR_FUNCTIONALITY_PROPERTY','IDN_USER_FUNCTIONALITY_PROPERTY')";
    }

    /**
     * Enum contains Error Codes and Error Messages.
     */
    public enum ErrorMessages {

        USER_NOT_FOUND(60001, "Invalid user ID"),
        ERROR_OCCURRED_WHILE_RETRIEVING_USER(65001, "Error occurred when retrieving user from user ID");

        private final int code;
        private final String description;
        private static final String USER_FUNCTIONALITY_MGT_ERROR_PREFIX = "UFM-";

        ErrorMessages(int code, String description) {

            this.code = code;
            this.description = description;
        }

        public String getCode() {

            return USER_FUNCTIONALITY_MGT_ERROR_PREFIX + this.code;
        }

        public String getDescription() {

            return this.description;
        }

        public String toString() {

            return this.code + " - " + this.description;
        }
    }

    /**
     * Enum contains the codes and status messages for per-user functionality locking.
     */
    public enum FunctionalityLockReasons {

        USER_MANUALLY_LOCKED("FL_001", "User manually locked."),
        ADMIN_MANUALLY_LOCKED("FL_002", "Admin manually locked.");

        private final String functionalityLockCode;
        private final String functionalityLockReason;

        /**
         * Per-user lock code constructor.
         *
         * @param functionalityLockCode   Lock reason code.
         * @param functionalityLockReason Reason for the functionality lock.
         */
        FunctionalityLockReasons(String functionalityLockCode, String functionalityLockReason) {

            this.functionalityLockCode = functionalityLockCode;
            this.functionalityLockReason = functionalityLockReason;
        }

        public String getFunctionalityLockReason() {

            return functionalityLockReason;
        }

        public String getFunctionalityLockCode() {

            return functionalityLockCode;
        }
    }
}
