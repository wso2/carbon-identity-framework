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

package org.wso2.carbon.identity.application.mgt.listener;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.api.resource.mgt.util.APIResourceManagementUtil;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.IMPERSONATE_ORG_SCOPE_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.IMPERSONATE_SCOPE_NAME;

/**
 * Test class for MyAccountAuthorizedAPIListener.
 */
public class MyAccountAuthorizedAPIListenerTest {

    private MyAccountAuthorizedAPIListener myAccountAuthorizedAPIListener;
    
    @Mock
    private ApplicationManagementService applicationManagementService;
    
    @Mock
    private APIResourceManager apiResourceManager;
    
    @Mock
    private ApplicationManagementServiceComponentHolder componentHolder;

    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<ApplicationManagementService> applicationManagementServiceMock;
    private MockedStatic<APIResourceManagementUtil> apiResourceManagementUtil;
    private MockedStatic<ApplicationManagementServiceComponentHolder> componentHolderMock;

    private static final String TENANT_DOMAIN = "wso2.com";
    private static final String SUPER_TENANT = "carbon.super";
    private static final String MY_ACCOUNT_APP_ID = "myaccount-app-id";
    private static final String OTHER_APP_ID = "other-app-id";
    private static final String API_ID = "api-id";

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        myAccountAuthorizedAPIListener = new MyAccountAuthorizedAPIListener();
        
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        applicationManagementServiceMock = mockStatic(ApplicationManagementService.class);
        apiResourceManagementUtil = mockStatic(APIResourceManagementUtil.class);
        componentHolderMock = mockStatic(ApplicationManagementServiceComponentHolder.class);

