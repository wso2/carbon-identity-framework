package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl;

import org.wso2.carbon.identity.application.authentication.framework.inbound.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.Sequence;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;

public class SequenceManager extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    public AuthenticationResponse handleSequence(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = null ;
        Sequence sequence = authenticationContext.getSequence();
        if(sequence.isRequestPathAuthenticatorsAvailable) {
            authenticationResponse = handleRequestPathAuthentication(authenticationContext);
        }
        //should check condition
        authenticationResponse = handleStepAuthentication(authenticationContext);

        return authenticationResponse;
    }

    protected AuthenticationResponse handleRequestPathAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        Sequence sequence = authenticationContext.getSequence();
        RequestPathAuthenticatorConfig[] requestPathAuthenticatorConfig = sequence.getRequestPathAuthenticatorConfig();


        return null;
    }

    protected AuthenticationResponse handleStepAuthentication(AuthenticationContext authenticationContext) {

        return null;
    }


}
