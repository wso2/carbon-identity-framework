/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.core.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.core.DebugContextProvider;
import org.wso2.carbon.identity.debug.framework.core.DebugExecutor;
import org.wso2.carbon.identity.debug.framework.core.DebugProtocolRouter;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for debugging Identity Provider (IdP) resources.
 */
public class IdpDebugResourceHandler implements DebugResourceHandler {

    private static final Log LOG = LogFactory.getLog(IdpDebugResourceHandler.class);
    private static final String STATUS = "status";
    private static final String MESSAGE = "message";
    private static final String FAILURE = "FAILURE";
    private static final String ERROR_TYPE = "errorType";
    private static final String REASON = "reason";

    @Override
    public Map<String, Object> handleDebugRequest(Map<String, Object> debugRequest) {
        try {
            String resourceId = (String) debugRequest.get("resourceId");
            String resourceType = (String) debugRequest.get("resourceType");
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("IdP debug handler processing resource: " + resourceId);
            }

            Map<String, Object> resolvedContext = resolveDebugContextSafely(resourceId, resourceType);
            
            // Check if context resolution returned an error response.
            if (resolvedContext != null && resolvedContext.containsKey(STATUS) 
                    && FAILURE.equals(resolvedContext.get(STATUS))) {
                return resolvedContext;
            }
            
            if (resolvedContext == null) {
                return createErrorResponse(FAILURE, "Unable to resolve debug context for resource: " + resourceId);
            }

            DebugExecutor executor = DebugProtocolRouter.getExecutorForResource(resourceId);
            if (executor == null) {
                return createErrorResponse(FAILURE, "Executor not available for resource: " + resourceId);
            }

            org.wso2.carbon.identity.debug.framework.model.DebugResult debugResult = executor.execute(resolvedContext);
            return convertDebugResultToMap(debugResult);

        } catch (Exception e) {
            LOG.error("Error in IdP debug handler: " + e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put(STATUS, FAILURE);
            errorResult.put(MESSAGE, e.getMessage());
            errorResult.put(ERROR_TYPE, e.getClass().getSimpleName());
            return errorResult;
        }
    }

    /**
     * Resolves the debug context for a given resource, returning error responses gracefully.
     *
     * @param resourceId The resource ID.
     * @param resourceType The resource type.
     * @return The resolved context map, or an error response map if resolution fails.
     */
    private Map<String, Object> resolveDebugContextSafely(String resourceId, String resourceType) {
        DebugContextProvider contextProvider = DebugProtocolRouter.getContextProviderForResource(resourceId);
        
        if (contextProvider == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context provider not available for resource: " + resourceId);
            }
            return createErrorResponse(FAILURE, "Context provider not available for resource: " + resourceId);
        }

        try {
            Map<String, Object> context = contextProvider.resolveContext(resourceId, resourceType);
            if (context == null && LOG.isDebugEnabled()) {
                LOG.debug("Debug context is null for resource: " + resourceId);
            }
            return context;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context resolution failed for resource: " + resourceId + ". Error: " + e.getMessage());
            }
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put(STATUS, FAILURE);
            errorResponse.put(MESSAGE, "Unable to resolve debug context for IdP: " + resourceId);
            errorResponse.put(REASON, e.getMessage());
            errorResponse.put(ERROR_TYPE, e.getClass().getSimpleName());
            return errorResponse;
        }
    }

    /**
     * Creates an error response map with given status and message.
     *
     * @param status The status value.
     * @param message The error message.
     * @return Error response map.
     */
    private Map<String, Object> createErrorResponse(String status, String message) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put(STATUS, status);
        errorResult.put(MESSAGE, message);
        return errorResult;
    }

    /**
     * Converts a DebugResult object to a Map representation.
     * Flattens resultData and metadata into the top-level map for easier access.
     *
     * @param debugResult The DebugResult to convert.
     * @return Map containing the debug result data.
     */
    private Map<String, Object> convertDebugResultToMap(
            org.wso2.carbon.identity.debug.framework.model.DebugResult debugResult) {
        Map<String, Object> resultMap = new HashMap<>();
        
        if (debugResult == null) {
            resultMap.put(STATUS, FAILURE);
            resultMap.put(MESSAGE, "Debug execution returned null result");
            return resultMap;
        }

        populateBasicDebugResultFields(resultMap, debugResult);
        flattenResultDataIntoMap(resultMap, debugResult.getResultData());
        flattenMetadataIntoMap(resultMap, debugResult.getMetadata());
        
        return resultMap;
    }

    /**
     * Populates basic debug result fields into the map.
     *
     * @param resultMap Map to populate.
     * @param debugResult Debug result containing basic fields.
     */
    private void populateBasicDebugResultFields(Map<String, Object> resultMap, 
            org.wso2.carbon.identity.debug.framework.model.DebugResult debugResult) {
        
        resultMap.put("successful", debugResult.isSuccessful());
        resultMap.put("resultId", debugResult.getResultId());
        resultMap.put("timestamp", debugResult.getTimestamp());
        resultMap.put(STATUS, debugResult.getStatus());
        
        if (debugResult.getErrorCode() != null) {
            resultMap.put("errorCode", debugResult.getErrorCode());
        }
        
        if (debugResult.getErrorMessage() != null) {
            resultMap.put("errorMessage", debugResult.getErrorMessage());
        }
    }

    /**
     * Flattens result data entries into the result map.
     *
     * @param resultMap Map to populate.
     * @param resultData Result data to flatten.
     */
    private void flattenResultDataIntoMap(Map<String, Object> resultMap, 
            Map<String, Object> resultData) {
        
        if (resultData != null && !resultData.isEmpty()) {
            for (Map.Entry<String, Object> entry : resultData.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    resultMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * Flattens metadata entries into the result map, avoiding overwrites of existing keys.
     *
     * @param resultMap Map to populate.
     * @param metadata Metadata to flatten.
     */
    private void flattenMetadataIntoMap(Map<String, Object> resultMap, 
            Map<String, Object> metadata) {
        
        if (metadata != null && !metadata.isEmpty()) {
            for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                String key = entry.getKey();
                if (key != null && entry.getValue() != null) {
                    resultMap.computeIfAbsent(key, k -> entry.getValue());
                }
            }
        }
    }
}
