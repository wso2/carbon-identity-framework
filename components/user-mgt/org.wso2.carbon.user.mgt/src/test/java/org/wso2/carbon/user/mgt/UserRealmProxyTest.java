/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.mgt;

import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.hybrid.HybridRoleManager;
import org.wso2.carbon.user.mgt.common.ClaimValue;
import org.wso2.carbon.user.mgt.common.FlaggedName;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.carbon.user.mgt.common.UserRealmInfo;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.user.mgt.UserRealmProxy.PERMISSION;

@PowerMockIgnore({"javax.net.*", "javax.security.*", "javax.crypto.*", "javax.xml.*"})
@PrepareForTest({UserRealm.class, AuthorizationManager.class, AbstractUserStoreManager.class})
public class UserRealmProxyTest {
    private UserRealm realm;
    private UserRealmProxy userRealmProxy;
    private UserStoreManager userStoreManagerWithAb;
    private UserStoreManager userStoreManager;
    private AuthorizationManager authorizationManager;
    private ClaimManager claimManager;

    @DataProvider(name = "userListCount")
    public static Object[][] itrCount() {
        return new Object[][]{{10, 3}, {0, 1}};
    }

    @BeforeTest
    public void setUp() throws Exception {
        startTenantFlow("carbon.super");
        realm = mock(UserRealm.class);
        userStoreManagerWithAb = mock(AbstractUserStoreManager.class);
        userStoreManager = mock(UserStoreManager.class);
        authorizationManager = mock(AuthorizationManager.class);
        claimManager = mock(ClaimManager.class);
        userRealmProxy = new UserRealmProxy(realm);
        Mockito.when(realm.getRealmConfiguration()).thenReturn(this.getSampleRelaimConfiguration());
    }

