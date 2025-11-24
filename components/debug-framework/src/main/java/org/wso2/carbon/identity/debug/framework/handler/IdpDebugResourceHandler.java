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
import org.wso2.carbon.identity.debug.framework.core.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.core.DebugProtocolRouter;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;

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
            Object debugContext = resolveDebugContext(resourceId, context);
            Object authUrlResult = executeDebugRequest(resourceId, debugContext);

            // Build and return result with metadata
            Map<String, Object> result = extractResultAsMap(authUrlResult);
            addResultMetadata(result, resourceId, resourceType);
            
            debugLog("Successfully processed IDP debug request for resourceId: " + resourceId);
            return result;

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
     * @param context The prepared debug context.
     * @return The resolved debug context object.
     * @throws DebugFrameworkException If context provider is not found or resolution fails.
     * @throws DebugFrameworkServerException If context resolution fails.
     */
    private Object resolveDebugContext(String resourceId, Map<String, Object> context) 
            throws DebugFrameworkException, DebugFrameworkServerException {
        
        Object contextResolver = DebugProtocolRouter.getContextProviderForResource(resourceId);
        if (contextResolver == null) {
            throw new DebugFrameworkException("No suitable DebugContextProvider found for IDP: " + resourceId);
        }

        debugLog("Successfully loaded context resolver for IDP: " + resourceId);

        Object debugContext = invokeContextResolverMethod(contextResolver, "resolveContext", context);
        if (debugContext == null) {
            throw new DebugFrameworkException("Failed to resolve debug context for IDP: " + resourceId);
        }

        debugLog("Successfully resolved debug context for IDP: " + resourceId);
        return debugContext;
    }

    /**
     * Executes the debug request using the appropriate executor.
     *
     * @param resourceId The IDP resource ID.
     * @param debugContext The resolved debug context.
     * @return The execution result object.
     * @throws DebugFrameworkException If executor is not found or execution fails.
     * @throws DebugFrameworkServerException If execution fails.
     */
    private Object executeDebugRequest(String resourceId, Object debugContext) 
            throws DebugFrameworkException, DebugFrameworkServerException {
        
        Object executor = DebugProtocolRouter.getExecutorForResource(resourceId);
        if (executor == null) {
            throw new DebugFrameworkException("No suitable DebugExecutor found for IDP: " + resourceId);
        }

        debugLog("Successfully loaded executor for IDP: " + resourceId);

        Object authUrlResult = invokeExecutorMethod(executor, "execute", debugContext);
        if (authUrlResult == null) {
            throw new DebugFrameworkException("Authorization URL generation failed for IDP: " + resourceId);
        }

        return authUrlResult;
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

    /**
     * Invokes resolveContext on a context resolver using reflection.
     * Handles reflection errors properly with specific exception types.
     *
     * @param contextResolver The context resolver instance.
     * @param methodName The method name to invoke.
     * @param context The debug context.
     * @return The resolved context object.
     * @throws DebugFrameworkServerException If the method cannot be invoked or execution fails.
     */
    private Object invokeContextResolverMethod(Object contextResolver, String methodName, 
                                              Map<String, Object> context) throws DebugFrameworkServerException {

        if (contextResolver == null) {
            throw new IllegalArgumentException("Context resolver is null");
        }

        try {
            Class<?> resolverClass = contextResolver.getClass();
            java.lang.reflect.Method method = resolverClass.getMethod(methodName, Map.class);
            Object result = method.invoke(contextResolver, context);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully invoked " + methodName + " on " + resolverClass.getSimpleName());
            }
            return result;

        } catch (NoSuchMethodException e) {
            String errorMsg = "Context resolver does not have method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        } catch (IllegalAccessException e) {
            String errorMsg = "Cannot access context resolver method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = "Error invoking context resolver method '" + methodName + "'";
            if (cause != null) {
                LOG.error(errorMsg + ": " + cause.getMessage(), cause);
                throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, cause);
            } else {
                LOG.error(errorMsg, e);
                throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
            }
        } catch (SecurityException e) {
            String errorMsg = "Security error accessing context resolver method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        }
    }

    /**
     * Invokes execute on an executor using reflection.
     * Handles reflection errors properly with specific exception types.
     *
     * @param executor The executor instance.
     * @param methodName The method name to invoke.
     * @param debugContext The debug context.
     * @return The execution result.
     * @throws DebugFrameworkServerException If the method cannot be invoked or execution fails.
     */
    private Object invokeExecutorMethod(Object executor, String methodName, Object debugContext) throws DebugFrameworkServerException {

        if (executor == null) {
            throw new IllegalArgumentException("Executor is null");
        }
        if (debugContext == null) {
            throw new IllegalArgumentException("Debug context is null");
        }

        try {
            Class<?> executorClass = executor.getClass();
            java.lang.reflect.Method method = executorClass.getMethod(methodName, Map.class);
            Object result = method.invoke(executor, debugContext);
            
            if (result == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Executor method '" + methodName + "' returned null");
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully invoked " + methodName + " on " + executorClass.getSimpleName());
            }
            return result;

        } catch (NoSuchMethodException e) {
            String errorMsg = "Executor does not have method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        } catch (IllegalAccessException e) {
            String errorMsg = "Cannot access executor method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = "Error invoking executor method '" + methodName + "'";
            if (cause != null) {
                LOG.error(errorMsg + ": " + cause.getMessage(), cause);
                throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, cause);
            } else {
                LOG.error(errorMsg, e);
                throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
            }
        } catch (SecurityException e) {
            String errorMsg = "Security error accessing executor method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        }
    }

    /**
     * Extracts result data as a Map from various result object types.
     * Handles both direct Map results and DebugResult-like objects with getResultData().
     *
     * @param resultObject The result object from executor (can be null).-
     * @return Map containing extracted result data, or empty Map if no data found.
     */
    private Map<String, Object> extractResultAsMap(Object resultObject) {

        if (resultObject == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Result object is null, returning empty map");
            }
            return new java.util.HashMap<>();
        }

        if (resultObject instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = (Map<String, Object>) resultObject;
            return resultMap;
        }

        // Try to extract from DebugResult-like objects
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            Object resultDataObj = invokeMethodByName(resultObject, "getResultData");
            if (resultDataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resultDataMap = (Map<String, Object>) resultDataObj;
                result.putAll(resultDataMap);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Extracted result data from DebugResult-like object");
                }
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not extract result data from object: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * Invokes a method by name on an object using reflection.
     * Handles reflection errors with specific exception types.
     *
     * @param object The object on which to invoke the method (must not be null).
     * @param methodName The name of the method to invoke (must not be null or empty).
     * @return The result of the method invocation.
     * @throws DebugFrameworkServerException If the method cannot be found or invoked.
     */
    private Object invokeMethodByName(Object object, String methodName) throws DebugFrameworkServerException {
        
        if (object == null) {
            throw new IllegalArgumentException("Object is null");
        }
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Method name is null or empty");
        }

        try {
            Class<?> objectClass = object.getClass();
            java.lang.reflect.Method method = objectClass.getMethod(methodName);
            return method.invoke(object);
        } catch (NoSuchMethodException e) {
            String errorMsg = "Object does not have method '" + methodName + "'";
            if (LOG.isDebugEnabled()) {
                LOG.debug(errorMsg + ": " + e.getMessage());
            }
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        } catch (IllegalAccessException e) {
            String errorMsg = "Cannot access method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = "Error invoking method '" + methodName + "'";
            if (cause != null) {
                LOG.error(errorMsg + ": " + cause.getMessage(), cause);
                throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, cause);
            } else {
                LOG.error(errorMsg, e);
                throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
            }
        } catch (SecurityException e) {
            String errorMsg = "Security error accessing method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new DebugFrameworkServerException("REFLECTION_ERROR", errorMsg, e);
        }
    }
}
