/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.vc.config.management.dao;

/**
 * SQL queries used by VC Config DAO.
 */
public final class SQLQueries {

    private SQLQueries() {}

    // VC_CONFIG
    public static final String LIST_CONFIGS =
            "SELECT ID, IDENTIFIER, CONFIGURATION_ID, SCOPE " +
            "FROM VC_CONFIG WHERE TENANT_ID = ? ORDER BY CURSOR_KEY";

    public static final String GET_CONFIG_BY_ID =
            "SELECT ID, IDENTIFIER, CONFIGURATION_ID, SCOPE, FORMAT, SIGNING_ALG, " +
            "TYPE, METADATA, EXPIRY_IN " +
            "FROM VC_CONFIG WHERE TENANT_ID = ? AND ID = ?";

    public static final String GET_CONFIG_BY_CONFIG_ID =
            "SELECT ID, IDENTIFIER, CONFIGURATION_ID, SCOPE, FORMAT, SIGNING_ALG, " +
            "TYPE, METADATA, EXPIRY_IN " +
            "FROM VC_CONFIG WHERE TENANT_ID = ? AND CONFIGURATION_ID = ?";

    public static final String EXISTS_BY_IDENTIFIER =
            "SELECT 1 FROM VC_CONFIG WHERE TENANT_ID = ? AND IDENTIFIER = ?";

    public static final String EXISTS_BY_CONFIGURATION_ID =
            "SELECT 1 FROM VC_CONFIG WHERE TENANT_ID = ? AND CONFIGURATION_ID = ?";

    public static final String INSERT_CONFIG =
            "INSERT INTO VC_CONFIG (ID, TENANT_ID, IDENTIFIER, CONFIGURATION_ID, SCOPE, FORMAT, " +
            "SIGNING_ALG, TYPE, METADATA, EXPIRY_IN) VALUES " +
            "(?,?,?,?,?,?,?,?,?,?)";

    public static final String UPDATE_CONFIG =
            "UPDATE VC_CONFIG SET IDENTIFIER = ?, CONFIGURATION_ID = ?, SCOPE = ?, FORMAT = ?, " +
            "SIGNING_ALG = ?, TYPE = ?, METADATA = ?, EXPIRY_IN = ? " +
            "WHERE TENANT_ID = ? AND ID = ?";

    public static final String DELETE_CONFIG =
            "DELETE FROM VC_CONFIG WHERE TENANT_ID = ? AND ID = ?";

    // VC_CONFIG_CLAIM
    public static final String LIST_CLAIMS_BY_CONFIG_PK =
            "SELECT CLAIM_URI FROM VC_CONFIG_CLAIM WHERE CONFIG_ID = ?";

    public static final String INSERT_CLAIM =
            "INSERT INTO VC_CONFIG_CLAIM (CONFIG_ID, CLAIM_URI) VALUES (?,?)";

    public static final String DELETE_CLAIMS_BY_CONFIG_PK =
            "DELETE FROM VC_CONFIG_CLAIM WHERE CONFIG_ID = ?";
}
