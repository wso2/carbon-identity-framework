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
import org.wso2.carbon.identity.debug.framework.core.DebugProtocolRouter;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;

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
            String resourceId = (String) debugRequestContext.get("resourceId");
            String resourceType = (String) debugRequestContext.get("resourceType");
            
            if (resourceId == null || resourceId.trim().isEmpty()) {
                throw new DebugFrameworkException("Resource ID is required for IDP debug request");
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing IDP debug request for resourceId: " + resourceId);
            }

            // Build context map for protocol-specific handler
            Map<String, Object> context = new HashMap<>(debugRequestContext);
            
            // Add IDP-specific fields expected by DebugRequestCoordinator
            if (!context.containsKey("idpId")) {
                context.put("idpId", resourceId);
            }
            if (!context.containsKey("idpName")) {
                context.put("idpName", resourceId);
            }
            
            // Extract properties if they exist
            @SuppressWarnings("unchecked")
            Map<String, String> properties = (Map<String, String>) debugRequestContext.get("properties");
            if (properties != null && !properties.isEmpty()) {
                context.putAll(properties);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Added " + properties.size() + " properties to IDP debug context");
                }
            }

            // Mark as resource debug request type
            context.put("requestType", "RESOURCE_DEBUG_REQUEST");
            context.put("resourceId", resourceId);
            context.put("resourceType", resourceType);

            // Get protocol-specific context resolver and executor
            Object contextResolver = DebugProtocolRouter.getContextProviderForResource(resourceId);
            if (contextResolver == null) {
                throw new DebugFrameworkException("No suitable DebugContextProvider found for IDP: " + resourceId);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully loaded context resolver for IDP: " + resourceId);
            }

            // Invoke resolveContext on the context resolver
            Object debugContext = invokeContextResolverMethod(contextResolver, "resolveContext", context);
            if (debugContext == null) {
                throw new DebugFrameworkException("Failed to resolve debug context for IDP: " + resourceId);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully resolved debug context for IDP: " + resourceId);
            }

            // Get protocol-specific executor
            Object executor = DebugProtocolRouter.getExecutorForResource(resourceId);
            if (executor == null) {
                throw new DebugFrameworkException("No suitable DebugExecutor found for IDP: " + resourceId);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully loaded executor for IDP: " + resourceId);
            }

            // Invoke execute on the executor
            Object authUrlResult = invokeExecutorMethod(executor, "execute", debugContext);
            if (authUrlResult == null) {
                throw new DebugFrameworkException("Authorization URL generation failed for IDP: " + resourceId);
            }

            // Extract result
            Map<String, Object> result = extractResultAsMap(authUrlResult);
            
            // Add metadata
            result.put("timestamp", System.currentTimeMillis());
            result.put("resourceId", resourceId);
            result.put("resourceType", resourceType);
            result.put("requestType", "RESOURCE_DEBUG_REQUEST");
            result.put("status", "SUCCESS");

            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully processed IDP debug request for resourceId: " + resourceId);
            }

            return result;

        } catch (DebugFrameworkException e) {
            throw e;
        } catch (Exception e) {
            throw new DebugFrameworkException("Error handling IDP debug request: " + e.getMessage(), e);
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
     * @throws Exception If the method cannot be invoked or execution fails.
     */
    private Object invokeContextResolverMethod(Object contextResolver, String methodName, 
                                              Map<String, Object> context) throws Exception {
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
            throw new Exception(errorMsg, e);
        } catch (IllegalAccessException e) {
            String errorMsg = "Cannot access context resolver method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new Exception(errorMsg, e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = "Error invoking context resolver method '" + methodName + "'";
            if (cause != null) {
                LOG.error(errorMsg + ": " + cause.getMessage(), cause);
                throw new Exception(errorMsg, cause);
            } else {
                LOG.error(errorMsg, e);
                throw new Exception(errorMsg, e);
            }
        } catch (SecurityException e) {
            String errorMsg = "Security error accessing context resolver method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new Exception(errorMsg, e);
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
     * @throws Exception If the method cannot be invoked or execution fails.
     */
    private Object invokeExecutorMethod(Object executor, String methodName, Object debugContext) throws Exception {
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
            throw new Exception(errorMsg, e);
        } catch (IllegalAccessException e) {
            String errorMsg = "Cannot access executor method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new Exception(errorMsg, e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = "Error invoking executor method '" + methodName + "'";
            if (cause != null) {
                LOG.error(errorMsg + ": " + cause.getMessage(), cause);
                throw new Exception(errorMsg, cause);
            } else {
                LOG.error(errorMsg, e);
                throw new Exception(errorMsg, e);
            }
        } catch (SecurityException e) {
            String errorMsg = "Security error accessing executor method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new Exception(errorMsg, e);
        }
    }

    /**
     * Extracts result data as a Map from various result object types.
     * Handles both direct Map results and DebugResult-like objects with getResultData().
     *
     * @param resultObject The result object from executor (can be null).
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
     * @throws Exception If the method cannot be found or invoked.
     */
    private Object invokeMethodByName(Object object, String methodName) throws Exception {
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
            throw new Exception(errorMsg, e);
        } catch (IllegalAccessException e) {
            String errorMsg = "Cannot access method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new Exception(errorMsg, e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            String errorMsg = "Error invoking method '" + methodName + "'";
            if (cause != null) {
                LOG.error(errorMsg + ": " + cause.getMessage(), cause);
                throw new Exception(errorMsg, cause);
            } else {
                LOG.error(errorMsg, e);
                throw new Exception(errorMsg, e);
            }
        } catch (SecurityException e) {
            String errorMsg = "Security error accessing method '" + methodName + "'";
            LOG.error(errorMsg + ": " + e.getMessage(), e);
            throw new Exception(errorMsg, e);
        }
    }
}
