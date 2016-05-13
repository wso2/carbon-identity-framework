package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.context;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .context.SequenceContext;

import java.util.Map;

public class AuthenticationContext extends IdentityMessageContext{

    private transient SessionContext sessionContext = null ;
    private SequenceContext sequenceContext = null ;

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
}
