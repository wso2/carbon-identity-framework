/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.role.v2.mgt.core.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantCache;
import org.wso2.carbon.user.core.tenant.TenantIdKey;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;

public class RoleManagementUtilsTest {

    private MockedStatic<RoleManagementServiceComponentHolder> mockedRoleManagementServiceComponentHolder;

    @Mock
    private RoleManagementServiceComponentHolder roleManagementServiceComponentHolder;

    @Mock
    private OrganizationManager organizationManager;

    @Mock
    private APIResourceManager apiResourceManager;

    private final String subOrgDomain = "subOrgDomain";
    private final String organizationId = "organizationId";
    private final String tenantDomain = "tenantDomain";
    private final String scopeFilter = "";

    @BeforeClass
    public void init() {

        openMocks(this);
    }

    @BeforeMethod
    public void setup() {

        mockedRoleManagementServiceComponentHolder = mockStatic(RoleManagementServiceComponentHolder.class);
        mockedRoleManagementServiceComponentHolder.when(RoleManagementServiceComponentHolder::getInstance).
                thenReturn(roleManagementServiceComponentHolder);
        when(roleManagementServiceComponentHolder.getOrganizationManager()).thenReturn(organizationManager);
        when(roleManagementServiceComponentHolder.getApiResourceManager())
                .thenReturn(apiResourceManager);
    }

    @AfterMethod
    public void tearDown() {

        mockedRoleManagementServiceComponentHolder.close();
    }

    @Test
    public void testGetOrganizationIdByTenantDomain()
            throws OrganizationManagementException, IdentityRoleManagementException {

        when(organizationManager.resolveOrganizationId(subOrgDomain)).thenReturn(organizationId);

        String resolvedOrgId = RoleManagementUtils.getOrganizationIdByTenantDomain(subOrgDomain);
        assert resolvedOrgId.equals(organizationId) : "Resolved organization ID does not match expected value.";
    }

    @Test(dataProvider = "validationOrganizationRoleAudienceProvider")
    public void testValidateOrganizationRoleAudience(boolean isValid,
                                                     boolean isOrganizationExistById, String organizationId)
            throws OrganizationManagementException, IdentityRoleManagementException {

        when(organizationManager.resolveOrganizationId(subOrgDomain)).thenReturn(organizationId);
        when(organizationManager.isOrganizationExistById(organizationId)).thenReturn(isOrganizationExistById);
        if (isValid) {
            RoleManagementUtils.validateOrganizationRoleAudience(organizationId, subOrgDomain);
        } else {
            try {
                RoleManagementUtils.validateOrganizationRoleAudience(organizationId, subOrgDomain);
            } catch (IdentityRoleManagementClientException e) {
                assert e.getErrorCode().equals(RoleConstants.Error.INVALID_AUDIENCE.getCode());
            }
        }
    }

    @DataProvider
    public Object[][] validationOrganizationRoleAudienceProvider() {
        return new Object[][]{
                {true, true, subOrgDomain},
                {false , false, subOrgDomain},
                {false, true, "invalidOrgId"},
                {false, true, null}
        };
    }

    @Test(dataProvider = "validatePermissionsForOrganizationProvider")
    public void testValidatePermissions(boolean isValid, List<Scope> scopes, List<Permission> permissions,
                                        String audience)
            throws IdentityRoleManagementException, APIResourceMgtException {

        when(apiResourceManager.getScopesByTenantDomain(tenantDomain, scopeFilter)).thenReturn(scopes);
        if (isValid) {
            RoleManagementUtils.validatePermissions(permissions, audience, tenantDomain);
        } else {
            try {
                RoleManagementUtils.validatePermissions(permissions, RoleConstants.ORGANIZATION, tenantDomain);
            } catch (IdentityRoleManagementException e) {
                assert e.getErrorCode().equals(RoleConstants.Error.INVALID_PERMISSION.getCode());
            }
        }
    }


    @DataProvider
    public Object[][] validatePermissionsForOrganizationProvider() {

        List<Scope> scopes1 = Arrays.asList(new Scope("1", "scope1", "scope 1", "scope 1"));
        List<Permission> permissions1 =  Arrays.asList(new Permission("scope1"));
        List<Permission> permissions2 = Arrays.asList(new Permission("scope2"));

        return new Object[][]{
                {true, scopes1, permissions1, RoleConstants.ORGANIZATION},
                {false, scopes1, permissions2, RoleConstants.ORGANIZATION},
                {true,  scopes1, permissions1, RoleConstants.APPLICATION},
        };
    }

    /*
     * resolveTenantOwnerUsername — see ISSUE-36404. The realm configuration is served from a node-local
     * cache that is not refreshed when the tenant's admin user changes underneath a running node, so the
     * tenant store is the authority and the cached value is only a fallback.
     */

    private static final int TENANT_ID = 11;
    private static final String CACHED_ADMIN = "ownera";
    private static final String CURRENT_OWNER = "userb";

