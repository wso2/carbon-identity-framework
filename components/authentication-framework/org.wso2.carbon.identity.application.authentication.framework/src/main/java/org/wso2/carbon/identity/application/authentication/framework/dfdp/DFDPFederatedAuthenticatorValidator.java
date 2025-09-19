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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.internal.core.ApplicationAuthenticatorManager;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.List;

/**
 * DFDP Federated Authenticator Validator.
 * This utility class helps validate and test federated authenticator configurations for DFDP.
 */
public class DFDPFederatedAuthenticatorValidator {

    private static final Log log = LogFactory.getLog(DFDPFederatedAuthenticatorValidator.class);

    /**
     * Validates if a given authenticator name corresponds to a valid federated authenticator.
     *
     * @param authenticatorName Name of the authenticator to validate
     * @param tenantDomain Tenant domain
     * @return true if the authenticator is a valid federated authenticator, false otherwise
     */
    public static boolean isValidFederatedAuthenticator(String authenticatorName, String tenantDomain) {
        try {
            // Try to get the authenticator
            ApplicationAuthenticator authenticator = ApplicationAuthenticatorManager.getInstance()
                .getApplicationAuthenticatorByName(authenticatorName, tenantDomain);
            
            // Check if it's a federated authenticator
            if (authenticator instanceof FederatedApplicationAuthenticator) {
                if (log.isDebugEnabled()) {
                    log.debug("Found valid federated authenticator: " + authenticatorName);
                }
                return true;
            }
            
            // Also check system defined authenticators
            authenticator = ApplicationAuthenticatorManager.getInstance()
                .getSystemDefinedAuthenticatorByName(authenticatorName);
            
            if (authenticator instanceof FederatedApplicationAuthenticator) {
                if (log.isDebugEnabled()) {
                    log.debug("Found valid system federated authenticator: " + authenticatorName);
                }
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error validating federated authenticator: " + authenticatorName, e);
            }
            return false;
        }
    }

    /**
     * Gets all available federated authenticators for DFDP testing.
     *
     * @param tenantDomain Tenant domain
     * @return List of federated authenticator names
     */
    public static List<String> getAvailableFederatedAuthenticators(String tenantDomain) {
        List<String> federatedAuthenticators = new ArrayList<>();
        
        try {
            // Get all system defined authenticators and filter federated ones
            for (ApplicationAuthenticator authenticator : 
                ApplicationAuthenticatorManager.getInstance().getSystemDefinedAuthenticators()) {
                if (authenticator instanceof FederatedApplicationAuthenticator) {
                    federatedAuthenticators.add(authenticator.getName());
                }
            }
            
            // Also get user-defined federated authenticators from the registry
            try {
                FederatedAuthenticatorConfig[] configs = IdentityProviderManager.getInstance()
                    .getAllFederatedAuthenticators(tenantDomain);
                    
                if (configs != null) {
                    for (FederatedAuthenticatorConfig config : configs) {
                        if (!federatedAuthenticators.contains(config.getName())) {
                            federatedAuthenticators.add(config.getName());
                        }
                    }
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not retrieve user-defined federated authenticators", e);
                }
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Found " + federatedAuthenticators.size() + " federated authenticators for DFDP testing");
            }
            
        } catch (Exception e) {
            log.error("Error getting available federated authenticators", e);
        }
        
        return federatedAuthenticators;
    }

    /**
     * Provides recommendations for DFDP testing with federated authenticators.
     *
     * @param authenticatorName Name of the authenticator
     * @return Recommendation message
     */
    public static String getDFDPRecommendation(String authenticatorName) {
        if (isValidFederatedAuthenticator(authenticatorName, 
            PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain())) {
            
            return authenticatorName + " is a valid federated authenticator for DFDP testing. " +
                   "This will allow testing of real external IdP claim mappings and authentication flows.";
        } else {
            return authenticatorName + " is not a valid federated authenticator. " +
                   "For DFDP federated testing, use authenticators like: SAML2Authenticator, OIDCAuthenticator, " +
                   "FacebookAuthenticator, GoogleOIDCAuthenticator, etc.";
        }
    }
}
