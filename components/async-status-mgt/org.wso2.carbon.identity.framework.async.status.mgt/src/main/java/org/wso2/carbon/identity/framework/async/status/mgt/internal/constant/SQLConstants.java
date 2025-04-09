package org.wso2.carbon.identity.framework.async.status.mgt.internal.constant;

/**
 * This class contains database queries related to CRUD operations for status of asynchronous operations.
 */
public class SQLConstants {

    public static final String LIMIT = "LIMIT";

    public static final String CREATE_ASYNC_OPERATION = "INSERT INTO IDN_ASYNC_OPERATION_STATUS(" +
            "IDN_CORRELATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_TYPE, IDN_OPERATION_SUBJECT_ID," +
            "IDN_OPERATION_INITIATED_ORG_ID, IDN_OPERATION_INITIATED_USER_ID, IDN_OPERATION_STATUS," +
            "IDN_CREATED_AT, IDN_LAST_MODIFIED, IDN_OPERATION_POLICY) VALUES(" +
            ":IDN_CORRELATION_ID;, :IDN_OPERATION_TYPE;, :IDN_OPERATION_SUBJECT_TYPE;, :IDN_OPERATION_SUBJECT_ID;, " +
            ":IDN_OPERATION_INITIATED_ORG_ID;, :IDN_OPERATION_INITIATED_USER_ID;, :IDN_OPERATION_STATUS;, " +
            ":IDN_CREATED_AT;, :IDN_LAST_MODIFIED;, :IDN_OPERATION_POLICY; )";

    public static final String UPDATE_ASYNC_OPERATION = "UPDATE IDN_ASYNC_OPERATION_STATUS " +
            "SET IDN_OPERATION_STATUS = :IDN_OPERATION_STATUS;, " +
            "IDN_LAST_MODIFIED = :IDN_LAST_MODIFIED; " +
            "WHERE IDN_OPERATION_ID = :IDN_OPERATION_ID;";

    public static final String CREATE_ASYNC_OPERATION_UNIT_BATCH = "INSERT INTO IDN_ASYNC_OPERATION_STATUS_UNIT (" +
            "IDN_OPERATION_ID, IDN_RESIDENT_RESOURCE_ID, IDN_TARGET_ORG_ID," +
            "IDN_UNIT_OPERATION_STATUS, IDN_OPERATION_STATUS_MESSAGE, IDN_CREATED_AT ) VALUES(" +
            ":IDN_OPERATION_ID;, :IDN_RESIDENT_RESOURCE_ID;, :IDN_TARGET_ORG_ID;, :IDN_UNIT_OPERATION_STATUS;, " +
            ":IDN_OPERATION_STATUS_MESSAGE;, :IDN_CREATED_AT; )";

    public static final String GET_OPERATIONS = "SELECT IDN_OPERATION_ID, IDN_CORRELATION_ID, IDN_OPERATION_TYPE, " +
            "IDN_OPERATION_SUBJECT_TYPE, IDN_OPERATION_SUBJECT_ID, IDN_OPERATION_INITIATED_ORG_ID, " +
            "IDN_OPERATION_INITIATED_USER_ID, IDN_OPERATION_STATUS, IDN_OPERATION_POLICY, IDN_CREATED_AT, " +
            "IDN_LAST_MODIFIED FROM IDN_ASYNC_OPERATION_STATUS " +
            "WHERE IDN_OPERATION_SUBJECT_TYPE = :OPERATION_SUBJECT_TYPE; " +
            "AND IDN_OPERATION_SUBJECT_ID = :OPERATION_SUBJECT_ID; AND IDN_OPERATION_TYPE = :OPERATION_TYPE; ";

    public static final String GET_OPERATIONS_TAIL = " ORDER BY IDN_CREATED_AT DESC LIMIT :LIMIT; ;";

    public static final String GET_UNIT_OPERATIONS = "SELECT IDN_UNIT_OPERATION_ID, IDN_OPERATION_ID, " +
            "IDN_RESIDENT_RESOURCE_ID, IDN_TARGET_ORG_ID, IDN_UNIT_OPERATION_STATUS, IDN_OPERATION_STATUS_MESSAGE, " +
            "IDN_CREATED_AT FROM IDN_ASYNC_OPERATION_STATUS_UNIT WHERE IDN_OPERATION_ID = :OPERATION_ID; ";

