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
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.common.model.sp.RequestValidationConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.RequestValidatorConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServiceProviderConfigStore {
    private static ServiceProviderConfigStore serviceProviderConfigStore = new ServiceProviderConfigStore();

    private Map<String, String> spUniqueKeyMap = new HashMap<>();
    private Map<String, ServiceProviderConfig> spEntityMap = new HashMap<>();

    private ServiceProviderConfigStore() {

    }

    public static ServiceProviderConfigStore getInstance() {
        return ServiceProviderConfigStore.serviceProviderConfigStore;
    }

    public void addServiceProvider(ServiceProviderConfig serviceProvider) {
        if (serviceProvider != null) {
            RequestValidationConfig requestValidationConfig = serviceProvider.getRequestValidationConfig();
            for (RequestValidatorConfig requestValidatorConfig : requestValidationConfig.getRequestValidatorConfigs()) {
                String uniquePropertyName = requestValidatorConfig.getUniquePropertyName();
                String uniqueKey = requestValidatorConfig.getProperties().getProperty(uniquePropertyName);
                spUniqueKeyMap.put(uniqueKey, serviceProvider.getName());
                spEntityMap.put(serviceProvider.getName(), serviceProvider);
            }
        }
    }

    public ServiceProviderConfig getServiceProvider(String uniqueKey) {
        ServiceProviderConfig serviceProvider = null;
        String spName = spUniqueKeyMap.get(uniqueKey);
        if (spName != null) {
            serviceProvider = spEntityMap.get(spName);
            if (serviceProvider != null) {
                buildServiceProvider(serviceProvider);
            }
        }
        return serviceProvider;
    }

    public void removeServiceProvider(String serviceProviderName) {
        if (serviceProviderName != null) {
            Iterator entries = spUniqueKeyMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry thisEntry = (Map.Entry) entries.next();
                Object value = thisEntry.getValue();
                if (value.equals(serviceProviderName)) {
                    entries.remove();
                }
            }
            spEntityMap.remove(serviceProviderName);
        }
    }

    private void buildServiceProvider(ServiceProviderConfig serviceProviderConfig) {

        AuthenticationConfig authenticationConfig = serviceProviderConfig.getAuthenticationConfig();
        List<AuthenticationStepConfig> authenticationStepConfigs = authenticationConfig.getAuthenticationStepConfigs();

        authenticationStepConfigs.forEach(authenticationStepConfig -> {
            List<IdentityProvider> identityProviders = authenticationStepConfig.getIdentityProviders();
            identityProviders.stream().filter(identityProvider -> identityProvider
                    .getIdentityProviderConfig()
                    == null)
                    .forEach(this::updateIdentityProvider);
        });
    }

    private void updateIdentityProvider(IdentityProvider identityProvider) {
        if (identityProvider.getIdentityProviderName() != null) {
            IdentityProviderConfig identityProviderConfig = IdentityProviderConfigStore.getInstance()
                    .getIdentityProvider(identityProvider.getIdentityProviderName());
            identityProvider.setIdentityProviderConfig(identityProviderConfig);
        }
    }
}
