package org.wso2.carbon.identity.framework.async.status.mgt.constant;

public class SQLConstants {
//    public static final String CREATE_B2B_RESOURCE_SHARING_OPERATION = "INSERT INTO UM_SHARING_OPERATION(" +
//            "UM_SHARING_OPERATION_TYPE, UM_RESIDENT_RESOURCE_ID, UM_RESOURCE_TYPE," +
//            "UM_SHARING_POLICY, UM_RESIDENT_ORG_ID, UM_OPERATION_INITIATOR_ID, UM_SHARING_OPERATION_STATUS," +
//            "UM_CREATED_TIME, UM_LAST_MODIFIED) VALUES("+
//            ":" + UMSharingOperationTableColumns.UM_SHARING_OPERATION_TYPE + ";, " +
//            ":" + UMSharingOperationTableColumns.UM_RESIDENT_RESOURCE_ID + ";, " +
//            ":" + UMSharingOperationTableColumns.UM_RESOURCE_TYPE + ";, " +
//            ":" + UMSharingOperationTableColumns.UM_SHARING_POLICY + ";, " +
//            ":" + UMSharingOperationTableColumns.UM_RESIDENT_ORG_ID + ";, " +
//            ":" + UMSharingOperationTableColumns.UM_OPERATION_INITIATOR_ID + ";, " +
//            ":" + UMSharingOperationTableColumns.UM_SHARING_OPERATION_STATUS + ";, " +
//            ":" + UMSharingOperationTableColumns.UM_CREATED_TIME + ";, " +
//            ":" + UMSharingOperationTableColumns.UM_LAST_MODIFIED + ";)";

    public static final String CREATE_B2B_RESOURCE_SHARING_OPERATION_UNIT = "INSERT INTO UM_SHARING_OPERATION_UNIT( UM_SHARE_OPERATION_ID, UM_SHARED_ORG_ID," + "UM_UNIT_OPERATION_STATUS, UM_STATUS_MESSAGE, UM_CREATED_TIME) VALUES(?,?,?,?,?)";

    public static final String CREATE_ASYNC_OPERATION = "INSERT INTO UM_ASYNC_OPERATION_STATUS(" +
            "UM_OPERATION_TYPE, UM_OPERATION_SUBJECT_ID, UM_RESOURCE_TYPE," +
            "UM_OPERATION_POLICY, UM_RESIDENT_ORG_ID, UM_OPERATION_INITIATOR_ID, UM_OPERATION_STATUS," +
            "UM_CREATED_TIME, UM_LAST_MODIFIED) VALUES("+
            ":" + UMAsyncOperationStatusTableColumns.UM_OPERATION_TYPE + ";, " +
            ":" + UMAsyncOperationStatusTableColumns.UM_OPERATION_SUBJECT_ID + ";, " +
            ":" + UMAsyncOperationStatusTableColumns.UM_RESOURCE_TYPE + ";, " +
            ":" + UMAsyncOperationStatusTableColumns.UM_OPERATION_POLICY + ";, " +
            ":" + UMAsyncOperationStatusTableColumns.UM_RESIDENT_ORG_ID + ";, " +
            ":" + UMAsyncOperationStatusTableColumns.UM_OPERATION_INITIATOR_ID + ";, " +
            ":" + UMAsyncOperationStatusTableColumns.UM_OPERATION_STATUS + ";, " +
            ":" + UMAsyncOperationStatusTableColumns.UM_CREATED_TIME + ";, " +
            ":" + UMAsyncOperationStatusTableColumns.UM_LAST_MODIFIED + ";)";

//    public static class UMSharingOperationTableColumns{
//        public static final String UM_SHARING_OPERATION_ID = "UM_SHARING_OPERATION_ID";
//        public static final String UM_SHARING_OPERATION_TYPE = "UM_SHARING_OPERATION_TYPE";
//        public static final String UM_RESIDENT_RESOURCE_ID = "UM_RESIDENT_RESOURCE_ID";
//        public static final String UM_RESOURCE_TYPE = "UM_RESOURCE_TYPE";
//        public static final String UM_SHARING_POLICY = "UM_SHARING_POLICY";
//        public static final String UM_RESIDENT_ORG_ID = "UM_RESIDENT_ORG_ID";
//        public static final String UM_OPERATION_INITIATOR_ID = "UM_OPERATION_INITIATOR_ID";
//        public static final String UM_SHARING_OPERATION_STATUS = "UM_SHARING_OPERATION_STATUS";
//        public static final String UM_CREATED_TIME = "UM_CREATED_TIME";
//        public static final String UM_LAST_MODIFIED = "UM_LAST_MODIFIED";
//    }
    public static class UMAsyncOperationStatusTableColumns{
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
 }
