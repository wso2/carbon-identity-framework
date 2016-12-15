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

import org.wso2.carbon.identity.framework.cache.IdentityMessageContextCache;
import org.wso2.carbon.identity.framework.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;

import java.util.Optional;

/**
 * Abstract implementation of {@link GatewayCallbackHandler}
 */
public abstract class AbstractAbstractCallbackHandler extends AbstractHandler implements GatewayCallbackHandler {


    @Override
    public HandlerResponseStatus handle(MessageContext context) {

        String sessionDataKey = getSessionIdentifier(context.getCurrentIdentityRequest());

        // load the context
        MessageContext oldContext = Optional.ofNullable(IdentityMessageContextCache.getInstance().get(sessionDataKey))
                .orElseThrow(() -> new FrameworkRuntimeException(
                        "Invalid SessionDataKey provided. Unable to find the persisted context for identifier : " +
                                sessionDataKey));

        // merge the new context with old context
        FrameworkUtil.mergeContext(context, oldContext);

        // get the handler that should resume the flow.
        AbstractHandler nextHandler = context.getCurrentHandler();

        // set it as my next handler
        this.setNextHandler(nextHandler);
        return HandlerResponseStatus.CONTINUE;
    }

    @Override
    public boolean canHandle(MessageContext messageContext) {

        return true;
    }
}
