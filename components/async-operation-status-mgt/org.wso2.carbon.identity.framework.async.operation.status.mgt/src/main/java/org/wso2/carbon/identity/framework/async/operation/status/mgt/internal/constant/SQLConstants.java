/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.constant;

/**
 * This class contains database queries related to CRUD operations for status of asynchronous operations.
 */
public class SQLConstants {

    public static final String LIMIT = "LIMIT";

    public static final String CREATE_ASYNC_OPERATION = "INSERT INTO IDN_ASYNC_OPERATION_STATUS( OPERATION_ID, " +
            "CORRELATION_ID, OPERATION_TYPE, SUBJECT_TYPE, SUBJECT_ID, INITIATED_ORG_ID, INITIATED_USER_ID, STATUS, " +
            "CREATED_AT, LAST_MODIFIED, POLICY) VALUES( :OPERATION_ID;, :CORRELATION_ID;, :OPERATION_TYPE;, " +
            ":SUBJECT_TYPE;, :SUBJECT_ID;, :INITIATED_ORG_ID;, :INITIATED_USER_ID;, :STATUS;, :CREATED_AT;, " +
            ":LAST_MODIFIED;, :POLICY;)";

    public static final String UPDATE_ASYNC_OPERATION = "UPDATE IDN_ASYNC_OPERATION_STATUS SET STATUS = :STATUS;, " +
            "LAST_MODIFIED = :LAST_MODIFIED; WHERE OPERATION_ID = :OPERATION_ID;";

    public static final String CREATE_ASYNC_OPERATION_UNIT_BATCH = "INSERT INTO IDN_ASYNC_OPERATION_STATUS_UNIT (" +
            "UNIT_OPERATION_ID, OPERATION_ID, RESIDENT_RESOURCE_ID, TARGET_ORG_ID, STATUS, STATUS_MESSAGE, " +
            "CREATED_AT) VALUES( :UNIT_OPERATION_ID;, :OPERATION_ID;, :RESIDENT_RESOURCE_ID;, :TARGET_ORG_ID;," +
            " :STATUS;, :STATUS_MESSAGE;, :CREATED_AT; )";

    public static final String GET_OPERATIONS = "SELECT OPERATION_ID, CORRELATION_ID, OPERATION_TYPE, " +
            "SUBJECT_TYPE, SUBJECT_ID, INITIATED_ORG_ID, INITIATED_USER_ID, STATUS, POLICY, CREATED_AT, " +
            "LAST_MODIFIED FROM IDN_ASYNC_OPERATION_STATUS WHERE INITIATED_ORG_ID = :INITIATED_ORG_ID;";

    public static final String GET_OPERATIONS_TAIL = " ORDER BY CURSOR_KEY DESC LIMIT :LIMIT;";

    public static final String GET_OPERATIONS_TAIL_ORACLE = " ORDER BY CURSOR_KEY DESC FETCH FIRST :LIMIT; ROWS ONLY";

    public static final String GET_OPERATIONS_TAIL_MSSQL = " ORDER BY CURSOR_KEY DESC OFFSET 0 ROWS " +
            "FETCH NEXT :LIMIT; ROWS ONLY";

    public static final String GET_OPERATION = "SELECT OPERATION_ID, CORRELATION_ID, OPERATION_TYPE, " +
            "SUBJECT_TYPE, SUBJECT_ID, INITIATED_ORG_ID, INITIATED_USER_ID, STATUS, POLICY, CREATED_AT, " +
            "LAST_MODIFIED FROM IDN_ASYNC_OPERATION_STATUS WHERE OPERATION_ID = :OPERATION_ID; AND " +
            "INITIATED_ORG_ID = :INITIATED_ORG_ID;";

    public static final String GET_UNIT_OPERATIONS = "SELECT UNIT_OPERATION_ID, OPERATION_ID, " +
            "RESIDENT_RESOURCE_ID, TARGET_ORG_ID, STATUS, STATUS_MESSAGE, " +
            "CREATED_AT FROM IDN_ASYNC_OPERATION_STATUS_UNIT WHERE OPERATION_ID = ( SELECT OPERATION_ID FROM " +
            "IDN_ASYNC_OPERATION_STATUS WHERE OPERATION_ID = :OPERATION_ID; AND INITIATED_ORG_ID = :INITIATED_ORG_ID;)";

    public static final String GET_UNIT_OPERATIONS_TAIL = " ORDER BY CURSOR_KEY DESC LIMIT :LIMIT; ;";

