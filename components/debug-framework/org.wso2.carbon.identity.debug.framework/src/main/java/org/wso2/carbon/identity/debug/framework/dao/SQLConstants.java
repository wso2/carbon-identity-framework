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
            "(DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME, RESOURCE_TYPE, CONNECTION_ID) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SQL_GET_DEBUG_SESSION = "SELECT DEBUG_ID, STATUS, SESSION_DATA, " +
            "RESULT_JSON, CREATED_TIME, EXPIRY_TIME, RESOURCE_TYPE, " +
            "CONNECTION_ID FROM IDN_DEBUG_SESSION WHERE DEBUG_ID = ?";

    public static final String SQL_DELETE_DEBUG_SESSION = "DELETE FROM IDN_DEBUG_SESSION WHERE DEBUG_ID = ?";

    public static final String SQL_DELETE_EXPIRED_DEBUG_SESSIONS
            = "DELETE FROM IDN_DEBUG_SESSION WHERE EXPIRY_TIME < ?";

    // MERGE statement for atomic upsert (H2 and most databases support this)
    public static final String SQL_UPSERT_DEBUG_SESSION
            = "MERGE INTO IDN_DEBUG_SESSION (DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, " +
            "CREATED_TIME, EXPIRY_TIME, RESOURCE_TYPE, CONNECTION_ID) KEY (DEBUG_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    // Legacy SQLs for backward compatibility (if DB schema is not updated)
    public static final String SQL_INSERT_DEBUG_SESSION_LEGACY = "INSERT INTO IDN_DEBUG_SESSION " +
            "(DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    public static final String SQL_GET_DEBUG_SESSION_LEGACY = "SELECT DEBUG_ID, STATUS, SESSION_DATA, " +
            "RESULT_JSON, CREATED_TIME, EXPIRY_TIME FROM IDN_DEBUG_SESSION WHERE DEBUG_ID = ?";

    public static final String SQL_UPSERT_DEBUG_SESSION_LEGACY = "MERGE INTO IDN_DEBUG_SESSION " +
            "(DEBUG_ID, STATUS, SESSION_DATA, RESULT_JSON, CREATED_TIME, EXPIRY_TIME) " +
            "KEY (DEBUG_ID) VALUES (?, ?, ?, ?, ?, ?)";

    private SQLConstants() {
    }
}
