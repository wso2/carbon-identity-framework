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

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimDialectDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ExternalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.LocalClaimDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataServerException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.user.api.UserStoreException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

@WithCarbonHome
@Test
public class DBBasedClaimMetadataManagerTest {

    private DBBasedClaimMetadataManager claimMetadataManager;
    private final ClaimDialectDAO mockClaimDialectDAO = Mockito.mock(ClaimDialectDAO.class);
    private final LocalClaimDAO mockLocalClaimDAO = Mockito.mock(LocalClaimDAO.class);
    private final ExternalClaimDAO mockExternalClaimDAO = Mockito.mock(ExternalClaimDAO.class);
    private final String LOCAL_CLAIM_DIALECT = "http://wso2.org/claims";
    private final String EXT_CLAIM_DIALECT_1 = "http://abc.org";
    private final String EXT_CLAIM_DIALECT_2 = "http://def.org";
    private final String LOCAL_CLAIM_1 = "http://wso2.org/claims/username";
    private final String LOCAL_CLAIM_2 = "http://wso2.org/claims/email";
    private final String LOCAL_CLAIM_3 = "http://wso2.org/claims/country";
    private final String LOCAL_CLAIM_4 = "http://wso2.org/claims/identity/accountLocked";
    private final String LOCAL_CLAIM_5 = "http://wso2.org/claims/identity/emailVerified";
    private final String LOCAL_CLAIM_6 = "http://wso2.org/claims/identity/lastLoginTime";
    private final String EXT_CLAIM_DIALECT_1_CLAIM_1 = "http://abc.org/claim1";
    private final String EXT_CLAIM_DIALECT_1_CLAIM_2 = "http://abc.org/claim2";
    private final String EXT_CLAIM_DIALECT_1_CLAIM_3 = "http://abc.org/claim3";
    private final String EXT_CLAIM_DIALECT_2_CLAIM_1 = "http://def.org/claim1";
    private final String EXT_CLAIM_DIALECT_2_CLAIM_2 = "http://def.org/claim2";
    private final String NON_EXISTING_CLAIM_DIALECT_URI = "http://nonexisting.org";
    private final String TEST_USER_STORE_DOMAIN = "TEST_USER_STORE_DOMAIN";

