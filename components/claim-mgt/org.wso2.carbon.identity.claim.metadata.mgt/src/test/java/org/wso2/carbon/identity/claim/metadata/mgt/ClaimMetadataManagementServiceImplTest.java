/*
 * Copyright (c) 2022-2025, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.claim.metadata.mgt.internal.ReadOnlyClaimMetadataManager;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.ArrayList;
import java.util.Arrays;
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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_CLAIM_MUST_BE_MANAGED_IN_USER_STORE;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.ErrorMessage.ERROR_CODE_INVALID_SHARED_PROFILE_VALUE_RESOLVING_METHOD;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.IS_SYSTEM_CLAIM;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.LOCAL_CLAIM_DIALECT_URI;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.READ_ONLY_PROPERTY;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.SHARED_PROFILE_VALUE_RESOLVING_METHOD;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.REQUIRED_PROPERTY;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.SUPPORTED_BY_DEFAULT_PROPERTY;
import static org.wso2.carbon.identity.testutil.Whitebox.setInternalState;

@WithCarbonHome
@Test
public class ClaimMetadataManagementServiceImplTest {

    private static final String LOCAL_CLAIM_DIALECT = "http://wso2.org/claims";
    private static final String LOCAL_CLAIM_1 = "http://wso2.org/claims/username";
    private static final String LOCAL_CLAIM_2 = "http://wso2.org/claims/email";
    private static final String CUSTOM_CLAIM = "http://wso2.org/claims/customAttribute";
    private static final String EXTERNAL_CLAIM_DIALECT_URI = "https://abc.org";
    private static final String EXTERNAL_CLAIM_URI = "test";
    private static final String MAPPED_LOCAL_CLAIM_URI = "http://wso2.org/claims/test";
    private static final String PRIMARY_DOMAIN = "PRIMARY";
    private static final String USERNAME_ATTRIBUTE = "username";
    private static final String CUSTOM_ATTRIBUTE = "custom_attribute";
    private static final String CLAIM_PROPERTY_KEY_1 = "property_1";
    private static final String CLAIM_PROPERTY_VALUE_1 = "value_1";

    private final ExternalClaim externalClaim = new ExternalClaim(EXTERNAL_CLAIM_DIALECT_URI, EXTERNAL_CLAIM_URI,
            MAPPED_LOCAL_CLAIM_URI);

    private ClaimMetadataManagementService service;
    private UnifiedClaimMetadataManager unifiedClaimMetadataManager;
    private ReadOnlyClaimMetadataManager systemDefaultClaimMetadataManager;
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
        systemDefaultClaimMetadataManager = Mockito.mock(ReadOnlyClaimMetadataManager.class);
        service = new ClaimMetadataManagementServiceImpl();
        setInternalState(service, "unifiedClaimMetadataManager", unifiedClaimMetadataManager);
        setInternalState(service, "systemDefaultClaimMetadataManager", systemDefaultClaimMetadataManager);

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

        when(unifiedClaimMetadataManager.isLocalClaimMappedWithinDialect(MAPPED_LOCAL_CLAIM_URI, EXTERNAL_CLAIM_DIALECT_URI,
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
        when(unifiedClaimMetadataManager.isLocalClaimMappedWithinDialect(MAPPED_LOCAL_CLAIM_URI, EXTERNAL_CLAIM_DIALECT_URI,
                SUPER_TENANT_ID)).thenReturn(Boolean.TRUE);

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
    public void testGetLocalClaim() throws ClaimMetadataException {

        service.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).getLocalClaim(anyString(), anyInt());
    }

    @Test
    public void testAddLocalClaim() throws ClaimMetadataException {

        LocalClaim localClaimToBeAdded = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes()
                .add(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE));

        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Username");
        claimProperties.put("subAttributes", "http://wso2.org/claims/testclaim1 http://wso2.org/claims/testclaim2 " +
                "http://wso2.org/claims/testclaim3 http://wso2.org/claims/testclaim4 http://wso2.org/claims/testclaim5 " +
                "http://wso2.org/claims/testclaim6 http://wso2.org/claims/testclaim7 http://wso2.org/claims/testclaim8 " +
                "http://wso2.org/claims/testclaim9 http://wso2.org/claims/testclaim10 http://wso2.org/claims/testclaim11" +
                " http://wso2.org/claims/testclaim12");
        claimProperties.put("canonicalValues", "[{\"label\":\"north\",\"value\":\"NORTHERN\"}, " +
                "{\"label\":\"west\",\"value\":\"WEST\"}]");
        localClaimToBeAdded.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());
        service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).addLocalClaim(any(), anyInt());

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt()))
                .thenReturn(Collections.singletonList(localClaimToBeAdded));
        assertThrows(ClaimMetadataClientException.class, () -> {
            service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @DataProvider(name = "sharedProfileValueResolvingMethodValidationForClaimAdditionData")
    public Object[][] sharedProfileValueResolvingMethodValidationForClaimAdditionData() {

        Map<String, String> propertiesWithOutSharedProfileValueResolvingMethod = new HashMap<>();
        propertiesWithOutSharedProfileValueResolvingMethod.put(CLAIM_PROPERTY_KEY_1, CLAIM_PROPERTY_VALUE_1);

        Map<String, String> propertiesWithFromOriginAsSharedProfileValueResolvingMethod = new HashMap<>();
        propertiesWithFromOriginAsSharedProfileValueResolvingMethod.put(SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());

        Map<String, String> propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod = new HashMap<>();
        propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod.put(SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_SHARED_PROFILE.getName());

        Map<String, String> propertiesWithFromFirstFoundInHierarchyAsProfileAsSharedProfileValueResolvingMethod =
                new HashMap<>();
        propertiesWithFromFirstFoundInHierarchyAsProfileAsSharedProfileValueResolvingMethod.put(
                SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_FIRST_FOUND_IN_HIERARCHY.getName());

        Map<String, String> propertiesWithInvalidProfileAsSharedProfileValueResolvingMethod = new HashMap<>();
        propertiesWithInvalidProfileAsSharedProfileValueResolvingMethod.put(SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                "InvalidMethod");

        return new Object[][]{
                {propertiesWithOutSharedProfileValueResolvingMethod, true},
                {propertiesWithFromOriginAsSharedProfileValueResolvingMethod, true},
                {propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod, true},
                {propertiesWithFromFirstFoundInHierarchyAsProfileAsSharedProfileValueResolvingMethod, true},
                {propertiesWithInvalidProfileAsSharedProfileValueResolvingMethod, false}
        };
    }

    @Test(dataProvider = "sharedProfileValueResolvingMethodValidationForClaimAdditionData")
    public void testSharedProfileValueResolvingMethodValidationOnLocalClaimAdd(Map<String, String> claimProperties,
                                                                               boolean isValidClaimAddition)
            throws ClaimMetadataException {

        LocalClaim localClaimToBeAdded = new LocalClaim(CUSTOM_CLAIM);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, CUSTOM_ATTRIBUTE));
        localClaimToBeAdded.getClaimProperties().putAll(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(SUPER_TENANT_ID)).thenReturn(new ArrayList<>());
        if (isValidClaimAddition) {
            service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);
            verify(unifiedClaimMetadataManager, times(1)).addLocalClaim(any(), anyInt());
        } else {
            assertThrows(ClaimMetadataClientException.class, () -> {
                service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);
            });
        }
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

    @DataProvider(name = "sharedProfileValueResolvingMethodValidationForSystemClaimData")
    public Object[][] sharedProfileValueResolvingMethodValidationForSystemClaimData() {

        Map<String, String> propertiesWithOutSharedProfileValueResolvingMethod = new HashMap<>();
        propertiesWithOutSharedProfileValueResolvingMethod.put(CLAIM_PROPERTY_KEY_1, CLAIM_PROPERTY_VALUE_1);

        Map<String, String> propertiesWithFromOriginAsSharedProfileValueResolvingMethod = new HashMap<>();
        propertiesWithFromOriginAsSharedProfileValueResolvingMethod.put(SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());
        propertiesWithFromOriginAsSharedProfileValueResolvingMethod.put(CLAIM_PROPERTY_KEY_1, CLAIM_PROPERTY_VALUE_1);

        Map<String, String> propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod = new HashMap<>();
        propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod.put(SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_SHARED_PROFILE.getName());
        propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod.put(CLAIM_PROPERTY_KEY_1,
                CLAIM_PROPERTY_VALUE_1);

        return new Object[][]{
                // Case 1: Updating claim and existing claim don't have SharedProfileValueResolvingMethod property.
                {
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithOutSharedProfileValueResolvingMethod),
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithOutSharedProfileValueResolvingMethod),
                        true
                },
                // Case 2: Updating claim has SharedProfileValueResolvingMethod matching to system default
                // but existing claim don't have SharedProfileValueResolvingMethod property.
                {
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithFromOriginAsSharedProfileValueResolvingMethod),
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithOutSharedProfileValueResolvingMethod),
                        true
                },
                // Case 3: Updating claim has SharedProfileValueResolvingMethod value same as the existing claim's
                // SharedProfileValueResolvingMethod value.
                {
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithFromOriginAsSharedProfileValueResolvingMethod),
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithFromOriginAsSharedProfileValueResolvingMethod),
                        true
                },
                // Case 4: Able to remove the SharedProfileValueResolvingMethod property from the existing claim.
                {
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithOutSharedProfileValueResolvingMethod),
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithFromOriginAsSharedProfileValueResolvingMethod),
                        true
                },
                // Case 5: Updating claim has SharedProfileValueResolvingMethod value different than system default
                // while existing claim don't have SharedProfileValueResolvingMethod property.
                {
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod),
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithOutSharedProfileValueResolvingMethod),
                        false
                },
                // Case 6: Updating claim has SharedProfileValueResolvingMethod value different than the existing claim's
                // SharedProfileValueResolvingMethod value.
                {
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod),
                        new LocalClaim(
                                LOCAL_CLAIM_1,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)),
                                propertiesWithFromOriginAsSharedProfileValueResolvingMethod),
                        false
                },
        };
    }

    @Test(dataProvider = "sharedProfileValueResolvingMethodValidationForSystemClaimData")
    public void testSharedProfileValueResolvingMethodValidationOnLocalSystemClaimUpdate(LocalClaim claimToBeUpdated,
                                                                                        LocalClaim existingClaim,
                                                                                        boolean isValidationSuccess)
            throws ClaimMetadataException {

        // Setting IS_SYSTEM_CLAIM property to true for both claims to identify this tested claim is system claim.
        claimToBeUpdated.getClaimProperties().put(IS_SYSTEM_CLAIM, Boolean.TRUE.toString());
        existingClaim.getClaimProperties().put(IS_SYSTEM_CLAIM, Boolean.TRUE.toString());
        when(unifiedClaimMetadataManager.getLocalClaim(claimToBeUpdated.getClaimURI(), SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingClaim));

        // Mock system default claim with FROM_ORIGIN as the default value
        LocalClaim systemDefaultClaim = new LocalClaim(claimToBeUpdated.getClaimURI());
        Map<String, String> systemDefaultProperties = new HashMap<>();
        systemDefaultProperties.put(SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());
        systemDefaultClaim.setClaimProperties(systemDefaultProperties);
        when(systemDefaultClaimMetadataManager.getLocalClaim(claimToBeUpdated.getClaimURI(), SUPER_TENANT_ID))
                .thenReturn(Optional.of(systemDefaultClaim));

        try {
            service.updateLocalClaim(claimToBeUpdated, SUPER_TENANT_DOMAIN_NAME);
            if (isValidationSuccess) {
                verify(unifiedClaimMetadataManager, times(1)).updateLocalClaim(any(), anyInt());
            } else {
                verify(unifiedClaimMetadataManager, times(0)).updateLocalClaim(any(), anyInt());
            }
        } catch (ClaimMetadataClientException e) {
            if (!isValidationSuccess) {
                assertEquals(e.getErrorCode(),
                        ClaimConstants.ErrorMessage.ERROR_CODE_NO_SHARED_PROFILE_VALUE_RESOLVING_METHOD_CHANGE_FOR_SYSTEM_CLAIM.getCode());
                assertEquals(e.getMessage(), String.format(
                        ClaimConstants.ErrorMessage.ERROR_CODE_NO_SHARED_PROFILE_VALUE_RESOLVING_METHOD_CHANGE_FOR_SYSTEM_CLAIM.getMessage(),
                        existingClaim.getClaimURI()));
            }
        }
    }

    @DataProvider(name = "sharedProfileValueResolvingMethodValidationForNonSystemClaimData")
    public Object[][] sharedProfileValueResolvingMethodValidationForNonSystemClaimData() {

        Map<String, String> propertiesWithOutSharedProfileValueResolvingMethod = new HashMap<>();
        propertiesWithOutSharedProfileValueResolvingMethod.put(CLAIM_PROPERTY_KEY_1, CLAIM_PROPERTY_VALUE_1);

        Map<String, String> propertiesWithFromOriginAsSharedProfileValueResolvingMethod = new HashMap<>();
        propertiesWithFromOriginAsSharedProfileValueResolvingMethod.put(SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_ORIGIN.getName());
        propertiesWithFromOriginAsSharedProfileValueResolvingMethod.put(CLAIM_PROPERTY_KEY_1, CLAIM_PROPERTY_VALUE_1);

        Map<String, String> propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod = new HashMap<>();
        propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod.put(SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                ClaimConstants.SharedProfileValueResolvingMethod.FROM_SHARED_PROFILE.getName());
        propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod.put(CLAIM_PROPERTY_KEY_1,
                CLAIM_PROPERTY_VALUE_1);

        Map<String, String> propertiesWithInvalidSharedProfileValueResolvingMethod = new HashMap<>();
        propertiesWithInvalidSharedProfileValueResolvingMethod.put(SHARED_PROFILE_VALUE_RESOLVING_METHOD,
                "InvalidSharedProfileValueResolvingMethod");
        propertiesWithInvalidSharedProfileValueResolvingMethod.put(CLAIM_PROPERTY_KEY_1,
                CLAIM_PROPERTY_VALUE_1);

        return new Object[][]{
                // Case 1: Updating claim and existing claim don't have SharedProfileValueResolvingMethod property.
                {
                        new LocalClaim(
                                CUSTOM_CLAIM,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, CUSTOM_ATTRIBUTE)),
                                propertiesWithOutSharedProfileValueResolvingMethod),
                        new LocalClaim(
                                CUSTOM_CLAIM,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, CUSTOM_ATTRIBUTE)),
                                propertiesWithOutSharedProfileValueResolvingMethod),
                        true
                },
                // Case 2: Updating claim has a SharedProfileValueResolvingMethod value but existing claim don't
                // have SharedProfileValueResolvingMethod property.
                {
                        new LocalClaim(
                                CUSTOM_CLAIM,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, CUSTOM_ATTRIBUTE)),
                                propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod),
                        new LocalClaim(
                                CUSTOM_CLAIM,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, CUSTOM_ATTRIBUTE)),
                                propertiesWithOutSharedProfileValueResolvingMethod),
                        true
                },
                // Case 3: Updating claim has a SharedProfileValueResolvingMethod value different than existing
                // claim's SharedProfileValueResolvingMethod value.
                {
                        new LocalClaim(
                                CUSTOM_CLAIM,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, CUSTOM_ATTRIBUTE)),
                                propertiesWithFromOriginAsSharedProfileValueResolvingMethod),
                        new LocalClaim(
                                CUSTOM_CLAIM,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, CUSTOM_ATTRIBUTE)),
                                propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod),
                        true
                },
                // Case 4: Updating claim don't have SharedProfileValueResolvingMethod property but existing claim has.
                {
                        new LocalClaim(
                                CUSTOM_CLAIM,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, CUSTOM_ATTRIBUTE)),
                                propertiesWithOutSharedProfileValueResolvingMethod),
                        new LocalClaim(
                                CUSTOM_CLAIM,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, CUSTOM_ATTRIBUTE)),
                                propertiesWithFromSharedProfileAsSharedProfileValueResolvingMethod),
                        true
                },
                // Case 5: Updating claim has invalid resolving method.
                {
                        new LocalClaim(
                                CUSTOM_CLAIM,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, CUSTOM_ATTRIBUTE)),
                                propertiesWithInvalidSharedProfileValueResolvingMethod),
                        new LocalClaim(
                                CUSTOM_CLAIM,
                                Arrays.asList(new AttributeMapping(PRIMARY_DOMAIN, CUSTOM_ATTRIBUTE)),
                                propertiesWithFromOriginAsSharedProfileValueResolvingMethod),
                        false
                },
        };
    }

    @Test(dataProvider = "sharedProfileValueResolvingMethodValidationForNonSystemClaimData")
    public void testSharedProfileValueResolvingMethodValidationOnLocalNonSystemClaimUpdate(LocalClaim claimToBeUpdated,
                                                                                           LocalClaim existingClaim,
                                                                                           boolean isValidationSuccess)
            throws ClaimMetadataException {

        when(unifiedClaimMetadataManager.getLocalClaim(claimToBeUpdated.getClaimURI(), SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingClaim));
        try {
            service.updateLocalClaim(claimToBeUpdated, SUPER_TENANT_DOMAIN_NAME);
            if (isValidationSuccess) {
                verify(unifiedClaimMetadataManager, times(1)).updateLocalClaim(any(), anyInt());
            } else {
                verify(unifiedClaimMetadataManager, times(0)).updateLocalClaim(any(), anyInt());
            }
        } catch (ClaimMetadataClientException e) {
            if (!isValidationSuccess) {
                assertEquals(e.getErrorCode(),
                        ERROR_CODE_INVALID_SHARED_PROFILE_VALUE_RESOLVING_METHOD.getCode());
                assertEquals(e.getMessage(), String.format(
                        ERROR_CODE_INVALID_SHARED_PROFILE_VALUE_RESOLVING_METHOD.getMessage(),
                        claimToBeUpdated.getClaimProperties().get(SHARED_PROFILE_VALUE_RESOLVING_METHOD)));
            }
        }
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

    @Test
    public void testGetSupportedLocalClaimsForProfile() throws Exception {

        String profileName = "selfRegistration";
        // Create test claims with different property combinations.
        List<LocalClaim> mockClaims = new ArrayList<>();

        // Claim with global supported by default enabled.
        LocalClaim emailClaim = new LocalClaim("http://wso2.org/claims/emailaddress");
        Map<String, String> emailProps = new HashMap<>();
        emailProps.put(SUPPORTED_BY_DEFAULT_PROPERTY, "true");
        emailProps.put(REQUIRED_PROPERTY, "true");
        emailProps.put(READ_ONLY_PROPERTY, "false");
        emailProps.put(buildProfilePropertyKey(profileName, READ_ONLY_PROPERTY), "true");
        emailProps.put(buildProfilePropertyKey(profileName, REQUIRED_PROPERTY), "false");
        emailClaim.setClaimProperties(emailProps);
        mockClaims.add(emailClaim);

        // Claim with global supported by default disabled, profile specific one enabled.
        LocalClaim countryClaim = new LocalClaim("http://wso2.org/claims/country");
        Map<String, String> countryProps = new HashMap<>();
        countryProps.put(SUPPORTED_BY_DEFAULT_PROPERTY, "false");
        countryProps.put(REQUIRED_PROPERTY, "false");
        countryProps.put(READ_ONLY_PROPERTY, "false");
        countryProps.put(buildProfilePropertyKey(profileName, SUPPORTED_BY_DEFAULT_PROPERTY), "true");
        countryProps.put(buildProfilePropertyKey(profileName, REQUIRED_PROPERTY), "true");
        countryClaim.setClaimProperties(countryProps);
        mockClaims.add(countryClaim);

        // Claim with global supported by default enabled, profile specific one disabled.
        LocalClaim testClaim = new LocalClaim("http://wso2.org/claims/test");
        Map<String, String> testProps = new HashMap<>();
        testProps.put(SUPPORTED_BY_DEFAULT_PROPERTY, "true");
        testProps.put(REQUIRED_PROPERTY, "true");
        testProps.put(buildProfilePropertyKey(profileName, SUPPORTED_BY_DEFAULT_PROPERTY), "false");
        testProps.put(buildProfilePropertyKey(profileName, REQUIRED_PROPERTY), "false");
        testClaim.setClaimProperties(testProps);
        mockClaims.add(testClaim);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(mockClaims);
        identityUtilStaticMock.when(IdentityUtil::isGroupsVsRolesSeparationImprovementsEnabled).thenReturn(false);

        List<LocalClaim> resultClaims =
                service.getSupportedLocalClaimsForProfile(SUPER_TENANT_DOMAIN_NAME, profileName);

        // Verify results.
        assertEquals(resultClaims.size(), 2);

        LocalClaim resultEmailClaim = findClaimByUri(resultClaims,
                "http://wso2.org/claims/emailaddress");
        Map<String, String> resultEmailProps = resultEmailClaim.getClaimProperties();
        assertEquals(resultEmailProps.get(READ_ONLY_PROPERTY), "true");
        assertEquals(resultEmailProps.get(REQUIRED_PROPERTY), "false");

        LocalClaim resultCountryClaim = findClaimByUri(resultClaims,
                "http://wso2.org/claims/country");
        Map<String, String> resultCountryProps = resultCountryClaim.getClaimProperties();
        assertEquals(resultCountryProps.get(READ_ONLY_PROPERTY), "false");
        assertEquals(resultCountryProps.get(REQUIRED_PROPERTY), "true");

        assertNull(findClaimByUri(resultClaims,"http://wso2.org/claims/test"));
    }

    @Test
    public void testGetSupportedLocalClaimsForProfileWithInvalidProfile() throws Exception {

        String profileName = "invalid";
        List<LocalClaim> mockClaims = new ArrayList<>();
        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(mockClaims);
        identityUtilStaticMock.when(IdentityUtil::isGroupsVsRolesSeparationImprovementsEnabled).thenReturn(false);

        assertThrows(ClaimMetadataClientException.class, () ->
                service.getSupportedLocalClaimsForProfile(SUPER_TENANT_DOMAIN_NAME, profileName));
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

    @Test
    public void testGetLocalClaimWithoutManagedInUserStoreProperty() throws ClaimMetadataException {

        LocalClaim mockLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        Map<String, String> properties = new HashMap<>();
        properties.put("DisplayName", "Username");
        mockLocalClaim.setClaimProperties(properties);

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(mockLocalClaim));

        Optional<LocalClaim> result = service.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME,
                false);

        assertTrue(result.isPresent());
        // Should not have ManagedInUserStore property when includeManagedInUserStoreInfo is false.
        assertFalse(result.get().getClaimProperties().containsKey(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY));
    }

    @Test
    public void testGetLocalClaimWithManagedInUserStoreForNonIdentityClaim()
            throws ClaimMetadataException {

        LocalClaim mockLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        Map<String, String> properties = new HashMap<>();
        properties.put("DisplayName", "Username");
        mockLocalClaim.setClaimProperties(properties);

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(mockLocalClaim));

        Optional<LocalClaim> result = service.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME,
                true);

        assertTrue(result.isPresent());
        // Non-identity claim should have ManagedInUserStore=true by default.
        assertEquals(result.get().getClaimProperties().get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY),
                "true");
    }

    @Test
    public void testGetLocalClaimWithManagedInUserStoreForIdentityClaim()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim mockLocalClaim = new LocalClaim(identityClaimUri);
        Map<String, String> properties = new HashMap<>();
        properties.put("DisplayName", "Account Locked");
        mockLocalClaim.setClaimProperties(properties);

        when(unifiedClaimMetadataManager.getLocalClaim(identityClaimUri, SUPER_TENANT_ID))
                .thenReturn(Optional.of(mockLocalClaim));

        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn(null);

        Optional<LocalClaim> result = service.getLocalClaim(identityClaimUri, SUPER_TENANT_DOMAIN_NAME,
                true);

        assertTrue(result.isPresent());
        // Identity claim should have ManagedInUserStore=false by default when not user-store based.
        assertEquals(result.get().getClaimProperties().get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY),
                "false");
    }

    @Test
    public void testGetLocalClaimWithManagedInUserStoreWhenClaimNotFound()
            throws ClaimMetadataException {

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.empty());

        Optional<LocalClaim> result = service.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME,
                true);

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetLocalClaimWithManagedInUserStoreForIdentityClaimWithUserStoreBasedDataStore()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim mockLocalClaim = new LocalClaim(identityClaimUri);
        Map<String, String> properties = new HashMap<>();
        properties.put("DisplayName", "Account Locked");
        mockLocalClaim.setClaimProperties(properties);

        when(unifiedClaimMetadataManager.getLocalClaim(identityClaimUri, SUPER_TENANT_ID))
                .thenReturn(Optional.of(mockLocalClaim));

        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn("org.wso2.carbon.identity.governance.store.UserStoreBasedIdentityDataStore");

        Optional<LocalClaim> result = service.getLocalClaim(identityClaimUri, SUPER_TENANT_DOMAIN_NAME,
                true);

        assertTrue(result.isPresent());
        // Identity claim should have ManagedInUserStore=true when identity data store is user-store based.
        assertEquals(result.get().getClaimProperties().get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY),
                "true");
    }

    @Test
    public void testGetLocalClaimWithManagedInUserStorePreservesExistingPropertyForNonIdentityClaim()
            throws ClaimMetadataException {

        LocalClaim mockLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        Map<String, String> properties = new HashMap<>();
        properties.put("DisplayName", "Username");
        properties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "false");
        mockLocalClaim.setClaimProperties(properties);

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(mockLocalClaim));

        Optional<LocalClaim> result = service.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME, true);

        assertTrue(result.isPresent());
        // Should preserve existing ManagedInUserStore property for non-identity claim.
        assertEquals(result.get().getClaimProperties().get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
    }

    @Test
    public void testGetLocalClaimWithManagedInUserStorePreservesExistingPropertyForIdentityClaim()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim mockLocalClaim = new LocalClaim(identityClaimUri);
        Map<String, String> properties = new HashMap<>();
        properties.put("DisplayName", "Account Locked");
        properties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "false");
        mockLocalClaim.setClaimProperties(properties);

        when(unifiedClaimMetadataManager.getLocalClaim(identityClaimUri, SUPER_TENANT_ID))
                .thenReturn(Optional.of(mockLocalClaim));

        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn(null);

        Optional<LocalClaim> result = service.getLocalClaim(identityClaimUri, SUPER_TENANT_DOMAIN_NAME, true);

        assertTrue(result.isPresent());
        // Should preserve existing ManagedInUserStore property for identity claim.
        assertEquals(result.get().getClaimProperties().get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
        // When managed in user store is false, should not have excluded user stores property.
        assertFalse(result.get().getClaimProperties().containsKey(ClaimConstants.EXCLUDED_USER_STORES_PROPERTY));
    }

    @Test
    public void testUpdateLocalClaimForNonIdentityClaimWithoutManagedInUserStoreProperty()
            throws ClaimMetadataException {

        LocalClaim existingLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        existingLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        Map<String, String> existingProperties = new HashMap<>();
        existingProperties.put("DisplayName", "Username");
        existingLocalClaim.setClaimProperties(existingProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));

        LocalClaim updatedLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        updatedLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        Map<String, String> updatedProperties = new HashMap<>();
        updatedProperties.put("DisplayName", "Username Updated");
        updatedLocalClaim.setClaimProperties(updatedProperties);

        service.updateLocalClaim(updatedLocalClaim, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .updateLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // Non-identity claim should have ManagedInUserStore=true by default.
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "true");
    }

    @Test
    public void testUpdateLocalClaimForIdentityClaimWithoutManagedInUserStoreProperty()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim existingLocalClaim = new LocalClaim(identityClaimUri);
        existingLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> existingProperties = new HashMap<>();
        existingProperties.put("DisplayName", "Account Locked");
        existingLocalClaim.setClaimProperties(existingProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(identityClaimUri, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));

        LocalClaim updatedLocalClaim = new LocalClaim(identityClaimUri);
        updatedLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> updatedProperties = new HashMap<>();
        updatedProperties.put("DisplayName", "Account Locked Updated");
        updatedLocalClaim.setClaimProperties(updatedProperties);

        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn(null);

        service.updateLocalClaim(updatedLocalClaim, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .updateLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // Identity claim should have ManagedInUserStore=false by default when not user-store based.
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
    }

    @Test
    public void testUpdateLocalClaimForIdentityClaimWithManagedInUserStoreFalse()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim existingLocalClaim = new LocalClaim(identityClaimUri);
        existingLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> existingProperties = new HashMap<>();
        existingProperties.put("DisplayName", "Account Locked");
        existingLocalClaim.setClaimProperties(existingProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(identityClaimUri, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));

        LocalClaim updatedLocalClaim = new LocalClaim(identityClaimUri);
        updatedLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> updatedProperties = new HashMap<>();
        updatedProperties.put("DisplayName", "Account Locked Updated");
        updatedProperties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "false");
        updatedProperties.put(ClaimConstants.EXCLUDED_USER_STORES_PROPERTY, "PRIMARY,SECONDARY");
        updatedLocalClaim.setClaimProperties(updatedProperties);

        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn(null);

        service.updateLocalClaim(updatedLocalClaim, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .updateLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // When ManagedInUserStore is false, ExcludedUserStores should be removed.
        assertFalse(capturedProperties.containsKey(ClaimConstants.EXCLUDED_USER_STORES_PROPERTY));
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
    }

    @Test
    public void testUpdateLocalClaimForNonIdentityClaimWithManagedInUserStoreProperty()
            throws ClaimMetadataException {

        LocalClaim existingLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        existingLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        Map<String, String> existingProperties = new HashMap<>();
        existingProperties.put("DisplayName", "Username");
        existingLocalClaim.setClaimProperties(existingProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));

        LocalClaim updatedLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        updatedLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        Map<String, String> updatedProperties = new HashMap<>();
        updatedProperties.put("DisplayName", "Username Updated");
        updatedProperties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "false");
        updatedLocalClaim.setClaimProperties(updatedProperties);

        service.updateLocalClaim(updatedLocalClaim, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .updateLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // Should preserve the ManagedInUserStore property value provided.
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
    }

    @Test
    public void testAddLocalClaimForNonIdentityClaimWithoutManagedInUserStoreProperty()
            throws ClaimMetadataException {

        LocalClaim localClaimToBeAdded = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Username");
        localClaimToBeAdded.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());

        service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .addLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // Non-identity claim should have ManagedInUserStore=true by default.
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "true");
    }

    @Test
    public void testAddLocalClaimForIdentityClaimWithoutManagedInUserStoreProperty()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim localClaimToBeAdded = new LocalClaim(identityClaimUri);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked"));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Account Locked");
        localClaimToBeAdded.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());
        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn(null);

        service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .addLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // Identity claim should have ManagedInUserStore=false by default when not user-store based
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
    }

    @Test
    public void testAddLocalClaimForIdentityClaimDefaultsToFalse()
            throws ClaimMetadataException {

        // Note: validateAndSyncClaimStoreSettings in addLocalClaim doesn't check for user-store based
        // identity data store. It simply defaults identity claims to false if not explicitly set.
        // The user-store based check is only in setManagedInUserStoreProperty used by getLocalClaim.
        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim localClaimToBeAdded = new LocalClaim(identityClaimUri);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked"));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Account Locked");
        localClaimToBeAdded.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());

        service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .addLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // Identity claim should default to ManagedInUserStore=false in addLocalClaim.
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
    }

    @Test
    public void testAddLocalClaimForNonIdentityClaimWithManagedInUserStoreProperty()
            throws ClaimMetadataException {

        LocalClaim localClaimToBeAdded = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Username");
        claimProperties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "false");
        localClaimToBeAdded.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());

        service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .addLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // Should preserve the ManagedInUserStore property value provided.
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
    }

    @Test
    public void testAddLocalClaimForIdentityClaimExplicitlySetToFalse()
            throws ClaimMetadataException {

        // When ManagedInUserStore is explicitly set to false for an identity claim,
        // it should be preserved and not trigger user store validation.
        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim localClaimToBeAdded = new LocalClaim(identityClaimUri);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked"));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Account Locked");
        claimProperties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "false");
        localClaimToBeAdded.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());

        service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .addLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // Should preserve the ManagedInUserStore=false property value.
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
    }

    @Test
    public void testAddLocalClaimForIdentityClaimWithManagedInUserStoreFalse()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim localClaimToBeAdded = new LocalClaim(identityClaimUri);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked"));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Account Locked");
        claimProperties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "false");
        claimProperties.put(ClaimConstants.EXCLUDED_USER_STORES_PROPERTY, "PRIMARY,SECONDARY");
        localClaimToBeAdded.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());
        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn(null);

        service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .addLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // When ManagedInUserStore is false, ExcludedUserStores should be removed.
        assertFalse(capturedProperties.containsKey(ClaimConstants.EXCLUDED_USER_STORES_PROPERTY));
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
    }

    @Test
    public void testAddLocalClaimForIdentityClaimThrowsErrorWhenManagedInUserStoreFalseWithUserStoreBasedDataStore()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim localClaimToBeAdded = new LocalClaim(identityClaimUri);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked"));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Account Locked");
        claimProperties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "false");
        localClaimToBeAdded.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());
        // Mock user store based identity data store.
        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn("org.wso2.carbon.identity.governance.store.UserStoreBasedIdentityDataStore");

        try {
            service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);
            // Should not reach here.
            assertFalse(true, "Expected ClaimMetadataClientException to be thrown");
        } catch (ClaimMetadataClientException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_CLAIM_MUST_BE_MANAGED_IN_USER_STORE.getCode());
            assertEquals(e.getMessage(),
                    String.format(ERROR_CODE_CLAIM_MUST_BE_MANAGED_IN_USER_STORE.getMessage(),
                            identityClaimUri));
        }
        // Verify that the claim was not added.
        verify(unifiedClaimMetadataManager, times(0)).addLocalClaim(any(), anyInt());
    }

    @Test
    public void testUpdateLocalClaimForIdentityClaimThrowsErrorWhenManagedInUserStoreFalseWithUserStoreBasedDataStore()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim existingLocalClaim = new LocalClaim(identityClaimUri);
        existingLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> existingProperties = new HashMap<>();
        existingProperties.put("DisplayName", "Account Locked");
        existingProperties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "true");
        existingLocalClaim.setClaimProperties(existingProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(identityClaimUri, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));

        LocalClaim updatedLocalClaim = new LocalClaim(identityClaimUri);
        updatedLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> updatedProperties = new HashMap<>();
        updatedProperties.put("DisplayName", "Account Locked Updated");
        updatedProperties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "false");
        updatedLocalClaim.setClaimProperties(updatedProperties);

        // Mock user store based identity data store.
        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn("org.wso2.carbon.identity.governance.store.UserStoreBasedIdentityDataStore");

        try {
            service.updateLocalClaim(updatedLocalClaim, SUPER_TENANT_DOMAIN_NAME);
            // Should not reach here.
            assertFalse(true, "Expected ClaimMetadataClientException to be thrown");
        } catch (ClaimMetadataClientException e) {
            assertEquals(e.getErrorCode(), ERROR_CODE_CLAIM_MUST_BE_MANAGED_IN_USER_STORE.getCode());
            assertEquals(e.getMessage(),
                    String.format(ERROR_CODE_CLAIM_MUST_BE_MANAGED_IN_USER_STORE.getMessage(),
                            identityClaimUri));
        }
        // Verify that the claim was not updated.
        verify(unifiedClaimMetadataManager, times(0)).updateLocalClaim(any(), anyInt());
    }

    @Test
    public void testAddLocalClaimForIdentityClaimWithUserStoreBasedDataStoreDefaultsToTrue()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim localClaimToBeAdded = new LocalClaim(identityClaimUri);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes().add(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked"));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Account Locked");
        // Not setting ManagedInUserStore property.
        localClaimToBeAdded.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());
        // Mock user store based identity data store.
        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn("org.wso2.carbon.identity.governance.store.UserStoreBasedIdentityDataStore");

        service.addLocalClaim(localClaimToBeAdded, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .addLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // When identity data store is user store based, identity claims should default to ManagedInUserStore=true.
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "true");
    }

    @Test
    public void testUpdateLocalClaimForIdentityClaimWithUserStoreBasedDataStoreDefaultsToTrue()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim existingLocalClaim = new LocalClaim(identityClaimUri);
        existingLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> existingProperties = new HashMap<>();
        existingProperties.put("DisplayName", "Account Locked");
        existingLocalClaim.setClaimProperties(existingProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(identityClaimUri, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));

        LocalClaim updatedLocalClaim = new LocalClaim(identityClaimUri);
        updatedLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> updatedProperties = new HashMap<>();
        updatedProperties.put("DisplayName", "Account Locked Updated");
        // Not setting ManagedInUserStore property.
        updatedLocalClaim.setClaimProperties(updatedProperties);

        // Mock user store based identity data store.
        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn("org.wso2.carbon.identity.governance.store.UserStoreBasedIdentityDataStore");

        service.updateLocalClaim(updatedLocalClaim, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .updateLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // When identity data store is user store based, identity claims should default to ManagedInUserStore=true.
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "true");
    }

    @Test
    public void testUpdateLocalClaimForIdentityClaimRemovesExcludedUserStoresWhenManagedInUserStoreFalse()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim existingLocalClaim = new LocalClaim(identityClaimUri);
        existingLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> existingProperties = new HashMap<>();
        existingProperties.put("DisplayName", "Account Locked");
        existingProperties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "true");
        existingProperties.put(ClaimConstants.EXCLUDED_USER_STORES_PROPERTY, "SECONDARY");
        existingLocalClaim.setClaimProperties(existingProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(identityClaimUri, SUPER_TENANT_ID))
                .thenReturn(Optional.of(existingLocalClaim));

        LocalClaim updatedLocalClaim = new LocalClaim(identityClaimUri);
        updatedLocalClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> updatedProperties = new HashMap<>();
        updatedProperties.put("DisplayName", "Account Locked Updated");
        updatedProperties.put(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY, "false");
        updatedProperties.put(ClaimConstants.EXCLUDED_USER_STORES_PROPERTY, "SECONDARY,TERTIARY");
        updatedLocalClaim.setClaimProperties(updatedProperties);

        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn(null);

        service.updateLocalClaim(updatedLocalClaim, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .updateLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // When ManagedInUserStore is false, ExcludedUserStores should be removed.
        assertFalse(capturedProperties.containsKey(ClaimConstants.EXCLUDED_USER_STORES_PROPERTY));
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
    }

    @Test
    public void testGetLocalClaimWithManagedInUserStoreInfoForIdentityClaim() throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim localClaim = new LocalClaim(identityClaimUri);
        localClaim.setMappedAttributes(Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Account Locked");
        localClaim.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(identityClaimUri, SUPER_TENANT_ID))
                .thenReturn(Optional.of(localClaim));
        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn(null);

        Optional<LocalClaim> result = service.getLocalClaim(identityClaimUri, SUPER_TENANT_DOMAIN_NAME, true);

        assertTrue(result.isPresent());
        Map<String, String> resultProperties = result.get().getClaimProperties();
        // Identity claim should default to ManagedInUserStore=false when identity data store is not user store based.
        assertEquals(resultProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "false");
    }

    @Test
    public void testGetLocalClaimWithManagedInUserStoreInfoForNonIdentityClaim()
            throws ClaimMetadataException {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        localClaim.setMappedAttributes(Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Username");
        localClaim.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(localClaim));
        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn(null);

        Optional<LocalClaim> result = service.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME, true);

        assertTrue(result.isPresent());
        Map<String, String> resultProperties = result.get().getClaimProperties();
        // Non-identity claim should default to ManagedInUserStore=true.
        assertEquals(resultProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "true");
    }

    @Test
    public void testGetLocalClaimWithManagedInUserStoreInfoForIdentityClaimWithUserStoreBasedDataStore()
            throws ClaimMetadataException {

        String identityClaimUri = "http://wso2.org/claims/identity/accountLocked";
        LocalClaim localClaim = new LocalClaim(identityClaimUri);
        localClaim.setMappedAttributes(Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "accountLocked")));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Account Locked");
        localClaim.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(identityClaimUri, SUPER_TENANT_ID))
                .thenReturn(Optional.of(localClaim));
        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn("org.wso2.carbon.identity.governance.store.UserStoreBasedIdentityDataStore");

        Optional<LocalClaim> result = service.getLocalClaim(identityClaimUri, SUPER_TENANT_DOMAIN_NAME, true);

        assertTrue(result.isPresent());
        Map<String, String> resultProperties = result.get().getClaimProperties();
        // When identity data store is user store based, all claims should be ManagedInUserStore=true.
        assertEquals(resultProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "true");
    }

    @Test
    public void testGetLocalClaimWithManagedInUserStoreInfoFalse() throws ClaimMetadataException {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        localClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Username");
        localClaim.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.of(localClaim));

        Optional<LocalClaim> result = service.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME, false);

        assertTrue(result.isPresent());
        Map<String, String> resultProperties = result.get().getClaimProperties();
        // When includeManagedInUserStoreInfo is false, ManagedInUserStore property should not be set.
        assertFalse(resultProperties.containsKey(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY));
    }

    @Test
    public void testGetLocalClaimWithManagedInUserStoreInfoWhenClaimNotPresent()
            throws ClaimMetadataException {

        when(unifiedClaimMetadataManager.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_ID))
                .thenReturn(Optional.empty());

        Optional<LocalClaim> result = service.getLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME, true);

        assertFalse(result.isPresent());
    }

    @Test
    public void testValidateClaimAttributeMapping() throws ClaimMetadataException {

        LocalClaim localClaim1 = new LocalClaim(LOCAL_CLAIM_1);
        localClaim1.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));

        LocalClaim localClaim2 = new LocalClaim(LOCAL_CLAIM_2);
        localClaim2.setMappedAttributes(Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, "email")));

        List<LocalClaim> localClaimList = Arrays.asList(localClaim1, localClaim2);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt()))
                .thenReturn(Arrays.asList(new LocalClaim(LOCAL_CLAIM_1), new LocalClaim(LOCAL_CLAIM_2)));

        // Should not throw exception for valid claims.
        service.validateClaimAttributeMapping(localClaimList, SUPER_TENANT_DOMAIN_NAME);
    }

    @Test
    public void testValidateClaimAttributeMappingWithEmptyClaimURI() {

        LocalClaim localClaim = new LocalClaim("");
        localClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        List<LocalClaim> localClaimList = Collections.singletonList(localClaim);

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.validateClaimAttributeMapping(localClaimList, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testValidateClaimAttributeMappingWithNullClaim() {

        List<LocalClaim> localClaimList = Collections.singletonList(null);

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.validateClaimAttributeMapping(localClaimList, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testValidateClaimAttributeMappingWithEmptyMappedAttributes() {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        localClaim.setMappedAttributes(new ArrayList<>());
        List<LocalClaim> localClaimList = Collections.singletonList(localClaim);

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.validateClaimAttributeMapping(localClaimList, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testValidateClaimAttributeMappingWithNonExistingLocalClaim() throws ClaimMetadataException {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        localClaim.setMappedAttributes(Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        List<LocalClaim> localClaimList = Collections.singletonList(localClaim);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.validateClaimAttributeMapping(localClaimList, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testGetMaskingRegexForLocalClaim() throws ClaimMetadataException {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.MASKING_REGULAR_EXPRESSION_PROPERTY, "^(.{2})(.*)(.{2})$");
        localClaim.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(Collections.singletonList(localClaim));

        String maskingRegex = service.getMaskingRegexForLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME);

        assertEquals(maskingRegex, "^(.{2})(.*)(.{2})$");
    }

    @Test
    public void testGetMaskingRegexForNonExistingLocalClaim() throws ClaimMetadataException {

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());

        String maskingRegex = service.getMaskingRegexForLocalClaim(LOCAL_CLAIM_1, SUPER_TENANT_DOMAIN_NAME);

        assertNull(maskingRegex);
    }

    @Test
    public void testAddLocalClaimWithInvalidMinLengthProperty() throws ClaimMetadataException {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        localClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.MIN_LENGTH, "invalid");
        localClaim.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.addLocalClaim(localClaim, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testAddLocalClaimWithMinLengthBelowLimit() throws ClaimMetadataException {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        localClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.MIN_LENGTH, "-1");
        localClaim.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.addLocalClaim(localClaim, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testAddLocalClaimWithMaxLengthAboveLimit() throws ClaimMetadataException {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        localClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.MAX_LENGTH, "2000");
        localClaim.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());

        assertThrows(ClaimMetadataClientException.class, () -> {
            service.addLocalClaim(localClaim, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testAddLocalClaimWithValidMinMaxLength() throws ClaimMetadataException {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        localClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put(ClaimConstants.MIN_LENGTH, "5");
        claimProperties.put(ClaimConstants.MAX_LENGTH, "100");
        localClaim.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());

        service.addLocalClaim(localClaim, SUPER_TENANT_DOMAIN_NAME);

        verify(unifiedClaimMetadataManager, times(1)).addLocalClaim(any(), anyInt());
    }

    @Test
    public void testAddLocalClaimForNonIdentityClaimDefaultsToManagedInUserStoreTrue()
            throws ClaimMetadataException {

        LocalClaim localClaim = new LocalClaim(LOCAL_CLAIM_1);
        localClaim.setMappedAttributes(
                Collections.singletonList(new AttributeMapping(PRIMARY_DOMAIN, USERNAME_ATTRIBUTE)));
        Map<String, String> claimProperties = new HashMap<>();
        claimProperties.put("DisplayName", "Username");
        localClaim.setClaimProperties(claimProperties);

        when(unifiedClaimMetadataManager.getLocalClaims(anyInt())).thenReturn(new ArrayList<>());
        identityUtilStaticMock.when(() -> IdentityUtil.getProperty("IdentityDataStore.DataStoreType"))
                .thenReturn(null);

        service.addLocalClaim(localClaim, SUPER_TENANT_DOMAIN_NAME);

        ArgumentCaptor<LocalClaim> capturedLocalClaim = ArgumentCaptor.forClass(LocalClaim.class);
        verify(unifiedClaimMetadataManager, times(1))
                .addLocalClaim(capturedLocalClaim.capture(), anyInt());

        Map<String, String> capturedProperties = capturedLocalClaim.getValue().getClaimProperties();
        // Non-identity claim should default to ManagedInUserStore=true.
        assertEquals(capturedProperties.get(ClaimConstants.MANAGED_IN_USER_STORE_PROPERTY), "true");
    }
}
