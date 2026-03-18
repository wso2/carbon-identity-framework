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

package org.wso2.carbon.identity.debug.idp.core.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.ErrorMessages;
import org.wso2.carbon.identity.debug.framework.core.DebugProcessor;
import org.wso2.carbon.identity.debug.framework.extension.DebugCallbackHandler;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.idp.core.DebugProtocolRouter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default OIDC/OAuth debug callback handler.
 */
public class OidcDebugCallbackHandler implements DebugCallbackHandler {

    private static final Log LOG = LogFactory.getLog(OidcDebugCallbackHandler.class);

    private static final String CONTEXT_KEY_CONNECTION_ID = "connectionId";
    private static final String CONTEXT_KEY_RESOURCE_TYPE = "resourceType";
    private static final String CONTEXT_KEY_RESOURCE_NAME = "resourceName";
    private static final String REQUEST_KEY_CONNECTION_ID = "connectionId";
    private static final String REQUEST_KEY_IDP_NAME = "idpName";
    private static final String REQUEST_KEY_RESOURCE_TYPE = "resourceType";

    @Override
    public boolean canHandle(HttpServletRequest request) {

        String state = request.getParameter(DebugFrameworkConstants.OIDC_STATE_PARAM);
        if (state == null || !state.startsWith(DebugFrameworkConstants.DEBUG_PREFIX)) {
            return false;
        }

        return request.getParameter(DebugFrameworkConstants.OIDC_CODE_PARAM) != null
                || request.getParameter(DebugFrameworkConstants.OIDC_ERROR_PARAM) != null;
    }

    @Override
    public boolean handleCallback(HttpServletRequest request, HttpServletResponse response) {

        if (!canHandle(request)) {
            return false;
        }

        try {
            processDebugFlowCallback(request, response);
        } catch (IOException e) {
            LOG.error("Error processing debug flow callback", e);
            if (!response.isCommitted()) {
                sendIOErrorResponse(response);
            }
        } catch (RuntimeException e) {
            LOG.error("Unexpected runtime error while processing debug flow callback", e);
            if (!response.isCommitted()) {
                sendIOErrorResponse(response);
            }
        }

        return true;
    }

    private void processDebugFlowCallback(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String code = request.getParameter(DebugFrameworkConstants.OIDC_CODE_PARAM);
        String state = request.getParameter(DebugFrameworkConstants.OIDC_STATE_PARAM);
        String error = request.getParameter(DebugFrameworkConstants.OIDC_ERROR_PARAM);
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
            if (!response.isCommitted()) {
                String connectionId = extractConnectionId(context, request);
                String description = String.format(ErrorMessages.ERROR_CODE_EXECUTOR_NOT_FOUND.getDescription(),
                        connectionId != null ? connectionId : "unknown");
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ErrorMessages.ERROR_CODE_EXECUTOR_NOT_FOUND.getCode(),
                        description);
            }
            return;
        }

        if (!response.isCommitted()) {
            processor.processCallback(request, response, context);
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
            context = createDebugContextForCallback(state);
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

        if (context == null) {
            LOG.warn("Cannot set context properties: context is null");
            return;
        }

        if (code != null) {
            context.setProperty(DebugFrameworkConstants.DEBUG_OIDC_CODE, code);
        }
        if (state != null) {
            context.setProperty(DebugFrameworkConstants.DEBUG_OIDC_STATE, state);
        }
        if (sessionDataKey != null) {
            context.setProperty(DebugFrameworkConstants.DEBUG_SESSION_DATA_KEY, sessionDataKey);
        }
        context.setProperty(DebugFrameworkConstants.DEBUG_CALLBACK_TIMESTAMP, System.currentTimeMillis());
        context.setProperty(DebugFrameworkConstants.DEBUG_CALLBACK_PROCESSED, DebugFrameworkConstants.TRUE);
    }

    private DebugProcessor getProtocolSpecificProcessor(HttpServletRequest request, AuthenticationContext context) {

        String connectionId = extractConnectionId(context, request);
        String resourceType = extractResourceType(context, request);
        if (resourceType == null) {
            resourceType = DebugFrameworkConstants.RESOURCE_TYPE_IDP;
        }
        DebugResourceHandler resourceHandler = DebugProtocolRouter.getDebugResourceHandler(resourceType);
        if (resourceHandler == null) {
            return null;
        }
        return resourceHandler.getProcessor(connectionId);
    }

    private String extractConnectionId(AuthenticationContext context, HttpServletRequest request) {

        String connectionId = extractConnectionIdFromContext(context);
        return connectionId != null ? connectionId : extractConnectionIdFromRequest(request);
    }

    private String extractResourceType(AuthenticationContext context, HttpServletRequest request) {

        String resourceType = extractResourceTypeFromContext(context);
        if (resourceType != null) {
            return resourceType;
        }
        return extractResourceTypeFromRequest(request);
    }

    private String extractConnectionIdFromContext(AuthenticationContext context) {

        if (context == null) {
            return null;
        }
        return firstNonBlankString(
                context.getProperty(CONTEXT_KEY_CONNECTION_ID),
                context.getProperty(CONTEXT_KEY_RESOURCE_NAME),
                context.getProperty(DebugFrameworkConstants.DEBUG_CONNECTION_ID));
    }

    private String extractConnectionIdFromRequest(HttpServletRequest request) {

        if (request == null) {
            return null;
        }
        return firstNonBlankString(
                request.getParameter(REQUEST_KEY_CONNECTION_ID),
                request.getParameter(REQUEST_KEY_IDP_NAME));
    }

    private String extractResourceTypeFromContext(AuthenticationContext context) {

        if (context == null) {
            return null;
        }
        return firstNonBlankString(context.getProperty(CONTEXT_KEY_RESOURCE_TYPE));
    }

    private String extractResourceTypeFromRequest(HttpServletRequest request) {

        if (request == null) {
            return null;
        }
        return firstNonBlankString(request.getParameter(REQUEST_KEY_RESOURCE_TYPE));
    }

    private AuthenticationContext createDebugContextForCallback(String state) {

        AuthenticationContext context = new AuthenticationContext();

        String debugId = extractDebugIdFromState(state);
        if (debugId != null) {
            context.setContextIdentifier(DebugFrameworkConstants.DEBUG_PREFIX + debugId);
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
        } catch (RuntimeException e) {
            LOG.error("Error retrieving debug context from cache: " + e.getMessage(), e);
        }
        return null;
    }

    private void cacheDebugContext(AuthenticationContext context) {

        AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(context.getContextIdentifier());
        AuthenticationContextCacheEntry cacheEntry = new AuthenticationContextCacheEntry(context);
        AuthenticationContextCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    private String extractDebugIdFromState(String state) {

        if (state != null && state.startsWith(DebugFrameworkConstants.DEBUG_PREFIX)) {
            return state.substring(DebugFrameworkConstants.DEBUG_PREFIX.length());
        }
        return null;
    }

    private void sendIOErrorResponse(HttpServletResponse response) {

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
