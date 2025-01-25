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
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;

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
     * Get the ApplicationAuthenticator for the given user defined federated authenticator config.
     *
     * @param config    Federated Authenticator Config.
     * @return  FederatedApplicationAuthenticator instance.
     */
    public FederatedApplicationAuthenticator getFederatedAuthenticatorAdapter(FederatedAuthenticatorConfig config) {

        return FrameworkServiceDataHolder.getInstance().getAuthenticatorAdapterService()
                .getFederatedAuthenticatorAdapter(config);
    }

    /**
     * Get the ApplicationAuthenticator for the given user defined local authenticator config.
     *
     * @param config    Local Authenticator Config.
     * @return  LocalApplicationAuthenticator instance.
     */
    public LocalApplicationAuthenticator getLocalAuthenticatorAdapter(LocalAuthenticatorConfig config) {

        return FrameworkServiceDataHolder.getInstance().getAuthenticatorAdapterService()
                .getLocalAuthenticatorAdapter(config);
    }
}
