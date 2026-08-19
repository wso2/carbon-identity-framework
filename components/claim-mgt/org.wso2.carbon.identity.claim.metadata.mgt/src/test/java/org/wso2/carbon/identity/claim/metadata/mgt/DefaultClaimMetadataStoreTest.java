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

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimConfigInitDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import static org.wso2.carbon.identity.base.IdentityConstants.ServerConfig.SKIP_CLAIM_METADATA_PERSISTENCE;

/**
 * Unit tests for {@link DefaultClaimMetadataStore}, focused on when the per-tenant claim configuration
 * initialization is seeded and when it is skipped.
 */
@WithCarbonHome
public class DefaultClaimMetadataStoreTest {

    private static final int PRIMARY_ORG_TENANT_ID = 1;
    private static final String PRIMARY_ORG_TENANT_DOMAIN = "primary.com";
    private static final int SUB_ORG_TENANT_ID = 2;
    private static final String SUB_ORG_TENANT_DOMAIN = "0e5b6d0b-9b6a-4f4c-8e0f-2b3c4d5e6f70";

    private MockedStatic<IdentityClaimManagementServiceDataHolder> dataHolderStaticMock;
    private MockedStatic<IdentityUtil> identityUtilStaticMock;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilStaticMock;
    private MockedStatic<OrganizationManagementUtil> organizationManagementUtilStaticMock;
    private MockedStatic<Utils> utilsStaticMock;
    private MockedConstruction<DBBasedClaimMetadataManager> dbBasedClaimMetadataManagerConstructionMock;

    private ClaimConfigInitDAO claimConfigInitDAO;

    /**
     * Isolates the claim metadata store from the database and from the organization management service, so each
     * test only has to declare the tenant type it exercises.
     */
    @BeforeMethod
    public void setUp() {

        dataHolderStaticMock = mockStatic(IdentityClaimManagementServiceDataHolder.class);
        identityUtilStaticMock = mockStatic(IdentityUtil.class);
        identityTenantUtilStaticMock = mockStatic(IdentityTenantUtil.class);
        organizationManagementUtilStaticMock = mockStatic(OrganizationManagementUtil.class);
        utilsStaticMock = mockStatic(Utils.class);

        claimConfigInitDAO = mock(ClaimConfigInitDAO.class);
        IdentityClaimManagementServiceDataHolder dataHolder = mock(IdentityClaimManagementServiceDataHolder.class);
        when(dataHolder.getClaimConfigInitDAO()).thenReturn(claimConfigInitDAO);
        dataHolderStaticMock.when(IdentityClaimManagementServiceDataHolder::getInstance).thenReturn(dataHolder);

        identityUtilStaticMock.when(() -> IdentityUtil.getProperty(SKIP_CLAIM_METADATA_PERSISTENCE))
                .thenReturn("false");
        identityTenantUtilStaticMock.when(() -> IdentityTenantUtil.getTenantDomain(PRIMARY_ORG_TENANT_ID))
                .thenReturn(PRIMARY_ORG_TENANT_DOMAIN);
        identityTenantUtilStaticMock.when(() -> IdentityTenantUtil.getTenantDomain(SUB_ORG_TENANT_ID))
                .thenReturn(SUB_ORG_TENANT_DOMAIN);

        /*
         * The constructor under test instantiates a DBBasedClaimMetadataManager directly, so its construction is
         * intercepted to keep the test away from the database. An empty dialect list means "not yet initialized",
         * which is the state in which the initialization would run.
         */
        dbBasedClaimMetadataManagerConstructionMock = mockConstruction(DBBasedClaimMetadataManager.class,
                (mockManager, context) -> when(mockManager.getClaimDialects(anyInt()))
                        .thenReturn(Collections.emptyList()));
    }

    /**
     * Closes the static and constructor mocks opened for the test.
     */
    @AfterMethod
    public void tearDown() {

        dbBasedClaimMetadataManagerConstructionMock.close();
        utilsStaticMock.close();
        organizationManagementUtilStaticMock.close();
        identityTenantUtilStaticMock.close();
        identityUtilStaticMock.close();
        dataHolderStaticMock.close();
    }

