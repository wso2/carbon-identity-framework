/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.application.authentication.framework.store;

/**
 * This class holds the SQL queries used by {@link UserSessionStore}.
 */
public class SQLQueries {

    private static final String SESSION_CONTEXT_CACHE_NAME = "AppAuthFrameworkSessionContextCache";
    private static final String DELETE_OPERATION = "DELETE";

    /**
     * Queries to store session data.
     */
    public static final String SQL_INSERT_USER_STORE_OPERATION =
            "INSERT INTO IDN_AUTH_USER(USER_ID, USER_NAME, TENANT_ID, DOMAIN_NAME, IDP_ID) VALUES " +
                    "(?,?,?,?,?)";

    public static final String SQL_INSERT_USER_SESSION_STORE_OPERATION =
            "INSERT INTO IDN_AUTH_USER_SESSION_MAPPING(USER_ID,SESSION_ID )VALUES (?,?)";

    /**
     * Queries to retrieve user ID.
     */
    public static final String SQL_SELECT_USER_ID =
            "SELECT USER_ID FROM IDN_AUTH_USER WHERE USER_NAME =? AND TENANT_ID =? AND " +
                    "DOMAIN_NAME =? AND IDP_ID = ?";

    public static final String SQL_SELECT_USER_IDS_OF_USER =
            "SELECT USER_ID FROM IDN_AUTH_USER WHERE USER_NAME =? AND TENANT_ID =? AND DOMAIN_NAME =?";

    public static final String SQL_SELECT_USER_IDS_OF_USER_STORE =
            "SELECT USER_ID FROM IDN_AUTH_USER WHERE DOMAIN_NAME = ? AND TENANT_ID =?";

    /**
     * Queries to retrieve session ID.
     */
    public static final String SQL_SELECT_SESSION_ID_OF_USER_ID =
            "SELECT SESSION_ID FROM IDN_AUTH_USER_SESSION_MAPPING WHERE USER_ID = ?";

    public static final String SQL_SELECT_TERMINATED_SESSION_IDS =
            "SELECT SESSION_ID FROM IDN_AUTH_SESSION_STORE WHERE SESSION_TYPE = '" + SESSION_CONTEXT_CACHE_NAME
                    + "' AND EXPIRY_TIME < ?";

    /**
     * Query to retrieve user session mapping.
     */
    public static final String SQL_SELECT_USER_SESSION_MAP =
            "SELECT * FROM IDN_AUTH_USER_SESSION_MAPPING WHERE USER_ID =? AND SESSION_ID =?";

    /**
     * Query to retrieve IdP Id of a registered IdP.
     */
    public static final String SQL_SELECT_IDP_ID_OF_IDP = "SELECT IDP.ID FROM IDP WHERE NAME = ?";

    /**
     * Query to delete session data.
     */
    public static final String SQL_DELETE_TERMINATED_SESSION_DATA =
            "DELETE FROM IDN_AUTH_USER_SESSION_MAPPING WHERE SESSION_ID = ?";
}
