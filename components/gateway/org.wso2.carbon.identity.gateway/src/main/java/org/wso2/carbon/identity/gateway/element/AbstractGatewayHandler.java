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

import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.gateway.artifact.model.HandlerConfig;
import org.wso2.carbon.identity.gateway.artifact.model.ServiceProvider;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.internal.DataHolder;

public abstract class AbstractGatewayHandler<T1>
        extends AbstractHandler<GatewayMessageContext> {

    private HandlerConfig handlerConfig = null ;

    public AbstractGatewayHandler() {
    }

    public AbstractGatewayHandler(AbstractHandler nextHandler) {
        super(nextHandler);
    }

    @Override
    public AbstractHandler nextHandler(GatewayMessageContext messageContext) {
        AbstractHandler nextHandler = null ;
        HandlerConfig currentHandler = messageContext.getCurrentHandler();
        if(currentHandler != null) {
            HandlerConfig nextHandler1 = currentHandler.getNextHandler();
            if(nextHandler1 != null) {
                messageContext.setCurrentHandler(nextHandler1);
                nextHandler = DataHolder.getInstance().getHandler(nextHandler1.getName());
            }
        }
        return nextHandler;
    }

    @Override
    public HandlerResponseStatus handle(GatewayMessageContext messageContext) throws HandlerException {

        return HandlerResponseStatus.CONTINUE;
    }

    @Override
    public void execute(GatewayMessageContext gatewayMessageContext) throws HandlerException {
        //gatewayMessageContext.setCurrentHandler(this);
        super.execute(gatewayMessageContext);

    }

    public HandlerConfig getHandlerConfig() {
        return handlerConfig;
    }

    public void setHandlerConfig(HandlerConfig handlerConfig) {
        this.handlerConfig = handlerConfig;
    }

    @Override
    public boolean canHandle(GatewayMessageContext messageContext) throws HandlerException {

        return super.canHandle(messageContext);
    }


}
