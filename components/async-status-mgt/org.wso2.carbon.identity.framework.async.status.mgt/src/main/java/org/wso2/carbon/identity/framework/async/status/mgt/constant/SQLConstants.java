package org.wso2.carbon.identity.framework.async.status.mgt.constant;

public class SQLConstants {

    public static final String CREATE_B2B_RESOURCE_SHARING_OPERATION_UNIT = "INSERT INTO UM_SHARING_OPERATION_UNIT( UM_SHARE_OPERATION_ID, UM_SHARED_ORG_ID," + "UM_UNIT_OPERATION_STATUS, UM_STATUS_MESSAGE, UM_CREATED_TIME) VALUES(?,?,?,?,?)";

    public static final String CREATE_ASYNC_OPERATION = "INSERT INTO UM_ASYNC_OPERATION_STATUS(" +
            "UM_OPERATION_TYPE, UM_OPERATION_SUBJECT_ID, UM_RESOURCE_TYPE," +
            "UM_OPERATION_POLICY, UM_RESIDENT_ORG_ID, UM_OPERATION_INITIATOR_ID, UM_OPERATION_STATUS," +
            "UM_CREATED_TIME, UM_LAST_MODIFIED) VALUES("+
            ":" + OperationStatusTableColumns.UM_OPERATION_TYPE + ";, " +
            ":" + OperationStatusTableColumns.UM_OPERATION_SUBJECT_ID + ";, " +
            ":" + OperationStatusTableColumns.UM_RESOURCE_TYPE + ";, " +
            ":" + OperationStatusTableColumns.UM_OPERATION_POLICY + ";, " +
            ":" + OperationStatusTableColumns.UM_RESIDENT_ORG_ID + ";, " +
            ":" + OperationStatusTableColumns.UM_OPERATION_INITIATOR_ID + ";, " +
            ":" + OperationStatusTableColumns.UM_OPERATION_STATUS + ";, " +
            ":" + OperationStatusTableColumns.UM_CREATED_TIME + ";, " +
            ":" + OperationStatusTableColumns.UM_LAST_MODIFIED + ";)";

    public static final String CREATE_ASYNC_OPERATION_UNIT = "INSERT INTO UM_ASYNC_OPERATION_STATUS_UNIT (" +
            "UM_OPERATION_ID, UM_RESIDENT_RESOURCE_ID, UM_TARGET_ORG_ID," +
            "UM_UNIT_OPERATION_STATUS, UM_OPERATION_STATUS_MESSAGE, UM_CREATED_AT ) VALUES("+
            ":" + UnitOperationStatusTableColumns.UM_OPERATION_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.UM_RESIDENT_RESOURCE_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.UM_TARGET_ORG_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.UM_UNIT_OPERATION_STATUS + ";, " +
            ":" + UnitOperationStatusTableColumns.UM_OPERATION_STATUS_MESSAGE + ";, " +
            ":" + UnitOperationStatusTableColumns.UM_CREATED_AT + ";)";

    public static class OperationStatusTableColumns {
        public static final String UM_OPERATION_ID = "UM_OPERATION_ID";
        public static final String UM_OPERATION_TYPE = "UM_OPERATION_TYPE";
        public static final String UM_OPERATION_SUBJECT_ID = "UM_OPERATION_SUBJECT_ID";
        public static final String UM_RESOURCE_TYPE = "UM_RESOURCE_TYPE";
        public static final String UM_OPERATION_POLICY = "UM_OPERATION_POLICY";
        public static final String UM_RESIDENT_ORG_ID = "UM_RESIDENT_ORG_ID";
        public static final String UM_OPERATION_INITIATOR_ID = "UM_OPERATION_INITIATOR_ID";
        public static final String UM_OPERATION_STATUS = "UM_OPERATION_STATUS";
        public static final String UM_CREATED_TIME = "UM_CREATED_TIME";
        public static final String UM_LAST_MODIFIED = "UM_LAST_MODIFIED";
    }

    public static class UnitOperationStatusTableColumns {
        public static final String UM_UNIT_OPERATION_ID = "UM_UNIT_OPERATION_ID";
        public static final String UM_OPERATION_ID = "UM_OPERATION_ID";
        public static final String UM_RESIDENT_RESOURCE_ID = "UM_RESIDENT_RESOURCE_ID";
        public static final String UM_TARGET_ORG_ID = "UM_TARGET_ORG_ID";
        public static final String UM_UNIT_OPERATION_STATUS = "UM_UNIT_OPERATION_STATUS";
        public static final String UM_OPERATION_STATUS_MESSAGE = "UM_OPERATION_STATUS_MESSAGE";
        public static final String UM_CREATED_AT = "UM_CREATED_AT";
    }
 }
