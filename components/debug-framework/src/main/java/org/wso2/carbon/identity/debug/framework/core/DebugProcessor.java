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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base processor for protocol-specific Authorization Code callback handling.
 * Implements template method pattern for OAuth/OIDC authentication callback processing.
 * 
 * This class provides the generic orchestration flow:
 * 1. validateProtocolCallback() - protocol-specific validation
 * 2. exchangeAuthorizationForTokens() - protocol-specific token exchange
 * 3. extractUserClaims() - protocol-specific claim extraction
 * 4. Build authenticated user and cache result - delegated to subclass
 * 
 * Subclasses MUST implement:
 * - extractUserClaims(): Extract claims from protocol-specific tokens
 * - isAuthorizationCodeAlreadyProcessed(): Detect replay attacks
 * - handleClaimsExtractionResult(): Validate extracted claims
 * - buildAndCacheDebugResult(): Create final authenticated user and cache
 * - buildAndCacheTokenExchangeErrorResponse(): Handle token exchange errors
 * - redirectToDebugSuccess(): Send response to client
 * 
 * Subclasses MAY override:
 * - validateProtocolCallback(): Protocol-specific callback validation (default provided)
 * - exchangeAuthorizationForTokens(): Protocol-specific token exchange (default returns false)
 * 
 */

public abstract class DebugProcessor {

    private static final Log LOG = LogFactory.getLog(DebugProcessor.class);

    /**
     * Processes the OAuth 2.0 Authorization Code callback from external Resource.
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
        String resourceId = "";
        
        try {
            IdentityProvider resource = (IdentityProvider) context.getProperty(DebugFrameworkConstants.RESOURCE_CONFIG);
            resourceId = resource != null ? resource.getResourceId() : "";
            
            // Validate OAuth callback parameters (protocol-specific).
            if (!validateProtocolCallback(request, context, response, state, resourceId)) {
                return;
            }
            
            // Check for duplicate authorization code (subclass specific).
            if (isAuthorizationCodeAlreadyProcessed(authorizationCode, request, context, response, state, resourceId)) {
                return;
            }
            
            // Exchange authorization code for tokens (protocol-specific).
            if (!exchangeAuthorizationForTokens(request, context, response, state, resourceId)) {
                // Token exchange failed - error details already cached by subclass.
                // Redirect to success page to display the cached error result.
                redirectToDebugSuccess(response, state, resourceId);
                return;
            }
            
            // Extract user claims from tokens (protocol-specific, e.g., parse ID token).
            Map<String, Object> claims = extractUserClaims(context);
            if (!handleClaimsExtractionResult(claims, context, response, state, resourceId)) {
                return;
            }
            
            // Create authenticated user and build final response (subclass specific).
            buildAndCacheDebugResult(context, state);
            
            redirectToDebugSuccess(response, state, resourceId);
            
        } catch (Exception e) {
            LOG.error("Unexpected error processing OAuth 2.0 callback.", e);
            context.setProperty(DebugFrameworkConstants.DEBUG_AUTH_ERROR, "Unexpected error: " + e.getMessage());
            context.setProperty(DebugFrameworkConstants.DEBUG_AUTH_SUCCESS, DebugFrameworkConstants.FALSE);
            if (e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("InvocationTargetException cause:", e.getCause());
                }
            }
            redirectToDebugSuccess(response, state, resourceId);
        }
    }

    /**
     * Validates OAuth/OIDC callback parameters for security and completeness.
     * Default implementation performs basic validation; subclasses should add protocol-specific checks.
     *
     * @param request HttpServletRequest containing callback parameters.
     * @param context AuthenticationContext.
     * @param response HttpServletResponse for error responses.
     * @param state State parameter for CSRF protection.
     * @param resourceId Resource ID.
     * @return false if validation fails, true if validation passes.
     * @throws IOException If response cannot be sent.
     */
    protected boolean validateProtocolCallback(HttpServletRequest request, AuthenticationContext context,
                                             HttpServletResponse response, String state, String resourceId) 
            throws IOException {
        // Default implementation: validate basic OAuth parameters.
        String code = request.getParameter("code");
        String error = request.getParameter("error");
        String errorDescription = request.getParameter("error_description");

        // Handle OAuth error responses.
        if (error != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authorization error from Resource: " + error + " - " + errorDescription);
            }
            buildAndCacheTokenExchangeErrorResponse(error, errorDescription, "", state, context);
            return false;
        }

