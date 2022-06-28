/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.framework.common.testng.realm;

import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;

/**
 * Simple In Memory Authorization Manager for mocking.
 */
public class MockAuthorizationManager implements AuthorizationManager {

    @Override
    public boolean isUserAuthorized(String s, String s1, String s2) throws UserStoreException {
        return true;
    }

    @Override
    public boolean isRoleAuthorized(String s, String s1, String s2) throws UserStoreException {
        return false;
    }

    @Override
    public String[] getExplicitlyAllowedUsersForResource(String s, String s1) throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] getAllowedRolesForResource(String s, String s1) throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] getDeniedRolesForResource(String s, String s1) throws UserStoreException {
        return new String[0];
    }

    @Override
    public String[] getExplicitlyDeniedUsersForResource(String s, String s1) throws UserStoreException {
        return new String[0];
    }

    @Override
    public void authorizeUser(String s, String s1, String s2) throws UserStoreException {

    }

    @Override
    public void authorizeRole(String s, String s1, String s2) throws UserStoreException {

    }

    @Override
    public void denyUser(String s, String s1, String s2) throws UserStoreException {

    }

    @Override
    public void denyRole(String s, String s1, String s2) throws UserStoreException {

    }

    @Override
    public void clearUserAuthorization(String s, String s1, String s2) throws UserStoreException {

    }

    @Override
    public void clearUserAuthorization(String s) throws UserStoreException {

    }

    @Override
    public void clearRoleAuthorization(String s, String s1, String s2) throws UserStoreException {

    }

    @Override
    public void clearRoleActionOnAllResources(String s, String s1) throws UserStoreException {

    }

    @Override
    public void clearRoleAuthorization(String s) throws UserStoreException {

    }

    @Override
    public void clearResourceAuthorizations(String s) throws UserStoreException {

    }

    @Override
    public String[] getAllowedUIResourcesForUser(String s, String s1) throws UserStoreException {
        return new String[0];
    }

    @Override
    public int getTenantId() throws UserStoreException {
        return 0;
    }

    @Override
    public void resetPermissionOnUpdateRole(String s, String s1) throws UserStoreException {

    }

    @Override
    public void refreshAllowedRolesForResource(String s) throws org.wso2.carbon.user.api.UserStoreException {

    }

    @Override
    public String[] normalizeRoles(String[] strings) {
        return new String[0];
    }
}
