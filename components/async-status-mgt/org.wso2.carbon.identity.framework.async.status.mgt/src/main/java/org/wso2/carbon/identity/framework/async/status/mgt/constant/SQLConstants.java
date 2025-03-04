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

    public static final String CREATE_ASYNC_OPERATION_IDN = "INSERT INTO IDN_ASYNC_OPERATION_STATUS(" +
            "IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_ID, IDN_RESOURCE_TYPE," +
            "IDN_RESIDENT_ORG_ID, IDN_OPERATION_INITIATOR_ID, IDN_OPERATION_STATUS," +
            "IDN_CREATED_TIME, IDN_LAST_MODIFIED) VALUES("+
            ":" + OperationStatusTableColumns.IDN_OPERATION_TYPE + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID + ";, " +
            ":" + OperationStatusTableColumns.IDN_RESOURCE_TYPE + ";, " +
            ":" + OperationStatusTableColumns.IDN_RESIDENT_ORG_ID + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID + ";, " +
            ":" + OperationStatusTableColumns.IDN_OPERATION_STATUS + ";, " +
            ":" + OperationStatusTableColumns.IDN_CREATED_TIME + ";, " +
            ":" + OperationStatusTableColumns.IDN_LAST_MODIFIED + ";)";

    public static final String UPDATE_ASYNC_OPERATION_STATUS =
            "UPDATE UM_ASYNC_OPERATION_STATUS " +
                    "SET UM_OPERATION_STATUS = :" + OperationStatusTableColumns.UM_OPERATION_STATUS + "; " +
                    "WHERE UM_OPERATION_ID = :" + OperationStatusTableColumns.UM_OPERATION_ID + ";";

    public static final String UPDATE_ASYNC_OPERATION_STATUS_IDN =
            "UPDATE IDN_ASYNC_OPERATION_STATUS " +
                    "SET IDN_OPERATION_STATUS = :" + OperationStatusTableColumns.IDN_OPERATION_STATUS + ", " +
                    "IDN_LAST_MODIFIED = :" + OperationStatusTableColumns.IDN_LAST_MODIFIED + " " +
                    "WHERE IDN_OPERATION_ID = :" + OperationStatusTableColumns.IDN_OPERATION_ID;

    public static final String CREATE_ASYNC_OPERATION_UNIT = "INSERT INTO UM_ASYNC_OPERATION_STATUS_UNIT (" +
            "UM_OPERATION_ID, UM_RESIDENT_RESOURCE_ID, UM_TARGET_ORG_ID," +
            "UM_UNIT_OPERATION_STATUS, UM_OPERATION_STATUS_MESSAGE, UM_CREATED_AT ) VALUES("+
            ":" + UnitOperationStatusTableColumns.UM_OPERATION_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.UM_RESIDENT_RESOURCE_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.UM_TARGET_ORG_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.UM_UNIT_OPERATION_STATUS + ";, " +
            ":" + UnitOperationStatusTableColumns.UM_OPERATION_STATUS_MESSAGE + ";, " +
            ":" + UnitOperationStatusTableColumns.UM_CREATED_AT + ";)";

    public static final String CREATE_ASYNC_OPERATION_UNIT_IDN = "INSERT INTO IDN_ASYNC_OPERATION_STATUS_UNIT (" +
            "IDN_OPERATION_ID, IDN_UNIT_OPERATION_TYPE, IDN_RESIDENT_RESOURCE_ID, IDN_TARGET_ORG_ID," +
            "IDN_UNIT_OPERATION_STATUS, IDN_OPERATION_STATUS_MESSAGE, IDN_CREATED_AT ) VALUES("+
            ":" + UnitOperationStatusTableColumns.IDN_OPERATION_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_TYPE + ";, " +
            ":" + UnitOperationStatusTableColumns.IDN_RESIDENT_RESOURCE_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.IDN_TARGET_ORG_ID + ";, " +
            ":" + UnitOperationStatusTableColumns.IDN_UNIT_OPERATION_STATUS + ";, " +
            ":" + UnitOperationStatusTableColumns.IDN_OPERATION_STATUS_MESSAGE + ";, " +
            ":" + UnitOperationStatusTableColumns.IDN_CREATED_AT + ";)";

    public static final String FETCH_LATEST_ASYNC_OPERATION_IDN =
            "SELECT IDN_OPERATION_ID, IDN_OPERATION_TYPE, IDN_OPERATION_SUBJECT_ID, IDN_RESOURCE_TYPE, " +
                    "IDN_RESIDENT_ORG_ID, IDN_OPERATION_INITIATOR_ID, IDN_OPERATION_STATUS " +
                    "FROM IDN_ASYNC_OPERATION_STATUS " +
                    "WHERE IDN_OPERATION_SUBJECT_ID = :" + OperationStatusTableColumns.IDN_OPERATION_SUBJECT_ID + " " +
                    "AND IDN_RESIDENT_ORG_ID = :" + OperationStatusTableColumns.IDN_RESIDENT_ORG_ID + " " +
                    "AND IDN_RESOURCE_TYPE = :" + OperationStatusTableColumns.IDN_RESOURCE_TYPE + " " +
                    "AND IDN_OPERATION_INITIATOR_ID = :" + OperationStatusTableColumns.IDN_OPERATION_INITIATOR_ID + " " +
                    "ORDER BY IDN_CREATED_TIME DESC " +
                    "LIMIT 1;";

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

        public static final String IDN_OPERATION_ID = "IDN_OPERATION_ID";
        public static final String IDN_OPERATION_TYPE = "IDN_OPERATION_TYPE";
        public static final String IDN_OPERATION_SUBJECT_ID = "IDN_OPERATION_SUBJECT_ID";
        public static final String IDN_RESOURCE_TYPE = "IDN_RESOURCE_TYPE";
        public static final String IDN_RESIDENT_ORG_ID = "IDN_RESIDENT_ORG_ID";
        public static final String IDN_OPERATION_INITIATOR_ID = "IDN_OPERATION_INITIATOR_ID";
        public static final String IDN_OPERATION_STATUS = "IDN_OPERATION_STATUS";
        public static final String IDN_CREATED_TIME = "IDN_CREATED_TIME";
        public static final String IDN_LAST_MODIFIED = "IDN_LAST_MODIFIED";
    }

    public static class UnitOperationStatusTableColumns {
        public static final String UM_UNIT_OPERATION_ID = "UM_UNIT_OPERATION_ID";
        public static final String UM_OPERATION_ID = "UM_OPERATION_ID";
        public static final String UM_RESIDENT_RESOURCE_ID = "UM_RESIDENT_RESOURCE_ID";
        public static final String UM_TARGET_ORG_ID = "UM_TARGET_ORG_ID";
        public static final String UM_UNIT_OPERATION_STATUS = "UM_UNIT_OPERATION_STATUS";
        public static final String UM_OPERATION_STATUS_MESSAGE = "UM_OPERATION_STATUS_MESSAGE";
        public static final String UM_CREATED_AT = "UM_CREATED_AT";

        public static final String IDN_UNIT_OPERATION_ID = "IDN_UNIT_OPERATION_ID";
        public static final String IDN_OPERATION_ID = "IDN_OPERATION_ID";
        public static final String IDN_UNIT_OPERATION_TYPE = "IDN_UNIT_OPERATION_TYPE";
        public static final String IDN_RESIDENT_RESOURCE_ID = "IDN_RESIDENT_RESOURCE_ID";
        public static final String IDN_TARGET_ORG_ID = "IDN_TARGET_ORG_ID";
        public static final String IDN_UNIT_OPERATION_STATUS = "IDN_UNIT_OPERATION_STATUS";
        public static final String IDN_OPERATION_STATUS_MESSAGE = "IDN_OPERATION_STATUS_MESSAGE";
        public static final String IDN_CREATED_AT = "IDN_CREATED_AT";
    }
 }
