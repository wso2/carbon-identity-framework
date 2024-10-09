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

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertThrows;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.LOCAL_CLAIM_DIALECT_URI;
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
    public void testAddLocalClaim() throws ClaimMetadataException {

        LocalClaim localClaimToBeAdded = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeAdded.setMappedAttributes(new ArrayList<>());
        localClaimToBeAdded.getMappedAttributes()
                .add(new AttributeMapping("PRIMARY", "username"));
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
                .add(new AttributeMapping("PRIMARY", "user_name"));

        LocalClaim existingLocalClaim = new LocalClaim(LOCAL_CLAIM_1);
        existingLocalClaim.setMappedAttributes(new ArrayList<>());
        existingLocalClaim.getMappedAttributes()
                .add(new AttributeMapping("PRIMARY", "username"));

        when(unifiedClaimMetadataManager.getLocalClaims(SUPER_TENANT_ID))
                .thenReturn(Collections.singletonList(existingLocalClaim));
        service.updateLocalClaim(localClaimToBeUpdated, SUPER_TENANT_DOMAIN_NAME);
        verify(unifiedClaimMetadataManager, times(1)).updateLocalClaim(any(), anyInt());

        when(unifiedClaimMetadataManager.getLocalClaims(SUPER_TENANT_ID)).thenReturn(new ArrayList<>());
        assertThrows(ClaimMetadataClientException.class, () -> {
            service.updateLocalClaim(localClaimToBeUpdated, SUPER_TENANT_DOMAIN_NAME);
        });
    }

    @Test
    public void testUpdateLocalClaimMappings() throws ClaimMetadataException {

        LocalClaim localClaimToBeUpdated = new LocalClaim(LOCAL_CLAIM_1);
        localClaimToBeUpdated.setMappedAttributes(new ArrayList<>());
        localClaimToBeUpdated.getMappedAttributes()
                .add(new AttributeMapping("PRIMARY", "user_name"));
        List<LocalClaim> localClaimsList = new ArrayList<>();
        localClaimsList.add(localClaimToBeUpdated);

        service.updateLocalClaimMappings(localClaimsList, SUPER_TENANT_DOMAIN_NAME, "PRIMARY");
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

    @AfterMethod
    public void tearDown() {

        dataHolderStaticMock.close();
        identityUtilStaticMock.close();
        identityTenantUtil.close();
        claimMetadataEventPublisherProxy.close();
    }
}
