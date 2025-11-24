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

    /**
     * Enum representing different debug protocol types and their implementation classes.
     */
    public enum DebugProtocolType {
        OAUTH2_OIDC(
            "OAuth2/OIDC",
            "org.wso2.carbon.identity.application.authenticator.oidc.debug.OAuth2ContextProvider",
            "org.wso2.carbon.identity.application.authenticator.oidc.debug.OAuth2Executer",
            "org.wso2.carbon.identity.application.authenticator.oidc.debug.OAuth2DebugProcessor"
        ),
        GOOGLE(
            "Google",
            "org.wso2.carbon.identity.application.authenticator.google.debug.GoogleContextProvider",
            "org.wso2.carbon.identity.application.authenticator.google.debug.GoogleExecuter",
            "org.wso2.carbon.identity.application.authenticator.google.debug.GoogleDebugProcessor"
        ),
        GITHUB(
            "GitHub",
            "org.wso2.carbon.identity.application.authenticator.github.debug.GitHubContextProvider",
            "org.wso2.carbon.identity.application.authenticator.github.debug.GitHubExecuter",
            "org.wso2.carbon.identity.application.authenticator.github.debug.GitHubDebugProcessor"
        ),
        SAML(
            "SAML",
            "org.wso2.carbon.identity.application.authenticator.saml.debug.SAMLContextProvider",
            null,  // SAML doesn't use DebugExecutor (synchronous flow)
            "org.wso2.carbon.identity.application.authenticator.saml.debug.SAMLDebugProcessor"
        );

        private final String displayName;
        private final String contextProviderClass;
        private final String executorClass;
        private final String processorClass;

        DebugProtocolType(String displayName, String contextProviderClass, 
                         String executorClass, String processorClass) {
            this.displayName = displayName;
            this.contextProviderClass = contextProviderClass;
            this.executorClass = executorClass;
            this.processorClass = processorClass;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getContextProviderClass() {
            return contextProviderClass;
        }

        public String getExecutorClass() {
            return executorClass;
        }

        public String getProcessorClass() {
            return processorClass;
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

        try {
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            String tenantDomain = IdentityTenantUtil.resolveTenantDomain();
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

            if (resource == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Resource not found: " + resourceId + ", defaulting to OAuth2/OIDC");
                }
                return DebugProtocolType.OAUTH2_OIDC;
            }

            try {
                return detectProtocolFromIdP(resource);
            } catch (NullPointerException npe) {
                LOG.warn("Resource configuration is corrupted (null federated authenticator configs) for resource: " + resourceId +
                        ". Defaulting to OAuth2/OIDC. This may indicate data integrity issues.", npe);
                return DebugProtocolType.OAUTH2_OIDC;
            }

        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error detecting protocol for resource " + resourceId + ": " + e.getMessage());
            }
            return DebugProtocolType.OAUTH2_OIDC;  // Default fallback
        }
    }

    /**
     * Detects protocol type from resource configuration by analyzing authenticator names.
     * Handles cases where resource authenticator configs may be null due to data corruption.
     *
     * @param resource Resource configuration (IdentityProvider object).
     * @return Detected DebugProtocolType.
     */
    private static DebugProtocolType detectProtocolFromIdP(IdentityProvider resource) {
        // Defensive check: Resource itself should not be null, but add explicit check
        if (resource == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resource configuration is null, defaulting to OAuth2/OIDC");
            }
            return DebugProtocolType.OAUTH2_OIDC;
        }
        
        FederatedAuthenticatorConfig[] configs = resource.getFederatedAuthenticatorConfigs();
        
        // Handle case where authenticator configs are null or empty
        // This can happen if the resource was not fully initialized or has corrupted data
        if (configs == null || configs.length == 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No authenticators found for resource: " + resource.getIdentityProviderName() + 
                         ", defaulting to OAuth2/OIDC");
            }
            return DebugProtocolType.OAUTH2_OIDC;
        }

        // Check each enabled authenticator
        for (FederatedAuthenticatorConfig config : configs) {
            if (config == null || !config.isEnabled()) {
                continue;
            }

            String authName = config.getName();
            if (StringUtils.isEmpty(authName)) {
                continue;
            }

            DebugProtocolType type = detectProtocolFromAuthenticator(authName);
            if (type != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Detected protocol: " + type.getDisplayName() + 
                             " for authenticator: " + authName);
                }
                return type;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("No recognized authenticator found for resource: " + resource.getIdentityProviderName() + 
                     ", defaulting to OAuth2/OIDC");
        }
        return DebugProtocolType.OAUTH2_OIDC;
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
     * Gets the context provider class name for the given protocol type.
     *
     * @param protocolType DebugProtocolType.
     * @return Fully qualified context provider class name.
     */
    public static String getContextProviderClass(DebugProtocolType protocolType) {
        return protocolType.getContextProviderClass();
    }

    /**
     * Gets the executor class name for the given protocol type.
     * Returns null for synchronous protocols like SAML.
     *
     * @param protocolType DebugProtocolType.
     * @return Fully qualified executor class name, or null if not applicable.
     */
    public static String getExecutorClass(DebugProtocolType protocolType) {
        return protocolType.getExecutorClass();
    }

    /**
     * Gets the processor class name for the given protocol type.
     *
     * @param protocolType DebugProtocolType.
     * @return Fully qualified processor class name.
     */
    public static String getProcessorClass(DebugProtocolType protocolType) {
        return protocolType.getProcessorClass();
    }

    /**
     * Gets the context provider class for the given resource ID.
     * Automatically detects protocol and returns appropriate class.
     *
     * @param resourceId Resource ID.
     * @return Fully qualified context provider class name.
     */
    public static String getContextProviderClassForResource(String resourceId) {
        DebugProtocolType type = detectProtocol(resourceId);
        return getContextProviderClass(type);
    }

    /**
     * Gets the executor class for the given resource ID.
     * Automatically detects protocol and returns appropriate class.
     *
     * @param resourceId Resource ID.
     * @return Fully qualified executor class name, or null if not applicable.
     */
    public static String getExecutorClassForResource(String resourceId) {
        DebugProtocolType type = detectProtocol(resourceId);
        return getExecutorClass(type);
    }

    /**
     * Gets the processor class for the given resource ID.
     * Automatically detects protocol and returns appropriate class.
     *
     * @param resourceId Resource ID.
     * @return Fully qualified processor class name.
     */
    public static String getProcessorClassForResource(String resourceId) {
        DebugProtocolType type = detectProtocol(resourceId);
        return getProcessorClass(type);
    }

    /**
     * Loads and instantiates the context provider for the given resource ID.
     * Falls back to OAuth2ContextProvider if protocol-specific provider is not available.
     *
     * @param resourceId Resource ID.
     * @return Context provider instance, or null if not available.
     */
    public static Object getContextProviderForResource(String resourceId) {
        String className = getContextProviderClassForResource(resourceId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading context provider class: " + className + " for resource: " + resourceId);
        }
        Object provider = loadServiceByClass(className);
        if (provider != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully loaded context provider: " + provider.getClass().getName());
            }
            return provider;
        }
        
        // Fallback: Try OAuth2 provider if detected protocol provider is not available
        DebugProtocolType detectedType = detectProtocol(resourceId);
        if (detectedType != DebugProtocolType.OAUTH2_OIDC) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Protocol-specific context provider not available for " + detectedType.getDisplayName() + 
                         ", falling back to OAuth2/OIDC provider");
            }
            String fallbackClassName = DebugProtocolType.OAUTH2_OIDC.getContextProviderClass();
            Object fallbackProvider = loadServiceByClass(fallbackClassName);
            if (fallbackProvider != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Loaded fallback context provider: " + fallbackProvider.getClass().getName());
                }
                return fallbackProvider;
            }
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Context provider not available for resource: " + resourceId);
        }
        return null;
    }

    /**
     * Loads and instantiates the executor for the given resource ID.
     * Falls back to OAuth2Executer if protocol-specific executor is not available.
     *
     * @param resourceId resource ID.
     * @return Executor instance, or null if not available or not applicable.
     */
    public static Object getExecutorForResource(String resourceId) {
        String className = getExecutorClassForResource(resourceId);
        if (className == null) {
            return null;
        }
        Object executor = loadServiceByClass(className);
        if (executor != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully loaded executor: " + executor.getClass().getName());
            }
            return executor;
        }
        
        // Fallback: Try OAuth2 executor if detected protocol executor is not available
        DebugProtocolType detectedType = detectProtocol(resourceId);
        if (detectedType != DebugProtocolType.OAUTH2_OIDC) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Protocol-specific executor not available for " + detectedType.getDisplayName() + 
                         ", falling back to OAuth2 executor");
            }
            String fallbackClassName = DebugProtocolType.OAUTH2_OIDC.getExecutorClass();
            Object fallbackExecutor = loadServiceByClass(fallbackClassName);
            if (fallbackExecutor != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Successfully loaded fallback executor: " + fallbackExecutor.getClass().getName());
                }
                return fallbackExecutor;
            }
        }
        return null;
    }

    /**
     * Loads and instantiates the processor for the given resource ID.
     * Falls back to OAuth2DebugProcessor if protocol-specific processor is not available.
     *
     * @param resourceId Resource ID.
     * @return Processor instance, or null if not available.
     */
    public static Object getProcessorForResource(String resourceId) {
        String className = getProcessorClassForResource(resourceId);
        Object processor = loadServiceByClass(className);
        if (processor != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully loaded processor: " + processor.getClass().getName());
            }
            return processor;
        }
        
        // Fallback: Try OAuth2 processor if detected protocol processor is not available
        DebugProtocolType detectedType = detectProtocol(resourceId);
        if (detectedType != DebugProtocolType.OAUTH2_OIDC) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Protocol-specific processor not available for " + detectedType.getDisplayName() + 
                         ", falling back to OAuth2 processor");
            }
            String fallbackClassName = DebugProtocolType.OAUTH2_OIDC.getProcessorClass();
            Object fallbackProcessor = loadServiceByClass(fallbackClassName);
            if (fallbackProcessor != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Successfully loaded fallback processor: " + fallbackProcessor.getClass().getName());
                }
                return fallbackProcessor;
            }
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Resource type is empty, unable to route debug request");
            }
            return null;
        }

        try {
            // Route based on resource type
            String resourceTypeUpper = resourceType.toUpperCase().trim();
            
            if ("IDP".equals(resourceTypeUpper) || "IDENTITY_PROVIDER".equals(resourceTypeUpper) || "RESOURCE".equals(resourceTypeUpper)) {
                // For resource resource type, load the resource debug resource handler
                String handlerClassName = "org.wso2.carbon.identity.debug.framework.handler.IdpDebugResourceHandler";
                Object handler = loadServiceByClass(handlerClassName);
                if (handler != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Loaded resource debug resource handler");
                    }
                    return handler;
                }
            } else if ("APPLICATION".equals(resourceTypeUpper)) {
                // For APPLICATION resource type, load the application debug resource handler
                String handlerClassName = "org.wso2.carbon.identity.debug.framework.handler.ApplicationDebugResourceHandler";
                Object handler = loadServiceByClass(handlerClassName);
                if (handler != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Loaded APPLICATION debug resource handler");
                    }
                    return handler;
                }
            } else if ("CONNECTOR".equals(resourceTypeUpper)) {
                // For CONNECTOR resource type, load the connector debug resource handler
                String handlerClassName = "org.wso2.carbon.identity.debug.framework.handler.ConnectorDebugResourceHandler";
                Object handler = loadServiceByClass(handlerClassName);
                if (handler != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Loaded CONNECTOR debug resource handler");
                    }
                    return handler;
                }
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("No handler available for resource type: " + resourceType);
            }
            return null;

        } catch (Exception e) {
            LOG.error("Error getting debug resource handler for resource type: " + resourceType + 
                     ", error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Loads a service implementation by class name using reflection.
     *
     * @param className Fully qualified class name.
     * @return Service instance, or null if not available.
     */
    private static Object loadServiceByClass(String className) {
        if (StringUtils.isEmpty(className)) {
            return null;
        }

        try {
            Class<?> serviceClass = Class.forName(className);
            Object instance = serviceClass.getDeclaredConstructor().newInstance();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Loaded service: " + className);
            }
            return instance;

        } catch (ClassNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Service class not found: " + className + 
                    " (This is OK if the protocol connector is not deployed)");
            }
        } catch (Exception e) {
            LOG.error("Error instantiating service " + className + ": " + e.getMessage(), e);
        }
        return null;
    }
}
