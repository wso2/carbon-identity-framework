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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.Claim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
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
public class UnifiedClaimMetadataManagerTest {

    private UnifiedClaimMetadataManager claimMetadataManager;
    private SystemDefaultClaimMetadataManager systemDefaultClaimMetadataManager;
    private DBBasedClaimMetadataManager dbBasedClaimMetadataManager;
    private CacheBackedDBBasedClaimMetadataManager cacheBackedDBBasedClaimMetadataManager;
    private MockedStatic<IdentityClaimManagementServiceDataHolder> dataHolderStaticMock;
    private MockedStatic<IdentityUtil> identityUtilStaticMock;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilStaticMock;
    private MockedStatic<Utils> utilsStaticMock;
    private final String LOCAL_CLAIM_DIALECT = "http://wso2.org/claims";
    private final String EXT_CLAIM_DIALECT_1 = "http://abc.org";
    private final String EXT_CLAIM_DIALECT_2 = "http://def.org";
    private final String NON_EXISTING_CLAIM_DIALECT = "http://nonexisting.org";
    private final String LOCAL_CLAIM_1 = "http://wso2.org/claims/username";
    private final String LOCAL_CLAIM_2 = "http://wso2.org/claims/email";
    private final String LOCAL_CLAIM_3 = "http://wso2.org/claims/country";
    private final String LOCAL_CLAIM_4 = "http://wso2.org/claims/identity/accountLocked";
    private final String LOCAL_CLAIM_5 = "http://wso2.org/claims/customClaim5";
    private final String EXT_CLAIM_DIALECT_1_CLAIM_1 = "http://abc.org/claim1";
    private final String EXT_CLAIM_DIALECT_1_CLAIM_2 = "http://abc.org/claim2";
    private final String EXT_CLAIM_DIALECT_1_CLAIM_3 = "http://abc.org/claim3";
    private final String EXT_CLAIM_DIALECT_2_CLAIM_1 = "http://def.org/claim1";
    private final String EXT_CLAIM_DIALECT_2_CLAIM_2 = "http://def.org/claim2";
    private final String FOO_TENANT_DOMAIN = "foo.com";
    private final int FOO_TENANT_ID = 1;

