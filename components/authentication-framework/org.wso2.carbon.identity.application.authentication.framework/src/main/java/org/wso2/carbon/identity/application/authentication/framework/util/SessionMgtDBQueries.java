/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org).
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

package org.wso2.carbon.identity.application.authentication.framework.util;

/**
 * This class contains default SQL queries.
 */
public class SessionMgtDBQueries {

    private static final String LOAD_SESSION_STORE_DATA = "SELECT SESSION_ID, MAX(TIME_CREATED) AS TIME_CREATED " +
            "FROM IDN_AUTH_SESSION_STORE WHERE SESSION_TYPE = ''AppAuthFrameworkSessionContextCache'' " +
            "AND EXPIRY_TIME > ? AND (TENANT_ID = ? OR TENANT_ID = -1){0} GROUP BY SESSION_ID";
            // TODO: remove TENANT_ID=-1 from clause when SessionDataStore.removeSessionData() had been updated
            //       to store session's actual tenant id. (https://github.com/wso2/product-is/issues/12210)

    private static final String LOAD_SESSION_METADATA_DATA = "SELECT SESSION_ID, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''IP'' THEN VALUE END) AS IP_ADDRESS, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''Login Time'' THEN VALUE END) AS LOGIN_TIME, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''Last Access Time'' THEN VALUE END) AS LAST_ACCESS_TIME, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''User Agent'' THEN VALUE END) AS USER_AGENT " +
            "FROM IDN_AUTH_SESSION_META_DATA GROUP BY SESSION_ID";

    private static final String LOAD_SESSION_METADATA_DATA_H2 = "SELECT SESSION_ID, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''IP'' THEN `VALUE` END) AS IP_ADDRESS, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''Login Time'' THEN `VALUE` END) AS LOGIN_TIME, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''Last Access Time'' THEN `VALUE` END) AS LAST_ACCESS_TIME, " +
            "MAX(CASE WHEN PROPERTY_TYPE = ''User Agent'' THEN `VALUE` END) AS USER_AGENT " +
            "FROM IDN_AUTH_SESSION_META_DATA GROUP BY SESSION_ID";

    private static final String LOAD_APP_DATA = "SELECT ID, APP_NAME, UUID FROM SP_APP {1}";

    private static final String LOAD_SESSION_APP_DATA_H2_MYSQL = "SELECT SESSION_ID, " +
            "GROUP_CONCAT(CONCAT_WS('':'', APP_ID, APP_NAME, SUBJECT, UUID) SEPARATOR ''|'') AS APPLICATIONS " +
            "FROM IDN_AUTH_SESSION_APP_INFO ai JOIN (" + LOAD_APP_DATA + ") a ON a.ID = ai.APP_ID " +
            "WHERE APP_ID IN (a.ID){2} GROUP BY SESSION_ID";

    private static final String LOAD_SESSION_APP_DATA_POSTGRES = "SELECT SESSION_ID, " +
            "STRING_AGG(CONCAT_WS('':'', APP_ID, APP_NAME, SUBJECT, UUID), ''|'') AS APPLICATIONS " +
            "FROM IDN_AUTH_SESSION_APP_INFO ai JOIN APPS a ON a.ID = ai.APP_ID " +
            "WHERE APP_ID IN (SELECT ID FROM APPS){2} GROUP BY SESSION_ID";

    private static final String LOAD_SESSION_APP_DATA_DB2 = "SELECT SESSION_ID, " +
            "LISTAGG(APP_ID CONCAT '':'' CONCAT APP_NAME CONCAT '':'' CONCAT SUBJECT CONCAT '':'' CONCAT UUID), '|') " +
            "AS APPLICATIONS FROM IDN_AUTH_SESSION_APP_INFO ai JOIN APPS a ON a.ID = ai.APP_ID " +
            "WHERE APP_ID IN (SELECT ID FROM APPS){2} GROUP BY SESSION_ID";

    private static final String LOAD_SESSION_APP_DATA_ORACLE = "SELECT SESSION_ID, " +
            "LISTAGG(APP_ID || '':'' || APP_NAME || '':'' || SUBJECT || '':'' || UUID, ''|'') " +
            "WITHIN GROUP (ORDER BY APP_ID) AS APPLICATIONS FROM IDN_AUTH_SESSION_APP_INFO ai " +
            "JOIN APPS a ON a.ID = ai.APP_ID WHERE APP_ID IN (SELECT ID FROM APPS){2} GROUP BY SESSION_ID";

    private static final String LOAD_SESSION_APP_DATA_MSSQL = "SELECT SESSION_ID, " +
            "STRING_AGG(CONCAT_WS('':'', APP_ID, APP_NAME, SUBJECT, UUID), ''|'') AS APPLICATIONS " +
            "FROM IDN_AUTH_SESSION_APP_INFO ai JOIN APPS a ON a.ID = ai.APP_ID " +
            "WHERE APP_ID IN (SELECT ID FROM APPS){2} GROUP BY SESSION_ID";

    public static final String LOAD_SESSIONS_H2 = "SELECT ss.SESSION_ID, TIME_CREATED, USER_ID, IP_ADDRESS, " +
            "LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS FROM IDN_AUTH_SESSION_STORE ss " +
            "JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN (" + LOAD_SESSION_METADATA_DATA_H2 + ") md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN (" + LOAD_SESSION_APP_DATA_H2_MYSQL + ") sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE (ss.SESSION_ID, TIME_CREATED) IN (SELECT SESSION_ID, TIME_CREATED FROM (" + LOAD_SESSION_STORE_DATA +
            ") s) AND OPERATION = ''STORE'' AND SESSION_TYPE = ''AppAuthFrameworkSessionContextCache''{3} " +
            "ORDER BY TIME_CREATED {4} LIMIT {5}";

