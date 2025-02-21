/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.temp;

import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to store the configuration details of the registration step executors.
 */
public class RegistrationStepExecutorConfig {

    private String id;
    private String name;
    private IdentityProvider identityProvider;
    private boolean isOptional = true;
    private Map<String, String> authenticatorProperties = new HashMap<>();
    private ClaimMapping[] requestedClaims;

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public IdentityProvider getIdentityProvider() {

        return identityProvider;
    }

    public void setIdentityProvider(IdentityProvider identityProvider) {

        this.identityProvider = identityProvider;
    }

    public Map<String, String> getAuthenticatorProperties() {

        return authenticatorProperties;
    }

    public void setAuthenticatorProperties(Map<String, String> authenticatorProperties) {

        this.authenticatorProperties = authenticatorProperties;
    }

    public ClaimMapping[] getRequestedClaims() {

        return requestedClaims;
    }

    public void setRequestedClaims(ClaimMapping[] requestedClaims) {

        this.requestedClaims = requestedClaims;
    }

    public boolean isOptional() {

        return isOptional;
    }

    public void setOptional(boolean optional) {

        isOptional = optional;
    }
}
