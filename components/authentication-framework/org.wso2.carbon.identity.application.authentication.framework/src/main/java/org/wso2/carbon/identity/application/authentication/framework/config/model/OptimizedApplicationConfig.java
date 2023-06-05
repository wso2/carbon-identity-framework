/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.SessionDataStorageOptimizationException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to have the mandatory attributes of the application config class.
 */
public class OptimizedApplicationConfig implements Serializable {

    private String serviceProviderResourceId;
    private List<OptimizedAuthStep> optimizedAuthSteps;
    private boolean mappedSubjectIDSelected;
    private Map<String, String> claimMappings;
    private Map<String, String> roleMappings;
    private Map<String, String> requestedClaims;
    private Map<String, String> mandatoryClaims;

    private static final Log log = LogFactory.getLog(OptimizedApplicationConfig.class);

    private class OptimizedAuthStep implements Serializable {

        private int stepOrder;
        private List<String> localAuthenticatorConfigNames;
        private List<String> federatedIdPResourceIds;
        private boolean subjectStep;
        private boolean attributeStep;

        private OptimizedAuthStep(AuthenticationStep authStep, String tenantDomain) throws
                SessionDataStorageOptimizationException {

            this.stepOrder = authStep.getStepOrder();
            this.localAuthenticatorConfigNames =
                    setLocalAuthenticatorConfigNames(authStep.getLocalAuthenticatorConfigs());
            this.federatedIdPResourceIds = setFederatedIdPResourceIds(authStep.getFederatedIdentityProviders(),
                    tenantDomain);
            this.subjectStep = authStep.isSubjectStep();
            this.attributeStep = authStep.isAttributeStep();
        }

        private List<String> setLocalAuthenticatorConfigNames(LocalAuthenticatorConfig[] localAuthenticatorConfigs) {

            List<String> localAuthConfigNames = new ArrayList<>();
            for (LocalAuthenticatorConfig authConfig : localAuthenticatorConfigs) {
                localAuthConfigNames.add(authConfig.getName());
            }
            return localAuthConfigNames;
        }

        private List<String> setFederatedIdPResourceIds(IdentityProvider[] idPs, String tenantDomain) throws
                SessionDataStorageOptimizationException {

            List<String> federatedIdPResourceIDs = new ArrayList<>();
            IdentityProviderManager manager = IdentityProviderManager.getInstance();
            for (IdentityProvider idp : idPs) {
                if (idp.getResourceId() == null) {
                    try {
                        IdentityProvider identityProvider = manager.
                                getIdPByName(idp.getIdentityProviderName(), tenantDomain);
                        if (identityProvider == null) {
                            throw new SessionDataStorageOptimizationException(String.format(
                                    "Cannot find the Identity Provider by the name: %s tenant domain: %s",
                                    idp.getIdentityProviderName(), tenantDomain));
                        }
                        federatedIdPResourceIDs.add(identityProvider.getResourceId());
                    } catch (IdentityProviderManagementException e) {
                        throw new SessionDataStorageOptimizationException(String.format(
                                "Failed to get the Identity Provider by name: %s tenant domain: %s",
                                 idp.getIdentityProviderName(), tenantDomain), e);
                    }

                } else {
                    federatedIdPResourceIDs.add(idp.getResourceId());
                }
            }
            return federatedIdPResourceIDs;
        }

        /**
         * This method is used to get the step order of the authentication step.
         *
         * @return step order
         */
        public int getStepOrder() {

            return stepOrder;
        }

        /**
         * This method is used to get the local authenticator config names of authentication step.
         *
         * @return list of authenticator config names
         */
        public List<String> getLocalAuthenticatorConfigNames() {

            return localAuthenticatorConfigNames;
        }

        /**
         * This method is used to get the resource ids of the federated IdPs of authentication step.
         *
         * @return list of resource ids
         */
        public List<String> getFederatedIdPResourceIds() {

            return federatedIdPResourceIds;
        }

        /**
         * This method is used to get the subject step of the authentication step.
         *
         * @return subject step
         */
        public boolean isSubjectStep() {

            return subjectStep;
        }

        /**
         * This method is used to get the attribute step of the authentication step.
         *
         * @return attribute step
         */
        public boolean isAttributeStep() {

            return attributeStep;
        }
    }

