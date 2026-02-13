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
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.DebugService;
import org.wso2.carbon.identity.debug.framework.extension.DebugExecutor;
import org.wso2.carbon.identity.debug.framework.extension.DebugProcessor;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.model.DebugRequest;
import org.wso2.carbon.identity.debug.framework.model.DebugResourceType;
import org.wso2.carbon.identity.debug.framework.model.DebugResponse;
import org.wso2.carbon.identity.debug.framework.model.DebugResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Routes incoming authentication requests to appropriate handlers.
 * Handles two main flows:
 * Generic debug requests (POST /api/server/v1/debug) - routes based on resourceType.
 * OAuth callback requests (/commonauth) - routes to protocol-specific DebugProcessor.
 */
public class DebugRequestCoordinator implements DebugService {

    private static final Log LOG = LogFactory.getLog(DebugRequestCoordinator.class);

    /**
     * Constructs a DebugRequestCoordinator instance.
     */
    public DebugRequestCoordinator() {

        // Stateless coordinator.
    }

    /**
     * Handles the initial debug request.
     * Routes to the appropriate protocol executor based on the resource ID.
     *
     * @param debugRequest The debug request with resource details.
     * @return DebugResponse containing the execution result.
     */
    public DebugResponse handleInitialDebugRequest(DebugRequest debugRequest) {

        if (debugRequest == null) {
            return DebugResponse.error("Debug request cannot be null");
        }

        try {
            String resourceId = debugRequest.getEffectiveResourceId();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Handling initial debug request for resource: " + resourceId);
            }

            // Get executor via Router.
            DebugExecutor executor = DebugProtocolRouter.getExecutorForResource(resourceId);

            if (executor == null) {
                LOG.warn("No DebugExecutor found for the given resource ID: " + resourceId);
                return DebugResponse.error("No debug executor found for the given resource.");
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Delegating to executor: " + executor.getClass().getSimpleName());
            }

            // Execute and convert DebugResult to DebugResponse.
            DebugResult result = executor.execute(debugRequest.toMap());
            return DebugResponse.fromDebugResult(result);

        } catch (Exception e) {
            LOG.error("Error handling initial debug request.", e);
            return DebugResponse.error("Server error processing debug request.");
        }
    }

    /**
     * Handles debug requests for any resource type using typed classes.
     * This is the preferred method with type safety.
     *
     * @param debugRequest The debug request with resource information.
     * @return DebugResponse containing debug result data.
     */
    public DebugResponse handleResourceDebugRequest(DebugRequest debugRequest) {

        if (debugRequest == null || debugRequest.getResourceId() == null) {
            return DebugResponse.error("Debug request or resource ID cannot be null");
        }

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Orchestrating debug request for resource: " + debugRequest.getResourceId());
            }

            // Route by resource type.
            DebugResourceType type = DebugResourceType.fromString(debugRequest.getResourceType());

            // Get the handler for this resource type.
            DebugResourceHandler handler = type.getHandler(debugRequest.getResourceId());

            if (handler == null) {
                return DebugResponse.error("No handler available for resource type: " +
                        debugRequest.getResourceType());
            }

            // Delegate to handler.
            Map<String, Object> result = handler.handleDebugRequest(debugRequest.toMap());
            return DebugResponse.success(result);

        } catch (Exception e) {
            LOG.error("Error in debug request orchestration.", e);
            return DebugResponse.error("Error processing debug request.");
        }
    }

    /**
     * Handles /commonauth requests with proper debug flow routing.
     *
     * @param request  HttpServletRequest.
     * @param response HttpServletResponse.
     * @return true if handled as debug flow.
     */
    public boolean handleCommonAuthRequest(HttpServletRequest request, HttpServletResponse response) {

        try {
            if (isDebugFlowCallback(request)) {
                handleDebugFlowCallback(request, response);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("Error processing /commonauth request", e);
            return false;
        }
    }

    @Override
    public boolean isDebugFlow(HttpServletRequest request) {
        return isDebugFlowCallback(request);
    }

    private boolean isDebugFlowCallback(HttpServletRequest request) {

        String state = request.getParameter(DebugFrameworkConstants.OAUTH2_STATE_PARAM);
        if (state == null || !state.startsWith(DebugFrameworkConstants.DEBUG_PREFIX)) {
            return false;
        }

        return request.getParameter(DebugFrameworkConstants.OAUTH2_CODE_PARAM) != null ||
                request.getParameter(DebugFrameworkConstants.OAUTH2_ERROR_PARAM) != null;
    }

    private String extractDebugSessionIdFromState(String state) {

        if (state != null && state.startsWith(DebugFrameworkConstants.DEBUG_PREFIX)) {
            return state.substring(DebugFrameworkConstants.DEBUG_PREFIX.length());
        }
        return null;
    }

    private void handleDebugFlowCallback(HttpServletRequest request, HttpServletResponse response) {

        try {
            String code = request.getParameter(DebugFrameworkConstants.OAUTH2_CODE_PARAM);
            String state = request.getParameter(DebugFrameworkConstants.OAUTH2_STATE_PARAM);
            String error = request.getParameter(DebugFrameworkConstants.OAUTH2_ERROR_PARAM);
            String sessionDataKey = request.getParameter(DebugFrameworkConstants.SESSION_DATA_KEY_PARAM);

            if (handleOAuthError(error, response)) {
                return;
            }

            AuthenticationContext context = retrieveOrCreateContext(code, state, sessionDataKey);
            if (context == null) {
                handleMissingContext(response);
                return;
            }

            setContextProperties(context, code, state, sessionDataKey);
            DebugProcessor processor = getProtocolSpecificProcessor(request, context);

            if (processor != null && !response.isCommitted()) {
                processor.processCallback(request, response, context);
            } else if (processor == null) {
                LOG.error("No suitable DebugProcessor found for callback");
            }

        } catch (Exception e) {
            LOG.error("Error processing debug flow callback", e);
            if (!response.isCommitted()) {
                sendErrorResponse(response, "DEBUG_PROCESSING_ERROR", e.getMessage());
            }
        }
    }

    private boolean handleOAuthError(String error, HttpServletResponse response) {

        if (error == null) {
            return false;
        }
        LOG.error("OAuth error in debug callback.");
        if (!response.isCommitted()) {
            sendErrorResponse(response, "OAUTH_ERROR", "OAuth error occurred.");
        }
        return true;
    }

    private AuthenticationContext retrieveOrCreateContext(String code, String state, String sessionDataKey) {

        AuthenticationContext context = null;

        if (sessionDataKey != null) {
            context = retrieveDebugContextFromCache(sessionDataKey);
        }

        if (context == null && code != null && state != null) {
            context = createDebugContextForCallback(code, state);
        }

        return context;
    }

    private void handleMissingContext(HttpServletResponse response) {

        LOG.error("Cannot process debug callback: missing context and/or OAuth parameters");
        if (!response.isCommitted()) {
            sendErrorResponse(response, "MISSING_CONTEXT", "Authentication context not found");
        }
    }

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

    private DebugProcessor getProtocolSpecificProcessor(HttpServletRequest request, AuthenticationContext context) {

        try {
            String resourceId = extractResourceId(context, request);
            return DebugProtocolRouter.getProcessorForResource(resourceId);
        } catch (Exception e) {
            LOG.error("Error discovering protocol-specific DebugProcessor: " + e.getMessage(), e);
            return null;
        }
    }

    private String extractResourceId(AuthenticationContext context, HttpServletRequest request) {

        String resourceId = extractResourceIdFromContext(context);
        if (resourceId == null) {
            resourceId = extractResourceIdFromRequest(request);
        }
        return resourceId;
    }

    private String extractResourceIdFromContext(AuthenticationContext context) {

        if (context == null) {
            return null;
        }
        String resourceId = (String) context.getProperty("resourceId");
        if (resourceId == null) {
            resourceId = (String) context.getProperty("resourceName");
        }
        if (resourceId == null) {
            resourceId = (String) context.getProperty("DEBUG_RESOURCE_ID");
        }
        return resourceId;
    }

    private String extractResourceIdFromRequest(HttpServletRequest request) {

        if (request == null) {
            return null;
        }
        String state = request.getParameter(DebugFrameworkConstants.OAUTH2_STATE_PARAM);
        if (state != null) {
            String debugSessionId = extractDebugSessionIdFromState(state);
            if (debugSessionId != null) {
                return debugSessionId;
            }
        }
        return null;
    }

    private AuthenticationContext createDebugContextForCallback(String code, String state) {

        AuthenticationContext context = new AuthenticationContext();

        String debugSessionId = extractDebugSessionIdFromState(state);
        if (debugSessionId != null) {
            context.setContextIdentifier(DebugFrameworkConstants.DEBUG_PREFIX + debugSessionId);
        } else {
            context.setContextIdentifier("debug-callback-" + System.currentTimeMillis());
        }

        context.setProperty(DebugFrameworkConstants.DEBUG_IDENTIFIER_PARAM, DebugFrameworkConstants.TRUE);
        context.setProperty(DebugFrameworkConstants.DEBUG_FLOW_TYPE, DebugFrameworkConstants.FLOW_TYPE_CALLBACK);
        context.setProperty(DebugFrameworkConstants.DEBUG_CONTEXT_CREATED, DebugFrameworkConstants.TRUE);
        context.setProperty(DebugFrameworkConstants.DEBUG_CREATION_TIMESTAMP, System.currentTimeMillis());

        cacheDebugContext(context);
        return context;
    }

    private AuthenticationContext retrieveDebugContextFromCache(String sessionDataKey) {

        try {
            AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(sessionDataKey);
            AuthenticationContextCacheEntry cacheEntry = AuthenticationContextCache.getInstance()
                    .getValueFromCache(cacheKey);
            if (cacheEntry != null) {
                return cacheEntry.getContext();
            }
        } catch (Exception e) {
            LOG.error("Error retrieving debug context from cache: " + e.getMessage(), e);
        }
        return null;
    }

    private void cacheDebugContext(AuthenticationContext context) {

        try {
            AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(context.getContextIdentifier());
            AuthenticationContextCacheEntry cacheEntry = new AuthenticationContextCacheEntry(context);
            AuthenticationContextCache.getInstance().addToCache(cacheKey, cacheEntry);
        } catch (Exception e) {
            LOG.error("Error caching debug context: " + e.getMessage(), e);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String errorCode, String errorMessage) {

        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

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

    private Map<String, Object> createErrorResponse(String status, String message) {

        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("message", message);
        return map;
    }

    private Map<String, Object> convertDebugResultToMap(DebugResult result) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("successful", result.isSuccessful());
        response.put("resultId", result.getResultId());
        response.put("timestamp", result.getTimestamp());
        response.put("status", result.getStatus());
        response.put("errorCode", result.getErrorCode());
        response.put("errorMessage", result.getErrorMessage());
        response.put("resultData", result.getResultData());
        response.put("metadata", result.getMetadata());
        return response;
    }
}
