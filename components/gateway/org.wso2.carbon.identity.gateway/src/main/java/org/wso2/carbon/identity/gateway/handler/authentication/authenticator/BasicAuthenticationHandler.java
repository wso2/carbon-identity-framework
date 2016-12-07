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

package org.wso2.carbon.identity.gateway.handler.authentication.authenticator;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.context.IdentityMessageContext;
import org.wso2.carbon.identity.framework.handler.GatewayEventHandler;
import org.wso2.carbon.identity.framework.handler.GatewayInvocationResponse;
import org.wso2.carbon.identity.framework.message.IdentityResponse;
import org.wso2.carbon.identity.framework.util.FrameworkUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.core.HttpHeaders;

public class BasicAuthenticationHandler extends GatewayEventHandler {

    private Logger logger = LoggerFactory.getLogger(BasicAuthenticationHandler.class);

    // TODO : Read from config
    private final String AUTH_ENDPOINT = "http://localhost:9090/authenticate";
    private final String CALLBACK = "http://localhost:9090/identity/callback";


    @Override
    public GatewayInvocationResponse handle(IdentityMessageContext context) {

        String sessionID = FrameworkUtil.getSessionIdentifier(context);

        if (StringUtils.isNotBlank(sessionID)) {
            IdentityResponse response = context.getIdentityResponse();

            // build the authentication endpoint url
            String redirectUrl = buildAuthenticationEndpointURL(AUTH_ENDPOINT, sessionID, CALLBACK);
            response.setStatusCode(302);
            response.addHeader(HttpHeaders.LOCATION, redirectUrl);

            return GatewayInvocationResponse.REDIRECT;

        } else {
            logger.error("Session Context Information Not Available.");
            return GatewayInvocationResponse.ERROR;
        }
    }

    @Override
    public boolean canHandle(IdentityMessageContext identityMessageContext) {
        return true;
    }


    private static String buildAuthenticationEndpointURL(String url, String state, String callback) {

        if (StringUtils.isNotBlank(state)) {
            url = url + "?state=" + state;
        }

        if (StringUtils.isNotBlank(callback)) {
            try {
                url = url + "&callback=" + URLEncoder.encode(callback, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                url = null;
            }
        }
        return url;
    }
}
