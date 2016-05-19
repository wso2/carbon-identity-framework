package org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl
        .model;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import java.io.Serializable;

public abstract class AbstractSequence implements Serializable {

    private transient AuthenticationContext authenticationContext = null;

    protected AbstractSequence(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public AuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }

    protected AbstractSequence() {

    }
    public abstract RequestPathAuthenticatorConfig[] getRequestPathAuthenticatorConfig() ;

    public abstract AuthenticationStep[] getStepAuthenticatorConfig();

    public abstract boolean isRequestPathAuthenticatorsAvailable();

    public abstract boolean isStepAuthenticatorAvailable();

    public abstract boolean getAuthenticatorConfig(int step,String authenticatorName);

    public abstract boolean getLocalAuthenticatorConfig(int step,String authenticatorName);

    public abstract FederatedAuthenticatorConfig getFederatedAuthenticatorConfig(int step,String authenticatorName);
}
