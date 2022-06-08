package org.wso2.carbon.identity.application.authentication.framework.config.model;

import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationContextOptimizationException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
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

    private class OptimizedAuthStep implements Serializable {

        private int stepOrder;
        private List<String> localAuthenticatorConfigNames;
        private List<String> federatedIdPResourceIds;
        private boolean subjectStep;
        private boolean attributeStep;

        private OptimizedAuthStep(AuthenticationStep authStep, String tenantDomain) throws
                AuthenticationContextOptimizationException {

            this.stepOrder = authStep.getStepOrder();
            this.localAuthenticatorConfigNames = setLocalAuthenticatorConfigNames
                    (authStep.getLocalAuthenticatorConfigs());
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
                AuthenticationContextOptimizationException {

            List<String> federatedIdPResourceIDs = new ArrayList<>();
            IdentityProviderManager manager = IdentityProviderManager.getInstance();
            for (IdentityProvider idp : idPs) {
                if (idp.getResourceId() == null) {
                    try {
                        IdentityProvider identityProvider = manager.
                                getIdPByName(idp.getIdentityProviderName(), tenantDomain);
                        if (identityProvider == null) {
                            throw new AuthenticationContextOptimizationException(String.format(
                                    "Cannot find the Identity Provider by the name: %s tenant domain: %s",
                                    idp.getIdentityProviderName(), tenantDomain));
                        }
                        federatedIdPResourceIDs.add(identityProvider.getResourceId());
                    } catch (IdentityProviderManagementException e) {
                        throw new AuthenticationContextOptimizationException(String.format(
                                "Failed to get the Identity Provider by name: %s tenant domain: %s",
                                 idp.getIdentityProviderName(), tenantDomain), e);
                    }

                } else {
                    federatedIdPResourceIDs.add(idp.getResourceId());
                }
            }
            return federatedIdPResourceIDs;
        }

        public int getStepOrder() {

            return stepOrder;
        }

        public List<String> getLocalAuthenticatorConfigNames() {

            return localAuthenticatorConfigNames;
        }

        public List<String> getFederatedIdPResourceIds() {

            return federatedIdPResourceIds;
        }

        public boolean isSubjectStep() {

            return subjectStep;
        }

        public boolean isAttributeStep() {

            return attributeStep;
        }
    }

    public OptimizedApplicationConfig(ApplicationConfig applicationConfig, String tenantDomain) throws
            AuthenticationContextOptimizationException {

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

    public String getServiceProviderResourceId() {

        return serviceProviderResourceId;
    }

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
        IdentityProviderManager manager = IdentityProviderManager.getInstance();
        for (int i = 0; i < federatedIdPResourceIds.size(); i++) {
            try {
                IdentityProvider idp = manager.getIdPByResourceId(federatedIdPResourceIds.get(i), tenantDomain,
                        false);
                if (idp == null) {
                    throw new AuthenticationContextOptimizationException(
                            String.format("Cannot find the IdP by the resource Id: %s Tenant Domain: %s",
                                    federatedIdPResourceIds.get(i), tenantDomain));
                }
                idPs[i] = idp;
            } catch (IdentityProviderManagementException e) {
                throw new AuthenticationContextOptimizationException(
                        String.format("Failed to get the IdP by the name: %s Tenant Domain: %s",
                                federatedIdPResourceIds.get(i), tenantDomain), e);
            }
        }
        return idPs;
    }

    public boolean isMappedSubjectIDSelected() {

        return mappedSubjectIDSelected;
    }

    public Map<String, String> getClaimMappings() {

        return claimMappings;
    }

    public Map<String, String> getRoleMappings() {

        return roleMappings;
    }

    public Map<String, String> getRequestedClaims() {

        return requestedClaims;
    }

    public Map<String, String> getMandatoryClaims() {

        return mandatoryClaims;
    }
}