    /**
     * Provides the tenant types the claim configuration initialization has to distinguish between, together with
     * whether the initialization is expected to run for each of them.
     *
     * @return Tenant id, tenant domain, organization status, inheritance status and the expected outcome.
     */
    @DataProvider(name = "claimConfigInitializationDataProvider")
    public Object[][] claimConfigInitializationDataProvider() {

        return new Object[][]{
                // Primary organization: owns its claim metadata, hence must always be initialized.
                {PRIMARY_ORG_TENANT_ID, PRIMARY_ORG_TENANT_DOMAIN, false, false, true},
                {PRIMARY_ORG_TENANT_ID, PRIMARY_ORG_TENANT_DOMAIN, false, true, true},
                // v0 sub-organization: stores its own claim metadata, hence must still be initialized.
                {SUB_ORG_TENANT_ID, SUB_ORG_TENANT_DOMAIN, true, false, true},
                // v1 sub-organization: inherits claim metadata from the primary organization, hence skipped.
                {SUB_ORG_TENANT_ID, SUB_ORG_TENANT_DOMAIN, true, true, false},
        };
    }

    /**
     * Asserts that the claim configuration is seeded for every tenant that owns its claim metadata and skipped only
     * for sub-organizations that inherit it from the primary organization.
     *
     * @param tenantId             Tenant id the claim metadata store is initialized for.
     * @param tenantDomain         Tenant domain of the given tenant.
     * @param isOrganization       Whether the tenant belongs to an organization hierarchy.
     * @param isInheritanceEnabled Whether claim and OIDC scope inheritance is enabled for the tenant.
     * @param shouldInitialize     Whether the claim configuration initialization is expected to run.
     * @throws Exception If the test setup or the assertion fails.
     */
    @Test(dataProvider = "claimConfigInitializationDataProvider")
    public void testClaimConfigInitialization(int tenantId, String tenantDomain, boolean isOrganization,
                                              boolean isInheritanceEnabled, boolean shouldInitialize)
            throws Exception {

        organizationManagementUtilStaticMock.when(() -> OrganizationManagementUtil.isOrganization(tenantId))
                .thenReturn(isOrganization);
        utilsStaticMock.when(() -> Utils.isClaimAndOIDCScopeInheritanceEnabled(tenantDomain))
                .thenReturn(isInheritanceEnabled);

        DefaultClaimMetadataStore claimMetadataStore =
                new DefaultClaimMetadataStore(new ClaimConfig(), tenantId);

        verify(claimConfigInitDAO, shouldInitialize ? times(1) : never())
                .initClaimConfig(any(ClaimConfig.class), eq(tenantId));
        assertEquals(getTenantId(claimMetadataStore), tenantId,
                "The tenant id must be assigned regardless of whether the initialization ran.");
    }

    /**
     * Asserts that the initialization falls back to running when the tenant's organization status cannot be
     * resolved, since leaving a primary organization without claim metadata is worse than writing redundant rows.
     *
     * @throws Exception If the test setup or the assertion fails.
     */
    @Test
    public void testClaimConfigInitializationWhenOrganizationResolutionFails() throws Exception {

        organizationManagementUtilStaticMock.when(() -> OrganizationManagementUtil.isOrganization(SUB_ORG_TENANT_ID))
                .thenThrow(new OrganizationManagementException("Error while resolving the organization."));

        DefaultClaimMetadataStore claimMetadataStore =
                new DefaultClaimMetadataStore(new ClaimConfig(), SUB_ORG_TENANT_ID);

        /*
         * The tenant type cannot be resolved, so the initialization must fall back to running: leaving a primary
         * organization without claim metadata is worse than writing redundant rows for a sub-organization.
         */
        verify(claimConfigInitDAO, times(1)).initClaimConfig(any(ClaimConfig.class), eq(SUB_ORG_TENANT_ID));
        assertEquals(getTenantId(claimMetadataStore), SUB_ORG_TENANT_ID);
    }

