/*
 * Copyright (c) 2021. WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.framework.common.testng.realm;

import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.hybrid.HybridRoleManager;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple mocked hybrid role manager for testing.
 */
public class MockHybridRoleManager extends HybridRoleManager {

    private Map<String, Set<String>> userRoleMap = new HashMap<>();

    public MockHybridRoleManager(DataSource dataSource, int tenantId, RealmConfiguration realmConfig, UserRealm realm)
            throws UserStoreException {

        super(dataSource, tenantId, realmConfig, realm);
    }

    public void addHybridRole(String roleName, String[] userList) throws UserStoreException {

        for (String user : userList) {
            userRoleMap.put(user, new HashSet<>(Arrays.asList(roleName)));
        }
    }

    protected void clearUserRolesCacheByTenant(int tenantID) {

    }

    public boolean isExistingRole(String roleName) throws UserStoreException {

        return false;
    }

    public String[] getHybridRoles(String filter) throws UserStoreException {

        return new String[0];
    }

    public String[] getUserListOfHybridRole(String roleName) throws UserStoreException {

        return new String[0];
    }

    public void updateUserListOfHybridRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {

    }

    public void updateGroupListOfHybridRole(String roleName, String[] deletedGroups, String[] newGroups)
            throws UserStoreException {

    }

    public String[] getGroupListOfHybridRole(String roleName) throws UserStoreException {

        return new String[0];
    }

    public String[] getHybridRoleListOfUser(String userName, String filter) throws UserStoreException {

        return userRoleMap.get(userName).toArray(new String[0]);
    }

    public Map<String, List<String>> getHybridRoleListOfUsers(List<String> userNames, String domainName) throws
            UserStoreException {

        return new HashMap<>();
    }

    public Map<String, List<String>> getHybridRoleListOfGroups(List<String> groupNames, String domainName)
            throws UserStoreException {

        return new HashMap<>();
    }

    public void updateHybridRoleListOfUser(String user, String[] deletedRoles, String[] addRoles)
            throws UserStoreException {

        Set<String> roles = userRoleMap.get(user);
        Arrays.asList(deletedRoles).forEach(roles::remove);
        roles.addAll(Arrays.asList(addRoles));
    }

    public void deleteHybridRole(String roleName) throws UserStoreException {

    }

    public void updateHybridRoleName(String roleName, String newRoleName) throws UserStoreException {

    }

    public Long countHybridRoles(String filter) throws UserStoreException {

        return (long) -1;
    }

    public boolean isUserInRole(String userName, String roleName) throws UserStoreException {

        return userRoleMap.get(userName).contains(roleName);
    }

    public void deleteUser(String userName) throws UserStoreException {

    }

    protected void initUserRolesCache() {

    }
}
