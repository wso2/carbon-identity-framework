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

package org.wso2.carbon.identity.debug.framework.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.core.DebugContextProvider;
import org.wso2.carbon.identity.debug.framework.core.DebugExecutor;
import org.wso2.carbon.identity.debug.framework.core.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.core.DebugProtocolRouter;
import org.wso2.carbon.identity.debug.framework.exception.ContextResolutionException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;
import org.wso2.carbon.identity.debug.framework.exception.ExecutionException;
import org.wso2.carbon.identity.debug.framework.model.DebugResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for debug requests with IDP (Identity Provider) resource type.
 * Processes debug requests for Identity Provider resources by routing to protocol-specific handlers.
 */
public class IdpDebugResourceHandler implements DebugResourceHandler {

    private static final Log LOG = LogFactory.getLog(IdpDebugResourceHandler.class);

    /**
     * Constructs an IdpDebugResourceHandler.
     */
    public IdpDebugResourceHandler() {
        // No initialization required. This handler is stateless and does not maintain any
        // instance-level state. All context is passed through method parameters, and logging
        // is handled via the static LOG instance.
    }

    /**
     * Checks if this handler can process IDP resource type.
     *
     * @param resourceType The resource type to check.
     * @return true if resourceType is "IDP" or "IDENTITY_PROVIDER", false otherwise.
     */
    @Override
    public boolean canHandle(String resourceType) {

        if (resourceType == null) {
            return false;
        }
        String resourceTypeUpper = resourceType.toUpperCase().trim();
        return "IDP".equals(resourceTypeUpper) || "IDENTITY_PROVIDER".equals(resourceTypeUpper);
    }

    /**
     * Handles a debug request for IDP resource type.
     * Builds context and delegates to protocol-specific handlers via reflection.
     *
     * @param debugRequestContext Map containing:
     *        - resourceId: The IDP identifier (required).
     *        - resourceType: Always "IDP" or "IDENTITY_PROVIDER" (required).
     *        - properties: Optional key-value properties for the debug request (Map<String, String>).
     * @return Map containing debug result data.
     * @throws DebugFrameworkException If debug processing fails.
     */
    @Override
    public Map<String, Object> handleDebugRequest(Map<String, Object> debugRequestContext) 
            throws DebugFrameworkException {
        
        if (debugRequestContext == null) {
            throw new DebugFrameworkException("Debug request context is null");
        }

        try {
            String resourceId = validateAndExtractResourceId(debugRequestContext);
            String resourceType = (String) debugRequestContext.get("resourceType");
            
            debugLog("Processing IDP debug request for resourceId: " + resourceId);
            
            // Build and prepare context for protocol-specific handler
            Map<String, Object> context = prepareDebugContext(debugRequestContext, resourceId, resourceType);

            // Resolve and execute through protocol-specific handlers
            Map<String, Object> debugContext = resolveDebugContext(resourceId, context);
            Map<String, Object> authUrlResult = executeDebugRequest(resourceId, debugContext);

            // Build and return result with metadata
            addResultMetadata(authUrlResult, resourceId, resourceType);
            
            debugLog("Successfully processed IDP debug request for resourceId: " + resourceId);
            return authUrlResult;

        } catch (DebugFrameworkException e) {
            throw e;
        } catch (Exception e) {
            throw new DebugFrameworkException("Error handling IDP debug request: " + e.getMessage(), e);
        }
    }

    /**
     * Validates and extracts the resource ID from the debug request context.
     *
     * @param debugRequestContext The debug request context map.
     * @return The validated resource ID.
     * @throws DebugFrameworkException If resource ID is null or empty.
     */
    private String validateAndExtractResourceId(Map<String, Object> debugRequestContext) 
            throws DebugFrameworkException {
        
        String resourceId = (String) debugRequestContext.get("resourceId");
        if (resourceId == null || resourceId.trim().isEmpty()) {
            throw new DebugFrameworkException("Resource ID is required for IDP debug request");
        }
        return resourceId;
    }

    /**
     * Prepares the debug context with IDP-specific fields and properties.
     *
     * @param debugRequestContext The original debug request context.
     * @param resourceId The IDP resource ID.
     * @param resourceType The resource type.
     * @return A prepared context map with IDP-specific fields.
     */
    private Map<String, Object> prepareDebugContext(Map<String, Object> debugRequestContext, 
                                                     String resourceId, String resourceType) {
        
        Map<String, Object> context = new HashMap<>(debugRequestContext);
        
        // Add IDP-specific fields expected by DebugRequestCoordinator
        if (!context.containsKey("idpId")) {
            context.put("idpId", resourceId);
        }
        if (!context.containsKey("idpName")) {
            context.put("idpName", resourceId);
        }
        
        // Extract and merge properties if they exist
        @SuppressWarnings("unchecked")
        Map<String, String> properties = (Map<String, String>) debugRequestContext.get("properties");
        if (properties != null && !properties.isEmpty()) {
            context.putAll(properties);
            debugLog("Added " + properties.size() + " properties to IDP debug context");
        }

        // Mark as resource debug request type
        context.put(DebugFrameworkConstants.DEBUG_RESOURCE_TYPE, "RESOURCE_DEBUG_REQUEST");
        context.put(DebugFrameworkConstants.DEBUG_RESOURCE_ID, resourceId);
        context.put(DebugFrameworkConstants.DEBUG_RESOURCE_TYPE, resourceType);

        return context;
    }

