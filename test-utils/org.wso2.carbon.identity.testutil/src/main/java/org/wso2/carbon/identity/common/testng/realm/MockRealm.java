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

import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.claim.ClaimMapping;
import org.wso2.carbon.user.core.profile.ProfileConfiguration;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;

import java.util.Map;

/**
 * Simple user realm for testing.
 */
public class MockRealm implements UserRealm {

    private RealmConfiguration realmConfiguration;
    private AuthorizationManager authorizationManager = new MockAuthorizationManager();
    private UserStoreManager userStoreManager = new MockUserStoreManager();
    private int tenantId;
    private MockClaimManager claimManager;

    @Override
    public void init(RealmConfiguration realmConfiguration, Map<String, ClaimMapping> claimMappingMap,
            Map<String, ProfileConfiguration> map1, int tenantId) throws UserStoreException {
        this.realmConfiguration = realmConfiguration;
        this.tenantId = tenantId;
        ((MockUserStoreManager)this.userStoreManager).setRealmConfiguration(this.realmConfiguration);
        claimManager = new MockClaimManager(claimMappingMap);
    }

    @Override
    public void init(RealmConfiguration realmConfiguration, Map<String, Object> map, int tenantId)
            throws UserStoreException {
        this.realmConfiguration = realmConfiguration;
        this.tenantId = tenantId;
    }

    @Override
    public AuthorizationManager getAuthorizationManager() throws UserStoreException {
        return authorizationManager;
    }

    @Override
    public UserStoreManager getUserStoreManager() throws UserStoreException {
        return userStoreManager;
    }

    @Override
    public ClaimManager getClaimManager() throws UserStoreException {
        return claimManager;
    }

    @Override
    public ProfileConfigurationManager getProfileConfigurationManager() throws UserStoreException {
        return null;
    }

    @Override
    public void cleanUp() throws UserStoreException {

    }

    @Override
    public RealmConfiguration getRealmConfiguration() throws UserStoreException {
        return this.realmConfiguration;
    }
}
