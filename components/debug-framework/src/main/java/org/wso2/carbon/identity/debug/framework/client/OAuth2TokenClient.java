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
     * Returns TokenResponse with error details if exchange fails.
     */
    public TokenResponse exchangeCodeForTokens(String authorizationCode, AuthenticationContext context) {
        IdentityProvider idp = null;
        String authenticatorName = null;
        String idpName = null;
        
        try {
            idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            if (idp == null) {
                LOG.error("Identity Provider configuration not found in context");
                return new TokenResponse("IDP_CONFIG_MISSING", "Identity Provider configuration not found in context",
                        "IDP_CONFIG was null in authentication context");
            }

            authenticatorName = (String) context.getProperty("DEBUG_AUTHENTICATOR_NAME");
            FederatedAuthenticatorConfig authenticatorConfig =

                    DebugUtils.findAuthenticatorConfig(idp, authenticatorName);
            if (authenticatorConfig == null) {
                LOG.error("Authenticator configuration not found");
                return new TokenResponse("AUTHENTICATOR_CONFIG_MISSING", "Authenticator configuration not found",
                        "No FederatedAuthenticatorConfig found for: " + authenticatorName);
            }

            String clientId = DebugUtils.getPropertyValue(authenticatorConfig, "ClientId", "client_id", "clientId");
            String clientSecret = DebugUtils.getPropertyValue(authenticatorConfig, "ClientSecret",

                    "client_secret", "clientSecret");
            String tokenEndpoint = DebugUtils.getPropertyValue(authenticatorConfig, "OAuth2TokenEPUrl",

                    "tokenEndpoint", "token_endpoint");

            if (clientId == null || clientSecret == null || tokenEndpoint == null) {
                String missing = (clientId == null ? "clientId " : "") + 
                                (clientSecret == null ? "clientSecret " : "") +
                                (tokenEndpoint == null ? "tokenEndpoint" : "");
                LOG.error("Missing OAuth 2.0 configuration for token exchange: " + missing);
                return new TokenResponse("CONFIG_MISSING", "Missing OAuth 2.0 configuration for token exchange",
                        "Missing properties: " + missing.trim());
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

        // Add Accept: application/json header for GitHub token endpoint.
        idpName = idp.getIdentityProviderName();
        boolean isGitHub = idpName != null && idpName.toLowerCase().contains("github");
        if (isGitHub) {
            request.addHeader("Accept", "application/json");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Exchanging authorization code for tokens at endpoint: " + tokenEndpoint + 
                    " for IdP: " + idpName);
        }

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);

        String accessToken = oAuthResponse.getAccessToken();
        String refreshToken = oAuthResponse.getRefreshToken();
        String tokenType = oAuthResponse.getParam("token_type");
        String idToken = oAuthResponse.getParam("id_token");

        if (LOG.isDebugEnabled()) {
            LOG.debug("Token exchange successful for IdP: " + idpName + ", received access_token and token_type.");
        }

        return new TokenResponse(accessToken, idToken, refreshToken, tokenType);
        } catch (Exception e) {
            // Capture detailed error information from the exception.
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            String errorCode = extractErrorCode(e);
            String enhancedDetails = buildDetailedErrorDescription(e, errorCode, idp, authenticatorName);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Token exchange failed with error code: " + errorCode + ", message: " + errorMessage + 
                        ". IdP: " + idpName);
            }
            LOG.error("Token exchange failed for IdP: " + idpName + " - Code: " + errorCode + 
                    ", Message: " + errorMessage, e);
            
            return new TokenResponse(errorCode, errorMessage, enhancedDetails);
        }
    }

    /**
     * Extracts error code from OAuth exceptions.
     */
    private String extractErrorCode(Exception e) {
        String exceptionMessage = e.getMessage() != null ? e.getMessage() : "";
        if (exceptionMessage.contains("invalid_client")) {
            return "INVALID_CLIENT";
        } else if (exceptionMessage.contains("invalid_grant")) {
            return "INVALID_GRANT";
        } else if (exceptionMessage.contains("Unauthorized")) {
            return "UNAUTHORIZED";
        } else if (exceptionMessage.contains("invalid_request")) {
            return "INVALID_REQUEST";
        } else if (exceptionMessage.contains("unsupported_grant_type")) {
            return "UNSUPPORTED_GRANT_TYPE";
        } else if (exceptionMessage.contains("Connection")) {
            return "CONNECTION_ERROR";
        } else if (exceptionMessage.contains("timeout") || exceptionMessage.contains("Timeout")) {
            return "TIMEOUT_ERROR";
        } else if (exceptionMessage.contains("SSL") || exceptionMessage.contains("Certificate")) {
            return "SSL_CERTIFICATE_ERROR";
        } else {
            return "TOKEN_EXCHANGE_ERROR";
        }
    }

    /**
     * Builds a detailed error description from exception details.
     * Focuses on actionable information without verbose stack traces.
     *
     * @param e The exception that occurred.
     * @param errorCode The error code extracted from the exception.
     * @param idp The Identity Provider configuration.
     * @param authenticatorName The name of the authenticator.
     * @return A detailed error description with troubleshooting hints.
     */
    private String buildDetailedErrorDescription(Exception e, String errorCode, IdentityProvider idp, 
            String authenticatorName) {
        StringBuilder details = new StringBuilder();
        
        // Add context-specific troubleshooting hints.
        switch (errorCode) {
            case "INVALID_CLIENT":
                details.append("Client credentials are invalid. Verify that the Client ID and Client Secret ")
                    .append("are correct in the IdP authenticator configuration (")
                    .append(authenticatorName).append(").");
                break;
            case "INVALID_GRANT":
                details.append("The authorization code may have expired (usually after 5-10 minutes) ")
                    .append("or was already used. Start the authentication process again to get a new ")
                    .append("authorization code.");
                break;
            case "INVALID_REQUEST":
                details.append("The token request is malformed. Verify redirect URI and PKCE parameters ")
                    .append("are configured correctly.");
                break;
            case "UNAUTHORIZED":
                details.append("The IdP rejected the request. Check that client credentials are correct ")
                    .append("and the authenticator type matches the IdP's requirements.");
                break;
            case "CONFIG_MISSING":
                details.append("Required OAuth 2.0 configuration is missing. Verify that Client ID, ")
                    .append("Client Secret, and Token Endpoint URL are all configured in the IdP ")
                    .append("authenticator settings.");
                break;
            case "CONNECTION_ERROR":
                details.append("Cannot connect to the IdP token endpoint. Verify the token endpoint URL ")
                    .append("is correct and the IdP server is reachable.");
                break;
            case "TIMEOUT_ERROR":
                details.append("The request to the IdP token endpoint timed out. ")
                    .append("Check if the IdP server is running and network connectivity is available.");
                break;
            case "SSL_CERTIFICATE_ERROR":
                details.append("SSL certificate validation failed. Verify that the IdP's SSL certificate ")
                    .append("is valid and trusted.");
                break;
            default:
                details.append("An error occurred during token exchange.")
                    .append(" Check the error code and message for details.").append(e.getMessage());
        }
        
        return details.toString();
    }

    /**
     * Fetches UserInfo claims using the provided access token. 
     */
    public Map<String, Object> fetchUserInfoClaims(String accessToken, AuthenticationContext context) {
        String userInfoEndpoint = (String) context.getProperty("DEBUG_USERINFO_ENDPOINT");
        if (userInfoEndpoint == null) {
            return Collections.emptyMap();
        }

        HttpFetcher fetcher = new UrlConnectionHttpFetcher();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        // Add Accept header for GitHub API if endpoint is GitHub
        if (userInfoEndpoint.contains("api.github.com")) {
            headers.put("Accept", "application/vnd.github.v3+json");
        }
        return fetcher.getJson(userInfoEndpoint, headers);
    }
}
