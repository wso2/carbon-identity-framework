/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientRequestException;
import org.wso2.carbon.identity.external.api.client.api.model.APIAuthentication;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.token.handler.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenRequestException;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext.Property;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenRequestContext;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for building token request related objects.
 */
public class TokenRequestBuilderUtils {

    private static final Log LOG = LogFactory.getLog(TokenRequestBuilderUtils.class);
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String CONTENT_TYPE_FORM_URL_ENCODED = "application/x-www-form-urlencoded";
    private static final String ACCEPT_HEADER_VALUE = "application/json";

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

        LOG.debug("Building APIRequestContext for refresh token grant.");

        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(new BasicNameValuePair("grant_type", "refresh_token"));
        formParams.add(new BasicNameValuePair("refresh_token", refreshToken));

        return buildBasicAPIRequestContext(
                requestContext, new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8));
    }

    private static APIRequestContext buildBasicAPIRequestContext(TokenRequestContext requestContext, HttpEntity payload)
            throws TokenRequestException {

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Building basic APIRequestContext for endpoint: " +
                        requestContext.getTokenEndpointUrl());
            }
            Map<String, String> headers = new HashMap<>(requestContext.getHeaders());
            headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_FORM_URL_ENCODED);
            headers.put(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
            APIAuthentication authentication = TokenRequestBuilderUtils
                    .buildTokenRequestAPIAuthentication(requestContext);
            APIRequestContext.Builder requestContextBuilder = new APIRequestContext.Builder()
                    .httpMethod(APIRequestContext.HttpMethod.POST)
                .headers(headers)
                    .endpointUrl(requestContext.getTokenEndpointUrl())
                    .apiAuthentication(authentication)
                    .payload(payload);
            APIRequestContext context = requestContextBuilder.build();
            LOG.debug("Successfully built APIRequestContext for token request.");
            return context;
        } catch (APIClientRequestException e) {
            throw new TokenRequestException(ErrorMessage.ERROR_CODE_BUILDING_API_REQUEST, null, e);
        }
    }

    private static APIAuthentication buildTokenRequestAPIAuthentication(TokenRequestContext requestContext)
            throws TokenRequestException {

        GrantContext grantContext = requestContext.getGrantContext();
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
                    throw new TokenRequestException(
                            ErrorMessage.ERROR_CODE_UNSUPPORTED_GRANT_TYPE, grantContext.getGrantType().name());
            }
        } catch (APIClientRequestException e) {
            throw new TokenRequestException(ErrorMessage.ERROR_CODE_BUILDING_API_AUTH, null, e);
        }
    }

    private static HttpEntity buildTokenRequestPayload(TokenRequestContext requestContext)
            throws TokenRequestException {

        GrantContext grantContext = requestContext.getGrantContext();
        switch (grantContext.getGrantType()) {
            case CLIENT_CREDENTIAL:
                List<NameValuePair> formParams = new ArrayList<>();
                formParams.add(new BasicNameValuePair("grant_type", "client_credentials"));
                if (StringUtils.isNotBlank(grantContext.getProperty(Property.SCOPE.getName()))) {
                    formParams.add(new BasicNameValuePair(
                            "scope", grantContext.getProperty(Property.SCOPE.getName())));
                }
                return new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8);
            default:
                throw new TokenRequestException(
                        ErrorMessage.ERROR_CODE_UNSUPPORTED_GRANT_TYPE, grantContext.getGrantType().name());
        }
    }
}
