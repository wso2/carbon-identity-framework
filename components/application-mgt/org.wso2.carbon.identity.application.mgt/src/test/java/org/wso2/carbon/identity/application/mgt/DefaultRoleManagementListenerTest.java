/*
 * Copyright (c) 2024-2026, WSO2 LLC. (http://www.wso2.com).
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

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.AssociatedRolesConfig;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.listener.DefaultRoleManagementListener;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.util.RoleManagementUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

/**
 * Contains the unit tests for the default role management listener.
 */
public class DefaultRoleManagementListenerTest {

    private DefaultRoleManagementListener defaultRoleManagementListener;
    private static final String ROLE_NAME = "test_role";
    private static final String APPLICATION_NAME = "app_name";
    private static final String APPLICATION_RES_ID = "app_id";
    private static final String IS_FRAGMENT_APP = "isFragmentApp";
    private static final String MAIN_APPLICATION_RES_ID = "main_app_id";
    private static final String APPLICATION_AUD = "APPLICATION";
    private static final String ORGANIZATION_AUD = "ORGANIZATION";
    private static final String TENANT_DOMAIN = "wso2.com";
    private static final String MAIN_APP_TENANT_DOMAIN = "carbon.super";
    private static final int MAIN_APP_TENANT_ID = -1234;
    private static final String ORGANIZATION_SCOPE = "internal_org_user_mgt_view";
    private static final String BUSINESS_SCOPE = "orders:read";
    private static final String MANAGEMENT_SCOPE = "internal_user_mgt_view";

    @BeforeClass
    public void setUp() {

        defaultRoleManagementListener = spy(new DefaultRoleManagementListener());
    }

    @AfterMethod
    public void tearDown() {

        IdentityUtil.threadLocalProperties.set(new HashMap<>());
    }

