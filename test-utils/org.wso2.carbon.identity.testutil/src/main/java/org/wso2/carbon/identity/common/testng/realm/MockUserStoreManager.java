/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.common.testng.realm;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.NotImplementedException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.RoleContext;
import org.wso2.carbon.user.core.hybrid.HybridRoleManager;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

/**
 * Simple mocked  UserStore Manager for testing.
 */
public class MockUserStoreManager extends AbstractUserStoreManager {

    private Map<String, UserStoreManager> secondaryUserStoreManagerMap = new HashMap<>();
    private Map<String, Set<String>> usersOfRole = new HashMap<>();
    private Map<String, Set<String>> userRoleMap = new HashMap<>();
    private static ThreadLocal<UserStoreModel> threadLocalUserStoreModel = new ThreadLocal<>();

    public MockUserStoreManager(DataSource dataSource) {
        try {
            Field userUniqueIDDomainResolverField
                    = this.getClass().getSuperclass().getDeclaredField("userUniqueIDDomainResolver");
            userUniqueIDDomainResolverField.setAccessible(true);
            userUniqueIDDomainResolverField.set(this, new MockUserUniqueIDDomainResolver(dataSource));
        } catch (Exception e) {

        }
    }

    @Override
    protected Map<String, String> getUserPropertyValues(String s, String[] strings, String s1) throws UserStoreException {

        return null;
    }

    @Override
    protected boolean doCheckExistingRole(String s) throws UserStoreException {

        return false;
    }

    @Override
    protected RoleContext createRoleContext(String s) throws UserStoreException {

        return null;
    }

    @Override
    protected boolean doCheckExistingUser(String s) throws UserStoreException {

        return false;
    }

    @Override
    protected String[] getUserListFromProperties(String s, String s1, String s2) throws UserStoreException {

        return new String[0];
    }

    @Override
    public boolean doAuthenticate(String s, Object o) throws UserStoreException {
        return false;
    }

    @Override
    protected void doAddUser(String s, Object o, String[] strings, Map<String, String> map, String s1, boolean b) throws UserStoreException {

    }

    @Override
    protected void doUpdateCredential(String s, Object o, Object o1) throws UserStoreException {

    }

    @Override
    protected void doUpdateCredentialByAdmin(String s, Object o) throws UserStoreException {

    }

    @Override
    protected void doDeleteUser(String s) throws UserStoreException {

    }

    @Override
    protected void doDeleteUserClaimValue(String s, String s1, String s2) throws UserStoreException {

    }

    @Override
    protected void doDeleteUserClaimValues(String s, String[] strings, String s1) throws UserStoreException {

    }

    @Override
    protected void doUpdateUserListOfRole(String s, String[] strings, String[] strings1) throws UserStoreException {

    }

    @Override
    protected void doUpdateRoleListOfUser(String s, String[] strings, String[] strings1) throws UserStoreException {

    }

    @Override
    protected String[] doGetExternalRoleListOfUser(String s, String s1) throws UserStoreException {

        return new String[0];
    }

    @Override
    protected String[] doGetSharedRoleListOfUser(String s, String s1, String s2) throws UserStoreException {

        return new String[0];
    }

    @Override
    protected void doAddRole(String s, String[] strings, boolean b) throws UserStoreException {

    }

    @Override
    protected void doDeleteRole(String s) throws UserStoreException {

    }

    @Override
    protected void doUpdateRoleName(String s, String s1) throws UserStoreException {

    }

    @Override
    public String[] doListUsers(String s, int i) throws UserStoreException {
        return new String[0];
    }

    @Override
    protected void doSetUserAttributesWithID(String userID, Map<String,
            String> processedClaimAttributes, String profileName) throws UserStoreException {
        UserStoreModel userStoreModel = threadLocalUserStoreModel.get();
        if(userStoreModel == null) {
            return;
        }
        Map<String, String> currentAttributes = userStoreModel.getClaimValues(userID);
        if(currentAttributes != null) {
            currentAttributes.putAll(processedClaimAttributes);
        }
    }

    @Override
    protected String[] doGetDisplayNamesForInternalRole(String[] strings) throws UserStoreException {

        return new String[0];
    }

    @Override
    public boolean isExistingUser(String s) throws UserStoreException {
        return true;
    }

    @Override
    public boolean isExistingRole(String s, boolean b) throws org.wso2.carbon.user.api.UserStoreException {
        return false;
    }

    @Override
    public boolean isExistingRole(String s) throws UserStoreException {
        return false;
    }

    @Override
    public boolean doCheckIsUserInRole(String s, String s1) throws UserStoreException {

        return false;
    }

