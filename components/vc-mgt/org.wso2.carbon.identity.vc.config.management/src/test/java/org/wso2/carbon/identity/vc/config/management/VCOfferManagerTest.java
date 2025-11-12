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

package org.wso2.carbon.identity.vc.config.management;

import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtClientException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;
import org.wso2.carbon.identity.vc.config.management.model.VCOffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test class for VCOfferManagerImpl.
 * Tests service layer business logic for VC Offer management.
 */
@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class VCOfferManagerTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String TEST_DISPLAY_NAME = "Employee Credentials Offer";

    private VCOfferManager offerManager;
    private VCCredentialConfigManager configManager;
    private VCOffer sampleOffer;

    // Store credential configuration IDs for testing.
    private String config1Id;
    private String config2Id;
    private String config3Id;

    @BeforeClass
    public void setUpClass() throws VCConfigMgtException {

        offerManager = VCOfferManagerImpl.getInstance();
        configManager = VCCredentialConfigManagerImpl.getInstance();

        // Create credential configurations first (offers reference these).
        config1Id = createCredentialConfiguration("EmployeeBadge-" + System.currentTimeMillis(),
                "Employee Badge").getId();
        config2Id = createCredentialConfiguration("DepartmentAccess-" + System.currentTimeMillis(),
                "Department Access").getId();
        config3Id = createCredentialConfiguration("CitizenID-" + System.currentTimeMillis(),
                "Citizen ID").getId();
    }

    @Test(priority = 1)
    public void testAddOffer() throws VCConfigMgtException {

        VCOffer creatingOffer = createSampleOffer(
                TEST_DISPLAY_NAME,
                Arrays.asList(config1Id, config2Id)
        );

        sampleOffer = offerManager.add(creatingOffer, TENANT_DOMAIN);

        Assert.assertNotNull(sampleOffer.getOfferId(), "Offer ID should not be null.");
        Assert.assertEquals(sampleOffer.getDisplayName(), TEST_DISPLAY_NAME,
                "Display name should match.");
        Assert.assertNotNull(sampleOffer.getCredentialConfigurationIds(),
                "Credential configuration IDs should not be null.");
        Assert.assertEquals(sampleOffer.getCredentialConfigurationIds().size(), 2,
                "Should have 2 credential configurations.");
        Assert.assertTrue(sampleOffer.getCredentialConfigurationIds().contains(config1Id),
                "Should contain first credential configuration.");
        Assert.assertTrue(sampleOffer.getCredentialConfigurationIds().contains(config2Id),
                "Should contain second credential configuration.");
    }

    @Test(priority = 2, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*Display name cannot be empty.*")
    public void testAddOfferWithEmptyDisplayName() throws VCConfigMgtException {

        VCOffer offer = createSampleOffer(
                StringUtils.EMPTY,
                Arrays.asList(config1Id)
        );
        offerManager.add(offer, TENANT_DOMAIN);
    }

    @Test(priority = 3, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*Credential configuration IDs cannot be empty.*")
    public void testAddOfferWithEmptyConfigIds() throws VCConfigMgtException {

        VCOffer offer = createSampleOffer(
                "Valid Display Name",
                Collections.emptyList()
        );
        offerManager.add(offer, TENANT_DOMAIN);
    }

    @Test(priority = 4, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*Credential configuration IDs cannot be empty.*")
    public void testAddOfferWithNullConfigIds() throws VCConfigMgtException {

        VCOffer offer = new VCOffer();
        offer.setDisplayName("Valid Display Name");
        offer.setCredentialConfigurationIds(null);

        offerManager.add(offer, TENANT_DOMAIN);
    }

    @Test(priority = 5, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*Credential configuration ID cannot be empty.*")
    public void testAddOfferWithBlankConfigId() throws VCConfigMgtException {

        List<String> configIds = new ArrayList<>();
        configIds.add(config1Id);
        configIds.add("");  // Empty config ID.
        configIds.add(config2Id);

        VCOffer offer = createSampleOffer("Valid Display Name", configIds);
        offerManager.add(offer, TENANT_DOMAIN);
    }

    @Test(priority = 6)
    public void testListOffers() throws VCConfigMgtException {

        List<VCOffer> offers = offerManager.list(TENANT_DOMAIN);

        Assert.assertNotNull(offers, "Offers list should not be null.");
        Assert.assertFalse(offers.isEmpty(), "Should have at least one offer.");

        // Verify the sample offer exists in the list.
        boolean found = false;
        for (VCOffer offer : offers) {
            if (sampleOffer.getOfferId().equals(offer.getOfferId())) {
                found = true;
                Assert.assertEquals(offer.getDisplayName(), TEST_DISPLAY_NAME);
                Assert.assertEquals(offer.getCredentialConfigurationIds().size(), 2);
                break;
            }
        }
        Assert.assertTrue(found, "Sample offer should be in the list.");
    }

    @Test(priority = 7)
    public void testGetOfferById() throws VCConfigMgtException {

        VCOffer retrieved = offerManager.get(sampleOffer.getOfferId(), TENANT_DOMAIN);

        Assert.assertNotNull(retrieved, "Retrieved offer should not be null.");
        Assert.assertEquals(retrieved.getOfferId(), sampleOffer.getOfferId());
        Assert.assertEquals(retrieved.getDisplayName(), sampleOffer.getDisplayName());
        Assert.assertEquals(retrieved.getCredentialConfigurationIds().size(),
                sampleOffer.getCredentialConfigurationIds().size());
    }

    @Test(priority = 8)
    public void testGetNonExistentOffer() throws VCConfigMgtException {

        VCOffer retrieved = offerManager.get("non-existent-offer-id", TENANT_DOMAIN);

        Assert.assertNull(retrieved, "Should return null for non-existent offer.");
    }

    @Test(priority = 9)
    public void testUpdateOffer() throws VCConfigMgtException {

        String updatedDisplayName = "Updated Employee Credentials";
        List<String> updatedConfigIds = Arrays.asList(config1Id, config2Id, config3Id);

        VCOffer updatingOffer = new VCOffer();
        updatingOffer.setDisplayName(updatedDisplayName);
        updatingOffer.setCredentialConfigurationIds(updatedConfigIds);

        VCOffer updated = offerManager.update(sampleOffer.getOfferId(), updatingOffer, TENANT_DOMAIN);

        Assert.assertNotNull(updated, "Updated offer should not be null.");
        Assert.assertEquals(updated.getOfferId(), sampleOffer.getOfferId(),
                "Offer ID should remain the same.");
        Assert.assertEquals(updated.getDisplayName(), updatedDisplayName,
                "Display name should be updated.");
        Assert.assertEquals(updated.getCredentialConfigurationIds().size(), 3,
                "Should have 3 credential configurations after update.");
        Assert.assertTrue(updated.getCredentialConfigurationIds().contains(config3Id),
                "Should contain newly added credential configuration.");

        // Update sampleOffer reference for subsequent tests.
        sampleOffer = updated;
    }

    @Test(priority = 10)
    public void testUpdateOfferWithBlankDisplayName() throws VCConfigMgtException {

        // When display name is blank, it should be preserved from existing.
        VCOffer updatingOffer = new VCOffer();
        updatingOffer.setDisplayName("");
        updatingOffer.setCredentialConfigurationIds(Arrays.asList(config1Id));

        VCOffer updated = offerManager.update(sampleOffer.getOfferId(), updatingOffer, TENANT_DOMAIN);

        Assert.assertEquals(updated.getDisplayName(), sampleOffer.getDisplayName(),
                "Display name should be preserved from existing when blank.");

        // Update sampleOffer reference.
        sampleOffer = updated;
    }

    @Test(priority = 11, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*not found.*")
    public void testUpdateNonExistentOffer() throws VCConfigMgtException {

        VCOffer updatingOffer = new VCOffer();
        updatingOffer.setDisplayName("Some Display Name");
        updatingOffer.setCredentialConfigurationIds(Arrays.asList(config1Id));

        offerManager.update("non-existent-offer-id", updatingOffer, TENANT_DOMAIN);
    }

    @Test(priority = 12, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*mismatch.*")
    public void testUpdateOfferWithMismatchedId() throws VCConfigMgtException {

        VCOffer updatingOffer = new VCOffer();
        updatingOffer.setOfferId("different-offer-id");
        updatingOffer.setDisplayName("Some Display Name");
        updatingOffer.setCredentialConfigurationIds(Arrays.asList(config1Id));

        offerManager.update(sampleOffer.getOfferId(), updatingOffer, TENANT_DOMAIN);
    }

    @Test(priority = 13, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*Credential configuration IDs cannot be empty.*")
    public void testUpdateOfferWithEmptyConfigIds() throws VCConfigMgtException {

        VCOffer updatingOffer = new VCOffer();
        updatingOffer.setDisplayName("Valid Display Name");
        updatingOffer.setCredentialConfigurationIds(Collections.emptyList());

        offerManager.update(sampleOffer.getOfferId(), updatingOffer, TENANT_DOMAIN);
    }

    @Test(priority = 14)
    public void testDeleteOffer() throws VCConfigMgtException {

        // Create a new offer for deletion.
        VCOffer offerToDelete = createSampleOffer(
                "Deletable Offer",
                Arrays.asList(config1Id)
        );
        VCOffer created = offerManager.add(offerToDelete, TENANT_DOMAIN);
        String idToDelete = created.getOfferId();

        // Verify it exists.
        VCOffer beforeDelete = offerManager.get(idToDelete, TENANT_DOMAIN);
        Assert.assertNotNull(beforeDelete, "Offer should exist before deletion.");

        // Delete it.
        offerManager.delete(idToDelete, TENANT_DOMAIN);

        // Verify it no longer exists.
        VCOffer afterDelete = offerManager.get(idToDelete, TENANT_DOMAIN);
        Assert.assertNull(afterDelete, "Offer should not exist after deletion.");
    }

    @Test(priority = 15)
    public void testAddOfferWithSingleConfigId() throws VCConfigMgtException {

        VCOffer offer = createSampleOffer(
                "Single Config Offer",
                Collections.singletonList(config3Id)
        );

        VCOffer created = offerManager.add(offer, TENANT_DOMAIN);

        Assert.assertNotNull(created.getOfferId(), "Offer ID should not be null.");
        Assert.assertEquals(created.getCredentialConfigurationIds().size(), 1,
                "Should have exactly 1 credential configuration.");
        Assert.assertTrue(created.getCredentialConfigurationIds().contains(config3Id),
                "Should contain the specified credential configuration.");
    }

    /**
     * Helper method to create a credential configuration for testing.
     *
     * @param identifier Unique identifier.
     * @param displayName Display name.
     * @return Created VCCredentialConfiguration.
     * @throws VCConfigMgtException on creation errors.
     */
    private VCCredentialConfiguration createCredentialConfiguration(String identifier, String displayName)
            throws VCConfigMgtException {

        VCCredentialConfiguration config = new VCCredentialConfiguration();
        config.setIdentifier(identifier);
        config.setDisplayName(displayName);
        config.setType("TestCredentialType");
        config.setFormat("jwt_vc_json");
        config.setScope("test_scope");
        config.setExpiresIn(3600);
        config.setMetadata(new VCCredentialConfiguration.Metadata());
        config.setClaims(Arrays.asList("email", "name"));

        return configManager.add(config, TENANT_DOMAIN);
    }

    /**
     * Helper method to create a sample VC Offer.
     *
     * @param displayName Display name.
     * @param credentialConfigurationIds Credential configuration IDs.
     * @return VCOffer.
     */
    private VCOffer createSampleOffer(String displayName, List<String> credentialConfigurationIds) {

        VCOffer offer = new VCOffer();
        offer.setDisplayName(displayName);
        offer.setCredentialConfigurationIds(new ArrayList<>(credentialConfigurationIds));
        return offer;
    }
}
