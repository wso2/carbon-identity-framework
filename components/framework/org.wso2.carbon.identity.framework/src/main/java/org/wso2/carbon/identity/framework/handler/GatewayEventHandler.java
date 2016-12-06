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

import org.wso2.carbon.identity.framework.context.IdentityMessageContext;

import static org.wso2.carbon.identity.framework.handler.GatewayInvocationResponse.CONTINUE;

/**
 * Abstract representation of a handler executed within a gateway sequence.
 */
public abstract class GatewayEventHandler {

    private GatewayEventHandler prevHandler = null;
    private GatewayEventHandler nextHandler = null;

    public void setNextHandler(GatewayEventHandler nextHandler) {
        this.nextHandler = nextHandler;
        this.nextHandler.setPrevHandler(this);
    }

    public GatewayEventHandler getNextHandler() {
        return this.nextHandler;
    }

    protected void setPrevHandler(GatewayEventHandler prevHandler) {
        this.prevHandler = prevHandler;
    }

    public GatewayEventHandler getPrevHandler() {
        return this.prevHandler;
    }

    public void execute(IdentityMessageContext identityMessageContext) {
        GatewayInvocationResponse gatewayInvocationResponse = CONTINUE;

        if (canHandle(identityMessageContext)) {
            gatewayInvocationResponse = handle(identityMessageContext);
        }

        if (getNextHandler() != null && gatewayInvocationResponse.name().equals(CONTINUE.name())) {
            getNextHandler().execute(identityMessageContext);
        }
    }

    public abstract GatewayInvocationResponse handle(IdentityMessageContext identityMessageContext);

    public abstract boolean canHandle(IdentityMessageContext identityMessageContext);
}
