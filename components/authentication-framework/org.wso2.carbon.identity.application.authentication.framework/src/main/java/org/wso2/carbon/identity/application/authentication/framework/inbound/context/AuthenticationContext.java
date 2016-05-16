package org.wso2.carbon.identity.application.authentication.framework.inbound.context;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.cache.SessionContextCache;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.request.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.Sequence;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .util.Utility;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.request
        .ClientAuthenticationRequest;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.Map;

public class AuthenticationContext extends IdentityMessageContext {

    protected AuthenticationRequest initialAuthenticationRequest ;

    private Sequence sequence = null;
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

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(
            Sequence sequence) {
        this.sequence = sequence;
    }

    public ServiceProvider getServiceProvider() throws AuthenticationHandlerException {
        ClientAuthenticationRequest clientAuthenticationRequest  = (ClientAuthenticationRequest) getInitialAuthenticationRequest();
        ServiceProvider serviceProvider =
                Utility.getServiceProvider(clientAuthenticationRequest.getRequestType(), clientAuthenticationRequest
                        .getClientId(), clientAuthenticationRequest.getTenantDomain());
        return serviceProvider;
    }
}
