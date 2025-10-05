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
 * Coordinates debug authentication requests.
 * Handles /commonauth callbacks and routes to processor with debug request identification.
 */
public class RequestCoordinator {

    private static final Log LOG = LogFactory.getLog(RequestCoordinator.class);
    private static final String DEBUG_IDENTIFIER_PARAM = "isDebugFlow";
    private static final String SESSION_DATA_KEY_PARAM = "sessionDataKey";
    private static final String DEBUG_SESSION_ID_PARAM = "debugSessionId";

    private final Executer executer;
    private final Processor processor;

    public RequestCoordinator() {
        this.executer = new Executer();
        this.processor = new Processor();
    }

    /**
     * Handles the debug authentication request and coordinates the flow.
     *
     * @param context AuthenticationContext for the debug flow.
     * @param request HttpServletRequest containing request parameters.
     * @param response HttpServletResponse for sending responses.
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

            // Determine if this is initial request or callback.
            if (isCallbackRequest(request)) {
                handleDebugCallback(context, request, response);
            } else {
                handleInitialDebugRequest(context, request, response);
            }

        } catch (Exception e) {
            LOG.error("Error coordinating debug authentication request: " + e.getMessage(), e);
            handleCoordinationError(context, request, response, e);
        }
    }

    /**
     * Determines if this is a debug flow request.
     *
     * @param request HttpServletRequest to check.
     * @param context AuthenticationContext to check.
     * @return true if this is a debug flow, false otherwise.
     */
    private boolean isDebugFlow(HttpServletRequest request, AuthenticationContext context) {
        // Check request parameter.
        String debugParam = request != null ? request.getParameter(DEBUG_IDENTIFIER_PARAM) : null;
        if ("true".equals(debugParam)) {
            return true;
        }

        // Check context property.
        if (context != null) {
            Boolean isDebug = (Boolean) context.getProperty("IS_DEBUG_FLOW");
            return Boolean.TRUE.equals(isDebug);
        }

        return false;
    }

