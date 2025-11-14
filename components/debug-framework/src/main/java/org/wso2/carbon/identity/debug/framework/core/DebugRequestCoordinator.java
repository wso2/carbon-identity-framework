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
 * Inspects /commonauth requests to identify debug flow callbacks and routes them 
 * to DebugProcessor while passing regular authentication to default WSO2 RequestCoordinator.
 */
public class DebugRequestCoordinator implements DebugService {

    private static final Log LOG = LogFactory.getLog(DebugRequestCoordinator.class);

    /**
     * Constructs a DebugRequestCoordinator.
     */
    public DebugRequestCoordinator() {
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
            // NOTE: Return true because debug flow handled the error - don't fall through to DefaultRequestCoordinator
            if (error != null) {
                LOG.error("OAuth error in debug callback: " + error);
                if (!response.isCommitted()) {
                    sendErrorResponse(response, "OAUTH_ERROR",
                            "OAuth error: " + error);
                }
                return true; // Debug flow handled the error response
            }

            // Try to retrieve or create authentication context.
            AuthenticationContext context = null;

            // First, try with sessionDataKey if available (framework cache).
            if (sessionDataKey != null) {
                context = retrieveDebugContextFromCache(sessionDataKey);
            }

            // If not found, try to retrieve from OAuth2UrlBuilder's DebugSessionCache using state parameter.
            if (context == null && state != null) {
                context = retrieveOAuth2DebugContext(state);
                
                if (context != null && LOG.isDebugEnabled()) {
                    LOG.debug("Retrieved OAuth2 debug context from cache for state: " + state);
                }
            }

            // If still no context found using cache lookups, try extracting from state parameter.
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
                } else {
                    LOG.error("Cannot process debug callback: missing OAuth parameters and no cached context");
                    if (!response.isCommitted()) {
                        sendErrorResponse(response, "MISSING_CONTEXT_AND_PARAMS",

                                "Authentication context not found and OAuth parameters missing");
                    }
                    return true; // Debug flow handled the error response
                }
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
            context.setProperty(DebugFrameworkConstants.DEBUG_CALLBACK_PROCESSED, DebugFrameworkConstants.TRUE_STRING);

