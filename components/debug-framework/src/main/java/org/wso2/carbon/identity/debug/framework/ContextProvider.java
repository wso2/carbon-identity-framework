package org.wso2.carbon.identity.debug.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.IdpManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.lang.reflect.Method;

/**
 * Provides context for debug authentication flows.
 * Centralizes context creation logic and handles IdP management integration.
 * This is the proper place for business logic related to context setup.
 */
public class ContextProvider {

    private static final Log LOG = LogFactory.getLog(ContextProvider.class);
    private static final String DEBUG_SERVICE_PROVIDER_NAME = "DFDP_DEBUG_SP";
    private static final String DEBUG_TENANT_DOMAIN = "carbon.super";

    /**
     * Creates and configures an AuthenticationContext for debug flows.
     * Uses IdP-mgt to get the IdP object and sets up context with IdP id and other relevant data.
     *
     * @param request HttpServletRequest containing debug parameters.
     * @param idpId Identity Provider ID for debug flow.
     * @param authenticatorName Name of the authenticator to use.
     * @return Configured AuthenticationContext.
     */
    public AuthenticationContext provideContext(HttpServletRequest request, String idpId, String authenticatorName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating context for debug flow with IdP ID: " + idpId + " and authenticator: " + authenticatorName);
        }

        AuthenticationContext context = new AuthenticationContext();
        
        try {
            // Use IdP-mgt to get the IdP object.
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            IdentityProvider idp = null;
            
            if (idpId != null) {
                try {
                    // First try to get by resource ID.
                    idp = idpManager.getIdPByResourceId(idpId, DEBUG_TENANT_DOMAIN, true);
                } catch (IdentityProviderManagementException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Failed to get IdP by resource ID, trying by name: " + e.getMessage());
                    }
                    // If not found by resource ID, try by name.
                    idp = idpManager.getIdPByName(idpId, DEBUG_TENANT_DOMAIN);
                }
            }
            
