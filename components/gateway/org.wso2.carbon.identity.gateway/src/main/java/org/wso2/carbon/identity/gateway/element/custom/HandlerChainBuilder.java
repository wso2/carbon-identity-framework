/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.element.custom;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.framework.FrameworkException;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.gateway.GatewayHandlerIdentifier;
import org.wso2.carbon.identity.gateway.element.SessionDataCleanupHandler;
import org.wso2.carbon.identity.gateway.element.authentication.handler.BasicAuthenticationHandler;
import org.wso2.carbon.identity.gateway.element.authentication.handler.MultiOptionAuthenticationHandler;
import org.wso2.carbon.identity.gateway.element.authentication.handler.RequestPathAuthenticator;
import org.wso2.carbon.identity.gateway.element.response.SAMLResponseHandler;
import org.wso2.carbon.identity.gateway.element.validation.SAMLValidationHandler;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public class HandlerChainBuilder {

    private LinkedList<AbstractHandler> handlers = new LinkedList<>();

    /**
     * Builds the handler chain and returns the first handler of the chain.
     */
    public AbstractHandler buildHandlerChain(String serviceProviderName) throws FrameworkException {

        // TODO : building handler chain from file/artifact needs to be implemented.
        // we read the handler sequence from somewhere
        // fetch the identifier for each handler.
        GatewayHandlerIdentifier handlerIdentifier = new GatewayHandlerIdentifier();
        handlerIdentifier.setSpName(serviceProviderName);
        handlerIdentifier.setConfigName(null);
        handlerIdentifier.setHandlerName(null);

        // add the custom handlers to the chain
        if (StringUtils.equalsIgnoreCase(serviceProviderName, "wso2.com")) {
            addHandler(new CustomRequestValidator(handlerIdentifier))
                    .addHandler(
                            new MultiOptionAuthenticationHandler(handlerIdentifier)
                                    .addOption(new RequestPathAuthenticator(handlerIdentifier))
                                    .addOption(new BasicAuthenticationHandler(handlerIdentifier))
                    )
                    .addHandler(new CustomResponseBuilder(handlerIdentifier))
                    .addHandler(new SessionDataCleanupHandler(handlerIdentifier));
        } else {
            addHandler(new SAMLValidationHandler(handlerIdentifier))
                    .addHandler(new BasicAuthenticationHandler(handlerIdentifier))
                    .addHandler(new SAMLResponseHandler(handlerIdentifier))
                    .addHandler(new SessionDataCleanupHandler(handlerIdentifier));
        }

        for (int i = 0; i < handlers.size(); i++) {
            // we do this for all handlers except the last one
            if (i != handlers.size() - 1) {
                AbstractHandler thisHandler = handlers.get(i);
                AbstractHandler nextHandler = handlers.get(i + 1);

                if (thisHandler != null && nextHandler != null) {

                    // TODO : Design Problem : Multi Options handler and generics
                    if (thisHandler instanceof MultiOptionAuthenticationHandler) {
                        // set next of all options in multi option authentication handler.
                        ((MultiOptionAuthenticationHandler) thisHandler).getMultiOptionHandlers()
                                .forEach(x -> x.setNextHandler(nextHandler));
                    } else {
                        thisHandler.setNextHandler(nextHandler);
                        nextHandler.setPreviousHandler(thisHandler);
                    }
                }
            }
        }

        AbstractHandler firstHandler;
        try {
            firstHandler = handlers.element();
        } catch (NoSuchElementException ex) {
            throw new FrameworkException("Handler Chain is empty.");
        }

        return firstHandler;
    }


    public HandlerChainBuilder addHandler(AbstractHandler gatewayHandler) {

        handlers.add(gatewayHandler);
        return this;
    }


    private GatewayHandlerIdentifier getHandlerIdentifier(String spName, String configName, String handlerName) {

        return new GatewayHandlerIdentifier(spName, configName, handlerName);
    }


}
