package org.wso2.carbon.identity.application.authentication.framework.demo;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl.model
        .AbstractSequence;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl.model.Step;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;


public class DemoSequence extends AbstractSequence {

    private AuthenticationStep[] authenticationSteps = null;

    public DemoSequence(AuthenticationContext authenticationContext) {
        super(authenticationContext);
        authenticationSteps = new AuthenticationStep[1];
        AuthenticationStep authenticationStep = new AuthenticationStep();
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName("demo-idp");
        identityProvider.setEnable(true);
        identityProvider.setAlias("demo-idp");

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        federatedAuthenticatorConfig.setDisplayName("demo-idp");
        federatedAuthenticatorConfig.setEnabled(true);

        identityProvider.setDefaultAuthenticatorConfig(federatedAuthenticatorConfig);

        authenticationStep.setFederatedIdentityProviders(new IdentityProvider[]{identityProvider});
        authenticationSteps[0] = authenticationStep;
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
        return true;
    }

    @Override
    public Step getStep(int stepCount) {
        AuthenticationStep authenticationStep = null;
        if (authenticationSteps.length >= stepCount) {
            authenticationStep = authenticationSteps[stepCount];
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
            if (tmpIdentityProvider.getDisplayName().equals(identityProviderName)) {
                identityProvider = tmpIdentityProvider;
                break;
            }
        }
        return identityProvider;
    }

}
