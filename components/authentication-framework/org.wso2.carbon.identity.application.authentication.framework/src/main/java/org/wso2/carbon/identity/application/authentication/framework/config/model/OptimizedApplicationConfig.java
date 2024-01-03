/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.lang.StringUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationClientException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationServerException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.createIdPClone;

/**
 * This class is used to have the mandatory attributes of the application config class.
 */
public class OptimizedApplicationConfig implements Serializable {

    private static final long serialVersionUID = -6197084910144813735L;

    private String serviceProviderResourceId;
    private List<OptimizedAuthStep> optimizedAuthSteps;
    private boolean mappedSubjectIDSelected;
    private Map<String, String> claimMappings;
    private Map<String, String> roleMappings;
    private Map<String, String> requestedClaims;
    private Map<String, String> mandatoryClaims;

    private static final Log LOG = LogFactory.getLog(OptimizedApplicationConfig.class);

    private class OptimizedAuthStep implements Serializable {

        private int stepOrder;
        private List<String> localAuthenticatorConfigNames;
        private List<OptimizedFederatedIdP> optimizedFederatedIdPs;
        private List<String> federatedIdPResourceIds;
        private boolean subjectStep;
        private boolean attributeStep;

        private OptimizedAuthStep(AuthenticationStep authStep, String tenantDomain) throws
                SessionDataStorageOptimizationException {

            this.stepOrder = authStep.getStepOrder();
            this.localAuthenticatorConfigNames =
                    setLocalAuthenticatorConfigNames(authStep.getLocalAuthenticatorConfigs());
            this.optimizedFederatedIdPs =
                    setOptimizedFederatedIdPs(authStep.getFederatedIdentityProviders(), tenantDomain);
            this.subjectStep = authStep.isSubjectStep();
            this.attributeStep = authStep.isAttributeStep();
        }

        /**
         * This class is used to have the mandatory attributes of the federated identity provider class.
         * The entire IDP object can be retrieved back from this class.
         */
        private class OptimizedFederatedIdP implements Serializable {

            private String idpResourceId;
            private String selectedAuthenticatorName;

            private OptimizedFederatedIdP(String idpResourceId, String selectedAuthenticatorName) {

                this.idpResourceId = idpResourceId;
                this.selectedAuthenticatorName = selectedAuthenticatorName;
            }

            /**
             * This method is used to get the IdP resource Id of optimized federated Idp.
             *
             * @return Subject step.
             */
            public String getIdpResourceId() {

                return idpResourceId;
            }

            /**
             * This method is used to get selected authenticator name of optimized federated Idp.
             *
             * @return Subject step.
             */
            public String getSelectedAuthenticatorName() {

                return selectedAuthenticatorName;
            }
        }

        private List<String> setLocalAuthenticatorConfigNames(LocalAuthenticatorConfig[] localAuthenticatorConfigs) {

            List<String> localAuthConfigNames = new ArrayList<>();
            for (LocalAuthenticatorConfig authConfig : localAuthenticatorConfigs) {
                localAuthConfigNames.add(authConfig.getName());
            }
            return localAuthConfigNames;
        }

        /**
         * This method is used to get the optimized federated IdPs from authentication step federated IdPs.
         *
         * @param idPsFromAuthStep FederatedIdentityProviders of authentication step.
         * @param tenantDomain     Tenant domain of the application.
         * @return List of optimized federated IdPs.
         * @throws SessionDataStorageOptimizationException If an error occurs when getting federated identity providers.
         */
        private List<OptimizedFederatedIdP> setOptimizedFederatedIdPs(IdentityProvider[] idPsFromAuthStep,
                                                                      String tenantDomain)
                throws SessionDataStorageOptimizationException {

            List<OptimizedFederatedIdP> optimizedFederatedIdPs = new ArrayList<>();
            IdentityProviderManager manager = IdentityProviderManager.getInstance();
            for (IdentityProvider idPFromAuthStep : idPsFromAuthStep) {
                if (StringUtils.isBlank(idPFromAuthStep.getResourceId())) {
                    try {
                        IdentityProvider idPByName =
                                manager.getIdPByName(idPFromAuthStep.getIdentityProviderName(), tenantDomain);
                        if (idPByName == null) {
                            throw new SessionDataStorageOptimizationException(String.format(
                                    "Cannot find the Identity Provider by the name: %s tenant domain: %s",
                                    idPFromAuthStep.getIdentityProviderName(), tenantDomain));
                        }
                        optimizedFederatedIdPs.add(new OptimizedFederatedIdP(idPByName.getResourceId(),
                                idPFromAuthStep.getDefaultAuthenticatorConfig().getName()));

                    } catch (IdentityProviderManagementException e) {
                        throw new SessionDataStorageOptimizationException(String.format(
                                "Failed to get the Identity Provider by name: %s tenant domain: %s",
                                idPFromAuthStep.getIdentityProviderName(), tenantDomain), e);
                    }
                } else {
                    optimizedFederatedIdPs.add(new OptimizedFederatedIdP(idPFromAuthStep.getResourceId(),
                            idPFromAuthStep.getDefaultAuthenticatorConfig().getName()));
                }
            }
            return optimizedFederatedIdPs;
        }

