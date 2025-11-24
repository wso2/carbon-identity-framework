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

package org.wso2.carbon.identity.debug.framework.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Routes incoming authentication requests to appropriate handlers.
 * Handles two main flows:
 * 1. Generic debug requests (POST /api/server/v1/debug) - routes based on resourceType
 * 2. OAuth callback requests (/commonauth) - routes to protocol-specific DebugProcessor
 */
public class DebugRequestCoordinator implements DebugService {

    private static final Log LOG = LogFactory.getLog(DebugRequestCoordinator.class);

    public DebugRequestCoordinator() {

    }

    /**
     * Handles debug requests for any resource type (IDENTITY_PROVIDER, APPLICATION, CONNECTOR, etc.).
     * Routes to appropriate handler based on resourceType and delegates processing.
     *
     * @param debugRequestContext Map containing:
     *        - resourceId: Resource identifier (required).
     *        - resourceType: Type of resource (e.g., "IDENTITY_PROVIDER", "APPLICATION", "CONNECTOR") (required).
     *        - properties: Optional key-value properties for the debug request (Map<String, String>).
     * @return Map containing debug result data, or null on failure.
     */
    public Map<String, Object> handleResourceDebugRequest(Map<String, Object> debugRequestContext) {

        if (debugRequestContext == null) {
            LOG.error("Debug request context is null");
            return null;
        }

        try {
            // Extract required parameters
            String resourceId = (String) debugRequestContext.get("resourceId");
            String resourceType = (String) debugRequestContext.get("resourceType");
            
            if (resourceId == null || resourceId.trim().isEmpty()) {
                LOG.error("Resource ID is required in debug request context");
                return null;
            }
            
            if (resourceType == null || resourceType.trim().isEmpty()) {
                LOG.error("Resource type is required in debug request context");
                return null;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing debug request for resourceId: " + resourceId + ", resourceType: " + resourceType);
            }

            // Get the appropriate handler for this resource type
            Object handler = DebugProtocolRouter.getDebugResourceHandler(resourceType);
            if (handler == null) {
                LOG.error("No handler available for resource type: " + resourceType);
                return null;
            }

            // Invoke handleDebugRequest on the handler using reflection
            Map<String, Object> result = invokeHandlerMethod(handler, debugRequestContext);
            
            if (result == null) {
                LOG.error("Handler returned null result for resourceType: " + resourceType);
                return null;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully processed debug request for resourceType: " + resourceType);
            }

            return result;

        } catch (Exception e) {
            LOG.error("Error handling debug request: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Invokes handleDebugRequest on a DebugResourceHandler using reflection.
     *
     * @param handler The handler instance.
     * @param debugRequestContext The debug request context.
     * @return Result map from the handler, or null if invocation fails.
     */
    private Map<String, Object> invokeHandlerMethod(Object handler, Map<String, Object> debugRequestContext) {

        if (handler == null) {
            LOG.error("Handler instance is null");
            return null;
        }

        try {
            Class<?> handlerClass = handler.getClass();
            java.lang.reflect.Method method = handlerClass.getMethod("handleDebugRequest", Map.class);
            Object result = method.invoke(handler, debugRequestContext);
            
            if (result == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Handler returned null result");
                }
                return null;
            }

            if (!(result instanceof Map)) {
                LOG.error("Handler returned non-Map result: " + result.getClass().getName());
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = (Map<String, Object>) result;
            return resultMap;

        } catch (NoSuchMethodException e) {
            LOG.error("Handler class does not have handleDebugRequest method: " + e.getMessage(), e);
            return null;
        } catch (IllegalAccessException e) {
            LOG.error("Cannot access handleDebugRequest method: " + e.getMessage(), e);
            return null;
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = "Error invoking handler's handleDebugRequest method";
            if (cause != null) {
                LOG.error(errorMsg + ": " + cause.getMessage(), cause);
            } else {
                LOG.error(errorMsg, e);
            }
            return null;
        } catch (SecurityException e) {
            LOG.error("Security error accessing handler method: " + e.getMessage(), e);
            return null;
        } catch (Exception e) {
            LOG.error("Unexpected error invoking handler method: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Handles /commonauth requests with proper debug flow routing.
     * Checks if request is a debug flow callback and routes to DebugProcessor.
     *
     * @param request HttpServletRequest from /commonauth endpoint.
     * @param response HttpServletResponse.
     * @return true if request was handled as debug flow, false if it should be handled by regular authentication.
     * @throws IOException If processing fails.
     */
    public boolean handleCommonAuthRequest(HttpServletRequest request, HttpServletResponse response) {

        try {
            // Check if this is a debug flow callback
            if (isDebugFlowCallback(request)) {
                return handleDebugFlowCallback(request, response);
            } else {
                return false; // Let regular authentication handle it
            }
            
        } catch (Exception e) {
            LOG.error("Error processing /commonauth request", e);
            return false; // Let regular authentication handle errors
        }
    }

    /**
     * Determines if the incoming request is a debug flow callback.
     * Checks for OAuth callback parameters and debug identifier in state parameter.
     *
     * @param request HttpServletRequest to inspect.
     * @return true if this is a debug flow callback, false otherwise.
     */
    private boolean isDebugFlowCallback(HttpServletRequest request) {

        String state = request.getParameter(DebugFrameworkConstants.OAUTH2_STATE_PARAM);
        if (state == null || !state.startsWith(DebugFrameworkConstants.DEBUG_PREFIX)) {
            return false;
        }

        // A true OAuth callback must have a 'code' or 'error' parameter.
        return request.getParameter(DebugFrameworkConstants.OAUTH2_CODE_PARAM) != null || 
               request.getParameter(DebugFrameworkConstants.OAUTH2_ERROR_PARAM) != null;
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
     * Extracts debug session ID from OAuth state parameter.
     * Assumes state parameter is in format "debug-{sessionId}".
     * 
     * @param state OAuth state parameter.
     * @return Debug session ID if found, null otherwise.
     */
    private String extractDebugSessionIdFromState(String state) {

        if (state != null && state.startsWith(DebugFrameworkConstants.DEBUG_PREFIX)) {
            return state.substring(DebugFrameworkConstants.DEBUG_PREFIX.length());
        }
        return null;
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
            String code = request.getParameter(DebugFrameworkConstants.OAUTH2_CODE_PARAM);
            String state = request.getParameter(DebugFrameworkConstants.OAUTH2_STATE_PARAM);
            String error = request.getParameter(DebugFrameworkConstants.OAUTH2_ERROR_PARAM);
            String sessionDataKey = request.getParameter(DebugFrameworkConstants.SESSION_DATA_KEY_PARAM);

            // Handle OAuth error responses.
            if (error != null) {
                LOG.error("OAuth error in debug callback: " + error);
                if (!response.isCommitted()) {
                    sendErrorResponse(response, "OAUTH_ERROR", "OAuth error: " + error);
                }
                return true;
            }

            // Try to retrieve authentication context from cache.
            AuthenticationContext context = null;

            // First, try with sessionDataKey if available (framework cache).
            if (sessionDataKey != null) {
                context = retrieveDebugContextFromCache(sessionDataKey);
            }

            // If not found, create a new one for OAuth callback processing.
            if (context == null && code != null && state != null) {
                context = createDebugContextForCallback(code, state, request);
            }

            if (context == null) {
                LOG.error("Cannot process debug callback: missing context and/or OAuth parameters");
                if (!response.isCommitted()) {
                    sendErrorResponse(response, "MISSING_CONTEXT", "Authentication context not found");
                }
                return true;
            }

            // Set OAuth callback parameters in context.
            if (code != null) {
                context.setProperty(DebugFrameworkConstants.DEBUG_OAUTH_CODE, code);
            }
            if (state != null) {
                context.setProperty(DebugFrameworkConstants.DEBUG_OAUTH_STATE, state);
            }
            if (sessionDataKey != null) {
                context.setProperty(DebugFrameworkConstants.DEBUG_SESSION_DATA_KEY, sessionDataKey);
            }
            context.setProperty(DebugFrameworkConstants.DEBUG_CALLBACK_TIMESTAMP, System.currentTimeMillis());
            context.setProperty(DebugFrameworkConstants.DEBUG_CALLBACK_PROCESSED, DebugFrameworkConstants.TRUE);

            // Route to protocol-specific DebugProcessor via reflection.
            Object processor = getProtocolSpecificProcessor(request, context);
            if (processor != null && !response.isCommitted()) {
                invokeProcessorCallback(processor, request, response, context);
            }
            return true;

        } catch (Exception e) {
            LOG.error("Error processing debug flow callback", e);
            if (!response.isCommitted()) {
                sendErrorResponse(response, "DEBUG_PROCESSING_ERROR", e.getMessage());
            }
            return true;
        }
    }

    /**
     * Gets the appropriate DebugProcessor implementation for the protocol.
     * Uses DebugProtocolRouter to detect protocol type and select appropriate processor.
     *
     * @param request HttpServletRequest to analyze for protocol detection.
     * @param context AuthenticationContext for additional protocol hints.
     * @return DebugProcessor implementation if found, null otherwise.
     */
    private Object getProtocolSpecificProcessor(HttpServletRequest request, AuthenticationContext context) {

        try {
            String resourceId = null;

            // Try to get resource ID from context properties
            if (context != null) {
                resourceId = (String) context.getProperty("resourceId");
                if (resourceId == null) {
                    resourceId = (String) context.getProperty("resourceName");
                }
            }

            // If still not found and we have request, try to extract from state
            if (resourceId == null && request != null) {
                String state = request.getParameter(DebugFrameworkConstants.OAUTH2_STATE_PARAM);
                if (state != null) {
                    // Note: In a real scenario, you'd retrieve resource info from cache using debugSessionId
                    // For now, we default to OAuth2/OIDC
                    if (LOG.isDebugEnabled()) {
                        String debugSessionId = extractDebugSessionIdFromState(state);
                        if (debugSessionId != null) {
                            LOG.debug("Debug session ID extracted from state: " + debugSessionId);
                        }
                    }
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Routing processor for resource: " + (resourceId != null ? resourceId : "default (OAuth2/OIDC)"));
            }

            Object processor = DebugProtocolRouter.getProcessorForResource(resourceId);
            if (processor == null && LOG.isDebugEnabled()) {
                LOG.debug("Protocol-specific processor not available for resource: " + resourceId);
            }
            return processor;

        } catch (Exception e) {
            LOG.error("Error discovering protocol-specific DebugProcessor: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Invokes the processCallback method on a DebugProcessor instance via reflection.
     *
     * @param processor The processor instance (DebugProcessor or subclass).
     * @param request HttpServletRequest from the callback.
     * @param response HttpServletResponse for the callback.
     * @param context AuthenticationContext for the debug flow.
     * @throws Exception If method invocation fails.
     */
    private void invokeProcessorCallback(Object processor, HttpServletRequest request, 
            HttpServletResponse response, AuthenticationContext context) throws Exception {

        if (processor == null) {
            throw new Exception("DebugProcessor is null");
        }
        
        try {
            Class<?> processorClass = processor.getClass();
            java.lang.reflect.Method processCallbackMethod = processorClass.getMethod(
                "processCallback", 
                HttpServletRequest.class, 
                HttpServletResponse.class, 
                AuthenticationContext.class
            );
            
            processCallbackMethod.invoke(processor, request, response, context);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully invoked processCallback on " + processorClass.getSimpleName());
            }
            
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = "Error in processor callback";
            if (cause != null) {
                errorMsg += " - actual error: " + cause.getMessage();
                LOG.error(errorMsg, cause);
            } else {
                LOG.error(errorMsg, e);
            }
            throw new Exception(errorMsg, cause != null ? cause : e);
        } catch (Exception e) {
            LOG.error("Error invoking processor callback: " + e.getMessage(), e);
            throw e;
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
            context.setContextIdentifier(DebugFrameworkConstants.DEBUG_PREFIX + debugSessionId);
        } else {
            context.setContextIdentifier("debug-callback-" + System.currentTimeMillis());
        }
        
        // Mark as debug flow.
        context.setProperty(DebugFrameworkConstants.DEBUG_IDENTIFIER_PARAM, DebugFrameworkConstants.TRUE);
        context.setProperty("DEBUG_FLOW_TYPE", "OAUTH_CALLBACK");
        context.setProperty(DebugFrameworkConstants.DEBUG_OAUTH_CODE, code);
        context.setProperty(DebugFrameworkConstants.DEBUG_OAUTH_STATE, state);
        context.setProperty("DEBUG_CONTEXT_CREATED", DebugFrameworkConstants.TRUE);
        context.setProperty("DEBUG_CREATION_TIMESTAMP", System.currentTimeMillis());
        
        // Cache the context for future lookups.
        cacheDebugContext(context);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created debug context for callback with ID: " + context.getContextIdentifier());
        }
        
        return context;
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
     * Sends a JSON error response to the client using proper JSON serialization.
     * Prevents JSON injection attacks by using org.json library.
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

            // Build error response using proper JSON escaping with manual but correct implementation.
            // Use StringBuilder for efficiency instead of string concatenation.
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\"error\":");
            appendJsonString(jsonBuilder, errorCode != null ? errorCode : "");
            jsonBuilder.append(",\"message\":");
            appendJsonString(jsonBuilder, errorMessage != null ? errorMessage : "");
            jsonBuilder.append(",\"timestamp\":");
            jsonBuilder.append(System.currentTimeMillis());
            jsonBuilder.append("}");

            response.getWriter().write(jsonBuilder.toString());
            response.getWriter().flush();

        } catch (IOException e) {
            LOG.error("Error sending error response: " + e.getMessage(), e);
        }
    }

    /**
     * Appends a JSON-escaped string to a StringBuilder.
     * Properly escapes all JSON special characters to prevent injection attacks.
     *
     * @param builder StringBuilder to append to.
     * @param value String value to escape and append.
     */
    private void appendJsonString(StringBuilder builder, String value) {
        
        builder.append("\"");
        if (value != null && !value.isEmpty()) {
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                switch (ch) {
                    case '"':
                        builder.append("\\\"");
                        break;
                    case '\\':
                        builder.append("\\\\");
                        break;
                    case '\b':
                        builder.append("\\b");
                        break;
                    case '\f':
                        builder.append("\\f");
                        break;
                    case '\n':
                        builder.append("\\n");
                        break;
                    case '\r':
                        builder.append("\\r");
                        break;
                    case '\t':
                        builder.append("\\t");
                        break;
                    case '/':
                        builder.append("\\/");
                        break;
                    default:
                        if (ch < 32 || (ch >= 127 && ch < 160) || (ch >= 0x2028 && ch <= 0x2029)) {
                            builder.append(String.format("\\u%04x", (int) ch));
                        } else {
                            builder.append(ch);
                        }
                }
            }
        }
        builder.append("\"");
    }
}
