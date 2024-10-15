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

public class AuthenticatorMgtSQLConstants {

    private AuthenticatorMgtSQLConstants() {

    }

    /**
     * Column Names.
     */
    public static class Column {

        public static final String IDP_ID = "IDP_ID";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String NAME = "NAME";
        public static final String IS_ENABLED = "IS_ENABLED";
        public static final String DEFINED_BY = "DEFINED_BY";
        public static final String AUTHENTICATOR_TYPE = "AUTHENTICATOR_TYPE";
        public static final String DISPLAY_NAME = "DISPLAY_NAME";

        private Column() {

        }
    }

    /**
     * Queries.
     */
    public static class Query {

        public static final String ADD_USER_DEFINED_AUTHENTICATOR= "INSERT INTO IDP_AUTHENTICATOR " +
                "(TENANT_ID, IDP_ID, NAME, IS_ENABLED, DEFINED_BY, AUTHENTICATOR_TYPE, DISPLAY_NAME) VALUES" +
                " (:TENANT_ID, (SELECT ID FROM IDP WHERE IDP.NAME=? AND IDP.TENANT_ID =?), " +
                ":NAME, :IS_ENABLED, :DEFINED_BY, :AUTHENTICATOR_TYPE, :DISPLAY_NAME)";

        private Query() {

        }
    }
}
