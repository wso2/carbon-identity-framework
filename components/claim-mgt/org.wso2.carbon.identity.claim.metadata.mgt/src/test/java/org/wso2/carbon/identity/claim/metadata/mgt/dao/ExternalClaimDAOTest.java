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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;

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
public class ExternalClaimDAOTest {
    private static final int TENANT_ID = -1234;
    private static final String EXTERNAL_CLAIM_DIALECT_URI = "TestExternalClaimDialect";
    private static final String UPDATED_CLAIM_URI_1 = "updatedURI1";
    private static final String UPDATED_CLAIM_URI_2 = "updatedURI2";
    private static final String UPDATED_CLAIM_URI_3 = "updatedURI3";

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

    @Test(dataProvider = "getExternalClaim")
    public void testGetExternalClaims(Object localClaim, Object externalClaim, int tenantId)
            throws ClaimMetadataException {

        ClaimDialectDAO claimDialectDAO1 = new ClaimDialectDAO();
        ClaimDialect claimDialect1 = new ClaimDialect(EXTERNAL_CLAIM_DIALECT_URI);
        claimDialectDAO1.addClaimDialect(claimDialect1, TENANT_ID);

        ClaimDialectDAO claimDialectDAO2 = new ClaimDialectDAO();
        ClaimDialect claimDialect2 = new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
        claimDialectDAO2.addClaimDialect(claimDialect2, TENANT_ID);

        LocalClaimDAO localClaimDAO = new LocalClaimDAO();
        localClaimDAO.addLocalClaim((LocalClaim) localClaim, tenantId);

        ExternalClaimDAO externalClaimDAO = new ExternalClaimDAO();

        externalClaimDAO.addExternalClaim((ExternalClaim) externalClaim, tenantId);

        assertNotNull(
                externalClaimDAO.getExternalClaims(((ExternalClaim) externalClaim).getClaimDialectURI(), tenantId),
                "Failed to retrieve external claim");

        assertEquals(externalClaimDAO.getExternalClaims(((ExternalClaim) externalClaim).getClaimDialectURI(), tenantId)
                .size(), 1, "Failed to retrieve external claim");

        assertEquals(externalClaimDAO.getExternalClaims(((ExternalClaim) externalClaim).getClaimDialectURI(), tenantId)
                        .get(0).getClaimURI(), ((ExternalClaim) externalClaim).getClaimURI(),
                "Failed to retrieve the claim id.");

        // Clean after test
        localClaimDAO.removeLocalClaim(((LocalClaim) localClaim).getClaimURI(), tenantId);
        externalClaimDAO.removeExternalClaim(((ExternalClaim) externalClaim).getClaimDialectURI(),
                ((ExternalClaim) externalClaim).getClaimURI(), tenantId);

        assertEquals(externalClaimDAO.getExternalClaims(((ExternalClaim) externalClaim).getClaimDialectURI(), tenantId)
                .size(), 0, "Failed to retrieve the claim id.");

        claimDialectDAO1.removeClaimDialect(claimDialect1, tenantId);
        claimDialectDAO2.removeClaimDialect(claimDialect2, tenantId);
    }

    @DataProvider(name = "getExternalClaim")
    public Object[][] testGetExternalClaimsData() {
        Claim externalClaim1 = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, "TestExternalClaimURI1",
                "http://wso2.org/claims/test1");
        Map<String, String> claimProperties1 = new HashMap<>();
        claimProperties1.put("DisplayName", "FirstName");
        claimProperties1.put("FriendlyName", "givenName");
        claimProperties1.put("Description", "Sampletest1");
        externalClaim1.setClaimProperties(claimProperties1);

