package org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl.model;

import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;

import java.io.Serializable;

public class Step implements Serializable{

    private AuthenticationStep authenticationStep = null ;

    public AuthenticationStep getAuthenticationStep() {
        return authenticationStep;
    }

    public void setAuthenticationStep(
            AuthenticationStep authenticationStep) {
        this.authenticationStep = authenticationStep;
    }

    public boolean isMultiOption(){
        AuthenticationStep authenticationStep = getAuthenticationStep();
        int localAuthenticatorsCount = authenticationStep.getLocalAuthenticatorConfigs().length;
        int federatedIdentityProviders = authenticationStep.getFederatedIdentityProviders().length;

        if((localAuthenticatorsCount + federatedIdentityProviders) > 1){
            return true ;
        }
        return false ;
    }
}
