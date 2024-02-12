/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManagerImpl;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.provider.ApplicationPermissionProvider;
import org.wso2.carbon.identity.application.mgt.provider.RegistryBasedApplicationPermissionProvider;
import org.wso2.carbon.identity.common.testng.WithAxisConfiguration;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.common.testng.realm.InMemoryRealmService;
import org.wso2.carbon.identity.common.testng.realm.MockUserStoreManager;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
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

import static java.lang.Boolean.FALSE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

@WithAxisConfiguration
@WithCarbonHome
@WithRegistry
@WithRealmService(injectToSingletons = {OrganizationManagementDataHolder.class})
@WithH2Database(files = {"dbscripts/identity.sql"})
public class AuthorizedAPIManagementServiceImplTest extends PowerMockTestCase {

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
        for (AuthorizedScopes authorizedScopes: authorizedScopesList) {
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

        APIResource.APIResourceBuilder apiResourceBuilder = new APIResource.APIResourceBuilder()
                .name("testAPIResource name " + postfix)
                .identifier("testAPIResource identifier " + postfix)
                .description("testAPIResource description " + postfix)
                .type("BUSINESS")
                .requiresAuthorization(true)
                .scopes(scopes);
        return apiResourceManager.addAPIResource(apiResourceBuilder.build(), tenantDomain);
    }

    private String addApplication() throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName("TestApp");
        return applicationManagementService.createApplication(serviceProvider, tenantDomain, "user 1");
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


}