    /**
     * Resolves the debug context using the appropriate context provider.
     *
     * @param resourceId The IDP resource ID.
     * @param context The prepared debug context (unused, kept for compatibility).
     * @return The resolved debug context object.
     * @throws DebugFrameworkException If context provider is not found or resolution fails.
     */
    private Map<String, Object> resolveDebugContext(String resourceId, Map<String, Object> context) 
            throws DebugFrameworkException {
        
        debugLog("Attempting to get context provider for IDP: " + resourceId);
        debugLog("Registered protocol types: " + DebugProtocolRouter.getAllRegisteredProtocolTypes());
        DebugContextProvider contextProvider = DebugProtocolRouter.getContextProviderForResource(resourceId);
        if (contextProvider == null) {
            LOG.error("No suitable DebugContextProvider found for IDP: " + resourceId 
                    + ". Available protocols: " 
                    + DebugProtocolRouter.getAllRegisteredProtocolTypes());
            throw new DebugFrameworkException("No suitable DebugContextProvider found for IDP: " + resourceId);
        }

        debugLog("Successfully loaded context provider for IDP: " + resourceId);

        try {
            Map<String, Object> debugContext = contextProvider.resolveContext(resourceId, "RESOURCE_DEBUG_REQUEST");
            if (debugContext == null) {
                throw new DebugFrameworkException("Failed to resolve debug context for IDP: " + resourceId);
            }

            debugLog("Successfully resolved debug context for IDP: " + resourceId);
            return debugContext;
        } catch (ContextResolutionException e) {
            throw new DebugFrameworkException("Error resolving context for IDP: " + resourceId + ": " 
                    + e.getMessage(), e);
        }
    }

    /**
     * Executes the debug request using the appropriate executor.
     *
     * @param resourceId The IDP resource ID.
     * @param debugContext The resolved debug context.
     * @return The execution result as a map.
     * @throws DebugFrameworkException If executor is not found or execution fails.
     */
    private Map<String, Object> executeDebugRequest(String resourceId, Map<String, Object> debugContext) 
            throws DebugFrameworkException {
        
        DebugExecutor executor = DebugProtocolRouter.getExecutorForResource(resourceId);
        if (executor == null) {
            throw new DebugFrameworkException("No suitable DebugExecutor found for IDP: " + resourceId);
        }

        debugLog("Successfully loaded executor for IDP: " + resourceId);

        try {
            DebugResult authUrlResult = executor.execute(debugContext);
            if (authUrlResult == null) {
                throw new DebugFrameworkException("Authorization URL generation failed for IDP: " + resourceId);
            }

            // Extract result data from DebugResult
            Map<String, Object> resultMap = new HashMap<>();
            if (authUrlResult.getResultData() != null) {
                resultMap.putAll(authUrlResult.getResultData());
            }
            if (authUrlResult.getMetadata() != null) {
                resultMap.putAll(authUrlResult.getMetadata());
            }
            return resultMap;
        } catch (ExecutionException e) {
            throw new DebugFrameworkException("Error executing debug flow for IDP: " + resourceId + ": " 
                    + e.getMessage(), e);
        }
    }

    /**
     * Adds metadata to the result map.
     *
     * @param result The result map to augment with metadata.
     * @param resourceId The IDP resource ID.
     * @param resourceType The resource type.
     */
    private void addResultMetadata(Map<String, Object> result, String resourceId, String resourceType) {

        result.put(DebugFrameworkConstants.DEBUG_TIMESTAMP, System.currentTimeMillis());
        result.put(DebugFrameworkConstants.DEBUG_RESOURCE_ID, resourceId);
        result.put(DebugFrameworkConstants.DEBUG_RESOURCE_TYPE, resourceType);
        result.put(DebugFrameworkConstants.DEBUG_REQUEST_TYPE, "RESOURCE_DEBUG_REQUEST");
        result.put(DebugFrameworkConstants.DEBUG_STATUS, "SUCCESS");
    }

    /**
     * Logs a debug message if debug logging is enabled.
     *
     * @param message The message to log.
     */
    private void debugLog(String message) {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
    }

    /**
     * Returns the name of this handler.
     *
     * @return "IdpDebugResourceHandler".
     */
    @Override
    public String getName() {

        return "IdpDebugResourceHandler";
    }
}
