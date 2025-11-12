package org.wso2.carbon.identity.vc.config.management.dao;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.vc.config.management.dao.impl.VCConfigMgtDAOImpl;
import org.wso2.carbon.identity.vc.config.management.dao.impl.VCOfferDAOImpl;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;
import org.wso2.carbon.identity.vc.config.management.model.VCOffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class VCOfferDAOTest {

    private static final int TENANT_ID = -1234;
    private static final int INVALID_TENANT_ID = 9999;
    private static final int ATTACKER_TENANT_ID = 5678;
    private VCOfferDAOImpl vcOfferDAOImpl;
    private VCConfigMgtDAOImpl vcConfigMgtDAOImpl;

    // Store created credential configuration ids for use in tests.
    private String config1Id;
    private String config2Id;
    private String config3Id;

    // Store created offer IDs for use in tests.
    private String employeeOfferId;
    private String citizenOfferId;

    @BeforeClass
    public void setUp() throws Exception {

        vcOfferDAOImpl = new VCOfferDAOImpl();
        vcConfigMgtDAOImpl = new VCConfigMgtDAOImpl();

        // Step 1: Create VC Credential Configurations that offers will reference.
        List<String> claims1 = Arrays.asList("email", "name", "employee_id");
        VCCredentialConfiguration config1 = createVCCredentialConfiguration(
                "EmployeeBadgeConfig", "jwt_vc_json", "employee_badge", claims1);
        VCCredentialConfiguration createdConfig1 = vcConfigMgtDAOImpl.add(config1, TENANT_ID);
        config1Id = createdConfig1.getId();

        List<String> claims2 = Arrays.asList("email", "name", "department");
        VCCredentialConfiguration config2 = createVCCredentialConfiguration(
                "DepartmentConfig", "jwt_vc_json", "department_access", claims2);
        VCCredentialConfiguration createdConfig2 = vcConfigMgtDAOImpl.add(config2, TENANT_ID);
        config2Id = createdConfig2.getId();

        List<String> claims3 = Arrays.asList("email", "name", "citizen_id");
        VCCredentialConfiguration config3 = createVCCredentialConfiguration(
                "CitizenIDConfig", "jwt_vc_json", "citizen_id", claims3);
        VCCredentialConfiguration createdConfig3 = vcConfigMgtDAOImpl.add(config3, TENANT_ID);
        config3Id = createdConfig3.getId();

        // Step 2: Create offers that reference the credential configurations.
        VCOffer employeeOffer = addVCOfferToDB("EmployeeOffer", Arrays.asList(config1Id, config2Id), TENANT_ID);
        employeeOfferId = employeeOffer.getOfferId();

        VCOffer citizenOffer = addVCOfferToDB("CitizenOffer", Collections.singletonList(config3Id), TENANT_ID);
        citizenOfferId = citizenOffer.getOfferId();
    }

    @DataProvider
    public Object[][] getVCOffersData() {
        return new Object[][]{
                // Valid tenant with 2 offers.
                {TENANT_ID, 2},
                // Invalid tenant with no offers.
                {INVALID_TENANT_ID, 0},
        };
    }

    /**
     * Test retrieving VC offers for different tenant scenarios.
     *
     * @param tenantId Tenant ID to test.
     * @param expectedCount Expected number of offers.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "getVCOffersData", priority = 1)
    public void testGetVCOffers(int tenantId, int expectedCount) throws Exception {

        List<VCOffer> offers = vcOfferDAOImpl.list(tenantId);

        Assert.assertNotNull(offers, "Offers list should not be null.");
        Assert.assertEquals(offers.size(), expectedCount,
                "Expected " + expectedCount + " offers for tenant " + tenantId);

        // Validate offer properties for valid tenant.
        if (expectedCount > 0) {
            for (VCOffer offer : offers) {
                Assert.assertNotNull(offer.getOfferId(), "Offer ID should not be null.");
                Assert.assertNotNull(offer.getDisplayName(), "Offer display name should not be null.");
                Assert.assertNotNull(offer.getCredentialConfigurationIds(),
                        "Credential configuration IDs should not be null.");
            }
        }
    }

    @DataProvider
    public Object[][] getVCOfferByIdData() {
        return new Object[][]{
                // Valid tenant and offer ID - should return offer.
                {TENANT_ID, employeeOfferId, true},
                // Valid tenant and different offer ID - should return offer.
                {TENANT_ID, citizenOfferId, true},
                // Invalid tenant - should return null.
                {INVALID_TENANT_ID, employeeOfferId, false},
                // Valid tenant but non-existent offer ID - should return null.
                {TENANT_ID, "non-existent-uuid-12345", false},
        };
    }

    /**
     * Test retrieving a specific VC offer by offer ID.
     *
     * @param tenantId Tenant ID to test.
     * @param offerId Offer ID.
     * @param shouldExist Whether the offer should exist.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "getVCOfferByIdData", priority = 2)
    public void testGetVCOfferById(int tenantId, String offerId, boolean shouldExist) throws Exception {

        VCOffer offer = vcOfferDAOImpl.get(offerId, tenantId);

        if (shouldExist) {
            Assert.assertNotNull(offer, "Offer should exist for offer ID: " + offerId);
            Assert.assertNotNull(offer.getDisplayName(), "Offer display name should not be null.");
            Assert.assertEquals(offer.getOfferId(), offerId, "Offer ID should match.");
            Assert.assertNotNull(offer.getCredentialConfigurationIds(),
                    "Credential configuration IDs should not be null.");
            Assert.assertFalse(offer.getCredentialConfigurationIds().isEmpty(),
                    "Offer should have at least one credential configuration.");
        } else {
            Assert.assertNull(offer, "Offer should not exist for offer ID: " + offerId +
                    " and tenant: " + tenantId);
        }
    }

    @DataProvider
    public Object[][] tenantIsolationAttackData() {
        return new Object[][]{
                // Attacker from INVALID_TENANT_ID tries to access TENANT_ID's offer - should fail.
                {TENANT_ID, INVALID_TENANT_ID},
                // Attacker from different tenant tries to access another tenant's offer - should fail.
                {INVALID_TENANT_ID, TENANT_ID},
                // Attacker tenant tries to access owner tenant's offer - should fail.
                {TENANT_ID, ATTACKER_TENANT_ID},
                // Reverse scenario - owner tries to access attacker tenant's offer - should fail.
                {ATTACKER_TENANT_ID, TENANT_ID},
        };
    }

    /**
     * Security test: Validate tenant isolation to prevent cross-tenant access attacks.
     * This test ensures that a user from one tenant cannot access another tenant's
     * VC offer even if they know the offer ID (server-level unique ID).
     * Vulnerability: Broken Access Control - Cross-tenant data access.
     * Risk: If not properly validated, an attacker from one tenant could access
     * sensitive VC offers from another tenant by guessing or discovering offer IDs.
     * Suggestion: Always validate tenant ID matches the offer's tenant before
     * returning data.
     *
     * @param ownerTenantId The tenant ID that owns the offer.
     * @param attackerTenantId The tenant ID of the attacker trying to access the offer.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "tenantIsolationAttackData", priority = 5)
    public void testTenantIsolation(int ownerTenantId, int attackerTenantId) throws Exception {

        // Step 1: Create credential configurations for the owner tenant.
        String uniqueConfigId1 = "SensitiveConfig1-" + ownerTenantId + "-" + System.currentTimeMillis();
        String uniqueConfigId2 = "SensitiveConfig2-" + ownerTenantId + "-" + System.currentTimeMillis();

        List<String> sensitiveClaims = Arrays.asList("email", "ssn", "salary");
        VCCredentialConfiguration sensitiveConfig1 = createVCCredentialConfiguration(
                uniqueConfigId1, "jwt_vc_json", "sensitive_scope_1", sensitiveClaims);
        VCCredentialConfiguration createdConfig1 = vcConfigMgtDAOImpl.add(sensitiveConfig1, ownerTenantId);

        VCCredentialConfiguration sensitiveConfig2 = createVCCredentialConfiguration(
                uniqueConfigId2, "jwt_vc_json", "sensitive_scope_2", sensitiveClaims);
        VCCredentialConfiguration createdConfig2 = vcConfigMgtDAOImpl.add(sensitiveConfig2, ownerTenantId);

        // Step 2: Create a VC offer for the owner tenant referencing the configurations.
        String uniqueOfferId = "SensitiveOffer-" + ownerTenantId + "-" + System.currentTimeMillis();
        List<String> configIds = Arrays.asList(createdConfig1.getId(), createdConfig2.getId());
        VCOffer ownerOffer = createVCOffer(uniqueOfferId, configIds);

        VCOffer createdOffer = vcOfferDAOImpl.add(ownerOffer, ownerTenantId);
        Assert.assertNotNull(createdOffer, "Offer should be created for owner tenant.");
        Assert.assertNotNull(createdOffer.getOfferId(), "Offer ID should not be null.");

        String offerId = createdOffer.getOfferId();

        // Step 3: Attacker from different tenant tries to access using the offer ID.
        VCOffer attackerAccessedOffer = vcOfferDAOImpl.get(offerId, attackerTenantId);

        // Step 4: Security assertion - attacker should NOT be able to access the offer.
        Assert.assertNull(attackerAccessedOffer,
                "SECURITY VIOLATION: Offer from tenant " + ownerTenantId +
                " should NOT be accessible by tenant " + attackerTenantId +
                " even with valid offer ID: " + offerId);

        // Step 5: Verify owner tenant can still access their own offer.
        VCOffer ownerAccessedOffer = vcOfferDAOImpl.get(offerId, ownerTenantId);
        Assert.assertNotNull(ownerAccessedOffer,
                "Owner tenant " + ownerTenantId + " should be able to access their own offer.");
        Assert.assertEquals(ownerAccessedOffer.getOfferId(), offerId, "Offer ID should match.");
        Assert.assertTrue(ownerAccessedOffer.getCredentialConfigurationIds().contains(createdConfig1.getId()),
                "Owner should have access to sensitive credential configurations.");
    }

    @DataProvider
    public Object[][] addVCOfferData() {
        return new Object[][]{
                // Valid tenant - should successfully add offer.
                {"AddOfferTest-1", TENANT_ID, true},
                // Valid tenant with different name - should successfully add offer.
                {"AddOfferTest-2", TENANT_ID, true},
                // Invalid tenant - should still add but to invalid tenant.
                {"AddOfferTest-3", INVALID_TENANT_ID, true},
        };
    }

    /**
     * Test adding new VC offers.
     *
     * @param displayName Display name for offer.
     * @param tenantId Tenant ID to test.
     * @param shouldSucceed Whether the operation should succeed.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "addVCOfferData", priority = 3)
    public void testAddVCOffer(String displayName, int tenantId, boolean shouldSucceed) throws Exception {

        // Step 1: Create credential configurations first.
        String uniqueConfigId1 = "TestConfig1-" + System.currentTimeMillis();
        String uniqueConfigId2 = "TestConfig2-" + System.currentTimeMillis();

        List<String> claims = Arrays.asList("email", "name");
        VCCredentialConfiguration testConfig1 = createVCCredentialConfiguration(
                uniqueConfigId1, "jwt_vc_json", "test_scope_1", claims);
        VCCredentialConfiguration createdConfig1 = vcConfigMgtDAOImpl.add(testConfig1, tenantId);

        VCCredentialConfiguration testConfig2 = createVCCredentialConfiguration(
                uniqueConfigId2, "jwt_vc_json", "test_scope_2", claims);
        VCCredentialConfiguration createdConfig2 = vcConfigMgtDAOImpl.add(testConfig2, tenantId);

        // Step 2: Create offer referencing the credential configurations.
        String uniqueDisplayName = displayName + "-" + System.currentTimeMillis();
        List<String> configIds = Arrays.asList(createdConfig1.getId(), createdConfig2.getId());
        VCOffer offer = createVCOffer(uniqueDisplayName, configIds);

        VCOffer createdOffer = vcOfferDAOImpl.add(offer, tenantId);

        if (shouldSucceed) {
            Assert.assertNotNull(createdOffer, "Created offer should not be null.");
            Assert.assertTrue(createdOffer.getDisplayName().contains(displayName),
                    "Offer display name should contain: " + displayName);
            Assert.assertNotNull(createdOffer.getOfferId(), "Offer ID should not be null.");
            Assert.assertNotNull(createdOffer.getCredentialConfigurationIds(),
                    "Credential configuration IDs should not be null.");
            Assert.assertEquals(createdOffer.getCredentialConfigurationIds().size(), 2,
                    "Offer should have 2 credential configurations.");
            Assert.assertTrue(createdOffer.getCredentialConfigurationIds().contains(createdConfig1.getId()),
                    "Offer should contain first credential configuration.");
            Assert.assertTrue(createdOffer.getCredentialConfigurationIds().contains(createdConfig2.getId()),
                    "Offer should contain second credential configuration.");
        }
    }

    @DataProvider
    public Object[][] checkVCOfferExistsData() {
        return new Object[][]{
                // Existing offer with valid tenant - should return true.
                {TENANT_ID, employeeOfferId, true},
                // Existing offer with different ID - should return true.
                {TENANT_ID, citizenOfferId, true},
                // Non-existing offer - should return false.
                {TENANT_ID, "non-existent-uuid-67890", false},
                // Existing offer with invalid tenant - should return false.
                {INVALID_TENANT_ID, employeeOfferId, false},
        };
    }

    /**
     * Test checking if VC offers exist by offer ID.
     *
     * @param tenantId Tenant ID to test.
     * @param offerId Offer ID.
     * @param shouldExist Whether the offer should exist.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "checkVCOfferExistsData", priority = 4)
    public void testVCOfferExists(int tenantId, String offerId, boolean shouldExist) throws Exception {

        boolean exists = vcOfferDAOImpl.existsByOfferId(offerId, tenantId);

        Assert.assertEquals(exists, shouldExist,
                "Offer existence check failed for offer ID: " + offerId + " and tenant: " + tenantId);
    }

    @DataProvider
    public Object[][] updateVCOfferData() {
        return new Object[][]{
                // Valid tenant - should successfully update offer.
                {"UpdateOfferTest-1", TENANT_ID, true},
                // Valid tenant with different name - should successfully update offer.
                {"UpdateOfferTest-2", TENANT_ID, true},
        };
    }

    /**
     * Test updating existing VC offers.
     *
     * @param displayName Display name for offer.
     * @param tenantId Tenant ID to test.
     * @param shouldSucceed Whether the operation should succeed.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "updateVCOfferData", priority = 6)
    public void testUpdateVCOffer(String displayName, int tenantId, boolean shouldSucceed) throws Exception {

        // Step 1: Create an offer first.
        String uniqueDisplayName = displayName + "-" + System.currentTimeMillis();
        List<String> initialConfigIds = Arrays.asList("initial_config_1");
        VCOffer initialOffer = createVCOffer(uniqueDisplayName, initialConfigIds);

        VCOffer createdOffer = vcOfferDAOImpl.add(initialOffer, tenantId);
        Assert.assertNotNull(createdOffer, "Initial offer should be created.");
        String offerId = createdOffer.getOfferId();

        // Step 2: Update the offer.
        String updatedDisplayName = "Updated-" + uniqueDisplayName;
        List<String> updatedConfigIds = Arrays.asList("updated_config_1", "updated_config_2", "updated_config_3");
        VCOffer updatePayload = createVCOffer(updatedDisplayName, updatedConfigIds);

        VCOffer updatedOffer = vcOfferDAOImpl.update(offerId, updatePayload, tenantId);

        if (shouldSucceed) {
            Assert.assertNotNull(updatedOffer, "Updated offer should not be null.");
            Assert.assertEquals(updatedOffer.getOfferId(), offerId, "Offer ID should remain the same.");
            Assert.assertEquals(updatedOffer.getDisplayName(), updatedDisplayName,
                    "Display name should be updated.");
            Assert.assertEquals(updatedOffer.getCredentialConfigurationIds().size(), 3,
                    "Should have 3 credential configurations after update.");
            Assert.assertTrue(updatedOffer.getCredentialConfigurationIds().contains("updated_config_2"),
                    "Should contain updated configuration IDs.");
        }
    }

    @DataProvider
    public Object[][] deleteVCOfferData() {
        return new Object[][]{
                // Valid tenant - should successfully delete offer.
                {"DeleteOfferTest-1", TENANT_ID, true},
                // Valid tenant with different name - should successfully delete offer.
                {"DeleteOfferTest-2", TENANT_ID, true},
        };
    }

    /**
     * Test deleting VC offers.
     *
     * @param displayName Display name for offer.
     * @param tenantId Tenant ID to test.
     * @param shouldSucceed Whether the operation should succeed.
     * @throws Exception If an error occurs during test execution.
     */
    @Test(dataProvider = "deleteVCOfferData", priority = 7)
    public void testDeleteVCOffer(String displayName, int tenantId, boolean shouldSucceed) throws Exception {

        // Step 1: Create an offer first.
        String uniqueDisplayName = displayName + "-" + System.currentTimeMillis();
        List<String> configIds = Arrays.asList("delete_config_1");
        VCOffer offer = createVCOffer(uniqueDisplayName, configIds);

        VCOffer createdOffer = vcOfferDAOImpl.add(offer, tenantId);
        Assert.assertNotNull(createdOffer, "Offer should be created.");
        String offerId = createdOffer.getOfferId();

        // Step 2: Verify the offer exists.
        boolean existsBefore = vcOfferDAOImpl.existsByOfferId(offerId, tenantId);
        Assert.assertTrue(existsBefore, "Offer should exist before deletion.");

        // Step 3: Delete the offer.
        vcOfferDAOImpl.delete(offerId, tenantId);

        // Step 4: Verify the offer no longer exists.
        if (shouldSucceed) {
            boolean existsAfter = vcOfferDAOImpl.existsByOfferId(offerId, tenantId);
            Assert.assertFalse(existsAfter, "Offer should not exist after deletion.");

            VCOffer deletedOffer = vcOfferDAOImpl.get(offerId, tenantId);
            Assert.assertNull(deletedOffer, "Should not be able to retrieve deleted offer.");
        }
    }

    /**
     * Add VC Offer to the database.
     *
     * @param displayName Display name.
     * @param credentialConfigurationIds Credential configuration IDs.
     * @param tenantId Tenant ID.
     * @return Created VCOffer with generated offer ID.
     * @throws Exception Error when adding to the database.
     */
    private VCOffer addVCOfferToDB(String displayName, List<String> credentialConfigurationIds, int tenantId)
            throws Exception {

        VCOffer offer = createVCOffer(displayName, credentialConfigurationIds);
        return vcOfferDAOImpl.add(offer, tenantId);
    }

    /**
     * Create VC Offer.
     *
     * @param displayName Display name.
     * @param credentialConfigurationIds Credential configuration IDs.
     * @return VCOffer.
     */
    private static VCOffer createVCOffer(String displayName, List<String> credentialConfigurationIds) {

        VCOffer offer = new VCOffer();
        offer.setDisplayName(displayName);
        offer.setCredentialConfigurationIds(new ArrayList<>(credentialConfigurationIds));
        return offer;
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
        vcCredentialConfiguration.setMetadata(new VCCredentialConfiguration.Metadata());
        vcCredentialConfiguration.setScope(scope);
        vcCredentialConfiguration.setClaims(new ArrayList<>(claims));
        return vcCredentialConfiguration;
    }
}
