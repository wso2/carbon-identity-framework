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
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants.ErrorMessages;
import org.wso2.carbon.identity.debug.framework.core.DebugContextProvider;
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
import org.wso2.carbon.identity.debug.idp.registry.IdpDebugProviderRegistry;
import org.wso2.carbon.identity.debug.idp.resolver.IdpDebugProtocolResolver;
import org.wso2.carbon.identity.debug.idp.resolver.IdpDebugProtocolResolver.ProtocolResolutionResult;

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
            ProtocolResolutionResult resolutionResult = resolveProtocol(connectionId);
            DebugProtocolProvider protocolProvider = resolveProtocolProvider(resolutionResult, connectionId);
            DebugContext resolvedContext = resolveDebugContext(connectionId, resourceType,
                    resolutionResult.getIdentityProvider(), protocolProvider);

            DebugResult debugResult = protocolProvider.getExecutor().execute(resolvedContext);
            return DebugFrameworkResponse.fromDebugResult(debugResult);

        } catch (ContextResolutionException e) {
            throw DebugFrameworkUtils.handleServerException(ErrorMessages.ERROR_CODE_CONTEXT_RESOLUTION_FAILED, e,
                    connectionId);
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
     * @throws ContextResolutionException   If context resolution fails.
     * @throws DebugFrameworkServerException If context provider is not available.
     */
    protected DebugContext resolveDebugContext(String connectionId, String resourceType,
            IdentityProvider identityProvider, DebugProtocolProvider protocolProvider)
            throws ContextResolutionException, DebugFrameworkServerException {

        DebugContextProvider contextProvider = protocolProvider.getContextProvider();

        if (contextProvider == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Context provider not available for resource: " + connectionId);
            }
            throw new DebugFrameworkServerException(
                    ErrorMessages.ERROR_CODE_CONTEXT_PROVIDER_NOT_FOUND.getCode(),
                    ErrorMessages.ERROR_CODE_CONTEXT_PROVIDER_NOT_FOUND.getMessage(),
                    String.format(ErrorMessages.ERROR_CODE_CONTEXT_PROVIDER_NOT_FOUND.getDescription(), connectionId));
        }

        Map<String, Object> params = new HashMap<>();
        params.put(IdpDebugConstants.CONNECTION_ID, connectionId);
        params.put(IdpDebugConstants.RESOURCE_TYPE_KEY, resourceType);
        params.put(IdpDebugConstants.IDENTITY_PROVIDER, identityProvider);
        return contextProvider.resolveContext(params);
    }

    protected ProtocolResolutionResult resolveProtocol(String connectionId)
            throws DebugFrameworkClientException {

        ProtocolResolutionResult result = IdpDebugProtocolResolver.resolveProtocol(connectionId);
        if (result == null) {
            throw DebugFrameworkUtils.handleClientException(
                    ErrorMessages.ERROR_CODE_EXECUTOR_NOT_FOUND, connectionId);
        }
        return result;
    }

    protected DebugProtocolProvider resolveProtocolProvider(ProtocolResolutionResult resolutionResult,
            String connectionId) throws DebugFrameworkClientException {

        DebugProtocolProvider provider = IdpDebugProviderRegistry.getInstance()
                .getProvider(resolutionResult.getProtocolKey());
        if (provider == null) {
            throw DebugFrameworkUtils.handleClientException(
                    ErrorMessages.ERROR_CODE_EXECUTOR_NOT_FOUND, connectionId);
        }
        return provider;
    }
}