    public static final String LOAD_SESSIONS_MYSQL = "SELECT ss.SESSION_ID, TIME_CREATED, USER_ID, IP_ADDRESS, " +
            "LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS FROM IDN_AUTH_SESSION_STORE ss " +
            "JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN (" + LOAD_SESSION_METADATA_DATA + ") md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN (" + LOAD_SESSION_APP_DATA_H2_MYSQL + ") sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE (ss.SESSION_ID, TIME_CREATED) IN (SELECT SESSION_ID, TIME_CREATED FROM (" + LOAD_SESSION_STORE_DATA +
            ") s) AND OPERATION = ''STORE'' AND SESSION_TYPE = ''AppAuthFrameworkSessionContextCache''{3} " +
            "ORDER BY TIME_CREATED {4} LIMIT {5}";

    public static final String LOAD_SESSIONS_POSTGRESQL = "WITH SESSIONS AS (" + LOAD_SESSION_STORE_DATA + "), " +
            "SESSION_METADATA AS (" + LOAD_SESSION_METADATA_DATA + "), APPS AS (" + LOAD_APP_DATA + "), " +
            "SESSION_APPS AS (" + LOAD_SESSION_APP_DATA_POSTGRES + ") SELECT ss.SESSION_ID, TIME_CREATED, USER_ID, " +
            "IP_ADDRESS, LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS FROM IDN_AUTH_SESSION_STORE ss " +
            "JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_METADATA md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_APPS sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE (ss.SESSION_ID, TIME_CREATED) IN (SELECT SESSION_ID, TIME_CREATED FROM SESSIONS) AND " +
            "OPERATION = ''STORE'' AND SESSION_TYPE = ''AppAuthFrameworkSessionContextCache''{3} " +
            "ORDER BY TIME_CREATED {4} LIMIT {5}";

    public static final String LOAD_SESSIONS_DB2 = "WITH SESSIONS AS (" + LOAD_SESSION_STORE_DATA + "), " +
            "SESSION_METADATA AS (" + LOAD_SESSION_METADATA_DATA + "), APPS AS (" + LOAD_APP_DATA + "), " +
            "SESSION_APPS AS (" + LOAD_SESSION_APP_DATA_DB2 + ") SELECT ss.SESSION_ID, TIME_CREATED, USER_ID, " +
            "IP_ADDRESS, LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS FROM IDN_AUTH_SESSION_STORE ss " +
            "JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_METADATA md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_APPS sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE (ss.SESSION_ID, TIME_CREATED) IN (SELECT SESSION_ID, TIME_CREATED FROM SESSIONS) AND " +
            "OPERATION = ''STORE'' AND SESSION_TYPE = ''AppAuthFrameworkSessionContextCache''{3} " +
            "ORDER BY TIME_CREATED {4} FETCH FIRST {5} ROWS ONLY";

    public static final String LOAD_SESSIONS_ORACLE = "WITH SESSIONS AS (" + LOAD_SESSION_STORE_DATA + "), " +
            "SESSION_METADATA AS (" + LOAD_SESSION_METADATA_DATA + "), APPS AS (" + LOAD_APP_DATA + "), " +
            "SESSION_APPS AS (" + LOAD_SESSION_APP_DATA_ORACLE + ") SELECT ss.SESSION_ID, TIME_CREATED, USER_ID, " +
            "IP_ADDRESS, LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS FROM IDN_AUTH_SESSION_STORE ss " +
            "JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_METADATA md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_APPS sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE (ss.SESSION_ID, TIME_CREATED) IN (SELECT SESSION_ID, TIME_CREATED FROM SESSIONS) AND " +
            "OPERATION = ''STORE'' AND SESSION_TYPE = ''AppAuthFrameworkSessionContextCache''{3} " +
            "ORDER BY TIME_CREATED {4} FETCH FIRST {5} ROWS ONLY";

    public static final String LOAD_SESSIONS_MSSQL = "WITH SESSIONS AS (" + LOAD_SESSION_STORE_DATA + "), " +
            "SESSION_METADATA AS (" + LOAD_SESSION_METADATA_DATA + "), APPS AS (" + LOAD_APP_DATA + "), " +
            "SESSION_APPS AS (" + LOAD_SESSION_APP_DATA_MSSQL + ") SELECT TOP({5}) ss.SESSION_ID, TIME_CREATED, " +
            "USER_ID, IP_ADDRESS, LOGIN_TIME, LAST_ACCESS_TIME, USER_AGENT, APPLICATIONS " +
            "FROM IDN_AUTH_SESSION_STORE ss JOIN IDN_AUTH_USER_SESSION_MAPPING sm ON sm.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_METADATA md ON md.SESSION_ID = ss.SESSION_ID " +
            "JOIN SESSION_APPS sa ON sa.SESSION_ID = ss.SESSION_ID " +
            "WHERE EXISTS (SELECT SESSION_ID, TIME_CREATED FROM SESSIONS s WHERE ss.SESSION_ID = s.SESSION_ID AND " +
            "ss.TIME_CREATED = s.TIME_CREATED) AND OPERATION = ''STORE'' " +
            "AND SESSION_TYPE = ''AppAuthFrameworkSessionContextCache''{3} ORDER BY TIME_CREATED {4}";
}
