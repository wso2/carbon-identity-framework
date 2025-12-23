/*
 * Copyright (c) 2018-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.claim.metadata.mgt.dao;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB",
        files = {"dbScripts/claim_properties.sql"})
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

    @BeforeClass
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
        // There can be canonical values without label value pairs. Those values should be safely ignored.
        claimProperties2.put("canonicalValues", "NORTHERN, WEST");

        claimProperties3 = new HashMap<>();
        claimProperties3.put("Description", "TestDescription3");
        claimProperties3.put("FriendlyName", "UserName");
        claimProperties3.put("subAttributes", "http://wso2.org/claims/test http://wso2.org/claims/test2");
        claimProperties3.put("canonicalValues", "[{\"label\":\"north\",\"value\":\"NORTHERN\"},{\"label\":\"west\",\"value\":\"WEST\"}]");

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

        return new Object[][]{
                {localClaim1, TEST_LOCAL_TENANT_ID},
                {localClaim2, TEST_LOCAL_TENANT_ID},
                {localClaim3, TEST_LOCAL_TENANT_ID},
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

        return new Object[][]{
                {
                        localClaim1, TEST_LOCAL_TENANT_ID
                }, {
                localClaim2, TEST_LOCAL_TENANT_ID
        }, {
                localClaim3, TEST_LOCAL_TENANT_ID
        },

        };
    }

    @Test(dataProvider = "updateLocalClaimMappings")
    public void testUpdateLocalClaimMappings(List<LocalClaim> claimsToBeUpdated, List<LocalClaim> claimsInDB,
                                             List<LocalClaim> resultClaims, String userStoreDomain)
            throws ClaimMetadataException {

        ClaimDialectDAO claimDialectDAO = new ClaimDialectDAO();
        ClaimDialect claimDialect = new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
        claimDialectDAO.addClaimDialect(claimDialect, TEST_LOCAL_TENANT_ID);

        LocalClaimDAO localClaimDAO = new LocalClaimDAO();
        for (LocalClaim localClaim : claimsInDB) {
            localClaimDAO.addLocalClaim(localClaim, TEST_LOCAL_TENANT_ID);
        }

        localClaimDAO.updateLocalClaimMappings(claimsToBeUpdated, TEST_LOCAL_TENANT_ID, userStoreDomain);

        List<LocalClaim> localClaimsFromDB = localClaimDAO.getLocalClaims(TEST_LOCAL_TENANT_ID);
        assertEquals(localClaimsFromDB.size(), resultClaims.size(), "Failed to update local claim mappings");

        for (LocalClaim localClaimInResultSet : resultClaims) {
            LocalClaim localClaimFromDB = localClaimsFromDB.stream()
                    .filter(claim -> claim.getClaimURI().equals(localClaimInResultSet.getClaimURI()))
                    .findFirst()
                    .orElse(null);
            assert localClaimFromDB != null;
            assertTrue(areLocalClaimsEqual(localClaimInResultSet, localClaimFromDB));
        }

        claimDialectDAO.removeClaimDialect(claimDialect, TEST_LOCAL_TENANT_ID);
        assertThrows(ClaimMetadataException.class, () -> localClaimDAO.updateLocalClaimMappings(claimsToBeUpdated,
                TEST_LOCAL_TENANT_ID, userStoreDomain));
    }

    @DataProvider(name = "updateLocalClaimMappings")
    public Object[][] testUpdateLocalClaimMappingsData() {

        String testUserStoreDomain = "TEST_DOMAIN";

        // Local claims to be updated.
        List<LocalClaim> localClaimsToBeUpdated = Arrays.asList(
                createLocalClaim("http://wso2.org/claims/test1", "TestDescription1",
                        createAttributeMappings("PRIMARY", "givenname", "TEST_DOMAIN", "firstname")),
                createLocalClaim("http://wso2.org/claims/test2", "TestDescription2",
                        createAttributeMappings("PRIMARY", "givenname", "TEST_DOMAIN", "firstname")),
                createLocalClaim("http://wso2.org/claims/test3", "TestDescription3",
                        createAttributeMappings("TEST_DOMAIN", "firstname")),
                createLocalClaim("http://wso2.org/claims/test4", null,
                        createAttributeMappings("TEST_DOMAIN", "firstname"))
        );

        // Local claims in DB.
        List<LocalClaim> localClaimsInDB = Arrays.asList(
                createLocalClaim("http://wso2.org/claims/test2", "TestDescription2",
                        createAttributeMappings("PRIMARY", "firstname", "TEST_DOMAIN", "givenname")),
                createLocalClaim("http://wso2.org/claims/test3", "TestDescription3",
                        createAttributeMappings("PRIMARY", "givenname")),
                createLocalClaim("http://wso2.org/claims/test4", "TestDescription4",
                        null)
        );

        // Expected result local claims.
        List<LocalClaim> resultLocalClaims = Arrays.asList(
                createLocalClaim("http://wso2.org/claims/test1", "TestDescription1",
                        createAttributeMappings("PRIMARY", "givenname", "TEST_DOMAIN", "firstname")),
                createLocalClaim("http://wso2.org/claims/test2", "TestDescription2",
                        createAttributeMappings("PRIMARY", "firstname", "TEST_DOMAIN", "firstname")),
                createLocalClaim("http://wso2.org/claims/test3", "TestDescription3",
                        createAttributeMappings("PRIMARY", "givenname", "TEST_DOMAIN", "firstname")),
                createLocalClaim("http://wso2.org/claims/test4", "TestDescription4",
                        createAttributeMappings("TEST_DOMAIN", "firstname"))
        );

        return new Object[][] {
                { localClaimsToBeUpdated, localClaimsInDB, resultLocalClaims, testUserStoreDomain }
        };
    }

    private LocalClaim createLocalClaim(String uri, String description, List<AttributeMapping> attributeMappings) {

        LocalClaim localClaim = new LocalClaim(uri);
        if (description != null) {
            Map<String, String> claimProperties = new HashMap<>();
            claimProperties.put("Description", description);
            localClaim.setClaimProperties(claimProperties);
        }
        if (attributeMappings != null) {
            localClaim.setMappedAttributes(attributeMappings);
        }
        return localClaim;
    }

    private List<AttributeMapping> createAttributeMappings(String... mappings) {

        List<AttributeMapping> attributeMappings = new ArrayList<>();
        for (int i = 0; i < mappings.length; i += 2) {
            attributeMappings.add(new AttributeMapping(mappings[i], mappings[i + 1]));
        }
        return attributeMappings;
    }

    private boolean areLocalClaimsEqual(LocalClaim localClaim1, LocalClaim localClaim2) {

        if (localClaim1 == localClaim2) {
            return true;
        }
        return localClaim1 != null && localClaim2 != null
                && Objects.equals(localClaim1.getClaimURI(), localClaim2.getClaimURI())
                && Objects.equals(localClaim1.getClaimProperties(), localClaim2.getClaimProperties())
                && areAttributeMappingsEqual(localClaim1.getMappedAttributes(), localClaim2.getMappedAttributes());
    }

    private boolean areAttributeMappingsEqual(List<AttributeMapping> attributeMappings1,
                                              List<AttributeMapping> attributeMappings2) {

        if (attributeMappings1 == attributeMappings2) {
            return true;
        }
        if (attributeMappings1 == null || attributeMappings2 == null) {
            return false;
        }
        if (attributeMappings1.size() != attributeMappings2.size()) {
            return false;
        }
        for (AttributeMapping attributeMapping1 : attributeMappings1) {
            boolean found = false;
            for (AttributeMapping attributeMapping2 : attributeMappings2) {
                if (Objects.equals(attributeMapping1.getUserStoreDomain(), attributeMapping2.getUserStoreDomain())
                        && Objects.equals(attributeMapping1.getAttributeName(), attributeMapping2.getAttributeName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
