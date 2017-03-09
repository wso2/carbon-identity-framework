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
import org.wso2.carbon.identity.gateway.api.handler.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.authentication.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.local.LocalAuthenticationRequest;
import org.wso2.carbon.identity.gateway.authentication.local.LocalAuthenticationResponse;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.context.SessionContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.internal.GatewayServiceHolder;
import org.wso2.carbon.identity.gateway.model.User;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class StepHandler extends AbstractGatewayHandler {
    @Override
    public boolean canHandle(MessageContext messageContext) {
        return true;
    }

    @Override
    public String getName() {
        return null;
    }

    public AuthenticationResponse handleStepAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {

        AuthenticationResponse authenticationResponse = null;
        ApplicationAuthenticator applicationAuthenticator = null;
        AbstractSequence sequence = authenticationContext.getSequence();

        SequenceContext sequenceContext = authenticationContext.getSequenceContext();
        SequenceContext.StepContext currentStepContext = sequenceContext.getCurrentStepContext();

        if (currentStepContext != null) {
            if (!currentStepContext.isAuthenticated()) {
                if(currentStepContext.getAuthenticatorName() != null) {
                    applicationAuthenticator = getApplicationAuthenticator(currentStepContext.getAuthenticatorName());
                }else if(authenticationContext.getIdentityRequest() instanceof LocalAuthenticationRequest){
                    LocalAuthenticationRequest localAuthenticationRequest = (LocalAuthenticationRequest)
                            authenticationContext.getIdentityRequest();
                    currentStepContext.setAuthenticatorName(localAuthenticationRequest.getAuthenticatorName());
                    currentStepContext.setIdentityProviderName(localAuthenticationRequest.getIdentityProviderName());
                    applicationAuthenticator = getApplicationAuthenticator(localAuthenticationRequest
                            .getAuthenticatorName());
                }
            } else {
                authenticationResponse = AuthenticationResponse.AUTHENTICATED;
            }
        } else {
            currentStepContext = sequenceContext.addStepContext();
            if (lookUpSessionValidity(authenticationContext)) {
                authenticationResponse = AuthenticationResponse.AUTHENTICATED;
            } else {
                if (sequence.isMultiOption(sequenceContext.getCurrentStep())) {
                    if(authenticationContext.getIdentityRequest() instanceof LocalAuthenticationRequest && ((LocalAuthenticationRequest)authenticationContext.getIdentityRequest()).getAuthenticatorName() != null){
                        LocalAuthenticationRequest localAuthenticationRequest = (LocalAuthenticationRequest)
                                authenticationContext.getIdentityRequest();

                        currentStepContext.setAuthenticatorName(localAuthenticationRequest.getAuthenticatorName());
                        currentStepContext.setIdentityProviderName(localAuthenticationRequest.getIdentityProviderName());
                        applicationAuthenticator = getApplicationAuthenticator(localAuthenticationRequest
                                                                                       .getAuthenticatorName());
                    }else{
                        return buildMultiOptionResponse(authenticationContext);
                    }

                } else {
                    AuthenticationStepConfig config = getAuthenticationStepConfig(authenticationContext, sequenceContext
                            .getCurrentStep());
                    IdentityProvider identityProvider = config.getIdentityProviders().get(0);
                    if (identityProvider != null) {
                        applicationAuthenticator = getApplicationAuthenticator(identityProvider.getAuthenticatorName());
                        if (applicationAuthenticator != null) {
                            authenticationResponse = AuthenticationResponse.AUTHENTICATED;
                            currentStepContext.setAuthenticatorName(applicationAuthenticator.getName());
                            currentStepContext.setIdentityProviderName(identityProvider.getIdentityProviderName());
                        }
                    }
                }
            }
        }


        if (applicationAuthenticator != null) {
            authenticationResponse = applicationAuthenticator.process(authenticationContext);
        }

        if (AuthenticationResponse.AUTHENTICATED.equals(authenticationResponse)) {
            currentStepContext.setIsAuthenticated(true);
            if (sequence.hasNext(sequenceContext.getCurrentStep())) {
                sequenceContext.setCurrentStep(sequenceContext.getCurrentStep() + 1);

                authenticationResponse = handleStepAuthentication(authenticationContext);
            }
        }

        return authenticationResponse;
    }

    private ApplicationAuthenticator getApplicationAuthenticator(String applicationAuthenticatorName){
        ApplicationAuthenticator applicationAuthenticator =
                GatewayServiceHolder.getInstance().getLocalApplicationAuthenticator(applicationAuthenticatorName);
        if (applicationAuthenticator == null) {
            applicationAuthenticator =
                    GatewayServiceHolder.getInstance()
                            .getFederatedApplicationAuthenticator(applicationAuthenticatorName);
        }
        return applicationAuthenticator ;
    }

    private AuthenticationResponse buildMultiOptionResponse(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        LocalAuthenticationResponse.LocalAuthenticationResponseBuilder
                localAuthenticationResponseBuilder = new LocalAuthenticationResponse
                .LocalAuthenticationResponseBuilder();
        localAuthenticationResponseBuilder.setRelayState(authenticationContext
                                                                 .getInitialAuthenticationRequest()
                                                                 .getRequestKey());
        List<IdentityProvider> identityProviders = authenticationContext.getSequence()
                .getIdentityProviders(authenticationContext.getSequenceContext().getCurrentStep());
        StringBuilder idpList = new StringBuilder();
        identityProviders.forEach(identityProvider -> idpList.append(identityProvider
                                                                             .getAuthenticatorName() +
                                                                     ":" + identityProvider
                                                                             .getIdentityProviderName()
                                                                     +","));
        AuthenticationResponse authenticationResponse = AuthenticationResponse.INCOMPLETE ;
        authenticationResponse.setGatewayResponseBuilder(localAuthenticationResponseBuilder);
        return authenticationResponse ;
    }

    protected boolean lookUpSessionValidity(AuthenticationContext authenticationContext) throws
                                                                                         AuthenticationHandlerException {

        boolean isSessionValid = false;
        SessionContext sessionContext = authenticationContext.getSessionContext();
        Collection<SequenceContext> existingContexts = null;
        if (sessionContext != null) {
            existingContexts = sessionContext.getSequenceContexts();
            Iterator<SequenceContext> it = existingContexts.iterator();
            while (it.hasNext()) {
                SequenceContext sequenceContext = it.next();
                int currentStep = authenticationContext.getSequenceContext().getCurrentStep();
                String idPName = sequenceContext
                        .getStepContext(authenticationContext.getSequenceContext().getCurrentStep())
                        .getIdentityProviderName();
                IdentityProvider identityProvider = authenticationContext.getSequence().getIdentityProvider(currentStep,
                                                                                                            idPName);
                String authenticatorName = sequenceContext.getStepContext(authenticationContext.getSequenceContext()
                                                                                  .getCurrentStep())
                        .getAuthenticatorName();
                User user = sequenceContext.getStepContext(authenticationContext.getSequenceContext().getCurrentStep())
                        .getUser();
                if (identityProvider != null) {
                    authenticationContext.getSequenceContext().getCurrentStepContext().setIdentityProviderName(idPName);
                    authenticationContext.getSequenceContext().getCurrentStepContext().setUser(user);
                    authenticationContext.getSequenceContext().getCurrentStepContext()
                            .setAuthenticatorName(authenticatorName);
                    authenticationContext.getSequenceContext().getCurrentStepContext().setIsAuthenticated(true);
                    isSessionValid = true;
                    break;
                }
            }
        }
        return isSessionValid;
    }

    private AuthenticationStepConfig getAuthenticationStepConfig(AuthenticationContext context, int step) throws
                                                                                                          AuthenticationHandlerException {
        ServiceProviderConfig serviceProvider = context.getServiceProvider();
        AuthenticationConfig authenticationConfig = serviceProvider.getAuthenticationConfig();
        AuthenticationStepConfig authenticationStepConfig = authenticationConfig.getAuthenticationStepConfigs()
                .get(step - 1);
        return authenticationStepConfig;
    }
}