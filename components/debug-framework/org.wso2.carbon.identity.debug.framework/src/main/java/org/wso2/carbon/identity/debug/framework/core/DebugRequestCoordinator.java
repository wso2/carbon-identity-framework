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
import org.wso2.carbon.identity.debug.framework.DebugCommonAuthHandler;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.ErrorMessages;
import org.wso2.carbon.identity.debug.framework.cache.DebugSessionCache;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkClientException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.internal.DebugFrameworkServiceDataHolder;
import org.wso2.carbon.identity.debug.framework.listener.DebugExecutionListener;
import org.wso2.carbon.identity.debug.framework.model.DebugRequest;
import org.wso2.carbon.identity.debug.framework.model.DebugResourceType;
import org.wso2.carbon.identity.debug.framework.model.DebugResponse;
import org.wso2.carbon.identity.debug.framework.util.DebugFrameworkUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Routes incoming authentication requests to appropriate handlers.
 * Handles two main flows:
 * OAuth callback requests (/commonauth) - routes to protocol-specific
 * DebugProcessor.
 */
public class DebugRequestCoordinator implements DebugCommonAuthHandler {

    private static final Log LOG = LogFactory.getLog(DebugRequestCoordinator.class);
    private static final String CONTEXT_KEY_CONNECTION_ID = "connectionId";
    private static final String CONTEXT_KEY_RESOURCE_NAME = "resourceName";
    private static final String REQUEST_KEY_CONNECTION_ID = "connectionId";
    private static final String REQUEST_KEY_RESOURCE_ID = "resourceId";
    private static final String REQUEST_KEY_IDP_NAME = "idpName";

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
     * @throws DebugFrameworkServerException If a server-side error occurs.
     */
    public DebugResponse handleInitialDebugRequest(DebugRequest debugRequest)
            throws DebugFrameworkClientException, DebugFrameworkServerException {

        if (debugRequest == null) {
            throw DebugFrameworkUtils.handleClientException(ErrorMessages.ERROR_CODE_INVALID_REQUEST);
        }

        // Default to IDP if resource type is not provided (legacy support).
        if (debugRequest.getResourceType() == null) {
            debugRequest.setResourceType(DebugFrameworkConstants.RESOURCE_TYPE_IDP);
        }

        return handleResourceDebugRequest(debugRequest);
    }

