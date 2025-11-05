
/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.debug.framework.client.OAuth2TokenClient;
import org.wso2.carbon.identity.debug.framework.client.TokenResponse;
import org.wso2.carbon.identity.debug.framework.internal.DebugFrameworkServiceDataHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    // Jackson ObjectMapper for robust JSON parsing.
    private static final ObjectMapper mapper = new ObjectMapper();

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
                    context.setProperty("DEBUG_AUTH_ERROR",
                        "Authorization code already used in this session. Please retry login.");
                    context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state
                        + "&idpId=" + idpId);
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
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state
                        + "&idpId=" + idpId);
                }
                return;
            }

            if (authorizationCode == null || authorizationCode.trim().isEmpty()) {
                LOG.error("Authorization code missing in callback");
                context.setProperty("DEBUG_AUTH_ERROR", "Authorization code not received from IdP");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state
                        + "&idpId=" + idpId);
                }
                return;
            }

            if (state == null || state.trim().isEmpty()) {
                LOG.error("State parameter missing in callback");
                context.setProperty("DEBUG_AUTH_ERROR", "State parameter missing - possible CSRF attack");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state
                        + "&idpId=" + idpId);
                }
                return;
            }

            String storedState = (String) context.getProperty("DEBUG_STATE");
            if (!state.equals(storedState)) {
                LOG.error("State parameter mismatch - CSRF attack detected");
                context.setProperty("DEBUG_AUTH_ERROR", "State validation failed - possible CSRF attack");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state
                        + "&idpId=" + idpId);
                }
                return;
            }

            TokenResponse tokens = exchangeCodeForTokens(authorizationCode, context);
            if (tokens == null || tokens.getAccessToken() == null) {
                // Check if there's error information in the token response.
                String errorMsg = "Failed to exchange authorization code for tokens";
                String errorDetails = "";
                if (tokens != null && tokens.hasError()) {
                    errorMsg = tokens.getErrorCode() + ": " + tokens.getErrorDescription();
                    errorDetails = tokens.getErrorDetails();
                    context.setProperty("step_connection_error_code", tokens.getErrorCode());
                    context.setProperty("step_connection_error_description", tokens.getErrorDescription());
                    context.setProperty("step_connection_error_details", errorDetails);
                } else {
                    context.setProperty("step_connection_error_code", "TOKEN_EXCHANGE_FAILED");
                    context.setProperty("step_connection_error_description", "No tokens received from IdP");
                }
                
                LOG.error("Token exchange failed - " + errorMsg);
                context.setProperty("DEBUG_AUTH_ERROR", errorMsg);
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                context.setProperty("step_connection_status", "failed");
                context.setProperty("step_connection_error", errorMsg);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Token exchange error details: " + errorDetails);
                }
                
                // Build and cache error response
                buildAndCacheTokenExchangeErrorResponse(tokens, state, context);
                redirectToDebugSuccess(response, state, idpId);
                return;
            }
            context.setProperty("step_connection_status", "success");

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
                context.setProperty("step_claim_extraction_status", "failed");
                context.setProperty("step_claim_extraction_error", "No claims found in ID token or UserInfo endpoint");
                
                // Cache the error result so GET endpoint can retrieve it.
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> debugResult = new HashMap<>();
                    
                    debugResult.put("success", "false");
                    debugResult.put("error", "Failed to extract user claims from tokens");
                    debugResult.put("sessionId", state);
                    debugResult.put("idpName", context.getProperty("DEBUG_IDP_NAME"));
                    debugResult.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
                    debugResult.put("timestamp", System.currentTimeMillis());
                    
                    // Step statuses.
                    debugResult.put("step_connection_status", "success");
                    debugResult.put("step_authentication_status", "failed");
                    debugResult.put("step_claim_extraction_status", "failed");
                    debugResult.put("step_claim_mapping_status", "not_started");
                    
                    // ID Token if available.
                    String idToken = (String) context.getProperty("DEBUG_ID_TOKEN");
                    if (idToken != null) {
                        debugResult.put("idToken", idToken);
                    }
                    
                    // Error details.
                    Map<String, Object> extractionError = new HashMap<>();
                    extractionError.put("error", "No claims found in ID token or UserInfo endpoint");
                    debugResult.put("step_claim_extraction_error", extractionError);
                    
                    // Claim mapping diagnostics (if any)
                    Object mappingDiagnostic = context.getProperty("DEBUG_CLAIM_MAPPING_DIAGNOSTIC");
                    if (mappingDiagnostic != null) {
                        debugResult.put("claimMappingDiagnostic", mappingDiagnostic);
                    }
                    
                    debugResult.put("externalRedirectUrl", context.getProperty("DEBUG_EXTERNAL_REDIRECT_URL"));
                    debugResult.put("callbackUrl", context.getProperty("DEBUG_CALLBACK_URL_USED"));
                    
                    String resultJson = mapper.writeValueAsString(debugResult);
                    DebugResultCache.add(state, resultJson);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Debug error result cached for state: " + state);
                    }
                } catch (Exception cacheEx) {
                    LOG.error("Failed to cache debug error result", cacheEx);
                }
                
                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state +
                        "&idpId=" + idpId);
                }
                return;
            }
            context.setProperty("step_claim_extraction_status", "success");

            AuthenticatedUser authenticatedUser = createAuthenticatedUser(claims, context);
            if (authenticatedUser != null) {
                context.setSubject(authenticatedUser);
                context.setProperty("DEBUG_AUTH_SUCCESS", "true");
                context.setProperty("DEBUG_USER_EXISTS", true);
                context.setProperty("DEBUG_AUTH_COMPLETED", "true");
                context.setProperty("DEBUG_AUTH_COMPLETION_TIMESTAMP", System.currentTimeMillis());
                context.setProperty("step_authentication_status", "success");

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
                    String executorClass = executorObj != null ? executorObj.getClass().getSimpleName()

                            : "UnknownExecutor";
                    debugResult.put("executor", executorClass);
                    debugResult.put("timestamp", context.getProperty("DEBUG_AUTH_COMPLETION_TIMESTAMP"));

                    // Incoming claims (from IdP)
                    debugResult.put("incomingClaims", claims);

                    // Mapped claims (local claims) with formatted display
                    Object mappedClaimsObj = context.getProperty("DEBUG_MAPPED_LOCAL_CLAIMS_MAP");
                    Map<String, String> formattedMappedClaims =
                            formatMappedClaimsWithStandardURIs(mappedClaimsObj, context);
                    debugResult.put("mappedClaims", formattedMappedClaims);

                    // IdP configured claim mappings (for reference)
                    Object idpConfiguredMappings = context.getProperty("DEBUG_IDP_CONFIGURED_MAPPINGS");
                    if (idpConfiguredMappings != null) {
                        debugResult.put("idpConfiguredClaimMappings", idpConfiguredMappings);
                    }

                    // Claim mapping diagnostics
                    Object mappingDiagnostic = context.getProperty("DEBUG_CLAIM_MAPPING_DIAGNOSTIC");
                    if (mappingDiagnostic != null) {
                        debugResult.put("claimMappingDiagnostic", mappingDiagnostic);
                    }
                    if (context.getProperty("DEBUG_STEP_CLAIM_MAPPING_AUTO_USED") != null) {
                        debugResult.put("autoMappingUsed", context.getProperty("DEBUG_STEP_CLAIM_MAPPING_AUTO_USED"));
                    }

                    // Set claim mapping status to success if mapped claims are present
                    if (formattedMappedClaims != null && !formattedMappedClaims.isEmpty()) {
                        context.setProperty("step_claim_mapping_status", "success");
                        debugResult.put("step_claim_mapping_status", "success");
                    } else {
                        debugResult.put("step_claim_mapping_status", "failed");
                    }

                    // User info
                    debugResult.put("username", authenticatedUser != null ? authenticatedUser.getUserName() : null);
                    debugResult.put("userId", authenticatedUser != null ? authenticatedUser.getUserId() : null);

                    // ID Token
                    String idToken = (String) context.getProperty("DEBUG_ID_TOKEN");
                    if (idToken != null) {
                        debugResult.put("idToken", idToken);
                    }

                    // Minimal debug metadata
                    debugResult.put("externalRedirectUrl", context.getProperty("DEBUG_EXTERNAL_REDIRECT_URL"));
                    debugResult.put("callbackUrl", context.getProperty("DEBUG_CALLBACK_URL_USED"));

                    // Add step status fields - use correct property keys with step_ prefix
                    debugResult.put("step_connection_status",
                            context.getProperty("step_connection_status"));
                    debugResult.put("step_authentication_status",
                            context.getProperty("step_authentication_status"));
                    debugResult.put("step_claim_extraction_status",
                            context.getProperty("step_claim_extraction_status"));
                    // step_claim_mapping_status already set above if mapped claims present

                    // Add step error details if any step failed
                    if (context.getProperty("step_connection_error") != null) {
                        Map<String, Object> connectionError = new HashMap<>();
                        connectionError.put("error", context.getProperty("step_connection_error"));
                        connectionError.put("errorCode",
                                context.getProperty("step_connection_error_code"));
                        connectionError.put("errorDescription",
                                context.getProperty("step_connection_error_description"));
                        if (context.getProperty("step_connection_error_details") != null) {
                            connectionError.put("details",
                                    context.getProperty("step_connection_error_details"));
                        }
                        debugResult.put("step_connection_error", connectionError);
                    }
                    if (context.getProperty("step_claim_extraction_error") != null) {
                        Map<String, Object> extractionError = new HashMap<>();
                        extractionError.put("error", context.getProperty("step_claim_extraction_error"));
                        if (context.getProperty("step_claim_extraction_error_details") != null) {
                            extractionError.put("details", context.getProperty("step_claim_extraction_error_details"));
                        }
                        debugResult.put("step_claim_extraction_error", extractionError);
                    }
                    if (context.getProperty("step_authentication_error") != null) {
                        Map<String, Object> authError = new HashMap<>();
                        authError.put("error", context.getProperty("step_authentication_error"));
                        if (context.getProperty("step_authentication_error_details") != null) {
                            authError.put("details", context.getProperty("step_authentication_error_details"));
                        }
                        debugResult.put("step_authentication_error", authError);
                    }

                    // User attributes: Only include mapped claims (cleaned up version)
                    debugResult.put("userAttributes", formattedMappedClaims);

                    // Add metadata summary
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("step_connection_status", context.getProperty("step_connection_status"));
                    metadata.put("step_authentication_status", context.getProperty("step_authentication_status"));
                    metadata.put("step_claim_extraction_status", context.getProperty("step_claim_extraction_status"));
                    metadata.put("step_claim_mapping_status", context.getProperty("step_claim_mapping_status"));
                    debugResult.put("metadata", metadata);

                    String resultJson = mapper.writeValueAsString(debugResult);
                    DebugResultCache.add(state, resultJson);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Debug result cached for state: " + state);
                    }
                } catch (Exception cacheEx) {
                    LOG.error("Failed to cache debug result", cacheEx);
                }

                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state +
                        "&idpId=" + idpId);
                }
            } else {
                LOG.error("Failed to create authenticated user from claims");
                context.setProperty("DEBUG_AUTH_ERROR", "Failed to create authenticated user");
                context.setProperty("DEBUG_AUTH_SUCCESS", "false");
                context.setProperty("DEBUG_USER_EXISTS", false);
                context.setProperty("step_authentication_status", "failed");
                context.setProperty("step_authentication_error", "User creation failed: missing or invalid claims");

                // Even if user creation failed, generate a detailed debug response
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> debugResult = new HashMap<>();

                    // Authentication status
                    debugResult.put("success", "false");
                    debugResult.put("error", context.getProperty("DEBUG_AUTH_ERROR"));
                    debugResult.put("sessionId", state);
                    debugResult.put("idpName", context.getProperty("DEBUG_IDP_NAME"));
                    debugResult.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
                    Object executorObj = context.getProperty("DEBUG_EXECUTOR_INSTANCE");
                    String executorClass = executorObj != null 
                            ? executorObj.getClass().getSimpleName() 
                            : "UnknownExecutor";
                    debugResult.put("executor", executorClass);
                    debugResult.put("timestamp", System.currentTimeMillis());

                    // Incoming claims (from IdP)
                    debugResult.put("incomingClaims", context.getProperty("DEBUG_INCOMING_CLAIMS"));

                    // Mapped claims (local claims) if available
                    Object mappedClaimsObj = context.getProperty("DEBUG_MAPPED_LOCAL_CLAIMS_MAP");
                    Map<String, String> formattedMappedClaims =
                            formatMappedClaimsWithStandardURIs(mappedClaimsObj, context);
                    debugResult.put("mappedClaims", formattedMappedClaims);

                    // IdP configured claim mappings (for reference)
                    Object idpConfiguredMappings = context.getProperty("DEBUG_IDP_CONFIGURED_MAPPINGS");
                    if (idpConfiguredMappings != null) {
                        debugResult.put("idpConfiguredClaimMappings", idpConfiguredMappings);
                    }

                    // Claim mapping diagnostics
                    Object mappingDiagnostic = context.getProperty("DEBUG_CLAIM_MAPPING_DIAGNOSTIC");
                    if (mappingDiagnostic != null) {
                        debugResult.put("claimMappingDiagnostic", mappingDiagnostic);
                    }
                    if (context.getProperty("DEBUG_STEP_CLAIM_MAPPING_AUTO_USED") != null) {
                        debugResult.put("autoMappingUsed", context.getProperty("DEBUG_STEP_CLAIM_MAPPING_AUTO_USED"));
                    }

                    // ID Token if available
                    String idToken = (String) context.getProperty("DEBUG_ID_TOKEN");
                    if (idToken != null) {
                        debugResult.put("idToken", idToken);
                    }
                        
                    // Step status fields - use correct property keys with step_ prefix
                    debugResult.put("step_connection_status",
                            context.getProperty("step_connection_status"));
                    debugResult.put("step_authentication_status",
                            context.getProperty("step_authentication_status"));
                    debugResult.put("step_claim_extraction_status",
                            context.getProperty("step_claim_extraction_status"));
                    debugResult.put("step_claim_mapping_status",
                            context.getProperty("step_claim_mapping_status") != null 
                            ? context.getProperty("step_claim_mapping_status") 
                            : "failed");

                    // Add step error details if any step failed
                    if (context.getProperty("step_connection_error") != null) {
                        Map<String, Object> connectionError = new HashMap<>();
                        connectionError.put("error", context.getProperty("step_connection_error"));
                        connectionError.put("errorCode", context.getProperty("step_connection_error_code"));
                        connectionError.put("errorDescription",
                                context.getProperty("step_connection_error_description"));
                        if (context.getProperty("step_connection_error_details") != null) {
                            connectionError.put("details",
                                    context.getProperty("step_connection_error_details"));
                        }
                        debugResult.put("step_connection_error", connectionError);
                    }
                    if (context.getProperty("step_claim_extraction_error") != null) {
                        Map<String, Object> extractionError = new HashMap<>();
                        extractionError.put("error", context.getProperty("step_claim_extraction_error"));
                        if (context.getProperty("step_claim_extraction_error_details") != null) {
                            extractionError.put("details", context.getProperty("step_claim_extraction_error_details"));
                        }
                        debugResult.put("step_claim_extraction_error", extractionError);
                    }
                    if (context.getProperty("step_authentication_error") != null) {
                        Map<String, Object> authError = new HashMap<>();
                        authError.put("error", context.getProperty("step_authentication_error"));
                        if (context.getProperty("step_authentication_error_details") != null) {
                            authError.put("details", context.getProperty("step_authentication_error_details"));
                        }
                        debugResult.put("step_authentication_error", authError);
                    }

                    // Debug metadata
                    debugResult.put("externalRedirectUrl", context.getProperty("DEBUG_EXTERNAL_REDIRECT_URL"));
                    debugResult.put("callbackUrl", context.getProperty("DEBUG_CALLBACK_URL_USED"));

                    // Metadata summary
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("step_connection_status", context.getProperty("step_connection_status"));
                    metadata.put("step_authentication_status", context.getProperty("step_authentication_status"));
                    metadata.put("step_claim_extraction_status", context.getProperty("step_claim_extraction_status"));
                    metadata.put("step_claim_mapping_status", context.getProperty("step_claim_mapping_status"));
                    debugResult.put("metadata", metadata);

                    String resultJson = mapper.writeValueAsString(debugResult);
                    DebugResultCache.add(state, resultJson);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Debug result cached (with authentication failure) for state: " + state);
                    }
                } catch (Exception cacheEx) {
                    LOG.error("Failed to cache debug result", cacheEx);
                }

                if (!response.isCommitted()) {
                    response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state +
                        "&idpId=" + idpId);
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
        OAuth2TokenClient tokenClient = new OAuth2TokenClient();
        return tokenClient.exchangeCodeForTokens(authorizationCode, context);
    }

    /**
     * Extracts user claims from tokens (ID token and/or UserInfo endpoint).
     * Stores the claims in the context for use in debug responses.
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
                OAuth2TokenClient tokenClient = new OAuth2TokenClient();
                Map<String, Object> userInfoClaims = tokenClient.fetchUserInfoClaims(tokens.getAccessToken(), context);
                if (userInfoClaims != null && !userInfoClaims.isEmpty()) {
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

            // Store incoming claims in context for use in debug responses.
            context.setProperty("DEBUG_INCOMING_CLAIMS", claims);

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
            if (idpLog != null && idpLog.getClaimConfig() != null &&

                    idpLog.getClaimConfig().getClaimMappings() != null) {
                // Capture IdP configured claim mappings for debug response.
                Map<String, Map<String, String>> idpConfiguredMappings = new HashMap<>();
                for (ClaimMapping cm : idpLog.getClaimConfig().getClaimMappings()) {
                    if (cm != null && cm.getRemoteClaim() != null && cm.getLocalClaim() != null) {
                        Map<String, String> mapping = new HashMap<>();
                        mapping.put("remote", cm.getRemoteClaim().getClaimUri());
                        mapping.put("local", cm.getLocalClaim().getClaimUri());
                        
                        // Use remote claim as key for the mapping
                        String remoteClaimUri = cm.getRemoteClaim().getClaimUri();
                        idpConfiguredMappings.put(remoteClaimUri, mapping);
                    }
                }
                context.setProperty("DEBUG_IDP_CONFIGURED_MAPPINGS", idpConfiguredMappings);
                
                if (LOG.isDebugEnabled()) {
                    StringBuilder mappingsLog = new StringBuilder("[DEBUG] Configured claim mappings: [");
                    for (ClaimMapping cm : idpLog.getClaimConfig().getClaimMappings()) {
                        if (cm != null && cm.getRemoteClaim() != null && cm.getLocalClaim() != null) {
                            mappingsLog.append("{remote: ").append(cm.getRemoteClaim().getClaimUri())
                                .append(", local: ").append(cm.getLocalClaim().getClaimUri()).append("}, ");
                        }
                    }
                    mappingsLog.append("]");
                    LOG.debug(mappingsLog.toString());
                }
            }
        try {
            // Extract essential user information.
            String subject = getClaimValue(claims, "sub", "user_id", "id");
            String email = getClaimValue(claims, "email");
            String preferredUsername = getClaimValue(claims, "preferred_username", "username");

            // Use email as username if preferred_username is not available.
            String username = preferredUsername != null ? preferredUsername : email;
            if (username == null) {
                username = subject; 
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
                        // If the remote URI is not found in claims, but a short name is, update the mapping for this

                        // context
                        if (!claims.containsKey(remoteUri) && remoteUri.contains("/")) {
                            String shortName = remoteUri.substring(remoteUri.lastIndexOf("/") + 1);
                            if (claims.containsKey(shortName)) {
                                // Update the remote claim URI in-memory for this mapping (for this context only)
                                mapping.getRemoteClaim().setClaimUri(shortName);
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Auto-mapped remote claim URI from '" + remoteUri + "' to short name '" +

                                            shortName + "' for this context.");
                                }
                            }
                        }
                    }
                }
            }

            // Create federated authenticated user.
            AuthenticatedUser authenticatedUser =
                    AuthenticatedUser.createFederateAuthenticatedUserFromSubjectIdentifier(subject);
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

    private Map<String, Object> parseIdTokenClaims(String idToken) {
        try {
            // Parse JWT ID token (simplified implementation).
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                LOG.error("Invalid ID token format");
                return null;
            }

            // Decode payload (second part)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return parseJsonToClaims(payload);

        } catch (Exception e) {
            LOG.error("Error parsing ID token claims: " + e.getMessage(), e);
            return null;
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

    private void mapClaimsToAttributes(Map<String, Object> claims, Map<ClaimMapping, String> userAttributes,

            AuthenticationContext context) {
        try {
            // Step status reporting: claim mapping started
            context.setProperty("DEBUG_STEP_CLAIM_MAPPING_STARTED", System.currentTimeMillis());

            IdentityProvider idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            ClaimMapping[] configuredMappings = null;
            if (idp != null && idp.getClaimConfig() != null && idp.getClaimConfig().getClaimMappings() != null) {
                configuredMappings = idp.getClaimConfig().getClaimMappings();
            }

            // If no configured mappings, log diagnostic info.
            if (configuredMappings == null || configuredMappings.length == 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No configured claim mappings found in IdP. Incoming claims: " + claims.keySet());
                }
                context.setProperty("DEBUG_CLAIM_MAPPING_DIAGNOSTIC", 
                        "No configured claim mappings in IdP. Received claims: " + claims.keySet());
                
                // Auto-map all incoming claims if no mappings configured.
                for (Map.Entry<String, Object> claimEntry : claims.entrySet()) {
                    ClaimMapping autoMapping = new ClaimMapping();
                    org.wso2.carbon.identity.application.common.model.Claim remoteClaim = 
                            new org.wso2.carbon.identity.application.common.model.Claim();
                    remoteClaim.setClaimUri(claimEntry.getKey());
                    autoMapping.setRemoteClaim(remoteClaim);
                    
                    org.wso2.carbon.identity.application.common.model.Claim localClaim = 
                            new org.wso2.carbon.identity.application.common.model.Claim();
                    localClaim.setClaimUri(claimEntry.getKey());
                    autoMapping.setLocalClaim(localClaim);
                    
                    userAttributes.put(autoMapping, claimEntry.getValue().toString());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Auto-mapped claim: " + claimEntry.getKey() + " = "
                                + claimEntry.getValue());
                    }
                }
                context.setProperty("DEBUG_STEP_CLAIM_MAPPING_AUTO_USED", true);
                context.setProperty("DEBUG_STEP_CLAIM_MAPPING_COMPLETED", System.currentTimeMillis());
                return;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing " + configuredMappings.length
                        + " configured claim mappings. Incoming claims: " + claims.keySet());
            }

            int mappedCount = 0;
            Map<String, String> mappingDiagnostics = new HashMap<>();

            // Enhanced normalization: try both URI→short name and short name→URI
            for (ClaimMapping configuredMapping : configuredMappings) {
                if (configuredMapping.getRemoteClaim() == null || configuredMapping.getLocalClaim() == null) {
                    continue;
                }
                String remoteClaimUri = configuredMapping.getRemoteClaim().getClaimUri();
                String localClaimUri = configuredMapping.getLocalClaim().getClaimUri();
                Object claimValue = claims.get(remoteClaimUri);
                String shortName = remoteClaimUri.contains("/") ?
                        remoteClaimUri.substring(remoteClaimUri.lastIndexOf("/") + 1) : remoteClaimUri;
                
                // Try short name if not found
                if (claimValue == null && !shortName.equals(remoteClaimUri)) {
                    claimValue = claims.get(shortName);
                    if (claimValue != null && LOG.isDebugEnabled()) {
                        LOG.debug("Found claim using short name: " + shortName
                                + " for remote claim: " + remoteClaimUri);
                    }
                }
                // Try URI if mapping uses short name but claim key is URI
                if (claimValue == null && shortName.equals(remoteClaimUri)) {
                    // Try all claim keys that look like URIs and end with this short name
                    for (String claimKey : claims.keySet()) {
                        if (claimKey.contains("/") && claimKey.endsWith("/" + shortName)) {
                            claimValue = claims.get(claimKey);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Found claim using URI match: " + claimKey + " for short name: " + shortName);
                            }
                            break;
                        }
                    }
                }
                if (claimValue != null) {
                    // Use local claim URI as key for mapped claims
                    userAttributes.put(configuredMapping, claimValue.toString());
                    mappedCount++;
                    mappingDiagnostics.put(localClaimUri, "mapped from " + remoteClaimUri);
                    context.setProperty("DEBUG_MAPPED_LOCAL_CLAIMS",
                            localClaimUri + ":" + claimValue.toString());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Mapped claim: " + remoteClaimUri + " -> " + localClaimUri
                                + " = " + claimValue);
                    }
                } else {
                    mappingDiagnostics.put(localClaimUri,
                            "NOT FOUND - looked for: " + remoteClaimUri + " or " + shortName);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Failed to map claim: " + remoteClaimUri + " (short: " + shortName
                                + "). Available claims: " + claims.keySet());
                    }
                }
            }
            
            // Store mapping diagnostics.
            context.setProperty("DEBUG_CLAIM_MAPPING_DIAGNOSTIC", 
                    "Mapped " + mappedCount + " out of " + configuredMappings.length
                    + " configured mappings. Details: " + mappingDiagnostics);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Claim mapping complete: " + mappedCount
                        + " claims mapped out of " + configuredMappings.length);
            }
            
            // Step status reporting: claim mapping completed
            context.setProperty("DEBUG_STEP_CLAIM_MAPPING_COMPLETED", System.currentTimeMillis());
        } catch (Exception e) {
            LOG.error("Error mapping claims to attributes dynamically: " + e.getMessage(), e);
            context.setProperty("DEBUG_CLAIM_MAPPING_ERROR", e.getMessage());
        }
    }

    private Map<String, Object> parseJsonToClaims(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return new HashMap<>();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = mapper.readValue(json, Map.class);
            return claims != null ? claims : new HashMap<>();
        } catch (Exception e) {
            LOG.error("Error parsing JSON to claims: " + e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * Formats mapped claims with standard claim URIs using ClaimMetadataManagementService.
     * Converts raw claim keys to standard WSO2 claim URIs for better readability.
     * Maps short claim names to standard WSO2 claim URIs based on the system configuration.
     *
     * @param mappedClaimsObj Object containing mapped claims (typically a Map).
     * @param context AuthenticationContext to retrieve tenant information.
     * @return Map with standard claim URIs as keys and claim values as values.
     */
    private Map<String, String> formatMappedClaimsWithStandardURIs(Object mappedClaimsObj,
            AuthenticationContext context) {
        Map<String, String> formattedClaims = new HashMap<>();

        if (mappedClaimsObj == null || !(mappedClaimsObj instanceof Map)) {
            return formattedClaims;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> mappedClaims = (Map<String, String>) mappedClaimsObj;

        // Get tenant domain from context.
        String tenantDomain = (String) context.getProperty("DEBUG_TENANT_DOMAIN");
        if (tenantDomain == null) {
            tenantDomain = "carbon.super";
        }

        // Define standard claim URIs mapping using claim management service.
        Map<String, String> claimUriMapping = createClaimUriMapping(tenantDomain);

        for (Map.Entry<String, String> entry : mappedClaims.entrySet()) {
            String claimKey = entry.getKey();
            String claimValue = entry.getValue();

            // Get the standard claim URI or use the original key if not found.
            String standardClaimUri = claimUriMapping.getOrDefault(claimKey, claimKey);

            // Use the standard claim URI as key.
            formattedClaims.put(standardClaimUri, claimValue);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Formatted claim mapping: " + standardClaimUri + " = " + claimValue);
            }
        }

        return formattedClaims;
    }

    /**
     * Creates a mapping between common claim short names and standard WSO2 claim URIs.
     * Uses ClaimMetadataManagementService to retrieve local claims from the system.
     *
     * @param tenantDomain The tenant domain to retrieve claims for.
     * @return Map of claim short names to standard claim URIs.
     */
    private Map<String, String> createClaimUriMapping(String tenantDomain) {
        Map<String, String> mapping = new HashMap<>();

        try {
            // Try to get ClaimMetadataManagementService from OSGi.
            DebugFrameworkServiceDataHolder dataHolder = DebugFrameworkServiceDataHolder.getInstance();
            ClaimMetadataManagementService claimMetadataService = 
                    dataHolder.getClaimMetadataManagementService();

            if (claimMetadataService != null && tenantDomain != null) {
                try {
                    // Get all local claims from the claim management service.
                    List<LocalClaim> localClaims = claimMetadataService.getLocalClaims(tenantDomain);
                    
                    if (localClaims != null) {
                        for (LocalClaim claim : localClaims) {
                            if (claim != null) {
                                String claimUri = claim.getClaimURI();
                                if (claimUri != null) {
                                    // Extract short name from URI
                                    // (e.g., "userid" from "http://wso2.org/claims/userid").
                                    String shortName = claimUri.contains("/") 
                                            ? claimUri.substring(claimUri.lastIndexOf("/") + 1) 
                                            : claimUri;
                                    
                                    // Map the short name to the full claim URI.
                                    mapping.put(shortName, claimUri);
                                    
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Added claim mapping: " + shortName + " -> " + claimUri);
                                    }
                                }
                            }
                        }
                    }
                    
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Successfully loaded " + mapping.size()
                                + " local claim mappings from ClaimMetadataManagementService");
                    }
                    
                    return mapping;
                } catch (ClaimMetadataException e) {
                    LOG.warn("Error retrieving claims from ClaimMetadataManagementService: "
                            + e.getMessage(), e);
                    // Fall back to default mappings.
                }
            }
        } catch (Exception e) {
            LOG.warn("Error accessing ClaimMetadataManagementService: " + e.getMessage(), e);
            // Fall back to default mappings.
        }

        // Fallback to default standard claim URI mappings.
        if (LOG.isDebugEnabled()) {
            LOG.debug("Using fallback default claim URI mappings");
        }
        
        // Build fallback mappings from FrameworkConstants and common OIDC claims.
        mapping.put("sub", FrameworkConstants.USER_ID_CLAIM);
        mapping.put("email", FrameworkConstants.EMAIL_ADDRESS_CLAIM);
        mapping.put("username", FrameworkConstants.USERNAME_CLAIM);
        
        // Map common OIDC short names to their full URIs where possible.
        mapping.put("name", getStandardClaimUri("givenname", FrameworkConstants.USER_ID_CLAIM));
        mapping.put("given_name", getStandardClaimUri("givenname", FrameworkConstants.USER_ID_CLAIM));
        mapping.put("family_name", getStandardClaimUri("lastname", FrameworkConstants.USER_ID_CLAIM));
        mapping.put("phone_number", getStandardClaimUri("phonenumber", FrameworkConstants.USER_ID_CLAIM));
        mapping.put("picture", getStandardClaimUri("profilePicture", FrameworkConstants.USER_ID_CLAIM));
        
        // OAuth/OIDC standard claims.
        mapping.put("aud", getStandardClaimUri("audience", FrameworkConstants.USER_ID_CLAIM));
        mapping.put("iss", getStandardClaimUri("issuer", FrameworkConstants.USER_ID_CLAIM));
        mapping.put("exp", getStandardClaimUri("expirationTime", FrameworkConstants.USER_ID_CLAIM));
        mapping.put("iat", getStandardClaimUri("issuedAtTime", FrameworkConstants.USER_ID_CLAIM));
        mapping.put("azp", getStandardClaimUri("authorizedParty", FrameworkConstants.USER_ID_CLAIM));
        mapping.put("at_hash", getStandardClaimUri("accessTokenHash", FrameworkConstants.USER_ID_CLAIM));
        mapping.put("email_verified", getStandardClaimUri("emailVerified", FrameworkConstants.USER_ID_CLAIM));
        
        mapping.put("groups", FrameworkConstants.GROUPS_CLAIM);
        mapping.put("roles", FrameworkConstants.ROLES_CLAIM);

        return mapping;
    }

    /**
     * Gets the standard claim URI for a short claim name.
     * Uses the base URI pattern from FrameworkConstants.
     *
     * @param claimShortName The short name of the claim (e.g., "givenname").
     * @param baseClaimUri A reference claim URI to extract the base pattern from.
     * @return The full claim URI.
     */
    private String getStandardClaimUri(String claimShortName, String baseClaimUri) {
        if (baseClaimUri != null && baseClaimUri.contains("/")) {
            String baseUri = baseClaimUri.substring(0, baseClaimUri.lastIndexOf("/") + 1);
            return baseUri + claimShortName;
        }
        // Fallback to explicit URI if pattern extraction fails.
        return "http://wso2.org/claims/" + claimShortName;
    }

    /**
     * Caches debug response result in a JSON format for later retrieval.
     *
     * @param debugResult Map containing debug response data.
     * @param state The state parameter for session identification.
     */
    private void cacheDebugResult(Map<String, Object> debugResult, String state) {
        try {
            String resultJson = mapper.writeValueAsString(debugResult);
            DebugResultCache.add(state, resultJson);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug result cached for state: " + state);
            }
        } catch (Exception cacheEx) {
            LOG.error("Failed to cache debug result", cacheEx);
        }
    }

    /**
     * Builds and caches token exchange error response.
     *
     * @param tokens TokenResponse with error information.
     * @param state The state parameter for session identification.
     * @param context AuthenticationContext.
     */
    private void buildAndCacheTokenExchangeErrorResponse(TokenResponse tokens, String state,
            AuthenticationContext context) {
        Map<String, Object> debugResult = new HashMap<>();
        
        String errorMsg = (String) context.getProperty("DEBUG_AUTH_ERROR");
        String errorDetails = tokens != null ? tokens.getErrorDetails() : "";
        
        debugResult.put("success", "false");
        debugResult.put("error", errorMsg);
        debugResult.put("sessionId", state);
        debugResult.put("idpName", context.getProperty("DEBUG_IDP_NAME"));
        debugResult.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
        debugResult.put("timestamp", System.currentTimeMillis());
        
        // Step statuses.
        debugResult.put("step_connection_status", "failed");
        debugResult.put("step_authentication_status", "not_started");
        debugResult.put("step_claim_extraction_status", "not_started");
        debugResult.put("step_claim_mapping_status", "not_started");
        
        // Error details.
        Map<String, Object> connectionError = new HashMap<>();
        connectionError.put("error", errorMsg);
        if (tokens != null && tokens.hasError()) {
            connectionError.put("errorCode", tokens.getErrorCode());
            connectionError.put("errorDescription", tokens.getErrorDescription());
            if (errorDetails != null && !errorDetails.isEmpty()) {
                connectionError.put("details", errorDetails);
            }
        }
        debugResult.put("step_connection_error", connectionError);
        
        debugResult.put("externalRedirectUrl", context.getProperty("DEBUG_EXTERNAL_REDIRECT_URL"));
        debugResult.put("callbackUrl", context.getProperty("DEBUG_CALLBACK_URL_USED"));
        
        cacheDebugResult(debugResult, state);
    }

    /**
     * Redirects to the debug success page after processing.
     *
     * @param response HttpServletResponse for sending the redirect.
     * @param state The state parameter for session identification.
     * @param idpId The IdP resource ID.
     * @throws IOException If response fails.
     */
    private void redirectToDebugSuccess(HttpServletResponse response, String state, String idpId)
            throws IOException {
        if (!response.isCommitted()) {
            response.sendRedirect("/authenticationendpoint/debugSuccess.jsp?state=" + state + "&idpId=" + idpId);
        }
    }

}



