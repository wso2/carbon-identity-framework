package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler
        .IdentityFrameworkHandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandler;
import org.wso2.carbon.identity.base.IdentityException;

public class AuthenticationRequestProcessor extends IdentityProcessor {

    @Override
    public IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest) throws FrameworkException {

        IdentityMessageContext identityMessageContext = null ; //read from cache, otherwise throw exception.

        try {
            FrameworkHandlerStatus identityFrameworkHandlerStatus = doAuthenticate(identityMessageContext);

        } catch (AuthenticationException e) {
            handleException(e, identityMessageContext);
        }

        return null;
    }

    protected FrameworkHandlerStatus doAuthenticate(IdentityMessageContext identityMessageContext)
            throws AuthenticationException {
        AuthenticationHandler authenticationHandler =
                IdentityFrameworkHandlerManager.getInstance().getAuthenticationHandler(identityMessageContext);
        return authenticationHandler.authenticate(identityMessageContext);
    }

    protected void handleException(IdentityException e, IdentityMessageContext identityMessageContext){

    }

    @Override
    public String getName() {
        return null;
    }


    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean canHandle(IdentityRequest identityRequest) {
        return false;
    }
}
