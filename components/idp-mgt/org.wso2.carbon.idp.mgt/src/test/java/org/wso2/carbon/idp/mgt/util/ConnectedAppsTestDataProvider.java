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

package org.wso2.carbon.idp.mgt.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Test utility class for creating connected applications test data.
 * This class provides helper methods to set up test data for testing
 * the getConnectedApplications functionality with filters.
 */
public class ConnectedAppsTestDataProvider {

    /**
     * Adds test data for connected applications.
     * Creates service provider apps and links them to identity providers through authentication steps
     * and provisioning connectors.
     *
     * @param connection     The database connection
     * @param sampleTenantId The tenant ID to use for test data
     * @throws Exception If an error occurs while setting up test data
     */
    public static void addConnectedApplicationsTestData(Connection connection, int sampleTenantId) throws Exception {

        String insertAppSql = "INSERT INTO SP_APP (TENANT_ID, APP_NAME, USER_STORE, USERNAME, DESCRIPTION, " +
                "AUTH_TYPE, UUID) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(insertAppSql)) {
            ps.setInt(1, sampleTenantId);
            ps.setString(2, "TestApp1");
            ps.setString(3, "PRIMARY");
            ps.setString(4, "admin");
            ps.setString(5, "Test Application 1");
            ps.setString(6, "default");
            ps.setString(7, "test-app-uuid-1");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(insertAppSql)) {
            ps.setInt(1, sampleTenantId);
            ps.setString(2, "TestApp2");
            ps.setString(3, "PRIMARY");
            ps.setString(4, "admin");
            ps.setString(5, "Test Application 2");
            ps.setString(6, "default");
            ps.setString(7, "test-app-uuid-2");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(insertAppSql)) {
            ps.setInt(1, sampleTenantId);
            ps.setString(2, "App3");
            ps.setString(3, "PRIMARY");
            ps.setString(4, "admin");
            ps.setString(5, "Application 3");
            ps.setString(6, "default");
            ps.setString(7, "test-app-uuid-3");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(insertAppSql)) {
            ps.setInt(1, sampleTenantId);
            ps.setString(2, "TestApp4");
            ps.setString(3, "PRIMARY");
            ps.setString(4, "admin");
            ps.setString(5, "Test Application 4");
            ps.setString(6, "default");
            ps.setString(7, "test-app-uuid-4");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(insertAppSql)) {
            ps.setInt(1, sampleTenantId);
            ps.setString(2, "ProvisioningApp");
            ps.setString(3, "PRIMARY");
            ps.setString(4, "admin");
            ps.setString(5, "Provisioning Application");
            ps.setString(6, "default");
            ps.setString(7, "test-app-uuid-5");
            ps.executeUpdate();
        }

        int idp1Id = getIdpId(connection, "testIdP1", sampleTenantId);
        int idp2Id = getIdpId(connection, "testIdP2", sampleTenantId);

        int authenticator1Id = getAuthenticatorId(connection, idp1Id, sampleTenantId);
        int authenticator2Id = getAuthenticatorId(connection, idp2Id, sampleTenantId);

        int app1Id = getAppId(connection, "TestApp1", sampleTenantId);
        int app2Id = getAppId(connection, "TestApp2", sampleTenantId);
        int app3Id = getAppId(connection, "App3", sampleTenantId);
        int app4Id = getAppId(connection, "TestApp4", sampleTenantId);
        int app5Id = getAppId(connection, "ProvisioningApp", sampleTenantId);

        int authStep1Id = insertAuthStep(connection, sampleTenantId, 1, app1Id);
        int authStep2Id = insertAuthStep(connection, sampleTenantId, 1, app2Id);
        int authStep3Id = insertAuthStep(connection, sampleTenantId, 1, app3Id);
        int authStep4Id = insertAuthStep(connection, sampleTenantId, 1, app4Id);

