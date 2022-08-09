/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.model;

import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.OptimizedAuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.exception.SessionContextLoaderException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to keep the optimized attributes of authenticatedIdPData.
 */
public class OptimizedAuthenticatedIdPData implements Serializable {

    private String idpName;
    private List<OptimizedAuthenticatorConfig> optimizedAuthenticators;
    //private AuthenticatedUser user;
    private String authenticatedUserName;

    public OptimizedAuthenticatedIdPData(AuthenticatedIdPData authenticatedIdPData) {

        this.idpName = authenticatedIdPData.getIdpName();
        this.optimizedAuthenticators = getOptimizedAuthenticators(authenticatedIdPData.getAuthenticators());
        //this.user = authenticatedIdPData.getUser();
        this.authenticatedUserName = authenticatedIdPData.getUser().getUserName();
    }

    private List<OptimizedAuthenticatorConfig> getOptimizedAuthenticators(List<AuthenticatorConfig> authenticators) {

        List<OptimizedAuthenticatorConfig> optimizedAuthenticators = new ArrayList<>();
        authenticators.forEach(authenticatorConfig -> {
            optimizedAuthenticators.add(new OptimizedAuthenticatorConfig(authenticatorConfig));
        });
        return optimizedAuthenticators;
    }

    public AuthenticatedIdPData getAuthenticatedIdPData(String tenantDomain,
                                                        Map<String, AuthenticatedUser> authenticatedUsers)
            throws SessionContextLoaderException {

        AuthenticatedIdPData authenticatedIdPData = new AuthenticatedIdPData();
        authenticatedIdPData.setIdpName(this.idpName);
        List<AuthenticatorConfig> authenticators = new ArrayList<>();
        for (OptimizedAuthenticatorConfig optimizedAuthenticatorConfig : this.optimizedAuthenticators) {
            authenticators.add(optimizedAuthenticatorConfig.getAuthenticatorConfig(tenantDomain));
        }
        authenticatedIdPData.setAuthenticators(authenticators);
        //authenticatedIdPData.setUser(this.user);
        authenticatedIdPData.setUser(authenticatedUsers.get(this.authenticatedUserName));

        return authenticatedIdPData;
    }
}
