/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.claim.metadata.mgt.dao;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB",
                files = { "dbScripts/claim_properties.sql" })
@WithCarbonHome
public class LocalClaimDAOTest {

    private static final int TEST_LOCAL_TENANT_ID = -1234;

    LocalClaim localClaim1;
    LocalClaim localClaim2;
    LocalClaim localClaim3;

    List<AttributeMapping> mappedAttributes1;
    List<AttributeMapping> mappedAttributes2;
    List<AttributeMapping> mappedAttributes3;

    AttributeMapping attributeMapping1;
    AttributeMapping attributeMapping2;
    AttributeMapping attributeMapping3;

    Map<String, String> claimProperties1;
    Map<String, String> claimProperties2;
    Map<String, String> claimProperties3;

    @BeforeMethod
    public void initTest() throws Exception {

        attributeMapping1 = new AttributeMapping("PRIMARY", "givenname");
        attributeMapping2 = new AttributeMapping("PRIMARY", "nickname");
        attributeMapping3 = new AttributeMapping("PRIMARY", "username");

        claimProperties1 = new HashMap<>();
        claimProperties1.put("Description", "TestDescription1");
        claimProperties1.put("FriendlyName", "Given Name");

        claimProperties2 = new HashMap<>();
        claimProperties2.put("Description", "TestDescription2");
        claimProperties2.put("FriendlyName", "Nick Name");

        claimProperties3 = new HashMap<>();
        claimProperties3.put("Description", "TestDescription3");
        claimProperties3.put("FriendlyName", "UserName");

        mappedAttributes1 = new ArrayList<>();
        mappedAttributes1.add(attributeMapping1);

        mappedAttributes2 = new ArrayList<>();
        mappedAttributes2.add(attributeMapping2);

        mappedAttributes3 = new ArrayList<>();
        mappedAttributes3.add(attributeMapping3);

        localClaim1 = new LocalClaim("http://wso2.org/claims/test1");
        localClaim1.setMappedAttributes(mappedAttributes1);
        localClaim1.setClaimProperties(claimProperties1);

        localClaim2 = new LocalClaim("http://wso2.org/claims/test2");
        localClaim2.setMappedAttributes(mappedAttributes2);
        localClaim2.setClaimProperties(claimProperties2);

        localClaim3 = new LocalClaim("http://wso2.org/claims/test3");
        localClaim3.setMappedAttributes(mappedAttributes3);
        localClaim3.setClaimProperties(claimProperties3);
    }

    @Test(dataProvider = "getLocalClaim")
    public void testGetLocalClaims(Object localClaim, int tenantId) throws ClaimMetadataException {

        ClaimDialectDAO claimDialectDAO = new ClaimDialectDAO();
        ClaimDialect claimDialect = new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
        claimDialectDAO.addClaimDialect(claimDialect, tenantId);

        LocalClaimDAO localClaimDAO = new LocalClaimDAO();

        localClaimDAO.addLocalClaim(((LocalClaim) localClaim), tenantId);

        List<LocalClaim> localClaimsFromDB = localClaimDAO.getLocalClaims(tenantId);

        assertNotNull(localClaimsFromDB, "Failed to retrieve the claim id.");

        assertEquals(localClaimsFromDB.size(), 1, "Failed to retrieve the claim id.");

        assertEquals(localClaimsFromDB.get(0).getClaimURI(), ((LocalClaim) localClaim).getClaimURI(),
                "Failed to retrieve the claim id.");

        localClaimDAO.removeLocalClaim(((LocalClaim) localClaim).getClaimURI(), tenantId);

        // Test for removal of local claims
        assertEquals(localClaimDAO.getLocalClaims(tenantId).size(), 0, "Failed to retrieve the claim id.");

        claimDialectDAO.removeClaimDialect(claimDialect, tenantId);
    }

    @DataProvider(name = "getLocalClaim")
    public Object[][] testGetLocalClaimData() {
        return new Object[][] {
                {
                        localClaim1, TEST_LOCAL_TENANT_ID
                }, {
                        localClaim2, TEST_LOCAL_TENANT_ID
                }, {
                        localClaim3, TEST_LOCAL_TENANT_ID
                },

                };
    }

