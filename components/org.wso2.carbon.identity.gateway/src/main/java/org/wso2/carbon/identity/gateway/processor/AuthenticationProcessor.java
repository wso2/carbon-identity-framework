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

import org.slf4j.Logger;
import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.carbon.identity.gateway.api.processor.GatewayProcessor;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.gateway.exception.ResponseHandlerException;
import org.wso2.carbon.identity.gateway.handler.GatewayHandlerManager;
import org.wso2.carbon.identity.gateway.handler.GatewayHandlerResponse;
import org.wso2.carbon.identity.gateway.request.AuthenticationRequest;
import org.wso2.carbon.identity.gateway.request.CallbackAuthenticationRequest;
import org.wso2.carbon.identity.gateway.request.ClientAuthenticationRequest;

/**
 * AuthenticationProcessor is the main processor in Authentication framework that is executing the template
 * for client and callback request.
 */
public class AuthenticationProcessor extends GatewayProcessor<AuthenticationRequest> {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(AuthenticationProcessor.class);

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

        if (log.isDebugEnabled()) {
            log.debug("AuthenticationProcessor is starting to process the request.");
        }
        AuthenticationContext authenticationContext = null;
        GatewayHandlerResponse response = new GatewayHandlerResponse(GatewayHandlerResponse.Status.CONTINUE);
        GatewayHandlerManager gatewayHandlerManager = GatewayHandlerManager.getInstance();
        try {
            authenticationContext = loadAuthenticationContext(authenticationRequest);
            if (log.isDebugEnabled()) {
                log.debug("AuthenticationProcessor loaded the AuthenticationContext.");
            }
            if (authenticationRequest instanceof ClientAuthenticationRequest) {
                response = gatewayHandlerManager
                        .getRequestValidator(authenticationContext).validate(authenticationContext);
                if (log.isDebugEnabled()) {
                    log.debug("AuthenticationProcessor called the validation.");
                }
            }
            if (response.status.equals(GatewayHandlerResponse.Status.CONTINUE)) {

                response = gatewayHandlerManager.getAuthenticationHandler(authenticationContext)
                        .authenticate(authenticationContext);
                if (log.isDebugEnabled()) {
                    log.debug("AuthenticationProcessor called the authentication.");
                }
                if (response.status.equals(GatewayHandlerResponse.Status.CONTINUE)) {

                    response = gatewayHandlerManager.getSessionHandler(authenticationContext)
                            .updateSession(authenticationContext);
                    if (log.isDebugEnabled()) {
                        log.debug("AuthenticationProcessor called the update session.");
                    }
                    if (response.status.equals(GatewayHandlerResponse.Status.CONTINUE)) {

                        response = gatewayHandlerManager.getResponseHandler(authenticationContext)
                                .buildResponse(authenticationContext);
                        if (log.isDebugEnabled()) {
                            log.debug("AuthenticationProcessor called the response handler.");
                        }
                    }
                }
            }
        } catch (GatewayServerException | GatewayClientException exception) {
            String errorMessage = "Error occurred in AuthenticationProcessor, " + exception.getMessage();
            log.error(errorMessage, exception);
            try {
                response = gatewayHandlerManager.getResponseHandler(authenticationContext, exception).buildErrorResponse
                        (authenticationContext, exception);
            } catch (ResponseHandlerException e) {
                errorMessage = "Error occurred while processing the response, " + e.getMessage();
                log.error(errorMessage, e);
                throw new GatewayRuntimeException(errorMessage, e);
            }
        } catch (GatewayRuntimeException exception) {
            String errorMessage = "Error occurred in AuthenticationProcessor, " + exception.getMessage();
            log.error(errorMessage, exception);
            try {
                response = gatewayHandlerManager.getResponseHandler(authenticationContext, exception).buildErrorResponse
                        (authenticationContext, exception);
            } catch (ResponseHandlerException e) {
                errorMessage = "Error occurred while processing the response, " + e.getMessage();
                log.error(errorMessage, e);
                throw new GatewayRuntimeException(errorMessage, e);
            }
        }

        AuthenticationContextCache.getInstance().put(authenticationRequest.getRequestKey(), authenticationContext);
        if (log.isDebugEnabled()) {
            log.debug("AuthenticationProcessor updated the authentication context.");
        }
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

            AuthenticationContextCache.getInstance().put(requestDataKey, authenticationContext);
        } else {
            GatewayMessageContext gatewayMessageContext =
                    AuthenticationContextCache.getInstance().get(requestDataKey);
            if (gatewayMessageContext instanceof AuthenticationContext) {
                authenticationContext =
                        (AuthenticationContext) gatewayMessageContext;
                if (authenticationContext == null) {
                    String errorMessage = "AuthenticationContext is not available for give state value.";
                    log.error(errorMessage);
                    throw new GatewayRuntimeException(errorMessage);
                }
                authenticationContext.setIdentityRequest(authenticationRequest);
            }
        }
        return authenticationContext;
    }
}
