package org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl
        .model;

import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import java.io.Serializable;

public abstract class AbstractSequence implements Serializable {

    private transient ServiceProvider serviceProvider = null;
    public boolean isRequestPathAuthenticatorsAvailable = false;

    protected AbstractSequence(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }
    protected AbstractSequence() {
    }
    public abstract RequestPathAuthenticatorConfig[] getRequestPathAuthenticatorConfig() ;

    public abstract AuthenticationStep[] getStepAuthenticatorConfig();

    public abstract boolean isRequestPathAuthenticatorsAvailable();

    public abstract boolean getAuthenticatorProperties(String authenticatorName);
}