        /**
         * This method is used to get the step order of the authentication step.
         *
         * @return Step order.
         */
        public int getStepOrder() {

            return stepOrder;
        }

        /**
         * This method is used to get the local authenticator config names of authentication step.
         *
         * @return List of authenticator config names.
         */
        public List<String> getLocalAuthenticatorConfigNames() {

            return localAuthenticatorConfigNames;
        }

        /**
         * This method is used to get the resource ids of the federated IdPs of authentication step.
         * @deprecated This method is deprecated. Use {@link #getOptimizedFederatedIdPs()} instead.
         *
         * @return List of resource ids.
         */
        @Deprecated
        public List<String> getFederatedIdPResourceIds() {

            if (federatedIdPResourceIds == null || federatedIdPResourceIds.isEmpty()) {
                List<String> idPResourceIds = new ArrayList<>();
                for (OptimizedFederatedIdP optimizedFederatedIdP : optimizedFederatedIdPs) {
                    idPResourceIds.add(optimizedFederatedIdP.getIdpResourceId());
                }
                return idPResourceIds;
            }
            return federatedIdPResourceIds;
        }

        /**
         * This method is used to get the Optimized federated IDPs of authentication step.
         *
         * @return List of optimized federated IdPs.
         */
        public List<OptimizedFederatedIdP> getOptimizedFederatedIdPs() {

            return optimizedFederatedIdPs;
        }

        /**
         * This method is used to get the subject step of the authentication step.
         *
         * @return Subject step.
         */
        public boolean isSubjectStep() {

            return subjectStep;
        }

        /**
         * This method is used to get the attribute step of the authentication step.
         *
         * @return Attribute step.
         */
        public boolean isAttributeStep() {

            return attributeStep;
        }
    }