    @Test
    public void testPreAddRoleForMainApp() throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class)) {

            ApplicationManagementService applicationManagementService =
                    mockApplicationManagementService(applicationManagementServiceMockedStatic, APPLICATION_AUD);
            when(applicationManagementService.getMainAppId(APPLICATION_RES_ID)).thenReturn(null);

            defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);

            // Shared application role creation is not validated for main applications.
            verify(applicationManagementService, never()).getApplicationByResourceId(anyString(), anyString());
        }
    }

    @DataProvider(name = "subOrgRoleAuthoringConfigProvider")
    public Object[][] subOrgRoleAuthoringConfigProvider() {

        return new Object[][]{
                // isEnhancedOrganizationAuthenticationEnabled, isSharedAppRoleManagementEnabled, main app audience,
                // isAllowed.
                {true, true, APPLICATION_AUD, true},
                {true, false, APPLICATION_AUD, false},
                {false, true, APPLICATION_AUD, false},
                {false, false, APPLICATION_AUD, false},
                // Application roles cannot be created for the shared applications of organization audience apps.
                {true, true, ORGANIZATION_AUD, false}
        };
    }

    @Test(dataProvider = "subOrgRoleAuthoringConfigProvider")
    public void testPreAddRoleForFragmentApp(boolean isEnhancedOrgAuthEnabled, boolean isSharedAppRoleManagementEnabled,
                                             String mainAppAudience, boolean isAllowed) throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class)) {

            ApplicationManagementService applicationManagementService =
                    mockApplicationManagementService(applicationManagementServiceMockedStatic, APPLICATION_AUD);
            mockMainApplication(applicationManagementService, identityTenantUtilMockedStatic,
                    isEnhancedOrgAuthEnabled, isSharedAppRoleManagementEnabled, mainAppAudience);

            try {
                defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                        new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);
                if (!isAllowed) {
                    fail("Role creation should be rejected for the shared application.");
                }
            } catch (IdentityRoleManagementClientException e) {
                if (isAllowed) {
                    fail("Role creation should be allowed for the shared application.", e);
                }
                assertEquals(e.getErrorCode(), RoleConstants.Error.OPERATION_FORBIDDEN.getCode());
            }
        }
    }

    @Test
    public void testPreAddRoleForFragmentAppInSharedRoleProvisioningFlow() throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class)) {

            ApplicationManagementService applicationManagementService =
                    mockApplicationManagementService(applicationManagementServiceMockedStatic, APPLICATION_AUD);
            when(applicationManagementService.getMainAppId(APPLICATION_RES_ID)).thenReturn(MAIN_APPLICATION_RES_ID);
            IdentityUtil.threadLocalProperties.get().put(RoleConstants.IS_SHARED_ROLE_PROVISIONING_FLOW, true);

            // Inherited roles are provisioned regardless of the main application configuration.
            defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);

            verify(applicationManagementService, never()).getApplicationByResourceId(anyString(), anyString());
        }
    }

    @Test(priority = 4)
    public void testPreAddRoleForOrgAudience() throws Exception {

        defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), ORGANIZATION_AUD, "org-id", TENANT_DOMAIN);
        // If the audience is not application, then in the preAddRole method, it will return without going to the
        // other methods. So the thread local properties will not be set.
        Map<String, Object> threadLocalProps = IdentityUtil.threadLocalProperties.get();
        assertFalse(threadLocalProps.containsKey(IS_FRAGMENT_APP));
    }

    @Test(priority = 5, expectedExceptions = {IdentityRoleManagementClientException.class},
            expectedExceptionsMessageRegExp = "Invalid audience. No application found with application id: " +
                    APPLICATION_RES_ID + " and tenant domain : " + TENANT_DOMAIN)
    public void testPreAddRoleWithNullApplication() throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class)) {

            ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
            applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance).
                    thenReturn(applicationManagementService);
            when(applicationManagementService.getApplicationBasicInfoByResourceId(anyString(), anyString())).
                    thenReturn(null);
            // Calling the preAddRole method.
            defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);
        }
    }

    @Test(priority = 6, expectedExceptions = {IdentityRoleManagementClientException.class},
            expectedExceptionsMessageRegExp = "Application: " + APPLICATION_RES_ID + " does not have Application " +
                    "role audience type")
    public void testPreAddRoleWithWrongAudience() throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class)) {

            // Creating service provider object.
            ServiceProvider serviceProvider = createServiceProvider();

            ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
            applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance).
                    thenReturn(applicationManagementService);
            when(applicationManagementService.getApplicationBasicInfoByResourceId(anyString(), anyString())).
                    thenReturn(new ApplicationBasicInfo());
            when(applicationManagementService.getAllowedAudienceForRoleAssociation(anyString(), anyString())).
                    thenReturn(ORGANIZATION_AUD);
            // Calling the preAddRole method.
            defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);
        }
    }

    @Test(priority = 7, expectedExceptions = {IdentityRoleManagementServerException.class},
            expectedExceptionsMessageRegExp = "Error while retrieving the application for the given id: " +
                    APPLICATION_RES_ID)
    public void testPreAddRoleWithApplicationRetrievingException() throws Exception {

        try (MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic =
                     mockStatic(ApplicationManagementService.class)) {

            ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
            applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance).
                    thenReturn(applicationManagementService);
            when(applicationManagementService.getApplicationBasicInfoByResourceId(anyString(), anyString())).
                    thenThrow(IdentityApplicationManagementException.class);

            // Calling the preAddRole method.
            defaultRoleManagementListener.preAddRole(ROLE_NAME, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);
        }
    }

    private static ServiceProvider createServiceProvider() {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName(APPLICATION_NAME);
        serviceProvider.setApplicationResourceId(APPLICATION_RES_ID);
        return serviceProvider;
    }

    @Test(priority = 8)
    public void testDoPostUpdateApplication() throws Exception {

        try (MockedStatic<RoleManagementUtils> roleManagementUtilsMockedStatic =
                     mockStatic(RoleManagementUtils.class)) {
            ServiceProvider serviceProvider = createServiceProvider();
            defaultRoleManagementListener.doPostUpdateApplication(serviceProvider, TENANT_DOMAIN, "admin");
            roleManagementUtilsMockedStatic.verify(() ->
                    RoleManagementUtils.clearRoleBasicInfoCacheByTenant(TENANT_DOMAIN),
                    times(1));
        }
    }

    @DataProvider(name = "organizationPermissionProvider")
    public Object[][] organizationPermissionProvider() {

        return new Object[][]{
                // isOrganization, permission, isAllowed.
                // Organization API resource scope in an organization.
                {true, ORGANIZATION_SCOPE, true},
                // Business API resource scope in an organization.
                {true, BUSINESS_SCOPE, true},
                // Management API resource scope authorized for the application, in an organization.
                {true, MANAGEMENT_SCOPE, false},
                // Management API resource scope authorized for the application, in the root organization.
                {false, MANAGEMENT_SCOPE, true}
        };
    }

    @Test(dataProvider = "organizationPermissionProvider")
    public void testPreUpdatePermissionsForApplicationRole(boolean isOrganization, String permission,
                                                           boolean isAllowed) throws Exception {

        List<Permission> permissions = Collections.singletonList(new Permission(permission));
        try (MockedConstruction<AuthorizedAPIManagementServiceImpl> authorizedAPIManagementService =
                     mockConstruction(AuthorizedAPIManagementServiceImpl.class, (mock, context) ->
                             when(mock.getAuthorizedScopes(APPLICATION_RES_ID, TENANT_DOMAIN)).thenReturn(
                                     Collections.singletonList(new AuthorizedScopes("RBAC", new ArrayList<>(
                                             Arrays.asList(ORGANIZATION_SCOPE, BUSINESS_SCOPE,
                                                     MANAGEMENT_SCOPE))))));
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class);
             MockedStatic<ApplicationManagementServiceComponentHolder> componentHolder =
                     mockStatic(ApplicationManagementServiceComponentHolder.class)) {

            organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(TENANT_DOMAIN))
                    .thenReturn(isOrganization);
            ApplicationManagementServiceComponentHolder holder =
                    mock(ApplicationManagementServiceComponentHolder.class);
            APIResourceManager apiResourceManager = mock(APIResourceManager.class);
            componentHolder.when(ApplicationManagementServiceComponentHolder::getInstance).thenReturn(holder);
            when(holder.getAPIResourceManager()).thenReturn(apiResourceManager);
            // Scopes available to the organization exclude the management API resource scopes.
            when(apiResourceManager.getScopesByTenantDomain(TENANT_DOMAIN, "")).thenReturn(Arrays.asList(
                    buildScope(ORGANIZATION_SCOPE), buildScope(BUSINESS_SCOPE)));

            try {
                defaultRoleManagementListener.preUpdatePermissionsForRole("role-id", permissions,
                        new ArrayList<>(), APPLICATION_AUD, APPLICATION_RES_ID, TENANT_DOMAIN);
                if (!isAllowed) {
                    fail("The permission should be rejected for roles in organizations.");
                }
            } catch (IdentityRoleManagementClientException e) {
                if (isAllowed) {
                    fail("The permission should be allowed.", e);
                }
                assertEquals(e.getErrorCode(), RoleConstants.Error.INVALID_PERMISSION.getCode());
            }
            if (!isOrganization) {
                // The organization scopes are not resolved outside organizations.
                verify(apiResourceManager, never()).getScopesByTenantDomain(anyString(), anyString());
            }
        }
    }

    private static Scope buildScope(String name) {

        return new Scope.ScopeBuilder().name(name).build();
    }

    private static ApplicationManagementService mockApplicationManagementService(
            MockedStatic<ApplicationManagementService> applicationManagementServiceMockedStatic,
            String allowedAudience) throws Exception {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        applicationManagementServiceMockedStatic.when(ApplicationManagementService::getInstance).
                thenReturn(applicationManagementService);
        when(applicationManagementService.getApplicationBasicInfoByResourceId(APPLICATION_RES_ID, TENANT_DOMAIN)).
                thenReturn(new ApplicationBasicInfo());
        when(applicationManagementService.getAllowedAudienceForRoleAssociation(APPLICATION_RES_ID, TENANT_DOMAIN)).
                thenReturn(allowedAudience);
        return applicationManagementService;
    }

    private static void mockMainApplication(ApplicationManagementService applicationManagementService,
                                            MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic,
                                            boolean isEnhancedOrgAuthEnabled, boolean isSharedAppRoleManagementEnabled,
                                            String mainAppAudience) throws Exception {

        ServiceProvider mainApplication = createServiceProvider();
        mainApplication.setApplicationResourceId(MAIN_APPLICATION_RES_ID);
        mainApplication.setEnhancedOrganizationAuthenticationEnabled(isEnhancedOrgAuthEnabled);
        mainApplication.setSharedAppRoleManagementEnabled(isSharedAppRoleManagementEnabled);
        AssociatedRolesConfig associatedRolesConfig = new AssociatedRolesConfig();
        associatedRolesConfig.setAllowedAudience(mainAppAudience);
        mainApplication.setAssociatedRolesConfig(associatedRolesConfig);
        when(applicationManagementService.getMainAppId(APPLICATION_RES_ID)).thenReturn(MAIN_APPLICATION_RES_ID);
        when(applicationManagementService.getTenantIdByApp(MAIN_APPLICATION_RES_ID)).thenReturn(MAIN_APP_TENANT_ID);
        identityTenantUtilMockedStatic.when(() -> IdentityTenantUtil.getTenantDomain(MAIN_APP_TENANT_ID))
                .thenReturn(MAIN_APP_TENANT_DOMAIN);
        when(applicationManagementService.getApplicationByResourceId(MAIN_APPLICATION_RES_ID,
                MAIN_APP_TENANT_DOMAIN)).thenReturn(mainApplication);
    }
}
