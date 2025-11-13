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
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtClientException;
import org.wso2.carbon.identity.vc.config.management.exception.VCConfigMgtException;
import org.wso2.carbon.identity.vc.config.management.internal.VCConfigManagementServiceDataHolder;
import org.wso2.carbon.identity.vc.config.management.model.VCCredentialConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Test class for VCCredentialConfigManagerImpl.
 * Tests service layer business logic for VC Credential Configuration management.
 */
@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class VCCredentialConfigManagerImplTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String TEST_IDENTIFIER = "EmployeeBadge-" + System.currentTimeMillis();
    private static final String TEST_DISPLAY_NAME = "Employee Badge Credential";
    private static final String TEST_TYPE = "EmployeeBadgeCredential";
    private static final String TEST_FORMAT = "jwt_vc_json";
    private static final String TEST_SCOPE = "employee_badge";
    private static final int TEST_EXPIRES_IN = 3600;

    private VCCredentialConfigManager configManager;
    private VCCredentialConfiguration sampleConfig;

    private MockedStatic<ClaimMetadataHandler> claimMetadataHandlerMock;

    @BeforeClass
    public void setUpClass() throws Exception {

        // Initialize Mockito annotations before using @Mock fields.
        MockitoAnnotations.openMocks(this);

        configManager = VCCredentialConfigManagerImpl.getInstance();

        // Setup API Resource mocks for scope validation.
        Scope testScope = new Scope(TEST_SCOPE, TEST_SCOPE, TEST_SCOPE,
                TEST_SCOPE,
                "test_api", "test_org");
        APIResource testApiResource = new APIResource.APIResourceBuilder()
                .id("test_api")
                .name("Test API Resource")
                .type("VC")
                .identifier("test_api")
                .description("API Resource for testing")
                .requiresAuthorization(false).build();
        APIResourceManager apiResourceManager = mock(APIResourceManager.class);
        VCConfigManagementServiceDataHolder.getInstance().setAPIResourceManager(apiResourceManager);
        when(apiResourceManager.getScopeByName(any(), any())).thenReturn(testScope);
        when(apiResourceManager.getAPIResourceById(any(), any())).thenReturn(testApiResource);

        ClaimMetadataHandler mockClaimMetadataHandler = mock(ClaimMetadataHandler.class);
        claimMetadataHandlerMock = mockStatic(ClaimMetadataHandler.class);
        claimMetadataHandlerMock.when(ClaimMetadataHandler::getInstance).thenReturn(mockClaimMetadataHandler);

        // Create a set of ExternalClaim objects for mocking.
        Set<ExternalClaim> vcClaims = new HashSet<>();
        vcClaims.add(new ExternalClaim("http://wso2.org/vc", "email", "http://wso2.org/claims/emailaddress"));
        vcClaims.add(new ExternalClaim("http://wso2.org/vc", "name", "http://wso2.org/claims/fullname"));
        vcClaims.add(new ExternalClaim("http://wso2.org/vc", "employee_id", "http://wso2.org/claims/employeeid"));
        vcClaims.add(new ExternalClaim("http://wso2.org/vc", "department", "http://wso2.org/claims/department"));
        vcClaims.add(new ExternalClaim("http://wso2.org/vc", "valid_claim", "http://wso2.org/claims/validclaim"));
        vcClaims.add(new ExternalClaim("http://wso2.org/vc", "another_claim", "http://wso2.org/claims/anotherclaim"));

        when(mockClaimMetadataHandler.getMappingsFromOtherDialectToCarbon(
                anyString(), isNull(), anyString())).thenReturn(vcClaims);
    }

    @AfterClass
    public void tearDownClass() {

        if (claimMetadataHandlerMock != null) {
            claimMetadataHandlerMock.close();
        }
    }

    @Test(priority = 1)
    public void testAddConfiguration() throws VCConfigMgtException {

        VCCredentialConfiguration creatingConfig = createSampleConfiguration(
                TEST_IDENTIFIER,
                TEST_DISPLAY_NAME,
                TEST_TYPE,
                TEST_FORMAT,
                TEST_SCOPE,
                TEST_EXPIRES_IN,
                Arrays.asList("email", "name", "employee_id")
        );

        sampleConfig = configManager.add(creatingConfig, TENANT_DOMAIN);

        Assert.assertNotNull(sampleConfig.getId(), "Configuration ID should not be null.");
        Assert.assertEquals(sampleConfig.getIdentifier(), TEST_IDENTIFIER,
                "Identifier should match.");
        Assert.assertEquals(sampleConfig.getDisplayName(), TEST_DISPLAY_NAME,
                "Display name should match.");
        Assert.assertEquals(sampleConfig.getType(), TEST_TYPE,
                "Type should match.");
        Assert.assertEquals(sampleConfig.getFormat(), TEST_FORMAT,
                "Format should match.");
        Assert.assertEquals(sampleConfig.getScope(), TEST_SCOPE,
                "Scope should match.");
        Assert.assertEquals(sampleConfig.getExpiresIn(), Integer.valueOf(TEST_EXPIRES_IN),
                "Expires in should match.");
        Assert.assertNotNull(sampleConfig.getClaims(),
                "Claims should not be null.");
        Assert.assertEquals(sampleConfig.getClaims().size(), 3,
                "Should have 3 claims.");
        Assert.assertEquals(sampleConfig.getSigningAlgorithm(), "RS256",
                "Default signing algorithm should be RS256.");
    }

    @Test(priority = 2, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*Identifier cannot be empty.*")
    public void testAddConfigurationWithEmptyIdentifier() throws VCConfigMgtException {

        VCCredentialConfiguration config = createSampleConfiguration(
                StringUtils.EMPTY,
                TEST_DISPLAY_NAME,
                TEST_TYPE,
                TEST_FORMAT,
                TEST_SCOPE,
                TEST_EXPIRES_IN,
                Arrays.asList("email")
        );
        configManager.add(config, TENANT_DOMAIN);
    }

    @Test(priority = 3, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*Display name cannot be empty.*")
    public void testAddConfigurationWithEmptyDisplayName() throws VCConfigMgtException {

        VCCredentialConfiguration config = createSampleConfiguration(
                "UniqueIdentifier1-" + System.currentTimeMillis(),
                StringUtils.EMPTY,
                TEST_TYPE,
                TEST_FORMAT,
                TEST_SCOPE,
                TEST_EXPIRES_IN,
                Arrays.asList("email")
        );
        configManager.add(config, TENANT_DOMAIN);
    }

    @Test(priority = 4, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*Credential type cannot be empty.*")
    public void testAddConfigurationWithEmptyType() throws VCConfigMgtException {

        VCCredentialConfiguration config = createSampleConfiguration(
                "UniqueIdentifier2-" + System.currentTimeMillis(),
                TEST_DISPLAY_NAME,
                StringUtils.EMPTY,
                TEST_FORMAT,
                TEST_SCOPE,
                TEST_EXPIRES_IN,
                Arrays.asList("email")
        );
        configManager.add(config, TENANT_DOMAIN);
    }

    @Test(priority = 5, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*Expiry must be at least.*")
    public void testAddConfigurationWithInvalidExpiry() throws VCConfigMgtException {

        VCCredentialConfiguration config = createSampleConfiguration(
                "UniqueIdentifier3-" + System.currentTimeMillis(),
                TEST_DISPLAY_NAME,
                TEST_TYPE,
                TEST_FORMAT,
                TEST_SCOPE,
                59,  // Less than minimum allowed (MIN_EXPIRES_IN_SECONDS = 60).
                Arrays.asList("email")
        );
        configManager.add(config, TENANT_DOMAIN);
    }

    @Test(priority = 6, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*already exists.*")
    public void testAddConfigurationWithDuplicateIdentifier() throws VCConfigMgtException {

        VCCredentialConfiguration config = createSampleConfiguration(
                TEST_IDENTIFIER,  // Same identifier as sampleConfig.
                "Different Display Name",
                TEST_TYPE,
                TEST_FORMAT,
                TEST_SCOPE,
                TEST_EXPIRES_IN,
                Arrays.asList("email")
        );
        configManager.add(config, TENANT_DOMAIN);
    }

    @Test(priority = 7)
    public void testListConfigurations() throws VCConfigMgtException {

        List<VCCredentialConfiguration> configs = configManager.list(TENANT_DOMAIN);

        Assert.assertNotNull(configs, "Configuration list should not be null.");
        Assert.assertTrue(configs.size() >= 1, "Should have at least one configuration.");

        // Verify the sample config exists in the list.
        boolean found = false;
        for (VCCredentialConfiguration config : configs) {
            if (sampleConfig.getId().equals(config.getId())) {
                found = true;
                Assert.assertEquals(config.getIdentifier(), TEST_IDENTIFIER);
                Assert.assertEquals(config.getDisplayName(), TEST_DISPLAY_NAME);
                Assert.assertEquals(config.getScope(), TEST_SCOPE);
                break;
            }
        }
        Assert.assertTrue(found, "Sample configuration should be in the list.");
    }

    @Test(priority = 8)
    public void testGetConfigurationById() throws VCConfigMgtException {

        VCCredentialConfiguration retrieved = configManager.get(sampleConfig.getId(), TENANT_DOMAIN);

        Assert.assertNotNull(retrieved, "Retrieved configuration should not be null.");
        Assert.assertEquals(retrieved.getId(), sampleConfig.getId());
        Assert.assertEquals(retrieved.getIdentifier(), sampleConfig.getIdentifier());
        Assert.assertEquals(retrieved.getDisplayName(), sampleConfig.getDisplayName());
        Assert.assertEquals(retrieved.getType(), sampleConfig.getType());
        Assert.assertEquals(retrieved.getFormat(), sampleConfig.getFormat());
        Assert.assertEquals(retrieved.getScope(), sampleConfig.getScope());
        Assert.assertEquals(retrieved.getExpiresIn(), sampleConfig.getExpiresIn());
        Assert.assertEquals(retrieved.getClaims().size(), sampleConfig.getClaims().size());
    }

    @Test(priority = 9)
    public void testGetConfigurationByConfigId() throws VCConfigMgtException {

        VCCredentialConfiguration retrieved = configManager.getByIdentifier(
                sampleConfig.getIdentifier(), TENANT_DOMAIN);

        Assert.assertNotNull(retrieved, "Retrieved configuration should not be null.");
        Assert.assertEquals(retrieved.getId(), sampleConfig.getId());
        Assert.assertEquals(retrieved.getIdentifier(), sampleConfig.getIdentifier());
        Assert.assertEquals(retrieved.getDisplayName(), sampleConfig.getDisplayName());
    }

    @Test(priority = 10)
    public void testGetNonExistentConfiguration() throws VCConfigMgtException {

        VCCredentialConfiguration retrieved = configManager.get("non-existent-id", TENANT_DOMAIN);

        Assert.assertNull(retrieved, "Should return null for non-existent configuration.");
    }

    @Test(priority = 11)
    public void testUpdateConfiguration() throws VCConfigMgtException {

        String updatedDisplayName = "Updated Employee Badge";
        List<String> updatedClaims = Arrays.asList("email", "name", "employee_id", "department");

        VCCredentialConfiguration updatingConfig = new VCCredentialConfiguration();
        // Note: Don't set identifier - it should be preserved from existing config.
        updatingConfig.setDisplayName(updatedDisplayName);
        updatingConfig.setType("UpdatedType");
        updatingConfig.setExpiresIn(7200);
        updatingConfig.setClaims(updatedClaims);
        // Don't set format - it should be preserved from existing config.

        VCCredentialConfiguration updated = configManager.update(
                sampleConfig.getId(), updatingConfig, TENANT_DOMAIN);

        Assert.assertNotNull(updated, "Updated configuration should not be null.");
        Assert.assertEquals(updated.getId(), sampleConfig.getId(),
                "ID should remain the same.");
        Assert.assertEquals(updated.getIdentifier(), sampleConfig.getIdentifier(),
                "Identifier should remain the same (preserved from existing).");
        Assert.assertEquals(updated.getDisplayName(), updatedDisplayName,
                "Display name should be updated.");
        Assert.assertEquals(updated.getType(), "UpdatedType",
                "Type should be updated.");
        Assert.assertEquals(updated.getExpiresIn(), Integer.valueOf(7200),
                "Expires in should be updated.");
        Assert.assertEquals(updated.getClaims().size(), 4,
                "Should have 4 claims after update.");

        // Update sampleConfig reference for subsequent tests.
        sampleConfig = updated;
    }

    @Test(priority = 12, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*Identifier cannot be updated.*")
    public void testUpdateConfigurationWithNewIdentifier() throws VCConfigMgtException {

        VCCredentialConfiguration updatingConfig = new VCCredentialConfiguration();
        updatingConfig.setIdentifier("NewIdentifier");
        updatingConfig.setDisplayName("Some Display Name");

        configManager.update(sampleConfig.getId(), updatingConfig, TENANT_DOMAIN);
    }

    @Test(priority = 13, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*not found.*")
    public void testUpdateNonExistentConfiguration() throws VCConfigMgtException {

        VCCredentialConfiguration updatingConfig = new VCCredentialConfiguration();
        updatingConfig.setDisplayName("Some Display Name");

        configManager.update("non-existent-id", updatingConfig, TENANT_DOMAIN);
    }

    @Test(priority = 14, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*mismatch.*")
    public void testUpdateConfigurationWithMismatchedId() throws VCConfigMgtException {

        VCCredentialConfiguration updatingConfig = new VCCredentialConfiguration();
        updatingConfig.setId("different-id");
        updatingConfig.setDisplayName("Some Display Name");

        configManager.update(sampleConfig.getId(), updatingConfig, TENANT_DOMAIN);
    }

    @Test(priority = 15)
    public void testDeleteConfiguration() throws VCConfigMgtException {

        // Create a new configuration for deletion.
        VCCredentialConfiguration configToDelete = createSampleConfiguration(
                "DeletableConfig-" + System.currentTimeMillis(),
                "Deletable Configuration",
                "DeletableType",
                TEST_FORMAT,
                "deletable_scope",
                TEST_EXPIRES_IN,
                Arrays.asList("email")
        );
        VCCredentialConfiguration created = configManager.add(configToDelete, TENANT_DOMAIN);
        String idToDelete = created.getId();

        // Verify it exists.
        VCCredentialConfiguration beforeDelete = configManager.get(idToDelete, TENANT_DOMAIN);
        Assert.assertNotNull(beforeDelete, "Configuration should exist before deletion.");

        // Delete it.
        configManager.delete(idToDelete, TENANT_DOMAIN);

        // Verify it no longer exists.
        VCCredentialConfiguration afterDelete = configManager.get(idToDelete, TENANT_DOMAIN);
        Assert.assertNull(afterDelete, "Configuration should not exist after deletion.");
    }

    @Test(priority = 16)
    public void testAddConfigurationWithDefaultFormat() throws VCConfigMgtException {

        VCCredentialConfiguration config = createSampleConfiguration(
                "DefaultFormatConfig-" + System.currentTimeMillis(),
                "Default Format Configuration",
                TEST_TYPE,
                null,  // Format not provided.
                TEST_SCOPE,
                TEST_EXPIRES_IN,
                Arrays.asList("email")
        );

        VCCredentialConfiguration created = configManager.add(config, TENANT_DOMAIN);

        Assert.assertNotNull(created.getFormat(),
                "Format should be set to default.");
        Assert.assertEquals(created.getFormat(), "jwt_vc_json",
                "Default format should be jwt_vc_json.");
    }

    @Test(priority = 17, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*Unsupported.*")
    public void testAddConfigurationWithUnsupportedFormat() throws VCConfigMgtException {

        VCCredentialConfiguration config = createSampleConfiguration(
                "UnsupportedFormatConfig-" + System.currentTimeMillis(),
                "Unsupported Format Configuration",
                TEST_TYPE,
                "ldp_vc",  // Unsupported format.
                TEST_SCOPE,
                TEST_EXPIRES_IN,
                Arrays.asList("email")
        );

        configManager.add(config, TENANT_DOMAIN);
    }

    @Test(priority = 18, expectedExceptions = VCConfigMgtClientException.class,
            expectedExceptionsMessageRegExp = ".*claim.*")
    public void testAddConfigurationWithInvalidClaims() throws VCConfigMgtException {

        List<String> invalidClaims = new ArrayList<>();
        invalidClaims.add("valid_claim");
        invalidClaims.add("");  // Empty claim.
        invalidClaims.add("another_claim");

        VCCredentialConfiguration config = createSampleConfiguration(
                "InvalidClaimsConfig-" + System.currentTimeMillis(),
                "Invalid Claims Configuration",
                TEST_TYPE,
                TEST_FORMAT,
                TEST_SCOPE,
                TEST_EXPIRES_IN,
                invalidClaims
        );

        configManager.add(config, TENANT_DOMAIN);
    }

    /**
     * Helper method to create a sample VC Credential Configuration.
     *
     * @param identifier Identifier.
     * @param displayName Display name.
     * @param type Type.
     * @param format Format.
     * @param scope Scope.
     * @param expiresIn Expiry in seconds.
     * @param claims Claims list.
     * @return VCCredentialConfiguration.
     */
    private VCCredentialConfiguration createSampleConfiguration(String identifier, String displayName, String type,
                                                                String format, String scope, int expiresIn,
                                                                List<String> claims) {

        VCCredentialConfiguration config = new VCCredentialConfiguration();
        config.setIdentifier(identifier);
        config.setDisplayName(displayName);
        config.setType(type);
        config.setFormat(format);
        config.setScope(scope);
        config.setExpiresIn(expiresIn);
        config.setMetadata(new VCCredentialConfiguration.Metadata());
        config.setClaims(claims);
        return config;
    }
}
