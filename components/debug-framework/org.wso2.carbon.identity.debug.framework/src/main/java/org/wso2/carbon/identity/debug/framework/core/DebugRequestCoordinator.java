/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
import org.json.JSONObject;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.debug.framework.core.extension.DebugExecutor;
import org.wso2.carbon.identity.debug.framework.core.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugResourceType;
import org.wso2.carbon.identity.debug.framework.model.DebugResult;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Routes incoming authentication requests to appropriate handlers.
 * Handles two main flows:
 * 1. Generic debug requests (POST /api/server/v1/debug) - routes based on
 * resourceType
 * 2. OAuth callback requests (/commonauth) - routes to protocol-specific
 * DebugProcessor
 * 
 * This is the main orchestrator for debug request handling using switch-case
 * based routing
 * for type-safe resource and protocol dispatching.
 */
public class DebugRequestCoordinator implements DebugService {

    private static final Log LOG = LogFactory.getLog(DebugRequestCoordinator.class);

    /**
     * Constructs a DebugRequestCoordinator instance.
     * 
     * This constructor is intentionally empty as DebugRequestCoordinator is a
     * stateless
     * service coordinator that delegates all work to static helper methods and
     * external
     * handlers/processors. There is no instance state to initialize.
     * 
     * All handler and processor instances are created dynamically on-demand via
     * reflection
     * based on the resource type and protocol configuration, so no upfront
     * initialization
     * is required.
     */
    public DebugRequestCoordinator() {

        // See class javadoc for details on the routing and delegation design.
    }

