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

package org.wso2.carbon.identity.debug.framework.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.debug.framework.extension.DebugContextProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugExecutor;
import org.wso2.carbon.identity.debug.framework.extension.DebugProcessor;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.internal.DebugFrameworkServiceDataHolder;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.Collection;
import java.util.Collections;

/**
 * Routes debug requests to appropriate protocol-specific context providers and
 * processors based on resource type and authenticator.
 * This class leverages the OSGi service registry to find the appropriate
 * providers.
 */
public class DebugProtocolRouter {

    private static final Log LOG = LogFactory.getLog(DebugProtocolRouter.class);
    private static final String PROTOCOL_INFO_LOG = ", protocol: ";

    /**
     * Enum representing different debug protocol types.
     */
    public enum DebugProtocolType {

        OAUTH2_OIDC("OAuth2/OIDC", "oauth2"),
        GOOGLE("Google", "google"),
        GITHUB("GitHub", "github"),
        SAML("SAML", "saml");

        private final String displayName;
        private final String protocolKey;

        DebugProtocolType(String displayName, String protocolKey) {

            this.displayName = displayName;
            this.protocolKey = protocolKey;
        }

        public String getDisplayName() {

            return displayName;
        }

        public String getProtocolKey() {

            return protocolKey;
        }
    }

    /**
     * Detects the debug protocol type for the given resource ID.
     * Loads the resource configuration and identifies the authenticator type.
     *
     * @param resourceId Resource ID or name.
     * @return Detected DebugProtocolType, defaults to OAUTH2_OIDC if detection fails.
     */
    public static DebugProtocolType detectProtocol(String resourceId) {

        if (StringUtils.isEmpty(resourceId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resource ID is empty, defaulting to OAuth2/OIDC");
            }
            return DebugProtocolType.OAUTH2_OIDC;
        }

        return detectProtocolWithFallback(resourceId);
    }

    /**
     * Gets the context provider for the given resource ID.
     * Automatically detects protocol and returns appropriate provider.
     *
     * @param resourceId Resource ID.
     * @return DebugContextProvider for the resource, or null if not available.
     */
    public static DebugContextProvider getContextProviderForResource(String resourceId) {

        return getProtocolProviderComponent(resourceId, DebugProtocolProvider::getContextProvider, "Context Provider");
    }

    /**
     * Gets the executor for the given resource ID.
     * Automatically detects protocol and returns appropriate provider.
     *
     * @param resourceId Resource ID.
     * @return DebugExecutor for the resource, or null if not available.
     */
    public static DebugExecutor getExecutorForResource(String resourceId) {

        return getProtocolProviderComponent(resourceId, DebugProtocolProvider::getExecutor, "Executor");
    }

    /**
     * Gets the processor for the given resource ID.
     * Automatically detects protocol and returns appropriate provider.
     *
     * @param resourceId Resource ID.
     * @return DebugProcessor for the resource, or null if not available.
     */
    public static DebugProcessor getProcessorForResource(String resourceId) {

        // Cast to DebugProcessor is handled by the generic helper.
        return (DebugProcessor) getProtocolProviderComponent(resourceId, DebugProtocolProvider::getProcessor,
                "Processor");
    }

    /**
     * Gets the debug resource handler for the given resource type.
     * Routes based on resourceType (e.g., "resource", "APPLICATION", "CONNECTOR").
     *
     * @param resourceType The type of resource to debug.
     * @return DebugResourceHandler instance if available, null otherwise.
     */
    public static Object getDebugResourceHandler(String resourceType) {

        if (StringUtils.isEmpty(resourceType)) {
            logDebug("Resource type is empty, unable to route debug request");
            return null;
        }

        try {
            String normalizedType = normalizeResourceType(resourceType);
            if (isIdpResourceType(normalizedType)) {
                return createIdpDebugHandler();
            }

            logDebug("No handler available for the resource type.");
            return null;

        } catch (Exception e) {
            LOG.error("Error getting debug resource handler.", e);
            return null;
        }
    }

    /**
     * Gets all registered protocol types.
     * Used for diagnostics and logging.
     *
     * @return Collection of registered protocol type names.
     */
    public static Collection<String> getAllRegisteredProtocolTypes() {

        try {
            return DebugFrameworkServiceDataHolder.getInstance()
                    .getAllDebugProtocolProviders().keySet();
        } catch (Exception e) {
            LOG.error("Error retrieving registered protocol types: " + e.getMessage(), e);
            return Collections.emptySet();
        }
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
    private static <T> T getProtocolProviderComponent(String resourceId, ProviderComponentExtractor<T> extractor,
            String componentName) {

        DebugProtocolType type = detectProtocol(resourceId);
        DebugProtocolProvider provider = getDebugProtocolProvider(type.getDisplayName());

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

    private static DebugProtocolType detectProtocolWithFallback(String resourceId) {

        try {
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
            IdentityProvider resource = loadResourceConfiguration(idpManager, resourceId, tenantDomain);

            if (resource == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Resource not found, defaulting to OAuth2/OIDC");
                }
                return DebugProtocolType.OAUTH2_OIDC;
            }

            return detectProtocolWithCorruptionHandling(resource, resourceId);

        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error detecting protocol for resource.");
            }
            return DebugProtocolType.OAUTH2_OIDC; // Default fallback.
        }
    }