            // Route to protocol-specific DebugProcessor implementation via reflection.
            // This ensures protocol-agnostic processing without hard dependency on specific protocol implementations.
            Object processor = getProtocolSpecificProcessor(request, context);
            if (processor != null && !response.isCommitted()) {
                invokeProcessorCallback(processor, request, response, context);
            }
            return true;

        } catch (Exception e) {
            LOG.error("Error processing debug flow callback", e);
            // Enhanced error logging for debugging InvocationTargetException root cause.
            if (e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null) {
                LOG.error("InvocationTargetException cause:", e.getCause());
            }
            if (!response.isCommitted()) {
                sendErrorResponse(response, "DEBUG_PROCESSING_ERROR", e.getMessage());
            }
            return true; // Debug flow handled the error response - don't fall through to DefaultRequestCoordinator
        }
    }

    /**
     * Gets the appropriate DebugProcessor implementation for the protocol detected in the request.
     * Uses reflection to discover protocol-specific processors to avoid hard dependencies.
     *
     * @param request HttpServletRequest to analyze for protocol detection.
     * @param context AuthenticationContext for additional protocol hints.
     * @return DebugProcessor implementation if found, null otherwise.
     */
    private Object getProtocolSpecificProcessor(HttpServletRequest request, AuthenticationContext context) {
        try {
            // Try to load OAuth2DebugProcessor via reflection from identity-outbound-auth-oidc
            // This is the first place to check since OAuth callbacks are most common
            String code = request.getParameter(DebugFrameworkConstants.OAUTH2_CODE_PARAM);
            if (code != null) {
                // This is likely an OAuth2 callback
                String oauth2DebugProcessorClass = 
                        "org.wso2.carbon.identity.application.authenticator.oidc.debug.OAuth2DebugProcessor";
                return loadProcessorByClass(oauth2DebugProcessorClass);
            }

            // Try to load SAMLDebugProcessor via reflection from SAML connector if available
            String samlResponse = request.getParameter("SAMLResponse");
            if (samlResponse != null) {
                // This is likely a SAML callback
                String samlDebugProcessorClass =
                        "org.wso2.carbon.identity.application.authenticator.saml.debug.SAMLDebugProcessor";
                return loadProcessorByClass(samlDebugProcessorClass);
            }

            // Default fallback - log and return null
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unable to determine protocol type for debug processor - neither OAuth nor SAML detected");
            }
            return null;

        } catch (Exception e) {
            LOG.error("Error discovering protocol-specific DebugProcessor: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Loads a DebugProcessor implementation by class name using reflection.
     * Instantiates the class if found.
     *
     * @param className Fully qualified class name of the DebugProcessor implementation.
     * @return DebugProcessor instance if class found and instantiated, null otherwise.
     */
    private Object loadProcessorByClass(String className) {
        try {
            Class<?> processorClass = Class.forName(className);
            Object instance = processorClass.getDeclaredConstructor().newInstance();
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Loaded DebugProcessor: " + className);
            }
            return instance;
            
        } catch (ClassNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugProcessor class not found: " + className + 
                    " (This is OK if the protocol connector is not deployed)");
            }
        } catch (Exception e) {
            LOG.error("Error instantiating DebugProcessor " + className + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Invokes the processCallback method on a DebugProcessor instance via reflection.
     * This avoids hard coupling to specific DebugProcessor implementations.
     *
     * @param processor The processor instance (DebugProcessor or subclass).
     * @param request HttpServletRequest from the callback.
     * @param response HttpServletResponse for the callback.
     * @param context AuthenticationContext for the debug flow.
     * @throws Exception If method invocation fails.
     */
    private void invokeProcessorCallback(Object processor, HttpServletRequest request, 
                                        HttpServletResponse response, AuthenticationContext context) throws Exception {
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
            LOG.error("Error in processor callback - actual error: " + 
                (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()), 
                e.getCause() != null ? e.getCause() : e);
            throw new Exception("Processor callback failed", e.getCause() != null ? e.getCause() : e);
        } catch (NoSuchMethodException e) {
            LOG.error("processCallback method not found on processor: " + e.getMessage(), e);
            throw new Exception("processCallback method not found", e);
        } catch (Exception e) {
            LOG.error("Error invoking processor callback: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Retrieves OAuth2 debug context from OAuth2UrlBuilder's DebugSessionCache.
     * This cache is populated during the OAuth2 authorization URL generation and
     * contains the full context including IDP_CONFIG and other debug parameters.
     *
     * @param state OAuth state parameter used as cache key.
     * @return AuthenticationContext if found in OAuth2 cache, null otherwise.
     */
    private AuthenticationContext retrieveOAuth2DebugContext(String state) {
        try {
            // Use reflection to access OAuth2UrlBuilder's DebugSessionCache
            // (OAuth2UrlBuilder is in an optional dependency)
            String oauth2UrlBuilderClassName =
                    "org.wso2.carbon.identity.application.authenticator.oidc.debug.OAuth2UrlBuilder";
            Class.forName(oauth2UrlBuilderClassName);
            
            // Get the DebugSessionCache inner class
            String debugSessionCacheClassName = 
                    "org.wso2.carbon.identity.application.authenticator.oidc.debug.OAuth2UrlBuilder$DebugSessionCache";
            Class<?> debugSessionCacheClass = Class.forName(debugSessionCacheClassName);
            
            // Get getInstance() method
            java.lang.reflect.Method getInstanceMethod = debugSessionCacheClass.getMethod("getInstance");
            Object cacheInstance = getInstanceMethod.invoke(null);
            
            // Get get() method to retrieve the context map
            java.lang.reflect.Method getMethod = debugSessionCacheClass.getMethod("get", String.class);
            Object cachedContextMap = getMethod.invoke(cacheInstance, state);
            
            if (cachedContextMap == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No OAuth2 debug context found in cache for state: " + state);
                }
                return null;
            }
            
            // Convert the cached Map<String, Object> to AuthenticationContext
            @SuppressWarnings("unchecked")
            Map<String, Object> contextMap = (Map<String, Object>) cachedContextMap;
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved context map from cache with keys: " + contextMap.keySet());
            }
            
            // Create new AuthenticationContext from cached values
            AuthenticationContext context = new AuthenticationContext();
            
            // Set context identifier based on state parameter (IMPORTANT for caching later)
            String debugSessionId = extractDebugSessionIdFromState(state);
            if (debugSessionId != null) {
                context.setContextIdentifier(DebugFrameworkConstants.DEBUG_PREFIX + debugSessionId);
            } else {
                context.setContextIdentifier("debug-callback-" + state);
            }
            
            // Copy all properties from the cached map to the context
            for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
                context.setProperty(entry.getKey(), entry.getValue());
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully retrieved OAuth2 debug context from cache for state: " + state +
                         ", context identifier set to: " + context.getContextIdentifier() +
                         ", total properties transferred: " + contextMap.size());
            }
            
            return context;
            
        } catch (ClassNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("OAuth2UrlBuilder not available - OAuth2 debug cache not accessible (optional dependency)");
            }
        } catch (Exception e) {
            LOG.error("Error retrieving OAuth2 debug context from cache: " + e.getMessage(), e);
        }
        
        return null;
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
            // Generate a new identifier based on OAuth parameters.
            context.setContextIdentifier("debug-callback-" + System.currentTimeMillis());
        }
        
        // Mark as debug flow.
        context.setProperty(DebugFrameworkConstants.DEBUG_IDENTIFIER_PARAM, DebugFrameworkConstants.TRUE_STRING);
        context.setProperty("DEBUG_FLOW_TYPE", "OAUTH_CALLBACK");
        context.setProperty(DebugFrameworkConstants.DEBUG_OAUTH_CODE, code);
        context.setProperty(DebugFrameworkConstants.DEBUG_OAUTH_STATE, state);
        context.setProperty("DEBUG_CONTEXT_CREATED", DebugFrameworkConstants.TRUE_STRING);
        context.setProperty("DEBUG_CREATION_TIMESTAMP", System.currentTimeMillis());
        
        // Set request information if available.
        if (request != null) {
            context.setProperty("DEBUG_HTTP_REQUEST", request);
            context.setProperty(DebugFrameworkConstants.DEBUG_CALLBACK_URL, request.getRequestURL().toString());
            
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
     * Caches the debug context for callback retrieval
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
