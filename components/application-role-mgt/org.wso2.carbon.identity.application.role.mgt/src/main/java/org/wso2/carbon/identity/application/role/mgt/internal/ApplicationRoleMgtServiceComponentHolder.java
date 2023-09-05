/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.role.mgt.internal;

import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Service component holder class for role management service.
 */
public class ApplicationRoleMgtServiceComponentHolder {

    private static final ApplicationRoleMgtServiceComponentHolder instance =
            new ApplicationRoleMgtServiceComponentHolder();
    private RealmService realmService;
    private IdpManager identityProviderManager;

    public static ApplicationRoleMgtServiceComponentHolder getInstance() {

        return instance;
    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    /**
     * Get IdentityProviderManager osgi service.
     *
     * @return IdentityProviderManager
     */
    public IdpManager getIdentityProviderManager() {

        return identityProviderManager;
    }

    /**
     * Set IdentityProviderManager osgi service.
     *
     * @param idpManager IdentityProviderManager.
     */
    public void setIdentityProviderManager(IdpManager idpManager) {

        this.identityProviderManager = idpManager;
    }
}