        Claim externalClaim2 = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, "TestExternalClaimURI2",
                "http://wso2.org/claims/test2");
        Map<String, String> claimProperties2 = new HashMap<>();
        claimProperties2.put("DisplayName", "FirstName");
        claimProperties2.put("FriendlyName", "givenName");
        claimProperties2.put("Description", "Sampletest2");
        externalClaim2.setClaimProperties(claimProperties2);

        Claim externalClaim3 = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, "TestExternalClaimURI3",
                "http://wso2.org/claims/test3");
        Map<String, String> claimProperties3 = new HashMap<>();
        claimProperties3.put("DisplayName", "FirstName");
        claimProperties3.put("FriendlyName", "givenName");
        claimProperties3.put("Description", "Sampletest3");
        externalClaim3.setClaimProperties(claimProperties3);

        return new Object[][] {
                {
                        localClaim1, externalClaim1, TENANT_ID
                }, {
                        localClaim2, externalClaim2, TENANT_ID
                }, {
                        localClaim3, externalClaim3, TENANT_ID
                },
                };
    }

    @Test(dataProvider = "updateExternalClaim")
    public void testUpdateExternalClaim(Object localClaim, Object externalClaim, int tenantId, String updatedClaimURI)
            throws ClaimMetadataException {

        ClaimDialectDAO claimDialectDAO1 = new ClaimDialectDAO();
        ClaimDialect claimDialect1 = new ClaimDialect(EXTERNAL_CLAIM_DIALECT_URI);
        claimDialectDAO1.addClaimDialect(claimDialect1, TENANT_ID);

        ClaimDialectDAO claimDialectDAO2 = new ClaimDialectDAO();
        ClaimDialect claimDialect2 = new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
        claimDialectDAO2.addClaimDialect(claimDialect2, TENANT_ID);

        LocalClaimDAO localClaimDAO = new LocalClaimDAO();
        localClaimDAO.addLocalClaim(((LocalClaim) localClaim), tenantId);

        ExternalClaimDAO externalClaimDAO = new ExternalClaimDAO();
        externalClaimDAO.addExternalClaim((ExternalClaim) externalClaim, tenantId);

        localClaimDAO.addLocalClaim(new LocalClaim("http://wso2.org/claims/" + updatedClaimURI), tenantId);

        ((ExternalClaim) externalClaim).setMappedLocalClaim("http://wso2.org/claims/" + updatedClaimURI);

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "NewDisplayName");
        claimProperties.put("FriendlyName", "NewFriendlyName");
        claimProperties.put("Description", "NewSampleDescription");

        ((ExternalClaim) externalClaim).setClaimProperties(claimProperties);

        externalClaimDAO.updateExternalClaim(((ExternalClaim) externalClaim), tenantId);

        assertEquals(externalClaimDAO.getExternalClaims(((ExternalClaim) externalClaim).getClaimDialectURI(), tenantId)
                .size(), 1, "Fail to update external claim");

        assertEquals(externalClaimDAO.getExternalClaims(((ExternalClaim) externalClaim).getClaimDialectURI(), tenantId)
                        .get(0).getClaimProperties().get("Description"), "NewSampleDescription",
                "Fail to update external claim");

        assertEquals(externalClaimDAO.getExternalClaims(((ExternalClaim) externalClaim).getClaimDialectURI(), tenantId)
                .get(0).getClaimProperties().get("FriendlyName"), "NewFriendlyName", "Fail to update external claim");

        assertEquals(externalClaimDAO.getExternalClaims(((ExternalClaim) externalClaim).getClaimDialectURI(), tenantId)
                        .get(0).getClaimProperties().get("Description"), "NewSampleDescription",
                "Fail to update external claim");

        // Clean after test
        localClaimDAO.removeLocalClaim(((LocalClaim) localClaim).getClaimURI(), tenantId);
        externalClaimDAO.removeExternalClaim(((ExternalClaim) externalClaim).getClaimDialectURI(),
                ((ExternalClaim) externalClaim).getClaimURI(), tenantId);

        claimDialectDAO1.removeClaimDialect(claimDialect1, tenantId);
        claimDialectDAO2.removeClaimDialect(claimDialect2, tenantId);
    }

    @DataProvider(name = "updateExternalClaim")
    public Object[][] testUpdateExternalClaimsData() {
        Claim externalClaim1 = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, "TestExternalClaimURI1",
                "http://wso2.org/claims/test1");
        Map<String, String> claimProperties1 = new HashMap<>();
        claimProperties1.put("DisplayName", "FirstName");
        claimProperties1.put("FriendlyName", "givenName");
        claimProperties1.put("Description", "Sampletest1");
        externalClaim1.setClaimProperties(claimProperties1);

        Claim externalClaim2 = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, "TestExternalClaimURI2",
                "http://wso2.org/claims/test2");
        Map<String, String> claimProperties2 = new HashMap<>();
        claimProperties2.put("DisplayName", "FirstName");
        claimProperties2.put("FriendlyName", "givenName");
        claimProperties2.put("Description", "Sampletest2");
        externalClaim2.setClaimProperties(claimProperties2);

        Claim externalClaim3 = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, "TestExternalClaimURI3",
                "http://wso2.org/claims/test3");
        Map<String, String> claimProperties3 = new HashMap<>();
        claimProperties3.put("DisplayName", "FirstName");
        claimProperties3.put("FriendlyName", "givenName");
        claimProperties3.put("Description", "Sampletest3");
        externalClaim3.setClaimProperties(claimProperties3);

        return new Object[][] {
                {
                        localClaim1, externalClaim1, TENANT_ID, UPDATED_CLAIM_URI_1
                }, {
                        localClaim2, externalClaim2, TENANT_ID, UPDATED_CLAIM_URI_2
                }, {
                        localClaim3, externalClaim3, TENANT_ID, UPDATED_CLAIM_URI_3
                },
                };
    }

    @Test(dataProvider = "mappedLocalClaimDataProvider")
    public void testIsLocalClaimMappedWithinDialect(Object localClaim, Object externalClaim1,
                                                    Object externalClaim2, int tenantId) throws ClaimMetadataException {

        ClaimDialectDAO claimDialectDAO = new ClaimDialectDAO();
        ClaimDialect claimDialect1 = new ClaimDialect(EXTERNAL_CLAIM_DIALECT_URI);
        claimDialectDAO.addClaimDialect(claimDialect1, TENANT_ID);

        ClaimDialect claimDialect2 = new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI);
        claimDialectDAO.addClaimDialect(claimDialect2, TENANT_ID);

        LocalClaimDAO localClaimDAO = new LocalClaimDAO();
        localClaimDAO.addLocalClaim(((LocalClaim) localClaim), tenantId);

        ExternalClaimDAO externalClaimDAO = new ExternalClaimDAO();

        ExternalClaim claim1 = (ExternalClaim) externalClaim1;
        assertFalse(externalClaimDAO.isLocalClaimMappedWithinDialect(claim1.getMappedLocalClaim(),
                claim1.getClaimDialectURI(), tenantId));
        externalClaimDAO.addExternalClaim(claim1, tenantId);

        ExternalClaim claim2 = (ExternalClaim) externalClaim2;
        assertTrue(externalClaimDAO.isLocalClaimMappedWithinDialect(claim2.getMappedLocalClaim(),
                claim2.getClaimDialectURI(), tenantId));

        claimDialectDAO.removeAllClaimDialects(tenantId);

    }

    @DataProvider(name = "mappedLocalClaimDataProvider")
    public Object[][] isLocalClaimMappedWithinDialectData() {
        
        ExternalClaim externalClaim1 = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, "TestExternalClaimURI1",
                localClaim1.getClaimURI());
        ExternalClaim externalClaim2 = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, "testExternalClaimURI2",
                localClaim1.getClaimURI());
        return new Object[][]{
                {localClaim1, externalClaim1, externalClaim2, TENANT_ID},
        };
    }
}
