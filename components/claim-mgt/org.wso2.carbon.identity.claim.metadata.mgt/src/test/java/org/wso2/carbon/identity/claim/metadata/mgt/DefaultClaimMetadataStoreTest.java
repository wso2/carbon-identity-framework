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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.claim.metadata.mgt.dao.ClaimConfigInitDAO;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.organization.management.service.util.Utils;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
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
 * Unit tests for {@link DefaultClaimMetadataStore}, covering which tenants get their claim configuration seeded on
 * initialization and which inherit it instead.
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
     * Isolates the claim metadata store from the database and from the organization management service. The mocks are
     * opened once for the class, since opening them is far more expensive than the assertions themselves, and only
     * the per-tenant stubbing differs between the test cases.
     */
    @BeforeClass
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
     * Closes the static and constructor mocks opened for the class.
     */
    @AfterClass
    public void tearDown() {

        dbBasedClaimMetadataManagerConstructionMock.close();
        utilsStaticMock.close();
        organizationManagementUtilStaticMock.close();
        identityTenantUtilStaticMock.close();
        identityUtilStaticMock.close();
        dataHolderStaticMock.close();
    }

    /**
     * Provides the tenant types the claim configuration initialization has to distinguish between. A {@code null}
     * organization status or inheritance status means the corresponding lookup fails for that tenant.
     *
     * @return Tenant id, tenant domain, organization status, inheritance status and the expected outcome.
     */
    @DataProvider(name = "claimConfigInitializationDataProvider")
    public Object[][] claimConfigInitializationDataProvider() {

        return new Object[][]{
                // Primary organization: owns its claim metadata, hence initialized.
                {PRIMARY_ORG_TENANT_ID, PRIMARY_ORG_TENANT_DOMAIN, false, false, true},
                // v0 sub-organization: stores its own claim metadata, hence still initialized.
                {SUB_ORG_TENANT_ID, SUB_ORG_TENANT_DOMAIN, true, false, true},
                // v1 sub-organization: inherits claim metadata from the primary organization, hence skipped.
                {SUB_ORG_TENANT_ID, SUB_ORG_TENANT_DOMAIN, true, true, false},
                // Organization status cannot be resolved: falls back to initializing.
                {SUB_ORG_TENANT_ID, SUB_ORG_TENANT_DOMAIN, null, false, true},
                // Organization version cannot be resolved: falls back to initializing.
                {SUB_ORG_TENANT_ID, SUB_ORG_TENANT_DOMAIN, true, null, true},
        };
    }

    /**
     * Asserts that the claim configuration is seeded for every tenant that owns its claim metadata, and skipped only
     * for sub-organizations that inherit it. When either organization lookup fails the initialization must still run,
     * since leaving a primary organization without claim metadata is worse than writing redundant rows.
     *
     * @param tenantId             Tenant id the claim metadata store is initialized for.
     * @param tenantDomain         Tenant domain of the given tenant.
     * @param isOrganization       Whether the tenant belongs to an organization hierarchy, {@code null} to fail.
     * @param isInheritanceEnabled Whether claim and OIDC scope inheritance is enabled, {@code null} to fail.
     * @param shouldInitialize     Whether the claim configuration initialization is expected to run.
     * @throws Exception If the test setup or the assertion fails.
     */
    @Test(dataProvider = "claimConfigInitializationDataProvider")
    public void testClaimConfigInitialization(int tenantId, String tenantDomain, Boolean isOrganization,
                                              Boolean isInheritanceEnabled, boolean shouldInitialize)
            throws Exception {

        clearInvocations(claimConfigInitDAO);
        /*
         * The organization lookups are the only stubbing that differs per test case, so they are reset rather than
         * re-stubbed on top of the previous case.
         */
        organizationManagementUtilStaticMock.reset();
        utilsStaticMock.reset();
        if (isOrganization == null) {
            organizationManagementUtilStaticMock.when(() -> OrganizationManagementUtil.isOrganization(tenantId))
                    .thenThrow(new OrganizationManagementException("Error while resolving the organization."));
        } else {
            organizationManagementUtilStaticMock.when(() -> OrganizationManagementUtil.isOrganization(tenantId))
                    .thenReturn(isOrganization);
        }
        if (isInheritanceEnabled == null) {
            utilsStaticMock.when(() -> Utils.isClaimAndOIDCScopeInheritanceEnabled(tenantDomain))
                    .thenThrow(new OrganizationManagementException("Error while resolving the organization version."));
        } else {
            utilsStaticMock.when(() -> Utils.isClaimAndOIDCScopeInheritanceEnabled(tenantDomain))
                    .thenReturn(isInheritanceEnabled);
        }

        DefaultClaimMetadataStore claimMetadataStore = new DefaultClaimMetadataStore(new ClaimConfig(), tenantId);

        verify(claimConfigInitDAO, shouldInitialize ? times(1) : never())
                .initClaimConfig(any(ClaimConfig.class), eq(tenantId));
        assertEquals(getTenantId(claimMetadataStore), tenantId,
                "The tenant id must be assigned regardless of whether the initialization ran.");
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
