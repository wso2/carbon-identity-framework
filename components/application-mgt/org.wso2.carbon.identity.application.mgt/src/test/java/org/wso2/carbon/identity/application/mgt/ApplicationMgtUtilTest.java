/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.mgt;

import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.provider.RegistryBasedApplicationPermissionProvider;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.config.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.nio.file.Paths;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;
import static org.wso2.carbon.base.MultitenantConstants.TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.DEFAULT_RESULTS_PER_PAGE;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.ENABLE_APPLICATION_ROLE_VALIDATION_PROPERTY;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.PATH_CONSTANT;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/*
  Unit tests for ApplicationMgtUtil.
 */
public class ApplicationMgtUtilTest {

    ApplicationManagementServiceComponentHolder mockApplicationManagementServiceComponentHolder;

    private CarbonContext mockCarbonContext;
    private UserStoreManager mockUserStoreManager;
    private AbstractUserStoreManager mockAbstractUserStoreManager;
    private UserRealm mockUserRealm;
    private UserRealm mockUserRealmFromRealmService;
    private RealmService mockRealmService;
    private RealmConfiguration mockRealmConfiguration;
    private Registry mockTenantRegistry;
    private Collection mockAppRootNode;
    private ApplicationPermission applicationPermission;
    private ApplicationPermission[] applicationPermissions;
    private Collection mockAppCollection;
    private Collection childCollection;

    private RegistryBasedApplicationPermissionProvider registryBasedApplicationPermissionProvider;

    private static final String USERNAME = "user";
    private static final String APPLICATION_NAME = "applicationName";
    private static final String NEW_APPLICATION_NAME = "newApplicationName";
    private static final String PERMISSION_PATH = "permission/applications";
    private static final String PERMISSION = "user";
    private static final String TRUE_VALUE = "true";
    private static final String FALSE_VALUE = "false";
    private static final String ROLE_NAME = "Application/applicationName";
    private static final int CODE = 30012;

    private final String applicationNode = PERMISSION_PATH + PATH_CONSTANT + APPLICATION_NAME;
    private final String applicationPermissionPath = PERMISSION_PATH  + PATH_CONSTANT + APPLICATION_NAME +
            PATH_CONSTANT + PERMISSION;

    @BeforeTest
    public void setup() {

        mockAppRootNode = mock(Collection.class);
        mockRealmConfiguration = mock(RealmConfiguration.class);
        mockAppCollection = mock(Collection.class);
        childCollection = mock(Collection.class);
        mockApplicationManagementServiceComponentHolder = mock(ApplicationManagementServiceComponentHolder.class);

        applicationPermission = new ApplicationPermission();
        applicationPermission.setValue(USERNAME);
        applicationPermissions = new ApplicationPermission[]{applicationPermission};
        registryBasedApplicationPermissionProvider = new RegistryBasedApplicationPermissionProvider();

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
    }

    @DataProvider(name = "getAppNamesForDefaultRegex")
    public Object[][] getAppNamesForDefaultRegex() {

        return new Object[][]{
                {"MyAppName99", true},
                {"My App Name1", true},
                {"My-App-Name2", true},
                {"My.App.Name3", true},
                {"My_App_Name4", true},
                {"My_App_Name5", true},
                {"My_App.Name-1234567890", true},

                {" My_App_Name", false},
                {"My_App_Name ", false},
                {" My_App_Name ", false},
                {"My_App.Name@carbon.super", false},
        };
    }

    @Test(dataProvider = "getAppNamesForDefaultRegex")
    public void testIsRegexValidated(String appName, boolean isValidName) {

        // Default app validation regex should allow names with alphanumeric, dot, space, underscore and hyphens.
        // Should not allow leading or trailing spaces.
        assertEquals(ApplicationMgtUtil.isRegexValidated(appName), isValidName);
    }

