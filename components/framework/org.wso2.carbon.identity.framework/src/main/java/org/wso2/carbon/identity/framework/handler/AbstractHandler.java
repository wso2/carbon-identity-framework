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


public abstract class AbstractHandler<T1 extends MessageContext> {


    public AbstractHandler() {
    }

    public AbstractHandler(AbstractHandler nextHandler) {

    }

    public abstract AbstractHandler nextHandler(T1 messageContext);


    public void execute(T1 messageContext) throws HandlerException {

        HandlerResponseStatus handlerResponseStatus = handle(messageContext);
        if(handlerResponseStatus == CONTINUE) {
            AbstractHandler nextHandler = nextHandler(messageContext);
            if (nextHandler != null) {
                nextHandler.execute(messageContext);
            }
        }
    }


    public abstract HandlerResponseStatus handle(T1 messageContext) throws HandlerException;


    public boolean canHandle(T1 messageContext) throws HandlerException {

        return true;
    }

}