        componentHolderMock.when(ApplicationManagementServiceComponentHolder::getInstance).thenReturn(componentHolder);
    }

    @AfterMethod
    public void tearDown() {

        identityTenantUtil.close();
        applicationManagementServiceMock.close();
        apiResourceManagementUtil.close();
        componentHolderMock.close();
    }

    @Test
    public void testGetExecutionOrderId() {

        assertEquals(myAccountAuthorizedAPIListener.getExecutionOrderId(), 2);
    }

    @Test
    public void testGetDefaultOrderId() {

        assertEquals(myAccountAuthorizedAPIListener.getDefaultOrderId(), 2);
    }

    @Test
    public void testIsEnable() {

        assertTrue(myAccountAuthorizedAPIListener.isEnable());
    }

    @DataProvider(name = "tenantDomainProvider")
    public Object[][] tenantDomainProvider() {

        return new Object[][]{
                {TENANT_DOMAIN, false},
                {SUPER_TENANT, false},
                {TENANT_DOMAIN, true}
        };
    }

    @Test(dataProvider = "tenantDomainProvider")
    public void testPostGetAuthorizedAPIsForMyAccountApp(String tenantDomain, boolean tenantQualifiedUrlsEnabled) 
            throws Exception {

        setupMockDependencies(tenantDomain, tenantQualifiedUrlsEnabled);

        List<AuthorizedAPI> authorizedAPIList = new ArrayList<>();
        List<APIResource> systemAPIResources = createTestSystemAPIResources();
        List<Scope> scopes = createTestScopes();

        apiResourceManagementUtil.when(() -> APIResourceManagementUtil.getSystemAPIs(tenantDomain))
                .thenReturn(systemAPIResources);
        when(componentHolder.getAPIResourceManager()).thenReturn(apiResourceManager);
        when(apiResourceManager.getAPIScopesById(anyString(), eq(tenantDomain))).thenReturn(scopes);

        myAccountAuthorizedAPIListener.postGetAuthorizedAPIs(authorizedAPIList, MY_ACCOUNT_APP_ID, tenantDomain);

        assertEquals(authorizedAPIList.size(), 1);
        AuthorizedAPI authorizedAPI = authorizedAPIList.get(0);
        assertEquals(authorizedAPI.getAppId(), MY_ACCOUNT_APP_ID);
        assertEquals(authorizedAPI.getAPIId(), "api-1");
        assertEquals(authorizedAPI.getPolicyId(), APIResourceManagementConstants.NO_POLICY);
        assertEquals(authorizedAPI.getAPIIdentifier(), "/api/users/v2/me/approval-tasks");
    }

    @Test
    public void testPostGetAuthorizedAPIsForNonMyAccountApp() throws Exception {
        
        setupMockDependencies(TENANT_DOMAIN, false);
        List<AuthorizedAPI> authorizedAPIList = new ArrayList<>();

        myAccountAuthorizedAPIListener.postGetAuthorizedAPIs(authorizedAPIList, OTHER_APP_ID, TENANT_DOMAIN);
        assertEquals(authorizedAPIList.size(), 0);
    }

    @Test
    public void testPostGetAuthorizedAPIsWithAPIResourceException() throws Exception {
        
        setupMockDependencies(TENANT_DOMAIN, false);
        
        List<AuthorizedAPI> authorizedAPIList = new ArrayList<>();

        apiResourceManagementUtil.when(() -> APIResourceManagementUtil.getSystemAPIs(TENANT_DOMAIN))
                .thenThrow(new APIResourceMgtException("Test exception"));

        try {
            myAccountAuthorizedAPIListener.postGetAuthorizedAPIs(authorizedAPIList, MY_ACCOUNT_APP_ID, TENANT_DOMAIN);
        } catch (IdentityApplicationManagementException e) {
            assertEquals(e.getMessage(), "Error while retrieving system APIs");
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof APIResourceMgtException);
        }
    }

    @Test(dataProvider = "tenantDomainProvider")
    public void testPostGetAuthorizedScopesForMyAccountApp(String tenantDomain, boolean tenantQualifiedUrlsEnabled) 
            throws Exception {
        
        setupMockDependencies(tenantDomain, tenantQualifiedUrlsEnabled);
        
        List<AuthorizedScopes> authorizedScopesList = new ArrayList<>();

        myAccountAuthorizedAPIListener.postGetAuthorizedScopes(authorizedScopesList, MY_ACCOUNT_APP_ID, tenantDomain);

        assertEquals(authorizedScopesList.size(), 2);
        authorizedScopesList.forEach(authorizedScopes -> {
            if (APIResourceManagementConstants.NO_POLICY.equals(authorizedScopes.getPolicyId())) {
                assertTrue(authorizedScopes.getScopes().contains("internal_approval_task_view"));
                assertTrue(authorizedScopes.getScopes().contains("internal_approval_task_update"));
                assertTrue(authorizedScopes.getScopes().contains("internal_org_approval_task_view"));
                assertTrue(authorizedScopes.getScopes().contains("internal_org_approval_task_update"));
            }

            if (APIResourceManagementConstants.RBAC_AUTHORIZATION.equals(authorizedScopes.getPolicyId())) {
                assertTrue(authorizedScopes.getScopes().contains(IMPERSONATE_SCOPE_NAME));
                assertTrue(authorizedScopes.getScopes().contains(IMPERSONATE_ORG_SCOPE_NAME));
            }
        });
    }

    @Test
    public void testPostGetAuthorizedScopesForNonMyAccountApp() throws Exception {
        
        setupMockDependencies(TENANT_DOMAIN, false);
        
        List<AuthorizedScopes> authorizedScopesList = new ArrayList<>();

        myAccountAuthorizedAPIListener.postGetAuthorizedScopes(authorizedScopesList, OTHER_APP_ID, TENANT_DOMAIN);
        assertEquals(authorizedScopesList.size(), 0);
    }

    @Test
    public void testPostGetAuthorizedAPIForMyAccountApp() throws Exception {
        
        setupMockDependencies(TENANT_DOMAIN, false);

        APIResource apiResource = createTestAPIResource("api-1", "/api/users/v2/me/approval-tasks");
        AuthorizedAPI inputAuthorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .appId(MY_ACCOUNT_APP_ID)
                .apiId(API_ID)
                .build();

        when(componentHolder.getAPIResourceManager()).thenReturn(apiResourceManager);
        when(apiResourceManager.getAPIResourceById(API_ID, TENANT_DOMAIN)).thenReturn(apiResource);

        AuthorizedAPI result = myAccountAuthorizedAPIListener.postGetAuthorizedAPI(
                inputAuthorizedAPI, MY_ACCOUNT_APP_ID, API_ID, TENANT_DOMAIN);

        assertNotNull(result);
        assertEquals(result.getAppId(), MY_ACCOUNT_APP_ID);
        assertEquals(result.getAPIId(), "api-1");
        assertEquals(result.getPolicyId(), APIResourceManagementConstants.NO_POLICY);
        assertEquals(result.getAPIIdentifier(), "/api/users/v2/me/approval-tasks");
    }

    @Test
    public void testPostGetAuthorizedAPIForNonMyAccountApp() throws Exception {
        
        setupMockDependencies(TENANT_DOMAIN, false);
        
        AuthorizedAPI inputAuthorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .appId(OTHER_APP_ID)
                .apiId(API_ID)
                .build();

        AuthorizedAPI result = myAccountAuthorizedAPIListener.postGetAuthorizedAPI(
                inputAuthorizedAPI, OTHER_APP_ID, API_ID, TENANT_DOMAIN);

        assertEquals(result, inputAuthorizedAPI);
    }

    @Test
    public void testPostGetAuthorizedAPIForUnauthorizedAPI() throws Exception {
        
        setupMockDependencies(TENANT_DOMAIN, false);

        APIResource apiResource = createTestAPIResource("api-1", "/unauthorized/api");
        AuthorizedAPI inputAuthorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .appId(MY_ACCOUNT_APP_ID)
                .apiId(API_ID)
                .build();

        when(componentHolder.getAPIResourceManager()).thenReturn(apiResourceManager);
        when(apiResourceManager.getAPIResourceById(API_ID, TENANT_DOMAIN)).thenReturn(apiResource);

        AuthorizedAPI result = myAccountAuthorizedAPIListener.postGetAuthorizedAPI(
                inputAuthorizedAPI, MY_ACCOUNT_APP_ID, API_ID, TENANT_DOMAIN);

        assertEquals(result, inputAuthorizedAPI);
    }

    @Test
    public void testPostGetAuthorizedAPIWithException() throws Exception {
        
        setupMockDependencies(TENANT_DOMAIN, false);
        
        AuthorizedAPI inputAuthorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .appId(MY_ACCOUNT_APP_ID)
                .apiId(API_ID)
                .build();

        when(componentHolder.getAPIResourceManager()).thenReturn(apiResourceManager);
        when(apiResourceManager.getAPIResourceById(API_ID, TENANT_DOMAIN))
                .thenThrow(new APIResourceMgtException("Test exception"));

        try {
            myAccountAuthorizedAPIListener.postGetAuthorizedAPI(
                    inputAuthorizedAPI, MY_ACCOUNT_APP_ID, API_ID, TENANT_DOMAIN);
        } catch (IdentityApplicationManagementException e) {
            assertEquals(e.getMessage(), "Error while retrieving system API");
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof APIResourceMgtException);
        }
    }

    private void setupMockDependencies(String tenantDomain, boolean tenantQualifiedUrlsEnabled) 
            throws IdentityApplicationManagementException {
        
        identityTenantUtil.when(IdentityTenantUtil::isTenantQualifiedUrlsEnabled)
                .thenReturn(tenantQualifiedUrlsEnabled);
        
        applicationManagementServiceMock.when(ApplicationManagementService::getInstance)
                .thenReturn(applicationManagementService);
        
        when(applicationManagementService.getApplicationResourceIDByInboundKey(
                anyString(), eq("oauth2"), eq(tenantDomain)))
                .thenReturn(MY_ACCOUNT_APP_ID);
    }

    private List<APIResource> createTestSystemAPIResources() {

        List<APIResource> resources = new ArrayList<>();
        
        APIResource resource1 = createTestAPIResource("api-1", "/api/users/v2/me/approval-tasks");
        APIResource resource2 = createTestAPIResource("api-2", "/unauthorized/api");
        
        resources.add(resource1);
        resources.add(resource2);
        
        return resources;
    }

    private APIResource createTestAPIResource(String id, String identifier) {

        return new APIResource.APIResourceBuilder()
                .id(id)
                .identifier(identifier)
                .name("Test API")
                .type("SYSTEM")
                .scopes(createTestScopes())
                .build();
    }

    private List<Scope> createTestScopes() {

        List<Scope> scopes = new ArrayList<>();
        Scope scope1 = new Scope("scope1-id", "internal_approval_task_view", 
                "Approval Task View", "View approval tasks");
        scopes.add(scope1);
        Scope scope2 = new Scope("scope2-id", "internal_approval_task_update", 
                "Approval Task Update", "Update approval tasks");
        scopes.add(scope2);
        return scopes;
    }
}
