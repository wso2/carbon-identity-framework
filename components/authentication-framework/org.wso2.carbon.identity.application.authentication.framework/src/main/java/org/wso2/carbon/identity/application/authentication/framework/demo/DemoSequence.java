package org.wso2.carbon.identity.application.authentication.framework.demo;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl.model
        .AbstractSequence;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;


public class DemoSequence extends AbstractSequence{

    private AuthenticationStep[] authenticationSteps = null ;

    public DemoSequence(AuthenticationContext authenticationContext){
        super(authenticationContext);
        authenticationSteps = new  AuthenticationStep[1];
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
        authenticationSteps[0] = authenticationStep ;
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
    public boolean getAuthenticatorConfig(int step, String authenticatorName) {
        return false;
    }

    @Override
    public boolean getLocalAuthenticatorConfig(int step, String authenticatorName) {
        return false;
    }

    @Override
    public FederatedAuthenticatorConfig getFederatedAuthenticatorConfig(int step, String authenticatorName) {
        AuthenticationStep authenticationStep = authenticationSteps[step];
        IdentityProvider[] federatedIdentityProviders = authenticationStep.getFederatedIdentityProviders();
        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                federatedIdentityProviders[0].getFederatedAuthenticatorConfigs();
        return federatedAuthenticatorConfigs[0];
    }
}
