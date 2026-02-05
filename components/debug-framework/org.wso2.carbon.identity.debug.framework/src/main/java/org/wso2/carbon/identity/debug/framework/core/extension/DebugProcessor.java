/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.core.extension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.debug.framework.core.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.core.event.DebugSessionEventContext;
import org.wso2.carbon.identity.debug.framework.core.event.DebugSessionEventManager;
import org.wso2.carbon.identity.debug.framework.core.event.DebugSessionLifecycleEvent;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base processor for debug flow callback handling.
 * Implements template method pattern for generic authentication/authorization
 * debug flow processing.
 * 
 * This framework is designed to be completely protocol-agnostic and
 * resource-agnostic.
 * It can be extended for debugging any authentication flow (OAuth2, SAML, OIDC,
 * custom protocols, etc.)
 * with any resource type (IdP, API, database, etc.).
 * 
 * Generic orchestration flow:
 * 1. validateCallback() - Validate callback parameters
 * 2. processAuthentication() - Process authentication/authorization
 * 3. extractDebugData() - Extract data for debugging
 * 4. validateDebugData() - Validate extracted data
 * 5. buildAndCacheDebugResult() - Build final debug result
 * 6. sendDebugResponse() - Send response to client
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
     * Fires lifecycle events:
     * - ON_CREATING: At the start of callback processing
     * - ON_COMPLETING: Before sending the final response
     * - ON_COMPLETION: After successful completion
     * - ON_ERROR: When an error occurs during processing
     *
     * @param request  HttpServletRequest containing callback parameters.
     * @param response HttpServletResponse for sending results.
     * @param context  AuthenticationContext for storing and retrieving debug state.
     * @throws IOException If processing fails.
     */
    public void processCallback(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) throws IOException {

        String sessionId = null;
        String state = null;
        String resourceId = null;

        try {
            // Extract protocol-specific parameters (OAuth2: state, SAML: RelayState, etc.)
            state = request.getParameter("state");
            if (state == null || state.trim().isEmpty()) {
                state = (String) context.getProperty("DEBUG_STATE");
            }

            // Extract session ID from state (state format: "debug-{sessionId}")
            sessionId = extractSessionIdFromState(state);

            // Extract resource identifier (IdP ID, API ID, etc.)
            resourceId = extractResourceId(context);

            // Fire ON_CREATING event at the start of processing
            fireLifecycleEvent(sessionId, DebugSessionLifecycleEvent.ON_CREATING, true, null);

            // Step 1: Validate callback (protocol/resource-specific).
            if (!validateCallback(request, context, response, state, resourceId)) {
                fireCompletionEvents(sessionId, false, "Callback validation failed");
                return;
            }

            // Step 2: Process authentication/authorization (protocol/resource-specific).
            if (!processAuthentication(request, context, response, state, resourceId)) {
                // Authentication failed - error details already cached by subclass.
                fireCompletionEvents(sessionId, false, "Authentication failed");
                sendDebugResponse(response, state, resourceId);
                return;
            }

            // Step 3: Extract debug data (protocol/resource-specific).
            Map<String, Object> debugData = extractDebugData(context);

            // Step 4: Validate extracted data (protocol/resource-specific).
            if (!validateDebugData(debugData, context, response, state, resourceId)) {
                fireCompletionEvents(sessionId, false, "Debug data validation failed");
                return;
            }

            // Step 5: Build and cache final debug result (protocol/resource-specific).
            buildAndCacheDebugResult(context, state);

            // Fire ON_COMPLETING event before sending response
            fireLifecycleEvent(sessionId, DebugSessionLifecycleEvent.ON_COMPLETING, true, null);

            // Step 6: Send response to client.
            sendDebugResponse(response, state, resourceId);

            // Fire ON_COMPLETION event after successful completion
            fireLifecycleEvent(sessionId, DebugSessionLifecycleEvent.ON_COMPLETION, true, null);

        } catch (Exception e) {
            LOG.error("Unexpected error processing debug callback.", e);
            handleUnexpectedError(e, context);

            // Fire ON_ERROR event
            fireLifecycleEvent(sessionId, DebugSessionLifecycleEvent.ON_ERROR, false, e.getMessage());

            // Try to extract state for error response
            if (state == null) {
                state = request.getParameter("state");
                if (state == null || state.trim().isEmpty()) {
                    state = (String) context.getProperty("DEBUG_STATE");
                }
            }
            if (resourceId == null) {
                resourceId = extractResourceId(context);
            }
            sendDebugResponse(response, state, resourceId);

            // Fire ON_COMPLETION event even on error (for cleanup)
            fireLifecycleEvent(sessionId, DebugSessionLifecycleEvent.ON_COMPLETION, false, e.getMessage());
        }
    }

    /**
     * Fires completion events (ON_COMPLETING followed by ON_COMPLETION).
     *
     * @param sessionId    The session ID.
     * @param successful   Whether the operation was successful.
     * @param errorMessage Error message if not successful.
     */
    private void fireCompletionEvents(String sessionId, boolean successful, String errorMessage) {

        fireLifecycleEvent(sessionId, DebugSessionLifecycleEvent.ON_COMPLETING, successful, errorMessage);
        fireLifecycleEvent(sessionId, DebugSessionLifecycleEvent.ON_COMPLETION, successful, errorMessage);
    }

    /**
     * Fires a lifecycle event to all registered listeners.
     *
     * @param sessionId    The session ID.
     * @param event        The lifecycle event type.
     * @param successful   Whether the operation was successful.
     * @param errorMessage Error message if applicable.
     */
    private void fireLifecycleEvent(String sessionId, DebugSessionLifecycleEvent event,
            boolean successful, String errorMessage) {

        if (sessionId == null || sessionId.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cannot fire lifecycle event: session ID is null or empty");
            }
            return;
        }

        try {
            DebugSessionEventContext.Builder builder = DebugSessionEventContext.builder()
                    .sessionId(sessionId)
                    .event(event)
                    .successful(successful);

            if (errorMessage != null) {
                builder.errorMessage(errorMessage);
            }

            DebugSessionEventManager.getInstance().fireEvent(builder.build());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Fired lifecycle event: " + event.getEventName() + " for session: " + sessionId);
            }
        } catch (Exception e) {
            LOG.error("Error firing lifecycle event: " + event.getEventName() + " for session: " + sessionId, e);
        }
    }

    /**
     * Extracts session ID from state parameter.
     * State format is expected to be "debug-{sessionId}".
     *
     * @param state The state parameter.
     * @return Session ID or the full state if not in expected format.
     */
    private String extractSessionIdFromState(String state) {

        if (state == null || state.isEmpty()) {
            return null;
        }

        // State format: "debug-{sessionId}"
        if (state.startsWith(DebugFrameworkConstants.DEBUG_PREFIX)) {
            return state; // Return the full state as session ID (includes prefix)
        }

        return state;
    }

    /**
     * Extracts resource identifier from context.
     * Subclasses can override to extract resource ID in protocol-specific way.
     * Default implementation tries common property names.
     *
     * @param context AuthenticationContext.
     * @return Resource identifier or empty string if not found.
     */
    protected String extractResourceId(AuthenticationContext context) {

        // Try common property names for resource ID
        Object resourceId = context.getProperty("RESOURCE_ID");
        if (resourceId == null) {
            resourceId = context.getProperty("IDP_RESOURCE_ID");
        }
        if (resourceId == null) {
            resourceId = context.getProperty("DEBUG_RESOURCE_ID");
        }
        return resourceId != null ? resourceId.toString() : "";
    }

    /**
     * Validates callback parameters.
     * Subclasses MUST implement with specific validation logic.
     * 
     * Examples:
     * - OAuth2: Validate authorization code, state parameter, error responses
     * - SAML: Validate SAMLResponse, RelayState
     * - Custom: Validate any protocol-specific parameters
     *
     * @param request  HttpServletRequest containing callback parameters.
     * @param context  AuthenticationContext for storing validation state.
     * @param response HttpServletResponse for error responses.
     * @return true if validation passes, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean validateCallback(HttpServletRequest request, AuthenticationContext context,
            HttpServletResponse response, String state, String resourceId) throws IOException;

    /**
     * Processes the authentication/authorization flow.
     * Subclasses MUST implement with specific authentication logic.
     * 
     * Examples:
     * - OAuth2: Exchange authorization code for tokens
     * - SAML: Validate and process SAML assertion
     * - Custom: Implement protocol-specific authentication
     *
     * @param request  HttpServletRequest containing authentication parameters.
     * @param context  AuthenticationContext for storing authentication state.
     * @param response HttpServletResponse for error responses.
     * @return true if authentication succeeds, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean processAuthentication(HttpServletRequest request, AuthenticationContext context,
            HttpServletResponse response, String state, String resourceId) throws IOException;

    /**
     * Extracts debug data from the authentication context.
     * Subclasses MUST implement with specific data extraction logic.
     * 
     * Examples:
     * - OAuth2: Extract claims from ID token and access token
     * - SAML: Extract attributes from SAML assertion
     * - Custom: Extract any protocol-specific debug data
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
     * - OAuth2: Validate required claims are present
     * - SAML: Validate required attributes are present
     * - Custom: Validate protocol-specific data requirements
     *
     * @param debugData Map of extracted debug data.
     * @param context   AuthenticationContext.
     * @param response  HttpServletResponse for error responses.
     * @return true if validation passes, false otherwise.
     * @throws IOException If response cannot be sent.
     */
    protected abstract boolean validateDebugData(Map<String, Object> debugData, AuthenticationContext context,
            HttpServletResponse response, String state, String resourceId) throws IOException;

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
     * - HTTP redirect to debug result page
     * - JSON response with debug data
     * - Custom response format
     *
     * @param response   HttpServletResponse for sending the response.
     * @param state
     * @param resourceId
     * @throws IOException If response cannot be sent.
     */
    protected abstract void sendDebugResponse(HttpServletResponse response, String state, String resourceId)
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