    /**
     * Determines if this is a callback request from IdP.
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
     * Handles initial debug authentication request.
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
            context.setProperty("DEBUG_HTTP_REQUEST", request);
            context.setProperty("DEBUG_HTTP_RESPONSE", response);

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
     * Handles callback from IdP after authentication.
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
            // Get session data key from request.
            String sessionDataKey = request.getParameter(SESSION_DATA_KEY_PARAM);
            if (sessionDataKey == null) {
                LOG.error("Session data key not found in callback request");
                return;
            }

            // Retrieve context from cache if not provided.
            if (context == null) {
                context = retrieveDebugContextFromCache(sessionDataKey);
                if (context == null) {
                    LOG.error("Debug context not found for session: " + sessionDataKey);
                    return;
                }
            }

            // Mark as callback request.
            context.setProperty("DEBUG_REQUEST_TYPE", "CALLBACK");
            context.setProperty("DEBUG_CALLBACK_TIMESTAMP", System.currentTimeMillis());

            // Process authentication response using executer.
            boolean authResult = executer.processAuthenticationResponse(context, request, response);
            
            // Store callback processing result.
            context.setProperty("DEBUG_CALLBACK_PROCESSED", "true");
            context.setProperty("DEBUG_CALLBACK_RESULT", authResult);

            // Process results using processor.
            Object processedResult = processor.process(context);
            context.setProperty("DEBUG_PROCESSED_RESULT", processedResult);

            // Send response back to client.
            sendDebugResponse(context, request, response, processedResult);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug callback processed successfully. Auth result: " + authResult);
            }

        } catch (Exception e) {
            LOG.error("Error handling debug callback: " + e.getMessage(), e);
            context.setProperty("DEBUG_CALLBACK_ERROR", e.getMessage());
            handleCallbackError(context, request, response, e);
        }
    }

    /**
     * Caches the debug context for callback retrieval.
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
     * Sends debug response back to client.
     *
     * @param context AuthenticationContext.
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @param processedResult Result from processor.
     */
    private void sendDebugResponse(AuthenticationContext context, 
                                 HttpServletRequest request, 
                                 HttpServletResponse response,
                                 Object processedResult) {
        try {
            // Set response content type.
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Create response JSON.
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");
            jsonResponse.append("\"debugSessionId\":\"").append(context.getProperty("DEBUG_SESSION_ID")).append("\",");
            jsonResponse.append("\"status\":\"").append(context.getProperty("DEBUG_CALLBACK_RESULT")).append("\",");
            jsonResponse.append("\"timestamp\":").append(System.currentTimeMillis()).append(",");
            jsonResponse.append("\"result\":").append(processedResult != null ? processedResult.toString() : "null");
            jsonResponse.append("}");

            // Write response.
            response.getWriter().write(jsonResponse.toString());
            response.getWriter().flush();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug response sent successfully");
            }

        } catch (IOException e) {
            LOG.error("Error sending debug response: " + e.getMessage(), e);
        }
    }

    /**
     * Handles coordination errors.
     *
     * @param context AuthenticationContext.
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @param error Exception that occurred.
     */
    private void handleCoordinationError(AuthenticationContext context, 
                                       HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       Exception error) {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            String errorJson = "{\"error\":\"COORDINATION_ERROR\"," +
                             "\"message\":\"" + error.getMessage() + "\"," +
                             "\"timestamp\":" + System.currentTimeMillis() + "}";

            response.getWriter().write(errorJson);
            response.getWriter().flush();

        } catch (IOException e) {
            LOG.error("Error sending coordination error response: " + e.getMessage(), e);
        }
    }

    /**
     * Handles callback errors.
     *
     * @param context AuthenticationContext.
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @param error Exception that occurred.
     */
    private void handleCallbackError(AuthenticationContext context, 
                                   HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   Exception error) {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            String errorJson = "{\"error\":\"CALLBACK_ERROR\"," +
                             "\"message\":\"" + error.getMessage() + "\"," +
                             "\"debugSessionId\":\"" + context.getProperty("DEBUG_SESSION_ID") + "\"," +
                             "\"timestamp\":" + System.currentTimeMillis() + "}";

            response.getWriter().write(errorJson);
            response.getWriter().flush();

        } catch (IOException e) {
            LOG.error("Error sending callback error response: " + e.getMessage(), e);
        }
    }

    /**
     * Handles HTTP requests with just request and response parameters.
     * This method is called by DebugAwareRequestCoordinator.
     *
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @throws IOException If an error occurs during request handling.
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            handleCommonAuthRequest(request, response);
        } catch (Exception e) {
            LOG.error("Error handling debug request: " + e.getMessage(), e);
            throw new IOException("Error handling debug request", e);
        }
    }

    /**
     * Integrates with the authentication framework to handle regular /commonauth requests
     * and identify debug flows.
     *
     * @param request HttpServletRequest from /commonauth.
     * @param response HttpServletResponse.
     */
    public void handleCommonAuthRequest(HttpServletRequest request, HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling /commonauth request - checking for debug flow");
        }

        try {
            // Check if this is a debug flow callback.
            if (isDebugFlow(request, null)) {
                // Extract session data key.
                String sessionDataKey = request.getParameter(SESSION_DATA_KEY_PARAM);
                if (sessionDataKey != null) {
                    // Retrieve debug context.
                    AuthenticationContext context = retrieveDebugContextFromCache(sessionDataKey);
                    if (context != null) {
                        // Handle as debug callback.
                        handleDebugCallback(context, request, response);
                        return;
                    }
                }
            }

            // If not a debug flow, let the normal authentication framework handle it.
            if (LOG.isDebugEnabled()) {
                LOG.debug("Non-debug flow detected, delegating to normal authentication framework");
            }

        } catch (Exception e) {
            LOG.error("Error handling /commonauth request: " + e.getMessage(), e);
            handleCoordinationError(null, request, response, e);
        }
    }

    // Static session management for API layer.
    private static final java.util.concurrent.ConcurrentHashMap<String, AuthenticationContext> sessionStorage = 
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.ConcurrentHashMap<String, java.util.Map<String, Object>> resultStorage = 
            new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Store debug context for API layer.
     *
     * @param sessionId Session ID
     * @param context Authentication context
     */
    public static void storeDebugContext(String sessionId, AuthenticationContext context) {
        sessionStorage.put(sessionId, context);
    }

    /**
     * Get session result for API layer.
     *
     * @param sessionId Session ID
     * @return Session result
     */
    public static java.util.Map<String, Object> getSessionResult(String sessionId) {
        return resultStorage.get(sessionId);
    }

    /**
     * Handle callback for API layer.
     *
     * @param sessionId Session ID
     * @return Callback result
     */
    public static java.util.Map<String, Object> handleCallback(String sessionId) {
        AuthenticationContext context = sessionStorage.get(sessionId);
        if (context == null) {
            return null;
        }

        // Process the callback result.
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("sessionId", sessionId);
        result.put("status", "COMPLETED");
        result.put("timestamp", System.currentTimeMillis());

        // Store result for later retrieval.
        resultStorage.put(sessionId, result);
        
        return result;
    }
}
