/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.claim.metadata.mgt.cache.AssociatedClaimCache;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ExternalClaimCache;
import org.wso2.carbon.identity.claim.metadata.mgt.cache.ExternalClaimCacheKey;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CacheBackedUnifiedClaimMetadataManager}.
 */
@WithCarbonHome
@Test
public class CacheBackedUnifiedClaimMetadataManagerTest {

    private static final String EXT_CLAIM_DIALECT = "http://abc.org";
    private static final String EXT_CLAIM = "http://abc.org/claim1";
    private static final String LOCAL_CLAIM_1 = "http://wso2.org/claims/username";
    private static final String LOCAL_CLAIM_2 = "http://wso2.org/claims/email";
    private static final String TENANT_DOMAIN = "foo.com";
    private static final int TENANT_ID = 1;

    private CacheBackedUnifiedClaimMetadataManager claimMetadataManager;
    private DBBasedClaimMetadataManager dbBasedClaimMetadataManager;
    private ExternalClaimCache externalClaimCache;
    private AssociatedClaimCache associatedClaimCache;
    private MockedStatic<IdentityClaimManagementServiceDataHolder> dataHolderStaticMock;
    private MockedStatic<IdentityUtil> identityUtilStaticMock;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilStaticMock;
    private MockedStatic<Utils> utilsStaticMock;

    @BeforeMethod
    public void setUp() throws Exception {

        dataHolderStaticMock = mockStatic(IdentityClaimManagementServiceDataHolder.class);
        identityUtilStaticMock = mockStatic(IdentityUtil.class);
        identityTenantUtilStaticMock = mockStatic(IdentityTenantUtil.class);
        utilsStaticMock = mockStatic(Utils.class);
        identityTenantUtilStaticMock.when(() -> IdentityTenantUtil.getTenantDomain(TENANT_ID))
                .thenReturn(TENANT_DOMAIN);
        identityTenantUtilStaticMock.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
        utilsStaticMock.when(() -> Utils.isClaimAndOIDCScopeInheritanceEnabled(TENANT_DOMAIN)).thenReturn(false);
        IdentityClaimManagementServiceDataHolder dataHolder = mock(IdentityClaimManagementServiceDataHolder.class);
        dataHolderStaticMock.when(IdentityClaimManagementServiceDataHolder::getInstance).thenReturn(dataHolder);
        when(dataHolder.getClaimConfig()).thenReturn(new ClaimConfig());

        dbBasedClaimMetadataManager = mock(DBBasedClaimMetadataManager.class);
        externalClaimCache = mock(ExternalClaimCache.class);
        associatedClaimCache = mock(AssociatedClaimCache.class);

        claimMetadataManager = new CacheBackedUnifiedClaimMetadataManager();
        setSuperclassField("systemDefaultClaimMetadataManager", mock(SystemDefaultClaimMetadataManager.class));
        setSuperclassField("dbBasedClaimMetadataManager", dbBasedClaimMetadataManager);
        setSuperclassField("cacheBackedDBBasedClaimMetadataManager",
                mock(CacheBackedDBBasedClaimMetadataManager.class));
        claimMetadataManager.externalClaimCache = externalClaimCache;
        claimMetadataManager.associatedClaimCache = associatedClaimCache;

        ExternalClaim existingClaim = new ExternalClaim(EXT_CLAIM_DIALECT, EXT_CLAIM, LOCAL_CLAIM_1);
        when(dbBasedClaimMetadataManager.getExternalClaims(EXT_CLAIM_DIALECT, TENANT_ID))
                .thenReturn(Collections.singletonList(existingClaim));
        when(dbBasedClaimMetadataManager.getExternalClaim(EXT_CLAIM_DIALECT, EXT_CLAIM, TENANT_ID))
                .thenReturn(Optional.of(existingClaim));
        when(dbBasedClaimMetadataManager.getLocalClaim(any(), eq(TENANT_ID)))
                .thenAnswer(invocation -> Optional.of(new LocalClaim(invocation.getArgument(0))));
    }

    @AfterMethod
    public void tearDown() {

        dataHolderStaticMock.close();
        identityUtilStaticMock.close();
        identityTenantUtilStaticMock.close();
        utilsStaticMock.close();
    }

    @Test
    public void testUpdateExternalClaimClearsPreviousAndNewMappedLocalClaimCaches() throws ClaimMetadataException {

        claimMetadataManager.updateExternalClaim(new ExternalClaim(EXT_CLAIM_DIALECT, EXT_CLAIM, LOCAL_CLAIM_2),
                TENANT_ID);

        verify(externalClaimCache).clearCacheEntry(any(ExternalClaimCacheKey.class), eq(TENANT_ID));
        verify(associatedClaimCache).clearCacheEntry(LOCAL_CLAIM_1, TENANT_ID);
        verify(associatedClaimCache).clearCacheEntry(LOCAL_CLAIM_2, TENANT_ID);
    }

    @Test
    public void testUpdateExternalClaimWithUnchangedMappingClearsOnce() throws ClaimMetadataException {

        claimMetadataManager.updateExternalClaim(new ExternalClaim(EXT_CLAIM_DIALECT, EXT_CLAIM, LOCAL_CLAIM_1),
                TENANT_ID);

        verify(associatedClaimCache, times(1)).clearCacheEntry(LOCAL_CLAIM_1, TENANT_ID);
        verify(associatedClaimCache, never()).clearCacheEntry(LOCAL_CLAIM_2, TENANT_ID);
    }

    @Test
    public void testRemoveExternalClaimClearsMappedLocalClaimCache() throws ClaimMetadataException {

        claimMetadataManager.removeExternalClaim(EXT_CLAIM_DIALECT, EXT_CLAIM, TENANT_ID);

        verify(externalClaimCache).clearCacheEntry(any(ExternalClaimCacheKey.class), eq(TENANT_ID));
        verify(associatedClaimCache).clearCacheEntry(LOCAL_CLAIM_1, TENANT_ID);
    }

    private void setSuperclassField(String fieldName, Object value) throws Exception {

        Field field = UnifiedClaimMetadataManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(claimMetadataManager, value);
    }
}
