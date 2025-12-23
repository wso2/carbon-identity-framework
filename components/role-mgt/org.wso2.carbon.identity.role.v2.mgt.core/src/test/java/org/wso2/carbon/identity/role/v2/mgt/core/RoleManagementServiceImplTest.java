/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.identity.role.v2.mgt.core;

import org.apache.commons.lang.StringUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.dao.CacheBackedRoleDAO;
import org.wso2.carbon.identity.role.v2.mgt.core.dao.RoleDAO;
import org.wso2.carbon.identity.role.v2.mgt.core.dao.RoleMgtDAOFactory;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.UserBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.util.RoleManagementUtils;
import org.wso2.carbon.identity.testutil.IdentityBaseTest;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ALLOW_SYSTEM_PREFIX_FOR_ROLES;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.APPLICATION;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

public class RoleManagementServiceImplTest extends IdentityBaseTest {

    @Mock
    private CacheBackedRoleDAO roleDAO;

    private RoleManagementServiceImpl roleManagementService;

    private MockedStatic<RoleMgtDAOFactory> roleMgtDAOFactory;
    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext;

    private static final String USERNAME = "user";
    private static final String tenantDomain = "tenantDomain";
    private static final String audienceId = "testId";
    private static final String roleId = "testRoleId";

    private static MockedStatic<RoleManagementEventPublisherProxy> roleManagementEventPublisherProxy;

    @BeforeClass
    public static void setUpClass() throws Exception {

        roleManagementEventPublisherProxy = mockStatic(RoleManagementEventPublisherProxy.class);
    }

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        mockCarbonContextForTenant();

        roleMgtDAOFactory = mockStatic(RoleMgtDAOFactory.class);
        RoleMgtDAOFactory mockRoleMgtDAOFactory = mock(RoleMgtDAOFactory.class);
        roleMgtDAOFactory.when(RoleMgtDAOFactory::getInstance)
                .thenReturn(mockRoleMgtDAOFactory);
        when(mockRoleMgtDAOFactory.getCacheBackedRoleDAO()).thenReturn((RoleDAO) roleDAO);

