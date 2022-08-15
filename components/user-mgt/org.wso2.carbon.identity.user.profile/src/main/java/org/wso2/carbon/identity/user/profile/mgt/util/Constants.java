/*
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.identity.user.profile.mgt.util;

public class Constants {

    public static class SQLQueries {

        public static final String UPDATE_USER_DOMAIN_NAME = "UPDATE IDN_ASSOCIATED_ID SET DOMAIN_NAME = ?" +
                                                             " WHERE DOMAIN_NAME = ? AND TENANT_ID = ?";

        public static final String DELETE_ASSOCIATED_ID_FROM_DOMAIN = "DELETE FROM IDN_ASSOCIATED_ID WHERE TENANT_ID " +
                "= ? AND DOMAIN_NAME = ?";

        public static final String ASSOCIATE_USER_ACCOUNTS = "INSERT INTO IDN_ASSOCIATED_ID (TENANT_ID, IDP_ID, " +
                "IDP_USER_ID, DOMAIN_NAME, USER_NAME, ASSOCIATION_ID) VALUES (? , (SELECT ID FROM IDP WHERE NAME = ? " +
                "AND TENANT_ID = ?), ? , ?, ?, ?)";

        public static final String RETRIEVE_USER_ASSOCIATED = "SELECT DOMAIN_NAME, USER_NAME FROM " +
                "IDN_ASSOCIATED_ID WHERE TENANT_ID = ? AND IDP_ID = (SELECT ID FROM IDP WHERE NAME = ? AND TENANT_ID " +
                "= ?) AND IDP_USER_ID = ?";

        public static final String DELETE_ASSOCIATION = "DELETE FROM IDN_ASSOCIATED_ID WHERE TENANT_ID = ? AND " +
                "IDP_ID" + " = (SELECT ID FROM IDP WHERE NAME = ? AND TENANT_ID = ? ) AND IDP_USER_ID = ? AND " +
                "USER_NAME = ? AND DOMAIN_NAME = ?";

        public static final String DELETE_ALL_ASSOCIATIONS_FOR_USER = "DELETE FROM IDN_ASSOCIATED_ID WHERE TENANT_ID " +
                "= ? AND DOMAIN_NAME = ? AND USER_NAME = ?";

        public static final String DELETE_ASSOCIATION_FOR_USER_BY_ID = "DELETE FROM IDN_ASSOCIATED_ID " +
                "WHERE DOMAIN_NAME = ? AND USER_NAME = ? AND ASSOCIATION_ID = ?";

        public static final String RETRIEVE_ASSOCIATIONS_FOR_USER = "SELECT IDP.NAME, IDP_USER_ID, ASSOCIATION_ID, " +
                "IDP.ID FROM IDN_ASSOCIATED_ID JOIN IDP ON IDN_ASSOCIATED_ID.IDP_ID = IDP.ID " +
                "WHERE IDN_ASSOCIATED_ID.TENANT_ID = ? AND USER_NAME = ? AND DOMAIN_NAME = ?";
    }
}
