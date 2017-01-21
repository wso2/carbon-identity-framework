package org.wso2.carbon.identity.gateway.processor.handler.authentication.impl;

import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.AbstractSequence;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.util.HandlerManager;

public class SequenceManager extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    public AuthenticationResponse handleSequence(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationResponse authenticationResponse = null;
        AbstractSequence abstractSequence = authenticationContext.getSequence();
        if (abstractSequence.isRequestPathAuthenticatorsAvailable()) {
            authenticationResponse = handleRequestPathAuthentication(authenticationContext);
        }
        if (authenticationResponse == null && abstractSequence.isStepAuthenticatorAvailable()) {
            authenticationResponse = handleStepAuthentication(authenticationContext);
        }

        return authenticationResponse;
    }

    protected AuthenticationResponse handleRequestPathAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        RequestPathHandler requestPathHandler =
                HandlerManager.getInstance().getRequestPathHandler(authenticationContext);
        return requestPathHandler.handleRequestPathAuthentication(authenticationContext);
    }


    protected AuthenticationResponse handleStepAuthentication(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        StepHandler stepHandler = HandlerManager.getInstance().getStepHandler(authenticationContext);
        return stepHandler.handleStepAuthentication(authenticationContext);
    }

    @Override
    public boolean canHandle(MessageContext messageContext) {
        return true ;
    }
}