    /**
     * Initialize optimized application config.
     *
     * @param applicationConfig Application config.
     * @param tenantDomain Tenant domain of the application.
     * @throws SessionDataStorageOptimizationException Error when optimizing authentication step.
     */
    public OptimizedApplicationConfig(ApplicationConfig applicationConfig, String tenantDomain) throws
            SessionDataStorageOptimizationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Optimization process for the application config has started with service provider resource id: "
                    + applicationConfig.getServiceProvider().getApplicationResourceId() + " and tenant domain: "
                    + tenantDomain + ".");
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
     * @return Resource id of the service provider.
     */
    public String getServiceProviderResourceId() {

        return serviceProviderResourceId;
    }

    /**
     * This method is used to get the array of authentication steps using optimized authentication steps.
     *
     * @param tenantDomain Tenant domain of the application.
     * @return Array of authentication step.
     * @throws FrameworkException Error when getting federated identity providers.
     */
    public AuthenticationStep[] getAuthenticationSteps(String tenantDomain) throws FrameworkException {

        AuthenticationStep[] authenticationSteps = new AuthenticationStep[this.optimizedAuthSteps.size()];
        for (int i = 0; i < this.optimizedAuthSteps.size(); i++) {
            OptimizedAuthStep authStep = optimizedAuthSteps.get(i);
            AuthenticationStep authenticationStep = new AuthenticationStep();
            authenticationStep.setStepOrder(authStep.getStepOrder());
            authenticationStep.setLocalAuthenticatorConfigs(
                    getLocalAuthenticatorConfigs(authStep.getLocalAuthenticatorConfigNames()));
            if (authStep.federatedIdPResourceIds == null || authStep.federatedIdPResourceIds.isEmpty()) {
                // For new caches federatedIdPResourceIds will be null. But will have optimized federated IdPs.
                authenticationStep.setFederatedIdentityProviders(getIdPsFromOptimizedFederatedIdPs(
                        authStep.getOptimizedFederatedIdPs(), tenantDomain));
            } else {
                // For old caches we don't have optimized federated IdPs. So need to get IdPs from resource Ids.
                authenticationStep.setFederatedIdentityProviders(getFederatedIdPs(
                        authStep.getFederatedIdPResourceIds(), tenantDomain));
            }
            authenticationStep.setSubjectStep(authStep.isSubjectStep());
            authenticationStep.setAttributeStep(authStep.isAttributeStep());
            authenticationSteps[i] = authenticationStep;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading the optimized authentication steps is completed successfully");
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
                    throw new SessionDataStorageOptimizationClientException(
                            String.format("Cannot find the IdP by the resource Id: %s Tenant Domain: %s",
                                    federatedIdPResourceIds.get(i), tenantDomain));
                }
                idPs[i] = idp;
            } catch (IdentityProviderManagementClientException e) {
                throw new SessionDataStorageOptimizationClientException(
                        String.format("IDP management client error. Failed to get the IdP by the name: %s " +
                                "Tenant Domain: %s", federatedIdPResourceIds.get(i), tenantDomain), e);
            } catch (IdentityProviderManagementServerException e) {
                throw new SessionDataStorageOptimizationServerException(
                        String.format("IDP management server error. Failed to get the IdP by the name: %s " +
                                "Tenant Domain: %s", federatedIdPResourceIds.get(i), tenantDomain), e);
            } catch (IdentityProviderManagementException e) {
                throw new SessionDataStorageOptimizationServerException(
                        String.format("IDP management error. Failed to get the IdP by the name: %s " +
                                "Tenant Domain: %s", federatedIdPResourceIds.get(i), tenantDomain), e);
            }
        }
        return idPs;
    }

    /**
     * This method is used to get the array of federated IdPs using optimized federated identity providers.
     *
     * @param optimizedFederatedIdPs Optimized federated identity providers.
     * @param tenantDomain Tenant domain of the application.
     * @return Array of federated identity providers.
     * @throws FrameworkException If any error occurs when getting federated identity providers.
     */
    private IdentityProvider[] getIdPsFromOptimizedFederatedIdPs(
            List<OptimizedAuthStep.OptimizedFederatedIdP> optimizedFederatedIdPs, String tenantDomain)
            throws FrameworkException {

        List<IdentityProvider> idPList = new ArrayList<>();
        IdentityProviderManager manager =
                (IdentityProviderManager) FrameworkServiceDataHolder.getInstance().getIdentityProviderManager();
        for (OptimizedAuthStep.OptimizedFederatedIdP optimizedFederatedIdP : optimizedFederatedIdPs) {
            try {
                IdentityProvider idPByResourceId = manager.getIdPByResourceId(optimizedFederatedIdP.getIdpResourceId(),
                        tenantDomain, false);
                if (idPByResourceId == null) {
                    throw new SessionDataStorageOptimizationClientException(
                            String.format("Cannot find the IdP by the resource Id: %s Tenant Domain: %s",
                                    optimizedFederatedIdP.getIdpResourceId(), tenantDomain));
                }
                if (StringUtils.equals(idPByResourceId.getDefaultAuthenticatorConfig().getName(),
                        optimizedFederatedIdP.getSelectedAuthenticatorName())) {
                    idPList.add(idPByResourceId);
                } else {
                    /**
                     * For applications that use custom connector IdPs as a sign-in method, admin can select and use one
                     * of the authenticators available in the custom connector IDP, other than the default authenticator
                     * of the IDP. So need to find the exact authenticator configured in the SP.
                     */
                    IdentityProvider clonedIdP = createIdPClone(idPByResourceId);
                    for (FederatedAuthenticatorConfig fedAuthConfig :
                            idPByResourceId.getFederatedAuthenticatorConfigs()) {
                        if (StringUtils.equals(fedAuthConfig.getName(),
                                optimizedFederatedIdP.getSelectedAuthenticatorName())) {
                            clonedIdP.setDefaultAuthenticatorConfig(fedAuthConfig);
                            break;
                        }
                    }
                    idPList.add(clonedIdP);
                }
            } catch (IdentityProviderManagementClientException e) {
                throw new SessionDataStorageOptimizationClientException(
                        String.format("IDP management client error. Failed to get the IdP by the name: %s " +
                                "Tenant Domain: %s", optimizedFederatedIdP.getIdpResourceId(), tenantDomain), e);
            } catch (IdentityProviderManagementServerException e) {
                throw new SessionDataStorageOptimizationServerException(
                        String.format("IDP management server error. Failed to get the IdP by the resource Id: %s " +
                                "Tenant Domain: %s", optimizedFederatedIdP.getIdpResourceId(), tenantDomain), e);
            } catch (IdentityProviderManagementException e) {
                throw new SessionDataStorageOptimizationServerException(
                        String.format("IDP management error. Failed to get the IdP by the resource Id: %s " +
                                "Tenant Domain: %s", optimizedFederatedIdP.getIdpResourceId(), tenantDomain), e);
            }
        }
        return idPList.toArray(new IdentityProvider[0]);
    }

    /**
     * This method is used to get is the mapped subject id selected.
     *
     * @return Mapped subject id selected.
     */
    public boolean isMappedSubjectIDSelected() {

        return mappedSubjectIDSelected;
    }

    /**
     * This method is used to get the claim mappings.
     *
     * @return Claim mappings.
     */
    public Map<String, String> getClaimMappings() {

        return claimMappings;
    }

    /**
     * This method is used to get the role mappings.
     *
     * @return Role mappings.
     */
    public Map<String, String> getRoleMappings() {

        return roleMappings;
    }

    /**
     * This method is used to the requested claims.
     *
     * @return Requested claims.
     */
    public Map<String, String> getRequestedClaims() {

        return requestedClaims;
    }

    /**
     * This method is used to get the mandatory claims.
     *
     * @return Mandatory claims.
     */
    public Map<String, String> getMandatoryClaims() {

        return mandatoryClaims;
    }
}
