
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

package org.wso2.carbon.identity.debug.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.debug.framework.utils.DebugUtils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Simplified executor that generates OAuth 2.0 Authorization URLs with PKCE parameters.
 * This class only handles URL generation and parameter setup - actual authentication 
 * is delegated to the external IdP and handled via /commonauth callback.
 */
public class Executer {

    private static final Log LOG = LogFactory.getLog(Executer.class);
    private static final String DEBUG_IDENTIFIER_PARAM = "isDebugFlow";

    /**
     * Generates a standard OAuth 2.0 Authorization URL with PKCE parameters.
     * The URL will be used to redirect the user's browser to the external IdP for authentication.
     *
     * @param idp Identity Provider configuration.
     * @param context AuthenticationContext to store generated parameters.
     * @return true if URL generation is successful, false otherwise.
     */
    public boolean execute(IdentityProvider idp, AuthenticationContext context) {
    // Step status: connection creation started
    context.setProperty("step_connection_status", "started");
    // Step status: authentication started
    context.setProperty("step_authentication_status", "started");
    // Step status: claim mapping started
    context.setProperty("step_claim_mapping_status", "started");
    
        if (LOG.isDebugEnabled()) {
            String idpName = idp != null ? idp.getIdentityProviderName() : "null";
            LOG.debug("Generating OAuth 2.0 Authorization URL for IdP: " + idpName);
        }

        try {
            if (idp == null) {
                LOG.error("Identity Provider is null, cannot generate authorization URL");
                context.setProperty("step_connection_status", "failed");
                context.setProperty("step_authentication_status", "failed");
                context.setProperty("step_claim_mapping_status", "failed");
                return false;
            }

            // Get authenticator configuration from context.
            String authenticatorName = (String) context.getProperty("DEBUG_AUTHENTICATOR_NAME");
            if (authenticatorName == null) {
                LOG.error("Authenticator name not found in context");
                context.setProperty("step_connection_status", "failed");
                context.setProperty("step_authentication_status", "failed");
                context.setProperty("step_claim_mapping_status", "failed");
                return false;
            }

            // Find the specific authenticator configuration.
            FederatedAuthenticatorConfig authenticatorConfig = findAuthenticatorConfig(idp, authenticatorName);
            if (authenticatorConfig == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authenticator configuration not found for: " + authenticatorName);
                }
                context.setProperty("step_connection_status", "failed");
                context.setProperty("step_authentication_status", "failed");
                context.setProperty("step_claim_mapping_status", "failed");
                return false;
            }

            // Generate OAuth 2.0 Authorization URL with PKCE.
            String authorizationUrl = buildAuthorizationUrl(idp, authenticatorConfig, context);
            if (authorizationUrl == null) {
                LOG.error("Failed to build authorization URL");
                context.setProperty("step_connection_status", "failed");
                context.setProperty("step_authentication_status", "failed");
                context.setProperty("step_claim_mapping_status", "failed");
                return false;
            }

            // Store the authorization URL in context for client retrieval.
            context.setProperty("DEBUG_EXTERNAL_REDIRECT_URL", authorizationUrl);

            // Mark debug flow properties.
            context.setProperty(DEBUG_IDENTIFIER_PARAM, "true");
            context.setProperty("DEBUG_AUTH_URL_GENERATED", "true");
            context.setProperty("DEBUG_AUTH_URL_TIMESTAMP", System.currentTimeMillis());
            // Step status reporting: authorization URL generated
            context.setProperty("DEBUG_STEP_AUTH_URL_GENERATED", true);
            context.setProperty("DEBUG_STEP_AUTH_URL", authorizationUrl);
            context.setProperty("DEBUG_STEP_AUTH_URL_TIMESTAMP", System.currentTimeMillis());

            // Step status: connection creation successful
            context.setProperty("step_connection_status", "success");
            // Step status: authentication successful (URL generated means ready for authentication)
            context.setProperty("step_authentication_status", "success");
            // Step status: claim mapping pending (will be set in claim mapping logic)
            context.setProperty("step_claim_mapping_status", "pending");

            // Cache the authentication context for WSO2 framework to find during callback.
            cacheAuthenticationContext(context);

            // Only log essential verification for production
            LOG.info("OAuth 2.0 Authorization URL Generated");

            return true;

        } catch (Exception e) {
            LOG.error("Error generating OAuth 2.0 Authorization URL: " + e.getMessage(), e);
            context.setProperty("step_connection_status", "failed");
            context.setProperty("step_authentication_status", "failed");
            context.setProperty("step_claim_mapping_status", "failed");
            return false;
        }
    }

    /**
     * Builds the complete OAuth 2.0 Authorization URL with PKCE parameters.
     *
     * @param idp Identity Provider configuration.
     * @param authenticatorConfig Authenticator configuration.
     * @param context AuthenticationContext to store generated parameters.
     * @return Complete authorization URL or null if failed.
     */
    private String buildAuthorizationUrl(IdentityProvider idp, FederatedAuthenticatorConfig authenticatorConfig,
                                        AuthenticationContext context) {
        try {
            // Read resolved properties from context
            String clientId = (String) context.getProperty("DEBUG_CLIENT_ID");
            String authorizationEndpoint = (String) context.getProperty("DEBUG_AUTHZ_ENDPOINT");

            if (clientId == null || clientId.trim().isEmpty()) {
                LOG.error("Missing OAuth 2.0 configuration - ClientId is required (from context)");
                return null;
            }
            if (authorizationEndpoint == null || authorizationEndpoint.trim().isEmpty()) {
                LOG.error("Missing OAuth 2.0 configuration - Authorization Endpoint is required (from context)");
                return null;
            }
            if (LOG.isDebugEnabled()) {
                String authzEndpointForLog = authorizationEndpoint;
        LOG.debug("OAuth 2.0 configuration validated from context - ClientId: FOUND, Authorization Endpoint: "
            + authzEndpointForLog);
            }
            // Generate PKCE parameters.
            String codeVerifier = generateCodeVerifier();
            String codeChallenge = generateCodeChallenge(codeVerifier);
            String state = context.getContextIdentifier();
            context.setProperty("DEBUG_CODE_VERIFIER", codeVerifier);
            context.setProperty("DEBUG_STATE", state);
            String redirectUri = DebugUtils.buildDebugCallbackUrl(context);
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(authorizationEndpoint);
            urlBuilder.append("?response_type=code");
            urlBuilder.append("&client_id=").append(encodeParam(clientId));
            urlBuilder.append("&redirect_uri=").append(encodeParam(redirectUri));

            String scope = (String) context.getProperty("CUSTOM_SCOPE"); // Priority 1: User override
            if (scope == null || scope.trim().isEmpty()) {
                scope = (String) context.getProperty("DEBUG_IDP_SCOPE"); // Priority 2: IdP's configured scope
            }
            if (scope == null || scope.trim().isEmpty()) {
                // Per requirement: do not add fallbacks. Must use the correct scopes configured in the connector.
                throw new RuntimeException(
                        "No scope configured for the IdP.");
            }
            urlBuilder.append("&scope=").append(encodeParam(scope));
            urlBuilder.append("&state=").append(encodeParam(state));
            urlBuilder.append("&code_challenge=").append(encodeParam(codeChallenge));
            urlBuilder.append("&code_challenge_method=S256");
            // Add access_type for refresh token support if configured.
            String accessType = (String) context.getProperty("DEBUG_CUSTOM_access_type");
            if (accessType != null && !accessType.trim().isEmpty()) {
                urlBuilder.append("&access_type=").append(encodeParam(accessType));
            }
            // Add login hint if username is available.
            String username = (String) context.getProperty("DEBUG_USERNAME");
            if (username != null && !username.trim().isEmpty()) {
                urlBuilder.append("&login_hint=").append(encodeParam(username));
            }
            // Add any additional custom parameters from context (ADDITIONAL_OAUTH_PARAMS)
            java.util.Map<String, String> additionalParams = null;
            Object additionalParamsObj = context.getProperty("ADDITIONAL_OAUTH_PARAMS");
            if (additionalParamsObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, String> temp = (java.util.Map<String, String>) additionalParamsObj;
                additionalParams = temp;
            }
            if (additionalParams != null) {
                for (java.util.Map.Entry<String, String> entry : additionalParams.entrySet()) {
                    urlBuilder.append("&")
                            .append(entry.getKey())
                            .append("=")
                            .append(encodeParam(entry.getValue()));
                }
            }
            String authorizationUrl = urlBuilder.toString();
            if (LOG.isDebugEnabled()) {
                String idpNameForLog = idp != null ? idp.getIdentityProviderName() : "null";
                LOG.debug("Generated OAuth 2.0 Authorization URL with PKCE for IdP (from context): " + idpNameForLog);
            }
            return authorizationUrl;
        } catch (Exception e) {
            LOG.error("Error building OAuth 2.0 Authorization URL: " + e.getMessage(), e);
            return null;
        }
    }

    // Use DebugUtils static methods for authenticator config and property lookup.
    private FederatedAuthenticatorConfig findAuthenticatorConfig(IdentityProvider idp, String authenticatorName) {
        return DebugUtils.findAuthenticatorConfig(idp, authenticatorName);
    }

    /**
     * Generates a cryptographically secure code verifier for PKCE.
     *
     * @return Base64URL-encoded code verifier.
     */
    private String generateCodeVerifier() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Generates code challenge from code verifier using SHA256.
     *
     * @param codeVerifier The code verifier.
     * @return Base64URL-encoded code challenge.
     */
    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes("UTF-8"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            LOG.error("Error generating code challenge: " + e.getMessage(), e);
            throw new RuntimeException("Failed to generate PKCE code challenge securely.", e);
        }
    }

    /**
     * URL-encodes a parameter value.
     *
     * @param param Parameter to encode.
     * @return URL-encoded parameter.
     */
    private String encodeParam(String param) {
        try {
            return java.net.URLEncoder.encode(param, "UTF-8");
        } catch (Exception e) {
            LOG.error("Error encoding parameter: " + e.getMessage(), e);
            throw new RuntimeException("Failed to URL-encode parameter.", e);
        }
    }

    /**
     * Caches the authentication context using WSO2 framework utilities.
     * This ensures the context can be retrieved by DefaultRequestCoordinator during callback.
     *
     * @param context AuthenticationContext to cache.
     */
    private void cacheAuthenticationContext(AuthenticationContext context) {
        try {
            // Use WSO2 framework utilities to cache the context.
            FrameworkUtils.addAuthenticationContextToCache(context.getContextIdentifier(), context);
            
            if (LOG.isDebugEnabled()) {
                String ctxId = context.getContextIdentifier();
                LOG.debug("Debug authentication context cached successfully with identifier: " + ctxId);
            }
        } catch (Exception e) {
            LOG.error("Error caching debug authentication context: " + e.getMessage(), e);
        }
    }
}
