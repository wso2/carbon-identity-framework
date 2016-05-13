package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.model;


import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Sequence implements Serializable{

    private ServiceProviderConfig serviceProviderConfig = null ;

    public Sequence(ServiceProviderConfig serviceProviderConfig)
            throws IdentityApplicationManagementException {
        this.serviceProviderConfig = serviceProviderConfig;
    }

    public RequestPathAuthenticatorConfig[] getRequestPathAuthenticatorConfig() throws AuthenticationHandlerException {
        RequestPathAuthenticatorConfig[] requestPathAuthenticatorConfigs =
                serviceProviderConfig.getServiceProvider().getRequestPathAuthenticatorConfigs();
        return requestPathAuthenticatorConfigs ;
    }


    public AuthenticationStep[] getStepAuthenticatorConfig() throws AuthenticationHandlerException {
        AuthenticationStep[] authenticationSteps =
                serviceProviderConfig.getServiceProvider().getLocalAndOutBoundAuthenticationConfig()
                        .getAuthenticationSteps();

        return authenticationSteps ;
    }

}
