package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler
        .AbstractProtocolRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.HandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.ResponseHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.AuthenticationHandler;
import org.wso2.carbon.identity.base.IdentityException;

public class LoginRequestProcessor extends IdentityProcessor{


    @Override
    public IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest) throws FrameworkException {

        IdentityMessageContext identityMessageContext = null ; //read from cache, otherwise throw exception.
        IdentityResponse.IdentityResponseBuilder identityResponseBuilder = null ;
        try {
            FrameworkHandlerStatus identityFrameworkHandlerStatus = validate(identityMessageContext);
            if(identityFrameworkHandlerStatus.equals(FrameworkHandlerStatus.REDIRECT)){
                identityResponseBuilder = identityFrameworkHandlerStatus.getIdentityResponseBuilder();
            }else if(identityFrameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)){
                identityFrameworkHandlerStatus = authenticate(identityMessageContext);
                if(identityFrameworkHandlerStatus.equals(FrameworkHandlerStatus.REDIRECT)){
                    identityResponseBuilder = identityFrameworkHandlerStatus.getIdentityResponseBuilder();
                }
            }
        } catch (AuthenticationException e) {
            identityResponseBuilder = buildErrorResponse(e, identityMessageContext);
        }
        return identityResponseBuilder;
    }

    protected FrameworkHandlerStatus validate(IdentityMessageContext identityMessageContext)
            throws AuthenticationException {
        AbstractProtocolRequestHandler protocolRequestHandler =
                HandlerManager.getInstance().getProtocolRequestHandler(identityMessageContext);
        return protocolRequestHandler.validate(identityMessageContext);
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
