package org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model;
 
import org.wso2.carbon.identity.gateway.common.model.idp.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.IdentityProvider;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;

import java.io.Serializable;
import java.util.List;

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

    public abstract List<RequestPathAuthenticatorConfig> getRequestPathAuthenticatorConfig();

    public abstract boolean isRequestPathAuthenticatorsAvailable();

    public abstract boolean isStepAuthenticatorAvailable() throws AuthenticationHandlerException;

    public abstract boolean hasNext(int currentStep) throws AuthenticationHandlerException;

    public abstract boolean isMultiOption(int step) throws AuthenticationHandlerException;


    public abstract IdentityProvider getIdentityProvider(int step)
            throws AuthenticationHandlerException;


}
