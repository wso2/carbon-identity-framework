/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.store;


import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderConfig;

import java.util.HashMap;
import java.util.Map;

public class IdentityProviderConfigStore {
    private static IdentityProviderConfigStore identityProviderConfigStore = new IdentityProviderConfigStore();
    private Map<String, IdentityProviderConfig> idpEntityMap = new HashMap<>();


    private IdentityProviderConfigStore() {

    }

    public static IdentityProviderConfigStore getInstance() {
        return IdentityProviderConfigStore.identityProviderConfigStore;
    }

    public void addIdentityProvider(IdentityProviderConfig identityProvider) {
        if (identityProvider != null) {
            idpEntityMap.put(identityProvider.getName(), identityProvider);
        }
    }

    public IdentityProviderConfig getIdentityProvider(String idpName) {
        IdentityProviderConfig identityProvider = idpEntityMap.get(idpName);
        return identityProvider;
    }

    public void removeIdentityProvider(String identityProviderName) {
        if (identityProviderName != null) {
            idpEntityMap.remove(identityProviderName);
        }
    }

    public boolean validate(IdentityProviderConfig identityProvider) {
        return true;
    }
}
