/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.internal.core;

import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to manage the authenticator adapters for user defined authenticators.
 */
public class ApplicationAuthenticatorManager {

    private static final ApplicationAuthenticatorManager instance = new ApplicationAuthenticatorManager();
    private final List<ApplicationAuthenticator> systemDefinedAuthenticators = new ArrayList<>();

    public static ApplicationAuthenticatorManager getInstance() {

        return instance;
    }

    public void addSystemDefinedAuthenticator(ApplicationAuthenticator authenticator) {

        systemDefinedAuthenticators.add(authenticator);
    }

    public void removeSystemDefinedAuthenticator(ApplicationAuthenticator authenticator) {

        systemDefinedAuthenticators.remove(authenticator);
    }

    public List<ApplicationAuthenticator> getSystemDefinedAuthenticators() {

        return systemDefinedAuthenticators;
    }

    public ApplicationAuthenticator getSystemDefinedAuthenticatorByName(String authenticatorName) {

        for (ApplicationAuthenticator authenticator : systemDefinedAuthenticators) {
            if (authenticator.getName().equals(authenticatorName)) {
                return authenticator;
            }
        }
        return null;
    }

    public ApplicationAuthenticator getAppAuthenticatorByName(String authenticatorName, String tenantDomain) {

        for (ApplicationAuthenticator authenticator : systemDefinedAuthenticators) {
            if (authenticator.getName().equals(authenticatorName)) {
                return authenticator;
            }
        }

        try {
            LocalAuthenticatorConfig localConfig = ApplicationAuthenticatorService.getInstance()
                    .getUserDefinedLocalAuthenticator(tenantDomain, authenticatorName);
            if (localConfig != null) {
                return FrameworkServiceDataHolder.getInstance().getAuthenticatorAdapterService()
                        .getLocalAuthenticatorAdapter(localConfig);
            }

            FederatedAuthenticatorConfig[] fedConfig = IdentityProviderManager.getInstance()
                    .getAllFederatedAuthenticators(tenantDomain);
            for (FederatedAuthenticatorConfig fedAuth : fedConfig) {
                if (fedAuth.getName().equals(authenticatorName)) {
                    return FrameworkServiceDataHolder.getInstance().getAuthenticatorAdapterService()
                            .getFederatedAuthenticatorAdapter(fedAuth);
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error while getting the authenticator for the name: " + authenticatorName, e);
        }
    }
}
