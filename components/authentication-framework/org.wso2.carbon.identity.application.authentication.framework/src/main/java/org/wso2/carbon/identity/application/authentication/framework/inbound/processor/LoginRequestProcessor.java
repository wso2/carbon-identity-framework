package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.request.AbstractRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.HandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.AuthenticationHandlerException;

public class LoginRequestProcessor extends AbstractRequestProcessor{


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
        } catch (AuthenticationHandlerException e) {
           // identityResponseBuilder = buildErrorResponse(e, identityMessageContext);
        }
        return identityResponseBuilder;
    }

    protected FrameworkHandlerStatus validate(IdentityMessageContext identityMessageContext)
            throws AuthenticationHandlerException {
        AbstractRequestHandler protocolRequestHandler =
                HandlerManager.getInstance().getProtocolRequestHandler(identityMessageContext);
        return protocolRequestHandler.validate(identityMessageContext);
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
