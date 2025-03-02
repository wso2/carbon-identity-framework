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

package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.DiscoverableGroup;
import org.wso2.carbon.identity.application.common.model.GroupBasicInfo;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.provider.ApplicationPermissionProvider;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.Group;
import org.wso2.carbon.user.core.service.RealmService;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

/**
 * Test class for ApplicationDAOImpl.
 */
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbscripts/identity.sql"})
public class ApplicationDAOImplTest {

    private static final String DEFAULT_USER_STORE_DOMAIN = "PRIMARY";
    private static final String USERNAME = "test-user";
    private static final String USER_ID = "test-user-id";

    MockedStatic<IdentityTenantUtil> mockIdentityTenantUtil;
    MockedStatic<IdentityUtil> mockIdentityUtil;
    MockedStatic<ApplicationManagementServiceComponentHolder> mockedApplicationManagementServiceComponentHolder;
    ApplicationManagementServiceComponentHolder mockComponentHolder;
    UserRealm mockUserRealm;
    RealmService mockRealmService;
    AbstractUserStoreManager mockAbstractUserStoreManager;
    ApplicationPermissionProvider mockApplicationPermissionProvider;

    /**
     * Setup the test environment for ApplicationDAOImpl.
     */
    @BeforeClass
    public void setup() throws org.wso2.carbon.user.api.UserStoreException, IdentityApplicationManagementException {

        mockIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
        mockIdentityUtil = mockStatic(IdentityUtil.class);
        mockedApplicationManagementServiceComponentHolder =
                mockStatic(ApplicationManagementServiceComponentHolder.class);
        mockComponentHolder = mock(ApplicationManagementServiceComponentHolder.class);
        mockUserRealm = mock(UserRealm.class);
        mockRealmService = mock(RealmService.class);
        mockAbstractUserStoreManager = mock(AbstractUserStoreManager.class);
        mockApplicationPermissionProvider = mock(ApplicationPermissionProvider.class);
        setupInitConfigurations();
    }

    /**
     * Clean up the test environment of ApplicationDAOImpl.
     */
    @AfterClass
    public void end() {

        mockIdentityTenantUtil.close();
        mockIdentityUtil.close();
        mockedApplicationManagementServiceComponentHolder.close();
    }

