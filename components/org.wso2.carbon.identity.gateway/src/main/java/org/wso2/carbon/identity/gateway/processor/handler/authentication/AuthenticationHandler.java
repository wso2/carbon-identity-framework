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
package org.wso2.carbon.identity.gateway.processor.handler.authentication;


import org.wso2.carbon.identity.common.base.exception.IdentityRuntimeException;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.response.GatewayHandlerResponse;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.api.handler.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AuthenticationResponse;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.SequenceManager;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.AbstractSequence;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.util.HandlerManager;

public class AuthenticationHandler extends AbstractGatewayHandler {
    @Override
    public String getName() {
        return null;
    }

    public GatewayHandlerResponse doAuthenticate(AuthenticationContext authenticationContext) throws
                                                                                                AuthenticationHandlerException {


        GatewayHandlerResponse gatewayHandlerResponse = null;

        HandlerManager handlerManager = HandlerManager.getInstance();

        AbstractSequenceBuildFactory abstractSequenceBuildFactory =
                handlerManager.getSequenceBuildFactory(authenticationContext);
        AbstractSequence abstractSequence = abstractSequenceBuildFactory.buildSequence(authenticationContext);

        authenticationContext.setSequence(abstractSequence);

        /*
        ContextInitializer contextInitializerHandler =
                handlerManager.getContextInitializerHandler(authenticationContext);
        contextInitializerHandler.initialize(authenticationContext);
        */
        SequenceManager sequenceManager =
                handlerManager.getSequenceManager(authenticationContext);

        AuthenticationResponse authenticationResponse =
                sequenceManager.handleSequence(authenticationContext);

        gatewayHandlerResponse = buildFrameworkHandlerResponse(authenticationResponse);


        return gatewayHandlerResponse;
    }

    @Override
    public boolean canHandle(MessageContext messageContext) throws IdentityRuntimeException {
        return true;
    }

    private GatewayHandlerResponse buildFrameworkHandlerResponse(AuthenticationResponse handlerResponse) {
        GatewayHandlerResponse gatewayHandlerResponse = null;
        if (AuthenticationResponse.AUTHENTICATED.equals(handlerResponse)) {
            gatewayHandlerResponse = GatewayHandlerResponse.CONTINUE;
        } else if (AuthenticationResponse.INCOMPLETE.equals(handlerResponse)) {
            gatewayHandlerResponse = GatewayHandlerResponse.REDIRECT;
            gatewayHandlerResponse.setGatewayResponseBuilder(handlerResponse.getGatewayResponseBuilder());
        }
        return gatewayHandlerResponse;
    }


}