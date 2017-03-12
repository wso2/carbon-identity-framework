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
package org.wso2.carbon.identity.gateway.authentication;


import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.common.model.idp.AuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.idp.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultSequence extends AbstractSequence {

    public DefaultSequence(AuthenticationContext authenticationContext) {
        super(authenticationContext);
    }

    public DefaultSequence() {
    }

    @Override
    public boolean canHandle(MessageContext messageContext) {
        return true;
    }

    @Override
    public IdentityProvider getIdentityProvider(int step, String identityProviderName)
            throws AuthenticationHandlerException {
        IdentityProvider identityProviderTmp = null;
        AuthenticationStepConfig authenticationStepConfig = getAuthenticationStepConfig(step);
        List<IdentityProvider> identityProviders = authenticationStepConfig.getIdentityProviders();
        for (IdentityProvider identityProvider : identityProviders) {
            if (identityProvider.getIdentityProviderName().equals(identityProviderName)) {
                identityProviderTmp = identityProvider;
            }
        }
        return identityProviderTmp;
    }

    @Override
    public List<IdentityProvider> getIdentityProviders(int step) throws AuthenticationHandlerException {
        AuthenticationStepConfig authenticationStepConfig = getAuthenticationStepConfig(step);
        List<IdentityProvider> identityProviders = authenticationStepConfig.getIdentityProviders();
        return identityProviders;
    }

    @Override
    public List<RequestPathAuthenticatorConfig> getRequestPathAuthenticatorConfig() {
        return null;
    }

    @Override
    public int getSteps() throws AuthenticationHandlerException {
        return authenticationContext.getServiceProvider().getAuthenticationConfig().getAuthenticationStepConfigs()
                .size();
    }

    @Override
    public boolean hasNext(int currentStep) throws AuthenticationHandlerException {
        ServiceProviderConfig serviceProvider = authenticationContext.getServiceProvider();
        AuthenticationConfig authenticationConfig = serviceProvider.getAuthenticationConfig();
        List<AuthenticationStepConfig> authenticationStepConfigs = authenticationConfig.getAuthenticationStepConfigs();
        if (authenticationStepConfigs.size() >= (currentStep + 1)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isMultiOption(int step) throws AuthenticationHandlerException {
        AuthenticationStepConfig authenticationStepConfig = getAuthenticationStepConfig(step);
        List<IdentityProvider> identityProviders = authenticationStepConfig.getIdentityProviders();
        if (identityProviders.size() > 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isRequestPathAuthenticatorsAvailable() {
        return false;
    }

    @Override
    public boolean isStepAuthenticatorAvailable() throws AuthenticationHandlerException {
        ServiceProviderConfig serviceProvider = authenticationContext.getServiceProvider();
        AuthenticationConfig authenticationConfig = serviceProvider.getAuthenticationConfig();
        if (authenticationConfig.getAuthenticationStepConfigs() != null && authenticationConfig
                                                                                   .getAuthenticationStepConfigs()
                                                                                   .size() > 0) {
            return true;
        }
        return false;
    }

    public AuthenticationStepConfig getAuthenticationStepConfig(int step)   {
        AtomicReference<AuthenticationStepConfig> authenticationStepConfig = new
                AtomicReference<AuthenticationStepConfig>(null);
        ServiceProviderConfig serviceProvider = authenticationContext.getServiceProvider();
        AuthenticationConfig authenticationConfig = serviceProvider.getAuthenticationConfig();
        List<AuthenticationStepConfig> authenticationStepConfigs = authenticationConfig.getAuthenticationStepConfigs();
        authenticationStepConfigs.stream().filter(authenticationStepConfigTmp -> authenticationStepConfigTmp.getStep
                () == step)
                .forEach(authenticationStepConfigTmp -> {
                    authenticationStepConfig.set(authenticationStepConfigTmp);
                });
        return authenticationStepConfig.get();
    }

    @Override
    public AuthenticatorConfig getAuthenticatorConfig(int step, String authenticatorName, String
            identityProviderName) {
        AuthenticationStepConfig authenticationStepConfig = getAuthenticationStepConfig(step);
        List<IdentityProvider> identityProviders = authenticationStepConfig.getIdentityProviders();
        Optional<IdentityProvider> identityProvider = identityProviders.stream().filter(identityProviderTmp ->
                identityProviderTmp
                .getIdentityProviderName().equals
                 (identityProviderName) && identityProviderTmp
                .getAuthenticatorName().equals(authenticatorName)).findFirst();
        if(identityProvider.isPresent()){
            org.wso2.carbon.identity.gateway.common.model.idp.AuthenticationConfig authenticationConfig =
                    identityProvider.get().getIdentityProviderConfig().getAuthenticationConfig();
            List<AuthenticatorConfig> authenticatorConfigs = authenticationConfig.getAuthenticatorConfigs();
            Optional<AuthenticatorConfig> authenticatorConfig = authenticatorConfigs.stream().filter
                    (authenticatorConfigTmp ->
                    authenticatorConfigTmp
                    .getName().equals
                    (authenticatorName)).findFirst();
            if(authenticatorConfig.isPresent()){
                return authenticatorConfig.get();
            }

        }
        return null;
    }
}
