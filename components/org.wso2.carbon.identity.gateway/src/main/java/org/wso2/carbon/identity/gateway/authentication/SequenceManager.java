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
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;

public class SequenceManager extends AbstractGatewayHandler {
    @Override
    public boolean canHandle(MessageContext messageContext) {
        return true;
    }

    @Override
    public String getName() {
        return null;
    }

    public AuthenticationResponse handleSequence(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = null;
        AbstractSequence abstractSequence = authenticationContext.getSequence();
        if (abstractSequence.isRequestPathAuthenticatorsAvailable()) {
            authenticationResponse = handleRequestPathAuthentication(authenticationContext);
        }
        if (authenticationResponse == null && abstractSequence.isStepAuthenticatorAvailable()) {
            authenticationResponse = handleStepAuthentication(authenticationContext);
        }

        return authenticationResponse;
    }

    protected AuthenticationResponse handleRequestPathAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        RequestPathHandler requestPathHandler =
                HandlerManager.getInstance().getRequestPathHandler(authenticationContext);
        return requestPathHandler.handleRequestPathAuthentication(authenticationContext);
    }

    protected AuthenticationResponse handleStepAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        StepHandler stepHandler = HandlerManager.getInstance().getStepHandler(authenticationContext);
        return stepHandler.handleStepAuthentication(authenticationContext);
    }
}
