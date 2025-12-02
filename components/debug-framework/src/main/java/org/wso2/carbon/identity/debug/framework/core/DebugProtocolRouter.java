/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
import org.wso2.carbon.identity.debug.framework.core.handler.IdpDebugResourceHandler;
import org.wso2.carbon.identity.debug.framework.internal.DebugFrameworkServiceDataHolder;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

/**
 * Routes debug requests to appropriate protocol-specific context providers and processors
 * based on resource type and authenticator.
 * 
 * Supports routing for:
 * - OAuth2/OIDC (OpenIDConnectAuthenticator)
 * - Google (GoogleOAuth2Authenticator, GoogleAuthenticator)
 * - GitHub (GitHubAuthenticator)
 * - SAML (SAMLAuthenticator)
 * - Custom OAuth2 implementations
 */

public class DebugProtocolRouter {

    private static final Log LOG = LogFactory.getLog(DebugProtocolRouter.class);
    private static final String PROTOCOL_INFO_LOG = ", protocol: ";

    /**
     * Enum representing different debug protocol types.
     * Class names are derived dynamically using naming conventions.
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

        /**
         * Gets the context provider class name using naming convention.
         * Returns null if class cannot be determined.
         *
         * @return Fully qualified class name or null.
         */
        public String getContextProviderClass() {

            return resolveDebugClass("ContextProvider");
        }

        /**
         * Gets the executor class name using naming convention.
         * Returns null if class cannot be determined.
         *
         * @return Fully qualified class name or null.
         */
        public String getExecutorClass() {

            return resolveDebugClass("Executer");
        }

        /**
         * Gets the processor class name using naming convention.
         *
         * @return Fully qualified class name or null.
         */
        public String getProcessorClass() {

            return resolveDebugClass("DebugProcessor");
        }

        /**
         * Resolves the fully qualified class name using naming convention.
         *
         * @param componentType Component type (e.g., "ContextProvider", "Executer", "DebugProcessor").
         * @return Fully qualified class name.
         */
        private String resolveDebugClass(String componentType) {

            if (this == OAUTH2_OIDC) {
                // OAuth2/OIDC is in the OIDC module
                return "org.wso2.carbon.identity.application.authenticator.oidc.debug.OAuth2" + componentType;
            }

            // For other protocols: capitalize the protocol key and construct the class name
            String capitalizedProtocol = capitalizeFirst(protocolKey);
            return String.format("org.wso2.carbon.identity.application.authenticator.%s.debug.%s%s",
                    protocolKey.toLowerCase(), capitalizedProtocol, componentType);
        }

