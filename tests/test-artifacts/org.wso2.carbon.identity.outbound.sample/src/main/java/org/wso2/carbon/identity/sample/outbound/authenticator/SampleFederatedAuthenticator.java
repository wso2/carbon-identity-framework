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

package org.wso2.carbon.identity.sample.outbound.authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.model.FederatedUser;
import org.wso2.carbon.identity.gateway.processor.authenticator.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.authenticator.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AuthenticationResponse;
import org.wso2.carbon.identity.sample.outbound.request.SampleACSRequest;
import org.wso2.carbon.identity.sample.outbound.response.SampleProtocolRequestResponse;

import java.util.List;
import java.util.Properties;

/**
 * SAML2 SSO Outbound Authenticator.
 */
public class SampleFederatedAuthenticator extends AbstractApplicationAuthenticator implements FederatedApplicationAuthenticator {

    private static Logger log = LoggerFactory.getLogger(SampleFederatedAuthenticator.class);

    @Override
    public String getName() {
        return "SampleFederatedAuthenticator";
    }

    @Override
    public String getFriendlyName() {
        return "SampleFederatedAuthenticator";
    }

    @Override
    public String getClaimDialectURI() {
        return null;
    }

    @Override
    public List<Properties> getConfigurationProperties() {
        return null;
    }

    @Override
    public boolean canHandle(AuthenticationContext authenticationContext) {
        return true;
    }

    @Override
    public String getContextIdentifier(AuthenticationContext authenticationContext) {
        return null;
    }

    @Override
    protected boolean isInitialRequest(AuthenticationContext authenticationContext) {

        if (authenticationContext.getIdentityRequest() instanceof SampleACSRequest) {
            return false;
        }
        return true;
    }


    @Override
    protected AuthenticationResponse processRequest(AuthenticationContext context)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = AuthenticationResponse.INCOMPLETE;
        GatewayResponse.GatewayResponseBuilder builder = new SampleProtocolRequestResponse
                .SampleProtocolRequestResponseBuilder();
        builder.setSessionKey(context.getInitialAuthenticationRequest().getRequestKey());
        authenticationResponse.setGatewayResponseBuilder(builder);
        return authenticationResponse;
    }

    @Override
    protected AuthenticationResponse processResponse(AuthenticationContext context) throws AuthenticationHandlerException {
        context.getSequenceContext().getCurrentStepContext().setUser(new FederatedUser(context.getIdentityRequest()
                .getParameter("Assertion")));
        AuthenticationResponse authenticationResponse =  AuthenticationResponse.AUTHENTICATED;
        return authenticationResponse;
    }

}
