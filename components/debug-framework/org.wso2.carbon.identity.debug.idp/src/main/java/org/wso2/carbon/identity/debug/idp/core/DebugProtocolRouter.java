/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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

package org.wso2.carbon.identity.debug.idp.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.extension.DebugCallbackHandler;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.model.DebugResourceType;
import org.wso2.carbon.identity.debug.framework.registry.DebugHandlerRegistry;
import org.wso2.carbon.identity.debug.framework.registry.DebugProtocolRegistry;

import java.util.List;
import java.util.Locale;

/**
 * Routes IdP debug requests to protocol-specific components.
 */
public class DebugProtocolRouter {

    protected static final Log LOG = LogFactory.getLog(DebugProtocolRouter.class);

    private DebugProtocolRouter() {

        // Utility class.
    }

    /**
     * Resolves the protocol provider for the specified connection.
     * This is the main router entry point used by the IdP handler.
     *
     * @param connectionId The connection ID.
     * @return DebugProtocolProvider instance, or null if not available.
     */
    public static DebugProtocolProvider resolveProvider(String connectionId) {

        // Find the protocol type
        String protocolType = resolveProtocolType(connectionId);
        if (StringUtils.isBlank(protocolType)) {
            return null;
        }
        
        // Get the provider for the protocol
        DebugProtocolProvider provider = getDebugProtocolProvider(protocolType);
        if (provider != null && LOG.isDebugEnabled()) {
            LOG.debug("Resolved protocol provider for resource: " + connectionId);
        }
        return provider;
    }

    /**
     * Retrieves the debug resource handler for the specified resource type.
     *
     * @param resourceType The resource type.
     * @return DebugResourceHandler instance, or null if not available.
     */
    public static DebugResourceHandler getDebugResourceHandler(String resourceType) {

        if (StringUtils.isBlank(resourceType)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resource type is empty, unable to route debug request");
            }
            return null;
        }

        DebugResourceType resolvedType = DebugResourceType.fromString(resourceType);
        if (resolvedType == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unsupported resource type: " + resourceType);
            }
            return null;
        }
        DebugResourceHandler resourceHandler = DebugHandlerRegistry.getInstance()
                .getHandler(resolvedType.name().toLowerCase(Locale.ENGLISH));
        if (resourceHandler == null && LOG.isDebugEnabled()) {
            LOG.debug("No DebugResourceHandler registered for resource type: " + resourceType);
        }
        return resourceHandler;
    }

    /**
     * Retrieves all registered callback handlers.
     *
     * @return List of all DebugCallbackHandler instances.
     */
    public static List<DebugCallbackHandler> getAllCallbackHandlers() {

        return DebugProtocolRegistry.getInstance().getDebugCallbackHandlers();
    }

    protected static DebugProtocolProvider getDebugProtocolProvider(String protocolType) {

        if (StringUtils.isEmpty(protocolType)) {
            return null;
        }

        String normalizedType = normalizeProtocolType(protocolType);
        DebugProtocolProvider provider = DebugProtocolRegistry.getInstance()
                .getDebugProtocolProvider(normalizedType);

        if (provider != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved protocol provider for type: " + normalizedType);
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Protocol provider not found for type: " + normalizedType);
        }
        return provider;
    }

    protected static String resolveProtocolType(String connectionId) {

        if (StringUtils.isBlank(connectionId)) {
            LOG.warn("Connection ID is blank — cannot resolve protocol. " +
                    "Ensure a valid connection ID is provided.");
            return null;
        }

        List<DebugProtocolResolver> resolvers = DebugProtocolRegistry.getInstance().getDebugProtocolResolvers();
        for (DebugProtocolResolver resolver : resolvers) {
            String resolvedProtocol = resolver.resolveProtocol(connectionId);
            if (StringUtils.isNotBlank(resolvedProtocol)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Protocol resolved by " + resolver.getClass().getSimpleName()
                            + ": " + resolvedProtocol);
                }
                return resolvedProtocol;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("No protocol resolved for resource: " + connectionId
                    + ". Ensure a matching protocol resolver is deployed and active.");
        }
        return null;
    }

    protected static String normalizeProtocolType(String protocolType) {

        if (protocolType == null) {
            return null;
        }
        return protocolType.trim().toLowerCase(Locale.ENGLISH);
    }
}
