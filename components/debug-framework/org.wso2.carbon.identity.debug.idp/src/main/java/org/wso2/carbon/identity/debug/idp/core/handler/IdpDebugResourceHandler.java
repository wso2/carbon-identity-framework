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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.ErrorMessages;
import org.wso2.carbon.identity.debug.framework.core.DebugContextProvider;
import org.wso2.carbon.identity.debug.framework.core.DebugExecutor;
import org.wso2.carbon.identity.debug.framework.core.DebugProcessor;
import org.wso2.carbon.identity.debug.framework.core.DebugProtocolRouter;
import org.wso2.carbon.identity.debug.framework.exception.ContextResolutionException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkClientException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;
import org.wso2.carbon.identity.debug.framework.model.DebugRequest;
import org.wso2.carbon.identity.debug.framework.model.DebugResponse;
import org.wso2.carbon.identity.debug.framework.model.DebugResult;
import org.wso2.carbon.identity.debug.framework.util.DebugFrameworkUtils;

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
     * For IDP resource type, the connectionId is required and must be present.
     *
     * @param debugRequest The debug request with resource information.
     * @return DebugResponse containing the execution result.
     * @throws DebugFrameworkClientException If the request has validation errors.
     * @throws DebugFrameworkServerException If a server-side error occurs.
     */
    @Override
    public DebugResponse handleDebugRequest(DebugRequest debugRequest)
            throws DebugFrameworkClientException, DebugFrameworkServerException {

        String connectionId = debugRequest.getEffectiveConnectionId();
        String resourceType = debugRequest.getResourceType();

        // Validate that connectionId is provided for IDP debugging.
        if (StringUtils.isEmpty(connectionId)) {
            throw DebugFrameworkUtils.handleClientException(ErrorMessages.ERROR_CODE_MISSING_CONNECTION_ID);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("IdP debug handler processing resource: " + connectionId);
        }

        try {
            DebugContext resolvedContext = resolveDebugContext(connectionId, resourceType);

            DebugExecutor executor = getExecutor(connectionId);
            if (executor == null) {
                throw DebugFrameworkUtils.handleClientException(
                        ErrorMessages.ERROR_CODE_EXECUTOR_NOT_FOUND, connectionId);
            }

            DebugResult debugResult = executor.execute(resolvedContext);
            return DebugResponse.fromDebugResult(debugResult);

        } catch (DebugFrameworkClientException e) {
            throw e;
        } catch (ContextResolutionException e) {
            throw new DebugFrameworkClientException(e.getErrorCode(), e.getMessage(), e.getDescription(), e);
        } catch (Exception e) {
            LOG.error("Error in IdP debug handler for resource: " + connectionId, e);
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_SERVER_ERROR, e);
        }
    }

    /**
     * Resolves the debug context for a given resource using typed classes.
     *
     * @param connectionId The connection ID.
     * @param resourceType The resource type.
     * @return DebugContext containing the resolved context.
     * @throws ContextResolutionException    If context resolution fails.
     * @throws DebugFrameworkClientException If context provider is not available.
     */
    private DebugContext resolveDebugContext(String connectionId, String resourceType)
            throws ContextResolutionException, DebugFrameworkClientException {

        DebugContextProvider contextProvider = getContextProviderForResource(connectionId);

        if (contextProvider == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context provider not available for resource: " + connectionId);
            }
            throw DebugFrameworkUtils.handleClientException(
                    ErrorMessages.ERROR_CODE_CONTEXT_PROVIDER_NOT_FOUND, connectionId);
        }

        try {
            Map<String, Object> contextMap = contextProvider.resolveContext(connectionId, resourceType);
            if (contextMap == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug context is null for resource: " + connectionId);
                }
                throw new ContextResolutionException(
                        ErrorMessages.ERROR_CODE_CONTEXT_RESOLUTION_FAILED.getCode(),
                        ErrorMessages.ERROR_CODE_CONTEXT_RESOLUTION_FAILED.getMessage(),
                        String.format(ErrorMessages.ERROR_CODE_CONTEXT_RESOLUTION_FAILED
                            .getDescription(), connectionId));
            }
            return DebugContext.buildFromMap(contextMap);
        } catch (ContextResolutionException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context resolution failed for resource: " + connectionId + ". Error: " + e.getMessage());
            }
            throw new ContextResolutionException(
                    ErrorMessages.ERROR_CODE_CONTEXT_RESOLUTION_FAILED.getCode(),
                    ErrorMessages.ERROR_CODE_CONTEXT_RESOLUTION_FAILED.getMessage(),
                    String.format(ErrorMessages.ERROR_CODE_CONTEXT_RESOLUTION_FAILED
                        .getDescription(), connectionId), e);
        }
    }

    /**
     * Gets the context provider for the given resource ID.
     * Delegates to DebugProtocolRouter which uses the framework's centralized
     * service registry (DebugFrameworkServiceDataHolder) for provider lookup.
     *
     * @param connectionId Connection ID or name.
     * @return DebugContextProvider for the resource, or null if not available.
     */
    private DebugContextProvider getContextProviderForResource(String connectionId) {

        DebugContextProvider contextProvider = DebugProtocolRouter.getContextProviderForResource(connectionId);
        if (contextProvider == null && LOG.isDebugEnabled()) {
            LOG.debug("No DebugContextProvider found for resource: " + connectionId +
                    ". Ensure the protocol module (e.g., OIDC) is deployed and active.");
        }
        return contextProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DebugExecutor getExecutor(String connectionId) {

        return DebugProtocolRouter.getExecutorForResource(connectionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DebugProcessor getProcessor(String connectionId) {

        return DebugProtocolRouter.getProcessorForResource(connectionId);
    }
}
