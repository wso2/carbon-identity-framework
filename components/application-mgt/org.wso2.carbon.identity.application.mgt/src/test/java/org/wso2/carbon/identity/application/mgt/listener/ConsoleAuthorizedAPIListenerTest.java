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
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class ConsoleAuthorizedAPIListenerTest {

    private ConsoleAuthorizedAPIListener consoleAuthorizedAPIListener;
    
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
    private MockedStatic<LoggerUtils> loggerUtils;
    
    private static final String TENANT_DOMAIN = "wso2.com";
    private static final String SUPER_TENANT = "carbon.super";
    private static final String CONSOLE_APP_ID = "console-app-id";
    private static final String OTHER_APP_ID = "other-app-id";
    private static final String API_ID = "api-id";

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        applicationManagementServiceMock = mockStatic(ApplicationManagementService.class);
        apiResourceManagementUtil = mockStatic(APIResourceManagementUtil.class);
        componentHolderMock = mockStatic(ApplicationManagementServiceComponentHolder.class);
        loggerUtils = mockStatic(LoggerUtils.class);

        loggerUtils.when(() -> LoggerUtils.triggerAuditLogEvent(any()))
                .thenAnswer(inv -> null);
        
        componentHolderMock.when(ApplicationManagementServiceComponentHolder::getInstance)
                .thenReturn(componentHolder);

        consoleAuthorizedAPIListener = new ConsoleAuthorizedAPIListener();
        IdentityUtil.threadLocalProperties.remove();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        IdentityUtil.threadLocalProperties.remove();

        identityTenantUtil.close();
        applicationManagementServiceMock.close();
        apiResourceManagementUtil.close();
        componentHolderMock.close();
        loggerUtils.close();
    }


    @DataProvider
    public Object[][] getPreAddAuthorizedAPITestData() {

        String appId1 = mockAppId();
        String appId2 = mockAppId();
        String appId3 = mockAppId();

        AuthorizedAPI authorizedAPI1 = getAuthorizedAPI("https://test.com/oauth2/token", appId1);
        AuthorizedAPI authorizedAPI2 = getAuthorizedAPI("https://test.com/applications", appId2);
        AuthorizedAPI authorizedAPI3 = getAuthorizedAPI("https://test.com/applications/local", appId3);

        return new Object[][]{
                // isTenantQualifiedURLsEnabled, appId, authorizedAPI, tenantDomain, consoleInboundKey, expectException
                { false, appId1, authorizedAPI1, ApplicationConstants.SUPER_TENANT,
                        ApplicationConstants.CONSOLE_APPLICATION_CLIENT_ID, true },
                { false, appId2, authorizedAPI2, "abc.com",
                        ApplicationConstants.CONSOLE_APPLICATION_CLIENT_ID + "_" + "abc.com", true },
                { true, appId3, authorizedAPI3, "abc.com", "client-id-test-1", false }
        };

    }

    @Test(dataProvider = "getPreAddAuthorizedAPITestData")
    public void testPreAddAuthorizedAPI(boolean isTenantQualifiedURLsEnabled, String appId,
                                        AuthorizedAPI authorizedAPI, String tenantDomain, String consoleInboundKey,
                                        boolean expectException) throws Exception {

        identityTenantUtil.when(IdentityTenantUtil::isTenantQualifiedUrlsEnabled)
                .thenReturn(isTenantQualifiedURLsEnabled);

        ApplicationManagementService applicationMgtSvc = mock(ApplicationManagementService.class);
        applicationManagementServiceMock.when(ApplicationManagementService::getInstance).thenReturn(applicationMgtSvc);
        when(applicationMgtSvc.getApplicationResourceIDByInboundKey(consoleInboundKey, "oauth2",
                tenantDomain)).thenReturn(appId);

        if (expectException) {
            IdentityApplicationManagementClientException e =
                    expectThrows(IdentityApplicationManagementClientException.class, () ->
                            consoleAuthorizedAPIListener.preAddAuthorizedAPI(appId, authorizedAPI, tenantDomain));

            assertEquals(e.getMessage(), "Adding authorized APIs to the console application is not allowed");
        } else {
            consoleAuthorizedAPIListener.preAddAuthorizedAPI(appId, authorizedAPI, tenantDomain);
        }
    }

    private AuthorizedAPI getAuthorizedAPI(String apiID, String appID) {

        AuthorizedAPI authorizedAPI = new AuthorizedAPI();
        authorizedAPI.setAPIId(apiID);
        authorizedAPI.setAppId(appID);

        return authorizedAPI;
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
    public void testPostGetAuthorizedAPIsForConsoleApp(String tenantDomain, boolean tenantQualifiedUrlsEnabled) 
            throws Exception {
        
        setupMockDependencies(tenantDomain, tenantQualifiedUrlsEnabled);
        
        List<AuthorizedAPI> authorizedAPIList = new ArrayList<>();
        List<APIResource> systemAPIResources = createTestSystemAPIResources();
        List<Scope> scopes = createTestScopes();
        
        apiResourceManagementUtil.when(() -> APIResourceManagementUtil.getSystemAPIs(tenantDomain))
                .thenReturn(systemAPIResources);
        when(componentHolder.getAPIResourceManager()).thenReturn(apiResourceManager);
        when(apiResourceManager.getAPIScopesById(anyString(), eq(tenantDomain))).thenReturn(scopes);
        
        consoleAuthorizedAPIListener.postGetAuthorizedAPIs(authorizedAPIList, CONSOLE_APP_ID, tenantDomain);
        
        assertEquals(authorizedAPIList.size(), 2);
        
        // Verify approval task API has NO_POLICY.
        AuthorizedAPI approvalTaskAPI = authorizedAPIList.stream()
                .filter(api -> "/api/users/v2/me/approval-tasks".equals(api.getAPIIdentifier()))
                .findFirst().orElse(null);
        assertNotNull(approvalTaskAPI);
        assertEquals(approvalTaskAPI.getPolicyId(), APIResourceManagementConstants.NO_POLICY);
        
        // Verify other API has RBAC_AUTHORIZATION.
        AuthorizedAPI otherAPI = authorizedAPIList.stream()
                .filter(api -> "/other/api".equals(api.getAPIIdentifier()))
                .findFirst().orElse(null);
        assertNotNull(otherAPI);
        assertEquals(otherAPI.getPolicyId(), APIResourceManagementConstants.RBAC_AUTHORIZATION);
    }

    @Test
    public void testPostGetAuthorizedAPIsForNonConsoleApp() throws Exception {

        setupMockDependencies(TENANT_DOMAIN, false);
        List<AuthorizedAPI> authorizedAPIList = new ArrayList<>();
        consoleAuthorizedAPIListener.postGetAuthorizedAPIs(authorizedAPIList, OTHER_APP_ID, TENANT_DOMAIN);
        assertEquals(authorizedAPIList.size(), 0);
    }

    @Test
    public void testPostGetAuthorizedAPIsWithAPIResourceException() throws Exception {

        setupMockDependencies(TENANT_DOMAIN, false);
        List<AuthorizedAPI> authorizedAPIList = new ArrayList<>();
        apiResourceManagementUtil.when(() -> APIResourceManagementUtil.getSystemAPIs(TENANT_DOMAIN))
                .thenThrow(new APIResourceMgtException("Test exception"));
        try {
            consoleAuthorizedAPIListener.postGetAuthorizedAPIs(authorizedAPIList, CONSOLE_APP_ID, TENANT_DOMAIN);
        } catch (IdentityApplicationManagementException e) {
            assertEquals(e.getMessage(), "Error while retrieving system APIs");
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof APIResourceMgtException);
        }
    }

    @Test(dataProvider = "tenantDomainProvider")
    public void testPostGetAuthorizedScopesForConsoleApp(String tenantDomain, boolean tenantQualifiedUrlsEnabled) 
            throws Exception {
        
        setupMockDependencies(tenantDomain, tenantQualifiedUrlsEnabled);

        List<AuthorizedScopes> authorizedScopesList = new ArrayList<>();
        List<Scope> systemAPIScopes = createTestSystemAPIScopes();
        
        when(componentHolder.getAPIResourceManager()).thenReturn(apiResourceManager);
        when(apiResourceManager.getSystemAPIScopes(tenantDomain)).thenReturn(systemAPIScopes);
        
        consoleAuthorizedAPIListener.postGetAuthorizedScopes(authorizedScopesList, CONSOLE_APP_ID, tenantDomain);
        
        assertEquals(authorizedScopesList.size(), 2);
        
        // Verify RBAC_AUTHORIZATION scopes.
        AuthorizedScopes rbacScopes = authorizedScopesList.stream()
                .filter(scope -> APIResourceManagementConstants.RBAC_AUTHORIZATION.equals(scope.getPolicyId()))
                .findFirst().orElse(null);
        assertNotNull(rbacScopes);
        assertTrue(rbacScopes.getScopes().contains("internal_org_config_mgt_view"));
        
        // Verify NO_POLICY scopes.
        AuthorizedScopes noPolicyScopes = authorizedScopesList.stream()
                .filter(scope -> APIResourceManagementConstants.NO_POLICY.equals(scope.getPolicyId()))
                .findFirst().orElse(null);
        assertNotNull(noPolicyScopes);
        assertTrue(noPolicyScopes.getScopes().contains("internal_approval_task_view"));
        assertTrue(noPolicyScopes.getScopes().contains("internal_approval_task_update"));
    }

    @Test
    public void testPostGetAuthorizedScopesForNonConsoleApp() throws Exception {

        setupMockDependencies(TENANT_DOMAIN, false);
        
        List<AuthorizedScopes> authorizedScopesList = new ArrayList<>();
        consoleAuthorizedAPIListener.postGetAuthorizedScopes(authorizedScopesList, OTHER_APP_ID, TENANT_DOMAIN);
        
        assertEquals(authorizedScopesList.size(), 0);
    }

    @Test
    public void testPostGetAuthorizedAPIForConsoleAppWithApprovalTaskAPI() throws Exception {

        setupMockDependencies(TENANT_DOMAIN, false);
        
        APIResource apiResource = createTestAPIResource("api-1", "/api/users/v2/me/approval-tasks");
        AuthorizedAPI inputAuthorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .appId(CONSOLE_APP_ID)
                .apiId(API_ID)
                .build();
        
        when(componentHolder.getAPIResourceManager()).thenReturn(apiResourceManager);
        when(apiResourceManager.getAPIResourceById(API_ID, TENANT_DOMAIN)).thenReturn(apiResource);
        
        AuthorizedAPI result = consoleAuthorizedAPIListener.postGetAuthorizedAPI(
                inputAuthorizedAPI, CONSOLE_APP_ID, API_ID, TENANT_DOMAIN);
        
        assertNotNull(result);
        assertEquals(result.getAppId(), CONSOLE_APP_ID);
        assertEquals(result.getPolicyId(), APIResourceManagementConstants.NO_POLICY);
        assertEquals(result.getAPIIdentifier(), "/api/users/v2/me/approval-tasks");
    }

    @Test
    public void testPostGetAuthorizedAPIForConsoleAppWithRegularAPI() throws Exception {

        setupMockDependencies(TENANT_DOMAIN, false);
        
        APIResource apiResource = createTestAPIResource("api-1", "/regular/api");
        AuthorizedAPI inputAuthorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .appId(CONSOLE_APP_ID)
                .apiId(API_ID)
                .build();
        
        when(componentHolder.getAPIResourceManager()).thenReturn(apiResourceManager);
        when(apiResourceManager.getAPIResourceById(API_ID, TENANT_DOMAIN)).thenReturn(apiResource);
        
        AuthorizedAPI result = consoleAuthorizedAPIListener.postGetAuthorizedAPI(
                inputAuthorizedAPI, CONSOLE_APP_ID, API_ID, TENANT_DOMAIN);
        
        assertNotNull(result);
        assertEquals(result.getAppId(), CONSOLE_APP_ID);
        assertEquals(result.getPolicyId(), APIResourceManagementConstants.RBAC_AUTHORIZATION);
        assertEquals(result.getAPIIdentifier(), "/regular/api");
    }

    @Test
    public void testPostGetAuthorizedAPIForNonConsoleApp() throws Exception {

        setupMockDependencies(TENANT_DOMAIN, false);
        
        AuthorizedAPI inputAuthorizedAPI = new AuthorizedAPI.AuthorizedAPIBuilder()
                .appId(OTHER_APP_ID)
                .apiId(API_ID)
                .build();
        
        AuthorizedAPI result = consoleAuthorizedAPIListener.postGetAuthorizedAPI(
                inputAuthorizedAPI, OTHER_APP_ID, API_ID, TENANT_DOMAIN);
        
        assertEquals(result, inputAuthorizedAPI);
    }

    @Test
    public void testPreDeleteAuthorizedAPIForConsoleApp() throws Exception {

        setupMockDependencies(TENANT_DOMAIN, false);
        
        try {
            consoleAuthorizedAPIListener.preDeleteAuthorizedAPI(CONSOLE_APP_ID, API_ID, TENANT_DOMAIN);
        } catch (IdentityApplicationManagementClientException e) {
            assertEquals(e.getMessage(), "Deleting authorized APIs from the console application is not allowed");
        }
    }

    @Test
    public void testPreDeleteAuthorizedAPIForNonConsoleApp() throws Exception {

        setupMockDependencies(TENANT_DOMAIN, false);
        consoleAuthorizedAPIListener.preDeleteAuthorizedAPI(OTHER_APP_ID, API_ID, TENANT_DOMAIN);
    }

    @Test
    public void testPrePatchAuthorizedAPIForConsoleApp() throws Exception {

        setupMockDependencies(TENANT_DOMAIN, false);
        
        List<String> addedScopes = Arrays.asList("scope1", "scope2");
        List<String> removedScopes = new ArrayList<>();
        removedScopes.add("scope3");
        
        try {
            consoleAuthorizedAPIListener.prePatchAuthorizedAPI(CONSOLE_APP_ID, API_ID, 
                    addedScopes, removedScopes, TENANT_DOMAIN);
        } catch (IdentityApplicationManagementClientException e) {
            assertEquals(e.getMessage(), "Patching authorized APIs of the console application is not allowed");
        }
    }

    @Test
    public void testPrePatchAuthorizedAPIForNonConsoleApp() throws Exception {

        setupMockDependencies(TENANT_DOMAIN, false);
        
        List<String> addedScopes = Arrays.asList("scope1", "scope2");
        List<String> removedScopes = new ArrayList<>();
        removedScopes.add("scope3");

        consoleAuthorizedAPIListener.prePatchAuthorizedAPI(OTHER_APP_ID, API_ID, 
                addedScopes, removedScopes, TENANT_DOMAIN);
    }

    private void setupMockDependencies(String tenantDomain, boolean tenantQualifiedUrlsEnabled) 
            throws IdentityApplicationManagementException {
        
        identityTenantUtil.when(IdentityTenantUtil::isTenantQualifiedUrlsEnabled)
                .thenReturn(tenantQualifiedUrlsEnabled);
        
        applicationManagementServiceMock.when(ApplicationManagementService::getInstance)
                .thenReturn(applicationManagementService);
        
        when(applicationManagementService.getApplicationResourceIDByInboundKey(
                anyString(), eq("oauth2"), eq(tenantDomain)))
                .thenReturn(CONSOLE_APP_ID);
    }

    private List<APIResource> createTestSystemAPIResources() {
        List<APIResource> resources = new ArrayList<>();
        
        APIResource resource1 = createTestAPIResource("api-1", "/api/users/v2/me/approval-tasks");
        APIResource resource2 = createTestAPIResource("api-2", "/other/api");
        
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

    private List<Scope> createTestSystemAPIScopes() {

        List<Scope> scopes = new ArrayList<>();
        // Add approval task scopes that will be filtered to NO_POLICY.
        scopes.add(new Scope("scope1-id", "internal_approval_task_view", 
                "Approval Task View", "View approval tasks"));
        scopes.add(new Scope("scope2-id", "internal_approval_task_update", 
                "Approval Task Update", "Update approval tasks"));
        
        // Add other scopes that will remain in RBAC_AUTHORIZATION.
        scopes.add(new Scope("scope3-id", "internal_org_config_mgt_view", 
                "Org Config View", "View org config"));
        
        return scopes;
    }

    private String mockAppId() {
        return UUID.randomUUID().toString();
    }
}
