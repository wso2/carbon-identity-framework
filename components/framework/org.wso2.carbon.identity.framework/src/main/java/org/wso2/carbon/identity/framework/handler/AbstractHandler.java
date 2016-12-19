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

package org.wso2.carbon.identity.framework.handler;


import org.wso2.carbon.identity.framework.context.MessageContext;

import static org.wso2.carbon.identity.framework.handler.HandlerResponseStatus.CONTINUE;


public abstract class AbstractHandler<T1 extends HandlerIdentifier, T2 extends HandlerConfig,
        T3 extends AbstractHandler, T4 extends MessageContext> implements HandlerConfigurator<T1, T2> {

    private T1 handlerIdentifier = null;

    private T3 nextHandler = null;
    private T3 previousHandler = null;

    public AbstractHandler(T1 handlerIdentifier, T3 nextHandler) {

        this.handlerIdentifier = handlerIdentifier;
        this.nextHandler = nextHandler;
        this.setPreviousHandler((T3) this);
    }

    public AbstractHandler(T1 handlerIdentifier) {

        this.handlerIdentifier = handlerIdentifier;
    }

    public T3 getNextHandler() {

        return nextHandler;
    }

    public void setNextHandler(T3 nextHandler) {

        this.nextHandler = nextHandler;
    }

    public T3 getPreviousHandler() {

        return previousHandler;
    }

    public void setPreviousHandler(T3 previousHandler) {

        this.previousHandler = previousHandler;
    }

    public void execute(T4 messageContext) throws HandlerException {

        HandlerResponseStatus handlerResponseStatus = handle(messageContext);

        if (getNextHandler() != null && handlerResponseStatus == CONTINUE) {
            getNextHandler().execute(messageContext);
        }
    }

    @Override
    public T2 getConfiguration(T1 handlerIdentifier) {

        return null;
    }

    public abstract HandlerResponseStatus handle(T4 messageContext) throws HandlerException;


    public boolean canHandle(T4 messageContext) throws HandlerException {

        return true;
    }

    ;
}
