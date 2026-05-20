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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.ErrorMessages;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkClientException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.extension.DebugCallbackHandler;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.internal.DebugFrameworkServiceDataHolder;
import org.wso2.carbon.identity.debug.framework.listener.DebugExecutionListener;
import org.wso2.carbon.identity.debug.framework.model.DebugFrameworkRequest;
import org.wso2.carbon.identity.debug.framework.model.DebugFrameworkResponse;
import org.wso2.carbon.identity.debug.framework.model.DebugResourceType;
import org.wso2.carbon.identity.debug.framework.registry.DebugHandlerRegistry;
import org.wso2.carbon.identity.debug.framework.registry.DebugProtocolRegistry;
import org.wso2.carbon.identity.debug.framework.store.DebugSessionStore;
import org.wso2.carbon.identity.debug.framework.util.DebugFrameworkUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Routes incoming authentication requests to appropriate handlers.
 * Handles protocol-specific callback requests (/commonauth) via registered
 * DebugCallbackHandler implementations.
 */
public class DebugRequestCoordinator {

    private static final Log LOG = LogFactory.getLog(DebugRequestCoordinator.class);

    /**
     * Constructs a DebugRequestCoordinator instance.
     */
    public DebugRequestCoordinator() {

        // Stateless coordinator.
    }

    /**
     * Handles debug requests for any resource type using typed classes.
     * This is the preferred method with type safety.
     *
     * @param debugFrameworkRequest The debug request with resource information.
     * @return DebugFrameworkResponse containing debug result data.
     * @throws DebugFrameworkClientException If the request has validation errors.
     * @throws DebugFrameworkServerException If a server-side error occurs.
     */
    public DebugFrameworkResponse handleDebugRequest(DebugFrameworkRequest debugFrameworkRequest)
            throws DebugFrameworkClientException, DebugFrameworkServerException {

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Orchestrating debug request for resource type: " + debugFrameworkRequest.getResourceType());
            }

            // Pre-execute listeners.
            executePreListeners(debugFrameworkRequest);

            // Route by resource type.
            DebugResourceType type = DebugResourceType.fromString(debugFrameworkRequest.getResourceType());

            // Get the handler for this resource type from the registry.
            DebugResourceHandler handler = DebugHandlerRegistry.getInstance()
                    .getHandler(type.name().toLowerCase(Locale.ROOT));

            if (handler == null) {
                throw DebugFrameworkUtils.handleClientException(
                        ErrorMessages.ERROR_CODE_HANDLER_NOT_FOUND, debugFrameworkRequest.getResourceType());
            }

            // Delegate to handler with typed objects.
            DebugFrameworkResponse debugFrameworkResponse = handler.handleDebugRequest(debugFrameworkRequest);

            // Post-execute listeners.
            executePostListeners(debugFrameworkResponse, debugFrameworkRequest);

