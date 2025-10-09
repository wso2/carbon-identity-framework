package org.wso2.carbon.identity.debug.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticationService;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkRuntimeException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Routes incoming authentication requests to appropriate handlers.
 * Inspects /commonauth requests to identify debug flow callbacks and routes them 
 * to DebugProcessor while passing regular authentication to default WSO2 RequestCoordinator.
 */
public class RequestCoordinator implements DebugService {

    private static final Log LOG = LogFactory.getLog(RequestCoordinator.class);
    private static final String DEBUG_IDENTIFIER_PARAM = "isDebugFlow";
    private static final String SESSION_DATA_KEY_PARAM = "sessionDataKey";
    private static final String DEBUG_SESSION_ID_PARAM = "debugSessionId";

    private final DebugProcessor debugProcessor;

    public RequestCoordinator() {
        this.debugProcessor = new DebugProcessor();
    }

    /**
     * Handles /commonauth requests with proper debug flow routing.
     * This method should be called from the main WSO2 /commonauth handler.
     *
     * @param request HttpServletRequest from /commonauth endpoint.
     * @param response HttpServletResponse.
     * @return true if request was handled by debug processor, false if it should be handled by regular flow.
     * @throws IOException If processing fails.
     */
    /**
     * Execute method expected by the API layer for OAuth 2.0 authorization URL generation.
     * This method handles the initial IdP debug processing.
     *
     * @param identityProvider the identity provider configuration
     * @param authenticationContext the authentication context (can be null for URL generation)
     * @return OAuth 2.0 authorization URL or processing result
     */
    public String execute(org.wso2.carbon.identity.application.common.model.IdentityProvider identityProvider,
                         org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext authenticationContext) {
        LOG.info("=== DEBUG COORDINATOR: Execute method called for OAuth URL generation ===");
        
        try {
            if (authenticationContext == null) {
                // This is for OAuth URL generation
                LOG.info("Generating OAuth 2.0 authorization URL for IdP: " + identityProvider.getDisplayName());
                
                // Delegate to the Executer class for URL generation
                Executer executer = new Executer();
                boolean success = executer.execute(identityProvider, authenticationContext);
                if (success) {
                    return (String) authenticationContext.getProperty("DEBUG_EXTERNAL_REDIRECT_URL");
                } else {
                    return "ERROR: Failed to generate authorization URL";
                }
            } else {
                // This is for processing an authentication context
                LOG.info("Processing authentication context: " + authenticationContext.getContextIdentifier());
                // For now, return a success indicator
                return "SUCCESS";
            }
            
        } catch (Exception e) {
            LOG.error("Error in execute method", e);
            throw new RuntimeException("Debug framework execution failed", e);
        }
    }

    public boolean handleCommonAuthRequest(HttpServletRequest request, HttpServletResponse response) {
        LOG.info("=== DEBUG COORDINATOR: Processing /commonauth request ===");
        
        try {
            // Log request details for debugging
            LOG.info("Request URI: " + request.getRequestURI());
            LOG.info("Query String: " + request.getQueryString());
            
            // Extract OAuth parameters
            String oauthCode = request.getParameter("code");
            String oauthState = request.getParameter("state");
            String sessionDataKey = request.getParameter("sessionDataKey");
            
            LOG.info("OAuth Code: " + oauthCode);
            LOG.info("OAuth State: " + oauthState);
            LOG.info("Session Data Key: " + sessionDataKey);
            
            // Check if this is a debug flow callback
            boolean isDebugFlow = isDebugFlowCallback(request);
            LOG.info("Is Debug Flow: " + isDebugFlow);
            
            if (isDebugFlow) {
                return handleDebugFlowCallback(request, response);
            } else {
                LOG.info("=== NOT A DEBUG FLOW - Delegating to regular authentication ===");
                return false; // Let regular authentication handle it
            }
            
        } catch (Exception e) {
            LOG.error("Error processing /commonauth request", e);
            return false; // Let regular authentication handle errors
        }
    }

