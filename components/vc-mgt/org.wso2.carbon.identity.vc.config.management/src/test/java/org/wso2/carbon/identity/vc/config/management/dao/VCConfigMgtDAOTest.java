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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.vc.config.management.dao.impl.VCConfigMgtDAOImpl;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;

import java.util.ArrayList;
import java.util.List;

@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class VCConfigMgtDAOTest {

    private static final int TENANT_ID = -1234;
    private static final int INVALID_TENANT_ID = 9999;
    private static final int ATTACKER_TENANT_ID = 5678;
    private VCConfigMgtDAOImpl vcConfigMgtDAOImpl;

    @BeforeClass
    public void setUp() throws Exception {

        vcConfigMgtDAOImpl = new VCConfigMgtDAOImpl();

        List<String> claims = new ArrayList<>();
        claims.add("email");
        claims.add("given_name");
        addVCCredentialConfigurationToDB("EmployeeBadge", "jwt_vc_json", "employee_badge", claims, TENANT_ID);
        addVCCredentialConfigurationToDB("NIC", "jwt_vc_json", "vc_nic", claims, TENANT_ID);
    }

    @DataProvider
    public Object[][] getVCCredentialConfigurationsData() {
        return new Object[][]{
                {TENANT_ID, 2},
                {INVALID_TENANT_ID, 0},
        };
    }

    /**
     * Test retrieving VC credential configurations for different tenant scenarios.
     *
     * @param tenantId Tenant ID to test.
     * @param expectedCount Expected number of configurations.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "getVCCredentialConfigurationsData", priority = 1)
    public void testGetVCCredentialConfigurations(int tenantId, int expectedCount)
            throws Exception {

        List<VCCredentialConfiguration> configurations = vcConfigMgtDAOImpl.list(tenantId);

        Assert.assertNotNull(configurations, "Configurations list should not be null.");
        Assert.assertEquals(configurations.size(), expectedCount,
                "Expected " + expectedCount + " configurations for tenant " + tenantId);

        if (expectedCount > 0) {
            for (VCCredentialConfiguration config : configurations) {
                Assert.assertNotNull(config.getId(), "Configuration ID should not be null.");
                Assert.assertNotNull(config.getIdentifier(), "Configuration identifier should not be null.");
                Assert.assertNotNull(config.getDisplayName(), "Configuration display name should not be null.");
                Assert.assertNotNull(config.getScope(), "Configuration scope should not be null.");
            }
        }
    }

    @DataProvider
    public Object[][] getVCCredentialConfigurationByIdData() {
        return new Object[][]{
                {TENANT_ID, "EmployeeBadge", true},
                {TENANT_ID, "NIC", true},
                {INVALID_TENANT_ID, "EmployeeBadge", false},
                {TENANT_ID, "NonExistent", false},
        };
    }

    /**
     * Test retrieving a specific VC credential configuration by identifier.
     *
     * @param tenantId Tenant ID to test.
     * @param identifier Configuration identifier.
     * @param shouldExist Whether the configuration should exist.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "getVCCredentialConfigurationByIdData", priority = 2)
    public void testGetVCCredentialConfigurationByIdentifier(int tenantId, String identifier, boolean shouldExist)
            throws Exception {

        VCCredentialConfiguration config = vcConfigMgtDAOImpl.getByIdentifier(identifier, tenantId);

        if (shouldExist) {
            Assert.assertNotNull(config, "Configuration should exist for identifier: " + identifier);
            Assert.assertEquals(config.getIdentifier(), identifier,
                    "Configuration identifier should match: " + identifier);
            Assert.assertNotNull(config.getFormat(), "Configuration format should not be null.");
            Assert.assertNotNull(config.getScope(), "Configuration scope should not be null.");
            Assert.assertNotNull(config.getClaims(), "Configuration claims should not be null.");
        } else {
            Assert.assertNull(config, "Configuration should not exist for identifier: " + identifier +
                    " and tenant: " + tenantId);
        }
    }

    @DataProvider
    public Object[][] tenantIsolationAttackData() {
        return new Object[][]{
                {TENANT_ID, INVALID_TENANT_ID},
                {INVALID_TENANT_ID, TENANT_ID},
                {TENANT_ID, ATTACKER_TENANT_ID},
                {ATTACKER_TENANT_ID, TENANT_ID},
        };
    }

    /**
     * Security test: Validate tenant isolation to prevent cross-tenant access attacks.
     * This test ensures that a user from one tenant cannot access another tenant's
     * VC configuration even if they know the UUID (server-level unique ID).
     * Vulnerability: Broken Access Control - Cross-tenant data access.
     * Risk: If not properly validated, an attacker from one tenant could access
     * sensitive VC configurations from another tenant by guessing or discovering UUIDs.
     * Suggestion: Always validate tenant ID matches the configuration's tenant before
     * returning data.
     *
     * @param ownerTenantId The tenant ID that owns the configuration.
     * @param attackerTenantId The tenant ID of the attacker trying to access the config.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "tenantIsolationAttackData", priority = 5)
    public void testTenantIsolation(int ownerTenantId, int attackerTenantId)
            throws Exception {

        List<String> claims = new ArrayList<>();
        claims.add("email");
        claims.add("ssn");
        claims.add("salary");

        String uniqueIdentifier = "SensitiveConfig-" + ownerTenantId + "-" + System.currentTimeMillis();
        VCCredentialConfiguration ownerConfig = createVCCredentialConfiguration(
                uniqueIdentifier,
                "jwt_vc_json",
                "sensitive_scope",
                claims
        );

        vcConfigMgtDAOImpl.add(ownerConfig, ownerTenantId);

        VCCredentialConfiguration createdConfig = vcConfigMgtDAOImpl.getByIdentifier(uniqueIdentifier, ownerTenantId);
        Assert.assertNotNull(createdConfig, "Configuration should be created for owner tenant.");
        Assert.assertNotNull(createdConfig.getId(), "Configuration ID (UUID) should not be null.");

        String configUuid = createdConfig.getId();

        VCCredentialConfiguration attackerAccessedConfig = vcConfigMgtDAOImpl.get(configUuid, attackerTenantId);

        Assert.assertNull(attackerAccessedConfig,
                "SECURITY VIOLATION: Configuration from tenant " + ownerTenantId +
                " should NOT be accessible by tenant " + attackerTenantId +
                " even with valid UUID: " + configUuid);

        VCCredentialConfiguration ownerAccessedConfig = vcConfigMgtDAOImpl.get(configUuid, ownerTenantId);
        Assert.assertNotNull(ownerAccessedConfig,
                "Owner tenant " + ownerTenantId + " should be able to access their own configuration.");
        Assert.assertEquals(ownerAccessedConfig.getId(), configUuid,
                "Configuration UUID should match.");
        Assert.assertTrue(ownerAccessedConfig.getClaims().contains("ssn"),
                "Owner should have access to sensitive claims.");
    }

    @DataProvider
    public Object[][] addVCCredentialConfigurationData() {
        return new Object[][]{
                {"AddVCConfigTest-1", TENANT_ID, true},
                {"AddVCConfigTest-2", TENANT_ID, true},
                {"AddVCConfigTest-3", INVALID_TENANT_ID, true},
        };
    }

    /**
     * Test adding new VC credential configurations.
     *
     * @param postfix Postfix to append to configuration identifiers for uniqueness.
     * @param tenantId Tenant ID to test.
     * @param shouldSucceed Whether the operation should succeed.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "addVCCredentialConfigurationData", priority = 3)
    public void testAddVCCredentialConfiguration(String postfix, int tenantId, boolean shouldSucceed)
            throws Exception {

        List<String> claims = new ArrayList<>();
        claims.add("email");
        claims.add("name");
        String uniqueIdentifier = "TestConfig-" + postfix + "-" + System.currentTimeMillis();
        VCCredentialConfiguration config = createVCCredentialConfiguration(
                uniqueIdentifier,
                "jwt_vc_json",
                "test_scope_" + postfix,
                claims
        );

        VCCredentialConfiguration createdConfig  = vcConfigMgtDAOImpl.add(config, tenantId);

        if (shouldSucceed) {
            Assert.assertNotNull(createdConfig, "Created configuration should not be null.");
            Assert.assertTrue(createdConfig.getIdentifier().contains(postfix),
                    "Configuration identifier should contain postfix: " + postfix);
            Assert.assertNotNull(createdConfig.getId(), "Configuration ID should not be null.");
            Assert.assertEquals(createdConfig.getFormat(), "jwt_vc_json",
                    "Configuration format should match.");
            Assert.assertEquals(createdConfig.getScope(), "test_scope_" + postfix,
                    "Configuration scope should match.");
            Assert.assertNotNull(createdConfig.getClaims(), "Configuration claims should not be null.");
            Assert.assertTrue(createdConfig.getClaims().size() >= 2,
                    "Configuration should have at least 2 claims.");
        }
    }

    @DataProvider
    public Object[][] checkVCCredentialConfigurationExistsData() {
        return new Object[][]{
                {TENANT_ID, "EmployeeBadge", true},
                {TENANT_ID, "NIC", true},
                {TENANT_ID, "NonExistent", false},
                {INVALID_TENANT_ID, "EmployeeBadge", false},
        };
    }

    /**
     * Test checking if VC credential configurations exist by identifier.
     *
     * @param tenantId Tenant ID to test.
     * @param identifier Configuration identifier.
     * @param shouldExist Whether the configuration should exist.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "checkVCCredentialConfigurationExistsData", priority = 4)
    public void testVCCredentialConfigurationExists(int tenantId, String identifier, boolean shouldExist)
            throws Exception {

        boolean exists = vcConfigMgtDAOImpl.existsByIdentifier(identifier, tenantId);

        Assert.assertEquals(exists, shouldExist,
                "Configuration existence check failed for identifier: " + identifier +
                " and tenant: " + tenantId);
    }

    /**
     * Add VC Credential Configuration to the database.
     *
     * @param identifier Identifier.
     * @param format Format.
     * @param scope Scope.
     * @param claims Claims.
     * @param tenantId Tenant ID.
     * @throws Exception Error when adding to the database.
     */
    private void addVCCredentialConfigurationToDB(String identifier, String format, String scope,
                                                   List<String> claims, int tenantId)
            throws Exception {

        VCCredentialConfiguration config = createVCCredentialConfiguration(identifier, format, scope, claims);
        vcConfigMgtDAOImpl.add(config, tenantId);
    }

    /**
     * Create VC Credential Configuration.
     *
     * @param identifier Identifier.
     * @param format Format.
     * @param scope Scope.
     * @param claims Claims.
     * @return VCCredentialConfiguration.
     */
    private static VCCredentialConfiguration createVCCredentialConfiguration(String identifier, String format,
                                                                             String scope, List<String> claims) {

        VCCredentialConfiguration vcCredentialConfiguration = new VCCredentialConfiguration();
        vcCredentialConfiguration.setIdentifier(identifier);
        vcCredentialConfiguration.setDisplayName(identifier);
        vcCredentialConfiguration.setType(identifier);
        vcCredentialConfiguration.setFormat(format);
        vcCredentialConfiguration.setSigningAlgorithm("RS256");
        vcCredentialConfiguration.setExpiresIn(3600);
        vcCredentialConfiguration.setScope(scope);
        vcCredentialConfiguration.setClaims(claims);
        return vcCredentialConfiguration;
    }
}
