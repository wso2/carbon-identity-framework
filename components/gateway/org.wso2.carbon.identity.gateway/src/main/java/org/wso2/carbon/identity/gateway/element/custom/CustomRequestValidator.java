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

package org.wso2.carbon.identity.gateway.element.custom;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.element.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;

public class CustomRequestValidator extends AbstractGatewayHandler {


    private Logger logger = LoggerFactory.getLogger(CustomRequestValidator.class);

    @Override
    public HandlerResponseStatus handle(GatewayMessageContext messageContext) throws HandlerException {

        final String PROTOCOL = "protocol";
        final String WSO2 = "wso2";
        GatewayRequest gatewayRequest = messageContext.getCurrentIdentityRequest();

        if (StringUtils.equalsIgnoreCase(WSO2, gatewayRequest.getHeaders().get(PROTOCOL)) ||
                StringUtils.equalsIgnoreCase(WSO2, (String) gatewayRequest.getProperties().get(PROTOCOL))) {

            if (logger.isDebugEnabled()) {
                logger.debug("Custom Protocol WSO2 Validated Successfully.");
            }
            return HandlerResponseStatus.CONTINUE;
        }

        throw new HandlerException("Invalid Request for Custom Protocol WSO2.");
    }
}
