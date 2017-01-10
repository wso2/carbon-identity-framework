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

package org.wso2.carbon.identity.gateway.element.callback;

import org.wso2.carbon.identity.framework.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.gateway.artifact.model.HandlerConfig;
import org.wso2.carbon.identity.gateway.cache.GatewayContextCache;
import org.wso2.carbon.identity.gateway.cache.GatewayContextCacheKey;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.element.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.internal.DataHolder;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;
import org.wso2.carbon.identity.gateway.util.GatewayUtil;

import java.util.Optional;

/**
 * Abstract implementation of {@link GatewayCallbackHandler}
 */
public abstract class AbstractCallbackHandler extends AbstractHandler<GatewayMessageContext> {


    @Override
    public AbstractHandler nextHandler(GatewayMessageContext messageContext) {
        AbstractHandler nextHandler = null ;
        HandlerConfig currentHandler = messageContext.getCurrentHandler();
        if(currentHandler != null) {
            nextHandler = DataHolder.getInstance().getHandler(currentHandler.getName());
        }
        return nextHandler;
    }

    @Override
    public HandlerResponseStatus handle(GatewayMessageContext context) {

        String sessionDataKey = getSessionIdentifier(context.getCurrentIdentityRequest());

        // load the context
        GatewayMessageContext oldContext = Optional.ofNullable(
                GatewayContextCache.getInstance().get(new GatewayContextCacheKey(sessionDataKey)))
                .orElseThrow(() -> new FrameworkRuntimeException(
                        "Invalid SessionDataKey provided. Unable to find the persisted context for identifier : " +
                                sessionDataKey));

        // merge the new context with old context
        GatewayUtil.mergeContext(oldContext, context);

        // get the handler that should resume the flow.

        // set it as my next handler
        return HandlerResponseStatus.CONTINUE;
    }

    @Override
    public boolean canHandle(GatewayMessageContext messageContext) {

        return true;
    }

    public abstract int getPriority();

    public abstract boolean canExtractSessionIdentifier(GatewayRequest request);

    public abstract String getSessionIdentifier(GatewayRequest request);
}
