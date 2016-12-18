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

package org.wso2.carbon.identity.gateway.processor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.FrameworkException;
import org.wso2.carbon.identity.gateway.cache.IdentityMessageContextCache;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.element.custom.HandlerChainBuilder;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;
import org.wso2.carbon.identity.gateway.message.GatewayResponse;

import java.util.HashMap;


/*
    This processor handler the initial identity requests that comes to the Identity Gateway.
 */
public class InitRequestProcessor extends GatewayProcessor {

    private static final Logger log = LoggerFactory.getLogger(InitRequestProcessor.class);

    @Override
    public GatewayResponse process(GatewayRequest identityRequest) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug(getName() + " starting to process the initial Identity Request.");
        }

        // Build the message context
        GatewayMessageContext gatewayMessageContext = new GatewayMessageContext(identityRequest);
        gatewayMessageContext.setInitialIdentityRequest(identityRequest);

        // Create a sessionDataKey and add to message context
        String sessionDataKey = gatewayMessageContext.getSessionDataKey();

        // add the context reference to cache.
        IdentityMessageContextCache.getInstance().put(sessionDataKey, gatewayMessageContext);

        // Add an authentication context map TODO: this should be initialized by the context
        gatewayMessageContext.addParameter(sessionDataKey, new HashMap<>());

        String serviceProvider = identityRequest.getServiceProvider();

        // Initiate the handler chain
        new HandlerChainBuilder().buildHandlerChain(serviceProvider).execute(gatewayMessageContext);
        return gatewayMessageContext.getCurrentIdentityResponse();
    }

    @Override
    public String getName() {

        return "InitialRequestProcessor";
    }

    @Override
    public int getPriority() {

        return 100;
    }

    @Override
    public boolean canHandle(GatewayRequest identityRequest) {

        return true;
    }
}
