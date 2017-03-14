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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.gateway.api.handler.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.authentication.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.response.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.authentication.sequence.Sequence;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.internal.GatewayServiceHolder;
import org.wso2.carbon.identity.gateway.request.AuthenticationRequest;
import org.wso2.carbon.identity.gateway.request.ClientAuthenticationRequest;

/**
 * Within the step handler, we have to execute authenticators based on the given execution strategy. To implement
 * that we have to provide execution handlers for each execution strategy. AbstractExecutionHandler is provide
 * generic function to do that.
 */
public abstract class AbstractExecutionHandler extends
        AbstractGatewayHandler<AuthenticationContext> {

    /**
     * Execute method is the method that we must trigger from the StepHanlder after checking the canHanlde mehtod.
     *
     * @param authenticationContext
     * @return
     * @throws AuthenticationHandlerException
     */
    public abstract AuthenticationResponse execute(AuthenticationContext authenticationContext) throws AuthenticationHandlerException;


    /**
     * Call the can canHanlde method to confirm whether this handler can execute this request or not.
     *
     * @param authenticationContext
     * @param executionStrategy
     * @return
     * @throws AuthenticationHandlerException
     */
    public boolean canHandle(AuthenticationContext authenticationContext, String executionStrategy) throws AuthenticationHandlerException {
        Sequence sequence = authenticationContext.getSequence();
        SequenceContext sequenceContext = authenticationContext.getSequenceContext();
        AuthenticationStepConfig authenticationStepConfig = sequence.getAuthenticationStepConfig(sequenceContext.getCurrentStep());
        if (authenticationStepConfig != null && executionStrategy.equals(authenticationStepConfig.getExecutionStrategy
                ())) {
            return true;
        }
        return false;
    }

    /**
     * Get the ApplicationAuthenticator for a given authenticator name.
     *
     * @param applicationAuthenticatorName
     * @return
     */
    protected ApplicationAuthenticator getApplicationAuthenticatorInContext(String applicationAuthenticatorName) {
        ApplicationAuthenticator applicationAuthenticator =
                GatewayServiceHolder.getInstance().getLocalApplicationAuthenticator(applicationAuthenticatorName);
        if (applicationAuthenticator == null) {
            applicationAuthenticator =
                    GatewayServiceHolder.getInstance()
                            .getFederatedApplicationAuthenticator(applicationAuthenticatorName);
        }
        return applicationAuthenticator;
    }


    /**
     * Get application authenticator from current context.
     *
     * @param authenticationContext
     * @return
     */
    protected ApplicationAuthenticator getApplicationAuthenticatorInContext(AuthenticationContext authenticationContext) {

        ApplicationAuthenticator applicationAuthenticator = null;
        SequenceContext sequenceContext = authenticationContext.getSequenceContext();
        SequenceContext.StepContext currentStepContext = sequenceContext.getCurrentStepContext();
        GatewayRequest identityRequest = authenticationContext.getIdentityRequest();

        if (identityRequest instanceof AuthenticationRequest) {
            AuthenticationRequest authenticationRequest = (AuthenticationRequest) identityRequest;
            if (currentStepContext != null) {
                if (StringUtils.isNotBlank(currentStepContext.getAuthenticatorName())
                        && StringUtils.isNotBlank(currentStepContext.getIdentityProviderName())) {
                    applicationAuthenticator = getApplicationAuthenticatorInContext(currentStepContext.getAuthenticatorName());
                } else if (StringUtils.isNotBlank(authenticationRequest.getAuthenticatorName()) && StringUtils.isNotBlank
                        (authenticationRequest.getIdentityProviderName())) {
                    applicationAuthenticator = getApplicationAuthenticatorInContext(authenticationRequest.getAuthenticatorName());
                    currentStepContext.setIdentityProviderName(authenticationRequest.getIdentityProviderName());
                    currentStepContext.setAuthenticatorName(authenticationRequest.getAuthenticatorName());
                }
            } else {
                currentStepContext = sequenceContext.addStepContext();
                if (authenticationRequest instanceof ClientAuthenticationRequest && StringUtils.isNotBlank(authenticationRequest
                        .getAuthenticatorName())
                        &&
                        StringUtils.isNotBlank
                                (authenticationRequest.getIdentityProviderName())) {
                    applicationAuthenticator = getApplicationAuthenticatorInContext(authenticationRequest.getAuthenticatorName());
                    currentStepContext.setIdentityProviderName(authenticationRequest.getIdentityProviderName());
                    currentStepContext.setAuthenticatorName(authenticationRequest.getAuthenticatorName());
                }
            }
        }
        return applicationAuthenticator;
    }
}
