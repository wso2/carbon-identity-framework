/*
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base class for routing incoming authentication requests to appropriate handlers.
 * Inspects /commonauth requests to identify debug flow callbacks and routes them 
 * to concrete DebugProcessor implementations while passing regular authentication to default WSO2 RequestCoordinator.
 * 
 * Implementations should handle protocol-specific callback processing (OAuth2, SAML, etc).
 */
public abstract class DebugRequestCoordinator implements DebugService {

    private static final Log LOG = LogFactory.getLog(DebugRequestCoordinator.class);
    private static final String SESSION_DATA_KEY_PARAM = "sessionDataKey";
    protected static final String DEBUG_SESSION_PREFIX = "debug-";

    /**
     * Handles /commonauth requests with proper debug flow routing.
     * This method should be called from the main WSO2 /commonauth handler.
     *
     * @param request HttpServletRequest from /commonauth endpoint.
     * @param response HttpServletResponse.
     * @return true if request was handled by debug processor, false if it should be handled by regular flow.
     * @throws IOException If processing fails.
     */
    @Override
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
     * Checks if the given request is a debug flow (protocol-specific implementation).
     * This method is implemented by subclasses to detect their protocol-specific debug flows.
     *
     * @param request HttpServletRequest to inspect.
     * @return true if this is a debug flow, false otherwise.
     */
    @Override
    public abstract boolean isDebugFlow(HttpServletRequest request);

    /**
     * Determines if the incoming request is a debug flow callback.
     * Checks for debug session identifier in request parameters.
     * Must be implemented by subclasses to handle protocol-specific callback detection.
     *
     * @param request HttpServletRequest to inspect.
     * @return true if this is a debug flow callback, false otherwise.
     */
    protected abstract boolean isDebugFlowCallback(HttpServletRequest request);

    /**
     * Processes a debug flow callback after it's been identified as such.
     * Retrieves or creates authentication context and delegates to protocol-specific processor.
     *
     * @param request HttpServletRequest containing callback parameters.
     * @param response HttpServletResponse.
     * @return true if callback was processed, false otherwise.
     * @throws IOException If processing fails.
     */
    private boolean handleDebugFlowCallback(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            // Extract callback parameters specific to this debug coordinator.
            String sessionDataKey = request.getParameter(SESSION_DATA_KEY_PARAM);

            // Try to retrieve authentication context from cache.
            AuthenticationContext context = null;

            // First, try with sessionDataKey if available.
            if (sessionDataKey != null) {
                context = retrieveDebugContextFromCache(sessionDataKey);
            }

            // If not found, try to find context using protocol-specific callback identifier.
            if (context == null) {
                context = retrieveContextFromCallbackParameters(request);
            }

            // If still no context found, create a new one for callback processing.
            if (context == null) {
                context = createDebugContextForCallback(request);
                if (context == null) {
                    LOG.error("Cannot process debug callback: no context available");
                    if (!response.isCommitted()) {
                        sendErrorResponse(response, "MISSING_CONTEXT", "Authentication context not found");
                    }
                    return false;
                }
            }

            // Update context with callback parameters.
            updateContextWithCallbackParameters(context, request);
            context.setProperty("DEBUG_CALLBACK_TIMESTAMP", System.currentTimeMillis());
            context.setProperty("DEBUG_CALLBACK_PROCESSED", "true");

            // Route to protocol-specific debug processor.
            if (!response.isCommitted()) {
                return processDebugCallback(request, response, context);
            }
            return true;

        } catch (Exception e) {
            LOG.error("Error processing debug flow callback", e);
            if (e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null) {
                LOG.error("InvocationTargetException cause:", e.getCause());
            }
            if (!response.isCommitted()) {
                sendErrorResponse(response, "DEBUG_PROCESSING_ERROR", e.getMessage());
            }
            return false;
        }
    }

    /**
     * Retrieves debug context from cache based on session data key.
     *
     * @param sessionDataKey Session data key for cache lookup.
     * @return AuthenticationContext if found, null otherwise.
     */
    protected AuthenticationContext retrieveDebugContextFromCache(String sessionDataKey) {
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
    protected void cacheDebugContext(AuthenticationContext context) {
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
     * Sends a JSON error response to the client.
     *
     * @param response HttpServletResponse.
     * @param errorCode Error code identifier.
     * @param errorMessage Detailed error message.
     */
    protected void sendErrorResponse(HttpServletResponse response, String errorCode, String errorMessage) {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            String errorJson = "{\"error\":\"" + errorCode + "\"," +
                             "\"message\":\"" + (errorMessage != null ? errorMessage.replace("\"", "\\\"") : "") +
                             "\"," +
                             "\"timestamp\":" + System.currentTimeMillis() + "}";

            response.getWriter().write(errorJson);
            response.getWriter().flush();

        } catch (IOException e) {
            LOG.error("Error sending error response: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves authentication context from protocol-specific callback parameters.
     * Must be implemented by subclasses to extract context identifiers from their protocol callbacks.
     * For example, OAuth2 implementations extract from 'state' parameter.
     *
     * @param request HttpServletRequest containing callback parameters.
     * @return AuthenticationContext if found, null otherwise.
     */
    protected abstract AuthenticationContext retrieveContextFromCallbackParameters(HttpServletRequest request);

    /**
     * Creates a new authentication context for debug callback processing.
     * Must be implemented by subclasses to handle protocol-specific context initialization.
     * 
     * @param request HttpServletRequest for additional context.
     * @return New AuthenticationContext configured for debug processing, or null if unable to create.
     */
    protected abstract AuthenticationContext createDebugContextForCallback(HttpServletRequest request);

    /**
     * Updates authentication context with protocol-specific callback parameters.
     * Must be implemented by subclasses to extract and store their protocol parameters.
     *
     * @param context AuthenticationContext to update.
     * @param request HttpServletRequest containing callback parameters.
     */
    protected abstract void updateContextWithCallbackParameters(AuthenticationContext context,
            HttpServletRequest request);

    /**
     * Processes the debug callback using protocol-specific processor.
     * Must be implemented by subclasses to delegate to their protocol-specific DebugProcessor.
     *
     * @param request HttpServletRequest containing callback parameters.
     * @param response HttpServletResponse.
     * @param context AuthenticationContext.
     * @return true if callback was processed successfully, false otherwise.
     * @throws IOException If processing fails.
     */
    protected abstract boolean processDebugCallback(HttpServletRequest request, HttpServletResponse response, 
            AuthenticationContext context) throws IOException;
}

