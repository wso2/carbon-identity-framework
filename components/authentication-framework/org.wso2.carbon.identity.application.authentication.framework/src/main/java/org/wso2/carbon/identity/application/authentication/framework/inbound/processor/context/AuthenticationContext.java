package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.context;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .context.SequenceContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.Sequence;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.ServiceProviderConfig;

import java.util.Map;

public class AuthenticationContext extends IdentityMessageContext {

    private ServiceProviderConfig serviceProviderConfig = null;
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

    public ServiceProviderConfig getServiceProviderConfig() {
        if (this.serviceProviderConfig == null) {
            synchronized (this) {
                AuthenticationRequest authenticationRequest = (AuthenticationRequest) getIdentityRequest();
                this.serviceProviderConfig =
                        new ServiceProviderConfig(authenticationRequest.getTenantDomain(), authenticationRequest
                                .getRequestType(), authenticationRequest.getClientId());
            }
        }

        return this.serviceProviderConfig;
    }
}