    /**
     * Initialize optimized application config.
     *
     * @param applicationConfig application config
     * @param tenantDomain tenant domain of the application
     * @throws SessionDataStorageOptimizationException Error when optimizing authentication step
     */
    public OptimizedApplicationConfig(ApplicationConfig applicationConfig, String tenantDomain) throws
            SessionDataStorageOptimizationException {

        if (log.isDebugEnabled()) {
            log.debug("Optimization process for the application config has started");
        }
        this.serviceProviderResourceId = applicationConfig.getServiceProvider().getApplicationResourceId();
        List<OptimizedAuthStep> optimizedAuthSteps = new ArrayList<>();
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = applicationConfig.
                getServiceProvider().getLocalAndOutBoundAuthenticationConfig();
        if (localAndOutboundAuthenticationConfig != null) {
            for (AuthenticationStep authStep : localAndOutboundAuthenticationConfig.getAuthenticationSteps()) {
                OptimizedAuthStep optimizedAuthStep = new OptimizedAuthStep(authStep, tenantDomain);
                optimizedAuthSteps.add(optimizedAuthStep);
            }
        }
        this.optimizedAuthSteps = optimizedAuthSteps;
        this.mappedSubjectIDSelected = applicationConfig.isMappedSubjectIDSelected();
        this.claimMappings = applicationConfig.getClaimMappings();
        this.roleMappings = applicationConfig.getRoleMappings();
        this.mandatoryClaims = applicationConfig.getMandatoryClaimMappings();
        this.requestedClaims = applicationConfig.getRequestedClaimMappings();
    }

    /**
     * This method is used to get the service provider's resource id.
     *
     * @return resource id of the service provider
     */
    public String getServiceProviderResourceId() {

        return serviceProviderResourceId;
    }

    /**
     * This method is used to get the array of authentication steps using optimized authentication steps.
     *
     * @param tenantDomain tenant domain of the application
     * @return array of authentication step
     * @throws FrameworkException Error when getting federated identity providers
     */
    public AuthenticationStep[] getAuthenticationSteps(String tenantDomain) throws FrameworkException {

        AuthenticationStep[] authenticationSteps = new AuthenticationStep[this.optimizedAuthSteps.size()];
        for (int i = 0; i < this.optimizedAuthSteps.size(); i++) {
            OptimizedAuthStep authStep = optimizedAuthSteps.get(i);
            AuthenticationStep authenticationStep = new AuthenticationStep();
            authenticationStep.setStepOrder(authStep.getStepOrder());
            authenticationStep.setLocalAuthenticatorConfigs(
                    getLocalAuthenticatorConfigs(authStep.getLocalAuthenticatorConfigNames()));
            authenticationStep.setFederatedIdentityProviders(getFederatedIdPs(
                    authStep.getFederatedIdPResourceIds(), tenantDomain));
            authenticationStep.setSubjectStep(authStep.isSubjectStep());
            authenticationStep.setAttributeStep(authStep.isAttributeStep());
            authenticationSteps[i] = authenticationStep;
        }
        if (log.isDebugEnabled()) {
            log.debug("Loading the optimized authentication steps is completed successfully");
        }
        return authenticationSteps;
    }

    private LocalAuthenticatorConfig[] getLocalAuthenticatorConfigs(List<String> localAuthConfigNames) {

        LocalAuthenticatorConfig[] localAuthenticatorConfigs = new
                LocalAuthenticatorConfig[localAuthConfigNames.size()];
        for (int i = 0; i < localAuthConfigNames.size(); i++) {
            localAuthenticatorConfigs[i] = ApplicationAuthenticatorService.getInstance().
                    getLocalAuthenticatorByName(localAuthConfigNames.get(i));
        }
        return localAuthenticatorConfigs;
    }

    private IdentityProvider[] getFederatedIdPs(List<String> federatedIdPResourceIds, String tenantDomain)
            throws FrameworkException {

        IdentityProvider[] idPs = new IdentityProvider[federatedIdPResourceIds.size()];
        IdentityProviderManager manager =
                (IdentityProviderManager) FrameworkServiceDataHolder.getInstance().getIdentityProviderManager();
        for (int i = 0; i < federatedIdPResourceIds.size(); i++) {
            try {
                IdentityProvider idp = manager.getIdPByResourceId(federatedIdPResourceIds.get(i), tenantDomain,
                        false);
                if (idp == null) {
                    throw new SessionDataStorageOptimizationException(
                            String.format("Cannot find the IdP by the resource Id: %s Tenant Domain: %s",
                                    federatedIdPResourceIds.get(i), tenantDomain));
                }
                idPs[i] = idp;
            } catch (IdentityProviderManagementException e) {
                throw new SessionDataStorageOptimizationException(
                        String.format("Failed to get the IdP by the name: %s Tenant Domain: %s",
                                federatedIdPResourceIds.get(i), tenantDomain), e);
            }
        }
        return idPs;
    }

    /**
     * This method is used to get is the mapped subject id selected.
     *
     * @return mapped subject id selected
     */
    public boolean isMappedSubjectIDSelected() {

        return mappedSubjectIDSelected;
    }

    /**
     * This method is used to get the claim mappings.
     *
     * @return claim mappings
     */
    public Map<String, String> getClaimMappings() {

        return claimMappings;
    }

    /**
     * This method is used to get the role mappings.
     *
     * @return role mappings
     */
    public Map<String, String> getRoleMappings() {

        return roleMappings;
    }

    /**
     * This method is used to the requested claims.
     *
     * @return requested claims
     */
    public Map<String, String> getRequestedClaims() {

        return requestedClaims;
    }

    /**
     * This method is used to get the mandatory claims.
     *
     * @return mandatory claims.
     */
    public Map<String, String> getMandatoryClaims() {

        return mandatoryClaims;
    }
}