        String insertFederatedIdpSql = "INSERT INTO SP_FEDERATED_IDP (ID, TENANT_ID, AUTHENTICATOR_ID) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(insertFederatedIdpSql)) {
            ps.setInt(1, authStep1Id);
            ps.setInt(2, sampleTenantId);
            ps.setInt(3, authenticator1Id);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(insertFederatedIdpSql)) {
            ps.setInt(1, authStep2Id);
            ps.setInt(2, sampleTenantId);
            ps.setInt(3, authenticator1Id);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(insertFederatedIdpSql)) {
            ps.setInt(1, authStep3Id);
            ps.setInt(2, sampleTenantId);
            ps.setInt(3, authenticator1Id);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = connection.prepareStatement(insertFederatedIdpSql)) {
            ps.setInt(1, authStep4Id);
            ps.setInt(2, sampleTenantId);
            ps.setInt(3, authenticator2Id);
            ps.executeUpdate();
        }

        String insertProvisioningSql = "INSERT INTO SP_PROVISIONING_CONNECTOR (TENANT_ID, IDP_NAME, " +
                "CONNECTOR_NAME, APP_ID, IS_JIT_ENABLED, BLOCKING, RULE_ENABLED) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(insertProvisioningSql)) {
            ps.setInt(1, sampleTenantId);
            ps.setString(2, "testIdP1");
            ps.setString(3, "scim");
            ps.setInt(4, app5Id);
            ps.setString(5, "0");
            ps.setString(6, "0");
            ps.setString(7, "0");
            ps.executeUpdate();
        }

        connection.commit();
    }

    /**
     * Gets the IDP ID for a given IDP name and tenant ID.
     *
     * @param connection The database connection
     * @param idpName    The name of the IDP
     * @param tenantId   The tenant ID
     * @return The IDP ID
     * @throws SQLException If the IDP is not found or a database error occurs
     */
    private static int getIdpId(Connection connection, String idpName, int tenantId) throws SQLException {

        String query = "SELECT ID FROM IDP WHERE NAME = ? AND TENANT_ID = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, idpName);
            ps.setInt(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }
        }
        throw new SQLException("IDP not found: " + idpName);
    }

    /**
     * Gets the authenticator ID for a given IDP ID.
     * If no authenticator exists, creates a default one for testing.
     *
     * @param connection The database connection
     * @param idpId      The IDP ID
     * @param tenantId   The tenant ID
     * @return The authenticator ID
     * @throws SQLException If a database error occurs
     */
    private static int getAuthenticatorId(Connection connection, int idpId, int tenantId) throws SQLException {

        String query = "SELECT ID FROM IDP_AUTHENTICATOR WHERE IDP_ID = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idpId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }
        }
        // If no authenticator found, create a default one for testing.
        String insertSql = "INSERT INTO IDP_AUTHENTICATOR (TENANT_ID, IDP_ID, NAME, IS_ENABLED, DISPLAY_NAME, " +
                "DEFINED_BY, AUTHENTICATION_TYPE) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tenantId);
            ps.setInt(2, idpId);
            ps.setString(3, "DefaultAuthenticator");
            ps.setString(4, "1");
            ps.setString(5, "Default Authenticator");
            ps.setString(6, "SYSTEM");
            ps.setString(7, "IDENTIFICATION");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to get or create authenticator for IDP ID: " + idpId);
    }

    /**
     * Gets the application ID for a given application name and tenant ID.
     *
     * @param connection The database connection
     * @param appName    The name of the application
     * @param tenantId   The tenant ID
     * @return The application ID
     * @throws SQLException If the application is not found or a database error occurs
     */
    private static int getAppId(Connection connection, String appName, int tenantId) throws SQLException {

        String query = "SELECT ID FROM SP_APP WHERE APP_NAME = ? AND TENANT_ID = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, appName);
            ps.setInt(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID");
                }
            }
        }
        throw new SQLException("App not found: " + appName);
    }

    /**
     * Inserts an authentication step for an application.
     *
     * @param connection The database connection
     * @param tenantId   The tenant ID
     * @param stepOrder  The order of the authentication step
     * @param appId      The application ID
     * @return The authentication step ID
     * @throws SQLException If a database error occurs
     */
    private static int insertAuthStep(Connection connection, int tenantId, int stepOrder, int appId)
            throws SQLException {

        String sql = "INSERT INTO SP_AUTH_STEP (TENANT_ID, STEP_ORDER, APP_ID) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tenantId);
            ps.setInt(2, stepOrder);
            ps.setInt(3, appId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert auth step");
    }
}
