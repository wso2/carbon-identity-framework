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

package org.wso2.carbon.identity.gateway.element.authentication.handler;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.framework.FrameworkException;
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.gateway.GatewayHandlerIdentifier;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;
import org.wso2.carbon.identity.gateway.util.GatewayUtil;

import java.util.Base64;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RequestPathAuthenticator extends BasicAuthenticationHandler {

    public RequestPathAuthenticator(GatewayHandlerIdentifier handlerIdentifier) {
        super(handlerIdentifier);
    }

    @Override
    public HandlerResponseStatus handle(GatewayMessageContext context) throws HandlerException {

        final String SEC_TOKEN = "secToken";

        GatewayRequest gatewayRequest = context.getCurrentIdentityRequest();
        if (!gatewayRequest.getProperties().containsKey(SEC_TOKEN)) {
            // cannot do request path authentication
            throw new HandlerException("Bad Request Path Authentication Request. No 'secToken' parameter " +
                    "found in the request.");
        }

        String secToken = (String) gatewayRequest.getProperty(SEC_TOKEN);
        if (StringUtils.isBlank(secToken)) {
            throw new HandlerException("Invalid 'secToken' parameter. Cannot be null or empty.");
        }

        String base64DecodedString = new String(Base64.getDecoder().decode(secToken.getBytes()), UTF_8);
        String[] requestPathParams = base64DecodedString.split(":");

        if (requestPathParams.length != 2) {
            throw new HandlerException("Required number of parameters not found in the 'secToken' param.");
        }

        String sessionID;
        try {
            sessionID = GatewayUtil.getSessionDataKeyFromContext(context);
        } catch (FrameworkException e) {
            throw new HandlerException(e.getMessage());
        }

        authenticate(requestPathParams[0], requestPathParams[1], (Map<String, Object>) context.getParameter(sessionID));
        return isAuthenticated() ? HandlerResponseStatus.CONTINUE : HandlerResponseStatus.SUSPEND;
    }


}