    @Test
    public void testListUsers() throws Exception {
        Mockito.when(realm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(userStoreManager.listUsers(null, 10)).thenReturn(new String[]{"test1", "test2"});
        String[] userList = userRealmProxy.listUsers(null, 10);
        Assert.assertEquals(userList, new String[]{"test1", "test2"});
    }

    @Test
    public void testListUsers1() throws Exception {
        ClaimValue value = new ClaimValue();
        value.setClaimURI("mail");
        value.setValue("sd@sds.com");
        Map<String, Integer> maxListCount = new HashMap();
        maxListCount.put(null, 100);
        Mockito.when(realm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(userStoreManager.getUserList(value.getClaimURI(),
                value.getValue(), null)).thenReturn(new String[]{"test1", "test2"});
        FlaggedName[] userList = userRealmProxy.listUsers(value, "test", 10);
        Assert.assertEquals(userList.length, 3);
    }

    @Test
    public void testGetAllSharedRoleNames() throws Exception {
        Mockito.when(((AbstractUserStoreManager) userStoreManagerWithAb)
                .getSharedRoleNames("test", 10)).thenReturn(new String[]{"test1", "test2"});
        Mockito.when(realm.getUserStoreManager()).thenReturn(userStoreManagerWithAb);
        FlaggedName[] roleList = userRealmProxy.getAllSharedRoleNames("test", 10);
        Assert.assertTrue(roleList[0].getItemName().equals("test1")
                        && roleList[1].getItemName().equals("test2"),
                "Role List doesnot contain intended values in order");
        Assert.assertEquals(roleList.length, 3);
    }

    @Test
    public void testGetAllRolesNames() throws Exception {
        Mockito.when(realm.getUserStoreManager()).thenReturn(userStoreManagerWithAb);
        HybridRoleManager hybridRoleManager = mock(HybridRoleManager.class);
        Object cc = userStoreManagerWithAb;
        Field f1 = cc.getClass().getSuperclass().getDeclaredField("hybridRoleManager");
        f1.setAccessible(true);
        f1.set(cc, hybridRoleManager);
        String[] test = {"role3x", "role4x"};
        Mockito.when(hybridRoleManager.getHybridRoles("role")).thenReturn(test);
        Mockito.when(((AbstractUserStoreManager) userStoreManagerWithAb)
                .getSharedRoleNames("role", 10)).thenReturn(new String[]{"role1", "role2"});
        Mockito.when(((AbstractUserStoreManager) userStoreManagerWithAb)
                .getRoleNames("role", 10,
                        true, true, true))
                .thenReturn(null);
        FlaggedName[] roleList = userRealmProxy.getAllRolesNames("role", 10);
        Assert.assertEquals(roleList.length, 3);
    }

    @Test
    public void testGetUserRealmInfo() throws Exception {
        Mockito.when(realm.getAuthorizationManager()).thenReturn(authorizationManager);
        Mockito.when(realm.getClaimManager()).thenReturn(claimManager);
        Mockito.when(authorizationManager.isUserAuthorized("admin",
                "/permission/admin/manage/identity", CarbonConstants.UI_PERMISSION_ACTION)).thenReturn(true);
        Mockito.when(realm.getRealmConfiguration()).thenReturn(this.getSampleRelaimConfiguration());
        Mockito.when(realm.getUserStoreManager()).thenReturn(userStoreManagerWithAb);
        ClaimMapping claimMapping = new ClaimMapping();
        claimMapping.setMappedAttribute("test1");
        claimMapping.setMappedAttribute("test2");
        claimMapping.setMappedAttribute("test3");
        Claim claim = new Claim();
        claim.setClaimUri("testURI");
        claim.setValue("testClaim");
        claimMapping.setClaim(claim);

        Mockito.when(claimManager.getAllClaimMappings(UserCoreConstants.DEFAULT_CARBON_DIALECT))
                .thenReturn(new ClaimMapping[]{claimMapping});
        Mockito.when(userStoreManagerWithAb.getRealmConfiguration()).thenReturn(this.getSampleRelaimConfiguration());
        Mockito.when(userStoreManagerWithAb.isBulkImportSupported()).thenReturn(false);
        UserRealmInfo realmInfo = userRealmProxy.getUserRealmInfo();
        Assert.assertEquals(realmInfo.getEveryOneRole(), "everyone");
        Assert.assertEquals(realmInfo.getAdminRole(), "admin");
    }

    @Test
    public void testAddUser() throws Exception {
        Mockito.when(realm.getRealmConfiguration()).thenReturn(this.getSampleRelaimConfiguration());
        Mockito.when(realm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(realm.getAuthorizationManager()).thenReturn(authorizationManager);
        Mockito.when(authorizationManager.isRoleAuthorized("role1", PERMISSION,
                UserMgtConstants.EXECUTE_ACTION)).thenReturn(true);
        userRealmProxy.addUser("testUser",
                "password", new String[]{"role1", "role2"},
                getSampleClaims(), "default");
        verify(realm.getUserStoreManager()).addUser(anyString(),
                anyString(), eq(new String[]{"role1", "role2"}),
                anyMap(), anyString(), eq(false));
    }

    @Test(expectedExceptions = UserStoreException.class)
    public void testChangePasswordSameUserNoOldPasword() throws UserStoreException {
        try {
            userRealmProxy.changePassword("admin", "newPassword");
        } catch (UserAdminException e) {
            assertEquals(e.getMessage(), "An attempt to change password with out providing old password");
            throw new UserStoreException(e);
        }
    }

    @Test
    public void testChangePassword() throws Exception {
        Mockito.when(realm.getRealmConfiguration()).thenReturn(this.getSampleRelaimConfiguration());
        Mockito.when(realm.getAuthorizationManager()).thenReturn(authorizationManager);
        Mockito.when(authorizationManager.
                isUserAuthorized("admin2", PERMISSION, UserMgtConstants.EXECUTE_ACTION))
                .thenReturn(true);
        Mockito.when(realm.getUserStoreManager()).thenReturn(userStoreManager);
        userRealmProxy.changePassword("admin2", "newPassword");
        verify(userStoreManager).updateCredentialByAdmin(anyString(), anyString());
    }

    @Test
    public void testDeleteUser() throws Exception {
        Registry registry = mock(Registry.class);
        Mockito.when(realm.getRealmConfiguration()).thenReturn(this.getSampleRelaimConfiguration());
        Mockito.when(realm.getAuthorizationManager()).thenReturn(authorizationManager);
        Mockito.when(realm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(authorizationManager.
                isUserAuthorized(anyString(), eq(PERMISSION),
                        eq(UserMgtConstants.EXECUTE_ACTION))).thenReturn(true);
        userRealmProxy.deleteUser("testUser", registry);
        verify(userStoreManager).deleteUser(anyString());
    }

    @Test(dataProvider = "userListCount")
    public void testGetRolesOfUser(int listCount, int assertCount) throws Exception {
        Mockito.when(realm.getUserStoreManager()).thenReturn(userStoreManagerWithAb);
        Mockito.when(((AbstractUserStoreManager) userStoreManagerWithAb)
                .getRoleListOfUser("admin2"))
                .thenReturn(new String[]{"PRIMARY/role1", "PRIMARY/role2"});
        Object cc = userStoreManagerWithAb;
        HybridRoleManager hybridRoleManager = mock(HybridRoleManager.class);
        Field f1 = cc.getClass().getSuperclass().getDeclaredField("hybridRoleManager");
        f1.setAccessible(true);
        f1.set(cc, hybridRoleManager);
        String[] test = {"PRIMARY/role3", "PRIMARY/role4"};
        String[] test2 = {"role3", "role4"};
        Mockito.when(((AbstractUserStoreManager) userStoreManagerWithAb)
                .getRoleNames("role", listCount,
                        true, true, true)).thenReturn(null);
        Mockito.when(hybridRoleManager.getHybridRoles("role")).thenReturn(test);
        FlaggedName[] flaggedNames = userRealmProxy
                .getRolesOfUser("admin2", "role", listCount);
        Assert.assertEquals(flaggedNames.length, assertCount);
    }

    @Test
    public void testUpdateUsersOfRole() throws Exception {
        Mockito.when(realm.getRealmConfiguration()).thenReturn(this.getSampleRelaimConfiguration());
        Mockito.when(realm.getAuthorizationManager()).thenReturn(authorizationManager);
        Mockito.when(realm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(authorizationManager.
                isRoleAuthorized(anyString(),
                        eq(PERMISSION), eq(UserMgtConstants.EXECUTE_ACTION))).thenReturn(true);
        userRealmProxy.updateRoleName("testRole", "testNewRole");
        verify(userStoreManager).updateRoleName("testRole", "testNewRole");
    }

    private static void startTenantFlow(String tenantDomain) {
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target").toString();
        System.setProperty("carbon.home", carbonHome);
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin");
        PrivilegedCarbonContext.getThreadLocalCarbonContext();
    }

    private RealmConfiguration getSampleRelaimConfiguration() {
        RealmConfiguration realmConfig = new RealmConfiguration();
        realmConfig.setAddAdmin("aldmin");
        realmConfig.setAdminPassword("admin");
        realmConfig.setAdminRoleName("admin");
        realmConfig.setEveryOneRoleName("everyone");
        realmConfig.setPrimary(true);
        realmConfig.setAdminUserName("admin");
        Map<String, String> userStoreProperties = new HashMap<>();
        userStoreProperties.put("WriteGroups", "true");
        realmConfig.setUserStoreProperties(userStoreProperties);
        return realmConfig;
    }

    private ClaimValue[] getSampleClaims() {
        Claim claim = new Claim();
        claim.setClaimUri("testURI");
        claim.setValue("testClaim");
        ClaimValue claimValue = new ClaimValue();
        claimValue.setClaimURI("testURI");
        claimValue.setValue("testClaim");
        return new ClaimValue[]{claimValue};
    }
}