    @BeforeMethod
    public void setUp() throws Exception {

        dataHolderStaticMock = mockStatic(IdentityClaimManagementServiceDataHolder.class);
        identityUtilStaticMock = mockStatic(IdentityUtil.class);
        identityTenantUtilStaticMock = mockStatic(IdentityTenantUtil.class);
        utilsStaticMock = mockStatic(Utils.class);
        identityTenantUtilStaticMock.when(() -> IdentityTenantUtil.getTenantDomain(1)).thenReturn(FOO_TENANT_DOMAIN);
        identityTenantUtilStaticMock.when(() -> IdentityTenantUtil.getTenantId(FOO_TENANT_DOMAIN)).thenReturn(1);
        utilsStaticMock.when(() -> Utils.isClaimAndOIDCScopeInheritanceEnabled(FOO_TENANT_DOMAIN)).thenReturn(false);
        IdentityClaimManagementServiceDataHolder dataHolder = mock(IdentityClaimManagementServiceDataHolder.class);
        dataHolderStaticMock.when(IdentityClaimManagementServiceDataHolder::getInstance).thenReturn(dataHolder);
        ClaimConfig claimConfig = new ClaimConfig();
        when(dataHolder.getClaimConfig()).thenReturn(claimConfig);
        systemDefaultClaimMetadataManager = mock(SystemDefaultClaimMetadataManager.class);
        dbBasedClaimMetadataManager = mock(DBBasedClaimMetadataManager.class);
        cacheBackedDBBasedClaimMetadataManager = mock(CacheBackedDBBasedClaimMetadataManager.class);

        claimMetadataManager = new UnifiedClaimMetadataManager();
        setPrivateField(claimMetadataManager, "systemDefaultClaimMetadataManager", systemDefaultClaimMetadataManager);
        setPrivateField(claimMetadataManager, "dbBasedClaimMetadataManager", dbBasedClaimMetadataManager);
        setPrivateField(claimMetadataManager, "cacheBackedDBBasedClaimMetadataManager",
                cacheBackedDBBasedClaimMetadataManager);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {

        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void testGetClaimDialects() throws ClaimMetadataException {

        List<ClaimDialect> systemDefaultClaimDialects = new ArrayList<>();
        systemDefaultClaimDialects.add(new ClaimDialect(LOCAL_CLAIM_DIALECT));
        systemDefaultClaimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_1));
        systemDefaultClaimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_2));
        List<ClaimDialect> dbClaimDialects = new ArrayList<>();
        when(systemDefaultClaimMetadataManager.getClaimDialects(anyInt())).thenReturn(systemDefaultClaimDialects);
        when(dbBasedClaimMetadataManager.getClaimDialects(anyInt())).thenReturn(dbClaimDialects);
        List<ClaimDialect> result = claimMetadataManager.getClaimDialects(0);
        assertNotNull(result);
        List<String> claimDialectURIsInResult = result.stream()
                .map(ClaimDialect::getClaimDialectURI)
                .collect(Collectors.toList());
        assertEquals(claimDialectURIsInResult.size(), 3);
        assertTrue(claimDialectURIsInResult.contains(LOCAL_CLAIM_DIALECT));
        assertTrue(claimDialectURIsInResult.contains(EXT_CLAIM_DIALECT_1));
        assertTrue(claimDialectURIsInResult.contains(EXT_CLAIM_DIALECT_2));

        systemDefaultClaimDialects = new ArrayList<>();
        systemDefaultClaimDialects.add(new ClaimDialect(LOCAL_CLAIM_DIALECT));
        systemDefaultClaimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_1));
        dbClaimDialects = new ArrayList<>();
        dbClaimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_2));
        when(systemDefaultClaimMetadataManager.getClaimDialects(anyInt())).thenReturn(systemDefaultClaimDialects);
        when(dbBasedClaimMetadataManager.getClaimDialects(anyInt())).thenReturn(dbClaimDialects);
        result = claimMetadataManager.getClaimDialects(0);
        assertNotNull(result);
        claimDialectURIsInResult = result.stream()
                .map(ClaimDialect::getClaimDialectURI)
                .collect(Collectors.toList());
        assertEquals(claimDialectURIsInResult.size(), 3);
        assertTrue(claimDialectURIsInResult.contains(LOCAL_CLAIM_DIALECT));
        assertTrue(claimDialectURIsInResult.contains(EXT_CLAIM_DIALECT_1));
        assertTrue(claimDialectURIsInResult.contains(EXT_CLAIM_DIALECT_2));

        systemDefaultClaimDialects = new ArrayList<>();
        systemDefaultClaimDialects.add(new ClaimDialect(LOCAL_CLAIM_DIALECT));
        systemDefaultClaimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_1));
        dbClaimDialects = new ArrayList<>();
        dbClaimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_1));
        dbClaimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_2));
        when(systemDefaultClaimMetadataManager.getClaimDialects(anyInt())).thenReturn(systemDefaultClaimDialects);
        when(dbBasedClaimMetadataManager.getClaimDialects(anyInt())).thenReturn(dbClaimDialects);
        result = claimMetadataManager.getClaimDialects(0);
        assertNotNull(result);
        claimDialectURIsInResult = result.stream()
                .map(ClaimDialect::getClaimDialectURI)
                .collect(Collectors.toList());
        assertEquals(claimDialectURIsInResult.size(), 3);
        assertTrue(claimDialectURIsInResult.contains(LOCAL_CLAIM_DIALECT));
        assertTrue(claimDialectURIsInResult.contains(EXT_CLAIM_DIALECT_1));
        assertTrue(claimDialectURIsInResult.contains(EXT_CLAIM_DIALECT_2));
    }

    @Test
    public void testGetClaimDialect() throws ClaimMetadataException {

        ClaimDialect claimDialect = new ClaimDialect(LOCAL_CLAIM_DIALECT);
        when(systemDefaultClaimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 0))
                .thenReturn(Optional.of(claimDialect));
        when(dbBasedClaimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 0)).thenReturn(Optional.empty());
        Optional<ClaimDialect> result = claimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 0);
        assertTrue(result.isPresent());
        assertEquals(result.get().getClaimDialectURI(), LOCAL_CLAIM_DIALECT);

        claimDialect = new ClaimDialect(EXT_CLAIM_DIALECT_1);
        when(systemDefaultClaimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 0)).thenReturn(Optional.empty());
        when(dbBasedClaimMetadataManager.getClaimDialect(EXT_CLAIM_DIALECT_1, 0))
                .thenReturn(Optional.of(claimDialect));
        result = claimMetadataManager.getClaimDialect(EXT_CLAIM_DIALECT_1, 0);
        assertTrue(result.isPresent());
        assertEquals(result.get().getClaimDialectURI(), EXT_CLAIM_DIALECT_1);

        claimDialect = new ClaimDialect(EXT_CLAIM_DIALECT_2);
        when(systemDefaultClaimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 0))
                .thenReturn(Optional.of(claimDialect));
        when(dbBasedClaimMetadataManager.getClaimDialect(EXT_CLAIM_DIALECT_2, 0))
                .thenReturn(Optional.of(claimDialect));
        result = claimMetadataManager.getClaimDialect(EXT_CLAIM_DIALECT_2, 0);
        assertTrue(result.isPresent());
        assertEquals(result.get().getClaimDialectURI(), EXT_CLAIM_DIALECT_2);

        when(systemDefaultClaimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 0))
                .thenReturn(Optional.empty());
        when(dbBasedClaimMetadataManager.getClaimDialect(NON_EXISTING_CLAIM_DIALECT, 0))
                .thenReturn(Optional.empty());
        result = claimMetadataManager.getClaimDialect(NON_EXISTING_CLAIM_DIALECT, 0);
        assertFalse(result.isPresent());
    }

    @Test
    public void testAddClaimDialect() throws ClaimMetadataException {

        ClaimDialect claimDialect = new ClaimDialect(EXT_CLAIM_DIALECT_1);
        claimMetadataManager.addClaimDialect(claimDialect, 0);
        verify(dbBasedClaimMetadataManager, times(1)).addClaimDialect(claimDialect, 0);
    }

    @Test
    public void testRenameClaimDialect() throws ClaimMetadataException {

        ClaimDialect oldClaimDialect = new ClaimDialect(EXT_CLAIM_DIALECT_1);
        ClaimDialect newClaimDialect = new ClaimDialect(EXT_CLAIM_DIALECT_2);
        claimMetadataManager.renameClaimDialect(oldClaimDialect, newClaimDialect, 0);
        verify(dbBasedClaimMetadataManager, times(1)).renameClaimDialect(oldClaimDialect, newClaimDialect, 0);
    }

    @Test
    public void testRemoveClaimDialect() throws ClaimMetadataException {

        ClaimDialect claimDialect = new ClaimDialect(EXT_CLAIM_DIALECT_1);
        claimMetadataManager.removeClaimDialect(claimDialect, 0);
        verify(dbBasedClaimMetadataManager, times(1)).removeClaimDialect(claimDialect, 0);
    }

    @Test
    public void testGetLocalClaims() throws ClaimMetadataException {

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());
        List<LocalClaim> localClaimsInSystem = new ArrayList<>();
        localClaimsInSystem.add(new LocalClaim(LOCAL_CLAIM_1, new ArrayList<>(), claimProperties));
        LocalClaim localClaim2InSystem = new LocalClaim(LOCAL_CLAIM_2, new ArrayList<>(), claimProperties);
        localClaimsInSystem.add(localClaim2InSystem);

        Map<String, String> claimPropertiesForLocalClaimsInDB = new HashMap<>();
        claimPropertiesForLocalClaimsInDB.put(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_SHARED_PROFILE.getName());
        List<LocalClaim> localClaimsInDB = new ArrayList<>();
        localClaimsInDB.add(new LocalClaim(LOCAL_CLAIM_3, new ArrayList<>(), claimPropertiesForLocalClaimsInDB));
        localClaimsInDB.add(new LocalClaim(LOCAL_CLAIM_4));
        LocalClaim duplicatedLocalClaim = new LocalClaim(LOCAL_CLAIM_2);
        duplicatedLocalClaim.setMappedAttributes(new ArrayList<>());
        duplicatedLocalClaim.getMappedAttributes().add(new AttributeMapping("PRIMARY", "username"));
        localClaimsInDB.add(duplicatedLocalClaim);

        when(systemDefaultClaimMetadataManager.getLocalClaims(0)).thenReturn(localClaimsInSystem);
        when(dbBasedClaimMetadataManager.getLocalClaims(0)).thenReturn(localClaimsInDB);
        when(systemDefaultClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_2, 0)).thenReturn(
                Optional.of(localClaim2InSystem));
        List<LocalClaim> result = claimMetadataManager.getLocalClaims(0);
        assertNotNull(result);
        assertEquals(result.size(), 4);
        List<String> localClaimURIsInResult = result.stream()
                .map(LocalClaim::getClaimURI)
                .collect(Collectors.toList());
        assertTrue(localClaimURIsInResult.contains(LOCAL_CLAIM_1));
        LocalClaim localClaim1 = result.stream()
                .filter(localClaim -> localClaim.getClaimURI().equals(LOCAL_CLAIM_1))
                .findFirst()
                .orElse(null);
        assertEquals(localClaim1.getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD),
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());
        LocalClaim localClaim2 = result.stream()
                .filter(localClaim -> localClaim.getClaimURI().equals(LOCAL_CLAIM_2))
                .findFirst()
                .orElse(null);
        assertNotNull(localClaim2);
        assertEquals(localClaim2.getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD),
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());
        assertEquals(localClaim2.getMappedAttributes().size(), 1);
        assertEquals(localClaim2.getMappedAttributes().get(0).getUserStoreDomain(), "PRIMARY");
        assertEquals(localClaim2.getMappedAttributes().get(0).getAttributeName(), "username");
        assertTrue(localClaimURIsInResult.contains(LOCAL_CLAIM_3));
        LocalClaim localClaim3 = result.stream()
                .filter(localClaim -> localClaim.getClaimURI().equals(LOCAL_CLAIM_3))
                .findFirst()
                .orElse(null);
        assertEquals(localClaim3.getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD),
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_SHARED_PROFILE.getName());
        assertTrue(localClaimURIsInResult.contains(LOCAL_CLAIM_4));
        LocalClaim localClaim4 = result.stream()
                .filter(localClaim -> localClaim.getClaimURI().equals(LOCAL_CLAIM_4))
                .findFirst()
                .orElse(null);
        assertEquals(localClaim4.getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD),
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());
    }

    @Test
    public void testGetLocalClaim() throws ClaimMetadataException {

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());
        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1, new ArrayList<>(), claimProperties);
        when(systemDefaultClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 0))
                .thenReturn(Optional.of(localClaim));
        when(dbBasedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 0)).thenReturn(Optional.empty());
        Optional<LocalClaim> result = claimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 0);
        assertTrue(result.isPresent());
        assertEquals(result.get().getClaimURI(), LOCAL_CLAIM_1);
        assertEquals(result.get().getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD),
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());

        localClaim = new LocalClaim(LOCAL_CLAIM_2, new ArrayList<>(), claimProperties);
        when(systemDefaultClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_2, 0)).thenReturn(Optional.empty());
        when(dbBasedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_2, 0))
                .thenReturn(Optional.of(localClaim));
        result = claimMetadataManager.getLocalClaim(LOCAL_CLAIM_2, 0);
        assertTrue(result.isPresent());
        assertEquals(result.get().getClaimURI(), LOCAL_CLAIM_2);
        assertEquals(result.get().getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD),
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());

        LocalClaim localClaimInSystem = new LocalClaim(LOCAL_CLAIM_3, new ArrayList<>(), claimProperties);
        when(systemDefaultClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_3, 0))
                .thenReturn(Optional.of(localClaimInSystem));
        when(systemDefaultClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_3, 0))
                .thenReturn(Optional.of(localClaimInSystem));
        LocalClaim localClaimInDB = new LocalClaim(LOCAL_CLAIM_3);
        localClaimInDB.setMappedAttributes(new ArrayList<>());
        localClaimInDB.getMappedAttributes().add(new AttributeMapping("PRIMARY", "country"));
        when(dbBasedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_3, 0))
                .thenReturn(Optional.of(localClaimInDB));
        result = claimMetadataManager.getLocalClaim(LOCAL_CLAIM_3, 0);
        assertTrue(result.isPresent());
        assertEquals(result.get().getClaimURI(), LOCAL_CLAIM_3);
        assertEquals(result.get().getMappedAttributes().size(), 1);
        assertEquals(result.get().getMappedAttributes().get(0).getUserStoreDomain(), "PRIMARY");
        assertEquals(result.get().getMappedAttributes().get(0).getAttributeName(), "country");
        assertEquals(result.get().getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD),
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());

        when(systemDefaultClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_4, 0)).thenReturn(Optional.empty());
        when(dbBasedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_4, 0)).thenReturn(Optional.empty());
        result = claimMetadataManager.getLocalClaim(LOCAL_CLAIM_4, 0);
        assertFalse(result.isPresent());

        localClaim = new LocalClaim(LOCAL_CLAIM_5);
        when(systemDefaultClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_5, 0)).thenReturn(Optional.empty());
        when(dbBasedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_5, 0)).thenReturn(Optional.of(localClaim));
        result = claimMetadataManager.getLocalClaim(LOCAL_CLAIM_5, 0);
        assertTrue(result.isPresent());
        assertEquals(result.get().getClaimURI(), LOCAL_CLAIM_5);
        assertEquals(result.get().getClaimProperty(ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD),
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());
    }

    @Test
    public void testAddLocalClaim() throws ClaimMetadataException {

        ClaimDialect claimDialect = new ClaimDialect(LOCAL_CLAIM_DIALECT);
        when(dbBasedClaimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 0))
                .thenReturn(Optional.of(claimDialect));
        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        claimMetadataManager.addLocalClaim(localClaim, 0);
        verify(dbBasedClaimMetadataManager, times(1)).addLocalClaim(localClaim, 0);
        clearInvocations(dbBasedClaimMetadataManager);

        when(dbBasedClaimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 0)).thenReturn(Optional.empty());
        when(systemDefaultClaimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 0))
                .thenReturn(Optional.of(claimDialect));
        claimMetadataManager.addLocalClaim(localClaim, 0);
        verify(dbBasedClaimMetadataManager, times(1)).addClaimDialect(claimDialect, 0);
        verify(dbBasedClaimMetadataManager, times(1)).addLocalClaim(localClaim, 0);
    }

    @Test
    public void testUpdateLocalClaim() throws ClaimMetadataException {

        LocalClaim localClaimInSystem = new LocalClaim(LOCAL_CLAIM_1);
        localClaimInSystem.setMappedAttributes(new ArrayList<>());
        localClaimInSystem.getMappedAttributes().add(new AttributeMapping("PRIMARY", "username"));
        localClaimInSystem.setClaimProperties(new HashMap<>());
        localClaimInSystem.getClaimProperties().put("Property1", "Value1");

        LocalClaim localClaimInDB = new LocalClaim(LOCAL_CLAIM_1);
        localClaimInDB.setMappedAttributes(new ArrayList<>());
        localClaimInDB.getMappedAttributes().add(new AttributeMapping("PRIMARY", "username"));
        localClaimInDB.getMappedAttributes().add(new AttributeMapping("SECONDARY", "user_name"));
        localClaimInDB.setClaimProperties(new HashMap<>());
        localClaimInDB.getClaimProperties().put("Property1", "Value1");

        LocalClaim updatedLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        updatedLocalClaim.setMappedAttributes(new ArrayList<>());
        updatedLocalClaim.getMappedAttributes().add(new AttributeMapping("PRIMARY", "username"));
        updatedLocalClaim.getMappedAttributes().add(new AttributeMapping("SECONDARY", "user_id"));
        updatedLocalClaim.setClaimProperties(new HashMap<>());
        updatedLocalClaim.getClaimProperties().put("Property2", "Value2");

        when(systemDefaultClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 0))
                .thenReturn(Optional.of(localClaimInSystem));
        when(dbBasedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 0)).thenReturn(Optional.empty());
        claimMetadataManager.updateLocalClaim(updatedLocalClaim, 0);
        verify(dbBasedClaimMetadataManager, times(1)).addLocalClaim(updatedLocalClaim, 0);
        clearInvocations(dbBasedClaimMetadataManager);

        when(systemDefaultClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 0))
                .thenReturn(Optional.of(localClaimInSystem));
        when(dbBasedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 0))
                .thenReturn(Optional.of(localClaimInDB));
        claimMetadataManager.updateLocalClaim(updatedLocalClaim, 0);
        verify(dbBasedClaimMetadataManager, times(1)).updateLocalClaim(updatedLocalClaim, 0);
        clearInvocations(dbBasedClaimMetadataManager);

        when(systemDefaultClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 0)).thenReturn(null);
        when(dbBasedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 0))
                .thenReturn(Optional.of(localClaimInDB));
        claimMetadataManager.updateLocalClaim(updatedLocalClaim, 0);
        verify(dbBasedClaimMetadataManager, times(1)).updateLocalClaim(updatedLocalClaim, 0);
    }

    @Test
    public void testUpdateLocalClaimMappings() throws ClaimMetadataException {

        List<LocalClaim> updatedLocalClaims = new ArrayList<>();
        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        updatedLocalClaims.add(localClaim);
        when(dbBasedClaimMetadataManager.getClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, 1))
                .thenReturn(Optional.empty());
        when(systemDefaultClaimMetadataManager.getClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, 1))
                .thenReturn(Optional.of(new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI)));
        List<LocalClaim> finalUpdatedLocalClaims = updatedLocalClaims;
        assertThrows(ClaimMetadataClientException.class, () -> {
            claimMetadataManager.updateLocalClaimMappings(finalUpdatedLocalClaims, 1, "PRIMARY");
        });
        verify(dbBasedClaimMetadataManager, times(1)).addClaimDialect(any(), anyInt());
        clearInvocations(dbBasedClaimMetadataManager);

        LocalClaim localClaimInSystem1 = new LocalClaim(LOCAL_CLAIM_1);
        localClaimInSystem1.setMappedAttributes(new ArrayList<>());
        localClaimInSystem1.getMappedAttributes().add(new AttributeMapping("PRIMARY", "username"));
        localClaimInSystem1.setClaimProperties(new HashMap<>());
        localClaimInSystem1.getClaimProperties().put("Property1", "Value1");

        LocalClaim localClaimInSystem2 = new LocalClaim(LOCAL_CLAIM_2);
        localClaimInSystem2.setMappedAttributes(new ArrayList<>());
        localClaimInSystem2.getMappedAttributes().add(new AttributeMapping("PRIMARY", "email"));
        localClaimInSystem2.getMappedAttributes().add(new AttributeMapping("SECONDARY", "email"));
        localClaimInSystem2.setClaimProperties(new HashMap<>());
        localClaimInSystem2.getClaimProperties().put("Property2", "Value2");

        List<LocalClaim> localClaimsInSystem = new ArrayList<>();
        localClaimsInSystem.add(localClaimInSystem1);
        localClaimsInSystem.add(localClaimInSystem2);
        when(systemDefaultClaimMetadataManager.getLocalClaims(1)).thenReturn(localClaimsInSystem);

        LocalClaim localClaimInDB1 = new LocalClaim(LOCAL_CLAIM_3);
        localClaimInDB1.setMappedAttributes(new ArrayList<>());
        localClaimInDB1.getMappedAttributes().add(new AttributeMapping("SECONDARY", "country"));
        localClaimInDB1.setClaimProperties(new HashMap<>());
        localClaimInDB1.getClaimProperties().put("Property3", "Value3");

        LocalClaim localClaimInDB2 = new LocalClaim(LOCAL_CLAIM_4);

        List<LocalClaim> localClaimsInDB = new ArrayList<>();
        localClaimsInDB.add(localClaimInDB1);
        localClaimsInDB.add(localClaimInDB2);
        when(dbBasedClaimMetadataManager.getLocalClaims(1)).thenReturn(localClaimsInDB);

        when(systemDefaultClaimMetadataManager.getClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI, 1))
                .thenReturn(Optional.of(new ClaimDialect(ClaimConstants.LOCAL_CLAIM_DIALECT_URI)));

        LocalClaim updatedLocalClaim1 = new LocalClaim(LOCAL_CLAIM_1);
        updatedLocalClaim1.setMappedAttributes(new ArrayList<>());
        updatedLocalClaim1.getMappedAttributes().add(new AttributeMapping("SECONDARY", "user_name"));

        LocalClaim updatedLocalClaim2 = new LocalClaim(LOCAL_CLAIM_2);
        updatedLocalClaim2.setMappedAttributes(new ArrayList<>());
        updatedLocalClaim2.getMappedAttributes().add(new AttributeMapping("PRIMARY", "email"));
        updatedLocalClaim2.getMappedAttributes().add(new AttributeMapping("SECONDARY", "email_address"));

        LocalClaim updatedLocalClaim3 = new LocalClaim(LOCAL_CLAIM_3);
        updatedLocalClaim3.setMappedAttributes(new ArrayList<>());
        updatedLocalClaim3.getMappedAttributes().add(new AttributeMapping("SECONDARY", "user_country"));

        LocalClaim updatedLocalClaim4 = new LocalClaim(LOCAL_CLAIM_4);
        updatedLocalClaim4.setMappedAttributes(new ArrayList<>());
        updatedLocalClaim4.getMappedAttributes().add(new AttributeMapping("SECONDARY", "isAccountLocked"));

        updatedLocalClaims = new ArrayList<>();
        updatedLocalClaims.add(updatedLocalClaim1);
        updatedLocalClaims.add(updatedLocalClaim2);
        updatedLocalClaims.add(updatedLocalClaim3);
        updatedLocalClaims.add(updatedLocalClaim4);

        claimMetadataManager.updateLocalClaimMappings(updatedLocalClaims, 1, "SECONDARY");
    }

    @Test
    public void testRemoveLocalClaim() throws ClaimMetadataException {

        claimMetadataManager.removeLocalClaim(LOCAL_CLAIM_1, 0);
        verify(dbBasedClaimMetadataManager, times(1)).removeLocalClaim(LOCAL_CLAIM_1, 0);
    }

    @Test
    public void testGetExternalClaims() throws ClaimMetadataException {

        List<ExternalClaim> externalClaimsInSystem = new ArrayList<>();
        externalClaimsInSystem.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1));
        externalClaimsInSystem.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_2, LOCAL_CLAIM_2));
        when(systemDefaultClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1))
                .thenReturn(externalClaimsInSystem);

        List<ExternalClaim> externalClaimsInDB = new ArrayList<>();
        externalClaimsInDB.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_2, LOCAL_CLAIM_4));
        externalClaimsInDB.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_3, LOCAL_CLAIM_3));
        when(dbBasedClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1))
                .thenReturn(externalClaimsInDB);

        List<ExternalClaim> result = claimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1);
        assertNotNull(result);
        assertEquals(result.size(), 3);
        List<String> externalClaimURIsInResult = result.stream()
                .map(ExternalClaim::getClaimURI)
                .collect(Collectors.toList());
        assertTrue(externalClaimURIsInResult.contains(EXT_CLAIM_DIALECT_1_CLAIM_1));
        assertTrue(externalClaimURIsInResult.contains(EXT_CLAIM_DIALECT_1_CLAIM_2));
        assertTrue(externalClaimURIsInResult.contains(EXT_CLAIM_DIALECT_1_CLAIM_3));
        List<String> mappedLocalClaimURIsInResult = result.stream()
                .map(ExternalClaim::getMappedLocalClaim)
                .collect(Collectors.toList());
        assertTrue(mappedLocalClaimURIsInResult.contains(LOCAL_CLAIM_1));
        assertTrue(mappedLocalClaimURIsInResult.contains(LOCAL_CLAIM_3));
        assertTrue(mappedLocalClaimURIsInResult.contains(LOCAL_CLAIM_4));
    }

    @Test
    public void testGetExternalClaim() throws ClaimMetadataException {

        ExternalClaim externalClaimsInSystem = new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1);
        when(systemDefaultClaimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1))
                .thenReturn(Optional.of(externalClaimsInSystem));
        when(dbBasedClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1))
                .thenReturn(Collections.emptyList());
        Optional<ExternalClaim> result = claimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1);
        assertTrue(result.isPresent());
        assertEquals(result.get().getClaimURI(), EXT_CLAIM_DIALECT_1_CLAIM_1);
        assertEquals(result.get().getMappedLocalClaim(), LOCAL_CLAIM_1);

        ExternalClaim externalClaimsInDB = new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1);
        when(systemDefaultClaimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1))
                .thenReturn(Optional.empty());
        when(dbBasedClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1))
                .thenReturn(Collections.singletonList(externalClaimsInDB));
        result = claimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1);
        assertTrue(result.isPresent());
        assertEquals(result.get().getClaimURI(), EXT_CLAIM_DIALECT_1_CLAIM_1);
        assertEquals(result.get().getMappedLocalClaim(), LOCAL_CLAIM_1);

        externalClaimsInSystem = new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1);
        externalClaimsInDB = new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_2);
        when(systemDefaultClaimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1))
                .thenReturn(Optional.of(externalClaimsInSystem));
        when(dbBasedClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1))
                .thenReturn(Collections.singletonList(externalClaimsInDB));
        result = claimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1);
        assertTrue(result.isPresent());
        assertEquals(result.get().getClaimURI(), EXT_CLAIM_DIALECT_1_CLAIM_1);
        assertEquals(result.get().getMappedLocalClaim(), LOCAL_CLAIM_2);

        when(systemDefaultClaimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1))
                .thenReturn(Optional.empty());
        when(dbBasedClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1))
                .thenReturn(Collections.emptyList());
        result = claimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1);
        assertFalse(result.isPresent());
    }

    @Test
    public void testAddExternalClaim() throws ClaimMetadataException {

        when(systemDefaultClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 1))
                .thenReturn(Optional.of(new LocalClaim(LOCAL_CLAIM_1)));
        when(systemDefaultClaimMetadataManager.getClaimDialect(EXT_CLAIM_DIALECT_1, 1))
                .thenReturn(Optional.of(new ClaimDialect(EXT_CLAIM_DIALECT_1)));
        when(systemDefaultClaimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 1))
                .thenReturn(Optional.of(new ClaimDialect(LOCAL_CLAIM_DIALECT)));

        ExternalClaim externalClaim = new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1);
        claimMetadataManager.addExternalClaim(externalClaim, 1);
        verify(dbBasedClaimMetadataManager, times(2)).addClaimDialect(any(), anyInt());
        verify(dbBasedClaimMetadataManager, times(1)).addLocalClaim(any(), anyInt());
        verify(dbBasedClaimMetadataManager, times(1)).addExternalClaim(externalClaim, 1);
        clearInvocations(dbBasedClaimMetadataManager);

        when(dbBasedClaimMetadataManager.getClaimDialect(EXT_CLAIM_DIALECT_1, 1))
                .thenReturn(Optional.of(new ClaimDialect(EXT_CLAIM_DIALECT_1)));
        claimMetadataManager.addExternalClaim(externalClaim, 1);
        verify(dbBasedClaimMetadataManager, times(1)).addClaimDialect(any(), anyInt());
        verify(dbBasedClaimMetadataManager, times(1)).addLocalClaim(any(), anyInt());
        verify(dbBasedClaimMetadataManager, times(1)).addExternalClaim(externalClaim, 1);
        clearInvocations(dbBasedClaimMetadataManager);

        when(dbBasedClaimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 1))
                .thenReturn(Optional.of(new ClaimDialect(LOCAL_CLAIM_DIALECT)));
        claimMetadataManager.addExternalClaim(externalClaim, 1);
        verify(dbBasedClaimMetadataManager, never()).addClaimDialect(any(), anyInt());
        verify(dbBasedClaimMetadataManager, times(1)).addLocalClaim(any(), anyInt());
        verify(dbBasedClaimMetadataManager, times(1)).addExternalClaim(externalClaim, 1);
        clearInvocations(dbBasedClaimMetadataManager);

        when(dbBasedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, 1))
                .thenReturn(Optional.of(new LocalClaim(LOCAL_CLAIM_1)));
        claimMetadataManager.addExternalClaim(externalClaim, 1);
        verify(dbBasedClaimMetadataManager, never()).addClaimDialect(any(), anyInt());
        verify(dbBasedClaimMetadataManager, never()).addLocalClaim(any(), anyInt());
        verify(dbBasedClaimMetadataManager, times(1)).addExternalClaim(externalClaim, 1);
        clearInvocations(dbBasedClaimMetadataManager);
    }

    @Test
    public void testUpdateExternalClaim() throws ClaimMetadataException {

        ExternalClaim updatedExternalClaim = new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_2);
        when(dbBasedClaimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1))
                .thenReturn(Optional.empty());
        claimMetadataManager.updateExternalClaim(updatedExternalClaim, 1);
        verify(dbBasedClaimMetadataManager, times(1)).addExternalClaim(updatedExternalClaim, 1);
        clearInvocations(dbBasedClaimMetadataManager);

        when(dbBasedClaimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1))
                .thenReturn(Optional.of(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1)));
        claimMetadataManager.updateExternalClaim(updatedExternalClaim, 1);
        verify(dbBasedClaimMetadataManager, times(1))
                .updateExternalClaim(updatedExternalClaim, 1);
    }

    @Test
    public void testRemoveExternalClaim() throws ClaimMetadataException {

        claimMetadataManager.removeExternalClaim(EXT_CLAIM_DIALECT_1_CLAIM_1, EXT_CLAIM_DIALECT_1, 1);
        verify(dbBasedClaimMetadataManager, times(1))
                .removeExternalClaim(EXT_CLAIM_DIALECT_1_CLAIM_1, EXT_CLAIM_DIALECT_1, 1);
    }

    @Test
    public void testIsMappedLocalClaim() throws ClaimMetadataException {

        List<ClaimDialect> claimDialects = new ArrayList<>();
        claimDialects.add(new ClaimDialect(LOCAL_CLAIM_DIALECT));
        claimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_1));
        claimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_2));
        when(systemDefaultClaimMetadataManager.getClaimDialects(1)).thenReturn(claimDialects);

        List<ExternalClaim> claimsOfExternalDialect1 = new ArrayList<>();
        claimsOfExternalDialect1.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1));
        claimsOfExternalDialect1.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_2, LOCAL_CLAIM_2));
        when(systemDefaultClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1))
                .thenReturn(claimsOfExternalDialect1);

        List<ExternalClaim> claimsOfExternalDialect2 = new ArrayList<>();
        claimsOfExternalDialect2.add(new ExternalClaim(EXT_CLAIM_DIALECT_2, EXT_CLAIM_DIALECT_2_CLAIM_1, LOCAL_CLAIM_2));
        claimsOfExternalDialect2.add(new ExternalClaim(EXT_CLAIM_DIALECT_2, EXT_CLAIM_DIALECT_2_CLAIM_2, LOCAL_CLAIM_3));
        when(systemDefaultClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_2, 1))
                .thenReturn(claimsOfExternalDialect2);

        assertTrue(claimMetadataManager.isMappedLocalClaim(LOCAL_CLAIM_1, 1));
        assertTrue(claimMetadataManager.isMappedLocalClaim(LOCAL_CLAIM_2, 1));
        assertFalse(claimMetadataManager.isMappedLocalClaim(LOCAL_CLAIM_4, 1));
    }

    @Test
    public void testRemoveClaimMappingAttributes() throws ClaimMetadataException {

        claimMetadataManager.removeClaimMappingAttributes(1, "PRIMARY");
        verify(dbBasedClaimMetadataManager, times(1)).removeClaimMappingAttributes(1, "PRIMARY");
    }

    @Test
    public void testRemoveAllClaimDialects() throws ClaimMetadataException {

        claimMetadataManager.removeAllClaimDialects(1);
        verify(cacheBackedDBBasedClaimMetadataManager, times(1)).removeAllClaimDialects(1);
    }

    @Test
    public void testGetMappedExternalClaims() throws ClaimMetadataException {

        List<ClaimDialect> claimDialects = new ArrayList<>();
        claimDialects.add(new ClaimDialect(LOCAL_CLAIM_DIALECT));
        claimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_1));
        claimDialects.add(new ClaimDialect(EXT_CLAIM_DIALECT_2));
        when(systemDefaultClaimMetadataManager.getClaimDialects(1)).thenReturn(claimDialects);

        List<ExternalClaim> claimsOfExternalDialect1 = new ArrayList<>();
        claimsOfExternalDialect1.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1));
        claimsOfExternalDialect1.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_2, LOCAL_CLAIM_2));
        when(systemDefaultClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1))
                .thenReturn(claimsOfExternalDialect1);

        List<ExternalClaim> claimsOfExternalDialect2 = new ArrayList<>();
        claimsOfExternalDialect2.add(new ExternalClaim(EXT_CLAIM_DIALECT_2, EXT_CLAIM_DIALECT_2_CLAIM_1, LOCAL_CLAIM_2));
        claimsOfExternalDialect2.add(new ExternalClaim(EXT_CLAIM_DIALECT_2, EXT_CLAIM_DIALECT_2_CLAIM_2, LOCAL_CLAIM_3));
        when(systemDefaultClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_2, 1))
                .thenReturn(claimsOfExternalDialect2);

        List<Claim> result = claimMetadataManager.getMappedExternalClaims(LOCAL_CLAIM_1, 1);
        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getClaimURI(), EXT_CLAIM_DIALECT_1_CLAIM_1);

        result = claimMetadataManager.getMappedExternalClaims(LOCAL_CLAIM_2, 1);
        assertNotNull(result);
        assertEquals(result.size(), 2);
        List<String> claimURIsInResult = result.stream()
                .map(Claim::getClaimURI)
                .collect(Collectors.toList());
        assertTrue(claimURIsInResult.contains(EXT_CLAIM_DIALECT_1_CLAIM_2));
        assertTrue(claimURIsInResult.contains(EXT_CLAIM_DIALECT_2_CLAIM_1));

        result = claimMetadataManager.getMappedExternalClaims(LOCAL_CLAIM_4, 1);
        assertNotNull(result);
        assertEquals(result.size(), 0);
    }

    @Test
    public void testIsLocalClaimMappedWithinDialect() throws ClaimMetadataException {

        List<ExternalClaim> claimsOfExternalDialect = new ArrayList<>();
        claimsOfExternalDialect.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1));
        claimsOfExternalDialect.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_2, LOCAL_CLAIM_2));
        when(systemDefaultClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1))
                .thenReturn(claimsOfExternalDialect);

        assertTrue(claimMetadataManager.isLocalClaimMappedWithinDialect(LOCAL_CLAIM_1, EXT_CLAIM_DIALECT_1, 1));
        assertFalse(claimMetadataManager.isLocalClaimMappedWithinDialect(LOCAL_CLAIM_3, EXT_CLAIM_DIALECT_1, 1));
    }

