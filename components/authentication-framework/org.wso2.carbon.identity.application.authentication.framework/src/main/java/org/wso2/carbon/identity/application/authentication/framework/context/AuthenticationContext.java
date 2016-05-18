package org.wso2.carbon.identity.application.authentication.framework.context;


import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCache;
import org.wso2.carbon.identity.application.authentication.framework.processor.request.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl.model.AbstractSequence;
import org.wso2.carbon.identity.application.authentication.framework.processor.handler.authentication.impl
        .util.Utility;
import org.wso2.carbon.identity.application.authentication.framework.processor.request
        .ClientAuthenticationRequest;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.Map;

public class AuthenticationContext extends IdentityMessageContext {

    protected AuthenticationRequest initialAuthenticationRequest ;

    private AbstractSequence abstractSequence = null;
    private SequenceContext sequenceContext = null;

    public AuthenticationContext(
            AuthenticationRequest authenticationRequest,
            Map parameters) {
        super(authenticationRequest, parameters);
        this.initialAuthenticationRequest = authenticationRequest;
    }

    public AuthenticationContext(
            AuthenticationRequest authenticationRequest) {
        super(authenticationRequest);
    }

    public AuthenticationRequest getInitialAuthenticationRequest() {
        return initialAuthenticationRequest;
    }

    public SessionContext getSessionContext() {
        AuthenticationRequest authenticationRequest = (AuthenticationRequest) getIdentityRequest();
        String browserCookieValue = authenticationRequest.getBrowserCookieValue();
        return SessionContextCache.getInstance().getValueFromCache(browserCookieValue);
    }



    public SequenceContext getSequenceContext() {
        return sequenceContext;
    }

    public void setSequenceContext(
            SequenceContext sequenceContext) {
        this.sequenceContext = sequenceContext;
    }

    public AbstractSequence getAbstractSequence() {
        return abstractSequence;
    }

    public void setAbstractSequence(
            AbstractSequence abstractSequence) {
        this.abstractSequence = abstractSequence;
    }

    public ServiceProvider getServiceProvider() throws AuthenticationHandlerException {
        ClientAuthenticationRequest clientAuthenticationRequest  = (ClientAuthenticationRequest) getInitialAuthenticationRequest();
        ServiceProvider serviceProvider =
                Utility.getServiceProvider(clientAuthenticationRequest.getRequestType(), clientAuthenticationRequest
                        .getClientId(), clientAuthenticationRequest.getTenantDomain());
        return serviceProvider;
    }
}
