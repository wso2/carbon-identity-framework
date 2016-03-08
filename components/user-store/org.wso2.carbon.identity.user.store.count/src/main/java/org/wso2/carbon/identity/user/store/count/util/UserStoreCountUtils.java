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
package org.wso2.carbon.identity.user.store.count.util;

import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.identity.user.store.count.internal.UserStoreCountDSComponent;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.Map;

public class UserStoreCountUtils {

    public static Map<String, RealmConfiguration> getUserStoreList() throws UserStoreCounterException {
        String domain;
        RealmConfiguration realmConfiguration;
        Map<String, RealmConfiguration> userStoreList = null;
        try {
            realmConfiguration = UserStoreCountDSComponent.getRealmService().getBootstrapRealm().getRealmConfiguration();
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
            throw new UserStoreCounterException("Error while listing user stores for metrics", e);
        }

        return userStoreList;
    }
}
