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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.FrameworkConstants;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.framework.handler.HandlerConfig;
import org.wso2.carbon.identity.framework.handler.HandlerException;
import org.wso2.carbon.identity.framework.handler.HandlerIdentifier;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.framework.message.IdentityResponse;
import org.wso2.carbon.identity.framework.model.User;
import org.wso2.carbon.identity.framework.model.UserClaim;
import org.wso2.carbon.identity.gateway.context.GatewayMessageContext;
import org.wso2.carbon.identity.gateway.element.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.element.authentication.Authenticator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;

/**
 * Basic(Username, Password) authentication handler.
 */
public class BasicAuthenticationHandler<T1 extends HandlerIdentifier,
        T2 extends HandlerConfig, T3 extends AbstractHandler, T4 extends GatewayMessageContext>
        extends AbstractHandler<T1,T2,T3,T4> implements Authenticator {

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

    public BasicAuthenticationHandler(T1 handlerIdentifier) {
        super(handlerIdentifier);

        isUserAuthenticated = false;
        authenticatedUser = null;
        claimMap = new HashMap<>();
    }


    @Override
    public T2 getConfiguration(T1 handlerIdentifier) {
        return null;
    }

    @Override
    public HandlerResponseStatus handle(T4 context) throws HandlerException{

        String sessionID = context.getSessionDataKey();
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

                context.setHandlerResponseStatus(HandlerResponseStatus.SUSPEND);
                return HandlerResponseStatus.SUSPEND;
            }

        } else {
            logger.error("Session Context Information Not Available.");
            throw new AuthenticationHandlerException("Session Context Information Not Available.");
        }
    }


    private static String buildAuthenticationEndpointURL(String url, String state, String callback) {

        if (StringUtils.isNotBlank(state)) {
            url = url + "?" + FrameworkConstants.SESSION_DATA_KEY + "=" + state;
        }

        if (StringUtils.isNotBlank(callback)) {
            try {
                url = url + "&" + FrameworkConstants.CALLBACK + "=" +
                        URLEncoder.encode(callback, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                url = null;
            }
        }
        return url;
    }

    private HandlerResponseStatus handleAuthentication(String username,
                                                       String password,
                                                       Map<String, Object> authContextMap) throws AuthenticationHandlerException{

        if ("admin".equalsIgnoreCase(username) && "admin".equalsIgnoreCase(password)) {
            // authenticated, lets set the subject and claims
            authContextMap.put("subject", username);

            Map<String, String> claimMap = new HashMap<>();
            claimMap.put("role", "admin");
            claimMap.put("email", "admin@wso2.com");
            authContextMap.put("claims", claimMap);

            isUserAuthenticated = true;
            return HandlerResponseStatus.CONTINUE;
        } else {
            throw new AuthenticationHandlerException("Session Context Information Not Available.");
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



    public boolean isCallback(GatewayMessageContext context) {

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
