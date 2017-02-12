package org.wso2.carbon.identity.gateway.processor.handler.authentication;


import org.wso2.carbon.identity.gateway.common.model.AuthenticationStep;
import org.wso2.carbon.identity.gateway.common.model.idp.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.AuthenticationStepConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.common.model.sp.ServiceProviderConfig;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.AbstractSequence;

import java.util.List;

public class DefaultAbstractSequence extends AbstractSequence {

    public DefaultAbstractSequence(AuthenticationContext authenticationContext) {
        super(authenticationContext);
    }

    public DefaultAbstractSequence() {
    }

    @Override
    public List<RequestPathAuthenticatorConfig> getRequestPathAuthenticatorConfig() {
        return null;
    }


    @Override
    public boolean isRequestPathAuthenticatorsAvailable() {
        return false;
    }

    @Override
    public boolean isStepAuthenticatorAvailable() throws AuthenticationHandlerException {
        ServiceProviderConfig serviceProvider = getAuthenticationContext().getServiceProvider();
        AuthenticationConfig authenticationConfig = serviceProvider.getAuthenticationConfig();
        if(authenticationConfig.getAuthenticationStepConfigs() != null && authenticationConfig
                .getAuthenticationStepConfigs().size() > 0){
            return true ;
        }
        return false;
    }

    @Override
    public boolean hasNext(int currentStep) throws AuthenticationHandlerException {
        ServiceProviderConfig serviceProvider = getAuthenticationContext().getServiceProvider();
        AuthenticationConfig authenticationConfig = serviceProvider.getAuthenticationConfig();
        List<AuthenticationStepConfig> authenticationStepConfigs = authenticationConfig.getAuthenticationStepConfigs();
        if(authenticationStepConfigs.size() >= (currentStep + 1)){
            return true;
        }
        return false;
    }

    @Override
    public boolean isMultiOption(int step) throws AuthenticationHandlerException {
        AuthenticationStepConfig authenticationStepConfig = getAuthenticationStepConfig(step);
        List<IdentityProvider> identityProviders = authenticationStepConfig.getIdentityProviders();
        if(identityProviders.size() > 1){
            return true ;
        }
        return false;
    }


    @Override
    public IdentityProvider getIdentityProvider(int step, String identityProviderName)
            throws AuthenticationHandlerException {
        IdentityProvider identityProviderTmp = null ;
        AuthenticationStepConfig authenticationStepConfig = getAuthenticationStepConfig(step);
        List<IdentityProvider> identityProviders = authenticationStepConfig.getIdentityProviders();
        for(IdentityProvider identityProvider : identityProviders){
            if(identityProvider.getIdentityProviderName().equals(identityProviderName)){
                identityProviderTmp = identityProvider ;
            }
        }
        return identityProviderTmp ;
    }

    private AuthenticationStepConfig getAuthenticationStepConfig(int step) throws AuthenticationHandlerException {
        ServiceProviderConfig serviceProvider = getAuthenticationContext().getServiceProvider();
        AuthenticationConfig authenticationConfig = serviceProvider.getAuthenticationConfig();
        AuthenticationStepConfig authenticationStepConfig = authenticationConfig.getAuthenticationStepConfigs()
                .get(step);
        return authenticationStepConfig;
    }

}
