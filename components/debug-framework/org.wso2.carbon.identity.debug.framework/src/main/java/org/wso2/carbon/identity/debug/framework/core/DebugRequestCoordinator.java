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
import org.wso2.carbon.identity.debug.framework.model.DebugFrameworkResponseBuilder;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;
import org.wso2.carbon.identity.debug.framework.registry.DebugTypeRegistry;
import org.wso2.carbon.identity.debug.framework.store.DebugSessionStore;
import org.wso2.carbon.identity.debug.framework.util.DebugFrameworkUtils;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Routes debug API requests to the appropriate resource handler and manages post-execution listeners.
 */
public class DebugRequestCoordinator {

    private static final Log LOG = LogFactory.getLog(DebugRequestCoordinator.class);

    public DebugRequestCoordinator() {

    }

    /**
     * Handles debug requests for any resource type.
     *
     * @param debugFrameworkRequest The debug request with resource information.
     * @return DebugFrameworkResponse containing debug result data.
     * @throws DebugFrameworkClientException If the request has validation errors.
     * @throws DebugFrameworkServerException If a server-side error occurs.
     */
    public DebugFrameworkResponse handleDebugRequest(DebugFrameworkRequest debugFrameworkRequest)
            throws DebugFrameworkClientException, DebugFrameworkServerException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Orchestrating debug request for resource type: " + debugFrameworkRequest.getResourceType());
        }

        String resourceType = debugFrameworkRequest.getResourceType();
        DebugResourceHandler handler = DebugTypeRegistry.getInstance().getResourceHandler(resourceType);
        if (handler == null) {
            throw DebugFrameworkUtils.handleClientException(ErrorMessages.ERROR_CODE_HANDLER_NOT_FOUND, resourceType);
        }

        DebugFrameworkResponse debugFrameworkResponse = handler.handleDebugRequest(debugFrameworkRequest);
        executePostListeners(debugFrameworkResponse, debugFrameworkRequest);
        return debugFrameworkResponse;
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

        DebugFrameworkRequest debugFrameworkRequest = new DebugFrameworkRequest();
        debugFrameworkRequest.addContextProperty(DebugFrameworkConstants.DEBUG_SESSION_DATA_KEY, debugId);
        debugFrameworkRequest.setResultRetrieval(true);

        DebugSessionData sessionData = DebugSessionStore.getInstance().getSession(debugId);
        if (sessionData == null) {
            throw DebugFrameworkUtils.handleClientException(ErrorMessages.ERROR_CODE_RESULT_NOT_FOUND, debugId);
        }

        String resultJson = sessionData.getResultJson();
        if (resultJson == null) {
            // Session exists but the IDP callback has not yet written the result — still in progress.
            return new DebugFrameworkResponseBuilder()
                    .debugId(debugId)
                    .status(DebugFrameworkConstants.DEBUG_STATUS_SUCCESS_INCOMPLETE)
                    .message("Debug session is still in progress. Please retry.")
                    .build();
        }

        Map<String, Object> resultData = parseResultJson(resultJson);

        DebugFrameworkResponse debugFrameworkResponse = new DebugFrameworkResponseBuilder()
                .debugId(debugId)
                .status(DebugFrameworkConstants.DEBUG_STATUS_SUCCESS_COMPLETE)
                .data(resultData)
                .build();

        executePostListeners(debugFrameworkResponse, debugFrameworkRequest);
        return debugFrameworkResponse;
    }

    /**
     * Handles a debug callback request from /commonauth.
     * Resolves the IdP type from the session, routes to the registered handler directly.
     *
     * @param request  The HTTP request containing callback parameters.
     * @param response The HTTP response for sending results.
     * @return true if the callback was handled; false if no handler matched.
     */
    public boolean handleCallbackRequest(HttpServletRequest request, HttpServletResponse response) {

        String state = request.getParameter(DebugFrameworkConstants.CALLBACK_STATE_PARAM);
        Map<String, Object> sessionData = loadSessionData(state);
        String debugType = extractDebugType(sessionData);
        DebugCallbackHandler handler = DebugTypeRegistry.getInstance().getCallbackHandler(debugType);

        try {
            return handler.handleCallback(request, response, sessionData);
        } catch (DebugFrameworkException e) {
            if (!response.isCommitted()) {
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Debug callback processing failed.");
                } catch (IOException ioEx) {
                    LOG.warn("Error sending error response after debug callback failure.", ioEx);
                }
            }
            return true;
        }
    }

    private Map<String, Object> loadSessionData(String state) {

        try {
            Map<String, Object> sessionData = DebugSessionStore.getInstance().get(state);
            return (sessionData == null || sessionData.isEmpty()) ? null : sessionData;
        } catch (DebugFrameworkException e) {
            return null;
        }
    }

    private String extractDebugType(Map<String, Object> sessionData) {

        Object debugType = sessionData.get(DebugFrameworkConstants.CONTEXT_DEBUG_TYPE_KEY);
        return debugType != null ? debugType.toString() : null;
    }

    private void executePostListeners(DebugFrameworkResponse debugFrameworkResponse,
            DebugFrameworkRequest debugFrameworkRequest)
            throws DebugFrameworkClientException, DebugFrameworkServerException {

        for (DebugExecutionListener listener : DebugFrameworkServiceDataHolder.getInstance()
                .getDebugExecutionListeners()) {
            if (!listener.isEnabled()) {
                continue;
            }
            try {
                if (!listener.doPostExecute(debugFrameworkResponse, debugFrameworkRequest)) {
                    throw DebugFrameworkUtils.handleClientException(
                            ErrorMessages.ERROR_CODE_LISTENER_ABORTED, "post-execute");
                }
            } catch (DebugFrameworkClientException | DebugFrameworkServerException e) {
                throw e;
            } catch (DebugFrameworkException e) {
                throw new DebugFrameworkServerException(e.getErrorCode(), e.getMessage(), e.getDescription(), e);
            }
        }
    }

    private Map<String, Object> parseResultJson(String resultJson) throws DebugFrameworkServerException {

        try {
            return DebugFrameworkUtils.getObjectMapper()
                    .readValue(resultJson, DebugFrameworkUtils.getMapTypeReference());
        } catch (JsonProcessingException e) {
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e);
        }
    }

}

