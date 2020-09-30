package org.wso2.carbon.identity.cors.mgt.core.constant;

/**
 * Grouping of constants related to database tables.
 */
public class SchemaConstants {

    /**
     * Constants related to the IDN_CORS_ORIGIN table.
     */
    public static class CORSOriginTableColumns {

        public static final String ID = "ID";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String ORIGIN = "ORIGIN";
        public static final String UUID = "UUID";

        private CORSOriginTableColumns() {

        }
    }

    /**
     * Constants related to the IDN_CORS_ASSOCIATION table.
     */
    public static class CORSAssociationTableColumns {

        public static final String IDN_CORS_ORIGIN_ID = "IDN_CORS_ORIGIN_ID";
        public static final String SP_APP_ID = "SP_APP_ID";

        private CORSAssociationTableColumns() {

        }
    }
}
