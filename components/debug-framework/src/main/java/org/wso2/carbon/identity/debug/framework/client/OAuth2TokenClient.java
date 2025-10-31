/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.client;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.debug.framework.utils.DebugUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
/**
 * Responsible for performing OAuth2 token exchanges. Isolates HTTP/network logic from higher-level processors.
 */
public class OAuth2TokenClient {

    private static final Log LOG = LogFactory.getLog(OAuth2TokenClient.class);

    /**
     * Exchange an authorization code for tokens using the Apache Oltu OAuth client.
     * Returns null if the exchange fails.
     */
    public TokenResponse exchangeCodeForTokens(String authorizationCode, AuthenticationContext context) {
        try {
            IdentityProvider idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            if (idp == null) {
                LOG.error("Identity Provider configuration not found in context");
                return null;
            }

            String authenticatorName = (String) context.getProperty("DEBUG_AUTHENTICATOR_NAME");
            FederatedAuthenticatorConfig authenticatorConfig =

                    DebugUtils.findAuthenticatorConfig(idp, authenticatorName);
            if (authenticatorConfig == null) {
                LOG.error("Authenticator configuration not found");
                return null;
            }

            String clientId = DebugUtils.getPropertyValue(authenticatorConfig, "ClientId", "client_id", "clientId");
            String clientSecret = DebugUtils.getPropertyValue(authenticatorConfig, "ClientSecret",

                    "client_secret", "clientSecret");
            String tokenEndpoint = DebugUtils.getPropertyValue(authenticatorConfig, "OAuth2TokenEPUrl",

                    "tokenEndpoint", "token_endpoint");

            if (clientId == null || clientSecret == null || tokenEndpoint == null) {
                LOG.error("Missing OAuth 2.0 configuration for token exchange");
                return null;
            }

            String codeVerifier = (String) context.getProperty("DEBUG_CODE_VERIFIER");
            String redirectUri = DebugUtils.buildDebugCallbackUrl(context);

        OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint)
            .setGrantType(GrantType.AUTHORIZATION_CODE)
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRedirectURI(redirectUri)
            .setCode(authorizationCode)
            .setParameter("code_verifier", codeVerifier)
            .buildBodyMessage();

            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);

            String accessToken = oAuthResponse.getAccessToken();
            String refreshToken = oAuthResponse.getRefreshToken();
            String tokenType = oAuthResponse.getParam("token_type");
            String idToken = oAuthResponse.getParam("id_token");

            return new TokenResponse(accessToken, idToken, refreshToken, tokenType);
        } catch (Exception e) {
        LOG.error("Error exchanging authorization code for tokens via OAuth2TokenClient: "
            + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Fetches UserInfo claims using the provided access token. Returns an empty map on failure.
     */
    public Map<String, Object> fetchUserInfoClaims(String accessToken, AuthenticationContext context) {
        String userInfoEndpoint = (String) context.getProperty("DEBUG_USERINFO_ENDPOINT");
        if (userInfoEndpoint == null) {
            return Collections.emptyMap();
        }

        HttpFetcher fetcher = new UrlConnectionHttpFetcher();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        return fetcher.getJson(userInfoEndpoint, headers);
    }
}
