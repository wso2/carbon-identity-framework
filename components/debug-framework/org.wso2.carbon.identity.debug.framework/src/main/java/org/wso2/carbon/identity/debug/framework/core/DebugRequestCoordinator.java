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
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.identity.application.authentication.framework.DebugAuthenticationInterceptor;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.ErrorMessages;
import org.wso2.carbon.identity.debug.framework.cache.DebugSessionCache;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkClientException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.extension.DebugCallbackHandler;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.internal.DebugFrameworkServiceDataHolder;
import org.wso2.carbon.identity.debug.framework.listener.DebugExecutionListener;
import org.wso2.carbon.identity.debug.framework.model.DebugRequest;
import org.wso2.carbon.identity.debug.framework.model.DebugResourceType;
import org.wso2.carbon.identity.debug.framework.model.DebugResponse;
import org.wso2.carbon.identity.debug.framework.util.DebugFrameworkUtils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Routes incoming authentication requests to appropriate handlers.
 * Handles protocol-specific callback requests (/commonauth) via registered
 * DebugCallbackHandler implementations.
 */
public class DebugRequestCoordinator implements DebugAuthenticationInterceptor {

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
     * @throws DebugFrameworkServerException If a server-side error occurs.
     */
    public DebugResponse handleInitialDebugRequest(DebugRequest debugRequest)
            throws DebugFrameworkClientException, DebugFrameworkServerException {

        if (debugRequest == null) {
            throw DebugFrameworkUtils.handleClientException(ErrorMessages.ERROR_CODE_INVALID_REQUEST);
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
     * Retrieves the debug result for the given debug ID, invoking listeners.
     * This ensures that post-execution listeners (like cleanup) are executed.
     *
     * @param debugId The debug ID to retrieve.
     * @return The debug result as a typed map.
     * @throws DebugFrameworkClientException If the session is not found or validation fails.
     * @throws DebugFrameworkServerException If a server-side error occurs.
     */
    public Map<String, Object> getDebugResult(String debugId)
            throws DebugFrameworkClientException, DebugFrameworkServerException {

        if (debugId == null) {
            throw DebugFrameworkUtils.handleClientException(ErrorMessages.ERROR_CODE_INVALID_REQUEST);
        }

        // Create a minimal request context for the listeners.
        DebugRequest debugRequest = new DebugRequest();
        debugRequest.setConnectionId(debugId);
        debugRequest.setResourceType(DebugFrameworkConstants.DEBUG_RESULT_RETRIEVAL);

        try {
            // Pre-execute Listeners.
            executePreListeners(debugRequest);

            // Execution: Get from cache (Pure Read).
            String resultJson = DebugSessionCache.getInstance().getResult(debugId);

            if (resultJson == null) {
                throw DebugFrameworkUtils.handleClientException(
                        ErrorMessages.ERROR_CODE_RESULT_NOT_FOUND, debugId);
            }

            Map<String, Object> resultData;
            try {
                resultData = new JSONObject(resultJson).toMap();
            } catch (JSONException e) {
                LOG.error("Invalid debug result JSON for session: " + debugId, e);
                throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e);
            }

            // Create response object for listeners.
            Map<String, Object> data = new HashMap<>();
            data.put("result", resultData);
            DebugResponse debugResponse = DebugResponse.success(data);

            // Post-Execute Listeners.
            executePostListeners(debugResponse, debugRequest);

            return resultData;

        } catch (DebugFrameworkClientException e) {
            throw e;
        } catch (DebugFrameworkServerException e) {
            LOG.error("Error retrieving debug result for session: " + debugId, e);
            throw e;
        } catch (DebugFrameworkException e) {
            LOG.error("Debug framework error retrieving result for session: " + debugId, e);
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e);
        }
    }

    /**
     * Handles /commonauth requests with proper debug flow routing.
     * Returns false by default to allow normal authentication flow to proceed.
     * Only returns true if debug handling explicitly completes the request.
     *
     * @param request  HttpServletRequest.
     * @param response HttpServletResponse.
     * @return true if handled as debug flow and normal authentication should be skipped.
     */
    public boolean handleCommonAuthRequest(HttpServletRequest request, HttpServletResponse response) {

        try {
            DebugCallbackHandler handler = resolveCallbackHandler(request);
            if (!handler.canHandle(request)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Request is not a debug flow callback. Skipping debug handling.");
                }
                return false;
            }

            return handler.handleCallback(request, response);
        } catch (Exception e) {
            LOG.warn("Error handling debug authentication flow. Normal authentication will proceed.", e);
            // Don't throw exception; allow normal authentication to continue.
            return false;
        }
    }

    private DebugCallbackHandler resolveCallbackHandler(HttpServletRequest request) {

        for (DebugCallbackHandler handler : DebugProtocolRouter.getAllCallbackHandlers()) {
            if (handler != null && handler.canHandle(request)) {
                return handler;
            }
        }

        return null;
    }
}