    @Test(dataProvider = "updateLocalClaim")
    public void testUpdateLocalClaim(Object localClaim, int tenantId) throws ClaimMetadataException {

        ClaimDialectDAO claimDialectDAO = new ClaimDialectDAO();
        ClaimDialect claimDialect = new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
        claimDialectDAO.addClaimDialect(claimDialect, tenantId);

        LocalClaimDAO localClaimDAO = new LocalClaimDAO();
        localClaimDAO.addLocalClaim(((LocalClaim) localClaim), tenantId);

        AttributeMapping attributeMapping = new AttributeMapping("PRIMARY", "newValue");
        List<AttributeMapping> mappedAttributes = new ArrayList<>();
        mappedAttributes.add(attributeMapping);

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("Description", "NewTestDescription1");
        claimProperties.put("FriendlyName", "NewFriendlyName");

        ((LocalClaim) localClaim).setMappedAttributes(mappedAttributes);
        ((LocalClaim) localClaim).setClaimProperties(claimProperties);

        localClaimDAO.updateLocalClaim(((LocalClaim) localClaim), tenantId);

        assertEquals(localClaimDAO.getLocalClaims(tenantId).size(), 1, "Fail to update local claim");

        assertEquals(localClaimDAO.getLocalClaims(tenantId).get(0).getMappedAttributes().get(0).getAttributeName(),
                "newValue", "Fail to update local claim");

        assertEquals(localClaimDAO.getLocalClaims(tenantId).get(0).getClaimProperties().get("Description"),
                "NewTestDescription1", "Fail to update local claim");

        assertEquals(localClaimDAO.getLocalClaims(tenantId).get(0).getClaimProperties().get("FriendlyName"),
                "NewFriendlyName", "Fail to update local claim");

        localClaimDAO.removeLocalClaim(((LocalClaim) localClaim).getClaimURI(), tenantId);

        claimDialectDAO.removeClaimDialect(claimDialect, tenantId);
    }

    @DataProvider(name = "updateLocalClaim")
    public Object[][] testUpdateLocalClaimData() {
        return new Object[][] {
                {
                        localClaim1, TEST_LOCAL_TENANT_ID
                }, {
                        localClaim2, TEST_LOCAL_TENANT_ID
                }, {
                        localClaim3, TEST_LOCAL_TENANT_ID
                },

                };
    }

    @Test
    public void testAddLocalClaimWithUniqueProperty() throws Exception {

        // Add claim dialect
        ClaimDialectDAO claimDialectDAO = new ClaimDialectDAO();
        ClaimDialect claimDialect = new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
        claimDialectDAO.addClaimDialect(claimDialect, TEST_LOCAL_TENANT_ID);

        // Add claim
        localClaim3.getClaimProperties().put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, "true");
        LocalClaimDAO localClaimDAO = new LocalClaimDAO();
        localClaimDAO.addLocalClaim(localClaim3, TEST_LOCAL_TENANT_ID);

        // Retrieve and verify
        List<LocalClaim> retrievedClaims = localClaimDAO.getLocalClaims(TEST_LOCAL_TENANT_ID);
        assertNotNull(retrievedClaims, "Retrieved claims should not be null");
        assertEquals(retrievedClaims.size(), 1, "Should have retrieved exactly one claim");

        LocalClaim retrievedClaim = retrievedClaims.get(0);
        Map<String, String> retrievedProperties = retrievedClaim.getClaimProperties();

        // Verify property transformation
        verifyUniqueClaimProperties(retrievedProperties);

