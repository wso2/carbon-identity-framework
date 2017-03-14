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

/**
 * MultiOptionExecutionHandler is for handle multi-option authenticators within single step. This is control the
 * multi-option landing page and handle the retry as well.
 */
public class SingleOptionExecutionHandler extends AbstractExecutionHandler {

    private static final Logger log = LoggerFactory.getLogger(SingleOptionExecutionHandler.class);

    @Override
    public AuthenticationResponse execute(AuthenticationContext authenticationContext) throws AuthenticationHandlerException {

        ApplicationAuthenticator applicationAuthenticator = null;
        SequenceContext sequenceContext = authenticationContext.getSequenceContext();
        Sequence sequence = authenticationContext.getSequence();

        applicationAuthenticator = getApplicationAuthenticatorInContext(authenticationContext);

        SequenceContext.StepContext currentStepContext = sequenceContext.getCurrentStepContext();

        if (applicationAuthenticator == null) {
            AuthenticationStepConfig authenticationStepConfig = sequence.getAuthenticationStepConfig(currentStepContext.getStep());
            IdentityProvider identityProvider = authenticationStepConfig.getIdentityProviders().get(0);
            currentStepContext.setAuthenticatorName(identityProvider.getAuthenticatorName());
            currentStepContext.setIdentityProviderName(identityProvider.getIdentityProviderName());
            applicationAuthenticator = getApplicationAuthenticatorInContext(identityProvider.getAuthenticatorName());

            if (applicationAuthenticator == null) {
                throw new AuthenticationHandlerException("Authenticator not found.");
            }
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
                    return execute(authenticationContext);
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
            return canHandle(authenticationContext, ExecutionStrategy.SINGLE.toString());
        } catch (AuthenticationHandlerException e) {
            log.error("Error occurred while trying to check the can handle for execution strategy, " + e.getMessage());
        }
        return false;
    }
}
