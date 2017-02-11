package org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model;

import org.wso2.carbon.identity.gateway.common.model.AuthenticationStep;
import org.wso2.carbon.identity.gateway.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;

import java.io.Serializable;

public abstract class AbstractSequence implements Serializable {

    private transient AuthenticationContext authenticationContext = null;

    protected AbstractSequence(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    protected AbstractSequence() {

    }

    public AuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }

    public abstract RequestPathAuthenticatorConfig[] getRequestPathAuthenticatorConfig();

    public abstract AuthenticationStep[] getStepAuthenticatorConfig();

    public abstract boolean isRequestPathAuthenticatorsAvailable();

    public abstract boolean isStepAuthenticatorAvailable();

    public abstract boolean hasNext(int step);

    public abstract Step getStep(int step);


    public abstract IdentityProvider getIdentityProvider(int step)
            throws AuthenticationHandlerException;


}
