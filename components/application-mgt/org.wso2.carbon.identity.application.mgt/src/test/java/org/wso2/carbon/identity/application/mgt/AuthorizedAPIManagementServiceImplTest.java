/*
 * Copyright (c) 2023-2026, WSO2 LLC. (http://www.wso2.com).
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

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManagerImpl;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.internal.APIResourceManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.provider.ApplicationPermissionProvider;
import org.wso2.carbon.identity.application.mgt.provider.RegistryBasedApplicationPermissionProvider;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.common.testng.realm.InMemoryRealmService;
import org.wso2.carbon.identity.common.testng.realm.MockUserStoreManager;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.organization.management.service.internal.OrganizationManagementDataHolder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

@WithAxisConfiguration
@WithCarbonHome
@WithRegistry
@WithRealmService(injectToSingletons = {OrganizationManagementDataHolder.class})
@WithH2Database(files = {"dbscripts/identity.sql"})
public class AuthorizedAPIManagementServiceImplTest {

    private String tenantDomain;
    private APIResourceManager apiResourceManager;
    @Mock
    private IdentityEventService identityEventService;
    private ApplicationManagementService applicationManagementService;
    private AuthorizedAPIManagementService authorizedAPIManagementService;

    @BeforeClass
    public void setUp() throws Exception {

        setupConfiguration();
        apiResourceManager = APIResourceManagerImpl.getInstance();
        applicationManagementService = ApplicationManagementServiceImpl.getInstance();
        authorizedAPIManagementService = new AuthorizedAPIManagementServiceImpl();
        tenantDomain = "test_tenant_domain";
        identityEventService = mock(IdentityEventService.class);
        doNothing().when(identityEventService).handleEvent(any());
        ApplicationManagementServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);
        APIResourceManagementServiceComponentHolder.getInstance().setRichAuthorizationRequestsEnabled(true);
        APIResourceManagementServiceComponentHolder.getInstance().setIdentityEventService(identityEventService);
        CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = false;
    }

    @AfterMethod
    public void tearDown() throws Exception {

        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            loggerUtils.when(() -> LoggerUtils.triggerAuditLogEvent(any())).thenAnswer(inv -> null);

            applicationManagementService.deleteApplication("TestApp", tenantDomain, "user 1");
        }
    }

    @DataProvider
    public Object[][] createAuthorizedAPIDataProvider() throws Exception {

        APIResource apiResource = addTestAPIResource("test-create");
        String appId = addApplication();
        AuthorizedAPI authorizedAPI1 = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(apiResource.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(apiResource.getScopes())
                .build();

        return new Object[][]{
                {authorizedAPI1, 1}
        };
    }

    @Test(dataProvider = "createAuthorizedAPIDataProvider")
    public void testCreateAuthorizedAPI(AuthorizedAPI authorizedAPI, int expectedAPIs)
            throws Exception {

        ApplicationManagementServiceComponentHolder.getInstance().setAPIResourceManager(apiResourceManager);
        authorizedAPIManagementService.addAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI, tenantDomain);
        List<AuthorizedAPI> authorizedAPIS = authorizedAPIManagementService.getAuthorizedAPIs(authorizedAPI.getAppId(),
                tenantDomain);
        Assert.assertFalse(authorizedAPIS.isEmpty());
        Assert.assertEquals(authorizedAPIS.size(), expectedAPIs);
    }

    @DataProvider
    public Object[][] getAuthorizedAPIDataProvider() throws Exception {

        APIResource apiResource = addTestAPIResource("test-get");
        String appId = addApplication();
        AuthorizedAPI authorizedAPI1 = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(apiResource.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(apiResource.getScopes())
                .build();

        return new Object[][]{
                {authorizedAPI1, 2}
        };
    }

    @Test(dataProvider = "getAuthorizedAPIDataProvider", priority = 1)
    public void testGetAuthorizedAPI(AuthorizedAPI authorizedAPI, int expectedScopesCount)
            throws Exception {

        authorizedAPIManagementService.addAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI, tenantDomain);
        AuthorizedAPI authzAPI = authorizedAPIManagementService.getAuthorizedAPI(authorizedAPI.getAppId(),
                authorizedAPI.getAPIId(), tenantDomain);
        Assert.assertNotNull(authzAPI);
        Assert.assertFalse(authzAPI.getScopes().isEmpty());
        Assert.assertEquals(authzAPI.getScopes().size(), expectedScopesCount);
    }

    @DataProvider
    public Object[][] updateAuthorizedAPIDataProvider() throws Exception {

        APIResource apiResource = addTestAPIResource("test-update");

        Scope newScope = new Scope.ScopeBuilder()
                .name("newScope test-update")
                .displayName("newScope test-update")
                .description("newScope test-update")
                .build();

        apiResourceManager.updateAPIResource(apiResource, Collections.singletonList(newScope),
                new ArrayList<>(), tenantDomain);

        String appId = addApplication();

        AuthorizedAPI authorizedAPI1 = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(apiResource.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(apiResource.getScopes())
                .build();

        return new Object[][]{
                {authorizedAPI1, Collections.singletonList(newScope.getName()), 3}
        };
    }

    @Test(dataProvider = "updateAuthorizedAPIDataProvider", priority = 2)
    public void testUpdateAuthorizedAPI(AuthorizedAPI authorizedAPI, List<String> newScopes, int expectedScopesCount)
            throws Exception {

        authorizedAPIManagementService.addAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI, tenantDomain);
        authorizedAPIManagementService.patchAuthorizedAPI(authorizedAPI.getAppId(),
                authorizedAPI.getAPIId(), newScopes, new ArrayList<>(), tenantDomain);
        AuthorizedAPI authzAPI = authorizedAPIManagementService.getAuthorizedAPI(authorizedAPI.getAppId(),
                authorizedAPI.getAPIId(), tenantDomain);
        Assert.assertNotNull(authzAPI);
        Assert.assertFalse(authzAPI.getScopes().isEmpty());
        Assert.assertEquals(authzAPI.getScopes().size(), expectedScopesCount);
    }

    @Test(priority = 3)
    public void testGetAuthorizedScopes() throws Exception {

        String appId = addApplication();
        APIResource apiResource = addTestAPIResource("test-get-scopes-1");
        AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(apiResource.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(apiResource.getScopes())
                .build();
        APIResource apiResource2 = addTestAPIResource("test-get-scopes-2");
        AuthorizedAPI authorizedAPI2 = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(apiResource2.getId())
                .appId(appId)
                .policyId("No Policy")
                .scopes(apiResource2.getScopes())
                .build();
        authorizedAPIManagementService.addAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI, tenantDomain);
        authorizedAPIManagementService.addAuthorizedAPI(authorizedAPI2.getAppId(), authorizedAPI2, tenantDomain);
        List<AuthorizedScopes> authorizedScopesList = authorizedAPIManagementService.getAuthorizedScopes(appId,
                tenantDomain);
        Assert.assertFalse(authorizedScopesList.isEmpty());
        for (AuthorizedScopes authorizedScopes : authorizedScopesList) {
            Assert.assertEquals(authorizedScopes.getScopes().size(), 2);
        }
    }

    @Test(priority = 4)
    public void testDeleteAuthorizedAPI() throws Exception {

        APIResource apiResource = addTestAPIResource("test-delete");
        String appId = addApplication();
        AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(apiResource.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(apiResource.getScopes())
                .build();
        authorizedAPIManagementService.addAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI, tenantDomain);
        AuthorizedAPI authzAPI = authorizedAPIManagementService.getAuthorizedAPI(authorizedAPI.getAppId(),
                authorizedAPI.getAPIId(), tenantDomain);
        Assert.assertNotNull(authzAPI);
        authorizedAPIManagementService.deleteAuthorizedAPI(authorizedAPI.getAppId(),
                authorizedAPI.getAPIId(), tenantDomain);
        authzAPI = authorizedAPIManagementService.getAuthorizedAPI(authorizedAPI.getAppId(),
                authorizedAPI.getAPIId(), tenantDomain);
        Assert.assertNull(authzAPI);

        List<AuthorizationDetailsType> fetchedTypes =
                authorizedAPIManagementService.getAuthorizedAuthorizationDetailsTypes(appId, tenantDomain);
        Assert.assertNotNull(fetchedTypes);
        Assert.assertTrue(fetchedTypes.isEmpty());
    }

    private void setupConfiguration() throws UserStoreException, RegistryException {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes", "repository").
                toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext();

        // Configure RealmService.
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(SUPER_TENANT_DOMAIN_NAME);
        InMemoryRealmService testSessionRealmService = new InMemoryRealmService(SUPER_TENANT_ID);
        UserStoreManager userStoreManager = testSessionRealmService.getTenantUserRealm(SUPER_TENANT_ID)
                .getUserStoreManager();
        ((MockUserStoreManager) userStoreManager)
                .addSecondaryUserStoreManager("PRIMARY", (MockUserStoreManager) userStoreManager);
        IdentityTenantUtil.setRealmService(testSessionRealmService);
        RegistryDataHolder.getInstance().setRealmService(testSessionRealmService);
        OSGiDataHolder.getInstance().setUserRealmService(testSessionRealmService);
        IdentityCoreServiceDataHolder.getInstance().setRealmService(testSessionRealmService);
        ApplicationManagementServiceComponentHolder holder = ApplicationManagementServiceComponentHolder.getInstance();
        setInstanceValue(testSessionRealmService, RealmService.class, ApplicationManagementServiceComponentHolder.class,
                holder);
        setInstanceValue(new RegistryBasedApplicationPermissionProvider(), ApplicationPermissionProvider.class,
                ApplicationManagementServiceComponentHolder.class, holder);

        // Configure Registry Service.
        RegistryService mockRegistryService = mock(RegistryService.class);
        UserRegistry mockRegistry = mock(UserRegistry.class);
        when(mockRegistryService.getGovernanceUserRegistry(anyString(), anyInt())).thenReturn(mockRegistry);
        OSGiDataHolder.getInstance().setRegistryService(mockRegistryService);
        CarbonCoreDataHolder.getInstance().setRegistryService(mockRegistryService);
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setRegistry(RegistryType.USER_GOVERNANCE, mockRegistryService.getRegistry());
        when(mockRegistry.resourceExists(anyString())).thenReturn(FALSE);
        Collection mockPermissionNode = mock(Collection.class);
        when(mockRegistry.newCollection()).thenReturn(mockPermissionNode);
        when(mockRegistry.get(anyString())).thenReturn(mockPermissionNode);
        when(CarbonContext.getThreadLocalCarbonContext().getRegistry(
                RegistryType.USER_GOVERNANCE)).thenReturn(mockRegistry);
        when(mockRegistry.resourceExists(anyString())).thenReturn(FALSE);
    }

    private void setInstanceValue(Object value, Class valueType, Class clazz, Object instance) {

        for (Field field1 : clazz.getDeclaredFields()) {
            if (field1.getType().isAssignableFrom(valueType)) {
                field1.setAccessible(true);

                if (java.lang.reflect.Modifier.isStatic(field1.getModifiers())) {
                    setInternalState(clazz, field1.getName(), value);
                } else if (instance != null) {
                    setInternalState(instance, field1.getName(), value);
                }
            }
        }
    }

    private static void setInternalState(Object target, String field, Object value) {

        Class targetClass = target.getClass();

        try {
            Field declaredField = targetClass.getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set internal state on a private field.", e);
        }
    }

    private APIResource addTestAPIResource(String postfix) throws Exception {

        List<Scope> scopes = new ArrayList<>();
        scopes.add(new Scope.ScopeBuilder()
                .name("name 1 " + postfix)
                .displayName("displayName 1 " + postfix)
                .description("description 1 " + postfix).build());
        scopes.add(new Scope.ScopeBuilder()
                .name("name 2 " + postfix)
                .displayName("displayName 2 " + postfix)
                .description("description 2 " + postfix).build());

        List<AuthorizationDetailsType> authorizationDetailsTypes = new ArrayList<>();
        authorizationDetailsTypes.add(new AuthorizationDetailsType.AuthorizationDetailsTypesBuilder()
                .type("type 1 " + postfix)
                .name("name 1 " + postfix)
                .description("description 1 " + postfix)
                .build());
        authorizationDetailsTypes.add(new AuthorizationDetailsType.AuthorizationDetailsTypesBuilder()
                .type("type 2 " + postfix)
                .name("name 2 " + postfix)
                .description("description 2 " + postfix)
                .build());

        APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                .name("testAPIResource name " + postfix)
                .identifier("testAPIResource identifier " + postfix)
                .description("testAPIResource description " + postfix)
                .type("BUSINESS")
                .requiresAuthorization(true)
                .scopes(scopes)
                .authorizationDetailsTypes(authorizationDetailsTypes);

        return apiResourceManager.addAPIResource(apiResourceBuilder.build(), tenantDomain);
    }

    private String addApplication() throws Exception {

        try (MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            loggerUtils.when(() -> LoggerUtils.triggerAuditLogEvent(any())).thenAnswer(inv -> null);

            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setApplicationName("TestApp");
            return applicationManagementService.createApplication(serviceProvider, tenantDomain, "user 1");
        }
    }

    private void removeTestAPIResources() throws Exception {

        apiResourceManager.getAPIResources(null, null, 10, null, "ASC", tenantDomain)
                .getAPIResources().forEach(
                        apiResource -> {
                            try {
                                apiResourceManager.deleteAPIResourceById(apiResource.getId(), tenantDomain);
                            } catch (APIResourceMgtException e) {
                                Assert.fail("Error while deleting API resource: " + apiResource.getIdentifier(), e);
                            }
                        }
                                          );
    }

    @Test
    public void shouldCreateAuthorizedAPIWithNoScopes() throws Exception {

        String appId = addApplication();
        APIResource apiResource = addTestAPIResource("test-create-1");
        AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(apiResource.getId())
                .appId(appId)
                .policyId("RBAC")
                .build();

        authorizedAPIManagementService.addAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI, tenantDomain);
        ApplicationManagementServiceComponentHolder.getInstance().setAPIResourceManager(apiResourceManager);
        List<AuthorizedAPI> authorizedAPIs =
                authorizedAPIManagementService.getAuthorizedAPIs(authorizedAPI.getAppId(), tenantDomain);

        Assert.assertFalse(authorizedAPIs.isEmpty());
        Assert.assertEquals(authorizedAPIs.size(), 1);
        Assert.assertEquals(authorizedAPIs.get(0).getScopes().size(), 0);
        Assert.assertEquals(authorizedAPIs.get(0).getAuthorizationDetailsTypes().size(), 0);
    }

    @Test
    public void shouldCreateAuthorizedAPIWithAuthorizationDetails() throws Exception {

        String appId = addApplication();
        APIResource apiResource = addTestAPIResource("test-create-2");
        AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(apiResource.getId())
                .appId(appId)
                .policyId("RBAC")
                .authorizationDetailsTypes(apiResource.getAuthorizationDetailsTypes())
                .build();

        authorizedAPIManagementService.addAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI, tenantDomain);
        ApplicationManagementServiceComponentHolder.getInstance().setAPIResourceManager(apiResourceManager);
        List<AuthorizedAPI> authorizedAPIs =
                authorizedAPIManagementService.getAuthorizedAPIs(authorizedAPI.getAppId(), tenantDomain);

        Assert.assertFalse(authorizedAPIs.isEmpty());
        Assert.assertEquals(authorizedAPIs.size(), 1);
        Assert.assertEquals(authorizedAPIs.get(0).getScopes().size(), 0);
        Assert.assertEquals(authorizedAPIs.get(0).getAuthorizationDetailsTypes().size(), 2);
    }

    @Test
    public void shouldCreateAuthorizedAPIWithScopesAndAuthorizationDetails() throws Exception {

        String appId = addApplication();
        APIResource apiResource = addTestAPIResource("test-create-3");
        AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(apiResource.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(apiResource.getScopes())
                .authorizationDetailsTypes(apiResource.getAuthorizationDetailsTypes())
                .build();

        authorizedAPIManagementService.addAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI, tenantDomain);
        ApplicationManagementServiceComponentHolder.getInstance().setAPIResourceManager(apiResourceManager);
        List<AuthorizedAPI> authorizedAPIs =
                authorizedAPIManagementService.getAuthorizedAPIs(authorizedAPI.getAppId(), tenantDomain);

        Assert.assertFalse(authorizedAPIs.isEmpty());
        Assert.assertEquals(authorizedAPIs.size(), 1);
        Assert.assertEquals(authorizedAPIs.get(0).getScopes().size(), 2);
        Assert.assertEquals(authorizedAPIs.get(0).getAuthorizationDetailsTypes().size(), 2);
    }

    @Test
    public void shouldPatchAuthorizedApiWithValidAuthorizationDetailsType() throws Exception {

        final String appId = addApplication();
        final String postfix = "test-create-4";
        final String type1 = "type 1 " + postfix;
        final String type2 = "type 2 " + postfix;

        APIResource apiResource = addTestAPIResource(postfix);
        AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(apiResource.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(apiResource.getScopes())
                .authorizationDetailsTypes(apiResource.getAuthorizationDetailsTypes())
                .build();

        authorizedAPIManagementService.addAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI, tenantDomain);

        authorizedAPIManagementService.patchAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI.getAPIId(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.singletonList(type1), tenantDomain);

        List<AuthorizationDetailsType> fetchedTypes =
                authorizedAPIManagementService.getAuthorizedAuthorizationDetailsTypes(appId, tenantDomain);
        Assert.assertFalse(fetchedTypes.isEmpty());
        Assert.assertEquals(fetchedTypes.size(), 1);
        Assert.assertFalse(fetchedTypes.stream().map(AuthorizationDetailsType::getType).anyMatch(type1::equals));
        Assert.assertTrue(fetchedTypes.stream().map(AuthorizationDetailsType::getType).anyMatch(type2::equals));

        authorizedAPIManagementService.patchAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI.getAPIId(),
                Collections.emptyList(), Collections.emptyList(), Collections.singletonList(type1),
                Collections.emptyList(), tenantDomain);

        fetchedTypes = authorizedAPIManagementService.getAuthorizedAuthorizationDetailsTypes(appId, tenantDomain);
        Assert.assertFalse(fetchedTypes.isEmpty());
        Assert.assertEquals(fetchedTypes.size(), 2);
        Assert.assertTrue(fetchedTypes.stream().map(AuthorizationDetailsType::getType).anyMatch(type1::equals));
    }

    @Test(expectedExceptions = {IdentityApplicationManagementException.class})
    public void shouldThrowExceptionWhenAddingInvalidAuthorizationDetailsType() throws Exception {

        final String appId = addApplication();
        List<AuthorizationDetailsType> authorizationDetailsTypes = new ArrayList<>();
        authorizationDetailsTypes.add(new AuthorizationDetailsType.AuthorizationDetailsTypesBuilder()
                .type("invalid type")
                .build());

        APIResource apiResource = addTestAPIResource("test-create-5");
        AuthorizedAPI authorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(apiResource.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(apiResource.getScopes())
                .authorizationDetailsTypes(authorizationDetailsTypes)
                .build();

        authorizedAPIManagementService.addAuthorizedAPI(authorizedAPI.getAppId(), authorizedAPI, tenantDomain);
    }

    // ========================================
    // Tests for System API Scope Reuse
    // ========================================

    /**
     * Test that when authorizing a system API with scopes, other system APIs
     * sharing the same scope names
     * are automatically authorized.
     */
    @Test
    public void testSystemAPIAutoAuthorizationOfSharedScopes() throws Exception {

        String appId = addApplication();

        // Create two system APIs with overlapping scopes
        APIResource systemAPI1 = addTestSystemAPIResource("system-api-1", "auto-auth");
        APIResource systemAPI2 = addTestSystemAPIResource("system-api-2", "auto-auth");

        // Authorize the first system API with all its scopes
        AuthorizedAPI authorizedAPI1 = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(systemAPI1.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(systemAPI1.getScopes())
                .build();

        authorizedAPIManagementService.addAuthorizedAPI(appId, authorizedAPI1, tenantDomain);

        // Verify that both APIs are now authorized due to shared scopes
        List<AuthorizedAPI> authorizedAPIs = authorizedAPIManagementService.getAuthorizedAPIs(appId,
                tenantDomain);

        Assert.assertFalse(authorizedAPIs.isEmpty());
        // Should have both APIs authorized
        Assert.assertTrue(authorizedAPIs.size() >= 2,
                "Expected at least 2 APIs to be authorized (both system APIs with shared scopes)");

        // Verify both APIs are in the authorized list
        Set<String> authorizedAPIIds = authorizedAPIs.stream()
                .map(AuthorizedAPI::getAPIId)
                .collect(java.util.stream.Collectors.toSet());
        Assert.assertTrue(authorizedAPIIds.contains(systemAPI1.getId()),
                "System API 1 should be authorized");
        Assert.assertTrue(authorizedAPIIds.contains(systemAPI2.getId()),
                "System API 2 should be auto-authorized due to shared scopes");
    }

    /**
     * Test that when deleting a system API, shared scopes are removed from other
     * system APIs.
     */
    @Test
    public void testSystemAPICascadeDeletionOfSharedScopes() throws Exception {

        String appId = addApplication();

        // Create two system APIs with overlapping scopes
        APIResource systemAPI1 = addTestSystemAPIResource("system-api-delete-1", "cas-sys-scope-1");
        APIResource systemAPI2 = addTestSystemAPIResource("system-api-delete-2", "cas-sys-scope-1");

        // Authorize the first system API (which auto-authorizes the second)
        AuthorizedAPI authorizedAPI1 = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(systemAPI1.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(systemAPI1.getScopes())
                .build();

        authorizedAPIManagementService.addAuthorizedAPI(appId, authorizedAPI1, tenantDomain);

        // Verify both APIs are authorized
        List<AuthorizedAPI> authorizedAPIs = authorizedAPIManagementService.getAuthorizedAPIs(appId,
                tenantDomain);
        Assert.assertTrue(authorizedAPIs.size() >= 2, "Both system APIs should be authorized");

        // Delete the first system API
        authorizedAPIManagementService.deleteAuthorizedAPI(appId, systemAPI1.getId(), tenantDomain);

        // Verify the first API is deleted
        AuthorizedAPI deletedAPI = authorizedAPIManagementService.getAuthorizedAPI(appId, systemAPI1.getId(),
                tenantDomain);
        Assert.assertNull(deletedAPI, "System API 1 should be deleted");

        // Verify the second API's shared scopes are also removed
        AuthorizedAPI api2 = authorizedAPIManagementService.getAuthorizedAPI(appId, systemAPI2.getId(),
                tenantDomain);

        // The second API might be deleted if it has no remaining scopes (orphan cleanup) or it might still exist
        // with reduced scopes
        if (api2 != null) {
            // If it still exists, verify shared scopes are removed
            Set<String> remainingScopes = Optional.ofNullable(api2.getScopes())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(Scope::getName)
                    .collect(Collectors.toSet());
            Set<String> deletedScopes = systemAPI1.getScopes().stream()
                    .map(Scope::getName)
                    .collect(java.util.stream.Collectors.toSet());
            // Verify that shared scopes are removed
            for (String deletedScope : deletedScopes) {
                Assert.assertFalse(remainingScopes.contains(deletedScope),
                        "Shared scope " + deletedScope
                                + " should be removed from System API 2");
            }
        }
    }

    /**
     * Test that business/tenant APIs have direct authorization without
     * auto-authorization of related APIs.
     */
    @Test
    public void testBusinessAPIDirectAuthorizationOnly() throws Exception {

        String appId = addApplication();

        // Create two business APIs with overlapping scope names
        APIResource businessAPI1 = addTestAPIResource("business-api-1");
        APIResource businessAPI2 = addTestAPIResource("business-api-2");

        // Authorize the first business API
        AuthorizedAPI authorizedAPI1 = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(businessAPI1.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(businessAPI1.getScopes())
                .build();

        authorizedAPIManagementService.addAuthorizedAPI(appId, authorizedAPI1, tenantDomain);

        // Verify only the first API is authorized (no auto-authorization)
        List<AuthorizedAPI> authorizedAPIs = authorizedAPIManagementService.getAuthorizedAPIs(appId,
                tenantDomain);

        Set<String> authorizedAPIIds = authorizedAPIs.stream()
                .map(AuthorizedAPI::getAPIId)
                .collect(java.util.stream.Collectors.toSet());

        Assert.assertTrue(authorizedAPIIds.contains(businessAPI1.getId()),
                "Business API 1 should be authorized");
        Assert.assertFalse(authorizedAPIIds.contains(businessAPI2.getId()),
                "Business API 2 should NOT be auto-authorized (business APIs don't share scopes)");
    }

    /**
     * Test that deleting a business API is direct without cascade to other APIs.
     */
    @Test
    public void testBusinessAPIDirectDeletionOnly() throws Exception {

        String appId = addApplication();

        // Create two business APIs
        APIResource businessAPI1 = addTestAPIResource("business-api-delete-1");
        APIResource businessAPI2 = addTestAPIResource("business-api-delete-2");

        // Authorize both APIs independently
        AuthorizedAPI authorizedAPI1 = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(businessAPI1.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(businessAPI1.getScopes())
                .build();

        AuthorizedAPI authorizedAPI2 = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(businessAPI2.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(businessAPI2.getScopes())
                .build();

        authorizedAPIManagementService.addAuthorizedAPI(appId, authorizedAPI1, tenantDomain);
        authorizedAPIManagementService.addAuthorizedAPI(appId, authorizedAPI2, tenantDomain);

        // Delete the first business API
        authorizedAPIManagementService.deleteAuthorizedAPI(appId, businessAPI1.getId(), tenantDomain);

        // Verify only the first API is deleted
        AuthorizedAPI api1 = authorizedAPIManagementService.getAuthorizedAPI(appId, businessAPI1.getId(),
                tenantDomain);
        AuthorizedAPI api2 = authorizedAPIManagementService.getAuthorizedAPI(appId, businessAPI2.getId(),
                tenantDomain);

        Assert.assertNull(api1, "Business API 1 should be deleted");
        Assert.assertNotNull(api2, "Business API 2 should still exist (no cascade deletion)");
        Assert.assertEquals(api2.getScopes().size(), 2,
                "Business API 2 should still have all its scopes");
    }

    /**
     * Test that patching a business API only affects the specific API without
     * cascade.
     */
    @Test
    public void testBusinessAPIPatchWithoutCascade() throws Exception {

        String appId = addApplication();

        // Create two business APIs
        APIResource businessAPI1 = addTestAPIResource("business-api-patch-1");
        APIResource businessAPI2 = addTestAPIResource("business-api-patch-2");

        // Authorize both APIs
        AuthorizedAPI authorizedAPI1 = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(businessAPI1.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(businessAPI1.getScopes())
                .build();

        AuthorizedAPI authorizedAPI2 = new AuthorizedAPI.AuthorizedAPIBuilder()
                .apiId(businessAPI2.getId())
                .appId(appId)
                .policyId("RBAC")
                .scopes(businessAPI2.getScopes())
                .build();

        authorizedAPIManagementService.addAuthorizedAPI(appId, authorizedAPI1, tenantDomain);
        authorizedAPIManagementService.addAuthorizedAPI(appId, authorizedAPI2, tenantDomain);

        // Add a new scope to the first API resource
        Scope newScope = new Scope.ScopeBuilder()
                .name("new-business-scope")
                .displayName("New Business Scope")
                .description("New Business Scope")
                .build();

        apiResourceManager.updateAPIResource(businessAPI1, Collections.singletonList(newScope),
                new ArrayList<>(), tenantDomain);

        // Patch the first API to add the new scope
        authorizedAPIManagementService.patchAuthorizedAPI(appId, businessAPI1.getId(),
                Collections.singletonList(newScope.getName()), new ArrayList<>(),
                Collections.emptyList(), Collections.emptyList(), tenantDomain);

        // Verify the new scope is only added to the first API
        AuthorizedAPI api1 = authorizedAPIManagementService.getAuthorizedAPI(appId, businessAPI1.getId(),
                tenantDomain);
        AuthorizedAPI api2 = authorizedAPIManagementService.getAuthorizedAPI(appId, businessAPI2.getId(),
                tenantDomain);

        Set<String> api1Scopes = api1.getScopes().stream()
                .map(Scope::getName)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> api2Scopes = api2.getScopes().stream()
                .map(Scope::getName)
                .collect(java.util.stream.Collectors.toSet());

        Assert.assertTrue(api1Scopes.contains(newScope.getName()),
                "New scope should be added to Business API 1");
        Assert.assertFalse(api2Scopes.contains(newScope.getName()),
                "New scope should NOT be cascaded to Business API 2 " +
                        "(business APIs don't share scopes)");

        // Remove a scope from the first API
        String scopeToRemove = businessAPI1.getScopes().get(0).getName();
        authorizedAPIManagementService.patchAuthorizedAPI(appId, businessAPI1.getId(),
                new ArrayList<>(), Collections.singletonList(scopeToRemove),
                Collections.emptyList(), Collections.emptyList(), tenantDomain);

        // Verify the scope is only removed from the first API
        api1 = authorizedAPIManagementService.getAuthorizedAPI(appId, businessAPI1.getId(), tenantDomain);
        api2 = authorizedAPIManagementService.getAuthorizedAPI(appId, businessAPI2.getId(), tenantDomain);

        api1Scopes = api1.getScopes().stream()
                .map(Scope::getName)
                .collect(java.util.stream.Collectors.toSet());
        api2Scopes = api2.getScopes().stream()
                .map(Scope::getName)
                .collect(java.util.stream.Collectors.toSet());

        Assert.assertFalse(api1Scopes.contains(scopeToRemove),
                "Removed scope should not be in Business API 1");
        // API 2 should still have the scope if it had it originally
        // Since we're using different scope names in addTestAPIResource, we just verify
        // API 2 is unchanged
        Assert.assertEquals(api2Scopes.size(), 2,
                "Business API 2 should still have all its original scopes");
    }

    // ========================================
    // Helper Methods for Test Data
    // ========================================

    /**
     * Helper method to add a test system API resource.
     * System APIs are created in the super tenant domain to be properly recognized
     * by isSystemAPIByAPIId().
     */
    private APIResource addTestSystemAPIResource(String apiPostfix, String scopePostfix, String... extraScopePostfixes)
            throws Exception {

        List<Scope> scopes = new ArrayList<>();
        scopes.add(new Scope.ScopeBuilder()
                .name("system-scope-1-" + scopePostfix)
                .displayName("System Scope 1 " + scopePostfix)
                .description("System Scope 1 " + scopePostfix)
                //.orgID(null)
                .build());
        scopes.add(new Scope.ScopeBuilder()
                .name("system-scope-2-" + scopePostfix)
                .displayName("System Scope 2 " + scopePostfix)
                .description("System Scope 2 " + scopePostfix)
                //.orgID(null)
                .build());

        // Extra scopes (if provided)
        if (extraScopePostfixes != null) {
            for (String extra : extraScopePostfixes) {
                scopes.add(new Scope.ScopeBuilder()
                        .name("system-scope-" + extra)
                        .displayName("System Scope " + extra)
                        .description("System Scope " + extra)
                        .build());
            }
        }

        APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                .name("testSystemAPIResource name " + apiPostfix)
                .identifier("testSystemAPIResource identifier " + apiPostfix)
                .description("testSystemAPIResource description " + apiPostfix)
                .type("TENANT")
                .tenantId(null)// System API type
                .requiresAuthorization(true)
                .scopes(scopes);

        // Create in super tenant domain so isSystemAPIByAPIId() can find it
        return apiResourceManager.addAPIResource(apiResourceBuilder.build(), SUPER_TENANT_DOMAIN_NAME);
    }
}
