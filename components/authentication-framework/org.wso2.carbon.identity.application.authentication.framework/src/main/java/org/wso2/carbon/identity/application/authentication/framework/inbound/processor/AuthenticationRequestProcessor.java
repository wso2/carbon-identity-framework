package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityFrameworkHandler;
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

public class AuthenticationRequestProcessor extends IdentityProcessor {

    @Override
    public IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest) throws FrameworkException {

        IdentityMessageContext identityMessageContext = null ; //read from cache, otherwise throw exception.

        IdentityResponse.IdentityResponseBuilder identityResponseBuilder = doAuthenticate(identityMessageContext);
        if(identityResponseBuilder != null){
            return identityResponseBuilder ;
        }


        return null;
    }

    protected IdentityResponse.IdentityResponseBuilder doAuthenticate(IdentityMessageContext identityMessageContext){
        AuthenticationHandler authenticationHandler =
                IdentityFrameworkHandlerManager.getInstance().getAuthenticationHandler(identityMessageContext);
        try {
            authenticationHandler.authenticate(identityMessageContext);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
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
