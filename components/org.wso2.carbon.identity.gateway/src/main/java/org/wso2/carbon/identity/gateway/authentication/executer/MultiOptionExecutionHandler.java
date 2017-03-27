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
 *
 */

package org.wso2.carbon.identity.gateway.authentication.executer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.authentication.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.response.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.authentication.sequence.Sequence;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.local.LocalAuthenticationResponse;

import java.util.List;

/**
 * MultiOptionExecutionHandler is for handle multi-option authenticators within single step. This is control the
 * multi-option landing page and handle the retry as well.
 */
public class MultiOptionExecutionHandler extends AbstractExecutionHandler {

    private static final Logger log = LoggerFactory.getLogger(MultiOptionExecutionHandler.class);

    @Override
    public AuthenticationResponse execute(AuthenticationContext authenticationContext) throws AuthenticationHandlerException {

        SequenceContext sequenceContext = authenticationContext.getSequenceContext();
        Sequence sequence = authenticationContext.getSequence();

        ApplicationAuthenticator applicationAuthenticator = getApplicationAuthenticatorInContext(authenticationContext);

        SequenceContext.StepContext currentStepContext = sequenceContext.getCurrentStepContext();

        if (applicationAuthenticator == null) {
            LocalAuthenticationResponse.LocalAuthenticationResponseBuilder
                    localAuthenticationResponseBuilder = new LocalAuthenticationResponse
                    .LocalAuthenticationResponseBuilder();
            localAuthenticationResponseBuilder.setRelayState(authenticationContext
                    .getInitialAuthenticationRequest()
                    .getRequestKey());
            localAuthenticationResponseBuilder.setEndpointURL("https://localhost:9292/gateway/endpoint");
            List<IdentityProvider> identityProviders = authenticationContext.getSequence()
                    .getIdentityProviders(authenticationContext.getSequenceContext().getCurrentStep());
            StringBuilder idpList = new StringBuilder();
            identityProviders.forEach(identityProvider -> idpList.append(identityProvider
                    .getAuthenticatorName() +
                    ":" + identityProvider
                    .getIdentityProviderName()
                    + ","));
            localAuthenticationResponseBuilder.setIdentityProviderList(idpList.toString());
            AuthenticationResponse authenticationResponse = new AuthenticationResponse
                    (localAuthenticationResponseBuilder);

            return authenticationResponse;
        }

        AuthenticationResponse response = null;
        try {
            response = applicationAuthenticator.process(authenticationContext);
            if (AuthenticationResponse.Status.AUTHENTICATED.equals(response.status)) {
                sequenceContext.getCurrentStepContext().setStatus(SequenceContext.Status.AUTHENTICATED);
            } else {
                sequenceContext.getCurrentStepContext().setStatus(SequenceContext.Status.INCOMPLETE);
            }
        } catch (AuthenticationHandlerException e) {
            currentStepContext.setStatus(SequenceContext.Status.FAILED);
            if (applicationAuthenticator.isRetryEnable(authenticationContext)) {
                AuthenticationStepConfig authenticationStepConfig = sequence.getAuthenticationStepConfig(currentStepContext.getStep());
                int retryCount = authenticationStepConfig.getRetryCount();
                if (currentStepContext.getRetryCount() <= retryCount) {
                    currentStepContext.setRetryCount(currentStepContext.getRetryCount() + 1);

                    LocalAuthenticationResponse.LocalAuthenticationResponseBuilder
                            localAuthenticationResponseBuilder = new LocalAuthenticationResponse
                            .LocalAuthenticationResponseBuilder();
                    localAuthenticationResponseBuilder.setRelayState(authenticationContext
                            .getInitialAuthenticationRequest()
                            .getRequestKey());
                    localAuthenticationResponseBuilder.setEndpointURL("https://localhost:9292/gateway/endpoint");
                    List<IdentityProvider> identityProviders = authenticationContext.getSequence()
                            .getIdentityProviders(authenticationContext.getSequenceContext().getCurrentStep());
                    StringBuilder idpList = new StringBuilder();
                    identityProviders.forEach(identityProvider -> idpList.append(identityProvider
                            .getAuthenticatorName() +
                            ":" + identityProvider
                            .getIdentityProviderName()
                            + ","));
                    localAuthenticationResponseBuilder.setIdentityProviderList(idpList.toString());
                    AuthenticationResponse authenticationResponse = new AuthenticationResponse
                            (localAuthenticationResponseBuilder);

                    return authenticationResponse;
                }

            } else {
                throw e;
            }
        }

        return response;
    }

    @Override
    public boolean canHandle(AuthenticationContext authenticationContext) {
        try {
            return canHandle(authenticationContext, ExecutionStrategy.MULTI.toString());
        } catch (AuthenticationHandlerException e) {
            log.error("Error occurred while trying to check the can handle for execution strategy, " + e.getMessage()
                    , e);
        }
        return false;
    }

    /*public String getMultiOptionEndpoint() {
        return "https://localhost:9292/gateway/endpoint";
    }*/
}
