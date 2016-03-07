/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.user.store.count;

import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.mgt.internal.UserMgtDSComponent;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreMetricsException;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractUserStoreCountRetriever implements UserStoreCountRetriever {

    private Map<String, RealmConfiguration> userStoreList = new HashMap<>();
    public int searchTime = UserCoreConstants.MAX_SEARCH_TIME;


    private Map<String, RealmConfiguration> getUserStoreList() throws UserStoreMetricsException {
        String domain;
        RealmConfiguration realmConfiguration;
        try {
            realmConfiguration = UserMgtDSComponent.getRealmService().getBootstrapRealm().getRealmConfiguration();
            domain = IdentityUtil.getPrimaryDomainName();
            userStoreList.put(domain, realmConfiguration);

            while (realmConfiguration != null) {
                realmConfiguration = realmConfiguration.getSecondaryRealmConfig();
                if (realmConfiguration != null) {
                    domain = realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                    userStoreList.put(domain, realmConfiguration);
                } else {
                    break;
                }


            }

        } catch (UserStoreException e) {
            throw new UserStoreMetricsException("Error while listing user stores for metrics",e);
        }

        return userStoreList;
    }

    public final Long countUsers(String filter) throws UserStoreMetricsException {
        return Long.valueOf(0);

    }

    @Override
    public final Long countRoles(String filter) throws UserStoreMetricsException {
        return null;
    }

    @Override
    public final Long countClaim(String claimURI, String valueFilter) throws UserStoreMetricsException {
        return null;
    }

    @Override
    public final Long countClaims(Map<String, String> claimSetToFilter) throws UserStoreMetricsException {
        return null;
    }

    @Override
    public abstract Long countUsersInDomain(String filter, String domain) throws UserStoreMetricsException;

    @Override
    public abstract Long countRolesInDomain(String filter, String domain) throws UserStoreMetricsException;

    @Override
    public abstract Long countClaimInDomain(String claimURI, String valueFilter, String domain) throws UserStoreMetricsException;

    @Override
    public abstract Long countClaimsInDomain(Map<String, String> claimSetToFilter, String domain) throws UserStoreMetricsException;
}
