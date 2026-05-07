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
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base processor for debug flow callback handling.
 * Implements template method pattern for generic authentication/authorization
 * debug flow processing.
 * Uses DebugContext (protocol-agnostic) instead of WSO2's AuthenticationContext,
 * ensuring the debug framework remains independent of specific authentication implementations.
 * 
 * ARCHITECTURAL NOTE:
 * The debug framework was previously coupled to WSO2's AuthenticationContext, which violated
 * separation of concerns. This refactoring decouples the framework by using DebugContext,
 * a lightweight, protocol-agnostic property container. This enables:
 * - Protocol-independent debug operations (can be reused outside WSO2 auth flows)
 * - Easier testing (no dependency on framework internals)
 * - Flexibility for future integrations (non-WSO2 systems, custom flows, etc.)
 *
 * Subclasses MUST implement all abstract methods with their specific logic.
 * Subclasses should NOT make assumptions about protocols or resource types.
 */

public abstract class DebugProcessor {

    private static final Log LOG = LogFactory.getLog(DebugProcessor.class);

    /**
     * Processes the debug flow callback from external system using DebugContext.
     * This is the primary method signature (protocol-agnostic).
     * Template method that orchestrates: validation -> authentication -> data
     * extraction -> response.
     *
     * @param request  HttpServletRequest containing callback parameters.
     * @param response HttpServletResponse for sending results.
     * @param debugContext DebugContext (protocol-agnostic) for storing and retrieving debug state.
     * @throws IOException If processing fails.
     */
    public void processCallback(HttpServletRequest request, HttpServletResponse response,
            DebugContext debugContext) throws IOException {

        String state = null;
        String resourceIdentifier = null;

        try {
            // Extract protocol-specific parameters.
            state = request.getParameter(DebugFrameworkConstants.CALLBACK_STATE_PARAM);
            if (state == null || state.trim().isEmpty()) {
                state = (String) debugContext.getProperty("DEBUG_STATE");
            }

            // Extract resource identifier.
            resourceIdentifier = extractResourceIdentifier(debugContext);

            // Step 1: Validate callback (protocol/resource-specific).
            if (!validateCallback(request, debugContext, response, state, resourceIdentifier)) {
                return;
            }

            // Step 2: Process authentication/authorization (protocol/resource-specific).
            if (!processAuthentication(request, debugContext, response, state, resourceIdentifier)) {
                sendDebugResponse(response, state, resourceIdentifier);
                return;
            }

            // Step 3: Extract debug data (protocol/resource-specific).
            Map<String, Object> debugData = extractDebugData(debugContext);

            // Step 4: Validate extracted data (protocol/resource-specific).
            if (!validateDebugData(debugData, debugContext, response, state, resourceIdentifier)) {
                sendDebugResponse(response, state, resourceIdentifier);
                return;
            }

            // Step 5: Build and cache final debug result (protocol/resource-specific).
            buildAndCacheDebugResult(debugContext, state);

            // Step 6: Send response to client.
            sendDebugResponse(response, state, resourceIdentifier);

        } catch (IOException e) {
            LOG.error("Unexpected error processing debug callback. Cause: " + e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stack trace for debug callback processing failure: ", e);
            }
            handleUnexpectedError(e, debugContext);

            // Try to extract state for error response.
            if (state == null) {
                state = request.getParameter(DebugFrameworkConstants.CALLBACK_STATE_PARAM);
                if (state == null || state.trim().isEmpty()) {
                    state = (String) debugContext.getProperty("DEBUG_STATE");
                }
            }
            sendDebugResponse(response, state, resourceIdentifier);
        }
    }

    /**
     * Extracts a resource identifier from the debug context.
     * Default implementation returns empty string.
     * Subclasses in resource-specific layers override this with
     * their own identifier concept.
     *
     * @param debugContext DebugContext.
     * @return Resource identifier or empty string if not found.
     */
    protected String extractResourceIdentifier(DebugContext debugContext) {
        return "";
    }

    /**
     * Validates callback parameters.
     * Subclasses MUST implement with specific validation logic.
     *
     * @param request  HttpServletRequest containing callback parameters.
     * @param debugContext DebugContext (protocol-agnostic) for storing validation state.
     * @param response HttpServletResponse for error responses.
     * @param state State parameter.
     * @param resourceIdentifier Generic resource identifier.
     * @return true if validation passes, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean validateCallback(HttpServletRequest request, DebugContext debugContext,
            HttpServletResponse response, String state, String resourceIdentifier) throws IOException;

    /**
     * Processes the authentication/authorization flow.
     * Subclasses MUST implement with specific authentication logic.
     *
     * @param request  HttpServletRequest containing authentication parameters.
     * @param debugContext DebugContext (protocol-agnostic) for storing authentication state.
     * @param response HttpServletResponse for error responses.
     * @param state State parameter.
     * @param resourceIdentifier Generic resource identifier.
     * @return true if authentication succeeds, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean processAuthentication(HttpServletRequest request, DebugContext debugContext,
            HttpServletResponse response, String state, String resourceIdentifier) throws IOException;

    /**
     * Extracts debug data from the debug context.
     * Subclasses MUST implement with specific data extraction logic.
     *
     * @param debugContext DebugContext containing authentication results.
     * @return Map of extracted debug data (key-value pairs).
     */
    protected abstract Map<String, Object> extractDebugData(DebugContext debugContext);

    /**
     * Validates the extracted debug data.
     * Subclasses MUST implement with specific validation logic.
     *
     * @param debugData Map of extracted debug data.
     * @param debugContext DebugContext (protocol-agnostic).
     * @param response HttpServletResponse for error responses.
     * @param state State parameter.
     * @param resourceIdentifier Generic resource identifier.
     * @return true if validation passes, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean validateDebugData(Map<String, Object> debugData, DebugContext debugContext,
            HttpServletResponse response, String state, String resourceIdentifier) throws IOException;

    /**
     * Builds and caches the final debug result.
     * Subclasses MUST implement to create and cache appropriate debug result.
     * Result should be stored in a format that can be retrieved later.
     *
     * @param debugContext DebugContext containing all debug information and
     *                     debug identifiers.
     * @param state        The state parameter for debug identification.
     */
    protected abstract void buildAndCacheDebugResult(DebugContext debugContext, String state);

    /**
     * Sends the debug response to the client.
     * Subclasses MUST implement to send appropriate HTTP response.
     *
     * @param response           HttpServletResponse for sending the response.
     * @param state              The state parameter for debug identification.
     * @param resourceIdentifier The resource identifier.
     * @throws IOException If response cannot be sent.
     */
    protected abstract void sendDebugResponse(HttpServletResponse response, String state, String resourceIdentifier)
            throws IOException;

    /**
     * Handles unexpected errors during callback processing.
     * Default implementation logs error and sets error properties in context.
     * Subclasses MAY override to add custom error handling.
     *
     * @param e           The exception that occurred.
     * @param debugContext DebugContext (protocol-agnostic) for storing error information.
     */
    protected void handleUnexpectedError(Exception e, DebugContext debugContext) {

        debugContext.setProperty(DebugFrameworkConstants.DEBUG_PROTOCOL_CODE, null);
        debugContext.setProperty(DebugFrameworkConstants.DEBUG_PROTOCOL_STATE, null);
        debugContext.setProperty(DebugFrameworkConstants.DEBUG_AUTH_ERROR, "Unexpected error: " + e.getMessage());
        debugContext.setProperty(DebugFrameworkConstants.DEBUG_AUTH_SUCCESS, DebugFrameworkConstants.FALSE_VALUE);
    }
}
