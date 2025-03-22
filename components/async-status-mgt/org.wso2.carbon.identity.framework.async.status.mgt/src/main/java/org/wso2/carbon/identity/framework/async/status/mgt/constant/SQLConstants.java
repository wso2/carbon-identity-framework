package org.wso2.carbon.identity.framework.async.status.mgt.constant;

/**
 * This class contains database queries related to CRUD operations for status of asynchronous operations.
 */
public class SQLConstants {

    public static final String CREATE_ASYNC_OPERATION_IDN = "INSERT INTO IDN_ASYNC_OPERATION_STATUS(" +
            "IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_TYPE, IDN_OPERATION_SUBJECT_ID," +
            "IDN_RESIDENT_ORG_ID, IDN_OPERATION_INITIATOR_ID, IDN_OPERATION_STATUS," +
            "IDN_CREATED_TIME, IDN_LAST_MODIFIED, IDN_OPERATION_POLICY) VALUES(" +
            ":" + OperationStatusTableColumns.IDN_OPERATION_TYPE + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_SUBJECT_TYPE + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID + ";, " +
            ":" + OperationStatusTableColumns.IDN_RESIDENT_ORG_ID + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_STATUS + ";, " +
            ":" + OperationStatusTableColumns.IDN_CREATED_TIME + ";, " +
            ":" + OperationStatusTableColumns.IDN_LAST_MODIFIED + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_POLICY + ";)";

    public static final String CREATE_ASYNC_OPERATION_WITH_UPDATE = "UPDATE IDN_ASYNC_OPERATION_STATUS(" +
            "IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_TYPE, IDN_OPERATION_SUBJECT_ID," +
            "IDN_RESIDENT_ORG_ID, IDN_OPERATION_INITIATOR_ID, IDN_OPERATION_STATUS," +
            "IDN_CREATED_TIME, IDN_LAST_MODIFIED, IDN_OPERATION_POLICY) VALUES(" +
            ":" + OperationStatusTableColumns.IDN_OPERATION_TYPE + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_SUBJECT_TYPE + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID + ";, " +
            ":" + OperationStatusTableColumns.IDN_RESIDENT_ORG_ID + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_STATUS + ";, " +
            ":" + OperationStatusTableColumns.IDN_CREATED_TIME + ";, " +
            ":" + OperationStatusTableColumns.IDN_LAST_MODIFIED + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_POLICY + ";)";

    public static final String UPDATE_ASYNC_OPERATION_IDN = "UPDATE IDN_ASYNC_OPERATION_STATUS SET " +
            "IDN_OPERATION_STATUS = :" + OperationStatusTableColumns.IDN_OPERATION_STATUS + ";, " +
            "IDN_LAST_MODIFIED = :" + OperationStatusTableColumns.IDN_LAST_MODIFIED + "; " +
            "WHERE IDN_OPERATION_SUBJECT_ID = :" + OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID + ";";

    public static final String UPDATE_ASYNC_OPERATION_STATUS_IDN =
            "UPDATE IDN_ASYNC_OPERATION_STATUS " +
                    "SET IDN_OPERATION_STATUS = :" + OperationStatusTableColumns.IDN_OPERATION_STATUS + ", " +
                    "IDN_LAST_MODIFIED = :" + OperationStatusTableColumns.IDN_LAST_MODIFIED + " " +
                    "WHERE IDN_OPERATION_ID = :" + OperationStatusTableColumns.IDN_OPERATION_ID;

    public static final String CREATE_ASYNC_OPERATION_UNIT_IDN = "INSERT INTO IDN_ASYNC_OPERATION_STATUS_UNIT (" +
            "IDN_OPERATION_ID, IDN_RESIDENT_RESOURCE_ID, IDN_TARGET_ORG_ID," +
            "IDN_UNIT_OPERATION_STATUS, IDN_OPERATION_STATUS_MESSAGE, IDN_CREATED_AT ) VALUES(" +
            ":" + UnitOperationStatusTableColumns.IDN_OPERATION_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.IDN_RESIDENT_RESOURCE_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.IDN_TARGET_ORG_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_STATUS + ";, " +
            ":" + UnitOperationStatusTableColumns.IDN_OPERATION_STATUS_MESSAGE + ";, " +
            ":" + UnitOperationStatusTableColumns.IDN_CREATED_AT + ";)";

    public static final String FETCH_LATEST_ASYNC_OPERATION_IDN =
            "SELECT IDN_OPERATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_TYPE, IDN_OPERATION_SUBJECT_ID, " +
                    "IDN_RESIDENT_ORG_ID, IDN_OPERATION_INITIATOR_ID, IDN_OPERATION_STATUS, IDN_OPERATION_POLICY " +
                    "FROM IDN_ASYNC_OPERATION_STATUS " +
                    "WHERE IDN_OPERATION_SUBJECT_ID = :" + OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID + " " +
                    "AND IDN_RESIDENT_ORG_ID = :" + OperationStatusTableColumns.IDN_RESIDENT_ORG_ID + " " +
                    "AND IDN_OPERATION_POLICY = :" + OperationStatusTableColumns.IDN_OPERATION_POLICY + " " +
                    "AND IDN_OPERATION_INITIATOR_ID = :" + OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID +
                    " " +
                    "ORDER BY IDN_CREATED_TIME DESC " +
                    "LIMIT 1;";

    /**
     * SQL Placeholders.
     */
    public static class OperationStatusTableColumns {

        public static final String IDN_OPERATION_ID = "IDN_OPERATION_ID";
        public static final String IDN_OPERATION_TYPE = "IDN_OPERATION_TYPE";
        public static final String IDN_OPERATION_SUBJECT_TYPE = "IDN_OPERATION_SUBJECT_TYPE";
        public static final String IDN_OPERATION_SUBJECT_ID = "IDN_OPERATION_SUBJECT_ID";
        public static final String IDN_RESIDENT_ORG_ID = "IDN_RESIDENT_ORG_ID";
        public static final String IDN_OPERATION_INITIATOR_ID = "IDN_OPERATION_INITIATOR_ID";
        public static final String IDN_OPERATION_STATUS = "IDN_OPERATION_STATUS";
        public static final String IDN_CREATED_TIME = "IDN_CREATED_TIME";
        public static final String IDN_LAST_MODIFIED = "IDN_LAST_MODIFIED";
        public static final String IDN_OPERATION_POLICY = "IDN_OPERATION_POLICY";
    }

    /**
     * SQL Placeholders.
     */
    public static class UnitOperationStatusTableColumns {

        public static final String IDN_UNIT_OPERATION_ID = "IDN_UNIT_OPERATION_ID";
        public static final String IDN_OPERATION_ID = "IDN_OPERATION_ID";
        public static final String IDN_RESIDENT_RESOURCE_ID = "IDN_RESIDENT_RESOURCE_ID";
        public static final String IDN_TARGET_ORG_ID = "IDN_TARGET_ORG_ID";
        public static final String IDN_UNIT_OPERATION_STATUS = "IDN_UNIT_OPERATION_STATUS";
        public static final String IDN_OPERATION_STATUS_MESSAGE = "IDN_OPERATION_STATUS_MESSAGE";
        public static final String IDN_CREATED_AT = "IDN_CREATED_AT";
    }
}