//    @Test
//    public void testIsSystemDefaultClaimDialect() throws ClaimMetadataException {
//
//        when(systemDefaultClaimMetadataManager.getClaimDialect(LOCAL_CLAIM_DIALECT, 1))
//                .thenReturn(Optional.of(new ClaimDialect(LOCAL_CLAIM_DIALECT)));
//        when(systemDefaultClaimMetadataManager.getClaimDialect(EXT_CLAIM_DIALECT_1, 1))
//                .thenReturn(Optional.of(new ClaimDialect(EXT_CLAIM_DIALECT_1)));
//        when(dbBasedClaimMetadataManager.getClaimDialect(EXT_CLAIM_DIALECT_1, 1))
//                .thenReturn(Optional.of(new ClaimDialect(EXT_CLAIM_DIALECT_1)));
//        when(dbBasedClaimMetadataManager.getClaimDialect(EXT_CLAIM_DIALECT_2, 1))
//                .thenReturn(Optional.of(new ClaimDialect(EXT_CLAIM_DIALECT_2)));
//
//        assertTrue(claimMetadataManager.isSystemDefaultClaimDialect(LOCAL_CLAIM_DIALECT, 1));
//        assertTrue(claimMetadataManager.isSystemDefaultClaimDialect(EXT_CLAIM_DIALECT_1, 1));
//        assertFalse(claimMetadataManager.isSystemDefaultClaimDialect(EXT_CLAIM_DIALECT_2, 1));
//    }