    @Test(description = "Update application without discoverable groups")
    public void updateApplicationWithoutDiscoverableGroups() throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName("test-app");
        serviceProvider.setApplicationVersion("v1.0.0");
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        int applicationId = applicationDAO.createApplication(serviceProvider, SUPER_TENANT_DOMAIN_NAME);
        serviceProvider.setApplicationID(applicationId);
        serviceProvider.setDiscoverable(true);
        serviceProvider.setAccessUrl("https://localhost:5000/test-app");
        applicationDAO.updateApplication(serviceProvider, SUPER_TENANT_DOMAIN_NAME);
    }

    @Test(description = "Update application with valid discoverable groups", dependsOnMethods = {
            "updateApplicationWithoutDiscoverableGroups"})
    public void updateApplicationWithValidDiscoverableGroups() throws IdentityApplicationManagementException {

        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        ServiceProvider serviceProvider = applicationDAO.getApplication("test-app", SUPER_TENANT_DOMAIN_NAME);
        serviceProvider.setDiscoverableGroups(
                new DiscoverableGroup[] {getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 2, 0)});
        applicationDAO.updateApplication(serviceProvider, SUPER_TENANT_DOMAIN_NAME);
    }

    @Test(description = "Update application by deleting existing discoverable groups", dependsOnMethods = {
            "updateApplicationWithValidDiscoverableGroups"})
    public void updateApplicationByDeletingExistingDiscoverableGroups()
            throws IdentityApplicationManagementException, UserStoreException {

        when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-0")))
                .thenReturn("test-group-name-0");
        when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-1")))
                .thenReturn("test-group-name-1");
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        ServiceProvider serviceProvider = applicationDAO.getApplication("test-app", SUPER_TENANT_DOMAIN_NAME);
        serviceProvider.setDiscoverableGroups(
                new DiscoverableGroup[] {getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 2, 0),
                        getNewDiscoverableGroup("SECONDARY", 2, 0)});
        applicationDAO.updateApplication(serviceProvider, SUPER_TENANT_DOMAIN_NAME);
    }

    @Test(description = "Update application with invalid discoverable groups", dependsOnMethods = {
            "updateApplicationByDeletingExistingDiscoverableGroups"})
    public void updateApplicationWithInvalidDiscoverableGroups()
            throws UserStoreException, IdentityApplicationManagementException {

        when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-0")))
                .thenReturn("test-group-name-0");
        when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-1")))
                .thenReturn("test-group-name-1");
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        ServiceProvider serviceProvider = applicationDAO.getApplication("test-app", SUPER_TENANT_DOMAIN_NAME);
        serviceProvider.setDiscoverableGroups(
                new DiscoverableGroup[] {getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 2, 0),
                        getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 2, 0)});
        assertThrows(IdentityApplicationManagementException.class,
                () -> applicationDAO.updateApplication(serviceProvider, SUPER_TENANT_DOMAIN_NAME));
    }

    @Test(description = "Test discoverable groups list",
            dependsOnMethods = {"updateApplicationWithInvalidDiscoverableGroups"})
    public void testDiscoverableGroupsList()
            throws IdentityApplicationManagementException, UserStoreException {

        when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-0")))
                .thenReturn("test-group-name-0");
        when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-1")))
                .thenReturn("test-group-name-1");
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        ServiceProvider serviceProvider = applicationDAO.getApplication("test-app", SUPER_TENANT_DOMAIN_NAME);
        DiscoverableGroup[] discoverableGroups = serviceProvider.getDiscoverableGroups();
        String[] domainNames = new String[] {DEFAULT_USER_STORE_DOMAIN, "SECONDARY"};
        for (int i = 0; i < discoverableGroups.length; i++) {
            DiscoverableGroup discoverableGroup = discoverableGroups[i];
            assertEquals(discoverableGroup.getGroups().length, 2);
            for (int j = 0; j < discoverableGroup.getGroups().length; j++) {
                assertEquals(discoverableGroup.getGroups()[j].getName(), "test-group-name-" + j);
                assertEquals(discoverableGroup.getGroups()[j].getId(), "test-group-id-" + j);
            }
            assertEquals(discoverableGroup.getUserStore(), domainNames[i]);
        }
    }

    @Test(description = "Test retrieving discoverable groups list when the mapped group ID does not exist",
            dependsOnMethods = {"testDiscoverableGroupsList"})
    public void testDiscoverableGroupsListWhenMappedGroupIdNotExist()
            throws IdentityApplicationManagementException, UserStoreException {

        when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-0")))
                .thenReturn("test-group-name-0");
        when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-1")))
                .thenThrow(new UserStoreException());
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        ServiceProvider serviceProvider = applicationDAO.getApplication("test-app", SUPER_TENANT_DOMAIN_NAME);
        DiscoverableGroup[] discoverableGroups = serviceProvider.getDiscoverableGroups();
        String[] domainNames = new String[] {DEFAULT_USER_STORE_DOMAIN, "SECONDARY"};
        for (int i = 0; i < discoverableGroups.length; i++) {
            DiscoverableGroup discoverableGroup = discoverableGroups[i];
            assertEquals(discoverableGroup.getGroups().length, 1);
            assertEquals(discoverableGroup.getGroups()[0].getName(), "test-group-name-0");
            assertEquals(discoverableGroup.getGroups()[0].getId(), "test-group-id-0");
            assertEquals(discoverableGroup.getUserStore(), domainNames[i]);
        }
    }

    @Test(description = "Test retrieving discoverable groups list when the mapped all group IDs do not exist",
            dependsOnMethods = {"testDiscoverableGroupsListWhenMappedGroupIdNotExist"})
    public void testDiscoverableGroupsListWhenAllMappedGroupIdsNotExist()
            throws IdentityApplicationManagementException, UserStoreException {

        when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-0")))
                .thenThrow(new UserStoreException());
        when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-1")))
                .thenThrow(new UserStoreException());
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        ServiceProvider serviceProvider = applicationDAO.getApplication("test-app", SUPER_TENANT_DOMAIN_NAME);
        assertNull(serviceProvider.getDiscoverableGroups());
    }

    @Test(description = "Test the correct discoverable apps list for logged in user",
            dependsOnMethods = { "testDiscoverableGroupsListWhenAllMappedGroupIdsNotExist" })
    public void testDiscoverableAppsList() throws IdentityApplicationManagementException, UserStoreException {

        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        ServiceProvider serviceProvider1 = new ServiceProvider();
        serviceProvider1.setApplicationName("scl-app");
        serviceProvider1.setApplicationVersion("v1.0.0");
        serviceProvider1.setApplicationID(applicationDAO.createApplication(serviceProvider1, SUPER_TENANT_DOMAIN_NAME));
        serviceProvider1.setDiscoverable(true);
        serviceProvider1.setAccessUrl("https://localhost:5000/scl-app");
        serviceProvider1.setDiscoverableGroups(
                new DiscoverableGroup[] {getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 1, 0)});
        applicationDAO.updateApplication(serviceProvider1, SUPER_TENANT_DOMAIN_NAME);
        ServiceProvider serviceProvider2 = new ServiceProvider();
        serviceProvider2.setApplicationName("medical-app");
        serviceProvider2.setApplicationVersion("v1.0.0");
        serviceProvider2.setApplicationID(applicationDAO.createApplication(serviceProvider2, SUPER_TENANT_DOMAIN_NAME));
        serviceProvider2.setDiscoverableGroups(
                new DiscoverableGroup[] {getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 1, 1)});
        serviceProvider2.setDiscoverable(true);
        serviceProvider2.setAccessUrl("https://localhost:5000/medical-app");
        applicationDAO.updateApplication(serviceProvider2, SUPER_TENANT_DOMAIN_NAME);
        ServiceProvider serviceProvider3 = applicationDAO.getApplication("test-app", SUPER_TENANT_DOMAIN_NAME);
        serviceProvider3.setDiscoverableGroups(null);
        applicationDAO.updateApplication(serviceProvider3, SUPER_TENANT_DOMAIN_NAME);
        when(mockAbstractUserStoreManager.getGroupListOfUser(eq(USER_ID), nullable(String.class),
                nullable(String.class))).thenReturn(Collections.singletonList(new Group("test-group-id-0")));
        List<ApplicationBasicInfo> applicationBasicInfos =
                applicationDAO.getDiscoverableApplicationBasicInfo(10, 0, null, null, null, SUPER_TENANT_DOMAIN_NAME);
        assertEquals(applicationBasicInfos.size(), 2);
        assertEquals(applicationBasicInfos.get(0).getApplicationName(), "scl-app");
        assertEquals(applicationBasicInfos.get(1).getApplicationName(), "test-app");
        when(mockAbstractUserStoreManager.getGroupListOfUser(eq(USER_ID), nullable(String.class),
                nullable(String.class))).thenReturn(null);
        applicationBasicInfos =
                applicationDAO.getDiscoverableApplicationBasicInfo(10, 0, null, null, null, SUPER_TENANT_DOMAIN_NAME);
        assertEquals(applicationBasicInfos.size(), 1);
        assertEquals(applicationBasicInfos.get(0).getApplicationName(), "test-app");
    }

    @Test(description = "Test retrieving discoverable apps when getGroupListOfUser throws an exception",
            dependsOnMethods = { "testDiscoverableAppsList" })
    public void testDiscoverableAppsListWhenGetGroupListOfUserThrowsException() throws UserStoreException {

        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        when(mockAbstractUserStoreManager.getGroupListOfUser(eq(USER_ID), nullable(String.class),
                nullable(String.class))).thenThrow(new UserStoreException());
        assertThrows(IdentityApplicationManagementException.class,
                () -> applicationDAO.getDiscoverableApplicationBasicInfo(10, 0, null, null, null,
                        SUPER_TENANT_DOMAIN_NAME));
    }

    @Test(description = "Test retrieving discoverable apps list with a filter",
            dependsOnMethods = {"testDiscoverableAppsListWhenGetGroupListOfUserThrowsException"})
    public void testDiscoverableAppsListWithFilter()
            throws IdentityApplicationManagementException, UserStoreException {

        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        when(mockAbstractUserStoreManager.getGroupListOfUser(eq(USER_ID), nullable(String.class),
                nullable(String.class))).thenReturn(
                Arrays.asList(new Group("test-group-id-0"), new Group("test-group-id-1"), new Group(null)));
        List<ApplicationBasicInfo> applicationBasicInfos =
                applicationDAO.getDiscoverableApplicationBasicInfo(10, 0, "medical*", null, null,
                        SUPER_TENANT_DOMAIN_NAME);
        assertEquals(applicationBasicInfos.size(), 1);
        assertEquals(applicationBasicInfos.get(0).getApplicationName(), "medical-app");
    }

    @Test(description = "Test retrieving discoverable apps using resource ID", dependsOnMethods = {
            "testDiscoverableAppsListWithFilter" })
    public void testGetDiscoverableAppWithResourceId()
            throws IdentityApplicationManagementException, UserStoreException {

        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        ServiceProvider serviceProvider = applicationDAO.getApplication("scl-app", SUPER_TENANT_DOMAIN_NAME);
        when(mockAbstractUserStoreManager.getGroupListOfUser(eq(USER_ID), nullable(String.class),
                nullable(String.class))).thenReturn(Collections.singletonList(new Group("test-group-id-0")));
        ApplicationBasicInfo applicationBasicInfo = applicationDAO.getDiscoverableApplicationBasicInfoByResourceId(
                serviceProvider.getApplicationResourceId(), SUPER_TENANT_DOMAIN_NAME);
        assertEquals(applicationBasicInfo.getApplicationName(), "scl-app");
        when(mockAbstractUserStoreManager.getGroupListOfUser(eq(USER_ID), nullable(String.class),
                nullable(String.class))).thenReturn(new ArrayList<>());
        assertNull(applicationDAO.getDiscoverableApplicationBasicInfoByResourceId(
                serviceProvider.getApplicationResourceId(), SUPER_TENANT_DOMAIN_NAME));
    }

    @Test(description = "Test the isApplicationDiscoverable method", dependsOnMethods = {
            "testGetDiscoverableAppWithResourceId" })
    public void testIsApplicationDiscoverable()
            throws IdentityApplicationManagementException, UserStoreException {

        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        ServiceProvider serviceProvider = applicationDAO.getApplication("scl-app", SUPER_TENANT_DOMAIN_NAME);
        when(mockAbstractUserStoreManager.getGroupListOfUser(eq(USER_ID), nullable(String.class),
                nullable(String.class))).thenReturn(Collections.singletonList(new Group("test-group-id-1")));
        assertFalse(applicationDAO.isApplicationDiscoverable(serviceProvider.getApplicationResourceId(),
                SUPER_TENANT_DOMAIN_NAME));
        serviceProvider = applicationDAO.getApplication("medical-app", SUPER_TENANT_DOMAIN_NAME);
        assertTrue(applicationDAO.isApplicationDiscoverable(serviceProvider.getApplicationResourceId(),
                SUPER_TENANT_DOMAIN_NAME));
        serviceProvider = applicationDAO.getApplication("test-app", SUPER_TENANT_DOMAIN_NAME);
        assertTrue(applicationDAO.isApplicationDiscoverable(serviceProvider.getApplicationResourceId(),
                SUPER_TENANT_DOMAIN_NAME));
    }

    @Test(description = "Test getting count of discoverable applications", dependsOnMethods = {
            "testIsApplicationDiscoverable" })
    public void testGetDiscoverableAppCount() throws UserStoreException, IdentityApplicationManagementException {

        when(mockAbstractUserStoreManager.getGroupListOfUser(eq(USER_ID), nullable(String.class),
                nullable(String.class))).thenReturn(Collections.singletonList(new Group("test-group-id-0")));
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        assertEquals(applicationDAO.getCountOfDiscoverableApplications(null, SUPER_TENANT_DOMAIN_NAME), 2);
        assertEquals(applicationDAO.getCountOfDiscoverableApplications("scl*", SUPER_TENANT_DOMAIN_NAME), 1);
    }

    /**
     * Get a new DiscoverableGroup object.
     *
     * @param userStore      User store domain.
     * @param numberOfGroups Number of groups to be added.
     * @param startIndex     Suffix start index of the group.
     * @return New DiscoverableGroup object.
     */
    private DiscoverableGroup getNewDiscoverableGroup(String userStore, int numberOfGroups, int startIndex) {

        DiscoverableGroup discoverableGroup = new DiscoverableGroup();
        discoverableGroup.setUserStore(userStore);
        List<GroupBasicInfo> groupBasicInfos = new ArrayList<>();
        for (int i = startIndex; i < numberOfGroups + startIndex; i++) {
            GroupBasicInfo groupBasicInfo = new GroupBasicInfo();
            groupBasicInfo.setId("test-group-id-" + i);
            groupBasicInfo.setName("test-group-name-" + i);
            groupBasicInfos.add(groupBasicInfo);
        }
        discoverableGroup.setGroups(groupBasicInfos.toArray(new GroupBasicInfo[0]));
        return discoverableGroup;
    }

    /**
     * Setup the configurations for the test.
     */
    private void setupInitConfigurations()
            throws org.wso2.carbon.user.api.UserStoreException, IdentityApplicationManagementException {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes", "repository").
                toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(USERNAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUserId(USER_ID);

        CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = false;

        mockIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(eq(SUPER_TENANT_DOMAIN_NAME)))
                .thenReturn(SUPER_TENANT_ID);
        mockIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(eq(SUPER_TENANT_ID)))
                .thenReturn(SUPER_TENANT_DOMAIN_NAME);

        mockIdentityUtil.when(IdentityUtil::getIdentityConfigDirPath)
                .thenReturn(Paths.get(carbonHome, "conf", "identity").
                        toString());
        mockIdentityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString()))
                .thenReturn(DEFAULT_USER_STORE_DOMAIN);
        mockIdentityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(SUPER_TENANT_DOMAIN_NAME);

        mockedApplicationManagementServiceComponentHolder.when(
                        ApplicationManagementServiceComponentHolder::getInstance)
                .thenReturn(mockComponentHolder);
        when(mockComponentHolder.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getTenantUserRealm(SUPER_TENANT_ID)).thenReturn(mockUserRealm);
        when(mockUserRealm.getUserStoreManager()).thenReturn(mockAbstractUserStoreManager);
        when(mockComponentHolder.getApplicationPermissionProvider()).thenReturn(mockApplicationPermissionProvider);
        when(mockApplicationPermissionProvider.loadPermissions(anyString())).thenReturn(new ArrayList<>());
    }
}
