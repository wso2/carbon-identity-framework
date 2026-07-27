/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.common.constant;

/**
 * SQL constants for authenticator configuration management service.
 */
public class AuthenticatorMgtSQLConstants {

    private AuthenticatorMgtSQLConstants() {

    }

    /**
     * Column Names.
     */
    public static class Column {

        public static final String IDP_ID = "ID";
        public static final String IDP_NAME = "IDP_NAME";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String NAME = "NAME";
        public static final String IS_ENABLED = "IS_ENABLED";
        public static final String DEFINED_BY = "DEFINED_BY";
        public static final String AUTHENTICATION_TYPE = "AUTHENTICATION_TYPE";
        public static final String DISPLAY_NAME = "DISPLAY_NAME";
        public static final String ID = "ID";
        public static final String AUTHENTICATOR_ID = "AUTHENTICATOR_ID";
        public static final String PROPERTY_KEY = "PROPERTY_KEY";
        public static final String PROPERTY_VALUE = "PROPERTY_VALUE";
        public static final String IS_SECRET = "IS_SECRET";
        public static final String IMAGE_URL = "IMAGE_URL";
        public static final String DESCRIPTION = "DESCRIPTION";

        private Column() {

        }
    }

    /**
     * Queries.
     */
    public static class Query {

        public static final String ADD_AUTHENTICATOR_SQL = "INSERT INTO IDP_AUTHENTICATOR " +
                "(TENANT_ID, IDP_ID, NAME, IS_ENABLED, DEFINED_BY, AUTHENTICATION_TYPE, DISPLAY_NAME, IMAGE_URL, " +
                "DESCRIPTION) VALUES" +
                " (:TENANT_ID;, (SELECT ID FROM IDP WHERE IDP.NAME = :IDP_NAME; AND IDP.TENANT_ID = :TENANT_ID;), " +
                ":NAME;, :IS_ENABLED;, :DEFINED_BY;, :AUTHENTICATION_TYPE;, :DISPLAY_NAME;, " +
                ":IMAGE_URL;, :DESCRIPTION;)";
        public static final String UPDATE_AUTHENTICATOR_SQL = "UPDATE IDP_AUTHENTICATOR SET IS_ENABLED = " +
                ":IS_ENABLED;, DISPLAY_NAME = :DISPLAY_NAME;, IMAGE_URL = :IMAGE_URL;, DESCRIPTION = :DESCRIPTION; " +
                "WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_USER_DEFINED_LOCAL_AUTHENTICATOR_SQL = "SELECT * FROM IDP_AUTHENTICATOR " +
                "WHERE DEFINED_BY = :DEFINED_BY; AND NAME = :NAME; AND TENANT_ID = :TENANT_ID;" +
                "AND IDP_ID IN (SELECT ID FROM IDP WHERE IDP.NAME = :IDP_NAME; " +
                        "AND IDP.TENANT_ID = :TENANT_ID;)";
        public static final String IS_AUTHENTICATOR_EXISTS_BY_NAME_SQL = "SELECT ID FROM IDP_AUTHENTICATOR " +
                "WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_ALL_USER_DEFINED_AUTHENTICATOR_SQL =
                "SELECT AUTHENTICATION_TYPE, NAME, DISPLAY_NAME, IMAGE_URL, DESCRIPTION, IS_ENABLED, DEFINED_BY, ID " +
                        "FROM IDP_AUTHENTICATOR " +
                        "WHERE DEFINED_BY = :DEFINED_BY; AND TENANT_ID = :TENANT_ID; " +
                        "AND IDP_ID IN (SELECT ID FROM IDP WHERE IDP.NAME = :IDP_NAME; " +
                        "AND IDP.TENANT_ID = :TENANT_ID;)";
        public static final String DELETE_AUTHENTICATOR_SQL = "DELETE FROM IDP_AUTHENTICATOR WHERE NAME = :NAME; " +
                " AND TENANT_ID = :TENANT_ID;";
        public static final String GET_AUTHENTICATOR_ID_SQL = "SELECT ID FROM IDP_AUTHENTICATOR " +
                "WHERE NAME = :NAME; AND TENANT_ID = :TENANT_ID;";
        public static final String ADD_AUTHENTICATOR_PROP_SQL = "INSERT INTO IDP_AUTHENTICATOR_PROPERTY " +
                "(AUTHENTICATOR_ID, TENANT_ID, PROPERTY_KEY, PROPERTY_VALUE, IS_SECRET) VALUES " +
                "(:AUTHENTICATOR_ID;, :TENANT_ID;, :PROPERTY_KEY;, :PROPERTY_VALUE;, :IS_SECRET;)";
        public static final String DELETE_AUTHENTICATOR_PROP_SQL = "DELETE FROM IDP_AUTHENTICATOR_PROPERTY " +
                "WHERE AUTHENTICATOR_ID = :AUTHENTICATOR_ID; AND TENANT_ID = :TENANT_ID;";
        public static final String GET_AUTHENTICATOR_PROP_SQL = "SELECT PROPERTY_KEY, PROPERTY_VALUE, IS_SECRET" +
                " FROM IDP_AUTHENTICATOR_PROPERTY " +
                "WHERE AUTHENTICATOR_ID = :AUTHENTICATOR_ID; AND TENANT_ID = :TENANT_ID;";

        private Query() {

        }
    }
}
