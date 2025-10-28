package org.wso2.carbon.identity.debug.framework;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.debug.framework.Utils.DebugUtils;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Processes OAuth 2.0 Authorization Code callbacks from external IdPs.
 * Handles code exchange for tokens, validates tokens, extracts claims, and creates authenticated user objects.
 */
public class DebugProcessor {

/**
 * Processes the result and writes a self-contained HTML page directly to the response.
 * This breaks the redirect loop and avoids JSP security issues.
 * The result is saved to browser localStorage with a unique key based on the 'state'.
 *
 * @param response HttpServletResponse
 * @param context  AuthenticationContext
 * @param success  true if authentication succeeded
 * @throws IOException
 */
    private static final Log LOG = LogFactory.getLog(DebugProcessor.class);

    /**
     * Processes the OAuth 2.0 Authorization Code callback from external IdP.
     * Performs code exchange, token validation, claim extraction, and response generation.
     *
     * @param request HttpServletRequest containing authorization code and state.
     * @param response HttpServletResponse for sending results.
     * @param context AuthenticationContext with stored PKCE parameters.
     * @throws IOException If processing fails.
     */
    public void processCallback(HttpServletRequest request, HttpServletResponse response,
                               AuthenticationContext context) throws IOException {
        // Extract callback parameters.
        String authorizationCode = request.getParameter("code");
        String state = request.getParameter("state");
        String error = request.getParameter("error");
        String errorDescription = request.getParameter("error_description");
        String idpId = "";
        try {
            // Prevent re-processing the same authorization code in the same session.
            String lastProcessedCode = (String) request.getSession().getAttribute("LAST_AUTH_CODE");
            IdentityProvider idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            idpId = idp != null ? idp.getResourceId() : "";
            if (authorizationCode != null && authorizationCode.equals(lastProcessedCode)) {
                if (!response.isCommitted()) {
                    context.setProperty("DEBUG_AUTH_ERROR", "Authorization code already used in this session. Please retry login.");
                    context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state + "&idpId=" + idpId);
                }
                return;
            }
            if (authorizationCode != null) {
                request.getSession().setAttribute("LAST_AUTH_CODE", authorizationCode);
            }

            if (error != null) {
                LOG.error("Authorization error from IdP: " + error + " - " + errorDescription);
                context.setProperty("DEBUG_AUTH_ERROR", error + ": " + errorDescription);
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state + "&idpId=" + idpId);
                }
                return;
            }

            if (authorizationCode == null || authorizationCode.trim().isEmpty()) {
                LOG.error("Authorization code missing in callback");
                context.setProperty("DEBUG_AUTH_ERROR", "Authorization code not received from IdP");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state + "&idpId=" + idpId);
                }
                return;
            }

            if (state == null || state.trim().isEmpty()) {
                LOG.error("State parameter missing in callback");
                context.setProperty("DEBUG_AUTH_ERROR", "State parameter missing - possible CSRF attack");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state + "&idpId=" + idpId);
                }
                return;
            }

            String storedState = (String) context.getProperty("DEBUG_STATE");
            if (!state.equals(storedState)) {
                LOG.error("State parameter mismatch - CSRF attack detected");
                context.setProperty("DEBUG_AUTH_ERROR", "State validation failed - possible CSRF attack");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state + "&idpId=" + idpId);
                }
                return;
            }

            TokenResponse tokens = exchangeCodeForTokens(authorizationCode, context);
            if (tokens == null || tokens.getAccessToken() == null) {
                LOG.error("Token exchange failed - no tokens received from IdP");
                context.setProperty("DEBUG_AUTH_ERROR", "Failed to exchange authorization code for tokens");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state + "&idpId=" + idpId);
                }
                return;
            }

            context.setProperty("DEBUG_ACCESS_TOKEN", tokens.getAccessToken());
            if (tokens.getIdToken() != null) {
                context.setProperty("DEBUG_ID_TOKEN", tokens.getIdToken());
            }
            if (tokens.getRefreshToken() != null) {
                context.setProperty("DEBUG_REFRESH_TOKEN", tokens.getRefreshToken());
            }
            context.setProperty("DEBUG_TOKEN_TYPE", tokens.getTokenType());

            Map<String, Object> claims = extractUserClaims(tokens, context);
            if (claims == null || claims.isEmpty()) {
                LOG.error("Failed to extract user claims from tokens");
                context.setProperty("DEBUG_AUTH_ERROR", "Failed to extract user claims from tokens");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state + "&idpId=" + idpId);
                }
                return;
            }

            AuthenticatedUser authenticatedUser = createAuthenticatedUser(claims, context);
            if (authenticatedUser != null) {
                context.setSubject(authenticatedUser);
                context.setProperty("DEBUG_AUTH_SUCCESS", "true");
                context.setProperty("DEBUG_USER_EXISTS", true);
                context.setProperty("DEBUG_AUTH_COMPLETED", "true");
                context.setProperty("DEBUG_AUTH_COMPLETION_TIMESTAMP", System.currentTimeMillis());

                // Build a developer-friendly debug result
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> debugResult = new HashMap<>();

                    // Authentication status
                    debugResult.put("success", "true");
                    debugResult.put("error", context.getProperty("DEBUG_AUTH_ERROR"));
                    debugResult.put("sessionId", state);
                    debugResult.put("idpName", context.getProperty("DEBUG_IDP_NAME"));
                        debugResult.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
                        // Add executor class name for clarity
                        Object executorObj = context.getProperty("DEBUG_EXECUTOR_INSTANCE");
                        String executorClass = executorObj != null ? executorObj.getClass().getSimpleName() : "UnknownExecutor";
                        debugResult.put("executor", executorClass);
                    debugResult.put("timestamp", context.getProperty("DEBUG_AUTH_COMPLETION_TIMESTAMP"));

                    // Incoming claims (from IdP)
                    debugResult.put("incomingClaims", claims);


                    // Mapped claims (local claims)
                    Object mappedClaims = context.getProperty("DEBUG_MAPPED_LOCAL_CLAIMS_MAP");
                    debugResult.put("mappedClaims", mappedClaims);

                    // Set claim mapping status to success if mapped claims are present
                    if (mappedClaims != null && mappedClaims instanceof Map && !((Map<?,?>) mappedClaims).isEmpty()) {
                        context.setProperty("step_claim_mapping_status", "success");
                        debugResult.put("step_claim_mapping_status", "success");
                    }

                    // User info
                    debugResult.put("username", authenticatedUser != null ? authenticatedUser.getUserName() : null);
                    debugResult.put("userId", authenticatedUser != null ? authenticatedUser.getUserId() : null);

                    // Minimal debug metadata
                    debugResult.put("externalRedirectUrl", context.getProperty("DEBUG_EXTERNAL_REDIRECT_URL"));
                    debugResult.put("callbackUrl", context.getProperty("DEBUG_CALLBACK_URL_USED"));

                    // Add step status fields for GET endpoint enrichment
                    debugResult.put("step_connection_status", context.getProperty("step_connection_status"));
                    debugResult.put("step_authentication_status", context.getProperty("step_authentication_status"));
                    // step_claim_mapping_status already set above if mapped claims present

                    // User attributes: convert keys to claim URIs
                    Map<String, Object> userAttributesMap = new HashMap<>();
                    if (authenticatedUser != null && authenticatedUser.getUserAttributes() != null) {
                        for (Map.Entry<org.wso2.carbon.identity.application.common.model.ClaimMapping, String> entry : authenticatedUser.getUserAttributes().entrySet()) {
                            org.wso2.carbon.identity.application.common.model.ClaimMapping mapping = entry.getKey();
                            String claimUri = mapping.getLocalClaim() != null ? mapping.getLocalClaim().getClaimUri() : mapping.toString();
                            userAttributesMap.put(claimUri, entry.getValue());
                        }
                    }
                    debugResult.put("userAttributes", userAttributesMap);

                    String resultJson = mapper.writeValueAsString(debugResult);
                    DebugResultCache.add(state, resultJson);
                    LOG.info("Debug result cached");
                } catch (Exception cacheEx) {
                    LOG.error("Failed to cache debug result", cacheEx);
                }

                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state + "&idpId=" + idpId);
                }
            } else {
                LOG.error("Failed to create authenticated user from claims");
                context.setProperty("DEBUG_AUTH_ERROR", "Failed to create authenticated user");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                context.setProperty("DEBUG_USER_EXISTS", false);
                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state + "&idpId=" + idpId);
                }
            }
            return;
        } catch (Exception e) {
            LOG.error("Unexpected error processing OAuth 2.0 callback.", e);
            context.setProperty("DEBUG_AUTH_ERROR", "Unexpected error: " + e.getMessage());
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            if (e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null) {
                LOG.error("InvocationTargetException cause:", e.getCause());
            }
            if (!response.isCommitted()) {
                response.sendRedirect("/authenticationendpoint/debugError.jsp?state=" + state + "&idpId=" + idpId);
            }
        }
    }

    /**
     * Exchanges authorization code for access token and ID token using PKCE.
     *
     * @param authorizationCode Authorization code from IdP.
     * @param context AuthenticationContext with stored PKCE parameters.
     * @return TokenResponse with tokens or null if exchange fails.
     */
    private TokenResponse exchangeCodeForTokens(String authorizationCode, AuthenticationContext context) {
        try {
            // Get OAuth 2.0 configuration.
            String authenticatorName = (String) context.getProperty("DEBUG_AUTHENTICATOR_NAME");
            
            IdentityProvider idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            if (idp == null) {
                LOG.error("Identity Provider configuration not found in context");
                return null;
            }

            FederatedAuthenticatorConfig authenticatorConfig = DebugUtils.findAuthenticatorConfig(idp, authenticatorName);
            if (authenticatorConfig == null) {
                LOG.error("Authenticator configuration not found");
                return null;
            }

            String clientId = DebugUtils.getPropertyValue(authenticatorConfig, "ClientId", "client_id", "clientId");
            String clientSecret = DebugUtils.getPropertyValue(authenticatorConfig, "ClientSecret", "client_secret", "clientSecret");
            String tokenEndpoint = DebugUtils.getPropertyValue(authenticatorConfig, "OAuth2TokenEPUrl", "tokenEndpoint", "token_endpoint");

            // OAuth 2.0 configuration loaded.

            if (clientId == null || clientSecret == null || tokenEndpoint == null) {
                LOG.error("Missing OAuth 2.0 configuration for token exchange");
                return null;
            }

            // Get stored PKCE parameters.
            String codeVerifier = (String) context.getProperty("DEBUG_CODE_VERIFIER");
            String redirectUri = buildRedirectUri(context);

            // Prepare token request.
            String requestBody = "grant_type=authorization_code" +
                                "&code=" + urlEncode(authorizationCode) +
                                "&redirect_uri=" + urlEncode(redirectUri) +
                                "&client_id=" + urlEncode(clientId) +
                                "&code_verifier=" + urlEncode(codeVerifier);

            // Make token request with retry logic.
            HttpURLConnection connection = null;
            int responseCode = -1;
            int maxRetries = 3;
            int attempt = 1;
            
            while (attempt <= maxRetries) {
                try {
                    connection = createTokenRequest(tokenEndpoint, clientId, clientSecret, requestBody);
                    responseCode = connection.getResponseCode();
                    break; // Success - exit retry loop
                } catch (java.net.SocketTimeoutException e) {
                    if (attempt == maxRetries) {
                        LOG.error("All " + maxRetries + " token exchange attempts failed due to timeout");
                        throw e; // Re-throw on final attempt
                    }
                    attempt++;
                    // Wait before retry (exponential backoff)
                    Thread.sleep(2000 * attempt);
                } catch (Exception e) {
                    if (attempt == maxRetries) {
                        throw e; // Re-throw on final attempt
                    }
                    attempt++;
                    Thread.sleep(1000 * attempt);
                }
            }

            if (responseCode == 200) {
                String responseBody = readResponse(connection.getInputStream());
                return parseTokenResponse(responseBody);
            } else {
                String errorResponse = readResponse(connection.getErrorStream());
                LOG.error("OAuth Token Exchange Failed - HTTP Status: " + responseCode);
                LOG.error("Error Response: " + errorResponse);
                LOG.error("Token Endpoint: " + tokenEndpoint);
                LOG.error("Redirect URI: " + redirectUri);
                
                // If callback URL mismatch, provide helpful guidance
                if (errorResponse != null && errorResponse.contains("Callback url mismatch")) {
                    LOG.error("CALLBACK URL MISMATCH - Generated redirect_uri: " + redirectUri);
                    LOG.error("Please ensure this URL is added to your IdP application's callback URLs configuration");
                }
                
                return null;
            }

        } catch (Exception e) {
            LOG.error("Error exchanging authorization code for tokens: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extracts user claims from tokens (ID token and/or UserInfo endpoint).
     *
     * @param tokens TokenResponse containing access token and ID token.
     * @param context AuthenticationContext.
     * @return Map of user claims or null if extraction fails.
     */
    private Map<String, Object> extractUserClaims(TokenResponse tokens, AuthenticationContext context) {
        try {
            Map<String, Object> claims = new HashMap<>();

            // Extract claims from ID token if available.
            if (tokens.getIdToken() != null) {
                Map<String, Object> idTokenClaims = parseIdTokenClaims(tokens.getIdToken());
                if (idTokenClaims != null) {
                    claims.putAll(idTokenClaims);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("ID Token claims: " + idTokenClaims.keySet());
                    }
                } else {
                    LOG.warn("Failed to parse ID Token claims");
                }
            } else {
            }

            // Extract additional claims from UserInfo endpoint if access token is available.
            if (tokens.getAccessToken() != null) {
                Map<String, Object> userInfoClaims = fetchUserInfoClaims(tokens.getAccessToken(), context);
                if (userInfoClaims != null) {
                    claims.putAll(userInfoClaims);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("UserInfo claims: " + userInfoClaims.keySet());
                    }
                } else {
                    LOG.warn("UserInfo endpoint call failed or returned no claims");
                }
            }
            if (claims.containsKey("sub")) {
                LOG.debug("Subject identifier: " + claims.get("sub"));
            }
            if (claims.containsKey("email")) {
                LOG.debug("Email claim present: " + claims.get("email"));
            }
            if (claims.containsKey("name")) {
                LOG.debug("Name claim present: " + claims.get("name"));
            }

            return claims;

        } catch (Exception e) {
            LOG.error("Error extracting user claims: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Creates an AuthenticatedUser object from extracted claims.
     *
     * @param claims Map of user claims.
     * @param context AuthenticationContext.
     * @return AuthenticatedUser object or null if creation fails.
     */
    private AuthenticatedUser createAuthenticatedUser(Map<String, Object> claims, AuthenticationContext context) {
            IdentityProvider idpLog = (IdentityProvider) context.getProperty("IDP_CONFIG");
            if (idpLog != null && idpLog.getClaimConfig() != null && idpLog.getClaimConfig().getClaimMappings() != null) {
                StringBuilder mappingsLog = new StringBuilder("[DEBUG] Configured claim mappings: [");
                for (ClaimMapping cm : idpLog.getClaimConfig().getClaimMappings()) {
                    if (cm != null && cm.getRemoteClaim() != null && cm.getLocalClaim() != null) {
                        mappingsLog.append("{remote: ").append(cm.getRemoteClaim().getClaimUri())
                            .append(", local: ").append(cm.getLocalClaim().getClaimUri()).append("}, ");
                    }
                }
                mappingsLog.append("]");
            }
        try {
            // Extract essential user information.
            String subject = getClaimValue(claims, "sub", "user_id", "id");
            String email = getClaimValue(claims, "email");
            String preferredUsername = getClaimValue(claims, "preferred_username", "username");

            // Use email as username if preferred_username is not available.
            String username = preferredUsername != null ? preferredUsername : email;
            if (username == null) {
                username = subject; // Fallback to subject if no other identifier is available.
            }

            if (subject == null || username == null) {
                LOG.error("Essential user claims missing - subject: " + subject + ", username: " + username);
                return null;
            }

            // --- Auto-map IdP claim mappings to match token claim keys (short name or URI) ---
            IdentityProvider idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            if (idp != null && idp.getClaimConfig() != null && idp.getClaimConfig().getClaimMappings() != null) {
                ClaimMapping[] mappings = idp.getClaimConfig().getClaimMappings();
                for (ClaimMapping mapping : mappings) {
                    if (mapping.getRemoteClaim() != null && mapping.getLocalClaim() != null) {
                        String remoteUri = mapping.getRemoteClaim().getClaimUri();
                        // If the remote URI is not found in claims, but a short name is, update the mapping for this context
                        if (!claims.containsKey(remoteUri) && remoteUri.contains("/")) {
                            String shortName = remoteUri.substring(remoteUri.lastIndexOf("/") + 1);
                            if (claims.containsKey(shortName)) {
                                // Update the remote claim URI in-memory for this mapping (for this context only)
                                mapping.getRemoteClaim().setClaimUri(shortName);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Auto-mapped remote claim URI from '" + remoteUri + "' to short name '" + shortName + "' for this context.");
                                }
                            }
                        }
                    }
                }
            }

            // Create federated authenticated user.
            AuthenticatedUser authenticatedUser = AuthenticatedUser.createFederateAuthenticatedUserFromSubjectIdentifier(subject);
            authenticatedUser.setUserName(username);
            authenticatedUser.setUserId(subject);
            authenticatedUser.setAuthenticatedSubjectIdentifier(subject);
            authenticatedUser.setUserStoreDomain("FEDERATED");

            // Set IdP information.
            String idpName = getIdpName(context);
            if (idpName != null) {
                authenticatedUser.setFederatedIdPName(idpName);
            }

            // Map claims to user attributes.

            Map<ClaimMapping, String> userAttributes = new HashMap<>();
            mapClaimsToAttributes(claims, userAttributes, context);
            authenticatedUser.setUserAttributes(userAttributes);

            // Build mappedLocalClaims map using local claim URI as key
            Map<String, String> mappedLocalClaims = new HashMap<>();
            for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {
                ClaimMapping mapping = entry.getKey();
                if (mapping != null && mapping.getLocalClaim() != null) {
                    String localClaimUri = mapping.getLocalClaim().getClaimUri();
                    mappedLocalClaims.put(localClaimUri, entry.getValue());
                }
            }
            context.setProperty("DEBUG_MAPPED_LOCAL_CLAIMS_MAP", mappedLocalClaims);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Created authenticated user: " + username + " with " + userAttributes.size() + " attributes");
            }

            return authenticatedUser;

        } catch (Exception e) {
            LOG.error("Error creating authenticated user: " + e.getMessage(), e);
            return null;
        }
    }

    private String buildRedirectUri(AuthenticationContext context) {
        // Use shared utility for redirect URI generation
        return DebugUtils.buildDebugCallbackUrl(context);
    }

    private HttpURLConnection createTokenRequest(String tokenEndpoint, String clientId, String clientSecret,
                                               String requestBody) throws Exception {
        URL url = new URL(tokenEndpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");

        // Set Basic Authentication header.
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);

        connection.setDoOutput(true);
        connection.setConnectTimeout(30000); 
        connection.setReadTimeout(30000);    
        
        // Send request body.
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        return connection;
    }    private String readResponse(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }

        StringBuilder response = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            response.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
        }
        return response.toString();
    }

    private TokenResponse parseTokenResponse(String responseBody) {
        try {
            // Simple JSON parsing for token response.
            String accessToken = extractJsonValue(responseBody, "access_token");
            String idToken = extractJsonValue(responseBody, "id_token");
            String refreshToken = extractJsonValue(responseBody, "refresh_token");
            String tokenType = extractJsonValue(responseBody, "token_type");
            
            return new TokenResponse(accessToken, idToken, refreshToken, tokenType);
        } catch (Exception e) {
            LOG.error("Error parsing token response: " + e.getMessage(), e);
            return null;
        }
    }

    private Map<String, Object> parseIdTokenClaims(String idToken) {
        try {
            // Parse JWT ID token (simplified implementation).
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                LOG.error("Invalid ID token format");
                return null;
            }

            // Decode payload (second part).
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return parseJsonToClaims(payload);

        } catch (Exception e) {
            LOG.error("Error parsing ID token claims: " + e.getMessage(), e);
            return null;
        }
    }

    private Map<String, Object> fetchUserInfoClaims(String accessToken, AuthenticationContext context) {
        // Get UserInfo endpoint from context.
        String userInfoEndpoint = (String) context.getProperty("DEBUG_USERINFO_ENDPOINT");
        if (userInfoEndpoint == null) {
            return new HashMap<>();
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(userInfoEndpoint).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                String responseBody = readResponse(connection.getInputStream());
                return parseJsonToClaims(responseBody);
            } else {
                return new HashMap<>();
            }
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String getClaimValue(Map<String, Object> claims, String... claimNames) {
        for (String claimName : claimNames) {
            Object value = claims.get(claimName);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    private String getIdpName(AuthenticationContext context) {
        try {
            IdentityProvider idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            if (idp != null) {
                return idp.getIdentityProviderName();
            }
        } catch (Exception e) {
            LOG.error("Error getting IdP name: " + e.getMessage(), e);
        }
        return "FederatedIdP";
    }

    private void mapClaimsToAttributes(Map<String, Object> claims, Map<ClaimMapping, String> userAttributes, AuthenticationContext context) {
        try {
            // Step status reporting: claim mapping started
            context.setProperty("DEBUG_STEP_CLAIM_MAPPING_STARTED", System.currentTimeMillis());

            IdentityProvider idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            ClaimMapping[] configuredMappings = null;
            if (idp != null && idp.getClaimConfig() != null && idp.getClaimConfig().getClaimMappings() != null) {
                configuredMappings = idp.getClaimConfig().getClaimMappings();
            }

            // If claim mappings are missing or empty, auto-map all extracted claim keys for this context only
            if (configuredMappings == null || configuredMappings.length == 0) {
                configuredMappings = new ClaimMapping[claims.size()];
                int i = 0;
                for (String claimKey : claims.keySet()) {
                    ClaimMapping autoMapping = new ClaimMapping();
                    autoMapping.setRemoteClaim(new Claim());
                    autoMapping.getRemoteClaim().setClaimUri(claimKey);
                    autoMapping.setLocalClaim(new Claim());
                    autoMapping.getLocalClaim().setClaimUri(claimKey);
                    configuredMappings[i++] = autoMapping;
                }
                // Step status reporting: auto-mapping used
                context.setProperty("DEBUG_STEP_CLAIM_MAPPING_AUTO", true);
            }

            // Enhanced normalization: try both URI→short name and short name→URI
            for (ClaimMapping configuredMapping : configuredMappings) {
                if (configuredMapping.getRemoteClaim() == null || configuredMapping.getLocalClaim() == null) {
                    continue;
                }
                String remoteClaimUri = configuredMapping.getRemoteClaim().getClaimUri();
                String localClaimUri = configuredMapping.getLocalClaim().getClaimUri();
                Object claimValue = claims.get(remoteClaimUri);
                String shortName = remoteClaimUri.contains("/") ? remoteClaimUri.substring(remoteClaimUri.lastIndexOf("/") + 1) : remoteClaimUri;
                // Try short name if not found
                if (claimValue == null && !shortName.equals(remoteClaimUri)) {
                    claimValue = claims.get(shortName);
                }
                // Try URI if mapping uses short name but claim key is URI
                if (claimValue == null && shortName.equals(remoteClaimUri)) {
                    // Try all claim keys that look like URIs and end with this short name
                    for (String claimKey : claims.keySet()) {
                        if (claimKey.contains("/") && claimKey.endsWith("/" + shortName)) {
                            claimValue = claims.get(claimKey);
                            break;
                        }
                    }
                }
                if (claimValue != null) {
                    // Use local claim URI as key for mapped claims
                    userAttributes.put(configuredMapping, claimValue.toString());
                    context.setProperty("DEBUG_MAPPED_LOCAL_CLAIMS", localClaimUri + ":" + claimValue.toString());
                }
            }
            // Step status reporting: claim mapping completed
            context.setProperty("DEBUG_STEP_CLAIM_MAPPING_COMPLETED", System.currentTimeMillis());
        } catch (Exception e) {
            LOG.error("Error mapping claims to attributes dynamically: " + e.getMessage(), e);
        }
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) {
            return null;
        }

        startIndex = json.indexOf(":", startIndex) + 1;
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }

        if (startIndex < json.length() && json.charAt(startIndex) == '"') {
            startIndex++;
            int endIndex = json.indexOf('"', startIndex);
            if (endIndex != -1) {
                return json.substring(startIndex, endIndex);
            }
        }

        return null;
    }

    private Map<String, Object> parseJsonToClaims(String json) {
        Map<String, Object> claims = new HashMap<>();
        try {
            // Simple JSON parsing (in production, use a proper JSON library).
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                
                String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":", 2);
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                        String value = keyValue[1].trim().replaceAll("^\"|\"$", "");
                        claims.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error parsing JSON to claims: " + e.getMessage(), e);
        }
        return claims;
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            LOG.error("Error encoding URL parameter: " + e.getMessage(), e);
            return value;
        }
    }

    /**
     * Simple token response holder class.
     */
    private static class TokenResponse {
        private final String accessToken;
        private final String idToken;
        private final String refreshToken;
        private final String tokenType;

        public TokenResponse(String accessToken, String idToken, String refreshToken, String tokenType) {
            this.accessToken = accessToken;
            this.idToken = idToken;
            this.refreshToken = refreshToken;
            this.tokenType = tokenType;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getIdToken() {
            return idToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public String getTokenType() {
            return tokenType;
        }
    }
}
