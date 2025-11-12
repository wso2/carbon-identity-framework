/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.external.api.token.handler.internal.util;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientRequestException;
import org.wso2.carbon.identity.external.api.client.api.model.APIAuthentication;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenRequestException;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext.Property;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenRequestContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building token request related objects.
 */
public class TokenRequestBuilderUtils {

    private static final Log LOG = LogFactory.getLog(TokenRequestBuilderUtils.class);

    private TokenRequestBuilderUtils() {
    }

    /**
     * Builds an APIRequestContext for acquiring an access token.
     *
     * @param requestContext The context containing details of the token request.
     * @return An APIRequestContext configured for the token request.
     * @throws TokenRequestException If an error occurs while building the APIRequestContext.
     */
    public static APIRequestContext buildAPIRequestContext(TokenRequestContext requestContext)
            throws TokenRequestException {

        return buildBasicAPIRequestContext(requestContext, buildTokenRequestPayload(requestContext));
    }

    /**
     * Builds an APIRequestContext for acquiring an access token using a refresh token.
     *
     * @param requestContext The context containing details of the token request.
     * @param refreshToken   The refresh token to be used for acquiring a new access token.
     * @return An APIRequestContext configured for the refresh token grant.
     * @throws TokenRequestException If an error occurs while building the APIRequestContext.
     */
    public static APIRequestContext buildAPIRequestContextForRefreshGrant(
            TokenRequestContext requestContext, String refreshToken) throws TokenRequestException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Building APIRequestContext for refresh token grant.");
        }
        JsonObject json = new JsonObject();
        json.addProperty("grant_type", "refresh_token");
        json.addProperty("refresh_token", refreshToken);
        String payload = json.toString();
        return buildBasicAPIRequestContext(requestContext, payload);
    }

    private static APIRequestContext buildBasicAPIRequestContext(TokenRequestContext requestContext, String payload)
            throws TokenRequestException {

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Building basic APIRequestContext for endpoint: " +
                        requestContext.getTokenEndpointUrl());
            }
            APIAuthentication authentication = TokenRequestBuilderUtils
                    .buildTokenRequestAPIAuthentication(requestContext);
            APIRequestContext.Builder requestContextBuilder = new APIRequestContext.Builder()
                    .httpMethod(APIRequestContext.HttpMethod.POST)
                    .headers(requestContext.getHeaders())
                    .endpointUrl(requestContext.getTokenEndpointUrl())
                    .apiAuthentication(authentication)
                    .payload(payload);
            APIRequestContext context = requestContextBuilder.build();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully built APIRequestContext for token request.");
            }
            return context;
        } catch (APIClientRequestException e) {
            throw new TokenRequestException("Error building API Request Context for token request.", e);
        }
    }

    private static APIAuthentication buildTokenRequestAPIAuthentication(TokenRequestContext requestContext)
            throws TokenRequestException {

        GrantContext grantContext = requestContext.getGrantContext();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Building API authentication for grant type: " + grantContext.getGrantType());
        }
        try {
            switch (grantContext.getGrantType()) {
                case CLIENT_CREDENTIAL:
                    Map<String, String> authProperties = new HashMap<>();
                    authProperties.put(APIAuthentication.Property.USERNAME.getName(),
                            grantContext.getProperty(Property.CLIENT_ID.getName()));
                    authProperties.put(APIAuthentication.Property.PASSWORD.getName(),
                            grantContext.getProperty(Property.CLIENT_SECRET.getName()));
                    APIAuthentication authentication = new APIAuthentication.Builder()
                            .authType(APIAuthentication.AuthType.BASIC)
                            .properties(authProperties)
                            .build();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Successfully built API authentication for CLIENT_CREDENTIAL grant type.");
                    }
                    return authentication;
                default:
                    throw new TokenRequestException("Unsupported authentication type: " + grantContext.getGrantType());
            }
        } catch (APIClientRequestException e) {
            throw new TokenRequestException("Error building API Authentication for grant type: " +
                    grantContext.getGrantType(), e);
        }
    }

    private static String buildTokenRequestPayload(TokenRequestContext requestContext) throws TokenRequestException {

        GrantContext grantContext = requestContext.getGrantContext();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Building token request payload for grant type: " + grantContext.getGrantType());
        }
        switch (grantContext.getGrantType()) {
            case CLIENT_CREDENTIAL:
                JsonObject json = new JsonObject();
                json.addProperty("grant_type", "client_credentials");
                json.addProperty("client_id", grantContext.getProperty(Property.CLIENT_ID.getName()));
                json.addProperty("client_secret", grantContext.getProperty(Property.CLIENT_SECRET.getName()));
                json.addProperty("scope", grantContext.getProperty(Property.SCOPE.getName()));
                String payload = json.toString();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Successfully built token request payload for CLIENT_CREDENTIAL grant type.");
                }
                return payload;
            default:
                throw new TokenRequestException("Unsupported authentication type: " + grantContext.getGrantType());
        }
    }
}
