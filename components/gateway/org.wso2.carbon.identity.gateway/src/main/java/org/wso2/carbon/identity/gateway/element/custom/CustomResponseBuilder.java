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
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.element.AbstractGatewayHandler;

import java.util.HashMap;
import java.util.Map;

public class CustomResponseBuilder extends AbstractGatewayHandler {



    @Override
    public HandlerResponseStatus handle(GatewayMessageContext gatewayMessageContext) throws HandlerException {

        String sessionID = gatewayMessageContext.getSessionDataKey();
        if (StringUtils.isBlank(sessionID)) {
            throw new HandlerException("Cannot find the sessionDataKey to correlate the request.");
        }

        // read the authentication context details from context.
        Map<String, Object> contextMap = (Map<String, Object>) gatewayMessageContext.getParameter(sessionID);
        String subject = (String) contextMap.getOrDefault("subject", null);
        Map<String, String> claimMap = (Map<String, String>) contextMap.getOrDefault("claims", new HashMap<>());

        // build the response.
        Map<String, String> responseMap = new HashMap<String, String>() {
            {
                put("authenticatedUser", subject);
                putAll(claimMap);
            }
        };

        gatewayMessageContext.getCurrentIdentityResponse().setStatusCode(200);
        gatewayMessageContext.getCurrentIdentityResponse().setBody("Success, Authenticated User : " + subject);
        return HandlerResponseStatus.CONTINUE;
    }
}
