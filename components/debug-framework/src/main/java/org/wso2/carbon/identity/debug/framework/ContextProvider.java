package org.wso2.carbon.identity.debug.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides context for debug authentication flows.
 * Uses IdP management to get IdP object and set up context with relevant data.
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
