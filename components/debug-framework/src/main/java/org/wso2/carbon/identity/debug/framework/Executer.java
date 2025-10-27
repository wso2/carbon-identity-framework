package org.wso2.carbon.identity.debug.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.application.common.model.Property;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Simplified executor that generates OAuth 2.0 Authorization URLs with PKCE parameters.
 * This class only handles URL generation and parameter setup - actual authentication 
 * is delegated to the external IdP and handled via /commonauth callback.
 */
public class Executer {

    private static final Log LOG = LogFactory.getLog(Executer.class);
    private static final String DEBUG_CALLBACK_PATH = "/commonauth";
    private static final String DEBUG_IDENTIFIER_PARAM = "isDebugFlow";

    /**
     * Generates a standard OAuth 2.0 Authorization URL with PKCE parameters.
     * The URL will be used to redirect the user's browser to the external IdP for authentication.
     *
     * @param idp Identity Provider configuration.
     * @param context AuthenticationContext to store generated parameters.
     * @return true if URL generation is successful, false otherwise.
     */
    public boolean execute(IdentityProvider idp, AuthenticationContext context) {
    // Step status: connection creation started
    context.setProperty("step_connection_status", "started");
    // Step status: authentication started
    context.setProperty("step_authentication_status", "started");
    // Step status: claim mapping started
    context.setProperty("step_claim_mapping_status", "started");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generating OAuth 2.0 Authorization URL for IdP: " + 
                     (idp != null ? idp.getIdentityProviderName() : "null"));
        }

        try {
            if (idp == null) {
                LOG.error("Identity Provider is null, cannot generate authorization URL");
                context.setProperty("step_connection_status", "failed");
                context.setProperty("step_authentication_status", "failed");
                context.setProperty("step_claim_mapping_status", "failed");
                return false;
            }

            // Get authenticator configuration from context.
            String authenticatorName = (String) context.getProperty("DEBUG_AUTHENTICATOR_NAME");
            if (authenticatorName == null) {
                LOG.error("Authenticator name not found in context");
                context.setProperty("step_connection_status", "failed");
                context.setProperty("step_authentication_status", "failed");
                context.setProperty("step_claim_mapping_status", "failed");
                return false;
            }

            // Find the specific authenticator configuration.
            FederatedAuthenticatorConfig authenticatorConfig = findAuthenticatorConfig(idp, authenticatorName);
            if (authenticatorConfig == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authenticator configuration not found for: " + authenticatorName);
                }
                context.setProperty("step_connection_status", "failed");
                context.setProperty("step_authentication_status", "failed");
                context.setProperty("step_claim_mapping_status", "failed");
                return false;
            }

            // Generate OAuth 2.0 Authorization URL with PKCE.
            String authorizationUrl = buildAuthorizationUrl(idp, authenticatorConfig, context);
            if (authorizationUrl == null) {
                LOG.error("Failed to build authorization URL");
                context.setProperty("step_connection_status", "failed");
                context.setProperty("step_authentication_status", "failed");
                context.setProperty("step_claim_mapping_status", "failed");
                return false;
            }

            // Store the authorization URL in context for client retrieval.
            context.setProperty("DEBUG_EXTERNAL_REDIRECT_URL", authorizationUrl);

            // Mark debug flow properties.
            context.setProperty(DEBUG_IDENTIFIER_PARAM, "true");
            context.setProperty("DEBUG_AUTH_URL_GENERATED", "true");
            context.setProperty("DEBUG_AUTH_URL_TIMESTAMP", System.currentTimeMillis());
            // Step status reporting: authorization URL generated
            context.setProperty("DEBUG_STEP_AUTH_URL_GENERATED", true);
            context.setProperty("DEBUG_STEP_AUTH_URL", authorizationUrl);
            context.setProperty("DEBUG_STEP_AUTH_URL_TIMESTAMP", System.currentTimeMillis());

            // Step status: connection creation successful
            context.setProperty("step_connection_status", "success");
            // Step status: authentication successful (URL generated means ready for authentication)
            context.setProperty("step_authentication_status", "success");
            // Step status: claim mapping pending (will be set in claim mapping logic)
            context.setProperty("step_claim_mapping_status", "pending");

            // Cache the authentication context for WSO2 framework to find during callback.
            cacheAuthenticationContext(context);

            // Only log essential verification for production
            LOG.info("OAuth 2.0 Authorization URL Generated");

            return true;

        } catch (Exception e) {
            LOG.error("Error generating OAuth 2.0 Authorization URL: " + e.getMessage(), e);
            context.setProperty("step_connection_status", "failed");
            context.setProperty("step_authentication_status", "failed");
            context.setProperty("step_claim_mapping_status", "failed");
            return false;
        }
    }

    /**
     * Builds the complete OAuth 2.0 Authorization URL with PKCE parameters.
     *
     * @param idp Identity Provider configuration.
     * @param authenticatorConfig Authenticator configuration.
     * @param context AuthenticationContext to store generated parameters.
     * @return Complete authorization URL or null if failed.
     */
    private String buildAuthorizationUrl(IdentityProvider idp, FederatedAuthenticatorConfig authenticatorConfig,
                                        AuthenticationContext context) {
        try {
            // Extract OAuth 2.0 configuration from authenticator using executor pattern with fallback.
            String clientId = getPropertyValue(authenticatorConfig, "ClientId", "client_id", "clientId");
            String authorizationEndpoint = getAuthorizationServerEndpoint(authenticatorConfig);

            if (clientId == null || clientId.trim().isEmpty()) {
                LOG.error("Missing OAuth 2.0 configuration - ClientId is required");
                return null;
            }
            
            if (authorizationEndpoint == null || authorizationEndpoint.trim().isEmpty()) {
                LOG.error("Missing OAuth 2.0 configuration - Authorization Endpoint is required");
                return null;
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("OAuth 2.0 configuration validated - ClientId: FOUND, Authorization Endpoint: " + authorizationEndpoint);
            }

            // Generate PKCE parameters.
            String codeVerifier = generateCodeVerifier();
            String codeChallenge = generateCodeChallenge(codeVerifier);
            // Use context identifier as state parameter for session lookup during callback.
            String state = context.getContextIdentifier();

            // Store PKCE parameters and state in context for callback verification.
            context.setProperty("DEBUG_CODE_VERIFIER", codeVerifier);
            context.setProperty("DEBUG_STATE", state);

            // Build redirect URI pointing to WSO2 /commonauth endpoint.
            String redirectUri = buildDebugCallbackUrl(context);

            // Build authorization URL.
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(authorizationEndpoint);
            urlBuilder.append("?response_type=code");
            urlBuilder.append("&client_id=").append(encodeParam(clientId));
            urlBuilder.append("&redirect_uri=").append(encodeParam(redirectUri));
            
            // Build scope - get from configuration or use default.
            String scope = getPropertyValue(authenticatorConfig, "Scope", "scope");
            if (scope == null || scope.trim().isEmpty()) {
                scope = "openid profile email";
            }
            urlBuilder.append("&scope=").append(encodeParam(scope));
            
            urlBuilder.append("&state=").append(encodeParam(state));
            urlBuilder.append("&code_challenge=").append(encodeParam(codeChallenge));
            urlBuilder.append("&code_challenge_method=S256");

            // Add access_type for refresh token support if configured.
            String accessType = getPropertyValue(authenticatorConfig, "AccessType", "access_type");
            if (accessType != null && !accessType.trim().isEmpty()) {
                urlBuilder.append("&access_type=").append(encodeParam(accessType));
            }

            // Add login hint if username is available.
            String username = (String) context.getProperty("DEBUG_USERNAME");
            if (username != null && !username.trim().isEmpty()) {
                urlBuilder.append("&login_hint=").append(encodeParam(username));
            }

            // Add any additional custom parameters from configuration.
            addCustomParameters(authenticatorConfig, urlBuilder);

            String authorizationUrl = urlBuilder.toString();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Generated OAuth 2.0 Authorization URL with PKCE for IdP: " + 
                         idp.getIdentityProviderName());
            }

            return authorizationUrl;

        } catch (Exception e) {
            LOG.error("Error building OAuth 2.0 Authorization URL: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Finds the authenticator configuration for the specified authenticator name.
     *
     * @param idp Identity Provider containing authenticator configurations.
     * @param authenticatorName Name of the authenticator to find.
     * @return FederatedAuthenticatorConfig if found, null otherwise.
     */
    private FederatedAuthenticatorConfig findAuthenticatorConfig(IdentityProvider idp, String authenticatorName) {
        FederatedAuthenticatorConfig[] configs = idp.getFederatedAuthenticatorConfigs();
        if (configs != null) {
            for (FederatedAuthenticatorConfig config : configs) {
                if (authenticatorName.equals(config.getName())) {
                    return config;
                }
            }
        }
        return null;
    }

    /**
     * Gets authorization server endpoint using executor pattern with fallback logic.
     * Mimics the behavior of GoogleExecutor and OpenIDConnectExecutor.
     *
     * @param config Authenticator configuration.
     * @return Authorization endpoint URL with fallback support.
     */
    private String getAuthorizationServerEndpoint(FederatedAuthenticatorConfig config) {
        String authzEndpoint = getPropertyValue(config, "OAuth2AuthzEPUrl", "authorizationEndpoint", "authorization_endpoint");
        
        if (authzEndpoint != null && !authzEndpoint.trim().isEmpty()) {
            return authzEndpoint;
        }
        
        // Do not use hardcoded endpoints. Always use endpoints from authenticator config.
        return null;
    }

    /**
     * Gets property value from authenticator configuration, trying multiple possible property names.
     *
     * @param config Authenticator configuration.
     * @param propertyNames Possible property names to look for.
     * @return Property value if found, null otherwise.
     */
    private String getPropertyValue(FederatedAuthenticatorConfig config, String... propertyNames) {
        if (config.getProperties() != null) {
            for (Property prop : config.getProperties()) {
                for (String propName : propertyNames) {
                    if (propName.equalsIgnoreCase(prop.getName())) {
                        return prop.getValue();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Generates a cryptographically secure code verifier for PKCE.
     *
     * @return Base64URL-encoded code verifier.
     */
    private String generateCodeVerifier() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Generates code challenge from code verifier using SHA256.
     *
     * @param codeVerifier The code verifier.
     * @return Base64URL-encoded code challenge.
     */
    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes("UTF-8"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            LOG.error("Error generating code challenge: " + e.getMessage(), e);
            // Fallback to plain text (not recommended for production).
            return codeVerifier;
        }
    }

    /**
     * Builds the debug callback URL for OAuth 2.0 redirect_uri.
     * Uses clean URL without query parameters to match Asgardeo's registered redirect URIs.
     *
     * @param context AuthenticationContext for session data.
     * @return Callback URL string.
     */
    private String buildDebugCallbackUrl(AuthenticationContext context) {
        try {
            String baseUrl = ServiceURLBuilder.create().build().getAbsolutePublicURL();
            String callbackUrl = baseUrl + DEBUG_CALLBACK_PATH;
            // Step status reporting: callback URL built
            context.setProperty("DEBUG_STEP_CALLBACK_URL_BUILT", true);
            context.setProperty("DEBUG_STEP_CALLBACK_URL", callbackUrl);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug callback URL built: " + callbackUrl + 
                         " (sessionDataKey will be handled via state parameter: " + context.getContextIdentifier() + ")");
            }
            
            return callbackUrl;
        } catch (URLBuilderException e) {
            LOG.error("Error building debug callback URL: " + e.getMessage(), e);
            String fallbackUrl = "https://localhost:9443/commonauth";
            // Step status reporting: fallback callback URL used
            context.setProperty("DEBUG_STEP_CALLBACK_URL_FALLBACK_USED", true);
            context.setProperty("DEBUG_STEP_CALLBACK_URL", fallbackUrl);
            return fallbackUrl;
        }
    }

    /**
     * URL-encodes a parameter value.
     *
     * @param param Parameter to encode.
     * @return URL-encoded parameter.
     */
    private String encodeParam(String param) {
        try {
            return java.net.URLEncoder.encode(param, "UTF-8");
        } catch (Exception e) {
            LOG.error("Error encoding parameter: " + e.getMessage(), e);
            return param;
        }
    }

    /**
     * Adds custom parameters from authenticator configuration to the authorization URL.
     *
     * @param config Authenticator configuration.
     * @param urlBuilder StringBuilder to append parameters to.
     */
    private void addCustomParameters(FederatedAuthenticatorConfig config, StringBuilder urlBuilder) {
        try {
            if (config.getProperties() != null) {
                for (Property prop : config.getProperties()) {
                    String propName = prop.getName();
                    String propValue = prop.getValue();
                    // Add any property that starts with "QueryParam_" as a custom parameter.
                    if (propName != null && propName.startsWith("QueryParam_") && propValue != null) {
                        String paramName = propName.substring("QueryParam_".length());
                        if (!paramName.trim().isEmpty()) {
                            urlBuilder.append("&").append(encodeParam(paramName)).append("=").append(encodeParam(propValue));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error adding custom parameters: " + e.getMessage(), e);
        }
    }
    
    /**
     * Caches the authentication context using WSO2 framework utilities.
     * This ensures the context can be retrieved by DefaultRequestCoordinator during callback.
     *
     * @param context AuthenticationContext to cache.
     */
    private void cacheAuthenticationContext(AuthenticationContext context) {
        try {
            // Use WSO2 framework utilities to cache the context.
            FrameworkUtils.addAuthenticationContextToCache(context.getContextIdentifier(), context);
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug authentication context cached successfully with identifier: " + 
                         context.getContextIdentifier());
            }
        } catch (Exception e) {
            LOG.error("Error caching debug authentication context: " + e.getMessage(), e);
        }
    }
}
