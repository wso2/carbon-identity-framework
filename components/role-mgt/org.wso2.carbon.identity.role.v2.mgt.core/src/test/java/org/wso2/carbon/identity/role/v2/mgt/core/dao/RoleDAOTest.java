/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.role.v2.mgt.core.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.model.IdpGroup;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.UserBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.util.GroupIDResolver;
import org.wso2.carbon.identity.role.v2.mgt.core.util.UserIDResolver;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.authorization.AuthorizationCache;
import org.wso2.carbon.user.core.authorization.JDBCAuthorizationManager;
import org.wso2.carbon.user.core.common.UserRolesCache;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.common.testng.TestConstants.USER_DOMAIN_PRIMARY;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_REQUEST;

@WithCarbonHome
@Listeners(MockitoTestNGListener.class)
public class RoleDAOTest {

    private static final int SAMPLE_TENANT_ID = 1;
    private static final String SAMPLE_TENANT_DOMAIN = "wso2.com";
    private static final String SAMPLE_ORG_ID = "test-org-id";
    private static final String SAMPLE_APP_ID = "test-app-id";
    private static final String DB_NAME = "ROLE_DB";
    private static final String ORGANIZATION_AUD  = "organization";
    private static final String APPLICATION_AUD  = "application";
    private static Map<String, BasicDataSource> dataSourceMap = new HashMap<>();
    private List<String> userNamesList = new ArrayList<>();
    private List<String> groupNamesList = new ArrayList<>();
    private List<String> roleNamesList = new ArrayList<>();
    private Map<String, String> groupNamesMap = new HashMap<>();
    private Map<String, String> groupIdsMap = new HashMap<>();
    private List<String> userIDsList = new ArrayList<>();
    private List<String> groupIDsList = new ArrayList<>();
    private List<Permission> permissions = new ArrayList<>();

    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtil;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<IdentityUtil> identityUtil;
    private MockedStatic<CarbonContext> carbonContext;
    private MockedStatic<UserCoreUtil> userCoreUtil;
    private MockedStatic<UserRolesCache> userRolesCache;
    private MockedStatic<AuthorizationCache> authorizationCache;

    @Mock
    UserRealm mockUserRealm;

    MockedStatic<OrganizationManagementUtil> organizationManagementUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        roleNamesList.add("role1");
        roleNamesList.add("role2");
        roleNamesList.add("role3");
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
        permissions.add(new Permission("read", "read"));
        permissions.add(new Permission("write", "write"));

        identityDatabaseUtil = mockStatic(IdentityDatabaseUtil.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityUtil = mockStatic(IdentityUtil.class);
        carbonContext = mockStatic(CarbonContext.class);
        userRolesCache = mockStatic(UserRolesCache.class);
        authorizationCache = mockStatic(AuthorizationCache.class);

        initializeDataSource(getFilePath("h2.sql"));
        populateData();

        organizationManagementUtil = mockStatic(OrganizationManagementUtil.class);
        userCoreUtil = mockStatic(UserCoreUtil.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        userNamesList = new ArrayList<>();
        userIDsList = new ArrayList<>();
        groupIdsMap = new HashMap<>();
        groupNamesMap = new HashMap<>();
        groupNamesList = new ArrayList<>();
        groupIDsList = new ArrayList<>();
        permissions = new ArrayList<>();
        clearDataSource();
        identityDatabaseUtil.close();
        identityTenantUtil.close();
        identityUtil.close();
        carbonContext.close();
        organizationManagementUtil.close();
        userCoreUtil.close();
        userRolesCache.close();
        authorizationCache.close();
    }

    @Test
    public void testAddOrgRole() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        addRole(roleNamesList.get(0), ORGANIZATION_AUD, SAMPLE_ORG_ID, roleDAO);
        assertTrue(roleDAO.isExistingRoleName(roleNamesList.get(0), ORGANIZATION_AUD, SAMPLE_ORG_ID,
                SAMPLE_TENANT_DOMAIN));
    }

