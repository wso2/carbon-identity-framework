/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.claim.metadata.mgt;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimKey;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

@Test
public class SystemDefaultClaimMetadataManagerTest {

    private SystemDefaultClaimMetadataManager claimMetadataManager;
    private MockedStatic<IdentityClaimManagementServiceDataHolder> dataHolderStaticMock;
    private MockedStatic<IdentityUtil> identityUtilStaticMock;
    private final String LOCAL_CLAIM_DIALECT = "http://wso2.org/claims";
    private final String LOCAL_CLAIM_1 = "http://wso2.org/claims/username";
    private final String LOCAL_CLAIM_2 = "http://wso2.org/claims/email";
    private final String LOCAL_CLAIM_3 = "http://wso2.org/claims/country";
    private final String LOCAL_CLAIM_4 = "http://wso2.org/claims/identity/accountLocked";
    private final String LOCAL_CLAIM_5 = "http://wso2.org/claims/identity/emailVerified";
    private final String LOCAL_CLAIM_6 = "http://wso2.org/claims/identity/lastLoginTime";
    private final String EXT_CLAIM_DIALECT_1 = "http://abc.org";
    private final String EXT_CLAIM_DIALECT_1_CLAIM_1 = "http://abc.org/claim1";
    private final String EXT_CLAIM_DIALECT_1_CLAIM_2 = "http://abc.org/claim2";
    private final String EXT_CLAIM_DIALECT_1_CLAIM_3 = "http://abc.org/claim3";
    private final String EXT_CLAIM_DIALECT_1_CLAIM_4 = "http://abc.org/claim4";
    private final String EXT_CLAIM_DIALECT_1_CLAIM_5 = "http://abc.org/claim5";
    private final String EXT_CLAIM_DIALECT_2 = "http://def.org";
    private final String EXT_CLAIM_DIALECT_2_CLAIM_1 = "http://def.org/claim1";
    private final String EXT_CLAIM_DIALECT_2_CLAIM_2 = "http://def.org/claim2";
    private final String EXT_CLAIM_DIALECT_2_CLAIM_3 = "http://def.org/claim3";
    private final String NON_EXISTING_CLAIM_DIALECT_URI = "http://nonexisting.org";
    private final String PRIMARY_DOMAIN = "PRIMARY";

    @BeforeClass
    public void setUp() {

        dataHolderStaticMock = mockStatic(IdentityClaimManagementServiceDataHolder.class);
        identityUtilStaticMock = mockStatic(IdentityUtil.class);

        IdentityClaimManagementServiceDataHolder dataHolder = mock(IdentityClaimManagementServiceDataHolder.class);

        dataHolderStaticMock.when(IdentityClaimManagementServiceDataHolder::getInstance).thenReturn(dataHolder);
        when(dataHolder.getClaimConfig()).thenReturn(getClaimConfigWithDummyData());
        identityUtilStaticMock.when(IdentityUtil::getPrimaryDomainName).thenReturn(PRIMARY_DOMAIN);
        claimMetadataManager = new SystemDefaultClaimMetadataManager();
    }

    @Test
    public void testGetClaimDialects() throws ClaimMetadataException {
        List<ClaimDialect> claimDialects = claimMetadataManager.getClaimDialects(1);
        List<String> dialectURIs = claimDialects.stream()
                .map(ClaimDialect::getClaimDialectURI)
                .collect(Collectors.toList());

        assertNotNull(claimDialects);
        assertEquals(claimDialects.size(), 3);
        assertTrue(dialectURIs.contains(ClaimConstants.LOCAL_CLAIM_DIALECT_URI));
        assertTrue(dialectURIs.contains(EXT_CLAIM_DIALECT_1));
        assertTrue(dialectURIs.contains(EXT_CLAIM_DIALECT_2));
    }