    /**
     * Handles the initial debug request (e.g., to generate authorization URL).
     * Routes to the appropriate protocol executor based on the resource ID.
     *
     * @param debugRequestContext Map containing request details (resourceId, etc.).
     * @return Map containing the execution result (e.g., authUrl, state).
     */
    public Map<String, Object> handleInitialDebugRequest(Map<String, Object> debugRequestContext) {

        if (debugRequestContext == null) {
            LOG.error("Debug request context is null");
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", "FAILURE");
            errorResponse.put("message", "Debug request context cannot be null");
            return errorResponse;
        }

        try {
            String resourceId = (String) debugRequestContext.get("resourceId");

            if (LOG.isDebugEnabled()) {
                LOG.debug("Handling initial debug request for resourceId: " + resourceId);
            }

            // Use the Factory Pattern (via Router) to get the correct Executor
            DebugExecutor executor = DebugProtocolRouter.getExecutorForResource(resourceId);

            if (executor == null) {
                LOG.warn("No DebugExecutor found for resourceId: " + resourceId);
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("status", "FAILURE");
                errorResponse.put("message", "No debug executor found for the given resource.");
                return errorResponse;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Delegating to executor: " + executor.getClass().getSimpleName());
            }

            // Execute and convert DebugResult to Map
            DebugResult result = executor.execute(debugRequestContext);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("successful", result.isSuccessful());
            response.put("resultId", result.getResultId());
            response.put("timestamp", result.getTimestamp());
            response.put("status", result.getStatus());
            response.put("errorCode", result.getErrorCode());
            response.put("errorMessage", result.getErrorMessage());
            response.put("resultData", result.getResultData());
            response.put("metadata", result.getMetadata());
            return response;

        } catch (Exception e) {
            LOG.error("Error handling initial debug request: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", "FAILURE");
            errorResponse.put("message", "Server error processing debug request: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Handles debug requests for any resource type (IDENTITY_PROVIDER, APPLICATION,
     * CONNECTOR, etc.).
     * Routes to appropriate handler based on resourceType using switch-case
     * pattern.
     * This is the main orchestration method for generic debug requests.
     *
     * @param debugRequestContext Map containing:
     *                            - resourceId: Resource identifier (required).
     *                            - resourceType: Type of resource (e.g., "idp",
     *                            "fraud_detection") (required).
     *                            - properties: Optional key-value properties for
     *                            the debug request (Map<String, String>).
     * @return Map containing debug result data, or error map on failure.
     */
    public Map<String, Object> handleResourceDebugRequest(Map<String, Object> debugRequestContext) {

        if (debugRequestContext == null) {
            LOG.error("Debug request context is null");
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", "FAILURE");
            errorResponse.put("message", "Debug request context cannot be null");
            return errorResponse;
        }

        try {
            String resourceId = (String) debugRequestContext.get("resourceId");
            String resourceType = (String) debugRequestContext.get("resourceType");

            if (LOG.isDebugEnabled()) {
                LOG.debug("Orchestrating debug request for resourceId: " + resourceId +
                        ", resourceType: " + resourceType);
            }

            // Route by resource type using enum-based switch case
            DebugResourceType type = DebugResourceType.fromString(resourceType);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Resolved resource type to: " + type.name());
            }

            // Get the handler for this resource type
            DebugResourceHandler handler = type.getHandler(resourceId);

            if (handler == null) {
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("status", "FAILURE");
                errorResponse.put("message", "No handler available for resource type: " + resourceType);
                return errorResponse;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Delegating to handler: " + handler.getClass().getSimpleName());
            }

            // Delegate to the resource-specific handler
            return handler.handleDebugRequest(debugRequestContext);

        } catch (Exception e) {
            LOG.error("Error in debug request orchestration: " + e.getMessage(), e);
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("status", "FAILURE");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Handles /commonauth requests with proper debug flow routing.
     * Checks if request is a debug flow callback and routes to DebugProcessor.
     *
     * @param request  HttpServletRequest from /commonauth endpoint.
     * @param response HttpServletResponse.
     * @return true if request was handled as debug flow, false if it should be
     *         handled by regular authentication.
     */
    public boolean handleCommonAuthRequest(HttpServletRequest request, HttpServletResponse response) {

        try {
            // Check if this is a debug flow callback
            if (isDebugFlowCallback(request)) {
                handleDebugFlowCallback(request, response);
                return true;
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
     * Extracts OAuth callback parameters, retrieves or creates authentication
     * context,
     * and routes to the appropriate protocol-specific processor. Handles all error
     * cases by sending appropriate error responses.
     *
     * @param request  HttpServletRequest containing callback parameters.
     * @param response HttpServletResponse.
     */
    private void handleDebugFlowCallback(HttpServletRequest request, HttpServletResponse response) {

        try {
            // Extract OAuth callback parameters.
            String code = request.getParameter(DebugFrameworkConstants.OAUTH2_CODE_PARAM);
            String state = request.getParameter(DebugFrameworkConstants.OAUTH2_STATE_PARAM);
            String error = request.getParameter(DebugFrameworkConstants.OAUTH2_ERROR_PARAM);
            String sessionDataKey = request.getParameter(DebugFrameworkConstants.SESSION_DATA_KEY_PARAM);

            // Handle OAuth error responses.
            if (handleOAuthError(error, response)) {
                return;
            }

            // Retrieve or create authentication context.
            AuthenticationContext context = retrieveOrCreateContext(code, state, sessionDataKey);

            if (context == null) {
                handleMissingContext(response);
                return;
            }

            // Set OAuth callback parameters and route to processor.
            setContextProperties(context, code, state, sessionDataKey);
            Object processor = getProtocolSpecificProcessor(request, context);

            if (processor != null && !response.isCommitted()) {
                invokeProcessorCallback(processor, request, response, context);
            }

        } catch (Exception e) {
            LOG.error("Error processing debug flow callback", e);
            if (!response.isCommitted()) {
                sendErrorResponse(response, "DEBUG_PROCESSING_ERROR", e.getMessage());
            }
        }
    }

    /**
     * Handles OAuth error responses.
     *
     * @param error    OAuth error parameter value.
     * @param response HttpServletResponse for sending error response.
     * @return true if error was present and handled, false otherwise.
     */
    private boolean handleOAuthError(String error, HttpServletResponse response) {

        if (error == null) {
            return false;
        }

        LOG.error("OAuth error in debug callback: " + error);
        if (!response.isCommitted()) {
            sendErrorResponse(response, "OAUTH_ERROR", "OAuth error: " + error);
        }
        return true;
    }

    /**
     * Retrieves context from cache or creates a new one for OAuth callback
     * processing.
     *
     * @param code           OAuth authorization code.
     * @param state          OAuth state parameter.
     * @param sessionDataKey Session data key for cache lookup.
     * @return AuthenticationContext if found or created, null otherwise.
     */
    private AuthenticationContext retrieveOrCreateContext(String code, String state, String sessionDataKey) {

        AuthenticationContext context = null;

        // First, try with sessionDataKey if available (framework cache).
        if (sessionDataKey != null) {
            context = retrieveDebugContextFromCache(sessionDataKey);
        }

        // If not found, create a new one for OAuth callback processing.
        if (context == null && code != null && state != null) {
            context = createDebugContextForCallback(code, state);
        }

        return context;
    }

    /**
     * Handles missing context error response.
     *
     * @param response HttpServletResponse for sending error response.
     */
    private void handleMissingContext(HttpServletResponse response) {

        LOG.error("Cannot process debug callback: missing context and/or OAuth parameters");
        if (!response.isCommitted()) {
            sendErrorResponse(response, "MISSING_CONTEXT", "Authentication context not found");
        }
    }

    /**
     * Sets OAuth callback parameters in the authentication context.
     *
     * @param context        AuthenticationContext to update.
     * @param code           OAuth authorization code.
     * @param state          OAuth state parameter.
     * @param sessionDataKey Session data key.
     */
    private void setContextProperties(AuthenticationContext context, String code, String state, String sessionDataKey) {

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
    }

    /**
     * Gets the appropriate DebugProcessor implementation for the protocol.
     * Uses DebugProtocolRouter to detect protocol type and select appropriate
     * processor.
     *
     * @param request HttpServletRequest to analyze for protocol detection.
     * @param context AuthenticationContext for additional protocol hints.
     * @return DebugProcessor implementation if found, null otherwise.
     */
    private Object getProtocolSpecificProcessor(HttpServletRequest request, AuthenticationContext context) {

        try {
            String resourceId = extractResourceId(context, request);
            logProcessorRouting(resourceId);

            Object processor = DebugProtocolRouter.getProcessorForResource(resourceId);
            logProcessorUnavailable(processor, resourceId);

            return processor;

        } catch (Exception e) {
            LOG.error("Error discovering protocol-specific DebugProcessor: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extracts resource ID from context properties or request state parameter.
     *
     * @param context AuthenticationContext for additional protocol hints.
     * @param request HttpServletRequest to analyze for protocol detection.
     * @return Resource ID if found, null otherwise.
     */
    private String extractResourceId(AuthenticationContext context, HttpServletRequest request) {

        String resourceId = extractResourceIdFromContext(context);

        if (resourceId == null) {
            resourceId = extractResourceIdFromRequest(request);
        }

        return resourceId;
    }

    /**
     * Extracts resource ID from context properties.
     *
     * @param context AuthenticationContext to extract from.
     * @return Resource ID if found, null otherwise.
     */
    private String extractResourceIdFromContext(AuthenticationContext context) {

        if (context == null) {
            return null;
        }

        String resourceId = (String) context.getProperty("resourceId");
        if (resourceId == null) {
            resourceId = (String) context.getProperty("resourceName");
        }

        return resourceId;
    }

    /**
     * Extracts resource ID from request state parameter.
     *
     * @param request HttpServletRequest to analyze.
     * @return Resource ID if found, null otherwise.
     */
    private String extractResourceIdFromRequest(HttpServletRequest request) {

        if (request == null) {
            return null;
        }

        String state = request.getParameter(DebugFrameworkConstants.OAUTH2_STATE_PARAM);
        if (state != null) {
            String debugSessionId = extractDebugSessionIdFromState(state);
            if (debugSessionId != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug session ID extracted from state: " + debugSessionId);
                }
                return debugSessionId;
            }
        }

        return null;
    }

    /**
     * Logs processor routing information.
     *
     * @param resourceId Resource ID for routing.
     */
    private void logProcessorRouting(String resourceId) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Routing processor for resource" + (resourceId != null ? ": " + resourceId : ""));
        }
    }

    /**
     * Logs when processor is unavailable.
     *
     * @param processor  The processor instance found.
     * @param resourceId Resource ID for which processor was not found.
     */
    private void logProcessorUnavailable(Object processor, String resourceId) {

        if (processor == null && LOG.isDebugEnabled()) {
            LOG.debug("Protocol-specific processor not available for resource: " + resourceId);
        }
    }

    /**
     * Invokes the processCallback method on a DebugProcessor instance via
     * reflection.
     *
     * @param processor The processor instance (DebugProcessor or subclass).
     * @param request   HttpServletRequest from the callback.
     * @param response  HttpServletResponse for the callback.
     * @param context   AuthenticationContext for the debug flow.
     * @throws IllegalArgumentException      If processor is null.
     * @throws DebugFrameworkServerException If method invocation fails.
     */
    private void invokeProcessorCallback(Object processor, HttpServletRequest request,
            HttpServletResponse response, AuthenticationContext context) throws DebugFrameworkServerException {

        if (processor == null) {
            throw new IllegalArgumentException("DebugProcessor is null");
        }

        try {
            Class<?> processorClass = processor.getClass();
            java.lang.reflect.Method processCallbackMethod = processorClass.getMethod(
                    "processCallback",
                    HttpServletRequest.class,
                    HttpServletResponse.class,
                    AuthenticationContext.class);

            processCallbackMethod.invoke(processor, request, response, context);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully invoked processCallback on " + processorClass.getSimpleName());
            }

        } catch (NoSuchMethodException e) {
            String errorMsg = "processCallback method not found " + processor.getClass().getName();
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        } catch (IllegalAccessException e) {
            String errorMsg = "Cannot access processCallback method  " + processor.getClass().getName();
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = "Error in processor callback";
            if (cause != null) {
                errorMsg += " - actual error: " + cause.getMessage();
                LOG.error(errorMsg, cause);
                throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, cause);
            } else {
                LOG.error(errorMsg, e);
                throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
            }
        } catch (SecurityException e) {
            String errorMsg = "Security exception accessing processCallback method: " + e.getMessage();
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        }
    }

    /**
     * Creates a new authentication context for debug callback processing.
     * 
     * @param code  OAuth authorization code.
     * @param state OAuth state parameter.
     * @return New AuthenticationContext configured for debug processing.
     */
    private AuthenticationContext createDebugContextForCallback(String code, String state) {

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
            AuthenticationContextCacheEntry cacheEntry = AuthenticationContextCache.getInstance()
                    .getValueFromCache(cacheKey);

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
     * @param response     HttpServletResponse.
     * @param errorCode    Error code identifier.
     * @param errorMessage Detailed error message.
     */
    private void sendErrorResponse(HttpServletResponse response, String errorCode, String errorMessage) {

        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            // Use org.json library for safe JSON serialization to prevent injection
            // attacks.
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", errorCode != null ? errorCode : "");
            errorResponse.put("message", errorMessage != null ? errorMessage : "");
            errorResponse.put("timestamp", System.currentTimeMillis());

            response.getWriter().write(errorResponse.toString());
            response.getWriter().flush();

        } catch (IOException e) {
            LOG.error("Error sending error response: " + e.getMessage(), e);
        }
    }
}
