package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.HandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.ResponseBuilderHandler;
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
            FrameworkHandlerStatus identityFrameworkHandlerStatus = authenticate(identityMessageContext);
            if(identityFrameworkHandlerStatus.equals(FrameworkHandlerStatus.REDIRECT)){
                return buildRedirectResponse(identityMessageContext);
            }
            return buildResponse(identityMessageContext);
        } catch (AuthenticationException e) {
            return buildErrorResponse(e, identityMessageContext);
        }
    }

    protected FrameworkHandlerStatus authenticate(IdentityMessageContext identityMessageContext)
            throws AuthenticationException {
        AuthenticationHandler authenticationHandler =
                HandlerManager.getInstance().getAuthenticationHandler(identityMessageContext);
        return authenticationHandler.authenticate(identityMessageContext);
    }

    protected IdentityResponse.IdentityResponseBuilder buildErrorResponse(IdentityException e, IdentityMessageContext identityMessageContext){
        ResponseBuilderHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseBuilderHandler(identityMessageContext);
        return responseBuilderHandler.buildErrorResponse(identityMessageContext);
    }

    protected IdentityResponse.IdentityResponseBuilder buildRedirectResponse(IdentityMessageContext identityMessageContext){
        ResponseBuilderHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseBuilderHandler(identityMessageContext);
        return responseBuilderHandler.buildRedirectResponse(identityMessageContext);
    }

    protected IdentityResponse.IdentityResponseBuilder buildResponse(IdentityMessageContext identityMessageContext){
        ResponseBuilderHandler responseBuilderHandler =
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
