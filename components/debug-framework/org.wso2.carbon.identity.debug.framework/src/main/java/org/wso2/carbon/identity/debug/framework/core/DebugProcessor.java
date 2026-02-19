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
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base processor for debug flow callback handling.
 * Implements template method pattern for generic authentication/authorization
 * debug flow processing.
 *
 * Subclasses MUST implement all abstract methods with their specific logic.
 * Subclasses should NOT make assumptions about protocols or resource types.
 */

public abstract class DebugProcessor {

    private static final Log LOG = LogFactory.getLog(DebugProcessor.class);

    /**
     * Processes the debug flow callback from external system.
     * Template method that orchestrates: validation -> authentication -> data
     * extraction -> response.
     * All specific implementations are delegated to subclass methods.
     *
    * @param request  HttpServletRequest containing callback parameters.
     * @param response HttpServletResponse for sending results.
     * @param context  AuthenticationContext for storing and retrieving debug state.
     * @throws IOException If processing fails.
     */
    public void processCallback(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws IOException {

        String state = null;
        String connectionId = null;

        try {
            // Extract protocol-specific parameters.
            state = request.getParameter("state");
            if (state == null || state.trim().isEmpty()) {
                state = (String) context.getProperty("DEBUG_STATE");
            }

            // Extract resource identifier.
            connectionId = extractConnectionId(context);

            // Step 1: Validate callback (protocol/resource-specific).
            if (!validateCallback(request, context, response, state, connectionId)) {
                return;
            }

            // Step 2: Process authentication/authorization (protocol/resource-specific).
            if (!processAuthentication(request, context, response, state, connectionId)) {
                sendDebugResponse(response, state, connectionId);
                return;
            }

            // Step 3: Extract debug data (protocol/resource-specific).
            Map<String, Object> debugData = extractDebugData(context);

            // Step 4: Validate extracted data (protocol/resource-specific).
            if (!validateDebugData(debugData, context, response, state, connectionId)) {
                return;
            }

            // Step 5: Build and cache final debug result (protocol/resource-specific).
            buildAndCacheDebugResult(context, state);

            // Step 6: Send response to client.
            sendDebugResponse(response, state, connectionId);

        } catch (Exception e) {
            LOG.error("Unexpected error processing debug callback.", e);
            handleUnexpectedError(e, context);

            // Try to extract state for error response.
            if (state == null) {
                state = request.getParameter("state");
                if (state == null || state.trim().isEmpty()) {
                    state = (String) context.getProperty("DEBUG_STATE");
                }
            }
            if (connectionId == null) {
                connectionId = extractConnectionId(context);
            }
            sendDebugResponse(response, state, connectionId);
        }
    }

    /**
     * Extracts resource identifier from context.
     * Subclasses can override to extract resource ID in protocol-specific way.
     * Default implementation tries common property names.
     *
     * @param context AuthenticationContext.
     * @return Resource identifier or empty string if not found.
     */
    protected String extractConnectionId(AuthenticationContext context) {

        // Try common property names for resource ID.
        Object connectionId = context.getProperty("RESOURCE_ID");
        if (connectionId == null) {
            connectionId = context.getProperty("IDP_RESOURCE_ID");
        }
        if (connectionId == null) {
            connectionId = context.getProperty("DEBUG_RESOURCE_ID");
        }
        return connectionId != null ? connectionId.toString() : "";
    }

    /**
     * Validates callback parameters.
     * Subclasses MUST implement with specific validation logic.
     * 
     * Examples:
     * - OAuth2: Validate authorization code, state parameter, error responses.
     * - SAML: Validate SAMLResponse, RelayState.
     * - Custom: Validate any protocol-specific parameters.
     *
     * @param request  HttpServletRequest containing callback parameters.
     * @param context  AuthenticationContext for storing validation state.
     * @param response HttpServletResponse for error responses.
     * @return true if validation passes, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean validateCallback(HttpServletRequest request, AuthenticationContext context,
            HttpServletResponse response, String state, String connectionId) throws IOException;

    /**
     * Processes the authentication/authorization flow.
     * Subclasses MUST implement with specific authentication logic.
     * 
     * Examples:
     * - OAuth2: Exchange authorization code for tokens.
     * - SAML: Validate and process SAML assertion.
     * - Custom: Implement protocol-specific authentication.
     *
     * @param request  HttpServletRequest containing authentication parameters.
     * @param context  AuthenticationContext for storing authentication state.
     * @param response HttpServletResponse for error responses.
     * @return true if authentication succeeds, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean processAuthentication(HttpServletRequest request, AuthenticationContext context,
            HttpServletResponse response, String state, String connectionId) throws IOException;

    /**
     * Extracts debug data from the authentication context.
     * Subclasses MUST implement with specific data extraction logic.
     * 
     * Examples:
     * - OAuth2: Extract claims from ID token and access token.
     * - SAML: Extract attributes from SAML assertion.
     * - Custom: Extract any protocol-specific debug data.
     *
     * @param context AuthenticationContext containing authentication results.
     * @return Map of extracted debug data (key-value pairs).
     */
    protected abstract Map<String, Object> extractDebugData(AuthenticationContext context);

    /**
     * Validates the extracted debug data.
     * Subclasses MUST implement with specific validation logic.
     * 
     * Examples:
     * - OAuth2: Validate required claims are present.
     * - SAML: Validate required attributes are present.
     * - Custom: Validate protocol-specific data requirements.
     *
     * @param debugData Map of extracted debug data.
     * @param context   AuthenticationContext.
     * @param response  HttpServletResponse for error responses.
     * @return true if validation passes, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean validateDebugData(Map<String, Object> debugData, AuthenticationContext context,
            HttpServletResponse response, String state, String connectionId) throws IOException;

    /**
     * Builds and caches the final debug result.
     * Subclasses MUST implement to create and cache appropriate debug result.
     * Result should be stored in a format that can be retrieved later.
     *
     * @param context AuthenticationContext containing all debug information and
     *                session identifiers.
     */
    protected abstract void buildAndCacheDebugResult(AuthenticationContext context, String state);

    /**
     * Sends the debug response to the client.
     * Subclasses MUST implement to send appropriate HTTP response.
     * 
     * Examples:
     * - HTTP redirect to debug result page.
     * - JSON response with debug data.
     * - Custom response format.
     *
     * @param response   HttpServletResponse for sending the response.
     * @param state      The state parameter for session identification.
     * @param connectionId The connection identifier.
     * @throws IOException If response cannot be sent.
     */
    protected abstract void sendDebugResponse(HttpServletResponse response, String state, String connectionId)
            throws IOException;

    /**
     * Handles unexpected errors during callback processing.
     * Default implementation logs error and sets error properties in context.
     * Subclasses MAY override to add custom error handling.
     *
     * @param e       The exception that occurred.
     * @param context AuthenticationContext for storing error information.
     */
    protected void handleUnexpectedError(Exception e, AuthenticationContext context) {

        context.setProperty(DebugFrameworkConstants.DEBUG_AUTH_ERROR, "Unexpected error: " + e.getMessage());
        context.setProperty(DebugFrameworkConstants.DEBUG_AUTH_SUCCESS, DebugFrameworkConstants.FALSE);
        if (e instanceof java.lang.reflect.InvocationTargetException
                && e.getCause() != null && LOG.isDebugEnabled()) {
            LOG.debug("InvocationTargetException cause:", e.getCause());
        }
    }
}
