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

    // Retrieve application id given the name and the tenant id.
    public static final String SQL_SELECT_APP_ID_OF_APP = "SELECT ID FROM SP_APP WHERE APP_NAME =? AND TENANT_ID =?";

    public static final String SQL_STORE_IDN_AUTH_SESSION_APP_INFO =
            "INSERT INTO IDN_AUTH_SESSION_APP_INFO(SESSION_ID,SUBJECT,APP_ID,INBOUND_AUTH_TYPE)VALUES (?,?,?,?)";

    public static final String SQL_CHECK_IDN_AUTH_SESSION_APP_INFO =
            "SELECT 1 FROM IDN_AUTH_SESSION_APP_INFO WHERE SESSION_ID =? AND SUBJECT =? AND APP_ID =? AND " +
                    "INBOUND_AUTH_TYPE =?";

    public static final String SQL_DELETE_IDN_AUTH_SESSION_APP_INFO =
            "DELETE FROM IDN_AUTH_SESSION_APP_INFO WHERE SESSION_ID = ?";

    public static final String SQL_INSERT_SESSION_META_DATA =
            "INSERT INTO IDN_AUTH_SESSION_META_DATA(SESSION_ID,PROPERTY_TYPE,VALUE)VALUES (?,?,?)";

    public static final String SQL_UPDATE_SESSION_META_DATA = "UPDATE IDN_AUTH_SESSION_META_DATA SET VALUE=? WHERE " +
            "SESSION_ID =? AND PROPERTY_TYPE=?";

    public static final String SQL_DELETE_IDN_AUTH_SESSION_META_DATA =
            "DELETE FROM IDN_AUTH_SESSION_META_DATA WHERE SESSION_ID = ?";

    public static final String SQL_GET_PROPERTY_FROM_SESSION_META_DATA = "SELECT VALUE  FROM IDN_AUTH_SESSION_META" +
            "_DATA WHERE PROPERTY_TYPE = ? AND SESSION_ID = ?";

    public static final String SQL_GET_PROPERTIES_FROM_SESSION_META_DATA = "SELECT PROPERTY_TYPE, VALUE FROM " +
            "IDN_AUTH_SESSION_META_DATA WHERE SESSION_ID = ?";

    public static final String SQL_DELETE_TERMINATED_SESSION_DATA =
            "DELETE FROM IDN_AUTH_USER_SESSION_MAPPING WHERE SESSION_ID = ?";

    // Retrieve data for the Application model.
    public static final String SQL_GET_APPLICATION = "SELECT SUBJECT, APP_NAME, APP_ID FROM " +
            "IDN_AUTH_SESSION_APP_INFO SESSION_STORE, SP_APP APP where SESSION_STORE.APP_ID = APP.ID AND " +
            "SESSION_ID = ?";

    public static final String SQL_GET_SESSIONS_BY_USER = "SELECT SESSION_ID FROM IDN_AUTH_USER_SESSION_MAPPING " +
            "WHERE USER_ID = (SELECT USER_ID FROM IDN_AUTH_USER WHERE USER_NAME =? AND TENANT_ID =? AND " +
            "DOMAIN_NAME =? AND IDP_ID = ?)";

    public static final String SQL_GET_SESSION_MAPPING_BY_USER =
            "SELECT * FROM IDN_AUTH_USER_SESSION_MAPPING WHERE SESSION_ID =? AND USER_ID = (SELECT USER_ID FROM " +
                    "IDN_AUTH_USER WHERE USER_NAME =? AND TENANT_ID =? AND DOMAIN_NAME =? AND IDP_ID = ?)";

}
