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
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.debug.framework.core.cache.DebugResultCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base class for processing debug callbacks from external IdPs.
 * Provides generic template method for callback processing flow orchestration.
 * Protocol-specific implementations (OAuth2, SAML, etc.) extend this class and
 * implement abstract methods for their respective protocols.
 *
 * Template Method Flow:
 * 1. Extract callback parameters (protocol-agnostic, can be overridden)
 * 2. Validate callback (ABSTRACT - protocol-specific)
 * 3. Exchange authorization for tokens (ABSTRACT - protocol-specific)
 * 4. Extract user claims (ABSTRACT - protocol-specific)
 * 5. Create authenticated user (ABSTRACT - protocol-specific)
 * 6. Build and cache debug result (generic/concrete)
 *
 * Subclasses implement:
 * - validateProtocolCallback(): Validate callback parameters for protocol.
 * - exchangeAuthorizationForTokens(): Exchange code/params for tokens.
 * - extractUserClaimsFromTokens(): Extract claims from tokens.
 * - createAuthenticatedUser(): Create user object from claims.
 */
public abstract class DebugProcessor {

    protected static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Log LOG = LogFactory.getLog(DebugProcessor.class);

    /**
     * Template method: Orchestrates debug callback processing.
     * Final method prevents subclasses from overriding overall flow.
     * Delegates protocol-specific logic to abstract methods.
     *
     * @param request HttpServletRequest with callback parameters.
     * @param response HttpServletResponse for sending results.
     * @param context AuthenticationContext with callback context.
     * @throws IOException If processing fails.
     */
    public final void processCallback(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationContext context) throws IOException {
        String state = extractStateParameter(request);
        String idpId = extractIdpId(context);
        
        try {
            // Step 1: Protocol-specific callback validation.
            if (!validateProtocolCallback(request, context, response, state, idpId)) {
                return;
            }
            
            // Step 2: Protocol-specific token/authorization exchange.
            if (!exchangeAuthorizationForTokens(request, context, response, state, idpId)) {
                return;
            }
            
            // Step 3: Protocol-specific claim extraction.
            Map<String, Object> claims = extractUserClaimsFromTokens(request, context);
            if (!handleClaimsExtractionResult(claims, context, response, state, idpId)) {
                return;
            }
            
            // Step 4: Protocol-specific user creation.
            AuthenticatedUser user = createAuthenticatedUser(claims, context);
            if (user == null) {
                LOG.error("Failed to create authenticated user from claims");
                buildAuthenticationFailureDebugResult(context, state);
                redirectToDebugPage(response, state, idpId, false);
                return;
            }
            
            // Step 5: Generic debug result building and caching.
            buildAndCacheDebugResult(user, context, state);
            redirectToDebugPage(response, state, idpId, true);
            
        } catch (Exception e) {
            LOG.error("Unexpected error processing debug callback: " + e.getMessage(), e);
            context.setProperty("DEBUG_AUTH_ERROR", "Unexpected error: " + e.getMessage());
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            redirectToDebugPage(response, state, idpId, false);
        }
    }

    /**
     * Extracts state parameter from request.
     * Concrete method - can be overridden by subclasses if needed.
     *
     * @param request HttpServletRequest.
     * @return State parameter or empty string.
     */
    protected String extractStateParameter(HttpServletRequest request) {
        String state = request.getParameter("state");
        return state != null ? state : "";
    }

