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
import org.wso2.carbon.identity.debug.framework.exception.ContextResolutionException;
import org.wso2.carbon.identity.debug.framework.exception.DebugExecutionException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkClientException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;
import org.wso2.carbon.identity.debug.framework.model.DebugFrameworkRequest;
import org.wso2.carbon.identity.debug.framework.model.DebugFrameworkResponse;
import org.wso2.carbon.identity.debug.framework.model.DebugResult;
import org.wso2.carbon.identity.debug.framework.util.DebugFrameworkUtils;
import org.wso2.carbon.identity.debug.idp.core.IdpDebugConstants;
import org.wso2.carbon.identity.debug.idp.core.IdpDebugProtocolRouter;

import java.util.HashMap;
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
     * @param debugFrameworkRequest The debug request with resource information.
     * @return DebugFrameworkResponse containing the execution result.
     * @throws DebugFrameworkClientException If the request has validation errors.
     * @throws DebugFrameworkServerException If a server-side error occurs.
     */
    @Override
    public DebugFrameworkResponse handleDebugRequest(DebugFrameworkRequest debugFrameworkRequest)
            throws DebugFrameworkClientException, DebugFrameworkServerException {

        String connectionId = (String) debugFrameworkRequest.getAdditionalContext()
                .get(IdpDebugConstants.CONNECTION_ID);
        String resourceType = debugFrameworkRequest.getResourceType();

        // Validate that connectionId is provided for IDP debugging.
        if (StringUtils.isEmpty(connectionId)) {
            throw DebugFrameworkUtils.handleClientException(ErrorMessages.ERROR_CODE_MISSING_RESOURCE_ID);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("IdP debug handler processing resource: " + connectionId);
        }

        try {
            DebugProtocolProvider protocolProvider = resolveProtocolProvider(connectionId);
            DebugContext resolvedContext = resolveDebugContext(connectionId, resourceType, protocolProvider);
            DebugExecutor executor = getExecutor(protocolProvider, connectionId);
            if (executor == null) {
                throw DebugFrameworkUtils.handleClientException(
                        ErrorMessages.ERROR_CODE_EXECUTOR_NOT_FOUND, connectionId);
            }

            DebugResult debugResult = executor.execute(resolvedContext);
            return DebugFrameworkResponse.fromDebugResult(debugResult);

        } catch (DebugFrameworkClientException e) {
            throw e;
        } catch (ContextResolutionException e) {
            throw new DebugFrameworkClientException(e.getErrorCode(), e.getMessage(), e.getDescription(), e);
        } catch (DebugExecutionException e) {
            LOG.error("Error in IdP debug handler for resource: " + connectionId);
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
    protected DebugContext resolveDebugContext(String connectionId, String resourceType,
            DebugProtocolProvider protocolProvider)
            throws ContextResolutionException, DebugFrameworkClientException {

        DebugContextProvider contextProvider = getContextProvider(protocolProvider, connectionId);

        if (contextProvider == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context provider not available for resource: " + connectionId);
            }
            throw DebugFrameworkUtils.handleClientException(
                    ErrorMessages.ERROR_CODE_CONTEXT_PROVIDER_NOT_FOUND, connectionId);
        }

        Map<String, Object> params = new HashMap<>();
        params.put(IdpDebugConstants.CONNECTION_ID, connectionId);
        params.put(IdpDebugConstants.RESOURCE_TYPE_KEY, resourceType);
        DebugContext debugContext = contextProvider.resolveContext(params);
        if (debugContext == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug context is null for resource: " + connectionId);
            }
            throw new ContextResolutionException(
                    ErrorMessages.ERROR_CODE_CONTEXT_RESOLUTION_FAILED.getCode(),
                    ErrorMessages.ERROR_CODE_CONTEXT_RESOLUTION_FAILED.getMessage(),
                    String.format(ErrorMessages.ERROR_CODE_CONTEXT_RESOLUTION_FAILED
                        .getDescription(), connectionId));
        }
        return debugContext;
    }

    /**
     * Resolves the protocol provider for the given resource ID.
     *
     * @param connectionId Connection ID or name.
     * @return DebugProtocolProvider for the resource, or null if not available.
     */
    protected DebugProtocolProvider resolveProtocolProvider(String connectionId) {

        DebugProtocolProvider protocolProvider = IdpDebugProtocolRouter.resolveProvider(connectionId);
        if (protocolProvider == null && LOG.isDebugEnabled()) {
            LOG.debug("No DebugProtocolProvider found for resource: " + connectionId +
                    ". Ensure a matching protocol resolver and provider are deployed and active.");
        }
        return protocolProvider;
    }

    /**
     * Retrieves the protocol-specific context provider from the resolved protocol provider.
     *
     * @param protocolProvider The resolved protocol provider.
     * @param connectionId The connection ID identifying the IdP resource.
     * @return DebugContextProvider instance, or null if not available.
     */
    protected DebugContextProvider getContextProvider(DebugProtocolProvider protocolProvider, String connectionId) {

        if (protocolProvider == null) {
            return null;
        }

        DebugContextProvider contextProvider = protocolProvider.getContextProvider();
        if (contextProvider == null && LOG.isDebugEnabled()) {
            LOG.debug("No DebugContextProvider found for resource: " + connectionId +
                    ". Ensure the protocol module is deployed and active.");
        }
        return contextProvider;
    }

    protected DebugExecutor getExecutor(DebugProtocolProvider protocolProvider, String connectionId) {

        if (protocolProvider == null) {
            return null;
        }

        DebugExecutor executor = protocolProvider.getExecutor();
        if (executor == null && LOG.isDebugEnabled()) {
            LOG.debug("No DebugExecutor found for resource: " + connectionId);
        }
        return executor;
    }

}
