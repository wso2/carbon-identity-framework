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

import org.wso2.carbon.identity.gateway.api.handler.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.authentication.AbstractSequence;
import org.wso2.carbon.identity.gateway.authentication.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.authentication.authenticator.ApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.authentication.local.LocalAuthenticationResponse;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.internal.GatewayServiceHolder;

import java.util.List;

public abstract class AbstractExecutionHandler extends
                                               AbstractGatewayHandler<AuthenticationContext> {
    public abstract AuthenticationResponse execute(AuthenticationContext authenticationContext) throws AuthenticationHandlerException;

    public boolean canHandle(AuthenticationContext authenticationContext, String executionStrategy) throws AuthenticationHandlerException {
        AbstractSequence sequence = authenticationContext.getSequence();
        SequenceContext sequenceContext = authenticationContext.getSequenceContext();
        AuthenticationStepConfig authenticationStepConfig = sequence.getAuthenticationStepConfig(sequenceContext.getCurrentStep());
        if(authenticationStepConfig != null &&executionStrategy.equals(authenticationStepConfig.getExecutionStrategy
                ())){
            return true ;
        }
        return false;
    }
    @Override
    public abstract boolean canHandle(AuthenticationContext authenticationContext);

    protected ApplicationAuthenticator getApplicationAuthenticator(String applicationAuthenticatorName){
        ApplicationAuthenticator applicationAuthenticator =
                GatewayServiceHolder.getInstance().getLocalApplicationAuthenticator(applicationAuthenticatorName);
        if (applicationAuthenticator == null) {
            applicationAuthenticator =
                    GatewayServiceHolder.getInstance()
                            .getFederatedApplicationAuthenticator(applicationAuthenticatorName);
        }
        return applicationAuthenticator ;
    }

    protected AuthenticationResponse buildEndpointURL(AuthenticationContext authenticationContext)
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
}
