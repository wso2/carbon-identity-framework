/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.role.mgt.core.dao;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.role.mgt.core.GroupBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.RoleBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.UserBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.dao.util.DAOUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.authorization.JDBCAuthorizationManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@WithCarbonHome
@PrepareForTest({IdentityDatabaseUtil.class, IdentityTenantUtil.class, IdentityUtil.class, UserCoreUtil.class,
                 CarbonContext.class, RoleDAOImpl.class})
@PowerMockIgnore("org.mockito.*")
public class RoleDAOTest extends PowerMockTestCase {

    private static final int SAMPLE_TENANT_ID = 1;
    private static final String SAMPLE_TENANT_DOMAIN = "wso2.com";
    private static final String DB_NAME = "ROLE_DB";
    private RoleDAO roleDAO;
    private List<String> userNamesList = new ArrayList<>();
    private List<String> emptyList = new ArrayList<>();
    private List<String> groupNamesList = new ArrayList<>();
    private Map<String, String> groupNamesMap = new HashMap<>();
    private Map<String, String> emptyMap = new HashMap<>();
    private Map<String, String> groupIdsMap = new HashMap<>();
    private List<String> userIDsList = new ArrayList<>();
    private List<String> groupIDsList = new ArrayList<>();

    @Mock
    UserRealm mockUserRealm;

    @BeforeMethod
    public void setUp() throws Exception {

        userNamesList.add("user1");
        userNamesList.add("user2");
        groupNamesList.add("group2");
        groupNamesList.add("group1");

        groupNamesMap.put("groupID2", "group2");
        groupNamesMap.put("groupID1", "group1");
        groupIdsMap.put("group2", "groupID2");
        groupIdsMap.put("group1", "groupID1");
        userIDsList.add("userID1");
        userIDsList.add("userID2");
        groupIDsList.add("groupID1");
        groupIDsList.add("groupID2");
        mockStatic(IdentityDatabaseUtil.class);
        mockStatic(IdentityTenantUtil.class);
        DAOUtils.initializeDataSource(DB_NAME, DAOUtils.getFilePath("role.sql"));
        populateDomainTable();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        userNamesList = new ArrayList<>();
        userIDsList = new ArrayList<>();
        groupIdsMap = new HashMap<>();
        groupNamesMap = new HashMap<>();
        groupNamesList = new ArrayList<>();
        groupIDsList = new ArrayList<>();
        DAOUtils.clearDataSource(DB_NAME);
    }

    @Test
    public void testAddRole() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            mockCacheClearing();
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            addRole("role1");

