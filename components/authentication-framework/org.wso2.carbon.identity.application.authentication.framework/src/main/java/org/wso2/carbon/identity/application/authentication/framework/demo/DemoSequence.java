package org.wso2.carbon.identity.application.authentication.framework.demo;

import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl.model
        .AbstractSequence;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;


public class DemoSequence extends AbstractSequence{
    @Override
    public RequestPathAuthenticatorConfig[] getRequestPathAuthenticatorConfig() {
        return new RequestPathAuthenticatorConfig[0];
    }

    @Override
    public AuthenticationStep[] getStepAuthenticatorConfig() {
        AuthenticationStep[] as = new  AuthenticationStep[1];
        AuthenticationStep authenticationStep = new AuthenticationStep();
        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName("SAMLSSOAuthenticator");
        identityProvider.setEnable(true);
        identityProvider.setAlias("SAMLSSOAuthenticator");
        authenticationStep.setFederatedIdentityProviders(new IdentityProvider[]{identityProvider});
        as[0] = authenticationStep ;
        return as;
    }

    @Override
    public boolean isRequestPathAuthenticatorsAvailable() {
        return false;
    }
}
