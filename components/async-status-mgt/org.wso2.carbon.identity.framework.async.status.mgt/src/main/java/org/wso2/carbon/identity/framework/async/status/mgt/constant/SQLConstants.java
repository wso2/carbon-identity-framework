package org.wso2.carbon.identity.framework.async.status.mgt.constant;

public class SQLConstants {
    public static final String CREATE_B2B_RESOURCE_SHARING_OPERATION = "INSERT INTO UM_SHARING_OPERATION(" +
            "UM_SHARING_OPERATION_TYPE, UM_RESIDENT_RESOURCE_ID, UM_RESOURCE_TYPE," +
            "UM_SHARING_POLICY, UM_RESIDENT_ORG_ID, UM_OPERATION_INITIATOR_ID, UM_SHARING_OPERATION_STATUS," +
            "UM_CREATED_TIME, UM_LAST_MODIFIED) VALUES(?,?,?,?,?,?,?,?,?)";

    public static final String CREATE_B2B_RESOURCE_SHARING_OPERATION_UNIT = "INSERT INTO UM_SHARING_OPERATION_UNIT( UM_SHARE_OPERATION_ID, UM_SHARED_ORG_ID," + "UM_UNIT_OPERATION_STATUS, UM_STATUS_MESSAGE, UM_CREATED_TIME) VALUES(?,?,?,?,?)";

    public static class UMSharingOperationTableColumns{
        public static final String UM_SHARING_OPERATION_ID = "UM_SHARING_OPERATION_ID";
        public static final String UM_SHARING_OPERATION_TYPE = "UM_SHARING_OPERATION_TYPE";
        public static final String UM_RESIDENT_RESOURCE_ID = "UM_RESIDENT_RESOURCE_ID";
        public static final String UM_RESOURCE_TYPE = "UM_RESOURCE_TYPE";
        public static final String UM_SHARING_POLICY = "UM_SHARING_POLICY";
        public static final String UM_RESIDENT_ORG_ID = "UM_RESIDENT_ORG_ID";
        public static final String UM_OPERATION_INITIATOR_ID = "UM_OPERATION_INITIATOR_ID";
        public static final String UM_SHARING_OPERATION_STATUS = "UM_SHARING_OPERATION_STATUS";
        public static final String UM_CREATED_TIME = "UM_CREATED_TIME";
        public static final String UM_LAST_MODIFIED = "UM_LAST_MODIFIED";
    }

 }
