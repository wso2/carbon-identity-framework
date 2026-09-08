/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.device.policy.internal.constant;

/**
 * SQL constants used by the device token replay store and its cleanup task.
 */
public final class DevicePolicySQLConstants {

    private DevicePolicySQLConstants() {
    }

    /**
     * Column and named-parameter names.
     */
    public static final class Column {

        public static final String JTI = "JTI";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String IAT = "IAT";
        public static final String EXPIRY_TIME = "EXPIRY_TIME";

        private Column() {
        }
    }

    /**
     * SQL query definitions for the device token replay store.
     */
    public static final class Query {

        public static final String IS_TOKEN_REPLAYED =
                "SELECT 1 FROM IDN_DEVICE_TOKEN_JTI " +
                        "WHERE JTI = :JTI; AND TENANT_ID = :TENANT_ID;";

        public static final String STORE_TOKEN_JTI =
                "INSERT INTO IDN_DEVICE_TOKEN_JTI " +
                        "(JTI, TENANT_ID, IAT, EXPIRY_TIME) " +
                        "VALUES (:JTI;, :TENANT_ID;, :IAT;, :EXPIRY_TIME;)";

        // Removes records whose expiry time has passed. The store only ever holds a few minutes of tokens,
        // so a single standard DELETE (identical across all supported databases) is sufficient.
        public static final String DELETE_EXPIRED_TOKENS =
                "DELETE FROM IDN_DEVICE_TOKEN_JTI WHERE EXPIRY_TIME < :EXPIRY_TIME;";

        private Query() {
        }
    }
}
