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
import org.wso2.carbon.identity.debug.framework.core.DebugContextProvider;
import org.wso2.carbon.identity.debug.framework.core.DebugExecutor;
import org.wso2.carbon.identity.debug.framework.core.DebugProcessor;
import org.wso2.carbon.identity.debug.framework.extension.DebugCallbackHandler;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.model.DebugResourceType;
import org.wso2.carbon.identity.debug.framework.registry.DebugProtocolRegistry;

import java.util.List;
import java.util.Locale;

/**
 * Routes IdP debug requests to protocol-specific components.
 */
public class DebugProtocolRouter {

    private static final Log LOG = LogFactory.getLog(DebugProtocolRouter.class);
    private static final String DEFAULT_PROTOCOL_TYPE = "OIDC";

    private DebugProtocolRouter() {

        // Utility class.
    }

    public static DebugContextProvider getContextProviderForResource(String connectionId) {

        return getProtocolProviderComponent(connectionId, DebugProtocolProvider::getContextProvider,
                "Context Provider");
    }

    public static DebugExecutor getExecutorForResource(String connectionId) {

        return getProtocolProviderComponent(connectionId, DebugProtocolProvider::getExecutor, "Executor");
    }

    public static DebugProcessor getProcessorForResource(String connectionId) {

        return getProtocolProviderComponent(connectionId, DebugProtocolProvider::getProcessor, "Processor");
    }

    public static DebugCallbackHandler getCallbackHandlerForResource(String connectionId) {

        return getProtocolProviderComponent(connectionId, DebugProtocolProvider::getCallbackHandler,
                "Callback Handler");
    }

    public static DebugResourceHandler getDebugResourceHandler(String resourceType) {

        if (StringUtils.isEmpty(resourceType)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resource type is empty, unable to route debug request");
            }
            return null;
        }

        DebugResourceType resolvedType = DebugResourceType.fromString(resourceType);
        DebugResourceHandler resourceHandler = resolvedType.getHandler();
        if (resourceHandler == null && LOG.isDebugEnabled()) {
            LOG.debug("No DebugResourceHandler registered for resource type: " + resourceType);
        }
        return resourceHandler;
    }

    public static List<DebugCallbackHandler> getAllCallbackHandlers() {

        return DebugProtocolRegistry.getInstance().getDebugCallbackHandlers();
    }

    @FunctionalInterface
    private interface ProviderComponentExtractor<T> {

        T extract(DebugProtocolProvider provider);
    }

    private static <T> T getProtocolProviderComponent(String connectionId, ProviderComponentExtractor<T> extractor,
                                                      String componentName) {

        DebugProtocolProvider provider = resolveProtocolProvider(connectionId);

        if (provider != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting " + componentName + " for resource.");
            }
            return extractor.extract(provider);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(componentName + " not available for resource.");
        }
        return null;
    }

    private static DebugProtocolProvider getDebugProtocolProvider(String protocolType) {

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

    private static DebugProtocolProvider resolveProtocolProvider(String connectionId) {

        String protocolType = resolveProtocolType(connectionId);
        if (StringUtils.isNotBlank(protocolType)) {
            DebugProtocolProvider provider = getDebugProtocolProvider(protocolType);
            if (provider != null) {
                return provider;
            }
        }

        return null;
    }

    private static String resolveProtocolType(String connectionId) {

        if (StringUtils.isBlank(connectionId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Connection ID is empty, defaulting to protocol: " + DEFAULT_PROTOCOL_TYPE);
            }
            return DEFAULT_PROTOCOL_TYPE;
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
                    + ", defaulting to protocol: " + DEFAULT_PROTOCOL_TYPE);
        }
        return DEFAULT_PROTOCOL_TYPE;
    }

    private static String normalizeProtocolType(String protocolType) {

        return protocolType.trim().toLowerCase(Locale.ENGLISH);
    }
}
