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
package org.wso2.carbon.identity.gateway.api.handler;


import org.wso2.carbon.identity.common.base.handler.AbstractMessageHandler;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;

import java.util.List;

/**
 * AbstractGatewayHandler is the root handler class to the gateway. All the handlers should extends this class.
 * Except that, this has AbstractMessageHandler as the super class and all the event based common functionality will
 * be inherited from that.
 *
 * @param <T>
 */
public abstract class AbstractGatewayHandler<T extends MessageContext> extends
        AbstractMessageHandler {

    /**
     * Default canHandle method for all the generic handlers.
     *
     * @param messageContext
     * @return
     */
    public abstract boolean canHandle(T messageContext);

    public AbstractGatewayHandler getHandler(List<? extends AbstractGatewayHandler> frameworkHandlers,
                                             MessageContext messageContext) {
        if (frameworkHandlers != null) {
            for (AbstractGatewayHandler abstractGatewayHandler : frameworkHandlers) {
                if (abstractGatewayHandler.canHandle(messageContext)) {
                    return abstractGatewayHandler;
                }
            }
        }
        String errorMessage = "Cannot find a Handler to handle this request, getHandler";
        throw new GatewayRuntimeException(errorMessage);
    }
}