            doCallRealMethod().when(roleDAO, "isExistingRoleName", anyString(), anyString());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            assertTrue(roleDAO.isExistingRoleName("role1", SAMPLE_TENANT_DOMAIN));
        }
    }

    @Test
    public void testGetRoles1() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME);
                Connection connection4 = DAOUtils.getConnection(DB_NAME);
                Connection connection5 = DAOUtils.getConnection(DB_NAME);
                Connection connection6 = DAOUtils.getConnection(DB_NAME);
                Connection connection7 = DAOUtils.getConnection(DB_NAME);
                Connection connection8 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            addRole("role1");
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection4);
            addRole("role2");
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection5);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection6);
            addRole("role3");

            List<String> expectedRoles = new ArrayList<>();
            expectedRoles.add("role2");
            expectedRoles.add("role3");

            mockRealmConfiguration();
            mockStatic(UserCoreUtil.class);
            when(UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class))).thenReturn(false);

            when(IdentityUtil.getDefaultItemsPerPage()).thenReturn(IdentityCoreConstants.DEFAULT_ITEMS_PRE_PAGE);
            when(IdentityUtil.getMaximumItemPerPage()).thenReturn(IdentityCoreConstants.DEFAULT_MAXIMUM_ITEMS_PRE_PAGE);
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection7);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection8);
            doCallRealMethod().when(IdentityUtil.class, "extractDomainFromName", anyString());
            doCallRealMethod().when(UserCoreUtil.class, "removeDomainFromName", anyString());
            List<RoleBasicInfo> roles = roleDAO.getRoles(2, 1, null, null, SAMPLE_TENANT_DOMAIN);
            assertEquals(getRoleNamesList(roles), expectedRoles);
        }
    }


    @Test
    public void testCountRoles() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
             Connection connection2 = DAOUtils.getConnection(DB_NAME);
             Connection connection3 = DAOUtils.getConnection(DB_NAME);
             Connection connection4 = DAOUtils.getConnection(DB_NAME);
             Connection connection5 = DAOUtils.getConnection(DB_NAME);
             Connection connection6 = DAOUtils.getConnection(DB_NAME);
             Connection connection7 = DAOUtils.getConnection(DB_NAME);
             Connection connection8 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            addRole("role1");
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection4);
            addRole("role2");
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection5);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection6);
            addRole("role3");

            mockRealmConfiguration();
            mockStatic(UserCoreUtil.class);
            when(UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class))).thenReturn(false);
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection7);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection8);
            int rolesCount = roleDAO.getRolesCount(SAMPLE_TENANT_DOMAIN);
            assertEquals(rolesCount, 3);
        }
    }

    @DataProvider(name = "filterData")
    public Object[][] filterData() {

        // expectedResult
        // filter
        return new Object[][] {
                { "login", "login" },
                { "login", "*in" },
                { "viewRole", "view*" },
                { "editRole", "*edit*" },
                };
    }

    @Test(dataProvider = "filterData")
    public void testGetRoles2(String expectedResult, String filter) throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME);
                Connection connection4 = DAOUtils.getConnection(DB_NAME);
                Connection connection5 = DAOUtils.getConnection(DB_NAME);
                Connection connection6 = DAOUtils.getConnection(DB_NAME);
                Connection connection7 = DAOUtils.getConnection(DB_NAME);
                Connection connection8 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            addRole("login");
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection4);
            addRole("viewRole");
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection5);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection6);
            addRole("editRole");

            List<String> expectedRoles = new ArrayList<>();
            expectedRoles.add(expectedResult);

            mockRealmConfiguration();
            mockStatic(UserCoreUtil.class);
            when(UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class))).thenReturn(false);

            mockStatic(IdentityUtil.class);
            when(IdentityUtil.getDefaultItemsPerPage()).thenReturn(IdentityCoreConstants.DEFAULT_ITEMS_PRE_PAGE);
            when(IdentityUtil.getMaximumItemPerPage()).thenReturn(IdentityCoreConstants.DEFAULT_MAXIMUM_ITEMS_PRE_PAGE);
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection7);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection8);
            doCallRealMethod().when(IdentityUtil.class, "extractDomainFromName", anyString());
            doCallRealMethod().when(UserCoreUtil.class, "removeDomainFromName", anyString());
            List<RoleBasicInfo> roles = roleDAO.getRoles(filter, 3, 0, null, null, SAMPLE_TENANT_DOMAIN);
            assertEquals(getRoleNamesList(roles), expectedRoles);
        }
    }

    @Test
    public void testUpdateRoleName() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME);
                Connection connection4 = DAOUtils.getConnection(DB_NAME);
                Connection connection5 = DAOUtils.getConnection(DB_NAME);
                Connection connection6 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            mockCacheClearing();
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            RoleBasicInfo role = addRole("role1");

            mockRealmConfiguration();

            AuthorizationManager authorizationManager = mock(JDBCAuthorizationManager.class);
            when(mockUserRealm.getAuthorizationManager()).thenReturn(authorizationManager);
            doNothing().when(authorizationManager).resetPermissionOnUpdateRole(anyString(), anyString());

            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection3);
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection4);
            doReturn(true).when(roleDAO, "isExistingRoleID", eq(role.getId()), anyString());
            doReturn(false).when(roleDAO, "isExistingRoleName", eq("newRole"), anyString());
            roleDAO.updateRoleName(role.getId(), "newRole", SAMPLE_TENANT_DOMAIN);

            doCallRealMethod().when(roleDAO, "isExistingRoleName", anyString(), anyString());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection5);
            assertFalse(roleDAO.isExistingRoleName("role1", SAMPLE_TENANT_DOMAIN));

            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection6);
            assertTrue(roleDAO.isExistingRoleName("newRole", SAMPLE_TENANT_DOMAIN));
        }
    }

    @Test
    public void testDeleteRole() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME);
                Connection connection4 = DAOUtils.getConnection(DB_NAME);
                Connection connection5 = DAOUtils.getConnection(DB_NAME);
                Connection connection6 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            mockCacheClearing();
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            RoleBasicInfo role = addRole("role1");

            mockRealmConfiguration();

            mockStatic(UserCoreUtil.class);
            when(UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class))).thenReturn(false);

            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection4);
            AuthorizationManager authorizationManager = mock(JDBCAuthorizationManager.class);
            when(mockUserRealm.getAuthorizationManager()).thenReturn(authorizationManager);
            doNothing().when(authorizationManager).clearRoleAuthorization(anyString());

            roleDAO.deleteRole(role.getId(), SAMPLE_TENANT_DOMAIN);
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection5);
            doCallRealMethod().when(roleDAO, "isExistingRoleName", anyString(), anyString());
            assertFalse(roleDAO.isExistingRoleName("role1", SAMPLE_TENANT_DOMAIN));
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection6);
            doCallRealMethod().when(roleDAO, "isExistingRoleID", anyString(), anyString());
            assertFalse(roleDAO.isExistingRoleID(role.getId(), SAMPLE_TENANT_DOMAIN));
        }
    }

    @Test
    public void testIsExistingRoleName() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME);
                Connection connection4 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            addRole("role1");

            doCallRealMethod().when(roleDAO, "isExistingRoleName", anyString(), anyString());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            assertTrue(roleDAO.isExistingRoleName("role1", SAMPLE_TENANT_DOMAIN));
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection4);
            assertFalse(roleDAO.isExistingRoleName("role2", SAMPLE_TENANT_DOMAIN));
        }
    }

    @Test
    public void testGetUserListOfRole() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME);
                Connection connection4 = DAOUtils.getConnection(DB_NAME);
                Connection connection5 = DAOUtils.getConnection(DB_NAME);
                Connection connection6 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            RoleBasicInfo role = addRole("role1");

            doReturn(true).when(roleDAO, "isExistingRoleName", anyString(), anyString());
            doCallRealMethod()
                    .when(roleDAO, "updateUserListOfRole", anyString(), anyCollection(), anyCollection(), anyString());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection4);
            mockStatic(IdentityUtil.class);
            when(IdentityUtil.getPrimaryDomainName()).thenReturn("PRIMARY");
            doReturn(userNamesList).when(roleDAO, "getUserNamesByIDs", eq(userIDsList), anyString());
            doReturn(emptyList).when(roleDAO, "getUserNamesByIDs", eq(null), anyString());
            roleDAO.updateUserListOfRole(role.getId(), userIDsList, null, SAMPLE_TENANT_DOMAIN);

            mockRealmConfiguration();

            mockStatic(UserCoreUtil.class);
            when(UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class))).thenReturn(false);
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection5);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection6);
            doCallRealMethod().when(UserCoreUtil.class, "addDomainToName", anyString(), anyString());
            doReturn("userID1").when(roleDAO, "getUserIDByName", eq(userNamesList.get(0)), anyString());
            doReturn("userID2").when(roleDAO, "getUserIDByName", eq(userNamesList.get(1)), anyString());
            List<UserBasicInfo> users = roleDAO.getUserListOfRole(role.getId(), SAMPLE_TENANT_DOMAIN);
            assertEquals(getUserNamesList(users), userNamesList);
        }
    }

    @Test
    public void testGetGroupListOfRole() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME);
                Connection connection4 = DAOUtils.getConnection(DB_NAME);
                Connection connection5 = DAOUtils.getConnection(DB_NAME);
                Connection connection6 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            RoleBasicInfo role = addRole("role1");

            doReturn(true).when(roleDAO, "isExistingRoleName", anyString(), anyString());
            doCallRealMethod()
                    .when(roleDAO, "updateGroupListOfRole", anyString(), anyCollection(), anyCollection(), anyString());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection4);
            mockStatic(IdentityUtil.class);
            when(IdentityUtil.getPrimaryDomainName()).thenReturn("PRIMARY");
            doReturn(groupNamesMap).when(roleDAO, "getGroupNamesByIDs", eq(groupIDsList), anyString());
            doReturn(emptyMap).when(roleDAO, "getGroupNamesByIDs", eq(null), anyString());
            roleDAO.updateGroupListOfRole(role.getId(), groupIDsList, null, SAMPLE_TENANT_DOMAIN);

            mockRealmConfiguration();

            mockStatic(UserCoreUtil.class);
            when(UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class))).thenReturn(false);
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection5);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection6);
            doCallRealMethod().when(UserCoreUtil.class, "addDomainToName", anyString(), anyString());
            List<GroupBasicInfo> groups = roleDAO.getGroupListOfRole(role.getId(), SAMPLE_TENANT_DOMAIN);
            assertEquals(getGroupNamesList(groups), groupNamesList);
        }
    }

    @Test
    public void testDeleteUser() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME);
                Connection connection4 = DAOUtils.getConnection(DB_NAME);
                Connection connection5 = DAOUtils.getConnection(DB_NAME);
                Connection connection6 = DAOUtils.getConnection(DB_NAME);
                Connection connection7 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            mockCacheClearing();
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            RoleBasicInfo role = addRole("role1");

            doReturn(true).when(roleDAO, "isExistingRoleName", anyString(), anyString());
            doCallRealMethod()
                    .when(roleDAO, "updateUserListOfRole", anyString(), anyCollection(), anyCollection(), anyString());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection4);
            mockStatic(IdentityUtil.class);
            when(IdentityUtil.getPrimaryDomainName()).thenReturn("PRIMARY");
            doReturn(userNamesList).when(roleDAO, "getUserNamesByIDs", eq(userIDsList), anyString());
            doReturn(emptyList).when(roleDAO, "getUserNamesByIDs", eq(null), anyString());
            roleDAO.updateUserListOfRole(role.getId(), userIDsList, null, SAMPLE_TENANT_DOMAIN);

            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection5);
            doReturn("user1").when(roleDAO, "getUserNameByID", anyString(), anyString());
            roleDAO.deleteUser("userID1", SAMPLE_TENANT_DOMAIN);
            userNamesList.remove("user1");

            mockRealmConfiguration();

            mockStatic(UserCoreUtil.class);
            when(UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class))).thenReturn(false);
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection6);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection7);
            doCallRealMethod().when(UserCoreUtil.class, "addDomainToName", anyString(), anyString());
            doReturn("userID1").when(roleDAO, "getUserIDByName", eq(userNamesList.get(0)), anyString());
            List<UserBasicInfo> users = roleDAO.getUserListOfRole(role.getId(), SAMPLE_TENANT_DOMAIN);
            assertEquals(getUserNamesList(users), userNamesList);
        }
    }

    @Test
    public void testDeleteGroup() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME);
                Connection connection4 = DAOUtils.getConnection(DB_NAME);
                Connection connection5 = DAOUtils.getConnection(DB_NAME);
                Connection connection6 = DAOUtils.getConnection(DB_NAME);
                Connection connection7 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            mockCacheClearing();
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            RoleBasicInfo role = addRole("role1");

            doReturn(true).when(roleDAO, "isExistingRoleName", anyString(), anyString());
            doCallRealMethod()
                    .when(roleDAO, "updateGroupListOfRole", anyString(), anyCollection(), anyCollection(), anyString());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection4);
            mockStatic(IdentityUtil.class);
            when(IdentityUtil.getPrimaryDomainName()).thenReturn("PRIMARY");
            doReturn(groupNamesMap).when(roleDAO, "getGroupNamesByIDs", eq(groupIDsList), anyString());
            doReturn(emptyMap).when(roleDAO, "getGroupNamesByIDs", eq(null), anyString());
            roleDAO.updateGroupListOfRole(role.getId(), groupIDsList, null, SAMPLE_TENANT_DOMAIN);

            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection5);
            doReturn("group1").when(roleDAO, "getGroupNameByID", anyString(), anyString());
            roleDAO.deleteGroup("groupID1", SAMPLE_TENANT_DOMAIN);
            groupNamesMap.remove("group1");

            mockRealmConfiguration();

            mockStatic(UserCoreUtil.class);
            when(UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class))).thenReturn(false);
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection6);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection7);
            doCallRealMethod().when(UserCoreUtil.class, "addDomainToName", anyString(), anyString());
            List<GroupBasicInfo> groups = roleDAO.getGroupListOfRole(role.getId(), SAMPLE_TENANT_DOMAIN);
            assertEquals(getGroupNamesList(groups), groupNamesList);
        }
    }

    @Test
    public void testGetRoleIDByName() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            RoleBasicInfo role = addRole("role1");

            doCallRealMethod().when(roleDAO, "getRoleIDByName", anyString(), anyString());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection3);
            assertEquals(roleDAO.getRoleIDByName("Internal/role1", SAMPLE_TENANT_DOMAIN), role.getId());
        }
    }

    @Test
    public void testGetRoleNameByID() throws Exception {

        try (Connection connection1 = DAOUtils.getConnection(DB_NAME);
                Connection connection2 = DAOUtils.getConnection(DB_NAME);
                Connection connection3 = DAOUtils.getConnection(DB_NAME)) {

            roleDAO = spy(RoleMgtDAOFactory.getInstance().getRoleDAO());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection1);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection2);
            RoleBasicInfo role = addRole("role1");

            doCallRealMethod().when(roleDAO, "getRoleIDByName", anyString(), anyString());
            when(IdentityDatabaseUtil.getUserDBConnection(anyBoolean())).thenReturn(connection3);
            when(IdentityDatabaseUtil.getDBConnection(anyBoolean())).thenReturn(connection3);
            assertEquals(roleDAO.getRoleNameByID(role.getId(), SAMPLE_TENANT_DOMAIN), "role1");
        }
    }

    private RoleBasicInfo addRole(String roleName) throws Exception {

        mockCacheClearing();
        mockStatic(IdentityUtil.class);
        when(IdentityUtil.getPrimaryDomainName()).thenReturn("PRIMARY");
        doCallRealMethod().when(IdentityUtil.class, "extractDomainFromName", anyString());
        doReturn(new ArrayList<>()).when(roleDAO, "getUserNamesByIDs", anyCollection(), anyString());
        doReturn(new HashMap<>()).when(roleDAO, "getGroupNamesByIDs", anyCollection(), anyString());
        doReturn(false).when(roleDAO, "isExistingRoleName", anyString(), anyString());
        doReturn(groupIdsMap).when(roleDAO, "getGroupIDsByNames", anyCollection(), anyString());
        doReturn("roleID").when(roleDAO, "getRoleIDByName", anyString(), anyString());
        doReturn(roleName).when(roleDAO, "getRoleNameByID", anyString(), anyString());
        when(IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);

        return roleDAO.addRole(roleName, userIDsList, groupIDsList, null, SAMPLE_TENANT_DOMAIN);
    }

    private List<String> getRoleNamesList(List<RoleBasicInfo> roles) {

        List<String> roleNames = new ArrayList<>();
        for (RoleBasicInfo role : roles) {
            roleNames.add(role.getName());
        }
        return roleNames;
    }

    private List<String> getUserNamesList(List<UserBasicInfo> users) {

        List<String> userNames = new ArrayList<>();
        for (UserBasicInfo user : users) {
            userNames.add(user.getName());
        }
        return userNames;
    }

    private List<String> getGroupNamesList(List<GroupBasicInfo> groups) {

        List<String> groupNames = new ArrayList<>();
        for (GroupBasicInfo group : groups) {
            groupNames.add(group.getName());
        }
        return groupNames;
    }

    private void mockRealmConfiguration() throws UserStoreException {

        mockStatic(CarbonContext.class);
        CarbonContext carbonContext = mock(CarbonContext.class);
        when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        when(CarbonContext.getThreadLocalCarbonContext().getUserRealm()).thenReturn(mockUserRealm);
        RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);
        when(mockUserRealm.getRealmConfiguration()).thenReturn(realmConfiguration);
    }

    private void populateDomainTable() throws Exception {

        String domainDataSQL = "INSERT INTO UM_DOMAIN (UM_DOMAIN_ID, UM_DOMAIN_NAME, UM_TENANT_ID) VALUES "
                + "(1,'PRIMARY',-1234), (2,'SYSTEM',-1234), (3,'INTERNAL',-1234), (4,'APPLICATION',-1234), "
                + "(5,'WORKFLOW',-1234), (6,'PRIMARY',1), (7,'SYSTEM',1), (8,'INTERNAL',1), (9,'APPLICATION',1), "
                + "(10,'WORKFLOW',1)";

        try (Connection connection = DAOUtils.getConnection(DB_NAME)) {
            connection.createStatement().executeUpdate(domainDataSQL);
        } catch (SQLException e) {
            String errorMessage = "Error while Adding test data for UM_DOMAIN table";
            throw new Exception(errorMessage, e);
        }
    }

    private void mockCacheClearing() throws Exception {

        doNothing().when(roleDAO, "clearUserRolesCache", anyString(), anyInt());
        doNothing().when(roleDAO, "clearUserRolesCacheByTenant", anyInt());
    }
}
