/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common;

import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;

import java.util.ArrayList;
import java.util.List;

public class ApplicationAuthenticatorService {

    private static volatile ApplicationAuthenticatorService instance;

    private List<LocalAuthenticatorConfig> localAuthenticators = new ArrayList<LocalAuthenticatorConfig>();
    private List<FederatedAuthenticatorConfig> federatedAuthenticators = new ArrayList<FederatedAuthenticatorConfig>();
    private List<RequestPathAuthenticatorConfig> requestPathAuthenticators = new ArrayList<RequestPathAuthenticatorConfig>();

    public static ApplicationAuthenticatorService getInstance() {
        if (instance == null) {
            synchronized (ApplicationAuthenticatorService.class) {
                if (instance == null) {
                    instance = new ApplicationAuthenticatorService();
                }
            }
        }
        return instance;
    }

    public List<LocalAuthenticatorConfig> getLocalAuthenticators() {
        return this.localAuthenticators;
    }

    public List<FederatedAuthenticatorConfig> getFederatedAuthenticators() {
        return this.federatedAuthenticators;
    }

    public List<RequestPathAuthenticatorConfig> getRequestPathAuthenticators() {
        return this.requestPathAuthenticators;
    }

    public LocalAuthenticatorConfig getLocalAuthenticatorByName(String name) {
        for (LocalAuthenticatorConfig localAuthenticator : localAuthenticators) {
            if (localAuthenticator.getName().equals(name)) {
                return localAuthenticator;
            }
        }
        return null;
    }

    public FederatedAuthenticatorConfig getFederatedAuthenticatorByName(String name) {
        for (FederatedAuthenticatorConfig federatedAuthenticator : federatedAuthenticators) {
            if (federatedAuthenticator.getName().equals(name)) {
                return federatedAuthenticator;
            }
        }
        return null;
    }

    public RequestPathAuthenticatorConfig getRequestPathAuthenticatorByName(String name) {
        for (RequestPathAuthenticatorConfig reqPathAuthenticator : requestPathAuthenticators) {
            if (reqPathAuthenticator.getName().equals(name)) {
                return reqPathAuthenticator;
            }
        }
        return null;
    }

    public void addLocalAuthenticator(LocalAuthenticatorConfig authenticator) {
        if (authenticator != null) {
            localAuthenticators.add(authenticator);
        }
    }

    public void removeLocalAuthenticator(LocalAuthenticatorConfig authenticator) {
        if (authenticator != null) {
            localAuthenticators.remove(authenticator);
        }
    }

    public void addFederatedAuthenticator(FederatedAuthenticatorConfig authenticator) {
        if (authenticator != null) {
            federatedAuthenticators.add(authenticator);
        }
    }

    public void removeFederatedAuthenticator(FederatedAuthenticatorConfig authenticator) {
        if (authenticator != null) {
            federatedAuthenticators.remove(authenticator);
        }
    }

    public void addRequestPathAuthenticator(RequestPathAuthenticatorConfig authenticator) {
        if (authenticator != null) {
            requestPathAuthenticators.add(authenticator);
        }
    }

    public void removeRequestPathAuthenticator(RequestPathAuthenticatorConfig authenticator) {
        if (authenticator != null) {
            requestPathAuthenticators.remove(authenticator);
        }
    }
}