    @Test
    public void testAddAppRole() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        assertTrue(roleDAO.isExistingRoleName(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID,
                SAMPLE_TENANT_DOMAIN));
    }

    @Test
    public void testAddRoles() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        addRole(roleNamesList.get(0), ORGANIZATION_AUD, SAMPLE_ORG_ID, roleDAO);
        addRole(roleNamesList.get(1), ORGANIZATION_AUD, SAMPLE_ORG_ID, roleDAO);
        addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole(roleNamesList.get(0), APPLICATION_AUD, "test-app-id-2", roleDAO);
    }

    @Test
    public void testAddRolesException() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(() -> IdentityUtil.getPrimaryDomainName()).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        addRole(roleNamesList.get(0), ORGANIZATION_AUD, SAMPLE_ORG_ID, roleDAO);
        Exception exception = null;
        try {
            addRole(roleNamesList.get(0), ORGANIZATION_AUD, SAMPLE_ORG_ID, roleDAO);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    @Test
    public void testGetRoles() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole(roleNamesList.get(1), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole(roleNamesList.get(2), ORGANIZATION_AUD, SAMPLE_ORG_ID, roleDAO);

        List<String> expectedRoles = new ArrayList<>();
        expectedRoles.add(roleNamesList.get(1));
        expectedRoles.add(roleNamesList.get(2));

        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();

        identityUtil.when(IdentityUtil::getDefaultItemsPerPage)
                .thenReturn(IdentityCoreConstants.DEFAULT_ITEMS_PRE_PAGE);
        identityUtil.when(IdentityUtil::getMaximumItemPerPage)
                .thenReturn(IdentityCoreConstants.DEFAULT_MAXIMUM_ITEMS_PRE_PAGE);
        List<RoleBasicInfo> roles = roleDAO.getRoles(2, 1, null, null,
                SAMPLE_TENANT_DOMAIN);
        Assert.assertEquals(getRoleNamesList(roles), expectedRoles);
    }

    @Test
    public void testGetRolesWithFilter() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        identityUtil.when(IdentityUtil::getDefaultItemsPerPage)
                .thenReturn(IdentityCoreConstants.DEFAULT_ITEMS_PRE_PAGE);
        identityUtil.when(IdentityUtil::getMaximumItemPerPage)
                .thenReturn(IdentityCoreConstants.DEFAULT_MAXIMUM_ITEMS_PRE_PAGE);

        addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole(roleNamesList.get(1), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole(roleNamesList.get(2), ORGANIZATION_AUD, SAMPLE_ORG_ID, roleDAO);

        List<String> expectedRoles = new ArrayList<>();
        expectedRoles.add(roleNamesList.get(1));

        List<ExpressionNode> expressionNodes =
                getExpressionNodes("name co 2 and audience co application and audienceId co test-app-id");
        List<RoleBasicInfo> roles = roleDAO.getRoles(expressionNodes, 2, 1, null, null,
                SAMPLE_TENANT_DOMAIN);
        Assert.assertEquals(getRoleNamesList(roles), expectedRoles);
    }

    @Test
    public void testDeleteRole() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.addDomainToName(anyString(), anyString())).thenCallRealMethod();
        RoleBasicInfo role = addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        mockRealmConfiguration();

        AuthorizationManager authorizationManager = mock(JDBCAuthorizationManager.class);
        when(mockUserRealm.getAuthorizationManager()).thenReturn(authorizationManager);
        doNothing().when(authorizationManager).clearRoleAuthorization(anyString());
        roleDAO.deleteRole(role.getId(), SAMPLE_TENANT_DOMAIN);

        assertFalse(roleDAO.isExistingRoleName(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID,
                SAMPLE_TENANT_DOMAIN));

    }

    @Test
    public void testGetPermissionListOfRole() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        RoleBasicInfo role = addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        List<Permission> rolePermissions = roleDAO.getPermissionListOfRole(role.getId(), SAMPLE_TENANT_DOMAIN);
        Assert.assertEquals(getPermissionNameList(rolePermissions), getPermissionNameList(permissions));
    }

    @Test
    public void testUpdatePermissionListOfRole() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        RoleBasicInfo role = addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        List<Permission> newPermissions = new ArrayList<>();
        newPermissions.add(new Permission("view", "view"));
        newPermissions.add(new Permission("update", "update"));
        roleDAO.updatePermissionListOfRole(role.getId(), newPermissions, permissions, SAMPLE_TENANT_DOMAIN);
        List<Permission> rolePermissions = roleDAO.getPermissionListOfRole(role.getId(), SAMPLE_TENANT_DOMAIN);
        Assert.assertEquals(getPermissionNameList(rolePermissions), getPermissionNameList(newPermissions));
    }

    @Test
    public void testUpdateIdpGroupListOfRole() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();

        IdpManager idpManager = mock(IdpManager.class);
        List<IdPGroup> idpGroups = buildIdPGroups();
        when(idpManager.getValidIdPGroupsByIdPGroupIds(anyList(), anyString())).thenReturn(idpGroups);
        RoleManagementServiceComponentHolder.getInstance().setIdentityProviderManager(idpManager);
        RoleBasicInfo role = addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        List<IdpGroup> newGroups = new ArrayList<>();
        newGroups.add(new IdpGroup("test-group1-id", "test-idp-id"));
        newGroups.add(new IdpGroup("test-group2-id", "test-idp-id"));
        doNothing().when(roleDAO).validateGroupIds(anyList(), anyString());
        roleDAO.updateIdpGroupListOfRole(role.getId(), newGroups, new ArrayList<>(), SAMPLE_TENANT_DOMAIN);
        List<IdpGroup> groups = roleDAO.getIdpGroupListOfRole(role.getId(), SAMPLE_TENANT_DOMAIN);
        List<String> groupIds = new ArrayList<>();
        groupIds.add("test-group1-id");
        groupIds.add("test-group2-id");
        Assert.assertEquals(getIdpGroupIdList(groups), groupIds);

    }

    private List<IdPGroup> buildIdPGroups() {

        List<IdPGroup> idpGroups = new ArrayList<>();
        IdPGroup group1 = new IdPGroup();
        group1.setIdpGroupId("test-group1-id");
        group1.setIdpId("test-idp-id");
        group1.setIdpGroupName("group1");
        idpGroups.add(group1);

        IdPGroup group2 = new IdPGroup();
        group2.setIdpGroupId("test-group2-id");
        group2.setIdpId("test-idp-id");
        group2.setIdpGroupName("group1");
        idpGroups.add(group2);

        return idpGroups;
    }

    @Test
    public void testGetRoleBasicInfoById() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        RoleBasicInfo role = addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        RoleBasicInfo roleBasicInfo = roleDAO.getRoleBasicInfoById(role.getId(), SAMPLE_TENANT_DOMAIN);
        assertEquals(roleBasicInfo.getAudience(), APPLICATION_AUD);
        assertEquals(roleBasicInfo.getAudienceId(), SAMPLE_APP_ID);

    }

    @Test
    public void testCountRoles() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole(roleNamesList.get(1), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole(roleNamesList.get(2), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);

        int rolesCount = roleDAO.getRolesCount(SAMPLE_TENANT_DOMAIN);
        assertEquals(rolesCount, 3);
    }

    @Test
    public void testUpdateRoleName() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        RoleBasicInfo role = addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        roleDAO.updateRoleName(role.getId(), "newRole", APPLICATION_AUD);

        assertFalse(roleDAO.isExistingRoleName(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID,
                SAMPLE_TENANT_DOMAIN));

        assertTrue(roleDAO.isExistingRoleName("newRole", APPLICATION_AUD, SAMPLE_APP_ID,
                SAMPLE_TENANT_DOMAIN));
    }

    @Test
    public void testIsExistingRoleName() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);

        assertTrue(roleDAO.isExistingRoleName(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID,
                SAMPLE_TENANT_DOMAIN));
        assertFalse(roleDAO.isExistingRoleName(roleNamesList.get(1), APPLICATION_AUD, SAMPLE_APP_ID,
                SAMPLE_TENANT_DOMAIN));
    }

    @Test
    public void testGetRoleListOfUser() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.addDomainToName(anyString(), anyString())).thenCallRealMethod();
        addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole(roleNamesList.get(1), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole("everyone", ORGANIZATION_AUD, SAMPLE_ORG_ID, roleDAO);

        mockRealmConfiguration();
        List<RoleBasicInfo> roles = roleDAO.getRoleListOfUser("userID1", SAMPLE_TENANT_DOMAIN);
        assertEquals(roles.size(), 3);
    }

    @Test
    public void testGetRoleListOfGroups() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.addDomainToName(anyString(), anyString())).thenCallRealMethod();
        addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole(roleNamesList.get(1), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);

        List<RoleBasicInfo> roles = roleDAO.getRoleListOfGroups(groupIDsList, SAMPLE_TENANT_DOMAIN);
        assertEquals(roles.size(), 2);
    }

    @Test
    public void testGetRoleListOfIdpGroups() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        RoleBasicInfo role = addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        List<IdpGroup> newGroups = new ArrayList<>();
        List<String> groupIds = new ArrayList<>();
        newGroups.add(new IdpGroup("test-group1-id", "test-idp-id"));
        groupIds.add("test-group1-id");
        newGroups.add(new IdpGroup("test-group2-id", "test-idp-id"));
        groupIds.add("test-group2-id");
        doNothing().when(roleDAO).validateGroupIds(anyList(), anyString());
        roleDAO.updateIdpGroupListOfRole(role.getId(), newGroups, new ArrayList<>(), SAMPLE_TENANT_DOMAIN);

        List<RoleBasicInfo> roles = roleDAO.getRoleListOfIdpGroups(groupIds, SAMPLE_TENANT_DOMAIN);
        assertEquals(roles.size(), 1);
    }

    @Test
    public void testGetRoleIdListOfUser() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        mockRealmConfiguration();
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.addDomainToName(anyString(), anyString())).thenCallRealMethod();
        addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole(roleNamesList.get(1), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole("everyone", ORGANIZATION_AUD, SAMPLE_ORG_ID, roleDAO);
        List<String> roles = roleDAO.getRoleIdListOfUser("userID1", SAMPLE_TENANT_DOMAIN);
        assertEquals(roles.size(), 3);
    }

    @Test
    public void testGetRoleIdListOfGroups() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.addDomainToName(anyString(), anyString())).thenCallRealMethod();
        addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        addRole(roleNamesList.get(1), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);

        List<String> roles = roleDAO.getRoleIdListOfGroups(groupIDsList, SAMPLE_TENANT_DOMAIN);
        assertEquals(roles.size(), 2);

    }

    @Test
    public void testGetRoleIdListOfIdpGroups() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.addDomainToName(anyString(), anyString())).thenCallRealMethod();
        RoleBasicInfo role = addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
        List<IdpGroup> newGroups = new ArrayList<>();
        List<String> groupIds = new ArrayList<>();
        newGroups.add(new IdpGroup("test-group1-id", "test-idp-id"));
        groupIds.add("test-group1-id");
        newGroups.add(new IdpGroup("test-group2-id", "test-idp-id"));
        groupIds.add("test-group2-id");
        IdpManager mockIdpManager = mock(IdpManager.class);
        IdentityProvider mockIdentityProvider = mock(IdentityProvider.class);
        IdPGroup[] idpGroups = buildIdPGroups().toArray(new IdPGroup[0]);
        ;
        when(mockIdpManager.getIdPByResourceId(anyString(), anyString(), anyBoolean())).thenReturn(
                mockIdentityProvider);
        when(mockIdentityProvider.getIdPGroupConfig()).thenReturn(idpGroups);
        RoleManagementServiceComponentHolder.getInstance().setIdentityProviderManager(mockIdpManager);
        roleDAO.updateIdpGroupListOfRole(role.getId(), newGroups, new ArrayList<>(), SAMPLE_TENANT_DOMAIN);

        List<String> roles = roleDAO.getRoleIdListOfIdpGroups(groupIds, SAMPLE_TENANT_DOMAIN);
        assertEquals(roles.size(), 1);
    }

    @Test
    public void testDeleteRolesByApplication() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
            addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);
            roleDAO.deleteRolesByApplication(SAMPLE_APP_ID, SAMPLE_TENANT_DOMAIN);
            assertFalse(roleDAO.isExistingRoleName(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID,
                    SAMPLE_TENANT_DOMAIN));
    }

    @Test
    public void testGetUserListOfRole() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        mockCacheClearing(roleDAO);
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getUserDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityDatabaseUtil.when(() -> IdentityDatabaseUtil.getDBConnection(anyBoolean()))
                .thenAnswer(invocation -> getConnection());
        identityUtil.when(IdentityUtil::getPrimaryDomainName).thenReturn(USER_DOMAIN_PRIMARY);
        identityUtil.when(() -> IdentityUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(anyString())).thenReturn(SAMPLE_TENANT_ID);
        userCoreUtil.when(() -> UserCoreUtil.isEveryoneRole(anyString(), any(RealmConfiguration.class)))
                .thenReturn(false);
        userCoreUtil.when(() -> UserCoreUtil.removeDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.extractDomainFromName(anyString())).thenCallRealMethod();
        userCoreUtil.when(() -> UserCoreUtil.addDomainToName(anyString(), anyString())).thenCallRealMethod();
        RoleBasicInfo role = addRole(roleNamesList.get(0), APPLICATION_AUD, SAMPLE_APP_ID, roleDAO);

        mockRealmConfiguration();
        when(IdentityUtil.getMaximumUsersListPerRole()).thenReturn(1000);
        List<UserBasicInfo> users = roleDAO.getUserListOfRole(role.getId(), SAMPLE_TENANT_DOMAIN);
        assertEquals(getUserNamesList(users), userNamesList);
    }

    @Test
    void testIsValidSubOrgPermission() throws Exception {

        RoleDAOImpl roleDAO = spy(new RoleDAOImpl());
        // Get the private method using reflection
        Method isValidSubOrgPermissionMethod = RoleDAOImpl.class.getDeclaredMethod("isValidSubOrgPermission",
                String.class);
        isValidSubOrgPermissionMethod.setAccessible(true);

        // Test case: permission starts with INTERNAL_ORG_SCOPE_PREFIX
        assertTrue((Boolean) isValidSubOrgPermissionMethod.invoke(roleDAO, "internal_org_application_mgt_view"));

        // Test case: permission starts with CONSOLE_ORG_SCOPE_PREFIX
        assertTrue((Boolean) isValidSubOrgPermissionMethod.invoke(roleDAO, "console:org:applications"));

        // Test case: permission does not start with INTERNAL_SCOPE_PREFIX or CONSOLE_SCOPE_PREFIX
        assertTrue((Boolean) isValidSubOrgPermissionMethod.invoke(roleDAO, "read"));

        // Test case: permission starts with INTERNAL_SCOPE_PREFIX
        assertFalse((Boolean) isValidSubOrgPermissionMethod.invoke(roleDAO, "internal_application_mgt_view"));

        // Test case: permission starts with CONSOLE_SCOPE_PREFIX
        assertFalse((Boolean) isValidSubOrgPermissionMethod.invoke(roleDAO, "console:applications"));
    }

    private List<String> getUserNamesList(List<UserBasicInfo> users) {

        List<String> userNames = new ArrayList<>();
        for (UserBasicInfo user : users) {
            userNames.add(user.getName());
        }
        return userNames;
    }

    private RoleBasicInfo addRole(String roleName, String audience, String audienceId, RoleDAOImpl roleDAO)
            throws Exception {

        OrganizationManager organizationManager = mock(OrganizationManager.class);
        RoleManagementServiceComponentHolder.getInstance().setOrganizationManager(organizationManager);
        lenient().when(organizationManager.getOrganizationNameById(anyString())).thenReturn("test-org");
        lenient().when(organizationManager.resolveOrganizationId(anyString())).thenReturn(SAMPLE_ORG_ID);
        organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(anyString())).thenReturn(false);
        UserIDResolver userIDResolver = mock(UserIDResolver.class);
        setPrivateFinalField(RoleDAOImpl.class, "userIDResolver", roleDAO, userIDResolver);
        when(userIDResolver.getNamesByIDs(anyList(), anyString())).thenReturn(userNamesList);
        lenient().when(userIDResolver.getNameByID(eq(userIDsList.get(0)), anyString()))
                .thenReturn(userNamesList.get(0));
        lenient().when(userIDResolver.getNameByID(eq(userIDsList.get(1)), anyString()))
                .thenReturn(userNamesList.get(1));

        GroupIDResolver groupIDResolver = mock(GroupIDResolver.class);
        setPrivateFinalField(RoleDAOImpl.class, "groupIDResolver", roleDAO, groupIDResolver);
        when(groupIDResolver.getNamesByIDs(anyList(), anyString())).thenReturn(groupNamesMap);

        if ("everyone".equals(roleName)) {
            return roleDAO.addRole(roleName, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), audience,
                    audienceId, SAMPLE_TENANT_DOMAIN);
        } else {
            return roleDAO.addRole(roleName, userIDsList, groupIDsList, permissions, audience, audienceId,
                    SAMPLE_TENANT_DOMAIN);
        }
    }

    private void mockCacheClearing(RoleDAOImpl roleDAO) throws Exception {

        UserRolesCache mockUserRolesCache = mock(UserRolesCache.class);
        userRolesCache.when(UserRolesCache::getInstance).thenReturn(mockUserRolesCache);
        lenient().doNothing().when(mockUserRolesCache).clearCacheEntry(anyString(), anyInt(), anyString());

        AuthorizationCache mockAuthorizationCache = mock(AuthorizationCache.class);
        userRolesCache.when(AuthorizationCache::getInstance).thenReturn(mockAuthorizationCache);
        lenient().doNothing().when(mockAuthorizationCache).clearCacheByTenant(anyInt());
        doNothing().when(mockAuthorizationCache).clearCacheByUser(anyInt(), anyString());

        RealmService mockRealmService = mock(RealmService.class);
        when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealm);
        RealmConfiguration mockRealmConfiguration = mock(RealmConfiguration.class);
        when(mockUserRealm.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
        lenient().when(mockRealmConfiguration.getUserStoreProperty(anyString())).thenReturn("true");
        RoleManagementServiceComponentHolder.getInstance().setRealmService(mockRealmService);
    }

    private void mockRealmConfiguration() throws UserStoreException {

        CarbonContext mockCarbonContext = mock(CarbonContext.class);
        carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
        when(mockCarbonContext.getUserRealm()).thenReturn(mockUserRealm);
        RealmConfiguration realmConfiguration = mock(RealmConfiguration.class);
        when(mockUserRealm.getRealmConfiguration()).thenReturn(realmConfiguration);
        lenient().when(realmConfiguration.getEveryOneRoleName()).thenReturn("Internal/everyone");
    }

    private List<String> getRoleNamesList(List<RoleBasicInfo> roles) {

        List<String> roleNames = new ArrayList<>();
        for (RoleBasicInfo role : roles) {
            roleNames.add(role.getName());
        }
        return roleNames.stream().sorted().collect(Collectors.toList());
    }

    private List<String> getPermissionNameList(List<Permission> permissions) {

        List<String> permissionNames = new ArrayList<>();
        for (Permission permission : permissions) {
            permissionNames.add(permission.getName());
        }
        return permissionNames.stream().sorted().collect(Collectors.toList());
    }

    private List<String> getIdpGroupIdList(List<IdpGroup> idpGroups) {

        List<String> ids = new ArrayList<>();
        for (IdpGroup group : idpGroups) {
            ids.add(group.getGroupId());
        }
        return ids.stream().sorted().collect(Collectors.toList());
    }
    private void initializeDataSource(String scriptPath) throws Exception {

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:h2:mem:" + RoleDAOTest.DB_NAME);

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("RUNSCRIPT FROM '" + scriptPath + "'");
        }
        dataSourceMap.put(RoleDAOTest.DB_NAME, dataSource);
    }

    private String getFilePath(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            return Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "dbscripts", fileName)
                    .toString();
        }
        return null;
    }

    private void populateData() throws Exception {

        String domainDataSQL = "INSERT INTO UM_DOMAIN (UM_DOMAIN_ID, UM_DOMAIN_NAME, UM_TENANT_ID) VALUES "
                + "(1,'PRIMARY',-1234), (2,'SYSTEM',-1234), (3,'INTERNAL',-1234), (4,'APPLICATION',-1234), "
                + "(5,'WORKFLOW',-1234), (6,'PRIMARY',1), (7,'SYSTEM',1), (8,'INTERNAL',1), (9,'APPLICATION',1), "
                + "(10,'WORKFLOW',1)";
        String aPIResourceSQL = "INSERT INTO API_RESOURCE (ID, NAME, IDENTIFIER, TENANT_ID, DESCRIPTION, TYPE," +
                " REQUIRES_AUTHORIZATION) VALUES (1,'DOC','DOC',1,'DOC','RBAC',true);";
        String scopeSQL = "INSERT INTO SCOPE (ID,API_ID,NAME,DISPLAY_NAME,TENANT_ID,DESCRIPTION) VALUES " +
                "(1,1,'read','read',1,'read'), (2,1,'write','write',1,'write'), (3,1,'view','view',1,'view') " +
                ", (4,1,'update','update',1,'update')";
        String spAppSQL = "INSERT INTO SP_APP (ID, TENANT_ID, APP_NAME, USER_STORE, USERNAME, AUTH_TYPE, UUID) " +
                "VALUES (1, 1, 'TEST_APP_NAME','TEST_USER_STORE', 'TEST_USERNAME', 'TEST_AUTH_TYPE', 'test-app-id'), " +
                "(2, 1, 'TEST_APP_NAME2','TEST_USER_STORE', 'TEST_USERNAME', 'TEST_AUTH_TYPE', 'test-app-id-2')";
        String idpSQL = "INSERT INTO IDP (ID, TENANT_ID, NAME, UUID) VALUES (1, 1, 'TEST_IDP_NAME', 'test-idp-id');";
        String idpGroupSQL = "INSERT INTO IDP_GROUP (ID, IDP_ID, TENANT_ID, GROUP_NAME, UUID) VALUES " +
                "(1, 1, 1, 'group1', 'test-group1-id'), (2, 1, 1, 'group2', 'test-group2-id');";

        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate(domainDataSQL);
            connection.createStatement().executeUpdate(aPIResourceSQL);
            connection.createStatement().executeUpdate(scopeSQL);
            connection.createStatement().executeUpdate(spAppSQL);
            connection.createStatement().executeUpdate(idpSQL);
            connection.createStatement().executeUpdate(idpGroupSQL);
        } catch (SQLException e) {
            String errorMessage = "Error while Adding test data for tables";
            throw new Exception(errorMessage, e);
        }
    }

    private Connection getConnection() throws Exception {
        if (dataSourceMap.get(RoleDAOTest.DB_NAME) != null) {
            return dataSourceMap.get(RoleDAOTest.DB_NAME).getConnection();
        }
        throw new RuntimeException("Invalid datasource.");
    }

    private void clearDataSource() throws Exception {

        BasicDataSource dataSource = dataSourceMap.get(DB_NAME);
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().executeUpdate("DROP ALL OBJECTS;");
        }
    }

    /**
     * Get the filter node as a list.
     *
     * @param filter Filter string.
     * @throws IdentityRoleManagementException Error when validate filters.
     */
    private List<ExpressionNode> getExpressionNodes(String filter) throws IdentityRoleManagementException {

        List<ExpressionNode> expressionNodes = new ArrayList<>();
        filter = StringUtils.isBlank(filter) ? StringUtils.EMPTY : filter;
        try {
            if (StringUtils.isNotBlank(filter)) {
                FilterTreeBuilder filterTreeBuilder = new FilterTreeBuilder(filter);
                Node rootNode = filterTreeBuilder.buildTree();
                setExpressionNodeList(rootNode, expressionNodes);
            }
            return expressionNodes;
        } catch (IOException | IdentityException e) {
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), "Invalid filter");
        }
    }

    /**
     * Set the node values as list of expression.
     *
     * @param node       filter node.
     * @param expression list of expression.
     */
    private void setExpressionNodeList(Node node, List<ExpressionNode> expression) {

        if (node instanceof ExpressionNode) {
            if (StringUtils.isNotBlank(((ExpressionNode) node).getAttributeValue())) {
                expression.add((ExpressionNode) node);
            }
        } else if (node instanceof OperationNode) {
            setExpressionNodeList(node.getLeftNode(), expression);
            setExpressionNodeList(node.getRightNode(), expression);
        }
    }

    public void setPrivateFinalField(Class clazz, String fieldName, Object instance, Object value)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(instance, value);
    }
}