    @Test
    public void testGetClaimDialect() throws ClaimMetadataException {
        String claimDialectURI = LOCAL_CLAIM_DIALECT;
        Optional<ClaimDialect> claimDialect = claimMetadataManager.getClaimDialect(claimDialectURI, 1);
        assertTrue(claimDialect.isPresent());
        assertEquals(claimDialectURI, claimDialect.get().getClaimDialectURI());

        Optional<ClaimDialect> nonExistingClaimDialect = claimMetadataManager.getClaimDialect(NON_EXISTING_CLAIM_DIALECT_URI, 1);
        assertFalse(nonExistingClaimDialect.isPresent());

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getClaimDialect(null, 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getClaimDialect("", 1);
        });
    }

    @Test
    public void testGetLocalClaims() throws ClaimMetadataException {

        List<LocalClaim> claims = claimMetadataManager.getLocalClaims(1);
        List<String> claimURIs = claims.stream()
                .map(LocalClaim::getClaimURI)
                .collect(Collectors.toList());
        assertNotNull(claims);
        assertEquals(claims.size(), 6);
        assertTrue(claimURIs.contains(LOCAL_CLAIM_1));
        assertTrue(claimURIs.contains(LOCAL_CLAIM_2));
        assertTrue(claimURIs.contains(LOCAL_CLAIM_3));
        assertTrue(claimURIs.contains(LOCAL_CLAIM_4));
        assertTrue(claimURIs.contains(LOCAL_CLAIM_5));
        assertTrue(claimURIs.contains(LOCAL_CLAIM_6));
    }

    @Test void getLocalClaim() throws ClaimMetadataException {

        Optional<LocalClaim> claim = claimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 1);
        assertTrue(claim.isPresent());
        assertEquals(LOCAL_CLAIM_1, claim.get().getClaimURI());

        Optional<LocalClaim> nonExistingClaim = claimMetadataManager.getLocalClaim(NON_EXISTING_CLAIM_DIALECT_URI, 1);
        assertFalse(nonExistingClaim.isPresent());

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getLocalClaim(null, 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getLocalClaim("", 1);
        });
    }

    @Test
    public void getExternalClaims() throws ClaimMetadataException {

        List<ExternalClaim> externalClaims = claimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1);
        List<String> externalClaimURIs = externalClaims.stream()
                .map(ExternalClaim::getClaimURI)
                .collect(Collectors.toList());

        assertNotNull(externalClaims);
        assertEquals(externalClaims.size(), 5);
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_1_CLAIM_1));
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_1_CLAIM_2));
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_1_CLAIM_3));
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_1_CLAIM_4));
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_1_CLAIM_5));

        List<ExternalClaim> externalClaimsForNonExistingDialect = claimMetadataManager.getExternalClaims(
                NON_EXISTING_CLAIM_DIALECT_URI, 1);
        assertNotNull(externalClaimsForNonExistingDialect);
        assertEquals(externalClaimsForNonExistingDialect.size(), 0);

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getExternalClaims(null, 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getExternalClaims("", 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getExternalClaims(LOCAL_CLAIM_DIALECT, 1);
        });
    }

    @Test
    public void getExternalClaim() throws ClaimMetadataException {

        Optional<ExternalClaim> externalClaim = claimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1,
                EXT_CLAIM_DIALECT_1_CLAIM_1, 1);
        assertTrue(externalClaim.isPresent());
        assertEquals(EXT_CLAIM_DIALECT_1_CLAIM_1, externalClaim.get().getClaimURI());

        String nonExistingExternalClaimURI = "http://nonexisting.org/nonExistingClaim";
        Optional<ExternalClaim> nonExistingExternalClaim = claimMetadataManager.getExternalClaim(NON_EXISTING_CLAIM_DIALECT_URI,
                nonExistingExternalClaimURI, 1);
        assertFalse(nonExistingExternalClaim.isPresent());

        nonExistingExternalClaim = claimMetadataManager.getExternalClaim(
                EXT_CLAIM_DIALECT_1, nonExistingExternalClaimURI, 1);
        assertFalse(nonExistingExternalClaim.isPresent());

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getExternalClaim(LOCAL_CLAIM_DIALECT, LOCAL_CLAIM_1, 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, null, 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getExternalClaim(null, EXT_CLAIM_DIALECT_1_CLAIM_1, 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, "", 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getExternalClaim("", EXT_CLAIM_DIALECT_1_CLAIM_1, 1);
        });
    }

    @Test
    public void testGetMappedExternalClaims() throws ClaimMetadataException {

        List<org.wso2.carbon.identity.claim.metadata.mgt.model.Claim> externalClaims = claimMetadataManager
                .getMappedExternalClaims(LOCAL_CLAIM_2, 1);
        List<String> externalClaimURIs = externalClaims.stream()
                .map(ExternalClaim.class::cast)
                .map(ExternalClaim::getClaimURI)
                .collect(Collectors.toList());
        assertNotNull(externalClaims);
        assertEquals(externalClaims.size(), 2);
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_1_CLAIM_1));
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_2_CLAIM_1));

        externalClaims = claimMetadataManager.getMappedExternalClaims(
                "http://wso2.org/claims/nonExistingClaim", 1);
        assertNotNull(externalClaims);
        assertEquals(externalClaims.size(), 0);

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getMappedExternalClaims(null, 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getMappedExternalClaims("", 1);
        });
    }

    @Test
    public void testIsMappedLocalClaim() throws ClaimMetadataException {

        assertTrue(claimMetadataManager.isMappedLocalClaim(LOCAL_CLAIM_1, 1));
        assertFalse(claimMetadataManager.isMappedLocalClaim(LOCAL_CLAIM_6, 1));
        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.isMappedLocalClaim(null, 1);
        });
        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.isMappedLocalClaim("", 1);
        });
    }

    @Test
    public void testIsLocalClaimMappedWithinDialect() throws ClaimMetadataException {

        assertTrue(claimMetadataManager.isLocalClaimMappedWithinDialect(LOCAL_CLAIM_1, EXT_CLAIM_DIALECT_1, 1));
        assertFalse(claimMetadataManager.isLocalClaimMappedWithinDialect(LOCAL_CLAIM_6, EXT_CLAIM_DIALECT_1, 1));
        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.isLocalClaimMappedWithinDialect(LOCAL_CLAIM_6, "", 1);
        });
        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.isLocalClaimMappedWithinDialect(LOCAL_CLAIM_6, null, 1);
        });
        assertFalse(claimMetadataManager.isLocalClaimMappedWithinDialect(LOCAL_CLAIM_6, NON_EXISTING_CLAIM_DIALECT_URI, 1));
        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.isLocalClaimMappedWithinDialect(null, EXT_CLAIM_DIALECT_1, 1);
        });
        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.isLocalClaimMappedWithinDialect("", EXT_CLAIM_DIALECT_1, 1);
        });
    }

    @AfterClass
    public void tearDown() {

        dataHolderStaticMock.close();
        identityUtilStaticMock.close();
    }

    private ClaimConfig getClaimConfigWithDummyData() {

        ClaimConfig claimConfig = new ClaimConfig();
        Map<ClaimKey, ClaimMapping> claims = new HashMap<>();
        Map<ClaimKey, Map<String, String>> propertyHolder = new HashMap<>();
        List<ClaimKey> claimKeys = Arrays.asList(
                new ClaimKey(LOCAL_CLAIM_1, LOCAL_CLAIM_DIALECT),
                new ClaimKey(LOCAL_CLAIM_2, LOCAL_CLAIM_DIALECT),
                new ClaimKey(LOCAL_CLAIM_3, LOCAL_CLAIM_DIALECT),
                new ClaimKey(LOCAL_CLAIM_4, LOCAL_CLAIM_DIALECT),
                new ClaimKey(LOCAL_CLAIM_5, LOCAL_CLAIM_DIALECT),
                new ClaimKey(LOCAL_CLAIM_6, LOCAL_CLAIM_DIALECT),
                new ClaimKey(EXT_CLAIM_DIALECT_1_CLAIM_1, EXT_CLAIM_DIALECT_1),
                new ClaimKey(EXT_CLAIM_DIALECT_1_CLAIM_2, EXT_CLAIM_DIALECT_1),
                new ClaimKey(EXT_CLAIM_DIALECT_1_CLAIM_3, EXT_CLAIM_DIALECT_1),
                new ClaimKey(EXT_CLAIM_DIALECT_1_CLAIM_4, EXT_CLAIM_DIALECT_1),
                new ClaimKey(EXT_CLAIM_DIALECT_1_CLAIM_5, EXT_CLAIM_DIALECT_1),
                new ClaimKey(EXT_CLAIM_DIALECT_2_CLAIM_1, EXT_CLAIM_DIALECT_2),
                new ClaimKey(EXT_CLAIM_DIALECT_2_CLAIM_2, EXT_CLAIM_DIALECT_2),
                new ClaimKey(EXT_CLAIM_DIALECT_2_CLAIM_3, EXT_CLAIM_DIALECT_2)
        );
        List<ClaimMapping> claimMappings = Arrays.asList(
                new ClaimMapping(new Claim(), "username"),
                new ClaimMapping(new Claim(), "email"),
                new ClaimMapping(new Claim(), "country"),
                new ClaimMapping(new Claim(), "accountLocked"),
                new ClaimMapping(new Claim(), "emailVerified"),
                new ClaimMapping(new Claim(), "lastLoginTime"),
                new ClaimMapping(new Claim(), null),
                new ClaimMapping(new Claim(), null),
                new ClaimMapping(new Claim(), null),
                new ClaimMapping(new Claim(), null),
                new ClaimMapping(new Claim(), null),
                new ClaimMapping(new Claim(), null),
                new ClaimMapping(new Claim(), null),
                new ClaimMapping(new Claim(), null)
        );

        for (int i = 0; i < claimKeys.size(); i++) {
            Map<String, String> properties = new HashMap<>();
            properties.put("property" + (i * 2 + 1), "value" + (i * 2 + 1));
            properties.put("property" + (i * 2 + 2), "value" + (i * 2 + 2));
            properties.put(ClaimConstants.MAPPED_LOCAL_CLAIM_PROPERTY, claimKeys.get(i%5).getClaimUri());
            properties.put(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                    ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());
            propertyHolder.put(claimKeys.get(i), properties);
        }
        for (int i = 0; i < claimKeys.size(); i++) {
            claims.put(claimKeys.get(i), claimMappings.get(i));
        }
        claimConfig.setClaimMap(claims);
        claimConfig.setPropertyHolderMap(propertyHolder);
        return claimConfig;
    }
}
