/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.LOCAL_CLAIM_DIALECT_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.REQUIRED_PROPERTY;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY;
import static org.wso2.carbon.identity.testutil.Whitebox.setInternalState;

@WithCarbonHome
@Test
public class ClaimMetadataManagementServiceImplTest {

    private static final String LOCAL_CLAIM_DIALECT = "http://wso2.org/claims";
    private static final String LOCAL_CLAIM_1 = "http://wso2.org/claims/username";
    private static final String LOCAL_CLAIM_2 = "http://wso2.org/claims/email";
    private static final String EXTERNAL_CLAIM_DIALECT_URI = "https://abc.org";
    private static final String EXTERNAL_CLAIM_URI = "test";
    private static final String MAPPED_LOCAL_CLAIM_URI = "http://wso2.org/claims/test";
    private static final String PRIMARY_DOMAIN = "PRIMARY";
    private static final String USERNAME_ATTRIBUTE = "username";

    private final ExternalClaim externalClaim = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, EXTERNAL_CLAIM_URI,
            MAPPED_LOCAL_CLAIM_URI);

    private ClaimMetadataManagementService service;
    private UnifiedClaimMetadataManager unifiedClaimMetadataManager;
    private MockedStatic<IdentityClaimManagementServiceDataHolder> dataHolderStaticMock;
    private MockedStatic<IdentityUtil> identityUtilStaticMock;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<ClaimMetadataEventPublisherProxy> claimMetadataEventPublisherProxy;
    private IdentityClaimManagementServiceDataHolder dataHolder;

    @BeforeMethod
    public void setup() throws Exception {

        dataHolderStaticMock = mockStatic(IdentityClaimManagementServiceDataHolder.class);
        identityUtilStaticMock = mockStatic(IdentityUtil.class);
        dataHolder = mock(IdentityClaimManagementServiceDataHolder.class);
        dataHolderStaticMock.when(IdentityClaimManagementServiceDataHolder::getInstance).thenReturn(dataHolder);

        unifiedClaimMetadataManager = Mockito.mock(UnifiedClaimMetadataManager.class);
        service = new ClaimMetadataManagementServiceImpl();
        setInternalState(service, "unifiedClaimMetadataManager", unifiedClaimMetadataManager);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        claimMetadataEventPublisherProxy = mockStatic(ClaimMetadataEventPublisherProxy.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SUPER_TENANT_ID);
        claimMetadataEventPublisherProxy.when(ClaimMetadataEventPublisherProxy::getInstance)
                .thenReturn(mock(ClaimMetadataEventPublisherProxy.class));
    }

    @Test
    public void testAddExternalClaim() throws Exception {

        when(unifiedClaimMetadataManager.getExternalClaims(anyString(), anyInt())).thenReturn(new ArrayList<>());
        List<ClaimDialect> claimDialects = new ArrayList<>();
        claimDialects.add(new ClaimDialect(EXTERNAL_CLAIM_DIALECT_URI));
        when(unifiedClaimMetadataManager.getClaimDialects(anyInt())).thenReturn(claimDialects);
        when(unifiedClaimMetadataManager.getLocalClaims(anyInt()))
                .thenReturn(Collections.singletonList(new LocalClaim(MAPPED_LOCAL_CLAIM_URI)));

        service.addExternalClaim(externalClaim, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).addExternalClaim(any(), anyInt());

        when(unifiedClaimMetadataManager.getExternalClaims(anyString(), anyInt()))
                .thenReturn(Collections.singletonList(externalClaim));
        assertThrows(ClaimMetadataException.class, () -> {
            service.addExternalClaim(externalClaim, SUPER_TENANT_DOMAIN_NAME);
        });

        when(unifiedClaimMetadataManager.isLocalClaimMappedWithinDialect(MAPPED_LOCAL_CLAIM_URI,
                EXTERNAL_CLAIM_DIALECT_URI,
                SUPER_TENANT_ID)).thenReturn(Boolean.TRUE);
        assertThrows(ClaimMetadataException.class, () -> {
            service.addExternalClaim(externalClaim, SUPER_TENANT_DOMAIN_NAME);
        });

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.addExternalClaim(null, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test(expectedExceptions = ClaimMetadataException.class)
    public void testAddExistingExternalClaim() throws Exception {

        when(unifiedClaimMetadataManager.getExternalClaims(anyString(), anyInt()))
                .thenReturn(Collections.singletonList(externalClaim));

        service.addExternalClaim(externalClaim, SUPER_TENANT_DOMAIN_NAME);
    }

    @Test(expectedExceptions = ClaimMetadataException.class)
    public void testAddExtClaimWithExistingLocalClaimMapping() throws Exception {

        when(unifiedClaimMetadataManager.getExternalClaims(anyString(), anyInt()))
                .thenReturn(Collections.singletonList(externalClaim));
        when(unifiedClaimMetadataManager.isLocalClaimMappedWithinDialect(MAPPED_LOCAL_CLAIM_URI,
                EXTERNAL_CLAIM_DIALECT_URI, SUPER_TENANT_ID)).thenReturn(Boolean.TRUE);

        service.addExternalClaim(externalClaim, SUPER_TENANT_DOMAIN_NAME);
    }

    @Test
    public void testGetClaimDialects() throws Exception {

        when(unifiedClaimMetadataManager.getExternalClaims(anyString(), anyInt())).thenReturn(new ArrayList<>());
        List<ClaimDialect> claimDialects = new ArrayList<>();
        claimDialects.add(new ClaimDialect(EXTERNAL_CLAIM_DIALECT_URI));
        when(unifiedClaimMetadataManager.getClaimDialects(anyInt())).thenReturn(claimDialects);

        service.getClaimDialects(SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).getClaimDialects(anyInt());
    }

    @Test
    public void testAddClaimDialect() throws ClaimMetadataException {

        ClaimDialect claimDialectToBeAdded = new ClaimDialect(EXTERNAL_CLAIM_DIALECT_URI);
        when(unifiedClaimMetadataManager.getClaimDialects(anyInt())).thenReturn(new ArrayList<>());
        service.addClaimDialect(claimDialectToBeAdded, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).addClaimDialect(any(), anyInt());

        when(unifiedClaimMetadataManager.getClaimDialects(anyInt()))
                .thenReturn(Collections.singletonList(claimDialectToBeAdded));
        assertThrows(ClaimMetadataException.class, () -> {
            service.addClaimDialect(claimDialectToBeAdded, SUPER_TENANT_DOMAIN_NAME);
        });

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.addClaimDialect(null, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testRenameClaimDialect() throws ClaimMetadataException {

        ClaimDialect newClaimDialect = new ClaimDialect(EXTERNAL_CLAIM_DIALECT_URI);
        ClaimDialect oldClaimDialect = new ClaimDialect(LOCAL_CLAIM_DIALECT_URI);
        service.renameClaimDialect(oldClaimDialect, newClaimDialect, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).renameClaimDialect(any(), any(), anyInt());
    }

    @Test
    public void testRemoveClaimDialect() throws ClaimMetadataException {

        ClaimDialect claimDialectToBeDeleted = new ClaimDialect(EXTERNAL_CLAIM_DIALECT_URI);
        service.removeClaimDialect(claimDialectToBeDeleted, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).removeClaimDialect(any(), anyInt());
    }

    @Test
    public void testGetLocalClaims() throws ClaimMetadataException {

        service.getLocalClaims(SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).getLocalClaims(anyInt());
    }

    @Test
    public void testAddLocalClaim() throws ClaimMetadataException {

        LocalClaim localClaimToBeAdded = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes()
                .add(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE));
        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());
        service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).addLocalClaim(any(), anyInt());

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt()))
                .thenReturn(Collections.singletonList(localClaimToBeAdded));
        assertThrows(ClaimMetadataClientException.class, () -> {
            service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testUpdateLocalClaim() throws ClaimMetadataException {

        LocalClaim localClaimToBeUpdated = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeUpdated.setMappedAttributes(new ArrayList<>());
        localClaimToBeUpdated.getMappedAttributes()
                .add(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE));

        LocalClaim existingLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        existingLocalClaim.setMappedAttributes(new ArrayList<>());
        existingLocalClaim.getMappedAttributes()
                .add(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE));

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));
        service.updateLocalClaim(localClaimToBeUpdated, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).updateLocalClaim(any(), anyInt());

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.empty());
        assertThrows(ClaimMetadataClientException.class, () -> {
            service.updateLocalClaim(localClaimToBeUpdated, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testUpdateLocalClaimMappings() throws ClaimMetadataException {

        LocalClaim localClaimToBeUpdated = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeUpdated.setMappedAttributes(new ArrayList<>());
        localClaimToBeUpdated.getMappedAttributes()
                .add(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE));
        List<LocalClaim> localClaimsList = new ArrayList<>();
        localClaimsList.add(localClaimToBeUpdated);

        service.updateLocalClaimMappings(localClaimsList, SUPER_TENANT_DOMAIN_NAME, PRIMARY_DOMAIN);
        verify(unifiedClaimMetadataManager, times(1))
                .updateLocalClaimMappings(any(), anyInt(), anyString());
    }

    @Test
    public void testRemoveLocalClaim() throws ClaimMetadataException {

        String localClaimURIToBeRemoved = LOCAL_CLAIM_1;
        service.removeLocalClaim(localClaimURIToBeRemoved, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).removeLocalClaim(anyString(), anyInt());

        when(unifiedClaimMetadataManager.isMappedLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID)).thenReturn(true);
        assertThrows(ClaimMetadataClientException.class, () -> {
            service.removeLocalClaim(localClaimURIToBeRemoved, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testGetExternalClaims() throws ClaimMetadataException {

        service.getExternalClaims(EXTERNAL_CLAIM_DIALECT_URI, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).getExternalClaims(anyString(), anyInt());

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.getExternalClaims(null, SUPER_TENANT_DOMAIN_NAME);
        });

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.getExternalClaims(LOCAL_CLAIM_DIALECT, null);
        });
    }

    @Test
    public void testUpdateExternalClaim() throws ClaimMetadataException {

        ClaimDialect externalDialect = new ClaimDialect(EXTERNAL_CLAIM_DIALECT_URI);
        ExternalClaim externalClaim = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, EXTERNAL_CLAIM_URI, LOCAL_CLAIM_1);
        when(unifiedClaimMetadataManager.getClaimDialects(SUPER_TENANT_ID))
                .thenReturn(Collections.singletonList(externalDialect));
        when(unifiedClaimMetadataManager.getExternalClaims(EXTERNAL_CLAIM_DIALECT_URI, SUPER_TENANT_ID))
                .thenReturn(Collections.singletonList(externalClaim));
        when(unifiedClaimMetadataManager.getLocalClaims(SUPER_TENANT_ID))
                .thenReturn(Collections.singletonList(new LocalClaim(LOCAL_CLAIM_1)));
        service.updateExternalClaim(externalClaim, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).updateExternalClaim(externalClaim, SUPER_TENANT_ID);

        when(unifiedClaimMetadataManager.getExternalClaims(EXTERNAL_CLAIM_DIALECT_URI, SUPER_TENANT_ID))
                .thenReturn(Collections.emptyList());
        assertThrows(ClaimMetadataClientException.class, () -> {
            service.updateExternalClaim(externalClaim, SUPER_TENANT_DOMAIN_NAME);
        });

        when(unifiedClaimMetadataManager.getClaimDialects(SUPER_TENANT_ID))
                .thenReturn(Collections.emptyList());
        assertThrows(ClaimMetadataClientException.class, () -> {
            service.updateExternalClaim(externalClaim, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testRemoveExternalClaim() throws ClaimMetadataException {

        service.removeExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, EXTERNAL_CLAIM_URI, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1))
                .removeExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, EXTERNAL_CLAIM_URI, SUPER_TENANT_ID);

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.removeExternalClaim(null, EXTERNAL_CLAIM_URI, SUPER_TENANT_DOMAIN_NAME);
        });
        assertThrows(ClaimMetadataClientException.class, () -> {
            service.removeExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, null, SUPER_TENANT_DOMAIN_NAME);
        });
        assertThrows(ClaimMetadataClientException.class, () -> {
            service.removeExternalClaim(LOCAL_CLAIM_DIALECT, LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testRemoveClaimMappingAttributes() throws ClaimMetadataException {

        String testUserStoreDomain = "TEST_DOMAIN";
        service.removeClaimMappingAttributes(SUPER_TENANT_ID, testUserStoreDomain);
        verify(unifiedClaimMetadataManager, times(1))
                .removeClaimMappingAttributes(SUPER_TENANT_ID, testUserStoreDomain);

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.removeClaimMappingAttributes(SUPER_TENANT_ID, null);
        });
    }

    @Test
    public void testRemoveAllClaims() throws ClaimMetadataException {

        service.removeAllClaims(SUPER_TENANT_ID);
        verify(unifiedClaimMetadataManager, times(1)).removeAllClaimDialects(SUPER_TENANT_ID);
    }

    @Test
    public void testGetMappedExternalClaimsForLocalClaim() throws ClaimMetadataException {

        service.getMappedExternalClaimsForLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).getMappedExternalClaims(LOCAL_CLAIM_1, SUPER_TENANT_ID);
    }

    @Test(dataProvider = "addLocalClaimUniquenessPropertiesData")
    public void testAddLocalClaimShouldSetCorrectUniquenessProperties(String newIsUnique, String newUniquenessScope,
                                                                      String expectedUniquenessScope,
                                                                      String expectedIsUnique)
            throws ClaimMetadataException {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        localClaim.setMappedAttributes(new ArrayList<>());
        localClaim.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE));

        Map<String, String> claimProperties = new HashMap<>();
        if (newIsUnique != null) {
            claimProperties.put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, newIsUnique);
        }
        if (newUniquenessScope != null) {
            claimProperties.put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY, newUniquenessScope);
        }
        localClaim.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());
        service.addLocalClaim(localClaim, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> localClaimCaptor = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager,
                times(1)).addLocalClaim(localClaimCaptor.capture(), anyInt());

        Map<String, String> capturedProperties = localClaimCaptor.getValue().getClaimProperties();
        if (expectedIsUnique != null) {
            assertEquals(capturedProperties.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY), expectedIsUnique);
        }
        if (expectedUniquenessScope != null) {
            assertEquals(capturedProperties.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY),
                    expectedUniquenessScope);
        }
    }

    @Test(dataProvider = "updateLocalClaimUniquenessPropertiesData")
    public void testUpdateLocalClaimShouldSetCorrectUniquenessProperties(String existingIsUnique,
                                                                         String existingUniquenessScope,
                                                                         String newIsUnique, String newUniquenessScope,
                                                                         String expectedUniquenessScope,
                                                                         String expectedIsUnique)
            throws ClaimMetadataException {

        // Create existing claim
        LocalClaim existingLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        existingLocalClaim.setMappedAttributes(new ArrayList<>());
        existingLocalClaim.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE));
        
        Map<String, String> existingProperties = new HashMap<>();
        if (existingIsUnique != null) {
            existingProperties.put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, existingIsUnique);
        }
        if (existingUniquenessScope != null) {
            existingProperties.put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY, existingUniquenessScope);
        }
        existingLocalClaim.setClaimProperties(existingProperties);

        // Create updated claim
        LocalClaim updatedLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        updatedLocalClaim.setMappedAttributes(new ArrayList<>());
        updatedLocalClaim.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE));
        
        Map<String, String> newProperties = new HashMap<>();
        if (newIsUnique != null) {
            newProperties.put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, newIsUnique);
        }
        if (newUniquenessScope != null) {
            newProperties.put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY, newUniquenessScope);
        }
        updatedLocalClaim.setClaimProperties(newProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));
        service.updateLocalClaim(updatedLocalClaim, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> localClaimCaptor = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager,
                times(1)).updateLocalClaim(localClaimCaptor.capture(), anyInt());

        Map<String, String> capturedProperties = localClaimCaptor.getValue().getClaimProperties();
        if (expectedIsUnique != null) {
            assertEquals(capturedProperties.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY), expectedIsUnique);
        }
        if (expectedUniquenessScope != null) {
            assertEquals(capturedProperties.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY),
                    expectedUniquenessScope);
        }
    }

    @Test
    public void testGetLocalClaimsUniquenessProperties() throws Exception {

        // Create test claims with different property combinations
        List<LocalClaim> mockClaims = new ArrayList<>();

        // Claim with only isUnique=true (e.g., email which should be unique)
        LocalClaim emailClaim = new LocalClaim("http://wso2.org/claims/emailaddress");
        Map<String, String> emailProps = new HashMap<>();
        emailProps.put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, "true");
        emailClaim.setClaimProperties(emailProps);
        mockClaims.add(emailClaim);

        // Claim with only isUnique=false (e.g., country which doesn't need to be unique)
        LocalClaim countryClaim = new LocalClaim("http://wso2.org/claims/country");
        Map<String, String> countryProps = new HashMap<>();
        countryProps.put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, "false");
        countryClaim.setClaimProperties(countryProps);
        mockClaims.add(countryClaim);

        // Claim with both properties (e.g., username which is unique across userstores)
        LocalClaim usernameClaim = new LocalClaim("http://wso2.org/claims/username");
        Map<String, String> usernameProps = new HashMap<>();
        usernameProps.put(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY, "true");
        usernameProps.put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY,
                ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString());
        usernameClaim.setClaimProperties(usernameProps);
        mockClaims.add(usernameClaim);

        // Claim with only UniquenessScope property (e.g., mobile number which is unique within userstore)
        LocalClaim mobileNumberClaim = new LocalClaim("http://wso2.org/claims/mobile");
        Map<String, String> mobileProps = new HashMap<>();
        mobileProps.put(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY,
                ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString());
        mobileNumberClaim.setClaimProperties(mobileProps);
        mockClaims.add(mobileNumberClaim);

        // Claim with no uniqueness properties (e.g., description)
        LocalClaim descriptionClaim = new LocalClaim("http://wso2.org/claims/description");
        Map<String, String> descriptionProps = new HashMap<>();
        descriptionProps.put("DisplayName", "Description");
        descriptionClaim.setClaimProperties(descriptionProps);
        mockClaims.add(descriptionClaim);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(mockClaims);
        identityUtilStaticMock.when(IdentityUtil::isGroupsVsRolesSeparationImprovementsEnabled).thenReturn(false);

        List<LocalClaim> resultClaims = service.getLocalClaims(SUPER_TENANT_DOMAIN_NAME);

        // Verify results
        assertEquals(resultClaims.size(), 5);

        // Verify email claim (only isUnique=true)
        LocalClaim resultEmailClaim = findClaimByUri(resultClaims,
                "http://wso2.org/claims/emailaddress");
        Map<String, String> resultEmailProps = resultEmailClaim.getClaimProperties();
        assertEquals(resultEmailProps.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY), "true");
        assertEquals(resultEmailProps.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY),
                ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString());

        // Verify country claim (only isUnique=false)
        LocalClaim resultCountryClaim = findClaimByUri(resultClaims,
                "http://wso2.org/claims/country");
        Map<String, String> resultCountryProps = resultCountryClaim.getClaimProperties();
        assertEquals(resultCountryProps.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY), "false");
        assertEquals(resultCountryProps.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY),
                ClaimConstants.ClaimUniquenessScope.NONE.toString());

        // Verify username claim (both properties)
        LocalClaim resultUsernameClaim = findClaimByUri(resultClaims,
                "http://wso2.org/claims/username");
        Map<String, String> resultUsernameProps = resultUsernameClaim.getClaimProperties();
        assertEquals(resultUsernameProps.get(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY), "true");
        assertEquals(resultUsernameProps.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY),
                ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString());

        // Verify mobile claim (only UniquenessScope property)
        LocalClaim resultMobileNumberClaim = findClaimByUri(resultClaims,
                "http://wso2.org/claims/mobile");
        Map<String, String> resultMobileProps = resultMobileNumberClaim.getClaimProperties();
        assertFalse(resultMobileProps.containsKey(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY));
        assertEquals(resultMobileProps.get(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY),
                ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString());

        // Verify description claim (no uniqueness properties)
        LocalClaim resultDescriptionClaim = findClaimByUri(resultClaims,
                "http://wso2.org/claims/description");
        Map<String, String> resultDescriptionProps = resultDescriptionClaim.getClaimProperties();
        assertFalse(resultDescriptionProps.containsKey(ClaimConstants.IS_UNIQUE_CLAIM_PROPERTY));
        assertFalse(resultDescriptionProps.containsKey(ClaimConstants.CLAIM_UNIQUENESS_SCOPE_PROPERTY));
        assertEquals(resultDescriptionProps.get("DisplayName"), "Description");
    }

    @DataProvider(name = "addLocalClaimUniquenessPropertiesData")
    public Object[][] addLocalClaimUniquenessPropertiesData() {

        return new Object[][]{
                // Case 1: Only isUnique property is included
                {"true", null, ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},
                {"false", null, ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},

                // Case 2: Only UniquenessScope property is included
                {null, ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(),
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), null},
                {null, ClaimConstants.ClaimUniquenessScope.NONE.toString(),
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), null},
                {null, ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(),
                        ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(), null},

                // Case 3: Both isUnique & UniquenessScope properties are included
                {"true", ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(),
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},
                {"true", ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(),
                        ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(), "true"},
                {"true", ClaimConstants.ClaimUniquenessScope.NONE.toString(),
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},
                {"false", ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(),
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},
                {"false", ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(),
                        ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(), "true"},
                {"false", ClaimConstants.ClaimUniquenessScope.NONE.toString(),
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"}};
    }

    @DataProvider(name = "updateLocalClaimUniquenessPropertiesData")
    public Object[][] updateLocalClaimUniquenessPropertiesData() {

        return new Object[][]{
                // Case 1: None of the properties exist
                {null, null, "true", null, ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},
                {null, null, "false", null, ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},
                {null, null, null, ClaimConstants.ClaimUniquenessScope.NONE.toString(),
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), null},
                {null, null, null, ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(),
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), null},
                {null, null, null, ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(),
                        ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(), null},
                {null, null, "true", ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(),
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},
                {null, null, "true", ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(),
                        ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(), "true"},
                {null, null, "false", ClaimConstants.ClaimUniquenessScope.NONE.toString(),
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},
                {null, null, "false", ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(),
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},
                {null, null, "false", ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(),
                        ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(), "true"},
                {null, null, "true", ClaimConstants.ClaimUniquenessScope.NONE.toString(),
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},

                // Case 2: Only isUnique property exists
                {"true", null, "true", null, ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},
                {"true", null, "false", null, ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},
                {"false", null, "true", null, ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},
                {"false", null, "false", null, ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},

                // Case 3: Only UniquenessScope property exists
                {null, ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true", null,
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},
                {null, ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "false", null,
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},
                {null, ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(), "true", null,
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},
                {null, ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(), "false", null,
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},
                {null, ClaimConstants.ClaimUniquenessScope.NONE.toString(), "true", null,
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},
                {null, ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false", null,
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},

                // Case 4: Both properties exist - only isUnique property changes
                {"true", ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "false",
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(),
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},
                {"false", ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true",
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(),
                        ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true"},

                // Case 5: Both properties exist - only UniquenessScope property changes
                {"true", ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true",
                        ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(),
                        ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(), "true"},
                {"true", ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "true",
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(),
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"},

                // Case 6: Both properties exist - both properties change
                {"true", ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "false",
                        ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(),
                        ClaimConstants.ClaimUniquenessScope.WITHIN_USERSTORE.toString(), "true"},
                {"true", ClaimConstants.ClaimUniquenessScope.ACROSS_USERSTORES.toString(), "false",
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(),
                        ClaimConstants.ClaimUniquenessScope.NONE.toString(), "false"}};
    }

    @Test
    public void testAddLocalClaimWithAttributeProfiles() throws ClaimMetadataException {

        LocalClaim localClaimToBeAdded = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes()
                .add(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE));

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(REQUIRED_PROPERTY, "true");
        claimProperties.put(
                buildProfilePropertyKey(ClaimConstants.DefaultAllowedClaimProfile.CONSOLE.getProfileName(),
                        REQUIRED_PROPERTY), "true");
        claimProperties.put(
                buildProfilePropertyKey(ClaimConstants.DefaultAllowedClaimProfile.END_USER.getProfileName(),
                        REQUIRED_PROPERTY), "false");
        localClaimToBeAdded.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());

        service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> captor = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1)).addLocalClaim(captor.capture(), anyInt());

        Map<String, String> captureLocalClaimProperties = captor.getValue().getClaimProperties();
        assertFalse(captureLocalClaimProperties.containsKey(
                buildProfilePropertyKey(ClaimConstants.DefaultAllowedClaimProfile.CONSOLE.getProfileName(),
                SUPPORTED_BY_DEFAULT_PROPERTY)));
    }

    @Test
    public void testUpdateLocalClaimRemoveAttributeProfilePropertiesEqualToGlobalProperties()
            throws ClaimMetadataException {

        LocalClaim existingLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        existingLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));

        LocalClaim localClaimToBeUpdated = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeUpdated.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(SUPPORTED_BY_DEFAULT_PROPERTY, "true");
        claimProperties.put(
                buildProfilePropertyKey(ClaimConstants.DefaultAllowedClaimProfile.CONSOLE.getProfileName(),
                        SUPPORTED_BY_DEFAULT_PROPERTY), "true");
        claimProperties.put(
                buildProfilePropertyKey(ClaimConstants.DefaultAllowedClaimProfile.END_USER.getProfileName(),
                        SUPPORTED_BY_DEFAULT_PROPERTY), "false");
        localClaimToBeUpdated.setClaimProperties(claimProperties);

        service.updateLocalClaim(localClaimToBeUpdated, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .updateLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedClaimProperties = capturedLocalClaim.getValue().getClaimProperties();
        assertFalse(capturedClaimProperties.containsKey(
                buildProfilePropertyKey(
                        ClaimConstants.DefaultAllowedClaimProfile.CONSOLE.getProfileName(),
                        SUPPORTED_BY_DEFAULT_PROPERTY)));
    }

    @Test
    public void testUpdateLocalClaimChangeGlobalPropertyFromProfileProperties()
            throws ClaimMetadataException {

        LocalClaim existingLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        existingLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));

        LocalClaim localClaimToBeUpdated = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeUpdated.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));

        identityUtilStaticMock.when(() -> IdentityUtil.getProperty(
                        eq(ClaimConstants.ALLOWED_ATTRIBUTE_PROFILE_CONFIG))).thenReturn("profile1");

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(SUPPORTED_BY_DEFAULT_PROPERTY, "true");
        claimProperties.put(
                buildProfilePropertyKey(ClaimConstants.DefaultAllowedClaimProfile.CONSOLE.getProfileName(),
                        SUPPORTED_BY_DEFAULT_PROPERTY), "false");
        claimProperties.put(
                buildProfilePropertyKey(ClaimConstants.DefaultAllowedClaimProfile.END_USER.getProfileName(),
                        SUPPORTED_BY_DEFAULT_PROPERTY), "false");
        claimProperties.put(
                buildProfilePropertyKey(ClaimConstants.DefaultAllowedClaimProfile.SELF_REGISTRATION.getProfileName(),
                        SUPPORTED_BY_DEFAULT_PROPERTY), "false");
        claimProperties.put(
                buildProfilePropertyKey("profile1", SUPPORTED_BY_DEFAULT_PROPERTY), "false");
        localClaimToBeUpdated.setClaimProperties(claimProperties);

        service.updateLocalClaim(localClaimToBeUpdated, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .updateLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedClaimProperties = capturedLocalClaim.getValue().getClaimProperties();
        assertFalse(capturedClaimProperties.containsKey(
                buildProfilePropertyKey(
                        ClaimConstants.DefaultAllowedClaimProfile.CONSOLE.getProfileName(),
                        SUPPORTED_BY_DEFAULT_PROPERTY)));
        assertFalse(capturedClaimProperties.containsKey(
                buildProfilePropertyKey("profile1", SUPPORTED_BY_DEFAULT_PROPERTY)));
        assertEquals(capturedClaimProperties.get(SUPPORTED_BY_DEFAULT_PROPERTY), "false");
    }

    @Test
    public void testUpdateLocalClaimWithInvalidAttributeProfile() throws ClaimMetadataException {

        LocalClaim existingLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        existingLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));

        LocalClaim localClaimToBeUpdated = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeUpdated.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(SUPPORTED_BY_DEFAULT_PROPERTY, "true");
        claimProperties.put(
                buildProfilePropertyKey("invalid", SUPPORTED_BY_DEFAULT_PROPERTY), "true");
        localClaimToBeUpdated.setClaimProperties(claimProperties);

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.updateLocalClaim(localClaimToBeUpdated, SUPER_TENANT_DOMAIN_NAME);
        });

        // Case 2: Invalid profile property.
        Map<String, String> claimProperties2 = new HashMap<>();
        claimProperties2.put(SUPPORTED_BY_DEFAULT_PROPERTY, "true");
        claimProperties2.put("Profiles.SupportedByDefault", "true");
        localClaimToBeUpdated.setClaimProperties(claimProperties2);

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.updateLocalClaim(localClaimToBeUpdated, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    private String buildProfilePropertyKey(String profileName, String property) {

        return ClaimConstants.PROFILES_CLAIM_PROPERTY_PREFIX + profileName +
                ClaimConstants.CLAIM_PROFILE_PROPERTY_DELIMITER + property;
    }

    @AfterMethod
    public void tearDown() {

        dataHolderStaticMock.close();
        identityUtilStaticMock.close();
        identityTenantUtil.close();
        claimMetadataEventPublisherProxy.close();
    }

    private LocalClaim findClaimByUri(List<LocalClaim> claims, String uri) {

        return claims.stream()
                .filter(claim -> claim.getClaimURI().equals(uri))
                .findFirst()
                .orElse(null);
    }
}