        roleManagementService = new RoleManagementServiceImpl();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        privilegedCarbonContext.close();
        roleMgtDAOFactory.close();
        IdentityUtil.threadLocalProperties.remove();
    }

    @DataProvider(name = "addRoleWithSystemPrefixData")
    public Object[][] addRoleWithSystemPrefixData() {

        return new Object[][]{
                {false, "testRole", false},
                {false, "system_testRole", true},
                {true, "testRole", false},
                {true, "system_testRole", false},
        };
    }

    @Test(dataProvider = "addRoleWithSystemPrefixData")
    public void testAddRoleWithSystemPrefix(boolean allowSystemPrefix, String roleName, boolean errorScenario)
            throws IdentityRoleManagementException {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<RoleManagementUtils> roleManagementUtilsMock = mockStatic(RoleManagementUtils.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(ALLOW_SYSTEM_PREFIX_FOR_ROLES))
                    .thenReturn(String.valueOf(allowSystemPrefix));
            roleManagementUtilsMock.when(RoleManagementUtils::isAllowSystemPrefixForRole)
                    .thenReturn(allowSystemPrefix);
            roleManagementUtilsMock.when(() -> RoleManagementUtils.getOrganizationId(anyString()))
                    .thenReturn(tenantDomain);
            roleManagementUtilsMock.when(() -> RoleManagementUtils.removeInternalDomain(anyString()))
                    .thenAnswer(invocation -> {
                        String input = invocation.getArgument(0, String.class);
                        if (input.startsWith("Internal/") || input.startsWith("INTERNAL/")) {
                            return input.substring(input.indexOf("/") + 1);
                        }
                        return input;
                    });

            RoleBasicInfo mockRoleBasicInfo = new RoleBasicInfo(roleId, roleName);
            mockRoleBasicInfo.setAudience(APPLICATION);
            when(roleDAO.addRole(anyString(), anyList(), anyList(), anyList(), anyString(), anyString(), anyString()))
                    .thenReturn(mockRoleBasicInfo);
            when(roleDAO.getRoleBasicInfoById(anyString(), anyString())).thenReturn(mockRoleBasicInfo);

            RoleManagementEventPublisherProxy mockRoleMgtEventPublisherProxy = mock(
                    RoleManagementEventPublisherProxy.class);
            roleManagementEventPublisherProxy.when(RoleManagementEventPublisherProxy::getInstance)
                    .thenReturn(mockRoleMgtEventPublisherProxy);
            lenient().doNothing().when(mockRoleMgtEventPublisherProxy).publishPreAddRoleWithException(anyString(),
                    anyList(), anyList(), anyList(), anyString(), anyString(), anyString());
            lenient().doNothing().when(mockRoleMgtEventPublisherProxy).publishPostAddRole(anyString(), anyString(),
                    anyList(), anyList(), anyList(), anyString(), anyString(), anyString());

            RoleBasicInfo addedRole = roleManagementService.addRole(roleName, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), APPLICATION, audienceId, SUPER_TENANT_DOMAIN_NAME);
            if (errorScenario) {
                fail("An exception should have been thrown.");
            }
            assertNotNull(addedRole, "Role should have been added successfully.");

        } catch (IdentityRoleManagementClientException e) {
            if (errorScenario) {
                assertTrue(StringUtils.contains(e.getMessage(), String.format("Role names with the prefix: %s, " +
                        "is not allowed", UserCoreConstants.INTERNAL_SYSTEM_ROLE_PREFIX)));
            } else {
                fail("An exception should not have been thrown.");
            }
        }
    }

    @DataProvider(name = "invalidRoleNames")
    public Object[][] invalidRoleNames() {
        return new Object[][] {
                { null, "Role name cannot be empty." },
                { "", "Role name cannot be empty." },
                { StringUtils.repeat("K", 256),
                        "Provided role name exceeds the maximum length of 255 characters." }
        };
    }

    @Test(dataProvider = "invalidRoleNames", expectedExceptions = IdentityRoleManagementClientException.class)
    public void testAddRoleInvalidRoleName(String roleName, String expectedMessage) throws Exception {

        String audience = "APPLICATION";
        String audienceId = "application_id_01";

        try {
            roleManagementService.addRole(roleName, new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(), audience, audienceId, tenantDomain);
        } catch (IdentityRoleManagementClientException e) {
            assertEquals(e.getMessage(), expectedMessage);
            throw e;
        }
    }

    @Test(dataProvider = "invalidRoleNames", expectedExceptions = IdentityRoleManagementClientException.class)
    public void testUpdateRoleInvalidRoleName(String roleName, String expectedMessage) throws Exception {

        RoleManagementEventPublisherProxy mockRoleMgtEventPublisherProxy =
                mock(RoleManagementEventPublisherProxy.class);
        roleManagementEventPublisherProxy.when(RoleManagementEventPublisherProxy::getInstance)
                .thenReturn(mockRoleMgtEventPublisherProxy);

        try {
            roleManagementService.updateRoleName(roleId, roleName, tenantDomain);
        } catch (IdentityRoleManagementClientException e) {
            assertEquals(e.getMessage(), expectedMessage);
            throw e;
        }
    }

    @Test
    public void testAddRoleWithIsFragmentAppProperty() throws Exception {

        String roleName = "validRole";
        String audience = "APPLICATION";
        String audienceId = "application_id_01";
        String tenantDomain = "tenantDomain";

        Map<String, Object> mockThreadLocalProperties = new HashMap<>();
        mockThreadLocalProperties.put("isFragmentApp", Boolean.TRUE.toString());
        IdentityUtil.threadLocalProperties.set(mockThreadLocalProperties);

        RoleBasicInfo expectedRole = new RoleBasicInfo("roleId", roleName);
        when(roleDAO.addRole(anyString(), anyList(), anyList(), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(expectedRole);

        when(roleDAO.getRoleBasicInfoById(anyString(), anyString())).thenReturn(expectedRole);

        RoleManagementEventPublisherProxy mockRoleMgtEventPublisherProxy = mock(
                RoleManagementEventPublisherProxy.class);
        roleManagementEventPublisherProxy.when(RoleManagementEventPublisherProxy::getInstance)
                .thenReturn(mockRoleMgtEventPublisherProxy);
        lenient().doNothing().when(mockRoleMgtEventPublisherProxy).publishPreAddRoleWithException(anyString(),
                anyList(), anyList(), anyList(), anyString(), anyString(), anyString());
        lenient().doNothing().when(mockRoleMgtEventPublisherProxy).publishPostAddRole(anyString(), anyString(),
                anyList(), anyList(), anyList(), anyString(), anyString(), anyString());

        RoleBasicInfo result = roleManagementService.addRole(roleName, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), audience, audienceId, tenantDomain);

        assertEquals(expectedRole, result);
    }

    @Test
    public void testGetUserListOfRoleWithFilter() throws IdentityRoleManagementException {

        String filter = "name eq admin and audience eq organization and " +
                "audienceId eq a80b12a4-5168-481d-9b79-1f24bf9b883c";
        List<UserBasicInfo> userBasicInfoList = roleManagementService.getUserListOfRoles(filter, 10, 0,
                null, null, tenantDomain, "PRIMARY");
        when(roleDAO.getUserListOfRoles(any(), anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(new ArrayList<>());
        Assert.assertNotNull(userBasicInfoList);
    }

    private void mockCarbonContextForTenant() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);

        privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext mockPrivilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContext.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                .thenReturn(mockPrivilegedCarbonContext);
        when(mockPrivilegedCarbonContext.getUsername()).thenReturn(USERNAME);
    }
}