    /**
     * Extracts IdP ID from authentication context.
     * Concrete method - can be overridden by subclasses if needed.
     *
     * @param context AuthenticationContext.
     * @return IdP resource ID or empty string.
     */
    protected String extractIdpId(AuthenticationContext context) {
        try {
            IdentityProvider idp = (IdentityProvider) context.getProperty("IDP_CONFIG");
            return idp != null ? idp.getResourceId() : "";
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error extracting IdP ID: " + e.getMessage());
            }
            return "";
        }
    }

    /**
     * ABSTRACT: Protocol-specific callback validation.
     * Subclasses must implement validation for their protocol.
     *
     * @param request HttpServletRequest.
     * @param context AuthenticationContext.
     * @param response HttpServletResponse.
     * @param state State parameter.
     * @param idpId IdP resource ID.
     * @return true if validation passes, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean validateProtocolCallback(HttpServletRequest request,
            AuthenticationContext context, HttpServletResponse response, String state,
            String idpId) throws IOException;

    /**
     * ABSTRACT: Protocol-specific authorization code exchange for tokens.
     * Subclasses implement code exchange and token validation.
     * Should set DEBUG_* properties in context with token/error details.
     *
     * @param request HttpServletRequest.
     * @param context AuthenticationContext.
     * @param response HttpServletResponse.
     * @param state State parameter.
     * @param idpId IdP resource ID.
     * @return true if exchange succeeds, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean exchangeAuthorizationForTokens(HttpServletRequest request,
            AuthenticationContext context, HttpServletResponse response, String state,
            String idpId) throws IOException;

    /**
     * ABSTRACT: Protocol-specific claim extraction from tokens.
     * Subclasses implement token parsing and UserInfo endpoint calls.
     *
     * @param request HttpServletRequest.
     * @param context AuthenticationContext with tokens from exchangeAuthorizationForTokens().
     * @return Map of user claims or null if extraction fails.
     */
    protected abstract Map<String, Object> extractUserClaimsFromTokens(HttpServletRequest request,
            AuthenticationContext context);

    /**
     * ABSTRACT: Protocol-specific authenticated user creation from claims.
     * Subclasses implement claim mapping and user object creation.
     *
     * @param claims Map of user claims.
     * @param context AuthenticationContext.
     * @return AuthenticatedUser or null if creation fails.
     */
    protected abstract AuthenticatedUser createAuthenticatedUser(Map<String, Object> claims,
            AuthenticationContext context);

    /**
     * Handles claims extraction result validation.
     * Generic logic - same for all protocols.
     *
     * @param claims Extracted claims map.
     * @param context AuthenticationContext.
     * @param response HttpServletResponse.
     * @param state State parameter.
     * @param idpId IdP resource ID.
     * @return true if claims are valid, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected boolean handleClaimsExtractionResult(Map<String, Object> claims,
            AuthenticationContext context, HttpServletResponse response, String state,
            String idpId) throws IOException {
        if (claims == null || claims.isEmpty()) {
            String errorMsg = "Failed to extract user claims from tokens";
            LOG.error(errorMsg);
            context.setProperty("DEBUG_AUTH_ERROR", errorMsg);
            context.setProperty("DEBUG_AUTH_SUCCESS", "false");
            context.setProperty("step_claim_extraction_status", "failed");
            
            buildClaimExtractionErrorDebugResult(context, state);
            redirectToDebugPage(response, state, idpId, false);
            return false;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Claims extraction successful. Extracted " + claims.size() + " claims");
        }
        
        context.setProperty("step_claim_extraction_status", "success");
        context.setProperty("DEBUG_INCOMING_CLAIMS", claims);
        return true;
    }

    /**
     * Builds debug result for successful authentication.
     * Generic logic - same for all protocols.
     *
     * @param user AuthenticatedUser object.
     * @param context AuthenticationContext.
     * @param state State parameter.
     */
    protected void buildAndCacheDebugResult(AuthenticatedUser user, AuthenticationContext context,
                                          String state) {
        try {
            Map<String, Object> debugResult = new HashMap<>();
            debugResult.put("success", "true");
            debugResult.put("sessionId", state);
            debugResult.put("username", user.getUserName());
            debugResult.put("userId", user.getUserId());
            debugResult.put("timestamp", System.currentTimeMillis());
            
            // Add any protocol-specific properties set by subclasses.
            debugResult.put("idpName", context.getProperty("DEBUG_IDP_NAME"));
            debugResult.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
            debugResult.put("incomingClaims", context.getProperty("DEBUG_INCOMING_CLAIMS"));
            debugResult.put("step_connection_status", context.getProperty("step_connection_status"));
            debugResult.put("step_claim_extraction_status", context.getProperty("step_claim_extraction_status"));
            
            String resultJson = MAPPER.writeValueAsString(debugResult);
            DebugResultCache.add(state, resultJson);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug result cached for state: " + state);
            }
        } catch (Exception e) {
            LOG.error("Failed to cache debug result: " + e.getMessage(), e);
        }
    }

    /**
     * Builds debug result for authentication failure.
     * Generic logic - same for all protocols.
     *
     * @param context AuthenticationContext.
     * @param state State parameter.
     */
    protected void buildAuthenticationFailureDebugResult(AuthenticationContext context, String state) {
        try {
            Map<String, Object> debugResult = new HashMap<>();
            debugResult.put("success", "false");
            debugResult.put("error", context.getProperty("DEBUG_AUTH_ERROR"));
            debugResult.put("sessionId", state);
            debugResult.put("timestamp", System.currentTimeMillis());
            
            // Add any protocol-specific properties set by subclasses.
            debugResult.put("idpName", context.getProperty("DEBUG_IDP_NAME"));
            debugResult.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
            debugResult.put("incomingClaims", context.getProperty("DEBUG_INCOMING_CLAIMS"));
            debugResult.put("step_connection_status", context.getProperty("step_connection_status"));
            debugResult.put("step_claim_extraction_status", context.getProperty("step_claim_extraction_status"));
            
            String resultJson = MAPPER.writeValueAsString(debugResult);
            DebugResultCache.add(state, resultJson);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug failure result cached for state: " + state);
            }
        } catch (Exception e) {
            LOG.error("Failed to cache debug failure result: " + e.getMessage(), e);
        }
    }

    /**
     * Builds debug result for claim extraction failure.
     * Generic logic - same for all protocols.
     *
     * @param context AuthenticationContext.
     * @param state State parameter.
     */
    protected void buildClaimExtractionErrorDebugResult(AuthenticationContext context, String state) {
        try {
            Map<String, Object> debugResult = new HashMap<>();
            debugResult.put("success", "false");
            debugResult.put("error", "Failed to extract user claims from tokens");
            debugResult.put("sessionId", state);
            debugResult.put("timestamp", System.currentTimeMillis());
            
            debugResult.put("idpName", context.getProperty("DEBUG_IDP_NAME"));
            debugResult.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
            debugResult.put("step_connection_status", context.getProperty("step_connection_status"));
            debugResult.put("step_claim_extraction_status", "failed");
            
            String resultJson = MAPPER.writeValueAsString(debugResult);
            DebugResultCache.add(state, resultJson);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Claim extraction error result cached for state: " + state);
            }
        } catch (Exception e) {
            LOG.error("Failed to cache claim extraction error result: " + e.getMessage(), e);
        }
    }

    /**
     * Redirects to debug page after processing.
     * Generic method - same for all protocols.
     *
     * @param response HttpServletResponse.
     * @param state State parameter.
     * @param idpId IdP resource ID.
     * @param success true for success page, false for error page.
     */
    protected void redirectToDebugPage(HttpServletResponse response, String state, String idpId,
                                      boolean success) {
        try {
            if (!response.isCommitted()) {
                String redirectUrl = success 
                        ? "/authenticationendpoint/debugSuccess.jsp?state=" + state + "&idpId=" + idpId
                        : "/authenticationendpoint/debugError.jsp?state=" + state + "&idpId=" + idpId;
                response.sendRedirect(redirectUrl);
            }
        } catch (IOException e) {
            LOG.error("Failed to redirect to debug page: " + e.getMessage(), e);
        }
    }
}
