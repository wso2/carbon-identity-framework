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

package org.wso2.carbon.identity.debug.idp.core.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.core.DebugProtocolRouter;
import org.wso2.carbon.identity.debug.framework.extension.DebugContextProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugExecutor;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;
import org.wso2.carbon.identity.debug.framework.model.DebugRequest;
import org.wso2.carbon.identity.debug.framework.model.DebugResponse;
import org.wso2.carbon.identity.debug.framework.model.DebugResult;

import java.util.Map;

/**
 * Handler for debugging Identity Provider (IdP) resources.
 * This is the IDP-specific implementation of the DebugResourceHandler
 * interface.
 */
public class IdpDebugResourceHandler implements DebugResourceHandler {

    private static final Log LOG = LogFactory.getLog(IdpDebugResourceHandler.class);

    /**
     * Handles a debug request using typed classes.
     * This is the preferred method with type safety.
     * For IDP resource type, the resourceId is required and must be present.
     *
     * @param debugRequest The debug request with resource information.
     * @return DebugResponse containing the execution result.
     */
    public DebugResponse handleDebugRequest(DebugRequest debugRequest) {

        try {
            String resourceId = debugRequest.getEffectiveResourceId();
            String resourceType = debugRequest.getResourceType();

            // Validate that resourceId is provided for IDP debugging.
            if (resourceId == null || resourceId.trim().isEmpty()) {
                return DebugResponse.error("Resource ID is required for IDP debugging. " +
                        "Provide it in the properties map with key 'resourceId'.");
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("IdP debug handler processing resource: " + resourceId);
            }

            DebugContext resolvedContext = resolveDebugContext(resourceId, resourceType);

            // Check if context resolution returned an error.
            if (resolvedContext.isError()) {
                return DebugResponse.error(resolvedContext.getErrorMessage());
            }

            DebugExecutor executor = DebugProtocolRouter.getExecutorForResource(resourceId);
            if (executor == null) {
                return DebugResponse.error("Executor not available for resource: " + resourceId);
            }

            DebugResult debugResult = executor.execute(resolvedContext.toMap());
            return DebugResponse.fromDebugResult(debugResult);

        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error in IdP debug handler: " + e.getMessage(), e);
            }
            return DebugResponse.error(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> handleDebugRequest(Map<String, Object> debugRequest) {

        // Convert Map to typed request.
        DebugRequest request = DebugRequest.fromMap(debugRequest);
        
        // Use the typed method.
        DebugResponse response = handleDebugRequest(request);
        
        // Convert response back to Map for backward compatibility.
        return response.toMap();
    }

    /**
     * Resolves the debug context for a given resource using typed classes.
     * This is the preferred method with type safety.
     *
     * @param resourceId   The resource ID.
     * @param resourceType The resource type.
     * @return DebugContext containing the resolved context or error information.
     */
    private DebugContext resolveDebugContext(String resourceId, String resourceType) {

        DebugContextProvider contextProvider = DebugProtocolRouter.getContextProviderForResource(resourceId);

        if (contextProvider == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context provider not available for resource: " + resourceId);
            }
            return DebugContext.error("Context provider not available for resource: " + resourceId);
        }

        try {
            Map<String, Object> contextMap = contextProvider.resolveContext(resourceId, resourceType);
            if (contextMap == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug context is null for resource: " + resourceId);
                }
                return DebugContext.error("Unable to resolve debug context for resource: " + resourceId);
            }
            return DebugContext.fromMap(contextMap);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context resolution failed for resource: " + resourceId + ". Error: " + e.getMessage());
            }
            return DebugContext.error("Unable to resolve debug context for IdP: " + resourceId, 
                    e.getClass().getSimpleName());
        }
    }

}
