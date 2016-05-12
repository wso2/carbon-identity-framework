package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.handler;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .AuthenticationResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .HandlerManager;

public class SequenceHandler extends FrameworkHandler {
    @Override
    public String getName() {
        return null;
    }

    public AuthenticationResponse handleSequence(IdentityMessageContext identityMessageContext){
        AuthenticationResponse authenticationResponse = handleRequestPathAuthentication(identityMessageContext);
        //should check condition
        authenticationResponse = handleStepAuthentication(identityMessageContext);

        return authenticationResponse ;
    }

    protected AuthenticationResponse handleRequestPathAuthentication(IdentityMessageContext identityMessageContext){

        RequestPathAuthenticationHandler requestPathAuthenticationHandler =
                HandlerManager.getInstance().getRequestPathProcessHandler(identityMessageContext);
        AuthenticationResponse authenticationResponse =
                requestPathAuthenticationHandler.handleAuthentication(identityMessageContext);
        return authenticationResponse ;
    }

    protected AuthenticationResponse handleStepAuthentication(IdentityMessageContext identityMessageContext){

        StepAuthenticationHandler stepAuthenticationHandler =
                HandlerManager.getInstance().getStepProcessHandler(identityMessageContext);
        AuthenticationResponse authenticationResponse =
                stepAuthenticationHandler.handleAuthentication(identityMessageContext);
        return authenticationResponse;

    }
}
