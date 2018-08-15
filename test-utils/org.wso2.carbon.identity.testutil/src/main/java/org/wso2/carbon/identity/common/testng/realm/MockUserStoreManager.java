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

import org.wso2.carbon.user.api.ClaimManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.tenant.Tenant;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simple mocked  UserStore Manager for testing.
 */
public class MockUserStoreManager implements UserStoreManager {

    private Map<String, UserStoreManager> secondaryUserStoreManagerMap = new HashMap();
    private RealmConfiguration inMemoryRealmConfiguration = null;
    private Map<String, Set<String>> usersOfRole = new HashMap<>();
    private Map<String, Set<String>> userRoleMap = new HashMap<>();

    @Override
    public boolean authenticate(String s, Object o) throws UserStoreException {
        return false;
    }

    @Override
    public String[] listUsers(String s, int i) throws UserStoreException {
        return new String[0];
    }

    @Override
    public boolean isExistingUser(String s) throws UserStoreException {
        return false;
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
    public String[] getRoleNames() throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] getRoleNames(boolean b) throws UserStoreException {
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
    public String[] getUserListOfRole(String s) throws UserStoreException {
        return new String[0];
    }

    @Override
    public String getUserClaimValue(String s, String s1, String s2) throws UserStoreException {
        return null;
    }

    @Override
    public Map<String, String> getUserClaimValues(String s, String[] strings, String s1) throws UserStoreException {
        return null;
    }

    @Override
    public Claim[] getUserClaimValues(String s, String s1) throws UserStoreException {
        return new Claim[0];
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
    public void addUser(String s, Object o, String[] strings, Map<String, String> map, String s1, boolean b)
            throws UserStoreException {

    }

    @Override
    public void updateCredential(String s, Object o, Object o1) throws UserStoreException {

    }

    @Override
    public void updateCredentialByAdmin(String s, Object o) throws UserStoreException {

    }

    @Override
    public void deleteUser(String s) throws UserStoreException {

    }

    @Override
    public void addRole(String role, String[] users, Permission[] permissions, boolean b)
            throws org.wso2.carbon.user.api.UserStoreException {
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
    public void deleteRole(String s) throws UserStoreException {

    }

    @Override
    public void updateUserListOfRole(String s, String[] strings, String[] strings1) throws UserStoreException {

    }

    @Override
    public void updateRoleListOfUser(String s, String[] strings, String[] strings1) throws UserStoreException {

    }

    @Override
    public void setUserClaimValue(String s, String s1, String s2, String s3) throws UserStoreException {

    }

    @Override
    public void setUserClaimValues(String s, Map<String, String> map, String s1) throws UserStoreException {

    }

    @Override
    public void deleteUserClaimValue(String s, String s1, String s2) throws UserStoreException {

    }

    @Override
    public void deleteUserClaimValues(String s, String[] strings, String s1) throws UserStoreException {

    }

    @Override
    public String[] getHybridRoles() throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] getAllSecondaryRoles() throws UserStoreException {
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
    public void updateRoleName(String s, String s1) throws UserStoreException {

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
    public ClaimManager getClaimManager() throws org.wso2.carbon.user.api.UserStoreException {
        return null;
    }

    @Override
    public boolean isSCIMEnabled() throws org.wso2.carbon.user.api.UserStoreException {
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
    public String[] getUserList(String s, String s1, String s2) throws UserStoreException {
        return new String[0];
    }

    @Override
    public UserStoreManager getSecondaryUserStoreManager() {
        return null;
    }

    @Override
    public void setSecondaryUserStoreManager(UserStoreManager userStoreManager) {

    }

    @Override
    public UserStoreManager getSecondaryUserStoreManager(String userDomain) {
        return userDomain == null ?
                null :
                (UserStoreManager) this.secondaryUserStoreManagerMap.get(userDomain.toUpperCase());
    }

    @Override
    public void addSecondaryUserStoreManager(String userDomain, UserStoreManager userStoreManager) {
        if (userDomain != null) {
            this.secondaryUserStoreManagerMap.put(userDomain.toUpperCase(), userStoreManager);
        }
    }

    @Override
    public RealmConfiguration getRealmConfiguration() {
        return this.inMemoryRealmConfiguration;
    }

    public void setRealmConfiguration(RealmConfiguration realmConfiguration) {
        this.inMemoryRealmConfiguration = realmConfiguration;
    }
}
