package org.wso2.carbon.identity.gateway.demo;

//extends AbstractSequence
public class DemoSequence {
/*
    private AuthenticationStep[] authenticationSteps = null;
    ServiceProvider serviceProvider = null ;

    public DemoSequence(AuthenticationContext authenticationContext) throws IdentityApplicationManagementException {
        super(authenticationContext);

        ApplicationManagementService appInfo = ApplicationManagementService.getInstance();

        ClientAuthenticationRequest initialAuthenticationRequest = (ClientAuthenticationRequest)authenticationContext.getInitialAuthenticationRequest();

        try {
            serviceProvider = appInfo.getServiceProviderByClientId(initialAuthenticationRequest
                                                                           .getUniqueId(), initialAuthenticationRequest.getType(),
                                                                   initialAuthenticationRequest

                                                                           .getTenantDomain());
            if(serviceProvider != null){
                LocalAndOutboundAuthenticationConfig localAndOutBoundAuthenticationConfig =
                        serviceProvider.getLocalAndOutBoundAuthenticationConfig();
                AuthenticationStep[] authenticationSteps =
                        localAndOutBoundAuthenticationConfig.getAuthenticationSteps();
                if(authenticationSteps != null){
                    for(AuthenticationStep authenticationStep : authenticationSteps){
                        IdentityProvider[] federatedIdentityProviders =
                                authenticationStep.getFederatedIdentityProviders();
                        int a = 0 ;
                        if(federatedIdentityProviders != null){
                            for(IdentityProvider identityProviderTmp : federatedIdentityProviders){
                                IdentityProvider identityProvider =
                                        appInfo.getIdentityProvider(identityProviderTmp
                                                                            .getIdentityProviderName(), initialAuthenticationRequest
                                                                            .getTenantDomain());
                                FederatedAuthenticatorConfig defaultAuthenticatorConfig =
                                        identityProviderTmp.getDefaultAuthenticatorConfig();
                                FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                                        identityProvider.getFederatedAuthenticatorConfigs();
                                for(FederatedAuthenticatorConfig federatedAuthenticatorConfig : federatedAuthenticatorConfigs){
                                    if(defaultAuthenticatorConfig.getName().equals(federatedAuthenticatorConfig.getName())){
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
            authenticationStep = authenticationSteps[stepCount-1];
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
*/
}