        // Clean up
        localClaimDAO.removeLocalClaim(localClaim3.getClaimURI(), TEST_LOCAL_TENANT_ID);
        claimDialectDAO.removeClaimDialect(claimDialect, TEST_LOCAL_TENANT_ID);
    }

    @Test
    public void testGetLocalClaimsWithUniqueProperty() throws Exception {

        // Add claim dialect
        ClaimDialectDAO claimDialectDAO = new ClaimDialectDAO();
        ClaimDialect claimDialect = new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
        claimDialectDAO.addClaimDialect(claimDialect, TEST_LOCAL_TENANT_ID);

        // Add initial claim
        LocalClaimDAO localClaimDAO = new LocalClaimDAO();
        localClaimDAO.addLocalClaim(localClaim3, TEST_LOCAL_TENANT_ID);

        // Get claim ID and add isUnique property directly using ClaimDAO
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            ClaimDAO claimDAO = new ClaimDAO();
            int claimId = claimDAO.getClaimId(connection, ClaimConstants.LOCAL_CLAIM_DIALECT_URI,
                    localClaim3.getClaimURI(), TEST_LOCAL_TENANT_ID);

            Map<String, String> uniqueProperty = new HashMap<>();
            uniqueProperty.put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, "true");
            claimDAO.addClaimProperties(connection, claimId, uniqueProperty, TEST_LOCAL_TENANT_ID);

            IdentityDatabaseUtil.commitTransaction(connection);
        }

        // Retrieve and verify
        List<LocalClaim> retrievedClaims = localClaimDAO.getLocalClaims(TEST_LOCAL_TENANT_ID);
        assertNotNull(retrievedClaims, "Retrieved claims should not be null");
        assertEquals(retrievedClaims.size(), 1, "Should have retrieved exactly one claim");

        LocalClaim retrievedClaim = retrievedClaims.get(0);
        Map<String, String> retrievedProperties = retrievedClaim.getClaimProperties();

        // Verify property transformation
        verifyUniqueClaimProperties(retrievedProperties);

        // Clean up
        localClaimDAO.removeLocalClaim(localClaim3.getClaimURI(), TEST_LOCAL_TENANT_ID);
        claimDialectDAO.removeClaimDialect(claimDialect, TEST_LOCAL_TENANT_ID);
    }

    @Test
    public void testUpdateLocalClaimWithUniqueProperty() throws ClaimMetadataException {

        // Setup initial claim without isUnique property
        ClaimDialectDAO claimDialectDAO = new ClaimDialectDAO();
        ClaimDialect claimDialect = new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
        claimDialectDAO.addClaimDialect(claimDialect, TEST_LOCAL_TENANT_ID);

        LocalClaimDAO localClaimDAO = new LocalClaimDAO();
        localClaimDAO.addLocalClaim(localClaim3, TEST_LOCAL_TENANT_ID);

        // Update claim with isUnique property
        Map<String, String> updatedProperties = new HashMap<>(localClaim3.getClaimProperties());
        updatedProperties.put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, "true");
        localClaim3.setClaimProperties(updatedProperties);

        localClaimDAO.updateLocalClaim(localClaim3, TEST_LOCAL_TENANT_ID);

        // Retrieve and verify
        List<LocalClaim> retrievedClaims = localClaimDAO.getLocalClaims(TEST_LOCAL_TENANT_ID);
        assertNotNull(retrievedClaims, "Retrieved claims should not be null");
        assertEquals(retrievedClaims.size(), 1, "Should have retrieved exactly one claim");

        LocalClaim retrievedClaim = retrievedClaims.get(0);
        Map<String, String> retrievedProperties = retrievedClaim.getClaimProperties();

        // Verify property transformation
        verifyUniqueClaimProperties(retrievedProperties);

        // Clean up
        localClaimDAO.removeLocalClaim(localClaim3.getClaimURI(), TEST_LOCAL_TENANT_ID);
        claimDialectDAO.removeClaimDialect(claimDialect, TEST_LOCAL_TENANT_ID);
    }

    /**
     * Verifies that the claim properties have been correctly transformed for unique claims.
     *
     * @param retrievedProperties The map of claim properties to verify
     */
    private void verifyUniqueClaimProperties(Map<String, String> retrievedProperties) {

        assertFalse(retrievedProperties.containsKey(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY),
                "isUnique property should have been removed");
        assertTrue(retrievedProperties.containsKey(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY),
                "UniquenessScope property should have been added");
        assertEquals(ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(),
                retrievedProperties.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY),
                "UniquenessScope should be set to ACROSS_USERSTORES");
    }
}
