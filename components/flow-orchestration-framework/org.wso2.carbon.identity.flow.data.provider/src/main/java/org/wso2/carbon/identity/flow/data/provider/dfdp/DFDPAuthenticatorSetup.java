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

package org.wso2.carbon.identity.flow.data.provider.dfdp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DFDP Authenticator Setup.
 * This class handles the direct configuration and setup of authenticators for DFDP flows,
 * bypassing the normal authentication sequence to directly test external IdP interactions.
 */
public class DFDPAuthenticatorSetup {

    private static final Log log = LogFactory.getLog(DFDPAuthenticatorSetup.class);

    /**
     * Sets up the target authenticator for DFDP processing.
     * This method creates a minimal StepConfig and configures the specific authenticator
     * needed to interact with the target external IdP.
     * 
     * @param context Authentication context containing DFDP parameters
     * @throws FrameworkException if authenticator setup fails
     */
    public void setupAuthenticatorForDFDP(AuthenticationContext context) throws FrameworkException {

        String targetIdP = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_IDP);
        String targetAuthenticator = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_AUTHENTICATOR);
        String requestId = (String) context.getProperty(FrameworkConstants.DFDP_REQUEST_ID);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Setting up DFDP authenticator for IdP: " + targetIdP + 
                         ", Authenticator: " + targetAuthenticator + ", Request ID: " + requestId);
            }

            // Step 1: Get Identity Provider configuration
            IdentityProvider identityProvider = getIdentityProvider(targetIdP, context.getTenantDomain());
            if (identityProvider == null) {
                throw new FrameworkException("Identity Provider not found: " + targetIdP);
            }

            // Step 2: Create minimal StepConfig for DFDP
            StepConfig stepConfig = createDFDPStepConfig(identityProvider, targetAuthenticator);
            
            // Step 3: Configure authenticator properties
            configureAuthenticatorProperties(stepConfig, identityProvider, targetAuthenticator);

            // Step 4: Store configuration in context
            context.setProperty(FrameworkConstants.DFDP_STEP_CONFIG, stepConfig);
            context.setProperty(FrameworkConstants.DFDP_IDENTITY_PROVIDER, identityProvider);

            if (log.isDebugEnabled()) {
                log.debug("DFDP authenticator setup completed for IdP: " + targetIdP);
            }

        } catch (Exception e) {
            log.error("Error setting up DFDP authenticator for IdP: " + targetIdP, e);
            throw new FrameworkException("Failed to setup DFDP authenticator: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the Identity Provider configuration.
     * 
     * @param idpName Identity Provider name
     * @param tenantDomain Tenant domain
     * @return IdentityProvider configuration
     * @throws FrameworkException if IdP retrieval fails
     */
    private IdentityProvider getIdentityProvider(String idpName, String tenantDomain) throws FrameworkException {

        try {
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            IdentityProvider identityProvider = idpManager.getIdPByName(idpName, tenantDomain);
            
            if (identityProvider == null) {
                throw new FrameworkException("Identity Provider not found: " + idpName + 
                                           " in tenant: " + tenantDomain);
            }

            if (!identityProvider.isEnable()) {
                throw new FrameworkException("Identity Provider is disabled: " + idpName);
            }

            return identityProvider;

        } catch (IdentityProviderManagementException e) {
            throw new FrameworkException("Error retrieving Identity Provider: " + idpName, e);
        }
    }

    /**
     * Creates a minimal StepConfig for DFDP processing.
     * 
     * @param identityProvider Identity Provider configuration
     * @param targetAuthenticator Target authenticator name (optional)
     * @return StepConfig for DFDP
     * @throws FrameworkException if step creation fails
     */
    private StepConfig createDFDPStepConfig(IdentityProvider identityProvider, String targetAuthenticator) 
            throws FrameworkException {

        StepConfig stepConfig = new StepConfig();
        stepConfig.setOrder(1); // DFDP uses a single step
        stepConfig.setSubjectIdentifierStep(true);
        stepConfig.setSubjectAttributeStep(true);

        // Add federated authenticator configurations
        FederatedAuthenticatorConfig[] federatedConfigs = identityProvider.getFederatedAuthenticatorConfigs();
        if (federatedConfigs == null || federatedConfigs.length == 0) {
            throw new FrameworkException("No federated authenticators configured for IdP: " + 
                                       identityProvider.getIdentityProviderName());
        }

        // Find the target authenticator or use the default one
        FederatedAuthenticatorConfig targetConfig = findTargetAuthenticator(federatedConfigs, targetAuthenticator);
        if (targetConfig == null) {
            targetConfig = identityProvider.getDefaultAuthenticatorConfig();
        }

        if (targetConfig == null) {
            throw new FrameworkException("No suitable authenticator found for IdP: " + 
                                       identityProvider.getIdentityProviderName());
        }

        // Create AuthenticatorConfig for the step
        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        authenticatorConfig.setName(targetConfig.getName());
        authenticatorConfig.setEnabled(true);
        
        // Set authenticator parameters
        Map<String, String> parameterMap = new HashMap<>();
        if (targetConfig.getProperties() != null) {
            for (org.wso2.carbon.identity.application.common.model.Property property : targetConfig.getProperties()) {
                parameterMap.put(property.getName(), property.getValue());
            }
        }
        authenticatorConfig.setParameterMap(parameterMap);

        // Add to step config
        List<AuthenticatorConfig> authenticatorConfigs = new ArrayList<>();
        authenticatorConfigs.add(authenticatorConfig);
        stepConfig.setAuthenticatorList(authenticatorConfigs);

        return stepConfig;
    }

    /**
     * Finds the target authenticator configuration.
     * 
     * @param federatedConfigs Array of federated authenticator configurations
     * @param targetAuthenticator Target authenticator name (optional)
     * @return FederatedAuthenticatorConfig or null if not found
     */
    private FederatedAuthenticatorConfig findTargetAuthenticator(FederatedAuthenticatorConfig[] federatedConfigs,
                                                               String targetAuthenticator) {

        if (StringUtils.isNotBlank(targetAuthenticator)) {
            // Look for specific authenticator
            for (FederatedAuthenticatorConfig config : federatedConfigs) {
                if (targetAuthenticator.equals(config.getName())) {
                    return config;
                }
            }
        }

        // Return the first enabled authenticator if no specific target or target not found
        for (FederatedAuthenticatorConfig config : federatedConfigs) {
            if (config.isEnabled()) {
                return config;
            }
        }

        return null;
    }

    /**
     * Configures authenticator properties for DFDP.
     * 
     * @param stepConfig Step configuration
     * @param identityProvider Identity Provider configuration
     * @param targetAuthenticator Target authenticator name
     */
    private void configureAuthenticatorProperties(StepConfig stepConfig, IdentityProvider identityProvider,
                                                 String targetAuthenticator) {

        // Get the authenticator config from the step
        List<AuthenticatorConfig> authenticatorConfigs = stepConfig.getAuthenticatorList();
        if (authenticatorConfigs != null && !authenticatorConfigs.isEmpty()) {
            AuthenticatorConfig authConfig = authenticatorConfigs.get(0);
            
            // Add DFDP-specific properties to the authenticator parameter map
            Map<String, String> parameterMap = authConfig.getParameterMap();
            if (parameterMap == null) {
                parameterMap = new HashMap<>();
            }
            
            parameterMap.put(FrameworkConstants.DFDP_ENABLED, "true");
            parameterMap.put(FrameworkConstants.DFDP_TARGET_IDP, identityProvider.getIdentityProviderName());
            
            if (StringUtils.isNotBlank(targetAuthenticator)) {
                parameterMap.put(FrameworkConstants.DFDP_TARGET_AUTHENTICATOR, targetAuthenticator);
            }
            
            authConfig.setParameterMap(parameterMap);

            if (log.isDebugEnabled()) {
                log.debug("Configured DFDP properties for authenticator config: " + parameterMap);
            }
        }
    }
}
