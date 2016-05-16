package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model;

import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import java.io.Serializable;

public class Sequence implements Serializable {

    private transient ServiceProvider serviceProvider = null;
    public boolean isRequestPathAuthenticatorsAvailable = false;

    public Sequence(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public RequestPathAuthenticatorConfig[] getRequestPathAuthenticatorConfig() throws AuthenticationHandlerException {
        RequestPathAuthenticatorConfig[] requestPathAuthenticatorConfigs =
                serviceProvider.getRequestPathAuthenticatorConfigs();
        return requestPathAuthenticatorConfigs;
    }

    public AuthenticationStep[] getStepAuthenticatorConfig() {
        AuthenticationStep[] authenticationSteps =
                serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                        .getAuthenticationSteps();
        return authenticationSteps;
    }

    public boolean isRequestPathAuthenticatorsAvailable(){
        AuthenticationStep[] stepAuthenticatorConfig = getStepAuthenticatorConfig();
        if(stepAuthenticatorConfig != null && stepAuthenticatorConfig.length > 0){
            return true ;
        }
        return false ;
    }
}
