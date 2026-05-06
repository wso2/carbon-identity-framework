/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.dao;

/**
 * SQL constants for debug session persistence.
 */
public class SQLConstants {

    public static final String SQL_INSERT_DEBUG_SESSION = "INSERT INTO IDN_DEBUG_SESSION " +
            "(DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    public static final String SQL_GET_DEBUG_SESSION = "SELECT DEBUG_ID, STATUS, SESSION_DATA, " +
            "RESULT_JSON, CREATED_TIME, EXPIRY_TIME FROM IDN_DEBUG_SESSION WHERE DEBUG_ID = ?";

    public static final String SQL_DELETE_DEBUG_SESSION = "DELETE FROM IDN_DEBUG_SESSION WHERE DEBUG_ID = ?";

    public static final String SQL_DELETE_EXPIRED_DEBUG_SESSIONS
            = "DELETE FROM IDN_DEBUG_SESSION WHERE EXPIRY_TIME < ?";

    public static final String SQL_UPSERT_DEBUG_SESSION_H2 =
            "MERGE INTO IDN_DEBUG_SESSION (DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, " +
                    "CREATED_TIME, EXPIRY_TIME) KEY (DEBUG_ID) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String SQL_UPSERT_DEBUG_SESSION_MYSQL =
            "INSERT INTO IDN_DEBUG_SESSION (DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, " +
                    "CREATED_TIME, EXPIRY_TIME) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                    "STATUS = VALUES(STATUS), SESSION_DATA = VALUES(SESSION_DATA), " +
                    "RESULT_JSON = VALUES(RESULT_JSON), CREATED_TIME = VALUES(CREATED_TIME), " +
                    "EXPIRY_TIME = VALUES(EXPIRY_TIME)";
    public static final String SQL_UPSERT_DEBUG_SESSION_POSTGRESQL =
            "INSERT INTO IDN_DEBUG_SESSION (DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, " +
                    "CREATED_TIME, EXPIRY_TIME) VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT (DEBUG_ID) DO UPDATE SET " +
                    "STATUS = EXCLUDED.STATUS, SESSION_DATA = EXCLUDED.SESSION_DATA, " +
                    "RESULT_JSON = EXCLUDED.RESULT_JSON, CREATED_TIME = EXCLUDED.CREATED_TIME, " +
                    "EXPIRY_TIME = EXCLUDED.EXPIRY_TIME";
    public static final String SQL_UPSERT_DEBUG_SESSION_MSSQL_OR_DB2 =
            "MERGE INTO IDN_DEBUG_SESSION T USING (VALUES (?, ?, ?, ?, ?, ?)) S " +
                    "(DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME) " +
                    "ON T.DEBUG_ID = S.DEBUG_ID WHEN MATCHED THEN UPDATE SET " +
                    "STATUS = S.STATUS, SESSION_DATA = S.SESSION_DATA, RESULT_JSON = S.RESULT_JSON, " +
                    "CREATED_TIME = S.CREATED_TIME, EXPIRY_TIME = S.EXPIRY_TIME " +
                    "WHEN NOT MATCHED THEN INSERT (DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, " +
                    "EXPIRY_TIME) VALUES (S.DEBUG_ID, S.STATUS, S.SESSION_DATA, S.RESULT_JSON, S.CREATED_TIME, " +
                    "S.EXPIRY_TIME);";
    public static final String SQL_UPSERT_DEBUG_SESSION_ORACLE =
            "MERGE INTO IDN_DEBUG_SESSION T USING (SELECT ? AS DEBUG_ID, ? AS STATUS, " +
                    "? AS SESSION_DATA, ? AS RESULT_JSON, ? AS CREATED_TIME, ? AS EXPIRY_TIME FROM dual) " +
                    "S ON (T.DEBUG_ID = S.DEBUG_ID) " +
                    "WHEN MATCHED THEN UPDATE SET T.STATUS = S.STATUS, T.SESSION_DATA = S.SESSION_DATA, " +
                    "T.RESULT_JSON = S.RESULT_JSON, T.CREATED_TIME = S.CREATED_TIME, " +
                    "T.EXPIRY_TIME = S.EXPIRY_TIME WHEN NOT MATCHED THEN INSERT " +
                    "(DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME) VALUES " +
                    "(S.DEBUG_ID, S.STATUS, S.SESSION_DATA, S.RESULT_JSON, S.CREATED_TIME, S.EXPIRY_TIME)";

    private SQLConstants() {
    }
}
