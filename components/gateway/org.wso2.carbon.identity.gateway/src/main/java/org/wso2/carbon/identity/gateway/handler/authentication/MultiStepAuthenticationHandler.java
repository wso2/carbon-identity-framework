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

package org.wso2.carbon.identity.gateway.handler.authentication;

import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.handler.GatewayEventHandler;
import org.wso2.carbon.identity.framework.handler.GatewayInvocationResponse;

import java.util.ArrayList;
import java.util.List;


public class MultiStepAuthenticationHandler extends GatewayEventHandler {

    private List<GatewayEventHandler> gatewayEventHandlers = new ArrayList<>();


    public void addIdentityGatewayEventHandler(GatewayEventHandler gatewayEventHandler) {
        this.gatewayEventHandlers.add(gatewayEventHandler);
    }

    @Override
    public GatewayInvocationResponse handle(IdentityMessageContext identityMessageContext) {
        for (GatewayEventHandler gatewayEventHandler : gatewayEventHandlers) {
            if (gatewayEventHandler.canHandle(identityMessageContext)) {
                setNextHandler(gatewayEventHandler);
                break;
            }
        }
        return GatewayInvocationResponse.CONTINUE;
    }

    @Override
    public boolean canHandle(IdentityMessageContext identityMessageContext) {
        return true;
    }
}
