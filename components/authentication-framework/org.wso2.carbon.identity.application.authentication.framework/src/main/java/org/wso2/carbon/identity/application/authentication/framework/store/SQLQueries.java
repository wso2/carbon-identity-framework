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

    public static final String SQL_GET_SESSION_META_DATA_FOR_USER_ID_AND_SESSION_ID =
            "SELECT PROPERTY_TYPE, VALUE FROM IDN_AUTH_SESSION_META_DATA sm JOIN IDN_AUTH_USER_SESSION_MAPPING su " +
                    "ON sm.SESSION_ID = su.SESSION_ID WHERE sm.SESSION_ID = ? AND USER_ID = ?";

    public static final String SQL_GET_SESSION_META_DATA_FOR_USER_ID_AND_SESSION_ID_H2 =
            "SELECT PROPERTY_TYPE, `VALUE` FROM IDN_AUTH_SESSION_META_DATA sm JOIN IDN_AUTH_USER_SESSION_MAPPING su " +
                    "ON sm.SESSION_ID = su.SESSION_ID WHERE sm.SESSION_ID = ? AND USER_ID = ?";

    public static final String SQL_DELETE_TERMINATED_SESSION_DATA =
            "DELETE FROM IDN_AUTH_USER_SESSION_MAPPING WHERE SESSION_ID = ?";

    // Retrieve data for the Application model.
    public static final String SQL_GET_APPS_FOR_SESSION_ID = "SELECT SUBJECT, APP_ID FROM IDN_AUTH_SESSION_APP_INFO " +
            "WHERE SESSION_ID = ?";

    public static final String SQL_GET_APPLICATION = "SELECT ID, APP_NAME, UUID FROM SP_APP WHERE ID IN (" +
            SCOPE_LIST_PLACEHOLDER + ")";

    public static final String SQL_GET_APPLICATIONS_BY_FILTER_AND_TENANT = "SELECT ID, APP_NAME, UUID FROM SP_APP {0}";

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

    private static final String SQL_LOAD_SESSION_STORE_DATA = "SELECT SESSION_ID, MAX(TIME_CREATED) AS TIME_CREATED " +
            "FROM IDN_AUTH_SESSION_STORE WHERE EXPIRY_TIME > ? AND SESSION_TYPE = ''AppAuthFrameworkSessionContextCache'' " +
            "AND (TENANT_ID = ? OR TENANT_ID = -1){0} GROUP BY SESSION_ID";
    // TODO: remove TENANT_ID=-1 from clause when SessionDataStore.removeSessionData() had been updated
    //       to store session's actual tenant id (https://github.com/wso2/product-is/issues/12210).

    private static final String SQL_LOAD_SESSION_METADATA_DATA = "SELECT SESSION_ID, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''IP'' THEN VALUE END) AS IP_ADDRESS, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''Login Time'' THEN VALUE END) AS LOGIN_TIME, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''Last Access Time'' THEN VALUE END) AS LAST_ACCESS_TIME, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''User Agent'' THEN VALUE END) AS USER_AGENT " +
            "FROM IDN_AUTH_SESSION_META_DATA GROUP BY SESSION_ID";

    private static final String SQL_LOAD_SESSION_METADATA_DATA_H2 = "SELECT SESSION_ID, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''IP'' THEN `VALUE` END) AS IP_ADDRESS, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''Login Time'' THEN `VALUE` END) AS LOGIN_TIME, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''Last Access Time'' THEN `VALUE` END) AS LAST_ACCESS_TIME, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''User Agent'' THEN `VALUE` END) AS USER_AGENT " +
            "FROM IDN_AUTH_SESSION_META_DATA GROUP BY SESSION_ID";

    private static final String SQL_LOAD_SESSION_APP_DATA_H2_MYSQL = "SELECT SESSION_ID," +
            "GROUP_CONCAT(CONCAT_WS('':'', APP_ID, SUBJECT) SEPARATOR ''|'') AS APPLICATIONS " +
            "FROM IDN_AUTH_SESSION_APP_INFO {1} {2} GROUP BY SESSION_ID";

    private static final String SQL_LOAD_SESSION_APP_DATA_POSTGRES = "SELECT SESSION_ID, " +
            "STRING_AGG(CONCAT_WS('':'', APP_ID, SUBJECT), ''|'') AS APPLICATIONS " +
            "FROM IDN_AUTH_SESSION_APP_INFO {1} {2} GROUP BY SESSION_ID";

    private static final String SQL_LOAD_SESSION_APP_DATA_DB2 = "SELECT SESSION_ID, " +
            "LISTAGG(APP_ID CONCAT '':'' CONCAT SUBJECT), '|') " +
            "AS APPLICATIONS FROM IDN_AUTH_SESSION_APP_INFO {1} {2} GROUP BY SESSION_ID";

    private static final String SQL_LOAD_SESSION_APP_DATA_ORACLE = "SELECT SESSION_ID, " +
            "LISTAGG(APP_ID || '':'' || SUBJECT, ''|'') " +
            "WITHIN GROUP (ORDER BY APP_ID) AS APPLICATIONS FROM IDN_AUTH_SESSION_APP_INFO {1} {2} " +
            "GROUP BY SESSION_ID";

    private static final String SQL_LOAD_SESSION_APP_DATA_MSSQL = "SELECT SESSION_ID, " +
            "STRING_AGG(CONCAT_WS('':'', APP_ID, SUBJECT), ''|'') AS APPLICATIONS " +
            "FROM IDN_AUTH_SESSION_APP_INFO {1} {2} GROUP BY SESSION_ID";

    public static final String SQL_LOAD_SESSIONS_H2 = "SELECT ss.SESSION_ID, TIME_CREATED, USER_ID, IP_ADDRESS, " +
            "LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS FROM IDN_AUTH_SESSION_STORE ss " +
            "JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN (" + SQL_LOAD_SESSION_METADATA_DATA_H2 + ") md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN (" + SQL_LOAD_SESSION_APP_DATA_H2_MYSQL + ") sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE (ss.SESSION_ID, TIME_CREATED) IN (SELECT SESSION_ID, TIME_CREATED FROM (" +
            SQL_LOAD_SESSION_STORE_DATA + ") s) AND OPERATION = ''STORE'' AND SESSION_TYPE = " +
            "''AppAuthFrameworkSessionContextCache''{3} ORDER BY TIME_CREATED {4} LIMIT {5}";

    public static final String SQL_LOAD_SESSIONS_MYSQL = "SELECT ss.SESSION_ID, TIME_CREATED, USER_ID, IP_ADDRESS, " +
            "LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS FROM IDN_AUTH_SESSION_STORE ss " +
            "JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN (" + SQL_LOAD_SESSION_METADATA_DATA + ") md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN (" + SQL_LOAD_SESSION_APP_DATA_H2_MYSQL + ") sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE (ss.SESSION_ID, TIME_CREATED) IN (SELECT SESSION_ID, TIME_CREATED FROM (" +
            SQL_LOAD_SESSION_STORE_DATA + ") s) AND OPERATION = ''STORE'' AND SESSION_TYPE = " +
            "''AppAuthFrameworkSessionContextCache''{3} ORDER BY TIME_CREATED {4} LIMIT {5}";

    public static final String SQL_LOAD_SESSIONS_POSTGRESQL = "WITH SESSIONS AS (" + SQL_LOAD_SESSION_STORE_DATA +
            "), SESSION_METADATA AS (" + SQL_LOAD_SESSION_METADATA_DATA + "), " + "SESSION_APPS AS (" +
            SQL_LOAD_SESSION_APP_DATA_POSTGRES + ") SELECT ss.SESSION_ID, TIME_CREATED, USER_ID, " +
            "IP_ADDRESS, LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS FROM IDN_AUTH_SESSION_STORE ss " +
            "JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_METADATA md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_APPS sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE (ss.SESSION_ID, TIME_CREATED) IN (SELECT SESSION_ID, TIME_CREATED FROM SESSIONS) AND " +
            "OPERATION = ''STORE'' AND SESSION_TYPE = ''AppAuthFrameworkSessionContextCache''{3} " +
            "ORDER BY TIME_CREATED {4} LIMIT {5}";

    public static final String SQL_LOAD_SESSIONS_DB2 = "WITH SESSIONS AS (" + SQL_LOAD_SESSION_STORE_DATA + "), " +
            "SESSION_METADATA AS (" + SQL_LOAD_SESSION_METADATA_DATA + "), " + "SESSION_APPS AS (" +
            SQL_LOAD_SESSION_APP_DATA_DB2 + ") SELECT ss.SESSION_ID, TIME_CREATED, USER_ID, " +
            "IP_ADDRESS, LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS FROM IDN_AUTH_SESSION_STORE ss " +
            "JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_METADATA md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_APPS sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE (ss.SESSION_ID, TIME_CREATED) IN (SELECT SESSION_ID, TIME_CREATED FROM SESSIONS) AND " +
            "OPERATION = ''STORE'' AND SESSION_TYPE = ''AppAuthFrameworkSessionContextCache''{3} " +
            "ORDER BY TIME_CREATED {4} FETCH FIRST {5} ROWS ONLY";

    public static final String SQL_LOAD_SESSIONS_ORACLE = "WITH SESSIONS AS (" + SQL_LOAD_SESSION_STORE_DATA + "), " +
            "SESSION_METADATA AS (" + SQL_LOAD_SESSION_METADATA_DATA + "), " + "SESSION_APPS AS (" +
            SQL_LOAD_SESSION_APP_DATA_ORACLE + ") SELECT ss.SESSION_ID, TIME_CREATED, USER_ID, " +
            "IP_ADDRESS, LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS FROM IDN_AUTH_SESSION_STORE ss " +
            "JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_METADATA md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_APPS sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE (ss.SESSION_ID, TIME_CREATED) IN (SELECT SESSION_ID, TIME_CREATED FROM SESSIONS) AND " +
            "OPERATION = ''STORE'' AND SESSION_TYPE = ''AppAuthFrameworkSessionContextCache''{3} " +
            "ORDER BY TIME_CREATED {4} FETCH FIRST {5} ROWS ONLY";

    public static final String SQL_LOAD_SESSIONS_MSSQL = "WITH SESSIONS AS (" + SQL_LOAD_SESSION_STORE_DATA + "), " +
            "SESSION_METADATA AS (" + SQL_LOAD_SESSION_METADATA_DATA + "), " + "SESSION_APPS AS (" +
            SQL_LOAD_SESSION_APP_DATA_MSSQL + ") SELECT TOP({5}) ss.SESSION_ID, TIME_CREATED, " +
            "USER_ID, IP_ADDRESS, LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS " +
            "FROM IDN_AUTH_SESSION_STORE ss JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_METADATA md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_APPS sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE EXISTS (SELECT SESSION_ID, TIME_CREATED FROM SESSIONS s WHERE ss.SESSION_ID = s.SESSION_ID AND " +
            "ss.TIME_CREATED = s.TIME_CREATED) AND OPERATION = ''STORE'' " +
            "AND SESSION_TYPE = ''AppAuthFrameworkSessionContextCache''{3} ORDER BY TIME_CREATED {4}";
}
