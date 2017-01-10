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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.FrameworkClientException;
import org.wso2.carbon.identity.framework.FrameworkException;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.gateway.GatewayProcessor;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.element.callback.AbstractCallbackHandler;
import org.wso2.carbon.identity.gateway.element.callback.GatewayCallbackHandler;
import org.wso2.carbon.identity.gateway.internal.DataHolder;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;
import org.wso2.carbon.identity.gateway.message.GatewayResponse;

/**
 * Handle callbacks coming into the Identity Gateway
 */
public class CallbackProcessor extends GatewayProcessor {


    private Logger logger = LoggerFactory.getLogger(CallbackProcessor.class);

    @Override
    public GatewayResponse process(GatewayRequest identityRequest) throws FrameworkException {

        if (logger.isDebugEnabled()) {
            logger.debug(getName() + " starting to process the initial Identity Request.");
        }

        // get registered callback handlers.
        AbstractCallbackHandler callbackHandler = DataHolder.getInstance().getGatewayCallbackHandlers()
                .stream()
                .filter(x -> x.canExtractSessionIdentifier(identityRequest))
                .findFirst()
                .orElseThrow(() -> new FrameworkException("Unable to find a handler to process the callback"));

        // restore the old context.
        String sessionDataKey = callbackHandler.getSessionIdentifier(identityRequest);
        if (StringUtils.isBlank(sessionDataKey)) {
            throw new FrameworkClientException("SessionDataKey not found in the request to correlate.");
        }

        GatewayMessageContext newContext = new GatewayMessageContext(identityRequest);
        ((AbstractHandler) callbackHandler).execute(newContext);

        return newContext.getCurrentIdentityResponse();
    }

    @Override
    public String getName() {
        return "CallbackProcessor";
    }

    @Override
    public int getPriority() {
        return 500;
    }

    @Override
    public boolean canHandle(GatewayRequest identityRequest) {
        // if the request url contains identity/callback
        return identityRequest.getRequestUri().contains("callback");
    }

}
