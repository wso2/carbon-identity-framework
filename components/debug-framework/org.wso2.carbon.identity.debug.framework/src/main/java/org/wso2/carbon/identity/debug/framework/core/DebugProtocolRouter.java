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

package org.wso2.carbon.identity.debug.framework.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.internal.DebugFrameworkServiceDataHolder;
import org.wso2.carbon.identity.debug.framework.model.DebugResourceType;

import java.util.List;
import java.util.Locale;

/**
 * Routes debug requests to appropriate protocol-specific context providers and
 * processors based on resource type and authenticator.
 * This class leverages the OSGi service registry to find the appropriate
 * providers.
 */
public class DebugProtocolRouter {

    private static final Log LOG = LogFactory.getLog(DebugProtocolRouter.class);

    private DebugProtocolRouter() {

        // Utility class.
    }

    /**
     * Enum representing different debug protocol types.
     * New protocols can be added here or use CUSTOM for dynamic protocol types.
     */
    public enum DebugProtocolType {

        OAUTH2_OIDC("OAuth2/OIDC", "oauth2"),
        GOOGLE("Google", "google"),
        GITHUB("GitHub", "github"),
        SAML("SAML", "saml"),
        CUSTOM("Custom", "custom");

        private final String displayName;
        private final String protocolKey;

        DebugProtocolType(String displayName, String protocolKey) {

            this.displayName = displayName;
            this.protocolKey = protocolKey;
        }

        /**
         * Gets the display name of this protocol type.
         *
         * @return The display name.
         */
        public String getDisplayName() {

            return displayName;
        }

        /**
         * Gets the protocol key for this type.
         *
         * @return The protocol key.
         */
        public String getProtocolKey() {

            return protocolKey;
        }

        /**
         * Finds a DebugProtocolType by display name or protocol key (case-insensitive).
         *
         * @param value The display name or protocol key to match.
         * @return The matching DebugProtocolType, or null if not found.
         */
        public static DebugProtocolType fromValue(String value) {

            if (StringUtils.isBlank(value)) {
                return null;
            }
            for (DebugProtocolType type : values()) {
                if (type.getDisplayName().equalsIgnoreCase(value)
                        || type.getProtocolKey().equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * Detects the debug protocol type for the given connectionId ID.
     * Delegates to registered DebugProtocolResolvers.
     *
     * @param connectionId connection ID or name.
     * @return Detected DebugProtocolType, defaults to OAUTH2_OIDC if detection fails.
     */
    public static DebugProtocolType detectProtocol(String connectionId) {

        if (StringUtils.isEmpty(connectionId)) {
            LOG.debug("Connection ID is empty, defaulting to OAuth2/OIDC");

            return DebugProtocolType.OAUTH2_OIDC;
        }

        List<DebugProtocolResolver> resolvers 
            = DebugFrameworkServiceDataHolder.getInstance().getDebugProtocolResolvers();
        for (DebugProtocolResolver resolver : resolvers) {
            String resolvedProtocol = resolver.resolveProtocol(connectionId);
            if (resolvedProtocol != null) {
                DebugProtocolType type = DebugProtocolType.fromValue(resolvedProtocol);
                if (type != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Protocol resolved by " + resolver.getClass().getSimpleName()
                                    + ": " + type.getDisplayName());
                    }
                    return type;
                }
                // If specific enum not found, we might need a more dynamic way to handle new protocols in future.
                LOG.warn("Resolved protocol string '" + resolvedProtocol + "' doesn't match any DebugProtocolType.");
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("No protocol resolved for resource: " + connectionId + ", defaulting to OAuth2/OIDC");
        }
        return DebugProtocolType.OAUTH2_OIDC;
    }

    /**
     * Gets the context provider for the given connection ID.
     * Automatically detects protocol and returns appropriate provider.
     *
     * @param connectionId connection ID.
     * @return DebugContextProvider for the resource, or null if not available.
     */
    public static DebugContextProvider getContextProviderForResource(String connectionId) {

        return getProtocolProviderComponent(connectionId, DebugProtocolProvider::getContextProvider,
             "Context Provider");
    }

    /**
     * Gets the executor for the given connection ID.
     * Automatically detects protocol and returns appropriate provider.
     *
     * @param connectionId Connection ID.
     * @return DebugExecutor for the resource, or null if not available.
     */
    public static DebugExecutor getExecutorForResource(String connectionId) {

        return getProtocolProviderComponent(connectionId, DebugProtocolProvider::getExecutor, "Executor");
    }

    /**
     * Gets the processor for the given Connection ID.
     * Automatically detects protocol and returns appropriate provider.
     *
     * @param connectionId Connection ID.
     * @return DebugProcessor for the resource, or null if not available.
     */
    public static DebugProcessor getProcessorForResource(String connectionId) {

        return getProtocolProviderComponent(connectionId, DebugProtocolProvider::getProcessor, "Processor");
    }

    /**
     * Gets the debug resource handler for the given resource type.
     * Routes based on explicit resourceType values supported by DebugResourceType.
     *
     * @param resourceType The type of resource to debug.
     * @return DebugResourceHandler instance if available, null otherwise.
     */
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

    public static DebugProtocolType resolveProtocolFromAuthenticator(String authenticatorName) {

        if (StringUtils.isBlank(authenticatorName)) {
            return null;
        }
        if ("GoogleOAuth2Authenticator".equalsIgnoreCase(authenticatorName)
                || "GoogleAuthenticator".equalsIgnoreCase(authenticatorName)
                || "GoogleOIDCAuthenticator".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.GOOGLE;
        }
        if ("GitHubAuthenticator".equalsIgnoreCase(authenticatorName)
                || "GithubAuthenticator".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.GITHUB;
        }
        if ("SAMLSSOAuthenticator".equalsIgnoreCase(authenticatorName)
                || "SAMLAuthenticator".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.SAML;
        }
        if ("OpenIDConnectAuthenticator".equalsIgnoreCase(authenticatorName)
                || "OAuth2OpenIDConnectAuthenticator".equalsIgnoreCase(authenticatorName)
                || "OIDC".equalsIgnoreCase(authenticatorName)
                || "OAuth2".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.OAUTH2_OIDC;
        }
        return null;
    }

    /**
     * Functional interface for extracting a component from a DebugProtocolProvider.
     */
    @FunctionalInterface
    private interface ProviderComponentExtractor<T> {

        T extract(DebugProtocolProvider provider);
    }

    /**
     * Generic helper to get a component from the protocol provider.
     */
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
        DebugProtocolProvider provider = DebugFrameworkServiceDataHolder.getInstance()
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

        DebugProtocolType type = detectProtocol(connectionId);
        if (type != null) {
            DebugProtocolProvider provider = getDebugProtocolProvider(type.getProtocolKey());
            if (provider != null) {
                return provider;
            }
            provider = getDebugProtocolProvider(type.getDisplayName());
            if (provider != null) {
                return provider;
            }
        }

        for (DebugProtocolResolver resolver : DebugFrameworkServiceDataHolder.getInstance()
                .getDebugProtocolResolvers()) {
            String resolvedProtocol = resolver.resolveProtocol(connectionId);
            if (StringUtils.isNotBlank(resolvedProtocol)) {
                DebugProtocolProvider provider = getDebugProtocolProvider(resolvedProtocol);
                if (provider != null) {
                    return provider;
                }
            }
        }
        return null;
    }

    private static String normalizeProtocolType(String protocolType) {

        return protocolType.trim().toLowerCase(Locale.ENGLISH);
    }
}