            if (idp != null) {
                // Set IdP-specific context properties.
                // Note: setExternalIdP expects ExternalIdPConfig, not IdentityProvider
                // context.setExternalIdP(idp);
                context.setProperty("DEBUG_IDP_NAME", idp.getIdentityProviderName());
                context.setProperty("DEBUG_IDP_RESOURCE_ID", idp.getResourceId());
                context.setProperty("DEBUG_IDP_DESCRIPTION", idp.getIdentityProviderDescription());
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Successfully configured context with IdP: " + idp.getIdentityProviderName());
                }
            } else {
                LOG.warn("IdP not found for ID: " + idpId + ". Continuing with basic debug context.");
            }
            
        } catch (IdentityProviderManagementException e) {
            LOG.error("Error retrieving IdP for debug context setup: " + e.getMessage(), e);
            // Continue with basic context setup even if IdP retrieval fails.
        }
        
        // Set debug-specific properties.
        context.setRequestType("DFDP_DEBUG");
        context.setCallerSessionKey(java.util.UUID.randomUUID().toString());
        context.setTenantDomain(DEBUG_TENANT_DOMAIN);
        context.setRelyingParty(DEBUG_SERVICE_PROVIDER_NAME);
        context.setProperty("isDebugFlow", Boolean.TRUE);
        context.setProperty("DEBUG_AUTHENTICATOR_NAME", authenticatorName);
        context.setProperty("DEBUG_SESSION_ID", java.util.UUID.randomUUID().toString());
        context.setProperty("DEBUG_TIMESTAMP", System.currentTimeMillis());
        
        // Set request-specific context if available.
        if (request != null) {
            context.setProperty("DEBUG_REQUEST_URI", request.getRequestURI());
            context.setProperty("DEBUG_REMOTE_ADDR", request.getRemoteAddr());
            context.setProperty("DEBUG_USER_AGENT", request.getHeader("User-Agent"));
            
            // Extract session data key if present.
            String sessionDataKey = request.getParameter("sessionDataKey");
            if (sessionDataKey != null) {
                context.setContextIdentifier(sessionDataKey);
            } else {
                context.setContextIdentifier("debug-" + java.util.UUID.randomUUID().toString());
            }
        } else {
            context.setContextIdentifier("debug-" + java.util.UUID.randomUUID().toString());
        }
        
        // Configure debug service provider context.
        try {
            configureDebugServiceProvider(context);
        } catch (Exception e) {
            LOG.error("Error configuring debug service provider context: " + e.getMessage(), e);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug context created successfully with context identifier: " + context.getContextIdentifier());
        }
        
        return context;
    }

    /**
     * Creates and configures an AuthenticationContext for debug flows (backward compatibility).
     *
     * @param request HttpServletRequest.
     * @return Configured AuthenticationContext.
     */
    public AuthenticationContext provideContext(HttpServletRequest request) {
        return provideContext(request, null, null);
    }

    /**
     * Creates and configures an AuthenticationContext for OAuth2 debug flows.
     * This method handles all business logic for context creation including IdP validation,
     * authenticator determination, and context property setup.
     *
     * @param idpId Identity Provider resource ID.
     * @param authenticatorName Optional authenticator name.
     * @param redirectUri Optional custom redirect URI.
     * @param scope Optional OAuth 2.0 scope.
     * @param additionalParams Optional additional OAuth 2.0 parameters.
     * @return Configured AuthenticationContext with all necessary properties.
     * @throws RuntimeException if IdP is not found, disabled, or no authenticator available.
     */
    public AuthenticationContext createOAuth2DebugContext(String idpId, String authenticatorName, 
                                                         String redirectUri, String scope, 
                                                         Map<String, String> additionalParams) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating OAuth2 debug context for IdP ID: " + idpId + " with authenticator: " + authenticatorName);
        }

        // Input validation.
        validateOAuth2DebugRequest(idpId);
        
        try {
            // Get the IdP object using IdP management service.
            IdentityProvider idp = getIdentityProvider(idpId);
            if (idp == null) {
                throw new RuntimeException("Identity Provider not found for ID: " + idpId);
            }

            if (!idp.isEnable()) {
                throw new RuntimeException("Identity Provider is disabled: " + idp.getIdentityProviderName());
            }

            // Validate OAuth 2.0 configuration.
            validateOAuth2Configuration(idp);

            // Determine authenticator to use.
            String targetAuthenticator = determineAuthenticator(idp, authenticatorName);
            if (targetAuthenticator == null) {
                throw new RuntimeException("No suitable authenticator found for IdP: " + idp.getIdentityProviderName());
            }

            // Generate session data key for debug flow.
            String sessionDataKey = "debug-" + java.util.UUID.randomUUID().toString();

            // Create and configure authentication context.
            AuthenticationContext context = new AuthenticationContext();
            context.setContextIdentifier(sessionDataKey);
            
            // Set core debug properties.
            context.setRequestType("DFDP_DEBUG");
            context.setCallerSessionKey(sessionDataKey);
            context.setTenantDomain(DEBUG_TENANT_DOMAIN);
            context.setRelyingParty(DEBUG_SERVICE_PROVIDER_NAME);
            context.setProperty("isDebugFlow", Boolean.TRUE);
            context.setProperty("DEBUG_SESSION", "true");
            context.setProperty("DEBUG_AUTHENTICATOR_NAME", targetAuthenticator);
            context.setProperty("DEBUG_SESSION_ID", java.util.UUID.randomUUID().toString());
            context.setProperty("DEBUG_TIMESTAMP", System.currentTimeMillis());

            // Set IdP configuration and properties.
            context.setProperty("IDP_CONFIG", idp);
            context.setProperty("DEBUG_IDP_NAME", idp.getIdentityProviderName());
            context.setProperty("DEBUG_IDP_RESOURCE_ID", idp.getResourceId());
            context.setProperty("DEBUG_IDP_DESCRIPTION", idp.getIdentityProviderDescription());
            
            // Add OAuth2 parameters if provided.
            if (redirectUri != null && !redirectUri.trim().isEmpty()) {
                context.setProperty("CUSTOM_REDIRECT_URI", redirectUri);
            }
            if (scope != null && !scope.trim().isEmpty()) {
                context.setProperty("CUSTOM_SCOPE", scope);
            }
            if (additionalParams != null && !additionalParams.isEmpty()) {
                context.setProperty("ADDITIONAL_OAUTH_PARAMS", additionalParams);
                
                // Set individual custom parameters with debug prefix for security.
                for (Map.Entry<String, String> entry : additionalParams.entrySet()) {
                    String key = "DEBUG_CUSTOM_" + entry.getKey();
                    context.setProperty(key, entry.getValue());
                }
            }
            
            // Configure debug service provider context.
            try {
                configureDebugServiceProvider(context);
            } catch (Exception e) {
                LOG.error("Error configuring debug service provider context: " + e.getMessage(), e);
                // Continue - this is not critical for debug flow.
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("OAuth2 debug context created successfully with context identifier: " + context.getContextIdentifier());
            }
            
            return context;
            
        } catch (RuntimeException e) {
            LOG.error("Runtime error creating OAuth2 debug context", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Unexpected error creating OAuth2 debug context", e);
            throw new RuntimeException("Failed to create OAuth2 debug context: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates OAuth2 debug request parameters for security.
     *
     * @param idpId Identity Provider resource ID to validate.
     * @throws RuntimeException if validation fails.
     */
    private void validateOAuth2DebugRequest(String idpId) {
        if (idpId == null || idpId.trim().isEmpty()) {
            throw new RuntimeException("Identity Provider ID is required for OAuth2 debug");
        }
        
        // Add proper UUID validation for resource IDs.
        if (!idpId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            throw new RuntimeException("Invalid Identity Provider ID format");
        }
        
        // Additional validation can be added here as needed.
        if (idpId.trim().length() > 255) {
            throw new RuntimeException("Identity Provider ID exceeds maximum length");
        }
    }
    
    /**
     * Retrieves an Identity Provider by its resource ID.
     *
     * @param idpId Identity Provider resource ID.
     * @return IdentityProvider object or null if not found.
     * @throws Exception if error occurs during retrieval.
     */
    private IdentityProvider getIdentityProvider(String idpId) throws Exception {
        try {
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            if (idpManager == null) {
                throw new RuntimeException("Identity Provider Manager not available");
            }
            
            return idpManager.getIdPByResourceId(idpId, DEBUG_TENANT_DOMAIN, true);
        } catch (IdentityProviderManagementException e) {
            LOG.error("Error retrieving Identity Provider with ID: " + idpId, e);
            throw new RuntimeException("Failed to retrieve Identity Provider: " + e.getMessage(), e);
        }
    }
    
    /**
     * Determines which authenticator to use for the debug session.
     * Priority: 1) Explicit authenticator name 2) First available federated authenticator.
     *
     * @param idp Identity Provider configuration.
     * @param requestedAuthenticator Optional requested authenticator name.
     * @return Selected authenticator name or null if none available.
     */
    private String determineAuthenticator(IdentityProvider idp, String requestedAuthenticator) {
        if (requestedAuthenticator != null && !requestedAuthenticator.trim().isEmpty()) {
            // Validate the requested authenticator exists in this IdP.
            if (authenticatorExistsInIdP(idp, requestedAuthenticator)) {
                return requestedAuthenticator;
            } else {
                LOG.warn("Requested authenticator '" + requestedAuthenticator + "' not found in IdP: " + idp.getIdentityProviderName());
            }
        }
        
        // Find first available federated authenticator.
        FederatedAuthenticatorConfig[] federatedConfigs = idp.getFederatedAuthenticatorConfigs();
        if (federatedConfigs != null && federatedConfigs.length > 0) {
            for (FederatedAuthenticatorConfig config : federatedConfigs) {
                if (config != null && config.isEnabled() && config.getName() != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Selected authenticator: " + config.getName() + " for IdP: " + idp.getIdentityProviderName());
                    }
                    return config.getName();
                }
            }
        }
        
        // No suitable authenticator found.
        return null;
    }
    
    /**
     * Checks if the specified authenticator exists and is enabled in the IdP.
     *
     * @param idp Identity Provider configuration.
     * @param authenticatorName Authenticator name to check.
     * @return true if authenticator exists and enabled, false otherwise.
     */
    private boolean authenticatorExistsInIdP(IdentityProvider idp, String authenticatorName) {
        FederatedAuthenticatorConfig[] federatedConfigs = idp.getFederatedAuthenticatorConfigs();
        if (federatedConfigs != null) {
            for (FederatedAuthenticatorConfig config : federatedConfigs) {
                if (config != null && config.isEnabled() && 
                    authenticatorName.equals(config.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Validates OAuth 2.0 configuration for the Identity Provider.
     * Validates based on property names without requiring executor classes.
     *
     * @param idp Identity Provider to validate.
     * @throws RuntimeException if OAuth 2.0 configuration is invalid or incomplete.
     */
    private void validateOAuth2Configuration(IdentityProvider idp) throws RuntimeException {
        if (idp == null) {
            throw new RuntimeException("Identity Provider cannot be null");
        }
        
        FederatedAuthenticatorConfig[] federatedConfigs = idp.getFederatedAuthenticatorConfigs();
        if (federatedConfigs == null || federatedConfigs.length == 0) {
            throw new RuntimeException("No federated authenticator configurations found for IdP: " + 
                                     idp.getIdentityProviderName());
        }
        
        // Find OAuth 2.0 authenticator configuration.
        FederatedAuthenticatorConfig oauthConfig = null;
        Object executor = null;
        
        for (FederatedAuthenticatorConfig config : federatedConfigs) {
            if (config != null && config.isEnabled()) {
                String authenticatorName = config.getName();
                // Check for OAuth 2.0 related authenticators and create executor.
                if ("GoogleOIDCAuthenticator".equals(authenticatorName)) {
                    oauthConfig = config;
                    executor = createExecutor("org.wso2.carbon.identity.application.authenticator.google.GoogleExecutor");
                    break;
                } else if ("OpenIDConnectAuthenticator".equals(authenticatorName) ||
                          "OAuth2OpenIDConnectAuthenticator".equals(authenticatorName)) {
                    oauthConfig = config;
                    executor = createExecutor("org.wso2.carbon.identity.application.authenticator.oidc.OpenIDConnectExecutor");
                    break;
                } else if ("GitHubAuthenticator".equals(authenticatorName) || 
                          "GithubAuthenticator".equals(authenticatorName)) {
                    oauthConfig = config;
                    executor = createExecutor("org.wso2.carbon.identity.authenticator.github.GithubExecutor");
                    break;
                }
            }
        }
        
        if (oauthConfig == null) {
            throw new RuntimeException("No OAuth 2.0 authenticator configuration found for IdP: " + 
                                     idp.getIdentityProviderName());
        }
        
        // Convert properties to Map for validation.
        java.util.Map<String, String> authenticatorProperties = new java.util.HashMap<>();
        Property[] properties = oauthConfig.getProperties();
        if (properties != null) {
            for (Property property : properties) {
                if (property != null && property.getName() != null) {
                    authenticatorProperties.put(property.getName(), property.getValue());
                }
            }
        }
        
        // Validate required properties using executor if available.
        try {
            String authenticatorName = oauthConfig.getName();
            String clientId = getClientIdFromExecutor(executor, authenticatorProperties);
            String authzEndpoint = getAuthorizationEndpointFromExecutor(executor, authenticatorProperties, authenticatorName);
            String tokenEndpoint = getTokenEndpointFromExecutor(executor, authenticatorProperties, authenticatorName);
            
            // Validate required properties.
            if (clientId == null || clientId.trim().isEmpty()) {
                throw new RuntimeException("OAuth 2.0 Client ID is missing for IdP: " + 
                                         idp.getIdentityProviderName());
            }
            
            if (authzEndpoint == null || authzEndpoint.trim().isEmpty()) {
                throw new RuntimeException("OAuth 2.0 Authorization Endpoint is missing for IdP: " + 
                                         idp.getIdentityProviderName());
            }
            
            if (tokenEndpoint == null || tokenEndpoint.trim().isEmpty()) {
                throw new RuntimeException("OAuth 2.0 Token Endpoint is missing for IdP: " + 
                                         idp.getIdentityProviderName());
            }
            
            // Store resolved endpoints in the authenticator properties so Executer can use them.
            // This ensures the Executer doesn't need to resolve endpoints again.
            storeResolvedEndpoints(oauthConfig, clientId, authzEndpoint, tokenEndpoint);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("OAuth 2.0 configuration validated for IdP: " + idp.getIdentityProviderName() +
                         " - ClientId: FOUND" +
                         ", AuthzEndpoint: " + authzEndpoint +
                         ", TokenEndpoint: " + tokenEndpoint);
            }
            
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("OAuth 2.0 validation failed for IdP: " + idp.getIdentityProviderName(), e);
            }
            throw new RuntimeException("OAuth 2.0 configuration validation failed for IdP: " + 
                                     idp.getIdentityProviderName() + ". " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates an executor instance using reflection.
     * Returns null if the executor class is not available (extension not installed).
     *
     * @param executorClassName Fully qualified executor class name.
     * @return Executor instance or null if class not found.
     */
    private Object createExecutor(String executorClassName) {
        try {
            Class<?> executorClass = Class.forName(executorClassName);
            return executorClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executor class not found: " + executorClassName + 
                         ". The corresponding authenticator extension may not be installed.", e);
            }
            return null;
        } catch (Exception e) {
            LOG.warn("Failed to instantiate executor: " + executorClassName + ". " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets the client ID using executor instance or fallback to properties.
     *
     * @param executor Executor instance (can be null).
     * @param authenticatorProperties Authenticator properties map.
     * @return Client ID or null if not found.
     */
    private String getClientIdFromExecutor(Object executor, java.util.Map<String, String> authenticatorProperties) {
        // Try common client ID property names.
        String clientId = authenticatorProperties.get("ClientId");
        if (clientId != null && !clientId.trim().isEmpty()) {
            return clientId;
        }
        
        clientId = authenticatorProperties.get("client_id");
        if (clientId != null && !clientId.trim().isEmpty()) {
            return clientId;
        }
        
        clientId = authenticatorProperties.get("OAuth2ClientId");
        if (clientId != null && !clientId.trim().isEmpty()) {
            return clientId;
        }
        
        return null;
    }
    
    /**
     * Gets the authorization endpoint using executor instance and its fallback logic.
     *
     * @param executor Executor instance (can be null if extension not available).
     * @param authenticatorProperties Authenticator properties map.
     * @param authenticatorName Name of the authenticator.
     * @return Authorization endpoint URL with fallback support.
     */
    private String getAuthorizationEndpointFromExecutor(Object executor, java.util.Map<String, String> authenticatorProperties, String authenticatorName) {
        if (executor != null) {
            try {
                // Try getAuthorizationServerEndpoint first.
                Method method = executor.getClass().getMethod("getAuthorizationServerEndpoint", java.util.Map.class);
                Object result = method.invoke(executor, authenticatorProperties);
                if (result != null && !result.toString().trim().isEmpty()) {
                    return result.toString();
                }
            } catch (NoSuchMethodException e) {
                // Method doesn't exist, try alternative methods.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getAuthorizationServerEndpoint method not found, trying alternatives");
                }
                try {
                    // Try getAuthorizationEndpoint as alternative.
                    Method altMethod = executor.getClass().getMethod("getAuthorizationEndpoint", java.util.Map.class);
                    Object result = altMethod.invoke(executor, authenticatorProperties);
                    if (result != null && !result.toString().trim().isEmpty()) {
                        return result.toString();
                    }
                } catch (Exception ex) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Alternative method also failed: " + ex.getMessage());
                    }
                }
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to get authorization endpoint from executor: " + e.getMessage());
                }
            }
        }
        
        // Fallback to direct property lookup.
        String authzEndpoint = authenticatorProperties.get("OAuth2AuthzEPUrl");
        if (authzEndpoint != null && !authzEndpoint.trim().isEmpty()) {
            return authzEndpoint;
        }
        
        authzEndpoint = authenticatorProperties.get("AuthzEndpoint");
        if (authzEndpoint != null && !authzEndpoint.trim().isEmpty()) {
            return authzEndpoint;
        }
        
        authzEndpoint = authenticatorProperties.get("authorization_endpoint");
        if (authzEndpoint != null && !authzEndpoint.trim().isEmpty()) {
            return authzEndpoint;
        }
        
        return null;
    }
    
    /**
     * Gets the token endpoint using executor instance and its fallback logic.
     *
     * @param executor Executor instance (can be null if extension not available).
     * @param authenticatorProperties Authenticator properties map.
     * @param authenticatorName Name of the authenticator.
     * @return Token endpoint URL with fallback support.
     */
    private String getTokenEndpointFromExecutor(Object executor, java.util.Map<String, String> authenticatorProperties, String authenticatorName) {
        if (executor != null) {
            try {
                // Use reflection to call getTokenEndpoint method.
                Method method = executor.getClass().getMethod("getTokenEndpoint", java.util.Map.class);
                Object result = method.invoke(executor, authenticatorProperties);
                if (result != null && !result.toString().trim().isEmpty()) {
                    return result.toString();
                }
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to get token endpoint from executor: " + e.getMessage());
                }
            }
        }
        
        // Fallback to direct property lookup.
        String tokenEndpoint = authenticatorProperties.get("OAuth2TokenEPUrl");
        if (tokenEndpoint != null && !tokenEndpoint.trim().isEmpty()) {
            return tokenEndpoint;
        }
        
        tokenEndpoint = authenticatorProperties.get("TokenEndpoint");
        if (tokenEndpoint != null && !tokenEndpoint.trim().isEmpty()) {
            return tokenEndpoint;
        }
        
        tokenEndpoint = authenticatorProperties.get("token_endpoint");
        if (tokenEndpoint != null && !tokenEndpoint.trim().isEmpty()) {
            return tokenEndpoint;
        }
        
        return null;
    }
    
    /**
     * Stores the resolved OAuth 2.0 endpoints back into the authenticator configuration.
     * This ensures the Executer class can use them without re-resolving.
     *
     * @param config Authenticator configuration to update.
     * @param clientId Resolved client ID.
     * @param authzEndpoint Resolved authorization endpoint.
     * @param tokenEndpoint Resolved token endpoint.
     */
    private void storeResolvedEndpoints(FederatedAuthenticatorConfig config, String clientId, 
                                       String authzEndpoint, String tokenEndpoint) {
        // Find and update existing properties or add new ones.
        Property[] properties = config.getProperties();
        java.util.List<Property> propertyList = new java.util.ArrayList<>();
        
        boolean hasClientId = false;
        boolean hasAuthzEndpoint = false;
        boolean hasTokenEndpoint = false;
        
        if (properties != null) {
            for (Property prop : properties) {
                if (prop != null && prop.getName() != null) {
                    String propName = prop.getName();
                    if ("ClientId".equals(propName) || "client_id".equals(propName) || "OAuth2ClientId".equals(propName)) {
                        prop.setValue(clientId);
                        hasClientId = true;
                    } else if ("OAuth2AuthzEPUrl".equals(propName) || "AuthzEndpoint".equals(propName) || 
                              "authorization_endpoint".equals(propName)) {
                        prop.setValue(authzEndpoint);
                        hasAuthzEndpoint = true;
                    } else if ("OAuth2TokenEPUrl".equals(propName) || "TokenEndpoint".equals(propName) || 
                              "token_endpoint".equals(propName)) {
                        prop.setValue(tokenEndpoint);
                        hasTokenEndpoint = true;
                    }
                    propertyList.add(prop);
                }
            }
        }
        
        // Add missing properties.
        if (!hasClientId) {
            Property prop = new Property();
            prop.setName("ClientId");
            prop.setValue(clientId);
            propertyList.add(prop);
        }
        if (!hasAuthzEndpoint) {
            Property prop = new Property();
            prop.setName("OAuth2AuthzEPUrl");
            prop.setValue(authzEndpoint);
            propertyList.add(prop);
        }
        if (!hasTokenEndpoint) {
            Property prop = new Property();
            prop.setName("OAuth2TokenEPUrl");
            prop.setValue(tokenEndpoint);
            propertyList.add(prop);
        }
        
        config.setProperties(propertyList.toArray(new Property[0]));
    }
    
    /**
     * Provides diagnostic information about OAuth 2.0 configuration for an Identity Provider.
     * This method is useful for debugging configuration issues.
     *
     * @param idp Identity Provider to diagnose.
     * @return OAuth configuration diagnostic information.
     */
    public String diagnoseOAuth2Configuration(IdentityProvider idp) {
        if (idp == null) {
            return "Identity Provider is null";
        }
        
        StringBuilder diagnosis = new StringBuilder();
        diagnosis.append("OAuth 2.0 Configuration Diagnosis for IdP: ").append(idp.getIdentityProviderName()).append("\n");
        diagnosis.append("Enabled: ").append(idp.isEnable()).append("\n");
        
        FederatedAuthenticatorConfig[] federatedConfigs = idp.getFederatedAuthenticatorConfigs();
        if (federatedConfigs == null || federatedConfigs.length == 0) {
            diagnosis.append("No federated authenticator configurations found\n");
            return diagnosis.toString();
        }
        
        diagnosis.append("Available Authenticators: ").append(federatedConfigs.length).append("\n");
        
        for (FederatedAuthenticatorConfig config : federatedConfigs) {
            if (config != null) {
                diagnosis.append("  - ").append(config.getName())
                         .append(" (Enabled: ").append(config.isEnabled()).append(")\n");
                
                Property[] properties = config.getProperties();
                if (properties != null) {
                    // Convert properties to Map.
                    java.util.Map<String, String> authenticatorProperties = new java.util.HashMap<>();
                    for (Property property : properties) {
                        if (property != null && property.getName() != null) {
                            authenticatorProperties.put(property.getName(), property.getValue());
                        }
                    }
                    
                    // Show resolved endpoints for OAuth 2.0 authenticators.
                    String authenticatorName = config.getName();
                    if ("GoogleOIDCAuthenticator".equals(authenticatorName) ||
                        "OpenIDConnectAuthenticator".equals(authenticatorName) ||
                        "OAuth2OpenIDConnectAuthenticator".equals(authenticatorName) ||
                        "GitHubAuthenticator".equals(authenticatorName) ||
                        "GithubAuthenticator".equals(authenticatorName)) {
                        
                        // Create executor for endpoint resolution.
                        Object executor = null;
                        if ("GoogleOIDCAuthenticator".equals(authenticatorName)) {
                            executor = createExecutor("org.wso2.carbon.identity.application.authenticator.google.GoogleExecutor");
                        } else if ("OpenIDConnectAuthenticator".equals(authenticatorName) ||
                                  "OAuth2OpenIDConnectAuthenticator".equals(authenticatorName)) {
                            executor = createExecutor("org.wso2.carbon.identity.application.authenticator.oidc.OpenIDConnectExecutor");
                        } else if ("GitHubAuthenticator".equals(authenticatorName) || 
                                  "GithubAuthenticator".equals(authenticatorName)) {
                            // Try both possible executor class paths.
                            executor = createExecutor("org.wso2.carbon.extension.identity.authenticator.github.connector.GithubAuthenticator");
                            if (executor == null) {
                                executor = createExecutor("org.wso2.carbon.identity.application.authenticator.github.GithubExecutor");
                            }
                        }
                        
                        try {
                            String clientId = getClientIdFromExecutor(executor, authenticatorProperties);
                            String authzEndpoint = getAuthorizationEndpointFromExecutor(executor, authenticatorProperties, authenticatorName);
                            String tokenEndpoint = getTokenEndpointFromExecutor(executor, authenticatorProperties, authenticatorName);
                            
                            diagnosis.append("    Client ID: ").append(clientId != null ? "CONFIGURED" : "MISSING").append("\n");
                            diagnosis.append("    Authorization Endpoint: ");
                            if (authzEndpoint != null) {
                                diagnosis.append("RESOLVED (").append(authzEndpoint).append(")");
                            } else {
                                diagnosis.append("MISSING");
                            }
                            diagnosis.append("\n");
                            diagnosis.append("    Token Endpoint: ");
                            if (tokenEndpoint != null) {
                                diagnosis.append("RESOLVED (").append(tokenEndpoint).append(")");
                            } else {
                                diagnosis.append("MISSING");
                            }
                            diagnosis.append("\n");
                        } catch (Exception e) {
                            diagnosis.append("    ERROR: Failed to get endpoints - ").append(e.getMessage()).append("\n");
                        }
                    }
                    
                    // Show raw properties for debugging.
                    for (Property property : properties) {
                        if (property != null && property.getName() != null) {
                            String name = property.getName();
                            String value = property.getValue();
                            diagnosis.append("    ").append(name).append(": ")
                                    .append(value != null && !value.trim().isEmpty() ? "CONFIGURED" : "MISSING")
                                    .append("\n");
                        }
                    }
                }
            }
        }
        
        return diagnosis.toString();
    }
    
    /**
     * Configures debug service provider context properties.
     *
     * @param context AuthenticationContext to configure.
     */
    private void configureDebugServiceProvider(AuthenticationContext context) {
        try {
            ApplicationManagementService appMgtService = ApplicationManagementServiceImpl.getInstance();
            ServiceProvider serviceProvider = null;
            
            try {
                serviceProvider = appMgtService.getServiceProvider(DEBUG_SERVICE_PROVIDER_NAME, DEBUG_TENANT_DOMAIN);
            } catch (IdentityApplicationManagementException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Debug service provider not found, will use default configuration: " + e.getMessage());
                }
            }
            
            if (serviceProvider == null) {
                // Create minimal debug service provider configuration.
                serviceProvider = new ServiceProvider();
                serviceProvider.setApplicationName(DEBUG_SERVICE_PROVIDER_NAME);
                serviceProvider.setDescription("Debug Service Provider for DFDP flows");
            }
            
            context.setServiceProviderName(DEBUG_SERVICE_PROVIDER_NAME);
            context.setServiceProviderResourceId(serviceProvider.getApplicationResourceId());
            context.setProperty("DEBUG_SERVICE_PROVIDER", serviceProvider);
            
        } catch (Exception e) {
            // Log error but continue - this is not critical for debug flow.
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error setting up debug service provider context: " + e.getMessage());
            }
        }
    }
}
