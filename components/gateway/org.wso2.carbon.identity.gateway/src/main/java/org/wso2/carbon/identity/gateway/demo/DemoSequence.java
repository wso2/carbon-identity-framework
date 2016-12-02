package org.wso2.carbon.identity.gateway.demo;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.AbstractSequence;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.Step;
import org.wso2.carbon.identity.gateway.request.ClientAuthenticationRequest;


public class DemoSequence extends AbstractSequence {

    private AuthenticationStep[] authenticationSteps = null;
    ServiceProvider serviceProvider = null;

    public DemoSequence(AuthenticationContext authenticationContext) throws IdentityApplicationManagementException {
        super(authenticationContext);
        /*authenticationSteps = new AuthenticationStep[1];
        AuthenticationStep authenticationStep = new AuthenticationStep();
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName("demo-idp");
        identityProvider.setEnable(true);
        identityProvider.setAlias("demo-idp");

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setName("SAMLSSOAuthenticator");
        federatedAuthenticatorConfig.setEnabled(true);

        identityProvider.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);

        authenticationStep.setFederatedIdentityProviders(new IdentityProvider[]{identityProvider});
        authenticationSteps[0] = authenticationStep;

        */
        ApplicationManagementService appInfo = ApplicationManagementService.getInstance();

        ClientAuthenticationRequest initialAuthenticationRequest = (ClientAuthenticationRequest) authenticationContext.getInitialAuthenticationRequest();

        try {
            serviceProvider = appInfo.getServiceProviderByClientId(initialAuthenticationRequest
                            .getUniqueId(), initialAuthenticationRequest.getType(),
                    initialAuthenticationRequest.getTenantDomain());
            if (serviceProvider != null) {
                LocalAndOutboundAuthenticationConfig localAndOutBoundAuthenticationConfig =
                        serviceProvider.getLocalAndOutBoundAuthenticationConfig();
                AuthenticationStep[] authenticationSteps =
                        localAndOutBoundAuthenticationConfig.getAuthenticationSteps();
                if (authenticationSteps != null) {
                    for (AuthenticationStep authenticationStep : authenticationSteps) {
                        IdentityProvider[] federatedIdentityProviders =
                                authenticationStep.getFederatedIdentityProviders();
                        int a = 0;
                        if (federatedIdentityProviders != null) {
                            for (IdentityProvider identityProviderTmp : federatedIdentityProviders) {
                                IdentityProvider identityProvider =
                                        appInfo.getIdentityProvider(identityProviderTmp
                                                .getIdentityProviderName(), initialAuthenticationRequest
                                                .getTenantDomain());
                                FederatedAuthenticatorConfig defaultAuthenticatorConfig =
                                        identityProviderTmp.getDefaultAuthenticatorConfig();
                                FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                                        identityProvider.getFederatedAuthenticatorConfigs();
                                for (FederatedAuthenticatorConfig federatedAuthenticatorConfig : federatedAuthenticatorConfigs) {
                                    if (defaultAuthenticatorConfig.getName().equals(federatedAuthenticatorConfig.getName())) {
                                        identityProviderTmp.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);
                                    }
                                }


                            }
                        }
                    }
                }
            }
        } catch (IdentityApplicationManagementException e) {
            e.printStackTrace();
        }
        authenticationSteps = serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();


    }

    @Override
    public RequestPathAuthenticatorConfig[] getRequestPathAuthenticatorConfig() {
        return new RequestPathAuthenticatorConfig[0];
    }

    @Override
    public AuthenticationStep[] getStepAuthenticatorConfig() {
        return authenticationSteps;
    }

    @Override
    public boolean isRequestPathAuthenticatorsAvailable() {
        return false;
    }

    @Override
    public boolean isStepAuthenticatorAvailable() {
        return true;
    }

    @Override
    public boolean hasNext(int step) {
        if (authenticationSteps.length >= step) {
            return true;
        }
        return false;
    }

    @Override
    public Step getStep(int stepCount) {
        AuthenticationStep authenticationStep = null;
        if (stepCount > 0 && authenticationSteps.length >= stepCount) {
            authenticationStep = authenticationSteps[stepCount - 1];
        }
        Step step = new Step();
        step.setAuthenticationStep(authenticationStep);
        return step;
    }

    @Override
    public LocalAuthenticatorConfig getLocalAuthenticatorConfigForSingleOption(int step) {
        LocalAuthenticatorConfig localAuthenticatorConfig = null;
        AuthenticationStep authenticatorStep = getStep(step).getAuthenticationStep();
        LocalAuthenticatorConfig[] localAuthenticatorConfigs = authenticatorStep.getLocalAuthenticatorConfigs();
        if (localAuthenticatorConfigs.length == 1) {
            localAuthenticatorConfig = localAuthenticatorConfigs[0];
        }
        return localAuthenticatorConfig;
    }

    @Override
    public IdentityProvider getFederatedIdentityProviderForSingleOption(int step) {
        IdentityProvider identityProvider = null;
        AuthenticationStep authenticatorStep = getStep(step).getAuthenticationStep();
        IdentityProvider[] federatedIdentityProviders = authenticatorStep.getFederatedIdentityProviders();

        if (federatedIdentityProviders.length == 1) {
            identityProvider = federatedIdentityProviders[0];
        }
        return identityProvider;
    }

    @Override
    public LocalAuthenticatorConfig getLocalAuthenticatorConfig(int step, String authenticatorName) {
        LocalAuthenticatorConfig localAuthenticatorConfig = null;
        AuthenticationStep authenticatorStep = getStep(step).getAuthenticationStep();
        LocalAuthenticatorConfig[] localAuthenticatorConfigs = authenticatorStep.getLocalAuthenticatorConfigs();
        for (LocalAuthenticatorConfig tmpAuthenticatorConfig : localAuthenticatorConfigs) {
            if (tmpAuthenticatorConfig.getName().equals(authenticatorName)) {
                localAuthenticatorConfig = tmpAuthenticatorConfig;
                break;
            }
        }
        return localAuthenticatorConfig;
    }

    @Override
    public IdentityProvider getFederatedIdentityProvider(int step, String identityProviderName) {
        IdentityProvider identityProvider = null;
        AuthenticationStep authenticatorStep = getStep(step).getAuthenticationStep();
        IdentityProvider[] federatedIdentityProviders = authenticatorStep.getFederatedIdentityProviders();
        for (IdentityProvider tmpIdentityProvider : federatedIdentityProviders) {
            if (tmpIdentityProvider.getIdentityProviderName().equals(identityProviderName)) {
                identityProvider = tmpIdentityProvider;
                break;
            }
        }
        return identityProvider;
    }

}
