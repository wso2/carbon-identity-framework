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

package org.wso2.carbon.identity.debug.framework.core;

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
import org.wso2.carbon.identity.debug.framework.core.cache.DebugResultCache;
import org.wso2.carbon.identity.debug.framework.internal.DebugFrameworkServiceDataHolder;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * Abstract base processor for protocol-specific OAuth/OIDC Authorization Code callback handling.
 * Processes OAuth 2.0 Authorization Code callbacks from external IdPs.
 * Handles token validation, claim extraction, and creates authenticated user objects.
 * 
 * Subclasses must implement:
 * - extractUserClaims(): Protocol-specific token claim extraction
 * 
 * Subclasses may override:
 * - validateProtocolCallback(): Protocol-specific callback validation
 * - exchangeAuthorizationForTokens(): Protocol-specific token exchange
 */
public abstract class DebugProcessor {

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
     * Template method that orchestrates: validation -> token exchange -> claim extraction -> response.
     * Protocol-specific implementations are delegated to subclass methods.
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
        String idpId = "";
        
        try {
            IdentityProvider idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            idpId = idp != null ? idp.getResourceId() : "";
            
            // Validate OAuth callback parameters (protocol-specific).
            if (!validateProtocolCallback(request, context, response, state, idpId)) {
                return;
            }
            
            // Check for duplicate authorization code.
            if (isAuthorizationCodeAlreadyProcessed(authorizationCode, request, context, response, state, idpId)) {
                return;
            }
            
            // Exchange authorization code for tokens (protocol-specific).
            if (!exchangeAuthorizationForTokens(request, context, response, state, idpId)) {
                // Token exchange failed - error details already cached by subclass (e.g., OAuth2DebugProcessor).
                // Just redirect to success page to display the cached error result.
                redirectToDebugSuccess(response, state, idpId);
                return;
            }
            
            // Extract user claims from tokens (protocol-specific, e.g., parse ID token).
            Map<String, Object> claims = extractUserClaims(context);
            if (!handleClaimsExtractionResult(claims, context, response, state, idpId)) {
                return;
            }
            
            // Create authenticated user and build final response (generic).
            AuthenticatedUser authenticatedUser = createAuthenticatedUser(claims, context);
            buildAndCacheDebugResult(authenticatedUser, context, state);
            
            redirectToDebugSuccess(response, state, idpId);
            
        } catch (Exception e) {
            LOG.error("Unexpected error processing OAuth 2.0 callback.", e);
            context.setProperty("DEBUG_AUTH_ERROR", "Unexpected error: " + e.getMessage());
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            if (e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("InvocationTargetException cause:", e.getCause());
                }
            }
            redirectToDebugSuccess(response, state, idpId);
        }
    }

    /**
     * Validates OAuth/OIDC callback parameters for security and completeness.
     * Protocol-specific implementations should override this method.
     * Default implementation performs basic validation; subclasses should add protocol-specific checks.
     *
     * @param request HttpServletRequest containing callback parameters.
     * @param context AuthenticationContext.
     * @param response HttpServletResponse for error responses.
     * @param state State parameter for CSRF protection.
     * @param idpId IdP resource ID.
     * @return false if validation fails and response is sent, true if validation passes.
     * @throws IOException If response cannot be sent.
     */
    protected boolean validateProtocolCallback(HttpServletRequest request, AuthenticationContext context,
                                             HttpServletResponse response, String state, String idpId) 
            throws IOException {
        // Default implementation: validate basic OAuth parameters.
        String code = request.getParameter("code");
        String error = request.getParameter("error");
        String errorDescription = request.getParameter("error_description");

        // Handle OAuth error responses.
        if (error != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authorization error from IdP: " + error + " - " + errorDescription);
            }
            context.setProperty("DEBUG_AUTH_ERROR", error + ": " + errorDescription);
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            redirectToDebugSuccess(response, state, idpId);
            return false;
        }

        // Validate authorization code.
        if (code == null || code.trim().isEmpty()) {
            LOG.error("Authorization code missing in callback");
            context.setProperty("DEBUG_AUTH_ERROR", "Authorization code not received from IdP");
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            redirectToDebugSuccess(response, state, idpId);
            return false;
        }

        // Validate state parameter.
        if (state == null || state.trim().isEmpty()) {
            LOG.error("State parameter missing in callback");
            context.setProperty("DEBUG_AUTH_ERROR", "State parameter missing - possible CSRF attack");
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            redirectToDebugSuccess(response, state, idpId);
            return false;
        }

        // Validate state matches stored value.
        String storedState = (String) context.getProperty("DEBUG_STATE");
        if (storedState != null && !state.equals(storedState)) {
            LOG.error("State parameter mismatch - CSRF attack detected");
            context.setProperty("DEBUG_AUTH_ERROR", "State validation failed - possible CSRF attack");
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            redirectToDebugSuccess(response, state, idpId);
            return false;
        }

        return true;
    }

    /**
     * Exchanges authorization code for tokens.
     * Protocol-specific implementations should override this method.
     * Default implementation returns false; subclasses must implement token exchange logic.
     *
     * @param request HttpServletRequest containing authorization code.
     * @param context AuthenticationContext.
     * @param response HttpServletResponse for error responses.
     * @param state State parameter for session tracking.
     * @param idpId IdP resource ID.
     * @return true if token exchange succeeds, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected boolean exchangeAuthorizationForTokens(HttpServletRequest request, AuthenticationContext context,
                                                   HttpServletResponse response, String state, String idpId) 
            throws IOException {
        LOG.error("exchangeAuthorizationForTokens not implemented in subclass");
        return false;
    }

    /**
     * Extracts user claims from protocol-specific tokens.
     * Subclasses MUST implement this method with protocol-specific token parsing logic.
     * Examples: Parse JWT ID token (OAuth2/OIDC), extract from UserInfo response, etc.
     *
     * @param context AuthenticationContext containing tokens (e.g., DEBUG_ID_TOKEN, DEBUG_ACCESS_TOKEN).
     * @return Map of extracted claims, or empty map if no claims found, or null if extraction fails.
     */
    protected abstract Map<String, Object> extractUserClaims(AuthenticationContext context);

    /**
     * Checks if authorization code was already processed in this session.
     * Used to detect and prevent authorization code replay attacks.
     * Validates authorization code format before storing in session.
     *
     * @param authorizationCode The authorization code from the callback.
     * @param request HttpServletRequest.
     * @param context AuthenticationContext.
     * @param response HttpServletResponse.
     * @param state State parameter for session tracking.
     * @param idpId IdP resource ID.
     * @return true if duplicate code detected, false otherwise.
     */
    private boolean isAuthorizationCodeAlreadyProcessed(String authorizationCode, HttpServletRequest request,
                                                       AuthenticationContext context, HttpServletResponse response,
                                                       String state, String idpId) throws IOException {
        // Validate authorization code format before processing.
        if (authorizationCode == null || authorizationCode.trim().isEmpty() || authorizationCode.length() > 2000) {
            LOG.error("Invalid authorization code: null, empty, or exceeds maximum length");
            context.setProperty("DEBUG_AUTH_ERROR", "Invalid authorization code format");
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            redirectToDebugSuccess(response, state, idpId);
            return true;
        }

        String lastProcessedCode = (String) request.getSession().getAttribute("LAST_AUTH_CODE");
        if (authorizationCode.equals(lastProcessedCode)) {
            context.setProperty("DEBUG_AUTH_ERROR",
                "Authorization code already used in this session. Please retry login.");
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            redirectToDebugSuccess(response, state, idpId);
            return true;
        }
        
        // Store the authorization code in session for replay attack detection.
        request.getSession().setAttribute("LAST_AUTH_CODE", authorizationCode);
        return false;
    }

    /**
     * Handles claim extraction result and validates if claims were successfully extracted.
     * Provides detailed error messages about why claim extraction failed.
     *
     * @return true if claims extraction succeeded, false otherwise.
     */
    private boolean handleClaimsExtractionResult(Map<String, Object> claims, AuthenticationContext context,
                                               HttpServletResponse response, String state, 
                                               String idpId) throws IOException {
        if (claims == null || claims.isEmpty()) {
            String errorMsg = "Failed to extract user claims from tokens";
            String errorDetails = "No claims were found in the ID token or returned from the UserInfo endpoint. " +
                    "Verify that the IdP is configured to return user information and that claim mappings are correct.";
            
            LOG.error(errorMsg + " - " + errorDetails);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Claims extraction failed for IdP. ID token present: " + 
                        (context.getProperty("DEBUG_ID_TOKEN") != null) + 
                        ", AccessToken present: " + (context.getProperty("DEBUG_ACCESS_TOKEN") != null));
            }
            
            context.setProperty("DEBUG_AUTH_ERROR", errorMsg);
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            context.setProperty("step_claim_extraction_status", "failed");
            context.setProperty("step_claim_extraction_error", errorMsg);
            context.setProperty("step_claim_extraction_error_details", errorDetails);
            
            buildAndCacheClaimExtractionErrorResponse(context, state);
            redirectToDebugSuccess(response, state, idpId);
            return false;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Claims extraction successful. Extracted " + claims.size() + 
                    " claims: " + claims.keySet());
        }
        
        context.setProperty("step_claim_extraction_status", "success");
        return true;
    }

    /**
     * Builds and caches a comprehensive debug result when claim extraction fails.
     * Includes detailed error information and troubleshooting hints.
     */
    private void buildAndCacheClaimExtractionErrorResponse(AuthenticationContext context, String state) {
        try {
            Map<String, Object> debugResult = new HashMap<>();
            debugResult.put("success", "false");
            debugResult.put("error", "Failed to extract user claims from tokens");
            debugResult.put("sessionId", state);
            debugResult.put("idpName", context.getProperty("DEBUG_IDP_NAME"));
            debugResult.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
            debugResult.put("timestamp", System.currentTimeMillis());
            
            debugResult.put("step_connection_status", "success");
            debugResult.put("step_authentication_status", "failed");
            debugResult.put("step_claim_extraction_status", "failed");
            debugResult.put("step_claim_mapping_status", "not_started");
            
            String idToken = (String) context.getProperty("DEBUG_ID_TOKEN");
            if (idToken != null) {
                debugResult.put("idToken", idToken);
            }
            
            Map<String, Object> extractionError = new HashMap<>();
            extractionError.put("errorCode", "CLAIMS_EXTRACTION_FAILED");
            extractionError.put("errorDescription", "No claims found in ID token or UserInfo endpoint");
            extractionError.put("troubleshootingHint", "Verify, IdP is configured to return user information. " +
                    "Check if the UserInfo endpoint is properly configured and accessible. " +
                    "Ensure the access token has sufficient scopes to retrieve user information.");
            
            boolean hasIdToken = idToken != null;
            boolean hasAccessToken = context.getProperty("DEBUG_ACCESS_TOKEN") != null;
            extractionError.put("idTokenPresent", hasIdToken);
            extractionError.put("accessTokenPresent", hasAccessToken);
            
            debugResult.put("step_claim_extraction_error", extractionError);
            
            Object mappingDiagnostic = context.getProperty("DEBUG_CLAIM_MAPPING_DIAGNOSTIC");
            if (mappingDiagnostic != null) {
                debugResult.put("claimMappingDiagnostic", mappingDiagnostic);
            }
            
            debugResult.put("externalRedirectUrl", context.getProperty("DEBUG_EXTERNAL_REDIRECT_URL"));
            debugResult.put("callbackUrl", context.getProperty("DEBUG_CALLBACK_URL_USED"));
            
            String resultJson = mapper.writeValueAsString(debugResult);
            DebugResultCache.add(state, resultJson);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Claim extraction error result cached for state: " + state + 
                        ". IdToken present: " + hasIdToken + ", AccessToken present: " + hasAccessToken);
            }
        } catch (Exception cacheEx) {
            LOG.error("Failed to cache claim extraction error result", cacheEx);
        }
    }

    /**
     * Builds and caches the final debug result after successful authentication.
     */
    private void buildAndCacheDebugResult(AuthenticatedUser authenticatedUser, AuthenticationContext context,
                                        String state) {
        if (authenticatedUser != null) {
            buildSuccessDebugResult(authenticatedUser, context, state);
        } else {
            buildAuthenticationFailureDebugResult(context, state);
        }
    }

    /**
     * Builds debug result when user authentication succeeds.
     */
    private void buildSuccessDebugResult(AuthenticatedUser authenticatedUser, AuthenticationContext context,
                                       String state) {
        try {
            Map<String, Object> debugResult = new HashMap<>();
            debugResult.put("success", "true");
            debugResult.put("error", context.getProperty("DEBUG_AUTH_ERROR"));
            debugResult.put("sessionId", state);
            debugResult.put("idpName", context.getProperty("DEBUG_IDP_NAME"));
            debugResult.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
            
            Object executorObj = context.getProperty("DEBUG_EXECUTOR_INSTANCE");
            String executorClass = executorObj != null ? executorObj.getClass().getSimpleName() : "UnknownExecutor";
            debugResult.put("executor", executorClass);
            debugResult.put("timestamp", context.getProperty("DEBUG_AUTH_COMPLETION_TIMESTAMP"));
            
            // Get incoming claims for building mapped claims array
            Map<String, Object> incomingClaimsObj = (Map<String, Object>) context.getProperty("DEBUG_INCOMING_CLAIMS");
            
            Object mappedClaimsObj = context.getProperty("DEBUG_MAPPED_LOCAL_CLAIMS_MAP");
            Map<String, String> formattedMappedClaims = formatMappedClaimsWithStandardURIs(mappedClaimsObj, context);
            
            // Convert mapped claims to array format with mapping status
            List<Map<String, Object>> mappedClaimsArray = buildMappedClaimsArray(
                formattedMappedClaims, incomingClaimsObj, context);
            debugResult.put("mappedClaims", mappedClaimsArray);
            
            // Convert idp configured mappings to array format for frontend
            Object idpConfiguredMappings = context.getProperty("DEBUG_IDP_CONFIGURED_MAPPINGS");
            if (idpConfiguredMappings != null && idpConfiguredMappings instanceof Map) {
                Map<String, Map<String, String>> mappingsMap = (Map<String, Map<String, String>>) idpConfiguredMappings;
                List<Map<String, String>> mappingsArray = new java.util.ArrayList<>();
                for (Map.Entry<String, Map<String, String>> entry : mappingsMap.entrySet()) {
                    mappingsArray.add(entry.getValue());
                }
                debugResult.put("idpConfiguredClaimMappings", mappingsArray);
            }
            
            Object mappingDiagnostic = context.getProperty("DEBUG_CLAIM_MAPPING_DIAGNOSTIC");
            if (mappingDiagnostic != null) {
                debugResult.put("claimMappingDiagnostic", mappingDiagnostic);
            }
            if (context.getProperty("DEBUG_STEP_CLAIM_MAPPING_AUTO_USED") != null) {
                debugResult.put("autoMappingUsed", context.getProperty("DEBUG_STEP_CLAIM_MAPPING_AUTO_USED"));
            }
            
            if (formattedMappedClaims != null && !formattedMappedClaims.isEmpty()) {
                context.setProperty("step_claim_mapping_status", "success");
                debugResult.put("step_claim_mapping_status", "success");
            } else {
                debugResult.put("step_claim_mapping_status", "failed");
            }
            
            debugResult.put("username", authenticatedUser.getUserName());
            debugResult.put("userId", authenticatedUser.getUserId());
            
            String idToken = (String) context.getProperty("DEBUG_ID_TOKEN");
            if (idToken != null) {
                debugResult.put("idToken", idToken);
            }
            
            debugResult.put("externalRedirectUrl", context.getProperty("DEBUG_EXTERNAL_REDIRECT_URL"));
            debugResult.put("callbackUrl", context.getProperty("DEBUG_CALLBACK_URL_USED"));
            debugResult.put("step_connection_status", context.getProperty("step_connection_status"));
            debugResult.put("step_authentication_status", context.getProperty("step_authentication_status"));
            debugResult.put("step_claim_extraction_status", context.getProperty("step_claim_extraction_status"));
            debugResult.put("userAttributes", formattedMappedClaims);
            
            cacheDebugResult(debugResult, state);
        } catch (Exception cacheEx) {
            LOG.error("Failed to cache debug result", cacheEx);
        }
    }

    /**
     * Builds debug result when user authentication fails.
     */
    private void buildAuthenticationFailureDebugResult(AuthenticationContext context, String state) {
        try {
            Map<String, Object> debugResult = new HashMap<>();
            debugResult.put("success", "false");
            debugResult.put("error", context.getProperty("DEBUG_AUTH_ERROR"));
            debugResult.put("sessionId", state);
            debugResult.put("idpName", context.getProperty("DEBUG_IDP_NAME"));
            debugResult.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
            Object executorObj = context.getProperty("DEBUG_EXECUTOR_INSTANCE");
            String executorClass = executorObj != null ? executorObj.getClass().getSimpleName() : "UnknownExecutor";
            debugResult.put("executor", executorClass);
            debugResult.put("timestamp", System.currentTimeMillis());
            
            // Get incoming claims for building mapped claims array
            Map<String, Object> incomingClaimsObj = (Map<String, Object>) context.getProperty("DEBUG_INCOMING_CLAIMS");
            
            Object mappedClaimsObj = context.getProperty("DEBUG_MAPPED_LOCAL_CLAIMS_MAP");
            Map<String, String> formattedMappedClaims = formatMappedClaimsWithStandardURIs(mappedClaimsObj, context);
            
            // Convert mapped claims to array format with mapping status
            List<Map<String, Object>> mappedClaimsArray = buildMappedClaimsArray(
                formattedMappedClaims, incomingClaimsObj, context);
            debugResult.put("mappedClaims", mappedClaimsArray);
            
            // Convert idp configured mappings to array format for frontend
            Object idpConfiguredMappings = context.getProperty("DEBUG_IDP_CONFIGURED_MAPPINGS");
            if (idpConfiguredMappings != null && idpConfiguredMappings instanceof Map) {
                Map<String, Map<String, String>> mappingsMap = (Map<String, Map<String, String>>) idpConfiguredMappings;
                List<Map<String, String>> mappingsArray = new java.util.ArrayList<>();
                for (Map.Entry<String, Map<String, String>> entry : mappingsMap.entrySet()) {
                    mappingsArray.add(entry.getValue());
                }
                debugResult.put("idpConfiguredClaimMappings", mappingsArray);
            }
            
            Object mappingDiagnostic = context.getProperty("DEBUG_CLAIM_MAPPING_DIAGNOSTIC");
            if (mappingDiagnostic != null) {
                debugResult.put("claimMappingDiagnostic", mappingDiagnostic);
            }
            if (context.getProperty("DEBUG_STEP_CLAIM_MAPPING_AUTO_USED") != null) {
                debugResult.put("autoMappingUsed", context.getProperty("DEBUG_STEP_CLAIM_MAPPING_AUTO_USED"));
            }
            
            String idToken = (String) context.getProperty("DEBUG_ID_TOKEN");
            if (idToken != null) {
                debugResult.put("idToken", idToken);
            }
            
            debugResult.put("step_connection_status", context.getProperty("step_connection_status"));
            debugResult.put("step_authentication_status", context.getProperty("step_authentication_status"));
            debugResult.put("step_claim_extraction_status", context.getProperty("step_claim_extraction_status"));
            debugResult.put("step_claim_mapping_status", 
                context.getProperty("step_claim_mapping_status") != null
                ? context.getProperty("step_claim_mapping_status") 
                : "failed");
            
            debugResult.put("externalRedirectUrl", context.getProperty("DEBUG_EXTERNAL_REDIRECT_URL"));
            debugResult.put("callbackUrl", context.getProperty("DEBUG_CALLBACK_URL_USED"));
            
            cacheDebugResult(debugResult, state);
        } catch (Exception cacheEx) {
            LOG.error("Failed to cache debug result", cacheEx);
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
            int notFoundCount = 0;
            Map<String, String> mappingDiagnostics = new HashMap<>();
            Map<String, String> unmappedClaims = new HashMap<>();

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
                    mappingDiagnostics.put(localClaimUri, "MAPPED: " + remoteClaimUri + " = " + claimValue);
                    context.setProperty("DEBUG_MAPPED_LOCAL_CLAIMS",
                            localClaimUri + ":" + claimValue.toString());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Mapped claim: " + remoteClaimUri + " -> " + localClaimUri
                                + " = " + claimValue);
                    }
                } else {
                    notFoundCount++;
                    String lookupDetails = "looked for '" + remoteClaimUri + "'";
                    if (!shortName.equals(remoteClaimUri)) {
                        lookupDetails += " or short name '" + shortName + "'";
                    }
                    mappingDiagnostics.put(localClaimUri, "NOT FOUND - " + lookupDetails);
                    unmappedClaims.put(localClaimUri, remoteClaimUri);
                    
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Failed to map claim: " + remoteClaimUri + " (short: " + shortName
                                + "). Available claims: " + claims.keySet());
                    }
                }
            }
            
            // Build comprehensive mapping diagnostic.
            StringBuilder diagnosticMsg = new StringBuilder();
            diagnosticMsg.append("Claim Mapping Report: ").append(mappedCount).append(" of ").
            append(configuredMappings.length)
                    .append(" mappings successful (").append(notFoundCount).append(" not found). ");
            diagnosticMsg.append("Incoming claims received: ").append(claims.keySet()).append(". ");
            
            if (!unmappedClaims.isEmpty()) {
                diagnosticMsg.append("Missing expected claims: ");
                for (Map.Entry<String, String> entry : unmappedClaims.entrySet()) {
                    diagnosticMsg.append("[").append(entry.getKey()).append(" <- ").
                    append(entry.getValue()).append("] ");
                }
            }
            
            // Store mapping diagnostics.
            context.setProperty("DEBUG_CLAIM_MAPPING_DIAGNOSTIC", diagnosticMsg.toString());
            context.setProperty("DEBUG_CLAIM_MAPPING_DETAILS", mappingDiagnostics);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Claim mapping complete: " + mappedCount
                        + " claims mapped out of " + configuredMappings.length + ". " + diagnosticMsg.toString());
            }
            
            // Step status reporting: claim mapping completed
            context.setProperty("DEBUG_STEP_CLAIM_MAPPING_COMPLETED", System.currentTimeMillis());
        } catch (Exception e) {
            LOG.info("Error mapping claims to attributes dynamically: " + e.getMessage(), e);
            context.setProperty("DEBUG_CLAIM_MAPPING_ERROR", e.getMessage());
        }
    }

    /**
     * Formats mapped claims with standard claim URIs using ClaimMetadataManagementService.
     * Converts raw claim keys to standard WSO2 claim URIs for better readability.
     * Safely validates type before unchecked cast to prevent ClassCastException.
     *
     * @param mappedClaimsObj Object containing mapped claims (typically a Map).
     * @param context AuthenticationContext to retrieve tenant information.
     * @return Map with standard claim URIs as keys and claim values as values.
     */
    private Map<String, String> formatMappedClaimsWithStandardURIs(Object mappedClaimsObj,
            AuthenticationContext context) {
        Map<String, String> formattedClaims = new HashMap<>();

        // Safely validate type before unchecked cast.
        if (mappedClaimsObj == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Mapped claims object is null");
            }
            return formattedClaims;
        }

        if (!(mappedClaimsObj instanceof Map)) {
            LOG.warn("Mapped claims object is not a Map. Actual type: " + mappedClaimsObj.getClass().getName());
            return formattedClaims;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> mappedClaims = (Map<String, String>) mappedClaimsObj;

        // Get tenant domain from context.
        String tenantDomain = (String) context.getProperty("DEBUG_TENANT_DOMAIN");
        if (tenantDomain == null || tenantDomain.isEmpty()) {
            tenantDomain = "carbon.super";
        }

        // Define standard claim URIs mapping using claim management service.
        Map<String, String> claimUriMapping = createClaimUriMapping(tenantDomain);

        for (Map.Entry<String, String> entry : mappedClaims.entrySet()) {
            String claimKey = entry.getKey();
            String claimValue = entry.getValue();

            if (claimKey == null || claimKey.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping null or empty claim key");
                }
                continue;
            }

            // Get the standard claim URI or use the original key if not found.
            String standardClaimUri = claimUriMapping.getOrDefault(claimKey, claimKey);

            // Use the standard claim URI as key.
            formattedClaims.put(standardClaimUri, claimValue);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Formatted claim mapping: " + standardClaimUri);
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
     * Converts incoming claims map to array format for frontend display.
     * Transforms both object and array formats into array of {claim, value} objects.
     *
     * @param claimsData The incoming claims map or array.
     * @return List of claim objects with claim name and value properties.
     */
    /**
     * Builds mapped claims array with mapping status for frontend display.
     * Merges mapped claims with incoming claims to show both successful and failed mappings.
     *
     * @param mappedClaims Map of successfully mapped local claim URIs to values.
     * @param incomingClaims Map of incoming claims from IdP.
     * @param context AuthenticationContext containing IdP configuration.
     * @return List of claim mappings with status showing which claims were successfully mapped.
     */
    private List<Map<String, Object>> buildMappedClaimsArray(
            Map<String, String> mappedClaims,
            Map<String, Object> incomingClaims,
            AuthenticationContext context) {
        
        List<Map<String, Object>> mappingsArray = new java.util.ArrayList<>();
        
        if (mappedClaims == null) {
            mappedClaims = new HashMap<>();
        }
        
        // Get IdP configured mappings
        Object idpConfiguredMappings = context.getProperty("DEBUG_IDP_CONFIGURED_MAPPINGS");
        Map<String, Map<String, String>> configuredMappings = new HashMap<>();
        
        if (idpConfiguredMappings instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> mappingsMap = (Map<String, Map<String, String>>) idpConfiguredMappings;
            configuredMappings = mappingsMap;
        }
        
        // Create reverse mapping: local claim URI -> remote claim name
        Map<String, String> localToRemoteMapping = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : configuredMappings.entrySet()) {
            Map<String, String> mapping = entry.getValue();
            if (mapping != null && mapping.containsKey("local") && mapping.containsKey("remote")) {
                localToRemoteMapping.put(mapping.get("local"), mapping.get("remote"));
            }
        }
        
        // Track which incoming claims have been mapped
        java.util.Set<String> mappedIncomingClaims = new java.util.HashSet<>();
        
        // First, add successfully mapped claims
        for (Map.Entry<String, String> entry : mappedClaims.entrySet()) {
            String localClaimUri = entry.getKey();
            String remoteClaimName = localToRemoteMapping.getOrDefault(localClaimUri, localClaimUri);
            
            Map<String, Object> mapping = new HashMap<>();
            mapping.put("idpClaim", remoteClaimName); // remote claim name from IdP
            mapping.put("isClaim", localClaimUri); // local claim URI
            mapping.put("value", entry.getValue());
            mapping.put("status", "Successful");
            mapping.put("localClaimUri", localClaimUri);
            mappingsArray.add(mapping);
            
            mappedIncomingClaims.add(remoteClaimName);
        }
        
        // Then, add unmapped incoming claims
        if (incomingClaims != null && !incomingClaims.isEmpty()) {
            for (Map.Entry<String, Object> entry : incomingClaims.entrySet()) {
                String incomingClaimKey = entry.getKey();
                
                // Check if this incoming claim was mapped
                if (!mappedIncomingClaims.contains(incomingClaimKey)) {
                    Map<String, Object> unmappedClaim = new HashMap<>();
                    unmappedClaim.put("idpClaim", incomingClaimKey);
                    unmappedClaim.put("isClaim", "-");
                    unmappedClaim.put("value", entry.getValue());
                    unmappedClaim.put("status", "Not mapped");
                    mappingsArray.add(unmappedClaim);
                }
            }
        }
        
        return mappingsArray;
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
     * Builds and caches token exchange error response with detailed error information.
     * Protected method for subclasses to call with error details.
     * Includes error code, description, and troubleshooting hints.
     *
     * @param errorCode The error code from token exchange failure.
     * @param errorDescription The error description/message.
     * @param errorDetails Additional error details or stack trace.
     * @param state The state parameter for session identification.
     * @param context AuthenticationContext.
     */
    protected void buildAndCacheTokenExchangeErrorResponse(String errorCode, String errorDescription,
            String errorDetails, String state, AuthenticationContext context) {
        Map<String, Object> debugResult = new HashMap<>();
        
        String errorMsg = errorCode + ": " + errorDescription;
        
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
        
        // Detailed error information.
        Map<String, Object> connectionError = new HashMap<>();
        connectionError.put("errorCode", errorCode);
        connectionError.put("errorDescription", errorDescription);
        connectionError.put("errorMessage", errorMsg);
        
        if (errorDetails != null && !errorDetails.isEmpty()) {
            connectionError.put("errorDetails", errorDetails);
        }
        
        // Add troubleshooting information based on error code.
        String troubleshooting = getTroubleshootingHint(errorCode, context);
        if (troubleshooting != null) {
            connectionError.put("troubleshootingHint", troubleshooting);
        }
        
        debugResult.put("step_connection_error", connectionError);
        
        debugResult.put("externalRedirectUrl", context.getProperty("DEBUG_EXTERNAL_REDIRECT_URL"));
        debugResult.put("callbackUrl", context.getProperty("DEBUG_CALLBACK_URL_USED"));
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Token exchange error result being cached - Code: " + errorCode + 
                    ", Description: " + errorDescription);
        }
        
        cacheDebugResult(debugResult, state);
    }

    /**
     * Provides troubleshooting hints based on the error code.
     * Helps users identify and fix common configuration issues.
     *
     * @param errorCode The error code from token exchange failure.
     * @param context AuthenticationContext with configuration information.
     * @return Troubleshooting hint message.
     */
    private String getTroubleshootingHint(String errorCode, AuthenticationContext context) {
        switch (errorCode) {
            case "INVALID_CLIENT":
                return "Verify Client ID and Client Secret are correct in the IdP authenticator configuration. " +
                        "Check the IdP's application/client settings to ensure credentials match.";
            case "INVALID_GRANT":
                return "The authorization code may have expired (usually after 5-10 minutes) or was already used. " +
                        "Start the authentication process again to get a new authorization code.";
            case "INVALID_REQUEST":
                return "The token request parameters may be malformed. Verify redirect URI configuration and " +
                        "PKCE parameters if enabled. Check that the token endpoint URL is correct.";
            case "UNAUTHORIZED":
                return "The IdP rejected the request. Verify the client credentials and that the authenticator " +
                        "type matches the IdP's requirements (OAuth 2.0, OIDC, etc.).";
            case "CONFIG_MISSING":
                return "Required OAuth 2.0 configuration is missing. Verify that Client ID, Client Secret, and " +
                        "Token Endpoint URL are all configured in the IdP authenticator settings.";
            case "IDP_CONFIG_MISSING":
                return "Identity Provider configuration not found. Ensure the IdP is properly registered and " +
                        "the authentication flow has access to the IdP configuration.";
            case "AUTHENTICATOR_CONFIG_MISSING":
                return "Authenticator configuration not found for the specified authenticator. Verify that the " +
                        "authenticator is properly configured in the IdP.";
            case "CONNECTION_ERROR":
                return "Cannot connect to the IdP token endpoint. Verify the token endpoint URL is correct, " +
                        "the IdP server is running, and there are no network/firewall issues.";
            case "TIMEOUT_ERROR":
                return "The request to the IdP token endpoint timed out. The IdP server may be slow or unresponsive. " +
                        "Check server logs and network connectivity.";
            case "SSL_CERTIFICATE_ERROR":
                return "SSL certificate validation failed when connecting to the IdP. Verify the IdP's SSL " +
                        "certificate is valid and trusted by the application server.";
            default:
                return "An unexpected error occurred during token exchange. Check the application server logs " +
                        "for detailed error messages.";
        }
    }

    /**
     * Redirects to the debug success page after processing.
     * Properly URL-encodes parameters to prevent XSS and injection attacks.
     *
     * @param response HttpServletResponse for sending the redirect.
     * @param state The state parameter for session identification.
     * @param idpId The IdP resource ID.
     * @throws IOException If response fails.
     */
    private void redirectToDebugSuccess(HttpServletResponse response, String state, String idpId)
            throws IOException {
        if (!response.isCommitted()) {
            String encodedState = encodeForUrl(state);
            String encodedIdpId = encodeForUrl(idpId);
            String redirectUrl = "/authenticationendpoint/debugSuccess.jsp?state=" + encodedState + 
                                 "&idpId=" + encodedIdpId;
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * URL-encodes a parameter for safe use in HTTP redirects.
     * Prevents XSS and injection vulnerabilities.
     *
     * @param param Parameter to encode.
     * @return URL-encoded parameter.
     */
    private String encodeForUrl(String param) {
        if (param == null || param.isEmpty()) {
            return "";
        }
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (Exception e) {
            LOG.warn("Error encoding URL parameter: " + e.getMessage());
            return "";
        }
    }

}