            return debugFrameworkResponse;

        } catch (DebugFrameworkClientException | DebugFrameworkServerException e) {
            throw e;
        } catch (DebugFrameworkException e) {
            if (e.getErrorCode() != null) {
                throw new DebugFrameworkServerException(e.getErrorCode(), e.getMessage(), e.getDescription(), e);
            }
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e);
        }
    }

    /**
     * Executes pre-execute listeners.
     *
     * @param debugFrameworkRequest The debug request.
     * @throws DebugFrameworkClientException If a listener aborts the request.
     * @throws DebugFrameworkException If a listener throws an exception.
     */
    private void executePreListeners(DebugFrameworkRequest debugFrameworkRequest) throws DebugFrameworkException {

        for (DebugExecutionListener listener : DebugFrameworkServiceDataHolder.getInstance()
                .getDebugExecutionListeners()) {
            if (listener.isEnabled() && !listener.doPreExecute(debugFrameworkRequest)) {
                throw DebugFrameworkUtils.handleClientException(
                        ErrorMessages.ERROR_CODE_LISTENER_ABORTED, "pre-execute");
            }
        }
    }

    /**
     * Executes post-execute listeners.
     *
     * @param debugFrameworkResponse The debug response.
     * @param debugFrameworkRequest The debug request.
     * @throws DebugFrameworkClientException If a listener aborts the request.
     * @throws DebugFrameworkException  If a listener throws an exception.
     */
    private void executePostListeners(DebugFrameworkResponse debugFrameworkResponse,
            DebugFrameworkRequest debugFrameworkRequest)
            throws DebugFrameworkException {

        for (DebugExecutionListener listener : DebugFrameworkServiceDataHolder.getInstance()
                .getDebugExecutionListeners()) {
            if (listener.isEnabled() && !listener.doPostExecute(debugFrameworkResponse, debugFrameworkRequest)) {
                throw DebugFrameworkUtils.handleClientException(
                        ErrorMessages.ERROR_CODE_LISTENER_ABORTED, "post-execute");
            }
        }
    }

    /**
     * Parses a JSON result string into a typed map.
     *
     * @param resultJson The JSON string to parse.
     * @return The parsed result as a typed map.
     * @throws DebugFrameworkServerException If parsing fails.
     */
    private Map<String, Object> parseResultJson(String resultJson, String debugId)
            throws DebugFrameworkServerException {

        try {
            return DebugFrameworkUtils.getObjectMapper()
                    .readValue(resultJson, DebugFrameworkUtils.getMapTypeReference());
        } catch (JsonProcessingException e) {
            LOG.error("Invalid debug result JSON for debug session: " + debugId + ". Cause: " + e.getMessage());
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e);
        }
    }

    /**
     * Retrieves the debug result for the given debug ID, invoking listeners.
     * This ensures that post-execution listeners (like cleanup) are executed.
     *
     * @param debugId The debug ID to retrieve.
     * @return The debug result as a {@link DebugFrameworkResponse}.
     * @throws DebugFrameworkClientException If the session is not found or validation fails.
     * @throws DebugFrameworkServerException If a server-side error occurs.
     */
    public DebugFrameworkResponse getDebugResult(String debugId)
            throws DebugFrameworkClientException, DebugFrameworkServerException {

        if (debugId == null) {
            throw DebugFrameworkUtils.handleClientException(ErrorMessages.ERROR_CODE_INVALID_REQUEST);
        }

        // Create a minimal request context for the listeners.
        DebugFrameworkRequest debugFrameworkRequest = new DebugFrameworkRequest();
        debugFrameworkRequest.addContextProperty(DebugFrameworkConstants.DEBUG_SESSION_DATA_KEY, debugId);
        debugFrameworkRequest.setResultRetrieval(true);

        try {
            // Pre-execute Listeners.
            executePreListeners(debugFrameworkRequest);

            // Execution: Get from store.
            String resultJson = DebugSessionStore.getInstance().getResult(debugId);

            if (resultJson == null) {
                throw DebugFrameworkUtils.handleClientException(
                        ErrorMessages.ERROR_CODE_RESULT_NOT_FOUND, debugId);
            }

            // Parse stored JSON into metadata map (protocol-specific data only).
            Map<String, Object> resultData = parseResultJson(resultJson, debugId);

            // Build a structured response: debugId and status at top level, metadata in data.
            DebugFrameworkResponse debugFrameworkResponse = new DebugFrameworkResponse();
            debugFrameworkResponse.setDebugId(debugId);
            debugFrameworkResponse.setStatus(DebugFrameworkConstants.DEBUG_STATUS_SUCCESS_COMPLETE);
            debugFrameworkResponse.setData(resultData);

            // Post-Execute Listeners.
            executePostListeners(debugFrameworkResponse, debugFrameworkRequest);

            return debugFrameworkResponse;

        } catch (DebugFrameworkClientException | DebugFrameworkServerException e) {
            throw e;
        } catch (DebugFrameworkException e) {
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e);
        }
    }

    /**
     * Handles callback requests from external debug systems.
     * Routes to the appropriate DebugCallbackHandler based on request characteristics.
     *
     * @param request The HTTP request containing callback parameters.
     * @param response The HTTP response for sending results.
     * @return true if callback was successfully handled, false if no handler matched or error occurred.
     */
    public boolean handleCallbackRequest(HttpServletRequest request, HttpServletResponse response) {

        DebugCallbackHandler handler = resolveCallbackHandler(request);
        if (handler == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No debug callback handler matched the request. Skipping debug handling.");
            }
            return false;
        }

        try {
            return handler.handleCallback(request, response);
        } catch (DebugFrameworkException e) {
            String debugId = request.getParameter(DebugFrameworkConstants.CALLBACK_STATE_PARAM);
            LOG.warn("Debug callback handler failed during callback processing for session: " + debugId
                    + ". Debug session may be orphaned.", e);
            try {
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Debug callback processing failed.");
                }
            } catch (IOException ioEx) {
                LOG.error("Error sending error response after debug callback failure.", ioEx);
            }
            return true;
        }
    }

    private DebugCallbackHandler resolveCallbackHandler(HttpServletRequest request) {

        List<DebugCallbackHandler> handlers = DebugProtocolRegistry.getInstance().getDebugCallbackHandlers();

        for (DebugCallbackHandler handler : handlers) {
            if (handler != null && handler.canHandle(request)) {
                return handler;
            }
        }

        return null;
    }
}
