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

package org.wso2.carbon.identity.gateway.processor;

import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.carbon.identity.gateway.api.processor.GatewayProcessor;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.api.response.GatewayHandlerResponse;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.cache.IdentityMessageContextCache;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.response.ResponseHandlerException;
import org.wso2.carbon.identity.gateway.processor.request.AuthenticationRequest;
import org.wso2.carbon.identity.gateway.processor.request.CallbackAuthenticationRequest;
import org.wso2.carbon.identity.gateway.processor.request.ClientAuthenticationRequest;
import org.wso2.carbon.identity.gateway.processor.util.HandlerManager;

/**
 * AuthenticationProcessor is the main processor in Authentication framework that is executing the template
 * for client and callback request.
 */
public class AuthenticationProcessor extends GatewayProcessor<AuthenticationRequest> {

    @Override
    public boolean canHandle(GatewayRequest gatewayRequest) {

        if (gatewayRequest instanceof ClientAuthenticationRequest
            || gatewayRequest instanceof CallbackAuthenticationRequest) {
            return true;
        }
        return false;
    }

    public int getPriority() {
        return 50;
    }

    @Override
    public GatewayResponse.GatewayResponseBuilder process(AuthenticationRequest authenticationRequest) {

        AuthenticationContext authenticationContext = null;
        GatewayHandlerResponse response = GatewayHandlerResponse.CONTINUE;
        HandlerManager handlerManager = HandlerManager.getInstance();
        try {
            authenticationContext = loadAuthenticationContext(authenticationRequest);

            if (authenticationRequest instanceof ClientAuthenticationRequest) {
                response = handlerManager.getRequestValidator(authenticationContext).validate(authenticationContext);
            }
            if (response.equals(GatewayHandlerResponse.CONTINUE)) {

                response = handlerManager.getAuthenticationHandler(authenticationContext)
                        .doAuthenticate(authenticationContext);
                if (response.equals(GatewayHandlerResponse.CONTINUE)) {

                    response = handlerManager.getSessionHandler(authenticationContext)
                            .updateSession(authenticationContext);
                    if (response.equals(GatewayHandlerResponse.CONTINUE)) {

                        response = handlerManager.getResponseHandler(authenticationContext)
                                .buildResponse(authenticationContext);
                    }
                }
            }
        } catch (GatewayServerException | GatewayClientException exception) {
            try {
                response = handlerManager.getResponseHandler(authenticationContext, exception).buildErrorResponse
                        (authenticationContext, exception);
            } catch (ResponseHandlerException e) {
                throw new GatewayRuntimeException("Error occurred while processing the response, " + e.getMessage(), e);
            }
        } catch (GatewayRuntimeException exception) {
            try {
                response = handlerManager.getResponseHandler(authenticationContext, exception).buildErrorResponse
                        (authenticationContext, exception);
            } catch (ResponseHandlerException e) {
                throw new GatewayRuntimeException("Error occurred while processing the response, " + e.getMessage(), e);
            }
        }

        IdentityMessageContextCache.getInstance().put(authenticationRequest.getRequestKey(), authenticationContext);
        return response.getGatewayResponseBuilder();
    }

    /**
     * Load last AuthenticationContext from cache for given requestKey.
     *
     * @param authenticationRequest
     * @return
     */
    protected AuthenticationContext loadAuthenticationContext(AuthenticationRequest authenticationRequest) {

        AuthenticationContext authenticationContext = null;
        String requestDataKey = authenticationRequest.getRequestKey();
        if (authenticationRequest instanceof ClientAuthenticationRequest) {
            authenticationContext = new AuthenticationContext((ClientAuthenticationRequest) authenticationRequest);
            //requestKey is the co-relation key to re-load the context after subsequent call to the system.

            IdentityMessageContextCache.getInstance().put(requestDataKey, authenticationContext);
        } else {
            authenticationContext =
                    (AuthenticationContext) IdentityMessageContextCache.getInstance().get(requestDataKey);
            if (authenticationContext == null) {
                throw new GatewayRuntimeException("AuthenticationContext is not available for give state value.");
            }
            authenticationContext.setIdentityRequest(authenticationRequest);
        }
        return authenticationContext;
    }
}
