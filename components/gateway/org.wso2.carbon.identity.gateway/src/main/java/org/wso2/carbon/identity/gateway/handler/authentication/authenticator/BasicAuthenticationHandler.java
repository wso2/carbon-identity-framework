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
import org.wso2.carbon.identity.framework.handler.GatewayHandlerStatus;
import org.wso2.carbon.identity.framework.handler.GatewayInvocationResponse;
import org.wso2.carbon.identity.framework.handler.auth.Authenticator;
import org.wso2.carbon.identity.framework.message.IdentityResponse;
import org.wso2.carbon.identity.framework.model.User;
import org.wso2.carbon.identity.framework.model.UserClaim;
import org.wso2.carbon.identity.framework.util.FrameworkUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;

public class BasicAuthenticationHandler extends GatewayEventHandler implements Authenticator {

    private Logger logger = LoggerFactory.getLogger(BasicAuthenticationHandler.class);

    // TODO : Read from config
    private final String AUTH_ENDPOINT = "http://localhost:9090/authenticate";
    private final String CALLBACK = "http://localhost:9090/identity/callback";

    private final String USERNAME = "username";
    private final String PASSWORD = "password";

    protected String uniqueIdentifier = UUID.randomUUID().toString();
    protected boolean isUserAuthenticated = false;
    protected User authenticatedUser;
    private Map<String, UserClaim> claimMap = new HashMap<>();

    public BasicAuthenticationHandler() {

        uniqueIdentifier = UUID.randomUUID().toString();
        isUserAuthenticated = false;
        authenticatedUser = null;
        claimMap = new HashMap<>();
    }

    @Override
    public GatewayInvocationResponse handle(IdentityMessageContext context) {

        String sessionID = FrameworkUtil.getSessionIdentifier(context);
        if (StringUtils.isNotBlank(sessionID)) {

            if (isCallback(context)) {
                // this is a callback request so we try to authenticate the user.
                Map<String, Object> properties = context.getCurrentIdentityRequest().getProperties();
                return handleAuthentication(
                        String.valueOf(properties.get(USERNAME)),
                        String.valueOf(properties.get(PASSWORD)),
                        (Map<String, Object>) context.getParameter(sessionID)
                );


            } else {
                // This is an initial request to the Basic Auth Handler, so redirect to login page.
                IdentityResponse response = context.getIdentityResponse();

                // build the authentication endpoint url
                String redirectUrl = buildAuthenticationEndpointURL(AUTH_ENDPOINT, sessionID, CALLBACK);
                response.setStatusCode(302);
                response.addHeader(HttpHeaders.LOCATION, redirectUrl);

                context.setCurrentHandlerStatus(GatewayHandlerStatus.INCOMPLETE);
                return GatewayInvocationResponse.REDIRECT;
            }

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


    private GatewayInvocationResponse handleAuthentication(String username,
                                                           String password,
                                                           Map<String, Object> authContextMap) {

        if ("admin".equalsIgnoreCase(username) && "admin".equalsIgnoreCase(password)) {
            // authenticated, lets set the subject and claims
            authContextMap.put("subject", username);

            Map<String, String> claimMap = new HashMap<>();
            claimMap.put("role", "admin");
            claimMap.put("email", "admin@wso2.com");
            authContextMap.put("claims", claimMap);

            isUserAuthenticated = true;
            return GatewayInvocationResponse.CONTINUE;
        } else {
            return GatewayInvocationResponse.ERROR;
        }

    }

    @Override
    public boolean isSubjectStep() {

        return true;
    }

    @Override
    public boolean isAttributeStep() {

        return true;
    }

    @Override
    public String getUniqueIdentifier() {

        return uniqueIdentifier;
    }

    @Override
    public boolean isCallback(IdentityMessageContext context) {

        Map<String, Object> properties = context.getCurrentIdentityRequest().getProperties();
        return properties.containsKey(USERNAME) && properties.containsKey(PASSWORD);
    }

    @Override
    public boolean isAuthenticated() {

        return isUserAuthenticated;
    }

    @Override
    public Map<String, UserClaim> getUserClaims() {

        return claimMap;
    }

    @Override
    public User getAuthenticatedUser() {

        return authenticatedUser;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {

        this.uniqueIdentifier = uniqueIdentifier;
    }

    public void setUserAuthenticated(boolean userAuthenticated) {

        isUserAuthenticated = userAuthenticated;
    }

    public void setAuthenticatedUser(User authenticatedUser) {

        this.authenticatedUser = authenticatedUser;
    }

    public void setClaimMap(Map<String, UserClaim> claimMap) {

        this.claimMap = claimMap;
    }
}