        /**
         * Capitalizes the first letter of a string.
         *
         * @param str String to capitalize.
         * @return Capitalized string.
         */
        private String capitalizeFirst(String str) {

            if (StringUtils.isEmpty(str)) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1);
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
                LOG.debug("Resource ID is empty");
            }
            return DebugProtocolType.OAUTH2_OIDC;
        }

        return detectProtocolWithFallback(resourceId);
    }

    /**
     * Detects protocol type for a resource ID with fallback to OAuth2/OIDC on error.
     * Loads the resource configuration and identifies the authenticator type.
     * Provides comprehensive error handling for resource loading and configuration issues.
     *
     * @param resourceId Resource ID or name.
     * @return Detected DebugProtocolType, defaults to OAUTH2_OIDC if detection fails.
     */
    private static DebugProtocolType detectProtocolWithFallback(String resourceId) {

        try {
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
            IdentityProvider resource = loadResourceConfiguration(idpManager, resourceId, tenantDomain);

            if (resource == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Resource not found: " + resourceId);
                }
                return DebugProtocolType.OAUTH2_OIDC;
            }

            return detectProtocolWithCorruptionHandling(resource, resourceId);

        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error detecting protocol for resource " + resourceId + ": " + e.getMessage());
            }
            return DebugProtocolType.OAUTH2_OIDC;  // Default fallback
        }
    }

    /**
     * Detects protocol from IdP configuration with corruption handling.
     * Handles NullPointerException that may occur due to corrupted resource configuration.
     *
     * @param resource The resource.
     * @param resourceId The resource ID (used for logging).
     * @return Detected DebugProtocolType, defaults to OAUTH2_OIDC if corruption is detected.
     */
    private static DebugProtocolType detectProtocolWithCorruptionHandling(IdentityProvider resource, 
            String resourceId) {

        try {
            return detectProtocolFromIdP(resource);
        } catch (NullPointerException npe) {
            LOG.warn("Resource configuration is corrupted for resource: " + resourceId +
                    ". This may indicate data integrity issues.", npe);
            return DebugProtocolType.OAUTH2_OIDC;
        }
    }

    /**
     * Loads the resource configuration by ID or name with fallback.
     * Attempts to load by resource ID first, then by name on failure.
     *
     * @param idpManager IdentityProviderManager instance.
     * @param resourceId Resource ID or name.
     * @param tenantDomain Tenant domain.
     * @return IdentityProvider resource configuration, or null if not found.
     * @throws IdentityProviderManagementException If loading by name fails.
     */
    private static IdentityProvider loadResourceConfiguration(IdentityProviderManager idpManager, 
            String resourceId, String tenantDomain) throws IdentityProviderManagementException {

        IdentityProvider resource = null;

        // Try to get by resource ID first
        try {
            resource = idpManager.getIdPByResourceId(resourceId, tenantDomain, true);
        } catch (IdentityProviderManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resource not found by ID, trying by name: " + resourceId);
            }
            // Try by name
            resource = idpManager.getIdPByName(resourceId, tenantDomain);
        }

        return resource;
    }

    /**
     * Detects protocol type from resource configuration by analyzing authenticator names.
     * Handles cases where resource authenticator configs may be null due to data corruption.
     *
     * @param resource Resource configuration (IdentityProvider object).
     * @return Detected DebugProtocolType.
     */
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

    /**
     * Validates if authenticator configurations are available.
     *
     * @param configs Authenticator configurations array.
     * @return true if configs is not null and not empty.
     */
    private static boolean hasValidAuthenticators(FederatedAuthenticatorConfig[] configs) {

        return configs != null && configs.length > 0;
    }

    /**
     * Finds protocol type from enabled authenticators in the configuration array.
     *
     * @param configs Authenticator configurations array.
     * @return Detected DebugProtocolType or null if not found.
     */
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

    /**
     * Validates if an authenticator configuration is valid and enabled.
     *
     * @param config Authenticator configuration to validate.
     * @return true if config is not null, enabled, and has a valid name.
     */
    private static boolean isValidAuthenticatorConfig(FederatedAuthenticatorConfig config) {

        return config != null && config.isEnabled() && !StringUtils.isEmpty(config.getName());
    }

    /**
     * Logs a debug message if debug logging is enabled.
     *
     * @param message Message to log.
     */
    private static void logDebug(String message) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
    }

    /**
     * Detects protocol type from authenticator name.
     *
     * @param authenticatorName Name of the authenticator.
     * @return Detected DebugProtocolType or null if not recognized.
     */
    private static DebugProtocolType detectProtocolFromAuthenticator(String authenticatorName) {

        if (StringUtils.isEmpty(authenticatorName)) {
            return null;
        }

        // Check for Google authenticators
        if ("GoogleOAuth2Authenticator".equalsIgnoreCase(authenticatorName) ||
            "GoogleAuthenticator".equalsIgnoreCase(authenticatorName) ||
            "GoogleOIDCAuthenticator".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.GOOGLE;
        }

        // Check for GitHub authenticators
        if ("GitHubAuthenticator".equalsIgnoreCase(authenticatorName) ||
            "GithubAuthenticator".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.GITHUB;
        }

        // Check for SAML authenticators
        if ("SAMLSSOAuthenticator".equalsIgnoreCase(authenticatorName) ||
            "SAMLAuthenticator".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.SAML;
        }

        // Check for OIDC/OAuth2 authenticators (must be after others since it's the fallback)
        if ("OpenIDConnectAuthenticator".equalsIgnoreCase(authenticatorName) ||
            "OAuth2OpenIDConnectAuthenticator".equalsIgnoreCase(authenticatorName) ||
            "OIDC".equalsIgnoreCase(authenticatorName) ||
            "OAuth2".equalsIgnoreCase(authenticatorName)) {
            return DebugProtocolType.OAUTH2_OIDC;
        }

        return null;
    }

    /**
     * Gets the context provider for the given resource ID.
     * Automatically detects protocol and returns appropriate provider.
     *
     * @param resourceId Resource ID.
     * @return DebugContextProvider for the resource, or null if not available.
     */
    public static DebugContextProvider getContextProviderForResource(String resourceId) {

        DebugProtocolType type = detectProtocol(resourceId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Detected protocol type for resource " + resourceId + ": " + type.getDisplayName() 
                    + " (key: " + type.getProtocolKey() + ")");
        }
        DebugProtocolProvider provider = getDebugProtocolProvider(type.getDisplayName());
        
        if (provider != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting context provider for resource: " + resourceId + PROTOCOL_INFO_LOG 
                        + type.getDisplayName());
            }
            return provider.getContextProvider();
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Context provider not available for resource: " + resourceId);
        }
        return null;
    }

    /**
     * Gets the executor for the given resource ID.
     * Automatically detects protocol and returns appropriate provider.
     *
     * @param resourceId Resource ID.
     * @return DebugExecutor for the resource, or null if not available or not applicable.
     */
    public static DebugExecutor getExecutorForResource(String resourceId) {

        DebugProtocolType type = detectProtocol(resourceId);
        DebugProtocolProvider provider = getDebugProtocolProvider(type.getDisplayName());
        
        if (provider != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting executor for resource: " + resourceId + PROTOCOL_INFO_LOG + type.getDisplayName());
            }
            return provider.getExecutor();
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executor not available for resource: " + resourceId);
        }
        return null;
    }

    /**
     * Gets the processor for the given resource ID.
     * Automatically detects protocol and returns appropriate provider.
     *
     * @param resourceId Resource ID.
     * @return DebugProcessor for the resource, or null if not available.
     */
    public static DebugProcessor getProcessorForResource(String resourceId) {

        DebugProtocolType type = detectProtocol(resourceId);
        DebugProtocolProvider provider = getDebugProtocolProvider(type.getDisplayName());
        
        if (provider != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting processor for resource: " + resourceId + PROTOCOL_INFO_LOG + type.getDisplayName());
            }
            return provider.getProcessor();
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Processor not available for resource: " + resourceId);
        }
        return null;
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
            
            logDebug("No handler available for resource type: " + resourceType);
            return null;

        } catch (Exception e) {
            LOG.error("Error getting debug resource handler for resource type: " + resourceType + 
                     ", error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Normalizes the resource type by converting to uppercase and trimming whitespace.
     *
     * @param resourceType The raw resource type string.
     * @return Normalized resource type in uppercase.
     */
    private static String normalizeResourceType(String resourceType) {

        return resourceType.toUpperCase().trim();
    }

    /**
     * Checks if the normalized resource type represents an identity provider resource.
     *
     * @param normalizedType The normalized resource type in uppercase.
     * @return true if the type matches IDP, IDENTITY_PROVIDER, or RESOURCE.
     */
    private static boolean isIdpResourceType(String normalizedType) {

        return "IDP".equals(normalizedType) || "IDENTITY_PROVIDER".equals(normalizedType) 
                || "RESOURCE".equals(normalizedType);
    }

    /**
     * Creates and returns an IDP debug resource handler instance.
     *
     * @return IdpDebugResourceHandler instance.
     */
    private static Object createIdpDebugHandler() {

        logDebug("Loaded resource debug resource handler");
        return new IdpDebugResourceHandler();
    }

    /**
     * Loads a service implementation by protocol type using OSGi service discovery.
     * Replaces reflection-based Class.forName with OSGi service lookups.
     *
     * @param protocolType Protocol type (e.g., "OAUTH2_OIDC").
     * @return DebugProtocolProvider for the protocol, or null if not registered.
     */
    private static DebugProtocolProvider getDebugProtocolProvider(String protocolType) {

        if (StringUtils.isEmpty(protocolType)) {
            return null;
        }

        try {
            DebugProtocolProvider provider = DebugFrameworkServiceDataHolder.getInstance()
                    .getDebugProtocolProvider(protocolType);
            
            if (provider != null && LOG.isDebugEnabled()) {
                LOG.debug("Retrieved protocol provider for type: " + protocolType);
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Protocol provider not found for type: " + protocolType 
                        + ". Available providers: " 
                        + DebugFrameworkServiceDataHolder.getInstance().getAllDebugProtocolProviders().keySet());
            }
            return provider;

        } catch (Exception e) {
            LOG.error("Error retrieving protocol provider for type: " + protocolType + ": " 
                    + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets all registered protocol types.
     * Used for diagnostics and logging.
     *
     * @return Collection of registered protocol type names.
     */
    public static java.util.Collection<String> getAllRegisteredProtocolTypes() {

        try {
            return DebugFrameworkServiceDataHolder.getInstance()
                    .getAllDebugProtocolProviders().keySet();
        } catch (Exception e) {
            LOG.error("Error retrieving registered protocol types: " + e.getMessage(), e);
            return java.util.Collections.emptySet();
        }
    }
}
