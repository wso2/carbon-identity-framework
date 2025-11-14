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
            "SELECT ID, IDENTIFIER, DISPLAY_NAME, SCOPE " +
            "FROM VC_CONFIG WHERE TENANT_ID = ? ORDER BY CURSOR_KEY";

    public static final String GET_CONFIG_BY_ID =
            "SELECT ID, IDENTIFIER, DISPLAY_NAME, SCOPE, FORMAT, SIGNING_ALG, " +
            "TYPE, METADATA, EXPIRES_IN " +
            "FROM VC_CONFIG WHERE TENANT_ID = ? AND ID = ?";

    public static final String GET_CONFIG_BY_IDENTIFIER =
            "SELECT ID, IDENTIFIER, DISPLAY_NAME, SCOPE, FORMAT, SIGNING_ALG, " +
            "TYPE, METADATA, EXPIRES_IN " +
            "FROM VC_CONFIG WHERE TENANT_ID = ? AND IDENTIFIER = ?";

    public static final String EXISTS_BY_IDENTIFIER =
            "SELECT 1 FROM VC_CONFIG WHERE TENANT_ID = ? AND IDENTIFIER = ?";

    public static final String INSERT_CONFIG =
            "INSERT INTO VC_CONFIG (ID, TENANT_ID, IDENTIFIER, DISPLAY_NAME, SCOPE, FORMAT, " +
            "SIGNING_ALG, TYPE, METADATA, EXPIRES_IN) VALUES " +
            "(?,?,?,?,?,?,?,?,?,?)";

    public static final String UPDATE_CONFIG =
            "UPDATE VC_CONFIG SET IDENTIFIER = ?, DISPLAY_NAME = ?, SCOPE = ?, FORMAT = ?, " +
            "SIGNING_ALG = ?, TYPE = ?, METADATA = ?, EXPIRES_IN = ? " +
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

    // VC_OFFER
    public static final String LIST_OFFERS =
            "SELECT OFFER_ID, DISPLAY_NAME FROM VC_OFFER WHERE TENANT_ID = ? ORDER BY CURSOR_KEY";

    public static final String GET_OFFER_BY_ID =
            "SELECT OFFER_ID, DISPLAY_NAME FROM VC_OFFER WHERE TENANT_ID = ? AND OFFER_ID = ?";

    public static final String EXISTS_OFFER_BY_ID =
            "SELECT 1 FROM VC_OFFER WHERE TENANT_ID = ? AND OFFER_ID = ?";

    public static final String INSERT_OFFER =
            "INSERT INTO VC_OFFER (OFFER_ID, TENANT_ID, DISPLAY_NAME) VALUES (?,?,?)";

    public static final String UPDATE_OFFER =
            "UPDATE VC_OFFER SET DISPLAY_NAME = ? WHERE TENANT_ID = ? AND OFFER_ID = ?";

    public static final String DELETE_OFFER =
            "DELETE FROM VC_OFFER WHERE TENANT_ID = ? AND OFFER_ID = ?";

    // VC_OFFER_CREDENTIAL_CONFIG
    public static final String LIST_CREDENTIAL_CONFIGS_BY_OFFER_ID =
            "SELECT CONFIG_ID FROM VC_OFFER_CREDENTIAL_CONFIG WHERE OFFER_ID = ? AND TENANT_ID = ?";

    public static final String INSERT_OFFER_CREDENTIAL_CONFIG =
            "INSERT INTO VC_OFFER_CREDENTIAL_CONFIG (OFFER_ID, CONFIG_ID, TENANT_ID) VALUES (?,?,?)";

    public static final String DELETE_OFFER_CREDENTIAL_CONFIGS_BY_OFFER_ID =
            "DELETE FROM VC_OFFER_CREDENTIAL_CONFIG WHERE OFFER_ID = ? AND TENANT_ID = ?";
}
