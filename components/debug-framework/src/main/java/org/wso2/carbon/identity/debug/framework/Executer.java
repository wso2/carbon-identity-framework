package org.wso2.carbon.identity.debug.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatorData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Executes authentication using the provided IdP and context.
 * Similar to OpenIDConnectExecutor pattern, handles authentication execution and callback handling.
 */
public class Executer {

    private static final Log LOG = LogFactory.getLog(Executer.class);
    private static final String DEBUG_CALLBACK_PATH = "/commonauth";
    private static final String DEBUG_IDENTIFIER_PARAM = "isDebugFlow";

    /**
     * Executes authentication for the given IdP and context.
     * Initiates authentication flow similar to OpenIDConnectExecutor and handles callback to /commonauth.
     *
     * @param idp Identity Provider for authentication.
     * @param context AuthenticationContext containing debug flow data.
     * @return true if authentication is initiated successfully, false otherwise.
     */
    public boolean execute(IdentityProvider idp, AuthenticationContext context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing debug authentication flow for IdP: " + 
                     (idp != null ? idp.getIdentityProviderName() : "null"));
        }

        try {
            if (idp == null) {
                LOG.error("Identity Provider is null, cannot execute authentication");
                return false;
            }

            // Get authenticator configuration from context.
            String authenticatorName = (String) context.getProperty("DEBUG_AUTHENTICATOR_NAME");
            if (authenticatorName == null) {
                LOG.error("Authenticator name not found in context");
                return false;
            }

            // Find the specific authenticator configuration.
            FederatedAuthenticatorConfig authenticatorConfig = findAuthenticatorConfig(idp, authenticatorName);
            if (authenticatorConfig == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authenticator configuration not found for: " + authenticatorName);
                }
                return false;
            }

            // Get the authenticator implementation.
            ApplicationAuthenticator authenticator = getAuthenticatorImplementation(authenticatorName);
            if (authenticator == null) {
                LOG.error("Authenticator implementation not found for: " + authenticatorName);
                return false;
            }

            // Set debug-specific properties in context.
            setupDebugContext(context, idp, authenticatorConfig);

            // Execute authentication initiation (similar to OpenIDConnectExecutor's initiateSocialSignup).
            return initiateDebugAuthentication(authenticator, context, authenticatorConfig);

        } catch (Exception e) {
            LOG.error("Error during debug authentication execution: " + e.getMessage(), e);
            return false;
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
     * Gets the authenticator implementation for the specified authenticator name.
     *
     * @param authenticatorName Name of the authenticator.
     * @return ApplicationAuthenticator implementation if found, null otherwise.
     */
    private ApplicationAuthenticator getAuthenticatorImplementation(String authenticatorName) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Looking for authenticator implementation: " + authenticatorName);
            }
            // Use FrameworkUtils to get the authenticator implementation by name
            ApplicationAuthenticator authenticator = FrameworkUtils.getAppAuthenticatorByName(authenticatorName);
            
            if (authenticator != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found authenticator implementation: " + authenticatorName);
                }
                return authenticator;
            }
            
            LOG.error("Authenticator implementation not found for: " + authenticatorName);
            return null;
        } catch (Exception e) {
            LOG.error("Error getting authenticator implementation: " + e.getMessage(), e);
            return null;
        }
    }


    /**
     * Sets up debug-specific context properties.
     *
     * @param context AuthenticationContext to configure.
     * @param idp Identity Provider information.
     * @param authenticatorConfig Authenticator configuration.
     */
    private void setupDebugContext(AuthenticationContext context, IdentityProvider idp, 
                                   FederatedAuthenticatorConfig authenticatorConfig) {
        // Set external IdP configuration.
        // Note: setExternalIdP expects ExternalIdPConfig, not IdentityProvider
        // context.setExternalIdP(idp);
        
        // Set debug flow identifier.
        context.setProperty(DEBUG_IDENTIFIER_PARAM, "true");
        
        // Set authenticator properties.
        Map<String, String> authenticatorProperties = new HashMap<>();
        if (authenticatorConfig.getProperties() != null) {
            for (org.wso2.carbon.identity.application.common.model.Property prop : authenticatorConfig.getProperties()) {
                authenticatorProperties.put(prop.getName(), prop.getValue());
            }
        }
        context.setProperty("DEBUG_AUTHENTICATOR_PROPERTIES", authenticatorProperties);
        
        // Set step information (debug flows are single-step).
        context.setCurrentStep(1);
        context.setProperty("DEBUG_CURRENT_STEP", "1");
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug context setup completed for IdP: " + idp.getIdentityProviderName());
        }
    }

    /**
     * Initiates debug authentication flow similar to OpenIDConnectExecutor pattern.
     *
     * @param authenticator ApplicationAuthenticator to use.
     * @param context AuthenticationContext.
     * @param authenticatorConfig Authenticator configuration.
     * @return true if authentication initiation is successful, false otherwise.
     */
    private boolean initiateDebugAuthentication(ApplicationAuthenticator authenticator, 
                                               AuthenticationContext context, 
                                               FederatedAuthenticatorConfig authenticatorConfig) {
        try {
            if (!(authenticator instanceof FederatedApplicationAuthenticator)) {
                LOG.error("Authenticator is not a federated authenticator: " + authenticator.getName());
                return false;
            }

            FederatedApplicationAuthenticator federatedAuth = (FederatedApplicationAuthenticator) authenticator;

            // Use the real HTTP request and response from context
            HttpServletRequest request = createDebugHttpRequest(context);
            HttpServletResponse response = createDebugHttpResponse(context);

            if (request == null || response == null) {
                LOG.error("Failed to create debug HTTP request/response objects");
                return false;
            }

            // Set callback URL with debug identifier.
            String callbackUrl = buildDebugCallbackUrl(context);
            context.setProperty("DEBUG_CALLBACK_URL", callbackUrl);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Initiating debug authentication with callback URL: " + callbackUrl);
            }

            // Initiate authentication using the real authenticator
            try {
                federatedAuth.process(request, response, context);
            } catch (AuthenticationFailedException e) {
                LOG.error("Authentication initiation failed: " + e.getMessage(), e);
                return false;
            } catch (LogoutFailedException e) {
                LOG.error("Unexpected logout during authentication initiation: " + e.getMessage(), e);
                return false;
            }

            // Mark as initiated successfully
            context.setProperty("DEBUG_AUTH_INITIATED", "true");
            context.setProperty("DEBUG_AUTH_TIMESTAMP", System.currentTimeMillis());

            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug authentication initiated");
            }

            return true;

        } catch (Exception e) {
            LOG.error("Unexpected error during authentication initiation: " + e.getMessage(), e);
            context.setProperty("DEBUG_AUTH_ERROR", "Unexpected error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a mock HTTP request for debug flow.
     *
     * @param context AuthenticationContext containing debug information.
     * @return HttpServletRequest mock object.
     */
    private HttpServletRequest createDebugHttpRequest(AuthenticationContext context) {
        // This would typically be a mock object or wrapper.
        // For now, return the request from context if available.
        return (HttpServletRequest) context.getProperty("DEBUG_HTTP_REQUEST");
    }

    /**
     * Creates a mock HTTP response for debug flow.
     *
     * @param context AuthenticationContext containing debug information.
     * @return HttpServletResponse mock object.
     */
    private HttpServletResponse createDebugHttpResponse(AuthenticationContext context) {
        // This would typically be a mock object or wrapper.
        // For now, return the response from context if available.
        return (HttpServletResponse) context.getProperty("DEBUG_HTTP_RESPONSE");
    }

    /**
     * Builds the debug callback URL with debug identifier.
     *
     * @param context AuthenticationContext.
     * @return Callback URL string.
     */
    private String buildDebugCallbackUrl(AuthenticationContext context) {
        try {
            String baseUrl = ServiceURLBuilder.create().build().getAbsolutePublicURL();
            return baseUrl + DEBUG_CALLBACK_PATH + 
                   "?" + DEBUG_IDENTIFIER_PARAM + "=true" +
                   "&sessionDataKey=" + context.getContextIdentifier() +
                   "&debugSessionId=" + context.getProperty("DEBUG_SESSION_ID");
        } catch (URLBuilderException e) {
            LOG.error("Error building debug callback URL: " + e.getMessage(), e);
            return "/commonauth?isDebugFlow=true&sessionDataKey=" + context.getContextIdentifier();
        }
    }

    /**
     * Processes authentication response from IdP (callback handling).
     * This method would be called by RequestCoordinator when /commonauth receives callback.
     *
     * @param context AuthenticationContext.
     * @param request HttpServletRequest containing authentication response.
     * @param response HttpServletResponse.
     * @return true if processing is successful, false otherwise.
     */
    public boolean processAuthenticationResponse(AuthenticationContext context, 
                                               HttpServletRequest request, 
                                               HttpServletResponse response) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing authentication response for debug session: " + 
                     context.getProperty("DEBUG_SESSION_ID"));
        }

        try {
            // Check if this is a debug flow callback.
            String isDebugFlow = request.getParameter(DEBUG_IDENTIFIER_PARAM);
            if (!"true".equals(isDebugFlow)) {
                LOG.warn("Non-debug flow detected in debug response processor");
                return false;
            }

            // Get the authenticator to process the response.
            String authenticatorName = (String) context.getProperty("DEBUG_AUTHENTICATOR_NAME");
            ApplicationAuthenticator authenticator = getAuthenticatorImplementation(authenticatorName);

            if (authenticator instanceof FederatedApplicationAuthenticator) {
                FederatedApplicationAuthenticator federatedAuth = (FederatedApplicationAuthenticator) authenticator;

                // Process the authentication response using the real authenticator
                try {
                    federatedAuth.process(request, response, context);
                } catch (AuthenticationFailedException e) {
                    LOG.error("Authentication response processing failed: " + e.getMessage(), e);
                    context.setProperty("DEBUG_AUTH_ERROR", "Authentication failed: " + e.getMessage());
                    context.setProperty("DEBUG_AUTH_RESULT", false);
                    return false;
                } catch (LogoutFailedException e) {
                    LOG.error("Unexpected logout during authentication response processing: " + e.getMessage(), e);
                    context.setProperty("DEBUG_AUTH_ERROR", "Unexpected logout: " + e.getMessage());
                    context.setProperty("DEBUG_AUTH_RESULT", false);
                    return false;
                }

                // Mark as completed
                context.setProperty("DEBUG_AUTH_RESULT", true);
                context.setProperty("DEBUG_AUTH_COMPLETED", "true");
                context.setProperty("DEBUG_AUTH_COMPLETION_TIMESTAMP", System.currentTimeMillis());

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authentication response processed. Result: true");
                }

                return true;
            }

        } catch (Exception e) {
            LOG.error("Unexpected error during response processing: " + e.getMessage(), e);
            context.setProperty("DEBUG_AUTH_ERROR", "Unexpected error: " + e.getMessage());
            context.setProperty("DEBUG_AUTH_RESULT", false);
        }

        return false;
    }
}