    /**
     * Handles debug requests for any resource type using typed classes.
     * This is the preferred method with type safety.
     * The connectionId is optional and may be null for connection types that don't
     * require it.
     *
     * @param debugRequest The debug request with resource information.
     * @return DebugResponse containing debug result data.
     * @throws DebugFrameworkClientException If the request has validation errors.
     * @throws DebugFrameworkServerException If a server-side error occurs.
     */
    public DebugResponse handleResourceDebugRequest(DebugRequest debugRequest)
            throws DebugFrameworkClientException, DebugFrameworkServerException {

        validateDebugRequest(debugRequest);

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Orchestrating debug request for resource type: " + debugRequest.getResourceType()
                        + ", resource ID: " + debugRequest.getEffectiveConnectionId());
            }

            // Pre-execute listeners.
            executePreListeners(debugRequest);

            // Route by resource type.
            DebugResourceType type = DebugResourceType.fromString(debugRequest.getResourceType());

            // Get the handler for this resource type.
            DebugResourceHandler handler = type.getHandler();

            if (handler == null) {
                throw DebugFrameworkUtils.handleClientException(
                        ErrorMessages.ERROR_CODE_HANDLER_NOT_FOUND, debugRequest.getResourceType());
            }

            // Delegate to handler with typed objects.
            DebugResponse debugResponse = handler.handleDebugRequest(debugRequest);

            // Post-execute listeners.
            executePostListeners(debugResponse, debugRequest);

            return debugResponse;

        } catch (DebugFrameworkClientException e) {
            // Re-throw client exceptions.
            throw e;
        } catch (DebugFrameworkException e) {
            LOG.error("Debug framework error in request orchestration.", e);
            if (e instanceof DebugFrameworkServerException) {
                throw (DebugFrameworkServerException) e;
            }
            if (e.getErrorCode() != null) {
                throw new DebugFrameworkServerException(e.getErrorCode(), e.getMessage(), e.getDescription(), e);
            }
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e);
        } catch (Exception e) {
            LOG.error("Unexpected error in debug request orchestration.", e);
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e);
        }
    }

    /**
     * Validates the debug request.
     *
     * @param debugRequest The debug request to validate.
     * @throws DebugFrameworkClientException If validation fails.
     */
    private void validateDebugRequest(DebugRequest debugRequest) throws DebugFrameworkClientException {

        if (debugRequest == null) {
            throw DebugFrameworkUtils.handleClientException(ErrorMessages.ERROR_CODE_INVALID_REQUEST);
        }

        if (debugRequest.getResourceType() == null || debugRequest.getResourceType().trim().isEmpty()) {
            throw DebugFrameworkUtils.handleClientException(ErrorMessages.ERROR_CODE_MISSING_RESOURCE_TYPE);
        }
    }

    /**
     * Executes pre-execute listeners.
     *
     * @param debugRequest The debug request.
     * @throws DebugFrameworkClientException If a listener aborts the request.
     * @throws DebugFrameworkException       If a listener throws an exception.
     */
    private void executePreListeners(DebugRequest debugRequest) throws DebugFrameworkException {

        for (DebugExecutionListener listener : DebugFrameworkServiceDataHolder.getInstance()
                .getDebugExecutionListeners()) {
            if (listener.isEnabled() && !listener.doPreExecute(debugRequest)) {
                throw DebugFrameworkUtils.handleClientException(
                        ErrorMessages.ERROR_CODE_LISTENER_ABORTED, "pre-execute");
            }
        }
    }

    /**
     * Executes post-execute listeners.
     *
     * @param debugResponse The debug response.
     * @param debugRequest  The debug request.
     * @throws DebugFrameworkClientException If a listener aborts the request.
     * @throws DebugFrameworkException       If a listener throws an exception.
     */
    private void executePostListeners(DebugResponse debugResponse, DebugRequest debugRequest)
            throws DebugFrameworkException {

        for (DebugExecutionListener listener : DebugFrameworkServiceDataHolder.getInstance()
                .getDebugExecutionListeners()) {
            if (listener.isEnabled() && !listener.doPostExecute(debugResponse, debugRequest)) {
                throw DebugFrameworkUtils.handleClientException(
                        ErrorMessages.ERROR_CODE_LISTENER_ABORTED, "post-execute");
            }
        }
    }

    /**
     * Retrieves the debug result for the given session ID, invoking listeners.
     * This ensures that post-execution listeners (like cleanup) are executed.
     *
     * @param sessionId The session ID to retrieve.
     * @return The debug result JSON string.
     * @throws DebugFrameworkClientException If the session is not found or validation fails.
     * @throws DebugFrameworkServerException If a server-side error occurs.
     */
    public String getDebugResult(String sessionId)
            throws DebugFrameworkClientException, DebugFrameworkServerException {

        if (sessionId == null) {
            throw DebugFrameworkUtils.handleClientException(ErrorMessages.ERROR_CODE_INVALID_REQUEST);
        }

        // Create a minimal request context for the listeners.
        DebugRequest debugRequest = new DebugRequest();
        debugRequest.setConnectionId(sessionId);
        debugRequest.setResourceType(DebugFrameworkConstants.DEBUG_RESULT_RETRIEVAL);

        try {
            // Pre-execute Listeners.
            executePreListeners(debugRequest);

            // Execution: Get from cache (Pure Read).
            String resultJson = DebugSessionCache.getInstance().getResult(sessionId);

            if (resultJson == null) {
                throw DebugFrameworkUtils.handleClientException(
                        ErrorMessages.ERROR_CODE_RESULT_NOT_FOUND, sessionId);
            }

            // Create response object for listeners.
            Map<String, Object> data = new HashMap<>();
            data.put("result", resultJson);
            DebugResponse debugResponse = DebugResponse.success(data);

            // Post-Execute Listeners.
            executePostListeners(debugResponse, debugRequest);

            return resultJson;

        } catch (DebugFrameworkClientException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error retrieving debug result for session: " + sessionId, e);
            if (e instanceof DebugFrameworkServerException) {
                throw (DebugFrameworkServerException) e;
            }
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e);
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

        boolean isDebugFlow = isDebugFlowCallback(request);
        try {
            if (isDebugFlow) {
                handleDebugFlowCallback(request, response);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("Error processing /commonauth request", e);
            if (isDebugFlow && !response.isCommitted()) {
                sendServerErrorResponse(response, e);
            }
            return isDebugFlow;
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

            if (processor == null) {
                LOG.error("No suitable DebugProcessor found for callback");
                return;
            }

            if (!response.isCommitted()) {
                processor.processCallback(request, response, context);
            }

        } catch (Exception e) {
            LOG.error("Error processing debug flow callback", e);
            if (!response.isCommitted()) {
                sendServerErrorResponse(response, e);
            }
        }
    }

    private boolean handleOAuthError(String error, HttpServletResponse response) {

        if (error == null) {
            return false;
        }
        LOG.error("OAuth error in debug callback.");
        if (!response.isCommitted()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "OAUTH_ERROR", "OAuth error occurred.");
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
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "MISSING_CONTEXT", "Authentication context not found");
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
            String connectionId = extractConnectionId(context, request);
            DebugResourceHandler resourceHandler =
                    DebugProtocolRouter.getDebugResourceHandler(DebugFrameworkConstants.RESOURCE_TYPE_IDP);
            if (resourceHandler == null) {
                return null;
            }
            return resourceHandler.getProcessor(connectionId);
        } catch (Exception e) {
            LOG.error("Error discovering protocol-specific DebugProcessor: " + e.getMessage(), e);
            return null;
        }
    }

    private String extractConnectionId(AuthenticationContext context, HttpServletRequest request) {

        String connectionId = extractConnectionIdFromContext(context);
        return connectionId != null ? connectionId : extractConnectionIdFromRequest(request);
    }

    private String extractConnectionIdFromContext(AuthenticationContext context) {

        if (context == null) {
            return null;
        }
        return firstNonBlankString(
                context.getProperty(CONTEXT_KEY_CONNECTION_ID),
                context.getProperty(CONTEXT_KEY_RESOURCE_NAME),
                context.getProperty(DebugFrameworkConstants.DEBUG_RESOURCE_ID));
    }

    private String extractConnectionIdFromRequest(HttpServletRequest request) {

        if (request == null) {
            return null;
        }
        return firstNonBlankString(
                request.getParameter(REQUEST_KEY_CONNECTION_ID),
                request.getParameter(REQUEST_KEY_RESOURCE_ID),
                request.getParameter(REQUEST_KEY_IDP_NAME));
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

        sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorCode, errorMessage);
    }

    private void sendServerErrorResponse(HttpServletResponse response, Exception e) {

        if (e instanceof DebugFrameworkClientException) {
            DebugFrameworkClientException clientException = (DebugFrameworkClientException) e;
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    resolveErrorCode(clientException.getErrorCode(), 
                        ErrorMessages.ERROR_CODE_INVALID_REQUEST.getCode()),
                    resolveErrorMessage(clientException.getDescription(), clientException.getMessage(),
                            ErrorMessages.ERROR_CODE_INVALID_REQUEST.getDescription()));
            return;
        }

        if (e instanceof DebugFrameworkException) {
            DebugFrameworkException frameworkException = (DebugFrameworkException) e;
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    resolveErrorCode(frameworkException.getErrorCode(), 
                        ErrorMessages.ERROR_CODE_SERVER_ERROR.getCode()),
                    resolveErrorMessage(frameworkException.getDescription(), frameworkException.getMessage(),
                            ErrorMessages.ERROR_CODE_SERVER_ERROR.getDescription()));
            return;
        }

        sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                ErrorMessages.ERROR_CODE_SERVER_ERROR.getCode(),
                ErrorMessages.ERROR_CODE_SERVER_ERROR.getDescription());
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String errorCode, String errorMessage) {

        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(status);

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

    private String resolveErrorCode(String value, String fallback) {

        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        return fallback;
    }

    private String resolveErrorMessage(String description, String message, String fallback) {

        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }
        return fallback;
    }

    private String firstNonBlankString(Object... values) {

        for (Object value : values) {
            if (value instanceof String) {
                String text = (String) value;
                if (!text.trim().isEmpty()) {
                    return text;
                }
            }
        }
        return null;
    }
}
