/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.mgt.rule;

import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Builds a realm whose tenant has a primary user store and a chain of secondary ones.
 */
final class UserStoreMocks {

    private UserStoreMocks() {

    }

    /**
     * A realm service whose tenant user stores carry the given domains.
     *
     * @param primaryDomain      Domain of the primary store.
     * @param secondaryDomains   Domains of the secondary stores, in chain order.
     * @param disabledSecondary  A further secondary store that is disabled, or null for none.
     * @return The realm service.
     * @throws Exception Never, for a mock.
     */
    static RealmService realmWithUserStores(String primaryDomain, String[] secondaryDomains,
                                            String disabledSecondary) throws Exception {

        UserStoreManager primary = store(primaryDomain, false);
        UserStoreManager previous = primary;
        for (String domain : secondaryDomains) {
            UserStoreManager secondary = store(domain, false);
            when(previous.getSecondaryUserStoreManager()).thenReturn(secondary);
            previous = secondary;
        }
        if (disabledSecondary != null) {
            UserStoreManager disabled = store(disabledSecondary, true);
            when(previous.getSecondaryUserStoreManager()).thenReturn(disabled);
        }

        UserRealm userRealm = mock(UserRealm.class);
        when(userRealm.getUserStoreManager()).thenReturn(primary);
        RealmService realmService = mock(RealmService.class);
        when(realmService.getTenantUserRealm(anyInt())).thenReturn(userRealm);
        return realmService;
    }

    private static UserStoreManager store(String domain, boolean disabled) {

        RealmConfiguration configuration = mock(RealmConfiguration.class);
        when(configuration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME))
                .thenReturn(domain);
        when(configuration.getUserStoreProperty(UserCoreConstants.RealmConfig.USER_STORE_DISABLED))
                .thenReturn(String.valueOf(disabled));
        UserStoreManager store = mock(UserStoreManager.class);
        when(store.getRealmConfiguration()).thenReturn(configuration);
        return store;
    }
}