    @BeforeClass
    public void setUp() throws Exception {

        claimMetadataManager = new DBBasedClaimMetadataManager();

        setPrivateField(claimMetadataManager, "claimDialectDAO", mockClaimDialectDAO);
        setPrivateField(claimMetadataManager, "localClaimDAO", mockLocalClaimDAO);
        setPrivateField(claimMetadataManager, "externalClaimDAO", mockExternalClaimDAO);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testGetClaimDialects() throws ClaimMetadataException {

        List<ClaimDialect> claimDialects = new ArrayList<>();
        claimDialects.add(new ClaimDialect(LOCAL_CLAIM_DIALECT));
        claimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_1));
        claimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_2));
        when(mockClaimDialectDAO.getClaimDialects(anyInt())).thenReturn(claimDialects);

        List<ClaimDialect> returnedClaimDialects = claimMetadataManager.getClaimDialects(1);
        List<String> claimDialectURIs = returnedClaimDialects.stream()
                .map(ClaimDialect::getClaimDialectURI)
                .collect(Collectors.toList());
        assertNotNull(returnedClaimDialects);
        assertEquals(returnedClaimDialects.size(), 3);
        assertTrue(claimDialectURIs.contains(LOCAL_CLAIM_DIALECT));
        assertTrue(claimDialectURIs.contains(EXT_CLAIM_DIALECT_1));
        assertTrue(claimDialectURIs.contains(EXT_CLAIM_DIALECT_2));
    }

    @Test
    public void testGetClaimDialect() throws ClaimMetadataException {

        List<ClaimDialect> claimDialects = new ArrayList<>();
        claimDialects.add(new ClaimDialect(LOCAL_CLAIM_DIALECT));
        claimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_1));
        claimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_2));
        when(mockClaimDialectDAO.getClaimDialects(anyInt())).thenReturn(claimDialects);

        Optional<ClaimDialect> returnedClaimDialect = claimMetadataManager.getClaimDialect(EXT_CLAIM_DIALECT_2, 1);
        assertTrue(returnedClaimDialect.isPresent());
        assertEquals(returnedClaimDialect.get().getClaimDialectURI(), EXT_CLAIM_DIALECT_2);

        returnedClaimDialect = claimMetadataManager.getClaimDialect(NON_EXISTING_CLAIM_DIALECT_URI, 1);
        assertFalse(returnedClaimDialect.isPresent());

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getClaimDialect(null, 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getClaimDialect("", 1);
        });
    }

    @Test
    public void testAddClaimDialect() throws ClaimMetadataException {

        ClaimDialect newClaimDialect = new ClaimDialect(EXT_CLAIM_DIALECT_1);
        claimMetadataManager.addClaimDialect(newClaimDialect, 1);
        verify(mockClaimDialectDAO, times(1)).addClaimDialect(newClaimDialect, 1);
    }

    @Test
    public void testRemoveClaimDialect() throws ClaimMetadataException {

        ClaimDialect claimDialect = new ClaimDialect(EXT_CLAIM_DIALECT_1);
        claimMetadataManager.removeClaimDialect(claimDialect, 1);
        verify(mockClaimDialectDAO, times(1)).removeClaimDialect(claimDialect, 1);
    }

    @Test
    public void testGetLocalClaims() throws ClaimMetadataException {

        List<LocalClaim> localClaims = new ArrayList<>();
        localClaims.add(new LocalClaim(LOCAL_CLAIM_1));
        localClaims.add(new LocalClaim(LOCAL_CLAIM_2));
        when(mockLocalClaimDAO.getLocalClaims(anyInt())).thenReturn(localClaims);

        List<LocalClaim> returnedLocalClaims = claimMetadataManager.getLocalClaims(1);
        List<String> localClaimURIs = returnedLocalClaims.stream()
                .map(LocalClaim::getClaimURI)
                .collect(Collectors.toList());
        assertNotNull(returnedLocalClaims);
        assertEquals(returnedLocalClaims.size(), 2);
        assertTrue(localClaimURIs.contains(LOCAL_CLAIM_1));
        assertTrue(localClaimURIs.contains(LOCAL_CLAIM_2));
    }

    @Test
    public void testGetLocalClaim() throws ClaimMetadataException {

        List<ClaimDialect> claimDialects = new ArrayList<>();
        claimDialects.add(new ClaimDialect(LOCAL_CLAIM_DIALECT));
        when(mockClaimDialectDAO.getClaimDialects(anyInt())).thenReturn(claimDialects);

        List<LocalClaim> localClaims = new ArrayList<>();
        localClaims.add(new LocalClaim(LOCAL_CLAIM_1));
        localClaims.add(new LocalClaim(LOCAL_CLAIM_2));
        localClaims.add(new LocalClaim(LOCAL_CLAIM_3));
        when(mockLocalClaimDAO.getLocalClaims(anyInt())).thenReturn(localClaims);

        Optional<LocalClaim> returnedLocalClaim = claimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 1);
        assertTrue(returnedLocalClaim.isPresent());
        assertEquals(returnedLocalClaim.get().getClaimURI(), LOCAL_CLAIM_1);

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getLocalClaim(null, 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getLocalClaim("", 1);
        });

        returnedLocalClaim = claimMetadataManager.getLocalClaim(LOCAL_CLAIM_4, 1);
        assertFalse(returnedLocalClaim.isPresent());
    }

    @Test
    public void testGetExternalClaims() throws ClaimMetadataException {

        List<ExternalClaim> externalClaims = new ArrayList<>();
        externalClaims.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1));
        externalClaims.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_2, LOCAL_CLAIM_2));
        externalClaims.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_3, LOCAL_CLAIM_3));
        when(mockExternalClaimDAO.getExternalClaims(EXT_CLAIM_DIALECT_1, 1)).thenReturn(externalClaims);

        List<ExternalClaim> returnedExternalClaims = claimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1);
        List<String> externalClaimURIs = returnedExternalClaims.stream()
                .map(ExternalClaim::getClaimURI)
                .collect(Collectors.toList());
        assertNotNull(returnedExternalClaims);
        assertEquals(returnedExternalClaims.size(), 3);
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_1_CLAIM_1));
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_1_CLAIM_2));
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_1_CLAIM_3));
    }

    @Test
    public void testGetExternalClaim() throws ClaimMetadataException {

        List<ExternalClaim> externalClaims = new ArrayList<>();
        externalClaims.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1));
        externalClaims.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_2, LOCAL_CLAIM_2));
        externalClaims.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_3, LOCAL_CLAIM_3));
        when(mockExternalClaimDAO.getExternalClaims(EXT_CLAIM_DIALECT_1, 1)).thenReturn(externalClaims);

        Optional<ExternalClaim> returnedExternalClaim = claimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1,
                EXT_CLAIM_DIALECT_1_CLAIM_2, 1);
        assertTrue(returnedExternalClaim.isPresent());
        assertEquals(returnedExternalClaim.get().getClaimURI(), EXT_CLAIM_DIALECT_1_CLAIM_2);

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, null, 1);
        });

        assertThrows(ClaimMetadataException.class, () -> {
            claimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, "", 1);
        });
    }

    @Test
    public void testAddLocalClaim() throws ClaimMetadataException {

        LocalClaim newLocalClaim = new LocalClaim(LOCAL_CLAIM_4);
        claimMetadataManager.addLocalClaim(newLocalClaim, 1);
        verify(mockLocalClaimDAO, times(1)).addLocalClaim(newLocalClaim, 1);
    }

    @Test
    public void testUpdateLocalClaim() throws ClaimMetadataException {

        LocalClaim updatedLocalClaim = new LocalClaim(LOCAL_CLAIM_5);
        claimMetadataManager.updateLocalClaim(updatedLocalClaim, 1);
        verify(mockLocalClaimDAO, times(1)).updateLocalClaim(updatedLocalClaim, 1);
    }

    @Test
    public void testUpdateLocalClaimMappings() throws ClaimMetadataException {

        List<LocalClaim> updatedLocalClaims = new ArrayList<>();
        updatedLocalClaims.add(new LocalClaim(LOCAL_CLAIM_6));
        claimMetadataManager.updateLocalClaimMappings(updatedLocalClaims, 1, TEST_USER_STORE_DOMAIN);
        verify(mockLocalClaimDAO, times(1)).updateLocalClaimMappings(updatedLocalClaims, 1, TEST_USER_STORE_DOMAIN);
    }

    @Test
    public void testRemoveLocalClaim() throws ClaimMetadataException {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_4);
        claimMetadataManager.removeLocalClaim(localClaim.getClaimURI(), 1);
        verify(mockLocalClaimDAO, times(1)).removeLocalClaim(localClaim.getClaimURI(), 1);
    }

    @Test
    public void testRemoveClaimMappingAttributes() throws ClaimMetadataException, UserStoreException {

        claimMetadataManager.removeClaimMappingAttributes(1, TEST_USER_STORE_DOMAIN);
        verify(mockLocalClaimDAO, times(1)).deleteClaimMappingAttributes(1, TEST_USER_STORE_DOMAIN);

        doThrow(new UserStoreException("User store error")).when(mockLocalClaimDAO)
                .deleteClaimMappingAttributes(1, TEST_USER_STORE_DOMAIN);
        assertThrows(ClaimMetadataServerException.class, () -> {
            claimMetadataManager.removeClaimMappingAttributes(1, TEST_USER_STORE_DOMAIN);
        });
    }

    @Test
    public void testAddExternalClaim() throws ClaimMetadataException {

        ExternalClaim externalClaim = new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1);
        claimMetadataManager.addExternalClaim(externalClaim, 1);
        verify(mockExternalClaimDAO, times(1)).addExternalClaim(externalClaim, 1);
    }

    @Test
    public void testUpdateExternalClaim() throws ClaimMetadataException {

        ExternalClaim updatedExternalClaim = new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_2, LOCAL_CLAIM_4);
        claimMetadataManager.updateExternalClaim(updatedExternalClaim, 1);
        verify(mockExternalClaimDAO, times(1)).updateExternalClaim(updatedExternalClaim, 1);
    }

    @Test
    public void testRemoveExternalClaim() throws ClaimMetadataException {

        claimMetadataManager.removeExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1);
        verify(mockExternalClaimDAO, times(1))
                .removeExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1);
    }

    @Test
    public void testGetMappedExternalClaims() throws ClaimMetadataException {

        List<Claim> externalClaims = new ArrayList<>();
        externalClaims.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1));
        externalClaims.add(new ExternalClaim(EXT_CLAIM_DIALECT_2, EXT_CLAIM_DIALECT_2_CLAIM_2, LOCAL_CLAIM_1));
        when(mockLocalClaimDAO.fetchMappedExternalClaims(LOCAL_CLAIM_1, 1)).thenReturn(externalClaims);

        List<Claim> returnedExternalClaims = claimMetadataManager.getMappedExternalClaims(LOCAL_CLAIM_1, 1);
        List<String> externalClaimURIs = returnedExternalClaims.stream()
                .map(Claim::getClaimURI)
                .collect(Collectors.toList());
        assertNotNull(returnedExternalClaims);
        assertEquals(returnedExternalClaims.size(), 2);
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_1_CLAIM_1));
        assertTrue(externalClaimURIs.contains(EXT_CLAIM_DIALECT_2_CLAIM_2));
    }

    @Test
    public void testRenameClaimDialect() throws ClaimMetadataException {

        ClaimDialect oldClaimDialect = new ClaimDialect(EXT_CLAIM_DIALECT_1);
        ClaimDialect newClaimDialect = new ClaimDialect(EXT_CLAIM_DIALECT_2);

        clearInvocations(mockExternalClaimDAO, mockClaimDialectDAO);
        claimMetadataManager.renameClaimDialect(oldClaimDialect, newClaimDialect, 1);
        verify(mockClaimDialectDAO, times(1)).renameClaimDialect(oldClaimDialect, newClaimDialect, 1);
    }

    @Test
    public void testRemoveAllClaimDialects() throws ClaimMetadataException {

        claimMetadataManager.removeAllClaimDialects(1);
        verify(mockClaimDialectDAO, times(1)).removeAllClaimDialects(1);
    }

    @Test
    public void testIsMappedLocalClaim() throws ClaimMetadataException {

        when(mockExternalClaimDAO.isMappedLocalClaim(LOCAL_CLAIM_1, 1)).thenReturn(true);
        assertTrue(claimMetadataManager.isMappedLocalClaim(LOCAL_CLAIM_1, 1));
    }

    @Test
    public void testIsLocalClaimMappedWithinDialect() throws ClaimMetadataException {

        when(mockExternalClaimDAO.isLocalClaimMappedWithinDialect(LOCAL_CLAIM_1, EXT_CLAIM_DIALECT_1, 1)).thenReturn(true);
        assertTrue(claimMetadataManager.isLocalClaimMappedWithinDialect(LOCAL_CLAIM_1, EXT_CLAIM_DIALECT_1, 1));
    }
}
