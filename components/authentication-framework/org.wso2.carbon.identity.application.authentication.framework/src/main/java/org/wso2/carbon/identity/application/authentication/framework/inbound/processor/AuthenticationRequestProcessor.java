package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.HandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.ResponseHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandler;
import org.wso2.carbon.identity.base.IdentityException;

public class AuthenticationRequestProcessor extends IdentityProcessor {

    @Override
    public IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest) throws FrameworkException {

        IdentityMessageContext identityMessageContext = null ; //read from cache, otherwise throw exception.
        IdentityResponse.IdentityResponseBuilder identityResponseBuilder = null ;
        try {
            FrameworkHandlerStatus identityFrameworkHandlerStatus = authenticate(identityMessageContext);
            if(identityFrameworkHandlerStatus.equals(FrameworkHandlerStatus.REDIRECT)){
                identityResponseBuilder = identityFrameworkHandlerStatus.getIdentityResponseBuilder();
            }else if(identityFrameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)){
                identityResponseBuilder = buildResponse(identityMessageContext);
            }
        } catch (AuthenticationException e) {
            identityResponseBuilder = buildErrorResponse(e, identityMessageContext);
        }
        return identityResponseBuilder;
    }

    protected FrameworkHandlerStatus authenticate(IdentityMessageContext identityMessageContext)
            throws AuthenticationException {
        AuthenticationHandler authenticationHandler =
                HandlerManager.getInstance().getAuthenticationHandler(identityMessageContext);
        return authenticationHandler.authenticate(identityMessageContext);
    }

    protected IdentityResponse.IdentityResponseBuilder buildErrorResponse(IdentityException e, IdentityMessageContext identityMessageContext){
        ResponseHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseBuilderHandler(identityMessageContext);
        return responseBuilderHandler.buildErrorResponse(identityMessageContext);
    }

    protected IdentityResponse.IdentityResponseBuilder buildResponse(IdentityMessageContext identityMessageContext){
        ResponseHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseBuilderHandler(identityMessageContext);
        return responseBuilderHandler.buildResponse(identityMessageContext);
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
