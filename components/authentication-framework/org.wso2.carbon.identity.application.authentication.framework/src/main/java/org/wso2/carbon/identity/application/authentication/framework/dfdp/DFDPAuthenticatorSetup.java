/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.dfdp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.core.ApplicationAuthenticatorManager;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DFDP Authenticator Setup.
 * This class handles the direct setup and configuration of authenticators for DFDP testing,
 * bypassing the normal authentication framework flow to enable direct IdP interaction.
 */
public class DFDPAuthenticatorSetup {

    private static final Log log = LogFactory.getLog(DFDPAuthenticatorSetup.class);

    /**
     * Creates and configures a StepConfig for direct authenticator execution in DFDP mode.
     * This method sets up the authenticator configuration without going through the
     * normal sequence building process.
     *
     * @param context Authentication context containing DFDP parameters
     * @return Configured StepConfig for DFDP testing
     * @throws FrameworkException if step configuration fails
     */
    public StepConfig createStepConfig(AuthenticationContext context) throws FrameworkException {

        String targetIdP = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_IDP);
        String targetAuthenticator = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_AUTHENTICATOR);

        if (log.isDebugEnabled()) {
            log.debug("Creating DFDP step config for IdP: " + targetIdP + 
                     ", Authenticator: " + targetAuthenticator);
        }

        try {
            // Create a new step config for DFDP
            StepConfig stepConfig = new StepConfig();
            stepConfig.setOrder(1); // Single step for DFDP testing
            stepConfig.setCompleted(false);

            // Get the Identity Provider configuration
            IdentityProvider identityProvider = getIdentityProvider(targetIdP, context);
            
            // Get the specific authenticator configuration
            FederatedAuthenticatorConfig authConfig = getAuthenticatorConfig(
                identityProvider, targetAuthenticator);

            // Get the authenticator instance
            ApplicationAuthenticator authenticator = getAuthenticatorInstance(authConfig);

            // Configure the step with the authenticator
            configureStepWithAuthenticator(stepConfig, identityProvider, authConfig, authenticator);

            // Store configurations in context for later use
            context.setProperty(FrameworkConstants.DFDP_STEP_CONFIG, stepConfig);
            context.setProperty(FrameworkConstants.DFDP_IDENTITY_PROVIDER, identityProvider);
            context.setProperty(FrameworkConstants.DFDP_AUTHENTICATOR_INSTANCE, authenticator);

            if (log.isDebugEnabled()) {
                log.debug("DFDP step config created successfully for authenticator: " + 
                         authConfig.getName());
            }

            return stepConfig;

        } catch (Exception e) {
            log.error("Error creating DFDP step config for IdP: " + targetIdP, e);
            throw new FrameworkException("Failed to create DFDP step config: " + e.getMessage(), e);
        }
    }

    /**
     * Configures authenticator properties for DFDP testing.
     * This method sets up the necessary properties and configurations
     * required for direct authenticator execution.
     *
     * @param context Authentication context
     * @throws FrameworkException if configuration fails
     */
    public void configureProperties(AuthenticationContext context) throws FrameworkException {

        try {
            String targetIdP = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_IDP);
            
            // Get the configured step config and authenticator
            StepConfig stepConfig = (StepConfig) context.getProperty(FrameworkConstants.DFDP_STEP_CONFIG);
            ApplicationAuthenticator authenticator = (ApplicationAuthenticator) 
                context.getProperty(FrameworkConstants.DFDP_AUTHENTICATOR_INSTANCE);

            if (stepConfig == null || authenticator == null) {
                throw new FrameworkException("DFDP step config or authenticator not found");
            }

            // Set up authenticator properties for DFDP mode
            Map<String, String> dfdpProperties = new HashMap<>();
            dfdpProperties.put("dfdp.mode", "true");
            dfdpProperties.put("dfdp.target.idp", targetIdP);
            dfdpProperties.put("dfdp.execution.mode", "direct");

            // Add to authenticator properties in context
            context.setAuthenticatorProperties(dfdpProperties);

            // Configure the authenticator with DFDP-specific settings
            configureAuthenticatorForDFDP(authenticator, context);

            if (log.isDebugEnabled()) {
                log.debug("DFDP properties configured for authenticator: " + 
                         authenticator.getClass().getSimpleName());
            }

        } catch (Exception e) {
            log.error("Error configuring DFDP properties", e);
            throw new FrameworkException("Failed to configure DFDP properties: " + e.getMessage(), e);
        }
    }

    /**
     * Executes the configured authenticator directly for DFDP testing.
     * This method bypasses the normal authentication framework flow and directly
     * invokes the authenticator to test external IdP connectivity and claim retrieval.
     *
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @param context Authentication context
     * @throws FrameworkException if authenticator execution fails
     * @throws IOException if IO operations fail
     */
    public void executeAuthenticator(HttpServletRequest request, HttpServletResponse response,
                                   AuthenticationContext context) throws FrameworkException, IOException {

        ApplicationAuthenticator authenticator = (ApplicationAuthenticator) 
            context.getProperty(FrameworkConstants.DFDP_AUTHENTICATOR_INSTANCE);

        if (authenticator == null) {
            throw new FrameworkException("DFDP authenticator instance not found");
        }

        String targetIdP = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_IDP);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing DFDP authenticator: " + authenticator.getClass().getSimpleName() + 
                         " for IdP: " + targetIdP);
            }

            // Set DFDP execution mode in context
            context.setProperty(FrameworkConstants.DFDP_EXECUTION_MODE, "executing");

            // Execute the authenticator directly
            authenticator.process(request, response, context);

            // Mark execution as complete
            context.setProperty(FrameworkConstants.DFDP_EXECUTION_MODE, "completed");

            if (log.isDebugEnabled()) {
                log.debug("DFDP authenticator execution completed for IdP: " + targetIdP);
            }

        } catch (Exception e) {
            log.error("Error executing DFDP authenticator for IdP: " + targetIdP, e);
            context.setProperty(FrameworkConstants.DFDP_EXECUTION_MODE, "failed");
            throw new FrameworkException("DFDP authenticator execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the Identity Provider configuration from the management service.
     *
     * @param idpName Identity Provider name
     * @param context Authentication context
     * @return IdentityProvider configuration
     * @throws FrameworkException if IdP is not found
     */
    private IdentityProvider getIdentityProvider(String idpName, AuthenticationContext context) 
            throws FrameworkException {

        try {
            String tenantDomain = context.getTenantDomain();
            if (StringUtils.isBlank(tenantDomain)) {
                tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            }

            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            IdentityProvider identityProvider = idpManager.getIdPByName(idpName, tenantDomain, false);

            if (identityProvider == null) {
                throw new FrameworkException("Identity Provider not found: " + idpName + 
                                           " in tenant: " + tenantDomain);
            }

            return identityProvider;

        } catch (Exception e) {
            log.error("Error retrieving Identity Provider: " + idpName, e);
            throw new FrameworkException("Failed to retrieve Identity Provider: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the specific authenticator configuration from the Identity Provider.
     *
     * @param identityProvider Identity Provider configuration
     * @param authenticatorName Specific authenticator name (optional)
     * @return FederatedAuthenticatorConfig
     * @throws FrameworkException if authenticator configuration is not found
     */
    private FederatedAuthenticatorConfig getAuthenticatorConfig(IdentityProvider identityProvider,
                                                              String authenticatorName) throws FrameworkException {

        FederatedAuthenticatorConfig[] authConfigs = identityProvider.getFederatedAuthenticatorConfigs();
        
        if (authConfigs == null || authConfigs.length == 0) {
            throw new FrameworkException("No authenticator configurations found for IdP: " + 
                                       identityProvider.getIdentityProviderName());
        }

        // If specific authenticator name is provided, find it
        if (StringUtils.isNotBlank(authenticatorName)) {
            for (FederatedAuthenticatorConfig authConfig : authConfigs) {
                if (authenticatorName.equals(authConfig.getName())) {
                    return authConfig;
                }
            }
            throw new FrameworkException("Authenticator not found: " + authenticatorName + 
                                       " for IdP: " + identityProvider.getIdentityProviderName());
        }

        // Otherwise, use the default authenticator
        FederatedAuthenticatorConfig defaultConfig = identityProvider.getDefaultAuthenticatorConfig();
        if (defaultConfig != null) {
            return defaultConfig;
        }

        // If no default, use the first available authenticator
        return authConfigs[0];
    }

    /**
     * Gets the authenticator instance from the framework service data holder.
     *
     * @param authConfig Authenticator configuration
     * @return ApplicationAuthenticator instance
     * @throws FrameworkException if authenticator instance cannot be obtained
     */
    private ApplicationAuthenticator getAuthenticatorInstance(FederatedAuthenticatorConfig authConfig) 
            throws FrameworkException {

        try {
            String authenticatorName = authConfig.getName();
            
            if (log.isDebugEnabled()) {
                log.debug("Looking for real authenticator instance: " + authenticatorName);
            }

            // Get the real authenticator from the authenticator registry
            ApplicationAuthenticator authenticator = getAuthenticatorFromRegistry(authenticatorName, authConfig);
            
            if (authenticator != null) {
                return authenticator;
            }

            throw new FrameworkException("Authenticator implementation not found: " + authenticatorName);

        } catch (Exception e) {
            log.error("Error getting authenticator instance", e);
            throw new FrameworkException("Failed to get authenticator instance: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the real authenticator from the authenticator registry.
     * This method retrieves the actual authenticator implementation for real IdP testing.
     *
     * @param authenticatorName Name of the authenticator
     * @param authConfig Authenticator configuration
     * @return Real ApplicationAuthenticator instance
     * @throws FrameworkException if authenticator cannot be found
     */
    private ApplicationAuthenticator getAuthenticatorFromRegistry(String authenticatorName, 
                                                                FederatedAuthenticatorConfig authConfig) 
            throws FrameworkException {
        
        if (log.isDebugEnabled()) {
            log.debug("Getting real authenticator from registry: " + authenticatorName);
        }

        try {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            
            // Step 1: Use ApplicationAuthenticatorManager to get the real authenticator by name (includes all types)
            ApplicationAuthenticator authenticator = ApplicationAuthenticatorManager.getInstance()
                .getApplicationAuthenticatorByName(authenticatorName, tenantDomain);
            
            if (authenticator != null) {
                if (log.isDebugEnabled()) {
                    String authType = (authenticator instanceof FederatedApplicationAuthenticator) ? 
                        "Federated" : "Local";
                    log.debug("Found real " + authType.toLowerCase() + " authenticator: " + 
                             authenticator.getClass().getSimpleName() + " for name: " + authenticatorName);
                }
                return authenticator;
            }

            // Step 2: Try system defined authenticators (covers both local and federated system authenticators)
            authenticator = ApplicationAuthenticatorManager.getInstance()
                .getSystemDefinedAuthenticatorByName(authenticatorName);
                
            if (authenticator != null) {
                if (log.isDebugEnabled()) {
                    String authType = (authenticator instanceof FederatedApplicationAuthenticator) ? 
                        "Federated" : "Local";
                    log.debug("Found system defined " + authType.toLowerCase() + " authenticator: " + 
                             authenticator.getClass().getSimpleName() + " for name: " + authenticatorName);
                }
                return authenticator;
            }

            // Step 3: For federated authenticators, also try the federated authenticator config registry
            // This handles cases where the authenticator might be registered as a configuration 
            // but not as a direct authenticator
            try {
                FederatedAuthenticatorConfig fedConfig = IdentityProviderManager.getInstance()
                    .getFederatedAuthenticatorByName(authenticatorName, tenantDomain);
                
                if (fedConfig != null) {
                    // If we found a federated authenticator config, try to get the corresponding 
                    // ApplicationAuthenticator
                    for (ApplicationAuthenticator sysAuth : 
                        ApplicationAuthenticatorManager.getInstance().getSystemDefinedAuthenticators()) {
                        if (sysAuth instanceof FederatedApplicationAuthenticator && 
                            sysAuth.getName().equals(authenticatorName)) {
                            
                            if (log.isDebugEnabled()) {
                                log.debug("Found federated authenticator from config registry: " + 
                                         sysAuth.getClass().getSimpleName() + " for name: " + authenticatorName);
                            }
                            return sysAuth;
                        }
                    }
                }
            } catch (Exception e) {
                // Log but don't fail - this is just an additional lookup attempt
                if (log.isDebugEnabled()) {
                    log.debug("Could not find federated authenticator config for: " + authenticatorName, e);
                }
            }

            throw new FrameworkException("Real authenticator not found: " + authenticatorName + 
                                       ". Ensure the authenticator is properly registered in the system.");

        } catch (FrameworkException fe) {
            throw fe;
        } catch (Exception e) {
            log.error("Error getting real authenticator from registry", e);
            throw new FrameworkException("Failed to get real authenticator: " + e.getMessage(), e);
        }
    }

    /**
     * Configures the step with the authenticator and related configurations.
     *
     * @param stepConfig Step configuration
     * @param identityProvider Identity Provider
     * @param authConfig Authenticator configuration
     * @param authenticator Authenticator instance
     */
    private void configureStepWithAuthenticator(StepConfig stepConfig, IdentityProvider identityProvider,
                                               FederatedAuthenticatorConfig authConfig, 
                                               ApplicationAuthenticator authenticator) {

        // Set the identity provider in step config
        stepConfig.setAuthenticatedIdP(identityProvider.getIdentityProviderName());
        
        // Create authenticator config for the step
        org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig 
            authenticatorConfig = new org.wso2.carbon.identity.application.authentication.framework
                .config.model.AuthenticatorConfig();
        
        authenticatorConfig.setName(authConfig.getName());
        authenticatorConfig.setEnabled(authConfig.isEnabled());
        authenticatorConfig.setApplicationAuthenticator(authenticator);

        // Set authenticator properties
        if (authConfig.getProperties() != null) {
            Map<String, String> properties = new HashMap<>();
            for (Property property : authConfig.getProperties()) {
                properties.put(property.getName(), property.getValue());
            }
            authenticatorConfig.setParameterMap(properties);
        }

        // Set the authenticator config in step config
        stepConfig.setAuthenticatedAutenticator(authenticatorConfig);
    }

    /**
     * Configures the authenticator for DFDP mode operation.
     * Sets up properties and flags that allow the authenticator to operate in testing mode.
     * Includes special handling for federated authenticators.
     *
     * @param authenticator Application authenticator
     * @param context Authentication context
     */
    private void configureAuthenticatorForDFDP(ApplicationAuthenticator authenticator, 
                                              AuthenticationContext context) {

        // Set general DFDP mode properties
        context.setProperty("authenticator.dfdp.mode", true);
        context.setProperty("authenticator.dfdp.claims.capture", true);
        
        // Special configuration for federated authenticators
        if (authenticator instanceof FederatedApplicationAuthenticator) {
            if (log.isDebugEnabled()) {
                log.debug("Configuring federated authenticator for DFDP mode: " + authenticator.getName());
            }
            
            // Set federated-specific DFDP properties
            context.setProperty("authenticator.dfdp.federated", true);
            context.setProperty("authenticator.dfdp.external.claims.test", true);
            
            // Enable claim mapping debug mode for federated authenticators
            context.setProperty("authenticator.dfdp.claim.mapping.debug", true);
            
            // Set flags to capture external IdP response
            context.setProperty("authenticator.dfdp.capture.external.response", true);
            context.setProperty("authenticator.dfdp.capture.external.claims", true);
            
            // Configure to bypass certain security checks for testing
            context.setProperty("authenticator.dfdp.bypass.security.checks", true);
            
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Configuring local authenticator for DFDP mode: " + authenticator.getName());
            }
            
            // Set local authenticator specific properties
            context.setProperty("authenticator.dfdp.local", true);
        }
    }
}
