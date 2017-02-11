package org.wso2.carbon.identity.gateway.processor.handler.authentication;


import org.wso2.carbon.identity.gateway.common.model.AuthenticationStep;
import org.wso2.carbon.identity.gateway.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.AbstractSequence;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.Step;

public class DefaultAbstractSequence extends AbstractSequence {

    public DefaultAbstractSequence(AuthenticationContext authenticationContext) {
        super(authenticationContext);
    }

    public DefaultAbstractSequence() {
    }

    @Override
    public RequestPathAuthenticatorConfig[] getRequestPathAuthenticatorConfig() {
        return new RequestPathAuthenticatorConfig[0];
    }

    @Override
    public AuthenticationStep[] getStepAuthenticatorConfig() {
        return new AuthenticationStep[0];
    }

    @Override
    public boolean isRequestPathAuthenticatorsAvailable() {
        return false;
    }

    @Override
    public boolean isStepAuthenticatorAvailable() {
        return false;
    }

    @Override
    public boolean hasNext(int step) {
        return false;
    }

    @Override
    public Step getStep(int step) {
        return null;
    }
    

    @Override
    public IdentityProvider getIdentityProvider(int step)
            throws AuthenticationHandlerException {

        ServiceProviderConfig serviceProvider = getAuthenticationContext().getServiceProvider();
        AuthenticationConfig authenticationConfig = serviceProvider.getAuthenticationConfig();
        AuthenticationStepConfig authenticationStepConfig = authenticationConfig.getAuthenticationStepConfigs()
                .get(step);
        return authenticationStepConfig.getIdentityProviders().get(1);
    }

}
