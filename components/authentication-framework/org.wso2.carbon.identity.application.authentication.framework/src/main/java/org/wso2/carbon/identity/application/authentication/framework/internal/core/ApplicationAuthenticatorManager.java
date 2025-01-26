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
 * This class is used to manage the ApplicationAuthenticator instances.
 */
public class ApplicationAuthenticatorManager {

    private static final ApplicationAuthenticatorManager instance = new ApplicationAuthenticatorManager();
    private final List<ApplicationAuthenticator> systemDefinedAuthenticators = new ArrayList<>();

    public static ApplicationAuthenticatorManager getInstance() {

        return instance;
    }

    /**
     * Add a new system authenticator.
     *
     * @param authenticator Authenticator to be added.
     */
    public void addSystemDefinedAuthenticator(ApplicationAuthenticator authenticator) {

        systemDefinedAuthenticators.add(authenticator);
    }

    /**
     * Remove a system authenticator.
     *
     * @param authenticator Authenticator to be removed.
     */
    public void removeSystemDefinedAuthenticator(ApplicationAuthenticator authenticator) {

        systemDefinedAuthenticators.remove(authenticator);
    }

    /**
     * Get all the system defined authenticators.
     *
     * @return List of system defined authenticators.
     */
    public List<ApplicationAuthenticator> getSystemDefinedAuthenticators() {

        return systemDefinedAuthenticators;
    }

    /**
     * Get the ApplicationAuthenticator for the given system defined authenticator configuration name.
     *
     * @param authenticatorName Authenticator name.
     * @return  ApplicationAuthenticator instance.
     */
    public ApplicationAuthenticator getSystemDefinedAuthenticatorByName(String authenticatorName) {

        for (ApplicationAuthenticator authenticator : systemDefinedAuthenticators) {
            if (authenticator.getName().equals(authenticatorName)) {
                return authenticator;
            }
        }
        return null;
    }

    /**
     * Get all the authenticators for the given tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @return List of authenticators.
     */
    public List<ApplicationAuthenticator> getAllAuthenticators(String tenantDomain) {

        List<ApplicationAuthenticator> allAuthenticators = new ArrayList<>(systemDefinedAuthenticators);

        if (FrameworkServiceDataHolder.getInstance().getAuthenticatorAdapterService() == null) {
            return allAuthenticators;
        }

        try {
            for (LocalAuthenticatorConfig localConfig : ApplicationAuthenticatorService.getInstance()
                    .getAllUserDefinedLocalAuthenticators(tenantDomain)) {
                allAuthenticators.add(FrameworkServiceDataHolder.getInstance().getAuthenticatorAdapterService()
                        .getLocalAuthenticatorAdapter(localConfig));
            }

            FederatedAuthenticatorConfig[] fedConfig = IdentityProviderManager.getInstance()
                    .getAllFederatedAuthenticators(tenantDomain);
            for (FederatedAuthenticatorConfig fedAuth : fedConfig) {
                allAuthenticators.add(FrameworkServiceDataHolder.getInstance().getAuthenticatorAdapterService()
                        .getFederatedAuthenticatorAdapter(fedAuth));
            }

            return allAuthenticators;
        } catch (Exception e) {
            throw new RuntimeException("Error while getting all application authenticators.", e);
        }
    }

    /**
     * Get the ApplicationAuthenticator for the given authenticator name.
     *
     * @param authenticatorName Authenticator name.
     * @param tenantDomain      Tenant domain.
     * @return  ApplicationAuthenticator instance.
     */
    public ApplicationAuthenticator getApplicationAuthenticatorByName(String authenticatorName, String tenantDomain) {

        // Check whether the authenticator is in the system defined authenticator.
        for (ApplicationAuthenticator authenticator : systemDefinedAuthenticators) {
            if (authenticator.getName().equals(authenticatorName)) {
                return authenticator;
            }
        }

        if (FrameworkServiceDataHolder.getInstance().getAuthenticatorAdapterService() == null) {
            return null;
        }

        // Check whether the authenticator config is the user defined local authenticator config, if so resolve it.
        try {
            LocalAuthenticatorConfig localConfig = ApplicationAuthenticatorService.getInstance()
                    .getUserDefinedLocalAuthenticator(tenantDomain, authenticatorName);
            if (localConfig != null) {
                return FrameworkServiceDataHolder.getInstance().getAuthenticatorAdapterService()
                        .getLocalAuthenticatorAdapter(localConfig);
            }

            // Check whether the authenticator config is the user defined fed authenticator config, if so resolve it.
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
