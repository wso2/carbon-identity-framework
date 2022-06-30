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

import static org.wso2.carbon.identity.application.authentication.framework.dao.impl.UserSessionDAOImpl.SCOPE_LIST_PLACEHOLDER;

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

    public static final String SQL_SELECT_INFO_OF_USER_ID =
            "SELECT USER_ID FROM IDN_AUTH_USER WHERE USER_ID = ?";

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

    public static final String SQL_SELECT_IDP_WITH_TENANT = "SELECT IDP.ID FROM IDP WHERE NAME = ? AND TENANT_ID = ?";

    // Retrieve application id given the name and the tenant id.
    public static final String SQL_SELECT_APP_ID_OF_APP = "SELECT ID FROM SP_APP WHERE APP_NAME =? AND TENANT_ID =?";

    public static final String SQL_STORE_IDN_AUTH_SESSION_APP_INFO_H2 =
            "MERGE INTO IDN_AUTH_SESSION_APP_INFO KEY(SESSION_ID,SUBJECT,APP_ID,INBOUND_AUTH_TYPE) VALUES(?, ?, ?, ?)";

    public static final String SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MSSQL_OR_DB2 =
            "MERGE INTO IDN_AUTH_SESSION_APP_INFO T USING " +
                    "(VALUES(?, ?, ?, ?)) S (SESSION_ID,SUBJECT,APP_ID,INBOUND_AUTH_TYPE) " +
                    "ON (T.SESSION_ID = S.SESSION_ID AND " +
                    "T.SUBJECT = S.SUBJECT AND " +
                    "T.APP_ID = S.APP_ID AND " +
                    "T.INBOUND_AUTH_TYPE = S.INBOUND_AUTH_TYPE ) " +
                    "WHEN NOT MATCHED THEN INSERT (SESSION_ID,SUBJECT,APP_ID,INBOUND_AUTH_TYPE) " +
                    "VALUES (S.SESSION_ID,S.SUBJECT,S.APP_ID,S.INBOUND_AUTH_TYPE);";

    public static final String SQL_STORE_IDN_AUTH_SESSION_APP_INFO_MYSQL_OR_MARIADB =
            "INSERT INTO IDN_AUTH_SESSION_APP_INFO(SESSION_ID,SUBJECT,APP_ID,INBOUND_AUTH_TYPE)VALUES " +
                    "(?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "SESSION_ID= VALUES(SESSION_ID), SUBJECT= VALUES(SUBJECT), APP_ID = VALUES(APP_ID), " +
                    "INBOUND_AUTH_TYPE = VALUES(INBOUND_AUTH_TYPE);";

    public static final String SQL_STORE_IDN_AUTH_SESSION_APP_INFO_POSTGRES =
            "INSERT INTO IDN_AUTH_SESSION_APP_INFO(SESSION_ID,SUBJECT,APP_ID,INBOUND_AUTH_TYPE)VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT(SESSION_ID,SUBJECT,APP_ID,INBOUND_AUTH_TYPE) DO UPDATE SET " +
                    "SESSION_ID = EXCLUDED.SESSION_ID, SUBJECT = EXCLUDED.SUBJECT, APP_ID = EXCLUDED.APP_ID, " +
                    "INBOUND_AUTH_TYPE = EXCLUDED.INBOUND_AUTH_TYPE;";

    public static final String SQL_STORE_IDN_AUTH_SESSION_APP_INFO_ORACLE =
            "MERGE INTO IDN_AUTH_SESSION_APP_INFO USING dual ON " +
                    "(SESSION_ID = ? AND SUBJECT = ? AND APP_ID = ? AND INBOUND_AUTH_TYPE = ?) " +
                    "WHEN NOT MATCHED THEN INSERT (SESSION_ID, SUBJECT, APP_ID, INBOUND_AUTH_TYPE) " +
                    "VALUES (?, ?, ?, ?)";

    public static final String SQL_CHECK_IDN_AUTH_SESSION_APP_INFO =
            "SELECT 1 FROM IDN_AUTH_SESSION_APP_INFO WHERE SESSION_ID =? AND SUBJECT =? AND APP_ID =? AND " +
                    "INBOUND_AUTH_TYPE =?";

    public static final String SQL_DELETE_IDN_AUTH_SESSION_APP_INFO =
            "DELETE FROM IDN_AUTH_SESSION_APP_INFO WHERE SESSION_ID = ?";

    public static final String SQL_INSERT_SESSION_META_DATA =
            "INSERT INTO IDN_AUTH_SESSION_META_DATA(SESSION_ID,PROPERTY_TYPE,VALUE)VALUES (?,?,?)";

    public static final String SQL_INSERT_SESSION_META_DATA_H2 =
            "INSERT INTO IDN_AUTH_SESSION_META_DATA(SESSION_ID,PROPERTY_TYPE,`VALUE`)VALUES (?,?,?)";

    public static final String SQL_UPDATE_SESSION_META_DATA = "UPDATE IDN_AUTH_SESSION_META_DATA SET VALUE=? WHERE " +
            "SESSION_ID =? AND PROPERTY_TYPE=?";

    public static final String SQL_UPDATE_SESSION_META_DATA_H2 = "UPDATE IDN_AUTH_SESSION_META_DATA SET `VALUE`=? " +
            "WHERE SESSION_ID =? AND PROPERTY_TYPE=?";

    public static final String SQL_DELETE_IDN_AUTH_SESSION_META_DATA =
            "DELETE FROM IDN_AUTH_SESSION_META_DATA WHERE SESSION_ID = ?";

    public static final String SQL_GET_PROPERTY_FROM_SESSION_META_DATA = "SELECT VALUE  FROM IDN_AUTH_SESSION_META" +
            "_DATA WHERE PROPERTY_TYPE = ? AND SESSION_ID = ?";

    public static final String SQL_GET_PROPERTY_FROM_SESSION_META_DATA_H2 = "SELECT `VALUE`  FROM " +
            "IDN_AUTH_SESSION_META_DATA WHERE PROPERTY_TYPE = ? AND SESSION_ID = ?";

    public static final String SQL_GET_PROPERTIES_FROM_SESSION_META_DATA = "SELECT PROPERTY_TYPE, VALUE FROM " +
            "IDN_AUTH_SESSION_META_DATA WHERE SESSION_ID = ?";

    public static final String SQL_GET_PROPERTIES_FROM_SESSION_META_DATA_H2 = "SELECT PROPERTY_TYPE, `VALUE` FROM " +
            "IDN_AUTH_SESSION_META_DATA WHERE SESSION_ID = ?";

    public static final String SQL_DELETE_TERMINATED_SESSION_DATA =
            "DELETE FROM IDN_AUTH_USER_SESSION_MAPPING WHERE SESSION_ID = ?";

    // Retrieve data for the Application model.
    public static final String SQL_GET_APPS_FOR_SESSION_ID = "SELECT SUBJECT, APP_ID FROM IDN_AUTH_SESSION_APP_INFO " +
            "WHERE SESSION_ID = ?";

    public static final String SQL_GET_APPLICATION = "SELECT ID, APP_NAME, UUID FROM SP_APP WHERE ID IN (" +
            SCOPE_LIST_PLACEHOLDER + ")";

    public static final String SQL_GET_SESSIONS_BY_USER = "SELECT SESSION_ID FROM IDN_AUTH_USER_SESSION_MAPPING " +
            "WHERE USER_ID = (SELECT USER_ID FROM IDN_AUTH_USER WHERE USER_NAME =? AND TENANT_ID =? AND " +
            "DOMAIN_NAME =? AND IDP_ID = ?)";

    public static final String SQL_GET_SESSION_MAPPING_BY_USER =
            "SELECT * FROM IDN_AUTH_USER_SESSION_MAPPING WHERE SESSION_ID =? AND USER_ID = (SELECT USER_ID FROM " +
                    "IDN_AUTH_USER WHERE USER_NAME =? AND TENANT_ID =? AND DOMAIN_NAME =? AND IDP_ID = ?)";

    // Store federated authentication session details to map the session context key with the idp session index.
    public static final String SQL_STORE_FEDERATED_AUTH_SESSION_INFO = "INSERT INTO IDN_FED_AUTH_SESSION_MAPPING "
            + "(IDP_SESSION_ID, SESSION_ID, IDP_NAME,  AUTHENTICATOR_ID, PROTOCOL_TYPE) VALUES (?, ?, ?, ?, ?)";

    // Store federated authentication session details to map the session context key with the idp session index.
    public static final String SQL_STORE_FEDERATED_AUTH_SESSION_INFO_WITH_TENANT = "INSERT INTO " +
            "IDN_FED_AUTH_SESSION_MAPPING (IDP_SESSION_ID, SESSION_ID, IDP_NAME,  AUTHENTICATOR_ID, " +
            "PROTOCOL_TYPE, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?)";

    // Get federated authentication session id using the idp session id.
    public static final String SQL_GET_FEDERATED_AUTH_SESSION_ID_BY_SESSION_ID = "SELECT SESSION_ID FROM " +
            "IDN_FED_AUTH_SESSION_MAPPING WHERE IDP_SESSION_ID = ?";

    // Get federated authentication session id using the idp session id and the tenant id.
    public static final String SQL_GET_FEDERATED_AUTH_SESSION_ID_BY_SESSION_ID_WITH_TENANT = "SELECT SESSION_ID " +
            "FROM IDN_FED_AUTH_SESSION_MAPPING WHERE IDP_SESSION_ID = ? AND TENANT_ID = ?";

    // Update federated authentication session id using the idp session id.
    public static final String SQL_UPDATE_FEDERATED_AUTH_SESSION_INFO = "UPDATE IDN_FED_AUTH_SESSION_MAPPING SET " +
            "SESSION_ID=? WHERE IDP_SESSION_ID=?";

    // Update federated authentication session id using the idp session id and the tenant id.
    public static final String SQL_UPDATE_FEDERATED_AUTH_SESSION_INFO_WITH_TENANT = "UPDATE " +
            "IDN_FED_AUTH_SESSION_MAPPING SET SESSION_ID=? WHERE IDP_SESSION_ID=? AND TENANT_ID = ?";

    // Get federated authentication session details if there is an already existing session.
    public static final String SQL_GET_FEDERATED_AUTH_SESSION_INFO_BY_SESSION_ID =
            "SELECT IDP_SESSION_ID, SESSION_ID, IDP_NAME, AUTHENTICATOR_ID, PROTOCOL_TYPE FROM " +
                    "IDN_FED_AUTH_SESSION_MAPPING WHERE IDP_SESSION_ID = ?";

    // Remove federated authentication session details of a given session context key.
    public static final String SQL_DELETE_FEDERATED_AUTH_SESSION_INFO = "DELETE FROM IDN_FED_AUTH_SESSION_MAPPING"
            + " WHERE SESSION_ID=?";

    public static final String SQL_GET_ACTIVE_SESSION_COUNT_BY_TENANT =
            "SELECT COUNT( DISTINCT IDN_AUTH_SESSION_META_DATA.SESSION_ID) " +
                    "FROM IDN_AUTH_SESSION_META_DATA INNER JOIN IDN_AUTH_USER_SESSION_MAPPING " +
                    "ON IDN_AUTH_SESSION_META_DATA.SESSION_ID = IDN_AUTH_USER_SESSION_MAPPING.SESSION_ID " +
                    "INNER JOIN IDN_AUTH_SESSION_STORE " +
                    "ON IDN_AUTH_USER_SESSION_MAPPING.SESSION_ID = IDN_AUTH_SESSION_STORE.SESSION_ID " +
                    "WHERE IDN_AUTH_SESSION_META_DATA.PROPERTY_TYPE = ? AND IDN_AUTH_SESSION_META_DATA.VALUE " +
                    "BETWEEN ? AND ? AND IDN_AUTH_SESSION_STORE.TENANT_ID = ? ";

    public static final String SQL_GET_ACTIVE_SESSION_COUNT_BY_TENANT_H2 =
            "SELECT COUNT( DISTINCT IDN_AUTH_SESSION_META_DATA.SESSION_ID) " +
                    "FROM IDN_AUTH_SESSION_META_DATA INNER JOIN IDN_AUTH_USER_SESSION_MAPPING " +
                    "ON IDN_AUTH_SESSION_META_DATA.SESSION_ID = IDN_AUTH_USER_SESSION_MAPPING.SESSION_ID " +
                    "INNER JOIN IDN_AUTH_SESSION_STORE " +
                    "ON IDN_AUTH_USER_SESSION_MAPPING.SESSION_ID = IDN_AUTH_SESSION_STORE.SESSION_ID " +
                    "WHERE IDN_AUTH_SESSION_META_DATA.PROPERTY_TYPE = ? AND IDN_AUTH_SESSION_META_DATA.`VALUE` " +
                    "BETWEEN ? AND ? AND IDN_AUTH_SESSION_STORE.TENANT_ID = ? ";
}
