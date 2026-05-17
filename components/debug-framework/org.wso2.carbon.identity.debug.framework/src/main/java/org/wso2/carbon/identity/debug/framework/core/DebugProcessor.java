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

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;

/**
 * Base processor for handling debug flow callbacks.
 */
public abstract class DebugProcessor {

    private static final Log LOG = LogFactory.getLog(DebugProcessor.class);

    /**
     * Processes the debug flow callback from an external system.
     *
     * @param request      Incoming HTTP request containing callback parameters.
     * @param response     HTTP response for sending results.
     * @param debugContext Debug context for storing and retrieving debug state.
     * @throws DebugFrameworkException If an error occurs while processing the callback.
     */
    public void processCallback(HttpServletRequest request, HttpServletResponse response,
            DebugContext debugContext) throws DebugFrameworkException {

        String state = null;
        String resourceIdentifier = null;

        try {
            state = request.getParameter(DebugFrameworkConstants.CALLBACK_STATE_PARAM);
            if (StringUtils.isBlank(state)) {
                state = (String) debugContext.getProperty("DEBUG_STATE");
            }

            resourceIdentifier = extractResourceIdentifier(debugContext);

            if (!validateCallback(request, debugContext, response, state, resourceIdentifier)) {
                sendDebugResponse(response, state, resourceIdentifier);
                return;
            }

            if (!processAuthentication(request, debugContext, response, state, resourceIdentifier)) {
                sendDebugResponse(response, state, resourceIdentifier);
                return;
            }

            Map<String, Object> debugData = extractDebugData(debugContext);

            if (!validateDebugData(debugData, debugContext, response, state, resourceIdentifier)) {
                sendDebugResponse(response, state, resourceIdentifier);
                return;
            }

            buildAndCacheDebugResult(debugContext, state);

            sendDebugResponse(response, state, resourceIdentifier);

        } catch (IOException e) {
            LOG.info("Unexpected error processing debug callback.", e);
            handleUnexpectedError(e, debugContext);

            // Try to extract state for error response.
            if (StringUtils.isBlank(state)) {
                state = request.getParameter(DebugFrameworkConstants.CALLBACK_STATE_PARAM);
                if (StringUtils.isBlank(state)) {
                    state = (String) debugContext.getProperty("DEBUG_STATE");
                }
                if (StringUtils.isBlank(state)) {
                    state = DebugFrameworkConstants.UNKNOWN_DEBUG_STATE;
                }
            }

            try {
                if (!response.isCommitted()) {
                    sendDebugResponse(response, state, resourceIdentifier);
                }
            } catch (IOException innerEx) {
                LOG.info("Error sending debug response after initial failure", innerEx);
                e.addSuppressed(innerEx);
            }
            throw new DebugFrameworkException(
                    DebugFrameworkConstants.ErrorMessages.ERROR_CODE_SERVER_ERROR.getCode(),
                    DebugFrameworkConstants.ErrorMessages.ERROR_CODE_SERVER_ERROR.getMessage(),
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Extracts a resource identifier from the debug context.
     *
     * @param debugContext Debug context.
     * @return Resource identifier, or empty string if not available.
     */
    protected String extractResourceIdentifier(DebugContext debugContext) {
        return "";
    }

    /**
     * Validates the callback parameters.
     *
     * @param request            Incoming HTTP request.
     * @param debugContext       Debug context.
     * @param response           HTTP response.
     * @param state              State parameter from the callback.
     * @param resourceIdentifier Resource identifier.
     * @return true if validation passes, false otherwise.
     * @throws IOException If an I/O error occurs while sending an error response.
     */
    protected abstract boolean validateCallback(HttpServletRequest request, DebugContext debugContext,
            HttpServletResponse response, String state, String resourceIdentifier) throws IOException;

    /**
     * Processes the authentication flow.
     *
     * @param request            Incoming HTTP request.
     * @param debugContext       Debug context.
     * @param response           HTTP response.
     * @param state              State parameter from the callback.
     * @param resourceIdentifier Resource identifier.
     * @return true if authentication succeeds, false otherwise.
     * @throws IOException If an I/O error occurs while sending an error response.
     */
    protected abstract boolean processAuthentication(HttpServletRequest request, DebugContext debugContext,
            HttpServletResponse response, String state, String resourceIdentifier) throws IOException;

    /**
     * Extracts debug data from the debug context.
     *
     * @param debugContext Debug context containing authentication results.
     * @return Extracted debug data as key-value pairs.
     */
    protected abstract Map<String, Object> extractDebugData(DebugContext debugContext);

    /**
     * Validates the extracted debug data.
     *
     * @param debugData          Extracted debug data to validate.
     * @param debugContext       Debug context.
     * @param response           HTTP response.
     * @param state              State parameter from the callback.
     * @param resourceIdentifier Resource identifier.
     * @return true if validation passes, false otherwise.
     * @throws IOException If an I/O error occurs while sending an error response.
     */
    protected abstract boolean validateDebugData(Map<String, Object> debugData, DebugContext debugContext,
            HttpServletResponse response, String state, String resourceIdentifier) throws IOException;

    /**
     * Builds and caches the final debug result.
     *
     * @param debugContext Debug context containing all collected debug information.
     * @param state        State parameter used for debug identification.
     */
    protected abstract void buildAndCacheDebugResult(DebugContext debugContext, String state);

    /**
     * Sends the debug response to the client.
     *
     * @param response           HTTP response.
     * @param state              State parameter used for debug identification.
     * @param resourceIdentifier Resource identifier.
     * @throws IOException If an I/O error occurs while sending the response.
     */
    protected abstract void sendDebugResponse(HttpServletResponse response, String state, String resourceIdentifier)
            throws IOException;

    /**
     * Handles unexpected errors during callback processing.
     *
     * @param e            The exception that occurred.
     * @param debugContext Debug context for storing error information.
     */
    protected void handleUnexpectedError(Exception e, DebugContext debugContext) {

        debugContext.setProperty(DebugFrameworkConstants.DEBUG_PROTOCOL_CODE, null);
        debugContext.setProperty(DebugFrameworkConstants.DEBUG_PROTOCOL_STATE, null);
        debugContext.setProperty(DebugFrameworkConstants.DEBUG_AUTH_ERROR, "Unexpected error: " + e.getMessage());
        debugContext.setProperty(DebugFrameworkConstants.DEBUG_AUTH_SUCCESS, DebugFrameworkConstants.FALSE_VALUE);
    }
}
