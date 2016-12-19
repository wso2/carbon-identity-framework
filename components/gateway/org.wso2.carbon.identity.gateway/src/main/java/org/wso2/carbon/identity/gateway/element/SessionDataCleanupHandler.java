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

package org.wso2.carbon.identity.gateway.element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.handler.HandlerIdentifier;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.gateway.cache.IdentityMessageContextCache;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;

/**
 * I cleanup the session data after the handle sequence is ended.
 */
public class SessionDataCleanupHandler extends AbstractGatewayHandler {

    private static final Logger logger = LoggerFactory.getLogger(SessionDataCleanupHandler.class);


    public SessionDataCleanupHandler(HandlerIdentifier handlerIdentifier) {

        super(handlerIdentifier);
    }


    @Override
    public HandlerResponseStatus handle(GatewayMessageContext context) {

        // All I do is to clean up the current context from cache.
        String sessionDataKey = context.getSessionDataKey();
        IdentityMessageContextCache.getInstance().clear(sessionDataKey);

        if (logger.isDebugEnabled()) {
            logger.debug("Clean up session data for key : " + sessionDataKey);
        }


        return HandlerResponseStatus.CONTINUE;
    }

    @Override
    public boolean canHandle(GatewayMessageContext messageContext) {

        return true;
    }
}
