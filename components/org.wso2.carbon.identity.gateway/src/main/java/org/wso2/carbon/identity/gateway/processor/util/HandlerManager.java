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
package org.wso2.carbon.identity.gateway.processor.util;


import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandler;
import org.wso2.carbon.identity.gateway.processor.handler.request.AbstractRequestHandler;
import org.wso2.carbon.identity.gateway.processor.handler.response.AbstractResponseHandler;

import java.util.List;

public class HandlerManager {

    private static volatile HandlerManager instance = new HandlerManager();

    private HandlerManager() {

    }

    public static HandlerManager getInstance() {
        return instance;
    }


    public AuthenticationHandler getAuthenticationHandler(GatewayMessageContext messageContext) {
        List<AuthenticationHandler> authenticationHandlers =
                FrameworkServiceDataHolder.getInstance().getAuthenticationHandlers();
        if(authenticationHandlers != null) {
            for (AuthenticationHandler authenticationHandler : authenticationHandlers) {
                if (authenticationHandler.canHandle(messageContext)) {
                    return authenticationHandler;
                }
            }
        }
        throw new GatewayRuntimeException("Cannot find AuthenticationHandler to handle this request.");
    }


    public AbstractResponseHandler getResponseHandler(GatewayMessageContext messageContext) {
        List<AbstractResponseHandler> responseBuilderHandlers =
                FrameworkServiceDataHolder.getInstance().getResponseHandlers();
        if(responseBuilderHandlers != null) {
            for (AbstractResponseHandler responseBuilderHandler : responseBuilderHandlers) {
                if (responseBuilderHandler.canHandle(messageContext)) {
                    return responseBuilderHandler;
                }
            }
        }
        throw new GatewayRuntimeException("Cannot find AbstractResponseHandler to handle this request.");
    }


    public AbstractRequestHandler getProtocolRequestHandler(GatewayMessageContext messageContext) {
        List<AbstractRequestHandler> protocolRequestHandlers =
                FrameworkServiceDataHolder.getInstance().getRequestHandlers();
        if(protocolRequestHandlers != null) {
            for (AbstractRequestHandler protocolRequestHandler : protocolRequestHandlers) {
                if (protocolRequestHandler.canHandle(messageContext)) {
                    return protocolRequestHandler;
                }
            }
        }
        throw new GatewayRuntimeException("Cannot find AbstractRequestHandler to handle this request.");
    }
}