    @DataProvider(name = "getAppNamesForCustomRegex")
    public Object[][] getAppNamesForCustomRegex() {

        return new Object[][]{
                {"MyAppName99", true},
                // These two names are valid according to our default regex. Our custom regex will make these invalid.
                {"My AppName 99", false},
                {"MyAppName99-", false},
        };
    }

    @Test(dataProvider = "getAppNamesForCustomRegex")
    public void testSpNameValidationWithCustomRegex(String appName, boolean isValidName) {

        final String customRegEx = "^[a-zA-Z0-9]+";

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty("ServiceProviders.SPNameRegex")).thenReturn(customRegEx);

            assertEquals(ApplicationMgtUtil.isRegexValidated(appName), isValidName);
        }
    }

    @Test
    public void testBuildPermissions() {

        String[] permissions = new String[]{"permission"};
        org.wso2.carbon.user.api.Permission[] permissionSet = ApplicationMgtUtil.buildPermissions(APPLICATION_NAME,
                permissions);

        assertEquals(permissionSet[0].getResourceId(), APPLICATION_NAME + "\\permission");
    }

    @DataProvider(name = "validateRolesDataProvider")
    public Object[][] validateRolesDataProvider() {

        return new Object[][]{
                {TRUE_VALUE, TRUE},
                {null, TRUE},
                {FALSE_VALUE, FALSE}
        };
    }

    @Test(dataProvider = "validateRolesDataProvider")
    public void testValidateRoles(String allowRoleValidationProperty, Boolean expected) {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(ENABLE_APPLICATION_ROLE_VALIDATION_PROPERTY)).
                    thenReturn(allowRoleValidationProperty);
            assertEquals(ApplicationMgtUtil.validateRoles(), expected);
        }
    }

    @DataProvider(name = "userAuthorizeDataProvider")
    public Object[][] userAuthorizeDataProvider() {

        String[] userRoles1 = {ROLE_NAME, ""};
        String[] userRoles2 = {APPLICATION_NAME, ""};
        String[] userRoles3 = {};

        return new Object[][]{
                {APPLICATION_NAME, USERNAME, TRUE_VALUE, userRoles1, 1, TRUE},
                {APPLICATION_NAME, USERNAME, "", userRoles2, 2, FALSE},
                {APPLICATION_NAME, USERNAME, FALSE_VALUE, userRoles1, 1, TRUE},
                {APPLICATION_NAME, USERNAME, TRUE_VALUE, userRoles3, 1, FALSE},
                {APPLICATION_NAME, USERNAME, "FALSE_VALUE", userRoles3, 1, TRUE}
        };
    }

    @Test(dataProvider = "userAuthorizeDataProvider")
    public void testIsUserAuthorized(String applicationName, String userName, String allowRoleValidationProperty,
                                     String[] userRoles, int applicationId, Boolean expected) throws
            UserStoreException, IdentityApplicationManagementException {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<ApplicationMgtSystemConfig> applicationMgtSystemConfig = mockStatic(
                     ApplicationMgtSystemConfig.class);
             MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(ENABLE_APPLICATION_ROLE_VALIDATION_PROPERTY)).
                    thenReturn(allowRoleValidationProperty);

            ApplicationDAO mockApplicationDAO = mock(ApplicationDAO.class);
            ApplicationMgtSystemConfig mockAppMgtSystemConfig = mock(ApplicationMgtSystemConfig.class);
            applicationMgtSystemConfig.when(ApplicationMgtSystemConfig::getInstance).thenReturn(mockAppMgtSystemConfig);
            when(mockAppMgtSystemConfig.getApplicationDAO()).thenReturn(mockApplicationDAO);
            when(mockApplicationDAO.getApplicationName(anyInt())).thenReturn(APPLICATION_NAME);

            mockUserStoreManager(privilegedCarbonContext, carbonContext);
            when(mockUserStoreManager.getRoleListOfUser(userName)).thenReturn(userRoles);

            assertEquals(ApplicationMgtUtil.isUserAuthorized(applicationName, userName), expected);
            assertEquals(ApplicationMgtUtil.isUserAuthorized(applicationName, userName, applicationId), expected);
        }
    }

    @Test
    public void testIsUserAuthorizedUserStoreException() throws UserStoreException {

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {
            mockUserStoreManager(privilegedCarbonContext, carbonContext);
            doThrow(new UserStoreException("")).when(mockUserStoreManager).getRoleListOfUser(anyString());

            assertThrows(IdentityApplicationManagementException.class, () -> ApplicationMgtUtil.isUserAuthorized
                    (APPLICATION_NAME, USERNAME));
        }
    }

    @Test
    public void testCreateAppRole() throws UserStoreException, IdentityApplicationManagementException {

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {
            mockUserStoreManager(privilegedCarbonContext, carbonContext);
            ApplicationMgtUtil.createAppRole(APPLICATION_NAME, USERNAME);
            verify(mockUserStoreManager).addRole(ROLE_NAME, new String[]{USERNAME}, null);
        }
    }

    @Test
    public void testCreateAppRoleUserStoreException() throws UserStoreException {

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {
            mockUserStoreManager(privilegedCarbonContext, carbonContext);
            UserStoreException mockUserStoreException = mock(UserStoreException.class);
            doThrow(mockUserStoreException).when(mockUserStoreManager).addRole(ROLE_NAME, new String[]{USERNAME},
                    null);
            when(mockUserStoreException.getMessage()).thenReturn(String.format(ERROR_CODE_ROLE_ALREADY_EXISTS.
                    getMessage() + CODE, ROLE_NAME));

            doThrow(new UserStoreException("")).when(mockUserStoreManager).updateRoleListOfUser(USERNAME, null,
                    new String[]{ROLE_NAME});
            when(mockUserStoreManager.getRoleListOfUser(USERNAME)).thenReturn(new String[]{ROLE_NAME});

            try {
                ApplicationMgtUtil.createAppRole(APPLICATION_NAME, USERNAME);
            } catch (IdentityApplicationManagementException e) {
                assertEquals(e.getMessage(), "Error while updating application role: " + ROLE_NAME + " with user "
                        + USERNAME);
            }
        }
    }

    @Test
    public void testDeleteAppRole() throws UserStoreException, IdentityApplicationManagementException {

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {
            mockUserStoreManager(privilegedCarbonContext, carbonContext);
            ApplicationMgtUtil.deleteAppRole(APPLICATION_NAME);
            verify(mockUserStoreManager).deleteRole(ROLE_NAME);
        }
    }

    @Test
    public void testDeleteAppRoleUserStoreException() throws UserStoreException {

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {
            mockUserStoreManager(privilegedCarbonContext, carbonContext);
            doThrow(new UserStoreException("")).when(mockUserStoreManager).deleteRole(ROLE_NAME);

            assertThrows(IdentityApplicationManagementException.class, () -> ApplicationMgtUtil.deleteAppRole
                    (APPLICATION_NAME));
        }
    }

    @Test
    public void testRenameRole() throws UserStoreException {

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class)) {
            mockUserStoreManager(privilegedCarbonContext, carbonContext);
            ApplicationMgtUtil.renameRole(APPLICATION_NAME, NEW_APPLICATION_NAME);
            verify(mockUserStoreManager).updateRoleName("Internal/" + APPLICATION_NAME, "Internal/" +
                    NEW_APPLICATION_NAME);
        }
    }

    @Test
    public void testRenameAppPermissionPathNode() throws IdentityApplicationManagementException, UserStoreException,
            RegistryException {

        try (MockedStatic<ApplicationManagementServiceComponentHolder> applicationManagementServiceComponentHolder =
                     mockStatic(ApplicationManagementServiceComponentHolder.class);
             MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);) {
            applicationManagementServiceComponentHolder.when(
                    ApplicationManagementServiceComponentHolder::getInstance).thenReturn(
                    mockApplicationManagementServiceComponentHolder);
            when(mockApplicationManagementServiceComponentHolder.getApplicationPermissionProvider()).thenReturn(
                    registryBasedApplicationPermissionProvider);

            loadPermissions(privilegedCarbonContext, carbonContext);
            Collection permissionNode = mock(Collection.class);
            when(mockTenantRegistry.newCollection()).thenReturn(permissionNode);

            String newApplicationNode = PERMISSION_PATH + PATH_CONSTANT + NEW_APPLICATION_NAME;
            String newApplicationPermissionPath =
                    PERMISSION_PATH + PATH_CONSTANT + NEW_APPLICATION_NAME + PATH_CONSTANT + PERMISSION + PATH_CONSTANT;

            ApplicationMgtUtil.renameAppPermissionPathNode(APPLICATION_NAME, NEW_APPLICATION_NAME);
            verify(mockTenantRegistry, times(1)).delete(applicationPermissionPath);
            verify(mockTenantRegistry, times(1)).delete(applicationNode);
            verify(mockTenantRegistry, times(1)).put(newApplicationNode, permissionNode);
            verify(mockTenantRegistry, times(1)).put(newApplicationPermissionPath, permissionNode);
        }
    }

    @Test
    public void testStorePermissions() throws  Exception {

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<ApplicationManagementServiceComponentHolder> applicationManagementServiceComponentHolder =
                     mockStatic(ApplicationManagementServiceComponentHolder.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class)) {

            applicationManagementServiceComponentHolder.when(
                    ApplicationManagementServiceComponentHolder::getInstance).thenReturn(
                    mockApplicationManagementServiceComponentHolder);
            when(mockApplicationManagementServiceComponentHolder.getApplicationPermissionProvider()).thenReturn(
                    registryBasedApplicationPermissionProvider);

            mockTenantRegistry(privilegedCarbonContext, carbonContext);

            identityTenantUtil.when(() -> IdentityTenantUtil.initializeRegistry(anyInt()))
                    .thenAnswer((Answer<Void>) invocation -> null);

            when(mockCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
            when(mockTenantRegistry.resourceExists(anyString())).thenReturn(FALSE);

            changeUserToAdmin();
            when(mockTenantRegistry.newCollection()).thenReturn(mockAppRootNode);

            PermissionsAndRoleConfig permissionsAndRoleConfig = new PermissionsAndRoleConfig();
            permissionsAndRoleConfig.setPermissions(applicationPermissions);

            Resource mockResource = mock(Resource.class);
            when(mockTenantRegistry.newResource()).thenReturn(mockResource);

            ApplicationMgtUtil.storePermissions(APPLICATION_NAME, USERNAME, permissionsAndRoleConfig);
            verify(mockTenantRegistry, times(1)).put(PERMISSION_PATH, mockAppRootNode);
            verify(mockTenantRegistry, times(1)).put(applicationNode, mockAppRootNode);
            verify(mockTenantRegistry, times(1)).put(applicationNode + PATH_CONSTANT +
                    applicationPermission, mockResource);
        }
    }

    @DataProvider(name = "updatePermissionDataProvider")
    public Object[][] updatePermissionDataProvider() {

       return new Object[][]{
               {new String[]{PERMISSION, ""}, 1},
               {new String[]{}, 0}
       };
    }

    @Test(dataProvider = "updatePermissionDataProvider")
    public void testUpdatePermission(String[] childPermissions, int childCount) throws
            IdentityApplicationManagementException, UserStoreException, RegistryException {

        try (MockedStatic<ApplicationManagementServiceComponentHolder> applicationManagementServiceComponentHolder =
                     mockStatic(ApplicationManagementServiceComponentHolder.class);
             MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);) {
            applicationManagementServiceComponentHolder.when(
                    ApplicationManagementServiceComponentHolder::getInstance).thenReturn(
                    mockApplicationManagementServiceComponentHolder);
            when(mockApplicationManagementServiceComponentHolder.getApplicationPermissionProvider()).thenReturn(
                    registryBasedApplicationPermissionProvider);

            loadPermissions(privilegedCarbonContext, carbonContext);
            when(mockTenantRegistry.resourceExists(anyString())).thenReturn(FALSE);
            when(mockTenantRegistry.newCollection()).thenReturn(mockAppRootNode);

            when(mockTenantRegistry.get(applicationNode)).thenReturn(mockAppCollection);
            when(mockAppCollection.getChildren()).thenReturn(childPermissions);
            when(mockAppCollection.getChildCount()).thenReturn(childCount);

            ApplicationMgtUtil.updatePermissions(APPLICATION_NAME, applicationPermissions);
            verify(mockTenantRegistry, times(1)).put(applicationNode, mockAppRootNode);
            verify(mockTenantRegistry, times(1)).put(applicationPermissionPath + PATH_CONSTANT,
                    mockAppRootNode);
        }
    }

    private void changeUserToAdmin() throws org.wso2.carbon.user.core.UserStoreException {

        org.wso2.carbon.user.core.UserRealm userRealm = mock(org.wso2.carbon.user.core.UserRealm.class);
        AuthorizationManager mockAuthorizationManager = mock(AuthorizationManager.class);

        when(mockCarbonContext.getUserRealm()).thenReturn(userRealm);
        when(userRealm.getAuthorizationManager()).thenReturn(mockAuthorizationManager);
        when(mockAuthorizationManager.isUserAuthorized(anyString(), anyString(), anyString())).thenReturn(FALSE);
        when(userRealm.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
        when(mockRealmConfiguration.getAdminUserName()).thenReturn("admin");
    }

    private void loadPermissions(MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext,
                                 MockedStatic<CarbonContext> carbonContext)
            throws RegistryException, UserStoreException {

        mockTenantRegistry(privilegedCarbonContext, carbonContext);
        when(mockTenantRegistry.resourceExists(anyString())).thenReturn(TRUE);
        changeUserToAdmin();

        when(mockTenantRegistry.newCollection()).thenReturn(mockAppRootNode);
        when(mockTenantRegistry.get(applicationNode)).thenReturn(mockAppCollection);
        when(mockAppCollection.getChildren()).thenReturn(new String[]{PATH_CONSTANT + applicationPermissionPath});
        when(mockTenantRegistry.get(PATH_CONSTANT + applicationPermissionPath)).thenReturn(childCollection);
        when(childCollection.getChildren()).thenReturn(new String[]{});
    }

    @Test
    public void testDeletePermissions() throws RegistryException, IdentityApplicationManagementException {

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<ApplicationManagementServiceComponentHolder> applicationManagementServiceComponentHolder =
                     mockStatic(ApplicationManagementServiceComponentHolder.class)) {
            mockTenantRegistry(privilegedCarbonContext, carbonContext);
            applicationManagementServiceComponentHolder.when(
                    ApplicationManagementServiceComponentHolder::getInstance).thenReturn(
                    mockApplicationManagementServiceComponentHolder);
            when(mockApplicationManagementServiceComponentHolder.getApplicationPermissionProvider()).thenReturn(
                    registryBasedApplicationPermissionProvider);
            when(mockTenantRegistry.resourceExists(anyString())).thenReturn(TRUE);
            ApplicationMgtUtil.deletePermissions(APPLICATION_NAME);
            verify(mockTenantRegistry).delete(anyString());
        }
    }

    @Test
    public void testDeletePermissionsRegistryException() throws RegistryException {

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<ApplicationManagementServiceComponentHolder>
                     applicationManagementServiceComponentHolder =
                     mockStatic(ApplicationManagementServiceComponentHolder.class);) {
            applicationManagementServiceComponentHolder.when(
                    ApplicationManagementServiceComponentHolder::getInstance).thenReturn(
                    mockApplicationManagementServiceComponentHolder);
            when(mockApplicationManagementServiceComponentHolder.getApplicationPermissionProvider()).thenReturn(
                    registryBasedApplicationPermissionProvider);

            mockTenantRegistry(privilegedCarbonContext, carbonContext);
            doThrow(new RegistryException("")).when(mockTenantRegistry).resourceExists(anyString());

            assertThrows(IdentityApplicationManagementException.class, () -> ApplicationMgtUtil.deletePermissions
                    (APPLICATION_NAME));
        }
    }

    @Test
    public void testConcatArrays() {

        Property[] properties1 = new Property[]{new Property()};
        Property[] properties2 = new Property[]{new Property()};
        assertEquals(ApplicationMgtUtil.concatArrays(properties1, properties2).length, 2);
    }

    @DataProvider(name = "validApplicationOwnerDataProvider")
    public Object[][] validApplicationOwnerDataProvider() {


        return new Object[][]{
                {USERNAME, SUPER_TENANT_DOMAIN_NAME, TRUE, TRUE},
                {null, null, TRUE, FALSE},
                {"", null, TRUE,  FALSE},
                {"", "", FALSE, FALSE},
        };
    }

    @Test(dataProvider = "validApplicationOwnerDataProvider")
    public void testIsValidApplicationOwner(String username, String tenantDomain, Boolean hasOwner,  Boolean expected)
            throws IdentityApplicationManagementException, UserStoreException {

        ServiceProvider serviceProvider = new ServiceProvider();
        if (hasOwner) {
            User user = new User();
            user.setUserName(username);
            user.setTenantDomain(tenantDomain);
            serviceProvider.setOwner(user);
        }

        try (MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
             MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdentityTenantUtil> identityTenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<ApplicationManagementServiceComponentHolder>
                     applicationManagementServiceComponentHolder =
                     mockStatic(ApplicationManagementServiceComponentHolder.class)) {

            mockUserStoreManager(privilegedCarbonContext, carbonContext);
            when(mockUserStoreManager.isExistingUser(anyString())).thenReturn(FALSE);
            when(mockUserRealm.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
            when(mockRealmConfiguration.getAdminUserName()).thenReturn("admin");
            when(mockRealmConfiguration.getUserStoreProperty(anyString())).thenReturn("property");
            when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(-1234);

            mockRealmService = mock(RealmService.class);
            mockUserRealmFromRealmService = mock(UserRealm.class);
            mockAbstractUserStoreManager = mock(AbstractUserStoreManager.class);

            applicationManagementServiceComponentHolder.when(ApplicationManagementServiceComponentHolder::getInstance)
                    .thenReturn(mockApplicationManagementServiceComponentHolder);
            when(mockApplicationManagementServiceComponentHolder.getRealmService()).thenReturn(mockRealmService);
            when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealmFromRealmService);
            when(mockUserRealmFromRealmService.getUserStoreManager()).thenReturn(mockAbstractUserStoreManager);
            when(mockAbstractUserStoreManager.isExistingUser(anyString())).thenReturn(TRUE);

            org.wso2.carbon.user.core.common.User user = new org.wso2.carbon.user.core.common.User();
            user.setUsername(UserCoreUtil.removeDomainFromName(USERNAME));
            user.setTenantDomain(tenantDomain);
            user.setUserStoreDomain(UserCoreUtil.extractDomainFromName(USERNAME));
            when(mockAbstractUserStoreManager.getUser(any(), anyString())).thenReturn(user);

            assertEquals(ApplicationMgtUtil.isValidApplicationOwner(serviceProvider), expected);
        }
    }

    @DataProvider(name = "getItemsPerPageDataProvider")
    public Object[][] getItemsPerPageDataProvider() {

        return new Object[][]{
                {"1", 1},
                {"", DEFAULT_RESULTS_PER_PAGE},
                {null, DEFAULT_RESULTS_PER_PAGE}
        };
    }

    @Test(dataProvider = "getItemsPerPageDataProvider")
    public void testGetItemsPerPage(String itemsPerPagePropertyValue, int itemsPerPage) {

        try (MockedStatic<ServerConfiguration> serverConfiguration = mockStatic(ServerConfiguration.class)) {
            ServerConfiguration mockServerConfiguration = mock(ServerConfiguration.class);
            serverConfiguration.when(ServerConfiguration::getInstance).thenReturn(mockServerConfiguration);
            when(mockServerConfiguration.getFirstProperty(anyString())).thenReturn(itemsPerPagePropertyValue);

            assertEquals(ApplicationMgtUtil.getItemsPerPage(), itemsPerPage);
        }
    }

    @DataProvider(name = "getApplicationUpdatedVersionDataProvider")
    public Object[][] getApplicationUpdatedVersionDataProvider() {

        return new Object[][]{
                { "v0.0.0", "oauth2", "v0.0.0" },
                { "v1.0.0", "oauth2", "v1.0.0" },
                { "v2.0.0", "oauth2", "v2.0.0" },
                { "v0.0.0", "samlsso", "v2.0.0" },
                { "v1.0.0", "samlsso", "v2.0.0" },
                { "v2.0.0", "samlsso", "v2.0.0" },
                { "v0.0.0", null, "v2.0.0" },
                { "v1.0.0", null, "v2.0.0" },
                { "v2.0.0", null, "v2.0.0" },
        };
    }

    @Test(dataProvider = "getApplicationUpdatedVersionDataProvider")
    public void testGetApplicationUpdatedVersion(String currentVersion, String authType,
                                                 String expectedUpdatedVersion) {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationVersion(currentVersion);
        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        InboundAuthenticationRequestConfig inboundAuthenticationRequestConfigs =
                new InboundAuthenticationRequestConfig();
        inboundAuthenticationRequestConfigs.setInboundAuthType(authType);
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                new InboundAuthenticationRequestConfig[]{inboundAuthenticationRequestConfigs});
        serviceProvider.setInboundAuthenticationConfig(inboundAuthenticationConfig);

        String updatedVersion = ApplicationMgtUtil.getApplicationUpdatedVersion(serviceProvider);

        assertEquals(updatedVersion, expectedUpdatedVersion);

    }

    private void mockTenantRegistry(MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext,
                                    MockedStatic<CarbonContext> carbonContext) {

        mockCarbonContext(privilegedCarbonContext, carbonContext);
        mockTenantRegistry = mock(Registry.class);
        when(mockCarbonContext.getRegistry(RegistryType.USER_GOVERNANCE)).thenReturn(mockTenantRegistry);
    }

    private void mockUserStoreManager(MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext,
                                      MockedStatic<CarbonContext> carbonContext) throws UserStoreException {

        mockCarbonContext(privilegedCarbonContext, carbonContext);
        mockUserRealm = mock(UserRealm.class);
        mockUserStoreManager = mock(UserStoreManager.class);
        when(mockCarbonContext.getUserRealm()).thenReturn(mockUserRealm);
        when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);
    }

    private void mockCarbonContext(MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext,
                                   MockedStatic<CarbonContext> carbonContext) {

        initPrivilegedCarbonContext(privilegedCarbonContext);
        mockCarbonContext = mock(CarbonContext.class);
        carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
    }

    private void initPrivilegedCarbonContext(MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext) {

        PrivilegedCarbonContext mockPrivilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContext.when(
                PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(mockPrivilegedCarbonContext);
        when(mockPrivilegedCarbonContext.getTenantDomain()).thenReturn(SUPER_TENANT_DOMAIN_NAME);
        when(mockPrivilegedCarbonContext.getTenantId()).thenReturn(SUPER_TENANT_ID);
        when(mockPrivilegedCarbonContext.getUsername()).thenReturn("admin");
    }
}
