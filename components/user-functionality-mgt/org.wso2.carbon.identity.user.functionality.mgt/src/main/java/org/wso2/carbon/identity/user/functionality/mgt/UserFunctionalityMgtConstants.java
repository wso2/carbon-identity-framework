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

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the constants used in the module, user-functionality-mgt.
 */
public class UserFunctionalityMgtConstants {

    public static final String ENABLE_PER_USER_FUNCTIONALITY_LOCKING = "EnablePerUserFunctionalityLocking";

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


        private ErrorMessages(int code, String description) {
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
     * Enum contains the Functionality and Functionality Identifier.
     */
    public enum FunctionalityTypes {

        FUNCTIONALITY_SECURITY_QUESTION_PW_RECOVERY("FUNCTIONALITY_SECURITY_QUESTION_PW_RECOVERY",
                "SecurityQuestionBasedPasswordRecovery"),
        FUNCTIONALITY_NOTIFICATION_BASED_PW_RECOVERY_SMS("FUNCTIONALITY_NOTIFICATION_BASED_PW_RECOVERY_SMS",
                "SMSBasedPasswordRecovery");

        private final String functionalityName;
        private final String functionalityIdentifier;
        private static Map<String, FunctionalityTypes> functionalityToTypeMapping;

        private FunctionalityTypes(String functionalityName, String functionalityIdentifier) {
            this.functionalityName = functionalityName;
            this.functionalityIdentifier = functionalityIdentifier;
        }

        public String getFunctionalityIdentifier() {
            return this.functionalityIdentifier;
        }

        public String getDescription() {
            return this.functionalityName;
        }

        public String toString() {
            return this.functionalityIdentifier + " - " + this.functionalityName;
        }

        public static FunctionalityTypes getFunctionality(String functionalityName){
            if(functionalityToTypeMapping == null){
                initMapping();
            }
            return functionalityToTypeMapping.get(functionalityName);
        }

        private static void initMapping(){
            functionalityToTypeMapping = new HashMap<>();
            for(FunctionalityTypes types : values()){
                functionalityToTypeMapping.put(types.functionalityName, types);
            }
        }
    }

    /**
     * Enum contains the codes and status messages for per-user functionality locking.
     */
    public enum FunctionalityLockReasons {

        USER_MANUALLY_LOCKED("FL_001", "User manually locked."),
        ADMIN_MANUALLY_LOCKED("FL_002", "Admin manually locked."),
        PWD_RECOVERY_MAX_ATTEMPTS_EXCEEDED("FL_003", "Maximum attempts exceeded for password recovery.");


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