    @Override
    public String[] doGetRoleNames(String filter, int maxItemLimit) throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] getProfileNames(String s) throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] getRoleListOfUser(String userName) throws UserStoreException {
        Set<String> roles = userRoleMap.get(userName);
        if (roles != null) {
            return roles.toArray(new String[0]);
        }
        return new String[0];
    }

    @Override
    public String[] doGetUserListOfRole(String roleName, String filter) throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] getAllProfileNames() throws UserStoreException {
        return new String[0];
    }

    @Override
    public boolean isReadOnly() throws UserStoreException {
        return false;
    }

    @Override
    public void addUser(String s, Object o, String[] strings, Map<String, String> map, String s1)
            throws UserStoreException {

    }

    @Override
    public void addRole(String role, String[] users, Permission[] permissions, boolean b)
            throws org.wso2.carbon.user.api.UserStoreException {
        if (UserCoreConstants.INTERNAL_DOMAIN.
                equalsIgnoreCase(UserCoreUtil.extractDomainFromName(role))
                || "Application".equalsIgnoreCase(UserCoreUtil.extractDomainFromName(role)) ||
                "Workflow".equalsIgnoreCase(UserCoreUtil.extractDomainFromName(role))) {
            doAddInternalRole(role, users, permissions);
            return;
        }
        if (users != null) {
            for (String user : users) {
                Set<String> roles = userRoleMap.computeIfAbsent(user, k -> new HashSet<>());
                roles.add(role);
            }
        }
    }

    @Override
    public void addRole(String s, String[] strings, Permission[] permissions)
            throws org.wso2.carbon.user.api.UserStoreException {
        addRole(s, strings, permissions, true);
    }

    @Override
    protected String[] doGetSharedRoleNames(String s, String s1, int i) throws UserStoreException {

        return new String[0];
    }

    @Override
    public Date getPasswordExpirationTime(String s) throws UserStoreException {
        return null;
    }

    @Override
    public int getUserId(String s) throws UserStoreException {
        return 0;
    }

    @Override
    public int getTenantId(String s) throws UserStoreException {
        return 0;
    }

    @Override
    public int getTenantId() throws UserStoreException {
        return 0;
    }

    @Override
    public Map<String, String> getProperties(org.wso2.carbon.user.api.Tenant tenant)
            throws org.wso2.carbon.user.api.UserStoreException {
        return null;
    }

    @Override
    public Map<String, String> getProperties(Tenant tenant) throws UserStoreException {
        return null;
    }

    @Override
    public boolean isMultipleProfilesAllowed() {
        return false;
    }

    @Override
    public void addRememberMe(String s, String s1) throws org.wso2.carbon.user.api.UserStoreException {

    }

    @Override
    public boolean isValidRememberMeToken(String s, String s1) throws org.wso2.carbon.user.api.UserStoreException {
        return false;
    }

    @Override
    public Properties getDefaultUserStoreProperties() {
        return null;
    }

    @Override
    public boolean isBulkImportSupported() throws UserStoreException {
        return false;
    }

    @Override
    public RealmConfiguration getRealmConfiguration() {
        return this.realmConfig;
    }

    public void setRealmConfiguration(RealmConfiguration realmConfiguration) {
        this.realmConfig = realmConfiguration;
    }

    public void setClaimManager(ClaimManager claimManager) {
        this.claimManager = claimManager;
    }

    public void setHybridRoleManager(HybridRoleManager hybridRoleManager) {
        this.hybridRoleManager = hybridRoleManager;
    }

    protected boolean doCheckExistingUserWithID(String userID) throws UserStoreException {

        return true;
    }

    protected String doGetUserNameFromUserIDWithID(String userID) throws UserStoreException {
        return StringUtils.EMPTY;
    }

    protected Map<String, String> doGetUserClaimValuesWithID(String userID, String[] claims, String domainName,
                                                             String profileName) throws UserStoreException {
        UserStoreModel userStoreModel = threadLocalUserStoreModel.get();
        if(userStoreModel == null) {
            return new HashMap<>();
        }
        Map<String, String> claimsMap = userStoreModel.getClaimValues(userID);
        return claimsMap;
    }

    protected String doGetUserIDFromUserNameWithID(String userName) throws UserStoreException {

        return StringUtils.EMPTY;
    }

    /*package private*/ static void bindUserStoreModel(UserStoreModel userStoreModel) {
        threadLocalUserStoreModel.set(userStoreModel);
    }

    /*package private*/ static void unbindUserStoreModel() {
        threadLocalUserStoreModel.remove();
    }
}