    public static final String GET_UNIT_OPERATIONS_TAIL = " ORDER BY IDN_CREATED_AT DESC LIMIT :LIMIT; ;";

    public static final String DELETE_RECENT_OPERATION_RECORD = "DELETE FROM IDN_ASYNC_OPERATION_STATUS " +
            "WHERE IDN_OPERATION_TYPE = :IDN_OPERATION_TYPE; AND IDN_OPERATION_SUBJECT_ID = :IDN_OPERATION_SUBJECT_ID;"
            + " AND IDN_CORRELATION_ID != :IDN_CORRELATION_ID; ";

    /**
     * SQL Placeholders.
     */
    public static class SQLPlaceholders {

        //Operation SQLPlaceholders
        public static final String OPERATION_ID = "IDN_OPERATION_ID";
        public static final String CORRELATION_ID = "IDN_CORRELATION_ID";
        public static final String OPERATION_TYPE = "IDN_OPERATION_TYPE";
        public static final String OPERATION_SUBJECT_TYPE = "IDN_OPERATION_SUBJECT_TYPE";
        public static final String OPERATION_SUBJECT_ID = "IDN_OPERATION_SUBJECT_ID";
        public static final String INITIATED_ORG_ID = "IDN_OPERATION_INITIATED_ORG_ID";
        public static final String INITIATED_USER_ID = "IDN_OPERATION_INITIATED_USER_ID";
        public static final String OPERATION_STATUS = "IDN_OPERATION_STATUS";
        public static final String CREATED_AT = "IDN_CREATED_AT";
        public static final String LAST_MODIFIED = "IDN_LAST_MODIFIED";
        public static final String OPERATION_POLICY = "IDN_OPERATION_POLICY";

        //Unit Operation SQLPlaceholders
        public static final String UNIT_OPERATION_ID = "IDN_UNIT_OPERATION_ID";
        public static final String UNIT_OPERATION_RESIDENT_RESOURCE_ID = "IDN_RESIDENT_RESOURCE_ID";
        public static final String UNIT_OPERATION_TARGET_ORG_ID = "IDN_TARGET_ORG_ID";
        public static final String UNIT_OPERATION_STATUS = "IDN_UNIT_OPERATION_STATUS";
        public static final String UNIT_OPERATION_STATUS_MESSAGE = "IDN_OPERATION_STATUS_MESSAGE";
        public static final String UNIT_OPERATION_CREATED_AT = "IDN_CREATED_AT";
    }

    /**
     * Model Properties For Operations.
     */
    public static class OperationModelProperties {

        public static final String MODEL_OPERATION_ID = "OPERATION_ID";
        public static final String MODEL_CORRELATION_ID = "CORRELATION_ID";
        public static final String MODEL_OPERATION_TYPE = "OPERATION_TYPE";
        public static final String MODEL_SUBJECT_TYPE = "OPERATION_SUBJECT_TYPE";
        public static final String MODEL_SUBJECT_ID = "OPERATION_SUBJECT_ID";
        public static final String MODEL_INITIATED_ORG_ID = "RESIDENT_ORG_ID";
        public static final String MODEL_INITIATED_USER_ID = "INITIATOR_ID";
        public static final String MODEL_OPERATION_STATUS = "OPERATION_STATUS";
        public static final String MODEL_CREATED_TIME = "CREATED_TIME";
        public static final String MODEL_LAST_MODIFIED = "MODIFIED_TIME";
        public static final String MODEL_OPERATION_POLICY = "OPERATION_POLICY";
    }

    /**
     * Model Properties For Unit Operations.
     */
    public static class UnitOperationModelProperties {
        public static final String MODEL_UNIT_OPERATION_ID = "UNIT_OPERATION_ID";
        public static final String MODEL_RESIDENT_RESOURCE_ID = "OPERATION_INITIATED_RESOURCE_ID";
        public static final String MODEL_TARGET_ORG_ID = "TARGET_ORG_ID";
        public static final String MODEL_UNIT_OPERATION_STATUS = "UNIT_OPERATION_STATUS";
        public static final String MODEL_STATUS_MESSAGE = "STATUS_MESSAGE";
        public static final String MODEL_CREATED_AT = "CREATED_TIME";

    }

}
