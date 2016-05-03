package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerStatus;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler
        .FrameworkHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.HandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.extension
        .ExtensionHandlerPoints;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.response
        .AbstractResponseHandler;

public class AuthenticationRequestProcessor extends AbstractRequestProcessor {


    public IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest) throws FrameworkException {

        IdentityMessageContext identityMessageContext = null; //read from cache, otherwise throw exception.

        FrameworkHandlerStatus frameworkHandlerStatus = null;
        try {
            frameworkHandlerStatus = authenticate(identityMessageContext);
            if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {
                frameworkHandlerStatus = buildResponse(identityMessageContext);
            }

        } catch (AuthenticationHandlerException e) {
            frameworkHandlerStatus = doBuildErrorResponse(e, identityMessageContext);
        }
        return frameworkHandlerStatus.getIdentityResponseBuilder();
    }


    protected FrameworkHandlerStatus doBuildResponse(IdentityMessageContext identityMessageContext)
            throws FrameworkHandlerException {
        FrameworkHandlerStatus frameworkHandlerStatus = null;

        frameworkHandlerStatus = doPreHandle(ExtensionHandlerPoints.RESPONSE_HANDLER, identityMessageContext);
        if (frameworkHandlerStatus.equals(FrameworkHandlerStatus.CONTINUE)) {

            frameworkHandlerStatus = buildResponse(identityMessageContext);
        }
        return frameworkHandlerStatus;


    }

    protected FrameworkHandlerStatus buildResponse(IdentityMessageContext identityMessageContext)
            throws FrameworkHandlerException {
        AbstractResponseHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseHandler(identityMessageContext);
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