    /**
     * Asserts that the initialization falls back to running when the tenant is a sub-organization but its
     * organization version, and hence whether it inherits claim metadata, cannot be resolved.
     *
     * @throws Exception If the test setup or the assertion fails.
     */
    @Test
    public void testClaimConfigInitializationWhenOrganizationVersionResolutionFails() throws Exception {

        organizationManagementUtilStaticMock.when(() -> OrganizationManagementUtil.isOrganization(SUB_ORG_TENANT_ID))
                .thenReturn(true);
        utilsStaticMock.when(() -> Utils.isClaimAndOIDCScopeInheritanceEnabled(SUB_ORG_TENANT_DOMAIN))
                .thenThrow(new OrganizationManagementException("Error while resolving the organization version."));

        DefaultClaimMetadataStore claimMetadataStore =
                new DefaultClaimMetadataStore(new ClaimConfig(), SUB_ORG_TENANT_ID);

        verify(claimConfigInitDAO, times(1)).initClaimConfig(any(ClaimConfig.class), eq(SUB_ORG_TENANT_ID));
        assertEquals(getTenantId(claimMetadataStore), SUB_ORG_TENANT_ID);
    }

    /**
     * Asserts that the organization version is not resolved for tenants that are not part of an organization
     * hierarchy, keeping the unnecessary lookup off the tenant creation path.
     *
     * @throws Exception If the test setup or the assertion fails.
     */
    @Test
    public void testOrganizationVersionIsNotResolvedForPrimaryOrganizations() throws Exception {

        organizationManagementUtilStaticMock
                .when(() -> OrganizationManagementUtil.isOrganization(PRIMARY_ORG_TENANT_ID)).thenReturn(false);

        new DefaultClaimMetadataStore(new ClaimConfig(), PRIMARY_ORG_TENANT_ID);

        verify(claimConfigInitDAO, times(1)).initClaimConfig(any(ClaimConfig.class), eq(PRIMARY_ORG_TENANT_ID));
        utilsStaticMock.verify(() -> Utils.isClaimAndOIDCScopeInheritanceEnabled(PRIMARY_ORG_TENANT_DOMAIN), never());
    }

    /**
     * Asserts that the pre-existing SkipClaimMetadataPersistence guard still short-circuits the initialization.
     *
     * @throws Exception If the test setup or the assertion fails.
     */
    @Test
    public void testClaimConfigInitializationSkippedWhenPersistenceIsSkipped() throws Exception {

        identityUtilStaticMock.when(() -> IdentityUtil.getProperty(SKIP_CLAIM_METADATA_PERSISTENCE))
                .thenReturn("true");
        organizationManagementUtilStaticMock
                .when(() -> OrganizationManagementUtil.isOrganization(PRIMARY_ORG_TENANT_ID)).thenReturn(false);

        new DefaultClaimMetadataStore(new ClaimConfig(), PRIMARY_ORG_TENANT_ID);

        verify(claimConfigInitDAO, never()).initClaimConfig(any(ClaimConfig.class), anyInt());
    }

    /**
     * Asserts that a tenant whose claim dialects are already persisted is not seeded a second time.
     *
     * @throws Exception If the test setup or the assertion fails.
     */
    @Test
    public void testClaimConfigInitializationSkippedWhenDialectsAlreadyExist() throws Exception {

        dbBasedClaimMetadataManagerConstructionMock.close();
        List<ClaimDialect> existingDialects = Collections.singletonList(new ClaimDialect("http://wso2.org/claims"));
        dbBasedClaimMetadataManagerConstructionMock = mockConstruction(DBBasedClaimMetadataManager.class,
                (mockManager, context) -> when(mockManager.getClaimDialects(anyInt())).thenReturn(existingDialects));
        organizationManagementUtilStaticMock
                .when(() -> OrganizationManagementUtil.isOrganization(PRIMARY_ORG_TENANT_ID)).thenReturn(false);

        new DefaultClaimMetadataStore(new ClaimConfig(), PRIMARY_ORG_TENANT_ID);

        verify(claimConfigInitDAO, never()).initClaimConfig(any(ClaimConfig.class), anyInt());
    }

    /**
     * Reads the tenant id the given claim metadata store was initialized with.
     *
     * @param claimMetadataStore Claim metadata store to read the tenant id from.
     * @return Tenant id assigned to the given claim metadata store.
     * @throws Exception If the tenant id field cannot be read.
     */
    private int getTenantId(DefaultClaimMetadataStore claimMetadataStore) throws Exception {

        Field tenantIdField = DefaultClaimMetadataStore.class.getDeclaredField("tenantId");
        tenantIdField.setAccessible(true);
        return (int) tenantIdField.get(claimMetadataStore);
    }
}
