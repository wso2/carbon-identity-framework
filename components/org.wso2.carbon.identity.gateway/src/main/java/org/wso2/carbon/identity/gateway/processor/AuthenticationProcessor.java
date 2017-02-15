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

import org.wso2.carbon.identity.common.base.exception.IdentityException;
import org.wso2.carbon.identity.gateway.api.response.FrameworkHandlerResponse;
import org.wso2.carbon.identity.gateway.api.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.api.exception.FrameworkServerException;
import org.wso2.carbon.identity.gateway.api.context.IdentityMessageContext;
import org.wso2.carbon.identity.gateway.api.processor.IdentityProcessor;
import org.wso2.carbon.identity.gateway.api.request.IdentityRequest;
import org.wso2.carbon.identity.gateway.api.response.IdentityResponse;
import org.wso2.carbon.identity.gateway.cache.IdentityMessageContextCache;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.request.AbstractRequestHandler;
import org.wso2.carbon.identity.gateway.processor.handler.request.RequestHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.response.AbstractResponseHandler;
import org.wso2.carbon.identity.gateway.processor.handler.response.ResponseException;
import org.wso2.carbon.identity.gateway.processor.request.AuthenticationRequest;
import org.wso2.carbon.identity.gateway.processor.request.CallbackAuthenticationRequest;
import org.wso2.carbon.identity.gateway.processor.request.ClientAuthenticationRequest;
import org.wso2.carbon.identity.gateway.processor.util.HandlerManager;

/**
 * AuthenticationProcessor is the main processor in Authentication framework that is executing the template
 * for client and callback request.
 */
public class AuthenticationProcessor extends IdentityProcessor<AuthenticationRequest> {

    @Override
    public boolean canHandle(IdentityRequest identityRequest) {
        //Since this the default processor, always can handle should be return and if some wants to override this,
        // they can override and give high priority to the new processor.
        return true;
    }

    @Override
    public IdentityResponse.IdentityResponseBuilder process(AuthenticationRequest authenticationRequest)
                                                                                    throws FrameworkServerException {
        IdentityResponse.IdentityResponseBuilder identityResponseBuilder = null;

        /*
        If the authenticationRequest is ClientAuthenticationRequest, that mean this is an initial request that is
        coming from the client. If it is CallbackAuthenticationRequest, so it should be a subsequent call to the
        gateway by local or external parties.
         */
        if (authenticationRequest instanceof ClientAuthenticationRequest) {

            AuthenticationContext authenticationContext = initAuthenticationContext(
                    (ClientAuthenticationRequest) authenticationRequest);
            identityResponseBuilder = processLoginRequest(authenticationContext);
        } else if (authenticationRequest instanceof CallbackAuthenticationRequest) {

            AuthenticationContext authenticationContext = loadAuthenticationContext(
                    (CallbackAuthenticationRequest) authenticationRequest);
            if (authenticationContext == null) {
                throw new FrameworkRuntimeException("Invalid Request.");
            }
            identityResponseBuilder = processAuthenticationRequest(authenticationContext);
        }
        return identityResponseBuilder;
    }


    /**
     * This is an initial request that is coming from the client.
     *
     * @param authenticationContext
     * @return
     * @throws FrameworkHandlerException
     */
    protected IdentityResponse.IdentityResponseBuilder processLoginRequest(AuthenticationContext authenticationContext)
            throws FrameworkHandlerException {

        FrameworkHandlerResponse identityFrameworkHandlerResponse = null;
        try {
            //Protocol validator request validate in this level.
            identityFrameworkHandlerResponse = validate(authenticationContext);
            if (identityFrameworkHandlerResponse.equals(FrameworkHandlerResponse.CONTINUE)) {
                //Authentication handler will start to execute.
                identityFrameworkHandlerResponse = authenticate(authenticationContext);
            }
            if (identityFrameworkHandlerResponse.equals(FrameworkHandlerResponse.CONTINUE)) {
                //If the authentication is done, now it should be built the response based on inbound protocol.
                identityFrameworkHandlerResponse = buildResponse(authenticationContext);
            }
        } catch (AuthenticationHandlerException e) {
            //If the authentication failed, then it should be build the error response based on the protocol level
            // handler.
            identityFrameworkHandlerResponse = buildErrorResponse(authenticationContext, e);
        }
        return identityFrameworkHandlerResponse.getIdentityResponseBuilder();
    }