    public static final String GET_UNIT_OPERATIONS_TAIL_ORACLE = " ORDER BY CURSOR_KEY DESC FETCH FIRST :LIMIT; " +
            "ROWS ONLY";

    public static final String GET_UNIT_OPERATIONS_TAIL_MSSQL = " ORDER BY CURSOR_KEY DESC OFFSET 0 ROWS " +
            "FETCH NEXT :LIMIT; ROWS ONLY";

    public static final String GET_UNIT_OPERATION = "SELECT U.UNIT_OPERATION_ID, U.OPERATION_ID, " +
            "U.RESIDENT_RESOURCE_ID, U.TARGET_ORG_ID, U.STATUS, U.STATUS_MESSAGE, U.CREATED_AT " +
            "FROM IDN_ASYNC_OPERATION_STATUS_UNIT U JOIN IDN_ASYNC_OPERATION_STATUS S ON U.OPERATION_ID = " +
            "S.OPERATION_ID WHERE UNIT_OPERATION_ID = :UNIT_OPERATION_ID; AND INITIATED_ORG_ID = :INITIATED_ORG_ID;";

    public static final String DELETE_RECENT_OPERATION_RECORD = "DELETE FROM IDN_ASYNC_OPERATION_STATUS WHERE " +
            "OPERATION_TYPE = :OPERATION_TYPE; AND SUBJECT_ID = :SUBJECT_ID; AND CORRELATION_ID != :CORRELATION_ID; ";

    public static final String GET_UNIT_OPERATION_STATUS_COUNT = "SELECT STATUS, COUNT(*) as COUNT FROM " +
            "IDN_ASYNC_OPERATION_STATUS_UNIT WHERE OPERATION_ID = ( SELECT OPERATION_ID FROM " +
            "IDN_ASYNC_OPERATION_STATUS WHERE OPERATION_ID = :OPERATION_ID; AND " +
            "INITIATED_ORG_ID = :INITIATED_ORG_ID;) GROUP BY STATUS";

    /**
     * SQL Placeholders.
     */
    public static class SQLPlaceholders {

        public static final String OPERATION_ID = "OPERATION_ID";
        public static final String CURSOR_KEY = "CURSOR_KEY";
        public static final String CORRELATION_ID = "CORRELATION_ID";
        public static final String OPERATION_TYPE = "OPERATION_TYPE";
        public static final String SUBJECT_TYPE = "SUBJECT_TYPE";
        public static final String SUBJECT_ID = "SUBJECT_ID";
        public static final String INITIATED_ORG_ID = "INITIATED_ORG_ID";
        public static final String INITIATED_USER_ID = "INITIATED_USER_ID";
        public static final String STATUS = "STATUS";
        public static final String CREATED_AT = "CREATED_AT";
        public static final String LAST_MODIFIED = "LAST_MODIFIED";
        public static final String POLICY = "POLICY";

        public static final String UNIT_OPERATION_ID = "UNIT_OPERATION_ID";
        public static final String RESIDENT_RESOURCE_ID = "RESIDENT_RESOURCE_ID";
        public static final String TARGET_ORG_ID = "TARGET_ORG_ID";
        public static final String STATUS_MESSAGE = "STATUS_MESSAGE";
        public static final String COUNT = "COUNT";
    }

    /**
     * Filter Placeholders.
     */
    public static class FilterPlaceholders {

        public static final String OPERATION_ID_FILTER = "operationId";
        public static final String CORRELATION_ID_FILTER = "correlationId";
        public static final String OPERATION_TYPE_FILTER = "operationType";
        public static final String SUBJECT_TYPE_FILTER = "subjectType";
        public static final String SUBJECT_ID_FILTER = "subjectId";
        public static final String INITIATED_ORG_ID_FILTER = "initiatedOrgId";
        public static final String INITIATED_USER_ID_FILTER = "initiatedUserId";
        public static final String STATUS_FILTER = "status";
        public static final String CREATED_TIME_FILTER = "createdTime";
        public static final String MODIFIED_TIME_FILTER = "modifiedTime";
        public static final String POLICY_FILTER = "policy";

        public static final String UNIT_OPERATION_ID_FILTER = "unitOperationId";
        public static final String INITIATED_RESOURCE_ID_FILTER = "initiatedResourceId";
        public static final String TARGET_ORG_ID_FILTER = "targetOrgId";
        public static final String STATUS_MESSAGE_FILTER = "statusMessage";
    }
}