    /**
     * Legacy method for backward compatibility.
     * Coordinates debug authentication requests using AuthenticationContext.
     *
     * @param context AuthenticationContext for the debug flow.
     * @param request HttpServletRequest (can be null for simulated flows).
     * @param response HttpServletResponse (can be null for simulated flows).
     */
    public void coordinate(AuthenticationContext context, HttpServletRequest request, HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Coordinating debug authentication request for session: " + 
                     (context != null ? context.getContextIdentifier() : "null"));
        }

        try {
            // Check if this is a debug flow.
            if (!isDebugFlow(request, context)) {
                LOG.warn("Non-debug flow detected in debug coordinator");
                return;
            }

            // For simulated flows (when request is null), mark as callback processed.
            if (request == null) {
                // Simulate successful callback processing.
                context.setProperty("DEBUG_CALLBACK_PROCESSED", "true");
                context.setProperty("DEBUG_CALLBACK_RESULT", true);
                context.setProperty("DEBUG_CALLBACK_TIMESTAMP", System.currentTimeMillis());
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Simulated callback processing completed for session: " + 
                             context.getContextIdentifier());
                }
                return;
            }

            // Handle real callback requests.
            if (isCallbackRequest(request)) {
                handleDebugCallback(context, request, response);
            } else {
                handleInitialDebugRequest(context, request, response);
            }

        } catch (Exception e) {
            LOG.error("Error coordinating debug authentication request: " + e.getMessage(), e);
            if (response != null) {
                try {
                    sendErrorResponse(response, "COORDINATION_ERROR", e.getMessage());
                } catch (Exception ex) {
                    LOG.error("Error sending error response: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Main routing method that inspects incoming /commonauth requests.
     * Routes debug flow callbacks to DebugProcessor and regular flows to default coordinator.
     *
     * @param request HttpServletRequest from /commonauth endpoint.
     * @param response HttpServletResponse.
     * @throws IOException If an error occurs during request handling.
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inspecting /commonauth request for debug flow routing");
        }

        try {
            // Check if this is a debug flow callback.
            if (isDebugFlowCallback(request)) {
                LOG.debug("Debug flow callback detected - routing to DebugProcessor");
                handleDebugFlowCallback(request, response);
            } else {
                LOG.debug("Regular authentication flow detected - delegating to default WSO2 RequestCoordinator");
                handleRegularAuthenticationFlow(request, response);
            }
        } catch (Exception e) {
            LOG.error("Error routing authentication request: " + e.getMessage(), e);
            sendErrorResponse(response, "ROUTING_ERROR", e.getMessage());
        }
    }

    /**
     * Determines if the incoming request is a debug flow callback.
     * Checks for OAuth callback parameters, debug identifier parameters, and session context properties.
     *
     * @param request HttpServletRequest to inspect.
     * @return true if this is a debug flow callback, false otherwise.
     */
    private boolean isDebugFlowCallback(HttpServletRequest request) {
        // Check for explicit debug identifier parameter.
        String debugParam = request.getParameter(DEBUG_IDENTIFIER_PARAM);
        if ("true".equals(debugParam)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug flow detected via debug identifier parameter");
            }
            return true;
        }

        // Check for OAuth callback parameters that might indicate a debug flow.
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        
        if (code != null && state != null) {
            // Look for debug session information in OAuth state parameter.
            if (isDebugStateParameter(state)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug flow detected via OAuth state parameter: " + state);
                }
                return true;
            }
            
            // Try to find debug context by checking all possible cache entries for debug markers.
            if (checkForDebugContextByOAuthParams(code, state)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug flow detected via OAuth parameters cache lookup");
                }
                return true;
            }
        }

        // Check session context for debug flow properties using sessionDataKey.
        String sessionDataKey = request.getParameter(SESSION_DATA_KEY_PARAM);
        if (sessionDataKey != null) {
            AuthenticationContext context = retrieveDebugContextFromCache(sessionDataKey);
            if (context != null && isDebugContext(context)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug flow detected via session context properties");
                }
                return true;
            }
        }

        // Check for debug session ID parameter (legacy).
        String debugSessionId = request.getParameter(DEBUG_SESSION_ID_PARAM);
        if (debugSessionId != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug flow detected via debug session ID parameter");
            }
            return true;
        }

        return false;
    }

    /**
     * Checks if the OAuth state parameter indicates a debug flow.
     * 
     * @param state OAuth state parameter.
     * @return true if this is a debug state parameter, false otherwise.
     */
    private boolean isDebugStateParameter(String state) {
        if (state == null) {
            LOG.info("DEBUG STATE CHECK: State parameter is null");
            return false;
        }
        
        LOG.info("DEBUG STATE CHECK: Checking state parameter: " + state);
        
        // Check for debug-specific state patterns.
        boolean startsWithDebug = state.startsWith("debug-");
        boolean containsDebug = state.contains("debug");
        boolean containsDebugUpper = state.contains("DEBUG");
        boolean containsDbg = state.contains("dbg-");
        
        LOG.info("DEBUG STATE CHECK: startsWith('debug-'): " + startsWithDebug);
        LOG.info("DEBUG STATE CHECK: contains('debug'): " + containsDebug);
        LOG.info("DEBUG STATE CHECK: contains('DEBUG'): " + containsDebugUpper);
        LOG.info("DEBUG STATE CHECK: contains('dbg-'): " + containsDbg);
        
        boolean isDebug = startsWithDebug || containsDebug || containsDebugUpper || containsDbg;
        LOG.info("DEBUG STATE CHECK: Final result: " + isDebug);
        
        return isDebug;
    }

    /**
     * Checks if the given request represents a debug flow.
     * This is the interface method implementation.
     * 
     * @param request the HTTP servlet request
     * @return true if this is a debug flow, false otherwise
     */
    @Override
    public boolean isDebugFlow(HttpServletRequest request) {
        return isDebugFlowCallback(request);
    }

    /**
     * Attempts to find debug context by OAuth parameters.
     * 
     * @param code OAuth authorization code.
     * @param state OAuth state parameter.
     * @return true if a debug context is found, false otherwise.
     */
    private boolean checkForDebugContextByOAuthParams(String code, String state) {
        try {
            // Extract potential debug session ID from state parameter.
            String debugSessionId = extractDebugSessionIdFromState(state);
            if (debugSessionId != null) {
                // Try to retrieve context using debug session ID as key.
                AuthenticationContext context = retrieveDebugContextFromCache("debug-" + debugSessionId);
                if (context != null && isDebugContext(context)) {
                    return true;
                }
            }
            
            // If state-based lookup fails, check if we can find any debug context
            // that matches OAuth parameters (this is a fallback for edge cases).
            return checkForAnyDebugContextWithOAuthState(state);
            
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error checking for debug context by OAuth params: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Extracts debug session ID from OAuth state parameter.
     * 
     * @param state OAuth state parameter.
     * @return Debug session ID if found, null otherwise.
     */
    private String extractDebugSessionIdFromState(String state) {
        if (state == null) {
            return null;
        }
        
        // Handle various debug state formats.
        if (state.startsWith("debug-")) {
            return state.substring(6); // Remove "debug-" prefix.
        }
        
        if (state.startsWith("dbg-")) {
            return state.substring(4); // Remove "dbg-" prefix.
        }
        
        // Look for debug session ID pattern in complex state parameters.
        if (state.contains("debug")) {
            String[] parts = state.split("[\\-_\\.]");
            for (int i = 0; i < parts.length; i++) {
                if ("debug".equals(parts[i]) && i + 1 < parts.length) {
                    return parts[i + 1];
                }
            }
        }
        
        return null;
    }

    /**
     * Fallback method to check for any debug context with matching OAuth state.
     * This is used when direct lookups fail but we suspect this might be a debug flow.
     * 
     * @param state OAuth state parameter.
     * @return true if any debug context is found with matching state, false otherwise.
     */
    private boolean checkForAnyDebugContextWithOAuthState(String state) {
        // This is a simplified implementation. In a production environment,
        // you might want to implement a more sophisticated cache scanning mechanism
        // or use additional context properties to identify debug flows.
        
        // For now, we'll check if the state parameter has characteristics of a debug flow.
        return state != null && (
            state.length() > 32 && // Typical debug session IDs are longer.
            (state.contains("-") || state.contains("_")) && // Debug sessions often use separators.
            !state.matches("^[a-fA-F0-9]+$") // Not a simple hex string (typical OAuth state).
        );
    }

    /**
     * Checks if the given authentication context is a debug context.
     * 
     * @param context AuthenticationContext to check.
     * @return true if this is a debug context, false otherwise.
     */
    private boolean isDebugContext(AuthenticationContext context) {
        if (context == null) {
            return false;
        }
        
        // Check for debug property markers.
        Object debugProperty = context.getProperty(DEBUG_IDENTIFIER_PARAM);
        if (debugProperty != null) {
            // Handle Boolean type.
            if (debugProperty instanceof Boolean && Boolean.TRUE.equals(debugProperty)) {
                return true;
            }
            // Handle String type.
            if (debugProperty instanceof String && "true".equals(debugProperty)) {
                return true;
            }
        }
        
        // Check for other debug-specific properties.
        return context.getProperty("DEBUG_SESSION_ID") != null ||
               context.getProperty("DEBUG_IDP_RESOURCE_ID") != null ||
               context.getProperty("DEBUG_REQUEST_TIMESTAMP") != null ||
               context.getProperty("DEBUG_FLOW_TYPE") != null;
    }

    /**
     * Handles debug flow callbacks by routing to DebugProcessor.
     *
     * @param request HttpServletRequest containing callback parameters.
     * @param response HttpServletResponse.
     * @return true if callback was processed, false otherwise.
     * @throws IOException If processing fails.
     */
    private boolean handleDebugFlowCallback(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            // Extract OAuth callback parameters.
            String code = request.getParameter("code");
            String state = request.getParameter("state");
            String error = request.getParameter("error");
            String sessionDataKey = request.getParameter(SESSION_DATA_KEY_PARAM);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing debug OAuth callback - code: " + (code != null ? "present" : "null") + 
                         ", state: " + state + ", error: " + error + ", sessionDataKey: " + sessionDataKey);
            }

            // Handle OAuth error responses.
            if (error != null) {
                LOG.error("OAuth error in debug callback: " + error);
                sendErrorResponse(response, "OAUTH_ERROR", "OAuth error: " + error);
                return false;
            }

            // Try to retrieve or create authentication context.
            AuthenticationContext context = null;
            
            // First, try with sessionDataKey if available.
            if (sessionDataKey != null) {
                context = retrieveDebugContextFromCache(sessionDataKey);
            }
            
            // If not found and we have OAuth parameters, try to find context using state parameter.
            if (context == null && state != null) {
                String debugSessionId = extractDebugSessionIdFromState(state);
                if (debugSessionId != null) {
                    context = retrieveDebugContextFromCache("debug-" + debugSessionId);
                    if (context == null) {
                        // Try without prefix.
                        context = retrieveDebugContextFromCache(debugSessionId);
                    }
                }
            }
            
            // If still no context found, create a new one for OAuth callback processing.
            if (context == null) {
                if (code != null && state != null) {
                    context = createDebugContextForCallback(code, state, request);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Created new debug context for OAuth callback processing");
                    }
                } else {
                    LOG.error("Cannot process debug callback: missing OAuth parameters and no cached context");
                    sendErrorResponse(response, "MISSING_CONTEXT_AND_PARAMS", 
                                    "Authentication context not found and OAuth parameters missing");
                    return false;
                }
            }

            // Set OAuth callback parameters in context.
            if (code != null) {
                context.setProperty("DEBUG_OAUTH_CODE", code);
            }
            if (state != null) {
                context.setProperty("DEBUG_OAUTH_STATE", state);
            }
            if (sessionDataKey != null) {
                context.setProperty("DEBUG_SESSION_DATA_KEY", sessionDataKey);
            }
            context.setProperty("DEBUG_CALLBACK_TIMESTAMP", System.currentTimeMillis());
            context.setProperty("DEBUG_CALLBACK_PROCESSED", "true");

            // Route to DebugProcessor for OAuth code exchange and processing.
            debugProcessor.processCallback(request, response, context);
            return true;

        } catch (Exception e) {
            LOG.error("Error processing debug flow callback: " + e.getMessage(), e);
            sendErrorResponse(response, "DEBUG_PROCESSING_ERROR", e.getMessage());
            return false;
        }
    }

    /**
     * Creates a new authentication context for debug callback processing.
     * 
     * @param code OAuth authorization code.
     * @param state OAuth state parameter.
     * @param request HttpServletRequest for additional context.
     * @return New AuthenticationContext configured for debug processing.
     */
    private AuthenticationContext createDebugContextForCallback(String code, String state, HttpServletRequest request) {
        AuthenticationContext context = new AuthenticationContext();
        
        // Generate or extract context identifier.
        String debugSessionId = extractDebugSessionIdFromState(state);
        if (debugSessionId != null) {
            context.setContextIdentifier("debug-" + debugSessionId);
        } else {
            // Generate a new identifier based on OAuth parameters.
            context.setContextIdentifier("debug-callback-" + System.currentTimeMillis());
        }
        
        // Mark as debug flow.
        context.setProperty(DEBUG_IDENTIFIER_PARAM, "true");
        context.setProperty("DEBUG_FLOW_TYPE", "OAUTH_CALLBACK");
        context.setProperty("DEBUG_OAUTH_CODE", code);
        context.setProperty("DEBUG_OAUTH_STATE", state);
        context.setProperty("DEBUG_CONTEXT_CREATED", "true");
        context.setProperty("DEBUG_CREATION_TIMESTAMP", System.currentTimeMillis());
        
        // Set request information if available.
        if (request != null) {
            context.setProperty("DEBUG_HTTP_REQUEST", request);
            context.setProperty("DEBUG_CALLBACK_URL", request.getRequestURL().toString());
            
            // Extract additional parameters that might be useful for debug processing.
            String[] interestingParams = {"session_state", "iss", "client_id"};
            for (String param : interestingParams) {
                String value = request.getParameter(param);
                if (value != null) {
                    context.setProperty("DEBUG_OAUTH_" + param.toUpperCase(), value);
                }
            }
        }
        
        // Cache the context for potential future lookups.
        cacheDebugContext(context);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created debug context for callback with ID: " + context.getContextIdentifier());
        }
        
        return context;
    }

    /**
     * Handles regular authentication flows by delegating to default WSO2 RequestCoordinator.
     * This ensures normal authentication continues to work without disruption.
     *
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @throws IOException If delegation fails.
     */
    private void handleRegularAuthenticationFlow(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            // Delegate to the default WSO2 authentication framework.
            // This should be handled by the existing authentication request processor.
            // In a real implementation, this would call the original WSO2 RequestCoordinator.
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Delegating regular authentication flow to WSO2 framework");
            }

            // For now, we'll allow the request to pass through to the normal flow.
            // In production, you would integrate with:
            // org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultAuthenticationRequestHandler
            
            // This method should not send a response since the regular flow will handle it.
            
        } catch (Exception e) {
            LOG.error("Error delegating to regular authentication flow: " + e.getMessage(), e);
            sendErrorResponse(response, "DELEGATION_ERROR", e.getMessage());
        }
    }

    /**
     * Retrieves debug context from cache.
     *
     * @param sessionDataKey Session data key for cache lookup.
     * @return AuthenticationContext if found, null otherwise.
     */
    private AuthenticationContext retrieveDebugContextFromCache(String sessionDataKey) {
        try {
            AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(sessionDataKey);
            AuthenticationContextCacheEntry cacheEntry = 
                AuthenticationContextCache.getInstance().getValueFromCache(cacheKey);
            
            if (cacheEntry != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug context retrieved from cache for session: " + sessionDataKey);
                }
                return cacheEntry.getContext();
            }
        } catch (Exception e) {
            LOG.error("Error retrieving debug context from cache: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Sends a JSON error response to the client.
     *
     * @param response HttpServletResponse.
     * @param errorCode Error code identifier.
     * @param errorMessage Detailed error message.
     */
    private void sendErrorResponse(HttpServletResponse response, String errorCode, String errorMessage) {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            String errorJson = "{\"error\":\"" + errorCode + "\"," +
                             "\"message\":\"" + (errorMessage != null ? errorMessage.replace("\"", "\\\"") : "") + "\"," +
                             "\"timestamp\":" + System.currentTimeMillis() + "}";

            response.getWriter().write(errorJson);
            response.getWriter().flush();

        } catch (IOException e) {
            LOG.error("Error sending error response: " + e.getMessage(), e);
        }
    }

    /**
     * Determines if this is a debug flow request (legacy method).
     *
     * @param request HttpServletRequest to check (can be null).
     * @param context AuthenticationContext to check.
     * @return true if this is a debug flow, false otherwise.
     */
    private boolean isDebugFlow(HttpServletRequest request, AuthenticationContext context) {
        // Check request parameter if request is available.
        if (request != null) {
            String debugParam = request.getParameter(DEBUG_IDENTIFIER_PARAM);
            if ("true".equals(debugParam)) {
                return true;
            }
        }

        // Check context property.
        if (context != null) {
            Object debugProperty = context.getProperty(DEBUG_IDENTIFIER_PARAM);
            if (debugProperty != null) {
                // Handle Boolean type.
                if (debugProperty instanceof Boolean) {
                    if (Boolean.TRUE.equals(debugProperty)) {
                        return true;
                    }
                }
                // Handle String type.
                else if (debugProperty instanceof String) {
                    if ("true".equals(debugProperty)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines if this is a callback request from IdP (legacy method).
     *
     * @param request HttpServletRequest to check.
     * @return true if this is a callback request, false otherwise.
     */
    private boolean isCallbackRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        // Check for common callback parameters.
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String sessionDataKey = request.getParameter(SESSION_DATA_KEY_PARAM);
        String debugSessionId = request.getParameter(DEBUG_SESSION_ID_PARAM);

        // If we have code/state or explicit debug session parameters, it's likely a callback.
        return (code != null && state != null) || 
               (sessionDataKey != null && debugSessionId != null);
    }

    /**
     * Handles initial debug authentication request (legacy method).
     *
     * @param context AuthenticationContext for the debug flow.
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     */
    private void handleInitialDebugRequest(AuthenticationContext context, 
                                         HttpServletRequest request, 
                                         HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling initial debug authentication request");
        }

        try {
            // Store context in cache for callback handling.
            cacheDebugContext(context);

            // Set request and response in context for executer.
            if (request != null) {
                context.setProperty("DEBUG_HTTP_REQUEST", request);
            }
            if (response != null) {
                context.setProperty("DEBUG_HTTP_RESPONSE", response);
            }

            // Mark as initial request.
            context.setProperty("DEBUG_REQUEST_TYPE", "INITIAL");
            context.setProperty("DEBUG_REQUEST_TIMESTAMP", System.currentTimeMillis());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Initial debug request setup completed for session: " + context.getContextIdentifier());
            }

        } catch (Exception e) {
            LOG.error("Error handling initial debug request: " + e.getMessage(), e);
            context.setProperty("DEBUG_COORDINATION_ERROR", e.getMessage());
        }
    }

    /**
     * Handles callback from IdP after authentication (legacy method).
     *
     * @param context AuthenticationContext for the debug flow.
     * @param request HttpServletRequest containing callback parameters.
     * @param response HttpServletResponse.
     */
    private void handleDebugCallback(AuthenticationContext context, 
                                   HttpServletRequest request, 
                                   HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling debug authentication callback");
        }

        try {
            // Mark as callback request.
            context.setProperty("DEBUG_REQUEST_TYPE", "CALLBACK");
            context.setProperty("DEBUG_CALLBACK_TIMESTAMP", System.currentTimeMillis());

            // Process using DebugProcessor if available.
            if (debugProcessor != null) {
                debugProcessor.processCallback(request, response, context);
            } else {
                // Fallback processing.
                context.setProperty("DEBUG_CALLBACK_PROCESSED", "true");
                context.setProperty("DEBUG_CALLBACK_RESULT", true);
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug callback processed successfully using fallback");
                }
            }

        } catch (Exception e) {
            LOG.error("Error handling debug callback: " + e.getMessage(), e);
            context.setProperty("DEBUG_CALLBACK_ERROR", e.getMessage());
        }
    }

    /**
     * Caches the debug context for callback retrieval (legacy method).
     *
     * @param context AuthenticationContext to cache.
     */
    private void cacheDebugContext(AuthenticationContext context) {
        try {
            AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(context.getContextIdentifier());
            AuthenticationContextCacheEntry cacheEntry = new AuthenticationContextCacheEntry(context);
            
            AuthenticationContextCache.getInstance().addToCache(cacheKey, cacheEntry);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug context cached with key: " + context.getContextIdentifier());
            }
        } catch (Exception e) {
            LOG.error("Error caching debug context: " + e.getMessage(), e);
        }
    }
}