    /**
     * This method will execute for the subsequent call to the gateway.
     *
     * @param authenticationContext
     * @return
     * @throws FrameworkHandlerException
     */
    protected IdentityResponse.IdentityResponseBuilder processAuthenticationRequest(
            AuthenticationContext authenticationContext) throws FrameworkHandlerException {
        FrameworkHandlerResponse frameworkHandlerResponse = null;
        try {
            //Authentication handler will start to execute.
            frameworkHandlerResponse = authenticate(authenticationContext);
            if (frameworkHandlerResponse.equals(FrameworkHandlerResponse.CONTINUE)) {
                //If the authentication is done, now it should be built the response based on inbound protocol.
                frameworkHandlerResponse = buildResponse(authenticationContext);
            }
        } catch (AuthenticationHandlerException e) {
            //If the authentication failed, then it should be build the error response based on the protocol level
            // handler.
            frameworkHandlerResponse = buildErrorResponse(authenticationContext, e);
        }
        return frameworkHandlerResponse.getIdentityResponseBuilder();
    }


    /**
     * Build new AuthenticationContext for the initial request to start the flow through the framework.
     *
     * @param clientAuthenticationRequest
     * @return
     */
    protected AuthenticationContext initAuthenticationContext(ClientAuthenticationRequest clientAuthenticationRequest) {

        AuthenticationContext authenticationContext = new AuthenticationContext(clientAuthenticationRequest);
        //requestDataKey is the co-relation key to re-load the context after subsequent call to the system.
        String requestDataKey = clientAuthenticationRequest.getRequestDataKey();
        IdentityMessageContextCache.getInstance().addToCache(requestDataKey, authenticationContext);
        return authenticationContext;
    }

    /**
     * Load last AuthenticationContext from cache for given requestDataKey.
     *
     * @param authenticationRequest
     * @return
     */
    protected AuthenticationContext loadAuthenticationContext(AuthenticationRequest authenticationRequest) {

        AuthenticationContext authenticationContext = null;
        String requestDataKey = authenticationRequest.getRequestDataKey();
        IdentityMessageContext identityMessageContext =
                IdentityMessageContextCache.getInstance().getValueFromCache(requestDataKey);
        if (identityMessageContext != null) {
            authenticationContext = (AuthenticationContext) identityMessageContext;
            //authenticationRequest is not the initial request , but this is subsequent request object that is not
            // going to store till end.
            authenticationContext.setIdentityRequest(authenticationRequest);
        }
        return authenticationContext;
    }

    /**
     * Do the validation of the request based on selected protocol.
     *
     * @param authenticationContext
     * @return
     * @throws AuthenticationHandlerException
     * @throws RequestHandlerException
     */
    protected FrameworkHandlerResponse validate(AuthenticationContext authenticationContext)
                                                        throws AuthenticationHandlerException, RequestHandlerException {
        AbstractRequestHandler protocolRequestHandler =
                HandlerManager.getInstance().getProtocolRequestHandler(authenticationContext);
        return protocolRequestHandler.validate(authenticationContext);
    }

    /**
     * Build successful response based on selected protocol.
     *
     * @param authenticationContext
     * @return
     * @throws FrameworkHandlerException
     */
    protected FrameworkHandlerResponse buildResponse(AuthenticationContext authenticationContext)
            throws FrameworkHandlerException {
        AbstractResponseHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseHandler(authenticationContext);
        return responseBuilderHandler.buildResponse(authenticationContext);
    }

    /**
     * Do authentication if there any authenticators.
     *
     * @param authenticationContext
     * @return
     * @throws AuthenticationHandlerException
     */
    protected FrameworkHandlerResponse authenticate(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationHandler authenticationHandler =
                HandlerManager.getInstance().getAuthenticationHandler(authenticationContext);
        return authenticationHandler.doAuthenticate(authenticationContext);
    }

    /**
     * Build Error Response based on
     *
     * @param identityException
     * @param authenticationContext
     * @return
     * @throws ResponseException
     */
    protected FrameworkHandlerResponse buildErrorResponse(AuthenticationContext authenticationContext,
                                                          IdentityException identityException) throws
                                                                                               ResponseException {
        AbstractResponseHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseHandler(authenticationContext);
        return responseBuilderHandler.buildErrorResponse(authenticationContext, identityException);
    }
}