    private static DebugProtocolType detectProtocolWithCorruptionHandling(IdentityProvider resource,
            String resourceId) {

        try {
            return detectProtocolFromIdP(resource);
        } catch (NullPointerException npe) {
            LOG.warn("Resource configuration is corrupted. This may indicate data integrity issues.", npe);
            return DebugProtocolType.OAUTH2_OIDC;
        }
    }

    private static IdentityProvider loadResourceConfiguration(IdentityProviderManager idpManager,
            String resourceId, String tenantDomain) throws IdentityProviderManagementException {

        IdentityProvider resource = null;

        // Try to get by resource ID first.
        try {
            resource = idpManager.getIdPByResourceId(resourceId, tenantDomain, true);
        } catch (IdentityProviderManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resource not found by ID, trying by name: " + resourceId);
            }
            // Try by name.
            resource = idpManager.getIdPByName(resourceId, tenantDomain);
        }

        return resource;
    }

    private static DebugProtocolType detectProtocolFromIdP(IdentityProvider resource) {

        if (resource == null) {
            logDebug("Resource configuration is null");
            return DebugProtocolType.OAUTH2_OIDC;
        }

        FederatedAuthenticatorConfig[] configs = resource.getFederatedAuthenticatorConfigs();
        if (!hasValidAuthenticators(configs)) {
            logDebug("No authenticators found for resource: " + resource.getIdentityProviderName());
            return DebugProtocolType.OAUTH2_OIDC;
        }

        DebugProtocolType detectedType = findProtocolFromAuthenticators(configs);
        if (detectedType != null) {
            return detectedType;
        }

        logDebug("No recognized authenticator found for resource: " + resource.getIdentityProviderName());
        return DebugProtocolType.OAUTH2_OIDC;
    }

    private static boolean hasValidAuthenticators(FederatedAuthenticatorConfig[] configs) {

        return configs != null && configs.length > 0;
    }

    private static DebugProtocolType findProtocolFromAuthenticators(FederatedAuthenticatorConfig[] configs) {

        for (FederatedAuthenticatorConfig config : configs) {
            if (isValidAuthenticatorConfig(config)) {
                String authName = config.getName();
                DebugProtocolType type = detectProtocolFromAuthenticator(authName);
                if (type != null) {
                    logDebug("Detected protocol: " + type.getDisplayName() + " for authenticator: " + authName);
                    return type;
                }
            }
        }
        return null;
    }

    private static boolean isValidAuthenticatorConfig(FederatedAuthenticatorConfig config) {

        return config != null && config.isEnabled() && !StringUtils.isEmpty(config.getName());
    }

    private static DebugProtocolType detectProtocolFromAuthenticator(String authenticatorName) {

        if (StringUtils.isEmpty(authenticatorName)) {
            return null;
        }

        // Check for Google authenticators.
        if ("GoogleOAuth2Authenticator".equalsIgnoreCase(authenticatorName) ||
                "GoogleAuthenticator".equalsIgnoreCase(authenticatorName) ||
                "GoogleOIDCAuthenticator".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.GOOGLE;
        }

        // Check for GitHub authenticators.
        if ("GitHubAuthenticator".equalsIgnoreCase(authenticatorName) ||
                "GithubAuthenticator".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.GITHUB;
        }

        // Check for SAML authenticators.
        if ("SAMLSSOAuthenticator".equalsIgnoreCase(authenticatorName) ||
                "SAMLAuthenticator".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.SAML;
        }

        // Check for OIDC/OAuth2 authenticators.
        if ("OpenIDConnectAuthenticator".equalsIgnoreCase(authenticatorName) ||
                "OAuth2OpenIDConnectAuthenticator".equalsIgnoreCase(authenticatorName) ||
                "OIDC".equalsIgnoreCase(authenticatorName) ||
                "OAuth2".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.OAUTH2_OIDC;
        }

        return null;
    }

    private static String normalizeResourceType(String resourceType) {

        return resourceType.toUpperCase().trim();
    }

    private static boolean isIdpResourceType(String normalizedType) {

        return "IDP".equals(normalizedType) || "IDENTITY_PROVIDER".equals(normalizedType)
                || "RESOURCE".equals(normalizedType);
    }

    private static Object createIdpDebugHandler() {

        logDebug("Loading IdP debug resource handler from registry");
        DebugResourceHandler idpHandler = org.wso2.carbon.identity.debug.framework.registry.DebugHandlerRegistry
                .getInstance()
                .getHandler("idp");
        if (idpHandler == null) {
            logDebug("IdP debug handler not registered. Ensure org.wso2.carbon.identity.debug.idp bundle is deployed.");
        }
        return idpHandler;
    }

    private static DebugProtocolProvider getDebugProtocolProvider(String protocolType) {

        if (StringUtils.isEmpty(protocolType)) {
            return null;
        }

        try {
            DebugProtocolProvider provider = DebugFrameworkServiceDataHolder.getInstance()
                    .getDebugProtocolProvider(protocolType);

            if (provider != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Retrieved protocol provider for type: " + protocolType);
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Protocol provider not found for type: " + protocolType);
            }
            return provider;

        } catch (Exception e) {
            LOG.error("Error retrieving protocol provider for type: " + protocolType + ": " + e.getMessage(), e);
        }
        return null;
    }

    private static void logDebug(String message) {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
    }
}
