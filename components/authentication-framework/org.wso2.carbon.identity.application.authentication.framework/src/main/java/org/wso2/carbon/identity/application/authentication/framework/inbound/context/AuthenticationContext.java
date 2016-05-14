package org.wso2.carbon.identity.application.authentication.framework.inbound.context;


import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.Sequence;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .util.Utility;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.Map;

public class AuthenticationContext extends IdentityMessageContext {

    private transient ServiceProvider serviceProvider = null;

    private transient Sequence sequence = null;
    private transient SessionContext sessionContext = null;
    private SequenceContext sequenceContext = null;

    public AuthenticationContext(
            AuthenticationRequest authenticationRequest,
            Map parameters) {
        super(authenticationRequest, parameters);
    }

    public AuthenticationContext(
            AuthenticationRequest authenticationRequest) {
        super(authenticationRequest);
    }

    public SessionContext getSessionContext() {
        return sessionContext;
    }

    public void setSessionContext(
            SessionContext sessionContext) {
        this.sessionContext = sessionContext;
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
        if (this.serviceProvider == null) {
            synchronized (this) {
                AuthenticationRequest authenticationRequest = (AuthenticationRequest) getIdentityRequest();
                ServiceProvider serviceProvider =
                        Utility.getServiceProvider(authenticationRequest.getRequestType(), authenticationRequest
                                .getClientId(), authenticationRequest.getTenantDomain());
            }
        }

        return this.serviceProvider;
    }
}