        // Validate authorization code.
        if (code == null || code.trim().isEmpty()) {
            LOG.error("Authorization code missing in callback");
            buildAndCacheTokenExchangeErrorResponse("NO_CODE", "Authorization code not received", "", state, context);
            return false;
        }

        // Validate state parameter.
        if (state == null || state.trim().isEmpty()) {
            LOG.error("State parameter missing in callback");
            buildAndCacheTokenExchangeErrorResponse("NO_STATE", "State parameter missing", "", state, context);
            return false;
        }

        // Validate state matches stored value.
        String storedState = (String) context.getProperty("DEBUG_STATE");
        if (storedState != null && !state.equals(storedState)) {
            LOG.error("State parameter mismatch - CSRF attack detected");
            buildAndCacheTokenExchangeErrorResponse("STATE_MISMATCH", "State validation failed", "", state, context);
            return false;
        }

        return true;
    }

    /**
     * Exchanges authorization code for tokens.
     * Subclasses must override this method with protocol-specific token exchange logic.
     *
     * @param request HttpServletRequest containing authorization code.
     * @param context AuthenticationContext.
     * @param response HttpServletResponse for error responses.
     * @param state State parameter for session tracking.
     * @param resourceId Resource ID.
     * @return true if token exchange succeeds, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected boolean exchangeAuthorizationForTokens(HttpServletRequest request, AuthenticationContext context,
                                                   HttpServletResponse response, String state, String resourceId) 
            throws IOException {
        LOG.error("exchangeAuthorizationForTokens not implemented in subclass");
        return false;
    }

    /**
     * Extracts user claims from protocol-specific tokens.
     * Subclasses MUST implement this method with protocol-specific token parsing logic.
     *
     * @param context AuthenticationContext containing tokens.
     * @return Map of extracted claims, or empty map if no claims found, or null if extraction fails.
     */
    protected abstract Map<String, Object> extractUserClaims(AuthenticationContext context);

    /**
     * Checks if authorization code was already processed (replay attack prevention).
     * Subclasses must implement this with appropriate session state tracking.
     *
     * @param authorizationCode The authorization code from callback.
     * @param request HttpServletRequest.
     * @param context AuthenticationContext.
     * @param response HttpServletResponse.
     * @param state State parameter for session tracking.
     * @param resourceId Resource ID.
     * @return true if duplicate code detected, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean isAuthorizationCodeAlreadyProcessed(String authorizationCode, HttpServletRequest request,
                                                       AuthenticationContext context, HttpServletResponse response,
                                                       String state, String resourceId) throws IOException;

    /**
     * Handles claim extraction result and validates successful extraction.
     * Subclasses must implement this with protocol-specific error handling.
     *
     * @param claims Map of extracted claims.
     * @param context AuthenticationContext.
     * @param response HttpServletResponse.
     * @param state State parameter.
     * @param resourceId Resource ID.
     * @return true if claims extraction succeeded, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean handleClaimsExtractionResult(Map<String, Object> claims, AuthenticationContext context,
                                               HttpServletResponse response, String state, 
                                               String resourceId) throws IOException;

    /**
     * Builds and caches the final debug result after successful authentication.
     * Subclasses must implement this to create and cache appropriate debug result.
     *
     * @param context AuthenticationContext containing all debug information.
     * @param state State parameter for session identification.
     */
    protected abstract void buildAndCacheDebugResult(AuthenticationContext context, String state);

    /**
     * Builds and caches token exchange error response with detailed error information.
     * Protected method for subclasses to call with error details.
     *
     * @param errorCode The error code from token exchange failure.
     * @param errorDescription The error description/message.
     * @param errorDetails Additional error details or stack trace.
     * @param state The state parameter for session identification.
     * @param context AuthenticationContext.
     */
    protected abstract void buildAndCacheTokenExchangeErrorResponse(String errorCode, String errorDescription,
            String errorDetails, String state, AuthenticationContext context);

    /**
     * Redirects to the debug success page after processing.
     * Subclasses must implement this to send appropriate response to client.
     *
     * @param response HttpServletResponse for sending the redirect.
     * @param state The state parameter for session identification.
     * @param resourceId resource ID.
     * @throws IOException If response fails.
     */
    protected abstract void redirectToDebugSuccess(HttpServletResponse 
    response, String state, String resourceId) throws IOException;
}
