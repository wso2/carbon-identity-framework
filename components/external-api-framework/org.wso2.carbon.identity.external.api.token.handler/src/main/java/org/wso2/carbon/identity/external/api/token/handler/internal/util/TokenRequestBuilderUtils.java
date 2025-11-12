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

import org.wso2.carbon.identity.external.api.client.api.exception.APIClientRequestException;
import org.wso2.carbon.identity.external.api.client.api.model.APIAuthentication;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenRequestException;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext.Property;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenRequestContext;

import java.util.Map;

/**
 * Utility class for building token request related objects.
 */
public class TokenRequestBuilderUtils {

    /**
     * Build APIRequestContext for token request.
     *
     * @param requestContext TokenRequestContext.
     * @return APIRequestContext.
     * @throws TokenRequestException TokenRequestException.
     */
    public static APIRequestContext buildAPIRequestContext(TokenRequestContext requestContext)
            throws TokenRequestException {

        return buildBasicAPIRequestContext(requestContext, buildTokenRequestPayload(requestContext));
    }

    /**
     * Build APIRequestContext for refresh token grant.
     *
     * @param requestContext TokenRequestContext.
     * @param refreshToken   Refresh token.
     * @return APIRequestContext.
     * @throws TokenRequestException TokenRequestException.
     */
    public static APIRequestContext buildAPIRequestContextForRefreshGrant(
            TokenRequestContext requestContext, String refreshToken) throws TokenRequestException {

        String payload = String.format(PayloadTemplateByType.REFRESH.getPayload(), refreshToken);
        return buildBasicAPIRequestContext(requestContext, payload);
    }

    private static APIRequestContext buildBasicAPIRequestContext(TokenRequestContext requestContext, String payload)
            throws TokenRequestException {

        try {
            APIAuthentication authentication = TokenRequestBuilderUtils
                    .buildTokenRequestAPIAuthentication(requestContext);
            APIRequestContext.Builder requestContextBuilder = new APIRequestContext.Builder()
                    .httpMethod(APIRequestContext.HttpMethod.POST)
                    .headers(requestContext.getHeaders())
                    .endpointUrl(requestContext.getTokenEndpointUrl())
                    .apiAuthentication(authentication)
                    .payload(payload);
            return requestContextBuilder.build();
        } catch (APIClientRequestException e) {
            throw new TokenRequestException("Error building API Request Context for token request.", e);
        }
    }

    private static APIAuthentication buildTokenRequestAPIAuthentication(TokenRequestContext requestContext)
            throws TokenRequestException {

        GrantContext grantContext = requestContext.getGrantContext();
        try {
            switch (grantContext.getGrantType()) {
                case CLIENT_CREDENTIAL:
                    return new APIAuthentication.Builder()
                            .authType(APIAuthentication.AuthType.BASIC)
                            .properties(Map.of(
                                    APIAuthentication.Property.USERNAME.getName(),
                                    grantContext.getProperty(Property.CLIENT_ID.getName()),
                                    APIAuthentication.Property.PASSWORD.getName(),
                                    grantContext.getProperty(Property.CLIENT_SECRET.getName())
                            ))
                            .build();
                default:
                    throw new TokenRequestException("Unsupported authentication type: " + grantContext.getGrantType());
            }
        } catch (APIClientRequestException e) {
            throw new TokenRequestException("Error building API Authentication for grant type: " +
                    grantContext.getGrantType(), e);
        }
    }

    private static String buildTokenRequestPayload(TokenRequestContext requestContext) throws TokenRequestException {

        String template;
        GrantContext grantContext = requestContext.getGrantContext();
        switch (grantContext.getGrantType()) {
            case CLIENT_CREDENTIAL:
                template = PayloadTemplateByType.CLIENT_CREDENTIAL.getPayload();
                return String.format(template,
                        grantContext.getProperty(Property.CLIENT_ID.getName()),
                        grantContext.getProperty(Property.CLIENT_SECRET.getName()),
                        grantContext.getProperty(Property.SCOPE.getName()));
            default:
                throw new TokenRequestException("Unsupported authentication type: " + requestContext.getGrantContext());
        }
    }

    /**
     * Enum for payload templates by grant type.
     */
    public enum PayloadTemplateByType {

        CLIENT_CREDENTIAL("grant_type=client_credentials&client_id=%s&client_secret=%s&scope=%s"),
        REFRESH("grant_type=refresh_token&refresh_token=%s");

        private final String payload;

        PayloadTemplateByType(String payload) {

            this.payload = payload;
        }

        public String getPayload() {

            return payload;
        }
    }
}
