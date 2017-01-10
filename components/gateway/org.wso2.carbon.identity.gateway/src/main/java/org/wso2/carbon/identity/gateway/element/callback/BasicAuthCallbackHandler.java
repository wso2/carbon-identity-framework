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

import org.wso2.carbon.identity.framework.FrameworkConstants;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;


/**
 * I help to retrieve the sessionDataKey from the from the callback request received from the authentication endpoint.
 */
public class BasicAuthCallbackHandler extends AbstractCallbackHandler implements GatewayCallbackHandler {

    public BasicAuthCallbackHandler() {
    }


    @Override
    public boolean canHandle(GatewayMessageContext messageContext) {
        // need to say I can handle this request
        return true;
    }

    @Override
    public int getPriority() {

        return 100;
    }

    @Override
    public boolean canExtractSessionIdentifier(GatewayRequest request) {

        return request.getProperty(FrameworkConstants.SESSION_DATA_KEY) != null;
    }

    @Override
    public String getSessionIdentifier(GatewayRequest request) {

        return (String) request.getProperty(FrameworkConstants.SESSION_DATA_KEY);
    }
}