//    @Test
//    public void testIsSystemDefaultLocalClaim() throws ClaimMetadataException {
//
//        List<LocalClaim> localClaimsInSystem = new ArrayList<>();
//        localClaimsInSystem.add(new LocalClaim(LOCAL_CLAIM_1));
//        localClaimsInSystem.add(new LocalClaim(LOCAL_CLAIM_2));
//        when(systemDefaultClaimMetadataManager.getLocalClaims(1)).thenReturn(localClaimsInSystem);
//
//        List<LocalClaim> localClaimsInDB = new ArrayList<>();
//        localClaimsInDB.add(new LocalClaim(LOCAL_CLAIM_3));
//        localClaimsInDB.add(new LocalClaim(LOCAL_CLAIM_4));
//        when(dbBasedClaimMetadataManager.getLocalClaims(1)).thenReturn(localClaimsInDB);
//
//        assertTrue(claimMetadataManager.isSystemDefaultLocalClaim(LOCAL_CLAIM_1, 1));
//        assertFalse(claimMetadataManager.isSystemDefaultLocalClaim(LOCAL_CLAIM_3, 1));
//    }

//    @Test
//    public void testIsSystemDefaultExternalClaim() throws ClaimMetadataException {
//
//        List<ExternalClaim> claimsOfExternalDialect = new ArrayList<>();
//        claimsOfExternalDialect.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, LOCAL_CLAIM_1));
//        claimsOfExternalDialect.add(new ExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_2, LOCAL_CLAIM_2));
//        when(systemDefaultClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT_1, 1))
//                .thenReturn(claimsOfExternalDialect);
//
//        assertTrue(claimMetadataManager.isSystemDefaultExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_1, 1));
//        assertFalse(claimMetadataManager.isSystemDefaultExternalClaim(EXT_CLAIM_DIALECT_1, EXT_CLAIM_DIALECT_1_CLAIM_3, 1));
//    }

    @AfterMethod
    public void tearDown() {

        dataHolderStaticMock.close();
        identityUtilStaticMock.close();
        identityTenantUtilStaticMock.close();
        utilsStaticMock.close();
    }
}