    /**
     * Wire a RealmService whose tenant manager returns the given tenant for {@link #TENANT_ID}.
     */
    private TenantManager mockTenantManager(Tenant tenant) throws UserStoreException {

        RealmService realmService = mock(RealmService.class);
        TenantManager tenantManager = mock(TenantManager.class);
        when(roleManagementServiceComponentHolder.getRealmService()).thenReturn(realmService);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenantId(tenantDomain)).thenReturn(TENANT_ID);
        when(tenantManager.getTenant(TENANT_ID)).thenReturn(tenant);
        return tenantManager;
    }

    private Tenant mockTenant(String adminUserName) {

        Tenant tenant = mock(Tenant.class);
        RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);
        when(tenant.getRealmConfig()).thenReturn(realmConfiguration);
        when(realmConfiguration.getAdminUserName()).thenReturn(adminUserName);
        return tenant;
    }

    @Test
    public void testResolveTenantOwnerUsernameReturnsCurrentOwnerWhenCachedValueIsStale()
            throws UserStoreException {

        try (MockedStatic<TenantCache> tenantCache = mockStatic(TenantCache.class)) {
            TenantCache cache = mock(TenantCache.class);
            tenantCache.when(TenantCache::getInstance).thenReturn(cache);
            mockTenantManager(mockTenant(CURRENT_OWNER));

            assertEquals(RoleManagementUtils.resolveTenantOwnerUsername(tenantDomain, CACHED_ADMIN),
                    CURRENT_OWNER);
            // The tenant cache entry has to be dropped, otherwise the re-read is served from cache too.
            verify(cache).clearCacheEntry(any(TenantIdKey.class));
        }
    }

    @Test
    public void testResolveTenantOwnerUsernameReturnsCachedValueWhenItIsCurrent() throws UserStoreException {

        try (MockedStatic<TenantCache> tenantCache = mockStatic(TenantCache.class)) {
            TenantCache cache = mock(TenantCache.class);
            tenantCache.when(TenantCache::getInstance).thenReturn(cache);
            mockTenantManager(mockTenant(CACHED_ADMIN));

            assertEquals(RoleManagementUtils.resolveTenantOwnerUsername(tenantDomain, CACHED_ADMIN),
                    CACHED_ADMIN);
        }
    }

    @DataProvider(name = "unusableTenantStoreResponses")
    public Object[][] unusableTenantStoreResponses() {

        Tenant noRealmConfig = mock(Tenant.class);
        when(noRealmConfig.getRealmConfig()).thenReturn(null);
        return new Object[][]{
                {null},                 // tenant not found
                {noRealmConfig},        // tenant carries no realm configuration
                {mockTenant(null)},     // realm configuration carries no admin username
                {mockTenant("  ")},     // ... or a blank one
        };
    }

    @Test(dataProvider = "unusableTenantStoreResponses")
    public void testResolveTenantOwnerUsernameFallsBackWhenTenantStoreCannotAnswer(Tenant tenant)
            throws UserStoreException {

        try (MockedStatic<TenantCache> tenantCache = mockStatic(TenantCache.class)) {
            TenantCache cache = mock(TenantCache.class);
            tenantCache.when(TenantCache::getInstance).thenReturn(cache);
            mockTenantManager(tenant);

            assertEquals(RoleManagementUtils.resolveTenantOwnerUsername(tenantDomain, CACHED_ADMIN),
                    CACHED_ADMIN);
        }
    }

    @Test
    public void testResolveTenantOwnerUsernameFallsBackOnUserStoreException() throws UserStoreException {

        try (MockedStatic<TenantCache> tenantCache = mockStatic(TenantCache.class)) {
            TenantCache cache = mock(TenantCache.class);
            tenantCache.when(TenantCache::getInstance).thenReturn(cache);
            RealmService realmService = mock(RealmService.class);
            TenantManager tenantManager = mock(TenantManager.class);
            when(roleManagementServiceComponentHolder.getRealmService()).thenReturn(realmService);
            when(realmService.getTenantManager()).thenReturn(tenantManager);
            when(tenantManager.getTenantId(tenantDomain)).thenThrow(new UserStoreException("db down"));

            // A failure to read must not change the decision the caller would have made before this change.
            assertEquals(RoleManagementUtils.resolveTenantOwnerUsername(tenantDomain, CACHED_ADMIN),
                    CACHED_ADMIN);
        }
    }

    @Test
    public void testResolveTenantOwnerUsernameSkipsTenantStoreForSuperTenant() throws UserStoreException {

        try (MockedStatic<TenantCache> tenantCache = mockStatic(TenantCache.class)) {
            TenantCache cache = mock(TenantCache.class);
            tenantCache.when(TenantCache::getInstance).thenReturn(cache);
            RealmService realmService = mock(RealmService.class);
            TenantManager tenantManager = mock(TenantManager.class);
            when(roleManagementServiceComponentHolder.getRealmService()).thenReturn(realmService);
            when(realmService.getTenantManager()).thenReturn(tenantManager);
            when(tenantManager.getTenantId(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME))
                    .thenReturn(MultitenantConstants.SUPER_TENANT_ID);

            // The super tenant realm is built from the bootstrap configuration, not from UM_TENANT.
            assertEquals(RoleManagementUtils.resolveTenantOwnerUsername(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, CACHED_ADMIN), CACHED_ADMIN);
            verify(cache, never()).clearCacheEntry(any(TenantIdKey.class));
            verify(tenantManager, never()).getTenant(MultitenantConstants.SUPER_TENANT_ID);
        }
    }
}
