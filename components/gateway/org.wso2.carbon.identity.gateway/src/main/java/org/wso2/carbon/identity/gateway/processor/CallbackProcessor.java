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

import org.wso2.carbon.identity.framework.IdentityProcessor;
import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.exception.FrameworkException;
import org.wso2.carbon.identity.framework.message.IdentityRequest;
import org.wso2.carbon.identity.framework.message.IdentityResponse;
import org.wso2.carbon.identity.gateway.handler.callback.GatewayCallbackHandler;
import org.wso2.carbon.identity.gateway.internal.DataHolder;

/**
 * Handle callbacks coming into the Identity Gateway
 */
public class CallbackProcessor extends IdentityProcessor {


    @Override
    public IdentityResponse process(IdentityRequest identityRequest) throws FrameworkException {

        IdentityMessageContext context = new IdentityMessageContext(identityRequest, new IdentityResponse());

        // get registered callback handlers.
        GatewayCallbackHandler handler = DataHolder.getInstance().getGatewayCallbackHandlers()
                .stream()
                .filter(x -> x.canHandle(context))
                .findFirst()
                .orElseThrow(() -> new FrameworkException("Unable to find a handler to process the callback"));

        handler.execute(context);
        return context.getIdentityResponse();
    }

    @Override
    public String getName() {

        return "CallbackProcessor";
    }

    @Override
    public int getPriority() {

        return 50;
    }

    @Override
    public boolean canHandle(IdentityRequest identityRequest) {
        // if the request url contains identity/callback
        return identityRequest.getRequestURI().contains("callback");
    }


}
