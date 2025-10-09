package org.wso2.carbon.identity.debug.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.core.ServiceURLBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Processes OAuth 2.0 Authorization Code callbacks from external IdPs.
 * Handles code exchange for tokens, validates tokens, extracts claims, and creates authenticated user objects.
 */
public class DebugProcessor {

    private static final Log LOG = LogFactory.getLog(DebugProcessor.class);
    private final Processor processor;

    public DebugProcessor() {
        this.processor = new Processor();
    }

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
        LOG.info("Processing OAuth 2.0 callback from external IdP for session: " + context.getContextIdentifier());
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Callback request URL: " + request.getRequestURL() + "?" + request.getQueryString());
        }

        try {
            // Extract callback parameters.
            String authorizationCode = request.getParameter("code");
            String state = request.getParameter("state");
            String error = request.getParameter("error");
            String errorDescription = request.getParameter("error_description");

            LOG.info("Callback parameters - Code: " + (authorizationCode != null ? "[PRESENT]" : "[MISSING]") +
                    ", State: " + (state != null ? state : "[MISSING]") +
                    ", Error: " + (error != null ? error : "[NONE]"));

            // Check for error response from IdP.
            if (error != null) {
                LOG.error("Authorization error from IdP: " + error + " - " + errorDescription);
                context.setProperty("DEBUG_AUTH_ERROR", error + ": " + errorDescription);
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                sendProcessedResponse(response, context);
                return;
            }

            // Validate required parameters.
            if (authorizationCode == null || authorizationCode.trim().isEmpty()) {
                LOG.error("Authorization code missing in callback");
                context.setProperty("DEBUG_AUTH_ERROR", "Authorization code not received from IdP");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                sendProcessedResponse(response, context);
                return;
            }

            if (state == null || state.trim().isEmpty()) {
                LOG.error("State parameter missing in callback");
                context.setProperty("DEBUG_AUTH_ERROR", "State parameter missing - possible CSRF attack");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                sendProcessedResponse(response, context);
                return;
            }

            // Validate state parameter (CSRF protection).
            String storedState = (String) context.getProperty("DEBUG_STATE");
            if (!state.equals(storedState)) {
                LOG.error("State parameter mismatch - CSRF attack detected");
                context.setProperty("DEBUG_AUTH_ERROR", "State validation failed - possible CSRF attack");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                sendProcessedResponse(response, context);
                return;
            }

            // Exchange authorization code for tokens.
            LOG.info("Exchanging authorization code for tokens");
            TokenResponse tokens = exchangeCodeForTokens(authorizationCode, context);
            if (tokens == null || tokens.getAccessToken() == null) {
                LOG.error("Token exchange failed - no tokens received from IdP");
                context.setProperty("DEBUG_AUTH_ERROR", "Failed to exchange authorization code for tokens");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                sendProcessedResponse(response, context);
                return;
            }

            LOG.info("Token exchange successful - Access Token: [PRESENT], ID Token: " + 
                    (tokens.getIdToken() != null ? "[PRESENT]" : "[NOT PRESENT]"));

            // Store token information in context for debug response
            context.setProperty("DEBUG_ACCESS_TOKEN", tokens.getAccessToken());
            if (tokens.getIdToken() != null) {
                context.setProperty("DEBUG_ID_TOKEN", tokens.getIdToken());
            }
            if (tokens.getRefreshToken() != null) {
                context.setProperty("DEBUG_REFRESH_TOKEN", tokens.getRefreshToken());
            }
            context.setProperty("DEBUG_TOKEN_TYPE", tokens.getTokenType());

            // Extract and validate claims from ID token and/or UserInfo endpoint.
            LOG.info("Extracting user claims from tokens");
            Map<String, Object> claims = extractUserClaims(tokens, context);
            if (claims == null || claims.isEmpty()) {
                LOG.error("Failed to extract user claims from tokens");
                context.setProperty("DEBUG_AUTH_ERROR", "Failed to extract user claims from tokens");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                sendProcessedResponse(response, context);
                return;
            }

            LOG.info("Claims extraction successful - Retrieved " + claims.size() + " claims");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Extracted claims: " + claims.keySet());
            }

            // Create authenticated user object.
            LOG.info("Creating authenticated user from claims");
            AuthenticatedUser authenticatedUser = createAuthenticatedUser(claims, context);
            if (authenticatedUser != null) {
                context.setSubject(authenticatedUser);
                context.setProperty("DEBUG_AUTH_SUCCESS", "true");
                context.setProperty("DEBUG_USER_EXISTS", true);
                context.setProperty("DEBUG_AUTH_COMPLETED", "true");
                context.setProperty("DEBUG_AUTH_COMPLETION_TIMESTAMP", System.currentTimeMillis());

                LOG.info("OAuth 2.0 authentication completed successfully for user: " + 
                         authenticatedUser.getUserName() + " (Subject: " + authenticatedUser.getAuthenticatedSubjectIdentifier() + 
                         ", IdP: " + authenticatedUser.getFederatedIdPName() + ")");
            } else {
                LOG.error("Failed to create authenticated user from claims");
                context.setProperty("DEBUG_AUTH_ERROR", "Failed to create authenticated user");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                context.setProperty("DEBUG_USER_EXISTS", false);
            }

            // Send processed response.
            sendProcessedResponse(response, context);

        } catch (Exception e) {
            LOG.error("Unexpected error processing OAuth 2.0 callback: " + e.getMessage(), e);
            context.setProperty("DEBUG_AUTH_ERROR", "Unexpected error: " + e.getMessage());
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            sendProcessedResponse(response, context);
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
            LOG.info("Initiating OAuth 2.0 token exchange for authorization code");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authorization code length: " + authorizationCode.length() + " characters");
            }

            // Get OAuth 2.0 configuration.
            String authenticatorName = (String) context.getProperty("DEBUG_AUTHENTICATOR_NAME");
            LOG.info("Token exchange for authenticator: " + authenticatorName);
            
            IdentityProvider idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            if (idp == null) {
                LOG.error("Identity Provider configuration not found in context");
                return null;
            }
            LOG.info("Using Identity Provider: " + idp.getIdentityProviderName());

            FederatedAuthenticatorConfig authenticatorConfig = findAuthenticatorConfig(idp, authenticatorName);
            if (authenticatorConfig == null) {
                LOG.error("Authenticator configuration not found");
                return null;
            }

            String clientId = getPropertyValue(authenticatorConfig, "ClientId", "client_id", "clientId");
            String clientSecret = getPropertyValue(authenticatorConfig, "ClientSecret", "client_secret", "clientSecret");
            String tokenEndpoint = getPropertyValue(authenticatorConfig, "OAuth2TokenEPUrl", "tokenEndpoint", "token_endpoint");

            // Handle Google-specific case where token endpoint might not be configured.
            if (tokenEndpoint == null && isGoogleOIDC(clientId, authenticatorConfig)) {
                tokenEndpoint = "https://oauth2.googleapis.com/token";
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using Google's standard token endpoint: " + tokenEndpoint);
                }
            }

            if (clientId == null || clientSecret == null || tokenEndpoint == null) {
                LOG.error("Missing OAuth 2.0 configuration for token exchange");
                return null;
            }

            // Get stored PKCE parameters.
            String codeVerifier = (String) context.getProperty("DEBUG_CODE_VERIFIER");
            String redirectUri = buildRedirectUri(context);

            // Prepare token request.
            LOG.info("Preparing token exchange request to endpoint: " + tokenEndpoint);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Token request parameters - Client ID: " + clientId + 
                         ", Redirect URI: " + redirectUri + ", Code Verifier: [PRESENT]");
            }

            String requestBody = "grant_type=authorization_code" +
                                "&code=" + urlEncode(authorizationCode) +
                                "&redirect_uri=" + urlEncode(redirectUri) +
                                "&client_id=" + urlEncode(clientId) +
                                "&code_verifier=" + urlEncode(codeVerifier);

            // Make token request with retry logic.
            LOG.info("Sending token exchange HTTP request");
            HttpURLConnection connection = null;
            int responseCode = -1;
            int maxRetries = 3;
            int attempt = 1;
            
            while (attempt <= maxRetries) {
                try {
                    LOG.info("Token exchange attempt " + attempt + " of " + maxRetries);
                    connection = createTokenRequest(tokenEndpoint, clientId, clientSecret, requestBody);
                    responseCode = connection.getResponseCode();
                    LOG.info("Token endpoint response code: " + responseCode + " (attempt " + attempt + ")");
                    break; // Success - exit retry loop
                } catch (java.net.SocketTimeoutException e) {
                    LOG.warn("Token exchange attempt " + attempt + " timed out: " + e.getMessage());
                    if (attempt == maxRetries) {
                        LOG.error("All " + maxRetries + " token exchange attempts failed due to timeout");
                        throw e; // Re-throw on final attempt
                    }
                    attempt++;
                    // Wait before retry (exponential backoff)
                    Thread.sleep(2000 * attempt);
                } catch (Exception e) {
                    LOG.error("Token exchange attempt " + attempt + " failed with exception: " + e.getMessage());
                    if (attempt == maxRetries) {
                        throw e; // Re-throw on final attempt
                    }
                    attempt++;
                    Thread.sleep(1000 * attempt);
                }
            }

            if (responseCode == 200) {
                String responseBody = readResponse(connection.getInputStream());
                LOG.info("Token exchange successful - parsing response");
                TokenResponse tokenResponse = parseTokenResponse(responseBody);
                
                if (tokenResponse != null) {
                    LOG.info("Token parsing successful - Access Token: [PRESENT], ID Token: " + 
                            (tokenResponse.getIdToken() != null ? "[PRESENT]" : "[NOT PRESENT]"));
                } else {
                    LOG.error("Token response parsing failed despite successful HTTP response");
                }
                
                return tokenResponse;
            } else {
                String errorResponse = readResponse(connection.getErrorStream());
                LOG.error("=== OAuth Token Exchange Failed ===");
                LOG.error("HTTP Status Code: " + responseCode);
                LOG.error("Error Response: " + errorResponse);
                LOG.error("Token Endpoint: " + tokenEndpoint);
                LOG.error("Client ID: " + clientId);
                LOG.error("Redirect URI: " + redirectUri);
                LOG.error("=== End OAuth Error Details ===");
                
                // If callback URL mismatch, provide helpful guidance
                if (errorResponse != null && errorResponse.contains("Callback url mismatch")) {
                    LOG.error("CALLBACK URL MISMATCH DETECTED!");
                    LOG.error("This error occurs when the redirect_uri in the token request doesn't match");
                    LOG.error("the registered redirect URIs in your Asgardeo application configuration.");
                    LOG.error("Generated redirect_uri: " + redirectUri);
                    LOG.error("Please ensure this URL is added to your Asgardeo app's 'Allowed Grant Types' and 'Callback URLs'");
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
            LOG.info("Starting user claims extraction from tokens");
            Map<String, Object> claims = new HashMap<>();

            // Extract claims from ID token if available.
            if (tokens.getIdToken() != null) {
                LOG.info("ID Token found - extracting claims from JWT");
                Map<String, Object> idTokenClaims = parseIdTokenClaims(tokens.getIdToken());
                if (idTokenClaims != null) {
                    claims.putAll(idTokenClaims);
                    LOG.info("Extracted " + idTokenClaims.size() + " claims from ID Token");
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("ID Token claims: " + idTokenClaims.keySet());
                    }
                } else {
                    LOG.warn("Failed to parse ID Token claims");
                }
            } else {
                LOG.info("No ID Token available for claims extraction");
            }

            // Extract additional claims from UserInfo endpoint if access token is available.
            if (tokens.getAccessToken() != null) {
                LOG.info("Access Token found - attempting UserInfo endpoint call");
                Map<String, Object> userInfoClaims = fetchUserInfoClaims(tokens.getAccessToken(), context);
                if (userInfoClaims != null) {
                    claims.putAll(userInfoClaims);
                    LOG.info("Extracted " + userInfoClaims.size() + " additional claims from UserInfo endpoint");
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("UserInfo claims: " + userInfoClaims.keySet());
                    }
                } else {
                    LOG.warn("UserInfo endpoint call failed or returned no claims");
                }
            } else {
                LOG.info("No Access Token available for UserInfo endpoint call");
            }

            if (claims.isEmpty()) {
                LOG.error("No claims extracted from tokens - authentication cannot proceed");
                return null;
            }

            LOG.info("Claims extraction completed successfully - Total claims: " + claims.size());
            if (LOG.isDebugEnabled()) {
                LOG.debug("All extracted claims: " + claims.keySet());
                // Log essential identity claims if available
                if (claims.containsKey("sub")) {
                    LOG.debug("Subject identifier: " + claims.get("sub"));
                }
                if (claims.containsKey("email")) {
                    LOG.debug("Email claim present: " + claims.get("email"));
                }
                if (claims.containsKey("name")) {
                    LOG.debug("Name claim present: " + claims.get("name"));
                }
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
        try {
            // Extract essential user information.
            String subject = getClaimValue(claims, "sub", "user_id", "id");
            String email = getClaimValue(claims, "email");
            String name = getClaimValue(claims, "name", "given_name", "family_name");
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
            mapClaimsToAttributes(claims, userAttributes);
            authenticatedUser.setUserAttributes(userAttributes);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Created authenticated user: " + username + " with " + userAttributes.size() + " attributes");
            }

            return authenticatedUser;

        } catch (Exception e) {
            LOG.error("Error creating authenticated user: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Sends the processed response using the existing Processor.
     *
     * @param response HttpServletResponse.
     * @param context AuthenticationContext with processing results.
     * @throws IOException If response sending fails.
     */
    private void sendProcessedResponse(HttpServletResponse response, AuthenticationContext context) throws IOException {
        try {
            // Mark callback as processed.
            context.setProperty("DEBUG_CALLBACK_PROCESSED", "true");
            context.setProperty("DEBUG_CALLBACK_RESULT", context.getProperty("DEBUG_AUTH_SUCCESS"));
            context.setProperty("DEBUG_CALLBACK_TIMESTAMP", System.currentTimeMillis());

            // Use existing Processor to generate structured response.
            Object processedResult = processor.process(context);

            // Send JSON response.
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            // Convert processed result to JSON string.
            String jsonResponse = convertToJson(processedResult);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug processing response sent successfully");
            }

        } catch (Exception e) {
            LOG.error("Error sending processed response: " + e.getMessage(), e);
            throw new IOException("Failed to send response", e);
        }
    }

    // Utility methods

    private FederatedAuthenticatorConfig findAuthenticatorConfig(IdentityProvider idp, String authenticatorName) {
        FederatedAuthenticatorConfig[] configs = idp.getFederatedAuthenticatorConfigs();
        if (configs != null) {
            for (FederatedAuthenticatorConfig config : configs) {
                if (authenticatorName.equals(config.getName())) {
                    return config;
                }
            }
        }
        return null;
    }

    private String getPropertyValue(FederatedAuthenticatorConfig config, String... propertyNames) {
        if (config.getProperties() != null) {
            for (org.wso2.carbon.identity.application.common.model.Property prop : config.getProperties()) {
                for (String propName : propertyNames) {
                    if (propName.equalsIgnoreCase(prop.getName())) {
                        return prop.getValue();
                    }
                }
            }
        }
        return null;
    }

    private boolean isGoogleOIDC(String clientId, FederatedAuthenticatorConfig config) {
        if (clientId != null && (clientId.contains("googleusercontent.com") || 
                                (clientId.matches("\\d+") && clientId.length() > 10))) {
            return true;
        }
        
        if (config.getProperties() != null) {
            for (org.wso2.carbon.identity.application.common.model.Property prop : config.getProperties()) {
                if (prop.getValue() != null && 
                    (prop.getValue().contains("googleapis.com") || 
                     prop.getValue().contains("google.com") ||
                     prop.getValue().toLowerCase().contains("google"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String buildRedirectUri(AuthenticationContext context) {
        try {
            // IMPORTANT: Must match EXACTLY the redirect_uri used in authorization URL generation
            // OAuth 2.0 spec requires exact match between authorization and token exchange requests
            String baseUrl = ServiceURLBuilder.create().build().getAbsolutePublicURL();
            String redirectUri = baseUrl + "/commonauth";
            
            // Cross-reference with authorization URL generation from Executer.buildDebugCallbackUrl()
            String contextCallbackUrl = (String) context.getProperty("DEBUG_CALLBACK_URL_USED");
            
            LOG.info("=== TOKEN EXCHANGE REDIRECT URI ===");
            LOG.info("Built redirect_uri: " + redirectUri);
            LOG.info("Authorization callback URL from context: " + contextCallbackUrl);
            
            // Use the same URL as used in authorization if available from context
            if (contextCallbackUrl != null && !contextCallbackUrl.isEmpty()) {
                LOG.info("Using authorization callback URL for consistency: " + contextCallbackUrl);
                LOG.info("=== END REDIRECT URI INFO ===");
                return contextCallbackUrl;
            } else {
                LOG.info("Context callback URL not available, using generated URL");
                LOG.info("This MUST match the redirect_uri used in authorization URL generation");
                LOG.info("=== END REDIRECT URI INFO ===");
                return redirectUri;
            }
        } catch (Exception e) {
            LOG.error("Error building redirect URI: " + e.getMessage(), e);
            return "/commonauth";
        }
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
        // Increase timeouts for better reliability with Asgardeo
        connection.setConnectTimeout(30000); // 30 seconds
        connection.setReadTimeout(30000);    // 30 seconds
        
        LOG.info("OAuth token request - Connect timeout: 30s, Read timeout: 30s");

        // Send request body.
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
            LOG.info("Token request body sent successfully");
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
        try {
            // Get UserInfo endpoint from configuration.
            String authenticatorName = (String) context.getProperty("DEBUG_AUTHENTICATOR_NAME");
            IdentityProvider idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            FederatedAuthenticatorConfig config = findAuthenticatorConfig(idp, authenticatorName);
            
            String userInfoEndpoint = getPropertyValue(config, "UserInfoEndpoint", "userinfo_endpoint", "userInfoUrl");
            if (userInfoEndpoint == null && isGoogleOIDC((String) context.getProperty("CLIENT_ID"), config)) {
                userInfoEndpoint = "https://openidconnect.googleapis.com/v1/userinfo";
            }

            if (userInfoEndpoint == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("UserInfo endpoint not configured, skipping UserInfo request");
                }
                return new HashMap<>();
            }

            // Make UserInfo request.
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
                LOG.error("UserInfo request failed with status: " + responseCode);
                return new HashMap<>();
            }

        } catch (Exception e) {
            LOG.error("Error fetching UserInfo claims: " + e.getMessage(), e);
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

    private void mapClaimsToAttributes(Map<String, Object> claims, Map<ClaimMapping, String> userAttributes) {
        try {
            // Create claim mappings for common claims.
            String[][] commonMappings = {
                {"sub", "http://wso2.org/claims/userid"},
                {"email", "http://wso2.org/claims/emailaddress"},
                {"name", "http://wso2.org/claims/fullname"},
                {"given_name", "http://wso2.org/claims/givenname"},
                {"family_name", "http://wso2.org/claims/lastname"},
                {"preferred_username", "http://wso2.org/claims/username"}
            };

            for (String[] mapping : commonMappings) {
                String remoteClaim = mapping[0];
                String localClaim = mapping[1];
                Object value = claims.get(remoteClaim);

                if (value != null) {
                    ClaimMapping claimMapping = new ClaimMapping();
                    org.wso2.carbon.identity.application.common.model.Claim remoteCl = 
                        new org.wso2.carbon.identity.application.common.model.Claim();
                    remoteCl.setClaimUri(remoteClaim);
                    
                    org.wso2.carbon.identity.application.common.model.Claim localCl = 
                        new org.wso2.carbon.identity.application.common.model.Claim();
                    localCl.setClaimUri(localClaim);
                    
                    claimMapping.setRemoteClaim(remoteCl);
                    claimMapping.setLocalClaim(localCl);
                    
                    userAttributes.put(claimMapping, value.toString());
                }
            }

        } catch (Exception e) {
            LOG.error("Error mapping claims to attributes: " + e.getMessage(), e);
        }
    }

    private String convertToJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        // Simple JSON serialization (in production, use a proper JSON library).
        if (obj instanceof Map) {
            StringBuilder json = new StringBuilder("{");
            Map<?, ?> map = (Map<?, ?>) obj;
            boolean first = true;
            
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":");
                json.append(convertToJson(entry.getValue()));
                first = false;
            }
            
            json.append("}");
            return json.toString();
        } else if (obj instanceof String) {
            return "\"" + obj.toString().replace("\"", "\\\"") + "\"";
        } else {
            return obj.toString();
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
