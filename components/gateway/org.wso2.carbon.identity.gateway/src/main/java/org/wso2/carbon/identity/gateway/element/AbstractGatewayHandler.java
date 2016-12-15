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
import org.wso2.carbon.identity.framework.handler.HandlerConfig;
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.HandlerIdentifier;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;

public abstract class AbstractGatewayHandler<T1 extends HandlerIdentifier, T2 extends HandlerConfig>
        extends AbstractHandler<T1, T2, AbstractHandler, GatewayMessageContext> {


    public AbstractGatewayHandler(T1 handlerIdentifier) {

        super(handlerIdentifier);
    }

    @Override
    public void execute(GatewayMessageContext messageContext) throws HandlerException {

        messageContext.setCurrentHandler(this);
        super.execute(messageContext);
    }
}
