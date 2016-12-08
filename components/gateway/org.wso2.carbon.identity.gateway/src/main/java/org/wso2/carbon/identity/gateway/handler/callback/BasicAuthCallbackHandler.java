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

package org.wso2.carbon.identity.gateway.handler.callback;

import org.wso2.carbon.identity.framework.cache.IdentityMessageContextCache;
import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.handler.GatewayEventHandler;
import org.wso2.carbon.identity.framework.handler.GatewayInvocationResponse;
import org.wso2.carbon.identity.framework.util.FrameworkUtil;

import java.util.Optional;


public class BasicAuthCallbackHandler extends GatewayCallbackHandler {

    @Override
    public GatewayInvocationResponse handle(IdentityMessageContext context) {

        // identify the state value
        String state = String.valueOf(
                Optional.ofNullable(context.getCurrentIdentityRequest().getProperty("state"))
                        .orElseThrow(() -> new RuntimeException("Unable to process the callback for Basic Auth."))
        );

        // load the context
        IdentityMessageContext oldContext = Optional.ofNullable(IdentityMessageContextCache.getInstance().get(state))
                .orElseThrow(() -> new RuntimeException("Unable to find the persisted context for identifier : " +
                        state));

        // merge the new context with old context
        FrameworkUtil.mergeContext(context, oldContext);

        // get the handler that should resume the flow.
        GatewayEventHandler nextHandler = context.getCurrentHandler();

        // set it as my next handler
        this.setNextHandler(nextHandler);
        return GatewayInvocationResponse.CONTINUE;
    }


    @Override
    public boolean canHandle(IdentityMessageContext identityMessageContext) {
        // need to say I can handle this request
        return true;
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
