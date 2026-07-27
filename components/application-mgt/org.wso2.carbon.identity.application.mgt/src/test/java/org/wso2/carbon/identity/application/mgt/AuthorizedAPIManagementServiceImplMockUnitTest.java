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

package org.wso2.carbon.identity.application.mgt;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.mgt.dao.AuthorizedAPIDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationMgtListenerServiceComponent;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.AUTHORIZE_ALL_SCOPES;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.AUTHORIZE_INTERNAL_SCOPES;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.MERGE_AUTHORIZED_SCOPES_BY_POLICY;

/**
 * Unit tests for AuthorizedAPIManagementServiceImpl.
 * All external dependencies (DB, OSGi services) are mocked.
 */
@WithCarbonHome
public class AuthorizedAPIManagementServiceImplMockUnitTest {

    private static final String APP_ID = "test-app-id";
    private static final String TENANT_DOMAIN = "test.tenant.com";
    private static final int TENANT_ID = 1;

    private AuthorizedAPIManagementServiceImpl service;
    private AuthorizedAPIDAO mockDAO;
    private APIResourceManager mockAPIResourceManager;
    private ApplicationManagementServiceImpl mockAppMgtService;

    private MockedStatic<ApplicationManagementServiceImpl> mockedAppMgtServiceClass;
    private MockedStatic<ApplicationMgtListenerServiceComponent> mockedListenerComponent;
    private MockedStatic<IdentityUtil> mockedIdentityUtil;
    private MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        service = new AuthorizedAPIManagementServiceImpl();

        mockDAO = mock(AuthorizedAPIDAO.class);
        Field daoField = AuthorizedAPIManagementServiceImpl.class.getDeclaredField("authorizedAPIDAO");
        daoField.setAccessible(true);
        daoField.set(service, mockDAO);

        mockAPIResourceManager = mock(APIResourceManager.class);
        ApplicationManagementServiceComponentHolder.getInstance().setAPIResourceManager(mockAPIResourceManager);

        mockAppMgtService = mock(ApplicationManagementServiceImpl.class);

        mockedAppMgtServiceClass = mockStatic(ApplicationManagementServiceImpl.class);
        mockedAppMgtServiceClass.when(ApplicationManagementServiceImpl::getInstance).thenReturn(mockAppMgtService);

        mockedListenerComponent = mockStatic(ApplicationMgtListenerServiceComponent.class);
        mockedListenerComponent.when(ApplicationMgtListenerServiceComponent::getAuthorizedAPIManagementListeners)
                .thenReturn(Collections.emptyList());

        mockedIdentityUtil = mockStatic(IdentityUtil.class);
        mockedIdentityUtil.when(() -> IdentityUtil.getProperty(anyString())).thenReturn(null);

        mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
        mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        when(mockAppMgtService.getMainAppId(APP_ID)).thenReturn(null);
    }

    @AfterMethod
    public void tearDown() {

        mockedAppMgtServiceClass.close();
        mockedListenerComponent.close();
        mockedIdentityUtil.close();
        mockedIdentityTenantUtil.close();
    }

    private void stubProperties(boolean mergeByPolicy) {

        mockedIdentityUtil.when(() -> IdentityUtil.getProperty(AUTHORIZE_ALL_SCOPES)).thenReturn("true");
        mockedIdentityUtil.when(() -> IdentityUtil.getProperty(AUTHORIZE_INTERNAL_SCOPES)).thenReturn("false");
        mockedIdentityUtil.when(() -> IdentityUtil.getProperty(MERGE_AUTHORIZED_SCOPES_BY_POLICY))
                .thenReturn(String.valueOf(mergeByPolicy));
    }

    private static Scope scope(String name) {

        return new Scope.ScopeBuilder().name(name).build();
    }

    private static AuthorizedScopes authorizedScopes(String policyId, String... scopeNames) {

        return new AuthorizedScopes(policyId, new ArrayList<>(Arrays.asList(scopeNames)));
    }

    @Test
    public void shouldMergeInternalScopesIntoMatchingAllTenantPolicyEntry() throws Exception {

        stubProperties(true);
        when(mockAPIResourceManager.getScopesByTenantDomain(TENANT_DOMAIN, null))
                .thenReturn(Arrays.asList(scope("regular_scope"), scope("internal_scope")));
        when(mockDAO.getAuthorizedScopes(APP_ID, TENANT_ID))
                .thenReturn(Collections.singletonList(
                        authorizedScopes("RBAC", "regular_scope", "internal_scope")));

        List<AuthorizedScopes> result = service.getAuthorizedScopes(APP_ID, TENANT_DOMAIN);

        Assert.assertEquals(result.size(), 1, "Expected a single merged RBAC entry");
        AuthorizedScopes rbac = result.get(0);
        Assert.assertEquals(rbac.getPolicyId(), "RBAC");
        Assert.assertTrue(rbac.getScopes().contains("regular_scope"),
                "Non-internal scope from all-tenant map must be present");
        Assert.assertTrue(rbac.getScopes().contains("internal_scope"),
                "Internal scope from app subscription must be merged into the RBAC entry");
    }

    @Test
    public void shouldAddAppOnlyPolicyIdAsSeparateEntry() throws Exception {

        stubProperties(true);
        when(mockAPIResourceManager.getScopesByTenantDomain(TENANT_DOMAIN, null))
                .thenReturn(Collections.singletonList(scope("regular_scope")));
        when(mockDAO.getAuthorizedScopes(APP_ID, TENANT_ID))
                .thenReturn(Arrays.asList(
                        authorizedScopes("RBAC", "regular_scope"),
                        authorizedScopes("No Policy", "internal_scope")));

        List<AuthorizedScopes> result = service.getAuthorizedScopes(APP_ID, TENANT_DOMAIN);

        AuthorizedScopes rbac = result.stream()
                .filter(s -> "RBAC".equals(s.getPolicyId())).findFirst().orElse(null);
        AuthorizedScopes noPolicy = result.stream()
                .filter(s -> "No Policy".equals(s.getPolicyId())).findFirst().orElse(null);

        Assert.assertEquals(result.size(), 2);
        Assert.assertNotNull(rbac, "RBAC entry must be present");
        Assert.assertNotNull(noPolicy, "App-only policy ID must be added as a separate entry");
        Assert.assertTrue(rbac.getScopes().contains("regular_scope"));
        Assert.assertTrue(noPolicy.getScopes().contains("internal_scope"));
    }

    @Test
    public void shouldDeduplicateInternalScopesWhenAppHasMultipleRbacEntries() throws Exception {

        stubProperties(true);
        when(mockAPIResourceManager.getScopesByTenantDomain(TENANT_DOMAIN, null))
                .thenReturn(Collections.singletonList(scope("regular_scope")));
        // Two app subscription entries share the RBAC policyId with an overlapping internal scope.
        when(mockDAO.getAuthorizedScopes(APP_ID, TENANT_ID))
                .thenReturn(Arrays.asList(
                        authorizedScopes("RBAC", "regular_scope", "internal_shared"),
                        authorizedScopes("RBAC", "internal_shared", "internal_other")));

        List<AuthorizedScopes> result = service.getAuthorizedScopes(APP_ID, TENANT_DOMAIN);

        AuthorizedScopes rbac = result.stream()
                .filter(s -> "RBAC".equals(s.getPolicyId())).findFirst().orElse(null);
        Assert.assertNotNull(rbac);
        long count = rbac.getScopes().stream().filter("internal_shared"::equals).count();
        Assert.assertEquals(count, 1, "Duplicate internal scope must not appear more than once in the merged result");
        Assert.assertTrue(rbac.getScopes().contains("internal_other"));
    }

    @Test
    public void shouldPreserveInternalScopesInNonRbacAppEntries() throws Exception {

        stubProperties(true);
        when(mockAPIResourceManager.getScopesByTenantDomain(TENANT_DOMAIN, null))
                .thenReturn(Collections.singletonList(scope("regular_scope")));
        // Non-RBAC app entry carries internal scopes; they must not be stripped.
        when(mockDAO.getAuthorizedScopes(APP_ID, TENANT_ID))
                .thenReturn(Arrays.asList(
                        authorizedScopes("RBAC", "regular_scope"),
                        authorizedScopes("No Policy", "internal_admin", "console_manage")));

        List<AuthorizedScopes> result = service.getAuthorizedScopes(APP_ID, TENANT_DOMAIN);

        AuthorizedScopes noPolicy = result.stream()
                .filter(s -> "No Policy".equals(s.getPolicyId())).findFirst().orElse(null);
        Assert.assertNotNull(noPolicy, "Non-RBAC app entry must be present");
        Assert.assertTrue(noPolicy.getScopes().contains("internal_admin"),
                "Internal scope in non-RBAC entry must not be filtered");
        Assert.assertTrue(noPolicy.getScopes().contains("console_manage"),
                "Console scope in non-RBAC entry must not be filtered");
    }

    @Test
    public void shouldAddAppScopesAsSeparateEntriesWhenMergeByPolicyDisabled() throws Exception {

        stubProperties(false);
        when(mockAPIResourceManager.getScopesByTenantDomain(TENANT_DOMAIN, null))
                .thenReturn(Arrays.asList(scope("regular_scope"), scope("internal_scope")));
        when(mockDAO.getAuthorizedScopes(APP_ID, TENANT_ID))
                .thenReturn(Collections.singletonList(
                        authorizedScopes("RBAC", "regular_scope", "internal_scope")));

        List<AuthorizedScopes> result = service.getAuthorizedScopes(APP_ID, TENANT_DOMAIN);

        // All-tenant entry (non-internal scopes only) and app entry are kept separate.
        boolean allTenantEntryStripsInternal = result.stream()
                .anyMatch(s -> "RBAC".equals(s.getPolicyId())
                        && s.getScopes().contains("regular_scope")
                        && !s.getScopes().contains("internal_scope"));
        boolean appEntryCarriesInternalScope = result.stream()
                .anyMatch(s -> "RBAC".equals(s.getPolicyId())
                        && s.getScopes().contains("internal_scope"));

        Assert.assertTrue(allTenantEntryStripsInternal,
                "All-tenant RBAC entry must not contain internal scopes when mergeByPolicy is disabled");
        Assert.assertTrue(appEntryCarriesInternalScope,
                "App-sourced entry must carry the internal scope as-is when mergeByPolicy is disabled");
    }
}
